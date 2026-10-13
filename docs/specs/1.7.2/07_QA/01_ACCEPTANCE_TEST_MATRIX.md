# SLAVIC MYTHS 1.7.2 — TEST MATRIX (must run, not just read)

Mark each `PASS`, `FAIL`, `NOT_RUN`; show environment and stacktrace for failures. EITHER actual game execution or the appropriate automated/unit harness is required; don't falsely claim successful runtime tests from static file presence alone.

## Art, rendering and rigging
- V01 Dobrynya final accepted white long hair and SHORT light stubble, explicitly not full beard. Head face visible front/side/back/top.
- V02 His gray/silver metal visibly dominates torso+shoulders; Ilya and Dobrynya immediately distinguishable even at 8b.
- V03 Dobrynya height exactly 2.4b when compared beside vanilla player 1.8b (user sometimes says "1 block" pictorially: physical actual player 1.8). Reference art is comparative mood; physically verify 1.8b player.
- V04 Spear measured tip-butt 3.6b ~=1.5x model height, no floating disconnected shaft, no sword model leakage or clipping hands.
- V05 Shield 3D thickness and true wrist attachment; cape folds and hair animate; no duplicate renderer/ghost shield.
- V06 Dobrynya full UV has no scrambled orientation or mirrored emblem; front and back plates differ; at least 64x64 base textures, preferably 128x128.
- V07 Marinka model height 2.1b, black braid with hair ribbon and aged dark red layered dress; no generic villager witch face/hat.
- V08 Marinka particle palette fire red, clone smoke charcoal, aurochs curse gold, shield ruby. Effects legible at 8–16b low graphics.
- V09 Magpie form recognizably black-and-white, not raven; wings flap visibly and reset on return.
- V10 Aurochs has broad gold horns, dark brown body, and is friendly to player even mid-charge.
- V11 All clips contain real bone transform animation, not just particles on a static model.

## Dobrynya combat & state
- D01 Spear long thrust hits at <=3.6b in front and misses a target behind stone wall.
- D02 Triple-combo hits exactly 3 timed distinct events at most; no per-tick damage loop.
- D03 Rotating sweep hits enemy in arc, respects friendlies and target idempotence.
- D04 Successful physical parry reduces one strike and performs one clear counter hit, cooldown works.
- D05 Magic shield reflects eligible single projectile but not floor curse/environment AOE.
- D06 Spellbreaker changes Marinka barrier state and creates a full 8s vulnerable window.
- D07 With <=15% HP, hero retreats once, has bottle-drinking animation, heals to capped level, never permanently idles inside solid block.
- D08 While cursed into aurochs, no spear/sheath render and no human skill; revert correct HP/gear/UUID.
- D09 Dying during retreat marks ally unavailable for encounter, does not free summon slot for another hero.

## Marinka AI, clones, spells and balance
- M01 Boss starts at 600 actual HP, not 800. Phase1->2 at 390, phase3 at 180.
- M02 Barrier reduces test hit 30%, never active through disappear or magpie; broken after deflect counter for 8s.
- M03 Exactly 3 bodies after shadow cast: 1 real plus 2 decoys, not four.
- M04 Before players damage the forms, 3 bodies have same sound/particles/face/clothes/nameplate/animation distributions; screen capture cannot identify true actor.
- M05 Decoy receives cumulative 24HP then smoke-dies without boss progress/drops.
- M06 Damage to real boss reduces original 600HP bar; real persists without special pre-hit outline.
- M07 Clone simultaneous projectiles obey group-wide damage cap; not triple one-shot from three 6HP attacks.
- M08 Boss can't spam clones; cooldown >=35s after end. No endless clone loop on reload.
- M09 Witch blink 8–12b within arena and cannot teleport into wall, closed basement, unloaded chunk, outside fight leash.
- M10 Bird flight is visible and can be interrupted; returns to valid standing cell.
- M11 Footprint hex is visible and avoidable with sensible movement; proper Weakness+Slowness durations.
- M12 Heavy fire center hit (no defense buffs, full diamond, 24HP) subtracts >=6 actual HP.
- M13 Minor side hit/tile tick subtracts significantly LESS than 6HP, resists unintended multiple same-tick hits.
- M14 With valid ward/cover/Protection bonus, damage may reduce rather than forcing unmitigated min.
- M15 Lure does not unavoidably pull into immediate heavy fire; 1.25s safe reaction gap.
- M16 In rage, after 2 casts, real 1.25s recover window; player can land a hit.
- M17 HP0 cleanup cancels hazards/clones/minions and boss bar, victory occurs exactly once.

## Quest, structure and basement
- S01 Yard generated via existing trigger or explicit safe test method once; 60x60, clear 40–45 diameter center and many detailed perimeter features.
- S02 House has grounded beams/broken roof with interior prop logic and support under uneven terrain.
- S03 Well cannot induce unavoidable lethal fall, travel around barn/trees/cart remains navigable.
- S04 Story entrance valid Dobrynya and player quest -> intro and boss; incompatible hero -> localized denial; no rogue summoning.
- S05 Normal world allows 2 hero slots as before; Marinka personal fight permits Dobrynya only; transformed/retreating hero slot stays locked.
- S06 If Ilya already present he is safely suspended and later restored without being killed or permanently duplicated.
- S07 Break 80HP/3-hit gold anchor -> reverse aurochs state + Marinka 8s window. Anchor never behind wall.
- S08 Losing battle does NOT open secret hatch, nor store boss victory.
- S09 Winning once causes seal break -> hatch opens 2.8s after defeat, safe 2b stair.
- S10 Basement contains craft workshop, transformation hall and north hideout; all navigable and visibly furnished.
- S11 Ordinary chest loot does not respawn when server reloaded or player revisits.
- S12 Boss defeat recorded per player; after patch v1.7.6 retroactive unique reward eligibility possible without replay. No unique reward already dispensed.
- S13 Server restarts mid-hatch: after recovery hatch in a coherent open state and no duplicate buried room.
- S14 Player logout mid-clones/curse cannot leave frozen 3 Marinkas or permanent spell lock.
- S15 Two players cannot both create fight at same yard; protected area and world-chunk checks work.

## Assets / localization / performance
- A01 Every declared sound event points to a real playable OGG in mod assets and no missing registry path.
- A02 No copyright movie voice/media present, attributions/provenance manifest includes all audio.
- A03 All hero/boss skill subtitles have Russian and English entries; UI not revealing true clone.
- A04 GUI at scales 2,3,4 without overlap; low particles still preserves telegraphs.
- A05 10-minute battle and multiple boss restarts not causing unbounded entity/particle count.
- A06 Dedicated server loads (no client-only class in server init), current project build passes, actual JAR generated.
- A07 Localized title/version remains 1.7.2; no project-wide migrations or 1.7.1 regression.
