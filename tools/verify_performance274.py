"""#274 explicit scope transfer. Source/JAR audit, never runtime evidence."""
from pathlib import Path
import subprocess, hashlib, json, zipfile
from verify_rituals273 import OWNED as RITUALS273, LANG as RITUALS273_LANG, scope as rituals273_scope
R=Path(__file__).resolve().parents[1];BASE='b34f602421f2d300280a39ea819ececd483b5455';J='src/main/java/org/slavicmyths/'
OWNED=['chud/ChudCreationJobs.java','chud/ChudGeometry.java','serpenthill/HillPlan.java','worldgen/LargeSiteJobs.java','worldgen/ManualStructureRecords.java','worldgen/OnDemandStructures.java','worldgen/SiteChunks.java','worldgen/TemplateSiteJobs.java','yaga/YagaPlacement.java']
def scope():
    rituals273_scope()  # Explicit subsequent #273 transfer; all other production remains pinned.
    changed=subprocess.check_output(['git','diff',BASE,'--name-only','--','src/main'],cwd=R,text=True).splitlines()
    untracked=subprocess.check_output(['git','ls-files','--others','--exclude-standard','src/main'],cwd=R,text=True).splitlines()
    assert set(changed+untracked)<=set(J+p for p in OWNED+RITUALS273)|set(RITUALS273_LANG),('outside #274',changed,untracked)
    for p in OWNED:assert 'net.minecraft.client' not in (R/J/p).read_text(),p
    for p in ['worldgen/SiteChunks.java','worldgen/LargeSiteJobs.java','worldgen/TemplateSiteJobs.java','chud/ChudCreationJobs.java','worldgen/OnDemandStructures.java']:
        assert 'LevelEvent.Unload' in (R/J/p).read_text(),p
    s=(R/J/'chud/ChudCreationJobs.java').read_text()
    for token in ['var selected=piece','cancelled::get','rasterCancelled.set(true)','columns.at(x,z)','ChudGeometry.checkCancelled(cancelled)']:assert token in s,token
    s=(R/J/'worldgen/SiteChunks.java').read_text();assert 'getChunk(' not in s and '.join(' not in s
    return True
if __name__=='__main__':
    scope()
    props=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar')
    with zipfile.ZipFile(jar) as z:
        assert not any('Offline' in p or p.startswith('tools/') for p in z.namelist())
        for p in OWNED:assert 'org/slavicmyths/'+p.replace('.java','.class') in z.namelist(),p
    receipt={'baseline':BASE,'branch':'hell','version':props['mod_version'],'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sources':{J+p:hashlib.sha256((R/J/p).read_bytes()).hexdigest() for p in OWNED},'minecraft_launches':0,'runtime':'NOT_RUN','polymc':'POLYMC_NOT_INSTALLED','issue':'OPEN / IN_PROGRESS / OWNER_PENDING'}
    (R/'build/performance274-current-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print('PASS #274 scope/preserved other production files/JAR; GAMEPLAY_NOT_RUN',receipt['sha256'])
