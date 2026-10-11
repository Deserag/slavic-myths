"""Write honest status only after exact-JAR build/resource/install proof. No production edits."""
from pathlib import Path
import json,hashlib,re,shutil
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/verification/earth-1.5.6'
r=json.loads((OUT/'resource-gates.json').read_text());o=json.loads((OUT/'offline-gates.json').read_text());i=json.loads((OUT/'polymc-installation.json').read_text())
sha=lambda p:hashlib.sha256(Path(p).read_bytes()).hexdigest()
assert r['production']==r['resources']==o['build']==o['offline_checks']=='PASS'
assert sha(r['jar'])==r['sha256']==o['production_sha256']==i['production_sha256']==sha(i['installed_jar'])
assert o['runtime_validation']=='NOT_RUN' and o['minecraft_launches']==i['minecraft_launches']==0
log=(ROOT/'work/earth/final-build.log').read_text(encoding='utf-8-sig');seconds=re.search(r'BUILD SUCCESSFUL in (\d+)s',log)[1]
shutil.copy2(ROOT/'work/earth/final-build.log',OUT/'build.log');shutil.copy2(ROOT/'work/earth/native-preview.png',OUT/'native-preview.png')
report=[
'# Дух земли 1.5.6 — 2026-10-10',
'',
'Пакет MODULAR V2 реализован с уточнениями пользователя; 72 файла манифеста сверены.',
'Босс: 800 HP, рост 6.5, три фазы, пять основных способностей, кулак, шипы и временная броня.',
'Арена: пять расходуемых столбов, два укрытия, ворота, преддверие; только новые Древние глубины.',
'Сон через 120 секунд сохраняет HP/фазу/столбы и открывает ворота; редкая руна только за настоящую победу.',
'Запирающий камень Духа земли вместо яйца: `/give @s slavicmyths:earth_heart_seal`, поставить и ударить. Ручной вызов — без редкой награды.',
'Оригинальная native-модель: 95 cuboids, суставные анимации, три 256px атласа, 64px руна; 30 OGG-прототипов архива.',
f'Финальный `gradlew.bat clean build -I tools/earth-offline.gradle earthOfflineChecks`: PASS, {seconds} секунд; {o["checks"]} offline-проверок.',
f'Ресурсы/production: PASS, {r["checks"]} проверок; {r["preserved_classes"]} прежних классов и {r["preserved_resources"]} ресурсов сохранены; генератор воспроизводим.',
f'JAR SHA256: `{r["sha256"]}`.',
f'Установлено в PolyMC Slavic-Myths-1.21.1-Testing; прежние JAR в backup вне mods; {i["other_files_preserved"]} остальных файлов неизменны; 1.16.5 не тронута.',
'Minecraft автоматически не запускался: **0 запусков**. Игровая приёмка OPEN; компиляция/codec/preview не заменяют бой, анимации, настоящий перезаход или измерение FPS/MSPT.',
'Есть предупреждения deprecated API; тестовые классы не входят в production JAR. Баланс конфигов изменяется с перезапуском; первый игровой QA — defaults.',
'[Команды и краткие проверки](COMMANDS.md) · [Карта реализации и ограничения](IMPLEMENTATION.md) · [Static native preview](native-preview.png).'
]
assert len(report)<=15;(OUT/'REPORT.md').write_text('\n'.join(report)+'\n',encoding='utf-8')
title='## Дух земли 1.5.6 — 2026-10-10'
sections={
'PROJECT_STATUS.md':f'''{title}

Реализован пакет Earth Spirit MODULAR V2 с уточнениями: оригинальный native-босс 800 HP/6.5 блока, три фазы, полный серверный набор атак, слуги 3×18 HP с cap 6, локальные аренные столбы/укрытия/ворота/преддверие, сон с сохранением HP и руна III на броню. По дополнительному запросу вместо яйца босса добавлен «Запирающий камень Духа земли»: поставленный вручную камень пробуждает тестового босса ударом/разрушением без поиска структуры и без редкой награды. ID блока `earth_heart_seal`; имя игровое, без исторической атрибуции. Настоящий encounter создаётся только завершением нового явного Depths job; старые миры не переоборудуются.

Финальный clean build — PASS ({seconds} с), {o['checks']} offline-проверок; {r['checks']} ресурсных/production проверок — PASS. Все 30 OGG декодируются, ресурсы воспроизводимы; {r['preserved_classes']} прежних классов и {r['preserved_resources']} ресурсов сохранены относительно установленной 1.5.5. Кандидат 1.5.6 установлен в существующую PolyMC 1.21.1/NeoForge 21.1.255, предыдущие JAR сохранены вне mods; {i['other_files_preserved']} остальных файлов неизменны. Старый PolyMC 1.16.5 не тронут. **Запусков Minecraft агентом: 0**, runtime/визуальная/боевая приёмка OPEN. [Отчёт](verification/earth-1.5.6/REPORT.md), [ручная проверка](verification/earth-1.5.6/COMMANDS.md). Никаких commit автоматически не сделано.

''',
'ROADMAP.md':f'''{title}

Подтверждённый пакет реализован в коде и production JAR: Дух земли, слуги, фазы/атаки/сон, штатная активация арены, руна Сердца Земли III, оригинальные модели/текстуры, 30 звуковых прототипов. Дополнительный пользовательский запрос реализован как переносной запирающий камень для плоского тестового мира вместо яйца босса. Генерация А/Б, поиск/геометрия Глубин и будущие milestone не расширялись.

Build/offline/resources PASS; установлен кандидат 1.5.6. Игровая приёмка **OPEN** (0 запусков агентом): проверить настоящую аренную активацию, столбы/укрытия, телеграфы/прыжок/щит, summons/cap, multiplayer, сон и реальный перезаход, одноразовую награду и снятие рун. Прототипам звука и анимациям не присвоен VISUAL/AUDIO/COMBAT PASS. [Результаты](verification/earth-1.5.6/REPORT.md), [краткий QA](verification/earth-1.5.6/COMMANDS.md).

''',
'ARCHITECTURE.md':f'''{title}

`earth` расширяет существующие deferred registries без изменения прежних ID. `EarthArena` — SavedData с адресными encounter ID/state, boss UUID, consumed pillars, gate state, reward flag и retired servants. Единственный локальный hook в завершении нового `DepthsService` job размещает элементы в проверенных пустых клетках прежнего cave layout, без реконструкции старых миров или изменения А/Б/поиска. `EarthSeal.attack` и BreakEvent активируют зарегистрированную арену однократно; поставленный вручную блок создаёт тестового босса без arena ID/награды. Никакой арены вокруг ручного блока не генерируется.

`EarthSpirit` ведёт один серверный cast, cooldown/recovery, локальный target раз в 10 тиков, pressure только действительных melee-участников через прямой UUID lookup, фазы и замороженные таймеры сна. `EarthActor` обслуживает ограниченные swept projectiles, волну со ступнями/LOS, линию шипов и временные FX; не сохраняется, не загружает чанки. `EarthServant` сохраняет ownership, не форсирует загрузку владельца; смерть/реальное удаление снимают UUID, разгрузка сохраняет. Spawn wave атомарна. Награда только настоящей победе; `DEFEATED` записывается до дропа и очистки.

`EarthRules` централизует defaults/raw calibration и wrist release transform; `EarthConfig` COMMON применяет HP/скорость новым сущностям и restart-настройки фаз/таймеров/руны/cooldown. `EarthRune` обновляет transient атрибуты событийно и учитывает только четыре надетых слота. Существующие ограничения наковальни сохранены. `EarthClient`/`EarthCamera` изолированы Dist.CLIENT: native 95-cuboid rig, суставные keyframes, точная привязка props к кисти, synced clocks, отдельные атласы/свечение, контролируемая смерть без vanilla flip, отключаемая локальная камера. `earth_imbalance` — короткий 13-tick эффект, только после успешного tremor damage. Отдельного глобального server tick dispatcher для этой системы нет.

`tools/earth_resources.py` воспроизводит собственные pixel assets и merge-добавления, сохраняя старые ключи/ресурсы; звук копируется побайтно из архива. Offline Gradle task находится вне production source sets. Финальные build/offline/resources PASS; **0 запусков Minecraft**, codec/mesh preview не являются игровой проверкой или измерением MSPT. [Карта реализации](verification/earth-1.5.6/IMPLEMENTATION.md).

'''
}
for name,section in sections.items():
 path=ROOT/'docs'/name;old=path.read_bytes()
 if old.startswith(title.encode('utf-8')):
  boundary=old.find(b'\n## ',len(title.encode('utf-8')));assert boundary>=0,'Existing status boundary missing';old=old[boundary+1:]
 else:assert title not in old.decode('utf-8'),'Status section is not at the expected owned position'
 path.write_bytes(section.encode('utf-8')+old)
path=ROOT/'docs/earth-spirit/DECISIONS.md';s=path.read_text(encoding='utf-8')
if '## Дополнительный запрос пользователя' not in s:
 s+='\n## Дополнительный запрос пользователя\n\nВместо яйца босса — поставленный игроком рунический блок для плоского тестового мира. Название реализации: «Запирающий камень Духа земли» (игровое). Удар/разрушение активируют; без свободного объёма блок сохраняется. Редкая награда по прежнему решению остаётся только настоящей арене.\n';path.write_text(s,encoding='utf-8')
print('PROJECT_STATUS, ROADMAP, ARCHITECTURE and 15-line report updated from exact-JAR proof. Runtime OPEN; 0 Minecraft launches.')
