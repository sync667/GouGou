# GouGou 🎮

A 2D multiplayer adventure game built with Java 17 and LibGDX.

![Java](https://img.shields.io/badge/Java-17-orange)
![LibGDX](https://img.shields.io/badge/LibGDX-1.12.1-red)
![License](https://img.shields.io/badge/License-MIT-blue)

## Features

- **Procedural World Generation** — Infinite variety with 29+ terrain types including forests, oceans, mountains, swamps, and more
- **Multiplayer** — UDP-based networking with LAN discovery and easy server hosting
- **Character System** — Choose from Warrior, Mage, or Ranger classes with customizable appearance
- **Full UI/UX** — Health/mana bars, chat system, inventory, minimap, and settings
- **Profile System** — Persistent user profiles with stats tracking
- **Modern Graphics** — Built on LibGDX with proper scaling for any resolution
- **Entity System** — Players, mobs (Slime, Skeleton, Wolf, Spider, Goblin), and more
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

## Project Structure

```
GouGou/
├── core/                    # Core game logic (platform-independent)
│   └── src/main/java/com/gougou/core/
│       ├── GouGouGame.java          # Main game class
│       ├── config/                  # Game settings
│       ├── entities/                # Player, Mob, EntityManager
│       ├── input/                   # Input handling
│       ├── net/                     # Networking (client, server, protocol)
│       ├── profile/                 # User profiles
│       ├── screens/                 # Game screens (menu, game, settings, etc.)
│       ├── ui/                      # HUD, chat, inventory, minimap
│       └── world/                   # World generation, tiles
├── desktop/                 # Desktop launcher (LWJGL3)
├── server/                  # Dedicated server
├── assets/                  # Game assets (sprites, maps)
└── docs/                    # Documentation
```

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

## Building from Source

```bash
# Build all modules
gradle build

# Run tests
gradle :core:test

# Create distribution
gradle :desktop:distZip
```

## Tech Stack

- **Java 17** — Modern Java with records, switch expressions, text blocks
- **LibGDX 1.12.1** — Cross-platform game framework
- **LWJGL3** — Desktop backend (OpenGL)
- **Gson** — JSON serialization for settings/profiles
- **JUnit 5** — Testing framework

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is open source. See [LICENSE](LICENSE) for details.

## Credits

Created by [sync667](https://github.com/sync667)
