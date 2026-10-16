package com.automate.app.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.automate.app.AppViewModel
import com.automate.app.data.AppTheme
import com.automate.app.data.ToastStyle
import com.automate.app.ui.AppCard
import com.automate.app.ui.ConfirmDialog
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.ToastKind
import com.automate.app.ui.bouncyClick
import com.automate.app.ui.enterAnim
import com.automate.app.ui.rememberHaptics

@Composable
fun SettingsScreen(vm: AppViewModel, onAbout: () -> Unit) {
    val tap = rememberHaptics(vm)
    var confirm by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }

        item {
            AppCard(Modifier.fillMaxWidth().enterAnim(0)) {
                Text("Your name", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = vm.userName, onValueChange = { vm.updateName(it) },
                    singleLine = true, label = { Text("What should I call you?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { SectionTitle("Appearance") }
        items(AppTheme.entries) { t -> ThemeCard(t, vm.theme == t) { tap(); vm.setTheme(t) } }
        item { SettingSwitch("Animations", "Smooth transitions, pulses and effects", vm.animations) { vm.setAnimations(it) } }
        item { SettingSwitch("Haptic feedback", "Light taps on buttons and switches", vm.haptics) { vm.setHaptics(it) } }

        item { SectionTitle("Toast notifications") }
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Text("How should quick messages appear?", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.padding(top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToastStyle.entries.forEach { s ->
                        FilterChip(selected = vm.toastStyle == s, onClick = { tap(); vm.setToastStyle(s) }, label = { Text(s.label) })
                    }
                }
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedButton(onClick = { vm.toast("This is how toasts look ✨", ToastKind.SUCCESS) }) { Text("Preview toast") }
            }
        }

        item { SectionTitle("Automation") }
        item { SettingSwitch("Start on boot", "Resume the engine after a restart", vm.runOnBoot) { vm.setRunOnBoot(it) } }

        item { SectionTitle("Permissions") }
        item { PermissionCenter() }

        item { SectionTitle("Data") }
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { confirm = "rules" }) { Text("Delete all rules") }
                    OutlinedButton(onClick = { confirm = "logs" }) { Text("Clear activity") }
                }
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedButton(onClick = { vm.replayOnboarding() }) { Text("Replay welcome tour") }
            }
        }

        item {
            Button(onClick = onAbout, modifier = Modifier.fillMaxWidth()) { Text("About AutoMate") }
        }
    }

    when (confirm) {
        "rules" -> ConfirmDialog("Delete all rules?", "Every rule will be removed permanently.", "Delete all",
            onConfirm = { vm.deleteAllRules(); confirm = null }, onDismiss = { confirm = null })
        "logs" -> ConfirmDialog("Clear activity?", "This removes the whole run history.", "Clear",
            onConfirm = { vm.clearLogs(); confirm = null }, onDismiss = { confirm = null })
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun ThemeCard(t: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val colors = when (t) {
        AppTheme.MATERIAL_YOU -> listOf(Color(0xFF6750A4), Color(0xFFD0BCFF))
        AppTheme.AMOLED -> listOf(Color.Black, Color(0xFF9FB8FF))
        AppTheme.GRADIENT -> listOf(Color(0xFF1B1464), Color(0xFF6C2BD9), Color(0xFFE84393))
        AppTheme.LIGHT -> listOf(Color.White, Color(0xFF4F46E5))
    }
    val w by animateDpAsState(if (selected) 3.dp else 1.dp, label = "themeBorder")
    Box(
        Modifier.fillMaxWidth()
            .bouncyClick(onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
            .then(Modifier.padding(0.dp))
    ) {
        androidx.compose.material3.Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(w, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(Brush.linearGradient(colors), RoundedCornerShape(14.dp)))
                Spacer(Modifier.width(14.dp))
                Text(t.label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                if (selected) Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class PermItem(val title: String, val desc: String, val granted: Boolean, val action: () -> Unit)

@Composable
private fun PermissionCenter() {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tick++ }
        owner.lifecycle.addObserver(o)
        onDispose { owner.lifecycle.removeObserver(o) }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { tick++ }

    fun has(p: String) = ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED
    fun open(action: String, withPkg: Boolean = true) {
        runCatching {
            val i = Intent(action)
            if (withPkg) i.data = Uri.parse("package:${ctx.packageName}")
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        }
    }

    val items = remember(tick) {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            PermItem("Notifications", "Rule alerts and the engine notice",
                Build.VERSION.SDK_INT < 33 || has(Manifest.permission.POST_NOTIFICATIONS)) {
                launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            },
            PermItem("Precise location", "For arrive / leave a place rules",
                has(Manifest.permission.ACCESS_FINE_LOCATION)) {
                launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            PermItem("Location all the time", "Lets location rules work with the screen off",
                Build.VERSION.SDK_INT < 29 || has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                launcher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            },
            PermItem("Nearby devices (Bluetooth)", "To see which Bluetooth device connected",
                Build.VERSION.SDK_INT < 31 || has(Manifest.permission.BLUETOOTH_CONNECT)) {
                launcher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
            },
            PermItem("Display over other apps", "Open apps and links instantly from the background",
                Settings.canDrawOverlays(ctx)) { open(Settings.ACTION_MANAGE_OVERLAY_PERMISSION) },
            PermItem("Exact alarms", "Run time rules right on the minute",
                Build.VERSION.SDK_INT < 31 || (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) {
                if (Build.VERSION.SDK_INT >= 31) open(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            },
            PermItem("Unrestricted battery", "Stops the system from pausing the engine",
                pm.isIgnoringBatteryOptimizations(ctx.packageName)) { open(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) },
            PermItem("Do Not Disturb access", "Needed for the “silent” ringer action",
                nm.isNotificationPolicyAccessGranted) { open(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, withPkg = false) }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { p ->
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.title, fontWeight = FontWeight.SemiBold)
                        Text(p.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (p.granted) Text("Allowed ✓", color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
                    else Button(onClick = p.action) { Text("Allow") }
                }
            }
        }
    }
}
