package com.automate.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.AppViewModel
import com.automate.app.data.tr
import com.automate.app.ui.theme.LocalAnimationsOn
import com.automate.app.ui.theme.dialogContainer
import kotlinx.coroutines.delay

/** Press-to-squish click. */
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
        label = "press"
    )
    this.graphicsLayer { scaleX = s; scaleY = s }
        .clickable(interactionSource = src, indication = null, onClick = onClick)
}

/** Staggered fade + slide-up entrance. */
@Composable
fun Modifier.enterAnim(index: Int = 0): Modifier {
    val on = LocalAnimationsOn.current
    var shown by remember { mutableStateOf(!on) }
    LaunchedEffect(Unit) {
        delay((index * 60L).coerceAtMost(480L))
        shown = true
    }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(400), label = "ea")
    val y by animateFloatAsState(
        if (shown) 0f else 60f,
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow), label = "ey"
    )
    val sc by animateFloatAsState(
        if (shown) 1f else 0.94f,
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow), label = "es"
    )
    return this.graphicsLayer { alpha = a; translationY = y; scaleX = sc; scaleY = sc }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val m = if (onClick != null) modifier.bouncyClick(onClick) else modifier
    Surface(
        modifier = m,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        tr(text).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String) {
    val t = rememberInfiniteTransition(label = "bob")
    val y by t.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bobY"
    )
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 64.sp, modifier = Modifier.graphicsLayer { translationY = y })
        Spacer(Modifier.height(12.dp))
        Text(tr(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            tr(subtitle), textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AnimatedCounter(value: Int, modifier: Modifier = Modifier) {
    val v by animateIntAsState(value, tween(700), label = "count")
    Text(v.toString(), modifier = modifier, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
}

@Composable
fun PulsingPower(on: Boolean, onClick: () -> Unit) {
    val anim = LocalAnimationsOn.current
    val t = rememberInfiniteTransition(label = "pulse")
    val ring by t.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ring"
    )
    val ringAlpha by t.animateFloat(
        initialValue = 0.45f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "ringA"
    )
    val bg by animateColorAsState(
        if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, label = "pbg"
    )
    val fg by animateColorAsState(
        if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "pfg"
    )
    Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) {
        if (on && anim) {
            Box(
                Modifier.size(66.dp)
                    .graphicsLayer { scaleX = ring; scaleY = ring; alpha = ringAlpha }
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        Box(
            Modifier.size(66.dp).bouncyClick(onClick).background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PowerSettingsNew, contentDescription = "Toggle engine", tint = fg, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun rememberHaptics(vm: AppViewModel): () -> Unit {
    val h = LocalHapticFeedback.current
    return remember(vm.haptics) {
        val f: () -> Unit = { if (vm.haptics) h.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
        f
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainer(),
        title = { Text(tr(title)) },
        text = { Text(tr(text)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(tr(confirm), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Bouncy scale + fade entrance, great for grids. */
@Composable
fun Modifier.popIn(index: Int = 0): Modifier {
    val on = LocalAnimationsOn.current
    var shown by remember { mutableStateOf(!on) }
    LaunchedEffect(Unit) {
        delay((index * 45L).coerceAtMost(600L))
        shown = true
    }
    val sc by animateFloatAsState(
        if (shown) 1f else 0.5f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "pop"
    )
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(300), label = "popA")
    return this.graphicsLayer { scaleX = sc; scaleY = sc; alpha = a }
}

/** Moving highlight used for loading placeholders. */
fun Modifier.shimmer(): Modifier = composed {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(
        initialValue = -300f, targetValue = 900f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val hi = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
    this.background(
        Brush.linearGradient(listOf(base, hi, base), start = Offset(x, 0f), end = Offset(x + 300f, 0f)),
        RoundedCornerShape(10.dp)
    )
}

@Composable
fun SkeletonList(rows: Int = 6) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { Box(Modifier.fillMaxWidth().height(34.dp).shimmer()) }
    }
}

/** A card with a rotating rainbow ring while [active]. */
@Composable
fun GlowCard(active: Boolean, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val anim = LocalAnimationsOn.current
    val t = rememberInfiniteTransition(label = "glow")
    val angle by t.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "glowAngle"
    )
    val c1 = MaterialTheme.colorScheme.primary
    val c2 = MaterialTheme.colorScheme.tertiary
    val c3 = MaterialTheme.colorScheme.secondary
    Box(
        modifier.drawWithContent {
            drawContent()
            if (active) {
                val r = 22.dp.toPx()
                val bw = 3.dp.toPx()
                val outer = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r))) }
                val inner = Path().apply {
                    addRoundRect(RoundRect(bw, bw, size.width - bw, size.height - bw, CornerRadius(r - bw)))
                }
                val ring = Path.combine(PathOperation.Difference, outer, inner)
                clipPath(ring) {
                    rotate(if (anim) angle else 0f) {
                        val m = maxOf(size.width, size.height)
                        drawRect(
                            Brush.sweepGradient(listOf(c1, c2, c3, c1), center = Offset(size.width / 2f, size.height / 2f)),
                            topLeft = Offset(-m, -m), size = Size(m * 3f, m * 3f)
                        )
                    }
                }
            }
        }
    ) { AppCard(Modifier.fillMaxWidth(), content = content) }
}
