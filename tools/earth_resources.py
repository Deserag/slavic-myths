"""Reproducible original pixel resources; supplied audio remains labelled prototypes.
Merges new entries only: never replace another subsystem's lang, sounds or tags.
"""
from pathlib import Path
import json, random, shutil
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]; RES=ROOT/'src/main/resources'; SPEC=ROOT/'work/earth-spirit-v2-spec'; ASSET=RES/'assets/slavicmyths'; DATA=RES/'data/slavicmyths'
def write(path,obj):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def merge(path,new):
    old=json.loads(path.read_text(encoding='utf-8')) if path.exists() else {};old.update(new);write(path,old)
def stone(size,seed):
    rng=random.Random(seed);im=Image.new('RGBA',(size,size));step=9
    seeds=[(x+rng.randint(-2,2),y+rng.randint(-2,2),rng.randint(-8,8)) for y in range(-step,size+step,step) for x in range(-step,size+step,step)]
    for y in range(size):
        for x in range(size):
            candidates=sorted(((x-a)**2+(y-b)**2,a,b,t) for a,b,t in seeds if abs(x-a)<step*2 and abs(y-b)<step*2)[:2]
            first,a,b,t=candidates[0];gap=candidates[1][0]-first
            if gap<10:c=(49,51,49)
            else:
                shade=12 if gap<25 and x<a and y<b else -7 if gap<25 else 0
                c=(112+t+shade,114+t+shade,107+t+shade)
            im.putpixel((x,y),c+(255,))
    return im
textures=ASSET/'textures/entity/earth_spirit';textures.mkdir(parents=True,exist_ok=True)
for phase in (1,2,3):
    im=stone(256,16901);d=ImageDraw.Draw(im)
    # Separate head/brows, upper/lower arms, legs and root panels; asymmetric veins.
    for rect,color in [((0,64,79,103),(107,105,98)),((80,64,123,87),(56,45,37)),((224,192,239,219),(87,64,44)),((224,224,255,242),(58,48,38))]:
        x0,y0,x1,y1=rect
        if x0!=0:d.rectangle(rect,fill=color+(255,))
        if x0==0:
            for x,y in [(5,66),(17,72),(41,68),(62,84),(9,93),(32,89),(55,97)]:
                d.rectangle((x,y,x+7,y+5),fill=(129,124,113,255));d.line([(x,y),(x+3,y),(x+3,y+4)],fill=(73,70,63,255))
        else:
            for y in range(y0+2,y1,4):d.line((x0+1,y,x1-1,y+1),fill=(48,40,31,255))
    glow=(242,162,71,255) if phase==1 else (255,182,89,255) if phase==2 else (240,132,54,255)
    d.rectangle((224,240,255,255),fill=glow)
    # Large clustered moss panels and distinct asymmetric wear instead of photo noise.
    for x,y in [(151,5),(183,4),(7,122),(88,141),(40,183),(97,217),(200,198)]:
        d.rectangle((x,y,x+8,y+4),fill=(89,100,68,255));d.rectangle((x+3,y-2,x+6,y+5),fill=(55,65,46,255))
    for x,y in [(12,5),(73,13),(150,18),(27,125),(93,151),(12,185),(88,213)]:
        d.line([(x,y),(x+5,y),(x+5,y+4),(x+8,y+4),(x+8,y+9)],fill=glow,width=1 if phase==1 else 2)
    im.save(textures/f'phase_{phase}.png')
for variant in (0,1):
    im=stone(64,17000+variant);d=ImageDraw.Draw(im);d.rectangle((56,56,63,63),fill=(242,162,71,255));d.rectangle((33,1,42,5),fill=(89,100,68,255));im.save(textures/f'servant_{variant}.png')
