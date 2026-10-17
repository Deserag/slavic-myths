from pathlib import Path
import csv,gzip,json,math,statistics,sys
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-2.0'
def rows(p):
 if p.exists():return list(csv.DictReader(p.open(encoding='utf-8-sig')))
 with gzip.open(str(p)+'.gz','rt',encoding='utf-8-sig') as f:return list(csv.DictReader(f))
def stats(a):
 a=sorted(a)
 return {'n':len(a),'mean':statistics.mean(a),'p95':a[math.ceil(.95*len(a))-1],'p99':a[math.ceil(.99*len(a))-1],'max':max(a)} if a else None
result={}
for d in sorted(O.glob('cube-*')):
 if not (d/'events.txt').exists() or 'COMPLETE' not in (d/'events.txt').read_text(encoding='utf-8'):continue
 fs=rows(d/'frames.csv');ts=rows(d/'ticks.csv');phases={}
 for mode,lo,hi,cap in [('cap140',2410,4790,140),('uncapped',5410,6590,260)]:
  raw=[v for v in fs if v.get('age') and lo<int(v['age'])<=hi]
  valid=[v for v in raw if v.get('focused')=='true' and v.get('visible')=='1' and v.get('iconified')=='0' and v.get('paused')=='false' and v.get('screen')=='none' and v.get('width')=='1280' and v.get('height')=='720' and v.get('cap')==str(cap) and abs(float(v['camera_x']))<1e-5 and abs(float(v['camera_z'])+14)<1e-5 and abs(float(v['camera_y'])+58.38000011444092)<1e-5 and abs(float(v['yaw']))<1e-5 and abs(float(v['pitch'])-12)<1e-5]
  tick=[v for v in ts if lo<int(v['age'])<=hi]
  interval=stats([float(v['ms']) for v in valid]);cpu=[float(v['render_cpu_ms']) for v in valid]
  phases[mode]={'valid_fraction':len(valid)/len(raw) if raw else 0,'eligible':len(valid)/len(raw)>=.99 if raw else False,'raw_frames':len(raw),'frame':interval,'fps':1000/interval['mean'] if interval else None,'render_wall':stats([float(v['render_ms']) for v in valid]),'render_cpu_sum_ms':sum(cpu),'render_cpu_mean_ms':statistics.mean(cpu) if cpu else None,'render_cpu_counter_resolution_ms':15.625,'tick':stats([float(v['ms']) for v in tick]),'tick_intervals':stats([float(v['interval_ns'])/1e6 for v in tick]),'frames_over50':sum(float(v['ms'])>50 for v in valid),'frames_over100':sum(float(v['ms'])>100 for v in valid),'heap_min':min(int(v['heap']) for v in tick),'heap_max':max(int(v['heap']) for v in tick)}
 result[d.name]=phases
(O/'cube-metrics.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
for name,p in result.items():
 for mode,v in p.items():print(name,mode,'valid',round(v['valid_fraction'],4),'FPS',round(v['fps'],1) if v['fps'] else None,'p99',v['frame']['p99'] if v['frame'] else None,'CPU/frame',round(v['render_cpu_mean_ms'],4) if v['render_cpu_mean_ms'] else None)
