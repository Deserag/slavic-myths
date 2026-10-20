# #77 — Ским-зверь по плакату 1.7.3 · 1.9.7.30 · 20.10.2026

**IN_PROGRESS / OWNER_PENDING.** Реализация модели, ресурсов, анимационной адаптации и офлайн-проверок выполнена. Игровая художественная приёмка, фактические контакты/коллизии, OpenGL-свечение, FPS и multiplayer/relog **NOT_RUN**. Issue [#77](https://github.com/Deserag/slavic-myths/issues/77) остаётся **OPEN**, без `owner-accepted` и без закрытия со стороны Codex. Minecraft/PolyMC/GameTest/сервер не запускались: **0 запусков**.

## Источник и референсы

- [Утверждённый плакат владельца 1.7.3](../../specs/1.7.3/06_References/Approved/APPROVED_Skim_Beast_4_views_and_attacks.png) — единственный художественный эталон. [Его канонический GitHub URL](https://github.com/Deserag/slavic-myths/blob/main/docs/specs/1.7.3/06_References/Approved/APPROVED_Skim_Beast_4_views_and_attacks.png).
- [Исходный инженерный SVG](../../design/ISSUE_77_SKIM_MODEL_SPEC.svg) сохранён без изменений; SHA проверен. Актуальные pivots/UV находятся в авторском rig и CSV, SVG не заменялся новым рисунком.
- [Паспорт модели/палитры/UV](../../specs/1.7.3/02_Skim_Beast/01_MODEL_TEXTURE_UV.md), [паспорт анимаций](../../specs/1.7.3/02_Skim_Beast/02_ANIMATIONS_KEYFRAMES.md), [snapshot актуального ТЗ v4](ISSUE_SNAPSHOT.md).
- `SOURCE_HEAD e5924bd909113dc587496940b270f963fe259714`; ранее начатый незакоммиченный merge main `a4f343f3421042736543dc2afa09cfc7f03bc65a` сохранён. Проверен актуальный `origin/main fc68908b983fe677063f6ce29c982aebc72d3e26`: дополнительный commit касается паспорта #76, источники Скима соответствовали main. Текущий source — **UNCOMMITTED working tree**, включая сохранённые прежние UI/command/#76 изменения. Автокоммит запрещён [AGENTS.md](../../../AGENTS.md); новых веток/PR/push нет.

[Baseline/SHA и координаты](before/source.json), [полный production manifest до #77](before-production-sha256.json), исходные Java/PNG/генератор в `before/`, [нативная исходная сетка](before/native-poses.json.gz). В начале задачи в PolyMC была установленная 1.9.7.29; source baseline снят независимо из её текущих источников. Оригинал игрового скриншота Скима от 20 октября в этом чате не предоставлен; не утверждаю, что видел именно его или что он доступен файлом GitHub. Плакат доступен и просмотрен, собственный исходный source-render снят до правок.

## Изменения модели и отрисовки

Несущая геометрия заменена: вместо `body30×23×34` и `hips25×18×29` построены более узкая грудная клетка с отдельными наклонными боковыми массами, холка/нижняя грудь, собранная поясница и наклонённый таз с отдельными объёмами бёдер. Верх корпуса имеет ступенчатый хребет и боковые складки шерсти. Это новый корпус, а не прежние две коробки под украшениями.

**59 именных узлов + корневой ModelPart, 231 cuboids.** Грива состоит из24 анимируемых групп в3 слоях, с толстыми сужающимися прядями разной длины. Голова имеет отдельные объёмы черепа/лба/удлинённой морды/носа, подвижную нижнюю челюсть, два трёхсекционных внешних клыка и дополнительные верхние/нижние зубы. Янтарно-красные глаза вынесены на читаемую поверхность. Передние конечности особенно тяжёлые, задние имеют мускулистые бёдра и отдельные скакательные суставы; на каждой из4лап разделены4пальца со светлыми когтями. Ровно **один хвост из5 костей**, с красными сегментами и выраженным кончиком.

Палитра из паспорта: тёмный мех/тёплые коричневые слои, локальные красные полосы/шрамы, слоновая кость, янтарные глаза. Геометрия и рисунок материалов авторские; плакат не использовался как наклеенная UV-текстура. Три согласованные512×512 PNG: body, eyes-only mask, existing-red-pixels mask. Глаза имеют локальный emissive проход; в существующем64-тиковом RAGE глаза усиливаются, красные метки светятся с визуальным плавным входом13–20 и выходом50–64. При DEATH глазной glow затухает26–75. Это клиентская отрисовка, серверные состояния и времена не менялись.

У общей `SkimModel` сохранена ветка Алёши; модельная математика Скима вынесена в `SkimBeastPose`. Канонические имена/родители генерируются вместе с mesh, поэтому новые части участвуют в reset, позах и floor-fit. Длины IK берутся из реальных pivots, подошвы/вылет когтей — из авторской геометрии. При подъёме лапы выше плеча сохраняется плоскость изгиба, без180° roll конечности. Ориентация плоской подошвы извлекается из обратной матрицы ModelPart; проверен именно установленный порядок `Rz*Ry*Rx`.

Рёв использует прежний `canRear()`: разрешённое вставание либо рёв на4лапах с небольшой визуальной присадкой. Голова/клыки и поднятые кисти во время rearing отводятся в проверяемую локальную область. Отдельно проверены целые и половинные фазы. **Это model-space диагностика направления0°, не доказательство коллизий мира при любом повороте:** исходная серверная проверка пространства остаётся как в main, её мировой AABB не менялся. Реальная проверка потолков во всех направлениях остаётся владельцу.

В GRIP сохранён серверный якорь ног **1.7 блока впереди, .55 вверх**. Пасть совмещается с задней поверхностью верхней части ноги нативного humanoid fixture, а ворот гривы сжимается у плеч; голова стоящей жертвы остаётся снаружи черепа/корпуса. На18–44 голова фиксирована на захвате, look/hurt не уводят пасть от якоря. Это визуальная адаптация без изменения положения/телепортации жертвы на сервере. Проверка head/hat выполнена для стоящего vanilla humanoid со стандартным визуальным масштабом .9375; иные игровые позы/размеры требуют runtime-проверки.

DEATH использует прежние75тиков: колени подгибаются, конечности складываются при падении; root смещается по фактическим cube corners, чтобы сетка не уходила в землю. Глобальный tick polling/поиск сущностей не добавлен.

## Сопоставимые изображения — NOT_IN_GAME

- [Плакат рядом с реальной новой моделью: front/side/back/top](poster-to-source.png).
- [8 source-derived кадров до/после](matched-8-views.png): одинаковые state/t=0, age=20, камера, масштаб и свет; [вид сверху до/после](matched-top.png), [силуэт без текстуры](silhouette.png), [голова/грива/когти крупно](details.png).
- [Атаки до/после на одинаковых t](actions-matched.png), [рёв allowed/blocked](roar-clearance.png), [AWAKEN/CLAWS/LEAP](actions-1.png), [WHISTLE/FLANK/RAGE](actions-2.png), [EXHAUSTED/STAGGER/RECOVER](actions-3.png), [ходьба/STALK-RUN inputs](walk.png).
- [GRIP0/14/18/21/31/41/45/65](grip-phases.png), [GRIP31 с нативным humanoid](grip-held-reference.png), [DEATH0/26/52/75 до/после](death-matched.png).
- [RISE/ROAR GIF](roar.gif), [LEAP GIF](leap.gif), [DEATH GIF](death.gif).
- [Фактические прозрачные glow-mask PNG](glow-masks.png), [source-позы/Java envelopes и CPU-приближение glow](glow-source.png), [замороженный AABB поверх реальной сетки](frozen-hitbox.png).
- [Актуальный авторский rig](after/authored-rig.json), [parts/pivots/UV/material CSV](after/parts-uv-pivots.csv), [размеченный атлас](after/atlas-annotated.png), [текущая нативная сетка](after/native-poses.json.gz).

Renderer CPU использует **фактические baked ModelPart polygons/UV** после production `pose/poseBeast`, фиксированную камеру, z-buffer и nearest texture. Рендеры и GIF не являются скриншотами Minecraft; отдельная glow-картинка — приближение смешивания, не OpenGL/ночной тест. Старые нестандартные texOffs сами по себе ошибкой не объявлялись: baseline проверен по настоящим нормализованным UV движка.

## REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

PASS означает указанную техническую/offline проверку, а не принятие внешности владельцем.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Утверждённый плакат1.7.3 и исходный SVG | Канонический PNG/паспорт просмотрены; оба исходника неизменны по SHA | PASS |
| Freeze исходной реальной модели | before/source.json, Java/PNG/tool snapshot, native baseline | PASS |
| Полная замена несущих форм, единая четвероногая анатомия | authored-rig, SkimMesh, source A/B, силуэт/верх/детали | PASS — реализация источника |
| Узнаваемое художественное соответствие плакату в игре | poster-to-source показывает материал для приёмки, решение владельца ожидается | NOT_RUN / OWNER_PENDING |
| Многослойная грива, клыки/зубы,4пальца/лапу, один хвост | Геометрия/CSV и actual textured views | PASS — source review |
| Корректные pivots/нерастянутые цепи/reset | Нативные world joints, неизменные длины сегментов, reset после каждого Move | PASS |
| IDLE/WALK/STALK-RUN | Статические anchors,96moving samples; STALK/RUN представлены существующими movement inputs | PASS — offline |
| AWAKEN/CLAWS11,19,30 | Native clips/attack A/B, серверный исходник SHA-identical | PASS — offline |
| LEAP29: сжатие/складывание/посадка | Native позы/GIF; исходная серверная баллистика сохранена, не имитировалась как игровой тест | PASS — offline |
| WHISTLE22/ROAR28/FLANK24 | Native key poses и неизменные серверные hit/effect windows | PASS — offline |
| ROAR canRear true/false, без отрыва/скольжения задних опор |59полных+58половинных фаз, low-roar, yaw/pitch/hurt; high/low frames | PASS — offline |
| Верх reared-модели в локальной области ceiling-check | reared_mesh_inside_frozen_ceiling_guard; ограниченияx±1.4,z±2.6,y2.95..4.7, направление0° | PASS — только model-space |
| GRIP18–44, попадания21/31/41, бросок45 | server source unchanged; mouth anchor в actual matrices, head/hat fixture intersections отсутствуют | PASS — offline |
| RAGE/EXHAUSTED/STAGGER/RECOVER/DEATH | Все существующие clips, complete DEATH75, позы/GIF и floor bound | PASS — offline |
| Нормальные глаза/локальный RAGE glow | Два eye nets, red-pixel mask, реальные Java envelopes, BeastRenderer callsite, JAR | PASS — static/native; GL ниже |
| Читаемые глаза спереди/сбоку/3⁄4 | Depth-tested native eye-cuboid visibility,2спереди/1впрофиле | PASS — software raster |
|512px atlas/UV, отсутствие пустых/чужих islands |231отдельных cube nets,3px gutter, every baked face interior coverage | PASS |
| Generator isolation/reproducibility |2повтора model-only, весь src/main byte-identical | PASS |
| Алёша/Серый Волк/#76/прочие ресурсы/аудио | Полный source manifest;150exact Alyosha pose pairs; SkimClient только BeastRenderer layer insertion | PASS |
| ID/NBT/AI/баланс/хитбокс/дальность/таймеры | Все common/server файлы и SkimAction исходники SHA-identical | PASS |
| Java21 clean build + реальные ресурсы/production JAR | java21-clean-build.log, jar-check.json, TOML/JSON/PNG/ZIP/class parity | PASS |
| Реальное свободное движение, посадки, хит-контакты, GRIP иных поз/размеров | Клиент/сервер не запускались | NOT_RUN |
| Потолки/проходы в4направлениях, приёмка в логове | Существующая проверка/AABB/worldgen неизменны; source checks не подменяют игровой тест | NOT_RUN |
| Реальное освещение/glow днём/ночью и3⁄4в Minecraft | CPU-masks/превью не равны OpenGL | NOT_RUN |
| MP/relog/фактический FPS/MSPT | Нет игровых замеров | NOT_RUN |
| PolyMC1.9.7.30 | Не устанавливалась; принятая29 остаётся неизменной | NOT_RUN / POLYMC_NOT_INSTALLED |

Точные измерения находятся в [offline-checks.json](offline-checks.json): **31 предметный контракт PASS**, 1000 source poses и 150 точных сопоставлений Алёши. Числа проверок не заменяют визуальные результаты/матрицу.

## Изменённые файлы и воспроизведение

Production:

- `client/SkimMesh.java` — сгенерированная новая геометрия/канонический graph/shape bounds.
- `client/SkimModel.java` — отдельный beast pose path, общая ветка Алёши сохранена.
- `client/SkimBeastPose.java` — IK, клипы, grip/ground/rearing adaptation.
- `client/SkimBeastGlow.java` и одна вставка в `client/SkimClient.java` — только слой Скима.
- `textures/entity/skim_beast.png`, `skim_beast_eyes.png`, `skim_beast_rage.png`.
- `gradle.properties` —1.9.7.30.

Авторский source: `tools/skim_77_model.py`; `tools/skim_assets.py --model-only` выходит **до импорта старого генератора/его побочных действий**, выдаёт только SkimMesh+3Skim PNG и собственный metadata JSON. Полный legacy-скрипт не запускался. Его `beast()` теперь также делегирует новой модели, поэтому следующий model generation не вернёт старые коробки.

Проверки: `tools/skim77-native/Skim77Native.java`, `skim77-native.init.gradle`, `verify_skim77.py`, `verify_skim77_jar.py`, `skim77_previews.py`; у `tools/skim-tests/SkimChecks.java` убран ненужный loader/registry bootstrap и прежняя точечная константа3.0 заменена реальным диапазоном mane envelope около2.95. Сам legacy native-suite выполнен:26clips PASS,214824assertions; мир/сервер не создавался.

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build -I tools/skim77-native.init.gradle -I tools/skim-offline.gradle captureSkim77 skimOfflineChecks
python tools/verify_skim77.py
python tools/verify_skim77_jar.py
python tools/skim77_previews.py
```

Java21/Minecraft1.21.1/NeoForge21.1.255, clean build **PASS1m54s**. [Production JAR](../../../build/libs/slavicmyths-1.9.7.30.jar): 7410resources / 1337PNG / 1717Java21classes; ZIP CRC и byte parity PASS, тестовых fixtures в JAR нет.

JAR SHA256: `c2dd2a3eb148ce88fa3bc7cd59964dd314f9dfb0642e2dc4d3ff5eca943e2375`.

Source fingerprint: `5d73e27b2d433454d01658707511a08db8febcdb98e47e3934be1d16bb0c5740`; [полный source manifest](build-source-sha256.json), [JAR квитанция](jar-check.json). Commit не создавался; точная версия описана hash manifest рабочего дерева, а не выдуманным source commit.

Цена:231cubes/1386quads/2772triangles для одного полного body pass; glow добавляет глазной проход и отдельный проход меток во время RAGE. Это измеримый объём геометрии, не FPS. [CPU sample benchmark](after/cpu-sample.json),200warmup+2000samples: IDLE≈19.9µs, ROAR≈35.2µs, GRIP≈36.3µs, DEATH≈39.2µs. JVM/GC и большая offline capture влияют на эти числа; GPU/FPS/MSPT не измерялись.

[PolyMC сохранён](polymc-preserved.json): активна только принятая 1.9.7.29 с SHA256 `023e7d198f74b8fb1c80cceb421cdbb8a808cde632f8d450f248cda5af4cd605`. Новая30 не устанавливалась; миры не менялись. #76 принят владельцем в чате, закрытие он выполняет сам. #77 не принят и остаётся OPEN.

## Один последующий прогон владельца

1. Сопоставить Скима с плакатом с4сторон/сверху/3⁄4: корпус, грива, морда/зубы,4лапы, один хвост; проверить глаза и RAGE днём/ночью.
2. Пройти полный бой: ходьба/бег, рёв под высоким/низким потолком в4направлениях, когти/прыжок/свист/фланг, удержание стоящей/присевшей цели и освобождение, усталость/падение.
3. Проверить Алёшу, multiplayer/перезаход и стабильность FPS; затем вынести своё решение по #77. До этого OWNER_PENDING.
