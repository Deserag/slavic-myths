"""Earth surfaces from Minecraft 1.21.1 textures copied into versioned local templates.
Preserve emissive/core/root UV panels; coherent pixel scale across all body segments.
"""
from pathlib import Path
import json
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';T=R/'tools/templates/earth-163'
def tile(name,w,h):
 src=Image.open(T/(name+'.png')).convert('RGBA');out=Image.new('RGBA',(w,h))
 for y in range(0,h,16):
  for x in range(0,w,16):out.paste(src,(x,y))
 return out
for phase in [1,2,3]:
 p=A/f'textures/entity/earth_spirit/phase_{phase}.png';old=Image.open(p).convert('RGBA');im=tile('cobblestone',256,256)
 # Solid stone face, darker deepstone fists/feet and large coherent moss shoulders.
 for box,name in [((0,64,80,104),'stone'),((128,64,224,110),'cobbled_deepslate'),((144,0,224,32),'mossy_cobblestone'),((0,176,64,208),'andesite'),((80,176,144,208),'andesite'),((128,208,224,239),'cobbled_deepslate')]:
  x,y,xx,yy=box;im.paste(tile(name,xx-x,yy-y),(x,y))
 for box in [(224,192,240,220),(224,224,256,256)]:im.paste(old.crop(box),(box[0],box[1]))
 im.save(p)
# Partial-height seal must not hide the grass below. Bottom texture and particle provided.
for crack in range(3):
 p=A/f'models/block/earth_heart_seal_{crack}.json';v=json.loads(p.read_text());v['textures']['particle']=v['textures']['all'];v['ambientocclusion']=True
 p.write_text(json.dumps(v,indent=2)+'\n')
print('Earth complete-body vanilla material atlas and seal models generated')
