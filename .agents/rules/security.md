---
trigger: always_on
description: "Security and secrets handling rules."
---

# Security and secrets (always loaded)

- Never commit or print: `local.properties`, `debug.properties`, `release.properties`, keystores, `google-services.json` contents, API keys, tokens, Stripe keys.
- Secrets reach code only through the secrets Gradle plugin / `BuildConfig`, never as string literals.
- Do not log auth tokens, full request/response bodies with credentials, or personal data. OkHttp logging interceptor stays at `BASIC` or lower in release.
- When a task needs a secret value, ask the user to add it to the ignored properties file; do not invent placeholder keys in committed files.
- Review generated diffs for accidentally included `.idea/`, `build/` or properties files before suggesting a commit.

