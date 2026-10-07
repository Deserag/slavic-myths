# SLAVIC MYTHS 1.1.3 — «ЖИВОТНОВОДСТВО»
## Финальный design document + implementation prompt для Codex

---

# 0. ОБЯЗАТЕЛЬНЫЕ ПРАВИЛА РАБОТЫ CODEX

Это не задача на свободный дизайн. Это точное техническое задание.
Реализуй описанное ниже без самостоятельного расширения scope.

## 0.1 Экономно расходовать токены

ОБЯЗАТЕЛЬНО работать экономно.

НЕ НАДО:
- проводить общий аудит всего Slavic Myths;
- читать весь репозиторий;
- исследовать старые системы, которые не относятся к 1.1.3;
- выполнять длинный архитектурный анализ;
- писать большие планы вместо внесения изменений;
- рефакторить существующие системы «заодно»;
- переделывать код 1.1.0/1.1.2 без необходимости;
- несколько раз искать одни и те же сущности/регистрации;
- многократно запускать build после каждой мелкой правки;
- тратить токены на PolyMC;
- переносить JAR;
- заниматься GitHub Release;
- придумывать дополнительные механики.

Разрешён только минимальный просмотр файлов, необходимых для:
- существующих entity registries;
- item/block registries;
- sounds registrations;
- creative tabs из 1.1.2;
- существующих helper/base классов;
- текущей системы worldgen/biome modifiers;
- food/crop items 1.1.0;
- organic farming integration, если нужна для feed tags;
- package/resource/data conventions.

Принцип:
**нашёл нужную точку интеграции -> внес правку -> двигайся дальше.**

## 0.2 Minecraft не запускать

ЗАПРЕЩЕНО:
- runClient;
- runServer;
- запуск Minecraft;
- создание тестового мира;
- игровой тест.

Пользователь сам тестирует мод вручную.

Разрешено:
- одна финальная compile/build проверка;
- максимум один повторный build, если первая ошибка вызвана именно изменениями 1.1.3 и после исправления нужно подтвердить компиляцию.

## 0.3 PolyMC не трогать

После реализации:
- НЕ копировать JAR в PolyMC;
- НЕ менять instance;
- НЕ запускать PolyMC;
- НЕ обновлять mods folder.

Перенос будет отдельной задачей только по прямой команде пользователя.

## 0.4 Никакой самостоятельной фантазии

НЕ добавлять:
- других домашних животных;
- пол животных;
- отдельные male/female entity types;
- отдельные модели самцов/самок;
- отдельные яйца призыва для самцов/самок;
- отдельные яйца призыва для детёнышей;
- инкубатор;
- систему жажды;
- поилку;
- автоматическое бесконечное размножение;
- сыр;
- масло;
- жир;
- туши;
- разделочную систему;
- новые шкуры;
- новые виды перьев;
- солому как новый item;
- сложное животноводческое GUI;
- профессии NPC;
- загон/хлев как структуру;
- новые биомы.

Если технически возможны несколько вариантов реализации, выбрать самый простой устойчивый вариант, сохраняющий заданное поведение.

## 0.5 Платформа

- Minecraft 1.21.1
- NeoForge
- Java 21
- текущий mod id проекта
- текущая package structure

Не менять loader/version/platform.

## 0.6 Git

Работать только в:
`main`

После реализации:
- commit только в `main`;
- push в `origin/main`, если авторизация уже работает;
- НЕ трогать release branch;
- НЕ создавать tag;
- НЕ создавать GitHub Release;
- НЕ force-push;
- НЕ удалять чужие незакоммиченные изменения.

Commit message:
`feat: implement Slavic Myths 1.1.3 animal husbandry`

Если push требует настройки авторизации:
- не тратить токены;
- оставить локальный commit;
- сообщить одну команду push в итоговом отчёте.

---

# 1. ЦЕЛЬ ВЕРСИИ

1.1.3 добавляет полноценный хозяйственный слой животных после:
- 1.1.0 — полевые культуры;
- 1.1.2 — сады, ягоды и яблоня.

Новые животные:
1. Гусь.
2. Утка.
3. Домашняя коза.

Также версия ОБЯЗАТЕЛЬНО интегрирует ванильных хозяйственных животных с новыми культурами:
- корова;
- овца;
- свинья;
- курица;
- кролик.

Главные системы:
- новые модели и анимации животных;
- детёныши внутри тех же entity types;
- breeding;
- новые корма;
- кормушка;
- гнездо;
- яйца гуся и утки;
- козье молоко;
- новое мясо;
- плавание/поведение птиц;
- защитное поведение гуся;
- естественный spawn;
- звуки;
- spawn eggs;
- creative tab integration;
- advancement.

---

# 2. КРИТИЧЕСКОЕ ПРАВИЛО: НЕТ ПОЛОВ И НЕТ ОТДЕЛЬНЫХ ЯИЦ ДЕТЁНЫШЕЙ

В 1.1.3 НЕ существует разделения:
- самец;
- самка.

НЕ создавать:
- `male_goose`;
- `female_goose`;
- `drake`;
- `female_duck`;
- `buck`;
- `doe`;
- любые gender NBT/data components;
- gender-specific textures;
- gender-specific breeding checks.

Minecraft-style breeding остаётся абстрактным:
два взрослых животных одного вида + подходящий корм -> один детёныш.

Для каждого нового животного существует РОВНО ОДНО яйцо призыва:

- `goose_spawn_egg`
- `duck_spawn_egg`
- `domestic_goat_spawn_egg`

НЕ создавать:
- gosling spawn egg;
- duckling spawn egg;
- kid spawn egg;
- male/female spawn eggs.

