# PHASE 1 — INVENTORY + BUILD SHELL
## Use together with MASTER prompt

Goal:
Make project a real Minecraft 1.21.1 NeoForge / Java 21 project and produce complete migration inventory.

Do:
1. Read build.gradle, gradle.properties, settings.gradle, META-INF metadata.
2. Generate registry inventory before deleting old registration code.
3. Replace Gradle/MDK stack with current stable NeoForge 1.21.1 configuration.
4. Update wrapper/toolchain to Java 21.
5. Create neoforge.mods.toml.
6. Establish mod constructor/event buses.
7. Port only enough registry skeleton to expose compile errors system-by-system.
8. Create docs/port/PORT_STATUS_0.9.4.md.
9. Do NOT stub away content permanently.
10. Do NOT run client.

Exit criteria:
- Gradle configuration resolves.
- Java 21 recognized.
- NeoForge 1.21.1 dependencies resolve.
- registry inventory exists.
- project compilation reaches real source migration errors rather than build-system failure.
