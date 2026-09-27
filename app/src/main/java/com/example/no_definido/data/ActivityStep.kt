package com.example.no_definido.data

enum class ActivityType {
    CATEGORIZATION,
    OBJECT_USAGE,
    SAFETY_CHECK
}

data class ActivityStep(
    val id: String,
    val title: String,
    val instruction: String,
    val activityType: ActivityType,
    val item: HouseholdItem,
    val availableTargetAreaIds: List<String>,
    val correctAreaId: String
)
