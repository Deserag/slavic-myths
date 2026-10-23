"""#77 original Skim anatomy from approved1.7.3 poster. Isolated deterministic model outputs."""
from pathlib import Path
from io import BytesIO
import math,json
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/skim-77/after'
PAL={'coat':['#4F3830','#211E1C','#80614B','#654234'],'mane':['#282122','#211E1C','#654234','#4F3830'],'warm':['#654234','#352624','#80614B','#4F3830'],'scar':['#872C2B','#5C2525','#D35B38','#982E2A'],'bone':['#D5C29E','#9B896A','#E0DBC0','#B3A17F'],'claw':['#D5C29E','#9B896A','#F0E2C4','#B6AA90'],'nose':['#211E1C','#171416','#4F3830','#322726'],'mouth':['#5A222B','#291A20','#873A3D','#702A31'],'eye':['#F4B73D','#B9492E','#FFD772','#F18436']}
def changed(p,b):
 p.parent.mkdir(parents=True,exist_ok=True)
 if not p.exists() or p.read_bytes()!=b:p.write_bytes(b)
def anatomy():
 parts=[]
 def node(name,parent,at,boxes=(),rotation=(0,0,0),mat='coat'):
  parts.append(dict(name=name,parent=parent,pivot=list(at),rotation=list(rotation),boxes=[dict(shape=list(b),material=mat) for b in boxes]));return parts[-1]
 def box(part,b,mat):part['boxes'].append(dict(shape=list(b),material=mat))
 body=node('body',None,[0,-9,0],[[-11,-5,-22.8,22,14,17.8],[-8,-9,-21,16,5,20],[-9,1,-22,18,10,20],[-8,-4,-7,16,12,23]])
 for sign in [-1,1]:
  node('ribs_'+('right' if sign<0 else 'left'),'body',[sign*10,-1,-14],[[-4,-5,-8,8,13,19]],(.08,0,-sign*.12))
 for i,z in enumerate([-6,1,8,15,22]):
  node('back_coat_'+str(i),'body',[(-1 if i%2 else 1)*1.8,-5+i*.4,z],[[-4.5,-2.5,-4,9,4,8],[-3,-1,2.5,6,4.8,5]],(.16,0,.04*(-1 if i%2 else 1)),mat='warm' if i%2 else 'coat')
 for sign in [-1,1]:
  for i,z in enumerate([0,11,23]):
   p=node('flank_coat_'+('right' if sign<0 else 'left')+str(i),'body',[sign*8,-3,z],[[-2,-1,-5,4,7.8,10],[-1.2,5.5,-2.5,2.4,4.5,6]],(.2,0,-sign*.22),mat='coat')
   if i!=1:box(p,[-2.05,.5,-3,.4,5,1.6],'scar')
 hips=node('hips','body',[0,4,17],[[-9,-7,-7,18,14,20],[-11,-4,-4,8,11,14],[3,-4,-4,8,11,14],[-7,-3,8,14,10,9]],(.10,0,0))
 neck=node('neck','body',[0,-2,-20],[[-9,-7.5,-9,18,15,17]])
 head=node('head','neck',[0,2,-9],[[-8.7,-8.5,-7,17.4,13,12],[-7,-7,-13,14,11,8],[-6.6,-.8,-20,13.2,6.2,11],[-5,-2,-21.5,10,3.6,3]])
 box(head,[-4.5,-1.6,-22,9,2.8,2],'nose')
 # Exposed slanted amber eyes, projecting brow, short ears integrated with the mane.
 for sign in [-1,1]:
  box(head,[sign*6.8-1,-4.3,-13.8,2,1.4,1],'eye')
  box(head,[sign*7.3-1.6,-5.6,-14,3.2,1.4,2.8],'mane')
  box(head,[sign*7.4-1.8,-9,-.5,3.6,4.8,4],'warm')
  # Swept outer tusks, not two straight rods. All three sections physically overlap.
  for i,(w,h,d) in enumerate([(2.8,4.4,3),(2.2,4.2,2.5),(1.4,3.5,1.8)]):box(head,[sign*(6.2+i*.25)-w/2,3.5+i*3.1,-20.2+i*.85,w,h,d],'bone')
  for z in [-15.5,-12,-8.5]:box(head,[sign*4.8-.6,4,z,1.2,3.3,1.4],'bone')
  box(head,[sign*4-1.1,-7.8,-8,2.2,1.4,7],'bone')
 jaw=node('jaw','head',[0,4,-2],[[-6.2,0,-17,12.4,4.4,15],[-4.9,2,-18,9.8,2.5,10]])
 box(jaw,[-5.3,-.3,-16.4,10.6,1,13.8],'mouth')
 for x in [-4.3,-2.6,-.9,.9,2.6,4.3]:box(jaw,[x-.55,-2.0,-16,1.1,2.3,1.3],'bone')
 for sign in [-1,1]:box(jaw,[sign*4.6-.75,-3.8,-12,1.5,4.4,2.0],'bone')
 # Three interleaved collars of heavy angular, tapering locks. Bone-colored edges follow poster.
 for layer in range(3):
  for sector in range(8):
   i=layer*8+sector;angle=2*math.pi*sector/8+(layer%2)*.22
   x=math.sin(angle)*(11.3+layer*.8);y=-math.cos(angle)*8.5+1.4;z=-9+layer*8
   length=14+(i%4)*1.3;w=6.4+(i%3)*.8;thick=8+(i%2)*1.1
   # Top locks rise only to the established ~3block silhouette, side locks hang below collar.
   top=-4.3 if math.cos(angle)>.45 else -3.0
   p=node('mane_'+str(i),'neck',[x,y,z],rotation=(.24+layer*.06,math.sin(angle)*.22,-math.sin(angle)*.32),mat='mane')
   box(p,[-w/2,top,-thick/2,w,length*.64,thick],'warm' if i%3==0 else 'mane')
   box(p,[-w*.32,top+length*.47,-thick*.35,w*.66,length*.38,thick*.76],'warm' if i%2 else 'coat')
   box(p,[-w*.14,top+length*.78,-thick*.20,w*.32,3.6,thick*.46],'bone' if i in [3,7,14,17,22] else 'scar' if i%4==0 else 'warm')
   if i in [1,8,17]:box(p,[-w*.25,top+.6,-thick*.25,w*.5,1.1,thick*.7],'bone')
 for side,sign in [('right',-1),('left',1)]:
  for front in [True,False]:
   leg=side+('_front' if front else '_back');upper=12.5 if front else 11;lower=10.5 if front else 11;w=8.8 if front else 9.6;d=10 if front else 10.5
   p=node(leg,'body' if front else 'hips',[sign*(13 if front else 10),8 if front else 3,-17 if front else 3],[[-w/2,-1,-d/2,w,8.5,d],[-w*.37,6,-d*.37,w*.74,upper-5,d*.74]])
   box(p,[-w*.47,1,-d*.53,w*.94,1.5,.8],'scar')
   node(leg+'_shin',leg,[0,upper,0],[[-3.5 if front else -2.9,-.3,-4.2 if front else -3.4,7 if front else 5.8,lower+.3,8.4 if front else 6.8]],mat='warm')
   paw=node(leg+'_paw',leg+'_shin',[0,lower,0],[[-5 if front else -4.4,-.5,-7,10 if front else 8.8,5.5 if front else 5.3,12]])
   for j in range(4):
    x=(-3.75+j*2.5) if front else (-3.3+j*2.2)
    box(paw,[x-.95,1.2,-8.5,1.9,3.8 if front else 3.6,4.5],'warm')
    box(paw,[x-.68,1.6,-11,1.36,3.1,4],'claw')
    box(paw,[x-.46,2.5,-12.4,.92,2.1,2.3],'bone')
 for i in range(5):
  w=5.2-i*.55;parent='hips' if i==0 else 'tail_'+str(i-1);p=node('tail_'+str(i),parent,[0,-2,13] if i==0 else [0,.2,6.1],rotation=([-.36,-.22,.12,.45,.55][i],0,0))
  box(p,[-w/2,-w*.42,-.6,w,w*.9,7.4],'coat');box(p,[-w*.51,-w*.44,1.2,w*1.02,1.0,4.8],'scar')
  if i in [1,3,4]:box(p,[-w*.46,-w*.57,2.8,w*.92,1.1,2.2],'bone')
  if i==4:box(p,[-2.7,-2.2,4.7,5.4,4.5,5.4],'mane');box(p,[-1.5,-2.5,7.2,3,2.1,3.4],'bone')
 return parts

