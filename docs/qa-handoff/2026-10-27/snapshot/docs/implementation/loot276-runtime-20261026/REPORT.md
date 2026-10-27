# #276 — настоящий Minecraft server integration, 26.10.2026

**IN_PROGRESS / OWNER_PENDING. #276 OPEN; к закрытию пока не готова.** Только `hell`; production и баланс не менялись. Ввод/окна пользователя после просьбы прекратить не использовались.

## Что реально запущено

- Minecraft1.21.1 + NeoForge21.1.255 + Curios9.5.1, Java21.0.12. Восемь отдельных запусков dedicated Minecraft server без UI (включая неуспешные итерации тестового стенда),0 клиентов,0GameTest-процессов. PolyMC launcher был открыт 1 раз до уточнения пользователя, клиент не запускался; два захвата окна завершились timeout, затем UI больше не использовался.
- Использован точный production JAR из установленного PolyMC: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-2.0.0-HELL\.minecraft\mods\slavicmyths-2.0.4-HELL.jar`, SHA256 `1c22bf186ab6886fee8a464fb44b123620354ef93ee0f990d1208660d3743010`, source `8d0f71b446f59d144f4ea9bd6631759117de80ff`. Ровно один JAR мода; в изолированную папку сервера скопированы те же байты. Проверка CodeSource подтверждает загрузку этого JAR, а не development classes. Установленную сборку/миры/config не заменяли.
- Новый тестовый мир: `D:\slavic-myths\work\loot276-runtime-20261026-run5\loot276-disposable`, seed2762026. Он создан сервером, отдельно от PolyMC saves. Flat, generate-structures=false, сервер только 127.0.0.1:25776. Настоящие production StructurePiece размещены явно; это проверяет блоки/маркеры/таблицы, **не естественную встречаемость и не terrain preflight естественного worldgen**.
- Основной прогон: **660 PASS**,33 набора частей/вариантов +полный DepthsService job.128 наградных контейнеров сооружений +2 глубин =130. Все 30 таблиц `rewards276/*` декодированы настоящим Minecraft; 60000 seeded draws и 267607 проверок допустимых ItemStack. Два native FakePlayer актёра проверяют последовательный доступ к одному inventory; это **не сетевой MP**.
- После штатной остановки и сохранения новый Java-процесс загрузил тот же мир: **415 PASS**. Проверены 130 пустых контейнеров, сундук с 3 алмазами и неоткрытая таблица; затем восстановлены serialized production pieces и повторно вызван настоящий postProcess. Призы не восстановились, ритуальные gates сохранились. Для Depths повторно вызван native chest этап с прежним DepthsId — без новой выдачи.

Доказательства: [preflight](preflight.json), [основной прогон](create-results.json), [перезапуск](restart-results.json), [независимая сверка](verification.json), [все позиции/ожидания](witnesses.json), [saved pieces для повторной обработки](pieces.json). Это реальные server-level действия Minecraft, а не прежний Python loot model. Saved pieces хранятся отдельным QA-файлом; этот тест не доказывает сохранение естественного StructureStart/полного натурального worldgen.

## По каждой структуре

| Структура / вариант | Фактически проверено | Результат | Проблемы / исправления |
|---|---|---|---|
| Курган I | 3 reward containers; assigned tables→native loot→empty→restart→postProcess retry; ровно 1L1main | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Курган II | 9 reward containers; assigned tables→native loot→empty→restart→postProcess retry; ровно 1L1main | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Курган III | 17 reward containers; assigned tables→native loot→empty→restart→postProcess retry; ровно 1L1main | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Лагерь малый +cart | 4 reward containers; assigned tables→native loot→empty→restart→postProcess retry; ровно 1L1main | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Лагерь средний | 9 reward containers; assigned tables→native loot→empty→restart→postProcess retry; ровно 1L1main | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Лагерь большой +cache_0 | 14 reward containers; assigned tables→native loot→empty→restart→postProcess retry; ровно 1L1main | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Святилище огня | 5 reward containers; assigned tables→native loot→empty→restart→postProcess retry; lockedread/extract/two actors denied; wrongitem denied; настоящийember ritual→open | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Обитель источника | 4 reward containers; assigned tables→native loot→empty→restart→postProcess retry; wrongitem/одинpool недостаточны;2powder/2nodes→open | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Крепость Горыныча | 11 reward containers; assigned tables→native loot→empty→restart→postProcess retry;6chests+5barrels; ровно 1main;1–2Poloz | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_abandoned_0 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_abandoned_1 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_abandoned_2 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_fisher_0 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_fisher_1 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_fisher_2 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_landing_0 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_landing_1 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_landing_2 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_boardwalk_0 | 1 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_boardwalk_1 | 1 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_boardwalk_2 | 1 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_watchtower_0 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_watchtower_1 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_watchtower_2 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/ruin_0 | 1 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/ruin_1 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/ruin_2 | 1 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_shrine_0 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_shrine_1 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_shrine_2 | 2 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_cluster_0 | 5 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_cluster_1 | 5 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| swamp/v097_cluster_2 | 5 reward containers; assigned tables→native loot→empty→restart→postProcess retry | PASS native integration; natural/GUI NOT_RUN | Production-дефект не подтверждён; баланс без правок |
| Древние глубины | Полный public DepthsService.start job, COMPLETE,2chests/12mobs; первый прежний, второйnewtable; restart receipts +native retry | PASS job/loot/save; карта/кампания NOT_RUN | Production без правок |
| Клиентские меню и анимация открытия | Клиент не запускался; FakePlayer.openMenu не моделирует реальное GUI | NOT_RUN | Владельцу |
| Два сетевых клиента | Только последовательные вызовы двух native FakePlayer | NOT_RUN network MP | Владельцу: одновременный доступ/пакеты |
| Натуральная генерация/встречаемость/крайние terrain | Стендflat и explicitpieces; полное естественное исследование не выполнялось | NOT_RUN | Владельцу: новый нормальный Survival-мир |
| Князь/Соловей/Горыныч: полный бой и личная выдача | Создание их мест/контейнеров проверено; бой/claim не разыгрывался | NOT_RUN combat/progression | Соседние исправления и дроп не менялись |

Лагеря на стенде собраны из реальных production templates, разнесённых по координатам для изоляции: это покрывает роли контейнеров, но не доказывает фактическую естественную компоновку/проходимость целого лагеря. Курганы — реальные KurganPlan seeds276/277/278 +KurganDungeonPiece; крепость — целый SerpentHillPiece; святилища — все реальные 16×16tilepieces. Болота — все 3 варианта каждого из 8 семейств. Старые открытые пользовательские миры не запускались.

## Таблицы, ритуалы и редкие награды

Внутри create-results каждый набор содержит фактическую карту `routes`. Никакие предметы не подставлялись в reward inventories вместо loot engine: они разворачивали назначенную таблицу через native unpackLootTable. Контрольный сундук с 3 алмазами — отдельно обозначенный save witness, не награда. После первого unpack второй актёр не меняет inventory/таблицу. После clear +save +другой процесс +reprocess markers ни одна награда не назначается заново.
До ритуала оба актёра не проходят canOpen; unpack/read/removeItem/removeItemNoUpdate не выдают содержимое. Неверный предмет не открывает cache. Для spring первый очищенный node недостаточен, два настоящих interact расходуют 2powder. Для fire настоящий interact расходуетember и выставляетFireRestored; прежнийNightLedger доставляетpurifying_ash в освободившийся слот. После ритуала оба canOpen разрешены, cache разворачивается один раз. После restart unlocked состояние сохраняется. Работа реального hopper/ломание блока/клиентский click и creative menu остаются NOT_RUN.
Фактический fortress_main native sample:2000draws,0empty,2159Poloz pieces (E1,0795),50runeIV (2,5% выборки),max16stacks. Это статистическая выборка около заданных 8% второго/3%IV, а не замена точных вероятностей. На самой построенной крепости проверены 11 контейнеров и 1–2 частиPolоз. Ни balances, coins, registry IDs, recipes, story keys, Tugarin/Prince drops не менялись.

## Найденные проблемы и исправления

**Подтверждённых ошибок production-механик в проверенном scope нет.** Исправлен только изолированный QA-стенд; это не объявляется исправлением игрового бага.
1. Первый прогон считал literal пустую бочку storage_tent обязательной наградой. NBT показывает отдельный loot:camp marker и отдельную decorative barrel. Стенд теперь отдельно фиксирует unassigned containers; loot-marker containers по-прежнему обязаны дать обещанную награду. Optional camp_3_cache может быть пустым по старым согласованным pools — это не guaranteedmain.
2. Ритуальный assert ошибочно требовал пустую руку послеember. Лог доказал `purifying_ash` в руке и `restored=true`: это прежняя доставканаграды. Assert проверяет расходember иFireRestored; параметры ритуала не переписаны.
3. Стенд долго держал старые BlockEntity references после выгрузки chunks. В первой паре create/restart изменение staleobject не попало в живой chunk. Это выявлено по настоящему Anvil NBT, где оставался pendingLootTable. Исправление стенда: брать liveBE из chunk перед действием, проверять identity/!isRemoved и держать только QA reward chunks загруженными до сохранения. Новый чистый run5 и независимый процесс restart подтвердили persistence; production chunk logic не менялся.
4. Первая проверка reprocess потребовала пустоту от dispenser со 8 стрелами и SlavicKurganTrap=1. Это технический контейнер, не loot witness. Теперь 130 наградных позиций обязаны оставатьсяпустыми, а вне них запрещена новая LootTable; traps/decor не выдаются за приз. Пополнение израсходованной ловушки отдельно не тестировалось.
Сохранены неуспешные receipts attempt1/2/3/4 и restart-trap-expectation; они не скрыты количеством PASS. Один compile attempt отсутствующего import исправлен до запуска; в 8 Minecraft-процессов он не включён. Финальные create/restart логи не содержат ERROR/parse-exception. Есть обычные devCurios refmap/vanilla teleport ambiguity/OS counter warnings и `Can’t keep up` при больших синхронных QAразмещениях; это не замер MSPT/игровой производительности.

## REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| hell, один установленный 2.0.4JAR, прежние saves | preflight+verification, SHA, исходныйlevel.dat совпал | PASS |
| Java21 clean build / resources / production JAR | clean-build.log:1m24s;8031resources/1950classes; SHA совпал с установленным | PASS build/package; не игровой тест |
| Отдельный тестовый мир | work/loot276-runtime-20261026-run5/loot276-disposable | PASS |
| Курганы/лагеряL1 |3+3native набора; одинmain; native inventories, save/retry | PASS integration; Survival NOT_RUN |
| Оба святилища и ограничения | actualheart.interact, twoactors/wrongitem/onepool/lockedread/remove | PASS server APIs; GUI/hopper/break NOT_RUN |
| Крепость: containers/награды | целыйSerpentHillPiece;6+5,1main,Polоз bound | PASS explicitgeneration; naturalterrain/combat NOT_RUN |
| Болота/глубины |24actualtemplates;DepthsService.COMPLETE2chests/12mobs | PASS integration; map acquisition NOT_RUN |
| Пустота/restart/сохранение |130emptied +filled/pending controls, новый Java процесс, marker retry | PASS world container persistence |
| Два игрока | nativeFakePlayerA/B, second unpack unchanged | PASS sequential server API; networkMP NOT_RUN |
| Логи Minecraft | verification errors:create[]/restart[]; QAfail отдельно | PASS final scoped logs |
| Баланс и production не менять | git production diff empty; установленныйSHA прежний | PASS |
| Игровая приёмка владельца / закрытие | Полная Survival/GUI/network проверка ещё нужна | NOT_RUN / OWNER_PENDING; #276 OPEN |

## Конкретный чек-лист владельцу

Использовать установленную 2.0.4-HELL с SHA выше. Создать новый обычный Survival-мир `Loot276-owner`, например с seed 2762026; основной мир не трогать. Старые сохранения проверять только на копии. OP-команды ниже предназначены для отдельной диагностики и не доказывают естественную доступность.

1. Без OP найти и пройти курганы I/II/III и лагеря small/medium/large. Записать количество контейнеров и сумму находок. Ожидается один главный тайник на сооружение и прежний L1. В лагерях нет незерита; новые руны III/IV в этих тайниках исключены. Отдельный прежний дроп князя/Соловья не суммировать с сундуками как новый pool. Отсутствие редкой награды в одной зачистке — не ошибка.
2. Для ускоренной диагностики: `/slavicmyths generate kurgan small 276`, затем `warrior 277`, `great 278`; `/slavicmyths generate bandit_camp small 276`, `medium 277`, `large 278`. Каждый раз дождаться завершения и выданных координат. `/slavicmyths generation status` показывает ход задания. Эти команды создают сооружение и не проверяют natural worldgen.
3. Найти оба святилища обычным путём. Диагностические команды: `/slavicmyths dev create sanctuary_unfading_fire` и `/slavicmyths dev create abode_pure_spring`, по одной, в отдельном месте. До ритуала попробовать открыть cache, применить неверный предмет, очистить только один узел: тайник должен оставаться закрытым. Ember из внешних припасов восстанавливает огонь; два powder очищают два узла. После ритуала cache открывается и выдаёт гарантированные ресурсы/руну. Восстановление огня также даёт прежний пепел. Ломание красного узла — существующая альтернатива очищения, а не новый обход.
4. Под ещё закрытым cache поставить hopper: приз в него не поступает. Разрушение закрытого cache проверять только на копии мира: закрытые награды не должны выпадать. Проверить настоящий клиентский click/menu в Survival и Creative. Два настоящих клиента должны открывать общий inventory без повторной выдачи; серверная проверка с FakePlayer не заменяет этот сценарий.
5. Найти крепость естественно или заработанным поиском. Для диагностики: `/slavicmyths dev create serpent_hill`, дождаться конца и координат. Ожидание: 6 сундуков + 5 бочек, один главный сундук, всего 1–2 части Полоза. Второй предмет может повторить слот; весь комплект не гарантируется. Пройти полный путь Горыныча и отдельно проверить личный claim. Шансы 3%/8% нельзя подтвердить одной крепостью.
6. Проверить swamp_hut, fishing_camp, swamp_remnants, bog_causeway, swamp_watchtower, underwater_ruins, flooded_shrine и abandoned_settlement. В текущем коде `/locate structure slavicmyths:<ID>` для малых мест может запустить явное создание: не считать это естественной находкой. Для больших мест locate ищет уже учтённые структуры. Cluster имеет 5 наградных контейнеров, ritual — 2, boardwalk — 1; ruins — 1, кроме variant1 с 2; остальные перечисленные — 2. Декоративная пустая бочка не равна сломанному loot-маркеру.
7. Получить и использовать карту глубин обычным способом. Диагностика: `/slavicmyths depths map`, затем использовать карту; либо `/slavicmyths depths generate` от имени игрока с OP. `/slavicmyths depths status` показывает состояние. Ожидаются 12 врагов и 2 сундука: первый прежний, второй с новой таблицей. Повторный вход в ту же instance не восстанавливает награды.
8. Оставить вещи в одном открытом сундуке, другой опустошить, третий не открывать. Выйти в меню, полностью закрыть Minecraft, запустить снова и вернуться. Вещи сохранились; пустой не пополнился; pending-контейнер сохранил свою таблицу. Повторить с выгрузкой чанков и двумя сетевыми клиентами. В копии старого мира сравнить прежние открытые и pending-контейнеры; смешанным старым/новым сооружениям новая гарантия не обещается.
9. Для каждой ошибки сохранить seed, координаты, действия и `logs/latest.log`. `/data get block X Y Z LootTable` показывает таблицу неоткрытого контейнера; после открытия ключ обычно исчезает. Обычные таблицы `chests/...`/`kurgan/...` в части контейнеров ожидаемы. Не выдавать main через `/loot` в каждом сундуке: это обходит ограничение на сооружение.

### Точные координаты стенда (только его world-копия)

Мир стенда: `D:\slavic-myths\work\loot276-runtime-20261026-run5\loot276-disposable`. Если нужно посмотреть его в PolyMC, **скопировать** папку под новым уникальным именем в saves при закрытой игре; основной мир не заменять. Он уже опустошён тестом — этостенд anti-refill, ане fresh loot playthrough. В testworld есть QA forced chunks; не использовать его как основную кампанию.

| Место | Проверенная позиция контейнера / команда | Ожидание в сохранённом стенде |
|---|---|---|
| Курган I | `/tp @s 999 91 29`; container `999 89 29` | EMPTY; не должен заполниться заново |
| Курган II | `/tp @s 1252 91 -22`; container `1252 89 -22` | EMPTY; не должен заполниться заново |
| Курган III | `/tp @s 1508 75 41`; container `1508 73 41` | EMPTY; не должен заполниться заново |
| Лагерь малый +cart | `/tp @s 2002 103 4`; container `2002 101 4` | EMPTY; не должен заполниться заново |
| Лагерь средний | `/tp @s 3026 103 6`; container `3026 101 6` | EMPTY; не должен заполниться заново |
| Лагерь большой +cache_0 | `/tp @s 4154 103 5`; container `4154 101 5` | EMPTY; не должен заполниться заново |
| Святилище огня | `/tp @s 5511 104 17`; container `5511 102 17` | EMPTY; не должен заполниться заново |
| Обитель источника | `/tp @s 6012 103 14`; container `6012 101 14` | EMPTY; не должен заполниться заново |
| Крепость Горыныча | `/tp @s 6536 163 89`; container `6536 161 89` | EMPTY; не должен заполниться заново |
| depths | `/tp @s 12928 19 1447`; container `12928 17 1447` | EMPTY; не должен заполниться заново |
| saved_control | `/tp @s 9000 102 0`; container `9000 100 0` | 3diamonds |
| saved_pending | `/tp @s 9003 102 0`; container `9003 100 0` | pending fortress_main, ещёнеоткрыт |

Полный список 130reward позиций, включая каждыйswampвариант, — witnesses.json. ПеремещениеOP к этимкоординатамне доказывает Survivalpath.

## Исправлено / Не сделано / Риски и откат / Следующая задача

**Исправлено:** только стенд: классификация декоративных и технических контейнеров, ожидание пепла после ритуала, работа с актуальными BlockEntity. Подтверждённых ошибок мода в проверенной области нет. Java21 clean build выполнен повторно; SHA сборки совпал с установленным. Версия остаётся 2.0.4-HELL.
**Не сделано:** полный естественный worldgen и крайние ландшафты, доступность в Survival и время прохождения, GUI/анимации, настоящий hopper и ломание блока, два сетевых клиента, полные бои/сюжет, старый мир в клиенте. После просьбы пользователя интерфейс не использовался.
**Риски/откат:** стенд находится только в tools и отдельном sourceSet, в production JAR не попадает. Исходные saves/config и установленный JAR сохранены. Неуспешные итерации стенда описаны выше; неподтверждённые баги не исправлялись изменением баланса. Тестовые миры находятся отдельно; автоматического удаления не было. Соседние #277/#278/#284/#287 не правились.
**Следующая задача:** ручные проверки оставшейся части #276 по списку выше. К закрытию задача пока не готова: требуется решение владельца. Issue не закрывалась, main не менялся, Project Status не изменялся; следующая разработка не запускалась.
