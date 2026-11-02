# Issue #391 — Morovoy Raven 2.3.1 — implementation status

## 3.3 remediation (Issue #391, 2026-11-01)

**Current integration branch: `3.3`. Do not update `main` or `hell`.**
The Java model in `MorovoyRavenModel.java` now has four independent shoulder pivots
`main_left`, `main_right`, `upper_left`, `upper_right` and four articulated `outer` wrists.
Design coordinates give 86/16 = 5.375 blocks from skull forehead to the tail base
and about 256/16 = 16 blocks between extended main feather tips.
The chest volume has been simplified, the bone-UV upper bill narrows into a hooked tip,
and the hinged lower jaw rotates independently.

The `MorovoyRavenRenderer.java` emissive pass loads only
`textures/entity/morovoy_raven_emissive.png` (256x256 transparent UV overlay).
No cuboid plague branches or procedural green glyphs remain in the new Java model.
The legacy `morovoy_raven_glow.png` is no longer sampled by this renderer.

**Art limitations:** The `MOROVOY_RAVEN_WIP.bbmodel` wing-group identifiers have been
renamed to match Java, and the existing animation UUIDs are preserved, but its OLD
cube mesh was not re-exported from the rewritten Java rig. Do **not** treat the WIP
Blockbench geometry as 1:1 synchronized or export it back over the new Java model.
The original approved PNG and real front/side/back/three-quarter Minecraft comparisons
are still required. Current visual gate remains **ART_SOURCE_BLOCKED / NOT_ACCEPTED**.
Do not mark Issue #391 closed on the strength of source edits or Java compilation.


Integration target: `2.3`. Feature branch: `feat/2.3.1-morovoy-raven-boss-391-20261101`.
Owner approval: **NOT GRANTED**. Do not close the issue, merge without review, or call this accepted.

## Dependency and source audit

- The arena branch `feat/2.3.1-morovoy-raven-arena-391` was compared with `2.3` on 2026-11-01: **identical, zero extra commits**, no arena PR. Therefore no worldgen or natural boss spawning is attempted in this feature. `setArenaAnchor(BlockPos)` is the explicit hook for the arena's future boss spawn.
- The first-pass editable Blockbench source is provided as `MOROVOY_RAVEN_WIP.bbmodel` with 4 wing bones and 4 prototype clips. It has NOT been validated against the Minecraft runtime or approved PNGs.
- The two owner-approved appearance/attacks PNGs named in #391 were **not** present in the GitHub reference branch; it contains only three supplemental SVGs. The custom runtime textures/voxel rig here are **first-pass original work, not verified matches**. Visual status: **ART_SOURCE_BLOCKED**.
- Existing `NightRaven` serves as a movement/API reference only. The large model is not an upscaled copy.
- The existing `MorZone` is tied to `LandSpiritEntity`; to avoid breaking it this feature uses bounded `RavenMiasma`. The plague DOT is vanilla Poison pending audit of the final Mor status effect.
- Material registry entries are provided for `morovoy_raven_feather` and `mor_core`. No drop rates, crafting, staffs, runes or quests are added (reserved for #392).

## Verification matrix

| Requirement | Real source | Art reference | Evidence / result | Status |
|---|---|---|---|---|
| Separate four-wing rig, beak, talons, tail | `MorovoyRavenModel.java` | Approved PNG A unavailable | Java ModelPart + editable Blockbench source; side-by-side in-game comparison absent | BLOCKED |
| 5–6-block torso length | `MorovoyRavenModel.java` | #391 | Source geometry approx 6 blocks; Minecraft measurement absent | NOT_RUN |
| Real PNG texture and item sprites | `textures/entity/morovoy_raven.png`, `textures/item/*` | Approved PNG A unavailable | First-pass procedural pixel textures and alpha-masked emissive glow; not final approved art | BLOCKED |
| 3D flight and steering | `MorovoyRaven.java` | Approved PNG B unavailable | Collision-envelope steering and timed phase source; no test world | NOT_RUN |
| Orbit, ranged, dive, claw, beak, wing sweep, summon | `MorovoyRaven.java`, `RavenBlightBolt.java` | #391 | Source implemented; frame timings, MP, obstacle QA not measured | NOT_RUN |
| Non-destructive temporary ground plague | `RavenMiasma.java` | #391 | Timed owner-bound entity (no world block writes); 2-client QA absent | NOT_RUN |
| Spawned minions survive load / no dupes | `MorovoyRaven.java` | #391 | Parent UUID, lifetime and attack timers serialized; world restart not tested | NOT_RUN |
| Arena integration | owner arena feature branch | Approved PNG B unavailable | Arena has not been committed; hook only | BLOCKED |
| Items only, no staff/recipes/quests | `MorovoyRavenContent.java` | #392 deferred | Two resource IDs registered, intentionally no reward drops | PASS_SOURCE_ONLY |
| Java 21 clean build | `.github/workflows/morovoy-raven-391.yml` | n/a | Pending workflow run; do not mark PASS before successful log | NOT_RUN |
| In-game screenshots, Steve scale, front/back/side/¾ | Minecraft 1.21.1 client | Both approved PNGs absent | No Minecraft screenshots produced | BLOCKED |
| Real MP, save/load, 5 seeds, server MSPT/FPS | Dedicated NeoForge server + client | #391 | No actual runs in this submission | NOT_RUN |

## Manual integration/QA when prerequisites arrive

- Attach arena's persisted anchor through `boss.setArenaAnchor(anchor)` during initial spawn, preserving a single owner per arena.
- Verify boss with `/summon slavicmyths:morovoy_raven` in an **isolated** Minecraft test world, not the owner's active desktop.
- Verify ground raycast in open Nether crater (never mark roof as polluted), arena boundary, unloaded-chunk path refusal, 4-wing clipping, death cleanup, minion cap, and 2-player target behaviour.
- Compare original PNG A/B side-by-side with actual Minecraft entity screenshots and rebuild Blockbench-approved art before visual acceptance.
- Run Java 21 clean build, dedicated server and two-client QA only in permitted isolated environments. Record JAR/source SHA, frames/logs, and measured performance.
