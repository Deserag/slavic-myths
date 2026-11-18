from pathlib import Path
import json,hashlib,math
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/2026-10-29-hell-remaining';ids=['navya_ostroga','ashen_greatsword','shadow_throwing_knife','volcanic_heart_flail','infernal_bow'];checks=0
atlas=Image.open(A/'textures/item/nether_weapon_materials.png');sheet=Image.new('RGB',(1000,750),'#ddd6c7');dr=ImageDraw.Draw(sheet)
for k,n in enumerate(ids):
 m=json.loads((A/'models/item'/f'{n+"_held" if k==3 else n}.json').read_text());els=m['elements'];assert len(els)>5
 for e in els:
  assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),(n,e);checks+=1
 recipe=json.loads((R/'src/main/resources/data/slavicmyths/recipe'/f'{n}.json').read_text());assert recipe['type']=='minecraft:smithing_transform' and recipe['template']['item']=='slavicmyths:nether_hardening_template';checks+=1
 # Four actual cuboid geometry views, painter-sorted. No original art comparison asserted.
 for row,angle in enumerate([0,math.pi/2,math.pi,math.pi/4]):
  faces=[]
  for e in els:
   lo,hi=e['from'],e['to'];verts=[(x,y,z) for x in [lo[0],hi[0]] for y in [lo[1],hi[1]] for z in [lo[2],hi[2]]]
   for indices in [[0,1,3,2],[4,6,7,5],[0,4,5,1],[2,3,7,6],[0,2,6,4],[1,5,7,3]]:
    points=[];depth=0
    for i in indices:
     x,y,z=verts[i];x-=8;z-=8;xx=x*math.cos(angle)-z*math.sin(angle);zz=x*math.sin(angle)+z*math.cos(angle);points.append((k*200+100+xx*3,row*180+130-y*3));depth+=zz
    uv=e['faces']['north']['uv'];color=atlas.getpixel((int(uv[0]),int(uv[1])))[:3];faces.append((depth,points,color))
  for _,points,col in sorted(faces):dr.polygon(points,fill=col,outline='#16171c')
 dr.text((k*200+5,725),n,fill='black');icon=Image.open(A/'textures/item'/f'{n}.png');sheet.paste(icon.resize((32,32)),(k*200+160,690),icon.resize((32,32)))
sheet.save(O/'weapons-source-four-views.png');report={'status':'SOURCE_PASS','checks':checks,'scope':'actual cuboid coordinates/limits,5 transform identities and four source views; actual animation/GUI/gameplay NOT_RUN; ART_BLOCKED_REFERENCE'};(O/'g2-source.json').write_text(json.dumps(report,indent=2)+'\n');print(report)
