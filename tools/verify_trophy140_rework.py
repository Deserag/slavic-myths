"""Full trophy rework source/resource and assembled JAR gates; never game QA."""
from pathlib import Path
import hashlib, json, sys, zipfile
from PIL import Image
from trophy140_skim_parts import source

root=Path(__file__).resolve().parents[1]
report=root/'docs/implementation/trophy140-rework'
protected=json.loads((report/'protected-source.json').read_text())
for name,digest in protected.items():
    assert hashlib.sha256((root/name).read_bytes()).hexdigest()==digest, name
assert (root/'src/main/java/org/slavicmyths/client/TrophySkimMesh.java').read_text()==source()
client=(root/'src/main/java/org/slavicmyths/client/TrophyHeadsClient.java').read_text()
block=(root/'src/main/java/org/slavicmyths/trophy/TrophyHeadBlock.java').read_text()
for token in ('TrophySkimMesh.create()', 'eyeLook(side,forward)',
              'model.trophyHeadPart("iris"),model.trophyHeadPart("pupil")',
              'earRight.zRot=earRightBaseZ-twitch', 'lid.z=lidBaseZ-',
              'lid.xScale=lidScaleX;', 'detail.y=detailBaseY;'):
    assert token in client,token
assert 'if(s.getValue(HALF)==DoubleBlockHalf.LOWER)breakBurst(w,p);' in block
assert 'server.sendParticles(particle' in block
assert '24,.325,height*.35,.325,.06' in block
assert 'w.addParticle' not in block # No client-predicted duplicate burst.
count=0
if '--jar' in sys.argv:
    jars=list((root/'build/libs').glob('slavicmyths-*.jar'))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as jar:
        for path in (root/'src/main/resources').rglob('*'):
            if not path.is_file():continue
            rel=path.relative_to(root/'src/main/resources').as_posix()
            expected=root/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else path
            assert jar.read(rel)==expected.read_bytes(),rel
            if path.suffix=='.json':json.loads(path.read_text(encoding='utf-8-sig'))
            if path.suffix=='.png':
                with Image.open(path) as im:im.verify()
            count+=1
        for cls in ('client/TrophySkimMesh','client/TrophyHeadsClient','trophy/TrophyHeadBlock'):
            assert 'org/slavicmyths/'+cls+'.class' in jar.namelist(),cls
        assert not any('Heads140Offline' in n or 'HeadPoseExport' in n or 'FinistChecks' in n or n.startswith(('tools/smoke/','org/slavicmyths/verify/')) for n in jar.namelist())
print(f'TROPHY140 REWORK source/protected originals/JAR PASS; resources={count}; gameplay NOT_RUN')
