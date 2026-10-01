"""Final 0.8.0 resource layer: native cuboid models, authored pixels and camp NBT."""
from pathlib import Path
import json, math, struct, zlib, sys
import swamp_073 as blue
R = Path(__file__).resolve().parents[1]
RES = R/'src/main/resources'
A = RES/'assets/slavicmyths'
D = RES/'data/slavicmyths'


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')


def png(path, width, height, pixel):
    def chunk(kind, data): return struct.pack('>I', len(data))+kind+data+struct.pack('>I', zlib.crc32(kind+data)&0xffffffff)
    raw=b''.join(b'\0'+bytes(c for x in range(width) for c in pixel(x,y)) for y in range(height))
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',width,height,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))


def cube(a,b,texture,rotation=None):
    obj={'from':a,'to':b,'faces':{side:{'texture':'#'+texture,'uv':[0,0,16,16]} for side in ['north','south','east','west','up','down']}}
    if rotation: obj['rotation']={'origin':rotation[0],'axis':rotation[1],'angle':rotation[2]}
    return obj


def model(name,elements,block=False):
    textures={'wood':'minecraft:block/dark_oak_log','grip':'minecraft:block/brown_terracotta','stone':'minecraft:block/stone','metal':'minecraft:block/iron_block','dark':'minecraft:block/gray_concrete','silver':'minecraft:block/light_gray_terracotta','cloth':'minecraft:block/brown_wool','particle':'minecraft:block/dark_oak_planks'}
    obj={'textures':textures,'elements':elements,'ambientocclusion':True}
    if not block:
        obj['display']={'gui':{'rotation':[20,135,-15],'scale':[.7]*3},'ground':{'scale':[.45]*3},'fixed':{'rotation':[0,180,0],'scale':[.7]*3},'thirdperson_righthand':{'rotation':[0,0,-8],'translation':[0,1,0],'scale':[.9]*3},'thirdperson_lefthand':{'rotation':[0,0,8],'translation':[0,1,0],'scale':[.9]*3},'firstperson_righthand':{'rotation':[0,-90,15],'translation':[1,1,0],'scale':[.75]*3},'firstperson_lefthand':{'rotation':[0,90,-15],'translation':[1,1,0],'scale':[.75]*3}}
    write(A/f'models/{"block" if block else "item"}/{name}.json',obj)


def ring(x,y,z,texture='metal',rotate=False):
    pieces=[cube([x,y,z],[x+3,y+.6,z+.6],texture),cube([x,y+2.4,z],[x+3,y+3,z+.6],texture),cube([x,y+.6,z],[x+.6,y+2.4,z+.6],texture),cube([x+2.4,y+.6,z],[x+3,y+2.4,z+.6],texture)]
    if rotate:
        for p in pieces:p['rotation']={'origin':[x+1.5,y+1.5,z+.3],'axis':'y','angle':45}
    return pieces


