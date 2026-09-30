"""Final 0.6.5 resource layer. Native cuboid models reuse vanilla materials."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths'
def write(p,obj):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def cube(a,b,tex,rot=None):
 e={'from':a,'to':b,'faces':{f:{'texture':'#'+tex} for f in ('north','south','east','west','up','down')}}
 if rot:e['rotation']={'origin':rot[0],'axis':rot[1],'angle':rot[2],'rescale':False}
 return e
def model(path,els,textures,display=None):
 m={'parent':'minecraft:block/block','ambientocclusion':True,'textures':textures,'elements':els}
 if display:m['display']=display
 write(A/('models/'+path+'.json'),m)
def drop(name,item,conditions=[]):write(D/f'loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item}],'conditions':[{'condition':'minecraft:survives_explosion'}]+conditions}]})
def shaped(name,pattern,key):write(D/f'recipes/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v if ':' in v else 'slavicmyths:'+v} for k,v in key.items()},'result':{'item':'slavicmyths:'+name}})
def generate():
 for red in (True,False):
  name='raspberry_bush' if red else 'blueberry_bush';variants={}
  for age in range(4):
   for upper in (False,True):
    half='upper' if upper else 'lower';els=[]
    if red:
     h=[5,12,16,16][age] if not upper else [1,1,13,13][age]
     for x,z in [(5,7),(9,9),(8,5)]:els.append(cube([x,0,z],[x+1,h,z+1],'stem'))
     clusters=[(5,2,5,5,2),(8,5,8,5,3),(2,8,6,5,3),(8,11,3,6,3)] if not upper else [(2,1,6,5,4),(8,3,4,6,4),(5,8,6,5,3)]
     if age==0:clusters=[(5,2,5,5,2)]
     elif age==1:clusters=clusters[:2]
    else:
     h=[3,6,9,9][age]
     for x,z in [(4,7),(9,5),(10,10)]:els.append(cube([x,0,z],[x+1,h,z+1],'stem'))
     clusters=[(1,3,3,7,5),(7,4,7,8,5),(5,7,1,7,4)] if age>=2 else [(4,2,4,8,3)]
    for x,y,z,w,h in clusters:els.append(cube([x,y,z],[min(16,x+w),y+h,min(16,z+w*.7)],'leaf'))
    if age==3:
     coords=[(3,9,5),(11,6,10),(7,13,3)] if red and not upper else [(3,4,5),(10,7,4),(6,9,8)] if red else [(2,6,3),(11,7,11),(7,9,1)]
     for x,y,z in coords:
      for dx,dy,dz in [(0,0,0),(1,-1,.5),(-.6,-1,.6)]:els.append(cube([x+dx,y+dy,z+dz],[x+dx+1.3,y+dy+1.5,z+dz+1.3],'berry'))
    textures={'stem':'minecraft:block/dark_oak_log','leaf':'minecraft:block/oak_leaves' if red else 'minecraft:block/spruce_leaves','berry':'minecraft:block/red_concrete' if red else 'minecraft:block/blue_terracotta','particle':'minecraft:block/oak_leaves' if red else 'minecraft:block/spruce_leaves'}
    for element in els:
     for face in element['faces'].values():
      if face['texture']=='#leaf':face['tintindex']=0
    model(f'block/{name}_{age}_{half}',els,textures);variants[f'age={age},half={half}']={'model':f'slavicmyths:block/{name}_{age}_{half}'}
  write(A/f'blockstates/{name}.json',{'variants':variants});drop(name,'raspberry' if red else 'blueberry',[{'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'half':'lower'}}])
 wood={'wood':'minecraft:block/dark_oak_planks','edge':'minecraft:block/stripped_oak_log','flour':'minecraft:block/white_terracotta','cut':'minecraft:block/dark_oak_log','particle':'minecraft:block/dark_oak_planks'}
 els=[cube([0,11,0],[16,14,16],'wood'),cube([2,3,2],[14,5,14],'wood'),cube([2,8,2],[14,10,3],'edge'),cube([2,8,13],[14,10,14],'edge')]
 for x in [1,12]:
  for z in [1,12]:els.append(cube([x,0,z],[x+3,11,z+3],'edge'))
 for x,z in [(3,4),(5,5),(4,6),(9,10)]:els.append(cube([x,14,z],[x+1.5,14.03,z+1],'flour'))
 for x,z in [(8,3),(10,6),(11,8)]:els.append(cube([x,14,z],[x+.15,14.04,z+2],'cut'))
 model('block/kitchen_table',els,wood);write(A/'blockstates/kitchen_table.json',{'variants':{'':{'model':'slavicmyths:block/kitchen_table'}}});write(A/'models/item/kitchen_table.json',{'parent':'slavicmyths:block/kitchen_table'});drop('kitchen_table','kitchen_table')
 display={'gui':{'rotation':[25,35,0],'translation':[0,0,0],'scale':[.85,.85,.85]},'firstperson_righthand':{'rotation':[0,-20,-10],'translation':[0,1,0],'scale':[.85,.85,.85]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,1,0],'scale':[.8,.8,.8]}}
 els=[cube([1,7,7],[15,9,9],'cut'),cube([4,6,6],[12,10,10],'edge'),cube([4,6.5,5.5],[12,9.5,10.5],'edge'),cube([5,10,7],[7,10.05,8.5],'flour')];model('item/rolling_pin',els,wood,display)
 metal={'iron':'minecraft:block/anvil','rim':'minecraft:block/smooth_stone','dark':'minecraft:block/blackstone','particle':'minecraft:block/anvil'}
 els=[cube([4,2,4],[12,4,12],'dark'),cube([2,4,3],[4,10,13],'iron'),cube([12,4,3],[14,10,13],'iron'),cube([4,4,2],[12,10,4],'iron'),cube([4,4,12],[12,10,14],'iron')]
 for a,b in [([3,10,3],[13,11,4]),([3,10,12],[13,11,13]),([3,10,4],[4,11,12]),([12,10,4],[13,11,12]),([0,7,6],[3,9,10]),([13,7,6],[16,9,10])]:els.append(cube(a,b,'rim'))
 model('item/metal_pot',els,metal,display)
 shaped('kitchen_table',['PPP','L L','LPL'],{'P':'minecraft:oak_planks','L':'minecraft:oak_log'});shaped('rolling_pin',['SPS'],{'S':'minecraft:stick','P':'minecraft:birch_planks'});shaped('metal_pot',['I I','I I','III'],{'I':'minecraft:iron_ingot'})
 for lang,ru in [('ru_ru',True),('en_us',False)]:
  p=A/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'))
  for key,v in {'block.slavicmyths.raspberry_bush':('Куст малины','Raspberry bush'),'block.slavicmyths.blueberry_bush':('Куст черники','Blueberry bush'),'block.slavicmyths.kitchen_table':('Кухонный стол','Kitchen table'),'item.slavicmyths.rolling_pin':('Деревянная скалка','Wooden rolling pin'),'item.slavicmyths.metal_pot':('Металлический горшок','Metal cooking pot'),'kitchen.slavicmyths.slots':('1   2   3: продукты     Инструмент → Блюдо','1 / 2 / 3: ingredients   Tool → Dish'),'kitchen.slavicmyths.yield':('Выход: %s; инструмент изнашивается','Yield: %s; tool takes wear'),'item.slavicmyths.invisibility_cap.effect':('Слот: голова. До 8 с невидимости после покоя, затем 30 с отдыха; бег и бой раскрывают.','Head slot. Up to 8s invisibility after resting, then 30s cooldown; sprinting/combat reveal you.')}.items():d[key]=v[0 if ru else 1]
  write(p,d)

def transport():
 textures={'wood':'minecraft:block/dark_oak_log','twig':'minecraft:block/stripped_oak_log','wrap':'minecraft:block/brown_terracotta','iron':'minecraft:block/anvil','light':'minecraft:block/stripped_birch_log','particle':'minecraft:block/dark_oak_log'}
 display={'gui':{'rotation':[25,45,0],'translation':[0,0,0],'scale':[.5,.5,.5]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.65,.65,.65]},'firstperson_righthand':{'rotation':[0,20,-10],'translation':[1,1,0],'scale':[.8,.8,.8]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.8,.8,.8]}}
 e=[cube([7.2,5.5,-7],[8.8,7.1,1],'wood'),cube([7.5,5.8,.5],[9.1,7.4,8],'wood'),cube([7.2,5.6,7.5],[8.8,7.2,16],'wood'),cube([6.8,5.1,1],[9.5,7.9,5],'wrap'),cube([8.5,7,8],[10.4,8.2,9.5],'wood')]
 for x,y,z,length,angle in [(7,5,14,11,0),(5,6,14,9,-22.5),(9,6,14,10,22.5),(5,3.5,15,9,0),(9,3,15,10,0),(3.5,5,16,7,-22.5),(11,5,16,8,22.5)]:
  e.append(cube([x,y,z],[x+1.2,y+1.2,z+length],'twig',([x,y,z],'y',angle) if angle else None))
 for x,z in [(5,20),(7,22),(9,20),(11,21)]:e.append(cube([x,4,z],[x+.55,4.6,25+(x%2)],'twig'))
 e.extend([cube([5.8,4.4,13],[10.4,8,14],'wrap'),cube([6,4.6,14.4],[10,7.8,15.1],'wrap')]);model('item/flying_broom',e,textures,display)
 e=[cube([3,0,3],[13,2,13],'wood'),cube([4,2,4],[12,3.5,12],'wood')]
 def ring(y1,y2,width,thick,tex):
  low=8-width/2;high=8+width/2;segment=width*.54
  faces=[([8-segment/2,y1,low],[8+segment/2,y2,low+thick]),([8-segment/2,y1,high-thick],[8+segment/2,y2,high]),([low,y1,8-segment/2],[low+thick,y2,8+segment/2]),([high-thick,y1,8-segment/2],[high,y2,8+segment/2])]
  for a,b in faces:
   e.append(cube(a,b,tex));e.append(cube(a,b,tex,([8,y1,8],'y',45)))
 ring(2,7,10.5,1.5,'wood');ring(6,14,13.3,1.3,'wood');ring(13,18,14.4,1.4,'wood');ring(18,19.2,14.8,1.8,'twig')
 for y,width in [(3,11.4),(10,14.2),(16.5,15.2)]:ring(y,y+1,width,.6,'iron')
 for x,y in [(5,12),(6,13),(7,12),(6,11),(9,12),(10,13),(11,12),(10,11)]:e.append(cube([x,y,1.29],[x+.55,y+.55,1.4],'wrap'))
 model('item/flying_mortar',e,textures,{**display,'gui':{'rotation':[28,35,0],'translation':[0,-1,0],'scale':[.7,.7,.7]}})
 e=[cube([7,1,7],[9,20,9],'light'),cube([6.5,10,6.5],[9.5,14,9.5],'wrap'),cube([5,-4,5],[11,1,11],'twig'),cube([4.5,-3,5.5],[11.5,0,10.5],'twig'),cube([5.5,-5,5.5],[10.5,-4,10.5],'light')]
 model('item/pestle',e,textures,{**display,'firstperson_righthand':{'rotation':[0,-10,-15],'translation':[1,2,0],'scale':[1,1,1]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[1,1,1]}})
 names={'item.slavicmyths.flying_broom':('Метла Бабы-Яги','Baba Yaga\'s broom'),'item.slavicmyths.flying_mortar':('Летающая ступа','Flying mortar'),'item.slavicmyths.pestle':('Пест','Pestle'),'entity.slavicmyths.flying_broom':('Метла Бабы-Яги','Flying broom'),'entity.slavicmyths.flying_mortar':('Летающая ступа','Flying mortar'),'enchantment.slavicmyths.tailwind':('Попутный ветер','Tailwind'),'flight.slavicmyths.cargo':('Груз Ступы','Mortar cargo'),'flight.slavicmyths.controls':('WASD — движение; Пробел / X — вверх / вниз; R — тормоз; B — груз; Shift — сойти.','WASD: move; Space / X: up / down; R: brake; B: cargo; Shift: dismount.'),'flight.slavicmyths.need_pestle':('Для управления Ступой возьмите Пест в руку.','Hold a pestle to steer the mortar.'),'key.slavicmyths.flight_down':('Снижение транспорта','Descend'),'key.slavicmyths.flight_brake':('Тормоз транспорта','Flight brake'),'key.slavicmyths.flight_cargo':('Груз Ступы','Open mortar cargo')}
 advancements=[('berry_garden','raspberry','Ягодная делянка','Berry patch','Посадите ягодный куст.','Plant a berry bush.'),('kitchen_meal','kitchen_table','У деревенского стола','Country kitchen','Приготовьте блюдо на кухонном столе.','Prepare a meal at the kitchen table.'),('first_flight','flying_broom','Над лесом','Above the forest','Взлетите на сказочном транспорте.','Take off on a folk flying vessel.')]
 for id,icon,ru,en,rd,ed in advancements:
  write(D/f'advancements/{id}.json',{'parent':'slavicmyths:root','display':{'icon':{'item':'slavicmyths:'+icon},'title':{'translate':f'advancements.slavicmyths.{id}.title'},'description':{'translate':f'advancements.slavicmyths.{id}.description'},'frame':'task','show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{'done':{'trigger':'minecraft:impossible'}}})
  names[f'advancements.slavicmyths.{id}.title']=(ru,en);names[f'advancements.slavicmyths.{id}.description']=(rd,ed)
 for lang,index in [('ru_ru',0),('en_us',1)]:
  p=A/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d.update({k:v[index] for k,v in names.items()});write(p,d)
def all_resources():generate();transport()
if __name__=='__main__':all_resources()
