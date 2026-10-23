"""Actual before/after PNGs: fixed 16px slot approximation and native nearest zoom."""
from pathlib import Path
from PIL import Image,ImageDraw
import materials81_n98_t99 as art

root=Path(__file__).resolve().parents[1]
out=root/'docs/implementation/materials-81/N98-T99'
for label,names in [('natural',art.NATURAL),('stone-gold',art.TIPS[:4]),('iron-silver',art.TIPS[4:])]:
    board=Image.new('RGB',(1200,470),'#252b34');draw=ImageDraw.Draw(board)
    draw.text((10,8),f'#{98 if label=="natural" else 99} {label}: actual PNG A/B. OFFLINE source preview, NOT Minecraft screenshots.',fill='white')
    draw.text((10,25),'16px slot approximation / slot x3 nearest / native x3 nearest; original source resolution preserved.',fill='#b5bec9')
    for col,name in enumerate(names):
        path=Path(f'src/main/resources/{art.PREFIX}{name}.png')
        for row,prefix in enumerate((out/'before',root)):
            pic=Image.open(prefix/path).convert('RGBA');x=col*300;y=60+row*200
            draw.text((x+8,y),name,fill='white');draw.text((x+8,y+17),('BEFORE' if row==0 else 'AFTER')+' '+str(pic.size),fill='#b5bec9')
            slot=pic.resize((16,16),Image.Resampling.NEAREST)
            for image,dx in [(slot,12),(slot.resize((48,48),Image.Resampling.NEAREST),44),(pic.resize((pic.width*3,pic.height*3),Image.Resampling.NEAREST),110)]:
                board.paste(image,(x+dx,y+50),image)
    board.save(out/('AB-'+label+'.png'))

sheet=Image.new('RGB',(1000,360),'#252b34');draw=ImageDraw.Draw(sheet)
draw.text((8,8),'#99 all eight current components / offline, same slot scale / ordinary vs light',fill='white')
for i,name in enumerate(art.TIPS):
    pic=Image.open(root/f'src/main/resources/{art.PREFIX}{name}.png').convert('RGBA')
    x=(i//2)*250;y=(i%2)*155+35;draw.text((x+5,y),name,fill='white')
    slot=pic.resize((16,16),Image.Resampling.NEAREST);sheet.paste(slot,(x+8,y+24),slot)
    zoom=pic.resize((96,96),Image.Resampling.NEAREST);sheet.paste(zoom,(x+72,y+24),zoom)
sheet.save(out/'all-eight.png')
