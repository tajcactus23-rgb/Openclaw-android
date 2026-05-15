package com.openhands.android.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openhands.android.presentation.ui.screens.AgentCanvasScreen
import com.openhands.android.presentation.ui.screens.DashboardScreen
import com.openhands.android.presentation.ui.screens.FilesScreen
import com.openhands.android.presentation.ui.screens.GitRepoScreen
import com.openhands.android.presentation.ui.screens.NotificationsScreen
import com.openhands.android.presentation.ui.screens.PromptScreen
import com.openhands.android.presentation.ui.screens.RuntimeMonitorScreen
import com.openhands.android.presentation.ui.screens.SettingsScreen
import com.openhands.android.presentation.ui.screens.SessionsScreen
import com.openhands.android.presentation.ui.screens.SkillsScreen
import com.openhands.android.presentation.ui.screens.ThemeScreen
import com.openhands.android.presentation.ui.screens.ToolManagerScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Sessions : Screen("sessions", "Sessions", Icons.Default.Terminal)
    data object Prompt : Screen("prompt", "Prompt", Icons.AutoMirrored.Filled.Message)
    data object Skills : Screen("skills", "Skills", Icons.Default.Code)
    data object Files : Screen("files", "Files", Icons.Default.Folder)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Notifications : Screen("notifications", "Notify", Icons.Default.Check)
    data object ToolManager : Screen("tools", "Tools", Icons.Default.Cloud)
    data object AgentCanvas : Screen("canvas", "Canvas", Icons.Default.Groups)
    data object GitRepo : Screen("git", "Git", Icons.Default.Cloud)
    data object RuntimeMonitor : Screen("runtime", "Runtime", Icons.Default.History)
    data object Theme : Screen("theme", "Theme", Icons.Default.Palette)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Sessions,
    Screen.Prompt,
    Screen.Skills,
    Screen.Files,
    Screen.Settings
)

@Composable
fun OpenHandsNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Sessions.route) { SessionsScreen() }
            composable(Screen.Prompt.route) { PromptScreen() }
            composable(Screen.Skills.route) { SkillsScreen() }
            composable(Screen.Files.route) { FilesScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Notifications.route) { NotificationsScreen() }
            composable(Screen.ToolManager.route) { ToolManagerScreen() }
            composable(Screen.AgentCanvas.route) { AgentCanvasScreen() }
            composable(Screen.GitRepo.route) { GitRepoScreen() }
            composable(Screen.RuntimeMonitor.route) { RuntimeMonitorScreen() }
            composable(Screen.Theme.route) { ThemeScreen() }
        }
    }
}
