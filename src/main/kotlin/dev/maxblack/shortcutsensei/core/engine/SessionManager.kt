package dev.maxblack.shortcutsensei.core.engine

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import dev.maxblack.shortcutsensei.core.catalog.ShortcutRegistry
import dev.maxblack.shortcutsensei.core.model.Session
import dev.maxblack.shortcutsensei.core.model.Shortcut
import dev.maxblack.shortcutsensei.core.state.PluginState
import dev.maxblack.shortcutsensei.core.state.PracticeOrder
import dev.maxblack.shortcutsensei.core.state.StatisticsState

/**
 * Project-scoped service that owns the [SessionManager] for the current project.
 * Registered as a projectService in plugin.xml so each open project gets its
 * own independent instance.
 */
@Service(Service.Level.PROJECT)
class SessionManagerService(private val project: Project) {

    private val registry = ShortcutRegistry()
    val sessionManager = SessionManager(registry)

    fun initialize() {
        registry.initialize()
    }

    fun totalShortcutCount(): Int = registry.getTotalCount()
}

/**
 * Core session engine.
 *
 * Responsibilities:
 * - Build the practice queue according to the selected [PracticeOrder].
 * - Advance through shortcuts on correct input.
 * - Handle the Reveal → send-to-back logic.
 * - Notify all registered [SessionListener]s of state changes.
 * - Delegate statistics recording to [StatisticsState].
 */
class SessionManager(
    private val registry: ShortcutRegistry,
    private val settings: PluginState = PluginState.getInstance(),
    private val statistics: StatisticsState = StatisticsState.getInstance(),
) {

    private var currentSession: Session? = null
    private val listeners = mutableListOf<SessionListener>()

    // ── Public API ────────────────────────────────────────────────────────────

    /** Starts a fresh session, replacing any active one. */
    fun startSession(): Session {
        val ordered = buildQueue()
        val session = Session(
            totalShortcuts = ordered.size,
            remainingCount = ordered.size,
        ).also { s -> s.queue.addAll(ordered) }

        currentSession = session
        advanceToNext(session)
        listeners.forEach { it.onSessionStarted(session) }
        return session
    }

    fun getCurrentSession(): Session? = currentSession

    /**
     * Call this when the user presses a key combo that matches the current shortcut.
     *
     * If the shortcut was revealed → send to back, don't count.
     * Otherwise → record correct, decrement remaining, advance.
     */
    fun onCorrectInput() {
        val session = currentSession ?: return
        val shortcut = session.currentShortcut ?: return

        if (session.isCurrentRevealed) {
            // User peeked — shortcut goes to the back of the queue for another try
            session.queue.addLast(shortcut)
            statistics.recordReveal(shortcut.id)
            session.revealedCount++
            session.isCurrentRevealed = false
            advanceToNext(session)
            listeners.forEach { it.onRevealedShortcutRetried(session) }
            return
        }

        // Genuine correct answer
        statistics.recordCorrect(shortcut.id)
        session.correctCount++
        session.remainingCount--
        session.isCurrentRevealed = false

        if (session.queue.isEmpty()) {
            finaliseSession(session)
        } else {
            advanceToNext(session)
            listeners.forEach { it.onProgressUpdated(session) }
        }
    }

    /**
     * Call this when the user presses the "Reveal" button.
     * Only sets the flag — the shortcut is NOT moved until the user presses it.
     */
    fun onReveal() {
        val session = currentSession ?: return
        session.isCurrentRevealed = true
        listeners.forEach { it.onShortcutRevealed(session) }
    }

    fun addListener(l: SessionListener) { listeners.add(l) }
    fun removeListener(l: SessionListener) { listeners.remove(l) }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private fun advanceToNext(session: Session) {
        session.currentShortcut = session.queue.removeFirstOrNull()
        listeners.forEach { it.onShortcutChanged(session) }
    }

    private fun finaliseSession(session: Session) {
        session.isActive = false
        statistics.completeSession()
        listeners.forEach { it.onSessionCompleted(session) }
    }

    private fun buildQueue(): List<Shortcut> {
        val all = registry.getAllShortcuts()
        return when (settings.practiceOrder) {
            PracticeOrder.POPULARITY -> all.sortedBy { it.popularityRank }
            PracticeOrder.RANDOM     -> all.shuffled()
            PracticeOrder.CATEGORY   -> all.sortedWith(
                compareBy({ it.category.name }, { it.popularityRank })
            )
        }
    }
}

/** Observer interface for UI components that need to react to session events. */
interface SessionListener {
    fun onSessionStarted(session: Session) {}
    fun onShortcutChanged(session: Session) {}
    fun onShortcutRevealed(session: Session) {}
    fun onRevealedShortcutRetried(session: Session) {}
    fun onProgressUpdated(session: Session) {}
    fun onSessionCompleted(session: Session) {}
}
