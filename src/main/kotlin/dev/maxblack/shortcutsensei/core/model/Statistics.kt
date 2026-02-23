package dev.maxblack.shortcutsensei.core.model

/**
 * Snapshot of aggregated statistics for display in the Statistics panel.
 * Built by [StatisticsState] and handed to the UI.
 */
data class Statistics(
    /** Total shortcuts in the current catalogue (may vary per OS / keymap). */
    val totalShortcutsInCatalog: Int,

    /** How many full passes through the entire catalogue the user has completed. */
    val sessionsCompleted: Int,

    /** All-time correct presses across all sessions. */
    val allTimeCorrect: Int,

    /** All-time Reveal presses across all sessions. */
    val allTimeRevealed: Int,

    /** Correct presses in the current (ongoing) session. */
    val currentSessionCorrect: Int,

    /** Shortcuts still left in the current session queue. */
    val currentSessionRemaining: Int,

    /**
     * The 10 shortcuts the user has revealed / failed the most,
     * sorted descending by reveal count. Useful for focused practice.
     */
    val hardestShortcuts: List<Pair<String, Int>> = emptyList(),
)
