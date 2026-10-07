# SLAVIC MYTHS 1.1.6 — «ХРАНЕНИЕ И ХОЗЯЙСТВЕННЫЙ БЫТ»
## Финальный design document + implementation prompt для Codex

---

# 0. ОБЯЗАТЕЛЬНЫЙ РЕЖИМ РАБОТЫ CODEX

Это точное ТЗ. Дизайн, список объектов и игровая логика уже определены.
Codex должен реализовать описанное и НЕ расширять scope самостоятельно.

## 0.1 ЭКОНОМИЯ ТОКЕНОВ — КРИТИЧЕСКОЕ ПРАВИЛО

Токены расходовать только на работу, которая непосредственно нужна версии 1.1.6.

ЗАПРЕЩЕНО:
- проводить общий аудит Slavic Myths;
- читать весь репозиторий подряд;
- анализировать боссов, структуры, мобов и боевые системы, не связанные с 1.1.6;
- заново анализировать 1.1.0–1.1.5 целиком;
- составлять длинный архитектурный документ до начала правок;
- повторять содержание этого ТЗ в собственном плане;
- рефакторить старые системы «заодно»;
- искать потенциальные улучшения вне scope;
- читать десятки файлов «на всякий случай»;
- многократно выполнять одинаковые поиски;
- делать лишние build-циклы;
- переносить сборку в PolyMC;
- придумывать новые контейнеры/продукты;
- создавать новую общую storage framework сложнее, чем реально нужно.

Разрешён минимальный просмотр:
- registrations блоков/items;
- существующих BlockEntity/Menu helpers;
- Creative Tabs;
- table food renderer из 1.1.5, если он существует и может быть переиспользован;
- food/crop/material tags 1.1.0–1.1.5;
- berries/mushrooms;
- существующих container helpers;
- save/network conventions.

Принцип:
**нашёл конкретную точку интеграции → реализовал → перешёл к следующей части.**

## 0.2 MINECRAFT НЕ ЗАПУСКАТЬ

НЕ запускать:
- runClient;
- runServer;
- Minecraft;
- тестовые миры;
- PolyMC.

Игровое тестирование пользователь делает вручную.

Разрешено:
- одна финальная compile/build проверка;
- максимум один повторный build после исправления ошибки, созданной изменениями 1.1.6.

## 0.3 POLYMC НЕ ТРОГАТЬ

После реализации:
- не переносить JAR;
- не менять instance;
- не копировать в mods;
- не запускать PolyMC.

Это будет отдельная задача только по прямой команде пользователя.

## 0.4 НИКАКОЙ САМОСТОЯТЕЛЬНОЙ ФАНТАЗИИ

НЕ добавлять:
- новые виды хранения;
- погреб;
- амбар как структуру;
- холодильник;
- соль;
- порчу еды;
- температуру хранения;
- алкоголь;
- ферментацию;
- брожение в бочке;
- сидр;
- квас;
- медовуху;
- пиво;
- жидкостные tank API;
- новые GUI, кроме тех, что прямо определены ниже;
- новые культуры;
- новые блюда;
- новые ягоды;
- новую систему веса;
- переносимые рюкзаки.

Если есть техническая развилка:
выбрать наиболее простой и устойчивый вариант, который сохраняет описанное игровое поведение.

## 0.5 ПЛАТФОРМА

- Minecraft 1.21.1
- NeoForge
- Java 21
- текущий mod id
- текущая package/resource/data структура

## 0.6 GIT

Рабочая ветка:
`main`

После реализации:
- commit только в `main`;
- push `origin/main`, только если авторизация уже работает;
- НЕ трогать release branch;
- НЕ создавать tag;
- НЕ создавать GitHub Release;
- НЕ force-push;
- НЕ удалять чужие изменения.

Commit:
`feat: implement Slavic Myths 1.1.6 household storage`

Если push требует настройки auth:
- не тратить токены;
- оставить локальный commit;
- дать одну команду push в финальном отчёте.

---

# 1. ЦЕЛЬ ВЕРСИИ

К 1.1.6 игрок уже имеет:
- полевые культуры;
- ягоды и яблоки;
- животных;
- мясо, яйца и молоко;
- текстиль;
- кухню;
- множество ингредиентов и готовых блюд.

1.1.6 должна убрать ощущение, что всё хозяйство хранится только в обычных сундуках.

Главный принцип:

**разные типы запасов → логичные способы хранения → содержимое по возможности видно прямо в мире.**

Обязательные системы:

1. Мешок.
2. Корзина.
3. Большая корзина.
4. Деревянный ящик.
5. Ларь.
6. Деревянная бочка.
7. Настенная полка.
8. Большая хозяйственная полка.
9. Сушилка.
10. Сушёные ягоды.
11. Сушёные грибы.
12. Сноп ржи.
13. Сноп ячменя.
14. Сноп овса.
15. Стог с несколькими уровнями наполнения.
16. Визуальные состояния заполнения хранилищ.
17. Реальное 3D-отображение предметов на полках.
18. Creative Tab / tags / localization / advancement.

---

# 2. ВИЗУАЛЬНЫЕ РЕФЕРЕНСЫ

Использовать:

`references/01_storage_and_household.png`

`references/02_drying_sheaves_haystack.png`

Это концептуальные референсы формы и назначения.

Они НЕ являются готовыми texture atlas.

Приоритет:
1. Точное текстовое ТЗ.
2. Референсы.

---

# 3. ОБЩИЙ ВИЗУАЛЬНЫЙ СТИЛЬ

Сохранять правило, введённое для 1.1.5:

- меньше мелкой детализации;
- ближе к vanilla Minecraft;
- крупные читаемые пиксельные формы;
- без HD-вида.

Предпочтительно:

Item sprites:
- 16x16.

Block textures:
- визуальная плотность около vanilla 16x16 на сторону блока.

Разрешено 32x32:
- только если конкретная геометрия/предмет теряет читаемость.

Запрещено:
- anti-aliasing;
- blur;
- photo textures;
- микроскопический орнамент;
- мелкий AI-noise.

