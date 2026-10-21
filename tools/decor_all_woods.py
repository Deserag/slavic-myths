"""Six-family timber art and dependent wood surfaces, authorised 21 October.

Linden's approved pilot, all end grains, pine plate bark and rowan lenticels
are retained exactly. Other motifs are independently authored short clusters.
Original construction overlays/holes/hardware/UV come from a pinned template
archive; this is not an import-time legacy generator or a bulk hue shift.
"""
from pathlib import Path
from io import BytesIO
from functools import lru_cache
import zipfile,json
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1];TEXTURES=ROOT/'src/main/resources/assets/slavicmyths/textures'
WOODS=('linden','willow','apple','pine','rowan','darkened')
ARCHIVE=Path(__file__).with_name('decor_wood_templates.zip')
PALETTE={
 'willow':{'planks':(178,177,134),'log':(147,153,119),'stripped_log':(204,202,157)},
 'apple':{'planks':(157,110,91),'log':(121,101,84),'stripped_log':(173,132,114)},
 'pine':{'planks':(161,117,65),'log':(85,75,65),'stripped_log':(200,153,82)},
 'rowan':{'planks':(169,103,73),'log':(129,107,94),'stripped_log':(201,145,117)},
 'darkened':{'planks':(57,48,39),'log':(46,42,38),'stripped_log':(74,62,52)}}
# (start-x, local-y, length, one-texel bend), separately drawn per species.
CLUSTERS={
 'willow':[(1,2,7,1),(13,4,5,-1),(23,2,6,1),(3,6,4,-1)],
 'apple':[(2,3,8,-1),(15,5,6,1),(25,2,4,-1),(7,6,5,0)],
 'pine':[(0,2,9,0),(13,4,8,-1),(25,5,5,1),(3,6,6,-1)],
 'rowan':[(1,2,5,-1),(9,5,7,1),(20,3,5,1),(27,6,4,-1),(5,6,3,0)],
 'darkened':[(2,3,6,0),(13,5,4,1),(23,2,7,-1)]}

@lru_cache(None)
def raw(rel):
 with zipfile.ZipFile(ARCHIVE) as z:return z.read(rel)

def template(rel):return Image.open(BytesIO(raw(rel))).convert('RGBA')
def name(wood,kind):return ('stripped_apple_log'+('_top' if kind.endswith('top') else '')) if wood=='apple' and kind.startswith('stripped_log') else wood+'_'+kind
def shade(c,delta):return tuple(max(0,min(255,v+delta)) for v in c)+(255,)

def keep_base(wood,kind):return wood=='linden' or kind.endswith('top') or kind=='log' and wood in ('pine','rowan')

