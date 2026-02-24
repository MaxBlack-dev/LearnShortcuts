# LearnShortcuts

> *Sensei* (先生) — a teacher, a master of their craft.

**LearnShortcuts** is a free IntelliJ IDEA plugin that teaches you every keyboard shortcut
through interactive, context-aware practice sessions. Instead of reading a cheat sheet,
you *do* the shortcuts — in a safe practice environment where you can see exactly what
each one does.

---

## How it works

1. Open the **LearnShortcuts** tool window (right sidebar, or Tools → Open LearnShortcuts).
2. Press **▶ Start Session**.
3. The plugin shows you an action name (e.g. *"Duplicate Line or Selection"*).
4. Press the correct shortcut on your keyboard.
5. If you're stuck, press **👁 Reveal Shortcut** — the answer is shown, but you won't get
   credit and the shortcut goes to the back of the queue for another try later.
6. When you've answered all shortcuts, a new session begins. The **Sessions Completed**
   counter goes up.

### Key features

| Feature | Detail |
|---|---|
| 🎯 **Context-aware practice** | Every shortcut is shown in context — a relevant file is open so you see what the shortcut actually does |
| 📊 **Statistics** | Track correct answers, reveal count, sessions completed, and your hardest shortcuts |
| 🔀 **Flexible ordering** | Practice in popularity order (most-used first), random, or by category |
| 🔗 **Grouped sequences** | Related shortcuts are practised back-to-back (e.g. Extend → Shrink Selection) |
| 👁 **Reveal without penalty** | Peek the answer, retry later — no streak broken |
| 💻 **Cross-platform** | Mac, Linux, Windows, Chrome OS — shortcuts adapt to your active keymap automatically |

---

## Architecture

```
learnshortcuts/
├── core/
│   ├── model/          # Shortcut, ShortcutGroup, Session, Statistics
│   ├── engine/         # SessionManager, ShortcutVerifier, SessionManagerService
│   ├── catalog/        # BuiltinShortcutDefinitions, ShortcutRegistry, ShortcutCatalog
│   └── state/          # PluginState (settings), StatisticsState (persistent stats)
├── ui/
│   ├── toolwindow/     # PracticePanel, StatisticsPanel, ToolWindowFactory
│   ├── settings/       # SettingsConfigurable, SettingsPanel
│   └── actions/        # OpenPracticeAction, ShowStatisticsAction
└── practice/
    ├── context/        # ContextProvider per ContextType
    └── project/        # PracticeProjectGenerator — sandbox code files
```

**Key design decisions:**

- **No hardcoded key bindings.** All shortcut keys are read from the active IntelliJ
  keymap at runtime via `KeymapManager`. This means the plugin works with any OS,
  any custom keymap, and any keymap plugin.

- **Separate practice project.** Practice files live in a temp directory so users never
  accidentally modify their real project.

- **Popularity ranking.** Shortcuts are ordered by how often developers actually use them
  (based on JetBrains productivity data and community surveys), so beginners get the
  highest-value shortcuts first.

---

## Technology stack

| Concern | Choice |
|---|---|
| Language | **Kotlin** (JVM 17) |
| Build | **Gradle 8** with Kotlin DSL + IntelliJ Platform Gradle Plugin v2 |
| Plugin SDK | **IntelliJ Platform** (since build 233 / IDEA 2023.3) |
| Testing | **JUnit 5** + **MockK** |
| Persistence | IntelliJ `PersistentStateComponent` (XML in IDE config dir) |
| UI | IntelliJ Swing UI DSL (no external UI framework) |

---

## Building

```bash
./gradlew buildPlugin          # produces build/distributions/LearnShortcuts-*.zip
./gradlew runIde               # launches a sandbox IDE with the plugin installed
./gradlew test                 # runs unit tests
./gradlew verifyPlugin         # validates plugin.xml and compatibility
```

---

## Publishing

To publish to the JetBrains Plugin Marketplace:

1. Create a JetBrains Marketplace account at https://plugins.jetbrains.com
2. Generate a plugin signing certificate (required since IDEA 2021.2):
   ```bash
   ./gradlew signPlugin
   ```
3. Set environment variables:
   - `CERTIFICATE_CHAIN` — PEM certificate chain
   - `PRIVATE_KEY` — PEM private key
   - `PRIVATE_KEY_PASSWORD` — key passphrase
   - `PUBLISH_TOKEN` — Marketplace API token
4. Run:
   ```bash
   ./gradlew publishPlugin
   ```

---

## Shortcuts that cannot be demonstrated

A handful of shortcuts fundamentally cannot be shown interactively — because
executing them would crash the practice session itself. See [REQUIREMENTS.md](REQUIREMENTS.md#8-shortcuts-that-cannot-be-demonstrated) for the full list and reasoning.

These shortcuts still appear in the queue, but the plugin shows a clear explanation
instead of a live demo.

---

## Contributing

Pull requests welcome. See [REQUIREMENTS.md](REQUIREMENTS.md) for the full product spec.

---

## Origin

This project was conceived on **22 February 2026**.

> *"I'd like you to create a new plugin that would help users to learn all shortcuts.
> It should be able to work on any OS — Mac, Linux, Windows, Chromebook. It should be
> free and easily installed from the IntelliJ IDEA plugin store.*
>
> *The idea is: the plugin shows the user a shortcut action name — for example 'Close this
> tab' — and the user should press it. If the user doesn't know what to press, there should
> be a Reveal button. If the user pressed Reveal and then pressed the correct shortcut, we
> do NOT consider it correct — we go to the next shortcut, and put the current one at the
> end of the queue.*
>
> *We need statistics — total shortcuts, how many times the user walked through all of them,
> correct and remaining for the current session. Settings should let the user choose random
> order or most-used order. All shortcuts should have context — if we ask the user to close
> a tab, there should be an open tab. Some shortcuts should be grouped together (Extend →
> Shrink Selection). Smart code completion should be split into variants.*
>
> *Our ultimate goal is an IntelliJ IDEA plugin accessible from the plugin store."*
>
> — Max, project founder

---

*© 2026 MaxBlack-dev — released under the Apache 2.0 licence (see LICENSE)*

<--- NAS push test: 2026-02-24 02:42:40 UTC -->