Красный славянский орнамент:
- использовать умеренно;
- только крупный геометрический мотив;
- не покрывать им всю поверхность каждого блока.

---

# 4. ОБЩИЙ HELPER ДЛЯ ВИЗУАЛЬНОГО СОДЕРЖИМОГО

В 1.1.5 уже должна существовать логика рендера ItemStack на обычном столе.

Codex разрешается ОДИН раз минимально проверить, есть ли reusable helper/render code.

Если есть:
- переиспользовать renderer utility / transforms.

Если нет:
- создать небольшой reusable helper для storage BlockEntityRenderers.

Helper должен:
- принимать ItemStack;
- position;
- rotation;
- scale;
- ItemDisplayContext.FIXED или корректный эквивалент 1.21.1;
- не создавать ItemEntity;
- не заставлять предмет вращаться;
- не делать bobbing animation.

Это особенно важно для полок.

---

# 5. МЕШОК — `storage_sack`

RU:
`Мешок`

EN:
`Storage Sack`

Creative:
`Декор и блоки`

## 5.1 Дизайн

Тканевый мешок:
- светлая грубая льняная ткань;
- тёмно-бежевые швы;
- внизу одна широкая полоса красного геометрического орнамента;
- верх визуально меняется при заполнении;
- на полном состоянии горловина собрана и перевязана тёмной верёвкой.

Footprint:
- 1 block.

Не full cube.
Collision:
- уже полного блока.

## 5.2 Назначение

Мешок предназначен для сыпучих сельскохозяйственных ресурсов.

Создать tag:
`slavicmyths:storage/sack_items`

Добавить:
- rye_grain
- barley_grain
- oat_grain
- rye_seeds
- barley_seeds
- oat_seeds
- flax_seeds
- turnip_seeds
- cabbage_seeds
- pea_seeds
- wheat_flour
- rye_flour
- oat_groats
- barley_groats
- pea_pod

Если какой-то exact id немного отличается в текущем проекте:
использовать реальный существующий id.
Не делать широкий аудит.

## 5.3 Capacity

Внутри:
- 4 logical inventory slots;
- каждый использует обычный max stack size предмета;
- мешок хранит ТОЛЬКО ОДИН item type одновременно.

Максимальный practical capacity:
обычно 256 единиц для stack-64 item.

Если уже лежит item A:
- item B вставить нельзя.

После полного опустошения:
- тип сбрасывается;
- можно положить другой допустимый item.

## 5.4 Визуальные состояния

BlockState:
`FILL = 0..4`

0:
- empty;
- мешок почти сложен;
- верх широко открыт/смят;
- высота около 0.45 блока.

1:
- 1–25% capacity;
- мешок начинает стоять;
- нижняя часть чуть округляется.

2:
- 26–50%;
- примерно половина высоты;
- горловина ещё открыта.

3:
- 51–75%;
- почти полный;
- широкий корпус;
- верх собран.

4:
- 76–100%;
- полностью стоящий наполненный мешок;
- горловина перевязана.

FILL вычисляется из реального количества items.
BlockState не является source of truth.

## 5.5 Interaction

Right click с допустимым item:
- вставить 1.

Sneak + right click с допустимым item:
- вставить максимально возможное количество из held stack.

Right click пустой рукой:
- вынуть 1 item.

Sneak + right click пустой рукой:
- вынуть один полный доступный stack.

Не создавать GUI.

Breaking:
- мешок item drop;
- содержимое drop обычными ItemEntity stacks;
- исключить duplication.

## 5.6 Рецепт

Shaped:

`CCC`
`C C`
`CTC`

C = linen_cloth
T = linen_thread

Результат:
1 storage_sack.

---

# 6. КОРЗИНА — `basket`

RU:
`Корзина`

EN:
`Basket`

Creative:
`Декор и блоки`

## 6.1 Дизайн

- небольшая плетёная прямоугольная корзина;
- 1 ручка дугой сверху;
- тёплое коричневое плетение;
- открытый верх;
- высота около 0.55 блока.

## 6.2 Допустимые предметы

Tag:
`slavicmyths:storage/basket_items`

Добавить:
- все `slavicmyths:berries`;
- minecraft:apple;
- turnip;
- cabbage;
- pea_pod;
- goose_egg;
- duck_egg;
- minecraft:egg;
- red_mushroom;
- brown_mushroom.

## 6.3 Inventory

4 slots.

Каждый slot:
- обычный max stack.

Разрешено смешивать разные допустимые предметы.

Не создавать GUI.

## 6.4 Визуал содержимого

Basket renderer показывает реальные ItemStacks.

Максимум:
- 4 визуальных representative stacks;
- по одному на каждый непустой inventory slot.

Позиции:
- внутри корзины;
- слегка выше края только для верхней части;
- scale маленький;
- не floating.

Если stack содержит ягоды:
- item-model допустим.
Если apple/egg:
- item-model.

Пустая корзина:
- абсолютно пустая внутри.

Нельзя:
- показывать декоративные яблоки, если реальных яблок нет;
- использовать fake blockstate contents.

## 6.5 Interaction

Right click допустимым item:
- вставить 1 в подходящий slot.

Sneak + right click:
- вставить максимум.

Right click пустой рукой:
- вынуть 1 из последнего непустого slot.

Sneak + right click пустой рукой:
- вынуть весь stack из последнего непустого slot.

Breaking:
- contents drop.

## 6.6 Рецепт

Shaped:

`S S`
`SPS`
`SSS`

S = stick
P = any plank

Результат:
1 basket.

---

# 7. БОЛЬШАЯ КОРЗИНА — `large_basket`

RU:
`Большая корзина`

EN:
`Large Basket`

Creative:
`Декор и блоки`

## 7.1 Дизайн

В отличие от маленькой:
- широкая;
- без высокой дуговой ручки;
- напоминает открытую плетёную хозяйственную коробку;
- плетение крупнее;
- высота около 0.7 блока;
- занимает 1 block.

## 7.2 Inventory

9 slots.

