# Программа #87 — накопительный рабочий журнал одной ветки

Ветка: `work/weapons87-continuous-20261022`.
Правило владельца: **не сливать эту ветку в main до явной команды**, все последующие пакеты добавлять в эту же ветку. Не закрывать Issues без фактической игровой приёмки.

## P42 — задачи #175, #176, #177, #179, #186

### Baseline и проект изменений (до production)

| Issue | Измеренный baseline main | Пакет P42 |
|---|---|---|
| #175 `three_breath_crossbow` | 65 реальных cuboids у пустой модели, уже есть три головы/глаза/пасти, 68 у arrow; шесть состояний с одинаковыми held transforms | **KEEP головы, FIX отделку**: три небольших отдельных мотива/щитка на верхней части ложа и единое улучшенное чтение GUI во всех шести состояниях. Ни пасти, ни три полётных линии не изменять |
| #176 `serpent_crossbow` | Всего 8 cuboids в базовой модели; шестёрка изометрических states, существующий складной ложевой grip | **FIX силуэт**: две короткие костяные дуги по бокам и парный хребет/крепления ложа, максимум 4–6 кубов, без изменения заряженных bolts и состояний |
| #177 `karachun_scythe` | `builtin/entity`, 60 детальных элементов в `_held`; длинный клинок X[-9.14,9.31], Y[-3.5,31.375], сохранённая собственная модель | **KEEP геометрию, FIX TP масштаб** до 0.70 вместо 0.75 для Steve/Alex и обеих рук; FP и иконка не трогать |
| #179 `morozko_staff` | `builtin/entity`, 66 cuboids в `_held`, Y[-4,27], TP scale 0.8 | **KEEP вершину/орнамент, FIX масштаб** TP 0.74 для обоих рук; добавлять чрезмерную массу на верх не нужно, original textures/UV без правок |
| #186 `finist_bow` | `minecraft:item/bow`, четыре состояния idle + 3 pulling, собственные PNG, штатные predicates | **FIX единый display** по 4 стадиям, собственная умеренная индивидуальная масштабность, без вмешательства в server bow power / enchanted arrows / quiver |

### Границы и проверка

Только `assets/slavicmyths/models/item/*.json`, точечная автоматизация, Java21 и source/JAR guards. Анимации, урон, loot, registries, bosses, recipe, другие ветки проекта не затрагиваются. `Minecraft runtime`: NOT_RUN. Результаты каждого следующего пакета добавлять ниже.

## P42 — реализовано в исходниках (после базового чертежа)

- **#175 / `three_breath_crossbow`:** три небольших цветовых мотива (fire/mor/storm) у трёх голов — +3 блока на каждый из `idle, pulling_0/1/2, arrow, firework`, без изменения существующих 65/68 деталей голов, болтов и ствольных линий.
- **#176 / `serpent_crossbow`:** выраженные парные накладки на концах плеч, две нижние защитные пластины и центральный гребень ложа — +5 блоков во всех шести состояниях. Без изменения серверного арбалета.
- **#177 / `karachun_scythe_held`:** TP .75→.70 в обеих руках, пересчитана точка привязки ладони относительно Y=12: translation (0,−0.9576564,−2.63113934). Все 60 геометрических блоков, UV, GUI и FP пути оставлены без изменений. Изменено SPECS `tools/weapons87_grips.py` для повторяемости.
- **#179 / `morozko_staff_held`:** TP .80→.74 в обеих руках, центрированная ладонь при Y=10: translation (0,−0.84889313,−1.21234503). 66 геометрических блоков, texture atlas, GUI и FP без изменений. SPECS обновлён.
- **#186 / `finist_bow`:** отдельный held-масштаб FP .72 / TP .92 вместо общего bow-default. Матрицы одинаковы на четырёх моделях `idle + pulling_0..2`, в обеих руках. Родитель minecraft:item/bow, оригинальные PNG, три native predicates и зарядка не изменены; колчан не менялся.
- **Воспроизводимость:** добавлен `tools/weapons87_p42_models.py` (безопасное идемпотентное дополнение 12 crossbow + 4 Finist JSON), подключён после crossbow-grip generator в `tools/weapons87_flails.py`. При незнакомой исходной геометрии — assert вместо перезаписи.
- **QA:** `tools/verify_weapons87_p42.py` проверяет 5 ID, все 18 моделей, симметрию физического хвата, пивоты, все 4 финальных bow-state, реальные байты ресурсов в JAR и двойной запуск overlay **в изолированном временном каталоге**. CI: `weapons87-continuous-java21.yml`, Java21 clean build. Исходные изменения не означают game acceptance.

