"""Scoped acquisition/localization audit and real pure Java geometry checks. No Minecraft launch."""
from pathlib import Path
import ast,json,re,subprocess,hashlib,zipfile,argparse
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths';J=ROOT/'src/main/java/org/slavicmyths'
parser=argparse.ArgumentParser();parser.add_argument('--jar',type=Path);args=parser.parse_args();checks=0
def check(ok,msg):
 global checks;checks+=1
 if not ok:raise AssertionError(msg)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def literal(file,name):
 tree=ast.parse((ROOT/'tools'/file).read_text(encoding='utf-8'))
 return next(ast.literal_eval(n.value)for n in tree.body if isinstance(n,ast.Assign)and any(isinstance(t,ast.Name)and t.id==name for t in n.targets))
scope=set()
for c in ['rye','barley','oat','turnip','cabbage','pea','flax']:scope.add(c+'_seeds')
scope.update(['rye_grain','barley_grain','oat_grain','turnip','cabbage','pea_pod','flax_stalk','sickle','field_hoe','watering_can','organic_fertilizer'])
berries=['raspberry','blueberry','blackcurrant','lingonberry','cranberry'];scope.update(berries);scope.update(b+'_sapling'for b in berries)
scope.update(literal('gardens_resources.py','WOOD'));scope.difference_update(['apple_wall_sign','apple_wall_hanging_sign'])
scope.update(['raw_goose','cooked_goose','raw_duck','cooked_duck','raw_goat','cooked_goat','goat_milk_bucket','goose_egg','duck_egg','feeder','straw_nest','goose_spawn_egg','duck_spawn_egg','domestic_goat_spawn_egg'])
scope.update(literal('textile_resources.py','names'))
scope.update(row[0]for row in literal('kitchen_resources.py','foods'));scope.update(row[0]for row in literal('kitchen_resources.py','materials'));scope.add('kitchen_table')
scope.update(literal('storage_resources.py','names'));scope.update(literal('brewing_resources.py','names'));scope.discard('hops_crop')
beers=list(literal('brewing_resources.py','beers'));scope.update(b+'_mug'for b in beers)
batches=['veles_dark_wort'if b=='veles_dark_beer'else'witch_berry_wort'if b=='witch_berry_beer'else b+'_wort'for b in beers]+['kvass_wort','mead_must','cider_must'];scope.update(batches)
recipes=list((D/'recipe').rglob('*.json'));result_sources={}
for p in recipes:
 r=read(p);result=r.get('result',{});id=result if isinstance(result,str)else result.get('id',result.get('item',''))
 if id.startswith('slavicmyths:'):result_sources.setdefault(id.split(':')[1],[]).append(p.relative_to(D).as_posix())
harvest=set(berries)|{b+'_sapling'for b in berries}|{'apple_sapling','apple_leaves','hops','hops_cutting'}|{c+'_seeds'for c in ['rye','barley','oat','turnip','cabbage','pea','flax']}|{'rye_grain','barley_grain','oat_grain','turnip','cabbage','pea_pod','flax_stalk'}
for wood in ['apple_log','apple_wood','stripped_apple_log','stripped_apple_wood']:harvest.add(wood)
mob={'animal_fat','goat_hide','raw_goose','raw_duck','raw_goat'}
process={'flax_fiber','linen_thread','linen_cloth','goat_milk_bucket','goose_egg','duck_egg','fruit_pomace','berry_mash','apple_juice_bottle','spoiled_brew','berry_mors_pitcher'}|{b+'_mug'for b in beers}|{'kvass_mug','mead_bottle','apple_cider_bottle','berry_mors_mug'}
inventory={};missing=[]
for id in sorted(scope):
 check((A/f'models/item/{id}.json').exists(),'registered scope asset '+id)
 if id in batches or id=='filled_pitcher':kind='TECHNICAL_HIDDEN';source='VatRecipe/BrewTile/Fermentation; Brewing.category=-1'
 elif id.endswith('_spawn_egg'):kind='CREATIVE_ONLY';source='Husbandry.init / existing Creative Tab'
 elif id in result_sources:
  source=', '.join(result_sources[id]);kind='CRAFT'if any(read(D/p)['type'].startswith('minecraft:crafting')for p in result_sources[id])else'PROCESS'
 elif id in harvest:kind='HARVEST_WORLDGEN';source='crop/leaf/bush loot; AppleLog stripping; Gardens/Hops worldgen'
 elif id in mob:kind='MOB_LOOT';source='Carcass.butcher; player kill drops'
 elif id in process:kind='PROCESS';source='TextileStation / YardAnimal / NestGoal / BrewTile / Fermentation'
 else:kind='MISSING';source='';missing.append(id)
 inventory[id]={'category':kind,'source':source}
check(not missing,'missing survival acquisition '+repr(missing))
ru=read(A/'lang/ru_ru.json');en=read(A/'lang/en_us.json');check(ru.keys()==en.keys(),'RU/EN key parity')
for id in scope:check('item.slavicmyths.'+id in ru or 'block.slavicmyths.'+id in ru,'translated acquisition '+id)
bad=[]
for k,v in ru.items():
 check('\ufffd'not in v,'replacement character '+k)
 try:
  repair=v.encode('cp1251').decode('utf-8')
  if len(repair)<len(v):bad.append(k)
 except UnicodeError:pass
