"""Independent data checks for the 0.8.0 production artifact; no Minecraft launch."""
from pathlib import Path
import hashlib, json, re, zipfile
from verify_swamp_073 import read_nbt
import bandits_080
R=Path(__file__).resolve().parents[1]
RES=R/'src/main/resources'
D=RES/'data/slavicmyths'


def main():
    before={p:hashlib.sha256(p.read_bytes()).digest() for p in RES.rglob('*') if p.is_file()}
    bandits_080.generate()
    after={p:hashlib.sha256(p.read_bytes()).digest() for p in RES.rglob('*') if p.is_file()}
    assert before==after, '0.8.0 generator is not reproducible'
    items=set(re.findall(r'(?:register|food|stew|egg)\(\s*"([a-z_]+)"',(R/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text()))
    recipes={}
    for p in (D/'recipes').glob('*.json'):
        doc=json.loads(p.read_text(encoding='utf-8'))
        if not doc['type'].startswith('slavicmyths:armorer_'):continue
        recipes[p.stem]=doc
        if doc['type'].endswith('shaped'):
            assert 1<=len(doc['pattern'])<=4
            assert len({len(row) for row in doc['pattern']})==1 and 1<=len(doc['pattern'][0])<=4
            assert set(''.join(doc['pattern']))-{' '}==set(doc['key'])
            ingredients=list(doc['key'].values())
        else:
            ingredients=doc['ingredients'];assert 1<=len(ingredients)<=16
        for entry in ingredients+[doc['result']]:
            name=entry['item']
            if name.startswith('slavicmyths:'):assert name[12:] in items,name
        assert 1<=doc['result'].get('count',1)<=64
    assert len(recipes)==12
    for part in ['helmet','chestplate','leggings','boots']:
        recipe=recipes['armorer_chainmail_'+part]
        assert recipe['result']['item']=='minecraft:chainmail_'+part
        assert recipe['key']=={'R':{'item':'slavicmyths:iron_rings'}}
    assert recipes['iron_rings']['result']['count']==2
    definitions={}
    for p in (D/'structures/bandit').glob('*.nbt'):
        doc=read_nbt(p.read_bytes());definitions[p.stem]=doc
        assert doc['DataVersion']==2586 and doc['entities']==[]
        blocks={tuple(b['pos']):doc['palette'][b['state']]['Name'] for b in doc['blocks']}
        assert len(blocks)==len(doc['blocks'])
        for block in doc['blocks']:
            assert all(0<=a<b for a,b in zip(block['pos'],doc['size']))
            data=block.get('nbt',{})
            if not data:continue
            assert blocks[tuple(block['pos'])]=='minecraft:structure_block' and data['mode']=='DATA'
            marker=data['metadata']
            assert marker=='loot:camp' or re.fullmatch(r'spawn:(fighter|archer|heavy|senior|ataman):[01]',marker),marker
            if marker.startswith('spawn:'):
                x,y,z=block['pos']
                assert blocks.get((x,y-1,z)) not in (None,'minecraft:air','minecraft:water'), (p.name,'unsupported spawn',block['pos'])
                assert blocks.get((x,y+1,z))=='minecraft:air',(p.name,'blocked headroom',block['pos'])
    assert len(definitions)==10
    source=(R/'src/main/java/org/slavicmyths/bandit/CampStructure.java').read_text()
    medium=source.split('if(medium){',1)[1].split('}else{',1)[0]
    small=source.split('}else{',1)[1].split('for(Object[] p:plan)',1)[0]
    for size,text,total in [('small',small,5),('medium',medium,9)]:
        roster={}
        for name,x,z,first in re.findall(r'new Object\[\]\{"([a-z_]+)",(\d+),(\d+),(-?\d+)\}',text):
            for block in definitions[name]['blocks']:
                marker=block.get('nbt',{}).get('metadata','')
                if marker.startswith('spawn:'):
                    _,role,local=marker.split(':');index=int(first)+int(local)
                    assert index not in roster,(size,'duplicate roster member',index)
                    roster[index]=role
        assert set(roster)==set(range(total)),(size,roster)
        assert list(roster.values()).count('ataman')==(size=='medium')
    def visit(node):
        if isinstance(node,dict):
            if node.get('type')=='minecraft:item' and node.get('name','').startswith('slavicmyths:'):assert node['name'][12:] in items
            for value in node.values():visit(value)
        elif isinstance(node,list):
            for value in node:visit(value)
    for p in list((D/'loot_tables/chests').glob('bandit_*.json'))+[D/f'loot_tables/entities/{n}.json' for n in ['bandit_fighter','bandit_archer','bandit_heavy','bandit_senior','ataman']]:visit(json.loads(p.read_text()))
    geometries=[]
    for name in ['wooden_mace','stone_mace','mace','silver_mace','flail']:
        doc=json.loads((RES/f'assets/slavicmyths/models/item/{name}.json').read_text())
        geometries.append([(e['from'],e['to'],e.get('rotation')) for e in doc['elements']])
        for e in doc['elements']:
            assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to']))
            assert 'faces' in e
    assert len({json.dumps(g,sort_keys=True) for g in geometries})==5,'Recolored weapon geometry'
    with zipfile.ZipFile(R/'build/libs/slavicmyths-0.8.0.jar') as jar:
        for path in RES.rglob('*'):
            if path.is_file() and path.name!='mods.toml':assert jar.read(path.relative_to(RES).as_posix())==path.read_bytes(),path
        for name in jar.namelist():
            if name.endswith('.class') and not name.startswith('org/slavicmyths/compat/'):
                assert b'mezz/jei' not in jar.read(name),'JEI leaked into common classes'
    print('PASS: 12 data-driven 4x4 recipes, vanilla chainmail outputs, 10 camp NBTs, exact 5/9-member rosters, one Ataman, marker headroom, loot references, five distinct weapon meshes, reproducible resources including OGG, optional JEI isolation and production resource equality.')


if __name__=='__main__':main()
