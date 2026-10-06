# Command and generation diagnosis — 2026-10-06

Historical diagnosis before the user authorized the structure-first override. The repair is now implemented; current behavior and acceptance evidence are described in STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md. Statements below about unchanged production describe the earlier research stage.

## Confirmed screenshot/log failure

KurganDebug.generate reads Level.getHeight(OCEAN_FLOOR, x, z)-1 at four candidate positions 192 blocks away without first ensuring that the chunk has a heightmap. Minecraft 1.21.1 Level.getHeight returns minBuildHeight for unavailable chunks. In this dimension this produces -65, not the ground elevation. safe(checkSlope=true) compares nearby available surface at 172 against -65 and rejects a fictitious 237-block relief. The user's latest.log contains exactly y=-65 / moundRelief=237 / EXTREME_TERRAIN=4. This is a mod command bug.

A separate thin-flat blocker is visible in the command pipeline: after Earthwork.plan calculates a raised site, safe(world, originalOrigin, plan) still checks the unraised underground bounding box. It includes bedrock; natural(state) does not allow bedrock. Consequently the old position can prevent acceptance of the raised one even though no bedrock would be overwritten by the final piece. Existing plan/piece tests bypassed the real command and did not cover this gate.

## Actual user save, read only

level.dat: flat generator, 1 bedrock + 230 stone + 5 dirt + 1 grass = surface block Y172; windswept_hills biome; WorldGenSettings.generate_features=0; structure_overrides contains only minecraft:mineshafts and minecraft:strongholds. Native structure starts are disabled globally. Even with the global switch enabled, this flat override omits all mod structure sets; current mod biome tags also omit windswept_hills. Manual generate should remain independent of those natural-generation settings. The command currently does not explain them reliably.

KurganCommands.run returns 0 silently if the structure placement is absent; CampCommands skips missing placements and eventually reports no result. These behaviors must be distinguished from a running search.

## Performance defects and evidence limits

Custom kurgan locate may inspect 441 cells per call, camp locate 225 per requested size (675 for any). Both synchronously call getChunk(..., STRUCTURE_STARTS). ServerChunkCache.getChunk waits with managedBlock/join; large calls can keep the command busy while generating chunks. Eligibility also runs terrain sampling before this and actual-neighbor planning can generate additional plans. No cancellation, per-tick work/time budget or concurrent-request guard exists. Native generation debug similarly scans full bounding volumes and places all chunks synchronously; soil-count budgets are not server-tick time budgets.

The actual user's save omits these placements, so its missing-placement paths can exit without that expensive search. latest.log records Saving and pausing game, but no thread dump was captured during the reported hang. The precise cause of that saving stall is NOT proven. Code identifies serious blocking risks; do not report a confirmed deadlock or attribute every silent command to searching.

## Minecraft 1.21.1 comparison, installed official sources/resources

StrongholdStructure.findGenerationPoint supplies a generation stub without surface-slope validation; generatePieces rebuilds the layout until a portal room exists and moves it below sea level. Strongholds use concentric_rings placement (128 starts in the vanilla data). Portal rooms are parts of the stronghold, not separately searched surface sites; their shell/interior is written through chunk-clipped StructurePiece operations.

Trial chambers use JigsawStructure, start_height uniform -40..-20, random_spread spacing34/separation12, max_distance_from_center116, dimension_padding10 and terrain_adaptation encapsulate. The planner assembles underground pieces at the chosen height; there is no equivalent of our whole-dungeon original-surface safety scan. Official Minecraft descriptions: https://www.minecraft.net/en-us/updates/tricky-trials and https://feedback.minecraft.net/hc/en-us/articles/27547857163917-Minecraft-Java-Edition-1-21-Tricky-Trials . Exact algorithm details above were inspected in the installed 1.21.1 sources/data, not inferred from those descriptions.

MonsterRoomFeature is a different, small feature: it requires solid floor/ceiling and 1–5 openings, then writes the room and spawner. It does NOT ignore every terrain condition and will not necessarily succeed in open superflat air. Vanilla structures still obey dimension/biome/structure settings; their useful distinction is integrated placement and chunk-clipped construction, not ignoring every world setting.

## Repair order proposed, not implemented

1. Real-command regression tests for unloaded candidate height, thin and high superflat, all three sizes, explicit failure feedback and command work limits.
2. Separate deterministic manual placement from natural locate. Sample valid terrain heights without mistaking unloaded chunks for void, validate the FINAL raised footprint, and preserve world bounds/player containers.
3. Replace synchronous remote scans/full-volume writes with bounded jobs, progress/cancel, bounded chunk requests and repeated-request guards; never move mutable world access to an arbitrary worker thread.
4. Complete native placement/coverage and rejection compensation on normal terrain, retaining registry IDs. Explain disabled structure settings immediately rather than searching for starts that cannot exist.

Headless diagnosis completed: the real Brigadier dispatcher executed `slavicmyths kurgan generate small` on an isolated fixture with unavailable center chunks and nearby surface at Y172. It returned 0 with EXTREME_TERRAIN=4 and moundRelief=237, reproducing the screenshot exactly; this invocation took 30 ms in that fixture. The 28-test diagnostic run passed, including the expected BUG reproduction, not a repair. Log: `work/mob-worldgen-099-command-diagnosis.log`. The one-off diagnostic source is archived under `tools/diagnostics/kurgan-099/`, outside the normal acceptance source set, so an assertion that a bug exists does not become a release gate. Bedrock preflight blocker above is established by source inspection, not by a separate command reproduction. Full natural-coverage/release acceptance remains incomplete.
