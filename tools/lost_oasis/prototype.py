import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.colors import to_rgb
from pathlib import Path
OUT=Path(__file__).parent
P={'sand':'#c7a572','stone':'#d9bd89','light':'#e7d1a4','old':'#b39469','dark':'#785b4a','brick':'#bd9b6f','gold':'#d6a23f','red':'#783b4c','purple':'#58364f','teal':'#4d9391','water':'#438f9e','water2':'#55a5ad','soil':'#786c46','leaf':'#57783b','leaf2':'#7c944b','wood':'#775335','wood2':'#a67b4f','fire':'#ffb42e','flame':'#ffe48c','lantern':'#ffce63','iron':'#4d4541','cloth':'#a65a6c','book':'#47666b','white':'#ded3bd'}
V={}; lights=[]
def b(x,z,y,m): V[int(x),int(z),int(y)]=m
def fill(x0,x1,z0,z1,y0,y1,m):
 for x in range(x0,x1+1):
  for z in range(z0,z1+1):
   for y in range(y0,y1+1): b(x,z,y,m)
def clear(x0,x1,z0,z1,y0,y1):
 for x in range(x0,x1+1):
  for z in range(z0,z1+1):
   for y in range(y0,y1+1): V.pop((x,z,y),None)
def column(x,z,y,h):
 fill(x-1,x+1,z-1,z+1,y,y,'dark');fill(x,x,z,z,y+1,y+h-2,'light');fill(x-1,x+1,z-1,z+1,y+h-1,y+h-1,'gold')
def lamp(x,z,y):
 b(x,z,y,'lantern');lights.append((x,z,y))
def brazier(x,z,y):
 b(x,z,y,'dark');b(x,z,y+1,'stone');b(x,z,y+2,'iron');b(x,z,y+3,'fire');b(x,z,y+4,'flame');lights.append((x,z,y+3))
def palm(x,z,y,h=8):
 for i in range(h): b(x+(i>=h-2),z,y+i,'wood')
 cx=x+1; cy=y+h
 for dx,dz in [(1,0),(-1,0),(0,1),(0,-1),(1,1),(-1,1),(1,-1),(-1,-1)]:
  for i in range(1,5 if dx*dz==0 else 4):
   yy=cy+(1 if i==1 else 0 if i<=2 else -1 if i==3 else -2);b(cx+dx*i,z+dz*i,yy,'leaf2' if i==1 else 'leaf')
   if i<=2 and dx*dz==0: b(cx+dx*i+dz,z+dz*i+dx,yy,'leaf')
 b(cx,z,cy,'leaf2')
