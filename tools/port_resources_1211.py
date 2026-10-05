"""Idempotent, schema-specific 1.16.5 -> 1.21.1 data migration.

Uses the target vanilla codecs/examples, not a global namespace replacement.
Textures and structure NBT are never rewritten. NBT load/DFU remains runtime QA.
"""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT/'src/main/resources'
FOLDERS = {'advancements':'advancement','recipes':'recipe','structures':'structure',
           'loot_tables':'loot_table','predicates':'predicate','item_modifiers':'item_modifier'}
TAGS = {'items':'item','blocks':'block','entity_types':'entity_type','fluids':'fluid',
        'game_events':'game_event'}
def target_path(path):
    parts = list(path.relative_to(RES).parts)
    if parts[0] == 'data' and len(parts) > 2:
        parts[2] = FOLDERS.get(parts[2], parts[2])
        if parts[2] == 'tags':
            parts[3] = TAGS.get(parts[3], parts[3])
        if parts[1] == 'forge':
            if parts[2] == 'loot_modifiers':
                parts[1] = 'neoforge'
            elif parts[2] == 'tags':
                parts[1] = 'c'
    return RES.joinpath(*parts)

def item_predicate(obj):
    if 'item' in obj:
        obj['items'] = [obj.pop('item')]
    if 'tag' in obj:
        assert 'items' not in obj, 'Legacy item+tag predicate needs explicit intersection migration'
        obj['items'] = '#' + obj.pop('tag')
    for old, new in (('enchantments','minecraft:enchantments'),
                     ('stored_enchantments','minecraft:stored_enchantments')):
        if old in obj:
            values = obj.pop(old)
            for enchantment in values:
                if 'enchantment' in enchantment:
                    enchantment['enchantments'] = enchantment.pop('enchantment')
            obj.setdefault('predicates', {})[new] = values
    if 'nbt' in obj:
        obj.setdefault('predicates', {})['minecraft:custom_data'] = obj.pop('nbt')

def location_predicate(obj):
    if 'feature' in obj:
        obj['structures'] = [obj.pop('feature')]
    if 'biome' in obj:
        obj['biomes'] = [obj.pop('biome')]

def entity_predicate(obj):
    if not isinstance(obj, dict):
        return
    for field in ('location','stepping_on'):
        if isinstance(obj.get(field), dict):
            location_predicate(obj[field])
    for value in obj.get('equipment', {}).values():
        item_predicate(value)
    for field in ('vehicle','passenger','targeted_entity'):
        if field in obj:
            entity_predicate(obj[field])

def loot_tree(obj):
    if isinstance(obj, list):
        for value in obj:
            loot_tree(value)
    elif isinstance(obj, dict):
        if obj.get('type') == 'minecraft:loot_table' and 'name' in obj:
            obj['value'] = obj.pop('name')
        if obj.get('condition') == 'minecraft:alternative':
            obj['condition'] = 'minecraft:any_of'
        if obj.get('condition') == 'minecraft:random_chance_with_looting':
            chance = obj.pop('chance')
            bonus = obj.pop('looting_multiplier')
            obj['condition'] = 'minecraft:random_chance_with_enchanted_bonus'
            obj['enchantment'] = 'minecraft:looting'
            obj['unenchanted_chance'] = chance
            # Target Linear is base + per_level_above_first * (level - 1).
            obj['enchanted_chance'] = {'type':'minecraft:linear',
                                      'base':round(chance+bonus,8),
                                      'per_level_above_first':bonus}
        if obj.get('function') == 'minecraft:looting_enchant':
            obj['function'] = 'minecraft:enchanted_count_increase'
            obj['enchantment'] = 'minecraft:looting'
        if obj.get('condition') == 'forge:loot_table_id':
            obj['condition'] = 'neoforge:loot_table_id'
        if obj.get('condition') == 'minecraft:match_tool':
            item_predicate(obj['predicate'])
        if obj.get('condition') == 'minecraft:entity_properties':
            entity_predicate(obj.get('predicate', {}))
        for value in obj.values():
            loot_tree(value)

