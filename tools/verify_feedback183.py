"""Production gates for 1.8.3 against installed 1.8.2. No game or world construction."""
from pathlib import Path
from PIL import Image
import zipfile,json,hashlib,re,struct,subprocess,sys
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/feedback-1.8.3';checks=0
def check(ok,label):
 global checks
 checks+=1
 if not ok:raise AssertionError(label)
log=(R/'work/feedback-1.8.3/clean-build-final.log').read_text(encoding='utf-8',errors='replace');check('BUILD SUCCESSFUL' in log,'clean build gate');match=re.search(r'HIGHER MOR OFFLINE PASS checks=(\d+) native_meshes=(\d+)',log);check(match is not None,'native/policy/NBT checks');grips=re.search(r'FEEDBACK183_NATIVE_PASS checks=(\d+) meshes=(\d+)',(R/'work/feedback-1.8.3/connected-native.log').read_text(encoding='utf-8',errors='replace'));check(grips is not None,'production hero palm/pose gates');check('UI_LOGIC_PASS checks=71' in log,'existing UI regression gate')
with zipfile.ZipFile('C:/Users/pavel/Downloads/Slavic_Myths_1.8.2_Codex_TZ.zip') as spec:
 for name in spec.namelist():
  if not name.endswith('/'):check((R/'docs/tasks/1.8.2'/name).read_bytes()==spec.read(name),'unaltered specification '+name)