Spawn egg по умолчанию создаёт взрослое животное.
Детёныш появляется:
- через breeding;
- через стандартное baby/age состояние entity;
- через команды/NBT разработчика, если Minecraft это позволяет.

Один entity type на вид:
- `goose`
- `duck`
- `domestic_goat`

Baby — это состояние того же entity type.

---

# 3. ВИЗУАЛЬНЫЙ РЕФЕРЕНС

Главный утверждённый референс:
`references/01_animal_husbandry_design.png`

Дизайн на нём утверждён пользователем.

Использовать его как основу:
- пропорций;
- окраса;
- формы тела;
- детёнышей;
- яиц;
- кормушки;
- гнезда;
- мяса;
- ведра козьего молока.

Если референс конфликтует с точными правилами ниже:
текстовое ТЗ имеет приоритет.

---

# 4. ГУСЬ — ENTITY `goose`

RU:
`Гусь`

EN:
`Goose`

## 4.1 Внешность взрослого гуся

Главный образ:
- крупная домашняя водоплавающая птица;
- белое/светло-серое тело;
- длинная шея;
- небольшая голова;
- оранжевый клюв;
- оранжевые лапы;
- хорошо заметные сложенные крылья;
- грудь немного выступает вперёд;
- хвост короткий;
- не похож на увеличенную курицу.

Модель:
- отдельные части:
  - body;
  - neck lower;
  - neck upper/head;
  - beak;
  - left wing;
  - right wing;
  - left leg;
  - right leg;
  - tail.
- голова и шея должны позволять поворот;
- крылья должны позволять отдельную анимацию.

Hitbox ориентир:
- width ≈ 0.70;
- height ≈ 1.15.

Не делать гигантским.

## 4.2 Цвет

Основное оперение:
- светло-серое/белое;
- нижние части и грудь немного светлее;
- крылья имеют серые тени;
- клюв/лапы насыщенно оранжевые;
- глаза маленькие тёмные.

Не делать чисто белую модель без shading.

## 4.3 Характеристики

Ориентир:
- max health: 12;
- movement speed: 0.22;
- follow range: 16;
- attack damage defensive nip: 1.0;
- knockback почти отсутствует.

Это хозяйственное животное, а не боевой моб.

## 4.4 Обычный AI

Приоритеты:
- float/swim;
- panic;
- breed;
- tempt;
- follow parent;
- stroll;
- look at player;
- random look.

Гусь:
- нормально ходит по земле;
- умеет плавать;
- не тонет;
- может входить в воду;
- не обязан постоянно искать воду.

## 4.5 Защитное поведение

Гусь имеет характерную защитную реакцию.

Триггеры:
1. игрок ударил взрослого гуся;
2. игрок ударил гусёнка;
3. не-креативный игрок подходит ближе примерно 2 блоков к гусёнку, рядом с которым есть взрослый гусь в радиусе примерно 6 блоков.

Реакция:
- взрослый гусь поворачивается к угрозе;
- вытягивает шею;
- воспроизводит hiss/alarm sound;
- короткая warning phase около 20 ticks;
- если угроза остаётся/была атакой — коротко преследует цель;
- максимальная defensive chase длится около 80 ticks;
- не преследует далеко: потеря интереса примерно после 8 blocks;
- укус наносит 1 damage;
- attack cooldown около 20 ticks.

После окончания:
- гусь возвращается к обычному passive AI.

Не атаковать:
- creative player;
- spectator.

Не делать гуся постоянно hostile/neutral как wolf.
Это короткая хозяйственная защитная реакция.

---

# 5. ГУСЁНОК

Это baby-state entity `goose`, НЕ отдельный entity.

Внешность:
- тело заметно округлее;
- короткая шея;
- большая относительно тела голова;
- маленькие крылья;
- серо-жёлтый/кремовый пух;
- маленький оранжевый клюв;
- короткие оранжевые лапки.

НЕ просто масштабировать взрослую модель целиком.
Использовать baby-specific transforms/proportions:
- голова относительно крупнее;
- шея короче;
- крылья меньше;
- ноги короче.

Поведение:
- следует за взрослыми;
- не участвует в breeding;
- не откладывает яйца;
- не атакует;
- взрослеет vanilla-style.

---

# 6. УТКА — ENTITY `duck`

RU:
`Утка`

EN:
`Duck`

## 6.1 Внешность

Утка меньше гуся.

Силуэт:
- низкое овальное тело;
- короткая шея;
- плоский широкий клюв;
- короткие лапы;
- хорошо видимое сложенное крыло;
- хвост короткий.

Основной окрас — один утверждённый вариант.
Не создавать половые варианты окраса.

Палитра:
- коричневое тело;
- более тёмная коричневая голова;
- светлая грудь/нижняя сторона;
- небольшой сине-серый акцент на крыле допустим;
- оранжевый клюв;
- оранжевые лапы.

Hitbox ориентир:
- width ≈ 0.65;
- height ≈ 0.70.

## 6.2 Характеристики

- max health: 10;
- land movement speed: 0.20;
- follow range: 16;
- attack damage отсутствует.

Утка полностью passive.

## 6.3 Поведение

AI:
- float/swim;
- panic;
- breed;
- tempt;
- follow parent;
- water preference;
- stroll;
- look.

Ключевая черта:
утка любит воду сильнее гуся.

Если поблизости есть доступная вода:
- периодически выбирает её как прогулочную цель;
- спокойно плавает;
- затем может выйти на берег.

Не заставлять утку постоянно находиться в воде.
Не делать aquatic MobCategory.
Не делать её рыбой.

В воде:
- движение должно выглядеть плавным;
- тело немного выше линии воды;
- лапы можно анимировать как гребки, если текущая animation architecture позволяет без лишнего рефакторинга.

---

# 7. УТЁНОК

Baby-state entity `duck`.

