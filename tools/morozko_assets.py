"""1.9.1 original pixel/voxel assets, deterministic UV, shared boss/item staff.
References are shape/palette guides; poster pixels are never used as textures.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json, math, random, subprocess, shutil, wave, struct
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/morozko-1.9.1'
PALETTE={'navy':('203B6D','182F54','294E80'),'fur':('DDE7EF','B8C9D6','F2F6FA'),'wood':('49392E','332A2A','655044'),'metal':('A7B4C6','606979','DDE7EF'),'skin':('D6B39B','B69586','EDCDB3'),'boot':('383D46','24282F','535B65'),'ice':('AEE8FF','6EC9FC','D8F7FF'),'eye':('53CAFF','3194DF','E9FCFF')}
def write(p,n):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(n,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def merge(p,n):old=json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {};old.update(n);write(p,old)
class Rig:
 def __init__(self):self.bones={};self.boxes=[]
 def bone(self,n,p,x,y,z):self.bones[n]={'parent':p,'pos':[x,y,z]}
 def box(self,n,x,y,z,w,h,d,m):self.boxes.append({'bone':n,'center':[x,y+h/2,z],'size':[w,h,d],'material':m})
def staff(r,n,x=0,y=0,z=0):
 r.bone(n+'_head',n,x,y+44,z);r.bone(n+'_crystals',n+'_head',x,y+50,z)
 b=lambda cx,yy,zz,w,h,d,m:r.box(n+'_crystals' if m in ['ice','eye'] and yy>=31 else n+'_head' if yy>=41 else n,cx+x,yy+y,zz+z,w,h,d,m)
 b(0,0,0,3,44,3,'wood');b(0,0,0,4,4,4,'metal');b(0,1,-2.1,2,2,.4,'eye')
 for yy in [7,19,34,41]:b(0,yy,0,4.5,1.5,4.5,'metal')
 for yy in [9,23]:
  b(0,yy,-1.6,2.5,8,.5,'navy')
  for off in range(5):b((off%2)*.7-.35,yy+off*1.5,-1.95,1,.7,.3,'fur')
 b(0,42,0,6,3,5,'metal');b(0,43,-2.7,2,1,.5,'eye')
 # Seven separate prongs; step taper creates a faceted crown, not a trident.
 for cx,yy,h in [(0,44,18),(-4,45,14),(4,45,14),(-8,46,8),(8,46,8),(-5,42,6),(5,42,6)]:
  b(cx,yy,0,3,h-3,3,'ice');b(cx,yy+h-3,0,2,2,2,'ice');b(cx,yy+h-1,0,1,1,1,'eye');b(cx,yy+2,-1.6,1,max(1,h-6),.4,'eye')
 b(0,48,0,17,2,2,'ice')
 for sign in [-1,1]:
  for step in range(4):b(sign*(2+step*2),49+step*2,-2,3,3,2,'ice')
 for cx,yy in [(-7,37),(7,37),(-4,32),(4,32)]:b(cx,yy,0,2.5,5,2.5,'ice');b(cx,yy-1,0,1.5,1,1.5,'eye')
def boss():
 r=Rig();r.bone('pelvis',None,0,24,0);r.bone('torso','pelvis',0,42,0);r.box('torso',0,22,0,22,22,13,'navy');r.box('pelvis',0,23,-.1,23,3,13.6,'wood');r.box('pelvis',0,23.3,-7.4,5,4,1,'metal')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone('leg_'+tag,'pelvis',side*6,24,0);r.box('leg_'+tag,side*6,8,0,8,16,8,'boot');r.bone('boot_'+tag,'leg_'+tag,side*6,9,0);r.box('boot_'+tag,side*6,0,-1,8,9,9,'boot');r.box('boot_'+tag,side*6,7,-1,8.5,2,9.5,'fur')
  r.bone('upper_arm_'+tag,'torso',side*14,41,0);r.box('upper_arm_'+tag,side*14,28,0,7,13,9,'navy');r.bone('forearm_'+tag,'upper_arm_'+tag,side*14,29,0);r.box('forearm_'+tag,side*14,22,0,7,8,9,'navy');r.box('forearm_'+tag,side*14,22,0,7.6,2,9.6,'fur');r.box('forearm_'+tag,side*14,26,0,7.5,1,9.5,'fur');r.bone('hand_'+tag,'forearm_'+tag,side*14,22,0);r.box('hand_'+tag,side*14,18,0,6.5,5,7,'boot')
  r.bone('shoulder_fur_'+tag,'torso',side*10,42,0);r.box('shoulder_fur_'+tag,side*10,39,0,12,6,15,'fur');r.box('shoulder_fur_'+tag,side*10,43,0,9,4,12,'fur')
  for i in range(3):r.box('shoulder_fur_'+tag,side*(7+i*3.5),37+i%2,-6.8,4,5,3,'fur')
  for direction,zz in [('front',-4),('tail',4)]:
   n='coat_'+direction+'_'+tag;r.bone(n,'pelvis',side*6.5,25,zz);r.box(n,side*6,15,zz,12,10,7,'navy');r.box(n,side*7,8,zz,14,8,7,'navy');r.box(n,side*7,7,zz,14,3,7.5,'fur');r.box(n,side*12,10,zz,2,14,7.8,'fur')
 r.bone('coat_center','pelvis',0,26,-8);r.box('coat_center',0,7,-8,6,20,1,'navy')
 # Geometric repeating embroidery rather than random rune noise.
 for yy in [10,16,22]:
  for dx,dy in [(0,0),(-1,1),(1,1),(0,2)]:r.box('coat_center',dx,yy+dy,-8.8,1,1,.4,'fur')
 for tag in ['left','right']:
  x=6 if tag=='left' else -6
  for yy in [11,17]:
   for dx,dy in [(-2,0),(-1,1),(0,2),(1,1),(2,0)]:r.box('coat_front_'+tag,x+dx,yy+dy,-7.8,1,1,.5,'fur')
 # Separate back ornament plate + voxel snowflake, visible from the rear.
 r.bone('back_emblem','torso',0,36,7);r.box('back_emblem',0,20,7,14,21,.5,'navy')
 for yy in range(22,40,2):
  off=abs(31-yy)*.45
  for sign in [-1,1]:r.box('back_emblem',sign*off,yy,7.5,2,2,.5,'fur')
 for i in range(-5,6):r.box('back_emblem',i,29,7.7,1,2,.5,'fur')
 r.bone('head','torso',0,44,0);r.box('head',0,41,0,11,10,10,'skin');r.box('head',0,43,-6,3,3,3,'skin');r.bone('hair_back','head',0,49,3);r.box('hair_back',0,42,4.8,11.5,10,3,'fur')
 for side in [-1,1]:
  r.box('head',side*5.4,46,0,2,6,10.5,'fur');r.box('head',side*5.5,43,-3.7,2,4,3,'fur');r.box('head',side*2.7,46,-5.3,2,2,.7,'eye');r.bone('brow_'+('l' if side<0 else 'r'),'head',side*2.8,48,-5.3);r.box('brow_'+('l' if side<0 else 'r'),side*2.8,48,-5.5,3.5,1.5,1.3,'fur')
 for i in range(5):r.box('head',-4+i*2,49.5+i%2*.5,-4.4,2.5,2,2,'fur')
 r.bone('beard_main','head',0,44,-6.3);r.box('beard_main',0,35,-6.3,11,9,4,'fur');r.box('beard_main',0,32,-7,9,6,4,'fur');r.bone('beard_tips','beard_main',0,35,-7)
 for x,h in [(-3,6),(0,8),(3,5)]:r.box('beard_tips',x,35-h,-7.1,3,h,3,'fur')
 r.bone('staff_root','hand_right',-16,20,-3.5);staff(r,'staff_root',-16,0,-3.5)
 r.bone('yield_heart','hand_left',14,22,-4);r.box('yield_heart',14,24,-4,4,4,4,'ice');r.box('yield_heart',12.5,27,-4,3,2,3,'eye');r.box('yield_heart',15.5,27,-4,3,2,3,'eye')
 return r
def emit(r):
 size=512;im=Image.new('RGBA',(size,size));x=y=2;row=0;f=lambda n:f'{float(n):.4f}F';lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','public final class MorozkoMesh {public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();']
 for i,b in enumerate(r.boxes):
  w,h,d=b['size'];rw=math.ceil(2*w+2*d);rh=math.ceil(h+d)
  if x+rw+2>size:x=2;y+=row+4;row=0
  assert y+rh+2<=size,(i,'UV overflow');row=max(row,rh);b['uv']=[x,y,rw,rh];colors=[tuple(bytes.fromhex(c))+(255,) for c in PALETTE[b['material']]]
  for py in range(rh):
   for px in range(rw):
    shade=1 if px%7==0 else 2 if (px//3+py//4+i)%11==0 else 0
    if b['material']=='navy' and py>rh*.75 and ((px//2)%4==(py//2)%4 or (px//2)%4==3-(py//2)%4):color=(185,225,249,255)
    else:color=colors[shade]
    im.putpixel((x+px,y+py),color)
  x+=rw+4
 for n,bone in r.bones.items():
  parent=bone['parent'];pos=bone['pos'];pp=r.bones[parent]['pos'] if parent else [0,24,0];c='CubeListBuilder.create()'
  for b in r.boxes:
   if b['bone']!=n:continue
   cx,cy,cz=b['center'];w,h,d=b['size'];u,v,_,_=b['uv'];coords=[cx-w/2-pos[0],pos[1]-cy-h/2,cz-d/2-pos[2],w,h,d];c+=f'.texOffs({u},{v}).addBox('+','.join(map(f,coords))+')'
  offset=[pos[0]-pp[0],pp[1]-pos[1],pos[2]-pp[2]];lines.append(f'var {n}={(parent or "root")}.addOrReplaceChild("{n}",{c},PartPose.offset('+','.join(map(f,offset))+'));')
 lines.append('return LayerDefinition.create(mesh,512,512).bakeRoot();}private MorozkoMesh(){}}');(R/'src/main/java/org/slavicmyths/client/MorozkoMesh.java').write_text('\n'.join(lines)+'\n',encoding='utf8')
 (A/'textures/entity').mkdir(parents=True,exist_ok=True);im.save(A/'textures/entity/morozko.png');glow=Image.new('RGBA',(size,size))
 for b in r.boxes:
  if b['material'] not in ['eye','ice']:continue
  u,v,w,h=b['uv'];glow.paste(im.crop((u,v,u+w,v+h)),(u,v))
 glow.save(A/'textures/entity/morozko_glow.png');clone=im.copy();pixels=clone.load()
 for yy in range(size):
  for xx in range(size):
   rr,gg,bb,aa=pixels[xx,yy]
   if aa:pixels[xx,yy]=(min(220,int(rr*.45+95)),min(245,int(gg*.55+95)),255,145)
 clone.save(A/'textures/entity/morozko_ice_double.png');write(O/'morozko-rig.json',{'atlas':size,'height':52,'bones':r.bones,'boxes':r.boxes})
 # Shared 62u geometry rendered at 50% for the player's hand; exact same boxes/UV.
 held=[]
 for b in r.boxes:
  if not b['bone'].startswith('staff_root'):continue
  x,y,z=b['center'];w,h,d=b['size'];u,v,_,_=b['uv'];k=.5
  lo=[8+(x+16-w/2)*k,-4+(y-h/2)*k,8+(z+3.5-d/2)*k];hi=[lo[0]+w*k,lo[1]+h*k,lo[2]+d*k]
  uv={'west':[u,v+d,u+d,v+d+h],'north':[u+d,v+d,u+d+w,v+d+h],'east':[u+d+w,v+d,u+2*d+w,v+d+h],'south':[u+2*d+w,v+d,u+2*d+2*w,v+d+h],'up':[u+d,v,u+d+w,v+d],'down':[u+d+w,v,u+d+2*w,v+d]}
  held.append({'from':lo,'to':hi,'faces':{face:{'texture':'#surface','uv':[value*16/512 for value in values]} for face,values in uv.items()}})
 write(A/'models/item/morozko_staff_held.json',{'texture_size':[512,512],'textures':{'surface':'slavicmyths:entity/morozko'},'elements':held,'display':{'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[1,1,1]},'firstperson_righthand':{'rotation':[0,-20,0],'translation':[1,1,-2],'scale':[.8,.8,.8]}}})
 write(A/'models/item/morozko_staff.json',{'parent':'builtin/entity','gui_light':'front'});write(A/'models/item/morozko_staff_icon.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/morozko_staff'}})
def icons():
 for name in ['morozko_staff','frost_mirror','frost_resistance_rune','frost_rune','heart_of_eternal_cold','morozko_trial_totem']:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);ice='#AEE8FF';white='#E9FCFF';silver='#A7B4C6';blue='#418ABD';dark='#34435D'
  if name=='morozko_staff':
   d.rectangle((14,10,16,30),fill='#49392E');d.rectangle((13,27,17,29),fill=silver)
   for y in [13,20,24]:d.rectangle((13,y,17,y+1),fill=silver)
   for x,y,h in [(15,1,12),(10,3,8),(20,3,8),(5,6,6),(25,6,6)]:d.polygon([(x,y),(x+2,y+2),(x+2,y+h),(x-1,y+h),(x-1,y+2)],fill=ice,outline=blue);d.line((x,y+2,x,y+h-1),fill=white)
   d.line((5,10,26,10),fill=ice,width=2)
   for x in [9,22]:d.rectangle((x,14,x+1,17),fill=ice)
  elif name=='frost_mirror':
   d.line([(12,13),(7,9),(8,4),(13,1),(20,2),(24,6),(22,12),(18,15)],fill=dark,width=3);d.line([(12,13),(7,9),(8,4),(13,1),(20,2),(24,6),(22,12),(18,15)],fill=silver,width=2)
   for x,y in [(8,5),(10,2),(17,1),(23,5),(22,10)]:d.rectangle((x,y,x+2,y+1),fill=white)
   d.polygon([(15,12),(23,18),(22,25),(16,30),(8,25),(7,18)],fill=silver,outline=dark);d.polygon([(15,15),(20,19),(19,25),(16,27),(10,23),(10,18)],fill=ice,outline=blue);d.line([(15,16),(12,20),(15,24)],fill=white,width=2)
   for x,y in [(5,17),(5,24),(24,18),(23,26)]:d.polygon([(x,y-2),(x+2,y+1),(x,y+4),(x-2,y+1)],fill=ice)
  elif 'rune' in name:
   defensive=name=='frost_resistance_rune';pts=[(10,3),(21,2),(26,8),(24,22),(19,29),(7,28),(3,23),(6,11)] if defensive else [(11,2),(23,4),(26,15),(24,26),(16,30),(4,27),(5,17),(7,7)]
   d.polygon(pts,fill=dark,outline=silver);d.polygon([(10,6),(20,5),(22,11),(20,24),(10,25),(7,20)],fill='#5A799F')
   if defensive:d.line([(8,14),(16,7),(24,14),(16,11),(9,21),(16,17),(22,22)],fill=ice,width=2);d.line((16,12,16,27),fill=white,width=2)
   else:
    d.line((16,6,16,27),fill=white,width=2)
    for y in [11,17,22]:d.line([(9,y-3),(16,y+2),(23,y-3)],fill=ice,width=2)
   for x,y in [(4,20),(23,7),(6,7),(24,23)]:d.polygon([(x,y),(x+2,y-2),(x+3,y+2),(x+1,y+4)],fill=ice)
  elif name=='heart_of_eternal_cold':
   d.polygon([(16,9),(11,4),(6,6),(4,13),(7,20),(16,29),(26,19),(28,11),(24,5),(20,5)],fill=blue,outline=ice);d.polygon([(16,12),(10,9),(8,14),(16,24),(24,14),(22,9)],fill=ice);d.polygon([(16,12),(12,15),(16,20),(20,15)],fill=white)
   for x,y in [(2,20),(28,23),(15,1)]:d.polygon([(x,y),(x+1,y+2),(x,y+4),(x-1,y+2)],fill=ice)
  else:
   d.rectangle((3,27,28,30),fill=silver,outline=dark);d.rectangle((9,23,23,27),fill='#738094');d.rectangle((11,10,21,23),fill=dark,outline=silver);d.rectangle((10,8,22,11),fill=silver)
   for x in [4,25]:d.rectangle((x,19,x+3,27),fill=silver);d.polygon([(x+1,12),(x+3,18),(x,18)],fill=ice)
   d.polygon([(16,2),(18,6),(16,9),(14,6)],fill=ice);d.line((16,13,16,21),fill=white);d.line([(13,15),(19,19)],fill=ice);d.line([(19,15),(13,19)],fill=ice)
  (A/'textures/item').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/item/{name}.png')
  if name!='morozko_staff':write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
def audio():
 names=['ambient','step','staff_hit','ice_bolt','blizzard_start','blizzard_loop','spike_telegraph','spike_break','breath','wave','clone_spawn','clone_break','frost_damage','yield','mirror_summon','mirror_break','staff_shoot','staff_stream','staff_icicles','staff_wave','trial_call'];durations=[2,.2,.35,.45,2.5,4,.9,.45,2.5,.7,2,.65,.3,2.5,.7,.5,.35,1.5,1.2,.6,3]
 ff=shutil.which('ffmpeg')
 if not ff:raise RuntimeError('ffmpeg required for authored OGG')
 sounds={}
 for index,(name,duration) in enumerate(zip(names,durations)):
  rng=random.Random(191+index);rate=22050;path=O/f'{name}.wav';samples=[];smooth=0
  for i in range(int(rate*duration)):
   t=i/rate;progress=t/duration;noise=rng.uniform(-1,1);smooth=smooth*.94+noise*.06;envelope=min(1,t/.06)*min(1,(duration-t)/.12)
   if 'start' in name:envelope*=.3+.7*progress
   elif 'loop' not in name and name not in ['ambient','breath','staff_stream']:envelope*=math.exp(-progress*3)
   crystal=sum(math.sin(2*math.pi*(410+index*47)*harm*t)*math.exp(-t*(2+harm)) /harm for harm in [1,2.37,3.91])*.12
   wind=(smooth*.7+noise*.07)*(1 if name in ['ambient','blizzard_start','blizzard_loop','breath','staff_stream','trial_call'] else .25)
   pulse=math.sin(2*math.pi*70*t)*math.exp(-t*18)*.2 if name in ['staff_hit','wave','staff_wave','step'] else 0
   if name=='staff_icicles':crystal*=.4+.6*max(0,math.cos(t*2*math.pi/ .14))
   samples.append(int(MathClamp((wind+crystal+pulse)*envelope,-.8,.8)*32767))
  with wave.open(str(path),'wb') as out:out.setnchannels(1);out.setsampwidth(2);out.setframerate(rate);out.writeframes(struct.pack('<'+'h'*len(samples),*samples))
  dest=A/f'sounds/morozko/{name}.ogg';dest.parent.mkdir(parents=True,exist_ok=True);subprocess.run([ff,'-hide_banner','-loglevel','error','-y','-i',str(path),'-fflags','+bitexact','-flags:a','+bitexact','-c:a','libvorbis','-q:a','3','-map_metadata','-1',str(dest)],check=True);path.unlink();sounds['morozko.'+name]={'subtitle':'subtitle.slavicmyths.morozko.'+name,'sounds':[{'name':'slavicmyths:morozko/'+name,'attenuation_distance':32 if 'blizzard' in name else 24}]}
 merge(A/'sounds.json',sounds)
def MathClamp(n,a,b):return max(a,min(b,n))
def resources():
 items=[('morozko_staff','Посох Морозко','Morozko’s Staff'),('frost_mirror','Морозное зерцало','Frost Mirror'),('frost_resistance_rune','Руна морозостойкости','Frost Resistance Rune'),('frost_rune','Руна мороза','Frost Rune'),('heart_of_eternal_cold','Сердце вечной стужи','Heart of Eternal Cold'),('morozko_trial_totem','Ледяной тотем призыва Морозко','Morozko Trial Totem')]
 ru={'boss':'Морозко','overworld':'Морозко можно призвать только в Верхнем мире','trust':'Сначала заслужи доверие Бабы-яги или Кота Баюна','wait':'Пока подожди, прежде чем вновь призывать Морозко','active':'Здесь уже проходит испытание','space':'Для испытания Морозко нужно больше свободного места','start':'Морозко явился на зов. Выдержи его стужу!','cancel':'Испытание прервано — участники покинули место боя','success':'Испытание пройдено. Морозко признал твою стойкость','received':'Получено: %s','queued':'Награда сохранена. Освободи место в инвентаре: %s','totem_hint':'Многоразовый вызов испытания по твёрдой поверхности. Только Верхний мир; 120 с, после победы 180 с.','no_target':'Нет подходящей цели для ледяных копий','offering':'Подношение: 8 снеговых блоков и 1 синий лёд','npc_title':'Морозко','npc_offer':'Испытание Морозко: доверие II у Яги или первое угощение Баюна. Подношение: 8 снеговых блоков и 1 синий лёд. Утраченный тотем можно восстановить.','totem_given':'Ледяной тотем доступен. Вызови Морозко на открытом месте в Верхнем мире.','staff_hint':'ЛКМ — ледяной снаряд; удержание ПКМ — морозный поток. Ледяные копья и волну можно переназначить в настройках управления.','staff_stats':'Снаряд 7 HP / 0,8 с; поток 1 HP каждые 0,5 с до 3 с / 10 с; 5 сосулек по последней цели за 8 с / 20 с; волна 4 HP / 16 с.','bindings':'%s — пять сосулек по последней цели; %s — морозная волна','cold':'❄ Стужа: %s / 100','cooldowns':'%s: %s с  ·  %s: %s с','stream':'Поток: %.1f с','mirror_cd':'Зерцало: %s с','compatible.frost':'Оружейное гнездо: меч, кинжал, копьё, тяжёлое оружие, посох, лук, плеть','compatible.frost_resistance':'Бронное гнездо; надетая броня'}
 en={'boss':'Morozko','overworld':'Summon Morozko in the Overworld','trust':'First earn Baba Yaga’s or Bayun’s trust','wait':'Wait before summoning Morozko again','active':'Another trial is already underway nearby','space':'Morozko’s trial needs more open space','start':'Morozko has answered. Endure his frost!','cancel':'Trial cancelled: the participants left','success':'Trial passed. Morozko acknowledges your endurance','received':'Received: %s','queued':'Reward saved. Free inventory space: %s','totem_hint':'Reusable trial summon on solid ground. Overworld only; 120s, 180s after success.','no_target':'No recent valid target for the ice lances','offering':'Offering: 8 snow blocks and 1 blue ice','npc_title':'Morozko','npc_offer':'Morozko’s trial: Yaga trust II or Bayun’s first gift. Offer 8 snow blocks and 1 blue ice. A lost totem can be recovered.','totem_given':'The ice totem is available. Summon Morozko in an open Overworld location.','staff_hint':'Attack: ice bolt. Hold use: frost stream. Rebind icicles and wave in Controls.','staff_stats':'Bolt 7 HP / 0.8s; stream 1 HP per 0.5s for 3s / 10s; 5 icicles at the last target within 8s / 20s; wave 4 HP / 16s.','bindings':'%s: five icicles at last target; %s: frost wave','cold':'❄ Cold: %s / 100','cooldowns':'%s: %ss  ·  %s: %ss','stream':'Stream: %.1fs','mirror_cd':'Mirror: %ss','compatible.frost':'Weapon socket: sword, dagger, spear, heavy weapon, staff, bow, whip','compatible.frost_resistance':'Armor socket; worn armor'}
 descriptions_ru=['Редкое оружие испытания: 15%. ЛКМ — снаряд; ПКМ — поток; ледяные копья и волна — через назначенные клавиши.','Создаёт ледяную копию владельца на 30 с. Прочность: 15 HP. Перезарядка: 180 с. Надеть в слот ожерелья; клавиша в настройках управления.','Защищает от Замедления и Слабости. Не отменяет морозный урон. Вставить в гнездо брони.','25% шанс наложить Замедление I и Слабость I на 4 сек. Вставить в гнездо оружия.','Частица силы Морозко. Пригодится для создания древнего защитного оберега.','Многоразовое испытание Морозко. Получить у Яги при доверии II или у Баюна после угощения за 8 снеговых блоков и 1 синий лёд.']
 descriptions_en=['Rare trial weapon: 15%. Attack: bolt; use: stream; bound keys: icicles and wave.','Creates a 15 HP ice copy for 30s. Cooldown: 180s. Equip in a necklace slot; bind its key in Controls.','Blocks Slowness and Weakness. Does not prevent frost damage. Install in armor.','25% chance of Slowness I and Weakness I for 4s. Install in a weapon.','A fragment of Morozko’s power. A future ingredient for an ancient protective charm.','Reusable Morozko trial summon. Yaga trust II or Bayun’s first gift; offer 8 snow blocks and 1 blue ice.']
 for locale,words,descriptions in [('ru_ru',ru,descriptions_ru),('en_us',en,descriptions_en)]:
  lang={'frost191.'+k:v for k,v in words.items()}
  for (id,russian,english),desc in zip(items,descriptions):lang['item.slavicmyths.'+id]=russian if locale=='ru_ru' else english;lang['item.slavicmyths.'+id+'.desc']=desc
  lang['item.slavicmyths.frost_mirror.effect']=descriptions[1]
  for id,russian,english in [('morozko','Морозко','Morozko'),('morozko_ice_double','Ледяной двойник Морозко','Morozko’s Ice Double'),('player_ice_double','Ледяная копия %s','Ice Copy of %s'),('morozko_ice_bolt','Ледяной снаряд','Ice Bolt'),('morozko_icicle','Ледяной пик','Ice Spike')]:lang['entity.slavicmyths.'+id]=russian if locale=='ru_ru' else english
  lang.update({'key.slavicmyths.frost_icicles':'Посох Морозко: ледяные копья' if locale=='ru_ru' else 'Morozko Staff: Icicles','key.slavicmyths.frost_wave':'Посох Морозко: морозная волна' if locale=='ru_ru' else 'Morozko Staff: Frost Wave','key.slavicmyths.frost_mirror':'Морозное зерцало: создать копию' if locale=='ru_ru' else 'Frost Mirror: Create Copy','key.categories.slavicmyths.frost':'Slavic Myths — Морозко' if locale=='ru_ru' else 'Slavic Myths — Morozko','bayun.dialogue.morozko':'Испытание Морозко' if locale=='ru_ru' else 'Morozko’s Trial','advancement.slavicmyths.endured_frost':'Выдержавший стужу' if locale=='ru_ru' else 'Endured the Frost','advancement.slavicmyths.endured_frost.desc':'Морозко признал твою стойкость' if locale=='ru_ru' else 'Morozko acknowledged your endurance'})
  rumor='Зимний старец испытывает стойкость. Вызови его тотемом от Яги или Баюна. Он не заражён Мором. При 5% здоровья признаёт победу, убивать его не нужно. Награды — руны, кристаллы и редкое оружие. Богатыри могут помочь.' if locale=='ru_ru' else 'The winter elder tests endurance. Summon him with Yaga’s or Bayun’s totem. He is not blighted. At 5% HP he yields; killing is unnecessary. Heroes may help. Rewards include runes, crystals and rare weapons.'
  verified=rumor+(' Каждый успех: сердце и одна руна (50/50); посох — 15%. Зерцало — лишь при первом успехе: надень ожерелье, назначенная клавиша создаёт копию 15 HP на 30 с / 180 с. Защитная руна отменяет Слабость и Замедление, но не урон стужи; атакующая — 25% на 4 с. Посох: снаряд, поток, 5 сосулек по последней цели и волна. Сердце пригодится будущему оберегу; рецепта пока нет.' if locale=='ru_ru' else ' Each success: heart and one rune (50/50); staff 15%. First win only: mirror necklace, 15 HP copy for 30s / 180s. Resistance blocks Slowness and Weakness, not cold damage; offensive rune: 25% for 4s. Staff: bolt, stream, five icicles at last target, wave. The heart is for a future charm; no recipe yet.')
  lang['tales.entry.morozko_trial.title']='Испытание Морозко' if locale=='ru_ru' else 'Morozko’s Trial'
  for stage in ['known','rumor','seen','discovered','studied','verified']:lang['tales.entry.morozko_trial.'+stage]=verified if stage=='verified' else rumor
  for name in ['ambient','step','staff_hit','ice_bolt','blizzard_start','blizzard_loop','spike_telegraph','spike_break','breath','wave','clone_spawn','clone_break','frost_damage','yield','mirror_summon','mirror_break','staff_shoot','staff_stream','staff_icicles','staff_wave','trial_call']:lang['subtitle.slavicmyths.morozko.'+name]=('Морозко: '+{'ambient':'зимний ветер','step':'тяжёлый шаг','staff_hit':'удар посоха','ice_bolt':'ледяной звон','blizzard_start':'буря набирает силу','blizzard_loop':'метель','spike_telegraph':'лёд трещит','spike_break':'лёд раскалывается','breath':'морозное дыхание','wave':'волна холода','clone_spawn':'собираются двойники','clone_break':'двойник разбивается','frost_damage':'стужа ранит','yield':'испытание завершено','mirror_summon':'зерцало пробуждается','mirror_break':'зерцало рассыпается','staff_shoot':'ледяной выстрел','staff_stream':'морозный поток','staff_icicles':'падают сосульки','staff_wave':'морозная волна','trial_call':'зимний зов'}[name]) if locale=='ru_ru' else 'Morozko: '+name.replace('_',' ')
  for id in ['morozko_frost','morozko_aura','morozko_stream','morozko_mirror']:
   lang['death.attack.'+id]='%s не выдержал стужи' if locale=='ru_ru' else '%s succumbed to frost';lang['death.attack.'+id+'.player']='%s не выдержал стужи %s' if locale=='ru_ru' else '%s succumbed to %s’s frost'
  merge(A/f'lang/{locale}.json',lang)
 for name in ['morozko_frost','morozko_aura','morozko_stream','morozko_mirror']:write(D/f'damage_type/{name}.json',{'message_id':name,'scaling':'when_caused_by_living_non_player','exhaustion':.1})
 # Independent reflected hits and five separate impacts must not vanish in vanilla hurt immunity.
 tag=R/'src/main/resources/data/minecraft/tags/damage_type/bypasses_cooldown.json';old=json.loads(tag.read_text()) if tag.exists() else {'replace':False,'values':[]};old['values']=list(dict.fromkeys(old['values']+['slavicmyths:morozko_mirror','slavicmyths:morozko_icicle_damage']));write(tag,old)
 write(D/'damage_type/morozko_icicle_damage.json',{'message_id':'morozko_frost','scaling':'when_caused_by_living_non_player','exhaustion':.1})
 tag=R/'src/main/resources/data/curios/tags/item/necklace.json';old=json.loads(tag.read_text()) if tag.exists() else {'replace':False,'values':[]};old['values']=list(dict.fromkeys(old['values']+['slavicmyths:frost_mirror']));write(tag,old)
 write(D/'tags/item/rune_staff.json',add_tag(D/'tags/item/rune_staff.json','slavicmyths:morozko_staff'))
 write(D/'tales/morozko_trial.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tales.entry.morozko_trial.title','icon':'slavicmyths:morozko_trial_totem','initial':0,'subjects':['entity/slavicmyths:morozko'],'advancements':['slavicmyths:endured_frost'],'links':[]})
 write(D/'advancement/endured_frost.json',{'parent':'slavicmyths:root','display':{'icon':{'id':'slavicmyths:heart_of_eternal_cold'},'title':{'translate':'advancement.slavicmyths.endured_frost'},'description':{'translate':'advancement.slavicmyths.endured_frost.desc'},'frame':'challenge','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}})
 crystal=Image.new('RGBA',(64,64),'#AEE8FF');cd=ImageDraw.Draw(crystal)
 for x in range(0,64,4):cd.rectangle((x,0,x+1,63),fill='#6EC9FC');cd.line((x+2,0,x+2,63),fill='#D8F7FF')
 crystal.save(A/'textures/entity/morozko_crystal.png')
 # Heart is a faceted voxel item in hand, with a separate readable inventory silhouette.
 heart=[]
 for lo,hi in [([4,8,5],[8,12,10]),([8,8,5],[12,12,10]),([3,6,5],[13,9,10]),([4,4,5.5],[12,7,9.5]),([5,2,6],[11,5,9]),([7,1,6.5],[9,3,8.5]),([6,6,4.5],[10,10,5.5]),([1,7,7],[2,10,8]),([14,6,7],[15,9,8]),([7,14,7],[9,16,9])]:heart.append({'from':lo,'to':hi,'faces':{f:{'texture':'#surface','uv':[0,0,4,4]} for f in ['up','down','north','south','east','west']}})
 write(A/'models/item/heart_of_eternal_cold_held.json',{'textures':{'surface':'slavicmyths:entity/morozko_crystal'},'elements':heart,'display':{'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,1,0],'scale':[.7,.7,.7]},'firstperson_righthand':{'rotation':[0,45,0],'translation':[0,2,0],'scale':[.7,.7,.7]}}})
 write(A/'models/item/heart_of_eternal_cold_icon.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/heart_of_eternal_cold'}})
 write(A/'models/item/heart_of_eternal_cold.json',{'parent':'builtin/entity','gui_light':'front'})
def add_tag(path,item):
 old=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]};old['values']=list(dict.fromkeys(old['values']+[item]));return old
def main():
 O.mkdir(parents=True,exist_ok=True);r=boss();emit(r);icons();audio();resources();from summon_access_policy import apply;apply();print('Morozko:',len(r.bones),'bones;',len(r.boxes),'boxes; 6 icons; 21 original OGG')
if __name__=='__main__':
 main()
 from reward_weapon_texts import apply
 apply()
