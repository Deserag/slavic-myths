## 1.1.4 — Textile processing (2026-10-07)

`textile.Textiles` дополняет существующие deferred registers. `TextileEvents` заменяет только стандартные animal drops после успешного создания одной `Carcass`; XP не затрагивается. Entity variant синхронизируется, размеры масштабируются по виду; local life/fire сохраняются в NBT, processed guard исключает повторную выдачу. Нет AI, глобальных обходов или force loading.

`StationBlock` имеет три конкретных state definitions; общий `TextileStation` хранит ограниченные counts/progress, материал фиксирован типом станка. Мялка не тикает; прялка/ткацкий стол работают локальным серверным ticker, меняют visual state только при изменении значения. Ввод/выдача/разрушение выполняются на сервере. `LinenBed` наследует vanilla BedBlock gameplay, использует authored MODEL без BedBlockEntity; completed-deep-sleep PlayerWakeUpEvent flags дают ровно два атрибута well_rested, без player polling.

`ClothingItem` реализует Equipable без ArmorItem/ArmorMaterial/durability. Client-only `TextileClothingLayer` строит отдельные HumanoidModel meshes, копирует позу игрока, поддерживает standard/slim; invisible/spectator скрыты. `BeltData` — NeoForge persistent ItemStack attachment с native stream synchronization/copyOnDeath; перед death drop attachment очищается, keepInventory сохраняет. Empty-air снятие использует небольшой client-to-server payload, сервер повторно проверяет состояние игрока/руки. GUI/keybind/обязательная Curios зависимость отсутствуют. `CarcassRenderer` имеет одну low bundle geometry/семь atlas variants; `SpinningWheelRenderer` рисует вращающийся rotor только у прялки.

`tools/textile_resources.py` воспроизводит native pixels/models/data, старые crafting генераторы больше не восстанавливают bypass. `tools/verify_textile.py` проверяет JSON/links/геометрию/UV dimensions/recipes/tags/translations и byte-identical production resources. Это статические проверки, не gameplay/save-load/runtime тесты. Реальных Minecraft запусков 0.

## 1.1.3 — Animal husbandry (2026-10-07)

`husbandry/Husbandry` использует существующие deferred registers. Только три EntityType<YardAnimal>; kind определяется фабрикой, baby age сохраняет vanilla, canMate и YardParentGoal фильтруют именно EntityType. Entity-local egg timer сохраняется в NBT; feeder cooldown — absolute game time в стандартных NeoForge persistent entity data, без world manager. GooseDefence — временный local goal и synced visual state, 20-tick warning/80-tick chase, 20-tick bite cooldown; после load пассивен. DuckWaterGoal ограничен radius 8 и cadence 200..399 ticks, использует стандартный navigation/FloatGoal и land roam.

`YardFeeding` EntityInteract выполняет love/baby growth/heal/consume только на сервере; EntityJoinLevel добавляет FeederGoal и дополнительный TemptGoal один раз по наличию goal, без лишнего persisted marker. Старые vanilla isFood/tempt/drops/models не заменены. FeederGoal ищет только для baby/injured, radius 10, cadence >=40 ticks, timeout 200, consume one/heal2/grow400/cooldown600, никогда setInLove. NestGoal ищет при истечении egg timer, radius 12, path timeout200/fallback drop. YardSearch проверяет только загруженные блоки в вертикальной полосе +/-3 и сферическом радиусе, максимум 4 path attempts к ближайшим candidates; duck search +/-2. Нет global polling/chunk force loading.

`YardStorageBlock.Feeder/Nest` имеют только свои count properties и horizontal facing; общий YardStorage — non-ticking BlockEntity, inventory source of truth, save/load sanitation и onLoad visual synchronisation, block removal drops contents. Capacity16 single-type / capacity3 mixed eggs. Козье молоко наследует vanilla MilkBucketItem, без hunger/buff. Biome modifiers/loot/recipes/feed tags/advancement data-driven. Client-only HusbandryRenderer выбирает один из шести meshes и texture atlases; head accessories иерархически прикреплены к head, baby не uniform adult scale. `tools/husbandry_resources.py` воспроизводит authored cuboids/UV/native pixel assets/data, `tools/verify_husbandry.py` делает static links/UV/JAR checks без Minecraft. Vanilla audio references документированы как временные.

## 1.1.2 gardens (2026-10-07)

`garden.PerennialBush` — reusable BushBlock/BonemealableBlock, PHASE 0..4 сериализуется как прежний `age` ради сохранения старых berry states; raspberry upper/lower синхронизированы, lower authoritative, переходы randomTick 1/18. FolkBerryBush сохраняет прежний класс/ID и делегирует общему base. Loot data-driven: shears sapling, early/adult probabilities, ripe extra berry; upper loot требует surviving adult lower, поэтому уничтожение lower не дублирует drop. Fertilizer обрабатывает только lower berry state в 3x3; watering/sickle остаются farmland/crop-only.

`AppleTreeShape` хранит immutable 9 log offsets, 74 leaf offsets и 26 deterministic fruitable offsets. AppleTreeFeature используется configured apple_tree/apple_sapling; стандартный WoodlandGrower reuse и предварительная проверка пространства без уничтожения соседних блоков. AppleLog CROWN_ANCHOR=false по умолчанию; generated top log true; chance 1/8 и максимум один missing air leaf с adjacent apple timber/leaves в mask. Player placement не создаёт anchor. AppleLeaves наследует vanilla distance/persistent/decay, добавляет fruitable/fruit_stage; chance 1/24, независимые leaves, ripe harvest 1 apple +25% второй, stage reset 0. Break loot целиком исключает apple и сохраняет vanilla-like sapling/stick/Silk/shears.

`Gardens` использует существующие ModBlocks/ModItems/ModFeatures/ModTiles, отдельные типы sign/hanging sign с client-only renderer registration/Sheets wood material. Для точных apple IDs и custom log/leaves требуется отдельная регистрация семейства; старые Woodlands семьи не рефакторятся. Worldgen placement/biomes остаются data-driven, никаких global tick scans или BlockEntities для растений/anchor. `ModItemGroup` содержит 9 стандартных табов с ordering edges и детерминированным category priority; старый tab ID сохранён как lore-only. Farm preferred ordering: 1.1.0 crops/fertilizer, berries, bush saplings, apple sapling; tools отдельно; apple family в decor.

`tools/gardens_resources.py` генерирует отдельные native pixels и использует установленные vanilla 1.21.1 geometry/data schemas; старые генераторы 1.1.0/RC2 сохранены. `tools/verify_gardens.py` проверяет resources/JAR без Minecraft; статическая маска/loot проверка не считается игровым regrowth/harvest тестом.

## RC2 UI / Visual correction — 2026-10-06

## 1.1.0 farming integration (2026-10-07)

`org.slavicmyths.farming` содержит общий `FarmingCrop` (собственный AGE 0..6, lazy seed supplier, коэффициент growth gate, vanilla CropBlock randomTick/NeoForge hooks). Посадка проверяет vanilla farmland; no collision/no block entity/no global polling. Vanilla runtime/data registries дополнены через существующие ModBlocks/ModItems/ModLoot.

Четыре item-use механики выполняют изменения только на сервере, проверяют mayInteract/mayUseItemAt и не загружают чужие chunks. SickleItem получает стандартный Block.getDrops с реальным инструментом, меняет age на 1, затем выдаёт loot и изнашивает инструмент. FieldHoeItem использует стандартную HOE_TILL ability/NeoForge modification hook и отдельные проверки каждой позиции в полосе; coarse/rooted dirt → farmland при свободном верхнем блоке. Это Item с точной durability 350: TieredItem иначе перезаписал бы её iron-tier значением 250.

`ItemState.WATER_CHARGES` — Codec.intRange(0,8), persistent + network synchronized. WateringCanItem содержит default component 0, fills from source fluid without removal, modifies only FarmBlock.MOISTURE. FertilizerItem использует `sickle_harvestable` tag (7 mod/4 vanilla crops), increments each immature crop exactly once, does not call vanilla bonemeal and does not consume on no-op.

`GrassSeedsModifier` добавляет один seed в existing loot; вероятность/table identity/валидная пара tall grass задаются JSON. `CappedFortune` — зарегистрированная loot function в Registries.LOOT_FUNCTION_TYPE, применяется один раз к mature produce, +0..min(Fortune,3); seeds unaffected. Дроп остальных возрастов полностью описан loot tables. Blockstates/model textures содержат ровно 7 стадий. ClientSetup помечает новые блоки cutout только в client-side классе.

`tools/farming_resources.py` воспроизводит актуальные 1.1.0 ресурсы без изменения RC2 UI/wood/weapon assets; `tools/verify_farming.py` проверяет спецификацию данных, прозрачность/размеры/различимость, JSON/ссылки и production JAR без Minecraft. Исходная схема рецепта лейки содержит 6 I, но письменная стоимость 5 nuggets; разрешённый альтернативный нижний ряд ` I ` сохраняет стоимость 5 nuggets + bucket.


Внесён пакет UI/Visual поверх текущего main/RC2: два входа наковальни и copy-only preview, три режима/автоматический расход доп. материалов, Creative XP=0, новый Path Stone/Kitchen UI, исправлен повторный blur книги, реальные оси щита, пересобраны Club/Battle Axe UV0..16, четыре разных древесных материала и двери/люки/signs. Registry ID, рецепты, баланс survival, курганный hotfix сохранены. Сброс пути и новые пары конфликтов рун не выдуманы. Разбойники/Соловей отложены.

PASS: clean build; 10/10 loader GameTests с companions; шесть CPU/data/codec/geometry gates; production resources 0 missing refs. Реальных headless запусков в этом этапе: 2 (первый 8/10 с ограничением synthetic connection/DataSlot, финальный 10/10). Клиент: 0. GUI scales 2/3/4, игровые held poses и реальный reconnect — MANUAL PENDING. Результат не объявлен визуально принятым. Версия остаётся 0.9.10-rc2; опубликованный tag/prerelease не перезаписывается. Исправленный JAR установлен в существующий PolyMC 1.21.1 RC2 после gates; прежний сохранён вне mods, остальные моды/config/saves без изменений. SHA256 `ca32e6ac1b8a67a24cd5efadb051fbc0079e525bdb5b9642b1b0552f228a571e`. Commit/push не выполнялись по текущему AGENTS.md. Отчёт и список файлов: `docs/verification/rc2-ui-visual/REPORT.md` и `changed-files.txt`.