Не отдельный entity.
Не отдельное spawn egg.

Внешность:
- маленькое округлое тело;
- кремово-жёлтый/светло-коричневый пух;
- тёмная полоска/пятно допустимы;
- крупная голова;
- короткий оранжевый клюв;
- очень маленькие крылья.

Не просто уменьшенная взрослая утка.

Поведение:
- следует за взрослыми;
- умеет плавать;
- не откладывает яйца;
- не размножается;
- взрослеет стандартно.

---

# 8. ДОМАШНЯЯ КОЗА — ENTITY `domestic_goat`

RU:
`Домашняя коза`

EN:
`Domestic Goat`

Это НЕ vanilla `minecraft:goat`.

## 8.1 Внешность

Должна выглядеть домашней, а не горной.

Силуэт:
- более компактное тело;
- умеренно короткие ноги;
- менее массивная грудь;
- небольшая бородка допустима;
- уши заметные;
- рога короче и мягче изогнуты назад;
- хвост короткий.

Окрас:
- бело-кремовая основа;
- серо-коричневые пятна;
- морда светлая;
- копыта тёмные;
- рога серо-бежевые.

Допустим небольшой декоративный ошейник/ремешок, если он соответствует утверждённому референсу.
Не превращать ошейник в отдельную механику/item.

Hitbox ориентир:
- width ≈ 0.90;
- height ≈ 1.25.

## 8.2 Характеристики

- max health: 18;
- movement speed: 0.23;
- follow range: 16;
- attack отсутствует.

## 8.3 Отличия от vanilla goat

Домашняя коза:
- НЕ ram-ит игрока;
- НЕ имеет screaming-goat механики;
- НЕ делает экстремальные высокие прыжки;
- НЕ ищет скалы;
- ведёт себя как спокойное сельскохозяйственное животное.

AI:
- float;
- panic;
- breed;
- tempt;
- follow parent;
- stroll;
- look at player;
- random look.

---

# 9. КОЗЛЁНОК

Baby-state `domestic_goat`.

Не отдельный entity.
Не отдельное spawn egg.

Внешность:
- большая относительно тела голова;
- короткие ноги;
- маленькие уши;
- рога отсутствуют либо представлены очень маленькими бугорками;
- пятна соответствуют взрослой палитре;
- тело более округлое.

Не просто uniform-scale взрослой модели.

---

# 10. SPAWN EGGS

Создать РОВНО 3:

- `goose_spawn_egg`
- `duck_spawn_egg`
- `domestic_goat_spawn_egg`

Creative tab:
`Мобы / спавн-яйца` / `Mobs / Spawn Eggs`

НЕ создавать:
- baby spawn eggs;
- gender spawn eggs;
- egg variants.

Цвета spawn eggs должны соответствовать существам:
- goose: светло-серый + оранжевый;
- duck: коричневый + оранжевый;
- domestic goat: кремовый + серо-коричневый.

---

# 11. BREEDING И КОРМА НОВЫХ ЖИВОТНЫХ

Использовать food tags, чтобы код не был разбросан по hardcoded проверкам.

Создать item tags:

`slavicmyths:animal_feed/goose`
- `slavicmyths:oat_grain`
- `slavicmyths:barley_grain`

`slavicmyths:animal_feed/duck`
- `slavicmyths:oat_grain`
- `slavicmyths:barley_grain`
- `slavicmyths:pea_pod`

`slavicmyths:animal_feed/domestic_goat`
- `minecraft:wheat`
- `slavicmyths:oat_grain`
- `slavicmyths:cabbage`

Новые животные используют соответствующие tags для:
- temptation;
- breeding;
- player feeding.

Обычное vanilla-style поведение:
- взрослое животное, готовое к breeding -> love mode;
- baby -> ускорение взросления;
- consumption 1 item;
- creative player item не расходует.

Если животное ранено:
- успешное кормление дополнительно восстанавливает 2 health;
- не выше max health.

Не добавлять hunger meter.

---

# 12. ИНТЕГРАЦИЯ ВАНИЛЬНЫХ ЖИВОТНЫХ С НОВЫМИ КУЛЬТУРАМИ

Это ОБЯЗАТЕЛЬНАЯ часть 1.1.3.

НЕ заменять vanilla foods.
Новые продукты ДОБАВЛЯЮТСЯ к существующим.

Не менять модели ванильных животных.
Не менять их дроп.
Не менять базовые stats.
Не переписывать весь vanilla AI.

Нужно добавить только food/breeding/healing integration и поддержку кормушки.

## 12.1 Корова

Vanilla compatibility сохраняется:
- wheat продолжает работать.

Добавить:
- `rye_grain`
- `barley_grain`
- `oat_grain`

Эти новые корма:
- привлекают корову;
- могут активировать breeding;
- baby получают ускорение взросления;
- при успешном кормлении восстанавливается до 2 health.

## 12.2 Овца

Vanilla wheat сохраняется.

Добавить:
- `rye_grain`
- `barley_grain`
- `oat_grain`

Те же правила:
- temptation;
- breeding;
- baby growth;
- heal 2.

НЕ менять wool/shearing mechanics.

## 12.3 Свинья

Все vanilla breeding foods сохраняются:
- carrot;
- potato;
- beetroot.

Добавить:
- `turnip`.

Репа:
- temptation;
- breeding;
- baby growth;
- heal 2.

Не добавлять новые pig drops.

## 12.4 Курица

Все vanilla seeds сохраняются.

Добавить:
- `rye_seeds`
- `barley_seeds`
- `oat_seeds`
- `flax_seeds`

Новые seeds:
- temptation;
- breeding;
- chick growth;
- heal 2.

НЕ создавать новую chicken entity.
НЕ менять chicken egg item.
НЕ менять chicken model.

