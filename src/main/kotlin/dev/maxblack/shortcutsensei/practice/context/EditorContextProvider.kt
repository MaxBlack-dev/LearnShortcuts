package dev.maxblack.shortcutsensei.practice.context

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.maxblack.shortcutsensei.core.model.ContextType
import dev.maxblack.shortcutsensei.core.model.Shortcut
import dev.maxblack.shortcutsensei.practice.project.PracticeProjectGenerator

/**
 * Handles shortcuts that need an editor with source code open.
 *
 * Sets up:
 * - A practice Java file from [PracticeProjectGenerator]
 * - Caret positioned at an appropriate location for the shortcut type
 */
class EditorContextProvider : ContextProvider {

    override val contextType: ContextType = ContextType.EDITOR_JAVA

    override fun setup(project: Project, shortcut: Shortcut) {
        ApplicationManager.getApplication().invokeLater {
            val file = getPracticeFile(project, shortcut) ?: return@invokeLater
            val editor = FileEditorManager.getInstance(project)
                .openTextEditor(OpenFileDescriptor(project, file), true)
                ?: return@invokeLater

            // Position caret at an appropriate offset for the shortcut's demo context
            val offset = PracticeProjectGenerator.getCaretOffsetFor(shortcut)
            if (offset >= 0 && offset < editor.document.textLength) {
                editor.caretModel.moveToOffset(offset)
            }
        }
    }

    private fun getPracticeFile(project: Project, shortcut: Shortcut): VirtualFile? =
        PracticeProjectGenerator.getOrCreatePracticeFile(project, shortcut)
}

/** No-op provider for shortcuts that need no special context. */
class NullContextProvider : ContextProvider {
    override val contextType: ContextType = ContextType.ANY
    override fun setup(project: Project, shortcut: Shortcut) { /* nothing */ }
}

/** Registry — maps each ContextType to a provider. */
object ContextProviderRegistry {

    private val providers: Map<ContextType, ContextProvider> = mapOf(
        ContextType.ANY to NullContextProvider(),
        ContextType.EDITOR_JAVA to EditorContextProvider(),
        ContextType.EDITOR_ANY_CODE to EditorContextProvider(),
        ContextType.EDITOR_WITH_ERRORS to EditorContextProvider(),
        ContextType.EDITOR_WITH_SELECTION to EditorContextProvider(),
        ContextType.EDITOR_MULTIPLE_CARETS to EditorContextProvider(),
        ContextType.MULTIPLE_TABS_OPEN to EditorContextProvider(),
        // DEBUGGER_ACTIVE, VCS_CHANGES etc. use NullContextProvider for now
        ContextType.DEBUGGER_ACTIVE to NullContextProvider(),
        ContextType.VCS_CHANGES to NullContextProvider(),
        ContextType.PROJECT_TREE to NullContextProvider(),
        ContextType.SEARCH_OPEN to NullContextProvider(),
    )

    fun get(type: ContextType): ContextProvider =
        providers[type] ?: NullContextProvider()
}
