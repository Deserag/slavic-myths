"""Schematic projection of authored NBT geometry, not an in-game screenshot."""
import json,sys,zipfile,io
from pathlib import Path
from PIL import Image,ImageDraw
sys.path.insert(0,str(Path(__file__).parent))
import village_buildings as v
catalog=json.loads((v.RES/'data/slavicmyths/gorodishche/catalog.json').read_text())['buildings']
jar=zipfile.ZipFile(v.JAR);colors={}
def color(name):
 key=name.split(':')[-1]
 if key in colors:return colors[key]
 tex=key.replace('_stairs','_planks').replace('_slab','_planks').replace('_fence','_planks')
 if 'log' in tex:tex+='_top'
 try:c=Image.open(io.BytesIO(jar.read('assets/minecraft/textures/block/'+tex+'.png'))).convert('RGB').resize((1,1)).getpixel((0,0))
 except Exception:c=(86,60,36) if 'pine' in key or 'wood' in key else (215,203,170) if 'cloth' in key else (166,137,69) if 'lantern' in key else (155,49,35) if 'red' in key else (203,203,191) if 'white' in key else (82,110,122) if 'glass' in key else (133,112,81)
 colors[key]=c;return c
names=['terem_01','trading_house_01','rich_izba_01','craftsman_weaver_01','druzhinnik_house_01','narrow_house_01','gate_main_01','tower_corner_01']
out=Image.new('RGB',(2000,1000),'#e6dfce')
for i,name in enumerate(names):
 m=next(x for x in catalog if x['name']==name and x['climate']=='temperate');n=v.read_nbt((v.RES/('data/slavicmyths/structure/'+m['id'].split(':')[1]+'.nbt')).read_bytes());blocks={tuple(b['pos']):n['palette'][b['state']]['Name'] for b in n['blocks'] if n['palette'][b['state']]['Name'] not in ['minecraft:air','minecraft:structure_block']};im=Image.new('RGB',(500,500),'#e6dfce');draw=ImageDraw.Draw(im);scale=min(9,440/(m['size'][0]+m['size'][2]));oy=270
 def project(x,y,z):return (25+(x+z)*scale,oy+(x-z)*scale*.5-y*scale)
 for (x,y,z),key in sorted(blocks.items(),key=lambda row:(row[0][0]-row[0][2]+2*row[0][1],row[0][1])):
  c=color(key);h=.5 if '_slab' in key or '_bed' in key else .125 if 'carpet' in key else 1
  faces=[('north',.75,(x,y,z-1),[(x,y,z),(x+1,y,z),(x+1,y+h,z),(x,y+h,z)]),('east',.9,(x+1,y,z),[(x+1,y,z),(x+1,y,z+1),(x+1,y+h,z+1),(x+1,y+h,z)]),('top',1.12,(x,y+1,z),[(x,y+h,z),(x+1,y+h,z),(x+1,y+h,z+1),(x,y+h,z+1)])]
  for face,mult,neighbor,corners in faces:
   if neighbor in blocks:continue
   draw.polygon([project(*p) for p in corners],fill=tuple(min(255,int(a*mult)) for a in c),outline=tuple(int(a*.60) for a in c))
 draw.text((12,475),name+' / actual NBT schematic',fill='#2e241b');out.paste(im,(i%4*500,i//4*500))
out.save(v.ROOT/'work/gorodishche-125-geometry.png')
