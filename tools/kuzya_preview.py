"""Static projection of exported production ModelPart faces and actual UVs, not gameplay."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
R=Path(__file__).resolve().parents[1];data=json.loads((R/'work/kuzya/meshes.json').read_text());out=R/'docs/verification/kuzya-1.6.7';out.mkdir(parents=True,exist_ok=True)
texture=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/kuzya.png').convert('RGB')
panels=[('idle_stand',0,'FRONT'),('idle_stand',90,'SIDE'),('idle_stand',180,'BACK'),('sleep_loop',25,'SLEEP'),('eat_bite',25,'EAT'),('grumble_large',25,'GRUMBLE')]
canvas=Image.new('RGB',(1500,1000),(34,28,25));draw=ImageDraw.Draw(canvas)
for i,(state,yaw,label) in enumerate(panels):
 xoff=i%3*500;yoff=i//3*500;yaw=math.radians(yaw);pitch=math.radians(5)
 def project(v):
  x,y,z=v[:3];y-=24;a=x*math.cos(yaw)+z*math.sin(yaw);b=z*math.cos(yaw)-x*math.sin(yaw)
  return (xoff+250+a*30,yoff+425+(y*math.cos(pitch)-b*math.sin(pitch))*30,b*math.cos(pitch)+y*math.sin(pitch))
 faces=[]
 for cube in data[state]:
  for face in cube['faces']:
   pts=[project(v) for v in face];faces.append((sum(p[2] for p in pts)/4,face,pts))
 def interp(a,b,vs):return [vs[0][k]*(1-a)*(1-b)+vs[1][k]*a*(1-b)+vs[2][k]*a*b+vs[3][k]*(1-a)*b for k in range(len(vs[0]))]
 for _,face,pts in sorted(faces,reverse=True,key=lambda x:x[0]):
  ucount=max(1,int(max(abs(face[0][3]-face[1][3]),abs(face[0][4]-face[1][4]))*256));vcount=max(1,int(max(abs(face[1][3]-face[2][3]),abs(face[1][4]-face[2][4]))*256))
  for a in range(ucount):
   for b in range(vcount):
    uv=interp((a+.5)/ucount,(b+.5)/vcount,face);color=texture.getpixel((max(0,min(255,int(uv[3]*256))),max(0,min(255,int(uv[4]*256)))))
    pixels=[interp(x,y,pts)[:2] for x,y in [(a/ucount,b/vcount),((a+1)/ucount,b/vcount),((a+1)/ucount,(b+1)/vcount),(a/ucount,(b+1)/vcount)]];draw.polygon([tuple(p) for p in pixels],fill=color)
 draw.text((xoff+20,yoff+20),label,fill=(235,221,203));draw.line((xoff+20,yoff+427,xoff+480,yoff+427),fill=(87,71,58));draw.text((xoff+20,yoff+455),'Production rig / static UV projection. NOT IN-GAME.',fill=(185,169,153))
path=out/'native-model-preview.png';canvas.save(path);print(path)
