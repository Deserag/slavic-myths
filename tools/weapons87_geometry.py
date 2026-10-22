"""Actual item JSON/UV source projections. No game, no conceptual replacement art."""
from pathlib import Path
import copy,functools,io,json,math,zipfile
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets';O=R/'docs/implementation/weapons87';Z=zipfile.ZipFile(Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
def rotation(a):
 x,y,z=np.radians(a);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
 return np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])
@functools.lru_cache(maxsize=256)
def texture(ref):
 ns,path=ref.split(':');return np.array(Image.open(io.BytesIO(Z.read('assets/minecraft/textures/'+path+'.png')) if ns=='minecraft' else A/ns/'textures'/(path+'.png')).convert('RGBA'))
def model(ref,context='gui',overrides=None):
 if ':' not in ref:ref='minecraft:'+ref
 # TugarinPairClient.bake selects standalone held in hand contexts.
 if ref=='slavicmyths:item/tugarin_sword' and context.startswith(('firstperson','thirdperson')):return model('slavicmyths:item/tugarin_sword_held',context,overrides)
 ns,path=ref.split(':');d=copy.deepcopy(overrides[ref]) if overrides and ref in overrides else json.loads(Z.read('assets/minecraft/models/'+path+'.json') if ns=='minecraft' else (A/ns/'models'/(path+'.json')).read_text())
 if d.get('loader')=='neoforge:separate_transforms':d=d.get('perspectives',{}).get(context,d['base'])
 if d.get('parent')=='builtin/entity' and ns=='slavicmyths':
  companion=A/ns/'models'/(path+('_icon' if context=='gui' else '_held')+'.json')
  if companion.exists():return model(ns+':'+path+('_icon' if context=='gui' else '_held'),context,overrides)
 parent=d.get('parent','')
 if parent and parent not in ['builtin/generated','builtin/entity']:
  base=model(parent,context,overrides);base['textures']=base.get('textures',{})|d.get('textures',{});base['display']=base.get('display',{})|d.get('display',{});base.update({k:v for k,v in d.items() if k not in ['textures','display']});return base
 return d
