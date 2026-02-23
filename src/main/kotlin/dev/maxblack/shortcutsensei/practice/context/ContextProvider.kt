package dev.maxblack.shortcutsensei.practice.context

import com.intellij.openapi.project.Project
import dev.maxblack.shortcutsensei.core.model.ContextType
import dev.maxblack.shortcutsensei.core.model.Shortcut

/**
 * A ContextProvider prepares the IDE environment so that the user can
 * meaningfully execute the shortcut being practised.
 *
 * For example, before asking the user to press "Extend Selection", an
 * [EditorContextProvider] opens a practice file, places the caret on a word,
 * and makes sure no existing selection is active.
 *
 * Each [ContextType] maps to one provider implementation.
 * [NullContextProvider] is used for [ContextType.ANY] — no setup needed.
 */
interface ContextProvider {

    /** Returns the [ContextType] this provider handles. */
    val contextType: ContextType

    /**
     * Sets up the IDE state required for [shortcut].
     * Called on the EDT, before the shortcut prompt is shown to the user.
     *
     * Implementations should be idempotent — calling setup twice should not
     * leave the environment in a broken state.
     */
    fun setup(project: Project, shortcut: Shortcut)

    /**
     * Tears down any state set up in [setup].
     * Called after the user completes (or reveals) the shortcut.
     */
    fun teardown(project: Project, shortcut: Shortcut) {}
}
