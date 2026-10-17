package com.automate.app.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.AppViewModel
import com.automate.app.data.Rule
import com.automate.app.data.shareText
import com.automate.app.data.thenText
import com.automate.app.data.whenText
import com.automate.app.ui.AppCard
import com.automate.app.ui.ConfirmDialog
import com.automate.app.ui.EmptyState
import com.automate.app.ui.enterAnim
import com.automate.app.ui.rememberHaptics

@Composable
fun RulesScreen(vm: AppViewModel, onEdit: (String) -> Unit) {
    val tap = rememberHaptics(vm)
    var filter by remember { mutableIntStateOf(0) }
    var toDelete by remember { mutableStateOf<Rule?>(null) }
    var fabVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { fabVisible = true }

    val shown = when (filter) {
        1 -> vm.rules.filter { it.enabled }
        2 -> vm.rules.filter { !it.enabled }
        else -> vm.rules
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(Modifier.enterAnim(0)) {
                    Text("Your rules", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${vm.rules.size} total • ${vm.rules.count { it.enabled }} active",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Row(Modifier.enterAnim(1), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Active", "Paused").forEachIndexed { i, s ->
                        FilterChip(selected = filter == i, onClick = { tap(); filter = i }, label = { Text(s) })
                    }
                }
            }
            if (shown.isEmpty()) {
                item {
                    EmptyState(
                        "🤖",
                        if (vm.rules.isEmpty()) "No rules yet" else "Nothing here",
                        if (vm.rules.isEmpty()) "Tap “New rule” to automate your first task." else "Try a different filter."
                    )
                }
            }
            itemsIndexed(shown, key = { _, r -> r.id }) { i, r ->
                RuleCard(r, i, vm, onEdit = { onEdit(r.id) }, onDelete = { toDelete = r })
            }
        }

        AnimatedVisibility(
            visible = fabVisible,
            enter = scaleIn(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { tap(); onEdit("new") },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New rule") }
            )
        }
    }

    toDelete?.let { r ->
        ConfirmDialog(
            title = "Delete rule?",
            text = "“${r.name.ifBlank { "Untitled" }}” will be removed permanently.",
            confirm = "Delete",
            onConfirm = { vm.deleteRule(r.id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun RuleCard(r: Rule, index: Int, vm: AppViewModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    val tap = rememberHaptics(vm)
    val ctx = LocalContext.current
    AppCard(Modifier.fillMaxWidth().enterAnim(index + 2), onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) { Text(r.trigger.emoji, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    r.name.ifBlank { "Untitled rule" },
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "When: ${r.whenText()}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Then: ${r.thenText()}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Switch(checked = r.enabled, onCheckedChange = { tap(); vm.toggleRule(r.id, it) })
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (r.lastRun == 0L) "Never run yet"
                else "Ran ${r.runCount}× • " + DateUtils.getRelativeTimeSpanString(
                    r.lastRun, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                tap()
                shareText(
                    ctx, "My AutoMate rule",
                    "🤖 ${r.name.ifBlank { "My rule" }}\nWhen: ${r.whenText()}\nThen: ${r.thenText()}\n\nMade with AutoMate ⚡" +
                        if (vm.projectLink.isNotBlank()) "\n${vm.projectLink}" else ""
                )
            }) { Icon(Icons.Rounded.Share, contentDescription = "Share rule") }
            IconButton(onClick = { tap(); vm.runNow(r) }) { Icon(Icons.Rounded.PlayArrow, contentDescription = "Run now") }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Delete") }
        }
    }
}
