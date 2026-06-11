# Browser demo on Appetize.io

[Appetize.io](https://appetize.io/) runs the APK in an emulator streamed to the browser,
so anyone can try the app without installing anything. The free plan (no credit card) is
enough for a portfolio demo.

## 1. Upload the signed APK (once)

1. Download `ai-news.apk` from the
   [latest GitHub Release](https://github.com/baruckis/ai-news/releases/latest)
   (publish one first — see [RELEASE.md](RELEASE.md)).
2. Sign up / log in at <https://appetize.io/> and choose **Upload** (dashboard → Apps →
   Upload), then drop in `ai-news.apk`. Platform is detected as Android automatically.
3. When the upload finishes, open the app's page and copy its **public key** — the link
   looks like `https://appetize.io/app/<publicKey>`.

## 2. Put the link in the README

Replace the `REPLACE_WITH_APPETIZE_PUBLIC_KEY` placeholder in the README's
"Download / Try it" section with the public key, so the
"▶ Try in browser" badge points at `https://appetize.io/app/<publicKey>`.

Optional embed (runs the demo inside any web page):

```html
<iframe src="https://appetize.io/embed/<publicKey>?device=pixel7&osVersion=14"
        width="378" height="800" frameborder="0" scrolling="no"></iframe>
```

## 3. Keep it fresh after each release

Appetize does not pull new builds by itself. After publishing a new release, open the
app in the Appetize dashboard and upload the new `ai-news.apk` over it — the public key
(and therefore the README link) stays the same.

> Automation idea (future work): the Appetize REST API can update the build from CI —
> `curl -u "$APPETIZE_API_TOKEN:" https://api.appetize.io/v1/apps/<publicKey>
> -F "file=@ai-news.apk"` as a step in `release.yml`, with the token stored as a secret.
