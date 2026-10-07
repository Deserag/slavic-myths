"""Scoped static acceptance for farming data/art and production packaging. No game launch."""
from pathlib import Path
from PIL import Image
import json,hashlib,zipfile,struct,argparse

root=Path(__file__).resolve().parents[1];r=root/'src/main/resources';d=r/'data/slavicmyths';a=r/'assets/slavicmyths'
parser=argparse.ArgumentParser();parser.add_argument('--jar',type=Path);args=parser.parse_args()
crops=['rye','barley','oat','turnip','cabbage','pea','flax'];produce=['rye_grain','barley_grain','oat_grain','turnip','cabbage','pea_pod','flax_stalk'];tools=['sickle','field_hoe','watering_can','organic_fertilizer']
checks=[]
def check(ok,name):
    if not ok:raise AssertionError(name)
    checks.append(name)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
textures=[];resources=[]
def require(p):check(p.is_file(),str(p.relative_to(r)));resources.append(p)
for c,p in zip(crops,produce):
    state=a/f'blockstates/{c}_crop.json';require(state);variants=read(state)['variants'];check(set(variants)=={f'age={age}' for age in range(7)},c+' age 0..6')
    stage_hashes=set()
    for age in range(7):
        name=f'{c}_crop_stage{age}';model=a/f'models/block/{name}.json';png=a/f'textures/block/{name}.png';require(model);require(png)
        check(read(model)['textures']['crop']=='slavicmyths:block/'+name,c+' model texture '+str(age));check(read(model)['render_type']=='minecraft:cutout',c+' cutout '+str(age));textures.append(png);stage_hashes.add(hashlib.sha256(png.read_bytes()).hexdigest())
    check(len(stage_hashes)==7,c+' seven distinct textures')
    loot=d/f'loot_table/blocks/{c}_crop.json';require(loot);pools=read(loot)['pools'];check(len(pools)==4,c+' four age/drop branches')
    check(pools[0]['conditions'][1]['chance']==.5,c+' early seed 50%');check(pools[1]['entries'][0]['name']=='slavicmyths:'+c+'_seeds',c+' intermediate seed only')
    main=pools[2]['entries'][0];check(main['name']=='slavicmyths:'+p,c+' produce id');check(main['functions'][-1]['function']=='slavicmyths:capped_crop_fortune',c+' bounded fortune only on produce')
    expected={'type':'minecraft:uniform','min':2,'max':3 if c=='flax' else 4} if c not in ['turnip','cabbage'] else 1
    check(main['functions'][0]['count']==expected,c+' base yield')
    seed=pools[3]['entries'][0];check(seed['functions']==[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3 if c=='flax' else 2}}],c+' seed yield no fortune')
    if c=='turnip':check(main['functions'][1]['conditions'][0]['chance']==.3,'turnip extra 30%')
for id in [c+'_seeds' for c in crops]+produce+tools:
    png=a/f'textures/item/{id}.png';model=a/f'models/item/{id}.json';require(png);require(model);textures.append(png)
    check(read(model)['textures']['layer0']=='slavicmyths:item/'+id,id+' correct item texture')
    for lang in ['ru_ru','en_us']:check('item.slavicmyths.'+id in read(a/f'lang/{lang}.json'),id+' '+lang)
for png in textures:
    im=Image.open(png);check(im.size==((16,16) if png.stem=='sickle' else (32,32)) and im.mode=='RGBA',png.stem+' native RGBA32');check(set(im.getchannel('A').getdata())=={0,255},png.stem+' binary alpha/no blur')
check(len({hashlib.sha256(p.read_bytes()).hexdigest() for p in textures})==67,'all 67 images distinct')
tag=read(d/'tags/block/sickle_harvestable.json');check(len(tag['values'])==12,'eight mod including hops + four vanilla harvestables')
for tag in ['durability','mining_loot']:
    values=read(r/f'data/minecraft/tags/item/enchantable/{tag}.json')['values'];check(all('slavicmyths:'+t in values for t in tools[:2]),tag+' enchants both tools')
