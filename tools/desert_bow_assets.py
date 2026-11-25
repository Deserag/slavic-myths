"""Vanilla bow artwork, transforms and draw predicates with a warm desert material palette."""
import zipfile,json,io,os
from pathlib import Path
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths'
def main():
 jar=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
 with zipfile.ZipFile(jar) as z:
  for suffix in ['', '_pulling_0','_pulling_1','_pulling_2']:
   im=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/bow'+suffix+'.png'))).convert('RGBA')
   for y in range(im.height):
    for x in range(im.width):
     r,g,b,a=im.getpixel((x,y))
     if a and max(r,g,b)-min(r,g,b)>12:
      l=(r*.3+g*.59+b*.11)/255;im.putpixel((x,y),(int(160+95*l),int(105+130*l),int(40+150*l),a))
   im.save(A/f'textures/item/desert_weapons/mirage_bow{suffix}.png')
   model=json.loads(z.read('assets/minecraft/models/item/bow'+suffix+'.json'));model['textures']={'layer0':f'slavicmyths:item/desert_weapons/mirage_bow{suffix}'}
   if suffix:model['parent']='slavicmyths:item/mirage_bow'
   else:
    for i,v in enumerate(model['overrides']):v['model']='slavicmyths:item/mirage_bow_pulling_'+str(i)
   (A/f'models/item/mirage_bow{suffix}.json').write_text(json.dumps(model,indent=2)+'\n')
if __name__=='__main__':main()
