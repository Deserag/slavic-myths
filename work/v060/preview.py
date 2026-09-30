from pathlib import Path
import json,zipfile,io
from PIL import Image,ImageDraw
import numpy as np
root=Path(r'D:\slavic-myths');assets=root/'src/main/resources/assets/slavicmyths'
z=zipfile.ZipFile(r'C:\Users\pavel\.gradle\caches\forge_gradle\minecraft_repo\versions\1.16.5\client.jar')
im=Image.new('RGB',(1280,480),'#dcd6c8');draw=ImageDraw.Draw(im)
files=[assets/'models/block/path_stone.json',assets/'models/block/runic_anvil.json']+list((assets/'models/item').glob('rune_*.json'))
for index,path in enumerate(files):
 m=json.loads(path.read_text());colors={}
 for k,v in m['textures'].items():
  domain,tex=v.split(':');fname=f'assets/{domain}/textures/{tex}.png';assert fname in z.namelist(),fname
  pixel=Image.open(io.BytesIO(z.read(fname))).convert('RGB').resize((1,1)).getpixel((0,0));colors[k]=pixel
 if index<2:cx=150+index*260;cy=315;scale=10;slope=.35
 else:cx=620+(index-2)%4*160;cy=185+(index-2)//4*210;scale=9;slope=.06
 faces=[]
 for e in m['elements']:
  a=np.array(e['from']);b=np.array(e['to']);vertices=np.array([a+(b-a)*np.array(q) for q in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]])
  for fi,(name,ids) in enumerate([('north',[0,1,2,3]),('south',[4,7,6,5]),('down',[0,4,5,1]),('up',[3,2,6,7]),('west',[0,3,7,4]),('east',[1,5,6,2])]):
   q=vertices[ids];color=colors[e['faces'][name]['texture'][1:]];shade=[.7,1,.6,1.1,.8,.85][fi];color=tuple(min(255,int(c*shade)) for c in color)
   pts=[(cx+(x-8+(z-8)*slope)*scale,cy-(y+(z-8)*.2)*scale) for x,y,z in q]
   faces.append((float(np.mean(q[:,2]+q[:,1]*.2)),pts,color))
 for depth,pts,color in sorted(faces,key=lambda f:f[0]):draw.polygon(pts,fill=color)
 draw.text((cx-65,cy+20),path.stem,fill='black')
draw.text((15,450),'Static JSON geometry preview, sampled vanilla materials. No Minecraft client launched.',fill='black')
im.save(root/'work/v060/models.png')
print('PASS: all native model texture references exist in Minecraft 1.16.5 assets')
