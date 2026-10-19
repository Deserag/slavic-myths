"""Explicitly requested intermediate test installation. Never starts any app."""
from pathlib import Path
import hashlib,json,subprocess,zipfile,tomllib,shutil,os,re
from datetime import datetime,timezone,timedelta
R=Path(__file__).resolve().parents[1]
INSTANCE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
MODS=(INSTANCE/'.minecraft/mods').resolve();VERSION='1.9.7.9'
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def guard():
 command="@(Get-CimInstance Win32_Process | Where-Object { $_.Name -ieq 'polymc.exe' -or ($_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|cpw.mods.bootstraplauncher.BootstrapLauncher|org.multimc|org.polymc') } | Select-Object ProcessId,Name) | ConvertTo-Json -Compress"
 result=subprocess.check_output(['powershell','-NoProfile','-Command',command],text=True).strip()
 assert not result or json.loads(result) in [None,[]], 'Minecraft/PolyMC running; no installation performed'
def metadata(path):
 with zipfile.ZipFile(path) as z:
  for name in ['META-INF/neoforge.mods.toml','META-INF/mods.toml']:
   if name in z.namelist():
    for mod in tomllib.loads(z.read(name).decode('utf-8-sig')).get('mods',[]):
     if mod.get('modId')=='slavicmyths':return mod
 return None
def installed_mods():return [p for p in MODS.glob('*.jar') if metadata(p)]
def main():
 assert next(l.split('=',1)[1].strip() for l in (R/'gradle.properties').read_text().splitlines() if l.startswith('mod_version='))==VERSION
 artifact=R/f'build/libs/slavicmyths-{VERSION}.jar';expected=digest(artifact)
 assert metadata(artifact)['version']==VERSION
 with zipfile.ZipFile(artifact) as z:assert z.testzip() is None
 for file in ['art/audio/pack-a-60/voice-rework/gates.json','art/audio/pack-a-60/voice-rework/batch-gates.json','docs/implementation/audio-60/gates.json']:
  gate=json.loads((R/file).read_text(encoding='utf-8'));assert gate['jar_sha256']==expected and gate['checks']>0
 assert 'BUILD SUCCESSFUL' in (R/'.tools/audio-1979-build.log').read_text(encoding='utf-8')
 pack=json.loads((INSTANCE/'mmc-pack.json').read_text());versions={r['uid']:r.get('version') for r in pack['components']}
 assert versions['net.minecraft']=='1.21.1' and versions['net.neoforged.neoforge']=='21.1.255'
 guard();prior=installed_mods();assert len(prior)==1,'Expected exactly one existing Slavic Myths mod'
 old=prior[0];oldhash=digest(old);target=MODS/f'slavicmyths-{VERSION}.jar';stage=MODS/f'.slavicmyths-{VERSION}.jar.new'
 for p in [old,target,stage]:assert p.resolve().parent==MODS
 assert not stage.exists() and not target.exists(),'Preserve unexpected existing artifact/staging file'
 cfg=INSTANCE/'instance.cfg';cfg_before=cfg.read_bytes();text=cfg_before.decode('utf-8-sig')
 name=f'Slavic Myths {VERSION} - NeoForge 1.21.1 TEST - Audio'
 cfg_after=re.sub(r'(?m)^name=[^\r\n]*','name='+name,text).encode('utf-8');assert cfg_after!=cfg_before
 # Only read saved worlds, never modify them. Exclude old backup archives from
 # repeated hashing; they are outside the live profile content being checked.
 others={p:digest(p) for p in INSTANCE.rglob('*') if p.is_file() and '.slavicmyths-backups' not in p.relative_to(INSTANCE).parts and p not in [old,cfg]}
 stamp=datetime.now(timezone(timedelta(hours=3))).strftime('%Y%m%d-%H%M%S')
 backup=INSTANCE/'.slavicmyths-backups'/('pre-audio-1979-'+stamp);assert backup.resolve().is_relative_to(INSTANCE)
 backup.mkdir(parents=True,exist_ok=False);shutil.copy2(old,backup/old.name);shutil.copy2(cfg,backup/'instance.cfg')
 assert digest(backup/old.name)==oldhash and (backup/'instance.cfg').read_bytes()==cfg_before
 shutil.copy2(artifact,stage);assert digest(stage)==expected
 guard();assert digest(old)==oldhash and cfg.read_bytes()==cfg_before
 mutated=False
 try:
  mutated=True;old.unlink();os.replace(stage,target);cfg.write_bytes(cfg_after)
  assert digest(target)==expected and installed_mods()==[target]
  for p,h in others.items():assert digest(p)==h,'Unrelated profile file changed: '+str(p)
 except Exception:
  if mutated:
   if target.exists() and digest(target)==expected:target.unlink()
   shutil.copy2(backup/old.name,old);cfg.write_bytes(cfg_before)
  if stage.exists():stage.unlink()
  raise
 base=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip()
 source=[p for p in (R/'src/main').rglob('*') if p.is_file()]+[R/'gradle.properties']
 snapshot=hashlib.sha256('\n'.join(p.relative_to(R).as_posix()+' '+digest(p) for p in sorted(source)).encode()).hexdigest()
 receipt=dict(status='INSTALLED_FOR_OWNER_TEST_NOT_ACCEPTED',version=VERSION,source_commit=base,source_has_uncommitted_changes=True,source_snapshot_sha256=snapshot,installed_jar=str(target),sha256=digest(target),backup=str(backup),previous_jar=old.name,previous_sha256=oldhash,profile_name=name,other_files_preserved=len(others),active_slavic_jars=1,runtime_launches=0,worlds='NOT_MODIFIED',old_1_16_5='NOT_MODIFIED',purpose='Explicit owner request: intermediate audio test build, not final delivery or release',platform=versions)
 (R/'docs/implementation/audio-60/POLYMC_INSTALLATION.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(receipt,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
