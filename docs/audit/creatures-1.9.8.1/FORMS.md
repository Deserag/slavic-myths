# Формы существ — продолжение 1.9.8.1

Форма не равна EntityType. Номера состояний ниже подтверждены исходниками;
восстановление после настоящего игрового перезахода NOT_RUN.

## Лихо

Обычное `slavicmyths:likho_one_eyed` — `WorldBoss`, 320 HP, базовая атака11,
броня10/стойкость2/KB .65: `src/main/java/org/slavicmyths/hunt/BossKind.java:3`,
`src/main/java/org/slavicmyths/hunt/WorldBoss.java:58`.
Фазы обычного Лиха: >65%, ≤65%, ≤30% (`src/main/java/org/slavicmyths/hunt/BossRules.java:9`).
Моровое Лихо использует тот же ID; kind5 в `src/main/java/org/slavicmyths/mor/MorController.java:38`.
Постоянный `mor_health` ADD_MULTIPLIED_TOTAL +1 даёт640 HP, `mor_damage` +.05
даёт11.55 атрибутного урона; спецатаки HigherMor имеют отдельные literal12/14.
Вторая моровая фаза при50%: `src/main/java/org/slavicmyths/mor/HigherRules.java:5`,
`src/main/java/org/slavicmyths/mor/HigherMor.java:28`. Название «проклятое Лихо» не задаёт
другой EntityType или третью доказанную форму. Цель350 относится к обычному;
целевые HP морового Лиха700 — VERIFIED по финальному заданию; тот же EntityType. Не создавать третью «проклятую» форму.

## Волколаки и Оборотень

`slavicmyths:volkolak` — VolkolakEntity → HuntMob,165 HP/base9,
отдельная ритуальная охота (`src/main/java/org/slavicmyths/hunt/HuntMob.java:52`).
`slavicmyths:cursed_volkolak` — CursedVolkolak,400 HP/base15/armor7/KB .6,
самостоятельный ID (`src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:33`).
Его ru_ru имя из существующего индекса: «Проклятый Оборотень». Это подтверждает
соответствие локализации, но не автоматическую замену всех употреблений слова
«Оборотень» в старых ТЗ. Цель700 для этого ID — PROPOSED по финальному заданию, не внедрять до подтверждения пользователя.
Фазы на250/100 HP; UI знаменатель400 — отдельные места будущего изменения:
`src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:102`,
`src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:103`.
UPRIGHT меняет габариты/скорость; это поза того же моба. Escape сюжетного экземпляра
перехватывает смертельный урон, оставляет1 HP и удаляет актёра после сцены;
не распространять это на независимый summon/egg:
`src/main/java/org/slavicmyths/volkolak/VolkolakEvents.java:12`,
`src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:99`.

## Все формы Мора

| kind | Исходное существо | HP normal → mor | Атрибут ATTACK_DAMAGE |
|---|---|---|---|
|1|Леший|100→250|8→9.6|
|2|Полудница|60→120|7→7.84|
|3|Кикимора|36→72|5→5.25|
|4|Огненный змей|155→387.5|7→8.4|
|5|Лихо|320→640|11→11.55|
|6|Овинник|175→350|10→10.5|

Источник всех множителей: `src/main/java/org/slavicmyths/mor/MorController.java:49`;
базы: CREATURES.csv. Это атрибуты, а не множитель каждого literal спецудара.
State0 обычный,1 трансформация50 тиков,2 заражённый. Natural сразу2 и полное HP;
склянка начинает1 и затем сохраняет долю здоровья:
`src/main/java/org/slavicmyths/mor/MorController.java:40`,
`src/main/java/org/slavicmyths/mor/MorRules.java:14`,
`src/main/java/org/slavicmyths/mor/MorFlaskProjectile.java:18`.
Склянка радиус3, доступны kinds1..6; protected/name/friend/owner/passenger/clone
отсекаются. Natural только kinds1..4: Overworld с7-го дня, >160 от world spawn,
NATURAL/SPAWNER, roll<4 из100, нет другого заражённого/ожидающего в48×24×48:
`src/main/java/org/slavicmyths/mor/MorEvents.java:12`,
`src/main/java/org/slavicmyths/mor/MorRules.java:11`.
Таким образом моровые Лихо/Овинник не входят в этот natural обработчик.
Клоны морового змея — тот же FIRE_SERPENT ID с MOR_CLONE и parent UUID;
HP30, максимум2, lifetime1800; summon один раз при≤50%, safe placement/retry20.
`src/main/java/org/slavicmyths/mor/MorController.java:104`,
`src/main/java/org/slavicmyths/mor/MorController.java:108`.
Это не обычный `serpent_projection` (отдельная краткая приманка).
NBT MorSnapshot/HigherSnapshot + постоянные modifiers; повторное добавление
защищено hasModifier(id). Порядок load важен: subclasses HuntMob/ElementHuntMob/
WorldBoss повторно устанавливают base attributes после общего load. Это риск
изменения баз, не доказанное удвоение modifiers:
`src/main/java/org/slavicmyths/mor/MorController.java:113`,
`src/main/java/org/slavicmyths/hunt/ElementHuntMob.java:129`.

## Хранители

