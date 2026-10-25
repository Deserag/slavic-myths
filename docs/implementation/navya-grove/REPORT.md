# Навья роща #251–#257 — IN_PROGRESS, 25.10.2026

Ветка `hell`, рабочая копия `D:/slavic-myths-hell`, базовый source commit `275ecaa5345f9b374f83f0829c58df16c0e54546`. Нового коммита нет: изменения не закоммичены по правилам владельца. Исходная рабочая копия `D:/slavic-myths` не изменялась. Единственный новый биом — Навья роща; оружие, руны/крафты будущих этапов и другие биомы не добавлялись.

Реализованы исходники полного контентного пакета и последняя интеграция генерации. **Это не итоговая готовность и не сдача цепочки в «Тестирование».** Все семь карточек фактически «В работе», Issues OPEN, приёмка владельца отсутствует. Minecraft/PolyMC/GameTest/сервер: **0 запусков**. `POLYMC_NOT_INSTALLED`.

## Утверждения владельца и доступные референсы

Параметры soul loot приняты явным ответом «Да, эти параметры». Оригинальные PNG повторно искались в рабочих и архивных Issues/проекте; доступных файлов нет, комментариев/ZIP нет. #257 прямо документирует отсутствие исходных изображений в GitHub. По направлению владельца «смотри в задачах» использованы production markdown и SVG из docs/concepts/navya-grove. Нельзя выдавать это за визуальное сравнение с отсутствующими PNG.

## Что реализовано

- Восемь геологических материалов; одна древесная порода со строительными вариантами, знаками, старой корой без второго ID; флора и возобновляемый саженец. Настоящие модели/16×16 PNG/loot/RU/EN/Creative Tab.
- Три связные формы деревьев (в том числе2×2 низ большого). TreeFeature делает preflight, не заменяет здания и не загружает чанки. FallingTree config не правилась.
- Навка, Корневик, Костяной ползун, мирный огонёк и Сторож: отдельные собственные voxel rigs, UV, PNG/glow masks, суставные позы и renderers. Серверные windup/damage/recovery, projectiles и временные корни. Новые модели отсутствуют в базовом commit: «до» — отсутствие этих ID/ModelParts, а не другой старый арт. «После» экспортирован из actual ModelPart vertices/UV в одинаковых позах/масштабе внутри каждого персонажа. PNG/GIF — source render, не игровой скриншот.
- Утверждённые взаимоисключающие душевные ветви и гарантированные награды Сторожа. Нет нового оружия или работающей руны. Призванные помощники без наград.
-35 vanilla sound aliases с mapping до actual cached OGG и SHA1; регистрация/subtitles/callsites. Ни синтетические голоса, ни карантинные события не возвращены. Прослушивание NOT_RUN.
- Биом в существующем Nether через узкий mixin vanilla preset; локальная геология до руд, смена камня/флора после руд. Лавовые акватории не застраиваются. Наклонные понижения соединяют уровни в профиле; actual traversal требует мира. Верхний лес, средние корневые уступы, нижние кости/сталактиты/сталагмиты; редкое Сердце с площадкой и привязанным Сторожем. Vanilla structure eligibility сохранена, пересекающиеся chunks защищены от локальной перестройки.