def normalize(path, obj):
    parts = path.relative_to(RES).parts
    if parts[0] != 'data':
        return obj
    kind = parts[2]
    if kind == 'recipe':
        result = obj.get('result')
        if isinstance(result, str):
            obj['result'] = {'id':result}
        elif isinstance(result, dict) and 'item' in result:
            result['id'] = result.pop('item')
    elif kind == 'advancement':
        icon = obj.get('display',{}).get('icon',{})
        if 'item' in icon:
            icon['id'] = icon.pop('item')
        for criterion in obj.get('criteria', {}).values():
            conditions = criterion.get('conditions', {})
            if criterion.get('trigger') == 'minecraft:inventory_changed':
                for predicate in conditions.get('items', []):
                    item_predicate(predicate)
            if criterion.get('trigger') == 'minecraft:location' and 'location' in conditions:
                location = conditions.pop('location')
                location_predicate(location)
                extra = {'condition':'minecraft:entity_properties','entity':'this',
                         'predicate':{'location':location}}
                if 'player' not in conditions:
                    conditions['player'] = [extra]
                else:
                    assert isinstance(conditions['player'],list)
                    conditions['player'].append(extra)
            for field in ('player','entity','victim'):
                predicate = conditions.get(field)
                if isinstance(predicate,list):
                    loot_tree(predicate)
                else:
                    entity_predicate(predicate)
    elif kind in ('loot_table','loot_modifiers','predicate','item_modifier'):
        loot_tree(obj)
    # Explicit target-ID compatibility and a pre-existing dangling reference.
    if parts[1:] == ('slavicmyths','tags','block','whistle_fragile.json'):
        obj['values'] = ['minecraft:short_grass' if value == 'minecraft:grass' else value for value in obj['values']]
    if parts[1:] == ('slavicmyths','loot_table','entities','likho_one_eyed.json'):
        for pool in obj.get('pools',[]):
            for entry in pool.get('entries',[]):
                if entry.get('type') == 'minecraft:item' and entry.get('name') == 'slavicmyths:pustoy_sosud':
                    entry['name'] = 'slavicmyths:pustoy_ritualny_sosud'
    return obj

def migrate():
    mappings, changed = {}, []
    # Includes binary NBT: move the exact bytes; no invented conversion/version.
    for path in sorted((RES/'data').rglob('*')):
        if not path.is_file():
            continue
        target = target_path(path)
        original = path.read_bytes()
        raw = original
        if path.suffix == '.json':
            obj = normalize(target,json.loads(original))
            raw = (json.dumps(obj,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
        if target != path:
            if target.exists() and target.read_bytes() != raw:
                raise RuntimeError(f'Refusing collision: {target}')
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(raw)
            mappings[path.relative_to(RES).as_posix()] = target.relative_to(RES).as_posix()
            path.unlink()
        elif raw != original:
            path.write_bytes(raw)
        if raw != original:
            changed.append(target.relative_to(RES).as_posix())
    for path in sorted((RES/'data').rglob('*'), key=lambda p:len(p.parts), reverse=True):
        if path.is_dir() and not any(path.iterdir()):
            path.rmdir()
    pack = json.loads((RES/'pack.mcmeta').read_text())
    pack['pack']['pack_format'] = 34
    (RES/'pack.mcmeta').write_text(json.dumps(pack,ensure_ascii=False)+'\n',encoding='utf-8')
    report = ROOT/'docs/port/resource-migration.json'
    old = json.loads(report.read_text()) if report.exists() else {'paths':{},'schema_updates':[]}
    old['paths'].update(mappings)
    old['schema_updates'] = sorted(set(old['schema_updates'])|set(changed))
    old['runtime_parse_verified'] = False
    report.write_text(json.dumps(old,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f'Data paths migrated: {len(mappings)}; JSON schemas updated: {len(changed)}; no texture/NBT edits.')

if __name__ == '__main__':
    migrate()
