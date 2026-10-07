"""Authoritative Kitchen II native pixels, geometry and recipes; ordinary SINGLE assets untouched."""
from pathlib import Path
import json, copy
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(im,path):path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
def item(s):return {'item': 'minecraft:'+s[2:] if s.startswith('v:') else 'slavicmyths:'+s}
def ing(s):return {'tag':s[1:]} if s.startswith('#') else item(s)
def result(s,n=1):return {'id':'slavicmyths:'+s,'count':n}
foods=[('rye_bread',6,.70,64,'Р Р¶Р°РЅРѕР№ С…Р»РµР±','Rye Bread'),('karavai',8,.85,16,'РљР°СЂР°РІР°Р№','Karavai'),('baked_turnip',4,.50,64,'РџРµС‡С‘РЅР°СЏ СЂРµРїР°','Baked Turnip'),('stewed_turnip',6,.65,1,'РўСѓС€С‘РЅР°СЏ СЂРµРїР°','Stewed Turnip'),('stewed_cabbage',6,.70,1,'РўСѓС€С‘РЅР°СЏ РєР°РїСѓСЃС‚Р°','Stewed Cabbage'),('oat_porridge',6,.65,1,'РћРІСЃСЏРЅР°СЏ РєР°С€Р°','Oat Porridge'),('barley_porridge',6,.65,1,'РЇС‡РјРµРЅРЅР°СЏ РєР°С€Р°','Barley Porridge'),('berry_porridge',7,.75,1,'РљР°С€Р° СЃ СЏРіРѕРґР°РјРё','Berry Porridge'),('vegetable_stew',7,.70,1,'РћРІРѕС‰РЅР°СЏ РїРѕС…Р»С‘Р±РєР°','Vegetable Pottage'),('meat_stew',9,.85,1,'РњСЏСЃРЅР°СЏ РїРѕС…Р»С‘Р±РєР°','Meat Pottage'),('pea_soup',7,.75,1,'Р“РѕСЂРѕС…РѕРІС‹Р№ СЃСѓРї','Pea Soup'),('mushroom_stew_slavic',6,.70,1,'Р“СЂРёР±РЅР°СЏ РїРѕС…Р»С‘Р±РєР°','Mushroom Pottage'),('ukha',8,.80,1,'РЈС…Р°','Ukha'),('bliny',6,.65,16,'Р‘Р»РёРЅС‹','Bliny'),('berry_bliny',8,.80,16,'Р‘Р»РёРЅС‹ СЃ СЏРіРѕРґР°РјРё','Berry Bliny'),('meat_bliny',9,.85,16,'Р‘Р»РёРЅС‹ СЃ РјСЏСЃРѕРј','Meat Bliny'),('apple_bliny',8,.78,16,'Р‘Р»РёРЅС‹ СЃ СЏР±Р»РѕРєРѕРј','Apple Bliny'),('honey_bliny',8,.82,16,'Р‘Р»РёРЅС‹ СЃ РјС‘РґРѕРј','Honey Bliny'),('apple_pie',8,.80,16,'РЇР±Р»РѕС‡РЅС‹Р№ РїРёСЂРѕРі','Apple Pie'),('berry_pie',8,.82,16,'РЇРіРѕРґРЅС‹Р№ РїРёСЂРѕРі','Berry Pie'),('meat_pie',10,.90,16,'РњСЏСЃРЅРѕР№ РїРёСЂРѕРі','Meat Pie'),('cabbage_pie',8,.80,16,'РљР°РїСѓСЃС‚РЅС‹Р№ РїРёСЂРѕРі','Cabbage Pie')]
materials=[('wheat_flour','РџС€РµРЅРёС‡РЅР°СЏ РјСѓРєР°','Wheat Flour'),('rye_flour','Р Р¶Р°РЅР°СЏ РјСѓРєР°','Rye Flour'),('oat_groats','РћРІСЃСЏРЅР°СЏ РєСЂСѓРїР°','Oat Groats'),('barley_groats','РЇС‡РјРµРЅРЅР°СЏ РєСЂСѓРїР°','Barley Groats'),('dough','РўРµСЃС‚Рѕ','Dough'),('rolling_pin','РЎРєР°Р»РєР°','Rolling Pin'),('metal_pot','РњРµС‚Р°Р»Р»РёС‡РµСЃРєРёР№ РіРѕСЂС€РѕРє','Metal Cooking Pot')]
bowlids=[f[0] for f in foods if f[3]==1]
colors={'stewed_turnip':'#d5aa54','stewed_cabbage':'#adb672','oat_porridge':'#dfd0aa','barley_porridge':'#bead88','berry_porridge':'#e0c9a2','vegetable_stew':'#bfa66b','meat_stew':'#966749','pea_soup':'#a2a55a','mushroom_stew_slavic':'#b7a180','ukha':'#d1c599'}
for name in [f[0] for f in foods]+[m[0] for m in materials]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);dark='#493325';crust='#a86931';gold='#ddad64';light='#f0d9a0'
 if name in bowlids:
  d.polygon([(1,6),(14,6),(12,13),(4,13)],fill=dark);d.line([(3,10),(12,10)],fill='#805737',width=2);d.ellipse((1,3,14,9),fill='#805737');d.ellipse((3,4,12,8),fill=colors[name]);accent='#668447' if name in ['stewed_cabbage','pea_soup','vegetable_stew'] else '#8c6243' if name in ['barley_porridge','meat_stew','mushroom_stew_slavic'] else '#edd498'
  for x,y in [(4,5),(8,6),(10,5)]:d.rectangle((x,y,x+1,y+1),fill=accent)
  if name=='berry_porridge':d.rectangle((4,5,5,6),fill='#a03d49');d.rectangle((9,5,10,6),fill='#434878')
  if name=='ukha':d.line((4,6,9,5),fill='#e7e0ca',width=2);d.point((10,7),fill='#547441')
 elif name.endswith('_flour'):
  if name=='wheat_flour':d.polygon([(5,3),(10,3),(12,7),(12,14),(3,14),(3,7)],fill='#ad8a53');d.rectangle((4,7,11,12),fill='#c7a976');d.ellipse((4,1,11,5),fill='#f1e4c1');d.line((7,8,7,12),fill='#7e5c31');d.line((5,9,9,11),fill='#e3c585')
  else:d.polygon([(3,6),(5,2),(10,2),(13,7),(12,13),(2,13)],fill='#897256');d.ellipse((5,1,11,5),fill='#c8b798');d.line((7,7,5,12),fill='#51452e');d.line((7,7,10,9),fill='#ad9468');d.rectangle((12,12,14,14),fill='#b6a386')
 elif name.endswith('_groats'):
  d.polygon([(2,7),(13,7),(11,13),(4,13)],fill=dark);d.ellipse((2,4,13,9),fill='#93663b');d.ellipse((3,4,12,8),fill='#d4b17a');
  for x,y in [(4,5),(8,5),(6,7),(10,7)]:d.rectangle((x,y,x+(1 if name=='oat_groats' else 0),y+1),fill='#eddbab' if name=='oat_groats' else '#b3915e')
  d.line((13,3,14,8),fill='#b4985d');d.line((11,3,14,6),fill='#d0b370')
 elif name=='dough':d.ellipse((2,4,13,12),fill='#c6b282');d.ellipse((3,3,12,10),fill='#eed8ab');d.rectangle((5,5,8,6),fill='#f6e6c7')
 elif name=='rolling_pin':d.line((1,13,14,2),fill='#744a2b',width=2);d.polygon([(3,9),(9,3),(13,6),(6,13)],fill='#b68449');d.line((4,9,10,3),fill='#e0b477',width=2)
 elif name=='metal_pot':d.rectangle((1,6,3,9),fill='#56565a');d.rectangle((12,6,14,9),fill='#56565a');d.polygon([(3,5),(12,5),(11,13),(4,13)],fill='#45434a');d.rectangle((4,7,5,11),fill='#6e6e73');d.ellipse((3,3,12,7),fill='#a3a3a5');d.ellipse((4,4,11,6),fill='#25252a')
 elif name=='rye_bread':d.ellipse((1,5,14,12),fill='#59432d');d.polygon([(2,7),(9,3),(14,6),(13,10),(4,12)],fill='#785736');d.polygon([(2,7),(5,8),(5,12),(2,11)],fill='#b18b55');
 elif name=='baked_turnip':d.ellipse((3,5,12,13),fill='#916232');d.ellipse((4,4,11,11),fill='#deb967');d.line((7,2,8,5),fill='#587642',width=2);d.line((8,2,11,2),fill='#6d8947')
 elif 'bliny' in name:
  if name in ['meat_bliny','apple_bliny']:d.polygon([(2,7),(9,4),(14,7),(7,11)],fill=crust);d.line((3,8,10,5),fill=gold,width=3);d.ellipse((2,7,6,11),fill=light);d.ellipse((3,8,5,10),fill='#71492d' if name=='meat_bliny' else '#deb551');d.line((7,12,14,8),fill=gold,width=2)
  else:
   for y in [7,5,3]:d.ellipse((1,y,14,y+6),fill=crust);d.ellipse((2,y,13,y+3),fill=gold)
   for x,y in [(4,4),(9,5),(6,6)]:d.rectangle((x,y,x+1,y),fill='#b78543')
   if name=='berry_bliny':d.rectangle((4,4,5,5),fill='#a3324a');d.rectangle((9,4,10,5),fill='#414779')
   if name=='honey_bliny':d.line((3,4,10,6),fill='#f2c24a',width=2)
 else:
  d.ellipse((1,4,14,13),fill=crust);d.ellipse((2,3,13,10),fill=gold)
  if name in ['apple_pie','berry_pie']:
   d.ellipse((4,4,11,9),fill='#aa6435' if name=='apple_pie' else '#773547')
   for x in [5,8,11]:d.line((x,4,x-2,9),fill=light);d.line((3,x-1,12,x-1),fill=light)
   if name=='berry_pie':d.point((6,6),fill='#41477b');d.point((10,7),fill='#b7424b')
  elif name=='meat_pie':
   for x in [5,8,11]:d.line((x,5,x-1,8),fill=dark)
   d.rectangle((10,9,12,11),fill='#674331')
  elif name=='cabbage_pie':d.line((5,5,10,8),fill='#8c672e');d.line((8,4,7,8),fill='#8c672e');d.rectangle((10,9,12,11),fill='#9cac62')
  else:d.line((4,6,11,6),fill=light);d.line((7,4,7,9),fill=light);d.point((4,9),fill=light);d.point((11,9),fill=light)
 if name=='rye_bread':
  for x in [6,9,12]:d.line((x,5,x-1,8),fill='#d6bc86')
 save(im,A/f'textures/item/{name}.png');write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
