# GouGou 🎮

A 2D multiplayer adventure game built with Java 17, LibGDX, and Netty.

![Java](https://img.shields.io/badge/Java-17-orange)
![LibGDX](https://img.shields.io/badge/LibGDX-1.12.1-red)
![Netty](https://img.shields.io/badge/Netty-4.1.108-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

## Features

- **Procedural World Generation** — Infinite variety with 29+ terrain types including forests, oceans, mountains, swamps, and more
- **Multiplayer** — Netty TCP networking with LAN discovery and easy server hosting
- **Server-Authoritative Architecture** — All game state managed server-side to prevent cheating
- **Persistent Player Data** — H2 embedded database stores player progress, inventory, and stats
- **Character System** — Choose from Warrior, Mage, or Ranger classes with customizable appearance
- **Full UI/UX** — Health/mana bars, chat system, inventory, minimap, and settings
- **Profile System** — Persistent user profiles with stats tracking
- **Modern Graphics** — Built on LibGDX with proper scaling for any resolution
- **Entity System** — Players, mobs (Slime, Skeleton, Wolf, Spider, Goblin), and more
- **Auto-Updater** — Launcher checks GitHub Releases for updates before starting the game
- **Cross-Platform** — Runs on Windows, macOS, and Linux

## Quick Start

### Prerequisites

- Java 17 or later ([Download](https://adoptium.net/))
- Gradle 8+ (included via wrapper, or install separately)

### Run the Game

```bash
# Clone the repository
git clone https://github.com/sync667/GouGou.git
cd GouGou

# Run the game
gradle :desktop:run
```

### Run with Auto-Updater

```bash
gradle :launcher:run
```

The launcher checks for new releases on GitHub, downloads updates automatically, then starts the game.

### Build a Release JAR

```bash
gradle :desktop:jar
# Output: desktop/build/libs/desktop-1.0.0.jar
java -jar desktop/build/libs/desktop-1.0.0.jar
```

### Run the Dedicated Server

```bash
gradle :server:run
```

The server stores player data in an H2 embedded database (`server-data/`). Available console commands:
- `status` — Show server status
- `players` — List online players with stats
- `save` — Force-save all player data
- `stop` — Shut down the server

## Project Structure

```
GouGou/
├── core/                    # Core game logic (platform-independent)
│   └── src/main/java/com/gougou/core/
│       ├── GouGouGame.java          # Main game class
│       ├── config/                  # Game settings
│       ├── entities/                # Player, Mob, EntityManager
│       ├── input/                   # Input handling
│       ├── net/                     # Networking (Netty client/server, protocol)
│       ├── profile/                 # User profiles
│       ├── screens/                 # Game screens (menu, game, settings, etc.)
│       ├── ui/                      # HUD, chat, inventory, minimap
│       └── world/                   # World generation, tiles
├── desktop/                 # Desktop launcher (LWJGL3)
├── server/                  # Dedicated server with H2 database
│   └── src/main/java/com/gougou/server/
│       ├── DedicatedServer.java     # Server entry point
│       └── db/ServerDatabase.java   # H2 player persistence
├── launcher/                # Auto-updater launcher
│   └── src/main/java/com/gougou/launcher/
│       └── GouGouLauncher.java      # GitHub release checker
├── assets/                  # Game assets (sprites, maps)
└── docs/                    # Documentation
```

## Architecture

### Networking (Netty TCP)

The game uses Netty for reliable TCP networking with 4-byte length-prefixed framing:

- **Client** → `Bootstrap` + `NioSocketChannel` with `LengthFieldBasedFrameDecoder`
- **Server** → `ServerBootstrap` + `NioServerSocketChannel` with game tick (20 TPS)
- **LAN Discovery** → UDP broadcast on port 7778 (kept separate from TCP)

### Server-Authoritative Model

The server controls all game state to prevent cheating:

1. Client sends **MOVE_INPUT** (directional key flags only, not positions)
2. Server computes movement with collision detection against the world
3. Server sends **PLAYER_STATE** with authoritative position, health, mana, etc.
4. Client renders the authoritative state received from server

### Database (H2)

Player data is persisted in an H2 embedded database:
- **players** table: username, class, level, XP, health, mana, inventory (JSON), last position
- **server_bans** table: username, reason, timestamp
- Auto-save every 60 seconds + save on shutdown

## Controls

| Key | Action |
|-----|--------|
| WASD / Arrow Keys | Move |
| T | Toggle Chat |
| I | Toggle Inventory |
| M | Toggle Minimap |
| E | Interact |
| Space | Attack |
| ESC | Menu / Close UI |

## Multiplayer

### Host a LAN Game
1. Click **Multiplayer** → **Host & Play**
2. Set server name and max players
3. Other players on your network can find your server via **Scan LAN**

### Join a Server
1. Click **Multiplayer**
2. Enter server address and port, or use **Scan LAN** to discover local servers
3. Click **Connect**

### Dedicated Server
Run `gradle :server:run` to start a headless server. Configuration is saved to `server-config.json`.

## Configuration

Settings are stored in `~/.gougou/`:
- `settings.json` — Game settings (resolution, audio, etc.)
- `profile.json` — Player profile and character data

Server data is stored in `server-data/`:
- `gougou_server.mv.db` — H2 database with player and ban data
- `server-config.json` — Server configuration

## Building from Source

```bash
# Build all modules
gradle build

# Run tests
gradle test

# Create distribution
gradle :desktop:distZip

# Build launcher JAR
gradle :launcher:jar
```

## Tech Stack

- **Java 17** — Modern Java with records, switch expressions, text blocks
- **LibGDX 1.12.1** — Cross-platform game framework
- **Netty 4.1.108** — High-performance TCP networking
- **H2 2.2.224** — Embedded SQL database for player persistence
- **LWJGL3** — Desktop backend (OpenGL)
- **Gson** — JSON serialization for settings/profiles
- **JUnit 5** — Testing framework

## Release Cycle

Releases are published as GitHub Releases with attached JAR files. The auto-updater launcher (`launcher` module) automatically checks for and downloads new versions.

To create a release:
1. Update version in `build.gradle`
2. Build the desktop JAR: `gradle :desktop:jar`
3. Create a GitHub Release with a semver tag (e.g., `v1.1.0`)
4. Attach the built JAR to the release

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is open source. See [LICENSE](LICENSE) for details.

## Credits

Created by [sync667](https://github.com/sync667)
