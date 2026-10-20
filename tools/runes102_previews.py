"""All28 actual PNGs, genuine pre-implementation baseline, 1x/3x slot previews."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/runes-102-full'
F=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',12)
def main():
 rows=json.loads((O/'inventory.json').read_text(encoding='utf-8'))
 out=Image.new('RGB',(1180,5*140+38),'#252c36');d=ImageDraw.Draw(out)
 d.text((10,8),'ALL 24 CRAFT RUNES + 4 BLANKS: actual native PNGs / OFFLINE source preview, not Minecraft',font=F,fill='white')
 groups=[('I',[r for r in rows if r['tier']==1 and not r['id'].startswith('blank')]),('II',[r for r in rows if r['tier']==2 and not r['id'].startswith('blank')]),('III',[r for r in rows if r['tier']==3 and not r['id'].startswith('blank')]),('IV',[r for r in rows if r['tier']==4 and not r['id'].startswith('blank')]),('BLANK',[r for r in rows if r['id'].startswith('blank')])]
 for y,(title,group) in enumerate(groups):
  yy=y*140+38;d.text((9,yy+35),title,font=F,fill='white')
  for x,row in enumerate(group):
   xx=x*140+55;im=Image.open(R/row['texture']).convert('RGBA');big=im.resize((64,64),Image.Resampling.NEAREST);out.paste(big,(xx+35,yy+5),big);small=im.resize((16,16),Image.Resampling.NEAREST);out.paste(small,(xx+8,yy+50),small);d.text((xx+5,yy+80),row['id'].removeprefix('rune_').replace('_','\n'),font=F,fill='white')
 out.save(O/'ALL-28.png')
 out=Image.new('RGB',(1080,4*220+30),'#252c36');d=ImageDraw.Draw(out)
 d.text((8,5),'ALL28 actual A/B: before1x/after1x, before3x/after3x, after on light background. OFFLINE.',font=F,fill='white')
 for i,row in enumerate(rows):
  x=i%7*154;y=i//7*220+32;d.text((x+5,y),row['id'].removeprefix('rune_'),font=F,fill='white')
  for phase,scale,xx,yy in [('before',1,30,29),('after',1,72,29),('before',3,20,65),('after',3,80,65)]:
   im=Image.open(O/'before'/row['texture'] if phase=='before' else R/row['texture']).convert('RGBA').resize((16*scale,16*scale),Image.Resampling.NEAREST);d.rectangle((x+xx-1,y+yy-1,x+xx+16*scale,y+yy+16*scale),fill='#8b8b8b');out.paste(im,(x+xx,y+yy),im)
  d.rectangle((x+8,y+128,x+145,y+190),fill='#d9d5cc');im=Image.open(R/row['texture']).convert('RGBA').resize((48,48),Image.Resampling.NEAREST);out.paste(im,(x+55,y+135),im)
 out.save(O/'AB-all-28.png')
if __name__=='__main__':main()
