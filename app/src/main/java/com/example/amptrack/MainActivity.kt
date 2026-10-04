package com.example.amptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amptrack.ui.navigation.AmpTrackApp
import com.example.amptrack.ui.theme.AmpTrackTheme
import com.example.amptrack.ui.viewmodel.AmpTrackViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AmpTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AmpTrackTheme(
                themeMode = uiState.settings.themeMode,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                AmpTrackApp(viewModel = viewModel)
            }
        }
    }
}