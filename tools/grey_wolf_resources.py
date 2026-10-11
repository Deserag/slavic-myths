"""Authored Grey Wolf anatomy/atlas. Reproducible pixel geometry; no vanilla wolf template."""
from pathlib import Path
import json,math,random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]; A=ROOT/'src/main/resources/assets/slavicmyths'
parts=[];boxes=[]
def part(n,parent,pos):parts.append(dict(name=n,parent=parent,pos=pos))
def box(n,b,material):boxes.append(dict(part=n,b=b,material=material))
part('root',None,[0,24,0]);part('pelvis','root',[0,-25,16]);box('pelvis',[-10,-9,-9,20,17,18],'side')
part('chest','root',[0,-34,-9]);box('chest',[-13,-11,-15,26,22,28],'side');box('chest',[-12,-13,-13,24,6,24],'back');box('chest',[-10,3,-16,20,11,13],'silver')
part('loin','root',[0,-29,7]);box('loin',[-10,-10,-10,20,15,19],'side');box('loin',[-10,-12,-10,20,5,19],'back')
part('neck','root',[0,-40,-21]);box('neck',[-11,-8,-7,22,20,15],'side')
part('head','neck',[0,-3,-5]);box('head',[-9,-7,-9,18,14,16],'side');box('head',[-7,-8,-8,14,4,13],'back')
part('upperJaw','head',[0,0,-7]);box('upperJaw',[-7,-2,-12,14,6,13],'cheek');box('upperJaw',[-5,-1,-16,10,5,6],'cheek')
part('nose','upperJaw',[0,0,-16]);box('nose',[-5,-2,-2,10,4,3],'black')
part('jaw','head',[0,5,-4]);box('jaw',[-6,0,-17,12,3,18],'cheek');box('jaw',[-5,-.5,-16,10,1,15],'mouth')
for side,x in [('L',1),('R',-1)]:
 part('ear'+side,'head',[x*6,-7,0]);box('ear'+side,[-3,-6,-2,6,6,4],'back');box('ear'+side,[-2,-9,-1.5,4,3,3],'back');box('ear'+side,[-1.8,-5.3,-2.2,3.6,4,.6],'mouth')
 part('ear'+side+'Tip','ear'+side,[0,-9,0]);box('ear'+side+'Tip',[-1,-2,-1,2,2,2],'back')
 box('head',[x*9-.3,-3,-7,.6,3,4],'amber');box('head',[x*9-.4,-2,-6,.8,1,1],'gold')
 box('head',[x*9-1,-5,-8,2,2,6],'back');box('head',[x*8-2,3,-8,4,7,8],'cheek')
 for z in [-14,-7]:box('upperJaw',[x*4-1,3,z,2,4 if z==-14 else 2,2],'teeth')
 box('jaw',[x*4-1,-3,-13,2,3,2],'teeth')
for name,y,z,mat in [('maneFront',-39,-23,'silver'),('maneRear',-40,-14,'back')]:
 part(name,'root',[0,y,z])
 for i in range(4):
  w=30-i*3;box(name,[-w/2,i*4-5,-2+i, w,6,8],'back' if name=='maneRear' else mat)
 for x in [-12,8]:box(name,[x,1,-4,4,12,6],mat)
for name,x,z,front in [('fl',10,-16,True),('fr',-10,-16,True),('bl',8,17,False),('br',-8,17,False)]:
 part(name,'root' if front else 'pelvis',[x,-30 if front else 0,z if front else z-16]);box(name,[-4,0,-4,8,14 if front else 13,9],'side')
 part(name+'Lower',name,[0,14 if front else 13,0]);box(name+'Lower',[-3,0,-3,6,12 if front else 9,7],'silver')
 part(name+'Foot',name+'Lower',[0,12 if front else 9,0]);box(name+'Foot',[-4,0,-6,8,4 if front else 3,10],'cheek')
 for toe in [-2.6,0,2.6]:box(name+'Foot',[toe-1,1,-8,2,2,4],'cheek');box(name+'Foot',[toe-.7,2,-9,.0+1.4,1.2,2],'black')
part('tail1','pelvis',[0,-3,8]);box('tail1',[-4,-3,0,8,8,12],'side')
part('tail2','tail1',[0,4,10]);box('tail2',[-5,-2,0,10,10,13],'side')
part('tail3','tail2',[0,5,11]);box('tail3',[-3,-1,0,6,7,11],'back')
# Bear measured neutral bounds: .943 x1.736 x2.435 blocks.
# Authored giant: about1.875 x3.469 x4.870, no renderer/global scale.
for p in parts:
 if p['name']!='root':p['pos']=[p['pos'][0],p['pos'][1]*.91,p['pos'][2]*(4.87/6.6875)]
for b in boxes:
 b['b']=[value*([1,.91,4.87/6.6875][i%3]) for i,value in enumerate(b['b'])]
COL={'side':(112,112,120),'back':(52,53,58),'silver':(167,165,162),'cheek':(194,189,181),'black':(33,33,38),'amber':(217,154,55),'gold':(255,208,106),'mouth':(64,40,40),'teeth':(219,211,189)}
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
