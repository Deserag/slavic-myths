"""Reproduce approved food atlas extraction and Issue363 pepper resources."""
from pathlib import Path
import json,hashlib
from PIL import Image
R=Path(__file__).resolve().parents[1]; A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths'
def write(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
source=R/'docs/concepts/nether-settlement-2.2/food-expansion-2.2.9/art'
raw=(source/'approved_16x_atlas_lossless.webp').read_bytes();assert hashlib.sha256(raw).hexdigest()=='47fb7ac006ae975b5600e5c27d4da63767513497fba6fa4cd5df4eea097c0fff'
atlas=Image.open(source/'approved_16x_atlas_lossless.webp').convert('RGBA')
for row in (source/'ATLAS_MANIFEST.tsv').read_text(encoding='utf-8').splitlines():
 if not row or row.startswith('#'):continue
 name,x,y,w,h=row.split('\t');x,y,w,h=map(int,[x,y,w,h]);folder='block' if '_stage' in name else 'item';p=A/'textures'/folder/(name+'.png');p.parent.mkdir(parents=True,exist_ok=True);atlas.crop((x,y,x+w,y+h)).save(p)
# Four silhouettes: short sprout, leafy stem, flowering bush, ripe hanging pods.
for age in range(4):
 im=Image.new('RGBA',(16,16));px=im.load();top=12-age*3
 for y in range(top,16):px[7,y]=(62,104,43,255)
 for y in range(top+1,15,3):
  for x in range(4,11):px[x,y]=(77,122,48,255)
 if age>=2:
  for x in [5,10]:
   for y in range(top+2,top+4+age):px[x,y]=(190,44,31,255) if age==3 else (207,144,63,255)
 im.save(A/'textures/block'/('crimson_pepper_stage'+str(age)+'.png'))
 write(A/'models/block'/('crimson_pepper_crop_stage'+str(age)+'.json'),{'parent':'minecraft:block/cross','textures':{'cross':'slavicmyths:block/crimson_pepper_stage'+str(age)}})
write(A/'blockstates/crimson_pepper_crop.json',{'variants':{'age='+str(a):{'model':'slavicmyths:block/crimson_pepper_crop_stage'+str(a)} for a in range(4)}})
write(A/'models/item/crimson_pepper.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/crimson_pepper'}})
Image.open(A/'textures/block/crimson_pepper_stage3.png').save(A/'textures/item/crimson_pepper.png')
write(D/'loot_table/blocks/crimson_pepper_crop.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:crimson_pepper','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':2,'max':4},'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:crimson_pepper_crop','properties':{'age':'3'}}]}]}]}]})
