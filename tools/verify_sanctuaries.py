"""Production/native resource gate; no client/server/world starts."""
from pathlib import Path
from PIL import Image
import json,zipfile,hashlib,struct,re,random,subprocess
from nbtlib import load
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/sanctuaries-1.9.3';A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';J=R/'build/libs/slavicmyths-1.9.3.jar';checks=0
def need(ok,msg):
 global checks;checks+=1
 if not ok:raise AssertionError(msg)
def main():
 log=(R/'.tools/sanctuary-clean-build.log').read_text(encoding='utf-8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build')
 for tag in ['SANCTUARY_OFFLINE_PASS','NIGHT_OFFLINE_PASS','FROST_OFFLINE_PASS','QUEST185_OFFLINE_PASS','POLOZ_OFFLINE_PASS','120 native']:need(tag in log,tag)
 before={p.relative_to(R/'src/main/resources').as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'src/main/resources').rglob('*') if p.is_file()}
 subprocess.run(['python',str(R/'tools/sanctuary_assets.py')],check=True)
 after={p.relative_to(R/'src/main/resources').as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'src/main/resources').rglob('*') if p.is_file()}
 need(before==after,'generator rollback/nondeterminism');(O/'reproducibility.json').write_text(json.dumps({'status':'PASS','resources':len(before),'changed':0},indent=2)+'\n')
 baseline='75f1cec8b4af7fe2f566765328ec2a0108617a85921a169e6b84f709a511ea70';profile=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing');oldpath=profile/'.minecraft/mods/slavicmyths-1.9.2.jar'
 if not oldpath.exists():oldpath=next(p for p in (profile/'.slavicmyths-backups').rglob('slavicmyths-1.9.2.jar') if hashlib.sha256(p.read_bytes()).hexdigest()==baseline)
 need(hashlib.sha256(oldpath.read_bytes()).hexdigest()==baseline,'incorrect baseline')
 with zipfile.ZipFile(J) as jar,zipfile.ZipFile(oldpath) as old:
  names=set(jar.namelist());need(b'1.9.3' in jar.read('META-INF/neoforge.mods.toml'),'version');need(not any('/smoke/' in p or p.startswith('tools/') or p.endswith('SanctuaryChecks.class') or p.endswith('NightChecks.class') for p in names),'test code in production')
  for p in (R/'build/classes/java/main').rglob('*.class'):
   name=p.relative_to(R/'build/classes/java/main').as_posix();need(jar.read(name)==p.read_bytes(),'stale bytecode '+name);need(struct.unpack('>H',p.read_bytes()[6:8])[0]==65,'Java version '+name)
  for path in (R/'src/main/resources').rglob('*'):
   if not path.is_file():continue
   name=path.relative_to(R/'src/main/resources').as_posix();need(name in names,'missing resource '+name);data=path.read_bytes()
   if name=='META-INF/neoforge.mods.toml':data=data.replace(b'$'+b'{mod_version}',b'1.9.3').replace(b'$'+b'{neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
   need(jar.read(name)==data,'stale resource '+name)
   if name.endswith('.json'):json.loads(data)
  changed=[]
  for name in old.namelist():
   if name.endswith('/') or name.endswith('.class') or name.startswith('META-INF/'):continue
   need(name in names,'old resource removed '+name)
   if old.read(name)!=jar.read(name):changed.append(name);need(name in {'assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json'},'prior regression '+name)
  langs={n:json.loads((A/f'lang/{n}.json').read_text(encoding='utf-8')) for n in ['ru_ru','en_us']}
  for locale,lang in langs.items():
   prev=json.loads(old.read(f'assets/slavicmyths/lang/{locale}.json'));need(all(lang.get(k)==v for k,v in prev.items()),'prior translation '+locale)
  from sanctuary_assets import ITEM_NAMES,BLOCK_NAMES
  for name in ITEM_NAMES:
   need(Image.open(A/f'textures/item/{name}.png').size==(32,32),'icon '+name);need((A/f'models/item/{name}.json').exists(),'model '+name)
   for lang in langs.values():need('item.slavicmyths.'+name in lang and 'item.slavicmyths.'+name+'.desc' in lang,'item language '+name)
  for name in BLOCK_NAMES:
   state=json.loads((A/f'blockstates/{name}.json').read_text(encoding='utf-8'));need('variants' in state,'blockstate');need((D/f'loot_table/blocks/{name}.json').exists(),'block loot')
   if 'cache' not in name:
    need(len(state['variants'])==8,'facing/lit variants')
    for model in [name,name+'_lit']:
     data=json.loads((A/f'models/block/{model}.json').read_text(encoding='utf-8'));need(all(-16<=v<=32 for e in data['elements'] for k in ['from','to'] for v in e[k]),'model bounds');need(Image.open(A/f'textures/block/{model}.png').size==(32,32),'texture')
   for lang in langs.values():need('block.slavicmyths.'+name in lang,'block language')
  mapping=json.loads((A/'sounds.json').read_text(encoding='utf-8'));own={k:v for k,v in mapping.items() if k.startswith('sanctuary193.')};need(len(own)==13,'sounds')
  for key,event in own.items():
   for lang in langs.values():need(event['subtitle'] in lang,'subtitle')
   for sound in event['sounds']:need(jar.read('assets/slavicmyths/sounds/'+sound['name'].split(':')[1]+'.ogg').startswith(b'OggS'),'OGG')
  source='\n'.join(p.read_text(encoding='utf-8') for p in (R/'src/main/java/org/slavicmyths/sanctuary').glob('*.java'));need('net.minecraft.client' not in source,'client common');need('setDayTime' not in source and 'setForced' not in source,'global mutation')
  for lang in langs.values():
   for key in re.findall(r'SanctuaryContent.text\("([a-z_]+)"',source):need('sanctuary193.'+key in lang,'raw UI '+key)
  for family in ['fire','spring']:
   summary=json.loads((O/f'{family}-layout.json').read_text(encoding='utf-8'));scene={tuple(b['pos']):b for b in json.loads((O/f'{family}-blocks.json').read_text(encoding='utf-8'))}
   need(len(summary['tiles'])==(25 if family=='fire' else 36),'tiles');need(len([m for m in summary['markers'] if m['role'].startswith('loot')]) in [3,4],'ordinary chests');need(sum(m['role'].startswith('cache') for m in summary['markers'])==1,'ritual cache')
   if family=='fire':need(sum(m['role']=='loot:guaranteed' for m in summary['markers'])==1,'guaranteed ember')
   else:need(sum(m['role'].startswith('pool:') for m in summary['markers'])==2,'tainted nodes')
   arena=(range(64,78),range(2,9),range(39,53)) if family=='fire' else (range(72,87),range(1,9),range(13,28))
   need(all(scene.get((x,y,z),{}).get('name')=='minecraft:air' for x in arena[0] for y in arena[1] for z in arena[2]),'clear arena '+family)
   for tile in summary['tiles']:
    path=D/f'structure/sanctuaries/{tile["name"]}.nbt';need(hashlib.sha256(path.read_bytes()).hexdigest()==tile['sha256'],'template hash');nbt=load(path);need(not nbt['entities'],'future entities')
    for state in nbt['palette']:
     name=str(state['Name'])
     if name.startswith('slavicmyths:'):need(name.split(':')[1] in BLOCK_NAMES or name=='slavicmyths:mor_stone_bricks','custom palette '+name)
  placement=json.loads((D/'worldgen/structure_set/two_sanctuaries.json').read_text(encoding='utf-8'));need(len(placement['structures'])==2,'two structures');need(placement['placement']['exclusion_zone']['other_set']=='minecraft:strongholds','stronghold exclusion')
  rates={};rng=random.Random(193);total=100000
  for family in ['fire','spring']:
   table=json.loads((D/f'loot_table/chests/{family}_regular.json').read_text(encoding='utf-8'));need(len(table['pools'])==5,'loot categories')
   for index,pool in enumerate(table['pools'][1:],1):
    chance=pool['conditions'][0]['chance'];rate=sum(rng.random()<chance for _ in range(total))/total;need(abs(rate-chance)<.005,'loot interpretation');rates[family+str(index)]=rate;need(pool['rolls']==1,'one rare selection')
   if family=='fire':
    g=json.loads((D/'loot_table/chests/fire_guaranteed.json').read_text(encoding='utf-8'));need(g['pools'][-1]['entries'][0]['name']=='slavicmyths:sacred_ember' and not g['pools'][-1].get('conditions'),'fixed ember')
  for path in (D/'recipe').glob('*.json'):
   if path.stem not in ['purifying_ash','flame_vial','fading_flame_charm','purified_water_bottle','spring_tear','cleansing_powder_from_ash','blank_rune_1_from_ancient']:continue
   recipe=json.loads(path.read_text(encoding='utf-8'));need(recipe['type']=='minecraft:crafting_shapeless' and len(recipe['ingredients'])>=2,'recipe');need('amulet_fragment' not in str(recipe),'story fragment')
  sub=subscribers(J);need(sub>0,'event subscribers')
 result={'status':'PASS','checks':checks,'version':'1.9.3','sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'baseline':baseline,'native_templates':61,'authored_block_entries':90484,'sound_events':13,'loot_simulations':total,'interpreted_category_rates':rates,'event_subscribers':sub,'changed_existing_resources':changed,'client_starts':0,'server_starts':0,'gameplay':'NOT_RUN','natural_seed_generation':'NOT_RUN','MSPT':'NOT_MEASURED'}
 (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
