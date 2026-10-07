"""Offline orthographic JSON asset preview; NOT a Minecraft renderer or gameplay test."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageChops,ImageEnhance
import math,json
A=Path('src/main/resources/assets/slavicmyths');O=Path('docs/verification/rc2-ui-visual');O.mkdir(parents=True,exist_ok=True)
canvas=Image.new('RGBA',(800,260),(235,220,185,255));d=ImageDraw.Draw(canvas)
def rotate(v,yaw):
 x,y,z=v;t=math.radians(yaw);x,z=x*math.cos(t)+z*math.sin(t),-x*math.sin(t)+z*math.cos(t);t=math.radians(15);return x,y*math.cos(t)-z*math.sin(t),y*math.sin(t)+z*math.cos(t)
for col,(name,yaw) in enumerate([('club',-25),('battle_axe',-25),('retainer_shield',180),('retainer_shield',0)]):
 m=json.loads((A/'models/item'/f'{name}.json').read_text());tex=Image.open(A/'textures'/f'{m["textures"]["0"].split(":")[1]}.png').convert('RGBA');faces=[]
 for e in m['elements']:
  x0,y0,z0=e['from'];x1,y1,z1=e['to']
  verts={'south':[(x0,y1,z1),(x1,y1,z1),(x1,y0,z1),(x0,y0,z1)],'north':[(x1,y1,z0),(x0,y1,z0),(x0,y0,z0),(x1,y0,z0)],'west':[(x0,y1,z0),(x0,y1,z1),(x0,y0,z1),(x0,y0,z0)],'east':[(x1,y1,z1),(x1,y1,z0),(x1,y0,z0),(x1,y0,z1)],'up':[(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)],'down':[(x0,y0,z1),(x1,y0,z1),(x1,y0,z0),(x0,y0,z0)]}
  normals={'north':(0,0,-1),'south':(0,0,1),'west':(-1,0,0),'east':(1,0,0),'up':(0,1,0),'down':(0,-1,0)}
  for side,points in verts.items():
   normal=rotate(normals[side],yaw)
   if normal[2]<=.001:continue
   r=[rotate(tuple(p[i]-8 for i in range(3)),yaw) for p in points];screen=[(100+p[0]*11,132-p[1]*11) for p in r];faces.append((sum(p[2] for p in r)/4,screen,e['faces'][side]['uv'],.65+.35*normal[2]))
 tile=Image.new('RGBA',(200,250),(0,0,0,0))
 for depth,p,uv,shade in sorted(faces):
  x,y=p[0];ax,ay=p[1][0]-x,p[1][1]-y;bx,by=p[3][0]-x,p[3][1]-y;det=ax*by-ay*bx
  if abs(det)<.01:continue
  u0,v0,u1,v1=uv;u0*=tex.width/16;u1*=tex.width/16;v0*=tex.height/16;v1*=tex.height/16
  aa=(u1-u0)*by/det;ab=-(u1-u0)*bx/det;ba=-(v1-v0)*ay/det;bb=(v1-v0)*ax/det
  face=tex.transform(tile.size,Image.Transform.AFFINE,(aa,ab,u0-aa*x-ab*y,ba,bb,v0-ba*x-bb*y),Image.Resampling.NEAREST)
  mask=Image.new('L',tile.size);ImageDraw.Draw(mask).polygon(p,fill=255);face.putalpha(ImageChops.multiply(mask,face.getchannel('A')));face=ImageEnhance.Brightness(face).enhance(shade);tile.alpha_composite(face)
 canvas.alpha_composite(tile,(col*200,0));d.text((col*200+12,238),name+(' front' if col==2 else ' back' if col==3 else ''),fill=(50,32,20))
canvas.save(O/'models-offline.png');print('Offline native JSON projections written; held poses remain manual')
