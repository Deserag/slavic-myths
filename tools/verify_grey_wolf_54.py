"""#54 native asset/production checks. Source proof, never in-game acceptance."""
from pathlib import Path
import sys,json,gzip,hashlib,subprocess,zipfile,runpy
from PIL import Image
R=Path(__file__).resolve().parents[1];BASE='8888d0e1';checks=[]
def check(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def main():
 expected={'src/main/java/org/slavicmyths/client/GreyWolfModel.java','src/main/java/org/slavicmyths/client/GreyWolfGeometry.java','src/main/resources/assets/slavicmyths/grey_wolf_geometry.json','src/main/resources/assets/slavicmyths/textures/entity/grey_wolf.png'}
 changed=set(subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=R,text=True).splitlines());check(changed==expected,'Exactly the approved model/PNG/geometry source scope; no AI/story/IDs/audio')
 snap={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in expected if not p.endswith('GreyWolfModel.java')}
 runpy.run_path(str(R/'tools/grey_wolf_resources.py'),run_name='__main__')
 for p,digest in snap.items():check(hashlib.sha256((R/p).read_bytes()).hexdigest()==digest,'Generator reproduces '+p)
 im=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/grey_wolf.png');im.load();check(im.size==(512,512) and im.mode=='RGB','Native authored atlas dimensions; opaque RGB')
 data=json.loads(gzip.decompress((R/'build/grey-wolf-54/after-native.json.gz').read_bytes()));before=json.loads(gzip.decompress((R/'docs/implementation/grey-wolf-54/before-native.json.gz').read_bytes()));check(set(data)==set(before) and len(data)==15,'Same 15 actual production state/move pose samples')
 for key,faces in data.items():
  for face in faces:
   for v in face:check(len(v)==5 and all(-1e6<x<1e6 for x in v) and 0<=v[3]<=1 and 0<=v[4]<=1,'Finite actual mesh/UV '+key)
 def bounds(mesh):
  p=[v for face in mesh for v in face];return [[min(v[i] for v in p)/16,max(v[i] for v in p)/16] for i in range(3)]
 b=bounds(data['idle']);old=bounds(before['idle']);extent=[hi-lo for lo,hi in b]
 check(extent[0]<=2.2 and extent[1]<=3.5,'Width/height stay within unchanged hitbox')
 # Long-axis mismatch pre-exists; report explicitly rather than silently changing collision dimensions.
 depth_fits=extent[2]<=2.2
 for path in ['src/main/java/org/slavicmyths/wolf/WolfTimeline.java','src/main/java/org/slavicmyths/wolf/GreyWolf.java','src/main/java/org/slavicmyths/registry/ModEntities.java','src/main/resources/data/slavicmyths/wolf_story/quests.json']:
  baseline=subprocess.check_output(['git','show',BASE+':'+path],cwd=R);check((R/path).read_bytes().replace(b'\r\n',b'\n')==baseline.replace(b'\r\n',b'\n'),'Preserved '+path)
 model=(R/'src/main/java/org/slavicmyths/client/GreyWolfModel.java').read_text(encoding='utf-8');check('new WeakHashMap<>()' in model and 'blend(e,age)' in model,'Existing per-entity state/move blending preserved');check('getTarget()' not in model,'No client target inference')
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production JAR CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    key=p.relative_to(R/'src/main/resources').as_posix()
    if key!='META-INF/neoforge.mods.toml':check(z.read(key)==p.read_bytes(),'Production parity '+key)
  check(not any('org/slavicmyths/verify/' in n for n in z.namelist()),'Offline fixtures excluded')
 out=R/'docs/implementation/grey-wolf-54';report=dict(status='IN_PROGRESS',checks=len(checks),native_pose_count=15,parts=30,cubes=69,before_bounds_blocks=old,after_bounds_blocks=b,extents_blocks=extent,unchanged_hitbox=[2.2,3.5,2.2],strict_long_axis_fit=depth_fits,known_limit='Long silhouette extends beyond the legacy square collision footprint. Hitbox unchanged; owner decision/runtime acceptance pending.',jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),runtime_launches=0,in_game_visual='NOT_RUN',owner_accepted=False)
 (out/'gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