**Текущая политика:** эта ветка продолжает накапливать следующие пакеты. В `main` ничего не переносить до отдельного поручения владельца, этот журнал не удалять до завершения всей разработки. Issues оставить OPEN, Minecraft/PolyMC NOT_RUN.


## P43 — план до правок: #188 / #174 / #117 / #168 / #121

Исходники в той же ветке после P42. Реальные baseline:
- `thunder_spear.json` 30 cuboids, стержень x[7.35,8.65] y[-10,21], колющий наконечник y[20,30], видимый односторонний малый орнамент на z[7.3,7.6]. Сохранить арт/клинок, добавить малые крепления втулки и второй малый орнамент на обратной стороне, не превращать копьё в топор.
- `berdysh.json` 21 cuboids, лезвие наклонно уходит к x1.5, y18–29; удерживать принятый топороподобный боковой силуэт. Добавить связку к древку, не удлинять головку.
- `carved_staff.json` 11 cuboids, цельная ветвистая деревянная вершина y22–30, добавить две малые резные стяжки под вершиной; уменьшить масштаб только в третьем лице с пересчётом grip pivot y10, оставить FP и GUI.
- `veles_staff.json` 15 cuboids, собственный деревянный/костяной мотив, TP currently translation [0,0,0] при grip y8. Проверить геометрию руки и, только при доказанном математическом смещении, центрировать отображение без изменения масштаба.
- #168: комплектующие копья — видимые втулки и обоймы в моделях, **не** перерисовывать 14 наконечников #99/#100 или отдельные чужие PNG.
- #121: канонический overlay и регрессия в изолированном временном каталоге, неизменность 68 registry IDs и ресурсов P42, Java21/JAR; historical generator rerun без разрешения не выполнять, особенно если возможна переработка аудио.

Это частичное техническое выполнение соответствующих Issues. Приёмка Minecraft NOT_RUN. Сохранять отдельные реальные before/after SHA и не сливать branch в main.


## P43 — реализовано в той же ветке · #188 / #174 / #117 / #168 / #121

- **#188 — громовое копьё (`thunder_spear`)**: 30→37 элементов. Два настоящих кольца крепления острия и пять небольших пиксельных сегментов грозового знака на обратной стороне. Исходное узкое колющее остриё и вся старая геометрия сохранены. Никаких игровых бонусов/молний в combat коде.
- **#174 — бердыш и резной посох**: `berdysh` 21→23 блока — два малых крепления тяжёлого лезвия к древку; `carved_staff` 11→14 — три скромных обруча под ветвистой деревянной вершиной. Полуденный серп/посох бури/посох Велеса в этой партии не менялись.
- **#117 — стойка длинного оружия**: резной посох в 3P уменьшен с 0.75 до 0.68 для обеих рук; точный palm-pivot при grip Y10/угле55: `[0,-0.78006395,-1.11404678]`. First-person, GUI и положение boss rigs не менялись. Это направленное уменьшение возможного клиппинга, **не доказательство отсутствия клиппинга в игре**.
- **#168 — крепления готового оружия**: добавлены физические кольца втулки `thunder_spear` и обоймы бердыша; никакие наконечники `#99/#100`, их текстуры/ID и рецепты не изменены. Остальные семьи комплектующих остаются отдельной работой.
- **#121 — сохранность генерации**: `tools/weapons87_p43_overlay.py` фиксирует ровно 7/2/3 добавленных геометрических элемента и не допускает новых произвольных изменений в этих моделях. Канонический `tools/weapons87_flails.py` теперь применяет P43 **после P42**, а `tools/weapons87_grips.py` воспроизводит компактный 3P хват carved_staff. `tools/verify_weapons87_p43.py` проверяет фактические модели, артикуляции, P42-обратную совместимость, двойной идемпотентный прогон в изолированном каталоге и байты файлов в JAR. Полный replay всех исторических генераторов ещё НЕ проведён.
- **CI:** `.github/workflows/weapons87-continuous-java21.yml` дополнен P43 source и JAR gates. Результат сборки вписать после GitHub Actions.
- **Runtime:** Minecraft/PolyMC/Steve/Alex/сетевая игра NOT_RUN. Все пять Issues остаются OPEN; не закрывать без владельца.

