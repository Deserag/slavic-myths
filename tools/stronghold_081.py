"""Authored III-tier settlement components; sparse irregular perimeters and furnished buildings."""
from pathlib import Path
import json
import swamp_073 as blue
from bandits_080 import write
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';D=RES/'data/slavicmyths';A=RES/'assets/slavicmyths'
MANIFEST={}
def block(b,x,y,z,n,**props):b.put(x,y,z,'slavicmyths:'+n,**props)
def marker(b,x,y,z,text):
 b.put(x,y,z,'structure_block',mode='data');state,_=b.blocks[x,y,z];b.blocks[x,y,z]=(state,{'id':'minecraft:structure_block','mode':'DATA','metadata':text})
def spawn(b,x,y,z,role,slot,zone=0,duty=0):marker(b,x,y,z,f'spawn:{role}:{slot}:{zone}:{duty}')
def furn(b,x,y,z,k,wood='pine',facing='north'):
 n=k if k in ['cloth_bag','wooden_crate','firewood_bundle','training_dummy','signal_bell']else wood+'_'+k
 block(b,x,y,z,n,facing=facing,**({'half':'lower'}if k=='wardrobe'else{}))
 if k=='wardrobe':block(b,x,y+1,z,n,facing=facing,half='upper')
def bed(b,x,y,z):
 b.put(x,y,z,'red_bed',facing='south',part='foot',occupied='false');b.put(x,y,z+1,'red_bed',facing='south',part='head',occupied='false')
