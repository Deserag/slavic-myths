"""Owner-authorized TEST_PREVIEW delivery. No app launch; fail closed before writes."""
from pathlib import Path
import json,hashlib,subprocess,zipfile,tomllib,shutil,os,re
from datetime import datetime,timezone
R=Path(__file__).resolve().parents[1];INSTANCE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve();MODS=(INSTANCE/'.minecraft/mods').resolve();OUT=R/'docs/implementation/audio-60'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def git(*args):return subprocess.check_output(['git',*args],cwd=R,text=True).strip()
def metadata(p):
 with zipfile.ZipFile(p) as z:
  for key in ['META-INF/neoforge.mods.toml','META-INF/mods.toml']:
   if key in z.namelist():
    for mod in tomllib.loads(z.read(key).decode('utf-8-sig')).get('mods',[]):
     if mod.get('modId')=='slavicmyths':return mod
 return None
def installed():return [p for p in MODS.glob('*.jar') if metadata(p)]
def guard():
 command="@(Get-CimInstance Win32_Process -ErrorAction Stop | Where-Object { $_.Name -ieq 'polymc.exe' -or ($_.Name -in @('java.exe','javaw.exe') -and (-not $_.CommandLine -or $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|cpw.mods.bootstraplauncher.BootstrapLauncher|org.multimc|org.polymc')) } | Select-Object ProcessId,Name) | ConvertTo-Json -Compress"
 result=subprocess.check_output(['powershell','-NoProfile','-Command',command],text=True).strip();assert not result or json.loads(result) in [None,[]],'Minecraft/PolyMC active or Java command line unknown: installation blocked'
def main():
 version=next(s.split('=',1)[1].strip() for s in (R/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='));artifact=R/f'build/libs/slavicmyths-{version}.jar';head=git('rev-parse','HEAD');assert git('rev-parse','origin/main')==head
 assert git('ls-remote','origin','refs/heads/main').split()[0]==head,'GitHub main differs from selected build source'
 assert not git('diff','HEAD','--','src/main','gradle.properties','tools'),'Uncommitted implementation is not a main build'
 expected=sha(artifact);build=json.loads((OUT/'TEST_PREVIEW_BUILD.json').read_text(encoding='utf-8'));assert build['source_commit']==head and build['jar_sha256']==expected and build['version']==version and build['clean_build']=='PASS'
 for name in ['art/audio/project-61/gates.json','art/audio/pack-a-60/voice-rework/gates.json','art/audio/pack-a-60/voice-rework/batch-gates.json','art/audio/pack-a-60/frost-foley/gates.json','art/audio/kurgan-65/gates.json','docs/implementation/audio-60/gates.json']:
  gate=json.loads((R/name).read_text(encoding='utf-8'));assert gate['jar_sha256']==expected and gate['checks']>0,name
 assert metadata(artifact)['version']==version
 with zipfile.ZipFile(artifact) as z:
  assert z.testzip() is None
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    n=p.relative_to(R/'src/main/resources').as_posix()
    if n!='META-INF/neoforge.mods.toml':assert z.read(n)==p.read_bytes(),n
 pack=json.loads((INSTANCE/'mmc-pack.json').read_text());platform={p['uid']:p.get('version') for p in pack['components']};assert platform['net.minecraft']=='1.21.1' and platform['net.neoforged.neoforge']=='21.1.255'
 guard();prior=installed();assert len(prior)==1;old=prior[0];oldhash=sha(old);target=MODS/f'slavicmyths-{version}.jar';stage=MODS/f'.slavicmyths-{version}.jar.new'
 assert old!=target and not target.exists() and not stage.exists(),'Unexpected existing target; choose a free test version'
 for p in [old,target,stage]:assert p.resolve().parent==MODS
 cfg=INSTANCE/'instance.cfg';before=cfg.read_bytes();prefix=b'\xef\xbb\xbf' if before.startswith(b'\xef\xbb\xbf') else b'';display=f'Slavic Myths {version} - NeoForge 1.21.1 TEST - Audio Owner Pending';text,n=re.subn(r'(?m)^name=[^\r\n]*','name='+display,before.decode('utf-8-sig'));assert n==1;after=prefix+text.encode('utf-8')
 critical=[cfg,INSTANCE/'mmc-pack.json',INSTANCE/'.minecraft/options.txt'];config=INSTANCE/'.minecraft/config'
 if config.exists():critical+=list(p for p in config.rglob('*') if p.is_file())
 critical=[p for p in critical if p.exists()];critical_hashes={p:sha(p) for p in critical};othermods={p:sha(p) for p in MODS.iterdir() if p.is_file() and p!=old}
 backup=INSTANCE/'.slavicmyths-backups'/('pre-audio-'+version+'-'+datetime.now(timezone.utc).strftime('%Y%m%d-%H%M%SZ'));assert backup.resolve().is_relative_to(INSTANCE);backup.mkdir(parents=True,exist_ok=False)
 shutil.copy2(old,backup/old.name);assert sha(backup/old.name)==oldhash
 backups=[]
 for p in critical:
  dest=backup/'profile'/p.relative_to(INSTANCE);dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,dest);assert sha(dest)==critical_hashes[p];backups.append(dict(path=str(p),backup=str(dest),sha256=critical_hashes[p]))
 shutil.copy2(artifact,stage);assert sha(stage)==expected;guard();assert sha(old)==oldhash and all(sha(p)==h for p,h in critical_hashes.items())
 # Replace content atomically, then rename atomically: never two active mod JARs.
 promoted=False;renamed=False
 try:
  os.replace(stage,old);promoted=True;os.replace(old,target);renamed=True
  assert installed()==[target] and sha(target)==expected and metadata(target)['version']==version
  cfg_stage=INSTANCE/'.instance.cfg.slavicmyths-preview.new';assert not cfg_stage.exists();cfg_stage.write_bytes(after);os.replace(cfg_stage,cfg)
  assert all(sha(p)==h for p,h in critical_hashes.items() if p!=cfg);assert all(sha(p)==h for p,h in othermods.items())
 except Exception:
  if promoted:
   active=target if renamed else old;rollback=MODS/'.slavicmyths-preview-rollback.new';assert not rollback.exists();shutil.copy2(backup/old.name,rollback);os.replace(rollback,active)
   if renamed:os.replace(active,old)
  if cfg.read_bytes()==after:cfg.write_bytes(before)
  raise
 receipt=dict(status='INSTALLED_TEST_PREVIEW',acceptance='AUDIO_OWNER_PENDING',version=version,source_commit=head,source_has_uncommitted_changes=False,installed_jar=str(target),sha256=sha(target),backup=str(backup),previous_jar=old.name,previous_sha256=oldhash,critical_configuration_backups=backups,active_slavic_jars=1,other_mod_files_preserved=len(othermods),profile_name=display,platform=platform,runtime_launches=0,worlds='NOT_MODIFIED: no write operation targets saves',old_1_16_5='NOT_MODIFIED',installed_at=datetime.now(timezone.utc).isoformat(),authorization='https://github.com/Deserag/slavic-myths/issues/61',clean_build='PASS',offline_checks='PASS')
 (OUT/'POLYMC_INSTALLATION.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');(OUT/f'TEST_PREVIEW_{version}.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(receipt,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
