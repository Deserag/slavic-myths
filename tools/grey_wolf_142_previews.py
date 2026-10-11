"""Asset contact sheet and native baked geometry projections; these are NOT game screenshots."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths';OUT=ROOT/'docs/verification/grey_wolf_1.4.2/previews'
OUT.mkdir(parents=True,exist_ok=True)
names=['ring_of_grey_pack','finist_bow','finist_bow_pulling_0','finist_bow_pulling_1','finist_bow_pulling_2','finist_quiver']
im=Image.new('RGB',(960,390),'#282B30');d=ImageDraw.Draw(im)
for i,name in enumerate(names):
 src=Image.open(A/f'textures/item/{name}.png').convert('RGBA');big=src.resize((128,128),Image.Resampling.NEAREST);im.paste(big,(i*160+16,30),big);gray=src.convert('LA').convert('RGBA').resize((96,96),Image.Resampling.NEAREST);im.paste(gray,(i*160+32,205),gray);d.text((i*160+5,170),name.replace('finist_',''),fill='white');d.text((i*160+32,320),f'{len(set(src.getdata()))-1} colors',fill='white')
im.save(OUT/'item-sprites.png')
geo=json.loads((OUT/'quiver-native-geometry.json').read_text());im=Image.new('RGB',(990,350),'#282B30');d=ImageDraw.Draw(im)
for column,(yaw,lean,label) in enumerate([(0,0,'BACK / armor body'),(.65,0,'THREE QUARTER / armor body'),(1.1,.5,'SIDE / crouch approximation')]):
 def project(x,y,z):
  y,z=y*math.cos(lean)-z*math.sin(lean),y*math.sin(lean)+z*math.cos(lean)
  x,z=x*math.cos(yaw)-z*math.sin(yaw),x*math.sin(yaw)+z*math.cos(yaw)
  return column*330+165+round(x*13),100+round(y*13+z*3),z
 boxes=[{'part':'armor','bounds':[-5,-1,-3,5,13,3]}]+geo;faces=[]
 for box in boxes:
  a,b,c,x,y,z=box['bounds'];verts=[(a,b,c),(x,b,c),(x,y,c),(a,y,c),(a,b,z),(x,b,z),(x,y,z),(a,y,z)];p=[project(*v)for v in verts]
  base='#785638'if box['part']=='pouch'else'#3F2C21'if 'strap'in box['part']or box['part']=='rim'else'#A1A7AA'if box['part']=='emblem'else'#C7C5B6'if'feather'in box['part']else'#B88D60'if'arrow'in box['part']else'#707782'
  for indices in [(0,1,2,3),(4,5,6,7),(0,4,7,3),(1,5,6,2),(0,1,5,4),(3,2,6,7)]:faces.append((sum(p[i][2]for i in indices)/4,[(p[i][0],p[i][1])for i in indices],base))
 for _,points,color in sorted(faces):d.polygon(points,fill=color,outline='#30343A')
 d.text((column*330+15,20),label,fill='white')
d.text((15,330),'Offline projections of actual native cubes. Client armor/lighting/animation QA remains pending.',fill='white');im.save(OUT/'quiver-three-views.png')
print('Generated source-sprite and native-geometry previews; no client launched')
