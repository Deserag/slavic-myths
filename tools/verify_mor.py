"""Production, source, linked-resource and baseline gates. Does not launch a game."""

if __name__ == "__main__":
    raise SystemExit('ARCHIVED historical tool. See docs/maintenance/issue-43/HISTORICAL_ARTIFACTS.md; restore its original revision and verified inputs in a separate historical checkout. Current source is not its baseline.')

from pathlib import Path
from PIL import Image
import hashlib,json,zipfile,struct,re,subprocess,sys
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/mor-1.8.1';checks=0
def check(ok,label):
 global checks
 checks+=1
 if not ok:raise AssertionError(label)
log=(R/'work/mor-1.8.1/clean-build-final.log').read_text(encoding='utf-8',errors='replace')
check('BUILD SUCCESSFUL' in log,'clean build failed');match=re.search(r'MOR OFFLINE PASS checks=(\d+) native_meshes=(\d+)',log);check(match is not None,'offline native checks missing');check('UI_LOGIC_PASS checks=71' in log,'UI regression missing')
task=R/'docs/tasks/1.8.1';manifest=json.loads((task/'MANIFEST_SHA256.json').read_text())
for path,digest in manifest['files'].items():check(hashlib.sha256((task/path).read_bytes()).hexdigest()==digest,'altered source specification '+path)
jar=R/'build/libs/slavicmyths-1.8.1.jar';prior=R/'release/slavicmyths-1.7.9.jar'
allow_classes=['SlavicMyths','entity/LandSpiritEntity','entity/LeshyEntity','entity/PoludnitsaEntity','entity/KikimoraEntity','hunt/HuntMob','hunt/ElementHuntMob','registry/ModItems','registry/ModItemGroup','tales/TaleEvents',*('client/'+c for c in ['ClientSetup','LeshyModel','LeshyRenderer','PoludnitsaModel','PoludnitsaRenderer','KikimoraModel','KikimoraRenderer','ElementModel','ElementRenderer'])]
allow_resources={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'}
vanilla_sounds=json.loads((R/'work/vanilla-sounds.json').read_text())
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(prior) as old:
 names=set(z.namelist());old_names=set(old.namelist());check(old_names<=names,'existing production entry removed')
 changed=[n for n in old_names if z.read(n)!=old.read(n)]
 for n in changed:check(n in allow_resources or any(n=='org/slavicmyths/'+c+'.class' or n.startswith('org/slavicmyths/'+c+'$') for c in allow_classes),'unexpected old production change '+n)
 for n in names:
  check(not any(x in n.lower() for x in ('gametest','tools/smoke','morchecks','uilogicchecks','test-fixtures')),'fixture leakage '+n)
  if n.endswith('.class'):
   check(struct.unpack('>H',z.read(n)[6:8])[0]==65,'Java21 '+n)
   check((R/'build/classes/java/main'/n).read_bytes()==z.read(n),'stale production class '+n)
  p=R/'src/main/resources'/n
  if p.is_file() and n!='META-INF/neoforge.mods.toml':check(p.read_bytes()==z.read(n),'stale resource '+n)
  if n.endswith('.json'):json.loads(z.read(n));check(True,'JSON syntax '+n)
 check('version="1.8.1"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'NeoForge metadata version')
 for lang in ('ru_ru','en_us'):
  prefix='assets/slavicmyths/lang/'+lang+'.json';before=json.loads(old.read(prefix));after=json.loads(z.read(prefix))
  for key,value in before.items():check(after.get(key)==value,'old localization altered '+lang+' '+key)
  for key in ['item.slavicmyths.mor_flask','item.slavicmyths.mor_flask.tooltip','tales.entry.mor_first_signs.title',*('tales.entry.mor_first_signs.'+s for s in ('rumor','discovered','studied','verified')),*('entity.slavicmyths.mor_'+s for s in ('leshy','poludnitsa','kikimora','fire_serpent'))]:check(after.get(key) and '\ufffd' not in after[key],'missing translation '+key)
 sounds=json.loads(z.read('assets/slavicmyths/sounds.json'));check(sounds==json.loads(old.read('assets/slavicmyths/sounds.json')),'existing sounds altered')
 for key,event in sounds.items():
  for entry in event['sounds']:
   entry={'name':entry} if isinstance(entry,str) else entry;name=entry['name'];space,path=name.split(':',1) if ':' in name else ('slavicmyths',name)
   if entry.get('type')=='event':check(path in vanilla_sounds if space=='minecraft' else path in sounds,'missing audio event '+name)
   elif space=='slavicmyths':check('assets/slavicmyths/sounds/'+path+'.ogg' in names,'missing OGG '+name)
 # SoundEvents used by MorAudio were verified against the installed primary source/API.
 for event in ['block.wood.break','block.grass.break','item.axe.scrape','entity.silverfish.hurt','item.crossbow.loading_middle','entity.blaze.ambient','block.fire.ambient','block.rooted_dirt.break','block.wood.step','block.beacon.power_select','entity.player.attack.sweep','entity.witch.celebrate','entity.phantom.flap','entity.illusioner.mirror_move','entity.ravager.roar','entity.breeze.wind_burst','block.wool.step','block.tripwire.attach','entity.blaze.shoot','item.elytra.flying']:
  check(event in vanilla_sounds,'Mor vanilla audio absent '+event)
 for name,size in [('leshy',64),('poludnitsa',256),('kikimora',256),('fire_serpent',512)]:
  for suffix in ('','_forming'):
   path='assets/slavicmyths/textures/entity/mor/'+name+suffix+'.png';check(path in names,'missing mor atlas '+path)
   with Image.open(R/'src/main/resources'/path) as im:check(im.size==(size,size) and len(im.getcolors(size*size) or [])>=5,'atlas dimensions/real colors '+path)
  check('assets/slavicmyths/models/item/mor_'+name+'_spawn_egg.json' not in names,'mor egg introduced')
 for n in names:
  if '/models/' in n and n.endswith('.json'):
   for value in json.loads(z.read(n)).get('textures',{}).values():
    if value.startswith('#'):continue
    ns,path=value.split(':',1) if ':' in value else ('minecraft',value)
    if ns=='slavicmyths':check('assets/slavicmyths/textures/'+path+'.png' in names,'model texture missing '+value)
  if n.startswith('data/') and any(k in n for k in ('/recipe/','/loot_table/')):check('slavicmyths:mor_flask' not in z.read(n).decode(errors='replace'),'flask obtainable through survival recipe/loot '+n)
 with Image.open(R/'src/main/resources/assets/slavicmyths/textures/item/mor_flask.png') as im:check(im.size==(16,16) and im.mode=='RGBA' and im.getextrema()[3][0]==0,'flask actual transparent 16px sprite')
 check(json.loads(z.read('assets/slavicmyths/models/item/mor_flask.json'))['parent']=='minecraft:item/generated','held/GUI flask model')
 entry=json.loads(z.read('data/slavicmyths/tales/mor_first_signs.json'));check(entry['part']=='creatures' and entry['chapter']=='forest' and entry['subjects']==[],'single encounter-discovered tale in existing section')
 for particle in ('mor_spark','mor_ash'):check(json.loads(z.read('assets/slavicmyths/particles/'+particle+'.json'))['textures']==['slavicmyths:mor_square'],'real particle sprite link')
 for texture in ('bolt','root'):check('assets/slavicmyths/textures/entity/mor/'+texture+'.png' in names,'technical effect sprite '+texture)
for p in (R/'src/main/java/org/slavicmyths/mor').glob('*.java'):
 source=p.read_text();check('org.slavicmyths.client' not in source and 'net.minecraft.client' not in source,'client linkage from common '+p.name)
 for bad in ('getAllEntities','setBlock(','setBlockAndUpdate','setChunkForced','getChunkAt(','addRegionTicket','setRemainingFireTicks','explode('):check(bad not in source,'forbidden world scan/grief/load '+p.name+' '+bad)
check('mor_flask' in (R/'src/main/java/org/slavicmyths/registry/ModItemGroup.java').read_text(),'flask categorized in existing Creative tab')
check('if(morClone())' in (R/'src/main/java/org/slavicmyths/hunt/ElementHuntMob.java').read_text(),'clone death bypass')
assets=list((R/'src/main/resources/assets/slavicmyths/textures/entity/mor').glob('*.png'))+[R/'src/main/resources/assets/slavicmyths/textures/item/mor_flask.png',*(R/'src/main/resources/assets/slavicmyths/lang'/f'{l}.json' for l in ('ru_ru','en_us')),R/'src/main/resources/data/slavicmyths/tales/mor_first_signs.json']
before={p:p.read_bytes() for p in assets};subprocess.run([sys.executable,str(R/'tools/mor_assets.py')],check=True,cwd=R)
for p,blob in before.items():check(p.read_bytes()==blob,'generator drift '+str(p))
result=dict(version='1.8.1',status='PASS',checks=checks,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),size=jar.stat().st_size,event_subscribers=subscribers(jar),offline_assertions=int(match[1]),native_profiles=int(match[2]),ui_checks=71,old_entries_identical=len(old_names)-len(changed),changed_old_entries=sorted(changed),client_starts=0,server_starts=0,visual_audio='REQUIRES_MANUAL_QA',real_multiplayer_rejoin_mspt='NOT_RUN')
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('MOR PRODUCTION/RESOURCE PASS checks='+str(checks)+' sha256='+result['sha256'])
