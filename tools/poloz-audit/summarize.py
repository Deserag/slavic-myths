"""Counter differences are rates over observed endpoints, not generated packets/draw calls."""
from pathlib import Path
import csv,gzip,json,statistics,math,collections
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/poloz-pressure';D=O/'native'
def rows(path):
 if path.exists():return list(csv.reader(path.open(encoding='utf-8-sig')))[1:]
 with gzip.open(str(path)+'.gz','rt',encoding='utf-8-sig') as f:return list(csv.reader(f))[1:]
def stats(a):
 a=sorted(a);return dict(n=len(a),mean=statistics.mean(a),p95=a[math.ceil(.95*len(a))-1],p99=a[math.ceil(.99*len(a))-1],max=max(a))
observations=rows(D/'observations.csv');ticks=rows(D/'ticks.csv');frames=rows(D/'frames.csv');combat=rows(D/'combat.csv');result={}
for scene in dict.fromkeys(r[0] for r in observations):
 obs=[r for r in observations if r[0]==scene];eng={int(r[1]):[int(x) for x in r[4:]] for r in obs if r[2]=='engine'}
 ages=sorted(a for a in eng if a>=300)
 if len(ages)<2:continue
 start,end=ages[0],ages[-1];seconds=(end-start)/20
 by=collections.defaultdict(dict)
 for r in obs:by[(r[2],r[3])][int(r[1])]=[int(x) for x in r[4:]]
 particles=[];sounds=[]
 for (kind,key),values in by.items():
  if kind=='engine':continue
  before=values.get(start,[0,0,0,0]);after=values.get(end,next(reversed(values.values())));peak=max(v[2] for a,v in values.items() if start<=a<=end) if any(start<=a<=end for a in values) else 0
  row=dict(key=key,created_or_play_requests_per_second=(after[0]-before[0])/seconds,peak_tracked=peak,created_total=after[0],removed_or_explicit_stop_total=after[1],last_tracked=after[2])
  if kind=='particle':row['observed_residence_mean_ticks']=after[3]/after[1] if after[1] else None;particles.append(row)
  else:row['native_channel_removals_total']=after[3];sounds.append(row)
 t=[r for r in ticks if r[0]==scene];f=[r for r in frames if r[0]==scene and float(r[2])>0];reset=[i for i in range(1,len(f)) if int(f[i][1])<int(f[i-1][1])];f=f[max(reset):] if reset else f;b=[r for r in combat if r[0]==scene and r[2]=='true'];ts=stats([float(r[2]) for r in t]);fs=stats([float(r[2]) for r in f]);ivs=[float(r[3])/1e6 for r in t if float(r[3])>0]
 result[scene]=dict(observed_rate_window_ticks=[start,end],seconds=seconds,tick=ts,frame=fs,fps=1000/fs['mean'],tps=min(20,1000/statistics.mean(ivs)),peak_particles_all=max(v[0] for v in eng.values()),peak_channels_all=max(v[1] for v in eng.values()),pool_warning_difference=eng[end][2]-eng[start][2],handle_failure_difference=eng[end][3]-eng[start][3],engine_last=eng[end],phases=sorted(set(int(r[3]) for r in b)),alive_action_ticks=sum(int(r[4])>0 for r in b),alive_target_ticks=sum(r[6]!='none' for r in b),particles=sorted(particles,key=lambda x:x['created_total'],reverse=True),sounds=sorted(sounds,key=lambda x:x['created_total'],reverse=True))
(O/'metrics.json').write_text(json.dumps(result,indent=2),encoding='utf-8',newline='\n')
lines=['# Native A/B/C/D observations','', 'One JVM, sequential scenes, one occurrence per scene. Rates: cumulative counter differences over observed endpoints after300 warmup ticks. Engine peaks below include warmup. Tick/frame windows: age>300; native scripted damage does not equal Survival/DPS acceptance.','', '|Scene|Tick avg /p95 /p99 ms|TPS|FPS|Frame p95 /p99 ms|Tracked particles peak|Channels peak|Handle failures in rate window|','|---|---|---:|---:|---|---:|---:|---:|']
for name,v in result.items():
 t=v['tick'];f=v['frame'];lines.append(f"|{name}|{t['mean']:.2f} /{t['p95']:.2f} /{t['p99']:.2f}|{v['tps']:.2f}|{v['fps']:.1f}|{f['p95']:.2f} /{f['p99']:.2f}|{v['peak_particles_all']}|{v['peak_channels_all']}|{v['handle_failure_difference']}|")
 lines+=[]
lines+=['','Top particle/sound types, calls/s, residence, native channel removal and active phase counts are in metrics.json. Queued/tracked sounds differ from physically playing OpenAL sources; particles tracked differ from rendered surfaces.']
(O/'METRICS.md').write_text('\n'.join(lines)+'\n',encoding='utf-8',newline='\n');print('\n'.join(lines))
