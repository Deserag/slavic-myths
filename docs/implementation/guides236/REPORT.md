> Current integrated acceptance matrix: [consolidated report](../three-issues-20261024/REPORT.md). Earlier pending rows below are historical.

# #236 — IN_PROGRESS

Implementation is on codex/guides-236, stacked on the redesigned book (#237 / PR259), which itself follows #235 / PR239. Main is preserved; no automatic merges/Issue closure/acceptance or gameplay JAR installation.

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Yaga 1/3 distinct victories; Bayun rumor2/3 and narrower3/3 | GuideDiscovery, native advancement gate matrix | PASS native GameTest
Per-cat trust25/60/85, feeding/day policy unchanged | authenticated Bayun choice7..9; actual milk interaction and forged nonce test | PASS native GameTest
Wolf trust4, old four quests retained | old claimed chain + claimed wolf_loyalty, old keys unchanged | PASS native GameTest
NPC three-target requests | YagaMenu704..706, Bayun nonce7..9, same TaleData GuideQuests | PASS source; normal-world path pending
Finite persisted reservation states and idempotence | GuideQuests: SEARCHING/WAITING/ACTIVE/RETURN/DONE/UNAVAILABLE/INTERRUPTED | PASS source/native existing-target test; restart NOT_RUN
Existing real target preferred; dead/unavailable not respawned | HuntRecords and native NavigationRecords, actual loaded UUID test | PASS native GameTest
Safe remote boss planning | native region preparation, max64 metadata candidates, activation unchanged, no force chunks | PASS source; natural terrain campaign pending
Full Nightingale fortress | max24 native plans in bounded25s worker, whole StructureStart/pieces/camp UUID; no manual NPC placement | NOT_RUN normal-world verification
No heavy generation on book open | Yaga discovery moved to explicit search button; ordinary opening reconciles metadata only | PASS source; final client pending
Book and navigation | same TaleScreen/Snapshot/Read, public GuideQuests delta strips exact anchors/UUIDs; existing markers/search areas | PASS source/native private-snapshot test
Flight trades | fixed policy, appended offers, server consumption/cooldown and shared JEI catalog | PASS native menu tests: gates, all ingredients, one output, duplicate denied
Mortar two passengers / broom one | explicit synchronized saved pilot UUID, owner-only input/cargo, separate seats/poses, legacy cargo | PASS native tests; two real client sockets/visuals NOT_RUN
Inactive descent | fixed hard-coded Y=2 boundary to dimension minBuildHeight+2 | First run FAIL preserved; second run PASS
Keyboard addition | Ctrl+1/2/3 defaults and native modifier capture, docs/CONTROLS.md | compile PASS; real client capture/save/reload pending
Requested PolyMC settings | skill-combos-installed.json, exact three lines + backup/SHA | PASS disk; physical key activation NOT_RUN; JAR unchanged
Clean build / production resources | build-guides.log1m23s, resources-guides.log7692 assets | PASS before final refinements; final clean build required
Server regression | runtime-guides-1:8/9, runtime-guides-2:9/9 required | PASS final server campaign at that source; later refinements not rerun
Command generation and sentinel | real /slavicmyths generate bandit_camp large2362026,761ticks,39277 changed blocks; sentinel preserved | PASS actual command GameTest
Performance | command placement peak110.98ms/8192 operations; first A/B book frames in ../book237/client | measured, not blanket performance PASS
Natural NPC fortress / combat completion / save-restart / MP / third-party claims | upcoming normal-world fixture; not implied by asserts/build | NOT_RUN
Owner acceptance / full epic | owner only; Issues remain open | NOT_RUN

Real Minecraft processes for this campaign so far:1 client A/B +2 isolated GameTest servers. The first server failure is retained. The next client is postponed until12:08:28MSK per the owner's seven-minute pause. The settings change requires no replacement JAR because the installed NeoForge already supports native modified key mappings. Full custom capture UI exists only in this branch until an eventual gated installation.


## Normal-world and final-book follow-up

Combined real client completed48 RU/EN/GUI/resolution frames and actual modifier capture/save/reload. Normal seed2362026: all3 Yaga requests accepted through actual YagaMenu; Nightingale native plan reserved, then normal travel materialized17 pieces,49 loaded footprint chunks,25 containers and a live boss with stable campUUIDf56e8046-91d9-d599-c5f0-9d6d82f8abdd. Original sentinel preserved. Screenshot: normal-world/native-npc-fortress.png. This is actual native fortress generation, not a standalone boss or manual NPC placement. Full raw results including failures remain in the work log and normal-world/FAIL.txt.

The first Bayun normal-world path hit a fixture error (test cat fell away from the hovering test player before the next dialogue), causing a missing test nonce; no production crash was found. Replay fixture now reuses the same existing cat, stabilizes test actors, and reopens the same saved world in a separate client process to check native camp/boss persistence. Pending until run. The UI-to-world transition also needed a harness-only normal disconnect correction; no player worlds were used.

Latest clean build SUCCESS1m,7692 packaged resources,1851 classes, production SHA256b0714dbe8eb7df17e27ce8734d409c2a238616a9fefc37d523001ef1bcea4fa7. Tracker controls use the existing NavigationNetwork; only the public markerUUID is exposed, never the targetUUID or exact anchor. A marker-specific STOP cannot clear an unrelated active marker. Native planner JFR:52s/3877 execution samples; see jfr-summary.json. This evidence does not turn two-socket multiplayer, boss combat, actual physical shortcut activation or full epic acceptance into PASS.

Correction of evidence scope: the first two command runs preserved a sentinel outside the projected fortress footprint. This proves no arbitrary overwrite at the source point, but does NOT prove collision rejection within the build area. A stronger fixture now puts a diamond block INSIDE the original planned footprint at12020,-56,11840 and requires relocation plus block preservation; result pending. Do not interpret the earlier wording as a completed protected-footprint matrix.


Actual separate-process restart verified the original17-piece campUUID and live Nightingale. The old source then selected another native fortress during a new fixture request; diagnosis found that NavigationManager.type still returned null for Nightingale despite the whitelist entry. Fixed the type dispatch and added an actual entity-join/two-player shared-target regression. This is a production bug found and fixed, not a successful no-duplicate result from the old run. The second native camp had18pieces/28containers; the current native-npc-fortress.png shows that later camp. The initial17-piece screenshot was overwritten by the fixture; its source/UUID/container evidence remains in the raw log. The harness now also re-enters SAFE immediately before each creative-mode Bayun choice, because normal Bayun AI intentionally cancels sessions for ineligible creative players between ticks. Neither incomplete normal-world campaign is reported as entirely PASS.

Third server campaign:12/13 required PASS. The strengthened in-footprint diamond sentinel forced relocation (entrance12103 instead of12039) and survived; complete fortress still generated. Authenticated Bayun choices7/8/9, two player records sharing one actual Nightingale, native friendship at85 and delayed duplicate-reward denial PASS. New marker-specific STOP regression failed because the old global STOP implementation had remained in place; corrected explicitly and rerun required. Peak command placement115.05ms remains unresolved.


## Latest native server result —13/13 PASS

Fourth isolated server process: runtime-guides-4.log (source273bb77a) passed all13 required tests. It verified the corrected marker-specific STOP, actual Nightingale entity-join indexing and shared target across two separate server players even after the bandit roster was cleared, friendship award at85, native NPC authenticated routes, one-shot reward/reclaim guard, old vessel cargo, explicit pilot identity/capacity and real command placement. The inside-footprint diamond block survived; the fortress relocated from predicted entrance12039,-56,11822 to12103,-56,11822. Final command peak101.39ms,1237ticks,39277changed blocks; this is still a measured placement spike, not an optimization PASS. Summary: runtime-server-final.txt. This is two server players, not two real socket clients.

Subsequent source-only UI refinement gives mortar passengers their own controls hint and refreshes Bayun offer gates when trust changes. It is included in the final build and next client source, without modifying server gameplay.


## Final normal-world replay

Fifth dev client, client-replay-3.log: separate-process restart confirms the original17-piece fortress, unchanged campUUID and living boss. All three Yaga and all three authenticated Bayun requests accepted. Original live Nightingale reused for both NPCs; no third camp. Likho/Tugarin remain WAITING reservations, not falsely reported as loaded or killed. results-replay.txt and guide-tracking-replay.png are final. Client screen callback tests PASS for node selection, drag threshold/pan, empty search, sidebar/fullscreen. Physical input is not covered. Normal exit saved all dimensions.

FAIL-replay.txt is a historical harness false failure: replay2 checked an unloaded entity from the second camp. Saved region data showed both bosses alive. Corrected replay first travels back, then checks the loaded original. Earlier failure preserved separately as FAIL-replay-1.txt. Snapshot first51.99ms/8,374,664B, repeat5.07ms/802,320B, full payload245,167B. Last100ticks mean14.43MSPT/p9532.44ms. These are not old/new server or multiplayer comparisons. Final production SHA256bcb93c26d5383963eef7d5f19cde64676c20cd2a4f9f1b35b9c26f82b512b656 supersedes earlier hash. Total9 Minecraft processes, including failed attempts. User removed the pause requirement. New gameplay JAR is not installed.
