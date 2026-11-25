import numpy as np, math, json, sys
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from matplotlib.colors import to_rgb
from pathlib import Path
from PIL import Image
P=Path(__file__).parent
K='#362832'; D='#513641'; R='#a32e39'; C='#e04b3c'; G='#dfa54b'; L='#f7c67c'; I='#e5cba4'; H='#63443a'; E='#ffb52d'; W='#51382d'
V=np.array([[-1,-1,-1],[1,-1,-1],[1,1,-1],[-1,1,-1],[-1,-1,1],[1,-1,1],[1,1,1],[-1,1,1]])/2
FS=[[0,3,2,1],[4,5,6,7],[0,1,5,4],[3,7,6,2],[0,4,7,3],[1,2,6,5]]
def v(x):return np.array(x,float)
def u(x):return x/max(np.linalg.norm(x),1e-8)
def rot(axis,a):
 x,y,z=u(v(axis));c=math.cos(a);s=math.sin(a);q=1-c
 return np.array([[c+x*x*q,x*y*q-z*s,x*z*q+y*s],[y*x*q+z*s,c+y*y*q,y*z*q-x*s],[z*x*q-y*s,z*y*q+x*s,c+z*z*q]])
class Mesh:
 def __init__(self):self.b=[];self.part='body'
 def box(self,p,s,c,r=None):self.b.append((v(p),v(s),c,np.eye(3) if r is None else r,self.part))
 def seg(self,a,b,w,d,c):
  a,b=v(a),v(b);y=u(b-a);ref=v((0,0,1)) if abs(y[2])<.94 else v((1,0,0));x=u(np.cross(y,ref));z=np.cross(x,y);self.box((a+b)/2,(w,np.linalg.norm(b-a)+.65,d),c,np.column_stack((x,y,z)))
 def feather(self,a,b,w,d,c,n=6,tip=None):
  a,b=v(a),v(b);axis=u(b-a);ref=v((0,0,1)) if abs(axis[2])<.94 else v((1,0,0));side=u(np.cross(axis,ref));normal=np.cross(side,axis);r=np.column_stack((side,axis,normal));length=np.linalg.norm(b-a)
  for j in range(n):
   t=(j+.5)/n;ww=np.interp(t,[0,.17,.4,.6,.8,1],[.76,1,.98,.84,.53,.08]);dd=d*(1-.55*t);blend=max(0,(t-.32)/.68);color=tuple(np.array(to_rgb(c))*(1-blend)+np.array(to_rgb(tip))*blend) if tip else c
   self.box(a+(b-a)*t,(w*ww,length/n+.28,dd),color,r)
  if tip:self.seg(a+normal*d*.42,a+(b-a)*.65+normal*d*.23,w*.14,d*.23,c)
 def chain(self,p,w,d,c):
  for j in range(len(p)-1):self.seg(p[j],p[j+1],w*(1-.8*j/(len(p)-1)),d*(1-.8*j/(len(p)-1)),c if isinstance(c,str) else c[j])
 def band(self,a,b,t,w,d):
  a,b=v(a),v(b);p=a+(b-a)*t;z=u(b-a);self.seg(p-z,p+z,w,d,G)
 def jewel(self,p,s):self.box(p,(s+1.5,s+1.5,1.5),G);self.box(v(p)+[0,0,1],(s,s,1),'#de3722');self.box(v(p)+[-s*.2,s*.2,1.6],(s*.3,s*.3,.5),E)
 def move(self,start,r,p=(0,0,0),o=(0,0,0)):
  p,o=v(p),v(o)
  for j in range(start,len(self.b)):
   a,s,c,m,g=self.b[j];self.b[j]=(p+r@(a-p)+o,s,c,r@m,g)
