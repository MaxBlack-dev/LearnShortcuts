package dev.maxblack.learnshortcuts.core.catalog

import com.intellij.openapi.keymap.KeymapManager
import dev.maxblack.learnshortcuts.core.model.Shortcut

/**
 * Loads the built-in shortcut definitions and cross-references them against
 * the currently active keymap, returning only shortcuts that actually have
 * a binding on the current machine / OS.
 *
 * This is the only place in the codebase that touches the IntelliJ keymap API —
 * everywhere else works with plain [Shortcut] data objects.
 */
class ShortcutCatalog {

    /**
     * Returns all shortcuts from [BuiltinShortcutDefinitions] that have at least
     * one keyboard shortcut bound in the currently active keymap.
     *
     * Shortcuts with [Shortcut.canDemonstrate] = false are included by default
     * so users can still see and learn them; the practice panel renders them
     * with an explanation instead of a live demo.
     */
    fun loadAvailableShortcuts(): List<Shortcut> {
        val keymap = KeymapManager.getInstance().activeKeymap
        return BuiltinShortcutDefinitions.ALL.filter { shortcut ->
            keymap.getShortcuts(shortcut.actionId).isNotEmpty()
        }
    }
}
