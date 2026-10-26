"""Exact scope and unchanged combat handlers, not a gameplay substitute."""
from pathlib import Path
import subprocess,json,hashlib,zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/prince277-20261026';checks=[]
def git(*args):return subprocess.check_output(['git',*args],cwd=R)
def check(ok,what):
 if not ok:raise AssertionError(what)
 checks.append(what)
name='src/main/java/org/slavicmyths/kurgan/KurganCreature.java'
old=git('show','1ef5dace:'+name).decode();new=(R/name).read_text()
check(git('diff','1ef5dace','--name-only','--','src/main').decode().splitlines()==[name],'production changes only KurganCreature; #278/#284 and all armor/resources unchanged')
def method(s,marker):
 start=s.index(marker);a=s.index('{',start);depth=1;b=a+1
 while depth:
  depth+=(s[b]=='{')-(s[b]=='}');b+=1
 return s[start:b].replace('\r\n','\n')
for marker in ['private Move choose(', 'private void begin(', 'private boolean allowed(', 'private void dash(', 'private boolean strike(', 'private void release(', 'private void reposition(', 'public void addAdditionalSaveData(', 'public void readAdditionalSaveData(', 'public void die(', 'public void tick(']:check(method(old,marker)==method(new,marker),'unchanged '+marker)
check('memory=120' in new and 'tickCount%8==0' in new and 'tickCount%36<(phase()>0?6:12)' in new,'existing memory, path cadence, guard cadence retained')
(O/'KurganCreature-before.java').write_bytes(git('show','1ef5dace:'+name))
jar=R/'build/libs/slavicmyths-2.0.1-HELL.jar'
with zipfile.ZipFile(jar) as z:
 check(not any('Prince277' in n or n.startswith('org/slavicmyths/verify/') or 'slavicmyths_prince277/' in n for n in z.namelist()),'all test code/templates excluded from production JAR')
 check('org/slavicmyths/kurgan/KurganCreature.class' in z.namelist(),'production AI packaged')
out={'checks':checks,'count':len(checks),'base_commit':git('rev-parse','1ef5dace').decode().strip(),'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'scope':'source/package only; native cases in run5.log'};(O/'source-jar.json').write_text(json.dumps(out,indent=2)+'\n');print('PRINCE277_SOURCE_JAR PASS checks='+str(len(checks)))
