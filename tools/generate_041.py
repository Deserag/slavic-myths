"""Reproducible small pixel assets and data for the 0.4.1 content pass."""
import json, struct, zlib

NAMES = {
 'silver_ore': ('Серебряная руда','Silver Ore'), 'silver_ingot': ('Серебряный слиток','Silver Ingot'), 'silver_nugget': ('Серебряный самородок','Silver Nugget'),
 'perunite_sword': ('Перунитовый меч','Perunite Sword'), 'perunite_axe': ('Перунитовый топор','Perunite Axe'), 'perunite_pickaxe': ('Перунитовая кирка','Perunite Pickaxe'), 'perunite_shovel': ('Перунитовая лопата','Perunite Shovel'), 'perunite_hoe': ('Перунитовая мотыга','Perunite Hoe'),
 'perunite_helmet': ('Перунитовый шлем','Perunite Helmet'), 'perunite_chestplate': ('Перунитовый нагрудник','Perunite Chestplate'), 'perunite_leggings': ('Перунитовые поножи','Perunite Leggings'), 'perunite_boots': ('Перунитовые сапоги','Perunite Boots'),
 'silver_sword': ('Серебряный меч','Silver Sword'), 'silver_dagger': ('Серебряный кинжал','Silver Dagger'), 'bone_knife': ('Костяной нож','Bone Knife'), 'ritual_knife': ('Обрядовый нож','Ritual Knife'),
 'linen_cloth': ('Льняная ткань','Linen Cloth'), 'herb_pouch': ('Мешочек трав','Herb Pouch'), 'st_johns_wort': ('Зверобой','St. John’s Wort'), 'nettle': ('Крапива','Nettle'), 'fireweed': ('Иван-чай','Fireweed'), 'juniper_berries': ('Ягоды можжевельника','Juniper Berries'), 'ground_wormwood': ('Измельчённая полынь','Ground Wormwood'), 'ground_st_johns_wort': ('Измельчённый зверобой','Ground St. John’s Wort'), 'juniper_blend': ('Можжевеловая смесь','Juniper Blend'),
 'honey_bread': ('Медовый хлеб','Honey Bread'), 'honey_baked_apple': ('Печёное яблоко с мёдом','Honey Baked Apple'), 'ritual_bowl': ('Ритуальная чаша','Ritual Bowl'), 'thunder_axe': ('Громовой топор','Thunder Axe'), 'perun_charm': ('Оберег Перуна','Perun Charm'),
 'carved_oak_pillar': ('Резной дубовый столб','Carved Oak Pillar'), 'carved_birch_planks': ('Резная берёзовая доска','Carved Birch Planks'), 'carved_oak_block': ('Резной дубовый блок','Carved Oak Block'), 'straw_block': ('Соломенный блок','Straw Block')}
PLANTS = {'st_johns_wort','nettle','fireweed','juniper_berries'}
BLOCKS = {'silver_ore','carved_oak_pillar','carved_birch_planks','carved_oak_block','straw_block'} | PLANTS
TOOLS = {'perunite_sword','perunite_axe','perunite_pickaxe','perunite_shovel','perunite_hoe','silver_sword','silver_dagger','bone_knife','ritual_knife','thunder_axe'}
P = {' ': (0,0,0,0), 'k':(45,43,44,255), 's':(98,100,103,255), 'S':(178,187,194,255), 'w':(229,233,220,255), 'b':(105,71,43,255), 'B':(159,111,61,255), 'y':(219,181,79,255), 'g':(52,94,87,255), 'G':(81,161,139,255), 'l':(145,222,185,255), 'r':(155,66,58,255)}

def png(rows):
    width, height = len(rows[0]), len(rows)
    assert all(len(row)==width for row in rows)
    def chunk(kind,data): return struct.pack('!I',len(data))+kind+data+struct.pack('!I',zlib.crc32(kind+data)&0xffffffff)
    raw=b''.join(b'\0'+bytes(ch for p in row for ch in P[p]) for row in rows)
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',width,height,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')

