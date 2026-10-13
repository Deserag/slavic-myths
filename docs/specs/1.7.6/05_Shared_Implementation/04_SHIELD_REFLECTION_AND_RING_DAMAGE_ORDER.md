# CRITICAL: shield reflection 1.5x / Dobrynya counter 1.75x

## Ilya shield exact sequence
Attack has a unique server hit-event ID. Let `incomingHP` = HP damage that this direct incoming attack WOULD have inflicted after armor, enchantments, Resistance and all other normal victim-side mitigation, but before active shield block cancellation. Clamp only negative/nonsensical values to zero. Successful active block requires valid facing angle, actual raised shield and a blockable direct damaging source. If a vanilla projectile is blockable, reflect to the actual originating living hostile attacker only; otherwise play defense FX without phantom victim.

At the time a VALID successful block happens and cooldown is ready:
1. Normal shield blocking happens as usual (no double attack on player).
2. Apply short 3-block radius defensive outward knockback impulse ONLY to hostile ordinary targets; resistant bosses may resist per existing knockback rules.
3. Deal `reflectedHPDamage = incomingHP * 1.5` to exactly the original attacker with server-authoritative custom reflect damage type bypassing recursive shield-trigger hooks; do NOT count it as player weapon direct melee, do NOT also buff by Dobrynya ring or offensive runes.
4. Spawn golden ring pulse facing shield at block location; 10 ticks bright rim then 8 tick fade; play one supplied reflect SFX. Start 35s cooldown after successful proc.

If blocked attack is not eligible, still block normally; do not proc. If attacker is absent or environmental, no reflect; keep normal shield. A single sword whirlwind with several sub-hit events must not reflect more than once for same attack instance. Knockback and reflect same cooldown. Tooltip explicitly says **×1.5** of blocked would-be health damage, not 1.5 flat HP. Example: unblocked would deal 6 HP after armor, successful block returns 9 HP; unblocked would deal 10 HP, returns 15 HP. No secret boss-only coefficient cap; damage engine's existing immunity/invulnerability windows still apply.

## Dobrynya ring exact sequence
After player's successful active shield block, when ring equipped, arm a 4.0s state. During state, first REAL direct melee hit against hostile target is multiplied by **1.75** final damage AFTER weapon enchantments/critical damage but BEFORE target health subtract, once. Trigger only once on that hit, then 12s cooldown. If no hit during 4s, state expires without buff and without entering cooldown (re-arm on next successful block). If ring unequipped, window clears immediately. Same event cannot reflect shield and take the ring bonus. Do not multiply DOT, ranged bow, reflected attacks, rune procs, splash, pet hits, friendly PvP hits not allowed.

## Concurrency
Apply normal damage via a single server damage pipeline, never by client packet; one procced ring window per player. Persist cooldown timestamp in a durable player-specific component or adapt existing combat timer store; cannot bypass by item swap/death/logout. All damage scalars source-of-truth constants.

## Display behavior
Use short animation: Ilya shield flashes red/gold for 6 ticks then golden shockwave to 3 blocks. Dobrynya ring ruby flashes for 8 ticks; its icon displays a 4-second ready ring arc then fades; after empowered hit play one metallic chime. Low-particle mode must still indicate activation in tooltip/buff UI if supported.
