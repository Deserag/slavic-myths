"""Complete 24 craft rune + 4 blank art family. No gameplay or future V outputs.

Native 32/64px integer geometry; frozen authored SVG glyph pixels are source data,
not a screenshot or production copy of a concept card. Four pilot identities stay.
"""
from pathlib import Path
import json
from PIL import Image,ImageDraw
import runes102_pilot as pilot
ROOT=Path(__file__).resolve().parents[1]
SPEC=json.loads(Path(__file__).with_name('runes102_spec.json').read_text(encoding='utf-8'))
BY_ID={r['id']:r for r in SPEC}
IDS=tuple(BY_ID)
def plate(tier,size):
 if tier>=3:
  im=pilot.frame(tier==4)
  if size==64:return im
  # Render native32 by exact sampling of the integer frame, no interpolation.
  small=Image.new('RGBA',(32,32))
  for y in range(32):
   for x in range(32):small.putpixel((x,y),im.getpixel((x*2,y*2)))
  return small
 im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
 d.polygon([(3,1),(28,1),(30,3),(30,28),(28,30),(3,30),(1,28),(1,3)],fill='#151b24')
 d.polygon([(4,3),(27,3),(28,4),(28,27),(27,28),(4,28),(3,27),(3,4)],fill='#586574' if tier==1 else '#788e9f')
 d.line([(4,26),(4,4),(26,4)],fill='#9ba8b0' if tier==1 else '#b0c9d3')
 d.line([(27,5),(27,27),(5,27)],fill='#303c49')
 d.rectangle((6,6,25,25),fill='#202a34');d.rectangle((7,7,24,24),fill='#2c3742')
 if tier==1:
  # A few deliberate cut facets, never a noisy whole-surface texture.
  d.line([(7,7),(11,7)],fill='#45505b');d.line([(23,20),(23,23),(20,23)],fill='#192530')
 else:
  d.rectangle((5,5,26,26),outline='#56758a')
  for x,y in [(4,4),(24,4),(4,24),(24,24)]:
   d.rectangle((x,y,x+3,y+3),fill='#587c90');d.rectangle((x,y,x+1,y+1),fill='#a7d2da')
 return im

def tool_image(id,tier):
 # Shared topology for the professional pairs. Pilot hoe/axe stay byte-identical.
 logical=id.removeprefix('rune_')
 if logical in pilot.IDS:return pilot.image(logical)
 im=plate(tier,64);d=ImageDraw.Draw(im);family=logical.rsplit('_',1)[0]
 if family=='miner':
  d.line([(22,47),(40,24)],fill='#604627',width=8)
  d.line([(22,46),(40,24)],fill='#bd9654',width=5)
  d.line([(23,45),(40,24)],fill='#ead193',width=2)
  d.polygon([(20,18),(28,16),(36,19),(44,25),(47,34),(43,32),(39,25),(31,22),(24,22),(20,24)],fill='#cb974c')
  d.line([(21,18),(28,17),(35,20),(42,25)],fill='#f0ce7d',width=2)
  d.line([(21,23),(24,21),(31,21)],fill='#80532e')
 else:
  d.rectangle((29,21,34,37),fill='#ab7549');d.line([(30,24),(30,36)],fill='#d4ab70',width=2)
  d.rectangle((27,16,36,24),outline='#d3a56c',width=3)
  d.polygon([(24,33),(39,33),(39,41),(32,49),(24,41)],fill='#ad764e')
  d.line([(25,34),(37,34)],fill='#e0b37b',width=2)
  d.line([(26,38),(26,41),(32,46)],fill='#d4a16a',width=2)
  d.line([(35,37),(35,41),(32,45)],fill='#70492f')
 if tier==4:
  # Accent only; tool shape and thematic material remain stable.
  d.rectangle((45,43,46,44),fill='#dfbb70')
 return im

