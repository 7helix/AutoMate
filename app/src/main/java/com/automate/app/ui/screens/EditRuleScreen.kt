package com.automate.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.automate.app.AppViewModel
import com.automate.app.data.ActionType
import com.automate.app.data.AppInfo
import com.automate.app.data.Rule
import com.automate.app.data.Social
import com.automate.app.data.Templates
import com.automate.app.data.TriggerType
import com.automate.app.data.appLabelFor
import com.automate.app.data.loadLaunchableApps
import com.automate.app.ui.AppCard
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.ToastKind
import com.automate.app.ui.SkeletonList
import com.automate.app.ui.bouncyClick
import com.automate.app.ui.enterAnim
import com.automate.app.ui.rememberHaptics
import com.automate.app.ui.theme.dialogContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private fun initialRule(vm: AppViewModel, id: String): Rule = when {
    id == "new" -> Rule()
    id.startsWith("tpl") -> {
        val idx = id.removePrefix("tpl").toIntOrNull() ?: 0
        Templates.all.getOrNull(idx)?.rule?.copy(id = UUID.randomUUID().toString()) ?: Rule()
    }
    else -> vm.rules.firstOrNull { it.id == id } ?: Rule()
}

@SuppressLint("MissingPermission")
private fun lastLocation(ctx: Context): Location? {
    val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.getProviders(true)
        .mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
        .maxByOrNull { it.time }
}

