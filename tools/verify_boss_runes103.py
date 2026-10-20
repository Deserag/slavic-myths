"""Offline production gate for the boss reward rune art plus unchanged craft family. Does not launch Minecraft."""
from pathlib import Path
import hashlib,json,zipfile,tomllib,struct,subprocess
from PIL import Image
import boss_runes103 as art
import runes102_all as craft
R=Path(__file__).resolve().parents[1]
O=R/'docs/implementation/boss-runes-103'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 baseline=json.loads((O/'baseline-source-sha256.json').read_text(encoding='utf-8'))
 paths=[f'src/main/resources/assets/slavicmyths/textures/item/rune_{id}.png' for id in art.IDS]
 current={p.relative_to(R).as_posix() for root in ['src/main/java','src/main/resources'] for p in (R/root).rglob('*') if p.is_file()}|{'gradle.properties'}
 assert current==set(baseline),'Unexpected added/deleted production file'
 assert {n for n,h in baseline.items() if sha(R/n)!=h}==set(paths)|{'gradle.properties'}
 inventory=json.loads((O/'inventory.json').read_text(encoding='utf-8'))
 masks=set();slot_masks=set()
 for row in inventory:
  p=R/row['textures'];id=row['id'];before=O/'before'/row['textures']
  with Image.open(p) as im:
   assert list(im.size)==row['size']
   assert set(im.convert('RGBA').getchannel('A').getdata())<={0,255}
   if row['decision']=='redesign':
    assert im.convert('RGBA').tobytes()==art.image(id.removeprefix('rune_')).tobytes()
    assert sha(p)!=sha(before)
    masks.add(im.convert('RGBA').getchannel('A').resize((32,32),Image.Resampling.NEAREST).tobytes())
    slot_masks.add(im.convert('RGBA').getchannel('A').resize((16,16),Image.Resampling.NEAREST).tobytes())
   else:assert p.read_bytes()==before.read_bytes()
  model=json.loads((R/row['models']).read_text())
  assert model['textures']['layer0']==f'slavicmyths:item/{id}'
  assert model['parent']=='minecraft:item/generated'
  assert sha(R/row['models'])==row['models_sha256']
 assert len(masks)==10 and len(slot_masks)==10,'Duplicated silhouette'
 for id in craft.IDS:
  p=R/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png'
  assert Image.open(p).convert('RGBA').tobytes()==craft.image(id).tobytes()
  for master in craft.master_paths(id):assert p.read_bytes()==(R/master).read_bytes()
 assert not art.write_owned(R/'src/main/resources/assets/minecraft/textures/item/rune_stomp.png')
 assert not art.write_owned(R/'src/main/resources/assets/slavicmyths/textures/item/rune_of_waning.png')
 jar=R/'build/libs/slavicmyths-1.9.7.39.jar';resources=R/'build/resources/main';classes=R/'build/classes/java/main'
 n=png=java=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
  assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.39' for m in meta['mods'])
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
 result=dict(status='PASS_OFFLINE_ONLY',jar=str(jar),sha256=sha(jar),version='1.9.7.39',resources=n,png_verified=png,java21_classes=java,source_base=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),source_commit='UNCOMMITTED working tree, no automatic commit',source_fingerprint=hashlib.sha256(json.dumps(manifest,sort_keys=True).encode()).hexdigest(),changed_production=paths+['gradle.properties'],runtime_launches=0,owner_acceptance='PENDING',polymc=json.loads((O/'installed.json').read_text(encoding='utf-8'))['status'] if (O/'installed.json').exists() else '39 NOT_INSTALLED')
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result))
if __name__=='__main__':main()
