# SLAVIC MYTHS 1.1.7 — «ПИВОВАРЕНИЕ И ФЕРМЕНТАЦИЯ»
## Финальный design document + implementation prompt для Codex

---

# 0. ОБЯЗАТЕЛЬНЫЙ РЕЖИМ РАБОТЫ CODEX

Это точное ТЗ. Все ключевые игровые решения уже определены.
Codex должен реализовать описанное, НЕ расширяя scope и НЕ задавая вопросы по дизайну.

## 0.1 ЭКОНОМИЯ ТОКЕНОВ — КРИТИЧЕСКОЕ ПРАВИЛО

Работать максимально экономно.

ЗАПРЕЩЕНО:
- проводить общий аудит Slavic Myths;
- читать весь репозиторий;
- анализировать старые подсистемы, не связанные с 1.1.7;
- перечитывать 1.1.0–1.1.6 целиком;
- писать длинный архитектурный анализ;
- повторно пересказывать это ТЗ;
- рефакторить старый код «заодно»;
- искать потенциальные улучшения вне scope;
- делать много одинаковых поисков;
- многократно запускать build;
- придумывать новые напитки/ингредиенты;
- делать новую универсальную алхимическую систему;
- тратить токены на полную переработку JEI, если existing integration легко не расширяется.

Разрешён минимальный просмотр:
- существующей `wooden_barrel` из 1.1.6;
- existing container/BlockEntity/menu helpers;
- item/block registries;
- tags;
- Creative Tabs;
- crops/berries/apple/honey;
- текущих magical item ids только для конкретных магических рецептов;
- existing effect/data attachment helpers;
- PolyMC path/config только на финальном шаге, как описано ниже.

Принцип:
**минимальный точечный поиск → реализация → следующий блок.**

## 0.2 MINECRAFT НЕ ЗАПУСКАТЬ

НЕ запускать:
- runClient;
- runServer;
- Minecraft;
- игровой тестовый мир.

Пользователь тестирует вручную.

## 0.3 POLYMC — ИСКЛЮЧЕНИЕ ДЛЯ ЭТОЙ ВЕРСИИ

Обычно сборку не переносим автоматически.
ДЛЯ 1.1.7 пользователь ПРЯМО потребовал:

**после успешной реализации и build перенести новый JAR Slavic Myths 1.1.7 в используемый PolyMC instance для ручного тестирования.**

Но:
- PolyMC НЕ запускать;
- Minecraft НЕ запускать;
- не менять другие моды;
- не менять instance configuration;
- не удалять посторонние JAR.

Точный алгоритм в разделе `POLYMC TRANSFER`.

## 0.4 НИКАКОЙ САМОСТОЯТЕЛЬНОЙ ФАНТАЗИИ

НЕ добавлять:
- новые напитки сверх списка;
- новые магические артефакты без fallback-правила ниже;
- отдельную систему алхимии;
- температуру брожения;
- сложную микробиологию;
- thirst;
- dehydration;
- drunk movement physics;
- управление мышью «как пьяный»;
- blackouts;
- смерть напрямую от одной кружки;
- addiction;
- hangover persistence после долгого ожидания;
- новые культуры сверх хмеля;
- новые ягоды;
- новые блюда;
- NPC tavern system;
- торговлю напитками;
- бармена;
- новый мир/биомы;
- новые бочки вместо уже существующей wooden_barrel.

## 0.5 ПЛАТФОРМА

- Minecraft 1.21.1
- NeoForge
- Java 21
- текущий mod id проекта
- текущая package/resource/data структура

## 0.6 GIT

Рабочая ветка:
`main`

После реализации:
- commit только в `main`;
- push `origin/main`, если авторизация уже работает;
- НЕ трогать release branch;
- НЕ создавать tag;
- НЕ создавать GitHub Release;
- НЕ force-push;
- НЕ удалять чужие изменения.

Commit:
`feat: implement Slavic Myths 1.1.7 brewing and fermentation`

Если push требует отдельной auth-настройки:
- не тратить токены;
- оставить локальный commit;
- сообщить одну команду push.

---

# 1. ЦЕЛЬ ВЕРСИИ

1.1.7 — последняя крупная контентная версия хозяйственной арки 1.1.x.

Она должна добавить:

1. Хмель как сельскохозяйственную культуру.
2. Солодовый ячмень.
3. Мешок солода.
4. Деревянную кружку.
5. Тёмную стеклянную бутылку.
6. Керамический кувшин.
7. Малый бочонок.
8. Пресс для яблок/ягод.
9. Бродильный чан.
10. Расширение существующей `wooden_barrel` системой ферментации.
11. Ягодную мезгу.
12. Яблочный сок / основу для сидра.
13. Медовый настой.
14. Лесной травяной настой.
15. Порошок громового кристалла.
16. Обычные сорта пива.
17. Магические сорта пива.
18. Квас.
19. Медовуху.
20. Яблочный сидр.
21. Ягодный морс.
22. Систему постепенного алкогольного/магического перегруза.
23. Временные ванильные негативные эффекты при злоупотреблении.
24. Более сильные отрицательные последствия у магических напитков.
25. Широкие окна ферментации без «ловли секунды».
26. Creative Tabs / recipes / tags / localization / advancement.
27. Финальный build + перенос JAR 1.1.7 в существующий PolyMC instance.

---

# 2. ВИЗУАЛЬНЫЕ РЕФЕРЕНСЫ

Использовать:

`references/01_brewing_items_and_equipment.png`

`references/02_beers_and_other_drinks.png`

Это утверждённые концепты.

Они задают:
- формы;
- общую палитру;
- различимость сортов;
- стиль тары;
- оборудование.

Но реальные assets:
- Minecraft-compatible;
- крупнее пиксели;
- меньше мелкой детализации;
- не копировать reference как готовую HD texture.

---

# 3. СТИЛЬ АССЕТОВ

Предпочтительно:

Item sprites:
- 16x16.

Разрешено:
- 32x32 только если предмет теряет читаемость.

Block textures:
- vanilla-like texel density.

Запрещено:
- anti-aliasing;
- blur;
- photo textures;
- слишком мелкая орнаментация;
- high-res illustration look.

---

# 4. ХМЕЛЬ — `hops_crop` / `hops`

## 4.1 Названия

Block:
`hops_crop`

Produce:
`hops`

Seed/planting item:
`hops_cutting`

RU:
- Хмель
- Черенок хмеля

EN:
- Hops
- Hop Cutting

Creative:
`Земледелие и растения`

## 4.2 Дизайн растения

Хмель — высокая вьющаяся культура.

Не делать её обычным wheat-like crop.

Стадии:
0..5.

Stage 0:
- короткий зелёный побег.

Stage 1:
- тонкий стебель с 2–3 листьями.

Stage 2:
- стебель выше;
- заметное вьющееся направление.

Stage 3:
- густая зелень;
- несколько мелких бутонов.

Stage 4:
- светло-зелёные молодые шишки.

Stage 5:
- зрелые крупные зелёные хмелевые шишки.

Высота:
- Stage 0–2: 1 block.
- Stage 3–5: до 2 blocks.

Для зрелого хмеля:
- lower + upper part, синхронизированные.

