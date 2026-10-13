"""Stage the gated candidate in the previously selected 1.21.1 profile.

Never starts Minecraft; aborts if a client is running or the installed JAR changed.
"""
from pathlib import Path
import datetime,hashlib,json,shutil,subprocess,sys
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/rewards-1.7.6'
gate=json.loads((O/'resource-gates.json').read_text(encoding='utf8'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
refresh=False
assert '--refresh-candidate' not in sys.argv, 'No candidate refresh permitted'
previous_receipt=json.loads((O/'polymc-installation.json').read_text(encoding='utf8')) if refresh else None
if refresh:assert previous_receipt['version']=='1.7.6' and previous_receipt['status']=='PASS'
source=R/'build/libs/slavicmyths-1.7.6.jar'
assert gate['status']=='PASS' and gate['clean_build']=='PASS' and gate['dedicated_cases']==1 and sha(source)==gate['sha256']
query="$p=Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\"; @($p | Where-Object { $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|org.multimc|org.prismlauncher|cpw.mods.bootstraplauncher.BootstrapLauncher' }).Count"
assert int(subprocess.check_output(['powershell.exe','-NoProfile','-Command',query],text=True).strip())==0,'Minecraft running: do not replace its loaded JAR'
instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
mods=(instance/'.minecraft/mods').resolve();assert mods.is_dir() and mods.is_relative_to(instance)
versions={v['uid']:v['version'] for v in json.loads((instance/'mmc-pack.json').read_text(encoding='utf8'))['components']}
assert versions['net.minecraft']=='1.21.1' and versions['net.neoforged.neoforge']=='21.1.255'
previous=list(mods.glob('slavicmyths-*.jar'));baseline=R/'release/slavicmyths-1.7.5.jar'
prior_digest=sha(baseline)
assert prior_digest=='03c52508b6ae5332f201259895df3e6f7fad085e35d41d8b8fc34704eac93cf4', 'Accepted baseline changed'
assert len(previous)==1 and sha(previous[0])==prior_digest,'Installed JAR differs from accepted baseline; keep it'
release=(R/'release'/source.name).resolve();assert release.is_relative_to(R.resolve())
assert not release.exists() or sha(release) in ({gate['sha256'],prior_digest} if refresh else {gate['sha256']})
if refresh:
 original_backup=Path(previous_receipt['previous_jar']).resolve();assert original_backup.is_relative_to(instance) and sha(original_backup)==sha(baseline),'Accepted baseline backup changed'
prior=previous[0];before={p.relative_to(instance).as_posix():sha(p) for p in instance.rglob('*') if p.is_file() and p.resolve()!=prior.resolve()}
backup=instance/'.slavicmyths-backups'/('pre-rewards-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))
assert backup.resolve().is_relative_to(instance);backup.mkdir(parents=True,exist_ok=False)
saved=backup/prior.name;shutil.copy2(prior,saved);assert sha(saved)==prior_digest
target=mods/source.name;staged=mods/(source.name+'.new')
assert target.parent==mods and (refresh or not target.exists()) and not staged.exists()
shutil.copy2(source,staged);assert sha(staged)==gate['sha256'] and sha(prior)==prior_digest
try:
 prior.unlink();staged.replace(target)
except Exception:
 if not prior.exists():shutil.copy2(saved,prior)
 if staged.exists():staged.unlink()
 raise
assert sha(target)==gate['sha256'] and list(mods.glob('slavicmyths-*.jar'))==[target]
for name,digest in before.items():assert sha(instance/name)==digest,'Other instance file changed: '+name
shutil.copy2(source,release);assert sha(release)==gate['sha256']
receipt=dict(version='1.7.6',status='PASS',installed_jar=str(target),sha256=sha(target),previous_jar=previous_receipt['previous_jar'] if refresh else str(saved),
 other_files_preserved=len(before),client_launches=0,old_1_16_5_instance='NOT_TOUCHED',
 acceptance='Candidate only; client visual/audio/controller/multiplayer acceptance NOT_RUN')
if refresh:
 assert sha(Path(previous_receipt['previous_jar']))==sha(baseline),'Accepted baseline backup changed'
 receipt['previous_candidate_jar']=str(saved)
(O/'polymc-installation.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print(json.dumps(receipt,ensure_ascii=False))
