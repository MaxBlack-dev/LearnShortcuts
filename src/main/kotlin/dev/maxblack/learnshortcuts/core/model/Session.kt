package dev.maxblack.learnshortcuts.core.model

import java.time.Instant
import java.util.UUID

/**
 * Represents a single practice session.
 *
 * A new session starts when:
 *  - The user installs the plugin for the first time, or
 *  - The user exhausts all shortcuts in a previous session (sessions completed++).
 *
 * Session state is kept in memory; [StatisticsState] persists the aggregate
 * counters across IDE restarts.
 *
 * ── Reveal logic ──────────────────────────────────────────────────────────────
 * When the user presses "Reveal" the [isCurrentRevealed] flag is set to true.
 * If they then press the correct shortcut:
 *   - the shortcut is appended to the END of the queue (try again later)
 *   - correct / remaining counters are NOT changed
 *   - [revealedThisSession] is incremented
 * This mirrors the "flashcard peek" pattern: peeking means you don't get credit.
 */
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val startedAt: Instant = Instant.now(),

    /** Total shortcut count when this session was initialised. */
    val totalShortcuts: Int = 0,

    /** How many shortcuts were answered correctly (without peeking). */
    var correctCount: Int = 0,

    /** How many times the user pressed Reveal in this session. */
    var revealedCount: Int = 0,

    /** Remaining items in the queue (decreases only on correct, non-revealed answers). */
    var remainingCount: Int = 0,

    /** The ordered queue of shortcuts still to be practised this session. */
    val queue: ArrayDeque<Shortcut> = ArrayDeque(),

    /** The shortcut currently shown to the user. */
    var currentShortcut: Shortcut? = null,

    /** True if the user pressed "Reveal" for the current shortcut. */
    var isCurrentRevealed: Boolean = false,

    /** True while the session is in progress. */
    var isActive: Boolean = true,
) {
    /** Convenience: how many shortcuts have been handled (correct + revealed). */
    val handledCount: Int get() = totalShortcuts - remainingCount
}
