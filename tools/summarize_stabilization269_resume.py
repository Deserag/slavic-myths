"""Summarize actual resumed runtime receipts; never starts Minecraft."""
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/implementation/stabilization269-resume-20261026'
OLD = ROOT / 'docs/implementation/stabilization269-20261026'

def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

summary = {'scope': 'Actual launches after renewed owner permission; automated scenes, not manual Survival acceptance.', 'runs': []}
sites = []
for path in sorted(OUT.rglob('*.json')):
    if path.name in ('summary.json', 'log-evidence-index.json', 'final-evidence-check.json') or path.name.endswith('-stale.json'):
        continue
    data = read(path)
    if not isinstance(data, dict) or 'status' not in data:
        continue
    checks = data.get('checks', [])
    row = {'receipt': path.relative_to(OUT).as_posix(), 'status': data['status'], 'checks': len(checks),
           'failed_checks': [c for c in checks if c.get('status', c.get('result')) == 'FAIL'], 'error': data.get('error')}
    if path.name == 'receipt.json' and path.parent.name in ('client-controls', 'client-relogin'):
        log_name = 'client-controls-repeat.log' if path.parent.name == 'client-controls' else 'client-relogin.log'
        log = (OUT / log_name).read_text(encoding='utf-8-sig', errors='replace') if (OUT / log_name).exists() else ''
        row['current_log_contains_all_receipt_checks'] = all('QA269_CLIENT ' + c['status'] + ' ' + c['test'] in log for c in checks)
    if path.name == 'receipt.json' and path.parent.name in ('mp-host', 'mp-guest'):
        log = (OUT / (path.parent.name + '.log')).read_text(encoding='utf-8-sig', errors='replace')
        row['current_log_contains_all_receipt_checks'] = all('QA269_MP ' + c['status'] + ' ' + c['test'] in log for c in checks)
    if 'natural_samples' in data:
        for sample in data['natural_samples']:
            if 'actual_structure' not in sample:
                continue
            bounds = sample['bounds']
            run = 'resume-natural0' if path.name == 'natural-targeted-results.json' else 'resume-' + path.name.removesuffix('-results.json')
            sites.append({'seed': data['seed'], 'world': 'work/stabilization269-' + run + '/stabilization269-test',
                          'id': sample['actual_structure'], 'dimension': sample['dimension'],
                          'chunk': sample['candidate_chunk'], 'bounds': bounds,
                          'center_xz': [(bounds[0] + bounds[3]) // 2, (bounds[2] + bounds[5]) // 2],
                          'main_route': sample.get('expected_main_route'), 'main_count': sample.get('actual_main_route_count')})
        row.update(seed=data['seed'], sampled_cells=len(data['natural_samples']),
                   observed=[{'id': c['actual_structure'], 'chunk': c['candidate_chunk'],
                              'main_count': c.get('actual_main_route_count')} for c in data['natural_samples'] if 'actual_structure' in c],
                   no_start_cells=sum('actual_structure' not in c for c in data['natural_samples']))
        baseline = OLD / path.name
        if baseline.exists():
            old = read(baseline)
            def site_map(report):
                return {(c['set'], tuple(c['candidate_chunk'])): c.get('actual_structure') for c in report.get('natural_samples', [])}
            before, after = site_map(old), site_map(data)
            row['same_cells_before_after_changes'] = [{'set': k[0], 'chunk': k[1], 'before': before.get(k), 'after': after.get(k)}
                                                       for k in sorted(before.keys() | after.keys()) if before.get(k) != after.get(k)]
    summary['runs'].append(row)

jar = ROOT / 'work/stabilization269-jar-regression/mods/slavicmyths-2.0.4-HELL.jar'
summary['jar_sha256'] = hashlib.sha256(jar.read_bytes()).hexdigest()
with ZipFile(jar) as archive:
    class_sha = hashlib.sha256(archive.read('org/slavicmyths/worldgen/StructureCandidates.class')).hexdigest()
summary['collision_class_sha256'] = class_sha
restart = OUT / 'jar-regression/restart-results.json'
if restart.exists():
    data = read(restart)
    summary['actual_jar_class_matches'] = data.get('production_collision_class_sha256') == class_sha
    summary['actual_jar_origin'] = data.get('production_code_origin')

index = []
for path in sorted(OUT.glob('*.log')):
    raw = path.read_bytes()
    content = raw.decode('utf-8-sig', errors='replace')
    normalized = path.with_name(path.name + '.txt')
    normalized.write_text(content, encoding='utf-8')
    index.append({'source': path.name, 'raw_sha256': hashlib.sha256(raw).hexdigest(),
                  'review_copy': normalized.name, 'review_sha256': hashlib.sha256(normalized.read_bytes()).hexdigest()})
summary['server_processes'] = sum("Launching target 'forgeserverdev'" in p.read_text(encoding='utf-8-sig', errors='replace') for p in OUT.glob('*.log'))
summary['client_processes'] = sum("Launching target 'forgeclientdev'" in p.read_text(encoding='utf-8-sig', errors='replace') for p in OUT.glob('*.log'))
(OUT / 'log-evidence-index.json').write_text(json.dumps(index, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
(OUT / 'summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
(OUT / 'natural-sites.json').write_text(json.dumps(sites, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
lines = ['# Координаты наблюдавшихся native-стартов', '',
         'Только отдельные QA-миры, без dev create. X/Z — центр bbox, не безопасный Y телепортации. Seed сам по себе не гарантирует эти места в старом мире/другом JAR. Не удалять личные чанки. Точный путь и bounds: natural-sites.json.', '',
         '| Seed | Мир | Измерение | Структура | Стартовый чанк | X/Z | L1 main |',
         '|---|---|---|---|---|---|---|']
for site in sites:
    lines.append(f"| {site['seed']} | `{site['world']}` | {site['dimension']} | `{site['id']}` | {site['chunk']} | {site['center_xz']} | {site['main_count'] if site['main_count'] is not None else 'не проверяется этим правилом'} |")
(OUT / 'NATURAL_SITES.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')
print(json.dumps(summary, ensure_ascii=False, indent=2))
