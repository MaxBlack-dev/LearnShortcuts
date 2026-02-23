package dev.maxblack.shortcutsensei.ui.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import dev.maxblack.shortcutsensei.core.engine.SessionListener
import dev.maxblack.shortcutsensei.core.engine.SessionManager
import dev.maxblack.shortcutsensei.core.engine.SessionManagerService
import dev.maxblack.shortcutsensei.core.engine.ShortcutVerifier
import dev.maxblack.shortcutsensei.core.model.Session
import dev.maxblack.shortcutsensei.core.model.Shortcut
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import javax.swing.*

/**
 * The main practice panel shown inside the ShortcutSensei tool window.
 *
 * Layout:
 * ┌─────────────────────────────────────┐
 * │  Progress bar  (correct / total)    │
 * │─────────────────────────────────────│
 * │  Action prompt                      │
 * │  (what shortcut does this?)         │
 * │─────────────────────────────────────│
 * │  [  Reveal  ]  [  Skip  ]          │
 * │─────────────────────────────────────│
 * │  Revealed shortcut (hidden by default) │
 * │─────────────────────────────────────│
 * │  [ Start / Restart Session ]        │
 * └─────────────────────────────────────┘
 *
 * Key input is captured via a [KeyboardFocusManager] dispatcher so the panel
 * receives shortcut key events even when another component has focus.
 */
class PracticePanel(private val project: Project) : JBPanel<PracticePanel>(BorderLayout()) {

    private val sessionManagerService: SessionManagerService =
        project.getService(SessionManagerService::class.java)
    private val manager: SessionManager = sessionManagerService.sessionManager

    // ── UI components ─────────────────────────────────────────────────────────

    private val progressLabel = JBLabel("Press 'Start' to begin").apply {
        font = font.deriveFont(Font.PLAIN, 12f)
        horizontalAlignment = SwingConstants.CENTER
    }
    private val progressBar = JProgressBar(0, 100).apply {
        isStringPainted = true
        string = ""
    }

    private val actionLabel = JBLabel("–").apply {
        font = font.deriveFont(Font.BOLD, 20f)
        horizontalAlignment = SwingConstants.CENTER
        border = JBUI.Borders.empty(20, 10)
    }
    private val descriptionLabel = JBLabel(" ").apply {
        font = font.deriveFont(Font.ITALIC, 13f)
        horizontalAlignment = SwingConstants.CENTER
        foreground = JBColor.GRAY
        border = JBUI.Borders.empty(0, 10, 10, 10)
    }

    private val revealButton = JButton("👁  Reveal Shortcut").apply {
        toolTipText = "Show the correct shortcut. If you then press it, it won't count — you'll retry it later."
        isEnabled = false
        addActionListener { onReveal() }
    }

    private val revealedShortcutLabel = JBLabel(" ").apply {
        font = font.deriveFont(Font.BOLD, 18f)
        foreground = JBColor(0x0067C0, 0x589DF6)
        horizontalAlignment = SwingConstants.CENTER
        isVisible = false
    }

    private val startButton = JButton("▶  Start Session").apply {
        addActionListener { onStartSession() }
    }

    private val feedbackLabel = JBLabel(" ").apply {
        font = font.deriveFont(Font.BOLD, 14f)
        horizontalAlignment = SwingConstants.CENTER
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        border = JBUI.Borders.empty(8)
        buildLayout()
        registerKeyDispatcher()
        registerSessionListener()
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private fun buildLayout() {
        val top = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            border = JBUI.Borders.emptyBottom(8)
            val gc = GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                gridx = 0
            }
            gc.gridy = 0; add(progressLabel, gc)
            gc.gridy = 1; add(progressBar, gc)
        }

