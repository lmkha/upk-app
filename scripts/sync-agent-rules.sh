#!/usr/bin/env bash
# Mirrors rule bodies from .claude/rules (source of truth) into .agents/rules.
#
# The two folders hold the same text under different frontmatter: Claude Code
# reads `paths:`, Antigravity reads `trigger:` / `globs:` / `description:`.
# The existing frontmatter of each .agents/rules file is kept; only the body is
# replaced. A rule that has no mirror yet gets frontmatter derived from the
# source (`paths:` -> glob trigger, no paths -> always_on).
#
# Usage:
#   scripts/sync-agent-rules.sh          rewrite .agents/rules from .claude/rules
#   scripts/sync-agent-rules.sh --check  exit 1 if anything is out of sync

set -euo pipefail

mode="${1:-}"
if [ -n "$mode" ] && [ "$mode" != "--check" ]; then
    echo "usage: $0 [--check]" >&2
    exit 2
fi

root="$(cd "$(dirname "$0")/.." && pwd)"
src_dir="$root/.claude/rules"
dst_dir="$root/.agents/rules"

# Prints the leading `---` ... `---` block, or nothing when the file has none.
frontmatter() {
    awk '
        NR == 1 { if ($0 != "---") exit; print; next }
        { print }
        $0 == "---" { exit }
    ' "$1"
}

# Prints everything after the frontmatter, without leading blank lines.
body() {
    awk '
        NR == 1 && $0 == "---" { in_frontmatter = 1; next }
        in_frontmatter { if ($0 == "---") in_frontmatter = 0; next }
        !started && $0 ~ /^[[:space:]]*$/ { next }
        { started = 1; print }
    ' "$1"
}

# Builds Antigravity frontmatter for a rule that has no mirror yet.
default_frontmatter() {
    local globs title
    globs="$(frontmatter "$1" | sed -n 's/^[[:space:]]*-[[:space:]]*"\(.*\)"[[:space:]]*$/\1/p' | paste -sd, - | sed 's/,/, /g')"
    title="$(body "$1" | awk '/^# / && !found { sub(/^# /, ""); print; found = 1 }')"
    echo "---"
    if [ -n "$globs" ]; then
        echo "trigger: glob"
        echo "globs: \"$globs\""
    else
        echo "trigger: always_on"
    fi
    echo "description: \"$title\""
    echo "---"
}

status=0
mkdir -p "$dst_dir"

for src in "$src_dir"/*.md; do
    name="$(basename "$src")"
    dst="$dst_dir/$name"

    header=""
    if [ -f "$dst" ]; then
        header="$(frontmatter "$dst")"
    fi
    if [ -z "$header" ]; then
        header="$(default_frontmatter "$src")"
    fi
    expected="$(printf '%s\n\n%s' "$header" "$(body "$src")")"

    if [ "$mode" = "--check" ]; then
        if [ ! -f "$dst" ] || [ "$expected" != "$(cat "$dst")" ]; then
            echo "out of sync: .agents/rules/$name"
            status=1
        fi
    else
        printf '%s\n' "$expected" > "$dst"
    fi
done

for dst in "$dst_dir"/*.md; do
    [ -e "$dst" ] || continue
    name="$(basename "$dst")"
    if [ ! -f "$src_dir/$name" ]; then
        echo "orphan: .agents/rules/$name has no source in .claude/rules"
        status=1
    fi
done

if [ "$status" -ne 0 ]; then
    echo "Edit rules under .claude/rules, then run scripts/sync-agent-rules.sh." >&2
elif [ "$mode" = "--check" ]; then
    echo "Rules are in sync."
fi
exit "$status"
