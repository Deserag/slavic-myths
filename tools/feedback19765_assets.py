"""Exact vanilla iron silhouettes, with a perunite metal palette; original serpent trophy sprite."""
from pathlib import Path
import io,json,zipfile
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths'
NATIVE=Path('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
def generate():
 with zipfile.ZipFile(NATIVE) as jar:
  for family in ['pickaxe','axe','shovel','hoe']:
   native=Image.open(io.BytesIO(jar.read('assets/minecraft/textures/item/iron_'+family+'.png'))).convert('RGBA')
   result=native.copy();palette={24:(27,49,60),68:(47,86,108),150:(74,129,157),193:(108,174,203),216:(159,210,230),255:(221,244,252)}
   for y in range(16):
    for x in range(16):
     r,g,b,alpha=native.getpixel((x,y))
     if alpha and r==g==b:result.putpixel((x,y),(*palette[r],alpha))
   # Handle pixels, dimensions and every alpha bit are identical to the vanilla source.
   assert result.getchannel('A').tobytes()==native.getchannel('A').tobytes()
   result.save(A/'textures/item'/('perunite_'+family+'.png'))
   model={'parent':'minecraft:item/handheld','textures':{'layer0':'slavicmyths:item/perunite_'+family}}
   (A/'models/item'/('perunite_'+family+'.json')).write_text(json.dumps(model,indent=2)+'\n')
 poloz_sprite().save(A/'textures/item/poloz_head.png')
def poloz_sprite():
 # Low side profile, broad scaled jaw, nostril and forked tongue; no ears or cat muzzle.
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 d.polygon([(2,22),(4,13),(10,7),(20,6),(26,10),(30,15),(29,23),(23,27),(11,26)],fill='#283c2d')
 d.polygon([(5,20),(7,13),(12,9),(20,9),(25,12),(28,16),(26,21),(17,23),(9,22)],fill='#697e43')
 d.polygon([(5,22),(13,24),(24,23),(28,21),(26,25),(22,27),(11,26)],fill='#b8ae72')
 d.line([(7,21),(16,22),(26,20)],fill='#1c3027',width=2)
 d.polygon([(10,13),(17,11),(18,15),(12,16)],fill='#d9ba5b');d.line((14,12,14,15),fill='#182a24',width=2)
 d.rectangle((25,15,26,16),fill='#1b2e24');d.line([(27,22),(30,24),(31,23)],fill='#a45b60');d.line((30,24,31,26),fill='#a45b60')
 for x,y in [(9,10),(19,9),(22,12),(7,17),(18,18),(22,18)]:d.line([(x-1,y),(x,y+1),(x+2,y)],fill='#899450')
 return im
if __name__=='__main__':generate()