def gear_models():
    for name in ['wooden_mace','stone_mace','mace','silver_mace']:
        el=[cube([7.1,0,7.1],[8.9,12.5,8.9],'wood')]
        for y in range(1,7,2):el.append(cube([6.9,y,6.9],[9.1,y+.7,9.1],'grip'))
        if name=='wooden_mace':
            el += [cube([5.4,10,5.4],[10.6,17,10.6],'wood'),cube([6,16,6],[10,18,10],'wood')]
            for x in (4.6,10.6):el.append(cube([x,12,6.5],[x+.8,15,9.5],'wood'))
        elif name=='stone_mace':
            el += [cube([4.2,11,5],[11.8,16.5,11],'stone'),cube([5.5,10.5,6],[10.5,17,10],'stone')]
            for x in (6,9):el.append(cube([x,10.3,4.8],[x+.7,17.2,11.2],'grip'))
        elif name=='mace':
            el += [cube([6.2,11,6.2],[9.8,18,9.8],'dark')]
            for angle in (0,45,-45):
                el.append(cube([4.5,12,7.5],[11.5,17.5,8.5],'metal',([8,14,8],'y',angle)))
            el.append(cube([7.4,17.5,7.4],[8.6,19,8.6],'metal'))
        else:
            el += [cube([5.5,12,5.5],[10.5,16.5,10.5],'silver',([8,14,8],'y',45)),cube([6.5,16.5,6.5],[9.5,18.5,9.5],'silver'),cube([6,11,6],[10,12,10],'dark')]
            for y in (13,15):el.append(cube([6.6,y,4.35],[9.4,y+.4,4.7],'dark'))
        model(name,el)
    el=[cube([5.3,0,7.3],[6.7,10,8.7],'wood')]
    for y in (1,3,5):el.append(cube([5.1,y,7.1],[6.9,y+.7,8.9],'grip'))
    el+=ring(4.5,9,7.7)+ring(5.7,11.2,7.7,rotate=True)+ring(7.2,13.1,7.7)+ring(9.1,12.3,7.7,rotate=True)
    el += [cube([10,8,6],[14,12,10],'dark'),cube([10.8,7.3,6.8],[13.2,12.7,9.2],'metal'),cube([9.3,9.2,7.2],[14.7,10.8,8.8],'metal')]
    model('flail',el)
    model('iron_rings',ring(3,7,7)+ring(5.2,5.3,7.2,rotate=True)+ring(7.7,7,7)+ring(10,5.2,7.2,rotate=True))
    for name in ['gambeson','plate_cuirass']:
        el=[cube([4,3,6],[12,13,10],'cloth'),cube([1.5,7,6],[4,13,10],'cloth'),cube([12,7,6],[14.5,13,10],'cloth'),cube([4,4,5.7],[12,5.2,10.3],'grip'),cube([5,12,5.7],[11,14,10.3],'cloth')]
        if name=='gambeson':
            for x in range(5,12,2):el.append(cube([x,5.5,5.75],[x+.4,12,6],'grip'))
        else:
            for y in (6,8.5,11):
                for x in (4.3,7,9.7):
                    el.append(cube([x,y,5.1],[x+2.3,y+2.8,6],'metal'))
                    el.append(cube([x+.2,y+.2,4.9],[x+.55,y+.55,5.2],'dark'))
            el.extend([cube([3,11,5],[5,14,11],'dark'),cube([11,11,5],[13,14,11],'dark')])
        model(name,el)
    el=[cube([0,11,0],[16,13,16],'wood'),cube([1,2,1],[3,11,3],'wood'),cube([13,2,1],[15,11,3],'wood'),cube([1,2,13],[3,11,15],'wood'),cube([13,2,13],[15,11,15],'wood'),cube([2,3,2],[14,4,14],'wood'),cube([3,5,2],[13,9,6],'wood'),cube([6,6,1.7],[10,7,2],'metal'),cube([1,11,1],[7,13.3,6],'dark'),cube([3,13.3,2],[5,15,5],'metal'),cube([8,13,3],[9,13.6,10],'wood'),cube([7,13,3],[11,14.3,5],'metal'),cube([11,13,7],[11.6,13.5,12],'dark'),cube([13,13,7],[13.6,13.5,12],'dark')]
    for x in (0,14):el.append(cube([x,10.5,0],[x+2,13.2,.6],'metal'))
    el += [cube([4,13.1,10],[6,13.4,10.5],'metal'),cube([4,13.1,12],[6,13.4,12.5],'metal'),cube([4,13.1,10.5],[4.5,13.4,12],'metal'),cube([5.5,13.1,10.5],[6,13.4,12],'metal')]
    model('armorer_table',el,True)
    write(A/'models/item/armorer_table.json',{'parent':'slavicmyths:block/armorer_table'})
    write(A/'blockstates/armorer_table.json',{'variants':{'':{'model':'slavicmyths:block/armorer_table'}}})
    write(D/'loot_tables/blocks/armorer_table.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:armorer_table'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    for name in ['gambeson','plate']:
        def armor(x,y):
            if name=='gambeson':
                c=(119,100,69) if (x+y)%7 and (x-y)%7 else (68,57,39)
                if y in (28,29):c=(53,37,24)
            else:
                c=(91,94,87) if y%5<4 else (45,39,30)
                if x%5==0:c=(52,49,40)
                if x%5==1 and y%5==1:c=(163,163,145)
            return (*c,255)
        png(A/f'textures/models/armor/{name}_layer_1.png',64,32,armor)


def human_textures():
    palettes=[(102,77,52),(64,80,58),(73,77,75),(86,66,48),(100,55,43)]
    for role in range(5):
        for face in range(4):
            def pixel(x,y):
                cloth=palettes[role];n=((x*3+y*5)%5)-2;c=tuple(v+n for v in cloth)
                # Leg islands: patched trousers and boots, not shirt recolors.
                if x<16 and 16<=y<32:c=(40,43,40) if y<27 else (43,30,23)
                if 16<=x<40 and 16<=y<32:
                    if y in (27,28):c=(42,28,18)
                    elif role==2 or role==4:
                        c=(111,114,106) if (y-18)%4<3 else (48,44,36)
                        if x%4==1 and y%4==1:c=(173,168,145)
                    elif role==3:
                        c=(118,120,110) if (x+y)%3==0 else (59,61,55)
                    elif role==0 and (x+y)%5==0:c=(67,52,34)
                    elif role==1 and x in (21,22):c=(72,44,25)
                # Bare hands in the arm texture.
                if 40<=x<56 and 28<=y<32:c=(163+face*4,126+face*3,94+face*2)
                # Distinct repaired patches on the garment.
                if role==0 and 28<=x<=31 and 23<=y<=25:c=(125,103,75) if (x+y)%2 else (72,55,37)
                if role==0 and face==1 and 20<=x<24 and 20<=y<26:c=(59,67,50) if x%2 else (80,79,56)
                if role==0 and face==2 and 44<=x<48 and 21<=y<28:c=(100,87,64) if y%3 else (56,41,29)
                if role==0 and face==3 and 21<=x<25 and 22<=y<27:c=(92,49,39) if (x+y)%4 else (131,95,67)
                if role==1 and 47<=x<=50 and 23<=y<=25:c=(88,79,55)
                # Head texture and front face. Eye whites are muted, ordinary human eyes.
                if y<16 and x<32:
                    hair=[(42,30,23),(89,65,40),(32,26,21),(58,42,29)][face];c=hair
                    if 8<=x<16 and 9<=y<16:
                        c=(166+face*4,130+face*3,99+face*2)
                        if y==11 and x in (9,10,13,14):c=(186,182,165) if x in (9,14) else (45,42,35)
                        if y==10 and x in (9,10,13,14):c=hair
                        if y==13 and x in (11,12):c=(131,91,70)
                        if (face==0 and y>=14) or (face==2 and (y>=13 or x in (8,15))):c=hair
                        if face==1 and x==14 and y in (12,13,14):c=(121,86,70)
                        if face==3 and y==15 and x in (11,12):c=(121,84,67)
                # Hood / fur cap UV, bag, collar and beard islands.
                if y>=32:
                    c=(58,66,48) if role==1 else (66,49,33)
                    if y>=48 and x<32:c=(98,84,65) if (x+y)%3 else (52,44,33)
                    if y>=48 and x>=48:c=[(42,30,23),(89,65,40),(32,26,21),(58,42,29)][face]
                    if 40<=x<52 and 32<=y<46:c=(89,59,33) if x%3 else (47,34,22)
                return (*c,255)
            png(A/f'textures/entity/bandit_{role}_{face}.png',64,64,pixel)
    for name in ['bandit_fighter','bandit_archer','bandit_heavy','bandit_senior','ataman']:
        write(A/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})


