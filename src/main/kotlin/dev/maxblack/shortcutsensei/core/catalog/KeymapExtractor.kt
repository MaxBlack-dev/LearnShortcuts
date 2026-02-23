package dev.maxblack.shortcutsensei.core.catalog

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager

/**
 * Low-level utility for inspecting the active keymap.
 * Used by the catalogue and settings UI to enumerate and display bindings.
 */
object KeymapExtractor {

    /** Returns every action ID that has at least one keyboard shortcut bound. */
    fun getAllBoundActionIds(): Set<String> {
        val keymap = KeymapManager.getInstance().activeKeymap
        return keymap.actionIdList
            .filter { id -> keymap.getShortcuts(id).any { it is KeyboardShortcut } }
            .toSet()
    }

    /** The name of the currently active keymap (e.g. "Mac OS X 10.5+"). */
    fun activeKeymapName(): String =
        KeymapManager.getInstance().activeKeymap.name

    /**
     * Returns a raw display string for the first keyboard shortcut of [actionId],
     * or "(unbound)" if none exists in the current keymap.
     */
    fun rawDisplayString(actionId: String): String {
        val keymap = KeymapManager.getInstance().activeKeymap
        val ks = keymap.getShortcuts(actionId)
            .filterIsInstance<KeyboardShortcut>()
            .firstOrNull()
            ?: return "(unbound)"
        return ks.firstKeyStroke.toString()
    }
}
