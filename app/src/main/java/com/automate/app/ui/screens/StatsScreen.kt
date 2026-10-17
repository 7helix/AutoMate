package com.automate.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.automate.app.AppViewModel
import com.automate.app.data.LogStore
import com.automate.app.data.StatsStore
import com.automate.app.data.tr
import com.automate.app.ui.AnimatedCounter
import com.automate.app.ui.AppCard
import com.automate.app.ui.BackBar
import com.automate.app.ui.EmptyState
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.enterAnim

@Composable
fun StatsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val version by LogStore.version.collectAsState()
    val days = remember(version) { StatsStore.lastDays(ctx, 7) }
    val totals = remember(version) { StatsStore.totals(ctx) }
    val streak = remember(version) { StatsStore.streak(ctx) }
    val week = days.sumOf { it.ok + it.fail }
    val total = totals.first + totals.second
    val rate = if (total == 0) 100 else totals.first * 100 / total
    val savedMin = total * 10 / 60
    val top = vm.rules.filter { it.runCount > 0 }.sortedByDescending { it.runCount }.take(5)
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { ready = true }

    Column(Modifier.fillMaxSize()) {
    BackBar(onBack)
    LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.enterAnim(0)) {
                Text(tr("Stats"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "How much AutoMate has done for you.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (total == 0) {
            item { EmptyState("📊", "No runs yet", "Create a rule and run it. Your numbers will appear here.") }
        }
        item {
            Row(Modifier.enterAnim(1), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Runs this week", Modifier.weight(1f)) { AnimatedCounter(week) }
                StatTile("Success rate", Modifier.weight(1f)) {
                    Text("$rate%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Row(Modifier.enterAnim(2), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Day streak", Modifier.weight(1f)) {
                    Text("🔥 $streak", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                StatTile("Time saved (estimate)", Modifier.weight(1f)) {
                    Text("≈ $savedMin min", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            AppCard(Modifier.fillMaxWidth().enterAnim(3)) {
                Text(tr("Last 7 days"), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                val max = (days.maxOfOrNull { it.ok + it.fail } ?: 0).coerceAtLeast(1)
                Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.forEach { d ->
                        val count = d.ok + d.fail
                        val frac = if (ready) count.toFloat() / max else 0f
                        val h by animateFloatAsState(
                            frac, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow), label = "bar"
                        )
                        Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                                Box(
                                    Modifier.fillMaxWidth(0.6f).fillMaxHeight(h.coerceIn(0.03f, 1f))
                                        .background(
                                            if (count == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                            Text("$count", style = MaterialTheme.typography.labelSmall)
                            Text(d.label.take(3), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        if (top.isNotEmpty()) {
            item { SectionTitle("Top rules") }
            val topMax = top.first().runCount.coerceAtLeast(1)
            items(top.size) { i ->
                val r = top[i]
                val f by animateFloatAsState(
                    if (ready) r.runCount.toFloat() / topMax else 0f,
                    spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow), label = "top"
                )
                AppCard(Modifier.fillMaxWidth().enterAnim(i + 4)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.trigger.emoji + "  " + r.name.ifBlank { "Untitled rule" }, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text("${r.runCount}×", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))) {
                        Box(Modifier.fillMaxWidth(f.coerceIn(0.02f, 1f)).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                    }
                }
            }
        }
        item {
            Text(
                "Time saved assumes about 10 seconds per run. Counts cover the last 120 days.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    }
}

@Composable
private fun StatTile(label: String, modifier: Modifier, value: @Composable () -> Unit) {
    AppCard(modifier) {
        value()
        Text(tr(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
