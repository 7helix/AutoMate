package com.automate.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.automate.app.AppViewModel
import com.automate.app.data.SystemKeys
import com.automate.app.data.isAccessibilityEnabled
import com.automate.app.data.tr
import com.automate.app.engine.ActionRunner
import com.automate.app.ui.AppCard
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.ToastKind
import com.automate.app.ui.enterAnim
import com.automate.app.ui.popIn

@Composable
fun ControlScreen(vm: AppViewModel, onBack: () -> Unit, onOpenDoc: (String) -> Unit) {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tick++ }
        owner.lifecycle.addObserver(o)
        onDispose { owner.lifecycle.removeObserver(o) }
    }
    val enabled = remember(tick) { isAccessibilityEnabled(ctx) }
    val canWrite = remember(tick) { Settings.System.canWrite(ctx) }

    fun open(action: String, withPkg: Boolean = false) {
        runCatching {
            val i = Intent(action)
            if (withPkg) i.data = Uri.parse("package:${ctx.packageName}")
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        }.onFailure { vm.toast("Couldn't open that page", ToastKind.ERROR) }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            Text(tr("Phone control"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppCard(Modifier.fillMaxWidth().enterAnim(0)) {
                Text(
                    if (enabled) "✅  " + tr("Accessibility is on") else "⚠️  " + tr("Accessibility is off"),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
                Text(
                    "AutoMate uses Android's Accessibility service only to press system buttons for you, such as Home, Back, " +
                        "Recents, the notification shade and the lock screen. It cannot read your screen or what you type.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
                if (!enabled) {
                    Text(
                        "1. Tap the button below.\n2. Open Installed apps (or Downloaded apps) and choose AutoMate.\n3. Switch it on and confirm.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Button(
                    onClick = { open(Settings.ACTION_ACCESSIBILITY_SETTINGS) },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) { Text(tr("Open Accessibility settings")) }
            }

            SectionTitle("Try it now")
            SystemKeys.actions.chunked(2).forEachIndexed { row, group ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    group.forEachIndexed { i, (key, label) ->
                        AppCard(Modifier.weight(1f).popIn(row * 2 + i), onClick = {
                            if (!enabled) {
                                vm.toast("Turn on the Accessibility service first", ToastKind.ERROR)
                            } else {
                                runCatching { ActionRunner.systemAction(key) }
                                    .onSuccess { vm.toast(it, ToastKind.SUCCESS) }
                                    .onFailure { vm.toast(it.message ?: "Action failed", ToastKind.ERROR) }
                            }
                        }) {
                            Text(tr(label), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            SectionTitle("Open a settings page")
            SystemKeys.settings.chunked(2).forEachIndexed { row, group ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    group.forEachIndexed { i, (key, label) ->
                        AppCard(Modifier.weight(1f).popIn(row * 2 + i), onClick = { open(SystemKeys.settingsAction(key)) }) {
                            Text(tr(label), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (group.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            SectionTitle("Permissions")
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("Modify system settings"), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Needed to change screen brightness.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (canWrite) Text(tr("Allowed ✓"), color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
                    else OutlinedButton(onClick = { open(Settings.ACTION_MANAGE_WRITE_SETTINGS, withPkg = true) }) { Text(tr("Allow")) }
                }
            }
            Row {
                TextButton(onClick = { onOpenDoc("phone-control") }) { Text(tr("Learn more")) }
                TextButton(onClick = { onOpenDoc("control-limits") }) { Text(tr("Limitations")) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
