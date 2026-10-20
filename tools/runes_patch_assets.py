"""Reproduce ONLY the five new rune models/icons, four recipes and owned translations."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,re
R=Path(__file__).resolve().parents[1]/'src/main/resources'
IDS=['counterstrike_iii','hunter_iii','last_stand_iii','depths_iii','veles_oath_iv']
shapes=[[(4,2),(11,2),(13,4),(12,13),(3,13),(2,5)],[(6,1),(10,2),(11,13),(8,15),(5,13),(4,3)],[(8,1),(14,7),(8,15),(1,8)],[(2,5),(5,3),(13,4),(15,7),(13,12),(3,13),(1,10)],[(5,1),(11,1),(14,4),(14,11),(11,14),(4,14),(1,11),(1,5)]]
palettes=[('#302824','#665c52','#939da0','#e6d686'),('#242d24','#485a3f','#758364','#d3ac59'),('#222934','#49566b','#8596a4','#e68c55'),('#262d32','#555c59','#8a8170','#62bbbd'),('#171c19','#514334','#92714b','#70a682')]
for id,shape,pal in zip(IDS,shapes,palettes):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon(shape,fill=pal[1],outline=pal[0]);d.line(shape[:3],fill=pal[2],width=1)
 if id=='counterstrike_iii':
  d.line([(5,5),(8,5),(8,8),(6,10),(4,8),(4,5)],fill=pal[2],width=1);d.line([(9,9),(11,6)],fill=pal[3]);d.point((9,5),fill=pal[3])
 elif id=='hunter_iii':
  d.line([(8,11),(8,4),(6,6)],fill=pal[3]);d.line([(8,4),(10,6)],fill=pal[3]);d.line([(5,7),(5,9),(6,10)],fill=pal[2]);d.line([(11,7),(11,9),(10,10)],fill=pal[2]);d.point((8,13),fill=pal[3])
 elif id=='last_stand_iii':
  d.polygon([(5,5),(10,5),(11,8),(8,12),(4,8)],fill=pal[2],outline=pal[0]);d.line([(8,5),(7,7),(9,8),(7,10)],fill=pal[0]);d.point((8,8),fill=pal[3]);d.point((7,9),fill=pal[3])
 elif id=='depths_iii':
  d.line([(3,6),(6,6),(6,8),(9,8),(9,10),(12,10)],fill=pal[0]);d.line([(10,5),(9,6),(10,7),(10,9)],fill=pal[3]);d.point((6,10),fill=pal[2])
 else:
  d.line([(5,3),(11,3),(12,5),(12,10),(10,12),(5,12),(3,10),(3,5),(5,3)],fill=pal[2]);d.line([(6,5),(6,4),(5,4)],fill=pal[3]);d.line([(10,5),(10,4),(11,4)],fill=pal[3]);d.line([(5,7),(7,6),(10,8),(8,10),(5,9),(9,6),(11,7),(10,10),(7,9)],fill=pal[2]);d.rectangle((7,7,8,8),fill=pal[3])
 from runes102_all import write_owned
 target=R/f'assets/slavicmyths/textures/item/rune_{id}.png'
 if not write_owned(target):im.resize((64,64),Image.Resampling.NEAREST).save(target)
 (R/f'assets/slavicmyths/models/item/rune_{id}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/rune_{id}'}},indent=2)+'\n')
materials=[ [('perunite_dust',3),('iron_dust',4)],[('diamond_dust',3),('soul_fragment',3)],[('perunite_dust',4),('empowered_soul',1)],[('diamond_dust',4),('perunite_dust',2)] ]
for id,needs in zip(IDS,materials):
 recipe={'type':'slavicmyths:rune_crafting','inputs':[{'ingredient':{'item':'slavicmyths:blank_rune_3'},'count':1,'base':{'tier':3,'material':'perunite'}}]+[{'ingredient':{'item':'slavicmyths:'+item},'count':n} for item,n in needs],'result':{'id':'slavicmyths:rune_'+id,'count':1},'chisel':False}
 (R/f'data/slavicmyths/recipe/rune_rework_{id}.json').write_text(json.dumps(recipe,indent=2)+'\n')
ru_names=['Руна Ответного удара III','Руна Охотника III','Руна Последнего рубежа III','Руна Глубин III','Руна Велесовой клятвы IV']
en_names=['Counterstrike Rune III','Hunter Rune III','Last Stand Rune III','Depths Rune III',"Veles’ Oath Rune IV"]
ru_effects=['Подтверждённый блок: следующий прямой удар +%s HP за %s с; КД %s с. Создаётся из заготовки III на рунической наковальне.','Стрела с расстояния от точки выстрела ≥%s блоков: +%s%% урона, КД %s с. Создаётся из заготовки III на рунической наковальне.','При HP ≤%s%% один боевой удар −%s%%; КД %s с. Создаётся из заготовки III на рунической наковальне.','Руда без обзора неба, Y ≤ морской уровень −16: +%s%% скорости. Общий бонус шахтёрских рун ≤%s%%. Заготовка III на наковальне.','Серия прямых попаданий по одной цели: +%s%% за прошлую метку, до +%s%%; сброс через %s с. Не создаётся на наковальне: обмен у Яги после сюжетной награды за очищение Горыныча.']
en_effects=['Confirmed block: next direct melee hit +%s HP within %s s; cooldown %s s. Crafted from a tier III blank at the rune anvil.','Arrow ≥%s blocks from launch position: +%s%% damage, cooldown %s s. Crafted from a tier III blank at the rune anvil.','At HP ≤%s%%: one combat hit −%s%% damage; cooldown %s s. Crafted from a tier III blank at the rune anvil.','Ore without sky access at Y ≤ sea level −16: +%s%% mining speed. Combined mining rune bonus ≤%s%%. Tier III blank at the anvil.','Direct hits on one target: +%s%% per prior mark, up to +%s%%; resets after %s s. Not crafted at the anvil: Yaga exchange after claiming the story reward for purifying Gorynych.']
for lang,names,effects,compat in [('ru_ru',ru_names,ru_effects,['Только щит','Лук / арбалет','Только нагрудник','Только кирка','Прямое ближнее оружие; одна действующая IV']),('en_us',en_names,en_effects,['Shield only','Bow / crossbow','Chestplate only','Pickaxe only','Direct melee weapon; one effective IV'])]:
 p=R/f'assets/slavicmyths/lang/{lang}.json';s=p.read_text(encoding='utf-8');owned={}
 for id,name,effect,c in zip(IDS,names,effects,compat):owned.update({f'item.slavicmyths.rune_{id}':name,f'runes_patch.effect.{id}':effect,f'runes_patch.compatible.{id}':c})
 owned['rpg.slavicmyths.iv_occupied']='На предмете уже установлена руна IV.' if lang=='ru_ru' else 'This item already has a tier IV rune.'
 owned['runes_patch.extra_inactive']='Повторные особые руны сохранены, но не усиливают эффект. Их можно извлечь.' if lang=='ru_ru' else 'Extra special copies are preserved but inactive. They can be extracted.'
 owned['yaga.fail.gorynych_claim']='Сначала получите личную сюжетную награду за очищение Горыныча.' if lang=='ru_ru' else 'First claim your personal story reward for purifying Gorynych.'
 owned['rpg.slavicmyths.effect_ominous_aura']='Две активные копии в экипировке: обычная враждебная нечисть до 50 макс. HP не нападает первой. Боссы, особые существа и спровоцированные враги не подчиняются.' if lang=='ru_ru' else 'Two active copies across equipment: ordinary hostile creatures with up to 50 max HP do not attack first. Bosses, special creatures and provoked enemies are excluded.'
 if lang=='ru_ru':
  for key,value in json.loads(s).items():
   if 'ominous_aura' in key:
    try:owned[key]=value.encode('cp1251').decode('utf-8')
    except (UnicodeError,LookupError):pass
 # Surgical insertion/replacement, preserving foreign translation formatting and order.
 for key,value in owned.items():
  pattern=r'("'+re.escape(key)+r'"\s*:\s*)"(?:[^"\\]|\\.)*"'
  if re.search(pattern,s):s=re.sub(pattern,lambda m:m[1]+json.dumps(value,ensure_ascii=False),s)
  else:s=s.rstrip();s=s[:-1].rstrip()+',\n  '+json.dumps(key)+': '+json.dumps(value,ensure_ascii=False)+'\n}\n'
 json.loads(s);p.write_text(s,encoding='utf-8')
if __name__=='__main__':print('5 models/icons, 4 recipes, owned RU/EN generated')