Не требовать отдельную шпалеру в 1.1.7.
Игровая абстракция допустима.

## 4.3 Посадка

На farmland.

Рост:
- примерно как flax / немного медленнее wheat;
- coefficient ~0.85 от базовой crop speed.

Bone meal:
- +1 stage;
- максимум 5.

Organic fertilizer:
- поддержать как crop;
- +1 stage в 3x3 по уже существующей логике.

## 4.4 Сбор

Зрелый Stage 5:

обычное breaking:
- 2–4 hops;
- 1–2 hops_cutting.

Sickle:
- должен поддерживать hops;
- mature harvest:
  - выдаёт mature loot;
  - возвращает Stage 1;
  - durability -1.

## 4.5 Получение первого черенка

Дополнительный редкий loot из:
- short_grass / tall_grass
в plains / meadow / forest.

Если biome-sensitive loot слишком дорого внедрять:
- добавить `hops_cutting` в существующую систему seed loot из травы с низким весом.

Ориентир:
примерно в 3 раза реже обычных seed 1.1.0.

---

# 5. СОЛОДОВЫЙ ЯЧМЕНЬ — `malted_barley`

RU:
`Солодовый ячмень`

EN:
`Malted Barley`

Creative:
`Ресурсы и материалы`

Stack:
64.

## Дизайн

16x16:
- кучка тёплых золотисто-коричневых пророщенных зёрен;
- у части grains короткий светлый росток;
- заметно отличается от обычного barley grain.

## Производство

Базовый этап:
- barley_grain x4
- water_bucket x1
на Brewing Vat в режиме MALTING.

Process:
1200 ticks = 60 сек.

Output:
4 malted_barley.

Return:
bucket.

Не создавать отдельную malting machine.

---

# 6. МЕШОК СОЛОДА — `malt_sack`

RU:
`Мешок солода`

EN:
`Malt Sack`

Creative:
`Ресурсы и материалы`

Это НЕ storage block.
Это compact ingredient item.

Recipe:
- 9 malted_barley -> 1 malt_sack.

Reverse:
- 1 malt_sack -> 9 malted_barley.

Design:
- небольшой светло-бежевый мешок;
- сверху видны золотистые grains;
- один крупный красно-коричневый символ зерна;
- не копировать storage_sack block item.

Stack:
64.

---

# 7. ДЕРЕВЯННАЯ КРУЖКА — `wooden_mug`

RU:
`Деревянная кружка`

EN:
`Wooden Mug`

Creative:
`Еда и кухня`

Stack:
16.

## Дизайн

- небольшая бочкообразная кружка;
- вертикальные деревянные клёпки;
- два тёмных металлических обруча;
- крупная ручка сбоку;
- открытый верх.

Empty:
- внутри тёмно.

Filled beverage items:
- используют ту же mug silhouette;
- сверху явно виден цвет напитка и пены.

Recipe:

Shaped:
`P P`
`P P`
` PP`

P = plank

Output:
2 wooden_mug.

Не использовать iron bucket.

---

# 8. ТЁМНАЯ БУТЫЛКА — `dark_bottle`

RU:
`Тёмная бутылка`

EN:
`Dark Bottle`

Creative:
`Еда и кухня`

Stack:
16.

Design:
- толстое тёмно-зелёно-коричневое стекло;
- короткое горлышко;
- пробка;
- широкое основание;
- напиток виден цветным оттенком.

Recipe:
- 3 glass
- 1 cork substitute.

НЕ вводить новый cork item.
Использовать:
- 1 stick как игровую абстракцию пробки.

Shaped:
` G `
`GSG`
` G `

G = glass
S = stick

Output:
2 dark_bottle.

---

# 9. КЕРАМИЧЕСКИЙ КУВШИН — `ceramic_pitcher`

RU:
`Керамический кувшин`

EN:
`Ceramic Pitcher`

Creative:
`Еда и кухня`

Stack:
16.

## Дизайн

- пузатый глиняный корпус;
- широкое короткое горлышко;
- большая ручка;
- терракотово-красно-коричневый цвет;
- один простой красный/тёмный геометрический орнамент.

Capacity:
4 portions.

Кувшин используется для:
- kvass;
- mead;
- berry mors.

Не использовать для beer mug recipes.

Recipe:
- bricks / clay product.

Shaped:
` B `
`B B`
`BBB`

B = brick

Output:
1 ceramic_pitcher.

---

# 10. МАЛЫЙ БОЧОНОК — `small_keg`

RU:
`Малый бочонок`

EN:
`Small Keg`

Creative:
`Декор и блоки`

## Дизайн

- горизонтальный маленький barrel;
- два металлических обруча;
- деревянная подставка;
- маленький brass/wood tap спереди.

1 block.

Capacity:
8 servings готового напитка.

Это storage/serving block, НЕ ферментер.

## Inventory / fluid abstraction

Не вводить real fluid API.

Хранить:
- beverage id;
- servings 0..8.

Наполнение:
- right click filled mug/bottle/pitcher suitable beverage;
- перелить 1 serving в keg;
- вернуть empty container.

Выдача:
- right click empty wooden_mug:
  - если beverage beer -> fill 1 mug.
- right click dark_bottle:
  - fill 1 bottle.
- ceramic_pitcher:
  - можно наполнить до 4 servings, если keg имеет >=4.

Не смешивать разные beverage ids.
После 0 servings id очищается.

---

# 11. ПРЕСС ДЛЯ ЯБЛОК / ЯГОД — `fruit_press`

RU:
`Пресс`

EN:
`Fruit Press`

Creative:
`Декор и блоки`

## 11.1 Дизайн

- деревянная прямоугольная рама;
- вертикальный винт/давилка;
- маленькая деревянная корзина/чан под прессом;
- спереди лоток для сока;
- видимые яблоки/ягоды только когда input реально загружен.

Size:
1 block footprint;
visual height ~1.5 block.

FACING horizontal.

## 11.2 Slots

Input:
- 1 stack.

Output:
- 1 stack.

Remainder:
- 1 stack.

No GUI if possible.

Direct interaction:
- item insertion;
- empty hand output retrieval.

## 11.3 Recipes

### Apple press
4 minecraft:apple
-> 1 `apple_juice_bottle`
+ 1 `fruit_pomace`

Process:
3 manual presses.

### Berry mash
4 any berries
-> 2 `berry_mash`
+ 1 `fruit_pomace`

Process:
3 manual presses.

Manual action:
right click empty hand while valid input loaded:
- progress +1;
- wood/press sound;
- visual screw moves down.

After third:
- produce output.

No automatic ticking needed.

---

# 12. `fruit_pomace`

RU:
`Жмых`

EN:
`Fruit Pomace`

Creative:
`Ресурсы и материалы`

Stack:
64.

Design:
- маленькая коричнево-красная спрессованная масса;
- 2–3 заметных растительных фрагмента.

В 1.1.7:
- простой побочный продукт;
- можно compostable.
- не добавлять новые food recipes.

---

# 13. `berry_mash`

RU:
`Ягодная мезга`

EN:
`Berry Mash`

Creative:
`Ресурсы и материалы`

Stack:
64.

Design:
- тёмно-бордовая густая ягодная масса;
- небольшая wooden bowl/heap silhouette;
- красно-фиолетовый цвет.

