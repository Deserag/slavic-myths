"""Install only a gated candidate, preserving prior JAR and every other instance file."""
from pathlib import Path
import json,hashlib,shutil,datetime
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/verification/sivka-1.6.0';gate=json.loads((OUT/'resource-gates.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();source=Path(gate['jar']);assert gate['status']=='PASS' and gate['minecraft_launches']==0 and sha(source)==gate['sha256']
instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve();mods=(instance/'.minecraft/mods').resolve();assert mods.is_dir() and mods.is_relative_to(instance)
versions={c['uid']:c['version'] for c in json.loads((instance/'mmc-pack.json').read_text())['components']};assert versions['net.minecraft']=='1.21.1' and versions['net.neoforged.neoforge']=='21.1.255'
old=list(mods.glob('slavicmyths-*.jar'));baseline=ROOT/'work/sivka/baseline/slavicmyths-1.5.6.jar';assert len(old)==1 and sha(old[0])==sha(baseline),'Installed mod changed: preserve user files'
prior=old[0];before={p.relative_to(instance).as_posix():sha(p) for p in instance.rglob('*') if p.is_file() and p.resolve()!=prior.resolve()}
backup=instance/'.slavicmyths-backups'/('pre-sivka-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'));assert backup.resolve().is_relative_to(instance);backup.mkdir(parents=True,exist_ok=False);saved=backup/prior.name;shutil.copy2(prior,saved);assert sha(saved)==sha(prior)
target=mods/source.name;staged=mods/(source.name+'.new');assert not staged.exists() and not target.exists();shutil.copy2(source,staged);assert sha(staged)==gate['sha256'];assert prior.resolve().parent==mods and sha(prior)==sha(baseline);prior.unlink();staged.replace(target);assert sha(target)==gate['sha256'] and list(mods.glob('slavicmyths-*.jar'))==[target]
for name,value in before.items():assert sha(instance/name)==value,'Other instance file changed: '+name
receipt={'version':'1.6.0','instance':str(instance),'installed_jar':str(target),'production_sha256':sha(target),'previous_jar':str(saved),'other_files_preserved':len(before),'minecraft_launches':0,'acceptance':'OPEN_RUNTIME_NOT_RUN','old_1_16_5_instance':'NOT_TOUCHED'};(OUT/'polymc-installation.json').write_text(json.dumps(receipt,indent=2)+'\n');print(json.dumps(receipt,indent=2))
