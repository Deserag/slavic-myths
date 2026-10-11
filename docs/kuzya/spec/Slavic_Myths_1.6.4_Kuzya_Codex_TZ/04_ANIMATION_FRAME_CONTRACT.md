# Rig animation system, frame timing, interpolation, sync

**Non-negotiable priority.** Current Earth Spirit animation regression must NOT be repeated. Do not ship clips with unchanged mannequin pose and named JSON files. Codex must animate actual bones, record transitions, prove correct hitbox, attachment and sounds.

## Units, synchronization
- Game = 20 ticks/second. Anim sequence authored at 20–30 keyed FPS and evaluated by partialTick interpolation. World positions pathfinding server-side; bone poses client-side keyed by synchronized animation-state ID/start tick. Interpolation: Catmull/smoothstep for body and pelvis, Hermite for arm swings, ease-in/out for staged sitting and getting up; foot contact pinned for walking, no foot sliding.
- `root` transforms world-position independent; do not move entity to mimic movement. Client replay based on state/tick; on state join/start playback at correct normalized phase, not t=0 every packet.
- Animation priority: `sleep`/`lie`/`sit` main-body state > locomotion; `eat` overrides upper body while seated; `dialogue face` additive but cannot override sleep eyes unless waking; `reaction` overlay shoulders/head only; `blink`, `breath`, `hair_secondary` lower layer. Crossfade 0.18–0.35 s.
- Twist head yaw ±35° normally, ±45° player glance; rapid snap forbidden. At tiny height mouth movements must still correspond to voiced sounds.

## Character acting rules
Low center of gravity, short hesitant strides, tiny torso counter-rotation, puppet-like 10–20° bobs (not robotic). Scowling with brow tilt when visitor overstays. Grumbling: clear eye contact, left hand hand-to-chest then dismissive arm wave. Eating: repeated pickup-to-mouth-bite-chew-swallow, actual food prop held from table. No hammer-like idle arm movement.

## Mandatory full clips and durations (see machine-readable manifests)
`idle_stand` 3.6s loop; `idle_sit` 2.4s loop; `idle_look` 1.4s; `blink` 0.18s; `walk` 0.9s loop; `turn_left/right` 0.55s; `approach_table` locomotion; `sit_down` 0.95s; `stand_up` 0.92s; `pick_food` 0.65s; `eat_bite` 0.95s; `eat_chew` 1.10s; `eat_swallow` 0.47s; `climb_stove` 2.1s; `lie_down` 1.15s; `lie_idle` 2.8s loop; `sleep_enter` 0.95s; `sleep_loop` 4s loop; `sleep_turn` 1.4s; `wake_up` 1.30s; `dismount_stove` 2.0s; `domestic_inspect` 1.9s; `domestic_sweep` 2.1s; `notice_guest` 1.15s; `mutter_small` 1.6s; `grumble_medium` 2.2s; `grumble_large` 2.45s; `shoo_guest` 1.8s; `respond_right_click` 1.45s; `react_to_yaga` 1.75s; `carry_bowl` 1.4s; `place_bowl` .7s.

### Clip composition
For each clip there must be (a) explicit curves for at least root, head, shoulder_L, shoulder_R, foot_L, foot_R; (b) authored keyframes, not computed sinusoid only except breathing/blinking; (c) a sound event schedule; (d) a completion signal that AI state uses (not world tick guess); (e) verified model not self-intersecting. Full samples in `manifests/animation_keyframes.csv` and detailed sheets `05` through `09`.

### Hard validation
`walk`: each foot has ≥0.28s foot contact followed by lift clearance ≥0.20 model px; head counter-bob ~1.5–2°; hands move opposite legs; no more than 5% stride distance drift against foot contact. `sleep`: eyelids closed, torso oscillation ~0.03 model px at breathing frequency; not stuck half-transparent. `eat`: visible bite transition with bowl/food attachment; mouth opens when hand reaches face; sound *on* bite. `shoo`: attention→gesture→return, not instant arm toggle. Sitting must match seat height chosen in structure. Stove climbing must physically traverse assigned stove access.
