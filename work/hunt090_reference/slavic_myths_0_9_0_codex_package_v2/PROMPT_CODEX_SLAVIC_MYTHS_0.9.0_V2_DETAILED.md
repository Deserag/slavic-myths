# SLAVIC MYTHS — STRICT CODEX IMPLEMENTATION SPECIFICATION
## Version: 0.9.0 — «Охота на нечисть I»
## Minecraft 1.16.5 / Forge 36.2.42 / Java 8 / mod id `slavicmyths`
## Status: FINAL IMPLEMENTATION SPEC FOR THIS PATCH

---

# 0. ГЛАВНОЕ ПРАВИЛО: CODEX НЕ ЗАНИМАЕТСЯ ГЕЙМДИЗАЙНОМ

Все решения по 0.9.0 уже приняты в этом документе и в `references/`.

Codex отвечает только за:
- чтение минимально необходимого количества существующего кода;
- корректную реализацию;
- подключение ресурсов;
- сборку;
- README;
- подготовку JAR для ручной проверки.

Codex НЕ должен:
- предлагать новый дизайн вместо описанного;
- придумывать третьего мини-босса;
- добавлять Бабу-ягу раньше 0.9.3;
- добавлять Лихо или Тугарина раньше 0.9.2;
- возвращать «Живую и мёртвую воду» в 0.9.0;
- делать новую большую структуру/данж;
- раздувать hunting-систему до RPG;
- заменять подробно описанные предметы простыми recolor-иконками;
- запускать Minecraft-клиент;
- тратить токены на полный аудит репозитория.

Если текст этого документа конфликтует со старым README/roadmap, для **0.9.0** приоритет имеет этот документ.

---

# 1. ЖЁСТКОЕ ОГРАНИЧЕНИЕ ПО РАСХОДУ ТОКЕНОВ

Эта задача будет выполняться моделью ниже максимального уровня. Поэтому:

1. НЕ читай весь проект.
2. НЕ пересказывай содержимое проекта перед реализацией.
3. НЕ пиши длинный архитектурный план.
4. Найди только существующие шаблоны:
   - entity registration;
   - renderer/model;
   - item registration;
   - accessories/equipment, если уже существует;
   - loot tables;
   - saved data / encounter data;
   - commands;
   - sounds;
   - JEI;
   - advancements;
   - README;
   - build/deploy.
5. Если уже существует общий helper/registry — используй его.
6. Не создавай вторую параллельную систему.
7. Не перечитывай один и тот же большой файл несколько раз без причины.
8. После понимания existing pattern сразу редактируй файлы.
9. Не запускать `runClient`.
10. Разрешён только headless build/compile/resource validation.

---

# 2. ЧТО ИМЕННО ДОБАВЛЯЕТ 0.9.0

0.9.0 — первая половина финальной «охотничьей» ветки 0.9.x.

Добавить:

## Мини-боссы
1. `ovinnik` — Овинник.
2. `volkolak` — Волколак.

## Новые предметы
1. `ovinnaya_zola` — Овинная зола.
2. `iskra_ovinnika` — Искра Овинника.
3. `klyk_volkolaka` — Клык Волколака.
4. `nochnoy_kogot` — Ночной коготь.
5. `zolny_obereg` — Зольный оберег.
6. `volchiy_poyas` — Волчий пояс.
7. `obereg_ohotnika` — Оберег охотника.
8. `ohotnichiy_rog` — Охотничий рог.
9. `meshochek_trofeev` — Мешочек трофеев.
10. `pustoy_ritualny_sosud` — Пустой ритуальный сосуд.

## Системы
- базовая hunting/encounter-система;
- естественные редкие встречи;
- контролируемый призыв через Охотничий рог;
- тег трофеев;
- 9-слотовый Мешочек трофеев;
- интеграция существующего серебряного оружия против Волколака;
- JEI для новых рецептов;
- advancements;
- spawn eggs;
- debug/summon commands или документированные vanilla `/summon`;
- README.

НЕ добавлять:
- Бабу-ягу;
- Избушку;
- Метлу;
- Ступу;
- Лихо;
- Тугарина;
- Огненного змея;
- Живую/мёртвую воду;
- новую boss-location.

---

# 3. ВИЗУАЛЬНЫЕ REFERENCES — ОБЯЗАТЕЛЬНАЯ ЧАСТЬ ТЗ

Папка `references/` должна использоваться как визуальное ограничение.

Приоритет:
1. точное описание в этом документе;
2. isolated PNG конкретного предмета/моба;
3. full reference board;
4. существующий визуальный язык Slavic Myths.

Нельзя трактовать PNG как «примерно похоже».
Форма, силуэт, материалы и ключевые детали должны быть перенесены в Minecraft-совместимый вид.

## Список
- `REF_00_ITEMS_FULL.png`
- `REF_01_CHARACTERS_FULL.png`
- `REF_02_ROADMAP_FULL.png`
- `REF_ITEM_01_OVINNAYA_ZOLA.png`
- `REF_ITEM_02_ISKRA_OVINNIKA.png`
- `REF_ITEM_03_KLYK_VOLKOLAKA.png`
- `REF_ITEM_04_NOCHNOY_KOGOT.png`
- `REF_ITEM_05_ZOLNY_OBEREG.png`
- `REF_ITEM_06_VOLCHIY_POYAS.png`
- `REF_ITEM_07_OBEREG_OHOTNIKA.png`
- `REF_ITEM_08_OHOTNICHIY_ROG.png`
- `REF_ITEM_09_MESHOCHEK_TROFEEV.png`
- `REF_ITEM_10_PUSTOY_RITUALNY_SOSUD.png`
- `REF_MOB_01_OVINNIK.png`
- `REF_MOB_02_VOLKOLAK.png`
- `REF_MOB_03_OVINNIK_SECONDARY.png`
- `REF_MOB_04_VOLKOLAK_SECONDARY.png`

---

# 4. ОБЩИЕ ТРЕБОВАНИЯ К НОВЫМ ITEM-ASSETS

Пользователь отдельно требует высокий уровень детализации.

## 4.1 Разрешение
Для НОВЫХ предметов 0.9.0:

### Ингредиенты / трофеи
- финальная texture: **минимум 256×256**;
- не понижать до 32/64;
- иконка должна оставаться читаемой в обычном inventory scale.

