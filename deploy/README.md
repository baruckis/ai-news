# Self-hosting the BFF

A reproducible recipe for running your own instance of the `:bff` GraphQL server on
any Linux box. There are two shapes, depending on what your server already runs:

- **[Option A — Docker Compose + Caddy](#option-a--docker-compose--caddy-bundled-reference)**:
  the self-contained reference bundled in this directory. Everything runs in
  containers and Caddy obtains and renews a Let's Encrypt TLS certificate
  automatically — no manual certbot. Start here on a fresh server.
- **[Option B — behind an existing nginx](#option-b--behind-an-existing-nginx-how-the-live-demo-runs)**:
  only the BFF container, published on a localhost port, with a host-level nginx
  reverse proxy terminating TLS. This is how the public demo at
  `https://ainews-api.baruckis.com/graphql` actually runs — its server already had
  an nginx managed by a hosting panel (CloudPanel), so the demo uses that instead of
  the bundled Caddy.

Tested target: Ubuntu on a small VPS. Throughout this guide, replace
`news-api.example.com` with your own (sub)domain.

App distribution lives in this directory too: [RELEASE.md](RELEASE.md) covers release
signing and publishing the APK to GitHub Releases; [DEMO.md](DEMO.md) covers the
Appetize.io browser demo.

## Prerequisites

- A server reachable over SSH.
- A DNS **A record** for your subdomain pointing at the server IP.
  Verify after a few minutes: `dig +short news-api.example.com` returns the server IP.
- Your own [NewsData.io](https://newsdata.io/) API key.
- Option A additionally needs ports 80 and 443 free and open to the internet;
  Option B assumes an nginx already owns them.

## Common first steps (both options)

### 1. One-time server setup

```bash
ssh root@SERVER_IP

apt update && apt upgrade -y

# Firewall: allow SSH + HTTP + HTTPS only. (Skip if your hosting panel
# already manages the firewall.)
apt install -y ufw
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw --force enable

# Docker (Compose v2 is included).
curl -fsSL https://get.docker.com | sh
docker compose version
```

### 2. Get the project

```bash
mkdir -p /opt/ainews && cd /opt/ainews
git clone https://github.com/baruckis/ai-news.git .
```

### 3. Configure secrets

```bash
cd /opt/ainews/deploy
cp .env.example .env
nano .env        # set NEWSDATA_KEY (and optionally GNEWS_KEY, NEWS_TIMEFRAME)
```

`.env` lives only on the server and is gitignored — never commit it.

## Option A — Docker Compose + Caddy (bundled reference)

[`docker-compose.yml`](docker-compose.yml) starts two containers: the BFF (internal
only) and Caddy as the sole public entry point, with automatic HTTPS via the
[`Caddyfile`](Caddyfile).

### A.1. Launch

```bash
cd /opt/ainews/deploy
nano Caddyfile                 # replace news-api.example.com with your domain

docker compose up -d --build   # first build takes a few minutes

docker compose ps              # bff and caddy must both be "Up"
docker compose logs -f bff     # Ctrl+C to exit
```

## Option B — behind an existing nginx (how the live demo runs)

If the server already runs nginx on ports 80/443 (bare or via a panel such as
CloudPanel), the bundled Caddy would fight it for those ports. Run only the BFF
container, publish it on a localhost-only port, and let nginx terminate TLS and
proxy to it.

### B.1. Publish the BFF on a localhost port

Add an override next to `docker-compose.yml` — Compose merges it in automatically —
so the committed file stays untouched (the override is untracked; it does not block
`git pull` when updating):

```bash
cd /opt/ainews/deploy
cat > compose.override.yml <<'EOF'
services:
  bff:
    ports:
      - "127.0.0.1:8000:8080" # localhost only — nginx is the public entry point
EOF

docker compose up -d --build bff   # only the bff service; no caddy container

docker compose ps                  # bff must be "Up (healthy)"
```

### B.2. Point nginx at it

A minimal server block (with a panel like CloudPanel, create the site/certificate
there and put the `location` block into its vhost; with plain nginx, obtain the
certificate with certbot first):

```nginx
server {
    listen 443 ssl;
    server_name news-api.example.com;

    ssl_certificate     /etc/letsencrypt/live/news-api.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/news-api.example.com/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Reload nginx (`nginx -t && systemctl reload nginx`) and you're live.

## Verify from the outside (both options)

```bash
curl https://news-api.example.com/graphql \
  -H 'content-type: application/json' \
  -d '{"query":"{ aiNews { articles { id title sourceName } } }"}'
```

Expect JSON with real articles over a valid TLS certificate.

## Updating to a new version

```bash
cd /opt/ainews
git pull
cd deploy
docker compose up -d --build        # Option B: append "bff"
```

## Operations

- Logs: `docker compose logs -f bff`
- Restart: `docker compose restart bff`
- Server reboot: nothing to do — `restart: unless-stopped` brings the container(s)
  back automatically (Option B: nginx is a system service and restarts on its own).
