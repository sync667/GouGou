# Contributing to GouGou

Thank you for your interest in contributing to GouGou! This document provides guidelines for contributing.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/GouGou.git`
3. Create a branch: `git checkout -b feature/your-feature`
4. Make your changes
5. Run tests: `gradle :core:test`
6. Build: `gradle build`
7. Commit and push
8. Open a Pull Request

## Development Setup

### Prerequisites
- Java 17+
- Gradle 8+
- An IDE (IntelliJ IDEA recommended)

### Project Structure
- `core/` — Platform-independent game logic (LibGDX + Netty)
- `desktop/` — Desktop launcher (LWJGL3)
- `server/` — Dedicated server with H2 database
- `launcher/` — Auto-updater
- `assets/` — Game resources

### Running
```bash
gradle :desktop:run    # Run the game
gradle :server:run     # Run the dedicated server
gradle :launcher:run   # Run with auto-updater
gradle test            # Run all tests
```

## Code Guidelines

- Use Java 17 features (records, switch expressions, var)
- Follow standard Java naming conventions
- Add tests for new logic in `core/src/test/`
- Keep UI/rendering code separate from game logic
- Use LibGDX APIs for graphics, input, and audio

## Adding New Features

### New Tile Types
1. Add entry to `TileType.java` enum
2. Update `WorldGenerator.java` if the tile should generate naturally
3. Add test in `TileTypeTest.java`

### New Entity Types
1. Extend `Entity` or `Mob` class
2. Register in `EntityManager`
3. Add rendering in `GameScreen.renderEntities()`
4. Add tests

### New Screens
1. Create class implementing `Screen` in `screens/` package
2. Add navigation from relevant screens
3. Use the skin pattern from existing screens for consistent UI

### New Packet Types
1. Add constant to `Protocol.java`
2. Add create method to `Protocol.java` using Netty ByteBuf
3. Handle in `GameClient.ClientHandler` and `GameServer.ServerHandler`
4. For server-authoritative state: only the server should create/send the packet
5. Add test in `ProtocolTest.java`

### Server-Side Features
1. Add to `ServerPlayerData` in `GameServer.java` for runtime state
2. Add to `players` table schema in `ServerDatabase.java` for persistence
3. Add corresponding `savePlayer`/`loadPlayer` fields
4. Add test in `ServerDatabaseTest.java`

## Bug Reports

When reporting bugs, please include:
- Java version (`java -version`)
- Operating system
- Steps to reproduce
- Expected vs actual behavior
- Log output if available

## Pull Request Guidelines

- Keep PRs focused on a single change
- Include tests for new functionality
- Update documentation if needed
- Ensure `gradle build` passes
- Write clear commit messages
