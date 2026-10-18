# Соловей-разбойник #57 — IN_PROGRESS

Обновление установки: по прямому указанию владельца модельная сборка956f4645 установлена в PolyMC Testing для ручной проверки; см. ../weapon-grip-58/POLYMC_INSTALLATION.json. Приёмка всё ещё NOT_RUN. Указания «не установлено» ниже описывают момент source-проверки до этого запроса.
Source-итерация 18.10.2026. Не художественная/игровая приёмка. Minecraft, PolyMC,
GameTest и сервер: 0 запусков. Версия 1.9.7.8 прежняя, JAR не установлен.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Две различные формы, четыре ракурса old/new | native-first-before-after.png, native-second-before-after.png; исходные baked ModelPart/UV, одинаковый масштаб | PASS source; owner NOT_RUN |
| Плотная первая / лёгкая вторая форма | отдельная грудная клетка, таз, предплечья/колени/кисти; 99/88 частей; лесной плащ из 3 широких панелей / две асимметричные полы | PASS source; owner NOT_RUN |
| QUICK/COMBO, оба клинка и контакт | native-combo.png, native-dash-onslaught.png; два фактических grip-point внутри ладони на каждой из 58 поз двух форм | PASS offline |
| Вдох/свист/волна, грудь и губы | native-inhale-wave.png; release16, ONSLAUGHT46 | PASS source; звук FAIL |
| Вихрь/опоры, рывок и два прыжка | native-whirlwind.png, native-dash-onslaught.png, native-leap.png, native-air-dive.png; физическое движение остаётся серверным | PASS source; runtime NOT_RUN |
| Смена формы / выход / смерть / reset | native-transform-break.png, native-death.png; render20 / phase40, все 55 кадров падения без пересечения пола, полный reset после загрязнения scales/visibility | PASS offline; relog NOT_RUN |
| AI, сюжет, hitbox .95×1.85, NBT, награды, held/icon | область src/main ограничена презентацией; NightingaleEntity/Renderer, предметы и исходный nightingale.png неизменны | PASS byte parity |
| UV, сборка Java21, production JAR | gates.json и verifyNightingale57; build без запуска игры | см. фактический gates.json |
| Реальные разные whistle/wind/foley и прослушивание | существующие OGG/mappings не менялись; новых принятых записей нет | FAIL / owner NOT_RUN |
| Установка / ручная оценка обоих обликов | не установлено, удерживается до аудио-приёмки #59 | BLOCKED |

## Реальные серверные точки

QUICK10; COMBO8/20/32 (duration36); release INHALE/WAVE/WHIRLWIND16;
DASH/ONSLAUGHT move8–18, стоп19, ONSLAUGHT удары22/34 и wave46.
LEAP launch10, AIR_DIVE10–36; удар при фактической посадке >=12, а не
вымышленный фиксированный кадр. BREAK_AWAY wave8; TRANSFORM40, renderer20.
Смерть: частицы32, loot/discard55. Эти методы сервера не редактировались.

## Что показывают изображения

Это проекции настоящих native ModelPart после production sample(...), с настоящим
UV-атласом. Не Minecraft screenshots и не доказательство качества движения в игре.
Сначала создан неизменяемый before-native.json.gz из a3a47d1e, затем изменён rig.
Восемь action sheets показывают выборку поз; воздушная поза не подменяет физику.
Контрольные 6 поз Тугарина побайтно совпали с baseline.

Высота idle укладывается в прежние 1.85. Весь силуэт с вынесенными вперёд клинками
и одеждой не помещается в квадрат .95: это явно открытое ограничение, не PASS
строгого footprint. Размер сущности не расширялся. Поворот головы ограничен35°,
чтобы длинные усы не следовали полному развороту головы через плечи.

Генератор --nightingale-model-only воспроизводит два atlas/JSON и общую Java
геометрию; Tugarin, sounds.json, язык и предметы не меняет. Исходные atlas1024²
сохраняют нативный UV; уменьшение картинки отдельно от UV не выполнялось.

Проверки: Java21 clean build; verifyNightingale57 (геометрия, реальные grip points,
48 фаз опоры, 55 кадров смерти, полный reset), verifyTugarinModels (общий bake/UV),
python tools/verify_nightingale_57.py (scope, детерминизм, ресурсы/compiled classes в JAR).
Повторная генерация проекций: python tools/nightingale_57_previews.py.
Задача остаётся OPEN: звук и ручная приёмка не выполнены.
