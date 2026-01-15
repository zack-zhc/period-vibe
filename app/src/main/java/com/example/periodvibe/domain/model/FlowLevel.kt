package com.example.periodvibe.domain.model

enum class FlowLevel(val displayName: String, val color: String, val value: Int) {
    LIGHT("少", "#FFCDD2", 1),
    MEDIUM("中", "#EF9A9A", 2),
    HEAVY("多", "#EF5350", 3);

    companion object {
        fun fromValue(value: Int): FlowLevel? {
            return values().find { it.value == value }
        }
    }
}
