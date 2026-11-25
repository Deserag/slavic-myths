import numpy as np
import os
if not os.environ.get('INDRIK_HEADLESS'):
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from pathlib import Path
import json

OUT = Path(os.environ.get('INDRIK_WORK', str(Path(__file__).resolve().parent)))
V = {}
PALETTE = {'stone':'#514b3e','stone_light':'#645b49','belly':'#8b7550','armor':'#a88b52','armor_light':'#c2a16a','horn':'#e4d3a2','horn_dark':'#bda779','eye':'#60e7dc','crystal':'#42bdb5','crystal_dark':'#267e80','mouth':'#271f1a','nostril':'#31291f','gum':'#775044'}

def ell(c,r,m, condition=None):
    c=np.array(c); r=np.array(r)
    for x in range(int(np.floor(c[0]-r[0])),int(np.ceil(c[0]+r[0]))):
        for y in range(int(np.floor(c[1]-r[1])),int(np.ceil(c[1]+r[1]))):
            for z in range(int(np.floor(c[2]-r[2])),int(np.ceil(c[2]+r[2]))):
                p=(x,y,z); q=np.array(p)+.5
                if np.sum(((q-c)/r)**2)<=1 and (condition is None or condition(q)):
                    V[p]=m

def box(x,y,z,w,h,d,m):
    for i in range(x,x+w):
        for j in range(y,y+h):
            for k in range(z,z+d):V[i,j,k]=m

def tube(points,radii,mat):
    for i in range(len(points)-1):
        a,b=np.array(points[i]),np.array(points[i+1]); ra,rb=np.array(radii[i]),np.array(radii[i+1]); n=max(2,int(np.linalg.norm(b-a)*4))
        for t in np.linspace(0,1,n):ell(a*(1-t)+b*t, np.maximum(.58,ra*(1-t)+rb*t),mat)

# Coordinate convention: X lateral, Y up, Z toward tail; head faces -Z.
ell((0,12,-3),(6.6,8,12.5),'stone')
ell((0,13,-8),(7,8,7.5),'stone')
ell((0,11,5),(5.5,6.5,7),'stone')
ell((0,10,-5),(5.4,6,10),'belly',lambda q:q[1]<9.8)
ell((0,15,-12),(5.7,6,5.2),'belly')
# Dorsal carapace, sculpted following the back curve.
for z in range(-13,10):
    y=19.8-0.016*(z+6)**2
    width=5.7-0.012*(z+5)**2
    ell((0,y-.6,z+.5),(max(2.7,width),2.0,1.1),'armor')
    if z%3==0:
        ell((0,y+.2,z+.5),(max(2.5,width-.5),1.8,1.05),'armor_light')
# Armored tail follows a slight sideward curve.
pts=[(0,10,6),(0,8.6,11),(.4,6.8,15),(1.1,5.7,19),(1.8,5.2,22),(2.2,5.4,24)]
rads=[(4.0,4.4,3.3),(3.2,3.0,2.6),(2.55,2.3,2.1),(1.95,1.75,1.65),(1.2,1.15,1.15),(.65,.65,.65)]
tube(pts,rads,'stone')
for z in range(8,23,2):
    idx=next(i for i in range(len(pts)-1) if pts[i][2]<=z<=pts[i+1][2]); t=(z-pts[idx][2])/(pts[idx+1][2]-pts[idx][2]); c=np.array(pts[idx])*(1-t)+np.array(pts[idx+1])*t; r=np.array(rads[idx])*(1-t)+np.array(rads[idx+1])*t
    ell((c[0],c[1]+r[1]-.2,z),(r[0]*.84,1.35,1.25),'armor_light' if z%4==0 else 'armor')
