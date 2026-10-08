"""Authored urban silhouettes, interiors and metadata; no runtime fallback architecture.

Rich izba: broad two-storey timber frame, cross-fronton and supported gallery.
Craftsman: narrow workshop beneath projecting residence, street-side lean-to.
Guard home: stone first floor, reinforced porch and stepped dark roof.
Narrow house: slender two-storey street infill with compact stairs.
Trading hall: three wide counter bays under a deep canopy, upstairs residence.
Princely terem: raised pale-stone hall, dark frame, side wings and three roofs.
"""
import json,copy
import village_buildings as v
from pathlib import Path
ROOT=v.ROOT;R=v.RES;CAT=[]
FAMILIES={'a':('spruce','oak','dark_oak','stone_bricks'),'b':('dark_oak','birch','spruce','polished_andesite'),'c':('oak','spruce','dark_oak','tuff_bricks'),'d':('spruce','dark_oak','oak','stone_bricks')}
def materials(climate,family):
 frame,infill,roof,stone=FAMILIES[family]
 if climate=='cold':frame='dark_oak' if family=='b' else 'spruce';infill='birch' if family in ['b','d'] else 'oak';roof='dark_oak' if family in ['a','c'] else 'spruce'
 if climate=='warm':frame='dark_oak' if family=='b' else 'oak';infill='birch' if family in ['b','d'] else 'spruce';roof='oak' if family=='d' else 'dark_oak' if family in ['a','c'] else 'spruce'
 return frame,infill,roof,stone
