# ALYOSHA animation state machine and keyframes

All angles are degrees relative to neutral pose. One game tick=50ms at 20 TPS; interpolation smooth and deterministic, keyframed transforms must work without adding new animation dependencies. Upper body animation layered over lower movement; animation data synced by state/event ID, not constant streaming of every bone.

## Layer order and transition
1. Global locomotion IDLE/WALK/RUN/STRAFE/ROLL, 2. upper item pose SABER/BOW, 3. ability overdraw, 4. HURT/RETREAT/HEAL. Root motion server-authoritative; visual animation must follow actual entity displacement, never fake ghost teleport.
- IDLE: 48 ticks breathing chest 1.5° yaw, arm sway ±3°, scarf tail ±6° at 0/12/24/36/48; blink face every randomized 100–190 ticks (if face rig exists).
- WALK: 16-tick cycle leg ±24°, arms ±12°, scarf lag 4 ticks; RUN: 12-tick cycle leg ±37°, torso forward 9°, body bob 0.07 blocks; SHORT STRAFE: 10 ticks lower quick feet side stepping, head aims target.
- All ability windups visually distinct from normal locomotion; if interrupted (stun/retreat/death), play 4–6 tick blend out and disarm pending damage callback.

## Named animations with precise keyframes

### 1. BARBED_TAUNT / "Задиристый вызов" — 26 ticks, cooldown 12s
- tick0 neutral, saber down right, head faces target.
- t4 step forward .17, left arm points toward enemy +35° pitch, chin +8°.
- t8 hands wide (left yaw -25°, right yaw +20°), grin/brow raised, red exclamation and sparse gold pixels (never confusing with damage particles).
- t11 **server taunt applies** to eligible entities in line-of-sight within 14 blocks; produce audio sting and enemy focus cue (not global command lock).
- t16 step back .20, saber waggle yaw 20°, cloth lag; t22 return combat guard; t26 idle.
- In player's vicinity, a subtle gold attention tether only for ~8 ticks; no wallhack target marker.

### 2. QUICK_DODGE / "Богатырская увертка" — 12 ticks and 1.8s internal lockout
- t0 torso leans planned left/right 12°, foot plants.
- t2 lateral root displacement starts; legs folded unequal (-40°/+25°); yellow dust from planted heel (<=8 particles).
- t4–7 server-detected evade frames (4 ticks at most) while lateral motion continues: **no blanket immunity**; safe against one telegraphed direct melee/contact move, not ambient DOT, projectiles already embedded, large AOE outside escape radius, or lava/fall damage.
- t8 turn face toward attacker for camera readability, scarf swing +18°, t12 combat ready. Move 2.3–3.0 blocks horizontally with valid collision/navigation check, no pass-through walls or lava; abort if path not clear.

### 3. FLICKERING_SABER / "Лихая сеча" — 22 ticks, 3.8s cooldown
- t0–4 crouch 8° and wind right arm back 45°.
- t5 first right diagonal slash (attack hit event t6, raw 2.0 HP); t8 recoil.
- t10 horizontal left slash (hit t11, raw 2.0); t13 recovery.
- t15 short upper diagonal slash (hit t16, raw 2.4); t18 short ankle cut (hit t19, raw 2.6). Distinct 4-hit sequence, **not simultaneous front-loaded damage**. Each strike requires target within 2.5 blocks and 100° frontal arc.
- t22 guard. VFX: white tapered arc short <2 blocks, fades <=5 ticks, <=12 particles per slash.

### 4. FEINT / "Ложный замах" — 18 ticks, 8s cooldown
- t0–6 visible high sabre raise/hop; **no hit at feint frame**.
- t7 fake strike stops midair, torso shifts right; if target is in aggressive windup, it can prematurely spend its targeted swing; boss cannot always be tricked.
- t10–12 lateral sidestep; t14 real weak poke hit event; t18 guard. Mark opening `FEINT_OPEN` for 20 ticks when enemy actually whiffs; prevents infinite stagger.

### 5. BOW_3_SHOTS — 35 ticks, 10s cooldown
- t0–5 weapon switch, left arm grips bow.
- t6 draw starts; t11 first arrow server spawn; t17 second, t23 third, each independently ray-aimed at target position with slight natural dispersion. Visual arrow line+bowstring recover between shots, muzzle (bow tip) effects minimal.
- t29 sheath bow, t35 saber ready. Never fire through solid walls. Arrow max range 18, arrow raw 2.0 HP, no explosive/elemental arrows by default.

### 6. COUNTER_AFTER_DODGE — 16 ticks, proc requires boss miss, lockout 5s
- t0 dodge land, t3 torso twist -18°, t5 sprint one small step, t8 contact window, t10 recover, t16 guard. Raw 4 HP, not massive hit; may briefly stagger regular mobs for 6 ticks; boss only contributes 12% to fatigue gauge with no stun spam.

### 7. LAST_STAND / RETREAT / ELIXIR / REJOIN
- t0 at <=15% HP, self-preservation triggers. t0–8 stumble & defensive low stance, dodge away from nearest target; t8–25 sprint exit to navigable recovery node at least 18 blocks from boss and outside attack radii.
- Drink: crouch/sit on one knee for 46 ticks, right hand raises flask from t5 to t16; sips at 18, 24, 30; green particles <=3 per sip. Cycle healing with visible once per 2s progression until HP reaches 65–75% (specific rules AI file).
- t0–6 after recovery stand; t6–20 return toward player at limited speed; do not instantly rejoin while boss active in the *same story encounter* unless existing hero rules explicitly permit; stay in recovery for rest of active boss encounter by default.
- If arena has no safe node, invulnerable retreat state MUST NOT stand in center attracting attacks. Resolve via despawn-as-recovering with saved owner and reappearance post-fight; no loot, no permanent death.

## Generic transitions
- `HURT`: 8 ticks, one 8° recoil, three stagger variants, never layer over INVULN_DODGE; `DEATH` not normally used (retreat at 15%) but handle forced /kill no duplication; `INTERACT`: 14 ticks bow, pointing, name bubble no idle voice spam.
- Every action stop resets animation locks, hit callbacks and mounted item offsets; late server packet should not replay damaging hit.
