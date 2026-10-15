# GuardianClient.cube — #33, 2026-10-15

**Статус PR: DRAFT до независимой frame/CPU проверки без window/pause confounding.**

**Подтверждено:** 38 временных массивов на вызов устранены; 24 вершины и все их атрибуты эквивалентны. **Прирост FPS не доказан.** Итоговый production patch — только `GuardianClient.cube` и два маленьких приватных helper, без кешей, новых static buffers и изменений render call sites.

Baseline: `main@fb2994e958911169317523e1066b8a1ca7a155af`, 1.9.7.5; Minecraft 1.21.1 / NeoForge 21.1.255 / official mappings / Java21. PR #32 использован как reference, его незамерженный код не переносился. Повторный fetch main перед публикацией подтвердил тот же SHA. Проверены открытые PR #36/#32/#31/#27/#25/#24/#21/#14/#13/#12/#11/#2; чужие ветки не интегрировались. Отдельная ветка `codex/guardian-cube-allocation`; main/PolyMC/пользовательские миры не менялись, auto-merge нет. Полоз — отдельный [PR #36](https://github.com/Deserag/slavic-myths/pull/36).

## Что измерено

- Независимые native dev clients `before` и `after`, каждый с integrated server. Один Fire LOCKDOWN, два Spring FLOOD/WHIRL, Prison, Echo одновременно, настоящие production renderers. Это client-only actors с фиксированными state/age, **не** проверка AI или Survival-боя.
- Seed330015, отдельный flat test world, игрок `(0,-60,-14)`, yaw0/pitch12, daytime6000/no natural spawns. 1280×720, rd8/sim5, VSync off/uncapped, AMD RX6600M, heap2–4GiB. Полные исходники fixture: [README](../../../tools/cube-audit/README.md).
- 1024 детерминированных native VertexConsumer сравнений ×24 вершины: PASS до/после. Сравниваются float bits XYZ/UV/normals, порядок/winding, packed color/alpha, NO_OVERLAY и light15728880; identity + translation/rotation/nonuniform negative scale, положительные/отрицательные и нулевые размеры. PoseStack/default normal transforms настоящие; Sink не GL. RenderType и вызовы renderers не менялись.
- Native screenshots до/после осмотрены: все пять объектов/эффекты видимы, missing/black/translucency artifacts на сохранённых кадрах не обнаружены. [До](before/scene-1501.png), [после](after/scene-1501.png). Скриншоты не pixel-identical: animation/partial ticks/HUD различаются. Строгая эквивалентность проверяется вершинами.
- ThreadMXBean: 100k warmup/function, затем6 alternating250k-call batches/function в каждой JVM. Это in-process batches, **не** шесть независимых повторов. Нативный default `VertexConsumer.setNormal` оставляет24 `Vector3f`: 576B/call. Старый путь1808B/call; разница1232B/call (−68.14%). Убраны именно собственные массивы; функция в целом не объявляется zero-allocation для любого consumer/JIT.
- JFR scene profile перезапускается после vertex/micro теста: [before summary](before/profile.jfr.summary.tsv), [after summary](after/profile.jfr.summary.tsv). До sampled float-array weight18,306,541,904B (`[F`, `[[F`, `[[[F`), после ни одного такого sampled site у cube. Остался `Vector3f` из Minecraft. Это оценка аллокаций sampling, **не** retained heap. На native scene есть подтверждение удаления исходного allocation site, а не только mock.

## Frame/tick и качество сравнения

[CSV-derived metrics](METRICS.md), [JSON](metrics.json): одинаковые поздние окна ages2401–3900, 75s после120s warmup. Ранняя часть before пересекалась с компиляцией диагностического инструмента и исключена с обеих сторон; исходные данные сохранены.

|Scene|Tick mean/p99 ms|TPS|FPS|Frame p95/p99 ms|
|---|---:|---:|---:|---:|
|before, исходный cube|1.339/2.442|20|1992.9|0.880/2.092|
|after, helper|1.317/2.312|20|965.7|1.784/2.416|

Первый after имел существенно ниже FPS. Приписывать это production patch нельзя: непрерывный window active/iconified/paused/camera trace отсутствует, независимого повтора в одинаковом состоянии окна нет. Это ограничение сохранено явно; данная пара **не** доказательство отсутствия frame regression и **не** FPS improvement claim.

Для проверки причины выполнен дополнительный within-client crossover: original → helper → immutable constant candidate → constant → helper → original, после90s warmup, по45s на блок с исключением первых5s. [Таблица/исключения](CROSSOVER.md), [interruption](crossover/interruption.json). В блоке1 сервер встал на паузу, окно наблюдалось свёрнутым; весь блок1 исключён (262159 кадров с frozen age2852). Capture recovery failed, blind input не отправлялся. Block0 до паузы оставлен для прозрачности и не используется как post-resume контроль.

