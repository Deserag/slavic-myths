"""Schematic voxel preview from the actual NBT; not an in-game screenshot."""
import sys,json,zipfile
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parent))
import village_buildings as v
from PIL import Image,ImageDraw,ImageFont
z=zipfile.ZipFile(v.JAR)
colors={}
def color(name):
 name=name.split(':')[-1]
 if name in colors:return colors[name]
 tex=name.replace('stripped_','stripped_').replace('_stairs','_planks').replace('_slab','_planks').replace('_fence','_planks')
 if 'log' in tex:tex+='_top'
 try:
  import io
  im=Image.open(io.BytesIO(z.read('assets/minecraft/textures/block/'+tex+'.png'))).convert('RGB').resize((1,1));c=im.getpixel((0,0))
 except Exception:
  c=(86,60,36) if 'pine' in tex or 'wood' in tex or 'rack' in tex else (221,148,50) if 'lantern' in tex else (159,43,35) if 'red' in tex else (205,204,183) if 'white' in tex else (80,108,120) if 'glass' in tex else (121,109,80)
 colors[name]=c;return c
def render(meta,cut=False):
 d=v.read_nbt((v.RES/('data/slavicmyths/structure/'+meta['id'].split(':')[1]+'.nbt')).read_bytes());blocks={tuple(row['pos']):d['palette'][row['state']] for row in d['blocks'] if d['palette'][row['state']]['Name'] not in ['minecraft:air','minecraft:jigsaw']}
 if cut:blocks={p:s for p,s in blocks.items() if p[1]<5 and (p[0]>2 and p[2]>4 or p[1]<2)}
 im=Image.new('RGB',(400,330),'#e6dfce');draw=ImageDraw.Draw(im)
 scale=9;ox=30;oy=155
 def project(x,y,z):return (ox+(x+z)*scale,oy+(x-z)*scale*.5-y*scale)
 for (x,y,z),state in sorted(blocks.items(),key=lambda e:(e[0][0]-e[0][2]+2*e[0][1],e[0][1])):
  name=state['Name'];c=color(name);height=.5 if '_slab' in name or '_bed' in name else .125 if 'carpet' in name else 1
  corners={'top':[(x,y+height,z),(x+1,y+height,z),(x+1,y+height,z+1),(x,y+height,z+1)],'north':[(x,y,z),(x+1,y,z),(x+1,y+height,z),(x,y+height,z)],'east':[(x+1,y,z),(x+1,y,z+1),(x+1,y+height,z+1),(x+1,y+height,z)]}
  for face,mult,neighbor in [('north',.75,(x,y,z-1)),('east',.9,(x+1,y,z)),('top',1.12,(x,y+1,z))]:
   if neighbor in blocks:continue
   draw.polygon([project(*p) for p in corners[face]],fill=tuple(min(255,int(a*mult)) for a in c),outline=tuple(int(a*.55) for a in c))
 draw.text((12,309),meta['kind']+' / '+meta['palette']+(' / interior' if cut else ''),fill='#2e241b')
 return im
catalog=json.loads((v.RES/'data/slavicmyths/village/catalog.json').read_text())['structures'];chosen=[]
for kind in ['seni','zemlyanka','izba','chronicler_terem','herbalist_house','brewer_house','weaver_house','cook_house','ambar','klet','hlev','druzhinnik_barracks']:
 chosen.append(next(m for m in catalog if m['kind']==kind and m['palette']=='temperate'))
output=Image.new('RGB',(1600,990))
for i,m in enumerate(chosen):output.paste(render(m),(i%4*400,i//4*330))
output.save(v.ROOT/'work/village-buildings-1.2.4-preview.png')
interior=Image.new('RGB',(1600,660))
for i,m in enumerate(chosen[:8]):interior.paste(render(m,True),(i%4*400,i//4*330))
interior.save(v.ROOT/'work/village-buildings-1.2.4-interiors.png')

