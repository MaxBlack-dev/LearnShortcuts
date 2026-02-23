package dev.maxblack.learnshortcuts.core.engine

import dev.maxblack.learnshortcuts.core.catalog.BuiltinShortcutDefinitions
import dev.maxblack.learnshortcuts.core.catalog.ShortcutRegistry
import dev.maxblack.learnshortcuts.core.model.ContextType
import dev.maxblack.learnshortcuts.core.model.Shortcut
import dev.maxblack.learnshortcuts.core.model.ShortcutCategory
import dev.maxblack.learnshortcuts.core.state.PluginState
import dev.maxblack.learnshortcuts.core.state.PracticeOrder
import dev.maxblack.learnshortcuts.core.state.StatisticsState
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SessionManagerTest {

    private lateinit var registry: ShortcutRegistry
    private lateinit var settings: PluginState
    private lateinit var statistics: StatisticsState
    private lateinit var manager: SessionManager

    private val sampleShortcuts = listOf(
        Shortcut("s1", "Action1", "Action One", "Desc 1", ShortcutCategory.EDITING, 1, ContextType.ANY),
        Shortcut("s2", "Action2", "Action Two", "Desc 2", ShortcutCategory.NAVIGATION, 2, ContextType.ANY),
        Shortcut("s3", "Action3", "Action Three", "Desc 3", ShortcutCategory.REFACTORING, 3, ContextType.ANY),
    )

    @BeforeEach
    fun setUp() {
        registry  = mockk(relaxed = true)
        settings  = mockk(relaxed = true)
        statistics = mockk(relaxed = true)

        every { registry.getAllShortcuts() } returns sampleShortcuts
        every { settings.practiceOrder } returns PracticeOrder.POPULARITY

        manager = SessionManager(registry, settings, statistics)
    }

    @Test
    fun `startSession creates session with correct total count`() {
        val session = manager.startSession()
        assertEquals(3, session.totalShortcuts)
        assertEquals(3, session.remainingCount)
        assertNotNull(session.currentShortcut)
    }

    @Test
    fun `onCorrectInput decrements remaining and increments correct`() {
        val session = manager.startSession()
        val initialRemaining = session.remainingCount
        manager.onCorrectInput()
        assertEquals(initialRemaining - 1, session.remainingCount)
        assertEquals(1, session.correctCount)
    }

    @Test
    fun `reveal then correct does not count as correct`() {
        val session = manager.startSession()
        manager.onReveal()
        assertTrue(session.isCurrentRevealed)

        val correctBefore = session.correctCount
        manager.onCorrectInput()

        assertEquals(correctBefore, session.correctCount, "Correct count must NOT increase after reveal")
        verify { statistics.recordReveal(any()) }
        verify(exactly = 0) { statistics.recordCorrect(any()) }
    }

    @Test
    fun `reveal sends shortcut to back of queue`() {
        val session = manager.startSession()
        val firstShortcut = session.currentShortcut
        manager.onReveal()
        manager.onCorrectInput() // advances past, shortcut goes to back

        // The revealed shortcut should reappear at the end
        val queueContents = session.queue.toList() + listOfNotNull(session.currentShortcut)
        assertTrue(queueContents.contains(firstShortcut))
    }

    @Test
    fun `session completes after all shortcuts answered correctly`() {
        val listener = mockk<SessionListener>(relaxed = true)
        manager.addListener(listener)

        manager.startSession()
        repeat(3) { manager.onCorrectInput() }

        verify { listener.onSessionCompleted(any()) }
    }

    @Test
    fun `listener is notified on reveal`() {
        val listener = mockk<SessionListener>(relaxed = true)
        manager.addListener(listener)

        manager.startSession()
        manager.onReveal()

        verify { listener.onShortcutRevealed(any()) }
    }

    @Test
    fun `statistics recordCorrect called on genuine correct input`() {
        manager.startSession()
        manager.onCorrectInput()
        verify { statistics.recordCorrect(any()) }
    }

    @Test
    fun `statistics completeSession called when all shortcuts done`() {
        manager.startSession()
        repeat(3) { manager.onCorrectInput() }
        verify { statistics.completeSession() }
    }
}
