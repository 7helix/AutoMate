package com.automate.app.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.automate.app.AppViewModel
import com.automate.app.data.tr
import com.automate.app.data.LogEntry
import com.automate.app.data.LogStore
import com.automate.app.ui.AppCard
import com.automate.app.ui.ConfirmDialog
import com.automate.app.ui.EmptyState
import com.automate.app.ui.enterAnim

@Composable
fun LogsScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val version by LogStore.version.collectAsState()
    val logs = remember(version) { LogStore.load(ctx) }
    var confirm by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().enterAnim(0), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Activity"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${logs.size} recent runs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (logs.isNotEmpty()) TextButton(onClick = { confirm = true }) { Text(tr("Clear")) }
            }
        }
        if (logs.isEmpty()) {
            item { EmptyState("🕰️", "No activity yet", "When a rule runs, you'll see exactly what happened here.") }
        }
        itemsIndexed(logs) { i, e -> LogRow(e, Modifier.enterAnim(i + 1)) }
    }

    if (confirm) {
        ConfirmDialog(
            title = "Clear activity?", text = "This removes the whole run history.", confirm = "Clear",
            onConfirm = { vm.clearLogs(); confirm = false }, onDismiss = { confirm = false }
        )
    }
}

@Composable
fun LogRow(e: LogEntry, modifier: Modifier = Modifier) {
    AppCard(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Spacer(Modifier.width(2.dp))
            androidx.compose.foundation.layout.Box(
                Modifier.padding(top = 6.dp).size(10.dp)
                    .background(if (e.ok) Color(0xFF2ECC71) else Color(0xFFE74C3C), CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(e.rule, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(e.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    DateUtils.getRelativeTimeSpanString(e.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
