"""Offline textured native-model views. These are not Minecraft screenshots."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/bogatyr-1.7.1'
meshes=json.loads((R/'work/bogatyr/meshes.json').read_text())
def render(key,angle):
 tex=np.asarray(Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity'/('ilya_muromets.png' if key.startswith('ilya') else 'idolishche_poganoe.png')))
 canvas=np.full((500,360,4),[35,38,42,255],dtype=np.uint8);depth=np.full((500,360),1e9)
 scale=9 if key.startswith('ilya') else 2.8;a=math.radians(angle);c,s=math.cos(a),math.sin(a)
 for cube in meshes[key]:
  for face in cube['faces']:
   v=np.array(face);px=v[:,0]*c-v[:,2]*s;zz=v[:,2]*c+v[:,0]*s;py=(24-v[:,1]);q=np.column_stack([180+px*scale,450-py*scale,zz,v[:,3],v[:,4]])
   normal=np.cross(v[1,:3]-v[0,:3],v[2,:3]-v[0,:3]);normal=normal/(np.linalg.norm(normal)+1e-9);shade=.73+.24*max(0,normal.dot(np.array([-.45,-.75,-.45])))
   for ids in [(0,1,2),(0,2,3)]:
    triangle=q[list(ids)];xs=triangle[:,0];ys=triangle[:,1];x0=max(0,int(math.floor(xs.min())));x1=min(359,int(math.ceil(xs.max())));y0=max(0,int(math.floor(ys.min())));y1=min(499,int(math.ceil(ys.max())))
    if x1<x0 or y1<y0:continue
    xx,yy=np.meshgrid(np.arange(x0,x1+1)+.5,np.arange(y0,y1+1)+.5);den=(ys[1]-ys[2])*(xs[0]-xs[2])+(xs[2]-xs[1])*(ys[0]-ys[2])
    if abs(den)<1e-6:continue
    w0=((ys[1]-ys[2])*(xx-xs[2])+(xs[2]-xs[1])*(yy-ys[2]))/den;w1=((ys[2]-ys[0])*(xx-xs[2])+(xs[0]-xs[2])*(yy-ys[2]))/den;w2=1-w0-w1
    z=w0*triangle[0,2]+w1*triangle[1,2]+w2*triangle[2,2];mask=(w0>=0)&(w1>=0)&(w2>=0)&(z<depth[y0:y1+1,x0:x1+1])
    u=w0*triangle[0,3]+w1*triangle[1,3]+w2*triangle[2,3];v=w0*triangle[0,4]+w1*triangle[1,4]+w2*triangle[2,4];rgba=tex[np.clip((v*256).astype(int),0,255),np.clip((u*256).astype(int),0,255)].copy();rgba[:,:,:3]=(rgba[:,:,:3]*shade).astype(np.uint8);mask&=rgba[:,:,3]>0
    canvas[y0:y1+1,x0:x1+1][mask]=rgba[mask];depth[y0:y1+1,x0:x1+1][mask]=z[mask]
 return Image.fromarray(canvas)
for who in ['ilya','idol']:
 sheet=Image.new('RGBA',(1440,535),(25,27,30,255));d=ImageDraw.Draw(sheet)
 for i,(a,label) in enumerate([(0,'FRONT'),(90,'SIDE'),(180,'BACK'),(35,'THREE QUARTERS')]):sheet.paste(render(who+'_IDLE',a),(i*360,25));d.text((i*360+130,8),label,fill='white')
 d.text((12,515),'OFFLINE NATIVE MESH / NOT A GAME SCREENSHOT',fill=(210,190,135));sheet.save(O/(who+'-offline-views.png'))
print('Offline native previews rendered')
