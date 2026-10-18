"""Nightingale #57 resource/native/JAR checks; no runtime launch or acceptance."""
from pathlib import Path
from PIL import Image
import subprocess,sys,json,gzip,hashlib,zipfile
R=Path(__file__).resolve().parents[1];BASE='a3a47d1e';checks=[]
def check(ok,why):
 if not ok:raise AssertionError(why)
 checks.append(why)
def main():
 expected={'src/main/java/org/slavicmyths/client/'+n+'.java' for n in ['NightingaleModel','NightingalePoses','BossReworkGeometry']}
 expected|={'src/main/resources/assets/slavicmyths/boss_geometry/nightingale_phase'+str(i)+'.json' for i in [1,2]}
 expected|={'src/main/resources/assets/slavicmyths/textures/entity/nightingale_phase'+str(i)+'.png' for i in [1,2]}
 changed=set(subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=R,text=True).splitlines());changed.add('src/main/java/org/slavicmyths/client/NightingalePoses.java')
 check(changed==expected,'Only Nightingale client presentation changed, no gameplay/sounds/items/IDs')
 snap={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in expected if not any(n in p for n in ['NightingaleModel','NightingalePoses'])}
 subprocess.run([sys.executable,str(R/'tools/boss_reimplementation_resources.py'),'--nightingale-model-only'],cwd=R,check=True,capture_output=True)
 for p,h in snap.items():check(hashlib.sha256((R/p).read_bytes()).hexdigest()==h,'Deterministic generator '+p)
 before=json.loads(gzip.decompress((R/'docs/implementation/nightingale-57/before-native.json.gz').read_bytes()));after=json.loads(gzip.decompress((R/'build/nightingale-57/after-native.json.gz').read_bytes()))
 check(before['tugarin_controls']==after['tugarin_controls'],'Exact six native Tugarin control samples unchanged')
 bounds={}
 for form in ['first','second']:
  check(set(before[form])==set(after[form]) and len(after[form])==58,'Same 58 samples '+form)
  for key,faces in after[form].items():
   for face in faces:
    for v in face:check(len(v)==5 and all(-1e6<x<1e6 for x in v) and 0<=v[3]<=1 and 0<=v[4]<=1,'Finite baked UV '+form+'/'+key)
  vs=[v for face in after[form]['idle'] for v in face];bounds[form]=[[min(v[i] for v in vs)/16,max(v[i] for v in vs)/16] for i in range(3)]
  check(bounds[form][1][0]>=1.5-1.85 and bounds[form][1][1]<=1.501,'Idle height fits unchanged 1.85 '+form)
 for i in [1,2]:
  im=Image.open(R/f'src/main/resources/assets/slavicmyths/textures/entity/nightingale_phase{i}.png');im.load();check(im.size==(1024,1024) and im.mode=='RGB','Established native atlas '+str(i))
 for path in ['src/main/java/org/slavicmyths/bandit/NightingaleEntity.java','src/main/java/org/slavicmyths/client/NightingaleRenderer.java','src/main/resources/assets/slavicmyths/textures/entity/nightingale.png','src/main/resources/assets/slavicmyths/models/item/nightingale_dagger_held.json']:
  if not (R/path).exists():continue
  old=subprocess.check_output(['git','show',BASE+':'+path],cwd=R);check((R/path).read_bytes().replace(b'\r\n',b'\n')==old.replace(b'\r\n',b'\n'),'Preserved '+path)
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    k=p.relative_to(R/'src/main/resources').as_posix()
    if k!='META-INF/neoforge.mods.toml':check(z.read(k)==p.read_bytes(),'JAR parity '+k)
  check(not any('org/slavicmyths/verify/' in n for n in z.namelist()),'No offline fixture classes in JAR')
  for owner in ['NightingaleModel','NightingalePoses','BossReworkGeometry']:
   for compiled in (R/'build/classes/java/main/org/slavicmyths/client').glob(owner+'*.class'):
    k=compiled.relative_to(R/'build/classes/java/main').as_posix();check(z.read(k)==compiled.read_bytes(),'Actual compiled class parity '+k)
 report=dict(status='IN_PROGRESS',checks=len(checks),forms=2,native_poses_per_form=58,tugarin_control_parity=True,idle_bounds_blocks=bounds,physical_hitbox=[.95,1.85],weapons_and_cloak_full_footprint_fit=False,jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),audio='FAIL: no new accepted real whistle/wind/foley; old assets unchanged',runtime_launches=0,owner_accepted=False,poly_mc='NOT_INSTALLED')
 (R/'docs/implementation/nightingale-57/gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
