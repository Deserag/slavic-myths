"""Actual PNG pixel-volume + installed vanilla display transforms. Offline only.
These isolated item projections do not include a player hand or gameplay camera.
"""
from pathlib import Path
import json,math
from PIL import Image,ImageDraw,ImageFont
import numpy as np
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/tools87'
V=json.loads((O/'vanilla-display-source.json').read_text());IDS=['perunite_pickaxe','perunite_shovel','perunite_hoe']
def render(im,display,yaw):
 cfg=V['handheld']['display'].get(display,{})
 angles=cfg.get('rotation',[0,0,0]);scale=np.array(cfg.get('scale',[1,1,1]));translation=np.array(cfg.get('translation',[0,0,0]))
 def rx(a):
  c,s=math.cos(a),math.sin(a);return np.array([[1,0,0],[0,c,-s],[0,s,c]])
 def ry(a):
  c,s=math.cos(a),math.sin(a);return np.array([[c,0,s],[0,1,0],[-s,0,c]])
 def rz(a):
  c,s=math.cos(a),math.sin(a);return np.array([[c,-s,0],[s,c,0],[0,0,1]])
 rot=rz(math.radians(angles[2]))@ry(math.radians(angles[1]))@rx(math.radians(angles[0]));cam=ry(math.radians(yaw));polys=[]
 for y in range(16):
  for x in range(16):
   color=im.getpixel((x,y))
   if not color[3]:continue
   a=np.array([x-8,7-y,-.5]);b=a+np.array([1,1,1])
   corners=[(a[0],a[1],a[2]),(b[0],a[1],a[2]),(b[0],b[1],a[2]),(a[0],b[1],a[2]),(a[0],a[1],b[2]),(b[0],a[1],b[2]),(b[0],b[1],b[2]),(a[0],b[1],b[2])]
   verts=[cam@(rot@(np.array(v)*scale)+translation) for v in corners]
   for f in [(0,1,2,3),(4,5,6,7),(0,4,7,3),(1,5,6,2),(0,1,5,4),(3,2,6,7)]:
    ps=[verts[i] for i in f];polys.append((sum(v[2] for v in ps),[(100+v[0]*7,120-v[1]*7) for v in ps],color))
 out=Image.new('RGBA',(200,220));d=ImageDraw.Draw(out)
 for _,ps,c in sorted(polys,key=lambda q:q[0]):d.polygon(ps,fill=c)
 return out

def main():
 out=Image.new('RGB',(1200,930),'#29313b');d=ImageDraw.Draw(out);font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',13)
 d.text((10,10),'#87 actual pixel geometry / vanilla JSON transforms. Offline; hands and player NOT rendered.',font=font,fill='white')
 for row,id in enumerate(IDS):
  y=row*290+35;d.text((10,y),id,font=font,fill='white')
  for col,(title,display,yaw) in enumerate([('SLOT 1x /3x','gui',0),('FIRST-PERSON transform','firstperson_righthand',80),('THIRD transform FRONT','thirdperson_righthand',80),('THIRD transform SIDE','thirdperson_righthand',40)]):
   x=col*300;d.text((x+5,y+22),title,font=font,fill='white')
   for phase,dx in [('before',0),('after',145)]:
    im=Image.open(O/'before'/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png' if phase=='before' else R/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png').convert('RGBA')
    d.text((x+dx+8,y+45),phase,font=font,fill='#aebec7')
    if col==0:
     for size,offset in [(16,0),(48,35)]:
      t=im.resize((size,size),Image.Resampling.NEAREST);out.paste(t,(x+dx+8+offset,y+100),t)
    else:
     t=render(im,display,yaw).resize((140,154),Image.Resampling.NEAREST);out.paste(t,(x+dx,y+65),t)
 out.save(O/'AB-4-views.png')
if __name__=='__main__':main()
