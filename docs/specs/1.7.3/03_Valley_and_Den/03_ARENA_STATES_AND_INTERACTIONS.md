# Skim arena encounter, story rules and co-op choreography

## Story encounter lifecycle
`UNDISCOVERED -> VALLEY_PRESENT -> READY -> STARTING (60t) -> ACTIVE -> VICTORY (75t) -> COMPLETE` (plus ABORT/RESET). Any command/egg-spawned Skim outside designated arena is ordinary DEBUG BOSS with boss bar and combat but **cannot fake story victory** without defined owner/encounter context.

- START trigger: owner/player stands within 5 blocks of local marker (0,+27), **holds Beast Horn**, uses right click and meets story quest gate (if prior quest chain supports stage, use that). Item **not consumed** even on failure. If Elytra-flying from outside perimeter, still require marker and server validation. 60t introduction: muted screen subtitle `Долина Ским-зверя`, warning paw tracks, low growl, beast emerges from north outer area (0,-20); no untelegraphed spawn on top of player.
- During fight: only **Alyosha** may be newly summoned. Other hero calls show refusal; do not silently consume tools. `Skim` aggro driven by Alyosha taunts and player actions. One boss entity per encounter; respawn upon reentry only if existing encounter fully aborted/reset per persisted state. Owner cannot place additional boss via survival Beast Horn while `ACTIVE`.
- Party slots: Ally summon item nonconsumable, no replacement when Alyosha recovering. If he is not present on start, player can call him at allowed staging marker before combat; if missing after fight starts, blocked by agreed **summon before boss fight only** rule. Codex must ensure story boss UI tells player beforehand.
- Boss arena soft leash: crossing boundary x or z >32 prompts subtitle and 10s return warning, pushes returning combat AI not player forcibly; if no valid player inside boundary after 30s, boss disengages, then resets after 60s; preserve structure and item; clear damage effects and bound grabbed targets.
- Victory: on boss death animate corpse, open cave passage, server sets progress on all qualified participants, collapse temporary effects, stop boss music (if any), persist state once. No rare boss item/unique reward until 1.7.6.

## Team counterplay 1 — baited leap
When Alyosha's `BARBED_TAUNT` successfully attracts boss during `READY`, Skim leap may target him. Alyosha dodge t4–7 moves out of impact; boss lands and has 16+ ticks natural recoil for player to attack exposed side. If player remains within impact circle it can still get hurt — no global team invuln. Particle guideline: 2-block circle on the ground shown at least 12 ticks before landing once leap path is locked; do NOT track player after lock.

## Team counterplay 2 — rescue from jaw
If player captured, Alyosha within 8 blocks and dodge not on cooldown attempts a rescue combo with **two short saber hits** at jaw within 30 ticks, total rescue threshold 16 raw-equivalent: strikes can be designated special rescue damage so actual HP damage remains low. If Alyosha is grabbed, owner player may attack jaw or use bow; exactly one rescue resolution, knock victim safely ~2 blocks away, boss stagger 18 ticks. A trapped player can independently mash attack to accelerate release if fairness requires; no permanent capture while ally far away.

## Team counterplay 3 — exhaustion flank
After Skim spends stamina, panting lasts 70 ticks. Alyosha moves to opposite side of beast from player (prefer ±100° angular separation at radius4), does modest weak strikes and uses 1 dodging feint, while player can unleash higher damage. No dramatic automatic team-finisher that kills boss without input. If player absent, Alyosha doesn't solo kill boss fast.

## UX
- Boss bar normal Skim health (no extra boss invulnerability bar); fatigue indicated by posture, labored sound and optionally a small downward-arrow over boss during actual EXHAUSTED. Don't provide a hard-to-read full separate fatigue resource bar unless existing HUD supports it.
- Clearly show `Атакуй сейчас!` subtitle for 1.2s at exhaustion start only when owner nearby; avoid flashing every exhaustion tick.
- Red whistle rings, landing circle and forward claw sweeps remain distinguishable for low particles mode. All cues are in-world, not a sequence of tutorial popups.

## Performance
No giant world placement loops during active boss. Terrain props placed once; any temporary particles server packets localized to players within 48m; disable unnecessary persistent AI if area unloaded. Limit world scans to 65x65 local footprint only when generating and only once per generated valley.
