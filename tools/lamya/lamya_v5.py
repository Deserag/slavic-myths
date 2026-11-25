import numpy as np
from scipy.interpolate import CubicSpline
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.colors import to_rgb
from pathlib import Path
from collections import Counter

O=Path(__file__).resolve().parent
rng=np.random.default_rng(81)
F=[]
C=[]
GROUP=[]
group="tail"
count=Counter()
D=['#332b36','#483844','#57434b','#3b303c']
B=['#b69265','#c4a471','#d8bb87','#a8865f']
G='#b88738'; L='#e4b964'; K='#775028'; R='#8d3031'; W='#e0c49c'; A='#ffb32b'
light=np.array([-.4,-.7,.8]);light/=np.linalg.norm(light)

def u(v):
    v=np.array(v,float)
    n=np.linalg.norm(v)
    return v/max(n,1e-9)

def face(v,c):
    v=np.array(v)
    if len(v) < 3:
        return
    n=u(np.cross(v[1]-v[0],v[2]-v[0]))
    F.append(v)
    GROUP.append(group)
    base=np.array(to_rgb(c))
    shade=np.clip(base*(.58+.44*abs(n@light)+.12*max(0,n[2])),0,1)
    C.append(shade)

def solid(v,fs,c,k):
    count[k]+=1
    for i,f in enumerate(fs):
        face(np.asarray(v)[f], c if isinstance(c,str) else c[i%len(c)])

def box(p,s,c,axes=np.eye(3)):
    p=np.array(p); s=np.array(s)/2
    ax=np.array(axes,float)
    v=[p+(np.array(t)*s)@ax for t in [(-1,-1,-1),(1,-1,-1),(1,1,-1),(-1,1,-1),(-1,-1,1),(1,-1,1),(1,1,1),(-1,1,1)]]
    solid(v,[[0,3,2,1],[4,5,6,7],[0,1,5,4],[1,2,6,5],[2,3,7,6],[3,0,4,7]],c,'cuboid')

def basis(t):
    t=u(t)
    a=u(np.cross(t,[0,1,0] if abs(t[1])<.95 else [1,0,0]))
    return a,u(np.cross(t,a)),t

def rod(a,b,w,d,c):
    a=np.array(a); b=np.array(b)
    box((a+b)/2,[w,d,np.linalg.norm(b-a)],c,basis(b-a))

def prism(a,b,r1,r2,c,n=6,k='faceted segment'):
    a=np.array(a); b=np.array(b)
    x,y,t=basis(b-a)
    v=[p+r*(np.cos(q)*x+np.sin(q)*y) for p,r in [(a,r1),(b,r2)] for q in np.linspace(0,2*np.pi,n,endpoint=False)]
    solid(v,[list(range(n-1,-1,-1)),list(range(n,2*n))]+[[i,(i+1)%n,(i+1)%n+n,i+n] for i in range(n)],c,k)

def crystal(a,b,r):
    a=np.array(a); b=np.array(b)
    x,y,t=basis(b-a)
    n=5
    v=[a]+[a+(b-a)*f+rr*r*(np.cos(q)*x+np.sin(q)*y) for f,rr in [(.3,1),(.72,.7)] for q in np.linspace(0,2*np.pi,n,endpoint=False)]+[b]
    fs=[]
    for i in range(n):
        j=(i+1)%n
        fs.extend([[0,1+i,1+j],[1+i,6+i,6+j,1+j],[6+i,11,6+j]])
    solid(v,fs,['#ffbc43','#ffe29a','#db8022','#ffca58'],'crystal')

def curve(points,n):
    p=np.array(points,float)
    t=np.r_[0,np.cumsum(np.linalg.norm(np.diff(p,axis=0),axis=1))]
    return CubicSpline(t,p,axis=0,bc_type='natural')(np.linspace(0,t[-1],n))

