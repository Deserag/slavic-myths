# SLAVIC MYTHS 1.1.2 — «САДЫ, ЯГОДЫ И ПЛОДОВЫЕ РАСТЕНИЯ»
## Единый финальный design document + implementation prompt для Codex

## 0. ОБЯЗАТЕЛЬНЫЕ ПРАВИЛА РАБОТЫ CODEX

Это не задача на аудит или самостоятельный геймдизайн. Нужно реализовать описанную ниже версию максимально буквально.

### 0.1 Экономия токенов
Токены расходовать экономно. Запрещено читать весь репозиторий, делать общий аудит, искать «что ещё улучшить», переписывать старые системы, делать лишние поиски, длинные рассуждения, многократные одинаковые сборки и дополнительные документы. Разрешён только минимальный просмотр registry-классов, creative tab registration, существующих helper/base классов растений 1.1.0, package names, datagen/resource conventions и списка текущих предметов, необходимого для распределения по новым вкладкам.

Работать по принципу: конкретная правка → только связанные файлы → следующая правка.

### 0.2 Minecraft и PolyMC
- Minecraft НЕ запускать.
- НЕ использовать runClient/runServer.
- Пользователь тестирует игру вручную.
- Разрешена одна финальная compile/build-проверка. Дополнительная — только если первая упала из-за изменений этой задачи.
- После реализации НЕ переносить JAR в PolyMC, НЕ менять instance PolyMC и НЕ запускать PolyMC. Перенос выполняется только по отдельной будущей команде пользователя.

### 0.3 Никакой самостоятельной фантазии
Не придумывать новые растения, рецепты, GUI, инструменты, варианты яблони, дополнительные механики, баланс, вкладки или декоративные блоки вне списка ниже. Не заменять утверждённый дизайн «своим улучшенным». Если есть чисто техническая развилка, выбрать простейшее решение, сохраняющее заданное игровое поведение.

### 0.4 Платформа
- Minecraft 1.21.1
- NeoForge
- Java 21
- существующий mod id и package structure
- не портировать и не менять loader

### 0.5 Git
Рабочая ветка: `main`.
После реализации менять только `main`.
Запрещено менять release branch, создавать tag, GitHub Release, force-push, удалять чужие изменения или публиковать JAR как релиз.
Если origin и авторизация уже работают — commit + push в `main`.
Commit message: `feat: implement Slavic Myths 1.1.2 gardens and creative tabs`
Если push требует настройки авторизации — не тратить токены, оставить локальный commit и указать одну команду для push.

---

# 1. ЦЕЛЬ ВЕРСИИ

1.1.2 добавляет второй слой хозяйства после полевых культур 1.1.0:
- многолетние ягодные кусты;
- сад;
- новый вид дерева — яблоню;
- повторный сбор плодов без уничтожения растения;
- собственный саженец яблони;
- собственную древесину яблони;
- восстановление повреждённой кроны только в пределах фиксированной формы;
- естественную генерацию ягод и яблонь;
- реорганизацию Creative Inventory Slavic Myths из одной общей вкладки в тематические вкладки сверху и снизу, как в vanilla Creative Inventory.

Добавляем только:
1. Малина.
2. Черника.
3. Чёрная смородина.
4. Брусника.
5. Клюква.
6. Яблоня.

Не добавлять грушу, сливу, вишню, клубнику, виноград, хмель и другие растения.

---

# 2. CREATIVE INVENTORY — ПОЛНАЯ РЕОРГАНИЗАЦИЯ

Старая общая вкладка Slavic Myths больше не должна служить свалкой всех предметов. Использовать стандартный поддерживаемый Minecraft/NeoForge CreativeModeTab API, без mixin и собственного GUI.

Целевой layout:
- 5 вкладок сверху;
- 4 вкладки снизу.

## Верхний ряд
1. `Основное / Лор` / `Main / Lore`
   - книги, Book of Tales, свитки, записки, квестовые документы, сюжетные жетоны/монеты, лорные коллекционные предметы и уникальные немагические сюжетные вещи.
   - icon: существующая основная lore-book.

2. `Ресурсы и материалы` / `Resources & Materials`
   - руды, raw ores, слитки, nuggets, минералы, обычное сырьё, кости/шкуры/материалы, будущие волокна/ткань.
   - icon: существующий серебряный ресурс/руда/слиток.

