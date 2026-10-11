"""Actual native posed mesh and UVs; a static preview, never an in-game screenshot."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
R=Path(__file__).resolve().parents[1];data=json.loads((R/'work/pesigolovtsy/meshes.json').read_text());out=R/'docs/verification/pesigolovtsy-1.6.5';out.mkdir(parents=True,exist_ok=True)
def sheet(panels,name):
 canvas=Image.new('RGB',(1600,550*math.ceil(len(panels)/4)),(35,41,50));draw=ImageDraw.Draw(canvas)
 for i,(role,state,rotation) in enumerate(panels):
  texture=Image.open(R/f'src/main/resources/assets/slavicmyths/textures/entity/pesigolovtsy/{role.lower()}.png').convert('RGB');xoff=i%4*400;yoff=i//4*550;yaw=math.radians(rotation);pitch=math.radians(5)
  def project(v):
   x,y,z=v[:3];y-=24;a=x*math.cos(yaw)+z*math.sin(yaw);b=z*math.cos(yaw)-x*math.sin(yaw);return (xoff+200+a*9,yoff+450+(y*math.cos(pitch)-b*math.sin(pitch))*9,b*math.cos(pitch)+y*math.sin(pitch))
  faces=[]
  for cube in data[role+'_'+state]:
   for face in cube['faces']:
    pts=[project(v) for v in face];faces.append((sum(p[2] for p in pts)/4,face,pts))
  def interp(a,b,vs):return [vs[0][k]*(1-a)*(1-b)+vs[1][k]*a*(1-b)+vs[2][k]*a*b+vs[3][k]*(1-a)*b for k in range(len(vs[0]))]
  for _,face,pts in sorted(faces,reverse=True,key=lambda x:x[0]):
   ucount=max(1,int(max(abs(face[0][3]-face[1][3]),abs(face[0][4]-face[1][4]))*256));vcount=max(1,int(max(abs(face[1][3]-face[2][3]),abs(face[1][4]-face[2][4]))*256))
   for a in range(ucount):
    for b in range(vcount):
     uv=interp((a+.5)/ucount,(b+.5)/vcount,face);color=texture.getpixel((max(0,min(255,int(uv[3]*256))),max(0,min(255,int(uv[4]*256)))));draw.polygon([tuple(interp(x,y,pts)[:2]) for x,y in [(a/ucount,b/vcount),((a+1)/ucount,b/vcount),((a+1)/ucount,(b+1)/vcount),(a/ucount,(b+1)/vcount)]],fill=color)
  draw.text((xoff+14,yoff+15),role+' '+state+' / '+str(rotation)+' deg',fill=(226,231,240));draw.line((xoff+10,yoff+450,xoff+390,yoff+450),fill=(92,102,119));draw.text((xoff+14,yoff+477),'Native model + actual UVs. NOT IN GAME.',fill=(165,185,209));draw.line((xoff+25,yoff+450-28.8*9,xoff+25,yoff+450),fill=(124,135,156));draw.text((xoff+32,yoff+450-28.8*9),'1.8b',fill=(165,185,209))
 canvas.save(out/name);print(out/name)
sheet([(r,'READY',angle) for r in ['SCOUT','HUNTER','LEADER'] for angle in [0,90,180,30]],'native-role-turnarounds.png')
sheet([(r,c,25) for r,c in [('HUNTER','THRUST'),('HUNTER','SWEEP'),('SCOUT','BOW'),('SCOUT','KNIFE'),('LEADER','CHOP'),('LEADER','CLEAVE'),('LEADER','HOWL'),('LEADER','LEADER_DEATH')]],'native-attack-poses.png')