def build(action='idle',t=0):
 m=Mesh();b,s,f=m.box,m.seg,m.feather;q=math.sin(math.pi*t);walk=math.sin(2*math.pi*t) if action=='walk' else 0
 cast=min(1,t/.38,max(0,(1-t)/.25)) if action in ('beam','volley','orb','pillars','ultimate') else 0;aim=cast*(.25 if t<.35 or action=='pillars' else 1);claw=q if action=='claw' else 0;flare=q*(.65 if action in ('flash','dive','ultimate') else .07);alt=23*q if action in ('dive','ultimate') else 0
 # Chest, waist, individually layered feathers, ornamental collar and belt.
 b((0,46,0),(17,19,15),D);b((0,57,0),(27,22,19),K);b((0,64,-1),(31,12,18),D);b((0,40,0),(23,4.5,17),G);b((0,50,7),(13,12,4),K)
 for k in (-1,1):
  for j in range(7):
   f((k*(2+2*j),67-j*.7,7),(k*(4+2.3*j),50-j*.7,11),5.5,4.1,[K,D,R][j%3]);f((k*(4+2*j),69-j*.5,-1),(k*(10+3*j),61-j*1.3,7),5.7,4.1,[C,R,D][j%3]);f((k*(2+1.7*j),39,5-j*.7),(k*(5+3*j),14+j*1.3,8-j*.6),5.5,3,[R,K,C,D][j%4]);f((k*(2+j*2),39,-5),(k*(5+j*2.8),13+j*1.8,-13),5,3,[K,R][j%2])
  s((k*15,66,10),(k*8,59,12),3.5,3,G);s((k*8,59,12),(0,56,13),3.3,3,L)
 m.jewel((0,56,14),5.5);s((0,53,11),(0,46,10),2,2,G);m.jewel((0,40,9),3.5)
 b((0,26,10),(7,24,1.5),R);b((0,12,10),(4,5,1.5),R)
 for k in (-1,1):s((k*3.4,38,11),(k*3.4,15,11),.8,.8,G);s((k*3.4,15,11),(0,10,11),.8,.8,G)
 for j in range(8):
  a=j*math.pi/4;s((1.5*math.cos(a),26+1.5*math.sin(a),11),(3*math.cos(a),26+3*math.sin(a),11),.7,.7,L)
 b((0,26,11.1),(2.3,2.3,.8),G)
 for j in range(9):f(((j-4)*1.7,39,-7),((j-4)*3,6+abs(j-4)*2,-26+abs(j-4)),5,3,[R,K,C][j%3])
 # Avian head: articulated-looking ruff, eyes and hooked ivory beak, eight horns.
 m.part='head';s((0,65,0),(0,83,4),12.5,13,K);b((0,86,6),(15.5,12,14),K);b((0,90,5),(12,6,13),D)
 for row in range(4):
  for j in range(18):
   a=j*math.pi/9+row*.16;x,z=math.cos(a),math.sin(a);y=91-row*4;f((x*(5+row),y,3+z*5),(x*(12+row*2.4),y-10-row*1.1,3+z*(9+row)),4.8,4.1,[R,K,R,D,C][(j+row)%5],5)
 for k in (-1,1):
  for j in range(5):f((k*(1.5+j*.9),91-j*.45,9-j*1.3),(k*(8+j*2),101-j*2,-3-j*.6),3.7,3.8,[C,R,R,K,D][j],5)
  b((k*4.8,86,13.4),(5.4,3.4,2.1),K);b((k*4.9,86.2,14.7),(3.8,2.35,1.4),'#ff571b');b((k*4.3,86.2,15.5),(1.9,1.6,.6),'#ffe75c')
  s((k*8,90.3,11.7),(k*2,87.6,15.6),2.9,3.4,I);s((k*7.8,88.8,14),(k*8.6,83.7,12.4),2.7,3,I);f((k*6.8,84.8,13.1),(k*5.3,77,13),3.6,3.4,I,5)
  for pts,w in [([(6,92,0),(11,94,0),(17,94,0),(23,93.8,0),(28,97,1),(31,102,2),(31,107,3),(28,111,4),(24,114,5)],7.3), ([(3,94,-3),(6,98,-4),(7,103,-4),(6,108,-4),(3,112,-3)],4.5), ([(6,86,0),(12,86,2),(16,84,4),(18,80,5),(17,76,6),(14,73,7)],5.1), ([(6,92,-5),(11,96,-7),(16,100,-8),(19,104,-8),(20,109,-7)],4.1)]:
   pts=[(k*x,y,z) for x,y,z in pts];m.chain(pts,w,w*.9,[K if j==0 else H if j<(len(pts)-1)*.52 else I for j in range(len(pts)-1)]);m.band(pts[1],pts[2],.5,w*1.12,w*1.04)
  for x,y in [(24,100),(18,94)]:s((k*x,y,0),(k*x,y-8,0),.9,.9,G);b((k*x,y-8,0),(2,3,1.8),L)
 s((0,88,13),(0,84,18),6.7,5.5,I);s((0,84,18),(0,81,22),5.1,4.2,I);s((0,81,22),(0,76.5,23),2.7,2.8,I);s((0,80,13),(0,79,18),4.2,2.5,H);b((0,90,13),(3.7,4,3),G);b((0,91,14.8),(1.6,2.4,1),R)
 for j in range(10):
  a=math.radians(25+j*13);a2=a+math.radians(13);s((18*math.cos(a),90+18*math.sin(a),-5),(18*math.cos(a2),90+18*math.sin(a2),-5),1.8,1.8,G)
 b((0,109,-5),(2.5,7,2.3),G);m.jewel((0,108,-3.6),2.7)
 # Wings: curved articulated skeleton, distinct tapered feathers, overlapping depth layers.
 for k in (-1,1):
  m.part='wing_R' if k<0 else 'wing_L';start=len(m.b);root=(k*12,65,-11)
  def pt(x,y,z):return (k*x,y,z)
  def qf(a,b,w,d,c,tip,n=8):f(pt(*a),pt(*b),w,d,c,n,tip)
  s(root,pt(26,76,-14),10,10,K);s(pt(26,76,-14),pt(43,94,-15),11,9,K);s(pt(43,94,-15),pt(57,88,-17),8,7,D);s(pt(57,88,-17),pt(78,73,-21),5.5,5.5,K)
  for j in range(13):
   t=j/12;qf((43+24*t,94-15*t,-17-7*t),(76+22*math.sin(t*math.pi*.53),83-69*t+2.3*math.sin(j*2.1),-24-11*t),11-2*t,6.2,K if j%4 else D,C if j%3!=1 else R,11)
  # Long intermediate remiges close the lower gap while preserving separate pointed ends.
  for j in range(11):
   t=j/10;qf((37+22*t,88+3*math.sin(math.pi*t)-7*t,-19-5*t),(46+40*t,20-6*math.sin(math.pi*t)+3*math.sin(j*1.8),-27-7*t),10.1,6,K if j%3 else R,C if j%3!=1 else R,10)
  for j in range(12):
   t=j/11;qf((13+32*t,68+24*t,-15-2*t),(17+31*t,39-19*t,-22-3*t),8.4,5.8,K if j%3 else D,R if j%3 else C,8)
  for j in range(15):
   t=j/14;qf((13+57*t,67+25*math.sin(t*math.pi*.88),-10-7*t),(22+60*t,48+4*t+12*math.sin(t*math.pi*.9),-16-7*t),7.6-1.5*t,5.1,D if j%4==0 else K,R if j%3==0 else D,7)
  for row in range(2):
   for j in range(14):
    t=(j+.35*row)/14;qf((13+61*t,69+24*math.sin(t*math.pi*.86)-row*5,-7-7*t+row*1.3),(19+64*t,58+20*math.sin(t*math.pi*.86)-row*7,-10-8*t+row*1.3),6.9-row*.5,4.2,[K,D,R][(j+row)%3],R if row==0 and j%2==0 else D,6)
  for j in range(15):
   t=j/14;qf((13+57*t,69+24*math.sin(t*math.pi*.88),-21-7*t),(22+62*t,44+17*math.sin(t*math.pi*.9),-26-8*t),7.8,4.2,K if j%3 else R,R if j%3 else C,7)
  s(pt(39,90,-8),pt(47,94,-9),5.3,5.7,G);s(pt(47,94,-9),pt(53,89,-10),4.7,5.6,L);f(pt(44,94,-10),pt(40,103,-10),4,4,I,5)
  points=[pt(46,91,-5),pt(46,78,-5),pt(52,74,-6),pt(59,77,-8),pt(63,84,-10)]
  for a,c in zip(points,points[1:]):s(a,c,1.7,1.7,G)
  m.jewel(pt(46,78,-3.8),3.5);s(pt(46,74,-4),pt(46,62,-6),1.5,1.5,G);b(pt(46,60,-5),(3.2,5,2),L);b(pt(46,60,-3.8),(1.6,2.6,.6),R)
  m.move(start,rot((0,0,1),k*(.015+flare*.55))@rot((0,1,0),k*.05),root)
 # Legs and three front talons, separate arms and staff attached to grip.
 for k in (-1,1):
  m.part='leg_R' if k<0 else 'leg_L';stride=k*walk*8;up=max(0,k*walk)*3;hip=v((k*8,38,0));knee=v((k*13,22,4+stride*.4));ankle=v((k*19,8+up,-1+stride));foot=v((k*20,3+up,6+stride));s(hip,knee,10,10,K);s(knee,ankle,7,7,D);s(ankle,foot,8,8,K);m.band(knee,ankle,.65,8.8,8.5);m.band(knee,ankle,.85,8.8,8.5);b(foot,(15,5,12),K)
  for j in (-1,0,1):
   a=foot+[j*4.2,0,3];c=foot+[j*6,0,10-abs(j)];d=foot+[j*6.8,-2,13-abs(j)];s(a,c,3.7,3.5,K);m.chain([c,d,d+[0,-.7,2]],3.3,3.1,I)
  s(foot+[0,0,-3],foot+[0,-1,-7],3,3,I)
 for k in (-1,1):
  m.part='arm_R' if k<0 else 'arm_L';sh=v((k*15,65,0));el=v((k*27,52,4));hand=v((k*37,39,13))
  if k<0:el+=v((-1,6+6*cast,8*aim));hand=v((-36,51+13*cast,16+16*aim))
  else:el+=v((4*claw,4*claw,9*claw));hand+=v((4*claw,13*claw,25*claw))
  s(sh,el,10,9,D);s(el,hand,8.5,8.5,K);s(el+(hand-el)*.2,el+(hand-el)*.7,10,9.5,R);m.band(el,hand,.18,11,10.5);m.band(el,hand,.75,11,10.5);b(hand,(10,8,9),K)
  for j in range(6):f(sh+[k*j*1.1,2-j*.6,4],sh+[k*(8+j*2),-10-j*.8,7],5.4,3.8,[R,C,D][j%3],6)
  if k>0:
   for j in range(3):
    a=hand+[(j-1)*3.8,-1,2];c=a+[(j-1)*2.3,-5,3];d=c+[(j-1)*.8,-9,3.5];s(a,c,4.2,4.2,K);m.chain([c,c+[(j-1)*.8,-4,1],d,d+[-(j-1)*2,-1,4],d+[-(j-1)*3,1,5]],4.1,3.7,I)
   m.chain([hand+[-4,0,0],hand+[-7,-4,3],hand+[-5,-8,5]],3.2,3,I)
  else:
   m.part='staff';start=len(m.b);s(hand+[0,-49,0],hand+[0,33,0],3.2,3.2,W)
   for y in (-47,-29,-14,6,23,30):b(hand+[0,y,0],(4.7,2.5,4.7),G)
   center=hand+[0,36,0];b(center,(6,6,5),E);b(center+[0,0,3],(4,4,1),'#fff080')
   for j in range(12):
    a=j*math.pi/6;a2=a+math.pi/6;s(center+[6*math.cos(a),6*math.sin(a),0],center+[6*math.cos(a2),6*math.sin(a2),0],2.3,3,G)
   for j in range(8):
    a=j*math.pi/4;s(center+[6*math.cos(a),6*math.sin(a),0],center+[11*math.cos(a),11*math.sin(a),0],2.7,2.5,L)
   rr=rot((1,0,0),1.4*aim)@rot((0,0,1),-.17*cast);m.move(start,rr,hand);staff=hand+rr@(center-hand);m.part='arm_R'
   for j in range(3):b(hand+[0,(j-1)*2.3,4.8],(8.5,1.9,2.5),I)
 lean=.05*walk+(.55*q if action=='dive' else 0)+(-.2*q if action=='hurt' else 0)+(1.28*t if action=='death' else 0);rr=rot((0,1,0),.3*claw)@rot((1,0,0),lean);m.move(0,rr,o=(0,alt+1.2,0));staff=rr@staff+[0,alt+1.2,0]
 m.part='FX'
 def ring(p,r):
  for j in range(40):
   a=j*math.pi/20;a2=a+math.pi/20;s(v(p)+[r*math.cos(a),0,r*math.sin(a)],v(p)+[r*math.cos(a2),0,r*math.sin(a2)],1.4,1.4,E)
 def orb(p,r):
  for h in np.linspace(-r,r,5):
   d=max(.5,math.sqrt(max(0,r*r-h*h)));b(v(p)+[0,h,0],(d*1.8,r*.45,d*1.8),E if abs(h)>r*.55 else '#ffdf50')
 if action=='beam' and .35<t<.8:s(staff,staff+[0,-5,80],3.5,3.5,'#ffe05a')
 if action=='volley' and .3<t<.9:
  for j in range(5):
   if t>.3+.05*j:orb(staff+[j*3-6,0,(t-.3-.05*j)*95],2.5)
 if action in ('orb','ultimate') and .18<t<.86:orb(staff+[0,5,max(0,t-.45)*100],(8 if action=='ultimate' else 5)*min(1,t/.35))
 if action=='pillars' and .18<t<.85:
  for x,z in [(-20,35),(8,48),(28,27)]:
   ring((x,.8,z),8)
   if t>.45:s((x,1,z),(x,95,z),5,5,'#ffe05a')
 if action in ('flash','dive') and .4<t<.9:ring((0,.8,0),8+(t-.4)*130)
 return m

