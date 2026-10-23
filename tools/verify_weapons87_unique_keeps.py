"""Preserve original art plus exact separately documented P41/P42/P43/P49 changes."""
from pathlib import Path
import hashlib,json,struct,subprocess
from weapons87_p41_overlay import ADDITIONS as P41
from weapons87_p42_models import FINIST
from weapons87_p43_overlay import ADDITIONS as P43
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'src/main/resources/assets/slavicmyths'
MANIFEST=ROOT/'docs/implementation/weapons87/unique-source-review/source-manifest.json'
IDS=('finist_bow','thunder_axe','thunder_spear')
BASE_COMMIT='ad9897b0eb4c5bb63462484d63a31e6593e05e4b'
rows={row['id']:row for row in json.loads(MANIFEST.read_text(encoding='utf-8'))}
total=0
stale_snapshot=0
for id in IDS:
    item=rows[id]
    for entry in item['files']:
        file=ROOT/entry['path']
        assert file.is_file(),file
        raw=file.read_bytes()
        original=subprocess.run(['git','show',BASE_COMMIT+':'+entry['path']],cwd=ROOT,check=True,capture_output=True).stdout
        if file.suffix=='.json':
            expected=json.loads(original);actual=json.loads(raw)
            if id=='finist_bow':expected['display']=FINIST
            elif id=='thunder_axe':
                expected['elements']+=P41['thunder_axe']
                gui=expected['display']['gui'];ratio=.65/gui['scale'][0];gui['scale']=[.65]*3;gui['translation']=[v*ratio for v in gui['translation']]
            elif id=='thunder_spear':
                expected['elements']+=P43['thunder_spear']
                for index in (10,13,16,19,22):expected['elements'][index]['faces'].pop('west')
            assert actual==expected,(id,str(file),'differs beyond exact documented overlays')
        else:assert raw==original,(id,str(file),'original texture changed')
        if hashlib.sha256(raw).hexdigest()!=entry['sha256']:stale_snapshot+=1
        total+=1
    root=json.loads((BASE/'models/item'/f'{id}.json').read_text(encoding='utf-8'))
    if id=='finist_bow':
        assert root['parent']=='minecraft:item/bow'
        names=[p['model'] for p in root.get('overrides',[]) if p.get('predicate',{}).get('pulling')==1]
        assert len(names)==3 and len(set(names))==3,names
        for n in names:
            model=json.loads((BASE/'models'/(n.split(':')[1]+'.json')).read_text(encoding='utf-8'))
            assert model['parent']=='minecraft:item/bow'
            key=model['textures']['layer0'].split(':')[1]+'.png'
            tex=BASE/'textures'/key
            assert tex.is_file()
            head=tex.read_bytes()
            assert head[:8]==b'\x89PNG\r\n\x1a\n'
    else:
        cubes=root['elements']
        assert len(cubes)==(30 if id=='thunder_axe' else 37)
        minimum=[min(e['from'][i] for e in cubes) for i in range(3)]
        maximum=[max(e['to'][i] for e in cubes) for i in range(3)]
        span=[maximum[i]-minimum[i] for i in range(3)]
        if id=='thunder_axe':
            assert span[0]>=13 and 25<=span[1]<=27 and span[2]>=2
        else:
            assert 3.5<=span[0]<=5 and span[1]==40 and span[2]>=2
        for hand in ('firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand'):
            assert hand in root.get('display',{})
print(f'PASS {total} original art files plus exact P41/P42/P43/P49 deltas; Finist three pull states; axe/spear distinct. Old archived manifest stale for {stale_snapshot} files (historical, NOT rewritten). Runtime NOT_RUN')
