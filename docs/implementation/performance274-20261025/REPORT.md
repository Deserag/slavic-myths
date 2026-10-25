# #274 — ограниченные исправления производительности, 25.10.2026

OPEN / IN_PROGRESS / OWNER_PENDING. Только `hell`; база `b34f602421f2d300280a39ea819ececd483b5455`, совпадала с origin/hell перед работой. Рабочее дерево было чистым. Main перед работой: `26e73424f16ae703e9a9b75fe432731663d30e05`. Issue #274, родитель #269 и связанные #235/#236/#271/#272/#275 прочитаны. Project9 действительно переведён из «Концепция» в «В работе» по прямому заданию владельца; «Тестирование»/«Готово» не выставлялись.

Minecraft/PolyMC/runClient/runServer/GameTest: **0 запусков**. Установка и миры не изменялись. POLYMC_NOT_INSTALLED для этого пакета. Наблюдавшийся установленный HELL JAR до работы имеет SHA256 `d85d42e521e1bd8f270033545555dcc5a804626669f223f8f591b50e514cd598`; это другой артефакт, не новая сборка.

## Контракт и путь игрока

Параметры встречаемости, биомы, баланс, геометрия, награды и registry IDs не меняются. Новых численных SLA нет: их требуется согласовать после настоящего baseline. Обычный игрок узнаёт о местах из Книги сказаний/поручений, выполняет прежние условия доступа, запрашивает дорогу, получает статус, идёт к реально созданному месту и продолжает существующее поручение. OP-команды ниже — отдельная диагностика, не доказательство доступности в Survival.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Поиск не сканирует дальний мир синхронно | BoundedStructureSearch.start/Job.tick, ManualStructureCommands, ChudSearchCommands, CommandTree | Книга → поиск → честный результат | Аудит callsites; затем реальный профиль | Уже загруженные starts либо существующая очередь, без ложного найдено |
| Яга/Баюн/Чудь доступны прежним путём | YagaDiscovery, BayunCreation, ChudCreationJobs, NavigationManager | Допуск #272 / прежние победы / поручение → книга → место | Native #272/#275 + будущий Survival | Нет второй уникальной избушки/кота; rewards и сюжет сохранены |
| Строительство безопасно и поэтапно | ManualStructureJobs, LargeSiteJobs, TemplateSiteJobs, SiteChunks | Запросить место, дождаться статуса, посетить | Native транзакции/индекс; живой тест позже | Защищены строительные блоки/контейнеры/порталы, сохранены reservation/start |
| Отмена и выгрузка освобождают ресурсы | ChudCreationJobs.cancelRaster/unload, SiteChunks.close/unload | Уйти/вернуться; диагностическая отмена отдельно | Cooperative cancellation fixture; lifecycle source audit | Работник прекращает устаревшую растеризацию; owners/tickets закрыты |
| Сохранение старых мест | ManualStructureRecords.add/replace/remove/load | Старый мир → книга → прежнее место → перезаход | Индекс против полного перебора, NBT | Никакой миграции формата, прежние IDs/families |
| Сохранить Ад | NavyaWorldgen/Geology/Nature/TreeFeature, Nether resource overrides | Портал → новые чанки → роща/Сердце | Source audit; настоящий worldgen NOT_RUN | Прежний внешний вид; без слепого redesign |

## Причины и изменения до/после

