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


## P49 — первая партия замечаний владельца, 22.10.2026

**Вход:** игровой тест владельца отверг автоприцел Финиста, каролингские мечи в руке, кистени (шар не достигает цели), мерцание наконечника громового копья, копьё победителя с «Второй выпад готов», новый дизайн/хват плети Полоза, слишком длинный CD грозового посоха, охотничий рог и недостаточные движения/партикулы голов. Арбалеты, коса Карачуна, посох Морозко и всё отдельно не перечисленное владельцем **принято по его отзыву**; это не повод перебирать принятые assets. В этой партии `#190` не меняется как механика: прежний оригинальный item model Полоза восстанавливается по прямому требованию владельца.

### Реальные исходники и scope

- **#115 / #162, каролингские мечи:** два существующих 3D `ancient_carolingian_sword_held` и `restored_carolingian_sword_held` имеют идентичную теперь группу четырёх `display` transforms, выверенную от обычного `silver_sword`: main/off-hand зеркальны, клинок вертикален, хват поднят с исходной точки y=-8..1 на y=7..7.5. 10 исходных cuboids, UV и текстуры обоих сохранены. Исключена порочная индивидуальная рука `[75,0,0]` и z=6.28.
- **#180, плеть Полоза:** главная модель `poloz_whip.json` восстановлена **байт-в-байт из известного исторического commit `3f114c32`**, когда дизайн ещё не был подменён объёмной P35-версией; native sprite `poloz_whip.png` и пять **тоже восстановленных из `3f114c32`** `poloz_whip_pose_0..4` (по 36 исходных элементов в каждой) через прежний `slavicmyths:whip_pose`. Таким образом rollback включает не только root, но и все пять реально выбираемых stage-моделей. Никаких изменений `PolozWhip.java`, урона, cooldown/дальности, #190 и файлов исходной плети. Owner должен проверить оригинальный хват у основания. Если source stage P39 запрещает историческую модель — обновить контракт только по точной owner rollback, не выключать защиту других видов.
- **#164, кистени:** `FlailMotion` вместо наклона на месте даёт физически связанной цепи до 7 звеньев **контактный вынос 7×(2.4+5×contact) модельных единиц**; контактная ось до 88°, idle length и 3D pivot остаются прежними. Точка удара визуально уходит вперёд; `FlailItem` hit ray 4 блока, задержка charge 18 ticks, damage 7+bonus и server physics **не меняются**. Runtime 1P/3P/MP требуется.
- **#186 / Finist:** `FinistArrow` больше не отменяет уже выданный направляемой стреле target по `player.hasLineOfSight` и текущему mutable `FinistMarks.target(player)` **на каждом тике полёта**. Сервер валидирует живую враждебную цель в радиусе, проверяет клип блоков и ограничивает угол steering прежними 6°/tick; unlock не превращается в homing through wall. `FinistQuiver` charges, manual Shift, vanilla ammo fallback и lock при спуске не менялись. Возможны отдельно всё ещё причины пропуска при исчерпании магических зарядов — live QA нужен.
- **#185 / копьё победителя:** `RareCombat` устанавливает 8с cooldown в момент выдачи окна второго выпада, а не после использования, не позволяет повторно спамить «Второй выпад готов» от дальних хитов. При фактическом втором попадании пустой actionbar очищает сообщение, а `PlayerTickEvent.Post` очищает просроченное окно; 25 тиков таймер, native Player.attack/второй удар и damage mechanics без правки.
- **#160 / #161, громовое копьё:** существующий bonus **+2** и CD 120 ticks остаются, **25% chance** на server-spawned `LIGHTNING_BOLT` **visualOnly** при принимаемом ударе, без поджога/нового урона; звуки и enchant feedback на каждый прок остаются прежними.
- **#188 / наконечник громового копья:** удалены **5 дублирующихся coplanar west faces** главных секций, полностью перекрытых отдельным левым ребром. Все 37 cuboids и все внешние видимые стороны/tip silhouette сохранены, уменьшен риск z-fighting.
- **#160 / грозовой посох:** обычный cooldown 60→54 ticks, с руной 54→48 ticks (ровно -6 ticks/0.3sec на 20 TPS), damage/range/runes/visual bolt сохраняются.
- **охотничий рог (`beast_challenge_horn`):** заменена flat generated модель на **10-cuboid 3D horn** из прежней texture PNG, зеркальный main/off hand и отдельные GUI/ground/fixed transforms. Объём и посадка ещё **не проходили** owner-game QA.
- **#140 головы, не mob AI:** `TrophyHeadsClient/TrophyHeadFxRules` на **реальных шести trophy heads** усиливают движение jaw/ears/mane/whiskers/eyebrows, делают слежение зрачка обычного/морового Лиха за близким игроком в передней полусфере головы и симметрично закрывают верхнее и нижнее веко обычного Лиха, сокращают время между видимыми частицами до 9–20 тиков и добавляют 3 частицы за событие. `TrophyHeadBlock` добавляет burst 24 частицы при ломании обоих уровней; штатные loot tables и paired-half teardown сохраняются. Это **source-first attempt**, ни runtime-снимков, ни owner approval ещё нет. Раздельная задача **AI Лиха #193** не входит в этот патч.

