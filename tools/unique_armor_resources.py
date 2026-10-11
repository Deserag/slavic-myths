"""Scoped reproducible pixel assets, additive recipes/loot; approved armor reference only.
Silhouettes: tall dark-leather boots with tiny cold feather fins; broad red lamellar
chest with modest iron shoulders; open-face iron helmet with short side fins; split
leather legs with separate thigh/knee plates; wooden pointed oval shield 10x14x2
with thick iron rim and geometric bronze ward. No new concept images.
"""
from pathlib import Path
import json
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
A=RES/'assets/slavicmyths'
D=RES/'data/slavicmyths'
IDS=['wind_boots','bogatyr_chestplate','clear_sight_helmet','indomitability_leggings','ward_shield','hardened_iron']
RECIPES={
 'wind_boots':['slavicmyths:seven_league_boots']+['slavicmyths:mysterious_black_feather']*4+['slavicmyths:goat_hide']*2,
 'bogatyr_chestplate':['minecraft:iron_chestplate']+['slavicmyths:hardened_iron']*4+['slavicmyths:princely_seal']*2,
 'clear_sight_helmet':['minecraft:iron_helmet','slavicmyths:oko_likha']+['slavicmyths:hardened_iron']*2+['slavicmyths:ancient_sign']*2,
 'indomitability_leggings':['minecraft:iron_leggings']+['slavicmyths:hardened_iron']*2+['slavicmyths:tugarinova_kozha']*2+['slavicmyths:klyk_volkolaka'],
 'ward_shield':['minecraft:shield']+['slavicmyths:hardened_iron']*2+['slavicmyths:warding_charm','slavicmyths:silver_ingot'],
}
COL={'edge':'#1c222a','steel':'#536373','shine':'#a8bdc9','dark':'#29242a','leather':'#493326','strap':'#8b6846','gold':'#bc9054','red':'#792d33','cloth':'#a04948','ice':'#75b6c6','wood':'#61402b'}
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def icon(kind):
 im=Image.new('RGBA',(32,32));p=ImageDraw.Draw(im)
 def box(bounds,c):p.rectangle(bounds,fill=COL[c])
 def poly(points,c):p.polygon(points,fill=COL[c])
 if kind=='wind_boots':
  for x in [5,18]:
   poly([(x,5),(x+8,5),(x+7,21),(x+10,24),(x+10,28),(x-3,28),(x-3,24),(x+1,21)],'edge')
   box((x+1,7,x+6,22),'leather');box((x,5,x+7,8),'steel');box((x,5,x+6,5),'shine');box((x-2,24,x+8,26),'steel');box((x-2,24,x+7,24),'ice');box((x+1,12,x+6,13),'strap');box((x+1,18,x+6,19),'strap')
   poly([(x+6,15),(x+10,11),(x+9,17),(x+6,20)],'ice');box((x+3,12,x+4,13),'gold')
 elif kind=='bogatyr_chestplate':
  poly([(8,4),(12,4),(13,8),(19,8),(20,4),(24,4),(30,8),(29,17),(25,18),(25,28),(7,28),(7,18),(3,17),(2,8)],'edge')
  box((8,8,24,27),'leather');box((13,8,19,25),'red')
  for y in [10,14,18,22]:
   box((8,y,12,y+2),'steel');box((20,y,24,y+2),'steel');box((8,y,12,y),'shine');box((20,y,24,y),'shine');box((14,y,18,y),'gold')
  for x in [3,24]:box((x,8,x+4,13),'steel');box((x,8,x+4,8),'shine');box((x,14,x+4,15),'gold')
  box((8,25,24,26),'strap');box((15,25,17,27),'gold')
 elif kind=='clear_sight_helmet':
  poly([(8,8),(12,5),(20,5),(24,8),(26,13),(25,26),(20,27),(20,16),(12,16),(12,27),(7,26),(6,13)],'edge')
  poly([(9,9),(13,6),(19,6),(23,9),(24,13),(8,13)],'steel');box((11,8,21,9),'shine');box((8,14,24,16),'strap');box((8,17,10,25),'steel');box((22,17,24,25),'steel');box((15,12,17,15),'ice')
  for x in [4,25]:poly([(x,6),(x+2,10),(x+2,19),(x,17),(x-1,12)],'gold');box((x,7,x,13),'shine')
 elif kind=='indomitability_leggings':
  poly([(7,4),(24,4),(25,27),(18,28),(16,14),(14,28),(6,27)],'edge');box((7,5,24,8),'strap')
  for x in [7,18]:
   box((x,9,x+6,26),'leather');box((x,10,x+5,14),'steel');box((x,10,x+5,10),'shine');box((x,19,x+6,23),'steel');box((x+1,19,x+5,19),'shine');box((x,25,x+6,26),'strap')
  box((14,5,17,8),'steel');box((15,6,16,7),'ice')
 elif kind=='ward_shield':
  poly([(16,2),(26,7),(27,19),(23,25),(16,30),(9,25),(5,19),(6,7)],'edge');poly([(16,3),(25,8),(25,19),(22,24),(16,28),(10,24),(7,19),(8,8)],'steel');poly([(16,6),(23,9),(23,18),(20,23),(16,26),(12,23),(9,18),(10,9)],'wood')
  for x in [12,16,20]:p.line((x,9,x,22),fill=COL['strap'])
  p.line((16,9,16,23),fill=COL['gold'],width=2);p.line((10,16,22,16),fill=COL['gold'],width=2)
  p.line([(16,10),(21,16),(16,22),(11,16),(16,10)],fill=COL['gold']);box((15,15,17,17),'shine')
  for x,y in [(9,9),(23,9),(9,19),(23,19),(16,25)]:box((x,y,x,y),'shine')
 else:
  poly([(5,13),(12,8),(25,8),(28,13),(26,22),(9,25),(4,21)],'edge');poly([(6,13),(13,9),(24,9),(26,13),(10,18)],'shine');poly([(6,15),(10,19),(25,14),(24,20),(10,23),(6,20)],'steel');p.line((11,19,24,15),fill=COL['ice'])
 return im
