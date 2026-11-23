package com.robnugen.tswa.temperature

// The rules for which scales the tap visits (set on the Settings page).
object ScaleChoice {
    // Switch [scale] on or off. Switching off the last scale that is on
    // is refused: at least one scale always stays on.
    fun toggle(
        enabled: Set<Scale>,
        scale: Scale,
    ): Set<Scale> =
        when {
            scale !in enabled -> enabled + scale
            enabled.size > 1 -> enabled - scale
            else -> enabled
        }

    // The scale to show: [current] while it is on, otherwise the first
    // scale that is on, in C, F, K, R order.
    fun shown(
        current: Scale,
        enabled: Set<Scale>,
    ): Scale =
        if (current in enabled) {
            current
        } else {
            Scale.entries.firstOrNull { it in enabled } ?: current
        }

    // Whether the switch for [scale] can be flipped at all: the last one
    // on cannot be switched off.
    fun canToggle(
        enabled: Set<Scale>,
        scale: Scale,
    ): Boolean = scale !in enabled || enabled.size > 1
}
