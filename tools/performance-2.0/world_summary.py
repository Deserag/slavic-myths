from pathlib import Path
import csv,gzip,json,math,statistics,collections
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-2.0'
input_quality={}
def rows(p):
 trailing=0
 def complete_lines(source):
  nonlocal trailing
  for line in source:
   if not line.endswith('\n'):trailing+=1;continue
   yield line
 if p.exists():
  with p.open(encoding='utf-8-sig') as f:raw=list(csv.DictReader(complete_lines(f)))
 else:
  with gzip.open(str(p)+'.gz','rt',encoding='utf-8-sig') as f:raw=list(csv.DictReader(complete_lines(f)))
 # A crashed JVM may leave a final partial writer buffer; preserve raw bytes, count exclusion.
 complete=[v for v in raw if None not in v and all(value is not None and value!='' for value in v.values())]
 input_quality[p.relative_to(O).as_posix()]={'read_rows':len(raw)+trailing,'complete_rows':len(complete),'incomplete_rows_excluded':len(raw)-len(complete)+trailing,'unterminated_physical_lines':trailing}
 return complete
def stats(v):
 a=sorted(v)
 return dict(n=len(a),mean=statistics.mean(a),p95=a[math.ceil(.95*len(a))-1],p99=a[math.ceil(.99*len(a))-1],max=max(a)) if a else None

def frame_valid(v):
 return v.get('focused')=='true' and v.get('visible')=='1' and v.get('iconified')=='0' and v.get('paused')=='false' and v.get('screen')=='none' and v.get('width')=='1600' and v.get('height')=='900' and v.get('cap')=='140'

def distance(t):
 return sum(math.sqrt(sum((float(a[k])-float(b[k]))**2 for k in ['player_x','player_y','player_z'])) for a,b in zip(t,t[1:]))

def horizontal_distance(t):
 return sum(math.hypot(float(a['player_x'])-float(b['player_x']),float(a['player_z'])-float(b['player_z'])) for a,b in zip(t,t[1:]))
