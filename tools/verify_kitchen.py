"""Static data/geometry/production validation only; never launches Minecraft."""
import argparse,json,hashlib,re,zipfile,subprocess
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths';J=ROOT/'src/main/java/org/slavicmyths'
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);args=p.parse_args();checks=0;links=0

def check(ok,message):
 global checks;checks+=1
 if not ok:raise AssertionError(message)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
expected={'rye_bread':(6,.70,64),'karavai':(8,.85,16),'baked_turnip':(4,.50,64),'stewed_turnip':(6,.65,1),'stewed_cabbage':(6,.70,1),'oat_porridge':(6,.65,1),'barley_porridge':(6,.65,1),'berry_porridge':(7,.75,1),'vegetable_stew':(7,.70,1),'meat_stew':(9,.85,1),'pea_soup':(7,.75,1),'mushroom_stew_slavic':(6,.70,1),'ukha':(8,.80,1),'bliny':(6,.65,16),'berry_bliny':(8,.80,16),'meat_bliny':(9,.85,16),'apple_bliny':(8,.78,16),'honey_bliny':(8,.82,16),'apple_pie':(8,.80,16),'berry_pie':(8,.82,16),'meat_pie':(10,.90,16),'cabbage_pie':(8,.80,16)}
source=(J/'kitchen/KitchenII.java').read_text();old=(J/'registry/ModItems.java').read_text()
for name,(n,sat,stack) in expected.items():
 if name in ['karavai','berry_pie']:
  line=next(l for l in old.splitlines() if name.upper()+'=' in l);check(f'stacksTo({stack})' in line and f'nutrition({n})' in line and float(re.search(r'saturationModifier\(([.\d]+)F\)',line)[1])==sat,'reused food '+name)
 elif name=='ukha':check('UKHA=stew("ukha",8,.8F)' in old,'reused ukha')
 else:
  m=re.search(r'food\("'+name+r'",(\d+),([.\d]+)F,(\d+),(true|false)\)',source);check(m and (int(m[1]),float(m[2]),int(m[3]))==(n,sat,stack),'nutrition/saturation '+name);check((m[4]=='true')==(stack==1),'bowl return '+name)
 for folder in ['textures/item','models/item','models/table_food']:check((A/f'{folder}/{name}{".png" if folder.startswith("textures") else ".json"}').is_file(),'food asset '+name)
for name in list(expected)+['wheat_flour','rye_flour','oat_groats','barley_groats','dough','rolling_pin','metal_pot']:
 im=Image.open(A/f'textures/item/{name}.png');check(im.size==(16,16),'native icon '+name);check(set(im.getchannel('A').getdata())<={0,255},'no antialias '+name)
for wood in ['pine','linden']:
 path=A/f'models/block/{wood}_table.json';rel=path.relative_to(ROOT).as_posix();check(path.read_text(encoding='utf-8')==subprocess.check_output(['git','show','HEAD:'+rel],cwd=ROOT).decode().replace('\r\n','\n'),'unchanged SINGLE '+wood)
 original=read(path);check(len(read(A/f'blockstates/{wood}_table.json')['variants'])==20,'table states '+wood)
 for direction in ['north','south','east','west']:
  paired=read(A/f'models/block/{wood}_table_pair_{direction}.json');check(paired['textures']==original['textures'],'old paired materials');check(sum(e['to'][1]<=12 for e in paired['elements'])==2,'two outer legs per physical half')
for name in ['kitchen_wood','kitchen_metal','kitchen_dough']:check(Image.open(A/f'textures/block/{name}.png').size==(16,16),'native block density')
check(len(read(A/'blockstates/kitchen_table.json')['variants'])==8,'kitchen states')
for name in ['rolling_pin','metal_pot']:check('durability(' not in next(l for l in old.splitlines() if name.upper()+'=' in l),'tool no durability')
recipes={p.stem:read(p) for p in (D/'recipe/kitchen').glob('*.json')};check(len(recipes)==22,'22 kitchen recipes')
for name,r in recipes.items():
 check(r['type']=='slavicmyths:kitchen','recipe type');check(1<=len(r['ingredients'])<=4,'four ingredients');check(r['result']['id']=='slavicmyths:'+name,'result ID');check(all(1<=x['count']<=3 for x in r['ingredients']),'recipe counts')
 check(r['tool']['item']=='slavicmyths:'+('metal_pot' if r['mode']=='POT' else 'rolling_pin'),'required tool')
 check(r['time']==(160 if r['mode']=='POT' else 120 if name in ['rye_bread','karavai'] else 80),'exact time')
 check(r['servings']==(2 if name in ['stewed_turnip','stewed_cabbage'] else 3 if r['mode']=='POT' else 0),'servings')
 check(r['result']['count']==(2 if name in ['dough','bliny'] else 1),'output count')
 check(len(r['remainders'])<=2,'return slots fit distinct containers')
 if r['mode']=='POT':check(r['remainders']==[{'id':'minecraft:bucket','count':1}],'pot water/milk return')
