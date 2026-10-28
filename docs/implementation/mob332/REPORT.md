# #332–#336 — реализация, 28.10.2026

Статус: код и офлайн-доказательства, НЕ художественная/игровая приёмка.
Исходная база до изменений: 94c9cc3e (boss331 сохранён dcccbde8).
19 вариантов = 16 обычных видов, причём у Шептуна четыре варианта одного EntityType.
Minecraft / PolyMC / сервер / GameTest в этой работе: **0 запусков**.

## Изменения

| Issue | Реализация | Доказательство | Статус |
|---|---|---|---|
| #333 | Ручная встреча Постеня в отдельном per-dimension SavedData; природный журнал Nether сохранён; два этапа, четыре копии, NBT, отказ unique rewards/XP, Peaceful cancellation | `posten-gates.log`, `pass1.log`, ManualPostenOffline.java | PASS offline; summon/egg/relog/MP NOT_RUN |
| #334 | 8 лап костяного ползуна: hip→upper→joint→shin→toe; 4 лапы угольного: hip→upper→knee→shin→foot. IK, опора/подъём/перенос, разные темп и duty cycle, подъём передних лап в атаке | native-validation.json; bone_crawler/coal_crawler renders+motion; MobMotionOffline | PASS offline; ground contact/runtime NOT_RUN; число лап coal ART_BLOCKED |
| #335 | Бордовая чешуйчатая саламандра, зелёный шлаковый краб/медные увеличенные клешни, холодный базальтовый голем/светлое ядро; угольные пластины и кирпичная печная броня Пекельника; золотое ядро Искровика; различные ткань, крылья, кристаллы и грибы | occupancy.json, все 19 PNG+GIF, воспроизводимые генераторы | PASS source/UV; ART_OWNER_PENDING |
| #336 | Разные idle/move/attack/recovery/hurt/death: 16 видов; лаг tail_tip, lateral crab legs, heavy golem/Pekelnik, двухсегментные крылья glide/sit, кристаллический заряд, резонанс, шляпка/реакция на кормление; 4 hood variants | after/*.json.gz, renders/*-motion.gif; HallsClient реальное подключение состояния | PASS source; полный in-game matrix NOT_RUN |
| Защита боссов | 41 boss rig/PNG/model/icon не изменён, старые IDs/HP/natural spawn/AI/loot обычных мобов сохранены; единственный common visual event — кормление Грибошмыга | boss-resource-parity.json; targeted diff | PASS source; old save/MP NOT_RUN |
| Оригинальный арт | Реальных byte-source original PNG нет; текущие игровые PNG доступны и экспортированы | реестр ранее проверенных references | BLOCKED_REFERENCE; не подмена оригиналов |
| Аудио | Новых голосов/синтетики/файлов нет | diff | Существующее; дополнительное качественное audio BLOCKED_NEEDS_AUDIO_SOURCE |

## Как воспроизвести доказательства

Java21: gradlew.bat exportNavyaRigs -I tools/navya-offline.gradle, scoped генераторы
`atlas`, `rig`, `rigs` (boss resources обязательно сравнить с parity ledger).
Затем gradlew.bat exportMob332 verifyMobMotion verifyManualPosten -I tools/mob332-offline.gradle.
`python tools/mob332_render.py` и `python tools/mob332_verify.py`.
Before снят ДО изменения client rigs, poses и PNG. Экспорт — production ModelPart.render
с настоящими координатами и UV, не придуманные фигуры. Одинаковые ортографическая
камера, масштаб пары, фон, nearest texture filtering; эмиссия выключена в обоих этапах.
Четыре вида: front/right/back/left. GIF: walk→stop→attack→idle, шаг 2 ticks (10fps).
Это source render, **не Minecraft screenshot**. 24/30/60 в тесте — дискретизация
математики, НЕ измеренный FPS. Склейка GIF не доказывает сетевое поведение.

## Метрики похожих пар

Пересечение RGB bin32: actual rendered pixels / UV weighted by emitted face area.
Не считать неиспользуемый прозрачный фон атласа. Процент не является art gate.

| Пара | Render до→после | UV до→после |
|---|---|---|
| heat_salamander / slag_crab | 93.77% → 11.86% | 94.37% → 12.90% |
| heat_salamander / vent_golem | 95.72% → 0.00% | 95.96% → 0.00% |
| slag_crab / vent_golem | 98.04% → 4.78% | 98.38% → 6.40% |
| coal_crawler / pekelnik | 93.97% → 6.26% | 93.16% → 6.22% |

## Owner smoke / regression (в этой сессии НЕ ВЫПОЛНЕН)

1. Проверить installation.json, единственный JAR и SHA; запускать копию старого мира
   и отдельный новый мир. Сохранения установщик не трогает.
2. Без OP: найти существующие новые Nether biome привычным survival маршрутом,
   наблюдать естественных мобов, ходьбу/остановку/атаку и возвращение в idle;
   сравнить рядом саламандру/краба/Жерловика и угольного/Пекельника.
3. У костяного ползуна увидеть восемь физических лап, чередование 4+4, колени и
   стопы в трёх циклах. У угольного четыре суставные лапы с более тяжёлым шагом.
   Проверить склон, ступень и бой; недопустимы обратные колени/полная синхронизация.
4. Наблюдать свободный полёт/планирование Гулокрыла. Приручить blaze powder,
   пустой рукой посадить/поднять: крылья складываются. Покормить Грибошмыга грибом
   после cooldown: наклон головы и шляпки; лут и расход остаются прежними.
5. OP-диагностика отдельно: `/execute in minecraft:overworld run summon slavicmyths:posten ~ ~ ~`,
   повторить с `minecraft:the_nether` и `minecraft:the_end`; яйца проверить вручную
   в каждом измерении. Режим Normal: bossbar, четыре маски, переход 650→260,
   четыре копии по65HP; manual победа без уникальных предметов/XP.
6. Негативные сценарии: повторные summons рядом; natural Posten не теряет свой
   журнал/награду; Peaceful удаляет manual тела/копии и bossbar; manual natural
   награды не выдаёт. Природная победа по-прежнему даёт награду один раз.
7. Сохраниться/выйти в обеих фазах manual и natural, перезайти. В MP два игрока:
   одинаковые bossbar/фазы/кормление, ни дюпа наград, ни общей manual арены.
8. Оригинальное художественное совпадение, biome lighting, gameplay, old save,
   звуки, MP, ground collision, FPS/MSPT — НЕ ПРОВЕРЕНЫ.

Риски/откат: геометрия клиентская, registry IDs прежние. Старый JAR сохраняется
установщиком вне mods; откат только при закрытой игре с единственным mod JAR.
Следующая очередь — C–G #337, по исходникам, без переизобретения принятых боссов.
