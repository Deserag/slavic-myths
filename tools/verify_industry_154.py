"""Production and JSON probability gates. Does not launch or bootstrap Minecraft."""
from pathlib import Path
from PIL import Image
import hashlib,json,random,math,zipfile,re
ROOT=Path(__file__).resolve().parent.parent;RES=ROOT/'src/main/resources';OUT=ROOT/'docs/verification/industry-1.5.4';OUT.mkdir(parents=True,exist_ok=True)
jar=ROOT/'build/libs/slavicmyths-1.5.4.jar';baseline=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.5.3.1.jar')
if not baseline.exists():baseline=Path(json.loads((OUT/'polymc-installation.json').read_text(encoding='utf-8'))['previous_jars'][0]['backup'])
checks=0
def check(ok,label):
 global checks
 checks+=1
 assert ok,label
names=['bog_iron_ore','bog_iron_lump','chud_bloomery','refining_hearth','purification_flux','purified_iron_ingot','purified_copper_ingot','miners_goggles','prospectors_ring','stone_path_amulet','underground_guard_belt','mining_rune_1','mining_rune_2','fortune_rune_1']
with zipfile.ZipFile(jar) as new,zipfile.ZipFile(baseline) as old,zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar') as vanilla:
 archive=set(new.namelist())
 check('version="1.5.4"' in new.read('META-INF/neoforge.mods.toml').decode(),'version')
 classes=list((ROOT/'build/classes/java/main').rglob('*.class'))
 for p in classes:
  key=p.relative_to(ROOT/'build/classes/java/main').as_posix();check(new.read(key)==p.read_bytes(),key);check(int.from_bytes(new.read(key)[6:8],'big')==65,'Java21 '+key)
 check(not any('OfflineChecks' in n or '/verify/' in n or '/smoke/' in n for n in archive),'no test classes')
 preserved=[]
 for n in old.namelist():
  if n.endswith('.class') and (n.startswith('org/slavicmyths/depths/') or n.startswith('org/slavicmyths/chud/') and n.rsplit('/',1)[-1] not in ('ChudPiece.class','AbandonedPiece.class')):
   check(new.read(n)==old.read(n),'unchanged '+n);preserved.append(n)
  if n.startswith('data/slavicmyths/') and ('chud_' in n or n.endswith('/silver_ore.json')):check(new.read(n)==old.read(n),'saved resource '+n)
 check(new.read('org/slavicmyths/item/SilverCombat.class')==old.read('org/slavicmyths/item/SilverCombat.class'),'existing silver bonus unchanged')
 for p in RES.rglob('*'):
  if not p.is_file() or p.suffix not in ('.json','.png','.ogg','.nbt'):continue
  key=p.relative_to(RES).as_posix();check(new.read(key)==p.read_bytes(),'resource '+key)
  if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
  if p.suffix=='.png':
   with Image.open(p) as im:im.verify()
 for n in names:
  model=RES/'assets/slavicmyths/models/item'/f'{n}.json';check(model.exists(),'item model '+n)
  sprite=RES/'assets/slavicmyths/textures/item'/f'{n}.png'
  if sprite.exists():
   with Image.open(sprite) as im:check(min(im.size)>=32,'authored32 '+n)
 for kind in ('chud_bloomery','refining_hearth'):
  state=json.loads((RES/'assets/slavicmyths/blockstates'/f'{kind}.json').read_text());check(len(state['variants'])==8,'four facings and lit '+kind)
  for suffix in ('','_lit'):
   model=json.loads((RES/'assets/slavicmyths/models/block'/f'{kind}{suffix}.json').read_text())
   for cube in model['elements']:check(all(0<=a<b<=16 for a,b in zip(cube['from'],cube['to'])),'one block model bounds')
 for p in (RES/'assets/slavicmyths/models').rglob('*.json'):
  data=json.loads(p.read_text(encoding='utf-8'))
  for texture in data.get('textures',{}).values():
   if texture.startswith('#'):continue
   domain,texture=texture.split(':',1) if ':' in texture else ('minecraft',texture)
   if domain=='slavicmyths':check((RES/'assets/slavicmyths/textures'/f'{texture}.png').is_file(),'texture '+texture)
   elif domain=='minecraft':check('assets/minecraft/textures/'+texture+'.png' in vanilla.namelist(),'vanilla texture '+texture)
 for language in ('ru_ru','en_us'):
  before=json.loads(old.read(f'assets/slavicmyths/lang/{language}.json'));after=json.loads(new.read(f'assets/slavicmyths/lang/{language}.json'))
  check(all(after.get(k)==v for k,v in before.items()),'existing translations '+language)
  for name in names:check(bool(after.get(('block' if name in ('bog_iron_ore','chud_bloomery','refining_hearth') else 'item')+'.slavicmyths.'+name)),language+' '+name)
 check('slavicmyths.industry.mixins.json' in archive,'mixin production registration')
 check(not any('industry' in n and 'structure_set' in n for n in archive),'no new structure grid')