# wood/metal/dough native 16px surfaces: restrained large features
for name,base in [('kitchen_wood','#ac8456'),('kitchen_metal','#48474c'),('kitchen_dough','#e6d0a3')]:
 im=Image.new('RGBA',(16,16),base);d=ImageDraw.Draw(im)
 if name=='kitchen_wood':
  for y in [3,8,13]:d.line((0,y,15,y),fill='#765737');d.line((2,y+1,8,y+1),fill='#bf996c')
 elif name=='kitchen_metal':d.rectangle((0,0,15,1),fill='#99989b');d.rectangle((2,3,4,12),fill='#626168')
 else:d.rectangle((3,3,8,5),fill='#efddbb');d.rectangle((9,10,12,12),fill='#c7ae7d')
 save(im,A/f'textures/block/{name}.png')
def cube(a,b,texture,uv=None):
 dx,dy,dz=[b[i]-a[i] for i in range(3)]
 return {'from':a,'to':b,'faces':{f:{'texture':texture,'uv':uv or [0,0,dx if f in ['north','south','up','down'] else dz,dz if f in ['up','down'] else dy]} for f in ['north','south','east','west','up','down']}}
def model(elements,textures):return {'textures':textures,'elements':elements,'ambientocclusion':False}
# Ordinary table SINGLE JSON and every old texture preserved byte for byte.
for wood in ['pine','linden']:
 original=json.loads((A/f'models/block/{wood}_table.json').read_text());variants={}
 for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
  variants[f'facing={facing},connection=none']={'model':f'slavicmyths:block/{wood}_table','y':y}
  for direction in ['north','south','east','west']:
   key=f'{wood}_table_pair_{direction}';path=A/f'models/block/{key}.json'
   # table has symmetric tabletop/legs; paired world axes ignore decorative facing (none on original).
   paired=copy.deepcopy(original);kept=[]
   for e in paired['elements']:
    a=e['from'];b=e['to'];leg=b[1]<=12
    inner=leg and (direction=='north' and b[2]<=4 or direction=='south' and a[2]>=12 or direction=='west' and b[0]<=4 or direction=='east' and a[0]>=12)
    if not inner:kept.append(e)
   paired['elements']=kept;write(path,paired);variants[f'facing={facing},connection={direction}']={'model':f'slavicmyths:block/{key}'}
 write(A/f'blockstates/{wood}_table.json',{'variants':variants})
