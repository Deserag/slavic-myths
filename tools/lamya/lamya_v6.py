import os
os.environ['OPENBLAS_NUM_THREADS']='1'
from pathlib import Path
# Reuse the actual V5 mesh, excluding its old render/animation entry points.
source=Path(__file__).with_name('lamya_v5.py').read_text().split('print(dict(count),len(F),flush=True)\nif __name__')[0]
source=source.replace('def draw_head(origin, side, scale):','def draw_head(origin, side, scale):\n    global group\n    headgroup=group').replace('    section(lower,','    group=f"jaw{side}"\n    section(lower,').replace("'jaw shell')\n    for s", "'jaw shell')\n    group=headgroup\n    for s").replace('            prism(H(xx,yy,hi-.04)','            group=f"jaw{side}"\n            prism(H(xx,yy,hi-.04)').replace(".21*scale,.015,W,5,'tooth')\n        p1=",".21*scale,.015,W,5,'tooth')\n            group=headgroup\n        p1=")
exec(source)
from scipy.spatial.transform import Rotation
from scipy.spatial import cKDTree
from PIL import Image
from concurrent.futures import ProcessPoolExecutor

def sm(x):
 x=np.clip(x,0,1);return x*x*(3-2*x)
def rot(v,p,a,ang):return (v-p)@Rotation.from_rotvec(u(a)*ang).as_matrix().T+p
V=np.concatenate(F); SPLIT=np.cumsum([len(f) for f in F])[:-1]; GV=np.repeat(np.array(GROUP),[len(f) for f in F]); FLOOR=V[:,2].min()
P,X,Y,TAN,RAD=T; S=np.r_[0,np.cumsum(np.linalg.norm(np.diff(P,axis=0),axis=1))]; U=S/S[-1]; tailmask=GV=='tail'; tv=V[tailmask]; _,ix=cKDTree(P).query(tv,k=2); i=np.minimum(np.min(ix,axis=1),len(P)-2); seg=P[i+1]-P[i]; a=np.clip(np.sum((tv-P[i])*seg,axis=1)/np.sum(seg*seg,axis=1),0,1); ts=S[i]+a*(S[i+1]-S[i])
def ip(arr,s):return np.stack([np.interp(s,S,arr[:,j]) for j in range(3)],axis=1)
def norm(a):return a/np.maximum(np.linalg.norm(a,axis=1,keepdims=True),1e-8)
ot=norm(ip(TAN,ts));ox=norm(ip(X,ts));ox=norm(ox-ot*np.sum(ox*ot,axis=1,keepdims=True));oy=np.cross(ot,ox);d=tv-ip(P,ts); local=np.stack([np.sum(d*b,axis=1) for b in (ox,oy,ot)],axis=1)
def tail(q,t,mode,p):
 if mode=='glide':
  ss=S; path=np.c_[1.15*sm(ss/17)*np.sin(2*np.pi*t-ss*.14),ss-9*(1-np.exp(-ss/8)),RAD*.94+.35+(22-RAD[0]*.94-.35)*np.exp(-ss/7)+4*sm((U-.88)/.12)];path[0]=P[0]
 elif mode.startswith('tail'):
  th=p*sm((U-.08)/.92)*(1.7 if mode=='tail_sweep' else .7);path=P.copy();path[:,0]=P[:,0]*np.cos(th)-P[:,1]*np.sin(th);path[:,1]=P[:,0]*np.sin(th)+P[:,1]*np.cos(th)
  if mode=='tail_sting':path[:,2]+=p*17*sm((U-.50)/.50);path[:,1]-=p*20*sm((U-.5)/.5)
 else:return
 nt=norm(np.gradient(path,axis=0));xx=[];x=X[0].copy()
 for tt in nt:
  if abs(x@tt)>.97:x=np.array([1.,0,0]) if abs(tt[0])<.9 else np.array([0.,1,0])
  x=u(x-tt*(x@tt));xx.append(x)
 bt=norm(ip(nt,ts));bx=norm(ip(np.array(xx),ts));bx=norm(bx-bt*np.sum(bx*bt,axis=1,keepdims=True));by=np.cross(bt,bx);q[tailmask]=ip(path,ts)+local[:,0,None]*bx+local[:,1,None]*by+local[:,2,None]*bt
