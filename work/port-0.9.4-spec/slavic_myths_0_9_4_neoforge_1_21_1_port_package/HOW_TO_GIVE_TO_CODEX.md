# HOW TO GIVE THIS PACKAGE TO CODEX

Recommended first message:

`Port the current Slavic Myths repository from Minecraft 1.16.5 Forge to Minecraft 1.21.1 NeoForge according to 00_MASTER_PROMPT_0.9.4_NEOFORGE_1.21.1.md. This is a migration-only version: preserve content, registry IDs, visuals and gameplay; do not fix the post-port backlog unless something blocks the port. Work gate-by-gate, do not run Minecraft client, and keep docs/port/PORT_STATUS_0.9.4.md updated.`

Token-saving workflow:
1. Give Codex the whole package.
2. It should read the MASTER first.
3. It should read only the phase file for the current gate.
4. It should not re-read all phase files every turn.
5. If context becomes tight, continue in the same repo using PORT_STATUS as handoff state.

Useful phase files:
- 01_PHASE_BUILD_INVENTORY.md
- 02_PHASE_DATA_CONTENT.md
- 03_PHASE_ENTITIES_UI_NETWORK.md
- 04_PHASE_WORLDGEN_PARITY.md

Do not tell Codex to implement POST_PORT_BACKLOG during 0.9.4.