### Сложные предметы
Для:
- Зольного оберега;
- Волчьего пояса;
- Оберега охотника;
- Охотничьего рога;
- Мешочка трофеев;
- Пустого ритуального сосуда;

использовать:
- **512×512 предпочтительно**;
- минимум 256×256 только если pipeline проекта объективно плохо работает с 512.

## 4.2 Нельзя
- взять vanilla item и просто перекрасить;
- делать однотонный предмет без материалов;
- терять ремни/шнуры/металл/кости/орнамент;
- копировать реалистичный концепт как фототекстуру;
- создавать placeholder и считать задачу закрытой.

## 4.3 Pixel-art
Несмотря на высокое разрешение:
- texture остаётся Minecraft-style pixel art;
- крупные pixel clusters;
- контролируемое количество цветов;
- silhouette читается на 16–32 px экранного размера;
- мелкие детали не превращаются в шум.

## 4.4 3D / held models
- Ингредиенты могут использовать `item/generated`.
- Охотничий рог ОБЯЗАТЕЛЬНО получает custom held model / layered JSON или существующий эквивалент, чтобы в руке это был рог, а не плоская картинка.
- Если accessory framework умеет render equipped accessories — Зольный оберег и Волчий пояс должны иметь visible equipped representation.
- Не создавать целую новую rendering framework только ради этого, если её нет.

---

# 5. ЦВЕТОВАЯ И МАТЕРИАЛЬНАЯ ЛОГИКА ПРЕДМЕТОВ

Чтобы предметы выглядели одной серией:

## Овинник
- угольно-чёрный;
- тёмно-коричневый;
- красно-оранжевые угли;
- немного жёлто-оранжевого hottest core;
- тёмно-красные тканевые шнуры.

## Волколак
- грязно-белая кость;
- серо-чёрный коготь;
- тёмно-коричневая кожа;
- тёмно-красные шнуры;
- матовое железо.

## Hunting gear
- дерево;
- кожа;
- кость;
- железо;
- красная славянская вышивка/знак;
- ограниченная зелёная растительность только у Оберега охотника.

Никакого яркого золота как основного материала.
Никаких неоновых магических цветов.

---

# 6. БАЗОВАЯ HUNT-СИСТЕМА

Создать минимальную общую систему, которую можно расширить в 0.9.1/0.9.2.

Не делать огромный framework.

Нужны:
- тип охоты: `OVINNIK`, `VOLKOLAK`;
- активный encounter;
- anchor/origin;
- target entity UUID;
- состояние `ACTIVE / CLEARED / FAILED`;
- cooldown игрока на использование рога;
- защита от создания большого количества mini-boss одновременно.

## 6.1 Один активный призыв
Через Охотничий рог один игрок не может одновременно иметь более **1** активного призванного hunt mini-boss.

Если активный жив:
- новый summon запрещён;
- игрок получает локализованное сообщение.

## 6.2 Encounter не должен бесконечно респавниться
Если mini-boss убит:
- encounter помечается cleared;
- новый появляется только после нового законного использования Horn либо отдельного natural spawn.

## 6.3 Leash
Призванный mini-boss:
- имеет encounter anchor;
- основной radius боя: ~40 блоков;
- не должен убегать на сотни блоков;
- если AI увёл его >56 блоков от anchor и игрок не находится рядом, entity возвращается к anchor через обычный pathing;
- не телепортировать сквозь мир без крайней необходимости.

## 6.4 Persistent
Mini-boss не должен despawn как обычный mob, пока encounter активен.

---

# 7. ОХОТНИЧИЙ РОГ — КЛЮЧЕВОЙ ПРЕДМЕТ СИСТЕМЫ

Registry:
`ohotnichiy_rog`

RU:
`Охотничий рог`

EN:
`Hunting Horn`

Reference:
`REF_ITEM_08_OHOTNICHIY_ROG.png`

## 7.1 Внешний вид — МАКСИМАЛЬНО ТОЧНО

Это большой изогнутый костяной рог.

Форма:
- широкое открытое устье;
- постепенно сужается;
- выраженный плавный изгиб;
- конец чуть приподнят;
- не выглядит как музыкальная труба из металла.

Материалы:
- основа — светлая кость;
- возле широкого края — тёмное железное кольцо;
- ближе к середине — кожаная обмотка;
- снизу висит короткая красная кисть;
- небольшая деревянная/костяная бирка с красным знаком охоты.

Palette target:
- bone highlight `#E8D7B0`
- bone mid `#CDB88E`
- bone shadow `#8C7759`
- iron `#36383B`
- dark iron `#202225`
- leather `#69442F`
- red textile `#8E2022`
- bright embroidery `#B3352F`

Texture:
- preferred 512×512.
- custom held model required.

## 7.2 Получение
Крафт из vanilla материалов.
НЕ требует трофея mini-boss, иначе игрок не сможет начать систему.

Shaped recipe:
`B L .`
`. B I`
`S . B`

Where:
- `B` = Bone
- `L` = Leather
- `I` = Iron Nugget
- `S` = String

Если существующий recipe format проекта требует другое расположение, сохранить именно эти ингредиенты.

## 7.3 Переключение цели
`Shift + ПКМ`:
переключает:
- `Овинник`
- `Волколак`

Выбор хранится в ItemStack NBT.

Tooltip показывает:
`Цель охоты: Овинник`
или
`Цель охоты: Волколак`

## 7.4 Обычный ПКМ
Пытается начать охоту на выбранную цель.

### Общие условия
- Overworld;
- ночь для обоих;
- игрок не имеет активного призванного hunt encounter;
- cooldown рога = `6000 ticks` = 5 минут после УСПЕШНОГО призыва;
- не тратить cooldown при неудачной проверке условий.

### Овинник
Дополнительно:
- в радиусе 18 блоков должно быть хотя бы одно:
  - hay_block,
  - campfire,
  - smoker;
- не в воде;
- safe spawn candidate в 12–24 блоках от игрока.

### Волколак
Дополнительно:
- biome category / biome должен быть forest/taiga-compatible;
- не в воде;
- safe spawn candidate в 12–24 блоках.

## 7.5 Поиск safe position
НЕ повторять ошибку генерации кургана с одной общей ошибкой.

