"""#77 actual native vertex/UV source rendering; NOT_IN_GAME, fixed camera/scale/light."""
from pathlib import Path
import gzip,json,math,hashlib
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/"docs/implementation/skim-77"

def render(pose,texture,depth,yaw,size=(400,400),scale=6,eye=None,flat=False,pitch=7,stats=None,perspective=False,emission=.30):
 w,h=size;rgb=np.full((h,w,3),(31,42,48),dtype=np.uint8);zbuf=np.full((h,w),np.inf);ids=np.full((h,w),"",dtype=object);t=np.asarray(texture.convert('RGBA'));et=np.asarray(eye.convert('RGBA')) if eye else None
 a=math.radians(yaw);b=math.radians(pitch)
 def project(vertices):
  v=np.array(vertices,dtype=float);v[:,2]*=depth;x=v[:,0]*math.cos(a)+v[:,2]*math.sin(a);z=v[:,2]*math.cos(a)-v[:,0]*math.sin(a);y=(v[:,1]-24)*math.cos(b)-z*math.sin(b);zz=z*math.cos(b)+(v[:,1]-24)*math.sin(b)
  factor=140/(140+zz) if perspective else 1
  return np.column_stack((w/2+x*scale*factor,(h/2 if pitch==90 else h-48)+y*scale*factor,zz,v[:,3],v[:,4]))
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
    glow=et[v,u];weight=glow[:,:,3:4]/255*emission;paint=np.clip(paint*(1-weight)+glow[:,:,:3]*weight,0,255).astype(np.uint8)
   ids[ymin:ymax+1,xmin:xmax+1][visible]=face["part"];rgb[ymin:ymax+1,xmin:xmax+1][visible]=paint[visible];zbuf[ymin:ymax+1,xmin:xmax+1][visible]=z[visible]
 if stats is not None:
  names,counts=np.unique(ids,return_counts=True);stats.update(dict(zip(names.tolist(),counts.tolist())))
 im=Image.fromarray(rgb);d=ImageDraw.Draw(im);d.line((8,h-48,w-8,h-48),fill='#8eaa9a');d.text((w-78,h-43),'floor / model px',fill='#adc0b8');return im

def data(label):
 result=json.loads(gzip.decompress((O/label/'native-poses.json.gz').read_bytes()))
 if label=='after':
  for name,expected in result.get('files',{}).items():assert hashlib.sha256((R/name).read_bytes()).hexdigest()==expected,'Recapture native mesh before rendering changed source: '+name
 return result
def main():
 old=data('before');new=data('after');oldtex=Image.open(O/'before/src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png');tex=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png')
 sheet=Image.new('RGB',(2080,920),'#1f2a30');d=ImageDraw.Draw(sheet)
 for row,(model,texture,title) in enumerate([(old,oldtex,'BEFORE'),(new,tex,'AFTER')]):
  for col,(yaw,label) in enumerate([(0,'FRONT'),(90,'RIGHT'),(180,'BACK'),(-35,'3/4')]):
   sheet.paste(render(model['poses']['IDLE_0'],texture,1,yaw,size=(520,420),scale=4),(col*520,row*450+23));d.text((col*520+8,row*450+5),title+' / '+label,fill='white')
 d.text((8,905),'NOT IN GAME / actual production ModelPart + UV / matched scale camera light / 16px=1block',fill='white');sheet.save(O/'matched-8-views.png')
 top=Image.new('RGB',(1040,555),'#1f2a30');d=ImageDraw.Draw(top)
 for col,(model,texture,title) in enumerate([(old,oldtex,'BEFORE'),(new,tex,'AFTER')]):
  pose=model['poses']['IDLE_0'];center=6.0 # identical model-space camera target for BEFORE/AFTER TOP
  aligned={'faces':[dict(f,vertices=[q[:2]+[q[2]-center]+q[3:] for q in f['vertices']]) for f in pose['faces']]}
  top.paste(render(aligned,texture,1,0,size=(520,520),scale=4,pitch=90),(col*520,22));d.text((col*520+8,5),title+' TOP / same scale',fill='white')
 top.save(O/'matched-top.png');print('SKIM77 native matched views rendered')
