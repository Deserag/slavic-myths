# SLAVIC MYTHS 1.1.5 — «КУХНЯ II»
## Финальный design document + implementation prompt для Codex

---

# 0. РЕЖИМ РАБОТЫ CODEX — ОБЯЗАТЕЛЬНО

Это точное ТЗ. Дизайн и механики уже определены.
Codex должен реализовать их, а не проектировать новую версию самостоятельно.

## 0.1 ЭКОНОМИЯ ТОКЕНОВ — КРИТИЧЕСКОЕ ПРАВИЛО

Токены расходовать только на работу, которая непосредственно нужна для 1.1.5.

ЗАПРЕЩЕНО:
- проводить общий аудит Slavic Myths;
- читать весь репозиторий подряд;
- анализировать мобов, боссов, структуры и системы, не связанные с кухней;
- повторно анализировать реализованные 1.1.0–1.1.4 целиком;
- делать архитектурный отчёт перед началом работы;
- долго рассуждать вместо правок;
- искать «что ещё можно улучшить»;
- рефакторить работающий старый код без прямой необходимости;
- читать десятки файлов «на всякий случай»;
- выполнять повторные одинаковые поиски;
- запускать много сборок;
- тратить токены на перенос в PolyMC;
- самостоятельно добавлять дополнительные блюда/ингредиенты/блоки.

Разрешён минимальный просмотр:
- существующего обычного стола;
- существующего kitchen table / rolling pin / pot, если они уже зарегистрированы;
- существующей мельницы;
- item/block registries;
- creative tabs;
- crops/berries/animal products из 1.1.0–1.1.4;
- текущих BlockEntity / menu / recipe conventions;
- существующей ухи;
- существующих food items, только если они нужны для интеграции с новым столом.

Принцип:
**найти конкретную точку интеграции → реализовать → перейти дальше.**

## 0.2 НЕ ЗАПУСКАТЬ MINECRAFT

НЕ запускать:
- `runClient`;
- `runServer`;
- Minecraft;
- тестовый мир;
- PolyMC.

Пользователь выполняет все игровые тесты вручную.

Разрешается:
- одна финальная compile/build-проверка;
- максимум один повторный build после исправления ошибки, вызванной именно изменениями 1.1.5.

## 0.3 POLYMC НЕ ТРОГАТЬ

После реализации:
- не копировать JAR;
- не менять instance;
- не обновлять mods directory;
- не запускать PolyMC.

Перенос сборки будет отдельной будущей командой пользователя.

## 0.4 НИКАКОЙ САМОСТОЯТЕЛЬНОЙ ФАНТАЗИИ

Не добавлять:
- новые культуры;
- новые ягоды;
- новые животные;
- новые напитки;
- алкоголь;
- ферментацию;
- новые виды мебели кроме прямо описанного kitchen table;
- новый обычный стол;
- редизайн существующего обычного стола;
- печь/плиту/духовку;
- новые эффекты еды;
- пищевые баффы;
- новые GUI помимо одного точного kitchen table screen;
- новую валюту;
- NPC;
- торговлю;
- систему качества еды;
- порчу еды;
- температуру еды.

Если технически есть несколько путей — выбрать самый простой устойчивый вариант, который буквально сохраняет заданное поведение.

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
- push в `origin/main`, если авторизация уже работает;
- не трогать release branch;
- не создавать tag;
- не создавать/обновлять GitHub Release;
- не force-push;
- не удалять чужие изменения.

Commit message:
`feat: implement Slavic Myths 1.1.5 Kitchen II`

Если push требует отдельной настройки авторизации:
- не тратить токены;
- оставить локальный commit;
- дать одну команду push в итоговом отчёте.

---

# 1. ЦЕЛЬ ВЕРСИИ

1.1.5 превращает хозяйственные ресурсы предыдущих версий в полноценную кухню.

Версия должна связать:

1.1.0:
- wheat;
- rye;
- oat;
- barley;
- turnip;
- cabbage;
- peas.

1.1.2:
- raspberry;
- blueberry;
- blackcurrant;
- lingonberry;
- cranberry;
- vanilla apple, собираемое с apple tree.

1.1.3:
- chicken egg;
- goose egg;
- duck egg;
- vanilla milk;
- goat milk;
- goose meat;
- duck meat;
- goat meat.

1.1.4:
- animal_fat.

Новые механики:
- переработка зерна;
- flour/groats;
- dough;
- новый двухблочный кухонный стол;
- rolling pin;
- metal cooking pot;
- несколько типов кухонных рецептов;
- многопорционные блюда из pot;
- полноценные блюда;
- визуальное приготовление;
- расширение существующего обычного стола:
  - два стола могут объединяться;
  - еду можно выкладывать на поверхность;
  - еда отображается в 3D.

---

# 2. ВИЗУАЛЬНЫЕ РЕФЕРЕНСЫ

Использовать:

`references/01_kitchen_ingredients_and_simple_foods.png`

`references/02_kitchen_soups_bliny_pies.png`

Это DESIGN REFERENCES, а не готовые игровые текстуры.

ВАЖНО:
реальные игровые assets должны быть заметно ближе к vanilla Minecraft по пиксельной плотности.

---

# 3. ОБЩЕЕ ПРАВИЛО СТИЛЯ 1.1.5

Пользователь отдельно потребовал снизить избыточную детализацию.

Поэтому:

## 3.1 Item sprites

Предпочтительно:
- 16x16 для простых ингредиентов и еды.

Разрешено:
- 32x32 только если конкретный предмет реально теряет читаемость в 16x16.

Не использовать высокое разрешение только потому, что reference подробный.

## 3.2 Block textures

Основная цель:
- vanilla-like texel density;
- примерно 16 pixels на один block-side по визуальному языку Minecraft.

Если resource технически 32x32:
- детали всё равно должны быть крупными;
- не превращать поверхность в мелкую HD-мозаику.

## 3.3 Запрещено

- blur;
- anti-aliasing;
- фотографические текстуры;
- мелкая AI-детализация;
- сложные градиенты;
- слишком много пиксельного шума.

Новые блюда должны выглядеть как Minecraft food, а не как уменьшенные иллюстрации из reference.

---

# 4. ВАЖНО: СУЩЕСТВУЮЩИЙ ОБЫЧНЫЙ СТОЛ НЕ ПЕРЕРИСОВЫВАТЬ

В проекте уже существует обычный стол.

Он используется:
- игроками;
- существующими структурами;
- в том числе разбойниками.

Поэтому:

ЗАПРЕЩЕНО:
- менять его базовую текстуру;
- менять цвет дерева;
- добавлять на него славянские полотенца;
- превращать его визуально в кухонную мебель;
- заменять registry id;
- создавать новый decorative table вместо существующего;
- ломать старые world placements.

