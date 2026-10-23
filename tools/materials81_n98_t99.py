"""Original, deterministic pixel assets for #98 and #99 only; no gameplay writes."""
from pathlib import Path
from PIL import Image, ImageDraw

NATURAL=('birch_bark','animal_fat','goat_hide','large_animal_bone')
TIPS=tuple(m+s for m in ('stone','gold','iron','silver') for s in ('_spearhead','_light_spearhead'))
OWNED=NATURAL+TIPS
PREFIX='assets/slavicmyths/textures/item/'

def render(name):
    if name not in OWNED:raise ValueError(name)
    size=16 if name in ('birch_bark','large_animal_bone') else 32
    im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
    if name=='birch_bark':
        d.polygon([(3,2),(12,1),(13,3),(12,10),(14,12),(12,14),(3,14),(1,12),(2,5)],fill='#76533b')
        d.polygon([(3,3),(11,2),(12,4),(11,10),(12,12),(4,13),(2,11)],fill='#c4ad81')
        d.polygon([(4,3),(11,3),(10,10),(11,11),(4,12),(3,10)],fill='#e6dfc6')
        d.line([(4,3),(10,2)],fill='#f5edd8');d.line([(3,4),(3,10)],fill='#f6efd9')
        d.line((5,5,8,5),fill='#726456');d.line((8,8,10,8),fill='#80715e');d.line((4,10,5,10),fill='#9b8870')
        d.polygon([(2,11),(4,11),(5,12),(12,11),(11,13),(4,14),(2,13)],fill='#b58e5a')
        d.line((4,12,10,12),fill='#edd6a5')
    elif name=='animal_fat':
        d.polygon([(3,17),(5,11),(10,8),(13,9),(17,6),(23,8),(25,12),(28,15),(27,22),(22,26),(14,27),(8,25),(4,22)],fill='#9b8e79')
        d.polygon([(5,17),(7,12),(11,10),(15,12),(18,8),(22,10),(23,14),(26,16),(25,21),(20,24),(13,25),(7,22)],fill='#c5bba4')
        d.polygon([(6,16),(9,12),(12,12),(15,15),(19,10),(22,12),(21,16),(25,18),(20,21),(12,22),(7,20)],fill='#e9dfc6')
        d.polygon([(8,15),(10,13),(12,14),(15,17),(19,12),(20,12),(19,16),(16,20),(11,20)],fill='#fff3da')
        d.line([(7,22),(13,24),(20,23)],fill='#ad9f86',width=2)
        d.line([(20,18),(23,18),(24,17)],fill='#f2e9d5');d.line((9,17,11,18),fill='#cec2a9')
    elif name=='goat_hide':
        d.polygon([(13,3),(18,3),(20,7),(24,5),(28,8),(24,12),(24,19),(29,24),(26,28),(21,25),(16,27),(11,25),(6,29),(3,25),(7,19),(7,13),(3,10),(5,6),(10,8)],fill='#65503e')
        d.polygon([(14,5),(17,5),(19,10),(24,8),(25,9),(21,13),(22,20),(26,24),(25,25),(20,22),(16,24),(11,22),(6,26),(5,25),(10,20),(9,12),(6,9),(7,8),(12,11)],fill='#b19877')
        d.polygon([(13,10),(16,8),(19,12),(20,19),(17,22),(12,20),(11,14)],fill='#c8b18b')
        d.line([(15,12),(14,16),(16,20)],fill='#d7c39f');d.line([(18,12),(18,17),(20,20)],fill='#92795d')
        for x,y in ((10,14),(12,18),(20,15),(16,11),(11,21),(22,22),(8,23)):
            d.line((x,y,x+1,y+1),fill='#8e7559')
        d.line((14,5,17,5),fill='#d2bc97')
    elif name=='large_animal_bone':
        d.polygon([(1,10),(3,9),(4,10),(9,5),(9,3),(11,1),(13,2),(13,4),(15,5),(14,7),(12,7),(7,12),(7,14),(5,15),(3,14),(2,15),(0,13)],fill='#9e8663')
        d.polygon([(2,10),(4,11),(10,5),(10,3),(11,2),(12,3),(12,5),(14,5),(13,6),(11,6),(6,12),(6,14),(4,13),(2,14),(1,12)],fill='#e1cfaa')
        d.line([(3,11),(4,12),(11,5),(11,3)],fill='#fff0d0',width=1)
        d.point((2,12),fill='#fff0d0');d.point((12,5),fill='#f4e4be');d.line((5,13,6,13),fill='#bca27a')
    else:
        material=name.split('_')[0];light='_light_' in name
        palettes={'stone':('#3e4140','#676d69','#929a8c','#bec3ae'),
                  'gold':('#73501b','#ba8326','#eac34b','#fff099'),
                  'iron':('#3e484e','#6e7c82','#a8b5b8','#e4e7dc'),
                  'silver':('#536778','#8fa4b3','#cedbe3','#f3f5eb')}
        edge,shadow,base,shine=palettes[material]
        outlines={
          ('stone',False):[(7,28),(7,23),(4,20),(8,16),(8,12),(13,12),(18,7),(26,3),(25,10),(22,16),(18,21),(14,22),(12,28)],
          ('stone',True):[(10,29),(11,23),(10,20),(15,13),(17,8),(27,2),(24,10),(20,17),(16,22),(14,29)],
          ('gold',False):[(5,27),(7,21),(5,16),(10,10),(19,7),(26,4),(24,12),(21,20),(16,24),(12,24),(10,29)],
          ('gold',True):[(10,29),(13,22),(12,18),(17,10),(27,2),(25,11),(21,18),(17,23),(15,30)],
          ('iron',False):[(5,27),(8,22),(5,18),(10,12),(17,7),(26,3),(23,12),(19,19),(14,23),(12,23),(10,29)],
          ('iron',True):[(10,29),(13,22),(12,19),(16,11),(27,2),(25,10),(22,16),(17,23),(15,30)],
          ('silver',False):[(6,28),(8,22),(7,17),(12,11),(20,7),(27,3),(24,12),(20,18),(15,22),(13,23),(11,29)],
          ('silver',True):[(11,30),(13,23),(14,18),(18,10),(28,2),(25,11),(21,17),(18,23),(16,30)]}
        pts=outlines[material,light];d.polygon(pts,fill=edge)
        if light:
            d.polygon([(14,23),(14,19),(18,11),(25,5),(23,12),(19,19),(16,24)],fill=base)
            d.line([(14,22),(19,13),(25,5)],fill=shine)
            d.line([(17,23),(20,18),(23,13)],fill=shadow)
            d.polygon([(12,27),(14,24),(16,25),(14,29)],fill=shadow)
        else:
            d.polygon([(8,18),(12,12),(19,9),(24,6),(22,13),(17,19),(12,22)],fill=base)
            d.polygon([(12,22),(17,18),(22,12),(24,6),(20,16),(16,21)],fill=shadow)
            d.line([(8,19),(15,14),(24,6)],fill=shine)
            d.polygon([(7,26),(9,23),(11,24),(9,28)],fill=shadow)
            d.line([(9,24),(10,25)],fill=base)
        if material=='stone':
            for a,b in (((10,18),(14,17)),((15,12),(18,12)),((20,8),(23,7))):d.line((a,b),fill=shadow)
            d.point((17,15),fill=shine);d.point((21,11),fill=edge)
        elif material=='gold':
            d.line([(17,13),(20,11),(21,9)],fill='#ffdf70');d.point((22,7),fill='#fff7c3')
        elif material=='silver':
            d.line([(17,16),(20,12),(23,8)],fill='#e3f0f4')
            d.point((20,11),fill='#ffffff')
    return im

def write_owned(path):
    path=Path(path)
    if path.stem not in OWNED or not path.as_posix().endswith('/'+PREFIX+path.name):return False
    path.parent.mkdir(parents=True,exist_ok=True)
    render(path.stem).save(path,compress_level=9)
    return True

def apply(root,names=OWNED):
    for name in names:
        assert write_owned(Path(root)/'src/main/resources'/PREFIX/(name+'.png'))

if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser();parser.add_argument('--group',choices=('natural','stone-gold','iron-silver','all'),default='all')
    args=parser.parse_args()
    names={'natural':NATURAL,'stone-gold':TIPS[:4],'iron-silver':TIPS[4:],'all':OWNED}[args.group]
    apply(Path(__file__).resolve().parents[1],names)