Поиск:
- до 32 кандидатов;
- радиус 12–24;
- валидный solid ground;
- минимум 3 блока вертикального пространства;
- не вода/лава;
- не внутри блока;
- не внутри чужой структуры collision box, если framework позволяет проверить.

При провале:
вывести КОНКРЕТНУЮ причину:
- wrong biome;
- daytime;
- no required Ovinik environment;
- no safe position;
- active encounter;
- cooldown.

## 7.6 Звук
При успешном использовании:
- использовать существующий raid horn / horn-like vanilla event, если доступен в 1.16.5;
- pitch чуть ниже стандартного;
- слышимость 32–48 блоков.

При failed summon:
- короткий dull/bone click, без громкого рога.

---

# 8. ОВИННИК — МИНИ-БОСС №1

Registry:
`slavicmyths:ovinnik`

RU:
`Овинник`

EN:
`Ovinnik`

Spawn egg:
`ovinnik_spawn_egg`

Primary egg color:
`#2C2722`

Secondary:
`#E34A18`

References:
- `REF_MOB_01_OVINNIK.png`
- `REF_MOB_03_OVINNIK_SECONDARY.png`

## 8.1 Визуальная идея
Овинник — тяжёлое человекоподобное существо из:
- обугленного дерева;
- соломы;
- золы;
- тлеющих внутренних углей.

Это НЕ голем и НЕ Blaze.

## 8.2 Рост / hitbox
Visual height:
`2.5–2.7 блока`.

Target hitbox:
- width: около `1.15`
- height: около `2.45`

Не делать хитбокс шириной всей визуальной соломы.

## 8.3 Пропорции
- голова относительно маленькая;
- плечи очень широкие;
- руки длинные, тяжёлые;
- кисти крупные;
- ноги короче и массивнее;
- корпус немного наклонён вперёд.

## 8.4 Детали модели
Обязательные отдельные model parts:
- charred core torso;
- left/right shoulder straw bundles;
- hanging straw back;
- left/right heavy forearms;
- claw-like fingers;
- ember cracks on chest;
- ember cracks on arms;
- blocky head;
- deep eye sockets;
- 2 glowing ember eyes.

Количество cubes держать разумным:
ориентир 25–45 model cubes.
Не делать 100+ мелких кубов.

## 8.5 Texture
Entity texture:
- минимум 256×256;
- emissive layer для углей/глаз, если текущий renderer проекта позволяет без новой огромной системы.

Palette:
- charred wood `#241C18`
- warm charcoal `#34251D`
- straw dark `#5A422A`
- straw mid `#8E6B39`
- ember red `#B92716`
- ember orange `#F15317`
- ember yellow `#FFAD28`
- ash gray `#625D56`

## 8.6 Базовые характеристики
- MAX_HEALTH = `175`
- ATTACK_DAMAGE = `10`
- MOVEMENT_SPEED = `0.245`
- ARMOR = `10`
- ARMOR_TOUGHNESS = `2`
- KNOCKBACK_RESISTANCE = `0.55`
- FOLLOW_RANGE = `32`

## 8.7 AI memory
После получения target:
- помнить последнюю известную позицию `200 ticks`;
- не сбрасывать игрока после краткого обхода колонны/дерева;
- каждые 10–20 ticks при необходимости обновлять path.

## 8.8 AI priority
Приоритет:
1. alive/valid player target;
2. ability currently casting;
3. melee if <= 2.7 blocks;
4. Furnace Slam opportunity;
5. Ash Burst opportunity;
6. anti-kite ember throw if player far;
7. chase;
8. return to encounter anchor;
9. idle.

Abilities не должны одновременно перезаписывать navigation.

## 8.9 Базовая атака — ТЯЖЁЛЫЙ ВЗМАХ
- range около 2.7;
- damage = базовый;
- attack interval target `22 ticks`;
- animation windup ~8 ticks;
- hit occurs после windup;
- recovery ~8 ticks.

Визуально:
одно плечо отходит назад, корпус поворачивается, тяжёлая рука делает дугу.

Не использовать мгновенный generic zombie slap.

## 8.10 Способность 1 — УДАР ЖАРОВНИ
Heavy ground slam.

Условия:
- player в 2–5 блоках;
- cooldown `120 ticks`.

Sequence:
1. Овинник поднимает обе руки — `18 ticks` телеграфа.
2. В момент удара:
   - radius `4.0 blocks`;
   - base damage `8`;
   - knockback наружу;
   - вертикальный knockback небольшой.
3. Земля НЕ разрушается.
4. Использовать ash/ember particles.
5. После удара recovery `22 ticks`.

Игрок должен успеть:
- отбежать;
- перепрыгнуть край;
- заблокировать часть урона.

## 8.11 Способность 2 — ЗОЛЬНЫЙ ВЫБРОС
Cone/short-range ash attack.

Cooldown:
`140 ticks`.

Range:
`6 blocks`.

Cone:
около 70 градусов.

Effects:
- damage `4`;
- Weakness I `80 ticks`;
- краткий экранный эффект делается только через vanilla potion visuals; не создавать новый shader.

Telegraph:
- грудные трещины ярче;
- Овинник отводит голову/корпус;
- звук втягивания воздуха/треска 12 ticks.

## 8.12 Способность 3 — ТЛЕЮЩИЙ СЛЕД
Овинник ускоряется к удаляющемуся игроку.

Условия:
- distance 7–14;
- cooldown `160 ticks`.

Sequence:
- 10 tick windup;
- speed multiplier около `1.55`;
- duration максимум `28 ticks`;
- оставляет временные ember/ash particles или lightweight damaging area;
- НЕ ставить реальные fire blocks;
- НЕ grief terrain.

Temporary trail:
- существует максимум 2 sec;
- при наступании наносит `2` fire-type damage максимум раз в 20 ticks на игрока.

## 8.13 Anti-cheese — УГОЛЬНЫЙ ОСКОЛОК
Если игрок:
- >10 blocks away;
- или стоит на pillar/ledge, куда path не находится >60 ticks,

Овинник может бросить ember clump:
- cooldown `100 ticks`;
- projectile speed умеренная;
- damage `5`;
- маленький splash radius <=1.5;
- НЕ ломает блоки.

Это не основная атака, а защита от безопасного cheesing.

