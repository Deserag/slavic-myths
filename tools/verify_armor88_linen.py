"""Bounded #88 source/mesh/production JAR gates. No runtime claims."""
from pathlib import Path
import json,hashlib,zipfile,tomllib,struct,subprocess
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor88-linen'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 base=json.loads((O/'baseline-source-sha256.json').read_text());current={p.relative_to(R).as_posix() for root in ['src/main/java','src/main/resources'] for p in (R/root).rglob('*') if p.is_file()}|{'gradle.properties'}
 assert current==set(base)
 changed=sorted(n for n,h in base.items() if sha(R/n)!=h);assert set(changed)=={'gradle.properties','src/main/java/org/slavicmyths/client/TextileClothingLayer.java'}
 a=json.loads((O/'before-native.json').read_text());b=json.loads((O/'after-native.json').read_text());assert a['controls']==b['controls'] and len(b['controls'])==12
 assert len(a['samples'])==len(b['samples'])==6
 for x,y in zip(a['samples'],b['samples']):assert x['layers'][0]==y['layers'][0] and x['right_hand_matrix']==y['right_hand_matrix']
 for row in b['bounds']:
  z=row['bounds'][5]
  expected=(2.32 if row['part']=='body' else 2.34) if row['id']=='linen_shirt' else (2.44 if row['part']=='body' else (2.38 if row['segment']==0 else 2.32))
  assert abs(z-expected)<1e-5
 for row in a['bounds']:
  if row['id']=='linen_ports' and row['part'].endswith('leg') and row['segment']==1:assert abs(row['bounds'][5]-1.92)<1e-5
 log=(R/'.tools/armor88-build.log').read_text(encoding='utf-8-sig');assert 'BUILD SUCCESSFUL' in log and 'ARMOR88_NATIVE_PASS' in log and 'RUNE104_PLAIN_JVM_PASS' in log
 jar=R/'build/libs/slavicmyths-1.9.7.43.jar';nr=np=nc=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['version']=='1.9.7.43' for m in meta['mods'])
  for p in (R/'build/resources/main').rglob('*'):
   if not p.is_file():continue
   assert z.read(p.relative_to(R/'build/resources/main').as_posix())==p.read_bytes();nr+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    np+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   data=p.read_bytes();assert struct.unpack('>H',data[6:8])[0]==65 and z.read(p.relative_to(R/'build/classes/java/main').as_posix())==data;nc+=1
  assert not any(any(x in n for x in ['Armor88LinenMeshes','RuneGlow104Checks','/smoke/']) for n in z.namelist())
 result=dict(version='1.9.7.43',status='PASS_SOURCE_AND_BUILD_ONLY',modified=changed,jar=str(jar),sha256=sha(jar),resources=nr,png=np,java21_classes=nc,unchanged_control_meshes=12,source_view_pairs=24,source_base_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),source_commit='UNCOMMITTED',minecraft_launches=0,runtime='NOT_RUN',owner_acceptance='PENDING',polymc='43 NOT_INSTALLED; installed42 unchanged')
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
 (O/'build-source-sha256.json').write_text(json.dumps({n:sha(R/n) for n in sorted(current)},indent=2)+'\n')
if __name__=='__main__':main()
