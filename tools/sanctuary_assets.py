"""1.9.3 original authored block layouts, deterministic native NBT tiles and pixel resources."""
from pathlib import Path
from PIL import Image,ImageDraw
from nbtlib import File,Compound,List,Int,String,Long
import io,gzip,json,math,random,hashlib,re,wave,struct,subprocess,shutil
from morozko_assets import write,merge
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/sanctuaries-1.9.3'
class Layout:
 def __init__(self,spring=False):self.spring=spring;self.width=96 if spring else 80;self.depth=86 if spring else 76;self.cells={};self.markers=[]
 def put(self,x,y,z,name,**props):
  assert 0<=x<self.width and 0<=z<self.depth and 0<=y<=26,(x,y,z,name)
  if ':' not in name:name='minecraft:'+name
  self.cells[x,y,z]=(name,{k:str(v).lower() for k,v in props.items()},None)
 def fill(self,x0,y0,z0,x1,y1,z1,name,**props):
  for x in range(x0,x1+1):
   for y in range(y0,y1+1):
    for z in range(z0,z1+1):self.put(x,y,z,name,**props)
 def air(self,x0,y0,z0,x1,y1,z1):self.fill(x0,y0,z0,x1,y1,z1,'air')
 def marker(self,x,y,z,role):
  self.put(x,y,z,'structure_block',mode='data');self.cells[x,y,z]=('minecraft:structure_block',{'mode':'data'},Compound({'id':String('minecraft:structure_block'),'mode':String('DATA'),'metadata':String(role)}));self.markers.append({'pos':[x,y,z],'role':role})
 def banner(self,x,y,z,blue=False):
  self.put(x,y,z,'blue_wall_banner' if blue else 'red_wall_banner',facing='south')
  name,props,_=self.cells[x,y,z];self.cells[x,y,z]=(name,props,Compound({'id':String('minecraft:banner'),'patterns':List[Compound]([Compound({'pattern':String('minecraft:rhombus'),'color':String('white')}),Compound({'pattern':String('minecraft:straight_cross'),'color':String('white')})])}))
 def stone(self,x,y,z,taint=False):
  n='slavicmyths:mor_stone_bricks' if taint and (x*17+z*11+y*5)%9==0 else ['stone_bricks','stone_bricks','cracked_stone_bricks','mossy_stone_bricks'][(x*17+y*7+z*11)%4];self.put(x,y,z,n)
 def platform(self,x0,z0,x1,z1,y=0,clear=9):
  self.air(x0,y+1,z0,x1,y+clear,z1)
  for x in range(x0,x1+1):
   for z in range(z0,z1+1):self.stone(x,y,z,True)
 def pillar(self,x,z,y,height,spring=None):
  if spring is None:spring=self.spring
  self.put(x,y,z,'chiseled_stone_bricks')
  for yy in range(y+1,y+height):self.put(x,yy,z,'slavicmyths:carved_spring_pillar' if spring else 'slavicmyths:carved_fire_pillar',facing='south',lit=False)
  self.put(x,y+height,z,'stone_brick_slab',type='bottom',waterlogged=False)
  for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
   if 0<=x+dx<self.width and 0<=z+dz<self.depth:self.put(x+dx,y+height-1,z+dz,'spruce_fence',north=True,south=True,east=True,west=True,waterlogged=False)
  self.put(x,y+height-2,z+1,'lantern',hanging=True,waterlogged=False)
 def room(self,x0,z0,x1,z1,floor,height,wood=False,roof=True):
  self.platform(x0,z0,x1,z1,floor,height+1)
  for y in range(floor+1,floor+height):
   for x in range(x0,x1+1):
    for z in [z0,z1]:self.put(x,y,z,'spruce_planks') if wood else self.stone(x,y,z,True)
   for z in range(z0,z1+1):
    for x in [x0,x1]:self.put(x,y,z,'spruce_planks') if wood else self.stone(x,y,z,True)
  for x,z in [(x0,z0),(x1,z0),(x0,z1),(x1,z1)]:self.pillar(x,z,floor,height)
  if roof:self.fill(x0,floor+height,z0,x1,floor+height,z1,'stone_bricks')
 def opening(self,x0,z0,x1,z1,y,height=4):self.air(x0,y,z0,x1,y+height-1,z1)
 def gable(self,x0,z0,x1,z1,base,wood=True):
  center=(x0+x1)//2
  for x in range(x0,x1+1):
   rise=max(0,(min(x-x0,x1-x))//3)
   for z in range(z0,z1+1):self.put(x,base+rise,z,'spruce_stairs' if wood else 'stone_brick_stairs',facing='east' if x<center else 'west',half='bottom',shape='straight',waterlogged=False)
  for z in [z0,z1]:
   for x in range(x0,x1+1):
    rise=max(0,(min(x-x0,x1-x))//3)
    for y in range(base,base+rise):self.put(x,y,z,'spruce_planks' if wood else 'stone_bricks')
  for z in range(z0,z1+1):self.put(center,base+(x1-x0)//6+1,z,'spruce_log',axis='z')
 def basin(self,x0,z0,x1,z1,y=1,taint=False):
  for x in range(x0,x1+1):
   for z in range(z0,z1+1):
    self.stone(x,y-1,z,taint)
    if x in [x0,x1] or z in [z0,z1]:self.stone(x,y,z,taint)
    else:self.put(x,y,z,'water',level=0)
 def seal(self,spring):
  if spring:
   self.fill(78,1,29,84,7,29,'chiseled_stone_bricks');self.fill(80,1,29,82,5,29,'slavicmyths:seal_spring_door',facing='south',lit=False)
  else:
   self.fill(61,2,40,61,8,46,'chiseled_stone_bricks');self.fill(61,2,42,61,6,44,'slavicmyths:seal_fire_door',facing='west',lit=False)
 def save(self):
  family='spring' if self.spring else 'fire';summary={'size':[self.width,27,self.depth],'markers':self.markers,'blocks':len(self.cells),'tiles':[]}
  for tx in range(0,self.width,16):
   for tz in range(0,self.depth,16):
    palette=[];ids={};blocks=[]
    for (x,y,z),(name,props,tag) in sorted(self.cells.items()):
     if not tx<=x<tx+16 or not tz<=z<tz+16:continue
     key=(name,tuple(sorted(props.items())))
     if key not in ids:
      ids[key]=len(palette);state=Compound({'Name':String(name)})
      if props:state['Properties']=Compound({k:String(v) for k,v in props.items()})
      palette.append(state)
     b=Compound({'pos':List[Int]([x-tx,y,z-tz]),'state':Int(ids[key])})
     if tag is not None:b['nbt']=tag
     blocks.append(b)
    root=Compound({'DataVersion':Int(3955),'size':List[Int]([min(16,self.width-tx),27,min(16,self.depth-tz)]),'palette':List[Compound](palette or [Compound({'Name':String('minecraft:air')})]),'blocks':List[Compound](blocks),'entities':List[Compound]([])})
    data=io.BytesIO();File(root).write(data);path=D/f'structure/sanctuaries/{family}/{tx}_{tz}.nbt';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(gzip.compress(data.getvalue(),mtime=0));summary['tiles'].append({'name':f'{family}/{tx}_{tz}','blocks':len(blocks),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
  write(O/f'{family}-layout.json',summary);write(O/f'{family}-blocks.json',[{'pos':list(p),'name':b[0],'properties':b[1]} for p,b in sorted(self.cells.items())]);return summary
def fire():
 l=Layout()
 # Open ringed courtyard; only authored footprints clear vegetation.
 for x in range(20,61):
  for z in range(26,66):
   radius=math.hypot(x-40,z-46)
   if radius>20:continue
   l.air(x,1,z,x,11,z);l.stone(x,0,z,radius>15)
   if 14.4<radius<15.4 or 18.4<radius<19.4:l.put(x,0,z,'polished_andesite')
   if 16.5<radius<17.5:l.put(x,1,z,'stone_brick_slab',type='bottom',waterlogged=False)
 for x,z in [(23,37),(23,55),(32,28),(48,28),(57,37),(57,55),(32,63),(48,63)]:l.pillar(x,z,0,7)
 # Broad southern stairs and carved lintel.
 l.platform(32,66,49,75,0,12)
 for z in range(66,76):
  y=max(0,3-abs(z-70))
  for x in range(36,45):l.put(x,y,z,'stone_brick_stairs',facing='north' if z>70 else 'south',half='bottom',shape='straight',waterlogged=False)
 for x in [33,48]:
  for z in [69,70]:
   l.fill(x,0,z,x+1,9,z,'dark_oak_log',axis='y')
   l.put(x,5,z+1,'slavicmyths:carved_fire_pillar',facing='south',lit=False)
 l.fill(32,9,69,49,9,70,'spruce_log',axis='x');l.fill(31,10,68,50,10,71,'spruce_slab',type='bottom',waterlogged=False)
 for x in [35,46]:l.put(x,8,71,'lantern',hanging=True,waterlogged=False)
 for x in [34,48]:l.banner(x,7,71)
 # Eleven-block-wide vessel, stepped stone plinth, iron rim and controlled campfires.
 for x in range(34,47):
  for z in range(40,53):
   r=math.hypot(x-40,z-46)
   if r<=6:l.put(x,1,z,'polished_andesite')
   if r<=5:l.put(x,2,z,'blackstone')
   if 4<=r<=5:l.put(x,3,z,'chiseled_polished_blackstone')
   if 4.4<r<=5:l.put(x,4,z,'iron_bars',north=True,south=True,east=True,west=True,waterlogged=False)
 for dx,dz in [(0,0),(1,0),(-1,0),(0,1),(0,-1)]:l.put(40+dx,3,46+dz,'campfire',facing='south',lit=dx==dz==0,signal_fire=False,waterlogged=False)
 l.marker(40,2,51,'heart:fire')
 # Raised northern timber hall, open doorway, tall gable and three distinct guardians.
 l.room(23,6,57,26,3,9,wood=True,roof=False);l.gable(21,4,59,28,11)
 l.opening(37,26,43,26,4,6)
 for x in [31,49]:l.banner(x,9,27)
 for z,y in [(27,2),(28,1),(29,0)]:
  for x in range(37,44):l.put(x,y,z,'stone_brick_stairs',facing='north',half='bottom',shape='straight',waterlogged=False)
 for index,(x,kind) in enumerate([(30,'sun'),(40,'tree'),(50,'circle')]):
  l.fill(x-1,4,12,x+1,4,14,'chiseled_stone_bricks');l.fill(x,5,13,x,8+index,13,'slavicmyths:carved_fire_pillar',facing='south',lit=False);l.put(x,9+index,13,'slavicmyths:fire_idol_'+kind,facing='south',lit=False)
  if kind=='sun':l.fill(x-2,8,13,x+2,8,13,'spruce_log',axis='x')
  elif kind=='tree':
   for side in [-1,1]:l.fill(x+side,8,13,x+side,10,13,'spruce_fence',north=False,south=False,east=True,west=True,waterlogged=False)
  else:l.fill(x-1,6,13,x+1,8,13,'stripped_spruce_log',axis='y');l.put(x,10,13,'slavicmyths:fire_idol_circle',facing='south',lit=False)
  l.put(x,4,16,'lantern',hanging=False,waterlogged=False)
 l.marker(29,4,21,'loot:regular')
 # Asymmetric altars and small ritual store.
 l.platform(5,33,20,52,0,10)
 for x,z in [(8,35),(8,49),(19,35)]:l.pillar(x,z,0,5)
 for x,z in [(12,37),(12,48)]:
  l.fill(x-1,1,z-1,x+1,1,z+1,'stone_bricks');l.put(x,2,z,'campfire',facing='east',lit=True,signal_fire=False,waterlogged=False)
 l.marker(17,1,39,'loot:regular');l.marker(10,1,45,'loot:regular')
 l.room(7,12,21,27,1,7,wood=True,roof=False);l.gable(6,11,22,28,8);l.opening(13,27,16,27,2,4);l.marker(11,2,17,'loot:guaranteed');l.marker(18,2,20,'cache:fire')
 for x,z in [(9,14),(19,14),(9,23)]:l.put(x,2,z,'barrel',facing='up',open=False)
 # Reserved eastern hall: real architecture, empty pedestal, finite stone seals.
 l.room(61,31,79,55,1,10,wood=False,roof=False);l.gable(61,31,79,55,11,wood=False);l.seal(False)
 l.fill(69,2,35,73,2,38,'polished_andesite');l.put(71,3,37,'chiseled_stone_bricks')
 for x,z in [(64,34),(76,34),(64,54),(76,54)]:l.pillar(x,z,1,7);l.put(x,2,z+1,'lantern',hanging=False,waterlogged=False)
 l.platform(58,41,60,45,0,8)
 return l
def spring():
 l=Layout(True)
 # Separate connected stone halls, each with a physical roof and contained floor.
 for box in [(28,26,68,57,14),(6,27,27,52,10),(8,8,27,27,10),(69,31,90,56,11),(68,7,94,29,11)]:
  x0,z0,x1,z1,h=box;l.room(x0,z0,x1,z1,0,h)
  for x in range(x0+3,x1,8):
   for z in [z0+1,z1-1]:l.pillar(x,z,0,h-2,True)
 # Dry connecting passages and a stepped, illuminated descent.
 for x0,z0,x1,z1 in [(26,39,29,44),(18,25,23,28),(67,39,70,44),(77,28,85,32)]:l.platform(x0,z0,x1,z1,0,7);l.fill(x0,7,z0,x1,7,z1,'stone_bricks')
 for z in range(55,86):
  step=min(18,max(0,z-55 if z<=65 else 10 if z<=68 else z-58))
  l.air(39,step+1,z,57,min(26,step+7),z);l.fill(39,step,z,57,step,z,'stone_bricks')
  for x in [39,57]:l.fill(x,step+1,z,x,step+5,z,'stone_bricks')
  if z<78:l.fill(39,step+6,z,57,step+6,z,'stone_bricks')
  for x in range(45,52):l.put(x,step,z,'stone_brick_stairs',facing='south',half='bottom',shape='straight',waterlogged=False)
  if z%6==0:
   for x in [41,55]:l.put(x,step+3,z,'lantern',hanging=False,waterlogged=False)
 # Surface ruins: low broken arch, not a second surface courtyard.
 for x,z in [(39,78),(57,78),(39,84),(57,84)]:l.pillar(x,z,18,5,True)
 l.fill(39,23,78,57,23,78,'stone_brick_slab',type='bottom',waterlogged=False);l.fill(45,24,78,51,24,78,'mossy_stone_bricks')
 l.fill(39,22,78,57,22,78,'spruce_log',axis='x')
 for x in [43,53]:l.banner(x,22,79,True)
 # Main reservoir, dry walkways and a bounded waterfall into a short channel.
 for x in range(34,63):
  for z in range(31,52):
   q=((x-48)/14)**2+((z-41)/10)**2
   if q<=1:l.put(x,1,z,'water',level=0);l.stone(x,0,z)
   if .86<=q<=1.08:l.put(x,1,z,'polished_andesite')
 for x in range(35,62):
  for z in range(40,43):l.put(x,1,z,'stone_brick_slab',type='top',waterlogged=False)
 for z in range(32,54):
  for x in range(47,50):l.put(x,1,z,'stone_brick_slab',type='top',waterlogged=False)
 for x in range(47,50):
  l.put(x,1,54,'stone_brick_stairs',facing='north',half='bottom',shape='straight',waterlogged=False);l.put(x,0,55,'stone_bricks')
 for z in range(40,43):
  l.put(34,1,z,'stone_brick_stairs',facing='east',half='bottom',shape='straight',waterlogged=False);l.put(62,1,z,'stone_brick_stairs',facing='west',half='bottom',shape='straight',waterlogged=False)
 for x,z in [(34,31),(62,31),(34,51),(62,51)]:l.pillar(x,z,0,12,True)
 for x in [34,62]:l.banner(x,9,32,True)
 for y in range(1,13):
  for x,z in [(47,30),(49,30),(48,29)]:l.stone(x,y,z)
 l.put(48,11,30,'water',level=0);l.put(48,12,30,'sea_lantern');l.put(48,11,29,'slavicmyths:carved_spring_pillar',facing='south',lit=True)
 l.basin(46,29,50,35,1);l.marker(53,1,55,'heart:spring')
 # Three distinct side basins; only two carry an interactive source of taint.
 for index,z in enumerate([28,37,46]):l.basin(9,z,21,z+6,1,index>0)
 l.marker(23,1,39,'pool:0');l.marker(23,1,48,'pool:1')
 for x,z in [(8,28),(24,28),(8,44),(24,51)]:l.put(x,2,z,'moss_block');l.put(x,3,z,'azalea')
 # Channel walls and safe stepping bridges; water never crosses the room floor bounds.
 for x in [73,84]:l.basin(x,34,x+4,53,1)
 for z in [39,48]:
  for x in range(70,90):l.put(x,2,z,'stone_brick_slab',type='top',waterlogged=False)
 l.marker(89,1,35,'loot:regular')
 # Storage and sealed northeastern reserve, with real pillars and an empty pedestal.
 l.opening(14,27,20,27,1,5);l.marker(12,1,14,'loot:regular');l.marker(23,1,22,'loot:regular');l.marker(11,1,23,'cache:spring')
 for x,z in [(10,10),(24,10),(10,19)]:l.put(x,1,z,'decorated_pot',cracked=False,waterlogged=False)
 l.seal(True);l.fill(79,1,10,83,1,12,'polished_andesite');l.put(81,2,12,'chiseled_stone_bricks')
 for x,z in [(71,10),(91,10),(71,25),(91,25)]:l.pillar(x,z,0,8,True)
 # Keep the reserved arena clear of projecting decorative pillar arms.
 l.air(72,1,13,86,8,27)
 # Lush accents restricted to supported, local corners.
 for x,z in [(29,28),(67,28),(29,55),(67,55),(70,32),(89,54)]:
  l.put(x,1,z,'moss_block');l.put(x,2,z,'flowering_azalea');l.put(x,3,z,'glow_lichen',north=False,south=False,east=x in [67,89],west=x in [29,70],up=False,down=False,waterlogged=False)
 return l
ITEM_NAMES={
 'sacred_ember':('Священный уголь','Sacred Ember'),
 'purifying_ash':('Пепел очищения','Purifying Ash'),
 'flame_vial':('Огненная склянка','Flame Vial'),
 'fading_flame_charm':('Оберег угасающего пламени','Fading Flame Charm'),
 'ancient_rune_fragment':('Древние обломки рун','Ancient Rune Fragment'),
 'pure_spring_drop':('Капля чистого источника','Pure Spring Drop'),
 'purified_water_bottle':('Очищенная вода','Purified Water'),
 'spring_tear':('Слеза источника','Spring Tear'),
 'purification_stone':('Камень очищения','Purification Stone')}
BLOCK_NAMES={'unfading_brazier':('Чаша незатухающего огня','Unfading Brazier'),'pure_spring_heart':('Сердце чистого источника','Pure Spring Heart'),'tainted_spring_node':('Оскверняющий камень','Tainted Spring Node'),'seal_fire_door':('Огненная печать','Fire Seal'),'seal_spring_door':('Печать источника','Spring Seal'),'carved_fire_pillar':('Резная огненная опора','Carved Fire Pillar'),'carved_spring_pillar':('Резная опора источника','Carved Spring Pillar'),'fire_idol_sun':('Древняя солнечная фигура','Ancient Sun Figure'),'fire_idol_tree':('Древняя древесная фигура','Ancient Tree Figure'),'fire_idol_circle':('Древняя кольцевая фигура','Ancient Circle Figure'),'sanctuary_ritual_cache':('Ритуальный тайник','Ritual Cache')}
SOUNDS={'fire_ambient':('Тление древнего огня','Ancient fire crackles'),'fire_restore':('Огонь возрождается','Fire awakens'),'fire_seal':('Огненная печать гудит','Fire seal hums'),'fire_relic_hint':('Шёпот огненного зала','Fire chamber whispers'),'spring_ambient':('Журчание родника','Spring murmurs'),'spring_channel':('Течение водовода','Channel flows'),'spring_purify':('Источник очищается','Spring is purified'),'spring_tainted':('Осквернённая вода плещется','Tainted pool splashes'),'door_blocked':('Печать не поддаётся','Seal refuses to open'),'vial_throw':('Бросок огненной склянки','Flame vial thrown'),'vial_hit':('Огненная склянка разбивается','Flame vial shatters'),'water_use':('Глоток чистой воды','Pure water drunk'),'tear_use':('Слеза источника исцеляет','Spring tear heals')}
def icons():
 for name in ITEM_NAMES:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);dark='#303033';silver='#D6E5E7';warm='#F79C42';blue='#65D1E8'
  if name=='sacred_ember':
   d.polygon([(5,9),(12,3),(24,6),(29,16),(24,27),(10,29),(3,22)],fill=dark,outline='#655044');d.polygon([(11,11),(20,8),(24,17),(18,24),(10,21)],fill='#91442A');d.line([(13,10),(15,16),(11,22)],fill=warm,width=2);d.rectangle((21,15,24,18),fill='#FFD385')
  elif name=='purifying_ash':
   d.polygon([(4,22),(9,12),(16,9),(25,13),(29,23),(21,29),(10,28)],fill='#CABBA2',outline='#7B715F');d.polygon([(6,22),(11,14),(18,12),(25,17),(25,23),(16,25)],fill=silver);d.line([(9,18),(15,16),(22,19)],fill='#909B9E',width=2);d.line([(6,23),(16,27),(26,23)],fill='#EADFC9')
  elif name in ['flame_vial','pure_spring_drop','purified_water_bottle']:
   d.rectangle((12,2,19,6),fill='#685344');d.rectangle((10,6,21,9),fill=silver);d.polygon([(10,9),(6,13),(6,27),(25,27),(25,13),(21,9)],fill='#36536A',outline=silver)
   color=warm if name=='flame_vial' else blue;d.rectangle((8,17,23,25),fill=color);d.line((9,13,9,21),fill='#E8FAFB',width=2)
   if name=='pure_spring_drop':d.polygon([(17,11),(20,17),(17,21),(14,17)],fill='#B9EFFB')
   if name=='purified_water_bottle':d.line([(12,13),(19,20),(12,20),(19,13)],fill='#E5FBFF',width=2)
   if name=='flame_vial':d.rectangle((7,19,24,20),fill='#AF3D35')
  elif name=='fading_flame_charm':
   d.arc((8,2,23,15),180,360,fill='#9D714C',width=2);d.polygon([(9,10),(23,10),(28,20),(21,29),(10,28),(4,20)],fill='#AE8050',outline='#614832');d.polygon([(16,11),(21,20),(16,25),(11,21)],fill='#AC4739');d.line([(16,13),(16,22)],fill=warm,width=2)
  elif name=='ancient_rune_fragment':
   for poly in [[(4,18),(7,5),(14,8),(17,18),(10,27)],[(18,4),(26,9),(28,21),(19,27),(17,15)]]:
    d.polygon(poly,fill='#667171',outline='#A9ABA3')
   d.line([(9,9),(10,18),(7,22)],fill='#D9D3BD',width=2);d.line([(21,10),(25,12),(22,21)],fill='#D9D3BD',width=1)
  elif name=='spring_tear':
   d.ellipse((4,4,28,29),outline=silver,width=1);d.polygon([(16,2),(24,16),(21,25),(16,29),(10,25),(8,17)],fill=blue,outline='#B9EFFB');d.line([(16,6),(12,18),(13,23)],fill='#F0FCFF',width=2)
  else:
   d.ellipse((3,8,28,27),fill='#AFBBB6',outline='#63716D',width=2);d.line([(16,11),(22,18),(16,24),(10,18),(16,11)],fill='#58ADBC',width=2);d.line((16,9,16,24),fill='#B9EFFB')
  (A/'textures/item').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/item/{name}.png');write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
 for name,color in [('sanctuary_flame_guard','#F79C42'),('spring_tear_healing','#65D1E8')]:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);d.polygon([(16,3),(25,10),(25,22),(16,29),(7,22),(7,10)],fill=color,outline='#D6E5E7');d.line((16,9,16,23),fill='#FFFFFF',width=2);(A/'textures/mob_effect').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/mob_effect/{name}.png')
def block_assets():
 for index,name in enumerate(BLOCK_NAMES):
  if name=='sanctuary_ritual_cache':
   write(A/f'blockstates/{name}.json',{'variants':{'':{'model':'minecraft:block/chest'}}})
   from ritual_cache_item40 import apply
   apply();continue
  fire='spring' not in name and 'tainted' not in name;wood='pillar' in name and fire or 'idol' in name
  for lit in [False,True]:
   im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);base=(87,64,44) if wood else (139,145,140);dark=(48,39,33) if wood else (75,86,88)
   for y in range(32):
    for x in range(32):
     delta=((x//3+y//4+index)%3-1)*8;im.putpixel((x,y),tuple(max(0,min(255,c+delta)) for c in base)+(255,))
   for y in [2,28]:d.rectangle((0,y,31,y+1),fill=dark)
   d.rectangle((2,0,3,31),fill=dark);d.rectangle((28,0,29,31),fill=dark)
   glyph='#F79C42' if fire and lit else '#B9EFFB' if lit else '#D9D3BD'
   if 'tainted' in name:
    d.polygon([(4,29),(9,12),(15,4),(20,15),(27,9),(25,28)],fill='#29252D');d.line([(15,6),(12,14),(20,19),(15,27)],fill='#9C263D',width=2)
   elif 'tree' in name:
    d.line((16,4,16,27),fill=glyph,width=2)
    for y in [8,14,20]:d.line([(9,y-3),(16,y),(23,y-3)],fill=glyph,width=2)
   elif 'circle' in name:d.ellipse((7,7,24,24),outline=glyph,width=2);d.line((16,4,16,27),fill=glyph)
   else:
    d.line([(16,5),(25,16),(16,26),(7,16),(16,5)],fill=glyph,width=2);d.line((16,3,16,28),fill=glyph,width=1);d.line((5,16,27,16),fill=glyph,width=1)
   suffix='_lit' if lit else '';(A/'textures/block').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/block/{name}{suffix}.png')
   texture='slavicmyths:block/'+name+suffix
   if 'fire_idol_sun'==name:boxes=[([3,0,3],[13,11,13]),([0,5,5],[16,9,11]),([6,11,6],[10,16,10])]
   elif 'fire_idol_tree'==name:boxes=[([6,0,5],[10,16,11]),([1,8,6],[6,12,10]),([10,10,6],[15,14,10]),([1,12,6],[4,16,10])]
   elif 'fire_idol_circle'==name:boxes=[([3,0,3],[13,3,13]),([2,3,4],[5,14,12]),([11,3,4],[14,14,12]),([5,12,4],[11,16,12])]
   elif name=='unfading_brazier':boxes=[([0,0,0],[16,5,16]),([2,5,2],[14,10,14]),([0,10,0],[16,14,16]),([6,14,6],[10,16,10])]
   elif name=='tainted_spring_node':boxes=[([1,0,1],[15,6,15]),([4,6,4],[12,12,12]),([6,12,6],[10,16,10]),([0,7,5],[5,11,12])]
   else:boxes=[([0,0,0],[16,16,16])]
   elements=[{'from':lo,'to':hi,'faces':{face:{'texture':'#all','uv':[0,0,16,16]} for face in ['up','down','north','south','east','west']}} for lo,hi in boxes]
   write(A/f'models/block/{name}{suffix}.json',{'textures':{'all':texture,'particle':texture},'elements':elements})
  variants={}
  for face,y in [('north',0),('east',90),('south',180),('west',270)]:
   for lit in [False,True]:variants[f'facing={face},lit={str(lit).lower()}']={'model':f'slavicmyths:block/{name}'+('_lit' if lit else ''),'y':y}
  write(A/f'blockstates/{name}.json',{'variants':variants});write(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name})
 for name in BLOCK_NAMES:
  write(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:chest' if 'cache' in name else 'minecraft:stone_bricks'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
def loot():
 def entry(name,minimum=1,maximum=1):
  e={'type':'minecraft:item','name':name if ':' in name else 'slavicmyths:'+name}
  if maximum>1:e['functions']=[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':minimum,'max':maximum}}]
  return e
 for family in ['fire','spring']:
  base=['minecraft:bread','minecraft:coal','minecraft:string'] if family=='fire' else ['minecraft:bread','minecraft:glass_bottle','minecraft:wheat']
  ingredient='sacred_ember' if family=='fire' else 'pure_spring_drop';special=['purifying_ash','flame_vial'] if family=='fire' else ['purified_water_bottle'];rare=['fading_flame_charm'] if family=='fire' else ['purification_stone','spring_tear']
  pools=[{'rolls':{'type':'minecraft:uniform','min':1,'max':3},'entries':[entry(n,1,3) for n in base]}]
  for chance,items,maxcount in [(.65,[ingredient],2),(.3,special,1),(.125,rare,1),(.1,['ancient_rune_fragment'],1)]:
   pools.append({'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':chance}],'entries':[entry(n,1,maxcount) for n in items]})
  write(D/f'loot_table/chests/{family}_regular.json',{'type':'minecraft:chest','pools':pools})
  guaranteed=pools+[{'rolls':1,'entries':[entry(ingredient)]}]
  write(D/f'loot_table/chests/{family}_guaranteed.json',{'type':'minecraft:chest','pools':guaranteed})
  write(D/f'loot_table/chests/{family}_cache.json',{'type':'minecraft:chest','pools':[{'rolls':1,'entries':[entry('purifying_ash' if family=='fire' else 'purification_stone')]},{'rolls':1,'entries':[entry('ancient_rune_fragment')]},{'rolls':1,'entries':[entry('flame_vial' if family=='fire' else 'spring_tear')]}]})
def recipes():
 plans={'purifying_ash':(['sacred_ember','minecraft:paper'],'purifying_ash',2),'flame_vial':(['minecraft:glass_bottle','sacred_ember','purifying_ash'],'flame_vial',1),'fading_flame_charm':(['purifying_ash','sacred_ember','purification_stone','minecraft:iron_nugget','ancient_rune_fragment'],'fading_flame_charm',1),'purified_water_bottle':(['pure_spring_drop','minecraft:glass_bottle'],'purified_water_bottle',1),'spring_tear':(['pure_spring_drop','ancient_rune_fragment','minecraft:gold_nugget'],'spring_tear',1),'cleansing_powder_from_ash':(['purifying_ash','minecraft:bone_meal'],'cleansing_powder',4)}
 plans['blank_rune_1_from_ancient']=(['ancient_rune_fragment','minecraft:stone','minecraft:iron_nugget'],'blank_rune_1',1)
 for name,(inputs,result,count) in plans.items():
  write(D/f'recipe/{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':n if ':' in n else 'slavicmyths:'+n} for n in inputs],'result':{'id':'slavicmyths:'+result,'count':count}})
  criteria={f'has_{i}':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[n if ':' in n else 'slavicmyths:'+n]}]}} for i,n in enumerate(inputs)}
  write(D/f'advancement/recipes/sanctuaries/{name}.json',{'parent':'minecraft:recipes/root','criteria':criteria,'requirements':[[k] for k in criteria],'rewards':{'recipes':['slavicmyths:'+name]}})
