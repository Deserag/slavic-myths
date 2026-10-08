"""Authored Slavic village templates for 1.21.1. Run after earlier overlays."""
from pathlib import Path
import gzip,io,struct,json,zipfile,re
import swamp_073 as n
ROOT=Path(__file__).resolve().parents[1]; RES=ROOT/'src/main/resources'
JAR=ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
PALETTES={'temperate':('oak','spruce','dark_oak','cobblestone'),'cold':('spruce','dark_oak','spruce','stone_bricks'),'warm':('oak','birch','oak','sandstone')}
JOBS={'chronicler_terem':('librarian','lectern'),'herbalist_house':('cleric','brewing_stand'),'cartographer_house':('cartographer','cartography_table'),'fisher_house':('fisherman','barrel'),'fletcher_house':('fletcher','fletching_table'),'shepherd_house':('shepherd','loom'),'tanner_house':('leatherworker','cauldron'),'butcher_house':('butcher','smoker'),'mason_workshop':('mason','stonecutter'),'toolsmith_forge':('toolsmith','smithing_table'),'weaponsmith_forge':('weaponsmith','grindstone'),'armorer_forge':('armorer','blast_furnace'),'miller_house':('slavicmyths:miller','slavicmyths:millstone'),'brewer_house':('slavicmyths:brewer','slavicmyths:fermentation_vat'),'weaver_house':('slavicmyths:weaver','slavicmyths:loom_table'),'hlev':('slavicmyths:herder','slavicmyths:feeder'),'hunter_house':('slavicmyths:hunter','slavicmyths:drying_rack'),'cook_house':('slavicmyths:cook','slavicmyths:kitchen_table'),'druzhinnik_barracks':('slavicmyths:druzhinnik','slavicmyths:druzhinnik_table'),'small_field':('farmer','composter'),'large_field':('farmer','composter')}
LOOT={'home':['bread','apple','wheat_seeds'],'chronicler_terem':['book','paper','feather','ink_sac','map','charcoal'],'herbalist_house':['brown_mushroom','red_mushroom','honey_bottle','glass_bottle','charcoal'],'cartographer_house':['paper','map','feather','charcoal'],'fisher_house':['cod','salmon','string','bowl'],'fletcher_house':['arrow','feather','stick','flint','string'],'shepherd_house':['white_wool','string','dandelion','wheat'],'tanner_house':['leather','rabbit_hide','brown_dye'],'butcher_house':['beef','porkchop','cooked_chicken','charcoal','bread'],'mason_workshop':['cobblestone','stone','brick','clay_ball'],'toolsmith_forge':['coal','iron_nugget','stick'],'weaponsmith_forge':['coal','iron_nugget','arrow'],'armorer_forge':['coal','iron_nugget','leather'],'miller_house':['wheat','slavicmyths:rye_grain','slavicmyths:wheat_flour','bread'],'brewer_house':['wheat','honey_bottle','glass_bottle','slavicmyths:raspberry'],'weaver_house':['white_wool','string','slavicmyths:flax_fiber','dandelion'],'hlev':['wheat','hay_block','leather','white_wool'],'hunter_house':['arrow','feather','rabbit_hide','beef'],'cook_house':['slavicmyths:wheat_flour','bowl','carrot','brown_mushroom','bread'],'ambar':['wheat','wheat_seeds','slavicmyths:rye_grain','bread','hay_block'],'klet':['wheat_seeds','string','apple','honey_bottle'],'stable':['hay_block','wheat','leather'],'small_field':['wheat_seeds','carrot','potato'],'large_field':['wheat','slavicmyths:rye_seeds','slavicmyths:flax_seeds'],'druzhinnik_barracks':['bread','arrow','torch','leather','iron_nugget']}
RARE={'chronicler_terem':('emerald',.008),'cartographer_house':('compass',.035),'fisher_house':('fishing_rod',.04),'fletcher_house':('bow',.025),'shepherd_house':('shears',.035),'tanner_house':('bucket',.04),'toolsmith_forge':('iron_pickaxe',.02),'weaponsmith_forge':('iron_sword',.02),'armorer_forge':('iron_helmet',.015),'hunter_house':('bow',.03),'hlev':('lead',.025),'stable':('saddle',.025),'klet':('emerald',.003)}
LOOT['home']+=['slavicmyths:rye_bread','slavicmyths:oat_groats','slavicmyths:dried_berries','slavicmyths:linen_thread']
LOOT['brewer_house']+=['slavicmyths:malted_barley','slavicmyths:hops','slavicmyths:wooden_mug','slavicmyths:kvass_mug']
LOOT['weaver_house']+=['slavicmyths:linen_thread','slavicmyths:linen_cloth']
LOOT['cook_house']+=['slavicmyths:barley_groats','slavicmyths:dried_mushrooms']
def write(rel,d):
 p=RES/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def encode(v):
 if isinstance(v,float):return 6,struct.pack('>d',v)
 if isinstance(v,list):
  tags=[encode(x) for x in v];kind=tags[0][0] if tags else 10
  assert all(t[0]==kind for t in tags)
  return 9,bytes([kind])+struct.pack('>i',len(tags))+b''.join(t[1] for t in tags)
 if isinstance(v,dict):return 10,b''.join(bytes([encode(x)[0]])+n.string(k)+encode(x)[1] for k,x in v.items())+b'\0'
 return n.tag(v)