## 0.9.10 RC2 — server acceptance и установка (2026-10-06)

Завершены оставшиеся серверные проверки: 9/9 workstation+hotfix GameTests и 27/27 регрессий, включая подход всех трёх курганов без повреждения кладки. Щит реально блокирует спереди, изнашивается, получает axe cooldown; repair/Unbreaking/Mending/NBT проходят. Камень пути: RMB/menu, offering/XP, навыки, navigation SavedData, range guard, NBT. Наковальня: forge/install/remove, расходы, validation, shift-click, close-return и components. Кухня: 12/12 рецептов, guards, consumption, tool wear, remainder, output shift-click и NBT.

Исправлены все 16 старых door-model parents у darkened/pine/rowan/willow; native blockstates включают все facing/half/hinge/open варианты, старые имена моделей сохранены как aliases. В кухне отображаются food effects или их отсутствие, подробности в tooltip; gameplay блюд не изменён. Воспроизведение door resources: `python tools/migrate_remaining_doors_1211.py` после исторических resource generators.

`clean build` + шесть CPU/data/codec checks PASS. Production resources: 4042 references, 0 missing asset issues; static data 2685/0 errors. JAR SHA256: `faa54bd5decf79dc029b1d722f1995b195aa313dd5cea900c16bd7a70ea5a499`. Установлен в существующий PolyMC Slavic-Myths-1.21.1-Testing; прежний dev JAR сохранён вне mods, 6 прочих модов и configs/saves не изменены. Client 0 по выбору пользователя; эта итерация имела 3 реальных headless server запуска (первый исправлял test fixture, затем 9/9 и 27/27). Реальный multiplayer/перезаход, визуал и GUI scales — MANUAL PENDING. Отчёт/receipt: `docs/verification/playtest-0.9.10-rc2/`. Сборка/docs/checksum: `release/0.9.10-rc2/`.

Исходники опубликованы в `main`, она установлена основной веткой GitHub; история существующей `master` сохранена. Player branch `release/0.9.10-distribution` обновлена до RC2. Prerelease: https://github.com/Deserag/slavic-myths/releases/tag/v0.9.10-rc2 — JAR/ZIP/MRPACK скачаны обратно и проверены по SHA256. RC1 не перезаписывается, новый tag `v0.9.10-rc2`. Нового overhaul разбойников/Соловья, Equipment & Art или rune system 0.9.11 нет. Полная ordinary-world natural coverage и measured MSPT не заявлены принятыми.

## RC1 — отдельная сборка для игроков и уточнение приёмки (2026-10-06)

Опубликована отдельная distribution branch `release/0.9.10-distribution` с 9 файлами: playable JAR в mods, installer закреплённых зависимостей, lock/checksum и player docs. Исходников/Gradle/миров/configs нет. Source branch `release/0.9.10-playtest` и tag `v0.9.10-rc1` сохранены. Загрузчик реально скачал 6/6 внешних модов с проверкой размеров/SHA256; набор содержит 7 JAR. XaeroLib вложен в обе карты и отдельно не требуется. ZIP и .mrpack подготовлены для prerelease; Java/NeoForge/Minecraft и профиль пользователя не устанавливались/не запускались.

Список невыполненного: `docs/release/RC1_REMAINING_WORK.md`. P0 исправлены в коде, но реальная игровая/визуальная приёмка не пройдена. GUI scales не проверены в клиенте; дополнительные meal effects в kitchen preview не показаны; регрессия расчистки кладки не перезапускалась после исправления; 16 старых door-model parents остаются. Не обозначать RC как полностью принятую игровую сборку. Этот этап меняет упаковку/документацию; production JAR/код/контрольная сумма RC1 не менялись.

## 0.9.10 RC1 — public playtest stabilization (2026-10-06)

Ветка `release/0.9.10-playtest`, версия `0.9.10-rc1`. Исправлены UV/round model/blocking щита, зарегистрирован экран существующего RPG-меню Камня пути/рунной наковальни, добавлен shift-click наковальни; книга получила непрозрачную страницу и scroll, кухня — выбор рецепта/условия/результат. Курганный hotfix включён: сохранённые encounter slots, фиксированные квоты/боссы, наружные patches и единый 100-tick Порез. Геометрия кургана и существующие ID сохранены. Разбойники/Соловей не перерабатывались; полный Equipment & Art Overhaul и новая система рун не заявлены реализованными.

`clean build` и шесть CPU/data/codec проверок PASS; production JAR/resources PASS, 2685 статических ссылок, 0 новых ошибок. 16 прежних door-model parent issues отмечены отдельно. До последнего запроса hotfix: 5/5 GameTests; общая регрессия 26/27 обнаружила расчистку кладки, исправленную затем без повторного запуска игры. Клиент 0; после последнего запроса Minecraft не запускался. Игровая/визуальная приёмка — MANUAL PENDING. Отчёт: `docs/verification/playtest-0.9.10-rc1/acceptance.json`; playtest docs/JAR/checksum: `release/0.9.10-rc1/`. Изменения прежнего рабочего дерева включены, runtime worlds/logs/configs не включаются в release commit.

## 0.9.10 — анализ нового пакета (2026-10-06)

Прочитан Equipment & Art Overhaul FINAL; начато сопоставление с реальными ID, способностями и boss loot. Production-изменений 0.9.10 пока нет: требуется согласовать отсутствующие точные параметры и рецепты согласно просьбе пользователя не додумывать логику. Разбор: `docs/equipment/SPEC_REVIEW_0.9.10.md`. Существующие изменения 0.9.9 сохранены. Build/resource gates этого этапа не запускались; Minecraft-клиент — 0 запусков.
Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

## Текущий override: структура имеет приоритет над сушей

Реализована отдельная пошаговая ручная генерация трёх курганов и трёх лагерей: FULL-чанки до измерения высоты, проверка конечного объёма, подъём на тонком superflat, заполнение грунтом, выравнивание суши, локальный перенос от воды/защищённых построек в пределах 256 блоков. Поиск и генерация имеют status/cancel и не выполняют прежние синхронные обходы сотен удалённых starts. Проверка команд выполняется на headless сервере, Minecraft-клиент не запускается.

Подробности, ограничения отмены и результаты проверок: `docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md`. Полная 0.9.9 остаётся в разработке: естественное покрытие обычного мира и компенсация не приняты. Записи ниже — история предыдущих этапов; их заявления о неизменённом production-коде относятся к моменту диагностики.

Research update 2026-10-06: real kurgan generate command reproduces the reported false 237-block terrain rejection caused by an unloaded heightmap; original-position bedrock preflight and synchronous search risks identified. User save structure settings inspected read-only. This is a diagnosis, not a fix; production code and installed JAR unchanged. Details: `docs/worldgen/COMMAND_GENERATION_DIAGNOSIS_0.9.9.md`.

Deployment update: by explicit user request, `0.9.9-dev` is installed in PolyMC `Slavic-Myths-1.21.1-Testing` for manual generation testing. Previous 0.9.7 JAR is backed up outside mods; other mods and worlds are unchanged. This is not release acceptance. Receipt: `docs/verification/mob-worldgen-0.9.9/polymc-dev-installation.json`. Minecraft was not launched.

# Slavic Myths 0.9.9 — implementation in progress

Continuation 2026-10-06: fixed raised-kurgan authored entrance stones using the persisted graded surface; soil excavation preserves underground construction. All three tiers and all three approach lanes pass; all 27 required companion GameTests pass. Clean production build `-Pmod_version=0.9.9-dev` passes; JAR resource validation passes. The latest natural-coverage measurement code includes relocated eligible cells and fails unmeasured/empty applicable networks; full natural gate still pending. Evidence: `docs/verification/mob-worldgen-0.9.9/continuation-2026-10-06.json` and `docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md`. Stable PolyMC artifact is unchanged.

COMPILES: six spirit combat timelines, wildlife families/stat changes, existing bandit HP alignment, shared placement profiles, retained template foundation bounds, accepted-neighbor collision planning, and bounded persisted kurgan earthworks for thin superflat (explicitly authorized by the user). Build and verification details: docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md and work/mob-worldgen-099-*.log. Static data audit: 2676 references, zero errors. Actual accepted-placement coverage/compensation, complete mob polish/audio audit, final resource/release gates and companion runtime checks remain unfinished. This is not 0.9.9 release acceptance. Installed stable PolyMC artifact remains 0.9.7; Minecraft client has not been launched.

# Slavic Myths 0.9.7 — болото и runtime hotfix

Реализованы: семь семейств болотных мест / 21 modern template; четыре дерева и природные детали только vanilla swamp; болотник 48 HP / редкий страж 112 HP; новые swamp русалки 36 HP и водяные 72 HP; пять таблиц структурной добычи + drop болотника. Старые ID/templates и остальной контент сохранены, новой biome/ore/block семьи нет.

Xaero logout/publish null-safe с очисткой внутренних данных перед optional callbacks, пользовательские waypoint сохраняются; JEI catalyst связан с зарегистрированным ritual RecipeType; linden_door использует native 1.21.1 states/models. Другие 16 старых door model parent issues остаются backlog.

DONE по build/headless: production 0.9.7, clean build; финальные core без JEI и шесть companions — по 14 GameTests без ERROR/FATAL. 42 сухих/мокрых размещения, 96 контейнеров, 720 опор, 400 форм деревьев, семь реальных accepted starts; пять features и болотник подтверждены в modified biome settings. Проверены 10 lifecycle cases production Xaero adapter с doubles и compiled JEI recipe-type contract. Проверки/gates и hash: docs/verification/swamp-0.9.7/acceptance.json; установка: installation-receipt.json в той же папке. JAR 0.9.7 установлен в Slavic-Myths-1.21.1-Testing, семь хешей проверены, прежний 0.9.6 сохранён; старые профили/миры не изменены. Headless размещение/данные/генерация/геометрия проверяются отдельно от клиента. Manual visual/gameplay QA: NOT STARTED; следующий конкретный блок — docs/MANUAL_QA_0.9.7_SWAMP.md. Клиент не запускался.

