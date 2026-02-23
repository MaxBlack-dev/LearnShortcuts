package dev.maxblack.learnshortcuts.core.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Application-level persistent settings for LearnShortcuts.
 * Stored in {@code LearnShortcutsSettings.xml} inside the IDE config directory.
 */
@State(
    name = "LearnShortcutsSettings",
    storages = [Storage("LearnShortcutsSettings.xml")],
)
class PluginState : PersistentStateComponent<PluginState> {

    /** Order in which shortcuts are presented to the user. */
    var practiceOrder: PracticeOrder = PracticeOrder.POPULARITY

    /**
     * Seconds before the shortcut hint is auto-revealed.
     * 0 = never auto-reveal (user must press the Reveal button manually).
     */
    var autoRevealAfterSeconds: Int = 0

    /** Whether to include shortcuts that cannot be demonstrated with live context. */
    var includeNonDemonstrableShortcuts: Boolean = true

    /** Category IDs the user has enabled. Empty set = all categories enabled. */
    var enabledCategoryIds: MutableSet<String> = mutableSetOf()

    /** Whether to play a success sound on correct input. */
    var enableSoundFeedback: Boolean = false

    /**
     * When true, every keyboard shortcut from the active keymap is included in
     * the practice queue (auto-discovered beyond the curated list).
     * When false, only the ~70–100 hand-picked curated shortcuts are used.
     */
    var includeAllKeymapShortcuts: Boolean = true

    override fun getState(): PluginState = this

    override fun loadState(state: PluginState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): PluginState =
            ApplicationManager.getApplication().getService(PluginState::class.java)
    }
}

enum class PracticeOrder(val displayName: String) {
    /** Most commonly used by developers first (based on JetBrains surveys). */
    POPULARITY("Most Popular First"),

    /** Completely random every session. */
    RANDOM("Random"),

    /** Grouped by category (Editing, Navigation, Refactoring…). */
    CATEGORY("By Category"),
}
