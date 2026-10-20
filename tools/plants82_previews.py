"""Actual PNG A/B and offline projection of the installed vanilla cross model.

No client, world, invented before or concept replacement. Not a game screenshot.
"""
from pathlib import Path
import math,json,zipfile
from PIL import Image,ImageDraw,ImageFont
import plants82_assets as art
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/plants-82'
FONT=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',12)
def texture(name,phase):
 folder='item' if name=='cabbage' else 'block'
 rel=Path(f'src/main/resources/assets/slavicmyths/textures/{folder}/{name}.png')
 return Image.open(O/'before'/rel if phase=='before' else R/rel).convert('RGBA')
def slots():
 names=[s+'_sapling' for s in art.TREES]+['cabbage'];out=Image.new('RGB',(940,350),'#252932');d=ImageDraw.Draw(out)
 d.text((12,5),'#82 ACTUAL PNG before/after, OFFLINE GUI-slot preview (not Minecraft)',font=FONT,fill='white')
 for i,n in enumerate(names):d.text((113+i*136,30),n.replace('_','\n'),font=FONT,fill='white')
 for phase,scale,y in [('before',1,75),('after',1,108),('before',4,154),('after',4,244)]:
  d.text((12,y+4),phase+' '+str(scale)+'x',font=FONT,fill='white')
  for i,n in enumerate(names):
   x=113+i*136;w=18*scale;d.rectangle((x,y,x+w-1,y+w-1),fill='#8b8b8b');d.line([(x,y+w-1),(x,y),(x+w-1,y)],fill='#373737',width=scale);d.line([(x+w-1,y),(x+w-1,y+w-1),(x,y+w-1)],fill='white',width=scale)
   im=texture(n,phase).resize((16*scale,16*scale),Image.Resampling.NEAREST);out.paste(im,(x+scale,y+scale),im)
 out.save(O/'AB-slots.png')

def placed():
 p=next((R/'build').rglob('*minecraft-resources.jar'))
 with zipfile.ZipFile(p) as z:cross=json.loads(z.read('assets/minecraft/models/block/cross.json'))
 (O/'actual-vanilla-cross.json').write_text(json.dumps(cross,indent=2)+'\n',encoding='utf-8')
 out=Image.new('RGB',(5*180+110,4*176+44),'#252932');d=ImageDraw.Draw(out)
 d.text((10,6),'OFFLINE source cross quads + actual PNGs. Fixed camera/light; NOT an in-game placement test.',font=FONT,fill='white')
 for col,s in enumerate(art.TREES):d.text((115+col*180,27),s+'_sapling',font=FONT,fill='white')
 for row,(phase,yaw) in enumerate([('before',25),('after',25),('before',115),('after',115)]):
  y0=50+row*176;d.text((6,y0+20),phase+' / '+str(yaw)+'deg',font=FONT,fill='white')
  for col,s in enumerate(art.TREES):
   im=texture(s+'_sapling',phase);width,height=im.size;quads=[];cx=200+col*180;cy=y0+145
   def project(point):
    x,y,z=point;x-=8;z-=8;a=math.radians(yaw);px=x*math.cos(a)-z*math.sin(a);pz=x*math.sin(a)+z*math.cos(a);b=math.radians(18)
    return (cx+px*5.6,cy-(y*math.cos(b)-pz*math.sin(b))*5.6,pz*math.cos(b)+y*math.sin(b))
   floor=[project([x,0,z])[:2] for x,z in [(0,0),(16,0),(16,16),(0,16)]];d.polygon(floor,fill='#4b5e3f',outline='#6e7c54')
   for el in cross['elements']:
    fr,to=el['from'],el['to'];rotation=el['rotation'];origin=rotation['origin'];a=math.radians(rotation['angle']);factor=1/math.cos(a) if rotation['rescale'] else 1
    def vertex(u,v):
     if fr[2]==to[2]:x=fr[0]+u*(to[0]-fr[0]);z=fr[2]
     else:x=fr[0];z=fr[2]+u*(to[2]-fr[2])
     y=to[1]-v*(to[1]-fr[1]);x=(x-origin[0])*factor;z=(z-origin[2])*factor
     return project([origin[0]+x*math.cos(a)-z*math.sin(a),y,origin[2]+x*math.sin(a)+z*math.cos(a)])
    for py in range(height):
     for px in range(width):
      color=im.getpixel((px,py))
      if color[3]<128:continue
      points=[vertex(u/width,v/height) for u,v in [(px,py),(px+1,py),(px+1,py+1),(px,py+1)]]
      quads.append((sum(p[2] for p in points)/4,[p[:2] for p in points],color[:3]))
   for _,points,color in sorted(quads,key=lambda x:x[0]):d.polygon(points,fill=color)
 out.save(O/'AB-cross-offline.png')
if __name__=='__main__':slots();placed()
