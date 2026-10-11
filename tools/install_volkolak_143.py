"""Install the verified release into the existing 1.21.1 testing instance, preserving all other files."""
from pathlib import Path
import datetime,hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[1];REPORT=ROOT/'docs/verification/volkolak_1.4.3';receipt=json.loads((REPORT/'resource_gates.json').read_text(encoding='utf-8'))
INSTANCE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing');MODS=INSTANCE/'.minecraft/mods';source=Path(receipt['jar']);target=MODS/source.name
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
assert receipt['status']=='PASS'and receipt['version']=='1.4.3'and sha(source)==receipt['sha256']
assert INSTANCE.is_dir()and MODS.is_dir()and '1.21.1' in (INSTANCE/'mmc-pack.json').read_text(encoding='utf-8')and '21.1.255'in (INSTANCE/'mmc-pack.json').read_text(encoding='utf-8')
old=list(MODS.glob('slavicmyths-*.jar'));oldpaths={p.resolve()for p in old};before={p.relative_to(INSTANCE).as_posix():sha(p)for p in INSTANCE.rglob('*')if p.is_file()and p.resolve()not in oldpaths};stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S');backup=INSTANCE/'.slavicmyths-backups'/('pre-volkolak-'+stamp);backup.mkdir(parents=True,exist_ok=False);saved=[]
for p in old:
 assert p.resolve().parent==MODS.resolve()
 dest=backup/p.name;shutil.copy2(p,dest);assert sha(dest)==sha(p);saved.append({'name':p.name,'sha256':sha(p),'backup':str(dest)})
staged=MODS/(source.name+'.new');assert not staged.exists();shutil.copy2(source,staged);assert sha(staged)==receipt['sha256']
for p in old:
 if p!=target:p.unlink()
staged.replace(target)
assert list(MODS.glob('slavicmyths-*.jar'))==[target]and sha(target)==receipt['sha256']
for name,digest in before.items():assert (INSTANCE/name).is_file()and sha(INSTANCE/name)==digest,'Changed protected instance file '+name
record={'status':'PASS','version':'1.4.3','instance':str(INSTANCE),'production_jar':str(source),'installed_jar':str(target),'sha256':sha(target),'previous_jars':saved,'protected_files':len(before),'active_slavicmyths_jars':1,'client_launches':0,'worlds_and_settings':'Preserved byte-for-byte','old_1_16_5_instance':'Untouched'};(REPORT/'polymc-installation.json').write_text(json.dumps(record,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(f'Installed {target}; {len(before)} protected files unchanged; SHA256 {sha(target)}')