def tube(points,radii,n,m,belly=None,ratio=1,initial=(1,0,0), torso=False):
    p=curve(points,n+1)
    t=np.array([u(v) for v in np.gradient(p,axis=0)])
    xx=[]; yy=[]; x=np.array(initial,float)
    for tt in t:
        if abs(u(x)@tt)>.98:
            x=np.array([0,1,0] if abs(tt[1])<.9 else [0,0,1])
        x=u(x-tt*(x@tt)); xx.append(x); yy.append(u(np.cross(tt,x)))
    x=np.array(xx); y=np.array(yy)
    r=np.interp(p[:,2],np.array(points)[:,2],radii) if torso else np.interp(np.linspace(0,1,n+1),np.linspace(0,1,len(radii)),radii)
    q=np.linspace(0,2*np.pi,m+1)
    def surf(i,a):
        v=p[i]+r[i]*(np.cos(a)*x[i]+np.sin(a)*y[i]*ratio)
        if torso: v[1]-=1.9*np.exp(-((p[i,2]-37.6)/2.8)**2)*max(0,-np.sin(a))**2
        return v
    for i in range(n):
        for j in range(m):
            a=(q[j]+q[j+1])/2
            normal=u(np.cos(a)*x[i]+np.sin(a)*y[i]/ratio)
            tt=u(p[i+1]-p[i])
            ac=u(np.cross(tt,normal))
            pal=B if belly is not None and np.cos(a-belly)>.57 else D
            c=pal[int(rng.integers(len(pal)))]
            face([surf(i,q[j]),surf(i+1,q[j]),surf(i+1,q[j+1]),surf(i,q[j+1])],pal[0])
            width=2*(r[i]+r[i+1])/2*np.sin(np.pi/m)*1.02
            length=np.linalg.norm(p[i+1]-p[i])*1.10
            box((surf(i,a)+surf(i+1,a))/2+normal*.12,[width,length,.34 if pal is B else .49],c,[ac,tt,normal])
    for i in [0,n]:
        face([surf(i,a) for a in q[:-1]],D[0])
    return p,x,y,t,r

def shell(p,r,c,n=12,m=8):
    v=[np.array(p)+np.array(r)*[np.cos(b)*np.cos(a),np.cos(b)*np.sin(a),np.sin(b)] for b in np.linspace(-np.pi/2,np.pi/2,m+1) for a in np.linspace(0,2*np.pi,n,endpoint=False)]
    solid(v,[[i*n+j,i*n+(j+1)%n,(i+1)*n+(j+1)%n,(i+1)*n+j] for i in range(m) for j in range(n)],c,'faceted shell')

def band(p,rx,ry,w,c,n=24):
    for j in range(n):
        q=2*np.pi*j/n
        box(np.array(p)+[rx*np.cos(q),ry*np.sin(q),0],[2*np.pi*max(rx,ry)/n*1.02,w,.55],c,[u([-rx*np.sin(q),ry*np.cos(q),0]),[0,0,1],u([np.cos(q)/rx,np.sin(q)/ry,0])])

def plate(p,w,h,d,c):
    p=np.array(p)
    v=np.array([[-w/2,0,h*.18],[0,0,h/2],[w/2,0,h*.18],[w*.35,0,-h*.27],[0,0,-h/2],[-w*.35,0,-h*.27]])+p
    solid(np.r_[v,v+[0,-d,0]],[[0,1,2,3,4,5],[11,10,9,8,7,6]]+[[i,(i+1)%6,(i+1)%6+6,i+6] for i in range(6)],c,'armor plate')

def charm(p,l=1.8):
    p=np.array(p)
    rod(p,p+[0,0,-l],.14,.16,G)
    box(p+[0,0,-l],[.52,.35,.7],L)
    box(p+[0,-.2,-l],[.22,.13,.35],R)

def cloth(points,w,n=18,m=9,red=True):
    p=curve(points,n+1)
    base,ink=(R,W) if red else (W,R)
    for i in range(n):
        for j in range(m):
            k=abs(j-(m-1)/2); t=i%10
            if i>=n-3 and k>(n-i)*1.5: continue
            motif=(k==0 and 2<=t<=8) or (k==1 and t in [2,5,8]) or (k==2 and t in [3,5,7]) or (k==3 and t in [4,5,6])
            c=ink if motif or j in [0,m-1] else base
            v=[]
            for a,b in [(i,j),(i+1,j),(i+1,j+1),(i,j+1)]:
                shift=(b/m-.5)*w
                v.append(p[a]+[shift,.15*np.cos(shift*1.4)+.10*np.sin(a*.7),0])
            face(v,c)
            face(np.array(v)+[0,.12,0],c)
            count['woven patch']+=1
    for j in range(1,m-1,2):
        a=p[-1]+[(j/m-.5)*w,0,.1]
        rod(a,a+[0,0,-.8],.14,.16,ink)