# Load-bearing quadruped limbs. The toes are part of the paw, not long ivory protrusions.
for s in [-1,1]:
    # Shoulder blends into the thorax, then a distinct elbow and wrist.
    ell((s*5.7,14,-8.5),(3.15,5.3,4.0),'stone_light')
    tube([(s*5.8,13,-9),(s*6.8,8.3,-7.7),(s*6.9,5.0,-9.0),(s*6.9,3.1,-10.3)],[(2.9,3.4,3),(2.45,2.8,2.6),(2.0,2.4,2.0),(1.9,1.9,1.9)],'stone')
    ell((s*6.8,8.3,-7.7),(2.55,2.45,2.65),'stone_light')
    ell((s*6.9,3.4,-10.2),(2.0,1.9,2.2),'stone_light')
    # Wide heel and thick padded palm; three forward toes with very short dark claws.
    ell((s*6.9,1.65,-11.0),(2.65,1.7,2.8),'stone')
    box(int(np.floor(s*6.9-1.5)),0,-13,4,2,4,'stone')
    for dx in [-1.65,0,1.65]:
        x=s*6.9+dx
        ell((x,1.25,-13.1),(1.0,1.3,1.65),'stone_light')
        ell((x,.85,-14.35),(.74,.78,.9),'horn_dark')
    # Shoulder ridge and overlapping scale plates, stopping above elbow.
    for j in range(3):
        ell((s*(7.3+.18*j),16-j*2,-9+j*.5),(1.25,1.25,2.1),'stone_light' if j%2==0 else 'stone')
    # Hind limb: pronounced thigh, backward hock and forward foot.
    ell((s*4.7,11.0,4.2),(3.0,4.8,3.8),'stone_light')
    tube([(s*4.7,10.7,4.1),(s*5.9,7,3.7),(s*6.1,4,7.1),(s*6.0,2.1,5.7)],[(2.8,3.5,2.9),(2.3,2.5,2.2),(1.65,2,1.7),(1.65,1.65,1.65)],'stone')
    ell((s*6,1.4,4.9),(2.1,1.5,2.5),'stone')
    for dx in [-1.25,0,1.25]:
        ell((s*6+dx,1.0,3.1),(.85,1.0,1.35),'stone_light')
        ell((s*6+dx,.65,2.1),(.6,.65,.75),'horn_dark')
    # Ochre side plates with thin glowing mineral veins, not floating cubes.
    for i,z in enumerate(range(-12,9,3)):
        yy=18.7-.019*(z+6)**2
        xx=5.25-.011*(z+5)**2
        ell((s*xx,yy,z+.25),(1.4,1.6,1.65),'armor')
        ell((s*(xx+.75),yy-.8,z+.25),(.72,.73,1.25),'crystal' if i%3!=1 else 'crystal_dark')
# Beast-like head and lowered muzzle, distinct from a dragon.
ell((0,17.2,-16),(4.5,4.7,4.5),'armor')
ell((0,15.1,-19.0),(3.9,2.65,3.0),'stone')
ell((0,13.1,-19.1),(3.25,1.6,2.5),'stone_light')
# Dark mouth seam wraps around the front and sides.
for p in list(V):
    x,y,z=p; xx=x+.5; yy=y+.5; zz=z+.5
    if zz < -18.1 and 13.45<yy<14.55 and abs(xx)<3.7:
        V[p]='mouth'
for s in [-1,1]:
    box(-2 if s<0 else 1,15,-22,1,1,1,'nostril')
    box(-3 if s<0 else 2,13,-20,1,2,1,'horn')
    ell((s*4,17.8,-17.9),(1.05,1.25,1.7),'mouth')
    box(-5 if s<0 else 4,17,-19,1,1,2,'eye')
    ell((s*3.85,19.0,-17.3),(1.35,.85,2.15),'armor_light')
    ell((s*3.6,15.5,-16.6),(1.5,2.05,2.2),'armor')
    # Side horns swept rearwards.
    tube([(s*3.2,20,-16),(s*4.1,22,-15),(s*5,23.4,-13),(s*5.7,24.8,-11.7)],[(1.7,1.5,1.6),(1.35,1.3,1.4),(.95,1,1.0),(.58,.75,.75)],'horn')
# Signature central horn, curved slightly back, with broad root.
tube([(0,20.2,-18.5),(0,22.7,-19),(0,25,-18.8),(0,27.2,-17.8),(0,29.15,-16.8)],[(1.9,1.6,1.9),(1.6,1.7,1.65),(1.2,1.45,1.25),(.85,1.15,.85),(.55,.7,.62)],'horn')
# Three distinct staggered rows of dorsal spikes. Side rows lean outward.
for row in [-1,0,1]:
    zs=range(-10,10,3) if row==0 else range(-9,10,3)
    for i,z in enumerate(zs):
        yy=20.1-.016*(z+6)**2
        xx=0 if row==0 else row*(3.5-.005*(z+6)**2)
        h=(5.5 if row==0 else 3.7)-.10*(z+10)
        # broader shoulder spikes, taper toward the tail
        base=1.55 if row==0 else 1.25
        tube([(xx,yy-.45,z),(xx+row*.35,yy+h*.42,z+.35),(xx+row*.85,yy+h*.84,z+1.05)],[(base,1.25,base),(base*.70,1.1,base*.72),(.59,.72,.61)],'horn_dark')
        tube([(xx+row*.5,yy+h*.57,z+.65),(xx+row*1.0,yy+h,z+1.25)],[(.85,.95,.9),(.55,.65,.55)],'horn')
