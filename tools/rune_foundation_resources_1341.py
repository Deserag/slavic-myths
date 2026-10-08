"""Reproducible, native pixel assets and counted JSON recipes for Rune Foundation."""
from pathlib import Path
import json,random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
NAMES={
'rune_chisel':('Рунное зубило','Rune Chisel'),
'iron_dust':('Железная рунная пыль','Iron Rune Dust'),'gold_dust':('Золотая рунная пыль','Gold Rune Dust'),
'coal_dust':('Угольная рунная пыль','Coal Rune Dust'),'lapis_dust':('Лазуритовая рунная пыль','Lapis Rune Dust'),
'diamond_dust':('Алмазная рунная пыль','Diamond Rune Dust'),'perunite_dust':('Перунитовая рунная пыль','Perunite Rune Dust'),
'soul_fragment':('Осколок души','Soul Fragment'),'soul':('Душа','Soul'),
'empowered_soul':('Усиленная душа','Empowered Soul'),'bound_soul':('Заключённая душа','Bound Soul'),
'blank_rune_1':('Пустая руна I','Blank Rune I'),'blank_rune_2':('Пустая руна II','Blank Rune II'),'blank_rune_3':('Пустая руна III','Blank Rune III')}
TEXT={'tier':('Уровень: %s','Tier: %s'),'base':('Основа: %s','Base: %s'),'no_effect':('Пустая основа: боевого эффекта нет','Blank substrate: no combat effect'),
'create':('Создание','Create'),'enhance':('Усиление','Enhance'),'install':('Установка','Install'),'remove':('Снять','Remove'),
'execute':('Выполнить','Apply'),'recipe':('Рецепт','Recipe'),'recipe_hint':('Выбор рецепта / колесо','Select recipe / scroll'),
'tool':('Зубило','Chisel'),'material':('Материал','Material'),'durability':('Зубило: -1 прочность','Chisel: -1 durability'),
'requirement':('%s: %s/%s','%s: %s/%s')}
for m,ru,en in [('iron','Железо','Iron'),('gold','Золото','Gold'),('diamond','Алмаз','Diamond'),('perunite','Перунит','Perunite')]:TEXT['material.'+m]=(ru,en)
TEXT.update({'info.rune_chisel':('Наковальня: измельчение, 192 применения','Runic Anvil: grinding, 192 uses'),
'info.soul_fragment':('Упыри курганов: 35%; 4 осколка для души','Burial upyrs: 35%; 4 fragments for a soul'),
'info.soul':('Создание рун I; воевода/волхв 50%, князь 100%','Rune I crafting; voevoda/volkhv 50%, prince 100%'),
'info.empowered_soul':('Компонент для пустой руны II','Component for Blank Rune II'),
'info.bound_soul':('Компонент для пустой руны III','Component for Blank Rune III')})
for material in ('iron','gold','coal','lapis','diamond','perunite'):TEXT['info.'+material+'_dust']=('Рунная наковальня: 1 материал + зубило = 4 пыли','Runic Anvil: 1 material + chisel = 4 dust')
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def recipe(name,inputs,result,chisel=False,base=None):
 out={'id':'slavicmyths:'+result[0],'count':result[1]}
 if base:out['components']={'slavicmyths:rune_base':{'tier':base[0],'material':base[1]}}
 write(RES/f'data/slavicmyths/recipe/rune_foundation_{name}.json',{'type':'slavicmyths:rune_crafting','inputs':[{'ingredient':{'item':i},'count':n} for i,n in inputs],'result':out,'chisel':chisel})
