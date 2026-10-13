"""Production/resource gates for the concrete 1.7.7 feedback changes."""
from pathlib import Path
import json,hashlib,zipfile,struct,subprocess,sys
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/feedback-1.7.7';O.mkdir(parents=True,exist_ok=True)
RES=R/'src/main/resources';checks=0
def check(value,label):
 global checks
 checks+=1
 if not value:raise AssertionError(label)
for p in RES.rglob('*.json'):
 data=json.loads(p.read_text(encoding='utf8'));check(isinstance(data,(dict,list)),str(p))
 if '/models/' in p.as_posix():
  for value in data.get('textures',{}).values():
   if value.startswith('slavicmyths:'):check((RES/'assets/slavicmyths/textures'/(value.split(':')[1]+'.png')).exists(),'Missing texture '+value)
sounds=json.loads((RES/'assets/slavicmyths/sounds.json').read_text(encoding='utf8'))
for event,data in sounds.items():
 for sound in data['sounds']:
  name=sound if isinstance(sound,str) else sound['name']
  if name.startswith('slavicmyths:') and (isinstance(sound,str) or sound.get('type','file')=='file'):
   check((RES/'assets/slavicmyths/sounds'/(name.split(':')[1]+'.ogg')).exists(),'Missing sound '+name)
for p in (RES/'assets/slavicmyths/textures').rglob('*.png'):
 with Image.open(p) as img:img.verify();check(True,'PNG '+str(p))
check(json.loads((RES/'data/slavicmyths/tags/entity_type/reward_interruptible.json').read_text())['values']==['slavicmyths:kartaus'],'Real Kartaus registry ID')
jar=R/'build/libs/slavicmyths-1.7.7.jar';baseline=R/'release/slavicmyths-1.7.6.jar'
owned=['bogatyr/HeroThreats','bogatyr/BogatyrLedger','bogatyr/IlyaEntity','bogatyr/IdolishcheEntity','marinka/DobrynyaEntity','marinka/MarinkaEntity','skim/AlyoshaEntity','skim/SkimBeastEntity','finist/FinistEntity','finist/KartausEntity','kalin/KalinEntity','client/HeroHud','client/ClassClient','client/BogatyrClient','client/MarinkaClient','client/SkimClient','client/BrightFalconClient']
meshes=['Ilya','Idolishche','Dobrynya','Marinka','Alyosha','Skim','Finist','Kartaus','Kalin','WarHorse','CommonSoldier','EliteSoldier']
owned+=['client/'+m+'Mesh' for m in meshes]
textures=['ilya_muromets','idolishche_poganoe','dobrynya_nikitich','marinka','alyosha_popovich','skim_beast','finist','kartaus','kalin_tsar','kalin_war_horse','kalin_common_soldier','kalin_elite_soldier']
allowed={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','data/slavicmyths/tags/entity_type/reward_interruptible.json'}|{'assets/slavicmyths/textures/entity/'+x+'.png' for x in textures}
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(baseline) as old:
 names=set(z.namelist());prior=set(old.namelist());check(prior<=names,'No prior entries removed')
 changed=[n for n in prior if old.read(n)!=z.read(n)]
 for n in changed:
  check(n in allowed or any(n=='org/slavicmyths/'+c+'.class' or n.startswith('org/slavicmyths/'+c+'$') for c in owned),'Unrelated changed entry '+n)
 for n in names:
  check(not any(v in n.lower() for v in ['gametest','org/slavicmyths/verify/','feedbackruntime','tools/smoke']),'Test leakage '+n)
  if n.endswith('.class'):
   check(struct.unpack('>H',z.read(n)[6:8])[0]==65,'Java21 '+n)
   source=R/'build/classes/java/main'/n
   if source.exists():check(source.read_bytes()==z.read(n),'Stale class '+n)
  source=RES/n
  if source.is_file() and n!='META-INF/neoforge.mods.toml':check(source.read_bytes()==z.read(n),'Stale resource '+n)
 check('version="1.7.7"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'Production version')
 # Source-derived mesh material/face generation must repeat without resource drift.
 before={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in RES.rglob('*') if p.is_file()}
 before.update({R/'src/main/java/org/slavicmyths/client'/f'{m}Mesh.java':hashlib.sha256((R/'src/main/java/org/slavicmyths/client'/f'{m}Mesh.java').read_bytes()).hexdigest() for m in meshes})
 subprocess.run([sys.executable,str(R/'tools/feedback_assets.py')],cwd=R,check=True)
 for p,digest in before.items():check(hashlib.sha256(p.read_bytes()).hexdigest()==digest,'Generator drift '+str(p))
result={'status':'PASS','checks':checks,'production_jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'prior_entries_identical':len(prior)-len(changed),'changed_entries':sorted(changed),'generator_drift':0,'client_launches':0,'client_visual_gameplay_acceptance':'NOT_RUN'}
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('FEEDBACK PRODUCTION/RESOURCE PASS checks='+str(checks)+' identical='+str(result['prior_entries_identical']))
