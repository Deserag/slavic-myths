# Архитектура

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
