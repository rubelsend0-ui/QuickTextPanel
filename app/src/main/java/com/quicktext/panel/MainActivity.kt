package com.quicktext.panel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicktext.panel.ui.navigation.QuickTextRoot
import com.quicktext.panel.ui.setup.SettingsViewModel
import com.quicktext.panel.ui.theme.QuickTextTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings: SettingsViewModel = hiltViewModel()
            val mode by settings.themeMode.collectAsStateWithLifecycle()
            QuickTextTheme(mode) { QuickTextRoot() }
        }
    }
}
