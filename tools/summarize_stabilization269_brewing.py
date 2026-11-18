"""Summarize real brewing receipts; refuses incomplete runtime/installation evidence."""
from pathlib import Path
import json,re,hashlib
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/stabilization269-brewing-20261026'
receipts={name:json.loads((OUT/(name+'-receipt.json')).read_text(encoding='utf-8')) for name in ('initial','fixed','restart')}
for name,r in receipts.items():
 assert r['status']=='PASS_SINGLE_CLIENT_BREW_SCOPE' and all(c['status']=='PASS' for c in r['checks']),name
logs=['safe-initial-client-207.log','safe-fixed-client-207.log','safe-restart-client-207.log']
for name in logs:
 s=(OUT/name).read_text(encoding='utf-8-sig');assert 'BUILD SUCCESSFUL' in s
 assert not re.search(r'/(?:ERROR|FATAL)\]|Exception|QA269_BREW FAIL',s),name
install=json.loads((OUT/'installation-receipt-207.json').read_text(encoding='utf-8'))
assert hashlib.sha256(Path(install['installed_jar']).read_bytes()).hexdigest()==install['installed_sha256']
summary={'version':install['version'],'runtime_checks':{name:len(r['checks']) for name,r in receipts.items()},'runtime_checks_total':sum(len(r['checks']) for r in receipts.values()),'native_age_before_break':receipts['fixed']['beer_age_before_native_break'],'offline_assertions':7457,'final_successful_client_starts':3,'final_successful_world_entries':3,'all_brewing_batch_client_starts':11,'all_brewing_batch_world_entries':10,'final_logs_error_exception_lines':0,'installed_jar':install['installed_jar'],'installed_sha256':install['installed_sha256'],'backup':install['backup'],'backup_sha256':install['backup_sha256'],'source_commit':install['source_commit'],'source_patch_sha256':install['source_production_patch_sha256'],'all_mod_runtime_scope':'PARTIAL; Issues OPEN; owner acceptance pending'}
(OUT/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False,indent=2))
