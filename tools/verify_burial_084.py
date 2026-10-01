"""0.8.4 artifact checks; no Minecraft launch or simulated claim of gameplay QA."""
from pathlib import Path
import json,hashlib,re
from burial_084 import generate,KINDS,NAMES
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';A=RES/'assets/slavicmyths';D=RES/'data/slavicmyths'
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def snapshot():return {str(p.relative_to(RES)):hashlib.sha256(p.read_bytes()).digest()for p in RES.rglob('*')if p.is_file()}
def main():
 before=snapshot();generate();assert before==snapshot(),'Resource regeneration changed output'
 manifest=read(R/'docs/verification/burial-0.8.4.json');code=(R/'src/main/java/org/slavicmyths/kurgan/DarkenedWood.java').read_text(encoding='utf-8');registered=re.findall(r'add\("([a-z_]+)"',code);assert registered==KINDS
 assert manifest['blocks']==['darkened_'+k for k in registered]
 for k in KINDS:
  assert(A/f'blockstates/darkened_{k}.json').exists();assert(D/f'loot_tables/blocks/darkened_{k}.json').exists()
 shapes={}
 for name in NAMES:
  m=read(A/f'models/item/{name}.json');assert len(m['elements'])>=6
  for e in m['elements']:assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to']))
  shapes[name]=json.dumps(m['elements'],sort_keys=True)
 for pair in [('ancient_carolingian_sword','ancient_chekan'),('ancient_chekan','ancient_spear'),('lunula','grivna'),('ancient_fibula','old_buckle')]:assert shapes[pair[0]]!=shapes[pair[1]]
 for n in ['restored_carolingian_sword','chekan','restored_spear']:
  recipe=read(D/f'recipes/restore_{n}.json');assert recipe['type']=='slavicmyths:armorer_shapeless';assert len(recipe['ingredients'])==4
 assert read(D/'recipes/chekan.json')['type']=='minecraft:crafting_shaped'
 for group in ['common','jewelry','weapons']:
  table=read(D/f'loot_tables/chests/burial_{group}.json');assert table['pools'][0]['rolls']==1
  if group=='common':assert any(e['name']=='slavicmyths:ancient_coin'for e in table['pools'][0]['entries'])
 for kind in ['small','warrior','great']:
  pools=read(D/f'loot_tables/chests/burial_{kind}.json')['pools'];assert len(pools)==2
  assert all(p['rolls']==1 and 0<p['conditions'][0]['chance']<1 for p in pools)
 coffin=read(D/'loot_tables/blocks/burial_log_coffin.json');assert 'foot'in json.dumps(coffin) and 'copy_nbt'not in json.dumps(coffin)
 for lang in ['ru_ru','en_us']:
  data=read(A/f'lang/{lang}.json');assert all(('block'if n=='burial_log_coffin'else'item')+'.slavicmyths.'+n in data for n in NAMES)
  assert data['entity.slavicmyths.nightingale']==('Соловей-разбойник'if lang=='ru_ru'else'Nightingale the Robber')
 assert len(read(A/'sounds.json')['coffin_open']['sounds'])==1
 print('PASS: 0.8.4 reproducible resources, nine wood variants, distinct models, existing-table restoration, max two loot rolls with empty outcomes, reused coin, foot-only coffin drop and RU/EN.')
if __name__=='__main__':main()