for grass,chance in [('short_grass',.12),('tall_grass',.18)]:
    data=read(d/f'loot_modifiers/{grass}_farming_seeds.json');check(data['conditions'][1]['chance']==chance,grass+' seed probability')
    check(data['type']=='slavicmyths:grass_farming_seeds',grass+' additive modifier')
    if grass=='tall_grass':check(len(data['conditions'][2]['terms'])==2,'tall grass both halves guarded against secondary removal')
can=read(d/'recipe/watering_can.json');check(sum(row.count('I') for row in can['pattern'])==5,'can recipe five nuggets');check(sum(row.count('B') for row in can['pattern'])==1,'can recipe one bucket')
for name in tools:require(d/f'recipe/{name}.json')
adv=read(d/'advancement/new_seeds.json');check(adv['parent']=='minecraft:husbandry/root' and 'rewards' not in adv,'advancement vanilla parent no XP');check(adv['criteria']['seeds']['conditions']['items'][0]['items']=='#slavicmyths:agricultural_seeds','advancement any of seven seeds')

# Parse all JSON and validate texture/model links against local resources and installed vanilla assets.
native=root/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
vanilla=set(zipfile.ZipFile(native).namelist());builtin={'assets/minecraft/models/builtin/generated.json','assets/minecraft/models/builtin/entity.json'}
refs=0
def ref(id,folder,suffix):
    global refs
    if id.startswith('#'):return
    ns,path=id.split(':',1) if ':' in id else ('minecraft',id);target=f'assets/{ns}/{folder}/{path}{suffix}';refs+=1
    check((r/target).is_file() or target in vanilla or target in builtin,'reference '+target)
def walk(value):
    if isinstance(value,dict):
        for key,v in value.items():
            if key in ['parent','model'] and isinstance(v,str):ref(v,'models','.json')
            elif key=='textures' and isinstance(v,dict):
                for texture in v.values():ref(texture,'textures','.png')
            elif isinstance(v,(dict,list)):walk(v)
    elif isinstance(value,list):
        for v in value:walk(v)
json_count=0
for p in r.rglob('*.json'):
    value=read(p);json_count+=1
    if 'assets' in p.parts and ('models' in p.parts or 'blockstates' in p.parts):walk(value)
report={'status':'PASS','checks':len(checks),'jsonFiles':json_count,'references':refs,'cropStages':49,'itemSprites':18,'gameLaunches':0,'scope':'Static resource/data and production-JAR acceptance; not an in-game test'}
if args.jar:
    jar=args.jar.resolve();z=zipfile.ZipFile(jar);names=z.namelist()
    check(not any('/verify/' in n or 'GameTests' in n or n.startswith('tools/') for n in names),'production excludes test harness')
    check('version="1.1.0"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'production mod version 1.1.0')
    for p in resources:check(z.read(p.relative_to(r).as_posix())==p.read_bytes(),'jar resource '+str(p.relative_to(r)))
    for name in ['FarmingCrop','Farming','CappedFortune','GrassSeedsModifier','SickleItem','FieldHoeItem','WateringCanItem','FertilizerItem']:
        code=z.read('org/slavicmyths/farming/'+name+'.class');check(struct.unpack('>H',code[6:8])[0]==65,name+' Java21 class')
    for tag in ['durability','mining_loot']:check(z.read(f'data/minecraft/tags/item/enchantable/{tag}.json')==(r/f'data/minecraft/tags/item/enchantable/{tag}.json').read_bytes(),tag+' final packaged tags')
    for grass in ['short_grass','tall_grass']:check(z.read(f'data/slavicmyths/loot_modifiers/{grass}_farming_seeds.json')==(d/f'loot_modifiers/{grass}_farming_seeds.json').read_bytes(),grass+' final packaged modifier')
    report.update(jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),productionChecks=len(checks)-report['checks'])
out=root/'docs/verification/farming-1.1.0';out.mkdir(parents=True,exist_ok=True);(out/'static-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
