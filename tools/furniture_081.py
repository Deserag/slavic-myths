"""Reproducible furniture models and data for the 0.8.1 settlement palette."""
from pathlib import Path
import json
from bandits_080 import write,png,cube
from woodlands_0801 import merge_tag
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';D=RES/'data/slavicmyths'
KINDS=['table','chair','stool','bench','shelf','wardrobe','bedside_cabinet','weapon_rack']
EXTRA=['cloth_bag','wooden_crate','firewood_bundle','training_dummy','signal_bell']
def boxes(kind,upper=False):
 b=[]
 def box(a,c,t='wood'):b.append(cube(a,c,t))
 if kind=='table':
  box([0,12,0],[16,16,16]);
  for x in [1,12]:
   for z in [1,12]:box([x,0,z],[x+3,12,z+3],'beam')
 elif kind in ['chair','stool','bench']:
  wide=kind=='bench';box([0 if wide else 2,7,2],[16 if wide else 14,10,14]);
  for x in [1 if wide else 3,12 if wide else 10]:
   for z in [3,11]:box([x,0,z],[x+2,7,z+2],'beam')
  if kind=='chair':
   for x in [2,12]:box([x,9,12],[x+2,16,14],'beam')
   box([3,12,12],[13,15,14])
 elif kind=='shelf':
  box([0,6,10],[16,9,16]);
  for x in [2,12]:box([x,2,13],[x+2,6,16],'beam')
 elif kind in ['wardrobe','bedside_cabinet']:
  box([1,0,2],[15,2,15],'beam');box([1,14,2],[15,16,15]);box([1,2,13],[15,14,15]);
  for x in [1,13]:box([x,2,2],[x+2,14,13],'beam')
  for x in [3,8]:box([x,2,2],[x+5,14,4]);box([x+3,4 if upper else 11,1],[x+4,7 if upper else 13,3],'iron')
 elif kind=='weapon_rack':
  for x in [1,12]:box([x,0,4],[x+3,16,8],'beam');box([x-1,0,2],[x+4,2,13],'beam')
  box([0,9,3],[16,12,9]);
  for x in [5,10]:box([x,9,2],[x+1,14,4],'iron')
 elif kind=='cloth_bag':
  for a,c in [([2,0,2],[14,8,14]),([3,8,3],[13,11,13]),([5,11,5],[11,14,11])]:box(a,c,'cloth')
  box([4,11,4],[12,12,12],'rope');box([6,14,6],[10,16,10],'cloth')
 elif kind=='wooden_crate':
  box([1,0,1],[15,15,15]);
  for y in [1,12]:box([0,y,0],[16,y+2,16],'beam')
  for i in range(6):box([2+i*2,2+i*2,0],[4+i*2,4+i*2,1],'beam')
 elif kind=='firewood_bundle':
  for x,y in [(1,0),(6,0),(11,0),(4,4),(9,4)]:box([x,y,2],[x+4,y+4,14],'log')
  box([0,0,6],[16,1,8],'rope');box([2,7,6],[14,8,8],'rope');box([0,1,6],[2,6,8],'rope');box([14,1,6],[16,6,8],'rope')
 elif kind=='training_dummy':
  box([6,0,6],[10,15,10],'beam');box([0,9,6],[16,11,10],'beam');box([3,5,4],[13,12,12],'straw');box([5,12,5],[11,16,11],'straw');box([2,6,3],[14,7,13],'rope')
 elif kind=='signal_bell':
  for x in [1,13]:box([x,0,6],[x+2,16,10],'beam')
  box([1,14,6],[15,16,10]);box([7,12,7],[9,15,9],'iron');box([4,5,7],[12,13,9],'iron');box([12,3,7],[13,11,8],'rope');box([11,2,6],[14,4,9],'beam')
 return b

