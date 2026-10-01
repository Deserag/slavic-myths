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
