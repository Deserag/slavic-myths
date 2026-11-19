# Slavic Myths — Tsar Pike V1: genuine procedural cuboids and articulated Matplotlib 3D render
# All anatomy and animations are generated from the same voxel parts.
import os, math, argparse
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.path import Path
from PIL import Image, ImageOps, ImageDraw, ImageFont
from dataclasses import dataclass
from functools import lru_cache

OUT=os.path.dirname(os.path.abspath(__file__))
rng=np.random.default_rng(9917)
VOX=2.5

P={
 'dark':'#293c30', 'back':'#405c3d', 'olive':'#667a47', 'flank':'#869263',
 'belly':'#b2ac85', 'bellylight':'#c6bea0', 'spot':'#c4bb78', 'spot2':'#a4a76c',
 'head':'#506b49','skull':'#3c5540', 'lip':'#7e8561', 'jaw':'#8d8c6c',
 'inside':'#653a39', 'inside2':'#8c5750','gum':'#b97e6e','teeth':'#f1e5c3',
 'iris':'#d5a554', 'pupil':'#101d19','glint':'#f8dba9',
 'fin':'#684d36','finlight':'#99764c','finrib':'#b28c56','bone':'#b0a075',
 'scar':'#b9b38b','nostril':'#1e2922', 'victim':'#4e99b3','victimhead':'#e0a47d'
}

@dataclass
class Cuboid:
    c: tuple
    s: tuple
    color: str

parts={n:[] for n in ['trunk0','trunk1','trunk2','tailbase','caudal','head','lowerjaw','dorsal','anal','pect_l','pect_r','pelv_l','pelv_r','teeth_upper','teeth_lower','decorhead','decorbody','target']}

def add(p,x,y,z,sx,sy,sz,color):
    parts[p].append(Cuboid((float(x),float(y),float(z)),(float(sx),float(sy),float(sz)),P.get(color,color)))

def rr(x, xp, fp): return float(np.interp(x,xp,fp))

def shape_color(x,y,z,rz):
    v=z/max(rz,1)
    if v>0.65: return 'dark' if (int(x*3+y*11)%6)<3 else 'back'
    if v>0.2: return 'back' if (int(x*7+y*2)%9)<4 else 'olive'
    if v> -0.45: return 'olive' if (int(x*3+y*5)%7)<4 else 'flank'
    return 'belly' if int(x+3*y)%5 else 'bellylight'

# Body: ring-by-ring voxel cross sections, pronounced shoulders and narrow tail peduncle
xs=[-15,-8,2,12,22,33,44,52,61]
ys=[10.5,12.6,16.6,17.6,16.1,13.7,9.7,6.0,3.5]
zs=[8.7,10.5,12.9,13.2,12.3,10.8,7.7,5.1,3.3]
for x in np.arange(-12.5,62,VOX):
    ry=rr(x,xs,ys); rz=rr(x,xs,zs)
    seg='trunk0' if x<15 else 'trunk1' if x<38 else 'trunk2' if x<52 else 'tailbase'
    for y in np.arange(-17.5,18,VOX):
        for z in np.arange(-15,16,VOX):
            if (y/ry)**2+(z/rz)**2<=1.0:
                add(seg,x,y,z,VOX,VOX,VOX,shape_color(x,y,z,rz))

# Head: broad skull narrowing to a flattened duck-bill snout; z<0 front reserved for mouth cavity
hx=[-62,-58,-51,-44,-36,-29,-21,-14,-9]
hy=[5.0,6.7,8.0,8.0,9.0,11.6,12.8,12.4,11.0]
hz=[2.2,3.2,3.9,4.2,5.1,7.3,9.4,10.0,9.2]
for x in np.arange(-61.25,-8,VOX):
    ry=rr(x,hx,hy); rz=rr(x,hx,hz)
    for y in np.arange(-13.75,14.0,VOX):
        for z in np.arange(-11.25,12.0,VOX):
            if (y/ry)**2+((z-0.5)/rz)**2>1.0: continue
            if x<-24 and z<-1.2: continue
            color='skull' if z>4 else 'head' if z>-0.5 else 'inside'
            if x<-38 and z>1.2: color='dark' if abs(y)<ry*0.7 else 'head'
            add('head',x,y,z,VOX,VOX,VOX,color)

