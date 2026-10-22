"""Final #148 registry/resource/scope/native-evidence/JAR gates. Never starts a game."""
from pathlib import Path
import hashlib,json,shutil,tempfile,zipfile,subprocess
from PIL import Image
import accessories148_resources as presentation
import accessories148_art as art
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';OUT=ROOT/'docs/implementation/accessories-148';BEFORE=OUT/'before';checks=[]
def require(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def rows(p):return {v[0]:v[1:] for line in p.read_text(encoding='utf8').splitlines() if line and not line.startswith('#') for v in [line.split('\t')]}
newids=set(art.NEW);oldids=set(art.OLD)
require(newids=={'pool_ring','bath_ward','harvest_ring','podvey_belt','leshy_necklace','spark_ring'},'exact six new IDs')
require(len(oldids)==12,'twelve existing GUI redesigns')
base=json.loads((OUT/'baseline.json').read_text());allowed={'src/main/java/org/slavicmyths/'+n for n in ['SlavicMyths.java','farming/SickleItem.java','registry/CreativeCatalog.java','registry/ModItemGroup.java','kurgan/BurialAccessories.java','item/FolkEquipmentEffects.java','rewards/RewardEvents.java','client/CuriosAccessories.java','client/BossAccessoryRenderer.java','client/HuntAccessoryRenderer.java','client/FinistQuiverRenderer.java','client/FolkEquipmentLayer.java','client/MiningGogglesRenderer.java']}
allowed|={'src/main/resources/assets/slavicmyths/'+n for n in ['creative_catalog.tsv','lang/ru_ru.json','lang/en_us.json']}
allowed|={f'src/main/resources/assets/slavicmyths/{folder}/item/{id}.{ext}' for id in oldids for folder,ext in [('models','json'),('textures','png')]}
allowed|={f'src/main/resources/data/curios/tags/item/{slot}.json' for slot in ['ring','charm','belt','necklace']}
allowed|={'tools/create_resources.py','tools/creative_tab_names.py','tools/accessories148_audit.py','gradle.properties'}
changed=[n for n,h in base.items() if not (ROOT/n).exists() or sha(ROOT/n)!=h]
require(set(changed)<=allowed,'exact scope whitelist preserves earlier dirty work, loot, bosses, HP, weapons, runes and armor art')
added={p.relative_to(ROOT).as_posix() for p in (ROOT/'src').rglob('*') if p.is_file() and p.relative_to(ROOT).as_posix() not in base}
expected_added={f'src/main/java/org/slavicmyths/accessories/{n}.java' for n in ['Accessories','AccessoryRules','AccessoryEvents']}|{f'src/main/java/org/slavicmyths/client/{n}.java' for n in ['AccessoryBodyFit','NewAccessoryRenderer']}
expected_added|={f'src/main/resources/assets/slavicmyths/models/item/{id}_held148.json' for id in oldids}
expected_added|={f'src/main/resources/assets/slavicmyths/models/item/{id}.json' for id in newids}
expected_added|={f'src/main/resources/assets/slavicmyths/textures/item/{id}.png' for id in oldids|newids if f'src/main/resources/assets/slavicmyths/textures/item/{id}.png' not in base}
expected_added|={'src/main/resources/assets/slavicmyths/textures/entity/accessories148.png'}
expected_added|={f'src/main/resources/data/slavicmyths/recipe/{id}.json' for id in newids}
require(added==expected_added,'only six complete new items and exact approved client/held files; no placeholders')
for p in (ROOT/'src/main/java/org/slavicmyths/accessories').glob('*.java'):require('net.minecraft.client' not in p.read_text(),'common/server client isolation '+p.name)
old=rows(BEFORE/'src/main/resources/assets/slavicmyths/creative_catalog.tsv');new=rows(A/'creative_catalog.tsv')
require(set(new)==set(old)|newids,'all legacy item IDs plus only six additions')
require({k for k in old if old[k]!=new[k]}==set(presentation.TRANSFERS),'exactly 34 old category moves')
require(sum(v[0]=='accessories' for v in new.values())==40,'forty functional accessories in new tab')
for tab in set(v[0] for v in old.values()):require([k for k,v in old.items() if v[0]==tab and k not in presentation.TRANSFERS]==[k for k,v in new.items() if v[0]==tab],'unrelated creative order preserved '+tab)
for id in oldids:
 held=A/f'models/item/{id}_held148.json';original=BEFORE/f'src/main/resources/assets/slavicmyths/models/item/{id}.json'
 require(held.read_bytes()==original.read_bytes(),'exact original held/body model preserved '+id)
 d=json.loads((A/f'models/item/{id}.json').read_text());require(d['loader']=='neoforge:separate_transforms' and d['base']['parent']=='slavicmyths:item/'+id+'_held148','GUI-only split '+id)
mask_hashes={};png_hashes={}
for id in oldids|newids:
 p=A/f'textures/item/{id}.png'
 with Image.open(p) as im:
  require(im.size==(16,16) and im.mode=='RGBA','native 16x16 RGBA '+id)
  alpha=im.getchannel('A');require(set(alpha.getdata())=={0,255},'binary alpha, no antialiasing '+id)
  mask_hashes[id]=hashlib.sha256(alpha.tobytes()).hexdigest();png_hashes[id]=sha(p)
require(len(set(mask_hashes.values()))==len(mask_hashes),'all eighteen new/redesigned silhouettes distinct without color')
for id,v in art.NEW.items():
 recipe=json.loads((RES/f'data/slavicmyths/recipe/{id}.json').read_text());require(recipe['result']=={'id':'slavicmyths:'+id,'count':1},'one real item recipe '+id)
 for x in recipe['key'].values():require(not x['item'].startswith('slavicmyths:') or x['item'].split(':')[1] in old,'existing material '+x['item'])
 tag=json.loads((RES/f'data/curios/tags/item/{v[0]}.json').read_text());require('slavicmyths:'+id in tag['values'],'correct Curios slot tag '+id)
for slot in ['head','necklace','ring','belt','charm']:
 values=json.loads((RES/f'data/curios/tags/item/{slot}.json').read_text())['values'];require(len(values)==len(set(values)),'no duplicate Curios tag '+slot)
 original=set(json.loads((OUT/'audit/population.json').read_text())['curios']);require(original<=set(new),'all twenty-seven old wearable IDs preserved')
for lang in ['ru_ru','en_us']:
 before=json.loads((BEFORE/f'src/main/resources/assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf8'));after=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf8'))
 require({k for k in before if before[k]!=after.get(k)}==set(presentation.LABELS[lang]),'only approved existing tooltip changes '+lang)
 require(set(after)-set(before)=={'itemGroup.slavicmyths.accessories'}|{'item.slavicmyths.'+id+suffix for id in newids for suffix in ['', '.effect']},'only six new item names/effects and tab '+lang)
for n,replacements in {
 'kurgan/BurialAccessories.java':[('.95F','.92F'),('.96F','.94F'),('.91F','.88F')],
 'item/FolkEquipmentEffects.java':[('wears(attacker,ModItems.HUNTER_BELT.get()))event.setAmount(event.getAmount()*1.1F)','wears(attacker,ModItems.HUNTER_BELT.get()))event.setAmount(event.getAmount()*1.15F)')],
 'rewards/RewardEvents.java':[('target.getMaxHealth()*.3F','target.getMaxHealth()*.35F'),('multiplier*=1.1F;fx(p,"trophy_skim_execute")','multiplier*=1.15F;fx(p,"trophy_skim_execute")'),('e.getNewDamage()*1.1F);fx(archer,"trophy_skim_execute")','e.getNewDamage()*1.15F);fx(archer,"trophy_skim_execute")')]
}.items():
 n='src/main/java/org/slavicmyths/'+n;s=(BEFORE/n).read_text(encoding='utf8')
 for a,b in replacements:s=s.replace(a,b)
 require(s==(ROOT/n).read_text(encoding='utf8'),'all old event guards preserved except approved numbers '+n)
with tempfile.TemporaryDirectory() as folder:
 root=Path(folder);assets=root/'src/main/resources/assets/slavicmyths';shutil.copytree(A,assets)
 for slot in ['ring','charm','necklace','belt']:
  rel=f'src/main/resources/data/curios/tags/item/{slot}.json';p=root/rel;p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/rel,p)
 for id in newids:
  p=root/f'src/main/resources/data/slavicmyths/recipe/{id}.json';p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(RES/f'data/slavicmyths/recipe/{id}.json',p)
 expected={p.relative_to(root).as_posix():sha(p) for p in root.rglob('*') if p.is_file()}
 presentation.apply(root);art.apply(root)
 require(all(sha(root/p)==h for p,h in expected.items()),'canonical presentation/art/recipe/tag overlay reproduces actual resources')
 first={p.relative_to(root).as_posix():sha(p) for p in root.rglob('*') if p.is_file()};presentation.apply(root);art.apply(root)
 require(first=={p.relative_to(root).as_posix():sha(p) for p in root.rglob('*') if p.is_file()},'resource generation idempotent')
 # Compile and execute the real production CreativeCatalog. This is not a game process.
 classes=root/'classes';classes.mkdir();harness=root/'CatalogCheck.java';harness.write_text('''import java.nio.file.*;import java.util.*;import org.slavicmyths.registry.CreativeCatalog;
 public class CatalogCheck {public static void main(String[] a)throws Exception{
 if(!CreativeCatalog.TABS.subList(0,10).equals(List.of("slavicmyths","resources","farming","food","tools","armor","decor","magic","mobs","weapons")))throw new AssertionError("legacy indices");
 if(CreativeCatalog.entries().values().stream().filter(e->e.tab().equals("accessories")).count()!=40)throw new AssertionError("population");
 if(CreativeCatalog.category("pool_ring")!=10||CreativeCatalog.category("accessory_registry_missing148")!=-1)throw new AssertionError("new/hidden");System.out.println("native CreativeCatalog PASS");}}
 ''');jdk=Path('C:/Program Files/Java/jdk-21.0.12/bin');subprocess.run([str(jdk/'javac.exe'),'-d',str(classes),str(ROOT/'src/main/java/org/slavicmyths/registry/CreativeCatalog.java'),str(harness)],check=True,capture_output=True)
 result=subprocess.run([str(jdk/'java.exe'),'-cp',str(classes)+';'+str(RES),'CatalogCheck'],capture_output=True,text=True);require(result.returncode==0 and 'PASS' in result.stdout,'native production creative parser executed: '+result.stdout+result.stderr)
