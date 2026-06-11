# Self-hosting the BFF (Docker Compose + Caddy)

A reproducible reference setup for running your own instance of the `:bff` GraphQL
server on any Linux box: the BFF in a Docker container, fronted by Caddy, which
obtains and renews a Let's Encrypt TLS certificate automatically — no manual certbot.
Tested target: Ubuntu on a small VPS.

The public demo at `https://ainews-api.baruckis.com/graphql` runs the same
containerized BFF behind a TLS-terminating reverse proxy; this directory is the
self-contained recipe anyone can use to host their own.

Throughout this guide, replace `news-api.example.com` with your own (sub)domain.

App distribution lives in this directory too: [RELEASE.md](RELEASE.md) covers release
signing and publishing the APK to GitHub Releases; [DEMO.md](DEMO.md) covers the
Appetize.io browser demo.

## Prerequisites

- A server reachable over SSH, with ports 80 and 443 open to the internet.
- A DNS **A record** for your subdomain pointing at the server IP.
  Verify after a few minutes: `dig +short news-api.example.com` returns the server IP.
- Your own [NewsData.io](https://newsdata.io/) API key.

## 1. One-time server setup

```bash
ssh root@SERVER_IP

apt update && apt upgrade -y

# Firewall: allow SSH + HTTP + HTTPS only.
apt install -y ufw
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw --force enable

# Docker (Compose v2 is included).
curl -fsSL https://get.docker.com | sh
docker compose version
```

## 2. Get the project

```bash
mkdir -p /opt/ainews && cd /opt/ainews
git clone https://github.com/baruckis/ai-news.git .
```

## 3. Configure

```bash
cd /opt/ainews/deploy
cp .env.example .env
nano .env        # set NEWSDATA_KEY (and optionally GNEWS_KEY, NEWS_TIMEFRAME)

nano Caddyfile   # replace news-api.example.com with your domain
```

`.env` lives only on the server and is gitignored — never commit it.

## 4. Launch

```bash
cd /opt/ainews/deploy
docker compose up -d --build   # first build takes a few minutes

docker compose ps              # bff and caddy must both be "Up"
docker compose logs -f bff     # Ctrl+C to exit
```

## 5. Verify from the outside

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
docker compose up -d --build
```

## Operations

- Logs: `docker compose logs -f bff`
- Restart: `docker compose restart bff`
- Server reboot: nothing to do — `restart: unless-stopped` brings both containers
  back automatically.
