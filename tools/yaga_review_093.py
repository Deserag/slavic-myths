"""Offline geometry/art review, no game launch. Reads the production model/plan export."""
from pathlib import Path
import json,re,sys
from PIL import Image,ImageDraw
import world_boss_models_092 as review
R=Path(__file__).resolve().parents[1];review.OUT=R/'docs/verification/yaga-0.9.3';review.OUT.mkdir(exist_ok=True)
review.PAL=['#b8a785','#ae9b54','#827a70','#3e3433','#6d382f','#614835','#c1b498','#71483b','#cebd94','#292420']
s=(R/'src/main/java/org/slavicmyths/client/BabaYagaModel.java').read_text('utf-8')
pat=r'a\("([^"\n]+)","([^"\n]*)",([^;\n]+)\);'
nodes=[]
for name,parent,rest in re.findall(pat,s):
 rest=rest.replace('F','');pre,tail=rest.split('null',1) if 'null' in rest else rest.split('new float[]{',1);pos=list(map(float,pre.strip(',').split(',')))
 if 'null' in rest:box=None;remaining=tail
 else:boxtext,remaining=tail.split('}',1);box=list(map(float,boxtext.split(',')))
 material,rx,ry,rz=remaining.strip(',').split(',');nodes.append(dict(name=name,parent=parent,pivot=pos,box=box,mat=int(material),rot=list(map(float,[rx,ry,rz]))))
assert len(nodes)>=75
review.review('baba_yaga',nodes);(review.OUT/'baba-yaga-geometry.json').write_text(json.dumps(nodes,indent=2),'utf-8')
items=['putevodny_klubok','svyazka_trav_yagi','otvar_ochishcheniya','letuchaya_maz','otvar_lesnoy_zorkosti','yagin_nastoy_stoykosti','otvar_bodrosti','lesnoy_nastoy_vosstanovleniya'];im=Image.new('RGBA',(1024,512),'#26231f');d=ImageDraw.Draw(im)
for i,id in enumerate(items):
 icon=Image.open(R/('src/main/resources/assets/slavicmyths/textures/item/'+id+'.png')).resize((200,200),Image.Resampling.NEAREST);x=(i%4)*256+28;y=(i//4)*256;im.alpha_composite(icon,(x,y));d.text((x-10,y+210),id,fill='#ead5a5')
im.save(review.OUT/'items.png')
print('Reviewed',len(nodes),'production model parts and 8 item sprites; offline only.')

plan_path=review.OUT/'hut-plan.json'
if plan_path.exists():
 import numpy as np,math
 plan=json.loads(plan_path.read_text('utf-8'));faces=[]
 def cube(x,y,z,lo,hi,color):
  pts=[np.array([x+(lo[0]+a*(hi[0]-lo[0]))/16,y+(lo[1]+b*(hi[1]-lo[1]))/16,z+(lo[2]+c*(hi[2]-lo[2]))/16]) for a,b,c in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]]
  for fi in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:faces.append(([pts[i] for i in fi],color,y,z))
 for cell in plan:
  x,y,z=cell['x'],cell['y'],cell['z'];id=cell['block'];base=id.split('[')[0]
  if base=='minecraft:air':continue
  props=dict(pair.split('=') for pair in id.split('[')[1].rstrip(']').split(',')) if '[' in id else {}
  if base=='slavicmyths:yaga_chicken_leg':
   m=json.loads((R/('src/main/resources/assets/slavicmyths/models/block/yaga_leg_'+props.get('part','0')+'.json')).read_text());angle={'north':0,'east':math.pi/2,'south':math.pi,'west':math.pi*1.5}[props.get('facing','north')]
   for e in m['elements']:
    # Rotation around the block center, including toes facing backward.
    before=len(faces);cube(x,y,z,e['from'],e['to'],'#373832' if props.get('part')=='4' else '#9d8e6e')
    for idx in range(before,len(faces)):
     points,col,yy,zz=faces[idx];center=np.array([x+.5,y,z+.5]);rot=np.array([[math.cos(angle),0,math.sin(angle)],[0,1,0],[-math.sin(angle),0,math.cos(angle)]]);faces[idx]=([center+rot@(pt-center) for pt in points],col,yy,zz)
  elif base.startswith('slavicmyths:yaga_'):
   model=R/('src/main/resources/assets/slavicmyths/models/block/'+base.split(':')[1]+'.json');m=json.loads(model.read_text());pal={'iron':'#3e4038','liquid':'#595d32','herbs':'#708050','binding':'#834031','bone':'#c6b690'}
   for e in m['elements']:cube(x,y,z,e['from'],e['to'],pal.get(e['faces']['north']['texture'].lstrip('#'),'#6c6655'))
  else:
   col='#4c3528' if 'dark_oak' in base else '#72533c' if 'spruce' in base else '#6d725b' if 'mossy' in base else '#6e7753' if 'dirt' in base or 'podzol' in base else '#858c76' if 'glass' in base else '#d2b676' if 'lantern' in base or 'candle' in base else '#8c6542'
   if 'fence' in base:cube(x,y,z,[6,0,6],[10,16,10],col);cube(x,y,z,[0,6,7],[16,9,9],col);cube(x,y,z,[0,12,7],[16,15,9],col)
   elif 'slab' in base:bottom=8 if props.get('type')=='top' else 0;cube(x,y,z,[0,bottom,0],[16,bottom+8,16],col)
   elif 'stairs' in base:
    cube(x,y,z,[0,0,0],[16,8,16],col);direction=props.get('facing','north');lo,hi=([0,8,0],[16,16,8]) if direction=='north' else ([0,8,8],[16,16,16]) if direction=='south' else ([8,8,0],[16,16,16]) if direction=='east' else ([0,8,0],[8,16,16]);cube(x,y,z,lo,hi,col)
   elif 'door' in base:cube(x,y,z,[0,0,0],[16,16,3],col)
   elif 'flower_pot' in base:cube(x,y,z,[5,0,5],[11,6,11],'#9c614a')
   elif 'lantern' in base or 'candle' in base:cube(x,y,z,[5,0,5],[11,10,11],col)
   elif 'vine' in base:cube(x,y,z,[0,0,0],[2,16,16],'#536044')
   elif 'wormwood' in base or 'mushroom' in base:cube(x,y,z,[4,0,4],[12,8,12],'#738354')
   else:cube(x,y,z,[0,0,0],[16,16,16],col)
 sheet=Image.new('RGB',(1600,850),'#242721');draw=ImageDraw.Draw(sheet)
 for view,(angle,cut) in enumerate([(.65,False),(.65,True)]):
  cy,sy=math.cos(angle),math.sin(angle);a=-.4;cx,sx=math.cos(a),math.sin(a);rot=np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]]);polys=[]
  for pts,col,y,z in faces:
   if cut and (y>=7 or z==-6 and y>=4):continue
   q=[rot@pt for pt in pts];polys.append((sum(pt[2] for pt in q)/4,[(view*800+400+pt[0]*24,570-pt[1]*24) for pt in q],col))
  for depth,poly,col in sorted(polys,reverse=True):draw.polygon(poly,fill=col);draw.line(poly+[poly[0]],fill='#38362d',width=1)
  draw.text((view*800+18,16),'Actual hut plan: '+('interior cutaway' if cut else 'exterior'),fill='white');draw.text((view*800+18,32),'Offline geometry projection; not a Minecraft screenshot',fill='#c8bea3')
 sheet.save(review.OUT/'hut-views.png')
