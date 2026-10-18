"""#56 native/source/JAR evidence; never runtime or owner acceptance."""
from pathlib import Path
from PIL import Image
import subprocess,sys,json,gzip,hashlib,zipfile
R=Path(__file__).resolve().parents[1];BASE='bcec6355';checks=[]
def check(ok,why):
 if not ok:raise AssertionError(why)
 checks.append(why)
def main():
 expected={'src/main/java/org/slavicmyths/client/'+n+'.java' for n in ['WorldBossModel','TugarinPoses','BossReworkGeometry']}
 expected|={'src/main/resources/assets/slavicmyths/boss_geometry/tugarin_phase'+str(i)+'.json' for i in [1,2]}
 expected|={'src/main/resources/assets/slavicmyths/textures/entity/tugarin_phase'+str(i)+'.png' for i in [1,2]}
 changed=set(subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=R,text=True).splitlines())
 changed.update(p.relative_to(R).as_posix() for p in [R/'src/main/java/org/slavicmyths/client/TugarinPoses.java'] if p.exists())
 check(changed==expected,'Only approved Tugarin presentation scope; no gameplay/audio/other creatures')
 snap={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in expected if not any(n in p for n in ['WorldBossModel','TugarinPoses'])}
 subprocess.run([sys.executable,str(R/'tools/boss_reimplementation_resources.py'),'--tugarin-model-only'],cwd=R,check=True,capture_output=True)
 for p,h in snap.items():check(hashlib.sha256((R/p).read_bytes()).hexdigest()==h,'Deterministic Tugarin generator '+p)
 before=json.loads(gzip.decompress((R/'docs/implementation/tugarin-56/before-native.json.gz').read_bytes()));after=json.loads(gzip.decompress((R/'build/tugarin-56/after-native.json.gz').read_bytes()))
 for form in ['likho','mor_likho']:check(after[form]==before[form],'Exact all-44-pose native geometry preservation '+form)
 for form in ['warrior','serpent']:
  check(set(before[form])==set(after[form]) and len(after[form])==44,'Same 44 native pose samples '+form)
  for key,faces in after[form].items():
   for face in faces:
    for v in face:check(len(v)==5 and all(-1e6<x<1e6 for x in v) and 0<=v[3]<=1 and 0<=v[4]<=1,'Finite native UV '+form+'/'+key)
 for i in [1,2]:
  im=Image.open(R/f'src/main/resources/assets/slavicmyths/textures/entity/tugarin_phase{i}.png');im.load();check(im.size==(1024,1024) and im.mode=='RGB','Actual established 1024 native atlas; no scaling to stale issue dimensions')
 vertices=[v for face in after['warrior']['IDLE'] for v in face];bounds=[[min(v[i] for v in vertices)/16,max(v[i] for v in vertices)/16] for i in range(3)]
 check(bounds[0][0]>=-1.75 and bounds[0][1]<=1.75 and bounds[2][0]>=-1.75 and bounds[2][1]<=1.75 and bounds[1][0]>=1.5-4.85 and bounds[1][1]<=1.501,'Warrior idle including weapons fits unchanged 3.5x4.85')
 for path in ['src/main/java/org/slavicmyths/hunt/TugarinCombat.java','src/main/java/org/slavicmyths/hunt/WorldBoss.java','src/main/java/org/slavicmyths/hunt/BossRules.java','src/main/java/org/slavicmyths/client/WorldBossRenderer.java','src/main/resources/assets/slavicmyths/boss_geometry/nightingale_phase1.json','src/main/resources/assets/slavicmyths/boss_geometry/nightingale_phase2.json']:
  old=subprocess.check_output(['git','show',BASE+':'+path],cwd=R);check((R/path).read_bytes().replace(b'\r\n',b'\n')==old.replace(b'\r\n',b'\n'),'Preserved '+path)
 old_model=subprocess.check_output(['git','show',BASE+':src/main/java/org/slavicmyths/client/WorldBossModel.java'],cwd=R,text=True)
 new_model=(R/'src/main/java/org/slavicmyths/client/WorldBossModel.java').read_text(encoding='utf-8')
 def higher(text):return text[text.index(' public void poseHigher('):text.index(' public void add(')].strip()
 check(higher(old_model)==higher(new_model),'Full higher-Likho skill implementation unchanged')
 vertices2=[v for face in after['serpent']['IDLE'] for v in face];serpent_bounds=[[min(v[i] for v in vertices2)/16,max(v[i] for v in vertices2)/16] for i in range(3)]
 check(serpent_bounds[1][0]>=-3.35 and serpent_bounds[1][1]<=1.501,'Serpent idle height fits existing dimensions')
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    k=p.relative_to(R/'src/main/resources').as_posix()
    if k!='META-INF/neoforge.mods.toml':check(z.read(k)==p.read_bytes(),'JAR parity '+k)
  check(not any('org/slavicmyths/verify/' in n for n in z.namelist()),'No offline fixtures in production')
  for owner in ['WorldBossModel','TugarinPoses','BossReworkGeometry']:
   for compiled in (R/'build/classes/java/main/org/slavicmyths/client').glob(owner+'*.class'):
    entry=compiled.relative_to(R/'build/classes/java/main').as_posix();check(z.read(entry)==compiled.read_bytes(),'Actual compiled model class parity '+entry)

 report=dict(status='IN_PROGRESS',checks=len(checks),native_poses_per_form=44,additional_fire_release_samples=3,forms=4,likho_and_mor_likho_native_parity=True,warrior_idle_armed_bounds_blocks=bounds,physical_hitbox=[3.5,4.85],serpent_idle_bounds_blocks=serpent_bounds,serpent_full_silhouette_inside_hitbox=all(-1.75<=serpent_bounds[i][0] and serpent_bounds[i][1]<=1.75 for i in [0,2]) and serpent_bounds[1][0]>=-3.35,jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),audio='BLOCKED_NEEDS_AUDIO_SOURCE: recorded candidates not retrieved; canonical preview URLs unverified, HTTP403/player blocked; no fake replacement',runtime_launches=0,owner_accepted=False,poly_mc='NOT_INSTALLED')
 (R/'docs/implementation/tugarin-56/gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
