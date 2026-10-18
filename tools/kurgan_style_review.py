"""Author three native 16px pixel-cluster prototypes outside production resources.

Shapes and palettes are drawn at target resolution. Original PNGs are read only
for comparison/checksums; no resampling feeds the new textures.
"""
from pathlib import Path
import hashlib
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT/'src/main/resources/assets/slavicmyths/textures/block'
OUT = ROOT/'art/review/kurgan-vanilla-style'
NAMES = ['bog_green_kurgan_stone', 'bog_green_kurgan_cobblestone',
         'cracked_bog_green_kurgan_cobblestone', 'mossy_bog_green_kurgan_cobblestone',
         'pale_blue_kurgan_stone', 'pale_blue_kurgan_stone_variant', 'pale_blue_kurgan_cobblestone',
         'cracked_pale_blue_kurgan_cobblestone', 'mossy_pale_blue_kurgan_cobblestone',
         'burial_stone_top', 'carved_burial_stone']


def wrap(im, primitive, points, color, width=1):
    d = ImageDraw.Draw(im)
    for dx in (-16, 0, 16):
        for dy in (-16, 0, 16):
            pts = [(x+dx, y+dy) for x, y in points]
            if primitive == 'polygon': d.polygon(pts, fill=color)
            else: d.line(pts, fill=color, width=width)


