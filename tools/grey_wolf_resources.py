"""Authored Grey Wolf anatomy/atlas. Reproducible pixel geometry; no vanilla wolf template."""
from pathlib import Path
import json,math,random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]; A=ROOT/'src/main/resources/assets/slavicmyths'
parts=[];boxes=[]
def part(n,parent,pos):parts.append(dict(name=n,parent=parent,pos=pos))
def box(n,b,material):boxes.append(dict(part=n,b=b,material=material))
# #54: a tapered powerful forest wolf, preserved 30-joint contract.
part('root',None,[0,24,0])
part('pelvis','root',[0,-23,11]);box('pelvis',[-7,-7,-7,14,14,14],'side');box('pelvis',[-6,-7.8,-5,12,6,13],'back')
part('chest','root',[0,-28,-8]);box('chest',[-10,-8,-10,20,18,19],'side');box('chest',[-9,-9.5,-8,18,6,17],'back');box('chest',[-8,3,-10.8,16,8,13],'silver')
part('loin','root',[0,-25.5,2.5]);box('loin',[-7.5,-7,-7,15,12,16],'side');box('loin',[-6.8,-8,-6,13.6,4,15],'back')
part('neck','root',[0,-34.5,-14.5]);box('neck',[-7.2,-6,-5,14.4,14,12],'side');box('neck',[-6,2,-6.5,12,9,9],'silver')
part('head','neck',[0,-3,-4]);box('head',[-6.3,-5.5,-5.5,12.6,11,11],'side');box('head',[-5.8,-6.2,-4.5,11.6,3,9],'back')
part('upperJaw','head',[0,.3,-4]);box('upperJaw',[-4.2,-1.5,-7.5,8.4,4.6,8.5],'cheek');box('upperJaw',[-3.3,-1.2,-10,6.6,4,4],'cheek')
part('nose','upperJaw',[0,0,-10]);box('nose',[-3.4,-1.5,-1.1,6.8,2.8,2],'black')
part('jaw','head',[0,3.8,-3]);box('jaw',[-3.5,0,-10.5,7,2.3,11],'cheek');box('jaw',[-3.1,-.35,-9.5,6.2,.65,8.5],'mouth')
for side,x in [('L',1),('R',-1)]:
 part('ear'+side,'head',[x*4.5,-5.5,.3]);box('ear'+side,[-2,-4,-1.5,4,4,3],'back');box('ear'+side,[-1.2,-3.5,-1.7,2.4,2.8,.5],'mouth')
 part('ear'+side+'Tip','ear'+side,[0,-4,0]);box('ear'+side+'Tip',[-1,-2,-1.1,2,2,2.2],'back')
 box('head',[x*6.3-.3,-2.5,-4.7,.6,1.2,2.2],'amber');box('head',[x*6.3-.35,-2.1,-4.4,.7,.45,.65],'gold')
 box('head',[x*6.1-.8,-3.8,-5.2,1.6,1.4,4],'back');box('head',[x*5.7-1.4,1,-3.7,2.8,4,5],'cheek')
 box('upperJaw',[x*2.7-.55,2.2,-7.3,1.1,2,1.1],'teeth');box('jaw',[x*2.5-.5,-1.6,-6.5,1,1.6,1],'teeth')
for name,y,z,mat in [('maneFront',-33,-16.5,'silver'),('maneRear',-34,-10,'back')]:
 part(name,'root',[0,y,z]);box(name,[-8.5,-4,-2,17,8,6],mat);box(name,[-7.2,2,-1,14.4,7,5],mat);box(name,[-5.5,7,0,11,4,4],mat)
for name,x,z,front in [('fl',7.8,-12,True),('fr',-7.8,-12,True),('bl',6.5,1.5,False),('br',-6.5,1.5,False)]:
 part(name,'root' if front else 'pelvis',[x,-27 if front else 0,z]);box(name,[-3.2 if front else -4.2,-.5,-3.6 if front else -4.2,6.4 if front else 8.4,14.5 if front else 12.5,7.2 if front else 8.4],'side')
 if front:box(name,[-3.8,-1.5,-4,7.6,6.5,8],'back')
 part(name+'Lower',name,[0,14 if front else 12,0]);box(name+'Lower',[-2.3,0,-2.5,4.6,10 if front else 8,5.5],'silver')
 part(name+'Foot',name+'Lower',[0,10 if front else 8,0]);box(name+'Foot',[-3.7,0,-5,7.4,3,9.5],'cheek')
 for toe in [-2.2,0,2.2]:box(name+'Foot',[toe-.65,1.1,-5.6,1.3,1.1,1.6],'black')
