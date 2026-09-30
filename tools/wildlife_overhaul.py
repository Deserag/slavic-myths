"""Anatomical meshes with face-painted UV islands; deterministic wildlife art master."""
from pathlib import Path
import math,json
from generate_051 import Mesh,png,rgba,f
R=Path(__file__).resolve().parents[1]; A=R/'src/main/resources/assets/slavicmyths'; J=R/'src/main/java/org/slavicmyths/client'
PALETTES={
'bear':['35261f','59402d','765638','99714b'], 'cub':['493226','715039','97704d','b38a60'],
'wolf':['292d30','434a4d','626b6b','848c87'],'boar':['302a25','504437','6d5c47','8c785d'],
'stag':['473229','755039','9a704b','b28b60'],'doe':['614735','896647','af885f','c2a177'],
'muzzle':['685341','947c5e','bba17c','d5be97'],'dark':['191b1c','292b2b','3c3d39','525148'],
'chest':['686e69','92978a','b6b9a5','c9ccba'],'horn':['615341','938268','b4a48a','d1c4a8'],
'nose':['16191b','252a2c','353a39','535b58'],'eye':['16181a','211b14','a47b39','d3ad64']}
def construct(species):
 m=Mesh(); root=m.part('root',None,(0,24,0)); spine=m.part('spine',root,(0,0,0))
 bear=species=='bear';cub=species=='cub';wolf=species=='wolf';boar=species=='boar';deer=species in ('stag','doe');stag=species=='stag'
 # Shoulder height, hip height, rib width/depth, body length, leg length.
 h,hip,w,depth,length,leg={'bear':(23,20,14,13,24,11),'cub':(11,10,8,7,13,5.5),'wolf':(15,14,7,8,17,9),'boar':(15,12,10,9,19,6.5),'stag':(24,23,8,10,20,15),'doe':(21,21,7,8,18,14)}[species]
 zf=-length*.32;zb=length*.34
 chest=m.part('chest',spine,(0,-h+depth*.52,zf));m.box(chest,(-w/2,-depth*.52,-length*.24),(w,depth,length*.47),species)
 belly=m.part('belly',spine,(0,-hip+depth*.48,1));m.box(belly,(-w*.43,-depth*.42,-length*.24),(w*.86,depth*.76,length*.51),species)
 rump=m.part('rump',spine,(0,-hip+depth*.48,zb));m.box(rump,(-w*.41,-depth*.46,-length*.17),(w*.82,depth*.85,length*.35),species)
 if bear:
  m.box(chest,(-5.8,-8,-4),(11.6,4,9),'bear');m.box(belly,(-5.7,2,-5),(11.4,3,12),'bear')
 if wolf:m.box(chest,(-3.2,-2,-4.6),(6.4,7,2),'chest')
 if boar:
  for i in range(7):m.box(spine,(-.7,-h-1+i*.32,zf-3+i*2.4),(1.4,2.2-(i%2)*.6,2.8),'dark')
 # Neck slopes up in deer, projects horizontally in carnivores.
 neckpos=(0,-h+2,zf-length*.17)
 neck=m.part('neck',spine,neckpos,(.38 if deer else .1,0,0))
 if deer:
  nw=4.2 if stag else 3.2;m.box(neck,(-nw/2,-10,-2),(nw,12,5),species)
  head=m.part('head',neck,(0,-10,-1),(-.28,0,0)); skull=(3.6,4.2,6);snout=(2.5,2.7,4)
 elif bear or cub:
  nw=8 if bear else 5.8;m.box(neck,(-nw/2,-3,-2),(nw,6 if bear else 4,5),species)
  head=m.part('head',neck,(0,0,-3));skull=(7.8,6.4,7) if bear else (6.6,5.8,5);snout=(4.8,3.8,4.7) if bear else (3.6,2.6,2.5)
 elif wolf:
  m.box(neck,(-3,-4,-2),(6,7,6),'wolf');head=m.part('head',neck,(0,-1,-3));skull=(4.9,5,6);snout=(2.7,2.8,5)
 else:
  m.box(neck,(-4,-3,-2),(8,7,5),'boar');head=m.part('head',neck,(0,1,-2));skull=(6.6,6,7);snout=(3.9,3.6,5)
 sw,sh,sd=skull;m.box(head,(-sw/2,-sh*.6,-sd*.7),(sw,sh,sd),species)
 m.box(head,(-sw*.42,-sh*.66,-sd*.55),(sw*.84,sh*.3,sd*.72),species)
 muzzle=m.part('muzzle',head,(0,sh*.04,-sd*.68),(.07 if boar else 0,0,0));mw,mh,md=snout
 m.box(muzzle,(-mw/2,-mh*.5,-md),(mw,mh,md+.5),'muzzle' if bear or cub or wolf else species)
 m.box(muzzle,(-mw*.43,-mh*.45,-md-.5),(mw*.86,mh*.66,.8),'nose')
 jaw=m.part('jaw',head,(0,sh*.3,-sd*.3));m.box(jaw,(-mw*.46,0,-sd*.38-md+.7),(mw*.92,.9,sd*.4+md),'muzzle' if wolf else species)
 # Tiny inset eyes on the sides, warm irises for wolves only.
 for sign in (-1,1):
  m.box(head,(sign*(sw/2+.02)-.15,-sh*.27,-sd*.47),(.3,.6,.85),'eye' if wolf else 'nose')
  ear=m.part('ear'+('L' if sign==1 else 'R'),head,(sign*sw*.39,-sh*.65,-.2),(-.12,0,sign*(.6 if deer else .13)))
  ew,eh,ed=(1.8,1.8,1) if bear else (2,2,1.1) if cub else (1.8,3.1,1) if wolf else (2.2,2.5,1) if boar else (2,4.5,.8)
  m.box(ear,(-ew/2,-eh,-ed/2),(ew,eh,ed),species);m.box(ear,(-ew*.3,-eh+.4,-ed*.54),(ew*.6,eh*.65,.18),'muzzle')
  if wolf:m.box(ear,(-.45,-eh-.55,-.4),(.9,.7,.8),'wolf')
  if boar:
   tusk=m.part('tusk'+str(sign),muzzle,(sign*2,1,-3),(0,0,sign*.35));m.box(tusk,(-.4,-1,-.4),(.8,2.5,.8),'horn');m.box(tusk,(sign*.3-.25,-2,-.25),(.5,1.6,.5),'horn')
  if stag:
   a=m.part('antler'+str(sign),head,(sign*1.3,-2.8,0),(-.32,0,sign*.32));m.box(a,(-.5,-8,-.5),(1,8,1),'horn')
   branch=m.part('beam'+str(sign),a,(0,-7,0),(-.32,0,sign*.35));m.box(branch,(-.4,-5,-.4),(.8,6,.8),'horn')
   for i in range(3):
    tine=m.part('tine'+str(sign)+'_'+str(i),a,(0,-2-i*2.3,0),(-.65 if i%2==0 else .45,0,-sign*.35));m.box(tine,(-.3,-3.2-i*.3,-.3),(.6,3.6+i*.3,.6),'horn')
 # All legs have a shoulder/hip, lower segment and independent broad paw/cloven hoof.
 for front,z in ((True,zf),(False,zb)):
  for sign in (1,-1):
   n=('f' if front else 'b')+('l' if sign==1 else 'r');x=sign*(w*.37);L=leg+(0 if front else hip-h)
   upper=m.part(n,spine,(x,-L,z),(0,0,0));uw=4.5 if bear and front else 3.8 if bear else 2.8 if cub else 2.4 if boar else 1.9 if wolf else 1.65
   m.box(upper,(-uw/2,-2,-uw/2),(uw,L*.56+2,uw+(.5 if not front else 0)),species)
   lower=m.part(n+'Lower',upper,(0,L*.51,0),(.12 if front else -.24,0,0));lw=uw*.64;m.box(lower,(-lw/2,0,-lw/2),(lw,L*.49,lw),species if deer else 'dark')
   foot=m.part(n+'Foot',lower,(0,L*.43,-.3));pw=uw*(1 if deer else 1.05);pd=uw*(1.1 if deer else 1.55)
   m.box(foot,(-pw/2,0,-pd*.7),(pw,max(1,L*.12),pd),'dark')
   if bear:
    for c in (-1,0,1):m.box(foot,(c*.95-.22,.8,-pd*.7-.6),(.44,.5,.8),'nose')
 tail=m.part('tail',rump,(0,-1,length*.17),(-.7 if wolf else -.3,0,0))
 if wolf:
  m.box(tail,(-1.5,-1.3,0),(3,3,5),'wolf');tip=m.part('tailTip',tail,(0,0,4),(-.32,0,0));m.box(tip,(-1.9,-1.4,0),(3.8,3.4,5),'wolf');m.box(tip,(-1.2,-1,4),(2.4,2.5,2.6),'dark')
 else:m.box(tail,(-1,-1,0),(2,2,2 if bear or cub else 4),species if not deer else 'muzzle')
 return m

