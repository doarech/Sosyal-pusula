package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.CompassViewModel
import com.example.ui.screens.CheckInScreen
import com.example.ui.screens.DigitalBalanceScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.SosyalPusulaTheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val viewModel: CompassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val userDarkModePref by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = userDarkModePref ?: systemDark

            SosyalPusulaTheme(darkTheme = isDark) {
                MainAppScaffold(
                    viewModel = viewModel,
                    isDarkTheme = isDark
                )
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: CompassViewModel,
    isDarkTheme: Boolean
) {
    var currentScreen by remember { mutableStateOf("home") }

    val showBottomBar = currentScreen == "home" || currentScreen == "stats"

    if (currentScreen == "stats") {
        BackHandler { currentScreen = "home" }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "home",
                        onClick = { currentScreen = "home" },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == "home") Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Ana Sayfa"
                            )
                        },
                        label = { Text("Ana Sayfa") },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_home_button")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "stats",
                        onClick = { currentScreen = "stats" },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == "stats") Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                contentDescription = "İstatistikler"
                            )
                        },
                        label = { Text("İstatistikler") },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_stats_button")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier.padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onStartCheckIn = {
                        viewModel.loadDynamicQuestions()
                        currentScreen = "checkin"
                    },
                    onViewResult = { currentScreen = "result" },
                    onOpenSettings = { currentScreen = "settings" },
                    isDarkTheme = isDarkTheme
                )

                "stats" -> StatsScreen(
                    viewModel = viewModel
                )

                "settings" -> SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "home" },
                    onNavigateToHistory = { currentScreen = "history" },
                    onNavigateToDigitalBalance = { currentScreen = "digital_balance" },
                    isDarkTheme = isDarkTheme
                )

                "history" -> HistoryScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "settings" }
                )

                "digital_balance" -> DigitalBalanceScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "settings" }
                )

                "checkin" -> CheckInScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "home" },
                    onFinished = { currentScreen = "result" }
                )

                "result" -> ResultDetailScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "home" }
                )
            }
        }
    }
}
