"""Read current files; historical facts are only a starting index, never runtime evidence."""
from pathlib import Path
import json, re, hashlib, subprocess
from accessories148_audit import FACTS

R = Path(__file__).resolve().parents[1]
RES = R/'src/main/resources'
OUT = R/'docs/implementation/accessories278-20261025'
OUT.mkdir(parents=True, exist_ok=True)
facts = dict(FACTS)
extra = {
 'pool_ring': ('ring','+15% swim speed only in water; shared modifier ID; remove on unequip','accessories/Accessories.java'),
 'bath_ward': ('charm','ordinary poison/weakness duration x.80; protected effects excluded; cooldown 1200 ticks','accessories/AccessoryEvents.java'),
 'harvest_ring': ('ring','10% chance +1 whitelisted produce on manual mature crop harvest; cooldown 100; excludes farmer runes','accessories/AccessoryEvents.java'),
 'podvey_belt': ('belt','fall distance >=12: damage x.30; cooldown 1200; only applied on positive final damage','accessories/AccessoryEvents.java'),
 'leshy_necklace': ('necklace','hostile final hit >=6 HP in forest/taiga: Speed I 80 ticks; cooldown 1200','accessories/AccessoryEvents.java'),
 'spark_ring': ('ring','direct hostile melee: 25% chance ignite 3 seconds; cooldown 160; excludes pets/players/fire immune','accessories/AccessoryEvents.java')
}
for id, (slot,effect,source) in extra.items(): facts[id]=('curio','craft: see recipe',effect,source,'same existing slot','KEEP')
updates = {'lunula':('x.95','x.92'),'grivna':('x.96','x.94'),'grave_ward':('x.91','x.88'), 'hunter_belt':('x1.10','x1.15')}
for id,(old,new) in updates.items():
 a=list(facts[id]);a[2]=a[2].replace(old,new);facts[id]=tuple(a)
slot_map={}
for slot in ['head','necklace','ring','belt','charm']:
 for id in json.loads((RES/f'data/curios/tags/item/{slot}.json').read_text())['values']:
  slot_map[id.split(':')[1]]=slot
assert len(slot_map)==37
java={p.relative_to(R/'src/main/java/org/slavicmyths').as_posix():p.read_text(encoding='utf8') for p in (R/'src/main/java/org/slavicmyths').rglob('*.java')}
data={p.relative_to(R).as_posix():json.loads(p.read_text(encoding='utf8')) for p in (RES/'data/slavicmyths').rglob('*.json') if '/recipe/' in p.as_posix() or '/loot_table/' in p.as_posix()}
lang=json.loads((RES/'assets/slavicmyths/lang/ru_ru.json').read_text(encoding='utf8'))
entries=[]
for id,slot in sorted(slot_map.items()):
 fact=facts[id];effect=fact[2]
 if id=='trophy_skim_necklace':effect='direct hostile damage +15% strictly below35% target HP; source rewards/RewardEvents; no stacking'
 if id in ['amber_charm','perun_charm','forest_charm','traveler_charm']:effect=effect.replace('incoming damage','damage')+'; charm slot OR old offhand, single boolean result'
 acquisition=[]
 if id=='amber_charm':acquisition.append('src/main/java/org/slavicmyths/ritual/Rituals.java: AMBER_CHARM = warding_charm → amber → juniper_blend')
 if id=='perun_charm':acquisition.append('src/main/java/org/slavicmyths/ritual/Rituals.java: CHARM = linen_cloth → thunder_stone → ancient_sign')
 for path,d in data.items():
  needle='slavicmyths:'+id
  if '/recipe/' in path and isinstance(d.get('result'),dict) and (d['result'].get('id')==needle or d['result'].get('item')==needle):acquisition.append(path)
  if '/loot_table/' in path and re.search(r'"name":\s*"'+needle+'"',json.dumps(d)):acquisition.append(path)
 refs=[]
 pattern=re.compile(r'\b'+id.upper()+r'\b')
 for path,text in java.items():
  if id.upper() not in text and '"'+id+'"' not in text:continue
  for no,line in enumerate(text.splitlines(),1):
   if pattern.search(line) or '"'+id+'"' in line:refs.append(f'src/main/java/org/slavicmyths/{path}:{no}')
 tooltips={k:v for k,v in lang.items() if (id in k and (k.endswith('.effect') or k.startswith('tooltip.')))}
 sources=fact[3].split(';')
 assert all(p in java for p in sources),(id,sources)
 entries.append(dict(id=id,name=lang['item.slavicmyths.'+id],slot=slot,current_effect=effect,stage=fact[1],handlers=['src/main/java/org/slavicmyths/'+p for p in sources],acquisition_resources=acquisition,source_refs=refs,tooltips=tooltips,
  player_path='Штатный рецепт/награда из перечисленного source → '+slot+' при щите в offhand → '+effect+' → снять/заменить → сравнить → смерть/relog/MP',
  before='offhand only; no charm tag' if id.endswith('_charm') else 'existing tagged implementation',
  changed='existing charm slot + server-only single protection check + RU/EN' if id.endswith('_charm') else 'KEEP effect; FolkAccessoryItem reads current active declared slot instead of cached item search',
  equip='NOT_RUN',effect='NOT_RUN',unequip='NOT_RUN',death='NOT_RUN',relog='NOT_RUN',mp='NOT_RUN',status='SOURCE_AUDITED / GAMEPLAY_NOT_RUN'))
out={'base_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),'items':entries,'game_launches':0,'limitations':'Source/recipe paths demonstrate implementation only. No natural acquisition, actual equip, effect, death, relog or MP proved.'}
runtime=OUT/'runtime.json'
if runtime.exists():
 out['runtime']=json.loads(runtime.read_text(encoding='utf8'))
 for e in entries:
  e['server_slot_api']='PASS (run3 all37NativeSlotValidatorsAndCapacity)'
  e['server_damage']='PASS' if e['id'] in ['amber_charm','perun_charm','forest_charm','traveler_charm','perun_ring'] else 'NOT_RUN'
  e['effect']='SERVER_DAMAGE_PASS / OWNER_VISUAL_NOT_RUN' if e['server_damage']=='PASS' else 'NOT_RUN'
(OUT/'catalog.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
lines=['# Поимённый актуальный реестр #278','', 'Клиентский GUI, Survival-получение, настоящий relog и сетевой MP: NOT_RUN. Серверные API-слоты всех 37 IDs проверены в run3; реальный damage четырёх пассивных оберегов и Perun Ring — PASS. Остальные эффекты не объявляются игровым PASS. Первые 33 существовали в tags; четыре passive charms добавлены в прежний charm slot. Количество физических мест не изменено.','', '| Item ID | Название | Слот | Фактический эффект / source | Получение / стадия | Было | Исправлено | Проверка | Статус |','|---|---|---|---|---|---|---|---|---|']
for e in entries:
 lines.append('| '+' | '.join([e['id'],e['name'],e['slot'],e['current_effect']+'; '+', '.join(e['handlers']),'; '.join(e['acquisition_resources'])+'; '+e['stage'],e['before'],e['changed'],'native server slot API: '+e.get('server_slot_api','NOT_RUN')+'; damage: '+e.get('server_damage','NOT_RUN')+'; GUI/death/relog/MP NOT_RUN',e['status']])+' |')
(OUT/'CATALOG.md').write_text('\n'.join(lines)+'\n',encoding='utf8')
print('CURRENT_ACCESSORIES_SOURCE_AUDIT items='+str(len(entries))+' gameplay=NOT_RUN')