check(not bad,'mojibake '+repr(bad))
patterns={'kitchen_table':['PPP','PCP','S S'],'dark_bottle':[' G ','GSG',' G '],'small_keg':['PNP','P P','PSP'],'fruit_press':['SIS','P P','PPP'],'fermentation_vat':['PIP','P P','PIP']}
for id,pattern in patterns.items():
 r=read(D/f'recipe/{id}.json');check(r['pattern']==pattern,'definitive recipe '+id)
 check(len([p for p in result_sources[id]if read(D/p)['type'].startswith('minecraft:crafting')])==1,'no conflicting craft '+id)
for p in recipes:
 r=read(p)
 if r['type']=='minecraft:crafting_shaped':
  chars=set(''.join(r['pattern']))-{' '};check(chars==set(r['key']),'undefined/unused pattern '+str(p))
  check(1<=len(r['pattern'])<=3 and len(set(map(len,r['pattern'])))==1 and max(map(len,r['pattern']))<=3,'recipe shape '+str(p))
for id in berries+['sickle']:
 im=Image.open(A/f'textures/item/{id}.png');check(im.size==(16,16),'native16 '+id);check(set(im.getchannel('A').getdata())<={0,255},'no AA '+id)
for id,group,biomes in [('goose',(2,5),['plains','meadow','river']),('duck',(2,4),['river','swamp','meadow','plains']),('domestic_goat',(2,3),['plains','meadow'])]:
 r=read(D/f'neoforge/biome_modifier/{id}_spawns.json');check(r['type']=='neoforge:add_spawns','NeoForge spawn schema');check((r['spawners']['minCount'],r['spawners']['maxCount'])==group,'spawn group');check(r['biomes']==['minecraft:'+b for b in biomes],'spawn biomes')
check(read(D/'worldgen/structure_set/kurgan_small.json')['structures']==[{'structure':'slavicmyths:kurgan_'+t,'weight':w}for t,w in [('small',41),('warrior',4),('great',1)]],'shared kurgan candidates')
for id in ['warrior','great']:check(read(D/f'worldgen/structure_set/kurgan_{id}.json')['placement']['frequency']==0,'legacy tier has no independent candidates')
# Run the actual production geometry, without Minecraft or stub world classes.
source=(J/'wood/TreeShape.java').read_text(encoding='utf-8')
test='''public static void main(String[]a){int n=0;for(int seed=0;seed<100;seed++){TreeShape s=new TreeShape("pine",new Random(seed));int h=s.logs.keySet().stream().mapToInt(c->c.y).max().orElseThrow()+1;if(h<9||h>12||s.logs.keySet().stream().anyMatch(c->c.x!=0||c.z!=0)||s.leaves.keySet().stream().anyMatch(c->c.y>=h))throw new AssertionError("single pine dimensions");n++;TreeShape g=giantPine(new Random(seed)::nextInt);int gh=g.logs.keySet().stream().mapToInt(c->c.y).max().orElseThrow()+1;if(gh<15||gh>20||g.logs.size()!=gh*4||g.leaves.keySet().stream().anyMatch(c->c.y>=gh||c.x < -3||c.x>4||c.z < -3||c.z>4))throw new AssertionError("giant bounds");n++;if(g.logs.keySet().stream().anyMatch(g.leaves::containsKey)||g.leaves.values().stream().anyMatch(d->d<1||d>6))throw new AssertionError("leaf/log overlap or decay distance");n++;}System.out.println("Actual TreeShape assertions: "+n);}\n'''
code=source[:source.rfind('}')]+test+'}\n';unit=ROOT/'work/integration-unit/TreeShape.java';unit.parent.mkdir(parents=True,exist_ok=True);unit.write_text(code,encoding='utf-8');subprocess.run(['C:/Program Files/Java/jdk-21.0.12/bin/java.exe',str(unit)],check=True)
report={'version':'1.1.8','checks':checks,'acquisition_checked':len(scope),'acquisition_missing':missing,'missing_ru_keys':len(set(en)-set(ru)),'missing_en_keys':len(set(ru)-set(en)),'mojibake_remaining':len(bad),'tree_java_assertions':300,'minecraft_launches':0}
if args.jar:
 z=zipfile.ZipFile(args.jar);check(b'version="1.1.8"'in z.read('META-INF/neoforge.mods.toml'),'production version')
 check('META-INF/accesstransformer.cfg'in z.namelist(),'head layer AT packaged')
 for cls in ['client/PeruniteArmor','client/ClothingHeadLayer','rpg/RuneDefinition']:check('org/slavicmyths/'+cls+'.class'in z.namelist(),'production class '+cls)
 check(not any('/smoke/'in p or '/verify/'in p or 'GameTest'in p for p in z.namelist()),'test harness excluded')
 report['sha256']=hashlib.sha256(args.jar.read_bytes()).hexdigest()
(ROOT/'work/obtainability-1.1.8.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');(ROOT/'work/integration-1.1.8-checks.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
