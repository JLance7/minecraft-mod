# Verification — Fabric / Minecraft 1.21.1

## Passed

- Java 21 compilation against Yarn 1.21.1+build.3 and Fabric API 0.116.17+1.21.1.
- Fabric Loader 0.20.1 loaded the mod and all three required mixins in an actual
  Minecraft game-test server.
- **7 JUnit unit tests** passed: humanoid coverage, exclusive upper bounds,
  negative coordinates, boundary-straddling cells, placement limit, refresh
  deadlines, and bounded cleanup delays.
- **9 Minecraft GameTests** passed in the real engine:
  - Freeze holds a moving mob in place and expires without persistent AI/gravity changes.
  - Breaking the cocoon releases the mob early.
  - Repeated encasing extends the timeout and still cleans up later.
  - Expiry and the original cave-air variant survive NBT serialization.
  - A primed creeper's fuse pauses while frozen.
  - An existing chest and its 16 diamonds survive encasing unchanged.
  - An aimed gun shot hits, spends ammo/durability, enforces cooldown, and refreshes through existing slime.
  - A stone wall blocks the shot.
  - An empty gun cannot fire or lose durability.
- `gradle build` succeeded, including the production `remapJar` task. The installable
  artifact is `dist/slime-gun-1.0.0.jar`, not the sources JAR.
- Generated the standard Gradle wrapper with a pinned distribution SHA-256.

The integration tests use temporary test structures, not any player save.
The desktop Minecraft client and final item/block appearance have not been
manually tested in a player session. No existing Minecraft profile was modified.

The build report is `build/reports/tests/test/index.html`; the retained log
`Verification/release-build.log` records all nine real Minecraft tests passing.