Нужно модифицировать именно существующий table block.

Разрешённые изменения:
1. connected model для двух соседних столов;
2. food placement;
3. BlockEntity для хранения отображаемой еды;
4. дополнительные blockstate properties, необходимые механике.

Одиночный пустой стол должен выглядеть практически точно так же, как до 1.1.5.

---

# 5. СОЕДИНЕНИЕ ДВУХ ОБЫЧНЫХ СТОЛОВ

## 5.1 Общая идея

Если два одинаковых существующих обычных стола находятся непосредственно рядом по горизонтали:
- они могут стать одной визуально длинной конструкцией;
- столешница выглядит непрерывной;
- внутренних ножек нет;
- у всей двухблочной конструкции визуально остаётся 4 наружные ножки.

## 5.2 Максимальный размер

Только:
- SINGLE;
- PAIR.

НЕ создавать цепочки 3+.

Если уже существует пара:
- третий соседний стол остаётся SINGLE.

## 5.3 Определение пары

Добавить connection property, например enum:

- NONE
- NORTH
- SOUTH
- EAST
- WEST

Значение указывает, где находится partner table.

Алгоритм при placement/neighbor update:

1. Проверить 4 cardinal neighbors.
2. Eligible neighbor:
   - тот же exact table block;
   - его connection = NONE.
3. Если найден РОВНО ОДИН eligible neighbor:
   - соединить пару взаимно.
4. Если найдено 0:
   - оставить SINGLE.
5. Если найдено более одного:
   - не создавать неоднозначную пару автоматически;
   - оставить новый table SINGLE.
6. Никогда не присоединяться к table, который уже является частью пары.

Если один table из пары уничтожен:
- второй НЕ ломается;
- connection второго становится NONE;
- он снова использует старую одиночную модель.

## 5.4 Модель

SINGLE:
- использовать существующую модель практически без изменений.

PAIRED:
- использовать те же существующие textures;
- скрыть две внутренние ножки на каждой соответствующей стороне;
- наружные четыре ножки остаются;
- столешница визуально соединена.

НЕ добавлять:
- новые скатерти;
- новый орнамент;
- новые цвета.

Это функциональный апгрейд существующей мебели, не редизайн.

---

# 6. ЕДА НА ОБЫЧНОМ СТОЛЕ

## 6.1 BlockEntity

Существующий table получает/расширяет BlockEntity.

Каждый физический table block хранит:
- 4 food display slots.

PAIR:
- не объединяет inventory технически;
- каждая половина хранит свои 4 slots;
- итого визуально доступно 8 мест.

Это намеренно:
- меньше сложной master/slave логики;
- меньше риска дюпов;
- меньше токенов и кода;
- визуально игрок всё равно получает один длинный стол.

## 6.2 Позиции еды

На каждом table block:
4 позиции поверхности:

- back-left;
- back-right;
- front-left;
- front-right.

Позиции должны:
- находиться немного выше столешницы;
- не пересекаться;
- оставлять небольшой отступ от краёв.

В paired model позиции не меняются:
- две половины вместе дают ровную сервировку 2x4.

## 6.3 Какие предметы можно положить

Создать item tag:

`slavicmyths:placeable_table_foods`

Обязательно добавить все новые готовые блюда 1.1.5.

Также добавить:
- minecraft:apple
- minecraft:bread
- minecraft:cooked_beef
- minecraft:cooked_porkchop
- minecraft:cooked_mutton
- minecraft:cooked_chicken
- minecraft:cooked_rabbit
- существующие cooked_goose
- cooked_duck
- cooked_goat
- существующую ukha, если она уже есть.

Не проводить аудит всей старой еды проекта только ради этого tag.
Tag остаётся расширяемым в будущих версиях.

## 6.4 Положить еду

Right click по столу с item из `placeable_table_foods`:

1. По hit position определить ближайший из 4 surface slots.
2. Если этот slot пуст:
   - переместить туда ровно 1 item.
3. Если занят:
   - выбрать ближайший свободный slot в пределах этого block.
4. Если свободных нет:
   - ничего не делать.
5. Creative:
   - stack в руке не уменьшается.

## 6.5 Забрать еду

Right click ПУСТОЙ рукой:
- определить ближайший занятый slot к hit position;
- вернуть ItemStack игроку;
- если inventory full -> drop у ног;
- очистить slot.

## 6.6 Съесть прямо со стола

Sneak + right click ПУСТОЙ рукой:

Если ближайший occupied item edible:
- если player может есть по обычным vanilla rules:
  - съесть item прямо из table slot;
  - применить его обычные nutrition/saturation;
  - slot очистить;
  - если food имеет container после еды:
    - bowl/bottle и т.п. вернуть в player inventory;
    - при full inventory drop рядом.
- если player не может есть:
  - ничего не делать.

Не использовать отдельный eating GUI.

---

# 7. 3D DISPLAY ЕДЫ НА СТОЛЕ

Это обязательная новая механика.

## 7.1 Общий renderer

Table BlockEntityRenderer:
- рендерит фактический ItemStack каждого slot;
- item всегда визуально находится на столе;
- поворот/scale фиксированы;
- без floating/spinning как dropped item;
- без bobbing.

## 7.2 Новые блюда 1.1.5

Для каждого нового блюда создать отдельный простой table-display model или dedicated render transform.

Обязательная форма:

Rye bread:
- небольшая буханка.

Karavai:
- круглый хлеб с простым верхним узором.

Baked turnip:
- одна запечённая репа.

Bowled dishes:
- реальная 3D-миска;
- внутри простой цветной слой содержимого.

Bliny:
- стопка из 3 тонких блинов;
- варианты начинки отличаются 2–4 крупными цветными элементами.

Pies:
- круглый пирог;
- яблочный — светлая решётка;
- ягодный — несколько красно-синих точек;
- мясной — тёмная прорезь/начинка;
- капустный — светло-зелёная начинка в разрезе.

Не создавать высокополигональные модели.
Всё должно читаться с расстояния в несколько блоков.

## 7.3 Fallback

Для поддерживаемой еды без dedicated table model:
- render её обычный item model;
- использовать FIXED transform;
- положить горизонтально/слегка наклонить;
- не генерировать новую модель автоматически.

---

# 8. НОВЫЙ ДВУХБЛОЧНЫЙ КУХОННЫЙ СТОЛ

Registry id:
`kitchen_table`

Если такой registry id уже существует:
- НЕ создавать дубликат;
- расширить существующий блок;
- сохранить id;
- обновить его механику до этого ТЗ.

Если kitchen table полностью отсутствует:
- создать его с этим id.

