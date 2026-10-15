# Полоз: происхождение звуковой и particle нагрузки — #34

15 октября2026, main `fb2994e958911169317523e1066b8a1ca7a155af`,1.9.7.5. Отдельная ветка `codex/poloz-effects-investigation`. PR#32 используется как baseline/reference, его незамерженный production-код не импортируется. Main повторно проверен перед публикацией; #33, #32, #31, #27, #25 и #24 не интегрировались. **Production не изменён: никакого снижения качества, количества или длительности эффектов, звуков, моделей, анимаций, разрушений, урона или наград.**

## Вывод

У исходного симптома есть **два независимо воспроизведённых источника**:

1. **CONFIRMED_VANILLA_OR_FIXTURE_CAUSE** для массовой сцены B:128 native pillagers без Полоза/активного события дали34489 отказов sound handle за полный105s сценарий. Запросы и ошибки относятся к native crossbow loading/shoot/hit, шагам и hurt. Это контроль без босса **с установленным модом**, не чистая Minecraft без Slavic Myths. Результат не доказывает ошибку всех естественных массовых боёв: HP1000 продлевает взаимный обстрел плотной искусственной группы.
2. **CONFIRMED_MOD_CAUSE: намеренное разрушение → native эффекты**, а не доказанное дублирование, для C.2178 успешных разрушений с source=GreatPoloz дали ровно139392 TerrainParticle =2178×64. Для D:1942×64=124288. Native Level.destroyBlock→level event2001→LevelRenderer→ParticleEngine.destroy создаёт один стандартный burst на полный блок. Ошибки C —91 grass.break +168 gravel.break, всего259. Других sound event в failures C нет. Это причинная атрибуция нагрузки, **не основание удалить/ослабить честные эффекты**.

Peak C:14790 tracked particles, phase3/ACTIVE, живая цель и HP200; [корреляция](peak-correlation.json). Peak D11818 произошёл в warmup (age120), phase1, target=pillager; не выдаётся за peak sampled окна. Исторические ~9800 из#32 — не число пакетов, draw calls или поверхностей. В этом отдельном fixture измерен другой peak.

**Однозначного лишнего terrain dispatch не обнаружено:** равенство64 частиц на каждое успешное разрушение и duplicate-add=0. Последний счётчик проверяет повторную постановку **того же объекта**, а не все возможные семантические дубликаты разных объектов. Производственный фикс не внесён, отдельный implementation PR для неподтверждённого дублирования не создаётся.

## Реальный прогон и метод

**1 dev client +1 integrated server**, запуск13:48:42MSK;591.444s JFR. Все шесть сцен COMPLETE,0 dedicated/2-client/GameTest. Предыдущий наш клиент запущен13:33:01; интервал15m40s. Никаких пользовательских миров/PolyMC. Свежий FLAT seed340015, night18000, normal difficulty, natural spawning/daylight off. Фиксированная камера `(0,-58,-28)`, yaw/pitch0, обычный грунтY=-60. Игрокsurvival, invulnerable=false, ResistanceV; HP/урон входящих атак не являются Survival балансом.

Ryzen5 5600H,16GB RAM, Radeon RX6600M (runtime renderer string).1280×720, rd8/sim5, VSync off, maximum slider260 фактически uncapped; Java21.0.12, heap2–4GiB. Minecraft1.21.1/NeoForge21.1.255, Curios9.5.1+1.21.1, JEI19.51.0.418; без оптимизационных add-ons. Нет uninstrumented baseline для overhead счётчиков/JFR/CSV.

