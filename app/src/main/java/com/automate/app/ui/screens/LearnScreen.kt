package com.automate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.automate.app.data.DocCategory
import com.automate.app.data.tr
import com.automate.app.data.Docs
import com.automate.app.ui.AppCard
import com.automate.app.ui.EmptyState
import com.automate.app.ui.enterAnim

@Composable
fun LearnScreen(onOpen: (String) -> Unit) {
    var q by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf<DocCategory?>(null) }
    val list = Docs.all.filter {
        (cat == null || it.category == cat) &&
            (q.isBlank() || it.title.contains(q, ignoreCase = true) || it.summary.contains(q, ignoreCase = true))
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.enterAnim(0)) {
                Text(tr("Learn"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${Docs.all.size} articles: guides, limitations and rules.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            OutlinedTextField(
                value = q, onValueChange = { q = it }, singleLine = true,
                label = { Text(tr("Search articles")) },
                modifier = Modifier.fillMaxWidth().enterAnim(1)
            )
        }
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).enterAnim(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = cat == null, onClick = { cat = null }, label = { Text(tr("All")) })
                DocCategory.entries.forEach { c ->
                    FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text("${c.emoji} ${c.label}") })
                }
            }
        }
        if (list.isEmpty()) {
            item { EmptyState("🔍", "No matches", "Try a different word or filter.") }
        }
        itemsIndexed(list, key = { _, d -> d.id }) { i, d ->
            AppCard(Modifier.fillMaxWidth().enterAnim(i + 3), onClick = { onOpen(d.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text(d.emoji, fontSize = 22.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(d.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            d.summary, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            d.category.label, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
