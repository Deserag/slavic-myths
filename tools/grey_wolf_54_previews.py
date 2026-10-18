"""Native ModelPart mesh/UV evidence for #54. Same scale before/after; no game launch."""
from pathlib import Path
from PIL import Image,ImageDraw
import gzip,json,math,io,subprocess
R=Path(__file__).resolve().parents[1];OUT=R/'docs/implementation/grey-wolf-54'
def draw(faces,texture,yaw,size=(500,420),silhouette=False,scale=4,view_pitch=7,center_y=185):
 im=Image.new('RGB',size,'#202B30');d=ImageDraw.Draw(im);w,h=size;angle=math.radians(yaw);pitch=math.radians(view_pitch)
 def project(v):
  x,y,z=v[:3];y+=4;a=x*math.cos(angle)+z*math.sin(angle);b=z*math.cos(angle)-x*math.sin(angle)
  return [215+a*scale,center_y+(y*math.cos(pitch)-b*math.sin(pitch))*scale,b*math.cos(pitch)+y*math.sin(pitch)]
 def interp(a,b,values):return [values[0][k]*(1-a)*(1-b)+values[1][k]*a*(1-b)+values[2][k]*a*b+values[3][k]*(1-a)*b for k in range(len(values[0]))]
 for face in sorted(faces,key=lambda f:sum(project(v)[2] for v in f)/4,reverse=True):
  pts=[project(v) for v in face]
  if silhouette:d.polygon([tuple(p[:2]) for p in pts],fill='#BBC7C9');continue
  tw,th=texture.size;nu=max(1,math.ceil(max(abs(face[0][3]-face[1][3])*tw,abs(face[0][4]-face[1][4])*th)));nv=max(1,math.ceil(max(abs(face[1][3]-face[2][3])*tw,abs(face[1][4]-face[2][4])*th)))
  edge1=[face[1][i]-face[0][i] for i in range(3)];edge2=[face[2][i]-face[1][i] for i in range(3)];normal=[edge1[1]*edge2[2]-edge1[2]*edge2[1],edge1[2]*edge2[0]-edge1[0]*edge2[2],edge1[0]*edge2[1]-edge1[1]*edge2[0]];length=math.sqrt(sum(x*x for x in normal)) or 1;shade=.65+.23*abs(normal[1])/length+.12*abs(normal[2])/length
  for a in range(nu):
   for b in range(nv):
    uv=interp((a+.5)/nu,(b+.5)/nv,face);color=texture.getpixel((min(tw-1,max(0,int(uv[3]*tw))),min(th-1,max(0,int(uv[4]*th)))))
    d.polygon([tuple(interp(x,y,pts)[:2]) for x,y in [(a/nu,b/nv),((a+1)/nu,b/nv),((a+1)/nu,(b+1)/nv),(a/nu,(b+1)/nv)]],fill=tuple(int(c*shade) for c in color[:3]))
 ground=project((0,24,0))[1];d.line((12,ground,485,ground),fill='#91A382')
 # Scale references, authored unit cube (1 Minecraft block) and 1.8m standing height marker.
 unit=16*scale;xx=432 if scale<=4 else w-unit-10;bottom=ground;d.rectangle((xx,bottom-unit,xx+unit,bottom),outline='#6D838D',width=2);d.text((xx,bottom+5),'1m block',fill='#91A382')
 d.line((xx-12,bottom,xx-12,bottom-28.8*scale),fill='#91A382',width=2);d.text((xx-42,bottom-28.8*scale-15),'1.8m',fill='#91A382')
 return im

def main():
 before=json.loads(gzip.decompress((OUT/'before-native.json.gz').read_bytes()));after=json.loads(gzip.decompress((R/'build/grey-wolf-54/after-native.json.gz').read_bytes()))
 old=Image.open(io.BytesIO(subprocess.check_output(['git','show','8888d0e1:src/main/resources/assets/slavicmyths/textures/entity/grey_wolf.png'],cwd=R))).convert('RGB');new=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/grey_wolf.png').convert('RGB')
 canvas=Image.new('RGB',(2000,900),'#202B30');d=ImageDraw.Draw(canvas)
 for row,(data,tex) in enumerate([(before,old),(after,new)]):
  for col,(yaw,label) in enumerate([(0,'FRONT'),(90,'SIDE'),(180,'BACK'),(-35,'3/4')]):
   canvas.paste(draw(data['idle'],tex,yaw),(col*500,row*445+25));d.text((col*500+12,row*445+7),('BEFORE' if row==0 else 'AFTER')+' / '+label,fill='#DCE2DD')
 d.text((12,885),'Actual native geometry + actual UV atlas. Same scale. NOT MINECRAFT SCREENSHOTS.',fill='#91A382');canvas.save(OUT/'native-before-after.png')
 keys=['idle','walk','run','watch','bite_12','claw_14','claw_33','pounce_12','pounce_20','pounce_27','dash_27','howl_31','howl_80','complete_70','sit']
 canvas=Image.new('RGB',(2000,1800),'#202B30');d=ImageDraw.Draw(canvas)
 for i,key in enumerate(keys):
  x=i%4*500;y=i//4*445;canvas.paste(draw(after[key],new,-35),(x,y+25));d.text((x+12,y+7),key.upper(),fill='#DCE2DD')
 d.text((12,1785),'Production sample(state,move,timing) poses. Source-level only; manual visual acceptance NOT_RUN.',fill='#91A382');canvas.save(OUT/'native-poses.png');print(OUT)
if __name__=='__main__':main()
