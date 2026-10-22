"""Perspective source views of actual FP matrices, item JSON/UV and native hand meshes."""
import collections,json,math
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
def clip(poly,near=.04):
 out=[]
 for i,b in enumerate(poly):
  a=poly[i-1];ai=a[2]<=-near;bi=b[2]<=-near
  if ai!=bi:out.append(a+(b-a)*((-near-a[2])/(b[2]-a[2])))
  if bi:out.append(b)
 return out
def render(faces,width=256,height=144):
 out=np.full((height,width,4),[37,48,57,255],dtype=np.uint8);depth=np.full((height,width),np.inf);f=height/(2*math.tan(math.radians(70)/2))
 for ref,points,uv in faces:
  tex=g.texture(ref);h,w=tex.shape[:2];poly=clip(list(np.column_stack((points,uv))))
  if len(poly)<3:continue
  for i in range(1,len(poly)-1):
   vertices=np.array([poly[0],poly[i],poly[i+1]]);d=-vertices[:,2];t=np.column_stack((width/2+vertices[:,0]/d*f,height/2-vertices[:,1]/d*f,1/d,vertices[:,3]/d,vertices[:,4]/d));lo=np.maximum(np.floor(t[:,:2].min(0)).astype(int),[0,0]);hi=np.minimum(np.ceil(t[:,:2].max(0)).astype(int),[width-1,height-1])
   if np.any(lo>hi):continue
   xx,yy=np.meshgrid(np.arange(lo[0],hi[0]+1)+.5,np.arange(lo[1],hi[1]+1)+.5);x0,y0=t[0,:2];x1,y1=t[1,:2];x2,y2=t[2,:2];den=(y1-y2)*(x0-x2)+(x2-x1)*(y0-y2)
   if abs(den)<1e-9:continue
   b0=((y1-y2)*(xx-x2)+(x2-x1)*(yy-y2))/den;b1=((y2-y0)*(xx-x2)+(x0-x2)*(yy-y2))/den;b2=1-b0-b1;values=b0[...,None]*t[0,2:]+b1[...,None]*t[1,2:]+b2[...,None]*t[2,2:];inv=np.maximum(values[:,:,0],1e-12);z=1/inv;u=values[:,:,1]/inv;v=values[:,:,2]/inv;colors=tex[np.clip((v*h).astype(int),0,h-1),np.clip((u*w).astype(int),0,w-1)];ds=depth[lo[1]:hi[1]+1,lo[0]:hi[0]+1];dst=out[lo[1]:hi[1]+1,lo[0]:hi[0]+1];mask=(b0>=-1e-7)&(b1>=-1e-7)&(b2>=-1e-7)&(colors[:,:,3]>127)&(z<ds);ds[mask]=z[mask];dst[mask]=colors[mask]
 image=Image.fromarray(out);draw=ImageDraw.Draw(image);draw.rectangle((0,0,width-1,height-1),outline='#52616a');return image,int(np.isfinite(depth).sum())
def faces(sample):
 ctx='firstperson_'+('lefthand' if sample['left'] else 'righthand');d=g.model('slavicmyths:item/'+sample['model'],ctx);matrix=np.array(sample['matrix']).reshape((4,4),order='F');result=[]
 for ref,v,uv in g.faces(d):
  points=(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3];result.append((ref,points,uv))
 if sample['arm_faces']:
  skin='minecraft:entity/player/'+('slim/alex' if sample['body']=='Alex' else 'wide/steve')
  for face in sample['arm_faces']:
   f=np.array(face);result.append((skin,f[:,:3],f[:,3:]))
 return result
def main():
 rows=json.loads((g.O/'fp-native.json').read_text());folder=g.O/'firstperson-source';folder.mkdir(exist_ok=True);metadata=[];families={r['id']:r['family'] for r in json.loads((g.O/'audit-68.json').read_text(encoding='utf-8'))}
 standard=[r for r in rows if r['body']=='ITEM_ONLY'];custom=[r for r in rows if r['body']!='ITEM_ONLY']
 for family in dict.fromkeys(families.values()):
  items=list(dict.fromkeys(r['id'] for r in standard if families[r['id']]==family))
  if not items:continue
  sheet=Image.new('RGB',(1024,40+len(items)*170),'#253039');draw=ImageDraw.Draw(sheet);draw.text((8,5),'ACTUAL source matrices + JSON/UV / standard idle basis, FOV70 16:9 / no game, equip=0, no view bob',fill='white')
  for i,id in enumerate(items):
   selection=[r for r in standard if r['id']==id]
   for r in selection:
    x=(int(r['left'])*2+int(r['after']))*256;y=40+i*170;image,count=render(faces(r));sheet.paste(image,(x,y));draw.text((x+3,y+3),id+' '+('L' if r['left'] else 'R')+' '+('AFTER' if r['after'] else 'BEFORE'),fill='white');metadata.append({'id':id,'frame':r['frame'],'left':r['left'],'after':r['after'],'pixels':count,'hands':'standard item-only; no simulated skin hand','camera':'perspective FOV70,16:9,near.04; source projection, NOT Minecraft'})
  sheet.save(folder/('standard-'+family+'.png'))
 groups=collections.defaultdict(list)
 for r in custom:groups[(r['id'],r['frame'].split('_t')[0] if r['id']=='karachun_scythe' else 'states')].append(r)
 for (id,group),selection in groups.items():
  frames=list(dict.fromkeys(r['frame'] for r in selection));sheet=Image.new('RGB',(2048,40+len(frames)*170),'#253039');draw=ImageDraw.Draw(sheet);draw.text((8,5),id+' / actual production pose helpers + native Steve/Alex arm UV / FOV70 / SOURCE, NOT game screenshots',fill='white')
  for r in selection:
   col=(r['body']=='Alex')*4+int(r['left'])*2+int(r['after']);x=col*256;y=40+frames.index(r['frame'])*170;source=faces(r);image,count=render(source);item_pixels=render(source[:-len(r['arm_faces'])])[1] if r['arm_faces'] else count;sheet.paste(image,(x,y));draw.text((x+3,y+3),r['body']+' '+('L' if r['left'] else 'R')+' '+('AFTER' if r['after'] else 'BEFORE')+' '+r['frame'],fill='white');metadata.append({'id':id,'body':r['body'],'frame':r['frame'],'left':r['left'],'after':r['after'],'pixels':count,'item_pixels':item_pixels,'native_arm_faces':len(r['arm_faces']),'model':r['model'],'runtime':'NOT_RUN / OWNER_PENDING'})
  sheet.save(folder/(id+'-'+group+'.png'))
 (folder/'matrix.json').write_text(json.dumps(metadata,indent=2)+'\n');print('FP source perspective sheets:',len(metadata),'samples /',len({r['id'] for r in metadata}),'IDs; custom hands are actual native mesh/UV; game launches0')
if __name__=='__main__':main()