|Post-resume block|Метод|FPS|Frame p95/p99 ms|Tick mean/p99 ms|
|---|---|---:|---:|---:|
|2|constant alternative|2265.8|0.705/1.698|1.120/1.675|
|3|constant alternative|2278.9|0.699/1.614|1.109/1.665|
|4|helper, итоговый алгоритм|2287.5|0.698/1.598|1.089/1.706|
|5|original reference|2214.7|0.742/1.651|1.040/1.698|

В этих коротких контрольных интервалах у helper не наблюдалась крупная CPU/frame регрессия; исходный reference сам изменился с1155.9 до2214.7 FPS между ранним/поздним блоками. Это указывает на влияние условий/JIT/окна, но конкретная причина независимого after падения **не установлена**. Контроль последовательный, JIT/order/window confounding сохраняется; 3.3% разница helper/reference не называется ускорением или статистической гарантией. Тестовый routing mixin добавляет одинаковый диагностический callback, в production его нет.

Выбран минимальный helper, уже проверенный в отдельном after и crossover, а не дополнительная immutable topology: та же устранённая аллокация без нового record/list/static storage. Альтернатива сохранена как диагностический patch; её production source не вошёл. Повторный новый игровой старт ради возврата к уже проверенному алгоритму не выполнялся.

GC всего scene JFR: before201.451s,54GC,sum pauses141.570ms,max12.584ms; after200.314s,60GC,sum96.571ms,max2.860ms. Это наблюдения whole capture, не нормализованный causal GC выигрыш: frame counts различаются, profiler overhead отдельно не измерялся. Crossover whole JFR567.648s включает паузу, поэтому его aggregate GC/bytes не сравниваются с независимыми сценами.

## Сборка и границы

Финальные `gradlew.bat clean build` и compile opt-in fixture: см. [build log](build-final.log). [Ресурсы/production JAR](gates.json), [payload diff](jar-payload-diff.json), [before bytecode](before-cube-bytecode.txt), [final bytecode](final-cube-bytecode.txt). Production bytecode не создаёт массивы в cube/helpers; все прочие production payloads идентичны main, кроме отладочных line numbers двух nested renderers. Fixture sourceSets/config не входят в JAR. Финальный production SHA256 `049c7e2dd9aca1a1ec07ffcc60d997dea80eb0b0098caf9af266c19e9fbe50fc`,19,696,896B;3919 JSON/1325 PNG/6804 resource parity/1685 class parity Java21 PASS. Первый clean после patch имел Windows JAR lock при одновременном gate, затем последовательные clean builds PASS. Исторический before/production.json содержит resource checks, но не class checks из-за раннего порядка gates; полной class-проверкой baseline он не называется. Финальный gate требует ненулевой class count. Финальный SHA также указан в gates; архивы/logs/JFR имеют [hash manifest](evidence-manifest.json), [local-only JFR hashes](profiles.json).

Реальные старты #33 (MSK):11:37:09 INVALID до мира/Title (onboarding);13:33:01 before COMPLETE;14:14:47 after COMPLETE;14:30:40 crossover COMPLETE с исключением паузы. Итого4 клиента,3 integrated servers; никаких dedicated/GameTest boots. Между before/after был отдельный #34 клиент13:48:42; интервалы всех собственных стартов≥15мин. Чужой PolyMC старт14:41:28 после завершения crossover14:40:41, пересечения с замером нет; не считался нашим тестом и не изменялся. Первоначальный неудачный запуск включён в счёт. Счёт всей пары задач #33+#34:5 собственных клиентов/4 integrated worlds.

NOT_RUN: 2-client/dedicated MP; полное Survival прохождение стражей/урон/ритуал/награды; все возможные state/animation frames; GPU/draw calls; длительный retained-heap/leak тест; независимые обратные before/after повторы с непрерывной записью visibility/camera и изолированным JIT warmup. Ресурсы/модели/анимации/звук/механики не редактировались. Маленький production patch пригоден к рассмотрению по доказанному allocation результату; строгая приёмка отсутствия FPS regression требует последнего из перечисленных контролей.

## Следующий шаг

1. Стабильный видимый клиент без паузы, trace active/iconified/screen/camera, чередование independent reference/helper JVM, достаточный render warmup; сравнить frame p95/p99 и CPU/cube, не только средний FPS.
2. По Полозу: per-shooter trace native crossbow charge flags + no-mod crowd контроль; затем отдельное исправление только доказанного повторного dispatch. Terrain particles не урезать: #36 подтвердил штатные64 частицы на успешно разрушенный полный блок.
3. Dedicated/2-client и5–10 циклов завершения/перезахода для retained memory, отдельно от текущего allocation-site исправления.
