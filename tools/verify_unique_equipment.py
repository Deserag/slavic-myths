"""Production resource/JAR/preservation audit; no claims of client rendering or relog."""
from pathlib import Path
import hashlib,json,zipfile,re,argparse
from PIL import Image
from unique_armor_resources import IDS,RECIPES
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';checks=0
def check(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
parser=argparse.ArgumentParser();parser.add_argument('--expected-version',default='1.3.4.5.3');parser.add_argument('--output',default='docs/verification/unique-equipment.json');args=parser.parse_args()
version=dict(line.split('=',1)for line in (ROOT/'gradle.properties').read_text().splitlines()if '='in line)['mod_version']
check(version==args.expected_version,'Version differs from requested gate')
jar=ROOT/f'build/libs/slavicmyths-{version}.jar';check(jar.exists(),'production jar')
baseline=json.loads((ROOT/'work/unique-armor/baseline.json').read_text())
changed=[];preserved=0
for name,hash in baseline.items():
 p=ROOT/name;check(p.exists(),'deleted '+name)
 if digest(p)!=hash:changed.append(name)
 else:preserved+=1
allowed_sources={'src/main/java/org/slavicmyths/'+n for n in ['SlavicMyths.java','armorer/ArmorerRecipe.java','client/ClientSetup.java','client/RareRuneClient.java','registry/ModItemGroup.java','rpg/Runes.java']}
allowed_resources={'src/main/resources/'+n for n in ['META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','data/slavicmyths/loot_table/entities/kurgan_voevoda.json','data/slavicmyths/loot_table/entities/buried_volkhv.json','data/slavicmyths/loot_table/entities/unresting_prince.json']}
allowed_tools={'tools/'+n for n in ['integration_polish_resources.py','rare_weapon_resources_1322.py','rare_rune_resources_1343.py']}
# Other work continued in this shared checkout while this patch was being authored.
# Record those files separately; this task neither authored nor reverts them.
concurrent_changes=sorted(set(changed)-(allowed_sources|allowed_resources|allowed_tools))
check(all('/bandit/'in n or n in {'tools/verify_boss_reimplementation.py','tools/tugarin-tests/org/slavicmyths/hunt/BossReimplementationGameTests.java','src/main/java/org/slavicmyths/client/NightingaleModel.java','src/main/java/org/slavicmyths/client/WorldBossModel.java','src/main/java/org/slavicmyths/hunt/TugarinCombat.java','src/main/java/org/slavicmyths/registry/ModEntities.java'} for n in concurrent_changes),'unexpected concurrent change '+str(concurrent_changes))
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));check(True,'json')
for p in RES.rglob('*.png'):
 with Image.open(p)as im:im.verify()
 check(True,'png')
source=(ROOT/'src/main/java/org/slavicmyths/equipment/UniqueEquipment.java').read_text()
for name in IDS:
 check('"'+name+'"'in source,'registration '+name)
 for locale in ['ru_ru','en_us']:
  check(bool(json.loads((RES/f'assets/slavicmyths/lang/{locale}.json').read_text(encoding='utf-8')).get('item.slavicmyths.'+name)),'localization '+name)
 check((RES/f'assets/slavicmyths/models/item/{name}.json').exists(),'model '+name)
 if name!='hardened_iron':
  recipe=json.loads((RES/f'data/slavicmyths/recipe/unique/{name}.json').read_text());check(recipe['copy_components']and recipe['type']=='slavicmyths:armorer_shapeless','upgrade type');check([i['item']for i in recipe['ingredients']]==RECIPES[name],'ingredients');check(len(recipe['ingredients'])<=16,'grid capacity')
for kind in ['wind','bogatyr','clear_sight','indomitability']:
 for layer in [1,2]:
  with Image.open(RES/f'assets/slavicmyths/textures/armor/unique_{kind}_layer_{layer}.png')as im:check(im.size==(64,32),'armor dimensions')
for entity,count in [('kurgan_voevoda',1),('buried_volkhv',1),('unresting_prince',2)]:
 table=json.loads((RES/f'data/slavicmyths/loot_table/entities/{entity}.json').read_text());pool=[p for p in table['pools']if p.get('name')=='slavicmyths:unique_hardened_iron'];check(len(pool)==1 and not pool[0].get('conditions')and pool[0]['entries'][0]['functions'][0]['count']==count,'guaranteed iron')
for directory in ['worldgen','dimension','dimension_type']:
 for p in (RES/'data/slavicmyths'/directory).rglob('*.json'):check('hardened_iron'not in p.read_text(),'iron worldgen')
with zipfile.ZipFile(jar)as z:
 names=z.namelist();check(not any('/verify/'in n or n.startswith('data/slavicmyths_unique/')or 'GameTests'in n for n in names),'test contamination')
 for p in RES.rglob('*'):
  if p.is_file()and p.suffix in ['.json','.png','.ogg','.nbt']:
   name=p.relative_to(RES).as_posix();check(name in names,'missing jar resource '+name);check(z.read(name)==p.read_bytes(),'jar source differs '+name)
 metadata=z.read('META-INF/neoforge.mods.toml').decode();check(f'version="{version}"'in metadata,'jar version');check('slavicmyths.equipment.mixins.json'in metadata,'mixins metadata')
 for n in ['UniqueEquipment','UniqueEquipmentEvents','UniqueEquipmentRules','ClearSightNetwork']:
  code=z.read('org/slavicmyths/equipment/'+n+'.class');check(int.from_bytes(code[6:8],'big')==65,'Java21 '+n)
report={'version_before':version,'version_after':version,'checks':checks,'preserved_files':preserved,'changed_existing_files':sorted(set(changed)-set(concurrent_changes)),'concurrent_changes_not_authored_by_this_task':concurrent_changes,'new_production_files':[p.relative_to(ROOT).as_posix()for p in (ROOT/'src').rglob('*')if p.is_file()and p.relative_to(ROOT).as_posix()not in baseline],'jar':str(jar),'sha256':digest(jar),'recipes':RECIPES,'client_launches':0,'client_status':'REQUIRES_MANUAL_CLIENT_CHECK'}
out=ROOT/args.output;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({k:report[k]for k in ['version_after','checks','preserved_files','sha256','client_status']},ensure_ascii=False))
