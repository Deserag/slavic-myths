# Карачун + Морозко #58 — IN_PROGRESS

Обновление установки: по прямому указанию владельца модельная сборка956f4645 установлена в PolyMC Testing для ручной проверки; см. ../weapon-grip-58/POLYMC_INSTALLATION.json. Приёмка всё ещё NOT_RUN. Указания «не установлено» ниже описывают момент source-проверки до этого запроса.
Исходная итерация 18.10.2026. Не приёмка, не игровой тест. Runtime0, версия прежняя,
PolyMC не обновлён; озвучка и ручная оценка владельца остаются невыполненными.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Правый хват за древко у обоих | native-*-idle-walk.png; pivot (0,2,0) внутри охватывающей ладони вместо прежних смещений (-3,2,-3)/(-2,2,-3.5); groove/fingers используют старый hand UV | PASS offline; owner NOT_RUN |
| Карачун 1..3: две руки и управляемый замах | native-karachun-actions.png; actual upper14/fore10, target по оси древка y=-6; 65 промежуточных тиков на действие | PASS offline; runtime NOT_RUN |
| Карачун 4..9: отдельные жесты | магическая постановка свободной руки сохранена, ориентация оружия у правой кисти ограничена | PASS source |
| Морозко idle/walk/1/2/3..8 | native-morozko-actions.png; grip не отсоединяется, оружие компенсирует плечо/локоть вместо качания всей косы/посоха | PASS source; owner NOT_RUN |
| Цвет, клинок, кристаллы, UV, glow | Все PNG/OGG/JSON в src/main/resources побайтно сохранены | PASS parity |
| Оружие игрока / inventory | Оба *_held.json и *_icon.json, их UV и текстуры неизменны; #29 не реализуется этим патчем | PASS offline; in-game NOT_RUN |
| AI/фазы/боёвка/XP/NBT/hitbox | Ни один common/server файл не изменён; phase>=4/yield/дубль проверяются native samples | PASS source; relog NOT_RUN |
| Build/production JAR | gates.json, verifyGrip58 и verify_weapon_grip_58.py | см. фактические gates |
| Реальные новые звуки косы/льда/посоха | новые записи отсутствуют; старые mappings и quarantine не подменялись | FAIL; listening NOT_RUN |
| Оба босса в установленном JAR приняты владельцем | установка отсутствует, аудио gate #59 остаётся | BLOCKED |

## Исходные тайминги, без правки сервера

Карачун: удары1/2 tick10, удар3 tick14; касания4/6 tick16, 5 tick20,
7/8 tick24, marker9 живёт отдельным серверным таймером. Длительности28/40/32/36/40/26.
Морозко: удар1 tick10, bolt2 tick13, storm3 tick50, spike4 tick5,
breath5 tick16/30/44, wave6 tick24, clones7 tick7, storm8 tick48.
Целью было согласовать existing action с кистью, а не придумать новый удар.

Native-изображения построены из реального ModelPart + production sample(...),
с сохранёнными atlas512 и UV. Это не игровые скриншоты. До-архив создан из
f61bb7bb до редактирования mesh/поз. Контакт проверяется transform chain и точками
на настоящем древке, а не только наличием child hand_right. Левой руке задаётся
геометрически достижимый путь, правая остаётся ведущей. Руки в магии свободны.

WeaponGrip58 — client-only: quaternion compensation в реальном порядке ModelPart
Rz*Ry*Rx и ограниченный двухзвенный solve, без мирового поиска/тики/физики.
Серверные clocks/ID/данные сохранения не трогаются. Mesh post-emit вызывается из
обоих канонических генераторов; он идемпотентен, не пишет atlas/held/icon/audio.
Полные legacy asset генераторы ради этого исправления не запускались.

Ручная проверка: оба босса idle/walk, три удара косы, strike/point/cast посоха,
yield/phase>=4 и IceDouble. Эти пункты NOT_RUN до проверки владельцем.

Ограничения evidence: проверка непересечения охватывает древко с консервативным
радиусом1.6/2.3 native units и основной torso AABB, а не полную коллизию каждого
волоса/элемента плаща/клинка. На closeups cyan — фактический центр правой ладони,
magenta — левой в 1..3. Для Морозко7 before был архивирован на tick5, after —
на действительном cast7; разные номера честно подписаны. Не выдаётся за один тик.

Финальная Java21 clean build PASS (1m55s): verifyGrip58 2063451 checks,
повторные verifyNightingale57 560578 и общий bake/UV 10640. Resource/JAR gate
1947809 checks; SHA25665894e8c63812201bd50ee17da29fe19d8ffe1a8d56fc1a66cfd70d22a03a7ba.
Gradle вывел существующие deprecation/unchecked warnings, ошибок нет.
Никаких Minecraft/PolyMC/GameTest/server запусков.
