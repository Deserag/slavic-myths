"""Static Household data/geometry/production checks; not gameplay tests."""
import argparse,json,hashlib,zipfile,subprocess
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths';J=ROOT/'src/main/java/org/slavicmyths';checks=0;links=0
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);args=p.parse_args()
def check(ok,s):
 global checks;checks+=1
 if not ok:raise AssertionError(s)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
blocks=['storage_sack','basket','large_basket','produce_crate','household_chest','wooden_barrel','wall_shelf','storage_shelf','drying_rack','haystack','rye_sheaf','barley_sheaf','oat_sheaf']
expected={'storage_sack':20,'basket':4,'large_basket':4,'produce_crate':4,'household_chest':8,'wooden_barrel':4,'wall_shelf':4,'storage_shelf':8,'drying_rack':16,'haystack':32,'rye_sheaf':4,'barley_sheaf':4,'oat_sheaf':4}
for name in blocks:
 check(len(read(A/f'blockstates/{name}.json')['variants'])==expected[name],'states '+name)
 check((A/f'models/item/{name}.json').is_file(),'item model '+name)
 loot=read(D/f'loot_table/blocks/{name}.json');check((not loot['pools'])==(name in ['storage_shelf','drying_rack','haystack']),'single owner '+name)
 for lang in ['ru_ru','en_us']:check('block.slavicmyths.'+name in read(A/f'lang/{lang}.json'),'language '+name)
for name in ['dried_berries','dried_mushrooms']:
 im=Image.open(A/f'textures/item/{name}.png');check(im.size==(16,16),'native sprite');check(set(im.getchannel('A').getdata())<={0,255},'no AA');check((A/f'models/item/{name}.json').exists(),'item asset')
source=(J/'storage/Household.java').read_text(encoding='utf-8');check('nutrition(3).saturationModifier(.35F)' in source,'dried food');check('register("dried_mushrooms",()->new Item(new Item.Properties()))' in source,'mushrooms not edible')
for name in ['berries','red_mushroom','brown_mushroom']:
 r=read(D/f'recipe/drying/{name}.json');check(r['type']=='slavicmyths:drying' and r['drying_time']==2400 and r['result']['count']==1,'exact drying '+name)
for grain in ['rye','barley','oat']:
 r=read(D/f'recipe/{grain}_sheaf.json');check(r['pattern']==['GGG']*3 and r['key']['G']['item']=='slavicmyths:'+grain+'_grain','nine grain');r=read(D/f'recipe/{grain}_sheaf_unpack.json');check(r['result']['count']==9 and r['ingredients']==[{'item':'slavicmyths:'+grain+'_sheaf'}],'reverse nine grain')