3. `Земледелие и растения` / `Farming & Plants`
   - все семена и сырые сельхоз-продукты 1.1.0;
   - organic_fertilizer;
   - все ягоды 1.1.2;
   - все саженцы ягодных кустов;
   - apple_sapling;
   - будущие farm plants.
   - icon: rye_seeds или другой утверждённый seed item 1.1.0.

4. `Еда и кухня` / `Food & Kitchen`
   - готовые блюда, напитки, супы, блины, пироги, переработанные ингредиенты, кухонные блоки и кухонные предметы.
   - сырые ягоды сюда НЕ дублировать.
   - icon: существующее готовое блюдо.

5. `Инструменты и оружие` / `Tools & Weapons`
   - оружие, ножи, копья, луки, кирки, топоры, shields, sickle, field_hoe, watering_can и иные инструменты.
   - icon: существующее узнаваемое оружие или sickle.

## Нижний ряд
6. `Броня и одежда` / `Armor & Clothing`
   - вся броня, wearables, будущая одежда.
   - icon: существующий chestplate/элемент брони.

7. `Декор и блоки` / `Decor & Blocks`
   - строительные и декоративные блоки, мебель, двери, люки, заборы, доски, брёвна, ступени, плиты, apple wood family.
   - icon после 1.1.2: apple_planks.

8. `Магия и артефакты` / `Magic & Artifacts`
   - амулеты, обереги, руны, магические камни, ритуальные предметы и блоки, Staff of Veles, Gusli-samogudy и другие явно магические уникальные вещи.
   - icon: существующий thunder stone / amulet.

9. `Мобы / спавн-яйца` / `Mobs / Spawn Eggs`
   - все spawn eggs Slavic Myths и только связанные spawn/dev items, если они уже предназначены для creative.
   - icon: существующее spawn egg.

## Правила распределения существующего контента
Разрешается минимально прочитать текущую регистрацию старой вкладки и registry list. Не читать механику каждого предмета.
Каждый предмет должен отображаться только в ОДНОЙ thematic Slavic Myths tab.
Приоритет классификации:
1. Spawn egg -> Mobs / Spawn Eggs.
2. Armor/wearable -> Armor & Clothing.
3. Явный magical artifact/rune/ritual -> Magic & Artifacts.
4. Weapon/tool/shield -> Tools & Weapons.
5. Raw plant/seed/crop/sapling/berry/farm input -> Farming & Plants.
6. Prepared food/drink/kitchen-specific item/block -> Food & Kitchen.
7. Ore/ingot/nugget/raw crafting material -> Resources & Materials.
8. Building/decorative block/item -> Decor & Blocks.
9. Book/scroll/quest/lore/story collectible -> Main / Lore.
10. Реально не классифицируемый item -> Main / Lore fallback.

Старую монолитную вкладку удалить из отображения после миграции. Дубликаты между Slavic Myths tabs не делать.

---

# 3. НОВЫЕ ЯГОДЫ — ПРЕДМЕТЫ

Все item textures: 32x32, transparent background, pixel art, без anti-aliasing. Нельзя использовать один sprite с recolor.

## Малина — `raspberry`
- 2 крупные ягоды, состоящие из заметных сегментов;
- насыщенный красно-малиновый цвет, тёмные тени между сегментами;
- 2 маленьких зелёных листа сверху;
- одна ягода чуть ниже другой;
- не должна выглядеть как клубника.
Food: nutrition 2, saturation modifier 0.20.

## Черника — `blueberry`
- 2 тёмно-синие круглые ягоды;
- маленький светлый синевато-серый блик;
- характерная тёмная «корона» на верхушке;
- 2–3 маленьких листа.
Food: nutrition 2, saturation modifier 0.20.

## Чёрная смородина — `blackcurrant`
- свисающая гроздь из 4–5 почти чёрных, но слегка фиолетово-синих ягод;
- маленькие блики;
- зелёная веточка и 1 лист.
Food: nutrition 2, saturation modifier 0.20.

## Брусника — `lingonberry`
- 3 небольшие ярко-красные ягоды;
- плотные овальные тёмно-зелёные листья;
- компактная горизонтальная композиция.
Food: nutrition 1, saturation modifier 0.20.

## Клюква — `cranberry`
- 2–3 тёмно-красные гладкие круглые ягоды;
- тонкий стелющийся стебель;
- маленькие узкие листья.
Food: nutrition 1, saturation modifier 0.15.

