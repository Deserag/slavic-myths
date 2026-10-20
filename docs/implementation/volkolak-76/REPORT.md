# #76 · Проклятый волколак · 1.9.7.29 · 20.10.2026

**IN_PROGRESS / OWNER_IN_GAME_PENDING. Issue остаётся OPEN.** Офлайн-реализация и проверки выполнены; игровая художественная/анимационная приёмка, multiplayer, реальный перезаход и FPS **NOT_RUN**. Реальных запусков Minecraft, PolyMC, GameTest и сервера: **0**. В PolyMC версия29 **не устанавливалась** по прямому запросу владельца.

## Источник и границы изменений

`SOURCE_HEAD e5924bd909113dc587496940b270f963fe259714 + origin/main a4f343f3421042736543dc2afa09cfc7f03bc65a → UNCOMMITTED working tree 1.9.7.29`.
Актуальные дополнительные чертежи/ТЗ из main применены через `merge --no-commit`. Merge и исполнительные изменения оставлены незакоммиченными: [AGENTS.md](../../../AGENTS.md) требует «Не делайте commit автоматически». Новых веток/PR, push и закрытия Issue нет. Ранее существовавшие локальные изменения UI/команд/документации сохранены; JAR включает весь текущий production tree, а не только файлы #76. Его точный состав фиксирует [source SHA256 manifest](build-source-sha256.json).

[Baseline provenance/SHA](before/source.json) и полные исходники исходной модели находятся в `before/`. Baseline — текущая модель92parts/94cuboids из источников установленной1.9.7.28, сверенная с полным manifest28. Точный JAR отклонённого скриншота владельца от19 октября **не установлен**: не приписываем ему эту версию без доказательства. Это ограничение provenance, не замена игрового скриншота выдуманным.

ТЗ: [A — исходная геометрическая схема](../../design/ISSUE_76_A_SOURCE_POSE_ATLAS.svg), [B — целевая анатомия](../../design/ISSUE_76_B_TARGET_ANATOMY_VIEWS.svg), [C — rig/UV/анимации](../../design/ISSUE_76_C_RIG_ANIMATION_UV.svg), [технический контракт](../../design/ISSUE_76_CURSED_VOLKOLAK_TECHNICAL_SPEC.md). SVG остаются ориентиром; фактические рендеры ниже получены из production Java.

## Что изменено

Новая собственная анатомия **69bones/77cuboids**: грудь12px шириной вместо18, таз9 вместо13, поясница7.5, шея6.6 вместо11, мозговой отдел головы8.8 вместо12; отдельные узкий лоб, вытянутая морда, нижняя челюсть, клыки, уши/внутренние уши и два открытых глазных островка. Передние конечности состоят из плеча10.5px, предплечья9.5, кисти/четырёх когтей; задние — бедро10, голень10.5, стопа/три пальца. Плечевые/шейные объёмы соединены с телом, убраны прежние крупные навесные пластины шерсти; хвост из четырёх уменьшающихся сегментов опущен. Тёмная кластерная шерсть, светлые подбородок и грудной V, шрамы только на прежних руке/рёбрах. Это не копия GreyWolf.

Причины прежнего эффекта «кузова»: грудной блок18×14×12 и широкая нижняя накладка; шея почти ширины головы; оболочка рёбер/шерсти скрывала талию и суставы; фронтальные глаза перекрывались окружающими объёмами. [Диагностика depth0.68/1.0](baseline-depth-diagnostic.png) на неизменённом baseline показывает, что depth1 лишь удлиняет ту же массу; саму анатомию не исправляет. **Renderer/Eyes layer не изменены**, depth0.68 сохранён. Заморожены ширина1.65, высоты1.9/2.8, eye-height1.55/2.35.

Убраны старые жёсткие длины11/контакт23.85: двухзвенное IK использует реальные pivot distances и sole фактической кисти/стопы. После того же5-тикового production blend опора решается повторно. При POUNCE сохранена временная диаграмма, новые лапы складываются в воздухе; ESCAPE укладывает реальные вершины всего rig на землю. GRAB охватывает замороженный серверный якорь жертвы0.7блока впереди/0.18вверх; сервер не меняется. Никаких новых мировых/игроковых tick searches.

512×512 body/eyes атласы пересобраны под фактические cuboids;77 независимых cube nets с2px полями. Светятся только два eye island, закрытие глаз **19≤t<49** сохранено и в part visibility, и в прежнем Eyes layer. [До CSV](before/parts-uv-pivots.csv), [после CSV](after/parts-uv-pivots.csv), [размеченный атлас](after/atlas-annotated.png); обычный и emissive PNG лежат в production ресурсах.

Production allowlist от полного baseline28:

