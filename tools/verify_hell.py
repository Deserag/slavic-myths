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
        assert actual == set(changed) | previous.keys(), ('Unexpected vanilla override', actual-set(changed)-previous.keys())
        for name, content in previous.items():
            assert (RES/name).read_bytes() == content, ('Unrelated existing vanilla integration changed', name)
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