RU:
`Кухонный стол`

EN:
`Kitchen Table`

Creative tab:
`Еда и кухня`

## 8.1 Размер

Один item размещает конструкцию:
- длина 2 blocks;
- ширина 1 block;
- обе половины на одном Y.

Properties:
- FACING horizontal;
- PART = LEFT / RIGHT.

Положение определяется направлением игрока.

Перед placement:
- обе позиции должны быть replaceable;
- если вторая занята -> placement fails;
- ничего не разрушать автоматически.

Breaking:
- breaking любой половины удаляет вторую;
- items/inventory drops только один раз;
- исключить duplication.

## 8.2 Дизайн

Это НОВЫЙ кухонный объект, поэтому его можно визуально отличать от ordinary table.

Однако сохранять Minecraft-style.

Внешность:
- широкая деревянная рабочая поверхность на 2 blocks;
- толстая ровная столешница;
- 4 наружные деревянные ножки;
- нижняя полка;
- одна небольшая секция хранения/полка снизу;
- несколько крупных утилитарных деталей;
- без мелкой HD-резьбы.

Допускается:
- небольшое сложенное полотенце сбоку;
- максимум одна простая красная полоска/узор.

Не делать его похожим на обычный разбойничий table.

Левая половина:
- основная зона rolling pin / dough.

Правая половина:
- место metal pot.

---

# 9. KITCHEN TABLE — BLOCKENTITY И INVENTORY

Использовать ОДИН master BlockEntity.

Master:
- выбрать LEFT part как master.

RIGHT:
- всегда делегирует interaction/inventory master.

Хранить:

Tool slots:
0. Rolling Pin
1. Metal Pot

Ingredient slots:
2–5. Четыре ingredient slots.

Output slot:
6. Для non-pot recipes.

Container return slots:
7–8. Два slots для:
- empty bucket;
- glass bottle;
- других explicit crafting remainders.

Pot state:
- currentDishId / enum/resource id;
- servingsRemaining;
- maxServings;
- processProgress.

Non-pot process:
- activeRecipeId;
- processProgress.

Не хранить recipe choice пользователя вручную:
- recipe определяется автоматически по ingredient set.

---

# 10. KITCHEN TABLE SCREEN

Создать один простой screen/menu.

Структура:

Левая верхняя часть:
- Rolling Pin tool slot.

Правая верхняя:
- Metal Pot tool slot.

Центр:
- 2x2 ingredient grid.

Справа:
- output slot.

Ниже output:
- 2 маленьких return-container slots.

Низ/центр:
- progress arrow/bar.

Если pot содержит готовое блюдо:
показывать:
- icon блюда;
- `Порций: X/Y`
- EN `Servings: X/Y`

Не выводить длинные descriptions.

UI не должен иметь:
- overlapping text;
- мелкий трудночитаемый font;
- десяток кнопок.

Recipe запускается автоматически при совпадении.

---

# 11. TOOL: СКАЛКА — `rolling_pin`

Если уже существует:
- reuse exact item;
- не создавать дубликат.

RU:
`Скалка`

EN:
`Rolling Pin`

Creative tab:
`Еда и кухня`

Stack:
1.

Durability:
- отсутствует.

## Дизайн

16x16 preferred.

- длинный деревянный цилиндр;
- центральная более толстая часть;
- две короткие ручки;
- 2–3 оттенка тёплого дерева;
- без металлических элементов;
- очень простой Minecraft silhouette.

## Рецепт

Shaped horizontal:

`SPS`

S = stick
P = any plank

Результат:
1 rolling_pin.

Rolling Pin:
- не расходуется рецептами;
- должен находиться в tool slot для ROLLING recipes.

---

# 12. TOOL: МЕТАЛЛИЧЕСКИЙ ГОРШОК — `metal_pot`

Если уже существует:
- reuse;
- не создавать duplicate.

RU:
`Металлический горшок`

EN:
`Metal Cooking Pot`

Creative tab:
`Еда и кухня`

Stack:
1.

Durability:
- отсутствует.

## Дизайн

16x16 item sprite.

Placed visual on kitchen table:
- low-poly/modelled pot.

Форма:
- тёмный металлический цилиндрический корпус;
- немного шире сверху;
- две короткие боковые ручки;
- внутренняя часть очень тёмная;
- светло-серый верхний край;
- не похож на cauldron block;
- не слишком большой.

## Рецепт

Shaped:

`I I`
`I I`
` I `

I = iron_ingot

Результат:
1 metal_pot.

Pot:
- не расходуется;
- должен быть установлен для POT recipes.

---

# 13. КАК УСТАНОВИТЬ / СНЯТЬ КУХОННЫЕ ИНСТРУМЕНТЫ

Основной способ:
- через kitchen table GUI tool slots.

Дополнительно быстрый способ:

Right click rolling_pin по LEFT half:
- если tool slot rolling pin пуст -> установить 1.

Right click metal_pot по RIGHT half:
- если pot tool slot пуст -> установить 1.

Sneak + right click пустой рукой по соответствующей половине:
- если tool не используется активным recipe:
  - снять tool в inventory;
  - если full -> drop.

Не снимать pot:
- если servingsRemaining > 0;
- если POT recipe active.

Actionbar:
RU:
`Сначала заберите готовое блюдо`
EN:
`Serve the prepared dish first`

---

# 14. PROCESSING TIMES

ROLLING/PREP recipes:
- 80 ticks = 4 seconds.

Bread/Karavai:
- 120 ticks = 6 seconds.

POT recipes:
- 160 ticks = 8 seconds.

Не добавлять fuel.

Kitchen Table в 1.1.5 механически является законченным cooking workstation.
Не создавать отдельную плиту/печь.

Это игровая абстракция и она зафиксирована.

---

# 15. ВИЗУАЛЬНОЕ ПРИГОТОВЛЕНИЕ

Во время работы kitchen table:

ROLLING recipe:
- на левой половине виден rolling pin;
- под ним простой плоский dough shape;
- небольшое движение rolling pin вперёд-назад допустимо.

PIE/BREAD:
- после половины progress dough model меняется на более оформленный shape.

POT:
- metal pot виден на правой половине;
- во время ACTIVE над ним появляются редкие smoke/steam particles;
- не использовать dense particles;
- содержимое pot можно показывать простым цветным верхним слоем.

Когда блюдо готово:
- pot остаётся;
- steam уменьшается;
- можно подавать порции.

---

# 16. МЕЛЬНИЦА — НЕ СОЗДАВАТЬ НОВУЮ БЕЗ НЕОБХОДИМОСТИ

В проекте ожидается существующая мельница / система помола.

Codex должен:

1. Сделать ОДИН минимальный поиск registry/class по:
   - mill;
   - мельница;
   - grinder/quern только если `mill` ничего не дал.
2. Если существующая мельница найдена:
   - не редизайнить;
   - добавить новые processing recipes.
3. Если мельницы действительно нет:
   - НЕ придумывать новый mill block и model;
   - добавить временные shapeless processing recipes из раздела ниже;
   - в финальном отчёте одной строкой написать:
     `Existing mill was not present; fallback crafting recipes were used.`
   - не задавать пользователю вопрос;
   - не останавливать всю реализацию.

---

# 17. НОВЫЕ ИНГРЕДИЕНТЫ

## 17.1 `wheat_flour`

RU: `Пшеничная мука`
EN: `Wheat Flour`
Creative: `Ресурсы и материалы`
Stack: 64.

Design:
- маленький светло-бежевый мешочек/кучка муки;
- пшеница как маленький символ;
- 16x16;
- бело-кремовая мука.

Processing:
- 2 minecraft:wheat -> 1 wheat_flour.

## 17.2 `rye_flour`

RU: `Ржаная мука`
EN: `Rye Flour`
Creative: `Ресурсы и материалы`
Stack: 64.

Design:
- более тёмная серо-бежево-коричневая мука;
- маленький ржаной колос;
- форма отличается от wheat_flour не только hue.

Processing:
- 2 rye_grain -> 1 rye_flour.

## 17.3 `oat_groats`

RU: `Овсяная крупа`
EN: `Oat Groats`
Creative: `Ресурсы и материалы`
Stack: 64.

Design:
- небольшая деревянная миска;
- крупные светло-бежевые овальные крупинки;
- 1 маленький овсяный фрагмент сбоку.

Processing:
- 2 oat_grain -> 1 oat_groats.

## 17.4 `barley_groats`

RU: `Ячменная крупа`
EN: `Barley Groats`
Creative: `Ресурсы и материалы`
Stack: 64.

Design:
- миска похожего размера, но grains чуть темнее и круглее;
- рядом короткий barley awn fragment.

Processing:
- 2 barley_grain -> 1 barley_groats.

## 17.5 `dough`

RU: `Тесто`
EN: `Dough`
Creative: `Ресурсы и материалы`
Stack: 64.

Design:
- один округлый светло-кремовый комок;
- слегка приплюснут;
- 2–3 крупных пиксельных пятна;
- не выглядит как clay ball.

Kitchen recipe:
- wheat_flour x2
- water_bucket x1
- rolling_pin required

Output:
- 2 dough.

Return:
- 1 empty bucket.

---

# 18. ОБЩИЕ COOKING TAGS

Создать/использовать:

`slavicmyths:cooking/eggs`
- minecraft:egg
- goose_egg
- duck_egg

`slavicmyths:cooking/milk`
- minecraft:milk_bucket
- goat_milk_bucket

`slavicmyths:cooking/berries`
- использовать существующий `slavicmyths:berries`, если он есть;
- НЕ дублировать tag без необходимости.

`slavicmyths:cooking/raw_meats`
- minecraft:beef
- minecraft:porkchop
- minecraft:mutton
- minecraft:chicken
- minecraft:rabbit
- raw_goose
- raw_duck
- raw_goat

`slavicmyths:cooking/cooked_meats`
- cooked_beef
- cooked_porkchop
- cooked_mutton
- cooked_chicken
- cooked_rabbit
- cooked_goose
- cooked_duck
- cooked_goat

`slavicmyths:cooking/mushrooms`
- red_mushroom
- brown_mushroom

`slavicmyths:cooking/fish`
- minecraft edible fish items;
- существующие edible pike/carp Slavic Myths, если ids легко находятся одним registry search.

Не читать всю water arc ради этого.

---

# 19. CONTAINER REMAINDERS

Kitchen recipes обязаны корректно возвращать containers.

milk bucket / goat milk bucket:
- -> bucket.

water bucket:
- -> bucket.

honey bottle:
- -> glass_bottle.

Перед запуском recipe:
- проверить, что 2 return slots смогут принять все различающиеся remainder stacks.
- если не смогут:
  - recipe НЕ запускается.

Не drop containers каждый recipe прямо в мир.

---

# 20. FOOD EFFECT POLICY

В 1.1.5 НИ ОДНО новое блюдо НЕ даёт potion/status effect.

Это специально.

Ценность кухни:
- nutrition;
- saturation;
- удобство;
- использование хозяйственных ресурсов.

НЕ добавлять:
- regeneration;
- speed;
- resistance;
- luck;
- strength;
- Well Rested;
- временные баффы.

---

# 21. ВАЖНО О SATURATION VALUES

Все значения ниже в колонке `Saturation Modifier` — это именно значение FoodProperties.saturationModifier.

Не интерпретировать его как прямое число saturation points.

---

# 22. ПРОСТЫЕ БЛЮДА

## 22.1 Ржаной хлеб — `rye_bread`

Design:
- тёмная овальная буханка;
- три светлые диагональные трещины/надреза;
- срез чуть светлее корки;
- не recolor vanilla bread.

Nutrition: 6
Saturation Modifier: 0.70
Effects: NONE
Stack: 64

Kitchen recipe:
- rye_flour x3
- water_bucket x1
- rolling_pin required

Process: 120 ticks
Output: 1 rye_bread
Return: 1 bucket

## 22.2 Каравай — `karavai`

Design:
- круглый праздничный хлеб;
- золотисто-коричневая корка;
- сверху простой декоративный wheat/star pattern из теста;
- узор крупный, 16px-friendly.

Nutrition: 8
Saturation Modifier: 0.85
Effects: NONE
Stack: 16

Recipe:
- wheat_flour x3
- any cooking egg x1
- any cooking milk bucket x1
- honey_bottle x1
- rolling_pin required

Process: 120 ticks
Output: 1 karavai
Return: bucket + glass bottle

## 22.3 Печёная репа — `baked_turnip`

Design:
- целая/крупно разрезанная репа;
- золотистая поверхность;
- тёмный край;
- маленький остаток зелени сверху.

Nutrition: 4
Saturation: 0.50
Effects: NONE
Stack: 64

Recipe:
- furnace: turnip -> baked_turnip
- smoker тоже поддерживается
- XP: 0.35

---

# 23. POT DISH SYSTEM

POT recipe НЕ кладёт готовый food item в output slot.

После завершения kitchen table хранит:
- dish id;
- servingsRemaining.

Чтобы получить порцию:
Right click kitchen table / GUI interaction с `minecraft:bowl`:
- если servings > 0:
  - consume 1 bowl;
  - give соответствующий bowl-food item;
  - servings -1.