Используется:
- berry witch beer;
- berry fermentation;
- berry mors.

---

# 14. `apple_juice_bottle`

RU:
`Яблочный сок`

EN:
`Apple Juice`

Container:
dark_bottle-like glass.

Stack:
16.

Nutrition:
2.

Saturation modifier:
0.30.

Effects:
NONE.

No intoxication load.

Используется:
- apple cider.

После питья:
- empty dark_bottle.

---

# 15. МЕДОВЫЙ НАСТОЙ — `honey_infusion`

RU:
`Медовый настой`

EN:
`Honey Infusion`

Creative:
`Ресурсы и материалы`

Stack:
16.

Design:
- маленькая баночка/бутылочка с золотистой жидкостью;
- белая тканевая перевязь сверху;
- тёплый янтарный цвет.

Recipe in Brewing Vat:
- honey_bottle x2
- water_bucket x1

Process:
800 ticks.

Output:
2 honey_infusion.

Return:
- 2 glass bottles;
- 1 bucket.

Используется:
- honey beer;
- mead.

Не drinkable напрямую.

---

# 16. ЛЕСНОЙ ТРАВЯНОЙ НАСТОЙ — `forest_herbal_infusion`

RU:
`Лесной травяной настой`

EN:
`Forest Herbal Infusion`

Creative:
`Ресурсы и материалы`

Stack:
16.

Design:
- тёмно-зелёная бутылочка;
- внутри видны крупные green herb shapes;
- пробка.

Recipe in Brewing Vat:
- brown_mushroom x1
- red_mushroom x1
- any berries x1
- water_bucket x1

Process:
1000 ticks.

Output:
2 forest_herbal_infusion.

Return:
bucket.

Это gameplay ingredient.
Не добавлять новый herb crop.

---

# 17. ПОРОШОК ГРОМОВОГО КРИСТАЛЛА — `thunder_crystal_powder`

RU:
`Порошок громового кристалла`

EN:
`Thunder Crystal Powder`

Creative:
`Магия и артефакты`

Stack:
16.

## Источник

Сделать один минимальный registry search текущего магического предмета:
- thunder stone;
- громовой камень;
- thunder_stone.

Если найден существующий thunder stone:
recipe:
- 1 thunder stone -> 4 thunder_crystal_powder.

НЕ создавать новый thunder stone.

Если exact item отсутствует:
- recipe для thunder_crystal_powder НЕ добавлять;
- Thunder Beer recipe также disabled/not generated;
- в final report одной строкой отметить:
  `Thunder Stone item was not present; Thunder Beer recipe was not enabled.`

Не задавать вопрос пользователю.

Design:
- синевато-белая кристаллическая крошка;
- 1 тёмный осколок;
- несколько ярких голубых pixels;
- не animated item texture.

---

# 18. BROODING / BREWING VAT — `fermentation_vat`

RU:
`Бродильный чан`

EN:
`Fermentation Vat`

Creative:
`Декор и блоки`

## 18.1 Дизайн

- большая открытая деревянная кадка;
- металлические обручи;
- сверху видна жидкость/масса при работе;
- деревянная мешалка сбоку/внутри;
- не full cube.

Capacity:
одна batch recipe.

## 18.2 Inventory

Input slots:
4.

Container return:
2.

Output:
2.

Catalyst:
1 optional slot.

No fuel.

## 18.3 Modes

Recipe-driven:
- MALTING;
- INFUSION;
- WORT;
- MUST.

Один custom recipe type:
`slavicmyths:vat`.

No giant switch if simple JSON codec reasonable.

## 18.4 Visual

Empty:
- пустая кадка.

Active:
- видимый top-layer color based on recipe visual color;
- occasional bubble/steam particles;
- no dense smoke.

Complete:
- activity stops;
- result available.

---

# 19. EXISTING WOODEN BARREL — РАСШИРЕНИЕ

Использовать СУЩЕСТВУЮЩИЙ:
`wooden_barrel`

НЕ создавать:
- fermentation_barrel как новый block.

1.1.6 barrel inventory storage должен сохранить совместимость.

## 19.1 Mode

Добавить enum:
- STORAGE
- FERMENTATION

По умолчанию existing old barrel:
- STORAGE.

Переключение в FERMENTATION:
right click empty barrel с `fermentation_starter` НЕ нужно — не создавать starter item.

Вместо этого:
- если barrel empty;
- игрок ПКМ по barrel с готовой `wort/must base` item из vat;
- barrel автоматически становится FERMENTATION и принимает batch.

После полного извлечения готового напитка:
- возвращается STORAGE.

Не разрешать хранение обычных items во время FERMENTATION.

## 19.2 Save compatibility

Старые 1.1.6 barrels:
- грузятся STORAGE;
- inventory не теряется.

Не переименовывать registry id.

---

# 20. ПРОМЕЖУТОЧНЫЕ BREW BASE ITEMS

Создать:

- `light_beer_wort`
- `dark_beer_wort`
- `hopped_beer_wort`
- `honey_beer_wort`
- `thunder_beer_wort`
- `forest_beer_wort`
- `veles_dark_wort`
- `witch_berry_wort`
- `kvass_wort`
- `mead_must`
- `cider_must`

Creative:
не показывать в обычной вкладке, если можно скрыть technical intermediate items из tab.
Но item ids существуют для transfer vat -> barrel.

Все:
- stack 1 или 16;
- simple bottle/bucket-like internal item models.

Если проект позволяет custom component batch without intermediate items безопасно — можно хранить vat result как filled batch container.
НО не усложнять.
Промежуточные items допустимы и предпочтительны.

---

# 21. ФЕРМЕНТАЦИЯ — ОСНОВНАЯ ЛОГИКА

Это ключевая механика версии.

Не использовать real-world clock.
Использовать game ticks только пока chunk loaded.

После загрузки wort/must в FERMENTATION barrel:

`fermentationAge` увеличивается на server tick.

Широкие окна:

## Stage 0 — YOUNG
0–2399 ticks
(0–2 мин)

- ещё недодержано;
- можно извлечь как `young_brew` только если пользователь специально забирает;
- положительный эффект отсутствует или заметно слабее;
- intoxication load сохраняется.

Чтобы не создавать десятки young item ids:
при early draw выдавать основной beverage item с component:
`BREW_QUALITY=YOUNG`.

## Stage 1 — READY
2400–8399 ticks
(2–7 мин)

- правильный основной напиток;
- полные заявленные эффекты;
- стандартный вкус/цвет.

## Stage 2 — AGED
8400–11999 ticks
(7–10 мин)

- напиток всё ещё хороший;
- основной positive effect duration +20%;
- intoxication load НЕ уменьшается;
- магические напитки получают +20% positive duration, но и magical penalty multiplier сохраняется.

Quality component:
`AGED`.

## Stage 3 — SPOILED
>=12000 ticks
(после 10 мин загруженного чанка)

- партия перебродила/испортилась;
- при извлечении выдаётся `spoiled_brew` generic item;
- положительных эффектов нет;
- при питье:
  - Nausea I 20 sec
  - Poison I 5 sec
- overload points +1.

Это специально широкая система.
Игроку не нужно ловить секунду.

## 21.1 Важно

