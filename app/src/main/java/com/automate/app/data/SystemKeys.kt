package com.automate.app.data

import android.content.Context
import android.provider.Settings

object SystemKeys {
    /** Global actions performed by the optional Accessibility service. */
    val actions = listOf(
        "home" to "Home button",
        "back" to "Back button",
        "recents" to "Recent apps",
        "notifications" to "Notification shade",
        "quick_settings" to "Quick settings",
        "lock" to "Lock screen",
        "screenshot" to "Take screenshot",
        "power_menu" to "Power menu"
    )

    val settings = listOf(
        "main" to "Settings home",
        "wifi" to "Wi-Fi settings",
        "network" to "Network settings",
        "bluetooth" to "Bluetooth settings",
        "display" to "Display settings",
        "sound" to "Sound settings",
        "battery" to "Battery saver",
        "location" to "Location settings",
        "apps" to "App settings",
        "date" to "Date and time",
        "accessibility" to "Accessibility settings",
        "airplane" to "Airplane mode settings",
        "nfc" to "NFC settings"
    )

    @JvmStatic
    fun settingsAction(key: String): String = when (key) {
        "wifi" -> Settings.ACTION_WIFI_SETTINGS
        "network" -> Settings.ACTION_WIRELESS_SETTINGS
        "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
        "display" -> Settings.ACTION_DISPLAY_SETTINGS
        "sound" -> Settings.ACTION_SOUND_SETTINGS
        "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
        "location" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
        "apps" -> Settings.ACTION_APPLICATION_SETTINGS
        "date" -> Settings.ACTION_DATE_SETTINGS
        "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
        "airplane" -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
        "nfc" -> Settings.ACTION_NFC_SETTINGS
        else -> Settings.ACTION_SETTINGS
    }
}

/** True when the user has switched on AutoMate's Accessibility service. */
fun isAccessibilityEnabled(ctx: Context): Boolean {
    val enabled = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        ?: return false
    val full = "${ctx.packageName}/com.automate.app.engine.AutoMateAccessibilityService"
    val short = "${ctx.packageName}/.engine.AutoMateAccessibilityService"
    return enabled.split(':').any { it.equals(full, true) || it.equals(short, true) }
}
