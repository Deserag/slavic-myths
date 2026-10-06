# Structure-first terrain override — 2026-10-06

## Implemented

Manual placement no longer goes through native biome, rarity, spacing or coverage rejection. It is a server-thread job: load FULL chunks by owned level-33 tickets without requesting entity ticking, measure actual heights, choose the final translated construction position, preflight affected blocks, prepare terrain, place existing architecture in clipped sections, record completed structures, release tickets. No client launch is required.

Thin worlds raise the complete kurgan above bedrock and fill stone/dirt under it. The final approach uses the saved prepared surface and protects buried rooms. Camps use their existing templates and encounter markers on a terrace. The optional cart and large-camp underground cache are retained. Ordinary slopes, trees, flowers and snow are prepared instead of rejected. Existing structures and containers trigger local relocation before any modification; exhaustion of local candidates or world bounds reports an explicit failure.

Water/lava and protected construction require local relocation, at most 16 candidates within 256 Euclidean blocks of the initial construction anchor. Frozen water is recognized. The anchor chunk is tested before loading a complete candidate footprint. No dry candidate means no terrain modification and a clear failure; no ocean island is created.

Heightmaps are read only after FULL chunks are available. Native WorldGenRegion and already-loaded ServerLevel use the appropriate heightmap variants. Completed structures have saved bounding boxes and a derived chunk index; known overlaps are rejected before full block scanning. Kurgan plan bounds are calculated once per job instead of rebuilding the complete graph for every voxel. Architecture skips corridor slices outside the current section and never reads external corridor blocks merely to discard their writes. Preflight container checks use the FINAL affected volume, not the old untranslated position or an entire vertical column.

Terrain scans/preflight/terraform target a 2 ms slice with bounded operation counts. Structure placement clips are 16 x 16 x 8, one per tick; a cold Java call or GC can exceed 50 ms. This is not a hard real-time guarantee or a measured client MSPT claim. Template loading uses the same thread-safe manager used by native worldgen workers, without off-thread world mutations. Progress messages appear every 100 ticks. Only one manual placement/search job is accepted per Overworld.

Native kurgans/camps and dry swamp templates prepare terraces. Land slope/budget policy reports TERRAFORM_REQUIRED rather than rejecting the structure. Physical underwater-bed constraints remain for underwater structures. Native grading stays within vanilla's eight-chunk reference range; the central approach uses one-block steps.

Neighbor collision planning retains copied bounding boxes in a bounded shared cache (2048 plans, 100000 boxes), keyed by generation inputs. Mutable placed pieces, players and block entities are not retained. The cache clears on world unload and datapack synchronization/reload. Coverage thresholds remain 1000 / 2000 / 4000; these constants are not themselves proof of natural coverage.

## Commands

Permission level 2; Overworld.

```
/slavicmyths generate kurgan small [seed]
/slavicmyths generate kurgan warrior [seed]
/slavicmyths generate kurgan great [seed]
/slavicmyths generate bandit_camp small [seed]
/slavicmyths generate bandit_camp fortified [seed]
/slavicmyths generate bandit_camp large [seed]
/slavicmyths generation status
/slavicmyths generation cancel
/slavicmyths search status
/slavicmyths search cancel
```

The existing `/slavicmyths kurgan generate ...` aliases remain. `medium` aliases `fortified` for camp generation. `locate` camp names retain their existing `medium` spelling.

Known manual structures are located through saved records, even when the world's natural structure generation is disabled. Unknown native searches request at most 32 candidate chunks, one ticket at a time, and stop after 30 seconds. No synchronous scan of 441/225 remote starts remains in kurgan/camp commands. Disabled natural generation or absent structure placement reports an explicit message. User Xaero waypoints are unaffected.

Cancellation/unload stops future work and releases tickets; already changed ground or partially placed blocks remain. Jobs themselves are not restart-resumable transactions. Completed structures are persisted. This limitation is visible in command feedback and must not be mistaken for atomic rollback.

## Verification

Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

Actual manual checks use isolated headless worlds with natural structures disabled. Ten commands per world cover all six structures, sloped camp/snow, water relocation, sloped great kurgan with a buried diamond chest outside the final volume, and protected underground brickwork. Assertions require real architecture, saved containers, bedrock clearance, accessible entrances and a continuous walking approach from kurgan mouth to original ground. Completed structure lookup, one-job rejection, status/cancel are checked. Verification reloads saved chunks after placement releases its FULL tickets.

A separate sequential test executes all six commands from one unchanged source position, requires relocation to stay within 256 blocks, and checks that each later structure preserves every earlier entrance. Bounds caching and early overlap checks reduced the same headless gate from 12m57s to 2m20s; these are test wall-clock durations, not in-game completion times. Measured great-kurgan jobs span thousands of server ticks; callers must wait for completion before the next command.

Water/ice/lava worlds check bounded failure with zero terrain writes and reject native ocean earthworks for all three tiers. A native terrain fixture checks 256 solid foundation columns, tree canopy/log removal, snow/flower removal and unchanged bedrock. The 27-test companion gate verifies registries, datapack codecs, saved kurgan piece reload/inventory preservation, native flat collision networks and existing mod integrations. Final logs and hashes are recorded in the machine receipt after completion.

Resource checks: 1945 JSON, 377 PNG, 3977 references, 189 sound entries; 16 pre-existing door-parent issues remain. Static data: 2676 references, zero errors with the 0.9.9 registry additions enabled.

Natural terrain coverage and full 0.9.9 release acceptance are separate from manual-placement acceptance. No claim of DONE is made from compilation alone.