## 12.5 Кролик

Vanilla:
- carrot;
- golden carrot;
- dandelion
остаются рабочими.

Добавить:
- `cabbage`.

Капуста:
- temptation;
- breeding;
- baby growth;
- heal 2.

Не менять rabbit variants.

## 12.6 Техническая реализация vanilla feeding

Не использовать mixin, если можно обойтись NeoForge events + reusable helper.

Предпочтительный путь:
- использовать PlayerInteractEntityEvent / подходящий NeoForge interaction hook;
- если held item входит в дополнительный feed tag конкретного vanilla entity:
  - вызвать стандартно эквивалентное breeding/baby growth поведение;
  - consume item;
  - heal 2;
  - particles/sound;
  - корректно обработать server/client side;
  - отменить дальнейшую обработку только если custom feed реально использован.

Для temptation:
- при создании/добавлении сущности сервером безопасно добавить дополнительный TemptGoal с mod feed ingredient/tag;
- не дублировать goal многократно;
- не выполнять каждый tick глобальные world scans.

Если в текущем NeoForge 1.21.1 есть более прямой официальный hook для добавления food behavior — можно использовать его, но результат должен соответствовать этому ТЗ.

---

# 13. КОРМУШКА — BLOCK `feeder`

RU:
`Кормушка`

EN:
`Feeder`

## 13.1 Дизайн

Деревянное длинное корыто:
- прямоугольный trough;
- высота около 0.45–0.55 блока;
- деревянные боковые стенки;
- тёмные металлические крепления/обручи по краям;
- внутри виден корм при заполнении;
- низкие ножки/опоры;
- не полный cube.

Модель:
- отдельный custom block model;
- collision соответствует форме корыта настолько, насколько разумно без сложной voxel-геометрии.

## 13.2 Inventory

Использовать BlockEntity только для хранения корма.

Capacity:
- максимум 16 items;
- кормушка хранит только один тип корма одновременно.

Допустимы только items, которые входят хотя бы в один `slavicmyths:animal_feed/...` tag или в специальный общий:
`slavicmyths:animal_feed/all_valid`.

Создать/обновить этот общий tag так, чтобы туда входили:
- все новые feed items;
- relevant vanilla feed items.

## 13.3 Player interaction

Right click с допустимым кормом:
- если empty -> вставить 1;
- если уже тот же item и count < 16 -> вставить 1;
- если другой item -> ничего;
- consume 1, кроме creative.

Sneak + right click с допустимым кормом:
- вставить столько held items, сколько помещается до 16;
- consume соответствующее количество.

Right click пустой рукой:
- достать 1 item.

Sneak + right click пустой рукой:
- достать всё содержимое одним stack.

При разрушении кормушки:
- содержимое drop в мир;
- сама кормушка drop стандартно.

## 13.4 Visual fill state

BlockState:
`FILL_LEVEL = 0..3`

0:
- пусто.

1:
- 1–5 items.

2:
- 6–10.

3:
- 11–16.

Внутри модели:
- уровень корма становится заметно выше;
- текстура корма может быть generic зерново-травяной;
- НЕ пытаться отображать каждый item отдельной текстурой.

## 13.5 Animals using feeder

Кормушкой пользуются:
- goose;
- duck;
- domestic_goat;
- cow;
- sheep;
- pig;
- chicken;
- rabbit.

Поиск:
- radius около 10 blocks;
- не сканировать каждый tick;
- AI goal/поиск обновлять примерно раз в 40 ticks или использовать стандартный goal cadence.

Животное идёт к кормушке ТОЛЬКО если:
- оно baby;
ИЛИ
- health < max health.

Полностью здоровый взрослый моб НЕ расходует корм автоматически.

Когда достигает кормушки:
- проверить, подходит ли stored item именно этому animal type;
- consume 1;
- adult injured: heal 2 health;
- baby: heal 2 + ускорить взросление примерно на 400 ticks (20 секунд);
- поставить индивидуальный feeder cooldown около 600 ticks (30 секунд);
- звук еды;
- небольшие particles.

Кормушка НИКОГДА:
- не включает love mode;
- не вызывает auto breeding;
- не создаёт детёнышей автоматически.

Это принципиальное ограничение против бесконтрольного размножения.

---

# 14. РЕЦЕПТ КОРМУШКИ

Shaped recipe:

`P P`
`PNP`
`PPP`

Где:
- `P` = любой vanilla plank через plank tag;
- `N` = iron nugget.

Результат:
- 1 feeder.

Итого:
- 6 planks;
- 1 iron nugget.

Не требовать конкретный вид древесины.

---

# 15. СОЛОМЕННОЕ ГНЕЗДО — BLOCK `straw_nest`

RU:
`Соломенное гнездо`

EN:
`Straw Nest`

Не создавать отдельный straw item в 1.1.3.

## 15.1 Дизайн

Низкое круглое/овальное гнездо:
- жёлто-золотая сухая трава/солома;
- центральное углубление;
- высота около 0.25–0.35 блока;
- внешний край немного выше;
- хорошо различимо на земле.

Visual states:
- пустое;
- 1 яйцо;
- 2 яйца;
- 3 яйца.

## 15.2 Storage

BlockEntity:
- capacity = 3 egg items.

Допустимые items:
- `goose_egg`
- `duck_egg`
- `minecraft:egg`

Гнездо может хранить смешанные яйца.
Visual model показывает только count, а не точный вид яйца.

BlockState:
`EGG_COUNT = 0..3`

Inventory является source of truth.
BlockState синхронизируется с количеством.

## 15.3 Player interaction

Right click пустой рукой:
- достать одно яйцо, начиная с первого доступного.

Sneak + right click пустой рукой:
- достать все яйца и выдать/drop игроку.