Все ягоды stack 64, обычная food animation, без potion effects.

# 4. САЖЕНЦЫ ЯГОДНЫХ КУСТОВ

Registry ids:
- raspberry_sapling
- blueberry_sapling
- blackcurrant_sapling
- lingonberry_sapling
- cranberry_sapling

Это отдельные plantable items. Berry item как seed не использовать.
Дизайн каждого sapling: маленький фрагмент соответствующего растения с корневым/земляным основанием; характерные листья; без полноценных зрелых гроздей; 32x32.

Посадка разрешена на dirt-like surface: grass_block, dirt, coarse_dirt, podzol, rooted_dirt, mud, moss_block при корректной tag-based поддержке.
Клюква при ручной посадке НЕ требует обязательной воды рядом.

---

# 5. ОБЩАЯ СИСТЕМА МНОГОЛЕТНИХ КУСТОВ

Создать reusable base для perennial berry bushes.

PHASE:
- 0 — молодой куст;
- 1 — взрослый зелёный куст;
- 2 — цветение;
- 3 — незрелые ягоды;
- 4 — зрелые ягоды.

После сбора: PHASE 4 -> PHASE 1, куст остаётся.
Natural growth: 0->1->2->3->4 через random tick. Дополнительный шанс перехода примерно 1/18 на подходящий random tick. Не создавать BlockEntity и отдельные реальные таймеры.

Bone meal: +1 PHASE, максимум 4.
Organic fertilizer из 1.1.0: расширить поддержку — в своей 3x3 обработке повышает PHASE каждого незрелого berry bush на 1; mature не меняет; предмет расходуется только если хотя бы одно растение реально изменено.
Watering can: не расширять на кусты, оставить farmland-only.
Sickle: ягодные кусты не собирать и не добавлять в sickle_harvestable.

## Сбор зрелых ягод правым кликом
- raspberry: 2–4;
- blueberry: 2–5;
- blackcurrant: 2–4;
- lingonberry: 1–3;
- cranberry: 1–3.

После harvest PHASE=1, мягкий vanilla plant/collect sound и немного растительных particles. Fortune и Silk Touch не увеличивают ручной harvest.

## Пересадка
Shears при разрушении взрослого berry bush:
- ровно 1 соответствующий sapling;
- никаких ягод;
- обычный durability damage shears.

Без shears:
- PHASE 0: 50% sapling;
- PHASE 1–3: 35% sapling;
- PHASE 4: 35% sapling + 50% шанс 1 berry;
- Fortune не влияет.

Правильная стратегия: ягоды — right click, пересадка — shears.

---

# 6. ВИЗУАЛ КУСТОВ

## Малина
Взрослое растение высотой 2 блока. Высокий куст с несколькими вертикальными побегами и группами листьев. Не наносит урон и не имеет sweet-berry-bush damage/collision.
PHASE 0: только lower block, молодой куст.
При переходе в PHASE 1, если сверху свободно, создаётся upper half; если нет — растение остаётся молодым до появления места.
PHASE 1–4: lower/upper синхронизированы.
Если сломан upper: lower остаётся, возвращается к взрослому зелёному состоянию и способен восстановить верх при свободном месте.
Если сломан lower: upper удаляется без дополнительного loot.
Цветы белые. Незрелые ягоды светло-зелёные/желтоватые. Зрелые красные.

## Черника
Высота визуально 0.6–0.8 блока. Широкий низкий куст, густые мелкие листья. Цветы бело-розовые. Незрелые ягоды зелёные, зрелые синие. Не копировать vanilla sweet berry bush.

## Чёрная смородина
Около 1 блока высотой. Более вертикальный куст с 3–4 заметными ветками и крупными листьями. Цветы зеленовато-белые. Незрелые грозди зеленоватые, зрелые тёмно-фиолетово-чёрные.

## Брусника
Около 0.4–0.5 блока. Низкий плотный вечнозелёный кустик, овальные тёмно-зелёные листья, бело-розовые цветы, зрелые маленькие красные ягоды. Сам на соседние blocks не расползается.

## Клюква
Около 0.25–0.4 блока. Стелющееся растение: горизонтальные тонкие побеги, маленькие листья, плоды близко к земле. Цветы розоватые, незрелые плоды светлые/зелёные, зрелые тёмно-красные. Отдельный water block не нужен.

