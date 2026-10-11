"""Offline orthographic view of the exported native cubes and UVs. No game or renderer."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
root=Path(__file__).resolve().parent.parent;data=json.loads((root/'work/mobs/model-meshes.json').read_text());out=Image.new('RGB',(1200,620),(27,30,34));draw=ImageDraw.Draw(out)
for i,(name,cubes) in enumerate(data.items()):
 texture=Image.open(root/f'src/main/resources/assets/slavicmyths/textures/entity/underground/{name}.png').convert('RGB');scale=.825 if name=='podzemnik' else .88 if name=='podzemnik_miner' else 1
 yaw=math.radians(24);pitch=math.radians(9)
 def project(v):
  x,y,z=v[:3];x*=scale;y=(y-24)*scale;z*=scale
  a=x*math.cos(yaw)+z*math.sin(yaw);b=z*math.cos(yaw)-x*math.sin(yaw)
  return (i*300+150+a*9,540+(y*math.cos(pitch)-b*math.sin(pitch))*9,b*math.cos(pitch)+y*math.sin(pitch))
 faces=[]
 for cube in cubes:
  if 'held_stone' in cube['part']:continue
  for face in cube['faces']:
   pts=[project(v) for v in face];faces.append((sum(p[2] for p in pts)/4,face,pts))
 for _,face,pts in sorted(faces,reverse=True,key=lambda x:x[0]):
  ucount=max(1,int(max(abs(face[0][3]-face[1][3]),abs(face[0][4]-face[1][4]))*128));vcount=max(1,int(max(abs(face[1][3]-face[2][3]),abs(face[1][4]-face[2][4]))*128))
  def interp(a,b,vals):return [vals[0][k]*(1-a)*(1-b)+vals[1][k]*a*(1-b)+vals[2][k]*a*b+vals[3][k]*(1-a)*b for k in range(len(vals[0]))]
  for a in range(ucount):
   for b in range(vcount):
    pa,pb=(a+.5)/ucount,(b+.5)/vcount;uv=interp(pa,pb,face);color=texture.getpixel((max(0,min(127,int(uv[3]*128))),max(0,min(127,int(uv[4]*128)))))
    pixels=[interp(x,y,pts)[:2] for x,y in [(a/ucount,b/vcount),((a+1)/ucount,b/vcount),((a+1)/ucount,(b+1)/vcount),(a/ucount,(b+1)/vcount)]]
    draw.polygon([tuple(p) for p in pixels],fill=color)
 draw.text((i*300+20,20),name,fill=(230,219,196));draw.line((i*300+10,541,i*300+290,541),fill=(89,87,80))
draw.text((20,584),'Native mesh/UV offline preview; not Minecraft gameplay or shader validation',fill=(184,184,184));out.save(root/'work/mobs/native-model-preview.png');print(root/'work/mobs/native-model-preview.png')
