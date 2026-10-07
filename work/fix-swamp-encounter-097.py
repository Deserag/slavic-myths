import zipfile
from pathlib import Path
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar');s=z.read('net/minecraft/gametest/framework/GameTestServer.java').decode();print('\n'.join(l for l in s.splitlines() if 'Difficulty' in l or 'difficulty' in l))
p=Path('tools/swamp_resources_097.py');s=p.read_text(encoding='utf-8').replace("b.encounter(9,2,10,'bolotnik_elite')","b.encounter(5,2,10,'bolotnik_elite')");p.write_text(s,encoding='utf-8')