def faces(d):
 textures=d.get('textures',{});elements=d.get('elements')
 if elements is None:
  assert 'layer0' in textures,d
  pixels=texture(textures['layer0']);height,width=pixels.shape[:2]
  elements=[{'from':[0,0,7.5],'to':[16,16,8.5],'faces':{'north':{'texture':'#layer0','uv':[16,0,0,16]},'south':{'texture':'#layer0','uv':[0,0,16,16]}}}]
  for py in range(height):
   for px in range(width):
    if pixels[py,px,3]<=127:continue
    exposed=[]
    for side,dx,dy in [('west',-1,0),('east',1,0),('up',0,-1),('down',0,1)]:
     nx,ny=px+dx,py+dy
     if nx<0 or ny<0 or nx>=width or ny>=height or pixels[ny,nx,3]<=127:exposed.append(side)
    if exposed:
     uv=[px*16/width,py*16/height,(px+1)*16/width,(py+1)*16/height]
     elements.append({'from':[px*16/width,(height-py-1)*16/height,7.5],'to':[(px+1)*16/width,(height-py)*16/height,8.5],'faces':{side:{'texture':'#layer0','uv':uv} for side in exposed}})
 result=[]
 for e in elements:
  x0,y0,z0=e['from'];x1,y1,z1=e['to'];points={'west':[[x0,y1,z0],[x0,y0,z0],[x0,y0,z1],[x0,y1,z1]],'east':[[x1,y1,z1],[x1,y0,z1],[x1,y0,z0],[x1,y1,z0]],'north':[[x1,y1,z0],[x1,y0,z0],[x0,y0,z0],[x0,y1,z0]],'south':[[x0,y1,z1],[x0,y0,z1],[x1,y0,z1],[x1,y1,z1]],'up':[[x0,y1,z0],[x0,y1,z1],[x1,y1,z1],[x1,y1,z0]],'down':[[x0,y0,z1],[x0,y0,z0],[x1,y0,z0],[x1,y0,z1]]}
  default={'west':[z0,16-y1,z1,16-y0],'east':[16-z1,16-y1,16-z0,16-y0],'down':[x0,16-z1,x1,16-z0],'up':[x0,z0,x1,z1],'north':[16-x1,16-y1,16-x0,16-y0],'south':[x0,16-y1,x1,16-y0]}
  for side,f in e.get('faces',{}).items():
   vertices=np.array(points[side],dtype=float)
   if 'rotation' in e:
    r=e['rotation'];angles=[0.,0.,0.];angles['xyz'.index(r['axis'])]=r['angle'];origin=np.array(r['origin']);vertices=(vertices-origin)@rotation(angles).T
    if r.get('rescale'):vertices*=np.array([1 if a==r['axis'] else 1/math.cos(math.radians(r['angle'])) for a in 'xyz'])
    vertices+=origin
   ref=f['texture'];seen=set()
   while ref.startswith('#'):
    assert ref not in seen;seen.add(ref);ref=textures[ref[1:]]
   a,b,c,d=f.get('uv',default[side]);uv=np.array([[a,b],[a,d],[c,d],[c,b]])/16;uv=np.roll(uv,-f.get('rotation',0)//90,axis=0)
   result.append((ref,vertices,uv))
 return result
def project(faces_,size=128,span=32,angles=(0,0,0),scale=(1,1,1),translation=(0,0,0)):
 out=np.zeros((size,size,4),dtype=np.uint8);depth=np.full((size,size),np.inf);rot=rotation(angles)
 for ref,vertices,uv in faces_:
  p=((vertices-8)*scale)@rot.T+translation;v=np.column_stack((size/2+p[:,0]*size/span,size/2-p[:,1]*size/span,-p[:,2],uv));tex=texture(ref);h,w=tex.shape[:2]
  for indices in [(0,1,2),(0,2,3)]:
   t=v[list(indices)];lo=np.maximum(np.floor(t[:,:2].min(0)).astype(int),[0,0]);hi=np.minimum(np.ceil(t[:,:2].max(0)).astype(int),[size-1,size-1])
   if np.any(lo>hi):continue
   xx,yy=np.meshgrid(np.arange(lo[0],hi[0]+1)+.5,np.arange(lo[1],hi[1]+1)+.5);x0,y0=t[0,:2];x1,y1=t[1,:2];x2,y2=t[2,:2];den=(y1-y2)*(x0-x2)+(x2-x1)*(y0-y2)
   if abs(den)<1e-9:continue
   b0=((y1-y2)*(xx-x2)+(x2-x1)*(yy-y2))/den;b1=((y2-y0)*(xx-x2)+(x0-x2)*(yy-y2))/den;b2=1-b0-b1;attr=b0[...,None]*t[0,2:]+b1[...,None]*t[1,2:]+b2[...,None]*t[2,2:]
   color=tex[np.clip((attr[:,:,2]*h).astype(int),0,h-1),np.clip((attr[:,:,1]*w).astype(int),0,w-1)];ds=depth[lo[1]:hi[1]+1,lo[0]:hi[0]+1];dst=out[lo[1]:hi[1]+1,lo[0]:hi[0]+1];mask=(b0>=-1e-7)&(b1>=-1e-7)&(b2>=-1e-7)&(color[:,:,3]>127)&(attr[:,:,0]<ds);ds[mask]=attr[:,:,0][mask];dst[mask]=color[mask]
 return Image.fromarray(out)
def main():
 rows=json.loads((O/'catalogue-provenance.json').read_text(encoding='utf-8'));folder=O/'source-before';folder.mkdir(exist_ok=True);sheet=Image.new('RGB',(1360,50+math.ceil(len(rows)/4)*180),'#293039');draw=ImageDraw.Draw(sheet);draw.text((10,8),'ACTUAL JSON/UV source: GUI 1x + enlarged / front / side / 3-4. No game screenshots. Same scale, no auto-fit.',fill='white');report=[]
 for i,row in enumerate(rows):
  id=row['id'];gui=model('slavicmyths:item/'+id,'gui');held=model('slavicmyths:item/'+id,'thirdperson_righthand');g=faces(gui);f=faces(held);pose=gui.get('display',{}).get('gui',{});tiny=project(g,16,16,pose.get('rotation',(0,0,0)),pose.get('scale',(1,1,1)),pose.get('translation',(0,0,0)));tiny.save(folder/(id+'-gui.png'));x=i%4*340;y=50+i//4*180;draw.text((x+5,y),id,fill='white');sheet.paste(tiny,(x+5,y+27),tiny);big=tiny.resize((64,64),Image.Resampling.NEAREST);sheet.paste(big,(x+28,y+20),big)
  for j,angles in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   image=project(f,76,40,angles);image.save(folder/(id+f'-view{j}.png'));sheet.paste(image,(x+96+j*79,y+18),image)
  verts=np.concatenate([v for _,v,_ in f]);bounds=[verts.min(0).tolist(),verts.max(0).tolist()];report.append({'id':id,'source_bounds':bounds,'span40_source_views':'model coordinates, no player poses','gui_model':gui.get('parent'),'gui_transform':pose,'runtime':'NOT_RUN / OWNER_PENDING'})
 sheet.save(O/'SOURCE-68-BEFORE.png');(O/'source-view-matrix.json').write_text(json.dumps(report,indent=2)+'\n');print('68 actual model/UV previews, source orthographic only; no player/game claim')
if __name__=='__main__':main()
