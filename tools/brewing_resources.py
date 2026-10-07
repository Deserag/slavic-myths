"""Native 16px brewing assets and exact recipe data. No image-generation runtime."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
def js(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def cube(a,b,t):
 dx,dy,dz=[b[i]-a[i] for i in range(3)]
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,min(16,dx if f in ['north','south','up','down'] else dz),min(16,dz if f in ['up','down'] else dy)]}for f in ['north','south','east','west','up','down']}}
names={'hops':('Хмель','Hops'),'hops_cutting':('Черенок хмеля','Hop Cutting'),'hops_crop':('Хмель','Hops'),'malted_barley':('Солодовый ячмень','Malted Barley'),'malt_sack':('Мешок солода','Malt Sack'),'wooden_mug':('Деревянная кружка','Wooden Mug'),'dark_bottle':('Тёмная бутылка','Dark Bottle'),'ceramic_pitcher':('Керамический кувшин','Ceramic Pitcher'),'filled_pitcher':('Кувшин напитка','Beverage Pitcher'),'berry_mors_pitcher':('Кувшин ягодного морса','Berry Mors Pitcher'),'small_keg':('Малый бочонок','Small Keg'),'fruit_press':('Пресс','Fruit Press'),'fruit_pomace':('Жмых','Fruit Pomace'),'berry_mash':('Ягодная мезга','Berry Mash'),'apple_juice_bottle':('Яблочный сок','Apple Juice'),'honey_infusion':('Медовый настой','Honey Infusion'),'forest_herbal_infusion':('Лесной травяной настой','Forest Herbal Infusion'),'thunder_crystal_powder':('Порошок громового кристалла','Thunder Crystal Powder'),'fermentation_vat':('Бродильный чан','Fermentation Vat'),'spoiled_brew':('Испорченный напиток','Spoiled Brew'),'kvass_mug':('Квас','Kvass'),'mead_bottle':('Медовуха','Mead'),'apple_cider_bottle':('Яблочный сидр','Apple Cider'),'berry_mors_mug':('Ягодный морс','Berry Mors')}
beers={'light_beer':('Светлое пиво','Light Beer','#d7a52c','#f4ead0'),'dark_beer':('Тёмное пиво','Dark Beer','#37271d','#d3c2a3'),'hopped_beer':('Хмельное пиво','Hopped Beer','#c87518','#f2e4c6'),'honey_beer':('Медовое пиво','Honey Beer','#e0b53b','#fff0ce'),'forest_beer':('Лесное пиво','Forest Beer','#65713a','#e8dfb3'),'thunder_beer':('Громовое пиво','Thunder Beer','#d79223','#edf5ed'),'veles_dark_beer':('Велесово тёмное пиво','Veles Dark Beer','#30212e','#b7aca8'),'witch_berry_beer':('Ягодное колдовское пиво','Witch Berry Beer','#7d223f','#eaa3ba')}
for k,v in beers.items():names[k+'_mug']=v[:2]
batches={('veles_dark_wort' if k=='veles_dark_beer' else 'witch_berry_wort' if k=='witch_berry_beer' else k+'_wort'):k for k in beers};batches.update(kvass_wort='kvass',mead_must='mead',cider_must='apple_cider')
for k,v in batches.items():names[k]=('Сусло: '+(beers[v][0]if v in beers else {'kvass':'Квас','mead':'Медовуха','apple_cider':'Сидр'}[v]),'Batch: '+v.replace('_',' ').title())
def texture(name,base,style):
 im=Image.new('RGBA',(16,16),base);d=ImageDraw.Draw(im)
 if style=='liquid':
  for x,y in [(2,4),(7,10),(11,3),(13,12)]:d.line((x,y,x+1,y),fill='#e8c879')
 elif style=='clay':
  d.line((0,2,15,2),fill='#da9969');d.line((0,10,15,10),fill='#662e24')
  for x in [2,7,12]:d.polygon([(x,6),(x+1,4),(x+3,6),(x+1,8)],fill='#ede0a8')
 else:
  for x in [3,8,13]:d.line((x,0,x,15),fill='#453123');d.line((x+1,3,x+1,10),fill='#b58958')
 im.save(A/f'textures/block/{name}.png')
for n,c,style in [('brewing_clay','#ab5836','clay'),('brewing_grain','#b49340','liquid'),('brewing_infusion','#657342','liquid'),('brewing_wort','#a9742b','liquid'),('brewing_must','#d5a640','liquid')]:texture(n,c,style)
def sprite(name):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);key=name.replace('_mug','');drink=key in beers or name in ['kvass_mug','berry_mors_mug'];mug=name=='wooden_mug' or drink
 if mug:
  d.rectangle((3,4,11,13),fill='#513822');d.rectangle((4,5,10,12),fill='#9a683a');d.rectangle((12,5,14,11),fill='#6e472b');d.rectangle((12,7,13,9),fill=(0,0,0,0));d.line((3,5,11,5),fill='#797579');d.line((3,12,11,12),fill='#797579')
  for x in [5,8]:d.line((x,6,x,11),fill='#704627')
  c,foam=(beers[key][2:4]if key in beers else ('#59382c','#bbae7e')if name=='kvass_mug' else ('#8e294d',None));d.rectangle((4,3,10,4),fill=c if drink else '#2d241b')
  if drink:
   d.rectangle((4,6,10,11),fill=c)
   if foam:d.rectangle((3,2 if name=='hopped_beer_mug' else 3,11,4),fill=foam);d.point((5,2),fill=foam);d.line((8,4,8,6),fill=foam)
   if key=='forest_beer':d.point((5,3),fill='#48663c');d.point((9,4),fill='#48663c')
   if key=='thunder_beer':d.line((1,2,1,4),fill='#71b5ed');d.line((0,3,2,3),fill='#cadffe')
   if key=='veles_dark_beer':d.point((4,8),fill='#755780');d.point((9,10),fill='#566b4c')
   if key=='witch_berry_beer':d.point((4,3),fill='#8a2342');d.point((10,2),fill='#712640')
 elif name in ['ceramic_pitcher','filled_pitcher','berry_mors_pitcher']:
  d.rectangle((11,4,14,10),fill='#7c3d2a');d.rectangle((12,6,13,8),fill=(0,0,0,0));d.polygon([(4,2),(10,2),(10,5),(12,8),(11,14),(3,14),(2,8),(4,5)],fill='#9b482e');d.rectangle((4,8,10,12),fill='#ca754c');d.rectangle((4,3,9,4),fill='#502d22'if name=='ceramic_pitcher'else'#982344'if name=='berry_mors_pitcher'else'#cfa735');d.line((3,11,11,11),fill='#e3c899');d.polygon([(6,9),(7,8),(8,9),(7,10)],fill='#ece0b3')
 elif name in ['dark_bottle','apple_juice_bottle','mead_bottle','apple_cider_bottle','forest_herbal_infusion','spoiled_brew'] or name in batches:
  d.rectangle((6,1,9,3),fill='#817044');d.rectangle((6,4,9,6),fill='#37452d');d.rectangle((4,7,11,14),fill='#273427');d.rectangle((5,8,10,13),fill='#495132');d.line((5,7,5,12),fill='#7e8456')
  color={'apple_juice_bottle':'#d4992e','mead_bottle':'#cf982f','apple_cider_bottle':'#caab44','forest_herbal_infusion':'#405830','spoiled_brew':'#4f433b'}.get(name)
  if color:d.rectangle((6,9,10,12),fill=color)
  if name=='forest_herbal_infusion':d.line((7,9,9,12),fill='#a6ad5e');d.point((6,11),fill='#a6ad5e')
 elif name=='honey_infusion':d.rectangle((4,5,11,13),fill='#95632b');d.rectangle((5,6,10,12),fill='#e8ad32');d.rectangle((3,3,12,5),fill='#d9cfad');d.line((3,5,12,5),fill='#77502b')
 elif name=='malt_sack':
  d.polygon([(4,3),(11,3),(13,6),(12,14),(3,14),(2,6)],fill='#cfc19a');d.rectangle((4,4,11,6),fill='#e0bb65');d.line((7,8,7,12),fill='#8e4030');d.line((5,8,9,12),fill='#8e4030');d.line((9,8,5,12),fill='#8e4030')
 elif name in ['hops','hops_cutting']:
  d.line((8,2,6,13),fill='#695331');d.polygon([(7,4),(2,3),(3,7),(7,7)],fill='#48713b');d.polygon([(7,6),(13,4),(12,8),(7,8)],fill='#698842')
  if name=='hops':
   for x,y in [(5,7),(9,9)]:d.polygon([(x,y),(x+3,y),(x+4,y+3),(x+2,y+6),(x,y+4)],fill='#89a743');d.line((x+1,y+1,x+2,y+4),fill='#c4ce6c')
 elif name=='malted_barley':
  for x,y in [(3,8),(7,5),(10,9)]:d.polygon([(x,y),(x+2,y-1),(x+3,y+2),(x+1,y+3)],fill='#ceac59');d.line((x,y+2,x-1,y+4),fill='#8e6e32')
 elif name=='thunder_crystal_powder':
  for x,y in [(3,10),(7,11),(10,9),(6,7)]:d.rectangle((x,y,x+2,y+2),fill='#659cda');d.point((x,y),fill='#d5e9f5')
  d.polygon([(8,2),(11,7),(7,7)],fill='#35465b')
 else:
  d.rectangle((3,11,12,13),fill='#805530');d.rectangle((4,8,11,11),fill='#b09a69'if name=='fruit_pomace'else'#733345');d.line((5,8,10,9),fill='#b17b4f'if name=='fruit_pomace'else'#a85665')
 im.save(A/f'textures/item/{name}.png');js(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
for name in names:
 if name not in ['hops_crop','fruit_press','fermentation_vat','small_keg']:sprite(name)
# Component-selected bottle and pitcher silhouettes preserve the actual container used.
for drink in list(beers)+['kvass','berry_mors']:
 name=drink+'_mug';alternate=drink+'_bottled';im=Image.open(A/'textures/item/dark_bottle.png').copy();d=ImageDraw.Draw(im);d.rectangle((6,9,10,12),fill=beers[drink][2]if drink in beers else '#583e27'if drink=='kvass'else'#8e294d');im.save(A/f'textures/item/{alternate}.png');js(A/f'models/item/{alternate}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+alternate}});p=A/f'models/item/{name}.json';v=json.loads(p.read_text());v['overrides']=[{'predicate':{'custom_model_data':1},'model':'slavicmyths:item/'+alternate}];js(p,v)
variants=[]
for index,color in enumerate(['#583e27','#d4a836','#dabf54','#625845'],1):
 name='filled_pitcher_'+str(index);im=Image.open(A/'textures/item/filled_pitcher.png').copy();ImageDraw.Draw(im).rectangle((4,3,9,4),fill=color);im.save(A/f'textures/item/{name}.png');js(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}});variants.append({'predicate':{'custom_model_data':index},'model':'slavicmyths:item/'+name})
p=A/'models/item/filled_pitcher.json';v=json.loads(p.read_text());v['overrides']=variants;js(p,v)
tex={'wood':'slavicmyths:block/household_wood','metal':'slavicmyths:block/household_metal','particle':'slavicmyths:block/household_wood'}
def equipment(name,els,visual,extra={}):
 model=f'{name}_{visual}';js(A/f'models/block/{model}.json',{'textures':dict(tex,**extra),'elements':els});return {f'facing={f},visual={visual}':{'model':'slavicmyths:block/'+model,'y':rot}for f,rot in [('north',0),('east',90),('south',180),('west',270)]}
for name in ['fruit_press','fermentation_vat','small_keg']:
 states={}
 for v in range(5):
  if name=='fruit_press':
   e=[cube([1,0,1],[15,2,15],'wood'),cube([1,2,3],[3,23,13],'wood'),cube([13,2,3],[15,23,13],'wood'),cube([1,21,3],[15,24,13],'wood'),cube([7,11-v,7],[9,23,9],'metal'),cube([4,10-v,4],[12,12-v,12],'metal'),cube([3,2,3],[13,3,13],'wood'),cube([3,3,3],[4,9,13],'wood'),cube([12,3,3],[13,9,13],'wood'),cube([4,3,3],[12,9,4],'wood'),cube([4,3,12],[12,9,13],'wood'),cube([5,2,0],[11,3,3],'wood')];extra={}
  elif name=='fermentation_vat':
   e=[cube([1,0,1],[15,2,15],'wood'),cube([1,2,1],[3,13,15],'wood'),cube([13,2,1],[15,13,15],'wood'),cube([3,2,1],[13,13,3],'wood'),cube([3,2,13],[13,13,15],'wood'),cube([7,10,11],[9,17,13],'wood')];extra={}
   for y in [3,10]:e += [cube([.8,y,.8],[15.2,y+1,1.1],'metal'),cube([.8,y,14.9],[15.2,y+1,15.2],'metal'),cube([.8,y,1.1],[1.1,y+1,14.9],'metal'),cube([14.9,y,1.1],[15.2,y+1,14.9],'metal')]
   if v:e.append(cube([3,10,3],[13,10.2,13],'liquid'));extra={'liquid':'slavicmyths:block/'+['','brewing_grain','brewing_infusion','brewing_wort','brewing_must'][v]}if v else{}
  else:
   e=[cube([1,0,2],[4,4,14],'wood'),cube([12,0,2],[15,4,14],'wood'),cube([2,4,2],[14,13,14],'wood'),cube([4,2,2],[12,15,14],'wood'),cube([7,7,0],[9,9,3],'wood'),cube([8,9,0],[9,11,1],'wood')];extra={}
   for z in [4,11]:e += [cube([1.8,4,z],[2.2,13,z+1],'metal'),cube([13.8,4,z],[14.2,13,z+1],'metal'),cube([4,14.8,z],[12,15.2,z+1],'metal'),cube([4,1.8,z],[12,2.2,z+1],'metal')]
  states.update(equipment(name,e,v,extra))
 js(A/f'blockstates/{name}.json',{'variants':states});js(A/f'models/item/{name}.json',{'parent':f'slavicmyths:block/{name}_0','display':{'gui':{'rotation':[25,225,0],'scale':[.55,.55,.55]},'fixed':{'scale':[.65,.65,.65]}}});js(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
# Winding stems / leaves / cones grow across two synchronized halves.
states={}
for age in range(6):
 for half in ['lower','upper']:
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);h=[4,7,11,16,16,16][age]if half=='lower'else[0,0,0,7,12,16][age]
  if h:
   d.line([(7,15),(8,12),(7,8),(9,4),(8,16-h)],fill='#5f6132')
   for y in range(16-h+2,15,4):d.polygon([(8,y),(3,y-2),(2,y),(4,y+2),(8,y+1)],fill='#56813e');d.polygon([(8,y+1),(13,y-1),(14,y+1),(11,y+3)],fill='#799448')
   if age>=3:
    for x,y in [(4,9),(10,5),(9,12)]:
     if y<16-h:continue
     d.rectangle((x,y,x+(2 if age>=4 else 1),y+(3 if age==5 else 1)),fill='#a5b65b');d.point((x,y),fill='#d0d775')
  n=f'hops_{age}_{half}';im.save(A/f'textures/block/{n}.png');js(A/f'models/block/{n}.json',{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':'slavicmyths:block/'+n}});states[f'age={age},half={half}']={'model':'slavicmyths:block/'+n}
js(A/'blockstates/hops_crop.json',{'variants':states})
lower={'condition':'minecraft:block_state_property','block':'slavicmyths:hops_crop','properties':{'half':'lower'}};mature={'condition':'minecraft:block_state_property','block':'slavicmyths:hops_crop','properties':{'age':'5'}}
js(D/'loot_table/blocks/hops_crop.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[lower,mature,{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':'slavicmyths:hops','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':2,'max':4}}]}]},{'rolls':1,'conditions':[lower,{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':'slavicmyths:hops_cutting','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2},'conditions':[mature]}]}]}]})
def append(p,values):
 v=json.loads(p.read_text())if p.exists()else{'replace':False,'values':[]};v['values']=sorted(set(v['values']+values));js(p,v)
append(D/'tags/block/sickle_harvestable.json',['slavicmyths:hops_crop'])
append(R/'data/minecraft/tags/block/mineable/axe.json',['slavicmyths:'+k for k in ['fruit_press','small_keg','fermentation_vat']])
for tag,vals in {'brewing/hops':['hops'],'brewing/malt':['malted_barley'],'drinks/alcoholic':['light_beer_mug','dark_beer_mug','hopped_beer_mug','honey_beer_mug','forest_beer_mug','mead_bottle','apple_cider_bottle'],'drinks/magical':['thunder_beer_mug','veles_dark_beer_mug','witch_berry_beer_mug'],'drinks/non_alcoholic':['kvass_mug','berry_mors_mug','apple_juice_bottle']}.items():js(D/f'tags/item/{tag}.json',{'replace':False,'values':['slavicmyths:'+v for v in vals]})
def ingredient(k):return {'tag':k[1:]}if k.startswith('#')else{'item':k if ':'in k else 'slavicmyths:'+k}
def shaped(k,pattern,key,count=1):js(D/f'recipe/{k}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{a:ingredient(b)for a,b in key.items()},'result':{'id':'slavicmyths:'+k,'count':count}})
def shapeless(k,ins,out,count):js(D/f'recipe/{k}.json',{'type':'minecraft:crafting_shapeless','ingredients':[ingredient(i)for i in ins],'result':{'id':'slavicmyths:'+out,'count':count}})
shaped('malt_sack',['MMM']*3,{'M':'malted_barley'});shapeless('malt_sack_unpack',['malt_sack'],'malted_barley',9)
shaped('wooden_mug',['P P','P P',' PP'],{'P':'#minecraft:planks'},2);shaped('dark_bottle',[' G ','GSG',' G '],{'G':'minecraft:glass','S':'minecraft:stick'},2);shaped('ceramic_pitcher',[' B ','B B','BBB'],{'B':'minecraft:brick'})
shaped('fruit_press',['SIS','P P','PPP'],{'P':'#minecraft:planks','I':'minecraft:iron_ingot','S':'minecraft:stick'});shaped('fermentation_vat',['PIP','P P','PIP'],{'P':'#minecraft:planks','I':'minecraft:iron_ingot'});shaped('small_keg',['PNP','P P','PSP'],{'N':'minecraft:iron_nugget','P':'#minecraft:planks','S':'minecraft:stick'})
shapeless('thunder_crystal_powder',['thunder_stone'],'thunder_crystal_powder',4)
def vat(k,mode,inputs,time,result=None,count=1,returns=None,catalyst=None,retain=False,components=None):
 r={'type':'slavicmyths:vat','mode':mode,'ingredients':[{'ingredient':ingredient(i),'count':n}for i,n in inputs],'time':time,'result':{'id':'slavicmyths:'+(result or k),'count':count},'remainders':[{'id':i if ':'in i else 'slavicmyths:'+i,'count':n}for i,n in (returns or[])]}
 if catalyst:r.update(catalyst=ingredient(catalyst),retain_catalyst=retain)
 if components:r['result']['components']=components
 js(D/f'recipe/vat/{k}.json',r)
water=('minecraft:water_bucket',1);bucket=[('minecraft:bucket',1)];malt=lambda n:('malted_barley',n);hops=lambda n:('hops',n)
vat('malted_barley','MALTING',[('barley_grain',4),water],1200,count=4,returns=bucket)
vat('honey_infusion','INFUSION',[('minecraft:honey_bottle',2),water],800,count=2,returns=bucket+[('minecraft:glass_bottle',2)])
vat('forest_herbal_infusion','INFUSION',[('minecraft:brown_mushroom',1),('minecraft:red_mushroom',1),('#slavicmyths:berries',1),water],1000,count=2,returns=bucket)
recipes=[('light_beer_wort',[malt(4),hops(1),water],1200,None,False),('dark_beer_wort',[malt(5),hops(1),('honey_infusion',1),water],1400,None,False),('hopped_beer_wort',[malt(4),hops(3),water],1200,None,False),('honey_beer_wort',[malt(3),hops(1),('honey_infusion',2),water],1400,None,False),('forest_beer_wort',[malt(3),hops(1),('forest_herbal_infusion',2),water],1600,None,False),('thunder_beer_wort',[malt(4),hops(2),('honey_infusion',1),water],1800,'thunder_crystal_powder',False),('veles_dark_wort',[malt(5),hops(1),('forest_herbal_infusion',1),water],2000,'veles_staff',True),('witch_berry_wort',[malt(3),hops(1),('berry_mash',2),water],1800,'rune_forest',False),('kvass_wort',[('rye_bread',1),('rye_flour',1),('honey_infusion',1),water],1000,None,False),('mead_must',[('honey_infusion',3),water],1600,None,False)]
for k,ins,t,c,retain in recipes:vat(k,'MUST'if k=='mead_must'else'WORT',ins,t,returns=bucket,catalyst=c,retain=retain)
vat('cider_must','MUST',[('apple_juice_bottle',4)],600,returns=[('dark_bottle',4)])
vat('berry_mors','INFUSION',[('berry_mash',2),water,('minecraft:honey_bottle',1)],600,result='berry_mors_pitcher',returns=bucket+[('minecraft:glass_bottle',1)],components={'minecraft:custom_data':{'Beverage':'berry_mors','Quality':'ready','Container':'ceramic_pitcher','Servings':4}})
for k,ru,en,desc in [('first_brew','Первый напиток','First Brew',('Свари свой первый напиток','Brew your first fermented drink')),('too_much','Кажется, хватит',"Maybe That's Enough",('Доведи опьянение до опасного уровня','Reach a dangerous level of intoxication'))]:
 names['adv_'+k]=(ru,en);names['adv_'+k+'_desc']=desc;js(D/f'advancement/{k}.json',{'display':{'icon':{'id':'slavicmyths:'+('light_beer_mug'if k=='first_brew'else'dark_beer_mug')},'title':{'translate':'advancement.slavicmyths.'+k},'description':{'translate':'advancement.slavicmyths.'+k+'.desc'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'brewed':{'trigger':'minecraft:impossible'}}})
texts={'inputs':('Сырьё','Inputs'),'returns':('Тара','Returns'),'outputs':('Результат','Result'),'catalyst':('Катализатор','Catalyst'),'mode_1':('Солод','Malting'),'mode_2':('Настой','Infusion'),'mode_3':('Сусло','Wort'),'mode_4':('Основа','Must'),'young':('Молодой','Young'),'ready':('Готов','Ready'),'aged':('Выдержанный','Aged'),'spoiled':('Испорчен','Spoiled'),'quality':('Качество: %s','Quality: %s'),'load':('Опьянение: +%s','Intoxication: +%s'),'magic_load':('Магическая нагрузка: +%s','Magical Load: +%s'),'servings':('Порций: %s','Servings: %s'),'status':('%s · Порций: %s','%s · Servings: %s'),'no_mix':('Нельзя смешивать разные напитки или качество','Cannot mix different drinks or qualities'),'vat_slots':('Сырьё 1–4 · Тара 5–6 · Результат 7–8 · Катализатор 9','Inputs 1–4 · Returns 5–6 · Results 7–8 · Catalyst 9')}
for index,lang in enumerate(['ru_ru','en_us']):
 p=A/f'lang/{lang}.json';v=json.loads(p.read_text(encoding='utf-8'))
 for k,n in names.items():
  if k.startswith('adv_'):key='advancement.slavicmyths.'+k[4:].replace('_desc','.desc')
  else:key=('block'if k in ['hops_crop','fruit_press','small_keg','fermentation_vat']else'item')+'.slavicmyths.'+k
  v[key]=n[index]
 for k,t in texts.items():v['brewing.slavicmyths.'+k]=t[index]
 js(p,v)
print('Brewing resources generated: native sprites, models, 15 vat recipes, tags, bilingual text.')

# Preserve the latest integration assets and definitive recipes.
from integration_polish_resources import main as polish_118
polish_118()
