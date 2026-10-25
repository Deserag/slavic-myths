# #273 — цепочки двух святилищ, 25.10.2026

OPEN / IN_PROGRESS / OWNER_PENDING. `hell`, baseline `feccd53167d5d4de1368a6636106d3494725fa35`, origin/hell совпадал; рабочее дерево было чистым. Main `26e73424f16ae703e9a9b75fe432731663d30e05` не изменяется. Прочитаны #273/#269, связанные #236/#241/#272/#274/#275/#276, действующие specs1.9.3/1.9.4 и фактические исходники/ресурсы. Старые specs являются историей: актуальная 2.0 уже содержит хранителей и рецепт оберега.

В Project9 #273 добавлена и переведена в «В работе» по прямому исполнительскому заданию. Закрытия, «Готово» и owner-accepted нет. Minecraft/PolyMC/GameTest/runClient/runServer: **0 запусков**. Миры и установленный JAR не менялись; **POLYMC_NOT_INSTALLED** для этого пакета.

## Контракт / реальные игровые пути

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH без OP | TEST | EXPECTED |
|---|---|---|---|---|
| Найти обе разные святыни | SanctuaryStructure/Geometry; structure IDs sanctuary_unfading_fire, abode_pure_spring; two_sanctuaries structure_set | Исследование подходящих лесов/тайги/лугов/холмов; у источника поверхностные руины и лестница; вход обновляет книгу | Source catalog + прежние #275 gates; natural exploration NOT_RUN | Реальная постройка, запись и подсказка книги |
| Огонь: получить ключ | SanctuaryPiece marker loot:guaranteed → chests/fire_guaranteed; fire_regular | Обычные сундуки, гарантированный уголь в северо-западном складе | Настоящие NBT markers и JSON pools | Минимум один sacred_ember, не за закрытым cache |
| Огонь: зажечь чашу | SanctuaryHeart.interact/activate; SanctuaryRecords.restore | Уголь в руке → ПКМ печати у края большой чаши, не костра | Production callback/flag ordering + source hook | Один уголь только после успешной активации, FireRestored, lit/эффект/пепел |
| Вода: доступное средство | Quest185Content.POWDER, recipes purifying_ash/cleansing_powder_from_ash; BreakEvent | Два багровых камня бассейнов: порошок либо кирка; связь с огненным углём необязательна | Реальные рецепты/markers, проверка индексов/состояний | Без нового редкого предмета и без тупика до огненного святилища |
| Вода: завершить оба бассейна | SanctuaryHeart.pool/purifyByBreaking; SanctuaryRecords.pool | Западный зал, 3 бассейна, 2 tainted nodes; очистить оба | Production SavedData flags/repeat/failure | Pools=3; второй отказ/повтор не тратит порошок, открывается дальнейший путь |
| Открыть зал, включая старое прохождение | GuardianLedger.unlock; GuardianWorld.ensure/install/openDoor | После обряда пройти в восточный / северо-восточный зал | Реальные metadata, 4 rotation, old flags, fake LevelAccessor | SEALED→READY, снимаются только свои seal blocks в загруженных чанках |
| Бой и очищение | GuardianEntity, FireGuardian, SpringKeeper, GuardianRules | Войти в арену; обессилить хранителя до 5%; коснуться очага/источника | Source audit, сохранение стадий; actual combat NOT_RUN | READY→ACTIVE→PURIFICATION→CLEARED; хранитель остаётся жить |
| Награда и полезность | GuardianLedger.claim, GuardianNode; mor_fire_amulet_fragment / mor_water_amulet_fragment; GorynychUpgrade | ПКМ постамента после очищения; две части + eye_of_mor на верстаке | Reward gate/claims NBT/source; real inventory/crafting NOT_RUN | Одна личная часть каждого типа; существующий mor_ward, без нового баланса |
| Перезаход/MP/старые миры | SanctuaryRecords/GuardianLedger + BE NBT + PathData receipts | Сохранить и вернуться; второй игрок получает свою часть | Native NBT roundtrip; actual relog/MP NOT_RUN | Никаких сбросов завершения/повторной выдачи; старые выполненные ритуалы действуют |
| Ясная RU/EN инструкция | rituals273_text; обе lang; SanctuaryDecor | Книга/печать/ошибочный предмет → объяснение предмета, места, действия и продолжения | Точные scoped translation guards; JAR packaging | Нет будущих обещаний уже существующего рецепта; водная дверь не просит уголь |

## Изменения и исходные проблемы

