"""Rebuild the source-audit inventory and evidence; does not modify gameplay."""
from pathlib import Path
import json, re, hashlib
R=Path(__file__).resolve().parents[1]
O=R/'docs/balance/weapon-audit-20261122'; O.mkdir(exist_ok=True)
java={p.relative_to(R).as_posix():p.read_text(encoding='utf-8-sig') for p in (R/'src/main/java').rglob('*.java')}
lang=json.loads((R/'src/main/resources/assets/slavicmyths/lang/ru_ru.json').read_text(encoding='utf-8-sig'))
historical=(R/'docs/requirements/15-WEAPON-BY-ID.md').read_text(encoding='utf-8-sig')
ids=set(re.findall(r'^\| ([a-z_]+) \|',historical,re.M))
ids.update('ritual_knife perunite_axe butchering_knife mytar_sickle mytar_shackled_sickle lomeya_hammer lomeya_hammer_offhand neveya_scythe morovoy_staff nine_mor_spear nine_mor_axe nine_mor_shield storm_anchor abyss_staff storm_harpoon pharaohka_trident zhelva_shield retainer_shield perunite_shield ward_shield gift_ilya_shield navya_ostroga ashen_greatsword shadow_throwing_knife volcanic_heart_flail infernal_bow empowered_infernal_bow chud_multitool_axe'.split())
ids.update('zhara_'+x for x in ['sword','axe','spear','sulitsa','throwing_knife','flail'])
recipes=[]
for p in (R/'src/main/resources/data').rglob('*.json'):
 if '/recipe/' not in p.as_posix():continue
 d=json.loads(p.read_text(encoding='utf-8-sig')); result=d.get('result',{})
 if isinstance(result,str):out=result
 else:out=result.get('id',result.get('item','')) if isinstance(result,dict) else ''
 if out.startswith('slavicmyths:') and out.split(':')[1] in ids:recipes.append((out.split(':')[1],p.relative_to(R).as_posix(),d))
tiers={'stone':(1,131),'iron':(2,250),'gold':(0,32),'diamond':(3,1561),'netherite':(4,2031),'silver':(2,220),'perunite':(2.5,850)}
stats={}
for m,(b,dur) in tiers.items():
 for typ in ['spear','sulitsa','throwing_knife','flail']:
  id='spear' if m=='stone' and typ=='spear' else 'flail' if m=='iron' and typ=='flail' else m+'_'+typ
  dmg={'spear':4+b,'sulitsa':3+b,'throwing_knife':2+b,'flail':6+b}[typ]
  speed={'spear':1.2,'sulitsa':1.5,'throwing_knife':2.3,'flail':.7}[typ]
  stats[id]=(dmg,speed,dur)
def add(id,d,s,u):stats[id]=(d,s,u)
for id,d,s,u in [
 ('bone_knife',3,2.5,75),('silver_dagger',4,2.5,220),('ritual_knife',4,2.3,220),('silver_sword',6,1.6,220),('perunite_sword',6.5,1.6,850),('perunite_axe',8.5,.9,850),('battle_axe',10,.7,250),('thunder_axe',8.5,.9,850),('club',5,.9,59),('wooden_mace',5,1.05,59),('stone_mace',7,.85,131),('mace',8,.9,250),('silver_mace',7,1,220),('perunite_mace',9.5,.8,850),('ancient_chekan',5,1.2,90),('chekan',6,1.3,400),('ancient_carolingian_sword',4,1.6,90),('restored_carolingian_sword',6,1.6,450),('ancient_spear',4,1.2,90),('restored_spear',6,1.2,380),('nightingale_dagger',10,2,700),('tugarin_sword',10,1.8,1561),('ataman_flail',12,.7,1561),('victor_spear',12,1.2,1561),('likho_staff',10,1.2,1561),('thunder_spear',7.5,1.1,850),('berdysh',9,.7,250),('noon_sickle',6,1.5,220),('carved_staff',3,1.2,59),('storm_staff',6,1.2,240),('veles_staff',4,1.2,48),('morozko_staff',8,1.2,0),('pool_spear',9,1.2,420),('karachun_scythe',12,1,2400),('poloz_whip',13,1,2031),('trophy_kalin_sabre',9,1.3,1561),('mytar_sickle',9,1.6,2031),('mytar_shackled_sickle',21,1.3,2031),('lomeya_hammer',21,1.4,2031),('lomeya_hammer_offhand',21,1.4,2031),('neveya_scythe',22,1.2,2031),('morovoy_staff',7,1.6,1500),('nine_mor_spear',11,2,2031),('nine_mor_axe',27,.5,2031),('navya_ostroga',10,1.15,1800),('ashen_greatsword',15,.9,1950),('shadow_throwing_knife',6,2,1650),('volcanic_heart_flail',13,.9,1800),('butchering_knife',3,1.8,250),('chud_multitool_axe',10,1,2304),('pharaohka_trident',9,1.1,900),('storm_harpoon',9,1.1,0)]:add(id,d,s,u)
