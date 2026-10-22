"""Actual native mob-subtree -> placed trophy comparison, with exact UV/PNG."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw
from armor88_preview import render
from trophy111_source_preview import rotation

R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/heads140-generation141'

def sample(vertices,texture):
 faces=[]
 for i in range(0,len(vertices),4):
  face=[]
  for x,y,z,u,v in vertices[i:i+4]:face.append([(x-.5)*16,(1.9-y)*16-8,(z-.5)*16,u,v])
  faces.append(face)
 return {'layers':[{'texture':texture.split(':',1)[1],'faces':faces}]}

def main():
 data=json.loads((O/'native-heads.json').read_text());board=Image.new('RGB',(1600,60+285*6),'#161b22');d=ImageDraw.Draw(board)
 d.text((12,10),'ACTUAL NATIVE MOB HEAD -> TROPHY / identical camera + physical scale / unchanged UV and PNG.',fill='white')
 d.text((12,28),'OFFLINE SOURCE RENDER. Before is the selected mob subtree, not a pre-existing trophy or game capture.',fill='white')
 compact=Image.new('RGB',(1200,330),'#293039');dc=ImageDraw.Draw(compact);dc.text((10,8),'OFFLINE actual six heads: same world scale. Inventory 1x / 3x below.',fill='white')
 for row,h in enumerate(data['heads']):
  raw=np.array(h['before_native_vertices']);posed=np.array(h['after_placed_vertices']);b=h['native_bounds'];s=h['uniform_scale'];expected=raw.copy();expected[:,0]=.5+(raw[:,0]-(b[0]+b[3])/2)*s;expected[:,1]=.04+(b[4]-raw[:,1])*s;expected[:,2]=.5-(raw[:,2]-(b[2]+b[5])/2)*s
  assert np.max(np.abs(expected-posed))<1e-5,'actual renderer differs from documented uniform placement'
  old,new=sample(expected,h['texture']),sample(posed,h['texture']);y=60+row*285;d.text((8,y),h['id']+' / reserved cells: '+str(h['height']),fill='white')
  for col,(label,yaw) in enumerate([('FRONT',180),('RIGHT',90),('BACK',0),('3/4',145)]):
   for side,mesh in enumerate([old,new]):
    x=(col*2+side)*200;board.paste(render(mesh,yaw),(x,y+15));d.text((x+4,y+18),label+(' SOURCE' if side==0 else ' TROPHY'),fill='white')
  dc.text((row*200+4,30),h['id'],fill='white');pic=render(new,145);compact.paste(pic.crop((0,40,200,270)),(row*200,50))
  # Real head close-up icon, including a clear face rather than all long hair.
  rgba,depth=render(new,145,return_depth=True);rgba[:,:,3]=np.isfinite(depth).astype(np.uint8)*255;large=Image.fromarray(rgba);bounds=large.getbbox();assert bounds
  x0,y0,x1,y1=bounds
  if h['id'] in ['likho_head','mor_likho_head']:y1=min(y1,y0+int((x1-x0)*1.5))
  crop=large.crop((x0,y0,x1,y1));scale=min(14/crop.width,14/crop.height);crop=crop.resize((max(1,round(crop.width*scale)),max(1,round(crop.height*scale))),Image.Resampling.NEAREST);icon=Image.new('RGBA',(16,16));icon.paste(crop,((16-crop.width)//2,(16-crop.height)//2))
  icon.save(R/f"src/main/resources/assets/slavicmyths/textures/item/{h['id']}.png")
  icon.save(O/(h['id']+'-slot.png'));compact.paste(icon,(row*200+65,296),icon);big=icon.resize((48,48),Image.Resampling.NEAREST);compact.paste(big,(row*200+90,280),big)
 board.save(O/'native-heads-source-before-after.png');compact.save(O/'six-heads-result.png')
 print('NATIVE_HEAD_PREVIEW PASS: actual VertexConsumer quads/UV/production PNG')

if __name__=='__main__':main()
