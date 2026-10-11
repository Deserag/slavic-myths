"""Write factual delivery evidence after production/install gates."""
from pathlib import Path
import json,re,shutil
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/verification/sivka-1.6.0';WORK=ROOT/'work/sivka';SPEC=ROOT/'work/sivka-spec/Sivka_Burka_1.6_Codex_TZ'
g=json.loads((OUT/'resource-gates.json').read_text());i=json.loads((OUT/'polymc-installation.json').read_text());assert g['status']=='PASS' and g['sha256']==i['production_sha256'] and i['minecraft_launches']==0
log=(WORK/'build.log').read_text(encoding='utf8');assert 'BUILD SUCCESSFUL' in log;offline=int(re.search(r'SIVKA OFFLINE PASS: (\d+)',log).group(1));duration=re.search(r'BUILD SUCCESSFUL in (.+)',log).group(1)
shutil.copyfile(WORK/'build.log',OUT/'build.log');shutil.copyfile(WORK/'native-preview.png',OUT/'native-preview.png');shutil.copyfile(WORK/'audio-source/manifest.json',OUT/'audio-manifest.json')
rows=[]
for line in (SPEC/'06_qa/01_ACCEPTANCE_MATRIX.md').read_text(encoding='utf8').splitlines():
    if re.match(r'\| [VASFWMPB]\d\d \|',line):
        cells=[v.strip() for v in line.strip('|').split('|')];key,priority,scenario=cells[:3];status='PASS' if key in ('V05','B01','B04') else 'NOT TESTED';proof={'V05':'resource-gates.json: atlas256/item64','B01':'build.log; production JAR hash','B04':'REPORT.md + this full matrix'}.get(key,'Minecraft launches: 0. Source/assets present; runtime evidence absent.');rows.append(f'| {key} | {priority} | {status} | {scenario} | {proof} |')
