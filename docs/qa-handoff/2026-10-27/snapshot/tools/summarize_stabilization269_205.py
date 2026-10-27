"""Evidence integrity only; does not launch or install anything."""
import hashlib
import json
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/stabilization269-205-20261026'
def read(path): return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
rows=[]
for path in sorted(OUT.rglob('*-results.json')):
    data=read(path);checks=data.get('checks',[])
    row={'receipt':path.relative_to(OUT).as_posix(),'status':data['status'],'checks':len(checks),
         'failed_checks':[c for c in checks if c.get('status',c.get('result'))=='FAIL'],'error':data.get('error')}
    if 'natural_samples' in data:
        row.update(seed=data['seed'],sampled_cells=len(data['natural_samples']),
                   found=[{k:s.get(k) for k in ('actual_structure','candidate_chunk','predicted_first_choice','bounds','full_footprint_chunks','actual_main_route_count')} for s in data['natural_samples'] if 'actual_structure' in s])
    rows.append(row)
index=[]
servers=clients=0
for path in sorted(OUT.glob('*.log')):
    raw=path.read_bytes();text=raw.decode('utf-8-sig',errors='replace');review=path.with_name(path.name+'.txt');review.write_text(text,encoding='utf-8')
    index.append({'original':path.name,'original_sha256':hashlib.sha256(raw).hexdigest(),'review':review.name,'review_sha256':sha(review)})
    servers+="Launching target 'forgeserverdev'" in text;clients+="Launching target 'forgeclientdev'" in text
installation=read(OUT/'installation-receipt.json')
installed=Path(installation['installed_jar']);backup=Path(installation['backup'])
assert sha(installed)==installation['installed_sha256'] and sha(backup)==installation['backup_sha256']
assert list(installed.parent.glob('slavicmyths*.jar'))==[installed]
patch=subprocess.check_output(['git','diff','--binary','--','gradle.properties','src/main'],cwd=ROOT,stderr=subprocess.DEVNULL)
summary={'scope':'Current limited native/server checks, not full Survival acceptance','runs':rows,'final_checks':sum(r['checks'] for r in rows),
         'server_processes_including_failed_QA':servers,'client_processes':clients,'game_test_processes':0,
         'installed_jar_verified':str(installed),'installed_sha256':sha(installed),'single_mod_jar':True,'backup_verified':True,
         'current_production_diff_matches_installed_snapshot':hashlib.sha256(patch).hexdigest()==installation['source_production_patch_sha256']}
(OUT/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(OUT/'log-evidence-index.json').write_text(json.dumps(index,indent=2)+'\n',encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False,indent=2))
