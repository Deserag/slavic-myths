# Сивка-Бурка 1.6.0 — тестовый кандидат

Собран и установлен кандидат; полная приёмка **OPEN_RUNTIME_NOT_RUN**. Клиентских/серверных запусков агентом: **0**. Игровых скриншотов/видео, dedicated-server startup, multiplayer, реального перезахода и измеренных MSPT/FPS нет.

- Среда: Minecraft1.21.1 / NeoForge21.1.255 / Java21 / official mappings; modid `slavicmyths`. Ветка main, базовый commit 1b702b69d5d5c6b2aa36c7488a0845451b5af9c5; пользовательское dirty дерево сохранено, нового commit нет.
- `gradlew.bat -I tools/sivka-offline.gradle clean build sivkaOfflineChecks`: exit0, 1m 35s; 7107 offline checks PASS. 42 existing-style deprecation warnings; tests excluded from production.
- Resource/production gates: **12878 PASS**; 1055 прежних классов и 5290 ресурсов сохранены побайтно относительно1.5.6. Lang/sounds расширены с сохранением прежних ключей.
- JAR: `D:\slavic-myths\build\libs\slavicmyths-1.6.0.jar`. SHA256 `0e0bcc1abc9dce2501e152cd6544795fa137e7ec9fd205b09cc5f368a31c9c61`.
- PolyMC: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.6.0.jar`; предыдущая1.5.6: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-sivka-20261010-132621\slavicmyths-1.5.6.jar`; 357 остальных файлов неизменны. Старый1.16.5 и игровые миры не изменены.

Native-модель: `src/main/java/org/slavicmyths/client/SivkaMesh.java`, редактируемый источник `docs/sivka/source/native-rig.json`, UV `docs/sivka/source/uv-regions.png`; 152кубоида/51узел,2.975блока,atlas256² + selective glow. Статическое actual-native превью `native-preview.png` просмотрено, но не является игровым доказательством fidelity/animation/seat.

Ключевые кадры: `SivkaPoses.java`, supplied timeline → `assets/slavicmyths/animations/sivka_burka.json`, editable `docs/sivka/source/keyframes.csv`; runtimecontroller `SivkaClient.Model`. Свисток64²: `textures/item/sivka_whistle.png`.

Питание: `minecraft:apple`, `minecraft:wheat`, `minecraft:carrot`, `slavicmyths:blueberry`, `slavicmyths:blueberry_pancakes`, `minecraft:honey_bottle`, `minecraft:golden_apple`. Все продукты реально существовали; mod berries natural taiga feature и рецепт pancake проверены. Timer72000 online ticks, expiry сохраняет stage/отзываетtoken; recovery apple; stage7 permanent. Owner ledger `SivkaBonds`, per-owner generation; безопасный local summon5–12/40проб, без forcedchunks, 25s ожидание ухода.

Управление: Ctrl+W разгон, >=9b/s в течение50непрерывных sprint-ground ticks → новый Space press; ground12/air16b/s; в воздухе Space вверх, **X вниз по ответу пользователя**, Shift спешиться/SlowFalling80ticks. Physics zero-mean wave .025b; визуальная волна отдельно. Lethal retreat:6000 active ticks recovery. Естественное появление: три открытых grassy биома, группа1/вес1/.002 placement; **наблюдений0, дистанция исследования0**, реальная встречаемость не измерена.

Звуки:31mono44100Vorbis /21event, реальное CC0 ржание/дыхание + оригинальный foley/magic synthesis. Авторство и URL — [audio_provenance.md](../../sivka/audio_provenance.md); actual audio properties/hash — `audio-manifest.json`. Декодирование/пики проверены, прослушивание в игре NOT TESTED. Все cues/subtitles зарегистрированы. FX native restrained wax particles + shaderless selective glow.

Проверенные команды: `python -X utf8 tools/sivka_resources.py`, `python -X utf8 tools/sivka_audio.py`, `gradlew.bat -I tools/sivka-offline.gradle clean build sivkaOfflineChecks`, `python -X utf8 tools/sivka_preview.py`, `python -X utf8 tools/verify_sivka_160.py`, `python -X utf8 tools/install_sivka_160.py`. Python — bundled Codex3.12; Gradle JDK21 из указанного пользователем пути. Игровые команды только записаны в [COMMANDS.md](COMMANDS.md), не выполнялись.

Полная [матрица приёмки](QA_MATRIX.md) сохраняет каждый сценарий ТЗ; каждый runtime-case NOT TESTED. [Реализация/архитектура и ограничения](../../sivka/IMPLEMENTATION.md). Перед выпуском требуется ручная survival/visual/audio/collision/network/restart приёмка. Сборка не заменяет игровой тест.
