"""Reproducible native pixel assets and bounded JSON geometry for the approved RC2 pass.
Silhouettes: club = thick octagonal tapered wood head + slim dark grip, 16 units;
axe = 16-unit haft + broad one-sided stepped steel blade, red wrap/socket;
shield = 14-unit round plank disc, negative-Z decorated face, positive-Z rear grip.
Different forms/material patterns, never one generic recolored template.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json, math, random
A=Path('src/main/resources/assets/slavicmyths')
def shade(c,v):return tuple(max(0,min(255,n+v)) for n in c)+(255,)
def save_json(name,data):
 (A/'models/item'/f'{name}.json').write_text(json.dumps(data,indent=2)+'\n')
def box(lo,hi,tile=0):
 return {'from':lo,'to':hi,'faces':{f:{'texture':'#0','uv':[tile*4,0,tile*4+4,16]} for f in ['north','south','west','east','up','down']}}
def transforms():
 d={}
 for hand in ['righthand','lefthand']:
  d['thirdperson_'+hand]={'rotation':[0,-90,0],'translation':[0,2,0],'scale':[.8,.8,.8]}
  d['firstperson_'+hand]={'rotation':[0,-90,15],'translation':[1,2,0],'scale':[.75,.75,.75]}
 d.update(gui={'rotation':[10,-25,-40],'scale':[.9,.9,.9]},ground={'translation':[0,3,0],'scale':[.55,.55,.55]},fixed={'rotation':[0,180,0],'scale':[.8,.8,.8]})
 return d
for name in ['club','battle_axe']:
 im=Image.new('RGBA',(64,16));d=ImageDraw.Draw(im);rng=random.Random(name)
 colors=[(129,87,44),(68,40,26),(111,119,122),(143,41,31)]
 for t,color in enumerate(colors):
  for y in range(16):
   for x in range(16):
    v=rng.randrange(-8,9)+(8 if x%5==1 else -7 if x%5==0 else 0) if t!=2 else rng.randrange(-4,5)
    im.putpixel((16*t+x,y),tuple(max(0,min(255,c+v)) for c in color)+(255,))
  if t==2:
   d.line((t*16,0,t*16+15,0),fill=(206,207,190),width=2);d.point((t*16+7,7),fill=(225,224,206))
  if t==3:
   d.line((t*16+3,4,t*16+12,4),fill=(208,146,86));d.line((t*16+7,1,t*16+7,13),fill=(208,146,86))
 im.save(A/'textures/item'/f'{name}.png')
 if name=='club':
  e=[box([7,0,7],[9,9,9],1),box([6,7,6],[10,16,10]),box([5.5,9,6.5],[10.5,15,9.5]),box([6.5,9,5.5],[9.5,15,10.5])]
  for y in [9,13]:
   e.append(box([5.4,y,5.4],[10.6,y+.7,10.6],2))
   for x,z in [(5.1,7.5),(10.1,7.5),(7.5,5.1)]:e.append(box([x,y+.1,z],[x+.8,y+.9,z+.8],2))
  for y in [1,3,5]:e.append(box([6.9,y,6.9],[9.1,y+.5,9.1],1))
 else:
  e=[box([7.2,0,7.2],[8.8,16,8.8]),box([6.7,10,6.6],[9.3,14.5,9.4],2)]
  for y in [1,3,6,9]:e.append(box([7,y,7],[9,y+.8,9],3 if y>4 else 1))
  # Solid connected stepped crescent; steel cutting edge at the left, socket at the right.
  for y,x0,x1 in [(9,4.5,7),(10,3.2,7),(11,2.4,7),(12,2,7),(13,2.2,7),(14,2.8,7),(15,4,7)]:
   blade=box([x0,y,7.3],[x1,y+1,8.7],2)
   for face in ['north','south']:blade['faces'][face]['uv']=[8+(x0-2)/7*4,(16-y-1)/7*16,8+(x1-2)/7*4,(16-y)/7*16]
   e.append(blade)
  e.append(box([4.5,11.2,7.15],[6.2,13.4,7.3],3));e.append(box([4.5,11.2,8.7],[6.2,13.4,8.85],3))
 save_json(name,{'credit':'Approved RC2 silhouette; normalized UV; connected bounded cuboids','textures':{'0':f'slavicmyths:item/{name}','particle':f'slavicmyths:item/{name}'},'gui_light':'front','elements':e,'display':transforms()})
# Four explicit 64px material tiles in the legacy 256px atlas: red front, wood rear, iron rim, boss.
shield=Image.new('RGBA',(256,256),(0,0,0,0));d=ImageDraw.Draw(shield);rng=random.Random('retainer-shield-rc2')
for y in range(64):
 for x in range(256):
  tile=x//64;px=x%64;base=[(148,40,29),(121,80,42),(113,117,116),(136,141,141)][tile];v=rng.randrange(-7,8)
  if tile in [0,1] and px%11==0:v-=20
  shield.putpixel((x,y),shade(base,v))
# Geometric linen-white solar motif entirely on the enemy-facing red tile.
cream=(228,218,182,255)
d.polygon([(32,5),(40,14),(32,23),(24,14)],outline=cream,width=3)
d.polygon([(32,41),(40,50),(32,59),(24,50)],outline=cream,width=3)
d.polygon([(5,32),(14,24),(23,32),(14,40)],outline=cream,width=3)
d.polygon([(41,32),(50,24),(59,32),(50,40)],outline=cream,width=3)
d.line((32,17,32,47),fill=cream,width=3);d.line((17,32,47,32),fill=cream,width=3)
for x in range(66,128,11):d.line((x,0,x,63),fill=(69,43,27,255));d.line((x+1,0,x+1,63),fill=(159,110,61,255))
for y in [0,63]:d.line((128,y,191,y),fill=(177,178,164,255),width=2)
d.ellipse((195,3,252,60),fill=(89,97,99,255),outline=(211,212,194,255),width=3);d.ellipse((205,13,242,50),fill=(151,157,157,255),outline=(195,199,190,255),width=2)
shield.save(A/'textures/item/retainer_shield.png')
# Preserve connected round geometry and blocking predicate, correct physical held axes.

m=json.loads((A/'models/item/retainer_shield.json').read_text())
for hand in ['righthand','lefthand']:
 m['display']['thirdperson_'+hand]={'rotation':[0,-90,0],'translation':[2,3,2],'scale':[1,1,1]}
 m['display']['firstperson_'+hand]={'rotation':[0,0,5],'translation':[4,1,-2],'scale':[.9,.9,.9]}
m['credit']='RC2: decorated face negative Z, grip positive Z; held axes corrected 180 degrees'
save_json('retainer_shield',m)
b=json.loads((A/'models/item/retainer_shield_blocking.json').read_text())
for hand in ['righthand','lefthand']:
 b['display']['firstperson_'+hand]={'rotation':[0,0,-5],'translation':[0,3,0],'scale':[.9,.9,.9]}
 b['display']['thirdperson_'+hand]={'rotation':[45,-25,0],'translation':[1,4,2],'scale':[1,1,1]}
save_json('retainer_shield_blocking',b)
# Unique native 32px wood structures, per-family seeds/forms instead of palette substitutions.
pal={'willow':[(147,153,119),(204,202,157),(178,177,134)],'rowan':[(129,107,94),(201,145,117),(169,103,73)],'pine':[(85,75,65),(200,153,82),(161,117,65)],'darkened':[(46,42,38),(74,62,52),(57,48,39)]}
def shade(c,v):return tuple(max(0,min(255,n+v)) for n in c)+(255,)
for family,(bark,core,plank) in pal.items():
 for kind in ['log','stripped_log','planks','log_top','stripped_log_top']:
  rng=random.Random(family+kind);base=bark if kind=='log' else plank if kind=='planks' else core
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
  for y in range(32):
   for x in range(32):im.putpixel((x,y),shade(base,rng.randrange(-5,6)))
  if kind.endswith('top'):
   # Different centers/ring cadence; retained wood rather than flat concrete.
   cx,cy={'willow':(15,17),'rowan':(17,14),'pine':(14,14),'darkened':(16,18)}[family]
   for r in range(3,24,3 if family in ['pine','rowan'] else 4):d.ellipse((cx-r,cy-r,cx+r,cy+r),outline=shade(base,-18 if family in ['pine','rowan'] else -9))
   if kind=='log_top':
    d.rectangle((0,0,31,31),outline=shade(bark,-10),width=2)
   if family=='darkened':d.line((3,2,14,15,10,28),fill=shade(base,-22),width=1)
  elif kind=='log':
   if family=='willow':
    for x in range(1,32,3):d.line([(x,y) if y%8<4 else (x+1,y) for y in range(32)],fill=shade(base,-23 if x%2 else 14))
    d.line((25,0,24,15,25,31),fill=(78,103,58,255))
   elif family=='rowan':
    for x,y,length in [(2,3,5),(18,8,8),(6,15,6),(23,21,5),(11,27,9)]:
     d.line((x,y,x+length,y),fill=shade(base,-38),width=1);d.line((x+1,y+1,x+length-1,y+1),fill=shade(base,16))
   elif family=='pine':
    for x in range(0,32,7):
     for y in range((x//7)%2*5,32,10):d.rectangle((x,y,min(31,x+5),min(31,y+8)),outline=shade(base,-25));d.line((x+1,y+1,x+4,y+1),fill=shade(base,25))
   else:
    for x in range(2,32,6):d.line((x,0,x+2,9,x-1,18,x+1,31),fill=shade(base,18),width=1)
    for y in [6,18,27]:d.line((0,y,31,y+2),fill=shade(base,-14))
  else:
   horizontal=kind=='planks'
   # Planks have continuous full-width boards, no brick-style staggered cross-joints.
   if horizontal:
    for y in [7,15,23]:d.line((0,y,31,y),fill=shade(base,-28));d.line((0,y+1,31,y+1),fill=shade(base,12))
   spacing={'willow':3,'rowan':5,'pine':4,'darkened':6}[family]
   for j in ([2,5,10,13,18,21,26,29] if horizontal else range(1,32,spacing)):
    points=[]
    for i in range(32):
     shift=round(math.sin((i+j)*(.24 if family=='rowan' else .15))* (1 if horizontal else 2 if family=='rowan' else 1))
     points.append((i,(j+shift)%32) if horizontal else ((j+shift)%32,i))
    d.line(points,fill=shade(base, -8 if family=='willow' else -17))
   if family in ['rowan','pine']:
    x,y=(22,19) if family=='pine' else (10,12);d.ellipse((x-3,y-2,x+3,y+2),outline=shade(base,-35));d.point((x,y),fill=shade(base,-44))
   if family=='pine' and not horizontal:d.line((24,4,23,26),fill=shade(base,24),width=2)
   if family=='darkened':d.line((1,2,2,12,1,26),fill=shade(base,-23))
  im.save(A/'textures/block'/f'{family}_{kind}.png')
 # Doors and trapdoors use distinct family construction and the same authored grain.
 grain=Image.open(A/'textures/block'/f'{family}_planks.png')
 door=Image.new('RGBA',(32,64));door.paste(grain,(0,0));door.paste(grain,(0,32));d=ImageDraw.Draw(door)
 for x in [1,30]:d.line((x,0,x,63),fill=shade(plank,-35),width=2)
 for y in ([10,48] if family=='willow' else [15,44] if family=='rowan' else [8,52] if family=='pine' else [5,31,58]):d.rectangle((2,y,29,y+2),fill=shade(plank,-32))
 if family=='willow':
  d.rectangle((8,6,23,25),fill=(0,0,0,0));d.line((15,6,15,25),fill=shade(plank,-15),width=2)
 elif family=='rowan':d.polygon([(16,5),(24,15),(16,25),(8,15)],outline=shade(plank,30))
 elif family=='pine':d.line((3,60,28,20),fill=shade(plank,-25),width=3)
 else:
  for y in [13,50]:d.rectangle((2,y,29,y+3),fill=(77,77,70,255))
 d.rectangle((25,34,27,37),fill=(160,151,111,255))
 door.crop((0,0,32,32)).save(A/'textures/block'/f'{family}_door_top.png');door.crop((0,32,32,64)).save(A/'textures/block'/f'{family}_door_bottom.png')
 icon=door.resize((16,32),Image.Resampling.NEAREST);icon.save(A/'textures/item'/f'{family}_door.png')
 trap=grain.copy();d=ImageDraw.Draw(trap);d.rectangle((0,0,31,31),outline=shade(plank,-30),width=3)
 holes=[(5,5,12,12),(19,5,26,12),(5,19,12,26),(19,19,26,26)] if family!='rowan' else [(7,7,24,24)]
 for bounds in holes:d.rectangle(bounds,fill=(0,0,0,0))
 if family=='willow':d.line((15,3,15,28),fill=shade(plank,-10),width=2)
 if family=='darkened':d.rectangle((1,14,30,17),fill=(74,76,71,255))
 trap.save(A/'textures/block'/f'{family}_trapdoor.png')
 if family!='darkened':
  sign=grain.resize((64,32),Image.Resampling.NEAREST);sign.save(A/'textures/entity/signs'/f'{family}.png')
  sign_icon=Image.new('RGBA',(16,16));sign_icon.paste(grain.resize((14,8),Image.Resampling.NEAREST),(1,2));d=ImageDraw.Draw(sign_icon);d.rectangle((7,10,8,15),fill=shade(plank,-20));sign_icon.save(A/'textures/item'/f'{family}_sign.png')
print('RC2 native resources regenerated: 3 weapon models, shield transforms, 4 distinct wood families')
