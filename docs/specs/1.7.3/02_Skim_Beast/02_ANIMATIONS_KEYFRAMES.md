# SKIM BEAST — boss motion and attack animation timeline

20 game ticks/sec. Animations drive hit event timestamps SERVER SIDE, with client rendering smoothing. Visible anticipation >=10 ticks for high damage attacks. Ensure consistent sync for multiplayer and replay of animation on nearby clients.

| Name | Duration | Frame timeline/poses and effect |
|---|---|---|
| IDLE | 80t looping | 0–20 breath chest expands 0.045; t30 mane shifts; t50 jaw slight open 7°; t70 slow tail +/-12°; t80 reset |
| WALK | 24t loop | opposite diagonal paw cadence 0–6–12–18–24, torso bob ±0.04, neck counter-rotates |
| STALK | 32t loop | shoulders lowered 0.35, tail tucked, paws deliberate; gaze locks only in reach |
| RUN | 14t loop | larger paw stride, torso pitch 8°, occasional dust <=6/tick |
| CLAW_COMBO | 39t | t0–8 wind right foreleg back 35°; t11 first swipe right→left hit; t16 coil left; t19 left swipe hit; t24 pause; t30 heavy cross swipe hit; t39 return. Target arc each strike front 125°, range 3.5/4.0, NO invisible backwards damage |
| LEAP | 44t + physics | t0–12 hips compress, front paws bend, head tracks target marker, t12 dust; t13–27 root ballistic jump server velocity, peak <=3.5 blocks; t27–32 land contact area radius2.2, t33–44 vulnerable landing recovery; cancel/abort on obstruction |
| SNAKE_WHISTLE | 48t | t0–14 head lifts, mouth opens 15°, eyes flash, t16–31 concentric red-black semi-transparent sound rings expanding, single debuff check t22; t31–48 jaw shuts, cooldown; NEVER permanent confusion |
| LION_ROAR_REAR | 58t | t0–18 hind legs brace, front legs lift +0.7–1.0, spine rotates +24°, head pitches up 18°; t19–30 jaw opens 43°, sound at t26, **server shockwave at t28**; t31–45 front legs return; t46–58 recover. Total visual height <=4.5 blocks. In narrow caves avoid rearing if ceiling <4.7 blocks, substitute ground roar |
| FLANK_POUNCE | 38t | t0–10 track player, sideways pivot ~35°, t11–22 flank run/hop, t23–26 bite/claw contact, t27–38 tail balances and regroup; counterable by dodge |
| DEADLY_GRIP | 65t | t0–14 crouch+open jaw with clear red mouth warning; t15–17 jaw collider checks target within2.5/ frontal 70°; t18 lift target 0.55 blocks and clamp in mouth (server tether, collision safe), t21,31,41 3 damage ticks if unrescued; t45 toss target forward 3 blocks (safe, no velocity exploit), t46–65 recovery. Rescuable via ally/player hits to jaw >=16 raw-equivalent during t18–44; auto release on target disconnect/death/large distance |
| RAGE | 64t once at <=30% HP | t0–12 shake head, t13–35 roar+eyes glow, t36–50 floor dust burst, t50–64 normal angry loop; no fire damage because reference sparks are atmospheric only |
| EXHAUSTION | 70t | t0–10 legs bend/knees shake, t11–20 breathing heavy, head down 0.4, t20–55 fully vulnerable, t55–70 recovery and rise; no attacks; wake if invalid/removed |
| HIT_REACT | 12t | minor head/shoulder recoil 4°-8° only, NO full knockback/stagger on every hit |
| STAGGER_ROCK | 40t | on failed charge versus valid large solid rock: t0 collision, t1–10 head shake, t11–30 vulnerable, t31–40 shake off |
| DEATH | 75t | t0–25 backward stumble, t26–52 controlled kneel, t53–75 collapse sideways, no falling through terrain; kill state/loot handled server only once |

## Safety for engine entities
- Animation offsets must not change authoritative AABB without collision validation. Render/physics orientation must be aligned across 4-legged and reared modes. When grabbing a player, provide non-jitter position/sync and ability to exit on death/disconnect or rescue.
- Keyframe particle envelope: e.g. roar <60 particles total; whistle <80 total; leap landing <42 total; red boss rage <110 total once. Provide reduced-particles mode by normal engine settings. Flash effects must be translucent; no full-screen forced overlay.
- Interpolation supports 20 TPS and partial ticks; no jerky jumps between action phases; don't bind attack hit to animation loop event repeat.
