"""CPU z-buffer of actual baked ModelPart vertices/UV. NOT an in-game screenshot."""
from pathlib import Path
import gzip,json,math
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/volkolak-76'

def load(label):return json.loads(gzip.decompress((O/label/'native-poses.json.gz').read_bytes()))

def render(pose,texture,depth,yaw,size=(400,400),scale=6,eye=None,flat=False,pitch=7,stats=None,perspective=False):
 w,h=size;rgb=np.full((h,w,3),(31,42,48),dtype=np.uint8);zbuf=np.full((h,w),np.inf);ids=np.full((h,w),"",dtype=object);t=np.asarray(texture.convert('RGBA'));et=np.asarray(eye.convert('RGBA')) if eye else None
 a=math.radians(yaw);b=math.radians(pitch)
 def project(vertices):
  v=np.array(vertices,dtype=float);v[:,2]*=depth;x=v[:,0]*math.cos(a)+v[:,2]*math.sin(a);z=v[:,2]*math.cos(a)-v[:,0]*math.sin(a);y=(v[:,1]-24)*math.cos(b)-z*math.sin(b);zz=z*math.cos(b)+(v[:,1]-24)*math.sin(b)
  factor=140/(140+zz) if perspective else 1
  return np.column_stack((w/2+x*scale*factor,h-48+y*scale*factor,zz,v[:,3],v[:,4]))
 for face in pose['faces']:
  verts=face['vertices'];p=project(verts);world=np.array(verts)[:,:3];n=np.cross(world[1]-world[0],world[2]-world[0]);norm=np.linalg.norm(n) or 1;shade=.63+.23*abs(n[1])/norm+.14*abs(n[2])/norm
  for inds in ((0,1,2),(0,2,3)):
   tri=p[list(inds)];xmin=max(0,math.floor(tri[:,0].min()));xmax=min(w-1,math.ceil(tri[:,0].max()));ymin=max(0,math.floor(tri[:,1].min()));ymax=min(h-1,math.ceil(tri[:,1].max()))
   if xmin>xmax or ymin>ymax:continue
   xx,yy=np.meshgrid(np.arange(xmin,xmax+1)+.5,np.arange(ymin,ymax+1)+.5)
   x0,y0=tri[0,:2];x1,y1=tri[1,:2];x2,y2=tri[2,:2];den=(y1-y2)*(x0-x2)+(x2-x1)*(y0-y2)
   if abs(den)<1e-9:continue
   q0=((y1-y2)*(xx-x2)+(x2-x1)*(yy-y2))/den;q1=((y2-y0)*(xx-x2)+(x0-x2)*(yy-y2))/den;q2=1-q0-q1
   inside=(q0>=-1e-6)&(q1>=-1e-6)&(q2>=-1e-6);z=q0*tri[0,2]+q1*tri[1,2]+q2*tri[2,2]
   u=np.clip(((q0*tri[0,3]+q1*tri[1,3]+q2*tri[2,3])*t.shape[1]).astype(int),0,t.shape[1]-1);v=np.clip(((q0*tri[0,4]+q1*tri[1,4]+q2*tri[2,4])*t.shape[0]).astype(int),0,t.shape[0]-1)
   colors=t[v,u];visible=inside&(z<zbuf[ymin:ymax+1,xmin:xmax+1])&(colors[:,:,3]>0)
   paint=np.full(colors[:,:,:3].shape,(181,190,192),dtype=np.uint8) if flat else np.clip(colors[:,:,:3]*shade,0,255).astype(np.uint8)
   if et is not None:
    glow=et[v,u];weight=glow[:,:,3:4]/255*.30;paint=np.clip(paint*(1-weight)+glow[:,:,:3]*weight,0,255).astype(np.uint8)
   ids[ymin:ymax+1,xmin:xmax+1][visible]=face["part"];rgb[ymin:ymax+1,xmin:xmax+1][visible]=paint[visible];zbuf[ymin:ymax+1,xmin:xmax+1][visible]=z[visible]
 if stats is not None:
  names,counts=np.unique(ids,return_counts=True);stats.update(dict(zip(names.tolist(),counts.tolist())))
 im=Image.fromarray(rgb);d=ImageDraw.Draw(im);d.line((8,h-48,w-8,h-48),fill='#8eaa9a');d.text((w-78,h-43),'floor / model px',fill='#adc0b8');return im

