# Вий · версия 2.5 (Gogol)
Status: implemented in branch; game/runtime QA required. Work based on approved real Python voxel prototypes, not AI image as geometry.

## Game object
- \`slavicmyths:viy\` size 2.90 × 4.0 blocks; 2800 HP and attack 30 **provisional balance to confirm**.
- \`slavicmyths:viy_spawn_egg\` Creative egg; Russian \`Вий\` overhead and boss bar.
- Distinct weak servants (only summoned in phase II): grave upyr 40 HP/6 damage, faceless nav 32 HP/5, kornelap 24 HP/4. At most four alive simultaneously and vanish after death of Viy, no separate eggs or loot.

## Appearance and rig
- Big hunched barrel-shaped body; huge low craggy head; thick folded soil/iron skin and roots; exaggerated hands, short legs; no armor.
- Independent arms / legs; layered cuboid eyelids extend almost to ground, animated via compressing their root bone and lifting to expose tiny glowing eyes.
- Phase I: eyes periodically open for ~9 sec every 25 sec, also during Iron Hand. Phase II (50% health): always open.

## Eyesight gameplay
- Server-side test checks actual player's camera view against Viy's small eye target within 24 blocks, with block ray trace.
- Continuous 40 ticks (2 sec) or ten discrete short glances (>=3 ticks each, separated by >=3 ticks off) trigger +1 stage.
- Rolling three-minute 3600-tick memory for glances; applied curse refreshes expiry to 3600 ticks after the last trigger. Five stages; III blind+Wither II; IV Wither III +25% received damage; V 3s immobilization and 16 magic damage once.
- Stage levels and HUD effect have Russian translation. Triggers only when eyes open.
- Grasp replaced by Iron Hand: reach for player on ground, hold in front of face ~2.5 sec with camera directed at eyes via server teleport packet; once per grasp grant one stage; then throw. Landing and multiplayer camera synchronization require runtime test.

## Abilities
- Phase I: Iron Fist, Heavy Stomp, Ground Rift, Iron Hand; periodically opening eyes.
- Phase II: all above plus summon, Iron Finger marks target (minions chase), direct eye wave, multi-wave Last Gaze / Verdict.
- Hits telegraphed with particles; no attack interruption from knockback. Unspecified natural spawning, arena, reward/loot deliberately not added.

## QA gate
- Java21 \`compileJava\`, \`processResources\`, \`clean build\` and in-game tests required. Current branch has not run Gradle.
- Test actual render against approved screenshots, eyelid open/closed, half-HP transition, summon counts/lifetime and three variant shapes.
- Verify two gaze triggers, 3600tick decay, obstructed LOS, spectator/creative immunity, forced camera/hold, stage5 single damage, server reload, dedicated server and two clients.
- Verify atlas UVs, Creative egg, Russian locale and performance.