def extras():
 a=data('after');b=data('before');tex=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png');old=Image.open(O/'before/src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png')
 def sheet(filename,rows,columns=6,cell=(350,400),scale=3.2):
  image=Image.new('RGB',(columns*cell[0],len(rows)*(cell[1]+22)+24),'#1f2a30');d=ImageDraw.Draw(image)
  for r,(title,frames) in enumerate(rows):
   for c,(model,key,yaw) in enumerate(frames):
    texture=old if model is b else tex;image.paste(render(model['poses'][key],texture,1,yaw,size=cell,scale=scale),(c*cell[0],r*(cell[1]+22)+20));d.text((c*cell[0]+6,r*(cell[1]+22)+4),title+' / '+key,fill='white')
  d.text((8,image.height-18),'NOT IN GAME / actual native ModelPart UV / fixed scale light camera',fill='white');image.save(O/filename)
 sheet('actions-matched.png',[(title,[(model,key,-35) for key in ['CLAWS_11','LEAP_29','WHISTLE_22','ROAR_28','FLANK_24','GRIP_31']]) for model,title in [(b,'BEFORE'),(a,'AFTER')]])
 sheet('roar-clearance.png',[(title,[(a,prefix+str(t),-35) for t in [0,10,18,28,45,58]]) for prefix,title in [('all_ROAR_','REAR ALLOWED'),('low_ROAR_','REAR BLOCKED')]])
 rows=[]
 for move,ticks in [('AWAKEN',[0,14,30,45,52,60]),('CLAWS',[0,8,11,19,30,39]),('LEAP',[0,12,18,28,29,44]),('WHISTLE',[0,14,22,31,44,48]),('FLANK',[0,10,22,24,31,38]),('RAGE',[0,12,22,31,52,64]),('EXHAUSTED',[0,10,22,31,52,70]),('STAGGER',[0,10,19,28,31,40]),('RECOVER',[0,5,8,11,19,20])]:
  rows.append((move,[(a,move+'_'+str(t),-35) for t in ticks]))
 for start in range(0,len(rows),3):sheet('actions-'+str(start//3+1)+'.png',rows[start:start+3])
 sheet('grip-phases.png',[('GRIP',[(a,'all_GRIP_'+str(t),-35) for t in [0,14,18,21,31,41,45,65]])],columns=8)
 sheet('death-matched.png',[(title,[(model,('DEATH_' if model is b else 'all_DEATH_')+str(t),-35) for t in [0,26,52,75]]) for model,title in [(b,'BEFORE'),(a,'AFTER')]],columns=4)
 sheet('walk.png',[(str(speed),[(a,'walk_'+str(speed)+'_'+str(t),-35) for t in [0,8,16,24,32,40]]) for speed in [.3,.8]])
 # Source masks and CPU approximation, never an OpenGL/night-time proof.
 eyes=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/skim_beast_eyes.png').convert('RGBA');rage=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/skim_beast_rage.png').convert('RGBA')
 masks=Image.new('RGB',(1024,548),'#1f2a30');dm=ImageDraw.Draw(masks)
 for col,(mask,label) in enumerate([(eyes,'EYES: ONLY TWO EYE NETS'),(rage,'RAGE: EXISTING RED PIXELS ONLY')]):
  bg=Image.new('RGBA',(512,512),'#293139');bg.alpha_composite(mask);masks.paste(bg.convert('RGB'),(col*512,22));dm.text((col*512+8,4),label,fill='white')
 dm.text((8,534),'ACTUAL TRANSPARENT512px PNG MASKS / not an in-game render',fill='white');masks.save(O/'glow-masks.png')
 glow_sheet=Image.new('RGB',(2080,455),'#1f2a30');dg=ImageDraw.Draw(glow_sheet)
 for col,key in enumerate(['IDLE_0','RAGE_21','RAGE_64','DEATH_75']):
  pose=a['poses'][key];eye_pixels=np.array(eyes);red_pixels=np.array(rage);e=pose['eyeGlow'];r=pose['rageGlow']*.45
  eye_pixels[:,:,3]=(eye_pixels[:,:,3]*e).astype(np.uint8);red_pixels[:,:,3]=(red_pixels[:,:,3]*r).astype(np.uint8);combined=np.where((eye_pixels[:,:,3]>0)[:,:,None],eye_pixels,red_pixels)
  glow_sheet.paste(render(pose,tex,1,-35,size=(520,420),scale=4,eye=Image.fromarray(combined),emission=1),(col*520,20));dg.text((col*520+8,3),key+' / native eye='+str(round(e,2))+' rage='+str(round(r,2)),fill='white')
 dg.text((8,438),'NOT IN GAME / actual Java envelopes + actual PNG masks / CPU blend approximation, OpenGL/lighting owner-pending',fill='white');glow_sheet.save(O/'glow-source.png')
 # The native vanilla humanoid retains the server's feet anchor and a documented visual .9375 scale.
 marked=a['poses']['all_GRIP_31']['faces']+[dict(f,part='victim',vertices=[v[:3]+[.99,.99] for v in f['vertices']]) for f in a['poses']['victim']['faces']]
 diagnostic=tex.copy();ImageDraw.Draw(diagnostic).rectangle((505,505,511,511),fill='#548caa')
 out=Image.new('RGB',(1560,455),'#1f2a30');d=ImageDraw.Draw(out)
 for c,yaw in enumerate([0,90,-35]):
  out.paste(render({'faces':marked},diagnostic,1,yaw,size=(520,420),scale=4),(c*520,20));d.text((c*520+8,3),'GRIP31 + native humanoid / '+str(yaw),fill='white')
 d.text((8,439),'NOT IN GAME / held feet .55up,1.7forward / blue diagnostic material / grips upper leg; head clearance verified offline',fill='white');out.save(O/'grip-held-reference.png')
 silhouettes=Image.new('RGB',(2080,450),'#1f2a30');d=ImageDraw.Draw(silhouettes)
 for c,yaw in enumerate([0,90,180,-35]):
  silhouettes.paste(render(a['poses']['IDLE_0'],tex,1,yaw,size=(520,420),scale=4,flat=True),(c*520,20));d.text((c*520+8,3),'SILHOUETTE / '+str(yaw),fill='white')
 d.text((8,434),'NOT IN GAME / actual native silhouette',fill='white');silhouettes.save(O/'silhouette.png')
 # Approved art is shown whole, separately from actual model, never used as a texture.
 poster=Image.open(R/'docs/specs/1.7.3/06_References/Approved/APPROVED_Skim_Beast_4_views_and_attacks.png').convert('RGB');poster=poster.resize((672,840),Image.Resampling.LANCZOS)
 board=Image.new('RGB',(1712,880),'#1f2a30');board.paste(poster,(0,23));d=ImageDraw.Draw(board);d.text((8,5),'APPROVED OWNER POSTER1.7.3 / artistic reference',fill='white')
 for c,(yaw,pitch,label) in enumerate([(0,7,'FRONT'),(90,7,'SIDE'),(180,7,'BACK'),(0,90,'TOP')]):
  pose=a['poses']['IDLE_0']
  if pitch==90:
   v=np.array([v[:3] for f in pose['faces'] for v in f['vertices']]);center=(v[:,2].min()+v[:,2].max())/2;pose={'faces':[dict(f,vertices=[q[:2]+[q[2]-center]+q[3:] for q in f['vertices']]) for f in pose['faces']]}
  board.paste(render(pose,tex,1,yaw,size=(520,400),scale=3.4,pitch=pitch),(672+(c%2)*520,23+(c//2)*420));d.text((680+(c%2)*520,5+(c//2)*420),'ACTUAL SOURCE / '+label+' / NOT IN GAME',fill='white')
 d.text((8,865),'Poster scale is artistic. Native views use one common model scale/camera/light; in-game approval remains pending.',fill='white');board.save(O/'poster-to-source.png')
 hitbox=Image.new('RGB',(1560,455),'#1f2a30');d=ImageDraw.Draw(hitbox)
 corners=np.array([[x,y,z] for x in [-16.8,16.8] for y in [-23.2,24] for z in [-47.2,29.6]])
 for c,yaw in enumerate([0,90,-35]):
  im=render(a['poses']['IDLE_0'],tex,1,yaw,size=(520,420),scale=4);di=ImageDraw.Draw(im);aa=math.radians(yaw);bb=math.radians(7);x=corners[:,0]*math.cos(aa)+corners[:,2]*math.sin(aa);z=corners[:,2]*math.cos(aa)-corners[:,0]*math.sin(aa);y=(corners[:,1]-24)*math.cos(bb)-z*math.sin(bb);points=np.column_stack((260+x*4,372+y*4))
  for i in range(8):
   for bit in [1,2,4]:
    j=i^bit
    if i<j:di.line([tuple(points[i]),tuple(points[j])],fill='#d38a68',width=1)
  hitbox.paste(im,(c*520,20));d.text((c*520+8,3),'ACTUAL NATIVE MESH + FROZEN AABB / '+str(yaw),fill='white')
 d.text((8,439),'NOT IN GAME / AABB2.1W x4.8L x2.95H, forward center.55 / tail,mane,tusks may extend; collision unchanged',fill='white');hitbox.save(O/'frozen-hitbox.png')
 # Detail cameras are recentered, never substitute for the main same-scale comparison.
 details=Image.new('RGB',(1560,480),'#1f2a30');d=ImageDraw.Draw(details)
 for c,(label,allowed,scale) in enumerate([('HEAD+MAW',['head','jaw'],9),('LAYERED MANE',['neck']+['mane_'+str(i) for i in range(24)],7),('FOREPAW',['right_front_paw'],13)]):
  faces=[f for f in a['poses']['IDLE_0']['faces'] if f['part'] in allowed];v=np.array([v[:3] for f in faces for v in f['vertices']]);center=(v.min(0)+v.max(0))/2
  aligned={'faces':[dict(f,vertices=[[q[0]-center[0],q[1]-center[1]+8,q[2]-center[2],q[3],q[4]] for q in f['vertices']]) for f in faces]}
  details.paste(render(aligned,tex,1,-25,size=(520,440),scale=scale),(c*520,20));d.text((c*520+8,3),label+' / native detail',fill='white')
 d.text((8,463),'NOT IN GAME / detail cameras differ from main fixed-scale A/B',fill='white');details.save(O/'details.png')
 for move,end,step in [('ROAR',58,2),('LEAP',44,2),('DEATH',75,3)]:
  frames=[]
  for t in list(range(0,end,step))+[end]:
   im=render(a['poses']['all_'+move+'_'+str(t)],tex,1,-35,size=(520,420),scale=4);ImageDraw.Draw(im).text((8,8),move+' t='+str(t)+' / NOT IN GAME',fill='white');frames.append(im)
  frames[0].save(O/(move.lower()+'.gif'),save_all=True,append_images=frames[1:],duration=step*50,loop=0)
 print('SKIM77 full native visual matrix rendered')
if __name__=='__main__':main();extras()
