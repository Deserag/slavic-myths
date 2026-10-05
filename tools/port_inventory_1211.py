"""Capture the actual pre-port tree, including uncommitted Yaga fixes. Run once.

Source literals are combined with existing generated-content manifests. This is
an inventory, not proof that a registry has frozen successfully on the target.
"""
from pathlib import Path
from collections import Counter
import hashlib, json, re, zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/port'
OUT.mkdir(parents=True, exist_ok=True)
snapshot = OUT / 'baseline-0.9.3.json'
if snapshot.exists():
    raise SystemExit('Baseline exists; refusing to replace pre-port evidence.')
rows = {}
def add(kind, name, evidence):
    if ':' not in name and kind not in ('saved_data', 'command', 'curios_slot'):
        name = 'slavicmyths:' + name
    key = kind, name
    if key not in rows:
        rows[key] = dict(kind=kind, old_id=name, target_id=name,
                         status='PENDING', evidence=[])
    if evidence not in rows[key]['evidence']:
        rows[key]['evidence'].append(evidence)

registers = {}
local_registers = {}
sources = {p: p.read_text(encoding='utf-8') for p in (ROOT/'src/main/java').rglob('*.java')}
kinds = {'ITEMS':'item', 'BLOCKS':'block', 'ENTITIES':'entity_type',
         'SOUND_EVENTS':'sound_event', 'TILE_ENTITIES':'block_entity_type',
         'POTIONS':'mob_effect', 'CONTAINERS':'menu', 'FEATURES':'feature',
         'STRUCTURE_FEATURES':'structure_type', 'ENCHANTMENTS':'enchantment',
         'RECIPE_SERIALIZERS':'recipe_serializer', 'LOOT_MODIFIER_SERIALIZERS':'loot_modifier_serializer'}
for p, source in sources.items():
    for match in re.finditer(r'(\w+)\s*=\s*DeferredRegister\.create\(ForgeRegistries\.(\w+)', source):
        registers[(p.stem, match[1])] = kinds[match[2]]
        local_registers.setdefault(p, {})[match[1]] = kinds[match[2]]
for p, source in sources.items():
    ev = p.relative_to(ROOT).as_posix()
    for match in re.finditer(r'(?:(\w+)\.)?(\w+)\.register\(\s*"([a-z0-9_/.:-]+)"\s*[,)]', source):
        kind = registers.get((match[1] or p.stem, match[2]))
        if kind:
            add(kind, match[3], ev)
    if p.name == 'ModItems.java':
        for name in re.findall(r'\b(?:food|stew|egg)\(\s*"([a-z0-9_]+)"', source):
            add('item', name, ev)
    if p.name == 'ModSounds.java':
        for name in re.findall(r'\bsound\("([a-z0-9_]+)"', source):
            add('sound_event', name, ev)
    for name in re.findall(r'IRecipeType\.register\("([^"]+)"', source):
        add('recipe_type', name, ev)
    if re.search(r'extends\s+(?:[\w.]+\.)?WorldSavedData', source):
        for name in re.findall(r'super\("([^"]+)"', source):
            add('saved_data', name, ev)
    for name in re.findall(r'(?:Commands\.)?literal\("([^"]+)"', source):
        add('command', name, ev + ' (literal; consult command tree for syntax)')
    for match in re.finditer(r'Registry\.register\((?:Registry|WorldGenRegistries)\.(\w+),\s*new ResourceLocation\("slavicmyths",\s*"([^"]+)"', source):
        add({'STRUCTURE_PIECE':'structure_piece','CONFIGURED_STRUCTURE_FEATURE':'structure',
             'CONFIGURED_FEATURE':'configured_feature'}.get(match[1], match[1].lower()), match[2], ev)
    if p.name == 'SwampStructures.java':
        names = re.search(r'String\[\] IDS=\{([^}]+)', source)
        for name in re.findall(r'"([^"]+)"', names[1]):
            add('structure_type', name, ev)
            add('structure', name, ev)
    if p.name == 'KurganStructures.java':
        for name in re.findall(r'STRUCTURES\.register\("([^"]+)"', source):
            add('structure', name, ev)
    for name in re.findall(r'\b(?:UID|ID)\s*=\s*new ResourceLocation\("slavicmyths",\s*"([^"]+)"', source):
        if '/compat/' in ev and 'jei' in source.lower():
            add('jei_category', name, ev)

