# Bayun / Book — audit of checkout 1.6.8

2026-10-11. Starting Git main `7f9f9181`, clean before extracting the supplied archive. Archive integrity: 51 SHA-256 entries verified. Minecraft 1.21.1, NeoForge 21.1.255, Java 21, official mappings. The archive's colloquial final1.6 label does not roll back version1.6.8. Next production candidate1.6.9.

## Actual registries

Dedicated GameTest audit1 actually booted the current mod and exported `registry-audit.json`: 74 entity types, 712 items, 244 blocks, 19 feature types, 17 structures, 57 configured features, 51 placed features, 487 recipes. These include projectiles, seats and cinematic actors; they must be explicitly associated with the corresponding system page, not invented as independent creatures. Initial entity registry has no Bayun. Entity variants named in localization (doghead roles, enraged Nightingale) are not separate entity IDs.

162 existing mod advancement JSON files include recipe unlock helpers without displays. Recipe helpers remain real vanilla recipe advancements; do not turn them into misleading visible challenges. Existing `ill_fate_broken` is inventory-derived and does not prove personal boss victory; `steppe_champion` and `defeat_nightingale` are explicit event grants. Preserve IDs, criteria and rewards, including historical completions.

## Reuse and integration

- `ModItems.LORE_BOOK`: stable `slavicmyths:lore_book`, `LoreBookItem.use` currently awards lore_book and opens `MythPayloads.LoreOpen(int mask)` through `LoreNetwork` and client-only `LoreScreen`. Keep item ID. New server snapshot supersedes the 29-bit legacy UI; keep old wire definition for existing internal tests, route actual book use to new snapshot.
- `Knowledge` persists old discoveries through actual vanilla advancements. Its mask method has inventory-derived awards and a duplicate depth_gift bit; the new book must read progress without invoking mask or fabricating earned events. Do not reset old advancements.
- No first-login book grant exists. Durable once-per-world UUID book delivery and catalog data will use overworld SavedData, consistent with existing NavigationRecords and YagaData architecture. Legacy players with old book, earned lore_book or nonzero play time are treated as already delivered rather than risk duplicating a lost/chest-held book; existing crafting remains the replacement path.
- `YagaData.Progress` stores intro/stage/active/contract and cooldowns. Book adapter reads fields only from existing map, never calls accept/complete or modifies rewards. `YagaServices` has actual main/contracts/brews; use its real labels and facts. YagaData schema and classes remain unchanged.
- Entity registrations reuse `ModEntities.ENTITIES`; feature registrations reuse `ModFeatures.FEATURES`; creative tab enumerates registered items. Sounds reuse ModSounds.SOUNDS. Dedicated native model/renderer remains Dist.CLIENT only.
- New discoveries use real server interaction, pickup/craft/smelt, living death credit, tracking visibility and advancement events. Any proximity sampling is local, throttled and bounded; no world scan when opening book.
- Search areas use captured real home/structure anchors only, deterministic offset and broad radius; no exact target coordinates or future bogatyr claims. Bayun dialogue can reveal his own actually resolved home area.

## Validation boundary

Initial audit: one dedicated server, zero clients. Final count: 19 dedicated servers, zero clients; six early failed runs were repaired, thirteen successful runs including the final 15/15 suite. Runtime visual Russian/animation/audio acceptance is pending because the user explicitly declined automatic client launch. No user saves are modified for testing. No commit/push is authorized for this new implementation. Supplied sounds are original synthesized prototypes, not field recordings.


## Final candidate audit (2026-10-11)

Runtime registry:75 entity types (+kot_bayun),713 items (+spawn egg),244 blocks (unchanged),20 feature types (+bayun_nook),17 structures (unchanged),58 configured/52 placed features (+one each),487 real recipes (unchanged). All1666 registry rows represented in217 entries;58 individual living profiles, technical projection/seat/projectiles associated with owners. No Yaga schema/class/reward rewrite; no bogatyr or joint quest introduced.

Old162 advancement IDs/criteria/requirements/rewards preserved;13 new display nodes (4 real events,9 sections without rewards). Native vanilla progress persisted through an actual saved-server-world restart. Read-only Yaga lookup does not create a Progress. Four old Cyrillic name corruptions corrected; other prior keys, all Kuzya audio/models and all unrelated classes/resources checked against accepted1.6.8 JAR. No commit/push.

Early failed dedicated runs: invalid `$` catalog resource paths, two nook fixtures whose test roof spoiled the heightmap, malformed restart fixture namespace, two restart reads before native chunk/entity activation. Corrected fixture waits/preflight and resource names; no failures hidden. Final native runtime tests15/15 PASS; real restart PASS; clean build/offline/resource/JAR/audio PASS. Client visuals/audio/controller/real2-client MP and ordinary-world natural seed remain NOT_RUN, actual MSPT/FPS NOT_MEASURED. Do not call this fully accepted final release.