Подробнее: docs/swamp/SWAMP_0.9.7_*.md. Ниже сохранены исторические состояния прежних версий.

# Slavic Myths 0.9.6 — курганы

DONE по build/headless: графовый план v2 и совместимость v1, 14 архетипов, альтернативные лестницы, новая архитектура/палитры, 26 блоков + 26 BlockItem, 60 рецептов, семь новых таблиц наград, 35 авторских текстур 256×256 (11 блоков и 24 иконки). Существующие ID, характеристики, старый loot и глобальная частота генерации сохранены.

PASS: clean build; 150 новых планов; 1200 legacy планов и 900 составов; по 10 GameTests без JEI и с companion-модами, включая реальное размещение/контейнеры/ловушку. Data totals: 411 items, 179 blocks, 273 recipes, 260 loot tables, 154 advancements. Production: `build/libs/slavicmyths-0.9.6.jar`; evidence: `docs/verification/kurgan-0.9.6/acceptance.json`.

Client: COMPILES. Ручная визуальная/игровая QA: NOT STARTED, клиент не запускался. Следующий конкретный блок — `docs/MANUAL_QA_0.9.6_KURGAN.md`. Известный post-port backlog: 20 старых missing door model parents; подробности в resource-check.json. Проверенный JAR 0.9.6 установлен в известный тестовый PolyMC, семь JAR сверены по hash; прежний 0.9.5 сохранён. Receipt: `docs/verification/kurgan-0.9.6/installation-receipt.json`. Остальные профили/миры не изменены.

Документация алгоритма, комнат, ID/рецептов, loot и аудита иконок: `docs/kurgan/*0.9.6.md`. Далее сохранено историческое состояние 0.9.5.

# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Слой навигации реализован по последнему
пакету V2. Предыдущий перенос 0.9.4 завершён; его отчёты сохранены как историческая база.

Core/server: DONE по headless проверкам. Client screen/HUD/Xaero: COMPILES, ручная QA не выполнена.
Build и финальные loader/data checks PASS. Minecraft-клиент не запускался.
Production: `build/libs/slavicmyths-0.9.5.jar`. Точный hash: `packaging/test-pack-lock.json`.

[Навигация, алгоритм и фактические ограничения](NAVIGATION_0.9.5.md),
[ручные проверки](MANUAL_QA_0.9.5_NAVIGATION.md), [evidence](verification/navigation-0.9.5/acceptance.json).

Core: navigation/SlavicMarker, NavigationState, SearchArea, NavigationRecords.
Серверный SavedData хранит exact assignments отдельно от public player snapshot.
Typed navigation_request проверяет ownership и права; navigation_snapshot содержит только
одного игрока. Client UI и compat/xaero изолированы; Xaero compileOnly и optional.
Реальный immutable home определяет стабильную revision зоны; discovery разрешает temporary
exact point, lifecycle очищает его. Проверки polling индексируются собственными marker IDs,
не обходят чужие quests; bounded loaded-chunk discovery не создаёт chunks.
Путевой камень по-прежнему открывает RPG menu; новые travel-механики не добавлены.

## Исторические записи до 0.9.5

# Slavic Myths 0.9.4 — текущий перенос

Launcher fix: исправлены несовпадающие official PolyMC/Maven checksums NeoForge installer/universal локальным patch нового профиля. Реальный ForgeWrapper detector PASS; ручной повторный запуск ещё не проверен.

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Все production sources компилируются; clean build, headless logic/resource/loader gates A–F PASS. Отдельный PolyMC Slavic-Myths-1.21.1-Testing установлен с семью проверенными JAR. Старый 1.16.5 профиль/миры сохранены. Клиент не запускался; игровая QA ещё не выполнена.

Фактическое состояние и журналы: [PORT_STATUS_0.9.4.md](port/PORT_STATUS_0.9.4.md). Исторические записи ниже описывают предыдущие этапы и версии, а не подтверждение работоспособности текущего порта.

Current port: provider-aware SavedData (all seven); persistent ItemStack data components with legacy custom_data/DFU migration; four typed payloads with server validation and client-only handlers; target event registration and GuiGraphics; data-driven features/spawns/structure sets with codecs and bounded candidate exclusions. Test sources/resources attach only to the opt-in GameTest run and never enter production JAR. Generic HP overlay removed under the package instruction; unique boss/UI mechanics retained.

# 0.9.4 — текущая архитектура переноса

Единственный активный toolchain — NeoForge 1.21.1 / Java 21, без dual-loader.
Entrypoint принимает IEventBus и ModContainer; конфигурация ModConfigSpec.
ModSounds использует DeferredHolder/DeferredRegister и variable-range SoundEvent.
BurialRecords, HuntRecords, PoolIndex используют SavedData.Factory, CompoundTag и
сохранённые старые имена файлов/ключей; сериализация не меняет игровую логику.
Некоторые runtime callers пока используют 1.16 API: полный port НЕ завершён.

portCore — тестовая компиляция выбранных реальных независимых production-файлов
на target dependencies; из main ничего не исключается, fake/stub API отсутствуют.
verifyKurgan/verifyHunt используют эти же production-классы; полный mod boot
и stateful ItemStack components этим не проверяются. Детали — port/PORT_STATUS_0.9.4.md.

port_resources_1211.py выполняет явные schema conversions, переносит singular data
folders; текстуры/звуки/structure NBT байт-в-байт сохраняются. Resource verifier
сравнивает нормализованные baseline data и бинарные SHA256; decoder/runtime QA pending.
Исторические генераторы assets ещё требуют адаптации: не запускать их как новый datagen.

---

# Исправление placement 0.9.3

YagaPlacement содержит production preflight/commit на IWorld, а не копию алгоритма
для тестов. Prepare проверяет loaded bounds до первого чтения, terrain Y>=0,
контейнеры/защиту/жидкости, полный desired/old snapshot и опоры почвы.
Commit использует flags=2|16; лишь затем вычисляет connected states всех блоков,
отклоняет неподдерживаемые/потерянные блоки, применяет формы и вызывает spawn.
Только успешный spawn позволяет YagaHut записать placed/anchor/canonical UUID.
Отказ блока, опоры, спавна или исключение откатывают snapshot с теми же flags18.

YagaPlacementHeadless использует vanilla bootstrap и локальные vanilla block tags,
изолированный IWorld fixture и этот же prepare/commit. Никакого server/client main.
NBT NPC теперь содержит YagaEggSummoned; SpawnReason.SPAWN_EGG включает сервисный
доступ standalone NPC, но не меняет world home/UUID. YagaMenu допускает egg NPC
без house intro; BabaYaga не выдаёт им hut advancement и не меняет house intro.
Все прежние серверные проверки и общая личная прогрессия остаются.

---

# 0.9.3 — избушка и сервисы Яги

YagaData — WorldSavedData `slavicmyths_yaga` в Overworld: anchor/placed/NPC UUID,
исключённые кандидаты, игроки по UUID (intro/stage/active/contract/cooldown/warnings).
Активный UUID резервируется и при выгрузке чанка. Три стадии завершаются только
по совпадению принятого поручения; повторная сдача не повышает стадию.

YagaHutPlan — чистый детерминированный blueprint и максимум 64 лесных кандидата.
YagaHut использует noise biome source без чтения незагруженных чанков; затем только
локальное PlayerTickEvent раз в 80 тиков до размещения. `hasChunksAt` предшествует
рельефу/блокам; height scan ≤48. Проверка полного plan до записи, snapshot+rollback,
после размещения восстанавливаются neighbor shapes (дверь, ограда, стекло, лестницы),
дневные грибы получают podzol. Нет force-load/глобального world-tick обхода. YagaLegBlock даёт 7 форм конечностей;
геометрия/чешуйки native, BlockItem не регистрируется.

BabaYaga — CreatureEntity, не Witch/boss; простые home/look goals, 40 HP, отказ вместо
смерти, жесты synchronized data. Canonical UUID проверяется на сервере.
YagaMenu зарегистрирован до регистрации RpgMenu.MENUS; NetworkHooks открывает меню,
все действия идут стандартным vanilla clickMenuButton, без новой сети.
Server checks: world, NPC, alive, distance≤8, intro/refusal, stage/active, полные inputs,
capacity/тип output. Персональная Inventory(5), возврат всего на removed.

YagaServices — единый конечный каталог MAIN/CONTRACTS/EXCHANGES/BREWS,
используемый сервером, экраном и изолированным необязательным JeiYaga.
YagaScreen/BabaYagaModel/Renderer загружаются только ClientSetup (Dist.CLIENT).
Статус лишь на Talk; 2 независимых modal popups, без постоянных справок;
награда Stranger скрывается; dynamic controls обновляются по смене menu state.

YagaUtilityItem: whitelist ordinary effect removal, никакого blanket clearEffects;
FlightRecovery — 60-tick repack delay у существующих flight items, с мазью 30,
6000-tick buff, без изменения cargo/motion/registry IDs. До этой итерации такого
таймера в FlyingVessel не было; README фиксирует фактическое расширение.
10 новых ModSounds завершаются vanilla event fallbacks с собственными субтитрами.

Scoped assets: tools/yaga_093.py. tools/yaga_review_093.py читает production Java
geometry и headless экспорт настоящего hut plan. Общий генератор подключает scoped
слой в конце; прежняя пользовательская weapon art в этой итерации не перезаписана.
Headless tests и ресурсные проверки не подтверждают игровой runtime.


---

# 0.9.2 — сохранённые региональные встречи

