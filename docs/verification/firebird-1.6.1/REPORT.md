# Жар-птица 1.6.1 — результат 2026-10-10

Тестовый кандидат реализован для Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. По прямому уточнению пользователя создана новая сущность `slavicmyths:firebird` без яйца: существующей Жар-птицы в этой версии проекта не было. Старые registry IDs и пользовательские изменения сохранены; commit не создавался.

- `gradlew.bat clean build -I tools/firebird-offline.gradle firebirdOfflineChecks`: PASS, 1m 2s; 46 deprecation warnings.
- Offline: 200020 assertions PASS; 100000 draws: 9964 / 30025 / 60011. Schedule bounds и SavedData codec PASS; это не игровой relog.
- Resources/production: 23286 checks PASS; 1072 предыдущих классов и 5116 ресурсов сохранены, исключения — необходимые additive registration/category/lang/sound/damage tag/AT/version.
- JAR: `build/libs/slavicmyths-1.6.1.jar`; SHA256 `b008d1626776d7676adcc1f24739ecf07bb002ff0bb1f2d9d3f64235dd197ee7`.
- Новое: entity/ledger/native model 138 bones /267 cuboids, 256² atlas + emissive, три разных 64² item sprites, 12 sound families /28 original mono Vorbis files, damage type, пустая death loot table, RU/EN.
- Recipes: NOT ADDED. Spawn egg: NOT ADDED. World light blocks/fire/grief: NOT ADDED.
- Клиент: NOT TESTED; фактических запусков 0. Dedicated server startup: NOT TESTED; запусков 0. In-game screenshots/video: NONE.
- Visual fidelity, реальные коллизии и drop landing, audio listening, бой/3000 HP/Protection/absorption, multiplayer, настоящий restart/crash/unload и performance: NOT TESTED.

[Реализация и ограничения](../../firebird/IMPLEMENTATION.md), [ручная проверка](COMMANDS.md), [gates](resource-gates.json). Native preview является статической проекцией, не игровым скриншотом. Между ItemEntity/инвентарём и SavedData нет атомарности при аварийном завершении JVM; гарантия полного exactly-once при catastrophic crash не заявляется.
