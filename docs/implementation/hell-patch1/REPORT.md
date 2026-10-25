# 24.10.2026 — HELL, первый пакет генерации: IN_PROGRESS
> История артефакта после начала Навьей рощи: прежний JAR SHA348473ec…5a48374 сохранён без изменения в `D:/slavic-myths-hell/.codex-input/hell-patch1-348473ec.jar`. Последующая сборка в build/libs может содержать незавершённую Навью рощу и иметь другой SHA; она не является прежним кандидатом или установкой PolyMC.

Прямое поручение владельца: новая ветка `hell`, пакет генерации/высоты первого патча; новые биомы не выполнять. Рабочая копия `D:/slavic-myths-hell`, база `275ecaa5345f9b374f83f0829c58df16c0e54546`. Незакоммиченная работа `D:/slavic-myths` сохранена в исходной рабочей копии. Автоматических commit/push/PR нет.

## Постановка и действительные статусы

Проверены все 157 элементов Project №9, его README и поля, связанные задачи и комментарии. Рабочие карточки: #243 (пакет), #244 (высота), #246 (рельеф), #247 (лава), #248 (совместимость). #245 повторяет ключ `nether-201-terrain`, но отсутствует на доске: не закрывается и не исполняется второй раз. #242 — программа, #249 — отдельная аура; #250–257/#261 — последующие биомы и их содержимое, вне scope. Исторические #131/#132/#133/#134/#144/#147 не заменяют рабочие карточки.

Фактическое поле Status пяти рабочих карточек изменено с «Анализ» на «В работе» и прочитано обратно. Не переведены в «Тестирование»/«Готово», Issues OPEN. В ТЗ нет согласованных точных Y; после проверки Project владелец явно выбрал «Реализовать этот вариант» для нижеследующего профиля, включая возможные швы старых/новых чанков.

## Профиль и исходники

Источник baseline — установленный официальный клиентский JAR 1.21.1; точные vanilla JSON сохранены в `vanilla/`. Никакие удалённые версии API не использовались.

| Параметр | Vanilla | HELL |
|---|---|---|
| dimension min_y / height | 0 / 256 | 0 / 256 |
| Строительство | Y=0…255 | Y=0…255 |
| noise min_y / height | 0 / 128 | 0 / 224 |
| Фактический диапазон заполнения NoiseBasedChunkGenerator | 0…127 | 0…223 |
| logical_height | 128 | 224 |
| Верх поиска новой рамки PortalForcer | 127 | 223 |
| Нижняя область bedrock по неизменному surface rule | 0…4 | 0…4 |
| Верхняя область bedrock по неизменному relative surface rule | 123…127 | 219…223 |
| Глобальный sea_level | 32 | 32 |
| Верхний полный жидкий слой до surface/features | 31 | 31 |
| coordinate_scale | 8 | 8 |

224 здесь — **224 слоя вместе с бедроком**, не 224 слоя свободного воздуха. Нижний рельеф формируется суммой трёх непрерывных вертикальных градиентов, continentalness и surface noise. Высокий свод — отдельным градиентом и erosion noise; локальные трёхмерные породы — прежним `minecraft:nether/base_3d_noise`. Применяются штатные max/add/mul, interpolation/blend_density/squeeze. Точные численные коэффициенты — параметры экспериментального прототипа, а не объявленные владельцем окончательные критерии художественной приёмки.

`NoiseBasedChunkGenerator.iterateNoiseColumn` использует реальные `noise.height` и native NoiseChunk interpolation. `WorldGenerationContext` ограничивает relative surface anchors глубиной генерации: это объясняет перенос верхнего bedrock без собственного block placement. Нативные rules, биомы и климат не переписаны. Это не новый dimension и не Java tick callback.

## Совместимость и границы

Изменены семь vanilla resource overrides: dimension type, noise settings и пять height placements руд. Перенос `below_top` расширил бы диапазон при прежнем числе попыток: малая жила древних обломков явно ограничена прежним Y=119, gold/quartz обеих разновидностей — Y=117. Нижние пределы, попытки, distributions и configured features сохранены. Крупная жила древних обломков остаётся Y=8…24 без override. Действительное число выпавших блоков зависит от новой породы и ещё не проверено в мире.