### QA, статус
- `tools/verify_weapons87_p49.py`: проверка точного shared grip обоих мечей, сохранённой модели/5 поз Полоза, 37 cuboids/5 removed coplanar faces, связанной contact chain, безопасного visualOnly bolt, 6-tick CD, целевых ограничений FinistArrow, 3D horn, head pupil/jaw/particles/breaking. Production JAR проверки классов + byte-exact 5 изменённых item model JSON.
- Continuous Java21 source/JAR workflow расширен P49. Техническое подтверждение P49 **PENDING CI** до внешнего запуска.
- **Minecraft/PolyMC/MP: NOT_RUN**. Эти отклонённые владельцем Issues **НЕ приняты**, не закрывать и не выставлять «Готово» до его повторного игрового тестирования. Поле Project Status №9 недоступно имеющемуся GitHub connector; комментарии не являются сменой поля.
- Вся реализация в `work/weapons87-continuous-20261022` / draft PR #216. `main` не затронут.


### P49 — Java21 CI (source commit) и устаревшие исторические snapshots

- [GitHub Actions #39823430427](https://github.com/Deserag/slavic-myths/actions/runs/39823430427), commit `8c1f4cf3b9a989389dce8c702aaeda3b38d24b5f`: **SUCCESS** на Java21 clean build и P42–P49 strict source/JAR class+resource checks; JAR `build/libs/slavicmyths-1.9.7.56.jar`, **SHA256 `ba6ac79353692b88902f168257850d1901bb87519c3afcce0f3814424cf0785a`**.
- Дополнительные **старые** Java21 workflows P33/P35/P38/Storm/P39 на том же commit выдали FAIL на строгих regression guard literals **до Gradle**: ждали откаты `60/54`, хотя владелец прямо попросил `54/48`, и старое ограничение угла/длины шарика кистеня `72°/2.4px`, хотя визуальный шар owner отверг. Это не повод возвращать отвергнутое поведение и не свидетельство runtime-pass.
- `tools/verify_weapons87_storm_range.py` и `tools/verify_weapons87_p33.py` сохраняют остальные server/range/damage/VFX asserts и проверяют теперь owner-approved `54/48`.
- `tools/verify_weapons87_p39.py` сохраняет 7 links/owner binding/idle spacing и проверяет контактное удлинение (2.4+5.0px/link), угол 88°, impulse (52+33*lag), требование distinct heavy attack. Это **строгая проверка новой requested baseline**, а не удаление теста.
- Повторный CI на исправленных QA файлаx **PENDING**. Никаких игровых скриншотов или реальных запусков Minecraft не выполнено.


### P49 — итог Java21 и отметка owner-accepted

- Исправления источников [`8c1f4cf3`](https://github.com/Deserag/slavic-myths/commit/8c1f4cf3b9a989389dce8c702aaeda3b38d24b5f), правка исторических source guards [`03f3f2e6`](https://github.com/Deserag/slavic-myths/commit/03f3f2e6fc6545db6ad7823b4badc2f719c4de4b), один существующий draft PR #216.
- **Java 21 PASS** [Continuous #39823972474](https://github.com/Deserag/slavic-myths/actions/runs/39823972474), [P39 Flail #39823972563](https://github.com/Deserag/slavic-myths/actions/runs/39823972563), [Storm #39823972609](https://github.com/Deserag/slavic-myths/actions/runs/39823972609), [P35 #39823972429](https://github.com/Deserag/slavic-myths/actions/runs/39823972429), [P38 #39823972691](https://github.com/Deserag/slavic-myths/actions/runs/39823972691). Все перечисленные COMPLETED/SUCCESS, включая ранее падавшие regression gates. Java21 clean build и P42–P49 source/JAR checks PASS.
- Production JAR `build/libs/slavicmyths-1.9.7.56.jar` SHA256 **`ba6ac79353692b88902f168257850d1901bb87519c3afcce0f3814424cf0785a`**. Документирующий P49a commit не меняет production bytes.
- По прямому сообщению владельца (принял всё **кроме** перечисленных дефектов, отдельно подтвердил косу/посох Морозко/арбалеты) добавлена метка `owner-accepted` **только на узкие, арт-ориентированные задачи**: **#170 (обычные булавы), #174 (бердыш/серп/обычные посохи, без нового навыка carved_staff), #175/#176 (арт обоих арбалетов), #177 (коса Карачуна), #179 (посох Морозко), #183 (кинжал Соловья), #186 (арт лука Финиста)**. Ошибка auto-aim Finist НЕ принята и исправлена отдельным P49 code path без объявления игрового успеха.
- **Важное различие:** это **label** GitHub Issue (owner-acceptance, прямо согласовано владельцем), но **не изменение поля Status=«Готово» в GitHub Project №9**: текущий GitHub connector не предоставляет operation для Project v2 Status. Issues остаются OPEN, не закрывались. Подзадачи #115/#140/#160/#164/#180/#185/#188 и исполнительская часть #162/#163/#165 остаются спорными до нового owner-test и **НЕ** получают `owner-accepted`.
- Реальный Minecraft/PolyMC/MP на этой сборке: **0 запусков**, скрины рук, оригинальной плети, разлёта шара кистеня, глаз/челюстей трофейных голов и рога: **NOT_RUN**, acceptance этих конкретных дефектов по-прежнему OWNER_PENDING. Изменения mob-AI Лиха #193 не входят в пакет голов.


## P49 — отзыв владельца, 22.10.2026: РЕАЛЬНАЯ игровая приёмка вместо offline предположений

**Владелец не принял**: Finist homing (периодически не наводится); хват обоих каролингских мечей; кистень (ядро не делает ударной дуги); мерцание наконечника обычного копья; thunder spear (механика грозы недостаточна); victor spear (ошибочный и слишком поздний текст повторного выпада); плеть Полоза (отмена неутверждённой правки, хват за рукоять, вернуть старый дизайн); 3D охотничий рог; трофейные головы #140 (слишком слабые и редкие анимации, нет частиц, поломка и неработающие мимические анимации: Лихо eye follow/open/close, подземник jaw, Волколак ears, Ским и Тугарин eyes/ears). **#140 не закрывать / не принимать** и не выдавать за выполненную визуальную проверку. Арбалеты, коса Карачуна и посох Морозко **приняты и не должны менять модель/animation ещё раз**. Резной посох KEEP (базовая/прогрессия). Всё остальное без замечаний принято владельцем только в пределах конкретных проверенных отдельных задач; большой issue нельзя закрывать, если есть непринятые child requirements. Project Status API unavailable, issue comments ≠ Project Status.

### P49a — первая адресная правка в этой итерации

- `item/StormStaffItem.java`: user requested faster cooldown by at least ~0.3 seconds. Existing 54 / 48 ticks -> **48 / 42**, exactly **-6 ticks = -0.3s** for normal / active-rune cast. Damage/targeting/range/VFX untouched.
- `finist/FinistArrow.java`: `steer` had **hard stop when angle > 85°** (no homing at all for target outside that forward cone), despite valid locked `FinistMarks.target`. Removed that hard cutoff; full 6° bounded turn for obtuse angles, and an antiparallel special case to avoid near-`sin(π)=0` numerical instability. All hit damage, ownership, claim, pickup and `FinistQuiver` untouched. **Periodic failure reported by owner still requires reproduction**; this fixes one independently verified code defect, does not assert all homing cases solved.
- User requested more work than these two edits. Remaining defects above stay **OPEN / NEEDS FIX**, not hidden as accepted. Minecraft test for these edits NOT_RUN. CI PENDING until GitHub Actions checks actual new commit.

| Scope | Code evidence | State |
|---|---|---|
| Storm staff -0.3s both cooldown modes | `PULSE_COOLDOWN=48`, `RUNE_COOLDOWN=42` | SOURCE IMPLEMENTED, CI PENDING |
| Finist arrow no 85-degree tracking dead zone | `FinistArrow.steer` | SOURCE IMPLEMENTED, CI PENDING, REAL GAME NOT_RUN |
| Swords, flails, spear, victor, whip, horn | Owner FAIL; P49 backlog | NOT FIXED |
| All 6 trophy heads #140 | Owner explicit REJECTED | NOT FIXED, #140 OPEN |


## P50 — #140 визуальные головы: переделка после отказа владельца (не считать ACCEPTED)

**Задача:** исходные FX-1/FX-2 и P49 формально присутствуют, но пользователь 22.10.2026 признал игровые анимации **незаметными, редкими** и отсутствие заметных частиц. Требуются реальные, заметные действия глаз/челюстей/ушей, а не строковое совпадение в Python.

**Новая локальная правка**:
- `TrophyHeadFxRules.java`: Лихо моргает каждые 64 тика вместо 94, верхнее веко подвижнее (1.8 вместо 1.2 условных model units), глаз Морового Лиха пульсирует заметнее; свечение Лиха чаще. У Тугарина/Ским-зверя/крупного Подземника амплитуда челюсти повышена; ус/грива/брови подвижнее; у проклятого Волколака уши двигаются каждые 64 тика вместо 137, также усилено реальное движение челюсти.
- `TrophyHeadsClient.java`: при взгляде игрока на поставленные головы Лиха смещение глазной детали по позиции `glow` усилено в уже существующем направлении относительно блока и физического положения player. Частицы каждого из 6 видов чаще (периоды 9/10/9/11/8/7 тиков), по 5 вместо 3 за локальный emission, только 24 блока вокруг игрока и только в непоставленном на паузу клиентском мире. Это реально `Level.addParticle`, а не просто описание эффекта.
- Реальные source/native детали остаются прежними: `jaw`, `earL`, `mane_0`, `irisRing`, `lowerLid`, `brows`; никакого изменения модели моба, 3D silhouette, размера дропа, серверного spawn и сохранения мира. `h.resetPose()` остается обязательным после кадра.
- Тесты `verify_weapons87_p49.py` скорректированы только на реальные новые частоты/амплитуды, `verify_trophy140_p50.py` проверяет 6 видов/расчёты/активные render-part references/ограниченность emission, P50 source+JAR hook в continuous Java21 workflow.

**НЕ ПРИНЯТО / OWNER_PENDING:** фактические визуальные результаты, корректность привязки pupil для морового Лиха, разрушение головы с релогом/дропом, эффект в инвентаре, FPS 6-головой комнаты, рука и ракурсы, скриншоты, а также анимация глаз **самих живых мобов** — P50 меняет *только поставленные трофейные блоки*, а не живых Тугарина, Скима и Волколака. CI при первом source commit PENDING. #140 OPEN/REWORK.

### P50 — Java21/JAR source receipt (визуальная приёмка отдельно)

- [Workflow #39828042779](https://github.com/Deserag/slavic-myths/actions/runs/39828042779) **SUCCESS** на `444053b332868c65196ecf8726f9c1039cde5f50`: Java 21 `clean build` **BUILD SUCCESSFUL**, P49 + P50 source checks PASS и packaged Java classes/JAR checks PASS.
- `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 **`18b97afa5f24d87ce77e8e79c057cb4fdb52a67ebe0dc6b838944345543639d4`**.
- Всё ещё **NOT_RUN в Minecraft**; визуальный эффект и правильность eye target/увеличенного blink в 6 реальных моделях — OWNER_PENDING. #140 всё ещё OPEN/REWORK, даже при зелёном CI, и это только трофейные блоки, не анимации живых мобов.

## P51 — адресная доработка непринятых Finist homing и трофея Тугарина (22.10.2026)

- **FinistArrow:** исправлен проверенный геометрический дефект при угле почти 180°: прежняя линейная смесь `from*(1-fraction)+to*fraction` оставляла стрелу в прежнем направлении при малом шаге. Теперь устойчивый касательный вектор, ортогональный текущей скорости, даёт ровно до 6° поворота за тик без изменения длины скорости. Остальные углы, ammo, quiver, damage, target validation и право сервера на коллизию не изменены.
- **TrophyHeadsClient:** в уже имеющейся геометрии Тугарина найдены настоящие `pupilL` и `pupilR` как независимые `ModelPart`. Оба привязаны к локальному gaze установленных трофейных голов в передней полусфере, с ограниченным сдвигом и обязательным сбросом исходных координат после каждого render. Модель живого Тугарина, текстуры, геометрия и принятые предметы не изменены. Глаза Ским-зверя запечены в mesh `head` и этим исправлением не объявляются готовыми.
- `tools/verify_weapons87_p51.py` проверяет указанные реальные связи, математический инвариант разворота и production class packaging; workflow непрерывной ветки дополнен. **CI после этого коммита ещё требуется подтвердить.** Minecraft/PolyMC/server/MP **NOT_RUN**, визуальная и функциональная приёмка **OWNER_PENDING**, задачи #140/#186 остаются OPEN. Не сливать PR #216 в main и не ставить Готово.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Steering почти на 180° даёт 6°/tick вместо нулевого поворота | `FinistArrow.steer`, P51 source/math guard | SOURCE FIX; CI PENDING; GAME NOT_RUN |
| Оба реальных зрачка Тугарина следят за игроком и восстанавливаются | `TrophyHeadsClient.Head`, `BossReworkGeometry`, P51 guard | SOURCE FIX; CI PENDING; VISUAL NOT_RUN |
| Все непринятые части голов #140 и повторная игровая приёмка Финиста | Сценарии владельца, источник модели Скима | OPEN / OWNER_PENDING |

### P51 — реальный CI receipt, 22.10.2026

- Java21 [Continuous workflow #39829846856](https://github.com/Deserag/slavic-myths/actions/runs/39829846856) на source commit [`b606f7276704e7cf1bd8da5347336bed69be8f98`](https://github.com/Deserag/slavic-myths/commit/b606f7276704e7cf1bd8da5347336bed69be8f98): **COMPLETED / SUCCESS**. `./gradlew --no-daemon clean build`: **BUILD SUCCESSFUL in 1m 33s**. P49/P50/P51 source tests PASS, P51 production JAR check PASS.
- Production JAR `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 **`d9b805ac7355d2e9d2846807f8816f1437207af772e1bd541880cfd5d6815c68`**, only in GitHub Actions artifact, **POLYMC_NOT_INSTALLED**.
- P51 source/math/JAR requirements = PASS; gameplay Finist trajectories at 179.99–180°, placed Tugarin eyes at four viewpoints, multiplayer and Minecraft runtime = **NOT_RUN / OWNER_PENDING**; #140/#186 OPEN, no `owner-accepted`, no main merge.

## P52 — #161: три дыхания, адресный MOR/FIRE hit feedback (22.10.2026)

**Scope строго новый:** прежняя P44 дульная вспышка всех трёх режимов и существующий StormCrossbow/StormVfx не менялись. На сервере `CrossbowDamage.hit(LivingDamageEvent.Post)` при успешно применённом стрелковом уроне и уникальном UUID стрелы тот же исходный статус WEAKNESS(40t) режима морока теперь сопровождается 8 SOUL; тот же исходный поджог (2с) режима огня сопровождается 8 FLAME. Частицы ограничены компактной областью цели, публикуются только при **accepted damage**, не во время полёта и без новых tick listeners. Один `r.arrows.add(UUID)` блокирует повторную отправку для одной стрелы, special-only guard исключает обычный арбалет. Штормовой режим использует прежний `StormVfx.play`; новая молния/партикулы поверх него не создаются.

- Production: **только** `src/main/java/org/slavicmyths/gorynych/CrossbowDamage.java`. Урон, i-frames, криты, ammo, три раунда, cooldown, loot, texture, animation и registry IDs неизменны.
- 2 режима × single target / fan / burst: source tests `tools/verify_weapons87_p52.py`, CI continuous source+production JAR; визуальные сервер/клиент/MP/SFX и массовый бой в Minecraft остаются **NOT_RUN / OWNER_PENDING**.
- **Матрица:** `MOR | successful arrow hurt after Post | Weakness + 8 SOUL | P52 source gate | SOURCE IMPLEMENTED, CI PENDING`; `FIRE | successful arrow hurt after Post | Fire 2s + 8 FLAME | P52 source gate | SOURCE IMPLEMENTED, CI PENDING`; `STORM | old storm burst | existing StormVfx only | P52 source gate | KEEP`; `MP/FPS/real game | retest | native runtime | none | NOT_RUN`.
- Status request of owner **«на тестировании2»** refers to Project №9 custom field, but the available GitHub connector cannot modify Project v2 fields. Do not pretend Issue label or comment equals status. #161 remains OPEN because broad 68-item issue scope is not completed. PR #216 stays Draft; no main merge.

### P52 CI follow-up — legacy storm_staff source guard

Older `weapons87-storm-range-java21` and P38 workflows failed before Gradle: `tools/verify_weapons87_storm_range.py` required original P49 `54/48` despite explicit owner-requested P49a `48/42`, which P33/P49/actual Java already assert. Updated **only those two test literals** to 48/42. Production gameplay/render source untouched. Do not revert owner's cooldown. P52 source+Java21+JAR gate run after update; status OPEN / owner testing.

### 22.10.2026 · P52 verified CI, release-gate separated from owner QA

- Code [04158550](https://github.com/Deserag/slavic-myths/commit/04158550aa142f43ad99e83b460e7ec2c1138b4f) + legacy test-only fix [e0560924](https://github.com/Deserag/slavic-myths/commit/e056092428d31bfcff2833e007043b1ed32a45ea). [Continuous Java21 #39832746501](https://github.com/Deserag/slavic-myths/actions/runs/39832746501) **COMPLETED/SUCCESS**: P49/P50/P51/P52 source and packaged JAR tests PASS, `BUILD SUCCESSFUL in 1m 32s`. **All 11** GitHub Actions runs for e0560924 completed SUCCESS, including the previously failing P38/Storm source regression checks; no runtime launched.
- `build/libs/slavicmyths-1.9.7.56.jar` SHA256 `ed90b2d3cc519d1bc3cae036e42658eb741fe5bc38c813f5cea9b3ef49c61f91`; built in GitHub Actions artifact, **POLYMC_NOT_INSTALLED**. P52 is ready for owner visual gameplay testing, not accepted. #161 full issue still open because only MOR/FIRE hit VFX addressed. No Project v2 Status field action in GitHub connector; requested «на тестировании2» remains NOT_SET.
