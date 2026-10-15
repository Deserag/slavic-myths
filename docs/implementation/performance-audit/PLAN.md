# План точечных оптимизаций

Не реализовывать все пункты сразу. Не менять gameplay/баланс/registry IDs/модели. Ожидаемый эффект ниже — гипотеза направления; проценты ускорения без before/after не назначены.

|Приоритет|Отдельная работа|Измеримая приёмка|Регрессии|
|---|---|---|---|
|P0|Расширить уже снятые ground native cycles: одинаковая камера/ручной Survival/3 независимых процесса, обычный наземный worldgen маршрут с no-mod контролем, 2 real players|Сохранить commit/JAR/settings/seed; ≥60s warmup + ≥180s sample ×3, MSPT/frame p95/p99, JFR stack/alloc, packets и после-GC heap. P0 crash/leak пока не подтверждён.|Настоящие фазы, награды, relog, очистка, loaded chunks/tickets|
|P1|GuardianClient.cube: убрать временные массивы, оставить те же24vertices и render semantics|Измерить allocation-by-site и frame p95/p99 с теми же эффекторными сценами, не обещать процентFPS. Профиль первой серии подтверждает allocation site.|UV/normals/colors/light, облака/снаряды/шипы, screenshot parity; никакого сокращения геометрии|
|P1|Poloz audio/particle saturation: сравнить идентичные terrain и128-native-control сцены; не снижать плотность визуала без согласования|Подтвердить источник sound pool247 и ~9800 particles через события/профиль; устранить только доказанное дублирование или избыточные allocation, before/after хвосты|Сохранить разрушение, звуки, lifetime, hit feedback и multiplayer delivery|
|P1|Poloz TRACK serialization, отдельный perf PR при подтверждении allocation/network cost|Снижение allocation bytes/s или serialization CPU по сравнению с тем же baseline; p95/p99 не ухудшаются. Заранее зафиксировать noise envelope по трём повторениям.|Все части, collision/hit, плавность, MP, teleport, reload, модель без изменений|
|P1|Dense-combat ClassUltimateRuntime damage fast path|Профиль identical damage traffic с ACTIVE empty/nonempty, стоимость handler и throughput, без изменения HP results.|12 IV, friendly/hostile, secondary guard, PvP, 2 клиента|
|P1|Poloz local targeting temporary-distance reuse|Снижение inclusive targeting CPU, те же выбранные цели при перестановке entity order; те же TPS и кадры.|LOS, multipart nearest, allied/excluded players, no forced loads|
|P1|Spawn predicate profiling и existence-only queries|Реальная frequency→cost, block reads/attempt, chunk generation p95 и spawn distribution на одинаковых seeds.|Не менять шанс/биомы/дистанции/домашние блоки/популяцию|
|P2|Client allocation/render state caching при подтверждённом стеке|Frame p95/p99 и sampled allocation на фиксированной камере, screenshots parity; GPU time отдельно от CPU.|Горыныч/Полоз/Морозко/Карачун, glow, позы, обе руки, частицы|
|P2|Cleanup lifecycle evidence и адресный фикс UUID/queues только при воспроизведении|5–10 полных циклов; outstanding jobs, tickets, emitters, retained entities, после-GC heap slope.|death/despawn/unload/logout/dimension/restart, summon caps|
|P2|Durable save profiling|I/O pauses/transaction duration, сравнение по одинаковому состоянию сохранения.|Защита от дюпа, crash windows, reward idempotence; никакого удаления save без эквивалентной durability|

## Git / release hygiene: отдельная работа, без удаления

`inventory.json` содержит фактические размеры **tracked blobs текущего дерева**, не размер history/clone и не FPS. Manifest ниже — предложения категории, не разрешение удалять.

|Путь/категория|Предложение|Источник восстановления и обязательная проверка|
|---|---|---|
|src/, build scripts, генераторы, mod resources|KEEP|main commit + Git remote; все generated resources воспроизводимы|
|docs/ активные отчёты и raw textual evidence|KEEP|Ссылки из PROJECT_STATUS/ROADMAP/ARCHITECTURE, hashes; не терять отрицательные результаты|
|release/*.jar и исторические zip|RELEASE_ASSET_CANDIDATE|Сначала manifest filename/size/SHA256/version/source commit, загрузка в release, проверка обратного скачивания, ссылка из docs; сейчас KEEP|
|work/ tracked результаты/бинарники|ARCHIVE_CANDIDATE|Установить владельца/происхождение/ссылки; независимый проверенный архив; сейчас KEEP|
|art/ исходники, reference и generators|KEEP / REVIEW|Авторство и лицензионные материалы, единственный оригинал нельзя терять|
|Большие повторные logs/profiles/screenshots|ARCHIVE_CANDIDATE|Побайтный digest, ссылка из reports, восстановление и воспроизводимость; сейчас KEEP|
|Новые локальные JFR этого аудита|LOCAL_PROFILE + COMMITTED_SUMMARY|SHA256 и размер в jfr-summary, профиль по локальному пути из REPORT; бинарный дамп не публиковать без проверки приватности|

Никаких `git rm`, удаления файлов, history rewrite, переустановки PolyMC или изменения миров этим PR. Измерение времени clone/CI отдельно, не объединять с игровыми метриками.

## Краткая ручная приёмка следующего этапа

1. Зафиксировать окончательный main после решений по #24/#25/#27; воспроизвести baseline без интеграции незавершённого баланса.
2. Снять полные Survival fights с записью camera/phase/action/damage/loot и 2-client dedicated, повторить трижды.
3. Измерить normal new/revisit маршруты и конкретные структуры отдельно; считать новые чанки, а не прирост loaded chunks.
4. Сравнить конкретный маленький patch с неизменённым baseline; проверить механику и visual parity до публикации результата.

[Поимённый manifest94 файлов](hygiene-manifest.json): path/size/SHA256/Git blob/восстановление; все решения KEEP, ссылки и внешние архивы ещё не проверены. 18 групп идентичных blobs≥1MiB содержат45 732 138 повторных логических bytes; Git pack уже может дедуплицировать их, это не экономия клона и не FPS.