def recipe(name,pattern,key,result,count=1):
    write(D/f'recipes/{name}.json',{'type':'slavicmyths:armorer_shaped','pattern':pattern,'key':{k:{'item':v if ':' in v else 'slavicmyths:'+v} for k,v in key.items()},'result':{'item':result if ':' in result else 'slavicmyths:'+result,'count':count}})


def recipes():
    write(D/'recipes/armorer_table.json',{'type':'minecraft:crafting_shaped','pattern':['III','PCP','P P'],'key':{'I':{'item':'minecraft:iron_ingot'},'P':{'item':'minecraft:spruce_planks'},'C':{'item':'minecraft:crafting_table'}},'result':{'item':'slavicmyths:armorer_table'}})
    write(D/'recipes/iron_rings.json',{'type':'slavicmyths:armorer_shapeless','ingredients':[{'item':'minecraft:iron_ingot'}],'result':{'item':'slavicmyths:iron_rings','count':2}})
    recipe('wooden_mace',[' WW ',' WWW',' LS ','  S '],{'W':'minecraft:oak_planks','L':'minecraft:leather','S':'minecraft:stick'},'wooden_mace')
    recipe('stone_mace',[' CC ','LCCL',' SS ',' S  '],{'C':'minecraft:cobblestone','L':'minecraft:leather','S':'minecraft:stick'},'stone_mace')
    recipe('mace',[' I  ','IRI ',' LS ','  S '],{'I':'minecraft:iron_ingot','R':'iron_rings','L':'minecraft:leather','S':'minecraft:stick'},'mace')
    recipe('silver_mace',[' SS ','SRS ',' LL ','  T '],{'S':'silver_ingot','R':'iron_rings','L':'minecraft:leather','T':'minecraft:stick'},'silver_mace')
    recipe('flail',['  II',' RRI',' SR ',' LS '],{'I':'minecraft:iron_ingot','R':'iron_rings','S':'minecraft:stick','L':'minecraft:leather'},'flail')
    for part,pattern in {'helmet':[' RR ','R  R'],'chestplate':['R  R','RRRR','RRRR',' RR '],'leggings':['RRRR','R  R','R  R','R  R'],'boots':['R  R','R  R']}.items():recipe('armorer_chainmail_'+part,pattern,{'R':'iron_rings'},'minecraft:chainmail_'+part)
    recipe('gambeson',['W  W','WLLW','WWWW',' SS '],{'W':'minecraft:white_wool','L':'minecraft:leather','S':'minecraft:string'},'gambeson')
    recipe('plate_cuirass',['L  L','IIGI','IRRI',' LL '],{'L':'minecraft:leather','I':'minecraft:iron_ingot','G':'gambeson','R':'iron_rings'},'plate_cuirass')
    tags=D/'tags/items/rune_heavy.json';data=json.loads(tags.read_text(encoding='utf-8'))
    data['values']=list(dict.fromkeys(data['values']+['slavicmyths:'+n for n in ['wooden_mace','stone_mace','mace','silver_mace','flail']]))
    write(tags,data)