HuntRecords остаётся прежним WorldSavedData slavicmyths_hunts и сохраняет Hunts;
BossAnchors/BossRegions добавлены отдельными полями. BossKind не расширяет HuntTarget.
BossRules: seed, кандидаты, фазы и cooldown; BossAnchors: ChunkEvent.Load и локальная
40-tick активация, без force-load. ACTIVE UUID сохраняется независимо от наличия сущности
в текущей памяти; finishBoss принимает только matching UUID и реальную смерть.
WorldBoss использует базовый HuntMob для spirit/save инфраструктуры, но заменяет goals,
атрибуты, собственный owner/encounter, bar и scheduler. Его owner не попадает в old horn lifecycle.
BossRitualItem использует vanilla 60-tick held use, повторную проверку и расход после addFreshEntity.
BossEffects: общая регистрация POTIONS, PotionApplicable с ограниченным recursion guard,
Curios-only Napор и transient UUID-модификатор. BossStone — физический ограниченный projectile.
DashFacing используется старыми и новыми наземными/воздушными атаками.
WorldBossGeometry генерируется из того же manifest, что и offline геометрические ракурсы;
WorldBossModel/Renderer/BossAccessoryRenderer загружаются только клиентской регистрацией.
Обычный крафт обеспечивает JEI без отдельной категории или ложных рецептов добычи.

---

# 0.9.1 — расширение существующей охоты

HuntTarget задаёт стабильные строковые IDs четырёх целей; HuntRecords по-прежнему
хранит по одной встрече owner→UUID/anchor/status/ready. Новое Kind дополняет
старое Ovinnik; чтение boolean 0.9.0 мигрирует, ACTIVE при выгрузке сохраняется.
HuntMob остаётся владельцем lifecycle/баров/Home/guardian/NBT, старые два AI
не заменяются. ElementHuntMob добавляет два собственных move sets на тех же
lifecycle hooks, synced MOVE/SINCE и абсолютные сохранённые cooldown.

Змей: проверка loaded collision перед физическим move, bounded orbit/высота,
телеграфы/восстановления, локальные временные traces. Сервер не ставит огонь и
не разрушает блоки. SerpentProjection — технические 1-HP визуальные копии,
TTL70/absolute expiry, parent UUID, ноль damage/XP/loot/encounter, не hunt target.
Максимум два при cast, повтор только после expiry. EmberClump переиспользован
для редкого fallback; параметр урона определяется владельцем.

Подвей использует существующую наземную навигацию. VORTEX_ACTIVE выполняет
локальный pull с тремя силовыми ступенями, velocity cap .45 и center-hit CD30;
снаряды проверяются раз в 2 тика только в этом состоянии. Набор UUID исключает
повторное отклонение одной стрелы; только обычные arrows/snowballs/eggs,
жемчуг и специальные типы не затрагиваются, LOS обязателен. После60 active
и на выходе guard отсутствует. Во всех игроковых атаках учитываются LOS и
creative/spectator. Рывок физический и ограничен7, без телепорта.

HuntSpawns расширяет существующие placements/BiomeLoading, не добавляет
player/world tick spawning. Horn ищет≤40 loaded candidate positions, безопасный
объём, border, exclusion кургана; один owner/6000tick gate общий для всех целей.
HuntTags используется детектором и утилитой; PouchMenu меняется только данными
hunt_trophies и сохраняет9слотов/slot-swap ограничения.

StarCharmEffects — серверное PlayerTickEND, equipped Curios necklace lookup,
fallDistance/downward/flight guards, Slow Falling100, fallDistance=0; player
absolute Overworld clock ready1200 в persistent data, Clone сохраняет ready.
WindKnotItem — только ПКМ, MobEntity local radius4/LOS/hostility, CD500;
без урона/PvP/снарядов, maxhealth/target helper снижает elite/boss толчок.
BottledElements использует native inventory_changed AND двух предметов.

Client-only ElementModel/ElementRenderer/HuntAccessoryRenderer: свои512atlases,
блоковая геометрия, synced state animations, emissive только eyes/mouth/seams.
Новые зависимости отсутствуют. tools/hunt_091.py — scoped native art/data;
Hunt I tag writer additive, полный pipeline запускает II после I.
verifyHunt проверяет pure timing/math/NBT migration/cycle; verify_hunt_091.py —
exact recipes/loot/tags/resources/protected art/reproducibility. Эти проверки
не проверяют реальный бой/GUI/перезаход/сеть/MSPT: Minecraft запусков0.

---

# 0.8.5 — лабиринты и состояние экземпляра

KurganPlan — чистый Java-план с ограничением 12 попыток. Три диапазона этажей и
помещений, сетка с разными расстояниями между осями, случайное связное дерево,
ветви/тупики/петли, отдельная нижняя гробница. Корневой южный порт зарезервирован
под вход/лестницу: обычный коридор не может затереть ступени. План проверяет пары
room/room, room/connector, connector/connector, высоты и связность. Модульные роли
включают длины, повороты, развилки, тупики, лестницы и shortcuts. Основной путь
маркируется; у Великого заканчивается у намеренно запечатанного входа.

KurganPlanNbt сохраняет фактический план Version=1, комнаты/соединения/палитры,
флаги critical, позиции, границы печати и ниш. При загрузке нет повторного RNG.
KurganDungeonPiece — новый тип kurgan_dungeon; старый kurgan_shell не изменён.
Placement ограничен текущим чанком; оболочка, комнаты, проходы, затем декор.
Две клетки новой колоды лежат в одном чанке при любом debug-anchor.
Новая оболочка использует только vanilla; естественные Structure ID kurgan_small,
kurgan_warrior, kurgan_great и их сетка 64/24, salt остаются прежними.

BurialRecords расширен, прежний Opened сохранён. Instances хранят UUID, origin,
полный план, disturbance, fired sources, thresholds, sealOpened; dimension задаёт
WorldSavedData-хранилище. Индекс chunk -> instance обновляется при register/load.
Worker worldgen только ставит запись экземпляра в server executor; SavedData
меняется на серверном потоке. Обработчик placement синхронизирован на piece.

Источники `loot:roomId` и `zone:roomId` учитываются по одному разу. Домашние колоды
не имеют natural UUID. Никакого HUD/сканирования мира для disturbance нет.
BurialRecords в Overworld отдельно хранит player UUID -> source Great UUID set.
KurganCurse: один Effect, amplifier 0 = -10%, 1 = -15%, MULTIPLY_TOTAL к movement
и attack. Временная длительность 36000 ticks. Persistent state восстанавливается
на login/respawn и раз в 20 тиков игрока (один lookup, не обход сущностей/чанков).
clear удаляет данные и эффект; bossDefeated(player, sourceUUID) очищает конкретный
источник, сохраняя другие. KurganDungeonPiece.openSeal(world,id) открывает известный
проход только при загруженных чанках и записывает sealOpened; сейчас не вызывается
survival-механиками. Пустых классов будущего босса нет.

Generate — OP2, четыре ограниченные кандидатуры в 192 блоках, предварительная
проверка всего footprint, worldborder, terrain, tile entities и искусственных блоков.
Только затем размещение. Это dev-инструмент тестового мира, без автотелепорта.
Custom locate включает сохранённые debug-экземпляры; vanilla — естественные starts.

Ресурсы: tools/kurgan_085.py, 64px собственная кладка/декор/иконка, прежние loot
находки 0.8.4. verifyKurgan — отдельный JVM sourceSet tools/kurgan-tests, в JAR
не входит и не загружает Minecraft-клиент/сервер. Headless checks не подтверждают
игровую производительность, визуал, сетевую синхронизацию или фактический respawn.

---

# 0.8.4 — материальная основа курганов

kurgan/BurialCoffinBlock: две клетки, FACING/PART, tile только FOOT; HEAD направлен
по FACING. Placement проверяет вторую клетку; предмет не переносит Natural/NBT.
Пять слотов LockableLootTileEntity, стандартный vanilla loot filling, отдельный Container.
Любая половина удаляет пару; loot блока только FOOT, contents выгружаются из FOOT.
Creative HEAD убирает FOOT без block loot. Поршни блокированы, свойства facing
сверяются при удалении пары. Player-placed tile очищает принадлежность кургану.
BurialCoffinTile: inventory/Natural UUID/remains/opened в NBT; packet передаёт только
remains/opening. Viewers — UUID-set; menu close и проверка текущего меню раз в секунду
закрывают крышку. Client-only renderer, плавная крышка, четыре статических вида останков.

BurialRecords: только Set<barrow UUID> opened, запись по событию первого открытия
естественной колоды. Никаких ticks, проклятий, mobs, глобального поиска или истории
домашних колод. UUID один на structure piece. Полное состояние dungeon отложено.

BurialWeapon использует SwordItem/существующую логику толчка копья. Найденное оружие
слабее восстановленного. Чекан использует MaceItem и существующий CombatEquipment
armor pressure (.22/.30), без bypassArmor. Restoration — armorer_shapeless с древним
оружием, железом, кожей, палкой; отдельного стола нет. Аксессуары — FolkAccessoryItem,
Curios necklace/charm. Лунница: -5% creature damage ночью; гривна -4% armor-sensitive;
grave_ward: -9% только от LivingEntity с CreatureAttribute.UNDEAD (включая его стрелы).
Существующий warding_charm — ритуальный материал, не anti-undead accessory.

DarkenedWood: девять блоков в существующих registries/tab, не новая порода дерева.
Axe strip сохраняет AXIS; vanilla state/model schemas, wood tags/recipes, flammability.
SmokeAging: event-discovered jobs в WorldSavedData; до 512 на dimension. Placement,
interaction и loaded-chunk tile list обнаруживают только вертикальную колонку костра.
Chunk-load позиции обрабатываются на следующем серверном периоде, не вызывая рекурсивной
загрузки чанка из callback. Раз в 20 тиков обходится только ограниченная очередь jobs;
до пяти клеток на колонку, hasChunkAt, без глобального scanner. 1200–4800 тиков
фактической обработки; пауза при погасшем/перекрытом дыме и при chunk unload.
Break target/fire удаляет job; target identity проверяется. AXIS сохраняется.
Лимит 512 — защитное ограничение: последующие новые задания не принимаются до
освобождения места. /setblock не является основным способом регистрации процесса;
для таких случаев взаимодействовать с костром или перезагрузить его чанк.