Если chunk unloaded:
- timer стоит.
- offline progression не нужна.

Если player забирает READY/AGED:
- barrel batch постепенно разливается порциями.
- после 0 servings barrel -> STORAGE.

---

# 22. `spoiled_brew`

RU:
`Испорченный напиток`

EN:
`Spoiled Brew`

Container:
dark bottle by default.

Stack:
16.

Effects:
- Nausea I 20 sec
- Poison I 5 sec

Overload:
+1.

No positive effect.

Design:
- мутно-коричнево-зелёная жидкость;
- тёмная пена/осадок;
- без gore.

---

# 23. КОЛИЧЕСТВО ПОРЦИЙ И BATCH

Один vat recipe создаёт:
- 1 wort/must batch item.

Один batch в barrel после fermentation:
- 8 servings.

Это standard batch size.

Одна кружка:
- 1 serving.

Одна bottle:
- 1 serving.

Pitcher:
- до 4 servings.

Small keg:
- до 8 servings.

---

# 24. РАЗЛИВ ИЗ FERMENTATION BARREL

ПКМ empty wooden_mug:
- получить 1 serving, если beverage supports mug.

ПКМ dark_bottle:
- получить 1 serving.

ПКМ ceramic_pitcher:
- наполнить до 4 servings, если есть >=1.
- pitcher stores beverage id + count 1..4.

Barrel GUI:
- можно сделать простой status screen:
  - drink name;
  - quality;
  - stage;
  - servings;
  - progress bar до следующего stage.

НЕ показывать exact seconds.
Показывать qualitative text:

RU:
- Молодой
- Готов
- Выдержанный
- Испорчен

EN:
- Young
- Ready
- Aged
- Spoiled

Так игрок не ловит секунды.

---

# 25. СИСТЕМА ПЕРЕГРУЗКИ / ОПЬЯНЕНИЯ

Это ОБЯЗАТЕЛЬНАЯ система.

Не добавлять custom drunk camera physics.

Использовать:
- hidden player attachment/data:
  `drink_overload_points`
- `last_overload_drink_game_time`.

## 25.1 Источники points

Обычное алкогольное пиво:
+1.

Cider:
+1.

Mead:
+1.

Magical beer / magical potion-like brew:
+2.

Spoiled brew:
+1.

Kvass:
0.

Berry mors:
0.

Apple juice:
0.

## 25.2 Спад

Если player НЕ употреблял напиток, повышающий overload:

каждые 1200 ticks = 60 sec:
- overload_points -= 1.

Минимум:
0.

При 0:
- следующий алкогольный напиток снова считается первой безопасной порцией.

Это реализует главное требование пользователя:
**если дать организму время, накопленные штрафы исчезают и новое употребление снова начинается с низкой стадии.**

## 25.3 Death

При death/respawn:
- overload_points = 0.

## 25.4 Milk

Vanilla milk / goat milk:
- может снять активные vanilla status effects обычным способом;
- НО НЕ сбрасывает hidden overload counter.

То есть milk не является мгновенным читом против накопленного алкоголя.
Через время overload всё равно естественно падает.

---

# 26. СТАДИИ НЕГАТИВНЫХ ЭФФЕКТОВ ОБЫЧНОГО АЛКОГОЛЯ

После добавления points пересчитать текущую stage.

## Points 0–1
Штраф:
NONE.

Одна обычная кружка:
- положительный эффект;
- без негативного наказания.

## Points 2
Apply:
- Nausea I — 10 sec.

## Points 3
Apply:
- Nausea I — 20 sec.
- Weakness I — 30 sec.

## Points 4
Apply:
- Nausea I — 30 sec.
- Weakness I — 45 sec.
- Slowness I — 30 sec.

## Points >=5
Apply:
- Nausea II — 40 sec.
- Weakness II — 60 sec.
- Slowness I — 45 sec.
- Poison I — 8 sec.

Эффекты:
- временные;
- обновляются/усиливаются при новом употреблении;
- не permanent.

Не добавлять все дебаффы с первой кружки.

---

# 27. МАГИЧЕСКИЕ НАПИТКИ — УСИЛЕННЫЕ ДЕФЕКТЫ

Magical drink:
- +2 overload points за одну порцию.

Кроме того, если именно magical drink активировал текущую negative stage:

Negative durations:
- умножить на 1.5.

Amplifiers:
- для stage points >=3:
  - Nausea amplifier +1, max II.
  - Weakness amplifier +1, max II.
- для points >=5:
  - Poison I duration = 12 sec вместо 8.
  - Slowness amplifier может стать II.

Иными словами:
магический напиток:
- мощнее по плюсам;
- быстрее перегружает игрока;
- сильнее наказывает за злоупотребление.

Но:
- всё временно;
- overload так же естественно спадает;
- после полного спада можно снова выпить одну магическую порцию без тяжёлого хвоста.

---

# 28. YOUNG / READY / AGED QUALITY И ЭФФЕКТЫ

Beverage item хранит quality component:
- YOUNG
- READY
- AGED

SPOILED — отдельный generic item.

## YOUNG

Positive effect duration:
50% от базовой.

Overload:
полный, без скидки.

Это специально:
ранний напиток менее выгоден.

## READY

Positive effect:
100%.

## AGED

Positive effect duration:
120%.

Overload:
такой же, как READY.

Никаких дополнительных amplifiers от aged quality.

---

# 29. ОБЫЧНЫЕ СОРТА ПИВА

Все выдаются преимущественно в wooden_mug.

Filled item ids:

- `light_beer_mug`
- `dark_beer_mug`
- `hopped_beer_mug`
- `honey_beer_mug`

Stack:
16.

Все:
- overload +1.

Все beer mugs после питья:
- возвращают wooden_mug.

---

# 30. СВЕТЛОЕ ПИВО

ID:
`light_beer_mug`

RU:
`Светлое пиво`

EN:
`Light Beer`

## Внешний вид

- деревянная кружка;
- ярко-золотистая жидкость;
- белая/кремовая пена;
- 1 крупный светлый блик.

## Positive effect

READY:
- Speed I — 45 sec.

AGED:
- Speed I — 54 sec.

YOUNG:
- Speed I — 22 sec.

No other positive effect.

## Wort recipe in Vat

- malted_barley x4
- hops x1
- water_bucket x1

Process:
1200 ticks.

Output:
1 light_beer_wort.

Return:
bucket.

---

# 31. ТЁМНОЕ ПИВО

ID:
`dark_beer_mug`

RU:
`Тёмное пиво`

EN:
`Dark Beer`

## Внешний вид

- тёмно-коричневая жидкость;
- каштановый highlight;
- кремовая густая пена.

## Positive

READY:
- Resistance I — 45 sec.

AGED:
- 54 sec.

YOUNG:
- 22 sec.

Overload:
+1.

## Wort

- malted_barley x5
- animal_fat НЕ использовать.
- hops x1
- honey_infusion x1
- water_bucket x1

Чтобы отличить от honey beer:
honey_infusion здесь выступает малой тёмной основой/карамелизацией в игровой абстракции.

Если это кажется технически лишним:
всё равно следовать ТЗ; не заменять рецептуру самостоятельно.

Process:
1400 ticks.

---

