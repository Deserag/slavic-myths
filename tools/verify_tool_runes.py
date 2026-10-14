"""Current tree preservation, exact tool-rune resources and production-JAR gates. No game launch."""
from pathlib import Path
import json,re,hashlib,zipfile,subprocess,sys
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/tool-runes-iii-iv';RES=R/'src/main/resources';J=R/'src/main/java/org/slavicmyths'
checks=[]
def require(value,label):
 if not value:raise AssertionError(label)
 checks.append(label)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def load(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def src(p):return (J/p).read_text(encoding='utf-8')
ids=[f'{f}_{t}' for t in ['iii','iv'] for f in ['miner','excavator','farmer','lumberjack']]
java_old=['rpg/Runes.java','rpg/RuneDefinition.java','rpg/RuneRework.java','rpg/RpgEvents.java','rpg/AdvancedRunes.java']
allowed={'src/main/java/org/slavicmyths/'+p for p in java_old}|{'src/main/resources/assets/slavicmyths/lang/'+l+'.json' for l in ['ru_ru','en_us']}|{'src/main/resources/assets/slavicmyths/creative_catalog.tsv','src/main/resources/META-INF/neoforge.mods.toml'}
baseline=load(O/'BASELINE_HASHES.json')
for path,h in baseline.items():
 if path.startswith('src/main/') and path not in allowed:require(digest(R/path)==h,'preserved pre-task production file '+path)
require(digest(R/'gradle.properties')==baseline['gradle.properties'],'version/dependency pins unchanged')
runids=re.findall(r'"([a-z_0-9]+)"',src('rpg/Runes.java').split('IDS={',1)[1].split('};',1)[0])
oldids=['thunder','heat','forest','midday','shadow','protection','life','wind','strength','speed','resilience','crushing','blood','fortitude','floating_weapon','flight','rare_protection','death','berserker','sacrifice','mining_1','mining_2','fortune_1','ominous_aura','earth_heart_iii','stomp','dispelling','pursuit','suppression','battle_line','earth_lord_iv','frost_resistance','frost','waning','mor_breath','dragon_flame','storm_strike','counterstrike_iii','hunter_iii','last_stand_iii','depths_iii','veles_oath_iv','unbreakable_iv','perun_rage_iv','forest_spirit_iv','mastery_iv']
require(runids==oldids+ids and len(runids)==len(set(runids))==54,'all 46 previous IDs in order + exactly eight new IDs')
require('ToolRuneRules.IDS)NEW_ITEMS.put(id,ModItems.ITEMS.register("rune_"+id' in src('rpg/RuneRework.java'),'eight items use prior DeferredRegister path')
require('ToolRuneRules.IDS)ALL.put(id,new RuneDefinition' in src('rpg/RuneDefinition.java'),'all eight definitions are real')
require('ToolRuneRules.conflict(installed,id)' in src('rpg/Runes.java') and 'ToolRuneContent.supports(equipment,id)' in src('rpg/Runes.java'),'server admission + compatibility paths')
for item in ['PickaxeItem','ShovelItem','HoeItem','AxeItem']:require('instanceof '+item in src('rpg/tools/ToolRuneContent.java'),'actual tool class matrix '+item)
category=src('rpg/Runes.java').split('public static String category(',1)[1].split('public static int capacity(',1)[0]
require('"shovel"' not in category and '"hoe"' not in category,'new sockets do not reclassify shovel/hoe as combat weapons')
needs=[[('perunite_dust',4),('diamond_dust',4)],[('iron_dust',8),('perunite_dust',2)],[('gold_dust',4),('soul_fragment',4)],[('perunite_dust',3),('coal_dust',4)],[('perunite_dust',10),('bound_soul',1)],[('diamond_dust',8),('bound_soul',1)],[('gold_dust',8),('bound_soul',1)],[('perunite_dust',8),('bound_soul',1)]]
for id,materials in zip(ids,needs):
 tier=4 if id.endswith('_iv') else 3;v=load(RES/f'data/slavicmyths/recipe/tool_rune_{id}.json')
 require(v['type']=='slavicmyths:rune_crafting' and len(v['inputs'])==3 and not v['chisel'],'native three input recipe '+id)
 require(v['inputs']==[{'ingredient':{'item':f'slavicmyths:blank_rune_{tier}'},'count':1,'base':{'tier':tier,'material':'perunite'}}]+[{'ingredient':{'item':'slavicmyths:'+item},'count':n} for item,n in materials],'exact tier/component/counts '+id)
 require(v['result']=={'id':'slavicmyths:rune_'+id,'count':1},'exact selected output '+id)
 require(load(RES/f'assets/slavicmyths/models/item/rune_{id}.json')=={'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/rune_'+id}},'generated model '+id)
 p=RES/f'assets/slavicmyths/textures/item/rune_{id}.png';im=Image.open(p)
 require(im.mode=='RGBA' and im.size==(64,64),'64 RGBA '+id);require(set(im.getchannel('A').getdata())=={0,255},'hard alpha '+id)
 require(all(im.getpixel(p)[3]==0 for p in [(0,0),(0,63),(63,0),(63,63)]),'no background rectangle '+id)
 require(im.resize((32,32),Image.Resampling.NEAREST).getbbox() is not None,'32px visible '+id)
 require(digest(p)==digest(R/f'tools/tool-runes-art/rune_{id}.png'),'curated sprite is production source '+id)
