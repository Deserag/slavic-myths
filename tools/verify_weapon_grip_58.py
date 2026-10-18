"""#58 actual mesh contact, item parity, production JAR; not owner acceptance."""
from pathlib import Path
from PIL import Image
import subprocess,sys,json,gzip,hashlib,zipfile
R=Path(__file__).resolve().parents[1];BASE='f61bb7bb';checks=[]
def check(ok,why):
 if not ok:raise AssertionError(why)
 checks.append(why)
def main():
 expected={'src/main/java/org/slavicmyths/client/'+n+'.java' for n in ['KarachunModel','MorozkoModel','KarachunMesh','MorozkoMesh','WeaponGrip58']}
 changed=set(subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=R,text=True).splitlines());changed.add('src/main/java/org/slavicmyths/client/WeaponGrip58.java');check(changed==expected,'Only two boss presentation rigs; no gameplay/assets/items/sounds changed')
 snap={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in expected}
 subprocess.run([sys.executable,str(R/'tools/weapon_grip_58_mesh.py')],cwd=R,check=True)
 for p,h in snap.items():check(hashlib.sha256((R/p).read_bytes()).hexdigest()==h,'Idempotent post-emit '+p)
 for cls,weapon in [('KarachunMesh','scythe'),('MorozkoMesh','staff_root')]:
  path='src/main/java/org/slavicmyths/client/'+cls+'.java';old=subprocess.check_output(['git','show',BASE+':'+path],cwd=R,text=True);new=(R/path).read_text(encoding='utf-8')
  import re
  for part in ([weapon] if weapon=='scythe' else [weapon,weapon+'_head',weapon+'_crystals']):
   line=lambda text:next(x for x in text.splitlines() if x.startswith('var '+part+'='))
   check(re.findall(r'texOffs\([^)]*\)\.addBox\([^)]*\)',line(old))==re.findall(r'texOffs\([^)]*\)\.addBox\([^)]*\)',line(new)),'Exact original weapon silhouette/material UV '+part)
 for name in ['karachun','morozko']:
  stem='karachun_scythe' if name=='karachun' else 'morozko_staff'
  for suffix in ['_held','_icon']:
   path=f'src/main/resources/assets/slavicmyths/models/item/{stem}{suffix}.json';old=subprocess.check_output(['git','show',BASE+':'+path],cwd=R);check((R/path).read_bytes().replace(b'\r\n',b'\n')==old.replace(b'\r\n',b'\n'),'Exact item parity '+path)
   data=json.loads((R/path).read_text(encoding='utf-8'))
   for element in data.get('elements',[]):
    check(all(-1000<v<1000 for v in element['from']+element['to']),'Finite original held element')
    for face in element['faces'].values():check(all(0<=v<=16 for v in face['uv']),'Original held atlas UV')
  check('apply_mesh' in (R/f'tools/{name}_assets.py').read_text(encoding='utf-8'),'Canonical generator calls mesh contact correction '+name)
 a=json.loads(gzip.decompress((R/'build/weapon-grip-58/after-native.json.gz').read_bytes()));counts={}
 for mob in ['karachun','morozko']:
  counts[mob]=len(a[mob])
  for key,faces in a[mob].items():
   for face in faces:
    for v in face:check(len(v)==5 and all(-1e6<x<1e6 for x in v) and 0<=v[3]<=1 and 0<=v[4]<=1,'Finite native '+mob+'/'+key)
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    k=p.relative_to(R/'src/main/resources').as_posix()
    if k!='META-INF/neoforge.mods.toml':check(z.read(k)==p.read_bytes(),'Production resource parity '+k)
  check(not any('org/slavicmyths/verify/' in n for n in z.namelist()),'No offline fixtures in JAR')
  for owner in ['KarachunModel','MorozkoModel','KarachunMesh','MorozkoMesh','WeaponGrip58']:
   for p in (R/'build/classes/java/main/org/slavicmyths/client').glob(owner+'*.class'):
    k=p.relative_to(R/'build/classes/java/main').as_posix();check(z.read(k)==p.read_bytes(),'Compiled class parity '+k)
 report=dict(status='IN_PROGRESS',checks=len(checks),source_poses=counts,grip_and_torso_sweep_ticks_per_action=65,server_unchanged=True,held_and_icon_unchanged=True,texture_and_glow_unchanged=True,audio='FAIL: real new foley/listening not completed; no replacements',owner_accepted=False,runtime_launches=0,poly_mc='NOT_INSTALLED',jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest())
 (R/'docs/implementation/weapon-grip-58/gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
