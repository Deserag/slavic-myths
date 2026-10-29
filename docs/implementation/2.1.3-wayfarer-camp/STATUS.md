# 2.1.3 «Стоянка странника» — технический checkpoint

**Дата:** 2026-10-29 · **Ветка:** `hell` · **Issue:** [#341](https://github.com/Deserag/slavic-myths/issues/341) (**OPEN, owner acceptance pending**) · **Версия:** `2.1.3-HELL`

## Источник дизайна и границы
Обязательные проверенные референсы: [SPEC](../../concepts/nether-structures/2.1.3_WAYFARER_SPEC.md), [BLUEPRINT SVG](../../concepts/nether-structures/2.1.3_WAYFARER_BLUEPRINT.svg), [VARIANTS SVG](../../concepts/nether-structures/2.1.3_WAYFARER_VARIANTS.svg), [PLACEMENT LAYERS SVG](../../concepts/nether-structures/2.1.3_WAYFARER_PLACEMENT_LAYERS.svg), [CONCEPT](../../concepts/nether-structures/2.1.3_WAYFARER_CONCEPT_DRAFT.md).
The approved artistic PNG is **not confirmed present in GitHub**. SVG files are drawings, not runtime screenshots.

Only existing registered vanilla blocks and items, no extra mobs, NPC, barrels, custom items, ores or rewards. Structure attempts only in native new-chunk Nether generation; no retro-generation, event-triggered player command or `/locate` mutation.

## Source implementation
- `WayfarerWorldgen`, `WayfarerStructure`, `WayfarerTerrain`, `WayfarerPiece`: native registered ID `slavicmyths:wayfarer_camp`, custom Java piece with persisted NBT plan (not an authored binary .nbt template), deterministic per-world seed/chunk 3 variants (A/B/C) and four rotations, 9×7×6 physical footprint.
- `worldgen/structure/wayfarer_camp.json` + `worldgen/structure_set/wayfarer_camp.json`: Nether biome tag includes five vanilla Nether biomes and four present Slavic Myths Nether biomes; spacing 22, separation 9, salt 20213013, frequency 0.8 are **provisional balance**, not separately accepted by owner.
- Strict local noise-column 3D preflight: 6-block clearance, natural material under every actual foundation pad, no fluid/bedrock below, no chunk tickets. Piece stays in its origin chunk, rechecks target voxels before any writes and refuses protected nonreplaceable voxels or block entities.
- Architectural layout: exactly one vanilla chest (3,1,2), crafting table (6,1,2), one campfire (4,1,5) with fireproof blackstone base (4,0,5), crimson frame/roof/low blackstone wall, stone seats (3,1,5),(5,1,5), hanging lantern (1,2,4) and chain (1,3,4) under real cantilever (1,4,4). Variant B mostly intact; A partially ruined; C heavily ruined. Guaranteed accessible chest headspace and alternative bypasses around the campfire.
- Standard `data/slavicmyths/loot_table/chests/wayfarer_camp.json`: guaranteed weighted food 50:30:20 (pork 2–4, bread 2–4, potato 3–6), guaranteed supplies 60:30:10 (torch 3–7, arrow 4–10, fire charge 1–2). Six independent optional groups: flint-and-steel 18%, string 30%, iron 13%, gold nuggets 25%, obsidian 8%, golden carrot 5%; no prohibited premium loot. No event award or opening-triggered reseeding.
- Automated tests `tools/hell-qa-runtime/WayfarerQaTests.java`: 12 source geometry/rotation profiles, 1,000 *actual chest loot-table unpack* operations and no second payout on repeated unpack, actual native Nether chunk structure start and physical blocks. Added alongside 2.1.1/2.1.2 and existing Hell regressions.

## Verified build and evidence

- [CI run 40840706388](https://github.com/Deserag/slavic-myths/actions/runs/40840706388): **PASS** clean production `gradlew clean build` (Java 21, MC 1.21.1, NeoForge 21.1.255), `PASS` resource verification; **FAIL** QA harness compilation at test source line 131, corrected later in [88a22b9](https://github.com/Deserag/slavic-myths/commit/88a22b9c142c3a2a2cfc327208b0826d400d9a7b). This CI does **not** establish GameTest pass.
- Initial production JAR for run 40840706388: `build/libs/slavicmyths-2.1.3-HELL.jar` (within workflow artifact); SHA256 **`66ab5af0aee47e8157b22c032ea5d5978f7c643dcd3295a78de9262488d6c58b`**. The final post-test JAR hash must be confirmed again on the final HEAD.
- Follow-up [CI run 40841112330](https://github.com/Deserag/slavic-myths/actions/runs/40841112330): **FAIL, 17/19 GameTests passed**. The source geometry test passed all 12 variant/rotation profiles. The native chest test passed 1,000 unpack operations: optional item occurrences [181,301,127,255,72,40], both guaranteed groups on all 1,000, no second payout on immediate repeat-open. The two failures: natural broken bridge deck contained `minecraft:weeping_vines` at (211,40,-807), and a native wayfarer start was valid but postProcess wrote zero expected blocks at chunk [75,141] (seed 20261024). Later patch [c4a1452](https://github.com/Deserag/slavic-myths/commit/c4a145241aa096655b55662bb9fbf5ef6ba1a4c9) handles Nether vines/nylium and enables an isolated diagnostic; **unverified pending fresh CI**.
- PolyMC: **NOT INSTALLED**. GUI images: **BLOCKED_ISOLATION** (no demonstrated separate graphics desktop).

## Requirements/acceptance matrix (initial; update from actual CI only)

| REQUIREMENT | SOURCE / EVIDENCE | STATUS |
|---|---|---|
| Source permissions, no `main`, no force | `hell` commits and owner scope | PASS |
| Native ID, expected biome tag and structure set | `wayfarer_camp` JSON/Java source; JAR resources | PASS (static/build) |
| 3 ruins × 4 rotations, 9×7×6; attachment/clearances | run 40841112330: 12 source profiles PASS | PASS (source geometry only) |
| Physical one chest/fire/table, actual new-chunk natural start | run 40841112330: valid native start, 0/5 functional blocks at chunk [75,141], seed 20261024 | FAIL (retest c4a1452 pending) |
| 1000 chest loot generations, optional pools, no repeated unpack | run 40841112330: 1000 PASS, extras [181,301,127,255,72,40] | PASS (in-process; save/restart separate) |
| At least five actual different world seeds, coordinates, variant/biome, accepted/rejected candidate counts | independent fresh-world CI runs | NOT_RUN |
| 3D cave/roof/lava/bedrock, edge chunks, actual ground access | generation preflight source; additional runtime scenarios required | NOT_RUN (runtime) |
| Protected vanilla/mod structures, claims, player builds | conservative bounded candidate check + placement guard; in-world negative tests | NOT_RUN |
| Save/restart, no loot duplication after save, two-player MP | isolated persistent world and MP | NOT_RUN |
| `/locate` read-only; survival without OP | native registration source; live check | NOT_RUN |
| 2.0, 2.1.1, 2.1.2 targeted headless regression | run 40841112330: bridge physical deck FAIL on weeping vines; others passing | FAIL (2.1.2); follow-up CI pending |
| Render compared with approved SVG, owner visual + survival acceptance | no GUI/owner evaluation | BLOCKED_ISOLATION / OWNER_PENDING |
| Repackaged JAR source hash / checksum for final HEAD | latest CI checksum | NOT_RUN |

**Known limitation:** actual runtime plus all 5 seeds, all biome types, save/restart, client, MP, and full player-structure protection have not been demonstrated by a single passing source build. Do not mark `DONE` or `OWNER_ACCEPTED` or close Issue #341 until explicit owner decision. No work on 2.1.4+, 2.1.8 or 2.1.9.

## Independent five-seed checkpoint: 40842359154

The [five-seed run](https://github.com/Deserag/slavic-myths/actions/runs/40842359154) executed **five actual independent new Nether test worlds** at JAR source `6f340715`. This run **FAILS overall**; the five seed-specific logs must not be conflated with five accepted camps:

| World seed | Actual wayfarer result | Candidate/start, biome, Y | Other failures |
|---|---|---|---|
| 20261025 | **PASS**, one real natural and physically furnished camp | chunk [-32,-12], `minecraft:nether_wastes`, Y37, variant A (0), sample 7, accepted 1 | None: all 21 GameTests passed |
| 20261026 | **PASS**, one real natural and physically furnished camp | chunk [-78,51], `minecraft:nether_wastes`, Y48, variant A (0), sample 46, accepted 1 | Broken bridge deck crossed a crimson stem; seed-specific boss test failed |
| 20261024 | **FAIL**, valid native start but physical postprocess refused tree collision | chunk [75,141], position (1209,52,2267) occupied by `minecraft:crimson_stem` | Broken bridge passage blocked |
| 9132026 | **FAIL**, valid native start but physical postprocess refused tree collision | chunk [144,-32], position (2312,76,-503) occupied by `minecraft:crimson_stem` | No other GameTest failure |
| 3412026 | **FAIL**, **zero** accepted natural camp plans among 140 allowed candidate cells | no accepted chunk/start/biome, no physical camp | Seed-specific boss regression test also failed |

All tested runs passed 12 static variant/rotation profiles and 1,000 real vanilla loot-table generations, but this is not five natural-placement passes. [Commit 258c7f1](https://github.com/Deserag/slavic-myths/commit/258c7f15490070566c448361ab1e87deee3f9926) extends bounded 3D site search and permits replacing naturally generated fungal stems in fresh chunks; [61727ae](https://github.com/Deserag/slavic-myths/commit/61727ae7d6b143a930feff96d671b85fdde9a5e7) strengthens mandatory real rock underneath walking bypass corridors and clears their floor-level vegetation. Re-tests of the later code are **NOT_YET_VERIFIED** as of this checkpoint.

The five-seed workflow ran the **whole** Hell QA suite; some older boss tests assume the fixture world seed and may fail on other seeds independently of the camp. They are shown above instead of silently suppressed. Detailed protected structures, restart, MP, permissions, visual, and full survival owner checks still NOT_RUN / BLOCKED_ISOLATION.
