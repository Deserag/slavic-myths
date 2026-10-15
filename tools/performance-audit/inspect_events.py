from pathlib import Path
import re,json,collections,sys
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-audit';O=O/'extended2' if '--extended2' in sys.argv else O
s=(O/'raw/events.txt').read_text(encoding='utf-8');casts=re.findall(r'CAST (\w+) success=(\w+) age=(\d+)',s);boss=collections.defaultdict(lambda:dict(target_true=0,active_action=0,phases=set()))
for name,age,target,phase,action in re.findall(r'BOSS_STATE (\w+) age=(\d+) target=(\w+) phase=(-?\d+) action=(-?\d+)',s):
 if int(age)<=100:continue
 b=boss[name];b['target_true']+=target=='true';b['active_action']+=int(action)>0;b['phases'].add(int(phase))
for v in boss.values():v['phases']=sorted(v['phases'])
result=dict(complete='COMPLETE' in s,invalid='INVALID_FIXTURE' in s,casts=[dict(skill=a,success=b=='true',age=int(c)) for a,b,c in casts],boss_sampled_observations=dict(boss),rune_attack_targets_present=len(re.findall(r'RUNE_ATTACK .* target=true',s)),rune_attack_attempts=len(re.findall(r'RUNE_ATTACK ',s)),client_fps_range=[min(map(int,re.findall(r'CLIENT_FPS .* fps=(\d+)',s)),default=0),max(map(int,re.findall(r'CLIENT_FPS .* fps=(\d+)',s)),default=0)])
(O/'coverage.json').write_text(json.dumps(result,indent=2),encoding='utf-8');print(json.dumps(result))