for form in ['sword','axe','spear','sulitsa','throwing_knife','flail']:
 base={'sword':(7,1.6),'axe':(9,1),'spear':(7,1.2),'sulitsa':(6,1.5),'throwing_knife':(5,2.3),'flail':(9,.7)}[form]
 add('zhara_'+form,round(base[0]*1.15,4),base[1],1874)
for id,dur in [('short_bow',300),('heavy_bow',500),('finist_bow',640),('serpent_crossbow',520),('three_breath_crossbow',680),('infernal_bow',1200),('empowered_infernal_bow',1600),('storm_anchor',400),('abyss_staff',320),('retainer_shield',280),('perunite_shield',420),('ward_shield',672),('gift_ilya_shield',672),('nine_mor_shield',2048),('zhelva_shield',1200),('bone_arrow',0)]:add(id,1,4,dur)
assert ids==set(stats), (ids-set(stats),set(stats)-ids)
def refs(id):
 found=[]
 symbols=[]
 for path,t in java.items():
  for match in re.finditer(r'(\w+)\s*=\s*(?:ModItems\.)?ITEMS\.register\(\s*"'+re.escape(id)+'"',t):symbols.append(match[1])
 for path,t in java.items():
  if '/client/' in path:continue
  for n,line in enumerate(t.splitlines(),1):
   if '"'+id+'"' in line or any(re.search(r'\b'+re.escape(s)+r'\b',line) for s in symbols if len(s)>4):
    found.append({'file':path,'line':n,'text':line.strip()})
 return found
rows=[]
for id in sorted(ids):
 d,s,u=stats[id]; rr=[{'file':p,'recipe':r} for i,p,r in recipes if i==id]
 row={'id':id,'name':lang.get('item.slavicmyths.'+id,id),'source_base_damage':d,'attacks_per_second':s,'source_durability':u,'nominal_melee_dps':round(d*s,4),'recipes':rr,'references':refs(id),'model_exists':(R/f'src/main/resources/assets/slavicmyths/models/item/{id}.json').exists()}
 rows.append(row)
(O/'catalog.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
lines=['# Поимённый каталог оружия: текущий исходный код','',f'Всего {len(rows)} ID. Числа рассчитаны по текущим конструкторам и default-компонентам. Это не измеренный игровой урон. Для щитов, луков и предметов без melee-атрибутов 1/4 — базовые атрибуты игрока, не урон способности. Нулевая прочность означает отсутствие износа, не сломанный предмет. Для SwordItem/TieredItem учтено переопределение прочности материалом; для WeaponDefaults и адской ковки — итоговый override.','', '| ID | Название | Удар | Скорость | Прочность | Номинальный melee DPS | Рецепты результата |','|---|---|---:|---:|---:|---:|---|']
for row in rows:
 lines.append('| '+ ' | '.join(str(row[k]) for k in ['id','name','source_base_damage','attacks_per_second','source_durability','nominal_melee_dps'])+' | '+('; '.join(x['file'].split('recipe/')[-1] for x in row['recipes']) or 'код выдачи/дропа: см. catalog.json')+' |')
(O/'CATALOG.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
sources={p:hashlib.sha256(t.encode()).hexdigest() for p,t in java.items() if any(x in p for x in ['/combat/','/nethersmithing/','/marine/','/ninemor/','/sisters/','/karachun/','/morozko/','/morovoy/','/finist/','/poloz/','/gorynych/','/rewards/']) or p.endswith('/ModItems.java')}
(O/'source-hashes.json').write_text(json.dumps(sources,indent=2),encoding='utf-8')
print(f'{len(rows)} IDs, {len(recipes)} recipes, {len(sources)} source hashes; inventory assertions passed')
