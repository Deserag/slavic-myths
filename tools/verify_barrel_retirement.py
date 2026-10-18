"""Current offline resource/model/structure/production checks; never runs Minecraft."""
import gzip
import hashlib
import io
import json
import re
import struct
import subprocess
import zipfile
from pathlib import Path
import nbtlib
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
checks = 0


def need(ok, message):
    global checks
    checks += 1
    if not ok:
        raise AssertionError(message)


def read(path):
    return json.loads(path.read_text('utf8'))


def main():
    version = re.search(r'^mod_version=(.+)$', (ROOT/'gradle.properties').read_text(), re.M)[1]
    jar = ROOT/f'build/libs/slavicmyths-{version}.jar'
    vanilla_path = ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
    vanilla = set(zipfile.ZipFile(vanilla_path).namelist())

    def ref(name, folder, suffix):
        if name.startswith('#'):
            return
        ns, path = name.split(':', 1) if ':' in name else ('minecraft', name)
        rel = f'assets/{ns}/{folder}/{path}{suffix}'
        need((RES/rel).is_file() or rel in vanilla or path.startswith('builtin/'), rel)

    def model(node):
        if isinstance(node, dict):
            for key, value in node.items():
                if key in ('parent', 'model') and isinstance(value, str):
                    ref(value, 'models', '.json')
                elif key == 'textures' and isinstance(value, dict):
                    for texture in value.values():
                        ref(texture, 'textures', '.png')
                elif isinstance(value, (dict, list)):
                    model(value)
        elif isinstance(node, list):
            for value in node:
                model(value)

    resources = [p for p in RES.rglob('*') if p.is_file()]
    for path in resources:
        if path.suffix == '.json':
            value = read(path); need(True, str(path))
            if '/models/' in path.as_posix() or '/blockstates/' in path.as_posix():
                model(value)
            if '/recipe/' in path.as_posix() or '/loot_table/' in path.as_posix():
                if path.name != 'wooden_barrel.json':
                    need('slavicmyths:wooden_barrel' not in json.dumps(value), 'new acquisition '+str(path))
        elif path.suffix == '.png':
            with Image.open(path) as im:
                im.verify()
            need(True, 'PNG '+str(path))
    need(not (RES/'data/slavicmyths/recipe/wooden_barrel.json').exists(), 'no barrel recipe')
    # Check every template. Compare changed templates to HEAD as typed NBT, allowing ONLY palette replacement.
    changed = subprocess.check_output(['git', 'diff', '--name-only', '58b76448b1513259cbcf3e5205f58d1b9227f357', '--', 'src/main/resources/data/slavicmyths/structure'], cwd=ROOT).decode().splitlines()
    cat = subprocess.Popen(['git', 'cat-file', '--batch'], cwd=ROOT, stdin=subprocess.PIPE, stdout=subprocess.PIPE)
    try:
        for rel in changed:
            cat.stdin.write(('58b76448b1513259cbcf3e5205f58d1b9227f357:'+rel+'\n').encode()); cat.stdin.flush()
            header = cat.stdout.readline().decode().split(); size = int(header[2])
            data = cat.stdout.read(size); cat.stdout.read(1)
            old = nbtlib.File.parse(io.BytesIO(gzip.decompress(data)))
            for state in old['palette']:
                if str(state['Name']) == 'slavicmyths:wooden_barrel':
                    facing = state['Properties']['facing']
                    state['Name'] = nbtlib.String('minecraft:barrel')
                    state['Properties'] = nbtlib.Compound({'facing': facing, 'open': nbtlib.String('false')})
            need(nbtlib.load(ROOT/rel) == old, 'no unrelated NBT changes '+rel)
    finally:
        cat.stdin.close(); cat.wait()
    templates = 0
    for path in (RES/'data/slavicmyths/structure').rglob('*.nbt'):
        doc = nbtlib.load(path); templates += 1
        need(all(str(s['Name']) != 'slavicmyths:wooden_barrel' for s in doc.get('palette', [])), 'legacy structure source '+str(path))
        need(all(0 <= int(b['state']) < len(doc['palette']) for b in doc.get('blocks', [])), 'palette indices '+str(path))
    for name in ('fruit_press', 'fermentation_vat', 'small_keg'):
        need(len(read(RES/f'assets/slavicmyths/blockstates/{name}.json')['variants']) == 20, name+' states')
    expected = {'malted_barley':1200,'honey_infusion':800,'forest_herbal_infusion':1000,'light_beer_wort':1200,'dark_beer_wort':1400,'hopped_beer_wort':1200,'honey_beer_wort':1400,'forest_beer_wort':1600,'thunder_beer_wort':1800,'veles_dark_wort':2000,'witch_berry_wort':1800,'kvass_wort':1000,'mead_must':1600,'cider_must':600,'berry_mors':600}
    for name, time in expected.items():
        need(read(RES/f'data/slavicmyths/recipe/vat/{name}.json')['time'] == time, 'recipe duration '+name)
    for lang in ('ru_ru', 'en_us'):
        need('brewing.slavicmyths.vat_aging' in read(RES/f'assets/slavicmyths/lang/{lang}.json'), 'instructions '+lang)
    with zipfile.ZipFile(jar) as z:
        need(z.testzip() is None, 'JAR CRC')
        need('data/slavicmyths/recipe/wooden_barrel.json' not in z.namelist(), 'retired recipe absent from JAR')
        for path in resources:
            rel = path.relative_to(RES).as_posix()
            if rel == 'META-INF/neoforge.mods.toml':
                need(version in z.read(rel).decode() and '${mod_version}' not in z.read(rel).decode(), 'version expansion')
            else:
                need(z.read(rel) == path.read_bytes(), 'resource parity '+rel)
        output = ROOT/'build/classes/java/main'
        expected_classes = {p.relative_to(output).as_posix() for p in output.rglob('*.class')}
        actual_classes = {n for n in z.namelist() if n.endswith('.class')}
        need(expected_classes == actual_classes, 'exact production class parity; no test classes')
        for name in actual_classes:
            need(struct.unpack('>H', z.read(name)[6:8])[0] == 65, 'Java21 '+name)
        for cls in ('storage/StorageBlock', 'storage/StorageTile', 'brewing/Fermentation'):
            need('org/slavicmyths/'+cls+'.class' in actual_classes, 'legacy retained '+cls)
    report = dict(status='PASS', version=version, checks=checks, resources=len(resources), templates=templates,
                  changed_templates=len(changed), jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),
                  client_starts=0, server_starts=0, gametest_starts=0, runtime='NOT_RUN')
    dest = ROOT/'docs/implementation/barrel-retirement/GATES.json'
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(report, indent=2)+'\n', encoding='utf8')
    print(json.dumps(report))


if __name__ == '__main__':
    main()
