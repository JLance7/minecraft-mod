# Slime Gun for Bedrock

Version 1.1.1 targets Minecraft Bedrock 1.21.130 or later and uses stable Script API 2.4.0.

1. Open **dist/Slime-Gun-Bedrock-1.1.1.mcaddon** with Minecraft to import both packs.
2. In your world settings, activate **Slime Gun Projectile Behavior** and check that **Slime Gun Projectile Resources** is active.
3. Find the gun in Creative equipment, craft it, or use `/give @s slimegun:slime_gun` with cheats enabled.
4. Aim at a mob and use the item. On touch controls, tap **Fire Slime Gun**.

Survival shots cost one slimeball and one point of durability. The gun launches a visible slimeball with a one-second cooldown and a range of 28 blocks. Walls stop the shot in flight. It targets living mobs, including tamed pets, the Wither, and the Ender Dragon. Players and armor stands are outside the target set.

```text
I S I
R D S
. . I
```

Use iron ingots for I, slimeballs for S, redstone dust for R, and a dispenser for D. Dots are empty slots.

New cocoons fill available air across the mob's collision bounds and have no timer. Break all the cocoon blocks to release the mob. The movement lock is stored on the mob and restored when it loads again. Cocoons from earlier builds still use their original timer. Terrain and liquids take priority over slime placement. Extended model parts and special boss animations can extend past the collision bounds. Vanilla AI, damage, and special attacks can still run, including creeper fuses. The gun has a 3D model in hand and uses its existing icon in inventory.

To upgrade, exit your world and import 1.1.1. It has fresh pack IDs. In the world settings, deactivate the earlier Slime Gun behavior and resource packs before activating the two Slime Gun Projectile packs.

Run `./Build-Bedrock.ps1` in PowerShell to rebuild the import file. The script was checked against the 2.4.0 API typings. In-game playtesting of 1.1.1 is pending.
