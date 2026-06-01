package com.example.periodvibe.domain.model

import androidx.compose.runtime.Stable

@Stable
enum class FlowLevel(val displayName: String, val color: String, val value: Int) {
    LIGHT("经量少", "#FFCDD2", 1),
    MEDIUM("经量中", "#EF9A9A", 2),
    HEAVY("经量大", "#EF5350", 3);

    companion object {
        fun fromValue(value: Int): FlowLevel? {
            return values().find { it.value == value }
        }
    }
}
