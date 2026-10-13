# Bayun animation — required clips, timing, keyframes, blending & QA

Target implementation: native model / Blockbench rig with independent bones, keyframe animator compatible with actual current renderer. Do not claim animated if all frames are root transforms or model static. 20 game ticks/s; render interpolate for 60fps; clip speed not tied to player FPS. CSV `09_ASSETS/animation/BAYUN_KEYFRAMES.csv` supplies **actual control points** with pitch/yaw/roll (degrees) and translations (blocks) for key bones, absolute local posing from rest. Apply axis conversion consistently, physically correct actual motion more important than code library brand. Animate both ears, tail segments, muzzle/jaw, all paws as appropriate. Transitions blend 4–8t except lethal action start 0–2t.

### Bone list, pivots
`root(0,0,0)`, `spine(0,0.9,0)`, `chest(0,0.88,-.42)`, `head(0,1.28,-.83)`, `jaw(0,1.14,-1.04)`, `ear_L(-.29,1.54,-.86)`, `ear_R(.29,1.54,-.86)`, `eye_L(-.17,1.37,-1.10)`, `eye_R(.17,1.37,-1.10)`, `shoulder_L(-.35,.92,-.43)`, `shoulder_R(.35,.92,-.43)`, `foreleg_L(-.35,.66,-.50)`, `foreleg_R(.35,.66,-.50)`, `paw_FL(-.35,.14,-.60)`, `paw_FR(.35,.14,-.60)`, `haunch_L(-.38,.77,.47)`, `haunch_R(.38,.77,.47)`, `hindleg_L(-.38,.45,.50)`, `hindleg_R(.38,.45,.50)`, `paw_BL(-.38,.12,.58)`, `paw_BR(.38,.12,.58)`, `tail_01..05(start x0,y .98,z.66)`, `ruff`, `claws_L/R`. Every bone in final exported animation is mapped to actual model file; no missing names.

### Required clip definitions
| Clip | Time ticks | Keys | Pose notes | Sound sync |
|---|---:|---|---|---|
| `idle_breath` | 80 loop | 0 20 40 60 80 | chest y 0/+0.018/0/-0.006/0, head pitch ±2°, tail tip yaw ±9° | `breath` subtle 1/5 cycles |
| `idle_sit` | 120 loop | 0 34 55 80 120 | hind haunch crouched, forelegs straight; eye blink at 34–39, left ear twitch 80 | `purr_soft` rare |
| `sleep_curl_in` | 34 one-shot | 0 7 14 23 34 | bend forelegs, lower chest 0.23, roll flank 12°, sweep tail across belly to muzzle, turn head inward; sleeping NOT standing | `settle` t=13 |
| `sleep_curl_loop` | 100 loop | 0 25 50 75 100 | curled hold, abdomen breath +0.015, one ear reflex at 50 | `purr_soft` every 3 cycles max |
| `wake_up` | 30 one-shot | 0 8 17 25 30 | eyes open first, head rise, paws extend, tail uncurls slowly | `mew_soft` t=20 |
| `stretch_front` | 46 one-shot | 0 9 18 28 38 46 | fore paws 0.46 forward, shoulders pitch 16°, head dips 0.28, hips lift +0.13, tail erect, return | `stretch_exhale` t=23 |
| `groom_paw` | 70 loop x1–3 | 0 12 20 28 37 52 70 | near forepaw to mouth, tongue/jaw strokes in 3 cycles, real elbow bend; switch paw on alternate loop | `groom` at 20,37,52 |
| `walk` | 24 loop | 0 6 12 18 24 | four-foot gait opposite legs offset, paws always find ground, spine oscillation ±0.03, tail counter-swing | `pawstep` t=0,12 |
| `turn_attention` | 22 one-shot | 0 6 12 22 | ears swivel first then head yaw ±26°, tail counterweight; torso follows ±8° | `ear_rustle` t=6 optional |
| `loaf_rest` | 36 one-shot | 0 9 21 36 | paws tuck, lower front 0.12, sits on sternum, head erect distinct from sleep | `settle` t=21 |
| `eat_milk` | 76 one-shot | 0 10 19 30 43 57 67 76 | head to 0.35 low, muzzle over gift placement, laps 3–5 strokes, head rises + tongue lip lick; consumed once server-confirmed | `lap_milk` ticks 22,33,44,55 |
| `eat_fish` | 65 one-shot | 0 12 24 32 41 53 65 | smell, lower snout, 3 bites/jaw motions, swallow, glance up | `chew_fish` ticks 26,35,45 |
| `pet_rub` | 28 one-shot | 0 8 16 22 28 | head cheek translate toward hand .14, eye slow blink, tail high curl | `purr_happy` t=9 |
| `threat_warn` | 70 one-shot | 0 12 26 48 70 | fur plates lift 0.05, back arches 10°, ears fold -22°, tail whips, pupil narrow, jaw opens | `warn` t=13 |
| `hypnosis_song` | 40 intro + loop 64 | 0 8 16 28 40, loop 0 16 32 48 64 | stable hind paws; jaw openings match 4-note motif, pupils/lights pulse subtly, ring particles sync to notes | `song_intro` t=4; `song_loop` beat 0 |
| `pounce_kill` | 30 one-shot | 0 8 14 19 25 30 | compress at 0-8, hind push at 8, front paws/claws extended at 14, actual server collision 17–21, land 25, recover 30 | `pounce` t=9, `claw` t=18 |
| `recover` | 34 one-shot | 0 8 18 34 | retract claws, lower tail and breathing, turn away to home | `breath` t=20 |

The clip animation file CSV includes representative keys for all named clips; complete additional joint motion with physically coherent interpolation. **Acceptance** needs screenshots/GIF captured of each clip at 0%,25%,50%,75%,100% and transition, with visible difference; animations cannot be missing silently.

### Sound event timing
Events trigger once per animation performance at specified tick (client trackers) using synced clip UUID+start tick; never per rendered frame or entity tick. Stop/attenuate loop immediately on scene abort. Simultaneously nearest players must hear correctly. Real sound file mapping in `SOUNDS_AND_MIX.md`.

### Repetition / idle weights
Outside encounter, home radius: idle_sit 28%, loaf_rest 20%, sleep 24%, groom 10%, stretch 7%, patrol/walk 11% in 30–90 second decision windows; DON'T random change every tick. Night sleep 1.3× weight. Wake on targeted approach. Cat should not continuously play loud sound or incessantly wag tail. When hostile prioritize threat_warn>hypnosis_song>pounce>recover, no clipping through belly or floor.

### Animation usability/performance
Head tracking yaw limited ±35° with 4t smoothing, clamp pitch ±18°; cat cannot rotate neck 180°. Jaw rotation max +25°, claws extended 0.12, ears within natural range ±22°. Pose bones do not reinitialize every tick while smoothly blending. Root motion for leap reconciles with server actual movement, not a fake attack sprite. Animation must work in paused/inactive chunk, world reload and ded server with NO client-only class loading.
