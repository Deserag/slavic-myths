# 1.7.4 — NO fortress build; compact contextual event only

This chapter does NOT require a new physical castle, new custom biome, new natural worldgen or a giant overworld structure. The previously proposed 55x55 fortress is explicitly REJECTED from scope. Kartaus can be summoned from a reusable Black Banner Seal on an ordinary safe surface. Combat effects provide ambience (three short-lived black-red pennant VFX markers and floor rune markers). Reuse existing arena/boss zone logic if current mod already has it. No permanently placed blocks.

## Preflight at item use
- Use player's current dimension; default restriction Overworld unless current boss encounter system already supports safe other dimensions. No opening global portals/teleport dimension. Player cannot be in flying-only or spectator survival context.
- Spawn center = safe ground near player, target clear radius >=15 blocks and vertical clearance >=5; verify at least 80% of sampled candidate positions are walkable solid nonfluid and not inside claims/protected tile entities. **Sampling**: center, 8 cardinal/diagonal at r=7, 8 at r=14, max 17 raycasts; do not scan every one of 32x32 positions.
- Need no more than 2 blockers (trees/huts) inside inner r7; otherwise fail with localized text `Недостаточно открытого места для боя` and NO spell circles spawned. Outside r15 environment can be uneven; boss and minions pathfind within <=24 r. If existing event center requires 24x24 flat, adapt within same overall budget.
- Spawn center r=7–9 ahead of player in valid ground with LoS; Finnist stands behind player if already summoned. Default encounter practical area diameter ~34 blocks, not a sealed boundary. Warn if player runs >36 blocks: boss loses target and one 100t warning, at >48 for 160t cancel with no rewards, cleanup.

## Temporary visuals to make a 'boss fight' readable
1. At cast t8 place a disk of 8 angular rune particles 0.4 blocks above ground (choose at least one visible outline even reduced particles), then fade by 40t. Not replace grass/stone; particles only.
2. At boss spawn near center, four dim black pennant *particles/decorating* silhouettes as VFX for <=40t; NOT permanent guard towers.
3. Actual Kartaus 'Black Banner' combat ability plants damageable banner entities up to 2 (separate from preview decoration). Each banner emerges by animation; occupied 0.8 x 1.9, blocks LOS only if existing entity collision API supports non-solid, no wall-of-banners maze. Destroy on victory/abandon.
4. Dark ground decal danger circles drawn as client-only particles / projected quads or light outline entities, use current renderer and no new shader. Never overwrite blocks or reserve chunk forever.
5. Low intensity ambience wind/horn radius <=24, not continuous loud music.

## Spawn collision / existing structure safety
Never move/break blocks, place real fire, carve rooms, grief farms, or spawn guard inside a villager home. No loot chests. Story clue can be the item tooltip. Despawn summoned minions and rune entities on restart/expiry. Any event terrain change is disallowed. If owner logs off mid-fight store only story encounter reset token and completed victory flags, cleanup live event; do not keep boss ticking while offline.

## Boss defeat / progression
On server verified death: remove banners/guards/curse chain, play short victory effect, restore Finist summoning behavior, mark owner victory + participation. No cinematic chest, rare relics, unique material drops, new achievements or quest reward this patch. Boss 'creative egg spawned' must never set narrative victory flag, nor be treated as an encounter. Preserve flag until v1.7.6 reward migration even if the story quest UI is not yet built.