- `src/main/java/org/slavicmyths/client/CursedVolkolakGeometry.java`
- `src/main/java/org/slavicmyths/client/CursedVolkolakModel.java`
- `src/main/resources/assets/slavicmyths/cursed_volkolak_geometry.json`
- `src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak.png`
- `src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak_eyes.png`
- `gradle.properties` —1.9.7.29.

Авторский источник: `tools/cursed_volkolak_76_model.py`, вызов через исправленный `tools/volkolak_143_resources.py --model-only`. Он пишет только четыре модельных output, ничего не записывает при идентичных bytes; код полной legacy-генерации находится после раннего выхода. Старый regression gate проверяет наличие всех анимируемых цепей вместо произвольного минимума70частей: добавление фиктивных кубов ради счётчика запрещено.

## Фактическое визуальное доказательство — NOT_IN_GAME

[Основной лист:16 сопоставимых видов](matched-16-views.png) — old/new × quad/upright × front/side/back/3quarter, одинаковые камера, масштаб, свет. [Переходы:5RISE+5DROP до/после](rise-drop-matched.png). [Силуэты без текстуры](silhouette.png), [голова/лапы крупно](details.png), [perspective рядом с неизменённым GreyWolf](greywolf-perspective-reference.png).

[Атаки A/B на одинаковых t](actions-matched-1.png), [ESCAPE/голосовые позы A/B](actions-matched-2.png). Полная новая матрица: [лапа/серии/укус](actions-1.png), [прыжок/фланг/нюх/слух/оскал](actions-2.png), [вой/alert/roar/stagger](actions-3.png), [GRAB held/unheld](grab-victim.png), [GRAB28 с нативным vanilla HumanoidModel](grab-held-reference.png), [ESCAPE0/19/30/48/49/60/87/102/131](escape.png).

Анимации из настоящих последовательных native frames: [RISE](rise.gif), [DROP](drop.gif), [ESCAPE](escape.gif). Манекен использует реальные vanilla cuboids на серверном якоре, с диагностическим синим материалом, а не скриншот игрока. Его тело остаётся различимо между/перед лапами; факт отсутствия runtime clipping проверяет владелец.

[Native baseline mesh](before/native-poses.json.gz) / [native current mesh](after/native-poses.json.gz): реальные ModelPart кубы, polygon vertices, UV и world pivots после production `sample()`/`blendPose()`. Текущий export содержит582позы, включая оба стиля walk96фаз, RISE17/DROP13 и ESCAPE132 последовательных кадра, всеMove с hit/prep/recover/end, GRAB в обоих состояниях,5-тиковые blend0..6, завершённые RISE/DROP→IDLE и yaw/pitch/hurt. `Cursed76Native` не создаёт entity/world и не запускает NeoForge. CPU z-buffer применяет фактический depth0.68, nearest pixel texture, одинаковое освещение; GL emissive/перспектива движка этим не доказываются.

## REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

