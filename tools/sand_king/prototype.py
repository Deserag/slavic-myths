import numpy as np
from math import sin,cos,pi
D='#35201c'; R='#542c25'; M='#774036'; T='#955b42'; E='#bc8252'; G='#e5a333'; L='#ffd76c'; J='#32231d'
V=np.array([[-1,-1,-1],[1,-1,-1],[1,1,-1],[-1,1,-1],[-1,-1,1],[1,-1,1],[1,1,1],[-1,1,1]])/2
def raw_build(action='idle',t=0):
 p=[]; amp=sin(pi*t)**2; wave=sin(2*pi*t); h=0; shift=0; pitch=0
 def box(c,s,col,g,rot=None):
  v=V*np.array(s);p.append((v@rot.T+np.array(c) if rot is not None else v+np.array(c),col,g))
 def beam(a,b,w,d,c,g):
  a,b=np.array(a),np.array(b);u=b-a;ll=np.linalg.norm(u);u=u/ll;r=np.cross(u,[0,0,1]);r=np.cross(u,[0,1,0]) if np.linalg.norm(r)<.001 else r;r=r/np.linalg.norm(r);q=np.cross(r,u);box((a+b)/2,(w,ll+.04,d),c,g,np.array([r,u,q]).T)
 def plate(a,b,w,d,g):
  a,b=np.array(a),np.array(b);v=b-a;beam(a,b,w*.72,d*.74,J,g);beam(a+.09*v,a+.92*v,w,d,R,g);beam(a+.12*v+[0,0,.035],a+.62*v+[0,0,.035],w*1.02,d*.95,M,g);beam(a+.15*v,a+.25*v,w*1.04,d*1.01,E,g)
 def taper(pts,ws,ds,cs,g):
  for i in range(len(pts)-1):beam(pts[i],pts[i+1],ws[i],ds[i],cs[i],g)
 for i,(y,w,l,z) in enumerate([(-1.61,1.75,.95,1.78),(-.92,2.65,1.12,1.87),(-.04,3.0,1.05,1.91),(.84,2.86,1.04,1.91),(1.64,2.55,.99,1.88),(2.34,2.12,.87,1.82),(2.98,1.51,.69,1.76)]):
  g=f'body{i}';box((0,y,z-.14),(w*.82,l,.91),D,g);box((0,y,z+.12),(w,l*.96,.66),R,g);box((0,y,z+.43),(w*.78,l*.89,.34),M,g);box((0,y,z+.63),(w*.53,l*.82,.21),T,g)
  for s in [-1,1]:
   box((s*w*.41,y,z+.34),(w*.19,l*.85,.28),T,g);box((s*w*.35,y-l*.39,z+.53),(w*.21,.105,.10),G,g)
  if i>0:
   box((0,y-.25,z+.77),(.34,.35,.13),G,g)
   for sign in [-1,1]:
    beam((0,y-.25,z+.77),(sign*w*.25,y-.12,z+.69),.12,.09,G,g)
    box((sign*w*.30,y+.13,z+.63),(.16,.24,.12),G,g)
    box((sign*w*.44,y+.05,z+.37),(.15,.23,.15),E,g)
   if i in (2,3,4,5):taper([(0,y+.20,z+.66),(0,y+.31,z+.97),(0,y+.46,z+1.12)],[.37,.18],[.38,.19],[M,T],g)
 box((0,-1.99,1.62),(1.54,.87,.80),D,'head');box((0,-2.10,1.95),(1.62,.74,.44),M,'head');box((0,-2.28,2.13),(1.43,.32,.23),T,'head')
 for s in [-1,1]:
  box((s*.43,-2.45,1.84),(.4,.12,.23),J,'eye');box((s*.43,-2.52,1.86),(.31,.07,.15),L,'eye');taper([(s*.27,-2.33,1.64),(s*.40,-2.62,1.37),(s*.31,-2.72,1.13)],[.31,.16],[.33,.17],[M,E],'mouth')
 for j in range(-2,3):
  x=.37*j;z=2.18;hh=[1.86,1.40,.96][abs(j)];g=f'horn{j}';taper([(x,-1.98,z),(x*1.17,-1.90,z+.34*hh),(x*1.38,-1.73,z+.72*hh),(x*1.58,-1.48,z+hh)],[.48,.35,.19],[.49,.33,.16],[M,T,E],g);beam((x,-2.20,2.26),(x*1.16,-2.04,2.26+.42*hh),.105,.085,G,g)
 for s in [-1,1]:
  for i in range(4):
   y=-.92+i*.94;g=f'leg{s}_{i}';gait=sin(2*pi*t+(i+(s>0))*pi) if action=='walk' else 0;spread=[3.9,4.35,4.35,3.8][i];dy=[-1.23,-.44,.57,1.56][i]+.58*gait;lift=.42*max(0,gait)
   if action=='jump':spread*=1-.27*amp;lift+=.75*amp
   a=(s*(1.15 if i<3 else .94),y,1.66);b=(s*2.22,y+dy*.38,2.13);c=(s*(spread-.56),y+dy*.76,1.61+lift*.45);d=(s*spread,y+dy,.15+lift);plate(a,b,.49,.52,g);plate(b,c,.59,.62,g);plate(c,d,.43,.49,g);box(d,(.30,.48,.30),D,g)
 for s in [-1,1]:
  strike=0;raised=0;op=.15
  if action=='claw':strike=1.35*amp if s==1 else -.32*amp;op=.12+.75*sin(pi*t);raised=.36*amp if s==1 else .08*amp
  if action=='combo':strike=1.25*min(1,t*12,(1-t)*12)*max(0,s*sin(6*pi*t));op=.10+.52*strike
  if action=='slam':raised=(1.75*sin(pi*t/.65) if t<.65 else 0)-.72*max(0,1-abs(t-.65)/.13);op=.35*amp
  if action=='jump':raised=.35*amp
  g=f'claw{s}';a=(s*1.08,-1.46,1.73);b=(s*(2.06-.25*strike),-2.22-.15*strike,1.59+raised*.45);c=(s*(2.53-.67*strike),-3.08-.63*strike,1.35+raised);plate(a,b,.77,.73,g);plate(b,c,.92,.83,g);x,y,z=c;box((x,y-.37,z),(2.15,1.81,1.31),R,g);box((x,y-.37,z+.37),(1.98,1.61,.49),M,g);box((x,y-.57,z+.58),(1.48,1.20,.25),T,g);box((x,y-.08,z+.49),(1.54,.16,.16),G,g)
  for q in [-1,1]:
   beam((x+q*.15,y+.18,z+.56),(x+q*.72,y-.68,z+.54),.13,.12,G,g)
  for f in [-1,1]:
   taper([(x+s*f*.85,y-1.00,z),(x+s*f*(1.02+op*.22),y-1.55,z-.05),(x+s*f*(.70+op*.42),y-2.24,z-.10),(x+s*f*(.33+op*.64),y-2.81,z-.15),(x+s*f*(.13+op*.64),y-3.13,z-.19)],[.98,.77,.49,.17],[1.06,.82,.52,.19],[R,M,T,E],g);box((x+s*f*.74,y-1.43,z+.32),(.16,.18,.10),G,g)
 tail=np.array([[0,2.88,1.96],[0,3.85,2.30],[0,4.63,3.04],[0,4.86,4.10],[0,4.50,5.20],[0,3.65,6.02],[0,2.47,6.38],[0,1.35,6.12]])
 if action=='sting':
  dif=np.diff(tail,axis=0);orig=np.unwrap(np.arctan2(dif[:,2],dif[:,1]));target=np.array([2.03,2.777,3.149,3.323,3.46,3.669,4.049]);pts=[tail[0]]
  for a,l in zip(orig*(1-amp)+target*amp,np.linalg.norm(dif,axis=1)):pts.append(pts[-1]+[0,l*cos(a),l*sin(a)])
  tail=np.array(pts)
 elif action=='walk':tail[:,0]=np.linspace(0,.30,8)*wave
 for i in range(7):
  a,b=tail[i:i+2];w=1.20-i*.073;g=f'tail{i}';plate(a,b,w,w*.9,g);v=b-a;beam(a+.70*v,a+.80*v,w*.96,w*.88,G,g)
 a=tail[-1];tip=a+np.array([0,-.30-.13*amp,-1.12] if action=='sting' else [0,-.30,-1.12]);taper([a,a+(tip-a)*.37,a+(tip-a)*.76,tip],[.66,.40,.13],[.70,.43,.15],[E,G,L],'stinger')
 if action=='walk':h=.07*abs(wave);shift=.6*(.5-t)
 if action=='jump':h=2.35*sin(pi*t);shift=-2.2*t;pitch=.12*sin(2*pi*t)
 if action=='slam':pitch=-.23*sin(pi*t/.65) if t<.65 else 0
 if action=='sting':pitch=0
 mat=np.array([[1,0,0],[0,cos(pitch),-sin(pitch)],[0,sin(pitch),cos(pitch)]]);p=[((v-[0,0,1.55])@mat.T+[0,shift,1.55+h],c,g) for v,c,g in p];return p
base=np.concatenate([v for v,c,g in raw_build()]); LO=base.min(0); HI=base.max(0); SCALE=np.array([9.,12.,7.])/(HI-LO); CENTER=(HI+LO)/2; CENTER[2]=LO[2]
def build(action='idle',t=0):
 return [((v-CENTER)*SCALE,c,g) for v,c,g in raw_build(action,t)]
