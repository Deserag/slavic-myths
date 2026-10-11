"""Projects native baked model faces and atlas pixels; this is an offline preview, not a game render."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,numpy as np
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths';OUT=ROOT/'work/boss-rework/previews';OUT.mkdir(parents=True,exist_ok=True)
manifest=json.loads((A/'cursed_volkolak_geometry.json').read_text());native=json.loads((ROOT/'work/volkolak-143/native-geometry.json').read_text());vertices={p['name']:p['vertices']for p in native['parts']};atlas=Image.open(A/'textures/entity/cursed_volkolak.png').convert('RGBA')
def transforms(upright):
 result={}
 for p in manifest['parts']:
  name=p['name'];pos=list(p['pos']);rot=list(p['rotation'])
  if upright:
   if name=='pelvis':pos[1]-=4
   if name=='spine':rot[0]-=1.13;pos[1]+=2
   if name=='chest':rot[0]-=.1
   if name=='neck':rot[0]+=.34
   if name=='head':rot[0]+=.71
   for prefix,value in [('upper',1.15),('fore',-.39),('hand',.26),('thigh',1.15),('shin',-2.08),('foot',.93)]:
    if name in (prefix+'L',prefix+'R'):rot[0]=value
  x,y,z=rot;sx,cx=math.sin(x),math.cos(x);sy,cy=math.sin(y),math.cos(y);sz,cz=math.sin(z),math.cos(z)
  rx=np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]]);ry=np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]]);rz=np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]]);m=np.eye(4);m[:3,:3]=rz@ry@rx;m[:3,3]=pos;result[name]=result[p['parent']]@m if p['parent']else m
 return result
im=Image.new('RGB',(1080,670),'#24262B');draw=ImageDraw.Draw(im);stats={}
for row,upright in enumerate((False,True)):
 matrices=transforms(upright);world={n:[(matrices[n]@np.array(v[:3]+[1]))[:3]*np.array([1,1,.85])for v in vs]for n,vs in vertices.items()};allpoints=np.array([v for vs in world.values()for v in vs]);body=np.array([v for n,vs in world.items()if not n.startswith('tail')and n!='furTail'for v in vs]);stats['biped'if upright else'quadruped']={'bounds_blocks':((allpoints.max(axis=0)-allpoints.min(axis=0))/16).tolist(),'body_length_without_tail':float((body[:,2].max()-body[:,2].min())/16),'renderer_depth_scale':.85,'height_above_ground':float((24-allpoints[:,1].min())/16),'lowest_below_ground':float((allpoints[:,1].max()-24)/16)}
 for column,(title,yaw)in enumerate([('FRONT',0),('SIDE',math.pi/2),('THREE QUARTER',math.pi/4)]):
  px=column*360+180;py=row*325+275;draw.text((column*360+12,row*325+10),('BIPED'if upright else'QUADRUPED')+' / '+title,fill='#DDD0AD');faces=[]
  for name,vs in vertices.items():
   points=world[name]
   for i in range(0,len(vs),4):
    face=points[i:i+4];projected=[(v[0]*math.cos(yaw)+v[2]*math.sin(yaw),v[1],-v[0]*math.sin(yaw)+v[2]*math.cos(yaw))for v in face];depth=sum(v[2]for v in projected)/4;poly=[(px+v[0]*4,py+(v[1]-24)*4)for v in projected];u=sum(v[3]for v in vs[i:i+4])/4;v=sum(v[4]for v in vs[i:i+4])/4;color=atlas.getpixel((min(atlas.width-1,int(u*atlas.width)),min(atlas.height-1,int(v*atlas.height))))[:3];normal=np.cross(face[1]-face[0],face[2]-face[0]);norm=np.linalg.norm(normal);shade=.65+.3*abs(normal[1]/norm)if norm else .8;color=tuple(int(c*shade)for c in color);faces.append((depth,poly,color))
  draw.line((column*360+20,py,column*360+340,py),fill='#62645F');
  for depth,poly,color in sorted(faces,key=lambda v:v[0],reverse=True):draw.polygon(poly,fill=color)
  draw.text((column*360+12,row*325+290),f'head top {stats["biped"if upright else"quadruped"]["height_above_ground"]:.2f} blocks',fill='#B9A16C')
draw.text((12,648),'Native baked cuboids + authored texture. Offline projection; client lighting and animation require in-game review.',fill='#AAA9A1');im.save(OUT/'variant-b-three-views.png');(OUT/'geometry-measurements.json').write_text(json.dumps(stats,indent=2));print(stats)
sheet=Image.new('RGB',(640,180),'#282A30');d=ImageDraw.Draw(sheet)
for n,(name,path)in enumerate([('Spawn egg','item/cursed_volkolak_spawn_egg'),('Quest skull','item/cursed_volkolak_skull'),('Bad Blood','mob_effect/bad_blood'),('Weeping herb','block/plach_trava'),('Quest','gui/grey_wolf/quest_3')]):
 pic=Image.open(A/f'textures/{path}.png');sheet.paste(pic.resize((112,112),Image.Resampling.NEAREST),(n*128+8,15),pic.resize((112,112),Image.Resampling.NEAREST));d.text((n*128+8,140),name,fill='#DDD0AD')
sheet.save(OUT/'sprites.png')
