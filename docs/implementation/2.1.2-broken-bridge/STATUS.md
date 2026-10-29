# Slavic Myths 2.1.2 — Разбитый мост (WIP checkpoint, 29.10.2026)

**Owner instruction:** proceed with next micro-patch now, retain `hell` only, final owner acceptance of 2.0+2.1 later. The earlier wait-for-2.0 gate is superseded ONLY for start of development, not acceptance.

## Primary sources available in the repository

- [Approved layout and exact width: 2.1.2_BROKEN_BRIDGE_BLUEPRINT.svg](../../concepts/nether-structures/2.1.2_BROKEN_BRIDGE_BLUEPRINT.svg). Native source file, not an actual screenshot.
- [Full design, palette, worldgen, testing contract](../../concepts/nether-structures/2.1.2_BROKEN_BRIDGE_SPEC.md)
- [Issue #339](https://github.com/Deserag/slavic-myths/issues/339), [Roadmap 2.1](../../concepts/nether-structures/ROADMAP_2.1_2026-10-29.md), [Required QA gates](../../requirements/13-TEST-GATES.md).

The original raster concept from ChatGPT is **not** present in GitHub: `ART_BLOCKED_REFERENCE`. The SVG exists and informs the geometry, but does not prove graphical similarity.

## Committed implementation — initial candidate

- Native `slavicmyths:broken_nether_bridge` StructureType / PieceType, actual structure JSON / independent placement set. Reuses the known 5 vanilla + 4 modded Nether biome tag without changing any existing worldgen. Registry registration is in `SlavicMyths.java`.
- `NetherBridgeTerrain`: bounded 3D noise-column-only preflight (no chunk tickets or world mutation), 9/12/17/23/30/38 block proposed lengths, four cardinal rotations, preferred real lava exposure with two natural-stone banks, safe ceiling clearance, two-sided narrow piers anchored to stone within 24 blocks; no arbitrary ocean filling. Refuse unsafe bedrock, unexpected solid layers or missing anchorage.
- `NetherBridgePiece`: native single multi-chunk piece, chunk-bound write clipping and persistent variant/footing/debris metadata; exactly five solid central lanes on intact spans and two outside guardrail lanes; four visual variants: nearly intact, battered edges, large central breach, fragmented ruins; no new item/block/entity IDs, no loot tables, chests, block entities or NPC.
- Configuration candidate: spacing 26 chunks, separation 10, frequency 0.85, salt 21012021; not owner-approved frequency until accepted starts/seed measurements. Endpoints/lengths/column positions similarly engineering baselines, not newly approved owner dimensions.
- Build version changed to `2.1.2-HELL`. This does not retroactively modify the separate prior 2.1.1 JAR.
- QA source: `tools/hell-qa-runtime/BridgeQaTests.java` covers 64 authored geometry profiles + an actual native (not developer-command) new-chunk bridge with chunk-crossing lane checks and absence of loot.

## Acceptance matrix — preserve pending statuses until evidence exists

| Requirement | Source | Evidence | Status |
|---|---|---|---|
| Native structure set & all known Nether biome IDs | Java / JSON / existing biome tag | Source registration | SOURCE_WIP |
| Four destruction variants and 5 free center lanes | SVG, BridgePiece, BridgeQaTests | 64 GameTest permutations | NOT_RUN until actual CI |
| Actual lava lake and rock-founded piers | BridgeTerrain 3D noise preflight | Natural fresh-chunk GameTest and later actual-client inspection | NOT_RUN |
| No chests, loot, NPC, new IDs | Native geometry and no loot JSON | Source review + GameTest | NOT_RUN until actual CI |
| Four rotations, short/medium/long, chunk-order stability | BridgePiece data & native piece clipping | Tests / manual new-world sampling | NOT_RUN |
| 5+ independent seeds and all 9 biome types, real rates | Issue #339, requirements | Seed / chunk / biome / rejected plans table | NOT_RUN |
| Survival discovery without OP, /locate not building | Native structure registration | Dedicated controlled world | NOT_RUN |
| Vanilla fortress/bastion/portals protection and existing worlds | Conservative clearance and non-retrogen | Repeatable mixed landscape/MP QA | NOT_RUN |
| GUI screenshots and matching original art | Missing original PNG + available SVG | Owner Minecraft inspection | ART_BLOCKED_REFERENCE / OWNER_PENDING |
| FPS/MSPT and true multiplayer/restart | Requirements 12,13 | Dedicated server + client | NOT_RUN |

No owner acceptance, Issue closing, `main` modifications, force-push, user-desktop interactions or PolyMC changes were performed. Do not claim the candidate production-ready from a clean build alone.

## CI evidence and diagnosed natural-worldgen failure

- [Run 40837834811](https://github.com/Deserag/slavic-myths/actions/runs/40837834811): Java 21 build & resources **PASS**, server test source initially had a missing `gz++` in a loop (**TEST_COMPILE_FAIL**). Repaired in `a86883f`.
- [Run 40838225692](https://github.com/Deserag/slavic-myths/actions/runs/40838225692): Java21 clean build, resource scan, and **15 of 16 GameTests PASS** (all previous 2.0/2.1.1 tests plus 2.1.2 sixty-four variant geometry checks). **Natural bridge test FAIL**: seed `20261024`, 130 candidate cells within 13×13 grid, **0 accepted starts**. Never reinterpret code registration as natural placement PASS.
- Follow-up [8530018](https://github.com/Deserag/slavic-myths/commit/853001826e04d64c2c10cad5b1b272afb7075305) broadens safe local bank probes (centre and four ±6 positions); ruin variants with pier footings may have an intentionally unsupported *destination shore* while intact bridges still require both banks. Every constructed pier still searches up to 24 blocks down for actual natural rock, no forced lava-sea fill. **Requires independent rerun** at [40838712956](https://github.com/Deserag/slavic-myths/actions/runs/40838712956); no PASS assumed prior to its result.

All owner-visual, five-seed, multiplayer and game-world acceptance gates remain open even if the one-seed headless check later passes.

## Follow-up: direct regression and independent seed run, 29.10.2026

The above early candidate statuses are historic. Verified actual later GitHub Actions logs (do **not** treat every checked candidate as a placed bridge):

- [40838712956](https://github.com/Deserag/slavic-myths/actions/runs/40838712956) **FAIL** 15/16 despite real accepted start: deck overlapped `crimson_roots` at (208,40,-807).
- [40839950532](https://github.com/Deserag/slavic-myths/actions/runs/40839950532) **FAIL** 15/16: deck overlapped `crimson_fungus` at (212,40,-808), after moving bridge to the `top_layer_modification` generation step.
- [40841112330](https://github.com/Deserag/slavic-myths/actions/runs/40841112330) **FAIL** 17/19: `weeping_vines` overlapped the bridge (211,40,-807). The expanding QA suite includes Wayfarer tests.
- [40842160665](https://github.com/Deserag/slavic-myths/actions/runs/40842160665) **FAIL** 18/21: physical bridge headroom blocked (211,40,-807). All 64 authored bridge geometry profiles still PASS; physical result does not.
- [five-seed run 40842359154](https://github.com/Deserag/slavic-myths/actions/runs/40842359154), build on `6f340715`:
  - seed `20261025`: **PASS** genuine natural bridge, chunk [91,-70], biome `minecraft:nether_wastes`, length38 variant2, 33 road steps / 165 unobstructed road cells; entire 21-test headless suite PASS.
  - seed `3412026`: bridge **PASS**, chunk [-95,1], `minecraft:nether_wastes`, length38 variant3, 30 steps / 150 road cells; separate Wayfarer/boss checks failed.
  - seed `9132026`: bridge **PASS**, chunk [62,-25], `slavicmyths:navya_grove`, length30 variant2, 25 steps / 125 cells; separate Wayfarer check failed.
  - seed `20261026`: bridge **FAIL**, `crimson_stem` at (1870,33,-2023).
  - seed `20261024`: bridge **FAIL**, obstructed deck headroom (211,40,-807).
- Follow-up [258c7f1](https://github.com/Deserag/slavic-myths/commit/258c7f15490070566c448361ab1e87deee3f9926) permits removal of identified *naturally generated* fungal tree parts from the structure footprint, and expands finite Wayfarer preflight. Re-evaluate at the next CI; this is not a verified PASS yet.

There is confirmed natural worldgen on multiple fresh Nether seeds, **but 2.1.2 remains FAIL as cross-seed geometry regression until a repeat pass**. Owner visual/survival and multiplayer/restart are still `NOT_RUN`; the Issue stays OPEN and no owner acceptance is asserted.
