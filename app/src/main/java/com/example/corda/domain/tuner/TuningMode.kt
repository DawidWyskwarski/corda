package com.example.corda.domain.tuner

enum class TuningMode {
    STANDARD,
    CHROMATIC,
    ;

    override fun toString(): String =
        name.lowercase().replaceFirstChar { it.uppercase() }
}