KurganStructures: три стандартных Structure/StructurePiece в существующем
SwampStructures DeferredRegister. Общая сетка 64/24 чанка, salt 841973; один
псевдослучайный выбор 60/30/10 на candidate. Дополнительное исключение соседних
Great candidate regions делает Great реже номинальных 10%. Биомы vanilla plains и
birch_forest. Наличие biome и terrain не гарантирует попадание каждого grid candidate.
KurganStructure проверяет сухие ground heights с шагом 2, перепад ≤4/6, height 63–140,
близкие кандидаты крупных структур; без загрузки чанков при preflight.
KurganShape: радиусы 14/24/40, высоты 10/18/30, плавный профиль (1-r²)²; основание
сопрягается с локальным terrain, не выравнивает прямоугольник. Piece пишет только свой
chunk clip; обе клетки колоды геометрически помещаются в один чанк. Есть короткая
входная ниша, а не dungeon. Деревья используют TreeShape pine/linden с фиксированным
seed, чтобы порядок чанков не менял геометрию. BurialPlaced сериализуется.

KurganCommands используют существующий namespace/стандартные structure starts;
permission 2, bounded ring search (до 10 regions), biome/type prefilter. Next исключает
радиус 192 от игрока и последний возвращённый экземпляр. Teleport ищет безопасную
поверхность около входа, исключает листья/брёвна/жидкости/опасный грунт.

Ресурсы: tools/burial_084.py, последний слой create_resources.py. Своё pixel-grain
состаривание, native cuboid silhouettes, две собственные mono OGG. Vanilla скопированы
только JSON-схемы древесины, не перекрашенные изображения. verify_burial_084.py
проверяет воспроизводимость, таблицы, recipes, модели/локализацию. Manifest добавляет
динамические darkened registrations в общий verifier. Его запрет smoke test classes
уточнён до реального package org/slavicmyths/smoke; production SmokeAging разрешён.
Java8 KurganShapeTest проверяет форму, границы и 100000 выборов кандидата.
Это не проверка живого worldgen, GUI/сохранений, звука или MSPT.

# 0.8.2 — Соловей-разбойник

NightingaleEntity — отдельный CreatureEntity, 250 HP, bbox 1.1 × 2.35.
Серверная state machine: NOTICE → melee/retreat → inhale/release/recovery;
короткий, тяжёлый удар, толчок, обычный/разрушительный свист и anti-contact radial.
Синхронизируются action, время начала, drawn и сила hurt; модель интерполирует позы.
HP-пороги 150/75 меняют паузы и частоту комбинаций. На свисте направление фиксировано.
С помоста босс физически идёт к открытому южному краю, спускается под гравитацией;
нет телепортации. При потере цели возвращается к home, здоровье не восстанавливает.

WindAttack: только на выпуске, до 64 living targets. WindGeometry — чистая Java
геометрия конуса и точный voxel traversal трёх лучей к корпусу (до 128 шагов каждый).
Полные collision blocks защищают; листва/забор/неполная мебель не считаются стеной.
Только загруженные чанки; незагруженный блок считается укрытием. Эффект ослабевает
с расстоянием и прикрытием. Terrain — 9 дорожек по максимум 20 клеток, максимум
24 разрушения из whistle_fragile, исключение tile entities и Forge mobGriefing.
Частицы — 9 cloud, до 6 dust и 2 leaf за один шаг, 12 шагов обычной волны.
Это статические ограничения работы, не измеренный MSPT.

StrongholdRecords получил два независимых persisted флага на UUID лагеря:
NightingaleSpawned/NightingaleDead. CLEARED обычных защитников не блокирует босса.
LargeCampPiece обрабатывает ровно один marker nightingale и deterministic entity UUID;
ставит spawned только после успешного addFreshEntity. Старая структура обновляется
только при генерации ещё не созданного чанка: нет ретрогенерации загруженных дворов.
Существующий шаблон двора дополнен мебелью, сундуком и маркером; остальные части
лагеря и штатный roster сохранены. tp_nightingale использует существующий поиск
structure starts, выбирает arrival из конкретного LargeCampPiece, затем safe-ground.

После успешного super.die (включая Forge cancellation) фиксируется победа и удаляется
boss bar. Обычный death loot отложен до 55-го тика падения; затем вызывается vanilla
super.dropAllDeathLoot с Forge loot hooks/XP и сущность удаляется. CollapseTicks и
lootReleased сохраняются в NBT. Camp dead записывается сразу при смертельном ударе.
Участники — живые survival игроки в радиусе боя и атакующие; при смерти присутствующим
в том же мире не дальше 64 блоков выдаётся победа. Книга использует bit 27 существующей
маски (28 entries из 32). Свист атакует область, полосы/лут управляются сервером.

NightingaleWhistle: hold 24 тика, 9 блоков, cooldown 600 тиков, малый damage,
отталкивание, тушение огня; terrain destruction выключен. Кинжал: 7 damage,
2 атаки/сек, 700 durability; hold ≥18, release, 3.5 блока, cooldown 140, износ 2.
Player artifacts пропускают боссов (max health ≥100 либо !canChangeDimensions).
Bandit horn использует ту же простую use-механику только для звука, без урона.

NightingaleModel/Renderer — самостоятельная геометрия и UV 256²; BreathingChest
масштабируется на вдохе, руки сегментированы, волосы/борода объёмные. Ножны остаются
на поясе, рукоять скрывается после извлечения. Death renderer отключает vanilla flip.
Ресурсы: nightingale_082.py — модели/UV/локализация/loot/recipe/13 audio events,
15 mono OGG (hurt ×3). Звуки собственного синтеза; пространственные, не UI.
Генератор нормализует Ogg serial/CRC для байтовой воспроизводимости. В общем pipeline
он выполняется после stronghold_081.py. Последний владеет изменённым шаблоном двора.

Тесты: tools/tests/WindGeometryTest.java (JDK8, без Minecraft),
verify_nightingale_082.py (воспроизводимость/двор/лут/tag/models/audio wiring),
verify_stronghold_081.py (roster/шаблоны/мебель), verify_resources.py (production JAR).
Minecraft/manual QA не выполнялся.

# Архитектура

## 0.8.1 — III уровень и мебель

`furniture/Furniture` добавляет 21 блок/22 предмета в существующие registries/tab.
FurnitureBlock — facing и составные collision shapes; обычная мебель без TileEntity.
WardrobeBlock — lower/upper, два блока и drop только lower. Shelf требует опору.
SeatEntity — transient MISC, noSave/noSummon, без визуального renderer; удаляется
после высадки или потери блока. Используется для игрока, не сохраняет сидящих NPC.
RackTile — один ItemStack, серверный обмен, NBT/update packet и сброс предмета при
разрушении; FurnitureClient отображает оружие стандартным ItemRenderer без tick.
Шкаф/тумба/ящик декоративные, собственного inventory не имеют.

`LargeCampStructure`/`LargeCampPiece` — отдельные стандартные StructureStart и
TemplateStructurePiece, регистрация через CampStructures. Сетка 192/64 чанка,
случайное смещение регионов, salt 813797 (3072 блока размер региона, не гарантия
реальных интервалов). Vanilla plains/forest/taiga; hill выбирается в приподнятом биоме.
Проверяются сухой грунт, высота 61–135, локальные перепады ≤3 у зданий/дворов,
≤8/12 у sparse perimeter; общий перепад ≤9/14. Отказ до размещения при нарушении.
Проверка соседних vanilla крупных структур/болотных кандидатов без загрузки чанков.
Малый/средний лагеря уступают large; их roster/Атаман не меняются.

23 NBT в structures/stronghold; forest/plains/hill меняют ломаный периметр и смещения
построек, варианты казармы/кузницы/склада выбираются отдельно. Sparse perimeter
следует высоте каждой колонки; здания имеют локальную основу и входные ступени.
Дворы допускают только малый перепад и локальные грунтовые опоры; общего квадрата
платформы 80×80 нет. Тайник — 35%, под домом или складом, привязан к высоте родителя.
Шаблоны и loot воспроизводятся tools/stronghold_081.py; мебель — furniture_081.py.

StrongholdRecords — отдельный WorldSavedData без глобального tick handler:
UUID→spawned/dead long masks, CLEARED, пять bell positions/state/deadlines.
26 защитников, slots 0/1 — Атаманы, вспомогательные horse/prisoner slots ≥40.
Повторные спавны блокируются маской и deterministic entity UUID; успех addFreshEntity
проверяется перед фиксацией. Зачистка: два Атамана и ≥18 погибших защитников;
достижение получает игрок последнего нужного убийства. CLEARED сохраняется,
тревога и поздние marker spawns отключены; ownership/boss-state сейчас не добавлялись.

StrongholdGoal работает только для campTotal=26. Проверка раз в 20 тиков на загруженном
NPC, без поиска всей структуры: короткое наблюдение (50 тиков), путь к известному
колоколу, локальный ALERT. Проверенные неразрушенные соседние сигналы передают
тревогу по одному ребру за 100 тиков. Незагруженные чанки не подгружаются; нет передачи
player UUID/координат дальним NPC, они ищут цель обычным локальным goal.
После 1200 тиков без нового звонка тревога затихает. При уничтожении сигнала передача
через него прекращается; уже поднятая локальная тревога не отменяется мгновенно.
Patrol/guard/training/smith используют home/duty; кузнец имеет видимый фартук.
NPC открывают деревянные двери стандартным OpenDoorGoal; атаки остаются 0.8.0.

Книга: bit 26 для black_feather. Только lore, без entity/boss bar/свиста Соловья.
Проверки 0.8.1: data/NBT/roster/headroom/ladder support, без игрового запуска.

## 0.8.0.1 — древесина и леса

`wood/Woodlands` регистрирует четыре упорядоченных семейства через существующие
ModBlocks/ModItems, в существующей Creative Tab. Каждый набор: log/wood/stripped
log/stripped wood, planks/leaves/sapling, stairs/slab/fence/gate/door/trapdoor,
pressure plate/button, standing/wall sign. 69 блоков и 66 предметов всего.
`TimberLog.getToolModifiedState` использует Forge 36.2.42 API и сохраняет AXIS.
Горение задаётся Forge block overrides; `WoodlandFuel` — карта Item→burnTime,
читаемая только событием печи, включая gates/signs. Vanilla tags дополнены без replace.

