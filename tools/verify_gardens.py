"""Static 1.1.2 acceptance and production validation; never starts Minecraft."""
from pathlib import Path
from PIL import Image
import json,zipfile,hashlib
R=Path('src/main/resources');A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
B=['raspberry','blueberry','blackcurrant','lingonberry','cranberry']
W=['apple_log','stripped_apple_log','apple_wood','stripped_apple_wood','apple_planks','apple_stairs','apple_slab','apple_fence','apple_fence_gate','apple_door','apple_trapdoor','apple_pressure_plate','apple_button','apple_sign','apple_wall_sign','apple_hanging_sign','apple_wall_hanging_sign','apple_leaves','apple_sapling']
J=Path('build/libs/slavicmyths-1.1.2.jar');V=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');VN=set(V.namelist());z=zipfile.ZipFile(J)
count=0
def check(ok,name):
 global count
 count+=1
 if not ok:raise AssertionError(name)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def packaged(p):check(z.read(p.relative_to(R).as_posix())==p.read_bytes(),'production '+str(p))
for n,b in enumerate(B):
 state=A/f'blockstates/{b}_bush.json';check(len(read(state)['variants'])==10,b+' five phases/two half states');packaged(state)
 loot=D/f'loot_table/blocks/{b}_bush.json';data=read(loot);check(len(data['pools'])==4,b+' four drop branches');check(data['pools'][0]['entries'][0]['name']=='slavicmyths:'+b+'_sapling',b+' shears sapling');check(data['pools'][0]['conditions'][0]['predicate']['items']=='minecraft:shears',b+' shears condition');check('fortune' not in json.dumps(data),b+' no fortune bonus')
 check([data['pools'][i]['conditions'][-1 if n else -2]['chance'] for i in [1,2,3]]==[.5,.35,.5],b+' immature/adult/ripe probabilities');packaged(loot)
 for suffix in ['', '_sapling']:
  p=A/f'textures/item/{b}{suffix}.png';im=Image.open(p);check(im.size==(32,32) and set(im.getchannel('A').getdata())=={0,255},b+suffix+' 32px binary alpha');packaged(p);packaged(A/f'models/item/{b}{suffix}.json')
 for phase in range(5):
  for half in ['lower','upper'] if n==0 else ['lower']:
   p=A/f'textures/block/{b}_bush_{half}_{phase}.png';check(Image.open(p).size==(32,32),'32px stage '+str(p));packaged(p)
 rarity=read(D/f'worldgen/placed_feature/{b}_garden_patch.json')['placement'][0]['chance'];check(rarity==[8,10,14,12,8][n],b+' rarity');packaged(D/f'worldgen/placed_feature/{b}_garden_patch.json');packaged(D/f'neoforge/biome_modifier/{b}_garden_patch.json')
for id in W:
 for folder in ['blockstates','models/block']:
  vanilla=id.replace('stripped_apple_','stripped_oak_').replace('apple_','oak_')
  if folder=='blockstates' or f'assets/minecraft/models/block/{vanilla}.json' in VN or id in ['apple_leaves','apple_sapling','apple_sign','apple_wall_sign','apple_hanging_sign','apple_wall_hanging_sign']:
   check((A/f'{folder}/{id}.json').is_file(),id+' '+folder)
 check((D/f'loot_table/blocks/{id}.json').is_file(),id+' loot');packaged(A/f'blockstates/{id}.json');packaged(D/f'loot_table/blocks/{id}.json')
 if id not in ['apple_wall_sign','apple_wall_hanging_sign']:check((A/f'models/item/{id}.json').is_file(),id+' item')
