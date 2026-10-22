"""Proof that accepted Finist bow, thunder axe and thunder spear art stays byte-identical."""
from pathlib import Path
import hashlib,json,struct,subprocess
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'src/main/resources/assets/slavicmyths'
MANIFEST=ROOT/'docs/implementation/weapons87/unique-source-review/source-manifest.json'
IDS=('finist_bow','thunder_axe','thunder_spear')
BASE='ad9897b0eb4c5bb63462484d63a31e6593e05e4b'
rows={row['id']:row for row in json.loads(MANIFEST.read_text(encoding='utf-8'))}
total=0
stale_snapshot=0
for id in IDS:
    item=rows[id]
    for entry in item['files']:
        file=ROOT/entry['path']
        assert file.is_file(),file
        raw=file.read_bytes()
        original=subprocess.run(['git','show',BASE+':'+entry['path']],cwd=ROOT,check=True,capture_output=True).stdout
        assert raw==original, (id,str(file),'differs from exact pre-branch main')
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
        assert len(cubes)==(24 if id=='thunder_axe' else 30)
        minimum=[min(e['from'][i] for e in cubes) for i in range(3)]
        maximum=[max(e['to'][i] for e in cubes) for i in range(3)]
        span=[maximum[i]-minimum[i] for i in range(3)]
        if id=='thunder_axe':
            assert span[0]>=13 and 25<=span[1]<=27 and span[2]>=2
        else:
            assert 3.5<=span[0]<=5 and span[1]==40 and span[2]>=2
        for hand in ('firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand'):
            assert hand in root.get('display',{})
print(f'PASS {total} unchanged JSON/PNG files against pinned main; Finist three pull states; axe/spear distinct. Old archived manifest stale for {stale_snapshot} files (historical, NOT rewritten). Runtime NOT_RUN')
