import json
from pathlib import Path
worst=[(1,None)]*3
for f in Path('build/reports/slavicmyths/kurgan').glob('*.json'):
 p=json.loads(f.read_text());total=side=0
 for r in p['rooms']:
  value=r['rewardPoints']*({'treasury':5.5,'secret':6,'great_special':5,'burial':3.5,'warrior':3.5,'ritual':3.5}.get(r['loot'],3))
  total+=value
  if not r['critical']:side+=value
 rate=side/total
 if rate<worst[p['tier']][0]:worst[p['tier']]=(rate,str(f.name))
print('worst side reward expected shares',worst)
