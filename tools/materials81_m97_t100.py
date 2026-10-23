"""Scoped original pixel art: amber face and three paired tip profiles."""
from pathlib import Path
from PIL import Image, ImageDraw

OWNED=('block/amber_block',)+tuple('item/'+m+s for m in ('diamond','perunite','netherite') for s in ('_spearhead','_light_spearhead'))

def render(name):
    assert name in OWNED
    if name=='block/amber_block':
        im=Image.new('RGBA',(16,16),'#b76518');d=ImageDraw.Draw(im)
        # Continuous resin strata, deliberately no cube drawn inside a face.
        d.polygon([(0,0),(15,0),(15,3),(11,2),(7,4),(2,3),(0,5)],fill='#e4a638')
        d.polygon([(0,6),(4,4),(9,5),(15,3),(15,7),(11,9),(6,8),(0,10)],fill='#cf841f')
        d.polygon([(0,11),(5,9),(10,11),(15,8),(15,12),(11,14),(5,12),(0,15)],fill='#e9ae42')
        d.line([(0,3),(4,2),(8,3),(12,1),(15,2)],fill='#f6cf70')
        d.line([(0,9),(5,7),(10,8),(15,6)],fill='#f0bc53')
        d.line([(0,15),(4,13),(9,15),(15,13)],fill='#8f461b')
        d.polygon([(8,5),(10,4),(12,5),(11,7),(9,7)],fill='#9d501d')
        d.line([(9,5),(10,5),(10,6)],fill='#60361c')
        d.point((3,11),fill='#ffe29a');d.line((12,11,13,10),fill='#f9d47e')
        return im
    material=name.split('/')[1].split('_')[0];light='_light_' in name
    palettes={'diamond':('#174e59','#277d87','#55c4c7','#c0f5e7'), 'perunite':('#111e39','#243f6c','#426292','#7496c4'), 'netherite':('#252125','#443c40','#625757','#92817a')}
    edge,shadow,base,shine=palettes[material]
    profiles={
        ('diamond',False):[(6,28),(9,22),(6,17),(13,10),(19,9),(26,3),(25,12),(19,21),(14,23),(11,29)],
        ('diamond',True):[(11,29),(14,22),(13,18),(19,9),(28,2),(24,13),(19,20),(17,24),(15,30)],
        ('perunite',False):[(5,27),(8,21),(7,15),(13,10),(26,3),(23,14),(20,20),(14,24),(10,29)],
        ('perunite',True):[(10,29),(13,23),(14,16),(20,8),(27,2),(24,12),(20,19),(17,24),(15,30)],
        ('netherite',False):[(4,27),(8,21),(5,18),(9,12),(17,9),(25,4),(24,12),(21,19),(15,24),(11,24),(9,29)],
        ('netherite',True):[(10,29),(13,22),(12,18),(17,10),(26,3),(24,12),(20,18),(17,23),(15,30)]}
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);d.polygon(profiles[material,light],fill=edge)
    if light:
        d.polygon([(14,22),(15,18),(20,10),(25,6),(22,14),(18,21),(16,24)],fill=base)
        d.line([(14,22),(20,12),(25,6)],fill=shine)
        d.line([(17,23),(20,18),(23,13)],fill=shadow)
        d.polygon([(12,27),(14,24),(16,25),(14,29)],fill=shadow)
    else:
        d.polygon([(8,18),(13,12),(20,9),(24,6),(22,14),(17,21),(12,23)],fill=base)
        d.polygon([(12,22),(17,18),(24,6),(21,17),(16,22)],fill=shadow)
        d.line([(8,18),(15,13),(24,6)],fill=shine)
        d.polygon([(7,26),(9,23),(11,24),(9,28)],fill=shadow)
    if material=='diamond':
        d.line([(16,15),(18,15),(20,12)],fill='#91e6dc');d.line([(19,11),(20,13),(22,13)],fill='#296f80')
    elif material=='perunite':
        d.line([(17,16),(18,13),(21,12),(21,10)],fill='#3bc5d0');d.point((19,13),fill='#9cf0e6')
    else:
        d.line([(15,17),(17,16)],fill='#ab9788');d.line([(20,10),(22,8)],fill='#746760')
    return im

def write_owned(path):
    p=Path(path)
    name=next((n for n in OWNED if p.as_posix().endswith('/assets/slavicmyths/textures/'+n+'.png')),None)
    if name is None:return False
    p.parent.mkdir(parents=True,exist_ok=True);render(name).save(p,compress_level=9);return True

def apply(root):
    for n in OWNED:assert write_owned(Path(root)/'src/main/resources/assets/slavicmyths/textures'/(n+'.png'))

if __name__=='__main__':apply(Path(__file__).resolve().parents[1])
