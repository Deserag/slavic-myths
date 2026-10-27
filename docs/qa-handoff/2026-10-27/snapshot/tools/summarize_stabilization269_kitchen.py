"""Evidence summary; no game launch, no installation."""
from pathlib import Path
from zipfile import ZipFile
import json,hashlib,re
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/implementation/stabilization269-kitchen-20261026'
runs=[];intervals=[];index=[];errors=[]
for phase,logname in [('initial','initial-client.log'),('restart','restart-client.log'),('visual','visual-client-206.log')]:
 path=OUT/(phase+'-receipt.json');r=json.loads(path.read_text(encoding='utf-8'));text=(OUT/logname).read_text(encoding='utf-8-sig',errors='replace')
 assert r['status']=='PASS_SINGLE_CLIENT_KITCHEN_SCOPE' and all(c['status']=='PASS' and 'QA269_KITCHEN PASS '+c['test'] in text for c in r['checks'])
 assert (r['master_volume'],r['fps_limit'],r['render_distance'],r['available_processors'],r['max_heap_bytes'],r['GLFW_visible'])==(0.3,30,2,2,1610612736,1)
 start=next(re.match(r'\[(\d+:\d+:\d+)\]',line).group(1) for line in text.splitlines() if "Launching target 'forgeclientdev'" in line)
 stop=next(re.match(r'\[(\d+:\d+:\d+)\]',line).group(1) for line in text.splitlines() if '[minecraft/Minecraft]: Stopping!' in line)
 assert 'BUILD SUCCESSFUL' in text
 intervals.append({'phase':phase,'launch_time':start,'stopping_time':stop})
 runs.append({'phase':phase,'checks':len(r['checks']),'status':r['status'],'current_log_matches_receipt':True,'saved_progress':r.get('saved_progress_before_stop'),'loaded_progress':r.get('loaded_progress')})
assert all(intervals[i]['stopping_time']<intervals[i+1]['launch_time'] for i in range(len(intervals)-1))
for path in sorted(OUT.glob('*.log')):
 raw=path.read_bytes();text=raw.decode('utf-8-sig',errors='replace');copy=path.with_name(path.name+'.txt');copy.write_text(text,encoding='utf-8');index.append({'original':path.name,'original_sha256':hashlib.sha256(raw).hexdigest(),'review':copy.name,'review_sha256':hashlib.sha256(copy.read_bytes()).hexdigest()})
 errors.extend({'log':path.name,'line':n,'text':line} for n,line in enumerate(text.splitlines(),1) if re.search(r'/ERROR\]|Exception',line))
install=json.loads((OUT/'installation-receipt-206.json').read_text(encoding='utf-8'));jar=Path(install['installed_jar']);assert hashlib.sha256(jar.read_bytes()).hexdigest()==install['installed_sha256'];assert list(jar.parent.glob('slavicmyths*.jar'))==[jar]
with ZipFile(jar) as archive:
 classes=[n for n in archive.namelist() if n.endswith('.class')];assert len(classes)==1950
 assert all(archive.read(n)==(ROOT/'build/classes/java/main'/n).read_bytes() for n in classes)
remaining=(OUT/'REMAINING.md').read_text(encoding='utf-8');rows=[x for x in remaining.splitlines() if re.match(r'\|\d+\|',x)];scoped=sum('PASS_SCOPED' in x for x in rows);assert len(rows)==16 and scoped==2
summary={'scope':'one client at a time, supplied kitchen fixture, ordinary production block ticks and native client menu packets','runs':runs,'final_checks':sum(r['checks'] for r in runs),'client_processes':3,'integrated_servers':3,'dedicated_servers':0,'GameTest':0,'process_intervals_local':intervals,'no_client_overlap':True,'errors_or_exceptions':errors,'production_compiled_classes_matching_206_jar':1950,'installed_sha256':install['installed_sha256'],'remaining_direction_count':16-scoped,'total_directions':16,'passed_scoped_technical_directions':scoped,'full_remaining_scenario_count':'NOT_ENUMERATED;14 means directions, not14 tests'}
(OUT/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');(OUT/'log-evidence-index.json').write_text(json.dumps(index,indent=2)+'\n',encoding='utf-8');print(json.dumps(summary,ensure_ascii=False,indent=2))
