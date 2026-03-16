# Local Deployment Changes — lausync fork

This document describes all modifications made to the upstream
[AsyncTI4/TI4_map_generator_bot](https://github.com/AsyncTI4/TI4_map_generator_bot)
to run a self-hosted instance with a companion web frontend
([ti4_web](https://github.com/lauofdoom/ti4_web)).

---

## Git branch strategy

```
upstream/master  ─── official AsyncTI4 repo (never commit here)
       │
       │  git merge upstream/master
       ▼
     master       ─── tracks upstream; merge-only, no local commits
       │
       │  git rebase master
       ▼
 local/lausync    ─── ALL local customizations live here (current branch)
```

**Workflow for pulling upstream updates:**

```bash
# 1. Pull latest upstream into master
git checkout master
git fetch upstream
git merge upstream/master

# 2. Rebase local customizations on top
git checkout local/lausync
git rebase master

# 3. Fix any rebase conflicts (most will be in the // LOCAL: marked files)
# 4. Rebuild
cd /async-ti4 && docker compose up -d --build
```

---

## New local-only files (zero upstream conflict risk)

These files have no upstream equivalent and will never cause merge conflicts.

### REST API — web data endpoints

| File | Purpose |
|------|---------|
| `src/main/java/ti4/spring/api/webdata/PublicWebdataController.java` | `GET /api/public/game/{gameId}/webdata` — serves full game state as JSON |
| `src/main/java/ti4/spring/api/webdata/PublicWebdataService.java` | Service layer; calls `AsyncTi4WebsiteHelper.buildWebData()` |
| `src/main/java/ti4/spring/api/games/PublicGamesController.java` | `GET /api/public/games` — serves game list for the frontend landing page |
| `src/main/java/ti4/spring/api/games/PublicGamesService.java` | Service layer for game list |
| `src/main/java/ti4/spring/api/games/GameSummary.java` | DTO for a single game entry |

### Spring configuration — local beans

| File | Purpose |
|------|---------|
| `src/main/java/ti4/spring/context/LocalWebConfiguration.java` | Serves `/images/**` from the bot's classpath with `.webp → .png → .jpg` fallback |
| `src/main/java/ti4/spring/security/LocalSecurityConfiguration.java` | Exempts `/images/**` from Spring Security via `WebSecurityCustomizer` |
| `src/main/java/ti4/local/LocalConfig.java` | Reads `WEB_BASE_URL` env var; provides `getWebBaseUrl()` |

### Infrastructure

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Wires up `asyncti4-bot` (port 8271) + `ti4-web` nginx frontend (port 8272) |
| `Dockerfile` | Adds `MAVEN_OPTS` memory limits for building on constrained hardware |
| `.env` | Runtime secrets and deployment config (gitignored — see `.env.example`) |

---

## Modified upstream files (minimal diffs, marked `// LOCAL:`)

These files were originally from upstream but required small changes.
Each change is marked with a `// LOCAL:` comment for easy identification
and re-application after upstream merges.

### `src/main/java/ti4/helpers/ButtonHelper.java`

**Change:** `sendFileWithCorrectButtons()` now passes the web UI URL to the
"Open in browser" Discord button, instead of the raw Discord CDN image link.

```java
// LOCAL: link "Open in browser" to the self-hosted web UI instead of the Discord CDN image
String webUrl = !game.isFowMode() ? LocalConfig.getWebBaseUrl() + game.getName() : null;
```

**Re-apply after upstream merge:** Find `sendFileWithCorrectButtons`, replace
`MessageHelper.sendFileToChannelAndAddLinkToButtons(channel, ..., onSuccess)` with the
two-line version above.

---

### `src/main/java/ti4/message/MessageHelper.java`

**Change:** `sendFileToChannelAndAddLinkToButtons()` overloaded with an optional
`@Nullable String webUrl` parameter. When provided, the "Open in browser" button
uses that URL instead of the Discord attachment URL.

```java
// LOCAL: overloaded to accept an optional webUrl; when provided, the "Open in browser"
// button links to the self-hosted web UI instead of the raw Discord CDN attachment URL.
public static void sendFileToChannelAndAddLinkToButtons(..., @Nullable String webUrl)
```

**Re-apply after upstream merge:** Add the overloaded method after the existing
`sendFileToChannelAndAddLinkToButtons(... onSuccess)` method.

---

### `src/main/java/ti4/website/AsyncTi4WebsiteHelper.java`

**Change:** `buildWebData(String gameId, Game game)` extracted from `putPlayerData()`
as a `public static` method so `PublicWebdataService` can call it without triggering
a CDN upload.

```java
// LOCAL: extracted from putPlayerData() so that PublicWebdataService can serve game state
// via REST without triggering a CDN upload. Must remain public.
public static Map<String, Object> buildWebData(String gameId, Game game)
```

**Re-apply after upstream merge:** Extract the data-building block from `putPlayerData()`
into this public method and update the call site to `buildWebData(gameId, game)`.

---

### `src/main/java/ti4/buttons/handlers/game/CreateGameButtonHandler.java`

**Change:** The experience-gate check (requiring prior completed games before
creating a new game) is temporarily disabled for testing purposes.

```java
// TODO: re-enable experience check once test game is completed
```

**Re-apply after upstream merge:** Uncomment the experience check block.

---

## Environment variables

Copy `.env.example` to `.env` and fill in the secrets before running.

| Variable | Required | Description |
|----------|----------|-------------|
| `BOT_TOKEN` | ✅ | Discord bot token from the Developer Portal |
| `BOT_USER_ID` | ✅ | Discord user ID of the bot account |
| `DISCORD_SERVER_ID` | ✅ | ID of the Discord server the bot operates in |
| `WEB_BASE_URL` | optional | Base URL for the web frontend game view.<br>Default: `https://lausyncti4.thefords.cloud/game/` |

---

## Web frontend (ti4_web)

The companion frontend is at `/mnt/user/appdata/ti4_web` (mapped from `/ti4_web`).
It is built by Docker and served via nginx on port **8272**.

The nginx proxy at `/ti4_web/nginx.conf` routes:

| Path | Proxied to |
|------|-----------|
| `/proxy/maps.json` | `asyncti4-bot:8081/api/public/games` |
| `/proxy/webdata/{gameId}/*` | `asyncti4-bot:8081/api/public/game/{gameId}/webdata` |
| `/images/**` | `asyncti4-bot:8081/images/**` (classpath images with webp fallback) |
| `/bot/**` | `asyncti4-bot:8081/**` |
| `/ws` | `asyncti4-bot:8081/ws` (WebSocket) |
| `/auth/**` | `host.docker.internal:8000` (Discord OAuth service) |

### Frontend environment

Set in `/ti4_web/.env` or as Docker build args:

| Variable | Description |
|----------|-------------|
| `VITE_WEBSOCKET_URL` | WebSocket URL (defaults to `wss://<host>/ws` in production) |
| `VITE_DISCORD_LOGIN_URL` | Discord OAuth login URL (defaults to `/auth/login`) |
| `VITE_DISCORD_REDIRECT_URI` | OAuth redirect URI (defaults to `window.location.origin + /login`) |

---

## Rebuilding

```bash
# Rebuild both services
cd /async-ti4
docker compose up -d --build

# Rebuild only the frontend (faster — no Java compilation)
docker compose up -d --build ti4-web

# Rebuild only the bot
docker compose up -d --build asyncti4-bot
```
