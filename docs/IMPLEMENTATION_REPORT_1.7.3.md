# Реализация Slavic Myths 1.7.3 — 2026-10-11

Реализован пакет пользователя Slavic_Myths_1.7.3_Codex_TZ_References_Audio_Summoning.zip. Мод 1.7.3 установлен в подтверждённый Slavic-Myths-1.21.1-Testing. Minecraft **1.21.1**, NeoForge **21.1.255**, Java **21**; registry/mod ID прежних систем сохранены. Клиент не запускался, полная игровая приёмка остаётся открытой.

## Результат

Алёша: собственная slim модель 2.15 блока, 175 HP, светлые волосы/голубые глаза/чистое лицо, сабля с отдельным силуэтом, лук и колчан, segmented cloak. 29 bones/128 cuboids, atlas256. Серверные taunt/dodge/counter/слабая серия из четырёх ударов/ограниченная серия стрел, daring, last stand и recovery; ни PvP, ни пассивных животных как целей. Однократная защита от lethal hit не даёт постоянной неуязвимости. Обычный recall сохраняет HP; recovery сохраняет занятый слот.

Ским: 750 HP, собственная quadruped модель с 46 bones/151 cuboids, гривой, бивнями, когтями и хвостом, atlas256. Три claw windows, прыжок с фиксируемой целью, whistle/roar/flank/grip, rescue, fatigue/exhaustion и одна rage transition. Visual rear около4.46 блока, низкий потолок выбирает ground roar. Collision остаётся наземной, удлинённой по yaw; routing требует ручного наблюдения. Серверные телеграфы синхронизированы с native client pose.

Авторская долина65×65, северное логово с одной камерой и двумя обычными сундуками, каменный проход после победы; лагерьSW, костиE, безопасный подходS. Размещение явно командой на проверенной свободной сухой площадке, максимум256 blocks/tick, только загруженные чанки. Изменённые игроком pending cells блокируют job вместо перезаписи. **Автоматический natural worldgen не добавлен.** Арена/герой/история сохраняются в Overworld SavedData; незавершённый бой после рестарта сбрасывается, победа продолжает открытие ворот. Force-loading отсутствует в production; тестовый fixture временно использовал свои чанки отдельного тестового мира.

Четыре многоразовых предмета: alyosha_call_whistle, ilya_call_token, dobrynya_call_emblem, beast_challenge_horn. Server use frame/cooldown, максимум2 героев через старые и новые входы, запрет замены в бою, matched story hero. Предмет Добрыни сохраняет существующее требование знаний. Три настоящих spawn eggs для Скима/Идолища/Маринки; Маринка вне story arena имеет ограниченный debug бой без личной победы. Все предметы в прежней Creative Tab, distinct64px icons, recipes, RU/EN messages/subtitles.

48 исходных синтетических OGG из пакета с SHA provenance, Vorbis48k mono; человеческая озвучка не заявляется. Уникальные награды и future milestone1.7.6 не добавлены.

## Использование

- `/give @s slavicmyths:alyosha_call_whistle`
- `/give @s slavicmyths:beast_challenge_horn`
- `/slavicmyths skim_valley place` — op2, размещает долину примерно70 блоков впереди на валидируемой свободной площадке; `/slavicmyths skim_valley status` показывает состояние.
- Призвать Алёшу, подойти к каменному маркеру у южного входа и использовать рог. Не начинается автоматически и не создаёт второго героя.
- `/summon slavicmyths:skim_beast` — debug boss, без сюжетной победы вне активной арены.

## Проверки

`JAVA_HOME=C:/Program Files/Java/jdk-21.0.12` задавался для команд. Выполнены:

