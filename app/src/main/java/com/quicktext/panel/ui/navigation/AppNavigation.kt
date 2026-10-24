package com.quicktext.panel.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quicktext.panel.ui.dashboard.DashboardScreen
import com.quicktext.panel.ui.lab.TestLabScreen
import com.quicktext.panel.ui.privacy.PrivacyScreen
import com.quicktext.panel.ui.setup.SetupScreen
import com.quicktext.panel.ui.templates.TemplatesScreen

sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Dest("home", "Home", Icons.Default.Home)
    data object Templates : Dest("templates", "Templates", Icons.Default.Menu)
    data object Setup : Dest("setup", "Setup", Icons.Default.Settings)
    data object Lab : Dest("lab", "Test Lab", Icons.Default.PlayArrow)
    data object Privacy : Dest("privacy", "Privacy", Icons.Default.Lock)
}

@Composable
fun QuickTextRoot() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val tabs = listOf(Dest.Home, Dest.Templates, Dest.Setup, Dest.Lab)

    Scaffold(
        bottomBar = {
            if (current != Dest.Privacy.route) {
                NavigationBar {
                    tabs.forEach { d ->
                        NavigationBarItem(
                            selected = current == d.route,
                            onClick = {
                                nav.navigate(d.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(d.icon, contentDescription = d.label) },
                            label = { Text(d.label) },
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(nav, startDestination = Dest.Home.route, modifier = Modifier.padding(pad)) {
            composable(Dest.Home.route) { DashboardScreen(onOpenPrivacy = { nav.navigate(Dest.Privacy.route) }) }
            composable(Dest.Templates.route) { TemplatesScreen() }
            composable(Dest.Setup.route) { SetupScreen() }
            composable(Dest.Lab.route) { TestLabScreen() }
            composable(Dest.Privacy.route) { PrivacyScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
