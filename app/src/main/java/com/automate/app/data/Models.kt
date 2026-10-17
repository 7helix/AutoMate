package com.automate.app.data

import java.util.UUID

enum class TriggerType(val label: String, val emoji: String) {
    TIME("At a specific time", "⏰"),
    BATTERY_LOW("Battery gets low", "🪫"),
    CHARGER_CONNECTED("Charger connected", "🔌"),
    CHARGER_DISCONNECTED("Charger disconnected", "🔋"),
    WIFI_CONNECTED("Wi-Fi connected", "📶"),
    WIFI_DISCONNECTED("Wi-Fi disconnected", "📴"),
    BLUETOOTH_CONNECTED("Bluetooth connected", "🎧"),
    BLUETOOTH_DISCONNECTED("Bluetooth disconnected", "🔕"),
    LOCATION_ENTER("Arrive at a place", "📍"),
    LOCATION_EXIT("Leave a place", "🚶"),
    DEVICE_UNLOCKED("Phone unlocked", "🔓")
}

enum class ActionType(val label: String, val emoji: String) {
    NOTIFICATION("Send a notification", "🔔"),
    TOAST("Show a toast message", "💬"),
    LAUNCH_APP("Open an app", "🚀"),
    OPEN_URL("Open a link", "🔗"),
    VIBRATE("Vibrate the phone", "📳"),
    FLASHLIGHT("Toggle flashlight", "🔦"),
    RINGER_MODE("Change ringer mode", "🔊"),
    OPEN_WIFI_PANEL("Open Wi-Fi panel", "🛜"),
    SOCIAL_OPEN("Open a social app or profile", "🌐"),
    SHARE_TEXT("Open share sheet with text", "📤"),
    WHATSAPP_CHAT("Open WhatsApp chat with text", "🟢"),
    COPY_TEXT("Copy text to clipboard", "📋")
}

/** days: bit0 = Monday ... bit6 = Sunday. ringer: 0 silent, 1 vibrate, 2 normal. */
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
    val action: ActionType = ActionType.NOTIFICATION,
    val message: String = "",
    val packageName: String = "",
    val url: String = "",
    val social: String = "instagram",
    val ringer: Int = 1,
    val lastRun: Long = 0L,
    val runCount: Int = 0
)

fun daysText(mask: Int): String {
    if (mask == 127) return "every day"
    if (mask == 31) return "weekdays"
    if (mask == 96) return "weekends"
    val n = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return n.filterIndexed { i, _ -> (mask and (1 shl i)) != 0 }.joinToString(", ")
}

fun Rule.whenText(): String = when (trigger) {
    TriggerType.TIME -> "At %02d:%02d, %s".format(hour, minute, daysText(days))
    TriggerType.BATTERY_LOW -> "Battery at or below $threshold%"
    TriggerType.CHARGER_CONNECTED -> "Charger plugged in"
    TriggerType.CHARGER_DISCONNECTED -> "Charger unplugged"
    TriggerType.WIFI_CONNECTED -> if (filter.isBlank()) "Any Wi-Fi connects" else "Wi-Fi “$filter” connects"
    TriggerType.WIFI_DISCONNECTED -> if (filter.isBlank()) "Wi-Fi disconnects" else "Wi-Fi “$filter” disconnects"
    TriggerType.BLUETOOTH_CONNECTED -> if (filter.isBlank()) "Any Bluetooth device connects" else "“$filter” connects"
    TriggerType.BLUETOOTH_DISCONNECTED -> if (filter.isBlank()) "Any Bluetooth device disconnects" else "“$filter” disconnects"
    TriggerType.LOCATION_ENTER -> "Arrive within ${radius} m of saved place"
    TriggerType.LOCATION_EXIT -> "Leave ${radius} m radius of saved place"
    TriggerType.DEVICE_UNLOCKED -> "Phone is unlocked"
}

