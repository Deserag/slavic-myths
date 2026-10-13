# Five new runes: one existing rune anvil, full preview, no new UI framework

Register runes in existing rune item registry/category and existing smithing/forging hook. Never create a second rune inventory, workbench, GUI or rune progression. Ensure existing eight runes retain behavior and compatibility. Use established item component/NBT data format and rune socket count rules. Do not use free enchantment duplication.

| Rune | Permitted equipment | Activation | Cooldown |
|---|---|---|---|
| Stomp | Boots only | Fall >=2 blocks then landing wave | 20s |
| Dispelling | Helmet OR chestplate | Negative magic debuff reduction and occasional cleanse | 45s cleanse |
| Pursuit | Melee weapon only | third consecutive same-target melee hit | on 3rd hit, no timed cd |
| Suppression | Melee weapon only | debuff next attack; interrupt permitted spell | 25s/target |
| Battle Line | Any armor piece | mitigate next heavy hit if 2 nearby allies | 30s |

## Install UI
Use existing grid and rune slots, do not reposition unrelated controls. In the existing rune details area show: icon, full name, equipment compatibility, numeric effect, cooldown, number of sockets remaining and conflict if incompatible. Tooltip examples:
- `Руна Топота: сапоги — при приземлении с ≥2 блоков отбрасывает врагов в радиусе 3,5 блока; перезарядка 20 с.`
- `Руна Преследования: оружие — третий удар по одной цели за 4 с получает +15% к урону.`
- `Руна Подавления: оружие — уменьшает следующую атаку цели на 15%, 25 с на цель.`
- `Руна Воинского строя: броня — 2 союзника рядом: уменьшает один мощный удар на 15%, 30 с.`
- `Руна Рассеяния: шлем/нагрудник — вредные магические эффекты короче на 25%; очищение раз в 45 с.`

## Slot stacking
Same rune inserted in more than one armor piece: effects NEVER stack; pick deterministic single active instance, ensure tooltip explains. Distinct rune socket compatibility inherited from existing system; if incompatible with previous runes, obey existing exclusivity rules. Extraction/rewiring must use current anvil's cost and non-consumption semantics. Server decides active rune list after item equip change; avoid scanning inventory every tick if already has equip event.

## Edge cases
Sprinting and regular jumping do NOT proc stomp. Elytra movement and forced teleport no landing wave; safe landing required. Dispelling cannot remove hunger, slow fall, beneficial buffs or cinematic curse progression flags. Pursuit counts only player-driven direct melee hits, no projectile/DoT/sweep. Suppression's 25-second per-target cooldown is server-side target UUID-bound but bounded/expired to prevent leaks. Battle Line counts allies at exact moment of attack, not friendly hostile summons nor spectators. Always show numerical effect in rune anvil so old UX issue (unexplained runes) is fixed for these five.
