package dev.maxblack.learnshortcuts

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.maxblack.learnshortcuts.core.engine.SessionManagerService

/**
 * Plugin entry-point: runs once after the IDE finishes loading a project.
 *
 * Responsibilities:
 * - Initialise the shortcut registry (loads from keymap + built-in catalogue)
 * - Wire up the session manager for this project
 */
class LearnShortcutsPlugin : ProjectActivity {

    private val log = Logger.getInstance(LearnShortcutsPlugin::class.java)

    override suspend fun execute(project: Project) {
        log.info("LearnShortcuts: initialising for project '${project.name}'")
        val service = project.getService(SessionManagerService::class.java)
        service.initialize()
        log.info("LearnShortcuts: ready — ${service.totalShortcutCount()} shortcuts available")
    }
}
