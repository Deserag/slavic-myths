"""Current-source/resource/JAR contracts. Static checks are explicitly NOT gameplay tests."""
from pathlib import Path
import json,re,hashlib,zipfile,subprocess,sys
from PIL import Image
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';J=R/'src/main/java/org/slavicmyths';O=R/'docs/implementation/runes-patch'
checks=[]
def require(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def java(file):return (J/file).read_text(encoding='utf-8')
source=java('rpg/Runes.java');old=subprocess.check_output(['git','show','b76160800223267abc04e7c6d46812dfba8976dc:src/main/java/org/slavicmyths/rpg/Runes.java'],cwd=R).decode()
def ids(s):return re.findall(r'"([a-z_0-9]+)"',s.split('IDS={',1)[1].split('};',1)[0])
original,current=ids(old),ids(source);new=['counterstrike_iii','hunter_iii','last_stand_iii','depths_iii','veles_oath_iv']
require(len(original)==37 and current[:37]==original and current[37:]==new and len(set(current))==42,'37 original ordered IDs + five unique additions')
special={'waning':'rune_of_waning','frost':'frost_rune','frost_resistance':'frost_resistance_rune','mining_1':'mining_rune_1','mining_2':'mining_rune_2','fortune_1':'fortune_rune_1'}
for id in original:
 item=special.get(id,'rune_'+id);require((RES/f'assets/slavicmyths/models/item/{item}.json').is_file(),'original rune item model '+item)
baseline=json.loads((O/'BASELINE_HASHES.json').read_text())
for path,digest in baseline.items():require(hashlib.sha256((R/path).read_bytes()).hexdigest()==digest,'foreign source / old rune asset unchanged: '+path)
for id in new:
 model=json.loads((RES/f'assets/slavicmyths/models/item/rune_{id}.json').read_text());require(model=={'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/rune_{id}'}},'own generated model '+id)
 image=Image.open(RES/f'assets/slavicmyths/textures/item/rune_{id}.png');require(image.size==(64,64) and image.mode=='RGBA' and image.getextrema()[3][0]==0 and image.getextrema()[3][1]==255,'transparent complete icon '+id)
require(len({hashlib.sha256((RES/f'assets/slavicmyths/textures/item/rune_{id}.png').read_bytes()).hexdigest() for id in new})==5,'five distinct image hashes')
expected=[[('perunite_dust',3),('iron_dust',4)],[('diamond_dust',3),('soul_fragment',3)],[('perunite_dust',4),('empowered_soul',1)],[('diamond_dust',4),('perunite_dust',2)]]
signatures={}
for p in (RES/'data/slavicmyths/recipe').glob('*.json'):
 v=json.loads(p.read_text(encoding='utf-8'))
 if v.get('type')!='slavicmyths:rune_crafting':continue
 signature=json.dumps(sorted(v['inputs'],key=lambda x:json.dumps(x,sort_keys=True)),sort_keys=True);signatures.setdefault(signature,[]).append(p.name)
for id,needs in zip(new,expected):
 v=json.loads((RES/f'data/slavicmyths/recipe/rune_rework_{id}.json').read_text());require(v['type']=='slavicmyths:rune_crafting' and len(v['inputs'])==3 and not v['chisel'] and v['result']=={'id':'slavicmyths:rune_'+id,'count':1},'existing recipe format '+id)
 require(v['inputs'][0]=={'ingredient':{'item':'slavicmyths:blank_rune_3'},'count':1,'base':{'tier':3,'material':'perunite'}},'typed tier III blank '+id)
 require(v['inputs'][1:]==[{'ingredient':{'item':'slavicmyths:'+item},'count':count} for item,count in needs],'exact ingredients '+id)
 signature=json.dumps(sorted(v['inputs'],key=lambda x:json.dumps(x,sort_keys=True)),sort_keys=True);require(len(signatures[signature])==1,'recipe has no duplicate signature '+id)
require(not any('rune_veles_oath_iv' in p.read_text() for p in (RES/'data/slavicmyths/recipe').glob('*.json')),'IV has no crafting recipe')
for lang in ['ru_ru','en_us']:
 d=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
 for id in new:
  for prefix in ['item.slavicmyths.rune_','runes_patch.effect.','runes_patch.compatible.']:require(bool(d.get(prefix+id)),lang+' translation '+prefix+id)
 for key in ['rpg.slavicmyths.iv_occupied','yaga.fail.gorynych_claim','runes_patch.extra_inactive']:require(key in d,lang+' reason '+key)