@Composable
fun EditRuleScreen(vm: AppViewModel, id: String, onBack: () -> Unit, onOpenDoc: (String) -> Unit) {
    val ctx = LocalContext.current
    val tap = rememberHaptics(vm)
    var d by remember(id) { mutableStateOf(initialRule(vm, id)) }
    val isNew = vm.rules.none { it.id == d.id }
    var latText by remember(id) { mutableStateOf(if (d.lat == 0.0) "" else d.lat.toString()) }
    var lngText by remember(id) { mutableStateOf(if (d.lng == 0.0) "" else d.lng.toString()) }
    var showTime by remember { mutableStateOf(false) }
    var showApps by remember { mutableStateOf(false) }
    val appLabel = remember(d.packageName) {
        if (d.packageName.isBlank()) "Choose an app…" else appLabelFor(ctx, d.packageName)
    }

    fun fillLocation() {
        val l = lastLocation(ctx)
        if (l == null) {
            vm.toast("No location fix yet — open Maps once, then try again", ToastKind.ERROR)
        } else {
            d = d.copy(lat = l.latitude, lng = l.longitude)
            latText = l.latitude.toString()
            lngText = l.longitude.toString()
            vm.toast("Location captured 📍", ToastKind.SUCCESS)
        }
    }

    val hasLoc = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val locPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) fillLocation()
    }

    fun validate(): String? = when {
        d.name.isBlank() -> "Give your rule a name"
        d.trigger == TriggerType.TIME && d.days == 0 -> "Pick at least one day"
        (d.trigger == TriggerType.LOCATION_ENTER || d.trigger == TriggerType.LOCATION_EXIT) &&
            d.lat == 0.0 && d.lng == 0.0 -> "Set a location first"
        d.action == ActionType.LAUNCH_APP && d.packageName.isBlank() -> "Choose an app to open"
        d.action == ActionType.OPEN_URL && d.url.isBlank() -> "Enter a link to open"
        d.action == ActionType.WHATSAPP_CHAT && d.url.none { it.isDigit() } -> "Enter a phone number with country code"
        else -> null
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            Text(
                if (isNew) "New rule" else "Edit rule",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
            )
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = d.name, onValueChange = { d = d.copy(name = it) },
                label = { Text("Rule name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().enterAnim(0)
            )

            SectionTitle("When this happens")
            DropdownField(
                label = "Trigger", items = TriggerType.entries, selected = d.trigger,
                text = { "${it.emoji}  ${it.label}" }
            ) { d = d.copy(trigger = it) }

            Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (d.trigger) {
                    TriggerType.TIME -> {
                        AppCard(Modifier.fillMaxWidth(), onClick = { showTime = true }) {
                            Text("Time (tap to change)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%02d:%02d".format(d.hour, d.minute), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        DaysRow(d.days) { d = d.copy(days = it) }
                    }
                    TriggerType.BATTERY_LOW -> AppCard(Modifier.fillMaxWidth()) {
                        Text("Run when battery is at or below ${d.threshold}%")
                        Slider(
                            value = d.threshold.toFloat(),
                            onValueChange = { d = d.copy(threshold = it.toInt()) },
                            valueRange = 5f..95f
                        )
                    }
                    TriggerType.WIFI_CONNECTED, TriggerType.WIFI_DISCONNECTED ->
                        OutlinedTextField(
                            value = d.filter, onValueChange = { d = d.copy(filter = it) },
                            label = { Text("Wi-Fi name (empty = any network)") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    TriggerType.BLUETOOTH_CONNECTED, TriggerType.BLUETOOTH_DISCONNECTED ->
                        OutlinedTextField(
                            value = d.filter, onValueChange = { d = d.copy(filter = it) },
                            label = { Text("Device name (empty = any device)") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    TriggerType.LOCATION_ENTER, TriggerType.LOCATION_EXIT -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = latText,
                                onValueChange = { latText = it; it.toDoubleOrNull()?.let { v -> d = d.copy(lat = v) } },
                                label = { Text("Latitude") }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = lngText,
                                onValueChange = { lngText = it; it.toDoubleOrNull()?.let { v -> d = d.copy(lng = v) } },
                                label = { Text("Longitude") }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                if (hasLoc) fillLocation()
                                else locPerm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Use my current location")
                        }
                        AppCard(Modifier.fillMaxWidth()) {
                            Text("Radius: ${d.radius} m")
                            Slider(
                                value = d.radius.toFloat(),
                                onValueChange = { d = d.copy(radius = it.toInt()) },
                                valueRange = 50f..1000f
                            )
                        }
                        Hint("Works best with location set to “Allow all the time”.", "location-rules", onOpenDoc)
                    }
                    else -> Text("No extra settings needed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            when (d.trigger) {
                TriggerType.WIFI_CONNECTED, TriggerType.WIFI_DISCONNECTED ->
                    Hint("Wi-Fi names can only be read with location permission and location services on.", "troubleshooting", onOpenDoc)
                TriggerType.BLUETOOTH_CONNECTED, TriggerType.BLUETOOTH_DISCONNECTED ->
                    Hint("Device names need the Nearby devices permission.", "troubleshooting", onOpenDoc)
                else -> {}
            }

            SectionTitle("Then do this")
            DropdownField(
                label = "Action", items = ActionType.entries, selected = d.action,
                text = { "${it.emoji}  ${it.label}" }
            ) { d = d.copy(action = it) }

            Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (d.action) {
                    ActionType.NOTIFICATION, ActionType.TOAST, ActionType.SHARE_TEXT, ActionType.COPY_TEXT ->
                        OutlinedTextField(
                            value = d.message, onValueChange = { d = d.copy(message = it) },
                            label = { Text("Message") }, modifier = Modifier.fillMaxWidth()
                        )
                    ActionType.SOCIAL_OPEN -> {
                        DropdownField(
                            label = "Platform", items = Social.all,
                            selected = Social.find(d.social) ?: Social.all[0],
                            text = { "${it.emoji}  ${it.label}" }
                        ) { d = d.copy(social = it.key) }
                        OutlinedTextField(
                            value = d.url, onValueChange = { d = d.copy(url = it) },
                            label = { Text("Username or link (optional)") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ActionType.WHATSAPP_CHAT -> {
                        OutlinedTextField(
                            value = d.url, onValueChange = { d = d.copy(url = it) },
                            label = { Text("Phone with country code, e.g. 15551234567") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = d.message, onValueChange = { d = d.copy(message = it) },
                            label = { Text("Message to prefill") }, modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ActionType.LAUNCH_APP ->
                        AppCard(Modifier.fillMaxWidth(), onClick = { showApps = true }) {
                            Text("App (tap to choose)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(appLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    ActionType.OPEN_URL ->
                        OutlinedTextField(
                            value = d.url, onValueChange = { d = d.copy(url = it) },
                            label = { Text("Link, e.g. example.com") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            modifier = Modifier.fillMaxWidth()
                        )
                    ActionType.RINGER_MODE ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0 to "Silent", 1 to "Vibrate", 2 to "Normal").forEach { (v, s) ->
                                FilterChip(selected = d.ringer == v, onClick = { tap(); d = d.copy(ringer = v) }, label = { Text(s) })
                            }
                        }
                    else -> Text("No extra settings needed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            when (d.action) {
                ActionType.LAUNCH_APP, ActionType.OPEN_URL ->
                    Hint("Android blocks background launches. Without “Display over other apps” you get a tap-to-open notification.", "android-restrictions", onOpenDoc)
                ActionType.RINGER_MODE ->
                    if (d.ringer == 0) Hint("Silent needs Do Not Disturb access.", "android-restrictions", onOpenDoc)
                ActionType.SOCIAL_OPEN, ActionType.SHARE_TEXT, ActionType.WHATSAPP_CHAT ->
                    Hint("Android can open the app or share sheet, but you still tap Post or Send yourself.", "social-limits", onOpenDoc)
                ActionType.FLASHLIGHT ->
                    Hint("If you switch the flashlight elsewhere, the next toggle can get out of sync.", "actions", onOpenDoc)
                else -> {}
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    val err = validate()
                    if (err != null) vm.toast(err, ToastKind.ERROR) else { tap(); vm.runNow(d) }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Test action") }
            Button(
                onClick = {
                    val err = validate()
                    if (err != null) vm.toast(err, ToastKind.ERROR)
                    else { tap(); vm.saveRule(d.copy(name = d.name.trim())); onBack() }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Save rule") }
        }
    }

    if (showTime) {
        TimeDialog(d.hour, d.minute, onDismiss = { showTime = false }) { h, m ->
            d = d.copy(hour = h, minute = m); showTime = false
        }
    }
    if (showApps) {
        AppPickerDialog(onDismiss = { showApps = false }) { a ->
            d = d.copy(packageName = a.pkg); showApps = false
        }
    }
}

@Composable
private fun Hint(text: String, docId: String, onOpenDoc: (String) -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Text("ℹ️  $text", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { onOpenDoc(docId) }) { Text("Learn more") }
    }
}

@Composable
private fun <T> DropdownField(label: String, items: List<T>, selected: T, text: (T) -> String, onSelect: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column {
        AppCard(Modifier.fillMaxWidth(), onClick = { open = true }) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text(selected), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.background(dialogContainer())) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(text(item)) }, onClick = { onSelect(item); open = false })
            }
        }
    }
}

@Composable
private fun DaysRow(mask: Int, onChange: (Int) -> Unit) {
    val names = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        names.forEachIndexed { i, n ->
            val on = (mask and (1 shl i)) != 0
            val bg by animateColorAsState(
                if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, label = "day"
            )
            Column(
                Modifier.weight(1f).aspectRatio(1f).bouncyClick { onChange(mask xor (1 shl i)) }.background(bg, CircleShape),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    n, fontWeight = FontWeight.Bold,
                    color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(h: Int, m: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val ctx = LocalContext.current
    val st = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = DateFormat.is24HourFormat(ctx))
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainer(),
        confirmButton = { TextButton(onClick = { onConfirm(st.hour, st.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = st) }
    )
}

@Composable
private fun AppPickerDialog(onDismiss: () -> Unit, onPick: (AppInfo) -> Unit) {
    val ctx = LocalContext.current
    val apps by produceState<List<AppInfo>?>(initialValue = null) {
        value = withContext(Dispatchers.Default) { loadLaunchableApps(ctx) }
    }
    var q by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainer(),
        title = { Text("Choose an app") },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column {
                OutlinedTextField(
                    value = q, onValueChange = { q = it }, singleLine = true,
                    label = { Text("Search") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                val list = apps
                if (list == null) {
                    SkeletonList()
                } else {
                    LazyColumn(Modifier.heightIn(max = 360.dp)) {
                        items(list.filter { it.label.contains(q, ignoreCase = true) }, key = { it.pkg }) { a ->
                            Text(
                                a.label,
                                Modifier.fillMaxWidth().clickable { onPick(a) }.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