class Camp(blue.Blueprint):
    def marker(self,x,y,z,text):
        self.put(x,y,z,'structure_block',mode='data');state,_=self.blocks[x,y,z]
        self.blocks[x,y,z]=(state,{'id':'minecraft:structure_block','mode':'DATA','metadata':text})
    def loot(self,x,y,z,category=''):
        self.marker(x,y,z,'loot:camp')
    def spawn(self,x,y,z,role,local):
        self.marker(x,y,z,f'spawn:{role}:{local}')


def tent(name):
    senior=name in ('senior','leader');w,l=(9,9) if senior else (7,7)
    b=Camp(name,(w,8,l));b.box((1,0,1),(w-2,0,l-2),'spruce_planks');b.box((1,1,1),(w-2,5,l-2),'air')
    for x in (1,w-2):
        for z in (1,l-2):
            for y in range(3):blue.log(b,x,y,z)
    # Low canvas A-frame; steep enough to read as tent, no wooden room walls.
    for z in range(l):
        for x in range(w):
            y=1+min(x,w-1-x)
            b.put(x,y,z,'brown_wool' if name=='storage_tent' else 'gray_wool' if senior else 'light_gray_wool')
            if z==0 and x not in (w//2-1,w//2,w//2+1):
                for yy in range(1,y):b.put(x,yy,z,'brown_wool' if name=='storage_tent' else 'gray_wool' if senior else 'light_gray_wool')
    for z in range(1,l-1):blue.log(b,w//2,4 if senior else 3,z,'z')
    if name=='storage_tent':
        b.loot(2,1,2);b.put(4,1,2,'barrel',facing='up',open=False);b.put(2,1,4,'hay_block',axis='y');b.spawn(3,1,4,'archer',0)
    else:
        for x in (2,w-3):
            b.put(x,1,2,'red_bed' if senior else 'gray_bed',facing='south',part='foot',occupied=False)
            b.put(x,1,3,'red_bed' if senior else 'gray_bed',facing='south',part='head',occupied=False)
        b.loot(2,1,l-3)
        if name=='leader':
            b.put(w-3,1,l-3,'slavicmyths:armorer_table');b.spawn(w//2,1,4,'ataman',0)
        elif senior:
            b.put(w-3,1,l-3,'cartography_table');b.spawn(4,1,4,'senior',0);b.spawn(4,1,6,'heavy',1)
        else:
            b.spawn(3,1,3,'fighter',0);b.spawn(3,1,5,'fighter',1)
    return b


def warehouse():
    b=Camp('warehouse',(9,9,7));b.box((1,0,1),(7,0,5),'cobblestone');b.box((1,1,1),(7,5,5),'air')
    for x in range(1,8):
        for z in (1,5):b.box((x,1,z),(x,3,z),'spruce_planks')
    for x in (1,7):
        for z in range(1,6):b.box((x,1,z),(x,3,z),'spruce_planks')
        for z in (1,5):
            for y in range(4):blue.log(b,x,y,z)
    b.box((4,1,5),(4,2,5),'air');blue.roof(b,1,1,5,7,4)
    for x in (2,3,6):b.loot(x,1,2)
    b.put(2,1,4,'crafting_table');b.put(6,1,4,'hay_block',axis='y')
    return b


def watch():
    b=Camp('watch',(7,9,7));b.box((2,1,2),(4,7,4),'air')
    for x in (1,5):
        for z in (1,5):
            for y in range(7):blue.log(b,x,y,z)
    b.box((1,4,1),(5,4,5),'spruce_planks');b.box((0,7,0),(6,7,6),'spruce_slab',type='bottom',waterlogged=False)
    for y in range(1,5):b.put(1,y,2,'ladder',facing='south',waterlogged=False)
    for x in range(2,5):
        for z in (1,5):b.put(x,5,z,'spruce_fence',east=True,west=True)
    b.spawn(3,5,3,'archer',0)
    return b


def canopy():
    b=Camp('canopy',(7,6,6));b.box((1,0,1),(5,0,4),'spruce_planks');b.box((1,1,1),(5,3,4),'air')
    for x in (1,5):
        for z in (1,4):
            for y in range(4):blue.log(b,x,y,z)
    b.box((0,4,0),(6,4,5),'spruce_slab',type='bottom',waterlogged=False);b.put(2,1,2,'crafting_table');b.loot(4,1,2)
    return b


def yard(medium):
    size=(43,6,38) if medium else (25,5,25);b=Camp('yard_medium' if medium else 'yard_small',size);cx,cz=(21,19) if medium else (12,11)
    for x in range(cx-3,cx+4):
        for z in range(cz-3,cz+4):
            if abs(x-cx)+abs(z-cz)<=5:b.put(x,0,z,'coarse_dirt')
    b.put(cx,1,cz,'campfire',lit=True,signal_fire=False,waterlogged=False,facing='north')
    for x in range(cx-2,cx+2):blue.log(b,x,1,cz-3,'x')
    for z in range(cz,cz+3):blue.log(b,cx+3,1,z,'z')
    # Offset fragments, leaving broad entrances and natural edges.
    if medium:
        for x,z,length in [(1,1,11),(31,2,9),(1,35,12),(33,35,8)]:
            for i in range(length):
                for y in range(3+(i%3==0)):blue.log(b,x+i,y,z)
        for x,z in [(12,32),(13,32),(29,21),(30,21)]:b.put(x,1,z,'hay_block',axis='y')
    return b


def camps():
    original=blue.OUT;blue.OUT=D/'structures/bandit'
    cart=blue.poi(1);cart.name='cart'
    for state,nbt in cart.blocks.values():
        if nbt and nbt.get('metadata','').startswith('loot:'):nbt['metadata']='loot:camp'
    try:write(R/'docs/verification/camps-0.8.0.json',{b.name:b.save() for b in [tent(n) for n in ['sleeping','storage_tent','senior','leader']]+[warehouse(),watch(),canopy(),yard(False),yard(True),cart]})
    finally:blue.OUT=original


def loot():
    basic=[('minecraft:bread',12,1,3),('minecraft:cooked_porkchop',7,1,2),('minecraft:arrow',12,3,9),('minecraft:leather',8,1,3),('minecraft:string',8,2,5),('minecraft:iron_nugget',10,2,6),('minecraft:iron_ingot',2,1,2),('slavicmyths:wormwood',4,1,2),('minecraft:glass_bottle',4,1,2),('slavicmyths:ancient_coin',2,1,2)]
    for size in ['small','medium']:
        pools=[{'rolls':{'min':3,'max':5},'entries':[{'type':'minecraft:item','name':name,'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'min':lo,'max':hi}}]} for name,weight,lo,hi in basic]}]
        if size=='medium':pools.append({'rolls':1,'entries':[{'type':'minecraft:empty','weight':16}]+[{'type':'minecraft:item','name':'slavicmyths:'+name,'weight':1} for name in ['silver_ingot','iron_rings','mace','gambeson','plate_cuirass','amber']]})
        write(D/f'loot_tables/chests/bandit_{size}.json',{'type':'minecraft:chest','pools':pools})
    for name in ['bandit_fighter','bandit_archer','bandit_heavy','bandit_senior','ataman']:
        entries=[{'type':'minecraft:empty','weight':6},{'type':'minecraft:item','name':'minecraft:bread','weight':2},{'type':'minecraft:item','name':'slavicmyths:ancient_coin','weight':1}]
        if name=='bandit_archer':entries.append({'type':'minecraft:item','name':'minecraft:arrow','weight':3,'functions':[{'function':'minecraft:set_count','count':{'min':1,'max':3}}]})
        if name=='ataman':entries.append({'type':'minecraft:item','name':'slavicmyths:iron_rings','weight':3})
        write(D/f'loot_tables/entities/{name}.json',{'type':'minecraft:entity','pools':[{'rolls':2 if name=='ataman' else 1,'entries':entries}]})


def advancements():
    write(D/'advancements/learn_armorer.json',{'parent':'slavicmyths:root','criteria':{'table':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'slavicmyths:armorer_table'}]}}}})
    write(D/'advancements/recipes/armorer_table.json',{'parent':'minecraft:recipes/root','criteria':{'iron':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'minecraft:iron_ingot'}]}}},'rewards':{'recipes':['slavicmyths:armorer_table']}})
    for name,icon in [('bad_people','minecraft:campfire'),('stolen_goods','minecraft:barrel'),('disperse_bandits','minecraft:iron_sword'),('without_ataman','slavicmyths:mace'),('armorer_craft','slavicmyths:armorer_table'),('find_small_camp','minecraft:white_wool'),('find_medium_camp','minecraft:spruce_log')]:
        criteria={'done':{'trigger':'minecraft:impossible'}}
        if name in ('bad_people','find_small_camp','find_medium_camp'):
            sizes=['small','medium'] if name=='bad_people' else ['small' if name=='find_small_camp' else 'medium']
            criteria={size:{'trigger':'minecraft:location','conditions':{'location':{'feature':'slavicmyths:bandit_camp_'+size,'dimension':'minecraft:overworld'}}} for size in sizes}
        obj={'parent':'slavicmyths:root' if name=='bad_people' else 'slavicmyths:bad_people','criteria':criteria,'requirements':[list(criteria)]}
        if name not in ('find_small_camp','find_medium_camp'):obj['display']={'icon':{'item':icon},'title':{'translate':f'advancement.slavicmyths.{name}.title'},'description':{'translate':f'advancement.slavicmyths.{name}.description'},'frame':'challenge' if name=='disperse_bandits' else 'task','show_toast':True,'announce_to_chat':True,'hidden':False}
        write(D/f'advancements/{name}.json',obj)


