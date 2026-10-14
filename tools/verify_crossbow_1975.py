from pathlib import Path
import json,hashlib,zipfile
from PIL import Image
R=Path(__file__).resolve().parents[1]
O=R/'docs/implementation/crossbow-1.9.7.5'
checks=0
for p in (R/'src/main/resources').rglob('*.json'):
    json.loads(p.read_text(encoding='utf-8-sig'));checks+=1
for p in (R/'src/main/resources').rglob('*.png'):
    with Image.open(p) as im:im.verify()
    checks+=1
jar=R/'build/libs/slavicmyths-1.9.7.5.jar'
with zipfile.ZipFile(jar) as z:
    assert not any('/verify/' in n or '/smoke/' in n or 'IssueTests' in n for n in z.namelist())
    for p in (R/'src/main/resources').rglob('*'):
        if p.is_file() and p.name!='neoforge.mods.toml':
            assert z.read(p.relative_to(R/'src/main/resources').as_posix())==p.read_bytes(),p
            checks+=1
    for p in (R/'build/classes/java/main').rglob('*.class'):
        assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes(),p
        checks+=1
    meta=z.read('META-INF/neoforge.mods.toml').decode()
    assert '1.9.7.5' in meta and '21.1.255' in meta and '[1.21.1]' in meta
log=(O/'BUILD_RUNTIME.log').read_text(encoding='utf-8-sig')
assert 'BUILD SUCCESSFUL' in log
assert 'BUILD SUCCESSFUL' in (O/'FINAL_BUILD.log').read_text(encoding='utf-8-sig')
assert 'All 13 required tests passed' in log
result=dict(status='PASS',version='1.9.7.5',jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),checks=checks,game_tests=13,client_starts=0,game_test_server_starts=1)
(O/'RESOURCE_JAR_CHECKS.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result))