def worldgen():
 for family,id in [('fire','sanctuary_unfading_fire'),('spring','abode_pure_spring')]:
  write(D/f'worldgen/structure/{id}.json',{'type':'slavicmyths:'+id,'biomes':'#slavicmyths:has_structure/'+id,'step':'surface_structures' if family=='fire' else 'underground_structures','terrain_adaptation':'none','spawn_overrides':{}})
  write(D/f'tags/worldgen/biome/has_structure/{id}.json',{'replace':False,'values':['minecraft:'+n for n in ['forest','birch_forest','old_growth_birch_forest','dark_forest','taiga','old_growth_pine_taiga','old_growth_spruce_taiga','windswept_forest','windswept_hills','meadow']]})
 write(D/'worldgen/structure_set/two_sanctuaries.json',{'structures':[{'structure':'slavicmyths:'+n,'weight':1} for n in ['sanctuary_unfading_fire','abode_pure_spring']],'placement':{'type':'minecraft:random_spread','spacing':48,'separation':20,'salt':1931013,'spread_type':'linear','exclusion_zone':{'other_set':'minecraft:strongholds','chunk_count':12}}})
def audio():
 ff=shutil.which('ffmpeg');assert ff;events={}
 for index,name in enumerate(SOUNDS):
  duration=1.7 if 'ambient' in name or name=='fire_restore' else .65;rate=22050;rnd=random.Random(193+index);smooth=0;samples=[]
  for i in range(int(duration*rate)):
   t=i/rate;u=t/duration;noise=rnd.uniform(-1,1);smooth=.9*smooth+.1*noise;env=min(1,t*40)*(1-u)**1.5
   if name.startswith('spring') or name in ['water_use','tear_use']:tone=(math.sin(math.tau*(510+index*12)*t)*math.exp(-t*5)*.14+smooth*.4+noise*.035)
   else:tone=math.sin(math.tau*(90+index*9)*t)*.12+smooth*.6+(noise*.15 if i%2000<100 else 0)
   samples.append(int(max(-.8,min(.8,tone*env))*32767))
  tmp=O/f'{name}.wav';dest=A/f'sounds/sanctuary193/{name}.ogg';dest.parent.mkdir(parents=True,exist_ok=True)
  with wave.open(str(tmp),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(struct.pack('<'+'h'*len(samples),*samples))
  subprocess.run([ff,'-hide_banner','-loglevel','error','-y','-i',str(tmp),'-fflags','+bitexact','-flags:a','+bitexact','-c:a','libvorbis','-q:a','3','-map_metadata','-1',str(dest)],check=True);tmp.unlink()
  events['sanctuary193.'+name]={'subtitle':'subtitle.slavicmyths.sanctuary193.'+name,'sounds':[{'name':'slavicmyths:sanctuary193/'+name,'attenuation_distance':20}]}
 merge(A/'sounds.json',events)
def language():
 desc={
 'sacred_ember':('Возобновляет огонь древней чаши один раз. Компонент пепла и склянки.','Restores an ancient brazier once; ash and vial ingredient.'),
 'purifying_ash':('Компонент очищающего порошка, огненной склянки и расходуемого оберега.','Ingredient for cleansing powder, flame vials and a consumable charm.'),
 'flame_vial':('Метательная: 3 урона и короткий огонь по враждебным мобам в радиусе 2,5. Заражённым духам +25%; блоки не поджигает.','Thrown: 3 damage and brief fire to hostile mobs within 2.5 blocks; +25% to blighted spirits; never burns blocks.'),
 'fading_flame_charm':('Одноразовый: на 40 с уменьшает огненный и моровой урон, длительность местных дебафов Мора на 30%. Не даёт полного иммунитета.','Consumed for 40s of 30% fire/Mor damage and local Mor duration reduction; not immunity.'),
 'ancient_rune_fragment':('Материал заготовки руны I (с камнем и железным самородком), оберега пламени и Слезы источника. Не меняет уровни рун.','Crafts a rune blank I with stone and an iron nugget, as well as the flame charm and spring tear; no tier changes.'),
 'pure_spring_drop':('Ингредиент очищенной воды и Слезы источника.','Ingredient for purified water and spring tears.'),
 'purified_water_bottle':('Сокращает обычные Замедление, Слабость и Отравление на 35%. Не снимает шкалы Мора, Морозко или Карачуна.','Reduces ordinary Slowness, Weakness and Poison durations by 35%; does not clear custom trial scales.'),
 'spring_tear':('Восстанавливает 8 здоровья за 8 с; ожидание 45 с, без складывания исцеления.','Restores 8 HP over 8s; 45s cooldown, no stacking.'),
 'purification_stone':('Редкая речная галька. Обязательный компонент оберега угасающего пламени.','Rare river stone; a required flame charm ingredient.')}
 ru={'sealed':'Древние печати ещё не позволяют войти. Хранители и фрагменты появятся позднее.','cache_sealed':'Тайник запечатан. Сначала восстанови огонь или очисти два бассейна.','already_restored':'Огонь уже восстановлен. Этот обряд нельзя повторять.','ember_needed':'Огонь едва теплится. Нужен один Священный уголь; нажми им на печать чаши.','fire_restored':'Древний огонь возобновлён. Боковой тайник открыт.','powder_needed':'Очисти камень одним очищающим порошком. Можно также разрушить сам нарост.','pool_clean':'Этот бассейн вновь чист.','spring_restored':'Воды источника вновь чисты. Боковой тайник открыт.','spring_hint':'Источник жив. Два дальних бассейна ещё нуждаются в очищении.','tear_wait':'Слеза источника ещё не восстановилась.'}
 en={'sealed':'Ancient seals still bar entry. Guardians and fragments come later.','cache_sealed':'Restore the fire or cleanse both pools first.','already_restored':'The fire is already restored; this ritual cannot be repeated.','ember_needed':'The fire needs one Sacred Ember. Use it on the brazier seal.','fire_restored':'The ancient fire is restored; the side cache opens.','powder_needed':'Use one cleansing powder on the node, or break the tainted growth.','pool_clean':'This pool is clean again.','spring_restored':'The pools are clean; the side cache opens.','spring_hint':'The spring lives. Two distant pools still need cleansing.','tear_wait':'The spring tear has not recovered yet.'}
 tales={'sanctuary_unfading_fire':('Святилище незатухающего огня','Sanctuary of the Unfading Fire','Там, где некогда не угасал очистительный огонь, тлеет малая искра. Древние пытались сдержать Хворь пламенем. Найди Священный уголь в ритуальном сундуке склада и используй на печати большой чаши: откроется только боковой тайник.','An ember remains where purifying fire once held the blight back. The ritual store has a guaranteed Sacred Ember. Use it on the great brazier seal to open only a side cache.'),
 'abode_pure_spring':('Обитель чистого источника','Abode of the Pure Spring','Вода ещё хранит память о чистоте, но скверна коснулась двух боковых бассейнов. Их источники — багровые камни. Очищай порошком или разрушь наросты: источник откроет побочный тайник, а главный зал останется запечатан.','The spring remembers purity, but two side pools are tainted. Clean their crimson nodes with powder or break them to open a side cache. The relic chamber remains sealed.'),
 'two_sanctuaries':('Две древние святыни','Two Ancient Sanctuaries','Огонь сдерживает, вода очищает. Две части древнего оберега укрыты порознь, но залы пока молчат. Это игровая интерпретация, не историческая атрибуция. Хранители и фрагменты относятся к 1.9.4; этот оберег от Мора не связан с будущим оберегом двух зим для Ада.','Fire holds back; water purifies. Two ward parts were hidden separately. These temples are game interpretations, not historical claims. Guardians and fragments come in 1.9.4; this Mor ward is distinct from the future two-winter ward.')}
 advancements={'found_ancient_sanctuary':('Древняя защита','Ancient Protection','Найди одну из двух древних святынь','Find either ancient sanctuary'),'restored_sanctuary_fire':('Незатухающий огонь','Unfading Fire','Возобнови огонь древней чаши','Restore the ancient brazier'),'cleansed_sanctuary_pool':('Память чистоты','Memory of Purity','Очисти местный бассейн источника','Cleanse a spring pool'),'found_two_sanctuaries':('Огонь и вода','Fire and Water','Найди обе разные святыни','Discover both distinct sanctuaries')}
 for locale,text,ix in [('ru_ru',ru,0),('en_us',en,1)]:
  out={'sanctuary193.'+k:v for k,v in text.items()}
  for k,v in ITEM_NAMES.items():out['item.slavicmyths.'+k]=v[ix];out['item.slavicmyths.'+k+'.desc']=desc[k][ix]
  for k,v in BLOCK_NAMES.items():out['block.slavicmyths.'+k]=v[ix]
  for k,v in SOUNDS.items():out['subtitle.slavicmyths.sanctuary193.'+k]=v[ix]
  out['entity.slavicmyths.flame_vial']=ITEM_NAMES['flame_vial'][ix];out['effect.slavicmyths.sanctuary_flame_guard']='Оберег пламени' if ix==0 else 'Flame Guard';out['effect.slavicmyths.spring_tear_healing']=ITEM_NAMES['spring_tear'][ix]
  for k,v in tales.items():
   out['tales.entry.'+k+'.title']=v[ix];out['structure.slavicmyths.'+k]=v[ix]
   for stage in ['known','rumor','seen','discovered','studied','verified']:out['tales.entry.'+k+'.'+stage]=v[ix+2]
  for k,v in advancements.items():out['advancement.slavicmyths.'+k]=v[ix];out['advancement.slavicmyths.'+k+'.desc']=v[ix+2]
  merge(A/f'lang/{locale}.json',out)
 for k,v in tales.items():write(D/f'tales/{k}.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tales.entry.'+k+'.title','icon':'slavicmyths:'+('sacred_ember' if k.startswith('sanctuary') else 'pure_spring_drop' if k.startswith('abode') else 'ancient_rune_fragment'),'initial':0,'subjects':['structure/slavicmyths:'+k] if k!='two_sanctuaries' else [],'advancements':['slavicmyths:'+('found_two_sanctuaries' if k=='two_sanctuaries' else 'found_ancient_sanctuary')],'links':[]})
 for k,v in advancements.items():write(D/f'advancement/{k}.json',{'parent':'slavicmyths:root','display':{'icon':{'id':'slavicmyths:'+('sacred_ember' if k.endswith('fire') else 'pure_spring_drop' if k.endswith('pool') else 'ancient_rune_fragment')},'title':{'translate':'advancement.slavicmyths.'+k},'description':{'translate':'advancement.slavicmyths.'+k+'.desc'},'frame':'goal' if 'two' in k else 'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}})
def main():
 O.mkdir(parents=True,exist_ok=True);spec=R/'docs/specs/1.9.3';rows=[re.match(r'([a-f0-9]{64})  (.+)',s) for s in (spec/'00_START_HERE/04_SHA256_MANIFEST.txt').read_text(encoding='utf-8').splitlines()];rows=[m for m in rows if m];assert rows and all(hashlib.sha256((spec/m[2]).read_bytes()).hexdigest()==m[1] for m in rows)
 layouts=[fire().save(),spring().save()];icons();block_assets();loot();recipes();worldgen();audio();language();print('SANCTUARY_ASSETS_PASS',len(rows),'spec files;',sum(len(l['tiles']) for l in layouts),'native tiles;',sum(l['blocks'] for l in layouts),'authored blocks')
if __name__=='__main__':main()

# Preserve owner-reported decor fixes after this historical pipeline.
from decor108_assets import apply as apply_decor108
apply_decor108()
