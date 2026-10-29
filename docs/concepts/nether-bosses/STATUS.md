# Slavic Myths 2.1.x — checkpoint summoned bosses (2026-10-29)
## Status: PARTIAL IMPLEMENTATION / NOT PLAYABLE OR OWNER-ACCEPTED
Work isolated in branch `codex/hell-bosses-morova-trizna`, based on `hell`. Issues: [#353](https://github.com/Deserag/slavic-myths/issues/353) (Morova) and [#354](https://github.com/Deserag/slavic-myths/issues/354) (Trizna).
**Current code prepared:**
- Two custom humanoid mob entity types `slavicmyths:morova_deva`, `slavicmyths:trizna_prince` using `NetherSummonBoss`, dimension and damage parameters, basic vanilla-style AI, server-side phase state, boss bar, telegraphed prototype AoE damage.
- Client registration of separate `NetherSummonBossRenderer`, direct custom `NetherSummonBossModel` with generated 53/54 model cubes and 128x256 PNG palette atlases under `textures/entity/`.
- Editable source .bbmodel with embedded 128x128 pixel atlases, plus separate PNG files under docs/concepts/nether-bosses/models. Morova geometry is 2.65 blocks incl. crown; Prince 3.0 blocks incl. crown.
- Separate and shared detailed design specs with agreed owner art, ordinary legendary soul ID, accessories vs unique runes, loot logic.

**Important — NOT YET IMPLEMENTED / NOT TESTED:**
- Gameplay summons / recipe ingredients / Nether rituals and structures integration;
- all 8 component items, 2 summon items, 2 wearable accessories, 2 runes, 1 generic legendary soul, 2 unique scrolls, all items' textures and recipes;
- real special attacks and subordinate minions with bespoke models; separate mechanic of Silence; full phase animations, bespoke SFX/OGG, advanced VFX and lighting;
- actual boss death loot roll / Looting distribution and reward persistence;
- item and entity localization (large language files not safely edited yet);
- build and Minecraft runtime testing; actual in-game visual matching and owner QA.

**Engineering caution:** In this stage, original models are available and renderer code references them, but there has been no successful Java 21/NeoForge compilation or Minecraft visual QA. Do not merge into `hell` or declare work complete. Next implementation phase must run compile and fix issues before adding further systems. Confirm texture UVs & alpha/glow effects against approved player poster and make final images available directly to executors. Do not claim the uploaded chat raster image itself is inside the repository.
