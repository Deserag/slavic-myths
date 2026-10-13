"""Production/resource checks; no game or server launch."""
from pathlib import Path
import hashlib,json,struct,zipfile,subprocess
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1]
O=R/'docs/verification/guardian-spawn-1.9.5.1'
J=R/'build/libs/slavicmyths-1.9.5.1.jar'
BASE='dbdb101677397ac28ebe4b937d6f2352956fb4e40f36d6c3c55aebd885047b22'
checks=0
def need(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 log=(R/'.tools/guardian-spawn-clean-build.log').read_text(encoding='utf-8',errors='replace')
 need('BUILD SUCCESSFUL' in log,'clean build')
 for marker in ('HILL_OFFLINE_PASS','GUARDIAN_OFFLINE_PASS','SANCTUARY_OFFLINE_PASS','NIGHT_OFFLINE_PASS','FROST_OFFLINE_PASS','QUEST185_OFFLINE_PASS','POLOZ_OFFLINE_PASS','120 native'):need(marker in log,marker)
 resources=R/'src/main/resources'
 before={p.relative_to(resources).as_posix():sha(p) for p in resources.rglob('*') if p.is_file()}
 subprocess.run(['python',str(R/'tools/guardian_spawn_assets.py')],check=True)
 need(before=={p.relative_to(resources).as_posix():sha(p) for p in resources.rglob('*') if p.is_file()},'egg generator reproducibility')
 profile=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing')
 oldpath=profile/'.minecraft/mods/slavicmyths-1.9.5.jar'
 if not oldpath.exists() or sha(oldpath)!=BASE:oldpath=next(p for p in (profile/'.slavicmyths-backups').rglob('slavicmyths-1.9.5.jar') if sha(p)==BASE)
 need(sha(oldpath)==BASE,'1.9.5 baseline')
 changed=[]
 with zipfile.ZipFile(J) as jar,zipfile.ZipFile(oldpath) as old:
  names=set(jar.namelist())
  need(b'1.9.5.1' in jar.read('META-INF/neoforge.mods.toml'),'version')
  need(not any('/smoke/' in n or n.startswith('tools/') or n.endswith('Checks.class') for n in names),'test code excluded')
  for p in (R/'build/classes/java/main').rglob('*.class'):
   name=p.relative_to(R/'build/classes/java/main').as_posix()
   need(jar.read(name)==p.read_bytes(),'current bytecode '+name)
   need(struct.unpack('>H',p.read_bytes()[6:8])[0]==65,'Java21 '+name)
  for p in resources.rglob('*'):
   if not p.is_file():continue
   name=p.relative_to(resources).as_posix();data=p.read_bytes()
   if name=='META-INF/neoforge.mods.toml':data=data.replace(b'${mod_version}',b'1.9.5.1').replace(b'${neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
   need(jar.read(name)==data,'current packaged resource '+name)
   if name.endswith('.json'):json.loads(data)
  for name in old.namelist():
   if name.endswith('/') or name.endswith('.class') or name.startswith('META-INF/'):continue
   need(name in names,'old resource removed '+name)
   if jar.read(name)!=old.read(name):
    changed.append(name)
    need(name in ('assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'),'unrelated resource changed '+name)
  for egg in ('extinguished_flame_guardian_spawn_egg','defiled_spring_keeper_spawn_egg'):
   model=json.loads(jar.read(f'assets/slavicmyths/models/item/{egg}.json'))
   need(model['parent']=='minecraft:item/template_spawn_egg','real vanilla egg model')
   for locale in ('ru_ru','en_us'):
    need(f'item.slavicmyths.{egg}' in json.loads(jar.read(f'assets/slavicmyths/lang/{locale}.json')),'egg localization')
 world=(R/'src/main/java/org/slavicmyths/guardians/GuardianWorld.java').read_text()
 entity=(R/'src/main/java/org/slavicmyths/guardians/GuardianEntity.java').read_text()
 piece=(R/'src/main/java/org/slavicmyths/sanctuary/SanctuaryPiece.java').read_text()
 need('server.getServer().execute(()->org.slavicmyths.guardians.GuardianWorld.generated' in piece,'population dispatched on server thread')
 need('if(!loaded(w,chamber(n)))return' in world,'population waits for loaded chamber')
 need('getChunkNow' in world and '.getChunk(' not in world,'no forced AI chunks')
 need('PopulationCheckAt' in world and 'now+20' in world,'bounded local population frequency')
 need('MissingSince' in world and 'LastPosition' in world and 'e!=keeper)e.discard()' in world,'UUID and unloaded duplicate safeguards')
 need('&&any&&!n.getString("Stage").equals("SEALED")' in entity,'sealed guardian stays dormant')
 need('if(instance==null){standalone(w);}' in entity,'unbound spawn creates encounter')
 need('layout.getBoolean("Standalone")&&state()==GuardianRules.PURIFY' in entity,'standalone direct purification')
 result={'status':'PASS','version':'1.9.5.1','checks':checks,'sha256':sha(J),'baseline_sha256':BASE,'changed_old_resources':changed,'event_subscriber_check':subscribers(J),'client_starts':0,'server_starts':0,'runtime':'NOT_RUN'}
 O.mkdir(parents=True,exist_ok=True)
 (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
