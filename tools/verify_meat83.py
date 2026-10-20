"""Offline production gate for the five meat assets. Does not launch Minecraft."""
from pathlib import Path
import hashlib,json,zipfile,tomllib,struct
from PIL import Image
import meat83_assets as art
R=Path(__file__).resolve().parents[1]
O=R/'docs/implementation/meat-83'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 baseline=json.loads((O/'baseline-source-sha256.json').read_text(encoding='utf-8'))
 paths=[f'src/main/resources/assets/slavicmyths/textures/item/{id}.png' for id in art.OWNED]
 assert {n for n,h in baseline.items() if sha(R/n)!=h}==set(paths)|{'gradle.properties'}
 for id in art.OWNED:
  model=R/f'src/main/resources/assets/slavicmyths/models/item/{id}.json'
  meta=json.loads(model.read_text(encoding='utf-8'));assert meta['parent'] in ('item/generated','minecraft:item/generated') and meta['textures']=={'layer0':'slavicmyths:item/'+id}
  p=R/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png';old=sha(p)
  assert art.write_owned(p) and sha(p)==old
  assert Image.open(p).size==Image.open(O/'before'/p.relative_to(R)).size
  assert {c[3] for c in Image.open(p).convert('RGBA').getdata()}=={0,255}
 inventory=json.loads((O/'inventory.json').read_text(encoding='utf-8'))
 for row in inventory:
  p=R/row['texture'];m=R/row['model'];id=row['id']
  meta=json.loads(m.read_text(encoding='utf-8'));assert meta['parent'] in ('item/generated','minecraft:item/generated') and meta['textures']=={'layer0':'slavicmyths:item/'+id}
  assert sha(m)==row['model_sha256'];assert list(Image.open(p).size)==row['size']
  if id not in art.OWNED:assert sha(p)==row['old_sha256']
  if id in ['raw_boar_meat','raw_venison','raw_bear_meat']:assert Image.open(p).getchannel('A').tobytes()==Image.open(O/'before'/row['texture']).getchannel('A').tobytes()
  row.update(status='CHANGED' if id in art.OWNED else 'UNCHANGED',new_sha256=sha(p),runtime='NOT_RUN / OWNER_PENDING')
 (O/'inventory.json').write_text(json.dumps(inventory,indent=2)+'\n',encoding='utf-8')
 import ast,tempfile
 for name,fn,id in [('generate_065.py','png','raw_venison'),('husbandry_resources.py','save','raw_duck')]:
  tree=ast.parse((R/'tools'/name).read_text(encoding='utf-8'));node=next(x for x in tree.body if isinstance(x,ast.FunctionDef) and x.name==fn)
  with tempfile.TemporaryDirectory(prefix='meat83-',dir=R/'.tools') as tmp:
   path=Path(tmp)/('assets/slavicmyths/textures/item/'+id+'.png');scope={}
   exec(compile(ast.Module(body=[node],type_ignores=[]),name,'exec'),scope)
   scope[fn](path,None)
   assert Image.open(path).tobytes()==art.image(id).tobytes()
 assert not art.write_owned(R/'.tools/assets/minecraft/textures/item/raw_duck.png')
 jar=R/'build/libs/slavicmyths-1.9.7.35.jar';resources=R/'build/resources/main';classes=R/'build/classes/java/main'
 n=png=java=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
  assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.35' for m in meta['mods'])
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
 result=dict(status='PASS_OFFLINE_ONLY',jar=str(jar),sha256=sha(jar),version='1.9.7.35',resources=n,png_verified=png,java21_classes=java,source_base='f48e42c2c4ec06dc81f7a241c3250a47b520031b',source_commit='UNCOMMITTED working tree, no automatic commit',source_fingerprint=hashlib.sha256(json.dumps(manifest,sort_keys=True).encode()).hexdigest(),changed_production=paths+['gradle.properties'],runtime_launches=0,owner_acceptance='PENDING',polymc='Installation pending after gates')
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result))
if __name__=='__main__':main()