def sprite(id):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);rng=random.Random(id)
 if id.endswith('_dust'):
  color={'iron_dust':(168,173,181),'gold_dust':(218,159,36),'coal_dust':(54,58,67),'lapis_dust':(42,85,173),'diamond_dust':(65,190,181),'perunite_dust':(145,87,187)}[id]
  d.polygon([(3,26),(8,21),(12,19),(14,12),(18,10),(21,17),(24,21),(29,26),(23,29),(9,29)],fill=tuple(int(c*.45) for c in color)+(255,))
  for y in range(12,28,2):
   width=3+(y-12)//2
   for x in range(16-width,17+width,2):
    v=rng.choice([.7,.9,1.1,1.25]);d.rectangle((x,y,x+1,y+1),fill=tuple(min(255,int(c*v)) for c in color)+(255,))
  for x,y in [(4,21),(25,19),(28,24)]:d.rectangle((x,y,x+1,y+1),fill=color+(255,))
 elif id=='rune_chisel':
  d.polygon([(5,26),(9,29),(21,14),(17,11)],fill='#33241b');d.line([(8,25),(18,13)],fill='#936444',width=4)
  d.polygon([(15,13),(20,16),(28,6),(24,3)],fill='#454951');d.polygon([(18,12),(21,13),(26,6),(24,5)],fill='#a5afb7');d.line([(22,6),(25,8)],fill='#dbe3e4',width=1)
  d.line([(8,22),(11,25)],fill='#b99364',width=2);d.line([(12,18),(15,21)],fill='#b99364',width=2)
 elif id=='soul_fragment':
  d.polygon([(10,25),(12,12),(22,4),(24,17),(16,28)],fill='#41596e');d.polygon([(12,23),(14,13),(21,7),(20,18),(16,25)],fill='#8dc6d5');d.line([(15,21),(20,9)],fill='#d3eeef',width=2)
 elif id in ('soul','empowered_soul','bound_soul'):
  bound=id=='bound_soul';d.polygon([(9,25),(6,18),(11,11),(13,4),(20,10),(24,16),(22,24),(17,28)],fill='#3e355f' if bound else '#274963')
  d.polygon([(11,23),(10,17),(15,11),(17,7),(18,14),(21,18),(19,24),(15,26)],fill='#a895e8' if bound else '#81c6df');d.rectangle((14,17,17,23),fill='#e5ddff' if bound else '#d4edf0')
  if id!='soul':
   d.ellipse((6,7,25,28),outline='#48464e',width=3);d.line([(7,15),(24,15)],fill='#8b7d6d',width=2)
   for x,y in [(7,11),(21,11),(7,23),(21,23)]:d.rectangle((x,y,x+2,y+2),fill='#aa8b54')
   if bound:d.line([(13,6),(13,27)],fill='#5d526b',width=2);d.line([(20,7),(20,25)],fill='#5d526b',width=2)
 else:
  tier=int(id[-1]);colors=[('#544636','#c1b493','#44392d'),('#374652','#91afbd','#54bad1'),('#352e4b','#807198','#ba90e3')][tier-1]
  d.polygon([(5,9),(22,4),(28,22),(10,28),(4,23)],fill=colors[0]);d.polygon([(7,10),(21,7),(25,21),(11,25),(7,22)],fill=colors[1]);d.line([(10,12),(20,10)],fill='#e0d7c3' if tier==1 else '#b8c8d3',width=1)
  d.line([(16,10),(12,17),(18,22),(21,15),(16,10)],fill=colors[2],width=2);d.line([(16,10),(18,22)],fill=colors[2],width=1)
 im.save(RES/f'assets/slavicmyths/textures/item/{id}.png')
def main():
 for id in NAMES:
  write(RES/f'assets/slavicmyths/models/item/{id}.json',{'parent':'minecraft:item/handheld' if id=='rune_chisel' else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}});sprite(id)
 for index,lang in enumerate(('ru_ru','en_us')):
  p=RES/f'assets/slavicmyths/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
  data.update({'item.slavicmyths.'+k:v[index] for k,v in NAMES.items()});data.update({'rune_foundation.slavicmyths.'+k:v[index] for k,v in TEXT.items()});write(p,data)
 write(RES/'data/slavicmyths/recipe/rune_chisel.json',{'type':'minecraft:crafting_shaped','pattern':[' II',' SI','S  '],'key':{'I':{'item':'minecraft:iron_ingot'},'S':{'item':'minecraft:stick'}},'result':{'id':'slavicmyths:rune_chisel','count':1}})
 # Third existing component: iron rings; no Perunite gate for a starting tool.
 p=RES/'data/slavicmyths/recipe/rune_chisel.json';data=json.loads(p.read_text());data['pattern']=[' II',' RI','S  '];data['key']['R']={'item':'slavicmyths:iron_rings'};write(p,data)
 for m,i in [('iron','minecraft:iron_ingot'),('gold','minecraft:gold_ingot'),('coal','minecraft:coal'),('lapis','minecraft:lapis_lazuli'),('diamond','minecraft:diamond'),('perunite','slavicmyths:perunite')]:recipe('dust_'+m,[(i,1)],(m+'_dust',4),True)
 for m in ('coal','lapis'):recipe('soul_'+m,[('slavicmyths:soul_fragment',4),('slavicmyths:'+m+'_dust',1)],('soul',1))
 recipe('empowered_soul',[('slavicmyths:soul',1),('minecraft:iron_ingot',4),('slavicmyths:perunite',2)],('empowered_soul',1))
 recipe('bound_soul',[('slavicmyths:empowered_soul',1),('slavicmyths:diamond_dust',4),('slavicmyths:perunite_dust',4)],('bound_soul',1))
 for m in ('iron','gold'):recipe('blank_1_'+m,[('slavicmyths:soul',1),('slavicmyths:'+m+'_dust',4),('slavicmyths:coal_dust',4)],('blank_rune_1',1),base=(1,m))
 for m in ('diamond','perunite'):recipe('blank_2_'+m,[('slavicmyths:empowered_soul',1),('slavicmyths:'+m+'_dust',4),('slavicmyths:lapis_dust',4)],('blank_rune_2',1),base=(2,m))
 recipe('blank_3',[('slavicmyths:bound_soul',1),('slavicmyths:perunite_dust',4),('slavicmyths:diamond_dust',4)],('blank_rune_3',1),base=(3,'perunite'))
 # Additive and idempotent; all old drops retained.
 for id,chance in [('kurgan_voevoda',.5),('buried_volkhv',.5),('unresting_prince',1)]:
  p=RES/f'data/slavicmyths/loot_table/entities/{id}.json';data=json.loads(p.read_text(encoding='utf-8'));data['pools']=[pool for pool in data['pools'] if pool.get('name')!='rune_foundation_soul']
  pool={'name':'rune_foundation_soul','rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:soul'}]}
  if chance<1:pool['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
  data['pools'].append(pool);write(p,data)
 print('Rune Foundation: 14 item assets, 15 anvil recipes + chisel, 3 additive loot pools')
if __name__=='__main__':main()
