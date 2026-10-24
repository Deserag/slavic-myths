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
