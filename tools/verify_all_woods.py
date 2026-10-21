"""Production, compatibility, UV dependencies and reproducibility for all six woods."""
from pathlib import Path
from io import BytesIO
from functools import partial
import ast,json,hashlib,tempfile,zipfile
from PIL import Image
from decor_all_woods import ROOT,TEXTURES,WOODS,ARCHIVE,targets,encoded,apply,base,name,keep_base,raw
from decor106_preview import Sources,material
from verify_decor106 import model_refs

OUT=ROOT/'docs/implementation/decor106-all-woods';A=TEXTURES.parent
sha=lambda b:hashlib.sha256(b).hexdigest();checks=[]
def check(ok,label):checks.append({'requirement':label,'status':'PASS' if ok else 'FAIL'});assert ok,label
def execute(nodes,scope):exec(compile(ast.Module(body=nodes,type_ignores=[]),'isolated-real-wood-hook','exec'),scope)

def main():
 baseline=json.loads((OUT/'workspace-before.json').read_text());prefix='src/main/resources/assets/slavicmyths/textures/'
 allowed={prefix+rel for rel in targets()}|{'tools/woodlands_0801.py','tools/gardens_resources.py','tools/rc2_visual_resources.py','tools/burial_084.py','docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md'}
 changed=[p for p,h in baseline.items() if not (ROOT/p).is_file() or sha((ROOT/p).read_bytes())!=h]
 check(set(changed)<=allowed,'all original src/tools/status docs outside timber scope byte-exact')
 added={p.relative_to(ROOT).as_posix() for p in (ROOT/'src').rglob('*') if p.is_file()}-{p for p in baseline if p.startswith('src/')}
 check(added=={prefix+'entity/signs/apple.png',prefix+'entity/signs/hanging/apple.png'},'only two missing active apple sign PNG paths added')
 (OUT/'workspace-diff.json').write_text(json.dumps({'modified':changed,'added':sorted(added)},indent=2)+'\n')
 manifest=[]
 with tempfile.TemporaryDirectory() as td:
  root=Path(td)/'textures';apply(root)
  for rel,(wood,kind,detail) in targets().items():
   path=TEXTURES/rel;data=path.read_bytes();check(data==encoded(rel)==(root/rel).read_bytes(),'exact canonical regeneration '+rel)
   im=Image.open(BytesIO(data)).convert('RGBA');oldrel=detail if kind=='alias' else rel;old=Image.open(BytesIO(raw(oldrel))).convert('RGBA')
   check(im.size==old.size,'native resolution preserved '+rel)
   check(im.getchannel('A').tobytes()==old.getchannel('A').tobytes(),'holes/silhouette/alpha preserved '+rel)
   before_hash=baseline.get(prefix+rel);changed_now=before_hash!=sha(data)
   if kind=='base' and keep_base(wood,detail):check(not changed_now,'approved linden/end grain/good bark KEEP '+rel)
   manifest.append({'family':wood,'kind':kind,'path':prefix+rel,'size':im.size,'action':'ADD_MISSING_ACTIVE_PATH' if before_hash is None else 'FIX' if changed_now else 'KEEP','sha_old':before_hash,'sha_new':sha(data)})
  # Execute only real hook functions, never import full legacy pipelines.
  tree=ast.parse((ROOT/'tools/woodlands_0801.py').read_text(encoding='utf8'));node=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='texture')
  def forbidden(*a):raise AssertionError('legacy fallback used for canonical wood')
  scope={'A':Path(td),'png':forbidden};execute([node],scope)
  for wood in ['linden','rowan','willow','pine']:
   for kind in ['planks','log','log_top','stripped_log','stripped_log_top','door_top','door_bottom','trapdoor']:scope['texture'](name(wood,kind),None)
  tree=ast.parse((ROOT/'tools/gardens_resources.py').read_text(encoding='utf8'));node=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='save');scope={'A':Path(td)};execute([node],scope)
  for rel in [r for r,v in targets().items() if v[0]=='apple' and v[1]!='alias']:
   folder,filename=rel.rsplit('/',1);scope['save'](None,folder,Path(filename).stem)
  for module in ['rc2_visual_resources','burial_084']:
   tree=ast.parse((ROOT/'tools'/f'{module}.py').read_text(encoding='utf8'))
   call=next(n for n in ast.walk(tree) if isinstance(n,ast.Expr) and isinstance(n.value,ast.Call) and isinstance(n.value.func,ast.Name) and n.value.func.id=='apply_current_timber')
   execute([call],{'apply_current_timber':partial(apply,root)})
  for rel in targets():check((root/rel).read_bytes()==encoded(rel),'all isolated legacy hooks retain current timber '+rel)
 # Shape motifs differ, not one generic drawing recoloured across species.
 masks=[]
 for wood in WOODS:
  im=base(wood,'planks').convert('RGB').resize((32,32),Image.Resampling.NEAREST);pixels=list(im.getdata());values=sorted(sum(c) for c in pixels);cut=values[len(values)//3];masks.append(tuple(sum(c)<=cut for c in pixels))
 check(len(set(masks))==6,'six distinct plank value-cluster layouts, not palette-only variants')
 catalog=[line.split('\t') for line in (A/'creative_catalog.tsv').read_text().splitlines() if len(line.split('\t'))==3];families={w:[r[0] for r in catalog if r[2]=='wood_'+w] for w in WOODS}
 check(sum(map(len,families.values()))==85,'all85 wood-family catalog records inventoried')
 source=Sources();graphs={};consumers=[]
 for wood,ids in families.items():
  for n in ids:
   if n.endswith(('_leaves','_sapling')):graphs[n]={'family':wood,'action':'KEEP_FOLIAGE','runtime':'NOT_RUN'};continue
   state=A/'blockstates'/f'{n}.json';refs=sorted(set(model_refs(json.loads(state.read_text()))));check(bool(refs),n+' native state model refs')
   for ref in refs:
    model=source.model(ref)
    for tex in model.get('textures',{}).values():
     if not tex.startswith('#'):source.texture(tex)
   source.model('slavicmyths:item/'+n);graphs[n]={'family':wood,'models':refs,'item_model':f'assets/slavicmyths/models/item/{n}.json','geometry':'UNCHANGED','redstone/placement':'NOT_RUN'}
 refs={rel[:-4] for rel in targets()}
 for p in (A/'models').rglob('*.json'):
  text=p.read_text(encoding='utf8')
  if any('slavicmyths:'+ref+'"' in text for ref in refs):consumers.append(p.relative_to(ROOT).as_posix())
 # Verify actual NeoForge sign material path, not the legacy double-namespace alias.
 sign_paths={w:'entity/signs/'+w+'.png' for w in WOODS if w!='darkened'};sign_paths['apple_hanging']='entity/signs/hanging/apple.png'
 for key,rel in sign_paths.items():check((TEXTURES/rel).is_file(),'active WoodType material exists '+key)
 check('entity/signs/' in (OUT/'Sheets-sign-path-constants.txt').read_text(encoding='utf8'),'installed Sheets material-path evidence captured')
 (OUT/'dependencies.json').write_text(json.dumps({'catalog':families,'graphs':graphs,'direct_model_consumers':consumers,'active_sign_materials':sign_paths,'resolved_model_dependencies':sorted(source.used)},indent=2)+'\n')
 jars=[p for p in (ROOT/'build/libs').glob('*.jar') if not any(s in p.name for s in ['sources','javadoc'])];check(len(jars)==1,'one production JAR');jar=jars[0];resources=pngs=jsons=0
 with zipfile.ZipFile(jar) as z:
  for p in (ROOT/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   rel=p.relative_to(ROOT/'src/main/resources').as_posix();expected=ROOT/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else p
   check(z.read(rel)==expected.read_bytes(),'JAR resource parity '+rel);resources+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'));jsons+=1
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    pngs+=1
  check(not any(n.startswith(('tools/','org/slavicmyths/verify/')) for n in z.namelist()),'no offline/smoke tools in production JAR')
 with zipfile.ZipFile(ARCHIVE) as z:templates={n:sha(z.read(n)) for n in z.namelist()}
 result={'assets':manifest,'checks':checks,'resources':resources,'json':jsons,'png':pngs,'template_archive':{'path':str(ARCHIVE),'sha256':sha(ARCHIVE.read_bytes()),'entries':templates},'jar':{'path':str(jar),'bytes':jar.stat().st_size,'sha256':sha(jar.read_bytes())},'minecraft_launches':0,'status':'IN_GAME_OWNER_PENDING'}
 (OUT/'verification.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps({k:v for k,v in result.items() if k not in ['assets','checks','template_archive']},indent=2))

if __name__=='__main__':main()
