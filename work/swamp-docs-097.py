from pathlib import Path
import json
R=Path('.');D=R/'docs/swamp'
def write(p,s):p.write_text(s.rstrip()+'\n',encoding='utf-8')
write(D/'SWAMP_0.9.7_STRUCTURES.md','''# Болотные постройки 0.9.7

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Семь семейств, по три авторских шаблона: 21 modern NBT (DataVersion 3955). Старые template IDs и NBT сохранены для старых StructureStart; новые starts выбирают v097_*.

| Назначение | Существующий/новый structure ID (slavicmyths:) | Новые шаблоны | Габариты | Награды |
|---|---|---|---|---|
| Рыбацкая избушка с причалом | fishing_camp | v097_fisher_0..2 | 13×12×17 | 2 |
| Сторожевая вышка | swamp_watchtower (новый) | v097_watchtower_0..2 | 9×21×9 | 2 |
| Открытое святилище | flooded_shrine | v097_shrine_0..2 | 13×9×13 | 2 |
| Заброшенная избушка | swamp_hut | v097_abandoned_0..2 | 13×12×15 | 2 |
| Полузатопленный настил | bog_causeway | v097_boardwalk_0..2 | 7×7×19 | 1 |
| Лодочная стоянка | swamp_remnants | v097_landing_0..2 | 11×10×13 | 2 |
| Редкое большое святилище с домом и вышкой | abandoned_settlement | v097_cluster_0..2 | 35×22×33 | 5 |

Габариты: X×Y×Z bounding box шаблона, а не высота занимающей весь объём постройки. Подводные руины underwater_ruins сохранены дополнительно, их исходные шаблоны не перерисованы. Печи, столы, снасти, керамика, светильники, кровати/полки и повреждённые крыши образуют различимые помещения. Новые входы избушек шириной два блока допускают крупного болотника. Декоративные бочки отдельно от отмеченных контейнеров добычи.

Новые точки наград не имеют старого случайного удаления контейнера. UUID/внутренние маркеры прежних курганов не меняются. Болотные контейнеры имеют собственную отметку SlavicSwampLoot, повторный postProcess сохраняет инвентарь; обработанные точки встреч сохраняются по координатам в StructurePiece NBT. В редком святилище предусмотрен один гарантированный страж вне Peaceful; остальные места имеют ограниченный детерминированный шанс встречи.

Ванильное болото — единственный target для этих structure tags. Никакой новой biome registry entry. Частота random spread (чанки spacing/separation): настил 16/8; стоянка 20/10; рыбацкая 26/13; заброшенная 30/15; вышка 32/16; святилище 36/18; редкий кластер 56/28. Candidate-проверка учитывает конкурирующие starts без загрузки чанков. Это параметры размещения кандидатов, не обещанная плотность принятых построек.

Весь start валидируется до добавления pieces: полный шаблон, перепад почвы и поверхности ≤4, глубина поддержки ≤7, допустимая высота мира. Опоры продолжаются до дна максимум на восемь блоков и пишут только в clip. Прямоугольного осушения/выравнивания нет. Невалидная глубокая вода/тонкий flat отвергаются без частичной постройки.

Headless: 42 размещения на искусственной сухой/мокрой площадке, 96 непустых наградных контейнеров, 720 опор, устойчивые фонари, повтор без сброса инвентаря/повторного спавна. Через настоящий Structure.generate проверены семь accepted starts на толстом swamp-flat и отказы для глубокой воды, тонкого flat и другого биома. Естественную плотность, швы ландшафта и вид в обычном мире ещё нужно проверять вручную.

Диагностика: templates-0.9.7.json; реализация SwampStructure/SwampPiece; воспроизводимый генератор tools/swamp_resources_097.py.
''')
write(D/'SWAMP_0.9.7_MOBS.md','''# Болотные встречи 0.9.7

| Существо | HP | Роль и ограничения |
|---|---|---|
| Русалка, новая в vanilla swamp | 36 (прежде 28) | Существующая песня/приманивание, ближнее давление у воды; AI и визуал сохранены |
| Водяной, новый в vanilla swamp | 72 (прежде 44) | Редкий водный противник; прежние отношения, дары и подтягивание сохраняются |
| Болотник | 48 | Медленный тяжёлый ближний противник: damage 7, armor 4, knockback resistance 0.4, range 24 |
| Страж большого святилища | 112 | Вариант болотника в редком месте, damage 9; охраняет лучшие награды |

Усиление водных духов выполняется только в finalizeSpawn для minecraft:swamp. Сохранённые существа не перебалансируются при загрузке, новые русалки/водяные других биомов сохраняют прежние HP. Боссы глубин и остальные мобы не менялись.

Болотник получает цель по прямой видимости в десяти блоках; умеет двигаться в воде через существующий WaterSpirit, melee goal и плавание. Успешный удар может дать Slowness I на три секунды, cooldown шесть секунд; нет постоянного root или нового глобального tick polling. Дикий спавн ночью, только vanilla swamp, по одному и вне Peaceful. Структурные встречи привязаны к отдельным сохранённым маркерам, без ticking spawner.

Новый ID slavicmyths:bolotnik, яйцо slavicmyths:bolotnik_spawn_egg; все предметы попадают в существующую creative tab. Болотник имеет отдельную широкую модель тела, тяжёлые руки, корни и отдельную авторскую текстуру 256×256, не перекрашенный водяной. Native mud sounds. Исходник изображения и provenance: art/swamp-0.9.7/. Drop: moss_block (1–3), pearl_fragment с шансом 30%; сильная встреча также связана с лучшей структурной добычей, не новым оружием.

PASS: создание/атрибуты/сохранение болотника, фактические HP новых водных духов в swamp и сохранение HP русалки вне swamp, существование стража при реальном размещении кластера и отсутствие повторного спавна при retry. Ручное качество боя/поиска пути и модели остаётся NOT STARTED.
''')
write(D/'SWAMP_0.9.7_TREES_AND_NATURE.md','''# Деревья и природа 0.9.7

Новый swamp_nature feature и пять configured/placed features внедрены через woodland_swamp biome modifier только в minecraft:swamp. Vanilla trees не удаляются. Лесные/саплинговые TreeShape и другие биомы не перерабатываются.

- Willow: ствол 7–12 блоков, наклон, асимметричные округлые кроны, редкие свисающие листья и корни в мелкой воде.
- Oak: ствол 8–14, двойная толщина, изломы и боковые сучья, неровная крона.
- Pine: 12–20, тонкий вертикальный ствол, узкие ярусы хвои, только сухая почва.
- Dead: 7–11, голые кривые ветви и корни, без новой листвы.

Несколько изгибов, направлений и размеров из seed; дерево ограничено 2400 cells. Геометрия строится до записи, весь объём проходит проверку. Не заменяет пользовательские блоки или соседние деревья. Корни допускают ограниченную замену натуральной почвы и воды; листва остаётся над водой. Leaves DISTANCE вычисляется по реальной связности с logs (1–6), отсоединённые фрагменты отбрасываются. Свисающие сегменты ставятся сверху вниз.

Rarity filters на попытки в подходящем чанке: willow 1/3, oak 1/8, pine 1/12, dead 1/10. Деталь: до 12 мест возле origin, существующие reeds/white lilies, moss carpet и иногда лежащий log. Никаких обходов мира каждый тик. Occupied-проверка не размещает природу поверх starts болотных структур.

PASS: 400 seeded shapes, больше 50 различных форм каждого типа, ограниченные размеры и связная листва. Реальные GameTests размещают четыре сухих и три водных дерева; pine в воде и все типы вне vanilla swamp отвергаются. Примерное распределение/читаемость силуэтов в обычном мире оцениваются вручную.
''')
write(D/'SWAMP_0.9.7_BLOCKS_AND_ORES.md','''# Материалы болот 0.9.7

Новых строительных блоков и руд не добавлено: пакет допускает целевые изменения материалов, но не требует новых ore variants. Текстуры и баланс существующих руд сохранены.

Постройки используют dark oak/spruce planks, logs, stairs/slabs/fences/trapdoors, mossy/cracked/chiseled stone bricks, ограниченный cobweb как старые снасти, candles, lantern/soul lantern, flower pots, настоящий существующий slavicmyths:altar и vanilla barrel. Дно — естественная clay/mud/dirt, без массового изменения terrain. Деревья используют существующие willow/pine семьи и vanilla oak; корни — настоящие logs, а не отдельные пустые decor IDs.

Полузатопленные фрагменты используют водонаполняемые slabs/trapdoors. Стойки имеют полноценные опоры до дна. Материалы можно добывать и повторно использовать для строительства без новой бессмысленной цепочки рецептов. Существующие рецепты и loot блоков не менялись.

В дополнительном runtime hotfix липовая дверь переведена на все 32 native door blockstates Minecraft 1.21.1, восемь актуальных моделей left/right/open; четыре старых model IDs сохранены как корректные aliases. ID linden_door и её текстуры/recipes/drop сохранены. Проверено разрешение всех новых ссылок и точное соответствие vanilla oak door rotations.

Остаются 16 прежних missing vanilla model parents дверей darkened/pine/rowan/willow; пакет пользователя требует исправить linden, остальные отдельно задокументированы. Новых missing assets нет. Проверку в игровом освещении не выдаём за выполненную.
''')
write(D/'SWAMP_0.9.7_LOOT.md','''# Награды болот 0.9.7

Пять новых таблиц: slavicmyths:chests/swamp_fisher, swamp_watchtower, swamp_ritual, swamp_salvage, swamp_great_shrine. Все старые 0.9.6 таблицы сохранены байт-в-байт. Дополнительно используются старые swamp_supplies/swamp_hidden.

Fisher: fish, fishing_rod, string, reed. Watchtower: arrows, bow, bread, iron_ingots. Ritual: bones, reed, pearl_fragment, редкий ancient_water_sign. Salvage: iron_nuggets, string, bread. Great shrine: больше бросков, pearl_fragment, ancient_water_sign, emeralds и gold_ingots. Common: 2–4 броска, great: 3–6. Нет условия, которое случайно удаляет все броски новой таблицы.

Микро-настил имеет одну награду; малые/средние места — две; кластер — пять распределённых точек, включая верх вышки. Трофеи поддерживают существующие отношения/ритуальные компоненты и практические припасы; новые ключи, карты с выдуманными координатами, оружие и отдельная progression не добавлены. Лучший кластер защищает существующий вариант нового болотника 112 HP.

В GameTests 96 контейнеров на 42 сухих/мокрых размещениях реально раскрыты через loot manager и оказались непустыми; повторная генерация того же piece не меняет сохранённые три тестовых diamonds. Декоративные бочки не считаются наградными точками. Экономическая ценность/частота редких результатов в длительном survival ещё не измерялась.
''')
write(R/'docs/MANUAL_QA_0.9.7_SWAMP.md','''# Ручная QA болот и hotfix 0.9.7

NOT STARTED: клиент автоматически не запускался. Используйте новый тестовый мир или копию своего, профиль Slavic-Myths-1.21.1-Testing, Java 21.

- [ ] Найти minecraft:swamp через `/locate biome minecraft:swamp`; проверить recognizability vanilla swamp, отсутствие отдельного нового биома и изменений других лесов.
- [ ] Найти несколько рыбацких избушек: крыша/док/снасти/интерьер, две полезные награды; открыть все контейнеры.
- [ ] Найти вышку: лестница проходима, верхняя площадка доступна, есть припасы и обзор.
- [ ] Святилище и редкий кластер: алтарь/приношения, отдельные помещения, редкая добыча и страж; не выглядят большой крепостью.
- [ ] Осмотреть заброшенные дома, настилы и лодочную стоянку; вариации, повреждения, освещение, смысл места.
- [ ] На нескольких берегах проверить опоры до дна, отсутствие висящих углов/осушенных прямоугольников, швы terrain и waterlogged фрагменты.
- [ ] Сравнить иву, кривой дуб, высокую сосну и мёртвое дерево: изгибы/корни/листва, отсутствие solid curtains и чрезмерного заполнения.
- [ ] Оценить плотность тростника/лилий/мха/лежалых logs, отсутствие повторяющихся одинаковых деревьев.
- [ ] Проверить болотника (48 HP), русалку (новая swamp 36 HP), водяного (новый swamp 72 HP), стража (112 HP); бой/targeting/pathfinding в воде, дверях и на настилах, Slowness и cooldown.
- [ ] `/give @s slavicmyths:bolotnik_spawn_egg`; модель/текстура/звук/creative/JEI item list.
- [ ] Сохранить и перезайти: прежние HP старых мобов, новые усиленные атрибуты, отсутствующие повторные стражи/дубли loot, прежние курганы/navigation сохраняются.
- [ ] Xaero: создать свой waypoint, открыть мир с целью Slavic, выйти через «Сохранить и выйти» минимум три раза; crash отсутствует, свой waypoint не удаляется; проверить смену измерения и включение/выключение adapter.
- [ ] JEI 19.51.0.418: ритуальная категория реально открывается, altar catalyst показывает её рецепты, нет recipeTypes must not be empty; также проверить core без JEI.
- [ ] Linden door: все стороны/верх-низ/левая-правая петля/открыто-закрыто, текстуры без missing model и корректный drop.

Vanilla locate structure: `/locate structure slavicmyths:fishing_camp`, `swamp_watchtower`, `flooded_shrine`, `swamp_hut`, `bog_causeway`, `swamp_remnants`, `abandoned_settlement` (каждый с полным namespace). Старый `/slavicmyths dev structure <id> [next]` ищет/телепортирует к месту, это не команда генерации. `/place structure slavicmyths:<id>` — vanilla dev generation, корректность placement всё равно зависит от подходящего грунта и мира.

Headless проверки не измеряют FPS/MSPT обычного мира, экономический баланс и реальные клиентские JEI/Xaero UI callbacks. 16 старых ссылок моделей других дверей остаются отдельным backlog; точный список в verification/swamp-0.9.7/resource-check.json.
''')
write(D/'SWAMP_0.9.7_RUNTIME_FIXES.md','''# Дополнительные исправления runtime 0.9.7

Xaero publish/clear обрабатывают отсутствующую сессию/WaypointsManager, смену мира/измерения и RuntimeException/LinkageError optional adapter. Clear сначала сбрасывает внутренний owned/session/world/set/publishedState, затем удаляет только точные собственные временные объекты, не очищая пользовательскую группу или файлы. Менеджер обновляется только у всё ещё текущей сессии и если не null. LoggingOut сначала сбрасывает весь NavigationClient, затем защищённо вызывает старый bridge.clear. Офлайн исполняется именно production adapter с lifecycle doubles: 10 случаев, включая null manager, уже закрытую/сменённую сессию, exception при update и сохранность user waypoint. Это не запуск настоящего клиента.

JeiRituals catalyst теперь получает ровно один существующий RecipeType<ResourceLocation> slavicmyths:rituals, общий для Category.getRecipeType/registerRecipes/registerRecipeCatalysts. Проверены compiled bytecode и фактическая сигнатура IRecipeCatalystRegistration из pinned JEI 19.51.0.418. Это JEI category type, не новый Minecraft recipe serializer. Core не требует JEI, общие классы не ссылаются на plugin. Клиентский callback/category rendering ещё требуют manual QA.

Linden door переносится на точную схему native oak door 1.21.1: 32 variants, восемь актуальных моделей; старые четыре aliases сохранены. Все новые references разрешаются по pinned assets. Другие 16 прежних door parent issues не скрываются как исправленные.
''')
summary='''# Slavic Myths 0.9.7 — болото и runtime hotfix

Реализованы: семь семейств болотных мест / 21 modern template; четыре дерева и природные детали только vanilla swamp; болотник 48 HP / редкий страж 112 HP; новые swamp русалки 36 HP и водяные 72 HP; пять таблиц структурной добычи + drop болотника. Старые ID/templates и остальной контент сохранены, новой biome/ore/block семьи нет.

Xaero logout/publish null-safe с очисткой внутренних данных перед optional callbacks, пользовательские waypoint сохраняются; JEI catalyst связан с зарегистрированным ritual RecipeType; linden_door использует native 1.21.1 states/models. Другие 16 старых door model parent issues остаются backlog.

COMPILES: production 0.9.7. Проверки/gates и hash: docs/verification/swamp-0.9.7/acceptance.json; установка: installation-receipt.json в той же папке. Headless размещение/данные/генерация/геометрия проверяются отдельно от клиента. Manual visual/gameplay QA: NOT STARTED; следующий конкретный блок — docs/MANUAL_QA_0.9.7_SWAMP.md. Клиент не запускался.

Подробнее: docs/swamp/SWAMP_0.9.7_*.md. Ниже сохранены исторические состояния прежних версий.

'''
for n in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=R/'docs'/n;write(p,summary+p.read_text(encoding='utf-8'))
p=R/'README.md';s=p.read_text(encoding='utf-8').replace('# Slavic Myths 0.9.6 —','# Slavic Myths 0.9.7 —',1);index=s.index('## Курганы 0.9.6');s=s[:index]+'''## Болото и runtime hotfix 0.9.7

Семь семейств мест, 21 вариант: рыбацкие избушки, вышки, святилища, заброшенные дома, настилы, стоянки и редкий кластер. Только vanilla swamp, без нового биома. Новые четыре силуэта деревьев/детали берегов, болотник и более сильные новые болотные водные духи, распределённые полезные награды. Существующие ID/старые templates и остальной контент сохранены. Новых руд/строительных блоков не добавлено.

Исправлены Xaero cleanup после закрытия сессии, пустой JEI catalyst recipeTypes и linden_door models после порта. Пользовательские Xaero waypoint не удаляются. Клиент не запускался; реальный logout/JEI GUI требуют ручной проверки.

[Постройки и частота](docs/swamp/SWAMP_0.9.7_STRUCTURES.md), [мобы](docs/swamp/SWAMP_0.9.7_MOBS.md), [деревья](docs/swamp/SWAMP_0.9.7_TREES_AND_NATURE.md), [материалы](docs/swamp/SWAMP_0.9.7_BLOCKS_AND_ORES.md), [loot](docs/swamp/SWAMP_0.9.7_LOOT.md), [hotfix и границы проверок](docs/swamp/SWAMP_0.9.7_RUNTIME_FIXES.md), [ручная QA](docs/MANUAL_QA_0.9.7_SWAMP.md).

```text
/locate biome minecraft:swamp
/locate structure slavicmyths:fishing_camp
/locate structure slavicmyths:swamp_watchtower
/locate structure slavicmyths:flooded_shrine
/locate structure slavicmyths:abandoned_settlement
/give @s slavicmyths:bolotnik_spawn_egg
```

Полный список остальных structure IDs и locate/dev команд — в документации построек и QA. Проверки Java 21: `gradlew.bat clean build verifyKurganRework verifyPortCore verifyNavigation verifyPortComponents verifyPortPayloads verifyPortGeometry`; loader: `gradlew.bat -PportRuntimeCheck -PwithoutJei runRuntimeChecks` и `gradlew.bat -PportRuntimeCheck -PportRuntimeCompanions runRuntimeChecks`; ресурсы: `python tools/verify_static_data_1211.py --swamp-rework`, `python tools/verify_swamp_097.py`. Офлайн hotfix: `python tools/verify_xaero_lifecycle_097.py`, `python tools/verify_jei_ritual_contract_097.py`.

Остаются 16 старых missing parents моделей других дверей, список в [resource report](docs/verification/swamp-0.9.7/resource-check.json). Новые модели/ссылки валидны.

'''+s[index:];write(p,s)
p=R/'docs/port/PORT_STATUS_0.9.4.md';write(p,p.read_text(encoding='utf-8')+'\n\n## Обновление дополнительного backlog в 0.9.7\n\nDONE по resource validation: linden_door — 32 native states / актуальные модели и старые aliases, четыре прежних missing parents исправлены. Остальные 16 (darkened/pine/rowan/willow) — NOT STARTED. Xaero/JEI runtime hotfix описан в docs/swamp/SWAMP_0.9.7_RUNTIME_FIXES.md; ручная проверка клиента не выполнена.\n')
print('Swamp documentation and manual QA written; evidence/deployment status finalized after remaining gates')