check('minecraft:apple' not in json.dumps(read(D/'loot_table/blocks/apple_leaves.json')),'apple excluded completely from break loot')
check(len(read(A/'blockstates/apple_leaves.json')['variants'])==4,'four fruit textures')
check(read(D/'worldgen/placed_feature/apple_tree.json')['placement'][0]['chance']==18,'apple rarity 1/18')
check(read(D/'neoforge/biome_modifier/core_berry_patch.json')['type']=='neoforge:none','old mixed berry injection disabled, registry preserved')
check(set(read(D/'tags/item/berries.json')['values'])=={'slavicmyths:'+b for b in B},'berries tag exactly five')
check(not any('bush' in s or 'apple_leaves' in s for s in read(D/'tags/block/sickle_harvestable.json')['values']),'sickle excludes gardens')
adv=read(D/'advancement/garden_harvest.json');check(adv['parent']=='minecraft:husbandry/root' and 'rewards' not in adv and not adv['display']['hidden'],'garden advancement task no XP')
for lang in ['ru_ru','en_us']:
 data=read(A/f'lang/{lang}.json')
 for id in B+[b+'_sapling' for b in B]:check('item.slavicmyths.'+id in data,'translation '+lang+id)
 for id in W:check('block.slavicmyths.'+id in data,'translation '+lang+id)
 for id in ['slavicmyths','resources','farming','food','tools','armor','decor','magic','mobs']:check('itemGroup.slavicmyths.'+id in data,'tab translation '+lang+id)
packaged(D/'recipe/apple_planks.json');check(read(D/'recipe/apple_planks.json')['result']['count']==4,'four planks from log family')

# All model/texture links; no broad code/repository audit.
refs=0
def ref(id,folder,suffix):
 global refs
 if id.startswith('#'):return
 ns,path=id.split(':',1) if ':' in id else ('minecraft',id);target=f'assets/{ns}/{folder}/{path}{suffix}';refs+=1
 check((R/target).is_file() or target in VN or target in ['assets/minecraft/models/builtin/generated.json','assets/minecraft/models/builtin/entity.json'],'reference '+target)
def walk(value):
 if isinstance(value,dict):
  for key,v in value.items():
   if key in ['parent','model'] and isinstance(v,str):ref(v,'models','.json')
   elif key=='textures' and isinstance(v,dict):
    for texture in v.values():ref(texture,'textures','.png')
   elif isinstance(v,(dict,list)):walk(v)
 elif isinstance(value,list):
  for v in value:walk(v)
jsons=0
for p in R.rglob('*.json'):
 value=read(p);jsons+=1
 if 'assets' in p.parts and ('models' in p.parts or 'blockstates' in p.parts):walk(value)
for p in Path('src/main/java/org/slavicmyths/garden').glob('*.java'):check('net.minecraft.client' not in p.read_text(),'common/server separated '+p.name)
check('version="1.1.2"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'JAR version 1.1.2')
check(not any('/verify/' in n or 'GameTests' in n or n.startswith('tools/') for n in z.namelist()),'production excludes test harness')
mask=[];logs={(0,y,0) for y in range(5)}|{(1,3,0),(-1,3,0),(0,3,1),(0,3,-1)}
for y in range(2,7):
 for x in range(-2,3):
  for zz in range(-2,3):
   allowed=abs(x)+abs(zz)<=1 if y==2 else ((abs(x)<=1 and abs(zz)<=1) or (abs(x)+abs(zz)==2 and (x==0 or zz==0))) if y==6 else not(abs(x)==2 and abs(zz)==2)
   if allowed and (x,y,zz) not in logs:mask.append((x,y,zz))
check(len(mask)==74 and len(logs)==9,'canonical specification: 74 leaf positions and 9 logs')
receipt={'status':'PASS','checks':count,'jsonFiles':jsons,'modelTextureReferences':refs,'canopyLeafPositions':74,'fruitablePositions':26,'gameLaunches':0,'jar':J.as_posix(),'sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'scope':'Static resource/production verification only. Gameplay and GUI rows require manual acceptance.'}
Path('work/gardens-1.1.2-static-checks.json').write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps(receipt,indent=2))