# 32. ХМЕЛЬНОЕ ПИВО

ID:
`hopped_beer_mug`

RU:
`Хмельное пиво`

EN:
`Hopped Beer`

Appearance:
- насыщенно-янтарное;
- пенка выше;
- более яркий orange-gold center.

Positive:

READY:
- Haste I — 40 sec.

AGED:
- 48 sec.

YOUNG:
- 20 sec.

Overload:
+1.

Wort:
- malted_barley x4
- hops x3
- water_bucket x1

Process:
1200 ticks.

---

# 33. МЕДОВОЕ ПИВО

ID:
`honey_beer_mug`

RU:
`Медовое пиво`

EN:
`Honey Beer`

Appearance:
- светло-медово-золотое;
- очень светлая кремовая пена;
- чуть более насыщенный жёлтый цвет, чем Light Beer.

Positive:

READY:
- Absorption I — 30 sec.

AGED:
- 36 sec.

YOUNG:
- 15 sec.

Overload:
+1.

Wort:
- malted_barley x3
- hops x1
- honey_infusion x2
- water_bucket x1

Process:
1400 ticks.

---

# 34. ЛЕСНОЕ ПИВО

ID:
`forest_beer_mug`

RU:
`Лесное пиво`

EN:
`Forest Beer`

Это ОСОБЫЙ, но не «сильный магический артефактный» напиток.

Overload:
+1.

Appearance:
- зелёно-золотая жидкость;
- кремовая пена;
- несколько крупных зелёных herb pixels.

Positive READY:
- Night Vision I — 45 sec.
- Luck I — 45 sec.

AGED:
- оба 54 sec.

YOUNG:
- оба 22 sec.

Wort:
- malted_barley x3
- hops x1
- forest_herbal_infusion x2
- water_bucket x1

Process:
1600 ticks.

Поскольку это особый травяной brew, НЕ применять magical penalty multiplier.
Это обычный special alcoholic beverage.

---

# 35. МАГИЧЕСКИЕ СОРТА ПИВА

Три обязательных:

1. Thunder Beer.
2. Veles Dark Beer.
3. Witch Berry Beer.

Все:
- overload +2.
- magical negative multiplier.
- stack 8.
- filled container wooden mug по умолчанию.

---

# 36. ГРОМОВОЕ ПИВО

ID:
`thunder_beer_mug`

RU:
`Громовое пиво`

EN:
`Thunder Beer`

## Design

- тёмно-золотое/янтарное;
- яркая белая пена;
- несколько голубых искристых pixels;
- 1–2 blue-white particle accents при drinking.

Не делать emissive всю кружку.
Допустим слабый emissive accent, если current rendering সহজо поддерживает.

## Positive

READY:
- Speed I — 60 sec.
- Haste I — 60 sec.

AGED:
- 72 sec оба.

YOUNG:
- 30 sec оба.

Overload:
+2.

Magical penalty:
YES.

## Wort

- hopped_beer base ingredients:
  - malted_barley x4
  - hops x2
- thunder_crystal_powder x1
- honey_infusion x1
- water_bucket x1

Process:
1800 ticks.

Если thunder_crystal_powder recipe disabled because source magical item absent:
- этот recipe также disabled.

---

# 37. ВЕЛЕСОВО ТЁМНОЕ ПИВО

ID:
`veles_dark_beer_mug`

RU:
`Велесово тёмное пиво`

EN:
`Veles Dark Beer`

## Design

- почти чёрно-коричневая жидкость;
- фиолетово-зелёный отблеск;
- серо-кремовая пена;
- 1–2 тёмно-фиолетовых magical pixels.

## Magic catalyst lookup

Сделать ОДИН минимальный search текущих item ids по:
- veles;
- staff_of_veles;
- rune + veles.

Приоритет:
1. существующая Veles rune / consumable Veles ingredient, если есть;
2. если такой consumable отсутствует, использовать `Staff of Veles` как NON-CONSUMED catalyst в vat catalyst slot.

Staff:
- не уничтожать;
- не уменьшать durability;
- только наличие catalyst требуется.

Если вообще никакого Veles-related item не найдено:
- recipe disabled;
- item можно зарегистрировать, но recipe отсутствует;
- final report:
  `No Veles-aligned magical item was present; Veles Dark Beer recipe was not enabled.`

## Positive

READY:
- Resistance I — 60 sec.
- Night Vision I — 45 sec.

AGED:
- Resistance 72 sec;
- Night Vision 54 sec.

YOUNG:
- Resistance 30 sec;
- Night Vision 22 sec.

Overload:
+2.

Magical penalty:
YES.

Wort:
- malted_barley x5
- hops x1
- forest_herbal_infusion x1
- optional/required Veles catalyst
- water_bucket x1

Process:
2000 ticks.

---

# 38. ЯГОДНОЕ КОЛДОВСКОЕ ПИВО

ID:
`witch_berry_beer_mug`

RU:
`Ягодное колдовское пиво`

EN:
`Witch Berry Beer`

## Design

- насыщенная бордово-фиолетовая жидкость;
- розовато-кремовая пена;
- 3–4 тёмные berry accents;
- лёгкие purple particles при употреблении.

## Magical catalyst

Минимальный search:
- rune;
- amulet;
- magical herb/ingredient already present.

Приоритет:
1. существующий low-tier rune / consumable magical reagent;
2. если only generic amulet exists, use amulet as NON-CONSUMED catalyst.

Не расходовать уникальный мощный артефакт без явного consumable id.

Если подходящий magical catalyst отсутствует:
- recipe может использовать thunder_crystal_powder x1 как fallback magical reagent.
- это разрешённый fallback.

## Positive

READY:
- Luck I — 90 sec.

AGED:
- 108 sec.

YOUNG:
- 45 sec.

Overload:
+2.

Magical penalty:
YES.

Wort:
- malted_barley x3
- hops x1
- berry_mash x2
- magical catalyst/reagent
- water_bucket x1

Process:
1800 ticks.

---

# 39. КВАС

ID:
`kvass_mug`

RU:
`Квас`

EN:
`Kvass`

Container:
wooden mug.

Stack:
16.

Appearance:
- тёмно-янтарно-коричневый;
- очень небольшая пена;
- хлебный тёплый оттенок.

Nutrition:
2.

Saturation modifier:
0.35.

Positive status effects:
NONE.

Overload:
0.

Это НЕ алкогольный штрафной напиток в геймплейной системе.

Wort:
- rye_bread x1
- rye_flour x1
- honey_infusion x1
- water_bucket x1

Process vat:
1000 ticks.

Fermentation barrel:
использует те же quality stages.

YOUNG/READY/AGED:
nutrition/saturation не меняются.
No positive effect.

SPOILED:
generic spoiled_brew.

---

# 40. МЕДОВУХА

ID:
`mead_bottle`

RU:
`Медовуха`

EN:
`Mead`

Container:
dark_bottle by default.
Pitcher may store it.

Stack:
16.

Appearance:
- прозрачный тёпло-золотой цвет;
- amber highlight;
- без beer foam в bottle.

Positive READY:
- Regeneration I — 8 sec.

AGED:
- 10 sec.

YOUNG:
- 4 sec.

Overload:
+1.

Не давать Regeneration II.

