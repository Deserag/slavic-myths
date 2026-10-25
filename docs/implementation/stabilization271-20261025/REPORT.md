# #271 — подпакет 1: объём появления Сивки и исходная карта (IN_PROGRESS)

Источник: hell 2f82f20e + текущий patch. Путь игрока: дневное исследование plains/sunflower_plains/meadow → естественное появление slavicmyths:sivka_burka → обычное знакомство/приручение. OP и новая структура не нужны. Старые чанки могут участвовать в natural spawning; уже существующих существ и NBT правка не меняет.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Не требовать площадку больше тела | SivkaContent.placements, SivkaRules.WIDTH/HEIGHT | Обычные луговые биомы, свет >7, небо, dirt под ногами | API compilation + source gates; actual collision NOT_RUN | Проверяется AABB 1.65×2.85, не старый пустой параллелепипед3×4×3 |
| Сохранить безопасность/нагрузку | noCollision, containsAnyLiquid, hasChunkAt | Естественная попытка | Source review; native spawning NOT_RUN | Коллизия/жидкость запрещены, пограничные незагруженные чанки не запрашиваются |
| Не менять спорные вероятности | biome modifier и прежние счётчики | Исследование | Resource preservation | Вес1, группа1; гарантии первой встречи ещё нет |

## Фактические исходные пути

Полная выгрузка **записей biome spawn**, включая vanilla и Навью: [biome-spawns.tsv](biome-spawns.tsv),37строк. Это НЕ полный аудит всех registry entities: event/structure-only существа перечислены ниже; прочие ещё требуют трассировки.

| ID | Биом/канал | Условия/текущий шанс | Мешающие ограничения | Изменения | Ожидаемая частота | Офлайн-результат | Необходимый игровой тест |
|---|---|---|---|---|---|---|---|
| sivka_burka | plains/sunflower_plains/meadow, natural CREATURE | вес1, группа1; overworld, небо, свет>7, dirt | пассивный cap; прежняя пустая площадка3×4×3 | collision/fluid только по фактическому AABB, loaded guard | Не измерена; подходящих мест больше, гарантия к дню30–40 НЕ реализована | Source/build gates, без мира | Новые/старые чанки, склон/трава/цветы, вода,5seed, дни1/10/30/40,MP |
| fire_serpent | #spawn_serpent, natural | вес1; ночь, overworld, не peaceful, #spawn_open ИЛИ forest | небо; airVolume5×5×5; ground; локальный лимит96 | Не менялся; старый RNG уже снят | Не измерена | Trace HuntSpawns.elemental/safe | Ночные plains и forest без структуры |
| podvey | #spawn_open, natural | вес1; ночь ИЛИ дождь, overworld, не peaceful | небо;3×3×4; локальный лимит96 | Не менялся; старый RNG уже снят | Не измерена | Trace HuntSpawns | Ночь и дождливый день, не peaceful |
| ovinnik | #spawn_ovinnik, natural | вес1; ночь, overworld, не peaceful | openBiome ИЛИ hay/campfire/smoker в8; clearSpace; локальный лимит64 | Не менялся; открытая местность допускается без декоративных блоков | Не измерена | Trace HuntSpawns.natural | Plains без строений/костров; отдельно лесной лагерь |
| volkolak | #spawn_forest_taiga, natural | вес1; ночь, overworld, не peaceful | forest/taiga; clearSpace; локальный лимит72 | Не менялся; старый RNG уже снят | Не измерена | Trace HuntSpawns.natural | Лес/тайга ночью без задания |
| grey_wolf | #grey_wolf_forests, event PlayerTick/40 | детерминированные35% регионов512; НЕ biome weight | центр чанка один раз/сессию; лесная арена35% безопасных колонн радиуса14; расстояние768; квота3–12; исключения96 | Не менялся | Не измерена;35% регионов НЕ шанс встречи | Trace WolfEncounter | Несколько лесных маршрутов/seed, дом/сохранение/MP |
| firebird | overworld, event LevelTick/100 | первая подходящая ночь; окно13000–23000; после события2–4дня;16кандидатов/попытку32–64блока | выживший не creative/spectator; площадка4×4×4, небо, запрет части блоков; одна ledger-встреча | Не менялся | Нет процентного броска начала; безопасная площадка может не находиться | Trace FirebirdEncounter + FirebirdRules | Несколько ночей, спавн в воздухе10–24 выше anchor, unload/relog |
| kot_bayun | feature bayun_nook + отдельное on-demand создание | rarity_filter1/128 чанков до biome/safety; НЕ natural mob weight |13×13площадка, перепад<=4, грунт, воздух/листва/брёвна; explicit book conditions отдельно | Не менялся | Не измерена | Trace BayunFeature/placed_feature | Новые лесные чанки; отдельно создание книгой; не смешивать каналы |
| likho_one_eyed, tugarin_zmey | BossAnchors/HuntRecords | существующие natural anchors; дневные/ночные gates различны | anchor+loaded+clear+cooldown; смерти ledger-owned | Не менялись | Не измерена | Source preservation | Сохранить хорошие естественные встречи; настоящее убийство |
| моровые формы | MorEvents.FinalizeSpawnEvent + natural Лихо в BossAnchors.spawn | overworld, день>=2, >160 от worldspawn,12/100 подходящих NATURAL/SPAWNER | protectedMob; один pending/заражённый в48; eligible kinds1–6 | Не менялись; Яга/Волк НЕ обязательны |12% только подходящих попыток, НЕ всех существ | Trace MorRules/MorEvents/BossAnchors | До Яги на днях2+, именованные/сюжетные NPC защищены |
| navka/rootling/bone_crawler/wisp | navya_grove biome, native biome entries | веса в TSV; Y33–145, air; не peaceful кроме wisp | профиль lower/middle/upper; wisp дополнительно1/8; vanilla Mob rules у ground видов | Не менялись | Не измерена | Trace NavyaSpawns | Новый Nether/Навья, все ярусы,light/caps/unload |

