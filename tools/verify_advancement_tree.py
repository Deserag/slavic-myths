"""Issue #22: current resources, dispatch references and immutable save contract.

Source dispatch coverage is evidence of wiring, not a gameplay simulation.
Run from any directory; --jar additionally checks the production artifact.
"""
import argparse
import hashlib
import json
import re
import zipfile
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
OUT = ROOT / 'docs/implementation/advancements-2-implementation'
DESIGN = ROOT / 'docs/implementation/advancements-redesign'
CONTRACT = ROOT / 'docs/implementation/advancements-micro-patch/save-contract.json'


def read(p):
    return json.loads(p.read_text(encoding='utf-8-sig'))


def data_refs(value):
    if isinstance(value, list):
        for entry in value:
            data_refs(entry)
    elif isinstance(value, dict):
        for field, raw in value.items():
            if field in ('structures', 'items'):
                for ref in raw if isinstance(raw, list) else [raw]:
                    if not isinstance(ref, str) or not ref.lstrip('#').startswith('slavicmyths:'):
                        continue
                    ns, name = ref.lstrip('#').split(':', 1)
                    if field == 'structures':
                        path = f'data/{ns}/'+('tags/worldgen/structure/' if ref.startswith('#') else 'worldgen/structure/')+name+'.json'
                    elif ref.startswith('#'):
                        path = f'data/{ns}/tags/item/{name}.json'
                    else:
                        path = f'assets/{ns}/models/item/{name}.json'
                    assert (RES/path).is_file(), 'Missing predicate reference '+ref
            data_refs(raw)


