package dev.maxblack.shortcutsensei.ui.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import dev.maxblack.shortcutsensei.core.engine.SessionManagerService
import dev.maxblack.shortcutsensei.core.state.StatisticsState
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * Statistics panel — second tab in the tool window.
 *
 * Shows:
 *  - High-level counters (total shortcuts, sessions completed, all-time correct/revealed)
 *  - Per-session progress
 *  - "Hardest shortcuts" table (most-revealed, top 10)
 */
class StatisticsPanel(private val project: Project) : JBPanel<StatisticsPanel>(BorderLayout()) {

    private val stats = StatisticsState.getInstance()
    private val registryService = project.getService(SessionManagerService::class.java)

    private val sessionsLabel = statLabel("–")
    private val totalLabel = statLabel("–")
    private val allCorrectLabel = statLabel("–")
    private val allRevealedLabel = statLabel("–")
    private val sessionCorrectLabel = statLabel("–")
    private val sessionRemainingLabel = statLabel("–")

    private val tableModel = DefaultTableModel(arrayOf("Shortcut ID", "Times Revealed"), 0)
    private val hardestTable = JBTable(tableModel).apply { isEnabled = false }

    private val refreshButton = JButton("↻  Refresh").apply {
        addActionListener { refresh() }
    }

    init {
        border = JBUI.Borders.empty(10)
        buildLayout()
        refresh()
    }

    private fun buildLayout() {
        val summaryPanel = JBPanel<JBPanel<*>>(GridLayout(0, 2, 8, 4)).apply {
            border = BorderFactory.createTitledBorder("Overall Statistics")
            add(JBLabel("Total shortcuts in catalogue:"))
            add(totalLabel)
            add(JBLabel("Sessions completed:"))
            add(sessionsLabel)
            add(JBLabel("All-time correct:"))
            add(allCorrectLabel)
            add(JBLabel("All-time revealed:"))
            add(allRevealedLabel)
        }

        val sessionPanel = JBPanel<JBPanel<*>>(GridLayout(0, 2, 8, 4)).apply {
            border = BorderFactory.createTitledBorder("Current Session")
            add(JBLabel("Correct so far:"))
            add(sessionCorrectLabel)
            add(JBLabel("Remaining:"))
            add(sessionRemainingLabel)
        }

        val tablePanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Hardest Shortcuts (most revealed)")
            add(JScrollPane(hardestTable), BorderLayout.CENTER)
        }

        val topPanel = JBPanel<JBPanel<*>>(GridLayout(2, 1, 0, 8)).apply {
            add(summaryPanel)
            add(sessionPanel)
        }

        add(topPanel, BorderLayout.NORTH)
        add(tablePanel, BorderLayout.CENTER)
        add(refreshButton, BorderLayout.SOUTH)
    }

    fun refresh() {
        val session = registryService.sessionManager.getCurrentSession()
        val catalogSize = registryService.totalShortcutCount()
        val snapshot = stats.buildSnapshot(
            catalogSize = catalogSize,
            remaining = session?.remainingCount ?: 0,
        )

        totalLabel.text = snapshot.totalShortcutsInCatalog.toString()
        sessionsLabel.text = snapshot.sessionsCompleted.toString()
        allCorrectLabel.text = snapshot.allTimeCorrect.toString()
        allRevealedLabel.text = snapshot.allTimeRevealed.toString()
        sessionCorrectLabel.text = snapshot.currentSessionCorrect.toString()
        sessionRemainingLabel.text = snapshot.currentSessionRemaining.toString()

        tableModel.rowCount = 0
        snapshot.hardestShortcuts.forEach { (id, count) ->
            tableModel.addRow(arrayOf(id, count))
        }
    }

    private fun statLabel(text: String) = JBLabel(text).apply {
        font = font.deriveFont(Font.BOLD)
        foreground = JBColor(0x0067C0, 0x589DF6)
    }
}