## 8.14 Фаза ярости
При HP <= `50%`:
- movement speed +8%;
- base attack interval уменьшается примерно с 22 до 18 ticks;
- ability cooldowns не сокращать более чем на 15%;
- ember emissive интенсивнее;
- один короткий roar/crackle cue.

Не восстанавливать HP.

## 8.15 ВОДА И ДОЖДЬ — КОНТРМЕХАНИКА
Овинник должен реально реагировать на воду.

Если:
- находится непосредственно в water block;
то:
- получает `2 damage` каждые 20 ticks;
- огненные частицы резко уменьшаются;
- Tleющий след недоступен.

Если под открытым небом во время дождя:
- ability cooldown progress можно замедлить примерно на 20%;
- fire-type damage Овинника -15%;
- НЕ наносить ему огромный пассивный урон от одного дождя.

Fire:
- immune к обычному огню;
- immune к lava/fire tick damage.

## 8.16 Поведение при щите игрока
Shield должен помогать:
- обычный взмах блокируется нормально;
- Slam уменьшает урон щитом, но knockback остаётся частично;
- Ash Burst не должен полностью исчезать от щита, потому что это area/cone.

## 8.17 Natural encounter
Редкий естественный encounter ночью.

Spawn placement:
- Overworld;
- ночь;
- solid ground;
- не Peaceful;
- рядом (<=16 blocks) есть hay_block, campfire или smoker;
- максимум 1 Ovinnik within 64 blocks;
- очень низкая частота.

Не делать обычный массовый biome mob.

## 8.18 Sounds
Если custom OGG нет — НЕ тратить токены на генерацию audio.

Использовать существующие SoundEvents после проверки актуальных имён 1.16.5.

Желаемый mapping:
- ambient: Blaze-like crackle на пониженном pitch;
- hurt: heavy wood/Blaze hurt;
- death: heavy wooden collapse + extinguish feel;
- step: wood/roots heavy step;
- slam windup: low crackle;
- slam impact: muted explosion/anvil-like thump, БЕЗ terrain explosion;
- ash burst: extinguish/fire hiss;
- rage: low Ravager-like roar + fire crackle if available.

Не использовать один звук для всех действий.

## 8.19 Loot
Только loot table.

Guaranteed:
- `ovinnaya_zola`: 8–14;
- `iskra_ovinnika`: 1.

Chance:
- extra `iskra_ovinnika`: 20%;
- `pustoy_ritualny_sosud`: 8%.

XP:
- ориентир 45–60 XP total.

---

# 9. ВОЛКОЛАК — МИНИ-БОСС №2

Registry:
`slavicmyths:volkolak`

RU:
`Волколак`

EN:
`Volkolak`

Spawn egg:
`volkolak_spawn_egg`

Egg primary:
`#3D3A38`

Egg secondary:
`#9A1F27`

References:
- `REF_MOB_02_VOLKOLAK.png`
- `REF_MOB_04_VOLKOLAK_SECONDARY.png`

## 9.1 Визуальная идея
Волколак — проклятый человек, полностью ушедший в звериный волчий облик.

Он:
- не обычный wolf scaled ×2;
- не голливудский гладкий оборотень;
- не человек с волчьей головой.

Silhouette:
- огромная верхняя часть тела;
- вытянутая волчья морда;
- длинные руки с когтями;
- согнутые ноги;
- густая неровная шерсть;
- куски ремней/старой одежды.

## 9.2 Рост / hitbox
Visual:
`2.25–2.4 блока`.

Hitbox target:
- width `1.05`
- height `2.25`.

## 9.3 Модель
Обязательные model parts:
- muzzle;
- ears;
- head;
- chest;
- back fur ridge;
- upper/lower arms;
- claws;
- thigh/shin feet;
- torn waist cloth/belt;
- shoulder fur clumps.

Не использовать WolfModel как основу без глубокой переработки.

Cubes:
ориентир 30–50.

## 9.4 Texture
Minimum:
256×256.

Palette:
- darkest fur `#262426`
- dark gray `#343336`
- mid gray `#575458`
- light fur `#858080`
- dirty bone teeth `#D8C7A5`
- gums/dark red `#692226`
- eyes `#C71E20`
- leather `#5B3A2B`
- red cord `#8A2027`

Допускается небольшой emissive pixel layer только для глаз.

## 9.5 Stats
- MAX_HEALTH = `165`
- ATTACK_DAMAGE = `9`
- MOVEMENT_SPEED = `0.335`
- ARMOR = `7`
- KNOCKBACK_RESISTANCE = `0.25`
- FOLLOW_RANGE = `36`

## 9.6 Target memory
- хранит последнюю позицию игрока 240 ticks;
- хороший path recalc;
- не перестаёт преследовать после одного дерева.

## 9.7 AI personality
Главная идея:
Волколак не стоит перед игроком.
Он:
- заходит сбоку;
- меняет угол;
- пытается сокращать дистанцию;
- прыгает;
- отступает на 2–3 блока после тяжёлой серии;
- снова атакует.

## 9.8 Базовый коготь
- range 2.4;
- interval target 16–18 ticks;
- animation от плеча;
- не generic zombie swing.

## 9.9 Способность 1 — ПРЫЖОК ОХОТНИКА
Cooldown:
`90 ticks`.

Use distance:
5–10 blocks.

Sequence:
1. приседает/отводит плечи `10 ticks`;
2. leap;
3. при попадании:
   - damage `8`;
   - небольшой knockback;
4. если промах:
   - recovery `14 ticks`.

Leap не должен:
- проходить через стены;
- менять позицию телепортом;
- гарантированно попадать.

## 9.10 Способность 2 — ТРОЙНОЙ РАЗРЫВ
Combo.

Cooldown:
`110 ticks`.

Hits:
1. левая когтевая атака;
2. правая когтевая;
3. тяжёлый перекрёстный удар.

Spacing:
~6 ticks между ударами.

Damage:
- 4
- 4
- 7

Третий удар:
- заметный knockback.

Если игрок ушёл из range после первого удара:
оставшиеся удары могут промахнуться.
Нельзя «приклеивать» игрока к entity.

## 9.11 Способность 3 — ВОЙ ОХОТЫ
Cooldown:
`200 ticks`.

Telegraph:
- останавливается;
- поднимает голову;
- howl `20 ticks`.

Radius:
`12 blocks`.

Player effect:
- Weakness I `80 ticks`;
- внутренний prey mark на `120 ticks`.

