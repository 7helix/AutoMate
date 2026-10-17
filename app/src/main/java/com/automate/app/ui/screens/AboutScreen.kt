package com.automate.app.ui.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automate.app.ui.AppCard
import com.automate.app.data.tr
import com.automate.app.ui.enterAnim

@Composable
fun AboutScreen(onBack: () -> Unit, onOpenDoc: (String) -> Unit) {
    val ctx = LocalContext.current
    val version = remember { runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "1.0" }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            Text(tr("About"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppCard(Modifier.fillMaxWidth().enterAnim(0)) {
                Text(tr("⚡ AutoMate"), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Version $version", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Automate anything, anywhere — with simple “when → then” rules.",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            AppCard(Modifier.fillMaxWidth().enterAnim(1)) {
                Text(tr("How it works"), fontWeight = FontWeight.Bold)
                Text(
                    "1. Create a rule with a trigger and an action.\n" +
                        "2. Turn the engine on from Home.\n" +
                        "3. Check Activity to see every run.\n\n" +
                        "Time rules use the system alarm clock. Battery, charger, Wi-Fi, Bluetooth, unlock and " +
                        "location rules are watched by the engine while it runs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            AppCard(Modifier.fillMaxWidth().enterAnim(2)) {
                Text(tr("Good to know"), fontWeight = FontWeight.Bold)
                Text(
                    "• Android blocks apps from starting other apps in the background. Allow “Display over other apps” " +
                        "(Settings → Permissions) for instant launches, otherwise you get a tap-to-open notification.\n" +
                        "• Silent mode needs Do Not Disturb access.\n" +
                        "• Some phone brands stop background apps — set battery to Unrestricted for best results.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            AppCard(Modifier.fillMaxWidth().enterAnim(3)) {
                Text(tr("Read more"), fontWeight = FontWeight.Bold)
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onOpenDoc("android-restrictions") }, modifier = Modifier.fillMaxWidth()) { Text(tr("Limitations")) }
                    OutlinedButton(onClick = { onOpenDoc("rules-of-use") }, modifier = Modifier.fillMaxWidth()) { Text(tr("Rules of use")) }
                    OutlinedButton(onClick = { onOpenDoc("privacy") }, modifier = Modifier.fillMaxWidth()) { Text(tr("Privacy policy")) }
                    OutlinedButton(onClick = { onOpenDoc("disclaimer-license") }, modifier = Modifier.fillMaxWidth()) { Text(tr("Disclaimer & license")) }
                }
            }
        }
    }
}
