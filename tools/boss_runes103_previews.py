"""Render actual saved before/after assets at identical 16px / nearest3x display.
Offline source evidence, never starts Minecraft or substitutes concept renders.
"""
from pathlib import Path
import json
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/boss-runes-103'
def main():
 rows=json.loads((O/'inventory.json').read_text(encoding='utf-8'))[:10]
 font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',13)
 out=Image.new('RGB',(1200,835),'#262d37');d=ImageDraw.Draw(out)
 d.text((10,8),'#103 ACTUAL PNG A/B: 16px slots and nearest x3 / OFFLINE, not Minecraft',font=font,fill='white')
 for i,row in enumerate(rows):
  x=i%2*600;y=i//2*160+35;d.text((x+12,y+4),row['id'],font=font,fill='white')
  for j,phase in enumerate(['before','after']):
   im=Image.open(O/phase/row['textures']).convert('RGBA')
   xx=x+20+j*270;d.text((xx,y+27),phase,font=font,fill='#aab9c7')
   for scale,dx in [(1,0),(3,40)]:
    img=im.resize((16*scale,16*scale),Image.Resampling.NEAREST)
    d.rectangle((xx+dx-2,y+54,xx+dx+16*scale+1,y+57+16*scale),fill='#8b8b8b');out.paste(img,(xx+dx,y+56),img)
   img=im.resize((48,48),Image.Resampling.NEAREST)
   d.rectangle((xx+110,y+52,xx+170,y+112),fill='#dfdbce');out.paste(img,(xx+116,y+58),img)
 out.save(O/'AB-10.png')
if __name__=='__main__':main()
