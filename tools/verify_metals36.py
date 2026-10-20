"""Vanilla topology/material-only regression and production JAR check; no client."""
from pathlib import Path
import json,hashlib,ast,tempfile,zipfile,tomllib,struct
from PIL import Image
import metals36_assets as art
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/metals-vanilla-36'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 base=json.loads((O/'baseline-source-sha256.json').read_text(encoding='utf-8'))
 expected={f'src/main/resources/assets/slavicmyths/textures/{s}.png' for s in art.OWNED}|{'gradle.properties'}
 assert {n for n,h in base.items() if sha(R/n)!=h}==expected
 rows=json.loads((O/'inventory.json').read_text(encoding='utf-8'))
 for row in rows:
  name=row['id'];p=R/row['texture'];im=Image.open(p).convert('RGBA');assert list(im.size)==row['size']
  digest=sha(p);assert art.write_owned(p) and sha(p)==digest
  scale=2 if name in art.NATIVE32 else 1
  ref=Image.open(O/'reference'/('ingot.png' if row['folder']=='item' else 'ore.png')).convert('RGBA')
  for y in range(16):
   for x in range(16):
    color=im.getpixel((x*scale,y*scale));original=ref.getpixel((x,y))
    assert color[3]==original[3]
    for yy in range(scale):
     for xx in range(scale):assert im.getpixel((x*scale+xx,y*scale+yy))==color
    if row['folder']=='block' and original[0]==original[1]==original[2]:assert color==original
  model=R/f'src/main/resources/assets/slavicmyths/models/{row["folder"]}/{name}.json'
  assert sha(model)==base[model.relative_to(R).as_posix()]
  meta=json.loads(model.read_text(encoding='utf-8'))
  if row['folder']=='item':assert meta['textures']['layer0']=='slavicmyths:item/'+name
  else:
   assert meta=={'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/'+name}}
   item=R/f'src/main/resources/assets/slavicmyths/models/item/{name}.json';assert json.loads(item.read_text(encoding='utf-8'))['parent']=='slavicmyths:block/'+name
  row.update(new_sha256=sha(p),model=model.relative_to(R).as_posix(),topology='iron_ingot' if row['folder']=='item' else 'iron_ore',runtime='NOT_RUN / OWNER_PENDING')
 (O/'inventory.json').write_text(json.dumps(rows,indent=2)+'\n',encoding='utf-8')
 import materials81_m1 as legacy
 for name in ['silver_ingot','perunite']:assert legacy.render_asset(name).tobytes()==art.image(name).tobytes()
 for name in ['materials81_m1.py','generate_045.py','industry_resources.py','unique_armor_resources.py','polish_046.py']:ast.parse((R/'tools'/name).read_text(encoding='utf-8'))
 with tempfile.TemporaryDirectory(prefix='metals36-',dir=R/'.tools') as tmp:
  for n in art.OWNED:
   p=Path(tmp)/'assets/slavicmyths/textures'/f'{n}.png';assert legacy.write_owned(p);assert Image.open(p).tobytes()==art.image(p.stem).tobytes()
  for script,fn,args,scope in [('industry_resources.py','png',None,{}),('polish_046.py','tex',None,{'res':Path(tmp)/'assets/slavicmyths'}),('unique_armor_resources.py','icon',None,{})]:
   tree=ast.parse((R/'tools'/script).read_text(encoding='utf-8'));node=next(n for n in ast.walk(tree) if isinstance(n,ast.FunctionDef) and n.name==fn);exec(compile(ast.Module(body=[node],type_ignores=[]),script,'exec'),scope)
   if fn=='png':
    p=Path(tmp)/'assets/slavicmyths/textures/item/purified_iron_ingot.png';scope[fn](p,None);assert Image.open(p).tobytes()==art.image(p.stem).tobytes()
   elif fn=='tex':
    scope[fn]('silver_ore',None);p=Path(tmp)/'assets/slavicmyths/textures/block/silver_ore.png';assert Image.open(p).tobytes()==art.image(p.stem).tobytes()
   else:assert scope[fn]('hardened_iron').tobytes()==art.image('hardened_iron').tobytes()
 assert not art.write_owned(R/'.tools/assets/minecraft/textures/item/iron_ingot.png')
 jar=R/'build/libs/slavicmyths-1.9.7.36.jar';res=R/'build/resources/main';classes=R/'build/classes/java/main';count=png=java=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None;meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.36' for m in meta['mods'])
  for p in res.rglob('*'):
   if not p.is_file():continue
   assert z.read(p.relative_to(res).as_posix())==p.read_bytes();count+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    png+=1
  for p in classes.rglob('*.class'):
   b=p.read_bytes();assert struct.unpack('>H',b[6:8])[0]==65;assert z.read(p.relative_to(classes).as_posix())==b;java+=1
  assert not any(any(s in n for s in ['Creature79Checks','Bogatyr78Checks','/smoke/']) for n in z.namelist())
 manifest={p.relative_to(R).as_posix():sha(p) for root in ['src/main/java','src/main/resources'] for p in (R/root).rglob('*') if p.is_file()}
 for n in ['gradle.properties','build.gradle','settings.gradle']:manifest[n]=sha(R/n)
 (O/'build-source-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
 result=dict(status='PASS_OFFLINE_ONLY',version='1.9.7.36',jar=str(jar),sha256=sha(jar),resources=count,png_verified=png,java21_classes=java,ingot_topology_exact=True,ore_stone_pixels_exact=True,all_other_production_unchanged=True,runtime_launches=0,source_fingerprint=hashlib.sha256(json.dumps(manifest,sort_keys=True).encode()).hexdigest())
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result))
if __name__=='__main__':main()
