package com.nmk.fitlife.service

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

val dateFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH)

fun getTodayDate(): LocalDate {
    return LocalDate.now(ZoneId.systemDefault())
}

fun getStartOfWeekDate(): String {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val startOfWeek = getTodayDate().with(TemporalAdjusters.previousOrSame(firstDayOfWeek + 1))
    return startOfWeek.format(dateFormatter)
}

fun getEndOfWeekDate(): String {
    val endOfWeek = getTodayDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return endOfWeek.format(dateFormatter)
}

