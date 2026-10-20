"""Six native pixel assets for #82, no stages, berries or other outputs.

Integer authored silhouettes. Apple/cabbage retain 32px, four woodland saplings 16px.
No resizing, filters, imported vanilla art, fruit on saplings or procedural noise.
"""
from pathlib import Path
from PIL import Image,ImageDraw
TREES=('apple','linden','rowan','willow','pine')
OWNED={f'block/{s}_sapling' for s in TREES}|{'item/cabbage'}

def image(name):
 n=32 if name in ('apple_sapling','cabbage') else 16
 im=Image.new('RGBA',(n,n));d=ImageDraw.Draw(im)
 if name=='apple_sapling':
  d.rectangle((15,13,17,29),fill='#654832');d.line([(16,27),(16,15)],fill='#a87e4c')
  d.line([(15,20),(10,17),(8,13)],fill='#795936',width=2)
  d.line([(17,19),(23,15),(24,11)],fill='#795936',width=2)
  for poly in [[(4,12),(6,10),(11,10),(13,12),(13,16),(10,18),(5,17),(4,15)],[(12,6),(14,4),(19,4),(21,7),(20,11),(16,13),(12,11)],[(21,10),(23,8),(27,9),(29,12),(28,16),(23,17),(21,15)]]:
   d.polygon(poly,fill='#365e32')
  d.polygon([(5,12),(7,11),(10,11),(12,13),(10,16),(6,16)],fill='#679744')
  d.polygon([(13,7),(15,5),(18,5),(20,7),(18,11),(14,10)],fill='#75a64d')
  d.polygon([(22,11),(24,10),(26,10),(28,12),(26,15),(23,15)],fill='#5f903f')
  d.line([(14,6),(17,6)],fill='#a3bf64');d.line([(6,12),(8,12)],fill='#8fb65a')
  d.point((24,11),fill='#9fb95c');d.line([(15,29),(18,29)],fill='#5b452f')
 elif name=='linden_sapling':
  d.rectangle((7,7,8,14),fill='#786244');d.line([(7,13),(7,9)],fill='#b4945a')
  d.line([(7,10),(4,7),(3,5)],fill='#786244');d.line([(8,9),(12,7)],fill='#786244')
  d.polygon([(4,2),(6,1),(9,1),(11,3),(10,6),(7,7),(4,5)],fill='#426a2f')
  d.polygon([(2,4),(5,4),(7,6),(5,9),(2,8),(1,6)],fill='#426a2f')
  d.polygon([(10,4),(13,4),(14,6),(13,8),(10,8),(9,6)],fill='#426a2f')
  d.polygon([(5,2),(8,2),(10,3),(8,5),(5,5)],fill='#86ab49')
  d.polygon([(2,5),(4,5),(5,6),(4,8),(2,7)],fill='#71953d')
  d.polygon([(11,5),(13,5),(13,7),(11,7),(10,6)],fill='#759e43')
  d.point((6,2),fill='#b5ca6a');d.point((2,5),fill='#a2be58')
 elif name=='rowan_sapling':
  d.line([(7,14),(7,5),(8,2)],fill='#76573e');d.line([(7,11),(3,7),(3,4)],fill='#927452')
  d.line([(7,9),(12,6),(12,3)],fill='#76573e')
  for x,y in [(6,2),(8,3),(2,3),(4,4),(1,5),(4,6),(10,2),(12,3),(10,4),(13,5),(2,7),(5,8),(10,7)]:
   d.line([(x,y),(x+1,y)],fill='#528143' if (x+y)%2 else '#76a454')
  d.line([(7,12),(8,14)],fill='#a58a5b')
 elif name=='willow_sapling':
  d.line([(7,14),(7,5),(8,3)],fill='#6d6346');d.line([(8,13),(8,7)],fill='#a59b63')
  d.line([(7,7),(3,5),(2,6)],fill='#6d6346');d.line([(8,6),(11,4),(13,5)],fill='#6d6346')
  for x,top,bottom in [(2,6,10),(4,5,8),(11,5,9),(13,6,11)]:
   d.line([(x,top),(x,bottom)],fill='#526e3a');d.line([(x,top),(x,top+1)],fill='#9bb364')
   d.point((x,bottom),fill='#77944f')
  d.line([(6,3),(8,2),(9,3)],fill='#829f50');d.line([(6,4),(8,4)],fill='#567738')
 elif name=='pine_sapling':
  d.rectangle((7,9,8,14),fill='#775233');d.line([(8,12),(8,14)],fill='#b07b43')
  d.polygon([(7,1),(8,1),(9,4),(8,5),(7,5),(6,4)],fill='#376743')
  d.polygon([(6,5),(8,4),(10,6),(11,7),(9,8),(5,8),(4,7)],fill='#28573b')
  d.polygon([(5,8),(8,7),(11,9),(13,11),(10,12),(3,12),(2,11)],fill='#28573b')
  d.line([(7,2),(7,3)],fill='#73a561');d.line([(6,5),(8,5),(9,6)],fill='#558a52')
  d.line([(5,9),(7,8),(9,9),(11,10)],fill='#4b8150')
  for pt in [(3,10),(6,10),(9,11),(11,11)]:d.point(pt,fill='#67975a')
 elif name=='cabbage':
  # Squared sides and clipped corners; outer leaves wrap a layered head.
  d.polygon([(6,4),(25,4),(28,7),(28,25),(25,28),(6,28),(3,25),(3,7)],fill='#294e2c')
  d.polygon([(7,5),(24,5),(27,8),(27,24),(24,27),(7,27),(4,24),(4,8)],fill='#527b36')
  d.polygon([(9,7),(21,6),(24,9),(24,19),(20,23),(11,24),(7,20),(7,11)],fill='#779d48')
  d.polygon([(11,8),(20,8),(22,10),(22,17),(19,20),(12,20),(9,17),(9,11)],fill='#96b65a')
  d.polygon([(12,9),(19,9),(21,11),(20,14),(13,15),(10,13)],fill='#adc771')
  # Irregular folds/veins, deliberately not gem-like straight specular bands.
  d.line([(8,9),(7,12),(8,17),(12,21),(17,22),(21,20)],fill='#456e33',width=2)
  d.line([(22,8),(24,12),(23,18),(20,21),(16,23)],fill='#5b843b')
  d.line([(10,10),(10,13),(12,16),(17,18),(20,16)],fill='#c0cf7d')
  d.polygon([(4,17),(7,18),(11,23),(15,24),(12,27),(7,26),(4,23)],fill='#648b3e')
  d.line([(5,19),(8,23),(11,25)],fill='#99b659')
  d.polygon([(25,15),(27,18),(27,23),(24,26),(18,27),(19,24),(23,22)],fill='#3e6932')
  d.line([(25,18),(24,22),(21,25)],fill='#7ea54c')
 else:raise ValueError(name)
 return im

def write_owned(path):
 p=Path(path)
 match=next((s for s in OWNED if p.as_posix().endswith('/assets/slavicmyths/textures/'+s+'.png')),None)
 if match is None:return False
 p.parent.mkdir(parents=True,exist_ok=True);image(p.stem).save(p);return True

def apply(root):
 for s in sorted(OWNED):assert write_owned(Path(root)/'src/main/resources/assets/slavicmyths/textures'/f'{s}.png')

if __name__=='__main__':apply(Path(__file__).resolve().parents[1])
