"""#76 original cursed-werewolf anatomy, only four deterministic model outputs.
No quest, other wolf, sound, item or registration writes. All units are model px.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
from io import BytesIO
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/slavicmyths'

def write_changed(path,data):
 if not path.exists() or path.read_bytes()!=data:path.write_bytes(data)

def authored_parts():
 parts=[]
 def part(n,p,xyz,boxes=(),r=(0,0,0),mat='fur'):
  parts.append(dict(name=n,parent=p,pos=list(xyz),rotation=list(r),boxes=[dict(shape=list(b),material=mat) for b in boxes]))
 part('root',None,[0,24,0])
 part('pelvis','root',[0,-13,1.5],[[-4.5,-3.5,-4.5,9,7,9]])
 part('spine','pelvis',[0,-1,-4],[[-3.75,-3,-5,7.5,6,10]],(.17,0,0))
 # Real new barrel: narrow waist, separate ribcage, angled breast and tucked lower keel.
 part('chest','spine',[0,-1,-5],[[-6,-5,-4,12,8,8],[-5,-4,-6,10,9,5],[-4,2,-4,8,3,7]])
 part('neck','chest',[0,-2.5,-4.2],[[-3.3,-3,-5.3,6.6,6.5,7]])
 part('head','neck',[0,.8,-4.2],[[-4.4,-4.1,-1.2,8.8,7.6,4.7],[-3.5,-3.9,-5,7,7.1,4.5]])
 part('muzzle','head',[0,1.1,-3.6],[[-2.65,-1.6,-6.4,5.3,3.2,7]],mat='muzzle')
 part('nose','muzzle',[0,-.2,-6.0],[[-2.1,-.7,-.85,4.2,1.8,1.4]],mat='nose')
 part('jaw','head',[0,2.4,-3.5],[[-2.35,0,-6.3,4.7,1.7,6.5]],mat='jaw')
 for side,sign in [('L',-1),('R',1)]:
  part('ear'+side,'head',[sign*3.1,-4,-.2],[[-1.3,-4.1,-1.1,2.6,4.4,2.2],[-.75,-5.4,-.8,1.5,1.7,1.6]],(0,0,sign*.17))
  part('innerEar'+side,'ear'+side,[0,-.1,-1.13],[[-.65,-3,.0,1.3,2.6,.12]],mat='ear')
  part('brow'+side,'head',[sign*3.0,-1.75,-5.05],[[-1.3,-.55,-.4,2.6,1.1,1.4]],(0,0,sign*.1))
  part('eye'+side,'head',[sign*3.3,-.8,-5.06],[[-.7,-.4,-.22,1.4,.8,.4]],mat='eye')
  # Back-facing elbow, shorter lean forelimb, broad but articulated weight-bearing paw.
  part('upper'+side,'chest',[sign*6,-2.5,-2.4],[[-2.45,-.7,-2.5,4.9,6.4,5],[-1.9,5,-1.9,3.8,5.5,3.8]],(.65,0,0))
  part('fore'+side,'upper'+side,[0,10.5,0],[[-1.85,-.3,-2,3.7,9.8,4]],(-1.3,0,0))
  part('hand'+side,'fore'+side,[0,9.5,0],[[-2.6,-.6,-4,5.2,3.6,6.0]],(.65,0,0))
  for i in range(4):
   part(f'claw{side}{i}','hand'+side,[-1.8+i*1.2,.8,-3.6],[[-.42,0,-2.2,.84,1.1,2.5]],mat='claw')
   part(f'hook{side}{i}',f'claw{side}{i}',[0,.3,-2.2],[[-.36,0,-.9,.72,1.3,1.2]],(.2,0,0),mat='clawtip')
  part('thigh'+side,'pelvis',[sign*3.7,.7,1.1],[[-2.6,-.6,-2.7,5.2,7.7,5.4],[-1.9,6.3,-2.2,3.8,3.8,4.4]],(1.25,0,0))
  part('shin'+side,'thigh'+side,[0,10,0],[[-1.7,-.3,-1.9,3.4,10.8,3.8]],(-2.15,0,0))
  part('foot'+side,'shin'+side,[0,10.5,0],[[-2.6,-.5,-4.3,5.2,3.5,6.6]],(.9,0,0))
  for i in range(3):part(f'toe{side}{i}','foot'+side,[-1.5+i*1.5,1,-4.3],[[-.42,0,-1.4,.84,1.1,1.6]],mat='clawtip')
  part('cheekRuff'+side,'head',[sign*3.8,1.7,.4],[[-1,-1.8,-2.2,2,4.5,4]],(0,0,sign*.12))
  part('scapula'+side,'chest',[sign*4,-4,1],[[-1.8,-1.2,-3,3.6,4,6]],(.17,0,sign*.1))
  part('fangUpper'+side,'muzzle',[sign*1.8,1.4,-4.7],[[-.45,0,-.45,.9,2.1,.9]],mat='tooth')
  part('fangLower'+side,'jaw',[sign*1.6,.1,-4.4],[[-.35,-1.2,-.35,.7,1.3,.7]],mat='tooth')
  part('toothSide'+side,'muzzle',[sign*2.3,1.4,-2],[[-.3,0,-.3,.6,1.2,.6]],mat='tooth')
 part('tail1','pelvis',[0,-.3,4.0],[[-1.9,-1.7,0,3.8,3.4,5]],(-.32,0,0))
 part('tail2','tail1',[0,1.1,4.3],[[-1.55,-1.4,0,3.1,3,5]],(-.23,0,0))
 part('tail3','tail2',[0,1,4.2],[[-1.1,-1.0,0,2.2,2.4,4]],(-.14,0,0))
 part('tailTip','tail3',[0,1,3.2],[[-.7,-.6,0,1.4,1.7,2]],(-.12,0,0))
 part('napeRidge','neck',[0,-3.1,1.2],[[-2.9,-1.2,-3,5.8,2.5,5.5]],(.12,0,0))
 part('furBack','spine',[0,-2.7,0],[[-2.8,-.8,-2.4,5.6,1.6,5.5]],(.08,0,0))
 # Only existing scar locations; close to the replacement body surface, no floating masks.
 part('scarArm','upperL',[-2.46,1.2,-1],[[-.08,0,-.35,.16,3.8,.7]],mat='scar')
 part('scarRibs','chest',[6.01,-.5,1],[[-.06,-2,-.35,.12,3.2,.7]],mat='scar')
 return parts

PALETTES={
 'fur':('#353137','#48434a','#51494d'), 'bib':('#a99d8c','#c4b9a4','#958a7e'),
 'muzzle':('#746b64','#8b8071','#625c57'), 'jaw':('#b6aa95','#cbbfa9','#978d7f'),
 'nose':('#171719','#29272b','#363035'), 'ear':('#6c4b49','#805b53','#543a3c'),
 'eye':('#c28c3d','#dfac59','#ffe09c'),'claw':('#393539','#514b49','#6b6256'),
 'clawtip':('#b1a38b','#c7b99e','#8d8374'),'tooth':('#ddd1b8','#f0e5cb','#b9ab95'),
 'scar':('#74514b','#8a5d53','#5b4040')}

def generate():
 parts=authored_parts();atlas=Image.new('RGBA',(512,512));eyes=Image.new('RGBA',(512,512));draw=ImageDraw.Draw(atlas);ed=ImageDraw.Draw(eyes);rectangles=[];cx=cy=row=0
 for p in parts:
  for b in p['boxes']:
   x,y,z,w,h,d=b['shape'];rw=2*(math.ceil(w)+math.ceil(d))+4;rh=math.ceil(h)+math.ceil(d)+4
   if cx+rw>512:cx=0;cy+=row;row=0
   if cy+rh>512:raise ValueError('UV overflow')
   u,v=cx+2,cy+2;b['uv']=[u,v];rectangles.append([cx,cy,rw,rh]);base,light,dark=PALETTES[b['material']]
   draw.rectangle((cx,cy,cx+rw-1,cy+rh-1),fill=base)
   # Broad coherent fur clusters and face edges, no random gray/noise overlay.
   if b['material'] not in ('eye','nose','scar','tooth','clawtip'):
    for px,py in [(u+math.ceil(d)+1,v+math.ceil(d)+1),(u+math.ceil(d)+max(1,math.ceil(w)//2),v+math.ceil(d)+max(1,math.ceil(h)//2))]:
     draw.rectangle((px,py,min(cx+rw-2,px+2),min(cy+rh-2,py+2)),fill=light)
    draw.line((u,v+math.ceil(d),cx+rw-2,v+math.ceil(d)),fill=dark)
   if p['name']=='chest':
    frontx=u+d;fronty=v+d
    draw.polygon([(frontx+w*.2,fronty+h*.3),(frontx+w*.8,fronty+h*.3),(frontx+w*.5,fronty+h*.95)],fill=PALETTES['bib'][0])
    # Actual y-max ModelPart face becomes the frontal breast in upright stance.
    bx=u+d+w
    draw.polygon([(bx+w*.18,v+.5),(bx+w*.82,v+.5),(bx+w*.5,v+d*.9)],fill=PALETTES['bib'][0])
   if b['material']=='eye':
    draw.rectangle((cx,cy,cx+rw-1,cy+rh-1),fill=base)
    ed.rectangle((cx,cy,cx+rw-1,cy+rh-1),fill=('#9c6f31'))
   cx+=rw;row=max(row,rh)
 for name,image in [('cursed_volkolak.png',atlas),('cursed_volkolak_eyes.png',eyes)]:
  buffer=BytesIO();image.save(buffer,format='PNG');write_changed(A/'textures/entity'/name,buffer.getvalue())
 data=dict(variant='C76',atlas=[512,512],unit='1/16 block',parts=parts,uv_rectangles=rectangles)
 write_changed(A/'cursed_volkolak_geometry.json',(json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))
 def arr(a):return 'new float[]{'+','.join(f'{float(n):g}F' for n in a)+'}'
 lines=['package org.slavicmyths.client;','/** Generated from tools/cursed_volkolak_76_model.py via volkolak_143_resources.py --model-only. */','final class CursedVolkolakGeometry {','static void build(CursedVolkolakModel m){']
 for p in parts:
  parent='null' if p['parent'] is None else '"'+p['parent']+'"';lines.append(f'm.part("{p["name"]}",{parent},{arr(p["pos"])},{arr(p["rotation"])});')
  for b in p['boxes']:lines.append(f'm.box("{p["name"]}",{b["uv"][0]},{b["uv"][1]},{arr(b["shape"])});')
 lines+=['}','private CursedVolkolakGeometry(){}','}'];write_changed(ROOT/'src/main/java/org/slavicmyths/client/CursedVolkolakGeometry.java',('\n'.join(lines)+'\n').encode('utf-8'))
 return parts
if __name__=='__main__':generate()
