# Setup Guide

## System Requirements

- **OS**: Windows 10+, macOS 10.15+, Linux (Ubuntu 20.04+)
- **Java**: 17 or later
- **RAM**: 512 MB minimum
- **Storage**: 50 MB
- **GPU**: Any GPU with OpenGL 2.0 support

## Installation

### From Release (Recommended)

1. Download the latest release JAR from the [Releases](https://github.com/sync667/GouGou/releases) page
2. Double-click the JAR file, or run from terminal:
   ```bash
   java -jar GouGou-desktop-1.0.0.jar
   ```

### From Source

1. Install Java 17+:
   - **Windows/Mac/Linux**: [Adoptium](https://adoptium.net/)
   
2. Clone and build:
   ```bash
   git clone https://github.com/sync667/GouGou.git
   cd GouGou
   gradle build
   ```

3. Run:
   ```bash
   gradle :desktop:run
   ```

## First Launch

1. **Set up your profile**: Click "Profile & Character" to:
   - Choose a username
   - Select a character class (Warrior, Mage, Ranger)
   - Pick a skin color

2. **Configure settings**: Click "Settings" to adjust:
   - Resolution (800x600 to 2560x1440)
   - Fullscreen mode
   - Audio volumes
   - FPS display

3. **Play**: Click "Single Player" to start exploring!

## Multiplayer Setup

### Hosting a LAN Game
1. Click "Multiplayer" → "Host & Play"
2. Enter a server name
3. Click "Host & Play" — the server starts and you connect automatically
4. Share your IP address with friends on the same network

### Joining a Game
1. Click "Multiplayer"
2. Enter the host's IP address and port (default: 7777)
3. Click "Connect"
4. Or use "Scan LAN" to automatically find servers

### Dedicated Server
For running a persistent server:
```bash
gradle :server:run
```

Edit `server-config.json` to customize:
```json
{
  "serverName": "My Server",
  "port": 7777,
  "maxPlayers": 10,
  "worldSize": 256,
  "worldSeed": 12345,
  "motd": "Welcome!"
}
```

## Troubleshooting

### Game won't start
- Ensure Java 17+ is installed: `java -version`
- Check that JAVA_HOME is set correctly
- Try running from terminal to see error messages

### Poor performance
- Reduce resolution in Settings
- Lower view distance
- Close other applications

### Can't connect to server
- Ensure both players are on the same network for LAN play
- Check firewall settings (allow port 7777 UDP)
- Verify the server is running and the IP/port are correct

### Settings reset
- Settings are stored in `~/.gougou/settings.json`
- Profile data is in `~/.gougou/profile.json`
- Delete these files to reset to defaults
