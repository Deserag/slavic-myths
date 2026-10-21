from pathlib import Path
import json,hashlib,zipfile,tomllib,struct
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor-health-48'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 before=json.loads((O/'source-before.json').read_text());after={p.as_posix():sha(R/p) for p in map(Path,before)}
 assert before==after,'Source changed during build'
 assert 'BUILD SUCCESSFUL' in (O/'build.log').read_text(encoding='utf-8-sig')
 jar=R/'build/libs/slavicmyths-1.9.7.48.jar';n=png=classes=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.48' for m in meta['mods'])
  for p in (R/'build/resources/main').rglob('*'):
   if not p.is_file():continue
   assert z.read(p.relative_to(R/'build/resources/main').as_posix())==p.read_bytes();n+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    png+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   data=p.read_bytes();assert struct.unpack('>H',data[6:8])[0]==65;assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==data;classes+=1
  assert not any('/smoke/' in s or 'RuneGlow104Checks' in s or 'RuneGlow104Meshes' in s or 'RuneHealthProbe' in s or 'Armor88FullMeshes' in s for s in z.namelist())
  for s in before:
   if s.startswith('src/main/resources/') and s!='src/main/resources/META-INF/neoforge.mods.toml':
    assert z.read(s[len('src/main/resources/'):])==(R/s).read_bytes(),s
 result=dict(status='PASS',jar=str(jar),sha256=sha(jar),resources=n,png=png,java21_classes=classes,source_stable=True,source_fingerprint=hashlib.sha256(json.dumps(before,sort_keys=True).encode()).hexdigest())
 (O/'jar-check.json').write_text(json.dumps(result,indent=2));print(json.dumps(result))
if __name__=='__main__':main()
