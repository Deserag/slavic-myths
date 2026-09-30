"""0.6.5 wildlife and food resources. Original deterministic pixel art; no external assets."""
from pathlib import Path
import json,struct,zlib
R=Path(__file__).resolve().parents[1]; A=R/'src/main/resources/assets/slavicmyths'; D=R/'src/main/resources/data/slavicmyths'
def js(p,o):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def png(p,pix):
 def c(t,d):return struct.pack('!I',len(d))+t+d+struct.pack('!I',zlib.crc32(t+d)&0xffffffff)
 h=len(pix);w=len(pix[0]);raw=b''.join(b'\0'+bytes(v for q in row for v in q) for row in pix);p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'\x89PNG\r\n\x1a\n'+c(b'IHDR',struct.pack('!IIBBBBB',w,h,8,6,0,0,0))+c(b'IDAT',zlib.compress(raw))+c(b'IEND',b''))
def entity(name,palette,marks):
 px=[]
 for y in range(256):
  row=[]
  for x in range(256):
   band=(x//7+y//11+(x*y)%5)%4;c=palette[band]
   if any(x0<=x<x1 and y0<=y<y1 for x0,y0,x1,y1 in marks):c=palette[min(3,band+1)]
   row.append(c+(255,))
  px.append(row)
 png(A/f'textures/entity/{name}.png',px)
entity('brown_bear',[(48,31,24),(72,45,31),(101,66,43),(145,104,70)],[(48,20,82,46),(0,30,35,60)])
entity('bear_cub',[(58,38,29),(91,59,40),(132,91,59),(177,133,91)],[(30,10,75,38)])
entity('forest_wolf',[(24,27,29),(44,48,50),(72,76,75),(157,151,132)],[(0,35,32,65),(55,10,90,42)])
entity('boar',[(39,31,28),(67,51,43),(95,72,57),(198,180,144)],[(0,0,128,12),(62,18,88,42)])
entity('stag',[(64,43,31),(105,71,48),(151,107,68),(207,173,126)],[(0,22,35,55),(80,0,125,25)])
entity('doe',[(92,63,44),(137,96,64),(179,133,91),(224,192,148)],[(0,28,38,58)])

P={'_':(0,0,0,0),'k':(39,28,28,255),'d':(83,39,38,255),'r':(139,52,48,255),'R':(194,84,68,255),'p':(230,137,111,255),'f':(242,194,140,255),'b':(87,50,29,255),'B':(139,82,42,255),'t':(194,132,66,255),'T':(239,188,101,255),'w':(242,225,184,255),'g':(58,91,47,255),'G':(93,139,65,255),'u':(65,44,68,255),'U':(92,70,123,255),'x':(37,45,54,255),'s':(109,124,132,255),'S':(195,205,198,255),'o':(212,107,41,255),'y':(244,201,74,255)}
def item(name,rows):png(A/f'textures/item/{name}.png',[[P[c] for c in row] for row in rows]);js(A/f'models/item/{name}.json',{'parent':'item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
def canvas():return [['_']*16 for _ in range(16)]
def meat(name,kind,cooked=False):
 a=canvas(); colors=('b','B','t','T') if cooked else ('k','d','r','R'); shapes={'bear':[(2,6,12,12),(5,4,13,10)],'venison':[(3,5,12,9),(5,9,10,13)],'boar':[(2,5,13,11),(4,3,11,13)]}
 for x0,y0,x1,y1 in shapes[kind]:
  for y in range(y0,y1+1):
   for x in range(x0,x1+1):
    if (x-x0 in (0,x1-x0) and y-y0 in (0,y1-y0)):continue
    a[y][x]=colors[1 if x+y<15 else 0]
 for x,y in [(5,6),(8,5),(10,8),(6,10)]:a[y][x]=colors[3 if cooked else 2]
 if kind=='boar':
  for x in range(4,11):a[4][x]='f'
 item(name,[''.join(r) for r in a])
for kind in ('bear','venison','boar'):
 meat('raw_'+('bear_meat' if kind=='bear' else 'boar_meat' if kind=='boar' else kind),kind);meat('cooked_'+('bear_meat' if kind=='bear' else 'boar_meat' if kind=='boar' else kind),kind,True)
def bowl(name,liquid,garnish):
 a=canvas()
 for y in range(7,13):
  for x in range(2+y//3,14-y//3):a[y][x]='b' if y>=11 else ('B' if x>4 else 't')
 for x in range(4,12):a[7][x]=liquid
 for x,y in garnish:a[y][x]='G' if (x+y)%2 else 'T'
 item(name,[''.join(r) for r in a])
for n,c,g in [('mushroom_stew','B',[(5,7),(9,7)]),('beef_stew','d',[(6,7),(10,7)]),('pork_stew','p',[(5,7),(8,7),(10,7)]),('venison_stew','r',[(6,7),(9,7)]),('bear_stew','k',[(5,7),(7,7),(10,7)])]:bowl(n,c,g)
simple={
'large_animal_bone':["________________","_________ww_____","________wffw____","_______wffw_____","______wffw______","_____wffw_______","____wffw________","___wffw_________","__wffw__________","_wffw___________","wffw____________","_ww_____________","________________","________________","________________","________________"],
'bone_arrow':["______________ww","_____________wfw","____________wfw_","___________wfw__","__________bS____","_________bS_____","________bS______","_______bS_______","______bS________","_____bS_________","____bS__________","___bS___________","__bS____________","_sS_____________","s_s_____________","________________"],
'flour':["________________","_____bbbb_______","____bTTTTb______","____bwwwwb______","___bwwwwwwb_____","___bwwywwwb_____","__bwwyyywwwb____","__bwwwywwwwb____","__bwwwwwwwwb____","__bwwwwwwwwb____","__bwwwwwwwwb____","___bwwwwwwb_____","____bbbbbb______","________________","________________","________________"],
'raspberry':["________________","_______G________","______GGG_______","_____rRrR_______","____rRrRrR______","____RrRrR_______","_____RrR________","______r_________","________________","________________","________________","________________","________________","________________","________________","________________"],
'blueberry':["________________","______gGg_______","____uUu_uUu_____","___uUuUuUuU_____","____uUuUuU______","_____uUuU_______","________________","________________","________________","________________","________________","________________","________________","________________","________________","________________"],
'karavai':["________________","________________","_____TTTT_______","___TTtyytTT_____","__TttttttttT____","_BttTttTtttTB___","_BttttttttttB___","__BBBBBBBBBB____","________________","________________","________________","________________","________________","________________","________________","________________"],
'berry_pie':["________________","____BBBBBBBB____","___BttttttttB___","__BtuUruUruUtB__","__BttBttBttBtB__","___BBBBBBBBBB___","____bbbbbbbb____","________________","________________","________________","________________","________________","________________","________________","________________","________________"],
'baked_apple':["________________","_______g________","______Gb________","____oRRRo_______","___oRyyRRo______","___oRRRRRo______","____odddo_______","_____ooo________","________________","________________","________________","________________","________________","________________","________________","________________"]}
for n,r in simple.items():item(n,r)
def pancake(name,fill=None,rolled=False):
 a=canvas()
 if rolled:
  for oy in (5,8,11):
   for y in range(oy,oy+2):
    for x in range(3,12):a[y][x]='T' if y==oy else 'B'
   a[oy][11]=fill
 else:
  for y in range(6,12):
   for x in range(3+(y-6)//3,13-(y-6)//3):a[y][x]='T' if y<8 else 'B'
 item(name,[''.join(r) for r in a])
pancake('pancakes');pancake('raspberry_pancakes','r',True);pancake('blueberry_pancakes','u',True);pancake('meat_pancakes','d',True)

names={'brown_bear':'Бурый медведь','bear_cub':'Медвежонок','forest_wolf':'Лесной волк','boar':'Кабан','stag':'Олень','doe':'Олениха','raw_bear_meat':'Сырая медвежатина','cooked_bear_meat':'Жареная медвежатина','raw_venison':'Сырая оленина','cooked_venison':'Жареная оленина','raw_boar_meat':'Сырая кабанина','cooked_boar_meat':'Жареная кабанина','large_animal_bone':'Крупная кость','bone_arrow':'Костяная стрела','flour':'Мука','raspberry':'Малина','blueberry':'Черника','pancakes':'Блины','raspberry_pancakes':'Блины с малиной','blueberry_pancakes':'Блины с черникой','meat_pancakes':'Блины с мясом','karavai':'Каравай','berry_pie':'Ягодный пирог','baked_apple':'Печёное яблоко','mushroom_stew':'Грибная похлёбка','beef_stew':'Говяжья похлёбка','pork_stew':'Свиная похлёбка','venison_stew':'Оленья похлёбка','bear_stew':'Медвежья похлёбка'}
en={'brown_bear':'Brown Bear','bear_cub':'Bear Cub','forest_wolf':'Forest Wolf','boar':'Boar','stag':'Stag','doe':'Doe','raw_bear_meat':'Raw Bear Meat','cooked_bear_meat':'Cooked Bear Meat','raw_venison':'Raw Venison','cooked_venison':'Cooked Venison','raw_boar_meat':'Raw Boar Meat','cooked_boar_meat':'Cooked Boar Meat','large_animal_bone':'Large Animal Bone','bone_arrow':'Bone Arrow','flour':'Flour','raspberry':'Raspberry','blueberry':'Blueberry','pancakes':'Pancakes','raspberry_pancakes':'Raspberry Pancakes','blueberry_pancakes':'Blueberry Pancakes','meat_pancakes':'Meat Pancakes','karavai':'Karavai','berry_pie':'Berry Pie','baked_apple':'Baked Apple','mushroom_stew':'Mushroom Stew','beef_stew':'Beef Stew','pork_stew':'Pork Stew','venison_stew':'Venison Stew','bear_stew':'Bear Stew'}
for lang,mapn in [('ru_ru',names),('en_us',en)]:
 p=A/f'lang/{lang}.json';o=json.loads(p.read_text(encoding='utf-8'))
 for k,v in mapn.items():o[('entity' if k in names and k in {'brown_bear','bear_cub','forest_wolf','boar','stag','doe'} else 'item')+'.slavicmyths.'+k]=v
 for k in ('brown_bear','bear_cub','forest_wolf','boar','stag','doe'):o[f'item.slavicmyths.{k}_spawn_egg']=mapn[k]+(' (яйцо призыва)' if lang=='ru_ru' else ' Spawn Egg')
 for animal in ('bear','cub','wolf','boar','stag','doe'):
  for event in ('ambient','hurt','death'):o[f'subtitles.slavicmyths.{animal}_{event}']=(animal+' '+event if lang=='en_us' else {'ambient':'звуки лесного зверя','hurt':'лесной зверь ранен','death':'лесной зверь погибает'}[event])
 js(p,o)
for n in names:
 if n not in {'brown_bear','bear_cub','forest_wolf','boar','stag','doe'} and not (A/f'models/item/{n}.json').exists():js(A/f'models/item/{n}.json',{'parent':'item/generated','textures':{'layer0':f'slavicmyths:item/{n}'}})
for n in ('brown_bear','bear_cub','forest_wolf','boar','stag','doe'):js(A/f'models/item/{n}_spawn_egg.json',{'parent':'item/template_spawn_egg'})

def loot(name,item_id,minn,maxn,bone=False):
 entries=[{'type':'minecraft:item','name':f'slavicmyths:{item_id}','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':minn,'max':maxn}},{'function':'minecraft:looting_enchant','count':{'type':'minecraft:uniform','min':0,'max':1}}]}]
 if bone:entries.append({'type':'minecraft:item','name':'slavicmyths:large_animal_bone','conditions':[{'condition':'minecraft:random_chance_with_looting','chance':.35,'looting_multiplier':.12}]})
 js(D/f'loot_tables/entities/{name}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':entries}]})
