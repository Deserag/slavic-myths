"""Production gates for 1.8.2 against installed 1.8.1. No game or world construction."""
from pathlib import Path
from PIL import Image
import zipfile,json,hashlib,re,struct,subprocess,sys
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/higher-1.8.2';checks=0
def check(ok,label):
 global checks
 checks+=1
 if not ok:raise AssertionError(label)
log=(R/'work/higher-1.8.2/clean-build-final.log').read_text(encoding='utf-8',errors='replace');check('BUILD SUCCESSFUL' in log,'clean build gate');match=re.search(r'HIGHER MOR OFFLINE PASS checks=(\d+) native_meshes=(\d+)',log);check(match is not None,'native/policy/NBT checks');check('UI_LOGIC_PASS checks=71' in log,'existing UI regression gate')
with zipfile.ZipFile('C:/Users/pavel/Downloads/Slavic_Myths_1.8.2_Codex_TZ.zip') as spec:
 for name in spec.namelist():
  if not name.endswith('/'):check((R/'docs/tasks/1.8.2'/name).read_bytes()==spec.read(name),'unaltered specification '+name)
jar=R/'build/libs/slavicmyths-1.8.2.jar';prior=R/'work/higher-1.8.2/baseline-1.8.1.jar'
allowed=['entity/LandSpiritEntity','entity/OvinnikEntity','hunt/HuntMob','hunt/WorldBoss','hunt/EmberClump',*('client/'+c for c in ['ClientSetup','HuntModel','HuntRenderer','WorldBossModel','WorldBossRenderer']),*('mor/'+c for c in ['MorController','MorRules','MorAudio','MorEvents','MorContent'])]
allowed_resources={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'}
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
 check('version="1.8.2"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'NeoForge version metadata')
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
 assets=list((R/'src/main/resources/assets/slavicmyths/textures/entity/mor').glob('*.png'))+[R/'src/main/resources/assets/slavicmyths/lang'/ (l+'.json') for l in ['ru_ru','en_us']]
 before={p:p.read_bytes() for p in assets}
 for generator in ['mor_assets.py','higher_mor_assets.py']:
  subprocess.run([sys.executable,str(R/'tools'/generator)],cwd=R,check=True)
  for p,data in before.items():check(p.read_bytes()==data,'generator drift '+generator+' '+str(p))
result=dict(version='1.8.2',status='PASS',checks=checks,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),size=jar.stat().st_size,offline_assertions=int(match[1]),native_profiles=int(match[2]),ui_checks=71,event_subscribers=subscribers(jar),old_entries_identical=len(oldnames)-len(changed),changed_old_entries=sorted(changed),new_entries=sorted(names-oldnames),client_starts=0,server_starts=0,gameplay_visual_audio_multiplayer_rejoin_mspt='NOT_RUN')
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print('HIGHER RESOURCE/PRODUCTION PASS checks='+str(checks)+' sha256='+result['sha256'])
