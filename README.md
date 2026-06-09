# AI News

[![CI](https://github.com/baruckis/ai-news/actions/workflows/ci.yml/badge.svg)](https://github.com/baruckis/ai-news/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/baruckis/ai-news/branch/main/graph/badge.svg)](https://codecov.io/gh/baruckis/ai-news)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 🚧 **Work in progress.** An open-source Android app that shows real AI news: list → tap → detail.

A small, deliberately polished demo of a modern Android engineering stack — built in public,
stage by stage, each stage reviewed before merge.

## Tech stack

- **Kotlin** (K2), **Jetpack Compose**, **Navigation 3**
- **Clean Architecture** + best-practice **MVI** (UiState / Intent / Effect)
- **Multi-module** by feature
- **Apollo Kotlin 5** (GraphQL client) with normalized cache
- **Hilt** for dependency injection
- A **Kotlin BFF** (Ktor + graphql-kotlin) that hides the news-provider API key
- Custom **design system** (light + dark), **Coil** for images
- Tests: JUnit5, MockK, Turbine, Robolectric + Roborazzi, apollo-mockserver
- CI: GitHub Actions with quality (detekt, ktlint, Android Lint) and coverage (Kover) gates

## Module structure

```
:app                  Application, MainActivity, DI wiring (entry points only)
:core:model           pure-Kotlin domain models (no dependencies)
:core:mvi             MVI base (UiState / Intent / Effect / MviViewModel)
:core:network         Apollo GraphQL layer
:core:designsystem    theme, tokens, components
:core:testing         shared test helpers and fakes
:feature:news         news list + detail (data / domain / ui)
```

Dependency rule: `:app → :feature:* → :core:*`; `:core:model` depends on nothing, and domain code
never depends on networking or UI frameworks.

## Building

Requires JDK 21 and the Android SDK (compileSdk 36).

```bash
# Copy the local config template and adjust if needed.
cp local.properties.example local.properties

# Assemble the debug app.
./gradlew :app:assembleDebug

# Run all quality and coverage gates (the same set CI runs).
./gradlew build detekt ktlintCheck lint test koverXmlReport koverVerify
```

`GRAPHQL_URL` is read from `local.properties` and exposed via `BuildConfig` — it is never
hard-coded or committed. The emulator-localhost default (`http://10.0.2.2:8080/graphql`) points at
the local BFF that arrives in a later stage.

## Status — planned stages

Built in numbered stages; each stage is a single reviewed pull request.

- [x] **Stage 0** — Repo bootstrap, multi-module Gradle skeleton, CI with quality + coverage gates
- [ ] **Stage 1** — Design system (light/dark theme, tokens, components)
- [ ] **Stage 2** — Kotlin BFF (Ktor + graphql-kotlin) serving real AI news
- [ ] **Stage 3** — BFF deployment
- [ ] **Stage 4** — `:core:network` (Apollo Kotlin 5, normalized cache)
- [ ] **Stage 5** — `:core:mvi` + `:core:model`
- [ ] **Stage 6** — `:feature:news` data + domain
- [ ] **Stage 7** — News list screen
- [ ] **Stage 8** — Article detail + Navigation 3
- [ ] **Stage 9** — Polish, adaptive layout, accessibility

This README will grow with architecture diagrams, screenshots and setup instructions as the
stages land.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Conventional commits, Kotlin official style, and tests for
new logic are expected.

## License

[MIT](./LICENSE) © 2026 Andrius Baruckis
