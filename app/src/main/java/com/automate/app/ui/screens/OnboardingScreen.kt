package com.automate.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    val pages = listOf(
        Triple("⚡", "Automate anything", "Build “when this happens → do that” rules for time, battery, Wi-Fi, Bluetooth and places."),
        Triple("🧠", "Smart, not noisy", "Pick a trigger, pick an action. AutoMate runs quietly in the background and logs every run."),
        Triple("🎨", "Make it yours", "Material You, AMOLED black, colorful gradients or light. And tell me your name so I can greet you.")
    )
    val pager = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    val last = pager.currentPage == pages.size - 1

    Column(Modifier.fillMaxSize().systemBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { vm.finishOnboarding(name) }) { Text("Skip") }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { p ->
            val t = rememberInfiniteTransition(label = "emoji")
            val y by t.animateFloat(
                initialValue = -10f, targetValue = 10f,
                animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "emojiY"
            )
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(pages[p].first, fontSize = 88.sp, modifier = Modifier.graphicsLayer { translationY = y })
                Spacer(Modifier.height(24.dp))
                Text(pages[p].second, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    pages[p].third, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (p == pages.size - 1) {
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it }, singleLine = true,
                        label = { Text("Your name (optional)") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "By continuing you agree to the Rules of use (Learn → Rules & policies).",
                        style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(pages.size) { i ->
                val w by animateDpAsState(if (pager.currentPage == i) 28.dp else 8.dp, label = "dot")
                Box(
                    Modifier.height(8.dp).size(width = w, height = 8.dp).background(
                        if (pager.currentPage == i) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        CircleShape
                    )
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (last) vm.finishOnboarding(name)
                else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text(if (last) "Get started" else "Next", fontWeight = FontWeight.Bold) }
    }
}
