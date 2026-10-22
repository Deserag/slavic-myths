# #140 · FX-2: четыре оставшиеся головы (1.9.7.56)

Дата: 22.10.2026. Владелец поручил завершить оставшиеся 4 из 6 трофейных голов после предварительно реализованных обычного и Морового Лиха.

## Реальные native-части и эффекты

| Голова | Части из production source | Анимация | World cosmetics |
|---|---|---|---|
| Тугарин-змей | `WorldBossModel.trophyHeadPart("jaw")`, `moustacheL` | Ритмическое шевеление нижней челюсти, дрожание левого уса | Очень редкое `DRAGON_BREATH` из морды |
| Ским-зверь | `SkimMesh.neck.head.jaw`, `mane_0` | Медленная дыхательная челюсть, колыхание пучка гривы | Очень редкий `CLOUD` |
| Проклятый волколак | `CursedVolkolakModel.trophyHeadPart("jaw")`, `earL` | Короткий оскал, вздрагивание уха | Очень редкий `SMOKE` |
| Крупный Подземник | `UndergroundClient.Model.mesh(3).body.head.jaw`, `brows` | Медленное движение каменной челюсти, дрожание надбровий | Очень редкий `ASH` |

FX исполняются только клиентом и **только для установленных блоков**, не для предметов в инвентаре/руке. Частицы не чаще 1 штуки за 72/86/78/92 игровых тика на видимую голову (для Лиха уже существующие 52/32), только в пределах 24 блоков от клиента; никакого server tick, взаимодействия или дебаффов. Используется существующий `TrophyHeadsClient.Renderer`; все временно изменённые native углы части восстановлены после render в finally, cache не протекает между разными установленными головами. Сохраняются все `ModelPart`, UV, PNG, 1-2 block footprint и registry ID; существующие головы Полоза и механика взгляда Лиха #193 не меняются.

## Test/gates

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Настоящие native детали и различающиеся периоды | Runtime source and `TrophyHeadFxRules.java` | PASS SOURCE |
| Сохранение UV, vertex count, bounding cell и neutral pose | `Heads140Offline`, 100 временных кадров × 6 голов | PASS CI #39754403311 (Java21) |
| Java21 clean build NeoForge 1.21.1, JAR 1.9.7.56 | `trophy140-fx-java21.yml` | PASS CI #39754403311 (Java21) |
| Server data / registry / original loot unchanged | Scoped changes and existing offline drop matrix | PASS CI #39754403311 (Java21) |
| Игровые визуальные эффекты, ориентации, ночь/день, multi-head FPS, shaders | Владелец в PolyMC | NOT_RUN |

Задача #140 остаётся В работе, не считается owner-accepted до ручной проверки всех 6 голов. Не запускать и не устанавливать PolyMC автоматически.
