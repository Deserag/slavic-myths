"""Update ONLY the already-known 1.21.1 test instance after successful kurgan gates."""
from pathlib import Path
import hashlib,json,os,shutil,subprocess,sys
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/verification/kurgan-0.9.6'
LOCK=ROOT/'packaging/test-pack-lock.json'
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def require(ok,message):
    if not ok:raise SystemExit(message)
pack=json.loads(LOCK.read_text(encoding='utf-8'))
instance=Path(pack['installedInstancePath']).resolve();mods=(instance/'.minecraft/mods').resolve()
require(instance.name=='Slavic-Myths-1.21.1-Testing','Refusing to modify another profile')
components=json.loads((instance/'mmc-pack.json').read_text(encoding='utf-8'))['components']
require({c['uid']:c['version'] for c in components}=={'org.lwjgl3':'3.3.3','net.minecraft':'1.21.1','net.neoforged.neoforge':'21.1.255'},'Wrong platform')
acceptance=json.loads((OUT/'acceptance.json').read_text(encoding='utf-8'))
jar=ROOT/'build/libs/slavicmyths-0.9.6.jar'
require(jar.is_file() and sha(jar)==acceptance['jar_sha256'],'Gated artifact differs')
subprocess.run([sys.executable,str(ROOT/'tools/verify_kurgan_096.py')],cwd=ROOT,check=True)
for mod in pack['mods']:
    require(sha(mods/mod['filename'])==mod['sha256'],'Companion drift: '+mod['filename'])
for file,digest in pack['legacyPreservationSha256'].items():require(sha(Path(file))==digest,'Legacy changed')
subprocess.run([sys.executable,str(ROOT/'tools/verify_polymc_094.py'),'--kurgan-rework','--previous-artifact'],cwd=ROOT,check=True)
own=pack['slavicMyths'];old=(mods/own['filename']).resolve();target=(mods/jar.name).resolve()
require(old.parent==mods and target.parent==mods,'Artifact path escaped mods directory')
require(old.is_file() and sha(old)==own['sha256'],'Old installed artifact differs from lock')
backup=instance/'.slavicmyths-backups'/own['version']/own['filename'];backup.parent.mkdir(parents=True,exist_ok=True)
if backup.exists():require(sha(backup)==own['sha256'],'Backup collision')
else:shutil.copy2(old,backup)
temporary=target.with_suffix('.jar.pending');shutil.copy2(jar,temporary)
require(sha(temporary)==acceptance['jar_sha256'],'Copy verification failed')
os.replace(temporary,target)
if old!=target:old.unlink() # Both resolved exact files are confined to the verified test mods directory.
pack['slavicMyths']={'version':'0.9.6','filename':jar.name,'sha256':sha(target),'installedPath':target.as_posix(),'status':'INSTALLED_NOT_LAUNCHED'}
pack['deploymentStatus']='INSTALLED_NOT_LAUNCHED';pack['kurganAcceptance']='docs/verification/kurgan-0.9.6/acceptance.json'
pack['previousArtifactBackup']=backup.as_posix()
LOCK.write_text(json.dumps(pack,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
cfg=instance/'instance.cfg';lines=cfg.read_text(encoding='utf-8').splitlines()
lines=[('name=Slavic Myths 0.9.6 - NeoForge 1.21.1 TEST' if line.startswith('name=') else line) for line in lines]
cfg.write_text('\n'.join(lines)+'\n',encoding='utf-8')
subprocess.run([sys.executable,str(ROOT/'tools/verify_polymc_094.py'),'--kurgan-rework'],cwd=ROOT,check=True)
print('PASS: 0.9.6 installed in existing 1.21.1 profile; previous artifact backed up; client not launched')
