# Falcon controller, detection, sync and boss curse

The companion is an ability representation, not a new grindable creature. A `FinistFalconController` may be in the hero instance or a small child entity depending on existing code. Keep 1 instance / owner, avoid additional per-player global poller.

STATE: PERCH -> TAKEOFF -> SCOUT / DIVE / INTERCEPT -> RETURN -> PERCH; during Finist morphed => MORPH_HIDE; during Kartaus curse => CURSED -> ANCHOR_BROKEN -> RETURN. All transition server initiated and synchronized. No explosion-on-dismiss or extra drops.

Scout every 20 ticks: choose up to 3 nearest threat candidates within horizontal 20, vertical 8 from owner, exclusions tagged friendly players/own minions/allied NPC; only highlight entities visible from bird position by LoS raytracing, not red wall-hack glow. Danger marker <=100t, no more than 3 concurrently, no particles beyond settings. If no candidates, return after 100t. Do not repeatedly scan all loaded entities.

Kartaus curse specifics:
- Cast requires `companionPresent && !alreadyCursed && phase>=2`; not allowed to hit Finist morph state—postpone, no wasted cast. Choose walkable anchor ~5 blocks from boss, >=4 blocks from owner, within 17 blocks of encounter center, with unobstructed hand reach (ordinary 3-block attack range) and no placement in air/lava.
- At cast t27 force bird CURSED and create one damageable 12 HP anchor visual; `CurseAnchor` owns boss UUID/encounter UUID and is visible as a dark violet rectangular talisman, not a permanent block. Show an emissive *low intensity* purple chain. Update position only at <=5t for chain rendering; no entity-block lattice each tick.
- Bird Watch/Guard/Dive disabled. Hero still attacks with sword and may inspire. If owner hits anchor to 0 HP, immediately terminate chains, release the bird, call 50t Kartaus stagger, grant 180t immunity to repeated curse. Timed release after 240t is automatic for accessibility, with a shorter 20t Kartaus stagger. On exit/death/despawn of either owner or boss, clear anchor and status.
- HUD progress: when cursed show `Сокол скован — разрушьте печать!` and small anchor icon with 12 HP (only when in owner vicinity). Subtitles: sharp metallic chain / distressed falcon, no panic flashing.

Party interaction: active story hero must be Finist; other heroes can't be summoned while encounter active. If companion curses when Finist retreats, curse cancels and anchor destroyed; enemy must not softlock. All mobs specific to encounter must carry its UUID for cleanup. Keep entity-count and network packets capped; no per-tick full NBT save for wing animations, only state+tick index+owner/target IDs.
