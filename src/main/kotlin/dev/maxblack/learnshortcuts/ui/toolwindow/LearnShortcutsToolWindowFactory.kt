package dev.maxblack.learnshortcuts.ui.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Registers and creates the LearnShortcuts tool window content.
 * Declared in plugin.xml under <toolWindow>.
 */
class LearnShortcutsToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val practicePanel = PracticePanel(project)
        val content = ContentFactory.getInstance()
            .createContent(practicePanel, "Practice", false)
        toolWindow.contentManager.addContent(content)

        val statsPanel = StatisticsPanel(project)
        val statsContent = ContentFactory.getInstance()
            .createContent(statsPanel, "Statistics", false)
        toolWindow.contentManager.addContent(statsContent)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}
