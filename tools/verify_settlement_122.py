"""Focused settlement data/production gate. No client launch."""
import argparse,json,hashlib,zipfile
from pathlib import Path
import settlement_resources, military_resources
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);args=p.parse_args();checks=0
def check(v,msg):
 global checks;checks+=1
 if not v:raise AssertionError(msg)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
capacity=(ROOT/'src/main/java/org/slavicmyths/village/WorkLimits.java').read_text(encoding='utf-8');check('STORAGE_SLOTS=64'in capacity,'Centralized 64 slots')
recipe=read(RES/'data/slavicmyths/recipe/settlement_chest.json');check(recipe['pattern']==['PPP','PCP','PPP'] and recipe['key']['P']['item']=='minecraft:dark_oak_planks','Chest recipe')
pois=read(RES/'data/minecraft/tags/point_of_interest_type/acquirable_job_site.json');check('slavicmyths:settlement_storage'not in pois['values'],'Storage cannot assign a profession')
for role in ['miller','weaver']:
 outputs=set(read(RES/f'data/slavicmyths/tags/item/villager_outputs/{role}.json')['values'])
 sold=set(read(RES/f'data/slavicmyths/tags/item/villager_trade_outputs/{role}.json')['values']);check(not outputs&sold,'Critical logistics vs sell outputs '+role)
for role in ['miller','brewer','weaver','herder','hunter','cook','farmer','fisherman','shepherd']:
 for group in ['inputs','outputs','trade_outputs']:
  values=read(RES/f'data/slavicmyths/tags/item/villager_{group}/{role}.json')['values'];check(bool(values),'Empty policy '+role+'/'+group)
for p in (RES/'data/slavicmyths/village_trades').glob('*.json'):
 sold={v['output']for rows in read(p).values()for v in rows if v['input']=='minecraft:emerald'}
 check(sold==set(read(RES/f'data/slavicmyths/tags/item/villager_trade_outputs/{p.stem}.json')['values']),'Deny tag matches all five levels '+p.stem)
snapshot={p:hashlib.sha256(p.read_bytes()).hexdigest()for p in RES.rglob('*')if p.is_file()};settlement_resources.main();military_resources.main();after={p:hashlib.sha256(p.read_bytes()).hexdigest()for p in RES.rglob('*')if p.is_file()};check(snapshot==after,'Generator byte reproducibility across all resources')
for p in (ROOT/'src/main/java/org/slavicmyths/village').rglob('*.java'):check('net.minecraft.client.'not in p.read_text(encoding='utf-8'),'Common side isolation '+p.name)
if args.jar:
 with zipfile.ZipFile(args.jar)as z:
  check(z.read('slavicmyths.work.mixins.json')==(RES/'slavicmyths.work.mixins.json').read_bytes(),'Production mixin configuration')
  for p in (ROOT/'build/classes/java/main').rglob('*.class'):check(z.read(p.relative_to(ROOT/'build/classes/java/main').as_posix())==p.read_bytes(),'Production class bytes '+p.name)
  check(not any('/verify/'in n or 'SettlementGameTests' in n for n in z.namelist()),'Tests excluded')
report={'version':'1.2.2','checks':checks,'jar':str(args.jar)if args.jar else None,'client_launches':0};(ROOT/'work/settlement-1.2.2-resource-checks.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