require(len({digest(RES/f'assets/slavicmyths/textures/item/rune_{id}.png') for id in ids})==8,'eight independent sprites')
for lang in ['ru_ru','en_us']:
 path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json';text=(R/path).read_text(encoding='utf-8');translations=json.loads(text)
 keys=['item.slavicmyths.rune_'+id for id in ids]+['tool_runes.effect.'+id for id in ids]+['tool_runes.compatible.'+family for family in ['miner','excavator','farmer','lumberjack']]+['tool_runes.crafting','tool_runes.inactive','rpg.slavicmyths.tool_family_occupied']
 for key in keys:
  require(bool(translations.get(key)),lang+' translated '+key);text,n=re.subn(r',\n  "'+re.escape(key)+r'": "(?:[^"\\]|\\.)*"','',text);require(n==1,lang+' one owned key '+key)
 require(any(hashlib.sha256(s.encode()).hexdigest()==baseline[path] for s in [text,text.replace('\n','\r\n')]),'all previous localized bytes preserved '+lang)
path='src/main/resources/assets/slavicmyths/creative_catalog.tsv';catalog=(R/path).read_text(encoding='utf-8')
for id in ids:
 row=f'rune_{id}\tmagic\trunes_'+('IV' if id.endswith('_iv') else 'III')+'\n';require(catalog.count(row)==1,'catalog unique expected tier '+id);catalog=catalog.replace(row,'')
require(any(hashlib.sha256(s.encode()).hexdigest()==baseline[path] for s in [catalog,catalog.replace('\n','\r\n')]),'previous catalog entries/order retained byte-for-byte')
require('selectedRecipe()' in src('rpg/RpgMenu.java') and 'recipeIndex=index' in src('rpg/RpgMenu.java') and 'crafting.removeItem(n,r.inputs().get(n).count())' in src('rpg/RpgMenu.java'),'explicit recipe selection disambiguates miner IV 10 dust vs lumberjack IV 8 dust')
require('getAllRecipesFor(RuneCraftRecipe.TYPE.get())' in src('compat/JeiRunes.java') and 'displayItems()' in src('compat/JeiRunes.java'),'JEI generic discovery and component/count displays preserved')
require('b.equals(s.get(org.slavicmyths.item.ItemState.RUNE_BASE.get()))' in src('rpg/RuneCraftRecipe.java'),'typed base component checked by actual craft recipe')
code=src('rpg/tools/ToolRuneRuntime.java')
for token in ['p.isShiftKeyDown()','block.getDirection()!=e.getFace()','p.pick(p.blockInteractionRange(),1,false)','p.gameMode.destroyBlock(pos)','player.gameMode.useItemOn(','mode==GameType.SURVIVAL||mode==GameType.CREATIVE','w.mayInteract(p,pos)','p.blockActionRestricted(','s.hasBlockEntity()','s.getFluidState().isEmpty()','s.getDestroySpeed(w,p)>=0','tool.isCorrectToolForDrops(s)','s.equals(target.expected)','player.getMainHandItem()!=stack','player.serverLevel()!=world','player.gameMode.getGameModeForPlayer()!=gameMode','player.getInventory().selected!=slot','now(player)>expires','GUARD.add(id)','finally{REMOVED.remove(id);GUARD.remove(id);}','captureBlockSnapshots','restoringBlockSnapshots','context.getLevel()!=frame.world','p.serverLevel()!=frame.world','ToolRuneRules.BREAKS_PER_TICK','ToolRuneRules.READS_PER_TICK','ToolLogOrigins.get(world).blocked(pos)','getToPush()','origins.denyChunk']:
 require(token in code,'runtime safety boundary '+token)