def audit():
    nodes = {'slavicmyths:' + p.relative_to(RES/'data/slavicmyths/advancement').as_posix()[:-5]: read(p)
             for p in (RES/'data/slavicmyths/advancement').rglob('*.json')}
    locales = [read(RES/f'assets/slavicmyths/lang/{lang}.json') for lang in ('ru_ru', 'en_us')]
    sources = {p.relative_to(ROOT).as_posix(): p.read_text(encoding='utf8')
               for p in (ROOT/'src/main/java').rglob('*.java')}
    # Only literals inside actual award calls; ternary alternatives are included.
    dispatch = {}
    for path, source in sources.items():
        if '/client/' in path or '/compat/' in path:
            continue
        for call in re.findall(r'\b(?:award|grant|nativeVictory|victory)\(([^;]*?)\);', source):
            for name in re.findall(r'"([a-z_]+)"', call):
                dispatch.setdefault('slavicmyths:'+name, []).append(path)
    for path,digest in read(OUT/'DESIGN_CONTRACT.json')['files'].items():
        assert subprocess.check_output(['git','hash-object','--path='+path,path],cwd=ROOT,text=True).strip()==digest, path+' frozen design/save contract'
    baseline = read(CONTRACT)
    tree = {'slavicmyths:'+n['id']: n for n in read(DESIGN/'TREE.json')}
    new_ids = set(tree)-set(baseline)
    assert len(new_ids) == 11 and set(nodes) == set(baseline)|new_ids, 'Advancement IDs added or removed'
    for path, source in sources.items():
        # These two existing helpers award exactly one hardcoded criterion.
        for helper, criterion in ((r'(?:FolkAccessoryItem|YagaServices)\.award', 'event'),
                                  (r'\bgrant', 'brewed')):
            for call in re.findall(helper+r'\(([^;]*?)\);', source):
                for name in re.findall(r'"([a-z_]+)"', call):
                    key = 'slavicmyths:'+name
                    if key in nodes:
                        assert criterion in nodes[key]['criteria'], path+' helper criterion mismatch '+key
    groups = {'burial', 'depths', 'forest', 'home', 'knowledge', 'legends', 'night', 'roads', 'water'}
    for group in groups:
        node = nodes['slavicmyths:group_'+group]
        assert node['parent'] == 'slavicmyths:root', group+' section'
        assert ('display' in node) == ('slavicmyths:group_'+group in tree), group+' section presentation'
        assert node['criteria'] == {'section': {'trigger': 'minecraft:tick'}}, group+' section availability'
    rows = []
    for key, node in sorted(nodes.items()):
        if key in baseline:
            assert {k: node.get(k) for k in ('criteria', 'requirements', 'rewards')} == baseline[key], key+' save contract'
        else:
            criterion = 'mastery' if key == 'slavicmyths:class_iv' else 'victory'
            assert node['criteria'] == {criterion: {'trigger':'minecraft:impossible'}} and node['requirements'] == [[criterion]] and node.get('rewards') == {}, key+' new contract'
        criteria = node['criteria']
        data_refs(criteria)
        for recipe in node.get('rewards', {}).get('recipes', []):
            if recipe.startswith('slavicmyths:'):
                assert (RES/f'data/slavicmyths/recipe/{recipe.split(":")[1]}.json').is_file(), key+' recipe reward'
        assert criteria and all(isinstance(c, dict) and re.fullmatch(r'[a-z_]+:[a-z_/]+', c['trigger']) for c in criteria.values()), key
        requirements = node.get('requirements', [[c] for c in criteria])
        assert requirements and all(group and all(c in criteria for c in group) for group in requirements), key
        assert set(sum(requirements, [])) == set(criteria), key+' omitted criteria'
        seen = set()
        at = key
        while at in nodes:
            assert at not in seen, key+' cycle'
            seen.add(at)
            parent = nodes[at].get('parent')
            if parent is None:
                assert at == 'slavicmyths:root', key+' unexpected root'
                break
            assert parent in nodes or parent in ('minecraft:recipes/root', 'minecraft:husbandry/root', 'minecraft:husbandry/breed_an_animal'), key+' missing parent '+parent
            at = parent
        recipe = key.startswith('slavicmyths:recipes/')
        display = node.get('display')
        if not recipe:
            assert bool(display) == (key in tree), key+' display allowlist'
        if display:
            assert display.get('frame', 'task') in ('task', 'goal', 'challenge'), key
            for flag in ('hidden', 'show_toast', 'announce_to_chat'):
                assert flag not in display or isinstance(display[flag], bool), key
            icon = display['icon']['id']
            assert ':' in icon, key
            if icon.startswith('slavicmyths:'):
                assert (RES/f'assets/slavicmyths/models/item/{icon.split(":")[1]}.json').is_file(), key+' icon'
            if 'background' in display:
                ns, path = display['background'].split(':')
                if ns == 'slavicmyths':
                    assert (RES/f'assets/{ns}/{path}').is_file(), key+' background'
            for field in ('title', 'description'):
                component = display[field]
                assert 'translate' in component or 'text' in component, key
                if 'translate' in component:
                    for locale in locales:
                        assert locale.get(component['translate'], '').strip(), key+' missing '+component['translate']
        impossible = any(c['trigger'] == 'minecraft:impossible' for c in criteria.values())
        assert not impossible or dispatch.get(key), key+' no server award dispatch'
        rows.append(dict(id=key, parent=node.get('parent'), hidden=bool(display and display.get('hidden')), recipe=recipe,
                         criteria=criteria, requirements=requirements, dispatch=dispatch.get(key, [])))
    source = read(DESIGN/'SOURCE.json')
    old_game = {n['id']:n for n in source['nodes'] if not n['recipe']}
    assert len(tree)==55 and sum(key in baseline for key in tree)==44, '55/44 visible inventory'
    assert sum('display' not in nodes[key] for key in old_game)==85, '85 data-only'
    for key,spec in tree.items():
        node=nodes[key]; display=node['display']
        assert node.get('parent') == ('slavicmyths:'+spec['parent'] if spec['parent'] else None), key+' parent allowlist'
        if spec['parent']:assert 'display' in nodes[node['parent']], key+' data-only visible parent'
        assert display['icon']=={'id':spec['icon']}, key+' icon allowlist'
        for field in ('frame','hidden','show_toast','announce_to_chat'):assert display.get(field)==spec[field], key+' display allowlist '+field
        for field in ('title','description'):
            for locale,language in zip(locales,('ru','en')):assert locale[display[field]['translate']]==spec[field+'_'+language], key+' approved localization'
        if key.startswith('slavicmyths:group_'):
            for locale,language in zip(locales,('ru','en')):assert locale['tales.group.'+spec['id'].removeprefix('group_')]==spec['title_'+language], key+' book navigation localization'
        if key in old_game:
            before=old_game[key]['resource']
            assert {k:v for k,v in node.items() if k not in ('display','parent')} == {k:v for k,v in before.items() if k not in ('display','parent')}, key+' legacy field contract'
    for key,record in old_game.items():
        if key not in tree:
            expected=dict(record['resource']);expected.pop('display',None)
            assert nodes[key]==expected, key+' data-only contract'
    for path,digest in read(OUT/'resource-contract.json')['files'].items():
        actual=subprocess.check_output(['git','hash-object','--path='+path,path],cwd=ROOT,text=True).strip()
        assert actual==digest, path+' resource byte parity'
    # Literal Knowledge reads must resolve; dynamic dispatch remains explicitly documented.
    for path, source in sources.items():
        for key in re.findall(r'Knowledge\.(?:award|knows)\([^,;]+,\s*"([a-z_]+)"', source):
            assert 'slavicmyths:'+key in nodes, path+' dangling '+key
    tales = {'slavicmyths:'+p.stem: read(p) for p in (RES/'data/slavicmyths/tales').rglob('*.json')}
    for key, tale in tales.items():
        assert all(a in nodes for a in tale.get('advancements', [])), key+' dangling advancement'
        assert all(a in tales for a in tale.get('links', [])), key+' dangling tale'
        assert all(locale.get(tale['title']) for locale in locales), key+' title'
    for path,expected in read(OUT/'resource-contract.json')['tales'].items():assert read(ROOT/path)==expected, path+' tale semantic contract'
    return dict(status='PASS', total=len(nodes), gameplay=sum(not r['recipe'] for r in rows),
                recipes=[r['id'] for r in rows if r['recipe']], hidden=[r['id'] for r in rows if r['hidden']],
                external_parents=[dict(id=r['id'], parent=r['parent']) for r in rows if r['parent'] and not r['parent'].startswith('slavicmyths:')],
                tales=len(tales), visible=len(tree), data_only=85, legacy_contract=len(baseline), nodes=rows, scope='Static wiring; runtime tests reported separately')


