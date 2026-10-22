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
