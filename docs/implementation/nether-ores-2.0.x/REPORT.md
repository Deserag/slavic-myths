# #324 — ресурсный блок, IN_PROGRESS

Ветка hell. Номер будущего рудного патча не присвоен. Minecraft/сервер/GameTest:0.
Техническое исполнение разрешено поздним master #337, старый ANALYSIS не заменяет
последнее прямое поручение. Оригинальный постер SHA22bbf103… физически отсутствует:
новые source-authored 16px ресурсы НЕ являются оригиналом и не имеют ART_PASS.

| REQUIREMENT | REAL_SOURCE / PLAYER_PATH | EVIDENCE | RESULT |
|---|---|---|---|
| Четыре руды, raw/roasted, sulfur не пятая руда | NetherOres.java, 5 block +10 standalone items; прежние IDs не заменены | регистрация+resources | PASS source; runtime NOT_RUN |
| Добыча без OP | новые Nether чанки → поверхность/шахта → кирка → raw → печь → roasted → верстак → слиток | NetherOreFeature; recipes | PASS wiring; реальная встречаемость NOT_RUN |
| Только Nether / не старые чанки | WorldGenRegion+dimension guard, один biome modifier, 9 биомов | feature/top_layer_modification | PASS source; 5seed×4×256chunks NOT_RUN |
| Surface+underground | отдельные попытки, exposed rock vs enclosed shell, обе разновидности | bounded chunk-clipped pass | PASS source; частоты/доступность NOT_RUN |
| Высоты | clamp minY+5 / maxY-6 | native-loot.json | PASS mathematical; real 224-profile NOT_RUN |
| Защита | все referenced structures пропускаются; host whitelist исключает air/lava/BE/portals/bedrock/ancient_debris и Halls masonry | feature guard | PASS source; claim integration/old-save NOT_RUN |
| Плотности | attempts12/1/1/5/2; separate surface2/.25/.25/.8/3; biome preference1.25; число попыток НЕ число блоков | исходник | PASS prototype; sulfur heat multiplier1.5 и полный диапазонsurface2–4 НЕ РЕАЛИЗОВАНЫ |
| Fortune0/I/II/III | actual vanilla OreDrops,10k/material/level | native-loot.json | PASS formula simulation; actual LootContext/tool/MP NOT_RUN |
| Silk | первый единственный alternatives branch, ровно один block; сырьё во второй ветке, no dual event drops | loot JSON | PASS structural; actual Silk/Fortune mining NOT_RUN |
| Оба типа печей | 4×smelting200t / blasting100t,1:1,XP0.1 engineer choice, никаких ore-block smelts | рецепты+advancements | PASS source; furnace/hopper/relog NOT_RUN |
| Алатырский слиток | QAQ/MGB/SAI = exact9 inputs из#324, output1 | recipe/alatyr_ingot.json | PASS source; crafting/negative20/shiftclick NOT_RUN |
| Реальные модели/переводы/Creative/JEI | blockstates/models/PNG/ru_ru/en_us/catalog, стандартные рецепты | nether324_assets.py | PASS resources; JEI UI NOT_RUN |
| Consumers | raw4→roasted4; alatyrite/infernal/sulfur→ingot; livingstone/runnik future consumers честно указаны | tooltips/таблицаниже | PARTIAL; gear independent stage G |
| Оригинальный арт | posterbytes нет | контракт#324 | BLOCKED_REFERENCE |
| Build/JAR | Java21 build и actual vanilla formula test | build-checkpoint.txt, native-offline.txt | PASS technical; не gameplay |

## Материалы

| material | producer | consumer |
|---|---|---|
| raw_livingstone/raw_alatyrite/raw_runnik/raw_infernal_shard | соответствующая ore+Fortune | furnace/blast_furnace |
| roasted_livingstone | обжарка raw | будущие gear-катализаторы; лечебная линейка не добавлена |
| roasted_runnik | обжарка raw | будущие gear-катализаторы; новые амулеты не добавлены |
| roasted_alatyrite/roasted_infernal_shard | обжарка raw | Алатырский слиток |
| sulfur | deposit | Алатырский слиток |
| alatyr_ingot | 9-ingredient craft | будущая кузница#325/#326 |

## Как проверить в Minecraft — сейчас NOT_RUN

Новый survival Nether без OP: найти открытые включения и подземные жилы каждого
вида в5seed/4окнах; записать координаты, biome,Y, количество по100чанков, quartz/gold/debris.
Каменная кирка для livingstone, железная infernal, алмазная alatyrite/runnik; вручную
и Silk/FortuneI–III проверить drop, wrong-tool/Creative/explosion негативы. Обжечь
каждый raw по1 в furnace/blast furnace, сверить1:1 и200/100t; выгрузить во время
работы, проверить хопперы/полныйoutput. Слиток: точная схема QAQ/MGB/SAI изIssue;
сырой вместообожжённого/пустойслот/неверныйitem должныотказать. Сохранить/перезайти,
повторить с2игроками. В Overworld/End,старыхчанках и структурах нетновойруды.
Риски: native frequency, производительность, codecs/tools/JEI не проверены в игре;
широкие structure skip могут снизить частоты. Не удалять новые IDs из использованного
сейва: для отката нужна исходная копия мира и JARbackup.
