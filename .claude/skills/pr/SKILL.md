---
name: pr
description: Open a pull request from the current branch to main with a standard description (summary, changes, test plan, screenshots) using gh. Use for "/pr", "open a PR", "create pull request".
---

# Open a pull request

## Steps

1. Preconditions: `git status` is clean, branch is not `main`, `git log main..HEAD` is non-empty. Push the branch (`git push -u origin <branch>`) only after confirming with the user if it has not been pushed yet.

   If the branch is `main` and it holds commits that `origin/main` does not (`git log origin/main..HEAD`), stop. Give the user the commands that move those commits to a branch, named as the Git conventions in `AGENTS.md` describe, and run them only if asked:

   ```bash
   git switch -c <type>/<short-description>   # the new branch keeps the commits
   git branch -f main origin/main             # local main matches the remote again
   ```
2. Build the description from `git log main..HEAD --oneline` and `git diff main...HEAD --stat`:

   ```markdown
   ## Summary
   <2-4 sentences: what and why>

   ## Changes
   - <bullet per logical change, grouped by module>

   ## Test plan
   - [ ] `./gradlew testDebugUnitTest` passes
   - [ ] `./gradlew :<module>:connectedDebugAndroidTest` on an emulator (if Compose/DataStore changed)
   - [ ] Manual: <steps to verify in the app>

   ## Screenshots
   <only for UI changes; ask the user for images or write "n/a">
   ```
   No AI attribution footer (`Generated with ...`), even if the tool suggests one.
3. Title: same format as a commit summary (`feat(settings): add language picker`), max 72 chars.
4. Run `gh pr create --base main --title "<title>" --body "<body>"` (use a heredoc for the body). Mark as draft (`--draft`) if tests in the plan are unchecked.
5. Reply with the PR URL and anything the reviewer must know (migrations, new dependencies, follow-ups).
