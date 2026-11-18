"""Summarize real client receipts and bind compiled production classes to installed JAR."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/stabilization269-mp-ritual-20261026'
rows=[]
for phase in ('initial','restart'):
    for who in ('host','guest'):
        path=OUT/phase/(who+'-receipt.json')
        if not path.exists():continue
        receipt=json.loads(path.read_text(encoding='utf-8'));log=OUT/(phase+'-'+who+'.log');text=log.read_text(encoding='utf-8-sig',errors='replace')
        checks=receipt['checks']
        assert all('QA269_RITUAL_MP '+who+' '+c['status']+' '+c['test'] in text for c in checks)
        assert receipt['master_volume']==0.3 and receipt['GLFW_visible']==1
        rows.append({'phase':phase,'role':who,'status':receipt['status'],'checks':len(checks),
                     'failed_checks':[c for c in checks if c['status']=='FAIL'],'current_log_matches_receipt':True,
                     'action_client_game_times':{k:v for k,v in receipt.items() if k.startswith('sent_phase_')}})
index=[];errors=[];starts=0
for path in sorted(OUT.glob('*.log')):
    raw=path.read_bytes();text=raw.decode('utf-8-sig',errors='replace');review=path.with_name(path.name+'.txt');review.write_text(text,encoding='utf-8')
    index.append({'original':path.name,'original_sha256':hashlib.sha256(raw).hexdigest(),'review':review.name,'review_sha256':hashlib.sha256(review.read_bytes()).hexdigest()})
    starts+="Launching target 'forgeclientdev'" in text
    errors.extend({'log':path.name,'line':n,'text':line} for n,line in enumerate(text.splitlines(),1) if re.search(r'/ERROR\]|Exception|Connection reset',line))
install=json.loads((ROOT/'docs/implementation/stabilization269-205-20261026/installation-receipt.json').read_text(encoding='utf-8'))
jar=Path(install['installed_jar']);assert hashlib.sha256(jar.read_bytes()).hexdigest()==install['installed_sha256'];assert list(jar.parent.glob('slavicmyths*.jar'))==[jar]
with ZipFile(jar) as archive:
    classes=[n for n in archive.namelist() if n.endswith('.class')]
    assert len(classes)==1950
    for name in classes:assert archive.read(name)==(ROOT/'build/classes/java/main'/name).read_bytes(),name
summary={'scope':'controlled real network fixture, supplied reagents; not full Survival, not same-server-tick proof',
         'runs':rows,'final_checks':sum(r['checks'] for r in rows),'actual_client_processes':starts,
         'integrated_servers':sum("Launching target 'forgeclientdev'" in p.read_text(encoding='utf-8-sig',errors='replace') for p in OUT.glob('*-host.log')),
         'dedicated_servers':0,'GameTest':0,'errors_or_exceptions':errors,
         'production_classes_matching_installed_jar':1950,'installed_jar':str(jar),'installed_sha256':install['installed_sha256'],'single_mod_jar':True}
(OUT/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(OUT/'log-evidence-index.json').write_text(json.dumps(index,indent=2)+'\n',encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False,indent=2))