`TreeShape` — чистая геометрия без доступа к миру: отдельные linden/rowan/willow/pine
алгоритмы. Ветви face-connected, нерегулярные листовые массы; bounded BFS вычисляет
vanilla distance≤6 и исключает неподдержанные листья. `WoodlandFeature.grow` сначала
проверяет ВСЕ клетки и грунт, затем размещает: воздух, собственный саженец и обычная
мягкая растительность разрешены; чужие деревья, постройки, жидкости и грунт не заменяются.
`WoodlandGrower` переопределяет Tree.growTree: общий путь с worldgen, без vanilla oak/spruce
генератора. Natural feature использует ground heightmap; у ивы максимум 9×9×3 проверок
воды. Варианты выбираются Random, история каждого дерева не сохраняется.

Worldgen — standard configured features + BiomeLoadingEvent, только vanilla Overworld.
Шанс попытки на чанк: forest/dark forest липа 1/4, рябина 1/8, сосна 1/24,
ива 1/12; taiga сосна 1/2 и рябина 1/12; plains липа 1/24 и рябина 1/12;
swamp ива 1/2 и рябина 1/12; river ива 1/4 и рябина 1/24.
Отбраковка занятых крон и неподходящего грунта снижает реальную частоту; она не измерена.
Vanilla features не удаляются, forest manager/chunk polling отсутствуют.

`RowanLeaves` сохраняет berries boolean вместе с vanilla leaf state: серверный ПКМ
сначала снимает berries, затем выдаёт 1–2 ягоды. Random tick восстанавливает урожай
с шансом 1/12 при distance<7 и свете≥9. Leaf loot не сохраняет ягодное состояние:
обычная добыча даёт саженцы/палки, ножницы/Silk Touch — обычные листья, без двойной награды.
`HangingLeaves`: cross model, без collision/TileEntity/анимации; цепочки 1–3 блока,
поддержка верхним блоком ивы/подвесом, scheduled tick на потерю опоры и random tick страховка.

Таблички: отдельный TileEntityType с восемью допустимыми блоками, SignTileEntity
переопределяет getType для сохранения собственного ID; стандартный клиентский renderer,
четыре WoodType/atlas textures 64×32. Client-only setup изолирован в WoodlandClient.
Книга: новый knowledge bit 25, открываемый woodlands advancement.

`tools/woodlands_0801.py` — последний слой create_resources.py; собственные рисунки,
vanilla 1.16.5 blockstate/model/loot/recipe schemas из установленного client-extra.jar
(не импортирует vanilla текстуры). `verify_woodlands_0801.py` проверяет ресурсы и запускает
только pure Java TreeShapeCheck (1024 формы), вне production JAR. Игровых запусков 0.

## 0.8.0 — бой, оружейник, лагеря

`armorer/`: custom IRecipe serializers shaped/shapeless, сетка 4×4, серверный
результат и расход/остатки ингредиентов; закрытие возвращает входные предметы.
Shift-click результата требует места под полный выход. JEI изолирован в compat.
`combat/`: MaceItem имеет собственные атрибуты без sword sweep; бронебойность
обрабатывается LivingHurt. Кистень: основная рука, заряд 18 тиков, дальность 4 блока,
перезарядка 40 тиков, серверная проверка луча. Пластинчатый нагрудник: скорость −5%.
`bandit/BanditEntity`: стандартные goals и ограниченные cooldown, melee windup/
recovery, лук с дистанцией 8–14 блоков, щит, тяжёлая атака и команда Атамана.
Команда ищет союзников локально только по событию, не глобально каждый тик.
Steve-like BipedModel, HeldItemLayer, 20 собственных текстур; семь original OGG.

CampStructures: стандартные StructureStart/TemplateStructurePiece, vanilla
plains/forest/taiga, авторские 10 NBT. Малый лагерь 25×25, средний 43×38.
Сетки spacing/separation: 48/16 и 64/24 чанка (768/1024 блока между регионами).
Это параметры кандидатов, не измеренные расстояния между фактическими лагерями:
биом, рельеф и резерв соседних структур могут отбраковать кандидата.
Высоты отдельных частей проверяются до размещения, глобального выравнивания нет.
CampRecords WorldSavedData хранит UUID и битовые маски состава/смертей; маркеры
задают ровно 5/9 жителей, у среднего ровно один Атаман. Нет периодического respawn.
Поиск команд ограничен семью кольцами, подтверждает STRUCTURE_STARTS; способен
генерировать эту стадию чанка как vanilla locate, не запускается фоновым polling.
HUD читает crosshair либо ID из S2C принятого урона, timeout 100 тиков; world scan нет.
Клиентские экран/рендерер/HUD отделены от common/server.

Ресурсы последовательно генерируются tools/create_resources.py; финальный слой
0.8.0 — tools/bandits_080.py. Собственные OGG имеют стабильные serial/CRC.
Существующие registry ID, включая mace/perunite_mace, сохранены.
Проверки структуры данных не подтверждают игровую синхронизацию/сохранение/баланс.

## 0.7.3 — структуры топей

`swamp/SwampStructures` регистрирует семь Structure<NoFeatureConfig> через Forge
DeferredRegister, configured structures и один тип TemplateStructurePiece. Явная
стадия SURFACE_STRUCTURES; только vanilla swamp, underwater_ruins также river.
При загрузке Overworld копируется structureConfig с сохранением чужих параметров;
добавляются только отсутствующие настройки мода. Единственный mapped reflection
доступ к `DimensionStructuresSettings.field_236193_d_` нужен из-за immutable codec maps.
API проверен по локальному Forge 36.2.42 mapped JAR; client/common разделение сохранено.

Spacing/separation в чанках: изба 24/12, поселение 52/26, гать 18/9,
святилище 48/24, стан 30/15, руины 22/11, малые POI 10/5. Это параметры кандидатов,
а не измеренные частоты: biome/terrain/collision rejection дополнительно сокращают их.
Перед стартом bounded noise-only проверка соседних кандидатов крупных vanilla и
приоритетных custom structures. Порядок приоритетов исключает взаимное наложение
наших разных типов независимо от очередности генерации. Это консервативный резерв,
а не глобальный анализ фактических построек; совместимость с произвольными чужими
структурами/изменёнными datapack сетками не гарантируется.

`SwampStructure.Start` формирует конечный список частей, загружает авторские NBT и
проверяет footprint через base-height queries без загрузки чанков. Все части валидируются
до принятия старта. Нет выравнивания всего поселения; отдельные основания и пути имеют
свои высоты. Подводные руины ограничены глубиной, стан требует сушу у навеса и воду у
конца причала. `SwampPiece` сохраняет имя/позицию/seed/EncounterProcessed, размещает
template с vanilla chunk bounding box, добавляет максимум восемь блоков опоры вниз.
Пути — sparse-шаблон, локальная высота каждой колонки, деревянный настил вместо грунта
над водой. Отсутствующие в шаблоне блоки не заменяются воздухом.

Loot markers выставляют vanilla barrel с lazy loot table и стабильным seed.
Encounter marker имеет одну сохранённую попытку на часть: Кикимора 10%, водные духи
12,5%, peaceful и коллизии исключены. Нет спавнеров, повторных попыток или тик-менеджера.
Старые DeepPoolFeature/малые постройки WaterFeature пропускают чанки, уже содержащие
references новых структур, без загрузки соседних чанков. Растительность сохраняется.

`SwampCommands` — permission 2, сервер, только явный вызов. Swamp ищет noise biomes
в радиусе до 8192, пропускает 1280 вокруг игрока уже в болоте и проверяет промежуточный
неболотный участок. Для безопасной посадки загружается только ближайшая область цели.
Structure search ограничен 12 кольцами сетки, проверяет настоящие STRUCTURE_STARTS;
возвращает координаты без размещения шаблона командой. Next пропускает ближайший старт.

Discovery использует vanilla location trigger/feature predicate; новый PlayerTickEvent
не добавлен. Новые knowledge bits 14–19 продолжают прежний int S2C без смены старых битов.
Источник ресурсов — последний слой `tools/swamp_073.py` в `create_resources.py`.
`verify_swamp_073.py` отдельно декодирует NBT, проверяет границы, маркеры, loot references,
варианты, повторяемость и упакованные ресурсы. Это не визуальный или игровой тест.

## 0.6.5 — wildlife

Шесть registry ID используют один серверный `WildlifeEntity` с фиксированным видом,
синхронизированным locomotion state и разными goal-наборами/attributes. Клиентские
`WildlifeModel` и renderer строят отдельные пропорции медведя, медвежонка, волка,
кабана, оленя и оленихи; gait, run, swim и attack pose зависят от состояния.
`WildlifeSpawns` использует стандартные placement/biome spawn lists без поиска мира
в тике. `generate_065.py` — последний ресурсный слой: текстуры, loot, food, recipes,
advancements, звуковые event-ссылки и локализация.

## 0.6.0 — RPG

`PathData` использует `PlayerPersisted/SlavicPaths`; Main/Secondary по умолчанию
пусты, навыки — ranks. `RpgNbt` меняет только собственные namespaces в NBT.
`RpgMenu` валидирует блок, расстояние, XP, материалы, путь и prerequisites на сервере.
Операции над предметом выполняются в первом слоте; при закрытии все слоты возвращаются.
`Runes` определяет категории/лимиты через tags и vanilla типы, не через один класс.
Proc cooldowns принадлежат игроку, что исключает сброс сменой оружия. Лук сохраняет
снимок использованного предмета в projectile NBT. Forge 36 не имеет ShieldBlockEvent:
щит подтверждается приростом vanilla blocked-damage stat после завершения hurt().
Обработчики событий проверяют используемый предмет; tick-проверка только завершает
помеченный блок/разряд, без обходов инвентаря или всех навыков. Клиентский ввод G
не передаёт стоимость/силу; сервер проверяет изученную выбранную способность и откат.
`generate_060.py` — последний слой генератора: native JSON voxel-модели с vanilla
материалами, рецепты, tags, достижения и RU/EN. Новых raster masters не требуется.

