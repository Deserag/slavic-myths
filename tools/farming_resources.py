"""Reproducible 1.1.0 resources: native 32px pixel art, unique crop silhouettes, no antialiasing.
Sickle: short 40% wood grip and inward curved steel crescent, not a scythe.
Field hoe: long wood shaft, wide short perpendicular iron plate, not a pickaxe.
Can: round galvanized body, overhead handle and long upper-right spout with sprinkler.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import json, math

R=Path('src/main/resources'); A=R/'assets/slavicmyths'; D=R/'data/slavicmyths'
CROPS=['rye','barley','oat','turnip','cabbage','pea','flax']
PRODUCE=['rye_grain','barley_grain','oat_grain','turnip','cabbage','pea_pod','flax_stalk']
TOOLS=['sickle','field_hoe','watering_can','organic_fertilizer']
GREEN='#497431'; LIGHT='#8eb657'; DARK='#294c2b'; WOOD='#986239'; STEEL='#b2bdc2'
def js(p,obj):
    p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def image():
    im=Image.new('RGBA',(32,32));return im,ImageDraw.Draw(im)
def save(im,kind,name):
    p=A/f'textures/{kind}/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def leaf(d,x,y,side=1,size=4,color=GREEN):
    d.polygon([(x,y),(x+side*size,y-size//2-1),(x+side*(size-1),y-3),(x+side,y-2)],fill=color)
    d.line([(x,y-1),(x+side*(size-1),y-2)],fill=LIGHT)
def grain(d,x,y,kind,angle=0,scale=1):
    colors={'rye':('#76502d','#b7873e','#d1a459'),'barley':('#97713b','#d9b96d','#f1d790'),'oat':('#99855a','#dbc99b','#f1e4c6'),'flax':('#714733','#ab7240','#d2a76c')}
    dark,mid,hi=colors[kind];wide=3 if kind=='barley' else 2;length=5 if kind in ['rye','oat'] else 4
    d.polygon([(x,y-length),(x+wide,y-length+1),(x+wide+angle,y-1),(x+angle,y+1),(x-1,y-1)],fill=dark)
    d.line([(x+1,y-length+1),(x+1+angle,y-1)],fill=mid,width=2)
    d.point((x+1,y-length+1),fill=hi)
def pea(d,x,y,r=3):
    d.ellipse((x-r,y-r,x+r,y+r),fill=DARK);d.ellipse((x-r+1,y-r+1,x+r-1,y+r-1),fill=GREEN);d.line((x-r+2,y-r+1,x,y-r+1),fill=LIGHT)
def pod(d,x,y):
    d.polygon([(x-9,y+2),(x-4,y-5),(x+6,y-6),(x+10,y-3),(x+4,y+4),(x-4,y+5)],fill=DARK)
    d.line([(x-8,y+2),(x-3,y-4),(x+6,y-5),(x+9,y-3)],fill=LIGHT,width=2)
    for dx,dy in [(-4,1),(0,-1),(4,-2)]:pea(d,x+dx,y+dy,2)
def turnip(d,x,y,r=8):
    d.ellipse((x-r,y-r//2,x+r,y+r),fill='#544054');d.ellipse((x-r+1,y-r//2+1,x+r-1,y+r-1),fill='#dfd5b4')
    d.pieslice((x-r+1,y-r//2+1,x+r-1,y+r//2+2),180,360,fill='#a45a8b');d.line((x,y+r-1,x+1,y+r+3),fill='#c2b38f')
    for dx,dy in [(-3,-4),(0,-6),(4,-4)]:leaf(d,x,y-r//2,1 if dx>=0 else -1,abs(dx)+3)
def cabbage(d,x,y,r=9):
    d.ellipse((x-r,y-r,x+r,y+r),fill=DARK)
    for dx,dy,rx,ry,col in [(-4,1,5,7,GREEN),(4,2,5,6,'#5b843d'),(0,-3,6,6,'#82a955'),(-1,-3,3,5,'#a3bd6b')]:
        d.ellipse((x+dx-rx,y+dy-ry,x+dx+rx,y+dy+ry),fill=col)
        d.arc((x+dx-rx,y+dy-ry,x+dx+rx,y+dy+ry),50,230,fill=LIGHT,width=1)

for c in CROPS:
    im,d=image()
    if c in ['rye','barley','oat','flax']:
        positions={'rye':[(9,24,1),(13,20,1),(17,16,1),(21,12,1)],'barley':[(11,21,0),(16,23,0),(19,17,0),(13,16,0)],'oat':[(10,22,-1),(16,21,0),(23,20,1)],'flax':[(9,21,1),(14,25,1),(20,23,1),(21,15,1),(14,15,1)]}[c]
        for x,y,a in positions:grain(d,x,y,c,a)
        if c=='rye':d.line((7,21,8,18),fill='#b29b63')
        if c=='barley':d.line((10,16,8,11),fill='#d4b36a');d.line((20,13,23,9),fill='#d4b36a')
        if c=='flax':
            for x,y in [(24,7),(25,8),(24,9),(23,8)]:d.point((x,y),fill='#6492d4')
            d.point((24,8),fill='#c9d4df')
    elif c in ['turnip','cabbage']:
        for x,y in [(9,13),(17,12),(22,17),(13,21),(20,24),(8,24)][:6 if c=='turnip' else 5]:
            d.ellipse((x-1,y-1,x+1,y+1),fill='#3e2a22' if c=='turnip' else '#292923');d.point((x,y-1),fill='#766143')
        if c=='turnip':d.rectangle((24,10,25,11),fill='#d9c692')
        else:leaf(d,14,15,-1,4);leaf(d,15,15,1,4)
    else:
        for x,y in [(9,20),(17,24),(21,14)]:pea(d,x,y,3)
        d.polygon([(8,10),(14,6),(18,7),(14,13)],fill=DARK);d.line((9,10,14,7,17,7),fill=LIGHT);pea(d,14,9,2)
    save(im,'item',c+'_seeds')

for name in PRODUCE:
    im,d=image()
    if name.endswith('_grain'):
        c=name.split('_')[0]
        positions={'rye':[(9,25,1),(14,25,0),(20,24,1),(24,22,-1),(12,18,1),(18,17,1)],'barley':[(8,23,0),(13,25,0),(19,25,0),(24,22,0),(12,19,0),(19,17,0)],'oat':[(8,23,-1),(14,25,0),(21,24,1),(25,20,0),(12,17,-1),(19,16,1)]}[c]
        for x,y,a in positions:grain(d,x,y,c,a)
        if c=='barley':d.line((8,18,6,13),fill='#dab86e');d.line((24,17,26,12),fill='#dab86e')
    elif name=='turnip':turnip(d,16,17,9)
    elif name=='cabbage':cabbage(d,16,16,11)
    elif name=='pea_pod':pod(d,16,17)
    else:
        for x in [10,13,16,19,22]:
            d.line([(x-3,28),(x,9),(x+1,6)],fill='#889254' if x%2==0 else '#b7a268');leaf(d,x-1,16,-1,3,'#788450')
            if x in [13,19]:d.ellipse((x-1,5,x+2,8),fill='#9a7649')
        d.rectangle((9,20,21,22),fill='#d8c590');d.line((14,20,15,24),fill='#f0dfb2')
    save(im,'item',name)

for tool in TOOLS:
    im,d=image()
    if tool=='sickle':
        d.polygon([(8,28),(5,26),(12,17),(15,19)],fill='#4c3529');d.line((7,26,13,18),fill=WOOD,width=3);d.line((8,25,13,18),fill='#b88651')
        d.polygon([(13,18),(12,12),(13,7),(17,3),(23,3),(27,6),(28,12),(25,17),(20,19),(18,17),(23,14),(24,9),(21,6),(17,7),(16,12),(17,17)],fill='#525d65')
        d.line([(17,17),(16,12),(17,7),(21,6),(24,9),(23,14),(18,17)],fill='#d0d5d3',width=2);d.point((14,18),fill='#2d3438')
    elif tool=='field_hoe':
        d.line((7,29,22,7),fill='#493323',width=4);d.line((8,28,22,8),fill=WOOD,width=2);d.line((9,27,22,9),fill='#b5834c')
        d.polygon([(14,3),(17,2),(29,10),(27,14),(24,15),(13,7)],fill='#3e484e');d.line((14,7,25,15,28,13),fill='#b3bcbf',width=2);d.rectangle((21,6,24,9),fill='#727d82')
    elif tool=='watering_can':
        d.arc((8,2,21,15),175,360,fill='#414c51',width=3);d.arc((9,3,20,14),180,355,fill='#a6b2b5')
        d.polygon([(20,19),(24,12),(28,9),(29,11),(26,15),(23,23)],fill='#526169');d.line((21,19,26,12),fill=STEEL,width=2)
        d.polygon([(26,7),(30,8),(31,12),(28,13),(26,10)],fill='#9aa9af');d.line((30,8,31,11),fill='#d2dbd8')
        d.ellipse((4,10,23,28),fill='#37464b');d.ellipse((5,11,22,26),fill='#707f84');d.rectangle((6,15,21,22),fill='#829196');d.ellipse((6,10,21,16),fill='#a0afb0');d.ellipse((8,11,19,14),fill='#425258');d.line((7,17,7,22),fill='#c6d0cc');d.line((8,23,10,24),fill='#9eafb0')
    else:
        d.polygon([(10,10),(8,7),(9,4),(22,4),(24,8),(22,10),(26,17),(27,26),(23,29),(8,29),(5,26),(6,18)],fill='#68543a')
        d.polygon([(11,11),(21,11),(24,19),(25,26),(22,27),(9,27),(7,25),(8,19)],fill='#b7a078');d.polygon([(10,5),(22,5),(20,9),(12,9)],fill='#d3c09a')
        d.line((9,10,23,10),fill='#60452e',width=2);d.line((16,10,21,13,24,12),fill='#8a6541');d.line((10,17,9,24),fill='#d5c098')
        d.line((16,24,16,19),fill=DARK,width=2);leaf(d,16,21,-1,4);leaf(d,16,20,1,4)
        for x,y in [(3,28),(29,26),(28,30)]:d.rectangle((x,y,x+1,y+1),fill='#705539')
    save(im,'item',tool)

def crop_texture(c,age):
    im,d=image(); h=[4,7,11,16,21,26,29][age]; base=30
    if c in ['rye','barley','oat','flax']:
        xs={'rye':[7,15,24],'barley':[5,11,19,26],'oat':[9,17,25],'flax':[5,12,20,27]}[c]
        for j,x in enumerate(xs[:2 if age==0 else len(xs)]):
            top=base-h+(j%2)*2; stem=GREEN if age<5 or c=='flax' else '#b09b55';d.line((x,base,x+(j%2),top),fill=stem,width=1)
            if age>=1:leaf(d,x,base-h//3,-1 if j%2 else 1,min(5,age+1),GREEN)
            if age>=2:leaf(d,x,base-h//2,1 if j%2 else -1,min(4,age),LIGHT if age<5 else '#baa562')
            if c in ['rye','barley'] and age>=3:
                length=(3+age) if c=='rye' else (2+age//2);gold=GREEN if age<4 else ('#aeb65b' if age==4 else ('#b99045' if c=='rye' else '#dbc278'))
                for k in range(length):
                    yy=top+k;d.line((x-1,yy,x+1+(k%2 if c=='barley' else 0),yy),fill=gold)
                    if c=='barley' and age>=4:d.line((x+(1 if k%2 else -1),yy,x+(4 if k%2 else -4),yy-4),fill='#cbb879')
                d.line((x,top-2,x,top+length),fill='#d3b16a' if age>=5 else GREEN)
            if c=='oat' and age>=3:
                for sign in [-1,1]:
                    d.line((x,top+6,x+sign*4,top+2),fill=stem)
                    d.line((x+sign*3,top+3,x+sign*5,top+7),fill=stem)
                    if age>=4:
                        yy=top+8;col='#a8b87a' if age==4 else '#dac99a';d.ellipse((x+sign*5-1,yy-2,x+sign*5+1,yy+1),fill=col)
                if age>=5:d.line((x,top+8,x+4,top+9),fill=stem);d.ellipse((x+3,top+9,x+5,top+12),fill='#e1d1a7')
            if c=='flax' and age>=4:
                for fx,fy in [(x,top),(x+3,top+4)][:1 if age==4 else 2]:
                    d.line((x,top+6,fx,fy),fill=GREEN)
                    for dx,dy in [(0,-1),(-1,0),(1,0),(0,1)]:d.point((fx+dx,fy+dy),fill='#699cdf')
                    d.point((fx,fy),fill='#d1d9a4')
                if age==6:d.ellipse((x-3,top+5,x-1,top+7),fill='#a38451')
    elif c=='turnip':
        if age>=4:turnip(d,16,26,age-1)
        for j in range([2,3,4,5,6,7,8][age]):
            x=16+((j%3)-1)*3;y=29-age//2
            leaf(d,x,y,-1 if j%2 else 1,2+age,GREEN if j%3 else LIGHT)
    elif c=='cabbage':
        if age<3:
            for j in range(2+age*2):leaf(d,15+j%2,29-j//2,-1 if j%2 else 1,2+age)
        else:cabbage(d,16,27-(age-3),4+(age-3)*2)
    else:
        for j in range(1 if age==0 else 3):
            x=[8,16,24][j];top=base-h+(j%2)*3;d.line((x,30,x,top),fill=GREEN)
            for k in range(1+age//2):
                y=29-k*5;leaf(d,x,y,-1,2+age//2);leaf(d,x,y-2,1,2+age//2)
            if age>=3:d.line([(x,top+3),(x+3,top),(x+5,top+1),(x+4,top+3)],fill=LIGHT)
            if age>=4:d.rectangle((x-2,top+4,x,top+6),fill='#e4e6cb')
            if age>=5 and (age==6 or j<2):
                d.polygon([(x+1,top+7),(x+3,top+8),(x+3,top+14),(x+1,top+15)],fill=DARK)
                d.line((x+2,top+8,x+2,top+13),fill=LIGHT)
    return im

def age_condition(c,lo,hi=None):
    return {'condition':'minecraft:block_state_property','block':'slavicmyths:'+c+'_crop','properties':{'age':str(lo) if hi is None else {'min':str(lo),'max':str(hi)}}}
def uniform(lo,hi):return {'type':'minecraft:uniform','min':lo,'max':hi}
def count(n):return {'function':'minecraft:set_count','count':n}
def entry(id,functions=None):
    e={'type':'minecraft:item','name':'slavicmyths:'+id}
    if functions:e['functions']=functions
    return e
def pool(e,conditions):return {'rolls':1,'entries':[e],'conditions':conditions}

for c,produce in zip(CROPS,PRODUCE):
    variants={}
    for age in range(7):
        name=f'{c}_crop_stage{age}';save(crop_texture(c,age),'block',name)
        js(A/f'models/block/{name}.json',{'parent':'minecraft:block/crop','render_type':'minecraft:cutout','textures':{'crop':'slavicmyths:block/'+name}})
        variants[f'age={age}']={'model':'slavicmyths:block/'+name}
    js(A/f'blockstates/{c}_crop.json',{'variants':variants})
    immature=[pool(entry(c+'_seeds'),[age_condition(c,0,1),{'condition':'minecraft:random_chance','chance':.5}]),pool(entry(c+'_seeds'),[age_condition(c,2,5)])]
    quantity=uniform(2,3) if c=='flax' else (1 if c in ['turnip','cabbage'] else uniform(2,4))
    functions=[count(quantity)]
    if c=='turnip':functions.append({'function':'minecraft:set_count','count':1,'add':True,'conditions':[{'condition':'minecraft:random_chance','chance':.3}]})
    functions.append({'function':'slavicmyths:capped_crop_fortune'})
    mature=[pool(entry(produce,functions),[age_condition(c,6)]),pool(entry(c+'_seeds',[count(uniform(1,3 if c=='flax' else 2))]),[age_condition(c,6)])]
    js(D/f'loot_table/blocks/{c}_crop.json',{'type':'minecraft:block','pools':immature+mature,'functions':[{'function':'minecraft:explosion_decay'}]})

for id in [c+'_seeds' for c in CROPS]+PRODUCE+TOOLS:
    js(A/f'models/item/{id}.json',{'parent':'minecraft:item/handheld' if id in ['sickle','field_hoe'] else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}})
recipes={'sickle':['iron_ingot','iron_nugget','stick'],'field_hoe':['iron_hoe','stick','stick'],'organic_fertilizer':['bone_meal']*4+['dirt']}
for name,ingredients in recipes.items():js(D/f'recipe/{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':'minecraft:'+i} for i in ingredients],'result':{'id':'slavicmyths:'+name,'count':1}})
# The supplied picture's grid accidentally contains six nuggets. Written ingredient cost is five.
js(D/'recipe/watering_can.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['I I','IBI',' I '],'key':{'I':{'item':'minecraft:iron_nugget'},'B':{'item':'minecraft:bucket'}},'result':{'id':'slavicmyths:watering_can','count':1}})
tags={'agricultural_seeds':[c+'_seeds' for c in CROPS],'cereal_grains':PRODUCE[:3],'raw_vegetables':PRODUCE[3:6],'farm_products':PRODUCE}
for name,values in tags.items():js(D/f'tags/item/{name}.json',{'replace':False,'values':['slavicmyths:'+v for v in values]})
js(D/'tags/block/sickle_harvestable.json',{'replace':False,'values':['slavicmyths:'+c+'_crop' for c in CROPS]+['minecraft:'+c for c in ['wheat','carrots','potatoes','beetroots']]})
for tag in ['durability','mining','mining_loot']:
    p=R/f'data/minecraft/tags/item/enchantable/{tag}.json';old=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
    for id in ['sickle','field_hoe']:
        if 'slavicmyths:'+id not in old['values']:old['values'].append('slavicmyths:'+id)
    js(p,old)
for grass,chance in [('short_grass',.12),('tall_grass',.18)]:
    conditions=[{'condition':'neoforge:loot_table_id','loot_table_id':'minecraft:blocks/'+grass},{'condition':'minecraft:random_chance','chance':chance}]
    if grass=='tall_grass':
        # Mirror vanilla's intact-pair predicates: upper/lower break each work, secondary removal cannot roll again.
        terms=[]
        for half,neighbor,offset in [('lower','upper',1),('upper','lower',-1)]:
            terms.append({'condition':'minecraft:all_of','terms':[{'condition':'minecraft:block_state_property','block':'minecraft:tall_grass','properties':{'half':half}},{'condition':'minecraft:location_check','offsetY':offset,'predicate':{'block':{'blocks':'minecraft:tall_grass','state':{'half':neighbor}}}}]})
        conditions.append({'condition':'minecraft:any_of','terms':terms})
    js(D/f'loot_modifiers/{grass}_farming_seeds.json',{'type':'slavicmyths:grass_farming_seeds','conditions':conditions})
p=R/'data/neoforge/loot_modifiers/global_loot_modifiers.json';obj=json.loads(p.read_text())
for grass in ['short_grass','tall_grass']:
    id='slavicmyths:'+grass+'_farming_seeds'
    if id not in obj['entries']:obj['entries'].append(id)
js(p,obj)
js(D/'advancement/new_seeds.json',{'parent':'minecraft:husbandry/root','display':{'icon':{'id':'slavicmyths:rye_seeds'},'title':{'translate':'advancement.slavicmyths.new_seeds.title'},'description':{'translate':'advancement.slavicmyths.new_seeds.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'seeds':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'#slavicmyths:agricultural_seeds'}]}}},'requirements':[['seeds']]})
ru=['Семена ржи','Семена ячменя','Семена овса','Семена репы','Семена капусты','Семена гороха','Семена льна','Зёрна ржи','Зёрна ячменя','Овёс','Репа','Кочан капусты','Стручок гороха','Стебель льна','Серп','Полевая мотыга','Лейка','Органическое удобрение']
en=['Rye Seeds','Barley Seeds','Oat Seeds','Turnip Seeds','Cabbage Seeds','Pea Seeds','Flax Seeds','Rye Grain','Barley Grain','Oats','Turnip','Cabbage','Pea Pod','Flax Stalk','Sickle','Field Hoe','Watering Can','Organic Fertilizer']
ids=[c+'_seeds' for c in CROPS]+PRODUCE+TOOLS
for lang,names in [('ru_ru',ru),('en_us',en)]:
    p=A/f'lang/{lang}.json';obj=json.loads(p.read_text(encoding='utf-8'));obj.update({'item.slavicmyths.'+i:n for i,n in zip(ids,names)})
    crops_ru=['Рожь','Ячмень','Овёс','Репа','Капуста','Горох','Лён']
    obj.update({'block.slavicmyths.'+c+'_crop':(crops_ru[i] if lang=='ru_ru' else c.title()) for i,c in enumerate(CROPS)})
    obj['tooltip.slavicmyths.watering_can.water']='Вода: %s/8' if lang=='ru_ru' else 'Water: %s/8'
    obj['advancement.slavicmyths.new_seeds.title']='Новые семена' if lang=='ru_ru' else 'New Seeds'
    obj['advancement.slavicmyths.new_seeds.description']='Найди семена новых культур' if lang=='ru_ru' else 'Find seeds of a new crop'
    js(p,obj)

# Native-resolution/contact-sheet preview is an offline artifact, never a game acceptance claim.
sheet=Image.new('RGB',(512,380),'#ded2aa');draw=ImageDraw.Draw(sheet)
for row,c in enumerate(CROPS):
    draw.text((4,row*44+15),c,fill='#25251e')
    for age in range(7):
        im=Image.open(A/f'textures/block/{c}_crop_stage{age}.png');sheet.paste(im,(80+age*44,row*44+8),im)
    for col,id in enumerate([c+'_seeds',PRODUCE[row]]):
        im=Image.open(A/f'textures/item/{id}.png');sheet.paste(im,(404+col*48,row*44+8),im)
for i,id in enumerate(TOOLS):
    im=Image.open(A/f'textures/item/{id}.png');sheet.paste(im,(80+i*100,330),im);draw.text((70+i*100,365),id,fill='#25251e')
p=Path('docs/verification/farming-1.1.0');p.mkdir(parents=True,exist_ok=True);sheet.save(p/'farming-assets.png')
print('Generated 49 crop stages, 18 item sprites and all farming data; existing content preserved.')

# Preserve the current brewing assets/tags after this earlier resource pass.
import runpy as _brewing_runpy
_brewing_runpy.run_path(str(Path(__file__).resolve().with_name("brewing_resources.py")))

# Preserve the latest integration assets and definitive recipes.
from integration_polish_resources import main as polish_118
polish_118()
