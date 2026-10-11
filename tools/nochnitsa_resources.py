"""Original code-native voxel rig/128px art following the two supplied Nochnitsa sheets."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data';S=R/'docs/nochnitsa/source';S.mkdir(parents=True,exist_ok=True)
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
palette={'skin':((0,0,63,31),'#B5BBC9','#747987','#D0D6DF'),'dress':((0,32,63,79),'#454B5D','#272B38','#626A80'),'hair':((64,0,127,31),'#1C202B','#101219','#343948'),'claw':((64,32,95,63),'#54414D','#28212C','#876573'),'shadow':((96,32,127,63),'#747987','#4A4E5B','#9298A8'),'eye':((96,64,127,79),'#EAF1FF','#EAF1FF','#FFFFFF')}
atlas=Image.new('RGBA',(128,128));glow=Image.new('RGBA',(128,128));draw=ImageDraw.Draw(atlas);gd=ImageDraw.Draw(glow)
for name,(bounds,base,shadow,high) in palette.items():
 x0,y0,x1,y1=bounds;draw.rectangle(bounds,fill=base)
 if name=='eye':gd.rectangle(bounds,fill=base);continue
 for x in range(x0,x1+1):
  if name=='hair':color=high if (x-x0)%7==1 else shadow if (x-x0)%7==5 else base;draw.line((x,y0,x,y1),fill=color)
  elif name=='dress':
   for y in range(y0,y1+1):
    stripe=(x-x0+(y-y0)//9)%11
    if stripe in [0,1]:draw.point((x,y),fill=shadow)
    elif stripe==6:draw.point((x,y),fill=high)
    if (x-x0)%17==4 and (y-y0)%13 in [4,5,6]:draw.point((x,y),fill=shadow)
  elif name=='skin':
   for y in range(y0,y1+1):
    if (x+y//4)%9 in [0,1]:draw.point((x,y),fill=high)
    if (x//2+y//3)%13==0:draw.point((x,y),fill=shadow)
  elif name=='claw':draw.line((x,y0,x,y1),fill=shadow if (x-x0)%5<2 else high if (x-x0)%5==4 else base)
  else:draw.line((x,y0,x,y1),fill=shadow if (x-x0)%5==0 else base)
for name,im in [('nochnitsa',atlas),('nochnitsa_emissive',glow)]:p=A/f'textures/entity/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
bones=[];serial=0
def bone(name,parent,pivot,boxes=(),rot=(0,0,0)):
 global serial
 cubes=[]
 for box in boxes:
  x,y,z,w,h,d,material=box;bounds=palette[material][0];u=bounds[0]+1;v=bounds[1]+1
  assert u+2*(w+d)<bounds[2]+1 and v+h+d<bounds[3]+1,(name,material)
  # Tiny, unique shells keep layered same-plane details from fighting at rest.
  serial+=1;cubes.append({'origin':[x,y,z],'size':[w,h,d],'material':material,'uv':[u,v],'shell':(serial%13)*.00071})
 bones.append({'name':name,'parent':parent,'pivot':pivot,'rotation':rot,'cubes':cubes})
bone('pelvis','root',(0,-18,0),[(-2.9,-1.5,-2,5.8,5,4,'dress')])
bone('spine_low','pelvis',(0,-1,0),[(-2.9,-5,-1.9,5.8,5.5,3.8,'dress')])
bone('spine_upper','spine_low',(0,-5,0),[(-3.25,-5,-1.9,6.5,7,3.8,'dress'),(-3,-5.1,-2.1,2.4,.7,1,'skin'),(.6,-5.1,-2.1,2.4,.7,1,'skin')],(.19,0,0))
bone('neck','spine_upper',(0,-5,-.5),[(-1.05,-2.5,-1.15,2.1,3,2.3,'skin')],(-.087,0,0))
bone('head','neck',(0,-1.8,-.6),[(-2.4,-3.2,-2.1,4.8,6.5,4.2,'skin'),(-.48,-.35,-2.4,.96,2.4,.6,'shadow'),(-2.05,.3,-2.22,.8,1.4,.36,'shadow'),(1.25,.3,-2.22,.8,1.4,.36,'shadow')],(-.10,-.07,0))
bone('jaw','head',(0,2,-1.45),[(-1.3,-.25,-.8,2.6,1.3,1.8,'skin'),(-.9,-.3,-.96,1.8,.3,.22,'hair')])
for side,sign in [('l',1),('r',-1)]:
 bone('socket_'+side,'head',(sign*1.2,-.9,-2.24),[(-.64,-.66,-.1,1.28,1.32,.25,'hair')])
 bone('eye_'+side,'socket_'+side,(0,0,-.14),[(-.43,-.39,-.12,.86,.78,.25,'eye')])
bone('hair_cap','head',(0,-3.05,.15),[(-2.8,-1.35,-2.6,5.6,1.5,5.2,'hair')])
hair=[]
for i,(x,z,length) in enumerate([(-2.5,-2.35,11),(-2.3,-2.55,7),(-.2,-2.62,2.2),(2.25,-2.5,9),(2.55,-2.25,12),(-2.65,-1.25,14),(-2.75,.1,16),(-2.7,1.45,17),(2.7,-1.1,13),(2.8,.25,15),(2.6,1.6,16),(-2.05,2.35,17),(-1.2,2.6,18),(-.35,2.7,16),(.5,2.75,18),(1.25,2.65,17),(2.1,2.4,16),(-1.7,2.85,15),(-.8,3.0,17),(.1,3.05,18),(1.0,3.0,16),(1.9,2.8,15)]):
 name=('hair_front_' if z<0 else 'hair_back_')+str(i);hair.append(name);w=.7+i%3*.23;d=.7+i%2*.22;split=length*.67
 bone(name,'head',(x,-2.7-i%3*.25,z),[(-w/2,0,-d/2,w,split+.7,d,'hair')],(.02 if z<0 else -.07,(i%3-1)*.035,(1 if x>0 else -1)*.025))
 bone(name+'_tip',name,(0,split,0),[(-w*.4,0,-d*.42,w*.8,length-split,d*.84,'hair')],(0,0,(i%2-.5)*.035))
for side,sign in [('l',1),('r',-1)]:
 bone('shoulder_'+side,'spine_upper',(sign*4,-3,0),[(-1.2,-.8,-1.3,2.4,2.2,2.6,'skin')])
 bone('upper_arm_'+side,'shoulder_'+side,(0,.7,0),[(-.95,0,-1,1.9,7.8,2,'skin')])
 bone('elbow_'+side,'upper_arm_'+side,(0,7.8,0),[(-1,-.6,-1.05,2,1.2,2.1,'shadow')],(.122,0,0))
 bone('forearm_'+side,'elbow_'+side,(0,.1,0),[(-.75,0,-.8,1.5,8,1.6,'skin')])
 bone('wrist_'+side,'forearm_'+side,(0,8,0),[(-.65,-.3,-.6,1.3,.8,1.2,'skin')],(.21,0,0))
 bone('palm_'+side,'wrist_'+side,(0,.5,0),[(-1.3,0,-.5,2.6,2.5,1,'skin')])
 for digit,length in enumerate([3.1,4.2,3.8,2.8]):
  x=(digit-1.5)*.8;bone(f'claw_{side}_{digit+1}','palm_'+side,(x,2.2,-.1),[(-.25,0,-.25,.5,1.6,.5,'skin')],(-.2,0,(digit-1.5)*.14))
  bone(f'claw_{side}_{digit+1}_tip',f'claw_{side}_{digit+1}',(0,1.4,0),[(-.22,0,-.23,.44,length-1.4,.46,'claw'),(-.12,length-1.8,-.35,.24,.8,.25,'claw')],(-.32,0,0))
 bone('hip_'+side,'pelvis',(sign*1.8,1,0),[])
 bone('thigh_'+side,'hip_'+side,(0,0,0),[(-1.2,0,-1.3,2.4,7.5,2.6,'skin')])
 bone('knee_'+side,'thigh_'+side,(0,7.5,0),[(-1,-.5,-1.1,2,1.2,2.2,'shadow')])
 bone('shin_'+side,'knee_'+side,(0,0,0),[(-.9,0,-1,1.8,8,2,'skin')])
 bone('ankle_'+side,'shin_'+side,(0,8,0),[(-.8,-.5,-.9,1.6,1.5,1.8,'skin')])
 bone('foot_'+side,'ankle_'+side,(0,.5,-.4),[(-1,-.1,-1.9,2,1.1,3.4,'skin')])
skirt=[]
for i,(x,z,w,h,angle) in enumerate([(-2,-2.3,3.6,11,.1),(1.7,-2.5,3.7,12,-.12),(-3.4,0,3,10,.22),(3.4,.2,3.2,11,-.23),(-2,2.2,3.5,12,.12),(1.8,2.25,3.6,10,-.14)]):
 name=['skirt_front_l','skirt_front_r','skirt_side_l','skirt_side_r','skirt_back_l','skirt_back_r'][i];skirt.append(name)
 bone(name,'pelvis',(x,1,z),[(-w/2,0,-.35,w,h*.6,.7,'dress')],(0,0,angle))
 for j in range(3):
  width=w/3-.1;length=h*.4+(j%2)*1.2-i%2*.6;bone(name+'_tatter'+str(j),name,((j-1)*w/3,h*.58,0),[(-width/2,0,-.30,width,length,.6,'dress')],(.02*(j-1),0,angle*.2))
bone('chest_seam','spine_upper',(0,-4,-2),[(-.4,0,-.1,.8,6,.3,'shadow')])
lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','public final class NochnitsaMesh {public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();']
f=lambda v:f'{v:.5f}F'
for b in bones:
 expr='CubeListBuilder.create()'
 for c in b['cubes']:expr+=f'.texOffs({c["uv"][0]},{c["uv"][1]}).addBox('+','.join(map(f,c['origin']+c['size']))+',new CubeDeformation('+f(c['shell'])+'))'
 lines.append(f'var {b["name"]}={b["parent"]}.addOrReplaceChild("{b["name"]}",{expr},PartPose.offsetAndRotation('+','.join(map(f,list(b['pivot'])+list(b['rotation'])))+'));')
lines+=['var baked=LayerDefinition.create(mesh,128,128).bakeRoot();baked.setInitialPose(PartPose.offset(0,24,0));baked.resetPose();return baked;}','public static java.util.Map<String,ModelPart> index(ModelPart root){var map=new java.util.LinkedHashMap<String,ModelPart>();map.put("root",root);']
for b in bones:lines.append(f'map.put("{b["name"]}",map.get("{b["parent"]}").getChild("{b["name"]}"));')
lines+=['return map;}private NochnitsaMesh(){}}'];p=R/'src/main/java/org/slavicmyths/client/NochnitsaMesh.java';p.write_text('\n'.join(lines)+'\n')
write(S/'native-rig.json',{'units_per_block':16,'axis':'Y down, root feet y24, front -Z','hair_bones':hair,'skirt_bones':skirt,'bones':bones})
write(D/'slavicmyths/tags/worldgen/biome/nochnitsa_surface.json',{'replace':False,'values':['minecraft:'+n for n in ['plains','forest','birch_forest','dark_forest','taiga']]})
write(D/'slavicmyths/neoforge/biome_modifier/nochnitsa.json',{'type':'slavicmyths:nochnitsa_spawns'})
write(D/'slavicmyths/loot_table/entities/nochnitsa.json',{'type':'minecraft:entity','pools':[]})
write(D/'slavicmyths/tags/entity_type/nochnitsa_recruitable.json',{'replace':False,'values':['slavicmyths:'+n for n in ['upyr','ovinnik','volkolak','podzemnik']]})
write(D/'slavicmyths/tags/entity_type/nochnitsa_villager_hunters.json',{'replace':False,'values':[]})
write(A/'models/item/nochnitsa_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
subtitles={'ambient':('Шёпот Ночницы','Nochnitsa whispers'),'breath':('Дыхание Ночницы','Nochnitsa breathes'),'step':('Шаги Ночницы','Nochnitsa steps'),'claw_swing':('Взмах когтей Ночницы','Nochnitsa claws swing'),'hurt':('Ночница ранена','Nochnitsa hurts'),'scream_inhale':('Ночница готовит крик','Nochnitsa inhales'),'scream_release':('Крик Ночницы','Nochnitsa screams'),'scream_interrupt':('Крик Ночницы прерван','Nochnitsa scream interrupted'),'target_spotted':('Ночница замечает цель','Nochnitsa spots a target'),'sunrise_dissolve':('Ночница рассеивается','Nochnitsa dissolves'),'death':('Ночница погибает','Nochnitsa dies'),'robes_rustle':('Шелест платья Ночницы','Nochnitsa robes rustle')}
for language,idx in [('ru_ru',0),('en_us',1)]:
 p=A/f'lang/{language}.json';v=json.loads(p.read_text(encoding='utf8'));v['entity.slavicmyths.nochnitsa']=('Ночница','Nochnitsa')[idx];v['item.slavicmyths.nochnitsa_spawn_egg']=('Яйцо призыва Ночницы','Nochnitsa Spawn Egg')[idx]
 for k,labels in subtitles.items():v['subtitles.slavicmyths.nochnitsa.'+k]=labels[idx]
 write(p,v)
print(len(bones),'bones;',sum(len(b['cubes']) for b in bones),'cuboids;',len(hair),'articulated hair strands')



Image.new("RGBA",(4,4),(89,108,148,255)).save(A/"textures/entity/nochnitsa_wave.png")
