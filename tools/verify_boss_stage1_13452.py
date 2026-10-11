"""Offline gates for stage 1 of the 1.3.4.5 boss request. Does not claim phase/model completion."""
from pathlib import Path
import json,zipfile,hashlib
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';count=0
def check(ok,reason):
 global count
 count+=1
 if not ok:raise AssertionError(reason)
def main():
 global count
 jar=ROOT/'build/libs/slavicmyths-1.3.4.5.2.jar'
 build=(ROOT/'.tools/boss-1345/stage1-build.log').read_text(errors='replace');check('BUILD SUCCESSFUL' in build,'clean build incomplete');check('RARE_RUNE_OFFLINE_CHECKS=20491' in build,'pure regression incomplete')
 server=(ROOT/'.tools/boss-1345/stage1-server.log').read_text(errors='replace');check('All 63 required tests passed' in server,'63 tests failed')
 with zipfile.ZipFile(jar) as z:
  names=set(z.namelist());check('1.3.4.5.2' in z.read('META-INF/neoforge.mods.toml').decode(),'version');check(not any('GameTests' in n or '/verify/' in n or '/smoke/' in n for n in names),'fixture leakage')
  for p in RES.rglob('*'):
   if not p.is_file():continue
   n=p.relative_to(RES).as_posix()
   if n=='META-INF/neoforge.mods.toml':continue # Gradle expands version placeholders.
   check(n in names and z.read(n)==p.read_bytes(),'resource mismatch '+n)
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'));count+=1
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify();count+=1
  prior=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.3.4.5.1.jar')
  if not prior.exists():prior=list(Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.slavicmyths-backups').rglob(prior.name))[-1]
  allowed={'assets/slavicmyths/sounds.json','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'}
  with zipfile.ZipFile(prior) as old:
   for n in old.namelist():
    if n.startswith(('assets/','data/')) and not n.endswith('/') and n not in allowed:check(n in names and old.read(n)==z.read(n),'unrelated resource changed '+n)
 sounds=json.loads((RES/'assets/slavicmyths/sounds.json').read_text(encoding='utf-8'));profiles=json.loads((ROOT/'tools/boss_sound_profiles_13452.json').read_text(encoding='utf-8'))
 for key,target in profiles.items():
  check(sounds[key]=={'sounds':[{'name':target,'type':'event'}]},'sound source '+key)
  if target.startswith('slavicmyths:'):check(target.split(':')[1] in sounds,'missing source event '+target)
 result={'version':'1.3.4.5.2','scope':'boss rework stage1: weapon range and sound profiles; full boss implementation incomplete','build':'clean build PASS','automated_server_tests':63,'actual_server_launches':1,'client_launches':0,'pure_rule_checks':20491,'resource_checks':count,'boss_sound_keys':len(profiles),'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'manual_client_check':'REQUIRES_MANUAL_CLIENT_CHECK'}
 (ROOT/'docs/verification/boss-stage1-1.3.4.5.2.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
