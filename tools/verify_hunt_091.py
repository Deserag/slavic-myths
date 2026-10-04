"""Hunt II authored asset reproducibility and exact data/API integration contracts."""
from hunt_091 import generate,ITEMS,SMALL,A,D,R,write
from PIL import Image,ImageDraw
from pathlib import Path
import json,hashlib

def read(p):return json.loads(p.read_text(encoding='utf8'))
def digest(paths):return {str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in paths}
paths=[A/f'textures/item/{n}.png' for n in ITEMS]+[A/f'textures/entity/{n}.png' for n in ['fire_serpent','podvey','hunt_star_accessory']]
protected=[A/f'{folder}/{n}.{ext}' for folder,ext in [('models/item','json'),('textures/item','png')] for n in ['battle_axe','carved_staff','club','retainer_shield','ohotnichiy_rog']]+[A/f'textures/entity/{n}.png' for n in ['ovinnik','volkolak','hunt_accessories']]
before=digest(paths);user=digest(protected);generate();assert before==digest(paths);assert user==digest(protected),'existing art changed'
board=Image.new('RGBA',(1024,600),'#25242B');b=ImageDraw.Draw(board)
for i,n in enumerate(ITEMS):
 image=Image.open(A/f'textures/item/{n}.png');size=256 if n in SMALL else 512;assert image.size==(size,size);assert image.getbbox() and len(image.getcolors(size*size))>4
 thumbnail=image.resize((220,220),Image.Resampling.NEAREST);board.alpha_composite(thumbnail,((i%4)*256+18,(i//4)*300+8));b.text(((i%4)*256+8,(i//4)*300+244),n,fill='white')
 for lang in ['ru_ru','en_us']:assert read(A/f'lang/{lang}.json')['item.slavicmyths.'+n+'.effect']
board.convert('RGB').save(R/'docs/verification/hunt-items-0.9.1.png')
for n in ['fire_serpent','podvey']:assert Image.open(A/f'textures/entity/{n}.png').size==(512,512)
assert read(D/'recipes/obereg_padayuschey_zvezdy.json')['pattern']==['SIS',' F ','STS'];assert read(D/'recipes/vetrovoy_uzel.json')['pattern']==['TUT','LBL',' S ']
for n,expected in [('sosud_ognennogo_dyhaniya',['pustoy_ritualny_sosud','pylayuschaya_cheshuya','pylayuschaya_cheshuya','ognennoe_pero']),('sosud_podveya',['pustoy_ritualny_sosud','uzel_podveya','vihrevaya_nit','vihrevaya_nit'])]:assert [e['item'] for e in read(D/f'recipes/{n}.json')['ingredients']]==['slavicmyths:'+x for x in expected]
for n in ['pylayuschaya_cheshuya','ognennoe_pero','uzel_podveya','vihrevaya_nit']:assert not (D/f'recipes/{n}.json').exists()
for n,minimum,maximum,ingredient in [('fire_serpent',3,6,'pylayuschaya_cheshuya'),('podvey',1,2,'uzel_podveya')]:
 pools=read(D/f'loot_tables/entities/{n}.json')['pools'];assert pools[0]['entries'][0]['functions'][0]['count']=={'min':minimum,'max':maximum};assert pools[0]['entries'][0]['name']=='slavicmyths:'+ingredient;assert [p['conditions'][0]['chance'] for p in pools if 'conditions' in p]==[.35,.1]
assert set(read(D/'tags/entity_types/hunt_targets.json')['values'])=={'slavicmyths:'+n for n in ['ovinnik','volkolak','fire_serpent','podvey']}
trophies=set(read(D/'tags/items/hunt_trophies.json')['values']);assert {'slavicmyths:'+n for n in ITEMS if n not in ['obereg_padayuschey_zvezdy','vetrovoy_uzel']}<=trophies;assert 'slavicmyths:vetrovoy_uzel' not in trophies and 'slavicmyths:obereg_padayuschey_zvezdy' not in trophies
criteria=read(D/'advancements/bottled_elements.json')['criteria']['event'];assert criteria['trigger']=='minecraft:inventory_changed' and len(criteria['conditions']['items'])==2
java=R/'src/main/java/org/slavicmyths';source=(java/'hunt/ElementHuntMob.java').read_text();assert 'getSensing().canSee' in source and 'getOwner()!=this' in source and 'instanceof AbstractArrowEntity' in source
assert 'deflected.clear()' in source and 'ticks>=60' in source and 'copiesUntil=level.getGameTime()+70' in source
assert '.setBlock(' not in source and '.teleport' not in source
star=(java/'hunt/StarCharmEffects.java').read_text();assert 'p.fallDistance=0' in star and 'now+1200' in star and 'SLOW_FALLING,100' in star and 'isFallFlying()' in star and 'PlayerEvent.Clone' in star
wind=(java/'hunt/WindKnotItem.java').read_text();assert 'addCooldown(this,500)' in wind and 'MobEntity.class' in wind and 'i<28' in wind and '.hurt(' not in wind
projection=(java/'hunt/SerpentProjection.java').read_text();assert 'xpReward=0' in projection and 'LootTables.EMPTY' in projection and 'HuntRecords' not in projection
pouch=(java/'hunt/PouchMenu.java').read_text();assert 'new Inventory(9)' in pouch and 'ClickType.SWAP' in pouch
report={'version':'0.9.1','resource_reproducibility':'PASS','exact_recipes_loot_tags_advancements':'PASS','native_art_dimensions':'PASS','existing_art_hashes':user,'minecraft_launches':0,'runtime_qa':'TODO'}
write(R/'docs/verification/headless-0.9.1.json',report)
print('PASS: Hunt II eight authored assets, exact four recipes/two loot tables, tags, advancements, reproducibility and preserved existing art. GUI/AI runtime not tested.')
