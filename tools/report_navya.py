"""Record actual source/build evidence without declaring owner acceptance."""
from pathlib import Path
import json,subprocess,hashlib
R=Path(__file__).resolve().parents[1];D=R/'docs/implementation/navya-grove'
def main():
 build=json.loads((R/'build/current-resources.json').read_text());gates=json.loads((D/'gates.json').read_text())
 project=json.loads(subprocess.check_output(['gh','project','item-list','9','--owner','Deserag','--format','json','--limit','200'],cwd=R).decode('utf-8'))
 cards=[{'issue':i['content']['number'],'status':i.get('status'),'id':i['id']} for i in project['items'] if i.get('content',{}).get('number') in range(251,258)]
 assert len(cards)==7 and all(i['status']=='В работе' for i in cards),cards
 (D/'project-status.json').write_text(json.dumps({'checked':'2026-10-25','items':cards,'issues':'OPEN; no close/ready/owner-accepted requested'},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 audio=json.loads((D/'audio-routing.json').read_text())
 calls={}
 for event in audio['events']:
  if '.' in event:
   cue=event.split('.')[1];method={'idle':'getAmbientSound','hurt':'getHurtSound','death':'getDeathSound','step':'playStepSound','warn':'begin','strike':'impact'}[cue]
   calls[event]='src/main/java/org/slavicmyths/navya/NavyaMob.java#'+method
  else:
   owner='NavyaRoots.java#tick' if event.startswith('roots_') else 'NavyaShot.java#onHit' if event in ['shot_hit','trunk_hit'] else 'NavyaMob.java#impact' if event=='trunk_throw' else 'NavyaMob.java#begin'
   calls[event]='src/main/java/org/slavicmyths/navya/'+owner
 audio['callsites']=calls
 (D/'audio-routing.json').write_text(json.dumps(audio,indent=2)+'\n',encoding='utf-8')
 rows=[
 ('#251 единственный биом, порядок пакета','Блоки → флора → существа → Сторож → worldgen; другие биомы не добавлены','PASS (source scope)'),
 ('#251 полная приёмка цепочки','Runtime/художественная приёмка не выполнены','NOT_RUN'),
 ('#252 единый контракт/ID/палитры','CONTRACT.md; технические SVG/specs; реальные PNG и source-render','PASS (source)'),
 ('#252 сравнение с оригинальными PNG прежнего чата','Текущие/архивные Issues и read_thread не дают доступных PNG; в #257 прямо указано, что картинки не загружены в GitHub. Владелец направил использовать задачи; точное совпадение не заявлено','NOT_RUN'),
 ('#253 восемь геологических блоков','NavyaBlocks; реальные blockstates/models/PNG/loot/RU/EN; actual-block-models.png','PASS (source/JAR)'),
 ('#253 размещение/добыча/освещение','Игра не запускалась','NOT_RUN'),
 ('#254 одна порода/строительные варианты/растения/саженец','31 inventory blocks всего; native LeavesBlock; old bark property; TreeGrower; генераторы актуальны','PASS (source/JAR)'),
 ('#254 три формы, связность и лимиты','tree-topology.txt: 1500 планов,15–93 logs, maxSize100; пользовательский config только прочитан','PASS (offline geometry)'),
 ('#254 настоящая рубка FallingTree/рост/соседние постройки','Не проверялись в Minecraft','NOT_RUN'),
 ('#255 четыре существа, HP80/120/150/20, AI/яйца/мирный огонёк','NavyaContent/Mob/Safety/Shot/Roots; серверные damage windows и cooldown','PASS (source)'),
 ('#255 отдельные модели/UV/текстуры/анимации','NavyaMesh/Pose/Model/Renderer; native-rigs.json; actual-entity-models.png; GIF всех поз; заряд кисти Навки','PASS (native source export)'),
 ('#255 утверждённые души/собственные компоненты','Личный ответ владельца:1–3 fragments; soul2%+1%/Looting; exclusive0.5% blight; component1–2. Три alternatives без совместной выдачи ветвей','PASS (source contract)'),
 ('#255 combat/slow/collision/save/reload/Looting','Не запускались; статическая проверка не выдана за выпадения','NOT_RUN'),
 ('#256 Сторож700HP/две фазы/атаки/арена/помощники','NavyaMob: transition350HP,20% protection, sweep/roots/trunk/summon; max2 marked helpers, no loot/XP; home; cancel interrupted damage on reload','PASS (source)'),
 ('#256 лицо в стволе/масштаб/видимое бревно/анимации','Actual ModelParts/UV:6.5-block rig, body+buttresses около3block; held log4.5block, lift/release; native pose frames','PASS (source export)'),
 ('#256 гарантированный лут','core1, branch2–5 c Looting capped5, soul2–3, soul_blight1; four unconditional pools','PASS (source contract)'),
 ('#256 бой/фазы/анимации/разгрузка/антифарм в игре','Не проверены; unloaded helper UUID reservations conservative, runtime needed','NOT_RUN'),
 ('#255/#256 sound routes/subtitles/quarantine','audio-routing.json:35 actual vanilla event routes, cached OGG SHA1, RU/EN; old sounds preserved; no generated voice or new copied OGG','PASS (source/resource)'),
 ('#255/#256 прослушивание/громкость/тайминг/сервер','Нет реального прослушивания/клиента/сервера','NOT_RUN'),
 ('#257 biome/climate/vanilla five types','Mixin только vanilla Nether preset; точка(-.3,.4),offset.175; climate-proposal.json5seed native analysis, vanilla types remain represented','PASS (offline climate only)'),
 ('#257 три яруса/переходы/лес/кости','Geology before ores; Nature after ores; profile-check.txt5seed×262144 columns; varied shelves, tier junctions, tall bone spikes up/down; native columns only','PASS (source/profile)'),
 ('#257 редкое Сердце/Сторож/поиск/четыре спавна','Structure+piece saved bossPlaced/UUID/home, upper or middle clearing; spacing32/separation16/frequency.6 technical prototype; /locate structure slavicmyths:navya_heart; vertical predicates','PASS (source; tuning unvalidated)'),
 ('#257 vanilla ores/structures/lava/old chunks','Pinned HELL overrides unchanged; vanilla biome features/structure tags preserved; lava sea skipped; protected structure chunks skipped; no retro-gen/tick/chunk loading','PASS (source preservation only)'),
 ('#257 actual5seed/coordinates/screenshots/traversal/spawns/MSPT/memory/save/seams/portals','Ни одного игрового мира не создано; registry/mixin application and data codecs in actual NeoForge NOT_RUN','NOT_RUN'),
 ('Java21 clean build/production resources','logs/final-clean-build.txt; logs/final-resources.txt; gates.json; build/current-resources.json','PASS'),
 ('PolyMC installation / manual owner acceptance','Не выполнялись; старый инстанс не изменён, новый не создавался','NOT_RUN')]
 text='''# Навья роща #251–#257 — IN_PROGRESS, 25.10.2026

Ветка `hell`, рабочая копия `D:/slavic-myths-hell`, базовый source commit `275ecaa5345f9b374f83f0829c58df16c0e54546`. Нового коммита нет: изменения не закоммичены по правилам владельца. Исходная рабочая копия `D:/slavic-myths` не изменялась. Единственный новый биом — Навья роща; оружие, руны/крафты будущих этапов и другие биомы не добавлялись.

Реализованы исходники полного контентного пакета и последняя интеграция генерации. **Это не итоговая готовность и не сдача цепочки в «Тестирование».** Все семь карточек фактически «В работе», Issues OPEN, приёмка владельца отсутствует. Minecraft/PolyMC/GameTest/сервер: **0 запусков**. `POLYMC_NOT_INSTALLED`.

## Утверждения владельца и доступные референсы

Параметры soul loot приняты явным ответом «Да, эти параметры». Оригинальные PNG повторно искались в рабочих и архивных Issues/проекте; доступных файлов нет, комментариев/ZIP нет. #257 прямо документирует отсутствие исходных изображений в GitHub. По направлению владельца «смотри в задачах» использованы production markdown и SVG из docs/concepts/navya-grove. Нельзя выдавать это за визуальное сравнение с отсутствующими PNG.

## Что реализовано

- Восемь геологических материалов; одна древесная порода со строительными вариантами, знаками, старой корой без второго ID; флора и возобновляемый саженец. Настоящие модели/16×16 PNG/loot/RU/EN/Creative Tab.
- Три связные формы деревьев (в том числе2×2 низ большого). TreeFeature делает preflight, не заменяет здания и не загружает чанки. FallingTree config не правилась.
- Навка, Корневик, Костяной ползун, мирный огонёк и Сторож: отдельные собственные voxel rigs, UV, PNG/glow masks, суставные позы и renderers. Серверные windup/damage/recovery, projectiles и временные корни. Новые модели отсутствуют в базовом commit: «до» — отсутствие этих ID/ModelParts, а не другой старый арт. «После» экспортирован из actual ModelPart vertices/UV в одинаковых позах/масштабе внутри каждого персонажа. PNG/GIF — source render, не игровой скриншот.
- Утверждённые взаимоисключающие душевные ветви и гарантированные награды Сторожа. Нет нового оружия или работающей руны. Призванные помощники без наград.
-35 vanilla sound aliases с mapping до actual cached OGG и SHA1; регистрация/subtitles/callsites. Ни синтетические голоса, ни карантинные события не возвращены. Прослушивание NOT_RUN.
- Биом в существующем Nether через узкий mixin vanilla preset; локальная геология до руд, смена камня/флора после руд. Лавовые акватории не застраиваются. Наклонные понижения соединяют уровни в профиле; actual traversal требует мира. Верхний лес, средние корневые уступы, нижние кости/сталактиты/сталагмиты; редкое Сердце с площадкой и привязанным Сторожем. Vanilla structure eligibility сохранена, пересекающиеся chunks защищены от локальной перестройки.

Параметры spawn weights, плотности, rare Heart frequency и climate пока технические defaults, **не прошедший игровой баланс**. Генерация меняет только новые чанки; старые не мигрируются. Возможны швы с прежним Nether и при смене генератора; реальная совместимость старых миров/порталов/структур не проверена.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
'''
 text+='\n'.join('| '+ ' | '.join(row)+' |' for row in rows)
 text+=f"\n\n## Проверенный локальный JAR\n\nВерсия `{build['version']}`; путь `{build['jar']}`; SHA256 `{build['sha256']}`. Ресурсов{build['resources']}, JSON{build['json']}, PNG{build['png']}, models{build['models']}, Java21classes{build['java21_classes']}. Это собранный локальный кандидат, **не installed JAR**. Source commit — базовый выше + незакоммиченный delta/source-manifest.json. Backup/installed path не заявляются, поскольку установки не было.\n\n"
 text+='''## Краткая ручная проверка владельца

После отдельного решения о запуске:5 seed/несколько областей, три яруса и проходы, деревья/FallingTree, vanilla structures/ores/lava/portals и старые чанки, natural spawn и повторный визит Heart; все атаки/телеграфы/души с Looting и anti-farm, phase2/save/unload/MP; модели со всех сторон и звук. Реальные координаты/видео/логи/MSPT приложить отдельно. Сейчас запуск не разрешался и не выполнялся.
'''
 (D/'REPORT.md').write_text(text,encoding='utf-8')
 intro='''## 25.10.2026 — Навья роща #251–#257: IN_PROGRESS, исходники и JAR

В отдельной ветке `hell` реализованы31 inventory block, одна порода/флора/три формы деревьев, четыре существа и Сторож с собственными ModelPart/UV/PNG/анимациями; утверждённый владельцем лут,35 vanilla sound aliases. Последним подключён новый biome/геология/три яруса/редкое Сердце/спавн. Java21 clean build и source/JAR gates PASS; native climate/profile5seed и source renders не являются игровыми тестами. Запусков0; runtime/прохождение/баланс/точное сравнение с недоступными оригинальными PNG/прослушивание/приёмка NOT_RUN. Все семь карточек фактически «В работе», Issues OPEN. PolyMC не изменён, POLYMC_NOT_INSTALLED; commit/push нет. [Матрица и доказательства](implementation/navya-grove/REPORT.md).

'''
 for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
  path=R/'docs'/name;old=path.read_text(encoding='utf-8');marker='## 24.10.2026 — HELL'
  index=old.find(marker)
  if old.startswith('## 24.10.2026 — Навья роща') and index>=0:old=old[index:]
  if old.startswith('## 25.10.2026 — Навья роща'):
   index=old.find(marker);old=old[index:]
  extra=''
  if name=='ARCHITECTURE.md':extra='Common `navya` owns registries/AI/local features/structure serialization; `navya.client` only through Dist.CLIENT subscriber owns native rigs/UV/poses/renderers. `NavyaTiming` shares server windup with client pose; action/gameTime/phase synced only on state changes. NetherParametersMixin extends only vanilla Nether climate preset. No serverwide polling/retro-generation. Scoped generators: navya_assets/audio/loot/worldgen/entity_assets; verify_navya checks legacy preservation, native UV coverage and packaged bytes.\n\n'
  path.write_text(intro+extra+old,encoding='utf-8')
 contract=D/'CONTRACT.md';c=contract.read_text(encoding='utf-8')
 c=c.replace('Оригинальный бестиарий/тёмный Сторож пока BLOCKED_REFERENCE; у владельца запрошен путь.','После ответа владельца «смотри в задачах» работа выполнена по доступным specs/SVG; точное сравнение с отсутствующими оригинальными PNG остаётся NOT_RUN.')
 c=c.replace('Точные проценты душевного лута и новый `soul_blight` ожидают ответа владельца; пока не подменять согласование временем ожидания.','Добавлен `soul_blight`; владелец явно утвердил1–3 fragments, soul2%+1%/Looting, exclusive0.5% blight, own material1–2, boss core1/branches2–5/soul2–3/blight1.')
 c=c.replace('До готовности обязательных моделей/визуальных референсов и решения лута финальная генерация не включается.','После изготовления моделей/UV/текстур и утверждения лута подключена финальная source-интеграция #257; runtime и визуальная приёмка остаются NOT_RUN.')
 contract.write_text(c,encoding='utf-8')
 delta={}
 paths=subprocess.check_output(['git','ls-files','-m','-o','--exclude-standard'],cwd=R,text=True).splitlines()
 for relative in paths:
  path=R/relative
  if path.is_file() and (relative.startswith('src/') or relative=='gradle.properties' or relative.startswith('tools/navya') or relative.startswith('tools/verify_navya')):delta[relative]=hashlib.sha256(path.read_bytes()).hexdigest()
 (D/'source-manifest.json').write_text(json.dumps({'baseCommit':'275ecaa5345f9b374f83f0829c58df16c0e54546','branch':'hell','committed':False,'productionAndTools':delta,'jar':build},indent=2)+'\n',encoding='utf-8')
 print('Recorded current matrix, source hashes, actual Project statuses and local JAR receipt; owner acceptance pending.')
if __name__=='__main__':main()
