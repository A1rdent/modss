# Companion Mod

A Fabric mod for Minecraft 1.19.1 that adds a controllable companion NPC.

## Playable now

The current build is designed to be immediately testable in Minecraft:

- Companion entity with 20 HP and persistent owner UUID.
- 36-slot persistent companion inventory.
- Follow mode with pathfinding and teleport when more than 50 blocks away.
- Mining mode searches within 16 blocks, walks to the target and breaks it.
- Gathering mode searches within 12 blocks, walks to dropped items and collects them.
- Deposit mode walks to a nearby chest and moves companion inventory items into it.
- Owner-only commands: your commands affect the nearest companion owned by you.
- Companion Spawner item.
- Client-side humanoid renderer and companion texture.

## Commands

Commands work for normal players; operator permission is not required.

```text
/companion summon
/companion follow
/companion mine
/companion gather
/companion deposit
/companion stop
/companion status
/companion help
```

Only the nearest owned companion within 64 blocks is controlled by `follow`, `mine`, `gather`, `deposit` and `status`. `stop` stops all of your companions within 64 blocks.

### Quick test

1. Start Minecraft 1.19.1 with Fabric Loader and this mod.
2. In a world, run:

```text
/companion summon
```

3. Test:

```text
/companion status
/companion mine
```

The companion will search for minable blocks, walk to them and break them. To return to you:

```text
/companion follow
```

To collect dropped blocks/items:

```text
/companion gather
```

To put its inventory into a nearby chest:

```text
/companion deposit
```

To immediately stop it:

```text
/companion stop
```

You can also get the spawner with:

```text
/give @s companionmod:companion_spawner
```

The spawner is also available in the Miscellaneous creative tab.

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

## Known limitations

The inventory/control GUI infrastructure exists but is not yet the authoritative command interface. For this test build, use the server-side `/companion ...` commands above; they work in singleplayer and on a dedicated server.

Mining intentionally uses `Level.destroyBlock`, so it does not simulate player mining time or tool durability yet.

## Next improvements

1. Open the companion GUI with right-click.
2. Route GUI buttons through a server-side handler.
3. Add tool-aware mining and mining speed.
4. Add combat/defense behavior.
5. Add configurable roles and priorities.