- `gradlew.bat clean build --console=plain` — PASS; стандартный test task NO-SOURCE, это не заменяет отдельные проверки.
- `gradlew.bat -I tools/skim-runtime.init.gradle runSkimChecks -PskimRun=final-02 -PwithoutJei --console=plain` —44 PASS.
- `gradlew.bat -I tools/bogatyr-runtime.init.gradle runBogatyrChecks -PbogatyrRun=173-regression -PwithoutJei --console=plain` —48 PASS.
- `gradlew.bat -I tools/marinka-runtime.init.gradle runMarinkaChecks` с отдельным regression run —36 PASS (полная команда в логе).
- Отдельный последовательный difficulty fixture и write/read fixture двумя реальными процессами —PASS.
- `gradlew.bat -I tools/skim-offline.gradle skimOfflineChecks --console=plain` —183715 native assertions,26 профилей. Это не игровой renderer.
- `python tools/verify_skim.py` —35782 checks PASS: JSON/PNG/UV, sound source hashes и полный ffmpeg decode, Java65, common/client isolation, source/JAR соответствие, отсутствие test leakage, генератор повторно0 изменений.

Реальные server launches: **15**, включая неуспешные итерации; client launches: **0**. Итоговые44+48+36 проверки успешны. Ранние ошибки fixture claw positioning и лишнего нового READY-запрета Маринки исправлены и проверены повторно. Отдельная ранняя ошибка release checker учитывала CRLF в expanded TOML; исправлена нормализация только metadata. 7220 старых JAR entries побайтно идентичны; изменения ограничены metadata/additive locale/sounds и нужными интеграционными classes. Изменённый synthetic MarinkaEntity$1 имеет идентичные javap instructions.

Diamond leap: Easy **3.0570564**, Normal **7**, Hard **13.0073595** фактических HP; требование6HP выполнено для Normal. Heavy claw/roar/flank Normal6.1, light claw2.5, whistle1; Resistance уменьшает7 до5.6, глобального armor bypass нет. Protection enchant отдельно не проверялся.

[Полная матрица и ручные пункты](verification/skim-1.7.3/TEST_MATRIX.md). Не выполнялись игровой осмотр/day-night/first-person/all animation clips, прослушивание, реальные2 клиента, клиентский relog/teleport и disconnect при захвате, длительная навигация/DPS,10-минутные измерения MSPT. Offline previews обозначены NOT A GAME SCREENSHOT. Сборка установлена как проверенный серверными/ресурсными gates кандидат; это не отметка PASS для всей54-кейсовой игровой приёмки.

## Установка

Production JAR: `D:/slavic-myths/build/libs/slavicmyths-1.7.3.jar`; release copy: `D:/slavic-myths/release/slavicmyths-1.7.3.jar`.

Installed: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.7.3.jar`.

SHA256 всех трёх: `6496e20b1d1dfc39ae3c05eead9e45126991c0ef10db33ad158676cb601133e6`.

Backup1.7.2: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-skim-20261011-194637\slavicmyths-1.7.2.jar`; SHA25698942b2a4c3f07e056f92f46fa8bf026a35b3ba6c18ca2265cac8d904b2ff33b.

260 остальных файлов экземпляра проверены и сохранены. Старый1.16.5 не затрагивался. Пользовательские миры не перезаписывались, commit не создавался. [Receipt](verification/skim-1.7.3/polymc-installation.json).

## Основные файлы

`skim/SkimActor`, `AlyoshaEntity`, `AlyoshaArrow`, `SkimBeastEntity`, `HeroCalls`, `HeroCallItem`, `SkimValley`, `ValleyBlueprint`, `SkimEvents`, `SkimContent`; клиентские `SkimClient`, `SkimModel`, `AlyoshaMesh`, `SkimMesh`. Из старых файлов изменены только registration/StoryPolicy/hero arrival-recall standby/Marinka standalone debug integration и версия. Старые исходные тесты не переписаны. Инструменты skim_assets/skim_preview/verify_skim/install_skim и opt-in tools/skim-* находятся вне production JAR. Пользовательский большой dirty tree предыдущих версий сохранён.