Текстуры кустов 32x32; 64x64 допустимо только для tall raspberry, если 32x32 реально не хватает. Запрещено делать пять одинаковых кустов с разными цветами ягод.

---

# 7. NATURAL WORLDGEN ЯГОД

Использовать стандартные configured/placed features + biome modifiers NeoForge. Никаких global ticks/world scans. Малые естественные группы, не поля.
Natural PHASE при генерации: 1–4; большинство 1–3; около 20% зрелые.

Raspberry:
- forest, birch_forest, old_growth_birch_forest, meadow;
- примерно 1 patch на 8 eligible chunks;
- 2–5 растений.

Blueberry:
- taiga, snowy_taiga, old_growth_pine_taiga, old_growth_spruce_taiga;
- примерно 1 patch на 10 eligible chunks;
- 3–6 растений.

Blackcurrant:
- forest, birch_forest, meadow;
- примерно 1 patch на 14 eligible chunks;
- 2–4 растения.

Lingonberry:
- taiga, snowy_taiga, old_growth_pine_taiga, old_growth_spruce_taiga;
- примерно 1 patch на 12 eligible chunks;
- 2–5 растений.

Cranberry:
- swamp; mangrove_swamp только если обычная placement logic на подходящей поверхности работает без хаков;
- примерно 1 patch на 8 eligible chunks;
- 3–7 растений;
- предпочитать mud/grass/dirt и, если дёшево стандартным predicate, наличие воды в горизонтальном радиусе до 4 блоков. Не писать дорогой custom scan.

---

# 8. ЯБЛОНЯ — ОТДЕЛЬНЫЙ ВИД ДЕРЕВА

Это НЕ дуб и не вариант oak feature.
Обязательно:
- собственный apple_sapling;
- собственная apple wood family;
- собственные apple leaves;
- одна фиксированная форма дерева;
- ручной сбор яблок без рубки дерева;
- повторное плодоношение;
- breaking плодоносящей листвы НЕ даёт яблок;
- повреждённая листва восстанавливается только внутри исходной формы;
- бесконечного разрастания нет.

Плод: использовать vanilla `minecraft:apple`. Не создавать отдельный slavic apple item.

## Apple Sapling
Registry: `apple_sapling`.
RU: Саженец яблони. EN: Apple Sapling.
Texture 32x32: тонкий серо-коричневый стебель, округлые зелёные листья, допускается один маленький бело-розовый цветочный акцент. Не копировать oak sapling.
Bone meal работает.
Саженец ВСЕГДА создаёт одну каноническую форму дерева. Никаких random variants, giant 2x2 variants или oak-like shape. Перед ростом проверить свободное пространство; соседние blocks не разрушать.

---

# 9. КАНОНИЧЕСКАЯ ФОРМА ЯБЛОНИ

Главный пользовательский референс: `references/03_user_apple_tree_shape.png`.
Итог должен быть немного выше референса и иметь чуть более объёмную крону, но оставаться компактным садовым деревом.

Размер:
- общая высота примерно 7 blocks;
- ширина кроны примерно 5 blocks;
- ствол проходит внутрь кроны;
- форма симметричная/почти симметричная и всегда одинаковая.

Координаты относительно base trunk `(0,0,0)`.

Ствол apple_log:
- (0,0,0)
- (0,1,0)
- (0,2,0)
- (0,3,0)
- (0,4,0)

Короткие ветви на Y=3:
- (+1,3,0)
- (-1,3,0)
- (0,3,+1)
- (0,3,-1)

Ветки — apple_log с корректной осью.
Top trunk `(0,4,0)` является техническим CROWN ANCHOR и внешне выглядит как обычный log.
У natural/grown tree: `CROWN_ANCHOR=true`.
Player-placed apple_log по умолчанию: false.

Allowed canopy mask:
- Y=2: 3x3 вокруг ствола, без четырёх углов; центр trunk.
- Y=3: 5x5, убрать четыре дальних угла; branch/log positions заняты logs, остальные leaves.
- Y=4: 5x5, убрать четыре дальних угла; центр trunk, остальные leaves.
- Y=5: 5x5, убрать четыре дальних угла; leaves, включая пространство над trunk.
- Y=6: центральный 3x3 + четыре cardinal позиции на расстоянии 2; без diagonal distance-2 corners.

