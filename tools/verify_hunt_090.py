"""Scoped reproducibility, exact recipes/loot, namespace, dimensions and production hooks."""
from hunt_090 import generate,ITEMS,INGREDIENTS,A,D,R
import json,hashlib,struct,re
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def snapshot():
 paths=[A/f'textures/item/{n}.png' for n in ITEMS]+[A/f'textures/entity/{n}.png' for n in ['ovinnik','volkolak','hunt_accessories']]+[D/f'loot_tables/entities/{n}.json' for n in ['ovinnik','volkolak']]+list((D/'recipes').glob('*ohot*.json'))
 return {str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in paths}
protected=[A/f'{folder}/{n}.{ext}' for folder,ext in [('models/item','json'),('textures/item','png')] for n in ['battle_axe','carved_staff','club','retainer_shield']]
user={str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in protected};before=snapshot();generate();assert before==snapshot(),'resource layer not reproducible';assert user=={str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in protected},'user artwork changed'
for n in ITEMS:
 p=A/f'textures/item/{n}.png';size=256 if n in INGREDIENTS else 512;assert struct.unpack('!II',p.read_bytes()[16:24])==(size,size)
 for lang in ['ru_ru','en_us']:assert read(A/f'lang/{lang}.json')['item.slavicmyths.'+n+'.effect']
for n in ['ovinnik','volkolak','hunt_accessories']:assert struct.unpack('!II',(A/f'textures/entity/{n}.png').read_bytes()[16:24])==(256,256)
assert set(read(D/'tags/items/hunt_trophies.json')['values'])>={'slavicmyths:'+n for n in INGREDIENTS+['pustoy_ritualny_sosud']}
assert set(read(D/'tags/entity_types/hunt_targets.json')['values'])>={'slavicmyths:ovinnik','slavicmyths:volkolak'}
registered=(R/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text(encoding='utf-8')
for n in read(D/'tags/items/silver_weapons.json')['values']:assert '"'+n.split(':')[1]+'"' in registered
expected={'ohotnichiy_rog':['BL ',' BI','S B'],'zolny_obereg':['ISI','AKA',' A '],'volchiy_poyas':['LLL','FIF'],'obereg_ohotnika':['SBS','WCW',' R '],'meshochek_trofeev':['LSL','L L','LIL']}
for n,rows in expected.items():assert read(D/f'recipes/{n}.json')['pattern']==rows
for n in INGREDIENTS+['pustoy_ritualny_sosud']:assert not (D/f'recipes/{n}.json').exists(),'loot-only item has fake recipe'
ovi=read(D/'loot_tables/entities/ovinnik.json')['pools'];wolf=read(D/'loot_tables/entities/volkolak.json')['pools'];assert ovi[0]['entries'][0]['functions'][0]['count']=={'min':8,'max':14};assert wolf[0]['entries'][0]['functions'][0]['count']=={'min':2,'max':4}
assert [p['conditions'][0]['chance'] for p in ovi if 'conditions' in p]==[.2,.08];assert [p['conditions'][0]['chance'] for p in wolf if 'conditions' in p]==[.35,.25,.08]
model=read(A/'models/item/ohotnichiy_rog.json');assert len(model['elements'])>=20 and 'firstperson_righthand' in model['display']
for e in model['elements']:assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to']))
source=(R/'src/main/java/org/slavicmyths/hunt/PouchMenu.java').read_text();assert 'new Inventory(9)' in source and 'mayPickup' in source and 'ClickType.SWAP' in source and 'save();' in source
effects=(R/'src/main/java/org/slavicmyths/hunt/HuntEffects.java').read_text();assert 'LivingDamageEvent' in effects and '1.25F' in effects and 'addTransientModifier' in effects
report={'version':'0.9.0','resource_reproducibility':'PASS','exact_loot_recipes_tags':'PASS','ingredients_256':'PASS','gear_512':'PASS','protected_weapon_hashes':user,'minecraft_launches':0,'runtime_qa':'TODO'}
(R/'docs/verification/headless-0.9.0.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print('PASS: ten detailed assets, 256/512px, exact five recipes and two loot tables, namespaced tags, 3D horn, equipped hooks, pouch locks and preserved user artwork. Runtime GUI/AI tests TODO.')
