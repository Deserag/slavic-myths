"""0.9.2 native geometry, atlas and deterministic review views. No existing art is rewritten."""
from pathlib import Path
from PIL import Image,ImageDraw
import math,json,random,copy
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';J=R/'src/main/java/org/slavicmyths/client';OUT=R/'docs/verification/world-bosses-0.9.2';OUT.mkdir(parents=True,exist_ok=True)
PAL=['#C9BAA5','#81766A','#171719','#302622','#632126','#C2AF8B','#241713','#D5C59B','#B94B26','#D7692F','#3C2822','#A9792F','#CB9B42','#C47C56','#DD956C','#94553F','#6C2026','#842B2D','#47191D','#25272B','#6D3A31','#5A392B']
N=[]
def group(name,parent='root',pivot=(0,0,0)):
 N.append(dict(name=name,parent=parent,pivot=list(pivot),box=None,mat=0,rot=[0,0,0]));return name

def box(name,parent,xyz,whd,mat,rot=(0,0,0)):
 N.append(dict(name=name,parent=parent,pivot=list(xyz),box=[0,0,0,*whd],mat=mat,rot=list(rot)));return name

def skull(name,x,y,z):
 g=group(name,'body',(x,y,z));box(name+'cord',g,(0,-2,0),(.3,2,.3),10)
 box(name+'bone',g,(-1.1,0,0),(2.2,2.1,1.4),5);box(name+'jaw',g,(-.7,2,.1),(1.4,1.2,1),5)
 for dx in [-.75,.3]:box(name+'socket'+str(dx),g,(dx,.6,-.13),(.65,.65,.2),6)
 box(name+'nose',g,(-.15,1.2,-.15),(.3,.5,.2),6)
 for i in range(3):box(name+'tooth'+str(i),g,(-.55+i*.4,2.5,-.12),(.22,.45,.2),0)

def snake(name,parent,x,y,z,r=1.3):
 # Raised continuous 1.75-turn coil; chunky native geometry, separate head.
 pts=[]
 for i in range(32):
  a=i*math.pi*3.5/31;rr=r*(1-i/42);pts.append((x+math.cos(a)*rr,y+math.sin(a)*rr))
 for i,((x1,y1),(x2,y2)) in enumerate(zip(pts,pts[1:])):
  box(name+str(i),parent,(x1,y1,z),(.32,max(.35,math.hypot(x2-x1,y2-y1)),.25),12,(0,0,-math.atan2(x2-x1,y2-y1)))
 box(name+'head',parent,(pts[-1][0]-.3,pts[-1][1]-.15,z-.05),(.65,.45,.35),12)
 box(name+'eye',parent,(pts[-1][0],pts[-1][1]-.05,z-.1),(.12,.12,.1),6)