### Продолжение работы следующему чату

Постоянная ветка `work/weapons87-continuous-20261022`, draft PR #216. Никаких merge/reset/force push в `main` до явной команды владельца. Сначала прочитать этот файл, `AGENTS.md`, `docs/PROJECT_WORKFLOW.md`, проверить актуальные GitHub SHA и последний CI. Затем брать **следующие 4–5 новых, ранее не отработанных** оружейных задач #87, вносить реальные scoped изменения с source evidence; не объявлять отдельные исходники полной игровой приёмкой; дополнять этот же журнал, не создавать новую ветку/PR. Известный нерешённый дизайн-гейт #190 — без owner-approved contact-frame/timing не менять server hit.


## P44 — функциональные клиентские фазы и арбалет · #162 / #165 / #163 / #160 / #161

**Baseline:** после P43 `bf077302b28250d28044d9b66daa0b8a42ab0eca`. До изменений 32 перечисленных обычных item ID использовали базовую vanilla 1P-анимацию; у `WeaponHands.crossbowWrist` при зарядке был лишь слабый сдвиг (без изменения pitch/roll); `SerpentCrossbow.fireRound` проигрывал `crossbow_special_*` **трижды** за трёхснарядный fan/burst; `CrossbowDamage.hit` записывал UUID, но не отказывал в повторном debuff-proc того же снаряда. Динамику восьми кистеней #164, луки P10/#163, TP косы Карачуна, статические модели P42/P43 не переписывать.

### Реальная реализация (НЕ полный scope этих больших Issues)

- **#162 — ближний бой:** `src/main/java/org/slavicmyths/client/WeaponMotionClient.java`, первый клиентский phase-aware renderer через штатное `IClientItemExtensions.applyForgeHandTransform` для **15 обычных ID**: 6 клинков, 9 булав/чеканов/дубин/топор. Плавные `idle → windup → impact → recovery`, различие резкого разреза и тяжёлого удара, мягкая реакция на ходьбу, offhand и зеркальная рука. Vanilla TP attacks оставлены штатными, **полный специализированный 3P-анимационный пакет и внутриигровая сцена НЕ исполнены**.
- **#165 — древковое:** тот же локальный клиентский класс содержит отдельные `SPEAR` (12 ID, выдвижение вперёд), `POLEAXE` (бердыш), `SICKLE` (полуденный серп), `STAFF` (резной/Велеса/бури; у storm_staff — короткая видимая отдача от уже существующего cooldown). Всего **17 ID**, без per-tick world scans. Не трогать существующие режиссуру Карачуна, модели и boss rigs; TP и специализированные cast-позы ещё OWNER_PENDING/IN_PROGRESS.
- **#163 — арбалеты:** в `WeaponHands.crossbowWrist` зарядка наклоняет кисть и ложе в X/Z пропорционально progress; штатные динамические model predicates и уже реализованный charge→shot kick сохранены. Без изменений charge-duration, burst scheduling, урона/патронов и состояния mode. Игра/мультиплеер не проверялись.
- **#160 — защита server proc:** `CrossbowDamage.hit` теперь прекращает повторное наложение слабости/горения, если UUID одной и той же стрелы уже был обработан для target+volley. Три **разные** стрелы, существующие лимиты и damage window намеренно не перебалансированы. Другие способности 68 ID остаются вне этого точечного исправления.
- **#161 — привязанный к боевому событию VFX:** `SerpentCrossbow.fireRound` отправляет **ровно один** короткий muzzle cue (6 vanilla particles: SOUL/FLAME/ELECTRIC_SPARK) в зависимости от mode при lane==0, вместе с **одним** `crossbow_special_*` sound на volley. Сохраняются три боевых projectile, burst/fan и индивидуальные impact effects; массовый performance/runtime тест не выполнялся.

