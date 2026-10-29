# Slavic Myths 2.1.5 — Ancestor Shrine (code checkpoint, NOT accepted)
Date: 2026-10-29. Working branch: `hell`; `main` untouched.
Owner's latest request authorizes **code implementation** of 2.1.5; graphical details and final
game acceptance remain owner-controlled. No GitHub Issue has been created or closed.

## Ground truth and source
[Owner-review concept](../../concepts/nether-structures/2.1.5_ANCESTOR_SHRINE_CONCEPT_DRAFT.md),
[exact visual blueprint](../../concepts/nether-structures/2.1.5_ANCESTOR_SHRINE_BLUEPRINT.svg),
[three TSV voxel maps](../../concepts/nether-structures/2.1.5_ANCESTOR_SHRINE_VOXELS_A.tsv);
B and C use the matching suffix; see concept for direct links.
The implementation embeds the exact 644/604/370 authored cells; no inferred blocks added.
The three states have a 13×15×11 one-chunk AABB in all four orientations, one accessible
chest at x=8,y=2,z=5, matching hanging soul lanterns, wall symbols made of existing chiseled
blackstone, and an unblocked southern opening. No new custom blocks/items/mobs/rituals.

## Registration and game path
`AncestorShrineWorldgen` registers structure and piece types with existing registries;
`SlavicMyths.java` initializes registration; datapack `structure/ancestor_shrine.json`,
`structure_set/ancestor_shrine.json`, and `tags/worldgen/biome/ancestor_shrine_biomes.json`
restrict spontaneous creation to Nether biome IDs basalt_deltas, soul_sand_valley and
slavicmyths:black_cinder. No retrogen and no building from `/locate`.
Engineering-only frequency baseline: spacing42/separation14/salt21015021/frequency0.38.
Variant weights 25%/45%/30%, **owner balance pending**.
Native chest's lazy `chests/ancestor_shrine` table uses only verified registered items.
Chest contents are never force-reset after opening.

## Defensive placement
Only a new candidate with 3D noise-column air clearance, five genuine ground anchors and
at least 40 natural supports within the 195-column horizontal footprint is admitted.
Only one chunk is touched. Before writes, postProcess checks ALL proposed block locations,
walking aisle and chest lid for replaceable foliage/air and protects foreign block entities.
On collision it aborts BEFORE any writes (does not claim to roll back third-party writers).
Placed blocks are idempotent; rotation and variant persist in native NBT. No chunk tickets,
whole-world tick polling, lava ocean filling, bedrock cutting or special NPC spawns.

## Verification matrix
| Required check | Status | Evidence / next action |
|---|---|---|
| 3 models, 4 rotations, exact geometry | SOURCE READY / runtime NOT_RUN | AncestorShrineQaTests (12 profiles), native actual world |
| Chest table and real rewards | SOURCE READY / runtime NOT_RUN | 1,000 actual loot rolls + one chest open/relog |
| Java21 clean build, JSON, JAR | NOT_RUN | GitHub Actions headless workflow after push |
| Three allowed biomes + forbidden negatives | NOT_RUN | 5+ world seeds, biome IDs and accepted starts |
| Natural formation; terrain, entrance, lava, ceiling | NOT_RUN | actual generated chunks and client screenshots |
| No structure overwrites, find-only locate, cancel, save and restart | NOT_RUN | isolated server QA and old-world copies |
| Stable MSPT, MP and 2 clients | NOT_RUN | isolation mandatory, no active desktop interference |
| Owner accepts look and final release | OWNER_PENDING | actual in-game views versus published SVG |

**Risks:** 13×15 flat clearance may be rare in rough Nether terrain; measure *accepted*
native starts rather than theoretical candidates and tune only with owner review.
Loot utility must be checked in survival, including coin/charcoal. The existing
2.1.1–2.1.4/2.1.6 tasks are not marked complete by this independent checkpoint.
No personal desktop game client or owner's PolyMC instance was touched.

## Verified headless CI checkpoint — 29.10.2026

- Exact tested source: [commit `3b18ee4a8aa82fd4bb4c251d1b6896933680a731`](https://github.com/Deserag/slavic-myths/commit/3b18ee4a8aa82fd4bb4c251d1b6896933680a731), [workflow run 40842160665](https://github.com/Deserag/slavic-myths/actions/runs/40842160665).
- Java 21 `./gradlew --no-daemon clean build`: **PASS** (GitHub Actions clean build in 1m11s).
- `tools/verify_current_resources.py`: **PASS**; production JAR version still `2.1.3-HELL` (not relabeled 2.1.5 as a release). 8718 resources, 4963 JSON, 2176 Java 21 classes. JAR SHA256 `c8b414d2f63cb782e2ebd89bf8c1724b0d2011a070354a89c9e70abccfb8d108`.
- Headless packed-server GameTest `exactVoxelGeometryTwelveProfiles`: **PASS**. Twelve geometry variants = 3 designs × 4 rotations; exact source counts A644/B604/C370, one chest, unblocked entrance/lid, chains, hanging lanterns and memory stones.
- Headless packed-server GameTest `chestLoot1000AndNoDuplication`: **PASS**. 1000 real native loot-roll trials: bone in 1000/1000, soul torches in 549/1000; repeated `unpackLootTable` did not reroll.
- Full workflow status **FAIL**, **not** due to either 2.1.5 test: existing `actualnativenetherlavabridge` failed because 5-lane headroom was blocked; existing `actualnativenetherwayfarer` found no physical camp components in accepted start; existing `realnetherbiomeandoceanchunk` did not find required natural boss. These failures are separately scoped regressions. Never call the full QA green.
- Real spontaneous **2.1.5** native start + full physical structure in 5+ seeds/biomes, survival without OP, portal/other-structure proximity, save/restart, player screenshots, MP/FPS/MSPT: **NOT_RUN**, **OWNER_PENDING**. No claims of natural worldgen acceptance, even though the codec/resources and offline GameTests pass.
- Visual concept/voxel consistency: separate direct TSV-to-Java source audit compared all 1,618 authored block coordinates and palettes exactly: **PASS as a static source comparison**, not game rendering.

**Remaining implementation QA:** add and run a dedicated actual-natural-world `ancestor_shrine` test, inspect Nether terrain/black_cinder custom surface feature interactions, measure accepted starts and rejected candidates across at least five seeds, and resolve any newly demonstrated 2.1.5-specific failures without expanding other patches' scope. Owner must still approve real in-game visuals.

This checkpoint must stay **WIP / IN_PROGRESS**, not owner accepted or complete. No new task/Issue is automatically started.