| Проблема | Исходный метод | Причина | Что исправлено | Доступный тест | Результат | Игровая проверка |
|---|---|---|---|---|---|---|
| Повторный просмотр всей геометрии Чуди | chud/ChudCreationJobs.raster → ChudGeometry.rasterize | Для каждой 16×16×16 секции перебирался весь Fill list | Упорядоченный индекс Fill по горизонтальным чанкам; сохранён 18-блочный lookahead и порядок последней записи | 405 clips, отрицательные координаты, air overwrite; также 16 реальных city sections/decor в двух rotation | PASS OFFLINE: offline.log | NOT_RUN |
| Отменённый worker продолжает работу | ChudCreationJobs.Job.step/finish | CompletableFuture.cancel(false) не останавливает supplier; lambda захватывала изменяемое Job.piece | Фиксированный selected piece и отдельный AtomicBoolean на попытку; проверка перед blueprint, при индексировании и перед каждой секцией | Отмена до индекса/на следующей секции | PASS OFFLINE: offline.log | NOT_RUN |
| Завершение одной записи переиндексирует весь мир | ManualStructureRecords.replace/remove; повторный add оставлял старые buckets | clear + index всех entries, а add не снимал прежние coordinates | Снять только старый footprint, добавить новый; удалять пустые buckets | 80 замен/удалений × 88 queries против полного перебора; NBT, очистка buckets | PASS OFFLINE: offline.log | NOT_RUN |
| Откат проверяет ещё не тронутый suffix | YagaPlacement.Cursor.abort / catch | rollback использовал весь desired list даже после нескольких записей | rollback проходит только written keys; ownership и undo сохранены | 0/1/7 записей из 1000, лимит шага, фактическое восстановление | PASS OFFLINE: offline.log; #275 fixture | NOT_RUN |
| Тригонометрия горы повторяется по высоте | serpenthill/HillPlan.state | vein/skin зависит только от X/Z/seed, но считалась для каждого Y | Вычисление один раз на столбец при создании плана | Исходная формула/patch precedence, 3 seed, края высот | PASS OFFLINE: offline.log | NOT_RUN |
| Уровень удерживается static job maps после отдельной выгрузки | LargeSiteJobs/TemplateSiteJobs/ChudCreationJobs/OnDemandStructures; SiteChunks | Очистка только при остановке всего сервера | LevelEvent.Unload вызывает прежний finish/close и удаляет pending | Source wiring + компиляция; настоящий unload/tickets NOT_RUN | SOURCE_ONLY | NOT_RUN |

Каждый production-файл вне явных девяти файлов #274 совпадает с HEAD #272; весь `src/main/resources` совпадает с ним. Старые scope guards #271/#275/#272 передают только эти файлы новому guard, остальные проверки сохранены. Старые квитанции не перезаписываются.

### Границы эффекта

Новый индекс Чуди сокращает число проверок Fill-кандидатов, а не количество нужных блоков. Он требует дополнительных ссылок по занятым горизонтальным чанкам; освобождается вместе с worker. У горы добавлены 16384 ссылки на существующие строки (около 64–128 KiB на plan, в зависимости от размера ссылки), вместо вычисления формулы для каждой высоты. Индекс записей пропорционален footprint изменённой записи, но поиск ближайшего через entries() всё ещё линейный; без измерений это не менялось.

**Новых гарантий максимального MSPT нет.** Убрана лишняя работа в серверных методах индекса, rollback и state; сам chunk generation, template loading, plan creation и отдельный postProcess остаются атомарными на своих текущих потоках. Не заявляется устранение всех блокировок /locate или причины 2–3 минут у владельца.

Отмена worker кооперативная: уже начатый blueprint.build и одна растеризуемая секция заканчиваются до следующей проверки. Очередь не переносит world/chunk/entity API в worker. Мировые чтения и записи остаются на сервере. Остановка Яги/Баюна обязана закончить ownership rollback до save; этот путь не удалён ради быстрого выхода. При большом уже изменённом footprint он может оставаться дорогим.

## Аудит без необоснованных изменений

