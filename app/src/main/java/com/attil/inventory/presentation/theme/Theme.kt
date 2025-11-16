package com.attil.inventory.presentation.theme 
 
import android.app.Activity 
import androidx.compose.foundation.isSystemInDarkTheme 
import androidx.compose.material3.MaterialTheme 
import androidx.compose.material3.lightColorScheme 
import androidx.compose.material3.darkColorScheme 
import androidx.compose.runtime.Composable 
import androidx.compose.runtime.SideEffect 
import androidx.compose.ui.graphics.toArgb 
import androidx.compose.ui.platform.LocalView 
import androidx.core.view.WindowCompat 
 
private val LightColorScheme = lightColorScheme( 
    primary = Primary, 
    secondary = Secondary, 
    background = Background, 
    surface = Surface 
) 
 
private val DarkColorScheme = darkColorScheme( 
    primary = Primary, 
    secondary = Secondary, 
    background = Background, 
    surface = Surface 
) 
 
@Composable 
fun AttilInventoryTheme( 
    darkTheme: Boolean = isSystemInDarkTheme(), 
    content: @Composable () -> Unit 
) { 
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme 
    MaterialTheme( 
        colorScheme = colorScheme, 
        content = content 
    ) 
} 
