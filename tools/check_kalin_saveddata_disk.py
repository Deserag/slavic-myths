"""Read back actual dedicated GameTest SavedData file. Not a player relog/world restart."""
from pathlib import Path
import gzip,struct,json,hashlib
R=Path(__file__).resolve().parents[1];source=R/'work/kalin/runtime-17/world/data/slavicmyths_kalin_175.dat';stream=gzip.open(source,'rb')
def num(fmt):return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
def text():return stream.read(num('H')).decode('utf8')
def payload(tag):
 if tag in range(1,7):return num({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[tag])
 if tag==7:return stream.read(num('i'))
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
assert num('B')==10;text();root=payload(10);data=root['data'];history=data['Progress'];assert len(history)==1
owner,flags=next(iter(history.items()));assert flags['kalin_battle_started']==1 and flags['kalin_battle_won']==1 and flags['times_kalin_defeated']==1 and flags['pending_reward_1_7_6']==1
assert flags['selected_hero_a']=='ilya_muromets' and flags['selected_hero_b']=='finist';assert data['Rollback']==[]
result=dict(status='PASS',source=str(source),sha256=hashlib.sha256(source.read_bytes()).hexdigest(),actual_saveddata_disk_readback=True,flags=flags,rollback_reservations=0,real_player_relog=False,real_world_restart=False)
(R/'docs/verification/kalin-1.7.5/saveddata-disk-readback.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
