"""Individual #114 firebird down design; retained native64 texture, no other assets."""
from pathlib import Path
from PIL import Image
ROWS=(
'................',
'.........s......',
'......s..sh.....',
'....s.s.hhs.....',
'....shshlh.s....',
'...shhlhlhhs....',
'..shhlhlhlhs.s..',
'...shhlllhhhs...',
'..shhlllllhhs...',
'...shhlllhhs....',
'....shllhhs.....',
'.....shlhs......',
'......shs.......',
'......hs........',
'.....hs.........',
'................')
COLORS={'.':(0,0,0,0),'s':(192,102,34,255),'h':(239,166,65,255),'l':(255,224,145,255)}

def image(size=64):
    assert size in (16,64)
    im=Image.new('RGBA',(size,size));scale=size//16
    for y,row in enumerate(ROWS):
        assert len(row)==16
        for x,key in enumerate(row):
            for dy in range(scale):
                for dx in range(scale):im.putpixel((x*scale+dx,y*scale+dy),COLORS[key])
    return im

def write_owned(path):
    p=Path(path)
    if not p.as_posix().endswith('/assets/slavicmyths/textures/item/firebird_down.png'):return False
    p.parent.mkdir(parents=True,exist_ok=True);image().save(p);return True

def apply(root):
    assert write_owned(Path(root)/'src/main/resources/assets/slavicmyths/textures/item/firebird_down.png')

if __name__=='__main__':apply(Path(__file__).resolve().parents[1])