- SiteChunks.ready уже использует только getChunkNow, native tickets, не более четырёх pending у одного владельца измерения; ни blocking future.join, ни getChunk здесь нет. Не менять таймаут или ticket radius вслепую. Held footprint нужен до завершения, иначе соседний чанк выгрузится между preflight и размещением. close идемпотентен; LOWEST unload/stop служит последней очисткой после owners.
- BoundedStructureSearch — максимум 32 уже загруженных candidates, одна проверка за tick. Известная координата для телепортации готовится через SiteChunks. OnDemandStructures и поиск книги переиспользуют ManualStructureRecords/NavigationRecords; открытие книги не запускает мировой поиск. Точные compatibility literals модовых /locate маршрутизируются через CommandTree. **Vanilla /locate для прочих IDs и tags остаётся vanilla и может блокировать**; переписывать общую ванильную команду без профиля и проверки совместимости нельзя.
- ManualStructureJobs уже имеет scan/preflight/terraform budgets и slices 16×16×4; LargeSiteJobs — deadline вокруг placement; TemplateSiteJobs — slices 16×16×2. Эти бюджеты не прерывают внутренний postProcess. Попытка «ускорить» повышением лимитов или исключением protection не выполнялась. Уникальность/резервации #275 сохранены, частичные крупные постройки остаются construction.
- Навья: geology/nature проходят локальные 16×16×112 колонки, tree preflight проверяет planned cells и 26 соседей каждого бревна; это потенциально дорого, но не ретрогенерация и не обход мира каждый tick. Не удалялись проверки пересечения/разделения деревьев. Высота Nether/noise и climate bootstrap сохранены; неизвестно, какая доля нагрузки принадлежит density/surface/features/entities полного модпака. Нужны JFR samples по этапам, отдельно cold/warm chunks.
- SaveData: индекс ManualStructureRecords не сохраняется, сериализуется прежний list. setDirty ставится при реальной add/replace/remove; запросы overlap/read его не ставят. Нет доказательств, что именно эти данные объясняют медленный save полного клиента. Глобальные save hooks и модпак не переписывались.

Исторические данные `runtime-repair-20261025/REPORT.md`: отдельный сервер 11–14 секунд startup/~2 секунды stop, большие места 8–63 секунды на одном seed. Это **не baseline текущего JAR и не замер клиентского модпака**. В текущем пакете FPS/MSPT/p95/p99/ускорение в процентах не измерены.

## Технические gates

Команда: Java21 `gradlew.bat --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1536m -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8" -I tools/performance274-offline.gradle -I tools/structures275-offline.gradle -I tools/encounters271-offline.gradle -I tools/yaga272-offline.gradle clean build verifyPerformance274Offline verifyStructures275Offline verifyEncounters271Offline verifyYaga272Offline`.

Результаты и точный JAR/SHA — build-clean.log, checks.log, receipt.json. Первый прогон содержал diagnostics неверной кодировки новой строки unload и закончился SUCCESS после исправления; он сохранён как build-first.log. Повторный clean build выполняется уже без этих compiler errors; итог ниже. Все native fixtures — обычная JVM с vanilla registry bootstrap, без мира/клиента/сервера. Gradle test NO-SOURCE не считается выполненным gameplay test.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Причины повторной CPU/index/rollback работы | Конкретные methods и diff выше | PASS (SOURCE) |
| Эквивалентность geometry/index/state/частичного rollback | Native offline fixture, build log | PASS (OFFLINE; квитанция) |
| Сохранение #271/#275/#272 и ресурсов | Scope guard + три native fixtures + JAR/resource audit | PASS (OFFLINE; квитанция) |
| Unload/actual ticket release, очереди и живой cancel | Lifecycle source evidence; запуск запрещён | NOT_RUN |
| Создание/закрытие полного клиентского мира; baseline→after | Запуск запрещён; сценарий ниже | NOT_RUN |
| Быстрый /locate любых IDs/tags | Vanilla fallthrough остаётся; нет runtime-профиля | NOT_RUN |
| Survival natural paths, 5 seed, MP4, старые сохранения | Запуск запрещён | NOT_RUN |
| Установка/приёмка владельцем | Не выполнялись | NOT_RUN / POLYMC_NOT_INSTALLED / OWNER_PENDING |

## Как проверить в Minecraft после отдельного разрешения

Точная сборка: `build/libs/slavicmyths-2.0.1-HELL.jar`, SHA в receipt.json, source — опубликованный коммит этого пакета. Перед будущей установкой сохранить backup единственного установленного JAR и конфигурации, проверить SHA и отсутствие дублей. Сейчас установка запрещена. Тестировать новый отдельный Survival-мир и копию старого мира, не оригинал; тот же NeoForge 21.1.255/Java21/модпак/render distance/heap, без фоновой нагрузки. Для сравнения до/после использовать отдельные копии одного untouched baseline save; нельзя сравнивать cold exploration до со сгенерированными чанками после. Минимум 5 seed: 274, -274, 2432026, 42, 20261025. Записывать biome/coords/seed/game day и исход отказа.

