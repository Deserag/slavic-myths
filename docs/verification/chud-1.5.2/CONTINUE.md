## Актуальное размещение — 2026-10-09

Minecraft запускать запрещено: никаких runClient/runServer/runChudChecks/Bootstrap. Текущий JAR8181 (`placement-fix/resource-gates.json`), установлен с backup предыдущего6ae,2366 защищённых файлов неизменны. Отчёт `placement-fix/REPORT.md`.

Новый placement проверяет прежнюю точку и до9 запасных в области, выбирает один А/Б root. Горы — предпочтение, biome tags разрешают Overworld;12% фильтр лесов удалён. Исправлена нижняя граница medium/great по фактической конструкции, сохранённые bbox/NBT/геометрия не менялись. Минимальный gap независимых комплексов2298≥2000, native owner совпадает с выбранной точкой. Курганы/поселения не правились.

Clean build PASS;18387 offline assertions, включая192 immutable plans на искусственном сухом грунте Y64;12010 resource/production checks;8 resource files воспроизводятся побайтно.935 compiled classes совпали с JAR;20 saved geometry classes совпадают с CA2. Это не проверка реального worldgen:0 клиентов/0 серверов, runtime/native density/chunk-order/MSPT NOT_RUN по запрету пользователя.

ChudRegionPlanner.select — общий resolver для worldgen/поиска/будущих меток. Ограничены128 region plans/1024 finished slots/8192 invocation noise columns. Generic vanilla locate tags/cartographer не адаптированы к retry slots; exact ID commands идут через resolver. Native runtime/частота/порядок чанков остаются открытыми. Не ссылаться на14 прежних GameTests как доказательство нового генератора.

## Актуальное продолжение после исправления поиска — 2026-10-09

Последнее указание пользователя: **Minecraft запускать нельзя**, включая headless/server/GameTests. Не запускать `runChudChecks`, `runServer`, `runClient` или профиль PolyMC. Указания ниже о дальнейшей native QA — исторический план, сейчас не разрешение на запуск.

Текущий production и установленный development candidate: `build/libs/slavicmyths-1.5.2.jar`, SHA256 `6ae23610d8390cc6e5792b8ad70048851ed5ea2bca8fea7618427c5edc75e524`. Новый отчёт/сертификаты: `search-fix/`; clean build PASS,11 offline checks,11992 resource checks,926 compilation classes совпадают,21 saved geometry class совпадает с baseline. Установка backup CA2/2260 защищённых файлов PASS. Runtime нового исправления **NOT_RUN**,0 игровых запусков.

`ChudSearchCommands` заменяет обработку двух точных vanilla locate ID; `/sm … locate` читает `getChunkNow`, при отсутствии включает тот же фоновый поиск. `search [тип] [страница]`, `chud_search status/cancel/next`;96 кандидатов/60 секунд кооперативно, прогресс2 секунды, native seed planning без чанков. `next` сохраняет незавершённого кандидата/центр/фильтр. Проверка worldgen quotas1.5.2 остаётся открытой (исторически medium5/attached1/great0).

Воспроизводимый offline gate: process-local JAVA_HOME21; `gradlew.bat --project-cache-dir work/chud-152/production-cache --init-script tools/chud-search-offline.gradle clean build searchOfflineChecks`. Затем строгий resource verifier с `--report docs/verification/chud-1.5.2/search-fix`, `verify_chud_search_offline.py` с сохранённым CA2 baseline `work/chud-152/pre-search-fix/slavicmyths-1.5.2.jar`. Обычные старые native certificates не переиспользовать для нового SHA; installer `--offline-fix --development-candidate` фиксирует `OPEN_RUNTIME_NOT_RUN`.

# Продолжение 1.5.2

По последующему запросу пользователя команды подготовлены и проверенный **тестовый кандидат1.5.2** установлен в PolyMC1.21.1. Это не завершение полной приёмки1.5.2. Архив1.5.1 выполнен отдельной контрольной поставкой ранее.

## Зафиксированные результаты

