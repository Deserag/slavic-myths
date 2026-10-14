"""Reproduce only the eight tool-rune models/recipes/curated sprites and owned localized/catalog rows."""
from pathlib import Path
import json,re,shutil
R=Path(__file__).resolve().parents[1]/'src/main/resources'
IDS=[f'{f}_{tier}' for tier in ['iii','iv'] for f in ['miner','excavator','farmer','lumberjack']]
needs=[[('perunite_dust',4),('diamond_dust',4)],[('iron_dust',8),('perunite_dust',2)],[('gold_dust',4),('soul_fragment',4)],[('perunite_dust',3),('coal_dust',4)],[('perunite_dust',10),('bound_soul',1)],[('diamond_dust',8),('bound_soul',1)],[('gold_dust',8),('bound_soul',1)],[('perunite_dust',8),('bound_soul',1)]]
def write(p,v):(R/p).write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for id,materials in zip(IDS,needs):
 tier=4 if id.endswith('_iv') else 3
 write(f'data/slavicmyths/recipe/tool_rune_{id}.json',{'type':'slavicmyths:rune_crafting','inputs':[{'ingredient':{'item':f'slavicmyths:blank_rune_{tier}'},'count':1,'base':{'tier':tier,'material':'perunite'}}]+[{'ingredient':{'item':'slavicmyths:'+item},'count':n} for item,n in materials],'result':{'id':'slavicmyths:rune_'+id,'count':1},'chisel':False})
 write(f'assets/slavicmyths/models/item/rune_{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/rune_'+id}})
 shutil.copyfile(Path(__file__).parent/'tool-runes-art'/f'rune_{id}.png',R/f'assets/slavicmyths/textures/item/rune_{id}.png')
ru_names=['Шахтёра','Землекопа','Земледельца','Лесоруба'];en_names=['Miner','Excavator','Farmer','Lumberjack']
for lang in ['ru_ru','en_us']:
 owned={}
 for i,id in enumerate(IDS):
  iv=id.endswith('_iv');tier='IV' if iv else 'III';area='3×3×2 (18)' if iv else '3×3 (9)';field='5×5 (25)' if iv else '3×3 (9)';radius=7 if iv else 4;logs=96 if iv else 48
  owned['item.slavicmyths.rune_'+id]=f'Руна {ru_names[i%4]} {tier}' if lang=='ru_ru' else f'{en_names[i%4]} Rune {tier}'
  if lang=='ru_ru':
   effects=[f'Shift: подходящие блоки кирки, {area} включая центр. Плоскость по грани; второй слой IV — вглубь. Без достоверной грани только один блок.',f'Shift: подходящий грунт лопаты, {area} включая центр. Плоскость по грани; второй слой IV — вглубь.',f'Shift+ПКМ: вспашка или сбор зрелых культур, {field} включая центр. Нарост отдельно от грядок. Сбор: 1 использование прочности на культуру, без автопосадки.',f'Shift: естественные прямые стволы одной породы и соседние основания в R{radius} по XZ (перепад ≤2). До {logs} брёвен включая центр; листья не рубит. Сложные/сомнительные деревья и постройки пропускает.']
  else:
   effects=[f'Shift: valid pickaxe blocks, {area} including center. Plane follows the hit face; tier IV second layer goes inward. Unknown face: one block only.',f'Shift: valid shovel blocks, {area} including center. Plane follows the hit face; tier IV second layer goes inward.',f'Shift+right-click: till or harvest mature crops, {field} including center. Nether wart is a separate group. Harvest costs one durability use per crop; no auto-replant.',f'Shift: natural straight trunks of one species, neighboring bases within R{radius} in XZ (height difference ≤2). Up to {logs} logs including center; leaves stay. Complex/uncertain trees and buildings are skipped.']
  owned['tool_runes.effect.'+id]=effects[i%4]
 for f,ru,en in zip(['miner','excavator','farmer','lumberjack'],['Только кирка','Только лопата','Только мотыга','Только топор'],['Pickaxe only','Shovel only','Hoe only','Axe only']):owned['tool_runes.compatible.'+f]=ru if lang=='ru_ru' else en
 owned['tool_runes.crafting']='В основной руке. Крафтовая: перунитовая заготовка своего уровня + 2 материала на рунической наковальне. Без Shift — обычное одиночное действие. Прочность за каждый обработанный блок; стандартные чары, дроп и XP.' if lang=='ru_ru' else 'Main hand. Crafted at the rune anvil from a perunite blank of the matching tier + two materials. Without Shift: normal single action. Per-block durability; normal enchantments, drops and XP.'
 owned['tool_runes.inactive']='Неподходящий инструмент или более низкий уровень этой семьи: эффект не действует, данные сохранены.' if lang=='ru_ru' else 'Wrong tool or lower tier of this family: inactive, saved data is retained.'
 owned['rpg.slavicmyths.tool_family_occupied']='На инструменте уже установлена руна другой ступени этой семьи. Сначала извлеките её.' if lang=='ru_ru' else 'This tool already has another tier of this rune family. Extract it first.'
 p=R/f'assets/slavicmyths/lang/{lang}.json';s=p.read_text(encoding='utf-8')
 for key,value in owned.items():
  pattern=r'("'+re.escape(key)+r'"\s*:\s*)"(?:[^"\\]|\\.)*"';v=json.dumps(value,ensure_ascii=False)
  if re.search(pattern,s):s=re.sub(pattern,lambda m:m.group(1)+v,s)
  else:
   end=s.rfind('}');s=s[:end].rstrip()+',\n  '+json.dumps(key)+': '+v+'\n'+s[end:]
 json.loads(s);p.write_text(s,encoding='utf-8')
p=R/'assets/slavicmyths/creative_catalog.tsv';lines=p.read_text(encoding='utf-8').splitlines()
for tier in ['III','IV']:
 missing=[f'rune_{id}\tmagic\trunes_{tier}' for id in IDS if id.endswith('_'+tier.lower()) and not any(line.startswith('rune_'+id+'\t') for line in lines)]
 if missing:
  last=max(i for i,line in enumerate(lines) if line.endswith('\trunes_'+tier));lines[last+1:last+1]=missing
p.write_text('\n'.join(lines)+'\n',encoding='utf-8')