def likho():
 group('root','', (0,0,0));group('body');group('head','body',(0,-25,0));group('armL','body',(3.3,-17,0));group('armR','body',(-3.3,-17,0))
 box('neck','body',(-.7,-22,-.5),(1.4,4,1.4),1)
 box('chest','body',(-2.5,-18,-1.4),(5,11,2.8),3);box('innerTunic','body',(-2.7,-8,-1.5),(5.4,8,3),2)
 box('face','head',(-1.8,-3,-1.6),(3.6,6.5,2.6),0)
 box('nose','head',(-.3,1,-2),(.6,1.3,.6),1);box('mouth','head',(-.6,2.65,-1.73),(1.2,.2,.18),6)
 box('sclera','head',(-.7,-.9,-1.9),(1.4,1.25,.35),7);box('iris','head',(-.38,-.7,-2.25),(.76,.83,.2),8);box('irisRing','head',(-.25,-.6,-2.48),(.5,.62,.12),9);box('pupil','head',(-.11,-.48,-2.61),(.22,.4,.1),6)
 box('upperLid','head',(-.9,-1.12,-2.02),(1.8,.3,.3),1);box('lowerLid','head',(-.9,.34,-2),(1.8,.28,.3),1)
 # 22 asymmetrical multi-layer locks; distinct lengths, not a helmet shell.
 for i in range(22):
  if i<8:x=-2.1+(i%4)*1.12;z=-1.5;length=4.3+(i*3%6)+(2 if i>=4 else 0);x+=.2 if i>=4 else 0
  elif i<14:x=-2.3 if i%2 else 1.9;z=-.5+(i%3)*.9;length=10+(i%4)*2
  else:x=-2.1+(i-14)*.58;z=.75+((i-14)%2)*.6;length=31+(i*3%9)
  g=group('hair'+str(i),'head',(x,-3.1,z));box('lock'+str(i),g,(0,0,0),(.55+(i%3)*.14,length,.5),2,(.025*(i%3),0,.02*(i%5-2)))
 for side in ('L','R'):
  arm='arm'+side;group('fore'+side,arm,(0,7,0));box('upper'+side,arm,(-.65,0,-.5),(1.3,7.5,1.2),0);box('elbow'+side,arm,(-.8,6,-.65),(1.6,1.8,1.5),1)
  box('lower'+side,'fore'+side,(-.55,0,-.5),(1.1,10.5,1.1),0)
  for i in range(2 if side=='L' else 1):box('wrap'+side+str(i),'fore'+side,(-.72,8+i*.75,-.68),(1.45,.5,1.45),2)
  box('palm'+side,'fore'+side,(-.85,10,-.55),(1.8,2.4,1.2),0)
  for i in range(4):
   g=group('finger'+side+str(i),'fore'+side,(-.8+i*.48,12,-.4));box('digit'+side+str(i),g,(0,0,0),(.32,2.9+(i%3)*.6,.36),0)
   box('tip'+side+str(i),g,(0,2.6+(i%3)*.6,0),(.25,1.1,.3),1,(.16,0,0))
  box('thumb'+side,'fore'+side,(-1.2,10.8,-.6),(.42,3.2,.4),0,(0,0,.35 if side=='L' else -.35))
  group('leg'+side,'root',(-1.65 if side=='R' else 1.35,.4,0));box('thigh'+side,'leg'+side,(-.65,0,-.6),(1.3,10,1.4),0);box('knee'+side,'leg'+side,(-.8,9,-.95),(1.6,1.5,1.8),1);box('shin'+side,'leg'+side,(-.52,10,-.5),(1.05,13,1.1),0);box('foot'+side,'leg'+side,(-.75,22,-2),(1.5,1.6,3),0)
  for i in range(3):box('toe'+side+str(i),'leg'+side,(-.72+i*.5,22.9,-2.65),(.43,.6,1),0)
 for front,num,z in [('front',8,-1.9),('back',6,1.6)]:
  for i in range(num):
   g=group(front+'Rag'+str(i),'body',(-2.9+i*5.8/num,-7,z));length=10+(i*7%8);box(front+'cloth'+str(i),g,(0,0,0),(.65,length,.4),3 if i%3 else 2);box(front+'hem'+str(i),g,(.12,length-.2,0),(.4,1.4,.4),3)
 box('redStrip','body',(.7,-17,-1.95),(.62,24,.35),4)
 for i in range(3):box('rope'+str(i),'body',(-3,-7+i*.45,-2.1),(6,.3,4.2),10)
 box('beltKnot','body',(-2.2,-6,-2.65),(1.1,1.3,.8),10);box('ropeEnd','body',(-2.1,-5,-2.4),(.35,4.5,.4),10,(0,0,-.12))
 skull('skullA',-2.2,-1,-2.3);skull('skullB',1.4,1.3,-2.2)
 for i in range(5):box('boneCharm'+str(i),'body',(-2.5+i*1.15,-2+(i%3),1.9),(.4,2.8,.4),5,(0,0,.15*(i-2)))
 box('chestCord','body',(-2.2,-17,-1.9),(.3,11,.3),10,(0,0,-.45))
 for i in range(4):box('chestBead'+str(i),'body',(-1.9+i*.95,-15+i*2,-2.1),(.65,.7,.6),5)

