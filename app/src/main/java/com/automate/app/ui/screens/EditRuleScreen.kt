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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
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
import com.automate.app.data.ActionItem
import com.automate.app.data.ActionType
import com.automate.app.data.AppInfo
import com.automate.app.data.Condition
import com.automate.app.data.ConditionType
import com.automate.app.data.Rule
import com.automate.app.data.Social
import com.automate.app.data.SystemKeys
import com.automate.app.data.Templates
import com.automate.app.data.TriggerType
import com.automate.app.data.appLabelFor
import com.automate.app.data.defaultCondition
import com.automate.app.data.hm
import com.automate.app.data.intervalText
import com.automate.app.data.loadLaunchableApps
import com.automate.app.data.tr
import com.automate.app.ui.AppCard
import com.automate.app.ui.SectionTitle
import com.automate.app.ui.SkeletonList
import com.automate.app.ui.ToastKind
import com.automate.app.ui.bouncyClick
import com.automate.app.ui.enterAnim
import com.automate.app.ui.rememberHaptics
import com.automate.app.ui.theme.dialogContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private data class TimeEdit(val cond: Int, val end: Boolean, val minutes: Int)

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

private fun triggerHint(t: TriggerType): Pair<String, String>? = when (t) {
    TriggerType.REPEAT -> "Intervals under 10 minutes can be delayed while the phone is idle." to "accuracy-and-battery"
    TriggerType.WIFI_CONNECTED, TriggerType.WIFI_DISCONNECTED ->
        "Wi-Fi names can only be read with location permission and location services on." to "troubleshooting"
    TriggerType.BLUETOOTH_CONNECTED, TriggerType.BLUETOOTH_DISCONNECTED ->
        "Device names need the Nearby devices permission." to "troubleshooting"
    TriggerType.HEADPHONES_PLUGGED, TriggerType.HEADPHONES_UNPLUGGED ->
        "Only wired headphones are detected here. Bluetooth headphones use the Bluetooth trigger." to "triggers"
    TriggerType.SCREEN_ON, TriggerType.SCREEN_OFF, TriggerType.AIRPLANE_ON, TriggerType.AIRPLANE_OFF,
    TriggerType.POWER_SAVER_ON, TriggerType.POWER_SAVER_OFF ->
        "This trigger needs the engine to be running (switch it on from Home)." to "triggers"
    TriggerType.DEVICE_BOOT -> "Some phones block this until you open AutoMate once after a restart." to "keep-alive"
    TriggerType.LOCATION_ENTER, TriggerType.LOCATION_EXIT ->
        "Works best with location set to “Allow all the time”." to "location-rules"
    else -> null
}

private fun stepHint(a: ActionItem): Pair<String, String>? = when (a.type) {
    ActionType.LAUNCH_APP, ActionType.OPEN_URL, ActionType.OPEN_SETTINGS, ActionType.OPEN_CAMERA,
    ActionType.DIAL_NUMBER, ActionType.SMS_DRAFT, ActionType.EMAIL_DRAFT, ActionType.WHATSAPP_CHAT,
    ActionType.SHARE_TEXT, ActionType.SOCIAL_OPEN ->
        "Android blocks background launches. Without “Display over other apps” you get a tap-to-open notification." to "android-restrictions"
    ActionType.RINGER_MODE -> if (a.ringer == 0) "Silent needs Do Not Disturb access." to "android-restrictions" else null
    ActionType.DND_MODE -> "Changing Do Not Disturb needs Do Not Disturb access." to "android-restrictions"
    ActionType.SET_BRIGHTNESS -> "Brightness needs the “Modify system settings” permission." to "phone-control"
    ActionType.SYSTEM_ACTION -> "System buttons need AutoMate's Accessibility service to be switched on." to "phone-control"
    ActionType.FLASHLIGHT -> "If you switch the flashlight elsewhere, the next toggle can get out of sync." to "actions"
    ActionType.SPEAK_TEXT -> "Speech uses your phone's text-to-speech voice and the media volume." to "actions"
    else -> null
}

