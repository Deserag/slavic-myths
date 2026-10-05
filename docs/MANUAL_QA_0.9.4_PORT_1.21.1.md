# Manual QA — 0.9.4 / NeoForge 1.21.1

Port incomplete: none of these checks is PASS yet. Use a new 1.21.1 test world;
do not open an original 1.16.5 save. Compilation/JVM persistence checks do not
verify rendering, actual logout/death, gameplay, registry freeze or server boot.

Record each result as PASS, EXPECTED LEGACY BUG, or PORT REGRESSION with a log.
Expected legacy issues are listed in the package's POST_PORT_BACKLOG_DO_NOT_FIX_YET.
The recent 0.9.3 Yaga placement/egg hotfix is part of the baseline, so its loss
is a PORT REGRESSION; the stale package references to Y>4/planned Y=0 are historical.

| Area | Required manual check | Current result |
|---|---|---|
| A — Boot | Java 21, NeoForge, Curios; new world, save/reopen, no registry/data errors | NOT TESTED |
| B — Items/blocks | All inventory IDs, correct stacks/attributes/drops, unchanged textures/models | NOT TESTED |
| C — Curios | Head/necklace/ring×2/belt/charm; equip effects and persistence | NOT TESTED |
| D — JEI | All four categories; optional JEI absent boot; no missing catalysts/recipes | NOT TESTED |
| E — Hunt | All four targets, eggs, horn conditions, unloaded UUID, owner/cooldown/loot/pouch | NOT TESTED |
| F — Bosses | Likho/Tugarin phases, orientation, rituals, anchors and seven-day respawn | NOT TESTED |
| G — Kurgans | Three tiers, full saved graph, tomb seal, encounters, disturbance and curses | NOT TESTED |
| H — Bandits | Camps/large bases, fixed roster, Solovey, saved death state, old loot | NOT TESTED |
| I — Water | Pools, mobs, plants, fishing, nets, food and existing swamp structures | NOT TESTED |
| J — Flight | Broom/mortar, mounted movement, cargo, Tailwind, 60/30 tick recovery | NOT TESTED |
| K — Yaga | Canonical hut, flat-world generate, egg NPC, intro, favor, quests/exchange/cauldron/klubok | NOT TESTED |
| L — Commands | Existing /slavicmyths trees, permissions, diagnostics and unavailable syntax | NOT TESTED |
| M — Furniture | Rack/wardrobe/signs/seat; reproduce legacy issues separately from new crashes | NOT TESTED |
| N — Server | Bounded dedicated server boot, registry/data load, multiplayer/server-authoritative actions | NOT TESTED |

No client launch is authorized for the automated port. Once a complete production
build succeeds, a bounded headless dedicated-server smoke may be performed in a
separate test directory; an EULA/setup limitation must be reported explicitly.