def draw_head(origin, side, scale):
    f=u([side*.72,-1,.045]) if side else u([0,-1,.055])
    a=u(np.cross([0,0,1],f))
    up=u(np.cross(f,a))
    assert up[2]>.95 and abs(f@up)<1e-8
    def H(x,y,z): return origin+scale*(x*a+(y*.81 if y>0 else y)*f+z*1.24*up)
    def section(sec,colors,tag):
        outline=[(-.64,1),(.64,1),(1,.65),(1,.12),(.72,0),(-.72,0),(-1,.12),(-1,.65)]
        m=len(outline)
        vv=[H(w*x,y,lo+(hi-lo)*z) for y,w,lo,hi in sec for x,z in outline]
        fs=[list(range(m-1,-1,-1)),list(range((len(sec)-1)*m,len(sec)*m))]
        fs += [[i*m+j,i*m+(j+1)%m,(i+1)*m+(j+1)%m,(i+1)*m+j] for i in range(len(sec)-1) for j in range(m)]
        solid(vv,fs,colors,tag)
    upper=[(-3.0,2.45,-1.55,3.65),(-1.5,3.35,-1.55,4.55),(0.5,3.75,-1.05,4.8),(2.25,3.35,-.50,4.3),(3.8,2.8,-.30,3.05),(5.6,2.4,-.40,2.65),(7.3,2.10,-.5,2.3),(7.85,1.70,-.42,1.78)]
    lower=[(-1.7,2.5,-2.65,-1.40),(0,2.85,-3.1,-1.75),(2.2,2.65,-3.5,-2.35),(4.3,2.25,-3.95,-2.90),(6.4,1.95,-4.22,-3.30),(7.6,1.52,-4.12,-3.38)]
    section(upper,[D[0],D[1],D[2],D[3],D[1],D[0],D[2],D[1]],'head shell')
    section(lower,[B[2],B[1],B[0],B[1],B[2],B[0],B[1],B[2]],'jaw shell')
    for s in [-1,1]:
        face([H(s*2.45,-1.4,-1.35),H(s*2.7,.7,-1.05),H(s*2.4,1.55,-2.15),H(s*2.45,-1.25,-2.2)],'#4b2425')
    for i in range(len(upper)-1):
        y,w,lo,hi=upper[i]; y2,w2,lo2,hi2=upper[i+1]
        face([H(-w*.7,y,lo-.015),H(w*.7,y,lo-.015),H(w2*.7,y2,lo2-.015),H(-w2*.7,y2,lo2-.015)],'#30191d')
    prism(H(0,.4,-1.95),H(0,6.0,-3.08),.75*scale,.35*scale,'#af482a')
    for s in [-1,1]:
        for i in range(6):
            yy=2.0+i*.86; xx=3.12-i*.23; zz=.37+.13*(i%2)
            box(H(s*xx,yy,zz),[.54*scale,1.04*scale,.89*scale],B[i%4],[a,f,up])
            box(H(s*(xx-.18),yy,2.40-i*.15),[.76*scale,.94*scale,.60*scale],D[(i+1)%4],[a,f,up])
        for k in range(3):
            p=H(s*(3.12-.15*k),-.65-k*.55,.45-k*.1)
            box(p,[.95*scale,1.30*scale,1.15*scale],B[k],[a,f,up])
        normal=u(s*.84*a+.54*f); tangent=u(np.cross(up,normal))
        eye=H(s*3.51,1.72,2.45)
        box(eye,[1.95*scale,.65*scale,1.25*scale],'#21181d',[tangent,normal,up])
        box(eye+normal*.37*scale,[1.48*scale,.22*scale,.73*scale],'#ffad20',[tangent,normal,up])
        box(eye+normal*.50*scale,[.22*scale,.09*scale,.68*scale],'#fff0a5',[tangent,normal,up])
        rod(H(s*3.28,-.25,3.85),H(s*3.22,2.95,3.12),.93*scale,.85*scale,G)
        rod(H(s*3.4,.25,1.65),H(s*2.9,3.4,.95),.56*scale,.66*scale,B[2])
        box(H(s*1.26,7.57,1.32),[.62*scale,.25*scale,.51*scale],'#1f1920',[a,f,up])
        us=np.array(upper); ls=np.array(lower)
        for i,yy in enumerate(np.linspace(1.6,7.25,8)):
            w=np.interp(yy,us[:,0],us[:,1]); lo=np.interp(yy,us[:,0],us[:,2]); xx=s*w*.78
            prism(H(xx,yy,lo+.06),H(xx*.97,yy+.09,lo-(1.05 if i in [1,5] else .64)),.27*scale,.015,W,5,'tooth')
            w=np.interp(yy,ls[:,0],ls[:,1]); hi=np.interp(yy,ls[:,0],ls[:,3]); xx=s*w*.77
            prism(H(xx,yy,hi-.04),H(xx*.97,yy-.03,hi+(.72 if i==2 else .47)),.21*scale,.015,W,5,'tooth')
        p1=H(s*2.1,-1.3,3.6); p2=H(s*3.0,-3.8,5.45); p3=H(s*4.25,-6.1,6.3)
        prism(p1,p2,1.12*scale,.74*scale,D[1],6,'horn'); prism(p2,p3,.74*scale,.045,D[2],6,'horn')
        for k in range(3):
            prism(H(s*(2.8+k*.05),-1.45-k*.52,2.25-k*.65),H(s*(4.35+k*.22),-3.8-k*.75,3.4-k*.6),.66*scale,.04,D[2],5,'spine')
        p=H(s*2.45,-1.0,3.4)
        cloth([p,p+[s*.38,-.5,-3.1*scale],p+[s*.7,-.65,-6.2*scale]],1.22*scale,12,7,False)
        charm(p+[s*.55,-.34,-5.8*scale],1.35)
    for j in range(4):
        rod(H(0,-1.6+j*.94,4.5-j*.15),H(0,-.9+j*.94,4.63-j*.24),.80*scale,.53*scale,D[2])
    crystal(H(0,.7,4.38),H(0,-.7,6.35),.85*scale)


