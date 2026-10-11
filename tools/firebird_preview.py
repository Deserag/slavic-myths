from pathlib import Path
import json, math
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];bones=json.loads((R/'docs/firebird/source/native-rig.json').read_text())['bones']
def rotate(p,r):
 x,y,z=p
 for axis,a in enumerate(r):
  c,s=math.cos(a),math.sin(a)
  if axis==0:y,z=y*c-z*s,y*s+z*c
  elif axis==1:x,z=x*c+z*s,-x*s+z*c
  else:x,y=x*c-y*s,x*s+y*c
 return x,y,z
lookup={b['name']:b for b in bones}
def point(name,p,fold):
 if name=='root':return p
 b=lookup[name];rot=list(b['rotation'])
 if fold:
  side=-1 if name.startswith('left') else 1
  if name.endswith('_shoulder'):rot[1]=-side*math.radians(75)
  if name.endswith('_mid'):rot[1]=-side*math.radians(55)
  if name.endswith('_tip'):rot[1]=side*math.radians(30)
 p=rotate(p,rot);p=tuple(x+y for x,y in zip(p,b['pivot']));return point(b['parent'],p,fold)
im=Image.new('RGB',(1500,850),(24,27,32));dr=ImageDraw.Draw(im);palette=[(224,100,30),(255,210,90),(247,226,176),(121,65,24),(39,28,18),(161,38,19)]
for index,(angle,fold,title) in enumerate([(0,False,'Front / extended'),(math.pi/2,True,'Side / folded'),(.7,False,'3/4 / extended')]):
 faces=[];cx=250+index*500;cy=640
 for b in bones:
  for x,y,z,w,h,d,k in b['boxes']:
   pts=[point(b['name'],(x+dx*w,y+dy*h,z+dz*d),fold) for dx,dy,dz in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]]
   pts=[rotate(p,(0,angle,0)) for p in pts]
   for face in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
    depth=sum(pts[i][2] for i in face)/4;poly=[(cx+pts[i][0]*4.2,cy+pts[i][1]*4.2-pts[i][2]*.8) for i in face];faces.append((depth,poly,palette[k]))
 for depth,poly,col in sorted(faces,key=lambda v:v[0],reverse=True):dr.polygon(poly,fill=col,outline=tuple(int(c*.55) for c in col))
 dr.text((index*500+25,30),title,fill='white');dr.line((index*500,640,index*500+500,640),fill=(70,75,82))
dr.text((25,800),'STATIC SOURCE GEOMETRY PREVIEW - NOT A MINECRAFT SCREENSHOT',fill='white');p=R/'work/firebird/native-preview.png';im.save(p);print(p)
