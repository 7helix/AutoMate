package com.automate.app.data

import java.util.UUID

enum class TriggerType(val label: String, val emoji: String, val group: String) {
    TIME("At a specific time", "⏰", "Time"),
    REPEAT("Repeat at an interval", "🔁", "Time"),
    BATTERY_LOW("Battery gets low", "🪫", "Power"),
    CHARGER_CONNECTED("Charger connected", "🔌", "Power"),
    CHARGER_DISCONNECTED("Charger disconnected", "🔋", "Power"),
    POWER_SAVER_ON("Battery saver turns on", "🌿", "Power"),
    POWER_SAVER_OFF("Battery saver turns off", "🍃", "Power"),
    WIFI_CONNECTED("Wi-Fi connected", "📶", "Connectivity"),
    WIFI_DISCONNECTED("Wi-Fi disconnected", "📴", "Connectivity"),
    BLUETOOTH_CONNECTED("Bluetooth connected", "🎧", "Connectivity"),
    BLUETOOTH_DISCONNECTED("Bluetooth disconnected", "🔕", "Connectivity"),
    AIRPLANE_ON("Airplane mode turned on", "✈️", "Connectivity"),
    AIRPLANE_OFF("Airplane mode turned off", "🛬", "Connectivity"),
    HEADPHONES_PLUGGED("Wired headphones plugged in", "🎶", "Audio"),
    HEADPHONES_UNPLUGGED("Wired headphones unplugged", "🔇", "Audio"),
    SCREEN_ON("Screen turns on", "🌞", "Device"),
    SCREEN_OFF("Screen turns off", "🌙", "Device"),
    DEVICE_UNLOCKED("Phone unlocked", "🔓", "Device"),
    DEVICE_BOOT("Phone starts up", "🏁", "Device"),
    LOCATION_ENTER("Arrive at a place", "📍", "Place"),
    LOCATION_EXIT("Leave a place", "🚶", "Place")
}

enum class ActionGroup(val label: String) {
    ALERTS("Alerts & messages"),
    COMMS("Calls & messaging"),
    SOUND("Sound & display"),
    APPS("Apps & links"),
    PHONE("Phone control"),
    SOCIAL("Social")
}

enum class ActionType(val label: String, val emoji: String, val group: ActionGroup) {
    NOTIFICATION("Send a notification", "🔔", ActionGroup.ALERTS),
    TOAST("Show a toast message", "💬", ActionGroup.ALERTS),
    SPEAK_TEXT("Speak text aloud", "🗣️", ActionGroup.ALERTS),
    COPY_TEXT("Copy text to clipboard", "📋", ActionGroup.ALERTS),
    DIAL_NUMBER("Open dialer with a number", "📞", ActionGroup.COMMS),
    SMS_DRAFT("Start a text message", "✉️", ActionGroup.COMMS),
    EMAIL_DRAFT("Start an email", "📧", ActionGroup.COMMS),
    WHATSAPP_CHAT("Open WhatsApp chat with text", "🟢", ActionGroup.COMMS),
    SHARE_TEXT("Open share sheet with text", "📤", ActionGroup.COMMS),
    VIBRATE("Vibrate the phone", "📳", ActionGroup.SOUND),
    PLAY_SOUND("Play a notification sound", "🔔", ActionGroup.SOUND),
    RINGER_MODE("Change ringer mode", "🔊", ActionGroup.SOUND),
    SET_VOLUME("Set a volume level", "🎚️", ActionGroup.SOUND),
    SET_BRIGHTNESS("Set screen brightness", "🔆", ActionGroup.SOUND),
    DND_MODE("Change Do Not Disturb", "🌙", ActionGroup.SOUND),
    LAUNCH_APP("Open an app", "🚀", ActionGroup.APPS),
    OPEN_URL("Open a link", "🔗", ActionGroup.APPS),
    OPEN_CAMERA("Open the camera", "📷", ActionGroup.APPS),
    FLASHLIGHT("Toggle flashlight", "🔦", ActionGroup.PHONE),
    SYSTEM_ACTION("Press a system button", "🎛️", ActionGroup.PHONE),
    OPEN_SETTINGS("Open a settings page", "⚙️", ActionGroup.PHONE),
    OPEN_WIFI_PANEL("Open Wi-Fi panel", "🛜", ActionGroup.PHONE),
    SOCIAL_OPEN("Open a social app or profile", "🌐", ActionGroup.SOCIAL)
}