Right click держа допустимое яйцо:
- вручную положить 1 яйцо, если есть место.

Sneak + right click egg:
- положить максимум возможного до capacity 3.

При разрушении:
- все яйца drop;
- block drop.

## 15.4 Автоматическое использование птицами

В 1.1.3 автоматически гнездо используют:
- goose;
- duck.

Когда у птицы заканчивается egg timer:
1. найти доступное straw_nest в радиусе 12 blocks;
2. если найдено и есть место:
   - птица идёт к гнезду;
   - когда находится примерно в 1.5 blocks, положить 1 соответствующее яйцо внутрь;
   - reset egg timer;
3. если гнезда нет/недоступно/полно:
   - drop egg рядом с собой обычным item entity;
   - reset timer.

Не заставлять птицу вечно пытаться дойти до недоступного гнезда.
Path timeout должен возвращать fallback drop.

Vanilla chicken:
- гнездо МОЖЕТ вручную хранить `minecraft:egg`;
- автоматическую перехватку vanilla chicken egg laying в 1.1.3 НЕ добавлять, если для этого потребуется mixin/инвазивное изменение `Chicken`.
- Не тратить токены на глубокое переписывание chicken AI ради гнезда.
- Основная переработка кур в этой версии — новые корма и кормушка.

---

# 16. РЕЦЕПТ ГНЕЗДА

Shaped:

`WWW`
`W W`
`WWW`

Где:
- `W` = minecraft:wheat

Результат:
- 1 `straw_nest`.

8 wheat.

Не создавать отдельную солому только ради рецепта.

---

# 17. ГУСИНОЕ ЯЙЦО — `goose_egg`

RU:
`Гусиное яйцо`

EN:
`Goose Egg`

Дизайн:
- крупнее куриного на item sprite;
- кремово-белое;
- вытянутая форма;
- мягкие бежевые тени;
- не полностью белый овал.

Stack:
- 16.

НЕ:
- spawn egg;
- throwable projectile;
- raw food;
- hatch item.

В 1.1.3 это хозяйственный продукт и будущий ингредиент.

Egg laying:
- только adult goose;
- random interval 9600–18000 ticks;
- baby никогда не откладывает.

Поскольку пола нет:
- любой adult goose технически может отложить яйцо.
Не вводить gender только ради яйцекладки.

---

# 18. УТИНОЕ ЯЙЦО — `duck_egg`

RU:
`Утиное яйцо`

EN:
`Duck Egg`

Дизайн:
- чуть крупнее vanilla chicken egg;
- светлый серо-зелёный/бледно-зеленоватый оттенок;
- мелкие мягкие серые speckles допустимы;
- отличается от goose egg размером/цветом.

Stack:
- 16.

Не throwable.
Не spawn egg.
Не edible raw.

Egg interval:
- adult duck;
- 9600–18000 ticks.

Пол не вводить:
- любой adult duck может отложить яйцо.

---

# 19. КОЗЬЕ МОЛОКО — `goat_milk_bucket`

RU:
`Ведро козьего молока`

EN:
`Goat Milk Bucket`

Item:
- bucket silhouette;
- молоко светлое кремовое;
- небольшое визуальное отличие от vanilla milk bucket;
- stack 1.

Получение:
- right click empty `minecraft:bucket` по adult domestic_goat;
- bucket -> goat_milk_bucket;
- стандартная milk interaction sound.

Baby goat:
- не доится.

Поведение при питье:
- аналогично vanilla milk:
  - remove status effects;
  - item превращается в empty bucket;
- НЕ восстанавливает hunger;
- НЕ даёт отдельный buff.

Не делать козье молоко «лучше» обычного.
Его расширенная ценность появится позже в Kitchen II.

---

# 20. МЯСО

Добавить 6 items:

- `raw_goose`
- `cooked_goose`
- `raw_duck`
- `cooked_duck`
- `raw_goat`
- `cooked_goat`

## 20.1 Дизайн

Следовать утверждённому референсу.

Raw:
- заметно розово-красное;
- жирные/светлые прожилки умеренно;
- форма каждого вида немного различается.

Cooked:
- тёмно-золотисто-коричневое;
- более тёмная корочка;
- не перекрашивать один и тот же sprite на все три вида.

Не делать слишком реалистичную мясную текстуру.
Minecraft-compatible pixel art.

## 20.2 Food values

`raw_goose`
- nutrition 2
- saturation modifier 0.30

`cooked_goose`
- nutrition 6
- saturation modifier 0.70

`raw_duck`
- nutrition 2
- saturation modifier 0.30

`cooked_duck`
- nutrition 6
- saturation modifier 0.65

`raw_goat`
- nutrition 3
- saturation modifier 0.30

`cooked_goat`
- nutrition 7
- saturation modifier 0.75

Никаких potion effects.

## 20.3 Cooking

Smelting + smoking:

raw_goose -> cooked_goose
raw_duck -> cooked_duck
raw_goat -> cooked_goat

Использовать vanilla-like meat XP/cooking duration.
Smoker быстрее обычной печи.

---

# 21. DROPS

## Goose
При обычном убийстве:
- 1–2 raw_goose;
- 0–2 `minecraft:feather`.

## Duck
- 1–2 raw_duck;
- 0–2 `minecraft:feather`.

## Domestic goat
- 1–3 raw_goat.

Looting:
- стандартно увеличивает quantity мяса/feathers;
- без чрезмерного scaling.

Если entity горит при смерти:
- использовать cooked equivalent в духе vanilla animals, если это легко реализуется loot condition data-driven.

НЕ добавлять:
- goose feather;
- duck feather;
- goat hide;
- fat;
- bones as special drop;
- carcass.

Расширенная разделка будет отдельной системой 1.1.4.

---

