# User requirements

Create a Java Minecraft mod locally in
`D:\joshu\Documents\programming\github\minecraft-mod\java`.
The user explicitly chose **Fabric / Minecraft Java 1.21.1**. The mod adds a slime
gun that freezes enemies in blocks of slime. Keep the Java code readable with
useful comments and provide a built installable JAR.

Current behavior: 28-block ray shot, one slimeball, one-second cooldown, 384
durability; 8-second breakable translucent cocoon; repeated shots refresh it.
Do not overwrite existing blocks or permanently disable mob AI/gravity.
Expiry and original air type are persisted with block entities. Real Minecraft
GameTests are isolated in their own source set and excluded from the release JAR.

Use Java 21, Fabric Loom 1.10.5, Yarn 1.21.1+build.3, and the pinned Fabric versions
in gradle.properties. Build.ps1 configures the project-local toolchain and runs
both unit and Minecraft tests through Gradle build. Do not publish/upload the mod
or install into the user's Minecraft profile unless asked.
