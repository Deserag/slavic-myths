# Kalin-tsar — abilities / AI / damage

## Stats
- HP: 1800
- Armor/resistance: high
- Knockback resistance: high, higher after 40% HP
- Attack style: melee commander

## Abilities
1. Royal Command
   - Summon 2–3 elite soldiers.
   - Cooldown approx 45s early, shorter later.
2. Raise All Troops!
   - Summon 6–10 common soldiers.
   - Used more often as HP decreases.
3. Crushing Overhead Slash
   - Heavy single-target melee hit.
4. Steel Whirlwind
   - Rotating AoE around Kalin.
5. Breakthrough Charge
   - Dash that scatters player / heroes.
6. King's Parry
   - One weapon-based parry followed by a counter.
7. Iron Will
   - Below 40% HP: increased resistance to stun/knockback.
8. Last Order
   - Below 20% HP: may summon mixed elites + common soldiers.

## Summon frequency by HP
- 100–65%: mostly elite summons, less frequent
- 65–40%: common wave becomes active
- 40–20%: alternates common and elite waves faster
- <20%: mixed wave allowed; highest summon frequency

## Limits
- Maximum active Kalin soldiers at once: 14 recommended
- New wave should not spawn directly on top of the player
- Use several edge spawn points around the camp

## Damage balance anchor
- Assume player commonly has 12 hearts and full diamond-tier armor by this stage.
- Major direct boss hits should feel heavy.
- Common soldiers deal pressure damage, not boss-tier burst.

## AI priorities
- Prefer player as the main target
- Shift pressure to heroes if they lock him down too long
- Use command abilities when distance is created or after parry windows