def condition_report(result):
    locales = [read(RES/f'assets/slavicmyths/lang/{l}.json') for l in ('ru_ru', 'en_us')]
    lines = ['# Advancement condition inventory', '',
             'Generated from current resources by tools/verify_advancement_tree.py. Dispatch paths prove wiring, not completion of every gameplay scenario.', '',
             '| ID | Parent | Condition / server dispatch | RU description | EN description |', '|---|---|---|---|---|']
    for row in result['nodes']:
        if row['recipe']:
            continue
        node = read(RES/f'data/slavicmyths/advancement/{row["id"].split(":")[1]}.json')
        display=node.get('display')
        if not display:display=next(n['resource']['display'] for n in read(DESIGN/'SOURCE.json')['nodes'] if n['id']==row['id'])
        key = display['description']['translate']
        condition = json.dumps(row['criteria'], ensure_ascii=False, separators=(',', ':'))
        fields = [row['id'], row['parent'] or '(root)', condition+'; '+', '.join(row['dispatch']),
                  *[locale[key] for locale in locales]]
        lines.append('| '+' | '.join(f.replace('|', '/') for f in fields)+' |')
    (OUT/'CONDITIONS.md').write_text('\n'.join(lines)+'\n', encoding='utf8')


def jar_check():
    from PIL import Image
    props = dict(line.split('=', 1) for line in (ROOT/'gradle.properties').read_text().splitlines() if '=' in line)
    jar = ROOT/f'build/libs/slavicmyths-{props["mod_version"]}.jar'
    checks = 0
    with zipfile.ZipFile(jar) as z:
        assert not any('/verify/' in n or '/smoke/' in n or 'slavicmyths_advancements/' in n or 'HistoricalPolicyChecks' in n for n in z.namelist()), 'Test code in production'
        for p in RES.rglob('*'):
            if p.is_file() and p.name != 'neoforge.mods.toml':
                assert z.read(p.relative_to(RES).as_posix()) == p.read_bytes(), str(p)
                if p.suffix == '.json':
                    read(p)
                if p.suffix == '.png':
                    with Image.open(p) as image:
                        image.verify()
                checks += 1
        for p in (ROOT/'build/classes/java/main').rglob('*.class'):
            data = z.read(p.relative_to(ROOT/'build/classes/java/main').as_posix())
            assert data == p.read_bytes() and int.from_bytes(data[6:8], 'big') == 65, str(p)
            checks += 1
        meta = z.read('META-INF/neoforge.mods.toml').decode()
        assert props['mod_version'] in meta and '21.1.255' in meta and '[1.21.1]' in meta
    from check_event_subscribers import check
    return dict(checks=checks, sha256=hashlib.sha256(jar.read_bytes()).hexdigest(), subscribers=check(jar))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--jar', action='store_true')
    args = parser.parse_args()
    result = audit()
    if args.jar:
        result['production'] = jar_check()
    # One row per node keeps the full evidence reviewable without thousands of repeated lines.
    fields = []
    for key, value in result.items():
        encoded = ('[\n'+',\n'.join('    '+json.dumps(row, ensure_ascii=False) for row in value)+'\n  ]') if key == 'nodes' else json.dumps(value, ensure_ascii=False)
        fields.append('  '+json.dumps(key)+': '+encoded)
    (OUT/'AUDIT.json').write_text('{\n'+',\n'.join(fields)+'\n}\n', encoding='utf8')
    condition_report(result)
    print(json.dumps({k: v for k, v in result.items() if k not in ('nodes', 'external_parents', 'recipes')}, ensure_ascii=False))
