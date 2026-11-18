"""Validate real exported geometry/UV/poses and protected boss assets."""
from pathlib import Path
import gzip,json,math,hashlib
R=Path(__file__).resolve().parents[1];D=R/'docs/implementation/mob332'
AFTER=(D/'latest-after.txt').read_text().strip() if (D/'latest-after.txt').exists() else 'after'
def load(n):return json.loads(gzip.decompress((D/AFTER/(n+'.json.gz')).read_bytes()))
def main():
    models=[]
    for p in sorted((D/AFTER).glob('*.json.gz')):
        name=p.name.removesuffix('.json.gz');d=load(name)
        for f in d['frames']:
            assert f['faces'],(name,'empty pose')
            for face in f['faces']:
                assert len(face)==4
                for v in face:assert all(math.isfinite(x) for x in v) and 0<=v[3]<=1 and 0<=v[4]<=1,(name,'UV/finite')
        models.append({'id':name,'frames':len(d['frames']),'states':sorted({f['state'] for f in d['frames']}),'parts':len(d['frames'][0]['parts'])})
    assert len(models)==19
    for n,prefixes in [('bone_crawler',[('shin_left',4),('shin_right',4),('toe_left',4),('toe_right',4)]),('coal_crawler',[('shin',4),('foot',4)])]:
        frames=[f for f in load(n)['frames'] if f['state']=='walk']
        for prefix,count in prefixes:
            for i in range(count):assert len({tuple(round(v,4) for v in f['parts'][prefix+str(i)][3:6]) for f in frames})>12,(n,prefix,i,'static joint')
    protected=json.loads((D/'boss-resource-parity.json').read_text())
    for p,sha in protected.items():assert hashlib.sha256((R/p).read_bytes()).hexdigest()==sha,('boss resource changed',p)
    report={'native_geometry':'PASS','variants':models,'boss_files_unchanged':len(protected),'runtime_launches':0,'runtime':'NOT_RUN','original_art':'BLOCKED_REFERENCE','limits':'Finite vertices and changing joints do not prove actual world-ground contact, render FPS or combat correctness.'}
    (D/'native-validation.json').write_text(json.dumps(report,indent=2)+'\n');print('PASS 19 native variants, moving bone/coal knees/feet, finite UV, unchanged boss resources. Runtime NOT_RUN.')
if __name__=='__main__':main()