Допустимые:
- `basket_items`;
- raw grains;
- flax_stalk;
- dried_berries;
- dried_mushrooms;
- дополнительные существующие herbs только если уже существует простой tag `slavicmyths:herbs`.

НЕ проводить аудит трав ради заполнения tag.

## 7.3 Визуал

Показывать максимум 6 representative real stacks:
- 3 front positions;
- 3 back positions.

Если inventory содержит больше 6 различных непустых slots:
- показывать первые 6;
- остальные остаются реально сохранены, но не рендерятся.

Это нормально.

Никогда не показывать fake items.

## 7.4 Interaction

Точно как basket:
- 1 item;
- shift = максимум;
- empty hand = вынуть;
- shift empty = stack.

## 7.5 Рецепт

Shaped:

`S S`
`SPS`
`PSP`

S = stick
P = plank

Результат:
1 large_basket.

---

# 8. ДЕРЕВЯННЫЙ ЯЩИК — `produce_crate`

RU:
`Ящик`

EN:
`Produce Crate`

Creative:
`Декор и блоки`

## 8.1 Дизайн

- открытый деревянный ящик;
- горизонтальные планки;
- щели между досками;
- четыре угловые стойки;
- без крышки;
- цвет обычного тёплого дерева.

Не делать сундуком.

## 8.2 Items

Tag:
`slavicmyths:storage/crate_items`

Добавить:
- apple;
- turnip;
- cabbage;
- pea_pod;
- berries;
- mushrooms;
- flax_stalk;
- baked_turnip;
- dried_berries;
- dried_mushrooms.

## 8.3 Inventory

9 slots.

Смешанные допустимые предметы разрешены.

## 8.4 Визуальное содержимое

Показывать реальные items:
- до 6 representative stacks;
- scale немного крупнее, чем в basket;
- предметы визуально лежат внутри ящика.

Яблоки, овощи и грибы должны быть различимы.

Не генерировать отдельную fake texture «crate with vegetables».

Renderer — source from actual inventory.

## 8.5 Interaction

Как large_basket.

## 8.6 Рецепт

Shaped:

`P P`
`S S`
`PPP`

P = any plank
S = stick

Результат:
1 produce_crate.

---

# 9. ЛАРЬ — `household_chest`

RU:
`Ларь`

EN:
`Household Chest`

Creative:
`Декор и блоки`

## 9.1 Дизайн

Традиционный тяжёлый деревянный ларь:
- низкий и широкий;
- массивная крышка;
- 2–3 тёмные металлические полосы;
- тёмные петли;
- центральный металлический замок;
- один крупный красно-коричневый геометрический орнамент на передней панели.

Не копировать vanilla chest.

Footprint:
- 1 block;
- визуально низкий и широкий в пределах блока.

## 9.2 Inventory

27 slots.

Универсальное хранение:
- любые обычные ItemStacks;
- без category restrictions.

Это основной большой универсальный storage этой версии.

## 9.3 GUI

Использовать vanilla-style 3-row container screen.

Не создавать сложный custom UI.

Title:
RU `Ларь`
EN `Household Chest`

## 9.4 Lid

Если текущая BlockEntity animation architecture позволяет просто:
- крышка открывается при использовании;
- закрывается.

Если это требует несоразмерно сложного нового renderer:
- достаточно open/closed model state при active viewer count.

Не тратить большой объём токенов на идеальную easing animation.

## 9.5 Рецепт

Shaped:

`PPP`
`PIP`
`PPP`

P = any plank
I = iron_ingot

Результат:
1 household_chest.

---

# 10. ДЕРЕВЯННАЯ БОЧКА — `wooden_barrel`

RU:
`Деревянная бочка`

EN:
`Wooden Barrel`

Creative:
`Декор и блоки`

ВАЖНО:
в 1.1.6 это ТОЛЬКО хранилище.

Ферментация появится в 1.1.7.
НЕ реализовывать её сейчас.

## 10.1 Дизайн

- вертикальная бочка;
- тёмные/средние деревянные клёпки;
- 3 тёмных металлических обруча;
- плоская верхняя крышка;
- корпус слегка расширен посередине.

Один registry id.

## 10.2 Inventory

9 slots.

Бочка хранит только ОДИН item type одновременно.

Допустимые items:
tag
`slavicmyths:storage/barrel_items`

Добавить:
- grains;
- flour;
- groats;
- dried_berries;
- dried_mushrooms;
- apples;
- berries;
- mushrooms.

Не добавлять liquids.

## 10.3 Future-ready requirement

Код BlockEntity не должен быть написан так, чтобы 1.1.7 пришлось полностью ломать save format.

Сейчас сохранить:
- inventory.

Разрешено заранее иметь:
- data version integer;
- но НЕ создавать mode/fermentation state, пока он не используется.

НЕ добавлять:
- progress;
- liquids;
- brew type;
- temperature;
- fermentation ticks.

## 10.4 GUI

9-slot простой container.

Title:
RU `Деревянная бочка`
EN `Wooden Barrel`

## 10.5 Визуал

Бочка снаружи НЕ обязана показывать конкретное содержимое.
Она закрытая.

Можно добавить:
- `FULLNESS = 0..3` небольшим blockstate;
- subtle верхняя/корпусная разница.

Но не обязательно отображать actual item.

Если добавляется:
0 empty
1 low
2 half+
3 mostly/full

Не показывать food floating над закрытой бочкой.

## 10.6 Рецепт

Shaped:

`PNP`
`P P`
`PNP`

P = plank
N = iron_nugget

Результат:
1 wooden_barrel.

---

# 11. НАСТЕННАЯ ПОЛКА — `wall_shelf`

RU:
`Настенная полка`

EN:
`Wall Shelf`

Creative:
`Декор и блоки`

## 11.1 Дизайн

- одна деревянная горизонтальная доска;
- две маленькие подпорки;
- крепится к вертикальной стене;
- не full cube;
- толщина небольшая.

FACING:
- north/south/east/west.

Support:
- placement требует solid support block behind;
- если support уничтожен:
  - shelf ломается;
  - содержимое drop.

