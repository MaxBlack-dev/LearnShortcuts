package dev.maxblack.learnshortcuts.core.engine

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import dev.maxblack.learnshortcuts.core.model.Shortcut
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * Verifies whether a [KeyEvent] matches any binding registered for a [Shortcut]
 * in the currently active keymap.
 *
 * Because this reads from the live keymap at the moment of verification,
 * it correctly handles:
 *  - Custom keymaps
 *  - macOS vs Windows vs Linux default bindings
 *  - User-remapped shortcuts
 */
object ShortcutVerifier {

    /**
     * Returns true if [event] matches the primary (or any secondary) keyboard
     * shortcut bound to [shortcut.actionId] in the active keymap.
     */
    fun matches(shortcut: Shortcut, event: KeyEvent): Boolean {
        val keymap = KeymapManager.getInstance().activeKeymap
        val pressed = KeyStroke.getKeyStrokeForEvent(event)
        return keymap.getShortcuts(shortcut.actionId).any { s ->
            s is KeyboardShortcut && s.firstKeyStroke == pressed
        }
    }

    /**
     * Returns a human-readable display string for the primary key binding of
     * [shortcut] in the active keymap (e.g. "Ctrl+W", "⌘ D", "Alt+Enter").
     *
     * Returns "(unbound)" if no keyboard shortcut is currently assigned.
     */
    fun getDisplayString(shortcut: Shortcut): String {
        val keymap = KeymapManager.getInstance().activeKeymap
        val ks = keymap.getShortcuts(shortcut.actionId)
            .filterIsInstance<KeyboardShortcut>()
            .firstOrNull()
            ?: return "(unbound)"

        return formatKeyStroke(ks.firstKeyStroke)
    }

    /**
     * Returns BOTH strokes for a two-keystroke shortcut, or just the first
     * stroke if it is a single-key binding.
     */
    fun getFullDisplayString(shortcut: Shortcut): String {
        val keymap = KeymapManager.getInstance().activeKeymap
        val ks = keymap.getShortcuts(shortcut.actionId)
            .filterIsInstance<KeyboardShortcut>()
            .firstOrNull()
            ?: return "(unbound)"

        val first = formatKeyStroke(ks.firstKeyStroke)
        val second = ks.secondKeyStroke?.let { ", ${formatKeyStroke(it)}" } ?: ""
        return "$first$second"
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun formatKeyStroke(ks: KeyStroke): String = buildString {
        val m = ks.modifiers
        // Order: Meta (⌘) → Ctrl → Alt → Shift
        if (m and KeyEvent.META_DOWN_MASK  != 0) append("⌘ ")
        if (m and KeyEvent.CTRL_DOWN_MASK  != 0) append("Ctrl+")
        if (m and KeyEvent.ALT_DOWN_MASK   != 0) append("Alt+")
        if (m and KeyEvent.SHIFT_DOWN_MASK != 0) append("Shift+")
        append(KeyEvent.getKeyText(ks.keyCode))
    }
}
