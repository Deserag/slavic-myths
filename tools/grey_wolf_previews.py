"""Orthographic authored geometry previews, not screenshots or in-game QA."""
from pathlib import Path
import json,math,re
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/verification/grey_wolf_1.4.1/previews';OUT.mkdir(parents=True,exist_ok=True)
def rotation(a):
 x,y,z=a;cx,sx=math.cos(x),math.sin(x);cy,sy=math.cos(y),math.sin(y);cz,sz=math.cos(z),math.sin(z)
 return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
def meshes(parts,boxes,pose=None):
 matrices={};pose=pose or {};result=[]
 for p in parts:
  value=pose.get(p['name'],{});pos=np.array(p['pos'])+np.array(value.get('offset',[0,0,0]));mat=np.eye(4);mat[:3,:3]=rotation(np.array(p.get('rot',[0,0,0]))+np.array(value.get('rot',[0,0,0])));mat[:3,3]=pos
  matrices[p['name']]=matrices[p['parent']]@mat if p['parent'] else mat
 for b in boxes:
  v=b['b'];verts=[]
  for x,y,z in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]:verts.append((matrices[b['part']]@np.array([v[0]+x*v[3],v[1]+y*v[4],v[2]+z*v[5],1]))[:3])
  result.append((np.array(verts),b.get('material','bear')))
 return result
COL={'side':(112,112,120),'back':(52,53,58),'silver':(167,165,162),'cheek':(194,189,181),'black':(33,33,38),'amber':(217,154,55),'gold':(255,208,106),'mouth':(64,40,40),'teeth':(219,211,189),'bear':(102,72,49),'steve':(61,170,181)}
FACES=[[0,1,2,3],[4,7,6,5],[0,4,5,1],[3,2,6,7],[0,3,7,4],[1,5,6,2]]
def draw(mesh,yaw,pitch,size=(600,460),scale=62,label=''):
 im=Image.new('RGB',size,(226,220,210));d=ImageDraw.Draw(im);r=rotation([pitch,yaw,0]);polys=[]
 points=np.concatenate([v for v,c in mesh]);camera=(r@points.T).T;center=(camera.min(axis=0)+camera.max(axis=0))/2
 for verts,material in mesh:
  vv=(r@verts.T).T
  for i,face in enumerate(FACES):
   poly=vv[face];xy=[(size[0]/2+(p[0]-center[0])*scale/16,size[1]/2+(p[1]-center[1])*scale/16) for p in poly]
   color=tuple(int(c*[.78,1,.66,1,.7,.92][i]) for c in COL[material]);polys.append((poly[:,2].mean(),xy,color))
 for depth,xy,col in sorted(polys):d.polygon(xy,fill=col);d.line(xy+[xy[0]],fill=tuple(max(0,c-15) for c in col),width=1)
 d.text((12,12),label,fill=(30,30,30));return im
s=(ROOT/'src/main/java/org/slavicmyths/client/WildlifeGeometry.java').read_text().split('case BEAR:')[1].split('case CUB:')[0];parts=[];boxes=[]
for line in s.splitlines():
 m=re.match(r'm.part\("([^"]+)\",(null|\"[^"]+\"),new float\[\]\{([^}]+)\},new float\[\]\{([^}]+)\}',line)
 if m:
  n,parent,pos,rot=m.groups();f=lambda x:[float(v.strip().rstrip('F')) for v in x.split(',')];parts.append({'name':n,'parent':None if parent=='null' else parent.strip('"'),'pos':f(pos),'rot':f(rot)})
 m=re.match(r'm.box\("([^"]+)\",\d+,\d+,new float\[\]\{([^}]+)\}',line)
 if m:boxes.append({'part':m.group(1),'b':[float(v.strip().rstrip('F')) for v in m.group(2).split(',')]})
bear=meshes(parts,boxes);data=json.loads((ROOT/'src/main/resources/assets/slavicmyths/grey_wolf_geometry.json').read_text());wolf=meshes(data['parts'],data['boxes'])
def bounds(mesh):
 points=np.concatenate([v for v,c in mesh]);return {'min_units':points.min(axis=0).tolist(),'max_units':points.max(axis=0).tolist(),'size_blocks':((points.max(axis=0)-points.min(axis=0))/16).tolist()}
metrics={'bear_neutral_render_geometry':bounds(bear),'bear_hitbox':[1.35,1.65],'wolf_neutral_render_geometry':bounds(wolf),'wolf_hitbox':[2.2,3.5],'unit':'1/16 block','not_game_screenshots':True};(OUT/'measurements.json').write_text(json.dumps(metrics,indent=2)+'\n')
poses={'neutral':{},'growl':{'jaw':{'rot':[.45,0,0]},'head':{'rot':[.2,0,0]}},'bite':{'head':{'offset':[0,0,-3]},'jaw':{'rot':[.65,0,0]}},'howl':{'neck':{'rot':[-.5,0,0]},'head':{'rot':[-.44,0,0]},'jaw':{'rot':[.52,0,0]},'chest':{'offset':[0,-.45,0]},'fl':{'rot':[0,0,-.07]},'fr':{'rot':[0,0,.07]}},'sit':{'pelvis':{'offset':[0,10,0]},'chest':{'rot':[-.12,0,0]},'bl':{'offset':[0,1,0],'rot':[-.75,0,0]},'br':{'offset':[0,1,0],'rot':[-.75,0,0]},'blLower':{'rot':[2.15,0,0]},'brLower':{'rot':[2.15,0,0]},'blFoot':{'rot':[-1.4,0,0]},'brFoot':{'rot':[-1.4,0,0]},'tail1':{'rot':[-.5,.65,0]},'tail2':{'rot':[.23,0,0]}}}
views=[('front',math.pi,0),('side',math.pi/2,0),('three-quarter',math.pi/4,-.12),('rear',0,0),('top',0,math.pi/2)]
thumbs=[]
for name,yaw,pitch in views:
 im=draw(wolf,yaw,pitch,label='Geometry preview: '+name);im.save(OUT/(name+'.png'));thumbs.append(im)
for name in ['growl','bite','howl','sit']:
 im=draw(meshes(data['parts'],data['boxes'],poses[name]),math.pi/3,-.1,label='Geometry pose preview: '+name);im.save(OUT/(name+'.png'));thumbs.append(im)
# Actual authoring units, shared projection scale with bear and 1.8-block Steve proxy.
compare=[]
for model,shift in [(wolf,-35),(bear,35)]:
 for v,c in model:compare.append((v+np.array([0,0,shift]),c))
steveparts=[{'name':'root','parent':None,'pos':[0,24,-80]}];steveboxes=[{'part':'root','b':[-4,-28.8,-2,8,28.8,4],'material':'steve'}];compare+=meshes(steveparts,steveboxes)
im=draw(compare,math.pi/2,0,size=(1200,500),scale=55,label='Geometry comparison: Steve (1.8 blocks), Grey Wolf, mod brown bear');im.save(OUT/'comparison.png')
montage=Image.new('RGB',(1800,1380));
for i,im in enumerate(thumbs):montage.paste(im,(i%3*600,i//3*460))
montage.save(OUT/'montage.png');print(json.dumps(metrics))