Creative:
- bowl не уменьшается.

Когда servings=0:
- pot становится пустым;
- новый POT recipe можно запускать.

Если player без bowl:
- порцию забрать нельзя.

Breaking kitchen table при готовом pot dish:
- содержимое pot ТЕРЯЕТСЯ;
- не создавать бесплатные bowl-items;
- pot item и все реальные inventory items drop как обычно.

---

# 24. ТУШЁНАЯ РЕПА — `stewed_turnip`

Item:
- bowl meal;
- eating returns minecraft:bowl.

Design:
- миска;
- несколько золотистых кубиков репы;
- небольшие зелёные точки трав.

Nutrition: 6
Saturation: 0.65
Effects: NONE
Stack: 1

Pot recipe:
- turnip x2
- animal_fat x1
- water_bucket x1

Process: 160 ticks
Servings: 2
Return: bucket

---

# 25. ТУШЁНАЯ КАПУСТА — `stewed_cabbage`

Design:
- миска;
- светло-зелёные полосы капусты;
- несколько тёмно-зелёных пикселей;
- немного золотистого оттенка от тушения.

Nutrition: 6
Saturation: 0.70
Effects: NONE
Stack: 1

Pot recipe:
- cabbage x2
- animal_fat x1
- water_bucket x1

Servings: 2
Return: bucket

---

# 26. ОВСЯНАЯ КАША — `oat_porridge`

Design:
- миска;
- светлая кремово-бежевая густая масса;
- 4–6 крупных зерновых фрагментов.

Nutrition: 6
Saturation: 0.65
Effects: NONE
Stack: 1

Recipe:
- oat_groats x2
- water_bucket ИЛИ любое item из cooking/milk

Servings: 3
Return: соответствующий empty bucket

---

# 27. ЯЧМЕННАЯ КАША — `barley_porridge`

Design:
- миска;
- немного темнее oat porridge;
- grains чуть крупнее и более коричневые.

Nutrition: 6
Saturation: 0.65
Effects: NONE
Stack: 1

Recipe:
- barley_groats x2
- water_bucket ИЛИ cooking/milk

Servings: 3

---

# 28. КАША С ЯГОДАМИ — `berry_porridge`

Design:
- светлая oat porridge base;
- сверху 4–6 крупных красных/синих ягодных точек.

Nutrition: 7
Saturation: 0.75
Effects: NONE
Stack: 1

Recipe:
- oat_groats x2
- berries x2
- water_bucket ИЛИ cooking/milk

Servings: 3

Не создавать отдельные варианты по каждой ягоде.

---

# 29. СУПЫ И ПОХЛЁБКИ

## 29.1 `vegetable_stew`
RU: `Овощная похлёбка`
EN: `Vegetable Pottage`

Design:
- светлый бульон;
- крупные кусочки репы, капусты и гороха.

Nutrition: 7
Saturation: 0.70
Effects: NONE
Stack: 1

Recipe:
- turnip x1
- cabbage x1
- pea_pod x2
- water_bucket x1

Servings: 3

## 29.2 `meat_stew`
RU: `Мясная похлёбка`
EN: `Meat Pottage`

Design:
- более тёмный бульон;
- 2–3 крупных мясных кубика;
- репа и капуста.

Nutrition: 9
Saturation: 0.85
Effects: NONE
Stack: 1

Recipe:
- raw meat tag x2
- turnip x1
- cabbage x1
- water_bucket x1

Servings: 3

## 29.3 `pea_soup`
RU: `Гороховый суп`
EN: `Pea Soup`

Design:
- зелёно-золотой густой суп;
- несколько заметных горошин.

Nutrition: 7
Saturation: 0.75
Effects: NONE
Stack: 1

Recipe:
- pea_pod x3
- barley_groats x1
- water_bucket x1

Servings: 3

## 29.4 `mushroom_stew_slavic`
RU: `Грибная похлёбка`
EN: `Mushroom Pottage`

Design:
- кремово-коричневый бульон;
- 2–3 ломтика mushroom;
- зелёные травяные точки.

Nutrition: 6
Saturation: 0.70
Effects: NONE
Stack: 1

Recipe:
- cooking mushrooms x2
- barley_groats x1
- water_bucket x1

Servings: 3

## 29.5 Уха

Сначала минимально найти существующий item/recipe ухи.

Если существующая `ukha` уже есть:
- НЕ создавать второй item;
- использовать её;
- привести nutrition к 8 и saturation к 0.80, если изменение не ломает важную существующую систему;
- добавить kitchen pot recipe.

Если item реально отсутствует:
создать `ukha`.

Design:
- светлый рыбный бульон;
- заметный кусок рыбы;
- маленькие кубики репы;
- один зелёный травяной акцент.

Nutrition: 8
Saturation: 0.80
Effects: NONE
Stack: 1

Recipe:
- cooking fish x2
- turnip x1
- water_bucket x1

Servings: 3

---

# 30. БЛИНЫ — ОБЩИЙ ВИЗУАЛ

Все bliny:
- тонкие круглые блины;
- золотистые;
- лёгкие коричневые spots;
- не американские толстые pancakes.

Table model:
- стопка примерно из 3 тонких discs.

---

# 31. ОБЫЧНЫЕ БЛИНЫ — `bliny`

Nutrition: 6
Saturation: 0.65
Effects: NONE
Stack: 16

Recipe:
- wheat_flour x2
- cooking egg x1
- cooking milk x1
- rolling_pin

Process: 80 ticks
Output: 2 bliny
Return: bucket

---

# 32. БЛИНЫ С ЯГОДАМИ — `berry_bliny`

Design:
- стопка/сложенные bliny;
- 3–4 крупные ягодные точки;
- немного красно-фиолетовой начинки.

Nutrition: 8
Saturation: 0.80
Effects: NONE
Stack: 16

Recipe:
- bliny x1
- berries x2
- rolling_pin

Output: 1

---

# 33. БЛИНЫ С МЯСОМ — `meat_bliny`

Design:
- 2 свёрнутых блина;
- один открытый край;
- внутри крупные тёмно-коричневые meat pixels.

Nutrition: 9
Saturation: 0.85
Effects: NONE
Stack: 16

Recipe:
- bliny x1
- cooked meat x1
- rolling_pin

Output: 1

---

# 34. БЛИНЫ С ЯБЛОКОМ — `apple_bliny`

Design:
- два свёрнутых блина;
- внутри светло-золотистые apple cubes;
- рядом маленький красный apple accent.

Nutrition: 8
Saturation: 0.78
Effects: NONE
Stack: 16

Recipe:
- bliny x1
- minecraft:apple x1
- rolling_pin

Output: 1