# Kitchen halves: NORTH left x=0, partner EAST. Four outer legs only.
tex={'wood':'slavicmyths:block/kitchen_wood','metal':'slavicmyths:block/kitchen_metal','particle':'slavicmyths:block/kitchen_wood','cloth':'slavicmyths:block/linen_cover'}
for half in ['left','right']:
 x=1 if half=='left' else 12
 els=[cube([0,12,0],[16,16,16],'#wood'),cube([x,0,1],[x+3,12,4],'#wood'),cube([x,0,12],[x+3,12,15],'#wood'),cube([1,3,2],[15,5,14],'#wood')]
 if half=='left':els += [cube([1,5,11],[9,10,14],'#wood'),cube([1,16,10],[4,16.4,15],'#cloth')]
 write(A/f'models/block/kitchen_table_{half}.json',model(els,tex))
write(A/'blockstates/kitchen_table.json',{'variants':{f'facing={f},part={p}':{'model':f'slavicmyths:block/kitchen_table_{p}','y':y} for f,y in [('north',0),('east',90),('south',180),('west',270)] for p in ['left','right']}})
left=json.loads((A/'models/block/kitchen_table_left.json').read_text());right=json.loads((A/'models/block/kitchen_table_right.json').read_text());els=copy.deepcopy(left['elements']);
for e in right['elements']:
 e=copy.deepcopy(e);e['from'][0]+=16;e['to'][0]+=16;els.append(e)
