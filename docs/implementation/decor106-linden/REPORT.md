# #91 → #106 · пилот липы · 21.10.2026

**Исторический пилот.** После положительной оценки липы владелец явно поручил
все шесть семейств вместе с производными поверхностями, без промежуточных остановок.
[Актуальный общий результат/сборка](../decor106-all-woods/REPORT.md).

**IN_PROGRESS / PILOT_OWNER_PENDING / Issue OPEN.** Реализован и технически проверен только первый пилот wood_linden. Вся #106 не передаётся в «Тестирование»: после принятия пилота предстоят ива и адресная диагностика четырёх других семейств. #107–108 зависят от результата древесины; #109 отдельный QA-only. Эти работы не запускались. Курганный V2 сохранён.

## Основание / scope

Владелец явно поручил реализацию декора после трофеев. Прочитаны актуальные #91/#106–109 и комментарии, AGENTS/project docs, PIXEL_ART_GUIDE. Обязательный новый `DECOR_91_WOOD_APPROVED_DIRECTION.svg` прочитан из GitHub main и добавлен локально; [визуализация референса](reference-board.png) — **схема направления, не production и не A/B**. Прежние concept/панель ракурсов использованы как вспомогательный контекст. По точному ТЗ #106 начинаем с липы, не с массовой перерисовки85 записей.

Это умеренная коррекция фактуры в уже утверждённом направлении: **точные исходные палитры и16px сохранены**, нет смены характера/тона породы, геометрии или нового материала. Прерываются механические волокна. Горизонтальные строительные стыки досок сохранены как осмысленная конструкция. Ни референсная плитка1:1, ни шумовой генератор не копировались в production. Два существующих торца уже читаются как кольца: KEEP.

Source baseline HEAD `69a2db12de4baac9cef7c43e6c4b0ae717d3b644` + dirty tree. [Все исходные SHA src/tools/status docs](workspace-before.json), [status до работы](git-status-before.txt), [наш минимальный diff](workspace-diff.json). Других активных Codex writer в доступных чатах не обнаружено; существующий advancements worktree не изменён. Чужие правки брони/рун и результаты #110/#111 byte-exact сохранены. Commit/push/ветки/PR не создавались.

## Реальная карта source / решения

| Registry ID / поверхность | Actual source | Defect | FIX/KEEP |
|---|---|---|---|
| linden_planks | models/block/linden_planks.json → cube_all → textures/block/linden_planks.png | длинные непрерывные ряды блика/однотипные диагональные полоски волокон | FIX: короткие смещённые кластеры, стыки досок неизменны |
| linden_log / linden_wood, side | cube_column/cube_column textures side/end → textures/block/linden_log.png | вертикальные борозды повторяются почти на всю высоту с одним сдвигом | FIX: мягкие разрывы и смещения без случайного шума |
| linden_stripped_log / linden_stripped_wood, side | соответствующие реальные model JSON → textures/block/linden_stripped_log.png | регулярные сплошные светлые/тёмные вертикальные линии | FIX: спокойные короткие смещённые участки волокна |
| linden_log, top/bottom | textures/block/linden_log_top.png | доказанного дефекта длинных полос нет, кольца различимы | KEEP exact bytes |
| linden_stripped_log, top/bottom | textures/block/linden_stripped_log_top.png | кольца уже соответствуют поверхности, нет причины менять | KEEP exact bytes |

Все пути выше относительно `src/main/resources/assets/slavicmyths/`. Для wood/stripped_wood весь куб использует соответствующий side material, отдельного нового PNG нет. Original/new SHA256, exact size/palette и измерения длины тёмных сегментов — [verification.json](verification.json). **Изменены ровно3 production PNG**, исходные модели/UV/state/renderer не менялись. Две KEEP поверхности сохранены побайтово.

## Actual A/B / контроль зависимых отображений

- [Компактный пилот: слоты1×/3×](pilot-strip.png).
- [Реальные native PNG /3× /2×2 tiling, до и после](surfaces-and-2x2-before-after.png), [сохранённые baseline PNG](before/).
- [14 реальных item model цепочек до/после](dependent-models-before-after.png), [бревно с четырёх ракурсов](log-four-views.png).
- [Пути всех blockstates/models/textures и прямые consumers](dependencies.json), [параметры source рендера](rendered-models.json).

Все панели **OFFLINE**, не игровые screenshots и не заявленная world-проверка. `decor106_preview.py` разрешает actual модовые модели и vanilla parents из установленного `minecraft_1.21.1_client.jar`, наследуемые display transforms и face UV. Для multi-texture моделей строится memory-only atlas из точных source pixels с UV remap; в production atlas не пишется. Orthographic source renderer использует одинаковые камеры/масштаб до/после, nearest sampling, без Minecraft AO/shaders. 2×2 — повторение фактических текстур, не мир Minecraft; четыре camera yaw не подменяют runtime-проверку размещения.