def render(action='idle',t=0,az=90,el=0,ax=None,size=8,title=None):
 own=ax is None
 if own:fig=plt.figure(figsize=(size,size*.72));ax=fig.add_subplot(111,projection='3d')
 m=build(action,t);faces=[];colors=[];light=u(v((-.4,.8,.6)))
 for p,sz,c,r,g in m.b:
  vs=(V*sz)@r.T+p;ff=vs[FS];nn=np.cross(ff[:,1]-ff[:,0],ff[:,2]-ff[:,0]);nn/=np.maximum(np.linalg.norm(nn,axis=1)[:,None],1e-9);shade=.64+.36*np.maximum(0,nn@light);faces.extend(ff[:,:,[0,2,1]]);colors.extend(np.array(to_rgb(c))[None,:]*shade[:,None])
 ax.add_collection3d(Poly3DCollection(faces,facecolors=colors,edgecolors=(.1,.07,.08,.24),linewidths=.12,zsort='average'))
 ymax=145 if action not in ('idle','walk','hurt','death') else 45;h=150 if action in ('ultimate','dive') else 120
 ax.set(xlim=(-104,104),ylim=(-45,ymax),zlim=(-3,h),xlabel='X',ylabel='Z',zlabel='Y');ax.set_box_aspect((208,ymax+45,h+3),zoom=1.20 if own else 1.02);ax.set_proj_type('ortho');ax.view_init(el,az);ax.tick_params(labelsize=7,pad=0);ax.grid(True);ax.set_title(title or f'{action.upper()} | t={t:.2f}',fontsize=11)
 if own:fig.subplots_adjust(left=.02,right=.98,top=.94,bottom=.05);return fig

