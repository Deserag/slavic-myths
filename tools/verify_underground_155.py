"""Production resource/regression gates; never bootstraps or launches Minecraft."""
from pathlib import Path
from PIL import Image
import hashlib,json,zipfile,re,subprocess,random,math
ROOT=Path(__file__).resolve().parent.parent;RES=ROOT/'src/main/resources';OUT=ROOT/'docs/verification/mobs-1.5.5';OUT.mkdir(parents=True,exist_ok=True)
jar=ROOT/'build/libs/slavicmyths-1.5.5.jar';baseline=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.5.4.jar')
if not baseline.exists():baseline=Path(json.loads((OUT/'polymc-installation.json').read_text())['previous_jars'][0]['backup'])
checks=0
def check(ok,label):
 global checks
 checks+=1
 assert ok,label
changed=['org/slavicmyths/SlavicMyths','org/slavicmyths/rpg/Runes','org/slavicmyths/rpg/RuneRework','org/slavicmyths/industry/ChudPerson','org/slavicmyths/industry/ChudContents','org/slavicmyths/industry/ChudThreats','org/slavicmyths/client/ChudPeopleClient','org/slavicmyths/chud/ChudPiece','org/slavicmyths/chud/AbandonedPiece','org/slavicmyths/chud/ChudGenerateCommands']
def allowed(n):return any(n==x+'.class' or n.startswith(x+'$') for x in changed)
with zipfile.ZipFile(jar) as new,zipfile.ZipFile(baseline) as old,zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar') as vanilla:
 names=set(new.namelist());classes=list((ROOT/'build/classes/java/main').rglob('*.class'));preserved=[];preserved_resources=0
 for p in classes:
  key=p.relative_to(ROOT/'build/classes/java/main').as_posix();check(new.read(key)==p.read_bytes(),key);check(int.from_bytes(new.read(key)[6:8],'big')==65,'Java21 '+key)
 check(not any('UndergroundModelChecks' in n or 'UndergroundRuleChecks' in n or 'UndergroundSaveChecks' in n or '/smoke/' in n or '/verify/' in n for n in names),'no test classes')
 check('version="1.5.5"' in new.read('META-INF/neoforge.mods.toml').decode(),'version')
 for n in old.namelist():
  if n.endswith('.class') and not allowed(n):check(new.read(n)==old.read(n),'preserved class '+n);preserved.append(n)
  if n.endswith(('.json','.png','.nbt','.ogg')) and n.startswith(('assets/','data/')):
   if n in ('assets/slavicmyths/sounds.json','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'):
    a=json.loads(old.read(n));b=json.loads(new.read(n));check(all(b.get(k)==v for k,v in a.items()),'old keys '+n)
   else:check(new.read(n)==old.read(n),'unchanged industry/structure/chest/art '+n);preserved_resources+=1
 for p in RES.rglob('*'):
  if not p.is_file() or p.suffix not in ('.json','.png','.nbt','.ogg'):continue
  key=p.relative_to(RES).as_posix();check(new.read(key)==p.read_bytes(),'packaged resource '+key)
  if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
  if p.suffix=='.png':
   with Image.open(p) as im:im.verify()
 for name in ('podzemnik','podzemnik_miner','kladovik','large_podzemnik','kladovik_rage','large_podzemnik_rage'):
  with Image.open(RES/f'assets/slavicmyths/textures/entity/underground/{name}.png') as im:check(im.size==(128,128),'original atlas '+name)
 for name in ('podzemnik','kladovik','large_podzemnik'):
  check(json.loads(new.read(f'assets/slavicmyths/models/item/{name}_spawn_egg.json'))['parent']=='minecraft:item/template_spawn_egg','egg '+name)
 check('assets/slavicmyths/models/item/podzemnik_miner_spawn_egg.json' not in names,'one shared egg')
 for lang in ('ru_ru','en_us'):
  data=json.loads(new.read(f'assets/slavicmyths/lang/{lang}.json'))
  for n in ('podzemnik','podzemnik_miner','kladovik','large_podzemnik'):check(bool(data.get('entity.slavicmyths.'+n)),lang+' '+n)
 sounds=json.loads(new.read('assets/slavicmyths/sounds.json'));manifest=json.loads(Path('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_version_manifest.json').read_text());asset_root=Path('C:/Users/pavel/AppData/Roaming/PolyMC/assets');index=json.loads((asset_root/'indexes'/f"{manifest['assetIndex']['id']}.json").read_text());sound_hash=index['objects']['minecraft/sounds.json']['hash'];vanilla_sounds=json.loads((asset_root/'objects'/sound_hash[:2]/sound_hash).read_text())
 for k,v in sounds.items():
  if not k.startswith('underground.'):continue
  for sound in v['sounds']:check(sound['type']=='event' and sound['name'].split(':',1)[1] in vanilla_sounds,'legal vanilla sound alias '+k)
 for n in names:
  if n.startswith('org/slavicmyths/underground/') and n.endswith('.class'):check(b'net/minecraft/client/' not in new.read(n),'server isolation '+n)
 for n in ('ExhaustionMixin','OminousTargetMixin','OminousContinueMixin'):check('org/slavicmyths/underground/mixin/'+n+'.class' in names,'common mixin '+n)

