# Руна Преследования / Rune of Pursuit

## Identity and registry
- Proposed registry path: `slavicmyths:rune_pursuit` (adapt ONLY namespace to existing real mod ID).
- Source: Ским-зверь. Category: rune — rune stone to be slotted, not consumable on hit.
- Approved sheet reference: see `07_Visual_References/Approved/` and precise approved visual criteria below.
- Required player-visible localization: `item.slavicmyths.rune_pursuit` and full description tooltip, rarity, source, applicable slot; preserve existing font/UI styling.
- Supplied `07_Visual_References/Item_Textures_64/rune_pursuit.png` is a ready transparent RGBA 64x64 pixel-art inventory icon; register as an item model with correct atlas path, nearest filtering and no blurry rescaling.

## Appearance — binding visual requirements
- Icon/mesh silhouette: Amber shard with three deep diagonal raking marks and dark leather binding crossing near corners.
- Symbol/centerpiece: three claw cuts.
- Exact pixel bounding map: Shard x12..51 y8..56, amber highlights near x18..30 y14, three claw cuts descending x28..47 y22..46.
- Required five-color palette: #754414, #b86a1a, #f5b82e, #e09a20, #652319. Use no other unrelated primary hues; adjacent dark/light shades are fine for metal highlights and cracks.
- Texture edges stay pixel-crisp; do not replace with flat monochrome recolor; do not directly copy item models of another hero/boss.
- Icon should read at GUI 16px after nearest reduction and at 64px in texture editor. Provide in-game screenshot demonstrating both.

## Acquisition / recipe and source
Rune drops as first-victory claim; rare on repeats. Install via existing rune anvil.
- Drop/claim must derive from an authoritative existing quest or actual boss encounter event. Ignore fake client-only commands, test spawn egg abuse, creative test kills for first-clear progression. Do not consume hero summon whistles/tokens.
- Do not drop all five runes at once. Only this item's source battle provides this specific item.

## Gameplay behavior — exact specification
Three consecutive direct melee hits on the SAME living target within 4 seconds: third hit gets +15% final melee damage, then counter resets. Different target or >4s resets. One triggering hit per three.
- Equipped/installed passive effects are server-authoritative. When the item is removed, its modifiers disappear on the same server tick; no ghost benefits or lasting potion effects.
- No repeated proc per multi-part hit event, and no player/team friendly fire unless the vanilla attack is explicitly permitted by current PvP rules.
- For timers use server tick monotonic time while the player is online; persist enough item/player state to avoid resetting on re-equip, relog, death or dimension transfer.
- Never rely on a client-only tooltip number for damage or eligibility; real event handler must use identical constants.

## Visual effect and animation
- At rest in inventory: subtle normal item glint only when the existing rarity system expects it; do not add constant full-screen particle spam.
- On successful activation: emit short event-synced VFX on authoritative trigger; 1–2 bursts with lifetime 8–16 ticks, particles at entity/equipment position, no loose particles left after chunk unload.
- Use colors anchored in #754414, #b86a1a, #f5b82e, #e09a20, #652319; the VFX should imply 'three claw cuts' but remain readable alongside existing boss telegraphs.
- For wearable parts: player rig must inherit vanilla player animations, first- and third-person equipment slots, idle, running, crouching, swimming, mounted movement and shield/item use without z-fighting.

## Sound and accessibility
- Cue should play once per activation with short positional range and a corresponding existing subtitles mechanism entry.
- Approved original synthesized effects are in `08_Audio/ogg`; do NOT take audio from cartoons, copyrighted movies or unrelated game audio.
- VFX-only loss must not hide game state: use proper tooltip, buff icon or subtle text/subtitle as appropriate to existing mod UX.

## Implementation test cases
1. Create item in creative test inventory: identifier, RU/EN name, RGBA 64px texture, tooltip and stack limits work.
2. Obtain through correct boss/hero source, with source-specific first-clear state and no duplicate one-time claims.
3. Ensure unrelated boss cannot yield it; creative spawn eggs cannot mint free story rewards.
4. Verify correct activation and negative case (wrong slot, cooldown, no eligible target).
5. Equip/remove/re-equip, kill/revive, change dimension, relog: no duplicated effect or cooldown reset exploit.
6. Check renderer first- and third-person and at least three accessibility scenarios (low particles, sound muted, no enemies nearby).
