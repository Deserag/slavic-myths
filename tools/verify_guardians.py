"""Production and native resource gates. Never starts Minecraft or a server."""
from pathlib import Path
from PIL import Image
import json,zipfile,hashlib,struct,re,subprocess
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/guardians-1.9.4';A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';J=R/'build/libs/slavicmyths-1.9.4.jar';checks=0
def need(ok,why):
 global checks;checks+=1
 if not ok:raise AssertionError(why)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 log=(R/'.tools/guardian-clean-build.log').read_text(encoding='utf-8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build')
 for marker in ['GUARDIAN_OFFLINE_PASS','SANCTUARY_OFFLINE_PASS','NIGHT_OFFLINE_PASS','FROST_OFFLINE_PASS','QUEST185_OFFLINE_PASS','POLOZ_OFFLINE_PASS','120 native']:need(marker in log,marker)
 res=R/'src/main/resources';before={p.relative_to(res).as_posix():digest(p) for p in res.rglob('*') if p.is_file()}
 meshes={p.name:digest(p) for p in (R/'src/main/java/org/slavicmyths/client').glob('*GuardianMesh.java')}
 meshes['SpringKeeperMesh.java']=digest(R/'src/main/java/org/slavicmyths/client/SpringKeeperMesh.java')
 subprocess.run(['python',str(R/'tools/guardian_assets.py')],check=True)
 need(before=={p.relative_to(res).as_posix():digest(p) for p in res.rglob('*') if p.is_file()},'asset generator nondeterminism/rollback')
 for name,sha in meshes.items():need(digest(R/'src/main/java/org/slavicmyths/client'/name)==sha,'mesh generator changed source')
 baseline='ab2397c7ae3aada79526305a620715515e986a7974cc31a54bd056cd4088f4e9';profile=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing');oldpath=profile/'.minecraft/mods/slavicmyths-1.9.3.jar'
 if not oldpath.exists():oldpath=next(p for p in (profile/'.slavicmyths-backups').rglob('slavicmyths-1.9.3.jar') if digest(p)==baseline)
 need(digest(oldpath)==baseline,'incorrect 1.9.3 baseline')
 with zipfile.ZipFile(J) as jar,zipfile.ZipFile(oldpath) as old:
  names=set(jar.namelist());need(b'1.9.4' in jar.read('META-INF/neoforge.mods.toml'),'version');need(not any('/smoke/' in p or p.startswith('tools/') or p.endswith('Checks.class') for p in names),'test code in production')
  for path in (R/'build/classes/java/main').rglob('*.class'):
   name=path.relative_to(R/'build/classes/java/main').as_posix();need(jar.read(name)==path.read_bytes(),'stale bytecode '+name);need(struct.unpack('>H',path.read_bytes()[6:8])[0]==65,'Java 21 '+name)
  for path in res.rglob('*'):
   if not path.is_file():continue
   name=path.relative_to(res).as_posix();data=path.read_bytes();need(name in names,'missing resource '+name)
   if name=='META-INF/neoforge.mods.toml':data=data.replace(b'$'+b'{mod_version}',b'1.9.4').replace(b'$'+b'{neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
   need(jar.read(name)==data,'stale resource '+name)
   if name.endswith('.json'):json.loads(data)
  changed=[]
  for name in old.namelist():
   if name.endswith('/') or name.endswith('.class') or name.startswith('META-INF/'):continue
   need(name in names,'prior resource removed '+name)
   if old.read(name)!=jar.read(name):changed.append(name);need(name in {'assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','data/minecraft/tags/damage_type/is_fire.json'},'prior resource regression '+name)
  langs={locale:json.loads((A/f'lang/{locale}.json').read_text(encoding='utf-8')) for locale in ['ru_ru','en_us']}
  for locale,lang in langs.items():need(all(lang.get(k)==v for k,v in json.loads(old.read(f'assets/slavicmyths/lang/{locale}.json')).items()),'prior translations '+locale)
  sounds=json.loads((A/'sounds.json').read_text(encoding='utf-8'));own={k:v for k,v in sounds.items() if k.startswith('guardians.')};need(len(own)==28,'28 distinct events')
  for key,event in own.items():
   for lang in langs.values():need(event['subtitle'] in lang,'subtitle '+key)
   for sound in event['sounds']:need(jar.read('assets/slavicmyths/sounds/'+sound['name'].split(':')[1]+'.ogg').startswith(b'OggS'),'real OGG '+key)
  rig_stats={}
  for name in ['extinguished_flame_guardian','defiled_spring_keeper']:
   rig=json.loads((O/f'{name}_corrupted-rig.json').read_text(encoding='utf-8'));clean=json.loads((O/f'{name}_cleansed-rig.json').read_text(encoding='utf-8'));need(rig==clean,'shared skeleton/UV');rig_stats[name]={'bones':len(rig['bones']),'boxes':len(rig['boxes']),'atlas':rig['atlas']}
   need(len(rig['bones'])>=14 and len(rig['boxes'])>=80,'substantive independent model')
   for b in rig['boxes']:
    u,v,w,h=b['uv'];need(u>=0 and v>=0 and u+w<=512 and v+h<=512,'UV bounds');need(all(size>0 for size in b['size']),'nonempty cuboid')
   for state in ['corrupted','cleansed','purifying_1','purifying_2']:
    for suffix in ['', '_glow']:need(Image.open(A/f'textures/entity/{name}_{state}{suffix}.png').size==(512,512),'texture variant')
   need(digest(A/f'textures/entity/{name}_corrupted.png')!=digest(A/f'textures/entity/{name}_cleansed.png'),'actual purification palette')
  for name in ['mor_fire_amulet_fragment','mor_water_amulet_fragment']:
   need(Image.open(A/f'textures/item/{name}.png').size==(32,32),'fragment icon');need((A/f'models/item/{name}.json').exists(),'fragment model')
   for lang in langs.values():need('item.slavicmyths.'+name in lang and 'item.slavicmyths.'+name+'.desc' in lang,'fragment translation')
   need(not any(name in jar.read(n).decode('utf-8') for n in names if n.startswith('data/slavicmyths/recipe/')),'no fragment recipe')
   need(not any(name in jar.read(n).decode('utf-8') for n in names if n.startswith('data/slavicmyths/loot_table/')),'no normal fragment loot')
  for name in ['guardian_relic_pedestal','spring_valve']:
   need(len(json.loads((A/f'blockstates/{name}.json').read_text(encoding='utf-8'))['variants'])==8,'native facing/lit blockstates');need((D/f'loot_table/blocks/{name}.json').exists(),'block loot');need((A/f'models/item/{name}.json').exists(),'block item model')
  sources='\n'.join(p.read_text(encoding='utf-8') for p in (R/'src/main/java/org/slavicmyths/guardians').glob('*.java'));need('net.minecraft.client' not in sources,'dedicated common side');need('setForced' not in sources and 'setDayTime' not in sources,'no global time/chunk loading')
  for key in re.findall(r'GuardianContent.text\("([a-z_]+)"',sources):
   for lang in langs.values():need('guardians194.'+key in lang,'raw UI key '+key)
  for name in ['cleansed_flame_guardian','cleansed_spring_keeper','mor_guardian_fragments']:
   need((D/f'tales/{name}.json').exists(),'lore entry')
   for lang in langs.values():
    for stage in ['title','rumor','known','studied','completed','legend']:need('tales.entry.'+name+'.'+stage in lang,'lore localization')
  # Verify a dry traversable valve route in the unchanged authored 1.9.3 layout.
  scene={tuple(b['pos']):b for b in json.loads((R/'docs/verification/sanctuaries-1.9.3/spring-blocks.json').read_text(encoding='utf-8'))}
  for x in range(72,87):
   need(scene.get((x,0,20),{}).get('name')!='minecraft:air','valve route floor')
   for y in range(1,4):need(scene.get((x,y,20),{}).get('name')=='minecraft:air','valve route clearance')
  for x,z in [(70,20),(90,20),(74,10),(79,21)]:
   need(scene.get((x,1,z),{}).get('name')=='minecraft:air','compact hook does not overwrite a structure block')
  sub=subscribers(J);need(sub>0,'event subscriber bytecode')
 result={'status':'PASS','version':'1.9.4','checks':checks,'sha256':digest(J),'baseline_sha256':baseline,'old_resources_changed':changed,'rigs':rig_stats,'original_sound_events':28,'fragments':2,'new_fragment_recipes':0,'client_starts':0,'server_starts':0,'gameplay':'NOT_RUN','MP':'NOT_RUN','relog':'NOT_RUN','MSPT':'NOT_MEASURED','event_subscribers':sub,'asset_reproducibility':'PASS','ordinary_193_loot_recipes_templates':'UNCHANGED'}
 (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
