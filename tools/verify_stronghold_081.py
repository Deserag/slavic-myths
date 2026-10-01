"""Targeted static validation of 0.8.1 templates and furniture; no game bootstrap."""
from pathlib import Path
import json,hashlib,zipfile,itertools
from verify_swamp_073 import read_nbt
from furniture_081 import generate as furniture
from stronghold_081 import generate as stronghold
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';D=RES/'data/slavicmyths';A=RES/'assets/slavicmyths'
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def snapshot():return {str(p.relative_to(RES)):hashlib.sha256(p.read_bytes()).digest()for p in RES.rglob('*')if p.is_file()}
def main():
 before=snapshot();furniture();stronghold();assert before==snapshot(),'Generators changed checked-in resources'
 files=list((D/'structures/stronghold').glob('*.nbt'));assert len(files)==23,len(files)
 markers={};models=set();template_hashes={}
 for path in files:
  n=read_nbt(path.read_bytes());size=n['size'];cells={tuple(b['pos']):(n['palette'][b['state']],b.get('nbt',{}))for b in n['blocks']};assert len(cells)==len(n['blocks'])
  template_hashes[path.stem]=hashlib.sha256(path.read_bytes()).hexdigest()
  for pos,(state,nbt)in cells.items():
   assert all(0<=v<s for v,s in zip(pos,size)),(path,pos)
   if state['Name'].startswith('slavicmyths:'):assert(A/f'blockstates/{state["Name"].split(":")[1]}.json').exists(),state
   if state['Name']=='minecraft:ladder':
    x,y,z=pos;dx,dz={'north':(0,1),'south':(0,-1),'west':(1,0),'east':(-1,0)}[state['Properties']['facing']];support=cells.get((x+dx,y,z+dz),({'Name':'minecraft:air'},{}))[0]['Name'];assert support not in ['minecraft:air','minecraft:ladder'],(path.stem,pos,'unsupported ladder')
   text=nbt.get('metadata','')
   if not text:continue
   assert text.split(':')[0]in ['spawn','loot','rack','bell']
   if text.startswith('loot:'):assert(D/f'loot_tables/chests/stronghold_{text.split(":")[1]}.json').exists()
   if text.startswith('spawn:'):
    x,y,z=pos;floor=cells.get((x,y-1,z),({'Name':'minecraft:air'},{}))[0]['Name'];head=cells.get((x,y+1,z),({'Name':'minecraft:air'},{}))[0]['Name']
    assert floor!='minecraft:air',(path.stem,pos,'no floor')
    assert head=='minecraft:air',(path.stem,pos,'blocked head',head)
   markers.setdefault(path.stem,[]).append(text)
 # Every building variant combination keeps exactly the same 26-defender roster and five bells.
 fixed=['gate','tower_roof','tower_open','tower_tall','kitchen','ataman_house','commander','stable','prison','utility','central_yard','nightingale_yard']
 for options in itertools.product(['barracks_porch','barracks_side'],['forge_open','forge_closed'],['warehouse_long','warehouse_square']):
  selected=fixed+list(options);spawn=[m.split(':')for name in selected for m in markers.get(name,[])if m.startswith('spawn:')];defenders=[v for v in spawn if v[1]not in ['horse','prisoner']]
  assert sorted(int(v[2])for v in defenders)==list(range(26))
  assert sorted(int(v[2])for v in defenders if v[1].startswith('ataman'))==[0,1]
  assert sorted(int(m.split(':')[1])for name in selected for m in markers.get(name,[])if m.startswith('bell:'))==list(range(5))
  assert len({int(v[2])for v in spawn})==len(spawn)
 assert len({template_hashes['perimeter_'+s]for s in ['forest','plains','hill']})==3
 for variants in [('barracks_porch','barracks_side'),('forge_open','forge_closed'),('warehouse_long','warehouse_square')]:assert template_hashes[variants[0]]!=template_hashes[variants[1]]
 manifest=read(ROOT/'docs/verification/furniture-0.8.1.json');assert len(manifest['blocks'])==21
 for name in manifest['blocks']:
  assert(D/f'recipes/{name}.json').exists();assert(D/f'loot_tables/blocks/{name}.json').exists()
  bs=read(A/f'blockstates/{name}.json');assert len(bs['variants'])==(8 if name.endswith('wardrobe')else 4)
  for value in bs['variants'].values():
   model=read(A/f'models/{value["model"].split(":")[1]}.json');assert len(model['elements'])>=3
   for cube in model['elements']:assert all(a<b for a,b in zip(cube['from'],cube['to']))
  if name.endswith('wardrobe'):assert 'lower'in json.dumps(read(D/f'loot_tables/blocks/{name}.json'))
 cache=read(D/'loot_tables/chests/stronghold_cache.json');assert len(cache['pools'])==12
 assert all(0<p['conditions'][0]['chance']<1 for p in cache['pools'])
 allitems={p.stem for p in (A/'models/item').glob('*.json')}
 def visit(o):
  if isinstance(o,dict):
   if o.get('type')=='minecraft:item'and o.get('name','').startswith('slavicmyths:'):assert o['name'].split(':')[1]in allitems,o['name']
   for v in o.values():visit(v)
  elif isinstance(o,list):
   for v in o:visit(v)
 for p in (D/'loot_tables/chests').glob('stronghold_*.json'):visit(read(p))
 for f in (ROOT/'src/main/java/org/slavicmyths/furniture').glob('*.java'):
  if f.stem!='FurnitureClient':assert 'net.minecraft.client'not in f.read_text()
 source=(ROOT/'src/main/java/org/slavicmyths/furniture/Furniture.java').read_text();assert '.noSave().noSummon()'in source
 assert not any('nightingale'in p.name.lower()for p in (ROOT/'src/main/java').rglob('*Entity.java'))
 jar=ROOT/'build/libs/slavicmyths-0.8.1.jar'
 if jar.exists():
  with zipfile.ZipFile(jar)as z:
   for p in RES.rglob('*'):
    if p.is_file()and p.name!='mods.toml':assert z.read(p.relative_to(RES).as_posix())==p.read_bytes(),p
 print('PASS: 23 settlement templates; 8 building combinations preserve 26 defenders, exactly two Atamans, five alarm zones and unique auxiliary spawns. Marker floor/headroom, furniture states/models/recipes/loot, optional cache pools, reference integrity and generator reproducibility passed.')
 print('Static checks only. Minecraft launches: 0; terrain/navigation/alarm timing/seating/rack sync/rejoin remain manual tests.')
if __name__=='__main__':main()
