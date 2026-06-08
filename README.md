# AI News

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

## Status

Bootstrapping. The app is being built in numbered stages — see the project board and pull
request history. This README will grow with architecture diagrams, screenshots and setup
instructions as the stages land.

## License

[MIT](./LICENSE) © 2026 Andrius Baruckis
