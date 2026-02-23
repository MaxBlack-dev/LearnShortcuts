package dev.maxblack.shortcutsensei.core.model

/**
 * A logical group of [Shortcut]s that should be practised in sequence because
 * they are conceptually related.
 *
 * Example: the "Extend / Shrink Selection" group contains:
 *   1. Extend Selection to word
 *   2. Extend Selection to line
 *   3. Extend Selection to statement
 *   4. Shrink Selection (×2)
 *
 * When the user reaches a shortcut that belongs to a group, the entire group
 * is presented back-to-back before the normal queue continues.
 */
data class ShortcutGroup(
    val id: String,
    val displayName: String,
    val description: String,
    /** Ordered list — the engine presents these in [Shortcut.groupPosition] order. */
    val shortcuts: List<Shortcut>,
)