Must:
- honey_infusion x3
- water_bucket x1

Process:
1600 ticks vat -> mead_must.

Fermentation:
same windows.

After drink:
- return dark_bottle.

---

# 41. ЯБЛОЧНЫЙ СИДР

ID:
`apple_cider_bottle`

RU:
`Яблочный сидр`

EN:
`Apple Cider`

Container:
dark_bottle.

Appearance:
- прозрачный золотисто-яблочный;
- чуть темнее apple juice;
- faint bubbles.

Positive READY:
- Jump Boost I — 45 sec.

AGED:
- 54 sec.

YOUNG:
- 22 sec.

Overload:
+1.

Must:
- apple_juice_bottle x4

Vat:
convert to cider_must:
600 ticks.

Return empty dark bottles appropriately only if juice is consumed into internal batch.
Use return slots / container handling safely.

---

# 42. ЯГОДНЫЙ МОРС

ID:
`berry_mors_pitcher`
и serving:
`berry_mors_mug`

RU:
`Ягодный морс`

EN:
`Berry Mors`

Это НЕ алкогольный напиток.

Overload:
0.

Effects:
NONE.

Nutrition:
2.

Saturation:
0.30.

Appearance:
- красно-бордовый напиток;
- без пены;
- bright berry tint.

Recipe in Vat:
- berry_mash x2
- water_bucket x1
- honey_bottle x1

Process:
600 ticks.

No barrel fermentation required.

Output:
1 filled ceramic pitcher with 4 servings.

Pitcher:
- right click wooden_mug -> transfer one serving to berry_mors_mug.
- pitcher count decreases.

After mug drink:
- wooden_mug returns.

This gives non-alcoholic household drink.

---

# 43. КУВШИН — SERVING LOGIC

Filled pitcher stores:
- beverage id;
- servings 1..4.

Accepted beverages:
- kvass
- mead
- berry mors
- apple cider

Not used for:
- magical beers by default.

Right click empty wooden_mug:
- if beverage has mug serving variant -> give it.

Right click dark_bottle:
- if beverage has bottle variant -> fill bottle.

When servings=0:
- pitcher becomes empty ceramic_pitcher.

No fluid API.

---

# 44. МАЛЫЙ БОЧОНОК — SERVING LOGIC

8 servings same beverage.

Best for:
- beers;
- kvass;
- cider;
- mead.

Not fermentation.

Visual fullness:
blockstate 0..3 optional:
- empty;
- low;
- half;
- full.

Do not show exact color outside unless simple small tap/badge texture solution exists.
Do not spend many tokens on dynamic liquid rendering.

---

# 45. DRINK USE BEHAVIOR

Alcoholic beverage:
- normal drink animation;
- 32 ticks consumption time.

On finish:
1. determine quality;
2. apply positive effect durations;
3. increment overload points;
4. apply current negative stage;
5. return empty container.

Magical:
same, but +2 and magical penalty multiplier.

Non-alcoholic:
- apply nutrition/saturation if specified;
- overload unchanged.

Do not allow beverage effect stacking to arbitrary amplifier:
- same positive effect refreshes duration;
- standard vanilla same-amplifier semantics.
- do not increase amplifier by repeatedly drinking same beer.

---

# 46. VISUAL PARTICLES

Ordinary beer:
- no special particles.

Thunder Beer:
- 3–5 small electric/light particles after drink.
- reuse safe vanilla particles; do not add custom animated texture unless trivial.

Forest Beer:
- 2–4 green happy/plant-like particles.

Veles Dark:
- 2–4 subtle portal/witch-like dark particles.

Witch Berry:
- 2–4 witch/purple particles.

No screen shaders.

---

# 47. SOUND

Do not create a huge custom audio pack.

Use:
- generic drinking sound;
- bottle cork/pop-like vanilla sound;
- barrel wood/container sound;
- press wood/piston-like sound;
- vat bubbling using subtle vanilla bubble sounds.

If existing project custom sound helper is simple, add:
- `block.fermentation_vat.bubble`
using existing vanilla sound references or one simple custom event.

Do not download audio from internet.

---

# 48. CREATIVE TABS

## Farming & Plants
- hops_cutting
- hops

## Resources & Materials
- malted_barley
- malt_sack
- fruit_pomace
- berry_mash
- honey_infusion
- forest_herbal_infusion

## Magic & Artifacts
- thunder_crystal_powder

## Food & Kitchen
- wooden_mug
- dark_bottle
- ceramic_pitcher
- apple_juice_bottle
- all filled beverage items
- spoiled_brew

## Decor & Blocks
- small_keg
- fruit_press
- fermentation_vat
- wooden_barrel already remains in existing category

Technical wort/must items:
- hide from creative if possible.

---

# 49. TAGS

Create:

`slavicmyths:brewing/hops`
- hops

`slavicmyths:brewing/malt`
- malted_barley

`slavicmyths:brewing/berries`
- reuse existing berries tag if possible.

`slavicmyths:drinks/alcoholic`
- light_beer_mug
- dark_beer_mug
- hopped_beer_mug
- honey_beer_mug
- forest_beer_mug
- mead_bottle
- apple_cider_bottle

`slavicmyths:drinks/magical`
- thunder_beer_mug
- veles_dark_beer_mug
- witch_berry_beer_mug

`slavicmyths:drinks/non_alcoholic`
- kvass_mug
- berry_mors_mug
- apple_juice_bottle

Do not rely ONLY on tags for exact overload values; beverage data should expose load points.

---

# 50. BEVERAGE DATA MODEL

Do not hardcode each drink behavior in separate repeated class if reusable data component/class can handle it.

Create small BeverageDefinition / config-like mapping:

Fields:
- beverage id;
- base container type;
- overload points;
- magical boolean;
- nutrition;
- saturation;
- positive effects list;
- quality support;
- display color/visual key.

Can be data-driven JSON if project architecture supports simple reloadable definitions.
But do not build a generic scripting engine.

Static registry/config mapping in code is acceptable if simpler and clear.

---

# 51. FERMENTATION DATA MODEL

Barrel batch stores:
- beverage id / definition key;
- quality timer age;
- servings;
- mode FERMENTATION.

Do not store full duplicated effect list in NBT.

Save:
- id;
- age;
- servings.

Derived:
- current quality stage from age.

---

# 52. PLAYER OVERLOAD DATA

Use NeoForge recommended persistent player attachment/data for 1.21.1.

Fields:
- int overloadPoints;
- long lastOverloadDrinkGameTime;
- long lastDecayGameTime or equivalent.

Sync:
- server authoritative.
- client sync only if needed for tooltip/UI.
- no every-tick packet.

Decay:
can be handled:
- server player tick with cheap once-per-second check;
OR
- on drink event compute elapsed time and subtract floor(elapsed/1200).

PREFERRED:
lazy decay on drink/access:
- store last relevant game time;
- before adding new points:
  - calculate how many full 1200-tick intervals passed;
  - subtract that many points;
  - clamp 0.
This avoids a per-player tick handler.

This is the preferred token/performance-efficient implementation.

---

# 53. EFFECT APPLICATION AND QUALITY

Before drinking:
- normalize overload via lazy decay.

