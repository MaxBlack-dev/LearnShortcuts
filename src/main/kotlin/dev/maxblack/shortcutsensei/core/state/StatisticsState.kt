package dev.maxblack.shortcutsensei.core.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.maxblack.shortcutsensei.core.model.Statistics

/**
 * Application-level persistent statistics for ShortcutSensei.
 * Stored in {@code ShortcutSenseiStatistics.xml} inside the IDE config directory.
 *
 * All counters survive IDE restarts and accumulate across multiple sessions.
 */
@State(
    name = "ShortcutSenseiStatistics",
    storages = [Storage("ShortcutSenseiStatistics.xml")],
)
class StatisticsState : PersistentStateComponent<StatisticsState> {

    /** Number of times the user completed the full shortcut catalogue. */
    var sessionsCompleted: Int = 0

    /** All-time correct shortcut presses (no peek). */
    var allTimeCorrect: Int = 0

    /** All-time Reveal button presses. */
    var allTimeRevealed: Int = 0

    /** Correct presses in the current in-progress session. */
    var currentSessionCorrect: Int = 0

    /** Reveal presses in the current in-progress session. */
    var currentSessionRevealed: Int = 0

    /**
     * Per-shortcut correct hit counts.
     * Key = [Shortcut.id], value = number of correct presses.
     */
    var shortcutHitCounts: MutableMap<String, Int> = mutableMapOf()

    /**
     * Per-shortcut reveal counts.
     * Key = [Shortcut.id], value = number of times user peeked.
     */
    var shortcutRevealCounts: MutableMap<String, Int> = mutableMapOf()

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun recordCorrect(shortcutId: String) {
        allTimeCorrect++
        currentSessionCorrect++
        shortcutHitCounts[shortcutId] = (shortcutHitCounts[shortcutId] ?: 0) + 1
    }

    fun recordReveal(shortcutId: String) {
        allTimeRevealed++
        currentSessionRevealed++
        shortcutRevealCounts[shortcutId] = (shortcutRevealCounts[shortcutId] ?: 0) + 1
    }

    fun completeSession() {
        sessionsCompleted++
        currentSessionCorrect = 0
        currentSessionRevealed = 0
    }

    fun buildSnapshot(catalogSize: Int, remaining: Int): Statistics {
        val hardest = shortcutRevealCounts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key to it.value }

        return Statistics(
            totalShortcutsInCatalog = catalogSize,
            sessionsCompleted = sessionsCompleted,
            allTimeCorrect = allTimeCorrect,
            allTimeRevealed = allTimeRevealed,
            currentSessionCorrect = currentSessionCorrect,
            currentSessionRemaining = remaining,
            hardestShortcuts = hardest,
        )
    }

    override fun getState(): StatisticsState = this

    override fun loadState(state: StatisticsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): StatisticsState =
            ApplicationManager.getApplication().getService(StatisticsState::class.java)
    }
}
