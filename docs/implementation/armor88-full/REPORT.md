## Состояние общей рабочей копии после параллельной работы

После clean build46 второй агент продолжил эффекты в общей47. Все принадлежащие
задаче88 Java/PNG побайтово совпадают со снимком46; различаются только три файла
эффектов и версия. Проверены production JAR47, source snapshot второго агента,
CRC/metadata/7442 resources/1742 Java21 classes, отсутствие offline fixture.
[Отдельная квитанция47](shared47-verification.json). Защищённая от следующего clean
копия JAR47: `.tools/armor88-artifacts/slavicmyths-1.9.7.47.jar`,
SHA256 `a7bf65fc0260c185f0196d099938156662c93bcdb1b2b6f435c4625cfc7a5ce3`.

Ниже приведён **неизменяемый отчёт46** и результат clean build, выполненного задачей
брони; его прежний JAR в build/libs заменён параллельной сборкой47. Native meshes,
PNG, per-ID SHA и все source A/B остаются актуальными для брони47. Игровые гейты и
приёмка остаются NOT_RUN. Тестовую45 установил другой агент; задача88 не доставляла
финальный PolyMC. Поздний фикс46/47 относительно установленной45 — вынос пояса
поножей неукротимости на0.06px. Коммит/публикация/закрытия не выполнялись.

Для замороженной46 используется verify_armor88_full.py. Для текущей общей47:
`python tools/verify_armor88_shared.py`. Не считать эти snapshots принятым релизом.

# #88 — броня и одежда, source 1.9.7.46

**IN_PROGRESS / IN_GAME_OWNER_PENDING.** Реализационные пакеты #122–#128 охватывают все **29 носимых ID**. Для #129 собраны технические результаты, финальная приёмка заблокирована до решения владельца по A1–A7. Ни одна Issue не закрывалась и не отмечалась принятой.

[29-ID аудит](AUDIT.md) · [old/new SHA, source paths, slots](inventory.json) · [иконки 1×/3×](ICONS-1x-3x.png).

[Ансамбль народной одежды](folk_ensemble.png) · [база/богатырь/трофеи](REDESIGN-CONTACT.png) · [полировка сетов](SET-POLISH-CONTACT.png).

По каждому ID: 5 ракурсов × Steve/Alex × idle/walk/raised arm/sneak = 40 A/B пар. Фактические нативные ModelPart/UV/PNG, одинаковые камера/масштаб/фазы, offline rasterizer с depth buffer. Это НЕ игровые скриншоты; предмет в руке не рисуется. Actual shaders/игровая камера не симулируются.

Одежда разделена по деталям, подолы и штанины следуют ногам, низ портов отделён от лаптей. На бронестойках подключена та же фабрика семи вещей; другие head items сохраняют vanilla path. Базовые доспехи — две разные конструкции. Богатырь — раздельные ламели вместо полос. Ветер сохраняет боковые перья, скороходы получили дорожный сапог с красным отворотом.

Перунит/Горыныч/Полоз сохраняют внешний характер, рога/шипы и наружные габариты; исправлены внутренние наложения и стыки. У драконьих сапог исключена лишняя полная штанина; поножам оставлен пояс вместо полного нагрудника. [Координаты копланарных пересечений](coplanar-intersections.json) — source evidence, не runtime-тест. Трофеи имеют отдельные рёбра/хвосты/воинский стяг.

## Дочерние матрицы

[122 одежда](ISSUE-122.md) → [123 база](ISSUE-123.md) → [124 уникальные](ISSUE-124.md) → [125 Перунит](ISSUE-125.md) → [126 Горыныч](ISSUE-126.md) → [127 Полоз](ISSUE-127.md) → [128 трофеи](ISSUE-128.md) → [129 пред-проверка, приёмка не завершена](ISSUE-129.md).

Партии ограничены 1–4 ID; реализация брони последовательная. При работе второго агента44 подготовка шла в изолированной рабочей копии. Его RuneGlowBuffers.clothing сохранён при объединении, эффекты/зачарования44 не откатывались. 13 аксессуаров и #130 не изменены. Эта задача не меняет registry IDs, stats, durability, способности, выдачу, loot/recipes, имена, NBT и creative order.

## Проверки и ограничения

Java21 clean build, native bake/UV29 ID и rune104 plain-JVM regression PASS. Production JAR: 7442 ресурсов, 1339 PNG, 1742 Java21 classes; CRC/parity/metadata PASS. Scoped art overlay идемпотентен и подключён к legacy entrypoints. [Квитанция](verification.json), [BUILD.log](BUILD.log).

JAR: `build/libs/slavicmyths-1.9.7.46.jar`; SHA256 `d31f0946e8a2b3833b1a4770a0ab9e4e141155f5dfdb885df01ab44d8109c222`. Source base `69a2db12de4baac9cef7c43e6c4b0ae717d3b644` + **UNCOMMITTED** changes; [build inputs](build-source-sha256.json). Автоматического commit/push нет.

Запусков Minecraft/PolyMC/сервера/GameTest **0**. [Игровая матрица](RUNTIME-MATRIX.md): clipping/z-fighting, настоящее движение/оружие, смешанные вещи, event бронестойки, first/third person, trim/runes/enchants, relog/MP — **NOT_RUN**. Bake/компиляция не доказывают реальный renderer event. Ручная приёмка остаётся обязательной; #88 не объявляется DONE/ACCEPTED/релизом.

**POLYMC_NOT_INSTALLED для46.** Параллельный агент по своему поручению установил45 с основной переработкой брони; его квитанция ../polymc-19745/installed.json. Эта поздняя46 дополнительно разводит на 0.06px пояс и верх поножей неукротимости. Задача88 сама JAR в instance не копировала и миры не меняла.

## Воспроизведение

JAVA_HOME только для команды: C:/Program Files/Java/jdk-21.0.12.

`gradlew.bat clean build -I tools/armor88-full-offline.gradle exportArmor88Full -I tools/rune104-offline.gradle checkRune104Offline`

Затем `python tools/armor88_full_preview.py`, `python tools/armor88_overlap_check.py`, `python tools/verify_armor88_full.py`, `python tools/armor88_report.py`.

Baseline43 сохранён в before/ и before-native.json; pinned API/source SHA — source-dependencies.json. Новый export пишет after-native.json: не заменять baseline новой фабрикой.
