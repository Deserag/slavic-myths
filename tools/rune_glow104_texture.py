"""Reproducible seamless low-contrast rune modulation; no item artwork."""
from pathlib import Path
from PIL import Image
import math
R=Path(__file__).resolve().parents[1]
def image():
 im=Image.new('RGB',(128,128))
 for y in range(128):
  for x in range(128):
   u=x/128*math.tau;v=y/128*math.tau
   q=.38+.12*math.sin(3*u+2*v+.8*math.sin(u-v))+.09*math.sin(5*v-2*u)+.06*math.sin(9*u+7*v)+.035*math.sin(17*u-11*v)
   im.putpixel((x,y),(round(max(0,min(1,q))*255),round((.5+.5*math.sin(2*u-v))*255),round((.5+.5*math.cos(u+3*v))*255)))
 return im
if __name__=='__main__':
 image().save(R/'src/main/resources/assets/slavicmyths/textures/misc/rune_glint.png')
