package dev.maxblack.learnshortcuts.core.catalog

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BuiltinShortcutDefinitionsTest {

    @Test
    fun `all shortcut IDs are unique`() {
        val ids = BuiltinShortcutDefinitions.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Duplicate shortcut IDs found: ${ids.groupBy { it }.filter { it.value.size > 1 }.keys}")
    }

    @Test
    fun `all shortcuts have non-blank displayName and description`() {
        BuiltinShortcutDefinitions.ALL.forEach { shortcut ->
            assertTrue(shortcut.displayName.isNotBlank(), "Blank displayName for id=${shortcut.id}")
            assertTrue(shortcut.description.isNotBlank(), "Blank description for id=${shortcut.id}")
        }
    }

    @Test
    fun `shortcuts with canDemonstrate false have demonstrationNote`() {
        BuiltinShortcutDefinitions.ALL
            .filter { !it.canDemonstrate }
            .forEach { shortcut ->
                assertNotNull(shortcut.demonstrationNote,
                    "Shortcut '${shortcut.id}' has canDemonstrate=false but no demonstrationNote")
                assertTrue(shortcut.demonstrationNote!!.isNotBlank(),
                    "Blank demonstrationNote for id=${shortcut.id}")
            }
    }

    @Test
    fun `all group shortcut IDs referenced in GROUP_DEFS exist in ALL`() {
        val allIds = BuiltinShortcutDefinitions.ALL.map { it.id }.toSet()
        BuiltinShortcutDefinitions.GROUP_DEFS.forEach { group ->
            group.shortcutIds.forEach { id ->
                assertTrue(id in allIds,
                    "Group '${group.id}' references unknown shortcut id='$id'")
            }
        }
    }

    @Test
    fun `group IDs on shortcuts match existing GROUP_DEFS`() {
        val groupIds = BuiltinShortcutDefinitions.GROUP_DEFS.map { it.id }.toSet()
        BuiltinShortcutDefinitions.ALL
            .filter { it.groupId != null }
            .forEach { shortcut ->
                assertTrue(shortcut.groupId in groupIds,
                    "Shortcut '${shortcut.id}' references unknown groupId='${shortcut.groupId}'")
            }
    }

    @Test
    fun `popularity ranks are positive`() {
        BuiltinShortcutDefinitions.ALL.forEach { shortcut ->
            assertTrue(shortcut.popularityRank > 0,
                "Shortcut '${shortcut.id}' has non-positive popularityRank=${shortcut.popularityRank}")
        }
    }

    @Test
    fun `group defs have unique IDs`() {
        val ids = BuiltinShortcutDefinitions.GROUP_DEFS.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Duplicate GROUP_DEF IDs found")
    }

    @Test
    fun `catalogue contains at least 50 shortcuts`() {
        assertTrue(BuiltinShortcutDefinitions.ALL.size >= 50,
            "Expected at least 50 shortcuts, got ${BuiltinShortcutDefinitions.ALL.size}")
    }

    @Test
    fun `non-demonstrable shortcuts list is documented`() {
        val nonDemo = BuiltinShortcutDefinitions.ALL.filter { !it.canDemonstrate }
        assertTrue(nonDemo.isNotEmpty(), "Expected at least some non-demonstrable shortcuts")
        println("Non-demonstrable shortcuts (${nonDemo.size}):")
        nonDemo.forEach { println("  [${it.id}] ${it.displayName} — ${it.demonstrationNote}") }
    }
}
