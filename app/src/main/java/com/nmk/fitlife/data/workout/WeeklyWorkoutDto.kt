package com.nmk.fitlife.data.workout

data class WeeklyWorkoutDto(
    val workoutId: Int,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dayOfWeek: String,
    val startDate: String,
    val endDate: String
)
