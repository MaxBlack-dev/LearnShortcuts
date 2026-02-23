package dev.maxblack.shortcutsensei.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class OpenPracticeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ToolWindowManager.getInstance(project)
            .getToolWindow("ShortcutSensei")
            ?.show()
    }
}

class ShowStatisticsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val tw = ToolWindowManager.getInstance(project)
            .getToolWindow("ShortcutSensei") ?: return
        tw.show()
        // Switch to the Statistics tab (index 1)
        tw.contentManager.setSelectedContent(
            tw.contentManager.getContent(1) ?: return,
        )
    }
}
