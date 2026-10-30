package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.MainScreen
import com.example.ui.theme.GasValveFinderTheme

/**
 * Main activity for the Gas Valve Finder («شیر‌یاب گاز») application.
 * Fully offline, single-activity native Android application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GasValveFinderTheme {
                MainScreen()
            }
        }
    }
}