def r(x,z):return np.hypot(x,z/1.08)
def arc(x,z):return np.arctan2(z/1.08,x)*180/np.pi
for x in range(-53,54):
 for z in range(-59,60):
  rr=r(x,z); a=arc(x,z)
  if rr>52: continue
  b(x,z,-2,'sand');b(x,z,-1,'old')
  if 20<=rr<30:b(x,z,0,'water2' if (x*17+z*13)%13==0 else 'water')
  else:b(x,z,0,'stone');b(x,z,1,'light' if (x+z)%8==0 else 'stone')
  if rr<20:
   if 18<=rr<19 or 14<=rr<15:b(x,z,1,'red')
   if 19<=rr<20:b(x,z,1,'gold')
   if rr<5 or (rr<12 and (abs(x)<=1 or abs(z)<=1 or abs(abs(x)-abs(z))<=1)):b(x,z,1,'gold' if int(rr)%3!=0 else 'red')
  if 30<=rr<35:
   if rr<31 or rr>=34:b(x,z,1,'teal')
   elif abs((a+180)%30-15)<2:b(x,z,1,'red')
  if z>=31 and abs(x)<=23:continue
  axial=(abs(x)<=5 and abs(z)>32) or (abs(z)<=2 and abs(x)>34)
  if 35<=rr<49:
   yseat=3+int(rr-35) if rr<42 else (10 if rr<44 else 10+int(rr-43))
   if not axial:
    b(x,z,yseat,'light' if int(rr)%2==0 else 'stone');b(x,z,yseat-1,'brick')
    if 42<=rr<44:b(x,z,yseat,'teal')
    if rr<38:
     for y in range(2,yseat-1):b(x,z,y,'old')
   d=abs((a+180+7.5)%15-7.5)*np.pi/180*39
   if 38<=rr<40 and not axial:
    for y in range(2,7):
     if not (d<=(2.2 if y<=4 else 1.35 if y==5 else .45)):b(x,z,y,'brick' if y<6 else 'light')
   if 47<=rr<49 and not axial:
    d2=abs((a+180+6)%12-6)*np.pi/180*48
    for y in range(2,15):
     if not (y<=6 and d2<=(2.1 if y<=4 else 1.1 if y==5 else .3)):b(x,z,y,'old' if y%5==0 else 'stone')
  if 49<=rr<51 and not axial:
   d=abs((a+180+6)%12-6)*np.pi/180*50
   for y in range(2,23):
    hole=(2<=y<=7 and d<(2.2 if y<=5 else 1.25 if y==6 else .4)) or (16<=y<=21 and d<(2.5 if y<=19 else 1.45 if y==20 else .5))
    if not hole:b(x,z,y,'gold' if y in [15,22] else 'dark' if y in [2,14] else 'brick' if y%4==0 else 'stone')
   if int(a+180)%12<2:b(x,z,23,'light')
for x in range(-32,33):
 for z in range(-35,36):
  if 19<=r(x,z)<=31 and (abs(x)<=5 or abs(z)<=2):b(x,z,0,'old');b(x,z,1,'stone' if abs(x)!=5 and abs(z)!=2 else 'teal')
for i,ang in enumerate([24,66,114,156,204,246,294,336]):
 t=np.deg2rad(ang);cx=round(25*np.cos(t));cz=round(27*np.sin(t))
 for x in range(cx-4,cx+5):
  for z in range(cz-4,cz+5):
   d=np.hypot(x-cx,z-cz)
   if d<=3.6+0.3*np.sin(x+z):
    fill(x,x,z,z,-1,1,'old');b(x,z,2,'soil' if d<2.7 else 'sand')
    if d>2.8 and (x*11+z*5)%5==0:b(x,z,3,'stone')
 palm(cx,cz,3,7+i%3)
 for dx,dz in [(2,1),(-2,-1),(1,-2)]:b(cx+dx,cz+dz,3,'leaf2')
for a in [40,140,220,320]:
 t=np.deg2rad(a)
 for rad in [21,24,28]:
  x=round(rad*np.cos(t));z=round(rad*1.08*np.sin(t));fill(x,x+1,z,z+1,-1,1,'stone')
for a in range(15,360,30):
 t=np.deg2rad(a);brazier(round(17.7*np.cos(t)),round(19.1*np.sin(t)),2)
for a in range(0,360,10):
 t=np.deg2rad(a)
 for rad in [40.5,46]:
  x=round(rad*np.cos(t));z=round(rad*1.08*np.sin(t))
  if (z>=30 and abs(x)<=24) or abs(x)<=5 or abs(z)<=2:continue
  b(x,z,2,'stone');b(x,z,3,'dark');lamp(x,z,4)
for a in range(0,360,15):
 t=np.deg2rad(a);x=round(49*np.cos(t));z=round(53*np.sin(t))
 if (z>=30 and abs(x)<=24) or abs(x)<=5 or abs(z)<=2:continue
 fill(x-1,x+1,z-1,z+1,14,15,'dark');fill(x,x,z,z,16,23,'light');fill(x-1,x+1,z-1,z+1,24,24,'gold');brazier(x,z,25)
 for y in range(8,13):b(round(51*np.cos(t)),round(55*np.sin(t)),y,'red' if y!=10 else 'gold')
