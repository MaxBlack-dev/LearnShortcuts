package dev.maxblack.learnshortcuts.ui.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.util.NlsContexts
import dev.maxblack.learnshortcuts.core.state.PluginState
import javax.swing.JComponent

/**
 * Integrates LearnShortcuts settings into IntelliJ's Settings / Preferences dialog.
 * Appears under Tools → LearnShortcuts.
 */
class SettingsConfigurable : SearchableConfigurable {

    private var panel: SettingsPanel? = null

    override fun getId(): String = "dev.maxblack.learnshortcuts.settings"

    @NlsContexts.ConfigurableName
    override fun getDisplayName(): String = "LearnShortcuts"

    override fun createComponent(): JComponent {
        panel = SettingsPanel()
        return panel!!
    }

    override fun isModified(): Boolean {
        val p = panel ?: return false
        val state = PluginState.getInstance()
        return p.getSelectedOrder() != state.practiceOrder ||
            p.getAutoRevealSeconds() != state.autoRevealAfterSeconds ||
            p.isIncludeNonDemonstrable() != state.includeNonDemonstrableShortcuts ||
            p.isSoundEnabled() != state.enableSoundFeedback
    }

    override fun apply() {
        val p = panel ?: return
        val state = PluginState.getInstance()
        state.practiceOrder = p.getSelectedOrder()
        state.autoRevealAfterSeconds = p.getAutoRevealSeconds()
        state.includeNonDemonstrableShortcuts = p.isIncludeNonDemonstrable()
        state.enableSoundFeedback = p.isSoundEnabled()
    }

    override fun reset() {
        val state = PluginState.getInstance()
        panel?.apply {
            setSelectedOrder(state.practiceOrder)
            setAutoRevealSeconds(state.autoRevealAfterSeconds)
            setIncludeNonDemonstrable(state.includeNonDemonstrableShortcuts)
            setSoundEnabled(state.enableSoundFeedback)
        }
    }

    override fun disposeUIResources() {
        panel = null
    }
}
