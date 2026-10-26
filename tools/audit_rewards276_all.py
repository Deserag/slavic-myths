"""Read-only production inventory of mod overworld sites, templates and dynamic
reward paths. Writes reports only; never starts Minecraft or generators."""
from pathlib import Path
import collections,csv,json,re,gzip,io,struct
def read_nbt(raw):
    stream=io.BytesIO(gzip.decompress(raw) if raw[:2]==b'\x1f\x8b' else raw)
    def number(fmt):return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
    def string():return stream.read(number('H')).decode('utf8')
    def value(kind):
        if kind in [1,2,3,4,5,6]:return number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind])
        if kind==7:return list(stream.read(number('i')))
        if kind==8:return string()
        if kind==9:
            child=number('B');return [value(child) for _ in range(number('i'))]
        if kind==10:
            result={}
            while True:
                child=number('B')
                if child==0:return result
                key=string();result[key]=value(child)
        if kind in [11,12]:return [number('i' if kind==11 else 'q') for _ in range(number('i'))]
        raise AssertionError(kind)
    assert number('B')==10;string();out=value(10);assert not stream.read();return out
R=Path(__file__).resolve().parents[1];D=R/'src/main/resources/data/slavicmyths';J=R/'src/main/java/org/slavicmyths';O=R/'docs/implementation/loot276-all-20261026'
def run():
    O.mkdir(parents=True,exist_ok=True);templates={}
    for p in (D/'structure').rglob('*.nbt'):
        doc=read_nbt(p.read_bytes());records=[]
        for b in doc.get('blocks',[]):
            n=b.get('nbt',{});state=doc['palette'][b['state']];name=state['Name']
            container=any(t in name for t in ['chest','barrel','burial_coffin','cabinet','cupboard','shulker_box','dispenser','dropper','hopper','furnace','bloomery','refining_hearth'])
            if container or 'LootTable' in n or 'Items' in n or n.get('metadata','').startswith(('loot:','cache','heart','pool','reward')):
                records.append(dict(pos=b['pos'],block=name,table=n.get('LootTable'),items=n.get('Items',[]),marker=n.get('metadata'),container_block=container))
        if records:templates[p.relative_to(D/'structure').as_posix()[:-4]]=dict(size=doc.get('size'),contents=records)
    definitions={p.stem:json.loads(p.read_text()) for p in (D/'worldgen/structure').glob('*.json')}
    sources={p.relative_to(J).as_posix():p.read_text(encoding='utf8') for p in J.rglob('*.java')}
    routes={f:[dict(line=i,text=l.strip()) for i,l in enumerate(s.splitlines(),1) if any(x in l for x in ['setLootTable(', 'LootTable"','chest.setItem(','.food.set('])] for f,s in sources.items()}
    routes={f:v for f,v in routes.items() if v}
    (O/'template-inventory.json').write_text(json.dumps(templates,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    (O/'source-loot-routes.json').write_text(json.dumps(routes,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    (O/'registered-structures.json').write_text(json.dumps(definitions,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    for family in ['sanctuaries/fire/','sanctuaries/spring/','swamp/','chud/','gorodishche/','village/']:
        found={k:v for k,v in templates.items() if k.startswith(family)};counts=collections.Counter()
        for v in found.values():
            for x in v['contents']:counts[x['table'] or x['marker'] or 'DIRECT_ITEMS']+=1
        print(family,'templates',len(found),json.dumps(dict(counts),ensure_ascii=True)[:1400])
    print('dynamic source routes',','.join(routes))
    for id in ['fire_regular','fire_cache','fire_guaranteed','spring_regular','spring_cache','spring_guaranteed','serpent_fortress','ancient_shrine','abandoned_mine','abandoned_treasury','chud_storehouse','bathhouse','old_barn']:
        d=json.loads((D/'loot_table/chests'/(id+'.json')).read_text());print(id,[(p['rolls'],[(e.get('name',e.get('value')),e.get('weight',1)) for e in p['entries']]) for p in d['pools']])
if __name__=='__main__':run()