MODES=['glide','cast','fire','solar','wind','claw','tail_sweep','tail_sting','drought','combo_fire_wind','combo_sun_wind','combo_fire_sun','ultimate']
LABEL=['CRAWL / EXTENDED TAIL','MAGIC WIND-UP','FIRE BREATH','SOLAR VERDICT','DESERT BREATH','CLAW / BITE','TAIL SWEEP','STINGER STRIKE','DROUGHT CURSE','FIRE + WIND','SUN + WIND','FIRE + SUN','LAST OASIS / ULTIMATE']
HEADS={'fire':[-1],'solar':[0],'wind':[1],'claw':[-1,0],'drought':[0],'combo_fire_wind':[-1,1],'combo_sun_wind':[0,1],'combo_fire_sun':[-1,0],'ultimate':[-1,0,1],'cast':[-1,0,1]}
def deform(t,mode):
 q=V.copy();w=2*np.pi*t;g=mode=='glide';p=0 if g else sm(t/.32)*(1-sm((t-.72)/.28));hit=0 if g or mode=='cast' else sm((t-.32)/.15)*(1-sm((t-.69)/.17));spell=mode in ('cast','solar','drought','combo_sun_wind','combo_fire_sun','ultimate');tail(q,t,mode,.35*p+.65*hit);tips={}
 for side in [-1,0,1]:
  spec=next(e for e in neck_specs if e[3]==side);root=np.array(spec[0][0]);end=np.array(spec[0][-1]);phase=w+side*1.8;k=float(side in HEADS.get(mode,[]));shift=np.array([.45*np.sin(phase)+side*2.3*p*k,.5*np.cos(phase)+2.4*p*k-4.8*hit*k,.35*np.sin(phase+.6)+(5.3 if spell else 2.0)*p*k-3*hit*k]);axis=end-root;m=GV==f'neck{side}';v=V[m];weight=sm(((v-root)@axis)/(axis@axis));q[m]=v+weight[:,None]*shift
  f=u([side*.72,-1,.045]) if side else u([0,-1,.055]);a=u(np.cross([0,0,1],f));up=u(np.cross(f,a));scale=spec[2]*.75;jawpivot=end+scale*(-1.4*f-1.9*up);yaw=.085*np.sin(phase)+side*.13*p*k;pitch=-.16*p*k+.35*hit*k
  for typ in ('head','jaw'):
   m=GV==f'{typ}{side}';v=V[m];v=rot(v,jawpivot,a,.20*(1-p*k)-.27*hit*k) if typ=='jaw' else v;q[m]=rot(rot(v,end,[0,0,1],yaw),end,a,pitch)+shift
  tips[side]=rot(rot(end+scale*(7*f-.7*up),end,[0,0,1],yaw),end,a,pitch)+shift
 for side in [-1,1]:
  m=GV==f'arm{side}';v=V[m].copy();sh=np.array([side*6.4,.35,40.9]);el=np.array([side*12.4,-1.1,35.8]);wr=np.array([side*17.9,-5.,30.7]);ax=u(wr-el)
  if g:lift=.15*np.sin(w+side);sweep=side*.05*np.sin(w);yaw=.03*np.cos(w)
  elif spell:lift=-1.25*p+.40*hit;sweep=side*(.95*p-.22*hit);yaw=side*(-.24*p+.30*hit)
  elif mode=='claw':lift=-.55*p+1.0*hit;sweep=side*(.58*p-.55*hit);yaw=side*(-.65*p+1.0*hit)
  else:lift=-.65*p;sweep=side*.36*p;yaw=side*.12*hit
  wt=sm(((v-wr)@ax+.8)/2);v+=wt[:,None]*(rot(v,wr,[0,1,0],side*.24*p)-v);wt=sm(((V[m]-el)@ax+1)/3);v+=wt[:,None]*(rot(v,el,[1,0,0],lift)-v);q[m]=rot(rot(v,sh,[0,1,0],-sweep),sh,[0,0,1],yaw)
 m=GV=='halo';q[m]=rot(q[m],halo_center,[0,1,0],2*np.pi*t if spell else .12*np.sin(w));m=GV=='skirt';v=q[m];wt=sm((28-v[:,2])/18);v[:,0]+=wt*(.6+1.5*p)*np.sin(w-v[:,2]*.25);v[:,1]+=wt*(.6+1.8*p)*np.cos(w-v[:,2]*.20);q[m]=v
 m=~tailmask;v=q[m];wt=sm((v[:,2]-20)/22);v[:,1]+=wt*(1.1*p-2.3*hit);v[:,2]+=wt*(.45*np.sin(w)+1.5*p);q[m]=v;dy=-14*t if g else 0;q[:,1]+=dy;q[:,2]-=FLOOR
 for s in tips:tips[s]=tips[s]+[0,1.1*p-2.3*hit+dy,1.5*p-FLOOR]
 return np.split(q,SPLIT),tips,p,hit

