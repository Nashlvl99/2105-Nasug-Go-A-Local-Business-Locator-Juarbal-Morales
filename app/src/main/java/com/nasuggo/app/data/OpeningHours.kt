package com.nasuggo.app.data

import java.util.Calendar

class OpeningHours(days: List<DayHours>) {
    private val days: List<DayHours> = days.toList()

    val isListed: Boolean get() = days.isNotEmpty()

    fun forDay(code: String): DayHours? = days.firstOrNull { it.day == code }

    fun isOpenAt(now: Calendar = Calendar.getInstance()): Boolean {
        val today = forDay(dayCode(now)) ?: return false
        val open = today.open ?: return false
        val close = today.close ?: return false
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return minutes in toMinutes(open) until toMinutes(close)
    }

    fun todayText(now: Calendar = Calendar.getInstance()): String {
        val today = forDay(dayCode(now)) ?: return "Hours not listed"
        val o = today.open
        val c = today.close
        return if (o != null && c != null) "Today  $o – $c" else "Closed today"
    }

    fun weeklyText(): String {
        if (!isListed) return "Hours not listed"
        return ORDER.joinToString("\n") { code ->
            val d = forDay(code)
            val label = NAMES.getValue(code)
            if (d?.open != null && d.close != null) "$label  ${d.open} – ${d.close}"
            else "$label  Closed"
        }
    }

    companion object {
        private val ORDER = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        private val NAMES = mapOf(
            "MON" to "Mon", "TUE" to "Tue", "WED" to "Wed", "THU" to "Thu",
            "FRI" to "Fri", "SAT" to "Sat", "SUN" to "Sun"
        )

        fun dayCode(cal: Calendar): String = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> "SUN"
        }

        private fun toMinutes(t: String): Int {
            val (h, m) = t.split(":")
            return h.toInt() * 60 + m.toInt()
        }
    }
}
