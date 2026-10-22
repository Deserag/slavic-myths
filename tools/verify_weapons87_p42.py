"""P42 production assets & canonical replay checks, no claim of Minecraft acceptance."""
from pathlib import Path
import hashlib,json,shutil,sys,tempfile,zipfile
import weapons87_p42_models as p42
from weapons87_grips import plan

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
MODELS=ASSETS/'models/item'
STATES=p42.STATES
p42.check()
samples=[]
for item,extra,base in [('serpent_crossbow',5,8),('three_breath_crossbow',3,65)]:
    normal=None
    for suffix in STATES:
        key=item+suffix
        model=p42.read(key)
        assert len(model['elements'])==base+extra+(1 if item=='serpent_crossbow' and suffix in ('_arrow','_firework') else 0)+(3 if item=='three_breath_crossbow' and suffix in ('_arrow','_firework') else 0)
        hand={k:v for k,v in model['display'].items() if k.startswith(('thirdperson_','firstperson_'))}
        if normal is None:normal=hand
        else:assert hand==normal,(key,'hand transform mismatch')
        assert model['overrides'] if not suffix else True
        for e in p42.ADDITIONS[item]:
            assert all(a<b for a,b in zip(e['from'],e['to']))
            for face in e['faces'].values():
                assert face['texture'][1:] in model['textures']
                assert len(face['uv'])==4 and all(0<=n<=16 for n in face['uv'])
        samples.append(key)
assert len(samples)==12
base=p42.read('serpent_crossbow')
late=p42.read('three_breath_crossbow')
assert len(base['elements'])==13 and len(late['elements'])==68
# The original authored three-head geometry is retained, not replaced with a giant new crest.
assert all(e['from'][2]>=3.5 for e in p42.ADDITIONS['three_breath_crossbow'])
for id,n,scale in [('karachun_scythe_held',60,.7),('morozko_staff_held',66,.74)]:
    d=json.loads((MODELS/(id+'.json')).read_text(encoding='utf-8'))
    assert len(d['elements'])==n
    expected=plan()[id]['display']
    for context in ('thirdperson_righthand','thirdperson_lefthand'):
        assert d['display'][context]==expected[context],(id,context)
        assert d['display'][context]['scale']==[scale]*3
    assert d['display']['firstperson_righthand']==expected['firstperson_righthand']
    assert d['display']['firstperson_lefthand']==expected['firstperson_lefthand']
    assert (MODELS/(id.removesuffix('_held')+'.json')).is_file()
assert p42.read('finist_bow')['overrides']==[
    {'predicate':{'pulling':1,'pull':0},'model':'slavicmyths:item/finist_bow_pulling_0'},
    {'predicate':{'pulling':1,'pull':.65},'model':'slavicmyths:item/finist_bow_pulling_1'},
    {'predicate':{'pulling':1,'pull':.9},'model':'slavicmyths:item/finist_bow_pulling_2'},
]
for suffix in ('','_pulling_0','_pulling_1','_pulling_2'):
    d=p42.read('finist_bow'+suffix)
    assert d['textures']['layer0']=='slavicmyths:item/finist_bow'+suffix
    assert (ASSETS/'textures/item'/('finist_bow'+suffix+'.png')).is_file()
# Replay P42 twice on an isolated copy. Never apply a generator to user's actual checkout.
paths=[MODELS/(item+suffix+'.json') for item in p42.ADDITIONS for suffix in STATES]
paths.extend(MODELS/('finist_bow'+suffix+'.json') for suffix in ('','_pulling_0','_pulling_1','_pulling_2'))
with tempfile.TemporaryDirectory(prefix='weapons87-p42-') as temp:
    sandbox=Path(temp)
    for path in paths:shutil.copy2(path,sandbox/path.name)
    original=p42.MODELS
    try:
        p42.MODELS=sandbox
        baseline={x.name:hashlib.sha256(x.read_bytes()).hexdigest() for x in sandbox.iterdir()}
        p42.apply();p42.apply()
        assert baseline=={x.name:hashlib.sha256(x.read_bytes()).hexdigest() for x in sandbox.iterdir()}
    finally:p42.MODELS=original
if '--jar' in sys.argv:
    jars=list((ROOT/'build/libs').glob('slavicmyths-*.jar'))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        for path in paths+[MODELS/'karachun_scythe_held.json',MODELS/'morozko_staff_held.json']:
            relative=path.relative_to(ROOT/'src/main/resources').as_posix()
            assert z.read(relative)==path.read_bytes(),relative
print('PASS P42: 18 model states, both crossbows, scythe, Morozko, Finist, idempotent isolated replay; runtime NOT_RUN')
