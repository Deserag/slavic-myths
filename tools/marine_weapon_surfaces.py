"""Small repeatable grayscale UV surfaces, tinted by the authored marine meshes."""
from PIL import Image
from pathlib import Path
root=Path('src/main/resources/assets/slavicmyths/textures/entity/marine');root.mkdir(parents=True,exist_ok=True)
for name in ['weapon_scales','weapon_scutes']:
 im=Image.new('RGBA',(32,32))
 for y in range(32):
  for x in range(32):
   row=y//8;u=(x+4*(row%2))%8;v=y%8
   edge=7-int(abs(u-3.5)*.65)
   c=115 if v>=edge else 245 if v<2 and 1<u<6 else 195 if u<4 else 170
   if name=='weapon_scutes':c=120 if u in (0,7) or v in (0,7) else 220 if u<4 and v<4 else 175
   im.putpixel((x,y),(c,c,c,255))
 im.save(root/(name+'.png'))
print('Marine scale and shell UV surfaces generated')