patterns={'storage_sack':['CCC','C C','CTC'],'basket':['S S','SPS','SSS'],'large_basket':['S S','SPS','PSP'],'produce_crate':['P P','S S','PPP'],'household_chest':['PPP','PIP','PPP'],'wooden_barrel':['PNP','P P','PNP'],'wall_shelf':['PPP','S S'],'storage_shelf':['SPS','PPP','SPS'],'drying_rack':['STS','S S','PPP']}
for n,pattern in patterns.items():r=read(D/f'recipe/{n}.json');check(r['pattern']==pattern,'exact craft '+n);check(r['result']['count']==(2 if n=='wall_shelf' else 1),'craft amount');check('P' not in r['key'] or r['key']['P']=={'tag':'minecraft:planks'},'any plank')
check(read(D/'recipe/haystack.json')['ingredients']==[{'item':'minecraft:hay_block'}],'one hay placement')
for tag in ['sack_items','basket_items','crate_items','barrel_items']:check((D/f'tags/item/storage/{tag}.json').is_file(),'storage tag')
check('#slavicmyths:berries' in read(D/'tags/item/storage/basket_items.json')['values'],'all berries');check('slavicmyths:flax_seeds' in read(D/'tags/item/storage/sack_items.json')['values'],'flax seed')
adv=read(D/'advancement/winter_stores.json');check('rewards' not in adv and not adv['display']['hidden'],'no XP visible advancement')
for p in (J/'storage').glob('*.java'):check('net.minecraft.client' not in p.read_text(encoding='utf-8'),'common isolated')
tile=(J/'storage/StorageTile.java').read_text(encoding='utf-8');check('kind==Kind.BARREL||items.stream()' not in tile,'barrel predicate remains explicit');check('allMatch(v->v.is(s.getItem()))' in tile,'barrel single type');check('time[i]==0' in tile and 'before!=t.stage(i)' in tile,'stage-based dry sync');check('ContainerHelper.saveAllItems(n,items,p)' in tile,'stable inventory format');check('Slot s=new Slot(tile' in tile and 'tile.canPlaceItem' in tile,'GUI validates barrel');check('hayCount=0' in tile and 'Items.HAY_BLOCK,count' in tile,'hay break exact count')
renderer=(J/'client/StorageDisplay.java').read_text(encoding='utf-8');check('t.items.get(i)' in renderer and 'for(int i=0;i<t.items.size();i++)' in renderer,'all actual shelf stacks');check('KitchenDisplay.draw' in renderer,'reuse real food meshes');check('new ItemEntity' not in renderer and 'renderStatic' in renderer,'no item entities')
block=(J/'storage/StorageBlock.java').read_text(encoding='utf-8');check('(kind==Kind.DRY||kind==Kind.BARREL&&s.getValue(BREWING))&&part(s)==0' in block,'dry master / active brewing barrel ticker');check('part(old)==part' in block,'cleanup exact partners');check('DoubleBlockHalf.LOWER' in block and 'DoubleBlockHalf.UPPER' in block,'vertical halves')
check('org.slavicmyths.storage.StorageBlock' in (J/'textile/BeltData.java').read_text(encoding='utf-8'),'sneak storage interaction has priority')
# Native links across all resources, plus new authored cuboids/UV.
vanilla=zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');vn=set(vanilla.namelist())
def ref(s,folder,suffix):
 global links
 if s.startswith('#'):return
 links+=1;ns,path=s.split(':',1) if ':' in s else ('minecraft',s);key=f'assets/{ns}/{folder}/{path}{suffix}';check((R/key).is_file() or key in vn or path.startswith('builtin/'),'link '+key)
def walk(v):
 if isinstance(v,dict):
  for k,x in v.items():
   if k in ['parent','model'] and isinstance(x,str):ref(x,'models','.json')
   elif k=='textures' and isinstance(x,dict):
    for t in x.values():ref(t,'textures','.png')
   elif isinstance(x,(dict,list)):walk(x)
 elif isinstance(v,list):
  for x in v:walk(x)
jsons=list(R.rglob('*.json'))
for p in jsons:
 v=read(p)
 if '/models/' in p.as_posix() or '/blockstates/' in p.as_posix():walk(v)
for folder in ['models/block','models/item']:
 for p in (A/folder).glob('*.json'):
  if not any(p.stem==n or p.stem.startswith(n+'_') for n in blocks):continue
  for e in read(p).get('elements',[]):
   check(all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),'cuboid '+p.name)
   for face in e['faces'].values():check(all(0<=u<=16 for u in face['uv']),'UV '+p.name)
   if 'rotation' in e:check(e['rotation']['angle'] in [-45,-22.5,0,22.5,45],'native rotation')
# A repeat of the generator must reproduce all resources byte for byte.
before={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in R.rglob('*') if p.is_file()};subprocess.run(['python','tools/storage_resources.py'],cwd=ROOT,check=True,stdout=subprocess.DEVNULL);after={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in R.rglob('*') if p.is_file()};check(before==after,'reproducible generator')
check('mod_version=1.1.8' in (ROOT/'gradle.properties').read_text(),'version')
report={'version':'1.1.8','checks':checks,'json':len(jsons),'model_texture_links':links,'minecraft_launches':0,'mode':'static/resources/geometry'}
if args.jar:
 z=zipfile.ZipFile(args.jar)
 for p in R.rglob('*'):
  if p.is_file() and p.suffix in ['.json','.png','.ogg']:check(z.read(p.relative_to(R).as_posix())==p.read_bytes(),'production resource')
 for c in ['Household','StorageBlock','StorageTile','SheafBlock','DryingRecipe']:check('org/slavicmyths/storage/'+c+'.class' in z.namelist(),'production class')
 check(b'version="1.1.8"' in z.read('META-INF/neoforge.mods.toml').replace(b' ',b''),'production version');check(not any('/smoke/' in p or '/verify/' in p or 'GameTest' in p for p in z.namelist()),'no test harness');report.update(checks=checks,mode='static/resources/geometry/production',sha256=hashlib.sha256(args.jar.read_bytes()).hexdigest())
(ROOT/'work/household-static-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