| Святилище | Этап прохождения | Исходная проблема | Исправление | Проверка | Статус |
|---|---|---|---|---|---|
| Огонь | Активация | FireRestored и расход угля выполнялись до проверки успешного setBlock | SanctuaryRecords.restore(UUID,BooleanSupplier), SanctuaryHeart.activate: сначала фактическое состояние блока, затем flag и расход | Отказ callback не фиксирует флаг; repeat не вызывает callback; source order | PASS OFFLINE / actual click NOT_RUN |
| Вода | Активация | Аналогично порошку; pool>=2 мог загрязнить bitmask и сделать cleansed недостижимым | Проверка pool0/1 до callback и расхода; shared pool transaction | -1/2/31/32/MAX_VALUE, отказ, два разных бассейна и повтор | PASS OFFLINE / actual break/event NOT_RUN |
| Оба | Проход | Переход READY был привязан к следующему player proximity update у зала; сразу после обряда ensure только пытался populate | Один GuardianLedger.unlock использует прежние prerequisite flags; ensure сразу применяет loaded-only install; old flags не требуют нового угля/порошка | 8 сценариев type×rotation, legacy SavedData, active/cleared не сбрасываются | PASS OFFLINE / physical passage NOT_RUN |
| Оба | Сохранность прохода | Механизм открытия существовал, но был сцеплен с большим install и не имел автономного теста | Выделен фактический openDoor(LevelAccessor,...), сохранена прежняя маска двери | Все 15 cells, 4 rotation, unloaded cells не читаются, chest replacement не удаляется | PASS OFFLINE / actual chunks NOT_RUN |
| Вода | Подсказка двери | Обе печати показывали огненный текст; useItemOn отсутствовал у реального SanctuaryDecor | Отдельный sealed_spring, тот же ответ при предмете в руке | Source hook + RU/EN exact text | PASS SOURCE / UI NOT_RUN |
| Оба | Продолжение | Книга обещала только cache и «главный зал останется запечатан» | Текст ведёт к хранителю, завершающему интеракту и постаменту | Scoped translation diff/reproducibility | PASS SOURCE / readability NOT_RUN |
| Оба | Фрагменты | Описания утверждали, что рецепт оберега ещё отсутствует, хотя mor_ward/GorynychUpgrade уже реализованы | Указаны реальные две части + Око Мора на верстаке | Реальный recipe ID/serializer/ingredients/result и неизменность кода рецепта | PASS SOURCE / actual craft NOT_RUN |
| Оба | Награда | Валидность encounter для claim проверялась inline без проверки Origin | Общий production rewardAvailable допускает только CLEARED не-Standalone с Origin; остальная выдача сохранена | Стадии до/после очищения, standalone, NBT receipts | PASS OFFLINE / inventory delivery NOT_RUN |

Это ограниченные связки существующих систем. Новых registry IDs, боссов, наград, моделей, геометрии, HP/урона/лут-весов, второй системы сохранений и tick world scans не добавлено. Изменены пять production Java-файлов и только две локализации в ресурсах. Номера бассейнов с неверными битами в уже повреждённом старом NBT автоматически не ремонтируются: новых таких записей не создаётся, а диагностика повреждённого сохранения требует отдельной копии и фактических данных.

## Что уже работало и сохранено

