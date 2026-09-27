package com.example.no_definido.data

data class HouseholdItem(
    val id: String,
    val name: String,
    val targetAreaId: String,
    val usageDescription: String,
    val isSafe: Boolean = true,
    val iconName: String = "placeholder"
)