## 0.5.1 — представление духов

Последний этап `create_resources.py` — `generate_051.py`: родительские ModelRenderer,
пиксельные атласы и оригинальные mono OGG только для Полудницы, Полевика,
Банника и Кикиморы. Звуки привязаны к существующим действиям на сервере;
редкие ambient/notice имеют интервалы. Геометрия Полудницы следует прежним
20 тикам формы; высота hitbox согласована с моделью. AI и игровые параметры
сохранены. Старые генераторы выполняются до 0.5.1, чтобы не откатывать ресурсы.

## 0.4.6 — ресурсы

`tools/generate_046.py` вызывается последним из `create_resources.py` и заменяет
только выбранные пиксельные textures и JSON models. Физические блоки используют
несколько cuboid elements и отдельные текстуры граней; Java renderer не добавлен.
Registry ID и механики 0.4.5 сохранены.


## Добавление 0.4.5

`create_resources.py` последовательно вызывает исторические генераторы 0.2/0.4/0.4.1
и последним `generate_045.py`, который задаёт актуальные уникальные текстуры,
модели, локализации и data-ресурсы. `visual_audit.py` проверяет модели и PNG и
строит контактный лист. Новый контент остаётся в прежних DeferredRegister и tab.
Amber встречается в loot капища, без новой руды и ретрогенерации.

Ритуалы имеют шесть фиксированных определений в `Rituals`; altar хранит номер и шаг
в NBT, старые сохранения без номера открываются как первый обряд. JEI изолирован.
Серебряная уязвимость и лесные духи задаются entity type tags. Урон и защита
обрабатываются событиями; импульс посоха — только по right-click, с ограниченной
областью и cooldown. Щиты используют vanilla ShieldItem и плоские item models.


## Добавление 0.4.1

Новые предметы и блоки зарегистрированы в прежних DeferredRegister без изменения ID.
Серебряная руда и четыре BushBlock-травы используют ConfiguredFeature в
BiomeLoadingEvent; нет поиска чанков в игровом тике. Снаряжение использует vanilla
SwordItem/ToolItem/ArmorItem и собственные ограниченные tier/material. Данные,
текстуры и рецепты 0.4.1 воспроизводит `tools/generate_041.py`, вызываемый перед 0.4.5
из `create_resources.py`. AltarTileEntity хранит вид обряда и шаг в NBT;
старое сохранение без RitualKind читается как первый обряд. Три определения в Rituals
показаны необязательным JEI. Громовой топор обрабатывает удар на сервере с cooldown;
защита оберега и полного комплекта применяется по событию урона без tick polling.

## Актуальное расширение 0.4.0

Разделы ниже описывают историю 0.1/0.2, а не полный текущий состав.

- Существующие реестры и ID сохранены; добавлены один TileEntityType и один Feature.
- DomovoyEntity: Map UUID → reputation/attacks/nextGift только для взаимодействовавших
  игроков. Home, owner, dimension и nextBlessing в Entity NBT. Deadline использует gameTime.
  Сервер расходует подарки и возвращает тару. AvoidEntityGoal/MoveTowardsRestrictionGoal
  отвечают за избегание/возвращение. Очаг ищет духа только по клику в AABB радиусом 8.
  Разрушенный очаг не снимает persistence; помощь требует существующий очаг.
- LeshyEntity: Slowness по успешному удару; escape по урону при низком HP,
  максимум шесть позиций, loaded-chunk check, vanilla randomTeleport проверяет безопасность.
  Deadline сохраняется. Зов использует vanilla ambient cadence; поиск игрока только при звуке.
- ShrineFeature: стадия SURFACE_STRUCTURES, vanilla forest/plains/taiga, проверка Overworld,
  1/96 попытка до проверки местности. Вся площадка 11×11 внутри одного чанка.
  До записи проверяются почва и четыре слоя препятствий/жидкостей (до 605 проверяемых позиций).
  Нет тикового поиска/ретрогенерации. Это Feature, не StructureStart: `/locate` и карт нет.
- FirstRitual определяет порядок/результат; AltarTileEntity исполняет и хранит int step;
  AltarBlock передаёт interaction. TileEntity не тикает. Обряд общий, результат последнему
  участнику. Полный инвентарь выдаёт предмет рядом с игроком; creative не расходует дар.
  Разрушение сбрасывает шаг без возврата принятых компонентов.
- Knowledge: source of truth — vanilla advancements. Они уже сохраняются по игроку,
  переживают смерть/вход и синхронизируются Minecraft. Дублирующая capability не нужна.
  Будущая система классов потребует отдельных данных и остаётся планом.
- LoreBookItem открывает книгу на сервере; LoreNetwork отправляет один S2C varint по
  открытию. Направление фиксировано; C2S выдачи знаний нет. LoreScreen — в client,
  вызов через DistExecutor. Нет периодических пакетов/глобальных Map игроков.
- compat.JeiRituals загружается только JEI @JeiPlugin. Common setup на него не ссылается.
  CompileOnly API + optional runtimeOnly; сторонних JAR в моде нет.
- generate_milestone.py выполняется последним в create_resources.py; это источник данных 0.4.
- tools/smoke включается только с -PsmokeTest, не входит в production. Тестовый стенд
  может загружать чанки/выдавать предметы только в отдельной копии мира.

Performance: нет новых глобальных tick-handler, тикающего жертвенника, поиска мира,
частой синхронизации или принудительной загрузки чанков в AI. Замеры и ограничения —
VERIFICATION_0.4.0. Исторические утверждения «нет сети/духов» ниже относятся к 0.1/0.2.

## Исходное состояние

Перед изменениями выполнен поиск Forge-проектов и AGENTS.md в рабочей папке
Documents. Проект, реестры, ресурсы и существующие системы не обнаружены.
Documents не является Git-репозиторием. Создан отдельный каталог slavic-myths;
чужие файлы не изменялись. Git-репозиторий и commit не создавались.

## Основа 0.1.0 (сохраняется в 0.2.0)

- `org.slavicmyths.SlavicMyths`: единственная @Mod-точка входа, MOD_ID и подключение реестра к mod event bus.
- `registry.ModItems`: единственный DeferredRegister<Item>, ленивые RegistryObject.
- `assets/slavicmyths`: модели item/generated, PNG 16×16 RGBA и ru_ru/en_us.
- `data/slavicmyths`: стандартные рецепты, advancement inventory_changed и открытия книги рецептов.
- `META-INF/mods.toml`: точные ограничения Minecraft и Forge; версия из Gradle.

Java 8, Forge 36.2.42; official mappings 1.16.5. В этих mappings используются
Item.Properties.tab, ItemGroup.TAB_MISC, Rarity.RARE. Forge RegistryObject находится
в net.minecraftforge.fml. Не переносить имена классов из современных версий.
Использованы wrapper из официального MDK и ForgeGradle 5.1.77 / Gradle 7.3.3
для работы сборочной JVM на Java 8. Более новый MDK использует другой стек сборки.

Регистрация общая для клиента и сервера. Клиентский рендер 0.2.0 изолирован в client.ClientSetup (Dist.CLIENT).
Ни одного tick-handler, сетевого пакета, глобального поиска, состояния игрока,
собственной системы достижений или фонового потока не создано.

## Расширение по необходимости

Предмет добавляется в существующий ModItems, затем получает модель, текстуру,
локализацию и нужные data-ресурсы. Блоки и BlockItem — отдельный реестр блоков
и тот же реестр предметов. Руды и растения опираются на блоки и будущий worldgen.
Мобы/NPC/духи/монстры получают реестр EntityType только при первой реализации;
клиентские рендеры должны быть изолированы от общих классов.
Структуры и измерения проектируются отдельно под worldgen API 1.16.5.
Рецепты, лут и advancements остаются data-driven.
Боги пока описываются в документации, не являются обязательными NPC.

Ритуалы, знания, классы и способности в будущем — отдельные пакеты систем.
Не размещать прогрессию в основном классе, ModItems или глобальном Map игроков.
Когда появятся данные игрока, добавить один провайдер Forge Capability с
версионированным NBT и небольшими логическими разделами. Это план, не код.
Подробности и нерешённые правила: CLASS_SYSTEM.md.
Не создавать интерфейс универсального модуля, собственную шину событий или DI
ради трёх предметов. Стабильные ID служат точками связи контента и будущих систем.

## Производительность будущих систем

Сервер — источник истины для боя, энергии, знаний и ритуалов. Проверять действие
по событию/запросу; ограничения дальности и частоты, права и стоимость — на сервере.
Синхронизировать изменённые поля адресно при входе и изменении, не каждый тик.
Временные эффекты: ограниченные очереди и бюджет работы, очистка при завершении.
AI: ограниченная частота поиска и область; worldgen — на генерации чанка,
без сканирования загруженного мира в игровом тике. Частицы — клиентские с лимитом.
Измерять MSPT, количество пакетов и расход памяти на реальном сценарии до расширения.

## Источники API и сборки

- https://docs.minecraftforge.net/en/1.16.x/concepts/registries/
- https://docs.minecraftforge.net/en/1.16.x/gettingstarted/
- https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html

Источником истины совместимости служит сборка против закреплённого Forge и игровой тест.

## Реализация 0.2.0

- ModItems остаётся единственным реестром предметов (10 ID), ModBlocks — блоков
  (flax, wormwood, perunite_ore). Три прежних registry ID не изменены.
- ModItemGroup: одна Creative Tab с ленивым созданием иконки.
- Растения используют BushBlock: без randomTick, стадий роста и BlockEntity;
  стандартная проверка почвы/соседей, cross-модель и cutout на клиенте.
- ModWorldGen регистрирует 3 ConfiguredFeature в common setup enqueueWork.
  BiomeLoadingEvent с HIGH дополняет списки генерации, не заменяет чужие features.
  Руда: NATURAL_STONE, size=3, range(15) означает исходную Y=0..14,
  шанс одной попытки 1/2 на чанк. Для сравнения vanilla diamond — size=8,
  одна попытка на чанк. Фактическая жила может содержать меньше блоков.
