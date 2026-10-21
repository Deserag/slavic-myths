"""#111 T2: scoped production pixel masters, section B of the approved #92 sheet.

KEEP: old_button. Updated owner direction of 21 October explicitly permits
replacing pearl_fragment's old cuboid with a separate generated item sprite.
"""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
TEXTURES=ROOT/'src/main/resources/assets/slavicmyths/textures/item'
FIX=('ancient_coin','pearl_fragment','shield_boss_fragment','old_buckle','bandit_token','ataman_sign','voevoda_insignia','princely_seal','serpent_seal')
KEEP=('old_button',)
BLOCKED=()
IDS=('old_button','ancient_coin','pearl_fragment','shield_boss_fragment','old_buckle','bandit_token','ataman_sign','voevoda_insignia','princely_seal','serpent_seal')

def sprite(name):
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
    if name=='pearl_fragment':
        # Direction A: narrow chipped nacre, four value clusters + tiny pink reflection.
        palette={'o':'#455d6e','s':'#7997a7','m':'#bfd2d7','h':'#f6f4e7','p':'#d6bdd1'}
        rows=[
            '................','........oh......','.......omhho....','......omhhmho...',
            '.....omhhmmmho..','.....ohhmmmmo...','....omhhmmso....','....ohhmmsso....',
            '...omhhmssso....','...ohhmmssso....','..ommmsssso.....','..ompsssso......',
            '..ossssso.......','...ossso........','....oo..........','................']
        im.putdata([(0,0,0,0) if c=='.' else tuple(bytes.fromhex(palette[c][1:]))+(255,) for row in rows for c in row])
        return im.resize((32,32),Image.Resampling.NEAREST)
    elif name=='ancient_coin':
        d.polygon([(5,1),(10,1),(14,5),(14,10),(10,14),(5,14),(1,10),(1,5)],fill='#513d2a')
        d.polygon([(5,2),(10,2),(13,5),(13,10),(10,13),(5,13),(2,10),(2,5)],fill='#b99048')
        d.line([(3,5),(5,3),(10,3),(12,5)],fill='#dfc179')
        d.polygon([(5,4),(10,4),(11,5),(11,10),(10,11),(5,11),(4,10),(4,5)],fill='#806038')
        d.rectangle((6,5,9,9),fill='#b99048');d.rectangle((7,6,8,8),fill='#806038')
    elif name=='shield_boss_fragment':
        d.polygon([(5,1),(10,2),(13,4),(12,6),(10,7),(12,9),(10,12),(6,14),(2,11),(1,6)],fill='#353c40')
        d.polygon([(5,2),(10,3),(12,4),(10,6),(8,7),(10,9),(9,11),(6,13),(3,10),(2,6)],fill='#87918e')
        d.polygon([(4,4),(6,3),(10,4),(8,5),(4,6)],fill='#c1c8bc')
        d.polygon([(4,7),(8,6),(9,9),(6,11),(3,9)],fill='#586767')
        d.point((6,8),fill='#c1c8bc');d.point((6,9),fill='#353c40')
    elif name=='bandit_token':
        d.polygon([(5,2),(7,3),(10,2),(12,5),(14,7),(12,9),(12,12),(9,13),(6,12),(3,13),(2,9),(3,6),(2,4)],fill='#3f4139')
        d.polygon([(5,3),(7,4),(9,3),(11,6),(12,7),(11,9),(11,11),(9,12),(6,11),(4,12),(3,9),(4,6),(3,4)],fill='#7b7b62')
        d.line((4,5,6,4),fill='#a0a08a');d.line([(6,6),(9,6),(9,9),(6,9)],fill='#535648');d.line((6,7,8,8),fill='#8c593f')
    elif name=='old_buckle':
        d.rectangle((1,3,14,12),fill='#514733');d.rectangle((2,4,13,11),fill='#af9558')
        d.line((2,4,12,4),fill='#d2bd7d');d.rectangle((4,6,11,9),fill=(0,0,0,0))
        d.line((8,7,13,7),fill='#dfd3ad');d.line((8,8,12,8),fill='#827044')
    elif name=='ataman_sign':
        # Direction B: broad iron plate, broken edge highlights and warm inlay.
        d.polygon([(5,3),(10,3),(11,5),(14,5),(14,10),(11,10),(10,12),(5,12),(4,10),(1,10),(1,5),(4,5)],fill='#263137')
        d.polygon([(5,4),(10,4),(11,6),(13,6),(13,9),(10,9),(9,11),(6,11),(5,9),(2,9),(2,6),(5,6)],fill='#64757a')
        d.line((5,4,9,4),fill='#c6c6ad');d.line((2,6,4,6),fill='#a2aaa0')
        d.line((10,10,12,9),fill='#46565d');d.line((6,10,9,10),fill='#46565d')
        d.polygon([(7,4),(8,5),(9,7),(10,8),(8,10),(7,11),(6,9),(6,6)],fill='#a55942')
        d.line((7,5,7,9),fill='#d28a59');d.line((5,7,10,7),fill='#dec181');d.point((8,8),fill='#693c35')
        im=im.resize((32,32),Image.Resampling.NEAREST)
    elif name=='voevoda_insignia':
        d.polygon([(8,1),(14,7),(14,8),(8,14),(1,8),(1,7)],fill='#4c483d')
        d.polygon([(8,2),(13,7),(13,8),(8,13),(2,8),(2,7)],fill='#a6aaa0')
        d.line([(3,7),(8,3),(11,6)],fill='#dfdfc6')
        d.polygon([(8,4),(10,7),(8,11),(6,8)],fill='#b18c43')
        d.line((8,6,8,9),fill='#724635')
    elif name=='princely_seal':
        # Square stamp with a raised border; small corner chips, not a round coin.
        d.rectangle((2,2,13,13),fill='#51412d')
        d.rectangle((3,3,12,12),fill='#b09a64')
        d.line((3,3,12,3),fill='#dfcc91');d.line((3,3,3,11),fill='#dfcc91')
        d.rectangle((5,5,10,10),fill='#745137')
        d.line((6,6,9,6),fill='#dfcc91');d.line((8,7,8,9),fill='#b09a64')
        d.point((2,2),fill=(0,0,0,0));d.point((13,13),fill=(0,0,0,0))
    elif name=='serpent_seal':
        d.polygon([(5,1),(10,1),(13,4),(14,6),(14,10),(11,13),(9,14),(5,14),(2,11),(1,8),(2,4)],fill='#252d2b')
        d.polygon([(5,2),(10,2),(12,4),(13,6),(13,10),(10,12),(9,13),(5,13),(3,10),(2,8),(3,4)],fill='#827549')
        d.line([(4,4),(6,3),(10,3)],fill='#bba06a')
        d.polygon([(5,4),(10,4),(12,6),(11,10),(9,12),(5,11),(3,8),(4,6)],fill='#3e4943')
        # Direction A: a headed coiled snake, with a tapered tail, not a letter S.
        d.line([(9,5),(6,5),(5,6),(5,8),(7,9),(10,9),(10,11),(7,11)],fill='#b79150',width=2)
        d.polygon([(8,4),(10,4),(11,5),(10,6),(8,6)],fill='#d6b675')
        d.line((6,5,7,5),fill='#d6b675');d.line((6,10,8,11),fill='#806139')
        d.point((10,5),fill='#252d2b');d.point((6,11),fill='#b79150')
        # Preserve native 32px, authored as 2px clusters without smoothing.
        im=im.resize((32,32),Image.Resampling.NEAREST)
    else:raise ValueError(name)
    return im

def write(name,directory=TEXTURES):
    p=Path(directory)/f'{name}.png';p.parent.mkdir(parents=True,exist_ok=True);sprite(name).save(p)
    if name=='pearl_fragment':
        import json
        model=p.parent.parent.parent/'models/item/pearl_fragment.json'
        model.parent.mkdir(parents=True,exist_ok=True)
        model.write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/pearl_fragment'}},indent=2)+'\n',encoding='utf8')

def write_owned(path):
    path=Path(path)
    if path.stem not in FIX or path.suffix!='.png' or path.parent.name!='item':return False
    write(path.stem,path.parent);return True

if __name__=='__main__':
    import argparse
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('ids',nargs='*',choices=FIX);a=p.parse_args()
    for name in a.ids or FIX:write(name);print(name)