---

# 35. БЛИНЫ С МЁДОМ — `honey_bliny`

Design:
- стопка bliny;
- сверху крупная золотистая полоса мёда.

Nutrition: 8
Saturation: 0.82
Effects: NONE
Stack: 16

Recipe:
- bliny x1
- honey_bottle x1
- rolling_pin

Output: 1
Return: glass_bottle

---

# 36. ПИРОГИ — ОБЩЕЕ

Все pies:
- один цельный круглый пирог как item;
- stack 16;
- не placeable block;
- едятся обычным food action;
- table model отображает целый пирог.

---

# 37. ЯБЛОЧНЫЙ ПИРОГ — `apple_pie`

Design:
- золотистая круглая корка;
- простая решётка сверху.

Nutrition: 8
Saturation: 0.80
Effects: NONE

Recipe:
- dough x2
- minecraft:apple x2
- minecraft:sugar x1
- rolling_pin

Process: 80 ticks
Output: 1

---

# 38. ЯГОДНЫЙ ПИРОГ — `berry_pie`

Design:
- golden crust;
- крупная решётка;
- между полосами 4–6 красно-синих ягодных pixels.

Nutrition: 8
Saturation: 0.82
Effects: NONE

Recipe:
- dough x2
- berries x3
- sugar x1
- rolling_pin

Output: 1

Не создавать berry-specific variants.

---

# 39. МЯСНОЙ ПИРОГ — `meat_pie`

Design:
- более тёмная корка;
- 3 крупные прорези;
- небольшой разрез сбоку с тёмной мясной начинкой.

Nutrition: 10
Saturation: 0.90
Effects: NONE
Stack: 16

Recipe:
- dough x2
- raw meat x2
- rolling_pin

Output: 1

Kitchen Table считается полным cooking process:
raw meat внутри pie становится приготовленной начинкой.

---

# 40. КАПУСТНЫЙ ПИРОГ — `cabbage_pie`

Design:
- golden pie;
- простой leaf-shaped надрез сверху;
- небольшой разрез показывает светло-зелёную начинку.

Nutrition: 8
Saturation: 0.80
Effects: NONE

Recipe:
- dough x2
- cabbage x2
- rolling_pin

Output: 1

---

# 41. FOOD SUMMARY

| Item | Nutrition | Saturation Modifier | Effect |
|---|---:|---:|---|
| rye_bread | 6 | 0.70 | none |
| karavai | 8 | 0.85 | none |
| baked_turnip | 4 | 0.50 | none |
| stewed_turnip | 6 | 0.65 | none |
| stewed_cabbage | 6 | 0.70 | none |
| oat_porridge | 6 | 0.65 | none |
| barley_porridge | 6 | 0.65 | none |
| berry_porridge | 7 | 0.75 | none |
| vegetable_stew | 7 | 0.70 | none |
| meat_stew | 9 | 0.85 | none |
| pea_soup | 7 | 0.75 | none |
| mushroom_stew_slavic | 6 | 0.70 | none |
| ukha | 8 | 0.80 | none |
| bliny | 6 | 0.65 | none |
| berry_bliny | 8 | 0.80 | none |
| meat_bliny | 9 | 0.85 | none |
| apple_bliny | 8 | 0.78 | none |
| honey_bliny | 8 | 0.82 | none |
| apple_pie | 8 | 0.80 | none |
| berry_pie | 8 | 0.82 | none |
| meat_pie | 10 | 0.90 | none |
| cabbage_pie | 8 | 0.80 | none |

Не менять эти значения по собственному решению.

---

# 42. BOWL FOOD BEHAVIOR

Все bowl dishes:
- stack size 1;
- после еды возвращают `minecraft:bowl`.

Список:
- stewed_turnip
- stewed_cabbage
- oat_porridge
- barley_porridge
- berry_porridge
- vegetable_stew
- meat_stew
- pea_soup
- mushroom_stew_slavic
- ukha

---

# 43. ORDINARY TABLE + BOWL FOODS

Bowl food можно ставить на ordinary table.

Table 3D model:
- bowl сохраняется визуально;
- soup/porridge top layer различается цветом.

Если bowl food съели прямо со стола:
- пустая bowl возвращается игроку.

---

# 44. KITCHEN TABLE RECIPE SYSTEM

Создать data-driven recipe type:

предпочтительное id:
`slavicmyths:kitchen`

Recipe data должны поддерживать:

- mode:
  - ROLLING
  - POT
- ingredients с count;
- required tool;
- process time;
- output item + count
  ИЛИ
- pot dish id + servings;
- container remainders.

Не hardcode все рецепты одним giant switch, если custom recipe system проекта уже существует.

Не создавать универсальный scripting engine.

Recipe matching:
- order-independent по 4 ingredient slots;
- учитывает item tags;
- учитывает count;
- лишние unrelated ingredients не допускаются.

---

# 45. RECIPE VIEWER / JEI

Core mod не должен жёстко зависеть от JEI.

Если существующая optional JEI integration уже есть:
- добавить одну category Kitchen Table;
- показывать:
  - 4 ingredients;
  - required tool icon;
  - result;
  - servings для POT recipes.

Если JEI integration отсутствует или сломана:
- НЕ тратить большой объём токенов на новую полную JEI архитектуру;
- оставить standard data-driven recipes;
- в final report одной строкой отметить, что отдельная viewer category не добавлялась.

Не добавлять hard dependency.

---

# 46. KITCHEN TABLE RECIPE START RULES

Recipe стартует только если:

- kitchen table не busy;
- для rolling:
  - rolling_pin установлен;
  - output slot может принять result;
- для pot:
  - metal_pot установлен;
  - servingsRemaining == 0;
- ingredients полностью совпадают;
- return slots имеют место для container remainders.

Предпочтительно:
- consume ingredients в начале;
- сохранить active recipe/progress;
- при break не возвращать уже потреблённые ингредиенты;
- tool не потребляется.

---

# 47. KITCHEN TABLE BREAKING

Breaking master/other half:

Drop:
- kitchen_table item один раз;
- rolling_pin;
- metal_pot;
- ingredient slots;
- output slot;
- return slots.

Active consumed recipe:
- прогресс теряется;
- уже consumed ingredients не возвращаются.

Prepared pot servings:
- теряются;
- никаких бесплатных bowls/food items.

Удалить second half.

Не дублировать drops.

---

# 48. REDSTONE / AUTOMATION

В 1.1.5 НЕ добавлять полную automation system.

Hoppers:
- могут вставлять ingredients и извлекать output/remainders только если стандартная item handler система проекта позволяет сделать это без лишней архитектуры.

Hoppers НЕ:
- устанавливают rolling pin/pot;
- не забирают pot servings без bowl logic.

