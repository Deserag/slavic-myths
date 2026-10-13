# RESOURCE AND INTEGRATION MAP (not a forced Java package rewrite)

Prefer actual repository package layout. Example logical paths, replace `slavicmyths` only if repo authoritative id differs.

Entity IDs: `slavicmyths:alyosha_popovich`, `slavicmyths:skim_beast` (preserve previous hero/boss IDs, no mass rename). New item IDs: `slavicmyths:alyosha_call_whistle`, `slavicmyths:beast_challenge_horn`, `slavicmyths:skim_beast_spawn_egg`, and only if missing `slavicmyths:ilya_call_token`, `slavicmyths:dobrynya_call_emblem`, `slavicmyths:idolishche_poganoe_spawn_egg`, `slavicmyths:marinka_spawn_egg`.

Assets: `assets/slavicmyths/textures/entity/alyosha_popovich.png` (128×128+), `.../skim_beast.png` (256×256+); `textures/item/*.png` (64×64 preferred) and item models as dictated by engine version. `assets/slavicmyths/sounds/alyosha/...`, `/skim/...`, `/arena/...`, `/items/...`; `assets/slavicmyths/sounds.json`; `lang/ru_ru.json`, `lang/en_us.json`.

If using existing library for animations, prefer `assets/slavicmyths/animations/...` corresponding rig; otherwise Java model keyframes in small methods acceptable, but no invisible dummy attack or one-pose static entity.

Server Java classes conceptual (adapt existing): `AlyoshaEntity`, `AlyoshaCombatController`, `SkimBeastEntity`, `SkimAbilityController`, `SkimEncounterState`, `SummonItem(s)`, small edits in `ModEntities`, `ModItems`, `ModSounds`, `ModCreativeTabs`, `EntityRenderers` and existing progress manager. Reuse existing hero base and boss base rather than duplicate. Do not introduce 30 abstractions or rewrite event bus architecture.

## Data pack and server integration
- Register spawn eggs through version-correct NeoForge egg API and append to already existing creative tabs. Vanilla `/summon slavicmyths:skim_beast` should work server-side. Regular `/give` for item IDs. Existing spawn egg max stack handled by engine.
- Recipe: under `data/slavicmyths/recipe/beast_challenge_horn.json` (or current version plural naming) and optional whistle recipe; resource path actual MC version must be verified against repo. For den vanilla loot, use already supported built-in loot table. No world migration tool just to add arena.
- Enforce signature: server-authoritative `trySummonHero(owner,item,position)` and `tryStartStoryBoss(player,marker)` instead of client spawn. Return boolean/result message, lock slot, cooldown, validate area and dimension.
- Migration from previous version: 1.7.1/1.7.2 entity types are unchanged. New registry values introduced safely. Absent old items get new IDs; existing saved heroes not duplicated. Dedicated server safe; no static client references in common entity class.

## Audio attachment
Manifest is explicit relative file mapping. For each `.ogg`, place into engine sound folder preserving naming, create `sounds.json` events, no broken resource references. Use subtitles map. Avoid bundling large raw WAVs in final jar.

## Final report format
- `Implemented`: grouped by entities, items, encounter, world, audio, UI.
- `Modified`: exact source/resource file paths.
- `Verified`: commands `/give`, `/summon`, build command, survival test observations.
- `Known limitations`: only real unresolved technical issues; do not claim success if animation or egg spawn fail.
