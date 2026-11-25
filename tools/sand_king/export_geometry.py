import gzip, struct, math
from pathlib import Path
import os
import numpy as np
import prototype as p
ROOT=Path(__file__).resolve().parents[2]
OUT=Path(os.environ.get('SAND_KING_RESOURCE_ROOT', ROOT/'src/main/resources'))/'assets/slavicmyths/sand_king/geometry.bin.gz'
OUT.parent.mkdir(parents=True,exist_ok=True)
ACTIONS=[('idle',1),('walk',33),('sting',41),('claw',41),('jump',41),('combo',61),('slam',81)]
base=p.build(); groups=[]
def part(g):
    return 'head' if g.startswith('horn') or g in ('mouth','eye','head') else g
for v,c,g in base:
    if part(g) not in groups: groups.append(part(g))
assert len(base)==327
assert np.allclose(np.ptp(np.concatenate([v for v,c,g in base]),axis=0),[9,12,7])
with gzip.GzipFile(filename='',mode='wb',fileobj=OUT.open('wb'),mtime=0) as f:
    def i(v): f.write(struct.pack('>i',v))
    i(0x534B5632);i(len(base));i(len(groups));i(len(ACTIONS))
    for g in groups:
        b=g.encode('ascii');f.write(struct.pack('>H',len(b)));f.write(b)
    for v,c,g in base:
        f.write(struct.pack('>Ih',0xff000000|int(c[1:],16),groups.index(part(g))))
    for a,n in ACTIONS:
        i(n)
        for t in np.linspace(0,1,n):
            cubes=p.build(a,t)
            assert [(c,g) for v,c,g in cubes]==[(c,g) for v,c,g in base]
            vs=np.array([v for v,c,g in cubes])
            if a=='walk': vs[:,:,1]-=.6*(.5-t)*p.SCALE[1]
            if a=='jump':
                vs[:,:,1]-=-2.2*t*p.SCALE[1];vs[:,:,2]-=2.35*math.sin(math.pi*t)*p.SCALE[2]
            vs=vs[:,:,[0,2,1]]*[-1,1,-1]
            assert np.isfinite(vs).all()
            f.write(vs.astype('>f4').tobytes())
print(f'{len(base)} cuboids, {len(groups)} hit groups, {sum(n for a,n in ACTIONS)} keyframes: {OUT.stat().st_size} bytes')