## Спорные параметры — без реализации

Сивка: согласовать per-player/per-world счётчик, что считать активным днём/исследованием, миграцию старых миров, deadline30–40, pending при отсутствии безопасного места и MP-лимит. Жар-птица: отдельно согласовать бюджет подходящих ночей (обсуждались10–15), учёт неудачных площадок и сохранение первой встречи. Эти числа не приняты и в source не внесены. Требуется решение владельца до соответствующего кода.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Удалён завышенный clearing Сивки | SivkaContent: actual WIDTH/HEIGHT AABB | PASS (source only) |
| Вес/биомы/registry/ресурсы не менялись | verify_stabilization_access_spawn.py, git diff | PASS |
| Полный аудит всех сущностей и достижимый gate-path | Здесь только37 biome entries и ключевые paths; native ALLOW всех классов не проверялся | NOT_RUN |
| Гарантия Сивки/Жар-птицы | Требуется решение по параметрам выше | BLOCKED |
| Настоящий natural spawn, частота,MP,5seed | Игровых запусков0 | NOT_RUN |

## Что тебе проверить в Minecraft (позже, после разрешённой установки)

1. Копия старого мира и отдельный новый survival, без OP. Днём исследовать plains/sunflower_plains/meadow; сравнить доступность Сивки с прежним JAR на одинаковых маршрутах. Новые чанки не обязательны для самой правки natural placement. Отсутствие встречи за короткое время не опровергает/не доказывает collision-фикс: вес1 и cap сохранены.
2. Плоская земля, края склонов, трава/цветы: появившаяся Сивка не застревает и не задыхается. Вода/лава/тесные места не становятся допустимыми. Отдельно дождаться естественной попытки у границы загруженных чанков.
3. Ночные plains/forest/taiga: фиксировать день, биом,seed,координаты каждой встречи с четырьмя мини-боссами. Жар-птицу искать ночью над поверхностью, а не только на земле. Не использовать summon как доказательство natural spawn.
4. До Яги, день2+, дальше160блоков от точки мирового спавна: фиксировать подходящих заражённых и естественного морового Лиха; именованные NPC не заражаются.
5. Перезаход и два игрока: нет дублирования уникальных встреч. Повторить на5seed и днях1/10/30/40. Это отложенный полноценный QA, сейчас он НЕ выполнен.

Следующий scope: полный gate-аудит #271, согласование first-encounter policy, затем #275. Закрытий Issues/переходов Project нет. Сборка общая с #272, точный receipt указан в соседнем отчёте.
