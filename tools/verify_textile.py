"""Static 1.1.4 assets/data/production checks. Never starts Minecraft."""
from pathlib import Path
from PIL import Image
import argparse,json,zipfile,hashlib,re
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
cli=argparse.ArgumentParser();cli.add_argument('--jar',type=Path);args=cli.parse_args();checks=0
def check(ok,label):
    global checks
    checks+=1
    if not ok:raise AssertionError(label)
def read(p):return json.loads(p.read_text(encoding='utf-8'))
items=['butchering_knife','animal_fat','goat_hide','flax_fiber','linen_thread','linen_cloth','linen_shirt','linen_ports','sarafan','linen_headscarf','linen_apron','folk_vest','bast_shoes','woven_belt']
blocks=['flax_breaker','spinning_wheel','loom_table','linen_bed','tallow_candle'];clothes=items[6:]
for name in items:
    p=A/f'textures/item/{name}.png';im=Image.open(p);check(im.size==(32,32),'32px icon '+name);check(set(im.getchannel('A').getdata())=={0,255},'binary alpha '+name)
    check(read(A/f'models/item/{name}.json')['textures']['layer0']=='slavicmyths:item/'+name,'item texture '+name)
for name in clothes:check(Image.open(A/f'textures/entity/clothing/{name}.png').size==(128,128),'separate wearable '+name)
check(len({hashlib.sha256((A/f'textures/item/{name}.png').read_bytes()).hexdigest() for name in clothes})==8,'eight different silhouettes/icons')
for name in ['cow','pig','sheep','rabbit','goose','duck','domestic_goat']:check(Image.open(A/f'textures/entity/carcass/{name}.png').size==(64,64),'carcass atlas '+name)
check(Image.open(A/'textures/mob_effect/well_rested.png').size==(18,18),'standard effect icon')
expected={'flax_breaker':24,'spinning_wheel':16,'loom_table':8,'linen_bed':16,'tallow_candle':16}
for name,n in expected.items():check(len(read(A/f'blockstates/{name}.json')['variants'])==n,'all state variants '+name)
recipePatterns={'butchering_knife':[' I','S '],'flax_breaker':['PPP',' S ','PPP'],'spinning_wheel':[' S ','PSP','PPP'],'loom_table':['PSP','S S','PPP'],'linen_bed':['CCC','PPP'],'linen_shirt':['C C','CRC',' C '],'linen_ports':['CCC','C C','C C'],'sarafan':['CRC','CCC','C C'],'linen_headscarf':['CC','CR'],'linen_apron':[' C ','CCC',' R '],'bast_shoes':['F F','FTF']}
for name,pattern in recipePatterns.items():check(read(D/f'recipe/{name}.json')['pattern']==pattern,'exact recipe '+name)
for name in ['flax_breaker','spinning_wheel','loom_table','linen_bed']:check(read(D/f'recipe/{name}.json')['key']['P']=={'tag':'minecraft:planks'},'all plank types '+name)
check(read(D/'recipe/folk_vest.json')['type']=='minecraft:crafting_shapeless' and len(read(D/'recipe/folk_vest.json')['ingredients'])==5,'vest shapeless five inputs')
check(read(D/'recipe/woven_belt.json')['ingredients']==[{'item':'slavicmyths:linen_thread'}]*3+[{'tag':'c:dyes/red'}],'belt three thread and red dye')
check(read(D/'recipe/tallow_candle.json')['result']['count']==2,'two candles')
for p in (D/'recipe').glob('*.json'):
    data=read(p);result=data.get('result');id=result.get('id') if isinstance(result,dict) else result
    check(id not in ('slavicmyths:linen_thread','slavicmyths:linen_cloth','slavicmyths:flax_fiber'),'no crafting bypass '+p.name)
for name in ['linen_thread','linen_cloth']:check(not(D/f'advancement/recipes/{name}.json').exists(),'obsolete recipe unlock removed '+name)
for tag,values in {'textile/flax_materials':['flax_stalk','flax_fiber','linen_thread','linen_cloth'],'textile/linen_clothing':clothes[:-1],'accessories/belts':['woven_belt'],'butchering_knives':['butchering_knife']}.items():check(read(D/f'tags/item/{tag}.json')['values']==['slavicmyths:'+v for v in values],'tag '+tag)
for name in blocks:
    loot=read(D/f'loot_table/blocks/{name}.json');check(loot['pools'][0]['entries'][0]['name']=='slavicmyths:'+name,'block loot '+name)
