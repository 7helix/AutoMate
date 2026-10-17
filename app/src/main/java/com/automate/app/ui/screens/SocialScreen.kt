package com.automate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.AppViewModel
import com.automate.app.data.tr
import com.automate.app.data.Docs
import com.automate.app.data.Social
import com.automate.app.data.SocialPlatform
import com.automate.app.data.Templates
import com.automate.app.data.shareText
import com.automate.app.ui.AppCard
import com.automate.app.ui.BackBar
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.ToastKind
import com.automate.app.ui.enterAnim
import com.automate.app.ui.popIn
import com.automate.app.ui.rememberHaptics

@Composable
fun SocialScreen(vm: AppViewModel, onEdit: (String) -> Unit, onOpenDoc: (String) -> Unit, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val tap = rememberHaptics(vm)

    fun openPlatform(p: SocialPlatform) {
        tap()
        runCatching {
            ctx.startActivity(Social.buildIntent(ctx, p, "").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { vm.toast("Couldn't open ${p.label}", ToastKind.ERROR) }
    }

    val shareMsg = "I'm automating my phone with AutoMate ⚡ “when this happens, do that” rules, fully offline." +
        if (vm.projectLink.isNotBlank()) "\n${vm.projectLink}" else ""

    Column(Modifier.fillMaxSize()) {
    BackBar(onBack)
    LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.enterAnim(0)) {
                Text(tr("Social hub"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Jump into your apps, share AutoMate and automate your posting routine.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth().enterAnim(1)) {
                Text(tr("Share AutoMate"), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.projectLink, onValueChange = { vm.updateProjectLink(it) },
                    label = { Text(tr("Project link (optional)")) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { tap(); shareText(ctx, "AutoMate", shareMsg) }) { Text(tr("Share app")) }
                    OutlinedButton(
                        enabled = vm.projectLink.isNotBlank(),
                        onClick = {
                            val l = vm.projectLink.trim().let { if (it.contains("://")) it else "https://$it" }
                            runCatching {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(l)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    ) { Text(tr("Open page")) }
                }
            }
        }

        item { SectionTitle("Open your apps") }
        itemsIndexed(Social.all.chunked(3)) { row, group ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                group.forEachIndexed { i, p ->
                    AppCard(Modifier.weight(1f).popIn(row * 3 + i), onClick = { openPlatform(p) }) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(p.emoji, fontSize = 30.sp)
                            Text(p.label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }
                repeat(3 - group.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        item { SectionTitle("Social recipes") }
        itemsIndexed(Templates.socialIndexed) { i, iv ->
            val t = iv.value
            AppCard(Modifier.fillMaxWidth().enterAnim(i), onClick = { tap(); onEdit("tpl${iv.index}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.emoji, fontSize = 30.sp)
                    Spacer(Modifier.height(0.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(t.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            t.subtitle, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { SectionTitle("Read before you automate") }
        itemsIndexed(listOf("social-automation", "social-limits")) { _, id ->
            Docs.find(id)?.let { d ->
                AppCard(Modifier.fillMaxWidth(), onClick = { onOpenDoc(d.id) }) {
                    Text(d.emoji + "  " + d.title, fontWeight = FontWeight.SemiBold)
                    Text(
                        d.summary, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    }
}