Это максимальный разрешённый объём кроны. Ни один regrowth leaf не может появляться вне этой mask. Хранить mask как маленький static set offsets; не искать форму дерева сканированием мира.

# 10. APPLE WOOD FAMILY

Цветовая идея:
- кора: тёплая серо-коричневая;
- трещины: тёмно-коричневые;
- торец: приглушённый светло-розово-бежевый;
- годичные кольца темнее;
- доски: тёплый приглушённый розовато-коричневый, не ярко-розовый.

Добавить:
- apple_log
- stripped_apple_log
- apple_wood
- stripped_apple_wood
- apple_planks
- apple_stairs
- apple_slab
- apple_fence
- apple_fence_gate
- apple_door
- apple_trapdoor
- apple_pressure_plate
- apple_button
- apple_sign
- apple_wall_sign
- apple_hanging_sign
- apple_wall_hanging_sign
- apple_leaves
- apple_sapling

Не добавлять apple boat и apple chest boat в 1.1.2.
Axe stripping: log->stripped log, wood->stripped wood.
Wood recipes — vanilla-like. 1 log/wood equivalent -> 4 planks. Остальные costs стандартные.
Если уже есть reusable wood helper — использовать, но не рефакторить всю старую регистрацию.

---

# 11. APPLE LEAVES — СОСТОЯНИЯ И ВИЗУАЛ

Использовать ОДИН custom leaves block с properties:
- стандартные leaves distance/persistent свойства, необходимые для decay;
- `FRUIT_STAGE` 0..3;
- `FRUITABLE` boolean.

FRUIT_STAGE:
0 = обычная листва;
1 = цветущая;
2 = маленькие зелёные яблоки;
3 = зрелые красные яблоки.

Player-placed apple_leaves:
- stage 0;
- FRUITABLE=false;
- persistent=true по обычной leaves logic.

Generated/regrown leaves:
- persistent=false;
- fruitable определяется фиксированной canopy mask.

Плодоносящими сделать примерно 35% разрешённых leaf positions. Этот набор offsets должен быть детерминированным и одинаковым для всех яблонь; в основном наружные и средние части кроны. Внутренние листья около ствола преимущественно fruitable=false.

Визуал:
- base leaves: средне-/тёмно-зелёные, более плотные и мелкие, чем oak, без копирования vanilla texture;
- flowering: зелёная листва остаётся основной, по ней небольшие белые цветы с бледно-розовыми центрами;
- green fruit: несколько маленьких светло-зелёных яблок, цветов почти нет;
- ripe fruit: 2–4 заметных красных яблочных акцента с маленьким светлым бликом, не делать огромные яблоки на полблока.

---

# 12. ЦИКЛ ПЛОДОНОШЕНИЯ ЯБЛОНИ

Только для FRUITABLE=true.
Random tick:
- 0 -> 1;
- 1 -> 2;
- 2 -> 3.

На каждом подходящем random tick шанс продвижения 1/24.
Каждый fruitable leaf развивается независимо; всю крону не синхронизировать.
FRUITABLE=false всегда остаётся stage 0.
Bone meal на fruitable leaf: +1 stage, максимум 3. На stage 3 или non-fruitable ничего не делает.
Organic fertilizer на tree leaves не действует.

## Ручной сбор яблок
Right click по FRUITABLE=true + FRUIT_STAGE=3:
- гарантированно 1 minecraft:apple;
- 25% шанс второго apple;
- leaf block остаётся;
- FRUIT_STAGE сбрасывается в 0;
- короткий plant/collect sound;
- немного leaf particles.

Никакой специальный инструмент в 1.1.2 не нужен. В будущем возможен отдельный инструмент массового сбора, но сейчас его не создавать.

## Ломание листвы
Абсолютное правило: breaking apple_leaves НИКОГДА не выдаёт apple, даже если leaf был ripe.
Shears/Silk Touch -> apple_leaves item.
Обычное разрушение -> leaves-style шанс sapling/stick.
Apple fruit отсутствует в break loot table полностью.
Это специально делает ручной harvest выгоднее рубки дерева.

Apple sapling base chance с обычных leaves: около 5%, Fortune — стандартное vanilla-like scaling. Shears/Silk выдаёт leaves item и не даёт отдельный sapling.

---

# 13. ВОССТАНОВЛЕНИЕ КРОНЫ

