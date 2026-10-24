package com.example.week2

import androidx.compose.runtime.Immutable

@Immutable
data class HardwareItem(
    val id: String,
    val name: String,
    val category: String,
    val cost: Double,
    val stockCount: Int
)