def read_nbt(raw):
 s=io.BytesIO(gzip.decompress(raw))
 def num(f):return struct.unpack('>'+f,s.read(struct.calcsize('>'+f)))[0]
 def string():return s.read(num('H')).decode()
 def val(k):
  if k in [1,2,3,4,5,6]:return num({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[k])
  if k==8:return string()
  if k==9:
   t,count=num('B'),num('i');return [val(t) for _ in range(count)]
  if k==10:
   d={}
   while True:
    t=num('B')
    if not t:return d
    key=string();d[key]=val(t)
  if k in [7,11,12]:return [num({7:'b',11:'i',12:'q'}[k]) for _ in range(num('i'))]
  raise ValueError(k)
 assert num('B')==10;string();return val(10)
def document(b,entities=None):
 palette=[];lookup={};blocks=[]
 for pos,(state,tag) in sorted(b.blocks.items(),key=lambda e:(e[0][1],e[0][2],e[0][0])):
  key=json.dumps(state,sort_keys=True)
  if key not in lookup:lookup[key]=len(palette);palette.append(state)
  row={'pos':list(pos),'state':lookup[key]}
  if tag:row['nbt']=tag
  blocks.append(row)
 return {'DataVersion':3955,'size':list(b.size),'palette':palette,'blocks':blocks,'entities':entities or []}
def save(rel,b,entities=None):
 d=document(b,entities);p=RES/('data/slavicmyths/structure/'+rel+'.nbt');p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(gzip.compress(b'\x0a\0\0'+encode(d)[1],mtime=0));return d
def be(b,x,y,z,block,tag,**props):
 b.put(x,y,z,block,**props);state,_=b.blocks[x,y,z];b.blocks[x,y,z]=(state,tag)
def stairs(b,x,y,z,wood,facing):b.put(x,y,z,wood+'_stairs',facing=facing,half='bottom',shape='straight',waterlogged=False)
def slab(b,x,y,z,wood):b.put(x,y,z,wood+'_slab',type='bottom',waterlogged=False)
def lamp(b,x,y,z,hanging=True):b.put(x,y,z,'lantern',hanging=hanging,waterlogged=False)
def fence(b,x,y,z,wood):b.put(x,y,z,wood+'_fence',north=False,south=False,east=True,west=True,waterlogged=False)
def entrance(b,x,y):be(b,x,y,0,'jigsaw',{'id':'minecraft:jigsaw','name':'minecraft:building_entrance','target':'minecraft:street','pool':'minecraft:empty','final_state':'minecraft:dirt_path','joint':'rollable','selection_priority':0,'placement_priority':0},orientation='north_up')
def loot(b,x,y,z,kind):be(b,x,y,z,'chest',{'id':'minecraft:chest','LootTable':'slavicmyths:chests/village/'+kind},facing='west',type='single',waterlogged=False)
def bed(b,x,y,z,color='red'):
 b.put(x,y,z+1,color+'_bed',facing='north',part='foot',occupied=False);b.put(x,y,z,color+'_bed',facing='north',part='head',occupied=False)
def entity(x,y,z,id='villager',role=None,zombie=False):
 tag={'id':'minecraft:'+('zombie_villager' if zombie and id=='villager' else id),'PersistenceRequired':1}
 if role:tag['VillagerData']={'type':'minecraft:plains','profession':role if ':' in role else 'minecraft:'+role,'level':1}
 return {'pos':[x+.5,float(y),z+.5],'blockPos':[x,y,z],'nbt':tag}
def roof(b,x0,x1,z0,z1,y,wood,cold=False,turf=False):
 mid=(x0+x1)//2
 for x in range(x0-1,x1+2):
  distance=min(x-(x0-1),(x1+1)-x);h=y+distance+(distance//2 if cold else 0)
  for z in range(z0-1,z1+2):
   if cold:
    for riser in range(y+distance,h):b.put(x,riser,z,wood+'_planks')
   if x==mid:slab(b,x,h,z,wood)
   else:stairs(b,x,h,z,wood,'east' if x<mid else 'west')
   if turf and x not in [x0-1,x1+1]:b.put(x,h+1,z,'grass_block',snowy=False)
   if cold and x==mid:b.put(x,h+1,z,'snow',layers=1)
 for z in [z0,z1]:
  for x in range(x0,x1+1):
   distance=min(x-(x0-1),(x1+1)-x);h=y+distance+(distance//2 if cold else 0)
   for yy in range(y,h):b.put(x,yy,z,wood+'_planks')
 # Structural horizontal rafters tie both eaves to the ridge, above head clearance.
 for z in [z0,z1,(z0+z1)//2]:b.box((x0,y-1,z),(x1,y-1,z),'stripped_'+wood+'_log',axis='x')
 return y+(x1-x0+2)//2+(((x1-x0+2)//2)//2 if cold else 0)

def house(kind,palette,variant=1,zombie=False):
 frame,wall,roofwood,stone=PALETTES[palette];cold=palette=='cold';warm=palette=='warm'
 small=kind=='seni';dug=kind=='zemlyanka';big=kind in ['izba','chronicler_terem','druzhinnik_barracks'];storage=kind in ['ambar','klet'];animal=kind in ['stable','hlev'];forge=kind.endswith('forge')
 w=5 if small else 7 if kind=='klet' else 9 if big or kind in ['mason_workshop','toolsmith_forge','weaponsmith_forge','armorer_forge','cook_house','weaver_house'] else 7
 length=6+(variant-1) if small else 8+(variant-1) if dug else 11+(variant-1) if big else 10 if kind in ['brewer_house','ambar','stable','cook_house'] else 8
 if kind=='druzhinnik_barracks':w=11;length=13
 x0,z0=2,4;x1=x0+w-1;z1=z0+length-1;cx=(x0+x1)//2;floor=1;fy=2;surface=3 if dug else 1
 yard=5 if animal else 3;extra=5 if kind=='druzhinnik_barracks' else 0
 b=n.Blueprint(kind,(w+4+extra,32,length+4+yard));b.box((x0,0,z0),(x1,0,z1),'cobblestone');b.box((x0,1,z0),(x1,1,z1),frame+'_planks');b.box((x0,2,z0),(x1,12,z1),'air')
 for z in [z0,z1]:b.box((x0,2,z),(x1,5,z),wall+'_planks')
 for x in [x0,x1]:b.box((x,2,z0),(x,5,z1),wall+'_planks')
 for x in [x0,x1]:
  for z in [z0,z1]+([z0+length//2] if length>=9 else []):b.box((x,1,z),(x,6,z),'stripped_'+frame+'_log',axis='y')
 for z in [z0,z1]:b.box((x0,5,z),(x1,5,z),frame+'_log',axis='x')
 for x in [x0,x1]:b.box((x,5,z0),(x,5,z1),frame+'_log',axis='z')
 for x in [x0,x1]:
  for z in ([z0+3] if cold else [z0+2,z1-2]):
   b.put(x,3,z,'glass_pane',north=True,south=True,east=False,west=False,waterlogged=False)
   b.put(x,3,z-1,'glass_pane',north=True,south=True,east=False,west=False,waterlogged=False)
 b.put(cx,3,z1,'glass_pane',north=False,south=False,east=True,west=True,waterlogged=False)
 ridge=roof(b,x0,x1,z0,z1,6,roofwood,cold,dug)
 # Supported north porch, three wide; cold has a closed vestibule, warm wider canopy.
 for x in range(cx-1,cx+2):
  b.box((x,0,1),(x,1,z0-1),'cobblestone');b.box((x,1,1),(x,1,z0-1),frame+'_planks')
  stairs(b,x,0,0,'cobblestone','south')
  for z in range(1,z0):slab(b,x,5,z,roofwood)
 for x in [cx-1,cx+1]:b.box((x,2,1),(x,4,1),'stripped_'+frame+'_log',axis='y')
 for y in [2,3]:b.put(cx,y,z0,frame+'_door',facing='north',half='lower' if y==2 else 'upper',hinge='left',open=False,powered=False)
 lamp(b,cx+1,4,2)
 if cold:
  for x in [cx-1,cx+1]:b.box((x,2,2),(x,4,3),wall+'_planks')
 if warm:
  for x in [cx-2,cx+2]:
   b.box((x,0,1),(x,1,3),'cobblestone');b.box((x,1,1),(x,1,3),frame+'_planks');b.box((x,2,1),(x,4,1),frame+'_log',axis='y')
   for z in range(1,4):slab(b,x,5,z,roofwood)
 if dug:
  # Retaining earth shoulders rise to the road datum; floor remains two blocks below.
  for x in [x0-1,x1+1]:b.box((x,0,z0),(x,3,z1),'dirt');b.box((x,3,z0),(x,3,z1),'grass_block',snowy=False)
  for x in range(cx-1,cx+2):
   stairs(b,x,3,0,'cobblestone','north');stairs(b,x,2,1,frame,'north');stairs(b,x,1,2,frame,'north')
  b.box((cx-1,5,1),(cx+1,5,3),'air')
  b.box((cx-1,6,1),(cx+1,6,3),roofwood+'_planks')
  for xx in [cx-1,cx+1]:b.put(xx,5,1,frame+'_log',axis='y')
  # Explicit entrance excavation is also required in natural underground placement.
  for zz in [1,2,3]:
   b.box((cx,3 if zz==1 else 2,zz),(cx,5,zz),'air')
 entrance(b,cx,surface)
 beds=0 if storage or kind=='stable' else 1 if kind in JOBS and kind!='druzhinnik_barracks' else 1+(variant%2) if small else 2+(variant%2) if dug else 4+(variant%2) if big else 1
 for i in range(beds):
  bx=x0+1+i if i<min(beds,w-3) else x1-1
  bz=z1-2 if i<min(beds,w-3) else z0+2
  bed(b,bx,fy,bz,'blue' if cold else 'red' if i%2==0 else 'white')
 # Hearth has a stone chimney capped above the ridge; no barrel/cauldron unintended POIs.
 if not storage and not animal:
  b.box((x0+1,fy,z0+1),(x0+(1 if small else 2),fy+1,z0+2),'stone_bricks');b.put(x0+1,fy,z0+2,'furnace',facing='south',lit=False)
  b.box((x0+1,fy+2,z0+1),(x0+1,ridge+2,z0+1),'stone_bricks');slab(b,x0+1,ridge+3,z0+1,'stone_brick')
 # Table stays off the two-wide entrance aisle. Dedicated mod furniture, no incidental POIs.
 tablex=x1-1 if small else x1-2;tablez=z0+2 if small else z0+4
 b.put(tablex,fy,tablez,'slavicmyths:pine_table',facing='north',connection='none')
 b.put(tablex,fy,tablez+1,'slavicmyths:pine_stool',facing='north')
 b.put(x1-1,fy,z0+1,'slavicmyths:wooden_barrel',facing='north',brewing=False)
 b.put(x1-1,fy+1,z0+2,'slavicmyths:pine_shelf',facing='west')
 loot(b,x1-1,fy,z1-1,kind if kind in LOOT else 'home')
 if not small:b.put(cx,fy,z0+3,'red_carpet')
 for lz in [z0+3,z1-3]:
  b.box((x0,5,lz),(x1,5,lz),frame+'_log',axis='x');lamp(b,cx,4,lz)
 b.put(x0-1,1,z1,'slavicmyths:firewood_bundle',facing='north')
 b.put(x1+1,1,z1,'slavicmyths:wooden_crate',facing='north')
 work=None;role=None
 if kind in JOBS:
  role,block=JOBS[kind];work=[x1-1,fy,z0+3]
  props={'facing':'west'} if block not in ['brewing_stand','cauldron','composter','cartography_table','fletching_table','smithing_table','slavicmyths:millstone'] else {}
  if block=='grindstone':props={'face':'floor','facing':'west'}
  if block in ['smoker','blast_furnace']:props['lit']=False
  if block=='barrel':props.update(open=False)
  if block=='slavicmyths:kitchen_table':
   work=[x1-2,fy,z0+3];b.put(*work,block,facing='north',part='left');b.put(x1-1,fy,z0+3,block,facing='north',part='right')
  elif block=='slavicmyths:drying_rack':
   work=[x1-2,fy,z0+3]
   for part in range(4):b.put(x1-2+part%2,fy+part//2,z0+3,block,facing='north',part=part)
  else:b.put(*work,block,**props)
 # Individually authored profession work walls, annexes and external cues.
 if kind=='chronicler_terem':
  for z in range(z0+2,z1-3):b.put(x0+1,fy,z,'bookshelf');b.put(x0+1,fy+1,z,'bookshelf')
  b.put(cx,6,z0,'red_banner',rotation=8);b.put(cx,7,z0,'white_banner',rotation=8)
  # Gallery fronton projects beyond the main gable with its own pitched roof.
  roof(b,cx-1,cx+1,1,3,6,roofwood,cold)
 if kind=='herbalist_house':
  for z in range(z0+2,z1-3):b.put(x0-1,1,z,'grass_block',snowy=False);b.put(x0-1,2,z,'poppy' if z%2 else 'dandelion')
  for z in [z0+3,z1-3]:b.put(x0+1,fy+1,z,'potted_brown_mushroom')
 if kind=='cartographer_house':b.put(x0+1,fy+1,z0+3,'white_banner',rotation=4);b.put(x0-1,1,z0+2,'slavicmyths:cloth_bag',facing='east')
 if kind=='fletcher_house':b.put(x0-1,1,z0+2,'target');b.put(x0-1,2,z0+3,'slavicmyths:pine_weapon_rack',facing='east')
 if kind in ['weaver_house','shepherd_house','tanner_house']:
  for z in range(z0+2,z0+5):
   b.put(x0-1,3,z,'white_wool' if z%2 else 'red_wool');b.put(x0-1,4,z,frame+'_log',axis='z')
  for z in [z0+1,z0+5]:b.box((x0-1,1,z),(x0-1,4,z),frame+'_log',axis='y')
 if forge:
  b.box((x0,2,z0),(x0+2,3,z0),'stone_bricks');b.put(x0+2,fy,z0+3,'coal_block');b.put(x0+2,fy,z0+4,'anvil',facing='north')
  b.put(x0-1,1,z0+3,'stone_bricks');b.put(x0-1,2,z0+3,'slavicmyths:pine_weapon_rack',facing='east')
 if kind=='mason_workshop':
  for z in range(z0+2,z0+5):b.put(x0-1,1,z,['stone','bricks','cobblestone'][z-z0-2])
 if kind in ['miller_house','brewer_house','cook_house','ambar','klet']:
  for z in [z0+3,z0+4]:b.put(x0+1,fy,z,'slavicmyths:storage_sack',facing='east',fill=0)
  if kind=='ambar':b.put(x0+1,fy,z1-2,'slavicmyths:settlement_chest',facing='east');b.put(x0+2,fy,z0+2,'hay_block',axis='y')
 if kind=='hunter_house':b.put(x0-1,1,z0+2,'slavicmyths:pine_weapon_rack',facing='east')
 if kind=='fisher_house':
  b.box((x0-1,0,z0+2),(x0-1,0,z0+5),'water');b.box((x0-1,1,z0+2),(x0-1,1,z0+5),frame+'_planks');b.put(x0-1,2,z0+3,'slavicmyths:wooden_barrel',facing='east',brewing=False)
 entities=[]
 if animal:
  # Three stalls flank a wide central route; covered water trough is not a job site.
  for z in [z0+2,z0+5,z0+8] if length>=10 else [z0+2,z0+5]:
   for x in [x0+1,x1-1]:
    if [x,fy,z]!=work:b.put(x,fy,z,'hay_block',axis='y')
    if [x,fy,z+1]!=work:fence(b,x,fy,z+1,frame)
  for x in range(x0,x1+1):fence(b,x,1,z1+3,frame)
  for x in [x0,x1]:
   for z in range(z1+1,z1+4):fence(b,x,1,z,frame)
  for xx in [cx,cx+1]:b.put(xx,1,z1+3,frame+'_fence_gate',facing='north',open=False,powered=False,in_wall=False)
  b.box((cx,2,z1),(cx+1,3,z1),'air');b.put(x0+2,fy-1,z0+3,'water');b.put(x0+2,fy-2,z0+3,'stone_bricks')
  entities=[entity(x0+2,fy,z1-2,'horse' if kind=='stable' else 'cow'),entity(x1-2,fy,z1-2,'horse' if kind=='stable' else 'sheep')]
  if kind=='hlev':bed(b,x0+1,fy,z1-2)
 if kind=='druzhinnik_barracks':
  tx=x1+1;tz=z1-4;top=ridge+4
  b.box((tx,0,tz),(tx+3,0,tz+3),'stone_bricks');b.box((tx,1,tz),(tx+3,top-1,tz+3),wall+'_planks');b.box((tx+1,2,tz+1),(tx+2,top-1,tz+2),'air')
  for x in [tx,tx+3]:
   for z in [tz,tz+3]:b.box((x,1,z),(x,top,z),'stripped_'+frame+'_log',axis='y')
  b.box((x1,fy,tz+1),(tx,fy+2,tz+1),'air')
  for y in range(fy,top):b.put(tx+2,y,tz+1,'ladder',facing='south',waterlogged=False)
  b.box((tx,top-1,tz),(tx+3,top-1,tz+3),frame+'_planks');b.put(tx+2,top-1,tz+1,'ladder',facing='south',waterlogged=False)
  b.box((tx+1,top,tz+1),(tx+2,top+2,tz+2),'air')
  for x in range(tx,tx+4):
   for z in range(tz,tz+4):
    if (x in [tx,tx+3] or z in [tz,tz+3]) and not (x in [tx,tx+3] and z in [tz,tz+3]):fence(b,x,top,z,frame)
  # Continuous corner posts connect the observation deck to its roof.
  for x in [tx,tx+3]:
   for z in [tz,tz+3]:b.box((x,top,z),(x,top+2,z),frame+'_log',axis='y')
  for x in range(tx,tx+4):
   for z in range(tz,tz+4):stairs(b,x,top+(2 if x in [tx,tx+3] else 3),z,roofwood,'east' if x<tx+2 else 'west')
  b.box((tx,top+1,tz),(tx+3,top+1,tz),frame+'_log',axis='x');b.put(tx+1,top+1,tz-1,'red_wall_banner',facing='north');b.put(tx+2,top+1,tz-1,'white_wall_banner',facing='north')
  b.put(x1-1,fy,z0+6,'slavicmyths:druzhinnik_table',facing='west')
  b.put(x0+1,fy,z0+4,'slavicmyths:pine_weapon_rack',facing='east');b.put(x0+1,fy,z0+5,'slavicmyths:training_dummy',facing='east')
  for x in range(x0,x1+1):fence(b,x,1,z1+2,frame)
  b.put(cx,1,z1+2,frame+'_fence_gate',facing='north',open=False,powered=False,in_wall=False)
  lamp(b,tx+1,top+2,tz+1)
 # Each working annex has posts, a descending roof and a distinct use.
 if kind in ['brewer_house','miller_house','cook_house','hunter_house','herbalist_house','tanner_house','weaver_house','mason_workshop'] or forge:
  for zz in [z0+2,z0+5]:b.box((x1+2,0,zz),(x1+2,3,zz),'stripped_'+frame+'_log',axis='y')
  for zz in range(z0+2,z0+6):
   stairs(b,x1+1,5,zz,roofwood,'west');stairs(b,x1+2,4,zz,roofwood,'west')
   b.put(x1+1,1,zz,'cobblestone')
  if forge:b.put(x1+1,2,z0+3,'anvil',facing='north')
  elif kind=='mason_workshop':b.put(x1+1,2,z0+3,'bricks')
  elif kind in ['weaver_house','tanner_house']:b.put(x1+1,2,z0+3,'white_banner',rotation=4)
  elif kind in ['brewer_house','cook_house']:b.put(x1+1,2,z0+3,'slavicmyths:wooden_barrel',facing='east',brewing=False)
  elif kind=='hunter_house':b.put(x1+1,2,z0+3,'slavicmyths:pine_weapon_rack',facing='east')
  elif kind=='herbalist_house':b.put(x1+1,2,z0+3,'potted_fern')
  else:b.put(x1+1,2,z0+3,'slavicmyths:storage_sack',facing='east',fill=0)
 if kind in ['seni','izba','miller_house','hunter_house']:
  for zz in range(z0+1,z1):
   for xx in [x0,x1]:b.put(xx,2,zz,frame+'_log',axis='z')
 spawn=[cx,fy,z0+1]
 if not dug:
  # Raised dry floor: expose the footing cap and keep road connectors at their original datum.
  old=dict(b.blocks);b.blocks={(xx,yy+1,zz):(state,tag) for (xx,yy,zz),(state,tag) in old.items()}
  for (xx,yy,zz),(state,tag) in old.items():
   if yy==0:b.blocks[xx,0,zz]=(state,tag)
  b.blocks.pop((cx,surface+1,0),None);entrance(b,cx,surface)
  fy+=1;spawn[1]+=1
  if work:work[1]+=1
  for ent in entities:ent['pos'][1]+=1;ent['blockPos'][1]+=1
  # Two step entrance rises from road to the raised porch.
  for xx in range(cx-1,cx+2):
   b.put(xx,2,1,'air');stairs(b,xx,1,1,'cobblestone','south')
  for xx in [x0,x1]:
   for zz in range(z0,z1+1):b.put(xx,1,zz,stone if cold else 'cobblestone')
 if beds or role:
  offsets=[1,2,4,5,6]
  for i in range(beds if kind in ['seni','zemlyanka','izba'] else 1):entities.append(entity(cx,fy,z0+offsets[i],role=None,zombie=zombie))
 if zombie:
  for pos,(state,tag) in list(b.blocks.items()):
   if state['Name'].endswith('_door') or state['Name']=='minecraft:lantern':b.put(*pos,'air')
  b.put(x0+1,4,z1-2,'cobweb')
 b.size=(b.size[0],max(p[1] for p in b.blocks)+1,b.size[2])
 return b,entities,{'kind':kind,'size':list(b.size),'surface':surface,'entrance':[cx,surface,0],'spawn':spawn,'beds':beds,'job':role,'workstation':work}

def field(kind,palette):
 frame,wall,r,stone=PALETTES[palette];w=11 if kind=='large_field' else 7;d=13 if kind=='large_field' else 9
 b=n.Blueprint(kind,(w,7,d));b.box((0,0,0),(w-1,0,d-1),'dirt');b.box((0,1,0),(w-1,1,d-1),'dirt_path')
 for x in range(1,w-1):
  for z in range(3,d-1):
   if x==w//2:b.put(x,1,z,'water')
   else:b.put(x,1,z,'farmland',moisture=7);b.put(x,2,z,'slavicmyths:rye_crop' if x%3==0 else 'slavicmyths:flax_crop' if x%3==1 else 'wheat',age=4)
 b.put(1,2,2,'composter',level=0);loot(b,w-2,2,2,kind);entrance(b,w//2,1)
 # Seed shed is connected, supported and roofed, away from crop approach.
 for x in [0,w-1]:b.box((x,1,1),(x,4,2),frame+'_log',axis='y')
 for x in range(w):slab(b,x,5,1,r);slab(b,x,5,2,r)
 lamp(b,w-2,4,1)
 return b,[entity(w//2,2,2)],{'kind':kind,'size':list(b.size),'surface':1,'entrance':[w//2,1,0],'spawn':[w//2,2,2],'beds':0,'job':'farmer','workstation':[1,2,2]}

def center(palette,variant=1,source=None):
 frame,wall,r,stone=PALETTES[palette]
 size=source['size'] if source else [17,12,17];size=[max(9,size[0]),max(12,size[1]),max(9,size[2])]
 b=n.Blueprint('center',tuple(size));sx,sy,sz=size;cx,cz=sx//2,sz//2
 b.box((0,0,0),(sx-1,0,sz-1),'cobblestone');b.box((0,1,0),(sx-1,1,sz-1),'gravel');b.box((0,2,0),(sx-1,sy-1,sz-1),'air')
 # Well, tree or communal canopy differs by original start variant.
 if variant%3==1:
  b.box((cx-1,1,cz-1),(cx+1,2,cz+1),'stone_bricks');b.put(cx,2,cz,'water')
  for x in [cx-1,cx+1]:b.box((x,3,cz),(x,5,cz),frame+'_log',axis='y')
  roof(b,cx-1,cx+1,cz-1,cz+1,6,r)
 elif variant%3==2:
  b.box((cx,2,cz),(cx,6,cz),frame+'_log',axis='y');b.box((cx-2,6,cz-2),(cx+2,7,cz+2),frame+'_leaves',persistent=True,distance=1)
 else:
  for x in [cx-2,cx+2]:
   for z in [cz-2,cz+2]:b.box((x,2,z),(x,4,z),frame+'_log',axis='y')
  roof(b,cx-2,cx+2,cz-2,cz+2,5,r)
 # Generic carved post in EVERY center, no named deity, no gameplay.
 ix,iz=2,2;b.put(ix,2,iz,'stone_bricks');b.box((ix,3,iz),(ix,4,iz),'stripped_'+frame+'_log',axis='y');b.put(ix,5,iz,frame+'_fence');b.put(ix+1,3,iz,'white_banner',rotation=0);b.put(ix,2,iz+1,'slavicmyths:large_basket',facing='north');b.put(ix-1,2,iz,'potted_poppy');lamp(b,ix+1,2,iz+1,False)
 b.put(cx+3,2,cz,'bell',attachment='floor',facing='north',powered=False);b.put(cx-3,2,cz,'slavicmyths:pine_bench',facing='east')
 if source:
  for row in source['blocks']:
   state=source['palette'][row['state']]
   if state['Name']=='minecraft:jigsaw':b.blocks[tuple(row['pos'])]=(state,row.get('nbt'))
 else:entrance(b,cx,1)
 old=dict(b.blocks);b.blocks={(xx,max(0,yy-1),zz):(st,tag) for (xx,yy,zz),(st,tag) in old.items() if yy!=0 and st['Name']!='minecraft:jigsaw'}
 for xx in range(sx):
  for zz in [0,sz-1]:b.put(xx,0,zz,'cobblestone')
 for xx in [0,sx-1]:
  for zz in range(sz):b.put(xx,0,zz,'cobblestone')
 b.box((cx-1,0,cz-1),(cx+1,0,cz+1),'stone_bricks')
 if source:
  for row in source['blocks']:
   state=source['palette'][row['state']]
   if state['Name']=='minecraft:jigsaw':b.blocks[tuple(row['pos'])]=(state,row.get('nbt'))
 else:entrance(b,cx,0)
 return b,[] if source else [entity(cx,1,1),entity(cx+1,1,1)],{'kind':'village_center','size':size,'surface':0,'entrance':[cx,0,0],'spawn':[cx,1,1],'beds':0,'job':None,'workstation':None}

def mapping(location):
 name=location.split('/')[-1]
 pairs=[('meeting_point','village_center'),('small_house','seni'),('medium_house','zemlyanka'),('big_house','izba'),('library','chronicler_terem'),('temple','herbalist_house'),('cartographer','cartographer_house'),('fisher','fisher_house'),('fletcher','fletcher_house'),('shepherd','shepherd_house'),('tannery','tanner_house'),('butcher','butcher_house'),('mason','mason_workshop'),('tool_smith','toolsmith_forge'),('weapon','weaponsmith_forge'),('armorer','armorer_forge'),('large_farm','large_field'),('farm','small_field'),('stable','stable'),('animal_pen','hlev')]
 for token,kind in pairs:
  if token in name:return kind
 raise ValueError('Unmapped vanilla family '+location)
def roads(archive,biome,palette):
 # Vanilla sockets inside a street bounding volume fit only vanilla-sized houses.
 # Extend each entrance spur to that volume's boundary while keeping the street graph unchanged.
 for suffix in ['streets','zombie/streets']:
  rel=f'data/minecraft/worldgen/template_pool/village/{biome}/{suffix}.json'
  if rel not in archive.namelist():continue
  pool=json.loads(archive.read(rel))
  for row in pool['elements']:
   el=row['element'];loc=el.get('location')
   if not loc:continue
   doc=read_nbt(archive.read('data/minecraft/structure/'+loc.split(':')[1]+'.nbt'))
   b=n.Blueprint('road',tuple(doc['size']))
   for block in doc['blocks']:b.blocks[tuple(block['pos'])]=(doc['palette'][block['state']],block.get('nbt'))
   for pos,(state,tag) in list(b.blocks.items()):
    if state['Name']!='minecraft:jigsaw' or not tag or tag.get('target')!='minecraft:building_entrance':continue
    facing=state.get('Properties',{}).get('orientation','').split('_')[0]
    if facing not in ['north','south','east','west']:continue
    x,y,zz=pos;dx,dz={'north':(0,-1),'south':(0,1),'west':(-1,0),'east':(1,0)}[facing]
    qx,qz=x,zz
    while 0<=qx+dx<b.size[0] and 0<=qz+dz<b.size[2]:qx+=dx;qz+=dz
    if (qx,y,qz) in b.blocks and b.blocks[qx,y,qz][0]['Name']=='minecraft:jigsaw' and (qx,y,qz)!=pos:continue
    b.put(*pos,'air')
    ax,az=x,zz
    while True:
     b.put(ax,0,az,'gravel' if palette=='cold' else 'dirt_path')
     if (ax,az)==(qx,qz):break
     if (ax,y,az)!=pos:b.put(ax,y,az,'air')
     ax+=dx;az+=dz
    b.blocks[qx,y,qz]=(state,tag)
   for pos,(state,tag) in list(b.blocks.items()):
    if state['Name']=='minecraft:dirt_path' and palette=='cold':b.put(*pos,'gravel')
   new=f'village/{palette}/roads/{biome}_{suffix.replace("/","_")}_{loc.split("/")[-1]}'
   save(new,b,doc.get('entities',[]));el['location']='slavicmyths:'+new
  write(rel,pool)

def main():
 catalog=[];audit=[]
 kinds=[('residential',k,i) for k,count in [('seni',3),('zemlyanka',3),('izba',3)] for i in range(1,count+1)]+[('profession',k,1) for k in JOBS if k not in ['hlev','druzhinnik_barracks','small_field','large_field']]+[('utility',k,1) for k in ['ambar','klet','stable','hlev']]+[('farm',k,1) for k in ['small_field','large_field']]+[('defense','druzhinnik_barracks',1),('center','village_center',1)]
 for palette in PALETTES:
  for family,kind,v in kinds:
   b,entities,meta=field(kind,palette) if family=='farm' else center(palette) if family=='center' else house(kind,palette,v)
   rel=f'village/{palette}/{family}/{kind}_{v:02}';save(rel,b,entities)
   meta.update(id='slavicmyths:'+rel,palette=palette,variant=v);catalog.append(meta)
   if kind not in ['village_center']:
    bz,ez,_=field(kind,palette) if family=='farm' else house(kind,palette,v,True)
    if family=='farm':ez=[entity(*meta['spawn'],zombie=True)]
    save(rel+'_zombie',bz,ez)
 # Retain the existing saved identifier for admin tools and the one-barracks limiter.
 b,e,m=house('druzhinnik_barracks','temperate');save('military/barracks',b,e)
 lookup={(m['palette'],m['kind'],m['variant']):m['id'] for m in catalog}
 with zipfile.ZipFile(JAR) as z:
  for biome in ['plains','taiga','snowy','savanna','desert']:
   palette='cold' if biome in ['taiga','snowy'] else 'warm' if biome in ['savanna','desert'] else 'temperate'
   for suffix in ['houses','zombie/houses']:
    rel=f'data/minecraft/worldgen/template_pool/village/{biome}/{suffix}.json';d=json.loads(z.read(rel));zombie=suffix.startswith('zombie')
    for row in d['elements']:
     el=row['element'];loc=el.get('location')
     if not loc:continue
     kind='klet' if 'accessory' in loc else mapping(loc);num=int(re.search(r'(\d+)$',loc).group(1)) if re.search(r'(\d+)$',loc) else 1
     v=(num-1)%3+1 if kind in ['seni','zemlyanka','izba'] else 1
     new=lookup[palette,kind,v]+('_zombie' if zombie and kind!='village_center' else '')
     audit.append({'pool':rel,'old':loc,'new':new,'weight':row['weight']});el.update(location=new,element_type='minecraft:single_pool_element',processors='slavicmyths:village_foundation')
    for kind in ['miller_house','brewer_house','weaver_house','hunter_house','cook_house','ambar','klet','druzhinnik_barracks']:
     family=next(f for f,k,v in kinds if k==kind)
     loc='slavicmyths:military/barracks' if kind=='druzhinnik_barracks' and palette=='temperate' and not zombie else lookup[palette,kind,1]+('_zombie' if zombie else '')
     d['elements'].append({'weight':1 if kind in ['klet','druzhinnik_barracks'] else 2,'element':{'element_type':'minecraft:single_pool_element','location':loc,'processors':'slavicmyths:village_foundation','projection':'rigid'}})
    write(rel,d)
   roads(z,biome,palette)
   rel=f'data/minecraft/worldgen/template_pool/village/{biome}/town_centers.json';d=json.loads(z.read(rel))
   for i,row in enumerate(d['elements']):
    el=row['element'];loc=el['location'];raw=z.read('data/minecraft/structure/'+loc.split(':')[1]+'.nbt');source=read_nbt(raw)
    b,e,m=center(palette,i+1,source)
    if '/zombie/' in loc:e=[entity(int(a['blockPos'][0]),int(a['blockPos'][1]),int(a['blockPos'][2]),zombie=True) for a in e]
    new=f'village/{palette}/center/{biome}_center_{i+1:02}';save(new,b,e)
    el.update(location='slavicmyths:'+new,processors='slavicmyths:village_foundation',element_type='minecraft:single_pool_element')
    audit.append({'pool':rel,'old':loc,'new':el['location'],'weight':row['weight']})
   write(rel,d)
 # Loot remains lazy, vanilla one-time container loot. Community chest has no NBT loot.
 for kind,items in LOOT.items():
  entries=[{'type':'minecraft:item','name':item if ':' in item else 'minecraft:'+item,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]} for item in items]
  pools=[{'rolls':{'type':'minecraft:uniform','min':2,'max':4},'entries':entries},{'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':.65}],'entries':[{'type':'minecraft:item','name':'slavicmyths:ancient_coin','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]}]}]
  if kind in RARE:
   item,chance=RARE[kind];entry={'type':'minecraft:item','name':'minecraft:'+item}
   if item in ['bow','fishing_rod','iron_pickaxe','iron_sword','iron_helmet']:entry['functions']=[{'function':'minecraft:set_damage','damage':{'type':'minecraft:uniform','min':.15,'max':.4}}]
   pools.append({'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':chance}],'entries':[entry]})
  write('data/slavicmyths/loot_table/chests/village/'+kind+'.json',{'type':'minecraft:chest','pools':pools})
 write('data/slavicmyths/worldgen/processor_list/village_foundation.json',{'processors':[{'processor_type':'slavicmyths:village_foundation'}]})
 write('data/slavicmyths/village/catalog.json',{'build':'1.2.4','structures':catalog})
 (ROOT/'docs/verification/village-1.2.4-mapping.json').write_text(json.dumps(audit,indent=2)+'\n')
 print('Village 1.2.4:',len(catalog),'showcase templates,',len(audit),'vanilla replacements')
 from gorodishche_resources import main as city_resources
 city_resources()
if __name__=='__main__':main()


