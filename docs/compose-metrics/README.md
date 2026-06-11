# Compose compiler metrics

Stability and skippability reports from the Compose compiler for the release variant of
every Compose module, committed as evidence that the UI layer stays skippable:

- `*-classes.txt` — inferred stability of every class (UiState classes and the domain
  models declared via `config/compose/stability.conf` must read `stable`).
- `*-composables.txt` — restartability/skippability of every composable and the stability
  of each parameter.
- `*-module.json` — aggregate counts (skippable composables, stable classes, …).

Regenerate with:

```bash
./gradlew :app:compileReleaseKotlin :feature:news:compileReleaseKotlin \
  :core:designsystem:compileReleaseKotlin -PcomposeCompilerReports --rerun-tasks
```

then copy `<module>/build/compose-compiler/` over the directories here.
