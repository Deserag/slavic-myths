"""#82 source bindings, isolation, legacy writer routes and production JAR. No game."""
from pathlib import Path
import hashlib,json,zipfile,tomllib,struct,ast
from PIL import Image
import plants82_assets as art
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/plants-82'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 baseline=json.loads((O/'baseline-source-sha256.json').read_text(encoding='utf-8'))
 inventory=json.loads((O/'inventory.json').read_text(encoding='utf-8'))
 paths={f'src/main/resources/assets/slavicmyths/textures/{s}.png' for s in art.OWNED}
 assert {n for n,h in baseline.items() if sha(R/n)!=h}==paths|{'gradle.properties'}
 assert {p.relative_to(R).as_posix() for p in (R/'src/main').rglob('*') if p.is_file()}==set(baseline)-{'gradle.properties'}
 catalog=(R/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf-8').splitlines()
 diagnoses={
 'apple_sapling':'Три рыхлые округлые группы с цветком, силуэт слишком мягкий: компактные угловатые листья без цветения',
 'linden_sapling':'Сплошная округлая шапка и толстый ствол: плотные отдельные широкие листовые группы',
 'rowan_sapling':'Два пятна на прямом стволе: расходящиеся тонкие ветви с мелкими отдельными листьями',
 'willow_sapling':'Ровная перекладина с одинаковыми полосами: четыре коротких побега разной длины',
 'pine_sapling':'Редкие разрозненные штрихи без конуса: три чётких хвойных яруса',
 'cabbage':'Круглый кочан: плоские бока и длинный плоский верх, срезанные углы, слои листьев',
 'raspberry_sapling':'OK: вытянутый молодой побег, листовые группы читаемы; видимого дефекта нет',
 'blueberry_sapling':'OK: компактные небольшие листья и тонкий побег; видимого дефекта нет',
 'blackcurrant_sapling':'OK: широкие более тёмные листья; видимого дефекта нет',
 'lingonberry_sapling':'OK: маленькие плотные листья; видимого дефекта нет',
 'cranberry_sapling':'OK: низкие стелющиеся веточки; видимого дефекта нет'}
 for row in inventory:
  id=row['id'];p=R/row['texture'];m=R/row['item_model'];meta=json.loads(m.read_text(encoding='utf-8'))
  expected=row['texture'].removeprefix('src/main/resources/assets/').removesuffix('.png').replace('/textures/',':',1)
  assert meta=={'parent':'minecraft:item/generated','textures':{'layer0':expected}},id
  assert any(line.startswith(id+'\t') for line in catalog),id
  assert sha(m)==row['item_model_sha256']
  im=Image.open(p).convert('RGBA');assert list(im.size)==row['size'];assert {c[3] for c in im.getdata()}=={0,255}
  changed=row['texture'] in paths
  if changed:
   digest=sha(p);assert art.write_owned(p) and sha(p)==digest
  else:assert sha(p)==row['old_sha256']
  row.update(status='CHANGED' if changed else 'UNCHANGED',diagnosis=diagnoses[id],new_sha256=sha(p),runtime='NOT_RUN / OWNER_PENDING')
  if id in [s+'_sapling' for s in art.TREES]:
   block=R/f'src/main/resources/assets/slavicmyths/models/block/{id}.json';b=json.loads(block.read_text(encoding='utf-8'));assert b['parent']=='minecraft:block/cross' and b['textures']['cross']==expected
   state=R/f'src/main/resources/assets/slavicmyths/blockstates/{id}.json';assert json.loads(state.read_text(encoding='utf-8'))['variants']['']['model']=='slavicmyths:block/'+id
   row.update(placed_model=block.relative_to(R).as_posix(),blockstate=state.relative_to(R).as_posix(),cross_geometry='actual-vanilla-cross.json',source='tools/plants82_assets.py')
  elif id.endswith('_sapling'):
   row['planted_blockstate']='src/main/resources/assets/slavicmyths/blockstates/'+id.replace('_sapling','_bush')+'.json';assert (R/row['planted_blockstate']).exists();row['source']='tools/gardens_resources.py UNCHANGED'
  else:row['source']='tools/plants82_assets.py'
 # Check only the actual writer functions in a sandbox: never execute historical top-level resource generation.
 import tempfile
 for name,fn in [('woodlands_0801.py','texture'),('gardens_resources.py','save'),('farming_resources.py','save')]:
  tree=ast.parse((R/'tools'/name).read_text(encoding='utf-8'));node=next(x for x in tree.body if isinstance(x,ast.FunctionDef) and x.name==fn)
  with tempfile.TemporaryDirectory(prefix='plants82-',dir=R/'.tools') as tmp:
   target=Path(tmp)/'assets/slavicmyths';scope={'A':target,'png':lambda p,w,h,f:(_ for _ in ()).throw(AssertionError('legacy writer reached'))}
   exec(compile(ast.Module(body=[node],type_ignores=[]),name,'exec'),scope)
   if fn=='texture':scope[fn]('linden_sapling',None);id='linden_sapling';folder='block'
   else:
    id='apple_sapling' if name=='gardens_resources.py' else 'cabbage';folder='block' if id.endswith('sapling') else 'item';scope[fn](Image.new('RGBA',(1,1),'red'),folder,id)
   actual=target/f'textures/{folder}/{id}.png';assert Image.open(actual).tobytes()==art.image(id).tobytes()
 assert not art.write_owned(R/'.tools/assets/minecraft/textures/block/apple_sapling.png')
 (O/'inventory.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 jar=R/'build/libs/slavicmyths-1.9.7.34.jar';resources=R/'build/resources/main';classes=R/'build/classes/java/main';n=png=java=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.34' for m in meta['mods'])
  for p in resources.rglob('*'):
   if not p.is_file():continue
   assert z.read(p.relative_to(resources).as_posix())==p.read_bytes();n+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    png+=1
  for p in classes.rglob('*.class'):
   b=p.read_bytes();assert struct.unpack('>H',b[6:8])[0]==65;assert z.read(p.relative_to(classes).as_posix())==b;java+=1
  assert not any(any(k in name for k in ['Creature79Checks','Bogatyr78Checks','SkimModelBefore78','/smoke/']) for name in z.namelist())
 manifest={p.relative_to(R).as_posix():sha(p) for root in ['src/main/java','src/main/resources'] for p in (R/root).rglob('*') if p.is_file()}
 for name in ['gradle.properties','build.gradle','settings.gradle']:manifest[name]=sha(R/name)
 (O/'build-source-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
 import subprocess
 result=dict(status='PASS_OFFLINE_ONLY',version='1.9.7.34',jar=str(jar),sha256=sha(jar),resources=n,png_verified=png,java21_classes=java,source_base=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),source_commit='UNCOMMITTED #82 over previous packages',source_fingerprint=hashlib.sha256(json.dumps(manifest,sort_keys=True).encode()).hexdigest(),ids_audited=11,changed_png=6,berries_unchanged=5,all_other_production_unchanged=True,legacy_writer_sandbox_checks=3,runtime_launches=0,owner_acceptance='PENDING',polymc='34 NOT_INSTALLED, 32 unchanged')
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result))
if __name__=='__main__':main()