# Substantial articulated lower jaw, hollow between the two separate jaws
jx=[-61,-53,-43,-33,-24,-14]
jy=[4.9,6.6,7.8,9.0,10.2,10.7]
for x in np.arange(-60,-13.5,VOX):
    ry=rr(x,jx,jy)
    for y in np.arange(-11.25,12,VOX):
        frac=abs(y)/ry
        if frac>1: continue
        for z in np.arange(-10.0,-1.0,VOX):
            mid=-6.6 if x<-36 else -6.3
            lower=mid-2.5*math.sqrt(max(0,1-frac*frac))
            upper=mid+2.7*math.sqrt(max(0,1-frac*frac))
            if lower<=z<=upper:
                color='jaw' if z<mid else 'inside2' if frac<0.66 else 'lip'
                add('lowerjaw',x,y,z,VOX,VOX,VOX,color)

# Teeth are actual 3D stepped fang structures, on upper palatal roof and lower jaw
for i,x in enumerate(np.arange(-57,-17,4.6)):
    width=rr(x,hx,hy)*0.82
    for side in (-1,1):
        y=side*width
        depth=4.3 if i%3==0 else 3.0
        # upper fangs with tapered tips pointing downward
        add('teeth_upper',x,y,-2.6,2.0,1.7,2.7,'teeth')
        add('teeth_upper',x-0.25,y,-2.6-depth/2,1.0,1.0,depth/1.6,'teeth')
        # lower fangs point upward, attached to lower jaw
        add('teeth_lower',x+1.1,y*.91,-3.8,2.2,2.0,2.6,'teeth')
        add('teeth_lower',x+1.1,y*.91,-2.5,1.0,1.1,2.1,'teeth')
    # auxiliary rows of smaller palatal teeth for retention
    for sy in (-0.34,0.34):
        add('teeth_upper',x+1.5,width*sy,-2.6,1.1,1.2,2.0,'teeth')

# Skull detail: independently modelled eyes, amber iris, pupils and gill creases
for side in (-1,1):
    y=side*12.5
    add('decorhead',-27.0,y,5.9,7.8,2.5,7.5,'skull')
    add('decorhead',-27.5,side*14.2,6.1,5.9,1.2,5.9,'iris')
    add('decorhead',-28.0,side*15.1,6.1,3.2,1.25,4.2,'pupil')
    add('decorhead',-28.9,side*15.9,7.5,0.9,0.7,1.2,'glint')
    for x in (-15.8,-12.2):
        add('decorhead',x,side*12.6,-2.2,1.7,1.7,10.6,'bone')
    add('decorhead',-54.0,side*4.9,3.7,2.2,1.6,1.5,'nostril')
    add('decorhead',-41.5,side*7.8,3.8,4.0,1.3,1.5,'scar')

# Scaly relief spots are separate raised small cuboids on curved surface, including the back
for x in np.arange(-7,52,3.7):
    ry=rr(x,xs,ys); rz=rr(x,xs,zs)
    for angle in np.arange(0,2*math.pi,0.48):
        if rng.random()>.48: continue
        jitter=rng.uniform(-0.95,0.95)
        a=angle+rng.uniform(-.12,.12)
        y=(ry+0.55)*math.cos(a); z=(rz+.55)*math.sin(a)
        if abs(z)<2 and rng.random()<.3: continue
        col='spot' if rng.random()>.28 else 'spot2'
        add('decorbody',x+jitter,y,z,1.9,1.6,1.45,col)
for x in np.arange(-4,47,6.2):
    rz=rr(x,xs,zs)
    add('decorbody',x,0,rz+1.2,3.2,4.1,2.3,'bone')
