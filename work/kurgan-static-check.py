import json
from pathlib import Path
p=Path('tools/kurgan_resources_096.py');s=p.read_text(encoding='utf-8-sig');s=s.replace("print('KURGAN_RESOURCES", "put(Path('docs/kurgan/registry-additions-0.9.6.json'),{'entries':[{'kind':kind,'target_id':'slavicmyths:'+n} for n in new for kind in ['block','item']]})\nprint('KURGAN_RESOURCES");p.write_text(s,encoding='utf-8')
p=Path('tools/verify_static_data_1211.py');s=p.read_text();needle="sources=ROOT/'build/moddev/artifacts/neoforge-21.1.255-sources.jar'";s=s.replace(needle,"""if '--kurgan-rework' in sys.argv:
    for entry in json.loads((ROOT/'docs/kurgan/registry-additions-0.9.6.json').read_text(encoding='utf-8'))['entries']:
        known.setdefault(entry['kind'],set()).add(entry['target_id'])
"""+needle);s=s.replace("audit=ROOT/('docs/verification/navigation", "audit=ROOT/('docs/verification/kurgan-0.9.6/static-data-audit.json' if '--kurgan-rework' in sys.argv else 'docs/verification/navigation");p.write_text(s,encoding='utf-8')
