"""Deterministic supplementary glow masks and original summon atlases; original boss PNGs untouched."""
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2];base=ROOT/'src/main/resources/assets/slavicmyths/textures/entity'
for name in ['morova_bride','trizna_flaming_prince']:
 src=Image.open(base/(name+'.png')).convert('RGBA');out=Image.new('RGBA',src.size)
 for y in range(96,min(128,src.height)):
  for x in range(src.width):
   r,g,b,a=src.getpixel((x,y))
   if r>g*1.25 and r>b*1.1 and r>60:out.putpixel((x,y),(r,g,b,a))
 assert out.getbbox(),name+' empty emissive mask'
 out.save(base/(name+'_glow.png'))
for shadow in [True,False]:
 image=Image.new('RGBA',(64,64),(18,26,26,175) if shadow else (32,32,37,255));d=ImageDraw.Draw(image)
 bone=(117,145,132,190) if shadow else (160,151,127,255)
 d.rectangle((8,8,15,15),fill=bone);d.rectangle((9,10,10,11),fill='#73b29d' if shadow else '#df7429');d.rectangle((13,10,14,11),fill='#73b29d' if shadow else '#df7429');d.line((10,13,13,13),fill='#27272b')
 if not shadow:
  for x in range(20,28,3):d.line((x,21,x,30),fill='#575158')
  d.rectangle((20,29,27,30),fill='#7d2a31');d.line((6,22,6,30),fill='#4e4a4d');d.line((46,22,46,30),fill='#4e4a4d')
 else:
  d.line((20,22,27,29),fill=(69,93,86,150));d.line((21,29,26,23),fill=(69,93,86,150))
 image.save(base/('morova_shadow.png' if shadow else 'trizna_retainer.png'))