## 11.2 Inventory

3 display slots.

Каждый slot:
- максимум 16 items;
- может хранить любой обычный item, кроме запрещённых special/internal blocks при необходимости.

Основная роль:
- визуальный display + небольшое хранение.

## 11.3 КРИТИЧЕСКОЕ ТРЕБОВАНИЕ: ВИЗУАЛЬНО ПОКАЗЫВАТЬ ПРЕДМЕТЫ

Каждый непустой slot ОБЯЗАТЕЛЬНО рендерится на полке.

Это не декоративная имитация.

Source of truth:
- реальный ItemStack в slot.

Пустой slot:
- ничего не рендерить.

Если slot содержит:
- bowl food → показать миску/food item;
- bottle → бутылку;
- bread → хлеб;
- tool → инструмент;
- material → его обычную item model;
- block item → уменьшенную block/item model.

Renderer:
- 3 фиксированные позиции слева/центр/справа;
- предметы стоят/лежат естественно;
- без spinning;
- без bobbing;
- orientation зависит от FACING полки.

Игрок должен без GUI видеть:
**какие именно предметы лежат на полке.**

## 11.4 Interaction

Определить slot по точке клика по ширине shelf.

Right click с item:
- если targeted slot пуст -> поместить 1 item;
- если в slot тот же item и count < 16 -> добавить 1;
- иначе ничего.

Sneak + right click с item:
- вставить максимально возможное до 16.

Right click пустой рукой:
- забрать 1 item из targeted slot.

Sneak + right click пустой рукой:
- забрать весь stack targeted slot.

GUI:
- НЕ нужен.

## 11.5 Рецепт

Shaped:

`PPP`
`S S`

P = plank
S = stick

Результат:
2 wall_shelf.

---

# 12. БОЛЬШАЯ ХОЗЯЙСТВЕННАЯ ПОЛКА — `storage_shelf`

RU:
`Хозяйственная полка`

EN:
`Storage Shelf`

Creative:
`Декор и блоки`

## 12.1 Дизайн

Напольная деревянная этажерка:
- 2 горизонтальных уровня хранения;
- вертикальные стойки по бокам;
- нижняя перекладина;
- открытая конструкция;
- без дверок;
- высота примерно 1.8 блока.

Это ДВУХБЛОЧНЫЙ вертикальный block:
- LOWER;
- UPPER.

Один item размещает обе части.

FACING horizontal.

Если top position занята:
- placement fails.

Breaking любой части:
- удаляет вторую;
- drops только один раз.

## 12.2 Inventory

6 display slots:

Нижняя полка:
- slots 0,1,2.

Верхняя:
- slots 3,4,5.

Каждый:
- max 16 items.

## 12.3 ВИЗУАЛЬНОЕ СОДЕРЖИМОЕ — ОБЯЗАТЕЛЬНО

Все 6 реальных ItemStacks отображаются непосредственно на полках.

Это одно из основных требований версии.

Нельзя:
- рисовать заранее горшок, бутылку и хлеб как часть block texture;
- показывать предметы, которых нет в inventory;
- скрывать реальные предметы за generic texture.

Renderer:

нижний уровень:
- 3 равномерные позиции.

верхний:
- 3 равномерные позиции.

Предметы:
- используют actual item/block model;
- FIXED transform;
- scale адаптирован к полке;
- bowl/food item может использовать существующий table-display transform;
- blocks уменьшаются;
- tools лежат горизонтально или под небольшим углом;
- бутылки стоят вертикально.

Если нет специальной transform category:
- использовать safe generic fixed transform.

Пустой slot:
- визуально пустая доска.

Содержимое после save/load должно сразу правильно отрисовываться.

## 12.4 Interaction

Hit position определяет:
- верхний/нижний ряд;
- левый/средний/правый slot.

Right click item:
- добавить 1.

Sneak + right click:
- добавить до 16.

Right click empty:
- взять 1.

Sneak + right click empty:
- взять stack.

GUI:
- НЕ создавать.

## 12.5 Рецепт

Shaped:

`SPS`
`PPP`
`SPS`

P = plank
S = stick

Результат:
1 storage_shelf.

---

# 13. СУШИЛКА — `drying_rack`

RU:
`Сушилка`

EN:
`Drying Rack`

Creative:
`Декор и блоки`

## 13.1 Дизайн

Деревянная рама:
- 2 боковые вертикальные стойки;
- верхняя перекладина;
- нижняя stabilizing plank;
- 4 точки подвеса;
- верёвки/петли.

Размер:
- 2 blocks wide;
- 2 blocks tall.

Один item размещает multiblock:
- 2 x 2.

Master:
- lower-left относительно FACING.

Все остальные части:
- delegate master.

Если пространство не свободно:
- placement fails.

Breaking любой part:
- вся сушилка удаляется;
- items drop один раз.

## 13.2 Slots

4 independent drying slots.

Каждый slot:
- ровно 1 input item.

Все четыре могут работать одновременно.

## 13.3 Допустимые рецепты

Создать custom recipe type:
`slavicmyths:drying`

Data-driven JSON:

- ingredient;
- result;
- drying_time.

Обязательные recipes:

Любая berry из `slavicmyths:berries`
-> `dried_berries`
time:
2400 ticks.

minecraft:red_mushroom
-> `dried_mushrooms`
time:
2400.

minecraft:brown_mushroom
-> `dried_mushrooms`
time:
2400.

Не добавлять herbs recipe, если нет уже очевидного existing herb tag.
Не проводить аудит ради трав.

## 13.4 Interaction

Right click подходящим item по одной из 4 областей:
- положить 1 input в targeted empty slot.

Right click empty hand:
- если slot finished:
  - забрать result;
- если ещё сушится:
  - забрать исходный input и сбросить progress.

Sneak не нужен.

## 13.5 Визуальные стадии

Каждый slot:
- EMPTY
- FRESH
- DRYING
- DRIED

Progress:
- 0–39% → fresh-looking.
- 40–79% → darker/shrunk.
- 80–99% → almost dry.
- complete → result item visual.

