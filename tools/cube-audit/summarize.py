"""Analyse identical late windows. No client launch; frames are engine Post intervals."""
from pathlib import Path
import csv,gzip,json,math,statistics,re
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/cube-allocation'
def rows(p):
 if p.exists():return list(csv.DictReader(p.open(encoding='utf-8-sig')))
 with gzip.open(str(p)+'.gz','rt',encoding='utf-8-sig') as f:return list(csv.DictReader(f))
def stats(values):
 a=sorted(values);return dict(n=len(a),mean=statistics.mean(a),p95=a[math.ceil(.95*len(a))-1],p99=a[math.ceil(.99*len(a))-1],max=max(a))
result={}
for phase in ['before','after']:
 d=O/phase
 if not (d/'ticks.csv').exists():continue
 t=[r for r in rows(d/'ticks.csv') if 2400<int(r['age'])<=3900]
 f=[r for r in rows(d/'frames.csv') if 2400<int(r['age'])<=3900 and float(r['ms'])>0]
 if not t or not f:continue
 events=(d/'events.txt').read_text(encoding='utf-8')
 micro=[dict(round=int(a),kind=b,calls=int(c),ns=int(n),allocated_bytes=int(x)) for a,b,c,n,x in re.findall(r'MICRO round=(\d+) kind=(\w+) calls=(\d+) ns=(\d+) allocated_bytes=(\d+)',events)]
 m=stats([float(r['ms']) for r in t]);frame=stats([float(r['ms']) for r in f]);intervals=[float(r['interval_ns'])/1e6 for r in t if float(r['interval_ns'])>0]
 result[phase]=dict(server_tick=m,frame=frame,fps=1000/frame['mean'],tps=min(20,1000/statistics.mean(intervals)),over50_percent=sum(float(r['ms'])>50 for r in t)/len(t)*100,heap_min=min(int(r['heap']) for r in t),heap_max=max(int(r['heap']) for r in t),micro=micro,parity='VERTEX_PARITY PASS cases=1024' in events)
(O/'metrics.json').write_text(json.dumps(result,indent=2),encoding='utf-8',newline='\n')
lines=['# Сопоставимые поздние окна','', 'Ages2401–3900:75s sampling after120s scene warmup. Earlier raw data retained; concurrent fixture compilation in the early before window excluded in both runs. One independent JVM per side, no statistical claim from six in-process micro batches.','', '|Run|Tick avg /p95 /p99 ms|TPS|FPS|Frame p95 /p99 ms|Production allocation B/call median|','|---|---|---:|---:|---|---:|']
for phase,v in result.items():
 t=v['server_tick'];f=v['frame'];alloc=statistics.median(r['allocated_bytes']/r['calls'] for r in v['micro'] if r['kind']=='production');lines.append(f"|{phase}|{t['mean']:.3f} /{t['p95']:.3f} /{t['p99']:.3f}|{v['tps']:.2f}|{v['fps']:.1f}|{f['p95']:.3f} /{f['p99']:.3f}|{alloc:.3f}|")
(O/'METRICS.md').write_text('\n'.join(lines)+'\n',encoding='utf-8',newline='\n')
print(json.dumps({k:{'tick':v['server_tick'],'frame':v['frame'],'fps':v['fps'],'tps':v['tps']} for k,v in result.items()},indent=2))
