"""Certify a compile/offline-only search fix; never substitutes for Minecraft runtime QA."""
from pathlib import Path
import argparse, hashlib, json, zipfile, re

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--report', type=Path, required=True)
parser.add_argument('--baseline', type=Path, required=True)
args = parser.parse_args()
receipt = json.loads((args.report / 'resource-gates.json').read_text())
jar = Path(receipt['jar'])
assert receipt['resources'] == receipt['production'] == 'PASS'
assert hashlib.sha256(jar.read_bytes()).hexdigest() == receipt['sha256']
log = (args.report / 'build.log').read_text(encoding='utf-8')
assert '> Task :clean' in log and '> Task :build' in log and 'BUILD SUCCESSFUL' in log
match=re.search(r'OFFLINE SEARCH CHECKS PASS: (\d+); Minecraft launches=0',log)
assert match and int(match.group(1))>=11 and 'BUILD FAILED' not in log
assert hashlib.sha256(args.baseline.read_bytes()).hexdigest() == 'ca2a1e9e7ab2ebfb740edee54f7318f604a04119d5aa6f8b49c05e8699810103'
unchanged = 0
matched = 0
with zipfile.ZipFile(jar) as current, zipfile.ZipFile(args.baseline) as previous:
    assert not any('SearchOfflineChecks' in name or '/verify/' in name for name in current.namelist())
    prefixes = ['ChudPlan', 'ChudGeometry', 'ChudPiece', 'AbandonedPlan', 'AbandonedGeometry', 'AbandonedPiece']
    for name in previous.namelist():
        if name.startswith('org/slavicmyths/chud/') and name.endswith('.class') and name.rsplit('/', 1)[-1].split('$')[0].removesuffix('.class') in prefixes:
            assert current.read(name) == previous.read(name), name
            unchanged += 1
    for path in (root / 'work/chud-152/production-build/classes/java/main').rglob('*.class'):
        name = path.relative_to(root / 'work/chud-152/production-build/classes/java/main').as_posix()
        assert current.read(name) == path.read_bytes(), name
        matched += 1
assert unchanged >= 20 and matched >= 900
source = (root / 'src/main/java/org/slavicmyths/chud/ChudSearchCommands.java').read_text()
worker = source.split('WORKER.execute(()->', 1)[1]
assert 'source.getLevel()' not in worker and 'getChunk(' not in worker and 'getBlockState(' not in worker
for name in ['ChudCommands', 'AbandonedCommands']:
    code = (root / f'src/main/java/org/slavicmyths/chud/{name}.java').read_text()
    assert 'getChunkNow(x,z)' in code and 'getChunk(x,z,ChunkStatus.FULL,false)' not in code
result = dict(build='PASS', offline_checks='PASS', offline_check_count=int(match.group(1)),
              production_sha256=receipt['sha256'], compiled_classes_identical=matched,
              saved_geometry_classes_identical=unchanged, baseline_sha256=hashlib.sha256(args.baseline.read_bytes()).hexdigest(),
              runtime_validation='NOT_RUN', minecraft_launches=0, acceptance='OPEN_RUNTIME_NOT_RUN',
              natural_worldgen_quota='OPEN; historical medium=5, unique attached=1, great=0')
(args.report / 'offline-gates.json').write_text(json.dumps(result, indent=2) + '\n')
print(json.dumps(result))
