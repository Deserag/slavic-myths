"""Scoped staff enchantment membership and accurate RU/EN damage hints."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1]/'src/main/resources'
for name in ['weapon','sharp_weapon']:
 p=R/'data/minecraft/tags/item/enchantable'/f'{name}.json';d=json.loads(p.read_text())
 for id in ['storm_staff','veles_staff','morozko_staff']:
  if 'slavicmyths:'+id not in d['values']:d['values'].append('slavicmyths:'+id)
 p.write_text(json.dumps(d,indent=2)+'\n')
p=R/'data/minecraft/tags/item/enchantable/durability.json';d=json.loads(p.read_text())
for id in ['storm_staff','veles_staff']:
 if 'slavicmyths:'+id not in d['values']:d['values'].append('slavicmyths:'+id)
p.write_text(json.dumps(d,indent=2)+'\n')
for lang,text in [('ru_ru','Сила и чары урона усиливают обычный удар. Магия использует отдельный урон; чары «Сила» для лука здесь не работают.'),('en_us','Strength and damage enchantments improve melee hits. Magic uses separate damage; bow Power does not apply.')]:
 p=R/'assets/slavicmyths/lang'/f'{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d['combat.slavicmyths.staff_damage_scope']=text
 d['frost191.staff_hint']=('ЛКМ по близкой цели — удар посохом (8 HP); ЛКМ в воздух — снаряд. Удерживайте ПКМ для морозного потока. Остальные приёмы — через меню способностей и назначенные клавиши.' if lang=='ru_ru' else 'Attack a nearby entity for an 8 HP melee hit; attack air to fire a bolt. Hold use for the frost stream. Other moves use the ability menu and assigned keys.')
 for key in d:
  if key.startswith('frost191.') and isinstance(d[key],str):d[key]=d[key].replace('поток 1 HP','поток 3 HP').replace('stream 1 HP','stream 3 HP')
 p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
