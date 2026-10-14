# Точки изменения будущего баланса — без исправлений

Это технические замечания к существующему аудиту 1.9.8.1. Production1.9.7 не изменена.
Номер строки относится к текущему рабочему дереву, включая ранее существовавшие правки.

## Горыныч 3600 → 2800; пороги1500/75

Нельзя исправить только регистрацию MAX_HEALTH: она намеренно1024.

| Место | Что согласовать |
|---|---|
|src/main/java/org/slavicmyths/gorynych/GorynychRules.java:4|HP3600→2800; TRANSFORM_HP1800→1500, FLOOR180→75. Все три значения VERIFIED; доли50%/5% не сохранять.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:30|Начальная synced логическая HP через Rules.HP.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:35|configure читает ledger HP, clamp и проекция1024*logical/HP. Нельзя мигрировать только entity NBT: ledger переопределяет его.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:48|Progress использует Rules.HP, но строка имени bossbar содержит literal3600.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:51|Очищение восстанавливает Rules.HP и служебные1024.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:82|hurt временно выставляет1024, собирает pendingDamage через setHealth, вычитает из logicalHealth; ветвление TRANSFORM/FLOOR, persist.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:83|setHealth — сборщик реально обработанного урона, не обычная setter логического HP.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:86|exhaust устанавливает Rules.FLOOR, очищает действия, переводит состояние.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:97|persist сохраняет HP/state/age/clean/wings/node в SavedData.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:98|entity NBT GorynychHP и остальные поля состояния.|
|src/main/java/org/slavicmyths/gorynych/Gorynych.java:99|literal clamp180..3600, fallback3600, делитель3600. После чтения вызывается configure из ledger.|
|src/main/java/org/slavicmyths/gorynych/GorynychLedger.java:13|ensure начального HP новых встреч.|
|src/main/java/org/slavicmyths/gorynych/GorynychWorld.java:14|Создание/восстановление отсутствующего актёра через configure(id, record).|

Для старых сохранений выбрать явно: сохранить абсолютный HP с clamp либо долю
HP (×2800/3600). Это даёт разное положение относительно1500/75 и может открыть
или пропустить фазу. Установить версию миграции/идемпотентность обоих NBT источников,
правило при расхождении entity/ledger, сохранить already cleansed/exhausted и clean mask.
Действия head/body/захват после загрузки сейчас сбрасываются; не возобновлять устаревший
захват при миграции. Публичный UI и локализации с двумя placeholders должны получать
новый максимум, а не служебный1024. Отдельный max logical следует отражать в будущем
спецификационном решении. Обновление tools/gorynych-tests и исторических утверждений
о3600 потребуется после gameplay изменения; в аудите тесты не переписываются.

## Атаман

Регистрационная фабрика `BanditEntity.attributes(4)` задаёт150:
src/main/java/org/slavicmyths/bandit/BanditEntity.java:51;
src/main/java/org/slavicmyths/registry/ModEntities.java:57.
Генерация большого лагеря повторно задаёт150, затем полное getMaxHealth:
src/main/java/org/slavicmyths/bandit/LargeCampPiece.java:79.
Ветви ataman_gate и другая роль задают разную экипировку/скорость. Цель100 должна
действовать в обоих путях, не менять registry ID и не выравнивать экипировку незаметно.
Существующие сохранённые attributes не гарантированно заменяются новой фабрикой:
миграция уже созданных атаманов — отдельное требование, не доказанный результат
изменения одной константы. Не лечить весь лагерь при каждой загрузке.

## Тугарин

База350 находится в src/main/java/org/slavicmyths/hunt/BossKind.java:3;
фабрика WorldBoss.attributes использует k.hp:
src/main/java/org/slavicmyths/hunt/WorldBoss.java:58.
Повторное назначение k.hp при NBT load:
src/main/java/org/slavicmyths/hunt/WorldBoss.java:124.
BossAnchors.spawn создаёт именно зарегистрированный тип, не отдельную HP шкалу:
src/main/java/org/slavicmyths/hunt/BossAnchors.java:46.
Цель400 требует согласования загрузки старого Health/Attributes (сохранить долю
или абсолютный HP), проверки обоих самостоятельных/сюжетных путей.
Трансформация на50% использует getMaxHealth; load принимает PHASE_TWO или
TRANSFORMING как уже вторую фазу. Не возвращать старого босса в первую фазу только
потому, что вырос максимум. ATTACK_DAMAGE отдельно фиксируется12 в tick/load
TugarinCombat; изменение регистрационной атаки не заменит это значение.
Сопротивление входящему урону*.15 во время перехода и*.5 во второй фазе сохранять
отдельно от HP: src/main/java/org/slavicmyths/hunt/WorldBoss.java:121.

## Жар-птица

