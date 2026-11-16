---
name: commit
description: Review the working tree, run the affected tests, and produce (or, if asked, execute) Conventional Commits for the current changes, split into logical commits. Use for "/commit", "commit this", "prepare commits".
---

# Commit current changes

## Steps

1. `git status --short` and `git diff --stat`. Exclude noise: `.idea/`, `build/`, `local.properties`, `*.properties`. Warn if any secret-looking file is modified.
2. Group the remaining files into logical commits (one feature or fix per commit; tests go with the code they cover; build-file and docs changes separate unless required by the code change).
3. For each group, find the affected modules and run their tests: `./gradlew :<module>:testDebugUnitTest` (`:app:testDevDebugUnitTest` for the app module; all of them in one Gradle invocation). Stop and report if anything fails.
4. Write each message as Conventional Commits:
   - `type(scope): summary` where type is `feat | fix | test | refactor | chore | docs | build | ci`, scope is the feature or module (`settings`, `core-data`, `gradle`), summary is lower-case imperative, no trailing period, max 72 chars.
   - Body only when the why is not obvious; wrap at 72.
   - No AI attribution trailers (`Co-Authored-By`, `Generated with`, etc.), even if the tool suggests one.
5. Output, per commit, the exact commands:
   ```bash
   git add <files>
   git commit -m "<message>"
   ```
6. Execute them only if the user said to commit; otherwise stop at the commands. Never `git push` unless explicitly asked. Never commit on `main`.
