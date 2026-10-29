package dev.cankolay.twodo.android.presentation.util

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    const val SUMMARY_DATE_PATTERN = "d MMM yyyy"
    const val DATE_TIME_PATTERN = "dd MMM yyyy HH:mm"
    const val TIME_PATTERN = "HH:mm"

    private fun parse(value: String): OffsetDateTime? = runCatching {
        OffsetDateTime.parse(value)
    }
        .getOrNull()

    fun format(date: LocalDate, pattern: String): String =
        date.format(DateTimeFormatter.ofPattern(pattern))

    fun formatRange(start: LocalDate, end: LocalDate? = null): String {
        val first = format(date = start, pattern = SUMMARY_DATE_PATTERN)

        return when (end) {
            null -> first

            else -> "$first – ${format(date = end, pattern = SUMMARY_DATE_PATTERN)}"
        }
    }

    fun format(value: String, pattern: String): String =
        parse(value = value)
            ?.atZoneSameInstant(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern(pattern))
            .orEmpty()

    fun daysSince(value: String): Long =
        parse(value = value)?.let { ChronoUnit.DAYS.between(it, OffsetDateTime.now()) } ?: 0L
}
