"""Independent preservation and packaged-resource checks for the Nether patch."""
import argparse
import copy
import hashlib
import json
import io
from pathlib import Path
import subprocess
import tarfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources/data/minecraft'
VANILLA = Path.home() / '.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', type=Path)
    parser.add_argument('--with-navya', action='store_true', help='Permit only additive tags for the integrated Navya asset manifest')
    args = parser.parse_args()
    changed = ['dimension_type/the_nether.json', 'worldgen/noise_settings/nether.json']
    ores = ['ore_debris_small', 'ore_gold_nether', 'ore_gold_deltas', 'ore_quartz_nether', 'ore_quartz_deltas']
    changed += ['worldgen/placed_feature/' + name + '.json' for name in ores]
    with zipfile.ZipFile(VANILLA) as vanilla:
        def original(name):
            return json.loads(vanilla.read('data/minecraft/' + name))
        def current(name):
            return json.loads((RES / name).read_text(encoding='utf-8'))
        dimension = current(changed[0])
        expected = original(changed[0]); expected['logical_height'] = 224
        assert dimension == expected, 'Only portal logical height may change in dimension type'
        settings = current(changed[1]); baseline = original(changed[1])
        restored = copy.deepcopy(settings)
        restored['noise']['height'] = baseline['noise']['height']
        restored['noise_router']['final_density'] = baseline['noise_router']['final_density']
        assert restored == baseline, 'Biome climate/surface/lava/ore/spawn/random settings drift'
        assert settings['noise'] == {'min_y': 0, 'height': 224, 'size_horizontal': 1, 'size_vertical': 2}
        assert settings['sea_level'] == 32
        for name in changed[2:]:
            old = original(name); new = current(name)
            for placement in old['placement']:
                if placement['type'] == 'minecraft:height_range':
                    maximum = placement['height']['max_inclusive']
                    placement['height']['max_inclusive'] = {'absolute': 127-maximum['below_top']}
            assert new == old, ('Only pin the previous ore maximum Y', name)
        # No other vanilla override can change frequencies, loot, biome definitions,
        # structures, templates, spawners or overworld/end settings.
        actual = {str(p.relative_to(RES)).replace('\\','/') for p in RES.rglob('*') if p.is_file()}
        prefix = 'src/main/resources/data/minecraft/'
        archive_bytes = subprocess.check_output(['git', 'archive', '275ecaa5', prefix], cwd=ROOT)
        with tarfile.open(fileobj=io.BytesIO(archive_bytes)) as archive:
            previous = {member.name.removeprefix(prefix): archive.extractfile(member).read()
                        for member in archive.getmembers() if member.isfile()}
        extra=actual-set(changed)-previous.keys()
        assert previous.keys()<=actual, 'Existing vanilla integration removed'
        assert not extra or args.with_navya and all(n.startswith('tags/') for n in extra), ('Unexpected vanilla override', extra)
        allowed=set()
        if args.with_navya:
            manifest=json.loads((ROOT/'docs/implementation/navya-grove/asset-manifest.json').read_text())
            allowed={'slavicmyths:'+n for group in ['geology','wood','flora'] for n in manifest[group]} | {'slavicmyths:navya_grove','#slavicmyths:navya_logs','slavicmyths:navya_wall_sign','slavicmyths:navya_wall_hanging_sign'}
        for name in extra:
            data=json.loads((RES/name).read_text());assert data.get('replace') is False and all(v in allowed for v in data['values']),('Unexpected additive Navya tag',name)
        for name, content in previous.items():
            actual_content=(RES/name).read_bytes()
            if actual_content.replace(b'\r\n',b'\n')==content.replace(b'\r\n',b'\n'):continue
            assert args.with_navya and name.startswith('tags/'), ('Unrelated existing vanilla integration changed', name)
            before=json.loads(content);after=json.loads(actual_content)
            assert {k:v for k,v in before.items() if k!='values'}=={k:v for k,v in after.items() if k!='values'},name
            assert all(v in after['values'] for v in before['values']),name
            assert all(v in before['values'] or v in allowed for v in after['values']),name
        receipt = {'status':'PASS', 'changedVanillaResources':changed, 'gameLaunches':0,
                   'runtime':'NOT_RUN', 'sourceBaseline':'275ecaa5',
                   'vanillaClientSha256':hashlib.sha256(VANILLA.read_bytes()).hexdigest()}
        if args.jar:
            with zipfile.ZipFile(args.jar) as artifact:
                for name in changed:
                    assert artifact.read('data/minecraft/'+name) == (RES/name).read_bytes(), name
                assert not any('HellColumns' in n or n.startswith('org/slavicmyths/verify/') for n in artifact.namelist())
            receipt['jar'] = str(args.jar.resolve())
            receipt['jarSha256'] = hashlib.sha256(args.jar.read_bytes()).hexdigest()
        out=ROOT/'docs/implementation/hell-patch1/resource-check.json'
        out.parent.mkdir(parents=True,exist_ok=True)
        out.write_text(json.dumps(receipt,indent=2)+'\n',encoding='utf-8')
        print(json.dumps(receipt,indent=2))


if __name__ == '__main__':
    main()
