# #193 / P1-4 — аудит боевого DPS и инструкция измерения

**Status: CODE/ANALYSIS ONLY — runtime BLOCKED/NOT_RUN.** Baseline: `8ee3ac3b909ed2915199d4bea6a87960f7d39feb` в `feat/likho-193-forest-spawn`. Проверены `WorldBoss`, `BossRules`, `BossKind`, `LikhoCombatRules`, `HigherMor`, `MorZone`, `MorPresence` и отчёт #93.

## Факты source
- Обычное Лихо: 350 HP, 10 armor, 2 toughness, отдельные числа навыков; фазы 65% и 30% HP.
- Моровое: до 700 HP при единственном модификаторе mor_health ×2; sweep 14 HP (release 22 ticks), beam 12 HP (release 30 ticks), ground/cloud и пассивная аура.
- Нормальное SWEEP 10 HP (windup 14 ticks, cooldown 50); CHASE 10 HP/pulse каждые 25 ticks при активности 80 ticks (cooldown 220); LEAP 8, GROUND 6, GRAB 4+6, GAZE 2. Навыки требуют фактических дистанции, конуса, LOS и/или взгляда. Исходный урон не равен фактическому.
- Ванильная броня full iron = 15 armor/0 toughness, full diamond = 20/8. Следующая таблица — иллюстративная формула урона по броне, БЕЗ зачарований, щита, Resistance и i-frames. **Не результаты боя и не measured DPS.**

| Приём | Raw HP | Iron HP | Diamond HP |
|---|---:|---:|---:|
| SWEEP / CHASE | 10 | 6.00 | 3.00 |
| LEAP | 8 | 4.48 | 2.24 |
| GROUND | 6 | 3.12 | 1.56 |
| GRAB initial / throw | 4 / 6 | 1.92 / 3.12 | 0.96 / 1.56 |
| GAZE | 2 | 0.88 | 0.44 |
| Mor SWEEP | 14 | 9.52 | 4.76 |
| Mor BEAM | 12 | 7.68 | 3.84 |

Моровое присутствие P0-2 использует `DamageTypes.MAGIC`: эти числа к нему НЕ применять. Мор-зоны используют отдельный `mobAttack` и общий антистак. Выполненные #93 source-исправления применимости приёмов не доказывают реальную частоту попаданий.

## Добавленная диагностика без изменения боя
Новое независимое от ограниченного 160 строками лога #93 средство: JVM-флаг `-Dslavicmyths.likho.dps=true`, выключен по умолчанию. `LikhoDpsProbe` слушает существующее `LivingDamageEvent.Post` и раз в 200 ticks выводит строки `LIKHO_DPS` через **локальный тик WorldBoss**, без глобальных scans / chunk tickets. На `WorldBoss.begin` и `HigherMor` skill begin учитываются начатые приёмы. Учитывается реально прошедший через armour/post-mitigation урон по игрокам/мобам, урон по боссу и отдельно от игроков, фактические damage events, последняя экипировка и приближённое время наличия target. При превращении normal→mor окна разделяются. Карта боссов weak-key, число уникальных жертв и уникальных названий приёмов ограничено (8 и 16).
**Ограничения:** `playerHitEvents` не тождественно hit rate (возможны multi-hit, предотвращённые и промахи); `starts` лишь запуски, не все успешные hit opportunities; длительное окно смешивает разных игроков — проводить 1-на-1. `lastVictimArmor`, `lastVictimToughness`, `lastPlayerWeapon` — последнее наблюдение, не доказательство сохранения комплекта на протяжении прогона. При отсутствии контакта выводить нулевые окна, не подменять ими победу.

## Обязательный реальный тест — NOT_RUN
На КОПИИ мира, Survival, фиксированная сложность/20 TPS, без чар/зелий/дополнительного сопротивления:
1. **Матрица 2×2, минимум 3 повтора до 5 минут:** normal iron armor + iron sword; normal diamond armor + netherite sword; mor iron/iron; mor diamond/netherite. Сбрасывать битву между повторами, оружие/броню фиксировать в журнале.
2. Отдельно щит, обычное активное уклонение, стояние/выход из 5-блочной ауры, земля и облако по отдельности и совместно, GAZE с/без взгляда, движение и прерывание release, границы фаз.
3. Собрать `latest.log` со строками `LIKHO_DPS` и при необходимости `LIKHO93`, видео телеграфов, время боя/HP игрока и босса, hit/cast, очаги урона, Minecraft build/JAR SHA, итог игры. Проверить обе формы против zombie/skeleton/spider (#93). MP, relog и frame/tick stability отдельно.
4. Сопоставить измеренный DPS и наблюдавшиеся hit rates, и только ТОГДА предложить владельцу отдельную таблицу `actual→proposal`, без автоматического +10/20%. Без измерения решения об изменении цифр НЕТ.

## Матрица приёмки
| Требование | Evidence | Status |
|---|---|---|
| Код и арифметика урона / armour | Source и таблица | PASS SOURCE / THEORY |
| Инструмент для измерения post-mitigation DPS, кастов | Opt-in код; source/diff проверки | CODE ONLY / QA PENDING |
| Iron/iron vs diamond/netherite, normal и mor | Нет игрового запуска | NOT_RUN |
| Shield/LOS/hit/cast и #93 regression | Нет игрового запуска | NOT_RUN |
| Java21 clean build/production JAR | Полный репозиторий недоступен для локальной сборки | NOT_RUN |
| Owner acceptance и повышение урона | Нет доказанных игровых результатов/согласования | BLOCKED / NOT_CHANGED |

**Issue #193 остаётся OPEN, P1-4 остаётся [ ] (runtime unresolved).** Это измерительный, а не балансный коммит. Другие стадии начинать только по новой команде.
