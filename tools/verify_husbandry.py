"""1.1.8 static resource/UV/production checks. Does not launch/bootstrap Minecraft."""
from pathlib import Path
import argparse,json,zipfile,hashlib,re,math
from PIL import Image

ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
parser=argparse.ArgumentParser();parser.add_argument('--jar',type=Path);args=parser.parse_args()
checks=0
def check(ok,label):
    global checks
    checks+=1
    if not ok:raise AssertionError(label)
def read(p):return json.loads(p.read_text(encoding='utf-8'))

animals=('goose','duck','domestic_goat');products=('goose_egg','duck_egg','goat_milk_bucket','raw_goose','cooked_goose','raw_duck','cooked_duck','raw_goat','cooked_goat')
files=[]
for lang in ('ru_ru','en_us'):
    value=read(A/f'lang/{lang}.json')
    for name in products+tuple(n+'_spawn_egg' for n in animals):check(bool(value.get('item.slavicmyths.'+name)),lang+name)
    for name in animals:check(bool(value.get('entity.slavicmyths.'+name)),lang+name)
    for name in ('feeder','straw_nest'):check(bool(value.get('block.slavicmyths.'+name)),lang+name)
for name in products:
    p=A/f'textures/item/{name}.png';im=Image.open(p);check(im.size==(32,32),'32px '+name);check(set(im.getchannel('A').getdata())=={0,255},'crisp alpha '+name)
    check(read(A/f'models/item/{name}.json')['textures']['layer0']=='slavicmyths:item/'+name,'item model '+name)
for name in animals:
    check(read(A/f'models/item/{name}_spawn_egg.json')['parent']=='minecraft:item/template_spawn_egg','spawn egg '+name)
    spawn=read(D/f'neoforge/biome_modifier/{name}_spawns.json');check(spawn['spawners']==dict(type='slavicmyths:'+name,weight={'goose':8,'duck':10,'domestic_goat':4}[name],minCount=2,maxCount={'goose':5,'duck':4,'domestic_goat':3}[name]),'spawn settings '+name)
    check(set(spawn['biomes'])=={'minecraft:'+b for b in {'goose':['plains','meadow','river'],'duck':['river','swamp','meadow','plains'],'domestic_goat':['plains','meadow']}[name]},'biomes '+name)
    loot=read(D/f'loot_table/entities/{name}.json');check(len(loot['pools'])==(1 if name=='domestic_goat' else 2),'drop pools '+name)
    check(loot['pools'][0]['entries'][0]['functions'][-1]['function']=='minecraft:furnace_smelt','burning meat '+name)
for name,prop in [('feeder','fill_level'),('straw_nest','egg_count')]:
    state=read(A/f'blockstates/{name}.json');check(len(state['variants'])==16,'count/facing states '+name)
    for c in range(4):
        for f in ('north','east','south','west'):check(f'{prop}={c},facing={f}' in state['variants'],'variant '+name+str(c)+f)
        model=read(A/f'models/block/{name}_{c}.json');check(bool(model['elements']),'nonempty geometry '+name+str(c))
    check(read(D/f'loot_table/blocks/{name}.json')['pools'][0]['entries'][0]['name']=='slavicmyths:'+name,'block drop '+name)
check(read(D/'recipe/feeder.json')['pattern']==['P P','PNP','PPP'],'user-confirmed seven planks')
check(read(D/'recipe/feeder.json')['key']['P']['tag']=='minecraft:planks','all planks')
check(read(D/'recipe/straw_nest.json')['pattern']==['WWW','W W','WWW'],'eight wheat nest')
for species in ('goose','duck','goat'):
    for method,duration in [('smelting',200),('smoking',100)]:
        recipe=read(D/f'recipe/cooked_{species}_from_{method}.json');check(recipe['cookingtime']==duration and recipe['experience']==.35,'cooking '+species+method)
        check(recipe['ingredient']=={'item':'slavicmyths:raw_'+species} and recipe['result']['id']=='slavicmyths:cooked_'+species,'recipe '+species+method)
for product in ('goose_egg','duck_egg','goat_milk_bucket')+tuple(n+'_spawn_egg' for n in animals):check(not(D/f'recipe/{product}.json').exists(),'no forbidden recipe '+product)
expected={'goose':['oat_grain','barley_grain'],'duck':['oat_grain','barley_grain','pea_pod'],'domestic_goat':['minecraft:wheat','oat_grain','cabbage'],'cow':['rye_grain','barley_grain','oat_grain'],'sheep':['rye_grain','barley_grain','oat_grain'],'pig':['turnip'],'chicken':['rye_seeds','barley_seeds','oat_seeds','flax_seeds'],'rabbit':['cabbage']}
for name,values in expected.items():check(set(read(D/f'tags/item/animal_feed/{name}.json')['values'])=={v if ':' in v else 'slavicmyths:'+v for v in values},'feed '+name)
valid=read(D/'tags/item/animal_feed/all_valid.json')['values'];check(all('#slavicmyths:animal_feed/'+k in valid for k in expected),'all species feeds')
adv=read(D/'advancement/full_yard.json');check(adv['requirements']==[[a] for a in animals] and 'rewards' not in adv,'AND advancement without XP');check(adv['parent']=='minecraft:husbandry/breed_an_animal','vanilla parent preserved')
# Inspect the literal cuboids/UVs that the renderer actually bakes; no untracked metadata needed.
model_source=(ROOT/'src/main/java/org/slavicmyths/client/HusbandryModel.java').read_text()
uv={}
for kind,baby,body in re.findall(r'if\(kind==(\d) && baby==(true|false)\)\{(.*?)\n        \}',model_source,re.S):
    key=animals[int(kind)]+('_baby' if baby=='true' else '')
    parts=[]
    for part,u,v,p,cube in re.findall(r'add\("([^"]+)",(\d+),(\d+),new float\[\]\{([^}]+)\},new float\[\]\{([^}]+)\}\);',body):
        xyz=[float(n.rstrip('F')) for n in p.split(',')];box=[float(n.rstrip('F')) for n in cube.split(',')]
        parts.append(dict(part=part,uv=[int(u),int(v)],size=[math.ceil(2*(box[3]+box[5])),math.ceil(box[4]+box[5])],pivot=xyz,cube=box))
    uv[key]=parts
