"""Stage-specific source contracts and current JAR/resources; no historical baseline rewrite."""
from pathlib import Path
import hashlib,json,zipfile,re
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/implementation/creatures-1.9.8.2';OUT.mkdir(parents=True,exist_ok=True)
checks=[]
def require(ok,label):
    if not ok:raise AssertionError(label)
    checks.append(label)
def java(path):return (ROOT/'src/main/java/org/slavicmyths'/path).read_text(encoding='utf-8')
require('mod_version=1.9.7' in (ROOT/'gradle.properties').read_text(),'production version unchanged')
require('role==ATAMAN?100' in java('bandit/BanditEntity.java') and 'MAX_HEALTH).setBaseValue(100)' in java('bandit/LargeCampPiece.java'),'both ataman initializers100')
require('getHealth()<=getMaxHealth()*.25f' in java('volkolak/CursedVolkolak.java') and 'getHealth()<=getMaxHealth()*.625f' in java('volkolak/CursedVolkolak.java'),'cursed relative phase expressions')
require('getHealth()/getMaxHealth()' in java('volkolak/CursedVolkolak.java'),'cursed UI uses actual maximum')
require('Math.round(logicalHealth()),Math.round(GorynychRules.HP)' in java('gorynych/Gorynych.java'),'Gorynych UI no literal3600')
require('logicalHealth()/3600' not in java('gorynych/Gorynych.java'),'no old projection divisor')
bird=java('firebird/FirebirdEntity.java');hurt=bird.split('boolean hurt(',1)[1].split('@Override',1)[0]
require('super.hurt' not in hurt and 'state(5)' in hurt and 'cue("warning"' in hurt,'prevented HP loss keeps warning transition')
require('if(state()==5&&age()>=11){state(6)' in bird and 'aggressor.hurt' in bird and ',20)' in bird,'retaliation scheduler and damage20 retained')
require('public void die(DamageSource s){}' in bird and 'public void kill(){}' in bird,'no ordinary death/kill path or death loot')
require('discard();return;' in bird and 'd.close(w);discard();' in bird,'narrative discard/encounter close retained')
require('Map.entry("health",1600d)' in java('karachun/NightConfig.java'),'Karachun default1600')
require('newMax=oldMax==1200?' in java('karachun/Karachun.java'),'custom saved NightMax not replaced')
require('if(!(this instanceof IceDouble))' in java('morozko/Morozko.java'),'Morozko migration excludes ice double')
for file in ['hunt/HuntMob.java','mor/MorController.java','mor/HigherMor.java','kalin/KalinEntity.java','poloz/GreatPoloz.java','volkolak/VolkolakEvents.java','hunt/TugarinCombat.java','guardians/FireGuardian.java','guardians/SpringKeeper.java','gorynych/GorynychWorld.java','item/SilverCombat.java']:
    import subprocess
    rel='src/main/java/org/slavicmyths/'+file
    original=subprocess.check_output(['git','show','HEAD:'+rel],cwd=ROOT)
    require((ROOT/rel).read_bytes().replace(b'\r\n',b'\n')==original.replace(b'\r\n',b'\n'),file+' unchanged from current HEAD; source contract only')
require('oven()?175:165' in java('hunt/HuntMob.java'),'ordinary Volkolak165 retained')
require('1800' in java('kalin/KalinEntity.java') and '3000' in java('poloz/GreatPoloz.java'),'Kalin/Poloz own ceilings retained')
jar=ROOT/'build/libs/slavicmyths-1.9.7.jar'
with zipfile.ZipFile(jar) as archive:
    require(archive.testzip() is None,'ZIP CRC')
    names=set(archive.namelist());require('META-INF/neoforge.mods.toml' in names,'NeoForge production metadata')
    require(not any(n.startswith(('tools/','smoke/')) or 'CreatureHealthChecks' in n or n.endswith('Checks.class') for n in names),'test fixtures excluded from production')
    require('org/slavicmyths/combat/CreatureHealthBalance.class' in names,'local migration class packaged')
    resources=0
    for p in (ROOT/'src/main/resources').rglob('*'):
        if not p.is_file():continue
        name=p.relative_to(ROOT/'src/main/resources').as_posix();require(name in names,'resource present '+name)
        if name.startswith(('assets/','data/')):require(archive.read(name)==p.read_bytes(),'resource byte parity '+name)
        if name.endswith('.json'):json.loads(archive.read(name))
        resources+=1
result=dict(status='PASS',source_contracts='static only; not runtime reachability',checks=len(checks),resources=resources,jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),runtime_runs=0,historical_resource_validator='NOT_REPEATED; known historical FAIL retained')
(OUT/'CHECKS.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
