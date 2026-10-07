"""Resource/production gates and actual pure-Java tick arithmetic tests; no game launch."""
import argparse,json,hashlib,zipfile,subprocess
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths';J=ROOT/'src/main/java/org/slavicmyths';checks=0;links=0
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);args=p.parse_args()
def check(ok,msg):
 global checks;checks+=1
 if not ok:raise AssertionError(msg)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
vjar=ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar';vn=set(zipfile.ZipFile(vjar).namelist())
def ref(s,folder,suffix):
 global links
 if s.startswith('#'):return
 links+=1;ns,k=s.split(':',1)if ':'in s else ('minecraft',s);path=f'assets/{ns}/{folder}/{k}{suffix}';check((R/path).exists()or path in vn or k.startswith('builtin/'),'Missing '+path)
def walk(v):
 if isinstance(v,dict):
  for k,x in v.items():
   if k in ['model','parent']and isinstance(x,str):ref(x,'models','.json')
   elif k=='textures'and isinstance(x,dict):
    for t in x.values():ref(t,'textures','.png')
   elif isinstance(x,(dict,list)):walk(x)
 elif isinstance(v,list):
  for x in v:walk(x)
jsons=list(R.rglob('*.json'))
for file in jsons:
 v=read(file)
 if '/models/'in file.as_posix()or'/blockstates/'in file.as_posix():walk(v)
recipes=list((D/'recipe/vat').glob('*.json'));check(len(recipes)==15,'15 vat recipes')
expected={'malted_barley':1200,'honey_infusion':800,'forest_herbal_infusion':1000,'light_beer_wort':1200,'dark_beer_wort':1400,'hopped_beer_wort':1200,'honey_beer_wort':1400,'forest_beer_wort':1600,'thunder_beer_wort':1800,'veles_dark_wort':2000,'witch_berry_wort':1800,'kvass_wort':1000,'mead_must':1600,'cider_must':600,'berry_mors':600}
for file in recipes:
 r=read(file);check(r['time']==expected[file.stem],'time '+file.stem);check(r['type']=='slavicmyths:vat' and 1<=len(r['ingredients'])<=4,'four inputs');check(len(r['remainders'])<=2,'return slots')
 for value in [r['result']]+r['remainders']+[p['ingredient']for p in r['ingredients']]+([r['catalyst']]if 'catalyst'in r else[]):
  id=value.get('id',value.get('item',''))
  if id.startswith('slavicmyths:'):check((A/f'models/item/{id.split(":")[1]}.json').exists(),'ingredient/result registered resource '+id)
  if 'tag'in value:check((D/f'tags/item/{value["tag"].split(":")[1]}.json').exists(),'tag link')
check('kind==Kind.BARREL&&s.getValue(BREWING)'in (J/'storage/StorageBlock.java').read_text(encoding='utf-8'),'barrel STORAGE has no ticker')
for k in expected:
 if k.endswith(('wort','must')):check((A/f'models/item/{k}.json').exists(),'exact technical registry resource '+k)
check(read(D/'recipe/vat/veles_dark_wort.json')['retain_catalyst']is True,'staff not consumed')
check(read(D/'recipe/vat/thunder_beer_wort.json')['catalyst']['item']=='slavicmyths:thunder_crystal_powder','fifth reagent uses catalyst slot')
check(read(D/'recipe/vat/witch_berry_wort.json')['catalyst']['item']=='slavicmyths:rune_forest','witch low-tier rune')
check(read(D/'recipe/dark_bottle.json')['pattern']==[' G ','GSG'],'user chose THREE glass + stick')
check(read(D/'recipe/vat/cider_must.json')['remainders']==[{'id':'slavicmyths:dark_bottle','count':4}],'cider returns four bottles')
check(read(D/'recipe/vat/berry_mors.json')['result']['components']['minecraft:custom_data']['Servings']==4,'mors four servings')
check(read(D/'recipe/malt_sack_unpack.json')['result']['count']==9,'malt reversible nine')
for k in ['fruit_press','small_keg','fermentation_vat']:check(len(read(A/f'blockstates/{k}.json')['variants'])==20,'facing/visual states');check((D/f'loot_table/blocks/{k}.json').exists(),'equipment drops')
check(len(read(A/'blockstates/hops_crop.json')['variants'])==12,'six hops stages both halves')
for lang in ['ru_ru','en_us']:
 text=read(A/f'lang/{lang}.json')
 for k in ['hops','hops_cutting','malted_barley','malt_sack','wooden_mug','dark_bottle','ceramic_pitcher','berry_mors_pitcher','thunder_crystal_powder']:check('item.slavicmyths.'+k in text,'bilingual '+k)
 for k in ['young','ready','aged','spoiled','quality','status','inputs','catalyst']:check('brewing.slavicmyths.'+k in text,'bilingual status '+k)