code=''.join(p.read_text(encoding='utf-8') for p in J.rglob('*.java'))
tag=json.loads((RES/'data/slavicmyths/tags/entity_type/rune_death_immune.json').read_text())
require(all('slavicmyths:'+id in tag['values'] for id in ['ataman','buried_volkhv','ovinnik','volkolak','fire_serpent','podvey','morozko','karachun','zmey_gorynych','great_poloz','earth_spirit','kartaus','kalin_tsar']),'all actual arena/hunt/story boss families covered')
for id in tag['values']:
 if id.startswith('slavicmyths:'):require('.register("'+id.split(':')[1]+'"' in code,'death immunity uses real registry ID '+id)
require('removeEffect(MobEffects.MOVEMENT_SLOWDOWN)' not in java('morozko/FrostCold.java')+java('morozko/FrostEvents.java') and 'void immunity(' not in java('morozko/FrostEvents.java'),'no broad frost removal/applicability veto')
require('!FrostCold.resistant(t)' in java('morozko/FrostEvents.java'),'frost rune protection at own application source')
require('AdvancedDirect(source,from)' in java('rewards/RewardEvents.java') and 'clock>=n.getLong("RewardSuppressed")' in java('rewards/RewardEvents.java'),'suppression direct attacks until deadline')
for call in ['RuneRuntime.refresh(p)','RareRuneRuntime.refresh(p)','EarthRune.refresh(p)']:require(call in source,'anvil refresh '+call)
require('new int[]{6,15,28}' in source and 'action==1?7:2' in source,'existing socket/operation costs')
require('AdvancedRunes.boss(target)' in java('rpg/RareRuneRuntime.java'),'execution class defense in addition to tag')
require('RuneEffects.killTarget(p,e.getEntity())' in java('rpg/RpgEvents.java') and 'e.isCanceled()||e.getEntity().level().isClientSide' in java('rpg/RpgEvents.java'),'life uses final death policy without requiring nonzero victim HP')
require('storyClaimed(server)' in java('yaga/YagaMenu.java') and 'YagaServices.consume' in java('yaga/YagaMenu.java'),'server story gate before transaction')
require('CreativeClaim' in java('gorynych/GorynychRewards.java') and 'CreativeClaim' in java('rpg/AdvancedRunes.java'),'new creative receipts excluded')
for token in ['RunesShotX','RunesHunterUsed','pending.source()!=e.getSource()','PolozRewardEvents.secondary(p)','LAST_PENDING']:
 require(token in java('rpg/AdvancedRunes.java'),'server dispatcher guard '+token)
for id,params in {'forest':{'melee_damage':1.0,'arrow_speed':.08,'shield_duration':60.0,'staff_cooldown':600.0},'shadow':{'dagger_damage':1.25,'speed_duration':60.0,'bow_multiplier':1.1},'midday':{'melee_damage':1.0,'bow_multiplier':1.12},'life':{'shield_health_threshold':.5,'kill_healing':3.0,'kill_cooldown':800.0}}.items():
 part=java('rpg/RuneDefinition.java').split('new RuneDefinition("'+id+'"',1)[1].split('Set.of(',1)[0]
 for key,v in params.items():require(float(re.search(r'Map.entry\("'+key+r'",([\d.]+)\)',part)[1])==v,'runtime/tooltip shared '+id+'.'+key)
resource_count=0
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));resource_count+=1
require(resource_count>0,'all source JSON syntax')
result={'status':'PASS','checks':len(checks),'source_json_count':resource_count,'runtime_runs':0,'scope':'static source/resource contracts; not gameplay or data-loader acceptance'}
if '--jar' in sys.argv:
 jar=R/'build/libs/slavicmyths-1.9.7.jar'
 with zipfile.ZipFile(jar) as z:
  require(z.testzip() is None,'production ZIP CRC');names=set(z.namelist());require('META-INF/neoforge.mods.toml' in names,'NeoForge metadata')
  require(not any('Offline' in n or n.startswith(('tools/','smoke/')) or n.endswith('Checks.class') for n in names),'offline/smoke fixtures excluded')
  for p in RES.rglob('*'):
   if not p.is_file():continue
   name=p.relative_to(RES).as_posix();require(name in names,'packaged resource '+name)
   if name.startswith(('assets/','data/')):require(z.read(name)==p.read_bytes(),'exact packaged bytes '+name)
  for name in names:
   if name.endswith('.class'):require(int.from_bytes(z.read(name)[6:8],'big')==65,'Java21 '+name)
  for name in ['AdvancedRunes','AdvancedRuneRules']:require('org/slavicmyths/rpg/'+name+'.class' in names,'new common server class '+name)
 result.update(checks=len(checks),jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest())
(O/('RESOURCE_JAR_CHECKS.json' if '--jar' in sys.argv else 'SOURCE_CHECKS.json')).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
