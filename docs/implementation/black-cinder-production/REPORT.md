# Чёрная гарь 2.0.3 — реализация #261–#267, 27.10.2026

**IN_PROGRESS / OWNER_PENDING. Код и ресурсы реализованы; это не принятый релиз.**
Основание — последнее прямое поручение владельца и верхние разделы Issues,
разрешающие рабочую реализацию по восстановленному визуальному паспорту без ZIP.
Отсутствие оригиналов больше не блокировало код. `ART_ORIGINAL_COMPARE_NOT_RUN`.
Minecraft-клиент/сервер/PolyMC/GameTest: **0 запусков**. Пять seed в offline отчётах
не являются пятью запусками мира. Issues остаются OPEN, Project Status не менялся.

## Источники и сохранность

Рабочая ветка `hell`, исходный commit
`02765c5ebcbced3595181b8ada8d01cdda0e1ad0` плюс незакоммиченные изменения;
точные SHA файлов — `source-manifest.json`. Local main оставлена на8789e08d,
прочитана свежая origin/main ba5c07cb. Оттуда адресно перенесён существующий
`ZharenitsaSeals.java` с прежним ID и marker, без merge всей main. Предыдущие
незакоммиченные документы и compile-fix SerpentHillStructure сохранены. Commit,
push, PR, force/reset, новые ветки и изменения миров не выполнялись.

Четыре оригинальных PNG исполнителю по-прежнему недоступны. Файлы
`docs/concepts/black-cinder/RECOVERED_VISUAL_SPEC_2.0.3.md`,
`CODEX_2.0.3_FULL_IMPLEMENTATION.md`, `STRICT_ACCEPTANCE_2.0.3.md` сохранены локально;
актуальные #261–#267 — в `issues/`. Рабочие модели/текстуры изготовлены по паспорту,
не названы копиями PNG. Старые отчёты о BLOCKED_ALL_WORK исторические и отменены
последним поручением. Никаких synthetic/audio generators не запускалось.

## Исправлено / реализовано

**#262:** реестр реальных новых IDs; пять самостоятельных voxel rigs с UV и
суставами, source palette/5×5 tiles; native ModelPart before/after evidence:
до — эти модели отсутствовали в hell HEAD (`before-models-absent.png`), после —
`native-rigs.json`, `actual-models-six-views.png` и пять `*-actual-poses.gif`.
Виды front/left/right/back/top/3⁄4, одинаковый масштаб в пределах набора. Это
геометрия реально запекаемых ModelParts с фактическими UV/PNG, **не игровые кадры**.
По source preview доработаны разветвлённые каменные жилы, расширенная ленточная
юбка Жареницы, связи глаз/венца с головой и зубов/челюсти Углеполза.

**#263:** пять базовых материалов: `black_cinder_soil`, `ash_crust`,
`smoking_cinder`, `charred_stone`, `cracked_charred_stone`; у двух камней свои
stairs/slab/wall и stonecutting. Сами камни не обжигают. Для опасной поверхности
использован vanilla magma. Smoke — ограниченный `animateTick` открытого очага,
редкий тихий vanilla fire cue, без серверного обхода блоков каждый tick.
Top/side грунта отдельные, камни имеют нерегулярные оранжевые жилы и случайные
повороты, все ресурсы/loot/RU/EN/Creative/recipes/tags включены в JAR.

**#264:** одна порода чернодрева: `blackwood_log`, `charred_blackwood_log`,
`blackwood_wood`, `charred_blackwood_wood`, `blackwood_planks`, stairs/slab/fence/
fence_gate/door/trapdoor/button/pressure_plate и `charred_blackwood_leaves`.
Обычная/горелая кора и торцы различимы, axis X/Y/Z; стандартные vanilla state
templates для half/double/facing/open/waterlogged там, где они предусмотрены
типом блока. Три связных формы деревьев: низкая, широкая, наклонённая; preflight
всех logs/leaves до записи. Нет саженца/самовольного превращения живого дерева.
FallingTree подключается через стандартные logs/leaves tags, без прямой загрузки
его классов и без правки пользовательского config; **реальная рубка NOT_RUN**.

