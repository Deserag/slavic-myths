"""Scoped Nether prototype; reads pinned vanilla resources, never audio/assets/worlds."""
import argparse
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
DEFAULT = Path.home() / '.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'


def binary(kind, a, b):
    return {'type': 'minecraft:' + kind, 'argument1': a, 'argument2': b}


def gradient(a, b, va, vb):
    return {'type': 'minecraft:y_clamped_gradient', 'from_y': a, 'to_y': b,
            'from_value': va, 'to_value': vb}


def noise(name, scale, vertical=0.0):
    density = {'type': 'minecraft:noise', 'noise': 'minecraft:' + name,
               'xz_scale': scale, 'y_scale': vertical}
    # These fields are independent of Y. Native FlatCache computes them once
    # per quart X/Z grid instead of for each of the 28 vertical noise cells.
    return {'type': 'minecraft:flat_cache', 'argument': density} if vertical == 0 else density


def generate(vanilla):
    with zipfile.ZipFile(vanilla) as archive:
        dimension = json.loads(archive.read('data/minecraft/dimension_type/the_nether.json'))
        settings = json.loads(archive.read('data/minecraft/worldgen/noise_settings/nether.json'))
        ores = {name: json.loads(archive.read('data/minecraft/worldgen/placed_feature/' + name + '.json'))
                for name in ['ore_debris_small', 'ore_gold_nether', 'ore_gold_deltas',
                             'ore_quartz_nether', 'ore_quartz_deltas']}
    # Raising genDepth would otherwise silently spread the SAME attempt count
    # across 96 extra layers. Preserve the original 1.21.1 ore height ranges.
    for ore in ores.values():
        for placement in ore['placement']:
            if placement['type'] == 'minecraft:height_range':
                maximum = placement['height']['max_inclusive']
                placement['height']['max_inclusive'] = {'absolute': 127 - maximum['below_top']}
    dimension['logical_height'] = 224
    settings['noise']['height'] = 224
    # Low frequency shorelines + smaller surface relief. The three gradient
    # segments create natural benches without integer quantization or platforms.
    floor_gradient = binary('add', gradient(8, 40, 1.0, 0.1),
                            binary('add', gradient(40, 56, 0.0, -0.65), gradient(56, 104, 0.0, -1.1)))
    floor = binary('add', floor_gradient,
                   binary('add', binary('mul', 2.4, noise('continentalness', 0.8)),
                          binary('mul', 0.22, noise('surface', 1.0))))
    roof = binary('add', gradient(156, 216, -1.0, 1.0),
                  binary('mul', 0.45, noise('erosion', 1.0)))
    # Keep 3-D formations locally, while the broad floor/roof shape dominates.
    formations = binary('add', -0.65, binary('mul', 0.22, 'minecraft:nether/base_3d_noise'))
    shape = binary('max', floor, binary('max', roof, formations))
    # Guarantee dense material for the original relative bedrock surface rules.
    shape = binary('max', shape, binary('max', gradient(0, 8, 2.0, -2.0),
                                      gradient(212, 223, -2.0, 2.0)))
    settings['noise_router']['final_density'] = {
        'type': 'minecraft:squeeze', 'argument': binary('mul', 0.64, {
            'type': 'minecraft:interpolated', 'argument': {
                'type': 'minecraft:blend_density', 'argument': shape}})}
    return {'dimension_type/the_nether.json': dimension, 'worldgen/noise_settings/nether.json': settings,
            **{'worldgen/placed_feature/' + name + '.json': ore for name, ore in ores.items()}}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--vanilla', type=Path, default=DEFAULT)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for name, resource in generate(args.vanilla).items():
        target = ROOT / 'src/main/resources/data/minecraft' / name
        content = json.dumps(resource, ensure_ascii=False, indent=2) + '\n'
        if args.check:
            if not target.exists() or target.read_text(encoding='utf-8') != content:
                raise SystemExit('Resource drift: ' + str(target))
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(content, encoding='utf-8')
        print(('CHECK ' if args.check else 'WRITE ') + str(target.relative_to(ROOT)))


if __name__ == '__main__':
    main()
