package dev.maxblack.shortcutsensei.ui.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import dev.maxblack.shortcutsensei.core.state.PluginState
import dev.maxblack.shortcutsensei.core.state.PracticeOrder
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

/**
 * Settings UI panel.
 *
 * Settings exposed to the user:
 * 1. Practice order  (Popularity / Random / By Category)
 * 2. Auto-reveal     (0 = off, or N seconds before hint shown automatically)
 * 3. Include shortcuts that cannot be demonstrated
 * 4. Sound feedback  (success chime on correct input)
 */
class SettingsPanel : JBPanel<SettingsPanel>(GridBagLayout()) {

    // ── Controls ──────────────────────────────────────────────────────────────

    private val orderCombo = JComboBox(PracticeOrder.entries.map { it.displayName }.toTypedArray())

    private val autoRevealSpinner = JSpinner(SpinnerNumberModel(0, 0, 120, 5)).apply {
        toolTipText = "0 = never auto-reveal. Set to N to auto-reveal after N seconds of inactivity."
    }

    private val includeNonDemonstrableCheck = JBCheckBox(
        "Include shortcuts that cannot be demonstrated live",
        true,
    ).apply {
        toolTipText = "These shortcuts still appear in the queue with an explanation of why they cannot be shown interactively."
    }

    private val soundCheck = JBCheckBox("Play sound on correct input", false)

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        border = JBUI.Borders.empty(10)
        buildLayout()

        val state = PluginState.getInstance()
        setSelectedOrder(state.practiceOrder)
        setAutoRevealSeconds(state.autoRevealAfterSeconds)
        setIncludeNonDemonstrable(state.includeNonDemonstrableShortcuts)
        setSoundEnabled(state.enableSoundFeedback)
    }

    private fun buildLayout() {
        val gc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(4, 0)
            gridx = 0
        }

        fun label(text: String) = JBLabel(text)
        fun row(label: JComponent, control: JComponent) {
            gc.gridy++
            gc.gridwidth = 1
            gc.weightx = 0.0
            add(label, gc)
            gc.gridx = 1
            gc.weightx = 1.0
            add(control, gc)
            gc.gridx = 0
        }

        row(label("Practice order:"), orderCombo)
        row(label("Auto-reveal after (seconds, 0 = off):"), autoRevealSpinner)

        gc.gridy++
        gc.gridwidth = 2
        gc.weightx = 1.0
        add(includeNonDemonstrableCheck, gc)

        gc.gridy++
        add(soundCheck, gc)

        // Filler
        gc.gridy++
        gc.weighty = 1.0
        gc.fill = GridBagConstraints.VERTICAL
        add(JPanel(), gc)
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    fun getSelectedOrder(): PracticeOrder =
        PracticeOrder.entries[orderCombo.selectedIndex]

    fun setSelectedOrder(order: PracticeOrder) {
        orderCombo.selectedIndex = PracticeOrder.entries.indexOf(order)
    }

    fun getAutoRevealSeconds(): Int = autoRevealSpinner.value as Int
    fun setAutoRevealSeconds(v: Int) { autoRevealSpinner.value = v }

    fun isIncludeNonDemonstrable(): Boolean = includeNonDemonstrableCheck.isSelected
    fun setIncludeNonDemonstrable(v: Boolean) { includeNonDemonstrableCheck.isSelected = v }

    fun isSoundEnabled(): Boolean = soundCheck.isSelected
    fun setSoundEnabled(v: Boolean) { soundCheck.isSelected = v }
}