for xx in [-8,8]:
 fill(xx-2,xx+2,-56,-45,0,15,'stone');fill(xx-2,xx+2,-56,-45,15,16,'gold')
 for z in [-56,-45]:column(xx,z,2,16)
 fill(xx-3,xx+3,-55,-46,17,18,'light');brazier(xx,-50,19)
for x in range(-7,8):
 h=12+int((7-abs(x))/2);fill(x,x,-54,-47,h,h+2,'light');b(x,-54,h+3,'gold')
# palace
for x in range(-20,21):
 for z in range(33,60):
  fill(x,x,z,z,0,1,'dark')
  for y in [2,13,25]:b(x,z,y,'light' if (x+z)%6==0 else 'stone')
  b(x,z,37,'brick')
  if abs(x)==20 or z in [33,59]:
   for y in range(3,37):
    local=y-3 if y<13 else y-14 if y<25 else y-26
    gate=z==33 and ((abs(x)<=5 and 3<=y<=10) or (abs(x)<=10 and 26<=y<=36))
    window=(abs(x)%9 in [3,4,5] if z in [33,59] else z%9 in [3,4,5]) and 2<=local<=6
    if not (gate or window):b(x,z,y,'gold' if y in [12,24,36] else 'dark' if y in [3,14,26] else 'stone')
  d=min(20-abs(x),z-33,59-z)
  for y in range(38,39+min(5,max(0,d//2))):b(x,z,y,'red' if y>38 else 'gold')
for ylo,yhi in [(3,12),(14,24)]:
 for x in [-8,8]:
  for z in range(36,59):
   for y in range(ylo,yhi+1):
    if not (43<=z<=46 and y<=ylo+4):b(x,z,y,'brick')
for base,nxt in [(2,13),(13,25)]:
 for i in range(nxt-base):
  z=39+i
  xs=[-17,-16,-15,15,16,17] if base==2 else [-13,-12,-11,11,12,13]
  for x in xs:
   if base+i+4>=nxt:clear(x,x,z,z,nxt,nxt+3)
   for y in range(base+1,base+i+2):b(x,z,y,'wood2' if y==base+i+1 else 'brick')
   clear(x,x,z,z,base+i+2,base+i+4)
for xx in [-19,-12,12,19]:
 for y in range(3,37):b(xx,32,y,'gold' if y in [12,24,36] else 'light')
 for y in [2,13,25]:fill(xx-1,xx+1,31,33,y,y,'gold')
fill(-11,11,23,34,24,25,'stone');fill(-11,11,23,23,25,25,'gold')
for x in [-11,11]:fill(x,x,23,32,26,26,'gold')
for x in range(-11,12):
 if abs(x)>5:b(x,23,26,'gold')
for x in [-10,10]:column(x,24,26,11)
fill(-11,11,23,33,37,37,'gold');fill(-11,11,23,33,38,38,'red')
for x in [-8,8]:fill(x-1,x+1,34,34,27,35,'red')
for x in range(-5,6):
 for z in range(40,54):b(x,z,25,'red' if abs(x)<5 else 'gold')
fill(-5,5,49,54,26,26,'dark');fill(-4,4,50,53,27,27,'gold');fill(-4,4,51,53,28,28,'purple');fill(-4,4,53,54,29,34,'red');fill(-4,4,54,54,35,35,'gold')
for x in [-5,5]:fill(x,x,51,54,28,30,'gold')
for x in [-9,9]:brazier(x,39,26)
for cx in [-22,22]:
 for x in range(cx-4,cx+5):
  for z in range(43,54):
   edge=abs(x-cx)==4 or z in [43,53]
   for y in range(2,39):
    if edge and not (y%12 in [5,6,7,8] and ((x==cx and z in [43,53]) or (z==48 and abs(x-cx)==4))):b(x,z,y,'gold' if y%12==0 else 'stone')
    elif y in [2,13,25,37]:b(x,z,y,'brick')
 for j in range(5):fill(cx-4+j,cx+4-j,44+j,52-j,39+j,39+j,'red' if j%2 else 'gold')
 b(cx,48,44,'gold')
for cx in [-26,26]:
 for x in range(cx-2,cx+3):
  for z in range(27,36):fill(x,x,z,z,-1,0,'old');b(x,z,1,'water')
 for y in range(2,17):
  zz=35-(16-y)//3
  for x in range(cx-1,cx+2):b(x,zz,y,'water2');b(x,zz+1,y,'old')
def chest(x,z,y):fill(x,x+1,z,z,y,y,'wood2');b(x,z-1,y,'gold')
def shelf(x,z,y):
 fill(x,x,z,z,y,y+3,'wood');b(x,z,y+1,'book');b(x,z,y+2,'red')
for xx in [-13,13]:
 for zz in [39,48,55]:chest(xx,zz,3)
 for zz in range(49,57,2):shelf(xx+(-3 if xx<0 else 3),zz,3)
for x in [-6,6]:
 for z in [37,48,55]:column(x,z,3,8);lamp(x+(1 if x<0 else -1),z,5)
for x in range(-3,4):
 for z in range(34,55):b(x,z,2,'red' if abs(x)<3 else 'gold')
fill(-14,-10,51,56,14,14,'wood');fill(-14,-10,52,55,15,15,'cloth');fill(-14,-10,56,56,15,15,'white');fill(-14,-10,57,57,14,17,'wood2')
for x,z in [(-12,38),(-12,47),(-10,57),(10,39),(12,56)]:chest(x,z,14)
for z in range(37,57,2):shelf(18,z,14)
fill(-2,2,43,50,14,14,'wood');fill(-3,3,42,51,15,15,'wood2')
for z in [43,46,49]:
 for x in [-5,5]:b(x,z,14,'wood');b(x,z,15,'red')
for x,z in [(-6,38),(6,38),(-6,54),(6,54),(-12,45),(12,45)]:lamp(x,z,17);b(x,z,18,'iron')
for x in [-16,16]:
 for z in [37,56]:brazier(x,z,26)
# Improve access to lower ring by adding arched cross passages through seat support.
for a in range(15,360,30):
 t=np.deg2rad(a)
 if 50<a<130:continue
 for x in range(-49,50):
  for z in range(-53,54):
   rr=r(x,z);d=abs((arc(x,z)-a+180)%360-180)*np.pi/180*rr
   if 34<=rr<40 and d<1.7:
    for y in range(2,5):V.pop((x,z,y),None)
# Final prototype detailing and light audit (six-neighbour block-light approximation, no sky light).
# Wall supports, balcony decoration, planted niches and furnished interiors.
for x in [-10,10]:
 for y in range(3,24):b(x,24,y,'light' if y%6 else 'gold')
 fill(x-1,x+1,23,25,2,2,'dark')
for x in range(-12,13):
 if abs(x)>5:b(x,23,27,'gold')
for x in [-18,-14,14,18]:
 for y in range(15,23):b(x,31,y,'red' if y not in [15,22] else 'gold')
# stepped roof crest, front ornament.
for x in range(-11,12):
 for y in range(38,39+max(0,(11-abs(x))//3)):b(x,33,y,'red' if y>38 else 'gold')
# Add walkable tier-access stairs at several radial aisles; seat heights follow radial steps.
# Upper terraces are shown as block stairs; orientation and stair state are implementation tasks.
for a in [-150,-120,-60,-30,30,150]:
 for x in range(-49,50):
  for z in range(-53,54):
   rr=r(x,z)
   if 35<=rr<47 and abs((arc(x,z)-a+180)%360-180)*np.pi/180*rr<1.3:
    ys=3+int(rr-35) if rr<42 else (10 if rr<44 else 10+int(rr-43))
    b(x,z,ys,'dark')
# Room carpets, furnishings, wall sconces on supports.
for y in [2,13,25]:
 for x in range(-19,20):
  for z in range(34,59):
   if y==13 and abs(x)<=6 and 36<=z<=57 and (abs(x)==6 or z in [36,57]):b(x,z,y,'red')
   if y==2 and abs(x) in [4,5] and 35<=z<=57:b(x,z,y,'teal')
for x0,x1,z0,z1 in [(-19,-9,51,58),(9,19,34,58)]:
 for x in range(x0,x1+1):
  for z in range(z0,z1+1):
   if x in [x0,x1] or z in [z0,z1]:b(x,z,13,'purple')
# replace old bed, move to back outside stairs.
clear(-14,-10,51,57,14,17)
fill(-18,-15,52,56,14,14,'wood');fill(-18,-15,53,55,15,15,'cloth');fill(-18,-15,56,56,15,15,'white');fill(-18,-15,57,57,14,17,'wood2')
for z in [37,40,43,46,49,52,55,57]:
 fill(18,18,z,z,14,17,'wood');b(18,z,15,'book');b(18,z,16,'red')
for x,z in [(-10,54),(10,54),(-6,38),(6,38),(-6,54),(6,54)]:
 if (x,z,17) in V and V[x,z,17]=='lantern':clear(x,x,z,z,17,18)
 b(x,z,14,'stone');b(x,z,15,'dark');lamp(x,z,16)
for x,z in [(-5,37),(5,37),(-5,48),(5,48),(-5,55),(5,55)]:
 if V.get((x,z,5))=='lantern':b(x,z,3,'stone');b(x,z,4,'dark')
# Place two low seating benches in library and flower pots in foyer.
for z in [36,55]:
 fill(10,14,z,z,14,14,'wood');fill(10,14,z,z,15,15,'red')
for x in [-11,11]:
 for z in [36,57]:b(x,z,3,'wood2');b(x,z,4,'leaf2')
# More lanterns at entrances and gallery arcs: on a stone plinth, never suspended in air.
for a in range(5,360,10):
 t=np.deg2rad(a)
 for rad in [43.5]:
  x=round(rad*np.cos(t));z=round(rad*1.08*np.sin(t))
  if z>=30 and abs(x)<=24:continue
  if abs(x)<=5 or abs(z)<=2:continue
  if all((x,z,y) not in V for y in [2,3,4]):b(x,z,2,'dark');lamp(x,z,3)
# Move the royal seat to the rear edge of the open loggia, facing the combat floor.
for (x,z,y),m in list(V.items()):
 if -5<=x<=5 and 49<=z<=54 and 26<=y<=35:
  del V[x,z,y];b(x,z-16,y,m)
for x in range(-5,6):
 for z in range(28,48):b(x,z,25,'red' if abs(x)<5 else 'gold')
for (x,z,y),m in list(V.items()):
 if m=='lantern' and 33<z<59 and abs(x)<20 and 15<=y<=17:
  for yy in range(14,y):
   if (x,z,yy) not in V:b(x,z,yy,'dark' if yy==y-1 else 'stone')
from collections import deque
# A lantern emits level 15 in Minecraft; simplified opaque voxel propagation, no skylight.
def light_field():
 q=deque(); levels={}
 for (x,z,y),m in V.items():
  if m=='lantern' and y<=6 and 30<=r(x,z)<=52:
   levels[x,z,y]=15;q.append((x,z,y))
 while q:
  x,z,y=q.popleft();v=levels[x,z,y]-1
  if v<=0:continue
  for dx,dz,dy in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
   p=(x+dx,z+dz,y+dy)
   if not(1<=p[2]<=8 and 30<=r(p[0],p[1])<=53):continue
   if p in V and V[p] not in ['lantern','water','water2']:continue
   if levels.get(p,0)<v:levels[p]=v;q.append(p)
 return levels
walk=[(x,z,2) for x in range(-51,52) for z in range(-56,57) if 40<=r(x,z)<47 and not(z>=30 and abs(x)<=24) and (x,z,1) in V and (x,z,2) not in V and (x,z,3) not in V]
levels=light_field()
for _ in range(70):
 dark=[p for p in walk if p not in V and (p[0],p[1],3) not in V and levels.get(p,0)<8]
 if not dark:break
 x,z,y=min(dark,key=lambda p:levels.get(p,0));lamp(x,z,y);levels=light_field()
walk=[p for p in walk if p not in V and (p[0],p[1],3) not in V]
print('Gallery light audit:',len(walk),'min',min(levels.get(p,0) for p in walk),'max',max(levels.get(p,0) for p in walk), 'below8',sum(levels.get(p,0)<8 for p in walk),flush=True)
# Light audit plot uses actual model cells and source coordinates, not decorative estimates.
def lightmap():
 arr=np.full((113,103),np.nan)
 for x,z,y in walk:arr[z+56,x+51]=levels.get((x,z,y),0)
 fig,ax=plt.subplots(figsize=(10,9),dpi=130,facecolor='#f3f1eb');ax.set_facecolor('#e5e0d5');im=ax.imshow(arr,origin='lower',extent=(-51.5,51.5,-56.5,56.5),vmin=0,vmax=15,cmap='viridis',interpolation='nearest')
 pts=[(x,z) for (x,z,y),m in V.items() if m=='lantern' and y<=6 and 39<=r(x,z)<47]
 if pts:ax.scatter(*zip(*pts),s=8,c='#ffd477',edgecolors='#543b25',linewidths=.3,label='Lantern')
 ax.set(xlabel='X, blocks',ylabel='Z, blocks',title='GALLERY / BLOCK LIGHT PROXY');ax.grid(alpha=.18);ax.legend(loc='lower right');fig.colorbar(im,ax=ax,label='Simulated block light (0–15)',shrink=.75);fig.text(.1,.02,'6-neighbour propagation | opaque blocks | no sky light | not Minecraft runtime verification',fontsize=9);fig.savefig(OUT/'08_lightmap.png',bbox_inches='tight');plt.close(fig)
# block mesh rendering
FACES=[((-1,0,0),[(0,0,0),(0,1,0),(0,1,1),(0,0,1)],.78),((1,0,0),[(1,0,0),(1,0,1),(1,1,1),(1,1,0)],.87),((0,-1,0),[(0,0,0),(0,0,1),(1,0,1),(1,0,0)],.88),((0,1,0),[(0,1,0),(1,1,0),(1,1,1),(0,1,1)],.72),((0,0,1),[(0,0,1),(0,1,1),(1,1,1),(1,0,1)],1.)]
RGB={k:np.array(to_rgb(v)) for k,v in P.items()}
def render(name,title,sub,data,view=(29,-63),limits=((-55,55),(-60,61),(-2,47)),night=False,figsize=(11,9)):
 print('render',name,len(data),flush=True)
 ee,aa=np.deg2rad(view);camera=np.array([np.cos(ee)*np.cos(aa),np.cos(ee)*np.sin(aa),np.sin(ee)])
 verts=[];colors=[];L=np.array(lights)
 for (x,z,y),m in data.items():
  for normal,vs,shade in FACES:
   if np.dot(normal,camera)<=1e-6:continue
   nb=data.get((x+normal[0],z+normal[1],y+normal[2]));
   if nb is not None and not (nb.startswith('water') and not m.startswith('water')):continue
   col=RGB[m]*shade
   if night and m not in ['fire','flame','lantern']:
    d=np.abs(L-np.array([x,z,y])).sum(axis=1).min();t=np.clip((14-d)/14,0,1);col=col*(.32+.68*t)+np.array([.12,.07,.005])*t
   jitter=1+(((x*13+z*7+y*11)%11)-5)*.009
   verts.append([(x+a,z+c,y+d) for a,c,d in vs]);colors.append(np.clip(col*jitter,0,1))
 fig=plt.figure(figsize=figsize,dpi=130,facecolor='#f3f1eb');ax=fig.add_subplot(111,projection='3d');ax.set_proj_type('ortho');ax.set_facecolor('#f3f1eb')
 pc=Poly3DCollection(np.array(verts,dtype=np.float32),facecolors=colors,edgecolors=(.19,.15,.10,.20),linewidths=.11,antialiased=False,zsort='average');ax.add_collection3d(pc)
 ax.set(xlim=limits[0],ylim=limits[1],zlim=limits[2],xlabel='X, blocks',ylabel='Z, blocks',zlabel='Y, blocks');ax.set_box_aspect(tuple(b-a for a,b in limits));ax.view_init(elev=view[0],azim=view[1]);ax.tick_params(labelsize=8)
 for a in [ax.xaxis,ax.yaxis,ax.zaxis]:a.pane.set_facecolor('#eeece6');a._axinfo['grid']['color']=(.5,.5,.5,.25)
 fig.subplots_adjust(left=.01,right=.98,bottom=.02,top=.91);fig.text(.045,.965,title,fontsize=19,fontweight='bold',color='#3b322b');fig.text(.045,.932,sub,fontsize=10,color='#65584d');fig.savefig(OUT/name,dpi=130);plt.close(fig)
if __name__=='__main__':
 import sys
 jobs={
 'overview':lambda:render('01_overview.png','LOST OASIS / V1','Procedural block model | approx. 105 x 116 x 47 blocks | south-east',V,(37,-65)),
 'top':lambda:render('02_top.png','ARENA / TOP VIEW','Dry central arena, grounded islands, four crossings and surrounding amphitheatre',V,(89.99,-90),figsize=(10,10)),
 'rear':lambda:render('03_rear.png','COLOSSEUM / EXTERIOR','Rear palace and annular outer arcade | north-west view',V,(29,135)),
 'gallery':lambda:render('04_gallery.png','UNDER THE STANDS / CUTAWAY','East sector | lanterns and hollow corridor | schematic light, roof partially removed',{p:m for p,m in V.items() if 34<=p[0]<=51 and -17<=p[1]<=17 and -1<=p[2]<=23 and not (p[2]>=7 and p[1]<1)},(34,-120),((33,53),(-18,19),(-1,25)),True,(11,8)),
 'ground':lambda:render('05_palace_ground.png','PALACE / LEVEL 1','Entry hall, treasure and storage wings, stair flights | section',{p:m for p,m in V.items() if -20<=p[0]<=20 and 33<=p[1]<=59 and 2<=p[2]<=11 and not (p[1]==33 and p[2]>3)},(57,-70),((-21,21),(32,61),(2,13)),False,(10,8)),
 'middle':lambda:render('06_palace_middle.png','PALACE / LEVEL 2','Bedroom, dining hall, library and stairs | section',{p:m for p,m in V.items() if -20<=p[0]<=20 and 33<=p[1]<=59 and 13<=p[2]<=23 and not (p[1]==33 and p[2]>14)},(57,-70),((-21,21),(32,61),(13,25)),False,(10,8)),
 'throne':lambda:render('07_palace_throne.png','PALACE / LEVEL 3','Royal loggia 23 x 12 blocks, throne chamber, open view towards arena',{p:m for p,m in V.items() if -20<=p[0]<=20 and 23<=p[1]<=59 and 25<=p[2]<=36},(43,-70),((-21,21),(22,61),(25,39)),False,(10,9)),
 }
 jobs['light']=lightmap
 jobs['front']=lambda:render('09_front.png','ROYAL LOGGIA / ARENA FRONT','Low front view | palace, open throne and grounded oasis islands',V,(22,-90))
 for k in sys.argv[1:] or jobs:jobs[k]()
 print('blocks:',len(V),'lights:',len(lights),flush=True)