Then:
- apply base positive effects according to quality.
- add load points.
- derive negative stage after addition.
- apply negative effects.
- magical drink uses magical modifiers.

Order does not materially affect output but must be consistent.

---

# 54. TOOLTIP

For filled beverages:

Display small concise lines:

RU example:
`Качество: Готов`
`Опьянение: +1`

Magical:
`Качество: Готов`
`Магическая нагрузка: +2`

Do NOT list all hidden formulas.

Potion-style effect lines can use vanilla tooltip if easy.

For YOUNG/AGED:
quality line changes.

No exact fermentation seconds in item tooltip.

---

# 55. ADVANCEMENTS

Create 2.

## 55.1 `first_brew`

RU:
`Первый напиток`

Description:
`Свари свой первый напиток`

EN:
`First Brew`
`Brew your first fermented drink`

Trigger:
obtain any READY beer/mead/cider.

Icon:
light_beer_mug.

Task.
No XP.

## 55.2 `too_much`

RU:
`Кажется, хватит`

Description:
`Доведи опьянение до опасного уровня`

EN:
`Maybe That's Enough`
`Reach a dangerous level of intoxication`

Trigger:
overload points >=5.

Icon:
dark_beer_mug.

Task.
No XP.

Do not create more achievements.

---

# 56. LOCALIZATION

At minimum ru_ru / en_us for every new item/block/effect text.

Mandatory RU names:

- Хмель
- Черенок хмеля
- Солодовый ячмень
- Мешок солода
- Деревянная кружка
- Тёмная бутылка
- Керамический кувшин
- Малый бочонок
- Пресс
- Жмых
- Ягодная мезга
- Яблочный сок
- Медовый настой
- Лесной травяной настой
- Порошок громового кристалла
- Бродильный чан
- Светлое пиво
- Тёмное пиво
- Хмельное пиво
- Медовое пиво
- Лесное пиво
- Громовое пиво
- Велесово тёмное пиво
- Ягодное колдовское пиво
- Квас
- Медовуха
- Яблочный сидр
- Ягодный морс
- Испорченный напиток
- Молодой
- Готов
- Выдержанный
- Испорчен
- Качество: %s
- Опьянение: +%s
- Магическая нагрузка: +%s
- Первый напиток
- Кажется, хватит

EN equivalents:
- Hops
- Hop Cutting
- Malted Barley
- Malt Sack
- Wooden Mug
- Dark Bottle
- Ceramic Pitcher
- Small Keg
- Fruit Press
- Fruit Pomace
- Berry Mash
- Apple Juice
- Honey Infusion
- Forest Herbal Infusion
- Thunder Crystal Powder
- Fermentation Vat
- Light Beer
- Dark Beer
- Hopped Beer
- Honey Beer
- Forest Beer
- Thunder Beer
- Veles Dark Beer
- Witch Berry Beer
- Kvass
- Mead
- Apple Cider
- Berry Mors
- Spoiled Brew
- Young
- Ready
- Aged
- Spoiled
- Quality: %s
- Intoxication: +%s
- Magical Load: +%s
- First Brew
- Maybe That's Enough

---

# 57. JEI / RECIPE VIEWER

If existing optional JEI integration is easy to extend:
add categories:
- Fermentation Vat;
- Fermentation Barrel;
- Fruit Press.

Show:
- ingredients;
- catalyst if any;
- result;
- broad stage labels, not exact perfect timing requirement.

No hard dependency.

If not easy:
- skip new category;
- do not waste tokens rebuilding JEI.

Report one line.

---

# 58. NO DUPLICATION / CONTAINER RULES

All filled containers must correctly return empties:

Beer mug -> wooden_mug.
Kvass mug -> wooden_mug.
Berry mors mug -> wooden_mug.
Bottle beverages -> dark_bottle.
Pitcher -> ceramic_pitcher when empty.

Transfer:
- never duplicate serving.
- source servings -1 exactly when target fill succeeds.
- if player inventory full, do not consume source unless output can be placed/drop safely.

Small keg:
same.

Breaking filled keg:
- drop block item + preserve beverage servings via item component if current container conventions make this easy;
OR
- if that is complex, drop filled beverage servings as containers and empty keg.
Choose one method and avoid loss/dupe.

Preferred:
drop beverages as filled wooden mugs/bottles plus empty keg.

---

# 59. SPOILAGE AND BARREL BREAK

Breaking fermentation barrel:

If STORAGE:
- behave exactly like 1.1.6.

If FERMENTATION:
- barrel block drops;
- active batch is LOST.
- do not convert to ready beverage.
- optionally drop one generic `spoiled_brew` only if stage SPOILED and this is easy.
Primary rule:
no duplication.

Do not preserve active fermentation in block item.

---

# 60. PERFORMANCE

No:
- global barrel manager;
- every-tick world scan;
- global brewery tick registry;
- per-player network spam.

Allowed:
- active Fermentation Vat BE tick;
- fermentation barrel BE tick only in FERMENTATION;
- lazy overload decay;
- fruit press manual, no ticking.

If barrel in STORAGE:
- no fermentation tick work.

---

# 61. НЕ ДОБАВЛЯТЬ В 1.1.7

- vineyards;
- wine;
- vodka;
- distilled spirits;
- moonshine;
- absinthe;
- distillation apparatus;
- temperature brewing;
- water purity;
- random recipe mutations;
- alcohol addiction;
- hangover next day;
- blackout teleport;
- permanent poisoning;
- custom camera wobble;
- drunk chat;
- NPC taverns;
- tavern structures;
- drinking contests;
- new gods;
- living water;
- dead water.

---

# 62. ВЕРСИЯ

Update mod version source of truth:
`1.1.7`

No duplicate version constants.

---

# 63. BUILD

After implementation:

1. Do NOT run Minecraft.
2. Run ONE final compile/build.
3. If success:
   - continue to Git + PolyMC transfer.
4. If fail due to 1.1.7 code:
   - fix;
   - one repeat build allowed.
5. If fail due to independent pre-existing error:
   - do not repair whole project;
   - report exact blocker;
   - PolyMC transfer is NOT allowed without a successful JAR.

---

# 64. GIT FINISH

Branch:
`main`

Commit:
`feat: implement Slavic Myths 1.1.7 brewing and fermentation`

If origin auth works:
- push origin main.

No release branch.
No tag.
No GitHub Release.
No force push.

---

# 65. POLYMC TRANSFER — ОБЯЗАТЕЛЬНО ПОСЛЕ УСПЕШНОГО BUILD

This is a direct user request for 1.1.7.

Goal:
place the newly built Slavic Myths 1.1.7 JAR into the PolyMC instance the user already uses for testing.

## 65.1 Find target instance economically

DO NOT search entire disk.

Check in order:

1. Repository documentation/scripts/config for an already known PolyMC instance/mods path.
2. Environment/user config references that are directly obvious.
3. Standard Windows paths only:
   - `%APPDATA%\PolyMC\instances`
   - `%LOCALAPPDATA%\PolyMC\instances`
   - current user's obvious portable PolyMC folder only if already referenced by existing project files.
4. Prefer the UNIQUE instance whose `mods` directory already contains an older Slavic Myths JAR.

