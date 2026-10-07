"""1.1.4 authored native pixels/models/data; preserves earlier registry IDs."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random,math
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
def js(p,obj):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(p,im):p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def ornament(d,x,y,width,color=(143,41,36,255),pale=(233,215,179,255)):
    d.line((x,y,x+width-1,y),fill=color)
    for px in range(x,x+width,4):d.polygon([(px,y+2),(px+1,y+1),(px+2,y+2),(px+1,y+3)],fill=color);d.point((px+1,y+2),fill=pale)
    d.line((x,y+4,x+width-1,y+4),fill=color)

names={
'butchering_knife':('Разделочный нож','Butchering Knife'),'animal_fat':('Животный жир','Animal Fat'),'goat_hide':('Козья шкура','Goat Hide'),'flax_fiber':('Льняное волокно','Flax Fiber'),'linen_thread':('Льняная нить','Linen Thread'),'linen_cloth':('Льняное полотно','Linen Cloth'),'flax_breaker':('Льномялка','Flax Breaker'),'spinning_wheel':('Прялка','Spinning Wheel'),'loom_table':('Ткацкий станок','Weaving Loom'),'tallow_candle':('Сальная свеча','Tallow Candle'),'linen_bed':('Льняная постель','Linen Bed'),'linen_shirt':('Льняная рубаха','Linen Shirt'),'linen_ports':('Порты','Linen Trousers'),'sarafan':('Сарафан','Sarafan'),'linen_headscarf':('Льняной платок','Linen Headscarf'),'linen_apron':('Льняной фартук','Linen Apron'),'folk_vest':('Народный жилет','Folk Vest'),'bast_shoes':('Лапти','Bast Shoes'),'woven_belt':('Тканый пояс','Woven Belt')}
CLOTHES=['linen_shirt','linen_ports','sarafan','linen_headscarf','linen_apron','folk_vest','bast_shoes','woven_belt']

def icons():
    for name in list(names):
        if name in ['flax_breaker','spinning_wheel','loom_table','tallow_candle','linen_bed']:continue
        im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);cream=(224,213,185,255);shade=(161,143,115,255);red=(128,35,37,255);dark=(66,47,32,255)
        if name=='butchering_knife':
            # Short wide upward-curved working blade, dark wood grip, one rivet; no dagger template.
            d.polygon([(11,20),(13,14),(18,9),(26,5),(29,3),(27,12),(22,18),(17,23)],fill=(60,68,70,255));d.polygon([(13,19),(16,14),(25,7),(27,6),(25,12),(20,18),(16,21)],fill=(172,183,180,255));d.line([(16,21),(22,17),(27,11)],fill=(229,234,215,255),width=2)
            d.polygon([(5,29),(2,26),(12,17),(15,20)],fill=dark);d.line((4,26,12,20),fill=(115,74,41,255),width=2);d.point((11,21),fill=(210,199,155,255))
        elif name=='animal_fat':
            d.polygon([(5,20),(7,13),(13,9),(18,11),(24,8),(27,15),(25,23),(18,27),(10,25)],fill=shade);d.polygon([(7,18),(11,12),(16,13),(23,11),(25,16),(21,22),(13,24),(8,22)],fill=(242,227,191,255));d.line((9,18,17,13),fill=(255,242,212,255),width=2)
        elif name=='goat_hide':
            d.polygon([(5,12),(9,7),(13,10),(20,6),(25,10),(23,16),(27,21),(23,26),(15,25),(9,28),(5,23),(7,18)],fill=(106,88,64,255));d.polygon([(7,12),(11,10),(15,13),(21,9),(22,16),(24,21),(20,24),(10,24),(8,20)],fill=(189,169,128,255));d.line((8,19,22,19),fill=(224,207,164,255),width=2)
        elif name=='flax_fiber':
            for i in range(10):d.line([(7+i%4,28-i%3),(12+i,17),(18+i%8,3+i%5)],fill=cream if i%2 else shade,width=1)
            d.rectangle((10,19,20,21),fill=(123,93,54,255))
        elif name=='linen_thread':
            d.ellipse((7,7,25,24),fill=shade);d.ellipse((8,7,23,22),fill=cream)
            for y in range(9,22,3):d.arc((7,y-2,24,y+6),0,180,fill=(179,163,132,255),width=1)
            d.line([(9,22),(6,26),(13,28)],fill=cream,width=2)
        elif name=='linen_cloth':
            d.polygon([(5,12),(21,6),(28,11),(26,25),(10,28),(4,24)],fill=shade);d.polygon([(5,11),(21,7),(27,11),(11,18),(5,16)],fill=cream);d.polygon([(6,17),(11,19),(26,13),(25,22),(10,26),(6,24)],fill=(207,196,169,255))
            for y in [19,22,25]:d.line((10,y,24,y-5),fill=cream)
            for x in range(11,23,3):d.point((x,24-(x-11)//3),fill=shade)
        elif name=='linen_shirt':
            d.polygon([(11,5),(5,8),(2,23),(8,25),(11,18),(10,29),(23,29),(22,18),(25,25),(30,23),(26,8),(21,5),(18,9),(14,9)],fill=dark);d.polygon([(11,7),(6,9),(4,22),(8,23),(12,13),(12,27),(21,27),(20,13),(24,23),(28,22),(25,10),(21,7),(18,11),(14,11)],fill=cream);d.line([(13,7),(15,14),(19,14),(21,7)],fill=red,width=2);ornament(d,12,23,10);d.line((4,22,8,23),fill=red,width=2);d.line((24,23,28,22),fill=red,width=2)
        elif name=='linen_ports':
            d.polygon([(8,4),(24,4),(25,28),(18,29),(16,16),(14,29),(6,28)],fill=dark);d.polygon([(9,6),(22,6),(23,27),(19,27),(17,14),(15,14),(12,27),(8,26)],fill=(186,172,143,255));d.line((9,8,22,8),fill=shade,width=2);d.line([(16,8),(14,12),(18,12)],fill=cream)
        elif name=='sarafan':
            d.polygon([(10,4),(14,4),(14,9),(19,9),(19,4),(22,4),(22,13),(29,29),(3,29),(10,13)],fill=dark);d.polygon([(11,5),(13,5),(13,11),(20,11),(20,5),(21,5),(20,14),(26,27),(6,27),(12,14)],fill=red);d.line((13,12,10,24),fill=(162,47,45,255),width=2);d.line((20,12,23,24),fill=(162,47,45,255),width=2);ornament(d,6,23,21,(225,190,150,255),red)
        elif name=='linen_headscarf':
            d.polygon([(16,3),(25,10),(29,23),(24,28),(16,17),(5,23),(3,15),(7,8)],fill=shade);d.polygon([(15,5),(23,11),(26,22),(23,25),(16,14),(5,19),(7,10)],fill=cream);ornament(d,7,12,16);d.line((17,17,23,24),fill=red,width=2)
        elif name=='linen_apron':
            d.line([(11,8),(11,3),(21,3),(21,8)],fill=dark,width=2);d.polygon([(11,8),(21,8),(22,17),(27,29),(5,29),(10,17)],fill=shade);d.polygon([(12,9),(20,9),(20,17),(25,27),(7,27),(12,17)],fill=cream);ornament(d,8,23,17);d.line((8,17,24,17),fill=dark)
        elif name=='folk_vest':
            d.polygon([(10,4),(4,7),(6,28),(26,28),(28,7),(22,4),(16,12)],fill=dark);d.polygon([(10,6),(7,9),(8,26),(14,26),(14,13)],fill=(106,59,42,255));d.polygon([(22,6),(25,9),(24,26),(18,26),(18,13)],fill=(106,59,42,255));d.line([(10,6),(14,13),(14,26)],fill=red,width=2);d.line([(22,6),(18,13),(18,26)],fill=red,width=2);d.line((8,26,24,26),fill=red,width=2)
        elif name=='bast_shoes':
            for x,y in [(2,15),(13,20)]:
                d.polygon([(x,y+6),(x+5,y),(x+14,y-3),(x+15,y+3),(x+10,y+8),(x+2,y+9)],fill=dark);d.polygon([(x+2,y+5),(x+7,y+1),(x+13,y-1),(x+13,y+3),(x+8,y+6),(x+3,y+7)],fill=(174,122,61,255))
                for k in range(3):d.line((x+3+k*3,y+5,x+6+k*3,y+1),fill=(226,180,104,255))
        elif name=='woven_belt':
            d.ellipse((3,7,28,20),fill=dark);d.ellipse((5,8,26,18),fill=red);d.ellipse((9,9,24,13),fill=dark);d.rectangle((19,16,22,29),fill=red);d.rectangle((24,16,27,26),fill=red);ornament(d,5,13,22,(224,177,123,255),red);d.line((19,28,22,28),fill=cream);d.line((24,25,27,25),fill=cream)
        save(A/f'textures/item/{name}.png',im);js(A/f'models/item/{name}.json',{'parent':'minecraft:item/handheld' if name=='butchering_knife' else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})

def textures():
    bases={'textile_wood':(133,86,44),'textile_fiber':(201,187,140),'textile_thread':(213,204,177),'textile_cloth':(226,217,188),'linen_cover':(126,36,39),'tallow_wax':(230,210,150),'tallow_wick':(43,35,27)}
    for name,base in bases.items():
        im=Image.new('RGBA',(32,32),base+(255,));d=ImageDraw.Draw(im);rng=random.Random(name)
        for _ in range(140):
            x=rng.randrange(32);y=rng.randrange(32);n=rng.randrange(-18,15);color=tuple(max(0,min(255,c+n)) for c in base)+(255,)
            if name=='textile_wood':d.line((x,y,x+3,y),fill=color)
            elif name=='textile_fiber':d.line((x,y,x,y+4),fill=color)
            else:d.point((x,y),fill=color)
        if name=='linen_cover':ornament(d,0,23,32,(226,204,163,255),(126,36,39,255))
        if name=='tallow_wax':d.line([(22,0),(22,9),(25,9),(25,16)],fill=(247,231,180,255),width=3)
        save(A/f'textures/block/{name}.png',im)
    for name in CLOTHES:
        base=(222,212,186) if name in ['linen_shirt','linen_headscarf','linen_apron'] else (175,159,130) if name=='linen_ports' else (116,33,38) if name in ['sarafan','woven_belt'] else (91,50,34) if name=='folk_vest' else (173,128,65)
        im=Image.new('RGBA',(128,128),base+(255,));d=ImageDraw.Draw(im);rng=random.Random(name)
        for y in range(128):
            for x in range(128):
                n=rng.choice([-4,-2,0,0,2,4]);d.point((x,y),fill=tuple(c+n for c in base)+(255,))
        if name=='linen_shirt':
            d.line([(4,4),(5,8),(10,8),(11,4)],fill=(151,39,36,255));ornament(d,4,13,8);ornament(d,36,12,8)
        elif name=='sarafan':
            d.rectangle((32,0,58,17),fill=(227,216,187,255));ornament(d,36,12,8);d.rectangle((4,4,11,6),fill=(235,220,191,255))
            for y in [30,46,62]:d.line((6,y,6,y+4),fill=(165,48,45,255));ornament(d,5,63,22,(228,203,162,255),(116,33,38,255))
        elif name=='linen_headscarf':
            for y in [7,25,40,47,53]:ornament(d,64,y,23)
        elif name=='linen_apron':
            ornament(d,1,30,10);d.rectangle((32,0,62,20),fill=(103,57,40,255))
        elif name=='folk_vest':
            for x in [0,9,32,35,48,51,64,80]:d.line((x,0,x,15),fill=(156,47,39,255))
        elif name=='bast_shoes':
            for y in range(0,128,4):
                for x in range(0,128,4):d.line((x,y,x+3,y+3),fill=(222,175,97,255));d.line((x+3,y,x,y+3),fill=(132,86,41,255))
        elif name=='woven_belt':
            for y in [4,8,12]:ornament(d,0,y,48,(229,197,146,255),(116,33,38,255))
        save(A/f'textures/entity/clothing/{name}.png',im)
    for name,base in [('cow',(137,107,74)),('pig',(186,150,122)),('sheep',(215,206,179)),('rabbit',(163,137,102)),('goose',(223,215,188)),('duck',(135,99,66)),('domestic_goat',(191,175,144))]:
        im=Image.new('RGBA',(64,64),base+(255,));d=ImageDraw.Draw(im);rng=random.Random(name)
        for _ in range(300):x=rng.randrange(64);y=rng.randrange(22);d.point((x,y),fill=tuple(max(0,c-rng.randrange(15)) for c in base)+(255,))
        d.rectangle((0,24,63,63),fill=(116,84,48,255))
        for y in range(24,64,3):d.line((0,y,63,y),fill=(180,145,91,255))
        save(A/f'textures/entity/carcass/{name}.png',im)
    im=Image.new('RGBA',(18,18));d=ImageDraw.Draw(im);d.rectangle((3,7,14,14),fill=(218,202,160,255));d.rectangle((4,8,13,12),fill=(247,233,197,255));d.line((6,3,11,3),fill=(138,39,37,255));d.point((9,2),fill=(138,39,37,255));save(A/'textures/mob_effect/well_rested.png',im)

def element(a,b,tex):return {'from':a,'to':b,'faces':{f:{'texture':'#'+tex} for f in ['north','south','east','west','up','down']}}
def model(name,elements):js(A/f'models/block/{name}.json',{'parent':'minecraft:block/block','textures':{'wood':'slavicmyths:block/textile_wood','fiber':'slavicmyths:block/textile_fiber','thread':'slavicmyths:block/textile_thread','cloth':'slavicmyths:block/textile_cloth','cover':'slavicmyths:block/linen_cover','particle':'slavicmyths:block/textile_wood'},'elements':elements})
def variants(name,states):
    result={}
    for props,m in states:
        for f,y in [('north',0),('east',90),('south',180),('west',270)]:result[props+(',facing=' if props else 'facing=')+f]={'model':'slavicmyths:block/'+m,'y':y,'uvlock':True}
    js(A/f'blockstates/{name}.json',{'variants':result})
def blocks():
    states=[]
    for loaded in [False,True]:
        for step in range(3):
            name=f'flax_breaker_{int(loaded)}_{step}';e=[element([1,0,2],[15,2,14],'wood'),element([1,2,3],[3,10,13],'wood'),element([13,2,3],[15,10,13],'wood'),element([5,2,3],[7,7,13],'wood'),element([9,2,3],[11,7,13],'wood')]
            handle=element([2,9,3],[14,11,13],'wood');handle['rotation']={'origin':[2,10,8],'axis':'z','angle':[-22.5,0,22.5][step]};e.append(handle)
            if loaded:e.append(element([3,6,5],[13,7.5,11],'fiber'))
            model(name,e);states.append((f'loaded={str(loaded).lower()},work_step={step}',name))
    variants('flax_breaker',states);js(A/'models/item/flax_breaker.json',{'parent':'slavicmyths:block/flax_breaker_0_0'})
    states=[]
    for loaded in [False,True]:
        for active in [False,True]:
            name=f'spinning_wheel_{int(loaded)}_{int(active)}';e=[element([1,0,2],[15,2,14],'wood'),element([2,2,7],[4,15,9],'wood'),element([12,2,7],[14,22,9],'wood'),element([3,13,6],[14,15,10],'wood'),element([12,18,5],[14,20,11],'wood')]
            if loaded:e.append(element([11,19,6],[15,22,10],'fiber'))
            model(name,e);states.append((f'loaded={str(loaded).lower()},active={str(active).lower()}',name))
    variants('spinning_wheel',states)
    # Inventory includes the wheel; in-world wheel is a small client BER rotor.
    e=list(e[:5])
    for i in range(8):
        angle=i*math.pi/4;x=8+6*math.cos(angle);y=14+6*math.sin(angle)
        rim=element([x-2.5,y-.7,7.2],[x+2.5,y+.7,8.8],'wood');rim['rotation']={'origin':[x,y,8],'axis':'z','angle':[-45,-22.5,0,22.5,45][i%5]};e.append(rim)
    e.extend([element([2,13.6,7.5],[14,14.4,8.5],'wood'),element([7.6,8,7.5],[8.4,20,8.5],'wood')]);model('spinning_wheel_inventory',e);js(A/'models/item/spinning_wheel.json',{'parent':'slavicmyths:block/spinning_wheel_inventory'})
    states=[]
    for active in [False,True]:
        name='loom_table_'+str(int(active));e=[element([1,0,2],[15,2,14],'wood'),element([1,2,7],[3,25,10],'wood'),element([13,2,7],[15,25,10],'wood'),element([2,22,7],[14,24,10],'wood'),element([2,6,7],[14,8,10],'wood')]
        for x in range(4,13):e.append(element([x,7,8],[x+.2,23,8.2],'thread'))
        if active:e.append(element([3,7,7.7],[13,15,8],'cloth'))
        model(name,e);states.append((f'active={str(active).lower()}',name))
    variants('loom_table',states);js(A/'models/item/loom_table.json',{'parent':'slavicmyths:block/loom_table_0'})
    states=[]
    for part in ['head','foot']:
        name='linen_bed_'+part;legZ=1 if part=='head' else 13;e=[element([0,3,0],[16,6,16],'wood'),element([0,6,0],[16,9,16],'cloth'),element([1,0,legZ],[3,3,legZ+2],'wood'),element([13,0,legZ],[15,3,legZ+2],'wood')]
        e.append(element([0,9,0 if part=='foot' else 8],[16,9.5,16],'cover'))
        if part=='head':e.append(element([2,9,1],[14,11,7],'cloth'))
        model(name,e)
        for occupied in [False,True]:states.append((f'part={part},occupied={str(occupied).lower()}',name))
    variants('linen_bed',states)
    inventory=[]
    for part,offset in [('head',0),('foot',16)]:
        data=json.loads((A/f'models/block/linen_bed_{part}.json').read_text(encoding='utf-8'))
        for e in data['elements']:e['from'][2]+=offset;e['to'][2]+=offset;inventory.append(e)
    model('linen_bed_inventory',inventory);js(A/'models/item/linen_bed.json',{'parent':'slavicmyths:block/linen_bed_inventory','display':{'gui':{'rotation':[30,225,0],'scale':[.45,.45,.45]}}})
    states={}
    positions=[[(8,8,7)],[(6,8,6),(10,7,7)],[(8,10,4),(6,8,6),(9,7,7)],[(7,9,4),(10,9,6),(6,6,6),(9,6,7)]]
    for count in range(1,5):
        e=[]
        for n,(x,z,h) in enumerate(positions[count-1]):
            e+=[element([x-1.5,0,z-1.5],[x+1.5,h,z+1.5],'wax'),element([x-.25,h,z-.25],[x+.25,h+1,z+.25],'wick')]
        js(A/f'models/block/tallow_candle_{count}.json',{'parent':'minecraft:block/block','textures':{'wax':'slavicmyths:block/tallow_wax','wick':'slavicmyths:block/tallow_wick','particle':'slavicmyths:block/tallow_wax'},'elements':e})
        for lit in [False,True]:
            for water in [False,True]:states[f'candles={count},lit={str(lit).lower()},waterlogged={str(water).lower()}']={'model':f'slavicmyths:block/tallow_candle_{count}'}
    js(A/'blockstates/tallow_candle.json',{'variants':states});js(A/'models/item/tallow_candle.json',{'parent':'slavicmyths:block/tallow_candle_1'})

def recipes_tags():
    def ingredient(id):return {'tag':id[1:]} if id.startswith('#') else {'item':id if ':' in id else 'slavicmyths:'+id}
    def shaped(name,pattern,key,count=1):js(D/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:ingredient(v) for k,v in key.items()},'result':{'id':'slavicmyths:'+name,'count':count}})
    def shapeless(name,values,count=1):js(D/f'recipe/{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[ingredient(v) for v in values],'result':{'id':'slavicmyths:'+name,'count':count}})
    # Disable historical crafting bypasses while preserving the existing item IDs.
    for name in ['linen_thread','linen_cloth']:
        (D/f'recipe/{name}.json').unlink(missing_ok=True)
        (D/f'advancement/recipes/{name}.json').unlink(missing_ok=True)
    P='#minecraft:planks';C='linen_cloth';red='#c:dyes/red'
    shaped('butchering_knife',[' I','S '],{'I':'minecraft:iron_ingot','S':'minecraft:stick'})
    for name,pattern in [('flax_breaker',['PPP',' S ','PPP']),('spinning_wheel',[' S ','PSP','PPP']),('loom_table',['PSP','S S','PPP'])]:shaped(name,pattern,{'P':P,'S':'minecraft:stick'})
    shaped('linen_bed',['CCC','PPP'],{'C':C,'P':P})
    for name,pattern in [('linen_shirt',['C C','CRC',' C ']),('sarafan',['CRC','CCC','C C']),('linen_headscarf',['CC','CR']),('linen_apron',[' C ','CCC',' R '])]:shaped(name,pattern,{'C':C,'R':red})
    shaped('linen_ports',['CCC','C C','C C'],{'C':C});shaped('bast_shoes',['F F','FTF'],{'F':'flax_fiber','T':'linen_thread'})
    shapeless('folk_vest',[C,C,C,'#c:dyes/brown',red]);shapeless('woven_belt',['linen_thread']*3+[red]);shapeless('tallow_candle',['animal_fat','linen_thread'],2)
    for tag,values in {'textile/flax_materials':['flax_stalk','flax_fiber','linen_thread','linen_cloth'],'textile/linen_clothing':CLOTHES[:-1],'accessories/belts':['woven_belt'],'butchering_knives':['butchering_knife']}.items():js(D/f'tags/item/{tag}.json',{'replace':False,'values':['slavicmyths:'+v for v in values]})
    for namespace,kind,tag,values in [('minecraft','block','mineable/axe',['flax_breaker','spinning_wheel','loom_table','linen_bed']),('minecraft','block','beds',['linen_bed']),('minecraft','item','beds',['linen_bed']),('minecraft','block','candles',['tallow_candle']),('minecraft','item','candles',['tallow_candle']),('minecraft','item','enchantable/durability',['butchering_knife'])]:
        p=R/f'data/{namespace}/tags/{kind}/{tag}.json';data=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'replace':False,'values':[]}
        for name in values:
            v='slavicmyths:'+name
            if v not in data['values']:data['values'].append(v)
        js(p,data)
    for name in ['flax_breaker','spinning_wheel','loom_table','linen_bed','tallow_candle']:
        entry={'type':'minecraft:item','name':'slavicmyths:'+name}
        conditions=[{'condition':'minecraft:survives_explosion'}]
        if name=='linen_bed':conditions.append({'condition':'minecraft:block_state_property','block':'slavicmyths:linen_bed','properties':{'part':'head'}})
        if name=='tallow_candle':
            entry['functions']=[{'function':'minecraft:set_count','count':n,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:tallow_candle','properties':{'candles':str(n)}}]} for n in [2,3,4]]+[{'function':'minecraft:explosion_decay'}];conditions=[]
        js(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry],'conditions':conditions}]})
    js(D/'advancement/linen_craft.json',{'parent':'minecraft:husbandry/root','display':{'icon':{'id':'slavicmyths:linen_cloth'},'title':{'translate':'advancement.slavicmyths.linen_craft.title'},'description':{'translate':'advancement.slavicmyths.linen_craft.description'},'frame':'task','hidden':False,'show_toast':True,'announce_to_chat':True},'criteria':{'cloth':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['slavicmyths:linen_cloth']}]}}}})

def lang():
    for n,code in enumerate(['ru_ru','en_us']):
        p=A/f'lang/{code}.json';v=json.loads(p.read_text(encoding='utf-8'))
        for name,title in names.items():v[('block' if name in ['flax_breaker','spinning_wheel','loom_table','tallow_candle','linen_bed'] else 'item')+'.slavicmyths.'+name]=title[n]
        for key,values in {'entity.slavicmyths.carcass':('Туша','Carcass'),'effect.slavicmyths.well_rested':('Хороший отдых','Well Rested'),'message.slavicmyths.requires_flax':('Нужно 4 стебля льна','Requires 4 flax stalks'),'message.slavicmyths.belt_equipped':('Пояс надет','Belt equipped'),'message.slavicmyths.belt_occupied':('Слот пояса занят','Belt slot is occupied'),'advancement.slavicmyths.linen_craft.title':('От стебля до полотна','From Stalk to Cloth'),'advancement.slavicmyths.linen_craft.description':('Изготовь льняное полотно','Craft a piece of linen cloth'),'advancements.slavicmyths.linen_thread.description':('Сплетите льняную нить из двух волокон на прялке.','Spin two flax fibers into linen thread using a spinning wheel.')}.items():v[key]=values[n]
        js(p,v)
if __name__=='__main__':icons();textures();blocks();recipes_tags();lang();print('1.1.4 textile assets/data generated')