check(recipes['karavai']['remainders']==[{'id':'minecraft:bucket','count':1},{'id':'minecraft:glass_bottle','count':1}],'karavai both returns')
for name in ['honey_bliny']:check(recipes[name]['remainders']==[{'id':'minecraft:glass_bottle','count':1}],'honey return')
for name in ['dough','rye_bread','bliny']:check(recipes[name]['remainders']==[{'id':'minecraft:bucket','count':1}],'bucket return')
for name in ['karavai','berry_pie']:check(not (D/f'recipe/{name}.json').exists(),'no kitchen crafting bypass')
for grain,out in [('minecraft:wheat','wheat_flour'),('slavicmyths:rye_grain','rye_flour'),('slavicmyths:oat_grain','oat_groats'),('slavicmyths:barley_grain','barley_groats')]:r=read(D/f'recipe/{out}.json');check(r['ingredients']==[{'item':grain}]*2 and r['result']['count']==1,'exact milling fallback')
check(read(D/'recipe/metal_pot.json')['pattern']==['I I','I I',' I '],'five iron pot');check(read(D/'recipe/rolling_pin.json')['key']['P']=={'tag':'minecraft:planks'},'any plank pin')
for name in ['smelting','smoking']:r=read(D/f'recipe/baked_turnip_{name}.json');check(r['experience']==.35,'turnip XP')
placed=set(read(D/'tags/item/placeable_table_foods.json')['values']);check(all('slavicmyths:'+s in placed for s in expected),'all foods placeable');check(not any('slavicmyths:'+s in placed for s in ['dough','wheat_flour','rolling_pin','metal_pot']),'no raw/tool displays')
check(read(D/'tags/item/cooking/eggs.json')['values']==['minecraft:egg','slavicmyths:goose_egg','slavicmyths:duck_egg'],'all eggs')
check(read(D/'tags/item/cooking/milk.json')['values']==['minecraft:milk_bucket','slavicmyths:goat_milk_bucket'],'both milks')
for lang in ['ru_ru','en_us']:
 text=read(A/f'lang/{lang}.json')
 for s in list(expected)+['wheat_flour','rye_flour','oat_groats','barley_groats','dough','rolling_pin','metal_pot']:check('item.slavicmyths.'+s in text,'lang '+s)
 check(text['kitchen.slavicmyths.servings'].count('%s')==2,'servings format')
adv=read(D/'advancement/generous_table.json');check('rewards' not in adv and not adv['display']['hidden'],'no XP hidden');check(adv['criteria']['dish']['conditions']['items'][0]['items']=='#slavicmyths:complex_dishes','adv tag')
check(read(D/'loot_table/blocks/kitchen_table.json')['pools']==[],'single owner block drop')
vanilla=zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');vn=set(vanilla.namelist())
def ref(s,kind,suffix):
 global links
 if s.startswith('#'):return
 links+=1;ns,path=s.split(':',1) if ':' in s else ('minecraft',s);key=f'assets/{ns}/{kind}/{path}{suffix}';check((R/key).is_file() or key in vn or path.startswith('builtin/'),'model/texture link '+key)
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
 value=read(p)
 if '/models/' in p.as_posix() or '/blockstates/' in p.as_posix():walk(value)
for folder in ['models/table_food','models/block','models/item']:
 for p in (A/folder).glob('*.json'):
  if folder!='models/table_food' and not p.stem.startswith(('kitchen_table','pine_table_pair','linden_table_pair')):continue
  for e in read(p).get('elements',[]):
   check(all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),'cuboid bounds '+p.name)
   for face in e['faces'].values():
    if 'uv' in face:check(all(0<=v<=16 for v in face['uv']),'UV legal '+p.name)
for p in (J/'kitchen').glob('*.java'):check('net.minecraft.client' not in p.read_text(),'common client isolation')
check('mod_version=1.1.8' in (ROOT/'gradle.properties').read_text(),'version')
belt=(J/'textile/BeltData.java').read_text(encoding='utf-8')
check('instanceof org.slavicmyths.furniture.TableBlock' in belt and 'instanceof org.slavicmyths.block.KitchenTableBlock' in belt,'table sneak has priority over belt')
report={'version':'1.1.8','checks':checks,'json':len(jsons),'model_texture_links':links,'minecraft_launches':0,'mode':'static/resources/geometry'}
if args.jar:
 jar=zipfile.ZipFile(args.jar)
 for p in R.rglob('*'):
  if p.is_file() and p.suffix in ['.json','.png','.ogg']:check(jar.read(p.relative_to(R).as_posix())==p.read_bytes(),'production resource '+str(p))
 for name in ['KitchenTile','TableTile','KitchenRecipe','KitchenMenu']:check('org/slavicmyths/kitchen/'+name+'.class' in jar.namelist(),'production class '+name)
 check(b'version="1.1.8"' in jar.read('META-INF/neoforge.mods.toml').replace(b' ',b''),'production version');check(not any('/smoke/' in s or '/verify/' in s or 'GameTest' in s for s in jar.namelist()),'no test harness')
 for name in ['karavai','berry_pie']:check('data/slavicmyths/recipe/'+name+'.json' not in jar.namelist(),'no stale crafting')
 report.update(checks=checks,mode='static/resources/geometry/production',sha256=hashlib.sha256(args.jar.read_bytes()).hexdigest())
(ROOT/'work/kitchen-static-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