def armor_texture(kind):
 im=Image.new('RGBA',(64,32));p=ImageDraw.Draw(im)
 # Standard humanoid UV islands; additional native plates reuse the same material islands.
 for bounds in [(0,0,31,15),(16,16,39,31),(40,16,63,31),(0,16,15,31)]:
  p.rectangle(bounds,fill=COL['leather'] if kind in ['wind','indomitability'] else COL['steel'])
  for y in range(bounds[1]+1,bounds[3],3):p.line((bounds[0],y,bounds[2],y),fill=COL['dark'])
 if kind=='bogatyr':
  p.rectangle((22,20,25,30),fill=COL['red']);p.rectangle((20,29,31,30),fill=COL['strap'])
  for y in [21,25,29]:p.line((22,y,25,y),fill=COL['gold'])
  for x in [20,28,44,52]:
   for y in [20,24,28]:p.rectangle((x,y,x+2,y+1),fill=COL['shine'])
 elif kind=='clear_sight':
  p.rectangle((8,9,15,13),fill=COL['dark']);p.line((8,8,15,8),fill=COL['strap']);p.rectangle((11,7,12,9),fill=COL['ice']);p.rectangle((24,0,31,10),fill=COL['gold'])
 elif kind=='wind':
  for y in [20,25]:p.rectangle((0,y,15,y+1),fill=COL['strap'])
  p.rectangle((0,16,15,18),fill=COL['steel']);p.line((0,17,15,17),fill=COL['shine']);p.rectangle((0,29,15,31),fill=COL['steel']);p.line((0,29,15,29),fill=COL['ice']);p.rectangle((24,16,31,23),fill=COL['ice'])
 else:
  p.rectangle((16,17,39,19),fill=COL['strap'])
  for y in [20,26]:p.rectangle((0,y,15,y+2),fill=COL['steel']);p.line((0,y,15,y),fill=COL['shine'])
 return im