Пока prey mark:
- Волколак получает +10% movement speed при движении именно к этой цели;
- НЕ даёт +50% damage;
- НЕ раскрывает игрока через всю карту.

## 9.12 БОКОВОЙ РЫВОК
Не отдельная большая ability.
AI maneuver.

Когда игрок целится/смотрит прямо на Волколака и дистанция 3–7:
- с небольшим шансом выбирает левую/правую точку;
- quick strafe 2–3 блока;
- cooldown ~50 ticks;
- затем пытается атаковать.

## 9.13 Фаза «ЗАПАХ КРОВИ»
При HP <= 50%:
- speed +7%;
- pounce cooldown уменьшается примерно на 15%;
- basic interval может стать на 2 ticks короче;
- визуально больше открытый рот/агрессивная idle animation;
- один howl cue.

## 9.14 ПОЛНОЛУНИЕ
Если бой происходит ночью при full moon:
- Волколак получает ещё +5% movement speed;
- damage НЕ увеличивать;
- HP НЕ увеличивать.

Это flavor-bonus, не отдельный difficulty wall.

## 9.15 СЕРЕБРО
Использовать существующее серебряное оружие мода.

Создать/использовать item tag:
`slavicmyths:silver_weapons`

Если damage source принадлежит атаке предметом из этого tag:
- финальный damage по Волколаку × `1.25`.

Не проверять конкретные item IDs вручную в 10 местах.
Один tag + один helper.

Не добавлять новое серебряное оружие в 0.9.0.

## 9.16 Natural encounter
- Overworld;
- ночь;
- forest/taiga-compatible biome;
- solid ground;
- не вода;
- max 1 Volkolak within 72 blocks;
- очень низкая вероятность.

Не превращать леса в бесконечный спавн мини-боссов.

## 9.17 Sounds
Fallback mapping после проверки реальных SoundEvents:
- ambient: wolf growl / pant, lower pitch;
- hurt: wolf hurt + low beast cue;
- death: wolf death + heavy body impact;
- pounce: short growl;
- combo: claw swishes;
- howl: Ravager roar или closest suitable low howl-like event;
- silver hit: optional subtle metal/high hiss only if легко.

## 9.18 Loot
Guaranteed:
- `klyk_volkolaka`: 2–4.

Chance:
- `nochnoy_kogot`: 35%;
- extra fang: 25%;
- `pustoy_ritualny_sosud`: 8%.

XP:
45–60.

---

# 10. ПРЕДМЕТ 1 — ОВИННАЯ ЗОЛА

ID:
`ovinnaya_zola`

Reference:
`REF_ITEM_01_OVINNAYA_ZOLA.png`

Texture:
256×256 minimum.

Stack:
64.

## Design
Это НЕ просто серая кучка.

В inventory icon должны читаться:
- небольшая куча тёмной золы;
- 3–5 крупных угольных кусочков;
- внутри отдельных кусочков красные тлеющие точки;
- сбоку/сверху маленький красный шнур/кусочек ткани как визуальная связь с Овинником.

Silhouette:
низкая треугольная куча.

Palette:
- ash `#403B37`
- ash light `#6A625A`
- coal `#211E1C`
- ember `#D43B19`
- hot ember `#F47A1F`
- cord `#7D1B1C`

## Получение
Только основной источник:
Ovinnik loot table.

8–14 guaranteed.

## Функция
Crafting ingredient:
- Зольный оберег.
Также tag:
`slavicmyths:hunt_trophies`.

Никаких прямых potion-effect по ПКМ.

## Tooltip
RU:
`Тёплая зола из логова Овинника.`

EN:
`Warm ash left by an Ovinnik.`

---

# 11. ПРЕДМЕТ 2 — ИСКРА ОВИННИКА

ID:
`iskra_ovinnika`

Reference:
`REF_ITEM_02_ISKRA_OVINNIKA.png`

Texture:
256×256 minimum.

Stack:
16.

Rarity:
UNCOMMON or project equivalent.

## Design
Крупный обломок угольно-чёрного материала с внутренним жаром.

Форма:
- асимметричный кристаллоподобный уголь;
- НЕ настоящий crystal;
- сколотые грубые края;
- две-три крупные трещины;
- центр горит оранжево-жёлтым.

Palette:
- outer `#211B1B`
- warm black `#302021`
- red `#B92616`
- orange `#F04B13`
- hot orange `#FF7A16`
- hot center `#FFC23A`

Optional:
inventory glint НЕ использовать автоматически.
Предмет и так яркий.

## Получение
1 guaranteed per Ovinnik.
20% chance second.

## Uses
- основной энергетический компонент Зольного оберега;
- future-use ingredient later allowed;
- tag `hunt_trophies`.

## Tooltip
RU:
`Даже вне огня она продолжает тлеть.`

---

# 12. ПРЕДМЕТ 3 — КЛЫК ВОЛКОЛАКА

ID:
`klyk_volkolaka`

Reference:
`REF_ITEM_03_KLYK_VOLKOLAKA.png`

Texture:
256×256.

Stack:
32.

## Design
Большой изогнутый клык.

Не делать маленьким волчьим зубиком.

Icon:
- tooth занимает 65–75% высоты texture silhouette;
- толстое основание;
- постепенно сужается;
- конец слегка изогнут;
- возле корня 2–3 кожаных/тёмных кольца;
- короткая красная петля.

Palette:
- ivory `#E8DAB9`
- bone `#C8B58E`
- shadow `#8A785B`
- root `#47372D`
- leather `#5B3828`
- red cord `#842024`

## Получение
Volkolak guaranteed 2–4.

## Uses
- Волчий пояс;
- hunt trophy;
- future Baba Yaga exchange later, но НЕ реализовывать NPC сейчас.

Tooltip:
`Клык зверя, который когда-то был человеком.`

---

# 13. ПРЕДМЕТ 4 — НОЧНОЙ КОГОТЬ

ID:
`nochnoy_kogot`

Reference:
`REF_ITEM_04_NOCHNOY_KOGOT.png`

Texture:
256×256.

Stack:
16.

Rarity:
UNCOMMON.

## Design
Длинный почти чёрный коготь.

Shape:
- strongly curved;
- thicker base;
- sharp hook tip;
- 3 dark red wrappings near base;
- tiny bone/wood tag and short cord.

