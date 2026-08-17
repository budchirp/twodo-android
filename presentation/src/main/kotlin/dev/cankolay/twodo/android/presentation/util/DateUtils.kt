package dev.cankolay.twodo.android.presentation.util

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    const val SUMMARY_DATE_PATTERN = "d MMM yyyy"
    const val DATE_TIME_PATTERN = "dd EEE yyyy HH:mm"
    const val TIME_PATTERN = "HH:mm"

    fun parse(value: String): OffsetDateTime? =
        runCatching { OffsetDateTime.parse(value) }.getOrNull()

    fun format(date: LocalDate, pattern: String): String =
        date.format(DateTimeFormatter.ofPattern(pattern))

    fun format(value: String, pattern: String): String =
        parse(value)?.format(DateTimeFormatter.ofPattern(pattern)).orEmpty()

    fun daysSince(value: String): Long =
        parse(value)?.let { ChronoUnit.DAYS.between(it, OffsetDateTime.now()) } ?: 0L
}