(OUT/'QA_MATRIX.md').write_text('# Матрица приёмки 1.6.0\n\nОтмечены только реально проверенные исходы. NOT TESTED не означает PASS. Полная игровая приёмка OPEN.\n\n| ID | Priority | Result | Original scenario | Evidence |\n|---|---|---|---|---|\n'+'\n'.join(rows)+'\n',encoding='utf8')
report=f'''# Сивка-Бурка 1.6.0 — тестовый кандидат

Собран и установлен кандидат; полная приёмка **OPEN_RUNTIME_NOT_RUN**. Клиентских/серверных запусков агентом: **0**. Игровых скриншотов/видео, dedicated-server startup, multiplayer, реального перезахода и измеренных MSPT/FPS нет.

- Среда: Minecraft1.21.1 / NeoForge21.1.255 / Java21 / official mappings; modid `slavicmyths`. Ветка main, пользовательское dirty дерево сохранено, commit не сделан.
- `gradlew.bat -I tools/sivka-offline.gradle clean build sivkaOfflineChecks`: exit0, {duration}; {offline} offline checks PASS. 42 existing-style deprecation warnings; tests excluded from production.
- Resource/production gates: **{g['checks']} PASS**; {g['preserved_classes']} прежних классов и {g['preserved_resources']} ресурсов сохранены побайтно относительно1.5.6. Lang/sounds расширены с сохранением прежних ключей.
- JAR: `{g['jar']}`. SHA256 `{g['sha256']}`.
- PolyMC: `{i['installed_jar']}`; предыдущая1.5.6: `{i['previous_jar']}`; {i['other_files_preserved']} остальных файлов неизменны. Старый1.16.5 и игровые миры не изменены.

Native-модель: `src/main/java/org/slavicmyths/client/SivkaMesh.java`, редактируемый источник `docs/sivka/source/native-rig.json`, UV `docs/sivka/source/uv-regions.png`; 152кубоида/51узел,2.975блока,atlas256² + selective glow. Статическое actual-native превью `native-preview.png` просмотрено, но не является игровым доказательством fidelity/animation/seat.

Ключевые кадры: `SivkaPoses.java`, supplied timeline → `assets/slavicmyths/animations/sivka_burka.json`, editable `docs/sivka/source/keyframes.csv`; runtimecontroller `SivkaClient.Model`. Свисток64²: `textures/item/sivka_whistle.png`.

Питание: `minecraft:apple`, `minecraft:wheat`, `minecraft:carrot`, `slavicmyths:blueberry`, `slavicmyths:blueberry_pancakes`, `minecraft:honey_bottle`, `minecraft:golden_apple`. Все продукты реально существовали; mod berries natural taiga feature и рецепт pancake проверены. Timer72000 online ticks, expiry сохраняет stage/отзываетtoken; recovery apple; stage7 permanent. Owner ledger `SivkaBonds`, per-owner generation; безопасный local summon5–12/40проб, без forcedchunks, 25s ожидание ухода.

Управление: Ctrl+W разгон, >=9b/s в течение50непрерывных sprint-ground ticks → новый Space press; ground12/air16b/s; в воздухе Space вверх, **X вниз по ответу пользователя**, Shift спешиться/SlowFalling80ticks. Physics zero-mean wave .025b; визуальная волна отдельно. Lethal retreat:6000 active ticks recovery. Естественное появление: три открытых grassy биома, группа1/вес1/.002 placement; **наблюдений0, дистанция исследования0**, реальная встречаемость не измерена.

Звуки:31mono44100Vorbis /21event, реальное CC0 ржание/дыхание + оригинальный foley/magic synthesis. Авторство и URL — [audio_provenance.md](../../sivka/audio_provenance.md); actual audio properties/hash — `audio-manifest.json`. Декодирование/пики проверены, прослушивание в игре NOT TESTED. Все cues/subtitles зарегистрированы. FX native restrained wax particles + shaderless selective glow.

Проверенные команды: `python -X utf8 tools/sivka_resources.py`, `python -X utf8 tools/sivka_audio.py`, `gradlew.bat -I tools/sivka-offline.gradle clean build sivkaOfflineChecks`, `python -X utf8 tools/sivka_preview.py`, `python -X utf8 tools/verify_sivka_160.py`, `python -X utf8 tools/install_sivka_160.py`. Python — bundled Codex3.12; Gradle JDK21 из указанного пользователем пути. Игровые команды только записаны в [COMMANDS.md](COMMANDS.md), не выполнялись.

Полная [матрица приёмки](QA_MATRIX.md) сохраняет каждый сценарий ТЗ; каждый runtime-case NOT TESTED. [Реализация/архитектура и ограничения](../../sivka/IMPLEMENTATION.md). Перед выпуском требуется ручная survival/visual/audio/collision/network/restart приёмка. Сборка не заменяет игровой тест.
'''
(OUT/'REPORT.md').write_text(report,encoding='utf8')
common=f'''## Сивка-Бурка 1.6.0 — 2026-10-10

По новому пакету реализован тестовый кандидат: отдельный белый native-конь152кубоида/51узел, артикулированные наземный/воздушный галоп,7этапов кормления, canonical SavedData/temporary72000online ticks/permanent bond, свисток, безопасный призыв, saddleless riding/разбег/полёт/уход,80HP/retreat, редкий native spawn,31новый OGG и RU/EN. По уточнению пользователя снижение на **X**, как у метлы/ступы; Space вверх, Shift спешивание. Кот Баюн/квесты/новые milestone не добавлены.

Clean build PASS ({duration}), {offline}offline /{g['checks']}resources-production PASS; сохранены {g['preserved_classes']}классов/{g['preserved_resources']}ресурсов1.5.6. Кандидат установлен в существующий PolyMC1.21.1 с backup; {i['other_files_preserved']}остальных файлов неизменны. **Запусков Minecraft:0**; gameplay/visual/audio/MP/relog/MSPT приёмка OPEN, все runtime-строки NOT TESTED. Статическое превью/codec не заменяют игру. [Отчёт](verification/sivka-1.6.0/REPORT.md), [краткая ручная проверка](verification/sivka-1.6.0/COMMANDS.md).

'''
arch='''`sivka`: `SivkaContent` расширяет прежние registries/spawn placement; `SivkaBonds` один overworld SavedData owner→bond, online-play timer, token/generation, адресный UUID lookup в загруженном измерении. `SivkaWhistle` bounded safe search/reuse/generation, custom data UI, серверная валидация; `SivkaEntity` локальные стандартные goals и server physics/native move, last-input timeout/collision/landing/retreat. `SivkaNetwork` intent-only finite/rate/owner/passenger/entity/sequence validation. Null controlling passenger не отдаёт movement authority клиентскому vanilla vehicle packet. Client-only `SivkaClient/SivkaMesh`, native eased `SivkaPoses`, weak per-entity visual cache. Движение не teleport/noPhysics; forcedchunks/global AI/world scans нет. Единственный player tick адресно обновляет обязательный active timer существующей связи; inventory reconciliation1Hz. `tools/sivka_resources.py` воспроизводит geometry/poses/textures/lang/sounds; editable sources/docs и opt-in offline fixtures исключены из JAR.

'''
for name in ('PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md'):
    p=ROOT/'docs'/name;old=p.read_text(encoding='utf8');prefix=common+(arch if name=='ARCHITECTURE.md' else '')
    if old.startswith('## Сивка-Бурка 1.6.0 —'):old=old[old.index('\n## ',5)+1:]
    p.write_text(prefix+old,encoding='utf8')
print('Factual report/status/matrix written; game launches 0; acceptance OPEN.')
