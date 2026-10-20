"""Owner correction: vanilla metal topology, material colours and small seams only.

The installed Minecraft iron_ingot/iron_ore pixel topology is recorded in the
adjacent JSON. Vanilla resources are never overwritten. Older #81 instructions
requiring separate ingot silhouettes are superseded by the owner's screenshots.
"""
from pathlib import Path
import json
from PIL import Image,ImageColor
ROOT=Path(__file__).resolve().parents[1]
TOPOLOGY=json.loads(Path(__file__).with_name('vanilla_metal_topology.json').read_text(encoding='utf-8'))
PALETTES={
 'silver_ingot':['#596b79','#8395a3','#91a4b2','#b6c8d3','#cbdbe4','#e4edf3','#f2f7fb','#ffffff'],
 'perunite':['#162238','#203350','#294267','#385984','#486e9d','#527db0','#6b96c5','#b2d8eb'],
 'purified_iron_ingot':['#2d3e49','#4a616b','#5a6e77','#738992','#8fa4ad','#aebfc8','#cad5db','#e8eff2'],
 'purified_copper_ingot':['#582c22','#84432e','#985136','#b86943','#d38453','#efa66d','#ffc58b','#ffe2b6'],
 'hardened_iron':['#24272c','#373c43','#424951','#53606a','#65727b','#7d8991','#9ba6ad','#c5cdd0']}
NATIVE32={'purified_iron_ingot','purified_copper_ingot','hardened_iron'}
SEAMS={
 'silver_ingot':[(7,6,'#d1ecf4'),(8,6,'#d1ecf4'),(9,5,'#e0f4fa')],
 'perunite':[(7,6,'#3bafc1'),(8,5,'#64d7df'),(9,5,'#64d7df'),(9,6,'#3bafc1'),(10,6,'#3bafc1')],
 'purified_iron_ingot':[(6,6,'#59b7ca'),(7,6,'#8bdde8'),(8,5,'#59b7ca'),(9,5,'#59b7ca'),(10,6,'#8bdde8')],
 'purified_copper_ingot':[(6,6,'#438f99'),(7,6,'#7fd5d7'),(8,5,'#438f99'),(9,5,'#438f99'),(10,6,'#7fd5d7')]}
ORES={'silver_ore':['#74899a','#91a6b7','#bdcedb','#e1ecf4','#f4faff'],
      'perunite_ore':['#283f66','#365e8d','#4c88b2','#54becf','#9de2e7']}
OWNED={f'item/{n}' for n in PALETTES}|{f'block/{n}' for n in ORES}
def image(name):
 if name in PALETTES:
  src=TOPOLOGY['ingot'];colors={'.':(0,0,0,0),**{chr(97+i):ImageColor.getrgb(c)+(255,) for i,c in enumerate(PALETTES[name])}}
  scale=2 if name in NATIVE32 else 1
 else:
  src=TOPOLOGY['ore'];colors={k:tuple(v) for k,v in src['palette'].items()}
  for key,color in zip(['.','b','f','g','h'],ORES[name]):colors[key]=ImageColor.getrgb(color)+(255,)
  scale=1
 # Emit authored logical 16px cells directly at retained native resolution.
 # No interpolating, resizing or changes to alpha/silhouette/face boundaries.
 im=Image.new('RGBA',(16*scale,16*scale))
 for y,row in enumerate(src['grid']):
  for x,key in enumerate(row):
   for yy in range(scale):
    for xx in range(scale):im.putpixel((x*scale+xx,y*scale+yy),colors[key])
 for x,y,color in SEAMS.get(name,[]):
  assert src['grid'][y][x] in 'gh' # Only top-plane interior, never a changed edge.
  for yy in range(scale):
   for xx in range(scale):im.putpixel((x*scale+xx,y*scale+yy),ImageColor.getrgb(color)+(255,))
 return im
def write_owned(path):
 p=Path(path)
 if not any(p.as_posix().endswith('/assets/slavicmyths/textures/'+n+'.png') for n in OWNED):return False
 p.parent.mkdir(parents=True,exist_ok=True);image(p.stem).save(p);return True
def apply(root):
 for n in sorted(OWNED):assert write_owned(Path(root)/'src/main/resources/assets/slavicmyths/textures'/f'{n}.png')
if __name__=='__main__':apply(ROOT)
