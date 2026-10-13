# Combat loop choreography and deliberate hero/player roles

## Example session sequence
0) Owner has reusable `finist_call_feather`, summons Finist (170 HP). Falcon perch appears, owner's party now 1/2.
1) Owner uses `kartaus_black_seal` on suitable ground while other heroes absent. Location preflight passes. Boss spawns 40 ticks later. Dedicated boss bar appears. Item remains in inventory. Dialogue in UI/subtitle optional, original fx guaranteed.
2) Phase 1 boss plants banner, summons black guard. Finist's Watch marks high-threat minion/banner. Player destroys banner (45 HP) while Finist intercepts a projectile/marks target (15% bonus) and deals relatively modest sword damage.
3) With banner down, Kartaus staggers 50–80t. If owner attacks boss, mark bonus applies. Boss recovers, uses staff and chains. Player must leave telegraphed shapes; Finist guard intercept happens once/240t max, not automatically every time.
4) At <=65% HP phase 2 Kartaus chains separate falcon via curse. Bounded physical talisman appears within reachable range. Player finds it via purple chain and breaks it (12 HP). Finist falls back on sword+buff while cursed. On release bird scout returns and Kartaus staggers 50t.
5) At <=30% HP stage 3: quicker banner/guards but capped 2 banners / 3 guards, boss ring marker remains visible. Finist's personal bird morph dodges an imminent attack once per phase (not invulnerable all phase).
6) Finist may withdraw at <=15%; the player remains responsible for finishing. Boss defeat immediately clears helpers, sets recorded progress flags with deferred rewards, re-enables normal two-hero summon logic.

## Damage reference
Player default assumed 24 HP = twelve hearts. All numbers referring to 'post-diamond HP' are health units NOT hearts. For the benchmark use unenchanted complete vanilla diamond armor (helmet, chest, leggings, boots) and no accessory buffs: severe Kartaus staff/eye hit final 6 to 7 HP *after armor*. With strong resistance effects or high-end Slavic armor it can be lower; with cursed/sundered defense it can be higher. Implementation must test actual damage type and armor reductions rather than hardcode true-damage for every attack. Finist normal sword ~5 raw HP, wing dive ~7 raw, bird peck 2-3 raw. He supports; cannot be stronger than Ilya or Dobrynya in sustained melee.

## Counterplay: no unavoidable overlapping attacks
Black Eye locks ground at t21 and strikes t42. Kartaus may not concurrently resolve staff wave at t42. Chains can slow then Black Eye only with >=40t reaction time and visible floor target, not an unavoidable checkmate. Guards spawn >=5 blocks from owner with >=14t warnings, max 3. Banner buff visible icon on boss + banners with obvious health response. No sudden boss snap behind camera. Finist mark is conditional on real visible target and does not follow unbounded distance.

## Escape and nonsoftlock
If boss pathfinding fails >100t, reposition via ordinary walkable path to encounter center inside radius 12; do not teleport into player. If companion path fails, reattach to owner (only bird VFX, no attack). If anchor unreachable, relocate within r5 to accessible clear tile or auto-break within 100t; no 12-sec frustration caused by a floating barrier in a tree. Never grant rewards from abandoned/creative fight. Any synchronized state can recover after client tracking (render list rebuild, bossbar on join) without ghost event entities.

## Performance requirements
No scan >32 blocks radius and no poll >1 per 10 ticks for complex proximity; minions have ordinary vanilla pathfinding and event UUID. Total event-added live entities cap 1 Kartaus + 3 guards + 2 banners + 1 curse anchor + 1 lightweight companion (if standalone) = <=8 entities excluding hero. 2 concurrent distant owners should not share banners. If engine already uses status animation packet events use it; else create one compact enum sync rather than 1 packet per feather.