# 22. NATURAL SPAWN — GOOSE

Biomes:
- plains;
- meadow;
- river.

Группы:
- min 2;
- max 5.

Редкость:
- умеренно редкий хозяйственный wildlife spawn;
- ориентир spawn weight около 8 относительно обычных creature spawns.

Spawn surface:
- grass/dirt-like land;
- допустим берег рядом с водой;
- не требуется дорогой поиск воды на огромном радиусе.

Не спавнить:
- nether;
- end;
- desert;
- ocean;
- deep ocean.

---

# 23. NATURAL SPAWN — DUCK

Biomes:
- river;
- swamp;
- meadow;
- plains.

Группы:
- 2–4.

Weight ориентир:
- около 10.

Spawn predicate:
- допускается безопасная поверхность на берегу;
- допускается shallow/surface water, только если это можно реализовать простой custom spawn predicate без дорогого поиска;
- если водный spawn сильно усложняет код, spawn on valid ground в указанных биомах, после чего AI сам ищет воду.

Не делать MobCategory.WATER_CREATURE.

---

# 24. NATURAL SPAWN — DOMESTIC GOAT

Biomes:
- plains;
- meadow.

Группы:
- 2–3.

Weight:
- около 4.

Это более редкое животное.

Не спавнить:
- mountains как основную среду;
- snowy peaks;
- nether/end.

Домашняя коза сейчас может встречаться в мире как редкая хозяйственная популяция.
В будущей 1.2.x её можно будет связать с поселениями/NPC, но сейчас это НЕ реализовывать.

---

# 25. АНИМАЦИИ

Использовать текущую animation architecture проекта, если она уже есть.
Не подключать тяжёлую новую библиотеку только ради 3 мобов, если проект уже умеет entity animations.

## Goose
Нужно:
- idle;
- walk;
- swim;
- wing flap краткий;
- defensive neck extension;
- peck/nip attack;
- baby walk.

## Duck
- idle;
- walk;
- swim;
- light wing flap;
- baby walk/swim.

## Domestic goat
- idle;
- walk;
- eat/head-down;
- baby walk.

Не делать десятки декоративных анимаций.
Главное — читаемое базовое поведение.

---

# 26. ЗВУКИ

Звуки важны для индивидуальности 1.1.3.

Нужные sound events:

Goose:
- `entity.goose.ambient`
- `entity.goose.hiss`
- `entity.goose.hurt`
- `entity.goose.death`

Duck:
- `entity.duck.ambient`
- `entity.duck.hurt`
- `entity.duck.death`

Domestic goat:
- `entity.domestic_goat.ambient`
- `entity.domestic_goat.hurt`
- `entity.domestic_goat.death`

Характер:
- goose ambient — спокойное короткое гоготание;
- goose hiss — резкий защитный шипящий/предупреждающий звук;
- duck ambient — характерное короткое кряканье;
- domestic goat — мягкое домашнее блеяние, спокойнее/менее резкое, чем mountain goat.

ВАЖНО ПО АУДИО-ФАЙЛАМ:
- если в репозитории/пакете уже есть подходящие утверждённые `.ogg` — подключить их;
- если реальных `.ogg` нет, НЕ тратить много токенов на попытки синтезировать низкокачественный шум;
- зарегистрировать sound events и `sounds.json`;
- временно использовать наиболее близкие vanilla sound references как fallback внутри кода/ресурсов, чтобы функционал не был сломан;
- в финальном отчёте отдельной одной строкой указать, какие custom audio assets всё ещё нужно заменить, если настоящих `.ogg` не было.

Не скачивать случайные звуки из интернета.
Не использовать copyrighted packs.

---

# 27. КОРМЛЕНИЕ ВРУЧНУЮ — ОБЩЕЕ

Для новых и расширенных vanilla foods:

Если adult:
- если animal может войти в love mode:
  - стандартный breeding behavior;
- если wounded:
  - heal 2.

Если baby:
- ускорить взросление стандартным vanilla-like способом;
- heal 2.

Если adult находится на breeding cooldown и полностью здоров:
- custom additional feed НЕ расходовать без эффекта.

Particles:
- hearts для breeding;
- стандартные eating particles при кормлении.

Не создавать nutrition/hunger meter животного.

---

# 28. FEEDER AI — ТЕХНИЧЕСКАЯ ОГРАНИЧЕННОСТЬ

Нельзя создавать дорогой глобальный менеджер кормушек.

Правильно:
- mob-local goal;
- небольшой radius;
- поиск не каждый tick;
- прекращать поиск при cooldown;
- не pathfind к кормушке, если она пуста или food не подходит.

Для vanilla animals:
- goal добавляется один раз при entity join/load;
- использовать persistent marker/capability/data attachment только если необходимо предотвратить дублирование goal;
- не сериализовать лишние данные.

---

# 29. CREATIVE TABS

Использовать систему тематических вкладок из 1.1.2.
НЕ возвращать старую общую вкладку.

## Food & Kitchen
Добавить:
- goose_egg
- duck_egg
- goat_milk_bucket
- raw_goose
- cooked_goose
- raw_duck
- cooked_duck
- raw_goat
- cooked_goat

## Decor & Blocks
Добавить:
- feeder
- straw_nest

## Mobs / Spawn Eggs
Добавить:
- goose_spawn_egg
- duck_spawn_egg
- domestic_goat_spawn_egg

Никаких baby eggs.
Никаких gender eggs.

---

# 30. LOCALIZATION

Минимум:
- `ru_ru`
- `en_us`

RU:
- Гусь
- Утка
- Домашняя коза
- Гусиное яйцо
- Утиное яйцо
- Ведро козьего молока
- Сырая гусятина
- Жареная гусятина
- Сырая утка
- Жареная утка
- Сырая козлятина
- Жареная козлятина
- Кормушка
- Соломенное гнездо
- Яйцо призыва гуся
- Яйцо призыва утки
- Яйцо призыва домашней козы

