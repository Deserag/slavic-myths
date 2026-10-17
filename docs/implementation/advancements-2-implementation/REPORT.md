# Slavic Myths 2.0 — реализация достижений

**IMPLEMENTED_FOR_GAMEPLAY_ACCEPTANCE. Игровая приёмка NOT_RUN.** Исходный код, ресурсы и офлайн-проверки реализуют утверждённый проект; готовность к релизу этим не подтверждается. По запрету владельца Minecraft не запускался ни в каком виде: **0 clients, 0 dedicated/integrated servers, 0 GameTest servers, 0 тестовых миров**. Выполнялись компиляция, чтение установленных исходников и чистые офлайн-проверки.

## A. Что реализовано

Основа: актуальный `origin/main fb2994e958911169317523e1066b8a1ca7a155af`, версия1.9.7.5; версия не повышается. Minecraft1.21.1, NeoForge21.1.255, Java21, official mappings, mod ID `slavicmyths`. Отдельная ветка `codex/advancements-2-implementation`, отдельный worktree. Пользовательский checkout, performance/model ветки, PolyMC и миры не менялись.

[#30](https://github.com/Deserag/slavic-myths/issues/30) реализуется по согласованному [проекту PR31](https://github.com/Deserag/slavic-myths/pull/31), HEAD `12eca688a44c7193ebcde1873dac03e9fa6f3a29`. PR31 и [PR25](https://github.com/Deserag/slavic-myths/pull/25), HEAD `e2ca8a8f998bb390cd6538f8979a2df13da006cc`, открыты и не объединены при проверке. Исправления PR25 явно включены как зависимость реализации, а не объявлены существующими в main. Исходные TREE/DECISIONS/SOURCE и документы PR31 сохранены как замороженная спецификация; их исторические результаты не считаются запусками этой реализации.

**55 отображаемых узлов =1 root +6 разделов +48 вех.** 44 прежних карточки переработаны по точному display/parent контракту; 85 прежних игровых узлов стали data-only без поля `display`; 11 новых. Фактический datapack содержит **217 JSON =140 игровых +77 recipe unlocks**, все206 старых ID на месте. Новых предметов, текстур, моделей или registry registrations нет.

| Раздел | Вехи без раздела |
|---|---:|
| У родного огня | 6 |
| Слово и знак | 7 |
| Дороги богатырей | 9 |
| Лес и тихие воды | 9 |
| Память под землёй | 8 |
| На краю сказания | 9 |

Имена, RU/EN описания, существующие иконки, task/goal/challenge, родители и флаги соответствуют [TREE.json](../advancements-redesign/TREE.json) и [полной таблице](../advancements-redesign/TREE_TABLE.md). Root/разделы без toast/chat, task/goal с toast без chat, challenge с toast/chat, `hidden=false` у всех55. Родитель не является требованием победы. [Статическая схема из фактических ресурсов](TREE.svg) / [PNG](TREE.png) проверена как артефакт, **не как Minecraft GUI**.

### Все 11 новых серверных условий

| ID | Подтверждающий callback | Допуск и отрицательные условия |
|---|---|---|
| `defeat_gorynych` | `Gorynych.aiStep`, после mask7, таймера, PURIFIED/persist/save | Личные native Participants, online/same world, survival/non-spectator, !Standalone. Не `die`, EXHAUSTED, подарок или успешный claim; полный инвентарь не отменяет медаль. |
| `defeat_idolishche` | `BogatyrLedger.victory` → deferred medal → `IdolishcheEntity.die` | Только получатели принятой native victory, без debug/creative/spectator/admin damage; затем фактическое `!wasDead && dead` после super.die. |
| `defeat_marinka` | `WitchYard.victory` → deferred medal → `MarinkaEntity.die` | COMBAT, !debug, native participants+contributed/radius/world; не ложное тело, не просто освобождение Добрыни; accepted death. |
| `defeat_skim` | `SkimValley.win` → deferred medal → `SkimBeastEntity.die` | Native contributor OR attendance≥60/inside32/survival, native LastEncounter dedup; accepted death. Attendance — существующая политика, не новое расширение коопа. |
| `defeat_kartaus` | `KartausEncounter.finish(true)` → deferred medal → `KartausEntity.die` | Native credited owner с ownerHit/дистанцией/живым survival игроком. Ни зритель, ни отмена/restart/ABANDONED не создают медаль. |
| `defeat_kalin` | `KalinEncounter.finish(win)` из нативной `KalinEntity.surrender` | Прежний owner-only/playerHit/живой survival/same world/≤48, !credited; дополнительный medal-only фильтр admin/creative source. Не raw death. |
| `class_iv` | Успешная ветвь `ClassState.branch` после записи Third/Ultimate | Существующие definition, level≥40, matching Second, completed trial. Классы и требования не переработаны. Не открытие экрана или raw NBT. |
| `curse_resolved` | `VolkolakStory.handOver`, после returned/SKULL_RETURNED/ready | Настоящий Owner+Encounter+Nonce черепа, личная сдача. Не смерть CursedVolkolak или общий RESOLVED. |
| `kurgan_rest` | Принятый `KurganEncounters.died` principal transition | Prince либо tier1 Voevoda; actual accepted death, UUID в alive roster, player-causal источник, damage contributors; не RuneSummoned/retired/unload. |
| `depth_champions` | `EncounterMob.die`, после native defeat и принятой смерти | Только RoomBoss: Kladovik/LargePodzemnik, непустой native key, population SPAWNED и детерминированный UUID слота. Не обычный podzemnik/debug/unbound/один consumed flag. |
| `night_watch` | `NochnitsaEntity.die`, после super/accepted death | Survival causal player, включая native projectile owner; не debug night override, /kill, proximity или смерть без player source. |

У новых узлов один `victory`, у класса `mastery`; `rewards={}`. `AdvancementMilestones` не вызывает loot/reward services. Существующие native фильтры Идолища/Маринки/Скима/Картауса/Калина сохранены; права случайных наблюдателей не расширены. Медали четырёх death-боссов откладываются до возврата из native `super.die`; при отмене очередь очищается. Число адресатов этой очереди определяется существующей native политикой, новый co-op cap не вводится.

Для Старшего Водяного/кургана/подземных элит добавлен отдельный entity-local damage tracker: максимум64 уникальных contributors, без tick polling, обходов игроков/мира или chunk requests. При победе выбираются online survival contributors в том же мире/≤96, плюс подтверждённый causal killer независимо от заполнения tracker. Это новая консервативная политика медали там, где персонального ledger не было. Native loot/Claims не изменены. Native projectile source поддерживается; environmental death без подтверждённого player source не приравнивается к победе.

Старые крупные вехи также согласованы с проектом: Полоз — running native event/тот же boss/accepted death/native participants; показ подаренного ядра изучает запись stage3 без новой победы. Старший Водяной требует настоящей привязки PoolStoneTile и личного damage credit вместо proximity. Лихо/Тугарин требуют успешного `HuntRecords.finishBoss` и прежнего last-hit player. Атаман/Соловей не дают медаль за отменённую/admin/creative смерть; native участники Соловья сохраняются. Морозко/Карачун по-прежнему используют существующие completed-trial ledgers, не убийство. Земля и Леший сохраняют прежние условия/criteria.

### Книга и исправления PR25

Все234 tale JSON и ссылки сохранены без изменения. `TaleCatalog`/`Knowledge.ENTRIES` и значения старых `advancementStage` не переработаны. `reconcile` продолжает читать data-only holders; только граф фильтрует отсутствие display. Новые55 карточек попадают в существующий граф без нового packet format. Tooltip `Available` поясняет свободный порядок прохождения; отдельные `tales.group.*` подписи навигации также соответствуют шести утверждённым ветвям.

`lore_keeper` при успешном открытии использует≥3 **уникальных** известных legacy ID; `kitchen_meal` выдаётся за реальное получение результата/порции; `claimed_*` используют правильный criterion `claim` через Knowledge. Для новых fragment medals требуется native participant, при этом прежняя выдача самого фрагмента не ограничена новым условием. Положительный/отрицательный fixture PR25 сохранён и дополнен participant/observer случаем, только скомпилирован.

`bookkeeper` считает десять реально сохранённых записей stage≥4. Проверка есть при повышении записи и успешном открытии после reconcile, чтобы уже подтверждённые десять записей не застряли без вехи. Это не login import. Новые accepted milestones повышают лишь существующие точные subjects Горыныча/проклятого Волколака/Ночницы до stage4; у остальных новых боссов отдельные encyclopedia subjects в нынешних234 отсутствуют, они представлены новыми карточками графа и прежними native story/reward данными. Выдуманные tale entries/Defeated friendly NPC не добавляются.

## B. Совместимость и перенос прогресса

[Полная матрица206 ID](COMPATIBILITY.md) / [JSON](COMPATIBILITY.json), [неизменный save-contract PR25](../advancements-micro-patch/save-contract.json), [контракт234 tales+77 recipes](resource-contract.json).

- M0:44 старых visible ID — меняются только утверждённые parent/display/локализации.
- M1:85 старых ID — удалено только поле display; прежний parent/criteria/requirements/rewards остаются. Не `hidden=true`.
- M2:77 recipe unlocks — точное совпадение tracked Git blobs; различия checkout CRLF/LF учитываются стандартными Git filters, а не переписыванием рецептов.
- M3:11 новых ID с пустыми rewards; визуальный successor ничего автоматически не выдаёт.

Файлы player advancements не открываются и не переписываются; completed/partial/timestamps не пересчитываются. По установленному vanilla loader display optional, сохранение прогресса не фильтруется по display, награды выполняются только при новом incomplete→done. Это статическое обоснование, **не проверка старого мира**. Старое done, включая победы по более широким прежним условиям, не отзывается. Скрытые старые rewards/XP сохраняются, чтобы не менять баланс.

**Автоматический исторический импорт НЕ АКТИВЕН.** `HistoricalMilestonePolicy` — чистый read-only planner для8 возможных персональных доказательств. Он не читает миров, не создаёт progress, не вызывает award/reward, не подключён к login/reload и не ставит markers. Для трёх новых элитных итогов старых персональных фактов нет — planner их не предлагает. Офлайн-проверка planner не доказывает получение/перенос в игре.

Причина блокировки импорта точная: нет проверенного pre-reset native load hook, save failure/revoke/idempotence/toast доказательств. Дополнительно некоторые старые native ledgers записывали победу **до** `super.die`; исторический count/flag сам по себе не доказывает отсутствие отменённой смерти. Новая очередь защищает медаль, но намеренно не меняет прежние reward/cleanup механики боссов. Поэтому массовый login award или импорт по предмету/world cleared запрещён и не реализован.

Откат к старому JAR возвращает старую подачу при сохранённых206 ID. Старый loader может не сохранить11 незнакомых новых ID при последующем save; сохранность новых медалей при downgrade не обещается. Резервные копии пользовательских миров не создавались и не менялись этой задачей.

## C. Проверки

Результаты и SHA JAR — [AUDIT.json](AUDIT.json), production section. Итоговый JAR SHA256 `c2a32c4c75b79da1232f598eb5752b92261f0f34d69ca4683277e3efd230503e`; 19708639 байт,3930JSON/1325PNG/1693Java21classes/222subscribers,8508 resource/class checks PASS. Сборочные логи прилагаются обезличенными. [TASK_GRAPH.json](TASK_GRAPH.json) подтверждает build/compile-only задачи; игровой Gradle task не запускался. GameTest fixture находится вне production sourceSet и лишь компилируется opt-in.

| Проверка | Результат | Граница |
|---|---|---|
|217 JSON,206 прежних ID,55 visible/85 data-only/6 ветвей, родители/циклы | PASS | Статика |
|Старые criteria/requirements/rewards и OR/AND | PASS | Точный неизменный PR25 baseline, разрешён только TREE/DECISIONS display/parent |
|77 recipe unlocks/234 tales и ссылки | PASS | Git blob parity и семантические refs; реальные player files не читались |
|Полные RU/EN и48 уникальных icon registrations/models | PASS_STATIC | [ICON_PROOFS](ICON_PROOFS.json); runtime registry/UI NOT_RUN |
|19 mutation tests | PASS | Сохранены10 PR25 и добавлены9 отрицательных; ошибки не скрываются ослаблением save-contract |
|Historical planner314 pure Java cases | PASS_OFFLINE | Minecraft classes/classpath=0; реальный импорт НЕ проверен и не активен |
|Генератор overlay, все advancement/locale bytes | PASS | [GENERATOR.json](GENERATOR.json), idempotence; старые полные генераторы не запускались, final overlay hooks добавлены |
|Java21 clean build и compileAdvancementCheckJava | PASS | Компиляция; обычный test task NO-SOURCE, не игровые тесты |
|Ресурсы/production JAR/class version/subscribers/test exclusion | PASS | [AUDIT.json](AUDIT.json), только production resources/classes |
|Статическая схема SVG/PNG | PASS | Просмотр артефакта, не vanilla/Книга GUI |
|Нативные GameTests/игровые award/deny сценарии | NOT_RUN | Категорический запрет запуска серверов |
|Загрузка старого мира/save/restart/revoke, toast/chat | NOT_RUN | Не подменены JSON/NBT симуляцией |
|Кооп/dedicated/GUI scales/RU-EN/полный Survival IV | NOT_RUN | Требуется последующая игровая приёмка |

Офлайн-воспроизведение (Java21; последние две команды не требуют Minecraft):

```powershell
./gradlew.bat clean build compileAdvancementCheckJava -PadvancementCheck
python tools/verify_advancement_tree.py --jar
python tools/test_advancement_audit.py
```

314 pure-Java cases воспроизводятся командами [CI workflow](../../../.github/workflows/advancements.yml), без Minecraft classpath. Канонический ресурсный entry point — `python tools/tale_advancements.py` либо `python tools/advancements2_resources.py`; он не читает сейвы. Полные исторические генераторы прочего контента не запускались.

В ранних tooling-попытках обнаружены ожидаемое несоответствие старого валидатора дереву55, тест frame с устаревшим именем карточки и разница checkout EOL. Исправлены адреса mutation tests/точные presentation allowlists/Git blob сравнение; исходный save-contract не изменён. По реальным исходникам уточнён extractor многострочных/vanilla block icon registrations. Ошибки инструмента не выдаются за игровые дефекты.

## D. Что проверить после разрешения Minecraft

1. На отдельной копии нового/старого survival мира — vanilla и Книга,55 карточек, RU/EN, scales1–4,1366×768/1920×1080, late cards/parent-before-or-after/pan/zoom, отсутствие data-only карточек и toast flood при входе.
2. Реальные native победы/отрицательные случаи всех новых боссов: очищение Горыныча с full inventory, сдача Калина, false-body/debug/creative/admin/cancelled death, owner-only и native contributors/attendance кооп. Существующие loot/quest flows должны остаться прежними.
3. Морозко/Карачун именно trial completion; Полоз без gifted-core обхода; Elder без observer credit; курган только principal/active roster; подземный native deterministic slot; Ночница с player projectile и без debug override.
4. КлассIV через реальные level/trial requirements;10 подтверждённых записей/первое открытие/legacy lore_keeper/кухня/оба хранителя, partial progress и рецепты.
5. Настоящее сохранение/новый процесс/reload/двое клиентов: completed и partial/timestamps старых206 ID, отсутствие повторных XP/items/toasts, revoke; лишь затем проектирование и native проверка исторического pre-reset import.

## E. Итог

**Всё утверждённое дерево55 реализовано в ресурсах и серверных callbacks.** Офлайн-gates и Java21 сборка проходят; схема доступна для просмотра без игры. Известные потери ID/criteria/requirements/rewards/77recipes/234tales статикой не обнаружены. Автоматический исторический импорт оставлен неактивным в соответствии с разрешённым безопасным fallback.

**Есть непроверенные блокеры приёмки:** реальный loader старого мира, native award/cancellation/co-op/save/GUI/уведомления. В старых reward lifecycles остаётся описанное ограничение pre-death history; этой задачей нельзя обосновать безопасный автоматический импорт. Сохранение прогресса защищено неизменными definitions и отсутствием записи в сейвы, но игровой roundtrip не выполнен. Это PR для последующей игровой приёмки, **не полностью принятый релиз2.0**. Main/PolyMC/миры не изменены, auto-merge не выполняется.
