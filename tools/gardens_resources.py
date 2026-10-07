"""Scoped 1.1.2 data and authored 32px assets. Vanilla geometry/schemas, independently drawn pixels."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,zipfile,math
R=Path('src/main/resources');A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
V=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');VN=set(V.namelist())
B=['raspberry','blueberry','blackcurrant','lingonberry','cranberry']
WOOD=['apple_log','stripped_apple_log','apple_wood','stripped_apple_wood','apple_planks','apple_stairs','apple_slab','apple_fence','apple_fence_gate','apple_door','apple_trapdoor','apple_pressure_plate','apple_button','apple_sign','apple_wall_sign','apple_hanging_sign','apple_wall_hanging_sign','apple_leaves','apple_sapling']
def js(p,obj):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(im,folder,name):p=A/f'textures/{folder}/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def blank():im=Image.new('RGBA',(32,32));return im,ImageDraw.Draw(im)
def leaf(d,x,y,kind,scale=1):
    green=['#54853b','#456d40','#5b823a','#355c30','#547344'][kind]
    if kind==0:d.polygon([(x-4*scale,y),(x,y-3*scale),(x+4*scale,y-1),(x+scale,y+3*scale)],fill=green)
    elif kind==1:d.ellipse((x-3*scale,y-2*scale,x+3*scale,y+2*scale),fill=green)
    elif kind==2:d.polygon([(x-4*scale,y),(x-3*scale,y-3*scale),(x-scale,y-2*scale),(x,y-4*scale),(x+3*scale,y-3*scale),(x+4*scale,y),(x+scale,y+3*scale)],fill=green)
    elif kind==3:d.ellipse((x-4*scale,y-2*scale,x+4*scale,y+2*scale),fill=green)
    else:d.polygon([(x-3*scale,y),(x+2*scale,y-2*scale),(x+3*scale,y),(x-scale,y+scale)],fill=green)
    d.line((x-2*scale,y,x+2*scale,y),fill='#8aad55')
def berry(d,x,y,kind,unripe=False,size=3):
    colors=['#b52e53','#334b83','#242332','#d14040','#85283b'];color='#9aad60' if unripe else colors[kind]
    d.ellipse((x-size,y-size,x+size,y+size),fill='#27322b' if unripe else ['#60243a','#1c294c','#17171f','#762531','#481d2c'][kind]);d.ellipse((x-size+1,y-size+1,x+size-1,y+size-1),fill=color)
    if kind==0:
        for dx,dy in [(-1,-2),(2,-1),(-2,1),(1,2)]:d.point((x+dx,y+dy),fill='#c3c87d' if unripe else '#ee6173')
    else:d.line((x-size+2,y-size+1,x-size+3,y-size+1),fill='#d3d898' if unripe else ['#ef7b83','#92a2c3','#797081','#ff9891','#ca777c'][kind])
    if kind==1:d.line((x-1,y-size,x,y-size+1,x+1,y-size),fill='#161f35')
def bloom(d,x,y,kind):
    for dx,dy in [(0,-1),(-1,0),(1,0),(0,1)]:d.point((x+dx,y+dy),fill='#dcaab8' if kind==4 else '#e5dfce')
    d.point((x,y),fill='#a5ad69')

for k,id in enumerate(B):
    im,d=blank()
    if k==0:
        berry(d,12,20,k,size=6);berry(d,23,16,k,size=5);leaf(d,12,11,k);leaf(d,22,8,k);d.line((12,13,12,11,20,8),fill='#465631')
    elif k==1:
        berry(d,10,16,k,size=6);berry(d,22,23,k,size=6)
        for x,y in [(15,8),(23,11),(6,25)]:leaf(d,x,y,k)
    elif k==2:
        d.line([(14,5),(18,11),(16,24)],fill='#775434',width=2);leaf(d,10,10,k)
        for x,y in [(19,13),(12,17),(23,19),(17,24),(9,23)]:berry(d,x,y,k,size=4)
    elif k==3:
        leaf(d,9,12,k);leaf(d,24,12,k)
        for x,y in [(9,21),(17,24),(24,20)]:berry(d,x,y,k,size=4)
    else:
        d.line([(4,22),(13,16),(24,10),(29,13)],fill='#80634a')
        for x,y in [(8,23),(23,7)]:leaf(d,x,y,k)
        for x,y in [(11,14),(23,21),(16,25)]:berry(d,x,y,k,size=4)
    save(im,'item',id)
    im,d=blank();d.polygon([(10,25),(12,22),(20,23),(24,28),(8,28)],fill='#674b35');d.line((11,25,21,25),fill='#9b7447')
    if k==4:d.line([(8,24),(14,20),(25,22)],fill='#7c6246');leaf(d,12,20,k);leaf(d,23,21,k)
    else:
        d.line((16,24,16,9),fill='#7b6144',width=2);leaf(d,11,16,k);leaf(d,22,12,k);leaf(d,16,7,k)
    save(im,'item',id+'_sapling')
    for phase in range(5):
        for half in ['lower','upper'] if k==0 else ['lower']:
            im,d=blank();height=5 if phase==0 else [31,23,30,15,10][k]
            if k==4:
                d.line([(2,29),(9,25),(17,27),(26,24),(31,27)],fill='#735a3d')
                spots=[(5,27),(13,27),(24,26),(28,28)] if phase else [(16,28)]
                for x,y in spots:leaf(d,x,y,k)
            else:
                top=31-height;stems=[8,16,24] if phase else [16]
                for n,x in enumerate(stems):
                    d.line((x,31,x+(n%2)*2,top+3),fill='#785735',width=1 if k==1 else 2)
                spots=[(8,31-height//2),(21,31-height//2-2),(13,top+5),(25,top+8),(6,27),(17,23)] if phase else [(14,28),(19,27)]
                if k==0 and half=='upper':spots=[(7,5),(18,6),(26,12),(10,17),(22,24),(5,28)]
                for x,y in spots:leaf(d,x,y,k,2 if k in [0,2] and phase else 1)
            if phase>=2:
                for n,(x,y) in enumerate(spots[:4]):
                    if phase==2:bloom(d,x+2,y+3,k)
                    elif k==2:
                        for dx,dy in [(0,2),(2,5),(-1,7)]:berry(d,x+dx,y+dy,k,phase==3,size=2)
                    else:berry(d,x+1,y+3,k,phase==3,size=2 if k!=0 else 3)
            name=f'{id}_bush_{half}_{phase}';save(im,'block',name)
            js(A/f'models/block/{name}.json',{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':'slavicmyths:block/'+name}})
    variants={}
    for phase in range(5):
        for half in ['lower','upper']:
            variants[f'age={phase},half={half}']={'model':f'slavicmyths:block/{id}_bush_{half if k==0 else "lower"}_{phase}'}
    js(A/f'blockstates/{id}_bush.json',{'variants':variants})
    for item in [id,id+'_sapling']:js(A/f'models/item/{item}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+item}})

def wood_color(x,y,kind):
    if kind=='log':
        streak=(x+(y//7)%3)%8;v=-30 if streak in [0,1] else 12 if streak==2 else 0
        if (x-11)**2+(y-19)**2<12:v-=25
        base=(121,101,84)
    elif kind in ['log_top','stripped_apple_log_top']:
        radius=math.hypot(x-14,y-17);v=-25 if int(radius)%4==0 else 4;base=(173,132,114)
        if radius>14 and kind=='log_top':base=(111,91,75)
    elif kind=='stripped_apple_log':v=-16 if (x+y//9)%7==0 else (7 if x%7==1 else 0);base=(173,132,114)
    else:
        row=y//8;v=-38 if y%8==0 else -25 if (x+row*11)%32==0 else 7 if y%8==1 else -8 if (x+2*(y%8))%17<3 else 0;base=(157,110,91)
    return tuple(max(0,min(255,c+v)) for c in base)+(255,)
for name,kind in [('apple_log','log'),('apple_log_top','log_top'),('stripped_apple_log','stripped_apple_log'),('stripped_apple_log_top','stripped_apple_log_top'),('apple_planks','planks')]:
    im=Image.new('RGBA',(32,32));im.putdata([wood_color(x,y,kind) for y in range(32) for x in range(32)]);save(im,'block',name)
for name in ['apple_door_top','apple_door_bottom','apple_trapdoor']:
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    for y in range(32):
        for x in range(32):d.point((x,y),fill=wood_color(x,y,'planks'))
    d.rectangle((1,1,30,30),outline='#694a38',width=2)
    if name.endswith('top'):
        for x in [8,20]:d.rectangle((x,7,x+4,19),fill=(0,0,0,0))
    if name.endswith('bottom'):d.rectangle((25,8,27,12),fill='#3b3d37')
    if name.endswith('trapdoor'):
        for x in [9,20]:
            for y in [9,20]:d.rectangle((x,y,x+2,y+2),fill=(0,0,0,0))
    save(im,'block',name)

for stage in range(4):
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    for y in range(32):
        for x in range(32):
            u=(x+(y//5%2)*3)%7;v=y%5
            if not(u==0 and v in [0,1]) and not(u==6 and v==4):
                shade=[-14,0,10,4][(x//3+y//2)%4];d.point((x,y),fill=tuple(c+shade for c in (57,92,41))+(255,))
    for x,y in [(7,7),(24,12),(12,25)]:
        if stage==1:bloom(d,x,y,0)
        elif stage in [2,3]:berry(d,x,y,3,stage==2,size=2 if stage==2 else 3);d.point((x,y-3),fill='#795d39')
    name='apple_leaves' if stage==0 else f'apple_leaves_stage{stage}';save(im,'block',name)
    js(A/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/'+name}})
js(A/'blockstates/apple_leaves.json',{'variants':{f'fruit_stage={s}':{'model':'slavicmyths:block/'+('apple_leaves' if s==0 else f'apple_leaves_stage{s}')} for s in range(4)}})
im,d=blank();d.line((16,28,16,8),fill='#806853',width=2)
for x,y in [(10,15),(22,14),(15,7)]:leaf(d,x,y,1,2)
bloom(d,20,9,0);save(im,'block','apple_sapling')

def replace(s):return s.replace('minecraft:stripped_oak_','slavicmyths:stripped_apple_').replace('minecraft:oak_','slavicmyths:apple_').replace('minecraft:block/stripped_oak_','slavicmyths:block/stripped_apple_').replace('minecraft:block/oak_','slavicmyths:block/apple_').replace('minecraft:item/oak_','slavicmyths:item/apple_')
def converted(path):return json.loads(replace(V.read(path).decode()))
for name in VN:
    if name.startswith('assets/minecraft/models/block/oak_') or name.startswith('assets/minecraft/models/block/stripped_oak_'):
        filename=Path(name).name.replace('stripped_oak_','stripped_apple_').replace('oak_','apple_')
        if not filename.startswith('apple_leaves'):js(A/'models/block'/filename,converted(name))
for id in WOOD:
    vanilla=id.replace('stripped_apple_','stripped_oak_').replace('apple_','oak_')
    for folder in ['blockstates','models/item']:
        path=f'assets/minecraft/{folder}/{vanilla}.json'
        if path in VN and not(folder=='blockstates' and id=='apple_leaves'):js(A/f'{folder}/{id}.json',converted(path))
    if id=='apple_sapling':js(A/'models/block/apple_sapling.json',{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':'slavicmyths:block/apple_sapling'}})
    source='oak_sign' if id=='apple_wall_sign' else 'oak_hanging_sign' if id=='apple_wall_hanging_sign' else vanilla
    path=f'data/minecraft/loot_table/blocks/{source}.json'
    if path in VN:
        loot=converted(path)
        if id=='apple_leaves':loot['pools']=[pool for pool in loot['pools'] if 'minecraft:apple' not in json.dumps(pool)]
        js(D/f'loot_table/blocks/{id}.json',loot)
    path=f'data/minecraft/recipe/{vanilla}.json'
    if path in VN and id not in ['apple_leaves','apple_sapling']:js(D/f'recipe/{id}.json',converted(path))
for id in ['apple_sign','apple_wall_sign','apple_hanging_sign','apple_wall_hanging_sign']:js(A/f'models/block/{id}.json',{'textures':{'particle':'slavicmyths:block/apple_planks'}})
for id in ['apple_door','apple_sign','apple_hanging_sign']:
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    if id=='apple_door':
        for y in range(3,30):
            for x in range(8,24):d.point((x,y),fill=wood_color(x,y,'planks'))
        d.rectangle((11,6,14,14),fill=(0,0,0,0));d.rectangle((18,6,21,14),fill=(0,0,0,0));d.point((21,20),fill='#363a35')
    else:
        d.rectangle((5,8,26,21),fill='#9d705c');d.line((6,9,25,9),fill='#c1977d')
        if id=='apple_sign':d.rectangle((14,22,16,30),fill='#81553d')
        else:d.line((8,3,8,8),fill='#91928b',width=2);d.line((23,3,23,8),fill='#91928b',width=2)
    save(im,'item',id);js(A/f'models/item/{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}})
for folder in ['entity/signs/slavicmyths','entity/signs/hanging/slavicmyths']:
    im=Image.new('RGBA',(64,32));im.putdata([wood_color(x%32,y,'planks') for y in range(32) for x in range(64)]);save(im,folder,'apple')

def cond_age(id,lo,hi=None):return {'condition':'minecraft:block_state_property','block':'slavicmyths:'+id+'_bush','properties':{'age':str(lo) if hi is None else {'min':str(lo),'max':str(hi)}}}
shears={'condition':'minecraft:match_tool','predicate':{'items':'minecraft:shears'}}
for k,id in enumerate(B):
    pools=[]
    def pool(item,conditions):return {'rolls':1,'conditions':conditions,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item}]}
    pools.append(pool(id+'_sapling',[shears]))
    pools.append(pool(id+'_sapling',[{'condition':'minecraft:inverted','term':shears},cond_age(id,0),{'condition':'minecraft:random_chance','chance':.5}]))
    pools.append(pool(id+'_sapling',[{'condition':'minecraft:inverted','term':shears},cond_age(id,1,4),{'condition':'minecraft:random_chance','chance':.35}]))
    pools.append(pool(id,[{'condition':'minecraft:inverted','term':shears},cond_age(id,4),{'condition':'minecraft:random_chance','chance':.5}]))
    if k==0:
        # Neighbor removal can execute an upper loot table: allow it only with a surviving adult lower.
        lower={'condition':'minecraft:block_state_property','block':'slavicmyths:raspberry_bush','properties':{'half':'lower'}}
        upper={'condition':'minecraft:all_of','terms':[{'condition':'minecraft:block_state_property','block':'slavicmyths:raspberry_bush','properties':{'half':'upper'}},{'condition':'minecraft:location_check','offsetY':-1,'predicate':{'block':{'blocks':'slavicmyths:raspberry_bush','state':{'half':'lower','age':{'min':'1','max':'4'}}}}}]}
        for p in pools:p['conditions'].append({'condition':'minecraft:any_of','terms':[lower,upper]})
    js(D/f'loot_table/blocks/{id}_bush.json',{'type':'minecraft:block','pools':pools})

def tag(ns,folder,name,values):
    p=R/f'data/{ns}/tags/{folder}/{name}.json';old=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};old['values']=list(dict.fromkeys(old['values']+values));js(p,old)
tag('slavicmyths','block','berry_soil',['minecraft:'+s for s in ['grass_block','dirt','coarse_dirt','podzol','rooted_dirt','mud','moss_block']])
tag('slavicmyths','block','berry_bushes',['slavicmyths:'+b+'_bush' for b in B]);tag('slavicmyths','item','berries',['slavicmyths:'+b for b in B]);tag('slavicmyths','item','berry_saplings',['slavicmyths:'+b+'_sapling' for b in B])
for folder in ['block','item']:
    tag('slavicmyths',folder,'apple_logs',['slavicmyths:'+s for s in WOOD[:4]])
    for name,values in [('logs',['#slavicmyths:apple_logs']),('logs_that_burn',['#slavicmyths:apple_logs']),('planks',['slavicmyths:apple_planks']),('leaves',['slavicmyths:apple_leaves']),('saplings',['slavicmyths:apple_sapling']),('wooden_stairs',['slavicmyths:apple_stairs']),('wooden_slabs',['slavicmyths:apple_slab']),('wooden_fences',['slavicmyths:apple_fence']),('fence_gates',['slavicmyths:apple_fence_gate']),('wooden_doors',['slavicmyths:apple_door']),('wooden_trapdoors',['slavicmyths:apple_trapdoor']),('wooden_pressure_plates',['slavicmyths:apple_pressure_plate']),('wooden_buttons',['slavicmyths:apple_button']),('signs',['slavicmyths:apple_sign']),('hanging_signs',['slavicmyths:apple_hanging_sign'])]:
        if f'data/minecraft/tags/{folder}/{name}.json' in VN:tag('minecraft',folder,name,values)
for name,values in [('standing_signs',['apple_sign']),('wall_signs',['apple_wall_sign']),('ceiling_hanging_signs',['apple_hanging_sign']),('wall_hanging_signs',['apple_wall_hanging_sign']),('all_signs',['apple_sign','apple_wall_sign','apple_hanging_sign','apple_wall_hanging_sign']),('mineable/axe',WOOD[:-2])]:tag('minecraft','block',name,['slavicmyths:'+s for s in values])

biomes=[['forest','birch_forest','old_growth_birch_forest','meadow'],['taiga','snowy_taiga','old_growth_pine_taiga','old_growth_spruce_taiga'],['forest','birch_forest','meadow'],['taiga','snowy_taiga','old_growth_pine_taiga','old_growth_spruce_taiga'],['swamp','mangrove_swamp']]
for k,id in enumerate(B):
    feature=id+'_garden_patch'
    js(D/f'worldgen/configured_feature/{feature}.json',{'type':'slavicmyths:'+feature,'config':{}})
    js(D/f'worldgen/placed_feature/{feature}.json',{'feature':'slavicmyths:'+feature,'placement':[{'type':'minecraft:rarity_filter','chance':[8,10,14,12,8][k]},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'},{'type':'minecraft:biome'}]})
    js(D/f'neoforge/biome_modifier/{feature}.json',{'type':'neoforge:add_features','biomes':['minecraft:'+b for b in biomes[k]],'features':['slavicmyths:'+feature],'step':'vegetal_decoration'})
# Old feature ID stays registered/configured; its former biome injection is replaced by five scoped patches.
js(D/'neoforge/biome_modifier/core_berry_patch.json',{'type':'neoforge:none'})
for id in ['apple_tree','apple_sapling']:js(D/f'worldgen/configured_feature/{id}.json',{'type':'slavicmyths:apple_tree','config':{}})
js(D/'worldgen/placed_feature/apple_tree.json',{'feature':'slavicmyths:apple_tree','placement':[{'type':'minecraft:rarity_filter','chance':18},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'},{'type':'minecraft:biome'}]})
js(D/'neoforge/biome_modifier/apple_tree.json',{'type':'neoforge:add_features','biomes':['minecraft:'+b for b in ['plains','sunflower_plains','forest','birch_forest','meadow']],'features':['slavicmyths:apple_tree'],'step':'vegetal_decoration'})
js(D/'advancement/garden_harvest.json',{'parent':'minecraft:husbandry/root','display':{'icon':{'id':'slavicmyths:raspberry'},'title':{'translate':'advancement.slavicmyths.garden_harvest.title'},'description':{'translate':'advancement.slavicmyths.garden_harvest.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'berry':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'#slavicmyths:berries'}]}}},'requirements':[['berry']]})
ruB=['Малина','Черника','Чёрная смородина','Брусника','Клюква'];ruS=['малины','черники','чёрной смородины','брусники','клюквы'];enB=['Raspberry','Blueberry','Blackcurrant','Lingonberry','Cranberry']
ruWood=['Яблоневое бревно','Обтёсанное яблоневое бревно','Яблоневая древесина','Обтёсанная яблоневая древесина','Яблоневые доски','Яблоневые ступени','Яблоневая плита','Яблоневый забор','Яблоневая калитка','Яблоневая дверь','Яблоневый люк','Яблоневая нажимная плита','Яблоневая кнопка','Яблоневая табличка','Яблоневая настенная табличка','Яблоневая подвесная табличка','Яблоневая настенная подвесная табличка','Яблоневые листья','Саженец яблони']
enWood=['Apple Log','Stripped Apple Log','Apple Wood','Stripped Apple Wood','Apple Planks','Apple Stairs','Apple Slab','Apple Fence','Apple Fence Gate','Apple Door','Apple Trapdoor','Apple Pressure Plate','Apple Button','Apple Sign','Apple Wall Sign','Apple Hanging Sign','Apple Wall Hanging Sign','Apple Leaves','Apple Sapling']
tabIds=['slavicmyths','resources','farming','food','tools','armor','decor','magic','mobs'];ruTabs=['Основное / Лор','Ресурсы и материалы','Земледелие и растения','Еда и кухня','Инструменты и оружие','Броня и одежда','Декор и блоки','Магия и артефакты','Мобы / спавн-яйца'];enTabs=['Main / Lore','Resources & Materials','Farming & Plants','Food & Kitchen','Tools & Weapons','Armor & Clothing','Decor & Blocks','Magic & Artifacts','Mobs / Spawn Eggs']
for lang in ['ru_ru','en_us']:
    p=A/f'lang/{lang}.json';obj=json.loads(p.read_text(encoding='utf-8'));ru=lang=='ru_ru'
    for n,id in enumerate(B):obj['item.slavicmyths.'+id]=ruB[n] if ru else enB[n];obj['item.slavicmyths.'+id+'_sapling']='Саженец '+ruS[n] if ru else enB[n]+' Sapling';obj['block.slavicmyths.'+id+'_bush']=ruB[n] if ru else enB[n]+' Bush'
    for id,name in zip(WOOD,ruWood if ru else enWood):obj['block.slavicmyths.'+id]=name
    for id,name in zip(tabIds,ruTabs if ru else enTabs):obj['itemGroup.slavicmyths.'+id]=name
    obj['advancement.slavicmyths.garden_harvest.title']='Лесной урожай' if ru else 'Forest Harvest';obj['advancement.slavicmyths.garden_harvest.description']='Собери одну из новых ягод' if ru else 'Harvest one of the new berries';js(p,obj)
print('1.1.2 garden sprites, vanilla-compatible apple family and scoped data generated.')
