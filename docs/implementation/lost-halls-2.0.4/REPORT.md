# Чертоги заблудших — независимая серверная инфраструктура, 28.10.2026

**IN_PROGRESS. Третий биом и его игровой цикл ещё не реализованы.**
Основание: прямое уточнение владельца в этом чате — пока PNG находятся в библиотеке
ChatGPT и ещё не переданы, выполнять независимые production-задачи, блокировать
только художественное соответствие. Это уточнение снимает прежнюю остановку
всего production до SOURCE; не разрешает выдумывать модели, текстуры и геометрию.

## Реально реализовано

`PostenEncounters.java` — сохраняемый журнал состояния боя. Отдельный arena UUID
и body UUID, четыре стабильных UUID копий и четыре маски. Состояния BODY → COPIES
→ DEFEATED, 650 HP, отсечение пограничного урона на 260 HP, затем 4×65. Остаток
пограничного overkill **отбрасывается**, а не переносится на копии. Общий запас
фазы II вычисляется из HP четырёх копий; повторный урон мёртвой копии, чужой UUID,
NaN/Infinity/неположительный урон отвергаются. Выдача разрешения одного reward batch
только после смерти всех четырёх, с сохранением флага. Это **авторизация**, ещё не
реальная выдача сердца/руны/души/головы; crash-consistent delivery остаётся задачей
интеграции настоящей death/drop pipeline. Журнал не создаёт сущности/XP/лут.

`LostHallsRooms.java` — metadata завершённых комнат S1–S6. Идемпотентная публикация
по стабильному UUID, защита от конфликтующего переноса узла, удаление по ID,
сохранение NBT. Сферический поиск ближайшего узла 48 blocks с учётом высоты;
большие залы S3/S4/S5 либо открытый проход с ≥2 выходами. Равные расстояния
разрешаются стабильным UUID независимо от порядка публикации чанков. Только
соседние 7×7 chunk-column buckets, максимум 64 узла/колонку; overflow явно
отвергается producer'у, не считается успешной публикацией. Это инженерный лимит
метаданных, не утверждённая частота комнат. Server helper проверяет hasChunkAt
перед чтением блоков, текущую опору, два свободных блока, fluids/fire/hot floor
и world border. Не вызывает getChunk(FULL), не создаёт chunks или ложные узлы.
Физическая достижимость от игрока не доказывается одной проверкой входа.

`LostHallsRouteCheck.java` — bounded проверка проходимости по конечному снимку
**фактического итогового** voxel plan. Unknown cells закрыты; обязательны опора
и 2 свободных блока; нет диагонального clipping/crawl/неподдержанных прыжков.
Перепад ≤1 блока с проверкой jump headroom. Невозможный маршрут и превышение
visit budget возвращают разные ошибки. Сам layout/классификацию collision должен
передать будущий генератор; это не 6 готовых модулей и не доказательство worldgen.
Проверка консервативна для полноразмерных клеток, stairs/slab collision ещё не
покрыты как отдельная геометрия.

Оба SavedData доступны через Nether-only server-thread accessor, без tick listener.
Неизвестная версия или повреждённая запись переводит весь журнал в read-only с
сохранением исходного NBT. Это предотвращает молчаливое пересоздание/второй лут;
повреждение требует диагностики, автоматический сброс не выполняется.

**Текущие consumers ещё отсутствуют:** нет генератора, публикующего room nodes,
предмета Камертона, entity/arena hook Постеня, bossbar или реального дропа.
В существующих мирах эти классы сами не активируются и SavedData не создают.

## Проверки

- `build-logic-final.log`: Java21 `clean build verifyLostHallsLogic`, **PASS,21s**.
  Локальный точный Curios fallback сохраняется: обычный Maven ранее timeout.
- `offline-logic.json`: **11755 assertions**, 200 расчётных боёв со случайным
  порядком ударов/смертей и binary NBT после каждого шага; это не200игровыхбоёв.
- Границы650→649→261→260→259, 4×65, overkill, отсутствие повторного split/reward,
  чужие UUID, NaN/Infinity, повреждённые/будущие NBT, стабильные copy IDs проверены.
- Room index: no-target, ±chunk coordinates, 48/49 blocks, вертикальный диапазон,
  predicate blocked/unloaded, stable ties, сохранение/удаление/column overflow;
 5000 дальних записей не вызывают block-entry predicate возле игрока.
