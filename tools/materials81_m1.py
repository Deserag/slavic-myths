"""#81/#96 M1: four hand-authored pixel sprites; no other production outputs.

Native sizes are retained: three 16px sprites and one 32px forged blank.
Explicit integer pixel geometry, opaque palette, no filters, random noise or hue shifts.
Legacy generators call write_owned before emitting one of these four PNGs.
"""
from pathlib import Path
from io import BytesIO
from PIL import Image, ImageDraw

OWNED = ('silver_nugget', 'silver_ingot', 'perunite', 'iron_blank')
PREFIX = 'assets/slavicmyths/textures/item/'

def render_asset(name):
    from metals36_assets import image, PALETTES
    if name in PALETTES: return image(name)
    assert name in OWNED
    im = Image.new('RGBA', (32,32) if name == 'iron_blank' else (16,16))
    d = ImageDraw.Draw(im)
    if name == 'silver_nugget':
        # Uneven compact ore fragment, not a round coin or a finished ingot.
        d.polygon([(6,4),(9,4),(10,5),(12,5),(12,7),(13,8),(12,10),(10,10),(9,12),(6,12),(5,11),(3,11),(3,8),(4,7),(4,5)],fill='#455566')
        d.polygon([(6,5),(9,5),(10,6),(11,6),(11,8),(12,8),(11,9),(9,9),(8,11),(5,10),(4,10),(4,8),(5,7),(5,6)],fill='#aabcc8')
        d.polygon([(6,5),(8,5),(8,6),(10,6),(9,8),(6,9),(4,8),(5,7)],fill='#e3ecf0')
        d.line([(6,5),(8,5)],fill='#ffffff')
        d.point((5,7),fill='#ffffff');d.line([(10,8),(10,9)],fill='#8195aa')
        d.line([(6,11),(8,11)],fill='#788b9e');d.point((11,6),fill='#cadbe4')
    elif name == 'silver_ingot':
        # Long bevelled hexagonal ingot, silver-white planes and fine icy seams.
        d.polygon([(5,3),(12,3),(14,6),(12,11),(4,12),(1,9),(2,7)],fill='#465b72')
        d.polygon([(5,4),(11,4),(13,6),(10,9),(3,10),(2,9),(3,7)],fill='#cad9e3')
        d.polygon([(5,4),(11,4),(12,5),(10,7),(4,9),(3,8)],fill='#edf5f7')
        d.line([(5,4),(10,4)],fill='#ffffff')
        d.line([(3,9),(5,10),(11,9),(12,7)],fill='#a7bdcf')
        d.line([(5,11),(11,10)],fill='#829db6')
        # One pixel, sparse veins; the white top plane remains the material.
        d.line([(6,7),(7,6),(9,6)],fill='#b1dfe9')
        d.point((10,5),fill='#d3f4fa');d.point((5,8),fill='#b1dfe9')
    elif name == 'perunite':
        # Heavy irregular blue-steel billet, asymmetric top and deep side plane.
        d.polygon([(4,2),(10,2),(11,3),(13,3),(14,6),(13,11),(10,13),(3,12),(1,9),(2,5)],fill='#18243f')
        d.polygon([(4,3),(10,3),(11,4),(12,4),(13,6),(11,9),(4,10),(2,8),(3,5)],fill='#35518a')
        d.polygon([(4,3),(9,3),(10,4),(8,6),(3,7),(3,5)],fill='#526ca4')
        d.polygon([(4,10),(11,9),(13,7),(12,11),(10,12),(4,11)],fill='#25365f')
        d.line([(4,3),(8,3)],fill='#8ba9d1')
        d.line([(9,4),(8,6),(9,7),(8,9),(6,10)],fill='#259eac')
        d.line([(8,6),(6,6),(5,7)],fill='#55d9df')
        d.line([(9,7),(11,6)],fill='#63e5e5');d.point((8,8),fill='#a4f8ed')
        d.point((6,10),fill='#38b9c4');d.line([(3,9),(4,10)],fill='#446497')
    else:
        # 32px native sprite: thick unfinished forging, stepped ragged ends,
        # hammer flats and scale; no magic veins or polished ingot outline.
        d.polygon([(4,16),(7,14),(7,13),(14,11),(14,10),(22,8),(26,9),(26,11),(29,12),(28,15),(26,16),(26,18),(18,20),(13,21),(12,23),(7,23),(6,21),(3,20),(3,18)],fill='#242a32')
        d.polygon([(5,16),(9,14),(15,12),(22,10),(25,10),(25,12),(27,13),(25,15),(18,17),(12,19),(7,20),(5,19)],fill='#707c85')
        d.polygon([(7,16),(14,13),(22,11),(24,11),(23,13),(16,15),(10,17)],fill='#9ca7ae')
        d.polygon([(7,20),(12,19),(18,17),(25,15),(25,17),(18,19),(12,21),(8,22)],fill='#45515d')
        d.line([(9,15),(14,13)],fill='#bec5c8')
        d.line([(17,12),(21,11)],fill='#b2bbc0')
        d.line([(15,15),(18,14)],fill='#596670',width=2)
        d.line([(8,18),(10,18)],fill='#555e67');d.point((22,13),fill='#46515d')
        d.point((5,18),fill='#939ea5');d.line([(19,18),(22,17)],fill='#303c47')
        d.point((7,21),fill='#677780');d.point((26,13),fill='#909ca5')
    return im

def write_owned(path):
    """Replace only an exact owned texture target; leave all other paths alone."""
    from materials81_m97_t100 import write_owned as current_m97_t100
    if current_m97_t100(path): return True
    from materials81_n98_t99 import write_owned as current_natural_tips
    if current_natural_tips(path): return True
    from metals36_assets import write_owned as current_metal
    if current_metal(path): return True
    path = Path(path)
    if path.name not in {name+'.png' for name in OWNED} or not path.as_posix().endswith('/'+PREFIX+path.name):
        return False
    path.parent.mkdir(parents=True,exist_ok=True)
    render_asset(path.stem).save(path)
    return True

def apply(root):
    for name in OWNED:
        assert write_owned(Path(root)/'src/main/resources'/PREFIX/(name+'.png'))

if __name__ == '__main__':
    apply(Path(__file__).resolve().parents[1])
