package com.kreeda.ankana.ui.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

object DateUtil {

    fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    fun todayIso(): String = today().toString()

    fun parseIso(s: String): LocalDate = runCatching { LocalDate.parse(s) }.getOrElse { today() }

    /** Convert a LocalDate to UTC midnight epoch millis (what Material3 DatePicker expects). */
    fun toUtcMillis(d: LocalDate): Long =
        d.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    /** Reverse of [toUtcMillis] — picker callbacks return UTC midnight. */
    fun fromUtcMillis(millis: Long): LocalDate =
        kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.UTC).date

    fun nextDay(d: LocalDate): LocalDate = d.plus(1, DateTimeUnit.DAY)

    fun prevDay(d: LocalDate): LocalDate = d.minus(1, DateTimeUnit.DAY)

    fun shortLabel(d: LocalDate): String =
        "${d.dayOfWeek.short()}, ${d.dayOfMonth} ${d.month.short()}"

    fun friendlyLabel(d: LocalDate): String {
        val today = today()
        return when (d) {
            today -> "Today"
            today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
            today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
            else -> shortLabel(d)
        }
    }

    /** "6 AM – 7 AM" */
    fun slotRangeLabel(hour: Int): String =
        "${hour12(hour)} – ${hour12((hour + 1) % 24)}"

    fun hour12(hour: Int): String {
        val h = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val ampm = if (hour < 12) "AM" else "PM"
        return "$h $ampm"
    }

    private fun DayOfWeek.short(): String = when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> name.take(3)
    }

    private fun Month.short(): String = when (this) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
        else -> name.take(3)
    }
}

/** Inclusive range of slot start hours for the village ground. */
val GROUND_HOURS: IntRange = 6..19
