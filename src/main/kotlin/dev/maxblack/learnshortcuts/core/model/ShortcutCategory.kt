package dev.maxblack.learnshortcuts.core.model

/**
 * All high-level shortcut categories.
 * Displayed in the Settings panel so the user can restrict practice to
 * specific areas (e.g. "only Refactoring shortcuts today").
 */
enum class ShortcutCategory(val displayName: String) {
    CODE_COMPLETION("Code Completion"),
    EDITING("Editing"),
    MULTIPLE_CURSORS("Multiple Cursors"),
    NAVIGATION("Navigation"),
    REFACTORING("Refactoring"),
    SEARCH_REPLACE("Search & Replace"),
    CODE_FOLDING("Code Folding"),
    BUILD_RUN("Build & Run"),
    DEBUGGING("Debugging"),
    VCS("Version Control"),
    TOOL_WINDOWS("Tool Windows"),
    WINDOW_MANAGEMENT("Window Management"),
    FILE_MANAGEMENT("File Management"),
    LIVE_TEMPLATES("Live Templates"),
    BOOKMARKS("Bookmarks"),
}

/**
 * Describes the IDE state / editor context that must be in place before
 * this shortcut can be demonstrated meaningfully.
 *
 * The [ContextProvider] implementations use this to set up the practice
 * environment automatically before each shortcut prompt.
 */
enum class ContextType {
    /** No special context needed — works anywhere. */
    ANY,

    /** Any source file open in the editor. */
    EDITOR_ANY_CODE,

    /** A Java or Kotlin file open with valid, compilable code. */
    EDITOR_JAVA,

    /** Editor with at least one active error or warning marker. */
    EDITOR_WITH_ERRORS,

    /** Editor with a non-empty selection. */
    EDITOR_WITH_SELECTION,

    /** Editor with more than one active caret. */
    EDITOR_MULTIPLE_CARETS,

    /** An active debug session with execution paused at a breakpoint. */
    DEBUGGER_ACTIVE,

    /** Project tree (file explorer) focused. */
    PROJECT_TREE,

    /** Find / Replace toolbar currently open in the editor. */
    SEARCH_OPEN,

    /** At least two editor tabs open simultaneously. */
    MULTIPLE_TABS_OPEN,

    /** Uncommitted VCS changes exist in the project. */
    VCS_CHANGES,
}