Renderer:
- actual input/output;
- hanging transform;
- ягоды выглядят как небольшая связка/мешочек/группа;
- mushroom item висит на короткой нити.

Не показывать fake berries на пустой сушилке.

## 13.6 Tick

Только master BlockEntity ticks.

Для каждого occupied slot:
- progress +1 per server tick.

Если chunk unloaded:
- progress не обязан симулировать offline time.
После загрузки продолжить с сохранённого значения.

Не использовать реальное системное время.

---

# 14. СУШЁНЫЕ ЯГОДЫ — `dried_berries`

RU:
`Сушёные ягоды`

EN:
`Dried Berries`

Creative:
`Еда и кухня`

Stack:
64.

## Дизайн

16x16:
- небольшая кучка тёмно-красных/бордовых сушёных ягод;
- часть ягод фиолетово-синяя;
- меньше и темнее свежих;
- рядом короткий сухой стебелёк.

Не создавать по отдельному dried item для каждого berry type.

## Food

Nutrition:
3.

Saturation Modifier:
0.35.

Effects:
NONE.

Это лёгкий походный продукт.

---

# 15. СУШЁНЫЕ ГРИБЫ — `dried_mushrooms`

RU:
`Сушёные грибы`

EN:
`Dried Mushrooms`

Creative:
`Еда и кухня`

Stack:
64.

## Дизайн

16x16:
- 2–3 маленьких коричневых сухих ломтика;
- сморщенные края;
- один светло-бежевый срез.

Не edible напрямую в 1.1.6.

FoodProperties:
- отсутствуют.

Будущая кухня сможет использовать как ingredient.

---

# 16. РЕЦЕПТ СУШИЛКИ

Shaped:

`STS`
`S S`
`PPP`

S = stick
T = linen_thread
P = plank

Результат:
1 drying_rack.

---

# 17. СНОП РЖИ — `rye_sheaf`

RU:
`Сноп ржи`

EN:
`Rye Sheaf`

Creative:
`Земледелие и растения`

## Дизайн

- вертикальная связка ржаных колосьев;
- тёмно-золотые длинные тонкие колосья;
- стебли стянуты светлой перевязью в нижней трети;
- высота около 0.9 блока;
- не full cube.

Recipe:
- 9 rye_grain -> 1 rye_sheaf.

Reverse shapeless:
- 1 rye_sheaf -> 9 rye_grain.

Не использовать sheaf как storage BE.
Это compact storage block/item.

---

# 18. СНОП ЯЧМЕНЯ — `barley_sheaf`

RU:
`Сноп ячменя`

EN:
`Barley Sheaf`

- светлее rye;
- более плотные колосья;
- длинные заметные ости.

Recipe:
9 barley_grain -> 1 block.

Reverse:
1 -> 9 grain.

---

# 19. СНОП ОВСА — `oat_sheaf`

RU:
`Сноп овса`

EN:
`Oat Sheaf`

- светло-бежевый;
- верх не колосом, а метёлками;
- визуально сильно отличается от rye/barley.

Recipe:
9 oat_grain -> 1.

Reverse:
1 -> 9 grain.

---

# 20. СНОПЫ — BLOCK BEHAVIOR

Все три:
- floor placement;
- rotation 0/90/180/270;
- без BlockEntity;
- flammable как растительный материал;
- mineable by axe допустимо, но рукой тоже относительно быстро;
- collision узкая;
- можно ставить рядом.

Не объединять в один variant block, чтобы:
- recipes;
- creative items;
- модели;
- data
были проще и понятнее.

Не создавать больше видов.

---

# 21. СТОГ — `haystack`

RU:
`Стог`

EN:
`Haystack`

Creative:
`Декор и блоки`

Главная идея:
игрок физически хранит `minecraft:hay_block` в декоративной форме.

## 21.1 Структура

Стог занимает:
- 1 block footprint;
- до 2 blocks высотой.

Один placement:
- размещает LOWER + UPPER parts сразу;
- обе позиции должны быть свободны.

Master:
LOWER.

UPPER:
delegate.

## 21.2 Capacity

Уровень:
`LEVEL = 1..4`

Stored hay blocks:
- LEVEL 1 = 1 hay_block
- LEVEL 2 = 2 hay_block
- LEVEL 3 = 3 hay_block
- LEVEL 4 = 4 hay_block

Source of truth:
- master BE integer count 1..4.

BlockState синхронизирует LEVEL для visual.

## 21.3 Визуал

LEVEL 1:
- небольшой низкий округлый пучок;
- около 0.55 высоты.

LEVEL 2:
- выше и шире;
- около 0.9.

LEVEL 3:
- уже высокий округлый стог;
- визуал частично входит в upper block;
- около 1.4.

LEVEL 4:
- полный стог;
- около 1.8;
- округлая/коническая вершина;
- вертикальный небольшой центральный кол/стойка сверху допустим.

Цвет:
- золотистая сухая трава;
- крупные пиксельные пряди;
- не копировать hay_block texture на шар.

## 21.4 Interaction

Haystack item должен быть создан из:
- 1 minecraft:hay_block
-> 1 haystack item.

При placement:
- count=1.

Right click holding hay_block:
- если count < 4:
  - consume 1 hay_block;
  - count +1.

Если full:
- ничего.

Sneak + right click empty hand:
- если count > 1:
  - give 1 hay_block;
  - count -1.
- если count == 1:
  - give 1 hay_block;
  - удалить haystack structure полностью;
  - НЕ давать отдельный haystack item.

Breaking:
- drop EXACT stored count of minecraft:hay_block;
- не drop haystack item дополнительно.

Это storage presentation, а не duplicating conversion block.

## 21.5 Collision

Collision должна примерно соответствовать уровню:
- level1 низкая;
- level2 около 1 block;
- level3/4 используют lower+upper reasonable shapes.

Не требуется идеальная коническая collision.

---

# 22. STORAGE CATEGORIES / TAGS

Создать:

`slavicmyths:storage/sack_items`

`slavicmyths:storage/basket_items`

