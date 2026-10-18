"""Rasterize exported native ModelPart polygons/UVs. Offline projection, never gameplay."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,math,subprocess,io
R=Path(__file__).resolve().parents[1];OUT=R/'docs/implementation/rusalka-49';OUT.mkdir(parents=True,exist_ok=True)
def render(faces,texture,yaw=0,size=(330,420),silhouette=False):
 im=Image.new('RGB',size,'#1C292D');draw=ImageDraw.Draw(im);w,h=size;angle=math.radians(yaw);pitch=math.radians(6);scale=min(w/34,h/40)
 def project(v):
  x,y,z=v[:3];y-=7;a=x*math.cos(angle)+z*math.sin(angle);b=z*math.cos(angle)-x*math.sin(angle)
  return [w/2+a*scale,h*.46+(y*math.cos(pitch)-b*math.sin(pitch))*scale,b*math.cos(pitch)+y*math.sin(pitch)]
 def interp(a,b,vs):return [vs[0][k]*(1-a)*(1-b)+vs[1][k]*a*(1-b)+vs[2][k]*a*b+vs[3][k]*(1-a)*b for k in range(len(vs[0]))]
 ordered=sorted([(sum(project(v)[2] for v in f)/4,f,[project(v) for v in f]) for f in faces],key=lambda x:x[0],reverse=True)
 for _,face,pts in ordered:
  if silhouette:draw.polygon([tuple(p[:2]) for p in pts],fill='#C8D0C6');continue
  tw,th=texture.size
  nu=max(1,math.ceil(max(abs(face[0][3]-face[1][3])*tw,abs(face[0][4]-face[1][4])*th)))
  nv=max(1,math.ceil(max(abs(face[1][3]-face[2][3])*tw,abs(face[1][4]-face[2][4])*th)))
  # Subdivision at actual texel density, nearest sampling, no invented detail.
  for a in range(nu):
   for b in range(nv):
    uv=interp((a+.5)/nu,(b+.5)/nv,face);color=texture.getpixel((min(tw-1,max(0,int(uv[3]*tw))),min(th-1,max(0,int(uv[4]*th)))))
    corners=[tuple(interp(x,y,pts)[:2]) for x,y in [(a/nu,b/nv),((a+1)/nu,b/nv),((a+1)/nu,(b+1)/nv),(a/nu,(b+1)/nv)]]
    draw.polygon(corners,fill=color)
 return im

def main():
 data=json.loads((R/'build/rusalka-49/meshes.json').read_text())
 old=Image.open(io.BytesIO(subprocess.check_output(['git','show','b4fb788e:src/main/resources/assets/slavicmyths/textures/entity/rusalka.png'],cwd=R))).convert('RGB')
 new=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/rusalka.png').convert('RGB')
 canvas=Image.new('RGB',(1650,900),'#1C292D');d=ImageDraw.Draw(canvas)
 views=[('FRONT',0),('SIDE',90),('BACK',180),('3/4',-35),('DISTANT SILHOUETTE',-20)]
 for row,(key,texture) in enumerate([('before',old),('idle',new)]):
  for col,(label,yaw) in enumerate(views):
   image=render(data[key],texture,yaw,silhouette=col==4)
   if col==4:
    small=image.resize((83,105),Image.Resampling.NEAREST);image=Image.new('RGB',(330,420),'#1C292D');image.paste(small,(123,165))
   canvas.paste(image,(330*col,450*row+25));d.text((330*col+12,450*row+9),('BEFORE' if row==0 else 'AFTER')+' / '+label,fill='#E8E3D6')
 d.text((12,880),'Actual production ModelPart vertices + UVs. OFFLINE SOURCE PROJECTION; NOT MINECRAFT SCREENSHOTS.',fill='#8FA27E');canvas.save(OUT/'native-before-after.png')
 keys=['idle','observe','walk','swim','song_start','lure','break','windup','grab','recovery','dive','hurt','death']
 canvas=Image.new('RGB',(1400,1360),'#1C292D');d=ImageDraw.Draw(canvas)
 for i,key in enumerate(keys):
  x=i%4*350;y=i//4*330;canvas.paste(render(data[key],new,-45 if key in ('swim','dive') else -25,(350,310)),(x,y+20));d.text((x+12,y+6),key.upper(),fill='#E8E3D6')
 d.text((12,1340),'Actual native rig poses; attack active frame exported directly. Offline, not gameplay.',fill='#8FA27E');canvas.save(OUT/'native-key-poses.png')
 print(OUT)
if __name__=='__main__':main()