part('tail1','pelvis',[0,-2,6]);box('tail1',[-3.2,-2.7,-.5,6.4,6.2,10],'side')
part('tail2','tail1',[0,1,8]);box('tail2',[-2.8,-2.4,0,5.6,5.8,10],'side')
part('tail3','tail2',[0,1,8]);box('tail3',[-1.9,-1.6,0,3.8,4,8],'back')
COL={'side':(92,104,112),'back':(47,54,63),'silver':(154,164,168),'cheek':(168,174,176),'black':(33,33,38),'amber':(197,149,74),'gold':(224,181,111),'mouth':(64,40,40),'teeth':(219,211,189)}
def write(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def apply():
 text=json.loads((ROOT/"tools/grey_wolf_text.json").read_text(encoding="utf-8"))
 for locale in ["ru_ru","en_us"]:
  path=A/f"lang/{locale}.json";lang=json.loads(path.read_text(encoding="utf-8"));lang.update(text[locale]);write(path,lang)
 path=A/"sounds.json";sounds=json.loads(path.read_text(encoding="utf-8"));sounds.update(text["sounds"]);write(path,sounds)
 im=Image.new('RGB',(512,512),COL['back']);d=ImageDraw.Draw(im);rng=random.Random(141);u=v=row=0
 for b in boxes:
  w,h,dep=[math.ceil(i) for i in b['b'][3:]];uw=2*(w+dep)+2;vh=h+dep+2
  if u+uw>512:u=0;v+=row;row=0
  assert v+vh<=512
  b['uv']=[u,v];c=COL[b['material']]
  for yy in range(v,v+vh):
   for xx in range(u,u+uw):
    shift=(((xx//3)*13+(yy//3)*7+(u+v))%7-3)*2 if b['material'] not in ['amber','gold','black','mouth','teeth'] else 0
    im.putpixel((xx,yy),tuple(max(0,min(255,k+shift)) for k in c))
  u+=uw;row=max(row,vh)
 texture=A/'textures/entity/grey_wolf.png';texture.parent.mkdir(parents=True,exist_ok=True);im.save(texture)
 write(A/'grey_wolf_geometry.json',{'units':'1/16 block, root at ground, +z rear, -y up','parts':parts,'boxes':boxes,'texture_size':[512,512]})
 java=['package org.slavicmyths.client;','/** Generated authored anatomy; tools/grey_wolf_resources.py. */','public final class GreyWolfGeometry { public static void build(GreyWolfModel m) {']
 f=lambda v: str(float(v))+'F'
 for p in parts:
  parent='null' if p['parent'] is None else '"'+p['parent']+'"'
  java.append('m.part("'+p['name']+'",'+parent+',new float[]{'+','.join(map(f,p['pos']))+'});')
 for b in boxes:java.append('m.box("'+b['part']+'",'+','.join(map(str,b['uv']))+',new float[]{'+','.join(map(f,b['b']))+'});')
 java.append('} private GreyWolfGeometry(){} }');(ROOT/'src/main/java/org/slavicmyths/client/GreyWolfGeometry.java').write_text('\n'.join(java)+'\n',encoding='utf-8')
 write(ROOT/'src/main/resources/data/slavicmyths/tags/worldgen/biome/grey_wolf_forests.json',{'replace':False,'values':['#minecraft:is_forest','#minecraft:is_taiga']})
 write(A/'models/item/grey_wolf_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
 icon=Image.new('RGBA',(32,32),(44,44,48,255));di=ImageDraw.Draw(icon)
 for x in [5,12,19]:di.polygon([(x,25),(x+1,13),(x+7,5),(x+5,17)],fill=(152,31,44,255))
 ip=A/'textures/mob_effect/wolf_cut.png';ip.parent.mkdir(parents=True,exist_ok=True);icon.save(ip)
if __name__=='__main__':apply()
