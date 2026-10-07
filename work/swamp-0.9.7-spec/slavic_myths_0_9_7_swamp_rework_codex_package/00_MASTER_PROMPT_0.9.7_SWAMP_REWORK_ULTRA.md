# SLAVIC MYTHS — 0.9.7 SWAMP REWORK
## ULTRA-DETAILED CODEX IMPLEMENTATION SPECIFICATION
### Target: Minecraft 1.21.1 / NeoForge / Java 21
### Mod ID: `slavicmyths`
### Repository: https://github.com/Deserag/slavic-myths

---

# 0. HOW TO TREAT THIS PACKAGE

This is a strict implementation package, not a list of vague ideas.

Codex must read all files in this package and use the included reference images.
Do not improvise broad replacement concepts.
Do not replace the user-approved swamp direction with a different biome concept.
Do not create a separate custom swamp biome.

The user approved the following high-level direction:
- keep ONLY vanilla swamp as the base biome target;
- place new content inside vanilla swamp instead of maintaining a separate custom swamp biome;
- make swamp content useful, rewarding, and atmospheric;
- improve trees and nature in the swamp update;
- improve swamp structures, their meaning, and their rewards;
- improve swamp mobs because they are currently too weak and underwhelming;
- improve swamp ores / decorative blocks / water behavior where it helps the update.

---

# 1. VERSION GOAL

0.9.7 is a full rework of swamp content inside VANILLA swamp.

The version must answer these current weaknesses:
1. Swamp mobs are too weak.
2. Swamp structures feel unclear, empty, and often pointless.
3. Swamp rewards and interaction loops are too weak.
4. Trees and swamp nature need a higher-quality visual language.
5. Some ores / natural blocks do not look integrated enough.
6. Waterline / damp detail / marsh atmosphere need stronger implementation.
7. The vanilla swamp should feel richer without losing recognizability.

Desired end state:
> A player entering vanilla swamp should immediately feel that Slavic Myths has transformed the biome into a richer, more atmospheric and more rewarding ecosystem, while still clearly remaining vanilla swamp.

---

# 2. HARD SCOPE

0.9.7 MUST include:
- swamp content only inside existing vanilla swamp (and compatible variants if needed, but no separate custom swamp biome);
- new and/or reworked swamp structures and POI;
- reworked swamp loot and purpose;
- reworked swamp mob roles and durability;
- reworked swamp tree generation and nature details;
- improved swamp materials / ores / decorative natural blocks where justified;
- better water-adjacent composition and marsh atmosphere;
- documentation and manual QA.

0.9.7 MUST NOT become:
- a total whole-world flora overhaul;
- a full all-biomes tree overhaul;
- a global rebalance of every mob in the mod;
- a full progression audit for all versions;
- a separate swamp dimension or new full biome family.

---

# 3. BIOME POLICY

IMPORTANT:
Use VANILLA SWAMP as the biome target.

Implementation direction:
- inject structures, features, mobs, plants and ambient content into vanilla swamp;
- preserve recognizability of Minecraft swamp;
- do not delete vanilla swamp identity;
- do not replace the whole biome with a totally different color language.

Possible targets:
- `minecraft:swamp`
- optionally compatible handling for nearby marsh-like wet areas if the project already has relevant logic,
  but the primary target remains vanilla swamp.

No separate Slavic Myths swamp biome should be required for this version.

---

# 4. REFERENCE IMAGES

You MUST inspect and use the bundled reference images:
- `references/ref_01_swamp_structures_sheet.png`
- `references/ref_02_swamp_rework_overview.png`
- `references/ref_03_swamp_trees_sheet.png`
- `references/ref_04_swamp_structures_alt_sheet.png`

These references define:
- structure silhouette and mood;
- the desired swamp settlement / ruin language;
- the tree forms and silhouettes;
- the expectation that the swamp remains atmospheric and readable in Minecraft style.

---

# 5. STRUCTURE PROGRAM

The swamp update must include a set of useful, understandable swamp locations.
Each one needs:
- a clear silhouette;
- a clear reason to exist;
- a gameplay function;
- structure-appropriate rewards;
- sensible generation in/around swamp water.

Use the structure set described in `01_SWAMP_STRUCTURES_AND_PURPOSES_ULTRA.md`.

---

# 6. MOB PROGRAM

Swamp mobs are currently too weak.
That must be corrected in 0.9.7.

Codex may increase HP, improve AI roles, improve encounter design, and refine visual presentation if needed.
The goal is NOT absurd stat inflation.
The goal is to make swamp encounters feel legitimate and memorable.

Use the detailed requirements in `02_SWAMP_MOBS_ROLES_AND_REBALANCE_ULTRA.md`.

---

# 7. NATURE PROGRAM

0.9.7 must also rework the swamp nature language.
Key targets:
- swamp willow;
- crooked swamp oak;
- tall bog pine;
- dead dry tree;
- reeds / cattails / marsh clutter / roots / damp ground dressing;
- better visual transitions at water edges.

Use the exact direction in `03_SWAMP_TREES_NATURE_AND_FEATURES_ULTRA.md`.

---

# 8. BLOCKS / ORES / NATURAL MATERIALS

If swamp blocks and swamp ores are touched, they must be improved to fit Minecraft better.

Rules:
- ores must read like Minecraft ores, not isolated drawn icons;
- natural damp/moss blocks should be usable and readable;
- decorative blocks should support swamp structures and world detail;
- avoid content bloat with many redundant blocks.

Use `04_SWAMP_BLOCKS_ORES_AND_MATERIALS_ULTRA.md`.

---

# 9. LOOT / PURPOSE / REWARD LOOP

Current complaint: structures have little point.
Therefore every swamp structure must offer at least one of:
- useful loot;
- lore/ritual value;
- crafting materials;
- route marker / navigation value;
- mob encounter value;
- unique swamp utility.

Use `05_LOOT_REWARDS_AND_INTERACTION_LOOPS_ULTRA.md`.

---

# 10. WORLDGEN / WATER / INTEGRATION

Swamp features must actually sit well in marsh terrain.
Focus on:
- waterlogged composition where appropriate;
- clean interaction with shallow water;
- posts/platforms/roots that visually justify wet placement;
- not floating awkwardly;
- not cutting harsh holes into swamp.

Use `06_WORLDGEN_WATER_AND_SPAWN_INTEGRATION_ULTRA.md`.

---

# 11. NO CLIENT AUTO-LAUNCH

Do not run Minecraft client.
Allowed:
- build;
- datagen/resource verification;
- structure/worldgen tests if headless;
- manual QA checklist creation.

---

# 12. DOCUMENTATION

Create/update:
- `docs/swamp/SWAMP_0.9.7_STRUCTURES.md`
- `docs/swamp/SWAMP_0.9.7_MOBS.md`
- `docs/swamp/SWAMP_0.9.7_TREES_AND_NATURE.md`
- `docs/swamp/SWAMP_0.9.7_BLOCKS_AND_ORES.md`
- `docs/swamp/SWAMP_0.9.7_LOOT.md`
- `docs/MANUAL_QA_0.9.7_SWAMP.md`

---

# 13. DEFINITION OF DONE

0.9.7 is complete only when:
- content is injected into vanilla swamp rather than depending on a separate swamp biome;
- swamp structures are understandable and worthwhile;
- swamp rewards are improved;
- swamp mobs are stronger and more interesting;
- tree silhouettes are improved and varied;
- swamp nature feels richer;
- ores/materials touched by the update read better visually;
- structure generation in water is convincing;
- documentation and QA checklist are updated;
- build passes.