def normalize_and_render(prefix='lamya_v5'):
    low=min(v[:,2].min() for v in F)
    for i in range(len(F)):
        F[i]=F[i]+[0,0,-low]

    def render(name,elev,azim,dpi=145):
        fig=plt.figure(figsize=(9,9),dpi=dpi,facecolor='#edf0ef')
        ax=fig.add_subplot(111,projection='3d')
        ax.set_facecolor('#edf0ef')
        ax.set_proj_type('ortho')
        idx=[i for i,v in enumerate(F) if v[:,2].min()>=39] if name=='heads' else range(len(F))
        ax.add_collection3d(Poly3DCollection([F[i] for i in idx],facecolors=[C[i] for i in idx],edgecolors='none',linewidths=0,antialiased=False,zsort='average'))
        ax.set(xlim=(-34,34),ylim=(-24,28),zlim=(0,78),xlabel='X · voxels',ylabel='Z · depth',zlabel='Y · height')
        ax.set_box_aspect((68,52,78))
        if name=='heads':
            ax.set(xlim=(-30,30),ylim=(-18,19),zlim=(40,77)); ax.set_box_aspect((60,37,37))
        ax.view_init(elev=elev,azim=azim)
        ax.set_xticks(np.arange(-30,31,10)); ax.set_yticks(np.arange(-20,21,10)); ax.set_zticks(np.arange(0,79,10))
        if name=='heads':
            ax.set_xticks(np.arange(-30,31,10)); ax.set_yticks(np.arange(-10,11,10)); ax.set_zticks(np.arange(40,78,5)); ax.set(xlim=(-31,31),ylim=(-18,19),zlim=(40,77))
        ax.tick_params(labelsize=8)
        for a in [ax.xaxis,ax.yaxis,ax.zaxis]:
            a.pane.set_facecolor('#f6f6f3')
            a._axinfo['grid']['color']=(.65,.68,.70,.62)
            a._axinfo['grid']['linewidth']=.65
        fig.subplots_adjust(left=.015,right=.985,bottom=.02,top=.94)
        fig.text(.045,.965,'LAMYA  /  V5',fontsize=16,weight='bold',color='#343138',va='top')
        fig.text(.045,.93,name.replace('_',' ').upper()+'  •  PROCEDURAL MESH',fontsize=8.5,color='#60666b',va='top')
        out=O/f'{prefix}_{name}.png'
        fig.savefig(out)
        plt.close(fig)
        print(out, flush=True)

    views=[('three_quarter',14,-68),('front',9,-90),('back',8,90),('side',8,0),('top',90,-90),('heads',13,-68)]
    for name,e,a in views:
        render(name,e,a,160 if name=='three_quarter' else 125)

    # composite
    import matplotlib.image as mpimg
    imgs=[mpimg.imread(O/f'{prefix}_{name}.png') for name,_,_ in [('front',0,0),('back',0,0),('side',0,0),('top',0,0)]]
    fig,axs=plt.subplots(2,2,figsize=(12,12),dpi=140,facecolor='#edf0ef')
    titles=['FRONT','BACK','SIDE','TOP']
    for ax,img,title in zip(axs.flat,imgs,titles):
        ax.imshow(img); ax.axis('off'); ax.set_title(title,fontsize=13,fontweight='bold')
    fig.suptitle('LAMYA V5 · MULTI-VIEW TECHNICAL PROTOTYPE',fontsize=18,fontweight='bold',y=.98)
    fig.tight_layout(rect=[0,0,1,.965])
    out=O/f'{prefix}_views.png'
    fig.savefig(out)
    plt.close(fig)
    print(out, flush=True)

