# Per-child source evidence; no runtime/acceptance claims.
from pathlib import Path
import json,hashlib,shutil
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor88-full'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 v=json.loads((O/'verification.json').read_text());base=json.loads((O/'baseline-sha256.json').read_text());samples=json.loads((O/'after-native.json').read_text());ids=v['ids']
 groups={122:ids[:7],123:ids[7:9],124:ids[9:14],125:ids[14:18],126:ids[18:22],127:ids[22:26],128:ids[26:]}
 reasons={
 'linen_headscarf':('REDESIGN','Открытая лобная лента, волосы/лицо видны, отдельный задний узел.'),
 'linen_shirt':('SMALL_FIX','Вырез, отдельные рукава, задний шов; ткань поверх skin overlay.'),
 'linen_ports':('SMALL_FIX','Независимые штанины и внутренние края, низ выше лаптей.'),
 'bast_shoes':('SMALL_FIX','Низкий плетёный верх, отдельная подошва, внутренние грани разделены.'),
 'sarafan':('REDESIGN','Лямки и раздельные передние/задние панели подола на костях ног.'),
 'linen_apron':('SMALL_FIX','Передняя панель, независимый низ; сзади только пояс/завязки.'),
 'folk_vest':('SMALL_FIX','Открытые передние полы, отдельные плечи и короткая цельная спинка.'),
 'gambeson':('REDESIGN','Мягкий верх, узкая талия, пояс, отдельные рукава, короткая диагональная стёжка.'),
 'plate_cuirass':('REDESIGN','Выпуклая ступенчатая центральная пластина, отдельные спинка/плечи, ремни и заклёпки.'),
 'bogatyr_chestplate':('REDESIGN','Раздельные крупные ламели на кожано-красной основе и солнечная застёжка; сплошные полосы убраны.'),
 'clear_sight_helmet':('KEEP','Меш/UV/PNG идентичны: лицо открыто, камень и боковины самостоятельны; runtime NOT_RUN.'),
 'indomitability_leggings':('SMALL_FIX','Сливовые бедренники и колени сохранены; внутреннее перекрытие двух ног устранено.'),
 'wind_boots':('SMALL_FIX','Боковые ступенчатые перья, отдельная подошва; внутренние грани разведены.'),
 'seven_league_boots':('REDESIGN','Дорожные сапоги с красным отворотом и ремнями вместо целых ванильных штанин; без перьев.'),
 'trophy_idol_chest':('SMALL_FIX','Раздельные костяные рёбра с промежутками и посаженные на руки плечи, бронзовая основа.'),
 'trophy_marinka_cloak':('REDESIGN','Открытый ворот, два самостоятельных нижних хвоста и безопасный наклон от ног; зеркало сохранено.'),
 'trophy_kartaus_mantle':('SMALL_FIX','Светлый открытый ворот, единый красный стяг движется с тяжёлой мантией; отличен от Маринки.')}
 for family in ['perunite','dragon','poloz']:
  for part in ['helmet','chestplate','leggings','boots']:
   why={'helmet':'Рога/гребень и открытое лицо сохранены; локальные кромки/атлас.','chestplate':'Основная геометрия сохранена; центральный знак/палитра уточнены.','leggings':'Внешние габариты сохранены, внутренние грани разделены; стык с обувью.','boots':'Защитная ступня и наружные размеры сохранены, внутренние наложения разведены.'}[part]
   if family=='dragon' and part=='leggings':why+=' Поясная часть вместо лишнего полного торса.'
   if family=='dragon' and part=='boots':why+=' Удалён лишний базовый меш целых штанин.'
   if family=='dragon' and part=='helmet':why+=' Убрано наложение крышки и боков на 0.5 px, сами рога неизменны.'
   if family=='poloz' and part=='chestplate':why+=' Небольшие реальные накладные чешуйки вокруг сердца.'
   if family=='perunite' and part=='chestplate':why+=' Одна центральная молния.'
   reasons[family+'_'+part]=('SMALL_FIX',why)
 def cls(id):
  if id in groups[122]:return 'TextileClothingLayer'
  if id in ['gambeson','plate_cuirass','seven_league_boots']:return 'BasicArmorModels'
  for f in ['perunite','dragon','poloz']:
   if id.startswith(f+'_'):return f.capitalize()+'Armor'
  return 'RewardArmor' if id.startswith('trophy_') else 'UniqueArmorModels'
 inventory=[]
 for id in ids:
  s=next(s for s in samples if s['id']==id);code=f'src/main/java/org/slavicmyths/client/{cls(id)}.java';files=[code,'src/main/resources/assets/slavicmyths/'+s['layers'][1]['texture'],f'src/main/resources/assets/slavicmyths/textures/item/{id}.png',f'src/main/resources/assets/slavicmyths/models/item/{id}.json'];decision,reason=reasons[id]
  inventory.append(dict(id=id,issue=next(k for k,items in groups.items() if id in items),slot=s['slot'],decision=decision,reason=reason,files=[dict(path=n,old_sha256=base.get(n),new_sha256=sha(R/n),changed=base.get(n)!=sha(R/n)) for n in files],before_source='Pinned vanilla HumanoidModel.createMesh(deformation1); source-dependencies.json' if cls(id)=='BasicArmorModels' else 'before/'+code,preview=id+'.png',status='IN_PROGRESS / IN_GAME_OWNER_PENDING'))
 (O/'inventory.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 audit='# #88 — 29-ID inventory\n\nIN_PROGRESS / IN_GAME_OWNER_PENDING. Полные SHA/paths: [inventory.json](inventory.json).\n\n| ID | Issue / slot | Решение и причина | Actual source A/B |\n|---|---|---|---|\n'
 for r in inventory:audit+=f"| `{r['id']}` | #{r['issue']} / {r['slot']} | {r['decision']}: {r['reason']} | [5 views × 2 bodies × 4 poses]({r['preview']}) |\n"
 (O/'AUDIT.md').write_text(audit,encoding='utf-8')
 batches={122:[ids[:4],ids[4:7]],123:[[i] for i in groups[123]],124:[[ids[9]],ids[10:12],ids[12:14]],125:[ids[14:16],ids[16:18]],126:[ids[18:20],ids[20:22]],127:[ids[22:24],ids[24:26]],128:[[i] for i in ids[26:]]}
 matrix="""
| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Точный scope и партии 1–4 ID, индивидуальные решения | inventory.json и список выше | PASS |
| Фактические ModelPart/UV/PNG, old/new SHA | before/, before-native.json, after-native.json, inventory.json | PASS |
| Front/back/right/left/3quarter, Steve/Alex, idle/walk | 40 source A/B пар на ID, тот же масштаб/камера | PASS для source |
| Raised arm/sneak | Те же нативные меши с заданными bone poses | PASS для source; runtime NOT_RUN |
| GUI icon 1×/3× nearest | ICONS-1x-3x.png и исходные PNG | PASS |
| UV/resource/JSON/production JAR/Java21 clean build | verification.json, BUILD.log | PASS |
| Воспроизводимый scoped atlas overlay | tools/armor88_art.py, idempotence/resource diff gate | PASS |
| ID/stats/recipes/loot/имена/NBT/creative не менялись задачей88 | baseline-sha256.json и verification.json; правки44 сохранены | PASS для source |
| 13 аксессуаров и woven_belt mesh не изменены | verification.json | PASS |
| Runtime clipping/z-fighting, смешанные вещи, оружие в руке, first/third person, armor stand, trim/runes/enchants, relog/MP | RUNTIME-MATRIX.md; запусков0 | NOT_RUN |
| Личная приёмка владельцем | Не получена, issue OPEN | NOT_RUN |
"""
 for issue,items in groups.items():
  text=f'# #{issue} / parent #88 — source evidence\n\nIN_PROGRESS / IN_GAME_OWNER_PENDING. Issue OPEN, приёмки нет.\n\nПартии: '+ '; '.join(', '.join(b) for b in batches[issue])+'.\n\n'
  for id in items:text+=f'- `{id}` — **{reasons[id][0]}**. {reasons[id][1]} [A/B]({id}.png).\n'
  text+=matrix
  if issue==122:text+='\n[Ансамбль четырёх вещей](folk_ensemble.png). ArmorStandClothingLayer использует те же семь мешей и RuneGlowBuffers.clothing из44. Реальный event/render NOT_RUN. Skin outer layer не отключается. Между штанинами есть фон в разнесённых позах; в idle сами ванильные ноги соприкасаются.\n'
  if issue in [124,125,126,127]:text+='\n[Копланарные пересечения с координатами](coplanar-intersections.json): анализ геометрии фронтальных поверхностей, не игровой тест. Наружный профиль сохранён, изменены внутренние стыки.\n'
  (O/f'ISSUE-{issue}.md').write_text(text,encoding='utf-8')
 (O/'ISSUE-129.md').write_text("""# #129 — техническая пред-проверка, parent #88

IN_PROGRESS / BLOCKED_ON_OWNER_ACCEPTANCE_A1_A7. Собраны source результаты после пакетов A1–A7. Финальный acceptance-gate не завершён: #122–128 ещё OPEN и не приняты владельцем; #129/#88 тоже OPEN.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Учтены все 8 дочерних задач, нет пропущенных номеров | ISSUE-122.md … ISSUE-128.md и эта матрица | PASS для source coverage |
| 29 ID: change/keep, пути, old/new SHA, A/B | inventory.json, AUDIT.md | PASS |
| 13 аксессуаров/ID/stats/loot/recipes/order сохранены задачей88 | verification.json | PASS для source |
| UV/ресурсы/production JAR/Java21 clean build | verification.json, BUILD.log | PASS |
| Реальные игровые ракурсы/анимации/смешанные вещи/GUI/руны/trim/стойка/relog/MP | RUNTIME-MATRIX.md, запусков0 | NOT_RUN |
| Личная приёмка A1–A7 и финальная приёмка #129/#88 | Ещё не получена | BLOCKED |

#130/щит дружинника отложена; не изменялась. PolyMC46 этой задачей не устанавливалась; параллельная тестовая45 сохранена. Никаких автоматических закрытий, owner-accepted, commit/push.
""",encoding='utf-8')
 runtime='# #88 — игровая матрица владельца\n\nВсе строки NOT_RUN; source preview не заменяет игру; запусков0. Проверить свет/тень, skin outer layer on/off, смешанные комплекты.\n\n| ID | Steve/Alex ракурсы | walk/sneak/arm/смешанные вещи | GUI/first/third person | armor stand | trim/runes/enchants | relog/MP |\n|---|---|---|---|---|---|---|\n'
 for id in ids:runtime+=f'| `{id}` | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN |\n'
 runtime+='\nПредмет в поднятой руке в preview не рисуется; задан только угол руки -1.2 rad. Реальный хват и взаимодействие с оружием требуют проверки владельцем. Просвет между штанинами оценивается в разнесённых позах; в idle ванильные ноги соприкасаются.\n';(O/'RUNTIME-MATRIX.md').write_text(runtime,encoding='utf-8')
 report=f"""# #88 — броня и одежда, source {v['version']}

**IN_PROGRESS / IN_GAME_OWNER_PENDING.** Реализационные пакеты #122–#128 охватывают все **29 носимых ID**. Для #129 собраны технические результаты, финальная приёмка заблокирована до решения владельца по A1–A7. Ни одна Issue не закрывалась и не отмечалась принятой.

[29-ID аудит](AUDIT.md) · [old/new SHA, source paths, slots](inventory.json) · [иконки 1×/3×](ICONS-1x-3x.png).

[Ансамбль народной одежды](folk_ensemble.png) · [база/богатырь/трофеи](REDESIGN-CONTACT.png) · [полировка сетов](SET-POLISH-CONTACT.png).

По каждому ID: 5 ракурсов × Steve/Alex × idle/walk/raised arm/sneak = 40 A/B пар. Фактические нативные ModelPart/UV/PNG, одинаковые камера/масштаб/фазы, offline rasterizer с depth buffer. Это НЕ игровые скриншоты; предмет в руке не рисуется. Actual shaders/игровая камера не симулируются.

Одежда разделена по деталям, подолы и штанины следуют ногам, низ портов отделён от лаптей. На бронестойках подключена та же фабрика семи вещей; другие head items сохраняют vanilla path. Базовые доспехи — две разные конструкции. Богатырь — раздельные ламели вместо полос. Ветер сохраняет боковые перья, скороходы получили дорожный сапог с красным отворотом.

Перунит/Горыныч/Полоз сохраняют внешний характер, рога/шипы и наружные габариты; исправлены внутренние наложения и стыки. У драконьих сапог исключена лишняя полная штанина; поножам оставлен пояс вместо полного нагрудника. [Координаты копланарных пересечений](coplanar-intersections.json) — source evidence, не runtime-тест. Трофеи имеют отдельные рёбра/хвосты/воинский стяг.

## Дочерние матрицы

[122 одежда](ISSUE-122.md) → [123 база](ISSUE-123.md) → [124 уникальные](ISSUE-124.md) → [125 Перунит](ISSUE-125.md) → [126 Горыныч](ISSUE-126.md) → [127 Полоз](ISSUE-127.md) → [128 трофеи](ISSUE-128.md) → [129 пред-проверка, приёмка не завершена](ISSUE-129.md).

Партии ограничены 1–4 ID; реализация брони последовательная. При работе второго агента44 подготовка шла в изолированной рабочей копии. Его RuneGlowBuffers.clothing сохранён при объединении, эффекты/зачарования44 не откатывались. 13 аксессуаров и #130 не изменены. Эта задача не меняет registry IDs, stats, durability, способности, выдачу, loot/recipes, имена, NBT и creative order.

## Проверки и ограничения

Java21 clean build, native bake/UV29 ID и rune104 plain-JVM regression PASS. Production JAR: {v['resources']} ресурсов, {v['png']} PNG, {v['java21_classes']} Java21 classes; CRC/parity/metadata PASS. Scoped art overlay идемпотентен и подключён к legacy entrypoints. [Квитанция](verification.json), [BUILD.log](BUILD.log).

JAR: `build/libs/slavicmyths-{v['version']}.jar`; SHA256 `{v['sha256']}`. Source base `{v['source_base_commit']}` + **UNCOMMITTED** changes; [build inputs](build-source-sha256.json). Автоматического commit/push нет.

Запусков Minecraft/PolyMC/сервера/GameTest **0**. [Игровая матрица](RUNTIME-MATRIX.md): clipping/z-fighting, настоящее движение/оружие, смешанные вещи, event бронестойки, first/third person, trim/runes/enchants, relog/MP — **NOT_RUN**. Bake/компиляция не доказывают реальный renderer event. Ручная приёмка остаётся обязательной; #88 не объявляется DONE/ACCEPTED/релизом.

**POLYMC_NOT_INSTALLED для46.** Параллельный агент по своему поручению установил45 с основной переработкой брони; его квитанция ../polymc-19745/installed.json. Эта поздняя46 дополнительно разводит на 0.06px пояс и верх поножей неукротимости. Задача88 сама JAR в instance не копировала и миры не меняла.

## Воспроизведение

JAVA_HOME только для команды: C:/Program Files/Java/jdk-21.0.12.

`gradlew.bat clean build -I tools/armor88-full-offline.gradle exportArmor88Full -I tools/rune104-offline.gradle checkRune104Offline`

Затем `python tools/armor88_full_preview.py`, `python tools/armor88_overlap_check.py`, `python tools/verify_armor88_full.py`, `python tools/armor88_report.py`.

Baseline43 сохранён в before/ и before-native.json; pinned API/source SHA — source-dependencies.json. Новый export пишет after-native.json: не заменять baseline новой фабрикой.
"""
 if (O/'SHARED-47.md').exists():report=(O/'SHARED-47.md').read_text(encoding='utf-8')+report
 (O/'REPORT.md').write_text(report,encoding='utf-8');shutil.copy2(R/'.tools/armor88-full-build.log',O/'BUILD.log');shutil.copy2(R/'.tools/armor88-full-before.log',O/'BASELINE-BUILD.log');print('ARMOR88_REPORT: 29 IDs, seven source matrices + pending acceptance129')
if __name__=='__main__':main()
