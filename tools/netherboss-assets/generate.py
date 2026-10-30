from pathlib import Path
import json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2]
RES=ROOT/'src/main/resources'
NAMES={
'morova_thread':('Моровая нить','Morova Thread'),'faded_rite_seal':('Печать угасшего обряда','Faded Rite Seal'),
'ancestor_bone_charm':('Костяной оберег предков','Ancestor Bone Charm'),'nameless_ashes':('Безымянный прах','Nameless Ashes'),
'broken_watch_horn':('Сломанный дозорный рог','Broken Watch Horn'),'darkened_waymark':('Потемневший путевой знак','Darkened Waymark'),
'mourning_circlet':('Траурный венец','Mourning Circlet'),'charred_signal_ribbon':('Обугленная сигнальная лента','Charred Signal Ribbon'),
'legendary_soul':('Легендарная душа','Legendary Soul'),'scroll_of_waning':('Свиток угасания','Scroll of Waning'),'scroll_of_last_call':('Свиток последнего зова','Scroll of the Last Call'),
'rune_morova_lingering':('Руна Тлеющего мора','Rune of Lingering Plague'),'rune_trizna_ember_ward':('Руна Погребального жара','Rune of Ember Ward'),
'amulet_of_the_faded':('Амулет угасших','Amulet of the Faded'),'last_watch_horn':('Рог последнего дозора','Last Watch Horn'),
'morova_silence_pendant':('Кулон морового безмолвия','Morova Silence Pendant'),'fallen_druzhina_horn':('Рог павшей дружины','Fallen Druzhina Horn')}
PAIRS={'nether_watchpost':['broken_watch_horn','charred_signal_ribbon'],'wayfarer_camp':['morova_thread','darkened_waymark'],'forgotten_shrine':['faded_rite_seal','nameless_ashes'],'ancestor_shrine':['ancestor_bone_charm','mourning_circlet'],'trizna_pyre':['nameless_ashes','charred_signal_ribbon']}
for table,pair in PAIRS.items():
 p=RES/f'data/slavicmyths/loot_table/chests/{table}.json';data=json.loads(p.read_text())
 data['pools']=[pool for pool in data['pools'] if not pool.get('name','').startswith('slavicmyths:boss_components_21x')]
 for index,item in enumerate(pair):data['pools'].append({'name':'slavicmyths:boss_components_21x_'+str(index),'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item}]})
 p.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
for name,vanilla,components in [
 ('amulet_of_the_faded',['spider_eye','ghast_tear','soul_sand','bone'],['morova_thread','faded_rite_seal','ancestor_bone_charm','nameless_ashes']),
 ('last_watch_horn',['blaze_rod','ghast_tear','netherite_scrap','bone'],['broken_watch_horn','darkened_waymark','mourning_circlet','charred_signal_ribbon'])]:
 keys={str(i+1):{'item':'slavicmyths:'+component} for i,component in enumerate(components)}
 keys.update({chr(65+i):{'item':'minecraft:'+item} for i,item in enumerate(vanilla)})
 recipe={'type':'minecraft:crafting_shaped','pattern':['1A2','B C','3D4'],'key':keys,'result':{'id':'slavicmyths:'+name,'count':1}}
 (RES/f'data/slavicmyths/recipe/{name}.json').write_text(json.dumps(recipe,indent=2)+'\n')
for i,(name,names) in enumerate(NAMES.items()):
 image=Image.new('RGBA',(16,16));d=ImageDraw.Draw(image)
 dark='#252329';silver='#b6ad91';red='#8f2436';gold='#d9a954';green='#83a083'
 if 'horn' in name:
  d.polygon([(2,3),(5,2),(7,6),(10,8),(14,8),(13,11),(8,12),(5,9)],fill=dark)
  d.line([(3,3),(6,6),(7,9),(11,10),(13,9)],fill=silver,width=2)
  d.line([(5,4),(7,3)],fill=gold,width=1)
  if name=='fallen_druzhina_horn':d.line([(8,8),(8,12)],fill=gold,width=2);d.polygon([(10,10),(13,10),(12,15),(9,14)],fill=red)
  if name=='broken_watch_horn':d.rectangle((9,7,10,11),fill=(0,0,0,0));d.point((8,13),fill=silver)
 elif 'rune_' in name:
  d.polygon([(5,1),(11,2),(14,7),(11,14),(3,14),(1,8)],fill=dark,outline=silver)
  if 'morova' in name:d.line([(5,4),(10,4),(10,9),(5,9),(5,12)],fill=green,width=1);d.point((8,6),fill=red)
  else:d.polygon([(8,3),(11,9),(9,12),(5,12),(4,9),(7,7)],fill='#ec742f');d.line([(8,7),(8,11)],fill=gold)
 elif 'scroll' in name:
  d.rectangle((3,3,12,12),fill=silver,outline=gold);d.rectangle((2,2,13,4),fill=gold);d.rectangle((2,11,13,13),fill=gold)
  d.line([(5,6),(10,6),(5,8),(10,9)],fill=red if name=='scroll_of_last_call' else green)
 elif name=='legendary_soul':
  d.polygon([(7,0),(11,4),(10,7),(14,9),(11,13),(8,15),(3,12),(2,8),(5,6)],fill='#9074c9');d.polygon([(7,4),(10,9),(8,13),(5,11)],fill='#e8ddfa')
 elif 'amulet' in name or 'pendant' in name or 'charm' in name:
  d.arc((3,0,12,10),0,180,fill=silver,width=1);d.line([(3,5),(6,9),(9,9),(12,5)],fill=silver)
  d.polygon([(7,7),(11,10),(9,15),(5,14),(4,10)],fill=dark,outline=gold)
  d.line([(7,9),(7,12),(9,12)],fill=red if 'amulet' in name else green,width=1)
 elif 'thread' in name or 'ribbon' in name:
  d.polygon([(3,1),(8,3),(5,7),(11,9),(7,13),(11,15),(6,14),(8,10),(2,8),(5,4)],fill=red);d.line([(4,2),(6,4),(4,7)],fill='#d47570')
 elif 'circlet' in name:
  d.polygon([(2,6),(4,2),(6,6),(8,1),(10,6),(12,3),(14,7),(12,12),(4,12)],fill=silver,outline=dark);d.rectangle((3,9,12,11),fill=red)
 elif 'ashes' in name:
  d.polygon([(3,5),(12,5),(14,12),(10,15),(4,14),(1,11)],fill='#726a68',outline=dark);d.rectangle((5,2,10,5),fill=silver);d.point((6,8),fill='#d8d1b9');d.point((9,11),fill='#bdb19d')
 else:
  d.polygon([(4,2),(11,1),(14,11),(10,14),(2,12)],fill=dark,outline=silver);d.line([(6,4),(10,5),(6,10),(11,11)],fill=gold if 'seal' in name else red,width=1)
 image.save(RES/f'assets/slavicmyths/textures/item/{name}.png')
 (RES/f'assets/slavicmyths/models/item/{name}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}},indent=2)+'\n')
cat=RES/'assets/slavicmyths/creative_catalog.tsv';lines=cat.read_text(encoding='utf-8').splitlines();known={x.split('\t')[0] for x in lines}
for name in NAMES:
 if name not in known:lines.append(name+'\t'+('accessories' if name in ('morova_silence_pendant','fallen_druzhina_horn') else 'magic')+'\tbosses_21x')
cat.write_text('\n'.join(lines)+'\n',encoding='utf-8')
for lang,index in [('ru_ru',0),('en_us',1)]:
 p=RES/f'assets/slavicmyths/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
 for name,names in NAMES.items():data['item.slavicmyths.'+name]=names[index]
 data['entity.slavicmyths.nether_retainer']='Призванный дружинник' if index==0 else 'Summoned Retainer'
 for key,pair in {'nether_only':('Ритуал возможен только в Незере.','The ritual requires the Nether.'),'busy':('Поблизости уже идёт ритуал или бой.','A nearby encounter or ritual is already active.'),'unloaded':('Область ритуала ещё не загружена.','The ritual area is not loaded.'),'altar':('Неверный ритуальный алтарь.','Invalid ritual altar.'),'space':('Недостаточно свободного места для босса.','Not enough space for the boss.')}.items():data['netherboss.slavicmyths.'+key]=pair[index]
 data['netherboss.rune.morova_lingering.effect']=('Прямой удар: продление существующих вредных эффектов на 25%, до 5 с; 10 с на цель/эффект.','Direct hit: extend existing harmful effects by 25%, max 5s; 10s per target/effect.')[index]
 data['netherboss.rune.trizna_ember_ward.effect']=('В бою: 2 HP огня каждые 2 с в радиусе 4; лечение 1 HP/5 с, до 6 HP/30 с после урона.','In combat: 2 fire HP every 2s within 4 blocks; heal 1 HP/5s, max 6 HP/30s after damage.')[index]
 data['netherboss.rune.morova_lingering.compatible']=('Оружие ближнего боя; руническая наковальня.','Melee weapons; runic anvil.')[index]
 data['netherboss.rune.trizna_ember_ward.compatible']=('Только нагрудники; руническая наковальня.','Chestplates only; runic anvil.')[index]
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Generated',len(NAMES),'distinct item assets, two recipes, five additive loot pools')