mi=model(els,tex);mi['display']={'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.45,.45,.45]},'ground':{'translation':[0,3,0],'scale':[.25,.25,.25]},'fixed':{'scale':[.4,.4,.4]}}
write(A/'models/item/kitchen_table.json',mi)
# No ordinary kitchen loot: onRemove master owns one block drop plus actual inventory.
write(D/'loot_table/blocks/kitchen_table.json',{'type':'minecraft:block','pools':[]})
# Dedicated food display cuboids with actual bowl walls, broad fills and intentional toppings.
for name in [f[0] for f in foods]:
 texture={'food':f'slavicmyths:item/{name}','wood':'minecraft:block/dark_oak_planks','crust':'slavicmyths:block/kitchen_dough','particle':f'slavicmyths:item/{name}'}
 if name in bowlids:
  els=[cube([3,0,3],[13,1,13],'#wood'),cube([2,1,2],[14,4,3],'#wood'),cube([2,1,13],[14,4,14],'#wood'),cube([2,1,3],[3,4,13],'#wood'),cube([13,1,3],[14,4,13],'#wood'),cube([3,2,3],[13,3.5,13],'#food',[3,4,12,8])]
 elif 'bliny' in name:
  els=[]
  for y in [0,.8,1.6]:
   for a,b in [([3,y,5],[13,y+.7,11]),([5,y,3],[11,y+.7,13]),([4,y,4],[12,y+.7,12])]:els.append(cube(a,b,'#food',[2,3,13,7]))
  if name!='bliny':
   for x,z in [(5,5),(9,7),(6,10)]:els.append(cube([x,2.3,z],[x+2,2.8,z+1.5],'#food',[3,4,11,6]))
 elif name=='rye_bread':els=[cube([2,0,4],[14,3,12],'#food'),cube([3,3,5],[13,5,11],'#food')]
 elif name=='baked_turnip':els=[cube([4,0,4],[12,4,12],'#food'),cube([5,4,5],[11,6,11],'#food'),cube([7,6,7],[9,7,9],'#food',[7,1,11,3])]
 else:
  els=[cube([3,0,3],[13,3,13],'#food'),cube([2,1,4],[14,3,12],'#food'),cube([4,1,2],[12,3,14],'#food'),cube([4,3,4],[12,4,12],'#food',[2,3,13,10])]
  if name in ['apple_pie','berry_pie']:
   for x in [5,8,11]:els.append(cube([x,4,3],[x+.7,4.4,13],'#crust'));els.append(cube([3,4.4,x],[13,4.8,x+.7],'#crust'))
  if name=='karavai':els += [cube([7,4,4],[8,4.5,12],'#crust'),cube([4,4,7],[12,4.5,8],'#crust')]
  if name in ['meat_pie','cabbage_pie']:els.append(cube([10,1,2],[13,2.5,3],'#food',[10,9,12,11]))
 write(A/f'models/table_food/{name}.json',model(els,texture))
for name in ['rolling_pin','metal_pot','dough','formed_dough']:
 if name=='rolling_pin':els=[cube([3,1,7],[13,3,9],'#wood'),cube([1,1.5,7.5],[3,2.5,8.5],'#wood'),cube([13,1.5,7.5],[15,2.5,8.5],'#wood')]
 elif name=='metal_pot':els=[cube([4,0,4],[12,1,12],'#metal'),cube([3,1,3],[13,6,4],'#metal'),cube([3,1,12],[13,6,13],'#metal'),cube([3,1,4],[4,6,12],'#metal'),cube([12,1,4],[13,6,12],'#metal'),cube([1,3,6],[3,4,10],'#metal'),cube([13,3,6],[15,4,10],'#metal')]
 else:els=[cube([3,0,4],[13,1.5 if name=='dough' else 3,12],'#dough')];
 write(A/f'models/table_food/{name}.json',model(els,{'wood':'slavicmyths:block/kitchen_wood','metal':'slavicmyths:block/kitchen_metal','dough':'slavicmyths:block/kitchen_dough','particle':'slavicmyths:block/kitchen_wood'}))
# recipes
for grain,out in [('v:wheat','wheat_flour'),('rye_grain','rye_flour'),('oat_grain','oat_groats'),('barley_grain','barley_groats')]:write(D/f'recipe/{out}.json',{'type':'minecraft:crafting_shapeless','ingredients':[ing(grain),ing(grain)],'result':result(out)})
write(D/'recipe/rolling_pin.json',{'type':'minecraft:crafting_shaped','pattern':['SPS'],'key':{'S':item('v:stick'),'P':{'tag':'minecraft:planks'}},'result':result('rolling_pin')})
write(D/'recipe/metal_pot.json',{'type':'minecraft:crafting_shaped','pattern':['I I','I I',' I '],'key':{'I':item('v:iron_ingot')},'result':result('metal_pot')})
for kind,time in [('smelting',200),('smoking',100)]:write(D/f'recipe/baked_turnip_{kind}.json',{'type':'minecraft:'+kind,'ingredient':item('turnip'),'result':{'id':'slavicmyths:baked_turnip'},'experience':.35,'cookingtime':time})
def recipe(name,ingredients,mode='ROLLING',time=80,amount=1,servings=0,rem=[]):
 write(D/f'recipe/kitchen/{name}.json',{'type':'slavicmyths:kitchen','mode':mode,'ingredients':[{'ingredient':ing(s),'count':n} for s,n in ingredients],'tool':item('metal_pot' if mode=='POT' else 'rolling_pin'),'time':time,'result':result(name,amount),'servings':servings,'remainders':[{'id':'minecraft:'+s,'count':1} for s in rem]})
recipe('dough',[('wheat_flour',2),('v:water_bucket',1)],amount=2,rem=['bucket'])
recipe('rye_bread',[('rye_flour',3),('v:water_bucket',1)],time=120,rem=['bucket'])
recipe('karavai',[('wheat_flour',3),('#slavicmyths:cooking/eggs',1),('#slavicmyths:cooking/milk',1),('v:honey_bottle',1)],time=120,rem=['bucket','glass_bottle'])
recipe('bliny',[('wheat_flour',2),('#slavicmyths:cooking/eggs',1),('#slavicmyths:cooking/milk',1)],amount=2,rem=['bucket'])
for name,s,n in [('berry_bliny','#slavicmyths:berries',2),('meat_bliny','#slavicmyths:cooking/cooked_meats',1),('apple_bliny','v:apple',1),('honey_bliny','v:honey_bottle',1)]:recipe(name,[('bliny',1),(s,n)],rem=['glass_bottle'] if name=='honey_bliny' else [])
for name,s,n,sugar in [('apple_pie','v:apple',2,True),('berry_pie','#slavicmyths:berries',3,True),('meat_pie','#slavicmyths:cooking/raw_meats',2,False),('cabbage_pie','cabbage',2,False)]:recipe(name,[('dough',2),(s,n)]+([('v:sugar',1)] if sugar else []))
for name,ingredients,n in [('stewed_turnip',[('turnip',2),('animal_fat',1)],2),('stewed_cabbage',[('cabbage',2),('animal_fat',1)],2),('vegetable_stew',[('turnip',1),('cabbage',1),('pea_pod',2)],3),('meat_stew',[('#slavicmyths:cooking/raw_meats',2),('turnip',1),('cabbage',1)],3),('pea_soup',[('pea_pod',3),('barley_groats',1)],3),('mushroom_stew_slavic',[('#slavicmyths:cooking/mushrooms',2),('barley_groats',1)],3),('ukha',[('#slavicmyths:cooking/fish',2),('turnip',1)],3)]:recipe(name,ingredients+[('v:water_bucket',1)],'POT',160,servings=n,rem=['bucket'])
# Ingredient arrays express a single water-or-milk counted demand, not two ingredients.
for name,base in [('oat_porridge',[('oat_groats',2)]),('barley_porridge',[('barley_groats',2)]),('berry_porridge',[('oat_groats',2),('#slavicmyths:berries',2)])]:
 recipe(name,base+[('v:water_bucket',1)],'POT',160,servings=3,rem=['bucket']);p=D/f'recipe/kitchen/{name}.json';data=json.loads(p.read_text());data['ingredients'][-1]['ingredient']=[item('v:water_bucket'),{'tag':'slavicmyths:cooking/milk'}];write(p,data)
def tag(name,values):write(D/f'tags/item/{name}.json',{'replace':False,'values':values})
raw=['beef','porkchop','mutton','chicken','rabbit'];tag('cooking/eggs',['minecraft:egg','slavicmyths:goose_egg','slavicmyths:duck_egg']);tag('cooking/milk',['minecraft:milk_bucket','slavicmyths:goat_milk_bucket'])
tag('cooking/raw_meats',['minecraft:'+s for s in raw]+['slavicmyths:raw_'+s for s in ['goose','duck','goat']]);tag('cooking/cooked_meats',['minecraft:cooked_'+s for s in raw]+['slavicmyths:cooked_'+s for s in ['goose','duck','goat']]);tag('cooking/mushrooms',['minecraft:red_mushroom','minecraft:brown_mushroom']);tag('cooking/fish',['minecraft:cod','minecraft:salmon','minecraft:tropical_fish','minecraft:pufferfish','minecraft:cooked_cod','minecraft:cooked_salmon']+['slavicmyths:'+s for s in ['raw_pike','cooked_pike','raw_carp','cooked_carp','smoked_carp']])
tag('placeable_table_foods',['slavicmyths:'+f[0] for f in foods]+['minecraft:'+s for s in ['apple','bread','cooked_beef','cooked_porkchop','cooked_mutton','cooked_chicken','cooked_rabbit']]+['slavicmyths:cooked_'+s for s in ['goose','duck','goat']]);complexids=['berry_bliny','meat_bliny','apple_bliny','honey_bliny','apple_pie','berry_pie','meat_pie','cabbage_pie','vegetable_stew','meat_stew','pea_soup','mushroom_stew_slavic','ukha'];tag('complex_dishes',['slavicmyths:'+s for s in complexids])
write(D/'advancement/generous_table.json',{'parent':'minecraft:husbandry/root','display':{'icon':{'id':'slavicmyths:meat_pie'},'title':{'translate':'advancement.slavicmyths.generous_table.title'},'description':{'translate':'advancement.slavicmyths.generous_table.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'dish':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'#slavicmyths:complex_dishes'}]}}},'requirements':[['dish']]})
for lang,index in [('ru_ru',1),('en_us',2)]:
 p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));names=[(f[0],f[4],f[5]) for f in foods]+materials+[('kitchen_table','РљСѓС…РѕРЅРЅС‹Р№ СЃС‚РѕР»','Kitchen Table')]
 for row in names:data['item.slavicmyths.'+row[0]]=row[index]
 data['block.slavicmyths.kitchen_table']='РљСѓС…РѕРЅРЅС‹Р№ СЃС‚РѕР»' if lang=='ru_ru' else 'Kitchen Table'
 for key,ru,en in [('kitchen.slavicmyths.servings','РџРѕСЂС†РёР№: %s/%s','Servings: %s/%s'),('kitchen.slavicmyths.serve_first','РЎРЅР°С‡Р°Р»Р° Р·Р°Р±РµСЂРёС‚Рµ РіРѕС‚РѕРІРѕРµ Р±Р»СЋРґРѕ','Serve the prepared dish first'),('kitchen.slavicmyths.serve','РџРѕРґР°С‚СЊ РїРѕСЂС†РёСЋ','Serve a portion'),('advancement.slavicmyths.generous_table.title','Р©РµРґСЂС‹Р№ СЃС‚РѕР»','Generous Table'),('advancement.slavicmyths.generous_table.description','РџСЂРёРіРѕС‚РѕРІСЊ СЃР»РѕР¶РЅРѕРµ Р±Р»СЋРґРѕ РЅР° РєСѓС…РѕРЅРЅРѕРј СЃС‚РѕР»Рµ','Prepare a hearty dish at the kitchen table')]:data[key]=ru if lang=='ru_ru' else en
 write(p,data)
# One simple 256px GUI atlas. No tiny fonts or recipe-selection buttons.
im=Image.new('RGBA',(256,256),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle((0,0,255,211),fill='#b9a27a',outline='#493a29',width=3);d.rectangle((3,3,252,11),fill='#654b35');d.rectangle((7,37,244,98),fill='#cbb995')
positions=[(20,24),(218,24)]+[(83+i%2*20,43+i//2*20) for i in range(4)]+[(181,44),(181,76),(201,76)]+[(47+c*18,130+y*18) for y in range(3) for c in range(9)]+[(47+c*18,188) for c in range(9)]
for x,y in positions:d.rectangle((x-1,y-1,x+16,y+16),fill='#695841',outline='#443723');d.line((x-1,y+16,x+16,y+16),fill='#e4d4b4');d.line((x+16,y-1,x+16,y+16),fill='#e4d4b4')
d.polygon([(132,48),(159,48),(159,44),(169,54),(159,64),(159,60),(132,60)],fill='#9a875f');save(im,A/'textures/gui/kitchen_ii.png')
if __name__=='__main__':print('Kitchen II resources generated: 22 foods, 7 materials/tools, 22 kitchen recipes; ordinary SINGLE preserved.')

# Kitchen-only recipes supersede the old crafting bypasses for the reused item IDs.
for name in ['karavai','berry_pie']:
 for folder in ['recipe','advancement/recipes']:
  old=D/f'{folder}/{name}.json'
  if old.exists():old.unlink()

# Preserve the current brewing assets/tags after this earlier resource pass.
import runpy as _brewing_runpy
_brewing_runpy.run_path(str(Path(__file__).resolve().with_name("brewing_resources.py")))