Параметры spawn weights, плотности, rare Heart frequency и climate пока технические defaults, **не прошедший игровой баланс**. Генерация меняет только новые чанки; старые не мигрируются. Возможны швы с прежним Nether и при смене генератора; реальная совместимость старых миров/порталов/структур не проверена.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| #251 единственный биом, порядок пакета | Блоки → флора → существа → Сторож → worldgen; другие биомы не добавлены | PASS (source scope) |
| #251 полная приёмка цепочки | Runtime/художественная приёмка не выполнены | NOT_RUN |
| #252 единый контракт/ID/палитры | CONTRACT.md; технические SVG/specs; реальные PNG и source-render | PASS (source) |
| #252 сравнение с оригинальными PNG прежнего чата | Текущие/архивные Issues и read_thread не дают доступных PNG; в #257 прямо указано, что картинки не загружены в GitHub. Владелец направил использовать задачи; точное совпадение не заявлено | NOT_RUN |
| #253 восемь геологических блоков | NavyaBlocks; реальные blockstates/models/PNG/loot/RU/EN; actual-block-models.png | PASS (source/JAR) |
| #253 размещение/добыча/освещение | Игра не запускалась | NOT_RUN |
| #254 одна порода/строительные варианты/растения/саженец | 31 inventory blocks всего; native LeavesBlock; old bark property; TreeGrower; генераторы актуальны | PASS (source/JAR) |
| #254 три формы, связность и лимиты | tree-topology.txt: 1500 планов,15–93 logs, maxSize100; пользовательский config только прочитан | PASS (offline geometry) |
| #254 настоящая рубка FallingTree/рост/соседние постройки | Не проверялись в Minecraft | NOT_RUN |
| #255 четыре существа, HP80/120/150/20, AI/яйца/мирный огонёк | NavyaContent/Mob/Safety/Shot/Roots; серверные damage windows и cooldown | PASS (source) |
| #255 отдельные модели/UV/текстуры/анимации | NavyaMesh/Pose/Model/Renderer; native-rigs.json; actual-entity-models.png; GIF всех поз; заряд кисти Навки | PASS (native source export) |
| #255 утверждённые души/собственные компоненты | Личный ответ владельца:1–3 fragments; soul2%+1%/Looting; exclusive0.5% blight; component1–2. Три alternatives без совместной выдачи ветвей | PASS (source contract) |
| #255 combat/slow/collision/save/reload/Looting | Не запускались; статическая проверка не выдана за выпадения | NOT_RUN |
| #256 Сторож700HP/две фазы/атаки/арена/помощники | NavyaMob: transition350HP,20% protection, sweep/roots/trunk/summon; max2 marked helpers, no loot/XP; home; cancel interrupted damage on reload | PASS (source) |
| #256 лицо в стволе/масштаб/видимое бревно/анимации | Actual ModelParts/UV:6.5-block rig, body+buttresses около3block; held log4.5block, lift/release; native pose frames | PASS (source export) |
| #256 гарантированный лут | core1, branch2–5 c Looting capped5, soul2–3, soul_blight1; four unconditional pools | PASS (source contract) |
| #256 бой/фазы/анимации/разгрузка/антифарм в игре | Не проверены; unloaded helper UUID reservations conservative, runtime needed | NOT_RUN |
| #255/#256 sound routes/subtitles/quarantine | audio-routing.json:35 actual vanilla event routes, cached OGG SHA1, RU/EN; old sounds preserved; no generated voice or new copied OGG | PASS (source/resource) |
| #255/#256 прослушивание/громкость/тайминг/сервер | Нет реального прослушивания/клиента/сервера | NOT_RUN |
| #257 biome/climate/vanilla five types | Mixin только vanilla Nether preset; точка(-.3,.4),offset.175; climate-proposal.json5seed native analysis, vanilla types remain represented | PASS (offline climate only) |
| #257 три яруса/переходы/лес/кости | Geology before ores; Nature after ores; profile-check.txt5seed×262144 columns; varied shelves, tier junctions, tall bone spikes up/down; native columns only | PASS (source/profile) |
| #257 редкое Сердце/Сторож/поиск/четыре спавна | Structure+piece saved bossPlaced/UUID/home, upper or middle clearing; spacing32/separation16/frequency.6 technical prototype; /locate structure slavicmyths:navya_heart; vertical predicates | PASS (source; tuning unvalidated) |
| #257 vanilla ores/structures/lava/old chunks | Pinned HELL overrides unchanged; vanilla biome features/structure tags preserved; lava sea skipped; protected structure chunks skipped; no retro-gen/tick/chunk loading | PASS (source preservation only) |
| #257 actual5seed/coordinates/screenshots/traversal/spawns/MSPT/memory/save/seams/portals | Ни одного игрового мира не создано; registry/mixin application and data codecs in actual NeoForge NOT_RUN | NOT_RUN |
| Java21 clean build/production resources | logs/final-clean-build.txt; logs/final-resources.txt; gates.json; build/current-resources.json | PASS |
| PolyMC installation / manual owner acceptance | Не выполнялись; старый инстанс не изменён, новый не создавался | NOT_RUN |

## Проверенный локальный JAR

Версия `2.0.0-HELL`; путь `D:\slavic-myths-hell\build\libs\slavicmyths-2.0.0-HELL.jar`; SHA256 `cb064337aaa02c278409197263afba0e331bc9964d96601539163c430cdb92c7`. Ресурсов8001, JSON4402, PNG1443, models1886, Java21classes1914. Это собранный локальный кандидат, **не installed JAR**. Source commit — базовый выше + незакоммиченный delta/source-manifest.json. Backup/installed path не заявляются, поскольку установки не было.

## Краткая ручная проверка владельца

После отдельного решения о запуске:5 seed/несколько областей, три яруса и проходы, деревья/FallingTree, vanilla structures/ores/lava/portals и старые чанки, natural spawn и повторный визит Heart; все атаки/телеграфы/души с Looting и anti-farm, phase2/save/unload/MP; модели со всех сторон и звук. Реальные координаты/видео/логи/MSPT приложить отдельно. Сейчас запуск не разрешался и не выполнялся.