def tugarin():
 group('root','',(0,0,0));group('body');group('head','body',(0,-19,0));box('neck','body',(-2,-17,-1),(4,3,3),13)
 box('chest','body',(-7,-16,-3.5),(14,9,7),13);box('belly','body',(-6.5,-9,-4.4),(13,10,9),13);box('back','body',(-5.8,-14,3),(11.6,12,2),15)
 box('headCore','head',(-2.8,-3,-2.2),(5.6,6.1,4.6),13);box('browL','head',(.3,-.75,-2.65),(2.2,.55,.65),2,(0,0,-.12));box('browR','head',(-2.5,-.6,-2.65),(2.2,.55,.65),2,(0,0,.1))
 for x in [-1.65,1.1]:box('eye'+str(x),'head',(x,-.08,-2.55),(.65,.5,.3),7);box('pupil'+str(x),'head',(x+.25,.04,-2.88),(.18,.2,.1),6)
 box('nose','head',(-.9,.15,-3.2),(1.8,1.35,1.6),14);box('mouth','head',(-1.3,1.95,-2.5),(2.6,.7,.35),6);box('mouthGlow','head',(-.75,2.1,-2.86),(1.5,.3,.06),8);box('jaw','head',(-2.2,2.7,-1.8),(4.4,.8,3.7),15)
 box('moustacheCenter','head',(-1.25,1.35,-2.95),(2.5,.8,.7),2)
 for side,sign in [('L',1),('R',-1)]:
  group('moustache'+side,'head',(sign*1.1,1.45,-2.7))
  for i in range(4):box('whisker'+side+str(i),'moustache'+side,(sign*(i*.55),i*.65,0),(.95-i*.13,2.6+i*.5,.75-i*.1),2,(.07,0,sign*(-.38+i*.11)))
  group('arm'+side,'body',(sign*7,-14,0));box('shoulder'+side,'arm'+side,(-2.1,-.8,-2.2),(4.2,4.5,4.4),22 if side=='L' else 23);box('upper'+side,'arm'+side,(-1.8,2,-1.8),(3.6,5.5,3.6),13)
  group('fore'+side,'arm'+side,(0,6.4,0));box('forearm'+side,'fore'+side,(-1.8,0,-1.8),(3.6,5.5,3.6),13);box('bracer'+side,'fore'+side,(-2,1,-2),(4,4.5,4),10)
  for j in range(2):box('bracerRim'+side+str(j),'fore'+side,(-2.15,1+j*4,-2.15),(4.3,.6,4.3),11)
  box('bracerPanel'+side,'fore'+side,(-1.6,1.7,-2.1),(3.2,3.1,.3),19);snake('braceSnake'+side,'fore'+side,0,3,-2.55,.95)
  for i in [-1,1]:box('braceRivet'+side+str(i),'fore'+side,(i*1.45,2,-2.55),(.32,.4,.35),12)
  box('fist'+side,'fore'+side,(-2,5,-2),(4,3.5,4),13)
  for i in range(4):box('knuckle'+side+str(i),'fore'+side,(-1.75+i*.9,5.2,-2.35),(.75,1,.5),14)
  group('leg'+side,'root',(sign*3.3,.9,0));box('pants'+side,'leg'+side,(-2.7,0,-2.5),(5.4,8.5,5),24 if side=='L' else 25);box('baggy'+side,'leg'+side,(sign*.4-2.5,1,-2.4),(5,6,4.8),16)
  box('knee'+side,'leg'+side,(-1.9,8,-1.9),(3.8,3,3.8),17);box('cuff'+side,'leg'+side,(-2.05,10,-2),(4.1,1.5,4.1),18)
  box('bootShaft'+side,'leg'+side,(-2.15,11,-2.1),(4.3,7.7,4.2),10);box('bootFoot'+side,'leg'+side,(-2.4,18,-3.7),(4.8,3.8,6.1),10);box('bootToe'+side,'leg'+side,(-2.5,19,-4.1),(5,2.9,1.4),21);box('heel'+side,'leg'+side,(-2.3,19,1.9),(4.6,3,1.2),19)
  for i in range(2):box('sole'+side+str(i),'leg'+side,(-2.6,21.5+i*.7,-4.2),(5.2,.65,7.5),19 if i else 2)
  for i in range(5):box('tread'+side+str(i),'leg'+side,(-2.4,22.8,-3.8+i*1.35),(4.8,.3,.7),2)
  box('bootSidePlate'+side,'leg'+side,(sign*2.12,13,-1.4),(.5,4,2.5),19);box('bootStud'+side,'leg'+side,(sign*2.4,14,-.5),(.35,.6,.6),12)
 for x in [-4,4]:
  box('shoulderStrap'+str(x),'body',(x-.45,-16,-3.9),(.9,8,.55),10);box('backStrap'+str(x),'body',(x-.45,-16,4.95),(.9,8,.5),10);box('strapFitting'+str(x),'body',(x-.65,-12,-4.2),(1.3,1.4,.35),19)
  for y in [-15,-10]:box('strapRivet'+str(x)+str(y),'body',(x-.15,y,-4.38),(.3,.3,.3),12)
 box('belt','body',(-7,-1.5,-4.9),(14,4.2,10.4),10);box('crotchFold','body',(-1.5,2,-2.7),(3,4.6,5.5),18)
 box('buckleOuter','body',(-2.6,-.85,-5.6),(5.2,2.8,.8),19);box('buckleDisk','body',(-2.2,-.6,-6.2),(4.4,2.4,.6),11);box('buckleOuterTall','body',(-1.7,-1.6,-5.6),(3.4,4.3,.8),19);box('buckleDiskTall','body',(-1.5,-1.3,-6.2),(3,3.8,.6),11);snake('buckleSnake','body',0,.6,-6.8,1.7)
 for i,x in enumerate([-5.6,-3.8,3.2,5]):box('beltPlaque'+str(i),'body',(x,-1.1,-5.3),(1.5,2.5,.5),11)
 for i,x in enumerate([-3.8,2.2]):box('backPlaque'+str(i),'body',(x,-1,5.4),(1.7,2.6,.4),11)
 for i,x in enumerate([-4.3,3.1]):box('hangingStrap'+str(i),'body',(x,2.3,-5.1),(1,5+i*1.4,.6),10);box('strapCap'+str(i),'body',(x-.1,6.4+i*1.4,-5.25),(1.2,.8,.9),11)
 box('backTattoo','body',(-2.3,-12,5.05),(4.6,6,.08),26)

