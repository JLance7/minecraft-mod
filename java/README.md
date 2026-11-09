# Slime Gun — Fabric / Minecraft Java 1.21.1

A Java mod that adds a craftable **Slime Gun**. Right-click to fire a slime shot:
a mob hit within **28 blocks** is frozen inside translucent, breakable slime
blocks for **8 seconds** (160 game ticks).

## Install and play

1. Install **Minecraft Java Edition 1.21.1** and
   [Fabric Loader](https://fabricmc.net/use/installer/).
2. Install [Fabric API for 1.21.1](https://modrinth.com/mod/fabric-api/versions?g=1.21.1).
3. Put **dist/slime-gun-1.0.0.jar** into that Minecraft installation's `mods` folder.
   For the default Windows installation this is `%APPDATA%/.minecraft/mods`.
4. Launch the Fabric 1.21.1 profile.

In multiplayer, install this mod and Fabric API on **both the server and clients**.
This JAR targets **Fabric, Java Edition, exactly Minecraft 1.21.1**. It is not a
Bedrock add-on or a Forge/NeoForge mod. Minecraft 1.21.1 runs on Java 21; the
official launcher normally supplies the game runtime.

## Get the gun

Find **Slime Gun** in the Creative **Combat** tab, or use:

```mcfunction
/give @s slimegun:slime_gun
```

Survival crafting recipe (crafting table):

```text
I S I
R D S
. . I
```

`I` = iron ingot, `S` = slimeball, `R` = redstone dust, `D` = dispenser,
`.` = empty slot. Total: 3 iron ingots, 2 slimeballs, 1 redstone, 1 dispenser.
Picking up a slimeball unlocks the recipe in the recipe book.

## Use

- Hold the gun and **right-click** while aiming at a mob.
- Each fired shot costs **1 slimeball** from your inventory and **1 durability**.
- The gun has **384 durability**, a **1-second cooldown**, and unlimited shots in Creative.
- Missed shots still use ammo. A message appears if you have no slimeballs.
- Slime particles trace the shot; normal blocks stop it.
- Shooting an already trapped mob refreshes its freeze timer.
- Break **all cocoon blocks touching the mob** to release it before the timer ends.
- Frozen mobs cannot move or run AI; a frozen creeper's fuse also pauses.

Players, tamed pets, the Wither, and the Ender Dragon are not valid targets.
Other living mobs, including animals, can be encased. The gun deals no direct damage.

## World behavior

The cocoon only occupies existing **air**. It does not replace terrain, chests,
plants, liquids, or other mods' blocks. Its blocks do not suffocate targets,
block player movement, or drop farmable slime items. Large mobs requiring more
than 64 cocoon blocks cannot be encased.
Underwater or fully obstructed targets may have no air available for a cocoon.

Cocoon expiry and the original air variant are saved in block-entity NBT.
Minecraft's persistent scheduled block ticks clean up the slime after reloads;
no permanent `NoAI` or `NoGravity` flags are changed. Breaking or removing the
cocoon lets the normal status-effect system release the mob. Unloaded chunks
do not simulate normally; overdue slime is cleaned when the chunk ticks again.

Entity immunity can be extended by a data pack using
`data/slimegun/tags/entity_type/immune_to_slime.json`.

## Build and edit

Readable, commented Java sources are in `src/main/java/dev/slimegun`.

On this computer, **Build.bat** uses the project-local Java 21 and Gradle. It builds
the remapped mod, runs unit tests and real Minecraft GameTests, and creates:

- `dist/slime-gun-1.0.0.jar` — install this file, not the sources JAR.
- `dist/Slime-Gun-Fabric-1.21.1.zip` — shareable mod + README + license.
- `build/libs/slime-gun-1.0.0-sources.jar` — Java sources for development.

For a fresh checkout on Windows:

```powershell
./Tools/Setup.ps1
./Build.ps1
```

Setup downloads Temurin Java 21 and Gradle 8.14.3 into `.runtime`, verifies their
SHA-256 checksums, and leaves system Java/PATH unchanged. With a suitable JDK
already installed, the standard wrapper also works:

```powershell
./gradlew.bat build
```

On Linux/macOS use `./gradlew build` with Java 21. The Gradle wrapper downloads its
pinned distribution automatically. **Run-Dev-Client.bat** starts Fabric's development
client for manual graphics testing; it may download Minecraft assets on first run.

## Source map

- `SlimeGunItem`: authoritative aiming, ammo, cooldown, particles, durability.
- `CocoonService`: target eligibility and air-only placement.
- `SlimeCocoonBlock` / `SlimeCocoonBlockEntity`: breakable shell and persistent cleanup.
- `SlimeboundEffect` / `mixin`: timed freezing, AI pause, movement and creeper fuse control.
- `FreezeRules`: geometry and timing shared by gameplay and unit tests.
- `src/gametest`: separate Minecraft integration test mod, not included in releases.

See `VERIFICATION.md` for actual build/test results and limits.
This is an independent mod, not an official Minecraft product or Mojang/Microsoft endorsement.

## Bedrock Edition

See the [Bedrock add-on](../bedrock/README.md).
