"""Production bytes, baseline preservation and reproducibility; no gameplay claims."""
from pathlib import Path
import hashlib,json,zipfile,re
from PIL import Image
import rare_weapon_resources_1322 as art
import weapon_resources_1321 as previous_art
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';checks=0
def check(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
def snapshot():return {p.relative_to(RES).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in RES.rglob('*') if p.is_file()}
before=snapshot();previous_art.main();art.main();check(before==snapshot(),'Generator does not reproduce canonical resources')
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M).group(1);jar=ROOT/f'build/libs/slavicmyths-{version}.jar'
for p in RES.rglob('*.json'):
 data=json.loads(p.read_text(encoding='utf-8'));checks+=1
 if '/models/' in p.as_posix():
  parent=data.get('parent','');check(not parent.startswith('slavicmyths:') or (RES/('assets/slavicmyths/models/'+parent.split(':')[1]+'.json')).is_file(),'Missing parent '+str(p))
  for texture in data.get('textures',{}).values():
   if texture.startswith('slavicmyths:'):check((RES/('assets/slavicmyths/textures/'+texture.split(':')[1]+'.png')).is_file(),'Missing texture '+texture)
for id in art.NAMES:
 for locale in ('ru_ru','en_us'):check('item.slavicmyths.'+id in json.loads((RES/f'assets/slavicmyths/lang/{locale}.json').read_text(encoding='utf-8')),'Locale missing '+id)
 with Image.open(RES/f'assets/slavicmyths/textures/item/{id}.png') as im:check(im.size==(32,32) and im.getbbox(),'Bad pixel icon '+id)
check(len({art.icon(id).getchannel('A').tobytes() for id in art.HINTS})==4,'Shared weapon silhouettes')
for id in art.HINTS:
 data=json.loads((RES/f'data/slavicmyths/recipe/rare_{id}.json').read_text());check(data['type']=='slavicmyths:armorer_shapeless' and data['copy_components'],'Invalid rare assembly')
with zipfile.ZipFile(jar) as z:
 check('Implementation-Version: '+version in z.read('META-INF/MANIFEST.MF').decode(),'Manifest version')
 check(version.encode() in z.read('META-INF/neoforge.mods.toml'),'NeoForge metadata')
 check(not any('/verify/' in n or '/smoke/' in n or '/slavicmyths_rare/' in n or 'GameTests' in n for n in z.namelist()),'Test code in production JAR')
 for p in RES.rglob('*'):
  if p.is_file() and p.relative_to(RES).as_posix()!='META-INF/neoforge.mods.toml':check(z.read(p.relative_to(RES).as_posix())==p.read_bytes(),'JAR differs '+str(p))
 for cls in ('RareWeapons','RareCombat','RareBleedEffect','DisorientationEffect','LikhoStaffItem'):check('org/slavicmyths/combat/'+cls+'.class' in z.namelist(),'Missing production class')
receipt=json.loads((ROOT/'docs/verification/polymc-1.3.2.1-installation.json').read_text());baseline=Path(receipt['installed']);check(hashlib.sha256(baseline.read_bytes()).hexdigest()==receipt['sha256'],'Baseline changed')
preserved=0;changed=[]
with zipfile.ZipFile(baseline) as z:
 for name in z.namelist():
  if name.endswith('/') or not name.startswith(('assets/','data/')):continue
  p=RES/name;check(p.exists(),'Existing resource removed '+name)
  if p.read_bytes()==z.read(name):preserved+=1;continue
  changed.append(name)
  old=json.loads(z.read(name));new=json.loads(p.read_text(encoding='utf-8'))
  if '/lang/' in name:check(all(new.get(k)==v for k,v in old.items()),'Old locale changed '+name)
  elif '/tags/' in name:check(all(x in new['values'] for x in old['values']) and old.get('replace',False)==new.get('replace',False),'Old tags changed '+name)
  elif name=='data/slavicmyths/loot_table/entities/ataman.json':check(new['pools'][:-1]==old['pools'],'Old ataman drops changed')
  else:raise AssertionError('Unexpected old resource change '+name)
report={'version':version,'checks':checks,'preserved_old_resources':preserved,'additive_changed_resources':changed,'new_item_ids':['slavicmyths:'+i for i in art.NAMES],'jar':str(jar),'bytes':jar.stat().st_size,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'client_launches':0,'performance_measured':False}
(ROOT/'docs/verification/rare-weapons-1.3.2.2-resources.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,ensure_ascii=False))