result={}
for d in sorted(O.glob('world-*')):
 if not (d/'ticks.csv').exists():continue
 ticks=rows(d/'ticks.csv');frames=rows(d/'frames.csv');chunk_rows=rows(d/'chunks.csv');events=(d/'events.txt').read_text(encoding='utf-8');scenes={}
 boss_trace=rows(d/'boss-ticks.csv') if (d/'boss-ticks.csv').exists() else []
 for scene in dict.fromkeys(t['scene'] for t in ticks):
  t=[v for v in ticks if v['scene']==scene and int(v['age'])>600]
  f=[v for v in frames if v.get('scene')==scene and v.get('age') and int(v['age'])>600]
  if not t or not f:continue
  valid=[v for v in f if frame_valid(v)]
  frame=stats([float(v['ms']) for v in valid]);native=stats([float(v['native_mspt']) for v in t]);body=stats([float(v['ms']) for v in t]);interval=stats([int(v['interval_ns'])/1e6 for v in t if int(v['interval_ns'])>0]);path=0
  for a,b in zip(t,t[1:]):path+=math.sqrt(sum((float(a[k])-float(b[k]))**2 for k in ['player_x','player_y','player_z']))
  elapsed=(int(t[-1]['epoch_ms'])-int(t[0]['epoch_ms']))/1000
  scenes[scene]=dict(window_valid_fraction=len(valid)/len(f),raw_frames=len(f),frame=frame,fps=1000/frame['mean'] if frame else None,render_wall=stats([float(v['render_ms']) for v in valid]),render_cpu_mean_ms=statistics.mean(float(v['render_cpu_ms']) for v in valid) if valid else None,frames_over50=sum(float(v['ms'])>50 for v in valid),frames_over100=sum(float(v['ms'])>100 for v in valid),native_mspt=native,event_tick_body=body,tps=min(20,1000/interval['mean']),tick_intervals=interval,native_ticks_over50=sum(float(v['native_mspt'])>50 for v in t),heap_min=min(int(v['heap']) for v in t),heap_max=max(int(v['heap']) for v in t),new_chunk_events=int(t[-1]['new_chunk_events'])-int(t[0]['new_chunk_events']),old_chunk_events=int(t[-1]['old_chunk_events'])-int(t[0]['old_chunk_events']),path3d_blocks=path,start_position=[float(t[0][k]) for k in ['player_x','player_y','player_z']],end_position=[float(t[-1][k]) for k in ['player_x','player_y','player_z']],server_cpu_fraction_one_core=(int(t[-1]['server_cpu_ns'])-int(t[0]['server_cpu_ns']))/1e9/elapsed if elapsed>0 else None,server_entities_range=[min(int(v['entities']) for v in t),max(int(v['entities']) for v in t)])
  scenes[scene]['eligible_for_frame_comparison']=len(valid)/len(f)>=.99
  scenes[scene]['scene_complete']=('END '+scene+' epoch=') in events
  if scene=='reload_idle':scenes[scene]['scene_complete']&=('RELOAD_VERIFY new_server=true' in events and 'marker=saved-native-world' in events)
  scenes[scene]['last_recorded_tick_age']=int(t[-1]['age'])
  scenes[scene]['steady_window_elapsed_seconds']=elapsed
  scenes[scene]['path_horizontal_blocks']=horizontal_distance(t)
  scenes[scene]['net_horizontal_displacement']=math.hypot(float(t[-1]['player_x'])-float(t[0]['player_x']),float(t[-1]['player_z'])-float(t[0]['player_z']))
  all_ticks=[v for v in ticks if v['scene']==scene]
  all_frames=[v for v in frames if v.get('scene')==scene and v.get('age')]
  all_valid=[v for v in all_frames if frame_valid(v)]
  all_chunks=[v for v in chunk_rows if v['scene']==scene]
  trace=[v for v in boss_trace if v['scene']==scene]
  if trace:
   engaged={int(v['age']) for v in trace if v['alive']=='true' and v['fighting']=='true' and (v['target_player']=='true' or any(int(v[k])>0 for k in ['action','head0','head1','head2']))}
   active_frames=[v for v in all_frames if int(v['age'])>10 and int(v['age']) in engaged and frame_valid(v)]
   active_ticks=[v for v in all_ticks if int(v['age'])>10 and int(v['age']) in engaged]
   active_frame=stats([float(v['ms']) for v in active_frames])
   scenes[scene]['native_boss_trace']={
    'scope':'Server-state-labelled windows, one-tick/packet-interpolation boundary uncertainty; includes early fight after age10. Additional observer only in follow-up.',
    'tick_observations':len(trace),'engaged_tick_observations':len(engaged),
    'states':sorted({int(v['state']) for v in trace}),'actions':sorted({int(v['action']) for v in trace}),
    'head_actions':{key:sorted({int(v[key]) for v in trace}) for key in ['head0','head1','head2']},
    'target_player_ticks':sum(v['target_player']=='true' for v in trace),
    'logical_hp_min':min(float(v['logical_hp']) for v in trace),'logical_hp_max':max(float(v['logical_hp']) for v in trace),
    'frame':active_frame,'fps':1000/active_frame['mean'] if active_frame else None,
    'native_mspt':stats([float(v['native_mspt']) for v in active_ticks])}
  scenes[scene]['full_phase_including_setup']={
   'scope':'Includes first30s, screenshots and any locate/teleport setup; not steady-state gameplay latency',
   'native_mspt':stats([float(v['native_mspt']) for v in all_ticks]),
   'valid_frame':stats([float(v['ms']) for v in all_valid]),
   'raw_frame':stats([float(v['ms']) for v in all_frames]),
   'new_chunk_events':sum(v['new_chunk']=='true' for v in all_chunks),
   'old_chunk_events':sum(v['new_chunk']=='false' for v in all_chunks),
   'distinct_new_chunk_coordinates':len({(v['dimension'],v['x'],v['z']) for v in all_chunks if v['new_chunk']=='true'}),
   'path3d_blocks':distance(all_ticks),
   'path_horizontal_blocks':horizontal_distance(all_ticks),
   'net_horizontal_displacement':math.hypot(float(all_ticks[-1]['player_x'])-float(all_ticks[0]['player_x']),float(all_ticks[-1]['player_z'])-float(all_ticks[0]['player_z']))}
  gpu=d/'gpu.csv'
  if gpu.exists():
   engines=collections.defaultdict(list)
   with gpu.open(encoding='utf-8-sig') as source:
    for sample in csv.DictReader(line for line in source if not line.startswith('#')):
     if sample.get('epoch_ms') and int(t[0]['epoch_ms'])<=int(sample['epoch_ms'])<=int(t[-1]['epoch_ms']):engines[sample['engine']].append(float(sample['utilization_percent']))
   scenes[scene]['gpu_3d_engine_utilization_percent']={engine:stats(values) for engine,values in engines.items()}
   scenes[scene]['gpu_time']='NOT_MEASURED; utilization counter is not GPU frame time'
  process=d/'gpu.csv.process.csv'
  if process.exists():
   samples=[v for v in rows(process) if int(t[0]['epoch_ms'])<=int(v['epoch_ms'])<=int(t[-1]['epoch_ms'])]
   if samples:scenes[scene]['process_memory_bytes']={key:stats([int(v[key]) for v in samples]) for key in ['working_set','private_bytes']}
 result[d.name]=dict(complete='COMPLETE elapsed_ms=' in events,invalid='INVALID_PLAYER' in events,forced_stop_fixture=(d/'forced-stop.json').exists(),reload_new_server_marker='RELOAD_VERIFY new_server=true' in events and 'marker=saved-native-world' in events,scenes=scenes)
(O/'world-metrics.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
(O/'world-input-quality.json').write_text(json.dumps(input_quality,indent=2),encoding='utf-8')
for run,v in result.items():
 print(run,'complete',v['complete'],'reload',v['reload_new_server_marker'])
 for name,s in v['scenes'].items():print(name,'FPS',round(s['fps'],1) if s['fps'] else None,'p99',s['frame']['p99'] if s['frame'] else None,'MSPT',round(s['native_mspt']['mean'],2),'max',round(s['native_mspt']['max'],2),'valid',round(s['window_valid_fraction'],3),'new_chunks',s['new_chunk_events'],'path',round(s['path3d_blocks'],1))