Ключевая механика: дерево способно медленно восстановить сломанную листву, но не может бесконечно разрастаться.

Top natural/grown trunk `(0,4,0)` имеет CROWN_ANCHOR=true и random ticking.
На random tick anchor:
1. шанс regrowth 1/8;
2. если не прошёл — ничего;
3. найти максимум ОДНУ missing position из static canonical canopy mask;
4. target должен быть air;
5. target должен иметь хотя бы один adjacent apple_log или apple_leaves, чтобы восстановление шло от живой структуры;
6. поставить apple_leaves;
7. stage=0;
8. fruitable согласно фиксированной fruitable mask;
9. persistent=false;
10. завершить tick.

Если все разрешённые позиции уже заполнены — ничего не делать.
Никогда не проверять/ставить leaves вне mask.
Никогда не создавать более одного leaf за успешный anchor tick.

Если CROWN_ANCHOR log уничтожен:
- regrowth полностью прекращается;
- natural foliage дальше подчиняется обычной leaves distance/decay logic.

Если игрок вручную поставил apple_log обратно:
- default CROWN_ANCHOR=false;
- самовосстановление дерева не запускается.

Если почти вся крона уничтожена, regrowth идёт от оставшихся logs/leaves внутрь allowed mask постепенно через adjacency. Это не должно восстанавливать всю крону мгновенно.

---

# 14. NATURAL WORLDGEN ЯБЛОНИ

Использовать ту же canonical tree feature, что и apple_sapling.
Biomes:
- plains
- sunflower_plains
- forest
- birch_forest
- meadow

Не генерировать в dark_forest, swamp, taiga, desert, Nether, End.
Редкость: ориентир около 1 apple tree на 18 eligible chunks.
Только одиночные редкие деревья, без огромных рощ и без замены vanilla trees.

Initial fruit stages:
- большинство fruitable leaves stage 0;
- небольшая часть stage 1;
- максимум около 10% fruitable leaves могут появиться stage 3.

Natural tree и sapling-grown tree используют одну и ту же форму, crown anchor и fruitable mask.

---

# 15. CREATIVE PLACEMENT НОВОГО КОНТЕНТА

Farming & Plants, после контента 1.1.0:
1. raspberry
2. blueberry
3. blackcurrant
4. lingonberry
5. cranberry
6. raspberry_sapling
7. blueberry_sapling
8. blackcurrant_sapling
9. lingonberry_sapling
10. cranberry_sapling
11. apple_sapling

Organic fertilizer остаётся в Farming & Plants.
Sickle, field_hoe, watering_can -> Tools & Weapons.

Decor & Blocks, apple family vanilla-like order:
1. apple_log
2. stripped_apple_log
3. apple_wood
4. stripped_apple_wood
5. apple_planks
6. apple_stairs
7. apple_slab
8. apple_fence
9. apple_fence_gate
10. apple_door
11. apple_trapdoor
12. apple_pressure_plate
13. apple_button
14. apple_sign
15. apple_hanging_sign
16. apple_leaves

Vanilla minecraft:apple в mod tab не добавлять.

---

# 16. TAGS И РЕЦЕПТЫ

Mod tags:
- item `slavicmyths:berries` = raspberry, blueberry, blackcurrant, lingonberry, cranberry;
- item `slavicmyths:berry_saplings` = 5 berry saplings;
- block `slavicmyths:berry_bushes` = 5 berry bush types;
- block/item `slavicmyths:apple_logs` = apple_log, stripped_apple_log, apple_wood, stripped_apple_wood.

Apple wood добавить в все корректные стандартные tags для logs_that_burn, planks, wooden stairs/slabs/fences/gates/doors/trapdoors/pressure plates/buttons, leaves, saplings, mineable/axe, signs/hanging signs и т.д.
Berry bushes не добавлять в sickle_harvestable.

Recipes:
- apple wood family — vanilla-like;
- berries и berry saplings не крафтить друг из друга;
- новых food recipes в 1.1.2 не добавлять.

---

# 17. LOCALIZATION

Обязательные ru_ru и en_us.

