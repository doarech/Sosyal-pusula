package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiService
import com.example.data.ai.DynamicQuestion
import com.example.data.local.CheckInEntity
import com.example.data.local.CompassDatabase
import com.example.data.local.DailyTaskEntity
import com.example.data.repository.CompassRepository
import com.example.util.UsageStatsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val PREFS_NAME = "sosyal_pusula_prefs"
private const val KEY_SHOW_MOTIVATION = "key_show_motivation_card"
private const val KEY_DARK_MODE = "key_dark_mode"
private const val KEY_SELECTED_LOGO = "key_selected_logo_id"

class CompassViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CompassRepository
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val allCheckIns: StateFlow<List<CheckInEntity>>
    val latestCheckIn: StateFlow<CheckInEntity?>
    val allTasks: StateFlow<List<DailyTaskEntity>>

    private val _selectedLogoId = MutableStateFlow(
        prefs.getInt(KEY_SELECTED_LOGO, 1)
    )
    val selectedLogoId: StateFlow<Int> = _selectedLogoId.asStateFlow()

    fun setSelectedLogo(conceptId: Int) {
        _selectedLogoId.value = conceptId
        prefs.edit().putInt(KEY_SELECTED_LOGO, conceptId).apply()
    }

    private val _dynamicQuestions = MutableStateFlow<List<DynamicQuestion>>(emptyList())
    val dynamicQuestions: StateFlow<List<DynamicQuestion>> = _dynamicQuestions.asStateFlow()

    private val _isQuestionsLoading = MutableStateFlow(false)
    val isQuestionsLoading: StateFlow<Boolean> = _isQuestionsLoading.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    private val _recentAnalysisResult = MutableStateFlow<CheckInEntity?>(null)
    val recentAnalysisResult: StateFlow<CheckInEntity?> = _recentAnalysisResult.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _showMotivationCard = MutableStateFlow(
        prefs.getBoolean(KEY_SHOW_MOTIVATION, true)
    )
    val showMotivationCard: StateFlow<Boolean> = _showMotivationCard.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _autoDetectedScreenHours = MutableStateFlow<Float?>(null)
    val autoDetectedScreenHours: StateFlow<Float?> = _autoDetectedScreenHours.asStateFlow()

    init {
        val db = CompassDatabase.getInstance(application)
        val aiService = AiService()
        repository = CompassRepository(db.compassDao(), aiService)

        allCheckIns = repository.allCheckIns.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        latestCheckIn = repository.latestCheckIn.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        allTasks = repository.allTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            try {
                repository.seedInitialDataIfEmpty()
                // Stockpile fresh AI questions in background while user uses app
                repository.prefetchQuestionsInBackground()
            } catch (e: Exception) {
                android.util.Log.e("CompassViewModel", "Failed to initialize and prefetch", e)
            }
        }

        refreshUsageStats()
    }

    fun refreshUsageStats() {
        val context = getApplication<Application>()
        val hasPerm = UsageStatsHelper.hasUsageStatsPermission(context)
        _hasUsagePermission.value = hasPerm
        if (hasPerm) {
            val hours = UsageStatsHelper.getWeeklyDailyAverageScreenHours(context)
            _autoDetectedScreenHours.value = hours ?: 3.5f // Fallback to a realistic baseline if emulator has no app usage
        } else {
            _autoDetectedScreenHours.value = null
        }
    }

    fun openUsageAccessSettings() {
        UsageStatsHelper.openUsageAccessSettings(getApplication())
    }

    fun setShowMotivationCard(show: Boolean) {
        _showMotivationCard.value = show
        prefs.edit().putBoolean(KEY_SHOW_MOTIVATION, show).apply()
    }

    fun toggleDarkMode(systemDark: Boolean) {
        val current = _isDarkMode.value ?: systemDark
        val newVal = !current
        _isDarkMode.value = newVal
        prefs.edit().putBoolean(KEY_DARK_MODE, newVal).apply()
    }

    fun setDarkMode(dark: Boolean?) {
        _isDarkMode.value = dark
        if (dark == null) {
            prefs.edit().remove(KEY_DARK_MODE).apply()
        } else {
            prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply()
        }
    }

    fun loadDynamicQuestions() {
        viewModelScope.launch {
            _isQuestionsLoading.value = true
            _analysisError.value = null
            try {
                val questions = repository.fetchDynamicQuestions()
                _dynamicQuestions.value = questions
            } catch (e: Exception) {
                _analysisError.value = e.localizedMessage ?: "Sorular yüklenirken bir hata oluştu."
            } finally {
                _isQuestionsLoading.value = false
            }
        }
    }

    fun submitCheckIn(
        answers: List<Pair<String, String>>,
        userNotes: String,
        onSuccess: (CheckInEntity) -> Unit
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisError.value = null

            // Determine screen hours directly from the device's usage stats
            refreshUsageStats()
            val screenHours = _autoDetectedScreenHours.value ?: 4.0f

            try {
                val savedCheckIn = repository.saveCheckInWithAi(answers, screenHours, userNotes)
                _recentAnalysisResult.value = savedCheckIn
                onSuccess(savedCheckIn)
            } catch (e: Exception) {
                _analysisError.value = e.localizedMessage ?: "Yapay zeka analizi sırasında bir sorun oluştu."
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun toggleTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, isCompleted)
        }
    }

    fun saveTaskReflection(taskId: Long, reflection: String) {
        viewModelScope.launch {
            repository.saveTaskReflection(taskId, reflection)
        }
    }

    fun deleteCheckIn(id: Long) {
        viewModelScope.launch {
            repository.deleteCheckIn(id)
        }
    }

    fun calculateCorrelation(checkIns: List<CheckInEntity>): Float? {
        return repository.calculateCorrelation(checkIns)
    }

    fun formatWeekLabel(weekString: String): String {
        return repository.formatWeekLabel(weekString)
    }
}
