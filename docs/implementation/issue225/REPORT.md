# #225 / #226 — оружие, модели и реальный runtime, 23.10.2026

Поручение владельца: исправить одним пакетом замечания по фигурам, настоящему охотничьему рогу, плети, кистеням и Сабле Воеводы; проверять самостоятельно в отдельном мире. Повторное разрешение запуска дано23.10. Прежние отчёты о «починенном роге» не доказывали правильный ID: ранее исправлялся beast_challenge_horn, фактически показан ohotnichiy_rog. Владелец отверг прежние визуальные результаты; #217/#219/#220 возвращены из готовых на доработку и теперь повторно сдаются на личную приёмку.

## Что исправлено

- Фигуры fire_idol_tree/sun/circle: полный occlusion скрывал землю под неполной моделью. Возвращена видимость основания без смены geometry/collision/loot. Проверены12facing состояний и native установка/разрушение. Текущий заданный loot — каменные кирпичи; он сохранён, выдача самой фигуры не заявляется.
- Именно ohotnichiy_rog:22исходных кубоида сохранены, прежний transparent icon больше не используется как материал всех3D поверхностей. Непрозрачный рог/металлические обручи/крепление, отдельный прежний GUI icon, обе FP руки целиком в кадре. Материалы, UV и display адресно исправлены. Последний actual client: issue225-horn-textured.
- Плеть: одинаковые существующие wood/leather/gold материалы у GUI и held; сохранены5×36исходных элементов. Исправлен проход bound-model, тонкая свободная часть, движение в обеих руках и направление от первого лица. Дальность/урон сервера не менялись.
- Кистени: bound model сохраняется через render passes, ядро и цепь движутся при native swing. Убрано удлинение интервалов между звеньями, которое разрывало цепь. Старый лишний рецепт flail отключён; сохранённый ID flail является железным вариантом, назван «Железный кистень» и собирается из железного ядра. Всего7материальных вариантов и отдельный атаманский, без удаления предметов из миров.
- Сабля Воеводы = trophy_kalin_sabre: Slime реализует Enemy, но не Monster; прежний фильтр пропускал слизней. Исправлено, союзники/животные не стали целями способности. Показанный сектор приведён к прежним фактическим5.5блокам и углу;5урона,6целей,140тиков cooldown сохранены. Обычный удар также проверен.
- #226 выявлена уже нормальной генерацией: обычная мебель ошибочно была EntityBlock с null factory; у сушилки/гроба и других multipart helpers та же проблема. Реальные контейнеры сохраняют свои типы и данные; scoped common mixin приводит hasBlockEntity неосновных частей к существующим factory. Все registryIDs/инвентари корневых блоков/геометрия сохранены.21вид мебели и176multipart состояний проверены; новый мир и реальный повторный вход без прежних BE warnings.

## Доказательства и пределы