### Матрица gate до игры

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| 32 обычных ID без вмешательства в special renderers | `WeaponMotionClient.java`; `creative_catalog.tsv` | SOURCE IMPLEMENTED / Minecraft NOT_RUN |
| Цикл windup/contact/recover и mirror main/offhand | `WeaponMotionClient.java`, `tools/verify_weapons87_p44.py` | SOURCE / AUTOMATED GUARD; 3P owner test NOT_RUN |
| Crossbow charge pose и модель P42/P43 KEEP | `WeaponHands.java`; P42/P43 guards | SOURCE / TEST PENDING |
| Один cue на volley и mode-specific muzzle | `SerpentCrossbow.java`, `tools/verify_weapons87_p44.py` | SOURCE / TEST PENDING |
| Только однократный proc на projectile UUID | `CrossbowDamage.java` | SOURCE / TEST PENDING |
| Java21 clean build, production JAR SHA | `.github/workflows/weapons87-continuous-java21.yml` + Actions | CI PENDING |
| 1P/3P/Alex/Steve/PvP/MP/fps/screenshots | Real Minecraft client/server | OWNER_PENDING / NOT_RUN |
| #190 гибкая плеть и timing | Без правок | NOT_RUN / OWNER APPROVAL REQUIRED |

**Code evidence:** original Git blobs `WeaponHands:39567b95`, `SerpentCrossbow:16492964`, `CrossbowDamage:81538c7a`; after blob `3cdd382b`, `2ff1e0a0`, `a661e980` соответственно; новый `WeaponMotionClient` `3b9fa6a9`. `tools/verify_weapons87_p44.py` выполняет source guards до clean build и class-package guards внутри JAR после него; Java21 checked exclusively through CI, not a claim of game acceptance.

**Не изменено:** registry IDs, damage, attack speed, hit range, server hit timing, hitboxes, recipes, drops, item NBT formats, bosses, flails, #190, sound assets, particles outside this specific crossbow event, другая ветка, `main`.

**Продолжение:** Issues остаются OPEN; P44 есть частичный production-срез пяти больших задач. После CI отдельно проверить правильную установку визуальных фаз в реальном Minecraft: обе руки, модели Alex/Steve, idle/walk/sprint/attack, длительность заряда, fan/burst, три режима, выделенный сервер и fps. Технический gate не равен игровой приёмке владельца.


### P44 — подтверждение CI после source/JAR публикации

