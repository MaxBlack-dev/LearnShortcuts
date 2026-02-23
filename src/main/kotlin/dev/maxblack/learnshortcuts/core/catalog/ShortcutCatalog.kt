package dev.maxblack.learnshortcuts.core.catalog

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import dev.maxblack.learnshortcuts.core.model.ContextType
import dev.maxblack.learnshortcuts.core.model.Shortcut
import dev.maxblack.learnshortcuts.core.model.ShortcutCategory
import dev.maxblack.learnshortcuts.core.state.PluginState

/**
 * Loads shortcuts for the practice queue using a two-tier strategy:
 *
 *  **Tier 1 – Curated** ([BuiltinShortcutDefinitions]):
 *  Hand-picked shortcuts with rich metadata (descriptions, context types,
 *  popularity ranks, group links). These always come first in the queue.
 *
 *  **Tier 2 – Auto-discovered** (active keymap):
 *  Every other action that has at least one keyboard shortcut bound in the
 *  active keymap. Display name is taken from the action's presentation text;
 *  category is inferred from action-ID naming conventions. These follow the
 *  curated shortcuts and have [Shortcut.canDemonstrate] = false.
 *
 * This gives 100% keymap coverage while preserving the quality of the curated
 * catalogue. The user can toggle Tier 2 off via Settings → LearnShortcuts.
 */
class ShortcutCatalog {

    /**
     * Returns all shortcuts to practice.
     *
     * Always includes every curated shortcut that has a keymap binding.
     * When [PluginState.includeAllKeymapShortcuts] is enabled, also appends
     * auto-discovered Tier-2 shortcuts for the remaining bound actions.
     */
    fun loadAvailableShortcuts(): List<Shortcut> {
        val keymap = KeymapManager.getInstance().activeKeymap

        // ── Tier 1: curated shortcuts ──────────────────────────────────────────
        val curated = BuiltinShortcutDefinitions.ALL.filter { shortcut ->
            keymap.getShortcuts(shortcut.actionId).isNotEmpty()
        }

        if (!PluginState.getInstance().includeAllKeymapShortcuts) return curated

        // ── Tier 2: auto-discovered from the active keymap ────────────────────
        val curatedIds = curated.map { it.actionId }.toHashSet()
        val actionManager = ActionManager.getInstance()

        val autoShortcuts = keymap.actionIdList
            .asSequence()
            .filter { id -> id !in curatedIds }
            .filter { id ->
                // Keyboard shortcuts only (exclude pure mouse bindings)
                keymap.getShortcuts(id).any { it is KeyboardShortcut }
            }
            .filter { id ->
                // Skip internal / system IDs that are not real user-facing actions
                !id.startsWith("Macro:") && !id.startsWith("_Dummy_") && id != "DummyAction"
            }
            .mapNotNull { id ->
                val action = actionManager.getAction(id) ?: return@mapNotNull null
                val name = action.templatePresentation.text.orEmpty().trim().ifBlank { id }
                Shortcut(
                    id = "auto:$id",
                    actionId = id,
                    displayName = name,
                    description = "Automatically discovered from your active keymap. " +
                        "No guided practice context is available — try it in your own project.",
                    category = guessCategory(id),
                    popularityRank = Int.MAX_VALUE,
                    contextType = ContextType.ANY,
                    canDemonstrate = false,
                    demonstrationNote = "This shortcut was auto-discovered from your keymap. " +
                        "Open any relevant file and try it manually.",
                )
            }
            .sortedBy { it.displayName.lowercase() }
            .toList()

        return curated + autoShortcuts
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Infers a [ShortcutCategory] from an action ID using naming conventions
     * common in the IntelliJ Platform. Falls back to [ShortcutCategory.EDITING].
     */
    private fun guessCategory(actionId: String): ShortcutCategory {
        val id = actionId.lowercase()
        return when {
            id.matches(Regex("(debug|step|resume|pause|evaluate|togglebreakpoint|addtowatch|viewbreakpoints|mutebreakpoints|frames|variables|watches).*")) ->
                ShortcutCategory.DEBUGGING

            id.matches(Regex("(run|build|compile|make|reloadclass|restart|stopprocess|kill|attach|coverage|profile).*")) ->
                ShortcutCategory.BUILD_RUN

            id.matches(Regex("(vcs|git\\.|svn|hg\\.|cvs|changelist|checkin|commit|push|pull|fetch|stash|merge|rebase|cherrypick|annotate|blame|diff|patch|shelve|rollback).*")) ->
                ShortcutCategory.VCS

            id.matches(Regex("(find|replace|searcheverywhere|gotosymbol|gotoclass|gotofile|gotoaction|navigateback|navigateforward|goto|highlight.*occurrence|find.*usage|showhierarchy).*")) ->
                ShortcutCategory.SEARCH_REPLACE

            id.matches(Regex("(navigate|jumptosource|finddeclaration|gotosuper|gotoimplementation|callers|callees|movenext|moveprevious|selectin|showinexplorer).*")) ->
                ShortcutCategory.NAVIGATION

            id.matches(Regex("(refactor|extract|inline|move|rename|introduce|convert|safedelete|pullmembers|pushmembers|migrate|changesignature|anonymoustoinner|wrapreturnvalue).*")) ->
                ShortcutCategory.REFACTORING

            id.matches(Regex("(bookmark|mnemonic|favorites).*")) ->
                ShortcutCategory.BOOKMARKS

            id.matches(Regex("(fold|unfold|expanddoccomments|codefolding|foldselection|foldblock).*")) ->
                ShortcutCategory.CODE_FOLDING

            id.matches(Regex("(completion|basiccompletion|smarttype|classnamecompletion|wordcompletion|codecompletion|hippie|cycleexpand|selectnexttemplate|quickfix|showintentionactions).*")) ->
                ShortcutCategory.CODE_COMPLETION

            id.matches(Regex("(multicaret|addcaret|clonecaret|columnselection|virtualedit|blockselection|togglecolumnmode|editmultiple).*")) ->
                ShortcutCategory.MULTIPLE_CURSORS

            id.matches(Regex("(toolwindow|activat.*toolwindow|hide.*toolwindow|show.*toolwindow|focustoolwindow|maximizetool|pintoolwindow|undocktool).*")) ->
                ShortcutCategory.TOOL_WINDOWS

            id.matches(Regex("(window|splitwindow|closewindow|closecontent|nexttab|prevtab|switcher|movetab|undocktab|flipframe|maximizewindow|minimizewindow|fullscreen|distractionfree|presentationmode).*")) ->
                ShortcutCategory.WINDOW_MANAGEMENT

            id.matches(Regex("(newfile|openfile|closefile|savefile|savealldocuments|syncfile|revertfile|filetype|newdir|newclass|newpackage|newelement|copyfilerefs|movetogroup|recentfiles|recentchangedfiles).*")) ->
                ShortcutCategory.FILE_MANAGEMENT

            id.matches(Regex("(livetemplate|surround.*live|insertlive|expandlive|listtemplates).*")) ->
                ShortcutCategory.LIVE_TEMPLATES

            else -> ShortcutCategory.EDITING
        }
    }
}