def base(wood,kind):
 rel='block/'+name(wood,kind)+'.png'
 if keep_base(wood,kind):return template(rel)
 c=PALETTE[wood][kind];im=Image.new('RGBA',(32,32),c+(255,));d=ImageDraw.Draw(im)
 if kind=='log':
  if wood=='willow':
   paths=[[(2,1),(2,5),(3,6),(3,10)],[(8,0),(8,4),(7,6)],[(12,3),(12,9),(13,11)],[(18,0),(18,6),(17,8)],[(24,2),(23,5),(23,10)],[(29,0),(29,4)],[(1,16),(2,19),(2,23)],[(7,13),(6,17),(7,20)],[(13,17),(13,23),(12,26)],[(19,12),(18,16),(19,19)],[(25,17),(24,22),(25,25)],[(29,23),(28,27),(28,31)],[(5,26),(5,31)],[(17,27),(17,31)]]
   for path in paths:d.line(path,fill=shade(c,-21));d.line([(x+1,y) for x,y in path[:-1]],fill=shade(c,9))
   d.polygon([(21,12),(23,14),(23,17),(21,19),(20,16),(20,14)],outline=shade(c,-21));d.line((21,14,21,17),fill=shade(c,-12))
  elif wood=='apple':
   paths=[[(2,0),(2,5),(4,7)],[(9,1),(9,6),(8,9)],[(16,0),(15,4),(16,7)],[(24,2),(23,8)],[(29,0),(29,6),(30,8)],[(3,13),(2,17),(3,21)],[(9,12),(8,16),(10,19)],[(16,12),(18,16),(17,22)],[(25,13),(26,17),(25,20)],[(30,17),(29,23)],[(4,25),(3,28),(3,31)],[(12,23),(12,29),(13,31)],[(21,25),(20,31)],[(27,26),(28,31)]]
   for path in paths:d.line(path,fill=shade(c,-25));d.line([(x+1,y) for x,y in path[:-1]],fill=shade(c,10))
   d.polygon([(7,17),(10,19),(10,22),(8,24),(6,22),(6,19)],outline=shade(c,-25));d.point((8,21),fill=shade(c,-35))
  else:
   paths=[[(3,0),(3,6),(2,8)],[(12,2),(11,6),(11,10)],[(21,0),(22,4),(22,8)],[(29,3),(28,8)],[(5,13),(4,17),(5,21)],[(15,12),(14,16),(15,22)],[(24,14),(25,18)],[(2,26),(3,31)],[(11,25),(11,31)],[(21,25),(20,29)],[(28,23),(28,28),(29,31)]]
   for path in paths:d.line(path,fill=shade(c,-12));d.line([(x+1,y) for x,y in path[:2]],fill=shade(c,6))
   d.line([(11,10),(14,9),(17,10)],fill=shade(c,-14));d.line([(3,23),(6,24),(8,23)],fill=shade(c,-12))
  return im
 vertical=kind=='stripped_log'
 for band in range(4):
  offset={'willow':[0,3,-2,1],'apple':[0,5,1,-4],'pine':[0,-3,4,2],'rowan':[0,2,-4,5],'darkened':[0,4,-2,1]}[wood][band]
  for x,y,length,bend in CLUSTERS[wood]:
   x=(x+offset)%32;y+=band*8;points=[(x,y),(min(31,x+length//2),y),(min(31,x+length//2+1),min(31,y+bend)),(min(31,x+length),min(31,y+bend))]
   if vertical:points=[(min(31,py),(px+band*3)%32) for px,py in points]
   d.line(points,fill=shade(c,-8 if wood=='willow' else -12 if wood=='darkened' else -14))
   if (band+x)%2==0:
    highlights=[(px,py-1) if not vertical else (px-1,py) for px,py in points[:2]];d.line(highlights,fill=shade(c,5 if wood in ['willow','darkened'] else 8))
 if not vertical:
  for y in ([0,8,16,24] if wood=='apple' else [7,15,23]):
   d.line((0,y,31,y),fill=shade(c,-38 if wood=='apple' else -28));d.line((0,(y+1)%32,31,(y+1)%32),fill=shade(c,8))
  if wood=='apple':
   for band,x in enumerate([0,21,10,29]):d.line((x,band*8+1,x,band*8+7),fill=shade(c,-25))
 # Rare purposeful knots, not a lattice of noise; species differ in shape/spacing.
 if wood in ['pine','rowan']:
  x,y=(23,19) if wood=='pine' else (10,12)
  if vertical:x,y=(24,18) if wood=='pine' else (10,17)
  d.polygon([(x-4,y),(x-2,y-2),(x+2,y-2),(x+4,y),(x+1,y+2),(x-2,y+2)],outline=shade(c,-28));d.line((x-1,y,x+1,y),fill=shade(c,-36));d.line((x-2,y-1,x+1,y-1),fill=shade(c,7))
 if wood=='willow' and vertical:d.line([(24,12),(25,15),(24,19)],fill=shade(c,-12))
 if wood=='darkened':d.line([(23,22),(23,26),(22,28)],fill=shade(c,-21)) if vertical else d.line([(11,9),(12,11),(16,12)],fill=shade(c,-21))
 return im

def derivative(rel,wood):
 old=template(rel);new=old.copy();oldgrain=template('block/'+wood+'_planks.png');grain=base(wood,'planks');w,h=old.size
 folder=rel.split('/')[0];stem=Path(rel).stem
 if wood=='linden':
  # Flat wood fields in old joinery are separate from its frame/hinge values.
  for y in range(h):
   for x in range(w):
    color=old.getpixel((x,y))
    if 'entity/signs/' in rel:
     new.putpixel((x,y),grain.getpixel((x%16,y%16)));continue
    if folder=='item' and stem.endswith('sign'):
     if color==(217,187,125,255):new.putpixel((x,y),grain.getpixel((x%16,(y-2)%16)))
    elif color==(219,189,127,255):
     # Follow vertical timber grain inside door/trapdoor construction, preserve frame.
     tile=base('linden','stripped_log');c=tile.getpixel((x%16,y%16));new.putpixel((x,y),tuple(min(255,v+2) for v in c[:3])+(255,))
  return new
 if wood=='apple' and folder=='item' and 'sign' in stem:
  for y in range(h):
   for x in range(w):
    if old.getpixel((x,y))==(157,112,92,255):
     c=grain.getpixel(((x-5)%32,(y-8)%32));new.putpixel((x,y),(c[0],min(255,c[1]+2),min(255,c[2]+1),255))
  return new
 for y in range(h):
  for x in range(w):
   if not old.getpixel((x,y))[3]:continue
   if 'entity/signs/' in rel:
    sx,sy=(x%32,y%32) if wood=='apple' else (x//2,y)
   elif folder=='item' and stem.endswith('_sign'):
    if not(1<=x<=14 and 2<=y<=9):continue
    sx,sy=int((x-1+.5)*32/14),int((y-2+.5)*32/8)
   elif folder=='item' and stem.endswith('_door') and wood!='apple':sx,sy=x*2+1,y*2+1
   else:sx,sy=x,y
   sx%=32;sy%=32
   # Preserve alpha holes, outlines, hinges, warm metal and family construction overlays.
   if old.getpixel((x,y))==oldgrain.getpixel((sx,sy)):new.putpixel((x,y),grain.getpixel((sx,sy)))
 return new

@lru_cache(None)
def targets():
 result={}
 for wood in WOODS:
  for kind in ['planks','log','log_top','stripped_log','stripped_log_top']:
   rel='block/'+name(wood,kind)+'.png';result[rel]=(wood,'base',kind)
 with zipfile.ZipFile(ARCHIVE) as z:
  for rel in z.namelist():
   if any(s in Path(rel).stem for s in ['door','sign']) or '/signs/' in rel:
    wood=next((w for w in WOODS if Path(rel).stem.startswith(w+'_') or Path(rel).stem==w),None)
    if wood:result[rel]=(wood,'derived',None)
 # Correct active namespaced WoodType paths (Sheets in installed NeoForge1.21.1).
 for prefix in ['entity/signs/','entity/signs/hanging/']:
  result[prefix+'apple.png']=('apple','alias',prefix+'slavicmyths/apple.png')
 return result

def encoded(rel):
 wood,kind,detail=targets()[rel]
 if kind=='base' and keep_base(wood,detail):return raw(rel)
 im=base(wood,detail) if kind=='base' else derivative(detail if kind=='alias' else rel,wood)
 # Unchanged overlays/templates retain original encoded bytes, not only pixels.
 if kind!='alias' and im.tobytes()==template(rel).tobytes():return raw(rel)
 buffer=BytesIO();im.save(buffer,format='PNG');return buffer.getvalue()

def write_owned(path):
 path=Path(path).resolve();parts=path.parts
 try:index=parts.index('textures');rel=Path(*parts[index+1:]).as_posix()
 except ValueError:return False
 if rel not in targets():return False
 path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(encoded(rel))
 if rel in ['entity/signs/slavicmyths/apple.png','entity/signs/hanging/slavicmyths/apple.png']:
  active=rel.replace('/slavicmyths/apple.png','/apple.png');root=Path(*parts[:index+1]);out=root/active;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(encoded(active))
 return True

def apply(root=TEXTURES):
 for rel in targets():
  path=Path(root)/rel;path.parent.mkdir(parents=True,exist_ok=True);data=encoded(rel)
  if not path.exists() or path.read_bytes()!=data:path.write_bytes(data)

if __name__=='__main__':apply();print('Six wood families and exact dependent surfaces reproduced; no models/data changed.')