require('setBlock(' not in code and 'dropResources(' not in code and 'getChunkAt(' not in code,'no raw removal, duplicate loot roll or forced chunk acquisition')
require('frame.removed' in src('rpg/tools/mixin/ToolRuneBreakMixin.java') and 'method="removeBlock"' in src('rpg/tools/mixin/ToolRuneBreakMixin.java'),'actual removal confirmation, not destroyBlock true alone')
require('CommonHooks.onPlaceItemIntoWorld(context)' in src('rpg/tools/mixin/ToolRuneUseMixin.java'),'post-transaction use path')
require('ToolRuneRuntime.tick((ServerPlayer)e.getEntity())' in src('rpg/RpgEvents.java') and 'ToolRuneRuntime.clear(p)' in src('rpg/AdvancedRunes.java'),'existing timer/lifecycle/anvil hooks used')
require('instanceof org.slavicmyths.farming.FarmingCrop' in code and 'NetherWartBlock.AGE)==3' in code,'known single-block mature crop set, no unvalidated double crops')
for p in (J/'rpg/tools').rglob('*.java'):require('net.minecraft.client' not in p.read_text(encoding='utf-8') and 'mezz.jei' not in p.read_text(encoding='utf-8'),'common/client isolation '+p.name)
metadata=(RES/'META-INF/neoforge.mods.toml').read_text(encoding='utf-8');restored=metadata.replace('[[mixins]]\nconfig="slavicmyths.tool_runes.mixins.json"\n','')
require(any(hashlib.sha256(s.encode()).hexdigest()==baseline['src/main/resources/META-INF/neoforge.mods.toml'] for s in [restored,restored.replace('\n','\r\n')]),'previous NeoForge metadata unchanged except one mixin config')
require(load(RES/'slavicmyths.tool_runes.mixins.json')['mixins']==['ToolRuneBreakMixin','ToolRuneUseMixin'],'exact two required server/common hooks')
before={p.relative_to(RES).as_posix():digest(p) for p in RES.rglob('*') if p.is_file()};subprocess.run([sys.executable,str(R/'tools/tool_runes_resources.py')],cwd=R,check=True);after={p.relative_to(RES).as_posix():digest(p) for p in RES.rglob('*') if p.is_file()};require(before==after,'owned generator reproduces complete current resource tree without rollback')
def unique(pairs):
 out={}
 for k,v in pairs:
  if k in out:raise ValueError('duplicate JSON key '+k)
  out[k]=v
 return out
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'),object_pairs_hook=unique);require(True,'unique-key JSON '+p.relative_to(RES).as_posix())
props=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'));jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar');require(jar.is_file(),'production JAR exists')
require(props['mod_version']=='1.9.7.3' and props['minecraft_version']=='1.21.1' and props['neo_version']=='21.1.255','release/platform versions preserved')
with zipfile.ZipFile(jar) as z:
 names=z.namelist();require(len(names)==len(set(names)),'no duplicate JAR entries')
 for path,h in after.items():
  if path=='META-INF/neoforge.mods.toml':
   expected=(RES/path).read_text(encoding='utf-8').replace('${mod_version}',props['mod_version']).replace('${neo_version}',props['neo_version']);require(z.read(path).decode().replace('\r\n','\n')==expected,'only normal Gradle metadata expansion')
  else:require(path in names and hashlib.sha256(z.read(path)).hexdigest()==h,'exact packaged resource '+path)
 for p in (R/'build/classes/java/main').rglob('*.class'):
  path=p.relative_to(R/'build/classes/java/main').as_posix();require(path in names and hashlib.sha256(z.read(path)).hexdigest()==digest(p),'exact packaged compiled class '+path)
 for name in names:
  if name.endswith('.class'):require(int.from_bytes(z.read(name)[6:8],'big')==65,'Java21 '+name)
 require(not any(any(t in name.lower() for t in ['/verify/','/smoke/','toolrunesoffline','runescraftoffline','/gametest/']) for name in names),'no test fixtures in production')
 for p in (J/'rpg/tools').rglob('*.java'):require('org/slavicmyths/rpg/tools/'+p.relative_to(J/'rpg/tools').with_suffix('.class').as_posix() in names,'packaged implementation '+p.name)
result={'status':'PASS','checks':len(checks),'version':props['mod_version'],'jar':str(jar),'sha256':digest(jar),'resource_files':len(after),'old_rune_ids':46,'new_tool_rune_ids':8,'total_rune_ids':54,'client_starts':0,'server_starts':0,'gametest_starts':0,'polymc_changes':0,'gameplay':'NOT_RUN'}
(O/'RESOURCE_JAR_CHECKS.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
