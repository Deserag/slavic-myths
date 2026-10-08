"""Authored city interiors: real mod furniture, finite supplies and supported lights."""
import village_buildings as v

PROFILES={
 'home':['slavicmyths:rye_bread','slavicmyths:oat_groats','slavicmyths:linen_thread','slavicmyths:dried_berries','minecraft:apple'],
 'guard':['minecraft:arrow','minecraft:leather','slavicmyths:rye_bread','slavicmyths:linen_cloth','minecraft:torch'],
 'cook':['slavicmyths:wheat_flour','slavicmyths:barley_groats','slavicmyths:dried_mushrooms','slavicmyths:rye_bread','slavicmyths:pea_soup'],
 'brewer':['slavicmyths:malted_barley','slavicmyths:hops','slavicmyths:wooden_mug','slavicmyths:light_beer_mug','slavicmyths:kvass_mug'],
 'weaver':['slavicmyths:flax_fiber','slavicmyths:linen_thread','slavicmyths:linen_cloth','minecraft:string','slavicmyths:linen_headscarf'],
 'knowledge':['minecraft:book','minecraft:paper','minecraft:feather','minecraft:ink_sac','minecraft:map','slavicmyths:linen_cloth'],
 'herbal':['slavicmyths:dried_berries','slavicmyths:dried_mushrooms','minecraft:honey_bottle','minecraft:glass_bottle','slavicmyths:herb_pouch'],
 'smith':['minecraft:coal','minecraft:iron_nugget','slavicmyths:iron_rings','minecraft:leather','minecraft:flint'],
 'trading':['slavicmyths:rye_bread','slavicmyths:linen_cloth','slavicmyths:wooden_mug','slavicmyths:dried_berries','minecraft:paper','slavicmyths:ancient_coin'],
 'princely':['slavicmyths:honey_bread','slavicmyths:linen_cloth','slavicmyths:mead_bottle','minecraft:book','minecraft:gold_nugget','slavicmyths:ancient_coin']}

def items(values):
 return [{'Slot':i,'id':name,'count':count} for i,(name,count) in enumerate(values)]

def storage(b,p,block,values,facing='east',**props):
 v.be(b,*p,'slavicmyths:'+block,{'id':'slavicmyths:household_storage','Items':items(values)},facing=facing,**props)

def table(b,p,foods):
 v.be(b,*p,'slavicmyths:pine_table',{'id':'slavicmyths:table_display','Items':items([(food,1) for food in foods])},facing='north',connection='none')

def light(b,x,y,z,wood='spruce'):
 # A real beam attaches the hanging lantern; no floating source lost on updates.
 b.put(x,y+1,z,wood+'_log',axis='x');v.lamp(b,x,y,z)

def empty(b,p):return b.blocks.get(p,({'Name':'minecraft:air'},None))[0]['Name']=='minecraft:air'

def put_empty(b,p,block,**props):
 if empty(b,p):b.put(*p,block,**props);return True
 return False

def furnish_urban(b,climate,family,kind,role,x0,x1,z0,z1,cx,floors,frame,beds):
 profile='trading' if kind=='trading' else 'guard' if kind=='guard' else 'smith' if kind=='forge' else 'weaver' if role=='slavicmyths:weaver' else 'brewer' if role=='slavicmyths:brewer' else 'cook' if role=='slavicmyths:cook' else 'knowledge' if kind=='knowledge' else 'herbal' if kind=='herbalist' else 'home'
 upper=6 if floors>1 else 1
 # Replace the generic chest contents with the actual household/workshop theme.
 v.be(b,x1-1,upper,z1-1,'chest',{'id':'minecraft:chest','LootTable':'slavicmyths:chests/gorodishche/'+profile},facing='west',type='single',waterlogged=False)
 meal='slavicmyths:rye_bread' if kind in ['guard','forge','craft'] else 'slavicmyths:honey_bread' if kind=='rich' else 'slavicmyths:oat_porridge'
 # All meals on display belong to the real table-food tag.
 if meal=='slavicmyths:honey_bread':meal='slavicmyths:honey_bliny'
 table(b,(7 if kind=='trading' else x0+2,1,z0+2 if kind=='trading' else z0+4),[meal,'minecraft:apple'])
 shelf=(x0+1,2,z0+5)
 storage(b,shelf,'wall_shelf',[(item,1) for item in PROFILES[profile][:3]])
 put_empty(b,(x0+1,1,z0+5),'slavicmyths:pine_bench',facing='east')
 put_empty(b,(x0+1,1,z0+6),'slavicmyths:pine_bedside_cabinet',facing='east')
 # Rear sitting area and rug stay off the door and the right-hand stair lane.
 for dz in range(3):put_empty(b,(cx,1,z1-5+dz),'blue_carpet' if climate=='cold' else 'red_carpet')
 if kind!='trading':
  p=(cx+1,1,z1-3)
  if empty(b,p):table(b,p,['slavicmyths:berry_bliny'])
  put_empty(b,(cx+1,1,z1-4),'slavicmyths:pine_chair',facing='south')
  put_empty(b,(cx+2,1,z1-3),'slavicmyths:pine_stool',facing='west')
 if floors>1:
  # Bedroom wardrobe, cabinet and a stocked personal shelf; no bed is overwritten.
  p=(x1-1,6,z1-3)
  if empty(b,p) and empty(b,(p[0],7,p[2])):
   for dy in [0,1]:b.put(p[0],6+dy,p[2],'slavicmyths:pine_wardrobe',facing='west',half='lower' if dy==0 else 'upper')
  p=(x1-1,7,z1-4)
  if empty(b,p):storage(b,p,'wall_shelf',[('minecraft:book',1),('slavicmyths:linen_thread',2)])
  for x,y,z in beds:put_empty(b,(x,y,z1-1),'slavicmyths:pine_bedside_cabinet',facing='south')
  for zz in [z0+5,z1-4]:light(b,cx,9,zz,frame)
 # Four sources on the lower floor, with fixed beams above head clearance.
 for xx in [6 if kind=='trading' else x0+3,x1-3]:
  for zz in [z0+5,z1-4]:light(b,xx,4,zz,frame)
 # Entrance lantern is hung from its existing porch roof by a short crosspiece.
 light(b,cx+1,3,1,frame)
 if profile in ['cook','brewer']:
  storage(b,(x0+2,1,z1-2),'storage_sack',[('slavicmyths:barley_grain',64),('slavicmyths:oat_grain',16)],fill=1)
 if profile=='weaver':
  p=(x0+1,1,z1-2)
  if empty(b,p):storage(b,p,'household_chest',[('slavicmyths:flax_fiber',8),('slavicmyths:linen_thread',6)],open=False)
 if profile in ['smith','guard']:
  p=(x0+2,1,z0+3)
  if empty(b,p) or b.blocks[p][0]['Name'].endswith('weapon_rack'):v.be(b,*p,'slavicmyths:pine_weapon_rack',{'id':'slavicmyths:weapon_rack','Weapon':{'id':'minecraft:stone_axe' if profile=='smith' else 'minecraft:bow','count':1}},facing='north')
 if profile in ['knowledge','herbal']:
  put_empty(b,(x0+1,1,z1-2),'bookshelf' if profile=='knowledge' else 'potted_fern')