def language():
    names={'armorer_table':('Оружейный стол','Armorer table'),'iron_rings':('Железные кольца','Iron rings'),'wooden_mace':('Деревянная булава','Wooden mace'),'stone_mace':('Каменная булава','Stone mace'),'mace':('Железная булава','Iron mace'),'silver_mace':('Серебряная булава','Silver mace'),'flail':('Кистень','Flail'),'gambeson':('Стёганка','Gambeson'),'plate_cuirass':('Пластинчатая кираса','Lamellar cuirass')}
    entities={'bandit_fighter':('Разбойник-боец','Bandit fighter'),'bandit_archer':('Разбойник-лучник','Bandit archer'),'bandit_heavy':('Тяжёлый разбойник','Heavy bandit'),'bandit_senior':('Старший разбойник','Senior bandit'),'ataman':('Атаман','Ataman')}
    advances={'bad_people':('Недобрые люди','Bad people','Найти разбойничий лагерь','Find a bandit camp'),'stolen_goods':('Чужое добро','Stolen goods','Открыть лагерные припасы','Open camp supplies'),'disperse_bandits':('Разогнать шайку','Disperse the gang','Победить весь постоянный состав среднего лагеря','Defeat the entire fixed roster of a medium camp'),'without_ataman':('Без атамана','Without an ataman','Победить Атамана','Defeat an Ataman'),'armorer_craft':('Оружейное дело','Arms craft','Создать предмет на Оружейном столе','Craft at the armorer table')}
    books={
      'bandit_gangs':('Разбойничьи шайки','Bandit gangs','Разбойники — люди, а не нечисть. Бойцы делают замах и восстанавливаются после удара. Лучники держат расстояние, тяжёлые ненадолго поднимают щит. Их лагеря встречаются в равнинах, лесах и тайге. Образы — игровая интерпретация.','Bandits are humans, not spirits. Fighters telegraph strikes and recover. Archers keep their distance; heavies briefly raise shields. Look for camps in plains, forests and taiga. These characters are game interpretations.'),
      'bandit_small':('Малый лагерь','Small camp','Несколько низких шатров, костёр и припасы выдают временную стоянку. Старший отличается экипировкой, но не является боссом. Основная добыча находится в лагере.','Low canvas tents, a fire and supplies mark a temporary camp. The senior wears better equipment but is not a boss. Most rewards are stored in camp.'),
      'bandit_medium':('Укреплённый лагерь','Fortified camp','Склад, сторожевая площадка и фрагменты частокола защищают более крупную шайку. Здесь один Атаман и девять постоянных защитников вместе с ним. Для зачистки нужно победить весь состав; выгрузка чанка не создаёт новых защитников.','A warehouse, lookout and palisade fragments protect a larger gang. One Ataman leads a fixed roster of nine including himself. Defeat every member to clear it; chunk unloading does not replenish defenders.'),
      'ataman':('Атаман','Ataman','Человек в хорошей защите. Следи за длинным замахом тяжёлого удара и короткими фазами щита. Его команды помогают ближайшим подчинённым удерживать цель. После его смерти организационная помощь исчезает.','A human in good armor. Watch for the long heavy-strike wind-up and short shield phases. His commands help nearby bandits maintain their target. His death ends that assistance.'),
      'arms_craft':('Оружейное дело','Arms craft','Оружейный стол создаётся из трёх слитков железа, четырёх еловых досок и верстака. Его сетка 4×4 использует отдельные рецепты; JEI, если установлен, показывает их. Один слиток даёт два комплекта колец. Кольца складывают в форму кольчуги. Булавы медленнее мечей, отбрасывают и сильнее давят на броню. Кистень: удерживай ПКМ 0,9 секунды и отпусти для удара до четырёх блоков; затем две секунды восстановления. Стёганка даёт 4 защиты, кираса — 7 и замедляет на 5%.','Craft the armorer table from three iron ingots, four spruce planks and a crafting table. Its 4×4 grid uses separate recipes, shown by optional JEI. One ingot makes two sets of rings. Arrange rings as chainmail armor. Maces are slower than swords, knock targets back and pressure armor. Hold flail use for 0.9 seconds, then release for a strike up to four blocks, followed by a two-second cooldown. Gambeson provides 4 armor; the cuirass provides 7 with a 5% movement penalty.')}
    for j,lang in enumerate(['ru_ru','en_us']):
        p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
        for id,pair in names.items():data[('block' if id=='armorer_table' else 'item')+'.slavicmyths.'+id]=pair[j]
        for id,pair in entities.items():data['entity.slavicmyths.'+id]=pair[j];data['item.slavicmyths.'+id+'_spawn_egg']=pair[j]+(' — яйцо призыва' if j==0 else ' spawn egg')
        for id,entry in advances.items():data[f'advancement.slavicmyths.{id}.title']=entry[j];data[f'advancement.slavicmyths.{id}.description']=entry[j+2]
        for id,entry in books.items():data[f'book.slavicmyths.{id}.title']=entry[j];data[f'book.slavicmyths.{id}.text']=entry[j+2]
        data['bandit.command.no_safe']=('Координаты найдены, но безопасная точка телепортации отсутствует.','Coordinates found, but no safe teleport landing is available.')[j]
        for key,pair in {'idle':('Разбойник вздыхает','Bandit sighs'),'alert':('Разбойник окликает','Bandit alerts'),'attack':('Разбойник атакует','Bandit attacks'),'hurt':('Разбойник ранен','Bandit hurt'),'death':('Разбойник падает','Bandit falls'),'command':('Атаман командует','Ataman commands'),'heavy':('Атаман замахивается','Ataman winds up')}.items():data['subtitles.slavicmyths.bandit_'+key]=pair[j]
        write(p,data)


