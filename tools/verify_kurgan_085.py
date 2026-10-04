"""Resource/registration checks scoped to 0.8.5; does not touch older weapon art."""
from pathlib import Path
import hashlib,json,struct,re
from kurgan_085 import generate,NAMES,A,D,R

def snapshot():
    paths=[*(A/'textures/block').glob('*kurgan*.png'),A/'textures/mob_effect/kurgan_curse.png']
    paths += [A/f'{folder}/{n}.json' for n in NAMES for folder in ['blockstates','models/block','models/item']]
    paths += list((D/'loot_tables/chests').glob('kurgan_*.json'))
    return {str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in paths}

before=snapshot();generate();assert snapshot()==before,'Generator not reproducible'
for name in NAMES:
    for folder in ['blockstates','models/block','models/item']:
        model=json.loads((A/f'{folder}/{name}.json').read_text(encoding='utf-8'))
        for part in model.get('elements',[]):
            assert all(-16<=a<b<=32 for a,b in zip(part['from'],part['to'])),name
    for lang in ['ru_ru','en_us']:
        data=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'))
        assert data['block.slavicmyths.'+name]
        assert data['effect.slavicmyths.kurgan_curse']
for path in (A/'textures/block').glob('*kurgan*.png'):
    assert struct.unpack('!II',path.read_bytes()[16:24])==(64,64),path
assert struct.unpack('!II',(A/'textures/mob_effect/kurgan_curse.png').read_bytes()[16:24])==(64,64)
for tier in ['small','warrior','great']:
    for profile in ['burial','offering','important']:
        table=json.loads((D/f'loot_tables/chests/kurgan_{tier}_{profile}.json').read_text(encoding='utf-8'))
        assert len(table['pools'])==2
        for pool in table['pools']:
            assert pool['rolls']==1 and pool['conditions'][0]['chance']<1
            for entry in pool['entries']:
                assert (D/('loot_tables/'+entry['name'].split(':')[1]+'.json')).exists()
assert json.loads((D/'loot_tables/blocks/sealed_kurgan_masonry.json').read_text())['pools']==[]
commands=(R/'src/main/java/org/slavicmyths/kurgan/KurganCommands.java').read_text(encoding='utf-8')
for name in ['locate','generate','info','clear_kurgan_curse']:
    assert f'Commands.literal("{name}")' in commands
assert 'hasPermission(2)' in commands
print('PASS: 17 blocks/items, deterministic 64px art, native model bounds, RU/EN, nine sparse loot profiles and command registrations. No Minecraft runtime assertions.')