# Three old wounds on flank
for x,z in [(12,5.5),(15,3.2),(17,0.8)]:
    ry=rr(x,xs,ys); yz=ry*math.sqrt(max(.1,1-(z/rr(x,xs,zs))**2))
    add('decorbody',x,-yz-1.0,z,4.4,1.5,1.5,'scar')

# Polygon-extruded fleshy fins, not infinitely thin planes

def fill_xz(name,poly,yhalf,step=2.0):
    polygon=Path(np.array(poly,float))
    xmin,xmax=min(p[0] for p in poly),max(p[0] for p in poly)
    zmin,zmax=min(p[1] for p in poly),max(p[1] for p in poly)
    for x in np.arange(xmin+.9,xmax,step):
        for z in np.arange(zmin+.7,zmax,step):
            if not polygon.contains_point((x,z)): continue
            for y in np.arange(-yhalf+.9,yhalf,step):
                rib=(round(x/step)%4)==0
                col='finrib' if rib else 'finlight' if int(x+z)%4==0 else 'fin'
                add(name,x,y,z,step,step,step,col)

fill_xz('dorsal',[(31,10),(35,19),(40,28),(44,27),(46,19),(53,12),(57,7),(45,9)],3.1)
fill_xz('anal',[(41,-8),(45,-16),(47,-26),(51,-25),(52,-20),(59,-8)],3.1)
fill_xz('caudal',[(58,-3),(61,-8),(67,-16),(71,-22),(73,-23),(72,-13),(67,-5),(65,0),(67,5),(72,13),(73,23),(71,22),(67,16),(61,8),(58,3)],3.0)

# Paired pectoral and pelvic swept 3D fins, wing-shaped with multi-voxel thickness
for side in (-1,1):
    for name, poly, zstart, slope in [
        ('pect', [(-10,10),(-2,19),(12,30),(22,25),(6,12)], -4.5, -.30),
        ('pelv', [(21,9),(30,16),(43,25),(47,23),(35,10)], -8.0, -.23)
    ]:
        nm=name+('_l' if side<0 else '_r')
        path=Path(poly)
        for x in np.arange(min(p[0] for p in poly),max(p[0] for p in poly),2.1):
            for ay in np.arange(9,31,2.1):
                if not path.contains_point((x,ay)): continue
                zz=zstart + slope*(ay-10)-0.08*(x+6)
                for k in (-0.8,1.2):
                    col='finrib' if round(x/2.1)%4==0 else 'finlight' if round(ay)%5==0 else 'fin'
                    add(nm,x,side*ay,zz+k,2.1,2.1,2.1,col)

# Voxel target dummy to prove victim is between teeth and protrudes outside mouth
for z in np.arange(-30,-11,3.0):
    add('target',-53,0,z,6,7,3,'victim')
add('target',-53,0,-9,9,8,9,'victimhead')
for y in [-6,6]:add('target',-53,y,-22,3,3,14,'victim')

# Build immutable, surface-only cuboid meshes, culling faces for common uniform voxel steps
# for off-grid decals & teeth, retain all six faces; transparency isn't used.

quad_corners=np.array([
    [[1,-1,-1],[1,1,-1],[1,1,1],[1,-1,1]],
    [[-1,1,-1],[-1,-1,-1],[-1,-1,1],[-1,1,1]],
    [[-1,1,-1],[1,1,-1],[1,1,1],[-1,1,1]],
    [[1,-1,-1],[-1,-1,-1],[-1,-1,1],[1,-1,1]],
    [[-1,-1,1],[1,-1,1],[1,1,1],[-1,1,1]],
    [[-1,1,-1],[1,1,-1],[1,-1,-1],[-1,-1,-1]]],dtype=np.float32)/2
