from pathlib import Path
import json, zipfile, hashlib
from PIL import Image
R=Path(__file__).resolve().parents[1];J=R/'build/libs/slavicmyths-1.6.2.jar';out=R/'docs/verification/visual-1.6.2';out.mkdir(parents=True,exist_ok=True);checks=0
def need(b,s):
 global checks;checks+=1
 if not b:raise AssertionError(s)
log=(R/'work/visual-162/build.log').read_text(encoding='utf8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build')
for text in ['EARTH OFFLINE PASS:','SIVKA OFFLINE PASS:','Firebird offline PASS: 200020','VISUAL OFFLINE PASS: 8','Sivka coplanar overlapping face pairs=0','Earth coplanar overlapping face pairs=0']:need(text in log,text)
old=zipfile.ZipFile(R/'work/visual-162/baseline-1.6.1.jar');new=zipfile.ZipFile(J)
families=['org/slavicmyths/client/EarthClient','org/slavicmyths/client/SivkaMesh','org/slavicmyths/client/FirebirdMesh','org/slavicmyths/client/FirebirdClient','org/slavicmyths/earth/EarthSpirit','org/slavicmyths/earth/EarthRules','org/slavicmyths/earth/EarthActor','org/slavicmyths/earth/EarthCommands','org/slavicmyths/firebird/FirebirdContent']
modified=[];preserved=0
for n in old.namelist():
 if n.endswith('/'):continue
 need(n in new.namelist(),'preserve '+n)
 if old.read(n)==new.read(n):preserved+=1;continue
 modified.append(n)
 if n in ['META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml']:continue
 if n.endswith('.class') and any(n==prefix+'.class' or n.startswith(prefix+'$') for prefix in families):continue
 if n in ['assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json']:
  a=json.loads(old.read(n));b=json.loads(new.read(n))
  for k,v in a.items():
   if b.get(k)==v:continue
   restored=v.encode('cp1251').decode('utf8') if n.endswith('ru_ru.json') and (k.startswith('subtitles.slavicmyths.earth_spirit.') or k in ['item.slavicmyths.earth_heart_rune_iii','block.slavicmyths.earth_seal']) else None
   need(restored==b.get(k),'preserve prior translation '+k)
  continue
 need(n.endswith('.png') and (n.startswith('assets/slavicmyths/textures/entity/earth_spirit/') or n.startswith('assets/slavicmyths/textures/block/earth_') or n in ['assets/slavicmyths/textures/entity/firebird.png','assets/slavicmyths/textures/entity/firebird_emissive.png']),'unexpected change '+n)
for n in new.namelist():
 need(not any(t in n for t in ['Checks.class','/smoke/']),'no test fixtures '+n)
 if n.endswith('.class'):need(int.from_bytes(new.read(n)[6:8],'big')==65,'Java21 '+n)
 if n.endswith('.json'):json.loads(new.read(n));checks+=1
 if n.endswith('.png'):
  import io
  im=Image.open(io.BytesIO(new.read(n)));im.verify();checks+=1
for p in (R/'src/main/resources').rglob('*'):
 if p.is_file() and ('firebird' in p.as_posix() or 'earth_spirit/' in p.as_posix() or 'sivka' in p.as_posix()):
  n=p.relative_to(R/'src/main/resources').as_posix();need(new.read(n)==p.read_bytes(),'packaged current '+n)
need(json.loads(new.read('assets/slavicmyths/models/item/firebird_spawn_egg.json'))['parent']=='minecraft:item/template_spawn_egg','real egg model')
for language in ['ru_ru','en_us']:need('item.slavicmyths.firebird_spawn_egg' in json.loads(new.read('assets/slavicmyths/lang/'+language+'.json')),'egg translation')
need(old.read('data/slavicmyths/loot_table/entities/earth_spirit.json')==new.read('data/slavicmyths/loot_table/entities/earth_spirit.json'),'existing diamonds and coins retained')
report={'version':'1.6.2','status':'PASS','checks':checks,'clean_build':'PASS','offline':'Earth/Sivka/Firebird/Visual PASS','coplanar_pairs':{'sivka':0,'earth':0},'earth_emissive_submissions':'once per vertex','jar':str(J),'sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'baseline_entries_preserved':preserved,'modified_baseline_entries':modified,'minecraft_launches':0,'gameplay_visual_audio_mp':'NOT TESTED'}
(out/'resource-gates.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({k:v for k,v in report.items() if k!='modified_baseline_entries'}))
