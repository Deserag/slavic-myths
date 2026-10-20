"""Four approved pilot identities, native 64px source + production only.

Same tool geometry through III/IV; foliage is subordinate. No blur or text/glint.
The existing tool_runes_resources writer copies these curated masters unchanged.
"""
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
IDS=('farmer_iii','farmer_iv','lumberjack_iii','lumberjack_iv')
def frame(iv):
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
 # Identical square silhouette, small clipped corners and fixed top-left light.
 d.polygon([(6,2),(57,2),(61,6),(61,57),(57,61),(6,61),(2,57),(2,6)],fill='#151b24')
 d.polygon([(7,4),(56,4),(59,7),(59,56),(56,59),(7,59),(4,56),(4,7)],fill='#3d4a59')
 d.line([(6,54),(6,7),(54,6)],fill='#9cadb9',width=3)
 d.line([(57,8),(57,56),(8,57)],fill='#293541',width=3)
 d.rectangle((10,10,53,53),fill='#1d2630')
 # Stone planes; native pixel facets rather than a smooth gradient.
 d.polygon([(12,12),(51,12),(48,16),(16,16),(16,48),(12,51)],fill='#3a4651')
 d.rectangle((16,16,49,49),fill='#28343e')
 d.line([(17,51),(51,51),(51,17)],fill='#111e28',width=2)
 metal='#d6b361' if iv else '#a9bdc8';shadow='#6d532e' if iv else '#4f677a'
 if iv:
  # Double inset rim, four fasteners; no particles extending the silhouette.
  d.rectangle((8,8,55,55),outline=shadow,width=3)
  d.line([(9,53),(9,9),(53,9)],fill=metal,width=2)
  d.line([(54,10),(54,54),(10,54)],fill='#8b713e',width=2)
  for x,y in [(8,8),(51,8),(8,51),(51,51)]:
   d.rectangle((x-2,y-2,x+4,y+4),fill='#6d532e');d.rectangle((x-1,y-1,x+3,y+3),fill='#d6b361');d.rectangle((x,y,x+1,y+1),fill='#f2d997')
  for x,y in [(31,7),(31,55),(7,31),(55,31)]:d.rectangle((x,y,x+1,y+1),fill='#e2c77e')
 else:
  for x,y in [(7,7),(52,7),(7,52),(52,52)]:
   d.rectangle((x,y,x+4,y+4),fill='#64778a');d.rectangle((x,y,x+2,y+1),fill='#c6b77b')
 return im

def tool_masks(family):
 # This shared mask is the exact invariant for both levels of each identity.
 handle=Image.new('L',(64,64));h=ImageDraw.Draw(handle)
 head=Image.new('L',(64,64));d=ImageDraw.Draw(head)
 if family=='farmer':
  h.line([(22,46),(40,24)],fill=255,width=6)
  d.polygon([(20,17),(39,17),(44,21),(42,25),(34,22),(25,22),(25,29),(20,29)],fill=255)
 else:
  h.line([(22,47),(39,24)],fill=255,width=6)
  d.polygon([(31,18),(39,18),(47,22),(47,31),(43,36),(35,32),(31,30)],fill=255)
 return handle,head

def image(id):
 family,tier=id.rsplit('_',1);iv=tier=='iv';im=frame(iv);d=ImageDraw.Draw(im)
 if family=='farmer':dark,mid,light='#3d7750','#88bb69','#d0e69c'
 else:dark,mid,light='#265d4d','#62ad88','#b1dfb0'
 handle,head=tool_masks(family)
 # A one-native-pixel engraved shadow keeps tool edges separate from the slab.
 for mask in [handle,head]:
  shadow=Image.new('RGBA',(64,64),'#101c24');im.paste(shadow,(1,1),mask)
 im.paste('#926d41' if not iv else '#c29a54',(0,0),handle)
 im.paste(mid,(0,0),head)
 d=ImageDraw.Draw(im)
 d.line([(22,45),(39,24)],fill='#c8ac6c' if not iv else '#e4cd88',width=2)
 if family=='farmer':
  d.line([(21,18),(38,18),(42,21)],fill=light,width=2)
  d.line([(21,21),(21,27)],fill=light,width=2)
  d.line([(27,21),(34,21)],fill=dark)
  # One small seedling, occupying much less area than the hoe.
  d.line([(43,46),(43,40)],fill=mid,width=2)
  d.polygon([(43,42),(38,39),(39,37),(43,39)],fill=mid)
  d.polygon([(44,40),(48,36),(49,38),(45,42)],fill=light)
  if iv:
   d.line([(47,49),(47,45),(49,43)],fill='#9dba69')
   d.point((49,42),fill='#d5cc83')
 else:
  d.line([(33,19),(38,19),(45,23)],fill=light,width=2)
  d.line([(46,24),(46,30),(43,34)],fill=light,width=2)
  d.line([(33,23),(33,29),(40,32)],fill=dark,width=2)
  # Small forest accent to the left; never replaces the large axe head.
  d.polygon([(17,35),(13,40),(15,40),(12,44),(16,44),(16,47),(18,47),(18,44),(22,44),(19,40),(21,40)],fill=dark)
  d.line([(17,37),(17,42)],fill=mid)
  if iv:
   d.polygon([(46,44),(50,42),(50,45),(47,47)],fill=dark)
   d.point((48,44),fill=mid)
 return im

def apply(root=ROOT):
 for id in IDS:
  im=image(id)
  for directory in ['tools/tool-runes-art','src/main/resources/assets/slavicmyths/textures/item']:
   p=Path(root)/directory/f'rune_{id}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
if __name__=='__main__':apply()