enum class ConditionType(val label: String, val emoji: String) {
    TIME_BETWEEN("Time is between", "🕒"),
    DAYS("Day of the week is", "📅"),
    BATTERY_ABOVE("Battery is above", "🔋"),
    BATTERY_BELOW("Battery is below", "🪫"),
    CHARGING("Phone is charging", "🔌"),
    NOT_CHARGING("Phone is not charging", "⚡"),
    WIFI_ON("Wi-Fi is connected", "📶"),
    WIFI_OFF("Wi-Fi is not connected", "📴"),
    WIFI_NAME("Connected to Wi-Fi named", "🏠"),
    AIRPLANE_ON("Airplane mode is on", "✈️"),
    AIRPLANE_OFF("Airplane mode is off", "🛬"),
    SCREEN_ON("Screen is on", "🌞"),
    DND_ON("Do Not Disturb is on", "🔕")
}

/** One step of a rule. ringer: 0 silent / 1 vibrate / 2 normal. stream: 0 media, 1 ring, 2 alarm, 3 alerts. */
data class ActionItem(
    val type: ActionType = ActionType.NOTIFICATION,
    val message: String = "",
    val packageName: String = "",
    val url: String = "",
    val social: String = "instagram",
    val ringer: Int = 1,
    val level: Int = 50,
    val stream: Int = 0,
    val sysAction: String = "home",
    val delaySec: Int = 0
)

/** a / b meaning depends on the type: minutes since midnight, a day mask or a battery percent. */
data class Condition(
    val type: ConditionType = ConditionType.TIME_BETWEEN,
    val a: Int = 0,
    val b: Int = 0,
    val text: String = ""
)

/** days: bit0 = Monday ... bit6 = Sunday. */
data class Rule(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val enabled: Boolean = true,
    val trigger: TriggerType = TriggerType.TIME,
    val hour: Int = 8,
    val minute: Int = 0,
    val days: Int = 127,
    val threshold: Int = 20,
    val filter: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radius: Int = 150,
    val intervalMin: Int = 30,
    val conditions: List<Condition> = emptyList(),
    val matchAll: Boolean = true,
    val actions: List<ActionItem> = listOf(ActionItem()),
    val folder: String = "",
    val lastRun: Long = 0L,
    val runCount: Int = 0
)

fun defaultCondition(type: ConditionType): Condition = when (type) {
    ConditionType.TIME_BETWEEN -> Condition(type, a = 9 * 60, b = 18 * 60)
    ConditionType.DAYS -> Condition(type, a = 31)
    ConditionType.BATTERY_ABOVE -> Condition(type, a = 50)
    ConditionType.BATTERY_BELOW -> Condition(type, a = 20)
    else -> Condition(type)
}

