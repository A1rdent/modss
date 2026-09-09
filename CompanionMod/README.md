# Companion Mod

A Fabric mod for Minecraft 1.19.1 that adds a controllable companion NPC.

## Current implementation

- Companion entity with 20 HP, armor, movement speed and persistent owner UUID.
- 36-slot persistent companion inventory.
- Companion follows its owner and teleports back when more than 50 blocks away.
- Mining mode searches a 16-block area for whitelisted blocks and breaks them.
- Gathering mode collects nearby dropped items into the companion inventory.
- Optional auto-deposit mode moves inventory items into a nearby chest.
- `/companion summon` and `/companion stop` commands.
- Companion Spawner item.
- Client-side humanoid renderer and companion texture.
- Basic inventory/control GUI infrastructure.

## Important limitations

The GUI is not yet connected to a server-side screen-opening/network flow, so its buttons should not be considered authoritative multiplayer controls yet. The next development step should be to open the screen from the companion interaction and send commands through a server-side menu handler.

Mining currently uses `Level.destroyBlock`, so it is intentionally simpler than a real player mining action and does not model tool durability or mining time.

## Build

Requirements:
- Java 17
- Minecraft 1.19.1
- Fabric Loader 0.14.21+
- Fabric API 0.79.6+1.19.1

From the `CompanionMod` directory:

```bash
./gradlew build
```

The resulting JAR is placed in `build/libs/`.

For development:

```bash
./gradlew runClient
```

## Commands

Operator-level command permission is required:

```text
/companion summon
/companion stop
```

## Planned next steps

1. Add right-click interaction with the companion to open its GUI.
2. Move GUI actions to server-side packets/menu state instead of changing the entity directly on the client.
3. Add a proper mining state machine with pathfinding, reach checks and configurable mining speed.
4. Add owner-only interaction protection.
5. Improve chest handling for double chests and locked/special containers.
6. Add combat/defense behavior and configurable companion roles.
