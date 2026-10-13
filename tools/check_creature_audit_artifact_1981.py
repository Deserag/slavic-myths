"""Current production/resource integrity gates; no historical port parity claims."""
from pathlib import Path
import hashlib
import json
import zipfile

root = Path(__file__).resolve().parents[1]
version = next(s.split('=', 1)[1] for s in (root / 'gradle.properties').read_text().splitlines() if s.startswith('mod_version='))
jar = root / f'build/libs/slavicmyths-{version}.jar'
checks = 0
with zipfile.ZipFile(jar) as archive:
    assert archive.testzip() is None
    names = archive.namelist()
    assert 'META-INF/neoforge.mods.toml' in names
    assert not any(n.startswith(('tools/', 'smoke/')) or n.endswith('Checks.class') for n in names)
    for path in (root / 'src/main/resources').rglob('*'):
        if not path.is_file():
            continue
        name = path.relative_to(root / 'src/main/resources').as_posix()
        assert name in names, name
        if name.startswith(('assets/', 'data/')):
            assert archive.read(name) == path.read_bytes(), name
        if name.endswith('.json'):
            json.loads(archive.read(name))
        checks += 1
result = {'status': 'PASS', 'scope': 'ZIP CRC, production metadata/test exclusion, source resource presence, assets/data byte parity, JSON syntax; not runtime loading or balance',
          'resources_checked': checks, 'jar': str(jar), 'sha256': hashlib.sha256(jar.read_bytes()).hexdigest(), 'game_starts': 0}
out = root / 'docs/audit/creatures-1.9.8.1/artifact-checks.json'
out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(result, ensure_ascii=False))