native=json.loads((OUT/'native-worn.json').read_text());require(len(native['samples'])==32,'Steve/Alex x4 source poses x2 armor states xA/B native export')
for s in native['samples']:
 if not s['after']:continue
 b=next(v for v in native['samples'] if v['body']==s['body'] and v['pose']==s['pose'] and v['armor']==s['armor'] and not v['after'])
 require(s['base_layers']==b['base_layers'],'skin/armor unchanged source A/B '+s['body']+'/'+s['pose'])
 for id in ['poyas_tugarina','odnoglazyy_obereg','volchiy_poyas','zolny_obereg','obereg_padayuschey_zvezdy','finist_quiver','miners_goggles']:
  a=s['artifacts'][id]['faces'];oldfaces=b['artifacts'][id]['faces'];require(len(a)==len(oldfaces),'native topology unchanged '+id)
  require(all(v[3:]==w[3:] for f,g in zip(a,oldfaces) for v,w in zip(f,g)),'native UV unchanged '+id)
  if not s['armor'] or id in ['finist_quiver']:require(a==oldfaces,'unaffected native placement identical '+id)
wearer=json.loads((OUT/'wearer-matrix.json').read_text(encoding='utf8'));require(len(wearer)==33,'all thirty-three wearer IDs covered')
require(all((ROOT/x['source_proof']).exists() for x in wearer if x['visible_expected']),'source-built views for every fifteen visible IDs')
for n in ['COMBINATION-BOSS','COMBINATION-NEW','COMBINATION-REWARDS','COMBINATION-QUIVER']:require((OUT/f'wearer/{n}.png').exists(),'six-slot combination source sheet '+n)
require(len(json.loads((OUT/'gui-matrix.json').read_text(encoding='utf8')))==33,'all GUI sources reviewed')
gui=json.loads((OUT/'gui-matrix.json').read_text(encoding='utf8'));all_masks=[]
for row in gui:
 with Image.open(ROOT/row['png']) as im:all_masks.append((im.size,hashlib.sha256(im.getchannel('A').tobytes()).hexdigest()))
