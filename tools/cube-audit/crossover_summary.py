from pathlib import Path
import csv,gzip,json,math,statistics
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/cube-allocation';D=O/'crossover'
def rows(p):
 if p.exists():return list(csv.DictReader(p.open(encoding='utf-8-sig')))
 with gzip.open(str(p)+'.gz','rt',encoding='utf-8-sig') as f:return list(csv.DictReader(f))
def stats(a):
 a=sorted(a);return dict(n=len(a),mean=statistics.mean(a),p95=a[math.ceil(.95*len(a))-1],p99=a[math.ceil(.99*len(a))-1],max=max(a))
frames=rows(D/'frames.csv');ticks=rows(D/'ticks.csv');modes=['reference','helper','constant','constant','helper','reference'];result=[]
for i,mode in enumerate(modes):
 if i==1:continue # whole block invalid: integrated pause/minimized window
 start=1800+i*900;end=start+900
 f=[float(r['ms']) for r in frames if start+100<int(r['age'])<=end and float(r['ms'])>0];t=[float(r['ms']) for r in ticks if start+100<int(r['age'])<=end]
 if not f or not t:continue
 fs=stats(f);ts=stats(t);result.append(dict(block=i,mode=mode,age_window=[start+101,end],frame=fs,tick=ts,fps=1000/fs['mean']))
(O/'crossover-metrics.json').write_bytes(json.dumps(result,indent=2).encode())
lines=['# Within-client crossover','', 'Order reference/helper/constant/constant/helper/reference after90s constant warmup;45s blocks, first5s excluded. Block1 excluded entirely after integrated pause/minimized window. Block0 precedes the interruption and is not used for post-resume inference. Window visibility was not recorded continuously. Blocks2–5 are the post-resume controls; one JVM, no independent repetition claim. Test-only mixin routing is common instrumentation, not a production feature.','', '|Block|Method|Frames|FPS|Frame p95/p99 ms|Tick avg/p99 ms|','|---|---|---:|---:|---|---|']
for v in result:
 f=v['frame'];t=v['tick'];lines.append(f"|{v['block']}|{v['mode']}|{f['n']}|{v['fps']:.1f}|{f['p95']:.3f}/{f['p99']:.3f}|{t['mean']:.3f}/{t['p99']:.3f}|")
(O/'CROSSOVER.md').write_bytes(('\n'.join(lines)+'\n').encode());print('\n'.join(lines))
