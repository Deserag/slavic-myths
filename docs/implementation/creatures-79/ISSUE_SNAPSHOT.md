# ИНДИВИДУАЛЬНОЕ ТЗ v4 • 20.10.2026 — КЛАССЫ СУЩЕСТВ, УГРОЗЫ И ЭФФЕКТЫ
**Решение владельца:** согласована архитектура из **8 природных классов + независимые поведение, дополнительные свойства и отношения**. В №79 входит весь мод, не только Чудь. **Серый Волк не должен самопроизвольно атаковать высшее Мор-Лихо**: прежнее требование про эту пару как об обязательной вражде **отменено**. Эту сущность оставить сюжетной, вмешательство не включать без отдельного сценария. Богатырей в рамках №79 не перерабатывать (ведутся в другом чате), но обеспечить совместимость.

## 0. Исходная база и границы
- Главный исходник инвентаря: [аудит существ 1.9.8.1](https://github.com/Deserag/slavic-myths/blob/main/docs/audit/creatures-1.9.8.1/CREATURES.csv), [отчёт](https://github.com/Deserag/slavic-myths/blob/main/docs/audit/creatures-1.9.8.1/REPORT.md), [реестр деклараций](https://github.com/Deserag/slavic-myths/blob/main/docs/audit/creatures-1.9.8.1/REGISTRY.md), [формы и NBT](https://github.com/Deserag/slavic-myths/blob/main/docs/audit/creatures-1.9.8.1/FORMS.md). **Индекс 111 типов = 69 самостоятельных существ + 42 вспомогательных типа**. Это исходная зафиксированная инвентаризация, **не гарантия, что текущий рабочий HEAD идентичен**; до правок подтвердить `git status`, HEAD и актуальный runtime registry, учесть новые файлы и параллельную работу.
- Существующий центр допуска атаки: `combat/CombatTargeting.java`; существующие `CreatureSafety.java`, `MorController/MorRejection/HigherMor`, `GuardBehavior`, `ChudPerson`, `ChudThreats`, `HeroThreats`, локальные контроллеры боссов и NPC. [Схема потоков отношений](https://github.com/Deserag/slavic-myths/blob/main/docs/design/MECHANICS_FLOW_BLUEPRINTS.svg) — **схема, не внедрённый контракт**.
- **Не менять автоматически** урон способностей, HP, кулдауны, шкалы наград, цели истории, entity ID, спавн, `MobCategory`, наследование Java-классов `Monster`/`Animal`, существующие NBT и квестовые события. Не строить новую параллельную `FactionManager` с собственным источником истины; расширять единый контракт.
- Реализация — после сохранения предыдущих незакоммиченных рабочих пакетов; не смешивать их diff и не делать commit вопреки `AGENTS.md`.

## 1. ТРИ НЕЗАВИСИМЫЕ ОСИ (не путать)
1. **Природа / physiology**: ровно один primary class `HUMANOID | ANIMAL | MYTHICAL_BEAST | SPIRIT | UNDEAD | ELEMENTAL | CONSTRUCT | PLANT`. Это не vanilla `MobCategory`, не `Enemy` и не существующий `class_mythical`. Первичный класс даёт только базовые свойства.
2. **Поведение / intent**: `PEACEFUL | NEUTRAL | HOSTILE | DEFENDER`, вычисляемое в контексте owner/team/story/mor/state и конкретной ситуации, **не постоянная характеристика всех экземпляров одного ID**. `HOSTILE` означает, что NPC способен искать допустимого врага, **но НЕ “все hostile атакуют всех hostile”**.
3. **Черты / traits**: `BLOODLESS, FIRE_AFFINITY, ICE_AFFINITY, WATER_AFFINITY, EARTH_AFFINITY, SILVER_VULNERABLE, CONTROL_RESISTANT, BOSS, SUMMONED, STORY_PROTECTED` и при необходимости дополнительные реально подтверждённые признаки. Не давать автоматически `BOSS` всем `MobCategory.MONSTER`. `STORY_PROTECTED` — контроль контекста сцены, **не абсолютная физическая неуязвимость к урону игрока по умолчанию**.
4. **Принадлежность / affiliation** — конкретный владелец, отряд/команда, жители Чуди, деревня, разбойники, курганная нежить, моровые существа, призванные слуги. Это отношения **между участниками**, а не физиология. Фракция/союз может быть временным. Состояния мира и сюжета имеют приоритет над статическим tag.

**Технически**: JSON `data/slavicmyths/tags/entity_type/nature_<class>.json` и минимальные tag sets для черт; один runtime resolver/adapter для static `EntityType` + dynamic state, единое API через `CombatTargeting.relation(actor,target,context)` и `canAttack`. `isEnemy` и `isThreatToSettlement` — явные методы/предикаты контекста, не 69 ручных `instanceof`. Кэшировать неизменяемые registry принадлежности; динамическое состояние проверять по экземпляру, без world scan и без Tick NBT миграций. Пустой primary class для новой сущности — явный `UNCLASSIFIED` в аудите и FAIL gate выпуска, **не падение игры**. Снаряды/props не получают биологический класс насильно.

## 2. Базовая таблица свойств (целевая политика после проверки сохранения прежних специальных правил)
| Primary nature | Обычное кровотечение | Обычный биологический яд | Множитель входящего ИМЕННО огненного урона |
|---|---|---|---|
| `HUMANOID` | да | да | 1.00 |
| `ANIMAL` | да | да | 1.00 |
| `MYTHICAL_BEAST` | да, если нет `BLOODLESS` | да, если нет иммунитета | 1.00 |
| `SPIRIT` | **нет** | **нет** | **1.25** |
| `UNDEAD` | **нет** | **нет** | 1.00 |
| `ELEMENTAL` | **нет** | **нет** | **1.00 до указания стихии** |
| `CONSTRUCT` | **нет** | **нет** | 1.00 до указания материала |
| `PLANT` | **нет** | **нет** | **1.50** |

- `FIRE_AFFINITY`: огонь ×**0.50**; `ICE_AFFINITY`: огонь ×**1.50**; конкретное свойство **ЗАМЕНЯЕТ** природный множитель, НЕ перемножает его. При наличии двух взаимоисключающих стихий конфликт должен быть явным FAIL инвентаризации, а не случайным порядком проверок.
- Сохранить **существующую нативную `.fireImmune()`** и специальные `isInvulnerableTo` (например, `ovinnik`, `fire_serpent`, `firebird` уже fireImmune). Коэффициент никогда не должен превращать штатный иммунитет в получаемый урон.
- Огненный коэффициент применять **однократно** в контролируемом обработчике accepted incoming damage после фильтра допустимых источников, не на каждом отдельном event hook; обрабатывать `DamageTypeTags.IS_FIRE`, верифицировать существующие custom damage types и по необходимости корректно добавить их в тег. Не расширять на произвольную магию, пар, яд или удар топором только из-за названия способности. Сохранить ванильную броню, защиту чар, fire resistance, щит и существующие бонусы.
- Биологический яд = `MobEffects.POISON` и только явно родственные физиологические яды: не отменять `HARM`, магию, моровую хворь, проклятие, слабость и другие дебаффы просто потому, что жертва дух.
- Кровотечение — уже существующие `CutEffect.CUT`, `RareBleedEffect.RARE_BLEED`, `WolfBleeding.WOLF_CUT` и другие реальные источники cut/bleed (инвентаризировать). Новый контракт `canBleed(target)` используется **при наложении и тиках**; для `BLOODLESS` эффект не появляется, оставшиеся исторические инстансы снимаются, нет наложения/наложения стаков из разных источников, старые NBT владельцев не очищать массово. `BAD_BLOOD` оборотня — **другая механика снижения лечения/еды, не физическое кровотечение**, не отменять её автоматически.
- Существующие теги `bleed_immune`, `class_bloodless`, `silver_vulnerable`, `class_mythical`, `class_control_resistant`, `control_immune`, `combat_protected`, `village_hostiles`, `ominous_aura_immune`, `#minecraft:undead` **не удалять и не переименовывать**. Развести semantic и фактическое применение: `MYTHICAL_BEAST` не тождественен `class_mythical`; старые исключения и формулы серебра оставить (в том числе 1.5 бонус по старой механике) без массового распространения на всех духов. Каждый старый tag — инвентарь callsites + регрессионная проверка.
- Множители из таблицы — новая согласованная **типовая политика**, но если конкретный существующий босс уже имеет механически значимую фазовую защиту или иммунитет, приоритет имеет текущая фаза/сценарий. Документировать exception, без “поправим фазу/баланс заодно”.

## 3. Полная типизация ВСЕХ 69 самостоятельных существ — исходная матрица
Ниже **каждый ID из `CREATURES.csv`** указан ровно один раз. Предложения основной природы согласованы архитектурно по общему смыслу и исходному аудиту, а не выданы за уже существующие runtime tag. Исполнитель перед внедрением сопоставляет реальные модели, состояние крови/физическую природу, события и эффекты; несовместимости выделяет в отчёте как `CONFLICT` и не подменяет новым видом/существом. В спорных случаях ниже даны отдельные оговорки.

### HUMANOID — 20 самостоятельных ID
- Алёша Попович — `slavicmyths:alyosha_popovich`
- Атаман — `slavicmyths:ataman`
- Баба-яга — `slavicmyths:baba_yaga`
- Разбойник-лучник — `slavicmyths:bandit_archer`
- Разбойник-боец — `slavicmyths:bandit_fighter`
- Тяжёлый разбойник — `slavicmyths:bandit_heavy`
- Старший разбойник — `slavicmyths:bandit_senior`
- Добрыня Никитич — `slavicmyths:dobrynya_nikitich`
- Финист Ясный Сокол — `slavicmyths:finist`
- Илья Муромец — `slavicmyths:ilya_muromets`
- Воин Калина — `slavicmyths:kalin_common_soldier`
- Дружинник Калина — `slavicmyths:kalin_elite_soldier`
- Калин-царь — `slavicmyths:kalin_tsar`
- Картаус Рыжий Ус — `slavicmyths:kartaus`
- Кладовик — `slavicmyths:kladovik`
- Крупный Подземник — `slavicmyths:large_podzemnik`
- Маринка — `slavicmyths:marinka`
- Соловей-разбойник — `slavicmyths:nightingale`
- Песиголовец — `slavicmyths:pesigolovets`
- Подземник — `slavicmyths:podzemnik`

### ANIMAL — 11 самостоятельных ID
- Медвежонок — `slavicmyths:bear_cub`
- Кабан — `slavicmyths:boar`
- Бурый медведь — `slavicmyths:brown_bear`
- Карп — `slavicmyths:carp`
- Речной рак — `slavicmyths:crayfish`
- Олениха — `slavicmyths:doe`
- Лесной волк — `slavicmyths:forest_wolf`
- Боевой конь Калина — `slavicmyths:kalin_war_horse`
- Щука — `slavicmyths:pike`
- Сивка-Бурка — `slavicmyths:sivka_burka`
- Олень — `slavicmyths:stag`

### MYTHICAL_BEAST — 10 самостоятельных ID
- Проклятый Оборотень — `slavicmyths:cursed_volkolak`
- Жар-птица — `slavicmyths:firebird`
- Великий Полоз — `slavicmyths:great_poloz`
- Серый Волк — `slavicmyths:grey_wolf`
- Идолище Поганое — `slavicmyths:idolishche_poganoe`
- Кот Баюн — `slavicmyths:kot_bayun`
- Ским-зверь — `slavicmyths:skim_beast`
- Тугарин Змей — `slavicmyths:tugarin_zmey`
- Волколак — `slavicmyths:volkolak`
- Змея Горыныча — `slavicmyths:zmey_gorynych`

### SPIRIT — 20 самостоятельных ID
- Банник — `slavicmyths:bannik`
- Болотник — `slavicmyths:bolotnik`
- Осквернённая хранительница источника — `slavicmyths:defiled_spring_keeper`
- Домовой — `slavicmyths:domovoy`
- Дух земли — `slavicmyths:earth_spirit`
- Старший Водяной — `slavicmyths:elder_vodyanoy`
- Игоша — `slavicmyths:igosha`
- Карачун — `slavicmyths:karachun`
- Кикимора — `slavicmyths:kikimora`
- Домовёнок Кузя — `slavicmyths:kuzya`
- Леший — `slavicmyths:leshy`
- Лихо Одноглазое — `slavicmyths:likho_one_eyed`
- Морозко — `slavicmyths:morozko`
- Ночница — `slavicmyths:nochnitsa`
- Овинник — `slavicmyths:ovinnik`
- Подвей — `slavicmyths:podvey`
- Полевик — `slavicmyths:polevik`
- Полудница — `slavicmyths:poludnitsa`
- Русалка — `slavicmyths:rusalka`
- Водяной — `slavicmyths:vodyanoy`

### UNDEAD — 6 самостоятельных ID
- Погребённый волхв — `slavicmyths:buried_volkhv`
- Курганный дружинник — `slavicmyths:kurgan_druzhinnik`
- Курганный воевода — `slavicmyths:kurgan_voevoda`
- Навь — `slavicmyths:nav`
- Неупокоенный князь — `slavicmyths:unresting_prince`
- Упырь — `slavicmyths:upyr`

### ELEMENTAL — 2 самостоятельных ID
- Страж погасшего пламени — `slavicmyths:extinguished_flame_guardian`
- Огненный змей — `slavicmyths:fire_serpent`

### CONSTRUCT — 0 самостоятельных ID
- В основном инвентаре 69 видов представителей нет; класс сохраняется для ванили, миньонов и будущего контента.

### PLANT — 0 самостоятельных ID
- В основном инвентаре 69 видов представителей нет; класс сохраняется для ванили, миньонов и будущего контента.

**Проверка покрытия:** `HUMANOID 20 + ANIMAL 11 + MYTHICAL_BEAST 10 + SPIRIT 20 + UNDEAD 6 + ELEMENTAL 2 + CONSTRUCT 0 + PLANT 0 = 69`. **Не добавлять новые entity ID.** Категории `CONSTRUCT/PLANT` нужны для ванили/других вспомогательных существ и будущего роста. `HUMANOID` включает физически телесных разумных нечеловеческих персонажей (например, подземников/песиголовцев), а не только людей; `MYTHICAL_BEAST` охватывает телесных сверхъестественных существ, не обязательно четвероногих.

### Явные черты/оговорки к 69
- `ovinnik` = `SPIRIT + FIRE_AFFINITY` с существующим абсолютным native fire immune; `fire_serpent` = `ELEMENTAL + FIRE_AFFINITY` + native fire immune; `firebird` = `MYTHICAL_BEAST + FIRE_AFFINITY + STORY_PROTECTED` + native fire immune. Не нарушать несмертельный сюжетный сценарий Жар-птицы.
- `morozko`, `karachun` = `SPIRIT + ICE_AFFINITY + BOSS`; их ледяные состояния и особые защиты сохраняются, **никакой автоматической взаимоагрессии**. `defiled_spring_keeper`, `vodyanoy`, `elder_vodyanoy`, `rusalka`, `bolotnik` = `SPIRIT + WATER_AFFINITY`; `earth_spirit` = `SPIRIT + EARTH_AFFINITY` плюс уже существующий bleed immune. `extinguished_flame_guardian` = `ELEMENTAL + BOSS`, но не объявлять fire immune без проверки его реальных фаз и исходников.
- `grey_wolf` = `MYTHICAL_BEAST + STORY_PROTECTED`, динамический сценарий trial/после-trial; **не добавлять ему автоматических targetSelector для Мора**, не переделывать историю. `kot_bayun`, `baba_yaga`, `kuzya`, `domovoy`, `sivka_burka` — сюжетная/нейтральная защита согласно текущему состоянию; `STORY_PROTECTED` не делает всех NPC неуязвимыми к любой существующей разрешённой механике.
- `leshy` = `SPIRIT`, а не новый `PLANT` только из-за лесной тематики: дополнительная древесная уязвимость не внедряется без доказанной природы тела. Старый `silver_vulnerable` у Лешего сохраняется.
- `volkolak`, `cursed_volkolak` = `MYTHICAL_BEAST` + curse/story trait по действующим условиям, **не UNDEAD просто из-за слова “проклятый”**. `marinka`, `baba_yaga` = телесные `HUMANOID` + магическое/сюжетное состояние; изменения обликов/ложных тел не порождают новый EntityType. `idolishche_poganoe` = крупное телесное мифическое существо (не обычный человек); сохранить имеющиеся аренные исключения.
- Курганные `upyr/nav/kurgan_druzhinnik/kurgan_voevoda/buried_volkhv/unresting_prince` = `UNDEAD`, в том числе уже существующий `bleed_immune`; не считать любые физические удары неэффективными. `BOSS` назначается по **подтверждённому игровому режиму**, а не одному HP; у сюжетных боссов действуют encounter gates.
- Герои `ilya_muromets/dobrynya_nikitich/alyosha_popovich/finist` получили `HUMANOID` **лишь для физической уязвимости**; их target/восстановление/owner/боевые действия **не менять** — работа по богатырям отдельно.

## 4. Все вспомогательные типы — отдельно от 69
Аудит 42 вспомогательных EntityType **не означает 42 живых моба**. Обязателен дополнительный регистр: `aux ID | Entity extends LivingEntity? | owner/proxy | nature/trait | damageable? | allowed targets | lifetime/cleanup`. Примеры реальных видов, подлежащих именно разбору (список не исчерпывает оставшиеся props/projectiles):
- `earth_servant` = `CONSTRUCT + SUMMONED + EARTH_AFFINITY`, союзник своего Духа земли; проверить реальные слабости и отсутствие крови.
- `guardian_water_echo` = `ELEMENTAL + SUMMONED + WATER_AFFINITY` (временный боевой WaterEcho); принадлежность хранительнице.
- `morozko_ice_double` = `ELEMENTAL + SUMMONED + ICE_AFFINITY` (отдельный ID), не переносить механически иммунитеты босса на свою копию без проверки.
- `player_ice_double` = `ELEMENTAL + SUMMONED + ICE_AFFINITY`, поведение мимика ограничивается существующим владелец/мишень-контекстом, а не полной групповой враждой.
- `marinka_tur_minion` = `MYTHICAL_BEAST + SUMMONED`, союзность Маринке; является телесным туром, кровотечение не запрещать только за призыв.
- `karachun_raven` = `ANIMAL + SUMMONED`, **динамический** `allied()`/владелец (ворон Карачуна либо союзный игрок); не объявлять всех воронов враждебными.
- `grey_wolf_support` = `MYTHICAL_BEAST + SUMMONED + STORY_PROTECTED` в пределах существующих `WolfSupport` правил; отдельно от настоящего `grey_wolf`.
- `serpent_projection` = краткоживущая визуальная/тактическая проекция `PathfinderMob`: **специальный PROJECTION/ILLUSION, не обычный зверь**; нет самостоятельного обычного лута/охоты, проверить возможность урона и lifetime.
- Остальные `alyosha_arrow, doghead_arrow, finist_magic_arrow, mor_bolt, ember_clump, mor_zone, kladovik_spike, guardian_water_prison, rune_visual` и аналогичные — снаряды, зоны, визуальные props; у них **нет primary physiology**, но урон и ownership должны наследовать разрешения источника. Полный остаток добрать автоматически по source registry; нельзя забыть aux просто потому, что это не живой моб.

## 5. СТАТУС/ОТНОШЕНИЯ — одна система, разные действия
- `PEACEFUL`: `chud_resident`, `chud_artisan`, vanilla villagers/торговцы, обычная фауна в мирном контексте — торговля/avoid/flee, не охота. `NEUTRAL`: все обычные животные в непровоцированном состоянии; большая часть природных духов в состоянии calm, `baba_yaga`, `kot_bayun`, `domovoy`, `kuzya`, `sivka_burka`, `firebird` и другие квестовые сущности. `HOSTILE`: реальные бандиты/военная нежить/активные враги и **только активированные** боссы и моровые формы. `DEFENDER`: `chud_guard`, деревенская стража/голем и существующие NPC-охранники. **Не превращать обычных добрых NPC в обязательных бойцов.**
- `MOR`: те же registry ID `leshy/poludnitsa/kikimora/fire_serpent/likho_one_eyed/ovinnik`; `morState 0/1/2` — разные текущие отношения/эффекты, **не новые виды или primary nature**. При `morState=1` трансформация и заморозка target, `morState=2` враждебное заражённое поведение; очищение/нейтралитет возвращают допустимые цели. Нейтрального Лиха не атаковать из-за того, что ID присутствует в списке смертельно опасных боссов.
- `ALLOWED` проверяет attacker/target жизнь, измерение, PvP/team, Creative/Spectator, владелец/призыв, защищённость, серверный сценарий и конкретный damage source. `relation(A,B)` не симметрична по умолчанию: `A→B` и `B→A` могут различаться. Скрытые/очищенные/сдавшиеся/восстанавливающиеся боссы не должны внезапно становиться целью из-за `Monster`/тега.
- `HOSTILE` по игроку не равно «атаковать других HOSTILE». Бандиты сохраняют союзность отряда; курганная нежить не обязана воевать с волколаками; вражда между монстрами возникает **только** от подтверждённой фракционной логики/реталиации/сюжета. Мирные звери бегут, нейтральные отвечают на провокацию/защиту детёнышей; аренные боссы не покидают свою encounter зону.
- **Чудь — реальный подтверждённый дефект в коде:** `ChudPerson.targetSelector` сейчас использует `NearestAttackableTargetGoal<Monster>` и только 16 блоков от `home` при выборе, сбрасывает цель за 24 блока от `home`; из-за этого ряд враждебных, не наследующих `Monster`, теряется. Исправить на **общий предикат угрозы + защиту группы/поселения**, а не на `instanceof Monster`. Розыск ограничен локальной областью стража/поселения и LOS, возврат/время преследования ограничены, **но не только личной точкой дома**; нет нового «стражи летят через 200 блоков». Удар по Чуди вызывает помощь ближайших стражей; нейтральных и чужих игроков без вины не атаковать; торговля жителей сохраняется.
- Деревенские `GuardBehavior` и железные големы уже используют `village_hostiles/bandits` плюс свои расширения (в частности `BossGolemTargetMixin`); перевести на единый семантический предикат угрозы **без потери приоритетов, работы у профессии, щита/дальности, LOS, запрета friendly-fire и спецправил Тугарина**. `CreatureSafety.HOSTILES` (51 type) ≠ `village_hostiles` (17 mod entries): **не копировать все 51 в общих врагов**, потому что там встречаются нейтральные духи и фазовые боссы.
- Сохранить боевых призывателей `GreyPackRing`, `KalinParty`, `PolozMinions`, `NightRaven`, `GuardianEntity`, `HigherMor`, `NochnitsaHorde`, `EncounterMob`, а также теги защищённых NPC. AoE/projectiles/HurtBy/revenge/taunts используют те же relation/friendly-fire проверки там, где это совместимо с текущими сюжетными правилами.
- `PEACEFUL` difficulty: не возрождать hostile в обход `CreatureSafety`, не создавать глобальные target scans; не ломать существующее поведение `Firebird`, `GreyWolf`, очищение стражей и Горыныча, восстановление союзников и сюжетные диалоги.

## 6. ТЕХНИЧЕСКИЙ ПЛАН (без непрошенных рефакторингов)
1. **Inventory gate:** на фактическом HEAD собрать runtime/sources все `EntityType` от всех регистраторов (не только `ModEntities.java`); сопоставить 69 основных, 42 aux и все новые появившиеся после старого аудита. Не путать `MobCategory.MONSTER` и природную классификацию; проверить JSON tag resolution и vanilla fallback.
2. **Классы/traits:** добавить data-driven nature tags, адаптер `CreatureProperties` (или минимальный эквивалент без конкурирующей таблицы), методы `nature(entity)`, `traits(entity)`, `canBleed`, `isPhysiologicalPoisonTarget`, `fireMultiplier`. Особенности экземпляра (mor/owner/state) читать только динамически.
3. **Эффекты:** централизованно и один раз включить проверки в `CutEffect`, `RareBleedEffect`, `WolfBleeding`, остальные найденные bleed-процедуры, vanilla poison hook в узком контексте и fire damage handler. Не отменять `BAD_BLOOD` и магические/скриптовые эффекты. Совместимость с damage pipeline NeoForge 1.21.1 — подтвердить offline и в игре.
4. **Отношения:** расширить `CombatTargeting` для relation/target-policy, точечно подключить `ChudPerson`, `GuardBehavior`, golem, соответствующие caller/targetSelector и projectile/AoE; не менять всех мобов вообще ради абстракции. Призванные и сюжетные — явно сохранённые делегаты/предикаты.
5. **Документация:** сделать `docs/CREATURE_CLASSIFICATION.md` со всеми `ID | nature | default intent | traits | dynamic overrides | affected effects | source`; `docs/ENTITY_RELATIONS.md` со всеми семьями и `A→B/B→A` на representative cases. `UNVERIFIED` для неиспытанных игровых контактов, не замалчивать.
6. **Тесты/сдача:** 69/69 primary + все 42 aux с явным `living / proxy / nonliving`, vanilla zombie/skeleton/iron_golem/wolf/vex/blaze/undead/player; контроль неизвестного нового ID. Контракты повреждений (bleed/poison/fire), один множитель, актуальный иммунитет и лут; отношения/retaliation/LOS/team/owner, arena/pacified/cleansed, Мирная сложность, PvP/Creative, FPS/MSPT без тяжёлых poll-циклов. Java21 clean build, JAR/resource/JSON tests, изменённые файлы/SHA. **Нельзя объявлять визуальный/игровой PASS без запуска**. Ручная проверка `OWNER_PENDING` до владельца.

## 7. Обязательная матрица ручной приёмки
| Сценарий | Ожидаемый результат |
|---|---|
| Чудь-страж + vanilla zombie/skeleton + бандит + курганная нежить | Атакует доступную угрозу в зоне поселения, возвращается на пост, не бросает весь город/защиту |
| Чудь-страж + незаражённый нейтральный дух / очищенный хранитель / сюжетный NPC | Не начинает бой, не ломает диалог и нейтралитет |
| Нападение на жителя Чуди со стороны монстра / игрока | Локальная защита адресного виновника, без бесконечной агрессии ко всем игрокам |
| Деревенский страж и голем + реально враждебные vs neutral | Приоритет угроз, нет friendly-fire, сохраняется защита и особые правила vanilla |
| Кровотечение на зомби/курганную нежить/духа/огненного змея | Не накладывается, старые stack/tick не вредят; прямой удар остаётся |
| Кровотечение на человека/зверя/мифического зверя без `BLOODLESS` | Имеющийся эффект наносит урон по прежним лимитам |
| Яд на духа, нежить, живого человека; проклятие/Мор на духа | Биологический яд блокирован по типу; сюжетные эффекты/Мор не блокируются автоматически |
| Огненный урон на нейтрального духа, Морозко, огненного змея, Жар-птицу | Множитель 1.25/1.50 или существующий абсолютный native immunity по таблице; строго один раз |
| Высшее Мор-Лихо, обычное Лихо, очищенные стражи | Правильные динамические отношения при том же EntityType; без спонтанного боя Серого Волка |
| Серый Волк в/после испытания, Жар-птица, Сивка-Бурка, Яга, Баюн | Нет регрессии истории, непрошенного targetSelector, порчи мирных состояний |
| Призванные вороны/волки/двойники/миньоны | Союзность, owner, lifetime, despawn, отсутствие своих союзных попаданий |
| Сохранение, повторный вход, multiplayer/лаг | Нет потери состояния/изменения NBT и дорогостоящих глобальных сканов |

**Приоритет решения:** безопасность/сюжет + доказанные runtime исключения → физиологическая классификация → динамическая relation → локальный AI. Нет магической “все враждебные воюют друг с другом”. **Исполнение №79 только по отдельному поручению**, после согласования текущих активных пакетов; постановка не равна выполненной задаче.