| Действие | Ожидается | Измерение и признак регрессии |
|---|---|---|
| 1. Новый мир, 3 повтора каждого JAR на одинаковом seed | Игрок появляется, работает движение/книга | Секундомер от подтверждения создания до управления; отдельно menu/resource reload; JFR CPU/GC. Не считать заранее обещанное ускорение |
| 2. Копия старого мира, cold и warm загрузка | Те же прогресс/инвентарь/координаты | Время от входа до управления, RAM/GC; зависший searching/потерянный marker — FAIL |
| 3. Без OP открыть книгу, прежним путём получить доступ к Яге, сделать 10 последовательных запросов мест | Быстрый статус, реальные координаты только после результата; повтор уже найденного места переиспользует его | От клика до статуса и до результата отдельно; MSPT/p95/p99, thread samples; новая избушка при повторе — FAIL |
| 4. Штатные победы/поручение → книга → Яга и Баюн | Избушка с Ягой/Кузей, платформа и кот, свежая карта | Время каждого этапа; осмотр NPC/дверей/декора и разговор; сундук/портал/свои брёвна рядом целы |
| 5. Поручение → Чудь (city/attached/medium/great — диагностические варианты отдельно) | Настоящий вход/лестницы/комнаты/население; уникальный результат | Время ожидания, CPU worker и server, heap peak; отсутствие входа или бесконечная очередь — FAIL |
| 6. Исследовать одинаковый новый маршрут Overworld и Nether, все ванильные биомы и рощу | Нет изменения вида/геометрии/доступности, деревья раздельны | Одинаковая дистанция/скорость, samples chunk generation/features/entities, MSPT/GC; зависания, швы, исчезновение биома — FAIL |
| 7. Повтор/отмена/негативный сценарий | На защищённом месте честный отказ, без потери сундука/портала; новая задача не ждёт старый raster | Время cancel→следующий результат; tickets/job maps/worker в профиле; partial construction остаётся резервом, не completed |
| 8. Сохранить/выйти после 10 запросов и 3 строек; отдельно во время подготовки | Нет зависшего worker/утечки, rollback Яги/Баюна, partial крупные места не выданы как готовые | Время кнопка→меню и stop tail, CPU/GC/IO; thread dump зависания; нельзя прерывать пользовательский процесс ради теста |
| 9. Настоящий повторный вход | Те же места/NPC/ledger/доверие/книга; без двойной награды/строительства | Сравнить координаты и пройти к местам; NBT roundtrip офлайн этого не доказывает |
| 10. MP4 на копии мира | Два игрока запрашивают одно место, другие исследуют/переходят в Nether; один выходит во время подготовки | Один canonical результат, нет перекрытия reservations; измерить server p95/p99 и client frame stalls отдельно; полная работа сети обязательна |

OP-диагностика отдельно: `/sm generation status`, `/sm generation cancel`, `/sm chud_search status`, `/sm chud_search cancel`; compatibility `/locate structure slavicmyths:chud_city`, `slavicmyths:gorodishche`, `slavicmyths:serpent_hill`, `slavicmyths:sanctuary_unfading_fire`, `slavicmyths:abode_pure_spring`. `/sm search cancel` относится к BoundedStructureSearch, а не ко всем construction jobs. У TemplateSiteJobs универсальной cancel-команды нет; не обещать, что её добавили. Проверить raw IDs и tag lookup отдельно, записав фактическую команду и call stack; /locate может создавать место по прежней compatibility-семантике. OP-тест не заменяет Survival.

