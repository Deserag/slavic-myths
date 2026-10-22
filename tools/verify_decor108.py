"""Production resource, boundary/UV, reproducibility and scope checks. No game launch."""
from pathlib import Path
from io import BytesIO
import ast,hashlib,json,tempfile,zipfile
from PIL import Image
from decor108_assets import ROOT,A,OUT,MASTER,apply,outputs,boundary,box
from decor108_review import overlaps,ReviewSources

sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 checks=[]
 def check(ok,label):
  checks.append({'requirement':label,'status':'PASS' if ok else 'FAIL'});assert ok,label
 # Real constructive geometry fixtures: shared join, partial overlay, duplicate.
 fixtures=[[box((0,0,0),(8,8,8),'a'),box((8,0,0),(16,8,8),'b')],[box((0,0,0),(16,8,16),'a'),box((4,0,4),(12,8,12),'b')],[box((0,0,0),(16,16,16),'a')]*2]
 for es,expected_area in zip(fixtures,[640,1024,1536]):
  result=boundary(es);check(not overlaps({'elements':result}),'boundary fixture has no exposed coplanar duplicates')
  check(sum(len(e['faces']) for e in result)>0,'boundary fixture retains outer surface')
  from decor108_assets import FACE
  area=sum((e['to'][FACE[f][2]]-e['from'][FACE[f][2]])*(e['to'][FACE[f][3]]-e['from'][FACE[f][3]]) for e in result for f in e['faces'])
  check(area==expected_area,'boundary fixture exact analytical union surface area')
 with tempfile.TemporaryDirectory() as td:
  dest=Path(td)/'resources/assets/slavicmyths';apply(dest)
  for p in Path(td).rglob('*'):
   if p.is_file():
    rel=p.relative_to(Path(td)/'resources');check(p.read_bytes()==(ROOT/'src/main/resources'/rel).read_bytes(),'canonical regeneration '+rel.as_posix())
 for rel,m in outputs().items():
  if 'elements' not in m:continue
  check(not overlaps(m),'no exposed coincident quads '+rel)
  for e in m['elements']:
   check(all(-16<=v<=32 for v in e['from']+e['to']),'legal model bounds '+rel)
   for f in e['faces'].values():check(all(0<=v<=16 for v in f.get('uv',[])),'UV remains inside its atlas sprite '+rel)
 dummy=json.loads((A/'blockstates/training_dummy.json').read_text());check(len(dummy['variants'])==8,'dummy4 directions x2 halves')
 loot=json.loads((ROOT/'src/main/resources/data/slavicmyths/loot_table/blocks/training_dummy.json').read_text());check(any(c.get('properties')=={'half':'lower'} for c in loot['pools'][0]['conditions']),'one dummy item: lower half loot only')
 # Execute only final overlay AST calls in a temporary tree, never old pipelines.
 hooks=['create_resources','furniture_081','storage_resources','kitchen_resources','brewing_resources','generate_050','husbandry_resources','sanctuary_assets','textile_resources','burial_084','rc2_visual_resources','integration_polish_resources']
 with tempfile.TemporaryDirectory() as td:
  for name in hooks:
   tree=ast.parse((ROOT/'tools'/f'{name}.py').read_text(encoding='utf8'))
   calls=[n for n in ast.walk(tree) if isinstance(n,ast.Expr) and isinstance(n.value,ast.Call) and isinstance(n.value.func,ast.Name) and n.value.func.id=='apply_decor108']
   check(len(calls)==1,'canonical final overlay present '+name)
   exec(compile(ast.Module(body=calls,type_ignores=[]),'isolated-hook','exec'),{'apply_decor108':lambda:apply(Path(td)/'resources/assets/slavicmyths')})
 baseline=json.loads((OUT/'baseline.json').read_text());changed=[p for p,h in baseline.items() if not (ROOT/p).is_file() or sha(ROOT/p)!=h]
 allowed={'src/main/resources/assets/slavicmyths/'+p for p in outputs()}|{'src/main/resources/assets/slavicmyths/textures/block/pine_log.png','src/main/resources/data/slavicmyths/loot_table/blocks/training_dummy.json','src/main/java/org/slavicmyths/furniture/Furniture.java','src/main/java/org/slavicmyths/furniture/FurnitureBlock.java','tools/decor_all_woods.py'}|{'tools/'+n+'.py' for n in hooks}
 check(set(changed)<=allowed,'all existing unrelated source/tools byte-exact')
 (OUT/'changed-existing.json').write_text(json.dumps(changed,indent=2)+'\n')
 jar=ROOT/'build/libs/slavicmyths-1.9.7.50.jar';resources=jsons=pngs=0
 with zipfile.ZipFile(jar) as z:
  for p in (ROOT/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   rel=p.relative_to(ROOT/'src/main/resources').as_posix();expected=ROOT/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else p
   check(z.read(rel)==expected.read_bytes(),'production JAR parity '+rel);resources+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'));jsons+=1
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    pngs+=1
  check('org/slavicmyths/furniture/CrateTile.class' in z.namelist(),'real crate class packaged')
  check('org/slavicmyths/furniture/TrainingDummyBlock.class' in z.namelist(),'real two-block dummy class packaged')
  check(not any(n.startswith(('tools/','org/slavicmyths/verify/')) for n in z.namelist()),'no offline tools in JAR')
 result={'status':'IN_GAME_OWNER_PENDING','checks':checks,'resources':resources,'json':jsons,'png':pngs,'canonical_models':len(outputs()),'template_sha256':sha(MASTER),'jar':{'path':str(jar),'sha256':sha(jar),'bytes':jar.stat().st_size},'runtime_launches':0,'polymc':'NOT_INSTALLED_THIS_CANDIDATE','base_commit':'32d1733457e7e90360ce16c23860304ece664c64'}
 (OUT/'verification.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps({k:v for k,v in result.items() if k!='checks'},indent=2))

if __name__=='__main__':main()
