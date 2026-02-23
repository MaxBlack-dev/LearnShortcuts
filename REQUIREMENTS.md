# LearnShortcuts — Project Requirements

> This document captures the original product requirements as stated on **22 February 2026**,
> the first day of the project. It is preserved here so we can always trace back to the
> founding vision.

---

## 1. Overview

**LearnShortcuts** is a free IntelliJ IDEA plugin that teaches users all IntelliJ IDEA
keyboard shortcuts through interactive, context-aware practice sessions.

---

## 2. Platform & Distribution

| Requirement | Detail |
|---|---|
| **Target IDEs** | IntelliJ IDEA (Community & Ultimate) and all JetBrains IDEs built on IntelliJ Platform |
| **Operating Systems** | macOS, Linux, Windows, Chrome OS (via Linux container / Crostini) |
| **Price** | Free |
| **Distribution** | JetBrains Plugin Marketplace (must be published and approved there) |
| **Installation** | Standard IDE plugin installation — no extra steps required |

---

## 3. Shortcut Source — Dynamic Keymap Extraction

- The plugin must read shortcuts directly from the **currently active IntelliJ IDEA keymap**
  at runtime.
- Shortcuts must NOT be hardcoded. A user on macOS sees the macOS bindings;
  a user on Windows sees the Windows bindings; a user with a custom keymap sees their
  custom bindings.
- This ensures the plugin works with any keymap (default, custom, or third-party).

---

## 4. Practice Mechanics

### 4.1 Core Loop

1. The plugin presents the user with a **shortcut action name** (e.g. "Close this tab").
2. The user presses the corresponding key combination.
3. If **correct** → correct counter increases, remaining counter decreases, next shortcut appears.
4. If **wrong** → nothing changes; the user can try again or press Reveal.

### 4.2 Reveal Button

- A **"Reveal" button** is always visible during practice.
- If the user presses Reveal:
  - The correct shortcut is shown on screen.
  - The `revealed` counter is incremented.
  - If the user then presses the correct shortcut, it is **NOT counted as correct**.
  - The shortcut is appended to the **end of the current queue** (the user will see it again).
  - The practice session advances to the next shortcut.
- Rationale: Peeking means you don't get credit — same as flashcard rules.

### 4.3 Sessions

- A **session** covers the full set of currently enabled shortcuts (one full pass).
- When a session is completed (all shortcuts answered without peeking), the
  **"sessions completed"** counter is incremented and a new session starts automatically.
- A new session can also be started manually from the UI.

---

## 5. Practice Context

- Every shortcut must be demonstrated **in context** — the user should be able to see
  what the shortcut does, not just memorise key labels.
  - Example: "Close Tab" → at least one tab should be open so the user can see it close.
  - Example: "Extend Selection" → a file is open, the caret is on a word.
- The plugin maintains a **separate practice project** (in a temp directory) so users
  never accidentally break their real project.
- Practice files contain carefully crafted code that provides the right context for each shortcut.

---

## 6. Grouped Shortcuts

- Some shortcuts are **logically grouped** and must be practised in sequence.
- Example — "Extend / Shrink Selection" group:
  1. Extend Selection → word
  2. Extend Selection → expression
  3. Extend Selection → statement/block
  4. Shrink Selection (one level)
  5. Shrink Selection (back to word)
- These five shortcuts must appear **back-to-back** as a unit; they cannot be separated
  by the queue ordering.
- The plugin ships with pre-defined groups. The grouping logic is part of the shortcut
  catalogue and is chosen by the development team based on logical coherence.

---

## 7. Smart Code Completion Variants

- Smart code completion (`EditorCompleteStatement`) produces **different results** depending
  on the code context:
  - In a statement missing a semicolon → inserts `;`
  - In an unclosed method call → closes the parenthesis `)`
  - In a method body missing a return → inserts `return …;`
- Each variant must be treated as a **separate shortcut entry** with its own practice context.
- The user must learn all variants individually.

---

## 8. Shortcuts That Cannot Be Demonstrated

Some shortcuts fundamentally cannot be demonstrated interactively:

| Shortcut | Reason |
|---|---|
| Toggle Full Screen | Hides the LearnShortcuts panel |
| Distraction Free Mode | Hides all UI including the practice window |
| Presentation Mode | Changes UI in a way that breaks the session |
| Exit (Close IDE) | Terminates the IDE and the practice session |
| Invalidate Caches and Restart | Restarts the IDE |
| Start Macro Recording | Interferes with the plugin's own key-capture mechanism |
| Run with Coverage | Requires a JVM coverage agent — cannot be set up in the practice sandbox |
| Debug step commands (Step Over, Step Into, Step Out, Resume, Evaluate Expression, Run to Cursor) | Require an active debug session paused at a breakpoint |

> These shortcuts are still included in the practice queue. Instead of a live demo, the
> plugin shows a clear explanation of what the shortcut does and why it cannot be
> demonstrated interactively.

---

## 9. Statistics

The Statistics panel must show:

| Metric | Description |
|---|---|
| Total shortcuts in catalogue | Varies per OS / keymap |
| Sessions completed | How many full passes the user has done |
| All-time correct | Total correct presses across all sessions |
| All-time revealed | Total Reveal button presses across all sessions |
| Current session correct | Correct presses in the ongoing session |
| Current session remaining | Shortcuts left in the current session queue |
| Hardest shortcuts | Top 10 shortcuts by reveal count (most-peeked = hardest) |

- Statistics must persist across IDE restarts.

---

## 10. Settings

The Settings panel (under Settings → Tools → LearnShortcuts) must expose:

| Setting | Options / Default |
|---|---|
| Practice order | Popularity (default), Random, By Category |
| Auto-reveal delay | 0 – 120 seconds (0 = never auto-reveal, default) |
| Include non-demonstrable shortcuts | On (default) |
| Sound feedback on correct input | Off (default) |

### 10.1 Practice Order — Popularity

- "Popularity" order is based on JetBrains developer surveys and community data.
- The most commonly used shortcuts appear first so beginners get the most value
  from their first session.

---

## 11. Non-Functional Requirements

| Requirement | Detail |
|---|---|
| Performance | Plugin startup must not delay IDE startup by more than 500 ms |
| Memory | Must not hold large data structures in memory between sessions |
| Compatibility | Must work from IntelliJ IDEA 2023.3 (build 233) onwards |
| Signing | Plugin must be signed with JetBrains-approved certificate before publishing |
| Open source | Repository hosted on GitHub (MaxBlack-dev / LearnShortcuts) |

---

## 12. Optional / Stretch Goals

The following were raised as ideas and are not blocking for v1.0:

- **Multi-agent development workflow** — separate AI agents for logic development,
  testing, catalogue curation, and review.
- **Leaderboard / gamification** — score sharing between team members.
- **Kotlin support** — Kotlin-specific practice files alongside Java ones.
- **Custom shortcut groups** — let users define their own grouped sequences.
- **IDE-wide integration** — intercept real actions the user performs and give
  silent credit when a shortcut is used naturally in their own project.

---

*Document created: 22 February 2026*
*Author: Max (LearnShortcuts project founder) + GitHub Copilot*
