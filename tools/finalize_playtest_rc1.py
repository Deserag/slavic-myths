from pathlib import Path
import json,hashlib,zipfile,shutil
from PIL import Image
root=Path('.');out=Path('docs/verification/playtest-0.9.10-rc1');out.mkdir(parents=True,exist_ok=True)
A=Path('src/main/resources/assets/slavicmyths/models/item')
m=json.loads((A/'retainer_shield.json').read_text(encoding='utf-8'));b=json.loads((A/'retainer_shield_blocking.json').read_text(encoding='utf-8'));body=json.loads((A/'retainer_shield_body.json').read_text(encoding='utf-8'))
assert body['elements']==m['elements'] and 'overrides' not in body and 'overrides' not in b
assert b['parent']=='slavicmyths:item/retainer_shield_body'
assert m['overrides']==[{'predicate':{'blocking':1},'model':'slavicmyths:item/retainer_shield_blocking'}]
im=Image.open('src/main/resources/assets/slavicmyths/textures/item/retainer_shield.png')
for element in m['elements']:
 assert all(a<c for a,c in zip(element['from'],element['to']))
 for face in element['faces'].values():
  uv=face['uv'];assert all(0<=v<=16 for v in uv)
  px=min(im.width-1,int((uv[0]+uv[2])/32*im.width));py=min(im.height-1,int((uv[1]+uv[3])/32*im.height));assert im.getpixel((px,py))[3]>0
for hand in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:assert m['display'][hand]!=b['display'][hand]
jar=Path('build/libs/slavicmyths-0.9.10-rc1.jar');sha=hashlib.sha256(jar.read_bytes()).hexdigest()
with zipfile.ZipFile(jar) as z:
 names=z.namelist();assert not any('GameTests' in n or n.startswith('data/slavicmyths_hotfix/') or n.startswith('org/slavicmyths/verify/') for n in names)
 assert '0.9.10-rc1' in z.read('META-INF/neoforge.mods.toml').decode()
 for name in ['org/slavicmyths/combat/CutEffect.class','assets/slavicmyths/models/item/retainer_shield_body.json','data/slavicmyths/damage_type/cut.json','data/minecraft/tags/item/enchantable/durability.json']:assert name in names
release=Path('release/0.9.10-rc1');shutil.copy2(jar,release/jar.name);(release/'SHA256SUMS.txt').write_text(sha+'  '+jar.name+'\n')
report={'version':'0.9.10-rc1','branch':'release/0.9.10-playtest','sha256':sha,'build':'PASS','productionResources':'PASS','staticReferences':2685,'staticErrors':0,'shieldUVAndBlockingModels':'PASS static, existing atlas, no override cycle','testFixturesExcluded':True,'pureJavaChecks':['verifyKurgan','verifyKurganRework','verifyPortData','verifyPortComponents','verifyPortPayloads','verifyPortGeometry'],'hotfixGameTestsBeforeFinalPrompt':'5/5 PASS core and companions','regressionBeforeFinalPrompt':'26/27; masonry clearance fixed afterwards, runtime NOT rerun','hotfixHeadlessServerLaunches':8,'clientLaunches':0,'newMinecraftLaunchesAfterFinalPrompt':0,'manualAcceptance':'PENDING','existingResourceIssues':16,'msptMeasured':False}
(out/'acceptance.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
text='''## 0.9.10 RC1 — public playtest stabilization (2026-10-06)

Ветка `release/0.9.10-playtest`, версия `0.9.10-rc1`. Исправлены UV/round model/blocking щита, зарегистрирован экран существующего RPG-меню Камня пути/рунной наковальни, добавлен shift-click наковальни; книга получила непрозрачную страницу и scroll, кухня — выбор рецепта/условия/результат. Курганный hotfix включён: сохранённые encounter slots, фиксированные квоты/боссы, наружные patches и единый 100-tick Порез. Геометрия кургана и существующие ID сохранены. Разбойники/Соловей не перерабатывались; полный Equipment & Art Overhaul и новая система рун не заявлены реализованными.

`clean build` и шесть CPU/data/codec проверок PASS; production JAR/resources PASS, 2685 статических ссылок, 0 новых ошибок. 16 прежних door-model parent issues отмечены отдельно. До последнего запроса hotfix: 5/5 GameTests; общая регрессия 26/27 обнаружила расчистку кладки, исправленную затем без повторного запуска игры. Клиент 0; после последнего запроса Minecraft не запускался. Игровая/визуальная приёмка — MANUAL PENDING. Отчёт: `docs/verification/playtest-0.9.10-rc1/acceptance.json`; playtest docs/JAR/checksum: `release/0.9.10-rc1/`. Изменения прежнего рабочего дерева включены, runtime worlds/logs/configs не включаются в release commit.

'''
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=Path('docs')/name;old=p.read_text(encoding='utf-8');p.write_text(old if old.startswith(text) else text+old,encoding='utf-8')
p=Path('docs/kurgan/KURGAN_ENCOUNTER_SURFACE_CUT_HOTFIX.md');s=p.read_text(encoding='utf-8');s+='\n## Итог проверок\n\nHotfix GameTests: 5/5 core и companions до последнего промпта. Всего 8 реальных headless server запусков hotfix/regression, клиент 0. Общая регрессия 26/27: обнаружена расчистка каменной кладки; камень удалён из approachSoil, нанесение палитры исключено из трёхблочной дорожки. Повторный runtime после исправления не выполнялся по последнему запрету auto-launch. CPU/plan/NBT/codec checks и clean build RC1 PASS, статические ссылки 2685/0 errors; production JAR проверен и включён в RC1. Логи: work/kurgan-hotfix-final-core.log, work/kurgan-hotfix-companions.log, work/kurgan-hotfix-regressions.log, work/playtest-rc1-clean-build.log. Никакой полной визуальной приёмки не заявлено.\n';p.write_text(s.split('\n## Итог проверок')[0]+s[s.rfind('\n## Итог проверок'):],encoding='utf-8')
p=release/'KNOWN_ISSUES.md';s=p.read_text(encoding='utf-8');s+='\n- Resource validator отмечает 16 существующих door model parents (birch-family mod woods): прежние имена vanilla door parents требуют отдельной миграции. Это не скрыто заявлением о resource PASS; перечень есть в docs/verification/mob-worldgen-0.9.9/resource-check.json.\n';marker='\n- Resource validator';p.write_text(s.split(marker)[0]+s[s.rfind(marker):],encoding='utf-8')
print('RC1 artifact/model/static verification PASS SHA256='+sha)
