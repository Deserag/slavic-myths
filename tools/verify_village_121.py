"""1.2.1 resource/production gates plus the actual pure Java outfit tests. Never launches Minecraft."""
from pathlib import Path
import argparse, hashlib, json, subprocess, zipfile
from PIL import Image, ImageDraw
import village_resources

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';J=ROOT/'src/main/java/org/slavicmyths'
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);p.add_argument('--version',default='1.2.1');args=p.parse_args();checks=links=0
def check(value,message):
 global checks
 checks+=1
 if not value:raise AssertionError(message)
def read(path):return json.loads(path.read_text(encoding='utf-8'))
vanilla=set(zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar').namelist())
def ref(s,kind,suffix):
 global links
 if s.startswith('#'):return
 links+=1;ns,k=s.split(':',1) if ':' in s else ('minecraft',s);path=f'assets/{ns}/{kind}/{k}{suffix}'
 check((RES/path).exists()or path in vanilla or k.startswith('builtin/'),'Missing resource '+path)
def walk(o):
 if isinstance(o,dict):
  for k,v in o.items():
   if k in ['parent','model'] and isinstance(v,str):ref(v,'models','.json')
   elif k=='textures'and isinstance(v,dict):
    for t in v.values():ref(t,'textures','.png')
   elif isinstance(v,(dict,list)):walk(v)
 elif isinstance(o,list):
  for v in o:walk(v)
files=list(RES.rglob('*.json'))
for file in files:
 obj=read(file)
 if '/models/' in file.as_posix()or'/blockstates/'in file.as_posix():walk(obj)
roles=['miller','brewer','weaver','herder','hunter','cook'];listing_count=0
for role in roles:
 obj=read(RES/f'data/slavicmyths/village_trades/{role}.json');check(set(obj)==set('12345'),'Five levels '+role)
 for level,rows in obj.items():
  check(len(rows)>=4,'At least four listings '+role+'/'+level)
  check(len({json.dumps(r,sort_keys=True)for r in rows})==len(rows),'Duplicate listing '+role+'/'+level)
  for row in rows:
   listing_count+=1
   for key in ['input','output']:
    value=row[key];ns,name=value.split(':');check((A/f'models/item/{name}.json').exists()if ns=='slavicmyths'else f'assets/minecraft/models/item/{name}.json'in vanilla,'Trade item '+value)
   check(1<=row['cost']<=64 and 1<=row['count']<=64 and 1<=row['max_uses']<=16 and row['price_multiplier']==.05,'Vanilla price bounds')
   check(row['input']not in ['minecraft:bucket','slavicmyths:goat_milk_bucket'],'No cheap bucket loop')
for tag in ['acquirable_job_site','village']:
 obj=read(RES/f'data/minecraft/tags/point_of_interest_type/{tag}.json');check(not obj['replace']and {'slavicmyths:'+r for r in roles}.issubset(set(obj['values'])) and set(obj['values']).issubset({'slavicmyths:'+r for r in roles}|{'slavicmyths:druzhinnik'}),'Append POI tags')
for lang in ['ru_ru','en_us']:
 obj=read(A/f'lang/{lang}.json');overrides=read(RES/f'assets/minecraft/lang/{lang}.json');check(len(overrides)==15,'Minimal vanilla name overrides')
 for role in roles:check(bool(obj[f'entity.minecraft.villager.slavicmyths.{role}']),'Name '+role)
 for key in ['block.slavicmyths.millstone','village.slavicmyths.debug.none','village.slavicmyths.test.invalid']:check(key in obj,'Localized key')
check(set(read(A/'lang/ru_ru.json'))==set(read(A/'lang/en_us.json')),'Translation key parity')
images=list((A/'textures/entity/villager').rglob('*.png'))+list((A/'textures/entity/merchant').rglob('*.png'))+list((A/'textures/block').glob('millstone_*.png'))
for path in images:
 with Image.open(path)as im:
  check(im.size==(128,128),'Native 128x128 atlas '+path.name);check(len(im.getcolors(20000)or[])>=2,'No flat placeholder '+path.name)
  check(im.getbbox()is not None,'Empty atlas '+path.name)
 # Novice detail is intentionally transparent; ordinary adult base layers remain visible.
for role in village_resources.ROLES:
 for variant in ['a','b']:check((A/f'textures/entity/villager/profession/{role}_{variant}.png').exists(),'Profession texture '+role)
for path in (J/'village').glob('*.java'):check('net.minecraft.client.'not in path.read_text(encoding='utf-8'),'Server isolation '+path.name)
visual=(J/'client/VillageVisuals.java').read_text(encoding='utf-8')
for token in ['Random','getBiome(','getPoiManager(']:check(token not in visual,'No frame-time world/random work')
check('instanceof VillagerProfessionLayer' in visual and 'entity.isBaby()'in visual,'Filtered native layer, baby fallback')
check('LivingConversionEvent.Post'in (J/'village/VillageEvents.java').read_text(encoding='utf-8'),'Conversion seed retention')
# Reproducibility of every new image and relevant JSON, without overwriting unrelated systems.
paths=images+[A/f'lang/{lang}.json'for lang in ['ru_ru','en_us']]
before={x:hashlib.sha256(x.read_bytes()).hexdigest()for x in paths};village_resources.main()
check(before=={x:hashlib.sha256(x.read_bytes()).hexdigest()for x in paths},'Generator is byte reproducible')
out=ROOT/'work/village-unit';out.mkdir(parents=True,exist_ok=True)
jdk=Path('C:/Program Files/Java/jdk-21.0.12/bin')
subprocess.run([str(jdk/'javac.exe'),'-encoding','UTF-8','-d',str(out),str(J/'village/OutfitRules.java'),str(ROOT/'tools/village-tests/OutfitRulesTest.java')],check=True)
unit=subprocess.run([str(jdk/'java.exe'),'-cp',str(out),'OutfitRulesTest'],check=True,capture_output=True,text=True).stdout.strip();print(unit)
if args.jar:
 with zipfile.ZipFile(args.jar)as z:
  names=set(z.namelist());check('Implementation-Version: '+args.version in z.read('META-INF/MANIFEST.MF').decode(),'Production version')
  check(not any('VillageGameTests' in n or 'OutfitRulesTest' in n or '/verify/'in n or '/smoke/'in n for n in names),'No test harness in JAR')
  for file in RES.rglob('*'):
   if file.is_file():
    name=file.relative_to(RES).as_posix();check(name in names,'Bundled resource '+name)
    if name!='META-INF/neoforge.mods.toml':check(z.read(name)==file.read_bytes(),'Production resource bytes '+name)
  for path in ['village/VillageRoles','village/VillageTrades','village/VillageEvents','client/VillageVisuals','client/VillageOutfitGeometry']:
   check('org/slavicmyths/'+path+'.class'in names,'Production class '+path)
report={'version':args.version,'checks':checks,'json':len(files),'model_texture_links':links,'listings':listing_count,'new_images':len(images),'pure_java_result':unit,'production_jar':str(args.jar)if args.jar else None,'client_launches':0}
(ROOT/f'work/village-{args.version}-checks.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