def sprite(name):
    a=[[' ']*16 for _ in range(16)]
    def dot(x,y,c):
        if 0<=x<16 and 0<=y<16: a[y][x]=c
    if name in TOOLS:
        metal='G' if name.startswith('perunite') else ('S' if name.startswith('silver') or name=='ritual_knife' else 'w')
        for i in range(5,14): dot(i,15-i,'b')
        for i in range(1,9): dot(i,i,metal); dot(i+1,i,metal)
        if 'axe' in name:
            for y in range(1,6):
                for x in range(2,7): dot(x,y,metal)
        elif 'pickaxe' in name or 'hoe' in name:
            for x in range(1,10): dot(x,2,metal)
        elif 'shovel' in name: 
            for y in range(1,6):
                for x in range(5,9): dot(x,y,metal)
        if name=='ritual_knife': dot(8,8,'r')
        if name=='thunder_axe':
            for x,y in ((10,1),(9,2),(8,3),(10,3),(9,4),(8,5),(7,6)): dot(x,y,'y')
    elif name.endswith(('helmet','chestplate','leggings','boots')):
        part=name.split('_')[-1]
        for y in range(3,13):
            for x in range(3,13):
                keep=(part=='helmet' and y<9 and (y<6 or x<6 or x>9)) or (part=='chestplate' and (4<=x<=11 or y<7)) or (part=='leggings' and y>=5 and (x<7 or x>8)) or (part=='boots' and y>=8 and (x<7 or x>8))
                if keep: dot(x,y,'G' if (x+y)%4 else 'l')
    elif name in BLOCKS:
        base='s' if name=='silver_ore' else ('y' if name=='straw_block' else ('w' if 'birch' in name else 'B'))
        for y in range(16):
            for x in range(16):
                c=base
                if name=='silver_ore': c='S' if (x*5+y*7)%13<3 else ('k' if (x+y)%7==0 else 's')
                elif name=='straw_block': c='B' if (x+y*3)%9==0 else 'y'
                elif x in (0,15) or y in (0,15) or x==y or x+y==15: c='b'
                elif (x+y)%5==0: c='y'
                dot(x,y,c)
        if name in PLANTS:
            a=[[' ']*16 for _ in range(16)]
            for y in range(4,16):
                dot(8,y,'g')
                if y in (5,8,11):
                    for x in range(5,12): dot(x,y,'G' if name!='fireweed' else 'r')
            if name=='juniper_berries':
                for x,y in ((5,6),(11,6),(6,10),(10,11)): dot(x,y,'S')
    else:
        base='S' if name.startswith('silver') or name=='ritual_bowl' else ('G' if name in ('nettle','st_johns_wort','juniper_berries','ground_wormwood','ground_st_johns_wort','juniper_blend') else ('r' if name in ('honey_baked_apple','fireweed') else 'B'))
        for y in range(4,13):
            for x in range(4,13):
                if (x-8)**2+(y-8)**2<23: dot(x,y,base if (x+y)%5 else ('w' if base=='S' else 'y'))
        if name in ('herb_pouch','linen_cloth'): 
            for x in range(4,13): dot(x,4,'w'); dot(x,12,'w')
        if name=='perun_charm':
            for x,y in ((8,4),(8,5),(7,6),(9,6),(8,7),(8,8),(7,9),(9,9),(8,10),(8,11)): dot(x,y,'y')
    return [''.join(row) for row in a]