Регистрация1024 — src/main/java/org/slavicmyths/firebird/FirebirdContent.java:27.
Конструктор setHealth3000, getMaxHealth3000 и getAttributeValue(MAX_HEALTH)3000:
src/main/java/org/slavicmyths/firebird/FirebirdEntity.java:23,
src/main/java/org/slavicmyths/firebird/FirebirdEntity.java:25,
src/main/java/org/slavicmyths/firebird/FirebirdEntity.java:26.
Не заменять их на vanilla capped attribute при сохранении3000 HP.
Полной неубиваемости в проверенном классе нет: hurt вызывает super.hurt,
die закрывает encounter и вызывает super.die:
src/main/java/org/slavicmyths/firebird/FirebirdEntity.java:68,
src/main/java/org/slavicmyths/firebird/FirebirdEntity.java:69.
Наличие fireImmune и отсутствие fall damage обеспечивают лишь частные иммунитеты.
Ответный20 HP удар не является защитой от летального входящего урона.
В следующем ТЗ явно решить семантику обычного/периодического/void/generic_kill урона,
setHealth0, die/kill и команды удаления; удаление Entity не тождественно смерти.
Проверить NBT Health и жизненный цикл encounter, не терять выдачи наград при
нормальном уходе. Runtime не запускался; «полная неубиваемость» остаётся
UNVERIFIED, а обнаруженный обычный путь смерти — подтверждённый конфликт требования.

## Серебро и другие уязвимости

Единственный найденный специализированный silver subscriber:
src/main/java/org/slavicmyths/item/SilverCombat.java:18.
На LivingIncomingDamageEvent прибавляет1.5 для Player, directEntity==entity,
серебряного main hand и EntityType из silver_vulnerable. Проверка оружия:
src/main/java/org/slavicmyths/item/SilverCombat.java:15.
Текущий тег содержит только slavicmyths:leshy:
src/main/resources/data/slavicmyths/tags/entity_type/silver_vulnerable.json:4.
Материал и семантический тип существа сами по себе этот тег не заменяют.
Моровый Леший сохраняет ID и потому попадает под тот же тег.

**Доказанного второго silver начисления в проверенных специализированных путях
не обнаружено.** Не записывать возможный будущий дубль как существующий дефект.
Будущее применение всех нескольких типов должно вычислять одну итоговую
уязвимость за hit, а не начислять отдельно за «дух» и «нечисть». Выбрать правило
max/priority/единого compound multiplier и событие. Не добавлять одновременно
в hurt сущности и в incoming subscriber; сохранить direct hit фильтр и semantics
projectile/periodic. Проверять обе стадии raw/pre, вложенные hurt и отражение.

Другие существующие корректировки независимы: hunter charm +.5 для Animal
(SilverCombat.java:21), руны добавляют raw на incoming
(src/main/java/org/slavicmyths/rpg/RuneEffects.java:24), разряд копья+2
(src/main/java/org/slavicmyths/item/ThunderSpearCombat.java:24),
Winters damage multiplier (src/main/java/org/slavicmyths/winters/WintersEvents.java:97),
защита знамений Картауса (src/main/java/org/slavicmyths/finist/FinistEvents.java:9),
сопротивление Тугарина в hurt. Их нельзя объединить в «серебро» либо потерять
при централизации. Все комбинации subscriber priorities/дополнительных datapacks
— UNVERIFIED; статический поиск не доказывает однократность runtime событий.

## Многоударный урон

ATTACKS.csv различает raw HP, net-calibrated expressions, эффекты и периодический
урон. Целевые A1..A7 — предложения категорий, не реализованные коэффициенты.
Не умножать каждую часть комбо на полную цель действия, иначе бюджет растёт
на число ударов. Сначала определить unit (hit/action/полное окно), доли попаданий,
пределы суммарного урона и правила DoT, затем менять все producers и consumers.

- Тугарин SPIN16×3, BLADE5+5+8, BREATH6×3; native immunity не сбрасывается.
  src/main/java/org/slavicmyths/hunt/TugarinCombat.java:78,
  src/main/java/org/slavicmyths/hunt/TugarinCombat.java:90.
- Волколак HuntMob combo4+4+7 сокращает window до10; CursedVolkolak
  применяет attribute delta и phase3+2 на каждый strike, fixed shield16 — другой путь.
  src/main/java/org/slavicmyths/hunt/HuntMob.java:89,
  src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:60.
- Добрыня, Алёша и Ским сбрасывают invulnerableTime0; сумма raw комбо действительно
  может отличаться от обычных vanilla комбо с той же частотой.
  src/main/java/org/slavicmyths/marinka/DobrynyaEntity.java:44,
  src/main/java/org/slavicmyths/skim/AlyoshaEntity.java:42,
  src/main/java/org/slavicmyths/skim/SkimBeastEntity.java:63.
- raw(net) у Скима/Картауса/Калина и rawForDiamond/ForIron у Духа земли —
  нелинейная обратная калибровка. Net число не является текущим прямым raw HP.
  src/main/java/org/slavicmyths/earth/EarthRules.java:18,
  src/main/java/org/slavicmyths/skim/SkimBeastEntity.java:62.
- Горыныч: 6 pulse ray/cone,3 grab ticks,5 бомбардировок,7 дождевых снарядов,
  отдельный splash и cloud every20/life240; бюджет двух действий — ограничитель
  concurrency, не общий HP budget. Менять Gorynych.hit недостаточно: BreathShot
  и BreathCloud вызывают hurt сами.