def atlas(name):
 im=Image.new('RGBA',(512,512));d=ImageDraw.Draw(im)
 for i,col in enumerate(PAL+['#C47C56']*5):
  x=i%8*64;y=i//8*64;d.rectangle((x,y,x+63,y+63),fill=col)
  # Material grain follows material, not blanket noisy overlays.
  if i in [0,1,13,14,15]:
   d.rectangle((x+2,y+2,x+10,y+4),fill=PAL[1] if i<2 else PAL[15]);d.line((x+35,y+5,x+44,y+8),fill=PAL[0] if i<2 else PAL[14],width=1)
  elif i in [3,4,10,16,17,18,21]:
   for k in range(3,64,13):d.line([(x+k,y),(x+k+2,y+22),(x+k-1,y+48),(x+k+1,y+63)],fill=PAL[18] if i>=16 else PAL[2],width=2)
  elif i in [11,12,19]:
   d.line((x+2,y+4,x+30,y+4),fill=PAL[12],width=1);d.line((x+15,y+10,x+18,y+16),fill=PAL[10],width=1)
  if i in [22,23,26]:
   # Dedicated front and side snake tattoos, broad S with snake head.
   for dx,dy in [(4,4),(9,4),(14,4),(4,10),(9,10),(14,10)]:
    pts=[(x+dx+1,y+dy),(x+dx+3,y+dy+1),(x+dx+3,y+dy+3),(x+dx,y+dy+4),(x+dx,y+dy+6),(x+dx+3,y+dy+7)]
    d.line(pts,fill='#6D3A31',width=1);d.rectangle((x+dx,y+dy,x+dx+2,y+dy+1),fill='#6D3A31')
  if i in [24,25]:
   d.rectangle((x,y,x+63,y+63),fill=PAL[16]);
   for k in [5,9,13,17,21]:d.line([(x+3,y+k),(x+9,y+k+1),(x+15,y+k-1),(x+21,y+k+1)],fill=PAL[18],width=2);d.line([(x+5,y+k-1),(x+15,y+k-2)],fill=PAL[17],width=1)
   d.line((x+8,y+8,x+14,y+18),fill=PAL[18],width=1)
 im.save(A/f'textures/entity/{name}.png')

