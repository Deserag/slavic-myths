# MARINKA — animations and transform timelines

20 ticks/s; angles degrees; `t` normalized 0..100. On hit event, server dispatches one projectile/entity damage event with `attackSequenceId`; visuals are deterministic interpolations. Blend time 0.10–0.16s except shapeshift 0.22s cross-fade when available.

| Clip | Duration | Keyframes and visuals | Event |
|---|---|---|---|
| M01 `idle_hex` | 2.8s loop | 0 palm coal ember; 25 robe hem sway 5°; 50 red ember hover .1b; 75 braid delayed sway 7°;100 neutral | whisper/ash 8–14s |
| M02 `run_caster` | .72s loop | 0 left foot forward, robe lift .1b; 25 airborne micro hop .04b;50 right forward;75 arm stabilizes spell;100 repeat | steps when ground |
| M03 `curse_tracks` | 1.45s | 0 inspects target footsteps; 18 bends hand to ground; 38 traces three claw marks; 52 orange-red glyph appears on target tile; 68 stands/snap fingers; 100 recover | track telegraph at t38; arm at t68; delayed burst per script |
| M04 `witch_fire` | 1.1s | 0 hands close; 22 left palm spirals red; 45 torso yaw +20°, sweep cast; 60 particle tongues unfold; 100 reset | projectile at t50, floor wave delayed |
| M05 `luring_chant` | 2.0s | 0 ritual palms open; 23 inhalation, head up; 50 sound ring pulses +.5b; 75 draws hands inward; 100 | charm pulse t55, warning from t23 |
| M06 `shadow_vanish` | .90s | 0 raises right hand; 23 dark silk spirals calves; 45 torso dissolves to smoke columns; 68 real hidden temporarily; 100 three bodies emerge at markers | server spawn two decoys at t70, real relocates in fair valid cell |
| M07 `shadow_clone_cast` | 1.2s loop | identical idle/fire casts for all 3, randomized mirrored minor body movement using same distribution | no identity leak |
| M08 `barrier_raise` | .85s | 0 arms across chest; 29 opens forearms; 45 runic crescent at .6b radius; 68 elliptical shield full;100 sustained pose | reduction begins t45; no impossible instant last-second activation |
| M09 `barrier_crack` | .56s | 0 stiffens; 20 shield cracks in 5 radial sectors; 40 bright short burst;70 flakes dissolve;100 open stance | 8s vulnerability start t35 |
| M10 `blink` | .78s | 0 turns head to arrival area; 20 front glyph flashes, red silhouette ghost; 43 fades from start; 64 rematerializes in prevalidated space; 100 regain idle | server position switch at tick 10, endpoint emitted beforehand |
| M11 `magpie_change` | .75s | 0 shoulders rise; 35 black feather silhouette; 60 visible bird wing joints;100 full bird | switch hitbox at tick 9 |
| M12 `magpie_flight` | 1.30s | 0 wing spread; 25 wings -35°;50 up +48°;75 down -42°;100 glide | bounded arc 6–12b, bird vulnerable to ranged hit after t35 |
| M13 `magpie_return` | .65s | 0 descend/perch smoke;40 wings fold, bird silhouette collapses;70 full human;100 cast guard | collision shape swap tick 9 |
| M14 `cast_aurochs_curse` | 2.35s | 0 crouches tracing a track; 20 golden ring around footprint;36 three rune pillars between her and Dobrynya;55 wrists clamp, golden glyph pulses;70 cursed hero transforms;100 exhausted recoil | anchor appears tick 23, Dobrynya transform begins tick 25; player can interrupt if counter earned |
| M15 `call_cursed_aurochs` | 1.18s | 0 jaw clenches; 36 fingers hook;58 hoof echo wave from marks;80 glow spreads;100 idle | optional herd cast, hard cap count |
| M16 `hurt` | .4s | 0 torso twist;40 recoil -13°, braid overshoot;100 recover | nonverbal hurt on real or clone, same sample |
| M17 `rage` | 2.10s | 0 gasps;24 tremor -6°;48 circles rise from beneath;75 higher brighter eyes+robe;100 return | at first HP<=180 only, once |
| M18 `cast_exhaustion` | 1.25s | 0 hand to chest;25 leans on knee;65 hunched breathing, fire shrinks;100 recovers | telegraphed attack opportunity |
| M19 `defeat` | 2.55s | 0 stagger;20 falls to kneel;42 pendant cracks;68 rune light extinguishes;88 settles;100 fades or body stays per repository convention | one victory flag; entrance lock release; basement unlock |

## Shadow trick visual principles
Do not show which of three is real before clones die (including death particles, bossbar, buff icon, entity hitbox color, debug hints in normal client HUD). All three use identical attacks and audio. Example placement after vanish: 3 nodes chosen 8–14 blocks from player, angular separation ≥55°, all on walkable, visible land and in court. Random shuffle of real location, cryptographically not needed but stable server-side RNG. No false body spawning into occupied block. Clone destruction after HP threshold in 0.45s smoke burst, and only then reveal falsity. Distinguish clone death from boss death: no achievements, no item drops.

## Magpie appearance and motion
Bird emerges with **black-and-white** wings, never a pure black raven cloud. During magpie exit leave brief feather trail, landing has circular pale dust; no teleport pop unless interrupted. If player shoots it mid-flight, stagger and force early return but avoid permanent airborne frozen state.

## Environmental animation cues
The yard ritual lamps react to phase 3 on a timed pulse, floor runes light progressively BEFORE ground damage, basement stone ring does not react until actual win, cloak and braid do not keep animating after entity removal. For low-effects graphics mode, preserve floor hazard boundaries by textured particles and block decals.