# =========================
# Build the model
# =========================

# Tail / body coil.
T=tube([(0,0,22),(0,-1,14),(-8,-3,8),(-17,-2,5.8),(-25,3,5.6),(-19,12,5.8),(-4,15,5.9),(11,14,6.0),(22,9,7.2),(26,3,10.8),(27,-1,16.8),(25,-3,24),(22,-4,27.2),(18.5,-4.8,25.6)],[5.8,6.9,6.6,5.9,5.3,4.9,4.3,3.8,3.1,2.4,1.55,.74],64,22,np.pi/2,.94)
p,x,y,t,r=T
for i in range(8,61,3):
    n=u(np.array([0,0,1])-t[i]*t[i,2])
    a=p[i]+n*r[i]*.93
    prism(a,a+n*(2.1+r[i]*.30)-t[i]*1.0,r[i]*.32,.04,D[2],5,'spine')
    if i%6==2 or i>47:
        crystal(a+t[i]*.6,a+n*(2.1+r[i]*.24)+t[i]*.62,r[i]*.20)
for i in [14,26,38,49,56]:
    for j in range(16):
        q=2*np.pi*j/16
        n=np.cos(q)*x[i]+np.sin(q)*y[i]
        ac=u(np.cross(t[i],n))
        box(p[i]+n*(r[i]+.22),[r[i]*.43,.88,.44],K,[ac,t[i],n])
        if j%2==0:
            box(p[i]+n*(r[i]+.50),[r[i]*.25,.55,.26],L,[ac,t[i],n])
prism(p[-1],[17.5,-5.0,24.5],1.05,.75,G)
crystal([17.6,-5.0,24.8],[17.8,-5.4,21.0],1.0)

# Hourglass torso, with a continuous curved cuirass.
group='torso'
tube([(0,0,18.5),(0,0,23.5),(0,.1,28.5),(0,.2,31.5),(0,.35,34.5),(0,.6,37.7),(0,.85,40),(0,1.25,43)],[6.3,6.45,3.35,3.1,4.2,5.95,5.55,4.5],30,28,-np.pi/2,.77,torso=True)
zs=[33.6,35.1,37.0,38.6,39.7,40.4]; widths=[3.4,4.6,5.5,5.5,5.1,4.2]; depths=[3.0,4.6,6.2,6.0,4.8,3.9]
def cuir(i,j):
    a=np.linspace(-1,1,17)[j]; return np.array([widths[i]*a,-depths[i]*np.sqrt(max(.04,1-a*a))-.55,zs[i]+.38*abs(a)])
for i in range(len(zs)-1):
    for j in range(16):
        vv=[cuir(i,j),cuir(i+1,j),cuir(i+1,j+1),cuir(i,j+1)];face(vv,[D[1],D[2],D[0]][(i+j)%3]);face(np.array(vv)+[0,.24,0],K);count['cuirass panel']+=1
for i in [0,2,4,5]:
    for j in range(16):rod(cuir(i,j)+[0,-.13,0],cuir(i,j+1)+[0,-.13,0],.22,.28,L if i in [0,5] else G)
for side in [-1,1]:
    for z,x,y in [(32.,2.1,-2.15),(33.6,2.65,-2.6),(35.3,3.25,-3.05)]:
        plate([side*x,y,z],1.7,2.0,.42,K);plate([side*x,y-.42,z],1.3,1.65,.17,D[2])
    for k in range(4):charm([side*(2.+k*.76),-4.8+k*.48,39.8+k*.08],1.1+(k%2)*.6)
