"""Offline evidence for ONE linden pilot; never runs legacy generators or game."""
from pathlib import Path
import ast,json,hashlib,tempfile,zipfile
from PIL import Image
from decor106_linden import ROOT,FIX,KEEP,sprite,write_owned
from decor106_preview import Sources,A,OUT

sha=lambda b:hashlib.sha256(b).hexdigest()
checks=[]
def check(ok,label):
 checks.append({'requirement':label,'status':'PASS' if ok else 'FAIL'});assert ok,label

def model_refs(obj):
 if isinstance(obj,dict):
  for key,value in obj.items():
   if key=='model' and isinstance(value,str):yield value
   else:yield from model_refs(value)
 elif isinstance(obj,list):
  for value in obj:yield from model_refs(value)

def longest_column(im,color):
 longest=0
 for x in range(im.width):
  run=0
  for y in range(im.height):
   run=run+1 if im.getpixel((x,y))==color else 0;longest=max(longest,run)
 return longest

def main():
 baseline=json.loads((OUT/'workspace-before.json').read_text());allowed={f'src/main/resources/assets/slavicmyths/textures/block/{n}.png' for n in FIX}|{'tools/woodlands_0801.py','docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md'}
 changes=[p for p,h in baseline.items() if not (ROOT/p).is_file() or sha((ROOT/p).read_bytes())!=h]
 check(set(changes)<=allowed,'all original src/tools/docs outside pilot preserved byte-exact')
 check(set(p.relative_to(ROOT).as_posix() for p in (ROOT/'src').rglob('*') if p.is_file())=={p for p in baseline if p.startswith('src/')},'no added/deleted source/resource files')
 (OUT/'workspace-diff.json').write_text(json.dumps(changes,indent=2)+'\n')
 assets=[]
 for n in FIX+KEEP:
  p=A/'textures/block'/f'{n}.png';old=OUT/'before'/p.name;im=Image.open(p).convert('RGBA');before=Image.open(old).convert('RGBA')
  check(im.size==before.size==(16,16),n+' native16px')
  check(set(im.getdata())==set(before.getdata()),n+' exact original palette, opaque/no AA')
  if n in FIX:
   check(im.tobytes()==sprite(n).tobytes(),n+' exact reproducible pixel master')
   check(im.tobytes()!=before.tobytes(),n+' actual changed source')
  else:check(p.read_bytes()==old.read_bytes(),n+' KEEP original end grain')
  metric={}
  if n in ['linden_log','linden_stripped_log']:
   dark=min(set(before.getdata()),key=lambda c:sum(c[:3]));metric={'dark_vertical_run_before':longest_column(before,dark),'dark_vertical_run_after':longest_column(im,dark)}
   check(metric['dark_vertical_run_after']<metric['dark_vertical_run_before'],n+' full-height groove broken into shorter clusters')
  if n=='linden_planks':
   check(all(im.getpixel((x,y))==before.getpixel((x,y)) for y in [0,4,8,12] for x in range(16)),'plank construction joints preserved; grain only')
  assets.append({'id':n,'action':'FIX' if n in FIX else 'KEEP','texture':p.relative_to(ROOT).as_posix(),'sha_old':sha(old.read_bytes()),'sha_new':sha(p.read_bytes()),'size':im.size,'evidence_metrics':metric})
 with tempfile.TemporaryDirectory() as td:
  temp=Path(td);root=temp/'textures/block'
  tree=ast.parse((ROOT/'tools/woodlands_0801.py').read_text(encoding='utf8'));fn=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='texture')
  def forbidden(*args):raise AssertionError('legacy PNG fallback should not run for owned files')
  ns={'A':temp,'png':forbidden};exec(compile(ast.Module(body=[fn],type_ignores=[]),'actual-legacy-texture','exec'),ns)
  for n in FIX:
   ns['texture'](n,None);check((root/f'{n}.png').read_bytes()==(A/'textures/block'/f'{n}.png').read_bytes(),'legacy woodland entrypoint exact '+n)
  check(not write_owned(root/'willow_log.png'),'willow not intercepted/written')
  check(not write_owned(temp/'textures/item/linden_log.png'),'item PNG not intercepted/written')
 catalog=[line.split('\t') for line in (A/'creative_catalog.tsv').read_text().splitlines() if len(line.split('\t'))==3]
 family=[row[0] for row in catalog if row[2]=='wood_linden'];source=Sources();graph={}
 for n in family:
  if n=='linden_leaves':continue # Leaves are explicitly outside the timber correction.
  data=json.loads((A/'blockstates'/f'{n}.json').read_text());refs=sorted(set(model_refs(data)))
  check(bool(refs),n+' blockstate references')
  for ref in refs:
   m=source.model(ref)
   for texture in m.get('textures',{}).values():
    if texture.startswith('#'):continue
    source.texture(texture)
  source.model('slavicmyths:item/'+n)
  graph[n]={'item_model':f'assets/slavicmyths/models/item/{n}.json','blockstate':f'assets/slavicmyths/blockstates/{n}.json','models':refs,'geometry':'UNCHANGED','placement/redstone':'NOT_RUN'}
 check(len(graph)==14,'all fourteen timber catalog item/state graphs inspected')
 # Check every reverse texture consumer, including existing furniture: no geometry edits.
 consumers=[]
 for p in (A/'models').rglob('*.json'):
  text=p.read_text(encoding='utf8')
  if any('slavicmyths:block/'+n+'"' in text for n in FIX):consumers.append(p.relative_to(ROOT).as_posix())
 (OUT/'dependencies.json').write_text(json.dumps({'family':family,'graphs':graph,'direct_texture_consumers':consumers,'resolved_dependencies':sorted(source.used)},indent=2)+'\n')
 jars=[p for p in (ROOT/'build/libs').glob('*.jar') if not any(s in p.name for s in ['sources','javadoc'])];check(len(jars)==1,'one production JAR');jar=jars[0];count=pngs=jsons=0
 with zipfile.ZipFile(jar) as z:
  for p in (ROOT/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   relative=p.relative_to(ROOT/'src/main/resources').as_posix();expected=ROOT/'build/resources/main'/relative if relative=='META-INF/neoforge.mods.toml' else p
   check(z.read(relative)==expected.read_bytes(),'JAR resource parity '+relative);count+=1
   if p.suffix=='.png':
    with Image.open(p) as image:image.verify()
    pngs+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'));jsons+=1
  check(not any(n.startswith(('tools/','org/slavicmyths/verify/')) for n in z.namelist()),'no smoke fixtures/tools in production')
 result={'assets':assets,'checks':checks,'resources':count,'png':pngs,'json':jsons,'jar':{'path':str(jar),'bytes':jar.stat().st_size,'sha256':sha(jar.read_bytes())},'minecraft_launches':0,'status':'PILOT_IN_PROGRESS_OWNER_PENDING','remaining':'willow and four other family audits wait for pilot acceptance'}
 (OUT/'verification.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps({k:v for k,v in result.items() if k not in ['checks','assets']},indent=2))

if __name__=='__main__':main()