Colors:
- black `#19191D`
- charcoal `#2B2A31`
- highlight `#48444D`
- deep red `#6E1920`
- cord red `#91242A`
- tag `#C0A473`

НЕ делать коготь ярко-фиолетовым.

## Получение
35% chance from Volkolak.

## Uses
- ключевой редкий ingredient Оберега охотника;
- hunt trophy.

Tooltip:
`Коготь холоднее ночного воздуха.`

---

# 14. ПРЕДМЕТ 5 — ЗОЛЬНЫЙ ОБЕРЕГ

ID:
`zolny_obereg`

Reference:
`REF_ITEM_05_ZOLNY_OBEREG.png`

Texture:
**512×512 preferred**, minimum 256.

Stack:
1.

Rarity:
RARE.

## 14.1 Design
Круглый тяжёлый амулет.

Layers visually:
1. внешний тёмный железный ring;
2. 6–8 заклёпок;
3. внутренний почти чёрный каменный/угольный диск;
4. красно-оранжевый славянский геометрический знак;
5. вокруг знака тонкие ember cracks;
6. сверху короткая петля;
7. сбоку/снизу красная кисть из 2–3 шнуров.

Не делать идеально круглым ювелирным медальоном.
Он должен быть грубым охотничьим/ритуальным предметом.

Colors:
- iron `#36393C`
- iron edge `#54575A`
- disk `#211E1D`
- rune dark red `#A32419`
- rune orange `#F04E17`
- hot pixels `#FF9327`
- tassel `#8D2023`

## 14.2 Recipe
Shaped:
`I S I`
`A K A`
`. A .`

Where:
- I = iron_nugget
- S = string
- A = ovinnaya_zola
- K = iskra_ovinnika

## 14.3 Equip rule
Preferred:
existing amulet/accessory slot if already implemented.

If no accessory framework exists:
- DO NOT build a huge new Curios clone;
- temporary 0.9.0 fallback = effect only while in OFFHAND.

Never give benefit merely because item sits anywhere in inventory.

## 14.4 Effect
While properly equipped:
- fire/lava/fire-based incoming damage × `0.80`;
- remaining burn duration when newly ignited reduced approximately 40%;
- does NOT grant vanilla Fire Resistance;
- does NOT make lava safe;
- multiple copies do NOT stack.

## 14.5 Feedback
When protection actually reduces fire damage:
- tiny ash particle;
- quiet crackle;
- rate limit feedback to avoid sound spam.

## Tooltip RU
`Сдерживает жар, но не делает владельца неуязвимым к огню.`

---

# 15. ПРЕДМЕТ 6 — ВОЛЧИЙ ПОЯС

ID:
`volchiy_poyas`

Reference:
`REF_ITEM_06_VOLCHIY_POYAS.png`

Texture:
512×512 preferred.

Stack:
1.

Rarity:
RARE.

## 15.1 Visual
Широкий тяжёлый кожаный пояс.

Mandatory visual elements:
- основной brown leather band;
- большая прямоугольная матовая металлическая buckle;
- меховой серый верхний край;
- два крупных клыка подвешены рядом;
- тёмно-красная тканевая бирка;
- маленькая костяная/железная подвеска;
- несколько дополнительных кожаных ремешков.

Silhouette must be clearly a BELT, not necklace.

Palette:
- leather dark `#4D3025`
- leather mid `#704734`
- fur `#5A5552`
- fur light `#817B76`
- buckle `#4B4E50`
- fang `#D2C29F`
- red tag `#7D1C21`

## 15.2 Recipe
Shaped:
`L L L`
`F I F`
`. . .`

L = leather
F = klyk_volkolaka
I = iron_ingot

## 15.3 Equip
Use existing belt slot if present.

Fallback if no belt system:
- item works only in a specifically designated offhand fallback mode;
- BUT Zolny Obereg and Volchiy Poyas cannot both gain benefit from one offhand simultaneously.
- Do not invent inventory-passive stacking.

## 15.4 Effect
At night:
- movement speed +8%.

At full moon:
- additional +3% movement speed;
total max +11%.

During day:
- no bonus.

No attack damage bonus.
No permanent Speed potion spam every tick if attribute modifier can be used.

Use stable UUID modifier pattern.

## Tooltip
`Ночью ноги сами находят быстрый путь.`

---

# 16. ПРЕДМЕТ 7 — ОБЕРЕГ ОХОТНИКА

ID:
`obereg_ohotnika`

Reference:
`REF_ITEM_07_OBEREG_OHOTNIKA.png`

Texture:
512×512 preferred.

Stack:
1.

Rarity:
RARE.

## 16.1 Visual
Вертикальная деревянная ритуальная пластина.

Mandatory:
- thick dark wooden board;
- irregular rough edges;
- red carved hunting sign in center;
- leather loop/strap from top;
- red tassel;
- small patches of moss/forest greenery at edges;
- small bone/iron fitting.

Do not make it a rectangular modern badge.

Colors:
- wood dark `#4A3424`
- wood mid `#765039`
- groove red `#A52320`
- tassel `#8C1E25`
- moss `#4D6437`
- bone `#CBB993`

## 16.2 Recipe
Shaped:
`S B S`
`W C W`
`. R .`

S = string
B = bone
W = oak_planks or project-equivalent wood plank tag
C = nochnoy_kogot
R = red_dye

## 16.3 Function
Active detector, NOT permanent minimap.

Right click:
- cooldown `400 ticks` (20 sec);
- search radius `128 blocks`;
- searches active entities tagged `slavicmyths:hunt_targets`:
  - Ovinnik
  - Volkolak
  - future expansion via tag.

If target found:
- report:
  - target type;
  - approximate distance rounded to nearest 10;
  - cardinal/ordinal direction from player.
Example:
`След Волколака: северо-восток, около 70 блоков.`

Also:
- target receives Glowing for `100 ticks` ONLY if target is within 48 blocks and line-of-sight or same encounter.
- do not reveal mobs through entire 128-block world with permanent glow.

If no target:
`Следов крупной нечисти поблизости нет.`

Sound:
quiet wooden/bone click + subtle chime.

## Tooltip
`Помогает отыскать уже потревоженную крупную нечисть.`

---

# 17. ПРЕДМЕТ 8 — ОХОТНИЧИЙ РОГ

Полная механика уже определена в разделе 7.