Fortress: исходный anchor Y=64 и `moveInsideHeights(48,70)`; bastion: start_height=33. Общий placement nether_complexes: spacing=27, separation=4, salt=30084232, веса 2:3. Ruined portal и player portal logic сохранены; изменился только разрешённый вертикальный поиск новой рамки через logical_height. Шаблоны, loot, спавнеры, biome lists, frequency не изменены. Доступность/сохранность конкретных структур и поддержек **не доказана** отсутствием source diff.

MinY и размер dimension/секций сохранены; нет кода миграции, удаления, очистки или регенерации сохранённых чанков. Полностью сгенерированные старые чанки имеют прежнюю крышу около Y=127, новые — около Y=223. Видимые швы и локальная несвязность ожидаемы; `blend_density` сам по себе не доказывает seamless Nether blending. Старый roof и построенные над ним базы не удаляются кодом патча. Реальная загрузка копии старого мира NOT_RUN, совместимость не объявлена PASS. Профиль не следует использовать на единственной копии рабочего мира до owner QA.

Datapack с теми же vanilla keys может перекрыть этот профиль по приоритету packs. Другие моды генерации не проверены. Новые биомы/аура/структуры/мобы/рецепты/лут/AI отсутствуют. Оформление лесов/дельт сохраняется ванильными biome features/surface rules, но фактическая узнаваемость и генерация декора требуют игры.

## Проверки

`tools/hell-offline.gradle` подключает отдельный opt-in source set. Fixture только bootstrap vanilla registries и вызывает native `getBaseColumn`; не создаёт MinecraftServer, Level, мир, чанковые saves или процесс launcher. Это **offline выборка базовых колонок**, не полный chunk generation и не GameTest. Ни один тестовый класс не должен входить в production JAR.

Пять seed: 0, 1, -8675309, 20261024, 987654321. Для каждого одинаковые четыре области 64×64 с origin (0,0), (1024,-1024), (-2048,1536), (4096,3072). Финальный шаг 1 блок: 81 920 колонок на вариант. Native base колонка не зависит от FixedBiomeSource; выбор wastes в fixture не проверяет декор или проходимость всех пяти биомов. Компонента шаг≤1 — proxy по одному выбранному нижнему полу с высотой33…109 и воздухом2, без vegetation/мобов. Это не измеренный маршрут игрока. Время вызовов не является chunk generation latency или MSPT.

Первый запуск candidate CLI не состоялся: PowerShell разделил некавыченный `-PhellSettings=...json`, Gradle сообщил `Task '.json' not found`. Повтор с кавычками прошёл. Предварительные sparse samples заменены финальными contiguous samples; не складываются с ними как новые seed.