loot=read(D/'loot_table/blocks/tallow_candle.json');func=loot['pools'][0]['entries'][0]['functions'];check([f['count'] for f in func[:-1]]==[2,3,4],'candle count literal native schema');check(not any(f['function']=='minecraft:copy_state' for f in func),'no candle count placement duplication')
check(read(D/'loot_table/blocks/linen_bed.json')['pools'][0]['conditions'][-1]['properties']=={'part':'head'},'one bed drop')
adv=read(D/'advancement/linen_craft.json');check(adv['parent']=='minecraft:husbandry/root' and 'rewards' not in adv and not adv['display']['hidden'],'single task without XP')
for lang in ['ru_ru','en_us']:
    data=read(A/f'lang/{lang}.json')
    for name in items:check(bool(data.get('item.slavicmyths.'+name)),'item lang '+name+lang)
    for name in blocks:check(bool(data.get('block.slavicmyths.'+name)),'block lang '+name+lang)
    for key in ['entity.slavicmyths.carcass','effect.slavicmyths.well_rested','message.slavicmyths.requires_flax','message.slavicmyths.belt_equipped','message.slavicmyths.belt_occupied','advancement.slavicmyths.linen_craft.title','advancement.slavicmyths.linen_craft.description']:check(bool(data.get(key)),'lang '+key+lang)
vanilla=zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');vn=set(vanilla.namelist());refs=0;jsonCount=0
def ref(id,folder,suffix):
    global refs
    if id.startswith('#'):return
    ns,path=id.split(':',1) if ':' in id else ('minecraft',id);key=f'assets/{ns}/{folder}/{path}{suffix}';refs+=1
    check((R/key).is_file() or key in vn or key in ('assets/minecraft/models/builtin/generated.json','assets/minecraft/models/builtin/entity.json'),'link '+key)
def walk(v):
    if isinstance(v,dict):
        for k,x in v.items():
            if k in ('parent','model') and isinstance(x,str):ref(x,'models','.json')
            elif k=='textures' and isinstance(x,dict):
                for t in x.values():ref(t,'textures','.png')
            elif isinstance(x,(dict,list)):walk(x)
    elif isinstance(v,list):
        for x in v:walk(x)
for folder in ['models','blockstates']:
    for p in (A/folder).rglob('*.json'):walk(read(p));jsonCount+=1
for p in D.rglob('*.json'):read(p);jsonCount+=1
for p in (A/'models/block').glob('*.json'):
    if not p.stem.startswith(('flax_breaker','spinning_wheel','loom_table','linen_bed','tallow_candle')):continue
    for e in read(p).get('elements',[]):
        check(all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),'legal cuboid '+p.name)
        if 'rotation' in e:check(e['rotation']['angle'] in [-45,-22.5,0,22.5,45],'native rotation '+p.name)
source=ROOT/'src/main/java/org/slavicmyths/textile'
for p in source.glob('*.java'):check('net.minecraft.client' not in p.read_text(encoding='utf-8'),'common isolation '+p.name)
t=(source/'Textiles.java').read_text();check(t.count('ENTITIES.register(')==1,'one carcass entity');check('Attributes.BLOCK_BREAK_SPEED' in t and ',.05,' in t and ',.10,' in t,'exact attributes');check('ArmorMaterial' not in t,'no armor material for clothes')
check('extends Item implements Equipable' in (source/'ClothingItem.java').read_text(),'clothes not armor/damageable')
check('sync(ItemStack.OPTIONAL_STREAM_CODEC)' in (source/'BeltData.java').read_text(),'native attachment wire codec')
check('mod_version=1.1.4' in (ROOT/'gradle.properties').read_text(),'version source')
if args.jar:
    jar=zipfile.ZipFile(args.jar)
    for p in R.rglob('*'):
        if p.is_file() and p.suffix in ['.json','.png','.ogg']:check(jar.read(p.relative_to(R).as_posix())==p.read_bytes(),'production '+str(p))
    for name in ['linen_thread','linen_cloth']:check(f'data/slavicmyths/recipe/{name}.json' not in jar.namelist(),'no stale recipe in production '+name)
    for name in ['Carcass','BeltData','TextileStation','TextileEvents','ClothingItem','LinenBed']:check(f'org/slavicmyths/textile/{name}.class' in jar.namelist(),'production class '+name)
    check(b'version="1.1.4"' in jar.read('META-INF/neoforge.mods.toml').replace(b' ',b''),'production version')
    check(not any('/smoke/' in p or '/verify/' in p or 'GameTest' in p for p in jar.namelist()),'no test harness')
report={'version':'1.1.4','checks':checks,'json':jsonCount,'model_texture_links':refs,'minecraft_launches':0,'mode':'static/UV/resources'+('/production' if args.jar else '')}
if args.jar:report['jar']=str(args.jar);report['sha256']=hashlib.sha256(args.jar.read_bytes()).hexdigest()
(ROOT/'work/textile-static-checks.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
