# #236 — trade policy fixed before implementation

24.10.2026: owner authorized implementing the epic and selecting non-free prices. Reuse YagaServices.EXCHANGES, its server stage/input/cooldown validation and JEI integration. Preserve all existing array indices by appending new offers.

Offer | Required Yaga stage | Cost | Output
--- | --- | --- | ---
flying_broom | 2 (second main quest complete) | 8 svyazka_trav_yagi + 4 vihrevaya_nit + 2 ognennoe_pero | 1 broom
flying_mortar | 3 (TRUSTED) | 16 svyazka_trav_yagi + 4 tugarinova_kozha + 16 minecraft:iron_ingot | 1 mortar
pestle | 3 | 4 svyazka_trav_yagi + 2 uzel_podveya + 8 minecraft:iron_ingot | 1 pestle

Mortar pilot identity is explicit, synchronized and saved. The owner boards as pilot; another player can board the second seat while the pilot is present. A passenger never inherits controls when pilot exits. Existing inactive-flight descent/braking remains; cargo/repack remains owner-only and broom capacity stays one. Distinct side-by-side seats prevent body overlap.


Guide policy: Yaga offers all three trails from stage1; Bayun requires this specific cat's trust25/60/85 for Likho/Tugarin/Nightingale. Distinct vanilla advancements, not kill counters, gate Bayun rumors at2/3. One non-repeatable errand per target in TaleData; return to the original guide/cat grants one ancient sign (and +5 trust for that cat). Victory before accepting cannot grant this new reward. Wolf keeps his four old quests; trust4 derives from the last claimed old quest plus claimed wolf_loyalty. No old NBT keys are renamed.

Reservation policy: at most4 queued explicit requests per server, one job step/tick. Boss anchors use native deterministic region preparation (up to64 metadata candidates) and existing activation/biome/clear rules; no forced chunks. Nightingale uses a single background planner with24 native structure candidates and25s cooperative timeout; full accepted StructureStart/pieces/campUUID, no standalone boss and no manual placement. Border validates full planned box. The planner places no blocks and adds no chunk tickets; native worldgen materializes only during exploration. Loaded chunks with no valid start are rejected. No acceptable plan yields UNAVAILABLE, never a fabricated successful target. Natural worldgen guarantees, third-party claims and native-world/restart/MP regression still need runtime evidence.

Keyboard addition requested by owner during implementation: use native NeoForge KeyModifier, default Ctrl+top-row1/2/3 for active slots; keep the server ability protocol unchanged. Extend class-window capture to modifier+key/mouse, document in docs/CONTROLS.md, and update only the three requested existing PolyMC options with backup. No incomplete gameplay JAR is installed for this settings change.