- Лён: до 24 попыток RANDOM_PATCH с шансом 1/3 на чанк; полынь: 16 с шансом 1/4.
  Размещение HEIGHTMAP_SQUARE; пригодность позиции проверяет vanilla generator.
- Ограничение: только vanilla-биомы, исключены NETHER/THEEND/NONE. Это привязка к
  биомам; стороннее измерение, переиспользующее Overworld-биом, может унаследовать
  его features. Гарантия отдельной dimension whitelist сейчас не заявляется.
- Никакой ретрогенерации и runtime-обходов чанков. Все три features работают только
  в штатном pipeline генерации новых чанков. Сохранённый тестовый мир содержит ресурсы.
- Руда — Block с harvestTool(PICKAXE), harvestLevel(2), requiresCorrectToolForDrops.
  Loot table выдаёт ресурс с Fortune либо блок с Silk Touch; XP не добавлен.
- FernFlowerModifier — один Forge Global Loot Modifier с сериализатором из ModLoot.
  JSON сначала ограничивает loot table до minecraft:blocks/fern, затем исключает
  ножницы/Silk Touch и применяет шанс 0,002. Исходные drops сохраняются.
  Это короткая проверка при генерации loot, а не обработчик каждого тика.
- Береста получается рецептом с расходованием бревна; никаких событий для деревьев.
- tools/create_resources.py — единая точка генерации, generate_data.py и
  generate_textures.py содержат новые данные/пиксели. Старый генератор обновлён,
  повторный запуск не возвращает рецепты 0.1.0.

Аудит производительности: PASS по статическому анализу. Нет polling игроков,
мира, блоков, тяжёлых тиковых обработчиков, сетевых пакетов и сохранённых данных игрока.
Число попыток генерации ограничено. MSPT и масштабная статистика не измерялись.

0.4.7: MythShieldItem overrides Forge isShield; blocking property/models are client-only. Weapon JSON and pixel material sources: tools/generate_047.py. ThunderSpearCombat augments accepted server melee hits with a 120-tick item cooldown; no terrain effects.

0.5.0: six separate entity/model classes; LandSpiritEntity shares bounded encounter/persistence helpers. Models are generated from explicit native cuboid masters in generate_050_models.py. Synced visual states, persistent cooldowns/offering UUIDs; homesteads occupy one chunk, guards store home positions. Hot stones use Forge spawn packets. Sound sources are original synthetic PCM encoded as Vorbis.
# Финализация 0.6.5

0.7.2: depth/ElderVodyanoy использует общую водную навигацию, но отдельные модель,
анимации и собственное расписание атак без vanilla instant melee. Стандартный ServerBossInfo.
PoolStoneTile хранит state/intro/UUID, вызывает spawn один раз; PoolIndex (WorldSavedData)
хранит известные/побеждённые омуты. Знак обращается к индексу только при использовании,
не ищет/генерирует чанки. DeepPoolFeature ограничен одним чанком. При фазе III сохраняется
слой до 121 блока, источники воды добавляются только над низкими выступами; при победе
вода в сохранённом слое восстанавливается. Амулет использует прежний Curios necklace.
Новые ресурсы — depth_072.py, последний слой общего генератора.

0.7.0: пакет water добавляет RiverFish поверх AbstractFishEntity, WaterSpirit поверх
существующего LandSpiritEntity, обработчик ItemFishedEvent без замены vanilla loot tables.
FishingNetTile сохраняет четыре улова, износ и владельца; только scheduled ticks раз в
6000–8400 тиков, ограниченная проверка соседства 7×3×7 без загрузки чанков.
VodyanoyEntity хранит до 64 локальных отношений; уведомления о вылове приходят по событиям.
RusalkaEntity проверяет слушателей раз в 20 тиков. RusalkaSong считает очарование на сервере,
корректирует скорость существующим velocity sync и не принимает Charm state от клиента.
WaterFeature размещает растения/малые POI в пределах чанка. Ресурсы — water_070.py;
JeiKitchen изолирован как optional JEI plugin. Книга расширяет существующую битовую маску знаний.

Дополнение артефактов: ArtifactEvents добавляет при появлении моба лёгкие Goal для музыки
и временного защитника. Поиск музыкой — раз в 10 тиков только во время использования,
радиус 12 и до 64 обрабатываемых существ. WildlifeEntity точечно пропускает собственный
encounter-контроллер для этих состояний. Владелец/срок призыва хранятся в Forge persistent NBT,
готовность Посоха — в PlayerPersisted. SkatertTile не тикает: используются scheduled block ticks,
порции синхронизируются blockstate, возврат предмета централизован в onRemove без обычного loot.
GusliClient запускает заранее записанный OGG и прекращает его при отпускании/удалении игрока.
Ресурсы дополнения генерирует tools/artifact_065.py последним слоем общего генератора.

FolkBerryBush использует random ticks, AGE и HALF; BerryPatchFeature создаёт небольшие
группы только при генерации чанка. FolkBerryItem сохраняет прежние ID ягод.
KitchenMenu выполняет рецепты KitchenRecipes на сервере; стол не имеет ticking block entity,
при закрытии меню ингредиенты возвращаются игроку.
FlyingVessel считает физику и сохраняет владельца, транспорт и 9 грузовых слотов в NBT.
FlightNetwork передаёт только ограниченные управляющие значения, без клиентской позиции;
ванильный controlling passenger отключён, чтобы CMoveVehiclePacket не управлял движением.
CargoMenu использует обычную серверную синхронизацию слотов, блокирует вложение транспорта
и подбор при открытом грузе. FlightClient/FlightRenderer отвечают за интерполяцию и позы.
Tailwind ограничен FlightItem. Новые ресурсы воспроизводит tools/finalize_065.py,
вызываемый последним из общего генератора. Curios остаётся единственной системой аксессуаров.


## 0.8.6: finite kurgan encounters

KurganFighter хранит фиксированные профили/тайминги; KurganRoster — чистую
детерминированную политику населения. KurganCreature использует единственный
боевой Goal: серверный release, sync action/start/phase/guard/decoy позиции,
ограниченные cooldown, память 120 тиков, navigation cadence 8 тиков. KurganBolt
— физический ProjectileItemEntity, нормальный урон 6, срок жизни 50 тиков.
KurganCreatureModel/Renderer полностью клиентские; геометрия и pose ветвятся
по фиксированному виду, два ложных силуэта не создают дополнительные entities.

KurganEncounters подписан на PlayerTick раз в секунду, использует существующий
chunk index BurialRecords. До первого спавна проверяет позиции всей группы;
не загружает чанки. KurganEncounterState (triggered/cleared/live UUID) вложен
в NBT экземпляра; отсутствие entity среди загруженных никогда не считается
смертью. Повторный спавн запрещён флагом triggered. Ограниченный summon и
боевые фазы сохраняются в entity NBT. BossDefeated предотвращает повторную
проклятую причину. KurganCurse.sourceDefeated удаляет один UUID кургана из
Overworld player/source map; отсутствующий source очищает устаревший сильный
эффект при следующем login/tick, включая офлайн победу. Старый NBT без
Encounters/BossDefeated загружается с пустым состоянием.

tools/kurgan_086.py воспроизводит только ресурсы 0.8.6, не затрагивая старое
оружие. Manifest содержит все fallback sources. verifyKurgan — чистая JVM
геометрия/NBT/roster проверка, не игровой AI тест и не измерение MSPT.


## 0.9.0 Hunt I

HuntMob наследует LandSpiritEntity для существующего Home/guardian/save API,
но отключает старое расписание usesSpiritSchedule и использует один Fight Goal.
OvinnikEntity сохраняет registry/class ID; VolkolakEntity — второй фиксированный
профиль. Synced action/start/rage управляют native HuntModel. Овинник применяет
physical EmberClump и ограниченный 40-tick trail без блоков/terrain grief.
Combo follow-ups учитывают vanilla hurt window только для собственного удара.
Старые saved attributes Овинника переводятся на 175/10/10/2/.55/32 без autoheal.

HuntRecords — минимальный WorldSavedData с последней охотой каждого owner:
target UUID, anchor, kind, status, HornReady. Отсутствие loaded entity не
завершает запись. Смерть = CLEARED, admin/Peaceful = FAILED. Summoned owner
сохраняется в entity NBT, смена измерения блокируется. После admin clear
выгруженный участник удаляется при следующей загрузке без награды.

HuntItems/HuntSpawns используют только use/placement scans с ограниченными
радиусами и количеством кандидатов. Проверяется только loaded пространство;
нет forced chunks и player tick scans на 128 блоков. Cooldown рога в records,
ванильный item cooldown не блокирует смену цели/локализованную причину отказа.

PouchMenu использует существующий RpgMenu.MENUS, 9-slot Inventory на точном
held ItemStack identity. Запись NBT немедленная; pouch slot/swap защищены,
tag применяется к ручной вставке/shift move. Vanilla ItemStack copy выполняет
глубокую копию nested NBT. PouchScreen переиспользует стиль CargoScreen.
HuntEffects проверяет Curios; stable transient UUID пояса и огненный множитель
без inventory-passive эффектов/дубликатов. SilverCombat использует один item
tag/helper, Volkolak получает ×1.25 в LivingDamageEvent после брони.
HuntAccessoryRenderer использует существующий Curios body API; все вызовы
client методов в OnlyIn/client коде. Custom category JEI не нужна для JSON
crafting. tools/hunt_090.py — воспроизводимая отдельная прослойка native art.

verifyHunt не запускает Minecraft: owner/UUID/state/cooldown NBT, модификаторы
по времени, сроки способностей и глубокая копия nested pouch NBT. Это не
runtime proof отсутствия duplication/AI bugs; ручной QA выделен отдельно.

## 0.9.4 stage-2 data boundaries

ItemState owns persistent/synchronized immutable components; player/entity/world NBT stays separate. ArmorerInput keeps the full grid instead of vanilla trimming; RecipeHolder owns recipe identity. Curios uses data-driven slots and client-only renderer registration. Optional JEI has no common entrypoint reference. All old content retained; source migration is not runtime acceptance.