def generate():
 parts=anatomy();atlas=Image.new('RGBA',(512,512));draw=ImageDraw.Draw(atlas);cx=cy=row=0;rects=[]
 for p in parts:
  for i,b in enumerate(p['boxes']):
   x,y,z,w,h,d=b['shape'];rw=2*(math.ceil(w)+math.ceil(d))+6;rh=math.ceil(h)+math.ceil(d)+6
   if cx+rw>512:cx=0;cy+=row;row=0
   if cy+rh>512:raise ValueError('Skim atlas overflow')
   u,v=cx+3,cy+3;b['uv']=[u,v];rects.append([cx,cy,rw,rh]);base,dark,light,warm=PAL[b['material']]
   draw.rectangle((cx,cy,cx+rw-1,cy+rh-1),fill=base)
   if b['material'] not in ['eye','mouth','bone','claw','scar']:
    # Four broad purposeful shade bands, stepped trailing clusters, no noise/fuzz overlay.
    for fx in [u,u+d,u+2*d+w]:
     draw.rectangle((fx,v+d,min(cx+rw-1,fx+max(1,w*.25)),min(cy+rh-1,v+d+h)),fill=dark)
     draw.rectangle((fx+1,v+d+1,min(cx+rw-1,fx+max(2,w*.3)),min(cy+rh-1,v+d+max(2,h*.3))),fill=light)
     draw.rectangle((fx+max(1,w*.4),v+d+max(2,h*.25),min(cx+rw-1,fx+max(2,w*.7)),min(cy+rh-1,v+d+max(3,h*.68))),fill=warm)
   if p['name'].startswith(('mane_','back_coat_','flank_coat_')):
    for fx,fw in [(u,d),(u+d,w),(u+d+w,d),(u+2*d+w,w)]:
     draw.polygon([(fx+.3,v+d+1),(fx+fw*.75,v+d+1),(fx+fw*.56,v+d+h*.55),(fx+fw*.33,v+d+h*.78)],fill=PAL['warm'][0])
   if p['name'] in ['body','hips']:
    # Rusty scars on side nets, small in area, no extruded scar plates.
    for sx in [u,u+d+w]:
     for j in range(3):draw.line((sx+2+j*2,v+d+2+j,sx+3+j*2,v+d+6+j),fill=PAL['scar'][0],width=2)
   if b['material'] in ['bone','claw']:
    draw.line((u,v+d+1,cx+rw-2,v+d+1),fill=light,width=1);draw.line((u,v+d+h-1,cx+rw-2,v+d+h-1),fill=dark,width=1)
   if b['material']=='eye':
    draw.rectangle((u,v,cx+rw-2,cy+rh-2),fill=base);draw.point((round(u+d+w*.55),round(v+d+h*.5)),fill=PAL['eye'][1])
   cx+=rw;row=max(row,rh)
 eyes=Image.new('RGBA',(512,512));rage=Image.new('RGBA',(512,512));i=0
 for p in parts:
  for b in p['boxes']:
   x,y,w,h=rects[i];i+=1
   if b['material']=='eye':eyes.paste(atlas.crop((x,y,x+w,y+h)),(x,y))
 red_colors={tuple(bytes.fromhex(c[1:])) for c in PAL['scar']}
 for y in range(512):
  for x in range(512):
   pixel=atlas.getpixel((x,y))
   if pixel[3] and pixel[:3] in red_colors:rage.putpixel((x,y),pixel)
 for name,image in [('skim_beast.png',atlas),('skim_beast_eyes.png',eyes),('skim_beast_rage.png',rage)]:
  stream=BytesIO();image.save(stream,format='PNG');changed(A/'textures/entity'/name,stream.getvalue())
 def f(n):return f'{float(n):g}F'
 lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','/** Generated by tools/skim_77_model.py. Original Skim anatomy from approved1.7.3 poster. */','public final class SkimMesh { public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();']
 for p in parts:
  cubes='CubeListBuilder.create()'
  for b in p['boxes']:cubes+=f'.texOffs({b["uv"][0]},{b["uv"][1]}).addBox('+','.join(map(f,b['shape']))+')'
  at=p['pivot']+p['rotation'];lines.append(f'var {p["name"]}={(p["parent"] or "root")}.addOrReplaceChild("{p["name"]}",{cubes},PartPose.offsetAndRotation('+','.join(map(f,at))+'));')
 lines+=['return LayerDefinition.create(mesh,512,512).bakeRoot();}','static java.util.List<String> names(){return java.util.List.of('+','.join('"'+p['name']+'"' for p in parts)+');}','static java.util.Map<String,String> parents(){return java.util.Map.ofEntries('+','.join('java.util.Map.entry("'+p['name']+'","'+(p['parent'] or 'root')+'")' for p in parts)+');}','record Shape(String name,float[] box){}','private static final Shape[] BOUNDS=new Shape[]{'+','.join('new Shape("'+p['name']+'",new float[]{'+','.join(map(f,b['shape']))+'})' for p in parts for b in p['boxes'])+'};','static Shape[] bounds(){return BOUNDS;}','/** Actual authored paw-pad maximum Y, used by the production support solver. */','static float sole(String leg){return leg.endsWith("front")?'+f(max(b['shape'][1]+b['shape'][4] for p in parts if p['name']=='right_front_paw' for b in p['boxes']))+':'+f(max(b['shape'][1]+b['shape'][4] for p in parts if p['name']=='right_back_paw' for b in p['boxes']))+';}','static float frontPawReach(){return '+f(-min(b['shape'][2] for p in parts if p['name']=='right_front_paw' for b in p['boxes']))+';}','private SkimMesh(){}','}']
 changed(R/'src/main/java/org/slavicmyths/client/SkimMesh.java',('\n'.join(lines)+'\n').encode('utf-8'));changed(O/'authored-rig.json',(json.dumps(dict(parts=parts,atlas=[512,512],rectangles=rects),indent=2)+'\n').encode('utf-8'))
 from trophy140_skim_parts import source as trophy_source
 changed(R/'src/main/java/org/slavicmyths/client/TrophySkimMesh.java',trophy_source(parts).encode('utf-8'))
 print('SKIM77 model-only:',len(parts),'bones',len(rects),'cuboids; SkimMesh,3Skim PNGs,authoring JSON and trophy-only articulation')
 return parts
if __name__=='__main__':generate()