plate([0,-6.4,37.4],3.1,4.45,.40,G);plate([0,-6.77,37.5],2.15,3.1,.25,R);crystal([0,-7.1,36.2],[0,-7.35,39.0],.82)
band([0,.1,28.6],3.45,2.68,.85,K);band([0,.1,28.0],3.65,2.82,.42,L)
plate([0,-2.9,28.25],2.7,2.65,.56,G);plate([0,-3.42,28.1],1.55,1.7,.3,R)
group='skirt'
cloth([(0,-3.42,27.2),(0,-7.1,22.2),(0,-8.95,16.1),(-.5,-10.2,9.8)],5.8,25,13)
for s in [-1,1]:
    cloth([(s*3.0,-2.7,27.8),(s*5.6,-6.3,23.0),(s*7.8,-8.1,17.2),(s*9.9,-8.2,12.0)],2.6,18,9,False)
    for k in range(5):
        charm([s*(2.35+k*.62),-3.25-k*.18,26.9-k*.72],1.5+k*.22)

# Arms.
for s in [-1,1]:
    group=f"arm{s}"
    sh=np.array([s*6.4,.35,40.9]); el=np.array([s*12.4,-1.1,35.8]); wr=np.array([s*17.9,-5.0,30.7]); pa=np.array([s*19.3,-6.05,29.1])
    tube([sh,[s*9.5,0,39.2],el],[2.70,2.28,1.92],9,15,-np.pi/2,.9,(0,0,1)); shell(el,[2.05,1.96,2.08],D)
    tube([el,[s*15.4,-2.8,33.1],wr],[1.96,2.42,1.73],11,16,-np.pi/2,.94,(0,0,1)); shell(pa,[2.73,2.22,2.26],D)
    for i in range(3):
        a=sh+[s*(.55+i*1.4),0,2.2-i*.7]; axes=[[s*.93,0,-.36],[0,1,0],[s*.36,0,.93]]
        box(a,[4.0,6.2-i*.7,.95],K,axes); box(a+[0,0,.5],[3.4,5.55-i*.7,.56],D[2],axes); crystal(a+[0,0,.54],a+[s*.80,0,3.4-i*.3],1-i*.12)
    for k in range(5):
        charm([s*(5.8+k*.84),-3.0+k*.14,40.7-k*.58],2.0+(k%3)*.35)
    t2=u(wr-el); a2,b2,_=basis(t2)
    for frac,rr in [(.36,2.53),(.76,2.16)]:
        pp=el+(wr-el)*frac
        for j in range(16):
            q=j*np.pi/8; n=np.cos(q)*a2+np.sin(q)*b2; ac=u(np.cross(t2,n))
            box(pp+n*rr,[1.08,1.2,.48],W,[ac,t2,n]); box(pp+n*(rr+.29),[.50,.50,.16],R,[ac,t2,n])
            for off in [-.76,.76]: box(pp+t2*off+n*rr,[1.10,.25,.46],L,[ac,t2,n])
    for k in range(4):
        q=k/3; p0=pa+[s*(1.65-q*3.15),-1.2-q*.3,-.75]; p1=p0+[s*(1.05-q*.80),-1.45,-2.28]; p2=p1+[s*.18,-.76,-1.82]; p3=p2+[-s*.72,-.38,-1.60]
        prism(p0,p1,.65,.57,D[1]); prism(p1,p2,.57,.42,D[2]); prism(p2,p3,.42,.27,K); crystal(p3,p3+[-s*.72,.28,-2.25],.52)
    p0=pa+[-s*1.72,-.85,.1]; p1=p0+[-s*1.3,-.65,-1.25]; p2=p1+[-s*.2,-.45,-1.35]
    prism(p0,p1,.78,.53,D[1]); prism(p1,p2,.53,.33,D[2]); crystal(p2,p2+[s*.62,-.1,-1.25],.46)

