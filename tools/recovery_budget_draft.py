"""Executable review model for the agreed rule; NOT integrated into Minecraft.
Two recovery cycles per battle, third exhaustion withdraws, reset only on end.
"""
from dataclasses import dataclass,asdict
from pathlib import Path
import json

@dataclass
class Budget:
 battle:str=''
 recoveries:int=0
 state:str='active'
 def start(self,battle):
  if self.battle and self.battle!=battle:raise ValueError('Finish the existing battle before resetting')
  self.battle=battle
 def exhausted(self):
  if self.state!='active':return self.state
  if self.recoveries==2:self.state='withdrawn'
  else:self.recoveries+=1;self.state='recovering'
  return self.state
 def healed(self):
  if self.state=='recovering':self.state='active'
 def finish(self):
  self.battle='';self.recoveries=0
  self.state='absent' if self.state=='withdrawn' else 'active'
 def summon(self):
  if self.battle:return False
  self.state='active';return True

checks=0
def check(value):
 global checks
 checks+=1;assert value
trace=[]
for hero in ['ilya_muromets','dobrynya_nikitich','alyosha_popovich','finist']:
 b=Budget();b.start('battle-1')
 for cycle in [1,2]:
  check(b.exhausted()=='recovering');check(b.recoveries==cycle)
  # Healing/duplicate low-HP observations cannot count as another recovery.
  for _ in range(40):check(b.exhausted()=='recovering' and b.recoveries==cycle)
  b.healed();check(b.state=='active' and b.recoveries==cycle)
 # Save/load snapshot and recall cannot restore a recovery budget mid-battle.
 b=Budget(**json.loads(json.dumps(asdict(b))));check(b.recoveries==2)
 b.start('battle-1');check(b.recoveries==2)
 check(b.exhausted()=='withdrawn');check(not b.summon())
 b.healed();check(b.state=='withdrawn')
 trace.append(dict(hero=hero,before_end=asdict(b)))
 b.finish();check(b.recoveries==0 and b.state=='absent');check(b.summon());b.start('battle-2');check(b.exhausted()=='recovering' and b.recoveries==1)
 trace[-1]['next_battle']=asdict(b)
O=Path(__file__).resolve().parents[1]/'docs/design-review/1.8.0-proposal/revision-2'
(O/'recovery-rule-checks.json').write_text(json.dumps(dict(status='DRAFT_MODEL_PASS',checks=checks,production_integration='NOT_IMPLEMENTED',minecraft_launches=0,trace=trace),ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('DRAFT_RECOVERY_MODEL_PASS checks='+str(checks)+' production integration NOT_IMPLEMENTED')
