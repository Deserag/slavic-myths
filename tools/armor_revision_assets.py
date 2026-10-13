"""Armor-only final overlay. Authored 16px silhouettes, nearest-neighbor 32px assets.

No registration, balance, recipes, weapons, language or audio changes. Run after
legacy generators; their entrypoints also call this overlay to preserve this art.
"""
from pathlib import Path
from io import BytesIO
import json
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/slavicmyths'
PARTS = ('helmet', 'chestplate', 'leggings', 'boots')
IDS = ['bogatyr_chestplate', 'clear_sight_helmet', 'indomitability_leggings',
       'trophy_idol_chest', 'trophy_marinka_cloak', 'trophy_kartaus_mantle',
       'gambeson', 'plate_cuirass', 'wind_boots', 'seven_league_boots'] + [
           f'{family}_{part}' for family in ('perunite', 'poloz', 'dragon') for part in PARTS]

def save(im, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    out = BytesIO(); im.save(out, format='PNG')
    if not path.exists() or path.read_bytes() != out.getvalue():
        path.write_bytes(out.getvalue())

def icon(name):
    im = Image.new('RGBA', (16, 16)); d = ImageDraw.Draw(im)
    edge = '#252731'; steel = '#718495'; light = '#c2d2d7'
    gold = '#c6a05a'; pale = '#f0d598'; red = '#a34544'; leather = '#76503a'
    def rect(b, c): d.rectangle(b, fill=c)
    def poly(p, c): d.polygon(p, fill=c)
    def chest(base, hi, broad=False):
        poly([(3,2),(5,2),(6,4),(9,4),(10,2),(12,2),(14,4),(13,8),
              (11,8),(11,13),(4,13),(4,8),(2,8),(1,4)], edge)
        poly([(3,3),(5,3),(6,5),(9,5),(10,3),(12,3),(13,4),(12,7),
              (10,7),(10,12),(5,12),(5,7),(3,7),(2,4)], base)
        d.line((5,5,5,11),fill=hi); d.line((6,12,10,12),fill=hi)
        if broad: rect((1,3,4,5),base);rect((11,3,14,5),base);rect((1,3,4,3),hi);rect((11,3,14,3),hi)
    def legs(base, hi):
        poly([(3,2),(12,2),(12,13),(9,13),(8,7),(7,7),(6,13),(3,13)],edge)
        rect((4,3,11,5),base)
        for x in (4,9): rect((x,6,x+1,12),base);d.line((x,7,x,11),fill=hi)
    def boots(base, hi, tall=False):
        for x in (3,9):
            poly([(x,2 if tall else 5),(x+3,2 if tall else 5),(x+3,10),
                  (x+4,11),(x+4,13),(x-1,13),(x-1,11),(x,10)],edge)
            rect((x,3 if tall else 6,x+2,11),base);rect((x-1,12,x+3,12),base)
            d.line((x,4 if tall else 7,x+2,4 if tall else 7),fill=hi)
            d.point((x-1,12),fill=hi)
    if name == 'bogatyr_chestplate':
        chest('#8f393f',gold,True)
        for y in (6,8,10):
            for x in (4,9): rect((x,y,x+2,y+1),steel);d.line((x,y,x+2,y),fill=light)
        rect((7,6,8,10),gold);rect((7,7,8,8),pale);rect((4,12,11,12),leather);d.point((8,12),fill=pale)
    elif name == 'clear_sight_helmet':
        poly([(5,2),(10,2),(12,4),(13,7),(12,13),(10,13),(10,8),(5,8),(5,13),(3,13),(2,7),(3,4)],edge)
        poly([(5,3),(10,3),(11,4),(12,6),(3,6),(4,4)],steel);d.line((5,3,10,3),fill=light)
        rect((3,7,12,7),gold);rect((3,8,4,12),steel);rect((11,8,12,12),steel)
        poly([(6,6),(8,5),(9,6),(8,7)],'#69d1d7');d.point((8,6),fill='#e4ffff')
        for x in (2,13): rect((x,3,x,8),gold);d.point((x,3),fill=pale)
    elif name == 'indomitability_leggings':
        legs('#49394c','#99758a');rect((4,3,11,4),leather);rect((7,3,8,4),gold)
        for x in (4,9):
            rect((x,6,x+2,8),steel);d.line((x,6,x+2,6),fill=light)
            rect((x,10,x+2,11),steel);d.point((x+1,10),fill=gold)
    elif name == 'trophy_idol_chest':
        chest('#554333',gold,True)
        for x in (1,11):
            rect((x,3,x+3,5),'#c9bc99');d.line((x,3,x+3,3),fill='#f0e5c4');d.point((x+1,5),fill='#96866b')
        for y in (6,8,10):
            d.line((4,y,11,y),fill='#e4d5b1');d.point((5,y+1),fill='#978469');d.point((10,y+1),fill='#978469')
        rect((7,5,8,11),'#bca379');rect((7,6,8,7),'#f0e5c4');rect((4,12,11,12),gold)
    elif name == 'trophy_marinka_cloak':
        # High collar, narrow waist, two pointed tails; silver mirror clasp.
        poly([(5,1),(10,1),(12,4),(13,11),(11,14),(8,12),(7,12),(4,14),(2,11),(3,4)],edge)
        poly([(5,2),(10,2),(11,4),(12,11),(11,12),(9,11),(9,6),(6,6),(6,11),(4,12),(3,11),(4,4)],'#514064')
        poly([(6,4),(9,4),(10,10),(8,12),(5,10)],'#884363')
        d.line((4,5,3,11),fill='#8c769e');d.line((11,5,12,11),fill='#8c769e')
        rect((6,3,9,3),'#b8c7d2');rect((7,4,8,5),'#a0d9df');d.point((7,4),fill='#e4f5f6')
        d.line((7,7,7,9),fill='#c27b9b');d.point((8,8),fill='#c27b9b')
    elif name == 'trophy_kartaus_mantle':
        # Square military cape, fur collar and broad crimson banner.
        poly([(3,2),(12,2),(14,5),(13,13),(10,13),(9,12),(6,12),(5,13),(2,13),(1,5)],edge)
        rect((3,4,12,12),'#485747');rect((2,5,3,11),'#657561');rect((12,5,13,11),'#657561')
        rect((3,3,12,4),'#bfb391');d.line((3,3,12,3),fill='#e7dac0')
        rect((6,5,9,12),'#933d39');rect((7,6,8,6),gold);d.line((7,7,7,10),fill=gold)
        rect((3,12,5,12),gold);rect((10,12,12,12),gold)
    elif name == 'gambeson':
        chest('#b4a27d','#e4d3a3')
        rect((2,5,4,9),'#b4a27d');rect((11,5,13,9),'#968265')
        for x in (5,8,10): d.line((x,6,x,11),fill='#8a785d')
        for y in (7,10):d.line((5,y,10,y),fill='#d1bd93')
        rect((4,12,11,12),leather);d.point((8,12),fill=gold)
    elif name == 'plate_cuirass':
        chest('#89939a',light,True)
        for y in (6,9):d.line((4,y,11,y),fill='#4d5864');d.line((5,y+1,10,y+1),fill=light)
        d.line((6,5,6,11),fill=gold);d.line((10,5,10,11),fill=gold)
        rect((4,12,11,12),'#574132');d.point((8,12),fill=gold)
    elif name in ('wind_boots','seven_league_boots'):
        boots('#725039',gold,True)
        for x in (3,9):
            rect((x,3,x+2,4),'#8daab4' if name=='wind_boots' else '#ac4f3f')
            d.line((x,8,x+2,8),fill='#ad8450');d.point((x+1,8),fill=gold)
            if name=='wind_boots':
                poly([(x+2,8),(x+4,5),(x+4,8),(x+2,10)],'#82cbd7');d.point((x+4,5),fill='#cce9e9')
                d.line((x-1,12,x+3,12),fill='#8daab4')
    else:
        family,part = name.split('_',1)
        base,hi = {'perunite':('#4e7189','#a4d6de'),'poloz':('#574d39','#d4ad5b'),'dragon':('#59616b','#a7b0b5')}[family]
        if part=='helmet':
            poly([(5,2),(10,2),(12,4),(13,7),(12,13),(10,13),(10,8),(5,8),(5,13),(3,13),(2,7),(3,4)],edge)
            poly([(5,3),(10,3),(12,5),(12,7),(3,7),(3,5)],base);d.line((5,3,10,3),fill=hi)
            rect((3,8,4,12),base);rect((11,8,12,12),base);d.point((3,8),fill=hi);d.point((11,8),fill=hi)
            if family=='perunite':
                rect((7,1,8,3),gold);d.line((4,6,11,6),fill=hi);d.point((8,6),fill='#e7fbfc')
            else:
                for x in (2,12):
                    poly([(x,6),(x-1,1),(x+1,2),(x+2,5)],gold if family=='poloz' else '#b4a48a')
                d.line((4,7,6,7),fill='#b74841');d.line((9,7,11,7),fill='#b74841')
        elif part=='chestplate':
            chest(base,hi,True)
            if family=='perunite':
                d.line([(8,5),(6,8),(8,8),(7,11)],fill='#bce4e6',width=1)
                rect((4,12,11,12),gold)
            elif family=='poloz':
                for x in (1,12):poly([(x,4),(x+1,1),(x+2,4)],gold)
                for y in (6,8,10): d.line((4,y,6,y),fill=hi);d.line((9,y,11,y),fill=hi)
                poly([(7,6),(9,7),(8,10),(7,9)],'#af4942');d.point((7,7),fill='#e98a67')
            else:
                for x in (1,12):poly([(x,4),(x,1),(x+2,4)],'#b4a48a')
                poly([(8,5),(10,8),(8,11),(6,8)],'#983e3e');poly([(8,6),(9,8),(8,9),(7,8)],'#e39d65')
                d.line((4,10,5,11),fill=hi);d.line((10,11,11,10),fill=hi)
        elif part=='leggings':
            legs(base,hi);rect((4,3,11,3),gold if family!='dragon' else '#b4a48a')
            for x in (4,9):
                rect((x,9,x+2,10),hi);d.point((x+1,10),fill=base)
                if family=='poloz': d.line((x+1,5,x+1,7),fill='#af4942')
                if family=='dragon':rect((x,5,x+2,7),'#983e3e');d.point((x,5),fill='#ca7960')
        else:
            boots(base,hi,family=='dragon')
            for x in (3,9):
                if family=='poloz':d.line((x,8,x+2,9),fill=gold);d.point((x+1,7),fill='#af4942')
                elif family=='dragon':rect((x,5,x+2,6),'#983e3e');d.point((x,5),fill='#ca7960')
                else:rect((x,8,x+2,9),gold);d.point((x+1,8),fill='#c7edec')
    return im.resize((32,32), Image.Resampling.NEAREST)

def standard_atlas(kind, inner=False):
    # Standard humanoid UV faces, with stable low-frequency material shading.
    colors = {
        'bogatyr':('#6d7b88','#c3ced4','#303844','#c5a05b'),
        'clear_sight':('#718494','#c2d4d9','#3e4b5b','#c6a05a'),
        'indomitability':('#564257','#9c7e94','#302b36','#c6a05a'),
        'wind':('#765039','#b49368','#453127','#88c4cf'),
        'seven_league':('#765039','#b49368','#453127','#ac4f3f'),
        'gambeson':('#b4a27d','#e4d3a3','#8a785d','#76503a'),
        'plate':('#89939a','#c2d2d7','#4d5864','#c6a05a'),
        'perunite':('#4e7189','#a4d6de','#2e455b','#c6a05a'),
    }
    base,hi,shade,trim=colors[kind]
    im=Image.new('RGBA',(64,32),base);d=ImageDraw.Draw(im)
    for x,y,w,h in [(0,0,32,16),(16,16,24,16),(0,16,16,16),(40,16,24,16)]:
        d.rectangle((x,y,x+w-1,y+h-1),fill=base);d.line((x,y,x+w-1,y),fill=hi)
        d.line((x,y+1,x,y+h-1),fill=hi);d.line((x+w-1,y,x+w-1,y+h-1),fill=shade)
    if kind in ('bogatyr','plate','perunite'):
        for x,w in ((20,8),(32,8),(44,4),(52,4),(4,4),(12,4)):
            for y in (21,25,29):
                d.line((x,y,x+w-1,y),fill=hi);d.line((x,y+2,x+w-1,y+2),fill=shade)
        d.rectangle((20,29,39,30),fill=trim)
        if kind=='bogatyr':
            d.rectangle((23,20,25,28),fill='#8f393f')
            for y in (21,25):d.line((23,y,25,y),fill=trim)
            d.rectangle((48,0,55,7),fill=trim);d.rectangle((50,1,52,3),fill='#f0d598')
        if kind=='perunite':
            d.line([(24,21),(22,24),(25,24),(23,27)],fill=hi)
            d.rectangle((40,0,59,3),fill=hi);d.rectangle((40,4,63,13),fill=trim)
            d.line([(20,1),(18,4),(20,4),(19,7)],fill=hi)
    elif kind=='gambeson':
        for x in (20,23,26,32,35,38,44,47,52,55):d.line((x,20,x,30),fill=shade)
        for y in (22,26):d.line((20,y,39,y),fill=hi)
        d.rectangle((20,29,39,30),fill=trim)
    elif kind in ('wind','seven_league','indomitability'):
        for x in (0,8):
            d.rectangle((x,16,x+7,19),fill=trim);d.line((x,16,x+7,16),fill=hi)
            d.rectangle((x,25,x+7,26),fill='#b49368');d.rectangle((x,30,x+7,31),fill=shade)
            if kind=='indomitability':
                for y in (20,26):d.rectangle((x,y,x+7,y+2),fill='#718495');d.line((x,y,x+7,y),fill='#c2d2d7')
        if kind=='wind':d.rectangle((24,16,31,23),fill=trim)
        if kind=='indomitability':d.rectangle((20,16,39,19),fill='#76503a');d.rectangle((48,0,55,7),fill=trim)
        if kind=='indomitability':
            # The raised metal plates have dedicated islands; the leg bone is leather.
            d.rectangle((0,16,15,31),fill=base)
            for x in (4,12):d.line((x,20,x,30),fill=hi)
            for y in (8,16):
                d.rectangle((48,y,63,y+7),fill='#718495');d.line((49,y+1,54,y+1),fill='#c2d2d7')
    elif kind=='clear_sight':
        d.line((8,8,15,8),fill=trim);d.rectangle((40,0,47,12),fill=trim)
        d.line((40,0,45,0),fill='#f0d598');d.rectangle((56,0,63,7),fill='#69d1d7');d.point((57,1),fill='#e4ffff')
    return im

def reward_atlas(kind):
    palette={
        'idol':('#554333','#8e7858','#e4d5b1','#c6a05a'),
        'marinka':('#514064','#342b42','#884363','#b8c7d2'),
        'kartaus':('#485747','#303c33','#933d39','#c6a05a')}
    base,shade,accent,trim=palette[kind]
    im=Image.new('RGBA',(128,128),base);d=ImageDraw.Draw(im)
    # Body uses (0,0), shoulders (40,0), ribs (0,20), collar (0,36), tails (0/26,48).
    d.rectangle((0,5,27,17),fill=base);d.rectangle((5,5,13,17),fill=base)
    d.line((5,5,13,5),fill=trim);d.line((5,17,13,17),fill=trim)
    d.line((5,6,5,16),fill=shade);d.line((13,6,13,16),fill=shade)
    if kind=='idol':
        d.rectangle((40,0,67,23),fill=accent)
        for y in (5,17):d.line((46,y,50,y),fill='#f0e5c4');d.line((46,y+2,50,y+2),fill='#978469')
        d.rectangle((0,20,23,23),fill=accent);d.line((1,21,10,21),fill='#f0e5c4')
        d.rectangle((8,6,10,15),fill=trim)
    else:
        for x in (0,26):
            d.rectangle((x,48,x+10,66),fill=base)
            d.line((x+1,49,x+1,65),fill=trim);d.line((x+4,49,x+4,65),fill=shade)
            d.line((x+1,65,x+4,65),fill=trim)
        d.rectangle((7,7,11,14),fill=accent)
        if kind=='marinka':
            d.rectangle((0,36,33,44),fill=base);d.line((6,41,15,41),fill=trim)
            d.rectangle((7,7,9,8),fill='#a0d9df');d.point((7,7),fill='#e4f5f6')
            d.line((8,11,8,13),fill='#c27b9b');d.point((9,12),fill='#c27b9b')
        else:
            d.rectangle((0,36,34,45),fill='#bfb391');d.line((6,42,15,42),fill='#e7dac0')
            d.rectangle((40,0,65,10),fill=base);d.line((46,5,51,5),fill=trim)
            d.rectangle((60,48,67,67),fill=accent);d.line((61,50,61,63),fill=trim)
            d.rectangle((8,8,9,9),fill=trim)
    return im

def scale_atlas(family):
    base,shade,hi,trim={'poloz':('#574d39','#453e31','#756342','#d4ad5b'),
                       'dragon':('#59616b','#484f59','#737e88','#b4a48a')}[family]
    im=Image.new('RGBA',(64,64),base);d=ImageDraw.Draw(im)
    for y in range(0,64,6):
        for x in range((y//6%2)*3,48,6):
            d.line([(x,y),(x+2,y+1),(x+4,y)],fill=hi)
            d.line([(x,y+4),(x+2,y+5),(x+4,y+4)],fill=shade)
    if family=='poloz':
        d.rectangle((48,32,63,47),fill=trim);d.line((49,32,49,45),fill='#f0d598')
        d.rectangle((48,48,63,63),fill='#af4942');d.rectangle((49,49,51,51),fill='#e98a67')
        for y in (20,28):d.line((20,y,39,y),fill=trim)
    else:
        d.rectangle((42,0,53,15),fill=trim);d.line((43,1,43,14),fill='#e1d1b4')
        # Dedicated front/core/shin islands, preserving the native horns and plates.
        d.rectangle((48,48,63,63),fill='#983e3e');d.rectangle((50,50,52,52),fill='#e39d65')
    return im

def apply():
    for name in IDS:
        save(icon(name),A/f'textures/item/{name}.png')
        path=A/f'models/item/{name}.json'
        value=json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}},indent=2)+'\n'
        if not path.exists() or path.read_text(encoding='utf-8')!=value:path.write_text(value,encoding='utf-8')
    for kind in ('wind','bogatyr','clear_sight','indomitability'):
        for layer in (1,2):save(standard_atlas(kind,layer==2),A/f'textures/armor/unique_{kind}_layer_{layer}.png')
    save(standard_atlas('seven_league'),A/'textures/armor/seven_league.png')
    for kind in ('gambeson','plate','perunite'):
        for layer in ((1,2) if kind=='perunite' else (1,)):
            save(standard_atlas(kind,layer==2),A/f'textures/models/armor/{kind}_layer_{layer}.png')
    for kind,name in [('idol','trophy_idol_chest'),('marinka','trophy_marinka_cloak'),('kartaus','trophy_kartaus_mantle')]:
        save(reward_atlas(kind),A/f'textures/models/armor/{name}.png')
    for family in ('poloz','dragon'):save(scale_atlas(family),A/f'textures/models/armor/{family}.png')

if __name__=='__main__':
    apply()
    print('ARMOR_ASSETS_PASS: 22 distinct armor sprites, worn atlases; no gameplay changes')