`slavicmyths:storage/crate_items`

`slavicmyths:storage/barrel_items`

Дополнительно:

`slavicmyths:storage/visual_display_items`

Не обязательно перечислять вообще все items.
Этот tag можно использовать только если renderer/helper требует allowlist.

Полки по умолчанию принимают почти любые обычные items, поэтому allowlist не обязателен.

---

# 23. ВАЖНО: ПОЛКИ НЕ ЯВЛЯЮТСЯ ФЕЙКОВЫМ ДЕКОРОМ

Абсолютное правило для:
- wall_shelf;
- storage_shelf.

Нельзя:
- рисовать хлеб/горшок/бутылку прямо на base texture;
- делать blockstate `HAS_FOOD=true` с generic картинкой;
- показывать один декоративный набор для всех inventory contents.

Нужно:
- хранить реальные ItemStacks;
- синхронизировать их на client;
- рендерить каждый непустой slot;
- использовать фактическую модель конкретного предмета.

Пример:
если на полке лежат:
- rye_bread;
- metal_pot;
- honey_bottle;

игрок должен визуально увидеть:
- ржаной хлеб;
- металлический горшок;
- бутылку мёда.

Если заменить rye_bread на apple_pie:
- модель на полке меняется на apple_pie.

Если slot пуст:
- там ничего нет.

Это обязательный acceptance criterion.

---

# 24. STORAGE INTERACTION CONSISTENCY

Для direct-interaction storage:

- sack;
- basket;
- large_basket;
- crate;
- wall_shelf;
- storage_shelf;

использовать одинаковые базовые правила, где они подходят:

ПКМ item:
- +1.

Shift+ПКМ item:
- максимально возможное.

ПКМ пустой:
- -1 / targeted.

Shift+ПКМ пустой:
- полный stack / targeted.

Shelf:
targeted по hit position.

Basket/crate:
last nonempty / first valid.

Не создавать шесть разных непредсказуемых schemes.

---

# 25. TOOLTIP / INFORMATION

Не добавлять огромные tooltip.

Sack:
если смотрит player и vanilla tooltip BlockItem позволяет component info:
- нет необходимости показывать contents до placement.

Barrel/chest:
GUI показывает.

Shelf/basket/crate:
визуал — основной источник информации.

Не создавать WAILA-style overlay dependency.

---

# 26. ДАННЫЕ И SAVE

## Sack
Save:
- 4 slots.

Derived:
- fill state.

## Basket
- 4 slots.

## Large basket
- 9 slots.

## Crate
- 9 slots.

## Household chest
- 27 slots.

## Wooden barrel
- 9 slots.

## Wall shelf
- 3 slots.

## Storage shelf
- 6 slots.

## Drying rack
per each 4:
- input/result stack;
- recipe id;
- progress.

## Haystack
- count 1..4.

Sheaves:
- no BE.

Не сохранять derived renderer positions.

---

# 27. NETWORK SYNC

Клиенту нужны contents для визуальных контейнеров:

- basket;
- large_basket;
- crate;
- wall_shelf;
- storage_shelf;
- drying_rack;
- sack fill only.

Sync:
- только при изменении inventory/progress visual stage;
- стандартный BlockEntity update packet;
- block update по необходимости.

Не отправлять полный inventory каждую tick.

Drying rack:
- не синхронизировать progress каждый tick.
Достаточно:
- при смене visual stage;
- complete;
- insert/remove.

---

# 28. STORAGE GUI

GUI только для:

1. `household_chest` — 27 slots.
2. `wooden_barrel` — 9 slots.

Для остальных GUI НЕ нужен.

Использовать vanilla-like screens.
Не создавать декоративный сложный UI.

---

# 29. ITEM HANDLERS / HOPPERS

Не тратить большой объём токенов на automation.

Минимально:

Household chest:
- hopper compatible как обычный container.

Wooden barrel:
- hopper compatible;
- same-item restriction должна соблюдаться.

Остальные direct visual storages:
- automation можно НЕ поддерживать в 1.1.6.

Drying rack:
- hopper automation НЕ нужна.

Это нормально и зафиксировано.

---

# 30. СУШЁНЫЕ ПРОДУКТЫ И КУХНЯ

1.1.6 НЕ расширяет Kitchen II recipes массово.

Добавить:
- dried_berries как съедобный простой item.

Dried mushrooms:
- пока material/food ingredient;
- не добавлять новые soups ради него.

Не менять старые food balance.

---

# 31. FIRE / FLAMMABILITY

Wooden storage:
- basket;
- large_basket;
- crate;
- household_chest;
- barrel;
- shelves;
- drying rack;

должны иметь разумную flammability как wood/wooden furniture, если проект уже использует стандартные flammable registration helpers.

Sack:
- flammable как cloth.

Sheaves/haystack:
- высокая flammability.

Не добавлять fireproof variants.

---

# 32. CREATIVE TAB DISTRIBUTION

## Decor & Blocks

Добавить:
- storage_sack
- basket
- large_basket
- produce_crate
- household_chest
- wooden_barrel
- wall_shelf
- storage_shelf
- drying_rack
- haystack

## Farming & Plants

Добавить:
- rye_sheaf
- barley_sheaf
- oat_sheaf

## Food & Kitchen

Добавить:
- dried_berries
- dried_mushrooms

Не создавать новую tab `Storage`.

---

# 33. LOCALIZATION

Минимум ru_ru и en_us.

RU:
- Мешок
- Корзина
- Большая корзина
- Ящик
- Ларь
- Деревянная бочка
- Настенная полка
- Хозяйственная полка
- Сушилка
- Сушёные ягоды
- Сушёные грибы
- Сноп ржи
- Сноп ячменя
- Сноп овса
- Стог
- Запасы на зиму
- Высуши ягоды для долгого хранения

EN:
- Storage Sack
- Basket
- Large Basket
- Produce Crate
- Household Chest
- Wooden Barrel
- Wall Shelf
- Storage Shelf
- Drying Rack
- Dried Berries
- Dried Mushrooms
- Rye Sheaf
- Barley Sheaf
- Oat Sheaf
- Haystack
- Winter Stores
- Dry berries for long-term storage