def static():
 fig=plt.figure(figsize=(15,11))
 for j,(az,el,name) in enumerate([(90,0,'FRONT'),(0,0,'RIGHT'),(-90,0,'BACK'),(180,0,'LEFT'),(67,12,'FRONT 3/4'),(-125,14,'BACK 3/4')]):render(ax=fig.add_subplot(2,3,j+1,projection='3d'),az=az,el=el,title=name)
 fig.suptitle('HALA V2 | procedural cuboid mesh | X / Z / Y',fontsize=16);fig.subplots_adjust(left=.01,right=.99,top=.94,bottom=.02,wspace=.02,hspace=.03);fig.savefig(P/'views.png',dpi=150);plt.close(fig)
 fig=render(az=90,el=5,size=13,title='HALA V2 | real cuboid geometry');fig.savefig(P/'front.png',dpi=170);plt.close(fig)
 fig=render(az=62,el=10,size=13,title='HALA V2 | 3/4 view');fig.savefig(P/'threequarter.png',dpi=170);plt.close(fig)
 m=build();pts=np.concatenate([(V*s)@r.T+p for p,s,c,r,g in m.b]);print('cuboids',len(m.b),'bounds',pts.min(0),pts.max(0),flush=True);(P/'geometry.json').write_text(json.dumps({'coordinate_system':'X lateral, Y up, Z forward','units_per_block':14,'bounds':[pts.min(0).tolist(),pts.max(0).tolist()],'cuboids':[dict(center=p.tolist(),size=s.tolist(),color=matplotlib.colors.to_hex(c),rotation=r.tolist(),part=g) for p,s,c,r,g in m.b]},indent=1))
 fig=plt.figure(figsize=(15,14));acts=['walk','beam','volley','orb','pillars','flash','dive','claw','ultimate']
 for j,a in enumerate(acts):render(a,.56,az=65,el=12,ax=fig.add_subplot(3,3,j+1,projection='3d'),title=a.upper())
 fig.suptitle('HALA V2 | procedural animation poses');fig.subplots_adjust(left=.01,right=.99,bottom=.01,top=.95,wspace=0,hspace=.05);fig.savefig(P/'poses.png',dpi=130);plt.close(fig)
def animate(a):
 frames=[]
 for t in np.linspace(0,1,9,endpoint=a!='walk'):
  fig=render(a,float(t),az=65,el=12,size=5);fig.canvas.draw();frames.append(Image.fromarray(np.asarray(fig.canvas.buffer_rgba()).copy()).convert('RGB'));plt.close(fig)
 frames[0].save(P/(a+'.gif'),save_all=True,append_images=frames[1:],duration=180,loop=0,disposal=2)
if __name__=='__main__':
 if len(sys.argv)>1:
  for a in sys.argv[1:]:animate(a)
 else:static()
