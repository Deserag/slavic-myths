from pathlib import Path
import json,hashlib,shutil,subprocess,datetime
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/rune-glow-visible-47'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 check=json.loads((O/'jar-check.json').read_text());assert check['status']=='PASS';jar=Path(check['jar']);assert sha(jar)==check['sha256']
 instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve();mods=(instance/'.minecraft/mods').resolve();assert mods.is_relative_to(instance)
 old=list(mods.glob('slavicmyths-*.jar'));assert len(old)==1,old
 others={p.name:sha(p) for p in mods.glob('*.jar') if p not in old}
 backup=instance/'codex-backups'/('before-1.9.7.47-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'));backup.mkdir(parents=True)
 oldsha=sha(old[0]);saved=backup/old[0].name;shutil.copy2(old[0],saved);assert sha(saved)==oldsha
 dest=mods/jar.name;assert not dest.exists();shutil.copy2(jar,dest);assert sha(dest)==check['sha256']
 # Delete only the exact old file, after verified backup and verified replacement.
 assert old[0].resolve().parent==mods and sha(old[0])==oldsha
 old[0].unlink()
 active=list(mods.glob('slavicmyths-*.jar'));assert active==[dest]
 assert others=={p.name:sha(p) for p in mods.glob('*.jar') if p!=dest}
 result=dict(status='INSTALLED_TEST_PREVIEW_IN_PROGRESS_OWNER_PENDING',version='1.9.7.47',installed_jar=str(dest),installed_sha256=sha(dest),backup_jar=str(saved),backup_sha256=oldsha,source_base_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),source_state='HEAD plus uncommitted changes; no commit',source_fingerprint=check['source_fingerprint'],other_mods_preserved=others,active_mod_jars=[p.name for p in active],runtime='NOT_RUN',runtime_launches=0,owner_acceptance='PENDING',final_release=False)
 (O/'installed.json').write_text(json.dumps(result,indent=2));print(json.dumps(result))
if __name__=='__main__':main()
