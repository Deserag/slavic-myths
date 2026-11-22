"""Minecraft UV-mapped diamond-based marine armor with individually shaded scale tiles."""
import io,zipfile,pathlib
from PIL import Image
from ocean519_recolor import find_vanilla_jar,colorize
root=pathlib.Path('src/main/resources/assets/slavicmyths/textures')
with zipfile.ZipFile(find_vanilla_jar()) as z:
 for layer in (1,2):
  src=Image.open(io.BytesIO(z.read(f'assets/minecraft/textures/models/armor/diamond_layer_{layer}.png'))).convert('RGBA')
  im=colorize(src,(45,210,214)).resize((256,128),Image.Resampling.NEAREST)
  for y in range(64,128):
   for x in range(256):
    r,g,b,a=im.getpixel((x,y))
    if not a:continue
    u=x//4;v=y//4
    # Separate dark scale fields on torso and greaves, retaining diamond plate strips.
    scales=(layer==1 and 21<=u<=26 and 23<=v<=30) or (layer==2 and 5<=u<=10 and 21<=v<=30) or (layer==1 and 33<=u<=38 and 23<=v<=30)
    if scales:
     tx=(x+(y//6%2)*3)%6;ty=y%6
     c=(19,58,83) if ty==5 or tx==0 else (37,113,150) if ty<2 and tx<4 else (26,82,118)
     im.putpixel((x,y),(*c,a))
    elif v in (20,31) and 20<=u<=39:
     im.putpixel((x,y),(183,148,80,a))
  for yy in range(64):
   for xx in range(128,256):
    shade=(xx//4+yy//4)%3
    tint=(42+shade*6,184+shade*8,190+shade*8)
    if xx>=192 and yy>=32:tint=(174+shade*8,136+shade*6,70+shade*4)
    im.putpixel((xx,yy),(*tint,255))
  im.save(root/f'armor/marine/marine_armor_layer_{layer}.png')
 for out,base in [('tide_helmet','helmet'),('abyss_chestplate','chestplate'),('storm_leggings','leggings'),('waterwalk_boots','boots')]:
  im=colorize(Image.open(io.BytesIO(z.read(f'assets/minecraft/textures/item/diamond_{base}.png'))),(38,199,204))
  im.save(root/f'item/marine/{out}.png')
print('Marine diamond base, scale UV panels and bronze trim generated')