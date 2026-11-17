---
trigger: always_on
description: "Security and secrets handling rules."
---

# Security and secrets (always loaded)

- Never commit or print: `local.properties`, `secrets.properties`, anything under `secrets/`, keystores, `google-services.json` contents, API keys, tokens, Stripe keys.
- `secrets/` holds the real files of every environment. Do not read or edit it, and do not run `scripts/use-env.sh` or `scripts/use-env.bat` unless the user asks: they overwrite `secrets.properties` and `app/google-services.json`.
- Secrets reach code only from `secrets.properties` (git-ignored) through the secrets Gradle plugin and `BuildConfig`, never as string literals or hand-written `buildConfigField` values.
- CI secrets live in GitHub Environments (`develop`, `staging`, `production`). Workflows read them as `secrets.<NAME>` and must never echo their values.
- Do not log auth tokens, full request/response bodies with credentials, or personal data. OkHttp logging interceptor stays at `BASIC` or lower in release.
- When a task needs a secret value, ask the user to add it to `secrets/<environment>/secrets.properties` and run the setup script again.
- Review generated diffs for accidentally included `.idea/`, `build/` or properties files before suggesting a commit.
