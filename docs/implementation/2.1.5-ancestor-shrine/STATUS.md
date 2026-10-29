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

## Native-start QA follow-up (source added, result pending)

- [Commit `5f2d35f`](https://github.com/Deserag/slavic-myths/commit/5f2d35f83185bc42567362629ccdbd64d6cf33ba) adds `actualNaturalAncestorShrine` to `tools/hell-qa-runtime/AncestorShrineQaTests.java`. It requires an accepted native start with physical chest, block entity, floor and entrance; a theoretical candidate is not PASS.
- [Run 40842749940](https://github.com/Deserag/slavic-myths/actions/runs/40842749940) and [run 40842977819](https://github.com/Deserag/slavic-myths/actions/runs/40842977819) were cancelled by later concurrent source pushes before GameTest. Thus **native-start test NOT_RUN** as of this checkpoint; no PASS is claimed. The earlier geometry and loot tests did PASS (run 40842160665).

## 29.10.2026 — corrected native generation (supersedes pending/failed checkpoints above)

Java21 clean build and resource/production-JAR checks PASS. Five fresh disposable headless worlds, 17/17 each (85/85); [logs/coordinates/hash](native-20261029/RESULTS.json). This is actual native placement, not dev-create.

Terrain probes now consider all legal same-chunk footprints, grade at most two extra natural layers, use actual stable footing and biome guards, and reject unsafe starts with locate-cache invalidation. Finite pre-write local fitting avoids otherwise safe small surface offsets. Exact A644/B604/C370 geometry stays unchanged. Native DeltaFeature/BasaltColumnsFeature control tests reproduce and prevent late destruction of crafted floors; natural terrain remains eligible. Final fungal decoration clears only authored passage flora in fresh chunks.

Calibrated rare baseline: spacing24/separation9/frequency0.8/salt21015021, restricted to basalt_deltas/soul_sand_valley/black_cinder. Earlier32/11/0.6 produced physical starts in3/5 bounded searches; final24/9/0.8 produced5/5, searching7/8/25/34/76 candidates. These bounded first-hit searches are not a density census or a survival discovery test. Balance remains subject to owner acceptance.

| Requirement | Evidence | Result |
|---|---|---|
| Exact three source designs × rotations | native geometry test; original TSV arrays | PASS |
| Physical natural starts on five seeds | native-20261029 logs | PASS |
| Native loot, no repeat unpack | 1000 native rolls per world | PASS |
| Delta/basalt neighbour protection | real native feature control tests per world | PASS |
| Build/resources/JAR | build.log/resources.json | PASS |
| Client visuals, survival discovery, old-world copy, true restart, MP, MSPT/tickets census | no such run | NOT_RUN |

Owner checklist: use a fresh survival Nether without OP; discover a shrine, enter from the south, open the side chest, remove contents, leave/save/rejoin and verify it remains empty. Repeat on a copy of an old world in new chunks and with two players. Check basalt-delta floors and entrances stay clear after neighbour generation. Diagnostic only: `/locate structure slavicmyths:ancestor_shrine` finds rather than constructs. Rollback requires the backed-up previous JAR and a world backup; new chunks already generated are not undone by changing JARs. No owner acceptance asserted.