def generate():
 for name,color in [('furniture_cloth',(145,125,88)),('furniture_rope',(105,86,53)),('furniture_metal',(91,94,93))]:
  png(A/f'textures/block/{name}.png',16,16,lambda x,y,c=color:tuple(v+(-10 if x%4==0 or y%4==0 else 4)for v in c)+(255,))
 ids=[]
 for wood in ['pine','linden']:
  ids.extend((wood+'_'+k,k,wood)for k in KINDS)
 ids.extend((k,k,'pine')for k in EXTRA)
 for name,kind,wood in ids:
  tex={'wood':f'slavicmyths:block/{wood}_planks','beam':f'slavicmyths:block/{wood}_stripped_log','iron':'slavicmyths:block/furniture_metal','cloth':'slavicmyths:block/furniture_cloth','rope':'slavicmyths:block/furniture_rope','straw':'minecraft:block/hay_block_side','log':'slavicmyths:block/pine_log_top','particle':f'slavicmyths:block/{wood}_planks'}
  for half in (['lower','upper']if kind=='wardrobe'else ['']):write(A/f'models/block/{name}{"_"+half if half else ""}.json',{'textures':tex,'elements':boxes(kind,half=='upper')})
  variants={}
  for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
   for half in (['lower','upper']if kind=='wardrobe'else ['']):variants['facing='+facing+(',half='+half if half else '')]={'model':f'slavicmyths:block/{name}{"_"+half if half else ""}','y':y}
  write(A/f'blockstates/{name}.json',{'variants':variants});write(A/f'models/item/{name}.json',{'parent':f'slavicmyths:block/{name}'+('_lower'if kind=='wardrobe'else'')})
  entry={'type':'minecraft:item','name':'slavicmyths:'+name}
  if kind=='wardrobe':entry['conditions']=[{'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'half':'lower'}}]
  write(D/f'loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
  patterns={'table':['PPP',' S ','S S'],'chair':['P  ','PPP','S S'],'stool':['PP','SS'],'bench':['PPP','S S'],'shelf':['PPP','S  '],'wardrobe':['PPP','PLP','PPP'],'bedside_cabinet':['PP','PP','SS'],'weapon_rack':['S S','PPP','S S'],'cloth_bag':[' W ','WLW','WWW'],'wooden_crate':['SPS','PPP','SPS'],'firewood_bundle':['LLL',' S '],'training_dummy':[' H ','SHS',' S '],'signal_bell':['SSS','SIS','S S']}
  pattern=patterns[kind];allkeys={'P':{'item':f'slavicmyths:{wood}_planks'},'S':{'item':'minecraft:stick'},'W':{'item':'minecraft:white_wool'},'L':{'item':'minecraft:leather'if kind=='cloth_bag'else'slavicmyths:pine_log'},'H':{'item':'minecraft:hay_block'},'I':{'item':'minecraft:iron_block'}}
  write(D/f'recipes/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:v for k,v in allkeys.items()if k in ''.join(pattern)},'result':{'item':'slavicmyths:'+name}})
  merge_tag('slavicmyths','blocks','furniture',['slavicmyths:'+name]);merge_tag('slavicmyths','items','furniture',['slavicmyths:'+name])
 def feather(x,y):
  if x==(14-y//2)and 1<=y<=14:return (166,170,164,255)
  d=x-(14-y//2)
  if 2<=y<=12 and -4<=d<=2 and (x+y)%5!=0:return (35,44,59,255)if d in (-4,2)else(18,23,30,255)
  return(0,0,0,0)
 png(A/'textures/item/mysterious_black_feather.png',16,16,feather)
 write(A/'models/item/mysterious_black_feather.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/mysterious_black_feather'}})
 rus=['Стол','Стул','Табурет','Лавка','Полка','Шкаф','Тумба','Оружейная стойка'];extra_ru=['Тканевый мешок','Деревянный ящик','Связка дров','Тренировочный манекен','Разбойничий сигнальный колокол']
 for lang in ['en_us','ru_ru']:
  data=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));ru=lang=='ru_ru'
  for name,kind,wood in ids:data['block.slavicmyths.'+name]=(rus[KINDS.index(kind)]+(' из сосны'if wood=='pine'else' из липы')if kind in KINDS else extra_ru[EXTRA.index(kind)])if ru else name.replace('_',' ').title()
  data['item.slavicmyths.mysterious_black_feather']='Загадочное чёрное перо'if ru else 'Mysterious Black Feather'
  data['entity.slavicmyths.furniture_seat']='Сиденье'if ru else 'Seat'
  data['book.slavicmyths.forest_whistle.title']='Свист в чаще'if ru else 'A Whistle in the Thicket'
  data['book.slavicmyths.forest_whistle.text']='Даже атаманы не хозяева этим дорогам. Когда над лесом раздаётся свист, костры гасят, а люди ищут укрытие за толстыми стенами. В глубине стана стоит старое дерево с пустым помостом. В щелях досок застряли тёмные перья, а тяжёлые мишени расколоты так, словно по ним ударили целым стволом.'if ru else 'Even the atamans do not rule these roads. When a whistle carries over the forest, fires are put out and people shelter behind thick walls. Deep inside the settlement an old tree bears an empty platform. Dark feathers catch between its boards, and heavy targets are split as though struck by a whole trunk.'
  data['advancement.slavicmyths.black_feather.title']='Свист в чаще'if ru else 'A Whistle in the Thicket';data['advancement.slavicmyths.black_feather.description']='Найдите загадочное чёрное перо'if ru else 'Find a mysterious black feather'
  write(A/f'lang/{lang}.json',data)
 write(D/'advancements/black_feather.json',{'parent':'slavicmyths:bad_people','display':{'icon':{'item':'slavicmyths:mysterious_black_feather'},'title':{'translate':'advancement.slavicmyths.black_feather.title'},'description':{'translate':'advancement.slavicmyths.black_feather.description'},'frame':'task','hidden':True,'show_toast':True,'announce_to_chat':False},'criteria':{'found':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'slavicmyths:mysterious_black_feather'}]}}}})
 write(ROOT/'docs/verification/furniture-0.8.1.json',{'blocks':[n for n,k,w in ids],'items':[n for n,k,w in ids]+['mysterious_black_feather']})
if __name__=='__main__':generate()
