"""Current named source/acquisition index; registry stats are supplied by native server evidence."""
from pathlib import Path
import json,re
R=Path(__file__).resolve().parents[1];OUT=R/'docs/implementation/armor287-20261026';RES=R/'src/main/resources'
OUT.mkdir(parents=True,exist_ok=True)
ids=[f[0] for line in (RES/'assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf8').splitlines() if line and not line.startswith('#') for f in [line.split('\t')] if f[1]=='armor'];assert len(ids)==34
java={p.relative_to(R).as_posix():p.read_text(encoding='utf8') for p in (R/'src/main/java').rglob('*.java')}
data={p.relative_to(R).as_posix():json.loads(p.read_text(encoding='utf8')) for p in (RES/'data/slavicmyths').rglob('*.json') if '/recipe/' in p.as_posix() or '/loot_table/' in p.as_posix()}
lang=json.loads((RES/'assets/slavicmyths/lang/ru_ru.json').read_text(encoding='utf8'));rows=[]
for id in ids:
 needle='slavicmyths:'+id;rx=re.compile(r'\b'+id.upper()+r'\b');refs=[];recipes=[];loot=[]
 for file,s in java.items():
  if id.upper() not in s and '"'+id+'"' not in s:continue
  for n,line in enumerate(s.splitlines(),1):
   if rx.search(line) or '"'+id+'"' in line:refs.append({'path':file,'line':n,'text':line.strip()})
 for file,d in data.items():
  result=d.get('result',{})
  if '/recipe/' in file and (result==needle or isinstance(result,dict) and (result.get('id')==needle or result.get('item')==needle)):recipes.append(file)
  if '/loot_table/' in file and re.search(r'"name":\s*"'+needle+'"',json.dumps(d)):loot.append(file)
 role='shield' if id.endswith('shield') else 'belt attachment' if id=='woven_belt' else 'zero-armor clothing' if id in ['linen_headscarf','linen_shirt','linen_ports','bast_shoes','sarafan','linen_apron','folk_vest'] else 'ArmorItem'
 dynamic='poloz/PolozRewardEvents.smith: netherite donor +2 scales at smithing table' if id.startswith('poloz_') else 'gorynych/GorynychUpgrade: perunite donor +scale +2perunite +2diamond' if id.startswith('dragon_') else 'rewards/RewardLedger: source-specific victory receipts' if id.startswith('trophy_') or id=='gift_ilya_shield' else ''
 rows.append(dict(id=id,name=lang.get('item.slavicmyths.'+id),role=role,recipes=recipes,loot=loot,dynamic=dynamic,source_refs=refs,descriptions={k:v for k,v in lang.items() if id in k and any(k.endswith(x) for x in ['.effect','.desc','.detail','.base','.extra'])},player_path='Крафт/персональная награда/улучшение из указанных sources → надеть по настоящему слоту → сравнить защиту/эффект → снять → повторить → смерть/измерение/перезаход/MP',decision='KEEP values/art; fix only reproduced functional defects',gameplay='native evidence file; natural route/client/real relog/MP NOT_RUN'))
native=OUT/'native.json'
if native.exists():
 stats=json.loads(native.read_text(encoding='utf8'));index={row['id']:row for row in stats['items']}
 for row in rows:row['native']=index.get(row['id'],{})
(OUT/'catalog.json').write_text(json.dumps({'base_commit':'edc915302243e481389a696727c2936d6ce030b3','items':rows},ensure_ascii=False,indent=2)+'\n',encoding='utf8')
lines=['# #287: фактический каталог34','', '| ID | Название | Registry роль | Native CURRENT | Получение | EXPECTED / решение | EVIDENCE |','|---|---|---|---|---|---|---|']
for row in rows:
 n=row.get('native',{});equipped=n.get('equipped',{});current='slot='+str(n.get('slot'))+'; armor='+str(equipped.get('generic.armor'))+'; toughness='+str(equipped.get('generic.armor_toughness'))+'; KB='+str(equipped.get('generic.knockback_resistance'))+'; durability='+str(n.get('durability'))+'; enchant='+str(n.get('enchantability'))+'; rune capacity='+str(n.get('rune_capacity'))
 lines.append('| '+' | '.join([row['id'],row['name'],row['role'],current,'; '.join(row['recipes']+row['loot']+[row['dynamic']]),row['decision'],'catalog.json source_refs; native.json before/equipped/removed; logs. Natural acquisition/relog/MP NOT_RUN'])+' |')
(OUT/'CATALOG.md').write_text('\n'.join(lines)+'\n',encoding='utf8');print('ARMOR287_SOURCE ids=34 ArmorItem22 clothing7 shield4 belt1; runtime separate')