- Исходный коммит: [`bbb3a353a9d393c3ce2e0222f29701cba0c02774`](https://github.com/Deserag/slavic-myths/commit/bbb3a353a9d393c3ce2e0222f29701cba0c02774).
- [GitHub Actions #39812159700](https://github.com/Deserag/slavic-myths/actions/runs/39812159700): **SUCCESS**, Java 21 `./gradlew clean build` **BUILD SUCCESSFUL**, прежние P42/P43 + P44 source gates PASS, P44 JAR-class checks PASS, full module tests в clean build PASS.
- Production JAR `build/libs/slavicmyths-1.9.7.56.jar`; **SHA256 `ed564259729eda1027a7e5c3331797a75593981eb28635a0787677163b148eee`** (строка опубликована в job logs, не SHA ZIP-артефакта).
- Реальных запусков Minecraft: **0**. PolyMC: **NOT_INSTALLED**. Результат — техническая сборка и исходный код, не visual/MP/interactive proof.
- Все #162/#165/#163/#160/#161 сохраняют статус **IN_PROGRESS, OPEN**, а не `Готово`: 3P, игровой storyboard и ручная проверка ещё не пройдены.


## P45 — продолжение анимационной ветки: 7 ножей и 7 сулиц · #162 / #165

**Реальный baseline** `ThrowingWeaponItem.java`: все 14 ID из `creative_catalog.tsv` используют `UseAnim.SPEAR`, но у мгновенно выбрасываемых ножей `use()` вообще не вызывал `player.swing`; у сулиц есть `startUsingItem` и release после 6 тиков. `WeaponProjectileRenderer` уже использует правильную полётную ориентацию из P6/#173 — её НЕ менять.

- `client/ThrowingMotionClient.java` регистрирует отдельные first-person transforms для 7 `*_throwing_knife` (действительная короткая дуга throwing swing) и 7 `*_sulitsa` (плавная подготовка/вынос перед throw по фактически удерживаемым тикам, завершение через swing). Учитывает реальную руку, main/offhand; без глобальных клиентских тиков.
- `combat/ThrowingWeaponItem.java` вызывает `player.swing(hand)` **только на клиенте** при валидном вызове броска ножа; для сулицы только после разрешённого удержания `held>=6`. Серверные `throwOne`, создание `WeaponProjectile`, нанесение урона, материальные статы, `KNIFE_LOSS_CHANCE`, скорость, расход и cooldown остаются прежними.
- **Ограничение:** при расходе последнего ножа его item stack исчезает; свинг руки может продолжаться, но предмет уже не рисуется. Реальные FP/TP/сетевая приёмка отсутствует. 3P сулицы продолжает штатный vanilla `UseAnim.SPEAR`; индивидуальное управление локтями только 1P.
- `tools/verify_weapons87_p45.py` проверяет 14 фактических ID, client-only вызов, сохранение server projectile и JAR class packaging. Continuous Java21 workflow расширен P45 source/JAR guards.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| 7 knife throwing animation | `ThrowingMotionClient`, client `swing(hand)` | SOURCE IMPLEMENTED, GAME NOT_RUN |
| 7 sulitsa charge/release animation | `ThrowingMotionClient`, `releaseUsing` | SOURCE IMPLEMENTED, GAME NOT_RUN |
| Сохранять server damage/range/velocity/cooldown | Exact `ThrowingWeaponItem.throwOne` и source guard | SOURCE PASS, GAME NOT_RUN |
| Source/JAR / Java21 clean build | P45 workflow | CI PENDING |
| Multiplayer/Steve/Alex, обе руки и actual 3P view | Minecraft/PolyMC | NOT_RUN / OWNER_PENDING |

**Запреты сохранены:** #190 не изменялась, баланса и серверного hit timing нет, существующие 14 flight models/PNG, texture генераторы и #173 не переписаны; Issues OPEN.


### P45 — подтверждённый CI / Java21 production JAR

- [GitHub Actions #39813871455](https://github.com/Deserag/slavic-myths/actions/runs/39813871455), коммит `b96024c4112b1b621908c49619113d6c583c2b9b`: **SUCCESS**; `./gradlew clean build` Java 21 SUCCESS; P42/P43/P44/P45 проверка исходников и packaged JAR PASS.
- Production `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 `75aede07fc2240f23cb1fdb6ead673c565fcbd8179586e2c02d663bba68f32e9`.
- Runtime Minecraft/PolyMC/remote multiplayer: **0 / NOT_RUN**. #162/#165 остаются OPEN/IN_PROGRESS; в 3P и в механике выбрасывания предмета при last-use ещё необходима игровая проверка.

## P46 — #121 системный regression gate исторического арбалета без отката P42

- **Подтверждённый дефект QA (а не дефект исходной модели):** отдельные старые GitHub Actions `Weapons87 Crossbow Pull`, `P36 Crossbow Held Stack`, `P38 Combat Visuals`, `Kalin wave` завершаются FAIL ещё на проверке исходников. `tools/verify_weapons87_crossbows.py` сравнивает все элементы 12 моделей с архивом **до** P42, а `tools/weapons87_owner_qa.py` сравнивает текущее количество элементов с замороженным счётчиком до P42. Поэтому **одобренные кодом P42 +5 физических деталей `serpent_crossbow` и +3 `three_breath_crossbow` вызывали ложное FAIL**, несмотря на успешные Java21/JAR continuous checks P42/P43/P44/P45. Сам архив baseline не изменялся.
- **Исправление защиты, не отключение тестов:** `tools/verify_weapons87_crossbows.py` теперь требует **строго** `final.elements == frozen_original.elements + P42.ADDITIONS[id]`, при проверке остальных frozen metadata исключает только этот exact suffix. `tools/weapons87_owner_qa.py` ожидает `oldCount + len(P42.ADDITIONS[id])` и проверяет exact одобренный suffix; у TP-поворотов/центровки ладони и всех остальных проверок остаётся прежний строгий gate. Ранее существовавшие model geometry и PNG/UV/TP не изменены; owner feedback 3P/Steve/Alex остаётся NOT_RUN.
- **Изменено:** только 2 Python-верификатора. Ни модель, ни исходная анимация, ни hit logic, ни `main` не менялись. Code/CI verification: PENDING до запуска GitHub Actions на том же PR #216.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Frozen baseline cuboids сохранены по 12 states | `verify_weapons87_crossbows.py` exact prefix | CODE IMPLEMENTED / CI PENDING |
| Разрешены строго дополнительные кубы P42 | `weapons87_p42_models.ADDITIONS`, 2 verifier files | CODE IMPLEMENTED / CI PENDING |
| TP grip + FP/GUI/metadata KEEP | Исходные asserts `verify_weapons87_crossbows`, `weapons87_owner_qa` | CI PENDING |
| Полный Minecraft/PolyMC runtime | Owner | NOT_RUN / OWNER_PENDING |


### P46 — финальное подтверждение CI

- На source `549e39bfe7fd5078f594a65692401cb9f9f0fc2d` исправленные QA регрессии прошли: [Continuous #39814790628](https://github.com/Deserag/slavic-myths/actions/runs/39814790628) **SUCCESS**; [Crossbow Pull #39814790623](https://github.com/Deserag/slavic-myths/actions/runs/39814790623), [P36 Held #39814790616](https://github.com/Deserag/slavic-myths/actions/runs/39814790616), [P38 Visuals #39814790592](https://github.com/Deserag/slavic-myths/actions/runs/39814790592), [Kalin #39814790671](https://github.com/Deserag/slavic-myths/actions/runs/39814790671) — **SUCCESS**.
- P46 устраняет legacy source false negative; gameplay QA по-прежнему OWNER_PENDING, Minecraft NOT_RUN.

## P47 — third-person анимации 48 оружий · #162 / #165 / #163

### Проблема и scope
P44/P45 добавили клиентские **first-person** фазы 32 melee/polearm ID, 14 метательных ID и расширение зарядки арбалетов. В третьем лице их штатный item model `display` оставался статичным: другие игроки/камера сзади не видели этих особенностей. Вступая в P47, ничего из P42–P46 не переписываем.

### Production изменение
- `src/main/java/org/slavicmyths/client/ThirdPersonWeaponMotion.java`: регистрация `ModelEvent.ModifyBakingResult` для **48 конкретных existing registry IDs** (15 коротких blade/heavy, 17 spear/poleaxe/sickle/staff, 14 thrown и 2 crossbow).
- Стандартный `BakedModelWrapper` / `ItemOverrides` привязывает `LivingEntity` и `ItemStack` только к конкретному render resolve. После штатного `applyTransform` динамика включается **только для `THIRD_PERSON_LEFT_HAND` или `THIRD_PERSON_RIGHT_HAND`**, только при действительном наличии того же предмета в той же руке. GUI/ground/fixed/first-person **не изменены**.
- Никаких отдельных глобальных player/world tick listeners: существующий `owner.getAttackAnim(partial)`, `walkAnimation.speed(partial)`, `getTicksUsingItem` формируют ограниченные *player-only* transform curves, зеркальные по руке. Колющие копья сдвигаются по оси вперёд, тяжёлые булавы делают верхнюю дугу, клинки — боковую, серп — короткую, нож — бросковую. Зарядка арбалета наклоняет ложе по действительному прогрессу и использует прежние 6 item-model states.
- Сохранены все оригинальные `ItemInHandLayer`, native `HumanoidModel`/локти, player rigs, оригинальные model JSON/PNG/UV, registry, damage, reach, attack timing, cooldown, audio, VFX, packet protocols. Это **оживление item в third-person**, не замена полноценного движения суставов при атаке. Отдельные индивидуальные player rigs и игровых ракурсы ещё требуются по #162/#165.
- Исключены ранее реализованные особые renderer: Karachun, Morozko, Likho, все flails, Poloz whip, Finist bow и боссы. Не менять #190.

### Проверки и ограничения
- `tools/verify_weapons87_p47.py`: 48 ID в каталоге, 9 shape-групп без повторов, наличие настоящих item models, отдельное поведение charge/swing/walk, правильный контекст и привязка руки, отсутствие server-side/world tick, исходные envelope-фазы, требование наличия Java class в production JAR.
- CI: `.github/workflows/weapons87-continuous-java21.yml` запускает source и JAR tests P47 поверх P42–P45 и Java21 clean build; результат фиксируется после CI.
- **Minecraft/PolyMC/Steve/Alex/remote multiplayer: NOT_RUN**. Размеры моделей и совместимость с ресурс-паками нужно проверить в реальном клиенте владельца.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Реальные динамические 3P движения для 48 IDs | `ThirdPersonWeaponMotion.java` | CODE IMPLEMENTED; CI PENDING |
| 1P/GUI/ground/fixed models KEEP | Контекст `THIRD_PERSON_*` после native transform | SOURCE GUARD; RUNTIME NOT_RUN |
| Раздельные руки, swing/walk/charge | owner/ItemStack binding, `getAttackAnim`, `getTicksUsingItem` | SOURCE GUARD; RUNTIME NOT_RUN |
| Независимость server gameplay, #190 и босса | Только CLIENT `ModelEvent`, без common hooks | SOURCE GUARD |
| Java21 build/JAR class packaging | `verify_weapons87_p47.py`, CI workflow | PENDING |
| Фактические Steve/Alex 3P, 1P, MP и клиппинг | Игровой клиент владельца | NOT_RUN / OWNER_PENDING |

**Issues #162/#165/#163 остаются OPEN, IN_PROGRESS** — это техническое улучшение 3P предметов, не full-scope player skeletal animation и не доказательство игрового результата. Для Project Status `Тестирование` необходимы все исполнительские gates отдельной задачи и фактический доступ к Project field: не подменять его ярлыками или комментариями.


### P47 — CI receipt / исходники и собранный JAR

- Исходный коммит [`97bf25c3408320ea43678332dda84d280d6f7932`](https://github.com/Deserag/slavic-myths/commit/97bf25c3408320ea43678332dda84d280d6f7932), только `work/weapons87-continuous-20261022`, существующий draft PR #216.
- [GitHub Actions #39816854559](https://github.com/Deserag/slavic-myths/actions/runs/39816854559): **SUCCESS**, Temurin Java21 `./gradlew clean build` **BUILD SUCCESSFUL**, P42/P43/P44/P45/P47 source gates PASS и проверка упакованного P47 class в production JAR PASS. Это offline build, не клиентский запуск.
- JAR `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 **`acf4d4d5ce8cc30dc17eaf3a38c60b29d74484e463728429edf1c05b89187fa6`** (GitHub Actions job SHA строки).
- Minecraft/PolyMC/server запуска: **0**, установка: **НЕ ВЫПОЛНЕНА**, скриншоты реальных 3P поз: NOT_RUN. Технический модуль подключен и компилируется, но реальный визуал Alex/Steve/offhand/MP не доказан.
- `#162/#165/#163` как большие Issues **OPEN / PARTIAL IN_PROGRESS**, исполнительский scope включает не только предметный 3P transform, но и third-person player arms/pose сцены и игровую проверку. В `Тестирование` по `docs/PROJECT_WORKFLOW.md` переводить только когда не остаётся исполнительских FAIL/BLOCKED/NOT_RUN, а доступ к Project field подтверждён. Доступ через подключённый GitHub action к полю Status проекта №9 пока отсутствует; комментарий к Issue не является сменой поля на доске.


## P48 — отдельный 3P draw для короткого, тяжёлого и лука Финиста · #163

**Реальный baseline:** в P10 `BowDrawClient` уже применил правильный отдельный **first-person** BOW pose для `short_bow`, `heavy_bow`, `finist_bow`; `FieldBowItem.drawTicks()` возвращает 12/30, а `FinistBow.power()` использует 30. До P48 имелись 4 разные item модельные стадии `idle/pulling_0/1/2`, но P47 third-person bound transform пока был только для арбалетов, не для этих трёх луков.

- `ThirdPersonWeaponMotion.java`: P48 расширяет предыдущие **48→51** registry IDs, новая `Shape.BOW` для 3 реальных ID. При фактическом `owner.isUsingItem`, том же `usedItemHand` и действительном `ItemStack` 3P item получает малое плавное движение/наклон к лицу пропорционально **12 тикам для short, 30 heavy и 30 Finist**. Модель, которая уже выбрана штатным `pull` property, не подменяется; её 4 состояния/JSON/PNG не меняются.
- Рендер только `THIRD_PERSON_LEFT_HAND`/`RIGHT_HAND`, общее зеркало по физической руке, прежние `ItemInHandLayer`/положение плеч и других PlayerModels не меняются. В 1P продолжает работать предыдущий `BowDrawClient`; `FinistQuiver`, стрельба/урон/скорость стрел, перезарядка и RPG эффекты untouched.
- `tools/verify_weapons87_p47.py` обновлён для 51, `tools/verify_weapons87_p48.py` проверяет длительности 12/30/30, модельные states, прямую ручную/контекстную привязку и byte-exact JAR ресурсы. Continuous workflow включает оба gate до/после Java21 clean build.
- **Запуск клиента/мультиплеера: NOT_RUN**, требуются owner checks Steve/Alex, оба hand slots, 1P vs 3P at 0/12/30 ticks и стрельба колчана Финиста. Issue #163 остаётся OPEN/IN_PROGRESS, не `Готово`. Проверка GitHub Actions нового commit — PENDING.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Short bow 3P 12 ticks | FieldBowItem + Shape.BOW + P48 QA | SOURCE IMPLEMENTED / CI PENDING |
| Heavy bow 3P 30 ticks | FieldBowItem + Shape.BOW + P48 QA | SOURCE IMPLEMENTED / CI PENDING |
| Finist 3P 30 ticks without quiver change | FinistBow + Shape.BOW + P48 QA | SOURCE IMPLEMENTED / CI PENDING |
| 12 native draw models/JSON/JAR unchanged | P48 source/JAR checks | PENDING |
| Real 3P Steve/Alex, MP, both hands | Minecraft owner testing | NOT_RUN / OWNER_PENDING |

### P48 — устранение первого CI false-negative

- Первый Java21 workflow для `ab7bd400` остановился на **source gate**, ещё до Gradle: `verify_weapons87_p47.py` правильно обнаружил, что прежний список специальных исключений всё ещё содержал `finist_bow` после его осознанного включения в `Shape.BOW`. Это **тестовый контракт, не ошибка упаковки модели**, но компиляция тогда не выполнялась.
- Исправлено только `verify_weapons87_p47.py`: из прежних excluded IDs исключён `finist_bow`, остаются все реальные independent renderer `karachun_scythe/poloz_whip/stone_flail/morozko_staff/likho_staff`. Требование **51 уникальный ID** и обязательная тройка луков сохранены. Второй CI запуск проверит и source, и JAR.


### P48 — успешный финальный CI после актуализации regression gate

- Основная реализация [`ab7bd400887f8930abbb6b31add384d188424caf`](https://github.com/Deserag/slavic-myths/commit/ab7bd400887f8930abbb6b31add384d188424caf), test fix [`4fd2c6658a5a157e00dae5716cb92e217893ee9e`](https://github.com/Deserag/slavic-myths/commit/4fd2c6658a5a157e00dae5716cb92e217893ee9e), одна постоянная ветка PR #216.
- [GitHub Actions #39818424258](https://github.com/Deserag/slavic-myths/actions/runs/39818424258): **SUCCESS**. P42–P45/P47/P48 source gates PASS (51 ID, три 3P bow, 12 model states), `./gradlew --no-daemon clean build` на Java21 **BUILD SUCCESSFUL**, production JAR tests P47/P48 **PASS**.
- JAR `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 **`15ba5e2185f6f14e7865278dafc324b0a62546cfe6d557e580531b70d34336d5`**. Это SHA нового jar из CI после P48, а не старого P47/архива workflow.
- Minecraft/PolyMC/MP/runtime: **NOT_RUN**; `#163` остаётся OPEN/IN_PROGRESS, никакой owner acceptance и не Project Status `Готово`. Third-person held rotation ≠ полная skeletal-анимация. Состояние `Тестирование` выставлять лишь по подтверждённому полному scope и при реальном доступе к Project field.
