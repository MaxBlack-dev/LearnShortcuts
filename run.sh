#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# run.sh  —  Launch a sandbox IntelliJ IDEA with the LearnShortcuts plugin loaded
#
# Usage:
#   ./run.sh           # build + launch (incremental, fast after first run)
#   ./run.sh --clean   # clean build before launching
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [[ "${1:-}" == "--clean" ]]; then
    echo "🧹  Cleaning build output..."
    ./gradlew clean
fi

echo "🔨  Building plugin..."
./gradlew assemble --quiet

echo "🚀  Launching sandbox IntelliJ IDEA with LearnShortcuts..."
echo "    (The IDE window will open shortly — this may take 30–60 seconds on first run)"
echo "    Log: /tmp/learnshortcuts-runIde.log"
echo ""
echo "    Once IDEA opens:"
echo "      1. Open or create any project"
echo "      2. View → Tool Windows → LearnShortcuts  (or look in the right sidebar)"
echo "      3. Click 'Start Session' and start learning!"
echo ""

./gradlew runIde 2>&1 | tee /tmp/learnshortcuts-runIde.log