def author():
    # Olive boulders: large angular facets, sparse moss in the seams.
    im = Image.new('RGB', (16, 16), '#30352b')
    rocks = [([(-3,0),(3,-2),(6,1),(5,5),(1,7),(-3,4)], '#64694e'),
             ([(7,0),(12,-2),(16,1),(14,5),(9,5),(7,3)], '#5b614b'),
             ([(4,7),(8,4),(11,7),(10,12),(7,14),(3,11)], '#70755a'),
             ([(12,7),(16,5),(19,9),(17,13),(13,13),(11,10)], '#5c634c'),
             ([(-2,10),(2,9),(3,13),(1,17),(-3,16)], '#72765a'),
             ([(4,15),(8,14),(12,15),(13,18),(6,20)], '#5c624b')]
    for pts, color in rocks: wrap(im, 'polygon', pts, color)
    for pts in [[(-2,0),(2,-1),(4,1)],[(5,7),(8,5),(10,7)],[(12,8),(15,6),(17,8)],[(0,10),(1,10),(2,12)]]:
        wrap(im, 'line', pts, '#87896b')
    for pts in [[(0,5),(1,6),(2,6)],[(9,13),(10,13),(11,14)],[(13,4),(14,4)]]:
        wrap(im, 'line', pts, '#4c5833')
    result = {'bog_green_kurgan_stone': im}
    # Blue cobblestone retains the original offset courses, now four broad stones.
    im = Image.new('RGB', (16,16), '#263942')
    for x,y,w,h,color in [(0,0,7,7,'#526b78'),(8,0,7,7,'#4a6370'),(-4,8,11,7,'#4d6774'),(8,8,11,7,'#5b7480')]:
        wrap(im,'polygon',[(x+1,y),(x+w-1,y),(x+w,y+1),(x+w,y+h-1),(x+w-1,y+h),(x+1,y+h),(x,y+h-1),(x,y+1)],color)
        wrap(im,'line',[(x+1,y+1),(x+w-2,y+1)],'#718995')
        wrap(im,'line',[(x+1,y+2),(x+1,y+3)],'#647f8b')
        wrap(im,'line',[(x+w-1,y+3),(x+w-1,y+h-1),(x+3,y+h-1)],'#3e5764')
        wrap(im,'polygon',[(x+2,y+3),(x+4,y+2),(x+5,y+4),(x+3,y+5)],'#587380')
    result['pale_blue_kurgan_cobblestone'] = im
    # Carved stone: broad rectangular labyrinth cuts and a central recessed square.
    im = Image.new('RGB',(16,16),'#596b70');d=ImageDraw.Draw(im)
    d.rectangle((1,1,14,14),outline='#819095');d.rectangle((2,2,13,13),outline='#384b52')
    for pts in [[(3,6),(3,3),(6,3),(6,5)],[(9,3),(12,3),(12,6),(10,6)],
                [(12,9),(12,12),(9,12),(9,10)],[(6,12),(3,12),(3,9),(5,9)]]:
        d.line([(x,y+1) for x,y in pts],fill='#819095',width=1)
        d.line(pts,fill='#253b44',width=1)
    d.rectangle((6,6,9,9),fill='#8b989a');d.rectangle((7,7,8,8),fill='#2d424b')
    for x,y in [(0,4),(15,11),(4,15),(11,0)]:d.point((x,y),fill='#3f555e')
    result['carved_burial_stone']=im
    return result


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    textures=author()
    for name,im in textures.items():im.save(OUT/(name+'.png'))
    font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',18)
    small=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',15)
    sheet=Image.new('RGB',(1100,1090),'#20252a');d=ImageDraw.Draw(sheet)
    d.text((22,12),'REAL PNG COMPARISON - review only, no Minecraft render',font=font,fill='white')
    for row,(name,im) in enumerate(textures.items()):
        y=55+row*340
        d.text((22,y),name,font=font,fill='#eee4cb')
        original=Image.open(SRC/(name+'.png')).convert('RGB');assert original.size==(256,256)
        sheet.paste(original,(22,y+30))
        sheet.paste(im.resize((256,256),Image.Resampling.NEAREST),(310,y+30))
        for tx,x in [('original 256px, 1x',22),('new 16px, 16x',310)]:d.text((x,y+292),tx,font=small,fill='#cbd1d6')
        d.text((600,y+30),'New: 1x / 4x',font=small,fill='white')
        sheet.paste(im,(600,y+62));sheet.paste(im.resize((64,64),Image.Resampling.NEAREST),(636,y+62))
        reference=['silver_ore','silver_ore','wall_carving'][row]
        ref=Image.open(SRC/(reference+'.png')).convert('RGB')
        d.text((740,y+30),'Existing mod: '+reference,font=small,fill='white')
        sheet.paste(ref.resize((128,128),Image.Resampling.NEAREST),(740,y+62))
        tiled=Image.new('RGB',(48,48))
        for xx in range(3):
            for yy in range(3):tiled.paste(im,(xx*16,yy*16))
        sheet.paste(tiled.resize((144,144),Image.Resampling.NEAREST),(600,y+155))
        d.text((755,y+224),'3x3 tile repeat at left',font=small,fill='#cbd1d6')
    sheet.save(OUT/'before-after.png')
    # All eleven source textures, at their real native resolution for inspection.
    originals=Image.new('RGB',(1120,915),'#20252a');d=ImageDraw.Draw(originals)
    for i,name in enumerate(NAMES):
        x=10+(i%4)*280;y=10+(i//4)*305
        originals.paste(Image.open(SRC/(name+'.png')).convert('RGB'),(x,y))
        words=name.replace('_',' ')
        d.text((x,y+260),words[:31],font=small,fill='white')
        d.text((x,y+277),words[31:],font=small,fill='white')
    originals.save(OUT/'originals-11.png')
    sources={n:dict(size=list(Image.open(SRC/(n+'.png')).size),sha256=hashlib.sha256((SRC/(n+'.png')).read_bytes()).hexdigest()) for n in NAMES}
    manifest=dict(status='AWAITING_OWNER_APPROVAL',sources=sources,
                  prototypes={n:dict(size=[16,16],colors=len(im.getcolors()),sha256=hashlib.sha256((OUT/(n+'.png')).read_bytes()).hexdigest()) for n,im in textures.items()},
                  method='Authored polygons and pixel clusters at 16x16; no source resizing used to create prototypes.',
                  production_textures_changed=False,client_starts=0)
    (OUT/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf8')
    print('Three native16 prototypes and real-PNG comparisons ready:',OUT)


if __name__=='__main__':main()
