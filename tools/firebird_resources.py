"""Original deterministic voxel rig/atlas/item art; no images copied from the concept sheet."""
from pathlib import Path
import json, math
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/slavicmyths'
D=ROOT/'src/main/resources/data'
def write(p,s):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf8')
def data(p,v):write(p,json.dumps(v,ensure_ascii=False,indent=2)+'\n')
atlas=Image.new('RGBA',(256,256));glow=Image.new('RGBA',(256,256));draw=ImageDraw.Draw(atlas);gd=ImageDraw.Draw(glow)
palette=[(224,100,30),(255,210,90),(247,226,176),(121,65,24),(39,28,18),(161,38,19)]
for k,c in enumerate(palette):
 for y in range(64):
  t=y/63;col=tuple(int(v*(.7+.3*t)) for v in c)
  x=k%3*80;yy=k//3*100+y
  draw.line((x,yy,x+79,yy),fill=(*col,255))
  for xx in range(80):
   ridge=5 if xx%8==3 else -4 if (xx+y//3)%8==0 else 0
   draw.point((x+xx,yy),fill=(*tuple(max(0,min(255,v+ridge)) for v in col),255))
  if k in (0,1,2):gd.line((x,yy,x+79,yy),fill=(*c,int((.15 if k==2 else .22 if k==0 else .85)*255)))
for p,im in [(A/'textures/entity/firebird.png',atlas),(A/'textures/entity/firebird_emissive.png',glow)]:p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
bones=[]
def bone(name,parent,pivot,boxes,rot=(0,0,0)):
 bones.append(dict(name=name,parent=parent,pivot=pivot,rotation=rot,boxes=boxes))
def box(x,y,z,w,h,d,p=0):return [x,y,z,w,h,d,p]
bone('body','root',(0,-15,0),[box(-6,-9,-10,12,13,24),box(-5,-11,-9,10,3,20,0),box(-4,3,-7,8,3,17,5),box(-4,-9,-11,8,11,2,2)])
for row in range(4):
 for col in range(5):
  xx=(col-2)*2.1
  bone(f'chest_plume_{row}_{col}','body',(xx,-9+row*3.2,-9.5),[box(-1.2,0,-.6,2.4,4.3,1.2,2),box(-.7,3,-.8,1.4,2,1.2,1)],(.12,0,xx*.015))
 for side in (-1,1):
  for col in range(4):bone(f'flank_{row}_{"left" if side<0 else "right"}_{col}','body',(side*5.6,-8+row*3, -5+col*3.8),[box(-1,0,0,2,4.8,3,0),box(-.6,3,1,1.2,3,2,1)],(-.25,side*.18,side*.15))
bone('neck_lower','body',(0,-7,-8),[box(-3.2,-10,-3,6.4,14,6,2),box(-3.6,-9,1,7.2,11,3,0)])
bone('neck_upper','neck_lower',(0,-9,-.8),[box(-2.5,-3,-2.3,5,5,4.8,2),box(-2.2,-6.5,-2.7,4.4,5,4.3,2),box(-1.9,-9,-3.1,3.8,4,4,2),box(-2.6,-8,1.1,5.2,8,2.4,0)])
for row in range(6):
 for col in range(3):bone(f'throat_feather_{row}_{col}','neck_upper',((col-1)*1.35,-8+row*1.7,-2.9+row*.13),[box(-.8,0,-.5,1.6,2.8,1,2),box(-.4,2,-.6,.8,1.3,.8,1)],(.10,0,(col-1)*.04))
for row in range(5):
 for side in (-1,1):
  bone(f'neck_plume_{row}_{"left" if side<0 else "right"}','neck_lower',(side*2.8,-9+row*2.6,1),[box(-1,0,0,2,3.8,2.7,0),box(-.5,2,1,1,2.8,2,1)],(-.35,side*.22,side*.08))
bone('head','neck_upper',(0,-6,-1.3),[box(-3,-3.5,-3.3,6,5.6,6.6,0),box(-3.2,-2.7,-2.8,1,1.1,2.6,1),box(2.2,-2.7,-2.8,1,1.1,2.6,1),box(-3.28,-2.1,-2.4,.25,.6,1.1,4),box(3.03,-2.1,-2.4,.25,.6,1.1,4)])
bone('beak','head',(0,-.6,-3.2),[box(-1.9,-1.1,-3.2,3.8,2.2,3.5,1),box(-1.25,-.7,-4.7,2.5,2.2,2,3),box(-.65,.6,-5.3,1.3,2,1.1,4)])
bone('crest_base','head',(0,-3.3,.2),[])
for i in range(7):
 side=(i-3)/3
 bone('crest'+str(i),'crest_base',(side*2,0,abs(side)*.6),[box(-.75,-2.5,-.3,1.5,3.5,4,0),box(-.65,-3.9,2.3,1.3,2.4,5,1),box(-.4,-4.4,6,.8,1.7,3,1)],(.14+abs(side)*.1,side*.24,side*.13))
for side in (-1,1):
 s='left' if side<0 else 'right'
 bone(s+'_shoulder','body',(side*5,-9,-3),[box(-9 if side<0 else 0,-1.5,-2,9,3,8,0)],(0,0,-side*.15))
 bone(s+'_mid',s+'_shoulder',(side*9,0,1),[box(-13 if side<0 else 0,-1.1,-2,13,2.2,6.5,0)],(0,0,side*.12))
 bone(s+'_tip',s+'_mid',(side*12,0,1),[box(-17 if side<0 else 0,-.8,-1,17,1.6,3.5,0)],(0,0,side*.18))
 for i in range(8):
  length=15+(7-i)*1.4
  bone(s+'_feather'+str(i),s+'_tip',(side*i*2.1,0,1.5),[box(-1.4,-.5,0,2.8,1,length,5),box(-1.15,-.65,4,2.3,1.1,length-1,0),box(-.6,-.8,length-5,1.2,1.2,9,1)],(-.78,side*(i-3)*.075,0))
 for i in range(8):bone(s+'_cover'+str(i),s+'_shoulder',(side*i*.9,0,1),[box(-1.2,-2,0,2.4,1.5,7+i*.4,1)],(-.5,side*.15,0))
 for i in range(8):bone(s+'_secondary'+str(i),s+'_mid',(side*i*1.7,0,2),[box(-1.4,-1,0,2.8,1.5,11+i*.5,0),box(-.7,-1.2,8,1.4,1.5,7,1)],(-.55,side*.12,0))
 bone(s+'_leg','root',(side*3.3,-11,.3),[box(-1.8,-.5,-1.8,3.6,4.8,3.6,0),box(-.9,3,-.8,1.8,2.7,1.8,1)])
 bone(s+'_ankle',s+'_leg',(0,5,1.1),[box(-.65,-.6,-.7,1.3,5.9,1.4,3),box(-.8,-.8,-.8,1.6,1.7,1.7,1)])
 bone(s+'_claws',s+'_ankle',(0,5.3,-.6),[box(-1,-.5,-1,2,.8,2,3)])
 for j in range(3):
  bone(s+'_toe'+str(j),s+'_claws',((j-1)*.7,0,-.7),[box(-.32,-.2,-2.3,.64,.6,2.5,1)],(0,(j-1)*-.27,0))
  bone(s+'_talon'+str(j),s+'_toe'+str(j),(0,.05,-2.1),[box(-.27,0,-1.1,.54,.65,1.3,3),box(-.18,.4,-1.6,.36,.4,.8,4)],(-.15,0,0))
 bone(s+'_back_toe',s+'_claws',(0,-.1,.6),[box(-.35,0,0,.7,.6,1.7,3),box(-.2,.2,1.3,.4,.6,.9,4)],(0,0,0))
bone('tail_base','body',(0,0,13),[box(-4,-2,0,8,3,7,0)],(-.20,0,0))
for i in range(7):
 bone('tail'+str(i),'tail_base',((i-3)*1.4,0,3),[box(-1.6,-.5,0,3.2,1.2,20+i%3*3,5),box(-1.3,-.7,12,2.6,1.3,20+i%3*3,0),box(-.65,-.9,16,1.3,1.4,14,1)],(0,(i-3)*.15,0))
 bone('tail_tip'+str(i),'tail'+str(i),(0,0,30+i%3*3),[box(-1.05,-.6,0,2.1,1.3,15,0),box(-.65,-.8,9,1.3,1.4,8,1)],(.05,0,0))
lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','public final class FirebirdMesh {public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();']
def f(v):return f'{v:.4f}F'
for b in bones:
 expr='CubeListBuilder.create()'
 for x,y,z,w,h,d,p in b['boxes']:expr+=f'.texOffs({p%3*80},{p//3*100}).addBox('+','.join(map(f,[x,y,z,w,h,d]))+')'
 lines.append(f'var {b["name"]}={b["parent"]}.addOrReplaceChild("{b["name"]}",{expr},PartPose.offsetAndRotation('+','.join(map(f,[*b['pivot'],*b['rotation']]))+'));')
lines+=['return LayerDefinition.create(mesh,256,256).bakeRoot();}', 'public static java.util.Map<String,ModelPart> index(ModelPart root){var map=new java.util.HashMap<String,ModelPart>();map.put("root",root);']
for b in bones:lines.append(f'map.put("{b["name"]}",map.get("{b["parent"]}").getChild("{b["name"]}"));')
lines+=['return map;}}']
write(ROOT/'src/main/java/org/slavicmyths/client/FirebirdMesh.java','\n'.join(lines))
data(ROOT/'docs/firebird/source/native-rig.json',{'units':'16 model units per block, Y down, root at feet','bones':bones})
for idx,name in enumerate(['firebird_feather','smoldering_feather','firebird_down']):
 im=Image.new('RGBA',(64,64));dr=ImageDraw.Draw(im)
 if idx<2:
  for j in range(12 if idx==0 else 10):
   x=12+j*3;y=54-j*3;length=(9 if idx==0 else 6)+int(5*math.sin(j/12*math.pi));color=(255,174+j*5,40,255) if idx==0 else (175+j*5,40+j*4,15,255)
   dr.polygon([(x,y),(x-length,y-2),(x-length+3,y-8),(x+2,y-4)],fill=color);dr.polygon([(x,y),(x+length,y+1),(x+length,y-5),(x+2,y-4)],fill=color)
  dr.line((10,58,49 if idx==0 else 43,17 if idx==0 else 24),fill=(255,235,134,255) if idx==0 else (246,116,27,255),width=2)
  if idx==1:
   for x,y in [(18,38),(31,28),(42,23)]:dr.rectangle((x,y,x+3,y+2),fill=(48,22,17,255))
 else:
  for i in range(7):
   a=i*math.tau/7;x=32+math.cos(a)*9;y=36+math.sin(a)*8;dr.ellipse((x-9,y-10,x+9,y+8),fill=(255,130+i*12,35,230));dr.line((32,39,x+math.cos(a)*18,y+math.sin(a)*15),fill=(255,211,95,230),width=2)
  dr.ellipse((26,30,39,44),fill=(255,233,164,255))
 p=A/f'textures/item/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p);data(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
ru=['Перо Жар-птицы','Тлеющее перо','Огненный пух'];en=['Firebird Feather','Smoldering Feather','Firebird Down']
for locale,names in [('ru_ru',ru),('en_us',en)]:
 p=A/f'lang/{locale}.json';v=json.loads(p.read_text(encoding='utf8'));v['entity.slavicmyths.firebird']='Жар-птица' if locale=='ru_ru' else 'Firebird'
 v['item.slavicmyths.firebird_spawn_egg']='Яйцо призыва Жар-птицы' if locale=='ru_ru' else 'Firebird Spawn Egg'
 for n,label in zip(['firebird_feather','smoldering_feather','firebird_down'],names):v['item.slavicmyths.'+n]=label
 data(p,v)
data(A/'models/item/firebird_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
data(D/'slavicmyths/damage_type/firebird_retaliation.json',{'message_id':'firebird_retaliation','scaling':'when_caused_by_living_non_player','exhaustion':.1})
tag=D/'minecraft/tags/damage_type/bypasses_armor.json';v=json.loads(tag.read_text()) if tag.exists() else {'replace':False,'values':[]}
if 'slavicmyths:firebird_retaliation' not in v['values']:v['values'].append('slavicmyths:firebird_retaliation')
data(tag,v);data(D/'slavicmyths/loot_table/entities/firebird.json',{'type':'minecraft:entity','pools':[]})
print(f'{len(bones)} bones, {sum(len(b["boxes"]) for b in bones)} cuboids; three original 64px sprites')
