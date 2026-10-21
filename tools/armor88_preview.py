"""Depth-tested native ModelPart/UV comparison; no Minecraft launch."""
from pathlib import Path
import json,zipfile,io,math
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor88-linen'
Z=zipfile.ZipFile(Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
def texture(n):
 if n.startswith('minecraft:'):return np.array(Image.open(io.BytesIO(Z.read('assets/minecraft/'+n.split(':')[1]))).convert('RGBA'))
 return np.array(Image.open(R/'src/main/resources/assets/slavicmyths'/n).convert('RGBA'))
def render(sample,yaw,*,base=None,return_depth=False):
 W,H=200,270
 if base is None:out=np.full((H,W,4),[41,48,57,255],dtype=np.uint8);depth=np.full((H,W),np.inf)
 else:out,depth=base[0].copy(),base[1].copy()
 a=math.radians(yaw);c,s=math.cos(a),math.sin(a);p=math.radians(6)
 for layer in sample['layers']:
  tex=texture(layer['texture']);th,tw=tex.shape[:2]
  for face in layer['faces']:
   f=np.array(face);x=f[:,0]*c+f[:,2]*s;z=-f[:,0]*s+f[:,2]*c;y=f[:,1]*math.cos(p)-z*math.sin(p);zz=f[:,1]*math.sin(p)+z*math.cos(p)
   v=np.column_stack((100+x*6.6,26+(y+8)*6.6,zz,f[:,3:]))
   for idx in [(0,1,2),(0,2,3)]:
    t=v[list(idx)];lo=np.maximum(np.floor(t[:,:2].min(0)).astype(int),[0,0]);hi=np.minimum(np.ceil(t[:,:2].max(0)).astype(int),[W-1,H-1])
    if np.any(lo>hi):continue
    xx,yy=np.meshgrid(np.arange(lo[0],hi[0]+1)+.5,np.arange(lo[1],hi[1]+1)+.5)
    x0,y0=t[0,:2];x1,y1=t[1,:2];x2,y2=t[2,:2];den=(y1-y2)*(x0-x2)+(x2-x1)*(y0-y2)
    if abs(den)<1e-8:continue
    b0=((y1-y2)*(xx-x2)+(x2-x1)*(yy-y2))/den;b1=((y2-y0)*(xx-x2)+(x0-x2)*(yy-y2))/den;b2=1-b0-b1
    inside=(b0>=-1e-7)&(b1>=-1e-7)&(b2>=-1e-7);attrs=b0[...,None]*t[0,2:]+b1[...,None]*t[1,2:]+b2[...,None]*t[2,2:]
    col=tex[np.clip((attrs[:,:,2]*th).astype(int),0,th-1),np.clip((attrs[:,:,1]*tw).astype(int),0,tw-1)]
    ds=depth[lo[1]:hi[1]+1,lo[0]:hi[0]+1];dst=out[lo[1]:hi[1]+1,lo[0]:hi[0]+1];mask=inside&(col[:,:,3]>127)&(attrs[:,:,0]<ds)
    ds[mask]=attrs[:,:,0][mask];dst[mask]=col[mask]
 return (out,depth) if return_depth else Image.fromarray(out)
def main():
 before=json.loads((O/'before-native.json').read_text());after=json.loads((O/'after-native.json').read_text());sheet=Image.new('RGB',(1600,1770),(22,27,34));d=ImageDraw.Draw(sheet)
 d.text((12,10),'#88 LINEN / actual baked ModelPart + native UV + actual PNG / same scale & camera',fill='white');d.text((12,28),'OFFLINE SOURCE POSES. Not Minecraft screenshots. Weapon hold = arm pose only. Runtime/owner review pending.',fill='white')
 for row,(old,new) in enumerate(zip(before['samples'],after['samples'])):
  y=60+row*285;d.text((8,y),old['body']+' / '+old['pose'],fill='white')
  for col,(name,yaw) in enumerate([('FRONT',0),('RIGHT',90),('BACK',180),('3/4',-35)]):
   for side,sample in enumerate([old,new]):
    x=(col*2+side)*200;sheet.paste(render(sample,yaw),(x,y+15));d.text((x+5,y+18),name+(' BEFORE' if side==0 else ' AFTER'),fill='white')
 sheet.save(O/'SOURCE-BEFORE-AFTER.png')
if __name__=='__main__':main()
