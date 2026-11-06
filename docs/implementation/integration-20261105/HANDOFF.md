# Next chat — resume paused #416

Use ONLY `C:/Users/user/Documents/slavic-myths/work/issue-416`, branch `codex/416-posad-fixes-20261105`. Original root master has a huge unfinished merge: DO NOT TOUCH. Read current Git status, PROJECT_STATUS/ROADMAP/ARCHITECTURE and owner contract first. Integration report in this folder records incoming branches and preservation checkpoint. User paused #416 for integration; resume it when instructed. Latest task forbids Minecraft launches without separate permission. Do not infer runtime authorization from old launches below.

## Already implemented, now preserved in Git
#416 geometry schema27: paired door orientation/hinge fixes; panes/fences/stairs final-state resolver shared across native/manual/standalone; dominant Veche roof; full two-part nonexploding Nether cots; crimson/warped furniture and authored throne; fresh house chest NBT-safe relocation; 17 stable residents/12 roles and loaded-chunk-only native population; native trading open order/busy customer/stock refresh fixes; proportional unit sulitsa sales.
Bounded exact noise batching (16x16 vertical window -16..81 with fallback) and LRU planner cache; vegetation clearing only in fresh generation; expanded Posad biome tag, StructureSet kept baseline spacing48/separation20/salt220211. Recovery releases ONLY provably empty safe construction claims, never potential loot; reward_locked persists before possible loot writes. No rollback/retrofit promised.
Diagnostic owner-authorized test proof/receipt items, actual damage contribution and confirmed Drops-based kill credit, durable pins/inbox, paired kills, leader exchange; loaded/recorded boss search without chunk loads. OP encounters create actual bosses/linked pairs and tagged cleanup only. Final rewards/trust/quests/full natural sister spawning deliberately deferred by owner. Proof is not final balance.

## Historical QA — predates final integration, do not call final runtime PASS
Ignored work/logs within worktree:
- `issue416-physical-runtime-12.log`: 22/22 GameTests.
- `issue416-proof-tests-6.log`: 4/4; confirmed/canceled death, genuine/forged/duplicate/full inventory/paired credit, six actual diagnostic encounter commands and leader search.
- `issue416-noise-window-parity.log`: 2/2; 108480 full +376320 window states across five seeds equal vanilla noise.
- five real native starts seeds29,11,101,2026,22022026; 64–72 chunks and 46368 expected blocks, all four rotations across results. Logs `issue416-native-seed*.log`; seed29 actual disk restart checked.
- `issue416-native-population-29-4.log`: actual restart and production loaded-chunk population reaches17. Two ServerPlayer proxies are NOT network multiplayer.
- `issue416-native-black-cinder.log`: latest optimized planner actual start, seed29, 88 chunks, 46367 blocks, candidate worst739ms; NOT measured MSPT.
- `issue416-hidden-client-3.log`: real isolated hidden GLFW client, 13 captures in `work/416-client-3/posad-client-evidence`; four rotations and saved native city. Spectator captures are not movement/trading tests.
Important: pictures exposed pine model references and table blocking throne. Generators/geometry have since fixed those references, raised throne back and cleared front table; corrections have NOT been rerun visually. Build/static checks pass.

## Remaining #416 priorities
1. Recheck final integrated build/runtime after explicit launch permission; rerun client after latest furniture/throne corrections; verify client panes, walking/stairs/floors, real merchant UI and dialogue.
2. Critical: native city exterior access in mod biomes. Black Cinder start Y43 may be below later Cinder feature surface~65; interior block checks do NOT prove reachable entrance/shore. Inspect/fix bounded approach algorithm. No arbitrary frequency increase. Test all four mod biomes, final five seeds, starts/rejections/distances and real performance. Current NoiseBatch speed ratios are microbenchmarks, not MSPT.
3. Actual process restart for new proof/exchange inbox/pins, crash/cancel/recovery cases, old player chest/loot safety. Real two-network-client multiplayer still NOT_RUN.
4. Update version (currently stale2.1.6.2-TEST), final clean build/scoped resource checks; global kitchen stays BLOCKED by owner decision, including malformed spicy_seasoning and unregistered crops/dishes. Preexisting missing mytar_shackled_sickle recipe output is not permission to invent weapon art.
5. Create NEW PolyMC test profile, preserve both old profiles and worlds. Not yet done. PolyMC exe `C:/Users/user/AppData/Local/Programs/PolyMC/polymc.exe`; config `C:/Users/user/AppData/Roaming/PolyMC/polymc.cfg`, InstanceDir=instances. Existing MC1.21.1 NeoForge21.1.255 profile `Slavic-Myths-Bosses-2.1.6.1-Testing`; also `Slavic-Myths-2.0.8-HELL-Testing` and1.16.5. User requested NEW section/profile. Discover again, copy config/dependencies only as needed, preserve worlds, verify installed one JAR/hash and actual launch only with authorization/isolation.
6. #416 target PR2.4, no automatic merge/closure/owner acceptance. Commit/push authorization for integration does not authorize calling unfinished #416 complete.

Tools: JDK `C:/Program Files/Java/jdk-21.0.12.1`, set JAVA_HOME per command. Runtime harnesses tools/posad-{client,native-dedicated,runtime-tests}; all are outside production source set. Gradle logs and disposable worlds ignored under work/. No user worlds overwritten. Resource generators must preserve latest authored geometry and both `slavicmyths:pine_` and `slavicmyths:block/pine_` replacement logic.
