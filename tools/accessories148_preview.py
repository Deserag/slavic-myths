"""Current actual GUI model/UV/PNG reference sheet. No art modifications or game."""
from pathlib import Path
import copy,hashlib,io,json,zipfile
from PIL import Image,ImageDraw
from trophy111_source_preview import render,CLIENT
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths';OUT=ROOT/'docs/implementation/accessories-148/audit/gui-before';OUT.mkdir(exist_ok=True)
BEFORE=ROOT/'docs/implementation/accessories-148/before'
def original(p):
 b=BEFORE/p.relative_to(ROOT)
 return b if b.exists() else p
IDS=['perun_ring','resin_ring','veles_amulet','depth_amulet','retribution_charm','hunter_belt','invisibility_cap','lunula','grivna','grave_ward']
def model(name):
 p=A/'models'/(name.removeprefix('slavicmyths:')+'.json');d=json.loads(original(p).read_text());d=d.get('perspectives',{}).get('gui',d)
 parent=d.get('parent','')
 if parent.startswith('slavicmyths:'):
  b=model(parent);b.update(d);return b
 return d
sheet=Image.new('RGB',(600,38+len(IDS)*84),(48,48,48));draw=ImageDraw.Draw(sheet);draw.text((8,8),'SOURCE GUI BEFORE / x1 and x4 NEAREST / not a game screenshot',fill='white');receipts=[]
with zipfile.ZipFile(CLIENT) as vanilla:
 for row,id in enumerate(IDS):
  d=model('slavicmyths:item/'+id);textures=d['textures']
  def ref(key):
   while key.startswith('#'):key=textures[key[1:]]
   return key
  refs=sorted({ref(v) for v in textures.values()});imgs=[]
  for name in refs:
   namespace,path=name.split(':');data=original(A/'textures'/(path+'.png')).read_bytes() if namespace=='slavicmyths' else vanilla.read('assets/minecraft/textures/'+path+'.png');imgs.append(Image.open(io.BytesIO(data)).convert('RGBA'))
  if d.get('parent')=='minecraft:item/generated':im=imgs[refs.index(ref(textures['layer0']))].resize((16,16),Image.Resampling.NEAREST)
  else:
   size=max(max(i.size) for i in imgs);atlas=Image.new('RGBA',(size*len(refs),size))
   for index,i in enumerate(imgs):atlas.paste(i.resize((size,size),Image.Resampling.NEAREST),(size*index,0))
   d=copy.deepcopy(d)
   for e in d['elements']:
    x0,y0,z0=e['from'];x1,y1,z1=e['to'];default={'west':[z0,16-y1,z1,16-y0],'east':[16-z1,16-y1,16-z0,16-y0],'down':[x0,16-z1,x1,16-z0],'up':[x0,z0,x1,z1],'north':[16-x1,16-y1,16-x0,16-y0],'south':[x0,16-y1,x1,16-y0]}
    for side,f in e['faces'].items():
     assert not f.get('rotation'),(id,side)
     index=refs.index(ref(f['texture']));u,v,s,t=f.get('uv',default[side]);f['uv']=[(u+16*index)/len(refs),v,(s+16*index)/len(refs),t]
   im,pose=render(d,atlas,pixels=16)
  im.save(OUT/(id+'.png'));y=38+row*84;draw.text((8,y+18),id,fill='white');sheet.paste(im,(240,y+24),im);large=im.resize((64,64),Image.Resampling.NEAREST);sheet.paste(large,(280,y),large)
  receipts.append({'id':id,'file':id+'.png','textures':refs,'limitations':'orthographic source projection; no engine AO/shaders; x4 is NEAREST of x1; not runtime'})
sheet.save(OUT/'sheet.png');(OUT/'sources.json').write_text(json.dumps(receipts,indent=2)+'\n')
print('source previews: '+str(len(receipts)))
