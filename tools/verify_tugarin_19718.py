"""Scoped #56 native pose, protected server and production JAR evidence."""
from pathlib import Path
import json,gzip,subprocess,hashlib,zipfile,tomllib,math,shutil
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/tugarin-56';BASE='59a279a4';checks=[]
def check(ok,why):
 assert ok,why
 checks.append(why)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 changed=set(subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=R,text=True).splitlines())
 check(changed=={'src/main/java/org/slavicmyths/client/TugarinPoses.java'},'Only Tugarin client poses changed; all other Java/resources preserved')
 check(not subprocess.check_output(['git','ls-files','--others','--exclude-standard','src/main'],cwd=R).strip(),'No added production files/registries')
 for name in ['TugarinCombat','WorldBoss','BossRules']:
  path='src/main/java/org/slavicmyths/hunt/'+name+'.java';old=subprocess.check_output(['git','show',BASE+':'+path],cwd=R)
  check((R/path).read_bytes().replace(b'\r\n',b'\n')==old.replace(b'\r\n',b'\n'),'Server damage/AI/NBT/sync preserved: '+name)
 before=json.loads(gzip.decompress((O/'before-19718-native.json.gz').read_bytes()));after=json.loads(gzip.decompress((R/'build/tugarin-56/after-native.json.gz').read_bytes()))
 for form in ['warrior','serpent','likho','mor_likho']:
  check(set(before[form])==set(after[form]) and len(after[form])==84,'Same actual 84 source-pose samples '+form)
 for form in ['likho','mor_likho']:check(before[form]==after[form],'All native geometry unchanged '+form)
 for form in ['warrior','serpent']:
  check(any(before[form][k]!=after[form][k] for k in after[form]),'Actual pose change in native mesh '+form)
  check(all(math.isfinite(v) for faces in after[form].values() for face in faces for point in face for v in point),'Finite source vertices/UV '+form)
 check(after['warrior']['film_spin_52']==after['warrior']['film_recovery_0'],'Exact SPIN end to recovery continuity')
 native=(R/'.tools/tugarin-19718-final-native.log').read_text(errors='replace')
 check('TUGARIN_56_NATIVE PASS' in native and 'BUILD SUCCESSFUL' in native,'Actual native boot ground/stance drift/flat sole/recovery/grip/fire origin assertions PASS')
 shutil.copyfile(R/'build/tugarin-56/after-native.json.gz',O/'after-19718-native.json.gz')
 for n in ['spin-before-after-19718.png','spin-support-top-19718.png','spin-metal-uv-19718.png','other-actions-19718.png','breath-mouth-source-19718.png','motion-19718.gif']:check((O/n).stat().st_size>1000,'Actual source render '+n)
 version=next(s.split('=',1)[1] for s in (R/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production JAR CRC')
  metadata=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
  check(any(m['modId']=='slavicmyths' and m['version']==version for m in metadata['mods']),'JAR mod ID/version')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    key=p.relative_to(R/'src/main/resources').as_posix()
    if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
  check(True,'All production resources including preserved audio equal JAR')
  check(not any('org/slavicmyths/verify/' in key for key in z.namelist()),'No native fixtures in production')
  for c in (R/'build/classes/java/main/org/slavicmyths/client').glob('TugarinPoses*.class'):
   check(z.read(c.relative_to(R/'build/classes/java/main').as_posix())==c.read_bytes(),'Actual revised compiled pose class packaged')
   check(int.from_bytes(c.read_bytes()[6:8],'big')==65,'Actual Java21 class version')
 report=dict(status='IN_PROGRESS_OWNER_QA_PENDING',baseline=BASE,version=version,checks=len(checks),evidence=checks,native_samples_per_form=84,forms=4,server='UNCHANGED',resources_and_audio='UNCHANGED',source_before_sha256=sha(O/'before-19718-native.json.gz'),source_after_sha256=sha(O/'after-19718-native.json.gz'),jar_sha256=sha(jar),runtime='NOT_RUN',owner_accepted=False)
 (O/'gates-19718.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