fun hm(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

fun intervalText(min: Int): String = when {
    min >= 60 && min % 60 == 0 -> if (min == 60) "hour" else "${min / 60} hours"
    else -> "$min minutes"
}

fun daysText(mask: Int): String {
    if (mask == 127) return "every day"
    if (mask == 31) return "weekdays"
    if (mask == 96) return "weekends"
    val n = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return n.filterIndexed { i, _ -> (mask and (1 shl i)) != 0 }.joinToString(", ")
}

fun streamLabel(s: Int): String = when (s) { 0 -> "Media"; 1 -> "Ring"; 2 -> "Alarm"; else -> "Alerts" }
fun ringerLabel(r: Int): String = when (r) { 0 -> "silent"; 1 -> "vibrate"; else -> "normal" }

fun Rule.whenText(): String = when (trigger) {
    TriggerType.TIME -> "At %02d:%02d, %s".format(hour, minute, daysText(days))
    TriggerType.REPEAT -> "Every " + intervalText(intervalMin)
    TriggerType.BATTERY_LOW -> "Battery at or below $threshold%"
    TriggerType.CHARGER_CONNECTED -> "Charger plugged in"
    TriggerType.CHARGER_DISCONNECTED -> "Charger unplugged"
    TriggerType.POWER_SAVER_ON -> "Battery saver turns on"
    TriggerType.POWER_SAVER_OFF -> "Battery saver turns off"
    TriggerType.WIFI_CONNECTED -> if (filter.isBlank()) "Any Wi-Fi connects" else "Wi-Fi “$filter” connects"
    TriggerType.WIFI_DISCONNECTED -> if (filter.isBlank()) "Wi-Fi disconnects" else "Wi-Fi “$filter” disconnects"
    TriggerType.BLUETOOTH_CONNECTED -> if (filter.isBlank()) "Any Bluetooth device connects" else "“$filter” connects"
    TriggerType.BLUETOOTH_DISCONNECTED -> if (filter.isBlank()) "Any Bluetooth device disconnects" else "“$filter” disconnects"
    TriggerType.AIRPLANE_ON -> "Airplane mode turned on"
    TriggerType.AIRPLANE_OFF -> "Airplane mode turned off"
    TriggerType.HEADPHONES_PLUGGED -> "Wired headphones plugged in"
    TriggerType.HEADPHONES_UNPLUGGED -> "Wired headphones unplugged"
    TriggerType.SCREEN_ON -> "Screen turns on"
    TriggerType.SCREEN_OFF -> "Screen turns off"
    TriggerType.DEVICE_UNLOCKED -> "Phone is unlocked"
    TriggerType.DEVICE_BOOT -> "Phone starts up"
    TriggerType.LOCATION_ENTER -> "Arrive within $radius m of saved place"
    TriggerType.LOCATION_EXIT -> "Leave $radius m radius of saved place"
}

fun Condition.summary(): String = when (type) {
    ConditionType.TIME_BETWEEN -> "time is ${hm(a)}–${hm(b)}"
    ConditionType.DAYS -> "day is " + daysText(a)
    ConditionType.BATTERY_ABOVE -> "battery above $a%"
    ConditionType.BATTERY_BELOW -> "battery below $a%"
    ConditionType.CHARGING -> "charging"
    ConditionType.NOT_CHARGING -> "not charging"
    ConditionType.WIFI_ON -> "Wi-Fi connected"
    ConditionType.WIFI_OFF -> "Wi-Fi not connected"
    ConditionType.WIFI_NAME -> "Wi-Fi is “${text.ifBlank { "…" }}”"
    ConditionType.AIRPLANE_ON -> "airplane mode on"
    ConditionType.AIRPLANE_OFF -> "airplane mode off"
    ConditionType.SCREEN_ON -> "screen on"
    ConditionType.DND_ON -> "Do Not Disturb on"
}

fun Rule.ifText(): String =
    if (conditions.isEmpty()) "" else conditions.joinToString(if (matchAll) " and " else " or ") { it.summary() }

fun ActionItem.summary(ruleName: String = ""): String = when (type) {
    ActionType.NOTIFICATION -> "Notify: " + message.ifBlank { "(default text)" }
    ActionType.TOAST -> "Toast: " + message.ifBlank { ruleName.ifBlank { "(rule name)" } }
    ActionType.SPEAK_TEXT -> "Speak: " + message.ifBlank { "(text)" }
    ActionType.COPY_TEXT -> "Copy: " + message.ifBlank { ruleName.ifBlank { "(text)" } }
    ActionType.DIAL_NUMBER -> "Dial " + url.ifBlank { "(number)" }
    ActionType.SMS_DRAFT -> "Text " + url.ifBlank { "(number)" }
    ActionType.EMAIL_DRAFT -> "Email " + url.ifBlank { "(address)" }
    ActionType.WHATSAPP_CHAT -> "WhatsApp chat " + url.ifBlank { "(number needed)" }
    ActionType.SHARE_TEXT -> "Share: " + message.ifBlank { ruleName.ifBlank { "(text)" } }
    ActionType.VIBRATE -> "Vibrate"
    ActionType.PLAY_SOUND -> "Play a sound"
    ActionType.RINGER_MODE -> "Ringer: " + ringerLabel(ringer)
    ActionType.SET_VOLUME -> streamLabel(stream) + " volume to $level%"
    ActionType.SET_BRIGHTNESS -> "Brightness to $level%"
    ActionType.DND_MODE -> "Do Not Disturb: " + when (ringer) { 0 -> "block all"; 1 -> "priority only"; else -> "off" }
    ActionType.LAUNCH_APP -> "Open " + packageName.ifBlank { "an app" }
    ActionType.OPEN_URL -> "Open " + url.ifBlank { "a link" }
    ActionType.OPEN_CAMERA -> "Open camera"
    ActionType.FLASHLIGHT -> "Toggle flashlight"
    ActionType.SYSTEM_ACTION -> "Press: " + (SystemKeys.actions.firstOrNull { it.first == sysAction }?.second ?: sysAction)
    ActionType.OPEN_SETTINGS -> "Open " + (SystemKeys.settings.firstOrNull { it.first == sysAction }?.second ?: "settings")
    ActionType.OPEN_WIFI_PANEL -> "Open Wi-Fi panel"
    ActionType.SOCIAL_OPEN -> "Open " + (Social.find(social)?.label ?: "social app") + if (url.isBlank()) "" else " ($url)"
}

fun Rule.thenText(): String =
    if (actions.isEmpty()) "Nothing" else actions.joinToString("  →  ") { it.summary(name) }

data class Template(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val category: String,
    val rule: Rule
)

private fun step(type: ActionType, message: String = "", pkg: String = "", url: String = "", social: String = "instagram",
                 ringer: Int = 1, level: Int = 50, stream: Int = 0, sys: String = "home") =
    ActionItem(type, message, pkg, url, social, ringer, level, stream, sys)

object Templates {
    val all = listOf(
        // Everyday
        Template("Good morning", "Greeting at 7:30 on weekdays", "☀️", "Everyday",
            Rule(name = "Good morning", trigger = TriggerType.TIME, hour = 7, minute = 30, days = 31,
                actions = listOf(step(ActionType.NOTIFICATION, "Good morning! Make today great ☀️")))),
        Template("Wind down", "Bedtime reminder at 10:30 pm", "🌙", "Everyday",
            Rule(name = "Wind down", trigger = TriggerType.TIME, hour = 22, minute = 30,
                actions = listOf(step(ActionType.NOTIFICATION, "Time to wind down 🌙")))),
        Template("Welcome home", "Message when your Wi-Fi connects", "🏠", "Everyday",
            Rule(name = "Welcome home", trigger = TriggerType.WIFI_CONNECTED,
                actions = listOf(step(ActionType.TOAST, "Welcome home 🏠")))),
        Template("Hydration reminder", "Hourly, 9 am to 9 pm", "💧", "Everyday",
            Rule(name = "Hydration reminder", trigger = TriggerType.REPEAT, intervalMin = 60,
                conditions = listOf(Condition(ConditionType.TIME_BETWEEN, 9 * 60, 21 * 60)),
                actions = listOf(step(ActionType.NOTIFICATION, "Time for a glass of water 💧")))),
        Template("Stretch break", "Every 45 min on workdays", "🧘", "Everyday",
            Rule(name = "Stretch break", trigger = TriggerType.REPEAT, intervalMin = 45,
                conditions = listOf(Condition(ConditionType.TIME_BETWEEN, 9 * 60, 18 * 60), Condition(ConditionType.DAYS, 31)),
                actions = listOf(step(ActionType.VIBRATE), step(ActionType.NOTIFICATION, "Stand up and stretch for a minute 🧘")))),
        Template("Restart notice", "Confirms after every reboot", "🏁", "Everyday",
            Rule(name = "Restart notice", trigger = TriggerType.DEVICE_BOOT,
                actions = listOf(step(ActionType.NOTIFICATION, "Your phone restarted successfully ✅")))),

        // Power
        Template("Low battery alert", "Warn me at 15%", "🪫", "Power",
            Rule(name = "Low battery alert", trigger = TriggerType.BATTERY_LOW, threshold = 15,
                actions = listOf(step(ActionType.NOTIFICATION, "Battery is low. Plug in soon 🔋")))),
        Template("Battery saver assist", "Dim the screen at 20%", "🔅", "Power",
            Rule(name = "Battery saver assist", trigger = TriggerType.BATTERY_LOW, threshold = 20,
                actions = listOf(step(ActionType.SET_BRIGHTNESS, level = 30), step(ActionType.NOTIFICATION, "Screen dimmed to save battery.")))),
        Template("Unplug reminder", "Vibrate when the charger is removed", "🔌", "Power",
            Rule(name = "Unplug reminder", trigger = TriggerType.CHARGER_DISCONNECTED,
                actions = listOf(step(ActionType.VIBRATE)))),
        Template("Battery saver on", "Confirm when it turns on", "🌿", "Power",
            Rule(name = "Battery saver on", trigger = TriggerType.POWER_SAVER_ON,
                actions = listOf(step(ActionType.TOAST, "Battery saver is on 🌿")))),

        // Sound & focus
        Template("Quiet at work", "Arrive → vibrate", "🤫", "Sound & focus",
            Rule(name = "Quiet at work", trigger = TriggerType.LOCATION_ENTER,
                actions = listOf(step(ActionType.RINGER_MODE, ringer = 1)))),
        Template("Night mode", "Dim and quiet at 10 pm", "🌃", "Sound & focus",
            Rule(name = "Night mode", trigger = TriggerType.TIME, hour = 22, minute = 0,
                actions = listOf(step(ActionType.SET_BRIGHTNESS, level = 15), step(ActionType.RINGER_MODE, ringer = 1)))),
        Template("Headphone volume", "Safe media volume on plug-in", "🎶", "Sound & focus",
            Rule(name = "Headphone volume", trigger = TriggerType.HEADPHONES_PLUGGED,
                actions = listOf(step(ActionType.SET_VOLUME, level = 40, stream = 0)))),
        Template("Spoken greeting", "Morning greeting when the screen wakes", "🗣️", "Sound & focus",
            Rule(name = "Spoken greeting", trigger = TriggerType.SCREEN_ON,
                conditions = listOf(Condition(ConditionType.TIME_BETWEEN, 6 * 60, 10 * 60)),
                actions = listOf(step(ActionType.SPEAK_TEXT, "Good morning. Have a productive day.")))),
        Template("Focus hour", "Priority-only calls at 9 am", "🎯", "Sound & focus",
            Rule(name = "Focus hour", trigger = TriggerType.TIME, hour = 9, minute = 0, days = 31,
                actions = listOf(step(ActionType.DND_MODE, ringer = 1)))),

        // Travel
        Template("Car mode", "Bluetooth → loud volume and Maps", "🚗", "Travel",
            Rule(name = "Car mode", trigger = TriggerType.BLUETOOTH_CONNECTED,
                actions = listOf(step(ActionType.SET_VOLUME, level = 70, stream = 0),
                    step(ActionType.LAUNCH_APP, pkg = "com.google.android.apps.maps")))),
        Template("Flight mode reminder", "Confirm when airplane mode is on", "✈️", "Travel",
            Rule(name = "Flight mode reminder", trigger = TriggerType.AIRPLANE_ON,
                actions = listOf(step(ActionType.TOAST, "Airplane mode is on ✈️")))),
        Template("Back online", "Prompt when airplane mode ends", "🛬", "Travel",
            Rule(name = "Back online", trigger = TriggerType.AIRPLANE_OFF,
                actions = listOf(step(ActionType.NOTIFICATION, "You are back online. Check your messages.")))),

        // Phone control
        Template("Night lock", "Lock the screen at 11:30 pm", "🔒", "Phone control",
            Rule(name = "Night lock", trigger = TriggerType.TIME, hour = 23, minute = 30,
                actions = listOf(step(ActionType.SYSTEM_ACTION, sys = "lock")))),
        Template("Morning notifications", "Open the shade when you wake the phone", "🔔", "Phone control",
            Rule(name = "Morning notifications", trigger = TriggerType.SCREEN_ON,
                conditions = listOf(Condition(ConditionType.TIME_BETWEEN, 7 * 60, 9 * 60)),
                actions = listOf(step(ActionType.SYSTEM_ACTION, sys = "notifications")))),

        // Social
        Template("Post reminder", "6 pm nudge to create", "📸", "Social",
            Rule(name = "Post reminder", trigger = TriggerType.TIME, hour = 18, minute = 0,
                actions = listOf(step(ActionType.NOTIFICATION, "Time to post something! 📸")))),
        Template("Stream time", "Open Twitch at 8 pm", "🎮", "Social",
            Rule(name = "Stream time", trigger = TriggerType.TIME, hour = 20, minute = 0,
                actions = listOf(step(ActionType.SOCIAL_OPEN, social = "twitch")))),
        Template("Good morning text", "WhatsApp chat ready at 8 am", "🟢", "Social",
            Rule(name = "Good morning text", trigger = TriggerType.TIME, hour = 8, minute = 0,
                actions = listOf(step(ActionType.WHATSAPP_CHAT, "Good morning! ☀️")))),
        Template("Share my day", "Share sheet at 9 pm", "📤", "Social",
            Rule(name = "Share my day", trigger = TriggerType.TIME, hour = 21, minute = 0,
                actions = listOf(step(ActionType.SHARE_TEXT, "My day in one line: ")))),
        Template("Caption saver", "Copy a caption when charging", "📋", "Social",
            Rule(name = "Caption saver", trigger = TriggerType.CHARGER_CONNECTED,
                actions = listOf(step(ActionType.COPY_TEXT, "Your caption here ✨ #automate"))))
    )

    val categories: List<String> = all.map { it.category }.distinct()

    val socialIndexed: List<IndexedValue<Template>> = all.withIndex().filter { it.value.category == "Social" }
}
