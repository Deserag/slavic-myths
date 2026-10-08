"""Focused 1.2.3 resource and production validation. Does not launch a client."""
from pathlib import Path
import argparse, json, hashlib, zipfile, re
from PIL import Image
import military_resources
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';checks=0
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);args=p.parse_args()
def check(ok,reason):
 global checks
 checks+=1
 if not ok:raise AssertionError(reason)
def read(rel):return json.loads((RES/rel).read_text(encoding='utf-8'))
manifest=(ROOT/'work/spec-1.2.3/MANIFEST.txt').read_text(encoding='utf-8')
for name,digest in re.findall(r'(\S+\.png)\s+SHA256=([a-f0-9]{64})',manifest):check(hashlib.sha256((ROOT/'work/spec-1.2.3/references'/name).read_bytes()).hexdigest()==digest,'Approved reference '+name)
files=list(RES.rglob('*.json'))
for f in files:json.loads(f.read_text(encoding='utf-8'));checks+=1
textures=list((RES/'assets/slavicmyths/textures/block').glob('military_*.png'))+[RES/'assets/slavicmyths/textures/entity/guard_equipment.png',RES/'assets/slavicmyths/textures/item/bandit_token.png']+list((RES/'assets/slavicmyths/textures/entity/villager/profession').glob('druzhinnik_*.png'))
for f in textures:check(min(Image.open(f).size)>=128,'Texture resolution '+str(f))
check(Image.open(RES/'assets/slavicmyths/textures/gui/military.png').size==(512,512),'512 GUI atlas')
model=read('assets/slavicmyths/models/block/druzhinnik_table.json')
for e in model['elements']:check(all(a<b for a,b in zip(e['from'],e['to'])),'Nondegenerate table geometry')
recipe=read('data/slavicmyths/recipe/druzhinnik_table.json');check(recipe['pattern']==['BIL','PPP','PPP'],'Approved recipe')
quests=read('data/slavicmyths/military/quests.json');check(len(quests)==6,'Six templates');check({q['type'] for q in quests}=={'kill','trophy','camp','defend','boss'},'Real quest types')
for q in quests:check(q['rarity']>=1 and q['refresh']=='daily' and q['eligibility'],'Offer context rules')
for lang in ['ru_ru','en_us']:
 data=read('assets/slavicmyths/lang/'+lang+'.json')
 for q in quests:
  for prefix in ['quest.','description.']:check('military.slavicmyths.'+prefix+q['id'] in data,'Translation '+lang+q['id'])
 check('block.slavicmyths.druzhinnik_table' in data and 'item.slavicmyths.bandit_token' in data,'Registry names')
for biome in ['plains','taiga','snowy','savanna','desert']:
 rel='data/minecraft/worldgen/template_pool/village/'+biome+'/houses.json';pool=read(rel)
 added=[e for e in pool['elements'] if e['element'].get('location')=='slavicmyths:military/barracks'];check(len(added)==1 and added[0]['weight']==1,'Rare single barracks entry '+biome)
 with zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar') as z:vanilla=json.loads(z.read(rel))
 check([e for e in pool['elements'] if e not in added]==vanilla['elements'],'Native village entries preserved '+biome)
for file in (ROOT/'src/main/java/org/slavicmyths/military').glob('*.java'):check('net.minecraft.client.' not in file.read_text(),'Common/client isolation '+file.name)
hostiles=read('data/slavicmyths/tags/entity_type/village_hostiles.json')['values'];check('slavicmyths:leshy' not in hostiles and 'minecraft:creeper' not in hostiles,'Calm spirit / creeper exclusions')
for rel in ['barracks_supply','bandit_small','bandit_medium','stronghold_cache','stronghold_warehouse','stronghold_barracks','stronghold_forge','stronghold_kitchen']:
 loot=read('data/slavicmyths/loot_table/chests/'+rel+'.json')
 for pool in loot['pools']:
  for entry in pool['entries']:
   if entry.get('name')=='minecraft:diamond':
    functions=entry.get('functions',[])+pool.get('functions',[])
    check(not any(f.get('function')=='minecraft:set_count' for f in functions) or any(f.get('function')=='minecraft:set_count' and f.get('count')==1 for f in functions),'Single diamond '+rel)
    conditions=entry.get('conditions',[])+pool.get('conditions',[])
    check(any(c.get('condition')=='minecraft:random_chance' and c['chance']<=.004 for c in conditions),'Rare diamond '+rel)
before={f:hashlib.sha256(f.read_bytes()).hexdigest() for f in RES.rglob('*') if f.is_file()};military_resources.main();after={f:hashlib.sha256(f.read_bytes()).hexdigest() for f in RES.rglob('*') if f.is_file()};check(before==after,'Military generator byte reproducibility')
if args.jar:
 with zipfile.ZipFile(args.jar) as z:
  names=set(z.namelist());check('Implementation-Version: 1.2.3' in z.read('META-INF/MANIFEST.MF').decode(),'Production version')
  check(not any('/verify/' in n or '/smoke/' in n or 'GameTests' in n for n in names),'No test harness in production')
  for f in RES.rglob('*'):
   if f.is_file():
    rel=f.relative_to(RES).as_posix();check(rel in names,'Bundled '+rel)
    if rel!='META-INF/neoforge.mods.toml':check(z.read(rel)==f.read_bytes(),'Exact resource '+rel)
  for c in ['Military','GuardBehavior','MilitaryQuests','MilitaryBoards','BanditRaids','MilitaryNetwork']:check('org/slavicmyths/military/'+c+'.class' in names,'Production class '+c)
report={'version':'1.2.3','checks':checks,'json_files':len(files),'new_textures':len(textures),'client_launches':0,'jar':str(args.jar) if args.jar else None}
(ROOT/'docs/verification/military-1.2.3-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
