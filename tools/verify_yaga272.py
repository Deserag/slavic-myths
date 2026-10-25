"""#272 scope transfer and resource/JAR audit. Does not claim runtime delivery."""
from pathlib import Path
import hashlib
import json
import subprocess
import zipfile
from yaga272_strings import TEXT
from verify_rituals273 import OWNED as RITUALS273
from rituals273_text import expected as rituals273_text
from verify_performance274 import OWNED as PERFORMANCE274, scope as performance274_scope

R=Path(__file__).resolve().parents[1]
BASE='0072c2f7'
J='src/main/java/org/slavicmyths/'
OWNED=['bandit/NightingaleEntity.java','client/TaleScreen.java','hunt/WorldBoss.java',
       'tales/TaleService.java','tales/TaleSnapshotMerge.java','yaga/BabaYaga.java',
       'yaga/YagaData.java','yaga/YagaDiscovery.java','yaga/YagaServices.java',
       'yaga/YagaStoryProgress.java','progression/BossVictoryCredit.java',
       'village/mixin/YagaGameModeMixin.java']
RESOURCES=['assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','slavicmyths.work.mixins.json']

def source(p):return (R/J/p).read_text(encoding='utf-8')
def before(p):return subprocess.check_output(['git','show',BASE+':'+p],cwd=R).decode('utf-8').replace('\r\n','\n')

def scope():
    performance274_scope()  # Explicit #274 transfer; all other production files pinned to #272 HEAD.
    # Every existing production file outside this explicit package must match #275.
    tracked=subprocess.check_output(['git','diff',BASE,'--name-only','--','src/main'],cwd=R,text=True).splitlines()
    untracked=subprocess.check_output(['git','ls-files','--others','--exclude-standard','src/main'],cwd=R,text=True).splitlines()
    for path in tracked+untracked:
        if path.startswith(J) and path[len(J):] in OWNED+PERFORMANCE274+RITUALS273:continue
        if path.startswith('src/main/resources/') and path[len('src/main/resources/'):] in RESOURCES:continue
        raise AssertionError(('unowned production change',path))
    # Existing entities keep combat/AI/loot byte-for-byte apart from the reviewed death credit lines.
    for p in ['hunt/WorldBoss.java','bandit/NightingaleEntity.java']:
        old=before(J+p).splitlines();new=source(p).splitlines()
        assert len(old)==len(new)
        changed=[(a,b) for a,b in zip(old,new) if a!=b]
        assert len(changed)==1 and 'BossVictoryCredit.winner(this,source)' in changed[0][1],p
        if p.startswith('hunt/'):
            a,b=changed[0]
            start=a.index('if(advancementFinished&&')
            assert b[:start]==a[:start] and '(encounter.isEmpty()||advancementFinished)&&winner!=null' in b
        else:
            a,b=changed[0]
            assert b==a+'var winner=org.slavicmyths.progression.BossVictoryCredit.winner(this,source);if(winner!=null)participants.add(winner.getUUID());'
    assert before(J+'yaga/YagaData.java').replace('return w.getDataStorage().computeIfAbsent','return w.getServer().overworld().getDataStorage().computeIfAbsent')==source('yaga/YagaData.java')
    assert before(J+'yaga/BabaYaga.java').replace('  if(!eggSummoned&&progress.intro==0)','  YagaServices.recognizeVictory(p);\n  if(!eggSummoned&&progress.intro==0)')==source('yaga/BabaYaga.java')
    # Actual stage/reward catalog and remaining quest mechanics are unchanged.
    a=before(J+'yaga/YagaServices.java');b=source('yaga/YagaServices.java')
    assert a[:a.index(' public static void recognizeVictory')]==b[:b.index(' /** The same transaction')]
    assert a[a.index(' public static boolean killed'):]==b[b.index(' public static boolean killed'):]
    for lang,index in [('ru_ru',0),('en_us',1)]:
        path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json'
        old=json.loads(before(path));new=json.loads((R/path).read_text(encoding='utf-8'))
        allowed=rituals273_text(old,index)
        assert all(new.get(k)==v for k,v in old.items() if k not in allowed),'old strings changed'
        assert set(new)-set(old)=={'yaga.search.'+k for k in TEXT}|(set(allowed)-set(old))
        for key,pair in TEXT.items():assert new['yaga.search.'+key]==pair[index]
    path='src/main/resources/slavicmyths.work.mixins.json'
    old=json.loads(before(path));new=json.loads((R/path).read_text());new['mixins'].remove('YagaGameModeMixin');assert old==new
    for p in OWNED:
        if not p.startswith('client/'):assert 'net.minecraft.client' not in source(p),p
    mixin=source('village/mixin/YagaGameModeMixin.java')
    assert '@At("RETURN")' in mixin and 'ci.getReturnValueZ()&&p.isCreative()' in mixin
    yaga=source('yaga/YagaDiscovery.java')
    assert 'TickTask' not in yaga and 'YagaHut.beginPlace' in yaga and 'new SiteChunks(world)' in yaga
    assert 'YagaStoryProgress.delta' in yaga and 'YagaDiscovery.refresh(p)' in source('tales/TaleEvents.java')
    assert 'TaleSnapshotMerge.merge' in source('client/TaleScreen.java')
    assert not any(s in source('yaga/YagaStoryProgress.java') for s in ['Knowledge.award','grantProgress','recognizeVictory('])
    return True

def main():
    scope()
    props=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar')
    with zipfile.ZipFile(jar) as z:
        for p in OWNED:assert 'org/slavicmyths/'+p.replace('.java','.class') in z.namelist(),p
        assert not any('Offline' in p or p.startswith('tools/') for p in z.namelist()),'fixture packaged'
        for p in RESOURCES:assert z.read(p)==(R/'src/main/resources'/p).read_bytes(),p
        for name in z.namelist():
            if name.endswith('.class'):assert int.from_bytes(z.read(name)[6:8],'big')==65,('not Java21',name)
    receipt={'baseline':BASE,'branch':'hell','version':props['mod_version'],'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sources':{J+p:hashlib.sha256((R/J/p).read_bytes()).hexdigest() for p in OWNED},'minecraft_launches':0,'runtime':'NOT_RUN','polymc':'POLYMC_NOT_INSTALLED','issue':'OPEN / OWNER_PENDING'}
    (R/'build/yaga272-current-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print('YAGA272_SOURCE_JAR_PASS',receipt['sha256'],'GAMEPLAY_NOT_RUN')

if __name__=='__main__':main()
