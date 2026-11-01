# Morovoy bosses — trophy-head implementation checkpoint (2026-11-01)

**Status: WIP / UNVERIFIED / NOT OWNER-ACCEPTED. Do not merge into `3.3` on this report alone.**

## Scope implemented
- Three separate registered blocks/items: `morovoy_raven_head`, `fire_serpent_head`, `mor_serpent_head`.
- Native model head subtrees are used directly: Raven from `MorovoyRavenModel`, Fire Zmey from the right head, Mor Zmey from the left head in `MorovoyFireZmeyMesh`. No generic cube surrogate. Each includes its original eye, beak/teeth, horns and jaw subparts.
- Independent idle jaw/head movement and bounded, client-only particles for each placed trophy, including a three-color purple-white fire palette and green Mor particles.
- Existing block-entity and BEWLR item renderer reused for world and item contexts. Custom item display transforms added for inventory and ground. No new global polling.
- Blockstates, block/item models, survival block-break loot, RU/EN names and Creative catalog entries provided. Existing trophy IDs left unchanged.
- Height is **one block** for each new trophy in this first pass. User has *permitted* larger placed models, not explicitly required them; increasing footprint requires matching geometry-based collisions and a real in-game visual review.

## Blocked or incomplete — no false PASS
1. Original approved `MOROVOY_FIRE_ZMEY_APPROVED_REFERENCE.png` and Raven reference archive must be reopened from the owner's prior uploads. Issue #396 records SHA-256 `e3b534bda20c1c36a9eddf5f7575bd6816cd1f7ad0df30e12c5f7ab5fe3083f5`; the source is not present in this GitHub checkout. No 1:1 visual acceptance can be claimed.
2. The owner-file requires exact reference-to-model detail mapping, screenshot comparisons from all angles, independent boss animations and corrected fight behaviors. None of these is proven by this trophy-resource patch.
3. **Overall boss-head drop chances are still unspecified.** The #396 QA ledger explicitly says no approved head loot table or overall probability. The existing drop event now normalizes any preexisting Fire/Mor head drop to exactly one randomly chosen 50/50 trophy, without creating a new drop chance. With no approved head-drop trigger, no serpent head drops. The Raven head's actual boss-drop trigger/chance is also pending approval. Preserve existing Dragon Bone/Dragon Core rewards and crafting.
4. A Java 21 clean Gradle build, registry/resource validation, client startup, dedicated server, multiplayer, world reload, creative/inventory/hand/item-frame and break/placement screenshots are **NOT_RUN** for this branch. No runtime evidence was provided here.
5. Fully authentic sound clips, model/texture correction and exact head appearance remain unresolved until original approved art and real runtime QA.

## Files touched
- Java: `TrophyHeads.java`, `TrophyHeadBlock.java`, `TrophyHeadDrops.java`, `TrophyHeadsClient.java`, `MorovoyRavenModel.java`.
- Resources: 3 item models, 3 block models, 3 blockstates, 3 block-break loot tables, 2 localization JSON files, `creative_catalog.tsv`.

## Required next verification
Run `./gradlew clean build --no-daemon -Pmod_version=3.3.0-TEST` with Java 21, resource validation and a dedicated-server + client smoke run. Then compare original approved PNGs with in-game boss/trophies before deciding geometry, item transforms, collision footprints, sound clips and drop event integration. Record failures with file and screenshots. No auto-merge.

Current integration target according to the **user-supplied rework file** is `3.3`; the repository's older 2.3 branch policy applies to its original 2.3 development cycle, not unconditional permission to overwrite this current integration.