Исходные пользовательские кадры: owner-before/. Actual JSON/UV source A/B: horn-actual-source-AB.png и whip-five-source-UV-AB.png (одинаковые камеры, исходники в before/); это offline, не игровой снимок. Настоящие клиентские кадры: ../weapons87/issue217/issue225-client-verified/ и issue225-horn-textured/. GIF poloz_whip-actual-phases.gif / flail-actual-phases.gif — последовательности реальных кадров, без синтеза промежуточной анимации. Native38tests в logs/issue225-runtime-release.log. Статический JAR checker сам запускает0Minecraft; весь пакет действительно имеет runtime, учтённый в runtime-runs.json, его поле0 не описывает весь пакет.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Основание фигур,12состояний, native placement/break/defined drop | ISSUE225_FIGURES_12_OCCLUSION_STATES_PASS;ISSUE225_FIGURE_NATIVE_PLACE_BREAK_DROP_PASS; actual кадры с землёй | PASS |
| Правильный рог,22элемента/UV/opaque surface, обе руки | source-geometry-gates.json; horn-textured4views; source AB | PASS source/runtime; ручная эстетическая приёмка владельца ожидается |
| Настоящий Item.use рога: выбор цели/отказ днём/ночная охота/без дубликата | ISSUE225_ACTUAL_HUNT_HORN_SELECTION_DAY_GUARD_PASS;ISSUE225_HUNT_HORN_NATIVE_NIGHT_ENCOUNTER_UNIQUE_PASS | PASS |
| Плеть и ядро кистеня движутся, непрерывная цепь, обе руки/Steve/Alex | native client38/40/44phases, actual side frames, GIF; исправлены первые неудачные варианты | PASS observed; не измерение FPS/MP |
| GUI:88предметов без выхода за слот | inventory-frame-bounds.json | PASS actual framebuffer |
| Семь материальных кистеней, старого рецепта нет, ID сохранён | ISSUE225_FLAIL_SEVEN_MATERIAL_RECIPES_NO_GENERIC_PASS;preservation-gates.json | PASS |
| Сабля:слизень/сектор/дальность/стена/союзники/6cap/cooldown | ISSUE225_SABRE_SLIME_CONE_PASS;SIX_CAP_WALL_PASS;USE_REUSE_FRIENDLY_PASS | PASS |
| Обычные удары саблей/8кистенями/плетью | ISSUE225_PRIMARY_NATIVE_DAMAGE_PASS weapons=10 | PASS |
| Реальное серверное меню:расход/выдача/запрет дублирования | ISSUE225_NATIVE_MENU_CRAFT_COST_ANTIDUPE_PASS recipes=37 crafts=74 | PASS |
| Мебель и multipart root/helper contracts | FURNITURE_ENTITY_CONTRACT_PASS21;MULTIPART_ENTITY_ALL_STATES_PASS176 | PASS |
| NORMAL seed2252026:два натуральных StructureStart,71chunk и контейнеры | world-gates.json;natural-world/results-fresh.json;world-verified.log | PASS sampled generation; не все структуры/биомы |
| Реальный отдельный процесс повторного входа,3алмаза в ящике сохранены | world-reload.log:ACTUAL_PROCESS_RELOG_CRATE_PASS;results-reload.json | PASS actual restart, не NBT roundtrip |
| Генератор повторяемый, scope24production paths, никаких удалений | generator-gates.json;production-diff.json;source-before.json | PASS |
| Java21 clean build, production NeoForge JAR | final-build.log49s;final-jar-gates.json7633resources/1811classes, QA excluded | PASS |
| Полная кампания/все боссы/все биомы/MP/измеренные MSPT/FPS | за пределами данного проверенного пакета | NOT_RUN / OUT_OF_SCOPE; не заявляется полное тестирование мода |
| Итоговая личная приёмка нового визуала владельцем | ожидается | OWNER_PENDING |

В нормальном мире проверены курган и огненное святилище, seed2252026: X=-720,Z=-640 и X=-2240,Z=1952. Первоначальный native locate дал эти координаты; повторы используют их в новом NORMAL мире и проверяют реальные сгенерированные starts/chunks. Снимки контейнеров до/после postprocessing имеют разные counts, не выдаются за полный loot audit. Редкие abode/swatchtower не пройдены: исходный locate прерван после дорогостоящего поиска, это не PASS всех4структур и не steady-state MSPT benchmark. Сохранены логи/стек. [Получение предметов](HOW_TO_OBTAIN.md).

Неудачные итерации сохранены: ночной horn fixture сначала не обновлял sky brightness; тест дропа сначала ошибочно требовал саму фигуру вместо заданного stone_bricks; первые анимации имели толщину/разрывы; сокращённый список QA Skins вызвал IndexOutOfBounds в tools-only экране и исправлен. Эти результаты НЕ объявлены PASS. Последние обязательные native38/client/world/relog проверки прошли. Полный исторический audio/bootstrap не запускался; карантинное аудио не регенерировалось.

## Артефакт и состояние

Локальная1.9.7.60, build/libs/slavicmyths-1.9.7.60.jar, SHA25619202904f71fa92917c314d5743637b132478ee1be1d8f1a8f0d1489b606b0d6. Source1192e229 + незакоммиченные изменения, точный manifest/source SHA в build-and-runtime.json. Commit/push/merge не выполнялись. Runtime проходил на dev client/server этих production исходников; упакованный JAR сам в PolyMC не запускался. PolyMC не обновлялся: там прежняя установленная1.9.7.59 SHAf211c5e5b00d626f1093b6e6ff1dcb7f87aa1051d520b155e939b71b08fd294b. Новый install не заявляется. Использованы только изолированные QA миры work/issue217 и тестовые GameTest folders; пользовательский мир не менялся. Громкость20%.

#225/#226 и повторная сдача #217/#219/#220 — «Тестирование», не «Готово». Issues OPEN, owner-accepted не добавлялся; финальная приёмка остаётся за владельцем. Общий #87/#191 и мод целиком не объявлены завершёнными.
