"""Current-source and packaged-JAR gates. These are not game/GUI acceptance."""
from pathlib import Path
import hashlib,json,re,zipfile,subprocess,sys
from PIL import Image
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';J=R/'src/main/java/org/slavicmyths';O=R/'docs/implementation/runes-craft-iii-iv'
checks=[]
def require(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def load(p):return json.loads(p.read_text(encoding='utf-8'))
def source(p):return (J/p).read_text(encoding='utf-8')
ids=['unbreakable_iv','perun_rage_iv','forest_spirit_iv','mastery_iv'];items=['blank_rune_4']+['rune_'+id for id in ids]
source_ids=lambda s:re.findall(r'"([a-z_0-9]+)"',s.split('IDS={',1)[1].split('};',1)[0])
old=source_ids(subprocess.check_output(['git','show','b76160800223267abc04e7c6d46812dfba8976dc:src/main/java/org/slavicmyths/rpg/Runes.java'],cwd=R).decode())
now=source_ids(source('rpg/Runes.java'));prior=['counterstrike_iii','hunter_iii','last_stand_iii','depths_iii','veles_oath_iv']
require(now==old+prior+ids and len(now)==len(set(now))==46,'37 original IDs + five prior + exactly four new IV')
allowed=['rpg/AdvancedRuneRules.java','rpg/AdvancedRunes.java','rpg/RuneBase.java','rpg/RuneFoundation.java','rpg/Runes.java','rpg/RpgEvents.java','rpg/RuneRuntime.java','industry/IndustryAccessories.java','compat/JeiRunes.java','rpg/CraftRuneRules.java','rpg/CraftRunes.java']
allowed_paths={'src/main/java/org/slavicmyths/'+p for p in allowed}|{'src/main/resources/assets/slavicmyths/lang/'+lang+'.json' for lang in ['ru_ru','en_us']}|{'src/main/resources/assets/slavicmyths/creative_catalog.tsv'}
baseline=load(O/'BASELINE_HASHES.json')
for path,h in baseline.items():
 if path.startswith('src/main/') and path not in allowed_paths:require((R/path).is_file() and digest(R/path)==h,'preserved pre-task production file '+path)
# Restore the pre-task bytes by removing ONLY the appended owned translations.
for lang in ['ru_ru','en_us']:
 path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json';s=(R/path).read_text(encoding='utf-8')
 keys=['item.slavicmyths.blank_rune_4']+[prefix+id for id in ids for prefix in ['item.slavicmyths.rune_','runes_patch.effect.','runes_patch.compatible.']]
 for key in keys:
  s,n=re.subn(r',\n  "'+re.escape(key)+r'": "(?:[^"\\]|\\.)*"','',s);require(n==1,'one owned translation '+key+' '+lang)
 for note in [' С Мастерством IV общий кап 35%.',' With Mastery IV the combined cap is 35%.',' (среди особых IV; крафтовые IV независимы)',' (among special IV; crafted IV are independent)']:s=s.replace(note,'')
 require(any(hashlib.sha256(v.encode()).hexdigest()==baseline[path] for v in [s,s.replace('\n','\r\n')]),'all pre-task translation bytes preserved '+lang)
for item in items:
 model=load(RES/f'assets/slavicmyths/models/item/{item}.json');require(model=={'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+item}},'generated item model '+item)
 p=RES/f'assets/slavicmyths/textures/item/{item}.png';im=Image.open(p);require(im.size==(64,64) and im.mode=='RGBA','64px RGBA '+item)
 require(set(im.getchannel('A').getdata())=={0,255},'hard alpha, no halos '+item)
 require(all(im.getpixel(pos)[3]==0 for pos in [(0,0),(0,63),(63,0),(63,63)]),'no background square '+item)
 require(im.resize((32,32),Image.Resampling.NEAREST).getbbox() is not None,'32px icon remains visible '+item)
 require(digest(p)==digest(R/'tools/runes-craft-art'/f'{item}.png'),'curated sprite source '+item)
require(len({digest(RES/f'assets/slavicmyths/textures/item/{id}.png') for id in items})==5,'five distinct symbols')
needs=[[('slavicmyths:perunite',4),('slavicmyths:bound_soul',1)],[('slavicmyths:perunite_dust',8),('slavicmyths:empowered_soul',1)],[('slavicmyths:bound_soul',1),('minecraft:emerald',8)],[('minecraft:netherite_ingot',1),('slavicmyths:diamond_dust',8)]]
for id,materials in zip(ids,needs):
 v=load(RES/f'data/slavicmyths/recipe/rune_rework_{id}.json');require(v['type']=='slavicmyths:rune_crafting' and len(v['inputs'])==3 and not v['chisel'],'three input native recipe '+id)
 require(v['inputs']==[{'ingredient':{'item':'slavicmyths:blank_rune_4'},'count':1,'base':{'tier':4,'material':'perunite'}}]+[{'ingredient':{'item':item},'count':n} for item,n in materials],'exact IV inputs/component/count '+id)
 require(v['result']=={'id':'slavicmyths:rune_'+id,'count':1},'exact IV output '+id)
v=load(RES/'data/slavicmyths/recipe/rune_foundation_blank_4.json');require(v['inputs']==[{'ingredient':{'item':'slavicmyths:blank_rune_3'},'count':1,'base':{'tier':3,'material':'perunite'}},{'ingredient':{'item':'slavicmyths:empowered_soul'},'count':2},{'ingredient':{'item':'slavicmyths:perunite_dust'},'count':8}],'exact blank IV upgrade')
require(v['result']=={'id':'slavicmyths:blank_rune_4','count':1,'components':{'slavicmyths:rune_base':{'tier':4,'material':'perunite'}}},'blank IV output component')
recipes=[]
for p in (RES/'data/slavicmyths/recipe').glob('*.json'):
 v=load(p)
 if v.get('type')=='slavicmyths:rune_crafting':
  require(1<=len(v['inputs'])<=3,'bounded inputs '+p.name);recipes.append(v)
require(not any(v['result']['id']=='slavicmyths:rune_veles_oath_iv' for v in recipes),'Veles remains Yaga exchange')
sigs=[json.dumps(v['inputs'],sort_keys=True) for v in recipes];require(len(sigs)==len(set(sigs)),'no duplicate ordered rune recipes')
for id in prior[:4]:require((RES/f'data/slavicmyths/recipe/rune_rework_{id}.json').exists(),'existing III recipe '+id)
for item in items:require('\n'+item+'\tmagic\trunes_IV' in (RES/'assets/slavicmyths/creative_catalog.tsv').read_text(),'explicit creative placement '+item)
require('tier<=4' in source('compat/JeiRunes.java') and 'getAllRecipesFor(RuneCraftRecipe.TYPE.get())' in source('compat/JeiRunes.java'),'JEI IV subtype and recipe discovery')
require('b.equals(s.get(org.slavicmyths.item.ItemState.RUNE_BASE.get()))' in source('rpg/RuneCraftRecipe.java'),'native recipe matches exact base component')
require('container.setBlockedDamage(event);org.slavicmyths.rpg.AdvancedRunes.blocked' in source('equipment/mixin/WardBlockMixin.java') and 'getDamageContainer().getBlockedDamage()<=0' in source('rpg/AdvancedRunes.java'),'Counterstrike uses existing final accepted block hook')
require('!CraftRunes.defending(victim,source)' in source('rpg/AdvancedRunes.java'),'IV suppresses III pending/cooldown')
require('defendedDamage(e.getNewDamage(),absorption)' in source('rpg/CraftRunes.java'),'defense after armor and accounted absorption')
require('s.attackAt=-1;' in source('rpg/CraftRunes.java') and 'target.getUUID().equals(s.target)' in source('rpg/CraftRunes.java'),'one direct target per attack marker; duplicate Post cannot advance series')
require('lastNightCast==cast.getLong("Start")' in source('rpg/CraftRunes.java'),'delayed scythe counts at most one primary contact per cast')
require('fortune(base,Runes.has(tool,"fortune_1"),Runes.has(tool,"mastery_iv")' in source('industry/IndustryAccessories.java'),'single existing ore-Fortune hook extended')
for token in ['LivingDamageEvent.Pre','LivingDamageEvent.Post','getAbsorptionAmount','BYPASSES_ARMOR','series.clear()','healing.stop()','p.tickCount%20!=0']:
 require(token in source('rpg/CraftRunes.java'),'dispatcher boundary '+token)
require('EventBusSubscriber' not in source('rpg/CraftRunes.java'),'no duplicate subscriber')
require('CraftRunes.tick((ServerPlayer)e.getEntity())' in source('rpg/RpgEvents.java'),'uses existing per-player timer hook')
# Reproduction check: the owned generator must preserve the complete current resource tree.
before={p.relative_to(RES).as_posix():digest(p) for p in RES.rglob('*') if p.is_file()}
subprocess.run([sys.executable,str(R/'tools/runes_craft_resources.py')],cwd=R,check=True)
after={p.relative_to(RES).as_posix():digest(p) for p in RES.rglob('*') if p.is_file()};require(before==after,'all resource bytes reproduced without foreign rollback')
def object_pairs(pairs):
 d={}
 for k,v in pairs:
  if k in d:raise ValueError('duplicate JSON key '+k)
  d[k]=v
 return d
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'),object_pairs_hook=object_pairs);require(True,'valid unique-key JSON '+p.relative_to(RES).as_posix())
properties=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
jar=R/'build/libs'/('slavicmyths-'+properties['mod_version']+'.jar');require(jar.exists(),'production JAR exists')
require(properties['minecraft_version']=='1.21.1' and properties['neo_version']=='21.1.255','target pins')
with zipfile.ZipFile(jar) as z:
 names=z.namelist();require(len(names)==len(set(names)),'no duplicate JAR entries')
 for path,h in after.items():
  if path=='META-INF/neoforge.mods.toml':
   expected=(RES/path).read_bytes().decode().replace('${mod_version}',properties['mod_version']).replace('${neo_version}',properties['neo_version'])
   require(z.read(path).decode().replace('\r\n','\n')==expected.replace('\r\n','\n'),'packaged metadata with only Gradle version expansion')
  else:require(path in names and hashlib.sha256(z.read(path)).hexdigest()==h,'packaged exact resource '+path)
 for p in (R/'build/classes/java/main').rglob('*.class'):
  path=p.relative_to(R/'build/classes/java/main').as_posix();require(path in names and hashlib.sha256(z.read(path)).hexdigest()==digest(p),'packaged exact compiled class '+path)

 for name in names:
  if name.endswith('.class'):require(int.from_bytes(z.read(name)[6:8],'big')==65,'Java21 class '+name)
 require(not any(any(token in x.lower() for token in ['slavicmyths/verify/','runescraftoffline','/smoke/','/gametest/']) for x in names),'no fixtures in production JAR')
 require('META-INF/neoforge.mods.toml' in names and 'modId="slavicmyths"' in z.read('META-INF/neoforge.mods.toml').decode(),'production NeoForge metadata')
 for name in ['CraftRunes','CraftRuneRules']:require('org/slavicmyths/rpg/'+name+'.class' in names,'packaged runtime '+name)
result={'status':'PASS','checks':len(checks),'version':properties['mod_version'],'jar':str(jar),'sha256':digest(jar),'resource_files':len(after),'real_client_starts':0,'real_server_starts':0,'gametest_starts':0,'gameplay':'NOT_RUN'}
(O/'RESOURCE_JAR_CHECKS.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
