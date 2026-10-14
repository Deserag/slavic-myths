"""Production gate against the exact installed Poloz-fixed 1.9.1; no game launch."""
from pathlib import Path
from PIL import Image
import json,hashlib,zipfile,struct,re,subprocess
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/karachun-1.9.2';A=R/'src/main/resources/assets/slavicmyths';J=R/'build/libs/slavicmyths-1.9.2.jar';checks=0
def need(ok,msg):
 global checks;checks+=1
 if not ok:raise AssertionError(msg)
def main():
 log=(R/'.tools/karachun-clean-build.log').read_text(encoding='utf-8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build')
 for marker in ['NIGHT_OFFLINE_PASS','FROST_OFFLINE_PASS','QUEST185_OFFLINE_PASS','POLOZ_OFFLINE_PASS','120 native']:need(marker in log,marker)
 before={p.relative_to(R/'src/main/resources').as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'src/main/resources').rglob('*') if p.is_file()}
 subprocess.run(['python',str(R/'tools/karachun_assets.py')],check=True)
 after={p.relative_to(R/'src/main/resources').as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'src/main/resources').rglob('*') if p.is_file()}
 need(before==after,'resource generator rollback/nondeterminism');write=O/'reproducibility.json';write.write_text(json.dumps({'status':'PASS','resources':len(before),'changed':0},indent=2)+'\n')
 oldpath=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.9.1.jar')
 oldsha='94e471d9c3c96a8d63168a499f6256015e870de8ecd227570d6d9def3ba951f1'
 if not oldpath.exists():oldpath=next(p for p in oldpath.parents[2].joinpath('.slavicmyths-backups').rglob('slavicmyths-1.9.1.jar') if hashlib.sha256(p.read_bytes()).hexdigest()==oldsha)
 need(hashlib.sha256(oldpath.read_bytes()).hexdigest()==oldsha,'wrong baseline')
 items=['karachun_scythe','rune_of_waning','black_wing_amulet','shard_of_last_night','karachun_trial_totem'];langs={n:json.loads((A/f'lang/{n}.json').read_text(encoding='utf-8')) for n in ['ru_ru','en_us']}
 with zipfile.ZipFile(J) as jar,zipfile.ZipFile(oldpath) as old:
  names=set(jar.namelist());need(b'1.9.2' in jar.read('META-INF/neoforge.mods.toml'),'version');need(not any('/smoke/' in n or 'NightChecks' in n or 'FrostChecks' in n or n.startswith('tools/') for n in names),'test code packaged')
  for p in (R/'build/classes/java/main').rglob('*.class'):
   name=p.relative_to(R/'build/classes/java/main').as_posix();need(jar.read(name)==p.read_bytes(),'stale class '+name);need(struct.unpack('>H',p.read_bytes()[6:8])[0]==65,'wrong Java '+name)
  for name in before:
   need(name in names,'resource missing '+name);expected=(R/'src/main/resources'/name).read_bytes()
   if name=='META-INF/neoforge.mods.toml':expected=expected.replace(b'${mod_version}',b'1.9.2').replace(b'${neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
   need(jar.read(name)==expected,'stale resource '+name)
   if name.endswith('.json'):json.loads(jar.read(name))
  allowed={'META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','data/curios/tags/item/necklace.json','data/slavicmyths/tags/item/rune_heavy.json','data/slavicmyths/tags/item/rune_capacity_3.json'}|{f'data/minecraft/tags/item/enchantable/{tag}.json' for tag in ['weapon','sharp_weapon','sword','durability']};changed=[]
  for name in old.namelist():
   if name.endswith('/') or name.endswith('.class') or name.startswith('META-INF/'):continue
   need(name in names,'old resource removed '+name)
   if old.read(name)!=jar.read(name):changed.append(name);need(name in allowed,'1.9.1 resource regression '+name)
  for locale,lang in langs.items():
   previous=json.loads(old.read(f'assets/slavicmyths/lang/{locale}.json'));need(all(lang.get(k)==v for k,v in previous.items()),'prior translations changed '+locale)
   for item in items:need('item.slavicmyths.'+item in lang,'item localization '+item);need(Image.open(A/f'textures/item/{item}.png').size==(32,32),'icon '+item);need(f'assets/slavicmyths/models/item/{item}.json' in names,'model '+item)
   for stage in ['known','rumor','seen','discovered','studied','verified']:need('tales.entry.karachun_trial.'+stage in lang,'tale '+stage)
   source='\n'.join(p.read_text(encoding='utf-8') for p in (R/'src/main/java/org/slavicmyths/karachun').glob('*.java'))
   for key in re.findall(r'NightContent.text\("([a-z_.]+)"',source):need('night192.'+key in lang,'raw text '+key)
  source='\n'.join(p.read_text(encoding='utf-8') for p in (R/'src/main/java/org/slavicmyths/karachun').glob('*.java'));need('net.minecraft.client' not in source,'common loads client');need('setDayTime' not in source and 'setBlock(' not in source,'world mutation')
  sounds=json.loads((A/'sounds.json').read_text());events={k:v for k,v in sounds.items() if k.startswith('karachun.')};need(len(events)==24,'sound registrations')
  for key,v in events.items():
   for lang in langs.values():need(v['subtitle'] in lang,'sound subtitle '+key)
   for e in v['sounds']:need(jar.read('assets/slavicmyths/sounds/'+e['name'].split(':')[1]+'.ogg').startswith(b'OggS'),'ogg '+key)
  for name,minimum in [('karachun',250),('karachun_raven',50)]:
   rig=json.loads((O/f'{name}-rig.json').read_text());need(len(rig['boxes'])>=minimum,'own geometry '+name);atlas=Image.open(A/f'textures/entity/{name}.png');need(atlas.size==(rig['atlas'],rig['atlas']),'atlas');need(all(b['uv'][0]+b['uv'][2]<=rig['atlas'] and b['uv'][1]+b['uv'][3]<=rig['atlas'] for b in rig['boxes']),'UV rectangle')
  # Player scythe is deliberately independent of the boss's larger mesh.
  from verify_scythe_stage_a import validate_geometry
  validate_geometry();need(True,'closed tapered player scythe / UV / contexts')
  poloz=(R/'src/main/java/org/slavicmyths/client/PolozRenderer.java').read_text();need(poloz.index('var buffer=b.getBuffer')>poloz.index('RenderType.eyes'),'Poloz buffer crash regression')
  nsub=subscribers(J);need(nsub>0,'event subscriber audit')
 result={'status':'PASS','checks':checks,'version':'1.9.2','sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'baseline':oldsha,'event_subscribers':nsub,'changed_existing_resources':changed,'original_sound_events':24,'client_starts':0,'server_starts':0,'gameplay':'NOT_RUN','MSPT':'NOT_MEASURED'};O.mkdir(parents=True,exist_ok=True);(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