RU items/blocks:
Малина; Черника; Чёрная смородина; Брусника; Клюква;
Саженец малины; Саженец черники; Саженец чёрной смородины; Саженец брусники; Саженец клюквы; Саженец яблони;
Яблоневое бревно; Обтёсанное яблоневое бревно; Яблоневая древесина; Обтёсанная яблоневая древесина; Яблоневые доски; Яблоневые ступени; Яблоневая плита; Яблоневый забор; Яблоневая калитка; Яблоневая дверь; Яблоневый люк; Яблоневая нажимная плита; Яблоневая кнопка; Яблоневая табличка; Яблоневая подвесная табличка; Яблоневые листья.

EN:
Raspberry; Blueberry; Blackcurrant; Lingonberry; Cranberry;
Raspberry Sapling; Blueberry Sapling; Blackcurrant Sapling; Lingonberry Sapling; Cranberry Sapling; Apple Sapling;
Apple Log; Stripped Apple Log; Apple Wood; Stripped Apple Wood; Apple Planks; Apple Stairs; Apple Slab; Apple Fence; Apple Fence Gate; Apple Door; Apple Trapdoor; Apple Pressure Plate; Apple Button; Apple Sign; Apple Hanging Sign; Apple Leaves.

Creative tabs RU/EN — строго названия из раздела 2.

---

# 18. ADVANCEMENT

ID: `garden_harvest`
Parent: minecraft:husbandry/root
Trigger: получить любой item из slavicmyths:berries.
RU title: `Лесной урожай`
RU description: `Собери одну из новых ягод`
EN title: `Forest Harvest`
EN description: `Harvest one of the new berries`
Icon: raspberry.
Frame: task. Без XP, hidden и challenge.
Отдельный apple advancement не создавать.

---

# 19. ASSET RULES И РЕФЕРЕНСЫ

Не использовать универсальное правило 128x128.
Рекомендуемые размеры:
- item sprites 32x32;
- berry bushes 32x32;
- tall raspberry 64x64 только при реальной необходимости;
- apple leaves 32x32;
- apple wood textures 32x32;
- door/sign resources — vanilla-compatible layout.

Стиль: Minecraft-compatible pixel art, no blur, no anti-aliasing, no photographic texture, no AI-smoothed edges. Формы должны читаться в GUI и мире.

References:
- `references/01_gardens_items_and_plants.png` — концепт ягод, кустов, яблони и предметов;
- `references/02_creative_tabs_concept.png` — концепт тематических creative tabs;
- `references/03_user_apple_tree_shape.png` — главный референс формы яблони;
- `references/04_current_single_tab.png` — текущее состояние общей вкладки, которую надо заменить.

Приоритет: текстовое ТЗ > пользовательский apple reference > остальные картинки.
Если на картинке есть объект, которого нет в scope этого файла — НЕ реализовывать его.

---

# 20. PERFORMANCE И ТЕХНИЧЕСКИЕ ОГРАНИЧЕНИЯ

Обязательно:
- никаких global every-tick scans яблонь;
- никакого поиска всех яблонь в chunk;
- regrowth только random tick crown anchor;
- один successful regrowth tick = максимум один leaf;
- canopy mask — маленький static set;
- worldgen — стандартные placed features/biome modifiers;
- berry growth — random tick;
- без BlockEntity для кустов, apple leaves и crown anchor;
- без runtime texture generation.

Создать необходимые registrations, blockstates, models, item models, textures, loot tables, recipes, tags, lang, advancement, configured/placed features и biome modifiers.

---

# 21. СОВМЕСТИМОСТЬ С 1.1.0

Не ломать rye, barley, oat, turnip, cabbage, pea, flax, sickle, field_hoe, watering_can и organic_fertilizer.
Изменить только:
- organic_fertilizer получает поддержку berry bushes;
- предметы 1.1.0 распределяются по новым thematic tabs.

Не менять food values, crop growth rates, sickle mechanics и watering can farmland mechanics.

---

# 22. ЧЕГО В 1.1.2 НЕТ

Не добавлять:
- варенье/джем/компот/морс/сок/сидр;
- пироги, ягодные блины и новую кухню;
- сушку ягод;
- storage;
- корзины;
- мельницу/муку;
- животных/NPC/торговлю/садовника;
- секатор;
- специальный инструмент для яблок;
- новые GUI;
- custom sounds;
- seasons/weather growth;
- обязательный полив кустов;
- apple boat/chest boat;
- любые дополнительные растения или фруктовые деревья.

---

# 23. ВЕРСИЯ, BUILD И GIT

Обновить mod version до `1.1.2` в реальном source of truth проекта.

