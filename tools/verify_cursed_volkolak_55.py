"""Offline source/asset/JAR gates for #55. No game launch, no owner acceptance."""
from pathlib import Path
from PIL import Image
import sys,json,gzip,hashlib,subprocess,zipfile
R=Path(__file__).resolve().parents[1];BASE='957b1055';checks=[]
def check(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def main():
 prefix='src/main/'
 expected={prefix+'java/org/slavicmyths/client/'+n+'.java' for n in ['CursedVolkolakModel','CursedVolkolakGeometry','CursedVolkolakEyes','CursedVolkolakRenderer']}
 expected|={prefix+'resources/assets/slavicmyths/'+n for n in ['cursed_volkolak_geometry.json','textures/entity/cursed_volkolak.png','textures/entity/cursed_volkolak_eyes.png']}
 changed=set(subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=R,text=True).splitlines());check(changed==expected,'Only Cursed Volkolak presentation resources changed')
 resource=[p for p in expected if not any(n in p for n in ['Model.java','Eyes.java','Renderer.java'])]
 snap={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in resource}
 subprocess.run([sys.executable,str(R/'tools/volkolak_143_resources.py'),'--model-only'],cwd=R,check=True,capture_output=True)
 for p,h in snap.items():check(hashlib.sha256((R/p).read_bytes()).hexdigest()==h,'Deterministic model-only generation '+p)
 for suffix in ['', '_eyes']:
  im=Image.open(R/('src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak'+suffix+'.png'));im.load();check(im.size==(512,512),'Authored atlas dimensions '+suffix)
 after=json.loads(gzip.decompress((R/'build/cursed-55/after-native.json.gz').read_bytes()));before=json.loads(gzip.decompress((R/'docs/implementation/cursed-volkolak-55/before-native.json.gz').read_bytes()));check(set(after)==set(before) and len(after)==37,'Same native 37 pose samples before/after')
 def bounds(faces,depth):
  vertices=[v for f in faces for v in f];return [[min(v[i] for v in vertices)*(depth if i==2 else 1)/16,max(v[i] for v in vertices)*(depth if i==2 else 1)/16] for i in range(3)]
 b={k:bounds(after[k],.68) for k in ['quad','upright','escape_19','escape_49','escape_87']}
 for stance,height in [('quad',1.9),('upright',2.8)]:
  bb=b[stance];check(bb[0][0]>=-.825 and bb[0][1]<=.825 and bb[2][0]>=-.825 and bb[2][1]<=.825,'Unchanged 1.65 square footprint '+stance)
  check(bb[1][0]>=1.5-height and bb[1][1]<=1.502,'Unchanged physical stance height and ground '+stance)
 for k,faces in after.items():
  for face in faces:
   for v in face:check(len(v)==5 and all(-1e6<x<1e6 for x in v) and 0<=v[3]<=1 and 0<=v[4]<=1,'Native finite UV '+k)
 for path in ['src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java','src/main/java/org/slavicmyths/registry/ModEntities.java','src/main/java/org/slavicmyths/wolf/GreyWolf.java']:
  old=subprocess.check_output(['git','show',BASE+':'+path],cwd=R);check((R/path).read_bytes().replace(b'\r\n',b'\n')==old.replace(b'\r\n',b'\n'),'Preserved gameplay '+path)
 model=(R/'src/main/java/org/slavicmyths/client/CursedVolkolakModel.java').read_text(encoding='utf-8');check('getTarget()' not in model,'Action comes from synced move, no client target inference');check('new WeakHashMap<>()' in model and 'blend(e,age)' in model,'Per-entity blend retained')
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production JAR CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    key=p.relative_to(R/'src/main/resources').as_posix()
    if key!='META-INF/neoforge.mods.toml':check(z.read(key)==p.read_bytes(),'Production parity '+key)
  check(not any('org/slavicmyths/verify/' in n for n in z.namelist()),'Offline fixtures excluded from production')
 report=dict(status='IN_PROGRESS',checks=len(checks),native_poses=37,parts=92,before_bounds_blocks={k:bounds(before[k],.85) for k in ['quad','upright','escape_19']},after_bounds_blocks=b,renderer_depth_before=.85,renderer_depth_after=.68,hitbox_quad=[1.65,1.9],hitbox_upright=[1.65,2.8],jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),runtime_launches=0,visual_acceptance='NOT_RUN',owner_accepted=False,audio_acceptance='NOT_RUN',legacy_procedural_foley='FAIL: retained unchanged; recorded replacements required')
 (R/'docs/implementation/cursed-volkolak-55/gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
