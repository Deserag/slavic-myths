"""Full trophy rework source/resource and assembled JAR gates; never game QA."""
from pathlib import Path
import hashlib, json, sys, zipfile, subprocess
from PIL import Image
from trophy140_skim_parts import source

root=Path(__file__).resolve().parents[1]
report=root/'docs/implementation/trophy140-rework'
protected=json.loads((report/'protected-source.json').read_text())
# Legacy protected SHA256 snapshot predates accepted current main. Keep the historical file,
# but pin every original boss rig and texture to exact current-main Git blob SHA1.
protected_main_blobs = {
    'src/main/java/org/slavicmyths/client/SkimMesh.java': '48697d475421342b4d80503cee2546234e3517d4',
    'src/main/java/org/slavicmyths/client/WorldBossModel.java': 'cf2cb934f1cc36dd990f1e9852935f4e54fdce47',
    'src/main/java/org/slavicmyths/client/WorldBossGeometry.java': '54774350f107b00966f949d99c6f9b07492aa747',
    'src/main/java/org/slavicmyths/client/BossReworkGeometry.java': 'db76760153b29ce43611f0e8d5b90fc0c97f67ff',
    'src/main/java/org/slavicmyths/client/CursedVolkolakModel.java': '52aac92ad1bba3b4daf587b6e1373e150cb58a21',
    'src/main/java/org/slavicmyths/client/UndergroundClient.java': '1485409684e33cde57fde33a8143b338bef7dfc3',
    'src/main/java/org/slavicmyths/client/PolozTrophyRenderer.java': '0da6f0205953b38c8c620c1d93fe7a5e289adcb4',
    'src/main/java/org/slavicmyths/poloz/PolozTrophy.java': '39d93a2ff8de6502346a58d7a5847a372aa430ca',
    'src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png': '456f76b852a0546bd4c73672fca5a5c7dd99992a',
    'src/main/resources/assets/slavicmyths/textures/entity/tugarin_phase2.png': 'dd8d881d688e9689003d5614595bebb135086e52',
    'src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak.png': '6dd4b46575cebe9fdaf9ce9fec4db6ad3532fdb2',
    'src/main/resources/assets/slavicmyths/textures/entity/likho_one_eyed.png': '48e55ed0b6032be7f8295d9f17c9784d4d6bd47f',
    'src/main/resources/assets/slavicmyths/textures/entity/mor/likho.png': 'e330c253a5f4ea0077d76b6cbff6b1e6bf0b8809',
    'src/main/resources/assets/slavicmyths/textures/entity/underground/large_podzemnik.png': '9b02620dcc26c01f4e9a3a9a4c595296f14ce8c4'
}
assert set(protected)==set(protected_main_blobs)
for name in protected:
    blob=subprocess.check_output(['git','hash-object',str(root/name)],text=True).strip()
    assert blob==protected_main_blobs[name],name
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