Build:
1. Статически проверить очевидные compile errors.
2. Выполнить одну финальную подходящую build/compile команду.
3. Minecraft не запускать.
4. PolyMC не трогать.
5. Если build падает из-за своей ошибки — исправить и повторить один раз.
6. Если fail вызван заранее существующей независимой проблемой — не ремонтировать весь проект, а указать точную ошибку.

Git:
- branch main;
- commit `feat: implement Slavic Myths 1.1.2 gardens and creative tabs`;
- push main, только если авторизация уже доступна;
- никакой release branch/tag/GitHub Release/force push.

---

# 24. КРИТЕРИИ ГОТОВНОСТИ

Creative Inventory:
[ ] 9 тематических вкладок.
[ ] 5 сверху, 4 снизу через нормальный API.
[ ] Старая общая вкладка больше не служит dump.
[ ] Весь существующий отображаемый контент распределён.
[ ] Нет дубликатов между Slavic Myths tabs.
[ ] Новый контент сразу находится в правильных вкладках.

Berries:
[ ] 5 berry items.
[ ] 5 sapling items.
[ ] 5 berry bush types.
[ ] PHASE 0..4.
[ ] Right-click harvest оставляет куст.
[ ] Shears гарантируют sapling.
[ ] Fortune не увеличивает harvest.
[ ] Raspberry adult = 2 blocks.
[ ] Raspberry не наносит damage.
[ ] Все кусты визуально различаются.
[ ] Organic fertilizer поддерживает berry bushes.
[ ] Sickle не собирает ягоды.
[ ] Watering can для кустов не расширена.
[ ] Natural worldgen работает по заданным biomes/редкости.

Apple tree:
[ ] apple_sapling существует.
[ ] Всегда одна canonical shape.
[ ] Нет random oak variants.
[ ] Apple wood family создана.
[ ] apple_leaves FRUIT_STAGE 0..3.
[ ] FRUITABLE property работает.
[ ] Flowering и green-fruit визуально существуют.
[ ] Right-click ripe leaf выдаёт vanilla apple.
[ ] Leaf не ломается при harvest.
[ ] Harvest сбрасывает stage в 0.
[ ] Breaking leaves никогда не даёт apple.
[ ] Sapling drop leaves работает.
[ ] Crown anchor создаётся только у generated/grown tree.
[ ] Missing leaves восстанавливаются.
[ ] Regrowth только внутри canonical mask.
[ ] Не более одного leaf за успешный anchor tick.
[ ] Полная крона перестаёт расти.
[ ] Нет бесконечного spread.
[ ] После удаления crown anchor regrowth прекращается.
[ ] Natural apple worldgen редкий и использует ту же shape.

Workflow:
[ ] Общий аудит не проводился.
[ ] Лишние файлы не читались.
[ ] Minecraft не запускался.
[ ] PolyMC не трогался.
[ ] Выполнена только необходимая build-проверка.
[ ] Version = 1.1.2.
[ ] Branch = main.
[ ] Release branch/tag/Release не тронуты.
[ ] Push только если авторизация уже была готова.

---

# 25. ФОРМАТ ФИНАЛЬНОГО ОТЧЁТА CODEX

Коротко.

## Implemented
5–12 bullets о реально добавленном.

## Build
- command;
- success/fail;
- при fail только конкретная причина.

## Git
- branch;
- commit hash;
- push status.

## Manual test checklist
1. Проверить 9 tabs и верхний/нижний ряд.
2. Проверить отсутствие старой общей свалки.
3. Найти каждый тип ягод natural worldgen.
4. Собрать зрелые ягоды right click и дождаться повторного плодоношения.
5. Пересадить куст shears.
6. Проверить 2-block raspberry.
7. Вырастить apple sapling bone meal и проверить одинаковую форму нескольких деревьев.
8. Собрать ripe apple рукой.
9. Сломать ripe apple leaves и убедиться, что apple НЕ выпал.
10. Сломать часть кроны и проверить постепенное восстановление.
11. Убедиться, что крона не разрастается сверх mask.
12. Сломать crown anchor/trunk и убедиться, что regrowth прекратился.
13. Проверить apple wood recipes.
14. Проверить ru/en localization и food values ягод.
15. Проверить advancement garden_harvest.

Codex эти игровые проверки НЕ выполняет.
Не писать длинное эссе, историю рассуждений или предложения новых фич.
