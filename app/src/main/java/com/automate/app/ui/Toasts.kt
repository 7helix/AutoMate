package com.automate.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ToastKind { SUCCESS, INFO, ERROR }

data class ToastMsg(val text: String, val kind: ToastKind, val id: Long)

class ToastController {
    var current by mutableStateOf<ToastMsg?>(null)
        private set

    fun show(text: String, kind: ToastKind = ToastKind.INFO) {
        current = ToastMsg(text, kind, System.nanoTime())
    }

    fun dismiss() { current = null }
}

@Composable
fun ToastHost(controller: ToastController, modifier: Modifier = Modifier) {
    val msg = controller.current
    var last by remember { mutableStateOf<ToastMsg?>(null) }
    LaunchedEffect(msg?.id) {
        if (msg != null) {
            last = msg
            delay(2800)
            if (controller.current?.id == msg.id) controller.dismiss()
        }
    }
    Box(modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = msg != null,
            enter = slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { -it * 2 } + fadeIn(),
            exit = slideOutVertically { -it * 2 } + fadeOut()
        ) {
            last?.let { ToastPill(it) }
        }
    }
}

@Composable
private fun ToastPill(m: ToastMsg) {
    val (bg, icon) = when (m.kind) {
        ToastKind.SUCCESS -> Color(0xFF1FA463) to "✅"
        ToastKind.INFO -> Color(0xFF3B5BDB) to "💡"
        ToastKind.ERROR -> Color(0xFFD64545) to "⚠️"
    }
    val pop = remember(m.id) { Animatable(0.3f) }
    LaunchedEffect(m.id) { pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy)) }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        shadowElevation = 10.dp,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 16.sp, modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value })
            Spacer(Modifier.width(10.dp))
            Text(m.text, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 2)
        }
    }
}