Identification:
- existing filename contains `slavic` / project mod name;
- or JAR metadata mod id matches Slavic Myths, but inspect only likely candidate JARs, not every JAR in every instance.

## 65.2 If exactly one target instance found

In its `mods` folder:

1. Find old Slavic Myths JAR(s) belonging to THIS mod only.
2. Remove/replace old Slavic Myths testing JAR.
3. Copy newly built 1.1.7 JAR.
4. Verify copied file exists and file size > 0.
5. Do NOT touch other mods.
6. Do NOT launch PolyMC.
7. Do NOT launch Minecraft.

If backup is trivial:
- rename old JAR to `.bak` outside `mods` or move to a nearby backup folder;
- but do not leave old `.jar` active in mods.

## 65.3 If zero or multiple ambiguous target instances

Do NOT guess.

Do NOT copy into random instance.

Final report:
`PolyMC transfer: blocked — no unique existing Slavic Myths test instance could be identified.`

This is the only acceptable reason not to transfer after successful build.

## 65.4 Important

PolyMC transfer happens AFTER:
- successful build;
- commit;
- push if available.

The user will then manually launch and test.

---

# 66. КРИТЕРИИ ГОТОВНОСТИ

## Hops
[ ] hops_crop.
[ ] hops_cutting.
[ ] hops item.
[ ] growth stages.
[ ] tall mature plant.
[ ] mature loot.
[ ] sickle integration.
[ ] fertilizer integration.

## Ingredients
[ ] malted_barley.
[ ] malt_sack.
[ ] fruit_pomace.
[ ] berry_mash.
[ ] apple_juice_bottle.
[ ] honey_infusion.
[ ] forest_herbal_infusion.
[ ] thunder_crystal_powder if source exists.

## Containers
[ ] wooden_mug.
[ ] dark_bottle.
[ ] ceramic_pitcher.
[ ] small_keg.
[ ] correct empty returns.
[ ] no serving duplication.

## Equipment
[ ] fruit_press.
[ ] manual 3-action press.
[ ] fermentation_vat.
[ ] vat recipes.
[ ] existing wooden_barrel extended.
[ ] old storage barrels load correctly.
[ ] fermentation mode only when batch inserted.

## Fermentation
[ ] game-tick timing only.
[ ] Young 0–2399.
[ ] Ready 2400–8399.
[ ] Aged 8400–11999.
[ ] Spoiled >=12000.
[ ] no real-world time.
[ ] unloaded chunk pauses.
[ ] quality saved.
[ ] 8 servings per batch.

## Beers
[ ] light beer.
[ ] dark beer.
[ ] hopped beer.
[ ] honey beer.
[ ] forest beer.
[ ] thunder beer.
[ ] Veles dark beer.
[ ] witch berry beer.

## Other drinks
[ ] kvass.
[ ] mead.
[ ] apple cider.
[ ] berry mors.
[ ] apple juice.

## Positive effects
[ ] Light = Speed I.
[ ] Dark = Resistance I.
[ ] Hopped = Haste I.
[ ] Honey = Absorption I.
[ ] Forest = Night Vision + Luck I.
[ ] Thunder = Speed + Haste.
[ ] Veles = Resistance + Night Vision.
[ ] Witch Berry = Luck.
[ ] Mead = Regeneration I.
[ ] Cider = Jump Boost I.
[ ] Kvass no status effect.
[ ] Mors no status effect.

## Overload
[ ] hidden persistent player points.
[ ] regular alcohol +1.
[ ] magical +2.
[ ] non-alcoholic +0.
[ ] 60 sec per point natural decay.
[ ] lazy decay preferred.
[ ] after full decay next drink starts safely.
[ ] death resets points.
[ ] milk does NOT reset hidden counter.
[ ] stage 2 = Nausea.
[ ] stage 3 = Nausea + Weakness.
[ ] stage 4 = Nausea + Weakness + Slowness.
[ ] 5+ = stronger + short Poison.
[ ] magical drinks make penalties stronger/longer.
[ ] all negative effects temporary.
[ ] no permanent intoxication.

## Quality
[ ] Young = 50% positive duration.
[ ] Ready = 100%.
[ ] Aged = 120%.
[ ] Spoiled = no positive + nausea/poison.

## Data/UI
[ ] ru_ru.
[ ] en_us.
[ ] tags.
[ ] recipes.
[ ] advancements.
[ ] simple status UI.
[ ] no exact second pressure.
[ ] creative tabs correct.

## Workflow
[ ] tokens used economically.
[ ] no full project audit.
[ ] Minecraft not launched.
[ ] version 1.1.7.
[ ] main only.
[ ] no release/tag.
[ ] successful build.

## PolyMC
[ ] after successful build attempted unique instance detection.
[ ] if unique instance found, old mod JAR replaced.
[ ] new 1.1.7 JAR exists in mods.
[ ] other mods untouched.
[ ] PolyMC NOT launched.
[ ] Minecraft NOT launched.

---

# 67. MANUAL TEST CHECKLIST ДЛЯ ПОЛЬЗОВАТЕЛЯ

Codex НЕ выполняет эти game tests.
After PolyMC transfer user will test manually.

Final report checklist:

1. Вырастить и собрать хмель.
2. Проверить sickle на зрелом хмеле.
3. Сделать malted barley.
4. Проверить fruit press на apples.
5. Проверить fruit press на berries.
6. Приготовить honey infusion и forest infusion.
7. Сделать обычный beer wort.
8. Перелить batch в existing wooden_barrel.
9. Проверить Young / Ready / Aged / Spoiled stages.
10. Разлить по mugs/bottles/pitcher.
11. Проверить small keg.
12. Выпить одну кружку обычного пива — негативных эффектов быть не должно.
13. Выпить вторую подряд — проверить Nausea.
14. Довести overload до 3/4/5 и проверить постепенное усиление Weakness/Slowness/Poison.
15. Подождать спад overload и убедиться, что после полного сброса следующая кружка снова безопасна.
16. Выпить magical beer и проверить +2 load и более сильные негативные последствия при злоупотреблении.
17. Проверить Thunder Beer.
18. Проверить Veles Dark Beer, если recipe enabled.
19. Проверить Witch Berry Beer.
20. Проверить kvass без overload.
21. Проверить berry mors без overload.
22. Проверить mead/cider.
23. Проверить empty container returns.
24. Проверить сохранение barrel fermentation после перезахода.
25. Проверить creative tabs / localization / advancements.
26. Запустить именно перенесённую PolyMC сборку вручную.

---

# 68. ФОРМАТ ИТОГОВОГО ОТЧЁТА CODEX

Коротко.

## Implemented
5–12 bullets.

## Build
- command;
- success/fail;
- JAR path.

## Optional magical integrations
Only if relevant:
- Thunder Stone found / not found.
- Veles catalyst found / not found.
- Witch catalyst used.

## Git
- branch;
- commit hash;
- push status.

## PolyMC
- detected instance name/path;
- old Slavic Myths JAR replaced: yes/no;
- new JAR path;
- PolyMC launch: NOT RUN.

If blocked:
- one-line exact reason.

## Manual test checklist
Short list from above.

НЕ писать:
- длинный архитектурный анализ;
- chain-of-thought;
- новые идеи 1.1.8;
- предложения дополнительных напитков.
