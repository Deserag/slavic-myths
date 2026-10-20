"""#85 production resource/JAR parity and dose routing; never launches the game."""
from pathlib import Path
import ast, hashlib, json, re, subprocess, tomllib, zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/intoxication-85'

def main():
 props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text().splitlines() if '=' in l)
 jar=R/f"build/libs/slavicmyths-{props['mod_version']}.jar"
 source=(R/'src/main/java/org/slavicmyths/brewing/BeverageItem.java').read_text(encoding='utf-8')
 assert 'int stage=BrewRules.stage(points)' in source
 assert 'BrewRules.stage(points)>=5' in source
 assert 'if(add==0)return' in source
 assert 'p.setData(Brewing.LOAD,n)' in source
 assert 'n.putInt("Points",points);n.putLong("LastDrink",now)' in source
 assert 'if(!w.isClientSide){apply(p,b,q)' in source
 assert 'q.equals("spoiled")||b.equals("spoiled_brew")' in source
 assert 'getUseDuration(ItemStack s,LivingEntity e){return 32;}' in source
 # Save schema and attachment/death policy are byte-for-byte unchanged.
 path='src/main/java/org/slavicmyths/brewing/Brewing.java'
 assert subprocess.check_output(['git','show','HEAD:'+path],cwd=R).decode().replace('\r\n','\n')==(R/path).read_text(encoding='utf-8')
 languages={l:json.loads((R/f'src/main/resources/assets/slavicmyths/lang/{l}.json').read_text(encoding='utf-8')) for l in ('ru_ru','en_us')}
 tree=ast.parse((R/'tools/brewing_resources.py').read_text(encoding='utf-8'))
 extra=next(ast.literal_eval(n.value.args[0]) for n in tree.body if isinstance(n,ast.Expr) and isinstance(n.value,ast.Call) and isinstance(n.value.func,ast.Attribute) and isinstance(n.value.func.value,ast.Name) and n.value.func.value.id=='texts' and n.value.func.attr=='update')
 for k,v in extra.items():
  key='brewing.slavicmyths.'+k
  assert tuple(languages[l][key] for l in ('ru_ru','en_us'))==v,key
  assert re.findall(r'%s',v[0])==re.findall(r'%s',v[1]),key
 resources=classes=jsons=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
  assert any(m['modId']=='slavicmyths' and m['version']==props['mod_version'] for m in meta['mods'])
  assert not any('IntoxicationChecks' in n or n.startswith(('tools/','smoke/')) or '/verify/' in n for n in z.namelist())
  for p in (R/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   key=p.relative_to(R/'src/main/resources').as_posix()
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'));jsons+=1
   if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
   resources+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes()
   assert int.from_bytes(p.read_bytes()[6:8],'big')==65
   classes+=1
 log=(R/'.tools/intoxication85-build.log').read_text(errors='replace');assert 'BUILD SUCCESSFUL' in log
 report=dict(issue=85,status='IN_PROGRESS / OWNER_PENDING',base_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),source='uncommitted working tree',jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),build='PASS',resources=resources,jsons=jsons,java21_classes=classes,language_generator_values='PASS (AST inspection, generator not executed)',runtime='NOT_RUN',minecraft_launches=0,polymc='POLYMC_NOT_INSTALLED',owner_acceptance=False)
 files=['src/main/java/org/slavicmyths/brewing/BeverageItem.java','src/main/java/org/slavicmyths/brewing/BrewRules.java','src/main/java/org/slavicmyths/brewing/Brewing.java','tools/brewing_resources.py','src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/lang/en_us.json']
 report['source_sha256']={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in files}
 (O/'checks.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,ensure_ascii=False))
if __name__=='__main__':main()
