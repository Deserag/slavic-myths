"""Install the gated, pinned pack into a NEW genuine PolyMC OneSix instance; never launch it.

Requires the schema already inspected in the installed PolyMC 7.0, official metadata
saved under work/polymc-meta, successful build/loader evidence and the lock file.
Refuses to replace any existing instance. Does not copy worlds or change global Java.
"""
from pathlib import Path
import hashlib, json, shutil, subprocess, sys, zipfile

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/port'
LOCK=ROOT/'packaging/test-pack-lock.json'
pack=json.loads(LOCK.read_text(encoding='utf-8'))
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def write_json(path,value): path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def require(ok,message):
    if not ok: raise SystemExit(message)

jar=ROOT/'build/libs/slavicmyths-0.9.4.jar'
require(jar.is_file(),'Production JAR missing')
logs={name:(OUT/name).read_text(encoding='utf-8') for name in
      ('finalize-clean-build.log','finalize-runtime-nojei.log','finalize-runtime-companions.log')}
require(all('BUILD SUCCESSFUL' in log for log in logs.values()),'Build gate failed')
for name,log in logs.items():
    if 'runtime' in name:
        require(all(marker in log for marker in ('All 4 required tests passed',
            'PORT_RUNTIME_COMPONENTS_PASS','PORT_RUNTIME_REGISTRIES_PASS checked=794 entities=47',
            'PORT_RUNTIME_DATA_PASS recipes=213 loot=227 advancements=154','PORT_RUNTIME_EXISTING_CHECKS_PASS')),
            'Loader gate failed: '+name)
with zipfile.ZipFile(jar) as z:
    require('META-INF/neoforge.mods.toml' in z.namelist(),'Wrong-loader production artifact')
    require(not any(n.startswith('org/slavicmyths/verify/') for n in z.namelist()),'Test classes bundled')
acceptance={'schema_version':2,'platform':{'minecraft':'1.21.1','neoforge':'21.1.255','java':21},
    'gates':{k:'PASS' for k in 'ABCDEF'},'jar_sha256':sha(jar),'client_launches':0,
    'headless_server_boots_this_session':6,'headless_server_failed_runs':1,
    'note':'Six GameTest server boots: one initial test-fixture failure, five passing runs. Loader attempt before server bootstrap failed; no client launch.',
    'evidence_sha256':{name:sha(OUT/name) for name in logs},
    'manual_gameplay_verified':False,'manual_qa':'docs/MANUAL_QA_0.9.4_1.21.1.md'}
write_json(OUT/'acceptance.json',acceptance)
for script,args in [('verify_static_data_1211.py',[]),('verify_port_1211.py',[])]:
    subprocess.run([sys.executable,str(ROOT/'tools'/script),*args],cwd=ROOT,check=True)

launcher=Path(pack['polyMCPath'])
instance=launcher/'instances'/pack['modpack']
require(not instance.exists(),'Existing target instance: refusing to overwrite '+str(instance))
java=Path(pack['javaPath'])
require(java.is_file(),'Configured Java executable missing')
legacy=launcher/'instances/Slavic-Myths-Testing'
protected=[legacy/'instance.cfg',legacy/'mmc-pack.json',legacy/'.minecraft/mods/slavicmyths-0.9.3.jar']
require(all(p.is_file() for p in protected),'Legacy fallback evidence missing')
before={str(p):sha(p) for p in protected}
require(len(pack['mods'])==6 and len({m['filename'] for m in pack['mods']})==6,'Companion set mismatch')
for mod in pack['mods']:
    source=Path(mod['stagedPath'])
    require(source.is_file() and sha(source)==mod['sha256'],'Companion hash mismatch '+mod['filename'])

components=[]
for uid,version in [('org.lwjgl3','3.3.3'),('net.minecraft','1.21.1'),('net.neoforged.neoforge','21.1.255')]:
    source=ROOT/'work/polymc-meta'/f'{uid}-{version}.json'
    meta=json.loads(source.read_text(encoding='utf-8'))
    require(meta['uid']==uid and meta['version']==version and meta['formatVersion']==1,'Metadata identity mismatch')
    entry={'uid':uid,'version':version,'cachedName':meta['name'],'cachedVersion':version}
    if uid=='org.lwjgl3': entry.update(dependencyOnly=True,cachedVolatile=True)
    else: entry.update(important=True,cachedRequires=meta['requires'])
    components.append(entry)
    target=launcher/'meta'/uid/(version+'.json')
    if target.exists():
        cached=json.loads(target.read_text(encoding='utf-8'))
        require(cached['uid']==uid and cached['version']==version,'Existing metadata cache mismatch')
    else:
        target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,target)

mods=instance/'.minecraft/mods';mods.mkdir(parents=True)
write_json(instance/'mmc-pack.json',{'formatVersion':1,'components':components})
cfg={'InstanceType':'OneSix','name':'Slavic Myths 0.9.4 - 1.21.1 Testing','iconKey':'default',
    'OverrideJavaLocation':'true','JavaPath':java.as_posix(),'JavaVersion':'21.0.12',
    'JavaArchitecture':'64','JavaRealArchitecture':'amd64','IgnoreJavaCompatibility':'false',
    'OverrideMemory':'true','MinMemAlloc':'512','MaxMemAlloc':'4096','PermGen':'128',
    'ManagedPack':'false','JoinServerOnLaunch':'false'}
(instance/'instance.cfg').write_text('\n'.join(k+'='+v for k,v in cfg.items())+'\n',encoding='utf-8')
shutil.copy2(jar,mods/jar.name)
pack['slavicMyths'].update(sha256=sha(jar),installedPath=(mods/jar.name).as_posix(),status='INSTALLED_NOT_LAUNCHED')
for mod in pack['mods']:
    destination=mods/mod['filename'];shutil.copy2(Path(mod['stagedPath']),destination)
    require(sha(destination)==mod['sha256'],'Installed hash mismatch')
    mod.update(installedPath=destination.as_posix(),status='INSTALLED_NOT_LAUNCHED')
require(len(list(mods.glob('*.jar')))==7,'Unexpected duplicate mods')
require(before=={str(p):sha(p) for p in protected},'Legacy fallback modified')
pack.update(deploymentStatus='INSTALLED_NOT_LAUNCHED',installedInstancePath=instance.as_posix(),
    legacyPreservationSha256=before,clientLaunches=0,
    launcherMetadataSource='https://meta.polymc.org/v1/',
    firstLaunch='Manual only; PolyMC resolves remaining game/loader libraries using official metadata.')
write_json(LOCK,pack)
write_json(OUT/'deployment-check.json',{'status':'PASS','instance':instance.as_posix(),
    'jar_count':7,'sha256_verified':True,'legacy_unchanged':True,'client_launches':0,
    'slavicmyths_sha256':sha(mods/jar.name),'manual_gameplay_verified':False})
print('INSTALLED_NOT_LAUNCHED: '+instance.as_posix())
