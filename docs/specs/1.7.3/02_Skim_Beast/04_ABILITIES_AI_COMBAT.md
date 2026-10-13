# SKIM BEAST — finite state AI, attacks, fatigue & intended counterplay

## Stats and difficulty
Max HP **750**; armor=8, toughness=2, boss knockback resistance=0.85; base movement speed walk .28, pursuit .33, brief charge .45; follow range=36, frontal hit reach 3.8. These are tunable initial values, not a license to nerf the player-damage contract.

**Health-unit convention:** player's commonly 12 HEARTS = **24 HP** by this stage, wearing full diamond. High-commitment direct landed attacks must deal **at least 6 HP (three hearts) AFTER diamond armor (no protective baubles, no Resistance)**. Damage before armor needs empirical tuning, respecting difficulty. For protected/enchanted players, defenses should help, but no attack should be laughably negligible; design curve testing in `05_Shared_Systems/01_DAMAGE_BALANCE_AND_TELEGRAPHS.md`.

## Combat phases
1. Phase P1 **100–70% (750→525)**: claws, leaps, stalking, no >1 dramatic move per 7 seconds. Teaches windows.
2. Phase P2 **70–30% (525→225)**: introduces whistle, grip, rear roar, strategic flanking. One major attack at a time; 1.0 sec universal post-major lockout.
3. Phase P3 **<30% (<225)**: RAGE phase once, movement +12%, major ability cooldowns ×0.87, but heavy attacks fill fatigue gauge +25% faster and expose longer exhaustion window (70 ticks). No hard immunity or additional untelegraphed damage.

## Action decision (server tick optimized)
- Every 5 ticks update short movement/collision; every 10 ticks choose action only if state READY and target valid, 20 ticks threat recalculation, 40 ticks arena retention, 60 ticks boss integrity/duplication; no scanning whole chunk every tick.
- State machine: `DORMANT > AWAKEN > READY > WINDUP > EXECUTE > RECOVER > READY`, with `GRAB_TARGET`, `EXHAUSTED`, `RAGE_TRANSITION`, `DYING` high priority. Each transition carries monotonic encounter tick (server epoch); no repeated action event if chunk reloads.
- Target chosen from owner/player and allowed summoned Alyosha using threat table; never targets creative/spectator/quest neutral by accident; boss switches after ~30 tick target dwell to reduce flicker. If Alyosha taunts, he becomes high priority, but boss periodically adapts and attacks player to avoid free damage.
- No more than one overlapping AOE/area effect active at once. Stun/interrupt cannot chain permanent locks; resistance/immunity for 80 ticks after confirmed stagger (not exhaustion).

## Skills table
| Ability | Cooldown | Damage **post diamond armor target** | Range/window | Counterplay |
|---|---:|---:|---|---|
| Rending claws | 84t | 2.5+2.5+6 HP if all hit; last strong hit >=6 when landed alone | 3.3m front 125°, hit at t11/19/30 | roll sideways/outside arc; don't face-tank full string |
| Hunter leap | 150t | 7 HP center landing, falloff to 3 HP radius 2.2 | landing t27–32, jump aim locks at t12 | move after red hoof/paw sigil/t12; ally baits leap |
| Snake whistle | 210t | 1 HP magical pulse then Slowness I 3s + Nausea 1.5s | cone 75°, range 18, pulse t22 | break LoS behind large rocks; no recurring DOT |
| Lion roar | 190t | 6 HP within cone <=4m, 3 HP out to radius 7m, push moderate | cone 115°, t28 | back away/take flank; clearly rears before effect |
| Flank pounce | 165t | 6 HP on direct connecting slash | flank angle >=40°, range <=9 | track boss, dodge last-second after pivot |
| Deadly grip | 260t | 3 HP on grip and 2×2 HP subsequent internal hits; throw 4 HP + safe knockback | bite contact <=2.5, target restrained <=2.0s | Alyosha assists or player hits jaw >=16 raw damage in window; no inescapable kill-combo |
| Rage | once at <30% | no flat damage from transition | 64t | visual tell; stamina becomes exploitable |

**IMPORTANT:** The above post-armor values are *playtesting targets*, not to be passed directly as raw Java/NeoForge `hurt()` values. Tune base damage with test runner to hit numbers after armor for full diamond. Only strong hits have strict >=6 HP minimum; whistle/quick strikes lighter. Difficulty scaling must not make `Easy` identical to `Hard`.

## The crucial mechanic: STAMINA / EXHAUSTION
- Hidden internal stamina integer 0–100. Start 100. Claw combo -14, leap -25, roar -20, flank -18, grip -25, whistle -10; in READY with no attack and not running restore +2 per 20 ticks, while pursuing +1 per 20 ticks. In rage heavy costs ×1.25, round up.
- If stamina <=15 after attack **and recover** finishes, enter EXHAUSTED for 70 ticks: body crouches, audible panting, no mobility/attacks, incoming damage multiplier ×1.25 applied to actual damage *after target armor etc*; short eye dim indicator and slower mane swing. At end regain stamina to 56. Avoid alternating near-free exhaustion: exhaustion lockout 140 ticks.
- Additional skill of Alyosha: `FEINT_OPEN` and dodge counter can contribute boss fatigue: +12 fatigue equivalent at most 1 per 5s, but never directly stun outside fixed windows. Use stagger only after a failed leap head-first into eligible worldgen rock or stone cliff: collision recovery 40t; prevent pulling arbitrary village buildings into destructive grief.
- Prevent cheese: after 2 misses on taunting Alyosha in <=10s, boss targets player for a window if reachable; charge trajectory locked at visible t12 and cannot follow target midair. Player has counterplay but boss retains threat.

## Hit detection / mechanics
- For each melee damage window, hit *at most once per victim* in that window; target candidates bounded by local hit capsule/cone, no 360° invisible strikes. Guard against giant AABB giving unbounded reach through stone. Boss tracks owner only in encounter; if no valid player for 60s, reset encounter gracefully, heal/cancel active skills, do not leave chunkloaded permanent foe.
- Grip tether stops on disconnect, death, flight mode, dimension transfer, rescue, invalid arena, boss stun/death; send velocity/position server side and smooth client. No fall-damage stacking beyond configured throw.
- Block break only special `weak prop` arena blocks and breakable wood/fence pieces deliberately tagged. Do not destroy random base, chests, protected blocks, terrain outside arena. Player-built blocks are not considered boss smash props.
- When killed, set unique victory once for contributing players, preserve data for 1.7.6 future rewards, unlock den access if gate exists. No special items dropped.