stone(64,18000).save(textures/'rock.png');stone(128,18001).save(textures/'pillar.png')
icon=Image.new('RGBA',(64,64));d=ImageDraw.Draw(icon);outline=[(14,6),(41,5),(41,11),(52,15),(56,34),(51,49),(43,56),(22,59),(10,51),(7,33),(11,16)]
d.polygon(outline,fill=(45,46,49,255));d.polygon([(16,9),(39,9),(40,15),(49,17),(52,34),(47,47),(41,53),(23,55),(14,48),(11,33),(14,17)],fill=(89,82,76,255))
d.line([(15,15),(22,10),(38,10),(40,16),(48,18)],fill=(149,138,126,255),width=3);d.rectangle((12,34,17,42),fill=(86,100,72,255));d.rectangle((40,43,45,50),fill=(56,45,37,255))
spiral=[(19,46),(41,46),(41,23),(20,23),(20,40),(35,40),(35,29),(26,29),(26,34),(30,34)]
d.line(spiral,fill=(110,50,30,255),width=7);d.line(spiral,fill=(169,99,44,255),width=5);d.line(spiral,fill=(245,179,89,255),width=3)
for x in (24,30,36):d.rectangle((x,13,x+2,18),fill=(242,162,71,255))
path=ASSET/'textures/item/rune_earth_heart_iii.png';path.parent.mkdir(parents=True,exist_ok=True);icon.save(path)
effect=Image.new('RGBA',(32,32));ed=ImageDraw.Draw(effect);ed.line([(3,23),(9,16),(13,22),(20,12),(29,18)],fill=(217,151,76,255),width=3);ed.rectangle((12,4,18,9),fill=(117,107,90,255));ed.line((15,10,20,19),fill=(157,143,117,255),width=3)
ep=ASSET/'textures/mob_effect/earth_imbalance.png';ep.parent.mkdir(parents=True,exist_ok=True);effect.save(ep)
items=['rune_earth_heart_iii','earth_servant_spawn_egg']
write(ASSET/'models/item/rune_earth_heart_iii.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/rune_earth_heart_iii'}})
for egg in items[1:]:write(ASSET/f'models/item/{egg}.json',{'parent':'minecraft:item/template_spawn_egg'})
for name,seed in [('earth_pillar_stone',19000),('earth_shelter_stone',19001),('earth_gate_stone',19002),('earth_heart_seal',19003)]:
    im=stone(32,seed);dr=ImageDraw.Draw(im)
    if name=='earth_gate_stone':dr.line((16,0,16,31),fill=(48,49,52,255),width=3);dr.rectangle((3,12,12,15),fill=(91,71,54,255));dr.rectangle((20,12,29,15),fill=(91,71,54,255))
    else:dr.line([(7,24),(25,24),(25,6),(7,6),(7,19),(19,19),(19,12),(13,12),(13,15)],fill=(166,106,57,255),width=2)
    p=ASSET/f'textures/block/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
    if name=='earth_heart_seal':
        variants={}
        for crack in range(3):
            tex=im.copy();td=ImageDraw.Draw(tex)
            if crack:td.line([(0,10),(12,14),(14,31)],fill=(242,162,71,255),width=crack)
            tex.save(ASSET/f'textures/block/{name}_{crack}.png')
            model={'textures':{'all':f'slavicmyths:block/{name}_{crack}'},'elements':[{'from':[1,0,1],'to':[15,13,15],'faces':{face:{'texture':'#all'} for face in ('up','down','north','south','west','east')}},{'from':[3,13,3],'to':[13,16,13],'faces':{face:{'texture':'#all'} for face in ('up','down','north','south','west','east')}}]}
            write(ASSET/f'models/block/{name}_{crack}.json',model);variants[f'crack={crack}']={'model':f'slavicmyths:block/{name}_{crack}'}
        write(ASSET/f'blockstates/{name}.json',{'variants':variants});write(ASSET/f'models/item/{name}.json',{'parent':f'slavicmyths:block/{name}_0'})
    else:
        write(ASSET/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'slavicmyths:block/{name}'}});write(ASSET/f'blockstates/{name}.json',{'variants':{'':{'model':f'slavicmyths:block/{name}'}}});write(ASSET/f'models/item/{name}.json',{'parent':f'slavicmyths:block/{name}'})
    write(DATA/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[]})