- A: idle, затем32 native zombies. B:128 native pillagers. C: один accepted command-spawn encounter Полоза. D: тот же Полоз плюс128 pillagers. E: остановка источников fixture cleanup и ожидание expiry. B/D одинаковые начальные позиции/предметы/HP1000, но дальнейшее поведение меняет бой с боссом. Сцены не равны контролю с полной независимостью исходов.
- Каждая A–D сцена300 warmup +1800 sampled ticks (15+90s); E300+800 (15+40s). Между ними восстановлены те же три слоя исходного плоского грунта97×97, удалены предыдущие сущности. Подготовка вне sampled tick окон; JFR помечает её transition.
- C/D используют состояние события и native сущность как существующая `/test poloz_spawn land`, не прохождение алтаря. Scripted native hurt100 раз40 ticks после warmup; не принудительная установка фаз. [Combat CSV](native/combat.csv) подтверждает1/2/3, alive/action/target, фактические позиции и очереди. C709 active-action ticks/1380 target ticks; D608/1208. Счётчики фильтруют alive, не считают stale action мёртвого актёра.
- C/D завершились DEFEATED и tickets=0 **до следующей принудительной очистки**. Получение всех loot/rewards и естественное удаление трупа не проверялись. Scripted damage/HP1000 меняют workload, не production.

## Измерения

[Полная таблица FPS/MSPT/TPS](METRICS.md), [машиночитаемые типы, calls/s и peaks](metrics.json). Server tick body — ServerTickEvent.Pre/Post, не гарантированно вся работа прочих Post subscribers. TPS из интервалов, capped20. FPS=1000/mean RenderFrameEvent.Post interval; p95/p99 nearest-rank, не GPU time/p1-low. Исключён первый frame с переехавшей меткой scene при сбросе возраста; сырые строки сохранены.

|Сцена|Tick avg /p99 ms|FPS /frame p99 ms|Pool failures полный сценарий|Tracked particles peak (включая warmup)|
|---|---|---|---:|---:|
|A idle|1.48 /2.52|1965.5 /1.98|0|9|
|A32 zombies|2.53 /4.60|1623.9 /1.88|0|9|
|B128 pillagers|5.95 /9.93|194.6 /10.58|34489|10|
|C Полоз|4.11 /15.58|659.5 /5.27|259|14790|
|D Полоз+128|9.71 /17.72|190.7 /12.21|19864|11818|
|E expiry|1.41 /2.88|1413.7 /2.71|0|9|

TPS всех sampled сцен20.00. В METRICS отдельные failures **по observed sampled endpoints**, поэтому30575 B и17293 D отличаются от полных34489/19864. Это разные окна, не потерянные ошибки. Rates используют фактическую разность cumulative counters и tick endpoints, примерно89s, не обещание равномерного потока.

|Источник|Измеренные запросы/создания в секунду sampled окна|Доказательство|
|---|---:|---|
|B crossbow.loading_start /middle|181.80 /159.25|Native SoundEngine.play event IDs; failures по каждому ID в events.txt|
|B grass.step /loading_end|71.49 /45.25|Обычные native сущности, boss отсутствует, event IDLE|
|C TerrainParticle|1566.20|Всего139392;2178 boss-sourced block destroys|
|C gravel.break /grass.break|15.76 /8.71|Native event2001 break sounds; только они в failures C|
|C mor_spark|12.75|Всего2071, отдельный particle registry ID; не большинство terrain particles|
|D loading_start /middle|112.09 /96.28|Другой исход обстрела/целей; не складывать эффекты B+C линейно|
|D TerrainParticle|762.25|Часть124288 созданий пришлась на warmup; rate не равен full-scene mean|

Снимки: [B](native/B_128pillagers.png), [C](native/C_poloz.png), [D](native/D_poloz_128pillagers.png). Они подтверждают сцены, не заменяют telemetry/доказательство полной игровой приёмки.

## Инструменты, звук и lifetime

Opt-in отдельный мод/sourceSet `tools/poloz-audit`, только в диагностическом клиенте. Mixins не отменяют/не меняют вызовы. Считаются create/add ParticleEngine, native Level.destroyBlock success/source, SoundEngine.play/explicit stop/удаление из instanceToChannel; логгер привязывает failed handle к текущему sound event. Первый caller по типу/ID сохранён; это **не полная история caller каждого вызова и не привязка каждого sound packet к entity UUID**. Рядом с производством телеметрии нет.

