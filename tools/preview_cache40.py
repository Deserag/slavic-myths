"""Offline native JSON/UV/PNG projection, no Minecraft runtime."""
from pathlib import Path
import json,math
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/creative-cache-40'
m=json.loads((R/'src/main/resources/assets/slavicmyths/models/item/sanctuary_ritual_cache.json').read_text());tex=Image.open(R/'src/main/resources/assets/slavicmyths/textures/item/sanctuary_ritual_cache.png').convert('RGBA')
im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
def project(v):
 x,y,z=v;return (round(64+(x+z-16)*3),round(91+(x-z)*1.7-y*4))
for el in m['elements']:
 a,b=el['from'],el['to'];x,y,z=a;X,Y,Z=b
 for f,corners in [('up',[(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)]),('east',[(X,Y,z),(X,Y,Z),(X,y,Z),(X,y,z)]),('north',[(x,Y,z),(X,Y,z),(X,y,z),(x,y,z)])]:
  u,v,U,V=el['faces'][f]['uv'];w=max(1,round((U-u)*2));h=max(1,round((V-v)*2))
  for j in range(h):
   for i in range(w):
    def point(s,t):return [corners[0][k]+s*(corners[1][k]-corners[0][k])+t*(corners[3][k]-corners[0][k]) for k in range(3)]
    color=tex.getpixel((min(31,int(u*2+i)),min(31,int(v*2+j))));shade=1 if f=='up' else .8 if f=='north' else .6;color=tuple(round(c*shade) for c in color[:3])+(color[3],)
    d.polygon([project(point(i/w,j/h)),project(point((i+1)/w,j/h)),project(point((i+1)/w,(j+1)/h)),project(point(i/w,(j+1)/h))],fill=color)
im.save(O/'native-projection.png')
out=Image.new('RGB',(600,240),'#28313a');d=ImageDraw.Draw(out);font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',14)
d.text((12,12),'Actual item JSON / UV / PNG: OFFLINE projection, not Minecraft',fill='white',font=font)
d.text((12,42),'Before: builtin entity item -> no item-specific renderer',fill='white',font=font)
d.text((12,68),'After: closed cache, 3 cuboids, ordinary baked item model',fill='white',font=font)
for size,x in [(16,35),(48,90),(128,200)]:
 small=im.resize((size,size),Image.Resampling.NEAREST);out.paste(small,(x,100),small)
out.save(O/'PREVIEW.png')
