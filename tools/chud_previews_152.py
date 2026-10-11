"""Cutaway diagrams of the exact native-test block fills. These are never gameplay screenshots."""
from pathlib import Path
import csv
import gzip
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/verification/chud-1.5.2/previews'
OUT.mkdir(parents=True, exist_ok=True)
font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 25)
small = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 17)
zones = [
    ('01_ruined_quarter','Разрушенный жилой квартал','INDEPENDENT_GREAT',-117,-37,-90,0,78,113),
    ('02_mine_gallery','Главная шахтная галерея','INDEPENDENT_GREAT',-99,-35,32,108,64,97),
    ('03_vertical_shaft','Колодец с четырьмя кольцами','INDEPENDENT_GREAT',40,96,-79,-31,24,101),
    ('04_deep_labyrinth','Глубокий лабиринт','INDEPENDENT_GREAT',6,113,0,125,24,33),
    ('05_sealed_gate','Запечатанные нижние врата','GATE',-18,28,-127,-83,24,47),
    ('06_attached_small','Малая пристройка и переход','ATTACHED_SMALL',-114,-10,-125,-43,10,36),
]

def color(name):
    if 'lantern' in name: return (239,177,72)
    if 'magma' in name: return (199,80,31)
    if 'spruce' in name: return (115,83,53)
    if 'dark_oak' in name: return (78,58,42)
    if 'deepslate' in name or 'basalt' in name: return (68,69,72)
    if 'iron' in name or 'chain' in name or 'anvil' in name: return (96,102,104)
    if 'glass' in name: return (142,176,178)
    if 'azalea' in name or 'moss' in name: return (84,113,60)
    if 'bed' in name or 'banner' in name: return (131,91,58)
    if 'hay' in name: return (168,137,55)
    if 'kelp' in name: return (93,101,68)
    if 'tuff' in name: return (120,124,110)
    if 'polished_andesite' in name: return (157,157,154)
    if 'stone_bricks' in name: return (133,137,138)
    return (124,128,129)

for file,title,source,x0,x1,z0,z1,bottom,top in zones:
    names = ['minecraft:air']
    values = np.zeros((279,160,279), dtype=np.uint16)
    with gzip.open(ROOT / f'work/chud-152/preview-{source}.csv.gz', 'rt') as stream:
        for row in csv.reader(stream):
            x,y,z,xx,yy,zz = map(int,row[:6]); block=row[6]
            if block not in names: names.append(block)
            values[x+139:xx+140,y:yy+1,z+139:zz+140] = names.index(block)
    natural = {names.index(n) for n in names if n in {'minecraft:stone','minecraft:tuff','minecraft:andesite'}}
    grid = values[x0+139:x1+140,bottom-1:top+1,z0+139:z1+140].copy()
    cx=(x1-x0)//2;cz=(z1-z0)//2
    # Open the two foreground cavern walls so the diagram reveals the actual interior.
    for i in natural:
        mask=(grid==i)
        mask[:,:3,:]=False
        mask[:cx+3,:,:cz+3]=False
        grid[mask]=0
    scale=min(5.7,920/(grid.shape[0]+grid.shape[2]))
    canvas=Image.new('RGB',(1200,870),(29,34,38))
    draw=ImageDraw.Draw(canvas)
    origin_x=600-(grid.shape[0]-grid.shape[2])*scale/2
    origin_y=735-(grid.shape[0]+grid.shape[2])*scale/3
    def project(x,y,z):return(origin_x+(x-z)*scale,origin_y+(x+z)*scale*.5-y*scale)
    occupied=np.argwhere(grid!=0)
    ordered=sorted(occupied,key=lambda p:(int(p[0])+int(p[2]),int(p[1])))
    for x,y,z in ordered:
        block=int(grid[x,y,z]);base=color(names[block])
        def face(points,factor):draw.polygon([project(*p) for p in points],fill=tuple(int(c*factor) for c in base),outline=tuple(int(c*.66) for c in base))
        if x==grid.shape[0]-1 or not grid[x+1,y,z]:
            face([(x+1,y,z),(x+1,y,z+1),(x+1,y+1,z+1),(x+1,y+1,z)],.70)
        if z==grid.shape[2]-1 or not grid[x,y,z+1]:
            face([(x,y,z+1),(x+1,y,z+1),(x+1,y+1,z+1),(x,y+1,z+1)],.84)
        if y==grid.shape[1]-1 or not grid[x,y+1,z]:
            face([(x,y+1,z),(x+1,y+1,z),(x+1,y+1,z+1),(x,y+1,z+1)],1.0)
    draw.text((36,26),title,font=font,fill=(233,225,206))
    draw.text((36,65),'Срез блочного плана — не игровой скриншот; потолок частично снят.',font=small,fill=(186,194,197))
    draw.text((36,828),'Generation-v2 · поворот 0 · ванильные блоки · ступени и ограды показаны условными кубами',font=small,fill=(186,194,197))
    canvas.save(OUT / (file+'.png'))
print('6 cutaway previews written to',OUT)
