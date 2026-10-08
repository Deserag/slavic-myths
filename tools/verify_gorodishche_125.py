"""Independent native NBT/catalog/model/production checks. No gameplay/MSPT claim."""
from pathlib import Path
import json,hashlib,zipfile,argparse
import village_buildings as v
import gorodishche_resources as city
R=v.RES;checks=0
version=next(line.split('=',1)[1].strip() for line in (v.ROOT/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
parser=argparse.ArgumentParser();parser.add_argument('--jar',type=Path);args=parser.parse_args()
def check(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
catalog=json.loads((R/'data/slavicmyths/gorodishche/catalog.json').read_text())['buildings']
check(len(catalog)==240,'240 authored buildings')
for climate in ['temperate','cold','warm']:check(sum(b['climate']==climate for b in catalog)==80,'80 templates '+climate)
for m in catalog:
 n=v.read_nbt((R/('data/slavicmyths/structure/'+m['id'].split(':')[1]+'.nbt')).read_bytes());check(n['size']==m['size'] and n['DataVersion']==3955,'metadata size/version '+m['id']);blocks={};spawn=[]
 for row in n['blocks']:
  p=tuple(row['pos']);check(p not in blocks and all(0<=c<d for c,d in zip(p,n['size'])),'native bounds '+m['id']);state=n['palette'][row['state']];blocks[p]=state;tag=row.get('nbt',{})
  if 'LootTable' in tag:
   ns,name=tag['LootTable'].split(':');check((R/f'data/{ns}/loot_table/{name}.json').is_file(),'real lazy loot '+tag['LootTable'])
  if state['Name']=='slavicmyths:settlement_chest':check(not tag.get('Items') and 'LootTable' not in tag,'empty settlement chest')
  if state['Name']=='minecraft:structure_block' and tag.get('metadata','').startswith('spawn|'):spawn.append(int(tag['metadata'].split('|')[1]))
 check(len(spawn)==len(set(spawn)) and all(0<=i<63 for i in spawn),'unique persisted spawn markers')
 for row in n['blocks']:
  if row.get('nbt',{}).get('metadata','').startswith('spawn|'):
   x,y,z=row['pos'];check(blocks.get((x,y+1,z),{}).get('Name','minecraft:air')=='minecraft:air','NPC headroom '+m['id']+' '+str(row['pos']))
 heads=[p for p,s in blocks.items() if s['Name'].endswith('_bed') and s.get('Properties',{}).get('part')=='head'];check(sorted(heads)==sorted(tuple(p) for p in m['beds']),'exact bed catalog '+m['id'])
 for p in heads:
  foot=(p[0],p[1],p[2]+1);check(blocks.get(foot,{}).get('Name')==blocks[p]['Name'] and blocks.get(foot,{}).get('Properties',{}).get('part')=='foot','intact bed halves '+m['id']);check(blocks.get((p[0],p[1]+1,p[2]),{}).get('Name','minecraft:air')=='minecraft:air','bed headroom '+m['id'])
 for job in m['workstations']:check(blocks[tuple(job['pos'])]['Name']==job['block'],'exact workstation '+m['id'])
 if m['name']=='tower_corner_01':
  for x in [0,8]:
   for z in [0,8]:
    for y in range(12):check(blocks.get((x,y,z),{}).get('Name')=='minecraft:dark_oak_log','continuous tower post '+m['id'])
  check(not any(s['Name'].endswith('_fence') for s in blocks.values()),'solid tower parapet')
 if m['category'] in ['residential','profession'] or m['name'].startswith('trading_house'):
  z0=4 if m['climate']=='warm' else 3;z1=m['size'][2]-2;x1=m['size'][0]-2;radius=0 if m['size'][0]<13 else 1
  for y in [2,7] if m['floors']>1 else [2]:
   for z in [z0,z1]:
    for x in [3,x1-2]:
     names=[blocks.get((x+dx,y,z),{}).get('Name','minecraft:air') for dx in range(-radius,radius+1)];check(len(set(names))==1 and (names[0]=='minecraft:glass_pane' or names[0].endswith('_trapdoor')),'single-material window '+m['id'])
  if m['floors']>1:
   sx=3 if m['name'].startswith('trading_house') else x1-2
   for dz in [1,2]:check(blocks.get((sx,1,z0+dz),{}).get('Name','minecraft:air')=='minecraft:air','clear stair bottom landing '+m['id'])
 if m['name']=='gate_main_01':
  for x in range(5,10):
   for z in range(9):
    for y in range(1,6):check(blocks.get((x,y,z),{}).get('Name','minecraft:air')=='minecraft:air','clear 5x5 gate')
 if m['name']=='druzhinnik_barracks_01':check(len(m['workstations'])>=6 and len(m['beds'])>=6,'existing barracks guard capacity')
for level in range(6):
 model=json.loads((R/f'assets/slavicmyths/models/block/storage_sack_{level}.json').read_text());check(len(model.get('elements',[]))>=5,'bulging gathered sack model');check(max(e['to'][1] for e in model['elements'])<=16,'native sack height')
from beer_models_125 import COLORS
for name in COLORS:
 for variant in ['mug','bottled']:
  path=R/f'assets/slavicmyths/models/item/{name}_{variant}.json';check(path.is_file(),'registered vessel model '+name);model=json.loads(path.read_text());check(len(model.get('elements',[]))>=5,'real beer vessel '+name)
  for e in model['elements']:check(all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),'vessel native cuboid')
settings=json.loads((R/'data/slavicmyths/worldgen/structure_set/gorodishche.json').read_text())['placement'];check(settings['spacing']==128 and settings['separation']==96,'rare native spacing')
before={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in R.rglob('*') if p.is_file()};city.main();after={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in R.rglob('*') if p.is_file()};check(before==after,'city resource reproducibility')
report={'version':version,'catalog_stage':'1.2.5','checks':checks,'templates':240,'climates':3,'mode':'static NBT/catalog/models','performance_measured':False}
if args.jar:
 with zipfile.ZipFile(args.jar) as jar:
  check('Implementation-Version: '+version in jar.read('META-INF/MANIFEST.MF').decode(),'production version');check(not any('/verify/' in p or '/smoke/' in p or 'GameTests' in p or '/slavicmyths_city/' in p for p in jar.namelist()),'test code/resources excluded')
  for p in R.rglob('*'):
   if p.is_file() and p.relative_to(R).as_posix()!='META-INF/neoforge.mods.toml':check(jar.read(p.relative_to(R).as_posix())==p.read_bytes(),'exact packaged resource '+str(p))
 report.update(sha256=hashlib.sha256(args.jar.read_bytes()).hexdigest(),jar=str(args.jar))
report['checks']=checks;(v.ROOT/f'docs/verification/gorodishche-{version}-static.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