def furnish_princely(b):
 for p,(state,tag) in list(b.blocks.items()):
  if tag and tag.get('LootTable')=='slavicmyths:chests/village/home':b.blocks[p]=(state,{**tag,'LootTable':'slavicmyths:chests/gorodishche/princely'})
 for x in [11,19,26]:
  b.put(x-1,9,22,'slavicmyths:pine_bedside_cabinet',facing='east')
  storage(b,(x+1,10,24),'wall_shelf',[('minecraft:book',1),('slavicmyths:linen_headscarf',1)],facing='north')
 for x in [10,28]:
  for z in [10,18,24]:light(b,x,7,z,'dark_oak')
 for x in [12,26]:
  for z in [12,21]:light(b,x,12,z,'dark_oak')
 for z in [16,19,22]:table(b,(13,4,z),['slavicmyths:meat_pie','slavicmyths:honey_bliny','minecraft:apple'])
 storage(b,(11,5,9),'wall_shelf',[('slavicmyths:wooden_mug',1),('slavicmyths:mead_bottle',1)],facing='south')
 storage(b,(5,4,22),'storage_sack',[('slavicmyths:rye_grain',64),('minecraft:wheat',32)],fill=1)
 for x in [4,34]:
  for z in [12,22]:light(b,x,7,z,'spruce')
 for x in [3,33]:table(b,(x,4,12),['slavicmyths:rye_bread','slavicmyths:berry_bliny'])
 for x in [2,35]:
  for dy in [0,1]:b.put(x,4+dy,18,'slavicmyths:pine_wardrobe',facing='east' if x==2 else 'west',half='lower' if dy==0 else 'upper')
 for x in [11,27]:light(b,x,7,5,'dark_oak')

def furnish_reused(b,kind):
 for p,(state,tag) in list(b.blocks.items()):
  name=state['Name']
  if name=='slavicmyths:storage_sack':storage(b,p,'storage_sack',[('slavicmyths:rye_grain',64),('minecraft:wheat',16)],fill=1)
  elif name=='slavicmyths:pine_table':table(b,p,['slavicmyths:rye_bread','minecraft:apple'])
  elif name=='slavicmyths:pine_weapon_rack':v.be(b,*p,name,{'id':'slavicmyths:weapon_rack','Weapon':{'id':'minecraft:bow','count':1}},**state.get('Properties',{}))
  elif name=='minecraft:chest' and kind=='druzhinnik_barracks' and tag:b.blocks[p]=(state,{**tag,'LootTable':'slavicmyths:chests/gorodishche/guard'})

def loot_tables():
 for name,values in PROFILES.items():
  food=name in ['home','cook','brewer','trading','princely']
  entries=[]
  for item in values:
   # Ordinary supplies dominate; finished equipment / meals / coins are finite.
   count=1 if any(w in item for w in ['mug','bottle','soup','pouch','headscarf','coin','bread','map']) else 4
   entries.append({'type':'minecraft:item','name':item,'weight':2 if item.endswith('ancient_coin') else 6,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':count}}]})
  pools=[{'rolls':{'type':'minecraft:uniform','min':4,'max':7},'entries':entries}]
  if food:pools.append({'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:rye_bread','functions':[{'function':'minecraft:set_count','count':2}]}]})
  v.write('data/slavicmyths/loot_table/chests/gorodishche/'+name+'.json',{'type':'minecraft:chest','pools':pools})
