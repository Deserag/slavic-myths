"""Read-only gates against the exact installed 1.9.5.1 baseline; no game/server starts."""
from pathlib import Path
from PIL import Image
import hashlib,json,re,struct,zipfile,subprocess
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/gorynych-1.9.6';J=R/'build/libs/slavicmyths-1.9.6.jar';checks=0
BASE='45c33a8b7275e475fa50287505ad0d0916068939ffcf41c9a59903d1c99fa910'
def need(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 log=(R/'.tools/gorynych-clean-build.log').read_text(encoding='utf-8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build')
 for marker in ['GORYNYCH_OFFLINE_PASS','HILL_OFFLINE_PASS','GUARDIAN_OFFLINE_PASS','SANCTUARY_OFFLINE_PASS','NIGHT_OFFLINE_PASS','FROST_OFFLINE_PASS','QUEST185_OFFLINE_PASS','POLOZ_OFFLINE_PASS','120 native']:need(marker in log,marker)
 resources=R/'src/main/resources';before={p.relative_to(resources).as_posix():sha(p) for p in resources.rglob('*') if p.is_file()};mesh=R/'src/main/java/org/slavicmyths/client/GorynychMesh.java';mesh_before=sha(mesh)
 subprocess.run(['python',str(R/'tools/gorynych_assets.py')],check=True)
 need(before=={p.relative_to(resources).as_posix():sha(p) for p in resources.rglob('*') if p.is_file()},'generator must reproduce resources exactly')
 need(mesh_before==sha(mesh),'native mesh generator reproducibility')
 profile=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing');oldpath=profile/'.minecraft/mods/slavicmyths-1.9.5.1.jar'
 if not oldpath.exists() or sha(oldpath)!=BASE:oldpath=next(p for p in (profile/'.slavicmyths-backups').rglob('slavicmyths-1.9.5.1.jar') if sha(p)==BASE)
 need(sha(oldpath)==BASE,'exact previous production baseline')
 allowed={'assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','data/slavicmyths/loot_table/chests/serpent_fortress.json','data/slavicmyths/recipe/mor_ward.json','data/minecraft/tags/damage_type/is_fire.json','slavicmyths.equipment.mixins.json'};changed=[]
 with zipfile.ZipFile(J) as jar,zipfile.ZipFile(oldpath) as old:
  names=set(jar.namelist());need(b'1.9.6' in jar.read('META-INF/neoforge.mods.toml'),'version')
  need(not any('/smoke/' in n or n.startswith('tools/') or n.endswith('Checks.class') for n in names),'test code must stay outside production')
  for p in (R/'build/classes/java/main').rglob('*.class'):
   name=p.relative_to(R/'build/classes/java/main').as_posix();need(jar.read(name)==p.read_bytes(),'current compiled class '+name);need(struct.unpack('>H',p.read_bytes()[6:8])[0]==65,'Java21 '+name)
  for p in resources.rglob('*'):
   if not p.is_file():continue
   name=p.relative_to(resources).as_posix();data=p.read_bytes()
   if name=='META-INF/neoforge.mods.toml':data=data.replace(b'${mod_version}',b'1.9.6').replace(b'${neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
   need(jar.read(name)==data,'packaged source resource '+name)
   if name.endswith('.json'):json.loads(data)
  for name in old.namelist():
   if name.endswith('/') or name.endswith('.class') or name.startswith('META-INF/'):continue
   need(name in names,'previous resource removed '+name)
   if old.read(name)!=jar.read(name):changed.append(name);need(name in allowed,'unrelated previous resource changed '+name)
  content=(R/'src/main/java/org/slavicmyths/gorynych/GorynychContent.java').read_text();sounds=json.loads(jar.read('assets/slavicmyths/sounds.json'))
  sound_line=content.split('SOUNDS_LIST=List.of(')[1].split(');')[0];sound_names=[s.strip().strip('"') for s in sound_line.split(',')]
  need(len(sound_names)==31,'31 actual audio events')
  for sound in sound_names:
   need('gorynych.'+sound in sounds,'sound event '+sound);need(jar.read('assets/slavicmyths/sounds/gorynych/'+sound+'.ogg').startswith(b'OggS'),'real OGG '+sound)
  for image in ['corrupted','purified']+['clean_'+str(i) for i in range(1,8)]:
   p=resources/('assets/slavicmyths/textures/entity/zmey_gorynych_'+image+'.png');im=Image.open(p);need(im.size==(1024,1024),'matching full atlas '+image)
  rig=json.loads((O/'zmey_gorynych_corrupted-rig.json').read_text());need(len(rig['boxes'])>=290 and len(rig['bones'])>=40,'real articulated rig')
  for box in rig['boxes']:
   u,v,w,h=box['uv'];need(u>=0 and v>=0 and u+w<=1024 and v+h<=1024,'authored UV rect')
  for weapon in ['serpent_crossbow','three_breath_crossbow']:
   for state in ['','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework']:
    model=json.loads(jar.read('assets/slavicmyths/models/item/'+weapon+state+'.json'));need(len(model['elements'])>=8,'real 3D crossbow '+weapon+state);need('firstperson_righthand' in model['display'] and 'thirdperson_righthand' in model['display'],'first/third person weapon placement')
  for locale in ['ru_ru','en_us']:
   language=json.loads(jar.read('assets/slavicmyths/lang/'+locale+'.json'))
   for item in ['gorynych_scale','ancient_serpent_steel','serpent_crossbow','three_breath_crossbow','serpent_seal','gorynych_flame_breath_bottle','gorynych_mor_breath_bottle','gorynych_storm_breath_bottle','rune_mor_breath','rune_dragon_flame','rune_storm_strike','dragon_helmet','dragon_chestplate','dragon_leggings','dragon_boots','zmey_gorynych_spawn_egg']:
    need('item.slavicmyths.'+item in language,'new item localization '+item);need('item.slavicmyths.'+item+'.desc' in language,'new item tooltip localization '+item);need('assets/slavicmyths/models/item/'+item+'.json' in names,'new item model '+item)
   for stage in ['rumor','discovered','studied','verified']:need('tales.entry.zmey_gorynych.'+stage in language,'book page localization')
  for shader in ['slavicmyths:ancient_serpent_steel','slavicmyths:serpent_crossbow']:
   need(shader in jar.read('data/slavicmyths/loot_table/chests/serpent_fortress.json').decode(),'fortress loot '+shader)
  ward=jar.read('data/slavicmyths/recipe/mor_ward.json').decode();need(all(name in ward for name in ['mor_fire_amulet_fragment','mor_water_amulet_fragment','eye_of_mor']),'existing ward chain');need(json.loads(ward)['category']=='misc','personal ward recipe category')
  need('GorynychEffectDuration' in jar.read('slavicmyths.equipment.mixins.json').decode(),'effect duration accessor registration')
 world=(R/'src/main/java/org/slavicmyths/gorynych/GorynychWorld.java').read_text();boss=(R/'src/main/java/org/slavicmyths/gorynych/Gorynych.java').read_text()
 for p in (R/'src/main/java/org/slavicmyths/gorynych').glob('*.java'):need('net.minecraft.client.' not in p.read_text(),'common/server isolation '+p.name)
 need('PopulationAt' in world and 'now+40' in world and 'areEntitiesLoaded' in world,'bounded UUID-aware loaded population')
 need('w.getChunk(' not in world and 'instance' in world,'no forced population chunks')
 need('Math.max(1,value)' in boss and 'pendingDamage' in boss and 'GorynychRules.remaining' in boss,'logical HP owns lethal damage')
 need('private int danger()' in boss and 'danger()<2' in boss,'bounded simultaneous attacks')
 need('tickCount-nearbyAt>=10' in boss,'bounded entity-local targeting')
 need('GorynychRules.canClean' in boss and 'stack.shrink(1)' in boss,'server head cleansing')
 need('surface()' in boss and 'age()>=20&&node>=3' in boss,'surface transition before wings unfold')
 need('hp' not in (R/'src/main/java/org/slavicmyths/gorynych/GorynychHead.java').read_text().lower(),'no independent head health')
 need('rune_three' not in content and 'rune_of_three' not in content,'no future combined rune')
 result={'status':'PASS','version':'1.9.6','checks':checks,'sha256':sha(J),'baseline_sha256':BASE,'changed_old_resources':changed,'event_subscribers':subscribers(J),'client_starts':0,'server_starts':0,'runtime':'NOT_RUN'};O.mkdir(parents=True,exist_ok=True);(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
