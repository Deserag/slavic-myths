"""Exact data-chain and unchanged diamond geometry checks. No runtime acceptance."""
from pathlib import Path
import json,hashlib
from nether337_smithing_assets import A,D,FORMS,NAMES
def load(p):return json.loads(p.read_text(encoding='utf-8'))
def main():
 for form in FORMS:
  name='zhara_'+form;r=load(D/'recipe'/Path(name+'.json'));base=('minecraft:' if form in ['sword','axe'] else 'slavicmyths:')+'diamond_'+form
  assert r=={'type':'minecraft:smithing_transform','template':{'item':'slavicmyths:nether_hardening_template'},'base':{'item':base},'addition':{'item':'slavicmyths:zhara_plate'},'result':{'id':'slavicmyths:'+name,'count':1}}
  if form not in ['sword','axe']:
   for suffix in ['','_held','_icon']+(['_handle87','_ball87'] if form=='flail' else []):
    before=load(A/'models/item'/Path('diamond_'+form+suffix+'.json'));after=load(A/'models/item'/Path(name+suffix+'.json'))
    expected=json.loads(json.dumps(before).replace('diamond_'+form,name).replace('slavicmyths:item/weapons87_materials','slavicmyths:item/nethersmithing_materials'));assert after==expected,(form,suffix,'geometry changed')
 # Check every recipe's actual ingredients against the item model inventory.
 recipe_names=[n for n in NAMES if n!='pekelnik_ember']+['zhara_'+n for n in FORMS]
 for name in recipe_names:
  r=load(D/'recipe'/Path(name+'.json'))
  def refs(v):
   if isinstance(v,dict):
    for k,x in v.items():
     if k=='item' and x.startswith('slavicmyths:'):assert (A/'models/item'/Path(x.split(':')[1]+'.json')).is_file(),x
     refs(x)
   elif isinstance(v,list):
    for x in v:refs(x)
  refs(r)
 pool=[p for p in load(D/'loot_table/entities/pekelnik.json')['pools'] if 'slavicmyths:pekelnik_ember' in json.dumps(p)]
 assert len(pool)==1 and pool[0]['conditions'][0]['chance']==.65
 assert load(D/'recipe/nether_hardening_template.json')['result']['count']==4
 assert load(D/'recipe/nether_hardening_template.json')['key']['N']['item']=='minecraft:nether_brick'
 # New icons have different alpha silhouettes, not copies recolored twelve times.
 from PIL import Image
 masks={hashlib.sha256(Image.open(A/'textures/item'/Path(n+'.png')).getchannel('A').tobytes()).hexdigest() for n in NAMES};assert len(masks)==len(NAMES)
 print('PASS six exact smithing recipes, source diamond geometry, component ingredients, one ember pool and twelve distinct material icon silhouettes. UI/runes/mixin runtime NOT_RUN.')
if __name__=='__main__':main()
