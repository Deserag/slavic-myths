"""Install the verified 1.5.6 candidate into the existing MC1.21.1 test instance; never launch it."""
from pathlib import Path
import json,hashlib,shutil,datetime
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/verification/earth-1.5.6'
r=json.loads((OUT/'resource-gates.json').read_text());p=json.loads((OUT/'offline-gates.json').read_text())
sha=lambda file:hashlib.sha256(file.read_bytes()).hexdigest()
source=Path(r['jar']);assert r['version']=='1.5.6' and r['production']==r['resources']==p['build']==p['offline_checks']=='PASS'
assert sha(source)==r['sha256']==p['production_sha256'] and p['runtime_validation']=='NOT_RUN' and p['minecraft_launches']==0
instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve();mods=(instance/'.minecraft/mods').resolve()
assert mods.is_relative_to(instance) and mods.is_dir()
components={c['uid']:c['version'] for c in json.loads((instance/'mmc-pack.json').read_text())['components']}
assert components['net.minecraft']=='1.21.1' and components['net.neoforged.neoforge']=='21.1.255'
prior=json.loads((OUT/'polymc-installation.json').read_text()) if (OUT/'polymc-installation.json').exists() else None
old=list(mods.glob('slavicmyths-*.jar'));expected={r['baseline_sha256']}
if prior:expected.add(prior['production_sha256'])
assert len(old)==1 and sha(old[0]) in expected,'Installed baseline changed; preserve user files'
if old[0].name==source.name and sha(old[0])==r['sha256']:
 print('Verified candidate already installed. Minecraft launches: 0.');raise SystemExit(0)
old_hash=sha(old[0])
oldpaths={f.resolve() for f in old}
before={f.relative_to(instance).as_posix():sha(f) for f in instance.rglob('*') if f.is_file() and f.resolve() not in oldpaths}
backup=instance/'.slavicmyths-backups'/('pre-earth-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'));assert backup.resolve().is_relative_to(instance)
backup.mkdir(parents=True,exist_ok=False);saved=[]
for f in old:
 assert f.resolve().parent==mods
 dest=backup/f.name;shutil.copy2(f,dest);assert sha(dest)==sha(f)
 saved.append({'name':f.name,'sha256':sha(f),'backup':str(dest)})
target=mods/source.name;staged=mods/(source.name+'.new');assert not staged.exists() and (not target.exists() or target==old[0])
shutil.copy2(source,staged);assert sha(staged)==r['sha256']
for f in old:
 assert f.resolve().parent==mods and sha(f)==old_hash
 if f!=target:f.unlink()
staged.replace(target);assert sha(target)==r['sha256'] and list(mods.glob('slavicmyths-*.jar'))==[target]
for name,digest in before.items():assert sha(instance/name)==digest,'Other instance file changed: '+name
history=(prior['previous_jars'] if prior else [])+saved
receipt={'version':'1.5.6','instance':str(instance),'installed_jar':str(target),'production_sha256':sha(target),'previous_jars':history,'other_files_preserved':len(before),'minecraft_launches':0,'acceptance':'OPEN_RUNTIME_NOT_RUN','old_1_16_5_instance':'NOT_TOUCHED'}
(OUT/'polymc-installation.json').write_text(json.dumps(receipt,indent=2),encoding='utf-8')
print('Installed verified 1.5.6 candidate, backup outside mods; '+str(len(before))+' other files unchanged. Minecraft launches: 0.')