def main():
 before=load('before');after=load('after');base=O/'before/src/main/resources/assets/slavicmyths/textures/entity';cur=R/'src/main/resources/assets/slavicmyths/textures/entity'
 old=Image.open(base/'cursed_volkolak.png');new=Image.open(cur/'cursed_volkolak.png');olde=Image.open(base/'cursed_volkolak_eyes.png');newe=Image.open(cur/'cursed_volkolak_eyes.png')
 sheet=Image.new('RGB',(1600,1700),'#1f2a30');d=ImageDraw.Draw(sheet)
 for row,(data,tex,eye,key,label) in enumerate([(before,old,olde,'quad','BEFORE QUAD'),(after,new,newe,'quad','AFTER QUAD'),(before,old,olde,'upright','BEFORE UPRIGHT'),(after,new,newe,'upright','AFTER UPRIGHT')]):
  for col,(yaw,view) in enumerate([(0,'FRONT'),(90,'SIDE'),(180,'BACK'),(-35,'3/4')]):
   sheet.paste(render(data['poses'][key],tex,data['depthScale'],yaw,eye=eye),(col*400,row*420+22));d.text((col*400+8,row*420+5),label+' / '+view,fill='white')
 d.text((10,1683),'ACTUAL BAKED ModelPart + source UV + renderer depth. Matched camera, scale, light. NOT IN GAME.',fill='white');sheet.save(O/'matched-16-views.png')
 diag=Image.new('RGB',(1200,820),'#1f2a30');d=ImageDraw.Draw(diag)
 for i,depth in enumerate((.68,1.0)):
  for j,yaw in enumerate((0,90,-35)):
   diag.paste(render(before['poses']['quad'],old,depth,yaw,eye=olde),(j*400,i*410+20));d.text((j*400+5,i*410+4),f'BASELINE DEPTH {depth} / yaw {yaw}',fill='white')
 diag.save(O/'baseline-depth-diagnostic.png');print('native source A/B rendered')