def java(kind,nodes):
 lines=[]
 for n in nodes:
  def f(v):return str(round(v,4))+'F'
  p=','.join(f(x) for x in n['pivot']);rot=','.join(f(x) for x in n['rot']);b='null' if n['box'] is None else 'new float[]{'+','.join(f(x) for x in n['box'])+'}'
  lines.append(f'  add("{n["name"]}","{n["parent"]}",{p},{b},{n["mat"]},{rot});')
 return '\n'.join(lines)

def review(name,nodes):
 # Render the very same generated cuboid geometry from five independent angles.
 coords={};faces=[]
 def mat(rx,ry,rz):
  # Neutral reviewed pose; children rotations are included.
  import numpy as np
  cx,sx=math.cos(rx),math.sin(rx);cy,sy=math.cos(ry),math.sin(ry);cz,sz=math.cos(rz),math.sin(rz)
  return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
 import numpy as np
 for n in nodes:
  parent=coords.get(n['parent'],(np.eye(3),np.array([0.,0.,0.])));rot=parent[0]@mat(*n['rot']);pos=parent[1]+parent[0]@np.array(n['pivot']);coords[n['name']]=(rot,pos)
  if n['box']:
   x,y,z,w,h,d=n['box'];corn=[pos+rot@np.array([x+a*w,y+b*h,z+c*d]) for a,b,c in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]]
   for fi in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:faces.append(([corn[i] for i in fi],n['mat']))
 scale=5.5 if 'attack' in name else 10
 sheet=Image.new('RGB',(1600,640),'#20232A');dr=ImageDraw.Draw(sheet)
 for view,a in enumerate([0,.65,math.pi/2,math.pi,math.pi+.65]):
  trans=mat(-.07,a,0);polys=[]
  for corners,color in faces:
   vs=[trans@v for v in corners];polys.append((sum(v[2] for v in vs)/4,[(view*320+160+v[0]*scale,320+v[1]*scale) for v in vs],color))
  for depth,poly,color in sorted(polys,reverse=True):
   col=PAL[color] if color<len(PAL) else ('#6C2026' if color in [24,25] else '#C47C56');dr.polygon(poly,fill=col);dr.line(poly+[poly[0]],fill='#36312C',width=1)
  dr.text((view*320+12,12),['front','three-quarter','side','back','back three-quarter'][view],fill='white')
 sheet.save(OUT/(name+'-views.png'))

def generate():
 global N
 cases=[]
 for name,func in [('likho_one_eyed',likho),('tugarin_zmey',tugarin)]:
  N=[];func();nodes=list(N);(OUT/(name+'-geometry.json')).write_text(json.dumps(nodes,indent=2),'utf8');atlas(name);review(name,nodes);cases.append(java(name,nodes))
  attack=copy.deepcopy(nodes)
  for n in attack:
   if name=='likho_one_eyed':
    if n['name'] in ['armL','armR']:n['rot'][0]=-1.2
    if n['name'] in ['foreL','foreR']:n['rot'][0]=-.4
    if n['name'].startswith('finger'):n['rot'][0]=-.65
   else:
    if n['name']=='root':n['pivot'][0]=3.3;n['rot'][1]=.7
    if n['name'] in ['body','legL','legR']:n['pivot'][0]-=3.3
    if n['name']=='legR':n['rot'][2]=math.pi/2
    if n['name']=='armL':n['rot'][2]=-1.1
    if n['name']=='armR':n['rot'][2]=1.1
  review(name+'-attack',attack)
 (J/'WorldBossGeometry.java').write_text('package org.slavicmyths.client;\nfinal class WorldBossGeometry {\n static void build(WorldBossModel m,boolean likho){if(likho){\n'+cases[0].replace('  add(', '  m.add(')+'\n }else{\n'+cases[1].replace('  add(', '  m.add(')+'\n }}\n}', 'utf8')
if __name__=='__main__':generate()
