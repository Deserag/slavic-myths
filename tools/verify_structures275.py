from verify_rituals273 import OWNED as RITUALS273
"""#275 ownership/preservation/JAR receipt. Does not assert live structure generation."""
from pathlib import Path
import subprocess,json,hashlib,zipfile
from verify_performance274 import OWNED as PERFORMANCE274
from verify_yaga272 import OWNED as YAGA272, scope as yaga272_scope
R=Path(__file__).resolve().parents[1];BASE='7d83da1a';J='src/main/java/org/slavicmyths/'
OWNED=['bayun/BayunCreation.java','bayun/BayunFeature.java','bayun/BayunHomes.java','bayun/BayunPlacement.java','chud/ChudCreationJobs.java','guides/GuideDiscovery.java','worldgen/ManualStructureRecords.java','worldgen/SiteChunks.java','yaga/YagaPlacement.java','yaga/YagaHut.java','navya/NavyaHeartStructure.java']
def source(p):return (R/J/p).read_text(encoding='utf-8')
def before(p):return subprocess.check_output(['git','show',BASE+':'+J+p],cwd=R).decode('utf-8').replace('\r\n','\n')
def main():
    changed=subprocess.check_output(['git','diff',BASE,'--name-only','--','src/main/java'],cwd=R,text=True).splitlines()
    assert set(changed)<=set(J+p for p in OWNED+YAGA272+PERFORMANCE274+RITUALS273),changed
    yaga272_scope()  # Explicit #272 transfer; all #275 geometry/resources still pinned to 0072c2f7.
    preserved=['firebird/FirebirdEncounter.java','firebird/FirebirdRules.java','hunt/HuntSpawns.java','sivka/SivkaContent.java','sivka/FirstSivkaProgress.java','sivka/SivkaExploration.java','wolf/WolfEncounter.java','wolf/EncounterRetryCache.java','world/EncounterPlacement.java','yaga/YagaDiscovery.java','yaga/YagaData.java','yaga/YagaServices.java','chud/ChudSimplePlacement.java','chud/ChudPlacement.java','chud/ChudTerrainPolicy.java','worldgen/LargeSiteJobs.java','navya/NavyaHeartPiece.java']
    for p in preserved:
        if p in YAGA272+PERFORMANCE274:continue
        assert before(p)==source(p),('unowned regression',p)
    old='source.sendSuccess(()->Component.literal(message),false);for(var callback:listeners)'
    new='if(result==null)source.sendFailure(Component.literal(message));else source.sendSuccess(()->Component.literal(message),false);for(var callback:listeners)'
    # Chud worker lifecycle/raster selection now owned by #274, checked against #272 scope and native equivalence fixture.
    site=source('worldgen/SiteChunks.java')
    for token in ['ACTIVE.add(this)','ACTIVE.remove(this)','List.copyOf(ACTIVE)','owner.close()','NEXT_TOKEN.incrementAndGet()','requested.size()>=4','getChunkNow']:
        assert token in site,token
    assert '.join(' not in site and 'getChunk(' not in site
    bayun=source('bayun/BayunCreation.java')
    for token in ['new YagaPlacement.Cursor','cursor.abort()','BayunHomes.get(world).forget','ManualStructureRecords.get(world).remove(token)','"site/bayun"','GuideDiscovery.refresh(p)','System.nanoTime()>deadline','candidate>=candidates.size()','candidates.addAll(loaded)']:
        assert token in bayun,token
    assert 'dy<=24' not in bayun and 'phase==2)world.setBlock' not in bayun
    assert 'YagaPlacement.commit' in source('bayun/BayunFeature.java') and 'BayunPlacement.prepare' in source('bayun/BayunFeature.java')
    records=source('worldgen/ManualStructureRecords.java')
    for family in ['serpent_hill','sanctuary_unfading_fire','abode_pure_spring']:assert 'family.equals("'+family+'")' in records
    for p in OWNED:assert 'net.minecraft.client' not in source(p),p
    props=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar')
    with zipfile.ZipFile(jar) as z:
        for p in OWNED:assert 'org/slavicmyths/'+p.replace('.java','.class') in z.namelist(),p
        assert not any('Structures275Offline' in n for n in z.namelist()),'offline fixture packaged'
    receipt={'baseline':BASE,'branch':'hell','jar':str(jar),'version':props['mod_version'],'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'source_files':{J+p:hashlib.sha256((R/J/p).read_bytes()).hexdigest() for p in OWNED},'minecraft_launches':0,'gameplay':'NOT_RUN','polymc':'NOT_INSTALLED'}
    (R/'build/structures275-current-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print('PASS #275 source scope/preservation/JAR; GAMEPLAY_NOT_RUN; SHA256',receipt['sha256'])
if __name__=='__main__':main()
