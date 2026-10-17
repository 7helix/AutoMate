package com.automate.app.data

import android.content.Context

enum class AppTheme(val label: String) {
    MATERIAL_YOU("Material You"),
    AMOLED("Dark AMOLED"),
    GRADIENT("Colorful gradient"),
    LIGHT("Minimal light")
}

enum class ToastStyle(val label: String) {
    IN_APP("Pop-up"),
    SYSTEM("System"),
    BOTH("Both")
}

object Prefs {
    const val THEME = "theme"
    const val ANIMATIONS = "animations"
    const val HAPTICS = "haptics"
    const val TOAST_STYLE = "toast_style"
    const val NAME = "name"
    const val ONBOARDED = "onboarded"
    const val RUN_ON_BOOT = "run_on_boot"
    const val SERVICE_ENABLED = "service_enabled"
    const val PROJECT_LINK = "project_link"
    const val LANG = "lang"

    private fun sp(c: Context) = c.getSharedPreferences("automate_settings", Context.MODE_PRIVATE)

    @JvmStatic fun getBool(c: Context, k: String, d: Boolean): Boolean = sp(c).getBoolean(k, d)
    @JvmStatic fun setBool(c: Context, k: String, v: Boolean) { sp(c).edit().putBoolean(k, v).apply() }
    @JvmStatic fun getString(c: Context, k: String, d: String): String = sp(c).getString(k, d) ?: d
    @JvmStatic fun setString(c: Context, k: String, v: String) { sp(c).edit().putString(k, v).apply() }
}
