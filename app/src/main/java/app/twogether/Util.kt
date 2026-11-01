package app.twogether

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object U {
    fun prefs(c: Context) = c.getSharedPreferences("tw", 0)
    private fun parse(s: String?) = runCatching { LocalDate.parse(s) }.getOrNull()
    fun days(start: String?): Long? = parse(start)?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) }
    /** next monthly anniversary: (date, month number, days left) */
    fun next(start: String?): Triple<LocalDate, Int, Long>? {
        val s = parse(start) ?: return null
        val t = LocalDate.now(); var n = 1
        while (s.plusMonths(n.toLong()) < t) n++
        val d = s.plusMonths(n.toLong())
        return Triple(d, n, ChronoUnit.DAYS.between(t, d))
    }
    /** 0..1 progress through the current month-cycle */
    fun progress(start: String?): Float {
        val s = parse(start) ?: return 0f; val (d, n, left) = next(start) ?: return 0f
        val len = ChronoUnit.DAYS.between(s.plusMonths((n - 1).toLong()), d).coerceAtLeast(1)
        return (1f - left.toFloat() / len).coerceIn(0f, 1f)
    }
    fun milestone(days: Long): Pair<Long, Long>? =
        listOf(100L, 200L, 300L, 365L, 500L, 730L, 1000L, 1500L, 2000L, 3650L).firstOrNull { it > days }?.let { it to (it - days) }
    fun dist(m: Float) = if (m < 1000) "${m.toInt()} m" else "%.1f km".format(m / 1000)
    fun eta(m: Float): String { val min = (m / 1000f / 50f * 60).toInt(); return if (min < 60) "$min min" else "${min / 60} h ${min % 60} min" }
    fun ago(ts: Long): String {
        if (ts <= 0) return "not sharing yet"
        val s = (System.currentTimeMillis() - ts) / 1000
        return when { s < 60 -> "just now"; s < 3600 -> "${s / 60} min ago"; s < 86400 -> "${s / 3600} h ago"; else -> "${s / 86400} d ago" }
    }
}