for file in (J/'brewing').glob('*.java'):check('net.minecraft.client'not in file.read_text(encoding='utf-8'),'common isolation '+file.name)
for name in ['Fermentation.java','BrewTile.java']:
 check('KitchenTile.give(p,out);BeverageItem.obtained(p,out)' not in (J/'brewing'/name).read_text(encoding='utf-8'),'advancement snapshot before mutating inventory add')
source=(J/'brewing/BrewRules.java').read_text(encoding='utf-8')
test='''public static void main(String[] args){int[] ages={0,2399,2400,8399,8400,11999,12000,99999};String[] q={"young","young","ready","ready","aged","aged","spoiled","spoiled"};int checks=0;for(int i=0;i<ages.length;i++){if(!quality(ages[i]).equals(q[i]))throw new AssertionError("quality");checks++;}if(duration(45,"young")!=22||duration(8,"aged")!=10||duration(45,"aged")!=54||duration(90,"spoiled")!=0)throw new AssertionError("duration");checks+=4;for(int points=0;points<=5;points++)for(int elapsed:new int[]{0,1199,1200,2399,2400,6000}){if(decay(points,100,100+elapsed)!=Math.max(0,points-elapsed/1200))throw new AssertionError("decay");checks++;}if(decay(5,100,99)!=5)throw new AssertionError("clock rollback");checks++;System.out.println("BrewRules actual Java assertions: "+checks);}\n'''
code=source[:source.rfind('}')]+test+'}\n';temp=ROOT/'work/brewing-unit/BrewRules.java';temp.parent.mkdir(parents=True,exist_ok=True);temp.write_text(code,encoding='utf-8');subprocess.run(['C:/Program Files/Java/jdk-21.0.12/bin/java.exe',str(temp)],check=True,cwd=ROOT);check(True,'actual Java boundary tests: 49 assertions')
before={p:hashlib.sha256(p.read_bytes()).hexdigest()for p in R.rglob('*')if p.is_file()};subprocess.run(['python','tools/brewing_resources.py'],check=True,cwd=ROOT,stdout=subprocess.DEVNULL);after={p:hashlib.sha256(p.read_bytes()).hexdigest()for p in R.rglob('*')if p.is_file()};check(before==after,'resource generator reproducible')
new=set(p for p in R.rglob('*')if p.is_file())
for file in list((A/'textures/item').glob('*.png'))+list((A/'textures/block').glob('hops_*.png')):
 if file.stem not in ['hops','hops_cutting','malt_sack','malted_barley','fruit_pomace','berry_mash','honey_infusion','forest_herbal_infusion','thunder_crystal_powder','wooden_mug','dark_bottle','ceramic_pitcher','spoiled_brew']and not any(x in file.stem for x in ['beer','pitcher','juice','mead','cider','kvass','mors','wort','must','hops_']):continue
 im=Image.open(file);check(im.size==(16,16),'native16 '+file.name);check(set(im.getchannel('A').getdata())<={0,255},'no AA '+file.name)
check('mod_version=1.1.7'in(ROOT/'gradle.properties').read_text(),'version')
report={'version':'1.1.7','resource_checks':checks,'json':len(jsons),'links':links,'pure_java_assertions':49,'minecraft_launches':0,'jei':'No new categories; optional integration unchanged'}
if args.jar:
 z=zipfile.ZipFile(args.jar)
 for file in new:
  if file.suffix in ['.json','.png','.ogg']:check(z.read(file.relative_to(R).as_posix())==file.read_bytes(),'production bytes '+file.name)
 for cls in ['Brewing','BrewTile','BrewBlock','HopsCrop','BeverageItem','BrewRules','VatRecipe','VatMenu','Fermentation']:check('org/slavicmyths/brewing/'+cls+'.class'in z.namelist(),'production class '+cls)
 check(b'version="1.1.7"'in z.read('META-INF/neoforge.mods.toml').replace(b' ',b''),'production version');check(not any('/smoke/'in n or'GameTest'in n or'/verify/'in n for n in z.namelist()),'no test code');report.update(resource_checks=checks,sha256=hashlib.sha256(args.jar.read_bytes()).hexdigest())
(ROOT/'work/brewing-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