def emit(species,m):
 rows=[[(0,0,0,0)]*256 for _ in range(256)];u=v=rowh=0;lines=[]
 for name,p in m.parts.items():
  lines.append('part("%s",%s,new float[]{%s},new float[]{%s});'%(name,'null' if p['parent'] is None else '"'+p['parent']+'"',','.join(map(f,p['pos'])),','.join(map(f,p['rot']))))
  for b in p['boxes']:
   w,h,d=[math.ceil(t) for t in b['size']];tw=2*(w+d);th=h+d
   if u+tw>=256:u=0;v+=rowh+1;rowh=0
   assert v+th<256,(species,name,v)
   pal=list(map(rgba,PALETTES[b['mat']]))
   for yy in range(th):
    for xx in range(tw):rows[v+yy][u+xx]=pal[1]
   faces=[(u+d,v,w,d),(u+d+w,v,w,d),(u,v+d,d,h),(u+d,v+d,w,h),(u+d+w,v+d,d,h),(u+2*d+w,v+d,w,h)]
   for face,(fx,fy,fw,fh) in enumerate(faces):
    for yy in range(fh):
     for xx in range(fw):
      shade=1 if face in (1,4) else 2
      if yy>fh*.72:shade=max(0,shade-1)
      if face==0:shade=1 if b['mat'] in ('wolf','boar') else 2
      if fw>4 and fh>4 and ((xx//3+yy//4)%5==0):shade=max(0,shade-1)
      if yy==0 and face==3:shade=min(3,shade+1)
      rows[fy+yy][fx+xx]=pal[shade]
   lines.append('box("%s",%d,%d,new float[]{%s});'%(name,u,v,','.join(map(f,b['pos']+b['size']))))
   u+=tw+1;rowh=max(rowh,th)
 file={'bear':'brown_bear','cub':'bear_cub','wolf':'forest_wolf'}.get(species,species)
 png(A/f'textures/entity/{file}.png',rows)
 return '\n'.join(lines)

def generate():
 cases=[];geometry={}
 for s in ('bear','cub','wolf','boar','stag','doe'):
  m=construct(s);geometry[s]=m.parts;cases.append('case '+s.upper()+':\n'+emit(s,m)+'\nbreak;')
 (J/'WildlifeGeometry.java').write_text('''package org.slavicmyths.client;
import org.slavicmyths.entity.WildlifeEntity;
/** Generated anatomical master; edit tools/wildlife_overhaul.py. */
public final class WildlifeGeometry {
 public static void build(WildlifeModel m,WildlifeEntity.Kind kind){switch(kind){
'''+ '\n'.join(cases).replace('part(','m.part(').replace('box(','m.box(')+'''\n}}
 private WildlifeGeometry(){}
}
''',encoding='utf-8')
 out=R/'work/wildlife';out.mkdir(parents=True,exist_ok=True);(out/'geometry.json').write_text(json.dumps(geometry),encoding='utf-8')
if __name__=='__main__':generate()