Если sided automation заметно усложняет код:
- полностью отключить hopper automation для kitchen table в 1.1.5.

Не тратить токены на сложную automation поддержку.

---

# 49. ОБЫЧНЫЙ СТОЛ — СОХРАНЕНИЕ И СТРУКТУРЫ

Поскольку ordinary table уже стоит в existing worlds/structures:

- новые food slots по умолчанию empty;
- старые table blocks продолжают грузиться;
- одиночный model остаётся старым;
- structures не нужно переписывать;
- bandit tables не получают случайную еду;
- никаких world migration scripts.

Если старый table уже имеет BlockEntity:
- расширить его.
Если нет:
- добавить минимальный BE.

---

# 50. CREATIVE TAB DISTRIBUTION

## Resources & Materials

Добавить:
- wheat_flour
- rye_flour
- oat_groats
- barley_groats
- dough

## Food & Kitchen

Добавить:
- kitchen_table
- rolling_pin
- metal_pot
- rye_bread
- karavai
- baked_turnip
- stewed_turnip
- stewed_cabbage
- oat_porridge
- barley_porridge
- berry_porridge
- vegetable_stew
- meat_stew
- pea_soup
- mushroom_stew_slavic
- ukha только если item принадлежит Slavic Myths
- bliny
- berry_bliny
- meat_bliny
- apple_bliny
- honey_bliny
- apple_pie
- berry_pie
- meat_pie
- cabbage_pie

## Decor & Blocks

Существующий ordinary table ОСТАЁТСЯ в своей текущей логичной категории.
Не переносить его в Food & Kitchen только из-за food display functionality.

---

# 51. PLACEABLE TABLE FOOD TAG

Добавить все готовые блюда 1.1.5.

Не добавлять:
- flour;
- groats;
- dough;
- raw ingredients;
- rolling pin;
- pot.

Table предназначен для подачи готовой еды.

---

# 52. ADVANCEMENT

ID:
`generous_table`

Parent:
выбрать ближайший существующий Slavic Myths farming/kitchen advancement, если он легко находится.
Иначе:
`minecraft:husbandry/root`.

RU title:
`Щедрый стол`

RU description:
`Приготовь сложное блюдо на кухонном столе`

EN title:
`Generous Table`

EN description:
`Prepare a hearty dish at the kitchen table`

Trigger:
получить любой item из tag:
`slavicmyths:complex_dishes`

Tag содержит:
- berry_bliny
- meat_bliny
- apple_bliny
- honey_bliny
- apple_pie
- berry_pie
- meat_pie
- cabbage_pie
- vegetable_stew
- meat_stew
- pea_soup
- mushroom_stew_slavic
- ukha

Frame:
- task
- no XP
- not hidden

---

# 53. LANG

Минимум:
- ru_ru
- en_us

RU:
- Пшеничная мука
- Ржаная мука
- Овсяная крупа
- Ячменная крупа
- Тесто
- Кухонный стол
- Скалка
- Металлический горшок
- Ржаной хлеб
- Каравай
- Печёная репа
- Тушёная репа
- Тушёная капуста
- Овсяная каша
- Ячменная каша
- Каша с ягодами
- Овощная похлёбка
- Мясная похлёбка
- Гороховый суп
- Грибная похлёбка
- Уха
- Блины
- Блины с ягодами
- Блины с мясом
- Блины с яблоком
- Блины с мёдом
- Яблочный пирог
- Ягодный пирог
- Мясной пирог
- Капустный пирог
- Порций: %s/%s
- Сначала заберите готовое блюдо
- Щедрый стол
- Приготовь сложное блюдо на кухонном столе

EN:
- Wheat Flour
- Rye Flour
- Oat Groats
- Barley Groats
- Dough
- Kitchen Table
- Rolling Pin
- Metal Cooking Pot
- Rye Bread
- Karavai
- Baked Turnip
- Stewed Turnip
- Stewed Cabbage
- Oat Porridge
- Barley Porridge
- Berry Porridge
- Vegetable Pottage
- Meat Pottage
- Pea Soup
- Mushroom Pottage
- Ukha
- Bliny
- Berry Bliny
- Meat Bliny
- Apple Bliny
- Honey Bliny
- Apple Pie
- Berry Pie
- Meat Pie
- Cabbage Pie
- Servings: %s/%s
- Serve the prepared dish first
- Generous Table
- Prepare a hearty dish at the kitchen table

---

# 54. ORDINARY TABLE DATA PERSISTENCE

Сохранять:
- 4 ItemStacks.

Connection blockstate не хранить в BE.

После load:
- food renderer сразу получает synced data.

При destruction:
- все 4 stacks drop.

---

# 55. ORDINARY TABLE NETWORK SYNC

Не отправлять packet каждый tick.

Синхронизация:
- только когда slot меняется;
- block entity update packet;
- block update.

Renderer читает client-side inventory snapshot.

---

# 56. KITCHEN TABLE NETWORK SYNC

Sync only:
- progress через стандартный DataSlot/разумную частоту;
- tool states;
- pot dish id;
- servings;
- inventory changes.

Не отправлять большой NBT каждый tick вручную.

---

# 57. PERFORMANCE

Запрещено:
- глобальные scans столов;
- глобальные scans кухонных столов;
- tick handler для всех table block positions;
- recipe search по всему registry каждый tick.

Правильно:
- ordinary table не ticking;
- kitchen table ticks только свой active process;
- recipe lookup через recipe manager/cache;
- renderer рендерит максимум 4 stacks на block;
- paired table не является одним сложным 8-slot master.

---

# 58. ASSET LIST

Новые item textures:
- wheat_flour
- rye_flour
- oat_groats
- barley_groats
- dough
- rolling_pin
- metal_pot
- все новые food items

Kitchen table:
- item model;
- left/right block models;
- active visual elements;
- rolling dough model;
- pot model.

Ordinary table:
- NO texture redesign;
- paired model variants using old textures.

Table food models:
- simple 3D models for every new ready dish.

GUI:
- one background texture.

Effect icons:
- none.

---

# 59. НЕ ДОБАВЛЯТЬ В 1.1.5

- alcohol;
- mead;
- kvass fermentation;
- cider;
- beer;
- brewing barrels;
- fermentation timers;
- jam;
- preserves;
- drying;
- bags;
- storage overhaul;
- new crops;
- onions;
- garlic;
- salt resource;
- spices system;
- food quality;
- food spoilage;
- temperature;
- special food buffs;
- plate item;
- clay bowl duplicate;
- fork/spoon items;
- oven;
- stove;
- frying pan;
- new ordinary table block;
- ordinary table redesign;
- infinite table chains;
- cake-like multi-bite pies;
- beverage system.

