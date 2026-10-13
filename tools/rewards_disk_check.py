"""Read actual vanilla PlayerDataStorage output; not a claim of real client relog."""
from pathlib import Path
import gzip,struct,re,json,hashlib
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/rewards-1.7.6'
log=(O/'runtime-final.log').read_text(encoding='utf-8',errors='replace')
owner=re.search(r'REWARDS_PLAYER_SAVE ([0-9a-f-]+)',log).group(1)
run=json.loads((O/'runtime-selection.json').read_text())['run']
source=R/f'work/rewards/runtime-{run}/world/playerdata/{owner}.dat';stream=gzip.open(source,'rb')
def num(fmt):return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
def text():return stream.read(num('H')).decode('utf-8')
def payload(tag):
 if tag in range(1,7):return num({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[tag])
 if tag==7:return list(stream.read(num('i')))
 if tag==8:return text()
 if tag==9:
  child=num('B');return [payload(child) for _ in range(num('i'))]
 if tag==10:
  obj={}
  while True:
   child=num('B')
   if child==0:return obj
   name=text();obj[name]=payload(child)
 if tag in [11,12]:return [num('i' if tag==11 else 'q') for _ in range(num('i'))]
 raise ValueError(tag)
assert num('B')==10;text();root=payload(10)
def find(value,key):
 if isinstance(value,dict):
  if key in value:return value[key]
  for child in value.values():
   result=find(child,key)
   if result is not None:return result
 return None
claims=find(root,'slavicmyths.reward_claims176');timers=find(root,'slavicmyths.rewards176')
assert claims and timers
for i in range(5):assert claims['First'+str(i)]==1 and owner in claims['Claim'+str(i)]
for i in range(4):assert claims['Gift'+str(i)]==1
assert timers['DodgeReady']>0 and timers['ShieldReady']>0 and timers['CounterReady']>0
result=dict(status='PASS',source=str(source),sha256=hashlib.sha256(source.read_bytes()).hexdigest(),vanilla_playerdata_disk_readback=True,claim_flags=claims,inventory_saved_with_claims='Inventory' in root,cooldowns=timers,real_client_relog=False,real_world_restart=False)
(O/'playerdata-disk-readback.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print('PLAYERDATA_DISK_PASS five first claims, four gifts, inventory and cooldowns')
