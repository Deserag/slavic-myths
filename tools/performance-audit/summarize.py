import sys
from pathlib import Path
import csv,json,math,statistics,collections,gzip
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-audit';O=O/('focused' if '--focused' in sys.argv else 'extended2' if '--extended2' in sys.argv else 'extended') if any(x in sys.argv for x in ['--extended','--extended2','--focused']) else O;raw=O/'raw'
def stats(v):
 v=sorted(v);n=len(v)
 return {'n':n,'mean':statistics.mean(v),'p95':v[max(0,math.ceil(n*.95)-1)],'p99':v[max(0,math.ceil(n*.99)-1)],'max':v[-1]}
t=collections.defaultdict(list);f=collections.defaultdict(list)
for row in csv.DictReader((raw/'ticks.csv').open()):t[(row['scene'],int(row['repeat']))].append(row)
frame_source=(raw/'frames.csv').open() if (raw/'frames.csv').exists() else gzip.open(raw/'frames.csv.gz','rt')
for row in csv.DictReader(frame_source):f[(row['scene'],int(row['repeat']))].append(float(row['frame_ms']))
out=[]
for key,rows in t.items():
 ms=[float(x['ms']) for x in rows];fr=f[key];interval=[int(x['interval_ns'])/1e6 for x in rows]
 extra={}
 if 'new_chunk_load_events' in rows[0]:
  seconds=sum(int(x['interval_ns']) for x in rows)/1e9
  new=int(rows[-1]['new_chunk_load_events'])-int(rows[0]['new_chunk_load_events']);loads=int(rows[-1]['chunk_load_events'])-int(rows[0]['chunk_load_events'])
  extra=dict(observed_seconds=seconds,new_chunk_load_events=new,chunk_load_events=loads,new_chunk_load_events_per_second=new/seconds)
 out.append(dict(**extra,scene=key[0],repeat=key[1],tick_ms=stats(ms),tick_interval_ms=stats(interval),tps_observed=min(20,1000/statistics.mean(interval)),over50_pct=100*sum(x>50 for x in ms)/len(ms),frame_ms=stats(fr) if fr else None,fps=1000/statistics.mean(fr) if fr else None,heap_min=min(int(x['heap_bytes']) for x in rows),heap_max=max(int(x['heap_bytes']) for x in rows),entities_min=min(int(x['entities']) for x in rows),entities_max=max(int(x['entities']) for x in rows),chunks_min=min(int(x['loaded_chunks']) for x in rows),chunks_max=max(int(x['loaded_chunks']) for x in rows),gc_count_delta=int(rows[-1]['gc_count'])-int(rows[0]['gc_count']),gc_ms_delta=int(rows[-1]['gc_ms'])-int(rows[0]['gc_ms'])))
(O/'metrics.json').write_text(json.dumps(out,indent=2),encoding='utf-8')
lines=['|Scenario/sample|Ticks|MSPT avg / p95 / p99 / max|>50ms %|TPS|FPS|frame p95 / p99 ms|heap MiB min–max|entities|','|---|---:|---|---:|---:|---:|---|---|---|']
for x in out:
 a=x['tick_ms'];b=x['frame_ms'];lines.append(f"|{x['scene']}/{x['repeat']}|{a['n']}|{a['mean']:.2f} / {a['p95']:.2f} / {a['p99']:.2f} / {a['max']:.2f}|{x['over50_pct']:.2f}|{x['tps_observed']:.2f}|{x['fps']:.1f}|{b['p95']:.2f} / {b['p99']:.2f}|{x['heap_min']/2**20:.0f}–{x['heap_max']/2**20:.0f}|{x['entities_min']}–{x['entities_max']}|")
(O/'METRICS.md').write_text('\n'.join(lines)+'\n',encoding='utf-8');print('\n'.join(lines))
