# Deploying the BFF to a VPS (Docker Compose + Caddy)

Serves the `:bff` GraphQL server at **https://ainews-api.baruckis.com/graphql**.
Caddy terminates TLS with an automatic Let's Encrypt certificate — no manual
certbot. Tested target: Ubuntu on a Hostinger VPS.

## Prerequisites

- A VPS reachable over SSH, with ports 80 and 443 open to the internet.
- A DNS **A record** for the subdomain pointing at the VPS IP
  (e.g. hPanel → domain → DNS → add `A` record `ainews-api` → `VPS_IP`).
  Verify after a few minutes: `dig +short ainews-api.baruckis.com` returns the VPS IP.
- Your own [NewsData.io](https://newsdata.io/) API key.

## 1. One-time server setup

```bash
ssh root@VPS_IP

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

## 3. Secrets

```bash
cd /opt/ainews/deploy
cp .env.example .env
nano .env        # set NEWSDATA_KEY (and optionally GNEWS_KEY, NEWS_TIMEFRAME)
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
curl https://ainews-api.baruckis.com/graphql \
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
- VPS reboot: nothing to do — `restart: unless-stopped` brings both containers
  back automatically.
