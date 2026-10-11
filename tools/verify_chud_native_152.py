"""Certify completed technical checks without marking unfinished worldgen acceptance PASS."""
from pathlib import Path
import argparse, hashlib, json, re, zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/verification/chud-1.5.2'
parser = argparse.ArgumentParser()
parser.add_argument('--log', type=Path, required=True)
options = parser.parse_args()
log = options.log.read_text(encoding='utf-8', errors='replace')
assert 'All 14 required tests passed' in log and 'BUILD SUCCESSFUL' in log
assert 'ERROR]' not in log and 'FATAL]' not in log, 'Final native runtime has errors'
production = json.loads((OUT/'resource-gates.json').read_text())
jar = Path(production['jar'])
digest = hashlib.sha256(jar.read_bytes()).hexdigest()
assert digest == production['sha256'] and production['production'] == production['resources'] == 'PASS'
classes = ROOT/'work/chud-152/build/classes/java/main'
identical = 0
with zipfile.ZipFile(jar) as archive:
    for name in archive.namelist():
        if name.startswith('org/slavicmyths/') and name.endswith('.class'):
            assert archive.read(name) == (classes/name).read_bytes(), name
            identical += 1
geometry = json.loads((OUT/'geometry.json').read_text())
assert len(geometry) == 36 and all(not p['unreachable'] for p in geometry)
full = json.loads((OUT/'full-native-placement.json').read_text())
assert len(full) == 5 and all(p['pathStatus'] == 'PASS' and p['nbtRoundtrip'] for p in full)
assert {p['shaftProfile'] for p in full if p['kind'] == 'INDEPENDENT_GREAT'} == {0,1,2}
natural = json.loads((OUT/'natural-candidates.json').read_text())
assert len(natural) == 12 and sum(p['sampledCells'] for p in natural) >= 480
cases = {kind: set() for kind in ['ATTACHED_SMALL','INDEPENDENT_MEDIUM','INDEPENDENT_GREAT']}
for world in natural:
    for p in world['starts']:
        cases[p['kind']].add((world['seed'],p['rootComplexId']))
    for p in world['cities']:
        if p['attached']:
            cases['ATTACHED_SMALL'].add((world['seed'],p['cityId']))
    if world['independentPairsChecked']:
        assert world['minimumBoundaryDistance'] >= 2000
finder = json.loads((OUT/'native-seed-finder.json').read_text())
for p in finder:
    if p['accepted']:
        cases[p['result']].add((p['seed'],p['rootComplexId']))
counts = {kind: len(found) for kind,found in cases.items()}
quota = all(value >= 4 for value in counts.values())
report = {'technical_status':'PASS','acceptance':'TECHNICAL_COMPLETE_MANUAL_OPEN' if quota else 'OPEN_WORLDGEN_QUOTA',
          'production_sha256':digest,'native_log':str(options.log.resolve()),
          'required_tests':14,'geometry_plans':36,'native_full_cases':5,
          'runtime_production_identical_classes':identical,'natural_world_seeds':12,
          'natural_candidates':sum(p['sampledCells'] for p in natural),
          'unique_natural_examples':counts,'minimum_four_per_kind': 'PASS' if quota else 'OPEN',
          'manual_gameplay':'NOT_TESTED','client_launches':0}
(OUT/'native-gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report))