def bp(name,size):return v.n.Blueprint(name,tuple(size))
def marker(b,p,name):v.be(b,*p,'structure_block',{'id':'minecraft:structure_block','mode':'DATA','metadata':name,'ignoreEntities':1,'showboundingbox':0,'powered':0,'posX':0,'posY':0,'posZ':0,'sizeX':0,'sizeY':0,'sizeZ':0},mode='data')
def publish(climate,category,name,b,family='a',floors=0,beds=None,jobs=None,roof='gable',required=False,spawns=None,front=None,surface=0,weight=1):
 # Metadata supports real template fitting and frontage placement without scaling.
 while not any(y==b.size[1]-1 for x,y,z in b.blocks) and b.size[1]>1:b.size=(b.size[0],b.size[1]-1,b.size[2])
 id=f'gorodishche/{climate}/{category}/{name}'
 for i,(p,role) in enumerate(spawns or []):marker(b,p,f'spawn|{i}|{role}')
 v.save(id,b)
 f=front or [b.size[0]//2,1,0]
 meta={'id':'slavicmyths:'+id,'name':name,'climate':climate,'category':category,'size':list(b.size),'front_side':'north','frontage_width':b.size[0],'depth':b.size[2],'entrance_offset':f,'family':family,'materials':list(materials(climate,family)),'floors':floors,'surface':surface,'beds':beds or [],'workstations':jobs or [],'roof_variant':roof,'allowed_street_classes':['MAIN','SECONDARY','ALLEY'],'weight':weight,'required':required}
 CAT.append(meta);return meta
def door(b,x,y,z,wood):
 for dy in [0,1]:b.put(x,y+dy,z,wood+'_door',facing='north',half='lower' if dy==0 else 'upper',hinge='left',open=False,powered=False)
def window(b,x,y,z,facing,wood,shutters=False,radius=1):
 # One material fills the whole framed aperture, never glass between shutters.
 for d in range(-radius,radius+1):
  xx=x+d if facing in ['north','south'] else x;zz=z if facing in ['north','south'] else z+d
  if shutters:b.put(xx,y,zz,wood+'_trapdoor',facing=facing,half='bottom',open=True,powered=False,waterlogged=False)
  else:b.put(xx,y,zz,'glass_pane',north=facing in ['east','west'],south=facing in ['east','west'],east=facing in ['north','south'],west=facing in ['north','south'],waterlogged=False)
def stairs(b,x,z,start,floor,wood,width=2):
 # Five consecutive one-block rises with a genuine upper-floor opening and headroom.
 for i in range(5):
  for dx in range(width):
   xx=x+dx;zz=z+i;y=start+i
   b.box((xx,y+1,zz),(xx,floor+3,zz),'air');v.stairs(b,xx,y,zz,wood,'south')
   if y>start:b.box((xx,start,zz),(xx,y-1,zz),wood+'_planks')
def workstation(b,p,role,block):
 x,y,z=p;props={} if block in ['lectern_dummy','cartography_table','fletching_table','smithing_table','brewing_stand','cauldron','composter','slavicmyths:millstone'] else {'facing':'north'}
 if block=='lectern':props={'facing':'north','powered':False,'has_book':False}
 if block=='grindstone':props={'face':'floor','facing':'north'}
 if block in ['blast_furnace','smoker']:props['lit']=False
 if block=='slavicmyths:kitchen_table':
  b.put(x,y,z,block,facing='north',part='left');b.put(x+1,y,z,block,facing='north',part='right')
 elif block=='slavicmyths:drying_rack':
  for part in range(4):b.put(x+part%2,y+part//2,z,block,facing='north',part=part)
 else:b.put(x,y,z,block,**props)
 return {'pos':list(p),'role':role if ':' in role else 'minecraft:'+role,'block':block if ':' in block else 'minecraft:'+block}
def furniture(b,x,y,z,kind='pine_table'):
 props={'facing':'north'}
 if kind=='pine_table':props['connection']='none'
 b.put(x,y,z,'slavicmyths:'+kind,**props)
def urban_house(climate,family,name,w=13,d=15,kind='rich',role=None,job=None,variant=0,floors=2):
 frame,wall,r,stone=materials(climate,family);cold=climate=='cold';roof_tag={'rich':'cross_gable','craft':'lean_to','guard':'stepped','narrow':'high_gable','forge':'workshop_gable','knowledge':'gallery_gable','herbalist':'twin_gable','trading':'double_gable'}.get(kind,'gable')
 porch=3 if climate=='warm' else 2;W=w+2;D=d+porch+2;x0=1;x1=w;z0=porch+1;z1=z0+d-1;cx=W//2
 b=bp(name,[W,32,D]);b.box((x0,0,0),(x1,0,z1),stone);b.box((x0,1,z0),(x1,4 if floors==1 else 9,z1),'air');b.box((x0,0,z0),(x1,0,z1),frame+'_planks')
 for y0,y1,infill in [(1,4,stone if kind in ['forge','guard','trading'] else wall+'_planks')]+([(6,9,wall+'_planks')] if floors>1 else []):
  for z in [z0,z1]:b.box((x0,y0,z),(x1,y1,z),infill)
  for x in [x0,x1]:b.box((x,y0,z0),(x,y1,z1),infill)
 for x in [x0,x1]:
  for z in [z0,z0+d//2,z1]:b.box((x,0,z),(x,10 if floors>1 else 5,z),'stripped_'+frame+'_log',axis='y')
 for y in [4,5,9] if floors>1 else [4]:
  for z in [z0,z1]:b.box((x0,y,z),(x1,y,z),frame+'_log',axis='x')
  for x in [x0,x1]:b.box((x,y,z0),(x,y,z1),frame+'_log',axis='z')
 if floors>1:b.box((x0+1,5,z0+1),(x1-1,5,z1-1),frame+'_planks')
 for y in [2,7] if floors>1 else [2]:
  for z in [z0,z1]:
   for x in [x0+2,x1-2]:window(b,x,y,z,'north' if z==z0 else 'south',frame,climate=='warm' and family in ['b','d'],0 if w<11 else 1)
  for x in [x0,x1]:
   for z in [z0+4,z1-3]:window(b,x,y,z,'west' if x==x0 else 'east',frame,climate=='warm' and family in ['b','d'])
 roof_base=10 if floors>1 else 5
 if roof_tag=='stepped':
  v.roof(b,x0,x1,z0,z0+d//2,roof_base,r,cold);v.roof(b,x0,x1,z0+d//2+1,z1,roof_base+1,r,cold)
 else:v.roof(b,x0,x1,z0,z1,roof_base,r,cold)
 # Wide street porch: supports start on ground, no unsupported roof or gallery.
 for x in [cx-2,cx+2]:b.box((x,0,0),(x,3,0),frame+'_log',axis='y')
 for x in range(cx-2,cx+3):
  for z in range(z0):v.slab(b,x,4,z,r)
 door(b,cx,1,z0,frame)
 if cold:
  for x in [cx-2,cx+2]:b.box((x,1,1),(x,3,z0-1),wall+'_planks')
 if kind in ['rich','knowledge','trading']:
  if floors>1:
   b.box((x0+1,5,z0-1),(x1-1,5,z0),frame+'_planks')
   for x in range(x0+1,x1):v.fence(b,x,6,z0-1,frame)
   for x in [x0+1,x1-1]:b.box((x,0,z0-1),(x,roof_base-1,z0-1),frame+'_log',axis='y')
   door(b,cx,6,z0,frame)
   if cold:
    for x in range(x0+2,x1-1):b.put(x,7,z0-1,'glass_pane',north=False,south=False,east=True,west=True,waterlogged=False)
  # Crosswise porch/fronton breaks the main roof silhouette.
  v.roof(b,cx-2,cx+2,1,z0+2,roof_base-1,'spruce' if r=='dark_oak' else 'dark_oak',cold)
 if kind in ['craft','forge']:
  for x in range(1,cx-2):
   for z in range(z0):v.slab(b,x,4,z,r)
  b.box((1,0,0),(1,3,0),frame+'_log',axis='y')
 if kind=='herbalist':v.roof(b,1,4,z1-4,z1,roof_base+1,'oak',cold)
 if kind=='guard':b.put(x0+1,3,z0,'red_banner',rotation=8);b.put(x1-1,3,z0,'white_banner',rotation=8);furniture(b,x0+2,1,z0+3,'pine_weapon_rack')
 beds=[];upper=6 if floors>1 else 1;bedn=2 if kind in ['craft','forge','knowledge','herbalist','narrow'] else 3
 if role:bedn=2 if name.startswith('craftsman_') else 1
 if kind=='trading':bedn=3
 for i in range(bedn):
  x=x0+1+i*2;z=z1-4 if kind=='trading' else z1-2
  v.bed(b,x,upper,z,'white' if i%2 else 'blue' if cold else 'red');beds.append([x,upper,z])
 # Stair core stays on the right, away from the door, counter and bed aisles.
 if floors>1 and kind!='trading':stairs(b,x1-2,z0+3,1,5,frame,1 if w<11 else 2)
 if kind!='trading':furniture(b,x0+2,1,z0+4);furniture(b,x0+2,1,z0+5,'pine_stool')
 if floors>1:furniture(b,cx,6,z1-4);furniture(b,cx+1,6,z1-4,'pine_stool')
 b.put(x0+1,1,z0+1,'furnace',facing='south',lit=False);b.box((x0+1,2,z0+1),(x0+1,roof_base+w//2+2,z0+1),'bricks' if variant%2 else 'stone_bricks');v.slab(b,x0+1,roof_base+w//2+3,z0+1,'brick' if variant%2 else 'stone_brick')
 b.put(cx,1,z1-1,'slavicmyths:wooden_barrel',facing='east',brewing=False);v.loot(b,x1-1,upper,z1-1,'home')
 jobs=[];spawns=[]
 if role:
  pos=[x0+2,1,z0+7];jobs.append(workstation(b,pos,role,job));spawns.append(([cx,1,z0+2],role))
  if kind=='forge':b.put(x0+4,1,z0+6,'anvil',facing='north');b.put(x0+2,1,z1-2,'coal_block')
  if role in ['slavicmyths:brewer','slavicmyths:cook']:b.put(x0+2,1,z1-2,'slavicmyths:storage_sack',facing='east',fill=0)
  if role in ['librarian','cartographer']:
   for z in [z0+3,z0+4]:b.put(x0+1,1,z,'bookshelf');b.put(x0+1,2,z,'bookshelf')
  if role=='slavicmyths:weaver':b.put(x0+1,3,z0,'white_banner',rotation=8)
 else:spawns=[]
 if kind=='trading':
  jobs=[];spawns=[];roles=[('slavicmyths:cook','slavicmyths:kitchen_table'),('fletcher','fletching_table'),('cartographer','cartography_table')] if variant==0 else [('slavicmyths:brewer','slavicmyths:fermentation_vat'),('slavicmyths:weaver','slavicmyths:loom_table'),('librarian','lectern')] if variant==1 else [('butcher','smoker'),('leatherworker','cauldron'),('cartographer','cartography_table')]
  for i,(role,job) in enumerate(roles):
   x=5+i*4;pos=[x,1,z0+7];jobs.append(workstation(b,pos,role,job));spawns.append(([x+1,1,z0+6],role))
   for dx in range(2):v.slab(b,x+dx,1,z0+4,frame)
   b.put(x,3,z0,'white_banner' if i==1 else 'red_banner',rotation=8)
  v.be(b,2,1,z1-1,'chest',{'id':'minecraft:chest','LootTable':'slavicmyths:chests/gorodishche/trading'},facing='north',type='single',waterlogged=False)
  # Two full landing rows precede the stair, beside rather than through stalls.
  stairs(b,3,z0+3,1,5,frame,2)
  door(b,cx,1,z0,frame)
 if kind=='herbalist':
  for x in range(2,5):b.put(x,0,z1+1,'grass_block',snowy=False);b.put(x,1,z1+1,'poppy' if x%2 else 'dandelion')
 from city_furnishing import furnish_urban
 furnish_urban(b,climate,family,kind,role,x0,x1,z0,z1,cx,floors,frame,beds)
 category='civic' if kind=='trading' else 'profession' if role else 'residential'
 return publish(climate,category,name,b,family,floors,beds,jobs,roof_tag,kind=='trading',spawns,[cx,1,0])
def princely(climate):
 b=bp('terem_01',[39,38,32]);cold=climate=='cold';frame='dark_oak';r='dark_oak';cx=19
 # Raised stepped stone terrace, with a generous seven-wide grand stair.
 b.box((1,0,4),(37,2,29),'stone_bricks');b.box((7,3,6),(31,3,28),'polished_andesite')
 for i in range(4):
  for x in range(cx-3,cx+4):v.stairs(b,x,i,i,'quartz','south')
 for lo,hi in [(1,7),(31,37)]:
  b.box((lo,3,8),(hi,7,26),'calcite');b.box((lo+1,4,9),(hi-1,7,25),'air');b.box((lo,3,8),(hi,3,26),'oak_planks');v.roof(b,lo,hi,8,26,8,'spruce',cold)
  for x in [lo,hi]:
   for z in [8,17,26]:b.box((x,0,z),(x,8,z),frame+'_log',axis='y')
  window(b,(lo+hi)//2,5,8,'north','oak');furniture(b,lo+2,4,12);v.loot(b,lo+1,4,25,'home')
 # Two complete main floors, dark structural rhythm and framed pale windows.
 b.box((8,4,6),(30,12,26),'calcite');b.box((9,4,7),(29,12,25),'air');b.box((8,3,6),(30,3,26),'oak_planks');b.box((9,8,7),(29,8,25),'spruce_planks')
 for x in [8,13,19,25,30]:
  for z in [6,26]:b.box((x,3,z),(x,13,z),frame+'_log',axis='y')
 for x in [8,30]:
  for z in [12,20]:b.box((x,3,z),(x,13,z),'quartz_pillar',axis='y')
 for y in [7,8,12]:
  for z in [6,26]:b.box((8,y,z),(30,y,z),frame+'_log',axis='x')
  for x in [8,30]:b.box((x,y,6),(x,y,26),frame+'_log',axis='z')
 for y in [5,10]:
  for x in [11,16,22,27]:window(b,x,y,6,'north','dark_oak');window(b,x,y,26,'south','dark_oak')
  for z in [10,16,24]:window(b,8,y,z,'west','dark_oak');window(b,30,y,z,'east','dark_oak')
 for x in range(cx-1,cx+2):b.box((x,4,6),(x,6,6),'air')
 # Gallery and porch columns explicitly meet terrace and roof.
 b.box((9,8,4),(29,8,5),'dark_oak_planks')
 for x in [9,14,24,29]:b.box((x,3,4),(x,12,4),'quartz_pillar',axis='y')
 for x in range(10,29):v.fence(b,x,9,4,'dark_oak')
 door(b,cx,9,6,'dark_oak')
 if cold:
  for x in range(10,29):b.put(x,10,4,'glass_pane',north=False,south=False,east=True,west=True,waterlogged=False)
 v.roof(b,8,30,6,26,13,r,cold);v.roof(b,16,22,3,10,13,'spruce',cold)
 for x in [16,22]:b.box((x,9,3),(x,12,3),'dark_oak_log',axis='y')
 # Unique rear roof pavilion and complementary frontons, not a tall generic cube.
 b.box((16,14,21),(22,17,27),'dark_oak_planks');b.box((17,14,22),(21,17,26),'air');b.box((16,13,21),(22,13,27),'spruce_planks');v.roof(b,16,22,21,27,18,'spruce',cold);window(b,19,15,21,'north','dark_oak')
 for x in [10,28]:b.put(x,6,6,'red_banner',rotation=8);b.put(x,11,6,'white_banner',rotation=8)
 for z in range(15,23):furniture(b,13,4,z);furniture(b,12,4,z,'pine_stool');furniture(b,14,4,z,'pine_stool')
 b.box((17,4,23),(21,4,25),'dark_oak_planks');furniture(b,19,5,24,'pine_chair')
 b.put(10,4,8,'furnace',facing='south',lit=False);b.box((10,5,8),(10,27,8),'stone_bricks');v.slab(b,10,28,8,'stone_brick')
 # Room dividers leave a broad upper circulation gallery and central doors.
 for x in [16,23]:b.box((x,9,18),(x,11,25),'spruce_planks');b.box((x,9,20),(x,10,20),'air')
 beds=[]
 for x in [11,19,26]:v.bed(b,x,9,22,'white');beds.append([x,9,22]);v.loot(b,x+1,9,25,'home')
 stairs(b,18,12,4,8,'dark_oak',2)
 furniture(b,11,9,13);b.put(10,9,14,'bookshelf');furniture(b,13,9,13,'pine_stool')
 v.be(b,10,4,24,'chest',{'id':'minecraft:chest','LootTable':'slavicmyths:chests/gorodishche/princely'},facing='east',type='single',waterlogged=False)
 from city_furnishing import furnish_princely
 furnish_princely(b)
 return publish(climate,'princely','terem_01',b,'b',2,beds,roof='princely_multi',required=True,front=[19,1,0])
def wall(climate,length):
 b=bp('wall',[length,11,4])
 b.box((0,0,0),(length-1,1,2),'cobblestone');b.box((0,2,0),(length-1,7,2),'stone_bricks')
 b.box((0,7,0),(length-1,7,2),'polished_andesite')
 for x in range(length):
  if x%4==0:b.box((x,2,0),(x,6,0),'andesite')
  if x%2==0:b.box((x,8,0),(x,9,0),'stone_bricks')
  else:v.slab(b,x,8,0,'stone_brick')
  if x%8==3:b.put(x,1,0,'mossy_stone_bricks');b.put(x,4,0,'cracked_stone_bricks')
  if x%8==0:b.box((x,0,3),(x,5,3),'tuff_bricks');v.stairs(b,x,6,3,'stone_brick','north')
  if climate=='cold':b.put(x,8,2,'snow',layers=1)
 if length>=8:b.put(length//2,8,2,'stone_bricks');v.lamp(b,length//2,9,2,False)
 return publish(climate,'defense',f'wall_straight_{length:02}',b,required=True)
def tower(climate):
 b=bp('tower',[9,25,9]);r='spruce' if climate=='cold' else 'dark_oak'
 # Full nine-block footprint meets both wall faces. Corner posts stay solid.
 b.box((0,0,0),(8,7,8),'stone_bricks');b.box((2,2,2),(6,7,6),'air');b.box((0,0,0),(8,1,8),'cobblestone')
 b.box((1,8,1),(7,8,7),'spruce_planks')
 for x in range(9):
  for z in range(9):
   if x in [0,8] or z in [0,8]:b.put(x,9,z,'dark_oak_planks')
 b.box((4,2,7),(5,3,8),'air')
 for x in [4,5]:v.stairs(b,x,1,8,'stone_brick','north')
 b.box((3,8,7),(5,10,8),'air');b.box((7,8,3),(8,10,5),'air')
 for x in [0,8]:
  for z in [0,8]:b.box((x,0,z),(x,11,z),'dark_oak_log',axis='y')
 for y in range(1,9):b.put(2,y,2,'ladder',facing='south',waterlogged=False)
 v.roof(b,1,7,1,7,12,r,climate=='cold');b.box((0,11,4),(8,11,4),'dark_oak_log',axis='x');v.lamp(b,4,10,4);b.put(3,4,0,'red_wall_banner',facing='north');b.put(6,4,2,'wall_torch',facing='west')
 return publish(climate,'defense','tower_corner_01',b,roof='tower_cap',required=True)

def gate(climate):
 b=bp('gate',[15,28,9]);r='spruce' if climate=='cold' else 'dark_oak'
 for x0,x1 in [(0,4),(10,14)]:
  b.box((x0,0,1),(x1,1,7),'cobblestone');b.box((x0,2,1),(x1,9,7),'stone_bricks');b.box((x0+1,2,2),(x1-1,7,6),'air')
  for x in [x0,x1]:b.box((x,0,1),(x,10,1),'andesite')
 b.box((5,0,0),(9,0,8),'coarse_dirt');b.box((5,1,0),(9,6,8),'air');b.box((0,7,1),(14,7,7),'dark_oak_planks');b.box((1,8,2),(13,10,6),'air')
 for x in [0,4,10,14]:b.box((x,8,1),(x,11,1),'dark_oak_log',axis='y');b.box((x,8,7),(x,11,7),'dark_oak_log',axis='y')
 for x in range(1,14):v.fence(b,x,8,1,'dark_oak');v.fence(b,x,8,7,'dark_oak')
 v.roof(b,1,13,1,7,11,r,climate=='cold')
 for x in [3,11]:b.put(x,5,1,'red_banner',rotation=8);v.lamp(b,x,6,0)
 for x in [2,3,11,12]:b.box((x,2,7),(x,3,7),'air');v.stairs(b,x,1,8,'stone_brick','north')
 for x in [0,14]:b.box((x,8,2),(x,10,3),'air')
 for x in [3,11]:v.slab(b,x,7,0,'dark_oak')
 for y in range(2,8):b.put(1,y,2,'ladder',facing='south',waterlogged=False)
 jobs=[workstation(b,[x,2,5],'slavicmyths:druzhinnik','slavicmyths:druzhinnik_table') for x in [2,12]]
 return publish(climate,'defense','gate_main_01',b,roof='gate_gable',required=True,front=[7,1,0],jobs=jobs,spawns=[([3,2,4],'slavicmyths:druzhinnik'),([11,2,4],'slavicmyths:druzhinnik')])
def square(climate):
 b=bp('square',[35,15,35]);r='spruce' if climate=='cold' else 'oak'
 for x in range(35):
  for z in range(35):b.put(x,0,z,'gravel' if x<8 or x>26 else 'packed_mud' if z<7 or z>27 else 'cobblestone' if x<14 or x>20 else 'stone')
 b.box((16,1,16),(18,1,18),'stone_bricks');b.box((17,2,17),(17,9,17),'dark_oak_log',axis='y');b.box((15,8,17),(19,8,17),'dark_oak_log',axis='x');b.put(16,7,17,'red_banner',rotation=8);b.put(18,7,17,'white_banner',rotation=8);v.lamp(b,15,7,17);v.lamp(b,19,7,17)
 # Bell supports native MEET; placed off the city pole and off the main axes.
 b.put(24,1,23,'stone_bricks');b.put(24,2,23,'bell',facing='north',attachment='floor',powered=False)
 for x,z in [(5,9),(5,24),(29,9),(29,24),(9,5),(24,5)]:
  for i in range(3):furniture(b,x+i,1,z,'pine_bench')
 b.box((4,1,15),(10,1,19),r+'_planks')
 for x in range(4,11):v.stairs(b,x,1,14,r,'south')
 b.put(7,2,19,'red_banner',rotation=8)
 for x,z,color in [(3,2,'red'),(25,2,'white'),(3,30,'white')]:
  for xx in [x,x+5]:
   for zz in [z,z+3]:b.box((xx,1,zz),(xx,3,zz),r+'_fence',north=False,south=False,east=False,west=False,waterlogged=False)
  b.box((x,4,z),(x+5,4,z+3),color+'_wool');b.box((x+1,1,z+2),(x+4,1,z+2),r+'_slab',type='top',waterlogged=False);b.put(x+1,1,z+3,'slavicmyths:wooden_crate',facing='north')
 # Small rich stone well beside square edge, never replaces the pole.
 for x in range(27,31):
  for z in range(15,19):b.put(x,1,z,'stone_bricks' if x in [27,30] or z in [15,18] else 'water')
 for x in [27,30]:b.box((x,2,16),(x,4,16),r+'_log',axis='y')
 v.roof(b,27,30,15,18,5,'dark_oak')
 return publish(climate,'civic','square_01',b,required=True)
def sacred(climate):
 b=bp('sacred',[13,12,13]);b.box((0,0,0),(12,0,12),'coarse_dirt')
 for x in range(13):
  for z in [0,12]:v.fence(b,x,1,z,'spruce')
 for z in range(13):
  for x in [0,12]:v.fence(b,x,1,z,'spruce')
 b.box((5,1,5),(7,1,7),'stone_bricks');b.box((6,2,6),(6,6,6),'stripped_oak_log',axis='y');b.box((5,4,6),(7,4,6),'oak_log',axis='x');b.put(6,5,5,'oak_trapdoor',facing='north',half='bottom',open=True,powered=False,waterlogged=False);b.box((5,1,0),(7,2,0),'air');b.put(2,1,2,'stone_bricks');b.put(10,1,10,'stone_bricks');v.lamp(b,2,2,2,False);v.lamp(b,10,2,10,False)
 return publish(climate,'civic','sacred_yard_01',b,required=True)
def reused(climate,kind):
 b,entities,m=v.house(kind,climate);b.blocks={p:(s,t) for p,(s,t) in b.blocks.items() if s['Name']!='minecraft:jigsaw'}
 jobs=[];spawns=[]
 if kind=='druzhinnik_barracks':
  # Six real POI tickets, eight beds: guard group uses existing profession logic.
  existing=[p for p,(s,t) in b.blocks.items() if s['Name']=='slavicmyths:druzhinnik_table']
  for p in existing:b.put(*p,'air')
  for x,z in [(3,7),(3,9),(3,11),(10,7),(10,9),(10,11)]:jobs.append(workstation(b,[x,3,z],'slavicmyths:druzhinnik','slavicmyths:druzhinnik_table'))
  for i in range(6):spawns.append(([6,3,5+i],'slavicmyths:druzhinnik'))
  for x in [7,8,9]:v.bed(b,x,3,13,'white')
 else:
  for i,ent in enumerate(entities):spawns.append((ent['blockPos'],ent['nbt']['id'].removeprefix('minecraft:')))
 beds=[list(p) for p,(s,t) in b.blocks.items() if s['Name'].endswith('_bed') and s.get('Properties',{}).get('part')=='head']
 from city_furnishing import furnish_reused
 furnish_reused(b,kind)
 return publish(climate,'military' if kind=='druzhinnik_barracks' else 'utility',kind+'_01',b,floors=1,beds=beds,jobs=jobs,required=True,spawns=spawns,front=m['entrance'],surface=m['surface'])
def main():
 CAT.clear()
 for climate in v.PALETTES:
  for family in FAMILIES:
   for variant in range(2):urban_house(climate,family,f'rich_izba_{variant+1:02}',13 if variant==0 else 12,15 if variant==0 else 14,'rich',variant=variant,floors=2 if variant==0 else 1)
   urban_house(climate,family,'druzhinnik_house_01',12,14,'guard')
   for variant in range(2):urban_house(climate,family,f'narrow_house_{variant+1:02}',8 if variant==0 else 9,13,'narrow',variant=variant)
   for name,role,job,kind in [('craftsman_weaver_01','slavicmyths:weaver','slavicmyths:loom_table','craft'),('craftsman_brewer_01','slavicmyths:brewer','slavicmyths:fermentation_vat','craft'),('cook_house_01','slavicmyths:cook','slavicmyths:kitchen_table','craft'),('chronicler_terem_01','librarian','lectern','knowledge'),('herbalist_house_01','cleric','brewing_stand','herbalist'),('toolsmith_forge_01','toolsmith','smithing_table','forge'),('weaponsmith_forge_01','weaponsmith','grindstone','forge'),('armorer_forge_01','armorer','blast_furnace','forge')]:
    urban_house(climate,family,name,13,15,kind,role,job)
   # The same name with distinct family gets its own stable resource ID.
   for meta in CAT:
    if meta['climate']==climate and meta['family']==family and meta['category'] in ['residential','profession'] and '/'+family+'/' not in meta['id']:
     old=meta['id'].split(':')[1];new=old.rsplit('/',1)[0]+'/'+family+'/'+old.rsplit('/',1)[1];src=R/('data/slavicmyths/structure/'+old+'.nbt');dst=R/('data/slavicmyths/structure/'+new+'.nbt');dst.parent.mkdir(parents=True,exist_ok=True);dst.write_bytes(src.read_bytes());src.unlink();meta['id']='slavicmyths:'+new
  for variant in range(3):urban_house(climate,['a','b','d'][variant],f'trading_house_{variant+1:02}',17,17,'trading',variant=variant)
  princely(climate);square(climate);sacred(climate);tower(climate);gate(climate)
  for length in range(1,17):wall(climate,length)
  for kind in ['druzhinnik_barracks','stable','ambar','klet']:reused(climate,kind)
 v.write('data/slavicmyths/gorodishche/catalog.json',{'version':'1.2.5','families':FAMILIES,'buildings':CAT,'trade_categories':{'food':['slavicmyths:cook','slavicmyths:brewer','minecraft:butcher'],'craft':['minecraft:fletcher','slavicmyths:weaver','minecraft:leatherworker'],'knowledge':['minecraft:cartographer','minecraft:librarian']}})
 v.write('data/slavicmyths/gorodishche/layouts.json',{'size':176,'square':[70,70,35,35],'layouts':['axial','cross_axis'],'spacing_chunks':128,'separation_chunks':96,'terrain_max_delta':16,'local_correction':6})
 for name,items in {'princely':['book','paper','bread','gold_nugget','slavicmyths:ancient_coin'],'trading':['bread','paper','string','leather','slavicmyths:ancient_coin']}.items():v.write('data/slavicmyths/loot_table/chests/gorodishche/'+name+'.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':2,'max':4},'entries':[{'type':'minecraft:item','name':it if ':' in it else 'minecraft:'+it,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]} for it in items]}]})
 from city_furnishing import loot_tables
 loot_tables()
 v.write('data/slavicmyths/tags/worldgen/biome/has_structure/gorodishche.json',{'replace':False,'values':['minecraft:plains','minecraft:sunflower_plains','minecraft:forest','minecraft:birch_forest','minecraft:taiga','minecraft:snowy_taiga','minecraft:snowy_plains','minecraft:savanna']})
 v.write('data/slavicmyths/worldgen/structure/gorodishche.json',{'type':'slavicmyths:gorodishche','biomes':'#slavicmyths:has_structure/gorodishche','step':'surface_structures','terrain_adaptation':'none','spawn_overrides':{}})
 v.write('data/slavicmyths/worldgen/structure_set/gorodishche.json',{'structures':[{'structure':'slavicmyths:gorodishche','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':128,'separation':96,'salt':125176,'spread_type':'linear','exclusion_zone':{'other_set':'minecraft:villages','chunk_count':12}}})
 for lang,text in [('ru_ru','Городище'),('en_us','Gorodishche')]:
  p=R/f'assets/slavicmyths/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));data['structure.slavicmyths.gorodishche']=text;p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(f'Gorodishche 1.2.5: {len(CAT)} authored templates, three climates, four urban material families')
if __name__=='__main__':main()
