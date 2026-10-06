"""Read-only verification of the installed exact pack, genuine components and old fallback."""
from pathlib import Path
import hashlib, json, sys
ROOT=Path(__file__).resolve().parents[1]
pack=json.loads((ROOT/'packaging/test-pack-lock.json').read_text(encoding='utf-8'))
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def require(ok,message):
    if not ok: raise SystemExit('FAIL: '+message)
instance=Path(pack['installedInstancePath']);mods=instance/'.minecraft/mods'
require(pack['deploymentStatus']=='INSTALLED_NOT_LAUNCHED','Deployment state')
require(pack['clientLaunches']==0,'Client launch forbidden')
own=pack['slavicMyths']; expected={own['filename'],*(m['filename'] for m in pack['mods'])}
require(len(expected)==7 and {p.name for p in mods.glob('*.jar')}==expected,'Unexpected duplicate/missing JAR')
for mod in [own,*pack['mods']]:
    installed=Path(mod['installedPath'])
    require(installed.resolve().parent==mods.resolve(),'Installed path outside target mods')
    require(installed.name==mod['filename'] and sha(installed)==mod['sha256'],'Hash drift '+mod['filename'])
production=ROOT/'build/libs'/own['filename']
if '--previous-artifact' in sys.argv:
    require(own['version'] in ['0.9.5','0.9.6'],'Previous artifact mode only supports preserved 0.9.5/0.9.6')
    production=ROOT/('.tools/port-backups/slavicmyths-'+own['version']+'-final.jar')
require(sha(production)==own['sha256'],'Production/installed JAR differ')
for name,digest in pack['legacyPreservationSha256'].items(): require(sha(Path(name))==digest,'Legacy fallback changed '+name)
cfg=dict(line.split('=',1) for line in (instance/'instance.cfg').read_text(encoding='utf-8').splitlines() if '=' in line)
require(cfg['InstanceType']=='OneSix' and cfg['OverrideJavaLocation']=='true','Invalid genuine instance schema')
require(Path(cfg['JavaPath']).is_file() and cfg['JavaPath']==pack['javaPath'],'Java 21 override missing')
require(cfg['JavaVersion'].startswith('21.') and cfg['OverrideMemory']=='true','Wrong Java/memory settings')
mmc=json.loads((instance/'mmc-pack.json').read_text(encoding='utf-8'))
require(mmc['formatVersion']==1,'Unsupported mmc-pack schema')
components={p['uid']:p for p in mmc['components']}
require({uid:p['version'] for uid,p in components.items()}==
        {'org.lwjgl3':'3.3.3','net.minecraft':'1.21.1','net.neoforged.neoforge':'21.1.255'},'Wrong pinned components')
for uid,entry in components.items():
    meta=json.loads((Path(pack['polyMCPath'])/'meta'/uid/(entry['version']+'.json')).read_text(encoding='utf-8'))
    require(meta['uid']==uid and meta['version']==entry['version'],'Cached metadata mismatch')
    if uid!='org.lwjgl3': require(entry['cachedRequires']==meta['requires'],'Inconsistent component dependencies')
require(components['net.neoforged.neoforge']['cachedRequires']==[{'equals':'1.21.1','uid':'net.minecraft'}],
        'NeoForge/Minecraft dependency mismatch')
repair=pack.get('launcherChecksumRepair')
if repair:
    patch=json.loads(Path(repair['instance_patch']).read_text(encoding='utf-8'))
    require(patch['uid']=='net.neoforged.neoforge' and patch['version']=='21.1.255','Repair changed component identity')
    for artifact in repair['artifacts']:
        installed=Path(artifact['installed_path'])
        require(sha(installed)==artifact['sha256'],'NeoForge artifact hash drift')
        require(hashlib.sha1(installed.read_bytes()).hexdigest()==artifact['official_sha1'],'NeoForge official SHA-1 mismatch')
        matching=[lib['downloads']['artifact'] for lib in patch['mavenFiles']
                  if lib.get('downloads',{}).get('artifact',{}).get('url')==artifact['url']]
        require(matching and all(a['sha1']==artifact['official_sha1'] and a['size']==artifact['size'] for a in matching),
                'Local component repair missing or inconsistent')
report={'status':'PASS','instance':instance.as_posix(),'jar_count':7,'sha256_verified':True,
        'legacy_unchanged':True,'client_launches':0,'slavicmyths_sha256':own['sha256'],
        'manual_gameplay_verified':False,'official_component_metadata_verified':True,
        'official_neoforge_artifacts_checked':bool(repair)}
import sys
output=ROOT/('docs/verification/swamp-0.9.7/deployment-check.json' if '--swamp-rework' in sys.argv else 'docs/verification/kurgan-0.9.6/deployment-check.json' if '--kurgan-rework' in sys.argv else 'docs/verification/navigation-0.9.5/deployment-check.json' if '--navigation' in sys.argv else 'docs/port/deployment-check.json')
output.parent.mkdir(parents=True,exist_ok=True)
output.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print('PASS: 7 installed JAR hashes, official PolyMC components, Java 21 override and unchanged legacy fallback. Client not launched.')
