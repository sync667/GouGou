# GouGou Architecture

## Overview

GouGou is a 2D multiplayer adventure game built with LibGDX and Netty. The architecture follows a multi-module Gradle project structure with clear separation between core logic, platform-specific code, server functionality, and the auto-updater launcher.

## Module Structure

```
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Desktop  │  │  Server  │  │ Launcher │
│ (LWJGL3) │  │(Netty+H2)│  │(Updater) │
└────┬─────┘  └────┬─────┘  └──────────┘
     │              │
     └────┬────────┘
          │
     ┌────▼─────┐
     │   Core   │
     │(LibGDX + │
     │  Netty)  │
     └──────────┘
```

### Core Module
Platform-independent game logic. Contains all game systems:

- **GouGouGame** — Main game class, manages screens and global resources
- **Screens** — UI screens using LibGDX Scene2D
- **World** — Tile-based world with procedural generation
- **Entities** — Player, mob, and entity management
- **Networking** — Netty TCP client/server with LAN discovery (UDP)
- **UI** — HUD, chat, inventory, minimap
- **Profile** — User profile persistence
- **Config** — Game settings

### Desktop Module
LWJGL3-based launcher for desktop platforms. Configures window, resolution, and starts the game.

### Server Module
Standalone dedicated server with H2 embedded database for persistent player data.

### Launcher Module
Auto-updater that checks GitHub Releases for new versions before launching the game.

## Game Loop

```
GouGouGame.create()
    → Load settings & profile
    → Show MainMenuScreen

MainMenuScreen
    → Single Player → GameScreen (local)
    → Multiplayer → ServerBrowserScreen → GameScreen (networked)
    → Profile → ProfileScreen
    → Settings → SettingsScreen

GameScreen.render(delta)
    → update(delta)
        → InputManager polls input
        → EntityManager.update() moves entities
        → Network sync (if multiplayer)
    → renderWorld() — Draw visible tiles
    → renderEntities() — Draw players and mobs
    → HUD.render() — Health, mana, XP bars
    → ChatSystem.render() — Chat messages
    → InventoryUI.render() — Inventory grid
    → MiniMap.render() — World overview
```

## World Generation

The `WorldGenerator` uses layered Perlin noise to create natural-looking terrain:

1. **Elevation noise** — Determines land vs water vs mountains
2. **Moisture noise** — Determines biome type (desert, forest, swamp)
3. **Detail noise** — Adds variation within biomes
4. **Decoration pass** — Places trees, flowers, rocks, mushrooms
5. **Path generation** — Creates winding paths through the world

### Biome Mapping
| Elevation | Moisture | Result |
|-----------|----------|--------|
| < 0.22 | Any | Deep Water |
| 0.22-0.32 | Any | Water |
| 0.32-0.36 | Any | Sand (Beach) |
| > 0.82 | Any | Snow |
| 0.75-0.82 | > 0.5 | Ice |
| 0.75-0.82 | < 0.5 | Stone |
| 0.36-0.75 | < 0.25 | Dirt/Gravel |
| 0.36-0.75 | > 0.75 | Swamp/Tall Grass |
| 0.36-0.75 | 0.25-0.75 | Grass |

## Networking

### Protocol
- **Transport**: Netty TCP (NioSocketChannel / NioServerSocketChannel)
- **Framing**: 4-byte length prefix (LengthFieldBasedFrameDecoder / LengthFieldPrepender)
- **Format**: Binary (Netty ByteBuf)
- **Discovery**: UDP Broadcast on port 7778 (separate DatagramSocket)

### Server-Authoritative Model

The server is the source of truth for all game state:

```
Client                         Server
  │                              │
  │─── MOVE_INPUT (key flags) ──→│ Server computes position
  │                              │ with collision detection
  │←── PLAYER_STATE (pos/hp/mp)──│ Server sends authoritative state
  │                              │
  │←── INVENTORY_UPDATE ─────────│ Server manages items
```