- Final attempt37:14/14 native GameTests PASS,36 физических планов и пять полностью размещённых комплексов; три профиля great76/60/44.
- Native Overworld12 seed/480 кандидатов:5 medium,2 города A,1 attached,0 great. Требование четырёх suitable attached и great открыто.
- Clean build27с;11982 production/resources checks PASS. Все921 класс совпадают с компиляцией финального runtime.
- JAR `D:/slavic-myths/build/libs/slavicmyths-1.5.2.jar`, SHA256 `ca2a1e9e7ab2ebfb740edee54f7318f604a04119d5aa6f8b49c05e8699810103`.
- PolyMC: `.minecraft/mods/slavicmyths-1.5.2.jar`; прежний combat-feedback JAR в backup вне mods;214 остальных файлов сохранены. См. polymc-installation.json.
- Клиент0. Реальные headless loader запуски этой1.5.2 задачи34; отдельно1.5.1 —28. Запуски других пользовательских задач в эти числа не входят. Commit не выполнялся.

## Что осталось

1. Подтвердить естественный great и минимум четыре уникальных native attached/great случая. Fixed seed finder deduplicates candidate chunks: attempt36 дважды проверил один root и ошибочно считал два accepted attached; отчёты/сертификат считают только один уникальный seed+root. Исправленная версия с selectedCells уже компилируется в attempt37, её поиск ещё не повторялся.
2. Улучшить выбор пригодных естественных кандидатов/планирование, не ослабляя защиту жидкости/породы и не выдавая stone fixtures за natural terrain. Текущий finder оценивает только пять высотных проб на главный зал, берёт highest64 eligible/20 ranked. Полноценные production проверки часто отклоняют их из-за caves/liquids/cover. Время planning принятого среднего кандидата в measured run — несколько секунд; это отдельная открытая производительная часть, не MSPT.
3. Клиент/семь референсов в игре, survival, multiplayer, реальный save/relog старого и нового мира, lighting/mobs и измеренный MSPT пока NOT TESTED. Запуск клиента запрещён без отдельного разрешения.

После установленного кандидата **сохранить generation-v2 геометрию и ShaftProfile0/1/2 для уже записанных starts**; не менять layout восстановления прежнего seed молча. Коррекции отбора новых candidates допустимы без изменения старых layouts. Generation-v1 и старые mineGate IDs/координаты не менять.

## Возобновить rare seed finder

Сначала прочитать PROJECT_STATUS/ROADMAP/ARCHITECTURE, реальный код и git status; пользовательские изменения босса/боёв общие и должны сохраняться. Не выполнять commit и не запускать GUI.

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
.\gradlew.bat --project-cache-dir work/chud-152/gradle-cache --init-script work/chud-152/isolated-build.gradle -PchudCheck -PabandonedCheck -PabandonedFinderCheck runChudChecks *> docs/verification/chud-1.5.2/attempt38.log
```

Это отдельный test sourceSet/собственный GameTest мир. Отдельными buildDirectory **и** project-cache-dir пользоваться обязательно: общий `.gradle` ранее позволял другой сборке удалить наши main classes. Не запускать два QA сервера в `run-chud-151` одновременно. Текущие собственные процессы завершены; attempt36 прерван, не помечать весь прогон PASS.

Смотреть `native-seed-finder.json`, считать уникальные `(seed,rootComplexId)` и сохранять номера/причины фактических неудач. Известный настоящий A+B для seed11: anchor62680,74,-24104; новый Overworld, default chance35%. Все пользовательские команды — в COMMANDS.md. Vanilla `/place structure` ставит блоки, но не сохраняет native StructureStart и не заменяет worldgen-тест.

При изменении production повторить соответствующие gates, isolated clean build, verify_chud_152.py и сравнение runtime/production. Native сертификация находится в verify_chud_native_152.py; сейчас её acceptance=`OPEN_WORLDGEN_QUOTA`, а техническая часть=`PASS`. Перед дальнейшей заменой PolyMC JAR сохранять актуальную версию пользователя, миры/настройки и остальные mods.