def generate(root):
    res=root/'src/main/resources'
    def js(rel,value):
        p=res/rel; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    for lang,i in [('ru_ru',0),('en_us',1)]:
        path=f'assets/slavicmyths/lang/{lang}.json'; data=json.loads((res/path).read_text(encoding='utf-8'))
        for name,pair in NAMES.items(): data[('block' if name in BLOCKS else 'item')+'.slavicmyths.'+name]=pair[i]
        data['ritual.slavicmyths.complete_item'] = ('Обряд завершён: %s' if i==0 else 'Rite complete: %s')
        data['tooltip.slavicmyths.perun_charm'] = ('В левой руке: −30% урона от молнии.' if i==0 else 'Offhand: 30% less lightning damage.')
        js(path,data)
    for name in NAMES:
        folder='block' if name in BLOCKS else 'item'; path=res/f'assets/slavicmyths/textures/{folder}/{name}.png'; path.parent.mkdir(parents=True,exist_ok=True); path.write_bytes(png(sprite(name)))
        js(f'assets/slavicmyths/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:block/'+name}} if name in PLANTS else ({'parent':'slavicmyths:block/'+name} if name in BLOCKS else {'parent':'minecraft:item/handheld' if name in TOOLS else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}}))
        if name in BLOCKS:
            js(f'assets/slavicmyths/models/block/{name}.json',{'parent':'minecraft:block/cross','textures':{'cross':'slavicmyths:block/'+name}} if name in PLANTS else {'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/'+name}})
            js(f'assets/slavicmyths/blockstates/{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}})
            if name!='silver_ore': js(f'data/slavicmyths/loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    js('data/slavicmyths/loot_tables/blocks/silver_ore.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:silver_ore'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    def shaped(name, pattern, key, count=1): js(f'data/slavicmyths/recipes/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{c:{'item':v} for c,v in key.items()},'result':{'item':'slavicmyths:'+name,'count':count}})
    def shapeless(name, inputs, count=1): js(f'data/slavicmyths/recipes/{name}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':v} for v in inputs],'result':{'item':'slavicmyths:'+name,'count':count}})
    per='slavicmyths:perunite'; stick='minecraft:stick'; silver='slavicmyths:silver_ingot'
    for name,pattern in {'sword':['P','P','S'],'axe':['PP','PS',' S'],'pickaxe':['PPP',' S ',' S '],'shovel':['P','S','S'],'hoe':['PP',' S',' S']}.items(): shaped('perunite_'+name,pattern,{'P':per,'S':stick})
    for name,pattern in {'helmet':['PPP','P P'],'chestplate':['P P','PPP','PPP'],'leggings':['PPP','P P','P P'],'boots':['P P','P P']}.items(): shaped('perunite_'+name,pattern,{'P':per})
    shaped('silver_sword',['I','I','S'],{'I':silver,'S':stick}); shaped('silver_dagger',['I','S'],{'I':silver,'S':stick}); shaped('bone_knife',['B','S'],{'B':'minecraft:bone','S':stick})
    shaped('ritual_knife',[' F',' I','S '],{'F':'slavicmyths:idol_fragment','I':silver,'S':stick})
    js('data/slavicmyths/recipes/silver_ingot_from_ore.json',{'type':'minecraft:smelting','ingredient':{'item':'slavicmyths:silver_ore'},'result':'slavicmyths:silver_ingot','experience':0.7,'cookingtime':200})
    shaped('silver_ingot',['NNN','NNN','NNN'],{'N':'slavicmyths:silver_nugget'}); shapeless('silver_nugget',[silver],9)
    shaped('linen_cloth',['TT','TT'],{'T':'slavicmyths:linen_thread'})
    shaped('herb_pouch',['CWJ','NCN'],{'C':'slavicmyths:linen_cloth','W':'slavicmyths:ground_st_johns_wort','N':'slavicmyths:nettle','J':'slavicmyths:juniper_blend'})
    shapeless('ground_wormwood',['slavicmyths:wormwood','minecraft:flint']); shapeless('ground_st_johns_wort',['slavicmyths:st_johns_wort','minecraft:flint']); shapeless('juniper_blend',['slavicmyths:juniper_berries','slavicmyths:ground_wormwood'])
    shapeless('honey_bread',['minecraft:bread','minecraft:honey_bottle']); shapeless('honey_baked_apple',['minecraft:apple','minecraft:honey_bottle'])
    shaped('ritual_bowl',['S S',' S '],{'S':silver})
    shaped('carved_oak_pillar',['O','O'],{'O':'minecraft:oak_log'},2); shaped('carved_birch_planks',['B','B'],{'B':'minecraft:birch_planks'},2)
    shaped('carved_oak_block',['OO','OO'],{'O':'minecraft:oak_planks'},4); shaped('straw_block',['FFF','FFF','FFF'],{'F':'slavicmyths:flax'})
    loot_path='data/slavicmyths/loot_tables/chests/ancient_shrine.json'; loot=json.loads((res/loot_path).read_text(encoding='utf-8'))
    loot['pools'][0]['entries'] += [{'type':'minecraft:item','name':'slavicmyths:'+n,'weight':w} for n,w in [('silver_ingot',3),('st_johns_wort',5),('nettle',4),('juniper_berries',4),('ritual_bowl',1)]]; js(loot_path,loot)
    # Armor layers use the same original palette, with a different pattern for legs.
    for layer in (1,2):
        rows=[]
        for y in range(32):
            row=''
            for x in range(64): row += ('G' if (x+y)%5 else 'l') if (x//8+y//8)%3!=0 else 'g'
            rows.append(row)
        path=res/f'assets/slavicmyths/textures/models/armor/perunite_layer_{layer}.png'; path.parent.mkdir(parents=True,exist_ok=True); path.write_bytes(png(rows))

if __name__=='__main__':
    from pathlib import Path
    generate(Path(__file__).resolve().parents[1])
