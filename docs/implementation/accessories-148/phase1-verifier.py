"""Scope, canonical regeneration, native catalog and production JAR checks. No game."""
from pathlib import Path
import hashlib,json,shutil,subprocess,tempfile,zipfile
from accessories148_resources import apply,TRANSFERS,LABELS
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/implementation/accessories-148';RES=ROOT/'src/main/resources';BEFORE=OUT/'before'
checks=[]
def require(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
base=json.loads((OUT/'baseline.json').read_text());allowed={'src/main/java/org/slavicmyths/'+n for n in ['registry/CreativeCatalog.java','registry/ModItemGroup.java','kurgan/BurialAccessories.java','item/FolkEquipmentEffects.java','rewards/RewardEvents.java']}|{'src/main/resources/assets/slavicmyths/'+n for n in ['creative_catalog.tsv','lang/ru_ru.json','lang/en_us.json']}|{'tools/creative_tab_names.py','tools/create_resources.py','tools/accessories148_audit.py'}
changed=[p for p,h in base.items() if not (ROOT/p).exists() or sha(ROOT/p)!=h]
require(set(changed)<=allowed,'baseline whitelist: previous parallel work/resources preserved')
require(not any(p.relative_to(ROOT).as_posix() not in base for p in (ROOT/'src').rglob('*') if p.is_file()),'no new production registration/placeholder added')
def rows(p):return {v[0]:v[1:] for line in p.read_text(encoding='utf8').splitlines() if line and not line.startswith('#') for v in [line.split('\t')]}
old=rows(BEFORE/'src/main/resources/assets/slavicmyths/creative_catalog.tsv');new=rows(RES/'assets/slavicmyths/creative_catalog.tsv')
require(set(old)==set(new),'catalog item ID set unchanged')
require({k for k in old if old[k]!=new[k]}==set(TRANSFERS),'exactly approved 34 accessory transfers')
require(all(new[k]==['accessories',v] for k,v in TRANSFERS.items()),'slot/type grouping for all transferred items')
for tab in set(v[0] for v in old.values()):
 require([k for k,v in old.items() if v[0]==tab and k not in TRANSFERS]==[k for k,v in new.items() if v[0]==tab], 'unrelated order preserved '+tab)
for lang,expected in LABELS.items():
 b=json.loads((BEFORE/f'src/main/resources/assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf8'));a=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf8'))
 require({k for k in b if b[k]!=a.get(k)}==set(expected),'only five approved item tooltip changes '+lang)
 require(set(a)-set(b)=={'itemGroup.slavicmyths.accessories'},'only new accessory tab label '+lang)
for n,replacements in {
 'kurgan/BurialAccessories.java':[('.95F','.92F'),('.96F','.94F'),('.91F','.88F')],
 'item/FolkEquipmentEffects.java':[('wears(attacker,ModItems.HUNTER_BELT.get()))event.setAmount(event.getAmount()*1.1F)','wears(attacker,ModItems.HUNTER_BELT.get()))event.setAmount(event.getAmount()*1.15F)')],
 'rewards/RewardEvents.java':[('target.getMaxHealth()*.3F','target.getMaxHealth()*.35F'),('multiplier*=1.1F;fx(p,"trophy_skim_execute")','multiplier*=1.15F;fx(p,"trophy_skim_execute")'),('e.getNewDamage()*1.1F);fx(archer,"trophy_skim_execute")','e.getNewDamage()*1.15F);fx(archer,"trophy_skim_execute")')]
}.items():
 p='src/main/java/org/slavicmyths/'+n;s=(BEFORE/p).read_text(encoding='utf8')
 for a,b in replacements:s=s.replace(a,b)
 require(s==(ROOT/p).read_text(encoding='utf8'),'event guards and all other behavior byte-equivalent except approved numbers: '+n)
with tempfile.TemporaryDirectory() as folder:
 tmp=Path(folder)
 for n in ['creative_catalog.tsv','lang/ru_ru.json','lang/en_us.json']:
  dest=tmp/'src/main/resources/assets/slavicmyths'/n;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(BEFORE/'src/main/resources/assets/slavicmyths'/n,dest)
 apply(tmp);one={p.relative_to(tmp).as_posix():p.read_bytes() for p in tmp.rglob('*') if p.is_file()};apply(tmp)
 require(all(p.read_bytes()==one[p.relative_to(tmp).as_posix()] for p in tmp.rglob('*') if p.is_file()),'scoped resource overlay idempotent')
 require(all((ROOT/n).read_bytes()==v for n,v in one.items()),'regeneration reproduces real production resources')
 # Execute the actual production parser with Java 21; no Minecraft classes or worlds.
 classes=tmp/'classes';classes.mkdir();harness=tmp/'CatalogCheck.java'
 harness.write_text('''import java.nio.file.*;import java.util.*;import org.slavicmyths.registry.CreativeCatalog;
 public class CatalogCheck {public static void main(String[] a)throws Exception {
 var before=CreativeCatalog.read(Files.newBufferedReader(Path.of(a[0])));var after=CreativeCatalog.entries();
 if(!CreativeCatalog.TABS.subList(0,10).equals(List.of("slavicmyths","resources","farming","food","tools","armor","decor","magic","mobs","weapons")))throw new AssertionError("legacy tab indices");
 int moved=0;for(var id:before.keySet()){var x=before.get(id);var y=after.get(id);if(y.tab().equals("accessories")){moved++;if(CreativeCatalog.category(id)!=10)throw new AssertionError(id);}else if(!x.tab().equals(y.tab())||!x.group().equals(y.group()))throw new AssertionError(id);}
 if(moved!=34||before.size()!=after.size())throw new AssertionError("population");System.out.println("native catalog PASS: "+moved+" transfers, "+after.size()+" IDs");}}
 ''',encoding='utf8')
 jdk=Path('C:/Program Files/Java/jdk-21.0.12/bin')
 subprocess.run([str(jdk/'javac.exe'),'-d',str(classes),str(ROOT/'src/main/java/org/slavicmyths/registry/CreativeCatalog.java'),str(harness)],check=True,capture_output=True)
 run=subprocess.run([str(jdk/'java.exe'),'-cp',str(classes)+';'+str(RES),'CatalogCheck',str(BEFORE/'src/main/resources/assets/slavicmyths/creative_catalog.tsv')],check=True,capture_output=True,text=True)
 require('PASS' in run.stdout,run.stdout.strip())
jar=ROOT/'build/libs/slavicmyths-1.9.7.52.jar'
with zipfile.ZipFile(jar) as z:
 for p in RES.rglob('*'):
  if p.is_file() and p.suffix in ['.json','.png','.tsv']:
   rel=p.relative_to(RES).as_posix();require(rel in z.namelist() and z.read(rel)==p.read_bytes(),'JAR resource '+rel)
 for c in ['registry/CreativeCatalog','registry/ModItemGroup','kurgan/BurialAccessories','item/FolkEquipmentEffects','rewards/RewardEvents']:
  require('org/slavicmyths/'+c+'.class' in z.namelist(),'production class '+c)
 require(not any('CatalogCheck' in n or 'tools/smoke' in n for n in z.namelist()),'offline verification classes excluded from production')
summary={'checks_passed':len(checks),'scope_changed':changed,'jar':str(jar),'jar_sha256':sha(jar),'source_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),'working_tree':'uncommitted; baseline.json + current-sources.json','runtime_launches':0,'runtime':'NOT_RUN / OWNER_PENDING','polymc':'POLYMC_NOT_INSTALLED'}
(OUT/'current-sources.json').write_text(json.dumps({p.relative_to(ROOT).as_posix():sha(p) for folder in ['src','tools'] for p in (ROOT/folder).rglob('*') if p.is_file() and '__pycache__' not in p.parts},indent=2)+'\n')
(OUT/'checks.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print(json.dumps(summary,ensure_ascii=False,indent=2))
