# POST-PORT BACKLOG — DO NOT IMPLEMENT IN 0.9.4

These are confirmed future tasks after the NeoForge port.

## P0 / crashes
- Furniture sitting interaction can crash because sitting logic is unfinished.
- Broken/incomplete commands need cleanup.

## World generation / discoverability
- Structures are too far/rare.
- Large structures are especially difficult to find.
- Move proper registered structures toward vanilla `/locate`.
- Yaga locator currently can expose planned `Y=0 / placed=false` anchor and teleport near an empty candidate.
- Yaga placement search needs more robust candidate rejection/retry.
- Waystones/path stones are unclear and can be awkward.

## Kurgans
- Add significantly more meaningful loot/chests/containers.
- Current labyrinth is too vertical/linear; routes often simply lead down.
- Redesign entrance.
- Redesign kurgan custom block family.
- Add slabs/stairs/walls where appropriate.
- New/redesigned art source target: at least 256×256.

## Structures / bandits
- Empty interiors.
- Weak palette.
- Use existing dark wood palette.
- Give buildings explicit functions and worthwhile loot.

## Swamp
- Reevaluate whether a separate swamp biome is justified.
- Rework plants, flat grass models, waterlogging/underwater stones.
- Fix visible water/terrain seams.

## Mobs
- Many old mobs are passive or underpowered.
- Bannyk lacks animation/sounds.
- Old enemies need combat tier rebalance vs mini-bosses.
- Improve active AI.
- Preserve fixed dash-facing rule.

## Visual/art
- Pine foliage/trunk/underside issues.
- Ores closer to Minecraft style.
- Rework old items/tools/weapons/armor.
- Minimum source resolution for redesigned/new detailed assets: 256×256.

## UI / compatibility
- Custom HP HUD should later be removed/replaced with established ecosystem integration.
- Other custom information interfaces need review.
- Advancement screen/categories/text need cleanup.
- Creative inventory needs logical item grouping.

## Enchantments / runes
- Missing planned new enchantments.
- Existing enchantability with vanilla items is wrong/incomplete.
- Rune/reforging system needs dedicated pass.

## External mods
After port/stabilization evaluate:
- Jade;
- Patchouli;
- AppleSkin;
- FallingTree or Tree Capitator;
- GeckoLib only if current animation framework blocks quality.

## Release
GitHub public ready-to-download release/publishing is intentionally deferred until the final product.
