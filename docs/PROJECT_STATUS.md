> Актуальный checkpoint 29.10.2026: 2.1.4/2.1.6 реализованы с разрешением всех девяти биомов Незера; редкость подобрана на пяти seed. Исправлена native generation 2.1.5. Итоговый 2.1.6-HELL установлен в тестовый PolyMC, GUI 0; headless 105/105 и 32/32. [Код, SHA, отчёт, NOT_RUN и checklist](implementation/2.1.6-trizna-pyre/STATUS.md). Старые BLOCKED_TZ для 2.1.4/2.1.6 и FAIL natural 2.1.5 ниже — исторические. 2.1.7 без ТЗ; 2.1.8/2.1.9 исключены.

> 29.10.2026: 2.1.5 native generation repaired; five-seed headless85/85, crafted floor protection from delta/basalt features, exact source designs retained. [Evidence and limitations](implementation/2.1.5-ancestor-shrine/STATUS.md). 2.1.4 implemented;2.1.6 authorized all9biomes;2.1.7 blocked specification;2.1.8/9 excluded.

> 29.10.2026: домашний VS Code `hell` синхронизирован; `be639736`/`bd604be3`
> исправляют поздние грибные кроны моста и phantom/locate-cache стоянки.
> Java21 clean build/resources PASS; 5 seed 40/40, общий baseline 21/22 (FAIL 2.1.5 natural).
> Тестовый 2.1.3-HELL установлен в отдельный PolyMC с backup/одним JAR; GUI 0.
> Новые 2.1.4/2.1.6/2.1.7 BLOCKED_TZ; 2.1.8/2.1.9 исключены, owner acceptance нет.
> [SHA, установка, фактические seed, ограничения и checklist](implementation/home-sync-20261029/REPORT.md).

> 28.10.2026 #337: ресурсный блок #324, Nature/Traits20мобов#328 и
> 6обычныхкузнечныхupgrades добавленывhell. МастерзадачаНЕзавершена;
> [фактический журнал](implementation/2026-10-28-hell-all-tasks/STATUS.md).

> 28.10.2026 #332–#336: manual Posten изолирован; ordinary ModelPart IK/material/poses.
> Офлайн gates и ограничения: [mob332 REPORT](implementation/mob332/REPORT.md).
> Gameplay/MP NOT_RUN, original art BLOCKED_REFERENCE; Issues OPEN.

## 28.10.2026 — #331: доработки боссов, головы и реальные клиентские кадры

Пересобраны3boss rigs/UV/PNG; полный skull Сторожа, jaw/6–8proximityFX,
увеличенный отдельный трофей Постеня4маски; loaded-only missingTile repair Жареницы.
Vanilla audio cues17events, явная причина no-arena loot; HP/registryIDs сохранены.
Java21 clean build/native/resources PASS;4разрешённых devclient запуска в новых
отдельных мирах,20финальных реальных кадров;4BlockItem placement/Tile/render и
break→1предмет PASS. Это не survival/MP/старыймир/ARToriginal приёмка.
Кандидат2.0.10-HELL установлен с backup/однимJAR; SHA256
e84bb45be63bfd04134373f524eb34bcd10a714292e37dee9c41e99a5f7d6953.
Изменения локальны, безcommit/push/Issueclose; originalPNG BLOCKED/OWNER_PENDING.
[Реальные скриншоты, исходники, матрица и ограничения](implementation/boss331-rework/REPORT.md).

## 28.10.2026 — #331: повторный аудит визуала, без production-исправлений

origin/hell=d97a625f; установлен2.0.9-HELL SHA d5fbefe1e0041dc3d2c3b32779c4c8f5eee23274089a811fb7843c209f0991f9.
Журнал владельца20:36МСК подтверждает запуск2.0.9; старыйJAR не объясняет последний тест.
Подтверждены source-различия3моделей, плоский guardian trophy и Posten .85scale.
Четыреheadпути упакованы; manualZharenitsaбезhome не выдаёт награды, oldBE migration рискNOT_RUN.
Арт владельцем отвергнут/originalPNG BLOCKED; собственных игровых запусков0.
[Аудит, реальные before/after и план следующего патча](implementation/boss-audit-331/REPORT.md).

## 28.10.2026 — номер кандидата обновлён до 2.0.9-HELL

По поручению владельца изменён только mod_version в gradle.properties.
Java21 clean build PASS1m47s; resources/JAR PASS; все2090classes и игровые
ресурсы побайтово равны предыдущему кандидату (различаются только два metadata).
2.0.9-HELL установлен в привычный HELL-инстанс с backup и однимJAR.
Minecraft/PolyMC/сервер/GameTest0; прежние ограничения2.0.5/ART/audio сохраняются.
[Квитанция/SHA/build](implementation/version-209-20261028/installation.json).

## 28.10.2026 — домашний ПК: актуальный hell собран и установлен

Fast-forward877b7d69→d97a625f; production без локальных правок. Java21 clean build
PASS2m17s, offline journals/native models/legacy heads PASS; runtime launches0.
Кандидат2.0.8-HELL установлен в реальный выбранный Slavic-Myths-2.0.0-HELL,
backup/одинJAR/SHA совпадают:4a76d2198a7c3dbad006774586ef8d041bb8df8dfefb5d02f6935896bcec830b.
Чертоги и #327 присутствуют в коде; старые записи ниже о backend-only исторические.
2.0.5 ocean-only worldgen/вулкан/арена отсутствуют; originalPNG/audio BLOCKED,
игровые/MP/старыймир NOT_RUN. Полная приёмка не заявляется.
[Текущий отчёт/матрица/установка/checklist](implementation/home-update-20261028/REPORT.md).

## 28.10.2026 — Ревизия первых трёх биомов Незера, только hell (2.0.2–2.0.4)

Новые адресные исправления **без изменения четвёртого биома 2.0.5**: Навья роща — preflight `navya_heart` до генерации и chunk-local проверки при размещении; Чёрная гарь — запрещён reward claim незарегистрированной арены и лут Жареницы без home; Чертоги — сверка канонических UUID и фазы копий при восстановлении Постеня. Негативные офлайн-тесты для двух последних подготовлены, **НЕ ЗАПУЩЕНЫ**. Общий [report/source matrix](implementation/nether-first-three-review-20261028/REPORT.md). Java/Gradle NOT_RUN, Minecraft/PolyMC/MP 0 запусков. Чертоги **НЕ ГОТОВЫ**: реальные модели/мобы/биом/worldgen/loot/руна/головы ещё отсутствуют. Арт BLOCKED_REFERENCE по отсутствующим доступным raw PNG/SHA, ссылки на архивы в Library не равны реальным бинарным файлам. Игровую проверку первых трёх биомов проведёт владелец совместно после завершения; никакие Issues не закрыты.

## 28.10.2026 — Чертоги: независимый production backend, IN_PROGRESS

По уточнению владельца отсутствие PNG блокирует ART, но не независимый код.
Добавлены LostHallsRooms (saved metadata/nearest48blocks без chunk loads),
PostenEncounters (650→260→4×65, stable UUID/NBT/одно разрешение reward batch)
и LostHallsRouteCheck (bounded проверка конечного voxel snapshot).
Consumers пока отсутствуют: предмет/мобы/генератор/боевые атаки/реальная выдача
не подключены. Это инфраструктура, не готовый третий биом.
Java21 clean build+offline logic PASS21s с точным localCurios fallback;
11755assertions/200расчётныхбоёв/binaryNBT/5000дальнихузлов, не игровые тесты.
Resource/JAR PASS8254resources/2019classes; SHA256
9ab1c2e7c68208b4d8dfdac077023b6859e8c7fd84ffcc670919113f7dffafa6.
9457прежнихproductionфайлов сохранены. Minecraft/сервер/GameTest/PolyMC=0;
не установлено, ART BLOCKED_REFERENCE, runtime NOT_RUN. Архивы обнаружены
в библиотеке ChatGPT, но физически ещё не переданы в эту сессию.
Следующий шаг: получить4архива, SHA-проверка и #289/#290; затем подключить
готовую геометрию/сущность к backend. Commit/push/Issues Status не менялись.
[Отчёт и матрица](implementation/lost-halls-2.0.4/REPORT.md),
[checkpoint для продолжения](implementation/lost-halls-2.0.4/PROGRESS.md).

## 28.10.2026 — Чертоги 2.0.4: начато исполнение, SOURCE BLOCKED_REFERENCE

Рабочая `hell` на 877b7d69 получена в отдельный worktree; старая локальная
`master` и её пользовательские изменения сохранены. Из origin/main e7fcd0e2
адресно перенесены 14 контрактов/чертежей, без merge. Добавлены source inventory,
SHA256/PNG/ZIP validator и 8 успешных тестов самого validator. Ни один из пяти
original PNG не найден среди 469 просмотренных кандидатов. Третий биом,
новые блоки/мобы/босс/руна пока НЕ реализованы; арт BLOCKED_REFERENCE.
Production src/main не менялся (9457 файлов в manifest). В build.gradle добавлены
Maven group filters; обычный clean build FAIL из-за timeout Curios Maven.
Java21 clean build с локальным точным Curios 9.5.1 (Modrinth, SHA512 проверен)
PASS за28s; resources/JAR PASS:8254resources/2004classes. JAR2.0.8-HELL,
SHA256872c3f468b5c626faafc4aeb32b500da2b7214f685905178ac93d9c52778be18.
Minecraft/client/server/GameTest/PolyMC:0запусков; установка не выполнялась.
Следующий шаг: получить оригиналы, пройти SOURCE и завершить #289, затем #290
в рамках уже порученной цепочки. Commit/push/GitHub Status не выполнялись.
[Матрица, baseline, ограничения и ручной checklist](implementation/lost-halls-source-gate/REPORT.md).

## 27.10.2026 — Чёрная гарь #261–#267: код и ресурсы реализованы, IN_PROGRESS

По восстановленному паспорту в hell добавлены 26 блоков/чернодрево/3 формы деревьев,
4 жителя с отдельными 3D rigs/атаками/анимациями, Жареница 750 HP с 2 фазами,
4 приёмами и 4 обязательными наградами. Печати из main перенесены адресно,
руна связана с существующими armor sockets, journal сохраняет defeated/one-shot claim.
Добавлены biome black_cinder, natural tables, bounded geology/nature, структура
zharenitsa_arena и native locate IDs; существующий Nether payload сохранён.
Гарантия арены пока условная по безопасному ядру 512×512; реальный connected
coverage/проходимость/спавны ещё не измерены. Java21 clean build/native/JAR PASS,
230 baked poses, 1500 связных tree plans, 5-seed offline climate сохраняет 7 biome
категорий. Это не игровые проверки: Minecraft/сервер/GameTest/PolyMC launches=0;
ART_ORIGINAL_COMPARE_NOT_RUN, OWNER_PENDING. Кандидат 2.0.8-HELL с контентом2.0.3
установлен по прямому поручению с backup/одним JAR; SHA64b042ae3def10952a0c120d8dac9b16c95658070450bb0b323fd48afad3084e.
Новых commit/push/PR/Project Status нет, Issues OPEN с IN_PROGRESS комментариями.
[Матрицы/исходники/evidence/ручной checklist](implementation/black-cinder-production/REPORT.md),
[installed receipt](implementation/black-cinder-production/installation-receipt.json).

## 27.10.2026 — единое поручение #261–#267, актуализация источников

Прочитаны обновлённые верхние разделы от17:55UTC. Без merge получена свежая
origin/main ba5c07cb: частичная ZharenitsaSeals найдена именно там, в hell её нет.
ZIP/полное новое ТЗ физически не поступили: доступно57TXT+1JSON, ZIP0/PNG0;
повторный SOURCE gate BLOCKED_REFERENCE. Запрошен путь/ссылка, новые поручения
между этапами не нужны. Gameplay не добавлялся; Minecraft/установка/commit/push/
Project Status не выполнялись. [Точная матрица #262–#267 и свидетельства](implementation/black-cinder262-20261027/single-run-update/REPORT.md).

## 27.10.2026 — продолжение #262, build blocker устранён

Переименованы только внешние локальные x/z в SerpentHillStructure, поведение
не менялось. Java21 clean build1m42s/resources8032/production JAR PASS;
SHA03547c81efaf364a09d2d72732e59d91c0c3de675641928d7e507d4b4faead53.
Расширенный поиск9119PNG не нашёл ни одного из4approved SHA. SOURCE gate
BLOCKED_REFERENCE, #263–#267 не реализованы, команда поиска ещё не существует.
Сборка не установлена, Minecraft/сервер/GameTest=0; финальная приёмка не заявлена.
[Квитанция/матрица/ограничения](implementation/black-cinder262-20261027/REPORT.md).

## 27.10.2026 — Чёрная гарь #262, начат первый этап

Прямое поручение на второй биом: прочитаны #261–#267, восстановлен strict2.0.3,
добавлены технический контракт и рабочая SHA/PNG/ZIP проверка четырёх оригиналов.
Негативные проверки PASS; реальные оригиналы отсутствуют — BLOCKED_REFERENCE.
Production не менялся: #263–#267 заблокированы зависимостью первого этапа.
Повторный Java21 clean build FAIL на прежних x/z в SerpentHillStructure:15,
нового JAR нет. Minecraft/PolyMC/сервер/GameTest=0, установка не выполнялась.
Project статусы «Анализ» проверены, не менялись; Issues OPEN, OWNER_PENDING.
[Матрица/контракт/следующий шаг](implementation/black-cinder262-20261027/REPORT.md).

## 27.10.2026 — #252, техническая подготовка Навьей рощи, IN_PROGRESS

По прямому поручению проверены #251–#257, main/hell и существующая реализация.
Добавлены воспроизводимая source-инвентаризация (33 блока/7 entity types), лист
фактических текстур восьми материалов, три вспомогательных разреза и локальная
копия STRICT_ACCEPTANCE_2.0.2. Production/worldgen не менялись; дубли не создавались.
SOURCE+DESIGN gate: BLOCKED_REFERENCE, оригиналы PNG недоступны. Java21 clean build
FAIL на исходном SerpentHillStructure:15 (повторные x/z); production JAR gate
BLOCKED. Minecraft/PolyMC/сервер/GameTest=0, POLYMC_NOT_INSTALLED для этой работы.
Переход к #253 заблокирован, прочие этапы не запускались; GitHub статусы уже
«В работе», не менялись. Матрица, артефакты, installed SHA и ручной checklist:
[отчёт #252](implementation/navya252-20261027/REPORT.md).

## 27.10.2026 — #300, генерация Чуди и площадка Яги, IN_PROGRESS

Работа ведётся в `hell`, без commit/push и изменения установленного PolyMC. Новые
планы получают отдельную сохраняемую лестницу до поверхности, точную проверку
покрытия кровли и защиту соседних построек; старые планы читаются без ретрогена.
Участок Яги подготавливается целиком с переходом к рельефу и откатом транзакции.
В реальном новом мире обнаружено перезаписывание склада ванильной жеодой;
добавлена адресная защита от конфликтующих подземных features, повторный прогон идёт.
Фикстуры участка Яги и настоящий перезапуск прошли; полный набор пяти seed,
пешие маршруты и оба поручения ещё не засчитаны. Версия пока 2.0.8-HELL;
повышение версии — после завершения исправлений. Issue OPEN, приёмки нет.
[Матрица и доказательства](implementation/worldgen-chud-yaga/REPORT.md).

## 27.10.2026 — продолжение стабилизации, 2.0.8-HELL, IN_PROGRESS

Сохранён воспроизводимый production baseline 2.0.7 (a08a19d6), затем исправлены выбор известного природного города Чуди и повторное создание после награды; исправлены портрет, контекстная помощь и модальный слой Яги. Source кандидата 2.0.8: 23c2728f. Шесть последовательных dev-client прогонов подтверждают только описанные сценарии и реальные перезапуски; отдельные preview-прогоны #298 не меняют production art. Владелец дал новый текстовый референс, принятие конкретного PNG ещё требуется. Частотная диагностика трёх seed неполна: UNKNOWN и пропущенные кандидаты запрещают сравнение редкости с курганом III. Исправлена ошибка старого аудита: боссы abandoned уже существуют в underground, их реальное появление ещё не подтверждено. Полный Survival/редкие встречи/босс III/станки/equipment/MP остаются NOT_RUN. Installed PolyMC остаётся 2.0.7; 2.0.8 не устанавливалась. Issues OPEN, main не менялась. [Матрица, доказательства, ограничения и ручные шаги](implementation/stabilization269-next-20261027/REPORT.md), [build/source/SHA](implementation/stabilization269-next-20261027/BUILD_RESULT.json), [preview #298](implementation/chud298-preview-20261027/APPROVED_REFERENCE.md).

Позднейшая поправка имеет приоритет над историческими записями ниже: утверждение «нет готового босса abandoned» было ошибочным; см. [ERRATA](implementation/stabilization269-next-20261027/ERRATA.md).

## 27.10.2026 — разведка Чуди/Яга/крепость/городище, IN_PROGRESS

Один отдельный normal QA-мир seed2692026,5последовательных client/integrated-server запусков, максимум1одновременно. До поручений native city0; запрос Яге из12000,12000 создал город с входом12132,79,11790 на248.04XZ, затем настоящий restart/menu повтор сохранил цель/одну запись. Защищённая tuff_bricks площадка первой попытки отклонена без цели. Расчёт100ячеек каждого крупного типа: по1accepted start крепости/городища, оба подтверждены native FULL/saved MCA реальными блоками.8соседних регионов Чуди дали4города(variant0/2) и4MEDIUM/GREAT, всеnative FULL; полные footprint/бои/Survival не засчитаны. Нет маршрутов Яги/Чуди к abandoned/крепости/городищу и готового боссаabandoned; предложения отдельно, не реализованы. Глобальная частота/сравнение фактической редкостиIII NOT_RUN. Production/баланс не менялись, installed2.0.7не заменялся; Java21clean build/resources/7457offline PASS. [Матрица, координаты, ограничения и checklist](implementation/scouting269-20261027/REPORT.md). Все Minecraft завершены, Issues OPEN, main/Project/commit/push не менялись.

## 27.10.2026 — #269: брожение, один клиент и 2.0.7-HELL, IN_PROGRESS

Подтверждена и исправлена потеря существующего self_brewed_beer после переноса брожения в чан: BrewTile вызывает прежний completed при смене качества; RU/EN описание исправлено, legacy barrels не включались. Три успешных последовательных client/integrated-server прогона: 48 ограниченных checks PASS, реальные 2400/8400/12000 ticks без ускорения, наливание/пресс/тара/бочонок/разрушение и два настоящих перезапуска. Ошибки QA fixture и earlydisplay crash сохранены в отчёте; полный Survival/MP/#299 не объявлены пройденными. Java21 clean build57s, 7457 offline assertions, 8031 resources/1950 classes PASS. 2.0.7-HELL установлена в PolyMC, один JAR, SHA/backup2.0.6/source+patch подтверждены. [Матрица, ограничения и ручные шаги](implementation/stabilization269-brewing-20261026/REPORT.md). Все игровые процессы завершены; Issues OPEN, main/Project не менялись, commit/push нет. Общий остаток — 14 широких частичных направлений, не 14 отдельных тестов.

## 26.10.2026 — #269: один клиент, кухня и2.0.6-HELL, IN_PROGRESS

По решению владельца игровые процессы строго по одному; MP-повторы отложены.3последовательных client/integrated-server запуска с30FPS/render2/JVM2CPU/heap1536MiB:34 функциональные проверки кухни PASS (реальные ticker/расход/тара/menu packets/active cooking restart/три порции) и2 контрольные проверки открытия экрана после правки. Подтверждён и исправлен двойной сдвиг подписи «Инвентарь» в KitchenScreen, actual до/после сохранены. Java21 clean build3m31s/resources8031/classes1950 PASS; установлена2.0.6-HELL, один JAR, backup2.0.5 проверен. [Матрица, source/SHA/backup/ручные шаги](implementation/stabilization269-kitchen-20261026/REPORT.md). [Остаток:16направлений,2 scoped technical gates пройдены,14 имеют остаток/owner review](implementation/stabilization269-kitchen-20261026/REMAINING.md) — не14 коротких тестов и не процент готовности. Все процессы завершены; Issues OPEN, main/Project не менялись, commit/push нет.

## 26.10.2026 — #269/#276: network ritual + restart, IN_PROGRESS

Два настоящих Minecraft-клиента/TCP loopback проверили оба ритуала: доступ до/после, duplicate requests с расходом одного реагента на действие мира, общий component-aware loot без дублирования; затем оба клиента перезапущены, тайники пусты и повторный ритуал не расходует новые реагенты.4client/2integrated-server старта,54 ограниченные проверки PASS;0ERROR/Exception/Connection reset в4логах. Все процессы завершены. Только QA fixture с выданными реагентами — не full Survival/natural acquisition/same-server-tick proof. [Матрица, логи/SHA/квитанции и manual checklist](implementation/stabilization269-mp-ritual-20261026/REPORT.md). Все1950mainклассов совпали с установленным2.0.5-HELL;8031resources PASS. Production/PolyMC не менялись, Issues OPEN, main/Project не менялись, commit/push нет. Остаток — Survival/AI/редкие встречи/остальные сети, не новые задачи.

## 26.10.2026 — #269/#276: установлена тестовая2.0.5-HELL, IN_PROGRESS

По прямому поручению владельца 2.0.5-HELL установлена в PolyMC: один JAR, SHA1746acf792f1599f8d36dbc855c88823e7ecd71bf072053cefd01f823dbd55df; backup установленной2.0.4 проверен. Java21 clean build55s,8031resources/1950classes,415 actual-JAR save-checks PASS. Все1950 игровых классов совпали с ранее проверенным developer snapshot2.0.4; bump меняет только metadata. Native курганIII (seed12345) и святилище огня (seed−269) теперь подтверждены: полные footprint-чанки, main1, настоящий locate без стройки. Обитель чистого ключа также подтверждена (seed8675309,42 footprint-чанка,main1). Оба природных ритуала и новый процесс после каждого прошли32 проверки. В этой серии467 ограниченных проверок,9 server стартов (включая QA отказ); все процессы завершены. Весь Survival/AI/MP не объявляется завершённым. [Матрица, installed path/SHA/source+patch/backup и ручной чек-лист](implementation/stabilization269-205-20261026/REPORT.md). Issues OPEN, main/Project не менялись, автоматических commit/push нет.

## 26.10.2026 — #269: комплексная стабилизация, IN_PROGRESS

Локальный diff в `hell` исправляет управление/сохранение/посадку Сивки, грунт дома Яги, создающий fallback locate, sprite bindings и учёт disabled-frequency соседей. Исторически выполнены29 dedicated/20 client процессов (включая неуспешные QA),26 естественных сооружений12типов на пяти seed, настоящий клиентский relog и ограниченный MP двумя клиентами. Владелец повторно разрешил Minecraft: [новая runtime-регрессия](implementation/stabilization269-resume-20261026/REPORT.md) завершила7 server/6 client запусков,510 ограниченных проверок: пять свежих миров, native курганII/большой лагерь/крепость, настоящий relog/MP и415 save-checks нового JAR. III/оба святилища natural NOT_RUN; QA relog исправлен и повторён, MP shutdown Connection reset зафиксирован. Последние Java21 clean build + placement/structures/performance suites и8031ресурс/1950классов PASS. [Полная матрица, SHA, сравнения и остаток](implementation/stabilization269-20261026/REPORT.md). Все игровые проверки не завершены, PolyMC не обновлялся, Issues OPEN, main не менялся, автоматических commit/push не было.

## 26.10.2026 — #276: native server loot/save regression, OWNER_PENDING

На точной копии установленного 2.0.4-HELL выполнены 8 изолированных запусков Minecraft server, включая неуспешные итерации стенда; клиентов и GameTest — 0. Финальные 660 проверок создания и 415 проверок перезапуска PASS: 33 набора частей/вариантов, 130 наградных контейнеров, 30 настоящих loot tables / 60 000 розыгрышей, ритуалы и полный DepthsService job. Новый процесс подтвердил сохранение пустых, заполненного и неоткрытого контейнеров, а также отсутствие повторной выдачи. Исправления только в QA: декор/ловушки, награда пеплом, актуальные BlockEntity. Production и баланс без изменений. [Матрица, неуспешные попытки и команды владельцу](implementation/loot276-runtime-20261026/REPORT.md). Java21 clean build 1m24s, 8031 ресурс / 1950 классов PASS; SHA совпал с установленным. Natural terrain / Survival / GUI / hopper / network MP / полные бои NOT_RUN. #276 OPEN, main не менялся.

## 26.10.2026 — #276: награды всех сооружений Overworld, OWNER_PENDING

В `hell` сохранён прежний L1 курганов/лагерей, добавлены16 scoped loot tables для двух святилищ, крепости Горыныча, болот, древнего святилища и второго сундука глубин. Чудь/города/деревни/малые бытовые места и сценарные награды учтены отдельно и сохранены. [Полный отчёт, before/after, матрица всех мест и пошаговый плейтест](implementation/loot276-all-20261026/REPORT.md). 132 профиля×20k=2,64млн автономных симуляций; Java21 clean build1m34s,8031resources/1950classes PASS,1941старый class побайтно сохранён. Игровых запусков0: natural generation/эффекты/relog/MP NOT_RUN. 2.0.4-HELL установлена файлово в PolyMC без запуска; один mod JAR, backup прежней2.0.2 проверен. Точные path/SHA/source8d0f71b4 — в отчёте и installation-receipt.json. #276 OPEN, main не менялся, ручной приёмки нет.

## 26.10.2026 — #287: расширение проверки до28/28, IN_PROGRESS

[Дополнение аудита](implementation/armor287-20261026/REPORT.md): после #277 ещё20/20 и28/28native cases, всего6изолированных armor server запусков/client0. Добавлены trophy3, Perunite lightning, SevenLeague ramp, Ward shield negative cases, Wind charge/native arc/fall-credit. Armor production не менялся. Natural full acquisition/GUI/real relog/MP и остальные shield procs NOT_RUN; owner acceptance ожидается. [Итоговый JAR/source/SHA](implementation/armor287-20261026/extended-receipt.json), POLYMC_NOT_INSTALLED; Issues OPEN, main не менялся.

## 26.10.2026 — #277: реакция князя на дальние попадания, IN_PROGRESS / OWNER_PENDING

Только KurganCreature: PRINCE запоминает допустимого attacker после принятого урона; target вне арены ведёт к внутреннему краю вместо home; видимый дальний projectile attacker разрешает прежние конечные окна guard. Stats/фазы/move release/leash/geometry/loot/save/остальные kinds не менялись. [Baseline3FAIL, финальные11/11 native GameTest, trace, source guards и owner checklist](implementation/prince277-20261026/REPORT.md). Пять изолированных server запусков,0клиентов; natural полный курган/реальный relog/network MP NOT_RUN. Java21 clean build,15source/package guards и8001resources PASS. #277 OPEN/«В работе», Project fields не менялись, POLYMC_NOT_INSTALLED. #278/#284/production броня сохранены; owner acceptance ожидается.

## 26.10.2026 — #287: native-аудит брони, IN_PROGRESS / OWNER_PENDING

В hell проверены34 catalog IDs:22 ArmorItem/7 ClothingItem/4 ShieldItem/1 BeltItem. Production-код/ресурсы/баланс не менялись: доказанных дефектов в проверенных сценариях нет. [Каталог,14/14 GameTest, матрица ограничений и owner checklist](implementation/armor287-20261026/REPORT.md). Реальные четыре изолированных server запуска; client0. Native equip/remove/repeat,22 anvil calculations,6sets physical damage, selected unique/dragon effects PASS; полный trophy/charge/lightning/GUI/relog/MP NOT_RUN. Отдельный opt-in harness исключён из JAR. #287 OPEN, Project Status не менялся; POLYMC_NOT_INSTALLED. Следующая разрешённая часть — #277 отдельным коммитом, не готовый релиз цепочки.

## 26.10.2026 — #284: аудит34 реликвий и пять транзакционных исправлений (IN_PROGRESS)

В `hell` исправлен расход при отклонённом projectile/effect у mor_flask, fading_flame_charm, mor_ward, tracker_sense и spring_tear. Только3 production Java files и2страницы книгиRU/EN: убран ложный совет искать отсутствующие рецепты гуслей/скатерти. Успешные параметры/gates/баланс/IDs/loot/art/полёт/оружие сохранены. [Поимённый реестр24+10, настоящие GameTest logs, матрица, JAR/SHA и owner checklist](implementation/artifacts284-20261025/REPORT.md). #278 сохранён ранее939cbfbf/4fba8ac1, повторная server regression7/7PASS.

Java21 clean build --no-build-cache1m4s,10 artifact+10 accessory offline checks,17source/package guards,8001ресурс/1948Java21classes PASS. Четыре изолированных GameTest #284: baseline7PASS/7FAIL, run2 13PASS/1FAIL, run3 15/15PASS, run4 19/19PASS. Клиент0; natural acquisition/full story/GUI/real relog/respawn/dimension/network MP NOT_RUN. Gusli/skatert/kartaus_black_seal/kalin_battle_seal: BLOCKED_NEEDS_ACQUISITION_APPROVAL — текущий survival source не найден, новые recipes/rewards не придуманы. Mor flask явно diagnostic по tooltip; bottles — upgrade ingredients. #284 OPEN/«В работе»/OWNER_PENDING, Project field не менялся, итоговая приёмка цепочки не заявлена. POLYMC_NOT_INSTALLED; main/user worlds/config/installed JAR не менялись. #286/#287 только оценены, реализация не начата.

## 25.10.2026 — #278: passive charms в Curios и актуальный active slot (IN_PROGRESS)

В `hell` amber/perun/forest/traveler charms надеваются в прежний единственный charm slot со щитом; старый offhand сохранён без двойного эффекта. StormProtection server-only; общий FolkAccessoryItem getter читает текущий active declared slot вместо Curios item-cache, устраняя stale смену внутри тика. RU/EN tooltip/book/legacy-skill и hunter detector text соответствуют коду. Число мест/IDs/loot/recipes/art/баланс неизменны. [Поимённый реестр37, матрица, реальные GameTest logs, JAR/SHA и Survival/MP checklist](implementation/accessories278-20261025/REPORT.md).

Java21 clean build --no-build-cache 1m4s,10 native production-rule checks,31 package/source guards,8001 current resources и1948 Java21 classes PASS. Три изолированных GameTest запуска по прямому разрешению владельца: run1 2PASS/4FAIL, run2 6/6PASS, run3 7/7PASS. Все37 server slot APIs и damage четырёх charms/Perun Ring подтверждены; GUI/natural acquisition/остальные эффекты/настоящий relog/respawn/dimension/network MP NOT_RUN. Curios NBT не равен relog. #278 OPEN/OWNER_PENDING, Project Status не изменён. POLYMC_NOT_INSTALLED, main и пользовательские worlds/config/installed JAR не менялись. Технические результаты сохранены; #284 разрешён отдельным scope исходного поручения, #276/#286/#287 автоматически не реализуются.

## 25.10.2026 — #296: числовая поисковая навигация (IN_PROGRESS)

В `hell` книга и маркеры показывают dimension, фактические центр X/Z/radius, границы круга и inside/outside/state. Книга использует свои public Hints, NavigationScreen — свой SearchArea; приватные Anchor/Target и неизвестный Y не подставляются. Полный текст прокручивается, компоненты/font split кешируются; подсказки обновляются/удаляются через прежний snapshot. Saved models/protocols/worldgen/registry и #295 не менялись. [Отчёт, исходники, native offline примеры, JAR/SHA и ручной checklist](implementation/navigation296-20261025/REPORT.md).

Java21 clean build3m51s,5068 новых bare-JVM checks,76 регрессий #272,50289 старых book-cache/line/viewport checks, scope/resource/JAR8001 PASS. Minecraft/client/server/GameTest0; реальный GUI854×480/1920×1080, relog/старый мир/MP/FPS NOT_RUN. NBT roundtrip не заменяет игровой перезаход. #296 OPEN/«В работе»/OWNER_PENDING; POLYMC_NOT_INSTALLED, прежние пользовательские config/saves/JAR не менялись. Main не изменён; передача #278 подготовлена без начала реализации.

## 25.10.2026 — #295 + часть #275: read-only locate и реальные генерационные прогоны (IN_PROGRESS)

В `hell` search/locate Чуди, городища/горы/святилищ отделены от явного create. Чудь загружает raster footprint (city99/98 вместо bounding289), не перебирает6площадок; natural raw ores/cave plants разрешены, dungeon/container/portal protections сохранены. Городище построено в superflat; Яга не обходит slope limit. По прямому согласованию владельца новые дома Баюна имеют минимум1024 по горизонтали, старые UUID не удаляются. [Отчёт, 27 реальных dedicated запусков, матрица, source/JAR SHA и survival-checklist](implementation/generation295-20261025/REPORT.md), [очередь передачи](implementation/generation295-20261025/HANDOFF.md).

Java21 clean build2m40s,99852offline регрессий и8001resource/JAR PASS. Реально созданы два города/medium на3seed и разныхY, городищеFlat, две контролируемые избушки и два разных Баюна; 32 блока города и UUID Баюнов пережили настоящий restart. Cancel/partial reservation/native tickets0 проверены. Из27запусков15PASS,11FAIL,1startupFAIL (включая исправленные fixtures и защитные отказы), не27PASS. Полное Survival/MP/client render/случайные частоты/исходные3маркера владельца NOT_RUN; universal claims/старые природные player blocks и полный rollback/resume не решены. #295/#275 фактически «В работе», OPEN/OWNER_PENDING. POLYMC_NOT_INSTALLED; основной инстанс/миры/JAR не менялись. Main не изменяется, следующие реализации не начаты.

## 25.10.2026 — #273: связки двух ритуалов (IN_PROGRESS)

В `hell` активация чаши/узлов фиксирует флаг и расход только после успешного изменения блока; pool принимает только0/1. Оба обряда сразу переводят прежний encounter в READY и открывают загруженные seal blocks; старые выполненные flags учитываются без нового обряда. Водная дверь/предмет в руке и RU/EN книга ведут через хранителя к очищению/личному постаменту; описан уже существующий рецепт двух частей + Око Мора. Геометрия, баланс, loot tables, registry IDs, #271/#275/#272/#274 сохранены. [Матрица, source/offline evidence, JAR/SHA и два подробных Survival-чек-листа](implementation/rituals273-20261025/REPORT.md).

Java21 clean build3м14с и201новая native offline проверка + регрессии PASS; actual combat/проходы/лут/выдача/перезаход/MP NOT_RUN. Игровых запусков0, POLYMC_NOT_INSTALLED, OWNER_PENDING, #273 OPEN/«В работе». Main не изменяется. Передача #278 подготовлена, реализация не начата; #276 баланс отдельно.

## 25.10.2026 — #274: source performance fixes (IN_PROGRESS)

В `hell` устранены повторный полный перебор Fill при растеризации Чуди, полный rebuild индекса при одной изменённой записи, лишний suffix rollback и повторная по высоте формула кожи горы. Worker Чуди получает фиксированный план и cooperative cancel; добавлена очистка владельцев/очередей при unload. Геометрия/ресурсы/registry IDs, #271/#275/#272 сохранены. [Причины, before/after, матрица, offline gates и подробный Survival/MP чек-лист](implementation/performance274-20261025/REPORT.md).

Java21 clean build/точные counts/SHA — в квитанции отчёта. Gameplay startup/save/MSPT/FPS, actual tickets/NPC/restart и MP NOT_RUN; игровых запусков 0. POLYMC_NOT_INSTALLED, OWNER_PENDING, #274 OPEN/«В работе». Vanilla locate fallthrough и атомарный postProcess остаются потенциально тяжёлыми: всех причин задержек без профиля не установлено. Main не изменяется. Передача #273 подготовлена, реализация не начата.

## 25.10.2026 — #272: доступ к Яге и сюжетный прогресс (IN_PROGRESS)

В `hell` по уточнению владельца победы Survival над Лихом/Тугарином из яйца/команды теперь засчитываются без encounter; непустой природный ledger по-прежнему проверяется. Creative-доступ сохраняется синхронно после успешного переключения, без combat/trust rewards. Добавлены причинный credit вспомогательного урона, четыре сохраняемых состояния поиска и обновление кеша книги; первая беседа признаёт прежнюю победу один раз. Отношения Яги читаются из canonical Overworld. Геометрия/резервации #275 и встречи #271 сохранены. [Причина, матрица, native offline evidence, JAR/SHA и ручной сценарий](implementation/yaga272-20261025/REPORT.md).

Java21 clean build и native #272/#271/#275 gates PASS (границы в отчёте); настоящие death/event/mixin/network/NPC/GUI/перезаход/сюжет — NOT_RUN. Runtime0, POLYMC_NOT_INSTALLED, OWNER_PENDING. #272 OPEN/IN_PROGRESS, Project Status не менялся, main не изменяется. #274 передан отдельным будущим пакетом, реализация не начата.

## 25.10.2026 — #275: транзакция Баюна и сохранность мест (IN_PROGRESS)

В `hell` исправлены подготовка места Баюна, откат отказа NPC и обновление карты; добавлены общие construction reservations Яги/Баюна и исправлено ownership записи Cursor при исключении после setBlock. Сохранены прежние family names горы и двух святилищ при NBT reload. SiteChunks учитывает всех фактических держателей билетов и закрывает их при остановке. Высокое Сердце Навьи не отклоняется из-за лавы ниже всей конструкции. Чудь получает честное сообщение об ошибке; её геометрия, #271 и доступ #272 сохранены. Spacing/биомы/лут не менялись.

[Отчёт, каталог 21 структур/30 мест, 5-seed расчёты, JAR/SHA и survival-чек-лист](implementation/structures275-20261025/REPORT.md). Java 21 clean build, 8001 resource/JAR и native offline gates PASS; настоящая генерация/NPC/MP/restart/tickets/MSPT — NOT_RUN. Игровых запусков 0, POLYMC_NOT_INSTALLED, OWNER_PENDING. #275 OPEN/IN_PROGRESS, Project Status не менялся. Следующий отдельный пакет — #272, performance #274 отдельно. `main` не изменяется.

## 25.10.2026 — #271: сохраняемая первая возможность Сивки и исправления natural spawn (IN_PROGRESS)

В `hell` реализован принятый владельцем вариант A: персональные активные дни, q=2–15%, pending к дню40, сохранение RNG/UUID/прогресса, одна ограниченная серверная попытка поиска и отсутствие локальных дублей. Natural predicates четырёх мини-боссов и perch Жар-птицы проверяют actual body вместо завышенного clearing; Волк повторяет временные отказы и вытесняет старые ключи из кеша8192. Прежние бои, ordinary Лихо/Тугарин, Мор, биомы/веса, ресурсы и генератор структур сохранены. [Полный отчёт, таблица124ID, параметры и сценарии](implementation/encounters271-20261025/REPORT.md).

Java21 clean build2м25с,8001ресурс/JAR,39253native offline checks и2млнисторий PASS; это не игровой тест. Runtime0/GAMEPLAY_NOT_RUN, POLYMC_NOT_INSTALLED, OWNER_PENDING. #271 OPEN/IN_PROGRESS; Project Status не менялся. Структурные зависимости переданы отдельному #275, его код в этот пакет не входит. `main` не изменяется.

## 25.10.2026 — офлайн-стабилизация #272/#271, IN_PROGRESS

В hell добавлены отдельный creative-допуск Яги на существующем TaleData stage и push Entries/Hints после трёх прежних побед; game-mode callback читает фактический режим следующим тиком. Combat credit/trust/награды и очередь строительства сохранены. У Сивки placement проверяет фактический AABB/коллизии/жидкость вместо пустого3×4×3; веса и спорные гарантии не менялись. [#272: контракт/QA/receipt](implementation/stabilization272-20261025/REPORT.md), [#271: таблица/остаток](implementation/stabilization271-20261025/REPORT.md). Runtime0/GAMEPLAY_NOT_RUN, POLYMC_NOT_INSTALLED; Issues OPEN, Project Status не менялся, main не изменяется. Технические результаты сборки — в receipt отчёта; это не игровая приёмка.

## 25.10.2026 — публикация HELL2.0.1 для аудита, IN_PROGRESS

По прямому запросу владельца публикуются текущие исходники и открытые дефекты в hell, без изменения main и без заявления готовности. [Входная точка аудита, метрики и ограничения](implementation/runtime-repair-20261025/AUDIT_INDEX.md). Новый сбой доступа к Яге и creative-требование пока не исправлены.

## 25.10.2026 — новый FAIL доступа к Яге; список остатка за два дня

Владелец сообщил: послеcreative→survival и убийства Лиха дорога к Яге закрыта. Причина не установлена, прежний строительныйPASS её не опровергает. Новое требование: вход в креатив сразу открывает доступ к Яге без боевых/сюжетных условий. В этом запросе только фиксация, реализации/новогоJAR нет. [Полный список16пунктов и критерии](implementation/runtime-repair-20261025/OWNER_REMAINING_FIXES.md).

## 25.10.2026 — HELL отвергнут владельцем; runtime-ремонт IN_PROGRESS

Выполнены27реальных запусков, включая провальные и2клиентских. Исправлены воспроизведённые ошибки подготовки чанков/bootstrap, реально построены Яга/Баюн/medium/city/great Чуди и4больших места рядом. Первый запрос крупных мест всё ещё занимает8–63с; полный клиентский replay заблокирован захватом окна. Высота/туман/полный набор встреч не приняты; По прямому запросу владельца промежуточный2.0.1-HELL установлен в тестовый HELL с backup/SHA/duplicate0; это IN_PROGRESS, не итоговая сдача. [Матрица, измерения и ограничения](implementation/runtime-repair-20261025/REPORT.md).

## 25.10.2026 — объединённый тестовый HELL: IN_PROGRESS

Основная папка D:/slavic-myths переведена в hell; объединены Ад/Навья роща и исправления поиска/генерации/встреч/UI Яги. Clean build2м12с и8001ресурс PASS; runtime0. Main не изменяется. Hell опубликована; обе лишние рабочие копии удалены после полной архивации. Отдельный PolyMC «Slavic Myths 2.0.0 - HELL - TEST» установлен с backup/SHA/duplicate0; прежний инстанс и миры сохранены. [Матрица и receipt](implementation/hell-consolidation-20261025/REPORT.md).

## 25.10.2026 — Навья роща #251–#257: IN_PROGRESS, исходники и JAR

В отдельной ветке `hell` реализованы31 inventory block, одна порода/флора/три формы деревьев, четыре существа и Сторож с собственными ModelPart/UV/PNG/анимациями; утверждённый владельцем лут,35 vanilla sound aliases. Последним подключён новый biome/геология/три яруса/редкое Сердце/спавн. Java21 clean build и source/JAR gates PASS; native climate/profile5seed и source renders не являются игровыми тестами. Запусков0; runtime/прохождение/баланс/точное сравнение с недоступными оригинальными PNG/прослушивание/приёмка NOT_RUN. Все семь карточек фактически «В работе», Issues OPEN. PolyMC не изменён, POLYMC_NOT_INSTALLED; commit/push нет. [Матрица и доказательства](implementation/navya-grove/REPORT.md).

## 24.10.2026 — HELL: первый пакет генерации Ада, IN_PROGRESS

По прямому поручению владельца создана отдельная ветка `hell` от275ecaa5; текущие незакоммиченные изменения другой рабочей копии сохранены. Project9 #243/#244/#246/#247/#248 фактически «В работе». Владелец подтвердил224 слоя генерации0…223 с бедроком, строительство256, logical_height224 и прежнюю лаву32. Реализован экспериментальный профиль широкого нижнего рельефа/высокого свода; biome surface/climate/структуры/лут сохранены, диапазоны gold/quartz/debris закреплены на прежних Y. Новые биомы и аура вне scope. Native A/B на5seed проверяет только базовые колонки; полноценные worldgen/порталы/старый мир/MSPT/owner acceptance NOT_RUN. Версия кандидата2.0.0-HELL, PolyMC не изменён, игровых запусков0; без commit/push/закрытия Issues. [Матрица и ограничения](implementation/hell-patch1/REPORT.md). Java21 clean build47s/7747packaged resources PASS; JAR SHA348473ec…5a48374. Полные результаты в отчёте.

## 25.10.2026 — генерация, встречи и интерфейс Яги: IN_PROGRESS

По прямому фидбеку владельца изменяются fallback площадок Яги/Чуди/Баюна, подстройка естественного ландшафта и чрезмерные ограничения встреч Волка, Сивки, Жар-птицы и Мора; малые лагеря реже. Добавлены разделение текста/кнопок Яги и однократное признание прежних побед, исправления спавна мини-боссов и естественного Мора Лиха. Clean build 1м21с, проверки изменённой логики и 7740 ресурсов PASS; UI/частота встреч/MSPT runtime NOT_RUN. Игровых запусков 0, PolyMC не изменён. [Матрица и точные изменения](implementation/generation-encounters-20261024/REPORT.md).

## 24.10.2026 — создание мест по запросу: IN_PROGRESS

Исправления книги/создания мест IN_PROGRESS. Удалены синхронные поисковые загрузки Яги и болотных объектов, старый поиск Чуди заменён очередью фактической постройки. PolyMC не изменён; runtime/MSPT NOT_RUN. [Матрица и ограничения](implementation/on-demand-generation/REPORT.md).

## 24.10.2026 — документация и очистка без смены 1.9.7.66

README ветки release и MOD_CONTENT описывают существующие механики; каталог и управление актуализированы. Игровой Java-код и ресурсы совпадают с принятой 1.9.7.66; По последнему поручению версия сохранена, PolyMC и запущенная игра не изменяются. Старые ветки удаляются только при сохранности коммитов, локальные QA-артефакты очищаются отдельно от пользовательских данных. Игровых запусков в этой итерации нет.

## 24.10.2026 — owner-accepted second-version baseline 1.9.7.66

The owner explicitly accepted installed 1.9.7.66 and authorized branch release. Source commit ba098d0da6b7c0658b9d0cc001ef236ac1d064d5; cleanup preserves production source/resources. Known worldgen stalls and unrun full campaign cases remain documented; individual Issues are not closed automatically. [Cleanup](implementation/cleanup-2.0-20261024/REPORT.md), [installation](implementation/polymc-19766/installed.json).

## 24.10.2026 — 1.9.7.66: four heroes, cooperative help, installed candidate

All four personal help gates and a server-wide cap of two summons implemented in existing TaleData/StoryPolicy. Final native tests 13/13, two TCP clients and full dedicated-server restart checked; clean build 42s and 7740 resources PASS. Test candidate 1.9.7.66 installed with backup/SHA; source remains uncommitted and is captured by manifest/patch. New Chud assignments/visits/rewards and GUI scale captures checked; first-arrival worldgen stalls remain. Owner acceptance pending, Issues OPEN. [Matrix](implementation/heroes-coop/REPORT.md), [installed receipt](implementation/polymc-19766/installed.json).

## 24.10.2026 — candidate 1.9.7.65, installation pending

Clean build 1m2s and 7734 production resources PASS. PolyMC installation blocked by running user Minecraft; installed version remains 1.9.7.64. Only Ilya has the new introduction/help gate; other three heroes retain previous conditions. No game launches or automatic commits. [Candidate receipt](implementation/polymc-19765/pending.json).

## 24.10.2026 — Ilya introduction: IN_PROGRESS

Approved NPC direction -> meeting -> help -> permanent summon implemented in existing TaleData/book/server gates. Final runtime NOT_RUN; no final PolyMC installation or automatic commit. [Evidence matrix](implementation/feedback19765/REPORT.md).

## 24.10.2026 — тестовая1.9.7.64: курганы #241 и инструменты Чуди #258

По прямому поручению владельца реализован отдельный пакет вне Ада: награды/души/руны курганов и четыре инструмента Чуди (фонарь, кирка режимов, мультиинструмент, подъёмник). Восстановлены18 отсутствовавших loot-маршрутов комнат Кургана V2, без геометрии/населения. Production source3b00acaf, clean build47s,7732 ресурса,50 289+11 731 offline checks; финальный native server8/8. Всего9 GameTest серверов+2 dev клиента с сохранёнными неудачными попытками. Клиентский свет14/сервер0 и снятие фонаря0 проверены в обеих руках;22 финальных реальных кадра предметов. В PolyMC установлен1.9.7.64: SHA/backup/duplicate0 подтверждены, не stable release. Реальный MP/шейдеры/полный перезаход новых предметов и приёмка владельца NOT_RUN; обе карточки «В работе», Issues OPEN. [Курганы](implementation/kurgan241/REPORT.md), [Чудь](implementation/chud258/REPORT.md), [installed receipt](implementation/polymc-19764/installed.json). Прежние записи ниже — хронология; чистка Git/публикация/Ада не выполнялись.

## 24.10.2026 — актуальные результаты #235/#237/#236: IN_PROGRESS

Все три Issue открыты, карточки Project9 «В работе», draft PR239 → PR259 → PR260. Реализованы оптимизация/редизайн книги, проводники/поручения, торговля и второй пассажир ступы, Ctrl+1/2/3. Финальный clean build и resources PASS; 13/13 native server tests, 48 финальных кадров книги, реальная NPC-генерация и перезапуск мира проверены. Всего 5 клиентов + 4 GameTest сервера, включая неудачные попытки. Новая версия в PolyMC не установлена; изменены только три сочетания клавиш с backup. Первый полный snapshot 51,99 мс, командная генерация пик101,39 мс; два настоящих сетевых клиента и полная боевая кампания NOT_RUN. Полной готовности и приёмки нет. [Актуальные матрицы, коммиты/PR, FPS/MSPT, SHA и ограничения](implementation/three-issues-20261024/REPORT.md). Более ранние записи ниже — хронология, не актуальный статус.

## 24.10.2026 — #235/#237/#236: последовательная реализация

Прямое поручение владельца: отдельные ветки/PR, после реализации разрешены disposable test worlds и проверки NPC/команд. Все три карточки Project9 добавлены; #235 В работе, #237/#236 Ожидание. Первый source пакет #235: кэши/layout/index/culling и session definitions, Java21 clean build1m35s,41271offlinechecks,7688resources PASS. Runtime campaign отложен до интеграции, не выдан за FPS PASS. [Матрица](implementation/optimization-19762/RESULTS.md).

## 24.10.2026 — первый пакет оптимизации в отдельной ветке

В codex/performance-20261024 EarthCamera повторно использует списки native entity query и один AABB; прежние формулы, радиус16 и NO_SPECTATORS сохранены, ссылки очищаются в finally. Java21 clean build59s, 7688 source/JAR resources и 820 offline native-query checks PASS. Это не замер FPS/MSPT. Runtime0, игровая приёмка NOT_RUN, карточка Project9 фактически «В работе». Изменения оптимизации не закоммичены и не установлены: PolyMC остаётся на main1.9.7.62. [Матрица и границы измерений](implementation/performance-20261024/REPORT.md).

## 24.10.2026 — интеграция main и тестовый кандидат 1.9.7.62

По прямому поручению владельца сохранены все локальные изменения и объединён origin/main. Тестовая 1.9.7.62 установлена: backup/SHA256/duplicate0 проверены; результаты и installed receipt: [отчёт](implementation/polymc-19762/REPORT.md). Приёмка прежних задач не заявляется. Minecraft/PolyMC/GameTest/server не запускать. После установки оптимизация ведётся отдельно от main.

## 24.10.2026 — посохи, зачарования и runtime-регрессия

Исправлены melee/enchanting трёх посохов: Грозовой 6 HP, Велеса 4, Морозко 8; поток Морозко 3 HP/импульс. Исправлены прицеливание Грозового и перехват близкого удара Морозко. Native ServerPlayer.attack проверил Силу/Остроту; финальный GameTest 11/11 PASS, 17 upgrade операций сохраняют компоненты, 14 throwing вариантов, 5 ягод с реальной костной мукой/сбором. Клиент: 45 реальных кадров. Всего 7 Minecraft процессов с сохранёнными неудачными попытками. Clean build 55s, 7688 packaged resources PASS; PolyMC/версия не менялись, commit/push нет. Ручная приёмка ожидается. [Матрица, измерения, кадры и JAR/SHA](implementation/staff-audit-20261024/REPORT.md).

## 24.10.2026 — все девять пунктов игрового фидбека

Реализован пакет всех 9 пунктов игрового фидбека: рецепты/серебряный блок, растения и саженцы, JEI/книга, масштаб Carcass, поиск Яги по старым достижениям и исследовательские поручения Чуди обоим NPC. Clean build 58s; 7055 native/JVM и 4853 Winters regression checks; 7688 source/JAR resources PASS, recipe conflicts 0. Minecraft/сервер/GameTest 0, игровая приёмка NOT_RUN. PolyMC не обновлялся, версия исполнителем не повышалась, commit/push нет. IN_PROGRESS до owner acceptance. [Матрица, evidence, JAR/SHA и ограничения](implementation/feedback-20261024/REPORT.md).

## 24.10.2026 — достижения v3 в актуальной рабочей копии main

Перенесены75 связанных карточек с четырьмя ветвями и серверными вехами побед; старые217 definitions/условия/награды и77 unlocks сохранены. Clean build1m45s;40326+314 pure JVM checks, два отдельных серверных прогона8/8 и реальная загрузка прогресса после перезапуска PASS. [Матрица, точный JAR и ограничения](implementation/advancements-main-port/REPORT.md). Визуальная и сюжетная приёмка владельца ожидается. PolyMC не обновлялся, commit/push не выполнялись.

## 23.10.2026 — ручная приёмка 1.9.7.60 и следующий пакет

По прямому решению владельца фактически переведены в «Готово» #217/#219/#220/#225/#226. [Основание и границы приёмки](implementation/project9-owner-acceptance-19760/REPORT.md). Новое поручение: #121 (защита актуальных оружейных ресурсов от отката старых генераторов) и #129 (итоговая техническая проверка брони после принятых #122–128). #129: 29 ID, 232 свежих native mesh-состояния, 13 исключённых аксессуаров сохранены; clean build44s и актуальный JAR/resources PASS, игровых запусков0, owner-only runtime NOT_RUN. [Матрица #129](implementation/issue129-current/REPORT.md). #129 и #121 фактически «Тестирование». #121: исправлены два legacy entrypoint и weapon-only вход Полоза; 7 entrypoints ×2 replay PASS, откатов0. [Матрица #121](implementation/issue121-current/REPORT.md). Новых художественных production-изменений и установки PolyMC в этом пакете нет.

## 23.10.2026 — PolyMC обновлён по запросу владельца

В тестовый профиль Slavic-Myths-1.21.1-Testing установлен 1.9.7.60; SHA256 установленного JAR проверен, предыдущий 1.9.7.59 сохранён в backup, дубликатов мода нет. [Точные пути, SHA256 и исходное состояние](implementation/polymc-19760/installed.json). При установке игра не запускалась; задачи остаются на ручной приёмке. Ниже сохранена история до установки.

## 23.10.2026 — #225/#226: замечания владельца исправлены и проверены runtime

Пакет: основания3фигур, именно ohotnichiy_rog, GUI/held/удар плети, непрерывная цепь/ядро кистеней и старый лишний рецепт, Сабля Воеводы по слизням. Нормальная генерация выявила #226: мебель и multipart helpers заявляли несуществующие block entities; исправлено без смены IDs/инвентарей/геометрии. Последний сервер38/38:10primary оружий,37native меню рецептов/74изготовления с расходом и antidupe,176multipart состояний; actual клиент обе руки/Steve/Alex,88GUI без spill. NORMAL seed2252026: курган/святилище, реальный отдельный процесс перезахода с сохранением3алмазов в ящике, BE warnings0. Всего21реальный Minecraft процесс с сохранёнными неудачными итерациями, не все объявлены PASS. [Полная матрица, границы и реальные кадры](implementation/issue225/REPORT.md). #225/#226/#217/#219/#220 «Тестирование», owner acceptance/Ready/close не заявлены. Local1.9.7.60 JAR SHA19202904f71fa92917c314d5743637b132478ee1be1d8f1a8f0d1489b606b0d6, clean build49s,7633resources/1811classes. В PolyMC прежняя установленная1.9.7.59, новой установки не было. Полная кампания/все структуры/MP/FPS/MSPT не проверены и не объявлены готовыми.

## 23.10.2026 — #101 NO_CHANGE; полный исполнительский объём #81 на проверке

Проверены10ресурсов:10KEEP/0FIX, фактические model/PNG/blockstate/UV и source формы трёх руд. Подтверждённого дефекта для перерисовки нет; production изменений0. Повторная проверка текущего JAR7631resources PASS, использован последний clean build46s без новых production правок. [Матрица и сводка шести пакетов #81](implementation/materials-81/Q101/REPORT.md). #101/#81 «Тестирование», owner-only runtime0; «Готово»/закрытие/PolyMC не заявлены.

## 23.10.2026 — #114: QA31 и сводный64-ID контроль

Проверены64actual model references,17ordinary/16elite/31legendary, порядок сохранён. В31QA-only трофеях30KEEP,1FIX: пух Жар-птицы после индивидуального blueprint. Ровно1production PNG; clean build46s иJAR7631resources PASS. [Поимённый аудит/матрица/actual slot sheets](implementation/trophy114/REPORT.md). #114 фактически «Тестирование»; #92 и игровые приёмки остаются открыты. Runtime0; новый локальный JAR не установлен в PolyMC.

## 23.10.2026 — тестовая1.9.7.59 фактически установлена в PolyMC

По прямому поручению владельца проверены #97/#100/#112/#113: все «Тестирование». Java21 clean build45s,7631 JAR resources и30canonical PNG PASS. Installed Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.9.7.59.jar, SHA256f211c5e5b00d626f1093b6e6ff1dcb7f87aa1051d520b155e939b71b08fd294b. Backup1.9.7.58, duplicate0. [Полная квитанция, source dirty manifest и матрица](implementation/polymc-19759/REPORT.md). Runtime запусков0. Новый delivery включает последние локальные исправления Финиста/метки, материалы и трофеи; это тестовый кандидат, не полная приёмка эпиков.

## 23.10.2026 — #112/#113: трофеи переданы на личную проверку

Из16ID исправлены11PNG;5actual специальных/удачных assets KEEP. Исходные geometry/UV/renderers сохранены. 26 scoped gates, production whitelist11PNG, clean build47s, JAR7631resources PASS. [Матрица/поимённый аудит/A-B](implementation/trophies112-113/REPORT.md). Runtime0/OWNER_ONLY, PolyMC не обновлялся, приёмка/закрытие не заявлены.

## 23.10.2026 — #97/#100: адресные материалы, ожидают личной проверки

Три уже реализованных металла KEEP. Изменены только янтарная block texture и6 наконечников, генераторы защищены от отката. 29 scoped checks, Java21 clean build48s и7631 JAR resources PASS. [Матрица/actual PNG A/B](implementation/materials-81/M97-T100/REPORT.md). Runtime0/OWNER_ONLY, PolyMC не обновлялся, версия1.9.7.58. Issues не закрыты, приёмка не заявлена.

## 23.10.2026 — принятые задачи перенесены в «Готово» по решению владельца

Фактически обновлены 22 карточки Project №9: #45, #98–99, #110–111, #122–128, #130, #140, #217–224. #140 добавлена на доску по прежней явной приёмке. Проверенное чтение: Готово41, Отменена56; остаток35 карточек, включая эпики и14 концептуальных (21 вне концепции). [Решение, доказательства, матрица и остаток](implementation/project9-owner-acceptance-20261023/REPORT.md). Иные статусы сохранены; Issues не закрывались. Исходные ограничения runtime не заменены утверждением о новых тестах. Никаких запусков, commit/push или установки PolyMC в этой итерации. Нижние OWNER_PENDING для перечисленных принятых карточек — история.

## 23.10.2026 — #223/#224: наведение Финиста и world-space метка исправлены

По новому поручению владельца устранены промахи в проверенных сценариях:91 matrix releases (натяжение8/18/30, обе стороны, дистанции8/16/24) и19/19 actual client-world hits. Поворот20–45° с горизонтальным разворотом назад; урон/скорость выпуска/расход/range48 сохранены. Скрытая цель не получает guide при выпуске; препятствие в полёте отключает guide, обхода стен нет. Метка над actual target, вне камеры/за стеной/при удалении скрыта; PNG прежний. Clean build59s,19 JAR/resource gates PASS, сервер26/26. [Матрица/неудачные итерации/ограничения](implementation/weapons87/issue217/finist223/REPORT.md). Новый local JAR SHA bf403773997cbfb652541713c7026277f76f8e9d8b2db72352326166ad530289. PolyMC не обновлялся. Нижние #223 FAIL/«баланс не менялся» описывают предыдущую диагностику до прямого поручения исправить.

## 23.10.2026 — #217 runtime и исправления, без новой установки PolyMC

#218–#222 исправлены локально:88 компактных GUI моделей, направленный вперёд удар плети с неизменным reach6, поверхности щита/рога, legacy projectile heat и тонкая V-тетива арбалетов. Java21 clean build52s,18 JAR/resource gates PASS; последний настоящий сервер26/26 и клиент144 кадра. Финист19 стрел: прямые/10° попадания при8/18/30ticks, большие углы дали промахи; #223 остаётся «Доработка», баланс не менялся. [Матрица и ограничения](implementation/weapons87/issue217/REPORT.md). Source1192e229 + сохранённый dirty snapshot, commit/push/merge нет. Новый build JAR SHA1bb4cdf7b2fd5c92956e481e538e07e8229cefa893acc95f9fe3a7b8a8756149; в PolyMC прежний P53 SHA5a4a58c61599978bf80ec517ee1642defb20dd60b6f1b1d31e01c7a5af40463e. Issues не закрыты. Нижние записи о нуле запусков/отсутствии Project write — история до разрешения владельца.


## 22.10.2026 — актуально: локальный оружейный пакет P53, тестовая 1.9.7.58 установлена

[Полный отчёт/матрицы/ограничения](implementation/weapons87/continuation53/REPORT.md), [installed receipt](implementation/weapons87/continuation53/installed.json). Реализованы оставшиеся 12 объёмных ножей/сулиц (#173), отдельная гибкая плеть с настоящим хватом (#190), реальные player ModelPart-позы для 52 ID (#162/#165), five-face ordinary spear surface fix (#165/#170), успешные cast swings трёх посохов, одобренные C reach4,25 и D рецепт Велеса. Реальный rollback восьми assets генератором устранён; пять безопасных weapon entrypoints проверены дважды без semantic/pixel drift. Общий исторический audio bootstrap не запускался; весь #121 не объявлен завершённым.

Java21 clean build PASS (1m09s), 23 активных source/JAR/resource gates PASS; 7 626 packaged resources, 1 808 Java21 classes, 68 native attribute IDs. Сборка **1.9.7.58 фактически установлена** только в Slavic-Myths-1.21.1-Testing, SHA256 `5a4a58c61599978bf80ec517ee1642defb20dd60b6f1b1d31e01c7a5af40463e`; backup1.9.7.57 есть, duplicate mod JAR0. Источник `1192e229c4aa9166b53661f71f5ca15299f3537e` **+ незакоммиченные изменения**, точный source manifest в отчёте. Minecraft/PolyMC/GameTest/server запусков0. Это прямо запрошенный тестовый кандидат, не final #87 release. Полный #87/#191 OPEN/OWNER_PENDING; исполнительские ограничения матриц остаются явными.

Project №9 является источником очереди. Поля Project фактически не менялись Codex: нет write capability/scope и browser auth. Нельзя считать рекомендацию «Тестирование» осуществлённым перемещением. #98/#99 уже наблюдаются на доске в «Тестирование» вследствие внешнего изменения; их 12 PNG и принятые владельцем головы #140 сохранены. Ни одна Issue не закрыта; новых commit/push/merge не было. `origin/main`61f604e2 получен fetch и уже предок рабочей ветки. Нижележащие P52/старые C/D pending записи — история, не актуальные запреты на уже одобренные C/D.

## 22.10.2026 — P52 #161 особые арбалеты, MOR/FIRE hit cues
В continuous ветке PR #216 появился отдельный не дублирующий ранее сделанное source пакет: после принятого попадания стрелы режима морока 8 SOUL, огня 8 FLAME. Existing muzzle P44 and StormVfx untouched; ammo/damage unchanged; P52 source/JAR QA pending, Minecraft NOT_RUN. Issue #161 full scope OPEN. Project Status «на тестировании2» connector не может установить.

## 22.10.2026 — P51 непринятые дефекты (#140, Finist)
В непрерывной ветке PR #216 исходно исправлены near-antiparallel 6°/tick в `FinistArrow` и непосредственное движение двух native зрачков Тугарина в поставленной голове; source/math+JAR guard `tools/verify_weapons87_p51.py`. Перед запуском CI все проверки P51 PENDING, Minecraft/PolyMC NOT_RUN, приёмка владельца отсутствует. Непринятая анимация глаз Ским-зверя не скрывается. `main` и owner-accepted предметы не менялись.

## 22.10.2026 — P31/32 final verified source and CI receipts
- **P31 #163:** crossbow FP visual recoil `WeaponHands` on same ItemStack charged→empty, 7-tick smooth client-only wrist+gun kick, per-hand state reset on switch/world, not ammo/server. [PR #202](https://github.com/Deserag/slavic-myths/pull/202) merged `4247c95ff8f8b82cb875161edf960686caaaa2af`. [Java21 CI 39775007057](https://github.com/Deserag/slavic-myths/actions/runs/39775007057) **PASS**: native 12 crossbow model states /48 handed pivot cases, 294 packaged resources, actual four-phase item-source/UV storyboards. JAR `slavicmyths-1.9.7.56.jar` SHA256 `d920d52874db3d9d1d811756ff10177f0c9b94aec3db7c9edcf73eb34f86790f`.
- **P32 #121:** isolated canonical source generator run **twice** in disposable temp sandbox; no actual project asset writes, 68 active weapon-ID models, 16 protected resources (#115/#163/#184/#185/#189). Byte differences **30** (12 item JSON +18 flail PNG), but JSON **semantics** and PNG **pixels** drift **0**, double-run **idempotence** drift **0**. Legacy `audit-68.json` SHA109 and old flail applied-manifest SHA51 still archived baseline differences (not silently overwritten). [PR #203](https://github.com/Deserag/slavic-myths/pull/203) merged `f1b9756454becb2cb424d6da272a68a9ab34ff6d`; [Java21 CI 39776454779](https://github.com/Deserag/slavic-myths/actions/runs/39776454779) **PASS** (68 ID,294 JAR resources, generator source gate), same JAR SHA `d920d52874db3d9d1d811756ff10177f0c9b94aec3db7c9edcf73eb34f86790f`.
- #121 remains **partial** (only canonical `weapons87_flails --apply` chain replayed, other historical standalone entrypoints and gameplay still pending); #163 remains partial (TP player animation/MP owner test pending).
- **Issues count:** 43 child issues #115–#121 + #157–#192, 42 OPEN /1 CLOSED (#184), plus parent #87 OPEN as of audit. No Issues closed automatically. Owner gameplay/PolyMC **NOT_RUN** in assistant session. Unapproved C/D/#190 untouched, #191 last.


## 22.10.2026 — пакет №31, #163 FIRST-PERSON CROSSBOW RECOIL, runtime not run
Новые source изменения находятся в feat/weapons87-p31-crossbow-firstperson-recoil, исходный main 6e28808da46078397bb2327b2c99b158335e1e4f. В client WeaponHands отслеживаются две независимые руки и существующий ItemStack, фиксируется реальный переход CHARGED→not charged без active item use, начинается 7-тиковый плавный kick (sin πt/7 × (1-.25t/7)), направленный назад и вверх вместе с визуализируемой рукой и оружием. Сброс при смене Stack, player или world; только client, ноль новых пакетов и server/abilities/charge/weapon stats изменений. Source guards + native existing 12 crossbow asset checks, owner screenshot fix preserved. Source four-phase storyboard from real JSON+UV, but НЕ Minecraft. GitHub Actions Java21 clean build/JAR checks в работе. Не считать весь #163 готовым: third-person reload/recoil, Steve/Alex/MP/charge variants ручная приёмка всё ещё NOT_RUN. Читай PACKAGE-31.md и tools/verify_weapons87_crossbow_recoil.py. Следом #121 generator reversion audit, #161 unique VFX and #162/#165 animations. Баланс C/D/#190 не трогать, #191 last.

## 22.10.2026 — #87 пакеты26–27 merged and Java21 PASS

### Проверенный итог и merge в main — 22.10.2026
- PR [#198](https://github.com/Deserag/slavic-myths/pull/198) merged `4fad4fa2bd231aa07d2bb0cb5c4d68fb2e395686`, gated source HEAD `4f5a3b3c880d94261f12c0a0c246903848cd5425`.
- [CI 39764351561](https://github.com/Deserag/slavic-myths/actions/runs/39764351561) **PASS**, Java21 clean NeoForge build 1m29s, 68 weapon ids source PASS, 294 source-assets byte-verified inside JAR PASS, Thunder Axe source guards PASS.
- Production output `build/libs/slavicmyths-1.9.7.56.jar` SHA256 `406a1027e06f0d6bc30c382e471bb814b7f3624ddd5320fef96cf3b00802d7b9`; downloadable **only as CI artifact**, not installed in PolyMC, client/server NOT_RUN.
- History: `audit-68.json` **109** stale SHA entries, `blueprints/flails/applied-manifest.json` **51** stale SHA entries, no missing historical paths, old full #87 manifest verifier remains **FAIL**; reports record drift rather than changing resources. Numbers reflect source lineage, not a claim of 160 combat bugs.
- Issue #87/#157/#160/#192 **OPEN**, OWNER_PENDING. Approved A/B stats preserved; C/D/#190 blocked, #191 not started.

## 22.10.2026 #87 P26–27 audit68 + ThunderAxe (#157/#160)
Текущий work branch: source audit68, archive SHA drift 109+51 без изменений исторических файлов; исправлен Thunder Axe proc +1 HP в серверном `LivingDamageEvent.Pre` вместо второго `hurt` после iframe, сохраняя 20% и 60 ticks. Java21 CI PENDING; без запуска игры/PolyMC. Детали `docs/implementation/weapons87/PACKAGE-26.md`, `PACKAGE-27.md`.

## 22.10.2026 — #87 P20–25 опубликованы, scoped Java21 tests прошли, owner runtime NOT_RUN
Пакеты20–25 в main: #184 сабля Калина FIX (6→34 cuboids, отдельная пиксельная палитра); #185 копьё победителя FIX (4→14 cuboids, наградное острие); #186 лук Финиста KEEP (3 состояния), #187 громовой топор KEEP (24 элементы), #188 громовое копьё KEEP (30 элементов); #189 копьё омута GUI FIX (14 cuboids KEEP). Production изменения ограничены 3 модельными JSON,1 новым PNG и связанными generator/test файлами, игровые классы/статистика не менялись. PR #196 main merge `22613105859a23294b366d33520edd25471ce54b`. CI [39761931287](https://github.com/Deserag/slavic-myths/actions/runs/39761931287), JAR version 1.9.7.56 SHA256 `0f5faaf8fc7d968a526a192a5503ca2c8b9ebbf8c289c5ef73038bb24cd16167`. Исторический flail #87 gate остаётся BLOCKED на неизменённом старом `stone_flail_held.json`, 6 stale SHA в archived source list, новые scoped checks PASS. `docs/implementation/weapons87/NEXT.md` и PACKAGE-20…25 — точная передача. Следующее: #157/#192 audit, #160–165; #190/C/D не утверждены, #191 не начинали, owner acceptance pending.

## 22.10.2026 — #87 P20–25: три адресных art/GUI FIX и три source KEEP (тех. CI в работе)
Пакеты20–25: #184 Kalin 6→34 элементов, отдельный 16px atlas и изгиб сабли FIX; #185 Victor 4→14 элементов и наградная втулка FIX; #186 лук Финиста с3 pull-моделями KEEP; #187 thunder_axe24 элементов KEEP; #188 thunder_spear30 элементов KEEP; #189 pool_spear сохранил14 деталей/текстуры/руки, только центрированный GUI FIX. Все before/draft для изменённого подготовлены отдельным предварительным коммитом; проверка CI в ветке, игровой запуск/PolyMC NOT_RUN. C/D/#190 не тронуты; #191 не начинали. Подробные FILE/BEFORE/AFTER/QA: `docs/implementation/weapons87/PACKAGE-20.md`—`PACKAGE-25.md`; branch `feat/weapons87-p20-p25-unique-art`. Производственный JAR нельзя считать игровым runtime. Новые registry IDs/данные сохранений/weapon stats не изменялись. #87 OPEN.

## 22.10.2026 — #87 пакеты14–19, частичная разработка оружия без игровой приёмки
В main merge `9910e5ca17e5bbf5fa14dff55fa371640fe93398` попал адресный #115 FIX third-person grips всех 6 состояний двух арбалетов, только transform metadata; 5 индивидуальных арт-аудитов #175/#176/#177/#179/#183 source KEEP без перерисовки. Java21 clean build [39757888300](https://github.com/Deserag/slavic-myths/actions/runs/39757888300) PASS, version 1.9.7.56; runtime/PolyMC NOT_RUN. #87 и дочерние Issues OPEN. Далее #184 сабля Калина, подробнее `docs/implementation/weapons87/NEXT.md`.

## 22.10.2026 — #140 FX-2: все шесть трофейных голов получили анимации · 1.9.7.56
Вторая партия #140 завершена по source и CI: челюсть/ус Тугарина, челюсть/грива Скима, оскал/ухо Проклятого волколака, каменная челюсть/надбровья Крупного Подземника; дополнены отличающимися редкими частицами. FX-1 для обычного и Морового Лиха сохранён. Все эффекты — только на установленных головах, без изменения original ModelPart/UV/PNG, gameplay, registry, drop, моделей существ и отдельной головы Полоза. [Java21 CI 39754403311](https://github.com/Deserag/slavic-myths/actions/runs/39754403311) PASS: NeoForge clean build, 1149670 native assertions, JAR `slavicmyths-1.9.7.56.jar` SHA256 `95b35a0cd1ffa9961cd2206a95f95207bfe2e2b9f37513b04472cede24de2747`. `#140` остаётся OPEN — visual acceptance, перезаход, FPS и падение loot в реальной игре NOT_RUN. [FX-2 отчёт](implementation/trophy140-fx/P2_REMAINING.md).

## 22.10.2026 — #140 FX-1: трофеи Лиха получили визуальную анимацию (1.9.7.55)
По прямому поручению владельца реализована первая партия эффектов шести голов: обычное Лихо моргает настоящим `upperLid`, периодически светится native `irisRing` и выпускает редкую локальную SOUL-частицу; Моровое Лихо пульсирует native `morEye`, светится и выпускает локальную CRIMSON_SPORE-частицу. Только установленный трофей, никакого влияния на игрока или боевые эффекты. Ресурсы, UV, дроп и другие четыре головы/Полоз не менялись. [CI 39751552635](https://github.com/Deserag/slavic-myths/actions/runs/39751552635): Java 21 clean build PASS, 173903 native checks PASS; JAR 1.9.7.55 SHA256 `b5618fe6ce93b72ce126a8953e8574f209e37b072e20dbf99f24a3e06029b75d`. Owner visual test/игра NOT_RUN; #140 OPEN, В работе. [Отчёт](implementation/trophy140-fx/P1_LIKHO.md).

## #193 — Лихо: объединённый source QA и Java 21 build · 22.10.2026

После отдельных коммитов P0-1 (`33fba1c`), P0-2 (`69ad290f`), P1-3 (`8ee3ac3b`) и диагностики P1-4 (`874f6ce8`) в ветке `feat/likho-193-forest-spawn` выполнены [проверки `957b60a1`](https://github.com/Deserag/slavic-myths/commit/957b60a1d376e426f82feef69d69b332ba583ab8). Java 21 pure-policy tests **61489 PASS** на 30720 сочетаниях seed/region. GitHub Actions [run 39741599767](https://github.com/Deserag/slavic-myths/actions/runs/39741599767) **SUCCESS**, NeoForge `./gradlew clean build` **PASS**, production `slavicmyths-1.9.7.53.jar` создан (SHA256 `ba866e16acf2c305bafac400bdb8f16b7f33bfedbe13871dd9a2b16baea79d49`), artifact GitHub Actions доступен временно. **Игровых запусков Minecraft/PolyMC/GameTest/сервера = 0, JAR не установлен.** Результаты source и build НЕ являются owner runtime QA. Issue [#193](https://github.com/Deserag/slavic-myths/issues/193) остаётся **OPEN / Доработка**, P0-1 QA/P0-2 QA/P1-3 QA/P1-4 runtime/P2-5 in-game/FINAL **не приняты**, никаких изменённых HP, моделей, registry ID или автоматического повышения raw damage. Измерение DPS требует `-Dslavicmyths.likho.dps=true` и iron/iron / diamond/netherite испытаний. План ручного прогона: [QA_HANDOFF](implementation/likho193/QA_HANDOFF.md), [P1-4_DPS_AUDIT](implementation/likho193/P1-4_DPS_AUDIT.md). **Без merge, owner-accepted и закрытия Issue.**

## #93: боевые механики Лиха · 21.10.2026

По прямому поручению владельца после технической сдачи #140/#141 реализован candidate1.9.7.52: только Likho LivingEntity victims/защита целей, приоритет существующих навыков при окружении, phase/focus commitment, заражённые зоны Лиха и opt-in bounded trace. HP350/обычная, mor_health×2 (700 без иных модификаторов), атрибуты, NBT, dimensions, все модели/UV/PNG/эффекты/звуки/registry сохранены. Ветка зон Овинника оставлена дословно, Тугарин сохраняет прежний scheduler. Production policy/exposure145 checks и native HP/NBT1545 checks PASS; боевые runtime-сценарии NOT_RUN / OWNER_PENDING, запусков0. [Причины, файлы, матрица, проверки и сценарии](implementation/likho93/REPORT.md). Сборка/JAR/source manifest: [verification.json](implementation/likho93/verification.json); установку следует считать состоявшейся только при наличии [installed.json](implementation/likho93/installed.json). Issue OPEN, рекомендуемый Status Тестирование, поле Projects технически не изменено; owner-accepted не добавлялся. #142 не начата и требует отдельной приёмки #93/поручения. Source не commit/push.

Тестовая **1.9.7.52 установлена** в PolyMC 1.21.1 Testing: SHA256 `1577b7abb0916323a8f0507d1a2d44a70a72108dcf732e0c9987a7b0b8a4bf38`; предыдущая1.9.7.51 в backup, дублей0, прочие моды неизменны. Точные пути/source: [installed.json](implementation/likho93/installed.json). Установка не означает runtime-приёмку.

## #140/#141: тестовая 1.9.7.51 установлена · 21.10.2026

Владелец подтвердил игровую приёмку декора #108. Реализованы шесть трофейных голов из настоящих ModelPart/UV и единое дерево `/slavicmyths dev generate <строение> [параметры]`; прежние обработчики и алиасы сохранены. Clean build, native-head offline, Brigadier offline, canonical resources и 7487 resource/JAR parity PASS. Тестовая 1.9.7.51 установлена в PolyMC 1.21.1 Testing, предыдущая 1.9.7.50 в backup, дублей нет; runtime Codex 0 / OWNER_PENDING. [Отчёт и матрицы](implementation/heads140-generation141/REPORT.md), [точная установка](implementation/heads140-generation141/installed.json), [команды](GENERATION_COMMANDS.md). Source base32d17334 + незакоммиченный snapshot, commit/push не выполнялись. Issues #140/#141 OPEN; рекомендуется Тестирование, поле Project технически не изменено. Следующее прямое поручение владельца — #93; #142 только после отдельной приёмки #93.

## Установлена тестовая1.9.7.50 · 21.10.2026

По прямому поручению владельца candidate #108 установлен в PolyMC Slavic-Myths-1.21.1-Testing;1.9.7.49 сохранена в backup, дубликатов нет, остальные моды неизменны. Source/SHA256/backup: [installed.json](implementation/decor108/installed.json). Запусков0 / IN_GAME_OWNER_PENDING, #108 OPEN. Эта запись заменяет прежний NOT_INSTALLED для1.9.7.50; новые изменения source не commit/push.

## #108: исправления по игровой проверке · 21.10.2026

Владелец принял древесину #106/#107 и разрешил закрытие; #107 закрыта; #106 закрытие откатила GitHub Actions (нужна owner-accepted от владельца). Замечание pine_log перенесено в #108. Исполняется обновлённая #108: двухблочный манекен, ящик27 слотов с постоянной BE, coplanar/UV fixes, адресный бытовой pixel art и аудит131 недревесных записей decor. Candidate1.9.7.50; runtime0 / OWNER_PENDING, #91/#108 OPEN. [Scope, реальные source A/B, матрица и сценарии](implementation/decor108/REPORT.md). В PolyMC остаётся предыдущая1.9.7.49; новый candidate не установлен. Новые source изменения не закоммичены.

## Тестовая установка 1.9.7.49 · 21.10.2026

По прямому поручению владельца текущий source snapshot опубликован и установлен в PolyMC 1.21.1 Testing. Clean build / 7444 resource parity / backup / no duplicate JAR PASS. Runtime0, IN_PROGRESS / OWNER_PENDING; Issue OPEN. Это тестовая сборка незавершённой цепочки. [Точная установка, SHA256, source commit и проверки](implementation/delivery-19749/REPORT.md). Исторические POLYMC_NOT_INSTALLED ниже относятся к прежним пакетам.

## Декор: все шесть семейств и производные поверхности · 21.10.2026

По новому явному поручению владельца продолжены все породы без остановок
после каждого пилота. Проверены85 catalog records;51 PNG change (49 FIX +2
недостающих active apple sign paths),17 поверхностей KEEP. Согласованные3
PNG липы и все торцы сохранены, кора сосны/рябины KEEP. Обновлены собственные
дверные/люковые/sign текстуры;277 прямых model consumers и89 item renders
проверены offline. Геометрия,Java,data,registry/recipes/NBT unchanged.
Java21 clean build49s и7444 production resource/JAR parity PASS; запусков0.
[Общий результат,матрица,сборка](implementation/decor106-all-woods/REPORT.md).
IN_GAME_OWNER_PENDING / Issue OPEN; рекомендовано «Тестирование» для #106,
Project write недоступен. POLYMC_NOT_INSTALLED. #109 V2 не менялся.
Следующая запись — исторический пилот, его остановка перед ивой отменена
последним прямым поручением владельца.

## Декор #91 → #106: пилот липы · 21.10.2026

По новому поручению начат первый пакет #106: исправлены только linden_planks,
linden_log и linden_stripped_log; native16px и исходные палитры сохранены.
Оба торца KEEP. Геометрия/UV/registry/gameplay не менялись. 14 timber item/state
цепочек проверены offline, есть source A/B1×/3×/2×2/four views.
Java21 clean build48s, ресурсы/JAR PASS; запусков0, POLYMC_NOT_INSTALLED.
[Отчёт/точная сборка/ручные сценарии](implementation/decor106-linden/REPORT.md).
#106 IN_PROGRESS / PILOT_OWNER_PENDING: по ТЗ после принятия липы идёт ива;
остальные семейства, #107–109 не исполнялись. Project write доступа нет.

## #111 — обновлённое решение реализовано · 21.10.2026

9 FIX +1 KEEP в полном scope #111; IN_GAME_OWNER_PENDING, Issue OPEN.
Блокер pearl_fragment снят новым явным решением владельца: адресно заменены
3D cuboid/UV на generated item с самостоятельным32px плоским сколом.
Уточнены широкая пластина атамана и змеиная печать; остальные удачные FIX/KEEP
сохранены. [Актуальные A/B, source renderer и сборка](implementation/trophy111-refinement/REPORT.md).
Java21 clean build56s и production resource/JAR parity PASS; запусков0,
POLYMC_NOT_INSTALLED. Рекомендуется «Тестирование»; фактически Project остаётся
«Ожидание» из-за read-only доступа. #112–114 production не начинались.
Ниже — историческая запись частичного результата, её DESIGN_BLOCKED уже снят.

## Трофеи #111 / T2 — частичный результат · 21.10.2026

IN_PROGRESS / IN_GAME_OWNER_PENDING. Восемь PNG исправлены партиями T2-A/B/C,
`old_button` оставлена KEEP. `pearl_fragment` — существующая native3D модель,
для неё есть плоская схема B, но нет согласованного native3D/UV чертежа;
BLOCKED_NEEDS_3D_UV_DESIGN, модель/текстура сохранены. Княжеская печать входит в FIX.
После нового скриншота владельца пряжка и знак атамана также переведены в FIX.
[Результат, матрица и вопросы дизайна](implementation/trophy111/REPORT.md).
До решения по фрагменту не переводить #111 в «Тестирование». Issue OPEN;
Project mutations недоступны. Minecraft запусков0; POLYMC_NOT_INSTALLED.

## Трофеи #110 / этап T1 родителя #92 · 21.10.2026

Семь production PNG16×16 переработаны партиями3/2/2: прямой клык/звериный зуб/
тёмный крюк, низкая зола/вертикальный уголёк, ромбовидная чешуя/рваная кожа.
Модели и gameplay сохранены; пять исторических art-entrypoints делегируют
`tools/trophy110_art.py`. [A/B, матрица, точная сборка и ручные сценарии](implementation/trophy110/REPORT.md).
Технические gates проверяются в отчёте; runtime0 / IN_GAME_OWNER_PENDING.
Project доступен только для чтения: рекомендовано «Тестирование» после gates,
фактический Status не изменён. Issue OPEN; #111–114 не начаты. POLYMC_NOT_INSTALLED.

## Сияние и полные комплекты ХП · установленная1.9.7.48 · 21.10.2026

IN_PROGRESS / OWNER_PENDING. Устранено чрезмерное ослабление glint45: линейная
плотность, alpha64/100/140 и additive свет без широких ремней. Настоящие GAME кадры
0–3 рун на железной/Перуните/Полозе/драконе/рубахе; день/ночь, спина/бок, vanilla
reference/coexistence, надетый игрок в survival InventoryScreen. Это scripted QA,
не ручная приёмка владельца. #104 OPEN, #105 NOT_STARTED.

По явному решению владельца полные сеты: Перунит +5ХП, Полоз +14ХП, драконий +20ХП.
Существующие equipment events, transient IDs, без нового tick polling/лечения;
partial/mixed/clamp/no-stacking проверены на настоящем ServerPlayer.
Java21 clean build1m5s + resources/JAR PASS;5 dev-client запусков,4входа в отдельную
тестовую копию,0dedicated/GameTest; финальные сценарии PASS. MP/relog/death/dimension,
Fabulous/полный контекстный QA и performance NOT_RUN. Пользовательские миры сохранены.
[Полная матрица и runtime](implementation/armor-health-48/REPORT.md),
[installed path/version/SHA256/source commit/backup45](implementation/armor-health-48/installed.json).
Один активный JAR48,6прочих модов сохранены. Installed PolyMC runtime NOT_RUN.
Нет commit/push/закрытия задач; прежние записи «48 не установлена» исторические.

## Общая47 после параллельной работы · 20.10.2026

Броня #88/#122–128 побайтово сохранена из проверенной46 в общей47 второго агента.
[Source/JAR47 parity и архив](implementation/armor88-full/shared47-verification.json).
Все29 ID имеют A/B/SHA и матрицы; #129 acceptance BLOCKED_ON_OWNER_A1_A7,
игровые гейты NOT_RUN, никаких закрытий/owner-accepted. Clean46 выполнен здесь;
clean47 выполнен вторым агентом и его артефакт проверен отдельно. PolyMC этой
задачей не обновлялась; наблюдалась тестовая45 другого агента, не финальный релиз.

## #88 / #122–129 — source 1.9.7.46, приёмка ожидается · 20.10.2026

IN_PROGRESS / IN_GAME_OWNER_PENDING. Реализационные пакеты #122–#128 охватывают
29 носимых ID: 7 предметов одежды, 2 базовых доспеха, 5 уникальных, 12 сетовых,
3 трофейных. Для #129 собрана техническая пред-проверка; финальный acceptance
заблокирован до ручной приёмки A1–A7. Все Issue остаются OPEN, чекбоксы не закрыты.
[Все восемь матриц](implementation/armor88-full/REPORT.md),
[29-ID аудит с actual source A/B и SHA](implementation/armor88-full/AUDIT.md).

Java21 clean build 1m13s, native bake/UV29 ID, rune regression, 7442 ресурсов,
1339 PNG и production JAR parity PASS. Runtime NOT_RUN, запусков0. В source
устранены совпадающие внутренние фронтальные плоскости портов/лаптей/поножей/сапог;
настоящие clipping/анимация/stand/trim/runes/enchants/relog/MP ждут проверки владельца.
13 аксессуаров, #130 и gameplay не менялись работой88. Правки второго агента44/45
сохранены; установленная им тестовая45 содержит основную переработку брони.
46 дополнительно разводит на 0.06px пояс поножей неукротимости и НЕ установлена.
Точные source/JAR SHA: [verification.json](implementation/armor88-full/verification.json).
Нет автоматического commit/push/закрытия Issues. Следующие записи исторические.


## PolyMC: тестовая1.9.7.45 установлена · 20.10.2026

По прямому запросу владельца установлен проверенный JAR45 с текущим мягким glint #104
и существующей работой брони. Clean build Java21 1m1s, стабильность source/resources/JAR PASS.
Backup44 вне mods, один Slavic JAR,6 прочих модов сохранены по SHA256.
[Installed path/SHA256/source commit/backup](implementation/polymc-19745/installed.json),
[матрица](implementation/polymc-19745/REPORT.md). Source69a2db12 + dirty tree; нет commit/push.
IN_PROGRESS / OWNER_PENDING, игровых запусков0, runtime NOT_RUN. #104 OPEN; #105 не начата.
Эта запись заменяет прежние сведения «45 не установлена» и текущую блокировку упаковки.


## #104 — мягкий glint, повторная ревизия · 20.10.2026

IN_PROGRESS / OWNER_PENDING. Только shared PNG/GLSL/strengths: мелкий мягкий узор,
приглушённая палитра, alpha24/40/58 и медленнее UV; три OFFLINE source GPU кандидата.
Броня этой задачей не менялась. GLSL/JVM PASS; clean build1m8s PASS на момент выполнения.
Затем обнаружены параллельные изменения брони и исчезновение build/libs: текущая
resource/JAR-проверка BLOCKED_CONCURRENT_WRITER, без подтверждённого артефакта.
[Матрица, baseline и ограничения](implementation/rune-glow-104-revision/REPORT.md).
Runtime NOT_RUN, запусков0, POLYMC_NOT_INSTALLED. Нет commit/push; #105 не начата.

## PolyMC: установлена тестовая1.9.7.44 · 20.10.2026

По прямому поручению владельца установлен проверенный44 с исправлением рунического
слоя надетой брони, усилением сияния и enchantable tags. Backup42 вне mods;
один активный Slavic JAR,6 соседних модов сохранены по SHA256.
[Точные installed path/SHA/source base и backup](implementation/rune-glow-fix-44/installed.json).
Source HEAD69a2db12 плюс незакоммиченные изменения; commit/push не выполнялись.
Игровых запусков0; IN_PROGRESS / OWNER_PENDING, runtime NOT_RUN.
Следующие записи «44 не установлена» исторические и заменены этой квитанцией.

## Исправления сияния и зачарований · кандидат1.9.7.44 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** По скриншотам владельца исправлено несовпадение
VIEW_OFFSET_Z_LAYERING у equipped armor overlay; отдельный armor RenderType,
textile без armor offset. Alpha1/2/3рун112/156/200 вместо36/58/82, контрастные полосы.
Добавлены отсутствовавшие vanilla enchantable slot tags для22 ArmorItem и категории
пропущенного оружия/инструментов/crossbows. Предсуществующая работа #88 сохранена.
[Причины, source GPU, регрессия глубины и матрица](implementation/rune-glow-fix-44/REPORT.md).
Java21 clean build1m7s, JVM/batch/ABI, standalone GLSL/GPU, data graph и JAR PASS.
Minecraft запусков0, игровая проверка/MP/relog/ручная приёмка NOT_RUN.
**44 НЕ установлена в PolyMC**; установленная42 не изменена. Нет commit/push/закрытия.
Следующие записи об43/42 относятся к своим пакетам и не означают установку44.

## #88 — посадка льняного комплекта, source 1.9.7.43 · 20.10.2026

IN_PROGRESS / OWNER_PENDING. Исправлены linen_shirt и linen_ports: одежда выведена
поверх внешнего слоя скина, низ портов больше не погружён в базовую ногу.
Нативные точки крепления Steve/Alex сохранены; 12 контрольных мешей остальных
шести предметов неизменны. ID, текстуры, баланс и рунический слой сохранены.
Java21 clean build, offline bake/UV и production JAR gates PASS; игровой runtime
NOT_RUN, запусков0. [Матрица и source A/B](implementation/armor88-linen/REPORT.md).
43 НЕ установлена в PolyMC; установленная42 сохранена. Коммит не создавался.
#87 отложена до уточнений; #105 ждёт ручной визуальной приёмки #104.

## Установлена тестовая1.9.7.42; публикация по поручению владельца · 20.10.2026

В PolyMC Testing1.21.1 установлен проверенный42: backup40 сохранён, один активный
Slavic JAR,6 соседних модов неизменны. [Точные path/SHA/source](implementation/rune-glow-104/installed.json).
В GitHub опубликованы изменения #102/#103, Creative/тайник и код #104 с отчётами.
Source commit `2f9a143a`; [проверка публикации](implementation/rune-glow-104/publication.json).
#87 остаётся отложенной: production-текстуры оружия восстановлены; эксперимент архивирован.
Статус #104 IN_PROGRESS / OWNER_PENDING: игровые матрицы NOT_RUN, запусков0,
known foreign-buffer exceptions описаны в отчёте. Никакой приёмки/закрытия Issues или релиза.
Следующие записи «42 не установлена» исторические и заменены этой квитанцией.

## #104 — руническое сияние, source1.9.7.42 · 20.10.2026

**IN_PROGRESS / OWNER_VISUAL_REVIEW_PENDING.** Общий клиентский shader/texture для
0–3 реальных рун каждого ItemStack, без новыхNBT/механик.1 зелёный,2 +мятный,
3 +бирюзовый. ItemRenderer и armor piece hooks, отдельный textile slot-wrapper;
vanilla glint сохранён. Исходные PNG/модели/Runes/ItemState/common код не изменялись.
[Аудит, source GPU A/B0–3, матрица и ограничения](implementation/rune-glow-104/REPORT.md).
Plain JVM batch/UV/target-ABI и Java21 clean build1m5s PASS; shader compile/link
в отдельном скрытом OpenGL context PASS. Это НЕ игровой тест. Minecraft запусков0.
Реальная GUI/руки/armor/enchants/MP/JEI/relog матрица NOT_RUN; foreign immediate/outline
иnonQUADS sources bypass overlay для безопасности. Производительность NOT_MEASURED.
В PolyMC остаётся40,42 НЕ установлена. #87 отложена владельцем;3 пробных головы
инструментов восстановлены изbefore, эксперимент сохранён отдельно. #105 не начата.
Нет commit/push/закрытия Issues. Следующие записи исторические.

## Исправления Creative и предмета тайника · 1.9.7.40 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** Убраны лишние материальные варианты пустых рун
I/II только из Creative: теперь по одной заготовке каждого уровня; рецепты материалов
сохранены. Невидимый предмет ритуального тайника заменён обычной3D item-моделью:
корпус/крышка/замок, собственная32px текстура дерева/металла/ритуальной печати.
Размещённый ChestBlock/renderer/loot/NBT не менялись. [Матрица, actual model preview](implementation/creative-cache-40/REPORT.md).
Java21 clean build2m13s и JAR gates PASS. В тестовый PolyMC установлена .40,
backup .39, один Slavic JAR,6 соседних модов сохранены. [Path/SHA/source](implementation/creative-cache-40/installed.json).
Runtime NOT_RUN, запусков0, требуется личная проверка Creative/руки/тайника.
Commit/push не выполнялись. Следующие сведения об установленных версиях исторические.

## Боссовые руны #103 · тестовая1.9.7.39 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** Полностью переработаны10 основных наградных
рун #103:5 наградных,3 Горыныча,2 земляные. Силуэты уникальны; рога/глаз/материал
объединяют тройку Горыныча, каждый знак самостоятельный. Квадратные плитки #102
не копировались. Карачун и2 ледяные проверены и сохранены. ID/модели/эффекты/дроп
не менялись. [Аудит13, actual A/B,10 чертежей и матрица](implementation/boss-runes-103/REPORT.md).
Java21 clean build49s PASS; JAR CRC/resources/classes PASS. В тестовый PolyMC
установлена .39 вместе со всей серией #102; backup .36, один JAR,6 соседних
модов сохранены. [Точные path/SHA/source fingerprint](implementation/boss-runes-103/installed.json).
Нет commit/push; source HEAD плюс незакоммиченный diff. Запусков0, runtime NOT_RUN.
Осталось: ручная приёмка и отсутствующий исходный исторический концепт-постер.
#103 остаётся OPEN. Следующие записи об установленной .36 — исторические.

## Полная серия крафтовых рун #102 · 1.9.7.38 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** Исправлена ошибка ограничения четырьмя пилотами:
переработаны все 24 руны согласованного каталога I–IV и четыре заготовки.
Квадратная плитка, индивидуальные символы, развитие рамки по уровню; мотыга/топор
сохраняются в парах III→IV. V остаётся концептом. ID, рецепты, модели и механики сохранены.
[Полная матрица и actual PNG A/B](implementation/runes-102-full/REPORT.md).
Java21 clean build PASS; 7431 ресурсов, 1337 PNG, 1732 классов проверены в JAR.
Игровых запусков 0, runtime NOT_RUN. PolyMC остаётся .36, .38 НЕ установлена.
Четыре редкие руны восстановления из фрагментов — отдельная ветка вне утверждённого
каталога #102; их существующие ресурсы сохранены. Нет автоматического commit/push/закрытия.
Следующие записи про пилот исторические; ожидание приёмки пилота больше не ограничивает серию.

## Крафтовые руны: первый исполнительный пилот · 1.9.7.37 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** По прямому новому поручению владельца реализованы
четыре реальные PNG farmer_iii/iv и lumberjack_iii/iv: квадратная плитка, неизменное
ядро мотыги/топора в паре, вторичная растительность, развитие обода III→IV.
Native64px и существующие item models/recipes/tiers/materials сохранены. Из production
поменялись4PNG+версия;4curated masters обновлены для существующего копирующего генератора.
[Actual baseline/A/B1×3×, source и матрица](implementation/runes-102-pilot/REPORT.md).
Остальные20руны/4заготовки и V не внедрялись. Пилот требует личной приёмки перед серией.
Игра/PolyMC/сервер/GameTest не запускались, runtime NOT_RUN. PolyMC остаётся .36;
.37 не устанавливалась. Нет автоматического commit/push/закрытия #102 или owner-accepted.

## Установлена коррекция металлов 1.9.7.36 · 20.10.2026

В PolyMC установлен проверенный JAR36, backup35 сохранён,1active Slavic JAR,6других модов
не менялись. В origin/main source commit fad9d44d:5слитков одного vanilla silhouette,
2руды с vanilla каменной основой; только материал/локальные жилки, без смены механик.
[Квитанция path/SHA/source commit/backup](implementation/polymc-19736/REPORT.md),
[A/B и матрица](implementation/metals-vanilla-36/REPORT.md). Clean build/JAR PASS;
игровых запусков0, OWNER_PENDING. Предыдущие формы M1 отвергнуты владельцем и заменены.
Следующие записи исторические; закрытие Issue не выполнялось.

## Коррекция металлов по скриншотам владельца · 1.9.7.36 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** Последнее указание владельца заменяет прежнее решение
о разных формах слитков: silver/perunite/purified iron/purified copper/hardened iron
используют одну точную vanilla iron_ingot topology, различаются только палитрами
и небольшими прожилками. Серебряная/перунитовая руда используют vanilla iron_ore layout,
каменные пиксели сохранены, меняется только цвет включений. Vanilla файлы не изменены.
Нативные16/32px сохранены,7PNG+версия; самородок/заготовка, рецепты/свойства неизменны.
[Actual A/B, исходные SHA, матрица](implementation/metals-vanilla-36/REPORT.md).
Java21 clean build1m40s PASS, runtime NOT_RUN/запусков0. Следом квитанция установки
.36/source commit; предыдущие записи и прежние силуэты M1 считаются историческими.

## PolyMC и Git обновлены до 1.9.7.35 · 20.10.2026

По прямому поручению владельца опубликован source commit eb113d4c в origin/main,
проверенный JAR35 установлен в тестовую1.21.1. Backup32, один active JAR,6других модов сохранены.
[Точные path/version/SHA/source commit/backup](implementation/polymc-19735/REPORT.md).
Сборка включает прежние #78/#79, #81 толькоM1, #82 и микропатч5raw #83.
Java21 clean build/resources/JAR PASS; игровых запусков0, #82/#83 OWNER_PENDING/OPEN,
#81 частичный. Никакого нового owner-accepted или закрытия без приёмки.
В GitHub опубликованы source/evidence/метаданные, отдельный binary Release не создавался.
Предыдущие записи ниже — исторические source snapshots.

## #83 — микропатч мяса и подготовка публикации .35 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** Пять raw PNG: утка, козлятина, кабанина, оленина,
медвежатина. Гусятина и все cooked пары UNCHANGED; другие еда/напитки, механики,
ID/lang/recipes сохранены. [Диагноз12ID, A/B, матрица, JAR](implementation/meat-83/REPORT.md).
Java21 clean build52s и ресурсы/JAR PASS, игровых запусков0. По явному поручению
владельца выполняются публикация исходников .35 в main и установка .35 в PolyMC;
точные SHA/source commit/backup будут в квитанции polymc-19735. #83 OPEN, #82 OWNER_PENDING,
#81 частичный M1. Исторические исходные матрицы ниже не означают игровую приёмку.

## #82 — модовые саженцы и капуста · 1.9.7.34 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** Пять древесных саженцев перерисованы адресно:
компактная яблоня, плотная липа, ветвистая рябина, короткие поникающие побеги ивы,
хвойные ярусы сосны. Предмет cabbage — квадратнее, с плоскими боками и листовыми слоями.
Пять ягодных саженцев проаудированы и UNCHANGED. Сохранены native32px apple/cabbage
и native16px woodland saplings. Production diff: только шесть PNG + версия.
[11 ID, реальный PNG A/B, offline cross и матрица](implementation/plants-82/REPORT.md).
Minecraft/placed runtime NOT_RUN, запусков0. В PolyMC остаётся .32, .34 НЕ установлена.
main fast-forward 5976ef3c добавил только утверждённый бриф/SVG, прежние пакеты сохранены.
#81 остаётся частичным M1; переход к #82 выполнен по новому поручению владельца.
Ни автоматического commit, ни закрытия Issue. Предыдущие записи ниже исторические.

## Приёмка владельца #77–79; #80 отменена; начат #81/M1 · 20.10.2026

Владелец после установки 1.9.7.32: «предыдущие закрывай задачи все окей».
#77, #78, #79: закрытие по явной приёмке выполнено, но Actions автоматически переоткрыла их
из-за отсутствия owner-accepted. Итог GitHub OPEN; владелец должен поставить метку лично.
Игровые запуски/измерения Codex остаются 0/NOT_RUN.
#80 владелец признал лишней; closed/not_planned, физически не удалена (нет delete Issue в connector,
браузер без авторизации). Следующая работа — родитель #81, только первый пакет M1/#96:
четыре иконки материалов, без оружия/трофеев/AI. Остальные #97–101 ещё не исполнялись.
[Пакет M1 и матрица](implementation/materials-81/M1-96/REPORT.md).
Ни автокоммита, ни установки 1.9.7.33; в PolyMC остаётся 1.9.7.32. Предыдущие записи ниже исторические.

## PolyMC 1.9.7.32 — тестовая установка по запросу владельца · 20.10.2026

Установлен preview #78 + #79, предыдущая 1.9.7.30 сохранена в backup; один активный
Slavic JAR, шесть других модов сохранены. [Точные path/SHA/source fingerprint и матрица](implementation/polymc-19732/REPORT.md).
Исходники остаются незакоммиченными на базе 7e6d64d4. Игровых запусков агентом 0;
#78/#79 OWNER_PENDING, #80 IDENTIFICATION_BLOCKED. Предыдущие записи ниже — история.

## #79 — классификация, физиология и локальная защита · 1.9.7.32 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING, Issue OPEN.** Восемь nature tags, независимые intent/traits/affiliation,
единый CombatTargeting, допуск и тики кровотечения/POISON, один IS_FIRE коэффициент.
Чудь защищает локальную группу по semantic threat; village guards/golems и scoped
projectiles используют общий допуск с сохранением native encounter правил.
114 source registrations / 84 living / 30 nonliving; runtime registry NOT_RUN.
Java21 clean build и 327 native policy/metadata checks PASS; игровых запусков 0.
#78 сохранён отдельным архивом и before-diff; его lifecycle/модель/аудио не переписаны.
Нет commit, закрытия Issue или установки PolyMC (осталась 1.9.7.30).
[Матрица и ограничения](implementation/creatures-79/REPORT.md),
[классификация](CREATURE_CLASSIFICATION.md), [отношения](ENTITY_RELATIONS.md).

## #78 — конечный lifecycle богатырей, 1.9.7.31 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING, Issue OPEN.** В существующем HeroCalls: 12000 тиков
помощи вместе с восстановлением, максимум 2 восстановления, отдельный КД 36000 тиков;
обязательные назначенные сюжетные фазы приостанавливают срок без новых квот.
Исправлены обычные смертельные удары Ильи/Добрыни, сохранность квоты у четырёх героев,
растягивание Алёши на ARRIVE/RECALL, безопасный спавн и интеграция Тугарина (также стрелы Алёши).
Серверная синхронизация остатка времени/квоты в существующий HUD; лимит группы 2/3 сохранён.
Java21 clean build PASS; native бюджеты/codec/исходные ModelPart и соседние клипы проверены.
[Полная матрица, точные файлы, native A/B и звуковая карта](implementation/bogatyr-78/REPORT.md).
78 OGG проверены структурно и декодированы. Жалобы на звук и зависание не объявлены
устранёнными: прослушивание/игровые сценарии/реальный relog/MP/FPS/TPS NOT_RUN.
Агент не запускал игру. PolyMC остаётся установленной тестовой 30; source `86e383ba`,
запись установки `7e6d64d4` отправлены в origin/main. 31 не устанавливалась и не коммитилась
автоматически. Предыдущие записи ниже — история.

## PolyMC / Git update — 20.10.2026

Installed test preview **1.9.7.30**, source `86e383ba014be404767c541730e81cea2fd7061c`; [installation receipt](implementation/polymc-19730/REPORT.md). Backup 1.9.7.29; one active mod JAR, six other mods preserved. Runtime launches 0. #77 remains OWNER_PENDING / OPEN. Next authorized task: #78. Earlier entries below are historical.

## #77 — Ским-зверь по плакату1.7.3 · 1.9.7.30 · 20.10.2026

Заново построены несущие объёмы, многослойная грива, морда/зубы, разные передние/задние
лапы и один хвост;59узлов/231cuboids,512px body/eye/rage PNG. Собственные позы/IK,
ROAR canRear/low-ceiling, GRIP standing-humanoid head clearance и DEATH floor-fit.
Java21 clean build1m54s,31контракт,1000native Skim poses,150exact Alyosha pose pairs,
production JAR7410resources/1337PNG/1717Java21classes PASS. Server/ID/NBT/audio/прочие
существа сохранены. [Полная матрица и native A/B](implementation/skim-77/REPORT.md).
IN_PROGRESS / OWNER_PENDING: игровые запуски0, GL/MP/relog/FPS/реальные коллизии NOT_RUN.
PolyMC остаётся принятой29,30 не устанавливалась; #77 OPEN, автокоммита нет.

## #76 — владелец принял результат · 20.10.2026

Владелец: «все хорошо меня все устраивает задачу закрою сам».
Принят результат установленной 1.9.7.29; [квитанция PolyMC](implementation/polymc-19729/REPORT.md).
Issue владелец закрывает самостоятельно. Предыдущие записи ниже — история проверок;
инструментальные runtime/FPS/MP замеры Codex не проводил.
Следующая явно назначенная работа — #77, модель Ским-зверя по плакату 1.7.3.

## #76 — новая модель проклятого волколака, 1.9.7.29 · 20.10.2026

Офлайн-реализация: новая анатомия69 bones/77 cuboids, согласованные512px UV и два глазных
острова; IK по фактическому rig, опора после5-тикового blend и адаптация действий.
Java21 clean build и production JAR проверяются в [полной матрице](implementation/volkolak-76/REPORT.md).
Реальные16 A/B видов, переходы, действия и native mesh сохранены вместе с baseline.
AI/таймеры/хитбоксы/registry/audio и остальные волки сохранены по SHA256.
Статус IN_PROGRESS / OWNER_IN_GAME_PENDING: игровые запуски0, MP/relog/FPS NOT_RUN,
Issue OPEN. PolyMC не обновляется по запросу владельца. Автокоммит не выполняется.

## PolyMC1.9.7.28 установлен —20.10.2026

По прямому запросу владельца доставлены все текущие production изменения: #90v3
(законные1/2/3), пиво/квас/морс и II7/III12, сохранённые ClassUI/command fixes.
Java21 clean build59с +32HUD cases +6220ClassUI +9249commands/13staff +beer/NBT PASS.
Backup25/63config/6othermods/installed SHA/один активныйJAR PASS.
[Installed path/SHA/source e5924bd9 и полный source ZIP](implementation/polymc-19728/REPORT.md).
Minecraft/PolyMC не запускались, миры не менялись. INSTALLED_TEST_PREVIEW;
runtime/owner acceptance NOT_RUN, #90 OPEN. Игровая проверка владельцем одним прогоном.

## #90 по актуальному ТЗ v3 —1.9.7.28,20.10.2026

Перепроверена Issue: прежнее требование4 одновременно отменено. HUD — одна колонка
1/2 реально загруженных спутников, законный3-й у Полоза; без пустого/четвёртого слота.
Лимиты HeroCalls2/3 и summon logic не менялись. Сортировка typeID+UUID стабильна
при измененииHP/unload/return; отсутствующие сущности не рисуются. Bottom-left anchor
для1/2 сохранён, максимум высоты3 строки, без переноса в дополнительные колонки.
Java21 clean build+beer/native NBT tests PASS59с;32 GUI/count cases и lifecycle-list
regression PASS; JAR7408resources/1713classes PASS. ClassUI6220checks/18branches PASS.
[Актуальная матрица #90](implementation/hero-hud-90-v3/REPORT.md). Runtime/owner NOT_RUN.
Установка28 по прямому запросу владельца — отдельная квитанция после gates. Issue OPEN.

## #90 — все загруженные богатыри в HUD,1.9.7.27,20.10.2026

Убран cap2: все4 типа, фильтр владельца/живых/64 блоков, UUID dedup и стабильный порядок.
Имя/HP/состояние сохраняются у каждой строки, отступление отделено от восстановления.
Компактная общая панель, перенос при нехватке высоты, клиентские смещения для оверлеев.
Жизненный цикл/AI/NBT/network и радиус не менялись; таймеры #78 не подделываются.
Java21 clean build PASS64с;40 GUI/count cases + source draw/native bitmap preview PASS;
JAR7408resources/1713classes PASS. [Матрица #90](implementation/hero-hud-90/REPORT.md).
Runtime/owner acceptance NOT_RUN; запусков0, POLYMC_NOT_INSTALLED для27. Issue OPEN.
Прежние UI/command правки сохранены. Исторические docs обновлены из актуального main.

## Квас/морс и усиление опьянения —1.9.7.26,20.10.2026

По двум замечаниям владельца: квас коричневый, как тёмное пиво; морс красно-ягодный.
Целая общая кружка сохранена, остальные8 сортов не менялись. Алкогольные штрафы:
I при4–6 дозах,II при7–11,III с12; load1/2 и decay1200 тиков прежние.
Java21 clean build+native offline tests PASS61с; JAR7408resources/1707classes PASS.
[Матрица и preview](implementation/drinks-19726/REPORT.md). Реальные runtime/relog/death
и owner acceptance NOT_RUN; запусков0, Issues OPEN. PolyMC остаётся25,26 не установлена.

## PolyMC1.9.7.25 установлен для проверки —20.10.2026

По прямому запросу владельца установлен проверенный единый пакет #84/#85.
Backup23/63config/installed SHA/один активный JAR PASS;6 остальных mods сохранены.
Source48ed6824 + сохранённые локальные UI/command правки зафиксированы полным ZIP+manifest.
[Точная квитанция установки](implementation/polymc-19725/REPORT.md).
Minecraft/PolyMC не запускались, миры не менялись; owner/runtime NOT_RUN.
INSTALLED_TEST_PREVIEW, Issues OPEN.

## Пиво #84/#85 — исправление отклонённой1.9.7.24, 20.10.2026

1.9.7.24 отклонена владельцем: перегруженный декор и выступающая пена.
В1.9.7.25 все8 сортов используют одну исходную целую модель кружки: корпус,
ручка, дно, пропорции и display transforms сохранены. Плоская поверхность внутри
борта, без потёков/табличек/наростов; различие только цветом напитка и пены.
Бутылочные модели сохранены; bottle override/NBT/ID неизменны. Порог опьянения4,
ступени5/6, load1/2 и decay1200 тиков в существующей системе сохранены.
[Единая матрица и проверки](implementation/beer-84-85-v2/REPORT.md).
Слоты16px и GUIscale3 показаны в одном итоговом OFFLINE preview, не Minecraft screenshot.
Java21 clean build + native offline NBT/rules PASS44с; JAR7406resources/1707classes PASS.
Игра/PolyMC не запускались, установка не выполнялась; owner/runtime NOT_RUN.
Обе Issues OPEN/IN_PROGRESS; приёмка только владельцем. Другие локальные изменения сохранены.

## Единый пакет пива #84 + #85 — 20.10.2026, OWNER_PENDING

По уточнению владельца внешний вид и опьянение поставляются вместе для одного
игрового прогона. Исправлены все8 production cuboid кружек и8 бутылочных моделей:
видимое содержимое, разные геометрия/рисунок пены и крупные знаки;40PNG16px.
Порог4 дозы и RU/EN из #85 включены. Полные source до/после, светлый/тёмный фон,
4 display contexts и [единая матрица](implementation/beer-84-85/REPORT.md).
Java21 clean build PASS1m9s, actual JVM progression/decay PASS, генератор byte-identical,
JAR7414resources/1707classes PASS. Артефакт build/libs/slavicmyths-1.9.7.24.jar.
Офлайн-проекции не являются Minecraft screenshots. Runtime/owner acceptance NOT_RUN;
запусков0, POLYMC_NOT_INSTALLED для24. Без commit/push; обе Issues OPEN/IN_PROGRESS.
Отдельная предыдущая передача #85 заменена этим общим пакетом.

## #85 — порог опьянения, 20.10.2026, IN_PROGRESS

В существующей BeverageItem/BrewRules первая алкогольная отрицательная ступень
перенесена с2 на4 дозы;5/6 добавляют слабость/замедление,7 сохраняет прежнюю
максимальную ступень. Existing load1/2, decay1200 тиков, длительности/усилители,
quality/spoil и NBT сохранены. Добавлены RU/EN threshold tooltip и серверный
снимок дозы/ступени после питья. JVM progression/decay/mixed/overflow PASS;
Java21 clean build PASS1m21s. Production JSON/resource/JAR — checks.json.
[Матрица #85](implementation/intoxication-85/REPORT.md).
Игровые состояния, relog/death/MP/UI и owner acceptance NOT_RUN; запусков0.
Issue OPEN/OWNER_PENDING, POLYMC_NOT_INSTALLED для этого пакета, commit/push нет.
Предыдущие локальные правки #73–75 сохранены; их приёмка не заявляется.

## #75 — уточнения по снимкам владельца / PolyMC1.9.7.23 — 20.10.2026

Устранён двойной native/manual tooltip в ClassScreen; у вкладок и выбора ветви
нет повторов названия. Hover навыка — только название, детали в карточке по клику;
оставшиеся многострочные подсказки корректно переносятся. Уровень ветви постоянно
виден под заголовком. Все18 реальных направлений получили RU/EN с точными выгодами,
процентами из definitions, отдельным изучением эволюций и будущим развитием.
Описания прокручиваются независимо. Одобренные части V2 сохранены:9 критических
source-файлов совпадают с архивом установленной22. Balance/state/network не менялись.
Java21 clean build2m4s,6220 UI checks +18 branch copy,7398resources/1707classes PASS.
1.9.7.23 INSTALLED_TEST_PREVIEW: backup/SHA/source ZIP/один JAR PASS.
[Матрица и квитанция](implementation/classes-75-v3/REPORT.md). Владелец положительно
оценил остальную V2; исправления V3 и полная #75 ещё без ручной приёмки.
V3 IN_GAME_NOT_RUN, Codex запусков0. Issue OPEN/IN_PROGRESS, commit/push не выполнялись.

## #75 V2 — прежний UI отклонён; тестовый PolyMC1.9.7.22 — 19.10.2026

Прочитан новый контракт GitHub и6 схем origin/main; источник отклонённого1.9.7.21
подтверждён installed SHA+архивом source. ClassScreen переделан: полное полотно,
нет выбора по умолчанию, transient card, pointer zoom/pan/fit, сохранение камеры,
компактные3active+P/P2, picker/key draft Save/Cancel/conflicts. Направления отдельно,
мастерство своего пути вместо общего каталога. Server state/balance/network неизменны.
Java21 clean build1m8s,6220 UI rules/geometry cases и JAR7398resources/1707classes PASS.
1.9.7.22 INSTALLED_TEST_PREVIEW: backup/source ZIP/installed SHA/один JAR PASS.
[Промежуточная матрица](implementation/classes-75-v2/REPORT.md).
6 игровых состояний, видео, шрифты, XP/death/relog/MP IN_GAME_NOT_RUN; Codex запусков0.
#75 OPEN/IN_PROGRESS; прежняя работа не принята, V2 owner acceptance отсутствует.
Другие Issues не брались; автоматического commit/push нет.

## Исправление загрузки / PolyMC 1.9.7.21 — 19.10.2026

Владелец обнаружил FAIL загрузки1.9.7.20: пустая @EventBusSubscriber у YagaCommands.
Удалена31 устаревшая подписка после переноса #74; native EventBus regression PASS,
9249 command checks/13 staff cases, Java21 clean build1m30s PASS. Установлен1.9.7.21:
backup/SHA/один активный JAR PASS. [Матрица и квитанция](implementation/loading-fix-19721/REPORT.md).
Codex запусков0; успешная игровая загрузка исправления и owner acceptance NOT_RUN.
#74/#75 остаются OPEN; это preview. Миры/старый1.16.5 не менялись.

## Тестовый PolyMC 1.9.7.20 установлен — 19.10.2026

По запросу владельца установлен preview дерева #75 с исправлениями #73/#74/посоха.
Java21 clean build PASS51s,7398 ресурсов/1706 классов и installed SHA256 PASS.
Backup/один активный JAR PASS; точный незакоммиченный source сохранён ZIP+patch.
[Акт установки](implementation/polymc-19720/REPORT.md). #75 IN_PROGRESS,
ручная игровая приёмка NOT_RUN, запусков0. Миры и старый1.16.5 не тронуты.

## #75 — дерево классов, IN_PROGRESS — 19.10.2026

Взята следующая Issue из GitHub: #75. ClassScreen подключён к ClassTreeLayout:
реальные связи 40 навыков, ранги/состояния, требования, вертикальная и горизонтальная
прокрутка; исправлена подсказка второго пассивного слота. Серверные правила/формат данных
не менялись. [Матрица и ограничения](implementation/classes-75/REPORT.md),
[руководство](implementation/classes-75/PLAYER_GUIDE.md). Проверки и JAR — checks.json.
Runtime XP/смерть/перезаход/MP и owner acceptance NOT_RUN; Issue OPEN.
Запусков 0; PolyMC и миры не тронуты. Первая часть задачи, не итоговая поставка.

## #73/#74 и посох Морозко — 19.10.2026, OWNER_PENDING

#73:8 повреждённых мест исправлены; #74:37 registrars → один event/root,
274 исходных варианта →250 канонических с help;26 повторов объединены,
старые пути сохранены. Полные [команды](COMMANDS.md) и [миграция](COMMAND_MIGRATION.md).
По новому сообщению владельца исправлены фильтр целей/близкий луч посоха,
ПКМ по существам и подписи HUD. [Посох](implementation/frost-staff-targeting/REPORT.md).
Java21 clean build1m6s,9057 command checks и13 staff predicate/geometry cases PASS.
JAR/resource/class parity PASS:7398 ресурсов/1705 классов, jar-check.json; offline proof.
Runtime/HP/HUD/event-bus QA NOT_RUN, запусков0. PolyMC не переустанавливался;
в нём прежняя19 без этих новых правок. Issues остаются OPEN до owner acceptance.
[Матрица #74](implementation/commands-74/REPORT.md).

## #73 — исправление локализации, 19.10.2026

Исправлены6 сообщений генератора Чуди и2 UI-символа;8 новых ключей RU/EN.
UTF-8/JSON/placeholders/JAR PASS; Java21 clean build1m13s PASS. Запусков0,
OWNER_PENDING; PolyMC не менялся. [Матрица](implementation/localization-73/REPORT.md).

## Обновление тестового PolyMC 1.9.7.19 — 19.10.2026

По запросу владельца установлен текущий main23a23441 в тестовый PolyMC.
Production src/main совпадает с1.9.7.18; версия повышена, удалённые владельцем
планы future/ сохранены удалёнными и опубликованы. Java21 clean build PASS1m23s;
JSON/resources и compiled classes проверены на совпадение с production JAR.
Backup/SHA/один активный JAR PASS; миры и1.16.5 не изменены, запусков0.
INSTALLED_TEST_PREVIEW / OWNER_PENDING; Issues не закрываются.
[Матрица и акт](implementation/consolidated-19719/REPORT.md).

## Приоритет владельца: визуал #56, затем #46, #54/#55

#61–#66 сохранены OPEN/OWNER_PENDING; массовые аудиоаудиты остановлены.
Тугарин1.9.7.18 IN_PROGRESS: опорные ноги/корпус/клинки и инерция, source-показ
до/после текущей17, серверные hit ticks/AI/NBT и ресурсы сохранены.
[Матрица #56](implementation/tugarin-56/ITERATION_19718.md). Java21 clean build2m + native/scoped gates PASS; compiled3d4ff858.
Нативные до/после и GIF сохранены; PolyMC1.9.7.18 INSTALLED_TEST_PREVIEW.
Backup17/config/SHA/one active JAR PASS; selected384cd496 production-identical
к compiled3d4ff858. Owner visual/runtime NOT_RUN.
Minecraft не запускать; финальная приёмка владельцем.

## #61 — реакции оленя и оленихи, 1.9.7.17 IN_PROGRESS

Подключены4 прежних hurt/death events /6 recorded OGG. Самец: kvgarlic CC0;
DOE: juskiddink CC-BY4, пол источника не указан, привязка требует слуховой оценки.
Последние реакции — игровая интерпретация, не записи смерти. Java/AI/модели
не менялись. Игоша3 DEFERRED/KEEP_CURRENT. Активные4 source gaps заполнены;
весь аудиопроект OWNER_PENDING, не DONE. [Матрица](audio-review/DEER_19717.md).
Java21 clean build1m37s +9 offline gates PASS; compiled main82679092.
PolyMC1.9.7.17 INSTALLED_TEST_PREVIEW: backup16/config/SHA/one active JAR PASS.
Selected main092253f8 production-identical к compiled82679092; оба в receipts.
Minecraft/PolyMC/GameTest/server запусков0; hearing/runtime owner pending.

## #61 — Карачун/медвежонок, Игоша отложена

Установлена тестовая1.9.7.16:3 прежних events/5 новых recorded OGG, одиночный мужской
шёпот Карачуна и собственные cub reactions. Java/AI/модели не менялись.
Игоша3 DEFERRED / LOW_PRIORITY / KEEP_CURRENT, bytes сохранены. Активны4 gaps
оленя/оленихи. Java21 clean build44s +6 общих/2 scoped offline gates PASS.
Compiled main1bb0a1f2; selected verified mainbc74fccb production-identical.
PolyMC16 INSTALLED_TEST_PREVIEW; backup/SHA/one active JAR PASS;0 launches.
[Матрица](audio-review/WHISPER_CUB_19716.md). Hearing/runtime owner pending.

## #61/#64–#66 — ordinary Волколак и два шёпота Маринки

Установлена тестовая1.9.7.15: обычный VolkolakEntity имеет отдельные реальные rage/howl
вместо общего Ravager;2 новых registry events/3 записанных OGG. CursedVolkolak
и модели сохранены; другие HuntMob используют прежний audio hook.
Маринка:2 записанных женских whisper phrases; idle01 и LURE frame22→02.
Source gaps12→10;458 вариантов/425 events. Не повторяем завершённый полный аудит.
Java21 clean build69s и6 общих+1 scoped offline gates PASS.
Compiled source66a19070; CursedVolkolak/другие Hunt dispatch сохранены.
PolyMC1.9.7.15 INSTALLED_TEST_PREVIEW: backup/SHA/one active JAR PASS.
Selected main43a39201 имеет ту же production tree, что compiled66a19070;
в build/installation receipt записаны оба commits. Запусков0, миры сохранены.
[Матрица](audio-review/VOLKOLAK_WHISPER_19715.md). Owner/hearing/runtime pending.

## #64–#66 — мороз, ночь и защита генераторов

1.9.7.14 IN_PROGRESS / OWNER_PENDING:35 существующих событий,36 новых OGG.
Ворон — реальная запись nielstii; последний час — записанный колокол Martineerok.
Остальное — подписанные материальные эффекты, не голоса. Синтетический шёпот
Карачуна отключён: BLOCKED_NEEDS_AUDIO_SOURCE. Принятые модели/механики сохранены.
Генераторы имеют --audio-only без возврата старой синтетики. Полный аудит:
1323 OGG предыдущей13 декодированы и совпали с JAR;139 дополнительных старых
hash traces не считаются голосовой приёмкой. Java21 clean build47s и6 offline gates PASS, source69a77a70.
Тестовый PolyMC обновлён до1.9.7.14 после проверки закрытых процессов.
Installed SHA da114d33f6faced3f29d298644aeb81abe2336877ada017b0f2e8633b32e91d2.
Backup/one active JAR проверены; миры не менялись. Запусков Codex0.
[Матрица](audio-review/FROST_NIGHT_19714.md).

## #63–#66 — дыхание, магические сигналы и действующие варианты

Подготовлена1.9.7.13:14 новых recorded OGG для14 прежних событий Соловья,
Ночницы, Ильи, Добрыни и Финиста;4 ранее не вызывавшихся варианта теперь
чередуются. Аудиоприоритеты не меняют AI/урон/кадры/явные RNG draws.
417 назначений/386 events,35 vanilla references. Owner/hearing/runtime pending;
Java21 clean build и6 offline gates PASS (source8e5c3494). PolyMC1.9.7.11 пока владелец проверяет не изменяется.
[Матрица](audio-review/BREATH_VARIANTS_19713.md).

## Проверенная1.9.7.12: callbacks героев; PolyMC остаётся1.9.7.11

Source25adb2c5: Алёша hurt1/2 и Финист successful sword contact подключены.
Java21 clean build50s,6 offline gates и compiled JAR parity PASS. Установка
NOT_INSTALLED: открыт polymc.exe PID10580, guard до мутаций. Запусков Codex0.
154/154 scanner dynamic rows вручную классифицированы (это не runtime coverage);
526 helper guards и UNKNOWN provenance остаются. #61–#66 IN_PROGRESS/OWNER_PENDING.
[Callbacks](audio-review/HERO_CALLBACK_REPAIR_61.md), [установка](implementation/audio-60/TEST_PREVIEW_1.9.7.12.json).

## #64/#65 — подключение пропущенных callbacks героев

Алёша: стандартные hurt callbacks для2 записанных takes; Финист: sword contact
только после успешного damage на существующем frame8. Без новых RNG draws,
изменений урона/AI/NBT. [Матрица](audio-review/HERO_CALLBACK_REPAIR_61.md).
Техническая1.9.7.12 подготовлена; build/install ещё NOT_RUN. В PolyMC пока1.9.7.11.

## Тестовая1.9.7.11 установлена; #62/#64/#66 IN_PROGRESS

Соловей и клинок:19 событий/21 recorded variant, вдох и смех остаются BLOCKED.
Java21 clean build50s и6 offline gates PASS;1309 OGG,914 events,34 muted.
Вручную классифицированы111 из155 scanner routes;44 ещё без ручного разбора.
Скрининг856086 пар:318 high matches, включая намеренно общий death обеих фаз.
Установлен один JAR, source bdf72453, backup и SHA проверены. Запусков0.
[Акт установки](implementation/audio-60/TEST_PREVIEW_1.9.7.11.json);
[матрица](audio-review/NIGHTINGALE_PACKAGE_61.md). Hearing NOT_LISTENED.

## #64 — Соловей и клинок, техническое внедрение

19 событий/21 recorded variant: человеческий свист, реакции, клинок и движение.
Вдох/мужской смех BLOCKED_NEEDS_AUDIO_SOURCE; персонаж не завершён.
[Матрица](audio-review/NIGHTINGALE_PACKAGE_61.md). Версия1.9.7.11 подготовлена,
build/JAR/install ещё NOT_RUN. Существующая тестовая1.9.7.10 пока установлена.

## #62 / #66 — актуализация проверки сходства 1.9.7.10

Проверены1288 OGG:828828 пар,424 спектральных кандидата,299 сильных
совпадений. Из них51 пара неактивных оригиналов,230 предметных props,18
активных legacy/mixed требуют дальнейшего разбора. Неизвестные банки и голоса
кошек больше не могут автоматически классифицироваться как props.
[Матрица и ограничения](audio-review/SIMILARITY_19710.md). Production не менялся;
установленная1.9.7.10 остаётся актуальной. Hearing NOT_LISTENED/runtime NOT_RUN.

## Тестовая1.9.7.10 установлена — 19.10.2026

INSTALLED_TEST_PREVIEW / AUDIO_OWNER_PENDING; source8bf39d78.
Clean build Java21 PASS1m25s,6 offline gates PASS;1288 OGG/914 events.
Все внедрённые main-пакеты включены; дополнительно исправлены weapon tells/contact
и bow/shield cues разбойников. Backup проверен; один активный JAR; запусков0.
Акт: [TEST_PREVIEW_1.9.7.10](implementation/audio-60/TEST_PREVIEW_1.9.7.10.json).
#61–#66 остаются OPEN. Отдельно вручную классифицированы38 из155 source routes;
117 остаются без ручной классификации, runtime NOT_RUN.

## #61 — обновлённый срез после продолжения, 19.10.2026

345 событий/374 записанных назначения и35 vanilla references; реестр1280 OGG.
Новые записи духов, мира, боссов, героев и прочих существ; исправлены namespace
записанных OGG, отказ Домового и animal step/contact. Java21 clean build PASS48s;
шесть offline gates PASS.315 старых процедурных OGG traced по exact SHA.
Остаются487 UNKNOWN,155 unresolved routes,526 helper guard reviews,32 muted.
[Текущая матрица](audio-review/GROUPS_STAGE_61.md). IN_PROGRESS / OWNER_PENDING;
hearing NOT_LISTENED; runtime0; PolyMC не обновлялся. Это не завершение #61–#66.

## #61: опубликован предыдущий пакет; курганная группа и сквозной реестр — 19.10.2026

Предыдущая локальная работа сохранена в main5c93408e. Единый реестр охватывает
111 типов/69 самостоятельных,898 событий/906 OGG;906 decode/JAR parity PASS.
Для6 курганных типов рассмотрены все47 прежних событий:41 замена,6 KEEP,
6 реальных contact cues;69 отдельных записанных вариантов. Полный source diff
ограничен audio routing/anti-stack, урон/AI/NBT/RNG сохранены. Java21 clean build
PASS38s; source/decoder/JAR gates PASS. [Крупный пакет](audio-review/KURGAN_PACKAGE.md)
и [прослушивание](audio-review/AUDIO_ACCEPTANCE_INDEX.md). OWNER_PENDING,
runtime0, PolyMC не обновлялся; #61/#62–#66 остаются OPEN. Дальше — оставшиеся
216 неоднозначных routes и семейства духов/боссов/фауны/людей; #59 отдельный вокал.

## Главный аудиопроект #61 — исполнительное решение 19.10.2026

Владелец разрешил последовательную реализацию #62–#66 целиком и сохранение
проверенных пакетов в main. Источник: #61 comment6260650720 / #60 comment6260654991.
Это заменяет прежний локальный NO_COMMIT и старый пофайловый preapproval-gate.
Подходящие лицензированные записи: AGENT_SELECTED; игровая приёмка OWNER_PENDING.
Русалка/Кузя/спорные голоса остаются отдельным согласованием. Закрывает владелец.
Сначала сохраняется существующий проверенный пакет (5 существ + коса/посох/лёд),
далее единый сквозной реестр #62 и тематические наборы #63–#65; QA #66 общий.
PolyMC не переустанавливать, игровые процессы не запускать. User deletions future/ сохранены.

## #60/#64 — записанный foley косы, посоха и льда — 2026-10-19

Актуальное решение владельца в #58: руки и хваты Карачуна и Морозко приняты,
визуальная задача закрыта владельцем. Эта итерация не меняет их геометрию.
В #60 оставлено отдельное аудио: 11 различных записанных takes → 9 существующих
событий: два замаха и два металлических удара косы; деревянный удар посоха;
отдельные вылеты снарядов босса/игрока; треск шипа/попадания, двух типов копий,
появления сосулек. Источники qubodup/rubberduck CC0; стеклянный prop изображает
фантастический лёд. Это предметный foley, не голос и не запись настоящего льда.
Тайминги, полный frost/night Java, registry IDs и принятые визуальные ресурсы сохранены.
Выбор по делегированию; слуховая/игровая приёмка NOT_RUN. Остальные голоса,
песня русалки и оставшиеся звуковые группы этим пакетом не завершены.
Состояние IN_PROGRESS, без коммита, без повышения версии и установки PolyMC.
Установленная ранее 1.9.7.9 имеет прежний SHA4fa880a3… и не содержит этот пакет.
[Матрица требований и проверки](implementation/audio-60/FROST_FOLEY.md).
[Клипы с подписями](../art/audio/pack-a-60/frost-foley/PREVIEW.md).

## 1.9.7.9 установлена для проверки владельцем — 2026-10-19

По прямому запросу владельца повышена тестовая версия до1.9.7.9 и установлена
в Slavic-Myths-1.21.1-Testing. Это промежуточная проверка, не final delivery/релиз.
Java21 clean build PASS1m14s; audio/source/JAR gates PASS. Metadata установленного
JAR=1.9.7.9, SHA4fa880a3d7becbb819071c9dec838b0fa2d31f4ae19468f16f142143eb99a3cd.
Одна активная копия Slavic Myths; старый JAR/config в backup,240 остальных файлов
профиля проверены без изменений. Миры/1.16.5 не изменены, runtime0 запусков.
База037f80ac + незакоммиченные изменения, не опубликованный main commit.
[Акт установки и точные пути](implementation/audio-60/POLYMC_INSTALLATION.json).
Issue остаются OPEN до всех обязательных результатов и ручной приёмки;
новый AGENTS от19.10 отменяет прежнюю общую возможность самостоятельного закрытия.

## #60 — пакет пяти существ — 2026-10-18

Новый пакет: Овинник5 раздельных голосов (дыхание/боль/смерть/угроза/ярость),
Идолище7 (два idle, две боли, ярость, вызов, смерть), Ским2 боли разными take.
У Волка3 recorded prop foley движения вместо growl/sweep, боль=vanilla wolf.hurt;
его оригинальные вой/ответ стаи сохранены. С ранее подключённым Тугариным
пакет затрагивает все5 приоритетных существ, но не объявляет всю #60 завершённой.
HuntMob изменён только для звука oven(), остальные волколаки/Element/Mor,
серверная боёвка/тайминги/NBT сохранены. Овиннику normal pitch1, Mor сохраняет.65.
Источники14 distinct mouth performances StarNinjas CC0;3 cloth props OwlishMedia CC0.
Ни один новый выбранный файл не помечен owner-approved. Генераторы восстанавливают
выбранный пакет поверх legacy aliases. [Подписанные клипы](../art/audio/pack-a-60/voice-rework/batch-preview.md).

## Продолжение #60 / фактическая готовность — 2026-10-18

По прямому поручению разрешено закрывать реализованные задачи. При сверке
активных #46/#49–#60 остаются обязательные недоделки/игровая приёмка;
они не закрыты. Реальный следующий audio diff: SPIN Тугарина вместо прямого
RAVAGER_ROAR использует отдельный короткий выдох перед hit16, без второго
крика удара в begin. Ским rage использует отдельный Monster_02 take вместо
того же ravager alias;3s укладываются в64 тика даже при pitch.96.
Это2 AGENT_SELECTED по делегированию, не OWNER_APPROVED. Четыре ранее
прослушанных файла остаются без изменений. Коммита/установки/запусков нет.

## Автономная аудио-итерация — 2026-10-18

Владелец одобрил дыхание/удар Тугарина и разрешил самостоятельно выбирать
и внедрять подходящие дальнейшие звуки, спорные оставлять кандидатами.
Итоговая игровая оценка за владельцем; это не приёмка всех файлов.
Подключены точные четыре файла: вызов/дыхание/удар Тугарина и рёв Скима.
Для голоса удара добавлен отдельный audio-only cooldown24 тика: клип1.1s
не накладывается сам на себя в begin/комбо. Hit ticks/урон/NBT сохранены.
Решения о коммитах и установке ещё ожидаются; пока их нет.
[Границы полномочий](audio-review/OWNER_DECISIONS.md).

Java21 clean build PASS1m35s; voice gates6977 PASS, audio gates7732 PASS.
807 production OGG decode/JAR parity PASS. JAR SHA
d7c86ea453ac76c377efe4e306091903722740d4139805984b926112f9a2fc84.
Дополнительно413 выровненных waveform-пар,75 >=0.98; не слуховая приёмка.
Два новых записанных кандидата Овинника оставлены спорными, не подключены.

## #62 — инвентаризация и новые пробы — 2026-10-18

Перечитаны GitHub #61/#62 и открытый список задач. Новые #63–#66 не означают
автоматического старта всех пакетов. Read-only проверены 111 ID, 891 событие,
805 OGG; decode/JAR parity PASS, production без изменений. Достижимость всех
фаз/полная цепочка лицензий/слуховая приёмка ещё не завершены. Подготовлены
10 OLD-сравнений и две новые пробы Тугарина (дыхание/удар), без подключения.
[Результаты и проигрыватели](audio-review/README.md). IN_PROGRESS, runtime0,
PolyMC не менялся, коммита нет; пользовательские удаления сохранены.

## Approved rough Tugarin voice — 2026-10-18

Owner approved the lower, rougher challenge preview. The exact heard OGG
(e66735ee332a3c4d51c865d00123f56614a86d6fc0f1ae9393fbf60ee35ee596)
now replaces the earlier high challenge in production. approved_files and frozen
SHA select it explicitly; regeneration reproduces the rough processing before
restoring approved assets. Skim remains unchanged. Other six voices remain
candidates; full issue/runtime acceptance is still false. PolyMC not updated.

## Уточнение Тугарина — 2026-10-18

По отзыву владельца подготовлен отдельный rough-preview: голос ниже на 5 полутонов,
снижена резкая верхняя середина, добавлена мягкая сатурация. Длительность 1.6602s,
декодирование/отсутствие clipping проверены. Это кандидат, production/PolyMC не менялись.
Runtime0; clean build заново не выполнялся: изменён только отдельный аудиопример.

## Голосовая переработка #60 — кандидаты — 2026-10-18

Восемь раздельных подписанных голосовых OGG: Тугарин4, Ским3, Волк1.
Владелец одобрил тембр двух прослушанных файлов: tugarin_challenge/skim_roar.
Именно эти два замороженных по SHA файла подключены к прежним событиям, pitch1;
остальные6 и EQ160Hz+2dB Тугарина пока кандидаты. Полная приёмка=false.
Указаны роли/фазы/источники/CC attribution; film audio не использовалось.
Runtime0, PolyMC этой итерацией не изменён; остальная озвучка #60 не завершена.
[Подписанные образцы](../art/audio/pack-a-60/voice-rework/README.md).

## Уточнение звуковой приёмки #60 — 2026-10-18

Каждый пример подписывать: существо, роль, действие, фаза и назначение эффекта.
Для враждебных мобов/мини-боссов/боссов требуется убедительная характерная угроза.
Сопутствующие огонь/ткань/оружие не запрещены; прежняя трактовка «весь набор
отклонён» была ошибкой Codex. Нынешняя подача и выразительность требуют доработки.
Owner acceptance=false; production/PolyMC по этому уточнению не менялись.

## PolyMC и аудио #60 — IN_PROGRESS — 2026-10-18

По прямому указанию владельца установлена проверенная модельная сборка956f4645,
версия1.9.7.8, SHA65894e8c…, backup и241 неизменный файл профиля. Это не приёмка.
Следующая source-итерация #60: три отдельных записанных взмаха Тугарина на
прежних16/28/40; новые3event/subtitle, остальные mappings/82quarantine сохранены.
Остальные голоса/пакет A не завершены. Новая audio-итерация ещё не установлена.
[Матрица и прослушиваемые записи](implementation/audio-60/README.md).

## Хват оружия #58 — IN_PROGRESS — 2026-10-18

Карачун и Морозко: исправлены точки хвата, ладони с выемкой, согласованные траектории косы/посоха, реальный контакт второй руки Карачуна. Исходные клинок/кристаллы/UV/glow/held/icon сохранены. Native проверка 65 тиков каждого действия; common/server неизменны. Новые foley FAIL, ручная приёмка NOT_RUN, runtime0, PolyMC не обновлён.
[Матрица и native сравнения](implementation/weapon-grip-58/README.md).

## Соловей #57 — IN_PROGRESS — 2026-10-18

Соловей: две формы, отдельные torso/pelvis/кисти/колени, источник 58 поз на форму, восемь action sheets. Server AI/timings/NBT и предметы неизменны. Аудио FAIL, runtime0, owner acceptance отсутствует; PolyMC не обновлён.
[Матрица и native сравнения](implementation/nightingale-57/README.md).

## Тугарин #56 — IN_PROGRESS — 2026-10-18

Таз/колени/кисти первой формы, новая постановка SPIN и плавное восстановление
combo; сохранены hit16/28/40, 52+24 тика, серверная механика/NBT/ID. Подготовлены
44 native timing samples на форму и сравнения до/после; обе формы Лиха совпадают
с baseline. Новых foley ещё нет, ручной приёмки/установки нет. Runtime0 запусков.
[Матрица требований и исходные проекции](implementation/tugarin-56/README.md).

## Проклятый Волколак #55 — IN_PROGRESS — 2026-10-18

Перестроены маска, конечности, шерсть и две стойки; сохранены физические размеры,
серверный бой, квест и NBT. RISE/DROP привязаны к прежней смене коллизии 8/6,
ESCAPE сохраняет 49/87/131 и проверяется на пересечение пола во всех кадрах.
Подготовлены реальные native сравнения и 37 поз. Игровая и ручная приёмка NOT_RUN.
Версия прежняя, PolyMC не обновлён; звуковой карантин сохранён.
[Матрица требований и доказательства](implementation/cursed-volkolak-55/README.md).

## Серый Волк #54 — IN_PROGRESS — 2026-10-18

Переработаны anatomy/texture/locomotion/sit/pounce;30суставов,69кубов,15native
poses и source comparisons. AI/квест/тайминги/NBT/ID/хитбокс не менялись.
Ширина/высота проверены; Z4.608блока против collision2.2 — открытое ограничение.
Runtime0запусков, owner acceptance отсутствует, PolyMC не обновлён.
[До/после, матрица требований и тесты](implementation/grey-wolf-54/README.md).

## Исправления #59/#53 после отказа владельца — 2026-10-18

Русалка: реальные CC0 vocal candidates12OGG, intro/loop/intense/break,
пауза и предупреждение перед агрессией, opt-in Creative preview. Старые5OGG
сохранены; кандидатные mappings подготовлены, production playback выключен.
Приёмка на слух ещё отсутствует.
Полевик: согласованное приседание вместо body.y=8; это отдельный возможный
дефект, не утверждение, что найден моб из жалобы. AI/ID/NBT/баланс сохранены.

Статус: IN_PROGRESS, не DONE. Новые записи выключены до прослушивания владельцем;
карантин307419b3 сохранён, в sounds.json пустые entries и client gate=false.
Minecraft/PolyMC/GameTest/server:0запусков, runtime NOT_RUN.
[Код, источники, реальные позы и проверки](implementation/quality-59-53/README.md).

## Русалка — #49, 2026-10-18

**PARTIAL_NEEDS_AUDIO_SOURCE**: реализована отдельная native модель/текстура
128×64,31часть,13поз; SynchedEntityData для визуальных действий, телеграфированный
single-hit, ограниченный физический нырок в глубокой загруженной воде,
срыв песни/LOS и ограниченный lifecycle loop. Водяной и другие мобы не менялись;
registry IDs/hitbox/attrs, Knowledge, charm-пороги/защитные вещи и старые NBT
сохранены. Генератор воспроизводит новую Русалку, не возвращает256px skin
и не пересинтезирует её legacy OGG.

Java21 `clean build -PrusalkaOfflineCheck verifyRusalkaOffline`: **PASS**,
20062 native/offline assertions;6869 source/resource/production JAR checks PASS.
31part/13pose UV/finite/reset, два интерливированных UUID, bounded transitions,
AttackTimeline/cancel, dive-policy, реальный CompoundTag roundtrip; не игровой
перезаход/сетевой runtime. Production `build/libs/slavicmyths-1.9.7.8.jar` проверен;
fixture отсутствует в JAR. Deprecated/unchecked предупреждения существующего
проекта остаются. Source projections до/после и ключевые позы приложены.

Все5legacy OGG побайтно сохранены, decode/loop-endpoints PASS, но прослушивание
и финальный женский вокал **NOT_RUN / NEEDS_AUDIO_SOURCE**. Требуется запись
женского голоса без слов с правами на обработку/распространение; отдельные
intro/intensity/break/grab и варианты ambient/hurt ещё не поставлены.
#49 не закрыта и не объявлена DONE. Версия1.9.7.8/PolyMC не менялись.
Minecraft/client/server/GameTest/PolyMC: **0 запусков**. Следующие мобы этим
коммитом не реализуются. Пользовательские10удалений future/** не затронуты.
[Модель, кадры, ограничения и проверки](implementation/rusalka-49/README.md).

## Курганные текстуры V2 — #46 (2026-10-18)

Отвергнутый V1 заменён на11 точных PNG RGB16×16 из переданного владельцем V2.
SHA256 ZIP98a01d34281371c40edfb5928a37f2538fbae78e140fa97bd148b348a9ae457b.
Канонический approved.zip/manifest обновлены, рисунок/цвета не изменялись.
В src/main изменены только11 PNG; registry IDs, рецепты, модели, структуры,
баланс и механики сохранены. Оригиналы0.9.6 и runtime256 backup не менялись.

Java21 clean build PASS (compileJava FROM-CACHE);22289 checks PASS:92 модели,
UV/верх/низ/blockstates/item parents,6804 resource/JAR parity,11 exact PNG.
Creative: повторных catalog IDs и RU/EN названий голубой группы нет; normal/
cracked/mossy имеют отдельные ID/боковые рисунки и общие burial_stone_top грани.
Weighted stone_variant не является item. По жалобе без исходных tooltip/ID
нельзя достоверно назвать показанную пару; блоки не удалялись.
Импортёры и старый085 core loop защищены от отката на V1/64/256; regression PASS.

Версия остаётся1.9.7.8. PolyMC не обновлялся: установленный ранее V1 не подменён.
Minecraft/GameTest/PolyMC/сервер:0 запусков этой итерации; визуальная приёмка V2
остаётся владельцу. Пользовательские10 удалений future/** сохранены.
[Перечень файлов, Creative и проверки V2](implementation/kurgan-16-v2/README.md).

## История V1 — технически внедрён, художественно отклонён (2026-10-18)

Все11 утверждённых RGB16×16 PNG установлены побайтно из ZIP SHA256
107bb2ae8413a34f36649238242e77604e123d00dfccf519845a5264167b8c6a.
Дизайн не перерисовывался. В src/main изменены только11 PNG: модели, registry ID,
worldgen, NBT построек, существа, баланс и механики сохранены относительно main.
Сохранены прежние runtime256, art/kurgan-0.9.6/provenance и художественные исходники;
импортёр восстанавливает утверждённые16, не прежние256.

Java21 clean build PASS (compileJava FROM-CACHE),22257 offline checks PASS,
92 связанных модели/UV и6804 resource/JAR parity. Production1.9.7.8 установлен по #47 в существующий PolyMC1.21.1 Testing:
SHA256/backup/один mod-ID JAR/11 exact PNG PASS,366 прочих файлов сохранены.
Имя профиля обновлено; миры и старый1.16.5 не менялись. Установленная ранее1.9.7.7 происходит из другой
линии: её несогласованные изменения баланса/достижений в main не переносились.
Версия повышена выше1.9.7.7; публичный2.0 не выпускался.

Ранее по разрешению выполнен1 dev-client launch с отдельным Creative-миром;
управление остановлено владельцем до съёмки галереи. GameTest/dedicated:0.
По последнему указанию новые запуски отменены, визуально проверяет владелец.
Пользовательские10 удалений future/** сохранены вне коммитов.
[Проверки, артефакт и установка](implementation/kurgan-16/README.md).

## Образцы курганных текстур — #46 (2026-10-18)

Подготовлены3 самостоятельных native16 PNG: зелёный камень, голубой булыжник,
резной погребальный камень. Они находятся вне production, с реальными PNG
сравнениями256→16, 1×/4×/16×, повтором3×3 и SHA256 всех11 оригиналов.
Изучены все11; ссылки92 block models (76 shaped) проверены статически.
Оригиналы, art/kurgan-0.9.6 и runtime textures сохранены. Решение по стилю
ожидает владельца; оставшиеся8 и #47 (версия/PolyMC/showcase) ещё не выполнены.
Клиент/сервер/GameTest/PolyMC:0 запусков. Это не Minecraft render acceptance.
[Образцы и краткие пояснения](../art/review/kurgan-vanilla-style/README.md).

## Выдержка в чане — #45 (2026-10-18)

Выдержка перенесена в существующий `fermentation_vat`: ПКМ суслом по пустому
свободному чану, затем тарой для розлива; пустая рука показывает качество.
15 рецептов изготовления, 11 напитков, 8 порций и границы 2400/8400/12000 тиков
сохранены. `small_keg` не менялся. Новая выдержка блокирует сырьевые слоты и
изготовление до окончания розлива; нового глобального polling нет.
`wooden_barrel` остаётся зарегистрированной для старых миров/предметов и NBT,
но убрана из рецептов, Creative/JEI и 364 шаблонов новых построек. Установленные
бочки сохраняют инвентарь/остатки; при разрушении оставшиеся порции выпадают.

Java21 clean build PASS (compileJava FROM-CACHE); 7455 plain-JVM NBT/quality/pour
assertions и 23723 resource/model/structure/JAR checks PASS. Сравнение typed NBT
подтверждает только замену декоративных palette states, без смены остальной
постройки. Клиент/сервер/GameTest: 0 запусков; gameplay/GUI/relog/MP NOT_RUN.
Версия пока 1.9.7.5; установка PolyMC ожидает утверждения и интеграции #46/#47.
Пользовательские 10 удалений future/** сохранены вне изменений этой задачи.
[Краткая проверка и ручные шаги](implementation/barrel-retirement/README.md).

## Консолидация и очистка main — issue #43 (2026-10-18)

PR #44 объединяет target-lifecycle guard Полоза из #38, allocation-only cube из #37,
подробный README, каталог текущего содержимого, CHANGELOG и VERSIONING. Версия
исходников остаётся 1.9.7.5; gameplay, баланс, registry ID, src resources и art сохранены.
17 старых JAR, 560 логов и 666 legacy work-файлов сняты с индекса с проверенным архивом;
31 зависимый исторический install/verify инструмент остановлен явным архивным режимом.
Реально удалены 55 временных каталогов (2381687605 bytes) и 10 неактивных worktree;
unique/ignored данные, старые refs и незакоммиченные изменения сохранены. V3 активна.
Независимый publish clone сохранён: автоматическая проверка запретила его удаление.
Java21 clean build PASS (compileJava FROM-CACHE), 3919 JSON/1325 PNG/6804 resources/
1685 classes, production JAR parity и 10000 offline cube cases PASS. Minecraft/сервер/
GameTest:0 запусков. Визуальная, звуковая, полная игровая и MP приёмка NOT_RUN.
Merge доставляется через PR #44; master только FF после архивирования старой вершины.
V3/75 карточек и баланс #27 в интеграцию не включены;55-card #40 не подменяет V3.
[Фактические результаты, архивы, ограничения и путь main/master](maintenance/issue-43/REPORT.md).

## Арбалет Горыныча — 1.9.7.5 (2026-10-14)

Арбалет трёх дыханий: каждая стрела наносит минимум14 базового урона до брони;
обычный змеиный арбалет сохраняет прежний множитель. Режим сохраняется отдельно
на projectile, включая отложенные стрелы: прямой Мор немедленно даёт Weakness40,
Пламя ignite40. Существующее моровое облако80ticks сохранено. Пламя и Буря
получили видимые кольца radius3 на40ticks, с client-only emitters≤32,
очисткой при logout/смене мира и без нового server tick polling. Буря сохраняет
+3HP/SlowII40/отбрасывание/LOS/targetCD30. RU/EN tooltip объясняет три силы;
генератор соответствующего описания обновлён. ID, модели и пользовательские
изменения в остальных системах сохранены.

Java21 clean build PASS;13/13 native GameTests PASS (1 реальный GameTest server,
0 клиентов). Финальная повторная clean build PASS;13731 resource/JAR checks PASS.
Ранняя проверка JAR обнаружила старое описание, финальная пересборка исправила.
Production1.9.7.5 установлен в PolyMC1.21.1 Testing: SHA256/backup PASS,
607 прочих файлов сохранены, миры/старый1.16.5 не тронуты. Client visual/MP/ручная
игровая приёмка NOT_RUN. Commit/push не выполнялись.
[Отчёт](implementation/crossbow-1.9.7.5/REPORT.md).

## PolyMC: исправления урона и навыков — 1.9.7.4 (2026-10-14)

Установлена единая версия поверх полного snapshot2389b876: исправления #3/#6/#4/#5, прежние модели/тройная стрельба без бутылок, существа и все руны сохранены. Clean build PASS,12/12 native tests (1 GameTest server,0 clients), регрессии Tool/Craft/Weapon/Night/Poloz PASS,21432 gates PASS. SHA256588d1f21841868baedbd71ebaea03dff0d45004213247a24bbb0c4c1e4b68528. PolyMC Testing обновлён с backup;598 прочих файлов/миры сохранены. Клиент/connected dedicated NOT_RUN; #7–#9 и полная приёмка #10 не завершены. docs/implementation/reward-polymc-update/REPORT.md. Commit/push интеграции не выполнялись.

## PolyMC: инструментальные руны III–IV — 2026-10-14

По запросу пользователя установлен последний production JAR 1.9.7.3 в существующий PolyMC Testing (Minecraft 1.21.1 / NeoForge 21.1.255). SHA256 1ade97e48ee8f8a12348b377e22f4f8edcb8f00cf4927894917d3974a95871f7 совпадает с gates: clean build PASS (GIT_PUBLICATION_BUILD_2026-10-14.log), 21993 resource/JAR checks PASS; повторная сборка при копировании не выполнялась. Backup прежнего JAR и instance.cfg проверен, 472 прочих файла сохранены побайтно. Миры и старый 1.16.5 не менялись. Клиент/сервер: 0 запусков; игровая приёмка остаётся открытой. Установщик tools/install_tool_runes.ps1; квитанция docs/implementation/tool-runes-iii-iv/POLYMC_INSTALLATION.json. Commit/push не выполнялись.

## Общая публикация GitHub — 2026-10-14

По запросу пользователя подготовлен полный текущий snapshot для main и codex/tool-runes-iii-iv: production исходники/ресурсы, поддерживаемые tools и доказательные отчёты. Версия1.9.7.3; новые механики этим шагом не добавляются. Исторические записи «commit/push не делались» описывают предыдущие задачи. [Актуальная сводка для ChatGPT](CHATGPT_HANDOFF_2026-10-14.md). Повторный clean build и проверка production JAR фиксируются в GIT_PUBLICATION_BUILD_2026-10-14.log и последнем RESOURCE_JAR_CHECKS.json. Игровых запусков этой публикации0; PolyMC/миры не менялись.

## Инструментальные руны III–IV — 2026-10-14

COMPLETE_WITH_LIMITATIONS:8 новых ID, прежние46 сохранены; Shift области кирки/
лопаты9/18, мотыги9/25, лесоруб R4/R7/48–96 с консервативными прямыми1×1/2×2
деревьями и dimension provenance. Native break/use/rollback confirmation,
4 extras/tick и256 tree reads/tick, без world scan/chunk forcing. Старые крафтовые/
трофейные руны, blankIV, баланс оружия/боссов и art сохранены. Rune-anvil recipes,
RU/EN,8 прозрачных64px sprites, magic catalog и generic JEI discovery.
Java21 clean build PASS;11731 Tool +3518 Craft +398 Rework +20491 Rare offline,
21993 source/resource/JAR checks PASS. Production1.9.7.3, SHA256 1ade97e48ee8f8a12348b377e22f4f8edcb8f00cf4927894917d3974a95871f7.
Реальных client/server/GameTest/PolyMC0; GUI/loot/claims/MP/relog/MSPT NOT_RUN.
Старое происхождение брёвен не доказуемо на100%; сложные деревья пропускаются.
Основная рука/Survival–Creative; offhand/adventure остаются vanilla single action.
Веткаcodex/tool-runes-iii-iv, HEAD сохранён, staged/commit/push/PolyMC/миры не менялись.
[Отчёт](implementation/tool-runes-iii-iv/REPORT.md).

## PolyMC: руны III–IV — 2026-10-14

Установлен прошедший gates JAR1.9.7.3 с крафтовыми рунами III–IV в существующий PolyMC1.21.1 Testing. SHA256 совпал; backup прежнего JAR/instance.cfg, 461 прочих файлов сохранены побайтно. Имя дополнено «Руны III–IV»; миры/старый1.16.5 не затронуты. Игровых запусков0, commit/push нет.
[Квитанция](implementation/runes-craft-iii-iv/POLYMC_INSTALLATION.md).

## Крафтовые руны III–IV — 2026-10-14

COMPLETE_WITH_LIMITATIONS: сохранены 42 прежних rune ID, четыре новые крафтовые IV
и blank_rune_4/perunite; пять rune-anvil recipes, RU/EN, JEI subtype/каталог,
прозрачные 64px иконки по чертежам. Новые IV независимы от трофеев; защита/Ярость/
лечение/скорость руд и Fortune подключены к существующим событиям и loot hook.
Предыдущие согласованные условия Last Stand/Глубин сохранены и отличия явно описаны.
Java21 clean build PASS; 3518 новых +398 RuneRework +20491 RareRune offline
assertions, 21793 source/resource/JAR checks PASS. Production версия1.9.7.3
не менялась; итоговый SHA256 1da8fff39146a1070b4340ea1332ab6c54c8736cbcb85ea5b6263e08d77cbdf8.
Ранний build FAIL из-за ошибочно удалённого hook исправлен; mixin/чужие патчи сохранены.
Реальных client/server/GameTest/PolyMC запусков0; игровая GUI/loot/MP/relog/MSPT
приёмка NOT_RUN. Профили/миры, staged файлы и commit/push не затронуты.
[Отчёт](implementation/runes-craft-iii-iv/REPORT.md).

## Восстановление оружия и три головы арбалета — 1.9.7.3 (2026-10-14)

Заменяет непринятый визуальный результат1.9.7.2. Возвращены прежняя ornate коса
(60 деталей/32px иконка), texture atlas и цельное движение кисти/древка по дуге;
середина взмаха совпадает с серверным hit. Исправлены UV12 арбалетных моделей.
Арбалеты массивнее в first-person; у three_breath_crossbow три драконьи головы,
пасти/зубы/рога/глаза и три болта в charged state, серверные muzzle lanes0/1/2.
Убрана физическая перехватываемость стрел облаком Мора; bottle interaction сохранён.
Все6 сочетаний effect×volley в Survival: 3 arrows/3 hits, реальные HP/effects,
ammo1/durability3 PASS. Шесть реальных клиентов+6 integrated servers за задачу,
0 GameTest; одна fixture input ошибка исправлена, ранние art-FAIL не скрыты.
После запрета частых запусков соблюдён интервал≥10мин; последний запуск после15:11MSK.
Clean build Java21 PASS;41260 focused +Night40/10000 +Poloz4215 PASS;
17873 gates/213 subscribers PASS, production classes совпадают с протестированными.
PolyMC Testing установлен на1.9.7.3 с SHA256/backup,460 прочих файлов сохранены;
миры и старый1.16.5 не затронуты. Commit/push не выполнялись.
MP/relog/длительный gameplay/FPS/MSPT NOT_TESTED. [Отчёт](implementation/weapon-visual-repair/REPORT.md).

## Исправления оружия и Полоза — 1.9.7.2 (2026-10-14)

COMPLETE_WITH_LIMITATIONS: рука/рукав у двух арбалетов в first-person;
посох Лихо и 12 voxel-моделей оружия — хват/вертикальные third-person transforms;
коса — непрерывная дуга с общей точкой хвата, без vanilla вращения рукояти.
Полоз принимает урон по любой части тела с первой фазы, ×1; scar-only gate удалён.
Арбалет трёх дыханий всегда стреляет тремя стрелами с выбранным эффектом:
Shift+ПКМ — Мор/Пламя/Буря, Shift+ЛКМ — веер/очередь 0/4/8 ticks,
без расхода бутылочек. ID/силуэты/материалы/рецепты и параллельные правки сохранены.
Clean build Java21 PASS; 41233 focused, Night40/10000 simulations,
Poloz4215 native checks PASS; 13606 resource/JAR gates, 213 subscribers PASS.
Production JAR1.9.7.2 установлен в отдельный PolyMC Testing с backup;
455 остальных файлов сохранены побайтно (название instance.cfg обновлено отдельно).
Клиент/сервер/GameTest/PolyMC запусков0; visual/gameplay/MP/relog/MSPT NOT_RUN.
Ошибки ранних fixtures/import исправлены и сохранены в истории логов.
Не выполнялись commit/push. [Отчёт](implementation/weapon-feedback/REPORT.md).
Нижеследующие ограничения по отсутствию скриншотов оружия — исторические;
предоставленные в этой задаче скриншоты использованы для адресных исправлений.

## Творческий инвентарь — 1.9.7.2 (2026-10-14)

COMPLETE_WITH_LIMITATIONS: 10 вкладок, прежние 9 registry ID сохранены, добавлен weapons.
Текущие регистрации статически сверены: 849 ID, 835 видимых / 837 стеков, 14 намеренно
скрытых. Явный каталог группирует оружие, комплекты брони, древесину, рецептурные уровни
рун; runtime сборка вкладок охватывает весь namespace с контролируемым fallback.
Механики/registry ID предметов этой задачей не менялись; параллельный патч оружия
1.9.7.2 сохранён в итоговой сборке. Native runtime/визуальная проверка NOT_RUN:
0 клиентов, 0 серверов, 0 GameTest по запрету пользователя.
Clean build Java21/NeoForge21.1.255 PASS; 1710 Java catalog checks и 24145
source/resource/JAR gates PASS. Production JAR в build/libs, установлен в PolyMC
1.21.1 Testing с SHA256/backup; 457 прочих файлов и миры сохранены.
Ветка `pred-relise`, HEAD `b76160800223267abc04e7c6d46812dfba8976dc`; commit/push/merge не делались.
[Отчёт и ведомость](implementation/creative-inventory/REPORT.md).

## Исправления существ после игрового теста (2026-10-14)

COMPLETE_WITH_LIMITATIONS: Peaceful/Creative защита, броня и локальный AI Полоза,
экипировка стража Чуди, устойчивый страх Карачуна, общая поза/контакт захвата Горыныча.
13 повреждённых ru_ru значений восстановлены; параллельные оружие/броня/руны сохранены.
Clean build Java21 PASS, 107 серверных assertions (4 изолированных GameTest запуска,
первый FAIL исправлен), 1545 HP/NBT assertions PASS. Клиентских запусков0;
визуальная проверка остаётся ручной. Свежий JAR установлен в PolyMC Testing,
имя дополнено «Исправления существ» поверх «Патч оружия», миры сохранены.
Production metadata1.9.7. Отчёт: [REPORT](implementation/creature-feedback/REPORT.md).

## Патч рун — A–C (2026-10-14)

COMPLETE_WITH_LIMITATIONS: 37 прежних ID сохранены, 4 крафтовые III и
Велесова клятва IV у Яги; совместимость/старые дубликаты/legacy extraction,
death immunity, source-local морозная защита, 4-секундное Подавление,
числа старых рун и немедленный anvil refresh. Оружие/броня и чужой HP-патч
сохранены. Production версия1.9.7. Финальная clean build PASS; 390/20491/75064
основных offline assertions и15333 resource/JAR gates PASS. Earth6186,
Frost6946, Poloz4290 PASS. Два старых смежных fixture FAIL: RewardModelChecks
(cube counts8/8/7), GorynychChecks (ожидает старые3600/1800/180). Они не исправлены.
Client/server/GameTest/PolyMC0; игровые события/GUI/MP/relog/MSPT NOT_RUN.
Профили и миры не менялись. Исторические creative receipts неотличимы по
старому формату; новые помечены отдельно. Отчёт: docs/implementation/runes-patch/REPORT.md.
Ветка runs, собственные отчёты staged; commit/push не выполнялись.

## Подготовка независимой проверки1.9.8.2 —2026-10-14

Итоговый REPORT.md в docs/implementation/creatures-1.9.8.2 дополнен полным44-file
списком, миграцией/фазами, Git/SHA256 и границами тестов:1545 assertions =1524
helper/native behaviour +21 фиксированное значение; игровых сценариев0.
Только документация, без изменения production/test-кода и повторной сборки.
Параллельные изменения основного дерева отделены от frozen audit staged diff.
Неподтверждённые config/ledger/phase/runtime случаи переданы для анализа;
commit/push/PolyMC/вторая часть не выполнялись.

## 1.9.8.2 — баланс существ, этап1 (2026-10-13)

Реализованы согласованные HP и адресная версионная миграция Entity NBT/ledger:
Атаман100, Тугарин400, Соловей350, Лихо350/mor700, cursed_volkolak700,
хранители900/800 (45/40), Морозко1400 default, Карачун1600 default,
Горыныч2800 logical/1500/75 с service1024. Жар-птица3000 защищена от боевой
смерти с сохранением прежнего ответного scheduler. Калин1800/Полоз3000/volkolak165,
урон, серебро, ID, награды, art и production-версия1.9.7 сохранены.
Локальные load adapters, без глобального tick polling или новых сетевых пакетов;
Morozko uncapped getter использует существующий attribute sync, IceDouble исключён.
Clean build Java21 PASS;1545 native offline и актуальные JAR/resource gates PASS.
Game/client/server/GameTest0 запусков, relog/MP/audio/MSPT NOT_RUN.
Параллельные опубликованные оружие/броня сохранены. Подготовлена отдельная ветка
audit со своими файлами; commit/push/PolyMC не выполнялись. Следующий этап не начат.
Отчёт: docs/implementation/creatures-1.9.8.2/REPORT.md; SHA256/CHECKS/FILES там же.
Нижеследующие записи описывают прежние этапы.

## Патч оружия — ревизия брони поверх 1.9.7 (2026-10-13)

Переработаны22 иконки и worn textures существующей экипировки;3 item model переведены на generated,4 client armor classes уточняют UV/декор/различимость плащей. Gameplay/ID/версия сохранены,7 предметов одежды без изменений. Параллельный опубликованный патч оружия f53447cc включён. Clean build PASS;8771 native geometry/UV и39061 production/resource checks PASS. PolyMC Testing обновлён с backup;381 прочий файл сохранён. Реальных запусков клиента/сервера/GameTest0. [Аудит, файлы, before/after и границы проверки](armor-revision/REPORT.md).

## Патч оружия — поверх 1.9.7, 2026-10-13

Исправлены фильтр целей/учёт боевых атрибутов косы Карачуна и сброс её позы; компактная объёмная коса/16px иконка, разные модели копья победителя, кистеня атамана, посоха Лихо и Сабли Воеводы. Арбалеты: безопасный pull predicate и компактные first-person transforms. Registry ID и номер версии сохранены. Clean build + Night offline PASS; 15764 resource gates, 921 item models, 208 subscribers PASS. Игровых запусков 0; визуальная/боевая приёмка OPEN. PolyMC Testing обновлён с backup, имя дополнено «Патч оружия». Параллельные правки брони/аудита сохранены отдельно. [Отчёт](verification/weapon-patch/REPORT.md).

## Финальный аналитический аудит1.9.8.1 —2026-10-13

COMPLETE_WITH_LIMITATIONS:69/69 существ классифицированы,262 известные строки
действий обработаны +3 исключённые недостижимые ветки (265). VERIFIED/PROPOSED
разделены;4 группы решений перед реализацией, source/runtime ограничения явные.
Горыныч target2800/1500/75; Лихо350/mor700 тот же ID; Жар-птица3000 и обязательная
неубиваемость (текущий путь смерти — дефект); cursed_volkolak700 PROPOSED.
Архитектура runtime и production-версия аудитом не менялись,1.9.8.2 не начата.
Build/resources NOT_REPEATED, игровые запуски0. Параллельные production правки
сохранены, текущий JAR hash отличается от исторического; чужие gates не приняты.
Результаты: docs/audit/creatures-1.9.8.1/REPORT.md. Git/PolyMC и маркировка
«патч оружия» после реализации: POST_IMPLEMENTATION_REQUIREMENTS.md там же.
Нижеследующие IN_PROGRESS записи — история предыдущих итераций.

## 1.9.8.1 — продолжение аудита существ (2026-10-13)

Аудит IN_PROGRESS: CREATURES.csv покрывает 111 исходных EntityType (69 самостоятельных существ, 42 auxiliary); ATTACKS.csv содержит 262 строку атак/компонентов/proc/боевых действий. FORMS.md и TECHNICAL_RISKS.md фиксируют формы и точки будущих изменений. Полная семантическая проверка всех полей/покрытия ещё не завершена; остаток явно в UNVERIFIED.csv. Табличная проверка PASS не доказывает полноту механик. Production 1.9.7 и JAR SHA256 сохранены, gameplay/ресурсы/ID не менялись; следующий патч не начат. Build/resources повторно NOT_RUN (только документы); game/client/server/GameTest: 0 запусков. Подробнее: docs/audit/creatures-1.9.8.1/REPORT.md.

## 1.9.8.1 — начат аналитический аудит существ (2026-10-13)

Статус IN_PROGRESS: исходный индекс 111 деклараций EntityType, выдержки кода и проверенные расхождения HP; полная ручная таблица атак/форм/типов/спавна ещё не завершена. Production остаётся 1.9.7; баланс, механики, ресурсы и ID не менялись. Следующие патчи не начаты. Генератор tools/creature_audit_1981.py работает только с документацией; новой runtime архитектуры нет. Отчёт: docs/audit/creatures-1.9.8.1/REPORT.md; результаты gates: CHECKS.md в том же каталоге. Клиент/сервер/GameTest: 0 запусков.

## 1.9.7 — Оберег двух зим (2026-10-13)

Один артефакт из существующих наград Морозко/Карачуна; точные рецепты оригинала и копии с серверной квитанцией и возвратом шаблона. Два серверных режима, пассивы во всех измерениях, активный щит/волна только в Нижнем мире, общий persisted кулдаун, персональная поэтапная аура и собственный эффект без удаления чужой Слабости. Старые боссы и награды не изменены.

Чистая production сборка + десять offline наборов **PASS**; новый набор 4853, ресурсные gates 10991. PolyMC Testing обновлён до **1.9.7**, SHA256 0f1e76dd6bef952350a67d83da5357cc26bcc8dcb290b7eb281c8e46d2290e0e; предыдущий JAR в backup, остальные файлы/миры/конфигурации сохранены. Клиент/сервер/GameTest: **0** запусков; игровые проверки не заявляются.

Архитектура: существующий PathData/RPG sync; WintersEvents — события боя и локальный cast, inventory/aura раз в 20 тиков плюс лёгкий Inventory.setChanged; WintersRecipe — exact 3×3 CustomRecipe/native remaining-items; WintersTemplates — SavedData выданных оригиналов. CLIENT property/tooltip; stable KB modifier, свой щит и эффект aura. Автоматический Crafter отключён для этих двух рецептов. Будущие milestone не реализованы.

Подробнее: docs/TWO_WINTERS_1.9.7.md, docs/verification/winters-1.9.7/REPORT.md.

## Змей Горыныч — 1.9.6, 2026-10-13

Добавлен сюжетный Горыныч Змеиной горы: один привязанный к instance босс, три отдельные multipart головы, серверные логические 3600 HP, наземная и воздушная фазы, появление крыльев при 50%, несмертельное поражение при 5%, очищение порошком/пеплом/водой в любом порядке, живая мирная форма. Яйцо `zmey_gorynych_spawn_egg` находится в существующей вкладке «Мобы» и создаёт самостоятельный бой без сюжетных требований. Самостоятельные экземпляры не выдают сюжетные награды.

Worldgen, завершение команды создания горы и загрузка её чанков подключены к локальному населению; проверяются UUID, последнее место и готовность entity chunks. Повторное посещение существующей горы также подключено. Девять размеченных точек, переходы между крупными верхними помещениями, отдельные нижние компоненты; перед второй фазой выполняется явная сцена выхода в открытый двор. Под потолками/в интерьерах шеи, хвост и крылья складываются. При движении и поиске пути не используются принудительные загрузки чанков.

Реализованы независимые атаки голов с бюджетом двух крупных действий, захват с сопротивлением/помощью и пределом три секунды, каменные копья с геометрическим эффектом, хвост, дождь Мора, огненная бомбардировка, ветер, пикирование и десятисекундные наземные окна. Три временных собираемых дыхания расходуют общие три порции области. Снаряды/области не портят блоки; их время жизни и количество ограничены.

После полного очищения участникам выдаются 4 чешуи, печать и одна равновероятная руна; полному инвентарю оставляется ожидающая персональная награда. Receipt игрока сохраняется вместе с инвентарём до world claim. Чешуя после спасения — одна на игрока раз в 72000 тиков. Три руны интегрированы в существующую установку/слоты и реальные hit events. Змеиный арбалет/сталь добавлены в лут крепости; арбалет трёх дыханий хранит раздельные заряды и использует vanilla arrows. Улучшения оружия/четырёх частей перунитовой брони сохраняют допустимые компоненты и долю износа. Все четыре части имеют отдельную worn geometry и эффекты.

Для связки оберега сохранён ID `mor_ward`: обе детали хранителей + существующее Око Мора создают собранный многоразовый экземпляр с `GuardianWard196`. Ранние экземпляры остаются расходуемыми. Защита горы требует победы над заражённым Лихом, обоих персональных получений фрагментов и наличия/действия оберега; активные атаки Горыныча этим оберегом не блокируются. Ownerless crafter сохраняет запрет персонального рецепта. Новый портал, измерение и объединённая руна не добавлялись.

Native mesh: 40 bones, 292 cuboids, 7008 проверенных UV vertices; 9 согласованных appearance atlases, реальные 3D состояния арбалетов, 4 worn armor models, три разные рунические иконки, 31 собственный синтезированный OGG. Генератор воспроизводит актуальные ресурсы и Java mesh; существующие генераторы горы/финальной 1.8.5 сохраняют новую интеграцию.

BUILD SUCCESSFUL in 1m 32s. Девять offline наборов — PASS; 17065 production/resource gates — PASS. PolyMC Testing установлен на 1.9.6, SHA256 `e197311398f3634c3e2303dce37535d6dd457f1cf88e52e503452d32125a37aa`; 311 остальных файлов профиля сохранены побайтно. Реальных запусков клиента/сервера/PolyMC/GameTest: **0**. Gameplay/MP/перезаход, seed QA, runtime visuals/audio, MSPT/FPS: **NOT_RUN / NOT_MEASURED**. NBT roundtrip и offline projection не являются игровым тестом. [Отчёт](verification/gorynych-1.9.6/REPORT.md).

## Яйца стражей и население святилищ — 1.9.5.1, 2026-10-13

По запросу игрока добавлены яйца Стража угасшего пламени и Хранителя осквернённого родника в существующую вкладку «Мобы». Vanilla-модель яйца с индивидуальными цветами, ru_ru/en_us; генератор guardian_assets включает воспроизводимое создание новых ресурсов. Остальные существующие яйца, рог Полоза и тотемы Морозко/Карачуна сохранены. Горыныч отсутствует в текущем реестре: ТЗ 1.9.5 предусматривает гору и заблокированный путь к будущему логову, полноценного босса откладывает на 1.9.6.

Два стража святилищ создаются вместе с загруженным местом, до входа игрока в арену. Worldgen передаёт метаданные на серверный поток; существующие загруженные сердца повторяют локальную проверку не чаще раза в 20 тиков на instance. Проверяется готовность чанков всей комнаты, UUID/последняя позиция и период восстановления; дубли удаляются. Чанки принудительно не загружаются. SEALED-страж присутствует, но не начинает бой до прежнего открытия святилища. CLEARED восстанавливает очищенную форму. Для уже существующих святилищ тот же механизм действует при загрузке их сердца.

Яйца и /summon создают самостоятельную арену 21×9×21 у места появления: моб не исчезает из-за отсутствия привязки к структуре. Запечатывание святилища не применяется; очищение самостоятельного стража завершается ПКМ по нему после победы. Эта форма не устанавливает блоки/пьедестал, не выдаёт фрагменты святилища; родниковое лечение от отсутствующего структурного источника отключено. Структурная версия сохраняет вентили, источник и награды. Этот патч меняет население двух святилищ, а не условия ритуальных охот и курганных встреч прежних версий.

Чистая сборка и восемь offline наборов — PASS; 16407 production/resource gates — PASS. Проверены сохранение самостоятельной привязки, границы арены и сброс срока проверки населения при загрузке SavedData. Это offline NBT-проверка, не игровой перезаход. PolyMC Testing обновлён на 1.9.5.1; 454 остальных файла профиля сохранены побайтно. Клиент/сервер/PolyMC/GameTest не запускались: 0 реальных запусков; появление мобов, бой, игровой перезаход и runtime визуализация — NOT_RUN. [Отчёт](verification/guardian-spawn-1.9.5.1/REPORT.md).

## Змеиная гора — 1.9.5, 2026-10-13

Реализована редкая единая структура Overworld 128×144×128: проклятый холм, крепость, два различных входа, связанная сеть тоннелей/пещер, внутренние лестницы, двор, залы, четыре башни с ярусами, галерея, оружейная, разрушенная комната и заблокированный путь к будущему логову. Одна детерминированная HillPlan используется native worldgen и операторской командой. 11 блоков с моделями/32px текстурами, 11 рецептов, пять собственных OGG, лут и сказание.

Команда /slavicmyths generate_serpent_hill [x y z] [rotation] размещает комплекс поверх обычных блоков в авторском объёме, сохраняет bedrock, проверяет высоту/границу мира. Работа распределена по 4096 проверкам клеток на серверный тик; очередь и cursor сохраняются. Глубинный Мор наносит исходные 3 HP каждые 30 тиков с обычной обработкой брони. Существующий mor_ward в любом слоте инвентаря или активный WARD_EFFECT полностью отключает эту ауру. Две детали 1.9.4 защитой не считаются; готовый оберег не добавлен.

Чистая сборка, восемь offline наборов и 23227 production/resource gates — PASS. Статический граф подтверждает маршруты между входами и доступность 15 именованных помещений/башен; это не игровой pathfinding. PolyMC Testing обновлён на 1.9.5; 374 других файлов сохранены побайтно. SHA256: dbdb101677397ac28ebe4b937d6f2352956fb4e40f36d6c3c55aebd885047b22. Клиент/сервер/PolyMC/GameTest не запускались: 0 реальных запусков. Команда в superflat, natural seed generation, MP, runtime light/visual/audio и MSPT/FPS — NOT_RUN / NOT_MEASURED. [Отчёт](verification/serpent-hill-1.9.5/REPORT.md).

## Команды создания святилищ — 2026-10-13

Добавлены operator-level-2 команды `/slavicmyths generate_fire_sanctuary [x y z]` и `/slavicmyths generate_pure_spring [x y z]`. Полное принудительное размещение через обычные SanctuaryPiece, StructureStart, маркеры, лут и ссылки чанков; собственный UUID и SanctuaryRecords. Только Overworld, проверка высоты/границы/загруженных чанков/существующих начал/всех шаблонов до изменения блоков; новые чанки не подгружаются. Естественная генерация, registry IDs и баланс не изменены. RU/EN для ошибок и результата. [Использование](SANCTUARY_COMMANDS_1.9.4.md).

Отдельная 1.9.4: clean build + семь offline suites — PASS; 23306 production/resource gates — PASS. SHA256 `0142494a6445e325c48194949f7d1a957110942357b232e20155346f8334b1b8`. Установлена в PolyMC Testing с backup, 360 прочих файлов сохранены побайтно. Начатая 1.9.5 сохранена локально; её clean build и те же семь offline suites также PASS, но это не игровая приёмка/готовность 1.9.5. Реальных запусков client/server/GameTest агентом: 0; команды в игре NOT_RUN. По запросу пользователя версия 1.9.4 с командами опубликована в GitHub вместе с проверенным JAR; версия 1.9.5 не включена. Квитанции: `docs/verification/sanctuary-commands-1.9.4/`.

## Исправление загрузки 1.9.4 — 2026-10-13

Устранён пользовательский startup crash `axis` при регистрации SanctuaryDecor: постоянный MapColor вместо функции OAK_LOG, требующей AXIS. Аналогичный путь SanctuaryBlock защищён. Registry IDs, facing/lit, gameplay и ресурсы сохранены. В production JAR изменились ровно два класса; ресурсы побайтно идентичны исходной 1.9.4. Начатая 1.9.5 сохранена отдельно от опубликованной версии.

Clean build, семь offline suites (63 новые проверки создания украшений) и 23304 production/resource gates — PASS. PolyMC Testing обновлён с backup; 305 прочих файлов сохранены. SHA256 `410407f918508c0b04fef14aca0c7cad7aa39b7d77c4279f2e1088bd870837c3`. Реальных запусков игры/client/server/GameTest агентом: 0; игровой запуск исправления NOT_RUN, MSPT/FPS NOT_MEASURED. [Отчёт](verification/startup-fix-1.9.4/REPORT.md).

## Осквернённые хранители — 1.9.4, 2026-10-13

Добавлены два уникальных хранителя в существующие залы 1.9.3: деревянный Страж погасшего пламени (500 HP, очищение на 25 HP) и блочная Хранительница источника (450 HP, очищение на 22.5 HP). Серверные фазы/телеграфия, сменяющиеся безопасные полосы, два затвора лечения, водоворот, плети, разрушаемая темница, ограниченный разлив и два временных водных воплощения. Очищенные сущности остаются нейтральными. Старые ритуальные флаги открывают залы без регенерации структуры.

Два персональных фрагмента выдаются постаментами после очищения, один раз на UUID/тип независимо от числа святилищ. Поздние участники могут получить свою копию; полный инвентарь не расходует право. Самостоятельные модели (14/34 bones, 92/119 cuboids), совместимые очищенные UV/текстуры, скелетные анимации, две сопряжённые иконки, 28 собственных OGG, три записи сказаний. Готового оберега, его рецепта, новых святилищ/измерений и контента 1.9.5–1.9.6 нет; лут/рецепты/NBT 1.9.3 сохранены.

Clean build, семь offline наборов и 23304 production/resource проверок — PASS. PolyMC Testing обновлён до 1.9.4; 302 остальных файлов сохранены побайтно. SHA256: 04a995f83146dd05a4c3c0c22950ec53fbe0da660026a890b916df8d2235f0f7. Реальных запусков клиента/сервера/PolyMC/GameTest: 0. Бой, визуал/звук, MP, полный инвентарь в игре, перезаход и реальный restart/unload — NOT_RUN; MSPT/FPS — NOT_MEASURED. [Отчёт](verification/guardians-1.9.4/REPORT.md).

## Две древние святыни — 1.9.3, 2026-10-13

Реализованы две естественные структуры Overworld: Святилище неугасимого огня (80×76) и Обитель чистого ключа (96×86), 61 нативный NBT-шаблон. Восстановление огня, очищение двух бассейнов, однократные награды и тайники, девять предметов, семь рецептов, тематический лут, достижения и предания. Закрытые помещения содержат пустые постаменты и арены; будущие боссы и сюжетные обломки не добавлены.

Чистая сборка, шесть offline наборов и 22684 production/resource проверок — PASS. PolyMC Testing обновлён на 1.9.3; 301 остальных файлов сохранены побайтно, предыдущий JAR в резервной копии. SHA256: ab2397c7ae3aada79526305a620715515e986a7974cc31a54bd056cd4088f4e9. Клиент/сервер/PolyMC/GameTest не запускались: 0 запусков. Реальная генерация, игровой визуал/звук, MP и перезаход — NOT_RUN; MSPT/FPS — NOT_MEASURED. Структуры генерируются в новых чанках. [Отчёт](verification/sanctuaries-1.9.3/REPORT.md).

## Испытание Карачуна — 1.9.2, 2026-10-13

Реализован отдельный server trial: 1200 HP по умолчанию с собственным health ceiling (по существующему паттерну Полоза), остановка на 5%, 4 физических ворона, 3 фазы, голод/мороз/смерть/погребальный взмах/локальная ночь/страх/фиксированный Последний час. Независимые шкалы угасания/страха; логическая блокировка offhand сохраняет предмет и ванильную проверку тотема. Источники эффектов учитываются отдельно. Фантомы — только клиентские модели, без server entity IDs/AI/damage.

Самостоятельные boss/raven rigs (26/7 bones, 293/55 boxes), общая объёмная коса boss/item, отдельные пиксельные иконки, 24 собственных sound events. Коса: три серверных удара по дуге, две активные способности, общий hit ledger трёх лезвий, вампиризм по фактически снятому HP и независимые passive cooldown/target chains. Персональные награды: первое Чёрное крыло, каждый раз одна руна и осколок, независимый шанс косы. Отдельные NPC ветки и SavedData без сброса 1.9.1. Готовый тотем не проверяет репутацию; повторное испытание разрешено.

Финальная чистая сборка и пять offline наборов — PASS; production/resource gates 21918 — PASS. SHA256 `75f1cec8b4af7fe2f566765328ec2a0108617a85921a169e6b84f709a511ea70`. PolyMC Testing обновлён после gates; 297 остальных файлов сохранены побайтно. Предыдущий JAR сохранён в `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-karachun-1.9.2-20261013-113133`. Агент не запускал клиент/сервер/PolyMC/GameTest: 0 запусков. Игровые бой, MP, relog, визуал/звук/анимации — NOT_RUN; MSPT/FPS — NOT_MEASURED. Не реализованы 1.9.3, новые структуры/измерения/порталы и будущий рецепт оберега. [Отчёт](verification/karachun-1.9.2/REPORT.md).

## Исправление краша Полоза — 2026-10-13

Пользовательский crash-report crash-2026-10-13_10.21.57-client.txt фиксирует IllegalStateException: Not building! в PolozRenderer.render, строка 34, при отрисовке тела. Буфер тела получался до переключений на текстуры головы и глаз; MultiBufferSource.BufferSource в установленном Minecraft 1.21.1 завершает общий буфер при смене RenderType. Теперь буфер тела получается после головы/глаз и используется для непрерывного прохода тела и шрамов. Геометрия, текстуры, свечение, gameplay и registry IDs сохранены. Генераторы tools не содержат шаблона этого renderer.

Проверки завершены: gradlew.bat clean build — PASS; offline Морозко 6946, quest185 182, Полоз 4290, Mor blocks 120 — PASS; production/resource gates 19725 — PASS. Исправленный JAR установлен в PolyMC Testing после gates: SHA256 94e471d9c3c96a8d63168a499f6256015e870de8ecd227570d6d9def3ba951f1; предыдущий JAR сохранён, 456 остальных файлов профиля побайтно сохранены. Реальных запусков игры агентом: 0; исправление ещё требует ручного входа в мир с Полозом. Миры и NBT не редактируются.

## Испытание Морозко — 1.9.1, 2026-10-13

Реализована 1.9.1: 1000HP испытатель с неубиваемым5% финалом, морозная шкала/семь боевых действий, до3×50HP двойников, два NPC пути тотема, персональные награды/очередь, зерцало15HP/30с/180с, две руны и четырёхрежимный посох. Native rig30 bones/198 cuboids, общий boss/player staff, самостоятельные icons/3D heart,21 original OGG/RU+EN/книга/advancement. Будущие патчи не реализованы.

По последнему прямому указанию пользователя: готовый тотем не проверяет репутацию; креатив пропускает ожидание и участвует в AI. Рог Полоза — обычный ПКМ3с, без алтаря/Shift/подтверждения/4 праха/послебоевого cooldown; только одно активное мировое событие. Ошибочное сообщение «повержен» в IDLE устранено. Эти правила заменяют соответствующие пункты архива и старые записи ниже.

Финальный clean build PASS; 6946 новых offline policy/NBT/native UV checks,182/4290/120 регрессий и 19725 production/resource gates PASS. Генератор воспроизводит 6195 ресурсов без изменений. Codex client/server/GameTest/PolyMC starts0; реальный relog/MP/visual/audio/claim-моды NOT_RUN, MSPT/FPS NOT_MEASURED. Пользовательский отзыв о ранней креативной проверке не выдаётся за финальную игровую приёмку. Testing обновлён без запуска, 400 других файлов сохранены, миры/1.16.5 не тронуты. SHA `92933210b8f6b507385954fc3f9da65e2ec73d4ccf7ce27ff3d0ea8510703dcc`. [Отчёт](verification/morozko-1.9.1/REPORT.md).

## Окончательная редакция 1.8.5 FINAL — 2026-10-13

Архив FINAL реализован поверх прежней 1.8.5. Очищение открывается после исследования Ока Лиха (60 тиков, без изъятия) независимо от Овинника/Полоза: 4 порошка, рецепт и глава книги. Параллельное «Возвращение земли»: 20 уникальных фактических восстановлений → 8 порошков/175 XP. Есть один безопасный учебный участок или интерактивный paid fallback; основная цепочка не блокируется. Рецепт порошка заменён на shaped bone_meal/dandelion/charcoal →4 и доступен без сюжета. Очистка: связность 6 граней, радиус4, максимум9, график60 тиков, иммунитет восстановленного блока1800 тиков; замена/разрушение снимает защиту. Q6 даёт финальную главу/800 XP, без новой выдачи12 порошков. Прежние заработанные награды не отнимаются.

Финальный clean build **PASS** (1 мин); **182** offline/native recipe/policy/NBT проверки, **4290** проверки Полоза, **120** состояния mor-блоков, **7761** production/resource gates **PASS**. Повтор FINAL-overlay не изменил ни один из 6147 ресурсов. Игра/client/server/PolyMC/GameTest-миры: **0 запусков**; реальная приёмка/перезаход/MP/claim-моды/визуал/звук **NOT_RUN**, MSPT/FPS **NOT_MEASURED**. Testing обновлён без запуска; предыдущая1.8.5 сохранена вне mods; **384** прочих файла сохранены побайтно. Версия файла осталась1.8.5; final SHA256: `f2b66c13d9034f9e94c47385750d843ac6838b2ccfb7fec26fef7c80c64741c0`. [Отчёт FINAL](verification/quest-1.8.5-final/REPORT.md).

## Охвороби, союзники и очищение — прежняя редакция 1.8.5, 2026-10-12

Реализован архив 1.8.5: приглашение и безопасный добровольный переход к избе, локальная сцена прежних NPC, шесть основных/три волчьих этапа, сохраняемые доказательства и отложенные награды. Добавлены четыре предмета с отдельными иконками/моделями, эффекты оберега/чутья, временный волк-помощник, ограниченное сохраняемое очищение с восстановлением исходных состояний и персональные рецепты/глава книги. Ресурсы Полоза и рога сохранены.

Clean build **PASS**; offline-проверки: **124** новых, **4290** моделей/политик Полоза, **120** состояний mor-блоков. **7696** production/resource gates **PASS**. Запусков Minecraft/client/server/PolyMC для 1.8.5: **0**; реальные механики, multiplayer, перезаход, визуал/звук **NOT_RUN**; MSPT/FPS **NOT_MEASURED**. Тестовый профиль обновлён после gates без запуска и с backup/SHA; **383** прочих файла сохранены побайтно. [Отчёт и ограничения](verification/quest-1.8.5/REPORT.md).

## Полное ТЗ Полоза — 1.8.3.2, 2026-10-12

Полный архив Полоза 1.8.3.2 реализован поверх предыдущей итерации. Добавлены трофейная голова 3×3×4, гарантированная плеть, сердце с сохранением ID `poloz_core`, 8–12 чешуй и редкая руна IV (5%). Незеритовая броня укрепляется за две чешуи на часть: Shift + ПКМ по столу кузнеца с бронёй в руке; компоненты сохраняются. Бонусы частей суммируются: 55% сопротивления отбрасыванию и 22% снижения урона; комплект даёт ровно +2 HP и защиту от заражённой земли. Плеть: 13 урона, 6 блоков, широкая атака, три временных каменных шипа, 18 с перезарядки. Руна: четвёртое успешное попадание, разлом 6 урона/до четырёх целей/2 блока, 20 с перезарядки.

Clean build **PASS**; 4290 native geometry/policy checks (8 моделей), 120 native block checks, 31 финальный headless GameTest, 8208 resource/production checks **PASS**. Эта итерация: 5 headless JVM/5 GameTestServer миров, 4 успешных прогона; один прогон обнаружил оставленную тестом наград сохранённую перезарядку — изоляция фикстуры исправлена. Первая чистая сборка обнаружила UV-выход нового рога; исправление подтверждено. Клиентских запусков **0**; реальный multiplayer, перезаход и игровая оценка визуала/звука **NOT_RUN**; MSPT/FPS **NOT_MEASURED**. PolyMC Testing обновлён без запуска; 382 прочих файлов сохранены побайтно. [Отчёт](verification/poloz-full-1.8.3.2/REPORT.md).

## Поражённая земля и Великий Полоз — 1.8.3.1 / 1.8.3.2, 2026-10-12

Добавлены 12 постоянных mor-блоков с переносом состояний, ванильным дропом и локальными порогами 3/5 секунд. Моровой рог получает реальный рецепт и дропы компонентов, подтверждение/60-tick hold, переносимый флаг первого использования, повторную стоимость 4 праха и авторский 9-секундный сценарий. Один 3000-HP Полоз с 24 сегментами/3 шрамами, фазами 70/30%, шестью атаками, реальными разрушениями/заражением, временной войной мобов/тремя богатырями, сохранением события, трофеями и Книгой. Идентификаторы прежнего контента сохранены; временный MorZone 1.8.2 не заменён постоянным заражением.

Clean build PASS; 834 native geometry/policy assertions, 112 block state/physics assertions, 23 финальных headless GameTests и 8079 resource/production checks PASS. Всего 12 headless JVM запусков (10 завершённых успешно, 1 bootstrap failure, 1 исправленный runtime crash); загруженных GameTestServer миров 11, клиентских запусков 0. Реальный multiplayer/перезаход/визуал/впечатление от звука — NOT_RUN; MSPT/FPS — NOT_MEASURED. PolyMC Testing обновлён без запуска, предыдущая 1.8.3 сохранена вне mods. [Отчёт](verification/poloz-1.8.3.2/REPORT.md).

## Исправления по игровому отзыву — 1.8.3, 2026-10-12

Исправлены повторное добавление игроков в bossbar мёртвыми Kartaus/Kalin/HuntMob, очистка полос при выгрузке/удалении сюжетных боссов. Общий HeroThreats распознаёт все ACTIVE моровые формы (включая двойников), не полагаясь на vanilla Enemy; призванные четыре богатыря могут выбирать их целью. Моровые защищаются от напавших призванных героев; игрок остаётся приоритетом высших форм. Владение/сюжетные приоритеты/registry IDs сохранены.

Согласованные ранее правки рук/хвата теперь реально интегрированы: Алёша +5px, Добрыня +2.5px, Финист +1px, длина Ильи сохранена. Компактная палица 6.8px, цельная сабля, наконечник копья вперёд, прямой меч Финиста без прежнего постоянного 58° перекоса. Реальные native модели/текстуры и текущие pose controllers обновлены; рукояти закреплены в ладонях. Генераторы воспроизводят эти ресурсы. Макеты книги/дерева, recovery budgets и новая геометрия лука не внедрялись этим исправлением.

Clean build PASS, 2 224 988 native UV/хват/связность checks на 1004 профилях, 677805 регрессионных Mor checks/145 профилей, 71 UI helpers и 32807 resource/production gates PASS. Большинство native checks — вершины/UV, не игровые сценарии. 1.8.3 установлена в PolyMC Testing с backup 1.8.2, 336 остальных файлов профиля сохранены. Клиент/сервер 0/0; игровая приёмка после реального убийства/бой/визуал NOT_RUN. [Отчёт](verification/feedback-1.8.3/REPORT.md).

## Высшие моровые — 1.8.2, 2026-10-12

Реализованы высшие формы существующих Лиха и Овинника: отдельная геометрия/текстуры, светящийся глаз/глаза, анимации подготовки, атак, облаков, реакции на удар и смерти; усиленные vanilla-звуки. Обычные registry IDs, модели, ресурсы, спавны и квестовые механизмы сохранены. Формы доступны склянкой мора на незакреплённых неназванных мобах либо отладочным NBT; новых яиц и естественных спавнов нет.

Общий `HigherMor` управляет лучом Лиха, площадным ударом, Хворью радиусом 5, тремя углями Овинника, зольной завесой и необратимой второй фазой при <=50% HP. `MorZone` создаёт временные поверхности/миазм без изменения блоков: 3 секунды до Slowness I и урона земли, срок земли 180 тиков, облака 120, максимум 12 зон/моб. Во второй фазе Овинник создаёт землю каждые 30 вместо 60 тиков. `MorRejection` добавляет ограниченные стандартные target/avoid goals при присоединении сущности к миру; игрок остаётся приоритетом высшей формы.

`clean build`, production/resource gates, реальные native ModelPart/UV, policy/NBT и UI offline gates — PASS. Клиент/сервер запущены 0/0. Игровые бои, аудио, MP, реальный перезаход и MSPT — NOT_RUN по запрету ТЗ. Статус установки PolyMC и SHA256: [отчёт](verification/higher-1.8.2/REPORT.md), [квитанция](verification/higher-1.8.2/polymc-installation.json).

Размер Лиха: обычный текущий хитбокс 0.95×3.25 сохранён, высший 1.4×4.4 (масштаб модели 1.35), а не уменьшение до примерных 2.7 из референса. Овинник 1.65×3.1, масштаб 1.25×1.15×1.25. Визуальное соответствие и баланс ждут ручной приёмки.

## Мор — финальная реализация 1.8.1, 2026-10-12

Четыре старых entity IDs получили сохраняемые/synced моровые формы:50tick transition, fraction-preserving HP modifiers,9 спецприёмов включая одноразовых двух двойников змея30HP, rare natural4% day8/>160/Overworld, creative Склянка мора и одна existing Book discovery. Четыре native mor rigs/atlases/action poses, реальные vanilla sounds; ordinary geometry/assets/IDs сохранены. Bossbar/Jade HP скрываются на время двойников, configs не меняются. Будущие1.8.0 макеты/системы не переносились.

Final clean build PASS;237786 offline assertions/69 native profiles,71 UI helpers,33390 production/resource checks и174 subscribers PASS. Это не игровые тесты: Codex client/server starts0/0; visual/audio/combat/MP/rejoin/MSPT REQUIRES_MANUAL_QA/NOT_RUN. Финальный SHA `8ea198902455bbde7406f06cb1387a971dbb9b673403267d2697e0375f975322` установлен в PolyMC Testing с backup промежуточной1.8.1;327 прочих файлов сохранены. Миры/1.16.5 не менялись; commit нет. [Отчёт](verification/mor-1.8.1/REPORT.md), [установка](verification/mor-1.8.1/POLYMC_INSTALLATION.md). Старые записи ниже описывают свои, в том числе промежуточные, артефакты.

## Установка текущей сборки 1.8.1 — 2026-10-12

По подтверждению пользователя текущая рабочая сборка 1.8.1 установлена в PolyMC Slavic-Myths-1.21.1-Testing. Minecraft1.21.1/NeoForge21.1.255 сохранены. Clean build PASS;71 UI helper checks и223098 Mor offline checks/69 meshes PASS;22245 production integrity checks и174 event subscribers PASS. Клиентов/серверов запущено0; gameplay/визуальная игровая приёмка NOT_RUN. Backup1.7.9 вне mods,274 других файлов профиля сохранены побайтно; миры/старый1.16.5 не затронуты.

Согласованные макеты книги/графического дерева, коррекция рук/хвата и лимит двух восстановлений за бой пока НЕ внедрены: в текущей сборке они не изменились. Установка текущей версии не является завершением этих задач. Проверка verify_static_data_1211.py остановилась на устаревшем предположении о biome modifiers; её результат не считается PASS. JSON/source/JAR integrity проверены отдельным текущим verifier. [Отчёт](verification/update-1.8.1/REPORT.md).

## Аудит пропорций рук и лука — 2026-10-12

В исходной опущенной геометрии Алёша заканчивает кисть на4.3px выше нижнего края таза; подготовлена коррекция+5px суммарно. Для Добрыни предложено+2.5px, Финиста+1px; длина Ильи сохранена. Сравнение всех четырёх героев/двух рук и20 offline geometry checks PASS. Найдены зазоры крайних сегментов лука, перемещение тетивы целиком и проблема системы координат BOW; подготовлена геометрическая схема закреплённых концов/V-натяжения. Это только макеты/статический аудит: production integration NOT_IMPLEMENTED,1.7.9/PolyMC не менялись,build/client/server не запускались. [Измерения и макеты](design-review/1.8.0-proposal/revision-3/README.md).

## Второй макет поз и ограничение восстановления — 2026-10-12

Offline предложены forward позы четырёх богатырей со сгибом предплечья/нейтральным хватом, увеличенная головка палицы6.8px; учтены противоположное направление копья Добрыни и постоянный58° наклон меча Финиста. Код-аудит подтверждает отсутствие recovery counter у Финиста/Алёши вне их персональных encounter и отдельную автоматическую snapshot ветку HeroCalls. Пользователь согласовал reset лимита после завершения боя. Executable модель правила2 восстановления/3-е истощение=уход:376 checks PASS; production integration NOT_IMPLEMENTED. Production остаётся1.7.9; build/клиент/сервер не запускались,PolyMC не менялся. [Проверка и макеты](design-review/1.8.0-proposal/revision-2/README.md).

## Визуальное согласование после отзыва1.7.9 — 2026-10-12

Пользовательская приёмка выявила перегруз книги/навыков и неудачную геометрию оружия Ильи/Алёши. Подготовлены только макеты компактного каталога/скрытого списка, возвращённого дерева и явного назначения, компактной палицы/цельной сабли. Production остаётся1.7.9; игровой код/ресурсы/PolyMC не менялись,build не запускался,client/server starts0. До переноса требуется согласование внешнего вида по прямому запросу пользователя. [Макеты и границы проверки](design-review/1.8.0-proposal/README.md).

## Исправления по отзыву 1.7.9 — 2026-10-12

Удалён вход в классовое дерево: навыки/изучение/явное назначение через «Пути развития», занятые назначения подписаны. Исправлено повторное затемнение текста Screen.render в классах, книге и настройках. Три отсутствовавшие модели яиц чуди добавлены; всего10 яиц используют vanilla template/tint. 63 предметных GUI спрайта16×16,99 vanilla звуковых aliases для пяти новых врагов/коня; внешний вид врагов и server mechanics сохранены. Jade включён, численные HP, переключатель NumPad6 без изменения первого навыка NumPad1.

Clean build,71 UI helper и24984 production/resource checks PASS;173 subscribers,7505 prior entries идентичны. Реальных client/server starts0 по просьбе пользователя: новая UI/gameplay/audio/MP/relog/MSPT приёмка NOT_RUN. 1.7.9 установлена в PolyMC Testing,backup JAR1.7.8 и3 configs;321 другой файл сохранён,миры/1.16.5 не затронуты. [Полный отчёт](verification/feedback-1.7.9/REPORT.md).

## Интерфейсы и клиентский hotfix 1.7.8 — 2026-10-12

Исправлен реальный loading crash1.7.7: пустые GAME subscribers у трёх прежних HUD-классов. Дерево классов занимает всю ширину, уменьшены nodes, descriptions/effects по наведению; реальные клавиши и именованные конфликты. Книга без blur, подписанные категории/двухстрочные названия, поиск по категориям и деяниям, tidy parent graph. Длинные тексты используют wrap/ellipsis/full tooltip.

Clean build,71 offline и19760 production gates PASS. Реальные5 dev client starts (включая loading crash и fixture failure),20 финальных native кадров на GUI2/3/4 PASS. Отдельных GameTest server starts0; ручной gameplay/MP/relog/MSPT NOT_RUN. 1.7.8 установлена в PolyMC Testing,backup1.7.7 вне mods,322 других файлов сохранены; пользовательские миры/1.16.5 не затронуты,commit нет. [Отчёт](verification/ui-1.7.8/REPORT.md), [квитанция](verification/ui-1.7.8/polymc-installation.json). Старые результаты ниже относятся к своим версиям; серверные gates1.7.7 не обнаруживали исправленный здесь клиентский loading crash.

## Исправления игрового отзыва 1.7.7 — 2026-10-12

Исправлены самостоятельные цели четырёх богатырей против обычных врагов и пяти standalone/debug-боссов, сохранены сюжетные ограничения. Упрощены лица/волосы, снижена ширина Ильи, металлическая броня получила спокойные материалы вместо контрастных пятен. Общий компактный HUD двух героев и уменьшенные навыки у нижнего правого края. Исправлена ссылка Картауса в reward_interruptible без изменения registry ID.

Clean build и production/resource gates PASS; 136 финальных GameTests, 174 native профиля PASS. Реальных GameTest server starts 13, клиентов 0. Client визуал/HUD/полный бой/real MP/relog/MSPT NOT_RUN. По отдельному запросу пользователя 1.7.7 установлена в PolyMC Slavic-Myths-1.21.1-Testing; 1.7.6 сохранена в backup вне mods, 321 другой файл профиля сохранён побайтно. SHA256 установленного JAR совпадает с production gates. Клиент не запускался; commit нет. [Отчёт](verification/feedback-1.7.7/REPORT.md), [квитанция установки](verification/feedback-1.7.7/polymc-installation.json). Старые результаты ниже относятся к своим версиям.

## Награды и снаряжение 1.7.6 — 2026-10-12

Добавлены ровно 19 предметов: пять рун, четыре дара богатырей, пять материалов и пять крафтовых трофеев. Руны используют прежнюю наковальню, sockets и ItemState; амулеты занимают единственный существующий слот necklace, пояс — belt, перстень — ring, три нагрудника — vanilla chest. Первые награды за реальные старые победы доступны через кнопку в Книге сказаний. Для дара присядьте и взаимодействуйте со своим соответствующим богатырём. Прежние предметы призыва не расходуются и не заменены.

Состояние выдачи/inbox хранится в persistent data игрока вместе с Inventory при vanilla PlayerDataStorage.save; SavedData фиксирует UUID сохранения и аудит. Связка saveUUID/playerUUID/boss/version записывается отдельно от once-only даров. Полный инвентарь не получает частичный комплект и не порождает дроп. Повторные authenticated callbacks выдают только свои материалы и 15% шанс руны. Игроки без истории не получают награды по версии или владению предметом.

Серверные эффекты включают отражение ×1.5 / CD35с, один контрудар ×1.75 / окно4с / CD12с, пять рун, безопасный боковой шаг/телепорт, предупреждение оберега, трофейное снаряжение и разгон сабли. Разгон хранит максимум три момента убийств в скользящем окне600ticks. Physical slowness и бесконечные story-control эффекты не входят в dispel whitelist.

Точные результаты build, серверных запусков, ресурсов и установки: docs/IMPLEMENTATION_REPORT_1.7.6.md и docs/verification/rewards-1.7.6/resource-gates.json. Клиент автоматически не запускался; клиентская визуальная/звуковая и двухклиентная приёмка остаются NOT_RUN. Старые результаты ниже относятся к своим версиям.

## Калин-царь 1.7.5 — 2026-10-11

Реализованы Калин1800HP, собственный боевой конь с настоящим пассажиром, спешивание65%, временная ставка65×65, дружинники/воины с общим cap14, reusable печать и2 debug eggs. Ровно2 уже доступных и призванных богатыря выбираются прежними call items; `/slavicmyths kalin select <hero_a> <hero_b>` задаёт явную пару. Выбор/UUID фиксируются при старте; замена и отзыв блокируются до конца боя. Все6 пар проверяются через реальные server summon entry points. Уникальные награды1.7.6 не добавлены.

Native модели/позы,256px UV и32px item sprites,23 supplied procedural OGG/RU+EN подключены. Ни крупного worldgen, ни новой фракционной системы. Размещение/уборка по256 blocks/tick; сохранённые rollback cells не удаляют изменённый игроком блок. Сохранение активного боя после перезапуска переводится в cleanup, без слепого возобновления NPC. Победа/состав/число завершений сохранены для1.7.6.

1.7.5 установлена в PolyMC Slavic-Myths-1.21.1-Testing с backup1.7.4. Clean build,1 focused GameTest/101 assertions, 149000 native checks/48 profiles и50122 production/resource gates PASS. Реальные16 GameTest server starts (включая исправленные итерации),0 клиентов.7426 prior JAR entries byte-identical;2 synthetic switch classes имеют прежние opcodes, изменились debug line tables. История победы дополнительно прочитана из настоящего SavedData .dat на диске. Полный игровой client/real network MP/relog/world restart/MSPT NOT_RUN. [Отчёт](IMPLEMENTATION_REPORT_1.7.5.md), [матрица](verification/kalin-1.7.5/TEST_MATRIX.md).

## Финист/Картаус1.7.4 — 2026-10-11

Реализованы Финист170HP с соколом/кратким morph, Картаус650HP со знамёнами/дружиной/печатью оков, reusable перо/печать и debug egg, собственные модели/54 original OGG/RU+EN. Призыв новых и прежних героев/врагов зарегистрирован в Creative и проверен черезgive. Вне боя общий max2; персональное испытание требует только Финиста, без midfight replacement. Компактный проверенный участок существующего мира, без terrain changes/worldgen/крепости; уникальные награды1.7.6 не добавлены.

1.7.4 установлена в подтверждённый PolyMC с backup1.7.3. Clean build,1 focused GameTest/75 assertions,126918 native checks и36387 production/resource gates PASS. 6 GameTest server starts (включая исправленные итерации),0 клиентов. Полная51-кейсовая игровая приёмка/real MP/relog/disk restart/MSPT NOT_RUN. [Отчёт](IMPLEMENTATION_REPORT_1.7.4.md), [матрица](verification/finist-1.7.4/TEST_MATRIX.md).

## Алёша и Ским-зверь 1.7.3 — 2026-10-11

Реализованы Алёша175HP, Ским750HP, авторская долина65×65 с логовом,4 reusable call tools и3 boss eggs,48 исходных OGG, RU/EN, собственные модели/позы/64px icons. Долина размещается явной безопасной командой, natural worldgen не добавлен. Story locks/max2/last stand, сохранениеHP и единственная победа проверены серверными fixtures; уникальных наград1.7.6 нет.

Clean build и production/resource gates PASS;44 новых+84 регрессионных GameTests PASS, отдельные difficulty и реальные disk restart phases PASS.15 запусков GameTest server (включая исправленные итерации),0 клиентов. Мод1.7.3 установлен в Slavic-Myths-1.21.1-Testing,1.7.2 сохранена backup. Полная игровая приёмка/render/audio/real multiplayer/relog/MSPT остаётся NOT_RUN. [Отчёт](IMPLEMENTATION_REPORT_1.7.3.md), [матрица](verification/skim-1.7.3/TEST_MATRIX.md).

## Добрыня и Маринка 1.7.2 — 2026-10-11

Реализован пользовательский пакет1.7.2: Добрыня230HP/копьё3.6b/переход в тура/конечное лечение; Маринка600HP/покров/три одинаковые формы/сорока/якорь/огонь/след/rage; физическое подворье60×60 с подвалом17×23, реальные модели/38 action profiles,39 OGG/RU+EN, сюжетные блокировки и сохраняемая история без уникальных наград. Безопасный admin staging и контекстная печать; случайная генерация в существующих мирах не добавлена. Minecraft1.21.1/NeoForge21.1.255/Java21 и прежние IDs сохранены.

Clean build PASS(44s),36/36 новых dedicated tests,48/48 regression Ильи/Идолища,216393 native checks, два процесса disk restart и49930 production/resource/audio checks PASS.16 серверных запусков,0 клиентов. В подтверждённом PolyMC установлена финальная1.7.2; backup1.7.1 вне mods,206 остальных файлов последней установки сохранены побайтно. Клиентская приёмка, настоящий multiplayer/relog/длительный бой/MSPT NOT_RUN; матрица59:30 PASS по автоматизированным сценариям/29 NOT_RUN. Commit не создавался. [Отчёт](IMPLEMENTATION_REPORT_1.7.2.md), [матрица](verification/marinka-1.7.2/TEST_MATRIX.md).

## Богатыри 1.7.1 — 2026-10-11

Реализованы Илья/Идолище из пользовательского ZIP: серверный бой, сохранение, native модели/36 анимаций, HUD/призыв, 33 оригинальных OGG, RU/EN, явный двор без удаления мира. Minecraft1.21.1/NeoForge21.1.255/Java21 сохранены. Clean build PASS; 48/48 dedicated tests, 227016 offline checks и дисковый restart PASS. 10 серверных запусков, 0 клиентских. 1.7.1 установлена в подтверждённый PolyMC с backup1.6.9; 560 остальных файлов сохранены, квитанция в отчёте. Клиентская визуальная/звуковая приёмка, настоящий multiplayer/relog, пятиминутный бой и MSPT BLOCKED — не выдаются за пройденные. [Отчёт](verification/bogatyr-1.7.1/REPORT.md), [46 требований](verification/bogatyr-1.7.1/TEST_MATRIX.md).

## Ночница 1.6.4 — 2026-10-10

Реализован весь пакет Nochnitsa 1.6.2 поверх текущей 1.6.3: отдельная сущность `slavicmyths:nochnitsa`, 50 HP, когти raw6/24 ticks, естественный ночной поверхностный спавн, рассветный fade30 без наград, крик windup30/cooldown600 с отменой от урона/защиты. Крик привлекает существующих обычных врагов в сфере 32, не создаёт мобов; локальный cap48 и память240. Используется прежняя OminousAura с двумя экипированными рунами и её персональной провокацией.

Модель по двум утверждённым листам: 120 bones/132 cuboids, 22 пряди, 6 частей рваного платья, 4 составных когтя на руку, eye-only fullbright, atlas128. 12 событий/31 оригинальный OGG; яйцо в существующей категории «Мобы». Также исправлен конфликт EarthConfig со скоростями 1.6.3: однократно мигрируют только старые поставляемые default, собственные значения сохраняются.

Итоговый clean build PASS (35s, compileJava FROM-CACHE после проверки тех же исходников); Nochnitsa17677/Earth6186/Sivka7107/Firebird200020/Visual15, audio31 и resource23921 checks PASS. Повтор генератора 0 изменений/5201 файлов; 6240 старых записей JAR побайтно сохранены. 1.6.4 установлена в выбранную PolyMC 1.21.1-Testing; backup 1.6.3 вне mods, 472 остальных файла сохранены. **Minecraft запусков 0; игровой спавн/AI/руны/визуал/звук/dedicated server/MP/relog/MSPT NOT TESTED IN GAME**. [Отчёт и матрица](verification/nochnitsa-1.6.4/REPORT.md), [реализация](nochnitsa/IMPLEMENTATION.md). Commit не создавался.

## Исправления боя и визуала 1.6.3 — 2026-10-10

По новому игровому отзыву: в последнем сохранении подтверждены EASY + Resistance I, которые снижали старый raw удар дополнительно. Для физических атак Духа введены scaling=never damage types без обхода брони/защиты: кулак 8 HP после обычного алмаза, с Resistance I 6.4 HP. Добавлена диагностика фактического Post damage в `/sm earth_spirit status`. Ускорены кулак/залп/снаряды и cooldown первой фазы, походка следует реальному перемещению. Весь корпус получил vanilla stone/cobble/moss/andesite/deepslate atlas; noOcclusion печати исправляет скрытую траву; хвост Сивки больше не использует сетевой modulo gait. Яйцо Жар-птицы первое в «Мобы»; её принятый дизайн сохранён.

1.6.3 установлена в PolyMC 1.21.1-Testing с резервной копией 1.6.2; 464 остальных файла сохранены. Полный Earth resource regeneration: 0 изменений.

Clean build PASS (31s), Earth6186/Sivka7107/Firebird200020/Visual15 и resource/production gates PASS. **Запусков Minecraft: 0, новый игровой бой и визуал NOT TESTED**. [Отчёт и ручные проверки](verification/visual-1.6.3/REPORT.md). Мировые данные read-only, registry IDs и чужие изменения сохранены; commit не создавался.

## Исправления Сивки, Духа земли и Жар-птицы 1.6.2 — 2026-10-10

По пользовательской игровой проверке: разнесены coplanar facets Сивки/Духа, устранён double draw emissive у Духа, добавлены каменная фактура/vanilla block clusters и поэтапный косметический распад на 58 блоков разных материалов. Жар-птица переработана (182 bones/369 cuboids), лапы с пальцами/когтями анимированы; **яйцо добавлено по новому прямому указанию**. Дух реагирует на нападающего и приоритетно отвечает melee. Пользователь отменил старое arena-only ограничение: **руна и обычные награды за победу вне арены тоже**. Кулак теперь 8 HP после diamond без enchantments; тяжёлые атаки на той же основе, слуги неизменны.

Clean build PASS (49s); Earth6186/Sivka7107/Firebird200020/Visual8 offline PASS, 18206 production/resource checks. Actual static geometry: 0 coplanar face pairs; glow vertices submitted once. **Запусков Minecraft агентом:0; исправленная runtime-приёмка NOT TESTED.** [Отчёт и ручная проверка](verification/visual-1.6.2/REPORT.md). Сохранены пользовательские изменения/registry IDs; commit не создавался.

## Жар-птица 1.6.1 — 2026-10-10

Тестовый кандидат: новая `slavicmyths:firebird` без яйца по уточнению пользователя; Overworld nighttime SavedData event/+20…30 дней, единый бюджет 2/3 drops и 10/30/60, три ресурса без рецептов, 3000 HP/20 HP defensive armor bypass, native rig138 bones/267 cuboids/fullbright и 28 оригинальных OGG. Существующий `ognennoe_pero` и прежние механики сохранены. [Архитектура и ограничения](firebird/IMPLEMENTATION.md).

Clean build PASS (1m 2s), 200020 offline assertions, 23286 resource/production checks; **запусков Minecraft: 0**. Fidelity/звук/бой/полёт/MP/relog/MSPT и dedicated server startup NOT TESTED; приёмка OPEN. [Отчёт](verification/firebird-1.6.1/REPORT.md), [ручная проверка](verification/firebird-1.6.1/COMMANDS.md). Runtime не выдан за компиляцию.

## Сивка-Бурка 1.6.0 — 2026-10-10

По новому пакету реализован тестовый кандидат: отдельный белый native-конь152кубоида/51узел, артикулированные наземный/воздушный галоп,7этапов кормления, canonical SavedData/temporary72000online ticks/permanent bond, свисток, безопасный призыв, saddleless riding/разбег/полёт/уход,80HP/retreat, редкий native spawn,31новый OGG и RU/EN. По уточнению пользователя снижение на **X**, как у метлы/ступы; Space вверх, Shift спешивание. Кот Баюн/квесты/новые milestone не добавлены.

Clean build PASS (1m 35s), 7107offline /12878resources-production PASS; сохранены 1055классов/5290ресурсов1.5.6. Кандидат установлен в существующий PolyMC1.21.1 с backup; 357остальных файлов неизменны. **Запусков Minecraft:0**; gameplay/visual/audio/MP/relog/MSPT приёмка OPEN, все runtime-строки NOT TESTED. Статическое превью/codec не заменяют игру. [Отчёт](verification/sivka-1.6.0/REPORT.md), [краткая ручная проверка](verification/sivka-1.6.0/COMMANDS.md).

## Дух земли 1.5.6 — 2026-10-10

Реализован пакет Earth Spirit MODULAR V2 с уточнениями: оригинальный native-босс 800 HP/6.5 блока, три фазы, полный серверный набор атак, слуги 3×18 HP с cap 6, локальные аренные столбы/укрытия/ворота/преддверие, сон с сохранением HP и руна III на броню. По дополнительному запросу вместо яйца босса добавлен «Запирающий камень Духа земли»: поставленный вручную камень пробуждает тестового босса ударом/разрушением без поиска структуры и без редкой награды. ID блока `earth_heart_seal`; имя игровое, без исторической атрибуции. Настоящий encounter создаётся только завершением нового явного Depths job; старые миры не переоборудуются.

Финальный clean build — PASS (39 с), 6186 offline-проверок; 13579 ресурсных/production проверок — PASS. Все 30 OGG декодируются, ресурсы воспроизводимы; 1016 прежних классов и 4996 ресурсов сохранены относительно установленной 1.5.5. Кандидат 1.5.6 установлен в существующую PolyMC 1.21.1/NeoForge 21.1.255, предыдущие JAR сохранены вне mods; 356 остальных файлов неизменны. Старый PolyMC 1.16.5 не тронут. **Запусков Minecraft агентом: 0**, runtime/визуальная/боевая приёмка OPEN. [Отчёт](verification/earth-1.5.6/REPORT.md), [ручная проверка](verification/earth-1.5.6/COMMANDS.md). Никаких commit автоматически не сделано.

## Мобы подземелий 1.5.5 — 2026-10-10

По подтверждённому Mobs-пакету реализованы поведение существующей Чуди, Подземник с двумя вариантами 28/40 HP, Кладовик 200 HP, крупный подземник 300 HP, серверные способности, оригинальные модели/анимации, лут и Зловещая аура. Новые Б получают ограниченную сохраняемую очередь появления в пригодных существующих комнатах; старые заселяются только явной командой. Неподходящие арены пропускаются. Прежние торговля, NPC art, геометрия и Древние глубины сохранены.

Финальный clean build с offline checks — **PASS, 46 секунд**; 18 проверок правил, 2387 проверок native моделей, SavedData roundtrip и 13 104 ресурсных/production проверки PASS. 1025 production-классов. **1.5.5 установлена в MC 1.21.1-Testing**, предыдущий JAR сохранён; 353 остальных файла неизменны. Запусков Minecraft: **0**. Runtime mixin, бой/AI/торговля/падение и игровой перезаход ещё не проверены. Wizard/Ender Skeleton в установленном наборе отсутствуют; падение использует native pose с визуальным наклоном. [Отчёт и ограничения](verification/mobs-1.5.5/REPORT.md), [команды для ручной проверки](verification/mobs-1.5.5/COMMANDS.md).

## Промышленность и торговля Чуди 1.5.4 — 2026-10-09

По подтверждённому заданию из Industry/Trade-пакета реализованы болотное железо, сыродутная печь и очистительный горн с GUI/JEI/хопперами, флюс и очищенные металлы, четыре аксессуара Curios, руны добычи I/II и удачи I, оригинальные NPC Чуди с торговлей и стражей, шесть таблиц сундуков. Содержимое добавляется только новым комплексам А/Б в безопасных свободных местах; старые сохранённые комплексы не переоборудуются. Геометрия, сетка размещения, защита энд-портала и Древние глубины сохранены. Существующий бонус серебра +1.5 против явно уязвимых целей сохранён согласно исключению 17.6 пакета.

Финальный `clean build industryOfflineChecks` — PASS, 36 секунд; 5443 проверки чистых правил, 79 781 ресурсная/production проверка, 6000 выборок вероятностей JSON — PASS. 994 production-класса, 60 прежних классов планирования/генерации А/Б/В побайтно сохранены (два writer-класса А/Б получили только hook содержимого и NBT-маркеры). 1.5.4 установлена в PolyMC 1.21.1-Testing; прежний JAR сохранён вне mods, 352 остальных файла неизменны. Запусков Minecraft: **0**; GUI, AI, торговля, хопперы, реальный перезаход и освещение в игре требуют ручной проверки. [Отчёт](verification/industry-1.5.4/REPORT.md), [все команды и ручные проверки](verification/industry-1.5.4/COMMANDS.md).

## Древние глубины 1.5.3.1 — исправление плоского мира, 2026-10-09

Исправлен отказ на четырёхслойном flat: при FlatLevelSource общий путь команды и карты поднимает origin на 32 блока над максимальной отметкой входа. Нижняя граница bbox оказывается на один блок выше поверхности. Все помещения, спуски, арена и содержимое перемещаются вместе; отдельный blueprint и формат сохранения не меняются. Подземная проверка покрытия и выбор природного пролома в этом режиме не применяются: используется CLOSED. В обычном мире размещение остаётся подземным. End portal/frame, бедрок, чужие контейнеры и фактическое пересечение с другими сооружениями защищены. Ссылки на соседние native сооружения не запрещают свободный объём над flat, но пересечение их bbox продолжает запрещаться.

Отказы теперь показывают число кандидатов и причины (вода, склон, высота, граница, занятый объём, защищённые блоки). Истечение общего времени поиска сообщает текущий кандидат/этап и не выдаётся за отсутствие сухой земли. Сообщение о flat содержит реальные координаты поднятого входа.

Финальный clean build — PASS, 46 секунд. 2318 геометрических проверок (включая все блоки поднятого blueprint при четырёх высотах поверхности и четырёх ориентациях), 144 поля NBT, 12 049 ресурсных/production и 1063 статических проверки — PASS. В production JAR 952 класса; по сравнению с 1.5.3 изменились только DepthsPlan/DepthsService/DepthsService$Job. Все прежние переводы сохранены. 1.5.3.1 установлена в PolyMC 1.21.1-Testing с резервной копией; 296 остальных файлов экземпляра неизменны. Запусков Minecraft: 0; игровая приёмка открыта. [Отчёт](verification/ancient-depths-1.5.3.1/REPORT.md), [команды](verification/ancient-depths-1.5.3.1/COMMANDS.md).

## Древние глубины 1.5.3 — 2026-10-09

Реализовано отдельное подземелье В по приложенному пакету 1.5.3. Появляется только по команде или активации карты: зоны E0–E5 и переход K, вход на расстоянии 1500–2000 блоков, закрытый свод либо безопасный пролом к небу. Внутри два сундука, десять обычных мобов и два ванильных стража; финальная арена 29×29 оставлена без босса. Общая серверная операция ограничивает загрузку чанков и число записей за тик; SavedData сохраняет план и прогресс. Завершение выдаёт настоящую карту с RED_X. Естественная генерация В отсутствует; А/Б не изменены.

Финальный `clean build depthsOfflineChecks` — PASS (29 секунд). Геометрия: 2269 проверок / 8 вариантов; NBT: 144 проверки / 12 roundtrip. Ресурсы и production JAR: 12 049 проверок; отдельный статический аудит: 1063 проверки. Все 952 скомпилированных класса совпадают с JAR; 50 классов Чуди и 6 её ресурсов побайтно сохранены. Версия 1.5.3 установлена в PolyMC 1.21.1-Testing с резервной копией; 260 остальных файлов экземпляра неизменны. Клиентских и серверных запусков: 0. Свет, AI, карта в игре, реальный перезаход и MSPT требуют ручной проверки. [Отчёт](verification/ancient-depths-1.5.3/REPORT.md), [команды](verification/ancient-depths-1.5.3/COMMANDS.md).

## Свет/кладка Чуди1.5.2.2 — 2026-10-09

Clean build PASS;29 614 plain JVM assertions (11 221 новых decor policy) и12 020 resources/production checks PASS;940 compiled classes совпадают с JAR.14 классов сохранённых планов и6 классов генерации/portal protection побайтно сохранены. Установлена1.5.2.2 в PolyMC1.21.1-Testing с backup;259 остальных файлов/миров/настроек неизменны. Клиенты0/серверы0; visual/light-engine/relog NOT_RUN. SHA256 925b9eb3914a39bd1b33a39de59f3acb6b36e3724db899e104fe25f08d40e398.

По ручной обратной связи пользователя flat: тёплые вставки8×8 в открытый каменный пол и чаще corridor lanterns. Шесть палитр по назначению зон; tuft/cobble/cracked/mossy/deepslate, patch3×3×4; ruined имеет больше износа. Отделка final chunk writer заменяет только полные кубы; saved plans, проходы, размещение, IDs и portal protection сохранены. Старые chunks не перестраиваются. Клиенты0/серверы0; игровая визуальная приёмка открыта. [Отчёт/gates](verification/chud-1.5.2/lighting-masonry/REPORT.md), [команды](verification/chud-1.5.2/COMMANDS.md).

## Генерация Чуди1.5.2.1 — 2026-10-09

Clean build +18 393 plain JVM assertions PASS;12 014 resource/production checks PASS;937 compiled classes совпадают с JAR;18 saved plan/geometry classes побайтно сохранены. Установлена1.5.2.1 в PolyMC1.21.1-Testing, backup прежней версии;2428 остальных файлов/миров/настроек побайтно неизменны. SHA256 5e692a7e90ebbb4dc29c648f2b54fd6e383489df87595134eaff996e5c085022. Клиенты0/серверы0; runtime NOT_RUN, ручная приёмка открыта.

Убраны rock/fluid/route-cover и конфликты обычных построек. Общий конечный blueprint/roof sampling16:20 блоков под грунтом,5 под дном воды; actual bounds выше бедрока. Существующая сетка independent gap2298≥2000 сохранена, часть клеток выделена B. Новые operator команды generate сохраняют native starts/references; flat размещает над поверхностью. Seed stronghold exclusion, командный preflight PortalRoom/frames и защита portal blocks в rasterizer. Saved geometry/IDs сохранены, старые chunks не перестраиваются. Никаких игровых запусков; runtime/relog/MSPT приёмка открыта. [Отчёт и gates](verification/chud-1.5.2/simple-generation/REPORT.md), [команды](verification/chud-1.5.2/COMMANDS.md).

## Чуди1.5.2 — исправлено размещение, 2026-10-09

Новый placement проверяет прежнюю точку и до9 запасных в области, выбирает один А/Б root. Горы — предпочтение, biome tags разрешают Overworld;12% фильтр лесов удалён. Исправлена нижняя граница medium/great по фактической конструкции, сохранённые bbox/NBT/геометрия не менялись. Минимальный gap независимых комплексов2298≥2000, native owner совпадает с выбранной точкой. Курганы/поселения не правились.

Clean build PASS;18387 offline assertions, включая192 immutable plans на искусственном сухом грунте Y64;12010 resource/production checks;8 resource files воспроизводятся побайтно.935 compiled classes совпали с JAR;20 saved geometry classes совпадают с CA2. Это не проверка реального worldgen:0 клиентов/0 серверов, runtime/native density/chunk-order/MSPT NOT_RUN по запрету пользователя.

Development candidate SHA256 `8181fe0481c542314b88d64f68de2681a240541fbe7cceb093266a7846988336` установлен в PolyMC1.21.1-Testing;backup предыдущего6ae сохранён,2366 остальных файлов/миров/настроек неизменны. Полная приёмка1.5.2 открыта. [Отчёт размещения](verification/chud-1.5.2/placement-fix/REPORT.md).

## Чуди1.5.2 — исправление поиска, 2026-10-09

Поиск обоих подземелий перенесён с серверного потока в отдельный ограниченный worker. `/locate structure slavicmyths:chud_city|chud_abandoned` и обе `/sm … locate` теперь сообщают о старте, ходе и результате; добавлены `search`, `chud_search status/cancel/next`. Чтение готовых чанков использует `getChunkNow`, без ожидания незавершённых futures. Поиск выдаёт планы новых чанков, не генерирует мир и не гарантирует наличие в старых областях.

Clean build PASS;11 offline checks PASS;11992 resource/production checks PASS;926 классов JAR совпали с compilation output,21 класс сохранённых планов/геометрии совпал с прежним JAR. Исправленный development candidate установлен в PolyMC1.21.1-Testing: SHA256 `6ae23610d8390cc6e5792b8ad70048851ed5ea2bca8fea7618427c5edc75e524`. Прежний1.5.2 сохранён;2260 остальных файлов профиля побайтно неизменны, старый1.16.5 не затронут.

Minecraft в этой итерации:0 клиентов /0 серверов по прямому запрету пользователя. Новая runtime QA — `NOT_RUN`; прежние14/14 native tests относятся к прежнему JAR. Полная приёмка1.5.2 остаётся открытой, включая natural great и квоту attached. [Отчёт исправления](verification/chud-1.5.2/search-fix/REPORT.md), [команды](verification/chud-1.5.2/COMMANDS.md).

## Исправления боя после обратной связи — 2026-10-09

ТугаринII получил видимый эффект защиты50%; Соловей отгоняет игрока после5 секунд близости и воII фазе летает/пикирует; AI Проклятого Оборотня переработан, устранены простои и зона недоступной атаки. Clean build и21813 resource/production checks PASS; финальные41/41 native GameTests PASS (всего4 headless запуска, первый с исправленной ошибкой). В этой итерации были2 dev client запуска до возражения пользователя; автоматический запуск признан ошибкой. После возражения0 клиентов/0 серверов; дальнейшие игровые запуски только по отдельному явному разрешению. Проверенный JAR установлен в тестовый PolyMC без запуска, backup/179 других файлов сохранены. Ручная survival/multiplayer/relog/MSPT приёмка открыта. [Отчёт](verification/combat-feedback-2026-10-09/REPORT.md).

## Переработка трёх боссов — 2026-10-09

По отдельному ТЗ переработаны пропорции обеих форм Соловья, гигантский Тугарин/змеиная формаII с50% защитой и тройное вращение; Проклятый Оборотень получил Variant B геометрию/две стойки, повышенные скорость/урон/комбо и корректное видимое имя. Stable registry IDs/старый Волколак/квест сохранены. Clean build PASS;34/34 native GameTests,12396 model checks,21807 resource/production checks PASS.8 реальных запусков клиента (5 игровых сцен,2 меню,1 сбой общего build directory) /3 headless сервера; финальный клиент без ERROR/FATAL. Ручной осмотр игровых кадров выполнен; ручной survival/multiplayer/relog/MSPT NOT TESTED, Computer Use не получил кадр окна. Полная игровая приёмка остаётся открытой. JAR `build/libs/slavicmyths-1.5.1-boss-rework.jar`; параллельные изменения общей задачи сохранены, Коммит не выполнялся. По последующему запросу пользователя JAR установлен в тестовый PolyMC1.21.1: backup, один активный mod JAR, SHA-256 совпадает, миры/настройки сохранены; запуск после установки0. Квитанция: `verification/boss-rework-2026-10-09/polymc-installation.json`. [Изменения, доказательства и короткая проверка](verification/boss-rework-2026-10-09/REPORT.md).

## 1.5.2 — тестовая сборка установлена; приёмка открыта (2026-10-09)

Generation-v2: attached-small и independent-medium/great, native codec/piece, общий candidate grid и optional NBT-пристройка новых городов. Final attempt37:14/14 native GameTests PASS,36 планов/12 seed/все повороты и пять полных размещений; три сохранённых профиля великого колодца76/60/44. Clean build27с и11982 production/resource checks PASS; все921 класс совпадают с финальной runtime-компиляцией. JAR build/libs/slavicmyths-1.5.2.jar установлен по последующему запросу пользователя в тестовый PolyMC1.21.1 как development candidate; backup прежнего combat-feedback JAR,214 прочих файлов/миров/настроек сохранены.12 natural seed/480 candidates:5 medium,2 A/1 attached,0 great; квота четырёх attached/great ОТКРЫТА. Полная приёмка1.5.2 НЕ завершена.34 headless loader запуска/0клиентов в1.5.2; gameplay/relog/MSPT NOT TESTED. [Отчёт](verification/chud-1.5.2/REPORT.md), [команды](verification/chud-1.5.2/COMMANDS.md), [продолжение](verification/chud-1.5.2/CONTINUE.md).

## 1.5.1 — техническая поставка; далее 1.5.2 (2026-10-09)

Город Чуди реализован:3/4/5залов,шесть зон,8/12/18домов,2/3террасы,физические маршруты и закрытые ворота. Attempt28:11/11 native tests PASS;640 естественных кандидатов/10seed дали один старт. Attempt29:9/9 PASS с тремя глубинами входа. Clean build и11924 resource/production checks PASS; проверенная1.5.1 установлена в PolyMC1.21.1,backup,432 остальных файлов сохранены.28 headless loader запусков/0клиентов. Игровая визуальная приёмка/relog/MSPT NOT TESTED. По явному поручению пользователя следующим реализуется1.5.2. [Факты и ограничения](verification/chud-1.5.1/REPORT.md).

## 1.4.3 — Проклятый Волколак, 2026-10-09

Продолжение по запросу пользователя завершено: прежний `volkolak` сохранён, отдельный `cursed_volkolak`/яйцо, Variant B/две позы, 400HP/атаки/хватка/Дурная кровь, четвёртое поручение/могила/личный череп/атомарная награда Финиста. 28 OGG/23 события с лицензиями. Clean build (isolated output), 156 финальных native GameTests, 1660 model checks и 15945 production/resource checks PASS. 16 headless loader запусков /0 клиентов, промежуточные ошибки исправлены. Установлена проверенная 1.4.3 в тестовый PolyMC, один активный JAR, backup; миры/настройки/старый 1.16.5 сохранены. Клиентская/survival/multiplayer/relog/restart/Xaero приёмка и измеренный MSPT открыты. [Отчёт](verification/volkolak_1.4.3/REPORT.md), [реализация](IMPLEMENTATION_MAP_1_4_3.md).

## Поручения Серого Волка и Финист — checkpoint 1.4.2, 2026-10-08

Три поручения, серверная выдача, собственное окно, Кольцо Стаи и рабочие Лук/Колчан Финиста реализованы. Общая версия 1.4.2.1 с параллельными исправлениями сохранена. Clean build, 25 story +20 wolf +63 combat +23 unique GameTests,1737 native model и22391 resources/JAR checks PASS.9 headless/0 клиентов; ручная приёмка открыта. Установка checkpoint отложена: пользователь добавил ТЗ1.4.3, работа продолжается поверх него. [Отчёт](verification/grey_wolf_1.4.2/REPORT.md).

## Исправления AI Волка и управления полётом — 2026-10-08

В общей версии 1.4.2.1 исправлены прыжки под кроной/в ствол, остаточное управление движением и самоурон Волка; снижение транспорта по умолчанию X, подсказка показывает сохранённые клавиши, Shift — высадка. Прежние registry IDs, art, урон атак и дальности сохранены. Clean build PASS; финальные 20/20 native GameTests и 10967 production/resource checks PASS; 6 headless запусков / 0 клиентов. Проверенная сборка установлена в тестовый PolyMC с backup и SHA-256, миры/настройки сохранены. Параллельные изменения версии сохранены; roadmap этой задачей не расширен. Клиентская приёмка конкретного дерева и клавиш открыта. [Отчёт и краткая ручная проверка](verification/wolf-flight-hotfix/REPORT.md), [установка](verification/wolf-flight-hotfix/polymc-installation.json).

## Проверка актуальности PolyMC — 2026-10-08

По запросу пользователя подтверждена установленная 1.4.1 в `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Повторный `gradlew.bat clean build` PASS, 22672 resource/production checks PASS; SHA-256 единственного активного JAR совпадает с новой сборкой. Замена не требуется, файлы экземпляра и миры не изменены; старый 1.16.5 сохранён. Код, версия и архитектура не менялись; roadmap не расширен. Реальных запусков в этой проверке: 0 клиентов / 0 серверов; игровая приёмка остаётся открытой. [Квитанция](verification/polymc-1.4.1-recheck.json).

## Серый Волк — 1.4.1 (2026-10-08)

Реализовано ТЗ пользовательского архива и яйцо `grey_wolf_spawn_egg`: самостоятельная модель30 частей/87 кубов,512px atlas, личное согласие и испытание500→50HP, пять атак, синхронный реальный вой/ограниченная стая, общий bleeding cap, постоянный NPC и персональная запись existing Journal/Xaero. Прежние Сапоги ветра сохранены. Будущие задания/награды1.4.2 не реализованы.

Серверный state machine + стандартные goals/entity data; safe loaded-only encounter, bounded candidate checks, без force loading. Seed probability.10 и квота≤40% подтверждённых natural Tugarin anchors (до3 таких якорей wolf отсутствует). Native pounce5.386/dash3.243blocks; wolf/bear визуальные размеры≈×1.99. Глобального обхода игроков/мира каждый тик нет. Истечение WOLF_CUT отложенно возвращает оставшийся слабый bleed через server TickTask; milk не восстанавливает удалённый эффект.

Итог: clean build и production/resource gates PASS;15 wolf +63 combat +23 unique native GameTests PASS,2116 wolf model +20491 rare +355 rune +147 armor +9176 existing models offline checks PASS.13 headless запусков /0 клиентов в этой итерации; промежуточные ошибки исправлены. Клиентская визуальная/звуковая приёмка, реальный relog/Xaero, natural частота и MSPT **не проверены**. Production установлен в тестовый PolyMC после gates с резервной копией прежнего JAR; игровые миры/1.16.5 сохранены. Commit не выполнялся.

[Реализация и ограничения](verification/grey_wolf_1.4.1/IMPLEMENTATION.md), [ручная приёмка](verification/grey_wolf_1.4.1/MANUAL_QA.md), [resources/JAR](verification/grey_wolf_1.4.1/resource_gates.json), [установка](verification/grey_wolf_1.4.1/polymc-installation.json), [звуки](verification/grey_wolf_1.4.1/SOUND_SOURCES.md). Ниже сохранена история.

## Разгон Сапог ветра — 1.3.4.5.5 (2026-10-08)

По запросу пользователя добавлен временный серверный заряд: 8 с спринта с прыжками повышают высоту от **4.154 до 12.461 блока**. Штатная физика и syncable JUMP_STRENGTH; дальность до ровного пола растёт от **7.133 до 11.492 блока**, максимальная дуга 36 тиков. Импульсы рассчитаны по высоте ×3; горизонтальная скорость и бонус отрыва не увеличены. Спад после остановки 1.6 с, стена сбрасывает заряд, lifecycle/equipment/flight/fluid/ladder/vehicle/teleport очищают состояние. Добавленная высота конкретного прыжка вычитается из fall distance однократно с ограничением по достигнутому пику; базовое снижение урона 40% сохранено. Registry IDs/рецепты/ресурсы внешнего вида сохранены; RU/EN и генератор актуальны.

`WindBootsMomentum`: O(1) PlayerTick.Post только владельца, без world/player scans, custom movement packets или сохранённого заряда. Атрибут меняется только по ступеням; микропроба 1000 синтетических образцов/handler 4.0043 мс, не MSPT. **1.3.4.5.4 → 1.3.4.5.5**; clean build PASS, 23/23 итоговых native GameTests, 19336 resource/JAR checks. **3 headless запуска / 0 клиентов**; один промежуточный сбой fixture исправлен. Production установлен в PolyMC `Slavic-Myths-1.21.1-Testing`, прежний JAR сохранён вне mods, SHA-256 проверен, 291 прочий файл экземпляра сохранён побайтно. Клиентская приёмка открыта; commit не выполнялся.

[Физика, поведение и проверки](WIND_BOOTS_MOMENTUM_1.3.4.5.5.md), [gates](verification/wind-charge-1.3.4.5.5.json), [установка](verification/polymc-1.3.4.5.5-wind-charge-installation.json). Ниже сохранена история.

## Сапоги ветра: дуга выше четырёх блоков — 1.3.4.5.4 (2026-10-08)

По запросу пользователя вертикальный импульс Сапог ветра изменён на 0.82 блока/тик (`JUMP_STRENGTH +95.238095%`). Штатная физика Minecraft даёт пик **4.154 блока**, дальность **7.133 блока с разбега и удержанием W** до ровного пола и 21 тик полёта. Native ServerPlayer.travel подтвердил расчёт и посадку на уступ высотой 4 блока. Скорость +40%, momentum +10%, снижение fall damage 40%, руны и registry IDs сохранены; RU/EN и генератор ресурсов обновлены.

Версия **1.3.4.5.3 → 1.3.4.5.4**. `gradlew.bat clean build` PASS; 19/19 native GameTests PASS; 19336 resource/production JAR checks PASS. **2 headless запуска сервера / 0 клиентов** для этой правки. Параллельные изменения боссов сохранены и отдельно записаны в audit. Production JAR установлен в PolyMC `Slavic-Myths-1.21.1-Testing`; предыдущая 1.3.4.5.3 сохранена вне mods, SHA-256 проверен, 273 прочих файла экземпляра сохранены побайтно. Клиент не запускался, ручная игровая приёмка открыта; commit не выполнялся.

[Расчёт и ограничения](WIND_BOOTS_JUMP_1.3.4.5.4.md), [gates](verification/wind-boots-1.3.4.5.4.json), [установка](verification/polymc-1.3.4.5.4-wind-boots-installation.json). Ниже сохранена история предыдущих итераций.

## Установка дополнения уникального снаряжения в PolyMC — 2026-10-08

По прямому запросу пользователя проверенный production `slavicmyths-1.3.4.5.3.jar` установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.3.4.5.2 сохранена вне mods; активен один JAR, SHA-256 совпал с unique-equipment gates. 246 прочих файлов экземпляра, включая миры и настройки, сохранены побайтно. Версия исходников не менялась. Клиент/сервер при установке не запускались; старый экземпляр 1.16.5 не затронут. [Квитанция](verification/polymc-1.3.4.5.3-unique-equipment-installation.json). Ручная клиентская приёмка открыта; ниже предыдущие записи.

## Уникальное снаряжение — дополнение 1.3.4.5.3 без изменения версии (2026-10-08)

Реализованы Сапоги ветра, Нагрудник богатыря, Шлем ясного взора, Поножи неукротимости, Щит-оберег и Закалённое железо. Пять upgrades существующего верстака сохраняют компоненты и долю износа; ёмкость рун 3. База сапог — Скороходы; чёрное перо из стана одобрено пользователем. RU/EN, нативные sprites/worn models, Creative/Search и optional JEI integration готовы. Версия **1.3.4.5.3 → 1.3.4.5.3**.

Clean build PASS; 17/17 итоговых native GameTests, 147 armor +355 rune offline, 19336 resource/JAR checks. 8 headless servers/0 clients; новый процесс проверил 5 stacks/2 КД в player .dat. Это не клиентский relog/визуальная приёмка/MSPT. Production `build/libs/slavicmyths-1.3.4.5.3.jar`; PolyMC/клиент не запускались, установка/commit/push не выполнялись. Внешние параллельные изменения боссов сохранены и отделены в machine audit. **REQUIRES_MANUAL_CLIENT_CHECK**: [отчёт](UNIQUE_EQUIPMENT_COMPLETION.md), [ручная приёмка](MANUAL_VISUAL_CHECK.md), [gates](verification/unique-equipment.json). Ниже предыдущий статус/история.

## Boss reimplementation — завершено, интегрировано в 1.3.4.5.5 (2026-10-08)

Реализованы четыре самостоятельные модели/текстуры, две фазы и атаки обоих боссов, village targeting, яйцо Соловья, волна кинжала и 39 звуковых событий. Последние исправления челюсти Тугарина и спуска Соловья с платформы включены.

В изолированной сборке 1.3.4.5.3: clean build PASS, 40/40 boss + 63/63 combat GameTests, 9176 native model checks, 20491 rune checks и resource/JAR gates PASS. 16 реальных headless запусков / 0 клиентов. Установленная параллельной задачей 1.3.4.5.5 содержит побайтно те же 37 файлов кода/ресурсов боссов и 39 звуковых событий; production JAR совпадает с build/libs. Новая версия не откатывалась, файлы снаряжения не изменялись этой задачей. Подробности: [отчёт](IMPLEMENTATION_REPORT.md), [gates](verification/boss-reimplementation/gates.json), [интеграция](verification/boss-reimplementation/integration-1.3.4.5.5.json). Визуальная/звуковая клиентская приёмка остаётся ручной. Commit/push не выполнялись.


## Общий Boss Rework — 1.3.4.5.2, этап1 (2026-10-08)

Первый атомарный этап нового архива Тугарин+Соловей: кинжал wind range6.5/base impulse4/visible arcs; прежний melee10/SPEAR/CD7с сохранён.39stable boss SoundEvent keys/aliases; оригинальные OGG неизменны. Полный rework **не завершён**: две фазы/новые модели/способностиСоловья и новыеdamage/combo/boss-heldbladesТугарина остаются в [STATUS_HANDOFF](../STATUS_HANDOFF.md). Механики1.3.4.5.1 сохранены.

Clean build PASS,63/63GameTests,20491pure rune,13503resource gates;1server/0clients.1.3.4.5.2 установлена в существующийtestPolyMCбеззапуска;765прочихфайловсохранены. REQUIRES_MANUAL_CLIENT_CHECK. [Sound notes](SOUND_PRODUCTION_NOTES.md), [manual](MANUAL_CHECK_BOSSES.md), [gates](verification/boss-stage1-1.3.4.5.2.json). Commit/push не выполнены. Ниже история.

## Тугарин Змей — 1.3.4.5.1 (2026-10-08)

Две формы и единичная трансформация при50% HP: отдельный серверный scheduler, finite DR85% на3сек, устойчивая aggro, четыре phase-two abilities, отдельная геометрия Змея с крыльями и разные звуковые профили.350HP без heal, raw melee6/6.6, phase-two speed+23%; hitbox1.3×2.35. Прежние трофеи сохранены, pair drop12%, natural region probability25% (один multiplier, применён и к прежним anchors). Дополнены alternating pair slash/sheath sound; offhand escrow/native bleed сохранены.

Clean build PASS; итог21/21Tugarin +61/61combat regression,20491pure rune,9772native model bake,11357resource gates. Реальных запусков5servers/0clients; первый11/14, последующие PASS. **REQUIRES_MANUAL_CLIENT_CHECK**: модели/анимации/звук/боевые окна не проверены клиентом.1.3.4.5.1 установлена в существующий тестовый PolyMC1.21.1 без запуска;765 остальных файлов сохранены. Commit/push не сделаны; пользовательские и прежние незакоммиченные изменения сохранены. [Отчёт](TUGARIN_REWORK_1.3.4.5.1.md), [ручная приёмка](MANUAL_CHECK_TUGARIN.md), [gates](verification/tugarin-1.3.4.5.1.json), [установка](verification/polymc-1.3.4.5.1-installation.json). Старые разделы ниже — история.

## Актуальная версия 1.3.4.4 — 2026-10-08

Реализованы CombatTargeting, исправления Flight/Floating/Likho/Death/Net, knife Loyalty и звук, утверждённый trophy damage10/12/12/10/10 (Pool Spear9), один stack парных клинков Тугарина с безопасным offhand escrow и render-only вторым клинком. Vanilla/material weapons и registry IDs сохранены. Clean build PASS,61/61GameTests,20491pure assertions,6591resource checks. Реальных запусков4servers/0clients; ручной renderer/keybind/sound check не выполнен.1.3.4.4 установлена в 1.21.1 test PolyMC,765прочих файлов сохранены. Commit/push не выполнены. [Полный отчёт](COMBAT_RUNE_FIXES_1.3.4.4.md); [установка](verification/polymc-1.3.4.4-installation.json). Более ранние разделы ниже — история.

## Установка 1.3.4.3 в PolyMC и публикация исходников (2026-10-08)

По прямому запросу пользователя 1.3.4.3 установлена в Slavic-Myths-1.21.1-Testing (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.3.3 сохранена вне mods, активен один JAR; SHA-256 совпал с production gates, 592 остальных файла экземпляра сохранены. Клиент/сервер не запускались. [Квитанция](verification/polymc-1.3.4.3-installation.json). Исходники, ресурсы и отчёты текущей версии подготовлены для main на origin; записи ниже об отложенной установке исторические.

## Редкие руны — 1.3.4.3 (2026-10-08)

Реализованы шесть rare runes, четыре именных fragment, четыре восстановления через существующую Rune Anvil и два stage-3 обмена Яги. Источники: Соловей (3 гарантированных flight fragments, одноразовый бой на стан), Тугарин/Лихо/курганный воевода (1 гарантированный fragment); 25% extra, 4% full rune. Старая rune_protection сохранена, новая — rune_rare_protection; прежние IDs/loot pools/runes I–II/Classes Screen и HUD layout сохранены.

RareRuneRules/Runtime/Network: server authority, persisted player cooldown по overworld clock, одна общая R-кнопка (Shift — выбор), временный полёт и safe cleanup, execute immunity/tag и exact health-cost marker, Berserker/Sacrifice buffs. RuneVisual — одна noSave/non-AI entity с tracked item/count/phase; own client renderer, shields/crack/fade и лёгкий configurable edge overlay. Архитектура/параметры/файлы: [отчёт](RARE_RUNES_1.3.4.3.md).

Clean production build PASS. Offline rare20491, ordinary355, foundation75064, armor147; production resource/preservation audit PASS, сохранены 4931 resources. Итоговый JAR build/libs/slavicmyths-1.3.4.3.jar; [машинная проверка](verification/rare-runes-1.3.4.3.json). Реальных клиентов/серверов 0/0 по запрету пользователя: игровой lifecycle, flight/render/PvP/JEI/dedicated smoke остаются непроверенными. MSPT не измерен. PolyMC остаётся 1.3.3; полная переделка Classes GUI по-прежнему ждёт утверждения. Future frost/clone/III upgrades/new bosses не добавлены; commit не создан. Записи ниже исторические.

## Rune System 2.0 — 1.3.4.2 (2026-10-08)

Восемь рун I–II реализованы: existing IDs heat/wind переиспользованы, шесть новых; RuneBalance централизует diminishing returns/caps, RuneRuntime применяет серверные эффекты и transient modifiers по событиям. Старые особенности heat/wind сохраняет persistent/network legacy_rune_specials; existing RUNES и registry IDs сохранены. RuneCraftRecipe проверяет base component, GUI/JEI используют counted inputs. Кровотечение: 0,5 HP, rune max2/native max3, совместный max3 по ответу пользователя. Силуэты физических камней обновлены.

Clean build PASS; offline 355 + armor147 + foundation75064; resource gates22838, preserved4898. Production build/libs/slavicmyths-1.3.4.2.jar. Реальных запусков клиент/сервер 0/0: игровая приёмка открыта, запрет пользователя сохраняется. Classes GUI ждёт утверждения, PolyMC остаётся 1.3.3. Rune III и следующие milestones не реализованы. [Полный отчёт](RUNES_REWORK_1.3.4.2.md).

## 1.3.4.1 — Rune Crafting Foundation (2026-10-08)

Реализована основа рунного дела на существующей наковальне: зубило 192, шесть пылей, осколки/души трёх уровней, пустые руны I–III с persistent/network RuneBase. 15 counted JSON-рецептов наковальни + крафт зубила; компактный GUI 202×214, серверная оплата выхода/прочности, isolated JEI. UPYR новых курганов +1 в конечном roster, старые экземпляры сохраняют прежний состав; исходные упыри дают 1 осколок с шансом 35%, призванные исключены. Душа: воевода/волхв 50%, князь 100%. Старые rune IDs/effects/баланс/слоты брони/Classes/HUD сохранены.

Clean production build PASS; 75 064 codec/planning checks (3000 курганов), 147 прежних armor checks, 13 862 resource/JAR checks PASS. Это только офлайн: 0 клиентов, 0 серверов; runtime GUI/craft/JEI/дроп/жизненный цикл/TPS не проверены. Production `build/libs/slavicmyths-1.3.4.1.jar`. PolyMC остаётся на 1.3.3; Classes UI-макеты по-прежнему ожидают утверждения. [Числа, файлы и ограничения](RUNE_FOUNDATION_1.3.4.1.md), [gates](verification/rune-foundation-1.3.4.1.json). Commit не выполнялся. Записи ниже исторические.

## Classes 2.0 — исправление, этап макетов на утверждение (2026-10-08)

По новому пользовательскому ТЗ выполнена диагностика затронутого UI и подготовлены пять PNG-макетов, дополнительный вид направлений без модалки и 40 resolution/state snapshots. 1344 проверки предлагаемой геометрии/bitmap glyphs PASS; это не проверки рабочей реализации. Проверены реальные prerequisites всех 34 навыков; KeyMapping snapshot — ЛКМ/H/J из options.txt, файл не изменён. Макеты и причины дефектов: [проект на утверждение](CLASSES_UI_REDESIGN_APPROVAL.md).

Ожидается утверждение согласно §18.2: «До моего утверждения не приступать к полной переделке GUI». Production GUI, ресурсы/механики, JAR и PolyMC на этом этапе не менялись; установленная версия остаётся 1.3.3. Клиент/сервер не запускались, сборка исправления ещё не выполнялась. Предлагаемый HUD18/right8/bottom8 требует утверждения: найденный в backup1.3.1 старый HUD24/Y=H−74 расходится с новыми координатами ТЗ. История Git для untracked ClassClient отсутствует. Будущая реализация, тесты её ввода/камеры и установка ещё не выполнены.

## Установка 1.3.3 в PolyMC — 2026-10-08

По прямому запросу пользователя production 1.3.3 установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Включены парные мечи Тугарина и остальное редкое оружие 1.3.2.2, Classes UI и рунные слоты брони. Предыдущая 1.3.2.3 сохранена вне mods, активен один JAR, SHA-256 установленной копии совпал; 542 остальных файла экземпляра (включая миры/настройки) побайтно сохранены. Клиент и сервер не запускались. [Квитанция](verification/polymc-1.3.3-installation.json). Записи ниже об отложенной установке исторические.

## 1.3.3 — Рунные слоты брони (2026-10-08)

Существующая Runes.capacity расширена на player ArmorItem: leather/семимильные сапоги 0; chainmail/gold/iron/turtle 1; diamond/netherite 2; perunite 3; gambeson/plate 1. Одежда без ArmorItem, аксессуары и animal BODY исключены. Armor max строго ограничен материалом; grandfathering прежних оружейных ячеек сохранён. Наковальня использует прежние открытие ячеек/установку/удаление, общий лимит и проверку дубликатов. Tooltip/GUI показывают руны, открытые ячейки и отсутствие эффектов на броне. Боевые характеристики и эффекты не добавлены.

`gradlew.bat clean build` PASS; 147 офлайн-проверок capacity/admission/RuneState JSON/NBT/packet, 10 542 проверки ресурсов/JAR PASS. По сравнению с production 1.3.2.3 изменены только пять классов Runes/RuneDefinition/RpgEvents/RpgScreen/RpgClient; 4 862 прежних ресурса побайтно сохранены, RU/EN добавлены только две строки. Новых registry ID нет. ItemState/RuneRepair, материалы/предметы/рецепты/текстуры/классы/HUD не менялись.

По прямому запрету пользователя в этой итерации 0 клиентов и 0 серверов. Полный native ItemStack harness остановился на offline bootstrap: FeatureFlagLoader требует LoadingModList; обходов в production нет. JSON/NBT roundtrip компонента не считается игровым relog. Смерть/respawn/relog/restart/dimension/drop-pickup, реальная починка/отделка и GUI не проверены в игре. Копирование компонентов vanilla-процессами сверено по установленным зависимостям, без заявления runtime PASS.

Production `build/libs/slavicmyths-1.3.3.jar`; в PolyMC в рамках этого задания не устанавливался, там остаётся 1.3.2.3. Пользовательские изменения/миры сохранены; commit не выполнялся. [Таблица, файлы и приёмка](ARMOR_RUNE_SLOTS_1.3.3.md), [gates](verification/armor-runes-1.3.3.json). Предыдущие записи ниже исторические.

## 1.3.2.3 — Classes 2.0: интерфейс (2026-10-08)

Переработаны существующие классы: дерево по фактическим prerequisites, крупный корень, состояния узлов и ранги, правая карточка с текущими/следующими параметрами и точными требованиями, компактные 3 active + 1 passive. Выбор класса: временный 3D-preview собственного игрока, три карточки с ролью, подробности и подтверждение. Нативные 32px иконки, 64px гербы, деревянные GUI sprites; blur отключён. Управление использует прежние Minecraft KeyMapping и сохраняет options.txt. HUD справа снизу; единственный действующий переключатель — Mods → Slavic Myths → настройки, клиентский config по умолчанию включён. Creative вручную изучает навыки своего класса без требований и расхода очков; пределы рангов сохранены, Survival проверяет обычные требования.

`gradlew.bat clean build` PASS (14 предупреждений deprecated/unchecked), 3 710 офлайн-проверок правил/геометрии и 530 проверок ресурсов/JAR PASS. Проверены границы для 1920×1080/2560×1440, GUI scale 2/3/4; это не проверка игровой отрисовки. В этой итерации 0 запусков клиента и 0 сервера, реальные relog/смерть/механики и FPS/MSPT не проверялись. Существующие сохранение Class2, lifecycle events, баланс, названия, ветви и registry IDs сохранены.

Production `build/libs/slavicmyths-1.3.2.3.jar` установлен в существующий PolyMC `Slavic-Myths-1.21.1-Testing`. Предыдущая 1.3.2.1 сохранена вне mods, установлен один JAR, SHA-256 совпал; 539 остальных файлов экземпляра, включая миры и настройки, побайтно сохранены. Старый экземпляр 1.16.5 не затронут. Commit/push не выполнялись. [Детали и ручная приёмка](CLASSES_UI_1.3.2.3.md), [gates](verification/classes-ui-1.3.2.3.json), [установка](verification/polymc-1.3.2.3-installation.json). Более старые записи об установленной версии ниже исторические.

## 1.3.2.2 — Редкое оружие (2026-10-08)

Реализованы парные мечи Тугарина, посох Лихо, кистень атамана и копьё победителя: разные силуэты, серверные механики, максимальная ёмкость 3 руны, 4 рецепта оружейного верстака, RU/EN. Существующие дропы Тугарина/Лихо и курганные трофеи переиспользованы; добавлен только гарантированный знак атамана дополнительным pool. Два меча крафтятся одновременно и сохраняют данные каждого донора отдельно; второй остаётся в ячейке верстака. Броня/новые руны/боссы не добавлены.

`gradlew.bat clean build` PASS; 15,935 ресурсных/JAR-проверок PASS, 4,618 прежних ресурсов побайтно сохранены, остальные изменения старых ресурсов только добавочные. Финальные headless suites: новые 16/16 + прежнее оружие 16/16. 11 реальных серверных запусков, ноль клиентов; промежуточные failures сохранены в отчёте. Проверены нативные крафт/ремонт/подбор, смерть, два dimension transitions, реальные boss drops и player .dat новым серверным процессом (4 оружия + 2 КД). Это не реальный клиентский relog; внешняя ручная приёмка остаётся открытой, FPS/MSPT не измерялись.

[Параметры, рецепты, файлы и краткая ручная приёмка](RARE_WEAPONS_1.3.2.2.md), [acceptance](verification/rare-weapons-1.3.2.2-acceptance.json), [ресурсы/JAR](verification/rare-weapons-1.3.2.2-resources.json). Production: `build/libs/slavicmyths-1.3.2.2.jar`. PolyMC остаётся на установленной 1.3.2.1; клиент/установка не запускались. Пользовательские изменения и игровые миры сохранены, commit/push не выполнялись.

## Установка 1.3.2.1 в PolyMC — 2026-10-08

По запросу пользователя проверенный JAR установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.3.1 сохранена вне mods; SHA-256 установленной копии совпал. Активен один JAR мода, 439 сопутствующих файлов (включая миры и настройки) сохранены побайтно. Клиент не запускался. [Квитанция установки](verification/polymc-1.3.2.1-installation.json). Записи об отложенной установке ниже исторические.

## 1.3.2.1 — Оружие 2.0 (2026-10-08)

Реализованы 30 форм оружия: копья, сулицы, кистени и метательные ножи семи материалов, короткий и тяжёлый луки. 53 новых предмета включают детали; три прежних оружейных ID сохранены. 36 рецептов оружейного верстака и 23 рецепта деталей; просмотр рецептов работает без JEI. Разные силуэты, ru_ru/en_us, серверные броски/подбор, сохранение данных и совместимые ремонты. Централизованы ёмкость рун и категории существующего оружия; ранее открытые слоты сохранены.

Финальный `gradlew.bat clean build` PASS; 15 067 проверок ресурсов/JAR PASS, 2 566 прежних ресурсов сохранены побайтно. Итоговые headless suites: оружие 16/16, классы 15/15. 11 реальных серверных запусков, ноль клиентов. 30 оружейных стеков проверены после загрузки player .dat новым процессом; проверены смерть и два нативных перехода между измерениями. 512 столкновений ножа: 117 потерь при заданной вероятности 25%.

[Реализация, характеристики и рецепты](WEAPONS_1.3.2.1.md), [отчёт и ограничения](verification/weapons-1.3.2.1-acceptance.json), [ручная приёмка](MANUAL_QA_WEAPONS_1.3.2.1.md). Клиентский вид, GUI, бой нескольких игроков и реальный выход/перезаход остаются ручными; FPS/MSPT не измерялись. PolyMC остаётся на проверенной 1.3.1; установка новой версии, commit и push не выполнялись.

Пользовательские изменения и существующие миры сохранены. Production JAR: `build/libs/slavicmyths-1.3.2.1.jar`.

## 1.3.1 — Классы 2.0 (2026-10-08)

Проверенный JAR установлен в существующий PolyMC `Slavic-Myths-1.21.1-Testing`; 1.2.5 сохранена вне mods, 52 сопутствующих файла/настройки не изменены. Миры не затронуты, клиент не запускался. [Квитанция установки](verification/polymc-1.3.1-installation.json).

Реализованы дружинник, ведун и разбойник, 18 ветвей, 28 основных навыков и шесть эволюций. Отдельные class XP/очки, серверные требования, три активных слота G/H/J и один пассивный, триггеры, экран K/камня пути, HUD и 34 разные pixel icons с ru_ru/en_us. Миграция прежних путей сохраняет данные/КД; нативные смерть, /kill и загрузка player .dat новым процессом проверены. API/debug `/smclass`; массовые источники class XP не добавлены.

Clean build PASS; ресурсы/воспроизводимость/JAR PASS. Итоговые серверные suites 15 + 53 + 8 + 6 = 82/82; 25 реальных headless запусков этой итерации, ноль клиентов. Промежуточные ошибки сохранены в [машинном отчёте](verification/classes-1.3.1-acceptance.json). Пользовательские изменения городских построек сохранены. Commit/push не выполнялись; игровые миры не перезаписывались.

[Реализация и ограничения](CLASSES_1.3.1.md). Ручная приёмка GUI scales/F1/keybindings, двух клиентов, боя и реального выхода в меню/перезахода остаётся открытой; FPS/MSPT не измерялись. [Краткий список](MANUAL_QA_CLASSES_1.3.1.md).

## 1.3.1 — Исправления башен, интерьеров, света и окон (2026-10-08)

Выполнены шесть замечаний: непрерывные деревянные стойки / сплошной борт башни, все стыки стены без щелей, реальные предметы на столах / полках и тематическая добыча, опирающиеся фонари в комнатах / на улицах, нижние площадки лестниц в два ряда, единый материал каждого окна. Узкие окна не пересекают дверь. Исправлены и старые сельские рамки / наблюдательная башня казармы. Только новые структуры: существующие города не перестраиваются автоматически.

[Изменения и доказательства](CITY_POLISH_1.3.1.md), [SHA / runtime receipt](verification/city-polish-1.3.1.json). Clean build / три production resource gate PASS; 8 + 6 + 53 = 67/67 финальных серверных проверок. Одиннадцать реальных headless запусков / ноль клиентов; ранние failures документированы. Установленный PolyMC JAR не заменялся автоматически, commit не создавался.

## Установка 1.2.5 в PolyMC — 2026-10-08

По прямому запросу пользователя проверенный production JAR 1.2.5 установлен в `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.2.4 сохранена вне mods; SHA-256 копии совпал, активен один JAR мода, восемь сопутствующих файлов / настроек не изменились. Клиент не запускался. [Квитанция](verification/polymc-1.2.5-installation.json). Прежние записи об отложенной установке ниже исторические.

## 1.2.5 — Локальная подготовка релиза (2026-10-08)

Выполнены disk/resource/code audits, резервное перемещение 25 QA runtime-папок (3,823,721,458 bytes), очистка Git index, .gitignore и gc без prune/expiry/history rewrite. Проект 5.559 → около 1.52 GB с новыми тестами; .git 837 → 463 MB. Миры/configs/source art сохранены. 127 duplicate groups дают лишь 279,520 raw bytes; assets/registry IDs не удалялись.

Work AI проверяет loaded chunk до POI query; local Xaero compile JAR заменён identical pinned Maven version. README/media/CHANGELOG/release notes готовы. Clean build PASS33s; production/resources PASS; independent source export с fresh cache PASS5m3s, 5,358 entries/SHA совпали. Финальные headless suites 53/53 + 6/6 + 6/6 PASS, реальные двухфазные settlement/military save/load assertions PASS. 8 новых headless запусков / 0 клиентов; промежуточные failures сохранены. Fishing fixture изолирован в daytime batch, assertions сохранены.

Commit/push/release/default не выполнялись: запрет автоматического commit; remote default main. Remote release clean-clone ещё не выполнен, source export его не заменяет. Client/PolyMC не запускались/не обновлялись. [Отчёт и ограничения](maintenance/RELEASE_PREPARATION_1.2.5.md).

## 1.2.5 — Городище, опоры домов, мешок и объёмные напитки

Реализовано 2026-10-08: нативное редкое городище `slavicmyths:gorodishche`, 240 шаблонов / три климата, два уличных плана, богатые городские дома, терем, торговый дом, площадь, каменная оборона и существующая дружина. Исправлены крыша наблюдательной башни / нижние опоры, добавлен мешок с пятью слотами по 64 и шестью размерами, 3D-кружки / бутылки. Команда: `/sm test gorodishche_showcase [temperate|cold|warm]`. [Реализация, команды и ограничения](GORODISHCHE_1.2.5.md).

Чистый build и три resource / production-JAR gate PASS; финальные серверные проверки 6 + 6 + 53 = 65/65 PASS. Девятнадцать реальных headless запусков, ноль клиентов; попытка компиляции 06 в это число не входит. Проверены native noise generate, три физические выставки при Y=-57, маршруты / POI, оплата / restock и нападение через ворота. Полная natural city на обычном рельефе и клиентская визуальная приёмка остаются непроверенными; FPS / MSPT не измерялись. PolyMC не обновлялся, commit не создавался. [Runtime evidence](verification/gorodishche-1.2.5-runtime.json).

## 1.2.4 — Village architecture; headless verification complete, client acceptance pending

Update 2026-10-07: at the user's subsequent request, the verified 1.2.4 JAR was installed in PolyMC `Slavic-Myths-1.21.1-Testing`. Previous 1.2.3 was backed up outside mods; SHA-256 matched, one active mod JAR and eight companion/configuration files unchanged. No client launch. [Installation receipt](verification/polymc-1.2.4-installation.json). The earlier installation deferral below describes the implementation stage.

See [implementation and verification](VILLAGE_BUILDINGS_1.2.4.md). Added 34 buildings per climate (102 showcase entries), replaced 334 native house/center selections across five village families and zombie pools, rebuilt rare barracks while retaining its saved ID, added themed lazy loot and guaranteed generic center shrines. Final new runtime checks 4/4 and existing regression 53/53 PASS; native assembly, negative-Y placement, paths and real workplace acquisition/arrival were checked. Clean production build and resource/JAR gates PASS. Twenty actual headless server launches, zero client launches. PolyMC was not updated, per user instruction; no commit/push. Manual appearance and varied-terrain acceptance remain pending. Earlier intermittent physical-chain test failures are documented, not hidden.

## 1.2.3 — Bandit aggression / golem defense hotfix

See [fix and verification](MILITARY_1.2.3_BANDIT_HOTFIX.md). Ordinary bandits target residents/golems; melee routing no longer stops outside attack reach. Stronghold player alarms remain intact. Golem bandit detection uses 32 blocks without changing other native targets. Clean build, production/resources and final 53/53 server checks PASS; corrected JAR installed in PolyMC with backup. Zero client launches, no commit/push.

## 1.2.3 — Current military iteration

See [implementation and verification](MILITARY_1.2.3.md). Native Villager guard profession, persistent military board/quests, bounded camp raids and modular rare barracks are implemented atop 1.2.1/1.2.2. Clean build, production/resource validation, geometry and final 49/49 isolated server tests PASS; real save/load assertions PASS. Installed 1.2.3 in the PolyMC testing instance with backup and hash validation; zero client launches. Manual visual acceptance is pending. Calm neutral spirits are excluded until aggression, by explicit user decision. No automatic commit/push.

## 1.2.2 — Work AI and settlement storage

See [implementation and verification](WORK_AI_1.2.2.md). Native WORK behavior, a 64-slot settlement chest and existing production engines implement this iteration. Weaver remains flax-only by explicit user decision. No future settlement systems were scaffolded. Clean build, resource/production gates and final 30/30 isolated server tests PASS; separate real save/load phases PASS. Installed 1.2.2 in the PolyMC testing instance; zero client launches. No commit/push.

## 1.2.1 — Villagers and professions: implemented, headless gates PASS; visual acceptance pending

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Version 1.2.1 extends vanilla Villager, ZombieVillager and WanderingTrader; it does not register substitute NPC entities or change vanilla profession IDs. Added miller, brewer, weaver, herder, hunter and cook with real native POIs, five trade levels and 120 themed listings. Vanilla picks two offers per unlocked level; saved offers, restock, demand, discounts, XP and conversion remain vanilla-owned.

Implemented three clothing climates, two coherent stable bundles, 58 native 128px images, distinct profession accessories, short tunic / long garment geometry, restrained level details, Idler and Merchant, damaged zombie overlays. Children retain vanilla appearance. A single saved/synchronized outfit index starts from UUID and is copied through infection/cure. No gender/age system, world scans or render-time RNG.

Workstations: millstone (new, approved workstation-only fallback), fermentation_vat, loom_table, feeder, drying_rack (only master part 0 is a POI), kitchen_table. Existing processing mechanics are unchanged. The millstone recipe is 4 planks + 2 cobblestone + 1 iron ingot.

Final `gradlew.bat clean build`: PASS (34s, 12 existing deprecation warnings); ordinary Gradle `test` is NO-SOURCE. Explicit isolated NeoForge server suite: 7/7 PASS; four real GameTest server launches, final run passing. Checked 360 trade samples/restock/NBT roundtrips, 13 vanilla professions at 65 levels, native job acquisition and distinct POI ownership, child/Nitwit rejection, new professions 1→5 via notifyTrade, actual infection and timed vanilla cure. Two earlier runs failed test assertions (identity comparison and poorly distributed RNG seeds); assertions were corrected, not gameplay relaxed. Pure production outfit function: 10,010 assertions. Actual baked accessory geometry: 127 meshes / 1,537 cubes, valid UVs, nose/eye clearance. Resource gate: 2,707 JSON / 5,900 model-texture links, generator byte reproducibility and production bytes PASS.

Production JAR: `build/libs/slavicmyths-1.2.1.jar`, 7,273,587 bytes, SHA-256 `283582b994e6a417f426ec7ce5d81af863e4b3af59e467ed6798c932aedd3471`. Installed in PolyMC `Slavic-Myths-1.21.1-Testing` after gates; only one active Slavic Myths JAR. Previous 1.1.8 backed up, companion mods and instance settings unchanged. Minecraft client / PolyMC launches: 0. No automatic Git commit or push.

Visual client rendering, actual relog/chunk reload, trade GUI and player discounts remain manual acceptance; NBT roundtrip is not a relog. Details and concise checks: [VILLAGERS_1.2.1.md](VILLAGERS_1.2.1.md). Evidence: [village checks](verification/village-1.2.1-checks.json), [installation receipt](verification/polymc-1.2.1-installation.json).

## 1.1.8 Integration Polish — build / production / PolyMC gates PASS

Implemented grouped Kitchen II screen and continuous supported shelf, single headscarf render path, rebuilt carved/storm staff and open-crescent sickle, native 16px berries/bushes, raised Perunite equipment meshes, common numeric rune definitions/tooltips and incompatibility, crate nine-slot GUI, C descent and smooth -0.32 descent for both vessels, rare individual animal ambient calls. Ambient source events: goose/duck use minecraft:entity.chicken.ambient at existing 0.75/1.10 sound pitch; domestic goat uses minecraft:entity.sheep.ambient at 0.9. Defensive hiss uses minecraft:entity.cat.hiss only from GooseDefence.

Apple uses one taller fixed crown with the old Y=4 saved-world anchor; pine is 9–12 blocks with separated tiers; four pine saplings select a validated 2x2 giant of 16–20 blocks, never natural giant spawning. Sapling preflight now respects world height and loaded chunks. Existing berries/hops bonemeal increments remain intact.

Five definitive recipes repaired. Shared kurgan candidates use 41:4:1 weights, approximating old relative density (1/20² : 1/64² : 1/128²); legacy warrior/great sets are preserved with frequency 0. Common placement 20/15 → 50/30; retained legacy placements 64/55 → 160/110 and 128/119 → 320/238. Large bandit camp 128/119 → 320/238, salt retained. Apple rarity 18 → 6. These are configured values, not measured world frequencies.

165 acquisition IDs reviewed, missing routes 0; RU/EN missing keys 0 and remaining mojibake 0. See LOCALIZATION_AUDIT_1.1.8, OBTAINABILITY_AUDIT_1.1.8 and FUNCTIONAL_AUDIT_1.1.8. `gradlew.bat clean build`: first attempt failed on Component.withStyle; permitted retry PASS in 33s, 12 deprecation warnings; Gradle test task NO-SOURCE. Actual pure Java assertions: 300 TreeShape + 49 BrewRules. Production/resource gates PASS (2692 JSON, 5894 asset links); duplicate Vat screen bytecode regression PASS; tests excluded from JAR. Source rune numerical equivalence PASS. Versioned JAR: build/libs/slavicmyths-1.1.8.jar, 7192357 bytes, SHA256 b108d74baf242c71136a63670bb9da90db40d7c029b025475e4e9091d107c4fe. Installed only this mod into Slavic-Myths-1.21.1-Testing; backed up/replaced 1.1.7; six companion files and two instance configurations have identical hashes. Receipt: docs/verification/polymc-1.1.8-installation.json. Branch main, no release branch/tag. Automated Minecraft/PolyMC/server launches: 0; visual, gameplay, rejoin and natural frequency acceptance remain manual.

## 1.1.7 — Исправление падения при регистрации экрана (2026-10-07)

По присланному пользователем логу реальный запуск клиента завершился ошибкой `Duplicate attempt to register screen: slavicmyths:vat` в ClientSetup.screens: экран был зарегистрирован дважды подряд. Удалена только повторная строка; gameplay/resources/registry IDs и версия1.1.7 сохранены. Предыдущая compilation/resource validation не проверяла уникальность screen registrations и не обнаружила эту runtime-ошибку.

Добавлен regression gate для уникальности menu registrations и проверки ровно одной ссылки VatMenu.TYPE в production ClientSetup bytecode (javap); проверка выявляет дубль в предыдущем committed source. Один gradlew.bat clean build PASS31s/12 deprecated warnings/test NO-SOURCE. python tools/verify_brewing.py --jar build/libs/slavicmyths-1.1.7.jar PASS9701 resource/production checks и49 pure-Java arithmetic assertions. Production SHA256 a3d5cca4fd7b41d58aa9111e1a7f0a4ab04f484fb87cd2db9cec243d4f0cd084. Автоматических Minecraft/PolyMC/client/server запусков для hotfix0; успешный client startup пока НЕ проверен.

PolyMC hotfix installation: после commit b8b388c6/push исправленный1.1.7 установлен в существующий тестовый профиль, installed SHA256 совпал с production. Прежний JAR сохранён в .slavicmyths-backups/pre-screen-hotfix-20261007-134329;6 companion mod hashes и2 profile config hashes не изменились. Receipt: docs/verification/polymc-1.1.7-screen-hotfix.json. Manual: повторить запуск профиля Slavic-Myths-1.21.1-Testing до главного меню, затем открыть экран бродильного чана.

## 1.1.7 — Пивоварение и брожение (2026-10-07)

Реализовано поверх 1.1.6 без отката: хмель AGE0..5, LOWER/UPPER с двухблочной высотой стадий3..5, рост0.85 vanilla, bonemeal +1, 3x3 fertilizer +1, sickle mature loot/reset1/durability1. Mature loot2..4 hops/1..2 cuttings; grass fallback chance0.04 через существующий seed modifier (прежние seeds0.12 сохранены). Native16px winding/leaf/cone stages, отдельные силуэты солода/мешка/настоев/жмыха/мезги/порошка/тары.

Ручной fruit_press: 4 apples или berries, ровно3 empty-hand действия, actual input display/опускающаяся давилка, output/remainder извлекаются рукой; Shift-empty снимает сырьё. Fermentation_vat:4 inputs/2 returns/2 outputs/1 catalyst, отдельный простой экран с подписями, progress/mode, native custom RecipeType/Serializer, 15 JSON recipes, consume snapshot/locked inputs и катализатор, loaded game ticks, no fuel. Лишний fifth reagent thunder powder использует catalyst slot; Veles staff не расходуется и не получает damage; Witch использует расходуемую rune_forest. Thunder stone ->4 powder. Craft dark_bottle:3 glass+stick по прямому уточнению пользователя. Для press/vat/keg ТЗ не задаёт craft-схемы: использованы доски/палки/железо/существующая wooden_barrel, выбор сообщён пользователю.

Существующий wooden_barrel/household_storage IDs и Items NBT сохранены: old storage default, fermentation включается только batch в empty barrel,8 servings; отдельный brewing blockstate включает ticker лишь в active mode. Quality ages0–2399 Young/2400–8399 Ready/8400–11999 Aged/12000+ Spoiled, только loaded ticks, NBT beverage/age/servings. При break active batch теряется, никаких free ready drinks. Small_keg8 servings без брожения, transfers1/4, запрещает смешивание IDs и качества, при break drops сохранённые порции и empty block. Pitcher4 servings для kvass/mead/cider/mors, перелив с пустой тарой в другой руке; Bottle/mug actual return/model component, частичный pitcher не превращается в пустой раньше срока, inventory-full output safely drops.

8 beers +kvass/mead/cider/mors/apple juice/spoiled, эффекты/времена/качество по ТЗ, native CustomData/CustomModelData для quality/container/pitcher count. Persistent NeoForge player attachment, ordinary+1/magic+2/nonalcoholic0, lazy decay1/1200ticks/no player ticker, no copyOnDeath, milk не пишет counter, temporary staged penalties и magic x1.5, bounded amplifiers. Два server-awarded advancements without XP, tags/ru/en/Creative; technical wort/must IDs hidden. JEI новые категории не добавлены, hard dependency нет.

Проверки: python tools/verify_brewing.py PASS6196 checks/2691JSON/5890links/49 actual pure-Java arithmetic assertions. Первый clean build FAILED на двух собственных API integration errors (ambiguous attachment supplier, LevelAccessor neighbor method); исправлены. Разрешённый пакетом retry PASS34s; после него обнаружен technical ID mismatch witch_berry_wort. Пользователь отдельно разрешил дополнительный build; ID и active-only barrel ticker уточнены. Финальный gradlew.bat clean build PASS29s/12 existing deprecated warnings/test NO-SOURCE. Production gate python tools/verify_brewing.py --jar build/libs/slavicmyths-1.1.7.jar PASS9698 resource/production checks,49 pure-Java assertions. SHA256 ed48756b49349a7a4651fdf61b53a1ff5fe4ddfb93cf8525c3e0aaebd628a691. После отдельного разрешения пользователя выполнены две дополнительные successful clean builds:30s (ID/ticker),29s (порядок advancement до inventory.add и counter без незаданного лимита100). Всего4 clean build attempts для1.1.7; игровые/runtime tests не выполнялись.

PolyMC: перед1.1.6 установлен проверенный1.1.5. После final successful build/production gate/commit54f4d08d/push origin main установлен проверенный slavicmyths-1.1.7.jar в Slavic-Myths-1.21.1-Testing MC1.21.1/NeoForge21.1.255. Installed SHA256 совпадает с production; прежний1.1.5 перенесён в .slavicmyths-backups/pre-1.1.7-20261007-133454. Все6 companion JAR hashes и instance.cfg/mmc-pack.json hashes совпали; worlds не трогались. Receipt: docs/verification/polymc-1.1.7-installation.json. Minecraft/PolyMC/client/server launches0.

Manual: hops grow/break either half/sickle/fertilizer; press3 actions/apples/berries; vat ingredients/catalyst/counts/returns/full output; old barrel inventory/hoppers +batch stages/save-rejoin/unloaded pause/break loss; mugs/bottles/offhand pitcher/keg portions/full inventory/break drops; effects/load2/3/4/5/magic/lazy recovery/milk/death; UI/models/tabs/lang/two advancements.

## 1.1.6 — Хранение и хозяйственный быт (2026-10-07)

Перед работой по прямой команде пользователя проверенный 1.1.5 JAR установлен в существующий PolyMC Slavic-Myths-1.21.1-Testing (MC1.21.1/NeoForge21.1.255), SHA256 c4bda9ee816a35f00ae02d703f1610dcd7426ba3d26f08dabd12ebabde4dd48d; прежняя1.1.0 в .slavicmyths-backups/pre-1.1.5-20261007. Companion mods не изменены; Minecraft launches0. Установка1.1.6 не выполняется, далее поручена1.1.7.

Реализовано поверх1.1.5: storage_sack capacity4/один item type/FILL0..4; basket4, large_basket9, produce_crate9 с соответствующими tags и реальными representative stacks (4/6/6). Household_chest27 universal/vanilla-style screen/OPEN по viewers, wooden_barrel9/tag/один тип/vanilla-style screen и ограничения вставки через GUI/воронку. Бочка в1.1.6 только storage, никаких fermentation fields. Wall_shelf3 slots и solid wall support, storage_shelf6 slots/LOWER+UPPER, каждый slot max16; каждый реальный occupied stack рендерится, hit position выбирает место. Direct storage insert/extract1/Shiftmax, server-only, save/client snapshots/drop-once; belt event уступает storage Shift actions.

Drying_rack2x2, один lower-left master, четыре независимых inputs/progress/recipe IDs/result snapshots, drying recipes2400ticks для ягод и двух mushrooms; early extraction возвращает input, complete — result, chunk load сохраняет прогресс, sync только stages/insert/remove/finish. Dried_berries nutrition3/saturationModifier.35/без effects; dried_mushrooms не съедобны, Food tab. Три отдельные снопа с разными силуэтами без BE, craft9grain и reverse9grain. HaystackLOWER+UPPER/count1..4/четыре высоты, inserthay1/sneakextract1, breaking возвращает EXACT hayCount без отдельного haystack item.

13 новых blocks/2 products, native16px textures/icons, отдельные meshes/states/item/loot/recipes/storage tags/ru/en/Creative/winter_stores без XP. KitchenDisplay food utility переиспользована; прочие items используют FIXED/category transforms без ItemEntity/bob/spin. Только drying master тикает; остальные containers без глобальных scans/постоянных packets. Статические data/resources/geometry/reproducibility checks PASS:7575 checks,2569JSON,5622model/texturelinks. Первый clean build остановился на собственной generic-map ошибке DeferredHolder<Block,StorageBlock>; карта исправлена на bounded wildcard. Единственный повтор `gradlew.bat clean build`: PASS29s,12 warnings, test NO-SOURCE. `python tools/verify_household.py --jar build/libs/slavicmyths-1.1.6.jar`: PASS10881 static/resources/geometry/production checks. SHA256005909dae4ecd2264d30406bcf2f7b9bc19847c5f68a6ddcdfd85480fea20c12. Minecraft/server/unit runtime tests не выполнялись.

Manual: sack fill/type reset; actual basket/crate items; chest27/barrel single type/GUI/hoppers; wall support/3 slots и standing shelf6/food-bottle-tool-block models/save-rejoin; drying berries/mushrooms/early extraction/progress/load/stages; sheaves/reversecraft; haycount1..4/extract/break exact; break every multiblock part/occupied placement/no duplicate or orphan; Creative/lang/winter_stores.

## 1.1.5 — Кухня II (2026-10-07)

Реализовано поверх main 1.1.4, прежние registry IDs сохранены. Обычные pine/linden tables используют старые SINGLE models/textures, взаимный connection state максимум PAIR, две наружные ножки на физическую половину; при разрушении партнёра остаётся SINGLE. Новый non-ticking TableTile хранит четыре ItemStacks, ближайший свободный/занятый surface slot, server placement/retrieval/eating по vanilla rules с container return, drop inventory и update packets только при изменении. Table food display: dedicated низкополигональные хлеб/репа/миски/блины/пироги для всех 22 блюд, FIXED fallback для перечисленной старой еды.

Существующий kitchen_table стал двухблочным LEFT/RIGHT workstation с одним master BE, placement проверяет вторую позицию; удаление любой половины очищает обе, master выдаёт ровно один block item/реальное содержимое, consumed ingredients/progress и готовые pot servings теряются. Существующие rolling_pin/metal_pot сохранены, stack1/без durability; GUI: 2 инструмента, 2x2 ингредиенты, output, 2 return slots, progress/servings/одна подача с bowl. Порции без миски недоступны, готовый/активный pot нельзя снять.

Новый slavicmyths:kitchen recipe type/serializer, 22 data-driven recipes, order-independent counted/tag matching; unrelated ingredients запрещены. Сервер проверяет инструмент, свободный output/return slots перед расходом; сохраняет active ID, result snapshot, mode/time/progress, pot dish/servings и inventory. Lookup только при изменении инвентаря/завершении/onLoad, без global scans; серверный ticker обрабатывает только локальный процесс. Progress sync каждые 10 ticks плюс изменения inventory/state; menu использует DataSlots. Hopper automation не добавлена.

Пять flour/groats/dough ресурсов, все 22 блюда с точными nutrition/saturationModifier/stack sizes без effects, bowl dishes возвращают bowl. Повторно использованы karavai/berry_pie/ukha, остальные старые food IDs сохранены; старые crafting bypass для karavai/berry_pie удалены. Existing mill was not present; fallback crafting recipes were used. JEI optional Kitchen category обновлена для реальных recipes/инструмента/порций, без hard dependency. Новые 16px native icons/block textures, kitchen halves/working props/GUI, ru/en, cooking/placeable/complex tags, generous_table без XP. Legacy kitchen generation завершается canonical Kitchen II overlay.

Проверки: static resources/geometry PASS (7205 checks, 2474 JSON, 5202 model/texture links). `gradlew.bat clean build`: финальный PASS, 18 s; compileJava FROM-CACHE, test NO-SOURCE. Всего 3 clean build команды, все PASS (27/24/18 s): после первой выявлены собственные integration defects (belt sneak interception, nested render pose, UV item partner); UTF-8 ошибка служебного скрипта не позволила включить все правки во второй проход, поэтому выполнена одна дополнительная сборка сверх лимита пакета. `python tools/verify_kitchen.py --jar build/libs/slavicmyths-1.1.5.jar`: PASS, 10406 static/resources/geometry/production checks. Production SHA256 c4bda9ee816a35f00ae02d703f1610dcd7426ba3d26f08dabd12ebabde4dd48d. Старый KitchenRecipes.class отсутствует, актуальные assets/data байт-в-байт совпадают с JAR. Minecraft launches 0; игровые/server runtime/unit tests не выполнялись. PolyMC не изменён (1.1.0), JAR автоматически не копируется.

Manual: старый SINGLE/PAIR/4 наружные ножки/третий SINGLE/разрушение партнёра; четыре еды/3D/выдача/eating/containers/full inventory/save-load; двухблочный kitchen placement/занятая вторая позиция/оба варианта разрушения; инструменты/помол/dough/rye bread/karavai; bliny +4 fillings/4 pies; pot dishes 2–3 servings/bowl/no-bowl/Creative; output/return-slot blocking, buckets/bottles, tool locks; chunk unload/rejoin/active process, breaking active/ready без дюпов; nutrition/saturation/no effects; Creative/lang/advancement/JEI.

## 1.1.4 — Текстиль и переработка (2026-10-07)

Реализовано поверх 1.1.3 без изменения существующих registry IDs: нож разделки (250 durability), одна carcass entity с семью вариантами и сохранением 6000-tick срока/обожжённого состояния; только взрослые животные с ответственным игроком, стандартный мясной/material loot заменён, XP сохранён. Разделка и fallback имеют отдельные таблицы из ТЗ; операция завершается до поломки ножа. Добавлены animal_fat/goat_hide, жировая свеча со стандартными candle states и светом.

Цепочка flax_stalk → flax_breaker → flax_fiber → spinning_wheel → linen_thread → loom_table → linen_cloth. Мялка: 4 стебля/3 клика; прялка: 2 волокна/100 ticks; ткацкий стол: 4 нити/160 ticks. Ручное заполнение/извлечение, сохранение input/output/progress, выпадение содержимого при разрушении; только два локальных BlockEntity tickers. Прямые crafting recipes нити/ткани удалены, старые ID сохранены.

Семь недамажимых предметов одежды без armor/бонусов, отдельные тонкие player meshes для standard/slim; пояс имеет внутренний синхронизируемый persistent attachment, серверное надевание/снятие и death/keepInventory handling. Льняная двухблочная кровать сохраняет vanilla bed mechanics; только завершённый глубокий сон даёт well_rested 12000 ticks, скорость +5%, добыча +10%, обновление общего срока без усиления. Новые native pixel assets, blockstates/loot/recipes/tags, ru/en, Creative categories и linen_craft без XP.

Первый clean build выявил собственную ошибку типа renderer при регистрации clothing layer; исправлена проверкой PlayerRenderer. Единственный повтор `gradlew.bat clean build`: PASS, 27 s (10 deprecated/unchecked warnings, test NO-SOURCE). `python tools/verify_textile.py --jar build/libs/slavicmyths-1.1.4.jar`: PASS, 8665 static/resource/production checks, 2309 JSON, 4952 model/texture links. Production JAR: build/libs/slavicmyths-1.1.4.jar; SHA256 a8bd0e05f4c3e0139cb97e7071b5b1afbce2c9e57ba166043ae47ae0d2e54bab. Minecraft launches: 0. Игровые/server runtime/unit проверки не выполнялись. PolyMC не изменён (1.1.0). Ручная приёмка: разделка семи взрослых видов и исключения baby/chicken/чужой моб; огонь/XP/timeout/save-load/последняя прочность; полная льняная цепочка, вместимость/выдача/разрушение/save-load; свеча 1–4/огонь/вода; одежда standard/slim/движение; пояс equip/occupied/unequip/full inventory/смерть/keepInventory/перезаход/синхронизация; кровать/прерванный и полный сон/бафф; recipes/Creative/advancement/translations.

## 1.1.3 — Животноводство (2026-10-07)

Реализовано поверх main 1.1.2: ровно goose/duck/domestic_goat, стандартный baby age и три взрослых spawn eggs. Шесть отдельных adult/baby meshes с разными пропорциями, UV-atlases 128px, базовые walk/swim/wing/neck/nip/eat анимации. Гусь предупреждает 20 ticks, защищает не более 80 ticks, укус 1 damage/20 ticks, отстаёт за 8 blocks; Creative/Spectator исключены. Утка периодически ищет близкую поверхность воды; spawn на земле в указанных биомах, что разрешено пакетом. Коза без vanilla ram/jump, adult milking через стандартный MilkBucketItem.

Feed tags поддерживают три новых вида и cow/sheep/pig/chicken/rabbit; vanilla foods сохранены. Серверное ручное кормление: breed/baby growth и heal 2, без расхода здоровому взрослому на breeding cooldown. Кормушка capacity 16/один тип, visual fill 0..3, ручной insert/extract, save/load; только baby/injured AI, рост +400 ticks, cooldown 600 ticks, никогда love mode. Рецепт по подтверждению пользователя: 7 planks + iron nugget. Гнездо capacity 3 mixed eggs, visual count/save/load/insert/extract; adult bird egg timer 9600..18000 ticks, bounded path timeout 200 ticks и fallback drop. Яйца — только продукты, не еда/снаряды/вылупление. Шесть meat items с ТЗ food values, smelting/smoking и Looting/fire drops; три biome modifiers, Food/Decor/Mobs tabs, ru/en, full_yard AND advancement без XP.

Ресурсы и UV: `python tools/verify_husbandry.py` PASS до сборки, 5091 static checks, 4715 model/texture links. `gradlew.bat clean build`: первый проход остановился на собственной ошибке переопределения final canBreatheUnderwater; заменено на актуальный NeoForge canDrownInFluidType. Единственный разрешённый повтор clean build: PASS (26 s). `python tools/verify_husbandry.py --jar build/libs/slavicmyths-1.1.3.jar`: PASS, 8063 static/resource/UV/production checks. Production JAR: build/libs/slavicmyths-1.1.3.jar, SHA256 72cbff5245d936ac9dcbe1546ea767fa2c21e476b1313a73fad0cfb7980d22a8. Реальных Minecraft launches: 0; unit/gameplay/server runtime tests не выполнялись. PolyMC не изменён: установленная версия остаётся 1.1.0. Нет custom goose/duck/domestic_goat OGG: 10 событий используют разрешённые vanilla event references; не заявляется окончательная звуковая идентичность. Gameplay/visual acceptance ожидает ручного теста пользователя.

Manual: три eggs/breeding/baby appearances; goose/duck swim и короткая защита гусёнка; natural spawn; eggs/nest fallback и insert/extract; milking/drinking; feeders для всех 8 видов, heal/growth/cooldown/no-auto-breeding; новые vanilla feeds; мясо/cooking/fire/Looting; sounds/tabs/full_yard; save/load inventories/timers.

## 1.1.2 — Сады и Creative tabs (2026-10-07)

Реализовано поверх текущего RC2/1.1.0: пять многолетних ягодных кустов age/PHASE 0..4, отдельные sapling items, harvest без уничтожения, two-block raspberry с синхронизацией половин и защитой от двойного loot. Старые raspberry/blueberry registry IDs сохранены; berry items больше не используются для посадки. Удобрение поддерживает кусты, серп/лейка не расширены. Пять отдельных garden patch features, ограниченные biome modifiers и rarity 8/10/14/12/8; прежняя mixed berry injection отключена с сохранением feature ID.

Яблоня: одна форма высотой 7/шириной 5, 9 logs и 74 leaf positions, 26 deterministic fruitable offsets. Плодоношение 0..3 и ручной vanilla apple harvest, без apple в break loot. Crown anchor random tick восстанавливает максимум один leaf, только внутри mask, без BlockEntity/global scan. Собственная wood family с sign/hanging sign, stripping, recipes/tags/textures. Девять стандартных CreativeModeTab, порядок 5 верхних/4 нижних, одна категория на предмет; старая вкладка ID сохраняется как Main/Lore, общего dump больше нет.

PASS: `gradlew.bat clean build` (27 s), затем `gradlew.bat build` (11 s) после исправления обнаруженной ошибки классификации старых Yaga utility items. Это два build-прохода, хотя пакет просил один; повтор выполнен ради исправления собственной ошибки классификации, не ради общего аудита. `python tools/verify_gardens.py`: PASS, 2262 JSON, 4624 model/texture references, production version 1.1.2 и test harness отсутствует в JAR. Unit/game tests не выполнялись. Minecraft launches: 0. PolyMC не изменён по отдельному подтверждению пользователя; установленный там мод остаётся 1.1.0. Commit/push main разрешены пользователем для этой итерации; release branches/tags/releases не меняются.

Manual: 9 tabs/rows/no duplicates; natural berries и повторный harvest; shears/raspberry halves; fertilizer +1/no-op; одинаковая apple shape; ripe hand harvest/no apples from broken leaves; bounded gradual canopy regrowth/anchor removal; wood recipes/signs/stripping; ru/en/food/advancement. GUI/gameplay acceptance не заявляется по компиляции.

## RC2 UI / Visual correction — 2026-10-06

## 1.1.0 Земледелие — реализовано локально, установлено в PolyMC (2026-10-07)

- Поверх текущего RC2: 7 культур age 0..6, 7 семян, 7 продуктов, серп, полевая мотыга, лейка, органическое удобрение. Дикорастущий `flax` и все старые ID сохранены.
- Общий `FarmingCrop` использует vanilla random ticks/свет/влажность и NeoForge growth hooks. Капуста: gate 0.80; лён: 0.90; остальные: 1.00. Bone meal: +1..2 стадии.
- Data-driven возрастной дроп; Fortune только на зрелый продукт, случайный дополнительный бонус 0..min(level,3), семена не затрагивает. Дополнительная репа: 30%.
- Серп: один зрелый crop, стандартный loot, возврат age 1, durability 250. Мотыга: поперечная линия из 3 блоков, sneak — 1, durability 350, расход по фактическим преобразованиям.
- Лейка: persistent/network data component 0..8; sneak source-water refill без удаления источника; увлажнение vanilla farmland 3x3, один заряд, без изменения crop age. Удобрение: поддерживаемые crops 3x3, +1 age, расход только при росте.
- Grass loot modifiers: short grass 12%, tall grass 18%, одно случайное семя из семи; vanilla drops сохранены, проверка целой пары tall grass исключает вторичный бросок.
- 49 разных стадий растений + 18 item sprites, все 32x32 RGBA с бинарной прозрачностью; ru/en, теги, advancement `new_seeds`, 4 рецепта, порядок в существующей Creative Tab.
- `gradlew.bat clean build`: PASS (43 s). Финальная перепаковка исправленных JSON `processResources jar`: PASS (19 s), compileJava UP-TO-DATE. Static acceptance: 4740 проверок, 2075 JSON, 4225 ссылок; 166 production JAR checks.
- PolyMC testing: `slavicmyths-1.1.0.jar` установлен; предыдущий RC2 вынесен в backup; companion mods сохранены. Minecraft не запускался: 0 запусков. Ручная игровая/визуальная приёмка остаётся пользователю.
- Git: текущая `main`; автоматический commit/push не выполнен по прямому правилу AGENTS.md. Вложенный пункт Git finish не отменяет это правило; изменения сохранены локально, релизные ветки/tags/releases не менялись.
- Отчёт: `docs/verification/farming-1.1.0/REPORT.md`; манифест файлов, static checks, installation receipt и offline contact sheet рядом.


Внесён пакет UI/Visual поверх текущего main/RC2: два входа наковальни и copy-only preview, три режима/автоматический расход доп. материалов, Creative XP=0, новый Path Stone/Kitchen UI, исправлен повторный blur книги, реальные оси щита, пересобраны Club/Battle Axe UV0..16, четыре разных древесных материала и двери/люки/signs. Registry ID, рецепты, баланс survival, курганный hotfix сохранены. Сброс пути и новые пары конфликтов рун не выдуманы. Разбойники/Соловей отложены.

PASS: clean build; 10/10 loader GameTests с companions; шесть CPU/data/codec/geometry gates; production resources 0 missing refs. Реальных headless запусков в этом этапе: 2 (первый 8/10 с ограничением synthetic connection/DataSlot, финальный 10/10). Клиент: 0. GUI scales 2/3/4, игровые held poses и реальный reconnect — MANUAL PENDING. Результат не объявлен визуально принятым. Версия остаётся 0.9.10-rc2; опубликованный tag/prerelease не перезаписывается. Исправленный JAR установлен в существующий PolyMC 1.21.1 RC2 после gates; прежний сохранён вне mods, остальные моды/config/saves без изменений. SHA256 `ca32e6ac1b8a67a24cd5efadb051fbc0079e525bdb5b9642b1b0552f228a571e`. Commit/push не выполнялись по текущему AGENTS.md. Отчёт и список файлов: `docs/verification/rc2-ui-visual/REPORT.md` и `changed-files.txt`.

## 0.9.10 RC2 — server acceptance и установка (2026-10-06)

Завершены оставшиеся серверные проверки: 9/9 workstation+hotfix GameTests и 27/27 регрессий, включая подход всех трёх курганов без повреждения кладки. Щит реально блокирует спереди, изнашивается, получает axe cooldown; repair/Unbreaking/Mending/NBT проходят. Камень пути: RMB/menu, offering/XP, навыки, navigation SavedData, range guard, NBT. Наковальня: forge/install/remove, расходы, validation, shift-click, close-return и components. Кухня: 12/12 рецептов, guards, consumption, tool wear, remainder, output shift-click и NBT.

Исправлены все 16 старых door-model parents у darkened/pine/rowan/willow; native blockstates включают все facing/half/hinge/open варианты, старые имена моделей сохранены как aliases. В кухне отображаются food effects или их отсутствие, подробности в tooltip; gameplay блюд не изменён. Воспроизведение door resources: `python tools/migrate_remaining_doors_1211.py` после исторических resource generators.

`clean build` + шесть CPU/data/codec checks PASS. Production resources: 4042 references, 0 missing asset issues; static data 2685/0 errors. JAR SHA256: `faa54bd5decf79dc029b1d722f1995b195aa313dd5cea900c16bd7a70ea5a499`. Установлен в существующий PolyMC Slavic-Myths-1.21.1-Testing; прежний dev JAR сохранён вне mods, 6 прочих модов и configs/saves не изменены. Client 0 по выбору пользователя; эта итерация имела 3 реальных headless server запуска (первый исправлял test fixture, затем 9/9 и 27/27). Реальный multiplayer/перезаход, визуал и GUI scales — MANUAL PENDING. Отчёт/receipt: `docs/verification/playtest-0.9.10-rc2/`. Сборка/docs/checksum: `release/0.9.10-rc2/`.

Исходники опубликованы в `main`, она установлена основной веткой GitHub; история существующей `master` сохранена. Player branch `release/0.9.10-distribution` обновлена до RC2. Prerelease: https://github.com/Deserag/slavic-myths/releases/tag/v0.9.10-rc2 — JAR/ZIP/MRPACK скачаны обратно и проверены по SHA256. RC1 не перезаписывается, новый tag `v0.9.10-rc2`. Нового overhaul разбойников/Соловья, Equipment & Art или rune system 0.9.11 нет. Полная ordinary-world natural coverage и measured MSPT не заявлены принятыми.

## RC1 — отдельная сборка для игроков и уточнение приёмки (2026-10-06)

Опубликована отдельная distribution branch `release/0.9.10-distribution` с 9 файлами: playable JAR в mods, installer закреплённых зависимостей, lock/checksum и player docs. Исходников/Gradle/миров/configs нет. Source branch `release/0.9.10-playtest` и tag `v0.9.10-rc1` сохранены. Загрузчик реально скачал 6/6 внешних модов с проверкой размеров/SHA256; набор содержит 7 JAR. XaeroLib вложен в обе карты и отдельно не требуется. ZIP и .mrpack подготовлены для prerelease; Java/NeoForge/Minecraft и профиль пользователя не устанавливались/не запускались.

Список невыполненного: `docs/release/RC1_REMAINING_WORK.md`. P0 исправлены в коде, но реальная игровая/визуальная приёмка не пройдена. GUI scales не проверены в клиенте; дополнительные meal effects в kitchen preview не показаны; регрессия расчистки кладки не перезапускалась после исправления; 16 старых door-model parents остаются. Не обозначать RC как полностью принятую игровую сборку. Этот этап меняет упаковку/документацию; production JAR/код/контрольная сумма RC1 не менялись.

## 0.9.10 RC1 — public playtest stabilization (2026-10-06)

Ветка `release/0.9.10-playtest`, версия `0.9.10-rc1`. Исправлены UV/round model/blocking щита, зарегистрирован экран существующего RPG-меню Камня пути/рунной наковальни, добавлен shift-click наковальни; книга получила непрозрачную страницу и scroll, кухня — выбор рецепта/условия/результат. Курганный hotfix включён: сохранённые encounter slots, фиксированные квоты/боссы, наружные patches и единый 100-tick Порез. Геометрия кургана и существующие ID сохранены. Разбойники/Соловей не перерабатывались; полный Equipment & Art Overhaul и новая система рун не заявлены реализованными.

`clean build` и шесть CPU/data/codec проверок PASS; production JAR/resources PASS, 2685 статических ссылок, 0 новых ошибок. 16 прежних door-model parent issues отмечены отдельно. До последнего запроса hotfix: 5/5 GameTests; общая регрессия 26/27 обнаружила расчистку кладки, исправленную затем без повторного запуска игры. Клиент 0; после последнего запроса Minecraft не запускался. Игровая/визуальная приёмка — MANUAL PENDING. Отчёт: `docs/verification/playtest-0.9.10-rc1/acceptance.json`; playtest docs/JAR/checksum: `release/0.9.10-rc1/`. Изменения прежнего рабочего дерева включены, runtime worlds/logs/configs не включаются в release commit.

## 0.9.10 — анализ нового пакета (2026-10-06)

Прочитан Equipment & Art Overhaul FINAL; начато сопоставление с реальными ID, способностями и boss loot. Production-изменений 0.9.10 пока нет: требуется согласовать отсутствующие точные параметры и рецепты согласно просьбе пользователя не додумывать логику. Разбор: `docs/equipment/SPEC_REVIEW_0.9.10.md`. Существующие изменения 0.9.9 сохранены. Build/resource gates этого этапа не запускались; Minecraft-клиент — 0 запусков.
Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

## Текущий override: структура имеет приоритет над сушей

Реализована отдельная пошаговая ручная генерация трёх курганов и трёх лагерей: FULL-чанки до измерения высоты, проверка конечного объёма, подъём на тонком superflat, заполнение грунтом, выравнивание суши, локальный перенос от воды/защищённых построек в пределах 256 блоков. Поиск и генерация имеют status/cancel и не выполняют прежние синхронные обходы сотен удалённых starts. Проверка команд выполняется на headless сервере, Minecraft-клиент не запускается.

Подробности, ограничения отмены и результаты проверок: `docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md`. Полная 0.9.9 остаётся в разработке: естественное покрытие обычного мира и компенсация не приняты. Записи ниже — история предыдущих этапов; их заявления о неизменённом production-коде относятся к моменту диагностики.

Research update 2026-10-06: real kurgan generate command reproduces the reported false 237-block terrain rejection caused by an unloaded heightmap; original-position bedrock preflight and synchronous search risks identified. User save structure settings inspected read-only. This is a diagnosis, not a fix; production code and installed JAR unchanged. Details: `docs/worldgen/COMMAND_GENERATION_DIAGNOSIS_0.9.9.md`.

Deployment update: by explicit user request, `0.9.9-dev` is installed in PolyMC `Slavic-Myths-1.21.1-Testing` for manual generation testing. Previous 0.9.7 JAR is backed up outside mods; other mods and worlds are unchanged. This is not release acceptance. Receipt: `docs/verification/mob-worldgen-0.9.9/polymc-dev-installation.json`. Minecraft was not launched.

# Slavic Myths 0.9.9 — implementation in progress

Continuation 2026-10-06: fixed raised-kurgan authored entrance stones using the persisted graded surface; soil excavation preserves underground construction. All three tiers and all three approach lanes pass; all 27 required companion GameTests pass. Clean production build `-Pmod_version=0.9.9-dev` passes; JAR resource validation passes. The latest natural-coverage measurement code includes relocated eligible cells and fails unmeasured/empty applicable networks; full natural gate still pending. Evidence: `docs/verification/mob-worldgen-0.9.9/continuation-2026-10-06.json` and `docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md`. Stable PolyMC artifact is unchanged.

COMPILES: six spirit combat timelines, wildlife families/stat changes, existing bandit HP alignment, shared placement profiles, retained template foundation bounds, accepted-neighbor collision planning, and bounded persisted kurgan earthworks for thin superflat (explicitly authorized by the user). Build and verification details: docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md and work/mob-worldgen-099-*.log. Static data audit: 2676 references, zero errors. Actual accepted-placement coverage/compensation, complete mob polish/audio audit, final resource/release gates and companion runtime checks remain unfinished. This is not 0.9.9 release acceptance. Installed stable PolyMC artifact remains 0.9.7; Minecraft client has not been launched.

# Slavic Myths 0.9.7 — болото и runtime hotfix

Реализованы: семь семейств болотных мест / 21 modern template; четыре дерева и природные детали только vanilla swamp; болотник 48 HP / редкий страж 112 HP; новые swamp русалки 36 HP и водяные 72 HP; пять таблиц структурной добычи + drop болотника. Старые ID/templates и остальной контент сохранены, новой biome/ore/block семьи нет.

Xaero logout/publish null-safe с очисткой внутренних данных перед optional callbacks, пользовательские waypoint сохраняются; JEI catalyst связан с зарегистрированным ritual RecipeType; linden_door использует native 1.21.1 states/models. Другие 16 старых door model parent issues остаются backlog.

DONE по build/headless: production 0.9.7, clean build; финальные core без JEI и шесть companions — по 14 GameTests без ERROR/FATAL. 42 сухих/мокрых размещения, 96 контейнеров, 720 опор, 400 форм деревьев, семь реальных accepted starts; пять features и болотник подтверждены в modified biome settings. Проверены 10 lifecycle cases production Xaero adapter с doubles и compiled JEI recipe-type contract. Проверки/gates и hash: docs/verification/swamp-0.9.7/acceptance.json; установка: installation-receipt.json в той же папке. JAR 0.9.7 установлен в Slavic-Myths-1.21.1-Testing, семь хешей проверены, прежний 0.9.6 сохранён; старые профили/миры не изменены. Headless размещение/данные/генерация/геометрия проверяются отдельно от клиента. Manual visual/gameplay QA: NOT STARTED; следующий конкретный блок — docs/MANUAL_QA_0.9.7_SWAMP.md. Клиент не запускался.

Подробнее: docs/swamp/SWAMP_0.9.7_*.md. Ниже сохранены исторические состояния прежних версий.

# Slavic Myths 0.9.6 — курганы

DONE по build/headless: графовый план v2 и совместимость v1, 14 архетипов, альтернативные лестницы, новая архитектура/палитры, 26 блоков + 26 BlockItem, 60 рецептов, семь новых таблиц наград, 35 авторских текстур 256×256 (11 блоков и 24 иконки). Существующие ID, характеристики, старый loot и глобальная частота генерации сохранены.

PASS: clean build; 150 новых планов; 1200 legacy планов и 900 составов; по 10 GameTests без JEI и с companion-модами, включая реальное размещение/контейнеры/ловушку. Data totals: 411 items, 179 blocks, 273 recipes, 260 loot tables, 154 advancements. Production: `build/libs/slavicmyths-0.9.6.jar`; evidence: `docs/verification/kurgan-0.9.6/acceptance.json`.

Client: COMPILES. Ручная визуальная/игровая QA: NOT STARTED, клиент не запускался. Следующий конкретный блок — `docs/MANUAL_QA_0.9.6_KURGAN.md`. Известный post-port backlog: 20 старых missing door model parents; подробности в resource-check.json. Проверенный JAR 0.9.6 установлен в известный тестовый PolyMC, семь JAR сверены по hash; прежний 0.9.5 сохранён. Receipt: `docs/verification/kurgan-0.9.6/installation-receipt.json`. Остальные профили/миры не изменены.

Документация алгоритма, комнат, ID/рецептов, loot и аудита иконок: `docs/kurgan/*0.9.6.md`. Далее сохранено историческое состояние 0.9.5.

# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Слой навигации реализован по последнему
пакету V2. Предыдущий перенос 0.9.4 завершён; его отчёты сохранены как историческая база.

Core/server: DONE по headless проверкам. Client screen/HUD/Xaero: COMPILES, ручная QA не выполнена.
Build и финальные loader/data checks PASS. Minecraft-клиент не запускался.
Production: `build/libs/slavicmyths-0.9.5.jar`. Точный hash: `packaging/test-pack-lock.json`.

[Навигация, алгоритм и фактические ограничения](NAVIGATION_0.9.5.md),
[ручные проверки](MANUAL_QA_0.9.5_NAVIGATION.md), [evidence](verification/navigation-0.9.5/acceptance.json).

Восемь категорий, семь сохраняемых настроек, одна основная цель, клубок только к
размещённой избушке, активированные stones и крупные найденные структуры, private SearchArea,
quest lifecycle и optional Xaero group готовы к ручной QA. Статистика: 1000 зон,
0 outside, 0% inner25, 75.7% outer50–85, 13.3% edge85–95.
Тестовый профиль обновлён на 0.9.5 после gates; семь JAR проверены по hash.

## Исторические записи до 0.9.5

# Slavic Myths 0.9.4 — текущий перенос

Launcher fix: исправлены несовпадающие official PolyMC/Maven checksums NeoForge installer/universal локальным patch нового профиля. Реальный ForgeWrapper detector PASS; ручной повторный запуск ещё не проверен.

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Все production sources компилируются; clean build, headless logic/resource/loader gates A–F PASS. Отдельный PolyMC Slavic-Myths-1.21.1-Testing установлен с семью проверенными JAR. Старый 1.16.5 профиль/миры сохранены. Клиент не запускался; игровая QA ещё не выполнена.

Фактическое состояние и журналы: [PORT_STATUS_0.9.4.md](port/PORT_STATUS_0.9.4.md). Исторические записи ниже описывают предыдущие этапы и версии, а не подтверждение работоспособности текущего порта.

Current: 385 items, 153 blocks, 47 entity types (six helper registrations verified in the legacy archive); all public IDs retained. Six exact companion JARs acquired, SHA256 in packaging/test-pack-lock.json. No client launch/commit/push.

# 0.9.4 — перенос на NeoForge 1.21.1 (В РАБОТЕ)

Старое развитие 1.16.5 остановлено по новому пакету пользователя.
Активный build: Java 21 / NeoForge 21.1.255 / ModDevGradle 2.0.148 / Gradle 9.2.1.
Curios 9.5.1+1.21.1 и необязательный JEI 19.57.0.450 разрешаются из Maven.
Есть полный source checkpoint с исправлениями Яги; старый PolyMC не изменён.
Inventory: 385 предметов, 153 блока, 41 entity type; ID mapping не означает runtime PASS.
Перенесены data paths и JSON schemas; 660 data-файлов и 547 бинарных assets проверены.
Перенесены независимые Kurgan/Hunt NBT modules и три SavedData factories;
основной мод ещё не компилируется из-за старых API в остальных системах.
Полный план/блокеры/команды: port/PORT_STATUS_0.9.4.md.
Production 0.9.4 JAR и runtime QA пока отсутствуют; Minecraft запусков 0.

---

# Исправление генерации Яги 0.9.3

Устранён отказ superflat Y=3 из-за нижнего порога Y>4; production YagaPlacement
читает поверхность до Y=0, атомарно размещает блоки с flags18, восстанавливает
neighbor shapes только после полной постройки и проверяет опоры/откат.
Добавлены точные причины отказа, восемь близких кандидатов generate, понятный
planned locate без ложной высоты Y=0 и `baba_yaga_spawn_egg`.
Яга из яйца обслуживает игрока без подмены хозяйки/anchor, сохраняет NBT происхождения,
не выдаёт find_the_hut и не изменяет intro дома. Котёл остаётся сервисом избушки.

verifyYagaPlacement проверяет реальный prepare/commit на vanilla BlockState/тегах:
Y=3/4/63, Y=0 scan, полный дом, отсутствие раннего разрушения декора,
граничные отказы и полный откат. Minecraft client/server запусков 0.
Результаты final build/ресурсов и установка: verification/yaga-placement-hotfix/,
verification/yaga-install-0.9.3.json. Версия/registry IDs прежнего контента сохраняются.

---

# 0.9.3 — Баба-яга: код и headless проверки

Реализованы: уникальный сервисный NPC 40 HP / .23 speed, собственная 79-part модель,
сохранённая избушка 11×13 с нативными суставчатыми лапами, окружением и интерьером,
scripted intro, 4 вкладки, два закрываемых `?`, независимый прогресс игроков,
3 последовательных задания, 14-позиционная очередь 6 контрактов, 6 обменов,
6 рецептов котла, 8 сервисных предметов + яйцо Яги, 4 world-only блока, 10 semantic vanilla sound fallbacks,
RU/EN, JEI и 6 достижений. Уникальные трофеи Лихо/Тугарина не расходуются.
Очищение не удаляет курганное проклятие. Метла/ступа сохраняют старые IDs.

clean build / verifyHunt / verifyKurgan / verifyYaga и проверки production ресурсов
выполняются headless; отчёт последней установки — verification/yaga-install-0.9.3.json.
Minecraft запусков 0: GUI/AI/JEI/реальный logout/внешний вид в игре не проверены.
Ручной QA: MANUAL_QA_0.9.3.md. Архив промежуточной 0.9.2 data-checkpoint сохраняется
как история и не описывает установленную итоговую сборку.


---

# 0.9.2 — Боссы мира (headless реализация)

Реализованы две сущности/яйца, региональные сохранённые anchors, ритуалы,
AI/фазы, boss bars, эффект Дурная доля, 11 предметов, два Curios аксессуара,
четыре рецепта/достижения, loot и поиск целей 192/128. Рог сохраняет 4 цели.
Shared DashFacing исправляет поворот до рывка у новых боссов и прежней охоты.
Native модели/512px atlases/предметы созданы отдельными scoped генераторами.
Полная информация — README и docs/MANUAL_QA_0.9.2.md.

Headless build, verifyHunt, verifyKurgan и verify_resources: PASS.
Minecraft launches: 0; игровой AI/Curios/JEI/визуал/звуки — TODO.
Сравнение пяти геометрических ракурсов и поз не заменяет игровой рендер.
Полное соответствие reference V2 в Minecraft пока не подтверждено.
0.9.3 не начато, игровые проверки не выдаются за выполненные.

---

# 0.9.1 — Охота на нечисть II

Расширены HuntMob/HuntRecords/HuntItems/HuntSpawns: четыре фиксированные цели,
миграция старых boolean-сохранений без потери ACTIVE/UUID/кулдауна.
Огненный змей 155 HP: управляемый полёт/укус/пролёт/след/пикирование/две
конечные проекции, вода/дождь/препятствия. Подвей 160 HP: наземная навигация,
боковое перемещение/коготь/воронка/отклонение обычных стрел/порыв/рывок.
Восемь предметов, защита от падения в Curios necklace с player cooldown,
активный недамажащий ветровой толчок. Сосуды намеренно инертны.
512px сущности/сложные предметы, 256px трофеи; native models/animations,
проверенные vanilla sound fallbacks. 4 точных рецепта/JEI, 4 достижения, RU/EN.

Headless clean build/reobf/verifyHunt/verifyKurgan PASS; ресурсы проверены.
PolyMC Slavic-Myths-Testing обновлён: один JAR 0.9.1, SHA256 совпадает с build;
0.9.0 архивирован, JEI/Curios сохранены. Отчёт: verification/install-0.9.1.json.
Minecraft запусков 0; игровой QA TODO: MANUAL_QA_0.9.1.md.
0.9.2–0.9.4 не реализованы, пользовательское оружие/миры сохранены, commit нет.

---

# 0.9.0 — Охота на нечисть I

Овинник обновлён на 175 HP с сохранением ID/Home/guardian интеграции, Волколак
добавлен на 165 HP. Реализованы замахи, slam/ash/trail/ember, ярость/вода/дождь,
pounce/combo/howl/flank/dodge, кровь/луна/серебро. Собственные модели 256px,
десять предметов 256/512px, объёмный рог, видимые аксессуары в Curios.

HuntRecords сохраняет owner/target UUID, anchor, состояние и 6000-tick cooldown;
выгрузка не завершает ACTIVE. Один призыв на игрока, безопасный ограниченный
поиск, редкие natural spawn placements. Детектор по ПКМ, оберег/пояс только
экипированы, мешочек 9 слотов со slot/swap/tag защитой, сосуд осознанно инертен.
Пять точных JSON recipes для штатного JEI crafting, четыре advancements,
RU/EN и проверенные vanilla sounds. Новые овины не создают гарантированного
Овинника; существующие существа обновляют старые saved attributes при загрузке.

Результаты: verification/headless-0.9.0.json и install-0.9.0.json.
Java8 clean build / verifyHunt / verifyKurgan / resources / production reobf PASS.
PolyMC Slavic-Myths-Testing обновлён до 0.9.0: один JAR, SHA256 совпадает;
0.8.6 архивирован, JEI/Curios сохранены. Подробности в этих отчётах.
Minecraft запусков 0, игровой бой/GUI/JEI/перезаход/MSPT не тестировались.
Пользовательское оружие сохранено, commit не выполнялся.

---

# 0.8.6 — Существа курганов

Реализованы Upyr, Nav, Druzhinnik, Voevoda (150 HP), Volkhv (150 HP), Prince
(350 HP / три фазы), шесть яиц, девять наград, собственная геометрия/анимации,
256px атласы и 32px предметы, 47 семантических vanilla fallback-звуков.
Встречи конечны и сохраняются вместе с UUID участников; выгрузка не вызывает
респавн. При 75+ открывается печать Великого кургана. Победа снимает проклятие
исходного кургана у онлайн/офлайн игроков, сохраняя другие источники.

Финальный Java8 clean build / reobf / headless / ресурсы PASS.
PolyMC Slavic-Myths-Testing обновлён до 0.8.6: один JAR, SHA256 совпадает;
0.8.5 архивирован, JEI/Curios сохранены. Финальный отчёт:
verification/headless-0.8.6.json. Проверяются 1200 планов, 900 составов комнат,
NBT/миграция/жизненный цикл встреч, ресурсы, реальные vanilla sound IDs и
сохранность пользовательского оружия. Minecraft запусков 0; runtime QA TODO.
Текущий JAR и установка: verification/install-0.8.6.json.

Главный босс перенесён в эту версию согласно новому ТЗ; старое ограничение
0.8.5 и план 0.8.7 больше не описывают текущую реализацию.

---

# 0.8.5 — Курганные лабиринты

Реализованы 17 блоков и 64px ресурсы, проверяемый процедурный план трёх типов,
закрытый восьмиугольный нижний зал, три ниши, саркофаги, девять loot-профилей,
сохранение экземпляров и событий нарушения покоя, временное/постоянное проклятие,
административные locate/generate/info/clear. Registry ID 0.8.4 сохранены.
Старые структуры не перестраиваются. Четыре пользовательские модели оружия и
их 256px текстуры сохранены; глобальный генератор старых ресурсов не запускался.

Технические проверки: clean build, финальный build, 1200 планов/NBT и ресурсы PASS.
PolyMC Slavic-Myths-Testing обновлён до 0.8.5: один JAR, SHA256 совпадает.
Отчёты: verification/headless-0.8.5.json и verification/install-0.8.5.json. Minecraft запусков 0, ручной QA TODO.
Боссы/враги/обычное лечение Великого проклятия не реализованы по заданию.

---

> 0.8.4: clean build, ресурсы и тест геометрии PASS; PolyMC Slavic-Myths-Testing обновлён, один JAR, SHA256 проверен. Minecraft manual QA — TODO; игра не запускалась.

## 0.8.4 — Погребальные древности

Добавлены двухблочная Погребальная колода на 5 слотов с поднимающейся крышкой,
четырьмя вариантами останков и собственными звуками; археологические находки,
древний/восстановленный каролингский меч, чекан и копьё. Восстановление — на
существующем Столе оружейника. Базовый чекан имеет обычный survival-рецепт.
Лунница, гривна и оберег от нежити используют существующие слоты Curios.
Древняя монета переиспользуется, нового аналога нет.

Потемневшая древесина: бревно, обтёсанное бревно, доски, ступени, плита, забор,
калитка, дверь и люк. Бревно/доски на высоте 1–5 блоков прямо над горящим костром
обрабатываются 1–4 минуты загруженного времени. Между ними нужен свободный дымовой
столб. При погасшем огне или выгрузке чанка прогресс приостанавливается и сохраняется.
Поставленная игроком колода — обычное хранилище, без курганных последствий.

В новых чанках равнин и берёзового леса появляются внешние прототипы курганов:
малый — около 10 блоков высотой, воинский — 18, великий — 30. Есть старые дорожки,
деревья существующих пород, метки, вход с небольшой нишей и естественная колода.
Полноценные dungeon, нежить, боссы, проклятия, обряды и новые статьи книги не добавлены.
Поиск: `/slavicmyths locate kurgan small|warrior|great [next]`.
Осмотр: `/slavicmyths dev tp_kurgan small|warrior|great [next]` (operator/cheats).

Minecraft не запускался. Внешний вид, частота, открытие/ломание колоды, дым,
сохранение и multiplayer требуют ручной проверки: `docs/MANUAL_TEST_0.8.4.md`.

Ниже — история предыдущих итераций.

# Текущий статус — 0.8.2

## 0.8.2 — Соловей-разбойник

Соловей встречается на помосте у старой сосны в новых больших разбойничьих станах.
Отдельная широкоплечая модель, волосы и борода, кинжал и ножны, дыхание, замахи,
три вида свиста и падение перед появлением наград. В бою помогают толстые стволы
и камни; забор не служит надёжным укрытием. Живые защитники двора могут вмешаться.
Разрушительный свист затрагивает только разрешённую слабую растительность;
постройки и контейнеры не входят в список. Бесконечных подкреплений нет.

Награды: Знак Соловья, Кинжал Соловья и редкая Прядь (28%). Из пряди, серебра,
кости и янтаря создаётся Свисток Соловья. На помосте отдельный сундук с награбленным
и Разбойничьим рогом. Встреча открывает запись книги, победа — достижение.
Убитый Соловей конкретного стана не восстанавливается при обычном перезаходе.

**Старые полностью сгенерированные дворы 0.8.1 автоматически не обновляются.**
Ищите новый стан в новых чанках. Опасная миграция сохранений не выполняется.
Частично сгенерированные структуры могут получить маркер при генерации нового чанка.

Команды с правами оператора:
- `/slavicmyths dev tp_nightingale` — поиск большого стана и телепорт к двору;
- `/slavicmyths dev spawn_nightingale` — отдельный тестовый босс перед игроком.

Minecraft не запускался: **0 запусков**. Внешний вид, звук, проходимость, баланс,
перезаход и multiplayer проверяет пользователь по `docs/CHECKLIST_0.8.2.md`.

Техническая проверка: clean build/reobf Java8 PASS; общий verifier PASS
(285 items, 122 blocks, 253 PNG, 188 recipes, 76 visible advancements),
verify_nightingale_082 / verify_stronghold_081 / WindGeometryTest PASS.
PolyMC Slavic-Myths-Testing обновлён: один 0.8.2 JAR, SHA256 совпадает;
старый JAR в mod-backups. Другие моды не изменены. Minecraft запусков 0.

# Текущий статус проекта

## 0.8.1 — Большой разбойничий стан

Реализация завершена, ожидает ручного игрового QA. Добавлены большой стан III уровня,
23 шаблона, три семейства периметра/расположения (forest/plains/hill), варианты
казармы/кузницы/склада, кухня, конюшня, тюрьма, дом и отдельная командирская зона.
Два Атамана (68/64 HP), 26 основных защитников; патруль/посты/тренировки/кузнец,
тревога CALM→SUSPICIOUS→ALERT с передачей через пять разрушаемых сигналов.
Сохранение состава и CLEARED: оба Атамана + ≥18 из 26 защитников; respawn отсутствует.
21 мебельный блок: сосна/липа, посадка на стул/табурет/лавку, стойка с предметом,
шкаф из двух блоков, полка, тумба, мешок/ящик/дрова/манекен и сигнал.
Тайник с шансом 35% и вероятностным лутом; задний двор 33×31 с крупным деревом,
пустым помостом и укрытиями. Перо открывает скрытую статью. Соловей НЕ реализован.

Compile / clean build / final build / reobf Java 8: PASS.
Ресурсы: PASS — 280 предметов, 122 блока, 245 PNG, 187 рецептов, 75 достижений.
Отдельная проверка 0.8.1: 23 NBT, варианты состава/маркеры/опоры лестниц,
мебель/loot/вероятности/воспроизводимость/JAR equality PASS.
Minecraft запусков **0**, как требовалось. Проходимость, внешний вид, AI, посадка,
синхронизация стойки, перезаход и реальная частота не проверены в игре; MSPT не измерен.
Подробности: CHECKLIST_0.8.1.md и CONTINUE_0.8.1.md. 0.8.2 не начата.
Дальше — история предыдущих итераций.

## 0.8.0.1 — Woodlands & Timber

Реализованы липа, рябина, ива и сосна: четыре разных генератора с тремя вариантами
формы, собственными 16×16 текстурами и полными древесными наборами. 69 новых блоков,
66 предметов: включая настенные таблички без отдельных предметов, ягоды и свисающую
листву ивы. ПКМ топором снимает кору; саженцы растут, поддерживают костную муку;
листва использует vanilla distance/persistent/decay, урожай рябины восстанавливается
random tick рядом с древесиной. Добавлены рецепты, теги, топливо, горение, два
достижения, статья книги и `/slavicmyths dev growtree <linden|rowan|willow|pine> [random]`.

Worldgen дополняет vanilla forest/dark forest, plains, taiga, swamp и river;
ива требует воду в пределах локальной проверки. Старые структуры не менялись.
Clean build и reobf: PASS. Общая проверка ресурсов: PASS — 258 предметов, 101 блок,
241 PNG, 166 рецептов, 72 видимых достижения. Проверка 0.8.0.1: PASS — рецепты,
теги, варианты моделей, loot, сигнальные текстуры, воспроизводимость и JAR equality.
Pure Java геометрия: 256 seed × 4 породы; связность ветвей и опоры листвы PASS.
Это не игровой тест. Minecraft запусков **0**, по заданию пользователя. Внешний вид,
рост/костная мука, фактическая частота, таблички, ягоды и decay проверяет пользователь.
MSPT и время worldgen не измерялись. Следующий шаг: CHECKLIST_0.8.0.1.md.
PolyMC Slavic-Myths-0.2.0 обновлён: один JAR 0.8.0.1, SHA256 совпадает с production.
Предыдущий 0.8.0 сохранён вне mods; миры и другие моды не изменены.
Отчёт установки: verification/install-0.8.0.1.txt.
0.8.1 не начата. Ниже сохранена история предыдущих версий.

## 0.8.0 — Combat & Bandits

Реализация завершена; игровой QA ожидает пользователя. Оружейный стол с серверным
меню 4×4 и 12 JSON-рецептами; четыре булавы, кистень, железные кольца для vanilla
кольчуги, стёганка и пластинчатый нагрудник. Пять человеческих ролей разбойников,
собственные модели/текстуры/звуки, малый и средний лагеря, Атаман, сохранение состава,
HUD, лут, достижения, пять статей книги, optional JEI и команды поиска/телепортации.

`gradlew.bat --offline clean build`: PASS, production/reobf Java 8 JAR.
Общая проверка: PASS — 192 предмета, 32 блока, 186 PNG, 118 рецептов, 70 достижений.
Проверка 0.8.0: PASS — 12 рецептов, 10 NBT, составы 5/9, один Атаман,
проходимость маркеров, loot references, различные геометрии оружия, побайтовая
воспроизводимость ресурсов (включая OGG), равенство ресурсов в production JAR.
Логи: docs/verification/build-0.8.0.log и resources-0.8.0.log.
Реальных запусков Minecraft: **0**, по заданию. Бой, внешний вид, shift-click,
перезаход, JEI и частота worldgen в игре не проверены; MSPT не измерялся.
Чеклист и команды: CHECKLIST_0.8.0.md. Рецепты: ARMORER_RECIPES_0.8.0.md.
PolyMC Slavic-Myths-0.2.0 обновлён: один JAR 0.8.0, SHA256 совпадает с build/libs.
Предыдущий JAR сохранён вне mods, Curios сохранён, миры не изменены.
Отчёт: verification/install-0.8.0.txt.
0.8.1 не начата. Следующие разделы — история предыдущих версий.

## 0.7.3 — Топи и затопленные руины

Реализация завершена, ожидает ручного теста пользователя. Добавлены стандартные
StructureStart с 23 собственными шаблонами: три избы, два состава поселения, гать,
два святилища, два стана, три подводных прототипа и четыре малых POI. Рельеф проверяется
при генерации; дома имеют отдельные фундаменты, пути следуют местной высоте. Нет
нового тикового поиска или ретрогенерации. Команды `/slavicmyths dev swamp` и
`/slavicmyths dev structure <id> [next]`, семь loot tables, шесть статей книги,
восемь exploration-достижений. Болотник/материалы отсутствующей 0.7.1 не добавлялись.

`--offline clean build`: PASS. Общая проверка ресурсов: PASS (179 предметов, 31 блок,
164 PNG, 106 рецептов, 65 достижений). Проверка NBT/повторяемости/loot: PASS.
Production/reobf JAR: `build/libs/slavicmyths-0.7.3.jar`, Java 8; тестового кода нет.
Игровых запусков: **0** по заданию. Внешний вид, частота, поиск, terrain integration,
loot и встречи не проверены в игре; MSPT не измерялся.
PolyMC: обновлён фактически существующий `Slavic-Myths-0.2.0`, один JAR мода,
добавлен отсутствовавший обязательный Curios из локальной зависимости. Старый JAR
сохранён вне mods, миры не изменены. Полные пути и ограничения: CONTINUE_0.7.3.md.

Краткая ручная проверка: новое/другое болото; шесть ID и next; варианты и крыши;
затопленные входы/тайники; книга/достижения; сохранение после перезахода.
0.7.4 не начата. Нижние разделы — история предыдущих проходов.

## 0.7.2 — Хозяева глубин

Основной контент реализован; общий STATUS: PARTIAL из-за отсутствующего в этой копии
проекта контента 0.7.1. Болотник/трясины/материалы 0.7.1 не регистрируются заново.

- [x] Progression clues — отношения с Водяным, редкая речная/болотная рыбалка, направление знакомого миру Омута
- [x] Elder Vodyanoy model
- [x] Elder Vodyanoy textures
- [x] Elder Vodyanoy animations
- [x] Elder Vodyanoy sounds
- [x] Encounter arena
- [x] Encounter activation
- [x] Boss bar
- [x] Phase I
- [x] Phase II
- [x] Phase III
- [x] Arena water mechanic
- [x] Defeated-state persistence
- [x] Pearl of the Pool
- [x] Pool Spear
- [x] Water thrust
- [x] Depth Amulet
- [x] Vodyanoy Net
- [x] Loot
- [x] Book of Tales
- [x] Advancements
- [x] Recipes/JEI
- [x] Resource verification
- [x] Production JAR
- [x] PolyMC
- [ ] Интеграция с отсутствующим контентом 0.7.1

Технически проверены clean build, JSON, PNG/OGG, Java 8, production/reobf JAR и модели
новых предметов. Игровых запусков: 0. Persistence/бой/волны/вода/навигация требуют ручного
теста в Minecraft, в том числе multiplayer и перезаход во время боя/после победы.
Подробности следующего шага: CONTINUE_0.7.2.md. 0.7.3 не начата.
Slavic-Myths-Testing обновлён до 0.7.2: один JAR, SHA-256 совпадает с production,
Curios/JEI не изменены; предыдущая сборка сохранена вне mods.

## 0.7.0 — Тихие воды

- [x] Fishing overhaul
- [x] Fishing nets
- [x] Pike
- [x] Carp
- [x] Crayfish
- [x] Water food
- [x] Aquatic plants
- [x] Water resources
- [x] Vodyanoy
- [x] Rusalka
- [x] Rusalka song
- [x] Water POI
- [x] Sounds
- [x] Book of Tales
- [x] Advancements
- [x] Recipes/JEI
- [x] Production build
- [x] PolyMC

Реализация 0.7.0 завершена; `--offline clean build` и resource verifier — PASS:
172 предмета, 30 блоков, 163 PNG, 103 рецепта, 52 достижения; JSON/OGG, Java 8 и reobf JAR.
Игровых запусков: 0. Ручная проверка: рыбные силуэты/движения, сети и сохранение улова,
Водяной/подношения/вылов, песня и сопротивление, кухня/JEI, растения и новые POI.
Вокал Русалки — собственная бессловесная формантная запись; звучание оценивает пользователь.
Новая крупная болотная арка и 0.7.1 не начаты.
PolyMC Slavic-Myths-Testing: установлен единственный slavicmyths-0.7.0.jar,
SHA-256 совпадает с production; Curios/JEI не изменены, предыдущая сборка в mod-backups.


## Artifact content drop — 0.6.5

- [x] Гусли-самогуды: удержание, музыка, постепенное успокоение, grace/cooldown, модель/поза.
- [x] Скатерть-самобранка: шесть общих порций, визуальное убывание еды, складывание, NBT cooldown.
- [x] Бадняк: трёхсекундное сжигание, расход, временное благословение, дым/угольки.
- [x] Посох Велеса: один временный зверь, владелец/союзники, срок, cooldown и прочность.
- [x] Оберег смерти: прежний ID retribution_charm, одноразовое спасение и ответный урон источнику.
- [ ] Дополнительные резной сундук, столб и утварь (необязательные).

`--offline clean build` и resource verifier — PASS: 153 предмета, 26 блоков, 93 рецепта,
46 достижений; production/reobf JAR, Java 8 и декодирование OGG проверены.
PolyMC `Slavic-Myths-Testing` обновлён: единственный JAR Slavic Myths совпадает с production
по SHA-256; Curios и JEI не изменены, прежняя сборка сохранена в mod-backups.
Игровых запусков: 0. Дополнительный декор пропущен; новые четыре артефакта пока Creative/commands.
Ручная проверка: удержание/остановка музыки, позы, поведение успокоенных мобов, расход порций
в multiplayer, таймер и складывание Скатерти, призыв/исчезновение зверя, спасение Оберегом.


## 0.6.5 — Finalization Patch (текущий проход)

Закрыты кусты малины/черники (рост, сбор, повторное созревание, лесные группы), кухня
с 11 рецептами и инструментами, Метла/Ступа/Пест, серверное движение, груз Ступы на 9 слотов
и «Попутный ветер» I–III. Существующие Curios-аксессуары сохранены без дублей;
у шапки ограничены длительность невидимости и частота активации.
Сбор из листвы заменён кустами. Новые модели — собственная JSON-геометрия с vanilla-материалами.
Minecraft и PolyMC: 0 запусков; PolyMC не изменён по актуальному указанию пользователя.
Ручная проверка: рост/сбор ягод, кухня, аксессуары, посадка и полёт, Пест, груз после
перезахода и переноса в предмет, first/third person, столкновения, звук и частицы.
Исторические записи ниже описывают предыдущие проходы. Версия 0.7.0 не начата.

Итог: `--offline clean build` и `tools/verify_resources.py` — PASS (149 предметов,
25 блоков, 93 рецепта, 42 достижения; JSON, PNG, OGG, Java 8 и production/reobf JAR).
JAR: `D:\slavic-myths\build\libs\slavicmyths-0.6.5.jar`. Игровые сценарии не проверялись.

## 0.6.5 — Wildlife & Cooking

Добавлены шесть wildlife entity type: бурый медведь, медвежонок, лесной волк,
кабан, олень и олениха. У видов отдельные размеры, модели, текстуры, параметры,
походки, плавание, боевые позы, AI, лесной/равнинный спавн и loot. Добавлены три
вида сырого/приготовленного мяса, крупная кость, костяная стрела, мука, две ягоды,
четыре вида блинов, каравай, пирог, печёное яблоко и пять похлёбок с рецептами.
До полноценной реализации кустов ягоды редко выпадают из листвы при обычной рубке;
ножницы и Шёлковое касание исключены, обработчик срабатывает только при ломании блока.
Вторичные showcase-предметы, ягодные кусты и кухонные станции перенесены без
placeholder-реализаций. Игровых запусков Codex: 0; ручная проверка обязательна.
`gradlew.bat --offline clean build` — PASS; `verify_resources.py` — PASS:
136 регистраций предметов, 22 блока, 148 PNG, 83 рецепта, 34 достижения,
production/reobf JAR и Java 8 проверены.
JAR установлен в PolyMC instance `Slavic-Myths-0.2.0`; оставлена ровно одна версия
Slavic Myths, SHA-256 установленной копии совпадает с `build/libs`.

## 0.6.0 — Пути, руны и перековка

Четыре пути, 24 навыка, основной/побочный выбор за vanilla levels и предметы.
Старшие навыки требуют открытий; побочный ограничен первыми тремя навыками.
Прогресс — отдельный compound в Forge PlayerPersisted, с копированием при Clone.
Камень Пути и Рунная наковальня имеют серверные меню; активная способность — G.
Восемь рун получаются существующими обрядами (JEI и Книга сказаний обновлены).
Перековка 0–3 гнёзд, установка/удаление без замены ItemStack и потери зачарований;
ванильные мечи, топоры, луки, щиты поддержаны. Малое святилище — редкий feature
5×5 в новых равнинных/лесных чанках; доступны также рецепты обоих блоков.
13 NBT contract checks — PASS: roundtrip, независимость копии, сохранность чужих
данных и зачарований, атомарный отказ некорректных гнёзд. Новые JSON, texture refs,
локализация, рецепты и tags проверены. Minecraft: 0 запусков по требованию.
`gradlew.bat --offline clean build` — PASS; production Java 8/reobf JAR проверен.
PolyMC instance `Slavic-Myths-0.2.0` обновлён до 0.6.0: ровно один JAR мода, SHA-256 совпал
с build/libs и выдаваемой копией; остальные моды сверены по хешам и сохранены.
Игровой persistence, меню, PvP/несколько игроков и баланс ещё не проверены в игре.
Ручная проверка: выбор/XP → покупка → смерть/перезаход → зачарованный железный меч
в наковальне → гнездо/руна/удаление → лук/щит/G → второй игрок с другим путём.

## 0.5.1 — Entity Visual & Audio Hotfix

Переработаны иерархические модели Полудницы и Полевика; доработаны поза,
кисти, борода Банника и птичьи конечности/одежда Кикиморы. Обновлены 8 текстур,
анимации четырёх существ и 25 собственных синтезированных OGG (11 новых событий).
`gradlew.bat --offline clean build` — PASS, включая reobfJar. Проверены изменённые
PNG/OGG, пути звуков/субтитры, Java 8 и соответствие ресурсов итоговому JAR.
Сравнение с 0.5.0 подтвердило неизменность Игоши, Овинника, предметов, блоков,
рецептов, loot и spawn-классов. Minecraft не запускался (0 запусков).
Статическая проверка силуэтов выполнена; игровой вид, звук и переход формы
требуют ручной проверки. PolyMC: существующий Slavic-Myths-Testing, версия 0.5.1.

## 0.5.0 — «Хозяева земли»

Добавлены Кикимора (4 одежды/2 убора), Полудница с превращением, Полевик
с персональными дарами, Банник с паром/камнем, Игоша с мирным исходом,
Овинник с предупреждениями, boss bar, тяжёлой лапой, рывком и второй фазой.
Собственные cuboid-модели, 10 новых entity textures, 30 оригинальных синтезированных
звуков; 9 предметов, 6 яиц, каменка и кадка. Редкие баня/овин — chunk features
в новых чанках, не locate-структуры. Новые ингредиенты имеют применение; ритуал
Угольного сердца включён в JEI. Кикимора — игровая интерпретация, не исторический канон.
`gradlew.bat --offline clean build` — PASS (обычный запуск остановился на проверке
сертификата Maven Forge; использован готовый локальный кэш без отключения TLS).
`verify_050.py` — PASS: JSON/PNG/OGG, регистрации, актуальность JAR, Java 8/reobf.
PolyMC Slavic-Myths-Testing обновлён, SHA-256 совпадает, остальные моды сохранены.
Minecraft не запускался. Ручная проверка: яйца/новые чанки, полуденный спавн,
переход Полудницы, подношения, защита сундуков, бой с Овинником и перезаход.
Баланс, естественная частота встреч и поведение в игре пока не подтверждены.
Входное задание оборвано в разделе 121; новые достижения не добавлялись.

## 0.4.7 — Weapon Overhaul

12 прежних оружий и 2 щита переведены на отдельные объёмные JSON-модели;
меч/кинжал по длине 1,83, копья и посохи длиннее меча. Forge isShield исправлен,
добавлены blocking predicates/models, ванильные use/block/durability сохранены.
Новые: Громовое копьё (+2 урона раз в 6 с), Булава, Перунитовая булава, Бердыш;
обмотка и серебряная оковка используются в рецептах. Два новых ритуала и JEI.
`clean build`, `reobfJar`, `verify_weapons.py` — PASS; Java 8, production JAR.
PolyMC Slavic-Myths-Testing обновлён, SHA-256 совпадает, прочие моды сохранены.
Minecraft не запускался. Положение в руках и блокирование требуют ручной проверки.
Опциональные серебряный бердыш и точильный камень отложены.

## 0.4.6 — visual polish

Финальный art pass: 27 предметов, 12 блоков (19 текстур, 4 JSON-модели),
2 слоя брони. Новые силуэты оружия, посохов, щитов и подвесок; резьба,
каменные поверхности, ниша очага, идол и свеча. Генератор: generate_046.py
с финальными пиксельными исходниками polish_046.py. У очага и идола отключено
отсечение соседних граней для неполных моделей; игровые характеристики сохранены.
`gradlew.bat clean build` — PASS, `reobfJar` — PASS. Проверены JSON-ссылки,
90 PNG, ресурсы внутри JAR и Java 8 bytecode. Звуковой этап общего проверяющего
скрипта не выполнен: отсутствует Python soundfile; звуки не менялись.
Production JAR: `D:/slavic-myths/build/libs/slavicmyths-0.4.6.jar`.
Установлен в PolyMC `Slavic-Myths-Testing`, имя «Slavic Myths - Test (0.4.6)».
SHA-256 копии совпадает; одна версия мода, JEI сохранён. Восстановлен JDK 8
по настроенному пути instance. Minecraft не запускался, игровых запусков: 0.

## 0.4.5 — Visual & Content Overhaul

Исправление после ручного запуска: Forge завершал `load_registries` с ошибкой
`Unable to have damage AND stack` при создании `retainer_shield`, потому что
`stacksTo(1)` вызывался после `durability(280)`. У обоих щитов удалён лишний
вызов `stacksTo(1)`; прочность уже делает предмет нестакуемым. Это исправление
пока проверено сборкой и статическим аудитом, повторного игрового запуска Codex нет.

74 предметные регистрации, 18 блоков; новых относительно 0.4.1: 20 предметов,
3 блока, 3 обряда, 17 обычных рецептов и 4 видимых достижения. Добавлены янтарь
из сундука капища, пять видов оружия/посохов, два функциональных щита, три новых
оберега, свеча, обрядовый уголь, еда и настенная резьба. Серебряное оружие получает
+1,5 урона по существам тега `silver_vulnerable` (сейчас Леший). Грозовой посох:
импульс 3 урона на расстояние до 7 блоков, cooldown 80 тиков, 240 применений.
Обработчики срабатывают по использованию/урону, не каждый тик. Новых мобов нет.

Перерисованы 37 существовавших предметных текстур, 15 блоковых и 2 слоя брони;
дополнительно растения получили отдельные item sprites. Старые registry ID сохранены.
`tools/generate_045.py` воспроизводит обновлённые assets после прежних генераторов.
`tools/visual_audit.py` проверяет ссылки моделей, размеры, непрозрачность, совпадения
пикселей и силуэтов; контактный лист просмотрен. Статический визуальный аудит — PASS:
80 различных item/block/armor текстур, одинаковых пиксельных файлов 0; одинаковые внешние
контуры у семейства подвесок допустимы, центральные знаки различаются.

Ограничения: щиты используют плоскую item-модель для надёжного vanilla блокирования,
не отдельный 3D renderer; вид на игроке и геймплей пользователь ещё не проверил.
Ступка, кувшины, вышитое полотенце и ритуальный коврик не добавлены, так как в
0.4.1 отсутствовали или требовали отдельной механики. Игровых запусков Codex: 0.
После финальных изменений `gradlew.bat clean build` — PASS, проверка ресурсов и
production/reobf Java 8 JAR — PASS. `visual_audit.py` — PASS; генератор ресурсов
повторён без изменения байтов. `slavicmyths-0.4.5.jar` установлен в подтверждённый
PolyMC instance `Slavic-Myths-0.2.0` (Minecraft 1.16.5, Forge 36.2.42), одна версия
мода в `mods`. Название instance историческое; игру пользователь запустит вручную.


## 0.4.1 — первый проход расширения контента

Добавлены 34 предметные регистрации и девять блоков: комплект инструментов и брони
из перунита, серебряная руда/слиток/самородок и оружие, костяной и обрядовый нож,
четыре дикорастущие травы, переработанные ингредиенты, ткань, мешочек, две еды,
ритуальная чаша и деревянный/соломенный декор. Всего 54 предмета и 15 блоков.
Серебро и травы появляются только в новых чанках; существующий мир не меняется.
Добавлены 28 рецептов и добыча новых ингредиентов в капище.

Добавлены два обряда: топор из перунитового топора, громового камня, Древнего знака
и чаши; оберег Перуна из ткани, громового камня и Древнего знака. Они показаны в JEI.
Громовой топор имеет ограниченный электрический удар без призыва молнии; полный
перунитовый комплект и оберег в левой руке уменьшают урон от молнии.

Границы этого прохода: новые травы пока прежде всего ремесленные ингредиенты;
бонус серебра против духов, напитки, прочие обереги и ступка ещё не реализованы.
Готового полного milestone 0.4.1 этот проход не означает. Модели брони
используют авторскую простую пиксельную раскраску. После последних изменений
`gradlew.bat clean build` — PASS, `tools/verify_resources.py` — PASS (54 предмета,
15 блоков, 60 PNG, 35 рецептов, reobf Java 8 JAR). JAR установлен в подтверждённый
PolyMC instance `Slavic-Myths-0.2.0` с Minecraft 1.16.5 и Forge 36.2.42;
название instance историческое. Minecraft не запускался по просьбе пользователя,
реальных запусков: 0.

## 0.4.0 — Духи, капища и первые ритуалы

Minecraft 1.16.5, Forge 36.2.42, Java 8; mod ID `slavicmyths`.
В начале прохода код уже содержал 0.3.0, хотя статус описывал 0.2.0.
Прежний документ сохранён в verification/status-before-0.4.txt; старые отчёты — история.

### Реализовано

- Сохранены 13 прежних предметных ID, три блока, оба духа, модели и семь оригинальных звуков.
- Домовой: отношения −100…100 по UUID взаимодействовавших игроков, четыре вида даров,
  cooldown 60 секунд, возврат тары, creative, возрастающая обида за удары, отказ и избегание.
  Четыре дара дают дружбу, восемь — преданность.
- Домовой очаг: рецепт, привязка друга в радиусе 8 блоков, хозяин, координаты/измерение,
  persistent и стандартное возвращение домой. NBT сохраняет отношения, дом и cooldown.
  Благословение пустой рукой у действующего очага: Regeneration I, 5 секунд, раз в 10 минут.
- Леший: лесной зов, 25% шанс Slowness I на 3 секунды при ударе; попытка безопасного
  исчезновения при HP ≤12, cooldown 30 секунд, максимум 6 локальных кандидатов.
- Древнее капище 11×11: каменный круг, дерево, костёр, растения, идол, жертвенник и сундук.
  Новые чанки vanilla forest/plains/taiga только в Overworld; попытка 1/96 до проверки площадки.
- Древний идол, жертвенник, древняя монета, осколок идола, Древний знак и Книга сказаний.
- Ритуал: полынь → берестяной свиток → оберег → Древний знак. Состояние в TileEntity NBT;
  подсказки, звук и частицы. Определение ритуала отделено от исполнения.
- Книга с GUI, страницами, пятью открываемыми записями, введением и намёком на будущие пути.
  Знания хранятся стандартными advancements; при открытии один серверный пакет.
- 20 предметов, 6 блоков, 7 обычных рецептов, 24 видимых достижения, единая Creative Tab.
  RU/EN, новые PNG 16×16, модели, blockstates, loot. Vanilla advancements сохраняются.
- Необязательная категория JEI «Ритуалы». JEI не вложен в JAR и не обязателен для запуска.

### Проверки и границы

Чистая сборка, ресурсы, Java 8, reobfuscation и содержимое production JAR проверены.
Dev client реально загружал меню и копию мира 0.3.0, в том числе без JEI.
Игровые assertions проверили подношения, тару, cooldown, сохранение NBT и перезаход,
благословение, полный ритуал, результат, loot, placement капища и открытие GUI сервером.
Снимки GUI, предметных текстур и тестовой сцены просмотрены.
Точные результаты запусков и ограничения — VERIFICATION_0.4.0.md.

Капище — worldgen Feature: `/locate` не поддерживается. Автотест размещает его на
подготовленной площадке; естественную частоту и survival-баланс проверяют вручную.
Dedicated server и два настоящих игрока не проверены. Статический performance-аудит
и единичный замер placement не заменяют массовый MSPT-тест.
Оставшиеся игровые проверки — MANUAL_TEST_0.4.0.md.

### Не реализовано намеренно

Классы, мана, выносливость, заклинания, боги, боссы, измерения, Водяной, Русалка и Баба-яга.
Знак — progression item; монеты и осколки пока не имеют торгового применения.
Нет автоматического распознавания домов, ретрогенерации или встроенного TreeCapitator.

Git-репозитория нет, commit не создавался. Пользовательские миры не перезаписывались.

## Следующий этап

После ручной проверки и баланса 0.4.0 — предварительный этап 0.5.0 «Воды и болота».


## Runtime audit 1.3.4.3 — 2026-10-08

Аудит без изменения production кода/баланса/ресурсов. Clean build успешен; проверены 659 предметов,3018JSON,901PNG и исключение тестов из JAR. Реальных запусков:7 GameTest servers и1 dev client; итоговый37/37 проверяет наблюдаемое поведение, включая дефекты A02 (Enemy predicate пропускает Соловья без lastHurtMob) и A18 (duplicate depth_gift рано открывает lore_keeper). Native capture клиента недоступен; визуальные симптомы, ручной игровой проход, multiplayer/restart и MSPT не подтверждены. Отчёт и предложения: [docs/audit/runtime-1.3.4.3/AUDIT_REPORT.md](audit/runtime-1.3.4.3/AUDIT_REPORT.md). Ничего не исправлено и не отправлено в Git.


## Песиголовцы 1.6.5 — 2026-10-10

По архиву добавлены три роли `slavicmyths:pesigolovets` с тремя яйцами, настоящие копьё/лук/нож/топор, ограниченный loaded-only индекс стаи и естественный спавн. Топор прерывается от4 реально прошедших HP до тика17 согласно ответу пользователя. Новые модели имеют отдельные atlas256, IK рук/ног, 70 оригинальных OGG. Существующие registry ID, руны и мобы сохранены. [Описание](pesigolovtsy/IMPLEMENTATION.md).

После разрешения пользователя выполнено10 реальных запусков Minecraft (7 GameTest-серверов,3 dev-клиента). Последний сервер:7/7 PASS; игровые кадры трёх ролей/атак и настоящее открытие сохранённого QA-мира получены. В ходе проверки исправлены укол по низкой добыче и синхронизация беговой походки. Повторный clean build PASS (1m8s), шесть offline-наборов PASS,25060 resource/JAR checks PASS;6300 прежних записей JAR побайтно сохранены. Непроверенные естественный спавн, two-client MP, stress MSPT, прослушивание и остальная матрица явно остаются открытыми. [Отчёт](verification/pesigolovtsy-1.6.5/REPORT.md). Сборка из snapshot1.6.5 исключает только параллельные добавления Кузи; общий checkout1.6.6 сохранён. Commit не создавался.

Установка1.6.5 выполнена после gates в согласованный PolyMC1.21.1-Testing; backup1.6.4 вне mods,473 прочих файла сохранены. Immutable JAR: `release/slavicmyths-1.6.5.jar`; второй чат уведомлён для последовательной установки1.6.6.


## Кузя и интерьер Яги 1.6.6 — 2026-10-10

Добавлен канонический домашний NPC `slavicmyths:kuzya`: нативная модель/33 клипа, 48 OGG, физический маршрут на печь, бытовые сцены, гостевые таймеры и явный repair. Новые дома получают интерьер/блюда/умеренный loot; старые автоматически не перестраиваются. Песиголовцы 1.6.5 сохранены. [Реализация](kuzya/IMPLEMENTATION.md).

Clean build PASS; 471427 offline checks Кузи, 73926 regression checks песиголовцев, 31058 resource/JAR checks PASS. Реальных запусков этой задачи: 10 GameTest-серверов, 0 клиентов; финальный интеграционный сценарий PASS. Кандидат установлен после gates в существующий PolyMC1.21.1-Testing, backup1.6.5 вне mods, 474 прочих файла сохранены. Commit не создавался.

Открыты клиентская визуальная/звуковая приёмка, полный длительный бытовой прогон, two-client MP, настоящий перезаход и MSPT. Четыре ориентации не реализованы: сохранена северная генерация. Эти пункты не считать выполненными. [Отчёт](verification/kuzya-1.6.6/REPORT.md).


## Доработка Кузи 1.6.7 — 2026-10-11

По пользовательским скриншотам убран контрастный пиксельный шум модели, исправлен UTF-8 импорт русских реплик. Вместо TTS подключены пять выбранных оригинальных мультфрагментов; 20 старых sound registry ID сохранены как aliases, синтетические файлы исключены из production. Таймер домашнего занятия независим от клипа/гостевых реакций; первое занятие8s, паузы15–45s. Интерьер оставлен принятым пользователем.

Clean build PASS; 471397 offline checks Кузи, 73926 regression checks песиголовцев, 31106 resource/JAR checks PASS; 3 новых GameTest-сервера (финальный autonomous + ПКМ + физическая печь PASS), 0 клиентов. Кандидат установлен после gates в PolyMC1.21.1-Testing, backup1.6.6 вне mods, 484 других файла сохранены. Миры и остальные системы сохранены. Commit не создавался.

Человеческое прослушивание/игровой микс, client visual, длительный бытовой прогон, MP/relog/MSPT остаются открыты. Локальный ASR не приравнивается к прослушиванию. [Отчёт и оригинальные аудиофайлы](verification/kuzya-1.6.7/REPORT.md).


## 1.6.8 — Kuzya autonomy and Russian entity name (2026-10-11)

The actual entity-name/SFX/repair localization was CP1251 mojibake in the resource generator; fixed UTF-8 and checked all owned Russian keys in the packaged JAR. First activity20–60 ticks, rest60–120 ticks (scheduler1Hz). Four fixed work anchors; table/stove scenes preserved. Scene schema2 saves current activity and route, resumes loaded movement and accounts elapsed world time for rest without forced chunks. Legacy residents activate promptly.

Clean build PASS; 471397 Kuzya offline and 73926 doghead regression checks; 28007 resource/JAR checks PASS. Four dedicated server launches (first two failed, last two passed), zero clients. Final2300-tick GameTest: physical stove ascent/rest/descent, autonomous chores at two positions, scene/route NBT restoration PASS. Real relog, client visuals and measured MSPT remain untested. Installed1.6.8 after gates, backup1.6.7, 486 other files preserved. [Report](verification/kuzya-1.6.8/REPORT.md).

## Баюн и Книга сказаний — кандидат 1.6.9 (2026-10-11)

Реализован утверждённый пакет поверх 1.6.8: постоянный Кот Баюн, персональные подарки/доверие/диалог, сон и уход, предупреждение → песня → притяжение → физический прыжок с отменой за стеной/при уклонении. Прежний lore_book теперь даёт однократную книгу, восемь разделов/три уровня оглавления, поиск/историю/реальные рецепты/широкие ориентиры/настоящее дерево достижений. 217 записей, 1666 строк registry coverage, 58 индивидуальных RU creature profiles. 162 старых advancement IDs/criteria/rewards сохранены; четыре новых события и девять заголовков без XP. Яга читается без изменения quest state/rewards. Четыре прежних испорченных имени подземников/Кладовика исправлены. Модель/голоса/автономность Кузи не изменены.

Clean build PASS; Bayun364871/Kuzya471397/doghead73926 native checks, 39486 resource/JAR gates, 19 decoded original prototype OGG PASS. Последний dedicated GameTest15/15 PASS. Настоящий disk-world restart сохранил UUID Баюна, trust25, книгу/знания и vanilla advancement progress; это не client relog. Всего19 dedicated запусков (6 ранних FAIL исправлены, 13 PASS), 0 клиентов. Snapshot codec229697 bytes; 100 server snapshot mean2.146881ms — не GUI latency/MSPT.

Кандидат установлен в прежний PolyMC1.21.1-Testing после gates: backup1.6.8 вне mods, 559 прочих файлов сохранены; старый1.16.5 не затронут. Production `build/libs/slavicmyths-1.6.9.jar`, копия `release/slavicmyths-1.6.9.jar`. SHA256 `56e73c2e6581bfa795c1ce81531ced65b8e9d163641c95bf94e49cb6fba84643`. **Финальная приёмка НЕ завершена:** Russian GUI screenshots/ingame animation/audio/controller/natural seed/реальный2-client MP/Yaga walkthrough NOT_RUN, MSPT/FPS NOT_MEASURED. Клиент не запускался по отказу пользователя; commit/push не делались. [Отчёт и матрица](verification/bayun-book-final/REPORT.md).
Validation: Java21 clean build PASS (1m17s), final resource rebuild PASS (1m18s); voice source/audio/JAR parity 6962 checks PASS; production JAR SHA256 f40cc9c7f2de752b27afec63ff94001bcaa191367454442e572f9068729702d5. Runtime launches: 0.

Final validation: Java21 clean build PASS1m18s; voice gates6988 PASS, existing audio gates7733 PASS. JAR SHA256 6a1432c6ca7da5166a9142bec111299143f7860837df3866e8f8eefbbf1bdafa. Runtime0; NO_COMMIT; PolyMC unchanged.

Batch validation: Java21 clean build PASS1m49s; scoped batch248 checks PASS, voice/resource/JAR7019 PASS, existing audio7733 PASS. JAR SHA256 1dd5273b80cc7a9ee6956a4bcf225f4d7923010915afd5ab2d046355ff13f121. 17 new recorded files /18 changed events; no runtime/PolyMC installation/commit.


## Аксессуары #148 / #149–156 — исполнительская сдача 21.10.2026

Выполнены исходниковый аудит38 предметов, новая вкладка40 функциональных аксессуаров, пять согласованных изменений баланса,12 отдельных плоских GUI-редизайнов и6 новых украшений (pool_ring, bath_ward, harvest_ring, podvey_belt, leshy_necklace, spark_ring) с рецептами/слотами/серверными эффектами/сохранением CD. Новый общий модуль accessories использует events/Curios callbacks и существующий Persisted PathData; без глобального tick polling и новых слотов. GUI отделён от исходных held моделей; минимальные client body/head fit и visibility guards, исходная геометрия/UV сохранены. Матрица33 wearer IDs и source-backed A/B Steve/Alex/armor/source poses готовы.

Java21 clean build, quarantine, shared-policy/native offline tests, scoped source/resource/JAR gates PASS. Установлен точный1.9.7.53 в PolyMC1.21.1-Testing с backup1.9.7.52, без дублей; остальные6 модов сохранены. Реальный UI/бой/движение/relog/MP — NOT_RUN / OWNER_PENDING, запусков0. Issues OPEN; recommended «Тестирование», поле Project не менялось, owner-accepted не добавлен, commit/push не делались. Подробная матрица, артефакты и installed path/SHA/source manifest: [отчёт](implementation/accessories-148/REPORT.md). Задачи Ада и исходники других механик не затронуты.

## Оружейная программа #87 — начало 21.10.2026

По прямому поручению владельца начат #157 (68 IDs), далее #192 и зависимые пакеты. Текущее состояние IN_PROGRESS, документация: implementation/weapons87/PROGRESS.md. Baseline clean build Java21 и offline attributes PASS, source previews не являются runtime. Оружейных production-изменений на этапе аудита нет; PolyMC не меняется.

### #87 пакет 1 — 21.10.2026

Статические кистени и компоненты (#166/#167/#116/#181) реализованы, source A/B и native Steve/Alex projections подготовлены; Java21 clean build/resource/JAR/generator sandbox PASS. Пакет A прочности8 IDs согласован владельцем и реализован через штатное ModifyDefaultComponentsEvent. Остальные числа не изменены. Общая цепочка IN_PROGRESS, динамика отдельной #164; локальный промежуточный JAR не установлен. См. implementation/weapons87/PACKAGE-1.md и package1-receipt.json.

### #87 адресные хваты и первые лица — 21.10.2026

28 display-моделей откалиброваны по actual native ItemTransform/ModelPart, без изменения геометрии/UV/PNG; все68 IDs включены в source A/B. Player-only ScytheGrip согласует обычный и custom render, не меняя босс-риг и серверный бой. Исправлены отсутствующие кисти у парных клинков Тугарина и camera-space orbit косы70→35°. Java21 clean build/native gates PASS, финальная упаковка текущего промежуточного пакета проверяется; вся #87 IN_PROGRESS, PolyMC не менялся. Следом #164 и остальные независимые дети. См. implementation/weapons87/PROGRESS.md и PACKAGE-2.md.


### #87 продолжение — пакет4,21.10.2026

Оружейная #87: пакет4 #164 (динамика8 кистеней) реализован технически; clean build/resource/JAR/generators PASS,15120 offline численных поз и24 actual source sheets. Runtime OWNER_PENDING; PolyMC не менялся. Следом #171. См. implementation/weapons87/PACKAGE-4.md.


### #87 продолжение — пакет5,21.10.2026

Оружейный пакет5 #171:5 объёмных материальных копий приведены к40-unit семейству,4 ранее удачных модели и все PNG сохранены. Java21 clean build/native/resource/JAR/generator gates PASS, gameplay OWNER_PENDING. #87 IN_PROGRESS; см. implementation/weapons87/PACKAGE-5.md.


### #87 продолжение — пакет6,21.10.2026

Оружейный пакет6 #173:14 ножей/сулиц больше не получают frame Y180 в flying renderer; actual native direction/source/JAR gates PASS, runtime OWNER_PENDING. Семейный source review обнаружил silver_mace material/head FIX; остальные17 обычных art сохранены KEEP с обоснованиями. #87 IN_PROGRESS, см. implementation/weapons87/PACKAGE-6.md.


### #87 продолжение — пакет7,21.10.2026

Оружейный пакет7 #170: серебряная булава получила связную гранёную металлическую головку и читаемый front GUI, прежние TP/FP сохранены. Исполнительские source/build/resource/JAR gates PASS; owner runtime NOT_RUN. #168 audit source-consistency завершена технически. #87 IN_PROGRESS, следующий #174. См. implementation/weapons87/PACKAGE-7.md.


### #87 продолжение — пакет8,21.10.2026

Оружейный пакет8 #174: грозовой посох теперь отличается металлической оправой/синим фокусом от деревянного резного. Остальные4 предмета и общие PNG сохранены. Source/native/build/resource/JAR gates PASS, owner runtime NOT_RUN; см. implementation/weapons87/PACKAGE-8.md. #87 продолжается на uniques.

### #87 продолжение — пакет9,22.10.2026

#182: исправлена сжатая парная GUI-модель Тугарина; PNG32px/held/рендерер/бой сохранены. Индивидуальный source-чертёж до production, actual A/B,18 native FP samples; Java21 clean build/resources/JAR/generator replay PASS. См. implementation/weapons87/PACKAGE-9.md. Runtime NOT_RUN / OWNER_PENDING, PolyMC не установлен. #87 IN_PROGRESS, далее отдельный ranged animation пакет #163.

### #87 продолжение — пакет10,22.10.2026

Добавлен BowDrawClient: vanilla FP BOW pose использует существующие сроки12/30 вместо20 только для short/heavy/Finist при активном use соответствующей физической рукой. Server/art/ammo неизменны.606 actual item power comparisons,120 независимых vanilla matrices,240 native transforms и72 source views; Java21 clean build/resource/JAR/generators PASS. См. implementation/weapons87/PACKAGE-10.md. Вся #163 остаётся IN_PROGRESS: crossbow charge/shot/reload/burst/fan/modes ещё требуют работы; owner runtime NOT_RUN. #87 не завершена, PolyMC не установлен.

### #87 продолжение — пакет11,22.10.2026

#180 статическая плеть:12 JSON, непрерывная сужающаяся тёмная рабочая часть, стабильный16px GUI, отдельный outward left grip.0 PNG/server/predicate code edits.40 native transforms/20 pivots,100 source cases,default idle head candidates0; Java21 clean build/resource/JAR/scoped generators PASS. См. implementation/weapons87/PACKAGE-11.md. Issue OPEN, owner runtime NOT_RUN; динамика190 отдельно. ПакетB баланса согласован владельцем, реализация следующим отдельным пакетом. C/D/190 decision ожидаются.

### #87 продолжение — пакет12,22.10.2026

Явно согласованныйB реализован отдельно: perunite_mace speed.8 при damage9.5;storm_staff magic6/CD60-54 с прежними multipliers/targeting. Exact4 production files,4 RU/EN tooltip values,68 native fields scoped gate,actual damage/CD helpers,Java21 clean build/resource/JAR/generator replay7739 PASS. См. implementation/weapons87/PACKAGE-12.md. Whole158/159/87 IN_PROGRESS, owner runtime NOT_RUN, PolyMC не установлен. C/D/190 decision ещё ожидаются.

### #87 продолжение — пакет13,22.10.2026

#178: принятую5-cube геометрию/UV/shared PNG/хват посоха Лихо сохранили, GUI центрирован и развёрнут существующим глазом; верх перестал выходить за16px canvas,eye-colored pixels0→1.20 native source cases,idle head candidates0,Java21 clean build/resources/JAR/generators7779 PASS. См. implementation/weapons87/PACKAGE-13.md. Owner runtime pending, Issue OPEN,87 IN_PROGRESS,PolyMC не установлен. C/D/190 по-прежнему ожидаются.


### #87 P44 — непрерывная ветка, 22.10.2026

В draft PR #216 (ветка `work/weapons87-continuous-20261022`, не переносить в main) частично реализованы #162/#165/#163/#160/#161: 32 обычных melee/polearm ID получили клиентские 1P фазовые позы, special crossbow получил дополнительную charge pose и единственный mode-cue на залп; повторный debuff proc одной стрелы прекращается. CI Java21 + JAR PASS для коммита `bbb3a353`, [run #39812159700](https://github.com/Deserag/slavic-myths/actions/runs/39812159700); `slavicmyths-1.9.7.56.jar` SHA256 `ed564259729eda1027a7e5c3331797a75593981eb28635a0787677163b148eee`. Все затронутые Issues IN_PROGRESS, Minecraft/PolyMC NOT_RUN, владелец ещё не принимал. Никаких изменений C/D баланса, hit timing плети #190 или main. См. `implementation/weapons87/WORKLOG-CONTINUOUS-BRANCH.md`.


### #87 P45–P46 — продолжаем только ветку PR #216, 22.10.2026

P45: семь ножей получили 1P swing броска и семь сулиц — собственный FP windup/release в `ThrowingMotionClient` + client-only swing после серверно допустимого выбрасывания в `ThrowingWeaponItem`. Для исходников/JAR Java21 CI PASS [#39813871455](https://github.com/Deserag/slavic-myths/actions/runs/39813871455), SHA256 `slavicmyths-1.9.7.56.jar`: `75aede07fc2240f23cb1fdb6ead673c565fcbd8179586e2c02d663bba68f32e9`. #162/#165 всё ещё не завершены; игровой 3P/MP/Steve/Alex тест отсутствует. P46 исправляет две неактуальные QA-сверки crossbow, допускающие только точный additive P42 overlay поверх оригинала; production geometry не трогается, отдельные legacy CI были FAIL до этого исправления. Все работы лишь в `work/weapons87-continuous-20261022`, без merge в main. Подробнее `implementation/weapons87/WORKLOG-CONTINUOUS-BRANCH.md`.


### #87 P47 — third-person weapon motion, 22.10.2026
В branch `work/weapons87-continuous-20261022` через draft PR #216 добавлен player-only 3P item-model animation для 48 ID (#162/#165/#163) на клиенте: ближнее/копья/посохи/метательное/арбалеты. Все исходные JSON/PNG, server damage/hit timing, registry и особые renderer (коса Карачуна, кистени, плеть) сохраняются. На момент source commit CI P47 PENDING; game runtime, Steve/Alex/MP, visual owner acceptance NOT_RUN, указанные Issues IN_PROGRESS. `main` без изменений. См. `docs/implementation/weapons87/WORKLOG-CONTINUOUS-BRANCH.md`.

### #87 P47 — подтверждение CI после публикации
Исходники `97bf25c3`, [GitHub Actions #39816854559](https://github.com/Deserag/slavic-myths/actions/runs/39816854559) **SUCCESS**: Java21 clean build, P42–P47 source checks и production JAR class/resource QA PASS. JAR `slavicmyths-1.9.7.56.jar`, SHA256 `acf4d4d5ce8cc30dc17eaf3a38c60b29d74484e463728429edf1c05b89187fa6`. Runtime Minecraft/PolyMC NOT_RUN; owner acceptance pending. Целые Issues #162/#163/#165 IN_PROGRESS, поля GitHub Project №9 через текущие доступные actions не изменены; комментарий не эквивалентен переходу «Тестирование».

### #87 P48 — third-person draw for three bows, 22.10.2026
В draft PR #216 P47 third-person BakedModel overlay расширен для `short_bow` (12 тиков), `heavy_bow` (30) и `finist_bow` (30), только фактическая рука и штатный `pull` model variant. Клиентский 1P BowDrawClient, server ammo/power/finist quiver, модели и registry сохранены. P48 Java21/JAR CI PENDING, Minecraft/PolyMC NOT_RUN; #163 OPEN/IN_PROGRESS. Journal `implementation/weapons87/WORKLOG-CONTINUOUS-BRANCH.md`.

### #87 P48 — Java21 gate подтверждён
[CI #39818424258](https://github.com/Deserag/slavic-myths/actions/runs/39818424258): SOURCE PASS / Java21 BUILD SUCCESSFUL / JAR PASS, 51 third-person models including three bow with 12/30/30 charge timings. Artifact `slavicmyths-1.9.7.56.jar` SHA256 `15ba5e2185f6f14e7865278dafc324b0a62546cfe6d557e580531b70d34336d5`. Runtime 0, PolyMC not installed, owner game acceptance pending. Issue #163 OPEN/IN_PROGRESS, actual Project Status не изменён.

### #87 P49 — первая техническая реакция на повторную игровую приёмку владельца, 22.10.2026
Владелец **принял** арбалеты, косу/посох Морозко и неперечисленные в замечаниях предметы; отверг игру с копьём победителя, хват двух каролингских мечей и плети Полоза, шар кистеня, прицел Финиста, мерцающее острие громового копья, рог, откат грозового посоха и анимации шести trophy heads. P49 меняет конкретные Java-классы / 3D модели / восстановленный native Poloz model, см. `implementation/weapons87/WORKLOG-CONTINUOUS-BRANCH.md`. Java21/JAR проверки будут отмечены после CI, runtime/PolyMC/MP NOT_RUN, не закрывать отклонённые Issues до повторного подтверждения. PR #216 draft, не merge `main`. GitHub Project №9 Status field через подключённые actions недоступен.

### #87 P49 — Java21/JAR и раздельная приёмка, 22.10.2026
[Continuous #39823972474](https://github.com/Deserag/slavic-myths/actions/runs/39823972474), [P39 #39823972563](https://github.com/Deserag/slavic-myths/actions/runs/39823972563), [Storm #39823972609](https://github.com/Deserag/slavic-myths/actions/runs/39823972609), [P35 #39823972429](https://github.com/Deserag/slavic-myths/actions/runs/39823972429), [P38 #39823972691](https://github.com/Deserag/slavic-myths/actions/runs/39823972691) — SUCCESS, Java21 clean build/P49 JAR PASS. JAR `slavicmyths-1.9.7.56.jar`, SHA256 `ba6ac79353692b88902f168257850d1901bb87519c3afcce0f3814424cf0785a`. По прямому разрешению владельца добавлена Issue-метка `owner-accepted` #170/#174/#175/#176/#177/#179/#183/#186 (только арт соответствующих задач; проблемное автонаведение Финиста отдельно). **Статус «Готово» проекта №9 не был изменён** из-за отсутствия Project v2 Status action в текущем коннекторе; Issue не закрыты. Непринятые задачи в P49 ещё нуждаются в игровом тесте, Minecraft/PolyMC/MP NOT_RUN. PR #216 draft, без merge main.

## 22.10.2026 — P51 technical CI receipt
Java21 [#39829846856](https://github.com/Deserag/slavic-myths/actions/runs/39829846856) SUCCESS on b606f727: source and packaged P51 checks PASS, clean build PASS. JAR 1.9.7.56 SHA256 d9b805ac7355d2e9d2846807f8816f1437207af772e1bd541880cfd5d6815c68; PolyMC NOT_INSTALLED, runtime NOT_RUN; accepted scope unchanged, #140/Finist OWNER_PENDING.

## 22.10.2026 — P52 production CI verified
All 11 workflows for e0560924 SUCCESS; Java21 clean build + P52 source/JAR PASS: [Actions #39832746501](https://github.com/Deserag/slavic-myths/actions/runs/39832746501). JAR 1.9.7.56 SHA256 ed90b2d3cc519d1bc3cae036e42658eb741fe5bc38c813f5cea9b3ef49c61f91. #161 partial implementation, Minecraft and Project Status «на тестировании2» NOT_RUN/NOT_SET, open owner QA, main unaffected.

## 22.10.2026 — локальное восстановление P49–P52
См. [LOCAL-RECOVERY](implementation/weapons87/LOCAL-RECOVERY-20261022.md): Java21 clean build и 302 packaged resources PASS; native Finist steering PASS; native trophy animated bounds FAIL у Тугарина, Волколака и Подземника. Цепочка IN_PROGRESS, runtime NOT_RUN, POLYMC_NOT_INSTALLED; main не менялся. Требуется исправление animated envelope с source before/after, без ослабления проверок.

## 22.10.2026 — исправление выхода челюстей трофеев за основание
Только trophy Head.pose: челюсти Тугарина, Волколака и Подземника втягиваются при раскрытии, угловая амплитуда и нейтральный размер сохранены. Native ModelPart/UV/bounds/reset: PASS на 600 позах каждой из 6 голов; исходные до/после и одинаковые ракурсы в [отчёте](implementation/weapons87/head-clearance/REPORT.md). P42–P52 source regression PASS. Clean build PASS (1m30s), JAR 1.9.7.56 SHA256 4bc71e94da5e8529584d9aff3ffd1623f5d236aaebb91cdc4819dae076cb3269, 68 ID/302 packaged assets PASS; Minecraft/MP/GameTest/сервер NOT_RUN, owner acceptance pending, вся цепочка не объявляется готовой. CI дополнен native geometry и реальным Finist steering.

## 22.10.2026 — #140: полный пакет исправлений трофейных голов для ручной проверки
В версии 1.9.7.57 исправлены взгляд/моргание обоих Лих, подключены отдельные native eye/ear части Скима без изменения живого моба, оба уха Волколака, реальные зрачки Тугарина и челюсти/брови/частицы шести голов. Burst разрушения перенесён в единственный серверный onRemove нижнего anchor; halves/drop не менялись. Java21 clean build + native 600 поз/голову + 7601 resource/JAR checks PASS. Тестовый кандидат установлен с backup и без дублей в PolyMC 1.21.1 Testing, SHA256 0793672e1c652d19410aa4c7491139e27a4977448b9a4cea65371567217d33ad. Minecraft/server/GameTest=0; owner runtime NOT_RUN; #140 OPEN, main не слит, Project Status не менялся. Источник 1192e229 + local patch. [Отчёт, installed path/SHA, source до/после](implementation/trophy140-rework/REPORT.md). Это не итоговый релиз всей #87.

## 22.10.2026 — пакет из «Ожидания»: #98 + #99
Реализованы все 4 природных ресурса #98 и 8 crafting-наконечников #99, только 12 PNG прежнего разрешения. T1-A/T1-B проверены последовательно; модели/оружие/механики/данные не менялись. Канонический scoped генератор + защита historical writers, A/B 1×/3×, точный production diff PASS. Java21 clean build PASS 56s; 7601 packaged resources PASS. Рекомендуется «Тестирование» для обеих; фактический Project Status NOT_CHANGED (credential без Projects scope, signed-out browser). Версия 1.9.7.57 НЕ повышалась, PolyMC НЕ обновлялся, runtime0. #140 владелец явно принял и разрешил «Готово»; поле также ещё NOT_CHANGED. [Полный отчёт](implementation/materials-81/N98-T99/REPORT.md).

## 24.10.2026 — #237 compact reader implementation (IN_PROGRESS)

Один TaleScreen поверх #235: компактное сворачиваемое оглавление, скрытие панели/широкое чтение, список деяний с реальными описаниями и дерево с общей геометрией fit/center/click. Clean build1m33s и7688ресурсов PASS до последних уточнений; первый реальный A/B клиентский прогон выполняется на копии BookQA. Полная визуальная/MP приёмка и финальные замеры ещё не подтверждены. [Матрица](implementation/book237/REPORT.md).


## 24.10.2026 — #236 guides, flight and control combinations (IN_PROGRESS)

Ветка codex/guides-236 поверх #237/#235: серверные поручения Яги/Баюна в TaleData, существующие anchors/навигация; фоновый native план полной крепости без NPC block placement/tickets. Открытие книги больше не запускает генерацию избушки: отдельная кнопка. Волк trust4 без миграции старых ключей;3обмена;ступа2места с сохранённым явным пилотом. Server runtime2:9/9 PASS после исправления нижней границы высоты; командная крепость и контрольный блок PASS, peakplacement111ms требует учёта. Natural NPC/restart/две реальные сессии ещё NOT_RUN. Дополнение владельца: Ctrl+1/2/3, captureCtrl/Shift/Alt;3строки PolyMC options изменены сbackup, JAR не установлен. [Матрица](implementation/guides236/REPORT.md), [управление](CONTROLS.md).


## 26.10.2026 — #286 исполнение, #276 audit-only, IN_PROGRESS
В hell2.0.2-HELL исправлены две существующие транзакции: лейка не расходует заряд на уже мокрую/отказанную запись; шесть средств Яги не меняют клиентский инвентарь при завершении употребления. Каталог923/441кандидат классифицирован, граф получения/применения и19кандидатов ценности опубликованы; ovinnik_claw без найденного producer требует approval. Цены/получение/баланс/loot не менялись. Java21clean build PASS,204transaction+70rules+25source/package checks,8001resources/1948classes PASS; game launches0/OWNER_PENDING. #276:81tables,6x20000контейнерных прогонов,60000actual pure layouts,631checks; V2не использует27legacy tables #241. Подробности и установка тестового кандидата с source/SHA/backup: [#286](implementation/items286-20261026/REPORT.md), [delivery](implementation/items286-20261026/delivery.json), [#276](implementation/loot276-20261026/REPORT.md). Все IssuesOPEN,mainне изменялся; никакой итоговой приёмки/релиза не заявлено.


## 26.10.2026 — #276 L1, шесть сооружений, OWNER_PENDING
По прямому поручению реализован один главный ценный roll в существующих контейнерах курганов V2 I–III и малого/среднего/большого лагерей. Только 14 новых rewards276 tables, pure routing helper и 3 подстановки table ID. Whole-rune шансы 8/25/45% и 5/20/35%; perunite 0/12/30% и 0/8/25%; III scrap15%, одна enchanted diamond armor piece3%, сырьевое iron0. Main кургана помещается в 5 slots, без сложения прежней полной таблицы поверх новых pools. Монеты, торговля, князь/Тугарин, прочие структуры/старые loot references сохранены. Java21 clean build1m16s, 8015 resources/1949 classes, 1760 offline checks, 60k actual pure layouts, 6x20k samples PASS; gameplay/server/GameTest/PolyMC запусков0. BUILT2.0.3-HELL, POLYMC_NOT_INSTALLED; установленный2.0.2 не менялся. #276 OPEN / OWNER_PENDING, main не менялся. [Отчёт, вероятности/количества, все table IDs, save limits и playtest](implementation/loot276-l1-20261026/REPORT.md). Старый audit-only отчёт #276 — baseline, не описание новой реализации.


## 28.10.2026 — локальная сборка и отдельный PolyMC Testing

hell35c81997: Java21 clean build PASS26s, production2.0.8-HELL установлен в новый экземпляр1.21.1/NeoForge21.1.255/Curios9.5.1; исходники и старые1.16.5 не изменены. Один клиент загрузил ресурсы без ERROR/FATAL и закрыт, миры0; визуальное меню NOT_RUN (capture timeout), gameplay/MP NOT_RUN. Чертоги НЕ ГОТОВЫ; приёмка не заявлена. [Матрица, SHA, пути, логи и ручные шаги](implementation/build-install-20261028/REPORT.md).


## 28.10.2026 — Горючие острова 2.0.5: независимый код, IN_PROGRESS / BLOCKED

hell, source f9beb28d: зарегистрированы три нейтральных моба и босс 850 HP,
написаны отдельные ModelPart/UV/PNG, материалы, рецепты, ответный эффект рун I/II,
новая голова в прежней TrophyHeads и журнал/inbox наград. Native/UV, прежние шесть
голов и resource/JAR gates PASS; Java21 clean build 21s, 8302 ресурса/2049 классов.
Настоящих ocean-only selection, островов, вулкана и арены НЕТ: 534/1053 проб
гипотетического climate append не имеют lava на уровне моря (5 native noise seed).
Временная магма II, PNG match, genuine audio и cross-player crash authority BLOCKED.
Реальные AI/бой/MP/performance NOT_RUN; игровых запусков этой задачи 0.
Новый кандидат SHA256 102952bb4c573fcb370c13fca2e702d19fed189d6d965d89aa26cdde677cc9ab
не установлен в PolyMC. Не DONE, приёмка владельцем отсутствует, Issues/Project не менялись.
[Полный отчёт, матрица и ручной checklist](implementation/burning-islands-205/FINAL_QA_REPORT.md).


## Установка кандидата 28.10.2026

2.0.8-HELL установлен в тестовый PolyMC, SHA256 `102952BB4C573FCB370C13FCA2E702D19FED189D6D965D89AA26CDDE677CC9AB`; backup сохранён вне mods, один JAR. Java21 clean build и ресурсы PASS. Исходники hell актуальны относительно origin (3 локальных коммита впереди), VS Code открыт на эту копию. Игровых запусков 0, runtime NOT_RUN; блокеры Горючих островов сохраняются. Это обновляет прежнюю запись «не установлен», не означает приёмку. [Установка и проверки Ада](implementation/polymc-update-20261028/INSTALL_AND_NETHER_CHECKLIST.md).


## P0 #327 — частичное исполнение 28.10.2026

Только hell, HEAD c884e63c + uncommitted delta. В IslandsMob/IslandsContent добавлены boss-only Peaceful discard до AI/hurt, MONSTER для Повелителя и bossbar cleanup. PostenEncounters резервирует body+4 clone UUID без cross-arena/self alias; V1 conflict read-only. Новый негативный тест воспроизвёл старый дефект; затем 12077 offline assertions и Java21 clean build 13s PASS, resources/JAR PASS. Кандидат SHA256 d7521e97b1dd51df4650155072a3f4643b106178549944992e6225fa4b44c574, POLYMC_NOT_INSTALLED; прежний установленный кандидат не заменён. Игровых запусков 0 / runtime NOT_RUN. Оригинальные 8 PNG не найдены (36005 headers/126 ZIP): ART BLOCKED_REFERENCE. Модели/головы/полные Чертоги 2.0.4 не завершены, 2.0.5 WORLDGEN_NOT_INTEGRATED. Нет DONE/owner acceptance/закрытия Issue. [Фактический отчёт #327 и checklist](implementation/p0-bosses-327/REPORT.md).


### 28.10.2026 — #327 resume / Nether 2.0.4 production candidate (hell)
Подключены lost_halls biome/terrain/metadata,5 species, Posten entity+4 stable clones, Shadow Face rune/decoys, utility rewards,3 new TrophyHeads preserving zharenitsa_head ID; native geometry3 bosses обновлена. Reward recipient pin хранится в world/data/slavicmyths_boss_reward_authority.json; batch+receipt в player save, corrupted authority read-only. Technical gates/evidence: docs/implementation/p0-bosses-327/REPORT.md, ACCEPTANCE_MATRIX.md, final-gates.log и BUILD_DELIVERY.json. IN_PROGRESS: originals ART_SOURCE_BLOCKED, all real gameplay/relog/MP/performance NOT_RUN, zero game launches; natural full-region/arena coverage not established. PolyMC candidate NOT_INSTALLED; no owner acceptance/Issue close. #313/#320/#323 ocean islands worldgen не объявлен реализованным. Decisions and two QA passes: OPEN_QUESTIONS_AND_RESOLUTIONS.md. Commit/push hell explicitly authorized by latest user message; main/release untouched.


### 29.10.2026 — обновление тестового PolyMC
По прямому поручению подтянута hell@537e3a7a, clean build/resources PASS. Установлен2.0.10-HELL, SHA256 `685387c355ba9665e6921dbd1757761cfddfe75651ff6bd5b48ab50f826a77f9`, backup и единственный JAR подтверждены. Каталог инстанса2.0.8 сохранён, отображаемое имя2.0.10. Исходный код/миры не менялись; запусков0, runtime NOT_RUN, не owner acceptance. Отчёт: implementation/polymc-update-20261029/REPORT.md.

29.10.2026 G2: реализованы пять особых оружий/приёмов через прежние melee/projectile/bow/flail pipelines, модели и рецепты. Подробности и неутверждённые инженерные числа: implementation/2026-10-29-hell-remaining/WEAPONS_IMPLEMENTATION.md. Игровые проверки не запускались; ART originals отсутствуют.

29.10.2026 G3: Адская печь и Адский уголь реализованы как функциональный прототип; vanilla furnace NBT/recipes/XP/progress и строгий fuel gate. Параметры BALANCE_OWNER_PENDING, игровой результат NOT_RUN. См. implementation/2026-10-29-hell-remaining/FURNACE_IMPLEMENTATION.md.

29.10 remaining debts: source52 current combat actions for20 Nether entities (without HP/damage rebalance), sulfur heat bias/surface range, independent nether_aura necklace prototype, StormStaff offline norm48/42. Реальные worldgen/combat/Curios tests NOT_RUN. См. implementation/2026-10-29-hell-remaining/REMAINING_DEBTS_IMPLEMENTATION.md.
