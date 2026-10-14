"""Production integration gate: exact source/compiled bytes and actual run evidence."""
from pathlib import Path
import hashlib,json,zipfile,subprocess,re,sys
R=Path(__file__).resolve().parents[1];O=R/'docs/integration-197';O.mkdir(parents=True,exist_ok=True)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def git(*args):return subprocess.check_output(['git','-C',str(R),*args],text=True).strip()
J=R/'build/libs/slavicmyths-1.9.7.jar'
baseline=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.9.7.jar')
assert sha(baseline)=='2108e49ad917af013792b21c43a20ade131bd8d606d40d5ae57eca229b0ab180','installed baseline changed again'
checks=0
with zipfile.ZipFile(J) as z,zipfile.ZipFile(baseline) as old:
 assert z.testzip() is None
 names=set(z.namelist());assert 'META-INF/neoforge.mods.toml' in names
 assert 'version="1.9.7"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
 assert not any(any(s in n for s in ['ScytheGameTests','ScytheClientProbe','CreatureFeedbackTests','CreatureHealthChecks']) or n.startswith(('tools/','data/slavicmyths_scythe/','data/slavicmyths_creature_feedback/')) for n in names)
 for path in (R/'src/main/resources').rglob('*'):
  if not path.is_file():continue
  name=path.relative_to(R/'src/main/resources').as_posix();assert name in names,name
  if name.startswith(('assets/','data/')):assert z.read(name)==path.read_bytes(),name
  if path.suffix=='.json':json.loads(z.read(name))
  checks+=1
 for name in names:
  if name.endswith('.class'):
   blob=z.read(name);assert int.from_bytes(blob[6:8],'big')==65,name
   assert blob==(R/'build/classes/java/main'/name).read_bytes(),name;checks+=1
 for name in ['org/slavicmyths/rpg/AdvancedRunes.class','org/slavicmyths/combat/CreatureHealthBalance.class','org/slavicmyths/combat/CreatureSafety.class','org/slavicmyths/equipment/mixin/ScythePlayerPoseMixin.class','assets/slavicmyths/models/item/karachun_scythe.obj','assets/minecraft/atlases/blocks.json']:assert name in names,name
 added=sorted(names-set(old.namelist()));removed=sorted(set(old.namelist())-names)
 changed=sorted(n for n in names&set(old.namelist()) if z.read(n)!=old.read(n) and z.read(n).replace(b'\r\n',b'\n')!=old.read(n).replace(b'\r\n',b'\n'))
 assert not any(n.startswith(('assets/slavicmyths/','data/slavicmyths/')) for n in removed if not n.endswith('/')),removed
from scythe_stage_a_assets import main as generate,MATERIALS
from verify_scythe_stage_a import validate_geometry
paths=[R/'src/main/resources/assets/slavicmyths/models/item'/n for n in ['karachun_scythe.obj','karachun_scythe.mtl','karachun_scythe_held.json']]+[R/'src/main/resources/assets/slavicmyths/textures/item'/('karachun_scythe_'+n+'.png') for n in MATERIALS]+[R/'src/main/resources/assets/slavicmyths/textures/item/karachun_scythe.png']
before=[sha(p) for p in paths];generate();assert before==[sha(p) for p in paths]
geometry=validate_geometry()
logs={name:(R/'.tools'/name).read_text(encoding='utf-8-sig',errors='replace') for name in ['clean-build.log','integrated-gametest.log','integrated-creature-gametest.log','integrated-native-client.log']}
for name,log in logs.items():assert 'BUILD SUCCESSFUL' in log,name
assert 'SCYTHE_NATIVE_CLIENT_COMPLETE=26' in logs['integrated-native-client.log']
assert 'Missing textures in model slavicmyths' not in logs['integrated-native-client.log']
assert not re.search(r'\[.*?/ERROR\]',logs['integrated-native-client.log'])
evidence=json.loads((R/'docs/weapon-stage-a/server-evidence.json').read_text());assert evidence['checks']==37 if isinstance(evidence.get('checks'),int) else len(evidence.get('checks',[]))==37
components={key:git('rev-parse',ref) for key,ref in {'weapons_base':'f53447cc','armor':'b7616080','scythe':'34a827b3','runes':'3dbc28e3','creature_health_stage1':'50622af9','creature_feedback':'6bd98360'}.items()}
result={'status':'PASS','version':'1.9.7','source_commit':git('rev-parse','HEAD'),'sha256':sha(J),'baseline_sha256':sha(baseline),'checks':checks,'components':components,'scythe_geometry':geometry,'scythe_generator':'byte reproducible','client_scenes':26,'jar_added':added,'jar_removed':removed,'jar_changed':changed,'unused_armor_layer2':'gambeson and plate both CHESTPLATE; not needed, no placeholders','independent_acceptance':'PENDING_AUTHOR_AND_CHATGPT'}
(O/'gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({k:v for k,v in result.items() if k not in ['jar_added','jar_removed','jar_changed']},ensure_ascii=False))
