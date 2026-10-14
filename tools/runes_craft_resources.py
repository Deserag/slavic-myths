"""Reproduce owned IV resources; curated pixel sources are in runes-craft-art."""
from pathlib import Path
import json,re,shutil
R=Path(__file__).resolve().parents[1]/'src/main/resources'
IDS=['unbreakable_iv','perun_rage_iv','forest_spirit_iv','mastery_iv']
needs=[ [('slavicmyths:perunite',4),('slavicmyths:bound_soul',1)], [('slavicmyths:perunite_dust',8),('slavicmyths:empowered_soul',1)], [('slavicmyths:bound_soul',1),('minecraft:emerald',8)], [('minecraft:netherite_ingot',1),('slavicmyths:diamond_dust',8)] ]
def write(path,value):
 (R/path).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for id,n in zip(IDS,needs):
 write(f'data/slavicmyths/recipe/rune_rework_{id}.json',{'type':'slavicmyths:rune_crafting','inputs':[{'ingredient':{'item':'slavicmyths:blank_rune_4'},'count':1,'base':{'tier':4,'material':'perunite'}}]+[{'ingredient':{'item':item},'count':count} for item,count in n],'result':{'id':'slavicmyths:rune_'+id,'count':1},'chisel':False})
write('data/slavicmyths/recipe/rune_foundation_blank_4.json',{'type':'slavicmyths:rune_crafting','inputs':[{'ingredient':{'item':'slavicmyths:blank_rune_3'},'count':1,'base':{'tier':3,'material':'perunite'}},{'ingredient':{'item':'slavicmyths:empowered_soul'},'count':2},{'ingredient':{'item':'slavicmyths:perunite_dust'},'count':8}],'result':{'id':'slavicmyths:blank_rune_4','count':1,'components':{'slavicmyths:rune_base':{'tier':4,'material':'perunite'}}},'chisel':False})
for item in ['blank_rune_4']+['rune_'+id for id in IDS]:
 write(f'assets/slavicmyths/models/item/{item}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+item}})
 source=Path(__file__).parent/'runes-craft-art'/f'{item}.png'
 if source.exists():shutil.copyfile(source,R/f'assets/slavicmyths/textures/item/{item}.png')
names={'ru_ru':['Руна Несокрушимости IV','Руна Перуновой ярости IV','Руна Духа леса IV','Руна Мастерства IV'],'en_us':['Unbreakable Rune IV',"Perun’s Rage Rune IV",'Forest Spirit Rune IV','Mastery Rune IV']}
effects={'ru_ru':['Враждебный удар от 8 HP после защиты: −25% урона; КД 50 с. Щит действует только при блокировании. Приоритет над Последним рубежом III.','4 прямых сильных удара (заряд ≥90%, интервал ≤8 с): на 5 с +16% урона и +10% скорости атаки; КД 45 с. Смена на оружие без этой руны отменяет эффект. Общий бонус урона рун ≤60%.','При HP ≤50% после 6 с без урона: 1 HP каждые 3 с, всего 4 HP за 12 с; КД 60 с от старта. Урон или снятие прерывают лечение.','Руды: +25% скорости, общий бонус шахтёрских рун ≤35%. +1 Fortune, суммарно до IV. При Silk Touch бонус Fortune отключён.'],'en_us':['Hostile hit of at least 8 HP after defenses: −25% damage; cooldown 50 s. Shield only while blocking. Takes priority over Last Stand III.','4 direct strong hits (charge ≥90%, gaps ≤8 s): +16% damage and +10% attack speed for 5 s; cooldown 45 s. Switching to a weapon without this rune cancels. Total rune damage bonus capped at 60%.','At HP ≤50%, after 6 s without damage: 1 HP every 3 s, 4 HP over 12 s; cooldown 60 s from start. Damage or removal interrupts healing.','Ores: +25% mining speed, combined mining rune bonus capped at 35%. +1 Fortune, total capped at IV. No Fortune bonus with Silk Touch.']}
compat={'ru_ru':['Нагрудник / используемый щит','Прямое ближнее оружие','Нагрудник / шлем','Только кирка'],'en_us':['Chestplate / shield while blocking','Direct melee weapon','Chestplate / helmet','Pickaxe only']}
for lang in names:
 p=R/f'assets/slavicmyths/lang/{lang}.json';s=p.read_text(encoding='utf-8');owned={}
 for id,name,effect,cat in zip(IDS,names[lang],effects[lang],compat[lang]):
  owned[f'item.slavicmyths.rune_{id}']=name
  owned[f'runes_patch.effect.{id}']=effect
  owned[f'runes_patch.compatible.{id}']=cat+(' — IV; крафт из перунитовой заготовки IV на рунической наковальне. Одинаковые копии не суммируются.' if lang=='ru_ru' else ' — IV; crafted from a perunite tier IV blank at the rune anvil. Identical copies do not stack.')
 owned['item.slavicmyths.blank_rune_4']='Заготовка руны IV' if lang=='ru_ru' else 'Blank Rune IV'
 current=json.loads(s)
 depth_note=' С Мастерством IV общий кап 35%.' if lang=='ru_ru' else ' With Mastery IV the combined cap is 35%.'
 iv_note=' (среди особых IV; крафтовые IV независимы)' if lang=='ru_ru' else ' (among special IV; crafted IV are independent)'
 for key,note in [('runes_patch.effect.depths_iii',depth_note),('runes_patch.compatible.veles_oath_iv',iv_note)]:
  owned[key]=current[key].removesuffix(note)+note

 for key,value in owned.items():
  pattern=r'("'+re.escape(key)+r'"\s*:\s*)"(?:[^"\\]|\\.)*"'
  replacement=json.dumps(value,ensure_ascii=False)
  if re.search(pattern,s):s=re.sub(pattern,lambda m:m.group(1)+replacement,s)
  else:
   end=s.rfind('}');s=s[:end].rstrip()+',\n  '+json.dumps(key)+': '+replacement+'\n'+s[end:]
 json.loads(s);p.write_text(s,encoding='utf-8')
p=R/'assets/slavicmyths/creative_catalog.tsv';s=p.read_text(encoding='utf-8')
for item in ['blank_rune_4']+['rune_'+id for id in IDS]:
 if not any(line.startswith(item+'\t') for line in s.splitlines()):s+='\n'+item+'\tmagic\trunes_IV'
p.write_text(s.rstrip()+'\n',encoding='utf-8')