- Полоз: funnel3×8 отдельно от phase multiplier strike; rain budget12 записывается
  до вызова hurt, i-frame может отклонить удар. Нельзя считать этот бюджет
  измеренной потерей HP. src/main/java/org/slavicmyths/poloz/GreatPoloz.java:124.
- Хранители: WHIP3×2, WHIRL2×4, LOCKDOWN2×3 pulse×3 wave;
  не превращать лечение/темницу/разлив в выдуманный прямой урон.

У Тугарина старые SLAM/SPIN/BREATH ветки WorldBoss.Fight недоступны после раннего
перехода в TugarinCombat; STONE в новом scheduler тоже не выбирается choose.
Они исключены из доступных атак. У Игоши поздняя ветка think с getTarget!=null
находится после раннего return при наличии target: не заявлять её Weakness/
Blindness45 как доказанную доступную способность.

Ручной план последующей приёмки: отдельный тестовый мир; old/new NBT сущности+
ledger; границы фаз и UI; lethal/fire/void у Жар-птицы; один silver bonus;
записанные фактические hits/суммарный HP в комбо. Здесь запусков **0**, build не
повторялся, MSPT/FPS/мобы в час не измерялись.

## Лихо — normal350 и mor700, один EntityType

- src/main/java/org/slavicmyths/hunt/BossKind.java:3 — LIKHO base320→350.
- src/main/java/org/slavicmyths/hunt/WorldBoss.java:58 — attributes(k.hp);
  src/main/java/org/slavicmyths/hunt/WorldBoss.java:124 — NBT повторно задаёт базу.
- src/main/java/org/slavicmyths/mor/MorController.java:49 — существующий
  ADD_MULTIPLIED_TOTAL+1 даст700; не добавить второй множитель/отдельный тип.
  src/main/java/org/slavicmyths/mor/MorController.java:48 — hasModifier по ID,
  src/main/java/org/slavicmyths/mor/MorController.java:113 — load: проверить порядок
  permanent modifier и повторного base reset, сохранение доли без autoheal.
- src/main/java/org/slavicmyths/mor/HigherMor.java:25 — активный mor scheduler;
  src/main/java/org/slavicmyths/mor/HigherMor.java:38 — sweep14/12,
  src/main/java/org/slavicmyths/mor/HigherMor.java:52 — beam12.
  Literal спецатак не равен атрибуту11×1.05; ordinary Fight заменяется MorGoal.

## Проклятый Оборотень — условная цель700, не внедрять до B1

- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:33 — HP400/base15.
- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:102 — phase250/100:
  PROPOSED сохранить доли62.5%/25%, получить437.5/175, утвердить с B2/B3.
- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:103 — bar denominator400.
- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:60 — strike attribute
  delta и phase3+2; GRAB shield16 не обязательно меняется с базой.
- VolkolakEvents.lethal (точный source в FORMS.md) и escape: сохранить сюжетное
  несмертельное завершение отдельно от независимой встречи. Проверить Health/phase
  NBT и сохранённые Attributes, не путать с hunt/HuntMob ordinary volkolak165.

## Хранители —900/800, состояние очищения того же типа

- src/main/java/org/slavicmyths/guardians/GuardianRules.java:6 — FIRE_HP/WATER_HP;
  src/main/java/org/slavicmyths/guardians/GuardianRules.java:10 — threshold5%:
  будущие45/40 следуют существующему правилу, PROPOSED сохранить5%.
- src/main/java/org/slavicmyths/guardians/GuardianEntity.java:25 — attributes;
  :38 configure ledger Health; :68 beginPurification; :72 setHealth interception.
  Проверить entity NBT/ledger, самостоятельную и структурную встречи отдельно.
- src/main/java/org/slavicmyths/guardians/GuardianLedger.java:17 — ensure;
  src/main/java/org/slavicmyths/guardians/GuardianLedger.java:21 — stage;
  src/main/java/org/slavicmyths/guardians/GuardianLedger.java:35 — load:
  ACTIVE→READY/сброс вентилей, не сбросить CLEANSED/claims.
- FireGuardian/SpringKeeper producers и WaterEcho/WaterPrison источники в ATTACKS:
  новая HP не меняет literal melee/slam/pulse/whip/whirl, лечение5 и echo25.
  Не считать очищение смертью, не выдавать standalone структурные фрагменты.

## Разделение рисков

Блокирующие решения **B1–B4** описаны в REPORT.md и UNVERIFIED.csv. Они блокируют
зависимые изменения, но не завершение аналитического инвентаря.
В рамках реализации проверить доступность STATIC_CANDIDATE/UNVERIFIED_PRODUCER,
компоненты damage budget, native immunity, сохранения, фактические lethal пути
Жар-птицы (обязательная защита уже согласована), no duplicate silver и subscriber
order. Эти проверки не являются дополнительными обязательными игровыми gates
аналитического1.9.8.1. Полная runtime достижимость здесь не заявляется.