- Voxel fixtures: границы2/4чанков и обратный порядок snapshot publication,
  lava/пустая опора/низкий потолок, путь вверх/обратно и bounded traversal.
- `resources-final.log`: **PASS**,8254resources/2019Java21classes; исходные ресурсы
  сохранены, tests/dependency classes в production JAR не попали.
- Первые две offline-итерации выявили неверные сравнения fixture с NaN/порядком
  ключей NBT; стенд исправлен на сравнение канонического SNBT после binary NBT.
  Неудачные logs сохранены, это не было игровым дефектом. В route checker отдельно
  исправлен ранний выход при достижении цели ровно на границе visit budget.

JAR `build/libs/slavicmyths-2.0.8-HELL.jar`, SHA256
`9ab1c2e7c68208b4d8dfdac077023b6859e8c7fd84ffcc670919113f7dffafa6`.
Это новый production JAR с инфраструктурой; **не готовый третий биом**.
Minecraft/client/server/GameTest/PolyMC: **0 запусков**. POLYMC_NOT_INSTALLED.

## Матрица и границы доказательств

| REQUIREMENT | REAL_SOURCE / PLAYER_PATH | TEST / EVIDENCE | RESULT |
|---|---|---|---|
| #293 P02/P05 арифметика650/260/4×65 | PostenEncounters; будущий arena encounter | binary NBT +200offline trials | PASS для backend; бой NOT_RUN |
| #293 P06/P07 единственный reward batch | claimRewards; будущая смерть4копий | повтор/чужойUUID/reload | PASS авторизация; выдача предметов BLOCKED_DEPENDENCY |
| #301 nearest registered48block node | LostHallsRooms; будущий ПКМ Камертона | loaded-predicate/index/NBT fixtures | PASS для index; world metadata/предмет BLOCKED_DEPENDENCY |
| #291 finite route check | LostHallsRouteCheck; будущий маршрут игрока | conservative voxel fixtures | PASS для алгоритма;6scenes BLOCKED_REFERENCE |
| #12 server-safe/persistence | два Nether-only SavedData | version/corruption/immutable snapshots | PASS offline; настоящий restart/MP NOT_RUN |
| #13 build/resources/JAR | реальные источники/production JAR | final logs +build-result.json | PASS с localCurios fallback |
| #289/#290 ART |blocks/scenes/mobs PNG отсутствуют| references-current.json | BLOCKED_REFERENCE |
| #292/#293/#302/#303 ART |PNG наград/Постеня/предыдущих боссов ещё не переданы| уточнение владельца | BLOCKED_REFERENCE |
| #294 worldgen/arena guarantee/natural spawn | генерация не подключена | нет игровых миров | BLOCKED_DEPENDENCY / NOT_RUN_ENV_BLOCK |
| OWNER | приёмка владельца | не состоялась | NOT_RUN |

## Как проверить в Minecraft после подключения consumers

Сверить JAR/SHA, отдельный новый Survival-мир без OP; старый мир только в копии.
Сейчас новые предметы/биом отсутствуют, проверить их этим JAR невозможно.
После интеграции: портал → естественный биом → S1–S6 и обратный пеший путь →
мирная добыча/приручение → craft метки/Камертона → доступная арена →4копии→1batch.
Негативно: no target/blocked entry, full inventory/две руки, чужой entity UUID,
повторное получение трофея. Затем настоящий unload/relog/restart, копия старого
мира,2клиента; 5seeds×4разнесённыеобласти и измерение FPS/MSPT. Всё это NOT_RUN.

## Риски/откат и следующий шаг

9457 прежних production-файлов сохранены; новые три Java source — единственная
production delta. Арт/биомы/registry IDs двух предыдущих биомов не менялись.
Новые SavedData keys не следует менять после начала реального использования.
Откат только новых адресных файлов и Maven changes, без reset и чужой master.

Точка продолжения: `PROGRESS.md`. Получить4архива из библиотеки ChatGPT, проверить
пять SHA, закончить #289/#290. Затем связать финальную геометрию с route checker
и metadata publication на серверном completion hook; entity Постеня — с ledger.
Не подменять выдачу лута флагом claimRewards или natural worldgen успешным index test.
