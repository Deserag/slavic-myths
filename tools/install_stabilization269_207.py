"""Install owner-authorized continuation build after native fermentation regression gates."""
from pathlib import Path
from datetime import datetime,timezone
from zipfile import ZipFile
import json,hashlib,subprocess,shutil,os
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/implementation/stabilization269-brewing-20261026'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT,stderr=subprocess.DEVNULL)
assert git('branch','--show-current').decode().strip()=='hell'
assert 'BUILD SUCCESSFUL' in (OUT/'clean-build-207.log').read_text(encoding='utf-8-sig')
resources=json.loads((ROOT/'build/current-resources.json').read_text(encoding='utf-8'));jar=ROOT/'build/libs/slavicmyths-2.0.7-HELL.jar'
assert resources['version']=='2.0.7-HELL' and resources['sha256']==sha(jar)
assert 'QA269_BREW PASS repeat destruction cannot duplicate spoiled drops' in (OUT/'safe-fixed-client-207.log').read_text(encoding='utf-8-sig')
for phase in ('initial','restart','fixed'):
 r=json.loads((OUT/(phase+'-receipt.json')).read_text(encoding='utf-8'));assert r['status']=='PASS_SINGLE_CLIENT_BREW_SCOPE' and all(c['status']=='PASS' for c in r['checks'])
 assert r['fps_limit']==30 and r['available_processors']==2 and r['max_heap_bytes']==1610612736 and r['master_volume']==0.3
assert 'Fermentation offline NBT/quality assertions: 7457' in (OUT/'clean-build-207.log').read_text(encoding='utf-8-sig')
mods=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/.minecraft/mods');old=mods/'slavicmyths-2.0.6-HELL.jar';installed=mods/'slavicmyths-2.0.7-HELL.jar'
assert list(mods.glob('slavicmyths*.jar'))==[old] and not installed.exists();assert sha(old)=='027b8269d9c893659e66b1e69742f9d18796564828a7dfb6e4c194d8cc07cb2c'
with ZipFile(old) as a,ZipFile(jar) as b:
 an=set(a.namelist());bn=set(b.namelist());assert an==bn
 changed=sorted(n for n in an if a.read(n)!=b.read(n));assert set(changed)=={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','org/slavicmyths/brewing/BrewTile.class','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'},changed
 assert hashlib.sha256(b.read('org/slavicmyths/brewing/BrewTile.class')).hexdigest()==r['production_brew_class_sha256']
 classes=[n for n in bn if n.endswith('.class')];assert len(classes)==1950
 assert all(b.read(n)==(ROOT/'build/classes/java/main'/n).read_bytes() for n in classes)
backup_dir=Path('D:/slavic-myths-backups')/('stabilization269-207-'+datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ'));backup_dir.mkdir(parents=True,exist_ok=False);backup=backup_dir/old.name;shutil.copy2(old,backup);assert sha(backup)==sha(old)
patch=git('diff','--binary','--','gradle.properties','src/main');(OUT/'source-production-207.patch').write_bytes(patch)
pending=mods/'slavicmyths-2.0.7-HELL.jar.pending';assert not pending.exists();shutil.copy2(jar,pending);assert sha(pending)==sha(jar)
try:
 os.replace(pending,old);old.rename(installed);assert sha(installed)==sha(jar) and list(mods.glob('slavicmyths*.jar'))==[installed]
except BaseException:
 if installed.exists() and not old.exists():installed.rename(old)
 shutil.copy2(backup,old);raise
receipt={'status':'INSTALLED_TEST_BUILD_NOT_OWNER_ACCEPTED','version':'2.0.7-HELL','installed_jar':str(installed),'installed_sha256':sha(installed),'mod_jar_count':1,'backup':str(backup),'backup_sha256':sha(backup),'source_commit':git('rev-parse','HEAD').decode().strip(),'source_dirty':True,'source_production_patch':str((OUT/'source-production-207.patch').resolve()),'source_production_patch_sha256':hashlib.sha256(patch).hexdigest(),'changed_entries_vs_installed_206':changed,'unchanged_gameplay_classes_vs_206':1949,'actual_compiled_main_classes_match_jar':1950,'clean_build':'PASS','resource_gate':'PASS','initial_runtime_checks':len(json.loads((OUT/'initial-receipt.json').read_text())['checks']),'fixed_runtime_checks':len(r['checks']),'fermentation_offline_assertions':7457,'installation_started_launcher':False,'installation_modified_worlds':False,'timestamp_utc':datetime.now(timezone.utc).isoformat()}
(OUT/'installation-receipt-207.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(receipt,ensure_ascii=False,indent=2))
