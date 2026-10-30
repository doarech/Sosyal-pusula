package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CompassViewModel
import com.example.ui.theme.ScreenTimeColor

enum class QuestionAnswerMode {
    OPTIONS,
    WRITING
}

@Composable
fun CheckInScreen(
    viewModel: CompassViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val dynamicQuestions by viewModel.dynamicQuestions.collectAsState()
    val isQuestionsLoading by viewModel.isQuestionsLoading.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisError by viewModel.analysisError.collectAsState()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()
    val autoHours by viewModel.autoDetectedScreenHours.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    val userCustomNotes = remember { mutableStateMapOf<Int, String>() }
    val questionModes = remember { mutableStateMapOf<Int, QuestionAnswerMode>() }
    var userFeelingNote by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.refreshUsageStats()
        viewModel.loadDynamicQuestions()
    }

    if (isAnalyzing) {
        AnalyzingAiLoadingView()
        return
    }

    val totalSteps = if (dynamicQuestions.isEmpty()) 1 else dynamicQuestions.size + 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("checkin_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentStep > 0) currentStep--
                    else onBack()
                },
                modifier = Modifier.testTag("checkin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri"
                )
            }

            // Step dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                for (i in 0 until totalSteps) {
                    val isActive = i <= currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }

            Text(
                text = "${currentStep + 1}/$totalSteps",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isQuestionsLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Yapay zeka sana özel dinamik soruları hazırlıyor...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (currentStep < dynamicQuestions.size) {
            // Dynamic Question Step
            val question = dynamicQuestions[currentStep]
            val currentMode = questionModes[question.id] ?: QuestionAnswerMode.OPTIONS
            val currentAnswer = userAnswers[question.id]
            val customNote = userCustomNotes[question.id] ?: ""
            val canProceed = when (currentMode) {
                QuestionAnswerMode.WRITING -> customNote.trim().isNotBlank()
                QuestionAnswerMode.OPTIONS -> currentAnswer != null
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Scrollable question area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header row: Question badge and single toggle button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Soru ${currentStep + 1}/${dynamicQuestions.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Single button to switch between options & writing mode
                        FilledTonalButton(
                            onClick = {
                                questionModes[question.id] = if (currentMode == QuestionAnswerMode.OPTIONS) {
                                    QuestionAnswerMode.WRITING
                                } else {
                                    QuestionAnswerMode.OPTIONS
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("toggle_answer_mode_button")
                        ) {
                            Icon(
                                imageVector = if (currentMode == QuestionAnswerMode.OPTIONS) Icons.Filled.EditNote else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentMode == QuestionAnswerMode.OPTIONS) "Yazarak Cevapla" else "Şıklardan Seç",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 28.sp
                    )

                    if (currentMode == QuestionAnswerMode.WRITING) {
                        // Free-form writing mode
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.EditNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Kendi Cümlelerinle Yanıtla",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Seçeneklere bağlı kalmadan hislerini serbestçe yaz",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = customNote,
                                    onValueChange = { userCustomNotes[question.id] = it },
                                    placeholder = {
                                        Text(
                                            "Bu soruya dair hislerini veya deneyimini kendi cümlelerinle yaz... Yapay zeka analiz ederken doğrudan dikkate alacaktır.",
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("custom_note_input_$currentStep"),
                                    shape = RoundedCornerShape(12.dp),
                                    minLines = 4,
                                    maxLines = 8
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (customNote.trim().isNotEmpty()) "${customNote.trim().length} karakter yazıldı" else "Cevap yazılması bekleniyor",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (customNote.trim().isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    TextButton(
                                        onClick = { questionModes[question.id] = QuestionAnswerMode.OPTIONS }
                                    ) {
                                        Text("Şıklara Geri Dön", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Multiple Choice Options mode (strictly options only - no write input here)
                        Text(
                            text = "Aşağıdaki seçeneklerden birini seçin:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 5 Options
                        question.options.forEachIndexed { index, option ->
                            val isSelected = currentAnswer == option
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        userAnswers[question.id] = option
                                    }
                                    .testTag("option_${currentStep}_$index")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Fixed Bottom Navigation button to proceed (Always visible above navigation bar)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(
                        onClick = {
                            if (currentStep < totalSteps - 1) {
                                currentStep++
                            }
                        },
                        enabled = canProceed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("next_step_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (currentStep == dynamicQuestions.size - 1) "Son Adıma Geç" else "Sonraki Soru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else {
            // Final Step: Automated Screen Time & Personal Feeling Note
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = ScreenTimeColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            tint = ScreenTimeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Otomatik Ekran Süresi Tespiti",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ScreenTimeColor
                        )
                    }
                }

                Text(
                    text = "Değerlendirmeyi Tamamla",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Automatic Screen Time Card (No manual entry required)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (hasUsagePermission) Icons.Filled.CheckCircle else Icons.Filled.Security,
                                contentDescription = null,
                                tint = if (hasUsagePermission) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = if (hasUsagePermission) "Ekran Süresi Telefondan Alındı" else "Otomatik Okuma Bekleniyor",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (hasUsagePermission) {
                                        "Cihazının son 7 günlük kullanım verileri Android sisteminden doğrudan otomatik olarak okundu."
                                    } else {
                                        "Uygulamanın kullanım süreni sormadan doğrudan telefondan okuyabilmesi için 'Kullanım Erişimi' izni gereklidir."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (hasUsagePermission) {
                            Surface(
                                color = ScreenTimeColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tespit Edilen Ortalama Ekran:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${autoHours ?: 3.5f} saat / gün",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ScreenTimeColor
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.openUsageAccessSettings() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("İzni Sistem Ayarlarından Aç")
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "İzin verilmediğinde ortalama baz değer (4.0 saat) kullanılacaktır.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Duygu Notu (İsteğe Bağlı)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = userFeelingNote,
                    onValueChange = { userFeelingNote = it },
                    placeholder = {
                        Text(
                            "Şu an genel hislerini veya son günlerdeki sosyal deneyimini birkaç cümleyle anlatabilirsin...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feeling_notes_input"),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 3
                )

                analysisError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val formattedAnswers = dynamicQuestions.map { q ->
                            val mode = questionModes[q.id] ?: QuestionAnswerMode.OPTIONS
                            val selected = userAnswers[q.id].orEmpty()
                            val custom = userCustomNotes[q.id]?.trim().orEmpty()
                            val combined = when {
                                mode == QuestionAnswerMode.WRITING && custom.isNotBlank() ->
                                    "Kullanıcı Kendi Cevabı: $custom"
                                selected.isNotBlank() && custom.isNotBlank() ->
                                    "$selected (Kullanıcı Notu: $custom)"
                                custom.isNotBlank() ->
                                    "Kullanıcı Kendi Cevabı: $custom"
                                selected.isNotBlank() ->
                                    selected
                                else ->
                                    q.options.getOrElse(2) { "Kısmen" }
                            }
                            Pair(q.text, combined)
                        }
                        viewModel.submitCheckIn(
                            answers = formattedAnswers,
                            userNotes = userFeelingNote,
                            onSuccess = { onFinished() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("finish_checkin_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Yapay Zeka Analizini Başlat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AnalyzingAiLoadingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "compassSpin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(90.dp)
                ) {}

                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(angle)
                )
            }

            Text(
                text = "Yapay Zeka Analiz Ediyor...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Cevapların, telefondan okunan ekran süren ve duygusal durumun sosyal denge standartlarına göre değerlendiriliyor. Sana özel günlük mikro-eylem görevleri hazırlanıyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