@Composable
fun EditRuleScreen(vm: AppViewModel, id: String, onBack: () -> Unit, onOpenDoc: (String) -> Unit) {
    val ctx = LocalContext.current
    val tap = rememberHaptics(vm)
    var d by remember(id) { mutableStateOf(initialRule(vm, id)) }
    val isNew = vm.rules.none { it.id == d.id }
    var latText by remember(id) { mutableStateOf(if (d.lat == 0.0) "" else d.lat.toString()) }
    var lngText by remember(id) { mutableStateOf(if (d.lng == 0.0) "" else d.lng.toString()) }
    var timeEdit by remember { mutableStateOf<TimeEdit?>(null) }
    var pickAppFor by remember { mutableStateOf<Int?>(null) }

    fun setStep(i: Int, a: ActionItem) { d = d.copy(actions = d.actions.toMutableList().also { it[i] = a }) }
    fun setCond(i: Int, c: Condition) { d = d.copy(conditions = d.conditions.toMutableList().also { it[i] = c }) }
    fun moveStep(i: Int, delta: Int) {
        val j = i + delta
        if (j in d.actions.indices) {
            val l = d.actions.toMutableList()
            val tmp = l[i]
            l[i] = l[j]
            l[j] = tmp
            d = d.copy(actions = l)
        }
    }

    fun fillLocation() {
        val l = lastLocation(ctx)
        if (l == null) {
            vm.toast("No location fix yet. Open Maps once, then try again.", ToastKind.ERROR)
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

    fun validate(): String? {
        if (d.name.isBlank()) return "Give your rule a name"
        if (d.trigger == TriggerType.TIME && d.days == 0) return "Pick at least one day"
        if ((d.trigger == TriggerType.LOCATION_ENTER || d.trigger == TriggerType.LOCATION_EXIT) && d.lat == 0.0 && d.lng == 0.0) {
            return "Set a location first"
        }
        if (d.conditions.any { it.type == ConditionType.DAYS && it.a == 0 }) return "Pick at least one day in your condition"
        if (d.conditions.any { it.type == ConditionType.WIFI_NAME && it.text.isBlank() }) return "Enter the Wi-Fi name for your condition"
        for ((i, a) in d.actions.withIndex()) {
            val n = i + 1
            when (a.type) {
                ActionType.LAUNCH_APP -> if (a.packageName.isBlank()) return "Step $n: choose an app to open"
                ActionType.OPEN_URL -> if (a.url.isBlank()) return "Step $n: enter a link to open"
                ActionType.WHATSAPP_CHAT -> if (a.url.none { it.isDigit() }) return "Step $n: enter a phone number with country code"
                ActionType.DIAL_NUMBER -> if (a.url.isBlank()) return "Step $n: enter a phone number"
                ActionType.SPEAK_TEXT -> if (a.message.isBlank()) return "Step $n: enter the text to speak"
                else -> {}
            }
        }
        return null
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            Text(
                if (isNew) tr("New rule") else tr("Edit rule"),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
            )
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = d.name, onValueChange = { d = d.copy(name = it) },
                label = { Text(tr("Rule name")) }, singleLine = true,
                modifier = Modifier.fillMaxWidth().enterAnim(0)
            )
            OutlinedTextField(
                value = d.folder, onValueChange = { d = d.copy(folder = it) },
                label = { Text(tr("Folder (optional)")) }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (vm.folders.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    vm.folders.forEach { f ->
                        FilterChip(selected = d.folder == f, onClick = { d = d.copy(folder = if (d.folder == f) "" else f) }, label = { Text(f) })
                    }
                }
            }

            // ------------------------------------------------ WHEN
            SectionTitle("When this happens")
            DropdownField(
                label = "Trigger", items = TriggerType.entries, selected = d.trigger,
                text = { "${it.emoji}  ${tr(it.label)}" }, groupOf = { it.group }
            ) { d = d.copy(trigger = it) }

            Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (d.trigger) {
                    TriggerType.TIME -> {
                        AppCard(Modifier.fillMaxWidth(), onClick = { timeEdit = TimeEdit(-1, false, d.hour * 60 + d.minute) }) {
                            Text(tr("Time (tap to change)"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(hm(d.hour * 60 + d.minute), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        DaysRow(d.days) { d = d.copy(days = it) }
                    }
                    TriggerType.REPEAT -> AppCard(Modifier.fillMaxWidth()) {
                        Text(tr("Run every ") + intervalText(d.intervalMin), fontWeight = FontWeight.SemiBold)
                        Slider(
                            value = d.intervalMin.toFloat(),
                            onValueChange = { d = d.copy(intervalMin = ((it / 5f).toInt() * 5).coerceAtLeast(5)) },
                            valueRange = 5f..720f
                        )
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(15 to "15 min", 30 to "30 min", 60 to "1 hour", 120 to "2 hours", 360 to "6 hours").forEach { (v, s) ->
                                FilterChip(selected = d.intervalMin == v, onClick = { tap(); d = d.copy(intervalMin = v) }, label = { Text(s) })
                            }
                        }
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
                            label = { Text(tr("Wi-Fi name (empty = any network)")) }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    TriggerType.BLUETOOTH_CONNECTED, TriggerType.BLUETOOTH_DISCONNECTED ->
                        OutlinedTextField(
                            value = d.filter, onValueChange = { d = d.copy(filter = it) },
                            label = { Text(tr("Device name (empty = any device)")) }, singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    TriggerType.LOCATION_ENTER, TriggerType.LOCATION_EXIT -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = latText,
                                onValueChange = { latText = it; it.toDoubleOrNull()?.let { v -> d = d.copy(lat = v) } },
                                label = { Text(tr("Latitude")) }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = lngText,
                                onValueChange = { lngText = it; it.toDoubleOrNull()?.let { v -> d = d.copy(lng = v) } },
                                label = { Text(tr("Longitude")) }, singleLine = true,
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
                            Text(tr("Use my current location"))
                        }
                        AppCard(Modifier.fillMaxWidth()) {
                            Text("Radius: ${d.radius} m")
                            Slider(
                                value = d.radius.toFloat(),
                                onValueChange = { d = d.copy(radius = it.toInt()) },
                                valueRange = 50f..1000f
                            )
                        }
                    }
                    else -> Text(tr("No extra settings needed."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            triggerHint(d.trigger)?.let { (text, doc) -> Hint(text, doc, onOpenDoc) }

            // ------------------------------------------------ CONDITIONS
            SectionTitle("Only if these are true (optional)")
            d.conditions.forEachIndexed { i, c ->
                ConditionCard(
                    c = c,
                    onChange = { setCond(i, it) },
                    onDelete = { d = d.copy(conditions = d.conditions.filterIndexed { j, _ -> j != i }) },
                    onPickTime = { end -> timeEdit = TimeEdit(i, end, if (end) c.b else c.a) }
                )
            }
            if (d.conditions.size >= 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = d.matchAll, onClick = { d = d.copy(matchAll = true) }, label = { Text(tr("All must be true")) })
                    FilterChip(selected = !d.matchAll, onClick = { d = d.copy(matchAll = false) }, label = { Text(tr("Any can be true")) })
                }
            }
            if (d.conditions.size < 8) {
                OutlinedButton(
                    onClick = { tap(); d = d.copy(conditions = d.conditions + defaultCondition(ConditionType.TIME_BETWEEN)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Add condition"))
                }
            }

            // ------------------------------------------------ THEN
            SectionTitle("Then do this")
            d.actions.forEachIndexed { i, a ->
                StepCard(
                    index = i, total = d.actions.size, a = a,
                    onChange = { setStep(i, it) },
                    onUp = { moveStep(i, -1) }, onDown = { moveStep(i, 1) },
                    onDelete = { if (d.actions.size > 1) d = d.copy(actions = d.actions.filterIndexed { j, _ -> j != i }) },
                    onPickApp = { pickAppFor = i },
                    ruleName = d.name, onOpenDoc = onOpenDoc
                )
            }
            if (d.actions.size < 10) {
                OutlinedButton(
                    onClick = { tap(); d = d.copy(actions = d.actions + ActionItem()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Add step"))
                }
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
            ) { Text(tr("Test action")) }
            Button(
                onClick = {
                    val err = validate()
                    if (err != null) vm.toast(err, ToastKind.ERROR)
                    else { tap(); vm.saveRule(d.copy(name = d.name.trim())); onBack() }
                },
                modifier = Modifier.weight(1f)
            ) { Text(tr("Save rule")) }
        }
    }

    timeEdit?.let { te ->
        TimeDialog(te.minutes / 60, te.minutes % 60, onDismiss = { timeEdit = null }) { h, m ->
            val mins = h * 60 + m
            if (te.cond < 0) d = d.copy(hour = h, minute = m)
            else {
                val c = d.conditions[te.cond]
                setCond(te.cond, if (te.end) c.copy(b = mins) else c.copy(a = mins))
            }
            timeEdit = null
        }
    }
    pickAppFor?.let { idx ->
        AppPickerDialog(onDismiss = { pickAppFor = null }) { a ->
            if (idx in d.actions.indices) setStep(idx, d.actions[idx].copy(packageName = a.pkg))
            pickAppFor = null
        }
    }
}

@Composable
private fun StepCard(
    index: Int, total: Int, a: ActionItem,
    onChange: (ActionItem) -> Unit, onUp: () -> Unit, onDown: () -> Unit, onDelete: () -> Unit,
    onPickApp: () -> Unit, ruleName: String, onOpenDoc: (String) -> Unit
) {
    val ctx = LocalContext.current
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Step") + " ${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            if (index > 0) IconButton(onClick = onUp) { Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Move up") }
            if (index < total - 1) IconButton(onClick = onDown) { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Move down") }
            if (total > 1) IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Remove step") }
        }
        DropdownField(
            label = "Action", items = ActionType.entries.sortedBy { it.group.ordinal }, selected = a.type,
            text = { "${it.emoji}  ${tr(it.label)}" }, groupOf = { tr(it.group.label) }
        ) { t ->
            onChange(
                a.copy(
                    type = t,
                    sysAction = when (t) {
                        ActionType.OPEN_SETTINGS -> "main"
                        ActionType.SYSTEM_ACTION -> "home"
                        else -> a.sysAction
                    }
                )
            )
        }
        Column(Modifier.animateContentSize().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (a.type) {
                ActionType.NOTIFICATION, ActionType.TOAST, ActionType.SPEAK_TEXT, ActionType.COPY_TEXT, ActionType.SHARE_TEXT ->
                    OutlinedTextField(
                        value = a.message, onValueChange = { onChange(a.copy(message = it)) },
                        label = { Text(tr("Message")) }, modifier = Modifier.fillMaxWidth()
                    )
                ActionType.DIAL_NUMBER ->
                    OutlinedTextField(
                        value = a.url, onValueChange = { onChange(a.copy(url = it)) },
                        label = { Text(tr("Phone number")) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
                    )
                ActionType.SMS_DRAFT, ActionType.WHATSAPP_CHAT -> {
                    OutlinedTextField(
                        value = a.url, onValueChange = { onChange(a.copy(url = it)) },
                        label = { Text(if (a.type == ActionType.WHATSAPP_CHAT) "Phone with country code, e.g. 15551234567" else "Phone number") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = a.message, onValueChange = { onChange(a.copy(message = it)) },
                        label = { Text(tr("Message")) }, modifier = Modifier.fillMaxWidth()
                    )
                }
                ActionType.EMAIL_DRAFT -> {
                    OutlinedTextField(
                        value = a.url, onValueChange = { onChange(a.copy(url = it)) },
                        label = { Text(tr("Email address (optional)")) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = a.message, onValueChange = { onChange(a.copy(message = it)) },
                        label = { Text(tr("Message")) }, modifier = Modifier.fillMaxWidth()
                    )
                }
                ActionType.SOCIAL_OPEN -> {
                    DropdownField(
                        label = "Platform", items = Social.all, selected = Social.find(a.social) ?: Social.all[0],
                        text = { "${it.emoji}  ${it.label}" }
                    ) { onChange(a.copy(social = it.key)) }
                    OutlinedTextField(
                        value = a.url, onValueChange = { onChange(a.copy(url = it)) },
                        label = { Text(tr("Username or link (optional)")) }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
                ActionType.LAUNCH_APP ->
                    OutlinedButton(onClick = onPickApp, modifier = Modifier.fillMaxWidth()) {
                        Text(if (a.packageName.isBlank()) tr("Choose an app") + "…" else appLabelFor(ctx, a.packageName))
                    }
                ActionType.OPEN_URL ->
                    OutlinedTextField(
                        value = a.url, onValueChange = { onChange(a.copy(url = it)) },
                        label = { Text(tr("Link, e.g. example.com")) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), modifier = Modifier.fillMaxWidth()
                    )
                ActionType.RINGER_MODE ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Silent", 1 to "Vibrate", 2 to "Normal").forEach { (v, s) ->
                            FilterChip(selected = a.ringer == v, onClick = { onChange(a.copy(ringer = v)) }, label = { Text(s) })
                        }
                    }
                ActionType.DND_MODE ->
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Block all", 1 to "Priority only", 2 to "Off").forEach { (v, s) ->
                            FilterChip(selected = a.ringer == v, onClick = { onChange(a.copy(ringer = v)) }, label = { Text(s) })
                        }
                    }
                ActionType.SET_VOLUME -> {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Media", 1 to "Ring", 2 to "Alarm", 3 to "Alerts").forEach { (v, s) ->
                            FilterChip(selected = a.stream == v, onClick = { onChange(a.copy(stream = v)) }, label = { Text(s) })
                        }
                    }
                    Text("Volume: ${a.level}%")
                    Slider(value = a.level.toFloat(), onValueChange = { onChange(a.copy(level = it.toInt())) }, valueRange = 0f..100f)
                }
                ActionType.SET_BRIGHTNESS -> {
                    Text("Brightness: ${a.level.coerceAtLeast(1)}%")
                    Slider(value = a.level.coerceAtLeast(1).toFloat(), onValueChange = { onChange(a.copy(level = it.toInt())) }, valueRange = 1f..100f)
                }
                ActionType.SYSTEM_ACTION ->
                    DropdownField(
                        label = "Button", items = SystemKeys.actions,
                        selected = SystemKeys.actions.firstOrNull { it.first == a.sysAction } ?: SystemKeys.actions[0],
                        text = { tr(it.second) }
                    ) { onChange(a.copy(sysAction = it.first)) }
                ActionType.OPEN_SETTINGS ->
                    DropdownField(
                        label = "Open a settings page", items = SystemKeys.settings,
                        selected = SystemKeys.settings.firstOrNull { it.first == a.sysAction } ?: SystemKeys.settings[0],
                        text = { tr(it.second) }
                    ) { onChange(a.copy(sysAction = it.first)) }
                else -> {}
            }
            stepHint(a)?.let { (text, doc) -> Hint(text, doc, onOpenDoc) }
            if (index > 0) {
                Text(
                    if (a.delaySec == 0) tr("Run right after the previous step") else tr("Wait") + " ${a.delaySec} s",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(value = a.delaySec.toFloat(), onValueChange = { onChange(a.copy(delaySec = it.toInt())) }, valueRange = 0f..60f)
            }
        }
    }
}

@Composable
private fun ConditionCard(c: Condition, onChange: (Condition) -> Unit, onDelete: () -> Unit, onPickTime: (Boolean) -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                DropdownField(
                    label = "Condition", items = ConditionType.entries, selected = c.type,
                    text = { "${it.emoji}  ${tr(it.label)}" }
                ) { onChange(defaultCondition(it)) }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Remove condition") }
        }
        Column(Modifier.animateContentSize().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (c.type) {
                ConditionType.TIME_BETWEEN ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onPickTime(false) }, modifier = Modifier.weight(1f)) { Text(tr("From") + " " + hm(c.a)) }
                        OutlinedButton(onClick = { onPickTime(true) }, modifier = Modifier.weight(1f)) { Text(tr("Until") + " " + hm(c.b)) }
                    }
                ConditionType.DAYS -> DaysRow(c.a) { onChange(c.copy(a = it)) }
                ConditionType.BATTERY_ABOVE, ConditionType.BATTERY_BELOW -> {
                    Text("${c.a}%")
                    Slider(value = c.a.toFloat(), onValueChange = { onChange(c.copy(a = it.toInt())) }, valueRange = 5f..95f)
                }
                ConditionType.WIFI_NAME ->
                    OutlinedTextField(
                        value = c.text, onValueChange = { onChange(c.copy(text = it)) },
                        label = { Text(tr("Wi-Fi name")) }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                else -> {}
            }
        }
    }
}

@Composable
private fun Hint(text: String, docId: String, onOpenDoc: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "ℹ️  " + tr(text), Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = { onOpenDoc(docId) }) { Text(tr("Learn more")) }
    }
}

@Composable
private fun <T> DropdownField(
    label: String, items: List<T>, selected: T, text: (T) -> String,
    groupOf: ((T) -> String)? = null, onSelect: (T) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().bouncyClick { open = true },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(tr(label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text(selected), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
                }
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.background(dialogContainer())) {
            var last = ""
            items.forEach { item ->
                val g = groupOf?.invoke(item)
                if (g != null && g != last) {
                    last = g
                    DropdownMenuItem(
                        text = { Text(tr(g), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) },
                        onClick = {}, enabled = false
                    )
                }
                DropdownMenuItem(text = { Text(text(item)) }, onClick = { onSelect(item); open = false })
            }
        }
    }
}

@Composable
internal fun DaysRow(mask: Int, onChange: (Int) -> Unit) {
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
        confirmButton = { TextButton(onClick = { onConfirm(st.hour, st.minute) }) { Text(tr("OK")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Cancel")) } },
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
        title = { Text(tr("Choose an app")) },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Cancel")) } },
        text = {
            Column {
                OutlinedTextField(
                    value = q, onValueChange = { q = it }, singleLine = true,
                    label = { Text(tr("Search")) }, modifier = Modifier.fillMaxWidth()
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
