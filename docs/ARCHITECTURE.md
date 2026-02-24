# GouGou Architecture

## Overview

GouGou is a 2D multiplayer adventure game built with LibGDX. The architecture follows a multi-module Gradle project structure with clear separation between core logic, platform-specific code, and server functionality.

## Module Structure

```
┌──────────┐     ┌──────────┐
│ Desktop  │     │  Server  │
│ (LWJGL3) │     │ (Headless)│
└────┬─────┘     └────┬─────┘
     │                │
     └────┬───────────┘
          │
     ┌────▼─────┐
     │   Core   │
     │ (LibGDX) │
     └──────────┘
```

### Core Module
Platform-independent game logic. Contains all game systems:

- **GouGouGame** — Main game class, manages screens and global resources
- **Screens** — UI screens using LibGDX Scene2D
- **World** — Tile-based world with procedural generation
- **Entities** — Player, mob, and entity management
- **Networking** — UDP client/server with LAN discovery
- **UI** — HUD, chat, inventory, minimap
- **Profile** — User profile persistence
- **Config** — Game settings

### Desktop Module
LWJGL3-based launcher for desktop platforms. Configures window, resolution, and starts the game.

### Server Module
Standalone dedicated server for hosting multiplayer games without a GUI.

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
- **Transport**: UDP (DatagramSocket)
- **Format**: Binary (ByteBuffer)
- **Discovery**: Broadcast on port 7778

### Packet Types
| ID | Name | Description |
|----|------|-------------|
| 0x01 | HANDSHAKE | Client → Server version check |
| 0x02 | HANDSHAKE_ACK | Server → Client accept/reject |
| 0x03 | LOGIN | Client sends username and character data |
| 0x04 | LOGIN_ACK | Server assigns entity ID and spawn position |
| 0x05 | DISCONNECT | Player leaving |
| 0x06 | SPAWN | New entity spawned |
| 0x07 | DESPAWN | Entity removed |
| 0x08 | MOVE | Position update |
| 0x09 | CHAT | Chat message |
| 0x0A | PING | Latency check |
| 0x0B | PONG | Ping response |

### Connection Flow
```
Client                    Server
  │                         │
  │──── HANDSHAKE ─────────→│
  │←─── HANDSHAKE_ACK ──────│
  │──── LOGIN ─────────────→│
  │←─── LOGIN_ACK ──────────│
  │←─── SPAWN (others) ─────│
  │                         │
  │←──→ MOVE/CHAT/PING ←──→│
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

All user data is stored in `~/.gougou/`:
- **settings.json** — Resolution, audio, gameplay settings
- **profile.json** — Username, character class, skin color, stats

Serialization uses Gson with pretty printing.
