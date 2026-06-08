<!-- Conventional commit style title, e.g. "feat(news): article detail screen". -->

## What

<!-- Describe what this PR changes and why. -->

## How to verify

```bash
./gradlew build detekt ktlintCheck lint test koverXmlReport koverVerify
```

<!-- Add any manual steps (e.g. run the app, open a screen). -->

## Checklist

- [ ] `./gradlew build detekt ktlintCheck lint test koverVerify` passes locally
- [ ] New public classes/functions have a short KDoc
- [ ] New logic is covered by tests
- [ ] No secrets committed (`local.properties`, `.env` stay out of git)
- [ ] Module dependency rules respected (domain depends only on `:core:model`)
- [ ] CI is green

## Review

- [ ] **Greptile review** is expected on this PR (score 1–5); address clear, correct findings before merge.
- [ ] Human review and approval before merge — do not merge on the score alone.
