# AI News — contributor guide for AI agents

This project is built in numbered stages. Each stage = one branch off `main` = one Pull Request =
one human review. Never start the next stage until the current PR is merged. The detailed plan lives
in `_planning/` — it is personal and gitignored; never commit it or reference it in public files.

## Workflow

- Start each stage: `git checkout main && git pull`, then create a `feat/stage-N-*` (or
  `chore/…` / `build/…`) branch.
- One PR per stage. Use conventional commits (`feat:`, `fix:`, `chore:`, `docs:`, `build:`, `test:`).
- Author every commit as **Andrius Baruckis**. Do NOT add "Co-authored-by", "Generated with", or any
  AI/tool signature to commits or PR descriptions.
- Stay strictly within the stage's scope. Capture extra ideas under "Future work" in the PR; don't build them.
- When a stage finishes: open the PR, list how to verify it, then STOP for human review. Do not merge.

## Quality gates (green before opening a PR)

- `./gradlew build detekt ktlintCheck lint test koverXmlReport koverVerify` must pass locally and in CI.
- KDoc on every public class / interface / object / function (detekt enforces it).
- Module rules: `:app → :feature:* → :core:*`; `:core:model` depends on nothing; domain code never
  depends on Apollo or Compose. The `:bff` module is a standalone server, separate from the Android build.

## Secrets

- Never commit secrets. API keys live only in gitignored `.env` / `local.properties` files.
- Committed `*.example` files contain placeholders only — never a real key.
- Read keys from the environment at runtime; never hard-code them or paste them into PR descriptions.

## Review loop

- A Greptile bot reviews each PR (1–5). Fix clear, correct comments (security, bugs, edge cases);
  leave architectural decisions to the human. Max ~2 rounds. Never loop just to chase a score.