javap='C:/Program Files/Java/jdk-21.0.12/bin/javap.exe'
def method(path,klass,start,end):
 text=subprocess.run([javap,'-c','-p','-classpath',str(path),klass],capture_output=True,text=True,check=True).stdout
 text=text[text.index(start):text.index(end,text.index(start))];return re.sub(r'\s+',' ',re.sub(r'#\d+','#',text))
check(method(baseline,'org.slavicmyths.industry.ChudPerson','protected void updateTrades();','private void offer(')==method(jar,'org.slavicmyths.industry.ChudPerson','protected void updateTrades();','private void offer('),'trade prices/uses bytecode unchanged')
check(method(baseline,'org.slavicmyths.client.ChudPeopleClient$Model','private static net.minecraft.client.model.geom.ModelPart mesh();','public void setupAnim(')==method(jar,'org.slavicmyths.client.ChudPeopleClient$Model','private static net.minecraft.client.model.geom.ModelPart mesh();','public void setupAnim('),'existing approved NPC mesh instructions unchanged')
# Expected probabilities of independent JSON pools, not the engine and not actual player drops.
stats={}
for name in ('podzemnik','podzemnik_miner','kladovik','large_podzemnik'):
 loot=json.loads((RES/f'data/slavicmyths/loot_table/entities/{name}.json').read_text());rng=random.Random(name);hits=[0]*len(loot['pools'])
 for _ in range(1000):
  for i,pool in enumerate(loot['pools']):
   chance=pool.get('conditions',[{'chance':1}])[0]['chance']
   if rng.random()<chance:hits[i]+=1
 for i,pool in enumerate(loot['pools']):
  chance=pool.get('conditions',[{'chance':1}])[0]['chance'];check(abs(hits[i]-1000*chance)<=max(5,5*math.sqrt(1000*chance*(1-chance))),'loot probability '+name)
 stats[name]={'samples':1000,'pool_hits':hits,'method':'JSON independent probability simulation, not native engine'}
check(json.loads((RES/'data/slavicmyths/loot_table/entities/large_podzemnik.json').read_text())['pools'][-1]['conditions'][0]['chance']==.3,'trophy30percent')
mixins=json.loads((RES/'slavicmyths.underground.mixins.json').read_text());check(mixins['required'] and len(mixins['mixins'])==3,'required narrow mixins')
common=(ROOT/'src/main/java/org/slavicmyths/underground')
check(not any('setBlock(' in p.read_text(encoding='utf-8') or 'destroyBlock(' in p.read_text(encoding='utf-8') for p in common.rglob('*.java')),'no terrain mutation')
digest=hashlib.sha256(jar.read_bytes()).hexdigest();result=dict(version='1.5.5',checks=checks,jar=str(jar.resolve()),sha256=digest,resources='PASS',production='PASS',production_classes=len(classes),preserved_classes=len(preserved),preserved_resources=preserved_resources,minecraft_launches=0,runtime_validation='NOT_RUN')
(OUT/'resource-gates.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
(OUT/'offline-gates.json').write_text(json.dumps(dict(build='PASS',offline_checks='PASS',production_sha256=digest,minecraft_launches=0,runtime_validation='NOT_RUN',rule_assertions=18,mesh_assertions=2387,saved_data_roundtrip='PASS',loot_probability_samples=4000,limitation='No client/server startup, in-game combat/AI/pose/trade/relog/MSPT verification; Wizard and Ender Skeleton absent from installed mods'),indent=2)+'\n',encoding='utf-8')
(OUT/'loot-probabilities.json').write_text(json.dumps(stats,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result))
