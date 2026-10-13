"""Current production artifact integrity; deliberately no gameplay claims."""
from pathlib import Path
import hashlib, json, struct, zipfile
from check_event_subscribers import check as subscribers

root = Path(__file__).resolve().parents[1]
props = dict(line.split('=', 1) for line in (root/'gradle.properties').read_text().splitlines() if '=' in line)
version = props['mod_version']
jar = root/'build/libs'/f'slavicmyths-{version}.jar'
checks = 0
def require(ok, message):
    global checks
    checks += 1
    assert ok, message
require(props['minecraft_version']=='1.21.1' and props['neo_version']=='21.1.255', 'Platform mismatch')
require('BUILD SUCCESSFUL' in (root/f'work/update-{version}-build.log').read_text(errors='replace'), 'Clean build missing')
with zipfile.ZipFile(jar) as archive:
    names = set(archive.namelist())
    require(f'version="{version}"' in archive.read('META-INF/neoforge.mods.toml').decode().replace(' ', ''), 'Version mismatch')
    for name in names:
        require(not any(token in name.lower() for token in ('gametest', 'org/slavicmyths/verify/', 'tools/smoke', 'uiscreenprobe')), 'Test code leaked: '+name)
        blob = archive.read(name)
        if name.endswith('.class'):
            require(struct.unpack('>H', blob[6:8])[0]==65, 'Java version: '+name)
            require((root/'build/classes/java/main'/name).read_bytes()==blob, 'Stale class: '+name)
        if name.endswith('.json'):
            json.loads(blob)
        source = root/'src/main/resources'/name
        if source.is_file() and name!='META-INF/neoforge.mods.toml':
            require(source.read_bytes()==blob, 'Stale resource: '+name)
    for source in (root/'src/main/resources').rglob('*'):
        if source.is_file():
            require(source.relative_to(root/'src/main/resources').as_posix() in names, 'Resource omitted: '+str(source))
    with zipfile.ZipFile(root/'release/slavicmyths-1.7.9.jar') as previous:
        removed = set(previous.namelist())-names
        require(not removed, 'Previous entries removed: '+str(sorted(removed)))
        changed = sorted(name for name in previous.namelist() if previous.read(name)!=archive.read(name))
result = dict(version=version, status='PASS', checks=checks, sha256=hashlib.sha256(jar.read_bytes()).hexdigest(), event_subscribers=subscribers(jar), changed_since_179=changed, client_launches=0, server_launches=0, gameplay='NOT_RUN')
out = root/'docs/verification'/f'update-{version}'
out.mkdir(parents=True, exist_ok=True)
(out/'resource-gates.json').write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
print(f'PRODUCTION_INTEGRITY_PASS checks={checks} subscribers={result["event_subscribers"]} sha256={result["sha256"]}')
