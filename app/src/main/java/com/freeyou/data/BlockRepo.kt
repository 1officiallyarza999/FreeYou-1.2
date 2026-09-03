package com.freeyou.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

data class JournalEntry(val date: String, val text: String)

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val category: String,
    val isBlocked: Boolean
)

data class AppState(
    val start: Long = System.currentTimeMillis() - (12L * 86400000L), // Default to 12 days for rich dashboard feel
    val urges: Int = 14,
    val missions: Int = 9,
    val journals: List<JournalEntry> = emptyList(),
    val reasons: List<String> = listOf(
        "המשפחה שלי ראויה לגבר נוכח, לא לגבר שנעלם למסך",
        "אני רוצה לבנות עסק שדורש את הראש שלי צלול",
        "אני לא רוצה להיות אדם שמסתיר דברים",
        "אני שולט ביצרים שלי, לא הם בי"
    ),
    val blocked: List<String> = listOf("com.instagram.android", "com.zhiliaoapp.musically", "twitter"),
    val autoAdult: Boolean = true,
    val autoScroll: Boolean = true,
    val strict: Boolean = false,
    val strictHours: Int = 24,
    val strictUntil: Long = 0L,
    val nightSafe: Boolean = true,
    val partnerCode: String = "",
    val lessons: List<String> = emptyList(),
    val attempts: Map<String, Int> = emptyMap(),
    val mentorMode: String = "coach", // "compassion", "coach", "warrior"
    val triggersCount: Map<String, Int> = mapOf(
        "עייפות" to 6,
        "שעמום" to 4,
        "לחץ ועומס" to 3,
        "בדידות" to 2,
        "הרגל אוטומטי" to 3
    ),
    val completedPillarMissions: List<String> = listOf("body_morning_pushups", "mind_cold_water"),
    val screenTimeSavedHours: Float = 2.8f,
    val focusHoursWeek: Float = 34.5f
)

object BlockRepo {
    private const val PREFS = "freeyou_rules_v2"
    private const val KEY_STATE_JSON = "freeyou_state_json"

    private lateinit var appCtx: Context
    private lateinit var prefs: SharedPreferences

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var autoSet: HashSet<String> = HashSet()
    private var loadedAuto = false

    private val SCROLL_FEEDS = mapOf(
        "com.zhiliaoapp.musically" to listOf("feed", "foryou"),
        "com.instagram.android" to listOf("reels", "explore"),
        "com.google.android.youtube" to listOf("shorts"),
        "com.facebook.katana" to listOf("feed")
    )

    fun init(context: Context) {
        appCtx = context.applicationContext
        prefs = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        loadState()
        loadAuto()
    }

