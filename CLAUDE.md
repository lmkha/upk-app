@AGENTS.md

# Claude Code specific notes

- Path-scoped rules live in `.claude/rules/` and load automatically; other tools must read them explicitly.
- Skills live in `.claude/skills/<name>/SKILL.md` and are invoked with `/<name>`.