**#265:** четыре EntityType: `coal_crawler`, `ash_spirit`, `pekelnik`, `sparkling`.
Собственные объёмные ModelParts/UV/текстуры/glow/анимации; яйца и natural tables.
Углеполз: windup → короткий наземный бросок → однократный bite → recovery.
Дух: без nearest-player агро, только ответ на провокацию, сохранённые provoker UUID
и anger timeout; collision-safe smoke shift и ash pulse. Пекельник: когти либо
видимый медленный coal projectile по LOS, без размещения огня. Искровик: нулевой
урон, запрет target/attack, собственное парение/искры. Спецэффекты — отдельные
геометрические `cinder_coal` и временный `cinder_ash_spike`, не дополнительные виды.
Проектиль/перемещение проверяют загруженность затронутых chunks до collision.
Удары/выбор атак/фазы/синхронизированное время считаются сервером.

**#266:** `zharenitsa`,750HP, переход на375HP, bossbar, отдельная женская модель:
узкий торс/сердцевина, две руки, венец, многослойные раздельные юбочные ленты.
Кольцо — расходящийся низкий heat front, через который можно прыгнуть;
пики — пять заранее отмеченных точек, реальные временные 3D шипы без terrain edit;
сдвиг — ограниченные проверенные позиции; вихрь — только фазаII, ограниченные
пульсы/притяжение, LOS/дистанция/конечное время. Перезагрузка отменяет прерванную
атаку и не повторяет её damage. Обычный огонь/магма не вредят обитателям этого
биома; нет общей неуязвимости к оружию.

Награды при default doMobLoot: голова×1 (реальный декоративный блок с facing,
геометрией/соответствующим collision и loot), печати×4–6, руна×1, заражённая
средняя душа×1. Душа — прежний `slavicmyths:soul` с сохранённым CUSTOM_DATA marker
`slavicmyths_infected_medium` и локализованным именем; второго soul ID/tier нет.
Печати сохраняют исходный armor stack/чары/damage/trim, смешанные iron/diamond
дают0/1/2/3/5HP; уже улучшенное/неподходящее не принимается, фиксированный
transient modifier снимается существующим equipment-event refresh.

Руна `rune_ash_mantle` включена в существующие `Runes`/`RuneDefinition` и слоты
брони: установленная активируется при огненном повреждении; отдельный trophy item
также активируется ПКМ. На8s уменьшает fire/новый cinder_heat damage на40%, один
ответный порыв с LOS/дистанцией, cooldown30s с сохранённым absolute ready time.
Это инженерная активная/реактивная схема, **не принятый игровой баланс**.
Руна не даёт абсолютный Fire Resistance и не наносит terrain damage.

**#267:** `slavicmyths:black_cinder` добавляется в существующий Nether preset
после загрузки registry, прежняя точка Навьей рощи и пять vanilla entries сохраняются.
Новый bounded geology pass до ores, nature после ores: открытая холмистая равнина,
ветвящиеся извилистые ash paths, небольшие локальные поверхностные lava pools,
vanilla magma, редкие деревья/дым. Пруд отделён от вычисляемой тропы буфером;
реальное растекание/проходимость требует мира. Глобальные noise/dimension/lava sea
JSON и существующий navya biome payload сохранены (checkout CRLF не считается
содержательным изменением). Старые chunks не мигрируются и не пересоздаются.

Арена `slavicmyths:zharenitsa_arena`: открытая площадка/ash cross paths, небольшая
лава/магма по краям, фундамент и освобождённый объём. Известные vanilla structure
footprints проверяются accepted-plan preflight; terrain yield при чужих references.
Построения/порталы/BE/непризнанные блоки не перетираются вслепую. Нет обещания
универсальной интеграции с неизвестными сторонними claims/структурами.
BossPlaced сериализуется в StructurePiece; детерминированный UUID, home и отдельный
`CinderEncounters` SavedData сохраняют ACTIVE/DEFEATED и одноразовый claim награды.
Журнал трогается только entity tick/death на server thread, не worldgen worker.

Поиск — штатные **`/locate biome slavicmyths:black_cinder`** и
**`/locate structure slavicmyths:zharenitsa_arena`**. Новая команда строительства
биома не создавалась. Регистрация/данные для native locate есть; выполнение
команд и итоговые координаты **NOT_RUN**, не выдуманы.

## Критерий полной области и ограничение доказательства арены

