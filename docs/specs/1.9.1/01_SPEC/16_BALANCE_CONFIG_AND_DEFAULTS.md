# Все числовые defaults в одном месте (не терять при интеграции)

Это **исходная таблица конкретных значений** для первоначальной реализации, после пользовательского тестирования значения можно аккуратно балансировать. Не подменять важные закреплённые значения. Хранить в existing config или константах одной структуры с понятными именами.

| Свойство | Значение |
|---|---|
| Морозко: maximum HP | 1000 HP |
| Финал испытания | 5% = 50 HP при исходных 1000 |
| Морозко: base melee | 10 HP |
| Морозко: ice projectile | 8 HP |
| Морозко: aura radius | 8 блоков |
| Aura accumulation: 0–3/3–6/6–8 blocks | +5 / +3 / +1.5 cold/сек |
| Aura decay outside 8 | -6 cold/сек после 2 с паузы |
| Cold Weakness / Slowness tiers | 25% slow I; 50% slow II + weak I; 75% slow II + weak II; 100% slow III + weak II |
| Cold HP damage | на 75–99: 1 HP/3 с; 100: 2 HP/2 с |
| Aura на союзных богатырей | 0.5× player accumulation |
| Blizzard | 28 с CD, 2.5 с cast, 12 c duration, radius12, +2 cold/sec |
| Boss ice spikes | 13 с CD, 3–5 spike, 0.9 warning, 7 HP, +12 cold |
| Boss frost breath | 18 с CD, 9 blocks, 50°, 2 c channel, 4 HP/0.7s +15 cold |
| Boss frost wave | 22 с CD, 1.2 warning, radius7, 5 HP +8 cold |
| Boss phase III buff | при <30%: 8 c storm, aura ×1.5 |
| Ледяные двойники Морозко | **3 одновременно, 50 HP каждый, 100% base hit damage** |
| Double first summon | не ранее 20 секунд от начала |
| Double summon cooldown | 35 с первая фаза, 28 с поздние |
| Trial owner summons | тотем многоразовый, попытка после 120 c, после завершения 180 c |
| Contribution radius | 40 blocks от якоря |
| «Морозное зерцало» | **15 HP**, **30 c**, **180 c cooldown**, 1 игрок = 1 клон |
| Руна морозостойкости | выключает Slowness+Weakness при активном ношении, морозный DoT остаётся |
| Руна мороза | 25% proc, Weakness I + Slowness I по 4 c |
| Rune drop | **одна, выбирается 50/50**, каждый бой каждому игроку |
| Staff drop | **15%** каждый бой каждому игроку |
| Heart drop | **100%** каждый бой каждому игроку |
| Зерцало drop | **100% только первая победа** каждого игрока |
| Staff projectile LMB | 7 HP, +8 cold, базовый attack CD |
| Staff frost stream RMB | 3 c, 1 HP/0.5 c +6 cold, CD10c |
| Staff icicles G | 5 shots, warning0.8c, stagger0.12–0.16, 5 HP каждый, CD20c |
| Staff wave V | 4 HP +8 cold, radius5, CD16c |

**Важно**: урон исчисляется игровыми `HP` (2 HP = 1 полное сердце), не процентами здоровья. В документах не путать `HP` и `hearts`.