def sounds():
    # Original short, voiced formant breaths/grunts. No samples from another game or mod.
    sys.path.insert(0,str(R/'.tools/audio-libs'));import numpy as np;import soundfile as sf
    events=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
    for i,name in enumerate(['idle','alert','attack','hurt','death','command','heavy']):
        rate=22050;duration=[.65,.5,.3,.35,.8,.75,.45][i];t=np.arange(int(rate*duration))/rate;f0=125+12*np.sin(t*7)+i*3
        phase=2*np.pi*np.cumsum(f0)/rate;wave=np.zeros(len(t));formants=[(650,110),(1200,150),(2450,230)]
        for harmonic in range(1,29):
            freq=(125+i*3)*harmonic;gain=sum(math.exp(-.5*((freq-center)/spread)**2) for center,spread in formants)/harmonic
            wave+=gain*np.sin(phase*harmonic)
        noise=np.random.default_rng(800+i).normal(0,1,len(t));noise=np.convolve(noise,np.ones(5)/5,mode='same')
        envelope=np.sin(np.pi*t/duration)**1.1
        if name in ('alert','command'):envelope*=.3+.7*np.sin(np.pi*t/duration*2)**2
        wave=(wave+.018*noise)*envelope;wave*=.55/(np.max(np.abs(wave))+1e-9)
        p=A/f'sounds/bandit/{name}.ogg';p.parent.mkdir(parents=True,exist_ok=True);sf.write(str(p),wave,rate,format='OGG',subtype='VORBIS')
        # libsndfile chooses a random Ogg stream serial; normalize it and page CRCs so
        # regeneration reproduces the exact resource bytes as well as the PCM sound.
        data=bytearray(p.read_bytes());offset=0
        while offset<len(data):
            assert data[offset:offset+4]==b'OggS'
            segments=data[offset+26];length=27+segments+sum(data[offset+27:offset+27+segments])
            data[offset+14:offset+18]=struct.pack('<I',808000+i);data[offset+22:offset+26]=b'\0'*4
            crc=0
            for byte in data[offset:offset+length]:
                crc^=byte<<24
                for _ in range(8):crc=((crc<<1)^0x04c11db7 if crc&0x80000000 else crc<<1)&0xffffffff
            data[offset+22:offset+26]=struct.pack('<I',crc);offset+=length
        p.write_bytes(data)
        events['bandit_'+name]={'subtitle':'subtitles.slavicmyths.bandit_'+name,'sounds':[{'name':'slavicmyths:bandit/'+name}]}
    write(A/'sounds.json',events)


def generate():
    gear_models();human_textures();recipes();camps();loot();advancements();language();sounds()


if __name__=='__main__':generate()