Рабочее формальное условие: связная область содержит безопасное наземное ядро
512×512 блоков (не lava sea наY31), внутри которого нет пересечения с accepted
vanilla structure footprint. Позиции — решётка256 блоков: start chunk кратен16,
центр chunk +8. `spacing16/separation15/frequency1` делает offset0, без RNG-шанса
отсутствия. В каждом таком ядре имеется interior anchor с33×33 biome core и
радиусом арены16. Геометрическая решётка проверена по всем остаткам координат,
две оси независимы. Структура фильтрует biome core/базальную лаву/пересечения.

Это **ограниченная гарантия по формальному безопасному ядру**, не доказанный
обход всех реальных connected components. Длинная узкая или фрагментированная
область без такого ядра не покрывается этой формулировкой. Допустимость критерия,
число арен в реальных больших компонентах, natural coverage и доступность должны
быть проверены на5seed×4окна и приняты владельцем. Утверждение «настоящая игра уже
доказала босса в каждой крупной области» здесь **не делается**.

## Инженерные defaults (не объявлены принятым балансом)

| Параметр | Реализация |
|---|---|
| HP обычных |90Углеполз/70дух/120Пекельник/16Искровик; регулируются `slavicmyths-black-cinder.toml` |
| Boss HP / phase |750/375 — канон, не снижены |
| Damage |bite9/claw11/pulse6/coal8/ring12/spike10/vortex3за10ticks active |
| Windups |bite22,claw/coal26,smoke/shift16,pulse24,ring/spikes/vortex36,phase40ticks |
| Руна |160ticks,40%fire reduction,600ticks cooldown, один порыв |
| Обычный loot |взаимоисключающе soul2%+1%/Looting либо1–3fragments; только прежние IDs |
| Terrain |чистый профиль55…74 на выборке, max соседний X-step1; не измеренный рельеф мира |
| Climate |(.7,-.4,offset.175), source/native classifier, не утверждённая фактическая частота |
| Arena |решётка256, радиус16, квалифицирующее ядро512; критерий требует owner review |

## Матрицы #262–#267

`APPROVED_IMAGE_SHA` во всех ART строках: известен из strict, **по байтам не сверён**.
Отсутствие original-vs-actual отмечается отдельно; source PASS не равен ART/game PASS.

