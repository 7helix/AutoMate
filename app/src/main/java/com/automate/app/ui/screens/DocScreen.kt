package com.automate.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.data.Docs
import com.automate.app.ui.AppCard
import com.automate.app.ui.EmptyState
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.enterAnim

@Composable
fun DocScreen(id: String, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val ctx = LocalContext.current
    val doc = remember(id) { Docs.find(id) }
    val text = remember(id) { doc?.let { runCatching { Docs.read(ctx, it.asset) }.getOrNull() } }
    val blocks = remember(text) { text?.let { parseMarkdown(it) } ?: emptyList() }
    val related = remember(id) { Docs.all.filter { it.category == doc?.category && it.id != id }.take(3) }
    val codeBg = MaterialTheme.colorScheme.surfaceVariant

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            Spacer(Modifier.weight(1f))
            if (doc != null && text != null) {
                IconButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, doc.title)
                        .putExtra(Intent.EXTRA_TEXT, doc.title + "\n\n" + text)
                    ctx.startActivity(Intent.createChooser(send, "Share article"))
                }) { Icon(Icons.Rounded.Share, contentDescription = "Share") }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (doc == null || text == null) {
                item { EmptyState("🔍", "Article not found", "This article could not be loaded.") }
            } else {
                item {
                    Column(Modifier.enterAnim(0)) {
                        Text(doc.emoji, fontSize = 44.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(doc.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            doc.category.emoji + " " + doc.category.label,
                            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                itemsIndexed(blocks) { i, b -> BlockView(b, minOf(i + 1, 8), codeBg) }
                if (related.isNotEmpty()) {
                    item { SectionTitle("Keep reading") }
                    itemsIndexed(related) { _, r ->
                        AppCard(Modifier.fillMaxWidth(), onClick = { onOpen(r.id) }) {
                            Text(r.emoji + "  " + r.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                r.summary, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockView(b: Block, index: Int, codeBg: androidx.compose.ui.graphics.Color) {
    val m = Modifier.enterAnim(index)
    when (b) {
        is Heading -> Text(
            annotate(b.text, codeBg),
            modifier = m.padding(top = 8.dp),
            style = when (b.level) {
                1 -> MaterialTheme.typography.headlineSmall
                2 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium
            },
            fontWeight = FontWeight.Bold
        )
        is Para -> Text(annotate(b.text, codeBg), modifier = m, style = MaterialTheme.typography.bodyLarge)
        is Bullet -> Row(m.fillMaxWidth()) {
            Text(b.marker, Modifier.width(26.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(annotate(b.text, codeBg), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        }
        is Note -> Text(
            annotate(b.text, codeBg),
            modifier = m.fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                .padding(14.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