Оба ID сохраняются после очищения; общий GuardianEntity/GuardianRules.
SEALED/IDLE, пробуждение60, бой, несмертельный предел5%, PURIFY60, CLEANSED;
порог огненного25 HP, водного22.5 HP:
`src/main/java/org/slavicmyths/guardians/GuardianRules.java:10`,
`src/main/java/org/slavicmyths/guardians/GuardianEntity.java:25`.
Огненный: lockdown доступен≤75%; LastEmbers≤25%, warning18 вместо24,
pulse rest224 вместо280. Водный: desperation≤30%,2 WaterEcho по25 HP,
whip cooldown110 вместо140; лечение запрещеноStandalone и двумя закрытыми вентилями.
`src/main/java/org/slavicmyths/guardians/FireGuardian.java:10`,
`src/main/java/org/slavicmyths/guardians/SpringKeeper.java:19`,
`src/main/java/org/slavicmyths/guardians/SpringKeeper.java:22`.
Структурная/Standalone привязка — вариант встречи, не новый ID;
очищение и выдача фрагментов не равны убийству.

## Горыныч

PATROL0 → GROUND1 → TRANSFORM2 при1800 → AIR3; EXHAUSTED4 при180;
CLEANSING5 с Clean bitmask трёх голов → PURIFIED6 после160.
TRANSFORM120, крылья на40; air цикл400 с наземными200 и воздушными200.
3600 логических HP проецируются в1024 служебных; головы multipart не имеют
самостоятельной полосы HP или registry ID.
`src/main/java/org/slavicmyths/gorynych/GorynychRules.java:4`,
`src/main/java/org/slavicmyths/gorynych/Gorynych.java:56`,
`src/main/java/org/slavicmyths/gorynych/Gorynych.java:82`.
NBT сущности GorynychHP/state/clean/wings/age/node и SavedData HP/state/clean/wings
должны мигрировать согласованно; configure может заменить entity NBT данными ledger.
После очищения HP снова3600. Самостоятельная встреча не даёт сюжетных наград.

## Прочие состояния, не новые существа

- Wildlife: медведица120 HP/armor6/damage8 и оленёнок12/damage0 того же
  WildlifeEntity ID: `src/main/java/org/slavicmyths/entity/WildlifeEntity.java:96`,
  `src/main/java/org/slavicmyths/entity/WildlifeEntity.java:75`.
- SWAMP Русалка36 и Водяной72 вместо28/44: `src/main/java/org/slavicmyths/water/WaterSpirit.java:16`.
- Подземник шахтёр40/base6.5 вместо28/5, Variant NBT:
  `src/main/java/org/slavicmyths/underground/Podzemnik.java:22`.
- Песиголовец HUNTER40/SCOUT28/LEADER50, один ID, роль сохраняется:
  `src/main/java/org/slavicmyths/pesigolovtsy/DogheadRules.java:6`,
  `src/main/java/org/slavicmyths/pesigolovtsy/DogheadEntity.java:82`.
- Полудница calm/transform/hostile: FORM state, не EntityType:
  `src/main/java/org/slavicmyths/entity/PoludnitsaEntity.java:26`.
- Тугарин PHASE_ONE/TRANSFORMING/PHASE_TWO на50%, incoming*.15/*.5:
  `src/main/java/org/slavicmyths/hunt/TugarinCombat.java:181`,
  `src/main/java/org/slavicmyths/hunt/WorldBoss.java:120`.
- Маринка falseBody/veil/bird и Добрыня bull: состояния актёров; HexAnchor,
  TurMinion — auxiliary: `src/main/java/org/slavicmyths/marinka/MarinkaEntity.java:68`,
  `src/main/java/org/slavicmyths/marinka/MarinkaEntity.java:60`.
- Финист morph/curse/falcon actor: тот же союзник;
  `src/main/java/org/slavicmyths/finist/FinistEntity.java:32`.
- Морозко/Карачун trial phases и yield5%, IceDouble/PlayerDouble/NightRaven
  — вспомогательные зарегистрированные сущности; Морозко1000, Карачун1200 default
  configurable, Калин1800, Полоз3000 getMaxHealth. Высокие HP нельзя выводить из
  одного MAX_HEALTH в регистрации.

Полнота остальных комбинаций NBT/экипировки/событий — UNVERIFIED;
этот документ не является игровым roundtrip тестом.

## Финальные цели состояний (2026-10-13)

|Состояние одного ID|Текущее HP|Целевое HP|Статус|
|---|---:|---:|---|
|likho_one_eyed normal|320|350|VERIFIED|
|likho_one_eyed mor|640|700|VERIFIED; base350 × существующий mor_health2|
|zmey_gorynych full logical|3600|2800|VERIFIED|
|zmey_gorynych TRANSFORM threshold|1800|1500|VERIFIED; абсолютный порог|
|zmey_gorynych EXHAUSTED floor|180|75|VERIFIED; абсолютный порог|
|firebird narrative actor|3000|3000 + неубиваемость|VERIFIED; обычный путь смерти — DEFECT|
|cursed_volkolak full|400|700|PROPOSED; отдельный ID от volkolak|

Все другие формы сохраняют текущие множители как PROPOSED, если отдельно не
согласованы: медведица120/оленёнок12; SWAMP Русалка36/Водяной72;
шахтёр40; песиголовец28/40/50. Не переносить normal HP на все роли.
Моровые цели при предложенном сохранении normal: Леший250, Полудница120,
Кикимора72, Огненный змей387.5, Овинник350; Лихо700 утверждено отдельно.
Предложенные базы моровых атак сохраняют normal nominal base, без нового
множителя каждой спецатаки; текущие атрибутные множители остаются отдельной
формулой. Уровень морового Лиха предлагается VI как normal, опасность усиливают
HP700 и отдельный набор HigherMor. Поза/phase/summon не добавляют самостоятельных
существ к69. Живой auxiliary и связанный WarHorse оставлены в исходной группе,
не меняя соглашение подсчёта.