---

# 34. ADVANCEMENT

ID:
`winter_stores`

Parent:
- ближайший существующий Slavic Myths farming advancement;
- если не найден одним коротким search — `minecraft:husbandry/root`.

Trigger:
получить `dried_berries`.

RU:
Title:
`Запасы на зиму`

Description:
`Высуши ягоды для долгого хранения`

EN:
Title:
`Winter Stores`

Description:
`Dry berries for long-term storage`

Frame:
task.

Icon:
dried_berries.

No XP.
Not hidden.

---

# 35. РЕЦЕПТЫ — СВОДКА

storage_sack:
- linen cloth + thread.

basket:
- sticks + plank.

large_basket:
- sticks + planks.

produce_crate:
- planks + sticks.

household_chest:
- planks + iron ingot.

wooden_barrel:
- planks + iron nuggets.

wall_shelf:
- planks + sticks.

storage_shelf:
- planks + sticks.

drying_rack:
- sticks + thread + planks.

rye_sheaf:
- 9 rye grain.

barley_sheaf:
- 9 barley grain.

oat_sheaf:
- 9 oat grain.

reverse sheaves:
- 1 -> 9 grain.

haystack item:
- 1 hay_block -> 1 haystack item.

Но при breaking placed haystack:
- вернуть stored hay_blocks;
- НЕ вернуть haystack item.

Это исключает dupe.

---

# 36. BLOCK MODELS / RENDERERS

## Static model only
- sack base/fill variants;
- household chest shell;
- barrel;
- sheaves.

## BlockEntity renderer required
- basket;
- large_basket;
- produce_crate;
- wall_shelf;
- storage_shelf;
- drying_rack.

Household chest lid:
- optional simple BER/model-state.

Haystack:
- static models by LEVEL acceptable;
- no BER required unless easier.

Главный приоритет:
полки должны отображать actual items.

---

# 37. ПОЛКИ И ОСОБЫЕ ITEM TRANSFORMS

Создать маленький transform resolver:

Category examples:

BOWL/FOOD:
- horizontal;
- scale ~0.55.

BOTTLE:
- vertical;
- scale ~0.65.

TOOL:
- lay flat;
- rotation ~90 degrees;
- scale ~0.55.

BLOCK:
- upright;
- scale ~0.45.

DEFAULT:
- FIXED;
- vertical/slight angle;
- scale ~0.5.

Не нужно классифицировать каждый item вручную.

Можно использовать:
- item class/type checks;
- known tags;
- fallback.

Если existing table renderer уже содержит logic:
переиспользовать.

---

# 38. ХРАНЕНИЕ И ДЮПЫ — ЖЁСТКИЕ ПРАВИЛА

На каждом interaction:
- mutation только server-side;
- client только animation/render response.

При breaking multiblock:
- drops только master.

При save/load:
- contents не дублируются.

При chunk unload:
- никаких duplicated onLoad drops.

При replacing block:
- drop contents один раз.

При explosion:
- contents следуют обычной container semantics.

При creative breaking:
- contents всё равно не должны бесследно копироваться.
Использовать consistent container drop logic проекта.

---

# 39. МУЛЬТИБЛОКИ

## storage_shelf
2 high:
- lower master;
- upper delegate.

## drying_rack
2 wide x 2 high:
- one master;
- 3 delegates.

## haystack
2 high:
- lower master;
- upper delegate.

Перед placement:
- все required positions replaceable.

Breaking:
- whole structure cleanup.

Не оставлять orphan blocks.

Neighbor check:
- если master отсутствует → delegate self-removes safely без extra drops.

---

# 40. БОЧКА И 1.1.7

Это важно:

1.1.6 barrel — storage ONLY.

Не добавлять сейчас:
- hops;
- wort;
- mash;
- fermentation;
- timing windows;
- beverage containers.

Но:
- registry id `wooden_barrel` должен быть стабильным;
- BlockEntity inventory design не должна требовать переименования блока в 1.1.7.

В 1.1.7 мы сможем расширить same barrel class/BE.

Не предугадывать реализацию 1.1.7 больше, чем необходимо.

---

# 41. НЕ ДОБАВЛЯТЬ В 1.1.6

- beer;
- mead;
- cider;
- kvass fermentation;
- hops crop;
- fruit press;
- fermentation vat;
- aging mechanics;
- jam;
- preserves;
- salting;
- smoking;
- food spoilage;
- cellar;
- warehouse structure;
- barn structure;
- NPC storage;
- backpack;
- portable bag inventory;
- chest upgrades;
- locks/keys;
- auto sorting;
- pipes;
- conveyors;
- fluid tanks;
- warehouse network;
- search bar across containers.

---

# 42. PERFORMANCE

Запрещено:
- global storage registry;
- global scan BlockEntities;
- world tick, обходящий shelves;
- каждый tick пересылать shelf inventories;
- render ItemEntity вместо item models;
- слишком сложный dynamic model baking every frame.

Правильно:
- visual containers render local synced stacks;
- ordinary storage не ticking;
- drying rack master ticking only;
- renderer uses cached baked item models through normal Minecraft item renderer;
- network updates only on changes/stage transitions.

---

# 43. ОБНОВЛЕНИЕ ВЕРСИИ

Source of truth version:
`1.1.6`

Не создавать дублирующие version constants.

---

# 44. DATA / RESOURCE FILES

Создать всё необходимое:

- block registrations;
- item/block items;
- BlockEntity types;
- menus для chest/barrel;
- screens;
- recipes;
- custom drying recipe type/serializer;
- blockstates;
- models;
- textures;
- renderer classes;
- tags;
- lang;
- advancement;
- flammability registration при необходимости.

Не создавать runtime-generated textures.

---

# 45. BUILD

После реализации:

1. Minecraft НЕ запускать.
2. PolyMC НЕ трогать.
3. Выполнить одну финальную compile/build command.
4. Если success — закончить.
5. Если fail из-за своих изменений:
   - исправить конкретную ошибку;
   - один повторный build разрешён.