PASS здесь означает конкретную офлайн-проверку, не принятие Issue.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Freeze актуального source baseline и pivots/UV | before/source.json,9сохранённых файлов, CSV, native baseline | PASS |
| Точный источник отклонённого screenshot19октября | Исходный screenshot/JAR не предоставлены для привязки, не угадываем | BLOCKED — provenance старого screenshot |
| Новый реальный rig, две стойки,16сопоставимых видов | matched-16-views.png, silhouette.png, native Java vertices | PASS — source implementation; эстетическая приёмка владельцем |
| Отдельные грудь/талия/таз/шея/голова/пальцы, единая иерархия | authored source,69bones77cubes, after CSV, details.png | PASS — source review |
| RISE0/4/7/8/11/12/15/16, DROP0/3/6/9/12 | rise-drop-matched.png,17+13 native frames/GIF | PASS |
| Задняя опора без смещения якорей в переходе | offline-checks.json: ground и XZ error<0.01model px | PASS |
| Цепи суставов соединены, конечности не растягиваются | actual world pivots; длины плечо/предплечье/бедро/голень во всех native позах | PASS |
| Настоящий5-тиковый blend и reset после каждогоMove | Java harness вызывает тот же blendPose, end-boundary и foot assertions; reset visibility/scales/angles | PASS |
| IDLE/ALERT/walk на месте и движение | static pad anchors,96walk samples; без продвижения phase от idle time | PASS — offline; world locomotion NOT_RUN |
| CLAW_L10,CLAW_R12,DOUBLE/TRIPLE,BITE | source hash frozen entity, native hit/prep/recover/end, actions1 и A/B1 | PASS — offline |
| SNIFF/LISTEN/SNARL/HOWL_SHORT/ALERT/ROAR/STAGGER | actions2/3, native Move matrix | PASS — offline |
| POUNCE/FLANK/GRAB held+unheld | actions2,grab-victim,grab-held-reference; targets по фактическому server anchor | PASS — offline |
| ESCAPE все132кадра и глаза19≤t<49,видимы49 | floor bounds, part visibility, unchanged Eyes.java, escape.png/GIF | PASS — offline |
| Geometry/UV positive/in-range, нет перекрытий чужих nets |77распаковок, hierarchy gate, native interior UV samples; offline-checks.json | PASS |
| Два512px атласа/две локальные glow mask islands | byte masks, eye depth-buffer visibility front/side/both3quarter, atlas-annotated | PASS — software renderer |
| Renderer depth/AI/hitbox/ID/story/loot/audio/прочие волки заморожены | весь production manifest28, исключены только5model files+version | PASS |
| Повторяемый генератор и чистый model-only output |2повтора, весь src/main SHA manifest byte-identical | PASS |
| Clean build Java21/Minecraft1.21.1/NeoForge21.1.255 | clean build + verifyCursed55 + verifyVolkolakModel + captureCursed76;62с | PASS |
| Production JAR/ресурсы/классы, без тестовых fixtures | jar-check.json:7408resources/1335PNG/1713Java21classes; ZIP integrity/parity | PASS |
| Цена модели/rig без обещания FPS |77vs94cubes, CPU sample bench, без мирового polling | PASS — offline CPU only |
| Игровая визуальная/анимационная/GL-emissive приёмка | запусков0, обязательная проверка владельцем | NOT_RUN |
| Реальные locomotion/collision/hit/victim contacts, no-target/MP sync/relog | offline fixture не подменяет игровой запуск | NOT_RUN |
| Реальный FPS/MSPT | не замерялись в игре | NOT_RUN |
| PolyMC установка29 | не запрошена; установленный28 SHA неизменён | NOT_RUN — NOT_UPDATED_BY_THIS_TASK |

Никакого DONE/ACCEPTED, owner-accepted label или закрытия #76: есть NOT_RUN и ограничение старого screenshot provenance.

## Проверки и артефакт

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build -I tools/cursed76-native.init.gradle captureCursed76 -PvolkolakModelOfflineCheck verifyCursed55 verifyVolkolakModel
python tools/verify_cursed76.py
python tools/verify_cursed76_jar.py
python tools/cursed76_previews.py
```

Java21 clean build PASS62с; прежний native regression37poses PASS (assert counter не заменяет матрицу); 38предметных офлайн-контрактов PASS. Учет отрисовки:77cubes/462quads/924triangles вместо94/564/1128 для одного полного mesh pass; Eyes использует прежний дополнительный проход. Это не измерение GPU/FPS. [CPU benchmark](after/cpu-sample.json):150warmup+1500sample без world, IDLE≈33.4µs, RISE≈13.9µs, GRAB≈12.0µs, ESCAPE≈35.6µs. Стенд чувствителен к JIT/GC/нагрузке. Худший зарегистрированный subpixel penetration в blend≈0.0373model px (0.00233блока), допуск<0.05px; переходные якоря стоп<0.01px. Это честные численные допуски, не обещание полного отсутствия clipping в Minecraft.

JAR: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.29.jar`

SHA256: `023e7d198f74b8fb1c80cceb421cdbb8a808cde632f8d450f248cda5af4cd605`

Source fingerprint: `7882f286e0d0294900ceabff4eedda7508d4f55f52ceadbe65779609f7a0a3be`; source не оформлен отдельным commit. [Полная квитанция](jar-check.json), [38контрактов](offline-checks.json). Результаты общей source/resources parity соответствуют текущему рабочему дереву, включая сохранённые до #76 изменения.

PolyMC28 остаётся по прежнему пути `C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.9.7.28.jar`, SHA256 `0d5076880f145dc38ea4a74380aa9f9285611708e072ce3485ed8f25bcb7138f` (перепроверен read-only). Ни замены, ни backup/установки29, ни запуска не выполнялось. **POLYMC_NOT_INSTALLED для29**, обновление не требуется по запросу.

## Один последующий игровой прогон владельца

1. Осмотреть проклятого волколака с4сторон днём и ночью: quad/walk→RISE→upright→DROP, силуэт/шея/глаза/опоры.
2. Проверить обе когтистые атаки, серии, укус, прыжок, захват с игроком, ESCAPE: контакты, глаза закрыты в ложной смерти и возвращаются; без исчезновения/разрывов.
3. Проверить no-target/hurt, перезаход и multiplayer, затем стабильность FPS; зафиксировать своё решение по #76. До этого Issue OPEN.