Профилировать разрешённую игровую сессию встроенным `/jfr start` и `/jfr stop` (OP), либо средствами JDK21 attach JFR к точно установленному тестовому PID. Писать wall clock boundaries каждой операции и .jfr/log рядом с таблицей before/after; сравнить server tick distribution и клиентские frame stalls раздельно. Для долгой операции снять thread dump: server getChunk/future waits, postProcess, io/save, worker raster и native chunk workers. Согласовать SLA с владельцем по этим данным. Не запускать тяжёлый soak автоматически; маршрут и длительность согласовать перед запуском.

**НЕ ПРОВЕРЕНО:** actual chunk tickets/unload, живое строительство/отмена/restart/NPC/сеть, natural spawn на 5 seed, полный модпак, startup/save latency, p95/p99/MSPT/FPS, heap удержание после длительного исследования, игровые награды и полный сюжет. Возможные соседние регрессии: порядок overwrite/decor на границах секций, snapshot rollback при вмешательстве игрока, records overlap после замены, unload callbacks при остановке, новый plan memory. Эти риски требуют сценариев выше.

## Риски/откат и следующая задача

Формат сохранений и ресурсы прежние. Откат только отдельным обратным коммитом данного пакета в `hell`, без reset/force и без отката пользовательских миров; construction reservations не удалять. Не переносить на main и не устанавливать JAR в рамках этого задания.

Передача #273: восстановить полное прохождение двух святилищ/ритуалов/хранителей/наград отдельным поручением. Здесь #273 не начиналась. Передать сохранённые #275 reservations/start/family IDs, #272 trust/reward правила и обязательность Survival+MP/relog. Не считать отсутствие runtime-профиля устранённым P0 #274 и не закрывать её по одному clean build.

## Финальная техническая квитанция

Java21 clean build: **PASS, 3 минуты**, max-workers=1, heap1536m, ActiveProcessorCount2. 100 существующих deprecation/unchecked warnings; повторный clean log не содержит compiler errors. Gradle test NO-SOURCE.

Новые проверки: **6 suites / 90701 checks PASS** (405 synthetic clips и 16 sections настоящей city geometry/decor, cooperative cancellation, 7040 overlap comparisons + NBT/empty buckets, partial rollback, hill formula). Отдельно #275 **99852**, #271 **39253 + 2M histories**, #272 **76** PASS. Всего **229882 native checks**, без миров/клиента/сервера. Число assert-ов #275 уменьшилось относительно исторического отчёта, поскольку Cursor теперь не читает нетронутую часть rollback; результат транзакций/восстановления проверяется тем же fixture, цифра не является метрикой производительности.

Offline до/после: **97200 → 3540** проверок Fill-кандидатов на одинаковых 405 clips; maps совпали. Это не ускорение FPS/MSPT или wall time. Частичный rollback: прежний проход по 1000 desired positions заменён проходом по 0/1/7 реально записанным positions; fixture подтверждает завершение за writes+1 шагов и восстановление блоков. Реальные latency до/после: NOT_RUN.

Production JAR: `D:/slavic-myths/build/libs/slavicmyths-2.0.1-HELL.jar`, версия **2.0.1-HELL**, SHA256 **ba2ae12661a0b4a1b874bb9c13c40d665dc6c1047a6500a41c9ed38d760c07b0**. 8001 ресурсов, 4402 JSON, 1443 PNG, 1886 models, 1942 Java21 classes; offline fixtures отсутствуют. Ресурсы совпадают с baseline #272. Предварительный JAR из build-first.log заменён окончательным, устанавливать следует только этот SHA после отдельного разрешения.

`checks.log`: все пять source/resource/JAR guards exit0. `offline.log`: фактические результаты fixture и build. `receipt.json`: SHA production-файлов и артефакта. `files.txt`: полный перечень файлов пакета. Source commit — коммит публикации этого каталога в hell (SHA сообщается в финальном ответе; self-referential commit SHA не записывается внутрь коммита).

Итог: технические build/offline gates PASS; actual gameplay/performance/tickets/unload/MP/restart NOT_RUN. #274 остаётся OPEN / IN_PROGRESS, Project «В работе», OWNER_PENDING. Ни установки, ни итоговой передачи на игровую приёмку как полностью исправленного пакета нет.
