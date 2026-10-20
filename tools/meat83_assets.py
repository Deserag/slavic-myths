"""#83: five raw-meat sprites only. Native sizes and original wildlife outlines.

Hand-directed integer pixels, natural muscle/fat tones, no vanilla samples or blur.
Goose and all cooked pairs deliberately stay outside OWNED.
"""
from pathlib import Path
from PIL import Image,ImageDraw
OWNED=('raw_duck','raw_goat','raw_boar_meat','raw_venison','raw_bear_meat')
def image(name):
 assert name in OWNED
 size=32 if name in ('raw_duck','raw_goat') else 16
 im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
 if name=='raw_duck':
  d.polygon([(4,24),(4,20),(9,14),(20,7),(24,8),(27,12),(26,17),(19,24),(11,28),(6,27)],fill='#713137')
  d.polygon([(6,23),(6,20),(11,15),(20,9),(23,10),(25,13),(23,18),(17,23),(10,26),(7,25)],fill='#b8545a')
  d.line([(7,23),(12,17),(20,11),(23,12)],fill='#e4aaa0',width=2)
  d.line([(10,24),(16,20),(21,15)],fill='#d17a76',width=2)
  d.line([(15,22),(21,18),(24,14)],fill='#923d47')
 elif name=='raw_goat':
  d.polygon([(5,25),(4,16),(10,12),(13,5),(20,4),(27,13),(28,23),(21,27),(11,29)],fill='#743039')
  d.polygon([(7,24),(6,17),(12,14),(15,7),(19,6),(25,14),(26,22),(20,25),(11,27)],fill='#b44b50')
  d.polygon([(8,17),(13,15),(16,9),(20,10),(23,15),(19,19),(13,22),(8,23)],fill='#cf6867')
  d.line([(8,25),(12,27),(20,25),(25,22)],fill='#dfb8a1',width=2)
  d.line([(12,19),(16,17),(20,13)],fill='#e59985',width=2)
  d.line([(18,23),(22,20),(23,16)],fill='#943b46')
 else:
  # Preserve the real original wildlife alpha: two clipped rectangles per cut.
  shapes={'raw_boar_meat':[(2,5,13,11),(4,3,11,13)],'raw_venison':[(3,5,12,9),(5,9,10,13)],'raw_bear_meat':[(2,6,12,12),(5,4,13,10)]}
  edge={'raw_boar_meat':'#673035','raw_venison':'#672d37','raw_bear_meat':'#512930'}[name]
  for x0,y0,x1,y1 in shapes[name]:
   for y in range(y0,y1+1):
    for x in range(x0,x1+1):
     if x in (x0,x1) and y in (y0,y1):continue
     d.point((x,y),fill=edge)
  mask=im.getchannel('A')
  if name=='raw_boar_meat':
   d.polygon([(4,5),(10,4),(12,6),(12,9),(9,12),(5,11),(3,8)],fill='#ad5050')
   d.line([(5,6),(8,5),(10,5)],fill='#cf7970')
   d.line([(4,4),(10,4)],fill='#ddbaa1')
   d.line([(5,10),(8,9),(10,10)],fill='#cf9584')
   d.line([(10,7),(11,8),(10,10)],fill='#85383e')
  elif name=='raw_venison':
   d.polygon([(5,6),(10,6),(11,8),(9,10),(8,12),(6,11),(6,9),(4,8)],fill='#a13e49')
   d.line([(5,6),(8,6)],fill='#c96569')
   d.line([(6,9),(8,8),(10,8)],fill='#bc5961')
   d.line([(7,11),(8,10)],fill='#ce7778')
  else:
   d.polygon([(6,5),(11,5),(12,7),(10,10),(10,11),(4,11),(3,9),(5,7)],fill='#8c3e49')
   d.line([(6,6),(9,5),(11,6)],fill='#ae5b60')
   d.line([(4,9),(7,8),(9,8)],fill='#ad555c',width=2)
   d.line([(6,10),(9,10)],fill='#c08b80')
   d.line([(11,8),(10,10)],fill='#6d303c')
  im.putalpha(mask)
 return im
def write_owned(path):
 p=Path(path)
 if p.stem not in OWNED or not p.as_posix().endswith('/assets/slavicmyths/textures/item/'+p.stem+'.png'):return False
 p.parent.mkdir(parents=True,exist_ok=True);image(p.stem).save(p);return True
def apply(root):
 for name in OWNED:assert write_owned(Path(root)/'src/main/resources/assets/slavicmyths/textures/item'/f'{name}.png')
if __name__=='__main__':apply(Path(__file__).resolve().parents[1])
