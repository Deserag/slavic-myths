from pathlib import Path
import json, shutil, re
R=Path(__file__).resolve().parents[1];out=R/'docs/verification/firebird-1.6.1';gate=json.loads((out/'resource-gates.json').read_text());log=(R/'work/firebird/build.log').read_text(encoding='utf8',errors='replace')
shutil.copy2(R/'work/firebird/build.log',out/'build.log');shutil.copy2(R/'work/firebird/native-preview.png',out/'native-preview.png');shutil.copy2(R/'docs/firebird/audio-manifest.json',out/'audio-manifest.json')
duration=re.search(r'BUILD SUCCESSFUL in (.*)',log).group(1)
report=f'''# Жар-птица 1.6.1 — результат 2026-10-10

Тестовый кандидат реализован для Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. По прямому уточнению пользователя создана новая сущность `slavicmyths:firebird` без яйца: существующей Жар-птицы в этой версии проекта не было. Старые registry IDs и пользовательские изменения сохранены; commit не создавался.

- `gradlew.bat clean build -I tools/firebird-offline.gradle firebirdOfflineChecks`: PASS, {duration}; 46 deprecation warnings.
- Offline: 200020 assertions PASS; 100000 draws: 9964 / 30025 / 60011. Schedule bounds и SavedData codec PASS; это не игровой relog.
- Resources/production: {gate['checks']} checks PASS; {gate['baseline_preserved']['classes']} предыдущих классов и {gate['baseline_preserved']['resources']} ресурсов сохранены, исключения — необходимые additive registration/category/lang/sound/damage tag/AT/version.
- JAR: `build/libs/slavicmyths-1.6.1.jar`; SHA256 `{gate['sha256']}`.
- Новое: entity/ledger/native model 138 bones /267 cuboids, 256² atlas + emissive, три разных 64² item sprites, 12 sound families /28 original mono Vorbis files, damage type, пустая death loot table, RU/EN.
- Recipes: NOT ADDED. Spawn egg: NOT ADDED. World light blocks/fire/grief: NOT ADDED.
- Клиент: NOT TESTED; фактических запусков 0. Dedicated server startup: NOT TESTED; запусков 0. In-game screenshots/video: NONE.
- Visual fidelity, реальные коллизии и drop landing, audio listening, бой/3000 HP/Protection/absorption, multiplayer, настоящий restart/crash/unload и performance: NOT TESTED.

[Реализация и ограничения](../../firebird/IMPLEMENTATION.md), [ручная проверка](COMMANDS.md), [gates](resource-gates.json). Native preview является статической проекцией, не игровым скриншотом. Между ItemEntity/инвентарём и SavedData нет атомарности при аварийном завершении JVM; гарантия полного exactly-once при catastrophic crash не заявляется.
'''
(out/'REPORT.md').write_text(report,encoding='utf8')
spec=R/'work/firebird-spec/Slavic_Myths_1.6.1_Firebird_Codex_TZ/12_ACCEPTANCE_AND_TESTS.md';rows=[s.strip()[6:] for s in spec.read_text().splitlines() if s.strip().startswith('- [ ] ')]
(out/'QA_MATRIX.md').write_text('# Runtime acceptance — OPEN\n\nКаждая строка ниже требует игровой проверки; build/codec/resources отдельно в REPORT.\n\n'+'\n'.join(f'- NOT TESTED: {s}' for s in rows)+'\n',encoding='utf8')
text=f'''## Жар-птица 1.6.1 — 2026-10-10

Тестовый кандидат: новая `slavicmyths:firebird` без яйца по уточнению пользователя; Overworld nighttime SavedData event/+20…30 дней, единый бюджет 2/3 drops и 10/30/60, три ресурса без рецептов, 3000 HP/20 HP defensive armor bypass, native rig138 bones/267 cuboids/fullbright и 28 оригинальных OGG. Существующий `ognennoe_pero` и прежние механики сохранены. [Архитектура и ограничения](firebird/IMPLEMENTATION.md).

Clean build PASS ({duration}), 200020 offline assertions, {gate['checks']} resource/production checks; **запусков Minecraft: 0**. Fidelity/звук/бой/полёт/MP/relog/MSPT и dedicated server startup NOT TESTED; приёмка OPEN. [Отчёт](verification/firebird-1.6.1/REPORT.md), [ручная проверка](verification/firebird-1.6.1/COMMANDS.md). Runtime не выдан за компиляцию.

'''
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=R/'docs'/name;s=p.read_text(encoding='utf8');p.write_text(text+s,encoding='utf8')
print('Reports and three project documents updated; runtime remains open.')