loot('bear','raw_bear_meat',2,4,True);js(D/'loot_tables/entities/cub.json',{'type':'minecraft:entity','pools':[]});loot('wolf','large_animal_bone',0,1,False);loot('boar','raw_boar_meat',1,3,True);loot('stag','raw_venison',2,4,True);loot('doe','raw_venison',1,3,True)
def smelt(src,out,xp):js(D/f'recipes/{out}.json',{'type':'minecraft:smelting','ingredient':{'item':src if ':' in src else f'slavicmyths:{src}'},'result':f'slavicmyths:{out}','experience':xp,'cookingtime':200})
for a,b in [('raw_bear_meat','cooked_bear_meat'),('raw_venison','cooked_venison'),('raw_boar_meat','cooked_boar_meat')]:smelt(a,b,.35)
def shape(name,pattern,key):js(D/f'recipes/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':key,'result':{'item':f'slavicmyths:{name}'}})
def shapeless(name,ings,count=1):js(D/f'recipes/{name}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':x} for x in ings],'result':{'item':f'slavicmyths:{name}','count':count}})
shapeless('flour',['minecraft:wheat'],2);shape('bone_arrow',[' F ',' S ',' T '],{'F':{'item':'slavicmyths:large_animal_bone'},'S':{'item':'minecraft:stick'},'T':{'item':'minecraft:feather'}})
shapeless('pancakes',['slavicmyths:flour','minecraft:egg','minecraft:milk_bucket']);shapeless('raspberry_pancakes',['slavicmyths:pancakes','slavicmyths:raspberry']);shapeless('blueberry_pancakes',['slavicmyths:pancakes','slavicmyths:blueberry']);shapeless('meat_pancakes',['slavicmyths:pancakes','slavicmyths:cooked_boar_meat']);shape('karavai',['FFF','FMF','FFF'],{'F':{'item':'slavicmyths:flour'},'M':{'item':'minecraft:milk_bucket'}});shape('berry_pie',['BBB','FFF','SSS'],{'B':{'item':'slavicmyths:blueberry'},'F':{'item':'slavicmyths:flour'},'S':{'item':'minecraft:sugar'}});smelt('minecraft:apple','baked_apple',.2)
for n,meat_id in [('mushroom_stew','minecraft:brown_mushroom'),('beef_stew','minecraft:cooked_beef'),('pork_stew','minecraft:cooked_porkchop'),('venison_stew','slavicmyths:cooked_venison'),('bear_stew','slavicmyths:cooked_bear_meat')]:shapeless(n,['minecraft:bowl',meat_id,'minecraft:carrot','minecraft:potato'])
s=A/'sounds.json';sounds=json.loads(s.read_text(encoding='utf-8'));profiles={'bear':'polar_bear','cub':'polar_bear','wolf':'wolf','boar':'pig','stag':'horse','doe':'horse'}
for animal,vanilla in profiles.items():
 for event in ('ambient','hurt','death'):sounds[f'{animal}_{event}']={'subtitle':f'subtitles.slavicmyths.{animal}_{event}','sounds':[{'name':f'minecraft:entity.{vanilla}.{event}','type':'event','volume':.65}]}