Дополнительные item rules:
- Stack 1.
- No durability.
- Cooldown only on successful summon.
- NBT stores selected target.
- Use animation = bow/drink style only if visually sensible; otherwise custom short use.
- Must not be consumable.
- Must not summon bosses outside allowed environment.

Tooltip:
`Shift + ПКМ — выбрать цель.`
`ПКМ — начать охоту при подходящих условиях.`

---

# 18. ПРЕДМЕТ 9 — МЕШОЧЕК ТРОФЕЕВ

ID:
`meshochek_trofeev`

Reference:
`REF_ITEM_09_MESHOCHEK_TROFEEV.png`

Texture:
512×512 preferred.

Stack:
1.

## 18.1 Visual
Небольшой тяжёлый охотничий мешок.

Mandatory:
- dark brown leather sack;
- thick top rim;
- red braided closing cord;
- side stitches;
- 1–2 small fang/bone trophies attached;
- one small metal medallion;
- cloth patch;
- asymmetrical silhouette.

Do not make vanilla Bundle recolor.

## 18.2 Recipe
Shaped:
`L S L`
`L . L`
`L I L`

L = leather
S = string
I = iron_nugget

## 18.3 Container
Right click opens dedicated small GUI.

Exactly:
- 9 slots;
- one row 9 or 3×3 based on easiest project UI convention;
- must save contents in item NBT;
- retains contents if item dropped/picked up;
- contents copied safely in item cloning.

## 18.4 Allowed items
Only items in item tag:
`slavicmyths:hunt_trophies`.

Initial tag includes:
- ovinnaya_zola
- iskra_ovinnika
- klyk_volkolaka
- nochnoy_kogot
- pustoy_ritualny_sosud

Optionally existing compatible monster trophies may be added ONLY if obviously appropriate.

## 18.5 Rules
- cannot insert another `meshochek_trofeev`;
- cannot insert shulker/large container items if that creates nesting issues;
- shift-click must obey tag;
- invalid items remain in player inventory;
- no item duplication on close/death.

## 18.6 Sound
open:
soft leather equip/open cue.

close:
soft leather close cue.

Tooltip:
`Хранит только трофеи охоты.`
`Вместимость: 9 ячеек.`

---

# 19. ПРЕДМЕТ 10 — ПУСТОЙ РИТУАЛЬНЫЙ СОСУД

ID:
`pustoy_ritualny_sosud`

Reference:
`REF_ITEM_10_PUSTOY_RITUALNY_SOSUD.png`

Texture:
512×512 preferred.

Stack:
16.

Rarity:
UNCOMMON.

## 19.1 Visual
Тяжёлый маленький чёрный ритуальный сосуд.

NOT a vanilla glass bottle recolor.

Form:
- round squat body;
- short narrow neck;
- heavy dark stopper;
- red thread/wax binding around neck;
- central red geometric sign;
- small bone parchment/tag tied to side.

Material impression:
blackened ceramic / volcanic dark stone.

Palette:
- vessel `#252426`
- highlight `#3A393B`
- shadow `#171719`
- red sign `#A72421`
- red cord `#7E1C20`
- stopper `#343334`
- parchment `#BEA47B`

## 19.2 Получение
Not craftable in 0.9.0.

Drop:
- Ovinnik 8%;
- Volkolak 8%.

## 19.3 Function
Intentional future-use item.

In 0.9.0:
- no right-click effect;
- no recipe output;
- no hidden unfinished function.

Tooltip:
`Сосуд пуст. Его назначение пока неизвестно.`

This is deliberate groundwork for later 0.9.x / Baba Yaga systems.

---

# 20. TAGS

Create/extend:

## `slavicmyths:hunt_trophies`
Initial:
- ovinnaya_zola
- iskra_ovinnika
- klyk_volkolaka
- nochnoy_kogot
- pustoy_ritualny_sosud

## `slavicmyths:hunt_targets`
Entity tag/equivalent:
- ovinnik
- volkolak

## `slavicmyths:silver_weapons`
Populate using already existing silver weapon IDs.

Do not manually hardcode each silver weapon in Volkolak class.

---

# 21. ADVANCEMENTS

Add concise 0.9.0 advancements.

## `hunt_begins`
RU:
`Охота началась`

Trigger:
first successful Hunting Horn summon.

## `extinguish_the_barn`
RU:
`Загасить овин`

Trigger:
kill Ovinnik.

## `silver_remedy`
RU:
`Серебряное средство`

Trigger:
kill Volkolak, preferably final hit with a tagged silver weapon if trigger system allows without huge complexity.
If too complex, simply kill Volkolak.

## `trophy_keeper`
RU:
`Добыча охотника`

Trigger:
put first valid trophy into Trophy Pouch.

Do not add 20 advancements.

---

# 22. JEI

All craftable recipes must appear:
- Hunting Horn
- Ash Charm
- Wolf Belt
- Hunter Charm
- Trophy Pouch

Do not create a whole new JEI category unless needed.
Normal crafting recipe display is enough.

Loot-only:
- Ash
- Spark
- Fang
- Night Claw
- Ritual Vessel
must NOT get fake crafting recipes.

---

# 23. LOCALIZATION

Add RU and EN for:
- entities;
- spawn eggs;
- items;
- tooltips;
- Horn target switching;
- hunt failures;
- advancement names/descriptions;
- pouch UI title.

Do not leave hardcoded Russian strings inside entity logic if project uses lang keys.

---

# 24. DEBUG / MANUAL TEST COMMANDS

At minimum document working vanilla:
- `/summon slavicmyths:ovinnik`
- `/summon slavicmyths:volkolak`
- `/give @s slavicmyths:ohotnichiy_rog`
- `/give @s slavicmyths:zolny_obereg`
- `/give @s slavicmyths:volchiy_poyas`
- `/give @s slavicmyths:obereg_ohotnika`
- `/give @s slavicmyths:meshochek_trofeev`

If project already has `/slavicmyths dev`:
add lightweight:
- `/slavicmyths dev hunt summon ovinnik`
- `/slavicmyths dev hunt summon volkolak`
- `/slavicmyths dev hunt clear`

Do not build a huge admin command tree if none exists.

---

# 25. SPAWN EGGS

Mandatory:
- `ovinnik_spawn_egg`
- `volkolak_spawn_egg`