- Реальные 25 fire + 36 spring NBT tiles, native start/family/reservations, источники/чаши/сундуки и ротации. Их содержимое не менялось. Гарантированный sacred_ember в fire_guaranteed уже был; в fire_regular тоже уже снята случайность ingredient pool. Это не новая выдача #273.
- Огонь: первое восстановление даёт исходный purifying_ash через прежнюю NightLedger durable delivery; cache содержит purifying_ash/ancient_rune_fragment/flame_vial. Доставка при полном инвентаре остаётся в прежней pending queue; новой награды нет.
- Вода: порошок cleansing_powder уже зарегистрирован; purifying_ash = sacred_ember + paper (2), cleansing_powder_from_ash = ash + bone_meal (4). Разрушение двух tainted_spring_node киркой — независимый путь без порошка. Cache содержит purification_stone/ancient_rune_fragment/spring_tear; обычные сундуки дают прежние ресурсы по прежним шансам.
- Хранители: `extinguished_flame_guardian` и `defiled_spring_keeper`; текущий баланс **900/800 HP**, armor7/4, melee5/3, порог5%=45/40 HP. Исторические 500/450 из specs1.9.4 уже заменены предыдущим общим health-пакетом; #273 их не возвращает. Огонь — melee/pulse/lockdown/slam; вода — whip/whirl/prison/flood/heal/две valve. Их combat-код, атрибуты, размеры, ветвь урона и модели не менялись.
- Завершение не является смертью: GuardianEntity.setHealth переводит в PURIFY, после правильного интеракта сцена60 ticks переводит в CLEARED, хранитель остаётся мирным. Пустой entity loot здесь намерен, а не «пропавший дроп».
- GuardianLedger.populate использует UUID/LastPosition/MissingSince и chamber-loaded checks; существующий алгоритм respawn/дедупликации не переписывался. После CLEARED восстановленная сущность конфигурируется очищенной; незавершённая сцена после relog повторно запускается интерактом, а не выдаёт фрагмент автоматически.
- Постамент даёт персональную часть один раз на UUID/тип, в том числе пришедшему позже игроку, по исходному spec. Spectator/мёртвый/Standalone/неочищенный encounter награды не дают. При полном инвентаре просит место; inventory+player receipt сохраняются до world claim. Потеря уже полученной части не разрешает новую выдачу. Требование участия относится к advancement, а не праву позднего игрока получить личную часть — это прежнее утверждённое правило.
- Рецепт `mor_ward` с serializer `slavicmyths:gorynych_upgrade` уже требует двух разных фрагментов + eye_of_mor, category misc и верстак шириной3. Результат помечен `GuardianWard196`, прежний use даёт WARD_EFFECT без расхода этого собранного предмета. Крафт, эффект и его баланс не менялись. Око Мора добывается прежним путём Полоза; полный аудит его доступности вне #273.

## Gates и честные границы

Новый fixture — обычная JVM, vanilla block registry bootstrap, без Level/Server/Client. Проверяет production методы SanctuaryRecords, GuardianLedger.unlock/rewardAvailable/load, GuardianWorld.openDoor с ограниченным LevelAccessor proxy, фактические compressed NBT markers всех61tiles. Это **не полноценное прохождение, не доказательство клика/смерти/лутания/AI или MP/relog**.

Команда: Java21 `gradlew.bat --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1536m -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8" -I tools/rituals273-offline.gradle -I tools/performance274-offline.gradle -I tools/structures275-offline.gradle -I tools/encounters271-offline.gradle -I tools/yaga272-offline.gradle clean build verifyRituals273Offline verifyPerformance274Offline verifyStructures275Offline verifyEncounters271Offline verifyYaga272Offline`.

`build-clean.log`, `checks.log`, `receipt.json` содержат фактические результаты и JAR/SHA. Новый scope guard pin-ит всё production вне пяти Java/двух lang к `feccd531`; guards #274/#275/#271/#272 явно передают только новый scope, остальные проверки остаются. Новые native fixture-классы не входят в JAR. `rituals273_text.apply` воспроизводит только свои ключи идемпотентно; оба исторических asset language generators применяют тот же override. Полные старые генераторы не запускались: они затрагивают чужие ресурсы/аудио. Звуки не менялись и не генерировались.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Настоящие IDs/центры/источники/рецепты/награды | Source graph, compressed NBT marker fixture | PASS (SOURCE/OFFLINE) |
| Flag после успешной активации; два pool; повтор/invalid index | Production callback fixture + consumption source wiring | PASS (OFFLINE); actual inventory NOT_RUN |
| READY/door и старые выполненные flags | Production unlock, door fixture, 4rotation×2types, NBT | PASS (OFFLINE) |
| Точные тексты RU/EN и отсутствие незаявленных resource changes | Scoped lang/resource guard | PASS (checks.log) |
| Фактический spawn, AI, урон, переход при настоящем lethal hit | Source audit без запуска | NOT_RUN |
| Настоящая выдача лута/fragment/player save/MP2 | Source receipt ordering + NBT policy, без игрока/сервера | NOT_RUN |
| Физическая проходимость/визуал/источники на 5 seed | Запуск запрещён | NOT_RUN |
| Java21 build/resources/JAR, #271/#275/#272/#274 regressions | Финальная квитанция ниже | PASS (build-clean.log, checks.log) |
| Установка и приёмка | Не выполнялись | NOT_RUN / POLYMC_NOT_INSTALLED / OWNER_PENDING |

## Как проверить в Minecraft — после отдельного разрешения

