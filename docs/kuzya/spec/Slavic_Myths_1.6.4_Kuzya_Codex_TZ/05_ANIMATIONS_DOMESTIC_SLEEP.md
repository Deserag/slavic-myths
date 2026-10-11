# Domestic motion / stove / sleep: prescriptive choreography

## `climb_stove` (2.10s / 42 ticks)
(0–.20s) Kuzya stands 0.28–0.48 b from stove stair, looks up head pitch −18°, right hand +15° reaches forward, knees bend 12°, hair follows 80ms later. (.20–.58) left foot lifts 0.21 b, places on lower step; pelvis +0.12 b then weight transfers, right hand contacts stove corner. (.58–1.08) right foot pushes, torso leans 14° forward, arms stabilize 25°/35°, knee joint visible; move along a collision-checked scripted short step OR ordinary navigable slabs, NOT teleport to bed. (1.08–1.55) rotate pelvis 30° toward ledge, plant feet, rise up 0.20 b, short cloth rustle. (1.55–2.10) move toward bed anchor, body uncurls, head aims left or right, no foot penetration. `stove_scramble_01` on frame 6, `step_wood_01` on 13, `cloth_rustle_01` on 28.

## `lie_down` (1.15s / 23 ticks)
0.00 standing/crouched at bed edge; 0.22 left hand on blanket, torso pitch +20°, knee 30° bend; 0.45 pelvis drops by 0.09b; 0.64 torso rolls ~−45° about forward axis and head tilts opposite +10°; 0.85 pelvis rotated to side, arms folded near cheeks; 1.15 final eye half-closed and head resting on bedding. Never clip 40% of head through blanket; blanket collision surface at predefined anchor height. Foley `stove_settle_01` at .65.

## `sleep_enter` (0.95s)
0: eyes 80% open and head slightly rolling; .30: one slow blink 0→75% lid; .56: lid 100%, mouth parted slightly, right shoulder drops 1px; .95: closed lids final. `sleep_loop` 4.0s with 0.03 modelpx chest rise at 0,2,4 seconds, mouth 0.1px yaw change every 2 s, hair very small follow motion; random `sleep_turn` after 90–180s ±5s at most, breath/snores 35–70s apart. Snore sounds never every tick.

## `wake_up` (1.30s)
0: body still lying, eyes closed; .22 eyelids twitch, mouth opens yawning 0.8px; .50 lift head 0.5px, head tilt +15°; .75 elbow supports upper torso, shoulders rotate 30°; 1.00 seated spine vertical 70%, gaze unfocused; 1.30 eyes open then blink, hands drop, sigh/wake rustle only once at .9.

## `dismount_stove` (2.00s)
0 sit on edge; .3 extend feet down; .75 foot lands on intermediate collision-safe slab; 1.25 hands brace, center-of-mass forward; 1.65 second foot reaches floor; 2.00 stand, stabilize. Avoid instant drop of .8 block or fall damage flicker. Audio footsteps at proper foot landing only.

## `idle_lying` / `idle_sit` / `sit_down` / `stand_up`
`sit_down` .95s: 0 look seat→.25 rotate pelvis +8°→.5 knees 65° and torso 12° forward→.75 pelvis lowers ~1.4px→.95 seated, soles on ground. `stand_up` reverse with different ease curve: body leans forward first then legs extend, not frame reversal. `idle_sit` torso moves 0.02px, glance every 2–7 seconds; never shuffle hard against table collision. `lie_idle` shifts shoulder to cushion .04px across 2.8s, arm over belly, eyelids occasional slow half blink.

## Environmental fit tests
Use a debug visualization of bed_anchor, intermediate_step, floor_anchor, table_seat. Each should be inside same hut interior bounding polygon, top Y valid and traversable. Test 5x spawn at different yaw and verify moves and returns, no repeated reset oscillation. Add fallback if stove too narrow: use alternate low-level stool bed at physically reachable surface **without changing approved visible standard hut bed** (only when world legacy layout blocks access).
