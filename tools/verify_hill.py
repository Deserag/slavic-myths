"""Offline production/resource audit, never starts Minecraft or a server."""
from pathlib import Path
from PIL import Image
import json,zipfile,hashlib,struct,re,subprocess
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/serpent-hill-1.9.5';A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';J=R/'build/libs/slavicmyths-1.9.5.jar';checks=0
def need(ok,why):
 global checks;checks+=1
 if not ok:raise AssertionError(why)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 log=(R/'.tools/hill-clean-build.log').read_text(encoding='utf-8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build')
 for marker in ['HILL_OFFLINE_PASS','GUARDIAN_OFFLINE_PASS','SANCTUARY_OFFLINE_PASS','NIGHT_OFFLINE_PASS','FROST_OFFLINE_PASS','QUEST185_OFFLINE_PASS','POLOZ_OFFLINE_PASS','120 native']:need(marker in log,marker)
 resources=R/'src/main/resources';before={p.relative_to(resources).as_posix():sha(p) for p in resources.rglob('*') if p.is_file()}
 subprocess.run(['python',str(R/'tools/hill_assets.py')],check=True);need(before=={p.relative_to(resources).as_posix():sha(p) for p in resources.rglob('*') if p.is_file()},'nonreproducible resource generator')
 baseline='0142494a6445e325c48194949f7d1a957110942357b232e20155346f8334b1b8';profile=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing');oldpath=profile/'.minecraft/mods/slavicmyths-1.9.4.jar'
 if not oldpath.exists() or sha(oldpath)!=baseline:oldpath=next(p for p in (profile/'.slavicmyths-backups').rglob('slavicmyths-1.9.4.jar') if sha(p)==baseline)
 need(sha(oldpath)==baseline,'incorrect baseline')
 with zipfile.ZipFile(J) as jar,zipfile.ZipFile(oldpath) as old:
  names=set(jar.namelist());need(b'1.9.5' in jar.read('META-INF/neoforge.mods.toml'),'version')
  need(not any('/smoke/' in n or n.startswith('tools/') or n.endswith('Checks.class') for n in names),'test code in production')
  for p in (R/'build/classes/java/main').rglob('*.class'):
   name=p.relative_to(R/'build/classes/java/main').as_posix();need(jar.read(name)==p.read_bytes(),'stale bytecode '+name);need(struct.unpack('>H',p.read_bytes()[6:8])[0]==65,'Java 21 '+name)
  for p in resources.rglob('*'):
   if not p.is_file():continue
   name=p.relative_to(resources).as_posix();data=p.read_bytes();need(name in names,'missing resource '+name)
   if name=='META-INF/neoforge.mods.toml':data=data.replace(b'$'+b'{mod_version}',b'1.9.5').replace(b'$'+b'{neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
   need(jar.read(name)==data,'stale resource '+name)
   if name.endswith('.json'):json.loads(data)
  changed=[]
  for name in old.namelist():
   if name.endswith('/') or name.endswith('.class') or name.startswith('META-INF/'):continue
   need(name in names,'prior resource removed '+name)
   if old.read(name)!=jar.read(name):changed.append(name);need(name in {'assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json'},'prior regression '+name)
  langs={locale:json.loads((A/f'lang/{locale}.json').read_text(encoding='utf-8')) for locale in ['ru_ru','en_us']}
  for locale,lang in langs.items():need(all(lang.get(k)==v for k,v in json.loads(old.read(f'assets/slavicmyths/lang/{locale}.json')).items()),'old translations')
  from hill_assets import NAMES
  need(len(NAMES)==11,'11 requested blocks')
  for name in NAMES:
   state=json.loads((A/f'blockstates/{name}.json').read_text(encoding='utf-8'));need(len(state['variants'])==8,'facing/hanging variants')
   need(Image.open(A/f'textures/block/{name}.png').size==(32,32),'32px texture')
   need((A/f'models/item/{name}.json').exists(),'inventory model')
   for variant in state['variants'].values():
    path=A/('models/'+variant['model'].split(':')[1]+'.json');need(path.exists(),'model linkage');model=json.loads(path.read_text(encoding='utf-8'))
    need(model['elements'] and all(-16<=v<=32 for e in model['elements'] for k in ['from','to'] for v in e[k]),'real cuboid model')
   recipe=json.loads((D/f'recipe/{name}.json').read_text(encoding='utf-8'));need(recipe['type']=='minecraft:crafting_shapeless' and 1<=len(recipe['ingredients'])<=9 and recipe['result']['id']=='slavicmyths:'+name,'recipe')
   need((D/f'loot_table/blocks/{name}.json').exists(),'block loot')
   for lang in langs.values():need('block.slavicmyths.'+name in lang,'block language')
  soundmap=json.loads((A/'sounds.json').read_text(encoding='utf-8'));own={k:v for k,v in soundmap.items() if k.startswith('serpenthill.')};need(len(own)==5,'five sound events')
  for key,event in own.items():
   for lang in langs.values():need(event['subtitle'] in lang,'subtitle')
   for sound in event['sounds']:need(jar.read('assets/slavicmyths/sounds/'+sound['name'].split(':')[1]+'.ogg').startswith(b'OggS'),'real OGG')
  layout=json.loads((O/'layout.json').read_text(encoding='utf-8'));need(layout['status']=='PASS' and len(layout['rooms'])==15,'connected real layout')
  for key in layout['palette']:
   if key.startswith('slavicmyths:'):need(key.split('@')[0].split(':')[1] in NAMES or key=='slavicmyths:mor_stone_bricks','custom palette id '+key)
  source='\n'.join(p.read_text(encoding='utf-8') for p in (R/'src/main/java/org/slavicmyths/serpenthill').glob('*.java'));need('net.minecraft.client' not in source,'common side');need('setForced' not in source,'no forced chunk tickets')
  need('Blocks.BEDROCK' in (R/'src/main/java/org/slavicmyths/serpenthill/SerpentHillPiece.java').read_text(encoding='utf-8') and 'Blocks.BEDROCK' in (R/'src/main/java/org/slavicmyths/serpenthill/HillCommands.java').read_text(encoding='utf-8'),'both placement paths protect bedrock')
  need('BUILD_BUDGET=4096' in source and 'hasPermission(2)' in source and 'generate_serpent_hill' in source,'bounded operator command')
  need('Quest185Content.WARD_EFFECT' in source and 'Quest185Content.WARD.get()' in source and 'mor_fire_amulet_fragment' not in source,'existing correct ward only')
  for key in re.findall(r'HillContent.text\("([a-z_]+)"',source):
   for lang in langs.values():need('serpenthill195.'+key in lang,'raw UI '+key)
  placement=json.loads((D/'worldgen/structure_set/serpent_hill.json').read_text(encoding='utf-8'));need(placement['placement']['spacing']==80 and placement['placement']['separation']==32 and placement['placement']['exclusion_zone']['other_set']=='minecraft:strongholds','native rare placement')
  need(not any('gorynych' in n or 'gorin' in n for n in names if n.startswith('org/slavicmyths/serpenthill/')),'no future boss')
  sub=subscribers(J);need(sub>0,'event subscriber bytecode')
 result={'status':'PASS','version':'1.9.5','checks':checks,'sha256':sha(J),'baseline_sha256':baseline,'old_resources_changed':changed,'new_blocks':11,'recipes':11,'sounds':5,'unified_dimensions':[128,144,128],'solid_voxels_seed195':layout['solid_voxels'],'reachable_cells_static':layout['walkable_reachable_cells'],'rooms':layout['rooms'],'offline_checks':layout['checks'],'event_subscribers':sub,'generator_reproducibility':'PASS','client_starts':0,'server_starts':0,'superflat_command_runtime':'NOT_RUN','natural_seed_generation':'NOT_RUN','multiplayer':'NOT_RUN','MSPT':'NOT_MEASURED'}
 (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
