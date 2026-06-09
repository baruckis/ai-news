# Contributing to AI News

Thanks for your interest in the project! This is a public portfolio project built in the open,
one reviewed stage at a time.

## Workflow

- **One change = one pull request.** Keep PRs focused and small.
- Branch off `main` (e.g. `feat/news-list`, `chore/ci-tweak`).
- A pull request must have a green CI run before it can be merged.
- A [Greptile](https://www.greptile.com/) review (score 1–5) runs on each PR; address clear,
  correct findings before merge. The score is an aid, not the sole merge gate — human review
  decides.

## Commit messages

This project uses [Conventional Commits](https://www.conventionalcommits.org/):

```
feat:  a new feature
fix:   a bug fix
chore: tooling / housekeeping
build: build system or dependencies
docs:  documentation only
test:  adding or fixing tests
perf:  performance improvement
```

Scope is encouraged, e.g. `feat(news): add list screen`.

## Code style & quality

- Kotlin official code style, enforced by **ktlint** and **detekt**.
- Every public class, interface, object and public function needs a short KDoc
  (enforced by detekt's documentation rules).
- New logic must be covered by tests; the coverage gate (Kover) requires **80%**.

Run all gates locally before pushing:

```bash
./gradlew build detekt ktlintCheck lint test koverXmlReport koverVerify
```

Optional: install the [lefthook](https://github.com/evilmartians/lefthook) pre-commit hook to run
ktlint automatically:

```bash
lefthook install
```

## Secrets

Never commit secrets. `local.properties` and `.env*` are git-ignored; use the provided
`local.properties.example` as a template.

## Module boundaries

Respect the module dependency rules: `:app → :feature:* → :core:*`, `:core:model` has no
dependencies, and domain code never depends on networking or UI frameworks.