EN:
- Goose
- Duck
- Domestic Goat
- Goose Egg
- Duck Egg
- Goat Milk Bucket
- Raw Goose
- Cooked Goose
- Raw Duck
- Cooked Duck
- Raw Goat
- Cooked Goat
- Feeder
- Straw Nest
- Goose Spawn Egg
- Duck Spawn Egg
- Domestic Goat Spawn Egg

---

# 31. ADVANCEMENT

ID:
`full_yard`

RU title:
`Полный двор`

RU description:
`Разведи гуся, утку и домашнюю козу`

EN title:
`Full Yard`

EN description:
`Breed a goose, a duck, and a domestic goat`

Parent:
`minecraft:husbandry/breed_an_animal`
или ближайший корректный husbandry parent в 1.21.1.

Критерии:
- breed goose;
- breed duck;
- breed domestic_goat.

Requirements:
- все 3 обязательны.

Frame:
- task.

Icon:
- goose_spawn_egg либо feeder.
Предпочтительно goose_spawn_egg.

Без XP reward.
Не hidden.

---

# 32. ENTITY DATA / SAVING

Для goose/duck:
сохранять:
- egg timer;
- feeder cooldown, если он должен переживать save/load;
- стандартный baby age уже сохраняется vanilla-style.

Не сохранять:
- gender;
- imaginary sex data.

Для goose defensive state:
- временное AI state не нужно сохранять между перезапусками;
- после load моб может вернуться к passive state.

Для nest/feeder:
- inventory обязательно сохраняется;
- block state count/fill синхронизируется после load.

---

# 33. PERFORMANCE

Запрещено:
- глобально сканировать всех животных каждый tick;
- глобально сканировать все кормушки;
- глобально сканировать все гнёзда;
- делать world-level manager, если не требуется;
- искать блоки в огромном радиусе;
- выполнять pathfinding search каждый tick.

Ориентиры:
- feeder search <= 10 blocks;
- nest search <= 12 blocks;
- search cadence ограничена;
- AI goal прекращается при невозможности.

---

# 34. ASSET QUALITY

Entity textures:
- выбрать разумное Minecraft-compatible разрешение;
- предпочтительно 64x64 или 128x128 для mob textures, если этого требует детализация моделей;
- не использовать размытые AI-картинки как texture atlas;
- textures должны быть вручную пригодны для UV mapping.

Item sprites:
- 32x32.

Block textures:
- 32x32, если текущий project style не требует другого.

Не делать:
- goose = chicken recolor;
- duck = chicken recolor;
- domestic goat = vanilla goat recolor.

Каждая модель отдельная.

Baby proportions отдельные через model transforms.

---

# 35. НЕ ИЗМЕНЯТЬ СИСТЕМЫ 1.1.0 / 1.1.2 БЕЗ НЕОБХОДИМОСТИ

1.1.0:
- crops;
- sickle;
- field hoe;
- watering can;
- fertilizer.

1.1.2:
- berries;
- apple tree;
- creative tabs.

Нужные изменения:
- использовать crop items как feed;
- добавить новые items в соответствующие creative tabs.

НЕ менять:
- growth rates;
- berry worldgen;
- apple canopy;
- apple regrowth;
- crop loot;
- tool mechanics.

---

# 36. НЕ ДОБАВЛЯТЬ В 1.1.3

- gender system;
- male/female models;
- male/female spawn eggs;
- baby spawn eggs;
- incubator;
- hatching from goose/duck eggs;
- throwable goose/duck eggs;
- automatic breeding feeder;
- thirst;
- water trough;
- cheese;
- butter;
- cream;
- fat;
- hide processing;
- carcasses;
- butchering knife behavior;
- tanning;
- wool rework;
- new chicken entity;
- new cow/sheep/pig/rabbit entities;
- geese breeds;
- duck breeds;
- goat breeds;
- animal genetics;
- sickness;
- seasons;
- barn structure;
- NPC farmers;
- trade system.

---

# 37. ОБНОВЛЕНИЕ ВЕРСИИ

Обновить mod version до:
`1.1.3`

Только в существующем source of truth проекта.

Не создавать параллельные version constants.

---

# 38. RESOURCE/DATA FILES

Создать необходимые:
- entity registrations;
- attributes;
- spawn placements;
- biome modifiers;
- models;
- textures;
- item models;
- blockstates;
- block models;
- loot tables;
- recipes;
- tags;
- lang;
- sounds.json;
- sound event registrations;
- advancement;
- feeder block entity;
- straw nest block entity.

Не генерировать runtime textures.

---

# 39. РЕЦЕПТЫ И DATA INTEGRATION

Обязательно:
- feeder recipe;
- straw_nest recipe;
- smelting raw meats;
- smoking raw meats.

Eggs и goat milk:
- не имеют crafting recipes.

Spawn eggs:
- не craftable.

Meat:
- normal recipe viewer visibility через стандартные recipes;
- не создавать отдельную JEI category.

---

# 40. BUILD

После реализации:

1. Не запускать Minecraft.
2. Не запускать PolyMC.
3. Выполнить одну финальную compile/build команду.
4. Если success — закончить.
5. Если fail из-за 1.1.3 — исправить конкретную ошибку и максимум один раз проверить повторно.
6. Если fail из-за старой независимой ошибки:
   - не чинить весь проект;
   - указать точный blocker.

---

# 41. GIT FINISH

Проверить:
- branch = main;
- только связанные изменения;
- нет случайного удаления чужих файлов.

Commit:
`feat: implement Slavic Myths 1.1.3 animal husbandry`

