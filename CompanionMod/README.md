# Companion Mod

A Fabric mod for Minecraft 1.19.1 that adds a controllable companion NPC.

## GUI control

The companion is now controlled from an in-game GUI instead of requiring commands.

1. Spawn a companion using the Companion Spawner item (available in the Miscellaneous creative tab).
2. Right-click your companion.
3. The Companion GUI opens with its inventory and control buttons:
   - **Mine** — search for nearby minable blocks and break them.
   - **Follow** — return to following the owner.
   - **Gather** — collect nearby dropped items.
   - **Deposit** — take the companion's inventory to a nearby chest and deposit it.
   - **Stop** — immediately stop all companion tasks.
4. The 36-slot companion inventory is directly usable from the same GUI, including shift-click transfers.

The GUI actions are sent to the server through the ScreenHandler, so the server remains authoritative instead of the client directly changing companion state.

## Commands

Commands are still available as a fallback for testing:

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

## Quick test

1. Start Minecraft 1.19.1 with Fabric Loader and this mod.
2. Get the spawner from the Miscellaneous creative tab, or use:

```text
/give @s companionmod:companion_spawner
```

3. Use the spawner to create the companion.
4. Right-click the companion to open the GUI.
5. Test **Mine**, **Gather**, **Deposit**, **Follow** and **Stop** directly from the GUI.

The old `/companion ...` commands remain available if something needs to be tested without the GUI.

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

On Windows:

```bat
gradlew.bat build
```

The resulting JAR is placed in `build/libs/`.

For development:

```bash
./gradlew runClient
```

## Current limitations

Mining intentionally uses `Level.destroyBlock`, so it does not simulate player mining time or tool durability.

The GUI is now the primary control interface. Commands are retained only as a fallback/test interface.

## Next improvements

1. Add combat/defense behavior.
2. Add tool-aware mining and mining speed.
3. Add configurable roles and priorities.
4. Add richer GUI status synchronization and companion settings.