Сборка `build/libs/slavicmyths-2.0.1-HELL.jar`, точный SHA ниже и в receipt.json; source — коммит публикации этого отчёта. В этом пакете установки нет. Перед будущей установкой — backup установленного JAR и проверка единственного modIDJAR/SHA. Новый отдельный Survival-мир + копия старого, не оригинал; Normal difficulty, прежний NeoForge21.1.255/Java21/модпак. Два survival-игрока для MP. Поиск и прохождение ниже без OP; команды отдельно лишь для диагностики. Направления в описании относительны планировке: в повернутых природных сооружениях ориентироваться по двору/входу/боковым залам; диагностические local coordinates приведены только для сопоставления.

### Огонь

1. Найти сооружение обычным исследованием и войти. Ожидать запись святилища в книге, объясняющую уголь→печать→зал→очищение→постамент. Для проверки natural availability повторить на нескольких seed, не засчитывать /locate за найденное в Survival.
2. Открыть обычные сундуки; в складе сбоку от деревянного зала найти гарантированный Священный уголь. Источник гарантии — marker local(11,2,17) → fire_guaranteed. Не ломать/не использовать закрытый ritual cache как единственный источник ключа. Повторное открытие/relog не должны генерировать уголь заново.
3. Сначала ПКМ печати чаши пустой рукой и чужим предметом. Ожидать понятный источник угля, без расхода/награды. Сам костёр не активирует обряд. Затем держать один уголь и ПКМ низкого блока печати у края большой чаши (local40,2,51). Ожидать расход ровно1, усиление огня, пепел через прежнюю доставку, открытый side cache и указание восточного зала. При защищённом/недоступном интеракте предмет сохраняется.
4. Повторить углём и вторым игроком: расхода и повторной ритуальной награды нет. Подойти к запечатанному залу: собственная печать снята; сундук/поставленный блок вместо одной части печати не должен исчезнуть. Дверь не должна подгружать далёкие чанки.
5. Войти в арену с обычным оружием. Хранитель просыпается, атакует только внутри комнаты, телеграфирует pulse/полосы/удар. На5% HP он обессиливает и остаётся живым. Никакого обещанного фрагмента из смерти нет. Записать, если застрял/неуязвим вне штатной фазы/не перешёл в очищение.
6. ПКМ ритуального очага в зале (local65,2,36) либо прежней центральной чаши. Дождаться сцены около3с; хранитель становится очищенным. ПКМ постамента (local71,3,37) со свободным слотом → ровно1 mor_fire_amulet_fragment. С полным инвентарём сначала отказ без потери права; освободить слот и повторить.
7. Второй игрок получает свою часть; первый повторно не получает. Выйти и реально перезайти: огонь/проход/мирный хранитель/claim сохранены, ни новых враждебных копий, ни повторного фрагмента. На копии старого мира с уже выполненным огнём не нужен новый уголь.

### Чистый источник

1. Найти поверхностные руины и спуститься штатной лестницей. Книга и водная печать должны вести к двум багровым камням, а не просить уголь. Проверить подсказку с пустой рукой и предметом в руке.
2. В боковом зале найти три бассейна, из них два tainted nodes (local23,1,39 и23,1,48). ПКМ чужим предметом: объяснение «порошок или разбить», без расхода. На одном прогоне использовать кирку для обоих — этот путь не требует первого посещения огненного святилища.
3. На другой новой постройке проверить порошок: уголь+бумага→2пепла, пепел+костная мука→4порошка на верстаке. ПКМ первого узла тратит1порошок; повтор уже очищенного не тратит. После первого зал ещё закрыт; после второго Pools=3, локальные тёмные блоки очищаются, cache/проход открываются. Не требуется чистить каждую трещину или вычерпывать воду.
4. Пройти в зал хранительницы. Должна быть одна хозяйка, оба затвора, чистый источник и постамент. Во время лечения перекрыть оба затвора; один не должен полностью отключать лечение. Не засчитывать простую регистрацию entity за работающий бой.
5. Обессилить до5%, ПКМ чистого источника в её зале (ритуальная точка74,1,10 либо источник79,1,21), дождаться сцены. Хранительница остаётся живой, мирной; ПКМ постамента81,2,12 выдаёт1 mor_water_amulet_fragment. Полный инвентарь/повтор/другой игрок — как для огня.
6. Настоящий relog после первого бассейна, после двух, во время боя и ожидания финального очищения, после награды. Ожидать сохранение бассейнов/прохода/claims; незавершённая боевая фаза может безопасно вернуться READY, pending purification требует повторного интеракта, а завершённая не сбрасывается.
7. Соединить обе полученные части и Око Мора на верстаке → существующий Оберег от Хвори. Проверить применение/эффект по текущим правилам; отсутствие рецепта теперь противоречит подсказке и должно быть записано как FAIL, а не обойдено выдачей предмета.