def extras():
 a=load('after');b=load('before');base=O/'before/src/main/resources/assets/slavicmyths/textures/entity';cur=R/'src/main/resources/assets/slavicmyths/textures/entity';tex=Image.open(cur/'cursed_volkolak.png');eye=Image.open(cur/'cursed_volkolak_eyes.png');old=Image.open(base/'cursed_volkolak.png')
 # Matched action A/B at common captured native phases.
 for sheetid,keys in enumerate([['CLAW_L_10','CLAW_R_12','POUNCE_24','GRAB_30','BITE_12'],['ESCAPE_19','ESCAPE_49','ESCAPE_87','ROAR_12','HOWL_SHORT_30']]):
  out=Image.new('RGB',(1600,790),'#1f2a30');d=ImageDraw.Draw(out)
  for row,data in enumerate([b,a]):
   ot=old if row==0 else tex;oe=Image.open(base/'cursed_volkolak_eyes.png') if row==0 else eye
   for col,key in enumerate(keys):
    out.paste(render(data['poses'][key],ot,.68,-35,size=(320,360),scale=4.8,eye=oe),(col*320,row*382+20));d.text((col*320+5,row*382+3),('BEFORE / ' if row==0 else 'AFTER / ')+key,fill='white')
  d.text((8,773),'NOT IN GAME / actual ModelPart UV / matched phases, camera, scale, light',fill='white');out.save(O/('actions-matched-'+str(sheetid+1)+'.png'))
 def sheet(filename,rows,columns=5,cell=(320,360),scale=4.8):
  out=Image.new('RGB',(columns*cell[0],len(rows)*(cell[1]+22)+24),'#1f2a30');d=ImageDraw.Draw(out)
  for r,(label,frames) in enumerate(rows):
   for c,(data,key,yaw) in enumerate(frames):
    ot=old if data is b else tex
    im=render(data['poses'][key],ot,.68,yaw,size=cell,scale=scale,eye=eye if data is a else None)
    out.paste(im,(c*cell[0],r*(cell[1]+22)+20));d.text((c*cell[0]+5,r*(cell[1]+22)+4),label+' / '+key,fill='white')
  d.text((8,out.height-18),'NOT IN GAME / native ModelPart / same scale and camera',fill='white');out.save(O/filename)
 sheet('rise-drop-matched.png',[(label,[(data,action+'_'+str(float(t)),-35) for t in ticks]) for action,ticks in [('rise',[0,4,8,12,16]),('drop',[0,3,6,9,12])] for data,label in [(b,'BEFORE '+action),(a,'AFTER '+action)]])
 rows=[]
 choices={'CLAW_L':[0,7,10,19,24],'CLAW_R':[0,7,12,19,24],'DOUBLE':[0,10,20,24,30],'TRIPLE':[0,10,20,30,36],'BITE':[0,7,13,19,24],'POUNCE':[0,13,20,28,36],'FLANK':[0,7,19,24,30],'SNIFF':[0,7,12,19,24],'LISTEN':[0,7,12,19,24],'SNARL':[0,7,12,19,24],'HOWL_SHORT':[0,10,20,30,36],'ALERT':[0,7,12,19,24],'ROAR':[0,7,12,19,24],'STAGGER':[0,7,12,19,24]}
 for move,ticks in choices.items():
  valid=[(a,move+'_'+str(t),-35) for t in ticks if move+'_'+str(t) in a['poses']];rows.append((move,valid))
 for i in range(0,len(rows),5):sheet('actions-'+str(i//5+1)+'.png',rows[i:i+5],scale=4.4)
 sheet('grab-victim.png',[(str(held),[(a,'grab_'+str(held).lower()+'_'+str(t),-35) for t in [0,21,24,28,45]]) for held in [False,True]])
 # Same frozen server anchor; humanoid is native vanilla geometry with diagnostic material.
 victim=a['poses']['victim'];vtex=Image.new('RGBA',(512,512),'#608b9b')
 out=Image.new('RGB',(1200,430),'#1f2a30');d=ImageDraw.Draw(out)
 for c,yaw in enumerate([0,90,-35]):
  combined={'faces':a['poses']['grab_true_28']['faces']+victim['faces']}
  # Bake an explicit diagnostic atlas section for reference humanoid, not an invented player skin.
  combined={'faces':[dict(f,vertices=[v[:3]+[.99,.99] for v in f['vertices']]) if f['part']=='victim' else f for f in combined['faces']]}
  reference=tex.copy();ImageDraw.Draw(reference).rectangle((505,505,511,511),fill='#608b9b')
  out.paste(render(combined,reference,.68,yaw,eye=eye),(c*400,22));d.text((c*400+8,5),'GRAB28 + native vanilla humanoid / '+str(yaw),fill='white')
 d.text((8,412),'NOT IN GAME / victim feet: +0.18 up, 0.7 forward / diagnostic blue material',fill='white');out.save(O/'grab-held-reference.png')
 sheet('escape.png',[('EYES OFF 19<=t<49',[(a,'ESCAPE_'+str(t),-35) for t in [0,19,30,48,49]]),('RETURN',[(a,'ESCAPE_'+str(t),-35) for t in [49,60,87,102,131]])])
 silhouette=Image.new('RGB',(1600,840),'#1f2a30');d=ImageDraw.Draw(silhouette)
 for r,key in enumerate(['quad','upright']):
  for c,yaw in enumerate([0,90,180,-35]):
   silhouette.paste(render(a['poses'][key],tex,.68,yaw,flat=True),(c*400,r*420+20));d.text((c*400+8,r*420+3),key+' / silhouette / NOT IN GAME',fill='white')
 silhouette.save(O/'silhouette.png')
 # Source close-ups use exact vertices, shifted camera target rather than invented detail.
 details=Image.new('RGB',(1200,440),'#1f2a30');d=ImageDraw.Draw(details)
 for c,(label,names,anchor) in enumerate([('HEAD',['head','muzzle','nose','jaw','ear','innerEar','brow','eye','fang','tooth','cheek'],[0,0,-15]),('FRONT PAW',['handL','clawL','hookL'],[-6,20,-10.5]),('REAR PAW',['footL','toeL'],[-3.7,20,3.2])]):
  pose=a['poses']['quad'];faces=[]
  selected=[f for f in pose['faces'] if any(f['part'].startswith(n) for n in names)]
  verts=np.array([v[:3] for f in selected for v in f['vertices']]);center=(verts.min(0)+verts.max(0))/2
  for f in selected:faces.append(dict(f,vertices=[[v[0]-center[0],v[1]-center[1]+10,v[2]-center[2],v[3],v[4]] for v in f['vertices']]))
  details.paste(render({'faces':faces},tex,.68,-25,scale=13,eye=eye),(c*400,25));d.text((c*400+8,5),label+' / actual native mesh',fill='white')
 d.text((8,423),'NOT IN GAME / source UV / detail cameras recentered; scale differs from main A/B',fill='white');details.save(O/'details.png')
 wolftex=Image.open(cur/'grey_wolf.png')
 compare=Image.new('RGB',(1200,430),'#1f2a30');d=ImageDraw.Draw(compare)
 for c,(pose,t,depth,label) in enumerate([(a['poses']['greywolf_reference'],wolftex,1,'GREY WOLF (unchanged reference)'),(a['poses']['quad'],tex,.68,'CURSED quad'),(a['poses']['upright'],tex,.68,'CURSED upright')]):
  compare.paste(render(pose,t,depth,-35,scale=5,perspective=True),(c*400,20));d.text((c*400+5,3),label,fill='white')
 d.text((8,412),'NOT IN GAME / native source perspective / same scale, light and camera',fill='white');compare.save(O/'greywolf-perspective-reference.png')
 for action,count in [('rise',17),('drop',13),('escape',132)]:
  frames=[]
  for t in range(count):
   im=render(a['poses'][action+'_all_'+str(t)],tex,.68,-35,size=(360,400),scale=5,eye=eye);ImageDraw.Draw(im).text((8,8),action+' t='+str(t)+' / NOT IN GAME',fill='white');frames.append(im)
  frames[0].save(O/(action+'.gif'),save_all=True,append_images=frames[1:],duration=75,loop=0)
 print('Full native visual matrix rendered')
if __name__=='__main__':main();extras()
