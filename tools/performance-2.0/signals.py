"""Offline observed peaks; log line counts are not distinct sounds or root causes."""
from pathlib import Path
import csv,json,re
O=Path(__file__).resolve().parents[2]/'docs/implementation/performance-2.0'
out={}
for name in ['world-mod','world-fixed','world-control2','world-bosses-flat']:
 d=O/name
 samples=list(csv.DictReader((d/'observations.csv').open(encoding='utf-8-sig')))
 scenes={}
 for scene in sorted({r['scene'] for r in samples}):
  rows=[r for r in samples if r['scene']==scene]
  scenes[scene]={'particles_peak':max(int(r['particles']) for r in rows),
   'sound_peak':max(int(re.search(r'Sounds: (\d+)',r['sound_debug']).group(1)) for r in rows),
   'client_entities_peak':max(int(r['client_entities']) for r in rows)}
 log=d/'client.log'
 if log.exists():lines=log.read_text(encoding='utf-8-sig',errors='replace').splitlines()
 else:
  import gzip
  with gzip.open(d/'client.sanitized.log.gz','rt',encoding='utf-8') as f:lines=f.read().splitlines()
 out[name]={'scenes':scenes,'audio_failure_lines':sum('Failed to create new sound handle' in v or 'Maximum sound pool size' in v for v in lines),
  'error_lines':sum('/ERROR]' in v for v in lines)}
(O/'signals.json').write_text(json.dumps(out,indent=2)+'\n',encoding='utf-8')
print('Signal summaries:',len(out))
