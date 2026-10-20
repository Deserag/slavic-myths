"""Actual meat PNG A/B.

No client, world, invented before or concept replacement. Not a game screenshot.
"""
from pathlib import Path
import math,json,zipfile
from PIL import Image,ImageDraw,ImageFont
import meat83_assets as art
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/meat-83'
FONT=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',12)
def texture(name,phase):
 folder='item'
 rel=Path(f'src/main/resources/assets/slavicmyths/textures/{folder}/{name}.png')
 return Image.open(O/'before'/rel if phase=='before' else R/rel).convert('RGBA')
def slots():
 names=['raw_goose','raw_duck','raw_goat','raw_boar_meat','raw_venison','raw_bear_meat'];out=Image.new('RGB',(940,350),'#252932');d=ImageDraw.Draw(out)
 d.text((12,5),'#83 ACTUAL PNG before/after, OFFLINE GUI-slot preview (not Minecraft)',font=FONT,fill='white')
 for i,n in enumerate(names):d.text((113+i*136,30),n.replace('_','\n'),font=FONT,fill='white')
 for phase,scale,y in [('before',1,75),('after',1,108),('before',4,154),('after',4,244)]:
  d.text((12,y+4),phase+' '+str(scale)+'x',font=FONT,fill='white')
  for i,n in enumerate(names):
   x=113+i*136;w=18*scale;d.rectangle((x,y,x+w-1,y+w-1),fill='#8b8b8b');d.line([(x,y+w-1),(x,y),(x+w-1,y)],fill='#373737',width=scale);d.line([(x+w-1,y),(x+w-1,y+w-1),(x,y+w-1)],fill='white',width=scale)
   im=texture(n,phase).resize((16*scale,16*scale),Image.Resampling.NEAREST);out.paste(im,(x+scale,y+scale),im)
 out.save(O/'AB-slots.png')

if __name__=='__main__':slots()