---

# 60. ВЕРСИЯ

Обновить source of truth mod version до:
`1.1.5`

Не создавать несколько несвязанных version constants.

---

# 61. BUILD

После реализации:

1. Не запускать Minecraft.
2. Не запускать PolyMC.
3. Выполнить одну финальную compile/build command.
4. Если success — закончить.
5. Если fail из-за изменений 1.1.5:
   - исправить конкретную ошибку;
   - допускается максимум один повторный build.
6. Если fail из-за старой независимой ошибки:
   - не чинить весь проект;
   - указать точный blocker.

---

# 62. GIT FINISH

Branch:
`main`

Commit:
`feat: implement Slavic Myths 1.1.5 Kitchen II`

Push:
- только если existing auth работает.

НЕ:
- release branch;
- tag;
- GitHub Release;
- force push;
- PolyMC copy.

---

# 63. КРИТЕРИИ ГОТОВНОСТИ

## Existing ordinary table
[ ] Старый table registry id сохранён.
[ ] Базовые textures не переработаны.
[ ] Single table выглядит как раньше.
[ ] Два adjacent single table могут стать pair.
[ ] Pair максимум 2 blocks.
[ ] У pair только 4 наружные ножки.
[ ] Third table не присоединяется к готовой pair.
[ ] Breaking одной половины возвращает вторую в SINGLE.
[ ] На каждом table 4 food slots.
[ ] Pair суммарно даёт 8 slots.
[ ] Food сохраняется.
[ ] Food drop при разрушении.
[ ] 3D food renderer работает.
[ ] Right click places food.
[ ] Empty hand retrieves food.
[ ] Sneak+empty hand eats edible food.
[ ] Bowls/containers возвращаются после eating.
[ ] Existing bandit structures не требуют переделки.

## Kitchen table
[ ] kitchen_table занимает 2 blocks.
[ ] LEFT/RIGHT structure.
[ ] Один master BE.
[ ] Breaking не дублирует drops.
[ ] Rolling pin tool slot.
[ ] Metal pot tool slot.
[ ] 4 ingredient slots.
[ ] output slot.
[ ] 2 container return slots.
[ ] GUI без overlap.
[ ] recipe auto matching.
[ ] rolling recipes.
[ ] pot recipes.
[ ] visual cooking.
[ ] no fuel.
[ ] no new stove.

## Milling
[ ] Existing mill extended if present.
[ ] wheat flour.
[ ] rye flour.
[ ] oat groats.
[ ] barley groats.
[ ] If mill absent, exact fallback crafting implemented and noted.

## Ingredients
[ ] wheat_flour.
[ ] rye_flour.
[ ] oat_groats.
[ ] barley_groats.
[ ] dough.
[ ] cooking tags.
[ ] bucket/bottle remainder logic.

## Foods
[ ] rye_bread.
[ ] karavai.
[ ] baked_turnip.
[ ] stewed_turnip.
[ ] stewed_cabbage.
[ ] oat_porridge.
[ ] barley_porridge.
[ ] berry_porridge.
[ ] vegetable_stew.
[ ] meat_stew.
[ ] pea_soup.
[ ] mushroom_stew_slavic.
[ ] ukha integrated/reused.
[ ] bliny.
[ ] berry_bliny.
[ ] meat_bliny.
[ ] apple_bliny.
[ ] honey_bliny.
[ ] apple_pie.
[ ] berry_pie.
[ ] meat_pie.
[ ] cabbage_pie.

## Food balance
[ ] Nutrition values exactly match spec.
[ ] Saturation modifiers exactly match spec.
[ ] None of new foods gives potion effects.
[ ] Bowl foods return bowls.
[ ] Stack sizes match spec.

## Pot
[ ] pot dishes create servings.
[ ] serving requires bowl.
[ ] bowls are consumed.
[ ] dish item returned.
[ ] no free bowls when table broken.
[ ] prepared contents prevent pot removal.

## Assets
[ ] Minecraft-like pixel density.
[ ] preferred 16x16 for simple item sprites.
[ ] no direct high-res reference copy.
[ ] simple 3D table food models.
[ ] ordinary table visuals preserved.

## Creative
[ ] processed ingredients in Resources & Materials.
[ ] kitchen tools/table/food in Food & Kitchen.
[ ] ordinary table remains in its existing category.
[ ] no old dump tab regression.

## Data
[ ] ru_ru.
[ ] en_us.
[ ] advancement.
[ ] tags.
[ ] recipes.
[ ] recipe serializer/type.
[ ] persistence/network sync.

## Workflow
[ ] tokens used economically.
[ ] no full project audit.
[ ] Minecraft not launched.
[ ] PolyMC untouched.
[ ] version 1.1.5.
[ ] main only.
[ ] no tag/release.
[ ] only necessary build(s).

---

# 64. MANUAL TEST CHECKLIST ДЛЯ ПОЛЬЗОВАТЕЛЯ

Codex НЕ запускает игру.

В final report вывести:

1. Поставить один existing ordinary table — сравнить со старым видом.
2. Поставить второй рядом — проверить объединение и 4 наружные ножки.
3. Поставить третий — проверить отсутствие цепочки.
4. Разломать один table pair — второй должен стать single.
5. Положить 4 разных блюда на один table.
6. Проверить 3D-модели еды.
7. Забрать еду пустой рукой.
8. Съесть еду со стола через Sneak+ПКМ.
9. Поставить двухблочный kitchen table.
10. Установить rolling pin и metal pot.
11. Проверить flour/groats processing.
12. Сделать dough.
13. Приготовить rye bread и karavai.
14. Приготовить bliny и все 4 варианта начинки.
15. Приготовить 4 pies.
16. Приготовить pot dish и получить 2/3 порции через bowls.
17. Проверить return bucket/glass bottle.
18. Проверить food nutrition/saturation вручную.
19. Убедиться, что никаких status effects блюда не дают.
20. Проверить creative tabs/lang/advancement.

---

# 65. ФОРМАТ ФИНАЛЬНОГО ОТЧЁТА CODEX

Очень коротко.

## Implemented
5–12 bullets.

## Build
- command;
- success/fail;
- точный blocker при fail.

## Integration notes
Только если применимо:
- existing mill reused;
ИЛИ
- fallback flour crafting used.
- existing ukha reused;
ИЛИ
- new ukha created.
- JEI category updated / not added.

## Git
- branch;
- commit hash;
- push status.

## Manual test checklist
Короткий список.

НЕ писать:
- длинный анализ;
- chain-of-thought;
- предложения для 1.1.6;
- новые design ideas;
- общий аудит проекта.
