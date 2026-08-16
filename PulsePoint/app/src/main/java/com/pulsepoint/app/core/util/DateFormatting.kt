package com.pulsepoint.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatting {

    private val monthDay: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    private val fullDate: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

    fun formatEpochDay(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(monthDay)

    fun formatFullDate(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(fullDate)

    fun formatEpochMillis(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(fullDate)
}
