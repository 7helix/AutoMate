package com.automate.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.automate.app.data.Rule
import com.automate.app.data.RuleStore
import com.automate.app.data.shareText
import com.automate.app.data.tr
import com.automate.app.ui.AppCard
import com.automate.app.ui.ToastKind
import com.automate.app.ui.enterAnim
import com.automate.app.ui.theme.dialogContainer

@Composable
fun BackupScreen(vm: AppViewModel, onBack: () -> Unit, onOpenDoc: (String) -> Unit) {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<List<Rule>?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportJson().toByteArray(Charsets.UTF_8)) }
            }.onSuccess { vm.toast("Backup saved ✅", ToastKind.SUCCESS) }
                .onFailure { vm.toast("Couldn't save the backup", ToastKind.ERROR) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching {
                ctx.contentResolver.openInputStream(uri)?.use { s ->
                    val bytes = s.readBytes()
                    if (bytes.size > 2_000_000) null else bytes.toString(Charsets.UTF_8)
                }
            }.getOrNull()
            val parsed = text?.let { RuleStore.parseBackup(it) }
            when {
                parsed == null -> vm.toast("That file is not a valid AutoMate backup", ToastKind.ERROR)
                parsed.isEmpty() -> vm.toast("The backup contains no rules", ToastKind.INFO)
                else -> pending = parsed
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            Text(tr("Backup & restore"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppCard(Modifier.fillMaxWidth().enterAnim(0)) {
                Text("💾  ${vm.rules.size} rules ready to back up", fontWeight = FontWeight.Bold)
                Text(
                    "A backup is a small file with all your rules. Keep it in your cloud drive, or send it to a new phone and import it there.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            AppCard(Modifier.fillMaxWidth().enterAnim(1)) {
                Text(tr("Export"), fontWeight = FontWeight.Bold)
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { exportLauncher.launch("automate-backup.json") }, modifier = Modifier.fillMaxWidth()) {
                        Text(tr("Export to file"))
                    }
                    OutlinedButton(
                        onClick = { shareText(ctx, "AutoMate backup", vm.exportJson()) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(tr("Share backup as text")) }
                }
            }
            AppCard(Modifier.fillMaxWidth().enterAnim(2)) {
                Text(tr("Import"), fontWeight = FontWeight.Bold)
                Text(
                    "Imported rules can run actions on your phone. Only import backups you created or trust.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text(tr("Import from file")) }
            }
            TextButton(onClick = { onOpenDoc("backup-widget") }) { Text(tr("Learn more")) }
        }
    }

    pending?.let { list ->
        AlertDialog(
            onDismissRequest = { pending = null },
            containerColor = dialogContainer(),
            title = { Text("Import ${list.size} rules?") },
            text = {
                Text(tr("Choose how to add them. Imported rules can run actions on your phone, so only continue if you trust this file."))
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { vm.importRules(list, replace = false); pending = null }) { Text(tr("Add to my rules")) }
                    TextButton(onClick = { vm.importRules(list, replace = true); pending = null }) {
                        Text(tr("Replace all"), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text(tr("Cancel")) } }
        )
    }
}
