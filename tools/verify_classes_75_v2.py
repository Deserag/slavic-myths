"""#75 V2: actual camera/rules on JVM and JAR parity, never game screenshots."""
from pathlib import Path
import hashlib,json,re,subprocess,tomllib,zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/classes-75-v2';JDK=Path('C:/Program Files/Java/jdk-21.0.12/bin')
def main():
 out=R/'.tools/class-ui-v2';out.mkdir(parents=True,exist_ok=True)
 files=['src/main/java/org/slavicmyths/rpg/classes/ClassDefinitions.java','src/main/java/org/slavicmyths/rpg/classes/ClassLearningRules.java','src/main/java/org/slavicmyths/client/ClassTreeLayout.java','src/main/java/org/slavicmyths/client/ClassTreeViewport.java','tools/class-ui-tests/ClassUiChecks.java']
 subprocess.run([str(JDK/'javac.exe'),'-encoding','UTF-8','-d',str(out),*[str(R/p) for p in files]],check=True)
 report=json.loads(subprocess.check_output([str(JDK/'java.exe'),'-cp',str(out),'ClassUiChecks'],text=True))
 langs={l:json.loads((R/f'src/main/resources/assets/slavicmyths/lang/{l}.json').read_text(encoding='utf-8')) for l in ('ru_ru','en_us')}
 for file in ('ClassScreen.java','ClassMasteryScreen.java'):
  text=(R/'src/main/java/org/slavicmyths/client'/file).read_text(encoding='utf-8')
  for key in re.findall(r'tr\("([a-z_]+)"\s*[,)]',text):
   key='classes.slavicmyths.'+key
   assert all(key in d for d in langs.values()),key
   assert re.findall(r'%(?:\d+\$)?[sd]',langs['ru_ru'][key])==re.findall(r'%(?:\d+\$)?[sd]',langs['en_us'][key]),key
 # Guards against inadvertently changing server authority, IDs, balance or storage.
 for file in ('ClassDefinitions.java','ClassState.java','ClassNetwork.java'):
  path='src/main/java/org/slavicmyths/rpg/classes/'+file
  assert subprocess.check_output(['git','show','HEAD:'+path],cwd=R).decode().replace('\r\n','\n')==(R/path).read_text(encoding='utf-8'),path
 props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text().splitlines() if '=' in l);jar=R/f"build/libs/slavicmyths-{props['mod_version']}.jar"
 resources=classes=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']==props['mod_version'] for m in meta['mods'])
  assert not any('/verify/' in n or 'ClassUiChecks' in n or n.startswith(('tools/','smoke/')) for n in z.namelist())
  for p in (R/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   key=p.relative_to(R/'src/main/resources').as_posix()
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
   resources+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes();assert int.from_bytes(p.read_bytes()[6:8],'big')==65;classes+=1
 log=(R/'.tools/classes75-v2-build.log').read_text(errors='replace');assert all(s in log for s in ('BUILD SUCCESSFUL','COMMANDS_OFFLINE PASS','FROST_STAFF_OFFLINE PASS'))
 report.update(version=props['mod_version'],jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),resources=resources,java21_classes=classes,source_base_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),source='uncommitted working tree',build='PASS',runtime='IN_GAME_NOT_RUN',screenshots='IN_GAME_NOT_RUN',video='IN_GAME_NOT_RUN',owner_acceptance=False)
 owned=['src/main/java/org/slavicmyths/client/ClassScreen.java','src/main/java/org/slavicmyths/client/ClassTreeViewport.java','src/main/java/org/slavicmyths/client/ClassMasteryScreen.java','src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/lang/en_us.json','gradle.properties'];report['source_sha256']={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in owned}
 (O/'checks.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,ensure_ascii=False))
if __name__=='__main__':main()