В [observations.csv](native/observations.csv) значения зависят от kind:

- particle: cumulative created, observed removed, tracked alive/queued, сумма observed residence ticks;
- sound: play requests, explicit stop calls, tracked instanceToChannel handles, native map removals;
- engine: все tracked particles, map handles, cumulative pool warnings, cumulative handle failures.

Snapshot примерно раз20ticks. Residence округлён моментом наблюдаемого удаления, включает eviction/другие причины отсутствия в engine queue; это **не точная configured lifetime частицы**. Registry ID есть для createParticle; direct terrain additions обозначены runtime class. Emitter атрибутирован первым caller/type; индивидуальный per-emitter/entity lifetime полностью не измерен. Native channels map может включать stopped/pending-delete handles, поэтому peaks342/352 **не означают342 физически одновременно играющих OpenAL sources** при native pool247. Точное физическое playing-count NOT_MEASURED.

C: TerrainParticle и mor_spark дошли до0 ещё до fixture cleanup. E после остановки источников заканчивается0 channels,0 boss-specific particles; остаются6 entity_effect игрока, как в idle. Это наблюдаемое expiry, не retained-heap доказательство и не restart test. Контроллеры/tickets долгих циклов и corpse unload не приняты по одному проходу.

JFR:169 GC, суммарные паузы745.7958ms, max80.0687ms за **всю запись**, включая warmup/transitions. [Summary по сценам](native/jfr-summary.tsv), [GC](native/gc.csv), [heap](native/gc-heap.csv). Inclusive samples пересекаются; allocation weights оценочные. Высокий uncapped FPS и тестовые счётчики сами создают allocation. Бинарный JFR хранится локально; [hash/размер](profiles.json) опубликованы вместо process/system metadata.

## Что исправлять дальше

1. Не удалять осколки/звуки разрушения и не уменьшать budget/скорость Полоза: это изменило бы согласованные эффекты/механику. Эта ветка — investigation, before/after производственного фикса **NOT_APPLICABLE: фикса нет**; выполнено load-on/load-off A/B/C/D/E сравнение.
2. Отдельно исследовать native CrossbowItem.onUseTick: shared item fields startSoundPlayed/midLoadSoundPlayed сбрасываются на ранней стадии зарядки, что при множестве стрелков может увеличивать повторы. Текущих event-ID/caller counters недостаточно, чтобы доказать лишний звук **на один charge cycle конкретного стрелка**. Это гипотеза, не confirmed fix-site; нужен per-shooter trace до implementation PR.
3. Если требуется оптимизировать сохранённую плотность terrain effects, отдельно профилировать native particle CPU/render allocation и эквивалентное batching; без измеренной эквивалентности и отдельного review не менять dispatch, lifetime, слышимость/радиус или геометрию.

## Проверки и ограничения

Java21 clean build +compilePolozAuditJava PASS; последний opt-in classes compile PASS. [Gates](gates.json):3919 JSON/1325 PNG/6804 resource parity/1685 Java21 class parity;221 subscribers PASS. Production JAR SHA256`77b79947b0c0df4ae2157410d7fa25c5ec225ecc9700fd603b04d3bf4cda887f` совпадает с main/PR#32; test classes/config не входят в него. Main не изменён, файлов репозитория не удаляли.

NOT_RUN: no-mod клиент, независимые повторы и обратный порядок сцен, обычный Survival/ритуал/награды, второй игрок/dedicated/PvP/network lag, реальный relog/restart,5–10 lifecycle циклов/retained heap, длительное естественное массовое событие. NOT_MEASURED: физически playing OpenAL sources, packets/bytes/s, GPU time/draw calls, все emitters по UUID, точная стоимость каждого handler и profiler overhead. Выводы ограничены зафиксированными workloads и этим железом.
