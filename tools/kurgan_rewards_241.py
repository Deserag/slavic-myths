"""Append bounded rewards without replacing the original artifact/weapon pools (#241)."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
TABLES = ROOT / 'src/main/resources/data/slavicmyths/loot_table'

def item(name, weight=1, lo=1, hi=1):
    entry = dict(type='minecraft:item', name=name, weight=weight)
    if lo != 1 or hi != 1:
        entry['functions'] = [dict(function='minecraft:set_count', count=dict(type='minecraft:uniform', min=lo, max=hi))]
    return entry

def pool(name, entries, chance=1):
    result = dict(name='slavicmyths:rewards241_' + name, rolls=1, entries=entries)
    if chance < 1:
        result['conditions'] = [dict(condition='minecraft:random_chance', chance=chance)]
    return result

def generate():
    for tier, label in enumerate(['small', 'warrior', 'great']):
        for role in ['burial', 'offering', 'important']:
            path = TABLES / 'chests' / f'kurgan_{label}_{role}.json'
            data = json.loads(path.read_text())
            old = [p for p in data['pools'] if not p.get('name', '').startswith('slavicmyths:rewards241_')]
            important = role == 'important'
            # Exactly three extra stacks at most: five-slot coffins retain room for both old pools.
            materials = [item('minecraft:iron_ingot', 6, 1+tier, 2+tier*2),
                         item('minecraft:gold_ingot', 2, 1, 1+tier),
                         item('minecraft:emerald', 1, 1, 1+tier)]
            fragment_chance = ([.20, .30, .40] if not important else [.40, .55, .70])[tier]
            rare_chance = ([.04, .08, .12] if not important else [.10, .18, .28])[tier]
            rare = [item('slavicmyths:blank_rune_1', 6),
                    item('slavicmyths:rune_resilience', 1),
                    item('slavicmyths:rune_strength', 1),
                    item('minecraft:diamond', 2)]
            if tier == 2 and important:
                rare.append(item('slavicmyths:soul', 1))
            data['pools'] = old + [pool('materials', materials, 1 if important else [.35, .50, .65][tier]),
                                   pool('fragments', [item('slavicmyths:soul_fragment', lo=1, hi=1+tier)], fragment_chance),
                                   pool('rare', rare, rare_chance)]
            path.write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')
        # V2 room archetypes already persist these table IDs, but the original resources
        # only provided burial/offering/important. Supply every native route, including
        # old unopened coffins; no geometry or saved IDs need to change.
        for alias, target in dict(common_cache='offering', warrior='burial', treasury='important',
                                  ritual='important', secret='important', great_special='important').items():
            path = TABLES / 'chests' / f'kurgan_{label}_{alias}.json'
            data = dict(type='minecraft:chest', pools=[dict(rolls=1, entries=[dict(type='minecraft:loot_table',
                        value=f'slavicmyths:chests/kurgan_{label}_{target}')])])
            path.write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')
    for boss in ['kurgan_voevoda', 'buried_volkhv', 'unresting_prince']:
        path = TABLES / 'entities' / (boss+'.json')
        data = json.loads(path.read_text())
        reward = next(p for p in data['pools'] if p.get('name') == 'rune_foundation_soul')
        if boss == 'unresting_prince':
            reward['entries'][0]['functions'] = [dict(function='minecraft:set_count', count=dict(type='minecraft:uniform', min=1, max=2))]
        else:
            reward['conditions'] = [dict(condition='minecraft:random_chance', chance=.70)]
        path.write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')

if __name__ == '__main__':
    generate()
