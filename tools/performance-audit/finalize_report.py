"""Assemble final audit notes from existing measurements; does not launch Minecraft."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[2]
O=R/'docs/implementation/performance-audit'
p=O/'REPORT.md'
s=p.read_text(encoding='utf-8-sig')
section='''## Итог всех четырёх запусков

Реально запущены **4 dev clients и 4 integrated servers**: 09:31:26, 09:46:45, 10:17:33 и 10:34:04 MSK. Второй прогон прерван из-за ошибки тестового fixture: высота незагруженного чанка дала minY, игрок оказался в пустоте. Его extended/raw сохранён как INVALID и исключён из итоговых чисел. Один запрет launch guard не запускал JVM. Наблюдавшийся чужой Minecraft стартовал 10:01:39; дождались закрытия и выдержали 15 минут. Чужие процессы не останавливались. Три валидных прогона: **17800 sampled ticks / 56 строк агрегатов**, но это не 56 независимых повторений. После четвёртого запуска Minecraft больше не запускался.

### Боссы на земле — отдельный focused-прогон

Свежий FLAT, реальная поверхность Y=-60, без воздушной платформы. По одному циклу: 200 ticks warmup +1800 sampling; cleanup 400 sampling. Игрок survival, Entity.invulnerable=false, Resistance V; native AI и атаки боссов исполнялись. Тестовые 100 damage через boss.hurt каждые40 ticks после warmup ускоряют переходы фаз: это **не ручной Survival бой, не DPS/TTK и не оценка баланса**. Карачун/Морозко используют созданные fixture native sessions, Горыныч standalone, не естественный путь крепости/ритуала. Снимки фаз и CSV/JFR дают дополнительный overhead, отдельно не измеренный.

|Босс|Средний tick body живого активного боя, ms|p95 / p99, ms|Средний FPS всей сцены|TPS всей сцены|
|---|---:|---:|---:|---:|
|Великий Полоз|3.84|5.80 / 8.45|967.8|20.00|
|Змей Горыныч|2.07|2.87 / 3.79|1006.9|20.00|
|Карачун|1.77|2.46 / 3.29|1428.4|20.00|
|Морозко|1.69|2.55 / 3.30|1375.3|20.00|

Активные окна отфильтрованы по alive/nonremoved и состоянию боя: [cycle coverage](focused/cycle-coverage.json), исходные [combat ticks](focused/raw/combat.csv). FPS относится ко всей сцене, включая ожидание после завершения; сравнивать напрямую с отфильтрованным боевым временем нельзя. Полные показатели сцены и хвосты: [focused METRICS](focused/METRICS.md), [ticks](focused/raw/ticks.csv), [frames](focused/raw/frames.csv.gz).

Подтверждены естественные переходы Полоза1/2/3, смерть и world loot (core/whip/head/scales); автоматическое удаление трупа до fixture cleanup не подтверждено. Полоз имел живую цель699 ticks и action435 ticks, значит боевой AI реально исполнялся. Горыныч прошёл0–6, три native cleanse SUCCESS, маска1/3/7, PURIFIED; получение его награды НЕ ПОДТВЕРЖДЕНО. Карачун завершился с native removal и наградами; Морозко — с native removal и наградами. Подробные состояния/инвентарь: [events](focused/raw/events.txt). Удерживаемая fixture ссылка после удаления не считается живым AI. Очистка следующей сцены до одной сущности не доказывает естественную очистку всех tickets/queues.

### Normal terrain, массовые сцены, классы и руны

[Расширенные метрики](extended2/METRICS.md), [coverage](extended2/coverage.json), [события](extended2/raw/events.txt). По3 среза100 warmup +200 sampling ticks. На боевых сценах одноразовая платформа Y=121, 128 native pillagers с целью игроком и test-only увеличенным HP. Это искусственная плотная сцена, не естественное массовое событие.

|Сцена|Средний tick body, ms по срезам|FPS по срезам|Границы|
|---|---:|---:|---|
|Полоз, terrain stress|6.11–6.84|462.8–660.2|Активные действия были в warmup; в sampled окнах target/action отсутствовали. Не sustained combat.|
|Горыныч, native phase activity|3.23–3.57|786.4–875.4|Цель/действия наблюдались; не полный Survival маршрут.|
|Classes IV +128 мобов|8.62–10.66|298.6–533.6|10/12 попыток активации successful; living_wall/false_ambush NOT_EXERCISED, не доказанный дефект production.|
|Руны +128 мобов|7.20–8.37|375.5–390.1|45 targeted native attack attempts; 6/54 рун, не полное покрытие. Один tick70.18ms.|
|Три режима арбалета +128 мобов|7.01–10.22|217.3–290.7|Native performShooting с injected charged stack; не проверка Survival ammo/reload/burst queue.|

В terrain-сценах воспроизведены предупреждения native sound pool247 / failed sound handle и до примерно9800 tracked client particles. Они встречались с Полозом и волнами128 pillagers. Это наблюдаемый симптом насыщения, но отдельного такого же no-mod/без разрушений контроля нет: точная причина и вклад мода не доказаны. Число попыток разрушения в коде не равно измеренному числу разрушений/пакетов.

### Генерация мира: только синтетический стресс

**Y=200 используется исключительно как воздушная точка телепортации для chunk-loading stress.** Каждые40 ticks fixture синхронно вызывает getChunk для удалённой точки и телепортирует игрока. Это не обычная ходьба/полёт игрока; FPS из воздуха не является обычным gameplay FPS. Измерены avg18.75/64.62/58.49ms, max0.889/5.013/5.674s, TPS20.00/14.30/16.27. Длительные блокировки воспроизведены под принудительной синхронной загрузкой, но **не атрибутированы worldgen мода**. По ChunkEvent.Load наблюдались172/166/121 новых chunks в sampled окнах; rates22.26/11.87/9.84s⁻¹ зависят от этой траектории и не равны общей скорости генератора.

Сцена worldgen_old тоже создала136/193/180 новых chunks: это revisit, не чистый контроль уже сгенерированного мира. Начальные idle47.34→23.42→5.57ms загрязнены startup/worldgen и не steady baseline. Для выводов об исследовании нужен отдельный наземный маршрут и совпадающий no-mod контроль без принудительного synchronous getChunk.

### Память и профиль поздних прогонов

Extended2 JFR473.43s:232 GC, сумма пауз4098.76ms, max199.31ms; used heap в sampled окнах примерно646–2292MiB. Focused JFR441.053s:118 GC, сумма580.83ms, max74.84ms, sampled heap458–1693MiB. Это записи со startup/warmup/переходами, не суммы пауз только sampled окон. [Extended GC](extended2/raw/gc.csv), [focused GC](focused/raw/gc.csv), streaming summaries и SHA256 находятся в соответствующих каталогах. JFR бинарники остаются локально из-за process/system metadata; публикуются summaries и hashes. Пилообразный used heap и оценочный allocation weight не доказывают retained-memory leak. 5–10 повторных полных lifecycle циклов, full-GC retained heap, длинный idle, MP/packets и relog НЕ ПРОВЕРЕНЫ.

### Матрица итогового покрытия

- MEASURED: dev FPS/frame tails, integrated tick body/interval/TPS, heap/GC/JFR,32 AI и128 pillagers, ground native boss cycles, отдельные class/rune/crossbow paths, синтетическая chunk нагрузка.
- PARTIAL:10/12 IV activations,6/54 runes, native scripted boss damage/сессии, Полоз loot и Карачун/Морозко rewards. Награда Горыныча не подтверждена.
- NOT_RUN: dedicated/2-client MP, ручные Survival маршруты, обычное исследование мира, no-mod контроль, независимые повторы, полный balance/DPS/TTK, настоящее сохранение/перезаход, долгосрочная утечка.
- NOT_MEASURED: GPU time/draw calls, bytes/packets per second, отдельная стоимость каждого handler, естественная частота спавна, влияние profiler overhead.

'''
if '## Итог всех четырёх запусков' not in s:s=s.replace('## Что означают результаты',section+'## Что означают результаты')
s=s.replace('## Матрица покрытия и границы','## Матрица первой серии (историческая, итог выше)')
s=s.replace('исправлен до первого запуска','исправлен до первого запуска')
s=s.replace('Открыты #27/#25/#24/#21/#14/#13/#12/#11/#2.','Повторно проверены main и открытые PR перед публикацией: #31/#27/#25/#24/#21/#14/#13/#12/#11/#2. #31 — только дизайн Advancements2.0, не runtime-патч; не входит в baseline.')
s=s.replace('Финальный Java21 `clean build compilePerformanceAuditJava`:PASS ([log](build-final.log))','Финальный Java21 `clean build compilePerformanceAuditJava -I tools/performance-audit/init.gradle -PauditFocused`:PASS ([log](focused-build.log)); предыдущие успешные сборки также сохранены')
s=s.replace('raw/frames.csv)', 'raw/frames.csv.gz)')
p.write_text(s,encoding='utf-8')
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=R/'docs'/name;s=p.read_text(encoding='utf-8-sig')
 if 'Независимый perf-аудит #28' not in s:
  s+='''\n\n## Независимый perf-аудит #28 — 2026-10-15

Baseline main fb2994e9 /1.9.7.5. Только opt-in tools/performance-audit и evidence, production/механики/модели/IDs не менялись. 4 реальных dev clients +4 integrated servers, один fixture run INVALID; 3 валидных прогона,17800 sampled ticks. Ground native boss cycles: активный tick body avg1.69–3.84ms; TPS20. Массовые128 мобов с классами/рунами/арбалетом avg7.01–10.66ms. Принудительная синхронная chunk нагрузка дала max5.674s, но не воспроизводит обычное исследование и не доказывает дефект мода. 10/12 IV и6/54 рун; Survival/MP/relog/retained leak NOT_RUN. Java21 clean build,3919 JSON/1325 PNG/production parity/221 subscribers PASS; audit classes отсутствуют в JAR. Оптимизации не внедрялись; next: allocation GuardianClient.cube и контролируемое исследование Poloz sound/particle pressure без сокращения визуала. [Отчёт, измерения, топ10 и план](implementation/performance-audit/REPORT.md).
'''
 p.write_text(s,encoding='utf-8')
p=O/'PLAN.md';s=p.read_text(encoding='utf-8-sig');s=s.replace('Завершить baseline: одинаковая камера/полный бой/3 независимых процесса, normal world structures, 2 real players','Расширить уже снятые ground native cycles: одинаковая камера/ручной Survival/3 независимых процесса, обычный наземный worldgen маршрут с no-mod контролем, 2 real players');s=s.replace('|P1|Poloz TRACK serialization','|P1|Poloz audio/particle saturation: сравнить идентичные terrain и128-native-control сцены; не снижать плотность визуала без согласования|Подтвердить источник sound pool247 и ~9800 particles через события/профиль; устранить только доказанное дублирование или избыточные allocation, before/after хвосты|Сохранить разрушение, звуки, lifetime, hit feedback и multiplayer delivery|\n|P1|Poloz TRACK serialization');p.write_text(s,encoding='utf-8')
p=O/'parallel-prs.json';d=json.loads(p.read_text());d['open_prs']=[31,27,25,24,21,14,13,12,11,2];d['final_main_fetch']='fb2994e958911169317523e1066b8a1ca7a155af';d['pr31']='Documentation-only Advancements 2.0 design; not integrated';p.write_text(json.dumps(d,indent=2),encoding='utf-8')
