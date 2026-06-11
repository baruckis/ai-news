# Cutting a release

How to set up release signing once, and how to publish a new version after that.
Releases are fully automated: pushing a `v*` tag builds, signs and publishes the APK
(see [`.github/workflows/release.yml`](../.github/workflows/release.yml)).

> **Never commit the keystore or any password.** `*.jks` / `*.keystore` and
> `local.properties` are gitignored; the CI copies live only in GitHub Actions secrets.

## 1. Generate the release keystore (once)

Keep the keystore **outside** the repository and back it up somewhere safe (a password
manager attachment works). If it is lost, future releases can no longer update existing
installs — users would have to uninstall first.

```bash
keytool -genkeypair -v \
  -keystore ~/keystores/ainews-release.jks \
  -alias ainews \
  -keyalg RSA -keysize 4096 \
  -validity 10000
```

Pick a strong store password and key password and note all three values (store password,
key alias `ainews`, key password) — they become the secrets below.

## 2. Add the four GitHub Actions secrets (once)

The workflow expects exactly these names:

| Secret | Value |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | the keystore file, base64-encoded |
| `SIGNING_KEY_ALIAS` | the alias from step 1 (`ainews`) |
| `SIGNING_STORE_PASSWORD` | the keystore password |
| `SIGNING_KEY_PASSWORD` | the key password |

With the GitHub CLI:

```bash
base64 -i ~/keystores/ainews-release.jks | gh secret set SIGNING_KEYSTORE_BASE64
gh secret set SIGNING_KEY_ALIAS --body "ainews"
gh secret set SIGNING_STORE_PASSWORD   # paste the value when prompted
gh secret set SIGNING_KEY_PASSWORD     # paste the value when prompted
```

(Or via the web UI: repo → Settings → Secrets and variables → Actions → New repository
secret. On Linux use `base64 -w0` instead of `base64 -i`.)

## 3. Publish a release

The app version is derived from the tag (`v1.2.3` → versionName `1.2.3`,
versionCode `1002003`), so releasing is just tagging:

```bash
git checkout main && git pull
git tag v1.0.0
git push origin v1.0.0
```

The `Release` workflow then:

1. builds `:app:assembleRelease` (R8 + the committed baseline profile) against the live
   BFF endpoint,
2. signs the APK with the keystore from the secrets and verifies the signature
   (`apksigner verify`),
3. generates the changelog from conventional commits (git-cliff),
4. creates the GitHub Release for the tag with the APK attached twice: versioned
   (`ai-news-v1.0.0.apk`) and under the stable name `ai-news.apk`, which keeps the
   README's `releases/latest/download/ai-news.apk` link and QR code permanent.

Check the result at <https://github.com/baruckis/ai-news/releases>, install the APK on a
phone and smoke-test list → detail. Then update the Appetize demo — see [DEMO.md](DEMO.md).

## 4. Signed builds on your own machine (optional)

CI does not need this, but to produce a signed APK locally add to `local.properties`:

```properties
SIGNING_KEYSTORE_PATH=/Users/you/keystores/ainews-release.jks
SIGNING_KEY_ALIAS=ainews
SIGNING_STORE_PASSWORD=...
SIGNING_KEY_PASSWORD=...
```

and run `./gradlew :app:assembleRelease`. Without these keys the release build still
assembles — it is just unsigned (fine for contributors, not installable as an update).
