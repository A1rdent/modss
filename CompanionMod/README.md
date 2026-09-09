# Companion Mod for Minecraft 1.20.1

A comprehensive Forge mod that adds a companion NPC entity to Minecraft 1.20.1 with intelligent AI, inventory management, mining, and resource gathering capabilities.

## Features

### Core Features
- **Companion NPC Entity** - A humanoid NPC that follows the player and executes commands
- **Custom Inventory** - 36-slot inventory system with persistence
- **Interactive GUI** - Control companion behavior through an in-game GUI
- **Health System** - Full health mechanics with damage and death
- **Owner System** - Each companion is bound to its owner/summoner

### Companion Abilities
- **Mining** - Break stone, ore, dirt, gravel, sand and other minable blocks
- **Resource Gathering** - Collect items from the ground
- **Auto-Deposit** - Automatically deposit mined materials into nearby chests
- **Player Following** - Follow the player with smart pathfinding
- **Teleportation** - Teleport to player if distance exceeds 50 blocks
- **Smart AI** - Goal-based AI system for autonomous behavior management

### Summoning Options
- **Command Summoning** - Use `/companion summon` to summon a companion
- **Item Summoning** - Right-click with Companion Spawner item to spawn
- **Command Stopping** - Use `/companion stop` to stop all companions

### Control Commands
- **Mine** - Start/stop mining mode
- **Follow** - Enable/disable player following
- **Gather** - Start/stop gathering mode
- **Stop** - Stop all current activities
- **Inventory** - Access companion's inventory

## Project Structure

```
CompanionMod/
├── src/main/java/com/example/companionmod/
│   ├── CompanionMod.java                    # Main mod class
│   ├── client/
│   │   ├── ClientEvents.java               # Client-side event handlers
│   │   ├── gui/
│   │   │   ├── CompanionScreen.java        # GUI screen for companion control
│   │   │   └── CompanionScreenHandler.java # GUI menu handler
│   │   └── render/
│   │       ├── CompanionModel.java         # Entity model definition
│   │       └── CompanionEntityRenderer.java # Entity renderer
│   ├── common/
│   │   ├── CommonEvents.java               # Common-side event handlers
│   │   ├── command/
│   │   │   └── CompanionCommand.java       # Command registration
│   │   ├── entity/
│   │   │   ├── CompanionEntity.java        # Main entity class
│   │   │   ├── CompanionAI.java            # AI behavior system
│   │   │   └── CompanionInventory.java     # Custom inventory system
│   │   ├── item/
│   │   │   └── CompanionSpawnerItem.java   # Spawner item implementation
│   │   └── util/
│   │       └── CompanionUtils.java         # Utility functions
│   └── registry/
│       ├── EntityTypeRegistry.java         # Entity type registration
│       └── ItemRegistry.java               # Item registration
└── src/main/resources/
    ├── META-INF/
    │   └── mods.toml                       # Mod metadata
    └── assets/companionmod/
        ├── lang/
        │   └── en_us.json                  # Language strings
        ├── models/item/
        │   └── companion_spawner.json      # Item model
        └── textures/
            ├── entity/companion/           # Entity textures
            ├── item/                       # Item textures
            └── gui/                        # GUI textures
```

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Gradle 7.0 or higher
- Minecraft 1.20.1 (Forge 47.3.0 or compatible)

### Building the Mod

1. **Clone/Extract the project:**
   ```bash
   cd CompanionMod
   ```

2. **Setup Gradle workspace:**
   ```bash
   ./gradlew genSources
   ```

3. **Build the mod JAR:**
   ```bash
   ./gradlew build
   ```

4. The compiled JAR will be in `build/libs/CompanionMod-1.0.0.jar`

### Installing the Mod

1. Place the JAR file in your Minecraft mods folder:
   ```
   %APPDATA%\.minecraft\mods\
   ```
   Or for other systems:
   - Linux: `~/.minecraft/mods/`
   - macOS: `~/Library/Application Support/minecraft/mods/`

2. Launch Minecraft with Forge 1.20.1

### Running in Development

Use the Gradle run configurations:

```bash
# Run client
./gradlew runClient

# Run server
./gradlew runServer
```

## Texture Setup

**Important:** The mod requires textures for proper visual rendering. See [TEXTURES.md](TEXTURES.md) for detailed instructions on creating or adding textures.

Quick summary of required textures:
- `assets/companionmod/textures/entity/companion/companion.png` (64x64)
- `assets/companionmod/textures/item/companion_spawner.png` (16x16)
- `assets/companionmod/textures/gui/companion_gui.png` (176x222)

## Usage Guide

### Summoning a Companion

**Method 1: Command**
```
/companion summon
```

**Method 2: Item**
1. Obtain a Companion Spawner item (Creative mode or via NBT editor)
2. Right-click in the world
3. Companion spawns next to you

### Controlling Your Companion

1. **Open GUI:** Open your inventory and click on the companion nearby
2. **Send Commands:**
   - **Mine** - Companion breaks nearby minable blocks
   - **Follow** - Companion follows you (default enabled)
   - **Gather** - Companion collects items from ground
   - **Stop** - Stop all companion activities

3. **Manage Inventory:**
   - Open the companion inventory in the GUI
   - Drag items to transfer between inventories

### Stopping Companions

Use the command:
```
/companion stop
```

This stops all your companions' activities.

## Configuration

### Entity Attributes
- Health: 20 HP (full bar)
- Movement Speed: 0.3 (follows player speed)
- Attack Damage: 4
- Follow Range: 32 blocks
- Armor: 2

### AI Behavior Ranges
- Mining search range: 16 blocks
- Chest detection range: 8 blocks
- Player follow range: 4 blocks
- Teleport distance: > 50 blocks

### Minable Blocks
- Stone variants (stone, deepslate)
- All ores (copper, iron, coal, lapis, gold, diamond, emerald, redstone)
- Dirt, gravel, sand, sandstone
- Block variants (raw copper, raw iron, raw gold blocks)

## Troubleshooting

### Companion doesn't spawn
- Check that you're not in Creative+ mode (some versions)
- Ensure you have commands enabled if using `/companion summon`
- Check server logs for errors

### Companion doesn't mine
- Ensure companion has line-of-sight to blocks
- Check that block type is in whitelist (see above)
- Verify mining goal is enabled

### Companion not following
- Check that follow mode is enabled
- Verify companion can pathfind to your location
- If you're >50 blocks away, companion should teleport

### Items not depositing to chest
- Ensure chest is within 8 blocks
- Check that chest has empty slots
- Try moving closer to the chest

### Textures not loading
- Verify texture files are in correct location with correct names
- Ensure textures are in PNG format with transparency
- Check that file paths match exactly (case-sensitive on Linux/Mac)

## Mod Support

This mod is built using:
- Forge 1.20.1 (47.3.0+)
- Minecraft Forge's event-driven architecture
- NBT serialization for data persistence

## Future Enhancements

Planned features for future versions:
- Multiple companion types (warrior, mage, healer)
- Trading system with companions
- Companion leveling and skill system
- More advanced AI behaviors
- Custom model support
- Configuration file for customization
- Multiplayer synchronized companions
- Combat AI for companion assistance

## License

MIT License - See LICENSE file for details

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Support

For issues, questions, or suggestions:
1. Check this README first
2. Review the troubleshooting section
3. Check existing issues
4. Create a new issue with detailed information

---

**Mod Version:** 1.0.0  
**Minecraft Version:** 1.20.1  
**Forge Version:** 47.3.0+  
**Java Version:** 17+
