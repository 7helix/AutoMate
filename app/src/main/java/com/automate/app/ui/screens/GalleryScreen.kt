package com.automate.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.AppViewModel
import com.automate.app.data.Templates
import com.automate.app.data.thenText
import com.automate.app.data.tr
import com.automate.app.data.whenText
import com.automate.app.ui.AppCard
import com.automate.app.ui.BackBar
import com.automate.app.ui.EmptyState
import com.automate.app.ui.enterAnim
import com.automate.app.ui.rememberHaptics

@Composable
fun GalleryScreen(vm: AppViewModel, onEdit: (String) -> Unit, onBack: () -> Unit) {
    val tap = rememberHaptics(vm)
    var cat by remember { mutableStateOf<String?>(null) }
    val list = Templates.all.withIndex().filter { cat == null || it.value.category == cat }

    Column(Modifier.fillMaxSize()) {
    BackBar(onBack)
    LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.enterAnim(0)) {
                Text(tr("Gallery"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    tr("Ready-made rules. Tap one to customise and save."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()).enterAnim(1), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = cat == null, onClick = { tap(); cat = null }, label = { Text(tr("All")) })
                Templates.categories.forEach { c ->
                    FilterChip(selected = cat == c, onClick = { tap(); cat = c }, label = { Text(c) })
                }
            }
        }
        if (list.isEmpty()) item { EmptyState("🧩", "Nothing here", "Try a different filter.") }
        itemsIndexed(list, key = { _, iv -> iv.index }) { i, iv ->
            val t = iv.value
            AppCard(Modifier.fillMaxWidth().enterAnim(i + 2), onClick = { tap(); onEdit("tpl${iv.index}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.emoji, fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(t.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(t.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "${t.rule.whenText()}  →  ${t.rule.thenText()}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = { tap(); onEdit("tpl${iv.index}") }, modifier = Modifier.padding(0.dp)) { Text(tr("Use this rule")) }
            }
        }
    }
    }
}