jar=R/'build/libs/slavicmyths-1.8.3.jar';prior=R/'work/feedback-1.8.3/baseline.jar'
allowed=['entity/LandSpiritEntity','entity/OvinnikEntity','hunt/HuntMob','hunt/WorldBoss','hunt/EmberClump',*('client/'+c for c in ['ClientSetup','HuntModel','HuntRenderer','WorldBossModel','WorldBossRenderer']),*('mor/'+c for c in ['MorController','MorRules','MorAudio','MorEvents','MorContent'])]
allowed += ['bogatyr/HeroThreats','bogatyr/IdolishcheEntity','marinka/MarinkaEntity','skim/SkimBeastEntity','finist/KartausEntity','kalin/KalinEntity','mor/HigherMor','client/IlyaMesh','client/AlyoshaMesh','client/DobrynyaMesh','client/FinistMesh','client/BogatyrClient','client/SkimModel','client/WitchModel','client/FinistModel']
allowed_resources={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'}
allowed_resources.update('assets/slavicmyths/textures/entity/'+n+'.png' for n in ['ilya_muromets','alyosha_popovich','dobrynya_nikitich','finist'])
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(prior) as old:
 names=set(z.namelist());oldnames=set(old.namelist());check(oldnames<=names,'no old production entry removed');changed=[n for n in oldnames if old.read(n)!=z.read(n)]
 for n in changed:check(n in allowed_resources or any(n=='org/slavicmyths/'+c+'.class' or n.startswith('org/slavicmyths/'+c+'$') for c in allowed),'unrelated production entry changed '+n)
 for n in names:
  check(not any(w in n.lower() for w in ['gametest','higherchecks','morchecks','tools/smoke','uilogicchecks','test-fixtures']),'test leakage '+n)
  if n.endswith('.class'):
   check(struct.unpack('>H',z.read(n)[6:8])[0]==65,'Java21 '+n);check(z.read(n)==(R/'build/classes/java/main'/n).read_bytes(),'stale class '+n)
  p=R/'src/main/resources'/n
  if p.is_file() and n!='META-INF/neoforge.mods.toml':check(z.read(n)==p.read_bytes(),'stale resource '+n)
  if n.endswith('.json'):json.loads(z.read(n));check(True,'valid json '+n)
  if n.endswith('.png'):
   with Image.open(R/'src/main/resources'/n) as im:im.verify();check(True,'decodable png '+n)
  if '/models/' in n and n.endswith('.json'):
   for texture in json.loads(z.read(n)).get('textures',{}).values():
    if texture.startswith('slavicmyths:'):check('assets/slavicmyths/textures/'+texture.split(':',1)[1]+'.png' in names,'linked texture '+texture)
 check('version="1.8.3"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'NeoForge version metadata')
 for language in ['ru_ru','en_us']:
  prefix='assets/slavicmyths/lang/'+language+'.json';before=json.loads(old.read(prefix));after=json.loads(z.read(prefix))
  for key,value in before.items():
   if key!='item.slavicmyths.mor_flask.tooltip':check(after.get(key)==value,'existing localization '+language+' '+key)
  for key in ['entity.slavicmyths.mor_likho','entity.slavicmyths.mor_ovinnik','entity.slavicmyths.mor_zone','item.slavicmyths.mor_flask.tooltip']:check(after.get(key) and '\ufffd' not in after[key],'new translation '+key)
 for name,size in [('likho',512),('ovinnik',256)]:
  for suffix in ['', '_forming']:
   with Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/mor'/ (name+suffix+'.png')) as im:check(im.size==(size,size) and len(im.getcolors(size*size) or [])>5,'authored atlas '+name+suffix)
  check(not any('mor_'+name+'_spawn_egg' in n for n in names),'no new eggs '+name)
 for name in ['ground_likho','ground_ovinnik','cloud']:check('assets/slavicmyths/textures/entity/mor/'+name+'.png' in names,'ground/cloud sprite '+name)
 check(z.read('assets/slavicmyths/sounds.json')==old.read('assets/slavicmyths/sounds.json'),'original sound registrations preserved; vanilla events reused')
 for path in (R/'src/main/java/org/slavicmyths/mor').glob('*.java'):
  s=path.read_text(encoding='utf-8');check('net.minecraft.client' not in s and 'org.slavicmyths.client' not in s,'server/client isolation '+path.name)
  for forbidden in ['setBlock(', 'setBlockAndUpdate(', 'getAllEntities(', 'getChunkAt(', 'setChunkForced(', 'addRegionTicket(', '.explode(']:check(forbidden not in s,'no grief/forced load/global scan '+path.name)
 assets=[R/'src/main/resources/assets/slavicmyths/textures/entity'/(n+'.png') for n in ['ilya_muromets','alyosha_popovich','dobrynya_nikitich','finist']]+list((R/'src/main/resources/assets/slavicmyths/textures/entity/mor').glob('*.png'))+[R/'src/main/resources/assets/slavicmyths/lang'/ (l+'.json') for l in ['ru_ru','en_us']]
 before={p:p.read_bytes() for p in assets}
 for generator in ['mor_assets.py','higher_mor_assets.py']:
  subprocess.run([sys.executable,str(R/'tools'/generator)],cwd=R,check=True)
  for p,data in before.items():check(p.read_bytes()==data,'generator drift '+generator+' '+str(p))
for family,file in [('hunt','HuntMob'),('finist','KartausEntity'),('kalin','KalinEntity')]:
 source=(R/'src/main/java/org/slavicmyths'/family/(file+'.java')).read_text(encoding='utf-8');guard=source.index('if(!isAlive()){bar.setVisible(false);bar.removeAllPlayers();return;}');check(guard<source.index('bar.setProgress('),'dead entity cannot reattach bar '+file)
for family,file in [('bogatyr','IdolishcheEntity'),('marinka','MarinkaEntity'),('skim','SkimBeastEntity'),('finist','KartausEntity'),('kalin','KalinEntity'),('hunt','HuntMob')]:
 source=(R/'src/main/java/org/slavicmyths'/family/(file+'.java')).read_text(encoding='utf-8');check('onRemovedFromLevel()' in source and 'bar.removeAllPlayers()' in source,'bar cleanup on removal '+file)
hero=(R/'src/main/java/org/slavicmyths/bogatyr/HeroThreats.java').read_text(encoding='utf-8');check('&& !blighted(enemy)' in hero and 's.morState()==2' in hero,'all active Mor variants recognized by hero target filter')
# Exact scoped generators, no full old release regeneration.
commands=["import bogatyr_assets as m;m.hero()","import skim_assets as m;m.hero()","import marinka_assets as m;m.hero()","from pathlib import Path;s={'__file__':str(Path('tools/finist_assets.py').resolve())};exec(Path('tools/finist_assets.py').read_text(encoding='utf-8').split('humanoid();humanoid(True);falcon();props();props(True)')[0],s);s['humanoid']()"]
hero_files=[R/'src/main/java/org/slavicmyths/client'/(n+'Mesh.java') for n in ['Ilya','Alyosha','Dobrynya','Finist']]+[R/'src/main/resources/assets/slavicmyths/textures/entity'/(n+'.png') for n in ['ilya_muromets','alyosha_popovich','dobrynya_nikitich','finist']]
original={p:p.read_bytes() for p in hero_files}
for cmd in commands:subprocess.run([sys.executable,'-c',"import sys;sys.path.insert(0,'tools');"+cmd],cwd=R,check=True)
for p,blob in original.items():check(p.read_bytes()==blob,'hero generator drift '+str(p))
result=dict(version='1.8.3',status='PASS',checks=checks,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),size=jar.stat().st_size,offline_assertions=int(match[1]),hero_grip_uv_checks=int(grips[1]),hero_pose_profiles=int(grips[2]),native_profiles=int(match[2]),ui_checks=71,event_subscribers=subscribers(jar),old_entries_identical=len(oldnames)-len(changed),changed_old_entries=sorted(changed),new_entries=sorted(names-oldnames),client_starts=0,server_starts=0,gameplay_visual_audio_multiplayer_rejoin_mspt='NOT_RUN')
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print('FEEDBACK183 RESOURCE/PRODUCTION PASS checks='+str(checks)+' sha256='+result['sha256'])