def shield_model():
 elements=[]
 def cube(lo,hi,uv):elements.append({'from':lo,'to':hi,'faces':{f:{'texture':'#plate','uv':uv}for f in ['north','south','east','west','up','down']}})
 # Pointed oval, narrower than a tower shield, 10x14 with a wooden recessed face.
 for y,x,width in [(1,7,2),(2,6,4),(3,5,6),(4,4,8),(5,3,10),(7,3,10),(9,3,10),(11,4,8),(13,5,6),(14,7,2)]:
  h=2 if y in [5,7,9,11] else 1;cube([x,y,7],[x+width,y+h,9],[x,16-y-h,x+width,16-y])
 cube([7,5,9],[9,11,10],[14,8,18,22])
 display={'gui':{'rotation':[0,180,0],'scale':[1,1,1]},'ground':{'translation':[0,3,0],'scale':[.5,.5,.5]},'fixed':{'rotation':[0,180,0]},'firstperson_righthand':{'rotation':[0,0,5],'translation':[4,1,-2],'scale':[.9,.9,.9]},'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[2,3,2]},'firstperson_lefthand':{'rotation':[0,0,-5],'translation':[4,1,-2],'scale':[.9,.9,.9]},'thirdperson_lefthand':{'rotation':[0,-90,0],'translation':[2,3,2]}}
 return {'textures':{'plate':'slavicmyths:item/ward_shield','particle':'slavicmyths:item/ward_shield'},'elements':elements,'display':display,'gui_light':'front'}
def apply():
 (A/'textures/item').mkdir(parents=True,exist_ok=True);(A/'textures/armor').mkdir(parents=True,exist_ok=True)
 for name in IDS:
  icon(name).save(A/f'textures/item/{name}.png')
  if name!='ward_shield':write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
 for kind in ['wind','bogatyr','clear_sight','indomitability']:
  for layer in [1,2]:armor_texture(kind).save(A/f'textures/armor/unique_{kind}_layer_{layer}.png')
 model=shield_model();blocking=json.loads(json.dumps(model))
 for key in ['firstperson_righthand','firstperson_lefthand']:blocking['display'][key].update(rotation=[0,30,0],translation=[0,2,-3])
 write(A/'models/item/ward_shield_blocking.json',blocking)
 model['overrides']=[{'predicate':{'blocking':1},'model':'slavicmyths:item/ward_shield_blocking'}];write(A/'models/item/ward_shield.json',model)
 for name,ingredients in RECIPES.items():write(D/f'recipe/unique/{name}.json',{'type':'slavicmyths:armorer_shapeless','copy_components':True,'ingredients':[{'item':i}for i in ingredients],'result':{'id':f'slavicmyths:{name}','count':1}})
 for entity,count in [('kurgan_voevoda',1),('buried_volkhv',1),('unresting_prince',2)]:
  path=D/f'loot_table/entities/{entity}.json';table=json.loads(path.read_text(encoding='utf-8'));table['pools']=[p for p in table['pools'] if p.get('name')!='slavicmyths:unique_hardened_iron']
  table['pools'].append({'name':'slavicmyths:unique_hardened_iron','rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:hardened_iron','functions':[{'function':'minecraft:set_count','count':count}]}]});write(path,table)
 write(D/'tags/mob_effect/equipment_control.json',{'replace':False,'values':['minecraft:slowness','slavicmyths:class_root','slavicmyths:disorientation']})
 write(D/'tags/entity_type/clear_sight_excluded.json',{'replace':False,'values':['minecraft:armor_stand',{'id':'slavicmyths:rune_visual','required':False}]})
 ru=['Сапоги ветра','Нагрудник богатыря','Шлем ясного взора','Поножи неукротимости','Щит-оберег','Закалённое железо'];en=['Wind Boots','Bogatyr Chestplate','Clear Sight Helmet','Indomitability Leggings','Ward Shield','Hardened Iron']
 effects_ru=['Улучшение: Сапоги-скороходы • скорость +40%, прыжок ~4,15 блока, разбег +10%, падение −40%','Броня 8 • прочность брони 3 • отбрасывание −20%\nСтойкость при HP ≤35%: урон −15%, отбрасывание ещё −15%','Ясный взор: радиус 24 • 5 с • перезарядка 40 с','Контроль −35% • сильный толчок: сопротивление +35% на 5 с, КД 15 с','Воздаяние: 25% при блоке • отражение 125% поглощённого урона']
 effects_en=['Upgrade: Seven-League Boots • speed +40%, jump height ~4.15 blocks, running leap +10%, fall damage −40%','Armor 8 • toughness 3 • knockback resistance 20%\nStance at HP ≤35%: damage −15%, extra knockback resistance 15%','Clear Sight: radius 24 • 5 s • cooldown 40 s','Control duration −35% • strong knockback: +35% resistance for 5 s, cooldown 15 s','Retribution: 25% on block • returns 125% of absorbed damage']
 for locale,names,effects in [('ru_ru',ru,effects_ru),('en_us',en,effects_en)]:
  path=A/f'lang/{locale}.json';lang=json.loads(path.read_text(encoding='utf-8'))
  for name,title in zip(IDS,names):lang[f'item.slavicmyths.{name}']=title
  for name,effect in zip(IDS,effects):lang[f'item.slavicmyths.{name}.effect']=effect
  lines_ru=[['Улучшение: Сапоги-скороходы','Скорость +40% • высота 4,15–12,46 блока','Разгон за 8 с спринта, включая прыжки','Падение −40%; доп. высота прыжка защищена'],['Улучшение: Железный нагрудник','Броня 8 • прочность брони 3','Сопротивление отбрасыванию +20%','HP ≤35%: урон −15%, сопротивление +15%'],['Улучшение: Железный шлем','Ясный взор • радиус 24 блока','Длительность 5 с • перезарядка 40 с','Контуры живых существ сквозь стены'],['Улучшение: Железные поножи','Длительность контроля −35%','Сильный толчок: сопротивление +35%','Длительность 5 с • перезарядка 15 с'],['Улучшение: Щит','Воздаяние: 25% при успешном блоке','Отражает 125% поглощённого урона','Без отражения урона окружения']]
  lines_en=[['Upgrade: Seven-League Boots','Speed +40% • jump height 4.15–12.46 blocks','Charge over 8 s sprinting, including jumps','Fall −40%; extra jump height protected'],['Upgrade: Iron Chestplate','Armor 8 • toughness 3','Knockback resistance +20%','HP ≤35%: damage −15%, resistance +15%'],['Upgrade: Iron Helmet','Clear Sight • radius 24 blocks','Duration 5 s • cooldown 40 s','Living outlines through walls'],['Upgrade: Iron Leggings','Control duration −35%','Strong knockback: resistance +35%','Duration 5 s • cooldown 15 s'],['Upgrade: Shield','Retribution: 25% on successful block','Returns 125% of absorbed damage','No environmental reflection']]
  for name,lines in zip(IDS,lines_ru if locale=='ru_ru' else lines_en):
   for suffix,line in zip(['base','effect','detail','extra'],lines):lang[f'item.slavicmyths.{name}.{suffix}']=line
  lang['unique.slavicmyths.sockets']='Рунные слоты: 3' if locale=='ru_ru' else 'Rune capacity: 3'
  lang['unique.slavicmyths.sight_key']='Ясный взор: Alt + %s' if locale=='ru_ru' else 'Clear Sight: Alt + %s'
  lang['unique.slavicmyths.sight_cooldown']='Ясный взор: ещё %s с' if locale=='ru_ru' else 'Clear Sight: %s s remaining'
  lang['unique.slavicmyths.iron_source']='Курганы: воевода и волхв — 1 гарантированно; князь — 2.' if locale=='ru_ru' else 'Burials: voevoda and volkhv always drop 1; prince always drops 2.'
  lang['death.attack.ward_reflection']='%1$s пал от воздаяния %2$s' if locale=='ru_ru' else '%1$s fell to %2$s\u2019s retribution'
  write(path,lang)
 write(D/'damage_type/ward_reflection.json',{'message_id':'ward_reflection','scaling':'when_caused_by_living_non_player','exhaustion':.1})
if __name__=='__main__':apply()