В каталоге липы15 записей, из них14 timber предметов проверены; foliage `linden_leaves` оставлена вне коррекции древесины. Проверены item parents и ссылки всех variants/multipart для14 ID. Текстуры planks автоматически влияют на stairs/slab/fence/gate/pressure_plate/button и существующую мебель, использующую этот источник. Прямых model consumers источников: **42**; это не число исправленных предметов.

Door/trapdoor/sign используют собственные independent PNG/item/atlases, они **не перекрашивались** и в A/B закономерно неизменны. Эта проверка не объявляет #107 выполненной. Настенная/напольная tabличка, text, redstone, interaction требуют реального игрового теста. Мебельные модели, storage и NBT не менялись; #108 не исполнялась.

## Воспроизводимость / инженерные gates

`tools/decor106_linden.py` содержит явные авторские pixel maps3 FIX. `write_owned` принимает **только** эти три `textures/block` имени. `woodlands_0801.texture` имеет scoped hook перед legacy fallback; листья/саженцы и другие породы/texture atlases не перехватываются. Изолированный тест исполняет реальную FunctionDef в temp-root; полный legacy generate (старые схемы1.16.5/прочие side effects) **не запускался**. KEEP торцы и остальные ресурсы не регенерировались.

Java21 `JAVA_HOME=C:/Program Files/Java/jdk-21.0.12`; `gradlew.bat clean build`: **PASS за48s**, compileJava FROM-CACHE, ресурсы/JAR пересобраны. [Лог](build.log).

`python tools/decor106_preview.py` и `python tools/verify_decor106.py`: PASS применимых offline checks. **7442 production resources** совпадают с JAR; **3947 JSON** разобраны, **1339 PNG** декодированы. TOML — parity с processed resource после штатного version expansion; fixtures/tools в production отсутствуют. Полный baseline src/tools byte audit подтверждает whitelist и отсутствие добавления/удаления source/resource файлов. [Результаты](checks.log).

- Metadata version: **1.9.7.48**, оставлен существующий номер, это локальный pilot candidate.
- Production: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.48.jar`
- Сохранённая копия пилота: `D:/slavic-myths/build/decor106-linden/slavicmyths-1.9.7.48-decor106-linden-pilot.jar`
- Размер: **23506006 bytes**
- SHA256: `fb69dc502b6f162ecbc6fadc1155bc45c0fea9b213fcb9613f6802b5641ddcaf`
- Source: указанный HEAD + dirty snapshot/diff; нового commit нет.
- **POLYMC_NOT_INSTALLED**. Установленный48 не обновлялся; worlds/backup чужого installed JAR не затрагивались.
- Minecraft/dev-client/GameTest/server/PolyMC запусков: **0**.

## Матрица REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
|Первый пакет wood_linden, утверждённое направление |актуальная#106/reference board/source diagnosis |PASS |
|3 реальных FIX,2 KEEP |production PNG,actual A/B,SHA |PASS offline |
|Native16px, исходные палитры, короткие волокна |manifest/карты/поверхности2×2 |PASS offline |
|Item1×/3×,side/top/bottom/four views |actual source render panels |PASS offline |
|14 timber dependency/state graphs |dependencies.json/unchanged models |PASS |
|No gameplay/geometry/ID/NBT/чужих изменений |полный baseline hash audit |PASS |
|Scoped reproducible generation |actual legacy FunctionDef temp-root |PASS |
|Java21 clean build/resources/JAR |build/checks logs,exact artifact |PASS |
|Принятие пилота владельцем |не выдано |NOT_RUN / PILOT_OWNER_PENDING |
|Ива/остальные4 породы |поТЗ после принятия липы |NOT_RUN / ещё не начаты |
|Реальная world2×2/размещение/ориентации/свет/redstone |запусков0 |NOT_RUN / OWNER_PENDING |
|Project «В работе» |коннектор без Projects mutations, доступная сессия read-only |BLOCKED_ACCESS; статус не заявлен изменённым |
|PolyMC installation |не выполнялась |NOT_RUN / POLYMC_NOT_INSTALLED |

Пилот технически подготовлен; **вся #106 остаётся IN_PROGRESS**, не DONE/не ACCEPTED и не «Тестирование» всей цепочки. #91 не закрыта.

## Ручная проверка / следующее действие

Владелец сравнивает пилот липы с approved direction и подтверждает/возвращает конкретные PNG. **Точный порядок #106:** «После принятия пилотного семейства повторить для следующего, только если нужна доработка». Поэтому ива и соседние этапы сейчас не начаты автоматически.

После разрешённой установки в отдельную копию профиля: проверить точный SHA/один JAR; посмотреть дерево липы рядом с ивой/яблоней/сосной/рябиной, не менять пользовательские миры. В Creative обычный GUI scale —14 timber items; в мире стены2×2/полы, направления оси log x/y/z, side/top/bottom, повороты stairs/slab/fence/gate. Проверить сохранность торцов и стыков, обычный дневной/ночной свет. Door/trapdoor/button/pressure_plate/sign — реальные размещение/повороты/редстоун/текст; не считать этот offline проход их игровой приёмкой. При замечании указать ID/PNG/ракурс/screenshot. Затем решение по продолжению пилотов.
