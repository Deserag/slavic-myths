# Щит «Богатырский оплот» / Shield of the Bogatyr Bastion

## Identity and registry
- Proposed registry path: `slavicmyths:gift_ilya_shield` (adapt ONLY namespace to existing real mod ID).
- Source: Илья Муромец. Category: gift — permanent, non-consumed, unique story item.
- Approved sheet reference: see `07_Visual_References/Approved/` and precise approved visual criteria below.
- Required player-visible localization: `item.slavicmyths.gift_ilya_shield` and full description tooltip, rarity, source, applicable slot; preserve existing font/UI styling.
- Supplied `07_Visual_References/Item_Textures_64/gift_ilya_shield.png` is a ready transparent RGBA 64x64 pixel-art inventory icon; register as an item model with correct atlas path, nearest filtering and no blurry rescaling.

## Appearance — binding visual requirements
- Icon/mesh silhouette: Wide crimson heater shield, steel beveled frame and rivets, ornate golden sun burst, darker grip.
- Symbol/centerpiece: sun and shield.
- Exact pixel bounding map: Shield occupies x12..52 y4..60, top rounded; red face x19..46; golden radial central x24..42 y22..39.
- Required five-color palette: #7c1715, #a52e22, #f3c553, #8b929a, #433a32. Use no other unrelated primary hues; adjacent dark/light shades are fine for metal highlights and cracks.
- Texture edges stay pixel-crisp; do not replace with flat monochrome recolor; do not directly copy item models of another hero/boss.
- Icon should read at GUI 16px after nearest reduction and at 64px in texture editor. Provide in-game screenshot demonstrating both.

## Acquisition / recipe and source
Claim once from Ilya after completion of personal victory quest. Never consume hero summoning item.
- Drop/claim must derive from an authoritative existing quest or actual boss encounter event. Ignore fake client-only commands, test spawn egg abuse, creative test kills for first-clear progression. Do not consume hero summon whistles/tokens.
- Do not drop all five runes at once. Only this item's source battle provides this specific item.

## Gameplay behavior — exact specification
On fully-timed successful ACTIVE blocking of an eligible direct hit: emit radius-3 impulse, knock back nearby ordinary enemies, and deal EXACTLY 1.5 × the hypothetical mitigated HP damage of this same hit to its real attacker (after victim armor/resistance but before shield cancellation). Damage reflection on 35s cooldown; never chain reflect reflected damage.
- Equipped/installed passive effects are server-authoritative. When the item is removed, its modifiers disappear on the same server tick; no ghost benefits or lasting potion effects.
- No repeated proc per multi-part hit event, and no player/team friendly fire unless the vanilla attack is explicitly permitted by current PvP rules.
- For timers use server tick monotonic time while the player is online; persist enough item/player state to avoid resetting on re-equip, relog, death or dimension transfer.
- Never rely on a client-only tooltip number for damage or eligibility; real event handler must use identical constants.

## Visual effect and animation
- At rest in inventory: subtle normal item glint only when the existing rarity system expects it; do not add constant full-screen particle spam.
- On successful activation: emit short event-synced VFX on authoritative trigger; 1–2 bursts with lifetime 8–16 ticks, particles at entity/equipment position, no loose particles left after chunk unload.
- Use colors anchored in #7c1715, #a52e22, #f3c553, #8b929a, #433a32; the VFX should imply 'sun and shield' but remain readable alongside existing boss telegraphs.
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