js(s,sounds)
for lang in ('ru_ru','en_us'):
 p=A/f'lang/{lang}.json';o=json.loads(p.read_text(encoding='utf-8'))
 for event in sounds.values():o.setdefault(event['subtitle'], 'Звук существа' if lang=='ru_ru' else 'Creature sound')
 js(p,o)
for aid,icon,title,desc in [('first_hunt','raw_venison','Лесная добыча','Добудьте мясо лесного зверя.'),('village_feast','karavai','Деревенский пир','Испеките каравай.')]:
 js(D/f'advancements/{aid}.json',{'parent':'slavicmyths:root','display':{'icon':{'item':f'slavicmyths:{icon}'},'title':{'translate':f'advancements.slavicmyths.{aid}.title'},'description':{'translate':f'advancements.slavicmyths.{aid}.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'has_item':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':f'slavicmyths:{icon}'}]}}}})
 ru=A/'lang/ru_ru.json';ro=json.loads(ru.read_text(encoding='utf-8'));ro.update({'advancements.slavicmyths.first_hunt.title':'Лесная добыча','advancements.slavicmyths.first_hunt.description':'Добудьте мясо лесного зверя.','advancements.slavicmyths.village_feast.title':'Деревенский пир','advancements.slavicmyths.village_feast.description':'Испеките каравай.'});js(ru,ro)
 ep=A/'lang/en_us.json';eo=json.loads(ep.read_text(encoding='utf-8'));eo.update({'advancements.slavicmyths.first_hunt.title':'Forest Quarry','advancements.slavicmyths.first_hunt.description':'Obtain meat from a forest animal.','advancements.slavicmyths.village_feast.title':'Village Feast','advancements.slavicmyths.village_feast.description':'Bake a karavai.'});js(ep,eo)
print('Created 0.6.5 wildlife and cooking resources.')
