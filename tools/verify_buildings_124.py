"""Independent packaged-NBT checks, reproducibility and production-byte gates."""
from pathlib import Path
import json,zipfile,hashlib,argparse
import village_buildings as v
R=v.RES; checks=0
version=next(line.split("=",1)[1].strip() for line in (v.ROOT/"gradle.properties").read_text().splitlines() if line.startswith("mod_version="))
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);a=p.parse_args()
def check(ok,why):
 global checks
 checks+=1
 if not ok:raise AssertionError(why)
vanilla=zipfile.ZipFile(v.JAR);catalog=json.loads((R/'data/slavicmyths/village/catalog.json').read_text())['structures']
for palette in v.PALETTES:check(sum(e['palette']==palette for e in catalog)==34,'34 showcase buildings '+palette)
for f in R.rglob('*.json'):json.loads(f.read_text(encoding='utf-8'));checks+=1
for f in (R/'data/slavicmyths/structure/village').rglob('*.nbt'):
 d=v.read_nbt(f.read_bytes());positions=set();names={s['Name'] for s in d['palette']}
 check(d['DataVersion']==3955,'1.21.1 authored NBT '+str(f))
 for row in d['blocks']:
  pos=tuple(row['pos']);check(pos not in positions and all(0<=x<n for x,n in zip(pos,d['size'])),'Bounds/duplicate '+str(f));positions.add(pos)
  state=d['palette'][row['state']];tag=row.get('nbt',{})
  if 'LootTable' in tag:
   ns,name=tag['LootTable'].split(':');check((R/f'data/{ns}/loot_table/{name}.json').exists(),'Loot '+str(f))
  if state['Name']=='slavicmyths:settlement_chest':check('LootTable' not in tag and not tag.get('Items'),'EMPTY community storage')
  if state['Name']=='minecraft:jigsaw':
   pool=tag.get('pool','minecraft:empty');ns,name=pool.split(':');check(pool=='minecraft:empty' or (R/f'data/{ns}/worldgen/template_pool/{name}.json').exists() or f'data/{ns}/worldgen/template_pool/{name}.json' in vanilla.namelist(),'Connector pool '+pool)
 if '/center/' not in f.as_posix() and '/roads/' not in f.as_posix():
  check(any(s.endswith('_stairs') for s in names),'Authored pitched roof '+str(f)) if '/farm/' not in f.as_posix() else None
for meta in catalog:
 d=v.read_nbt((R/('data/slavicmyths/structure/'+meta['id'].split(':')[1]+'.nbt')).read_bytes());blocks={tuple(r['pos']):d['palette'][r['state']] for r in d['blocks']}
 heads=[p for p,s in blocks.items() if s['Name'].endswith('_bed') and s.get('Properties',{}).get('part')=='head'];feet=[p for p,s in blocks.items() if s['Name'].endswith('_bed') and s.get('Properties',{}).get('part')=='foot']
 check(len(heads)==len(feet)==meta['beds'],'Bed count '+meta['id'])
 if meta['workstation']:
  s=blocks[tuple(meta['workstation'])];block=v.JOBS[meta['kind']][1];expected=block if ':' in block else 'minecraft:'+block;check(s['Name']==expected,'Exact workstation '+meta['id'])
 if meta['kind']=='village_center':check('minecraft:white_banner' in {s['Name'] for s in blocks.values()},'Guaranteed generic shrine')
 for pos in feet:check(blocks.get((pos[0],pos[1]+1,pos[2]),{}).get('Name')=='minecraft:air','Bed headroom '+meta['id'])
for f in (R/'data/minecraft/worldgen/template_pool/village').rglob('*.json'):
 d=json.loads(f.read_text())
 for row in d['elements']:
  el=row['element'];loc=el.get('location')
  if loc:
   ns,path=loc.split(':');ref=f'data/{ns}/structure/{path}.nbt';check((R/ref).exists() or ref in vanilla.namelist(),'Pool template '+loc)
  processors=el.get('processors')
  if isinstance(processors,str):
   ns,path=processors.split(':');ref=f'data/{ns}/worldgen/processor_list/{path}.json';check((R/ref).exists() or ref in vanilla.namelist(),'Processors '+processors)
 before=[]
for row in json.loads((v.ROOT/'docs/verification/village-1.2.4-mapping.json').read_text()):check(row['new'].startswith('slavicmyths:village/'),'Mapped '+row['old'])
before={f:hashlib.sha256(f.read_bytes()).hexdigest() for f in R.rglob('*') if f.is_file()};v.main();after={f:hashlib.sha256(f.read_bytes()).hexdigest() for f in R.rglob('*') if f.is_file()};check(before==after,'All resources byte reproducible')
if a.jar:
 with zipfile.ZipFile(a.jar) as z:
  check('Implementation-Version: '+version in z.read('META-INF/MANIFEST.MF').decode(),'Production version');check(not any('/verify/' in f or '/smoke/' in f or 'GameTests' in f or 'slavicmyths_buildings/' in f for f in z.namelist()),'No test-only code/resources')
  for f in R.rglob('*'):
   if f.is_file():
    rel=f.relative_to(R).as_posix();check(rel in z.namelist(),'Bundled '+rel)
    if rel!='META-INF/neoforge.mods.toml':check(z.read(rel)==f.read_bytes(),'Production bytes '+rel)
report={'version':version,'static_checks':checks,'showcase_per_palette':34,'catalog_templates':102,'total_nbt':len(list((R/'data/slavicmyths/structure/village').rglob('*.nbt'))),'mapped_elements':334,'client_launches':0,'jar':str(a.jar) if a.jar else None}
(v.ROOT/f'docs/verification/village-{version}-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
