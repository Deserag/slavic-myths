"""Woodlands & Timber resource layer. Own pixels; vanilla 1.16.5 state/model schemas."""
from pathlib import Path
import json,zipfile,math
from bandits_080 import png,write
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';D=RES/'data/slavicmyths'
SPECIES=('linden','rowan','willow','pine')
KINDS=('log','wood','stripped_log','stripped_wood','planks','leaves','sapling','stairs','slab','fence','fence_gate','door','trapdoor','pressure_plate','button','sign','wall_sign')
PALETTES=[((113,105,86),(217,187,125),(71,120,44)),((121,111,99),(178,133,99),(74,119,48)),((96,96,74),(174,166,114),(91,119,65)),((147,82,43),(174,126,64),(54,97,48))]
def tint(c,n):return tuple(max(0,min(255,v+n)) for v in c)+(255,)
def texture(name,fn,w=16,h=16,folder='block'):png(A/f'textures/{folder}/{name}.png',w,h,fn)
def pixel_textures(s,i):
 bark,wood,leaf=PALETTES[i]
 # Authored repeatable motifs, no sampled vanilla images or hue shifted leaves.
 def side(x,y):
  if i==0: n=-17 if (x+(y//7)%2)%5==0 else (5 if x%5==1 else 0)
  elif i==1:n=-23 if (y%7==2 and (x+y//7*3)%9<3) else 3*((x//5+y//8)%2)
  elif i==2:n=-26 if (x+y//5)%6<2 else (9 if (x+y//5)%6==2 else -3)
  else:n=-30 if y%6==0 or (x+3*(y//6))%7==0 else (12 if (x+y//6)%7==1 else 0)
  return tint(bark,n)
 texture(s+'_log',side)
 def cut(x,y):
  dx=x-7.2;dy=y-8;radius=math.sqrt(dx*dx+dy*dy*.84);n=-13 if int(radius*1.2)%3==0 else 4
  if radius<2:n-=10
  if radius>7:return side(x,y)
  return tint(wood,n)
 texture(s+'_log_top',cut)
 texture(s+'_stripped_log',lambda x,y:tint(wood,-12 if (x+(y//8))%6==0 else (4 if x%3==0 else 0)))
 texture(s+'_stripped_log_top',lambda x,y:tint(wood,-15 if int(math.hypot(x-7,y-8))%3==0 else 3))
 def boards(x,y):
  period=[4,5,4,8][i];offset=(y//period)*[5,7,3,9][i]
  if y%period==0:return tint(wood,-32)
  if (x+offset)%16==0:return tint(wood,-24)
  n=5 if y%period==1 else (-7 if (x+y*3+i)%13<4 else 0)
  return tint(wood,n)
 texture(s+'_planks',boards)
 def foliage(x,y,berries=False):
  # Linden: clustered small hearts. Rowan: compound leaflets. Willow: narrow blades. Pine: needle fans.
  if i==0:
   u=(x+(y//4%2)*2)%5;v=y%4;solid=(v==0 and u in (1,3))or(v==1 and u<5)or(v==2 and 1<=u<=3)or(v==3 and u==2);shade=(u-v)*4
  elif i==1:
   u=(x+(y//8)*3)%8;v=y%8;solid=u==3 or (v in (1,3,5) and u in (1,2,4,5))or(v in (2,4,6)and u in (0,6));shade=(v%3-1)*10
  elif i==2:
   u=(x+y//5)%5;v=y%8;solid=(u==2 and v<7)or(u in (1,3)and 1<=v<=4);shade=8 if u==1 else -6
  else:
   u=x%8;v=y%8;solid=u==3 or (v in (2,4,6)and abs(u-3)<=3)or(u-v==0 and v<4)or(u+v==7 and v<4);shade=8 if v%2==0 else -12
  if berries and ((x-2)%9,(y-3)%10) in ((0,0),(2,0),(1,2),(3,2)):
   return (213,74 if x%2 else 92,29,255)
  return tint(leaf,shade) if solid else (0,0,0,0)
 texture(s+'_leaves',foliage)
 if s=='rowan':texture('rowan_leaves_berries',lambda x,y:foliage(x,y,True))
 def sap(x,y):
  if x in (7,8)and y>=6:return tint(bark,0)
  if i==3:solid=3<=y<=11 and abs(x-7)<=((12-y)//3+1) and (x+y)%3!=0
  elif i==2:solid=3<=y<=11 and ((x in (3,5,10,12)and y>=5)or(y<6 and 3<=x<=12))
  elif i==1:solid=3<=y<=9 and ((x-4)**2+(y-5)**2<7 or(x-10)**2+(y-7)**2<7)
  else:solid=(x-7)**2/28+(y-5)**2/13<1
  return tint(leaf,(x%3)*5)if solid else (0,0,0,0)
 texture(s+'_sapling',sap)
 def door(x,y):
  # Four independently laid out joinery patterns, shared only their species material.
  widths=[4,3,5,6];n=-22 if x%widths[i]==0 else 2
  if i==0:
   if y in (14,15,16):n=-16
   if 5<=y<=9 and abs(x-7)==abs(y-7):n=-28
  elif i==1:
   if y<7 and x in (3,7,11):return (0,0,0,0)
   if (x+y)%19<2:n=-23
  elif i==2:
   if y<8 and x in (4,9):return (0,0,0,0)
   if abs(x-(y//2))<2:n=-30
  else:
   if y in (6,7,24,25):n=-28
   if x<3 and y in (6,7,24,25):return (63,61,51,255)
  if x==12 and y in (17,18):return (58,52,39,255)
  return tint(wood,n)
 texture(s+'_door_bottom',lambda x,y:door(x,y+16));texture(s+'_door_top',door)
 texture(s+'_door',lambda x,y:door(x,(y*2)),folder='item')
 def trap(x,y):
  if x<2 or x>13 or y<2 or y>13:return tint(wood,-27)
  if i==0 and x in (5,10)and 5<=y<=10:return (0,0,0,0)
  if i==1 and (x+y)%7==0:return (0,0,0,0)
  if i==2 and x in (5,10):return (0,0,0,0)
  if i==3 and x in (3,12)and y in (3,12):return (65,63,55,255)
  return tint(wood,-16 if x%[4,3,5,6][i]==0 else 2)
 texture(s+'_trapdoor',trap)
 texture(s+'_sign',lambda x,y:tint(wood,-23 if x in (1,14)or y in (2,9)else 0)if(1<=x<=14 and 2<=y<=9)or(7<=x<=8 and 9<=y<=15)else(0,0,0,0),folder='item')
 texture(s,lambda x,y:boards(x%16,y%16),64,32,'entity/signs')
def convert(obj,s):
 # Only species identifiers are remapped, vanilla model parents stay vanilla.
 text=json.dumps(obj)
 for k in sorted(KINDS,key=len,reverse=True):
  text=text.replace('minecraft:oak_'+k,'slavicmyths:'+s+'_'+k).replace('minecraft:stripped_oak_'+k,'slavicmyths:'+s+'_stripped_'+k)
 text=text.replace('minecraft:block/stripped_oak_','slavicmyths:block/'+s+'_stripped_').replace('minecraft:block/oak_','slavicmyths:block/'+s+'_').replace('minecraft:item/oak_','slavicmyths:item/'+s+'_')
 return json.loads(text)
def merge_tag(ns,folder,tag,values):
 path=RES/f'data/{ns}/tags/{folder}/{tag}.json';data=json.loads(path.read_text())if path.exists()else {'replace':False,'values':[]}
 data['values']=list(dict.fromkeys(data['values']+values));write(path,data)
def generate():
 cache=Path.home()/'.gradle/caches/forge_gradle/minecraft_repo/versions/1.16.5/client-extra.jar'
 with zipfile.ZipFile(cache) as vanilla:
  names=vanilla.namelist()
  for i,s in enumerate(SPECIES):
   pixel_textures(s,i)
   for k in KINDS:
    v=('stripped_oak_'+k[len('stripped_'):])if k.startswith('stripped_')else 'oak_'+k
    for folder in ['blockstates','models/item']:
     path=f'assets/minecraft/{folder}/{v}.json'
     if path in names:write(A/f'{folder}/{s}_{k}.json',convert(json.loads(vanilla.read(path)),s))
    loot=convert(json.loads(vanilla.read('data/minecraft/loot_tables/blocks/'+('oak_sign' if k=='wall_sign' else v)+'.json')),s)
    if k=='leaves':loot['pools']=[pool for pool in loot['pools']if 'minecraft:apple'not in json.dumps(pool)]
    write(D/f'loot_tables/blocks/{s}_{k}.json',loot)
   for path in names:
    if path.startswith('assets/minecraft/models/block/oak_')or path.startswith('assets/minecraft/models/block/stripped_oak_'):
     name=Path(path).stem.replace('stripped_oak_',s+'_stripped_').replace('oak_',s+'_')
     write(A/f'models/block/{name}.json',convert(json.loads(vanilla.read(path)),s))
   # Override item parents for stripped IDs and leaf models use authored colors, not biome tint.
   write(A/f'models/block/{s}_leaves.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'slavicmyths:block/{s}_leaves'}})
   for k in ['door','sign']:write(A/f'models/item/{s}_{k}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{s}_{k}'}})
   for k in ['sign','wall_sign']:write(A/f'models/block/{s}_{k}.json',{'textures':{'particle':f'slavicmyths:block/{s}_planks'}})
   for k in ['log','wood','stripped_log','stripped_wood']:
    # Per-family logs tag includes stripped forms; do not broaden other species tags.
    for folder in ['blocks','items']:merge_tag('slavicmyths',folder,s+'_logs',['slavicmyths:'+s+'_'+k])
   for folder in ['blocks','items']:
    for tag,kind in [('logs',None),('logs_that_burn',None),('planks','planks'),('leaves','leaves'),('saplings','sapling'),('wooden_stairs','stairs'),('wooden_slabs','slab'),('wooden_fences','fence'),('wooden_doors','door'),('wooden_trapdoors','trapdoor'),('wooden_buttons','button'),('wooden_pressure_plates','pressure_plate'),('signs','sign')]:
     if f'data/minecraft/tags/{folder}/{tag}.json' in names:merge_tag('minecraft',folder,tag,['#slavicmyths:'+s+'_logs'if kind is None else 'slavicmyths:'+s+'_'+kind])
    merge_tag('forge',folder,'fences/wooden',['slavicmyths:'+s+'_fence']);merge_tag('forge',folder,'fence_gates/wooden',['slavicmyths:'+s+'_fence_gate'])
   for tag,kind in [('fence_gates','fence_gate'),('standing_signs','sign'),('wall_signs','wall_sign')]:merge_tag('minecraft','blocks',tag,['slavicmyths:'+s+'_'+kind])
   for path in names:
    if path.startswith('data/minecraft/recipes/oak_')or path.startswith('data/minecraft/recipes/stripped_oak_'):
     name=Path(path).stem.replace('stripped_oak_',s+'_stripped_').replace('oak_',s+'_')
     if name.endswith('_boat'):continue
     obj=convert(json.loads(vanilla.read(path)),s)
     # Convert vanilla family ingredient tag.
     obj=json.loads(json.dumps(obj).replace('#minecraft:oak_logs','#slavicmyths:'+s+'_logs').replace('minecraft:oak_logs','slavicmyths:'+s+'_logs'))
     write(D/f'recipes/{name}.json',obj)
     result=obj['result'];item=result['item']if isinstance(result,dict)else result
     write(D/f'advancements/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_wood':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'tag':'slavicmyths:'+s+'_logs'}]}},'has_the_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'slavicmyths:'+name}}},'requirements':[['has_wood','has_the_recipe']],'rewards':{'recipes':['slavicmyths:'+name]}})
 # Rowan berry state is deliberately not copied to item loot: shearing never stores a free harvest.
 write(A/'blockstates/rowan_leaves.json',{'variants':{'berries=false':{'model':'slavicmyths:block/rowan_leaves'},'berries=true':{'model':'slavicmyths:block/rowan_leaves_berries'}}})
 write(A/'models/block/rowan_leaves_berries.json',{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/rowan_leaves_berries'}})
 texture('hanging_willow_leaves',lambda x,y:tint(PALETTES[2][2],6 if x%3==0 else -5)if ((x+y//6)%6 in (1,2)and y%6<5)or(x%6==2)else(0,0,0,0))
 write(A/'models/block/hanging_willow_leaves.json',{'parent':'minecraft:block/cross','textures':{'cross':'slavicmyths:block/hanging_willow_leaves'}})
 write(A/'blockstates/hanging_willow_leaves.json',{'variants':{'':{'model':'slavicmyths:block/hanging_willow_leaves'}}})
 write(A/'models/item/hanging_willow_leaves.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:block/hanging_willow_leaves'}})
 write(D/'loot_tables/blocks/hanging_willow_leaves.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:hanging_willow_leaves','conditions':[{'condition':'minecraft:match_tool','predicate':{'item':'minecraft:shears'}},{'condition':'minecraft:survives_explosion'}]}]}]})
 def berries(x,y):
  if(y==3 and 5<=x<=11)or(x==8 and 2<=y<=7):return (101,84,40,255)
  for cx,cy in [(5,7),(9,7),(12,10),(7,11),(4,11)]:
   if (x-cx)**2+(y-cy)**2<=2:return (232,101,34,255)if x<cx else(175,51,25,255)
  return (0,0,0,0)
 texture('rowan_berries',berries,folder='item');write(A/'models/item/rowan_berries.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/rowan_berries'}})
 criteria={s:{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'tag':'slavicmyths:'+s+'_logs'}]}}for s in SPECIES}
 for name,allfour in [('woodlands',False),('all_timber',True)]:
  write(D/f'advancements/{name}.json',{'parent':'slavicmyths:woodlands'if allfour else 'slavicmyths:root','display':{'icon':{'item':'slavicmyths:pine_log'if allfour else 'slavicmyths:linden_log'},'title':{'translate':f'advancement.slavicmyths.{name}.title'},'description':{'translate':f'advancement.slavicmyths.{name}.description'},'frame':'challenge'if allfour else 'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':criteria,'requirements':[[s]for s in SPECIES]if allfour else[list(SPECIES)]})
 for lang in ['en_us','ru_ru']:
  data=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));ru=lang=='ru_ru'
  en=['Log','Wood','Stripped Log','Stripped Wood','Planks','Leaves','Sapling','Stairs','Slab','Fence','Fence Gate','Door','Trapdoor','Pressure Plate','Button','Sign','Wall Sign']
  stems=['липы','рябины','ивы','сосны'];labels=['Бревно','Древесина','Обтёсанное бревно','Обтёсанная древесина','Доски','Листва','Саженец','Ступени','Плита','Забор','Калитка','Дверь','Люк','Нажимная плита','Кнопка','Табличка','Настенная табличка']
  for i,s in enumerate(SPECIES):
   for k,a,b in zip(KINDS,en,labels):data['block.slavicmyths.'+s+'_'+k]=b+' '+stems[i]if ru else s.title()+' '+a
  data.update({'item.slavicmyths.rowan_berries':'Ягоды рябины'if ru else 'Rowan Berries','block.slavicmyths.hanging_willow_leaves':'Свисающая листва ивы'if ru else 'Hanging Willow Leaves','woodlands.command.grown':'Выращено %s: %s, %s, %s'if ru else 'Grown %s: %s, %s, %s','woodlands.command.blocked':'Нет подходящего свободного места перед игроком.'if ru else 'No suitable clear ground in front of the player.','advancement.slavicmyths.woodlands.title':'Лес иного рода'if ru else 'Another Kind of Woodland','advancement.slavicmyths.woodlands.description':'Получите древесину новой породы'if ru else 'Obtain timber from a new tree species','advancement.slavicmyths.all_timber.title':'От липы до сосны'if ru else 'From Linden to Pine','advancement.slavicmyths.all_timber.description':'Получите древесину всех четырёх пород'if ru else 'Obtain timber from all four species','book.slavicmyths.woodlands.title':'Деревья родной земли'if ru else 'Trees of the Homeland','book.slavicmyths.woodlands.text':'Липа раскинула широкую крону над светлым стволом. Небольшая рябина растёт среди других деревьев: её ягодные гроздья можно собрать рукой. На живой листве у ветвей ягоды со временем восстановятся. Ива тянется к берегам, опуская тонкие листья над водой. Сосна поднимает неровные хвойные ветви высоко над открытым стволом. Древесина всех четырёх пород годится для досок, дверей, оград и табличек. Посадите саженец на землю и помогите ему костной мукой.'if ru else 'Linden spreads a broad crown above pale timber. Small rowans grow among other trees; pick their berry clusters by hand. Berries slowly regrow on living leaves near branches. Willows favour banks, trailing narrow leaves over water. Pines raise uneven needle boughs above an open trunk. All four timbers make planks, doors, fences and signs. Plant a sapling on soil and encourage it with bone meal.'})
  write(A/f'lang/{lang}.json',data)
 write(ROOT/'docs/verification/woodlands-0.8.0.1.json',{'species':SPECIES,'kinds':KINDS,'blocks':[s+'_'+k for s in SPECIES for k in KINDS]+['hanging_willow_leaves'],'items':[s+'_'+k for s in SPECIES for k in KINDS if k!='wall_sign']+['hanging_willow_leaves','rowan_berries']})
if __name__=='__main__':generate()