check(len(uv)==6 and all(uv.values()),'six actual renderer meshes')
for model,parts in uv.items():
    im=Image.open(A/f'textures/entity/{model}.png');check(im.size==(128,128),'entity atlas '+model)
    for part in parts:
        u,v=part['uv'];w,h=part['size'];check(u+w<=128 and v+h<=128,'UV bounds '+model+part['part']);check(im.crop((u,v,u+w,v+h)).getchannel('A').getextrema()==(255,255),'UV opacity '+model+part['part'])
for name in animals:check(uv[name]!=uv[name+'_baby'],'separate baby proportions '+name)
check(len({tuple((tuple(p['pivot']),tuple(p['cube'])) for p in uv[a]) for a in animals})==3,'three different adult silhouettes')
sounds=read(A/'sounds.json');audioCount=0
for name in animals:
    for action in (('ambient','hiss','hurt','death') if name=='goose' else ('ambient','hurt','death')):
        event=sounds['entity.'+name+'.'+action];check(event['sounds'][0]['type']=='event' and event['sounds'][0]['name'].startswith('minecraft:entity.'),'explicit vanilla fallback '+name+action);audioCount+=1

# Validate resource links using the pinned local vanilla assets, not web guesses.
vanilla=zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');names=set(vanilla.namelist());refs=0
def ref(id,folder,suffix):
    global refs
    if id.startswith('#'):return
    ns,p=id.split(':',1) if ':' in id else ('minecraft',id);target=f'assets/{ns}/{folder}/{p}{suffix}';refs+=1
    check((R/target).is_file() or target in names or target in ('assets/minecraft/models/builtin/generated.json','assets/minecraft/models/builtin/entity.json'),'resource link '+target)
def walk(v):
    if isinstance(v,dict):
        for k,item in v.items():
            if k in ('model','parent') and isinstance(item,str):ref(item,'models','.json')
            elif k=='textures' and isinstance(item,dict):
                for t in item.values():ref(t,'textures','.png')
            elif isinstance(item,(dict,list)):walk(item)
    elif isinstance(v,list):
        for item in v:walk(item)
jsonCount=0
for folder in ('models','blockstates'):
    for p in (A/folder).rglob('*.json'):walk(read(p));jsonCount+=1
for p in D.rglob('*.json'):read(p);jsonCount+=1

common=ROOT/'src/main/java/org/slavicmyths/husbandry'
for p in common.glob('*.java'):check('net.minecraft.client' not in p.read_text(),'server isolation '+p.name)
source=(common/'Husbandry.java').read_text();check(source.count('=animal(')==3,'exactly three new entity types');check(source.count('item("goose_spawn_egg"')+source.count('item("duck_spawn_egg"')+source.count('item("domestic_goat_spawn_egg"')==3,'exactly three adult spawn eggs')
check('mod_version=1.1.8' in (ROOT/'gradle.properties').read_text(),'single version source')
if args.jar:
    jar=zipfile.ZipFile(args.jar)
    # All current mod resources must be the exact production resources, including preserved gardens/crops.
    for p in R.rglob('*'):
        if p.is_file() and p.suffix in ('.json','.png','.ogg'):
            key=p.relative_to(R).as_posix();check(jar.read(key)==p.read_bytes(),'packaged '+key)
    for name in animals:check('org/slavicmyths/husbandry/YardAnimal.class' in jar.namelist(),'production animal '+name)
    check(b'version="1.1.8"' in jar.read('META-INF/neoforge.mods.toml').replace(b' ',b''),'production version')
    check(not any('/verify/' in name or '/smoke/' in name or 'GameTest' in name for name in jar.namelist()),'no test harness in production')
report={'version':'1.1.8','checks':checks,'json':jsonCount,'model_texture_links':refs,'audio_fallback_events':audioCount,'minecraft_launches':0,'kind':'static resources/UV'+('/production JAR' if args.jar else ''),'jar':str(args.jar) if args.jar else None}
if args.jar:report['sha256']=hashlib.sha256(args.jar.read_bytes()).hexdigest()
write=ROOT/'work/husbandry-static-checks.json';write.write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
