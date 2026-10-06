"""Join loaded server registry facts with source/resource references. No invented IDs."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/org/slavicmyths'
DATA = ROOT / 'src/main/resources/data/slavicmyths'
REPORT = ROOT / 'docs/verification/mob-worldgen-0.9.9'

def read(path):
    return path.read_text(encoding='utf-8')

def main():
    REPORT.mkdir(parents=True, exist_ok=True)
    rows = json.loads(read(REPORT / 'loaded-mob-registry.json'))
    sources = {p.relative_to(JAVA).as_posix(): read(p) for p in JAVA.rglob('*.java')}
    sounds = json.loads(read(ROOT / 'src/main/resources/assets/slavicmyths/sounds.json'))
    full = {'kikimora', 'poludnitsa', 'polevik', 'bannik', 'igosha'}
    major = {'leshy', 'brown_bear', 'boar', 'forest_wolf'}
    npc = {'baba_yaga', 'domovoy', 'vodyanoy'}
    wildlife = {'bear_cub', 'stag', 'doe', 'pike', 'carp', 'crayfish'}
    lines = ['# Mob registry audit 0.9.9', '',
             'Current values measured from loaded NeoForge server registries. Historical baseline is in MOB_REGISTRY_BASELINE_0.9.9.md. '
             'Source links describe existing implementations; they do not certify gameplay.', '',
             '| ID | Class | HP / damage / armor / speed / range | Eggs | Natural spawns | Loot | Sounds | Renderer source | Category | Problems |',
             '|---|---|---|---|---|---|---|---|---|---|']
    for row in rows:
        ident = row['id'].split(':')[1]
        category = 'FULL_REWORK' if ident in full else 'MAJOR_REWORK' if ident in major else 'NON_COMBAT_NPC' if ident in npc else 'AMBIENT/WILDLIFE' if ident in wildlife else 'POLISH_ONLY' if any('max_health' in key for key in row) else 'HELPER/NON_MOB'
        stats = ' / '.join(str(row.get('minecraft:generic.' + key, row.get('minecraft:' + key, '—'))) for key in ['max_health', 'attack_damage', 'armor', 'movement_speed', 'follow_range'])
        spawns = ', '.join(f"{e['biome']} w={e['weight']} {e['min']}–{e['max']}" for e in row['naturalSpawns']) or 'structure/egg only'
        aliases = {'brown_bear': 'bear', 'bear_cub': 'cub', 'forest_wolf': 'wolf'}
        loot_id = row.get('lootTable', 'slavicmyths:entities/' + aliases.get(ident, ident))
        loot = DATA / 'loot_table' / (loot_id.split(':', 1)[1] + '.json')
        audio = ', '.join(k for k in sounds if k.startswith(aliases.get(ident, ident) + '_')) or 'class vanilla mapping / source review'
        render = ', '.join(p for p, text in sources.items() if p.startswith('client/') and (ident in text or row['class'].rsplit('.', 1)[-1] in text) and ('Renderer' in p or p.endswith('ClientSetup.java')))
        problems = 'implemented; headless windows/family checks; manual combat/navigation/visual QA pending' if category in {'FULL_REWORK', 'MAJOR_REWORK'} else 'full kit/loot/audio review pending'
        lines.append(f"| {row['id']} | {row['class']} | {stats} | {', '.join(row['spawnEggs']) or '—'} | {spawns} | {str(loot.relative_to(ROOT)) if loot.exists() else 'class-defined / source review'} | {audio} | {render or 'helper renderer registration review'} | {category} | {problems} |")
    target = ROOT / 'docs/mobs/MOB_REGISTRY_AUDIT_0.9.9.md'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text('\n'.join(lines) + '\n', encoding='utf-8')
    worldgen = []
    for folder in ['structure', 'configured_feature']:
        for path in sorted((DATA / 'worldgen' / folder).glob('*.json')):
            record = json.loads(read(path))
            worldgen.append({'kind': folder, 'id': 'slavicmyths:' + path.stem, 'type': record['type'], 'data': record})
    (REPORT / 'worldgen-inventory.json').write_text(json.dumps(worldgen, indent=2) + '\n', encoding='utf-8')
    print(f"AUDIT entries={len(rows)} worldgen={len(worldgen)}")

if __name__ == '__main__':
    main()
