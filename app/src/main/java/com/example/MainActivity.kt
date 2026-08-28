package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CameraScannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SessionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentRegistrationScreen
import com.example.ui.theme.AttendanceSystemTheme
import com.example.ui.theme.MinimalistBackground
import com.example.ui.theme.MinimalistPurpleContainer
import com.example.ui.theme.MinimalistPurplePrimary
import com.example.ui.theme.MinimalistPurpleText
import com.example.ui.theme.MinimalistSurface
import com.example.ui.theme.MinimalistTextMuted
import com.example.ui.theme.MinimalistTextPrimary
import com.example.viewmodel.AttendanceViewModel

enum class NavigationScreen(val route: String, val title: String, val icon: ImageVector) {
    SCANNER("scanner", "Scanner", Icons.Default.CameraAlt),
    REGISTRATION("registration", "Students", Icons.Default.PersonAdd),
    SESSIONS("sessions", "Sessions", Icons.Default.Tune),
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Assessment),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AttendanceSystemTheme {
                MainAppStructure(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppStructure(viewModel: AttendanceViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val toastMessage by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationScreen.SCANNER.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MinimalistBackground,
        bottomBar = {
            NavigationBar(
                containerColor = MinimalistSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_navigation")
            ) {
                NavigationScreen.entries.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MinimalistPurpleText,
                            selectedTextColor = MinimalistPurplePrimary,
                            indicatorColor = MinimalistPurpleContainer,
                            unselectedIconColor = MinimalistTextMuted,
                            unselectedTextColor = MinimalistTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavigationScreen.SCANNER.route
            ) {
                composable(NavigationScreen.SCANNER.route) {
                    CameraScannerScreen(
                        viewModel = viewModel,
                        onNavigateToRegistration = {
                            navController.navigate(NavigationScreen.REGISTRATION.route)
                        }
                    )
                }
                composable(NavigationScreen.REGISTRATION.route) {
                    StudentRegistrationScreen(viewModel = viewModel)
                }
                composable(NavigationScreen.SESSIONS.route) {
                    SessionsScreen(viewModel = viewModel)
                }
                composable(NavigationScreen.DASHBOARD.route) {
                    DashboardScreen(viewModel = viewModel)
                }
                composable(NavigationScreen.SETTINGS.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