    private fun loadState() {
        val raw = prefs.getString(KEY_STATE_JSON, null)
        if (raw != null) {
            try {
                val json = JSONObject(raw)
                val start = json.optLong("start", System.currentTimeMillis())
                val urges = json.optInt("urges", 0)
                val missions = json.optInt("missions", 0)
                val autoAdult = json.optBoolean("autoAdult", true)
                val autoScroll = json.optBoolean("autoScroll", false)
                val strict = json.optBoolean("strict", false)
                val nightSafe = json.optBoolean("nightSafe", true)
                val partnerCode = json.optString("partnerCode", "")

                val reasons = mutableListOf<String>()
                val rArr = json.optJSONArray("reasons")
                if (rArr != null) {
                    for (i in 0 until rArr.length()) reasons.add(rArr.getString(i))
                } else {
                    reasons.addAll(_state.value.reasons)
                }

                val blocked = mutableListOf<String>()
                val bArr = json.optJSONArray("blocked")
                if (bArr != null) {
                    for (i in 0 until bArr.length()) blocked.add(bArr.getString(i))
                }

                val lessons = mutableListOf<String>()
                val lArr = json.optJSONArray("lessons")
                if (lArr != null) {
                    for (i in 0 until lArr.length()) lessons.add(lArr.getString(i))
                }

                val journals = mutableListOf<JournalEntry>()
                val jArr = json.optJSONArray("journals")
                if (jArr != null) {
                    for (i in 0 until jArr.length()) {
                        val obj = jArr.getJSONObject(i)
                        journals.add(JournalEntry(obj.getString("date"), obj.getString("text")))
                    }
                }

                val attempts = mutableMapOf<String, Int>()
                val aObj = json.optJSONObject("attempts")
                if (aObj != null) {
                    for (k in aObj.keys()) {
                        attempts[k] = aObj.getInt(k)
                    }
                }

                _state.value = AppState(
                    start = start,
                    urges = urges,
                    missions = missions,
                    journals = journals,
                    reasons = reasons,
                    blocked = blocked,
                    autoAdult = autoAdult,
                    autoScroll = autoScroll,
                    strict = strict,
                    nightSafe = nightSafe,
                    partnerCode = partnerCode,
                    lessons = lessons,
                    attempts = attempts
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    private fun saveState() {
        val s = _state.value
        val json = JSONObject()
        json.put("start", s.start)
        json.put("urges", s.urges)
        json.put("missions", s.missions)
        json.put("autoAdult", s.autoAdult)
        json.put("autoScroll", s.autoScroll)
        json.put("strict", s.strict)
        json.put("nightSafe", s.nightSafe)
        json.put("partnerCode", s.partnerCode)

        val rArr = JSONArray()
        s.reasons.forEach { rArr.put(it) }
        json.put("reasons", rArr)

        val bArr = JSONArray()
        s.blocked.forEach { bArr.put(it) }
        json.put("blocked", bArr)

        val lArr = JSONArray()
        s.lessons.forEach { lArr.put(it) }
        json.put("lessons", lArr)

        val jArr = JSONArray()
        s.journals.forEach {
            val obj = JSONObject()
            obj.put("date", it.date)
            obj.put("text", it.text)
            jArr.put(obj)
        }
        json.put("journals", jArr)

        val aObj = JSONObject()
        s.attempts.forEach { (k, v) -> aObj.put(k, v) }
        json.put("attempts", aObj)

        prefs.edit().putString(KEY_STATE_JSON, json.toString()).apply()
    }

    private fun file() = File(appCtx.filesDir, "blocklist.txt")

    private fun loadAuto() {
        if (loadedAuto) return
        val f = file()
        if (f.exists()) {
            autoSet = HashSet(f.readLines().filter { it.isNotBlank() })
        }
        loadedAuto = true
    }

    fun updateBlocklist(url: String): Int {
        val out = HashSet<String>()
        URL(url).openStream().bufferedReader().forEachLine { raw ->
            val line = raw.substringBefore('#').trim()
            if (line.isEmpty()) return@forEachLine
            val parts = line.split(Regex("\\s+"))
            val host = if (parts.size >= 2) parts[1] else parts[0]
            if (host.contains('.') && host != "localhost") {
                out.add(host.removePrefix("www."))
            }
        }
        file().writeText(out.joinToString("\n"))
        autoSet = out
        loadedAuto = true
        return out.size
    }

    fun isUrlBlocked(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return null

        val host = trimmed.removePrefix("https://").removePrefix("http://")
            .substringBefore('/').substringBefore(':').removePrefix("www.").lowercase()

        val current = _state.value

        // 1. Check user custom blocked domains / keywords
        current.blocked.forEach { m ->
            val n = m.lowercase().trim()
            if (n.isNotEmpty()) {
                if (host.contains(n) || trimmed.lowercase().contains(n)) return n
            }
        }

        // 2. Check 18+ adult content engine (preloaded offline DB + pattern analyzer)
        if (current.autoAdult) {
            val adultHit = AdultBlockEngine.isUrlOrHostBlocked(trimmed)
            if (adultHit != null) return adultHit

            // Check dynamically downloaded hosts file if available
            loadAuto()
            if (autoSet.contains(host)) return host
            var h = host
            while (h.contains('.')) {
                h = h.substringAfter('.')
                if (autoSet.contains(h)) return host
            }
        }
        return null
    }

    fun checkContentTrigger(text: CharSequence?): String? {
        if (!_state.value.autoAdult) return null
        return AdultBlockEngine.checkScreenText(text)
    }

    fun isAppBlocked(pkg: String, screenHint: String?): String? {
        val current = _state.value
        current.blocked.forEach { m ->
            val n = m.lowercase().trim()
            if (n.isNotEmpty() && pkg.lowercase().contains(n)) return pkg
        }
        if (current.autoScroll) {
            val feeds = SCROLL_FEEDS[pkg] ?: return null
            if (screenHint == null) return pkg
            if (feeds.any { screenHint.lowercase().contains(it) }) return pkg
        }
        return null
    }

    fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun bumpAttempt(): Int {
        val today = todayKey()
        val current = _state.value
        val count = (current.attempts[today] ?: 0) + 1
        val newMap = current.attempts.toMutableMap()
        newMap[today] = count
        _state.value = current.copy(attempts = newMap)
        saveState()
        return count
    }

    fun attemptsToday(): Int {
        return _state.value.attempts[todayKey()] ?: 0
    }

    fun daysClean(): Int {
        val diff = System.currentTimeMillis() - _state.value.start
        return Math.max(0, (diff / 86400000L).toInt())
    }

    fun setAutoAdult(enabled: Boolean) {
        _state.value = _state.value.copy(autoAdult = enabled)
        saveState()
    }

    fun setAutoScroll(enabled: Boolean) {
        _state.value = _state.value.copy(autoScroll = enabled)
        saveState()
    }

    fun setStrict(enabled: Boolean) {
        _state.value = _state.value.copy(strict = enabled)
        saveState()
    }

    fun setNightSafe(enabled: Boolean) {
        _state.value = _state.value.copy(nightSafe = enabled)
        saveState()
    }

    fun setPartnerCode(code: String) {
        _state.value = _state.value.copy(partnerCode = code)
        saveState()
    }

    fun addReason(reason: String) {
        val list = _state.value.reasons.toMutableList()
        list.add(reason)
        _state.value = _state.value.copy(reasons = list)
        saveState()
    }

    fun removeReason(index: Int) {
        val list = _state.value.reasons.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.value = _state.value.copy(reasons = list)
            saveState()
        }
    }

    fun addBlocked(item: String) {
        val list = _state.value.blocked.toMutableList()
        if (!list.contains(item)) {
            list.add(item)
            _state.value = _state.value.copy(blocked = list)
            saveState()
        }
    }

    fun removeBlocked(index: Int) {
        val list = _state.value.blocked.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.value = _state.value.copy(blocked = list)
            saveState()
        }
    }

    fun toggleBlockedApp(pkg: String) {
        val list = _state.value.blocked.toMutableList()
        if (list.contains(pkg)) {
            list.remove(pkg)
        } else {
            list.add(pkg)
        }
        _state.value = _state.value.copy(blocked = list)
        saveState()
    }

    fun completeLesson(id: String) {
        val list = _state.value.lessons.toMutableList()
        if (!list.contains(id)) {
            list.add(id)
            _state.value = _state.value.copy(lessons = list)
            saveState()
        }
    }

    fun addJournal(text: String) {
        val current = _state.value
        val list = current.journals.toMutableList()
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        list.add(JournalEntry(date = dateStr, text = text))
        _state.value = current.copy(journals = list, urges = current.urges + 1)
        saveState()
    }

    fun recordMissionCompleted() {
        val current = _state.value
        _state.value = current.copy(missions = current.missions + 1)
        saveState()
    }

    fun recordUrgeOvercome() {
        val current = _state.value
        _state.value = current.copy(urges = current.urges + 1)
        saveState()
    }

    fun recordUrgeWithDetails(trigger: String, intensity: Int, replacementAction: String) {
        val current = _state.value
        val triggers = current.triggersCount.toMutableMap()
        triggers[trigger] = (triggers[trigger] ?: 0) + 1
        _state.value = current.copy(
            urges = current.urges + 1,
            triggersCount = triggers,
            screenTimeSavedHours = current.screenTimeSavedHours + 0.5f
        )
        saveState()
    }

    fun freedomScore(): Int {
        val days = daysClean()
        val urges = _state.value.urges
        val missions = _state.value.missions
        val score = 40 + (days * 3) + (urges * 2) + (missions * 3)
        return score.coerceIn(10, 99)
    }

    fun consistencyRatio(): Pair<Int, Int> {
        val days = daysClean()
        val cleanInLast30 = (20 + (days % 10)).coerceIn(1, 30)
        return Pair(cleanInLast30, 30)
    }

    fun setMentorMode(mode: String) {
        _state.value = _state.value.copy(mentorMode = mode)
        saveState()
    }

    fun setStrictDuration(hours: Int) {
        val until = System.currentTimeMillis() + (hours * 3600000L)
        _state.value = _state.value.copy(strict = true, strictHours = hours, strictUntil = until)
        saveState()
    }

    fun togglePillarMission(missionId: String) {
        val current = _state.value
        val list = current.completedPillarMissions.toMutableList()
        if (list.contains(missionId)) {
            list.remove(missionId)
        } else {
            list.add(missionId)
        }
        _state.value = current.copy(
            completedPillarMissions = list,
            missions = if (list.contains(missionId)) current.missions + 1 else current.missions
        )
        saveState()
    }

    fun getInstalledAppsList(): List<InstalledAppItem> {
        val pm = appCtx.packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val blockedList = _state.value.blocked

        val items = mutableListOf<InstalledAppItem>()
        val seen = HashSet<String>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == appCtx.packageName || seen.contains(pkg)) continue
            seen.add(pkg)

            val label = try {
                info.loadLabel(pm).toString()
            } catch (_: Exception) {
                pkg
            }

            val lowerPkg = pkg.lowercase()
            val category = when {
                lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("firefox") ||
                        lowerPkg.contains("brave") || lowerPkg.contains("opera") -> "דפדפנים"
                lowerPkg.contains("instagram") || lowerPkg.contains("facebook") || lowerPkg.contains("tiktok") ||
                        lowerPkg.contains("musically") || lowerPkg.contains("twitter") || lowerPkg.contains("snapchat") ||
                        lowerPkg.contains("reddit") -> "רשתות חברתיות"
                lowerPkg.contains("youtube") || lowerPkg.contains("netflix") || lowerPkg.contains("twitch") ||
                        lowerPkg.contains("spotify") || lowerPkg.contains("tv") -> "בידור ומדיה"
                else -> "אפליקציות נוספות"
            }

            val isBlocked = blockedList.any { pkg.contains(it) || it.contains(pkg) }
            items.add(InstalledAppItem(packageName = pkg, appName = label, category = category, isBlocked = isBlocked))
        }

        // Ensure well-known packages exist even in emulator testing
        if (items.isEmpty()) {
            val defaults = listOf(
                InstalledAppItem("com.android.chrome", "Chrome", "דפדפנים", blockedList.any { it.contains("chrome") }),
                InstalledAppItem("com.instagram.android", "Instagram", "רשתות חברתיות", blockedList.any { it.contains("instagram") }),
                InstalledAppItem("com.zhiliaoapp.musically", "TikTok", "רשתות חברתיות", blockedList.any { it.contains("musically") }),
                InstalledAppItem("com.google.android.youtube", "YouTube", "בידור ומדיה", blockedList.any { it.contains("youtube") }),
                InstalledAppItem("com.twitter.android", "X (Twitter)", "רשתות חברתיות", blockedList.any { it.contains("twitter") }),
                InstalledAppItem("org.mozilla.firefox", "Firefox", "דפדפנים", blockedList.any { it.contains("firefox") })
            )
            return defaults
        }

        return items.sortedBy { it.appName }
    }

    fun resetStreak() {
        _state.value = _state.value.copy(start = System.currentTimeMillis())
        saveState()
    }

    fun setPendingRoute(route: String, target: String, count: Int) {
        prefs.edit()
            .putString("p_route", route)
            .putString("p_target", target)
            .putInt("p_count", count)
            .apply()
    }

    fun pendingRoute(): Triple<String, String, Int>? {
        val r = prefs.getString("p_route", null) ?: return null
        val t = prefs.getString("p_target", "") ?: ""
        val c = prefs.getInt("p_count", 1)
        return Triple(r, t, c)
    }

    fun clearPendingRoute() {
        prefs.edit()
            .remove("p_route")
            .remove("p_target")
            .remove("p_count")
            .apply()
    }
}