### Общие негативные/MP и соседние регрессии

На копии старого мира проверить уже очищенные святилища и неоткрытые/открытые сундуки; не удалять старые items. MP2: одновременно активировать один узел/чашу, открыть один постамент, выйти одному во время сцены; общий ритуал однократный, личные claims независимы, нет дубля NPC. Peaceful: бой не обязан запускаться; вернуться на Normal и проверить восстановление без создания второй структуры. Проверить отмену и сохранность reservations #275, поиск книги и доверие #272, отсутствие новых stalls/ticket leaks #274, встречи #271. OP-диагностика отдельно: точные `/locate structure slavicmyths:sanctuary_unfading_fire` / `slavicmyths:abode_pure_spring` используют прежний compatibility creation path; они не доказывают естественную доступность.

**НЕ ПРОВЕРЕНО:** реальные новые/старые структуры и проходимость, runtime block events/защита/расход предметов, падение/контейнерный лут, бой/AI/урон/клиентская сцена, actual fragment delivery и crafting/use, синхронизация MP, настоящий restart/respawn/deduplication, FPS/MSPT/tickets. Ноль игровых запусков означает ни одного gameplay PASS.

## Риски/откат / передача

Риски: немедленная loaded-only установка encounter nodes после обряда, пользовательские заменённые блоки прохода, custom protection mods/BreakEvent ordering, неполностью загруженные комнаты, переполнение инвентаря и crash ordering двух существующих receipts. Новые flags не меняют формат и не обнуляют старый прогресс. Откат — обратный коммит пакета в `hell`, без reset/force и без перезаписи миров; не возвращать старые JAR на оригинальный мир ради теста без копии.

#276: лут-веса и полезность не менялись. Перед ребалансом сравнить всю награду приключения: ordinary chests + cache + личный фрагмент/действующий рецепт, а не только один сундук. Сейчас нет основания самовольно повышать числа. Конкретное предложение: сначала owner-run цепочки выше и оценка пользы собранного оберега, затем согласовать отдельно любые изменения ресурсов/шансов. Не добавлять случайные сюжетные ключи или новые украшения в #273.

Следующий пакет — **#278**, только после отдельного поручения: аудит существующих accessory IDs, реальных слотов/надевания/эффектов/снятия/relog/MP. Здесь #278 не реализовывалась. Сохранить полученные фрагменты и текущий отдельный consumable/use путь mor_ward; не объявлять любой оберег Curios-украшением по названию.

## Финальная техническая квитанция

Java21 clean build **PASS, 3м14с**, 16 tasks executed, max-workers1/ActiveProcessorCount2/heap1536m. 100 прежних deprecation/unchecked warnings; Gradle test NO-SOURCE. Новый native fixture **3 suites / 201 checks PASS**. Регрессии: #274 **90701**, #275 **99852**, #271 **39253 + 2M histories**, #272 **76**, все PASS. Итого **230083 native checks**, без миров/клиента/сервера.

Все6 source/resource/JAR scripts в checks.log exit0. Scoped RU/EN generator применён дважды, изменённых bytes0. Все production-файлы вне пяти Java и двух lang совпадают с baseline; в частности templates, loot/recipes, entity/combat/render/health, #274 jobs/tickets и #272/#271 mechanics сохранены.

Production JAR: `D:/slavic-myths/build/libs/slavicmyths-2.0.1-HELL.jar`, версия **2.0.1-HELL**, SHA256 **273767789fb6555f16c485bf64bc2cf37627787f1ca50255d820bbe36660182c**. 8001 ресурсов, 4402 JSON, 1443 PNG, 1886 models, 1942 Java21 classes; offline fixtures отсутствуют. JAR не установлен. Старый установленный HELL JAR сохранён с SHA `d85d42e521e1bd8f270033545555dcc5a804626669f223f8f591b50e514cd598`.

`files.txt` — все изменённые файлы; `receipt.json` — source hashes/JAR; `offline.log` — фактические fixture results. Source commit — коммит публикации этого каталога в hell, его SHA сообщается в финальном ответе (не записывается в себя). GitHub #273 OPEN/«В работе», main прежний. Результат не является игровой приёмкой: runtime/owner NOT_RUN.