Creative tab:
same mod tab as other creature eggs/items.

Eggs are debug/creative convenience.
They ignore natural/summon environmental constraints, as vanilla spawn eggs usually do.

---

# 26. PERFORMANCE

Avoid:
- per-tick scan of 128 blocks for every player;
- per-tick biome search;
- per-tick huge entity searches.

Rules:
- Hunter Charm scans only on right-click.
- Horn candidate search only when used.
- Mini-boss AI uses normal goal ticks/path recalculation.
- Natural spawn uses spawn placement conditions, not global world scanning.

Particles:
- moderate;
- never hundreds every tick.

---

# 27. ANTI-EXPLOIT / EDGE CASES

Mandatory checks:

## Hunting Horn
- no successful summon inside water;
- no summon into walls;
- no summon if active owned encounter exists;
- cooldown server-side;
- selection NBT server-authoritative.

## Trophy Pouch
- no nesting itself;
- no duping on death;
- no invalid item insertion;
- NBT preserved.

## Accessories
- duplicate copies do not stack same modifier;
- modifiers removed correctly when unequipped;
- no permanent modifier after relog.

## Mini-bosses
- cannot despawn normally mid-fight;
- no block griefing;
- abilities don't attack through solid walls unless explicitly AoE around boss and line rules make sense;
- phase flags persist while entity lives;
- no multiple rage trigger.

---

# 28. README UPDATE — MANDATORY

Add a full `0.9.0 — Охота на нечисть I` section.

Must document:
- Ovinnik;
- Volkolak;
- HP;
- core abilities;
- silver weakness;
- water/rain Ovinnik counter;
- natural encounter idea;
- Hunting Horn controls;
- exact summon requirements;
- every new item and its function;
- Trophy Pouch allowed contents;
- Ritual Vessel currently intentionally unused;
- recipes;
- commands;
- spawn eggs;
- manual test procedure.

README cannot say Baba Yaga exists in 0.9.0.

---

# 29. MANUAL TEST CHECKLIST FOR README

Include:

1. `/give @s slavicmyths:ohotnichiy_rog`
2. Test target switching.
3. Test invalid daytime summon.
4. Test Ovinnik environment validation.
5. Successfully summon Ovinnik.
6. Check all four combat behaviors:
   - melee
   - slam
   - ash burst
   - chase/trail/projectile anti-kite
7. Put Ovinnik in water.
8. Test rain.
9. Kill and verify exact loot.
10. Test Volkolak summon in allowed biome at night.
11. Check pounce.
12. Check combo.
13. Check howl.
14. Check side movement.
15. Hit with non-silver weapon.
16. Hit with silver weapon and confirm bonus.
17. Check drops.
18. Craft/equip Ash Charm.
19. Verify fire damage reduction.
20. Craft/equip Wolf Belt and compare day/night.
21. Test Hunter Charm.
22. Open Trophy Pouch.
23. Insert allowed item.
24. Attempt invalid item.
25. Drop/re-pick pouch and confirm contents.
26. Verify Ritual Vessel does nothing except exist as intended.
27. Verify spawn eggs.

---

# 30. BUILD / POLYMC

After implementation:
1. DO NOT launch Minecraft.
2. Run headless build.
3. Fix compilation/resource errors caused by this task.
4. Produce JAR.
5. If known PolyMC instance path exists, replace current Slavic Myths test JAR.
6. Avoid multiple active versions in `mods`.
7. If path is unknown, do not guess — report output path.

---

# 31. IMPLEMENTATION ORDER FOR LOW-TOKEN CODEX

Follow this order:

1. inspect only current entity/item/registry/accessory/sound patterns;
2. inspect existing silver weapon IDs/tag possibility;
3. register items and tags;
4. register two entities + eggs;
5. implement models/textures/render;
6. implement loot tables;
7. implement Ovinnik AI;
8. implement Volkolak AI;
9. implement Horn;
10. implement Hunter Charm;
11. implement accessory effects;
12. implement Trophy Pouch;
13. recipes + JEI;
14. advancements;
15. localization;
16. README;
17. headless build;
18. PolyMC copy.

Do not stop after registration and claim completion.

---

# 32. DEFINITION OF DONE

## Ovinnik
- [ ] unique model;
- [ ] 256 texture;
- [ ] 175 HP;
- [ ] active AI;
- [ ] heavy melee;
- [ ] slam;
- [ ] ash burst;
- [ ] chase/trail;
- [ ] anti-kite projectile;
- [ ] rage phase;
- [ ] water weakness;
- [ ] rain interaction;
- [ ] fire immunity;
- [ ] sounds;
- [ ] spawn egg;
- [ ] loot.

## Volkolak
- [ ] unique model;
- [ ] 256 texture;
- [ ] 165 HP;
- [ ] active flank AI;
- [ ] pounce;
- [ ] triple combo;
- [ ] howl;
- [ ] side dodge;
- [ ] blood-scent phase;
- [ ] full moon flavor bonus;
- [ ] silver +25%;
- [ ] sounds;
- [ ] spawn egg;
- [ ] loot.

## Items
- [ ] all 10 registered;
- [ ] reference designs followed;
- [ ] ingredient textures >=256;
- [ ] complex item textures 512 preferred;
- [ ] recipes exact;
- [ ] tooltips;
- [ ] Horn functional;
- [ ] Ash Charm functional;
- [ ] Wolf Belt functional;
- [ ] Hunter Charm functional;
- [ ] Trophy Pouch 9 slots functional;
- [ ] Ritual Vessel intentionally inert.

## Systems
- [ ] Hunt encounter basic state;
- [ ] one active summoned encounter per player;
- [ ] no endless respawn;
- [ ] natural spawns rare;
- [ ] tags implemented;
- [ ] JEI;
- [ ] advancements;
- [ ] README;
- [ ] build passes;
- [ ] no client auto-launch.

---

# 33. FINAL CODEX RESPONSE

Return ONLY:

## Реализовано
Short factual bullets.

## Мобы
Actual IDs + HP + implemented abilities.

## Предметы
Actual IDs + implemented functions.

## Команды
Exact working commands.

## Файлы
Main changed paths.

## Build
Result + JAR path.

## PolyMC
Copied path or explicitly unknown.

## Не выполнено
Only items that are genuinely incomplete.

Do not claim completed work that is not in repository files.
