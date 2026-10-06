"""Install the gated RC2 into the named PolyMC 1.21.1 test profile only; preserve other mods and saves."""
from pathlib import Path
import json,hashlib,shutil,os,zipfile
R=Path(__file__).resolve().parents[1];V='0.9.10-rc2';pack_path=R/'packaging/test-pack-lock.json';pack=json.loads(pack_path.read_text(encoding='utf-8'))
instance=Path(pack['installedInstancePath']).resolve();mods=(instance/'.minecraft/mods').resolve()
assert instance.name=='Slavic-Myths-1.21.1-Testing' and mods.is_relative_to(instance)
components={c['uid']:c['version'] for c in json.loads((instance/'mmc-pack.json').read_text(encoding='utf-8'))['components']};assert components['net.minecraft']=='1.21.1' and components['net.neoforged.neoforge']=='21.1.255'
report=R/'docs/verification/playtest-0.9.10-rc2';gates=json.loads((report/'acceptance.json').read_text(encoding='utf-8'));assert gates['build']=='PASS' and gates['regressionTests']=='27/27 PASS' and gates['hotfixAndWorkstationTests']=='9/9 PASS'
jar=R/'build/libs'/('slavicmyths-'+V+'.jar')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(jar)==gates['sha256']
legacy_before={name:sha(Path(name)) for name in pack.get('legacyPreservationSha256',{})}
own=[]
for p in mods.glob('*.jar'):
 with zipfile.ZipFile(p) as z:
  if 'META-INF/neoforge.mods.toml' in z.namelist() and 'modId="slavicmyths"' in z.read('META-INF/neoforge.mods.toml').decode():own.append(p.resolve())
assert len(own)==1,'Expected one existing Slavic Myths JAR'
old=own[0];target=(mods/jar.name).resolve();assert old.parent==mods and target.parent==mods
other={p.name:sha(p) for p in mods.glob('*.jar') if p.resolve()!=old}
def protected_snapshot():
 result={}
 for name in ['config','saves']:
  base=instance/'.minecraft'/name
  if base.exists():
   for p in base.rglob('*'):
    if p.is_file():result[p.relative_to(instance).as_posix()]=[p.stat().st_size,p.stat().st_mtime_ns]
 return result
before=protected_snapshot();old_hash=sha(old)
backup=(instance/'.slavicmyths-backups'/('pre-'+V)/old.name).resolve();assert backup.is_relative_to(instance) and not backup.is_relative_to(mods);backup.parent.mkdir(parents=True,exist_ok=True)
if backup.exists():assert sha(backup)==old_hash
else:shutil.copy2(old,backup)
assert sha(backup)==old_hash
if target.exists() and target!=old:raise SystemExit('Target JAR already exists; refusing to overwrite.')
temporary=(mods/(jar.name+'.pending')).resolve();assert temporary.parent==mods
shutil.copy2(jar,temporary);assert sha(temporary)==gates['sha256'];os.replace(temporary,target)
if old!=target:old.unlink() # Exact verified file inside the named mods directory, backed up first.
assert {p.name:sha(p) for p in mods.glob('*.jar') if p.resolve()!=target}==other and protected_snapshot()==before
assert {name:sha(Path(name)) for name in legacy_before}==legacy_before
cfg=instance/'instance.cfg';lines=cfg.read_text(encoding='utf-8').splitlines();cfg.write_text('\n'.join('name=Slavic Myths 0.9.10 RC2 - NeoForge 1.21.1 TEST' if l.startswith('name=') else l for l in lines)+'\n',encoding='utf-8')
pack['slavicMyths']={'version':V,'filename':jar.name,'sha256':sha(target),'installedPath':target.as_posix(),'status':'INSTALLED_NOT_LAUNCHED'};pack['deploymentStatus']='INSTALLED_NOT_LAUNCHED';pack['previousArtifactBackup']=backup.as_posix();pack['playtestAcceptance']=str(report.relative_to(R)/'acceptance.json');pack_path.write_text(json.dumps(pack,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
receipt={'version':V,'profile':str(instance),'installedJar':str(target),'sha256':sha(target),'previousArtifact':str(backup),'previousSha256':old_hash,'otherModHashesUnchanged':other,'configAndSaveMetadataUnchanged':True,'legacyProtectedHashesUnchanged':True,'clientLaunches':0}
(report/'polymc-installation.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print('INSTALLED_RC2_PASS previous JAR backed up; 6 other mods unchanged; client not launched.')
