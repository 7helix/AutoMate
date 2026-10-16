package com.automate.app

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.automate.app.data.AppTheme
import com.automate.app.data.LogStore
import com.automate.app.data.Prefs
import com.automate.app.data.Rule
import com.automate.app.data.RuleStore
import com.automate.app.data.ToastStyle
import com.automate.app.engine.AlarmScheduler
import com.automate.app.engine.AutomationService
import com.automate.app.engine.RuleEngine
import com.automate.app.ui.ToastController
import com.automate.app.ui.ToastKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

inline fun <reified T : Enum<T>> enumOr(name: String, def: T): T =
    try { enumValueOf<T>(name) } catch (e: Exception) { def }

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx: Application = app
    val toasts = ToastController()

    var rules by mutableStateOf(RuleStore.load(app))
        private set
    var theme by mutableStateOf(enumOr(Prefs.getString(app, Prefs.THEME, ""), AppTheme.MATERIAL_YOU))
        private set
    var toastStyle by mutableStateOf(enumOr(Prefs.getString(app, Prefs.TOAST_STYLE, ""), ToastStyle.IN_APP))
        private set
    var animations by mutableStateOf(Prefs.getBool(app, Prefs.ANIMATIONS, true))
        private set
    var haptics by mutableStateOf(Prefs.getBool(app, Prefs.HAPTICS, true))
        private set
    var runOnBoot by mutableStateOf(Prefs.getBool(app, Prefs.RUN_ON_BOOT, true))
        private set
    var onboarded by mutableStateOf(Prefs.getBool(app, Prefs.ONBOARDED, false))
        private set
    var userName by mutableStateOf(Prefs.getString(app, Prefs.NAME, ""))
        private set
    var serviceOn by mutableStateOf(Prefs.getBool(app, Prefs.SERVICE_ENABLED, false))
        private set

    init {
        // Rules get run counts updated by the background engine; keep UI in sync.
        viewModelScope.launch { LogStore.version.collect { rules = RuleStore.load(ctx) } }
    }

    // ---- toasts ----
    fun toast(text: String, kind: ToastKind = ToastKind.INFO) {
        if (toastStyle != ToastStyle.SYSTEM) toasts.show(text, kind)
        if (toastStyle != ToastStyle.IN_APP) Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
    }

    // ---- settings ----
    fun setTheme(t: AppTheme) { theme = t; Prefs.setString(ctx, Prefs.THEME, t.name) }
    fun setToastStyle(s: ToastStyle) { toastStyle = s; Prefs.setString(ctx, Prefs.TOAST_STYLE, s.name) }
    fun setAnimations(v: Boolean) { animations = v; Prefs.setBool(ctx, Prefs.ANIMATIONS, v) }
    fun setHaptics(v: Boolean) { haptics = v; Prefs.setBool(ctx, Prefs.HAPTICS, v) }
    fun setRunOnBoot(v: Boolean) { runOnBoot = v; Prefs.setBool(ctx, Prefs.RUN_ON_BOOT, v) }
    fun updateName(n: String) { userName = n; Prefs.setString(ctx, Prefs.NAME, n) }
    fun finishOnboarding(name: String) {
        if (name.isNotBlank()) updateName(name.trim())
        onboarded = true
        Prefs.setBool(ctx, Prefs.ONBOARDED, true)
    }
    fun replayOnboarding() { onboarded = false; Prefs.setBool(ctx, Prefs.ONBOARDED, false) }

    // ---- rules ----
    fun saveRule(r: Rule) {
        val list = rules.toMutableList()
        val i = list.indexOfFirst { it.id == r.id }
        if (i >= 0) list[i] = r else list.add(0, r)
        commit(list)
        toast(if (i >= 0) "Rule updated" else "Rule created", ToastKind.SUCCESS)
    }

    fun toggleRule(id: String, on: Boolean) =
        commit(rules.map { if (it.id == id) it.copy(enabled = on) else it })

    fun deleteRule(id: String) {
        AlarmScheduler.cancel(ctx, id)
        commit(rules.filter { it.id != id })
        toast("Rule deleted", ToastKind.INFO)
    }

    fun deleteAllRules() {
        rules.forEach { AlarmScheduler.cancel(ctx, it.id) }
        commit(emptyList())
        toast("All rules deleted", ToastKind.INFO)
    }

    fun clearLogs() { LogStore.clear(ctx); toast("Activity log cleared", ToastKind.INFO) }

    fun runNow(r: Rule) {
        viewModelScope.launch(Dispatchers.Default) { RuleEngine.fire(ctx, r, "Manual test") }
        toast("Ran “${r.name.ifBlank { "rule" }}” — see Logs", ToastKind.INFO)
    }

    private fun commit(list: List<Rule>) {
        rules = list
        RuleStore.save(ctx, list)
        AlarmScheduler.rescheduleAll(ctx)
        pingService()
    }

    // ---- engine service ----
    fun refreshFlags() { serviceOn = Prefs.getBool(ctx, Prefs.SERVICE_ENABLED, false) }

    fun toggleService() = setService(!serviceOn)

    fun setService(on: Boolean) {
        try {
            val i = Intent(ctx, AutomationService::class.java)
            if (on) {
                i.putExtra("from_ui", true)
                ContextCompat.startForegroundService(ctx, i)
            } else {
                ctx.stopService(i)
            }
            serviceOn = on
            Prefs.setBool(ctx, Prefs.SERVICE_ENABLED, on)
            toast(if (on) "Automation engine started ⚡" else "Engine stopped", if (on) ToastKind.SUCCESS else ToastKind.INFO)
        } catch (e: Exception) {
            toast("Couldn't change the engine: ${e.message}", ToastKind.ERROR)
        }
    }

    private fun pingService() {
        if (!serviceOn) return
        runCatching {
            ContextCompat.startForegroundService(
                ctx,
                Intent(ctx, AutomationService::class.java)
                    .setAction(AutomationService.ACTION_REFRESH)
                    .putExtra("from_ui", true)
            )
        }
    }
}
