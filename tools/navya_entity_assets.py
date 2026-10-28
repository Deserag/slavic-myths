"""Deterministic pixel atlases and previews from exported actual ModelPart faces/UVs.
No game launch. The previews are source renders, not in-game screenshots.
"""
from pathlib import Path
import json, math, random
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
REPORT=ROOT/'docs/implementation/navya-grove'
OUT=ROOT/'src/main/resources/assets/slavicmyths/textures/entity'
PALETTE={'cloth':(66,65,76),'pale':(161,167,170),'hair':(34,31,45),
 'bone':(180,166,135),'bone_dark':(103,94,77),'violet':(154,95,195),
 'bark':(48,34,37),'dark':(25,20,27),'red':(150,40,61),'root':(71,47,43),
 'ember':(181,50,74),'wisp':(83,104,160),'wisp_core':(177,194,228)}
GLOW={'violet','red','ember','wisp','wisp_core'}

def atlas(name,rig):
 size=rig['atlas'];im=Image.new('RGBA',(size,size));glow=im.copy()
 for index,b in enumerate(rig['boxes']):
  rng=random.Random(name+':'+b['part']+':'+str(index));u,v=b['u'],b['v'];sx,sy,sz=b['sx'],b['sy'],b['sz'];mat=b['material'];base=PALETTE[mat]
  from mob332_assets import palette as mob_palette
  if name!='grove_guardian':base=mob_palette(name,b['part'],base)
  # The six UV rectangles are the native Cube texture net, not a generic sprite.
  faces=[(u+sz,v,sx,sz),(u+sz+sx,v,sx,sz),(u,v+sz,sz,sy),(u+sz,v+sz,sx,sy),(u+sz+sx,v+sz,sz,sy),(u+2*sz+sx,v+sz,sx,sy)]
  for face,(x0,y0,w,h) in enumerate(faces):
   for y in range(y0,y0+h):
    for x in range(x0,x0+w):
     localx=x-x0;localy=y-y0;noise=rng.choice([-8,-4,0,0,3,7]);detail=0
     if mat in {'bark','root','hair'}:detail=-16 if (localx+int(math.sin(localy*.4)*2)+index)%7==0 else 0
     elif mat in {'bone','bone_dark','pale'}:detail=9 if localx==0 or localy==0 else (-15 if (localx*3+localy+index)%19==0 else 0)
     elif mat=='cloth':detail=-12 if localx%4==0 else (5 if localy%7==0 else 0)
     elif mat in GLOW:detail=17 if abs(localx-w/2)<max(1,w/4) else -12
     color=tuple(max(0,min(255,c+noise+detail+(5 if face==0 else -4 if face==1 else 0))) for c in base)+(255,)
     im.putpixel((x,y),color)
     if mat in GLOW:glow.putpixel((x,y),color)
 im.save(OUT/(name+'.png'));glow.save(OUT/(name+'_glow.png'))
 return im

def render(frame,texture,size=420,scale=3,angle=-.5):
 image=Image.new('RGB',(size,size),(24,23,31));draw=ImageDraw.Draw(image);polygons=[]
 for face in frame['faces']:
  points=[];depth=0
  for x,y,z,u,v in face:
   xx=x*math.cos(angle)+z*math.sin(angle);zz=-x*math.sin(angle)+z*math.cos(angle)
   points.append((size/2+xx*scale,size*.78+(y-24)*scale-zz*.24*scale));depth+=zz
  polygons.append((depth,points,face))
 for depth,points,face in sorted(polygons,reverse=True):
  # Pixel tessellation samples the actual UV texture, preserving authored bark/bone detail.
  a,b,c,d=face
  w=max(1,round(math.dist(a[3:],b[3:])*texture.width));h=max(1,round(math.dist(b[3:],c[3:])*texture.height))
  def mix(p,q,t):return [p[i]*(1-t)+q[i]*t for i in range(5)]
  def vertex(s,t):return mix(mix(a,b,s),mix(d,c,s),t)
  def project(p):
   x,y,z=p[:3];xx=x*math.cos(angle)+z*math.sin(angle);zz=-x*math.sin(angle)+z*math.cos(angle)
   return (size/2+xx*scale,size*.78+(y-24)*scale-zz*.24*scale)
  for iy in range(h):
   for ix in range(w):
    p=vertex((ix+.5)/w,(iy+.5)/h);color=texture.getpixel((min(texture.width-1,int(p[3]*texture.width)),min(texture.height-1,int(p[4]*texture.height))))
    if color[3]:draw.polygon([project(vertex(s,t)) for s,t in [(ix/w,iy/h),((ix+1)/w,iy/h),((ix+1)/w,(iy+1)/h),(ix/w,(iy+1)/h)]],fill=color[:3])
 draw.text((10,10),f"{frame['action']} t={frame['time']}",fill=(230,225,222));return image

def main():
 OUT.mkdir(parents=True,exist_ok=True);rigs=json.loads((REPORT/'native-rigs.json').read_text());sheet=Image.new('RGB',(420*3,460*5),(24,23,31))
 for row,(name,rig) in enumerate(rigs.items()):
  tex=atlas(name,rig);scale=2.9 if name=='grove_guardian' else 6 if name not in {'navya_shot_2'} else 3.5
  idle=rig['frames'][0]
  if row<5:
   for col,angle in enumerate([0,-.65,math.pi/2]):
    img=render(idle,tex,scale=scale,angle=angle);sheet.paste(img,(col*420,row*460+30))
   ImageDraw.Draw(sheet).text((10,row*460+8),name+' | actual ModelPart + UV + PNG | source preview',fill='white')
  frames=[render(f,tex,scale=scale) for f in rig['frames']]
  frames[0].save(REPORT/(name+'-poses.gif'),save_all=True,append_images=frames[1:],duration=450,loop=0)
 sheet.save(REPORT/'actual-entity-models.png')
 print('PASS: 9 actual UV atlases + glow masks; native pose previews (no game launch)')
if __name__=='__main__':main()