Если auth работает:
- push origin main.

НЕ:
- release branch;
- tag;
- GitHub Release;
- PolyMC copy.

---

# 42. КРИТЕРИИ ГОТОВНОСТИ

## Entities
[ ] goose entity существует.
[ ] duck entity существует.
[ ] domestic_goat entity существует.
[ ] только 3 entity types.
[ ] baby — состояние взрослого entity type.
[ ] нет gender system.
[ ] нет отдельных baby entities.

## Spawn eggs
[ ] goose_spawn_egg.
[ ] duck_spawn_egg.
[ ] domestic_goat_spawn_egg.
[ ] нет baby eggs.
[ ] нет male/female eggs.

## Goose
[ ] отдельная модель.
[ ] отдельный baby appearance.
[ ] плавание.
[ ] breeding.
[ ] egg timer.
[ ] protective hiss/chase.
[ ] короткий defensive attack.
[ ] не permanent hostile.

## Duck
[ ] отдельная модель.
[ ] отдельный baby appearance.
[ ] water preference.
[ ] swimming.
[ ] breeding.
[ ] egg timer.
[ ] passive.

## Domestic goat
[ ] отдельная модель.
[ ] baby appearance.
[ ] нет ram.
[ ] нет extreme jump.
[ ] breeding.
[ ] goat milking.

## Vanilla animal integration
[ ] cows accept rye/barley/oat grains in addition to vanilla wheat.
[ ] sheep accept rye/barley/oat grains.
[ ] pigs accept turnip in addition to vanilla foods.
[ ] chickens accept rye/barley/oat/flax seeds.
[ ] rabbits accept cabbage.
[ ] custom feed supports breeding.
[ ] babies grow faster from custom feed.
[ ] wounded animals heal up to 2.
[ ] vanilla foods remain fully functional.
[ ] vanilla models/drops unchanged.

## Feeder
[ ] capacity 16.
[ ] one food type at a time.
[ ] visual fill 0..3.
[ ] player insert/extract.
[ ] content persists.
[ ] new animals use it.
[ ] cow/sheep/pig/chicken/rabbit use it.
[ ] adults only eat when injured.
[ ] babies can eat for growth.
[ ] feeder never triggers breeding.
[ ] cooldown prevents constant consumption.

## Nest
[ ] capacity 3.
[ ] visual 0..3 eggs.
[ ] inventory persists.
[ ] player can insert/remove eggs.
[ ] goose searches nest.
[ ] duck searches nest.
[ ] fallback world drop works.
[ ] minecraft:egg can be manually stored.
[ ] no invasive chicken mixin required.

## Products
[ ] goose_egg.
[ ] duck_egg.
[ ] goat_milk_bucket.
[ ] raw/cooked goose.
[ ] raw/cooked duck.
[ ] raw/cooked goat.
[ ] food values match spec.
[ ] smelting/smoking recipes.

## Drops
[ ] goose meat + vanilla feather.
[ ] duck meat + vanilla feather.
[ ] goat meat.
[ ] no new hide/fat/carcass.
[ ] Looting works reasonably.

## Spawn/world
[ ] goose biome spawns.
[ ] duck biome spawns.
[ ] domestic goat rare spawn.
[ ] group sizes match scope.
[ ] no extreme spawn density.

## Sounds
[ ] sound events registered.
[ ] sounds.json valid.
[ ] goose hiss used by defense.
[ ] duck ambient used.
[ ] goat ambient appropriate.
[ ] if custom OGG absent, fallback documented briefly.

## Creative
[ ] food/products in Food & Kitchen.
[ ] feeder/nest in Decor & Blocks.
[ ] only 3 new spawn eggs in Mobs / Spawn Eggs.
[ ] no return to single dump tab.

## Workflow
[ ] no global project audit.
[ ] tokens used economically.
[ ] no Minecraft launch.
[ ] no PolyMC changes.
[ ] version 1.1.3.
[ ] main branch only.
[ ] no release/tag.
[ ] final build only as required.

---

# 43. MANUAL TEST CHECKLIST ДЛЯ ПОЛЬЗОВАТЕЛЯ

Codex НЕ выполняет эти игровые тесты.
В итоговом отчёте вывести короткий checklist:

1. Призвать goose/duck/domestic goat тремя spawn eggs.
2. Проверить, что отдельных baby/gender eggs нет.
3. Развести все 3 новых вида.
4. Проверить внешний вид детёнышей.
5. Проверить плавание гуся и утки.
6. Подойти к гусёнку и проверить защиту взрослого гуся.
7. Проверить яйцекладку гуся/утки и работу nest.
8. Проверить ручную загрузку/извлечение яиц из nest.
9. Подоить взрослую domestic goat.
10. Выпить goat milk и проверить снятие эффектов.
11. Проверить feeder с новым животным.
12. Проверить feeder с cow/sheep/pig/chicken/rabbit.
13. Проверить, что feeder не запускает auto breeding.
14. Проверить новые корма ванильных животных.
15. Проверить мясо и cooking.
16. Проверить natural spawn.
17. Проверить sounds.
18. Проверить creative tabs и advancement.

---

# 44. ФОРМАТ ИТОГОВОГО ОТЧЁТА CODEX

Коротко.

## Implemented
5–12 коротких bullets.

## Build
- command;
- success/fail;
- конкретная ошибка, только если есть.

## Audio
- custom OGG present / fallback used.
- если fallback — перечислить только отсутствующие custom sound families одной строкой.

## Git
- branch;
- commit hash;
- push status.

## Manual test checklist
Короткий список из раздела выше.

НЕ писать:
- длинное объяснение архитектуры;
- chain-of-thought;
- новые идеи;
- предложения следующих версий;
- общий аудит проекта.
