"""Reproduce only Chud city registrations/localization; preserve unrelated live edits."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
DATA = RES / 'data/slavicmyths'
resources = {
    'worldgen/structure/chud_city.json': {
        'type': 'slavicmyths:chud_city', 'biomes': '#slavicmyths:has_structure/chud_city',
        'step': 'underground_structures', 'terrain_adaptation': 'none', 'spawn_overrides': {}},
    'worldgen/structure_set/chud_city.json': {
        'structures': [{'structure': 'slavicmyths:chud_city', 'weight': 1}],
        'placement': {'type': 'slavicmyths:chud_region', 'spacing': 256,
                      'separation': 160, 'salt': 1128813892, 'spread_type': 'linear'}},
    'tags/worldgen/biome/has_structure/chud_city.json': {
        'replace': False, 'values': ['#minecraft:is_overworld']},
}
for relative, value in resources.items():
    path = DATA / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
for language, title in [('ru_ru', 'Подземный град Чуди'), ('en_us', 'Underground Chud City')]:
    path = RES / f'assets/slavicmyths/lang/{language}.json'
    value = json.loads(path.read_text(encoding='utf-8'))
    value['structure.slavicmyths.chud_city'] = title
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
