package dev.maxblack.learnshortcuts.practice.project

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import dev.maxblack.learnshortcuts.core.model.ContextType
import dev.maxblack.learnshortcuts.core.model.Shortcut
import java.nio.file.Paths

/**
 * Creates and maintains a small practice project structure inside a temporary
 * directory so that users can practice shortcuts without touching any of their
 * real project files.
 *
 * The practice directory is located at:
 *   <system-temp>/LearnShortcutsPractice/
 *
 * Contents:
 *   src/main/java/practice/
 *     PracticeMain.java        – general editing context
 *     PracticeWithErrors.java  – file with intentional errors for error-nav shortcuts
 *     PracticeSelection.java   – file with pre-built selections for selection shortcuts
 *     PracticeRefactor.java    – file suitable for refactoring shortcuts
 *     PracticeCompletion.java  – file with incomplete statements for completion shortcuts
 *
 * TODO (future iteration):
 *  - Create a proper IntelliJ module so the practice files have full Java/Kotlin support
 *  - Wire up context providers to position the caret precisely per shortcut
 *  - Add Kotlin practice files
 */
object PracticeProjectGenerator {

    private const val PRACTICE_DIR = "LearnShortcutsPractice"

    /**
     * Returns an existing practice file appropriate for [shortcut], creating it
     * (and any parent directories) if it does not yet exist.
     *
     * Returns null if the file system is not writable.
     */
    fun getOrCreatePracticeFile(project: Project, shortcut: Shortcut): VirtualFile? {
        val tempDir = System.getProperty("java.io.tmpdir")
        val practiceRoot = Paths.get(tempDir, PRACTICE_DIR, "src", "main", "java", "practice")
            .toFile()

        return try {
            WriteAction.computeAndWait<VirtualFile?, Exception> {
                practiceRoot.mkdirs()
                val fileName = fileNameFor(shortcut.contextType)
                val file = practiceRoot.resolve(fileName)
                if (!file.exists()) {
                    file.writeText(templateFor(shortcut))
                }
                VfsUtil.findFileByIoFile(file, true)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the ideal caret offset for the given shortcut's demo context.
     * -1 means "leave caret where it is".
     */
    fun getCaretOffsetFor(shortcut: Shortcut): Int {
        // Simple heuristic for now — future versions will be precise per shortcut
        return when (shortcut.contextType) {
            ContextType.EDITOR_WITH_SELECTION -> 120
            ContextType.EDITOR_WITH_ERRORS    -> 200
            else                              -> 80
        }
    }

    // ── Templates ─────────────────────────────────────────────────────────────

    private fun fileNameFor(context: ContextType): String = when (context) {
        ContextType.EDITOR_WITH_ERRORS    -> "PracticeWithErrors.java"
        ContextType.EDITOR_WITH_SELECTION -> "PracticeSelection.java"
        ContextType.EDITOR_MULTIPLE_CARETS -> "PracticeMultiCaret.java"
        ContextType.MULTIPLE_TABS_OPEN    -> "PracticeMain.java"
        else                              -> "PracticeMain.java"
    }

    private fun templateFor(shortcut: Shortcut): String = when {
        shortcut.codeCompletionVariant == "SEMICOLON"  -> TEMPLATE_COMPLETION_SEMICOLON
        shortcut.codeCompletionVariant == "PARENTHESIS" -> TEMPLATE_COMPLETION_PAREN
        shortcut.codeCompletionVariant == "RETURN"     -> TEMPLATE_COMPLETION_RETURN
        shortcut.contextType == ContextType.EDITOR_WITH_ERRORS -> TEMPLATE_WITH_ERRORS
        shortcut.contextType == ContextType.EDITOR_WITH_SELECTION -> TEMPLATE_SELECTION
        else -> TEMPLATE_MAIN
    }

    // ── Code templates ────────────────────────────────────────────────────────

    private val TEMPLATE_MAIN = """
        package practice;

        import java.util.ArrayList;
        import java.util.List;

        /**
         * LearnShortcuts Practice File — General Editing
         * This file exists solely for practicing IntelliJ IDEA shortcuts.
         * Feel free to edit, break, and experiment — it will be regenerated.
         */
        public class PracticeMain {

            private String name;
            private int value;

            public PracticeMain(String name, int value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public int getValue() {
                return value;
            }

            public List<String> getItems() {
                List<String> items = new ArrayList<>();
                items.add("alpha");
                items.add("beta");
                items.add("gamma");
                return items;
            }

            public static void main(String[] args) {
                PracticeMain practice = new PracticeMain("LearnShortcuts", 42);
                System.out.println("Name: " + practice.getName());
                System.out.println("Value: " + practice.getValue());
                for (String item : practice.getItems()) {
                    System.out.println("Item: " + item);
                }
            }
        }
    """.trimIndent()

    private val TEMPLATE_WITH_ERRORS = """
        package practice;

        /**
         * LearnShortcuts Practice File — Error Navigation
         * This file intentionally contains errors for practicing error-navigation shortcuts.
         */
        public class PracticeWithErrors {

            public void methodWithErrors() {
                // Error 1: missing semicolon
                String missing = "hello"
                // Error 2: undefined variable
                System.out.println(undefinedVariable);
                // Error 3: wrong type
                int wrongType = "this should be a string";
                // Error 4: unclosed block
                if (true) {
                    System.out.println("unclosed")
            }

            public String anotherMethod() {
                return 42; // Error: wrong return type
            }
        }
    """.trimIndent()

    private val TEMPLATE_SELECTION = """
        package practice;

        import java.util.stream.Collectors;
        import java.util.List;
        import java.util.Arrays;

        /**
         * LearnShortcuts Practice File — Selection & Multiple Cursors
         * Use this file to practice Extend/Shrink Selection and Multi-Caret shortcuts.
         */
        public class PracticeSelection {

            private static final String ALPHA = "alpha";
            private static final String BETA = "beta";
            private static final String GAMMA = "gamma";

            public void demonstrateSelection() {
                // Place caret on a word and use Extend Selection (W/Ctrl+W)
                String selectedWord = "extend me to expression and then to statement";
                List<String> items = Arrays.asList(ALPHA, BETA, GAMMA);
                String result = items.stream()
                    .filter(s -> s.startsWith("a"))
                    .collect(Collectors.joining(", "));
                System.out.println(result);
            }

            public void multiCaret() {
                // Place caret on 'name' below and use Add Caret Below / Select All Occurrences
                String name = "sensei";
                String name2 = "shortcut";
                String name3 = "practice";
                System.out.println(name + name2 + name3);
            }
        }
    """.trimIndent()

    private val TEMPLATE_COMPLETION_SEMICOLON = """
        package practice;

        /**
         * LearnShortcuts Practice File — Statement Completion (semicolon variant)
         * Position caret at end of an incomplete statement and press Complete Statement.
         */
        public class PracticeCompletion {

            public void semicolonCompletion() {
                // Position caret at end of next line (after the closing quote) and press Complete Statement
                String message = "Hello, LearnShortcuts"
                int count = 42
                System.out.println(message)
            }
        }
    """.trimIndent()

    private val TEMPLATE_COMPLETION_PAREN = """
        package practice;

        /**
         * LearnShortcuts Practice File — Statement Completion (parenthesis variant)
         * Position caret inside the open method call and press Complete Statement.
         */
        public class PracticeCompletion {

            public void parenCompletion() {
                // Position caret after the argument below and press Complete Statement
                System.out.println("close my parenthesis"
                Math.max(10, 20
                String.valueOf(42
            }
        }
    """.trimIndent()

    private val TEMPLATE_COMPLETION_RETURN = """
        package practice;

        /**
         * LearnShortcuts Practice File — Statement Completion (return variant)
         * Press Complete Statement inside a method body to insert a return statement.
         */
        public class PracticeCompletion {

            public String returnCompletion() {
                // Position caret here and press Complete Statement
                String result = "LearnShortcuts"
            }

            public int anotherReturn() {
                int x = 42
            }
        }
    """.trimIndent()
}