sounds=SPEC/'assets/slavicmyths/sounds/earth_spirit';target=ASSET/'sounds/earth_spirit';target.mkdir(parents=True,exist_ok=True)
for p in sounds.glob('*.ogg'):shutil.copyfile(p,target/p.name)
merge(ASSET/'sounds.json',json.loads((SPEC/'assets/slavicmyths/sounds.json').read_text()))
ru={'entity.slavicmyths.earth_spirit':'Дух земли','entity.slavicmyths.earth_servant':'Слуга земли','entity.slavicmyths.earth_encounter_actor':'Каменные осколки','item.slavicmyths.rune_earth_heart_iii':'Руна Сердца Земли III','item.slavicmyths.earth_spirit_spawn_egg':'Яйцо призыва Духа земли','item.slavicmyths.earth_servant_spawn_egg':'Яйцо призыва Слуги земли','earth.slavicmyths.boss_phase':'%s — фаза %s','earth.slavicmyths.rune_tier':'Уровень III','earth.slavicmyths.rune_hp':'+2 HP к максимальному здоровью','earth.slavicmyths.rune_damage':'−2,5% входящего урона','earth.slavicmyths.rune_kb':'+15% сопротивления отбрасыванию','earth.slavicmyths.armor_only':'Только броня; одна руна на предмет','rpg.slavicmyths.effect_earth_heart_iii':'+2 HP, −2,5% урона, +15% сопротивления отбрасыванию','advancement.slavicmyths.earth_heart.title':'Сердце земли','advancement.slavicmyths.earth_heart.description':'Победите Духа земли в Древних глубинах'}
en={'entity.slavicmyths.earth_spirit':'Earth Spirit','entity.slavicmyths.earth_servant':'Earth Servant','entity.slavicmyths.earth_encounter_actor':'Stone fragments','item.slavicmyths.rune_earth_heart_iii':'Earth Heart Rune III','item.slavicmyths.earth_spirit_spawn_egg':'Earth Spirit Spawn Egg','item.slavicmyths.earth_servant_spawn_egg':'Earth Servant Spawn Egg','earth.slavicmyths.boss_phase':'%s — phase %s','earth.slavicmyths.rune_tier':'Tier III','earth.slavicmyths.rune_hp':'+2 HP maximum health','earth.slavicmyths.rune_damage':'−2.5% incoming damage','earth.slavicmyths.rune_kb':'+15% knockback resistance','earth.slavicmyths.armor_only':'Armour only; one rune per piece','rpg.slavicmyths.effect_earth_heart_iii':'+2 HP, −2.5% damage, +15% knockback resistance','advancement.slavicmyths.earth_heart.title':'Earth Heart','advancement.slavicmyths.earth_heart.description':'Defeat the Earth Spirit in the Ancient Depths'}
ru.update({'earth.slavicmyths.block_hint':'Поставьте на просторной площадке и ударьте, чтобы пробудить Духа земли.','earth.slavicmyths.block_needs_space':'Для Духа земли нужно свободное место шириной 4 блока и высотой 7 блоков.','book.slavicmyths.earth_heart.title':'Сердце земли','book.slavicmyths.earth_heart.text':'Игровая интерпретация духа камня Древних глубин. Разрушенная печать пробуждает хранителя. Столбы можно вырвать лишь однажды; постоянные укрытия защищают от камней. Если все покинули арену, через 120 секунд хранитель засыпает, сохраняя здоровье. Руна Сердца Земли III подходит только для брони: +2 HP, −2,5% урона и +15% сопротивления отбрасыванию за предмет, максимум четыре предмета. Допуск к гнёздам определяется Рунной наковальней.'})
en.update({'earth.slavicmyths.block_hint':'Place on a spacious site and strike to awaken the Earth Spirit.','earth.slavicmyths.block_needs_space':'The Earth Spirit needs a clear space 4 blocks wide and 7 blocks high.','book.slavicmyths.earth_heart.title':'Earth Heart','book.slavicmyths.earth_heart.text':'A game interpretation of a stone spirit in the Ancient Depths. Breaking the seal awakens its guardian. Each pillar can be uprooted once; permanent shelters protect against rocks. When everyone leaves, the guardian sleeps after 120 seconds and keeps its health. Earth Heart Rune III fits armour only: +2 HP, −2.5% damage and +15% knockback resistance per piece, up to four pieces. Socket availability follows the Runic Anvil material rules.'})
ru['effect.slavicmyths.earth_imbalance']='Потеря равновесия';en['effect.slavicmyths.earth_imbalance']='Off balance'
for name,r,e in [('earth_pillar_stone','Камень столба','Pillar Stone'),('earth_shelter_stone','Каменное укрытие','Stone Shelter'),('earth_gate_stone','Ворота Сердца земли','Earth Heart Gate'),('earth_heart_seal','Запирающий камень Духа земли','Earth Spirit Binding Stone')]:ru['block.slavicmyths.'+name]=r;en['block.slavicmyths.'+name]=e
for lang,extra in [('ru_ru',ru),('en_us',en)]:
    merged=json.loads((SPEC/f'assets/slavicmyths/lang/{lang}.json').read_text());merged.update(extra);merge(ASSET/f'lang/{lang}.json',merged)
for name in ['bleed_immune','ominous_aura_immune']:
    p=DATA/f'tags/entity_type/{name}.json';old=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
    for entity in ['earth_spirit']:
        if 'slavicmyths:'+entity not in old['values']:old['values'].append('slavicmyths:'+entity)
    write(p,old)
write(DATA/'loot_table/entities/earth_servant.json',{'type':'minecraft:entity','pools':[]})
write(DATA/'loot_table/entities/earth_spirit.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:diamond','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':2,'max':4}}]}]},{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:ancient_coin','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]}]}]})
write(DATA/'advancement/earth_heart.json',{'display':{'icon':{'id':'slavicmyths:rune_earth_heart_iii'},'title':{'translate':'advancement.slavicmyths.earth_heart.title'},'description':{'translate':'advancement.slavicmyths.earth_heart.description'},'frame':'challenge','show_toast':True,'announce_to_chat':True,'hidden':False},'parent':'slavicmyths:root','criteria':{'victory':{'trigger':'minecraft:impossible'}}})
print('Earth resources generated; 30 supplied audio prototypes preserved.')

# Keep the approved 1.6.3 complete-body materials when regenerating resources.
import runpy
runpy.run_path(str(ROOT/"tools/earth_materials_163.py"))