def lantern(b,x,y,z):b.put(x,y,z,'lantern',hanging='false',waterlogged='false')
def shell(name,w,d,open_side=False):
 b=blue.Blueprint(name,(w,12,d));b.box((0,0,0),(w-1,0,d-1),'cobblestone');b.box((1,1,1),(w-2,5,d-2),'air')
 for x in range(w):
  for z in range(d):
   if x in (0,w-1)or z in (0,d-1):
    if not(open_side and z==0):b.box((x,1,z),(x,4,z),'slavicmyths:pine_planks')
 for x in [0,w-1]:
  for z in [0,d-1]:b.box((x,1,z),(x,5,z),'slavicmyths:pine_log',axis='y')
 for x in range(w):
  for z in range(d):
   y=5+min(z,d-1-z)//2
   block(b,x,y,z,'pine_stairs',facing='south'if z<d/2 else'north',half='bottom',shape='straight',waterlogged='false')
 # Gable closure below rising roof.
 for x in [0,w-1]:
  for z in range(1,d-1):
   for y in range(5,5+min(z,d-1-z)//2):block(b,x,y,z,'pine_planks')
 for x in [2,w-3]:
  b.put(x,3,d-1,'glass_pane');b.put(x,2,d-1,'glass_pane')
 if not open_side:
  for x in [w//2,w//2+1]:
   block(b,x,1,0,'linden_door',facing='north',half='lower',hinge='left'if x==w//2 else'right',open='false',powered='false');block(b,x,2,0,'linden_door',facing='north',half='upper',hinge='left'if x==w//2 else'right',open='false',powered='false')
 lantern(b,1,1,1)
 return b

def save(b):MANIFEST[b.name]=b.save()
def buildings():
 for variant in ['porch','side']:
  b=shell('barracks_'+variant,13,11)
  for x in [2,4,8,10]:bed(b,x,1,7)
  furn(b,2,1,3,'table');furn(b,2,1,2,'bench');furn(b,2,1,4,'bench');furn(b,10,1,3,'wardrobe');furn(b,11,3,6,'shelf',facing='west');marker(b,10,1,5,'loot:barracks');marker(b,1,1,6,'rack:sword')
  for x,z,role,slot in [(5,4,'fighter',7),(7,4,'fighter',8),(5,7,'heavy',9),(7,7,'archer',10)]:spawn(b,x,1,z,role,slot,1,1 if slot==7 else 0)
  marker(b,1,1,3,'bell:1')
  if variant=='porch':b.box((4,0,0),(8,0,2),'slavicmyths:pine_planks');b.box((4,4,0),(8,4,2),'slavicmyths:pine_slab',type='bottom',waterlogged='false')
  else:
   b.box((0,4,2),(3,4,6),'slavicmyths:willow_slab',type='bottom',waterlogged='false');furn(b,1,1,5,'wooden_crate')
  save(b)
 for variant in ['open','closed']:
  b=shell('forge_'+variant,11,9,variant=='open');b.box((1,1,6),(3,3,7),'stone_bricks');b.put(2,1,5,'blast_furnace',facing='north',lit='false');b.box((2,4,7),(2,8,7),'cobblestone');b.put(4,1,5,'anvil',facing='north');block(b,6,1,6,'armorer_table');b.put(3,1,3,'coal_block');furn(b,8,1,6,'cloth_bag');furn(b,7,1,3,'stool');marker(b,8,1,2,'loot:forge');spawn(b,5,1,4,'fighter',11,2,3);spawn(b,8,1,4,'heavy',12,2,0);save(b)
 for variant,w,d in [('long',13,9),('square',11,11)]:
  b=shell('warehouse_'+variant,w,d)
  for x,z in [(1,3),(2,4),(w-3,4),(w-2,d-2)]:furn(b,x,1,z,'wooden_crate');furn(b,x,2,z,'cloth_bag')
  marker(b,2,1,d-3,'loot:warehouse');marker(b,w-3,1,d-3,'loot:warehouse');spawn(b,4,1,5,'fighter',13,2,1);spawn(b,7,1,5,'archer',14,2,0);save(b)
 b=shell('kitchen',11,11);b.box((1,1,7),(3,2,8),'cobblestone');b.put(2,2,7,'smoker',facing='north',lit='false');b.put(2,1,6,'cauldron',level='2');b.put(8,1,8,'hay_block',axis='y')
 for z in [3,5]:furn(b,4,1,z,'table','linden');furn(b,3,1,z,'stool','linden');furn(b,5,1,z,'stool','linden')
 furn(b,1,1,4,'firewood_bundle');marker(b,8,1,6,'loot:kitchen');spawn(b,7,1,3,'fighter',15,2,1);spawn(b,7,1,5,'fighter',16,2,0);save(b)
 b=shell('ataman_house',15,11);b.box((1,0,1),(13,0,9),'slavicmyths:linden_planks')
 for x in [4,5,6]:furn(b,x,1,4,'table','linden');furn(b,x,1,3,'chair','linden');furn(b,x,1,5,'chair','linden',facing='south')
 bed(b,11,1,7);furn(b,10,1,7,'bedside_cabinet','linden');furn(b,12,1,2,'wardrobe','linden');furn(b,1,3,6,'shelf','linden',facing='east');b.put(2,1,7,'cartography_table');marker(b,12,1,5,'loot:ataman');marker(b,1,1,8,'rack:sword');spawn(b,8,1,3,'senior',19,3,0);spawn(b,8,1,6,'heavy',20,3,0);marker(b,2,1,2,'bell:3');save(b)
 b=shell('commander',11,9);bed(b,2,1,5);furn(b,7,1,5,'table','linden');furn(b,7,1,6,'chair','linden');furn(b,8,1,2,'wardrobe','linden');marker(b,1,1,3,'loot:ataman');marker(b,1,1,1,'bell:4');spawn(b,5,1,4,'ataman_inner',1,4,0);spawn(b,7,1,3,'heavy',21,4,0);spawn(b,3,1,3,'senior',22,4,1);save(b)
 b=shell('stable',13,11,True)
 for x in [1,5,9]:
  b.box((x,1,3),(x,2,8),'slavicmyths:pine_fence',north='true',south='true',east='false',west='false',waterlogged='false');b.put(x+1,1,8,'hay_block',axis='y');b.put(x+2,1,8,'water_cauldron'if False else'cauldron',level='3');spawn(b,x+2,1,5,'horse',40+x//4)
 spawn(b,7,1,1,'fighter',17,1,1);save(b)
 b=shell('prison',9,11)
 for x in [1,5]:
  b.box((x,1,5),(x+2,3,5),'iron_bars',north='false',south='false',east='true',west='true');bed(b,x,1,7)
  for y in [1,2]:b.put(x+1,y,5,'iron_door',half='lower'if y==1 else'upper',facing='north',hinge='left',open='false',powered='false')
  spawn(b,x+1,1,6,'prisoner',44+x//4)
 spawn(b,4,1,3,'heavy',18,3,0);save(b)
 b=shell('utility',11,9,True);b.box((0,5,0),(10,5,8),'slavicmyths:willow_slab',type='bottom',waterlogged='false')
 for x in [2,4,6,8]:furn(b,x,1,5,'firewood_bundle');furn(b,x,1,6,'cloth_bag')
 marker(b,8,1,2,'loot:warehouse');save(b)

def towers():
 for name,height,slot,zone in [('roof',8,4,0),('open',9,5,1),('tall',11,6,4)]:
  b=blue.Blueprint('tower_'+name,(7,height+2,7));b.box((0,0,0),(6,0,6),'cobblestone')
  for x in [0,6]:
   for z in [0,6]:b.box((x,1,z),(x,height,z),'slavicmyths:pine_log',axis='y')
  b.box((0,height-3,0),(6,height-3,6),'slavicmyths:pine_planks')
  for x in range(7):
   for z in range(7):
    if x in [0,6]or z in [0,6]:block(b,x,height-2,z,'pine_fence',north='true',south='true',east='true',west='true',waterlogged='false')
  for y in range(1,height-2):b.put(1,y,0,'ladder',facing='north');block(b,1,y,1,'pine_planks')
  b.put(1,height-3,0,'air');b.put(1,height-2,0,'air')
  if name!='open':b.box((0,height,0),(6,height,6),'slavicmyths:pine_slab',type='bottom',waterlogged='false')
  else:b.box((0,height,3),(3,height,6),'slavicmyths:willow_slab',type='bottom',waterlogged='false')
  lantern(b,5,height-2,5);marker(b,1,height-2,5,'loot:barracks');spawn(b,3,height-2,3,'archer',slot,zone,0);save(b)
 b=blue.Blueprint('gate',(12,10,8));b.box((0,0,0),(11,0,7),'cobblestone');b.box((4,1,0),(7,4,7),'air')
 for x in [1,2,9,10]:b.box((x,1,1),(x,6,5),'slavicmyths:pine_log',axis='y')
 b.box((0,5,0),(11,5,7),'slavicmyths:pine_planks');b.box((0,8,0),(11,8,7),'slavicmyths:pine_slab',type='bottom',waterlogged='false')
 for y in range(1,7):b.put(0,y,2,'ladder',facing='west');b.put(0,5,2,'air')
 marker(b,3,1,6,'bell:0');spawn(b,5,1,4,'ataman_gate',0,0,0);spawn(b,8,1,6,'heavy',2,0,0);spawn(b,4,6,3,'archer',3,0,0);save(b)

def yards():
 b=blue.Blueprint('central_yard',(19,6,12));b.box((0,0,0),(18,0,11),'coarse_dirt');b.box((0,1,0),(18,4,11),'air');b.put(8,1,5,'campfire',lit='true',signal_fire='false',waterlogged='false',facing='north')
 for x in [3,4,5]:furn(b,x,1,3,'table');furn(b,x,1,2,'bench');furn(b,x,1,4,'bench')
 furn(b,13,1,4,'training_dummy');b.put(16,1,7,'target');furn(b,13,1,8,'wooden_crate');marker(b,1,1,9,'bell:2');spawn(b,12,1,3,'fighter',23,2,2);spawn(b,15,1,3,'archer',24,2,2);save(b)
 b=blue.Blueprint('nightingale_yard',(33,27,31));b.box((0,0,0),(32,0,30),'coarse_dirt');b.box((0,1,0),(32,23,30),'air')
 # Keep the edges irregular; this is a lived-in yard, not an 80-block terrain platform.
 for x in range(33):
  for z in range(31):
   if ((x-16)/18)**2+((z-15)/17)**2>1:b.blocks.pop((x,0,z),None)
 # Old pine: thick trunk, face-connected boughs, open lower canopy, empty platform.
 b.box((15,1,14),(17,21,16),'slavicmyths:pine_log',axis='y')
 for dx,dz,y in [(7,0,13),(-6,0,16),(0,7,17),(0,-6,19)]:
  b.box((min(16,16+dx),y,min(15,15+dz)),(max(16,16+dx),y,max(15,15+dz)),'slavicmyths:pine_log',axis='x'if dx else'z')
  for x in range(16+dx-3,16+dx+4):
   for z in range(15+dz-3,15+dz+4):
    for yy in [y,y+1]:
     if (x-16-dx)**2+(z-15-dz)**2<=9:block(b,x,yy,z,'pine_leaves',distance='1',persistent='true')
 b.box((11,7,10),(21,7,20),'slavicmyths:pine_planks');b.box((15,7,14),(17,7,16),'slavicmyths:pine_log',axis='y')
 for x in [11,21]:
  b.box((x,1,10),(x,10,10),'slavicmyths:pine_log',axis='y');b.box((x,1,20),(x,7,20),'slavicmyths:pine_log',axis='y')
 b.box((11,11,10),(21,11,13),'slavicmyths:pine_slab',type='bottom',waterlogged='false')
 for y in range(1,8):b.put(14,y,14,'ladder',facing='west')
 b.put(14,7,14,'air')
 for x,z in [(5,6),(26,7),(5,24)]:b.box((x,1,z),(x+2,2,z+2),'cobblestone')
 for x in range(22,28):block(b,x,1,23,'pine_log',axis='x')
 for x in [5,7,9]:block(b,x,1,15,'pine_log',axis='y');b.put(x,2,15,'target')
 b.put(7,2,15,'air');furn(b,25,1,15,'wooden_crate');furn(b,26,1,16,'cloth_bag');marker(b,20,8,11,'loot:feather');marker(b,26,1,18,'loot:feather');spawn(b,7,1,19,'senior',25,4,1)
 for tx,tz in [(3,27),(29,3)]:
  b.box((tx,1,tz),(tx,4,tz),'slavicmyths:rowan_log',axis='y')
  for dx,dz in [(-1,0),(0,-1),(0,0),(1,0),(0,1),(-1,1)]:
   block(b,tx+dx,4,tz+dz,'rowan_leaves',distance='2',persistent='false',berries='true'if dx==1 else'false')
   block(b,tx+dx,5,tz+dz,'rowan_leaves',distance='3',persistent='false',berries='false')
 for tx,tz in [(3,27),(29,3)]:block(b,tx,4,tz,'rowan_log',axis='y')
 # A low cart and secondary weathered canopy form physical cover.
 b.box((3,2,10),(7,2,12),'slavicmyths:pine_planks')
 for x in [3,7]:
  for z in [9,13]:b.put(x,1,z,'grindstone',face='floor',facing='north')
 b.box((25,4,25),(30,4,29),'slavicmyths:willow_slab',type='bottom',waterlogged='false')
 for x in [25,30]:b.box((x,1,29),(x,3,29),'slavicmyths:willow_log',axis='y')
 save(b)
 for family in ['forest','plains','hill']:
  b=blue.Blueprint('perimeter_'+family,(82,9,80))
  # Broken outline with a four-block entrance and rear service gap; not a rectangular wall.
  points={'forest':[(3,3),(31,3),(35,7),(61,3),(78,11),(79,39),(76,72),(53,78),(28,75),(4,63)],'plains':[(2,4),(32,4),(36,7),(67,4),(79,15),(80,56),(71,76),(41,78),(7,69),(2,31)],'hill':[(6,3),(32,3),(36,7),(61,5),(77,18),(78,44),(70,76),(38,78),(8,64),(3,30)]}[family]
  for (ax,az),(bx,bz) in zip(points,points[1:]+points[:1]):
   steps=max(abs(bx-ax),abs(bz-az))
   for t in range(steps+1):
    x=round(ax+(bx-ax)*t/max(1,steps));z=round(az+(bz-az)*t/max(1,steps))
    if 37<=x<=41 and z<12 or 71<=x<=75 and 69<=z<=75:continue
    h=3+(x*3+z)%3
    for y in range(1,h+1):block(b,x,y,z,'pine_log',axis='y')
    if (x+z)%9==0 and x>1:block(b,x-1,2,z,'pine_planks')
  # Winding routes between functional districts, only a few blocks wide.
  routes=[[(40,10),(39,13),(37,27),(37,42),(38,53)],[(10,29),(24,28),(37,27),(57,27),(71,27)],[(21,43),(24,45),(37,42),(50,42)],[(13,61),(23,61),(37,60)]]
  for route in routes:
   for (ax,az),(bx,bz) in zip(route,route[1:]):
    count=max(abs(bx-ax),abs(bz-az))
    for t in range(count+1):
     x=round(ax+(bx-ax)*t/max(1,count));z=round(az+(bz-az)*t/max(1,count))
     for dx in [-1,0,1]:b.put(x+dx,0,z,['grass_path','coarse_dirt','gravel'][(x+z+dx)%3])
  # Felled timber outside the working area softens the transition into woodland.
  for x,z in [(4,72),(75,4),(18,5)]:block(b,x,1,z,'pine_log',axis='y')
  save(b)
 for variant in [0,1]:
  b=blue.Blueprint('cache_'+str(variant),(7,7,7));b.box((0,0,0),(6,5,6),'cobblestone');b.box((1,1,1),(5,4,5),'air');marker(b,4,1,4,'loot:cache');furn(b,2,1,4,'wooden_crate');furn(b,4,1,2,'cloth_bag')
  for y in range(1,6):b.put(1,y,1,'ladder',facing='south');b.put(1,y,0,'cobblestone')
  block(b,1,5,1,'linden_trapdoor',facing='north',half='top',open='false',powered='false',waterlogged='false');b.put(1,6,1,'brown_carpet');save(b)

def pool(items,rolls=3):return {'rolls':rolls,'entries':[{'type':'minecraft:item','name':n,'weight':w,'functions':[{'function':'minecraft:set_count','count':{'min':a,'max':b}}]}for n,w,a,b in items]}
def data():
 groups={'barracks':[('minecraft:arrow',5,6,18),('minecraft:iron_sword',1,1,1),('slavicmyths:iron_rings',3,2,7),('slavicmyths:gambeson',1,1,1)],'forge':[('minecraft:iron_ingot',5,2,7),('minecraft:coal',6,3,10),('slavicmyths:iron_rings',3,2,8),('slavicmyths:silver_fitting',1,1,2)],'warehouse':[('minecraft:leather',4,2,6),('minecraft:string',4,2,7),('minecraft:iron_ingot',2,1,4),('minecraft:bread',3,2,5),('slavicmyths:ancient_coin',1,1,4)],'kitchen':[('minecraft:bread',5,2,7),('minecraft:cooked_beef',2,1,4),('minecraft:carrot',4,2,8),('slavicmyths:rowan_berries',3,2,5)],'ataman':[('minecraft:iron_ingot',4,3,8),('minecraft:gold_ingot',2,1,3),('slavicmyths:ancient_coin',3,3,8),('slavicmyths:mace',1,1,1)],'feather':[('slavicmyths:mysterious_black_feather',1,1,1)]}
 for name,items in groups.items():write(D/f'loot_tables/chests/stronghold_{name}.json',{'type':'minecraft:chest','pools':[pool(items,1 if name=='feather'else 3)]})
 probabilities=[('minecraft:iron_ingot',.7,3,9),('minecraft:gold_ingot',.45,2,6),('slavicmyths:silver_ingot',.45,2,7),('minecraft:diamond',.23,1,2),('minecraft:emerald',.35,2,7),('slavicmyths:ancient_coin',.5,4,14),('slavicmyths:rune_protection',.2,1,1),('slavicmyths:rune_thunder',.06,1,1),('slavicmyths:veles_amulet',.1,1,1),('slavicmyths:resin_ring',.12,1,1),('slavicmyths:depth_amulet',.08,1,1),('slavicmyths:silver_mace',.05,1,1)]
 pools=[]
 for name,chance,a,b in probabilities:
  p=pool([(name,1,a,b)],1);p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}];pools.append(p)
 # Common rune roll chooses among existing runes, rather than always giving the same one.
 pools[6]['entries']=[{'type':'minecraft:item','name':'slavicmyths:rune_'+r}for r in ['heat','forest','midday','shadow','protection','life','wind']]
 write(D/'loot_tables/chests/stronghold_cache.json',{'type':'minecraft:chest','pools':pools})
 for name,ru,en in [('find_large_camp','Разбойничий стан','Bandit Stronghold'),('clear_large_camp','Кто здесь хозяин?','Who Rules Here?')]:
  crit={'visit':{'trigger':'minecraft:location','conditions':{'location':{'feature':'slavicmyths:bandit_camp_large'}}}}if name.startswith('find')else{'clear':{'trigger':'minecraft:impossible'}}
  write(D/f'advancements/{name}.json',{'parent':'slavicmyths:bad_people'if name.startswith('find')else'slavicmyths:find_large_camp','display':{'icon':{'item':'slavicmyths:pine_log'if name.startswith('find')else'slavicmyths:mace'},'title':{'translate':f'advancement.slavicmyths.{name}.title'},'description':{'translate':f'advancement.slavicmyths.{name}.description'},'frame':'task'if name.startswith('find')else'challenge','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':crit})
  for lang,title in [('ru_ru',ru),('en_us',en)]:
   path=A/f'lang/{lang}.json';v=json.loads(path.read_text(encoding='utf-8'));v[f'advancement.slavicmyths.{name}.title']=title;v[f'advancement.slavicmyths.{name}.description']=('Найдите большой укреплённый стан'if name.startswith('find')else'Победите обоих Атаманов и не менее 18 основных защитников')if lang=='ru_ru'else('Find a large fortified settlement'if name.startswith('find')else'Defeat both atamans and at least 18 main defenders');write(path,v)

def generate():
 old=blue.OUT;blue.OUT=D/'structures/stronghold'
 try:buildings();towers();yards()
 finally:blue.OUT=old
 data();write(ROOT/'docs/verification/stronghold-0.8.1.json',MANIFEST)
if __name__=='__main__':generate()