        val center = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            val gc = GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                gridx = 0
            }
            gc.gridy = 0; add(actionLabel, gc)
            gc.gridy = 1; add(descriptionLabel, gc)
            gc.gridy = 2; add(feedbackLabel, gc)
            gc.gridy = 3; add(revealedShortcutLabel, gc)

            val btnPanel = JBPanel<JBPanel<*>>().apply {
                add(revealButton)
            }
            gc.gridy = 4; add(btnPanel, gc)
        }

        val bottom = JBPanel<JBPanel<*>>().apply {
            add(startButton)
        }

        add(top, BorderLayout.NORTH)
        add(JBScrollPane(center), BorderLayout.CENTER)
        add(bottom, BorderLayout.SOUTH)
    }

    // ── Session listener ──────────────────────────────────────────────────────

    private fun registerSessionListener() {
        manager.addListener(object : SessionListener {
            override fun onSessionStarted(session: Session) = SwingUtilities.invokeLater {
                startButton.text = "↺  Restart Session"
                revealButton.isEnabled = true
                updateProgress(session)
                showShortcut(session.currentShortcut)
                showFeedback("", JBColor.GRAY)
            }

            override fun onShortcutChanged(session: Session) = SwingUtilities.invokeLater {
                updateProgress(session)
                showShortcut(session.currentShortcut)
                revealedShortcutLabel.isVisible = false
                revealedShortcutLabel.text = " "
            }

            override fun onShortcutRevealed(session: Session) = SwingUtilities.invokeLater {
                val shortcut = session.currentShortcut ?: return@invokeLater
                revealedShortcutLabel.text = ShortcutVerifier.getFullDisplayString(shortcut)
                revealedShortcutLabel.isVisible = true
                showFeedback("Revealed — press the shortcut to continue (won't count as correct)", JBColor(0xB07000, 0xFFCC44))
            }

            override fun onRevealedShortcutRetried(session: Session) = SwingUtilities.invokeLater {
                showFeedback("Noted — this shortcut goes to the back of the queue", JBColor.GRAY)
                revealedShortcutLabel.isVisible = false
                updateProgress(session)
                showShortcut(session.currentShortcut)
            }

            override fun onProgressUpdated(session: Session) = SwingUtilities.invokeLater {
                updateProgress(session)
                showFeedback("✓ Correct!", JBColor(0x007A00, 0x59C259))
            }

            override fun onSessionCompleted(session: Session) = SwingUtilities.invokeLater {
                progressBar.value = 100
                progressBar.string = "Session complete!"
                actionLabel.text = "🎉 All shortcuts done!"
                descriptionLabel.text = "Session ${session.id.take(6)} — ${session.correctCount} correct, ${session.revealedCount} revealed"
                revealButton.isEnabled = false
                showFeedback("", JBColor.GRAY)
            }
        })
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun showShortcut(shortcut: Shortcut?) {
        if (shortcut == null) {
            actionLabel.text = "–"
            descriptionLabel.text = " "
            return
        }
        actionLabel.text = shortcut.displayName
        descriptionLabel.text = "<html><center>${shortcut.description}</center></html>"

        if (!shortcut.canDemonstrate) {
            showFeedback(
                "⚠ Cannot demonstrate: ${shortcut.demonstrationNote}",
                JBColor(0xB07000, 0xFFCC44),
            )
        } else {
            showFeedback("", JBColor.GRAY)
        }
    }

    private fun updateProgress(session: Session) {
        val done = session.totalShortcuts - session.remainingCount
        val pct = if (session.totalShortcuts > 0) done * 100 / session.totalShortcuts else 0
        progressBar.value = pct
        progressBar.string = "${session.correctCount} correct   |   ${session.remainingCount} remaining"
        progressLabel.text = "Session progress — ${done} / ${session.totalShortcuts}"
    }

    private fun showFeedback(text: String, color: java.awt.Color) {
        feedbackLabel.text = text
        feedbackLabel.foreground = color
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private fun onStartSession() {
        manager.startSession()
    }

    private fun onReveal() {
        manager.onReveal()
    }

    // ── Key capture ───────────────────────────────────────────────────────────

    /**
     * Installs a global key dispatcher that intercepts KEY_PRESSED events and
     * checks them against the current shortcut.
     *
     * Using a global dispatcher (rather than a key binding on this panel) means
     * the user does not need to click on the panel first — they can stay in the
     * editor and practice naturally.
     */
    private fun registerKeyDispatcher() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher { event ->
                if (event.id != KeyEvent.KEY_PRESSED) return@addKeyEventDispatcher false
                val session = manager.getCurrentSession() ?: return@addKeyEventDispatcher false
                if (!session.isActive) return@addKeyEventDispatcher false
                val shortcut = session.currentShortcut ?: return@addKeyEventDispatcher false

                if (ShortcutVerifier.matches(shortcut, event)) {
                    manager.onCorrectInput()
                    return@addKeyEventDispatcher true // consume the event
                }
                false
            }
    }
}