6. Если fail из-за старой независимой ошибки:
   - не ремонтировать весь проект;
   - указать точный blocker.

---

# 46. GIT FINISH

Branch:
`main`

Commit:
`feat: implement Slavic Myths 1.1.6 household storage`

Если auth уже работает:
- push origin main.

НЕ:
- release branch;
- tag;
- GitHub Release;
- force push;
- PolyMC copy.

---

# 47. КРИТЕРИИ ГОТОВНОСТИ

## Sack
[ ] storage_sack registered.
[ ] one item type only.
[ ] 4 internal slots.
[ ] only sack tag accepted.
[ ] FILL 0..4.
[ ] visual shape changes with actual fill.
[ ] no GUI.
[ ] insert/extract works.
[ ] breaking drops contents once.

## Basket
[ ] 4 slots.
[ ] mixed allowed items.
[ ] actual contents rendered.
[ ] empty slots visibly empty.
[ ] no fake fruit texture.
[ ] direct interaction works.

## Large basket
[ ] 9 slots.
[ ] up to 6 actual representative stacks rendered.
[ ] direct interaction works.
[ ] save/load works.

## Crate
[ ] 9 slots.
[ ] correct category restriction.
[ ] actual items rendered inside.
[ ] save/load/drop correct.

## Household chest
[ ] 27 slots.
[ ] universal storage.
[ ] simple container GUI.
[ ] custom chest visual.
[ ] no dupe.

## Wooden barrel
[ ] 9 slots.
[ ] one item type at a time.
[ ] no liquids.
[ ] no fermentation.
[ ] GUI.
[ ] future-stable registry id.
[ ] hopper support if easy/current container helper supports it.

## Wall shelf
[ ] wall placement.
[ ] 3 slots.
[ ] support check.
[ ] ACTUAL ITEM in every occupied slot is rendered.
[ ] three separate visible positions.
[ ] item type visibly recognizable.
[ ] empty slot renders nothing.
[ ] hit-position interaction.
[ ] no GUI.
[ ] correct save/client sync.

## Storage shelf
[ ] 2-block height.
[ ] 6 slots.
[ ] all 6 ACTUAL ItemStacks can render simultaneously.
[ ] upper/lower rows correct.
[ ] bowl/bottle/tool/block/default transforms work.
[ ] empty positions are empty.
[ ] no baked decorative fake food.
[ ] direct interaction.
[ ] multiblock breaking safe.
[ ] client sync on changes only.

## Drying rack
[ ] 2x2 multiblock.
[ ] 4 independent slots.
[ ] drying recipe type.
[ ] berries dry.
[ ] red/brown mushrooms dry.
[ ] 2400 ticks.
[ ] progress persists.
[ ] visual stage changes.
[ ] actual hanging items visible.
[ ] no fake items on empty rack.
[ ] outputs correct.

## Dried products
[ ] dried_berries.
[ ] nutrition 3.
[ ] saturation modifier 0.35.
[ ] no effect.
[ ] dried_mushrooms.
[ ] dried mushrooms not edible.

## Sheaves
[ ] rye_sheaf.
[ ] barley_sheaf.
[ ] oat_sheaf.
[ ] visually distinct.
[ ] 9 grain craft.
[ ] reversible to 9 grain.
[ ] no BE needed.

## Haystack
[ ] 2-block structure.
[ ] 1..4 hay capacity.
[ ] four visual sizes.
[ ] right-click hay adds.
[ ] sneak empty removes.
[ ] breaking returns exact hay count.
[ ] no haystack item dupe.
[ ] full state stops accepting hay.

## Creative / data
[ ] correct tabs.
[ ] ru_ru.
[ ] en_us.
[ ] tags.
[ ] recipes.
[ ] advancement.
[ ] flammability.
[ ] save/load.
[ ] network sync.

## Workflow
[ ] tokens used economically.
[ ] no full audit.
[ ] Minecraft not launched.
[ ] PolyMC untouched.
[ ] version 1.1.6.
[ ] branch main.
[ ] no release/tag.
[ ] only required build(s).

---

# 48. MANUAL TEST CHECKLIST ДЛЯ ПОЛЬЗОВАТЕЛЯ

Codex НЕ выполняет игровые тесты.

В финальном отчёте вывести:

1. Заполнить мешок до каждого из 4 визуальных уровней.
2. Опустошить мешок и положить другой допустимый тип.
3. Положить ягоды/яблоки/яйца в basket и проверить реальные 3D-модели.
4. Проверить large_basket с несколькими разными предметами.
5. Проверить produce_crate с овощами/яблоками.
6. Открыть ларь и проверить 27 slots.
7. Проверить one-item restriction деревянной бочки.
8. Поставить wall_shelf и положить 3 разных предмета.
9. Убедиться, что на wall_shelf видны именно реальные предметы.
10. Поставить storage_shelf и заполнить все 6 мест разными items.
11. Проверить визуально bowl, bottle, tool и block item на полке.
12. Перезайти в мир и убедиться, что содержимое/визуал полок сохранились.
13. Высушить ягоды.
14. Высушить оба vanilla mushroom.
15. Прервать сушку и вернуть исходный item.
16. Проверить 3 вида снопов и обратный craft.
17. Создать haystack и добавить 4 hay blocks.
18. Извлекать hay обратно по одному.
19. Сломать заполненные storages и убедиться в отсутствии dupe/loss.
20. Проверить Creative Tabs, localization и advancement.

---

# 49. ФОРМАТ ИТОГОВОГО ОТЧЁТА CODEX

Коротко.

## Implemented
5–12 bullets.

## Build
- command;
- success/fail;
- точный blocker при fail.

## Integration notes
Только если реально применимо:
- table item-render helper reused / small storage render helper created.

## Git
- branch;
- commit hash;
- push status.

## Manual test checklist
Короткий список.

НЕ писать:
- длинный анализ;
- chain-of-thought;
- предложения для 1.1.7;
- аудит проекта;
- новые feature ideas.
