# 1.7.3 — hard scope, execution policy, prohibitions

**This archive is a detailed implementation order.** Do not answer with only a proposal or a generated TODO. Implement the requested assets and behavior. Output: game-ready source/resources, a compilable jar, concise proof checklist; do not simply place this ZIP into the repo.

## Pinned project context and boundaries
- Preserve actual modid, loader, build tool, package naming, registries and naming conventions. Prior plan targets `slavicmyths`, Minecraft 1.21.1, NeoForge; check only `gradle.properties`, build script and one main mod registration point to confirm. Do not migrate loader or MC version.
- Only examine classes necessary to extend **hero/summon**, **boss/spawn egg**, **sound**, **world structure**, **progress** and **boss bar** features. Search by exact known symbols/resource names. No total repo inventory, no sweeping decompilation, no full build-system refactor.
- Source authority priority: latest user-approved blond Alyosha sheet > Skim/valley sheets > numeric requirements herein > pre-existing prototype assets. Alyosha is **distinct from Ilya and Dobrynya** visually and mechanically.
- No gods, no Koschei, no Zmey Gorynych, no unrelated enemies, no changes to 1.7.1/1.7.2 combat except small missing summoning/accessibility additions. No unique loot/achievement/relic awards until planned 1.7.6. Vanilla-only filler loot in den is optional if already supported by loot tables.
- Do not invent persistent AI global tick scans or expensive chunk queries. Every scan is radius-bounded and interval-capped. No forced chunkloads to keep a far-off hero/boss alive.

## Existing interface/behavior preservation
- Existing 2-hero general cap remains; story personal-boss encounters enforce one **specific** hero in the active encounter. Skim fight permits Alyosha only. Idolishe→Ilya, Marinka→Dobrynya remain; future Kartaus→Finist. Final Kalin Tsar will eventually allow ANY TWO, not implemented now.
- A party slot remains occupied when a hero is in 15% HP 'Last Stand' recovery; player may not refill it mid-boss.
- Player health balance target is **24 health points = 12 hearts**, full vanilla diamond gear. Important unit clarification: `6 HP` = **3 hearts** after armor, before buffs/debuffs. Do not mistake 12 hearts for 12 health points. Existing health modifiers may differ; compute damage after armoring accurately via test harness instead of assuming pre-armor attack amounts.
- Do not award anything exclusive on first kill. Persist a per-player encounter completion and involvement record now for later reward grant migration. Do not remove existing boons/achievements; only avoid introducing new unique 1.7.3 rewards.
- No consumable use of hero-call items on success OR on failed attempts. Items do not lose durability, even in survival.

## Fast execution constraints
- Suggested exact-symbol lookup sequence: summon Sivka/Burka item (reference), existing hero entity/AI, existing story encounter locks, boss bar, spawn egg registration, structure placement, `sounds.json`, localization. Stop searching once interfaces are known.
- Keep implementations modular with a few small classes rather than expansive infrastructure rewriting. Reuse animation library already present, if any. Do NOT add GeckoLib or other external dependencies unless already in repo.
- Make a single representative structure/template with deterministic rotations and piece placement (no procedural multiroom dungeon system). Valley must be navigable with its designed layout. If structure tooling exists reuse it. Do not pre-generate thousands of blocks per tick: bounded chunks or structure templates, single generation at entry.
- Do not add unrelated recipes, config UI or multi-language novels; only strings listed.
- One short README summary with actual files edited and executed tests is enough; omit codebase survey essays.

## Rendering prohibitions
- No flat vanilla-skinned biped for Alyosha; build geometry with 32px+ textures and detailed volume/straps/scarf. No simple reskin of Dobrynya/Ilya.
- No Skim model composed from a standard wolf/lion 1:1 or 'generic giant quadruped'; unique mane segmentation, fangs, tail, red angular marks, true articulated fore/hind legs, jaw.
- No copy-pasted model textures at 16×16; body atlas **at least 128×128** Alyosha and **256×256** Skim; 32×32+ independent item textures.
- No violent camera hijack, forced zoom, exaggerated blindness, unbreakable fully enclosed boss pit, permanent grief of surrounding world, or untelegraphed hit behind player.
- Gameplay cues need to remain legible without particles for low graphics settings.

## Safety/ownership/concurrency
- Server-authoritative AI, damage, cooldowns, item usage, spawns, progression. Client only renders/sounds/HUD. Sync minimal state and ensure one summon command cannot duplicate entity.
- Every entity/arena generated holds owner/encounter UUID only if compatible with existing progression conventions; unload safely. Ensure no cross-world (dimension) ownership leak.
- Boss summon egg is a **creative/testing tool**. Survival encounter is started with a dedicated reusable Beast Horn at a valid valley/den entrance. If a storyline gate already exists, reuse it rather than inventing new global progression systems.
- Original audio in package (manifest under `07_Audio`) can be redistributed in mod. Do not extract film dialogue, copyrighted themes, actor voices or other third-party recordings without rights.

## Final delivery
1. Implement and build `.jar` with the current Gradle wrapper.
2. Smoke-test mob eggs and reusable summons in survival and creative; test server/dedicated environment if available.
3. Report actual tested commands, counts, failed/blocked tests, exact jar path.
4. Copy jar to the already configured launcher mods folder **only if** repository/environment reveals the exact path; do not guess a PolyMC instance path or overwrite unrelated jars without verifying filename/modid.
