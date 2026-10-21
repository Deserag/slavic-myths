# Руническое сияние и совместимость зачарований — 1.9.7.44

**IN_PROGRESS / OWNER_PENDING. #104 остаётся OPEN.** Исполнительное поручение владельца
по двум игровым скриншотам: слабое сияние в инвентаре, отсутствующее на надетой броне
и незаметное на оружии, проверить vanilla и невозможность зачаровать модовую броню.
Новых запусков Minecraft/PolyMC/сервера/GameTest: **0**. Ручная приёмка не выполнена.

## Причины и исправления

У установленной 1.9.7.42 основной `armorCutoutNoCull` включает `VIEW_OFFSET_Z_LAYERING`,
то есть масштаб model-view 0.99975586. Рунический MAIN не включает его, но проверяет
EQUAL_DEPTH_TEST. Поэтому геометрия не совпадает по глубине. Это подтверждено исходниками
установленных зависимостей и отдельным GPU-регрессионным тестом реальных native meshes:
у перунитового и железного нагрудников с прежним несовпадением абсолютная RGB-разница
с отсутствующим эффектом = 0; после совпадения смещения разница >0.

Добавлен отдельный ARMOR RenderType с тем же VIEW_OFFSET_Z_LAYERING и fixed buffer.
Item MAIN/TRANSLUCENT_ITEM оставлены без этого смещения. Текстиль использует отдельный
factory clothing без armor offset, потому что его основной слой — entityCutoutNoCull.
EQUAL/depth-only masking сохранены; проход сквозь стены не добавлен. Чужие immediate/
outline sources без зарегистрированных buffers по-прежнему пропускаются безопасно.

Сила 1/2/3 рун: alpha 36/58/82 → 112/156/200 (рост 3.11/2.69/2.44 раза).
Интенсивность текстуры теперь smoothstep(0.08,0.65,r): чётче светящиеся полосы,
прозрачные промежутки сохранены. Порог добавочных оттенков согласован с новой alpha.
0 рун не создаёт overlay; количество берётся из actual Runes.list(stack), по предмету,
а не по вместимости или всему комплекту. Световые источники/частицы не добавлены.

Minecraft 1.21.1 задаёт совместимость зачарований item tags, а не только Java-классом.
В моде отсутствовали head/chest/leg/foot_armor tags. Добавлены все **22 ArmorItem**:
перунит/дракон/Полоз, четыре уникальные вещи, три наградных нагрудника, gambeson,
plate_cuirass и seven_league_boots. Включена durability через vanilla armor tag.
Нативные материалы/enchantment values не менялись. Непрочная декоративная текстильная
одежда, которая не является ArmorItem, не превращалась в броню.

Также восстановлены пропущенные vanilla class categories исходных Sword/Axe/добывающих
инструментов, PoolSpear/NightingaleDagger, двух SerpentCrossbow и durability WardShield.
Существующие entries и replace=false сохранены, специальные ограничения mace/throwing
не менялись. `tools/equipment_enchant44.py` повторяет только эти additive membership edits.
Уже наложенные зачарования не удаляются, компоненты сохранений не менялись.

## Доказательства и границы

- `before/`: прежние Style/RenderTypes/fragment shader и прежний source GPU preview.
- `SOURCE-GPU-0-3.png`: реальные исходные модели/UV/PNG железного и перунитового мечей
  и нагрудников с production GLSL, одна камера/масштаб, 0–3 руны, светлая/тёмная база.
- `DEPTH-REGRESSION.png`, `depth-regression.json`: колонка 1 — старое несовпадение глубины;
  колонки 2/3 — совпадающее смещение. Для оружия это обычные 1/2/3 руны.
- `shader-compile.json`: standalone OpenGL compile/link PASS; это не shader reload в Minecraft.
- `verification.json`: раскрытие vanilla+mod tag graph и eligibility Protection/Unbreaking/
  Mending для всех22, корректные head/foot special enchants; выборочные оружие/инструменты/
  crossbow/shield. Это data graph, не действие стола чар/наковальни в игре.
- `BUILD.log`: Java21 clean build + checkRune104Offline, **PASS 1m7s**.
- `verification.json`: 7442 ресурсов,1339PNG,1739Java21 classes; ZIP CRC/JSON/PNG/metadata/
  исходные ресурсы и compiled classes совпадают с JAR, offline fixtures отсутствуют.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Причина исчезновения equipped armor overlay | Native RenderType source + DEPTH-REGRESSION | PASS offline |
| Исправленный armor offset и отдельный textile path | RuneGlowRenderTypes/Buffers, clean build | PASS source; runtime NOT_RUN |
| Ярче/чётче оружие и броня,0–3 руны | SOURCE-GPU-0-3.png, style/shader, JVM batch checks | PASS offline; OWNER_PENDING |
| Vanilla sword/armor | Actual iron source GPU cases + общий hook без namespace filter | PASS offline; runtime NOT_RUN |
| Чары модовой брони,22предмета | verification.json, additive slot tags | PASS data graph; runtime NOT_RUN |
| Оружие/инструменты/crossbows/shield enchant categories | Generator + verification.json | PASS source/data; runtime NOT_RUN |
| Сочетание runes+enchants+trim; GUI/руки/экипировка/relog/MP | Игру не запускали | NOT_RUN |
| Производительность и художественная приёмка владельца | Измерений/решения нет | NOT_RUN |
| Production Java21 clean build/resources/JAR | BUILD.log, verification.json | PASS |
| Установка44 в PolyMC | installed.json: backup42, один JAR,6 соседних модов сохранены | PASS / TEST PREVIEW |

JAR: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.44.jar`.
SHA256: `30a127f7e2bb0a897d9548ea9c13e753a60b3ad814bff30f70c90520113cd981`.
PolyMC на момент аудита: установленная42, SHA
`ddbfccabb8ccf2609ce38474f75e051b284fd764df6881a80f71a2f0917f91e5`.
**44 не установлена; нельзя считать её эффект проверенным в текущей игре.**
Source — текущий HEAD плюс незакоммиченный diff; автоматического commit/push нет.
Предсуществовавшие изменения #88 linen в TextileClothingLayer/docs/tools сохранены;
версия43 была занята ими, для совмещённого source-кандидата выделена44.
Никакой приёмки #88 или всей цепочки и выпуска final PolyMC этим не заявлено.

Краткая ручная проверка после отдельной установки тестового кандидата:
железные и модовые меч/нагрудник с0/1/2/3 доступными рунами в GUI/руке/F5 днём и ночью;
Protection на нагруднике, Respiration только на шлеме, Feather Falling только на ботинках;
совместное сияние с vanilla enchant/trim, перезаход. Лишние руны сверх вместимости
обычным gameplay не устанавливаются; standalone3-rune iron source — проверка renderer.


## Последующая установка по поручению владельца

44 установлена в Slavic-Myths-1.21.1-Testing. Предыдущие строки об отсутствии установки
являются историей подготовки и заменены [квитанцией](installed.json): точный path,
SHA256, backup42, source base69a2db12 + незакоммиченные изменения,6соседних модов
сохранены, один Slavic JAR. Повторная проверка ресурсов/production JAR PASS до установки.
Игру/PolyMC/сервер/GameTest не запускали. Runtime NOT_RUN, OWNER_PENDING, не release.
