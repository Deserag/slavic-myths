"""Reproducible source/resource inventory and preservation check for #289.

No loader bootstrap: resource-only rows are explicitly candidates, NOT proof of
runtime registration. Dynamic Java expressions still require runtime validation.
"""
import csv
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/implementation/lost-halls-source-gate/evidence'
JAVA = ROOT / 'src/main/java/org/slavicmyths'
RES = ROOT / 'src/main/resources'


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT).decode('utf-8').strip()


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    rows = {}

    def add(kind, name, evidence, scope):
        key = (kind, 'slavicmyths:' + name)
        value = rows.setdefault(key, {'evidence': set(), 'scope': set()})
        value['evidence'].add(evidence)
        value['scope'].add(scope)

    for kind, folder in [('block', 'assets/slavicmyths/blockstates'),
                         ('item', 'assets/slavicmyths/models/item'),
                         ('biome', 'data/slavicmyths/worldgen/biome')]:
        for path in sorted((RES / folder).glob('*.json')):
            add(kind, path.stem, path.relative_to(ROOT).as_posix(), 'RESOURCE_CANDIDATE')
    for path in sorted(JAVA.rglob('*.java')):
        code = re.sub(r'/\*.*?\*/|//[^\n]*', '', path.read_text(encoding='utf-8'), flags=re.S)
        for match in re.finditer(r'(?:ModItems\.)?ITEMS\.register\(\s*"([a-z0-9_]+)"\s*,', code):
            add('item', match[1], path.relative_to(ROOT).as_posix(), 'STATIC_JAVA')
        for match in re.finditer(r'(?:ModEntities\.)?ENTITIES\.register\(\s*"([a-z0-9_]+)"\s*,', code):
            add('entity_type', match[1], path.relative_to(ROOT).as_posix(), 'STATIC_JAVA')
    # Explicit expansions of the two existing Nether families, with source guards.
    for package, file in [('navya', 'NavyaContent.java'), ('blackcinder', 'CinderContent.java')]:
        path = JAVA / package / file
        code = path.read_text(encoding='utf-8')
        if package == 'navya':
            names = re.findall(r'Kind\.\w+,"([a-z_]+)"', code.split('static {')[0])
            assert names and 'ModEntities.ENTITIES.register' in code
        else:
            assert 'for(Kind k:Kind.values())' in code and 'ModEntities.ENTITIES.register(id(k)' in code
            assert 'k.name().toLowerCase(Locale.ROOT)' in code
            names = [name.strip().lower() for name in code.split('public enum Kind {')[1].split('}')[0].split(',')]
            assert len(names) == 5, 'Cinder dynamic source changed; review expansion'
        for name in names:
            add('entity_type', name, path.relative_to(ROOT).as_posix(), 'STATIC_JAVA_FAMILY')
    with (OUT / 'registry_inventory.tsv').open('w', encoding='utf-8', newline='') as stream:
        writer = csv.writer(stream, delimiter='\t')
        writer.writerow(['registry', 'id', 'evidence', 'scope'])
        for (kind, name), row in sorted(rows.items()):
            writer.writerow([kind, name, ';'.join(sorted(row['evidence'])), ';'.join(sorted(row['scope']))])
    # Exact bytes of this checkout, alongside Git object identity (CRLF may differ).
    tracked = git('ls-files', 'src/main').splitlines()
    manifest = {name: hashlib.sha256((ROOT / name).read_bytes()).hexdigest() for name in tracked}
    (OUT / 'production-sha256.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
    candidates = ['lost_hall_stone', 'cracked_lost_hall_stone', 'luminous_vein_stone',
                  'cave_coating', 'lost_whisper_thread', 'lightvein_shard', 'echo_vial',
                  'spore_pouch', 'lost_halls_waymark', 'lost_halls_tuning_fork', 'posten', 'posten_head',
                  'rune_shadow_face', 'polished_lost_hall_stone']
    candidates += [base + suffix for base in ['lost_hall_stone', 'polished_lost_hall_stone']
                   for suffix in ['_stairs', '_slab', '_wall']]
    collisions = {name: [kind for kind, identifier in rows if identifier == 'slavicmyths:' + name]
                  for name in candidates}
    with (OUT.parent / 'id-plan.tsv').open('w', encoding='utf-8', newline='') as stream:
        writer = csv.writer(stream, delimiter='\t')
        writer.writerow(['candidate_id', 'owner_issue', 'source_collision', 'state'])
        for name in candidates:
            issue = 303 if name == 'rune_shadow_face' else 302 if name == 'posten_head' else 293 if name == 'posten' else 290 if ('stone' in name or name == 'cave_coating') else 301
            writer.writerow(['slavicmyths:' + name, issue, ','.join(collisions[name]) or 'NONE_IN_STATIC_INVENTORY', 'PROPOSAL_NOT_REGISTERED'])
        writer.writerow(['slavicmyths:zharenitsa_head', 302, 'EXISTING: CinderBlocks.java', 'PRESERVE_ID'])
        for scene in ['S1_corridor', 'S2_bridge', 'S3_large_hall', 'S4_fungus_hall', 'S5_luminous_grotto', 'S6_junction']:
            writer.writerow([scene, '291 modules; 294 worldgen', 'NOT_A_REGISTRY_ID', 'BLOCKED_REFERENCE'])
        for name in ['nether_bricks', 'red_nether_bricks', 'blackstone', 'basalt', 'shroomlight', 'warped_fungus', 'warped_nylium']:
            writer.writerow(['minecraft:' + name, '291/294', 'REUSE_VANILLA', 'NO_NEW_WOOD'])
    report = {
        'scope': 'STATIC source inventory plus resource candidates; NOT a runtime registry dump',
        'branch': git('branch', '--show-current'), 'head': git('rev-parse', 'HEAD'),
        'spec_commit': git('rev-parse', 'origin/main'),
        'production_diff': git('diff', 'HEAD', '--', 'src/main'),
        'untracked_production': git('ls-files', '--others', '--exclude-standard', 'src/main'),
        'counts': {kind: sum(k == kind for k, _ in rows) for kind in sorted({k for k, _ in rows})},
        'candidate_collisions': collisions, 'hashed_production_files': len(manifest),
        'existing_head': 'slavicmyths:zharenitsa_head; preserve ID and migrate behavior in #302',
        'runtime_launches': 0, 'runtime_registry_validation': 'NOT_RUN_ENV_BLOCK',
    }
    (OUT / 'inventory.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: report[k] for k in ['branch', 'head', 'counts', 'hashed_production_files']}))


if __name__ == '__main__':
    main()