for p in (ROOT/'docs/verification').glob('*.json'):
    data = json.loads(p.read_text(encoding='utf-8-sig'))
    for field, kind in (('items','item'),('blocks','block'),('world_only_blocks','block')):
        for name in data.get(field, []):
            if isinstance(name, str):
                add(kind, name, p.relative_to(ROOT).as_posix() + ':' + field)
for name in ('head','necklace','ring','belt','charm'):
    add('curios_slot', name, 'FolkAccessoryItem.slots; ring size=2, others size=1')
res = ROOT/'src/main/resources'
for p in (res/'data').rglob('*'):
    if not p.is_file():
        continue
    rel = p.relative_to(res/'data')
    parts = rel.parts
    if parts[1] == 'tags':
        kind = 'tag/' + parts[2]
        name = '/'.join(parts[3:]).removesuffix('.json')
    else:
        kind = {'recipes':'recipe','advancements':'advancement','loot_tables':'loot_table',
                'structures':'structure_template'}.get(parts[1], parts[1])
        name = '/'.join(parts[2:]).removesuffix('.json').removesuffix('.nbt')
    add(kind, parts[0]+':'+name, p.relative_to(ROOT).as_posix())

hashes = {p.relative_to(ROOT).as_posix():hashlib.sha256(p.read_bytes()).hexdigest()
          for p in (ROOT/'src').rglob('*') if p.is_file()}
entries = sorted(rows.values(), key=lambda r:(r['kind'],r['old_id']))
data = dict(source_version='0.9.3+yaga-placement-hotfix', java_files=len(sources),
            counts=dict(sorted(Counter(r['kind'] for r in entries).items())),
            entries=entries, source_sha256=hashes)
snapshot.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
lines = ['# Registry inventory before the 1.21.1 port', '',
         'Captured from the actual working tree, including the Yaga placement hotfix.',
         'PENDING means only a target ID mapping exists; target registration is unverified.',
         'Command rows are literals with source evidence, not independently executable syntax.', '',
         '| Category | OLD ID | TARGET ID | STATUS | NOTES |', '|---|---|---|---|---|']
for r in entries:
    lines.append('| '+' | '.join([r['kind'],r['old_id'],r['target_id'],r['status'],
                                  '<br>'.join(r['evidence'])])+' |')
(OUT/'REGISTRY_INVENTORY_1.16.5.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
imports = {}
for p, source in sources.items():
    for imp in re.findall(r'import\s+(net\.minecraftforge\.[^;]+);', source):
        imports.setdefault(imp, []).append(p.relative_to(ROOT).as_posix())
(OUT/'forge-api-usage.json').write_text(json.dumps(imports,indent=2)+'\n',encoding='utf-8')
backup = ROOT/'.tools/port-backups/slavicmyths-0.9.3-pre-port.zip'
backup.parent.mkdir(parents=True, exist_ok=True)
if backup.exists():
    print(json.dumps(data['counts'],ensure_ascii=False))
    raise SystemExit('Preserved original source checkpoint; inventory corrected.')
with zipfile.ZipFile(backup,'w',zipfile.ZIP_DEFLATED) as z:
    for folder in ('src','tools','docs','gradle'):
        for p in (ROOT/folder).rglob('*'):
            if p.is_file() and '__pycache__' not in p.parts:
                z.write(p,p.relative_to(ROOT))
    for name in ('build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','AGENTS.md'):
        z.write(ROOT/name,name)
print(json.dumps(data['counts'],ensure_ascii=False))
print('Snapshot and complete source checkpoint:', backup)
