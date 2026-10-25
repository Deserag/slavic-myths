"""Offline nearest-pixel rendering of actual block JSON/PNG, never a game screenshot."""
from pathlib import Path
import json
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths'
report=ROOT/'docs/implementation/navya-grove'
names=json.loads((report/'asset-manifest.json').read_text())['geology']
canvas=Image.new('RGB',(640,444),'#202128');draw=ImageDraw.Draw(canvas)
draw.text((10,4),'ACTUAL BLOCK JSON + PNG / OFFLINE / NOT MINECRAFT',fill='#e6dfd1')
for index,name in enumerate(names):
 data=json.loads((A/'models/block'/(name+'.json')).read_text());textures=data['textures']
 parts=data.get('elements',[{'from':[0,0,0],'to':[16,16,16]}])
 ox=80+(index%4)*160;oy=135+(index//4)*210
 def project(point):
  x,y,z=point;return ox+(x-z)*3,oy+(x+z)*1.5-y*3
 for part in sorted(parts,key=lambda p:sum(p['from'])+sum(p['to'])):
  x0,y0,z0=part['from'];x1,y1,z1=part['to']
  for face,shade in [('south',.65),('east',.82),('up',1.)]:
   ref=part.get('faces',{}).get(face,{}).get('texture')
   if not ref:ref='#'+('top' if face=='up' and 'top' in textures else 'end' if face=='up' and 'end' in textures else 'side' if 'side' in textures else 'all')
   while ref.startswith('#'):ref=textures[ref[1:]]
   tex=Image.open(A/'textures'/(ref.split(':')[1]+'.png')).convert('RGB')
   def point(u,v):
    if face=='up':return (x0+(x1-x0)*u/16,y1,z0+(z1-z0)*v/16)
    if face=='east':return (x1,y1-(y1-y0)*v/16,z0+(z1-z0)*u/16)
    return (x0+(x1-x0)*u/16,y1-(y1-y0)*v/16,z1)
   for v in range(16):
    for u in range(16):
     color=tuple(int(c*shade) for c in tex.getpixel((u,v)))
     draw.polygon([project(point(u,v)),project(point(u+1,v)),project(point(u+1,v+1)),project(point(u,v+1))],fill=color)
 draw.text(((index%4)*160+4,(index//4)*210+195),name,fill='#e6dfd1')
canvas.save(report/'actual-block-models.png')
# Tile tests use the same production PNG, with no smoothing.
tiles=Image.new('RGB',(640,340),'#202128');td=ImageDraw.Draw(tiles)
for i,name in enumerate(names):
 texture=Image.open(A/'textures/block'/(name+'.png')).convert('RGB')
 for x in range(5):
  for y in range(5):tiles.paste(texture.resize((24,24),Image.Resampling.NEAREST),((i%4)*160+x*24+20,(i//4)*165+y*24+10))
 td.text(((i%4)*160+3,(i//4)*165+139),name,fill='#e6dfd1')
tiles.save(report/'actual-texture-tiles.png')
print('Actual source previews written; no game/renderer runtime claimed')
