package ru.mirea.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF426854),
                    background = Color(0xFFF6F7F2),
                    surface = Color(0xFFF6F7F2),
                    secondaryContainer = Color(0xFFE2EBDD)
                )
            ) {
                GalleryScreen()
            }
        }
    }
}
