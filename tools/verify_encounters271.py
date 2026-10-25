"""Scope and packaged-byte receipts; native behavioral checks are in the opt-in JVM fixture."""
from pathlib import Path
import subprocess,json,zipfile,hashlib
R=Path(__file__).resolve().parents[1];BASE='cecaa4b2';J='src/main/java/org/slavicmyths/'
OWNED=['firebird/FirebirdEncounter.java','hunt/HuntSpawns.java','sivka/SivkaContent.java','sivka/FirstSivkaProgress.java','sivka/SivkaExploration.java','wolf/WolfEncounter.java','wolf/EncounterRetryCache.java','world/EncounterPlacement.java']
def source(p):return (R/J/p).read_text(encoding='utf-8')
changed=subprocess.check_output(['git','diff',BASE,'--name-only','--','src/main/java'],cwd=R,text=True).splitlines()
assert set(changed)<=set(J+p for p in OWNED),changed
assert not subprocess.check_output(['git','diff',BASE,'--','src/main/resources'],cwd=R),'unexpected art/audio/biome/weight changes'
for p in ['hunt/WorldBoss.java','hunt/BossAnchors.java','hunt/BossRules.java','mor/MorController.java','mor/MorRules.java','mor/MorEvents.java','firebird/FirebirdEntity.java','firebird/FirebirdRules.java','yaga/YagaDiscovery.java','navya/NavyaSpawns.java','bayun/BayunFeature.java']:
    before=subprocess.check_output(['git','show',BASE+':'+J+p],cwd=R).decode().replace('\r\n','\n')
    assert before==source(p),('unowned regression',p)
for p in OWNED:assert 'net.minecraft.client' not in source(p),('common client dependency',p)
s=source('sivka/SivkaExploration.java')
for text in ['Stats.WALK_ONE_CM','Stats.HORSE_ONE_CM','old.player()==p','p.tickCount-old.tick()==20','progress.pending','!nearby.isEmpty()','d.attemptAt==w.getGameTime()/100','i<12','w.hasChunk(x>>4,z>>4)','p.hasLineOfSight(horse)','w.addFreshEntity(horse)','progress.encounter()']:
    assert text in s,('exploration wiring',text)
assert 'nearby' in s and 'getEntitiesOfClass' in s and 'inflate(128)' in s
wolf=source('wolf/WolfEncounter.java');assert 'checked.attempt(chunkKey,w.getGameTime())' in wolf and 'checked.size()>=8192' not in wolf
firebird=source('firebird/FirebirdEncounter.java');assert 'observer.hasLineOfSight(b)' in firebird and 'dx=-2;dx<=1' not in firebird
props=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar')
with zipfile.ZipFile(jar) as z:
    for p in OWNED:assert 'org/slavicmyths/'+p.replace('.java','.class') in z.namelist(),p
    assert not any('Encounters271Offline' in p for p in z.namelist()),'test fixture packaged'
manifest={'baseline':BASE,'branch':'hell','jar':str(jar),'version':props['mod_version'],'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'source_files':{J+p:hashlib.sha256((R/J/p).read_bytes()).hexdigest() for p in OWNED},'gameplay':'NOT_RUN','polymc':'NOT_INSTALLED'}
(R/'docs/implementation/encounters271-20261025/receipt.json').write_text(json.dumps(manifest,indent=2)+'\n')
print('PASS #271 source scope/preservation and production JAR; GAMEPLAY_NOT_RUN; SHA256',manifest['sha256'])
