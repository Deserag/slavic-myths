"""Ritual cache closed inventory/hand model; placed chest keeps its vanilla lid/renderer.
Authored 32px pixel texture and exact JSON cuboids, no builtin entity item dependency.
"""
from pathlib import Path
import json
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]
def texture():
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 # Front, side, lid, brass are separate16px atlas quadrants.
 for ox,oy in [(0,0),(16,0),(0,16)]:
  d.rectangle((ox,oy,ox+15,oy+15),fill='#614531')
  for y in [1,5,9,13]:
   d.line((ox+1,oy+y,ox+14,oy+y),fill='#94704a')
   d.line((ox+1,oy+y+2,ox+14,oy+y+2),fill='#382d26')
  for x in [2,12]:
   d.rectangle((ox+x,oy,ox+x+1,oy+15),fill='#364749')
   d.line((ox+x,oy,ox+x,oy+15),fill='#70807a')
   for y in [2,12]:d.point((ox+x,oy+y),fill='#bfaa75')
 # Front ritual seal: enclosed green/amber engraving with a clear clasp.
 d.rectangle((6,4,10,10),fill='#252e2d');d.line([(8,4),(10,7),(8,10),(6,7),(8,4)],fill='#c5aa62')
 d.line((8,5,8,9),fill='#9ccd99');d.rectangle((7,1,9,3),fill='#c5aa62')
 d.rectangle((16,16,31,31),fill='#3d5050');d.rectangle((17,17,30,30),fill='#89958a');d.rectangle((19,19,28,28),fill='#6b7770');d.line((19,19,28,19),fill='#c0b789')
 return im

def model():
 def box(a,b,front=(0,0,8,8),side=(8,0,16,8),top=(0,8,8,16)):
  return {'from':a,'to':b,'faces':{f:{'uv':list(front if f=='north' else top if f in ('up','down') else side),'texture':'#cache'} for f in ['north','south','east','west','up','down']}}
 return {'parent':'minecraft:block/block','textures':{'cache':'slavicmyths:item/sanctuary_ritual_cache','particle':'slavicmyths:item/sanctuary_ritual_cache'},'elements':[
  box([1,0,1],[15,9,15]),box([1,9.5,1],[15,14,15]),box([7,7,0],[9,11,1],(8,8,16,16),(8,8,16,16),(8,8,16,16))],
  'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]},'ground':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.25,.25,.25]},'fixed':{'rotation':[0,180,0],'scale':[.5,.5,.5]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.375,.375,.375]},'firstperson_righthand':{'rotation':[0,45,0],'scale':[.4,.4,.4]},'firstperson_lefthand':{'rotation':[0,225,0],'scale':[.4,.4,.4]}}}

def apply(root=R):
 base=Path(root)/'src/main/resources/assets/slavicmyths';p=base/'textures/item/sanctuary_ritual_cache.png';p.parent.mkdir(parents=True,exist_ok=True);texture().save(p)
 p=base/'models/item/sanctuary_ritual_cache.json';p.write_text(json.dumps(model(),indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':apply()