fun Rule.thenText(): String = when (action) {
    ActionType.NOTIFICATION -> "Notify: " + message.ifBlank { "(default text)" }
    ActionType.TOAST -> "Toast: " + message.ifBlank { name.ifBlank { "(rule name)" } }
    ActionType.LAUNCH_APP -> "Open " + packageName.ifBlank { "an app" }
    ActionType.OPEN_URL -> "Open " + url.ifBlank { "a link" }
    ActionType.VIBRATE -> "Vibrate"
    ActionType.FLASHLIGHT -> "Toggle flashlight"
    ActionType.RINGER_MODE -> "Set ringer to " + when (ringer) { 0 -> "silent"; 1 -> "vibrate"; else -> "normal" }
    ActionType.OPEN_WIFI_PANEL -> "Open Wi-Fi panel"
    ActionType.SOCIAL_OPEN -> "Open " + (Social.find(social)?.label ?: "social app") + if (url.isBlank()) "" else " ($url)"
    ActionType.SHARE_TEXT -> "Share: " + message.ifBlank { name.ifBlank { "(text)" } }
    ActionType.WHATSAPP_CHAT -> "WhatsApp chat " + url.ifBlank { "(number needed)" }
    ActionType.COPY_TEXT -> "Copy: " + message.ifBlank { name.ifBlank { "(text)" } }
}

data class Template(val title: String, val subtitle: String, val emoji: String, val rule: Rule)

object Templates {
    val basic = listOf(
        Template("Low battery alert", "Warn me at 15%", "🪫",
            Rule(name = "Low battery alert", trigger = TriggerType.BATTERY_LOW, threshold = 15,
                action = ActionType.NOTIFICATION, message = "Battery is low — plug in soon 🔋")),
        Template("Good morning", "Greeting at 7:30", "☀️",
            Rule(name = "Good morning", trigger = TriggerType.TIME, hour = 7, minute = 30,
                action = ActionType.NOTIFICATION, message = "Good morning! Make today great ☀️")),
        Template("Welcome home", "When Wi-Fi connects", "🏠",
            Rule(name = "Welcome home", trigger = TriggerType.WIFI_CONNECTED,
                action = ActionType.TOAST, message = "Welcome home 🏠")),
        Template("Car mode", "Bluetooth → Maps", "🚗",
            Rule(name = "Car mode", trigger = TriggerType.BLUETOOTH_CONNECTED,
                action = ActionType.LAUNCH_APP, packageName = "com.google.android.apps.maps")),
        Template("Quiet at work", "Arrive → vibrate", "🤫",
            Rule(name = "Quiet at work", trigger = TriggerType.LOCATION_ENTER,
                action = ActionType.RINGER_MODE, ringer = 1)),
        Template("Wind down", "Bedtime reminder", "🌙",
            Rule(name = "Wind down", trigger = TriggerType.TIME, hour = 22, minute = 30,
                action = ActionType.NOTIFICATION, message = "Time to wind down 🌙"))
    )

    val social = listOf(
        Template("Post reminder", "6 pm nudge to create", "📸",
            Rule(name = "Post reminder", trigger = TriggerType.TIME, hour = 18, minute = 0,
                action = ActionType.NOTIFICATION, message = "Time to post something! 📸")),
        Template("Stream time", "Open Twitch at 8 pm", "🎮",
            Rule(name = "Stream time", trigger = TriggerType.TIME, hour = 20, minute = 0,
                action = ActionType.SOCIAL_OPEN, social = "twitch")),
        Template("Good morning text", "WhatsApp chat ready", "🟢",
            Rule(name = "Good morning text", trigger = TriggerType.TIME, hour = 8, minute = 0,
                action = ActionType.WHATSAPP_CHAT, message = "Good morning! ☀️")),
        Template("Share my day", "Share sheet at 9 pm", "📤",
            Rule(name = "Share my day", trigger = TriggerType.TIME, hour = 21, minute = 0,
                action = ActionType.SHARE_TEXT, message = "My day in one line: ")),
        Template("Caption saver", "Copy a caption on charge", "📋",
            Rule(name = "Caption saver", trigger = TriggerType.CHARGER_CONNECTED,
                action = ActionType.COPY_TEXT, message = "Your caption here ✨ #automate"))
    )

    val all = basic + social
}
