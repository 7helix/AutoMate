package com.automate.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.AppViewModel
import com.automate.app.data.LogStore
import com.automate.app.data.Templates
import com.automate.app.data.tr
import com.automate.app.ui.AnimatedCounter
import com.automate.app.ui.AppCard
import com.automate.app.ui.GlowCard
import com.automate.app.ui.PulsingPower
import com.automate.app.ui.popIn
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.enterAnim
import com.automate.app.ui.rememberHaptics
import java.util.Calendar

@Composable
fun HomeScreen(vm: AppViewModel, onEdit: (String) -> Unit, onTab: (String) -> Unit) {
    val ctx = LocalContext.current
    val tap = rememberHaptics(vm)
    val logsVersion by LogStore.version.collectAsState()
    val logs = remember(logsVersion) { LogStore.load(ctx) }
    val todayStart = remember(logsVersion) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val runsToday = logs.count { it.time >= todayStart }
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour < 5 -> "Burning the midnight oil"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
    val who = if (vm.userName.isBlank()) "" else ", ${vm.userName}"

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(Modifier.enterAnim(0)) {
                Text("$greeting$who 👋", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Your phone is ready to work for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            GlowCard(vm.serviceOn, Modifier.enterAnim(1)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingPower(on = vm.serviceOn) { tap(); vm.toggleService() }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (vm.serviceOn) "Engine is running" else "Engine is stopped",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (vm.serviceOn) "Watching battery, Wi-Fi, Bluetooth, location and more."
                            else "Tap the button to start automating.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(Modifier.enterAnim(2), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Rules", vm.rules.size, Modifier.weight(1f))
                StatCard("Active", vm.rules.count { it.enabled }, Modifier.weight(1f))
                StatCard("Runs today", runsToday, Modifier.weight(1f))
            }
        }

        item { Box(Modifier.enterAnim(3)) { SectionTitle("Quick start") } }
        item {
            LazyRow(Modifier.enterAnim(3), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(Templates.all.take(10)) { i, t ->
                    AppCard(Modifier.width(172.dp), onClick = { tap(); onEdit("tpl$i") }) {
                        Text(t.emoji, fontSize = 30.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(t.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(
                            t.subtitle, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2
                        )
                    }
                }
            }
        }

        item { Box(Modifier.enterAnim(4)) { SectionTitle("Explore") } }
        itemsIndexed(
            listOf(
                Triple("stats", "📊", "Stats"), Triple("gallery", "🧩", "Gallery"),
                Triple("control", "🎛️", "Phone control"), Triple("backup", "💾", "Backup")
            ).chunked(2)
        ) { row, group ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                group.forEachIndexed { i, (route, emoji, label) ->
                    AppCard(Modifier.weight(1f).popIn(row * 2 + i), onClick = { tap(); onTab(route) }) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 28.sp)
                            Text(tr(label), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth().enterAnim(4), onClick = { tap(); onTab("social") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tr("🌐"), fontSize = 30.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tr("Social hub"), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Open your apps, share AutoMate, social recipes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth().enterAnim(4), onClick = { tap(); onTab("learn") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tr("📘"), fontSize = 30.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tr("Learn & limitations"), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Guides, what Android won't allow, and the rules of use.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().enterAnim(4), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { SectionTitle("Recent activity") }
                TextButton(onClick = { onTab("logs") }) { Text(tr("See all")) }
            }
        }
        if (logs.isEmpty()) {
            item {
                Text(
                    "Nothing has run yet. Create a rule and tap ▶ to test it.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.enterAnim(5)
                )
            }
        } else {
            itemsIndexed(logs.take(3)) { i, e -> LogRow(e, Modifier.enterAnim(5 + i)) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier) {
    AppCard(modifier) {
        AnimatedCounter(value)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
