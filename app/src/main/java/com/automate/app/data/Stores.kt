package com.automate.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

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
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, rulesToArray(rules).toString()).apply()
    }

    @JvmStatic
    fun find(ctx: Context, id: String): Rule? = load(ctx).firstOrNull { it.id == id }

    @JvmStatic @Synchronized
    fun markRun(ctx: Context, id: String) {
        val now = System.currentTimeMillis()
        save(ctx, load(ctx).map { if (it.id == id) it.copy(lastRun = now, runCount = it.runCount + 1) else it })
    }

    // ---- backup -------------------------------------------------------------

    @JvmStatic
    fun exportJson(rules: List<Rule>): String = JSONObject()
        .put("app", "AutoMate")
        .put("format", 2)
        .put("exportedAt", System.currentTimeMillis())
        .put("rules", rulesToArray(rules))
        .toString(2)

    /** Returns null when the text is not a valid AutoMate backup. Imported rules get fresh ids and zeroed stats. */
    @JvmStatic
    fun parseBackup(text: String): List<Rule>? {
        if (text.length > 2_000_000) return null
        var result: List<Rule>? = null
        try {
            val root = JSONObject(text)
            if (root.optString("app") == "AutoMate") {
                val arr = root.getJSONArray("rules")
                if (arr.length() <= 500) {
                    result = (0 until arr.length()).mapNotNull { i ->
                        runCatching { fromJson(arr.getJSONObject(i)) }.getOrNull()
                    }.map { it.copy(id = UUID.randomUUID().toString(), lastRun = 0L, runCount = 0) }
                }
            }
        } catch (e: Exception) {
            result = null
        }
        return result
    }

    // ---- json ---------------------------------------------------------------

    private fun rulesToArray(rules: List<Rule>): JSONArray {
        val arr = JSONArray()
        rules.forEach { arr.put(toJson(it)) }
        return arr
    }

    private fun toJson(r: Rule): JSONObject = JSONObject().apply {
        put("id", r.id); put("name", r.name); put("enabled", r.enabled); put("trigger", r.trigger.name)
        put("hour", r.hour); put("minute", r.minute); put("days", r.days); put("threshold", r.threshold)
        put("filter", r.filter); put("lat", r.lat); put("lng", r.lng); put("radius", r.radius)
        put("interval", r.intervalMin); put("matchAll", r.matchAll); put("folder", r.folder)
        put("lastRun", r.lastRun); put("runCount", r.runCount)
        val cs = JSONArray()
        r.conditions.forEach { c -> cs.put(JSONObject().put("t", c.type.name).put("a", c.a).put("b", c.b).put("s", c.text)) }
        put("conditions", cs)
        val acts = JSONArray()
        r.actions.forEach { a ->
            acts.put(
                JSONObject().put("type", a.type.name).put("message", a.message).put("pkg", a.packageName)
                    .put("url", a.url).put("social", a.social).put("ringer", a.ringer).put("level", a.level)
                    .put("stream", a.stream).put("sys", a.sysAction).put("delay", a.delaySec)
            )
        }
        put("actions", acts)
    }

    private fun actionFrom(o: JSONObject, typeKey: String): ActionItem = ActionItem(
        type = runCatching { ActionType.valueOf(o.getString(typeKey)) }.getOrDefault(ActionType.NOTIFICATION),
        message = o.optString("message", ""),
        packageName = o.optString("pkg", ""),
        url = o.optString("url", ""),
        social = o.optString("social", "instagram"),
        ringer = o.optInt("ringer", 1),
        level = o.optInt("level", 50),
        stream = o.optInt("stream", 0),
        sysAction = o.optString("sys", "home"),
        delaySec = o.optInt("delay", 0)
    )

    private fun fromJson(o: JSONObject): Rule {
        val actions: List<ActionItem> = if (o.has("actions")) {
            val arr = o.getJSONArray("actions")
            (0 until arr.length()).mapNotNull { i -> runCatching { actionFrom(arr.getJSONObject(i), "type") }.getOrNull() }
        } else {
            listOf(actionFrom(o, "action")) // rules saved by version 1.0 to 1.2
        }
        val conditions: List<Condition> = o.optJSONArray("conditions")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                runCatching {
                    val c = arr.getJSONObject(i)
                    Condition(ConditionType.valueOf(c.getString("t")), c.optInt("a", 0), c.optInt("b", 0), c.optString("s", ""))
                }.getOrNull()
            }
        } ?: emptyList()
        return sanitize(
            Rule(
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
                intervalMin = o.optInt("interval", 30),
                conditions = conditions,
                matchAll = o.optBoolean("matchAll", true),
                actions = actions,
                folder = o.optString("folder", ""),
                lastRun = o.optLong("lastRun", 0L),
                runCount = o.optInt("runCount", 0)
            )
        )
    }

    /** Clamps every value to a safe range. Used for stored and imported rules. */
    @JvmStatic
    fun sanitize(r: Rule): Rule = r.copy(
        name = r.name.take(120),
        folder = r.folder.trim().take(40),
        hour = r.hour.coerceIn(0, 23),
        minute = r.minute.coerceIn(0, 59),
        days = r.days and 127,
        threshold = r.threshold.coerceIn(1, 100),
        filter = r.filter.take(120),
        lat = if (r.lat.isNaN()) 0.0 else r.lat.coerceIn(-90.0, 90.0),
        lng = if (r.lng.isNaN()) 0.0 else r.lng.coerceIn(-180.0, 180.0),
        radius = r.radius.coerceIn(20, 5000),
        intervalMin = r.intervalMin.coerceIn(1, 1440),
        conditions = r.conditions.take(20).map { it.copy(a = it.a.coerceIn(0, 100000), b = it.b.coerceIn(0, 100000), text = it.text.take(120)) },
        actions = r.actions.take(20).map {
            it.copy(
                message = it.message.take(2000), packageName = it.packageName.take(200), url = it.url.take(500),
                social = it.social.take(30), ringer = it.ringer.coerceIn(0, 2), level = it.level.coerceIn(0, 100),
                stream = it.stream.coerceIn(0, 3), sysAction = it.sysAction.take(30), delaySec = it.delaySec.coerceIn(0, 60)
            )
        }.ifEmpty { listOf(ActionItem()) },
        runCount = r.runCount.coerceAtLeast(0)
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

data class DayStat(val key: String, val label: String, val ok: Int, val fail: Int)

/** Daily run counters (kept for 120 days). They power the Stats page. */
object StatsStore {
    private const val PREF = "automate_stats"
    private const val KEY = "days"
    private const val KEEP = 120

    private fun keyOf(cal: Calendar): String =
        String.format(Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))

    private fun read(ctx: Context): JSONObject = try {
        JSONObject(ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "{}") ?: "{}")
    } catch (e: Exception) {
        JSONObject()
    }

    @JvmStatic @Synchronized
    fun record(ctx: Context, ok: Boolean) {
        val o = read(ctx)
        val k = keyOf(Calendar.getInstance())
        val cur = o.optJSONArray(k) ?: JSONArray().put(0).put(0)
        val idx = if (ok) 0 else 1
        cur.put(idx, cur.optInt(idx) + 1)
        o.put(k, cur)
        val keys = o.keys().asSequence().toList().sorted()
        if (keys.size > KEEP) keys.take(keys.size - KEEP).forEach { o.remove(it) }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, o.toString()).apply()
    }

    @Synchronized
    fun lastDays(ctx: Context, n: Int): List<DayStat> {
        val o = read(ctx)
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        return (n - 1 downTo 0).map { back ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -back)
            val a = o.optJSONArray(keyOf(cal))
            DayStat(keyOf(cal), fmt.format(cal.time), a?.optInt(0) ?: 0, a?.optInt(1) ?: 0)
        }
    }

    /** Consecutive days with at least one successful run. Today only counts once it has a run, but never breaks the streak. */
    @Synchronized
    fun streak(ctx: Context): Int {
        val o = read(ctx)
        val cal = Calendar.getInstance()
        if ((o.optJSONArray(keyOf(cal))?.optInt(0) ?: 0) == 0) cal.add(Calendar.DAY_OF_YEAR, -1)
        var n = 0
        while ((o.optJSONArray(keyOf(cal))?.optInt(0) ?: 0) > 0) {
            n++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return n
    }

    /** Returns (successful runs, failed runs) over the stored period. */
    @Synchronized
    fun totals(ctx: Context): Pair<Int, Int> {
        val o = read(ctx)
        var ok = 0
        var fail = 0
        o.keys().forEach { k ->
            val a = o.optJSONArray(k)
            ok += a?.optInt(0) ?: 0
            fail += a?.optInt(1) ?: 0
        }
        return ok to fail
    }
}