for z in [11,14,17,20]:
    i=next(i for i in range(len(pts)-1) if pts[i][2]<=z<=pts[i+1][2]);t=(z-pts[i][2])/(pts[i+1][2]-pts[i][2]); c=np.array(pts[i])*(1-t)+np.array(pts[i+1])*t;r=np.array(rads[i])*(1-t)+np.array(rads[i+1])*t
    tube([(c[0],c[1]+r[1],z),(c[0],c[1]+r[1]+2.2-(z-11)*.08,z+.8)],[(.95,.95,1.0),(.58,.62,.58)],'horn_dark')

if os.environ.get('INDRIK_HEADLESS'):
    def to_rgb(c): return tuple(int(c[i:i+2],16)/255 for i in (1,3,5))
else:
    from matplotlib.colors import to_rgb
    from PIL import Image
from collections import deque
from scipy.spatial import cKDTree
import math, sys
DIRS=np.array([(-1,0,0),(1,0,0),(0,-1,0),(0,1,0),(0,0,-1),(0,0,1)])
FACES=np.array([[(0,0,0),(0,0,1),(0,1,1),(0,1,0)],[(1,0,0),(1,1,0),(1,1,1),(1,0,1)],[(0,0,0),(1,0,0),(1,0,1),(0,0,1)],[(0,1,0),(0,1,1),(1,1,1),(1,1,0)],[(0,0,0),(0,1,0),(1,1,0),(1,0,0)],[(0,0,1),(1,0,1),(1,1,1),(0,1,1)]])
# Ensure the rest-pose volume is connected; preserve the same method as V1.
remaining=set(V); components=[]
while remaining:
    root=min(remaining); remaining.remove(root); component={root}; queue=deque([root])
    while queue:
        x,y,z=queue.popleft()
        for dx,dy,dz in DIRS:
            n=(x+dx,y+dy,z+dz)
            if n in remaining:remaining.remove(n);component.add(n);queue.append(n)
    components.append(component)
components.sort(key=len,reverse=True); main=components[0]
for comp in components[1:]:
    ordered=sorted(main); points=sorted(comp); ds,inds=cKDTree(ordered).query(points,p=1)
    k=int(np.argmin(ds)); q=list(points[k]); dest=ordered[inds[k]]; m=V[points[k]]
    for axis in (1,2,0):
        while q[axis]!=dest[axis]:
            q[axis]+=1 if dest[axis]>q[axis] else -1; V.setdefault(tuple(q),m); main.add(tuple(q))
    main.update(comp)
# V3 rig: pelvis, lumbar, thorax, neck, skull, 6 tail links, 12 limb bones.
pivots={'body':(0,11,4),'lumbar':(0,11.5,0),'chest':(0,12,-5),'neck':(0,15,-11),'head':(0,16,-15)}
parents={'lumbar':'body','chest':'lumbar','neck':'chest','head':'neck'}
for i,pt in enumerate([(0,10,8),(.1,8.8,11),(.4,7.4,14),(1,6.2,17),(1.5,5.4,20),(2,5.3,23)]):
    pivots[f'tail{i}']=pt;parents[f'tail{i}']='body' if i==0 else f'tail{i-1}'
for s in [-1,1]:
    for kind,coords in [('F',[(s*5.8,13,-9),(s*6.8,8.3,-7.7),(s*6.9,3.1,-10.3)]),('H',[(s*4.7,10.7,4.1),(s*5.9,7,3.7),(s*6,2.1,5.7)])]:
        name=kind+('L' if s<0 else 'R')
        for j,pt in enumerate(coords):
            pivots[name+str(j)]=pt;parents[name+str(j)]=name+str(j-1) if j else ('chest' if kind=='F' else 'body')
