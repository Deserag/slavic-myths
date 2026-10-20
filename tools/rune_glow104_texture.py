"""Shared neutral modulation data, not item-specific art. Integer32px repeat texture."""
from pathlib import Path
from PIL import Image
R=Path(__file__).resolve().parents[1]
def image():
 im=Image.new('RGB',(32,32))
 for y in range(32):
  for x in range(32):
   q=(x+y*2)%32;distance=min(abs(q-8),32-abs(q-8));intensity=max(0,204-distance*34)
   mint=max(0,255-abs(((x-y)%32)-16)*32);turquoise=max(0,255-abs(((x+3*y)%32)-12)*42)
   im.putpixel((x,y),(intensity,mint,turquoise))
 return im
if __name__=='__main__':
 p=R/'src/main/resources/assets/slavicmyths/textures/misc/rune_glint.png';p.parent.mkdir(parents=True,exist_ok=True);image().save(p)