# Recipe economy and actual JSON independent pools; statistical simulation is not the Minecraft loot engine.
recipes=[json.loads(p.read_text(encoding='utf-8')) for p in (RES/'data/slavicmyths/recipe').rglob('*.json')]
processing=[r for r in recipes if r.get('type') in ('slavicmyths:chud_bloomery','slavicmyths:refining_hearth')]
check(len(processing)==3,'exact commissioned processing recipes')
for r in processing:check(r['result']['count']==1 and r['duration'] in (200,300),'one-to-one time')
check(not any(r.get('type') in ('minecraft:smelting','minecraft:blasting') and 'bog_iron_lump' in json.dumps(r) for r in recipes),'no vanilla lump smelting')
for s,name in [('head','miners_goggles'),('ring','prospectors_ring'),('necklace','stone_path_amulet'),('belt','underground_guard_belt')]:check('slavicmyths:'+name in json.loads((RES/f'data/curios/tags/item/{s}.json').read_text())['values'],'correct slot '+s)
def number(value,r):return r.randint(int(value['min']),int(value['max'])) if isinstance(value,dict) else int(value)
values={'diamond':50,'netherite_scrap':150,'netherite_ingot':600,'gold_ingot':8,'silver_ingot':8,'purified_iron_ingot':12,'purified_copper_ingot':10,'iron_ingot':4,'copper_ingot':2,'ancient_coin':5,'mining_rune_1':60,'mining_rune_2':140,'fortune_rune_1':200,'miners_goggles':600}
stats={}
for name in ('chud_residential','chud_workshop','chud_storehouse','abandoned_living','abandoned_mine','abandoned_treasury'):
 data=json.loads((RES/f'data/slavicmyths/loot_table/chests/{name}.json').read_text());check(data['type']=='minecraft:chest','chest context')
 r=random.Random(name);hits=[0]*len(data['pools']);wealth=[]
 for roll in range(1000):
  value=0;stacks=0
  for i,pool in enumerate(data['pools']):
   chance=pool.get('conditions',[{'chance':1}])[0]['chance'];check(0<chance<=1,'chance')
   if r.random()>=chance:continue
   hits[i]+=1
   for _ in range(number(pool['rolls'],r)):
    e=r.choices(pool['entries'],weights=[e.get('weight',1) for e in pool['entries']])[0];count=1
    for f in e.get('functions',[]):
     if f['function']=='minecraft:set_count':count=number(f['count'],r)
    check(0<count<=64,'valid count');value+=count*values.get(e['name'].split(':')[1],1);stacks+=1
  check(stacks<=27,'27-slot bound');wealth.append(value)
 for i,pool in enumerate(data['pools']):
  if 'conditions' not in pool:continue
  p=pool['conditions'][0]['chance'];check(abs(hits[i]-1000*p)<=max(5,5*math.sqrt(1000*p*(1-p))),'independent chance distribution '+name)
 stats[name]={'samples':1000,'mean_value_proxy':sum(wealth)/1000,'pool_hits':hits,'kind':'JSON probability simulation; not actual engine loot'}
 if name.startswith('chud_'):check(not any(x in json.dumps(data) for x in ('netherite','mining_rune_2','fortune_rune_1','miners_goggles')),'city excludes expensive rewards')
check(stats['abandoned_treasury']['mean_value_proxy']>stats['abandoned_mine']['mean_value_proxy']>stats['chud_residential']['mean_value_proxy'],'loot ordering')
(OUT/'loot-simulation.json').write_text(json.dumps(stats,indent=2)+'\n',encoding='utf-8')
digest=hashlib.sha256(jar.read_bytes()).hexdigest()
result=dict(checks=checks,version='1.5.4',jar=str(jar.resolve()),sha256=digest,resources='PASS',production='PASS',production_classes=len(classes),preserved_geometry_and_depths_classes=len(preserved),minecraft_launches=0,runtime_validation='NOT_RUN')
(OUT/'resource-gates.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
offline=dict(build='PASS',offline_checks='PASS',production_sha256=digest,runtime_validation='NOT_RUN',minecraft_launches=0,rule_assertions=5443,loot_json_samples=6000,limitation='No native loot simulation, GUI/AI/hopper/relog/gameplay/MSPT validation')
(OUT/'offline-gates.json').write_text(json.dumps(offline,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result))