def image(id):
 spec=BY_ID[id];tier=spec['tier'];size=spec['size'];scale=size//16
 if any(id.startswith('rune_'+s+'_') for s in ['miner','excavator','farmer','lumberjack']):return tool_image(id,tier)
 im=plate(tier,size);d=ImageDraw.Draw(im)
 if id.startswith('blank_rune_'):
  # Empty recessed socket, never an effect symbol or bright ready-rune glyph.
  a,b=5*scale,11*scale-1;d.rectangle((a,a,b,b),fill='#14212c',outline='#4a5965',width=max(1,scale//2))
  d.line([(a+scale,b-scale),(a+scale,a+scale),(b-scale,a+scale)],fill='#263642',width=max(1,scale//2))
  d.line([(a+scale,b),(b,b),(b,a+scale)],fill='#73838e',width=1)
  return im
 # Larger native64 pictographs clarify the meaning of the small schematic glyphs.
 if id=='rune_counterstrike_iii':
  d.polygon([(18,20),(34,20),(34,34),(26,45),(18,34)],fill='#c46b4d')
  d.line([(19,21),(32,21),(32,33),(26,41),(21,34)],fill='#ffb083',width=3)
  d.line([(40,42),(47,35),(47,22),(40,17),(33,20)],fill='#f3a57a',width=3)
  d.polygon([(32,20),(39,20),(35,25)],fill='#ffd0a3')
  return im
 if id=='rune_hunter_iii':
  d.line([(22,16),(29,23),(32,32),(29,41),(22,48)],fill='#e2ba68',width=4)
  d.line([(22,16),(22,48)],fill='#9e8250',width=2)
  d.line([(20,32),(44,32)],fill='#f5d891',width=3)
  d.line([(39,27),(44,32),(39,37)],fill='#f5d891',width=3)
  d.rectangle((44,28,47,36),outline='#a48b52')
  return im
 if id=='rune_last_stand_iii':
  d.line([(18,18),(32,14),(46,18),(43,38),(32,49),(21,38),(18,18)],fill='#b47a8e',width=3)
  d.polygon([(23,23),(28,23),(32,27),(36,23),(41,23),(43,29),(32,40),(21,29)],fill='#da7b91')
  d.line([(24,24),(27,24)],fill='#f4b2bf',width=2)
  return im
 if id=='rune_depths_iii':
  for y,x1,x2 in [(19,17,46),(28,20,43),(37,17,40)]:
   d.line([(x1,y),(x2,y)],fill='#70becb',width=4)
   d.line([(x1,y-1),(x2-3,y-1)],fill='#b4e0dc')
  d.line([(45,29),(45,45)],fill='#9ee2e1',width=3)
  d.polygon([(40,41),(50,41),(45,49)],fill='#9ee2e1')
  return im
 if id=='rune_unbreakable_iv':
  d.line([(17,19),(32,15),(47,19),(44,36),(32,49),(20,36),(17,19)],fill='#b99ae5',width=3)
  d.line([(24,24),(32,22),(40,24),(38,34),(32,41),(26,34),(24,24)],fill='#dcc8fb',width=4)
  return im
 if id=='rune_perun_rage_iv':
  d.polygon([(35,15),(23,32),(30,32),(25,49),(43,27),(34,27),(40,15)],fill='#65b8f3')
  d.line([(35,17),(27,29)],fill='#b6e5ff',width=2)
  d.rectangle((17,18,26,23),fill='#6b9cc0');d.line([(22,24),(19,34)],fill='#91bddc',width=3)
  return im
 if id=='rune_forest_spirit_iv':
  d.line([(32,43),(32,18)],fill='#83cc9c',width=5)
  for pts in [[(32,30),(23,24),(20,18)],[(32,34),(42,27),(46,20)],[(32,24),(39,18)]]:
   d.line(pts,fill='#83cc9c',width=4)
  for x,y in [(17,16),(22,23),(39,16),(44,19),(40,27)]:d.rectangle((x,y,x+3,y+3),fill='#aedb9e')
  d.line([(22,48),(32,42),(42,48)],fill='#60a77e',width=3)
  return im
 if id=='rune_mastery_iv':
  d.polygon([(17,22),(46,22),(46,26),(39,30),(28,30),(23,27),(17,26)],fill='#e6c47a')
  d.rectangle((28,30,37,39),fill='#b58d4f')
  d.polygon([(28,38),(37,38),(42,44),(24,44)],fill='#e6c47a')
  d.line([(18,22),(44,22)],fill='#ffe0a0',width=2)
  return im
 # Draw the retained central theme in integer logical cells, keeping frame free.
 for x,y,c in spec['glyph']:
  d.rectangle((x*scale+1,y*scale+1,(x+1)*scale,(y+1)*scale),fill='#13202b')
 for x,y,c in spec['glyph']:
  d.rectangle((x*scale,y*scale,(x+1)*scale-1,(y+1)*scale-1),fill=c)
 # Native engraved edge, not an external emissive layer or smooth gradient.
 if scale==4:
  for x,y,c in spec['glyph']:
   rgb=tuple(int(c[i:i+2],16) for i in [1,3,5]);hi=tuple(min(255,v+22) for v in rgb)
   d.line((x*scale,y*scale,x*scale+2,y*scale),fill=hi)
 return im

def master_paths(id):
 logical=id.removeprefix('rune_')
 if any(logical==f'{f}_{t}' for f in ['miner','excavator','farmer','lumberjack'] for t in ['iii','iv']):return ['tools/tool-runes-art/'+id+'.png']
 if id in ['blank_rune_4','rune_unbreakable_iv','rune_perun_rage_iv','rune_forest_spirit_iv','rune_mastery_iv']:return ['tools/runes-craft-art/'+id+'.png']
 return []
def write_owned(path):
 p=Path(path)
 if p.stem not in BY_ID or not p.as_posix().endswith('/assets/slavicmyths/textures/item/'+p.stem+'.png'):return False
 p.parent.mkdir(parents=True,exist_ok=True);image(p.stem).save(p);return True
def apply(root=ROOT):
 for id in IDS:
  im=image(id)
  for rel in ['src/main/resources/assets/slavicmyths/textures/item/'+id+'.png']+master_paths(id):
   p=Path(root)/rel;p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
if __name__=='__main__':apply()
