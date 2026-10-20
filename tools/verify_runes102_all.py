"""Offline production gate for the complete craft rune family. Does not launch Minecraft."""
from pathlib import Path
import hashlib,json,zipfile,tomllib,struct,subprocess
from PIL import Image
import runes102_all as art
R=Path(__file__).resolve().parents[1]
O=R/'docs/implementation/runes-102-full'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 baseline=json.loads((O/'baseline-source-sha256.json').read_text(encoding='utf-8'))
 paths=[f'src/main/resources/assets/slavicmyths/textures/item/{id}.png' for id in art.IDS]
 assert {n for n,h in baseline.items() if sha(R/n)!=h}==set(paths)|{'gradle.properties'}
 for row in art.SPEC:
  id=row['id'];p=R/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png'
  with Image.open(p) as im:
   assert im.size==(row['size'],row['size'])
   assert set(im.convert('RGBA').getchannel('A').getdata())<={0,255}
   assert im.convert('RGBA').tobytes()==art.image(id).tobytes()
  for master in art.master_paths(id):assert p.read_bytes()==(R/master).read_bytes()
  model=json.loads((R/f'src/main/resources/assets/slavicmyths/models/item/{id}.json').read_text())
  assert model['textures']['layer0']==f'slavicmyths:item/{id}'
 jar=R/'build/libs/slavicmyths-1.9.7.38.jar';resources=R/'build/resources/main';classes=R/'build/classes/java/main'
 n=png=java=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
  assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.38' for m in meta['mods'])
  for p in resources.rglob('*'):
   if not p.is_file():continue
   assert z.read(p.relative_to(resources).as_posix())==p.read_bytes();n+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    png+=1
  for p in classes.rglob('*.class'):
   b=p.read_bytes();assert struct.unpack('>H',b[6:8])[0]==65
   assert z.read(p.relative_to(classes).as_posix())==b;java+=1
  assert not any(any(k in name for k in ['Creature79Checks','Bogatyr78Checks','SkimModelBefore78','/smoke/']) for name in z.namelist())
  for name in paths:assert z.read(name.removeprefix('src/main/resources/'))==(R/name).read_bytes()
 manifest={p.relative_to(R).as_posix():sha(p) for root in ['src/main/java','src/main/resources'] for p in (R/root).rglob('*') if p.is_file()}
 for name in ['gradle.properties','build.gradle','settings.gradle']:manifest[name]=sha(R/name)
 (O/'build-source-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
 result=dict(status='PASS_OFFLINE_ONLY',jar=str(jar),sha256=sha(jar),version='1.9.7.38',resources=n,png_verified=png,java21_classes=java,source_base=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),source_commit='UNCOMMITTED working tree, no automatic commit',source_fingerprint=hashlib.sha256(json.dumps(manifest,sort_keys=True).encode()).hexdigest(),changed_production=paths+['gradle.properties'],runtime_launches=0,owner_acceptance='PENDING',polymc='1.9.7.36 unchanged; 38 NOT_INSTALLED')
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result))
if __name__=='__main__':main()