# Necks, less drooped.
neck_specs=[
    ([(0,1.2,42.5),(0,1.7,47.4),(0,1.4,53.0),(0,-.3,58.0)],[4.0,3.75,3.25,2.62],1.50,0),
    ([(-4.9,1.8,42.3),(-8.5,2.6,46.4),(-12.4,3.2,50.0),(-16.0,2.8,52.0),(-17.2,1.2,53.2)],[3.08,3.02,2.72,2.38,2.02],1.22,-1),
    ([(4.9,1.8,42.3),(8.5,2.6,46.4),(12.4,3.2,50.0),(16.0,2.8,52.0),(17.2,1.2,53.2)],[3.08,3.02,2.72,2.38,2.02],1.22,1),
]
for pts,rr,scale,side in neck_specs:
    group=f"neck{side}"
    p,x,y,t,r=tube(pts,rr,20 if side==0 else 22,17,-np.pi/2,.9)
    for k in range(2,len(p)-1,2):
        n=u(np.array([0,1,.95])-t[k]*(np.array([0,1,.95])@t[k]))
        a=p[k]+n*r[k]*.94
        prism(a,a+n*2.7*scale-t[k]*.75,r[k]*.44,.05,D[2],5,'spine')
    origin=np.array(pts[-1],float)
    group=f"head{side}"
    head_start=len(F)
    draw_head(origin, side, scale)
    # Scale the complete head assembly uniformly about its neck joint.
    for j in range(head_start,len(F)):
        F[j]=origin+(F[j]-origin)*.75

# Larger halo, moved to the back near the neck.
group="halo"
halo_center=np.array([0,10.8,54.8])
radius=11.8
for j in range(40):
    q=2*np.pi*j/40
    n=np.array([np.cos(q),0,np.sin(q)])
    tangent=np.array([-np.sin(q),0,np.cos(q)])
    p0=halo_center+radius*n
    box(p0,[1.65,.95,.82],G,[tangent,[0,1,0],n])
    box(p0+[0,-.45,0],[.96,.30,.48],L,[tangent,[0,1,0],n])
    if j%2==0:
        rod(p0,halo_center+n*(radius+(3.8 if j%4==0 else 2.5)),.76,.72,L)
# crown centerpiece on the top of the ring
plate(halo_center+[0,0,12.5],4.2,4.0,.55,G)
plate(halo_center+[0,-.55,12.5],2.8,2.9,.36,R)
box(halo_center+[0,-.8,12.5],[.34,.20,2.1],W)
box(halo_center+[0,-.8,12.5],[2.1,.20,.34],W)
# supports from back/shoulders to halo
group="torso"
for s in [-1,1]:
    rod([s*3.0,4.4,50.2],[s*8.0,9.2,51.2],.72,.72,K)
    rod([s*1.8,4.2,45.2],[s*5.4,10.2,46.8],.64,.60,G)
    rod([s*0.5,4.6,42.8],[s*2.3,9.8,45.2],.56,.54,G)

# shoulder armor / back layers under halo
for z,w in [(40.5,6.0),(37.2,5.6),(34.2,4.6),(31.5,3.8)]:
    for s in [-1,1]:
        p0=[s*w*.46,3.7 if z>35 else 3.0,z]
        box(p0,[w*.84,1.08,2.5],D[2]); box([p0[0],p0[1]+.58,z+.95],[w*.84,.34,.42],K)
    crystal([0,3.9 if z>35 else 3.1,z],[0,5.8 if z>35 else 4.9,z+1.15],.68)

print(dict(count),len(F),flush=True)
if __name__=='__main__':
    normalize_and_render('lamya_v5')

# Technical motion preview. All frames use the static mesh, not generated imagery.
from scipy.spatial.transform import Rotation
from PIL import Image
from concurrent.futures import ProcessPoolExecutor

def smooth(x):
    x=np.clip(x,0,1);return x*x*(3-2*x)
def rotate(v,pivot,axis,angle):
    return (v-pivot)@Rotation.from_rotvec(np.asarray(axis)*angle).as_matrix().T+pivot

def prepare():
    global V,SPLIT,GVERT,FLOOR
    V=np.concatenate(F);SPLIT=np.cumsum([len(f) for f in F])[:-1];GVERT=np.repeat(np.array(GROUP),[len(f) for f in F]);FLOOR=min(V[:,2])
