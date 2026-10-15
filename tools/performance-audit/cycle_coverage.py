from pathlib import Path
import csv,json,math,statistics,collections
O=Path('docs/implementation/performance-audit/focused');combat={(r['scene'],int(r['age'])):r for r in csv.DictReader((O/'raw/combat.csv').open())};ticks=collections.defaultdict(list);states=collections.defaultdict(lambda:dict(phases=set(),alive_target_ticks=0,alive_action_ticks=0,alive_ticks=0))
for (scene,age),r in combat.items():
 s=states[scene];s['phases'].add(int(r['phase']))
 if r['alive']=='true' and r['removed']=='false':s['alive_ticks']+=1;s['alive_target_ticks']+=r['target_player']=='true';s['alive_action_ticks']+=int(r['action'])>0
for r in csv.DictReader((O/'raw/ticks.csv').open()):
 c=combat.get((r['scene'],int(r['age'])))
 if not c or c['alive']!='true' or c['removed']=='true':continue
 phase=int(c['phase']);fight=phase in ([0,1,2,3] if r['scene']!='gorynych_cycle' else [0,1,2,3])
 if fight:ticks[r['scene']].append(float(r['ms']))
result={}
for name,ss in states.items():
 ss['phases']=sorted(ss['phases']);v=sorted(ticks[name]);ss['sampled_fighting_ticks']=len(v)
 if v:ss['fighting_tick_ms']=dict(mean=statistics.mean(v),p95=v[math.ceil(.95*len(v))-1],p99=v[math.ceil(.99*len(v))-1],max=max(v))
 result[name]=ss
(O/'cycle-coverage.json').write_text(json.dumps(result,indent=2),encoding='utf-8');print(json.dumps(result))