def owner(p):
    x,y,z=np.array(p)+.5;ax=abs(x);side='L' if x<0 else 'R'
    if y<6 and z<-4 and ax>2.5:return 'F'+side+str(2 if y<3.2 else 1)
    if y<5.5 and -1<z<10 and ax>2.5:return 'H'+side+str(2 if y<2.6 else 1)
    if ax>3.6 and -15.5<z<-4 and y<16 and (ax>5.3 or y<8):return 'F'+side+str(0 if y>=8.3 else 1 if y>=3.2 else 2)
    if ax>3.2 and 0<z<9.7 and y<13.4 and (ax>4.5 or y<6):return 'H'+side+str(0 if y>=7 else 1 if y>=2.6 else 2)
    if z<-14 or (y>20 and z<-11):return 'head'
    if z<-10.5:return 'neck'
    if z>=8.5:return 'tail'+str(min(5,max(0,int((z-8.5)//3))))
    return 'chest' if z<-2 else 'lumbar' if z<3 else 'body'
G={p:owner(p) for p in V}
# Exposed whole-body faces for rest; closed joint surfaces for posed geometry.
restfaces=[]; restcolors=[]; bonefaces={k:[] for k in pivots}; bonecolors={k:[] for k in pivots}; shell={}; articulated={}
for p,m in sorted(V.items()):
    seed=(p[0]*73856093 ^ p[1]*19349663 ^ p[2]*83492791)&0xffffffff
    base=np.array(to_rgb(PALETTE[m])); variation=(seed%17-8)*.008; group=G[p]
    for i,d in enumerate(DIRS):
        nb=tuple(np.array(p)+d); ext=nb not in V
        if not ext and G[nb]==group:continue
        face=FACES[i]+p; color=np.clip(base*([.80,.93,.69,1.12,.89,.99][i]+variation),0,1)
        bonefaces[group].append(face);bonecolors[group].append(color);articulated[p]=m
        if ext:restfaces.append(face);restcolors.append(color);shell[p]=m
bonefaces={k:np.array(v,dtype=float) for k,v in bonefaces.items()};bonecolors={k:np.array(v) for k,v in bonecolors.items()}
restfaces=np.array(restfaces,dtype=float);restcolors=np.array(restcolors)
mins=np.min(list(V),axis=0); maxs=np.max(list(V),axis=0)+1
print('solid',len(V),'surface',len(shell),'rig shell',len(articulated),'dimensions',maxs-mins, 'components before repair',len(components),flush=True)
(OUT/'indrik_v3_geometry.json').write_text(json.dumps({'axes':'X width, Y up, Z length (head -Z)','dimensions':(maxs-mins).tolist(),'palette':PALETTE,'pivots':pivots,'parents':parents,'cuboids':[{'x':x,'y':y,'z':z,'w':1,'h':1,'d':1,'material':m,'bone':G[x,y,z]} for (x,y,z),m in articulated.items()]},separators=(',',':')))
def axis(ax,elev=20,azim=-54,lim=((-15,15),(-28,30),(0,34))):
    ax.set_proj_type('ortho');ax.view_init(elev=elev,azim=azim)
    ax.set_xlim(*lim[0]);ax.set_ylim(*lim[1]);ax.set_zlim(*lim[2]);ax.set_box_aspect(tuple(b-a for a,b in lim),zoom=1.06)
    ax.set_xlabel('X',labelpad=-3);ax.set_ylabel('Z',labelpad=-3);ax.set_zlabel('Y',labelpad=-3)
    ax.tick_params(labelsize=7,pad=0)
    for a in (ax.xaxis,ax.yaxis,ax.zaxis):
        a.pane.set_facecolor('#f6f8f9');a.pane.set_edgecolor('#d8dfe2');a._axinfo['grid']['color']='#d3dade';a._axinfo['grid']['linewidth']=.5
    ax.set_facecolor('white');ax.grid(True)
def add(ax,faces,colors):
    if len(faces): ax.add_collection3d(Poly3DCollection(faces[:,:,[0,2,1]],facecolors=colors,edgecolors='none',antialiased=False,linewidths=0,zsort='average'))
def static():
    fig=plt.figure(figsize=(11,8));ax=fig.add_subplot(111,projection='3d');axis(ax,23,-54);add(ax,restfaces,restcolors);ax.set_title(f'INDRIK V2  |  {len(shell):,} surface cuboids  |  articulated paws',fontweight='bold');fig.subplots_adjust(left=.015,right=.985,top=.94,bottom=.025);fig.savefig(OUT/'indrik_v2_hero.png',dpi=145);plt.close(fig)
    fig=plt.figure(figsize=(12,10));
    for i,(e,a,t) in enumerate([(8,-90,'Front'),(6,0,'Right side'),(12,90,'Rear'),(80,-90,'Top')]):
        ax=fig.add_subplot(2,2,i+1,projection='3d');axis(ax,e,a);add(ax,restfaces,restcolors);ax.set_title(t,fontweight='bold')
    fig.suptitle('INDRIK V2 / technical orthographic inspection',fontweight='bold');fig.subplots_adjust(left=.025,right=.975,bottom=.03,top=.93,wspace=.02,hspace=.08);fig.savefig(OUT/'indrik_v2_views.png',dpi=135);plt.close(fig)
    fig=plt.figure(figsize=(11,6));
    for i,(e,a,t) in enumerate([(15,-65,'Forepaws'),(6,0,'Fore & hind leg profile')]):
        ax=fig.add_subplot(1,2,i+1,projection='3d');axis(ax,e,a,((-12,12),(-17,11),(0,17))); mask=(restfaces[:,:,1].mean(1)<16.5)&(restfaces[:,:,2].mean(1)>-16)&(restfaces[:,:,2].mean(1)<10);add(ax,restfaces[mask],restcolors[mask]);ax.set_title(t,fontweight='bold')
    fig.suptitle('INDRIK V2 / load-bearing joints, toes and short claws',fontweight='bold');fig.subplots_adjust(left=.02,right=.98,top=.9,bottom=.035,wspace=.02);fig.savefig(OUT/'indrik_v2_paws.png',dpi=140);plt.close(fig)
# Rotation about anatomical X, Y, Z in model coordinates.
def rot(x=0,y=0,z=0):
    x,y,z=np.radians([x,y,z]);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
    return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
from scipy.spatial.transform import Rotation
P={k:np.array(v,float) for k,v in pivots.items()}
LEGS=['FL','FR','HL','HR']
def smooth(x):
    x=np.clip(x,0,1);return x*x*(3-2*x)
def keys(t,values):
    for (ta,a),(tb,b) in zip(values,values[1:]):
        if t<=tb:return np.asarray(a)+(np.asarray(b)-a)*smooth((t-ta)/(tb-ta))
    return np.asarray(values[-1][1])
def align(a,b):
    a=a/np.linalg.norm(a);b=b/np.linalg.norm(b);v=np.cross(a,b);s=np.linalg.norm(v);c=np.dot(a,b)
    if s<1e-9:return np.eye(3)
    return Rotation.from_rotvec(v/s*np.arctan2(s,c)).as_matrix()
def solve(n,target,pitch,M,O):
    k,j,f=[n+str(i) for i in range(3)];par=parents[k];A=M[par]@P[k]+O[par];base=M[par];v=P[j]-P[k];w=P[f]-P[j];L=np.linalg.norm(v);W=np.linalg.norm(w)
    D=target-A;d0=np.linalg.norm(D);u=D/max(d0,1e-8);d=np.clip(d0,abs(L-W)+.001,L+W-.001)
    bias=base@v;bias=bias-u*np.dot(bias,u);bias/=max(np.linalg.norm(bias),1e-9)
    a=(L*L-W*W+d*d)/(2*d);B=A+a*u+np.sqrt(max(0,L*L-a*a))*bias;C=A+d*u
    M1=align(base@v,B-A)@base;M2=align(M1@w,C-B)@M1;M3=rot(pitch,0,0)
    for b,mat,pos in [(k,M1,A),(j,M2,B),(f,M3,C)]:M[b]=mat;O[b]=pos-mat@P[b]
    return np.linalg.norm(C-target)
def matrices(a,s):
    M={};O={}
    for k,p in P.items():
        if k[:2] in LEGS:continue
        r=rot(*a.get(k,(0,0,0)));q=p-r@p
        if k=='body':M[k]=r;O[k]=q+s
        else:par=parents[k];M[k]=M[par]@r;O[k]=M[par]@q+O[par]
    errors={n:solve(n,*a['goals'][n],M,O) for n in LEGS}
    return M,O,errors
def transform(a,s):
    M,O,errors=matrices(a,s)
    return np.concatenate([bonefaces[k]@M[k].T+O[k] for k in pivots]),np.concatenate([bonecolors[k] for k in pivots])
def pose(k,t):
    a={};s=np.zeros(3);goals={n:P[n+'2'].copy() for n in LEGS};pitch={n:0 for n in LEGS};fx=''
    if k=='claw':
        u=float(keys(t,[(0,0),(.27,1),(.46,.7),(.55,0),(.67,-.23),(1,0)]));s[:]=[-.6*max(0,u),float(keys(t,[(0,0),(.27,-1.15),(.44,-.8),(.55,-1.65),(.68,-1.4),(1,0)])),float(keys(t,[(0,0),(.27,.3),(.44,-.8),(.55,-1.5),(.68,-1.3),(1,0)]))]
        a['chest']=(4*u,-6*u,4*u);a['neck']=(-3*u,4*u,-2*u);a['head']=(-4*u,3*u,0);a['lumbar']=(2*u,-2*u,1*u)
        dy,dz=keys(t,[(0,(0,0)),(.27,(5,1.5)),(.44,(4,-5.6)),(.55,(0,-5.1)),(.68,(0,-5.1)),(.83,(1.9,-2.4)),(1,(0,0))]);goals['FR']+=np.array([.2*max(0,u),dy,dz]);pitch['FR']=float(keys(t,[(0,0),(.27,-20),(.44,10),(.55,0),(.68,0),(.83,-8),(1,0)]))
        for i in range(6):a['tail'+str(i)]=(0,-3*u/(1+i*.2),0)
        label='Wind-up' if t<.27 else 'Reach / strike' if t<.55 else 'Contact / load' if t<.7 else 'Recovery'
    elif k=='tail':
        q=float(keys(t,[(0,0),(.28,-1),(.36,-.85),(.57,1),(.68,.9),(1,0)]));s[1]=-.55*abs(q);a['body']=(0,4*q,0);a['lumbar']=(0,-3*q,0);a['chest']=(0,-2*q,0);a['neck']=(0,-4*q,0);a['head']=(0,3*q,0)
        for i in range(6):
            tt=max(0,(t-i*.023)/(1-i*.023));v=float(keys(tt,[(0,0),(.28,-1),(.36,-.85),(.57,1),(.68,.75),(1,0)]));a['tail'+str(i)]=(1.5*v,(17 if i<3 else 13)*v,0)
        label='Coil' if t<.36 else 'Sequential sweep' if t<.68 else 'Recovery'
    elif k=='quake':
        u=float(keys(t,[(0,0),(.18,-.30),(.40,1),(.49,.93),(.57,0),(.68,-.38),(.82,.12),(1,0)]));s[1]=float(keys(t,[(0,0),(.18,-.8),(.4,-.9),(.57,-.7),(.68,-1.0),(1,0)]));a['body']=(1*u,0,0);a['lumbar']=(5*u,0,0);a['chest']=(9*u,0,0);a['neck']=(5*u,0,0);a['head']=(-7*u,0,0)
        for n in ['FL','FR']:
            dy,dz=keys(t,[(0,(0,0)),(.18,(0,0)),(.40,(3.8,1)),(.49,(3.6,-.9)),(.57,(0,-1)),(.74,(0,-1)),(.88,(.8,-.4)),(1,(0,0))]);goals[n]+=np.array([0,dy,dz]);pitch[n]=float(keys(t,[(0,0),(.4,-15),(.49,8),(.57,0),(1,0)]))
        for i in range(6):a['tail'+str(i)]=(-1.4*u,0,0)
        label='Brace / crouch' if t<.18 else 'Rear / lift both paws' if t<.49 else 'Ground slam' if t<.68 else 'Shockwave / recovery';fx='quake' if t>=.57 else ''
    a['goals']={n:(goals[n],pitch[n]) for n in LEGS}
    return a,s,label,fx
def clip_ground(f,c):
    yy=f[:,:,1];keep=np.all(yy>=0,axis=1);cross=np.where((np.min(yy,axis=1)<0)&(np.max(yy,axis=1)>0))[0];poly=list(f[keep]);cols=list(c[keep])
    for i in cross:
        p=f[i];out=[]
        for j in range(len(p)):
            u=p[j-1];v=p[j];a=u[1]>=0;b=v[1]>=0
            if a!=b:out.append(u+(v-u)*(-u[1]/(v[1]-u[1])))
            if b:out.append(v)
        if len(out)>=3:poly.append(np.array(out));cols.append(c[i])
    return poly,cols
def animate(job):
    k,n=job;frames=[];fig=plt.figure(figsize=(7,5.1),dpi=95);ax=fig.add_subplot(111,projection='3d');fig.subplots_adjust(left=.01,right=.99,top=.89,bottom=.04)
    for t in np.linspace(0,1,n,endpoint=k not in ('idle','walk','run','enraged')):
        ax.clear();a,s,label,fx=pose(k,t);axis(ax,52 if k=='tail' else 15,-12 if k=='claw' else -25,((-20,20),(-25,27),(0,33)));f,c=transform(a,s)
        if k in ['burrow','death']:
            poly,col=clip_ground(f,c)
            if poly:ax.add_collection3d(Poly3DCollection([p[:,[0,2,1]] for p in poly],facecolors=col,edgecolors='none',linewidths=0,antialiased=False))
        else:
            if fx=='glow':
                mask=(c[:,1]>c[:,0]*1.35)&(c[:,2]>c[:,0]*1.25);c=c.copy();c[mask]=np.minimum(1,c[mask]*(1.15+.4*math.sin(math.pi*t)))
            add(ax,f,c)
        if fx=='quake':
            r=2+25*np.clip((t-.57)/.43,0,1);q=np.linspace(0,2*np.pi,80);ax.plot(r*np.cos(q),-6+r*np.sin(q),np.full_like(q,.08),color='#af8538',lw=2)
        if fx=='dust':
            q=np.linspace(0,2*np.pi,25);r=3+5*abs(math.sin(t*9));ax.scatter(r*np.cos(q),s[2]-3+r*np.sin(q),np.full_like(q,.2),s=8,color='#c1a277',depthshade=False)
        if fx=='projectiles':
            u=(t-.48)/.34
            for ang in np.linspace(-.6,.6,5):
                x=15*u*math.sin(ang);z=-20-15*u*math.cos(ang);y=15+4*math.sin(math.pi*u);cube=np.array([x-.5,y-.5,z-.5])+FACES;add(ax,cube,np.tile([.18,.8,.76],(6,1)))
        ax.set_title(f'{k.upper()} / {label}',fontsize=10,fontweight='bold',pad=2);fig.canvas.draw();rgb=np.asarray(fig.canvas.buffer_rgba())[:,:,:3].copy();frames.append(Image.fromarray(rgb).convert('P',palette=Image.Palette.ADAPTIVE,colors=192))
    plt.close(fig);frames[0].save(OUT/(k+'.gif'),save_all=True,append_images=frames[1:],duration=110 if k in ('walk','run','idle','enraged') else 85,loop=0,disposal=2,optimize=False);print('Rendered',k,flush=True)
def diagnostic():
    fig=plt.figure(figsize=(12,5),dpi=120)
    for i,(k,t) in enumerate([('claw',.27),('tail',.57),('quake',.4)]):
        a,s,_,_=pose(k,t);M,O,_=matrices(a,s);ax=fig.add_subplot(1,3,i+1,projection='3d');axis(ax,22,-35,((-22,22),(-25,28),(0,35)))
        for b,par in parents.items():
            q=np.array([M[par]@P[par]+O[par],M[b]@P[b]+O[b]]);color='#df7045' if b[:2] in LEGS else '#168b9a' if b.startswith('tail') else '#5e6eb4';ax.plot(q[:,0],q[:,2],q[:,1],color=color,lw=2.2,marker='o',markersize=4)
        ax.set_title(k.upper(),fontweight='bold')
    fig.suptitle('INDRIK V3 / 23 bones / articulated skeleton');fig.subplots_adjust(left=.02,right=.98,top=.92,bottom=.04,wspace=.05);fig.savefig(OUT/'rig_v3.png');plt.close(fig)
if __name__=='__main__':
    diagnostic()
    for k in ['claw','tail','quake']:
        errors=[];support=[]
        for t in np.linspace(0,1,121):
            a,s,_,_=pose(k,t);M,O,e=matrices(a,s);errors.extend(e.values());support.extend(e[n] for n in LEGS if abs(a['goals'][n][0][1]-P[n+'2'][1])<1e-7)
        print(k,'max error',max(errors),'support error',max(support),flush=True)
    from multiprocessing import get_context
    with get_context('fork').Pool(3) as pool:pool.map(animate,[(k,30) for k in ['claw','tail','quake']])
