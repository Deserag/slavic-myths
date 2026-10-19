"""Orthographic native mesh/actual nearest-neighbour UV renderer, CPU only."""
import math
from PIL import Image,ImageDraw

def draw(faces,texture,yaw,size=(500,420),silhouette=False,scale=4,view_pitch=7,center_y=185):
 im=Image.new('RGB',size,'#202B30');w,h=size;angle=math.radians(yaw);pitch=math.radians(view_pitch)
 def project(v):
  x,y,z=v[:3];y+=4;a=x*math.cos(angle)+z*math.sin(angle);b=z*math.cos(angle)-x*math.sin(angle)
  return (215+a*scale,center_y+(y*math.cos(pitch)-b*math.sin(pitch))*scale,b*math.cos(pitch)+y*math.sin(pitch))
 tw,th=texture.size
 for face in sorted(faces,key=lambda f:sum(project(v)[2] for v in f)/4,reverse=True):
  pts=[project(v) for v in face]
  if silhouette:ImageDraw.Draw(im).polygon([p[:2] for p in pts],fill='#BBC7C9');continue
  x0,y0=pts[0][:2];ax=pts[1][0]-x0;ay=pts[1][1]-y0;bx=pts[3][0]-x0;by=pts[3][1]-y0;det=ax*by-ay*bx
  if abs(det)<.001:continue
  left=max(0,math.floor(min(p[0] for p in pts)));top=max(0,math.floor(min(p[1] for p in pts)));right=min(w,math.ceil(max(p[0] for p in pts))+1);bottom=min(h,math.ceil(max(p[1] for p in pts))+1)
  if left>=right or top>=bottom:continue
  coeff=[]
  for index,extent in [(3,tw),(4,th)]:
   u0=face[0][index]*extent;du=(face[1][index]-face[0][index])*extent;dv=(face[3][index]-face[0][index])*extent
   a=(du*by-dv*ay)/det;b=(-du*bx+dv*ax)/det;c=u0-a*x0-b*y0
   coeff.extend([a,b,c+a*left+b*top])
  patch=texture.transform((right-left,bottom-top),Image.Transform.AFFINE,coeff,Image.Resampling.NEAREST)
  edge1=[face[1][i]-face[0][i] for i in range(3)];edge2=[face[2][i]-face[1][i] for i in range(3)];normal=[edge1[1]*edge2[2]-edge1[2]*edge2[1],edge1[2]*edge2[0]-edge1[0]*edge2[2],edge1[0]*edge2[1]-edge1[1]*edge2[0]]
  length=math.sqrt(sum(v*v for v in normal)) or 1;shade=.65+.23*abs(normal[1])/length+.12*abs(normal[2])/length
  patch=patch.point([int(c*shade) for c in range(256)]*3)
  mask=Image.new('L',patch.size);ImageDraw.Draw(mask).polygon([(p[0]-left,p[1]-top) for p in pts],fill=255);im.paste(patch,(left,top),mask)
 d=ImageDraw.Draw(im);ground=project((0,24,0))[1];d.line((12,ground,485,ground),fill='#91A382')
 unit=16*scale;xx=432 if scale<=4 else w-unit-10;d.rectangle((xx,ground-unit,xx+unit,ground),outline='#6D838D',width=2);d.text((xx,ground+5),'1m block',fill='#91A382');d.line((xx-12,ground,xx-12,ground-28.8*scale),fill='#91A382');d.text((xx-42,ground-28.8*scale-15),'1.8m',fill='#91A382')
 return im
