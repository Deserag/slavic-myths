# Desert weapon revision — 2026-11-25

Branch: `feature/desert-biome-20261125`, base `1f17cf637b767226e9a64fd03c6daccba2db8787`.

## Changes

- Replaced the rejected cuboid placeholders with authored pixel silhouettes, material-shaded OBJ geometry and separate 32px inventory sprites. Ordinary weapons follow Minecraft proportions and use ivory, leather and bronze. The four legendary weapons have distinct amber/chitin, wind-gauntlet, serpent-polearm and runic hammer designs.
- Added craftable desert sword, axe, mace, halberd and scythe, recipe unlocks, catalogue entries, translations and enchantment tags.
- Mirage Bow now extends native `BowItem`: hold RMB to draw and release to shoot; all three native pulling states and vanilla transforms are retained. The ordinary full draw retains its 26 base projectile damage; diamond durability and prior melee attributes remain. Shift+LMB fires three mirage arrows, Shift+RMB fires a piercing arrow. Special skills cancel an existing draw to avoid a second ordinary shot on release. Native ordinary shooting consumes ammunition; the pre-existing special-skill costs remain unchanged.
- Scorpion stingers and Four Winds use one actual item and one held model per hand through the existing winter paired-weapon system. Visual duplication does not create a second transferable item.
- The buried hammer, desert halberd, desert scythe and Lamya polearm reserve the second hand. The existing offhand storage/restore contract also handles these weapons. Both wrists participate in their first-person rendering and third-person poses. Existing non-desert great-weapon poses retain their previous angles.
- The weapon HUD uses the existing winter text-binding layout. Replaced the desert atlas with 29 distinct 16px symbols, including a desert bow-draw icon; removed the desert-only drawn mouse widgets. Tooltips describe the actual mouse combinations, damage and cooldowns.

## Reproduction

- `python tools/desert_weapon_visuals.py` generates native models, inventory sprites, the vanilla-derived bow states and skill atlas. Requires Pillow and the Minecraft 1.21.1 client JAR already downloaded by NeoForm in the normal Gradle cache.
- `python tools/verify_desert_weapon_visuals.py` checks model references, both hands in both camera contexts, inventory sprites, translations, bow states and distinct skill symbols.
- `gradlew -I tools/desert-visual-client.init.gradle runClient --offline` creates a **new** isolated superflat world under `work/desert-weapons-visual-v2`. It never opens an existing save. The master volume is 10%.
- `gradlew -I tools/desert-visual-client.init.gradle -PdesertMotionOnly runClient --offline` captures first-person and third-person attack phases at GUI scale 3; the full inventory pass uses GUI scale 2.
- Both runs verify native bow draw/release, ammunition, durability, full-draw base damage, triple-shot and piercing skills, duplicate cooldown rejection, cancellation of a native draw, and paired/two-handed offhand reservation and restoration. The real server handlers for all 22 other desert skill slots are invoked against a live test target. This verifies accepted activation/cooldown routing, not every delayed damage tick or every boss immunity.
- `gradlew jar --offline` packages production sources only; the test probe is not shipped.

## Findings resolved during visual QA

- Long weapons clipped the top of the first-person frame: reduced their first-person scale and moved the two-handed grip farther from the camera.
- Gauntlets obscured too much of the frame: reduced first- and third-person scale.
- The supporting wrist missed the shaft: adjusted only desert support-arm angles and attached first-person wrists to a shared animated grip.
- Bow item-only animation interfered with the native bow pose: removed it and use the native two-arm draw.
- Missing translated item names, duplicate skill symbols, a winter-colored bow icon and material-edge colors were corrected.
- The initial all-skill test used Peaceful difficulty, which prevented the combat-ant summon. The final fixture uses Normal difficulty with natural mob spawning disabled. No production summon mechanic was changed to bypass this restriction.

## Results

- Scepter-description recheck: **12 frames**; corrected the misleading summon damage label to four ants for 30 seconds, each with 12 damage per hit, cooldown 10 seconds. The native inventory tooltip was visually checked.
- Full client pass: **228 frames**, inventory, both third-person views, first-person and native bow draw/release, Russian descriptions, GUI scale 2.
- Final two-handed grip recheck: **36 frames**, all four two-handed desert weapons plus bow controls.
- Attack pass: **120 frames**, first-/third-person attacks and bow draw/release, GUI scale 3.
- Both passes use a real Minecraft client with its integrated server. The skill and offhand checks passed in the final Normal-difficulty fixture.
- Selected unmodified frames and clearly labeled contact sheets are in `art/desert-weapons/revision-2/game/`; the full captures remain in the isolated test directory. Contact sheets enlarge player crops for grip inspection.

## Scope of verification

Real single-player client with its integrated server, first-/third-person rendering, inventory, two GUI scales and skill activation. Dedicated-server startup, multiplayer latency and a comprehensive balance regression are not claimed. Existing unrelated ocean trophy texture and subtitle warnings remain outside this change.