require(len(all_masks)==len(set(all_masks)),'all 33 final GUI silhouettes have no exact duplicates')
require((OUT/'VISIBILITY-SOURCE.png').exists(),'source visibility branches visualized without runtime claims')
require('isInvisible' not in (OUT/'curios-layer-bytecode.txt').read_text(),'installed Curios layer has no implicit global invisibility gate')
require('ACCESSORIES148_OFFLINE PASS' in (OUT/'final-build-gates.log').read_text(),'shared production policies and real NBT/native UV checks passed')
jar=ROOT/'build/libs/slavicmyths-1.9.7.53.jar'
with zipfile.ZipFile(jar) as z:
 names=set(z.namelist())
 for p in RES.rglob('*'):
  if p.is_file() and p.suffix in ['.json','.png','.tsv']:
   name=p.relative_to(RES).as_posix();require(name in names and z.read(name)==p.read_bytes(),'production JAR resource '+name)
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf8'))
 for c in ['accessories/Accessories','accessories/AccessoryEvents','accessories/AccessoryRules','client/NewAccessoryRenderer']:
  require('org/slavicmyths/'+c+'.class' in names,'production class '+c)
 require('version="1.9.7.53"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'production mod version 1.9.7.53')
 require(not any('Accessories148Offline' in n or 'Armor88FullMeshes' in n or 'CatalogCheck' in n or 'tools/smoke' in n for n in names),'offline test/native export classes excluded from production')
summary={'checks_passed':len(checks),'native_checks':native['checks'],'native_checks_note':'mostly actual vertex/UV validation, not runtime assertions','changed_original_paths':changed,'added_production_paths':sorted(added),'gui_redesigns':sorted(oldids),'new_ids':sorted(newids),'jar':str(jar),'jar_bytes':jar.stat().st_size,'jar_sha256':sha(jar),'source_commit':subprocess.check_output(['git','rev-parse','HEAD'],text=True,cwd=ROOT).strip(),'working_tree':'uncommitted; final-source-manifest.json','runtime_launches':0,'runtime':'NOT_RUN / OWNER_PENDING','project_status_changed':False,'owner_accepted':False}
(OUT/'final-checks.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf8');(OUT/'final-source-manifest.json').write_text(json.dumps({p.relative_to(ROOT).as_posix():sha(p) for folder in ['src','tools'] for p in (ROOT/folder).rglob('*') if p.is_file() and '__pycache__' not in p.parts},indent=2)+'\n');print(json.dumps(summary,ensure_ascii=False,indent=2))
if __name__=='__main__':pass