normals=[(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]

@lru_cache(None)
def build_mesh(name):
    blocks=parts[name]
    # face-cull aligned volumes only, and only when their next neighbour touches directly
    lookup={tuple(round(v,3) for v in b.c): b for b in blocks}
    faces=[]; colors=[]
    for b in blocks:
        c=np.array(b.c,dtype=float); s=np.array(b.s,dtype=float)
        for face_idx,(dx,dy,dz) in enumerate(normals):
            neighbor=tuple(round(c[i]+v*s[i],3) for i,v in enumerate((dx,dy,dz)))
            other=lookup.get(neighbor)
            if other is not None and all(abs(other.s[i]-s[i])<.01 for i in range(3)):
                continue
            f=c+quad_corners[face_idx]*s
            faces.append(f)
            colors.append(b.color)
    return np.asarray(faces,dtype=np.float32),np.asarray(colors,dtype=object)

# Transform composed of rotations about separately articulated pivots.
def rot_y(pts,ang,pivot):
    if not ang:return pts
    v=pts-np.array(pivot); c,s=np.cos(ang),np.sin(ang)
    out=v.copy();out[...,0]=c*v[...,0]+s*v[...,2];out[...,2]=-s*v[...,0]+c*v[...,2]
    return out+np.array(pivot)

def rot_z(pts,ang,pivot):
    if not ang:return pts
    v=pts-np.array(pivot);c,s=np.cos(ang),np.sin(ang)
    out=v.copy();out[...,0]=c*v[...,0]-s*v[...,1];out[...,1]=s*v[...,0]+c*v[...,1]
    return out+np.array(pivot)

ALL_PARTS=['trunk0','trunk1','trunk2','tailbase','caudal','head','lowerjaw','dorsal','anal','pect_l','pect_r','pelv_l','pelv_r','teeth_upper','teeth_lower','decorhead','decorbody']

def motion(name,t):
    w=2*math.pi*t
    s={'jaw':0.02,'headpitch':0.,'lunge':0.,'wave':0.06*math.sin(w),'tail':0.11*math.sin(w-.55),'fin':0.08*math.sin(w+1),'turn':0.,'prey':False,'prey_shift':0.}
    if name=='idle':
        s['wave']=.025*math.sin(w);s['tail']=.07*math.sin(w);s['jaw']=.015+.02*max(0,math.sin(w))
    elif name=='swim':
        s['wave']=.10*math.sin(2*w);s['tail']=.23*math.sin(2*w-.8);s['fin']=.13*math.sin(2*w+.4)
    elif name=='fast_swim':
        s['wave']=.22*math.sin(3*w);s['tail']=.47*math.sin(3*w-1);s['fin']=.15*math.sin(3*w)
        s['lunge']=-5*t
    elif name=='bite':
        s['jaw']=.63*math.sin(math.pi*min(1,max(0,(t-.10)/.68)))**1.2
        s['lunge']=-13*(.5-.5*math.cos(2*math.pi*min(1,max(0,(t-.22)/.55))))
        s['headpitch']=.05*math.sin(2*w)
    elif name=='grab':
        s['jaw']=.70*min(1,max(0,t/.45))
        s['lunge']=-13*min(1,max(0,(t-.3)/.55))
        s['prey']=True;s['prey_shift']=(-18 + 15*min(1,max(0,(t-.35)/.55)))
    elif name=='shake':
        s['jaw']=.47;s['prey']=True;s['prey_shift']=-3
        s['turn']=.22*math.sin(4*w);s['headpitch']=.13*math.sin(4*w+.3)
    elif name=='release':
        s['jaw']=.5*(1-t);s['prey']=t<.8;s['prey_shift']=-4-24*t
    elif name=='tail_sweep':
        s['wave']=.12*math.sin(w)
        s['tail']=1.17*math.sin(math.pi*min(1,max(0,(t-.22)/.7)))
        s['fin']=.08*math.sin(w);s['turn']=-.12*math.sin(math.pi*t)
    return s

# Render movement via genuine changing coordinates of the same geometric faces

def transform_mesh(name,vertices,s):
    v=vertices
    # Jaw/teeth attach to jaw pivot. Important: teeth rotate WITH the jaw.
    if name in ('lowerjaw','teeth_lower'):
        v=rot_y(v,-s['jaw'],(-14,0,-1.5))
    if name in ('head','decorhead','teeth_upper','lowerjaw','teeth_lower'):
        v=rot_y(v,s['headpitch'],(-9,0,0))
        v=rot_z(v,s['turn'],(-12,0,0))
    elif name in ('trunk1','trunk2','tailbase','caudal','dorsal','anal','decorbody','pelv_l','pelv_r'):
        amount=1.0 if name in ('trunk1','dorsal','decorbody') else 1.6 if name in ('trunk2','anal','pelv_l','pelv_r') else 2.0
        v=rot_z(v,s['wave']*amount,(14,0,0))
        if name in ('tailbase','caudal'):
            v=rot_z(v,s['tail'],(50,0,0))
        if name=='caudal':v=rot_z(v,s['tail']*.44,(61,0,0))
    if name in ('pect_l','pect_r','pelv_l','pelv_r'):
        v=rot_y(v,s['fin']*(-1 if name.endswith('l') else 1),(-7,0,-5))
    v=v.copy();v[...,0]+=s['lunge']
    return v

AXES_LABELS=('X · length (px)','Y · width (px)','Z · height (px)')
def setup_ax(ax,view='side',detail=False):
    ax.set_facecolor('#f8faf6')
    ax.figure.patch.set_facecolor('#f8faf6')
    ax.xaxis.pane.set_facecolor((.90,.93,.90,1))
    ax.yaxis.pane.set_facecolor((.92,.94,.92,1))
    ax.zaxis.pane.set_facecolor((.91,.93,.93,1))
    ax.grid(True,linewidth=.5,alpha=.5)
    if detail: ax.set(xlim=(-69,-10),ylim=(-21,21),zlim=(-38,22))
    else: ax.set(xlim=(-77,84),ylim=(-36,36),zlim=(-31,34))
    ax.set_xlabel(AXES_LABELS[0],fontsize=8,labelpad=3)
    ax.set_ylabel(AXES_LABELS[1],fontsize=8,labelpad=3)
    ax.set_zlabel(AXES_LABELS[2],fontsize=8,labelpad=3)
    ax.tick_params(labelsize=7,pad=0)
    ax.set_box_aspect((2.7,1.2,1.2) if not detail else (1.3,1.0,1.1),zoom=.89)
    cams={'side':(10,-90),'front':(7,180),'top':(90,-90),'threequarter':(22,-127),'mouth':(10,-139),'tail':(26,45)}
    elev,azi=cams.get(view,cams['side']);ax.view_init(elev=elev,azim=azi)
    if view=='front':ax.set_box_aspect((1.5,1.25,1.2),zoom=.97)

def render(ax,pose=None,phase=0,view='side',detail=False,target=False):
    if pose is None:
        s=motion('idle',0)
    elif isinstance(pose,dict):s=pose
    else:s=motion(pose,phase)
    setup_ax(ax,view,detail)
    face_parts=[];color_parts=[]
    for name in ALL_PARTS:
        f,colors=build_mesh(name)
        if not len(f):continue
        face_parts.append(transform_mesh(name,f,s))
        color_parts.append(colors)
    if target or s['prey']:
        fs,cc=build_mesh('target')
        f=fs.copy(); f[...,0]+=s['prey_shift']+s['lunge']
        if pose=='shake':f=rot_z(f,.23*math.sin(8*math.pi*phase),(-56+s['lunge'],0,-17))
        face_parts.append(f);color_parts.append(cc)
    # One mesh, one global depth-sort: prevents lower jaw rendering through roof in top view.
    ax.add_collection3d(Poly3DCollection(np.concatenate(face_parts),
        facecolors=np.concatenate(color_parts),edgecolors='#263126',linewidths=.075,
        alpha=1,zsort='average',rasterized=True))

STATIC=[
 ('01_side','side',{'jaw':.025,'headpitch':0,'lunge':0,'wave':0,'tail':0,'fin':0,'turn':0,'prey':False,'prey_shift':0},False),
 ('02_front_closed','front',{'jaw':0,'headpitch':0,'lunge':0,'wave':0,'tail':0,'fin':0,'turn':0,'prey':False,'prey_shift':0},False),
 ('03_front_open','front',{'jaw':.69,'headpitch':0,'lunge':0,'wave':0,'tail':0,'fin':0,'turn':0,'prey':False,'prey_shift':0},False),
 ('04_top','top',{'jaw':.01,'headpitch':0,'lunge':0,'wave':0,'tail':0,'fin':0,'turn':0,'prey':False,'prey_shift':0},False),
 ('05_threequarter','threequarter',{'jaw':.15,'headpitch':0,'lunge':0,'wave':.03,'tail':.08,'fin':0,'turn':0,'prey':False,'prey_shift':0},False),
 ('06_mouth_teeth','mouth',{'jaw':.72,'headpitch':0,'lunge':0,'wave':0,'tail':0,'fin':0,'turn':0,'prey':False,'prey_shift':0},True)
]

def do_static():
    from PIL import Image
    for i,(nm,view,s,detail) in enumerate(STATIC):
        fig=plt.figure(figsize=(12,7.0),dpi=112)
        ax=fig.add_subplot(111,projection='3d',computed_zorder=False)
        render(ax,s,view=view,detail=detail)
        ax.set_title('TSAR PIKE V1   |   '+nm[3:].replace('_',' ').upper()+'   |   procedural voxel cuboids',fontsize=12,weight='bold',pad=16)
        outfile=os.path.join(OUT,nm+'.png');fig.savefig(outfile,dpi=112,facecolor=fig.get_facecolor(),bbox_inches='tight',pad_inches=.15)
        plt.close(fig)
        print('STATIC',i+1,nm,os.path.getsize(outfile),flush=True)

def do_animation(selected=None):
    names=[('idle','07_idle'),('swim','08_swim'),('fast_swim','09_fast_swim'),('bite','10_bite'),('grab','11_grab'),('shake','12_shake'),('release','13_release'),('tail_sweep','14_tail_sweep')]
    for i,(mode,nm) in enumerate(names):
        if selected is not None and mode!=selected:continue
        frames=[]
        N=10 if mode not in ('bite','grab','tail_sweep') else 12
        for j,t in enumerate(np.linspace(0,1,N,endpoint=False)):
            fig=plt.figure(figsize=(8.4,5.3),dpi=70)
            ax=fig.add_subplot(111,projection='3d',computed_zorder=False)
            view='threequarter' if mode in ('shake','grab','bite','release') else 'side'
            if mode=='tail_sweep':view='top'
            render(ax,mode,t,view=view)
            ax.set_title(mode.replace('_',' ').upper()+f'  |  frame {j+1:02d}/{N}',fontsize=12,weight='bold',pad=11)
            fig.canvas.draw();w,h=fig.canvas.get_width_height()
            frames.append(Image.frombytes('RGB',(w,h),fig.canvas.tostring_rgb()) if hasattr(fig.canvas,'tostring_rgb') else Image.frombuffer('RGBA',(w,h),fig.canvas.buffer_rgba()).convert('RGB'))
            plt.close(fig)
        out=os.path.join(OUT,nm+'.gif')
        frames[0].save(out,save_all=True,append_images=frames[1:],duration=115,loop=0,optimize=True)
        print('ANIM',i+1,nm,os.path.getsize(out),flush=True)

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--only',choices=['static','gif','all','tail'],default='all');args=parser.parse_args()
    print('parts', {k:len(v) for k,v in parts.items()},flush=True)
    if args.only in ('static','all'):do_static()
    if args.only in ('gif','all'):do_animation()
    if args.only=='tail':do_animation('tail_sweep')