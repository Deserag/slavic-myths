"""Orthographic rendering of exported native cuboids and actual UV atlas. No game launch."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
root=Path(__file__).resolve().parents[1];data=json.loads((root/'work/earth/meshes.json').read_text());out=Image.new('RGB',(1350,850),(27,30,34));draw=ImageDraw.Draw(out)
for i,(name,yaw) in enumerate([('boss',24),('boss',90),('boss',180)]):
 texture=Image.open(root/'src/main/resources/assets/slavicmyths/textures/entity/earth_spirit/phase_1.png').convert('RGB');yaw=math.radians(yaw);pitch=math.radians(8)
 def project(v):
  x,y,z=v[:3];y-=24;a=x*math.cos(yaw)+z*math.sin(yaw);b=z*math.cos(yaw)-x*math.sin(yaw)
  return (i*450+225+a*5,740+(y*math.cos(pitch)-b*math.sin(pitch))*5,b*math.cos(pitch)+y*math.sin(pitch))
 faces=[]
 for cube in data[name]:
  if '/armor_' in cube['part']:continue
  for face in cube['faces']:
   pts=[project(v) for v in face];faces.append((sum(p[2] for p in pts)/4,face,pts))
 for _,face,pts in sorted(faces,reverse=True,key=lambda x:x[0]):
  ucount=max(1,int(max(abs(face[0][3]-face[1][3]),abs(face[0][4]-face[1][4]))*256));vcount=max(1,int(max(abs(face[1][3]-face[2][3]),abs(face[1][4]-face[2][4]))*256))
  def interp(a,b,vals):return [vals[0][k]*(1-a)*(1-b)+vals[1][k]*a*(1-b)+vals[2][k]*a*b+vals[3][k]*(1-a)*b for k in range(len(vals[0]))]
  for a in range(ucount):
   for b in range(vcount):
    uv=interp((a+.5)/ucount,(b+.5)/vcount,face);color=texture.getpixel((max(0,min(255,int(uv[3]*256))),max(0,min(255,int(uv[4]*256)))))
    pixels=[interp(x,y,pts)[:2] for x,y in [(a/ucount,b/vcount),((a+1)/ucount,b/vcount),((a+1)/ucount,(b+1)/vcount),(a/ucount,(b+1)/vcount)]];draw.polygon([tuple(p) for p in pixels],fill=color)
 draw.text((i*450+20,30),['FRONT 24 deg','SIDE','BACK'][i],fill=(230,219,196));draw.line((i*450+10,741,i*450+440,741),fill=(89,87,80))
draw.text((20,805),'Native mesh and UV preview: static pose only, not an in-game animation test.',fill=(184,184,184));path=root/'work/earth/native-preview.png';out.save(path);print(path)