- Clients send **MOVE_INPUT** packets with directional key flags (up/down/left/right)
- Server processes movement against world collision maps
- Server periodically sends **PLAYER_STATE** with authoritative position, health, mana, level
- Client cannot set its own position, health, mana, or inventory
- Chat messages are re-tagged with server-verified entity IDs

### Packet Types
| ID | Name | Direction | Description |
|----|------|-----------|-------------|
| 0x01 | HANDSHAKE | C→S | Client version check |
| 0x02 | HANDSHAKE_ACK | S→C | Accept/reject connection |
| 0x03 | LOGIN | C→S | Username and character data |
| 0x04 | LOGIN_ACK | S→C | Entity ID, spawn position, world seed |
| 0x05 | DISCONNECT | Both | Player leaving / server shutdown |
| 0x06 | SPAWN | S→C | New entity spawned |
| 0x07 | DESPAWN | S→C | Entity removed |
| 0x08 | MOVE | S→C | Position update (other players) |
| 0x09 | CHAT | Both | Chat message |
| 0x0A | PING | C→S | Latency check |
| 0x0B | PONG | S→C | Ping response |
| 0x0F | PLAYER_STATE | S→C | Authoritative player state |
| 0x10 | MOVE_INPUT | C→S | Movement intent (key flags) |
| 0x11 | INVENTORY_UPDATE | S→C | Server-managed inventory |

### Connection Flow
```
Client                    Server
  │                         │
  │──── HANDSHAKE ─────────→│
  │←─── HANDSHAKE_ACK ──────│
  │──── LOGIN ─────────────→│
  │←─── LOGIN_ACK ──────────│
  │←─── SPAWN (others) ─────│
  │←─── INVENTORY_UPDATE ───│
  │                         │
  │──── MOVE_INPUT ────────→│
  │←─── PLAYER_STATE ───────│
  │←──→ CHAT ←─────────────→│
  │                         │
  │──── DISCONNECT ────────→│
```

## Entity System

```
Entity (abstract)
├── Player — User-controlled character
│   ├── Health/Mana system
│   ├── Inventory
│   ├── Level/Experience
│   └── Character customization
└── Mob — AI-controlled creature
    ├── MobType (Slime, Skeleton, Wolf, Spider, Goblin)
    ├── Wandering AI
    └── Type-specific stats
```

## Data Persistence

### Client-Side
All user data is stored in `~/.gougou/`:
- **settings.json** — Resolution, audio, gameplay settings
- **profile.json** — Username, character class, skin color, stats

Serialization uses Gson with pretty printing.

### Server-Side (H2 Database)
The dedicated server stores all authoritative game data in an H2 embedded database (`server-data/gougou_server.mv.db`):

**players table:**
| Column | Type | Description |
|--------|------|-------------|
| username | VARCHAR(32) PK | Unique player identifier |
| character_class | INT | 0=Warrior, 1=Mage, 2=Ranger |
| skin_color | INT | Character appearance (0-5) |
| level | INT | Current level |
| experience | INT | XP toward next level |
| health/max_health | INT | Current and max HP |
| mana/max_mana | INT | Current and max MP |
| inventory_json | TEXT | JSON array of item names |
| last_x, last_y | FLOAT | Last known position |
| last_login | TIMESTAMP | When player last connected |

**server_bans table:**
| Column | Type | Description |
|--------|------|-------------|
| username | VARCHAR(32) PK | Banned player |
| reason | VARCHAR(256) | Ban reason |
| banned_at | TIMESTAMP | When ban was issued |

Player data auto-saves every 60 seconds and on server shutdown.

## Auto-Updater

The `launcher` module provides an auto-updater that runs before the main game:

1. Reads current version from `~/.gougou/version.properties`
2. Queries GitHub API: `GET /repos/sync667/GouGou/releases/latest`
3. Compares semver versions
4. If newer version available, downloads the JAR asset with a progress bar
5. Saves new version and launches the updated game

The launcher can be bypassed with `--no-update` flag.
