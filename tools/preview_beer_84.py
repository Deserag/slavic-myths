"""Textured production JSON cuboid projection. Offline evidence, NOT Minecraft screenshots."""
from pathlib import Path
import json, math, zipfile
from PIL import Image,ImageDraw,ImageChops,ImageEnhance
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/beer-84-85-v2'
BEERS=['light_beer','dark_beer','hopped_beer','honey_beer','forest_beer','thunder_beer','veles_dark_beer','witch_berry_beer']
V=R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'

def render(root,name,size=32,context='gui'):
 model=json.loads((root/f'models/item/{name}.json').read_text());display=model['display'][context];pitch,yaw,roll=display.get('rotation',[0,0,0]);scale=display.get('scale',[1,1,1]);faces=[];tile=Image.new('RGBA',(size,size));textures={}
 def rotate(p):
  x,y,z=p;t=math.radians(yaw);x,z=x*math.cos(t)+z*math.sin(t),-x*math.sin(t)+z*math.cos(t);t=math.radians(pitch);y,z=y*math.cos(t)-z*math.sin(t),y*math.sin(t)+z*math.cos(t);return x,y,z
 def tex(ref):
  if ref not in textures:
   ns,path=ref.split(':');p=root/f'textures/{path}.png'
   if ns=='slavicmyths':textures[ref]=Image.open(p if p.exists() else A/f'textures/{path}.png').convert('RGBA')
   else:
    from io import BytesIO
    with zipfile.ZipFile(V) as z:textures[ref]=Image.open(BytesIO(z.read(f'assets/{ns}/textures/{path}.png'))).convert('RGBA')
  return textures[ref]
 for e in model['elements']:
  x0,y0,z0=e['from'];x1,y1,z1=e['to']
  verts={'south':[(x0,y1,z1),(x1,y1,z1),(x1,y0,z1),(x0,y0,z1)],'north':[(x1,y1,z0),(x0,y1,z0),(x0,y0,z0),(x1,y0,z0)],'west':[(x0,y1,z0),(x0,y1,z1),(x0,y0,z1),(x0,y0,z0)],'east':[(x1,y1,z1),(x1,y1,z0),(x1,y0,z0),(x1,y0,z1)],'up':[(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)],'down':[(x0,y0,z1),(x1,y0,z1),(x1,y0,z0),(x0,y0,z0)]}
  normals={'north':(0,0,-1),'south':(0,0,1),'west':(-1,0,0),'east':(1,0,0),'up':(0,1,0),'down':(0,-1,0)}
  defaults={'north':[16-x1,16-y1,16-x0,16-y0],'south':[x0,16-y1,x1,16-y0],'west':[z0,16-y1,z1,16-y0],'east':[16-z1,16-y1,16-z0,16-y0],'up':[x0,z0,x1,z1],'down':[x0,16-z1,x1,16-z0]}
  for side,f in e['faces'].items():
   normal=rotate(normals[side])
   if normal[2]<=.001:continue
   points=[rotate(tuple((p[i]-8)*scale[i] for i in range(3))) for p in verts[side]]
   points2=[(size/2+p[0]*size/18,size/2-p[1]*size/18) for p in points]
   ref=f['texture']
   while ref.startswith('#'):ref=model['textures'][ref[1:]]
   faces.append((sum(p[2] for p in points)/4,points2,f.get('uv',defaults[side]),tex(ref),.7+.3*normal[2]))
 for depth,p,uv,texture,shade in sorted(faces,key=lambda v:v[0]):
  x,y=p[0];ax,ay=p[1][0]-x,p[1][1]-y;bx,by=p[3][0]-x,p[3][1]-y;det=ax*by-ay*bx
  if abs(det)<.0001:continue
  u0,v0,u1,v1=uv;u0*=texture.width/16;u1*=texture.width/16;v0*=texture.height/16;v1*=texture.height/16
  aa=(u1-u0)*by/det;ab=-(u1-u0)*bx/det;ba=-(v1-v0)*ay/det;bb=(v1-v0)*ax/det
  face=texture.transform(tile.size,Image.Transform.AFFINE,(aa,ab,u0-aa*x-ab*y,ba,bb,v0-ba*x-bb*y),Image.Resampling.NEAREST)
  mask=Image.new('L',tile.size);ImageDraw.Draw(mask).polygon(p,fill=255);face.putalpha(ImageChops.multiply(mask,face.getchannel('A')));face=ImageEnhance.Brightness(face).enhance(shade);tile.alpha_composite(face)
 return tile

def main():
 O.mkdir(parents=True,exist_ok=True)
 im=Image.new('RGBA',(640,340),'#25292e');d=ImageDraw.Draw(im)
 d.text((12,8),'1.9.7.25 | production JSON + textures | OFFLINE, not Minecraft',fill='white')
 d.text((12,35),'Native 16px items / 18px slots (GUI scale 1)',fill='#c9c9c9')
 for i,beer in enumerate(BEERS):
  x=12+i*18;y=54;d.rectangle((x,y,x+17,y+17),fill='#8b8b8b');d.line((x,y,x+16,y),fill='#373737');d.line((x,y,x,y+16),fill='#373737');im.alpha_composite(render(A,beer+'_mug',16),(x+1,y+1))
 d.text((12,88),'Same slots at GUI scale 3, nearest-neighbor pixels',fill='#c9c9c9')
 for i,beer in enumerate(BEERS):
  x=12+i*72;y=112;slot=Image.new('RGBA',(18,18),'#8b8b8b');sd=ImageDraw.Draw(slot);sd.line((0,0,16,0),fill='#373737');sd.line((0,0,0,16),fill='#373737');sd.line((1,17,17,17),fill='white');sd.line((17,1,17,17),fill='white');slot.alpha_composite(render(A,beer+'_mug',16),(1,1));im.alpha_composite(slot.resize((54,54),Image.Resampling.NEAREST),(x,y));d.text((x,y+60),str(i+1),fill='white')
 d.text((12,201),'Top surfaces (actual 16px texture, x3) - no symbols',fill='#c9c9c9')
 for i,beer in enumerate(BEERS):im.alpha_composite(Image.open(A/f'textures/item/{beer}_foam.png').convert('RGBA').resize((48,48),Image.Resampling.NEAREST),(12+i*72,224))
 d.text((12,287),'1 Light   2 Dark   3 Hopped   4 Honey   5 Forest',fill='white');d.text((12,307),'6 Thunder   7 Veles dark   8 Witch berry',fill='white');im.save(O/'eight-beers-slots.png')
 im=Image.new('RGBA',(576,150),'#8b8b8b');d=ImageDraw.Draw(im)
 for row,root in enumerate((O/'before/assets/slavicmyths',A)):
  d.text((0,row*75),'Rejected 24' if row==0 else 'Revision 25',fill='black')
  for i,beer in enumerate(BEERS):im.alpha_composite(render(root,beer+'_mug',32).resize((48,48),Image.Resampling.NEAREST),(i*72,20+row*75))
 im.save(O/'source-before-after.png')
if __name__=='__main__':main()