| REQUIREMENT_ID | APPROVED_IMAGE_SHA | ACTUAL_CODE/MODEL/UV | ACTUAL_EVIDENCE | RESULT |
|---|---|---|---|---|
|262 IDs/источники/техгеометрия |passport; original не сверён |CinderContent/Blocks; cinder_rigs/*.json |asset-manifest/source-manifest |PASS (source) |
|262 пять отдельных baked rigs/UV/позы |не сверён |CinderModel; authored parts |native-rigs.json,230frames finiteUV |PASS (native source) |
|262 source before/after/6views/palette/5×5 |не сверён |фактические ModelPart faces+PNG |before-models-absent; actual-models-six-views; actual-palette-and-tiling |PASS (source preview) |
|262 точность оригиналов/native shader-free сравнение |не сверён |оригинальных4PNG нет; game не запускался |ART_ORIGINAL_COMPARE_NOT_RUN |NOT_RUN |
|263 ровно5семейств/6каменных форм/полные ресурсы |blocks не сверён |CinderBlocks;blockstates/models/loot/recipes/tags |26blocks full manifest, JAR audit |PASS (source/JAR) |
|263 no duplicate magma/hurt block, локальный дым |passport |CinderBlocks#animateTick; стандартные Block |source review;15vanilla aliases |PASS (source) |
|263 placement/mine/collision/light/5×5вигре |не сверён |реализация есть |игра запрещена |NOT_RUN |
|264 дваlog+woodvariants/9строительныхформ/листва |blocks не сверён |CinderBlocks;vanilla templates+собственныеPNG |source/JAR resources |PASS (source/JAR) |
|264 три связных дерева/безсаженца/preflight |passport |CinderTreePlan/CinderTerrain |1500native production plans, connected/envelope |PASS (offline plans) |
|264 FallingTree/полнаярубка/рост/перезаход |n/a |стандартные tags, optional безимпорта API |реальная конфигурация не запускалась |NOT_RUN |
|265 ровно4жителя/разныеroles/HP |mobs не сверён |CinderContent/Config/Mob |source, compiled attrs/registry declarations |PASS (source) |
|265 bite/smoke/pulse/claw/projectile/peaceful |mobs не сверён |CinderMob/Coal/Model |native poses; authoritative finite-state source |PASS (source only) |
|265 звук/subtitles/реальныекешированныеOGG |n/a |CinderSounds/sounds.json |audio-routing.json event→asset/hash, безкопирования |PASS (resource routes) |
|265 настоящий бой/LOS/collision/нейтральность/слух/MP |не сверён |реализация есть |0игровыхзапусков |NOT_RUN |
|266 женскаягеометрия750HP/2фазы/4способности |boss не сверён |CinderMob/Model/Spike/Damage |native export,source/JAR |PASS (source only) |
|266 все4обязательныенаграды/стабильныеIDs |boss не сверён |dropCustomDeathLoot,head,seal,rune,soulmetadata |source;BossPlaced+journal |PASS (source only) |
|266 печати0/1/2/3/4→0/1/2/3/5 |n/a |реальный ZharenitsaSeals.seal/bonusForPieces |8iron/diamondstack copies, damage37, ProtectionIII/Redstonetrim/custommetadata, negative duplicate/unsupported |PASS (offline stack methods) |
|266 руна всуществующихslot+реальнаязащита |n/a |Runes/RuneDefinition/CinderMantle |ID recognized; event/expiry/source review |PASS (source only) |
|266 defeated/ownership/повторclaim/NBT |n/a |CinderEncounters |native claim/duplicate/foreign UUID/NBT roundtrip |PASS (offline NBT, не relog) |
|266 полныйбой/снятиебонуса/смерть/перезаход/MP |не сверён |реализация есть |игра не запускалась |NOT_RUN |
|267 biome/preset/terrain/trees/4natural tables |landscape не сверён |CinderWorldgen/Terrain/mixin;worldgenJSON |native classifier5seed;profile5×4offline;JAR |PASS (source/offline) |
|267 ванильные5+Навьясохраняются |n/a |originalpoints/resourcepayloads сохранены |offline-climate:все7present;не реальныеchunk percentages |PASS (limited offline) |
|267 гарантированнаяарена по безопасномуядру512 |passport |spacing16/sep15/freq1;ArenaStructure/Piece |lattice arithmetic всехresidue;условия выше |PASS (conditional source/arithmetic only) |
|267 каждаяреальнаякрупнаяобласть/доступныйбосс |n/a |условная реализация есть |критерий/coverage не приняты, naturalworlds0 |NOT_RUN |
|267 native locate среальнымиIDs |n/a |biome/structure зарегистрированы |source/JAR declaration;безвыдуманныхкоординат |PASS (source only) |
|267 ≥5seed×4реальныхокна/oldworld/portals/MP/perf |n/a |не запускалось |runtime запрещён |NOT_RUN |
|BUILD Java21/clean/resources/production/testisolation |n/a |build/libs;toolsverification |final-gates/resource-final/cinder-final;build-result/gates |PASS (technical gates) |
|PolyMC установка кандидата |n/a |installation-receipt.json |installed path/version/SHA,backup,одинJAR;по прямому поручению обновитьмод |PASS (installation only) |
|OWNER/manualacceptance |n/a |не выполнялась |OWNER_PENDING,release не объявлен |NOT_RUN |

Дополнительно [requirements-matrix.csv](requirements-matrix.csv) трассирует54
применимых нумерованных правила00/01/03/07/09/10/12. Полная игровая приёмка этих
широких правил оставлена NOT_RUN даже при технических subset PASS. Требования04,
13 и14 отражены в матрицах/регрессиях здесь; старое «Искровик агрессивен» из04
переопределено текущим ТЗ: Искровик мирный. Не считалась статистика assert-ов
доказательством мира/боёв/производительности.

## Проверки и JAR

Сборка существующей ветки сохраняет mod version **2.0.8-HELL**:2.0.3 — номер
контентного этапа, версия стабилизации не откатывается назад. Production artifact:
`D:/slavic-myths/build/libs/slavicmyths-2.0.8-HELL.jar`. Точный финальный SHA256,
число classes/resources и путь — [build-result.json](build-result.json),
[gates.json](gates.json). Источник — commit выше плюс source-manifest/delta.

Финальный SHA256:
`64b042ae3def10952a0c120d8dac9b16c95658070450bb0b323fd48afad3084e`.
Java21 clean build + native tasks PASS за 1m12s; последний clean build после
исправления EOF генератора PASS за 48s. Все 2004 production classes совпали
с native-проверенным кандидатом (native-class-continuity.json); 8254 ресурса.
Точные production delta bytes сохранены в `working-source-delta.zip`,285файлов;
это **не** оригинальный ZIP с референсами.

Команды реально выполнены с JDK21 `C:/Program Files/Java/jdk-21.0.12`, JAVA_HOME
ограничен дочерней командой:

- `gradlew.bat clean build -I tools/cinder-offline.gradle exportCinderRigs surveyCinderClimate`.
- `python tools/verify_resources.py`; `python tools/verify_cinder.py`.
- Двойной scoped `cinder_assets.py`: drift0по всему src/main/resources
  (`generator-reproducibility.json`); legacy/audio writers не запускались.
- `python tools/cinder_preview.py`; PNG source previews просмотрены визуально.
- Native offline JVM использует vanilla ModelPart/UV, stack methods и pureplans;
  ни Minecraft main, ни client/server/world/GameTest не стартуют.

Первоначальные ошибки (неверный ModItems.SOUL, недостающий stone_top) исправлены;
текущие gates проверяют финальные ресурсы. Windows checkout CRLF нормализуется
только при сравнении tracked text; числа/структура старых Nether JSON не менялись.
Неудачный native head fixture сохранён в `offline-head-fixture-failed.log`:
он создавал Block после bootstrap freeze. Финальный стенд читает тот же production
VoxelShape без создания нового registry object; игровая ошибка этим не объявлялась.

### Установка кандидата по прямому поручению владельца

Разрешение «после реализации обновляешь мод» использовано только после технических
gates. Проверены mmc-pack1.21.1/21.1.255 и отсутствие запущенного игрового процесса.
Installed JAR:
`C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/.minecraft/mods/slavicmyths-2.0.8-HELL.jar`.
Точный итоговый SHA/source commit+delta/backup — `installation-receipt.json`.
В mods ровно1slavicmyths JAR, копия подтверждена hash. Это тестовый кандидат
**CANDIDATE_INSTALLED_NOT_OWNER_ACCEPTED**, не финальная доставка всей цепочки.
Первый atomic replace helper отклонил пустой backup-path; старый JAR восстановлен
из проверенного backup, затем native Move-Item выполнен с повторной проверкой SHA.
Игровые процессы ни для установки, ни для QA не запускались.
Последний кандидат дополнительно содержит финальные AI/ground-ring/smoke fixes;
предыдущий кандидат и самый первый pre-task JAR сохранены раздельно, оба backup
пути/SHA есть в installation receipt. Все #261–#267 получили комментарии IN_PROGRESS
со scope/ограничениями и локальными evidence paths: `github-comments.json`.
Код не коммитился/не публиковался, комментарий не равен смене Project Status.

## Не сделано / не проверено

Не выполнены оригинальные SHA/ART сравнение, реальные natural spawns/арена/бой,
Survival progression, фактические команды locate/координаты, слушание/громкости,
сохранение/перезаход/MP/FallingTree,5seed×4настоящихокна, границы старых/новых
chunks/порталы и FPS/MSPT/GC. Balance и размер критерия полной области остаются
инженерными решениями для игровой проверки, не принятой финальной настройкой.
Нет утверждения «каждая структура/моб в мире проверены» или «готово к релизу».

## Как проверить в Minecraft — только по отдельному разрешению запуска

Сначала сверить exact source/JAR/SHA из build-result и использовать отдельный
тестовый профиль/новый Survival-мир; старый мир только в копии. Shaders выключены,
RU/EN, установленный реальный modpack (в том числе наличие FallingTree), записать
seed/координаты/время/кадры. Новый кандидат **установлен для ручной проверки**;
проверить его SHA по `installation-receipt.json`. Прежний JAR сохранён в backup,
его исходный SHA подтверждён в `installed-before-unchanged.json`.

1. **Без OP:** добыть obsidian, построить портал, войти в Nether, исследовать и
   найти гарь. Ожидаются все пять vanilla биомов, Навья роща и новая открытая гарь,
   низкие холмы, малые пруды/безопасные ash paths; записать реальный маршрут.
2. Пройти между прудами без Fire Resistance и обхода в Creative. Негативный тест:
   charred/cracked stone не обжигает; magma/lava опасны, но не перекрывают все пути.
   Добыть5материалов обычными подходящими инструментами, сделать5×5пол/стену,
   проверить дроп и разные top/side/освещение. Неверный инструмент не дублирует loot.
3. Срубить обычное/горелое дерево, проверить оба торца и3формы. Скрафтить planks,
   stairs/slab/fence/gate/door/trapdoor/button/plate; проверить facing/open/half/
   double/waterlogged, работу кнопки/плиты, разбор двери ровно в1предмет. Саженца нет.
   При наличии FallingTree срубить целое дерево без соседних построек; без мода
   штатная рубка не падает. Поставить предметы, сохранить/перезайти — всё сохраняется.
4. Найти4**естественных**жителя без eggs/summon. Пройти мимо духа без атаки — он
   не агрится; ударить и проверить ответ/таймаут/перезаход. Проверить bite за одну
   атаку, ground dash перед стеной/обрывом; у Пекельника — разные claws/coal,
   непрозрачная стена блокирует damage, постройки не горят. Искровик не атакует.
5. Найти natural arena по безопасному пути. Сразиться с Жареницей750HP, увидеть
   windup/recover каждой атаки; прыгнуть через кольцо/уйти от меток пиков/укрыться
   от вихря. До375HP вихря нет, после есть. Проверить дальний обстрел/укрытия/
   движение босса и доступный hitbox со всех сторон, без невозможного stun-lock.
6. Победить: ровно1head,4–6seals,1rune,1помеченнаядуша. Установить голову во всех
   направлениях, сравнить geometry/collision, добыть ровно1. Вернуться после unload
   и настоящего выхода/входа — новый boss/повторный комплект не появляются.
7. Имея4печати,держать iron/diamond armor stack и sneak+ПКМ по smithing table:
   одна печать расходуется на часть. Надеть0/1/2/3/4смешанныхчасти — +0/1/2/3/5HP.
   Снять/поменять/умереть/перезайти — бонус не копится, текущие HP ограничены max.
   Чары/прочность/trim сохраняются. Негативно: gold/netherite/повторное улучшение/
   отсутствие печати не расходуют инвентарь и не дают бонус.
8. ПКМ руной — ash cue/8s40%fire reduction/одинпорыв/30s cooldown. Установить её в
   armor slot существующей рунной системой — срабатывает от огня; не абсолютный
   иммунитет. Проверить cooldown после выхода/входа, сохранение socket, первый
   ответный порыв/LOS/дистанцию и совместимость с другими защитными эффектами.
9. В копии старого мира проверить базу/порталы/старые chunks и границы новых,
   fortress/bastion/ruined portal/ores/lava sea. Новые chunks получают новый контент,
   старые не переделываются. В2клиентах проверить fight/loot/seals/rune/сохранение,
   одновременные действия и одноразовую награду.
10. После разрешения провести5seed×4разнесённыхокна, записать размеры компонентов,
    наличие безопасного ядра/количество арен/доступность/4natural spawns, доли7biomes,
    world create/save/exit и реальные FPS/MSPT/память. Непойманная встреча — NOT_RUN,
    сломанная гарантия/путь — FAIL, не «работает по JSON».

**Отдельная OP диагностика, не Survival доказательство:**
`/locate biome slavicmyths:black_cinder` и `/locate structure slavicmyths:zharenitsa_arena`.
Не телепортировать/генерировать ради ложного отчёта об естественном прохождении.
Все игровые пункты выше сейчас **NOT_RUN**, реальные координаты не придуманы.

## Риски/откат / следующая задача

Главные риски: порядок datapack/preset/mixin в настоящем loader, границы с vanilla/
Навьей рощей, естественная площадь/редкость гари и допустимость512ядра, фактический
MSPT геологии, безопасное растекание лавы, LOS/размеры/анимации/звук, совместимость
FallingTree/Curios/JEI/Jade/Xaero. Эти риски не выдаются за воспроизведённые баги
или измеренную производительность; runtime0. Будущие2.0.4иостальныебиомы не добавлены.

До появления новых chunks откат source состоит в адресном удалении только новых
IDs/ресурсов/точек подключения по source-manifest; пользовательские прежние delta
не трогать. **После использования нового контента в мире нельзя просто удалять
мод/новыеregistryIDs:** сохранить backup мира и вернуть прежнюю копию либо
подготовить миграцию отдельно. Автоматический reset/delete/rollback не выполнялся.

Следующий шаг — разрешённая игровая/художественная приёмка этого кандидата и
доработка по её реальным результатам; original-vs-actual после доступа к PNG.
Следующий биом автоматически не начинается. Установка/релиз всей стабилизационной
цепочки не заявлены. Владелец сам принимает и закрывает #261–#267.
