"""Scoped #88 authored atlas overlay. No registry/gameplay or accessory writes."""
from pathlib import Path
from PIL import Image,ImageDraw
import armor_revision_assets as old
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths'
CLOTHES=['linen_headscarf','linen_shirt','linen_ports','bast_shoes','sarafan','linen_apron','folk_vest']
def save(im,p):
 import io
 b=io.BytesIO();im.save(b,format='PNG');p.parent.mkdir(parents=True,exist_ok=True)
 if not p.exists() or p.read_bytes()!=b.getvalue():p.write_bytes(b.getvalue())
def apply_clothing():
 for name in CLOTHES:
  base={'linen_ports':'#af9f82','bast_shoes':'#ad8041','sarafan':'#742126','folk_vest':'#5b3222'}.get(name,'#ded4ba')
  im=Image.new('RGBA',(128,128),base);d=ImageDraw.Draw(im)
  if name=='linen_shirt':
   d.line((19,5,19,13),fill='#b5aa90');d.point((20,9),fill='#f0e6cd')
   for x in [4,36]:
    d.line((x,12,x+7,12),fill='#972724')
    for dx in range(0,8,3):d.rectangle((x+dx,13,x+dx+1,13),fill='#972724')
   d.line([(5,4),(5,6),(10,6),(10,4)],fill='#972724')
  elif name=='linen_ports':
   d.rectangle((36,48,47,49),fill='#897557');d.line((37,28,37,33),fill='#c5b695');d.line((45,28,45,33),fill='#958163')
   d.rectangle((4,4,11,5),fill='#766147');d.point((8,4),fill='#d4bd81')
  elif name=='linen_headscarf':
   for x,y,w in [(65,1,9),(65,21,9),(64,41,10),(64,47,8),(64,53,8)]:
    d.line((x,y,x+w-1,y),fill='#972724')
    for dx in range(1,w,3):d.point((x+dx,y+1),fill='#972724')
  elif name=='bast_shoes':
   for y in range(0,24,3):
    for x in range((y//3%2),26,3):d.line((x,y,x+1,y+1),fill='#deaf61');d.point((x+2,y+1),fill='#845629')
   d.rectangle((32,0,63,10),fill='#66472b');d.line((38,6,43,6),fill='#a27b44')
  elif name=='sarafan':
   for x in [32,48]:d.rectangle((x,0,x+12,20),fill='#b84942')
   for y in [28,44,60]:
    d.line((0,y,23,y),fill='#e4cba2')
    for x in range(1,24,4):d.point((x,y-1),fill='#e4cba2')
   d.line((18,5,18,12),fill='#501e25')
  elif name=='linen_apron':
   d.rectangle((32,0,62,20),fill='#673928');d.line((0,30,12,30),fill='#972724');d.line((3,5,3,10),fill='#c7bda2')
  elif name=='folk_vest':
   d.line((5,1,5,9),fill='#80533c');d.line((11,1,11,9),fill='#80533c')
   for x in [32,35,48,51,64,80]:d.line((x,1,x,10),fill='#9c2f27')
  save(im,A/f'textures/entity/clothing/{name}.png')
def apply_armor():
 for kind in ['gambeson','plate','bogatyr']:
  im=old.standard_atlas(kind);d=ImageDraw.Draw(im)
  base,hi,shade={'gambeson':('#b4a27d','#e4d3a3','#8a785d'),'plate':('#89939a','#c2d2d7','#4d5864'),'bogatyr':('#603f36','#b35d53','#3c2d2c')}[kind]
  d.rectangle((16,16,63,31),fill=base)
  if kind=='gambeson':
   for x in [21,24,27,33,36,45,48,53]:
    for y in [21,25,29]:d.line((x,y,x+1,y+1),fill=shade);d.point((x+1,y),fill=hi)
   d.rectangle((0,0,23,9),fill='#76503a');d.point((6,5),fill='#c6a05a')
  elif kind=='plate':
   d.rectangle((16,0,36,15),fill='#89939a');d.line((18,2,23,2),fill=hi);d.line((18,3,18,8),fill=hi);d.line((24,3,24,8),fill=shade)
   for x,y in [(18,3),(23,3),(18,8),(23,8)]:d.point((x,y),fill='#c6a05a')
   d.rectangle((0,0,15,9),fill='#574132');d.rectangle((40,0,63,10),fill=base);d.line((45,5,49,5),fill=hi)
   for x in [21,34]:d.line((x,21,x,29),fill=hi)
  else:
   # One broad steel panel island and leather/red backing, no all-width prison bands.
   d.rectangle((0,0,23,15),fill='#71818d');d.line((1,1,7,1),fill='#c3ced4');d.line((1,2,1,8),fill='#c3ced4');d.line((8,2,8,8),fill='#37434e')
   for x in [2,6]:d.point((x,3),fill='#c5a05b')
   d.rectangle((48,0,55,7),fill='#c5a05b');d.point((50,2),fill='#f0d598')
   d.line((34,21,34,29),fill='#8f393f')
  path=A/(f'textures/armor/unique_{kind}_layer_1.png' if kind=='bogatyr' else f'textures/models/armor/{kind}_layer_1.png');save(im,path)
 # Keep the existing successful Perunite mesh; isolate one readable lightning, not repeated bands.
 for layer in [1,2]:
  im=old.standard_atlas('perunite',layer==2);d=ImageDraw.Draw(im)
  if layer==1:
   d.rectangle((17,1,27,10),fill='#4e7189');d.line([(22,2),(19,5),(22,5),(20,9)],fill='#d7eff1',width=1)
   d.rectangle((20,20,39,28),fill='#4e7189');d.line((21,20,21,27),fill='#a4d6de');d.line((37,20,37,27),fill='#2e455b')
   d.rectangle((5,24,10,25),fill='#2e455b');d.rectangle((7,22,8,22),fill='#c6a05a')
  else:
   for x in [4,12]:d.rectangle((x,21,x+3,23),fill='#a4d6de');d.line((x,24,x+3,24),fill='#2e455b')
  save(im,A/f'textures/models/armor/perunite_layer_{layer}.png')
 for family in ['dragon','poloz']:
  im=old.scale_atlas(family);d=ImageDraw.Draw(im)
  if family=='dragon':
   d.rectangle((48,48,63,63),fill='#733535');d.polygon([(51,49),(54,53),(51,57),(49,53)],fill='#bc6647');d.point((51,52),fill='#f0b773')
   d.line((43,1,43,13),fill='#e1d1b4');d.line((44,2,44,12),fill='#b4a48a')
  else:
   d.rectangle((0,48,15,55),fill='#b59859');d.line([(1,49),(3,51),(5,49)],fill='#edcf88');d.line((1,52,5,52),fill='#756342')
   d.rectangle((48,48,63,63),fill='#8d3837');d.polygon([(50,49),(52,51),(51,55),(49,52)],fill='#d77753');d.point((50,50),fill='#f2b775')
  save(im,A/f'textures/models/armor/{family}.png')
 for kind,name in [('idol','trophy_idol_chest'),('marinka','trophy_marinka_cloak'),('kartaus','trophy_kartaus_mantle')]:
  im=old.reward_atlas(kind);d=ImageDraw.Draw(im)
  if kind=='idol':
   d.rectangle((0,20,23,27),fill='#d6c7a2');d.line((1,21,6,21),fill='#f0e5c4');d.line((1,23,6,23),fill='#978469')
  else:
   base='#514064' if kind=='marinka' else '#485747'
   for x in [0,26]:
    d.rectangle((x,48,x+14,80),fill=base);d.line((x+1,49,x+1,77),fill='#8c769e' if kind=='marinka' else '#657561')
    d.line((x+2,61,x+5,61),fill='#b8c7d2' if kind=='marinka' else '#c6a05a')
   if kind=='kartaus':d.rectangle((60,48,67,70),fill='#933d39');d.rectangle((62,50,63,51),fill='#c6a05a')
  save(im,A/f'textures/models/armor/{name}.png')
 # Travelling boot atlas: red cuffs/straps and dark sole, no lateral feathers.
 im=old.standard_atlas('seven_league');d=ImageDraw.Draw(im);d.rectangle((0,0,23,10),fill='#ac4f3f');d.rectangle((24,0,47,9),fill='#382c25');d.point((7,19),fill='#c6a05a');save(im,A/'textures/armor/seven_league.png')
 # Plate icon reflects a single forged breastplate; other valid icons are kept.
 im=old.icon('plate_cuirass').resize((16,16),Image.Resampling.NEAREST);d=ImageDraw.Draw(im);d.rectangle((5,6,10,11),fill='#89939a');d.line((6,6,6,10),fill='#c2d2d7');d.point((10,7),fill='#c6a05a');save(im.resize((32,32),Image.Resampling.NEAREST),A/'textures/item/plate_cuirass.png')
def apply():apply_clothing();apply_armor()
if __name__=='__main__':apply()
