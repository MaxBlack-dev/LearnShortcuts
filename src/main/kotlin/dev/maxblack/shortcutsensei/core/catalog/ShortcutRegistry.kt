package dev.maxblack.shortcutsensei.core.catalog

import dev.maxblack.shortcutsensei.core.model.Shortcut
import dev.maxblack.shortcutsensei.core.model.ShortcutCategory
import dev.maxblack.shortcutsensei.core.model.ShortcutGroup

/**
 * Central registry — initialised once on plugin startup and queried by the engine.
 *
 * Call [initialize] after the IDE keymap is available (i.e. inside a coroutine
 * started from [ProjectActivity.execute]).
 */
class ShortcutRegistry(private val catalog: ShortcutCatalog = ShortcutCatalog()) {

    private var shortcuts: List<Shortcut> = emptyList()
    private var groups: List<ShortcutGroup> = emptyList()

    fun initialize() {
        shortcuts = catalog.loadAvailableShortcuts()
        groups = buildGroups()
    }

    fun getAllShortcuts(): List<Shortcut> = shortcuts

    fun getByCategory(category: ShortcutCategory): List<Shortcut> =
        shortcuts.filter { it.category == category }

    fun getAllGroups(): List<ShortcutGroup> = groups

    fun getGroupFor(shortcut: Shortcut): ShortcutGroup? =
        shortcut.groupId?.let { gid -> groups.find { it.id == gid } }

    fun getTotalCount(): Int = shortcuts.size

    // ── Private ───────────────────────────────────────────────────────────────

    private fun buildGroups(): List<ShortcutGroup> =
        BuiltinShortcutDefinitions.GROUP_DEFS.mapNotNull { def ->
            val members = def.shortcutIds.mapNotNull { id ->
                shortcuts.find { it.id == id }
            }
            if (members.isEmpty()) null
            else ShortcutGroup(
                id = def.id,
                displayName = def.displayName,
                description = def.description,
                shortcuts = members.sortedBy { it.groupPosition },
            )
        }
}