def deform(t,mode):
    q=V.copy();wave=2*np.pi*t;gait=mode=='glide';attack=np.sin(np.pi*t)**4 if mode=='cast' else 0.
    for side in [-1,0,1]:
        spec=next(e for e in neck_specs if e[3]==side);root=np.array(spec[0][0]);end=np.array(spec[0][-1]);phase=wave+side*1.8
        shift=np.array([.65*np.sin(phase),.7*np.cos(phase)-attack*(2 if side else 1.25),.4*np.sin(phase+.6)+attack*(.6 if side else 1.4)])*(1.6 if gait else 1)
        m=GVERT==f'neck{side}';v=V[m];w=smooth((v[:,2]-root[2])/(end[2]-root[2]));q[m]=v+w[:,None]*shift
        m=GVERT==f'head{side}';v=rotate(V[m],end,[0,0,1],.10*np.sin(phase)-side*.1*attack);q[m]=rotate(v,end,[1,0,0],.055*np.cos(phase)+.13*attack)+shift
    for side in [-1,1]:
        m=GVERT==f'arm{side}';v=V[m];sh=np.array([side*6.4,.35,40.9]);el=np.array([side*12.4,-1.1,35.8]);wr=np.array([side*17.9,-5.,30.7]);axis=u(wr-el)
        w=smooth(((v-wr)@axis+.7)/2);p=rotate(v,wr,[0,1,0],side*(.08*np.sin(wave+side)-.18*attack));v=v+(p-v)*w[:,None]
        w=smooth(((V[m]-el)@axis+1.2)/3.2);p=rotate(v,el,[1,0,0],.13*np.sin(wave+side*1.4)+.75*attack);v=v+(p-v)*w[:,None]
        q[m]=rotate(v,sh,[0,1,0],side*(.08*np.sin(wave+side*1.3)-.22*attack))
    m=GVERT=='skirt';v=q[m];w=smooth((28-v[:,2])/18);v[:,0]+=w*(1.4 if gait else .6)*np.sin(wave-v[:,2]*.25);v[:,1]+=w*(.45+attack*.75)*np.cos(wave-v[:,2]*.24);q[m]=v
    m=GVERT=='tail';v=q[m];w=smooth((np.linalg.norm(v[:,:2],axis=1)-4)/16);amp=1.5 if gait else .30;v[:,0]+=amp*w*np.sin(wave-v[:,0]*.10-v[:,1]*.12);v[:,1]+=amp*w*np.cos(wave-v[:,0]*.12);q[m]=v
    m=GVERT=='halo';q[m]=rotate(q[m],halo_center,[0,1,0],.13*np.sin(wave)+attack*1.0)
    m=GVERT!='tail';v=q[m];w=smooth((v[:,2]-19)/23);v[:,0]+=w*(1.1 if gait else .40)*np.sin(wave);v[:,1]+=w*(.8 if gait else .28)*np.cos(wave);v[:,2]+=w*(.55*np.sin(wave*2) if gait else .30*np.sin(wave));q[m]=v
    q[:,2]-=FLOOR;return np.split(q,SPLIT)

def frame(arg):
    mode,i,n=arg;fs=deform(i/n,mode);fig=plt.figure(figsize=(5,5),dpi=100,facecolor='#edf0ef');ax=fig.add_subplot(111,projection='3d');ax.set_proj_type('ortho');ax.set_facecolor('#edf0ef');ax.add_collection3d(Poly3DCollection(fs,facecolors=C,edgecolors='none',linewidths=0,antialiased=False,zsort='average'));ax.set(xlim=(-34,34),ylim=(-24,28),zlim=(0,78),xlabel='X',ylabel='Z',zlabel='Y');ax.set_box_aspect((68,52,78));ax.view_init(elev=13,azim=-68);ax.set_xticks(np.arange(-30,31,10));ax.set_yticks(np.arange(-20,21,10));ax.set_zticks(np.arange(0,79,10));ax.tick_params(labelsize=6)
    for a in [ax.xaxis,ax.yaxis,ax.zaxis]:a.pane.set_facecolor('#f6f6f3');a._axinfo['grid']['color']=(.65,.68,.70,.62)
    fig.subplots_adjust(left=.01,right=.98,bottom=.01,top=.91);fig.text(.045,.96,'LAMYA V5 / '+{'idle':'IDLE','glide':'SERPENTINE GLIDE','cast':'SPELL WIND-UP'}[mode],fontsize=10,weight='bold');fig.canvas.draw();im=Image.fromarray(np.asarray(fig.canvas.buffer_rgba())[:,:,:3].copy());plt.close(fig);return im

def animations():
    prepare();n=12
    with ProcessPoolExecutor(max_workers=4) as ex:
        for mode in ['idle','glide','cast']:
            imgs=list(ex.map(frame,[(mode,i,n) for i in range(n)]));imgs[0].save(O/f'lamya_v5_{mode}.gif',save_all=True,append_images=imgs[1:],duration=180 if mode!='cast' else 220,loop=0,disposal=2,optimize=False);print('GIF',mode,flush=True)
if __name__=='__main__':animations()
