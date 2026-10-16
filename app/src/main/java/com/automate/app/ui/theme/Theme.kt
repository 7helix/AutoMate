package com.automate.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.automate.app.data.AppTheme

val LocalThemeKind = staticCompositionLocalOf { AppTheme.MATERIAL_YOU }
val LocalAnimationsOn = staticCompositionLocalOf { true }

private val AmoledScheme = darkColorScheme(
    primary = Color(0xFF9FB8FF), onPrimary = Color(0xFF00205C),
    primaryContainer = Color(0xFF16254D), onPrimaryContainer = Color(0xFFDCE4FF),
    secondary = Color(0xFF7EE0C3), secondaryContainer = Color(0xFF1C2E29), onSecondaryContainer = Color(0xFFD5F5EA),
    tertiary = Color(0xFFFFB86B),
    background = Color.Black, onBackground = Color(0xFFEDEDED),
    surface = Color.Black, onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF1F1F1F), onSurfaceVariant = Color(0xFFB5B5B5),
    surfaceContainer = Color(0xFF0D0D0D), surfaceContainerHigh = Color(0xFF151515), surfaceContainerHighest = Color(0xFF1E1E1E),
    outline = Color(0xFF444444), error = Color(0xFFFF6B6B)
)

private val GradientScheme = darkColorScheme(
    primary = Color.White, onPrimary = Color(0xFF3B1E8E),
    primaryContainer = Color(0x40FFFFFF), onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFD1F0), secondaryContainer = Color(0x44FFFFFF), onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFFE08A),
    background = Color(0xFF1B1464), onBackground = Color.White,
    surface = Color(0x22FFFFFF), onSurface = Color.White,
    surfaceVariant = Color(0x33FFFFFF), onSurfaceVariant = Color(0xFFEADFFF),
    surfaceContainer = Color(0x55000000), surfaceContainerHigh = Color(0x2EFFFFFF), surfaceContainerHighest = Color(0x3AFFFFFF),
    outline = Color(0x99FFFFFF), error = Color(0xFFFFB4AB)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF4F46E5), onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E1FF), onPrimaryContainer = Color(0xFF1B1464),
    secondary = Color(0xFF0E9F8E), secondaryContainer = Color(0xFFDDF4F0), onSecondaryContainer = Color(0xFF05302B),
    tertiary = Color(0xFFE67E22),
    background = Color(0xFFF4F4F8), onBackground = Color(0xFF16161D),
    surface = Color(0xFFF4F4F8), onSurface = Color(0xFF16161D),
    surfaceVariant = Color(0xFFE6E6EE), onSurfaceVariant = Color(0xFF5B5B6B),
    surfaceContainer = Color(0xFFFFFFFF), surfaceContainerHigh = Color(0xFFFFFFFF), surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = Color(0xFFC9C9D6)
)

private val FallbackDark = darkColorScheme(
    primary = Color(0xFFB8A9FF), onPrimary = Color(0xFF2A1B6E),
    primaryContainer = Color(0xFF3D2F8F), onPrimaryContainer = Color(0xFFE6DEFF),
    background = Color(0xFF121218), onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF121218), onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF2B2A33), onSurfaceVariant = Color(0xFFC9C4D0),
    surfaceContainer = Color(0xFF1C1B22), surfaceContainerHigh = Color(0xFF24232B), surfaceContainerHighest = Color(0xFF2E2D36)
)

private val FallbackLight = lightColorScheme(
    primary = Color(0xFF6750A4), background = Color(0xFFFEF7FF), surface = Color(0xFFFEF7FF),
    surfaceContainer = Color(0xFFF3EDF7), surfaceContainerHigh = Color(0xFFECE6F0), surfaceContainerHighest = Color(0xFFE6E0E9)
)

@Composable
fun AutoMateTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val scheme: ColorScheme = when (theme) {
        AppTheme.MATERIAL_YOU -> when {
            Build.VERSION.SDK_INT >= 31 && systemDark -> dynamicDarkColorScheme(ctx)
            Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(ctx)
            systemDark -> FallbackDark
            else -> FallbackLight
        }
        AppTheme.AMOLED -> AmoledScheme
        AppTheme.GRADIENT -> GradientScheme
        AppTheme.LIGHT -> LightScheme
    }
    val dark = theme == AppTheme.AMOLED || theme == AppTheme.GRADIENT || (theme == AppTheme.MATERIAL_YOU && systemDark)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val c = WindowCompat.getInsetsController(window, view)
                c.isAppearanceLightStatusBars = !dark
                c.isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalThemeKind provides theme) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

/** Container color for dialogs / menus (the gradient theme's translucent surfaces need an opaque one). */
@Composable
fun dialogContainer(): Color =
    if (LocalThemeKind.current == AppTheme.GRADIENT) Color(0xFF2B1A70) else MaterialTheme.colorScheme.surfaceContainerHigh

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    val kind = LocalThemeKind.current
    val animate = LocalAnimationsOn.current
    val shift = if (kind == AppTheme.GRADIENT && animate) {
        val t = rememberInfiniteTransition(label = "bg")
        val v by t.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
            label = "shift"
        )
        v
    } else 0.5f

    val bg = if (kind == AppTheme.GRADIENT) {
        Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFF1B1464), Color(0xFF6C2BD9), Color(0xFFE84393)),
                start = Offset(0f, 0f),
                end = Offset(900f + 900f * shift, 1800f + 600f * (1f - shift))
            )
        )
    } else Modifier.background(MaterialTheme.colorScheme.background)

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Box(Modifier.fillMaxSize().then(bg), content = content)
    }
}