def effects(ax,mode,t,h,p,a):
 if mode=='glide':return
 rng=np.random.default_rng(120+int(t*100));colors={-1:'#f04b1c',0:'#ffbf27',1:'#63b9ce'}
 def ring(x,y,z,r,c):
  th=np.linspace(0,2*np.pi,80);ax.plot(x+r*np.cos(th),y+r*np.sin(th),np.full_like(th,z),color=c,lw=2,alpha=.8)
 def stream(side,c,length=37,width=8):
  n=140;v=rng.uniform(0,length,n);ang=rng.uniform(0,2*np.pi,n);rr=v/length*width*rng.random(n);o=h[side];ax.scatter(o[0]+side*.18*v+rr*np.cos(ang),o[1]-v,o[2]-.60*v+rr*np.sin(ang),s=6+10*rng.random(n),c=c,alpha=.7,depthshade=False,edgecolors='none')
 if p>.15:
  for side in HEADS.get(mode,[]):o=h[side];ax.scatter(*o,s=80*p,c=colors[side],alpha=.9,depthshade=False);ring(*o,2.3*p,colors[side])
 if a<.08:return
 if mode=='fire':stream(-1,colors[-1])
 elif mode=='wind':stream(1,colors[1],40,12)
 elif mode=='solar':
  for x,y in [(-13,-22),(0,-28),(13,-22)]:ring(x,y,.8,3.5,colors[0]);ax.plot([0,x],[10,y],[55,1],color=colors[0],lw=4*a)
 elif mode=='claw':
  th=np.linspace(-1.2,1.2,40);ax.plot(17*np.sin(th),-16-5*np.cos(th),24+4*np.sin(th),color='#e6b653',lw=3*a)
 elif mode.startswith('tail'):
  for r in [5,9,13]:ring(15,-16 if mode=='tail_sting' else 7,1,r*a,'#d49c49')
 elif mode=='drought':
  for r in [9,17,26]:ring(0,-10,.7,r*a,'#b37b31')
  for k in range(20):
   x,y=rng.uniform(-25,25,2);ax.plot([x,x+1.5,x+3],[y,y-1.5,y+.5],[.6]*3,color='#a55a2e',alpha=.75)
 elif mode in ('combo_fire_wind','combo_sun_wind','ultimate'):
  for k in range(4):
   z=np.linspace(1,43 if mode=='ultimate' else 28,100);th=z*.36+6*t+k*np.pi/2;rr=5+z*.13;ax.plot(rr*np.cos(th),-22+rr*np.sin(th),z,color='#d6a74a' if mode=='combo_sun_wind' else '#f15d20',lw=2.5*a)
  if mode=='ultimate':
   for r in [15,24,34]:ring(0,-12,1,r*a,'#ff811d')
   ax.plot([0,0],[9,-22],[55,1],color='#ffcf29',lw=8*a)
 elif mode=='combo_fire_sun':
  stream(-1,colors[-1],31,6)
  for x,y in [(-12,-23),(0,-27),(12,-23)]:ring(x,y,.6,5,colors[0]);ax.plot([0,x],[10,y],[55,1],color=colors[0],lw=3*a)
def frame(job):
 mode,i,n=job;t=i/(n-1);fs,h,p,a=deform(t,mode);fig=plt.figure(figsize=(5,5),dpi=86,facecolor='#edf0ef');ax=fig.add_subplot(111,projection='3d');ax.set_proj_type('ortho');ax.add_collection3d(Poly3DCollection(fs,facecolors=C,edgecolors='none',linewidths=0,antialiased=False,zsort='average'));g=mode=='glide';yl=(-32,120) if g else (-48,33);xl=(-35,35) if g else (-38,38);ax.set(xlim=xl,ylim=yl,zlim=(0,81),xlabel='X',ylabel='Z',zlabel='Y');ax.set_box_aspect((xl[1]-xl[0],yl[1]-yl[0],81));ax.view_init(elev=24 if g else 16,azim=-35 if g else -63);ax.tick_params(labelsize=6);effects(ax,mode,t,h,p,a);fig.subplots_adjust(left=.01,right=.97,bottom=.015,top=.91);fig.text(.045,.965,'LAMYA V6 / '+LABEL[MODES.index(mode)],fontsize=10,weight='bold');phase='CRAWL' if g else 'WIND-UP' if t<.34 or mode=='cast' and t<.72 else 'ACTIVE' if t<.72 else 'RECOVERY';fig.text(.045,.925,phase,fontsize=8);fig.canvas.draw();im=Image.fromarray(np.asarray(fig.canvas.buffer_rgba())[:,:,:3].copy());plt.close(fig);return im
if __name__=='__main__':
 import sys
 modes=sys.argv[1:] or MODES;n=7
 with ProcessPoolExecutor(max_workers=5) as pool:
  for mode in modes:
   imgs=list(pool.map(frame,[(mode,i,n) for i in range(n)]));imgs[0].save(O/f'lamya_v6_{mode}.gif',save_all=True,append_images=imgs[1:],duration=[200,180,260,150,160,200,250],loop=0,disposal=2);imgs[3].save(O/f'lamya_v6_{mode}_peak.png');print('DONE',mode,flush=True)