Воспроизведение (PowerShell, JAVA_HOME только в окружении команды):

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat -I tools/hell-offline.gradle sampleHellOffline
./gradlew.bat -I tools/hell-offline.gradle sampleHellOffline '-PhellSettings=src/main/resources/data/minecraft/worldgen/noise_settings/nether.json'
python tools/generate_hell.py --check
./gradlew.bat clean build
python tools/verify_resources.py
python tools/verify_hell.py --jar build/libs/slavicmyths-2.0.0-HELL.jar
```

## Сравнение базового рельефа

Финальные `vanilla-columns.json` и `candidate-columns.json`, сводка `summary.json`, пять `section-seed-*.png` — фактические данные native interpolation, не ImageGen и не игровые кадры. В сечениях одинаковые координаты и масштаб; серый выше Y127 слева означает отсутствие vanilla noise generation. Эти рисунки не показывают surface bedrock/биомы/декор.

В исходной сплошной выборке: число колонок с непрерывным воздухом≥48 выросло с 21 029 до 81 241 из 81 920; улучшение во всех 20 областях. Средняя крупнейшая компонента нижнего пола с соседним шагом≤1 выросла с 1889,9 до 2689,35 блоков; улучшение 13/20 областей. Лавовых колонок на Y31 стало 26 407 вместо 32 750, общий объём базовой лавы вырос 315 184→442 241: сохранение уровня не означает сохранение формы/площади моря. Выборка64×64 не доказывает наличие огромных акваторий.

Первый native column timing был 5830 мс vanilla /20 137 мс uncached HELL (суммы 20 областей, без прогрева и полноценного performance benchmark). Это выявило повторные Y-независимые вычисления: включён штатный `flat_cache` для трёх horizontal noises. Данные до оптимизации сохранены в `uncached-candidate-columns.json`; после оптимизации все сохранённые сечения совпали блок в блок, а метрики 20 областей совпали с прежней выборкой; повторное суммарное время 10 999 мс (cache-comparison.json), примерно 1,89× baseline при 1,75× высоте. Это отдельные JVM-прогоны без формального benchmark. Улучшенное время функции не является замером полного чанка/MSPT/памяти; performance acceptance остаётся NOT_RUN.

## Сборка и артефакт

Java21 `gradlew.bat clean build`: **PASS, 47s**, compileJava FROM-CACHE (production Java не изменялась). `test NO-SOURCE` не выдан за suite игровых тестов. `verify_resources.py`: **PASS**, 7747 ресурсов / 4206 JSON / 1385 PNG / 1789 models / 1865 Java21 classes. Независимая проверка vanilla preservation, семи packaged overrides, отсутствия тестовых классов и воспроизводимости generator: **PASS**. Проверка `git diff --check`: PASS.

Production JAR: `D:/slavic-myths-hell/build/libs/slavicmyths-2.0.0-HELL.jar`, 24 033 408 байт. SHA256: `348473ecf31f4b94d173594b28f72679d06db305e5b7591fd31b32d3a5a48374`. Source: базовый commit 275ecaa5345f9b374f83f0829c58df16c0e54546 + **незакоммиченные** production deltas с SHA256 в `source-manifest.json`. Это не новый source commit и не установленный JAR. Все выводы команд (включая неуспешный первый candidate CLI) сохранены в `logs/*.txt`; снимок фактических Project статусов — `project-status.json`.

## Матрица обязательных результатов

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| #244 профиль 224, native codecs и base volume | noise settings, native columns, dimension codec | PASS (base terrain only) |
| #244 реальный bedrock, старый мир, порталы | Не запускались | NOT_RUN |
| #246 широкий рельеф, связность базового пола | A/B columns и численные proxy в summary | PASS (base terrain only) |
| #246 пять vanilla биомов с декором и реальные маршруты | Не запускались | NOT_RUN |
| #247 неизменный уровень лавы, более высокие базовые полости | unchanged sea_level=32, native samples | PASS (base terrain only) |
| #247 размеры действительно огромных морей, острова/скалы после features | 64×64 недостаточно для ocean-size acceptance | NOT_RUN |
| #248 неизменные шаблоны/loot/frequency/climate, старые Y руд | independent preservation/resource check | PASS (source only) |
| #248 структуры, спавнеры, ресурсы/End, портальные переходы/сохранение | Не запускались | NOT_RUN |
| clean build / production JAR / SHA / packaged resources | clean build 47s; 7747 ресурсов, Java21 classes 1865; resource-check.json и logs/hell-resources.txt | PASS |
| chunk generation time, MSPT, память сервера | Native function timing их не заменяет | NOT_RUN |
| Установка PolyMC | Не выполнялась | NOT_RUN / POLYMC_NOT_INSTALLED |
| Приёмка владельца #243/#244/#246/#247/#248 | Не получена | NOT_RUN |

Игровых запусков Minecraft/PolyMC/GameTest/сервера **0**. Текущая версия/папка PolyMC не изменялась; отдельный инстанс также не создан. Production candidate имеет версию `2.0.0-HELL`. Сборка не означает установку и не является финальной поставкой цепочки. Все пять карточек остаются **«В работе»**.

После отдельного разрешения запуска: проверить 5 seed/все биомы, верхний/нижний bedrock и лаву; fortress/bastion/ruined/player portal с координатами и лутом; копию старого мира и швы/переподключение; сравнить chunk latency/MSPT/память. Только затем оценивать переход в «Тестирование» по полному scope.
