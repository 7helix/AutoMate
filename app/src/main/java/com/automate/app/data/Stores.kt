package com.automate.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject

object RuleStore {
    private const val PREF = "automate_rules"
    private const val KEY = "rules"

    @JvmStatic @Synchronized
    fun load(ctx: Context): List<Rule> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i -> runCatching { fromJson(arr.getJSONObject(i)) }.getOrNull() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @JvmStatic @Synchronized
    fun save(ctx: Context, rules: List<Rule>) {
        val arr = JSONArray()
        rules.forEach { arr.put(toJson(it)) }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    @JvmStatic
    fun find(ctx: Context, id: String): Rule? = load(ctx).firstOrNull { it.id == id }

    @JvmStatic @Synchronized
    fun markRun(ctx: Context, id: String) {
        val now = System.currentTimeMillis()
        save(ctx, load(ctx).map { if (it.id == id) it.copy(lastRun = now, runCount = it.runCount + 1) else it })
    }

    private fun toJson(r: Rule): JSONObject = JSONObject().apply {
        put("id", r.id); put("name", r.name); put("enabled", r.enabled); put("trigger", r.trigger.name)
        put("hour", r.hour); put("minute", r.minute); put("days", r.days); put("threshold", r.threshold)
        put("filter", r.filter); put("lat", r.lat); put("lng", r.lng); put("radius", r.radius)
        put("action", r.action.name); put("message", r.message); put("pkg", r.packageName)
        put("url", r.url); put("ringer", r.ringer); put("lastRun", r.lastRun); put("runCount", r.runCount)
    }

    private fun fromJson(o: JSONObject): Rule = Rule(
        id = o.getString("id"),
        name = o.optString("name", ""),
        enabled = o.optBoolean("enabled", true),
        trigger = runCatching { TriggerType.valueOf(o.getString("trigger")) }.getOrDefault(TriggerType.TIME),
        hour = o.optInt("hour", 8),
        minute = o.optInt("minute", 0),
        days = o.optInt("days", 127),
        threshold = o.optInt("threshold", 20),
        filter = o.optString("filter", ""),
        lat = o.optDouble("lat", 0.0),
        lng = o.optDouble("lng", 0.0),
        radius = o.optInt("radius", 150),
        action = runCatching { ActionType.valueOf(o.getString("action")) }.getOrDefault(ActionType.NOTIFICATION),
        message = o.optString("message", ""),
        packageName = o.optString("pkg", ""),
        url = o.optString("url", ""),
        ringer = o.optInt("ringer", 1),
        lastRun = o.optLong("lastRun", 0L),
        runCount = o.optInt("runCount", 0)
    )
}

data class LogEntry(val time: Long, val rule: String, val message: String, val ok: Boolean)

object LogStore {
    private const val PREF = "automate_logs"
    private const val KEY = "logs"
    private const val MAX = 200

    val version = MutableStateFlow(0)

    @JvmStatic @Synchronized
    fun load(ctx: Context): List<LogEntry> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LogEntry(o.getLong("t"), o.getString("r"), o.getString("m"), o.getBoolean("ok"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @JvmStatic @Synchronized
    fun add(ctx: Context, rule: String, message: String, ok: Boolean) {
        val list = (listOf(LogEntry(System.currentTimeMillis(), rule, message, ok)) + load(ctx)).take(MAX)
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("t", it.time).put("r", it.rule).put("m", it.message).put("ok", it.ok))
        }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
        version.value = version.value + 1
    }

    @JvmStatic @Synchronized
    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, "[]").apply()
        version.value = version.value + 1
    }
}
