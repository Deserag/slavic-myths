"""Reproduce the 0.9.9 data overlay without changing registry IDs or unrelated data."""
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'

def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def main():
    source = (ROOT / 'src/main/java/org/slavicmyths/worldgen/StructureCoverageService.java').read_text(encoding='utf-8')
    profiles = re.findall(r'add\(p,"([^"]+)","([^"]+)",Tier\.(\w+),(\d+),(\d+),(\d+),(\d+),(\d+),(\d+),(\d+)\);', source)
    assert len(profiles) == 14, 'Incomplete coverage profile inventory'
    for ident, family, tier, spacing, jitter, salt, water, slope, depth, budget in profiles:
        path = RES / f'data/slavicmyths/worldgen/structure_set/{ident}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        assert [entry['structure'] for entry in data['structures']] == ['slavicmyths:' + ident]
        spacing, jitter = int(spacing), int(jitter)
        data['placement'] = {'type': 'minecraft:random_spread', 'spacing': spacing, 'separation': spacing - jitter - 1, 'salt': int(salt), 'spread_type': 'linear'}
        write(path, data)
    base = RES / 'data/slavicmyths/loot_table/entities'
    for entity, item in [('kikimora', 'linen_cloth'), ('poludnitsa', 'field_bundle'), ('polevik', 'field_bundle'), ('bannik', 'bath_stone'), ('igosha', 'old_button'), ('leshy', 'forest_mix')]:
        path = base / f'{entity}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        pool = {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'slavicmyths:' + item}]}
        if pool not in data['pools']:
            data['pools'].insert(0, pool)
        write(path, data)
    loot = {'type': 'minecraft:entity', 'pools': []}
    for name, low, high in [('minecraft:leather', 3, 5), ('slavicmyths:large_animal_bone', 2, 3), ('slavicmyths:raw_bear_meat', 4, 6)]:
        loot['pools'].append({'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': name, 'functions': [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': low, 'max': high}}]}]})
    write(base / 'mother_bear.json', loot)
    for locale, name, deer in [('ru_ru', 'Медведица-мать', 'Яйцо призыва оленей'), ('en_us', 'Mother Bear', 'Deer Family Spawn Egg')]:
        path = RES / f'assets/slavicmyths/lang/{locale}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        data['entity.slavicmyths.mother_bear'] = name
        data['item.slavicmyths.stag_spawn_egg'] = deer
        write(path, data)
    for name in ['wildlife_forest', 'wildlife_plains']:
        path = RES / f'data/slavicmyths/neoforge/biome_modifier/{name}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        for row in data['spawners']:
            if row['type'] in ['slavicmyths:stag', 'slavicmyths:doe']:
                row['minCount'], row['maxCount'] = 2, 5
        write(path, data)
    path = RES / 'data/slavicmyths/tags/worldgen/biome/structures/underwater_ruins.json'
    data = json.loads(path.read_text(encoding='utf-8'))
    for biome in ['minecraft:river', 'minecraft:frozen_river', 'minecraft:ocean', 'minecraft:cold_ocean', 'minecraft:lukewarm_ocean']:
        if biome not in data['values']:
            data['values'].append(biome)
    write(path, data)
    print('MOB_RESOURCES_099 profiles=14 lootOverlay=7 languages=2 deerGroups=2..5')

if __name__ == '__main__':
    main()
