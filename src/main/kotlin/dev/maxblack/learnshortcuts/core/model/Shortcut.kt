package dev.maxblack.learnshortcuts.core.model

/**
 * Represents a single IntelliJ IDEA shortcut / action that the user should learn.
 *
 * Key design decisions:
 * - [actionId] is the IntelliJ Platform action ID (e.g. "CloseContent").
 *   The actual key binding is always retrieved at runtime from the active keymap —
 *   never hardcoded — so the plugin works on every OS and keymap automatically.
 * - [popularityRank] is derived from JetBrains developer surveys and community data.
 *   Lower = used more often (rank 1 = most popular).
 * - [groupId] links related shortcuts that should be practised in sequence
 *   (e.g. Extend Selection → Shrink Selection).
 * - [canDemonstrate] flags shortcuts where it is technically impossible to show
 *   live context (e.g. exiting the IDE, full-screen toggle). These still appear
 *   in the queue but are presented with a descriptive explanation instead of
 *   a live demo.
 */
data class Shortcut(
    /** Unique identifier within LearnShortcuts's catalogue. */
    val id: String,

    /** IntelliJ Platform action ID used to look up the current keymap binding. */
    val actionId: String,

    /** Short human-readable name shown as the prompt in the practice panel. */
    val displayName: String,

    /** Longer explanation of what the shortcut does, shown after correct input. */
    val description: String,

    /** Grouping for filtering and display in the statistics / settings UI. */
    val category: ShortcutCategory,

    /**
     * Popularity rank among developers (1 = most popular).
     * Used when the user selects "Popularity order" in settings.
     */
    val popularityRank: Int,

    /** The type of editor/IDE context required to demonstrate this shortcut. */
    val contextType: ContextType,

    /** Links this shortcut to a [ShortcutGroup] for sequential practice. Null if standalone. */
    val groupId: String? = null,

    /** Position within the group (0-based). Groups are practised in ascending order. */
    val groupPosition: Int = 0,

    /**
     * Whether a live demonstration is possible.
     * If false, the practice panel shows [demonstrationNote] instead of a live demo.
     */
    val canDemonstrate: Boolean = true,

    /**
     * When [canDemonstrate] is false, this explains WHY to the user.
     * Also surfaced in the "Shortcuts we cannot demonstrate" report.
     */
    val demonstrationNote: String? = null,

    /**
     * For shortcuts that share the same [actionId] but produce different results
     * depending on code context (e.g. Complete Statement variants).
     * Used by context providers to set up the right code snippet.
     */
    val codeCompletionVariant: String? = null,
)
