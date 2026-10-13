# DOBRYNYA — animation clip contracts, keyframes and events

Units: seconds; 20 ticks per second. Root entity yaw vs local bone rotation as supported by current animation library. Rotations below are degrees, not radians. Blend transitions 0.10–0.18 s unless explicitly specified. Animation curves ease-in-out except hit frames which use sharp cubic-out. Server combat events occur at fixed timestamps; client animation cannot apply a second damage event.

| ID | Clip / duration | Explicit pose/action keyframes, normalized percent | Server event | Root motion |
|---|---|---|---|---|
| D01 | `idle_guard` 2.4s loop | 0: spear vertical 12deg rear, shield 15deg toward threat; 50: slight breath chest +.02b, shield tip -3deg; 100: reset | ambient breath every 8–15s | no |
| D02 | `walk_ready` 0.80s loop | 0 L leg +21°, R -21°; 25 both 0; 50 R +21°, L -21°; 75 both 0; hands spear 12° counter sway | footsteps on 0,50% | normal nav |
| D03 | `side_step` 0.36s | 0 stance; 20 knee crouch .08b; 55 lean ±12°, step sideways .72b; 100 spear guard snap back | dodged body window ticks 4–9, NOT invincible vs AoE | 0.7–1.2b side |
| D04 | `parry_spear_shield` 0.62s | 0 spear diagonal, shield low; 18 shield rise 55°, shoulders yaw -14°; 32 impact frozen 2 ticks; 47 spear flick +18°; 72 thrust rear->front 40°; 100 reset | perfect-parry check ticks 4–8; counter hit tick 10 | <= .20b |
| D05 | `three_hit_combo` 1.48s | 0 ready; 10 spear backswing -32°; 18 hit1 horizontal slash; 35 second windup +25°; 43 hit2; 61 recoil; 76 hit3 diagonal downward 45°; 100 reset | hits ticks 6,13,23 (one event each; guard against multiple targets per frame) | each <=.4b |
| D06 | `long_thrust` .88s | 0 crouch; 19 shift hips +.15b and shaft -15°; 39 thrust spear forward 1.0b; 54 short hold; 80 withdraw; 100 guard | damage tick 8; hit range <=3.8b excluding shaft tip clipping | lunge <=1.1b |
| D07 | `circular_sweep` 1.23s | 0 pole across shoulders; 20 torso yaw -75°; 48 yaw +115° spear flat around hip height; 76 final arc +200°; 100 reset | arc samples ticks 9 and 15, each target at most once per attack | rotate in place |
| D08 | `ward_guard` 1.05s | 0 shield low; 18 shield front, knees bend .1b; 38 blue-white ring appears; 65 receives/redirects projectile; 90 fade; 100 relax | spell deflect ticks 6–13, each projectile once | none |
| D09 | `unravel_hex` 1.08s | 0 spear shaft vertical; 24 raise tip and shield; 56 bright steel spark at spear tip; 61 precision thrust into sigil; 100 recover | break spell anchor at tick 13 if within reach and state valid | <=.5b |
| D10 | `hurt_light` .36s | 0; 35 head roll -10° and shoulders back; 100 recover | grunt/foot skid tick 1 | <=.1b |
| D11 | `hurt_heavy` .75s | 0; 21 braced shield angle 42°, knee sink .15b; 58 head shake; 100 recover | heavy grunt + armor shock | <= .2b |
| D12 | `transform_to_aurochs` 2.20s | 0 stunned, spear lowered; 18 hands seize shoulders; 32 body crouches 0.5b; 45 rune coils around; 65 player-facing silhouette crossfade to taurine renderer on server entity state; 100 four hooves grounded, glow golden horns | server switch to transformed state at tick 29, ensure one entity UUID preserved | root stationary |
| D13 | `aurochs_charge` 1.38s | 0 hooves scrape; 18 head down 20°; 37 accelerate; 50 horn contact; 75 overrun brake; 100 reset | single charge impact tick 14; decorative collision break once | 5–8b, clipped against arena bounds |
| D14 | `transform_return` 1.50s | 0 hindquarters stop; 22 runes fracture; 46 silhouette regains human proportions; 78 spear/shield summon visually to hands; 100 spear rally | server switch back at tick 15, retain HP/cooldowns | stationary |
| D15 | `retreat` 1.25s | 0 weapon lower; 30 shield guard; 55 turns toward cover and jogs; 100 begins recover | trigger at <=15% HP once; leave aggro, never teleport into wall | scripted path to cover |
| D16 | `drink_elixir` 3.15s | 0 seated/half-kneeling in cover; 18 bottle from belt; 31 bottle to lips; 51 drink; 68 lowering; 80 slow exhale; 100 reset | sfx at ticks 18–38, healing pulse ticks 44/60, never while attacked | none |
| D17 | `return_rally` .90s | 0 stand up, secure bottle; 34 spear upright; 62 shield angled; 100 return to ally | back to ready state only after valid recover state | <=1b |
| D18 | `victory_salute` 1.70s | 0 lowers spear; 40 raises to diagonal; 65 nod and cloak settle; 100 idle | subtitle once; no reward item | stationary |

## Weapon and shield constraints
Spear point must not swing through body/shield. Player-facing telegraphs show spear orientation before attack. For circular sweep, real hit wedge + swept sector roughly 210° around root in facing direction with radius 3.2 blocks; no phantom hits from hidden spear trail. Shield blocks magic in visual projected arc ±55° frontal to facing and height ~2 blocks. `parry` success distinct amber/steel sparks vs `ward_guard` blue-white runic block. Cloak panel rotations limited ±18°, hair bob ±10°, no floppy realism.

## Blending / interruption / state invariants
- One attack active at a time. Interrupt priority transformed > retreat > hard stun > ward_guard > parry > strike > walk > idle. Summon/despawn cannot orphan invisible spear mesh.
- At transform start remove weapon/shield hitboxes from combat without removing persistent equipment data. Only reveal equipment when human again.
- At retreat below 15%, release active threat, stop combat pathing and choose bounded cover; if no valid cover, fallback phased guarded recovery (still damageable) near boundary rather than unbounded teleport.
- Each timeline dispatched by server-defined action start tick and sequence ID; client interpolates; joining mid-clip reconstructs current state and partial clip.
- Walking animation speed tied to true movement speed; no visible gliding; damage frames not tied to FPS.
