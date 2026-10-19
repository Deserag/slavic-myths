"""Original reference-guided 1.9.2 geometry/pixels/sounds; never uses poster pixels."""
from pathlib import Path
from PIL import Image,ImageDraw
import math,json,random,wave,struct,subprocess,shutil
from morozko_assets import Rig,write,merge,add_tag
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/karachun-1.9.2'
PAL={'cloth':['10121B','242735','313441'],'fur':['42404A','34333D','53515E'],'skin':['7A7B84','555760','A2A6B2'],'hair':['8A8B94','676974','B4B8C4'],'bone':['BAC4D8','7B8291','D0D3DE'],'wood':['252025','17151C','44323C'],'metal':['242331','14131F','464255'],'ice':['6A70A1','434B76','A9C6EE'],'glow':['6F49BC','8E68DC','C5ABFF']}
def scythe(r,n,x,y,z):
 r.box(n,x,y,z,2,42,2,'wood')
 for yy in [1,8,16,24,32,39]:r.box(n,x,y+yy,z,3,1.5,3,'bone' if yy in [1,24] else 'metal')
 for yy in [9,19,29]:r.box(n,x,y+yy,z,2.5,4,2.5,'wood');r.box(n,x,y+yy+.6,z-1.4,.6,2.5,.3,'glow')
 # Thick asymmetric crescent: lateral sweep and descending taper; never an axe silhouette.
 for i in range(13):
  xx=x-i*1.8;yy=y+42-(i/12)**2*11;h=4.5-i*.23
  r.box(n,xx,yy,z,2.5,h,2.4,'metal');r.box(n,xx,yy-.3,z-.05,2.1,.65,2.7,'glow')
  if i%2==0:r.box(n,xx,yy-2.5-i*.16,z,.9,2.5+i*.15,1.3,'ice')
 for off in [3,9]:
  for j in range(5):r.box(n,x-off,y+36-j*1.5,z,.65,.85,.65,'bone')
  r.box(n,x-off,y+27,z,1.3,2,1.3,'ice')
 r.box(n,x,y,z,3.5,2.5,3.5,'bone');r.box(n,x,y+.6,z-1.85,1,1,.2,'glow')
def boss():
 r=Rig();r.bone('pelvis',None,0,24,0);r.bone('torso','pelvis',0,48,0);r.box('torso',0,27,1,16,23,10,'cloth');r.box('pelvis',0,24,0,18,3,11,'wood')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone('leg_'+tag,'pelvis',side*5,24,0);r.box('leg_'+tag,side*5,5,0,7,19,7,'cloth');r.box('leg_'+tag,side*5,0,-1.2,8,6,9,'metal')
  r.bone('arm_'+tag,'torso',side*11,46,0);r.box('arm_'+tag,side*11,31,0,7,15,8,'cloth');r.bone('forearm_'+tag,'arm_'+tag,side*11,32,0);r.box('forearm_'+tag,side*11,21,0,6,11,7,'cloth');r.bone('hand_'+tag,'forearm_'+tag,side*11,22,0);r.box('hand_'+tag,side*11,17,0,5,5,5,'skin')
  r.bone('fur_'+tag,'torso',side*10,48,0)
  for i in range(7):
   xx=side*(6+i*1.7);r.box('fur_'+tag,xx,42-i*.5,1,3,4,12,'fur');r.box('fur_'+tag,xx,39-i*.6,-5,2,4,3,'fur')
   if i%2==0:r.box('fur_'+tag,xx,36-i*.6,-5.5,.9,3,1,'ice')
  for face,z in [('front',-6),('back',6),('side',1)]:
   n='cloak_'+face+'_'+tag;r.bone(n,'pelvis',side*7,27,z)
   for layer in range(4):
    y=25-layer*6;xx=side*(7+layer*.7) if face!='side' else side*(11+layer*.8)
    r.box(n,xx,y-7,z,8 if face!='side' else 3,8,2 if face!='side' else 11,'cloth')
    for j in range(4):r.box(n,xx-3+j*2,y-10-(j%2),z-1.3,1.3,5+(j%2),.7,'fur')
  r.bone('chains_'+tag,'torso',side*6,41,-6)
  for j in range(12):r.box('chains_'+tag,side*(6+math.sin(j*.4)),39-j*1.5,-6.6,.9,1.1,.8,'bone')
  for j in [2,6,10]:r.box('chains_'+tag,side*7,38-j*1.5,-7,2,2,1,'bone');r.box('chains_'+tag,side*7,38.5-j*1.5,-7.6,.8,.5,.3,'metal')
 r.bone('head','torso',0,52,-2);r.box('head',0,48,-3,9,11,8,'skin');r.box('head',0,49,-7,2,4,2,'skin')
 for side in [-1,1]:
  r.box('head',side*2.5,53,-7.3,3,2.3,.7,'metal');r.box('head',side*2.5,53.5,-7.8,1.5,.8,.4,'glow');r.box('head',side*3.5,50,-7.2,2,2.8,1,'skin')
  n='hair_'+('right' if side<0 else 'left');r.bone(n,'head',side*5,59,-2)
  for j in range(5):r.box(n,side*(4.5+j*.45),47-j*2,-5+j*1.6,1.8,11+j*2,2,'hair')
 r.bone('hair_back','head',0,59,2)
 for i in range(6):r.box('hair_back',-5+i*2,43+i%3,3,1.8,16-i%3,2,'hair')
 r.bone('beard','head',0,50,-7.5)
 for i in range(5):r.box('beard',-4+i*2,40+abs(i-2),-7.5,1.7,10-abs(i-2),2,'hair')
 r.bone('scythe','hand_right',-14,20,-3);scythe(r,'scythe',-14,2,-3)
 for b in r.boxes:
  bottom=b['center'][1]-b['size'][1]/2
  if bottom<0:b['size'][1]+=bottom;b['center'][1]=b['size'][1]/2
 return r
def raven():
 r=Rig();r.bone('bird',None,0,24,0);r.box('bird',0,20,0,5,5,8,'fur');r.bone('head','bird',0,25,-4);r.box('head',0,23,-4,4,4,4,'metal');r.box('head',0,23.5,-7,2.5,2,3,'metal');r.box('head',0,23,-9,1.5,1,2,'metal')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.box('head',side*2.05,25,-5.2,.3,.8,1.1,'glow');r.bone('wing_'+tag,'bird',side*2.5,24,0)
  for i in range(6):
   xx=side*(3.5+i*1.3);r.box('wing_'+tag,xx,22+i*.1,i*.25,2.4,1.2,5+i*.5,'metal');r.box('wing_'+tag,xx,21.8,i*.5+2.8,1.2,.7,3+i*.25,'fur');r.box('wing_'+tag,xx,21.6,i*.5+5,1,.5,1,'ice')
  r.bone('claw_'+tag,'bird',side*1.4,21,1);r.box('claw_'+tag,side*1.4,17.5,1,.6,3,.6,'metal')
  for i in range(3):r.box('claw_'+tag,side*1.4+(i-1)*.6,17.2,-.2,.4,.5,2,'bone')
 r.bone('tail','bird',0,22,4)
 for i in range(5):r.box('tail',-2+i,20.2,5+i%2,1,1,5,'fur')
 # Feet at entity origin: do not reuse the humanoid's y=24 pivot as a flying hitbox offset.
 for bone in r.bones.values():bone['pos'][1]-=17.2
 for b in r.boxes:b['center'][1]-=17.2
 return r
def emit(r,name,cls,size=512):
 im=Image.new('RGBA',(size,size));x=y=2;row=0
 for i,b in enumerate(r.boxes):
  w,h,d=b['size'];rw=math.ceil(2*w+2*d);rh=math.ceil(h+d)
  if x+rw+3>size:x=2;y+=row+3;row=0
  assert y+rh+3<=size;row=max(row,rh);b['uv']=[x,y,rw,rh];colors=[tuple(bytes.fromhex(c))+(255,) for c in PAL[b['material']]]
  for yy in range(rh):
   for xx in range(rw):im.putpixel((x+xx,y+yy),colors[1 if xx%5==0 else 2 if (xx//2+yy//3+i)%9==0 else 0])
  x+=rw+3
 f=lambda n:f'{float(n):.4f}F';lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;',f'public final class {cls} {{public static ModelPart create(){{var mesh=new MeshDefinition();var root=mesh.getRoot();']
 for n,bone in r.bones.items():
  parent=bone['parent'];pos=bone['pos'];pp=r.bones[parent]['pos'] if parent else [0,24,0];c='CubeListBuilder.create()'
  for b in r.boxes:
   if b['bone']!=n:continue
   cx,cy,cz=b['center'];w,h,d=b['size'];u,v,_,_=b['uv'];c+=f'.texOffs({u},{v}).addBox('+','.join(map(f,[cx-w/2-pos[0],pos[1]-cy-h/2,cz-d/2-pos[2],w,h,d]))+')'
  offset=[pos[0]-pp[0],pp[1]-pos[1],pos[2]-pp[2]];lines.append(f'var {n}={(parent or "root")}.addOrReplaceChild("{n}",{c},PartPose.offset('+','.join(map(f,offset))+'));')
 lines.append(f'return LayerDefinition.create(mesh,{size},{size}).bakeRoot();}}private {cls}(){{}}}}');(R/f'src/main/java/org/slavicmyths/client/{cls}.java').write_text('\n'.join(lines)+'\n',encoding='utf-8')
 from weapon_grip_58_mesh import apply_mesh
 apply_mesh(cls)
 (A/'textures/entity').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/entity/{name}.png');glow=Image.new('RGBA',(size,size))
 for b in r.boxes:
  if b['material']!='glow':continue
  u,v,w,h=b['uv'];glow.paste(im.crop((u,v,u+w,v+h)),(u,v))
 glow.save(A/f'textures/entity/{name}_glow.png');write(O/f'{name}-rig.json',{'atlas':size,'bones':r.bones,'boxes':r.boxes})
 return im
def held(r):
 elements=[]
 for b in r.boxes:
  if b['bone']!='scythe':continue
  cx,cy,cz=b['center'];w,h,d=b['size'];u,v,_,_=b['uv'];k=.75
  lo=[8+(cx+14-w/2)*k,-5+(cy-h/2)*k,8+(cz+3-d/2)*k];hi=[lo[0]+w*k,lo[1]+h*k,lo[2]+d*k]
  uv={'west':[u,v+d,u+d,v+d+h],'north':[u+d,v+d,u+d+w,v+d+h],'east':[u+d+w,v+d,u+2*d+w,v+d+h],'south':[u+2*d+w,v+d,u+2*d+2*w,v+d+h],'up':[u+d,v,u+d+w,v+d],'down':[u+d+w,v,u+d+2*w,v+d]}
  elements.append({'from':lo,'to':hi,'faces':{face:{'texture':'#surface','uv':[value*16/512 for value in values]} for face,values in uv.items()}})
 write(A/'models/item/karachun_scythe_held.json',{'textures':{'surface':'slavicmyths:entity/karachun'},'elements':elements,'display':{'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[1,1,1]},'thirdperson_lefthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[1,1,1]},'firstperson_righthand':{'rotation':[0,-10,0],'translation':[0,1,-2],'scale':[.8,.8,.8]},'firstperson_lefthand':{'rotation':[0,10,0],'translation':[0,1,-2],'scale':[.8,.8,.8]}}})
 write(A/'models/item/karachun_scythe.json',{'parent':'builtin/entity','gui_light':'front'});write(A/'models/item/karachun_scythe_icon.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/karachun_scythe'}})
def icons():
 for name in ['karachun_scythe','rune_of_waning','black_wing_amulet','shard_of_last_night','karachun_trial_totem']:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);black='#242735';edge='#676974';ice='#A9C6EE';violet='#AC79FF'
  if name=='karachun_scythe':
   d.rectangle((23,7,25,30),fill='#44323C');d.line([(24,8),(19,5),(13,5),(7,8),(3,16)],fill=black,width=5);d.line([(23,7),(18,4),(12,4),(6,7),(2,15)],fill=violet,width=1)
   for x,y in [(18,8),(13,8),(8,11),(4,15)]:d.polygon([(x,y),(x+2,y),(x,y+5)],fill=ice)
   for y in [10,17,24,29]:d.rectangle((22,y,26,y+1),fill=edge)
  elif name=='rune_of_waning':
   d.polygon([(10,2),(22,4),(26,25),(21,30),(6,27),(5,8)],fill=black,outline=edge);d.line([(16,6),(23,15),(16,24),(9,15),(16,6)],fill=violet,width=2);d.line((16,4,16,27),fill='#D4B8FF',width=1)
   for x,y in [(5,9),(6,21),(23,5),(24,22)]:d.rectangle((x,y,x+3,y+4),fill=ice)
  elif name=='black_wing_amulet':
   d.arc((3,1,28,18),0,180,fill=edge,width=2)
   for s in [-1,1]:
    for i in range(5):x=16+s*(4+i*2);d.polygon([(16+s*3,13),(x,12+i),(x-s,23+i),(16+s*3,18)],fill=black,outline=edge)
   d.polygon([(16,12),(20,18),(16,27),(12,18)],fill=ice);d.line((16,16,16,23),fill=violet,width=2)
  elif name=='shard_of_last_night':
   d.polygon([(18,1),(25,11),(23,25),(12,31),(7,21),(10,7)],fill=black,outline='#6F49BC');d.polygon([(18,3),(18,26),(12,28),(13,13)],fill='#6F49BC');d.line((18,5,16,26),fill=violet,width=2)
   for x,y in [(8,13),(22,20),(12,5)]:d.polygon([(x,y),(x+3,y+2),(x+1,y+6)],fill=ice)
  else:
   d.rectangle((9,8,23,28),fill=black,outline=edge);d.rectangle((6,27,26,30),fill=edge)
   for x,y in [(8,5),(16,1),(24,5)]:d.polygon([(x,y),(x+2,y+4),(x+1,10),(x-2,8)],fill=ice)
   d.line((16,10,16,25),fill=violet,width=2);d.line([(11,13),(21,21),(16,25),(11,21),(21,13)],fill=ice,width=1)
  (A/'textures/item').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/item/{name}.png')
  if name!='karachun_scythe':write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
def audio():
 names=['ambient_low_wind','enter_trial','voice_whisper','scythe_windup','scythe_hit','dark_frost','long_night_cast','touch_hunger','touch_frost','touch_death','last_hour_mark','last_hour_impact','fear_phantom_spawn','fear_phantom_dispel','yield','raven_call','raven_flap','raven_dive','raven_hit','life_eater','last_breath','black_harvest','wing_activate','waning_stage_up'];sounds={};ff=shutil.which('ffmpeg');assert ff
 for index,name in enumerate(names):
  from frost_foley_60 import EVENTS
  if 'karachun.'+name in EVENTS:continue
  variants=3 if name in ['raven_call','raven_flap','ambient_low_wind','voice_whisper'] else 1;entries=[]
  for variant in range(variants):
   rnd=random.Random(1920+index*3+variant);rate=22050;duration=2.8 if name=='ambient_low_wind' else 1.7 if name in ['long_night_cast','yield','last_hour_mark'] else .65;count=int(rate*duration);smooth=0;samples=[]
   for i in range(count):
    t=i/rate;u=t/duration;noise=rnd.uniform(-1,1);smooth=smooth*.93+noise*.07;envelope=min(1,t*40)*max(0,1-u)**1.7;freq=82+index*3+variant*7
    low=math.sin(math.tau*freq*t)*.22+math.sin(math.tau*(freq*1.49)*t)*.08
    if name.startswith('raven'):signal=math.sin(math.tau*(280+80*math.sin(t*14))*t)*(.35 if name!='raven_flap' else .08)+smooth*.7;envelope*=.25+.75*max(0,math.sin(t*18))
    elif name in ['touch_frost','waning_stage_up','fear_phantom_dispel']:signal=math.sin(math.tau*(920+index*31)*t)*math.exp(-t*7)*.3+noise*.07
    elif 'scythe' in name or 'harvest' in name or name=='last_breath':signal=smooth*.8+low*math.exp(-t*6)+math.sin(math.tau*610*t)*.04
    else:signal=low+smooth*.55+noise*.02
    samples.append(int(max(-.8,min(.8,signal*envelope))*32767))
   tmp=O/f'{name}-{variant}.wav';dest=A/f'sounds/karachun/{name}_{variant}.ogg';dest.parent.mkdir(parents=True,exist_ok=True)
   with wave.open(str(tmp),'wb') as out:out.setnchannels(1);out.setsampwidth(2);out.setframerate(rate);out.writeframes(struct.pack('<'+'h'*len(samples),*samples))
   subprocess.run([ff,'-hide_banner','-loglevel','error','-y','-i',str(tmp),'-fflags','+bitexact','-flags:a','+bitexact','-c:a','libvorbis','-q:a','3','-map_metadata','-1',str(dest)],check=True);tmp.unlink();entries.append({'name':f'slavicmyths:karachun/{name}_{variant}','attenuation_distance':32})
  sounds['karachun.'+name]={'subtitle':'subtitle.slavicmyths.karachun.'+name,'sounds':entries}
 merge(A/'sounds.json',sounds)
 from frost_foley_60 import restore_selected
 restore_selected()
def resources():
 names={'karachun_scythe':('Коса Карачуна','Karachun’s Scythe'),'rune_of_waning':('Руна угасания','Rune of Waning'),'black_wing_amulet':('Чёрное крыло','Black Wing'),'shard_of_last_night':('Осколок последней ночи','Shard of the Last Night'),'karachun_trial_totem':('Тотем призыва Карачуна','Karachun Trial Totem')}
 ru={'boss':'Карачун','boss_phase':'Карачун · фаза %s','success':'Испытание пройдено — Карачун отступает','start':'Карачун услышал зов. Выдержи последнюю ночь!','overworld':'Испытание Карачуна доступно в Верхнем мире','active':'Испытание Карачуна уже идёт','space':'Нужно больше свободного места','received':'Получено: %s','queued':'Награда сохранена. Освободи места: %s','cancel':'Испытание прервано; незавершённый бой закрыт','trust':'Заслужи доверие Яги или угости Баюна','offering':'Подношение: 4 обсидиана и 1 синий лёд','offhand':'Угасание: второстепенная рука временно недоступна','totem_hint':'Многоразовый призыв по твёрдой поверхности. Репутация не нужна для готового тотема. Одно испытание одновременно.','npc_title':'Карачун','npc_offer':'Доверие II Яги или первое угощение Баюна. 4 обсидиана + 1 синий лёд; утраченный тотем восстанавливается.','scythe_hint':'ЛКМ — три тяжёлых удара по дуге; способности: Последний вздох и Чёрная жатва.','scythe_passives':'Жатва душ: каждый третий hit по цели — Иссушение I. Пожиратель жизни: до 2 HP / 5 с. Нежить: +25% урона, +3 HP за убийство.','waning':'Угасание %s · %s','fear':'Страх %s','cooldowns':'Вздох %s с · Жатва %s с · Крыло %s с','bindings':'%s — Последний вздох; %s — Чёрная жатва; %s — Чёрное крыло','reduced':'Сниженные искажения: %s','compatible.waning':'Вставляется в оружие; 20% на +25 Угасания, не чаще раза в секунду по цели.'}
 en={'boss':'Karachun','boss_phase':'Karachun · phase %s','success':'Trial endured — Karachun yields','start':'Karachun answers. Endure the last night!','overworld':'Karachun requires the Overworld','active':'A Karachun trial is already active','space':'More free space is needed','received':'Received: %s','queued':'Rewards queued. Free slots: %s','cancel':'The unfinished trial has been cancelled','trust':'Earn Yaga’s trust or feed Bayun','offering':'Offering: 4 obsidian and 1 blue ice','offhand':'Waning temporarily blocks your offhand','totem_hint':'Reusable surface summon; possession requires no reputation. One trial at a time.','npc_title':'Karachun','npc_offer':'Yaga trust II or Bayun’s first gift. 4 obsidian + 1 blue ice; lost totems can be restored.','scythe_hint':'Attack: three heavy arc swings; Last Breath and Black Harvest abilities.','scythe_passives':'Every third target hit: Wither I. Life Eater: up to 2 HP / 5s. Undead: +25% damage, 3 HP per kill.','waning':'Waning %s · %s','fear':'Fear %s','cooldowns':'Breath %ss · Harvest %ss · Wing %ss','bindings':'%s — Last Breath; %s — Black Harvest; %s — Black Wing','reduced':'Reduced distortions: %s','compatible.waning':'Weapon socket: 20% for +25 Waning, once per target per second.'}
 descriptions={'rune_of_waning':('20% шанс на +25 Угасания при настоящем попадании. Замедление → головокружение → блокировка второй руки; тотем бессмертия работает.','20% chance for +25 Waning on real hits. Slow → dizziness → offhand block; undying totems remain safe.'),'black_wing_amulet':('В слоте ожерелья: два союзных ворона на 20 с, ожидание 150 с. Награда только за первый успех; утраченный амулет не дублируется.','Necklace slot: two allied ravens for 20s, 150s cooldown. First clear only; lost amulets are not duplicated.'),'shard_of_last_night':('Гарантированная награда каждого испытания. Компонент будущего оберега; рецепта пока нет.','Guaranteed each trial. A future ward component; no recipe yet.')}
 attacks=['Погребальный взмах','Обратная жатва','Последний удар','Прикосновение голода','Прикосновение мороза','Прикосновение смерти','Долгая ночь','Первобытный страх','Последний час'];english=['Funeral Sweep','Reverse Reap','Final Stroke','Touch of Hunger','Touch of Frost','Touch of Death','Long Night','Primal Fear','Last Hour']
 for locale,local,idx in [('ru_ru',ru,0),('en_us',en,1)]:
  out={'night192.'+k:v for k,v in local.items()};out.update({'item.slavicmyths.'+n:v[idx] for n,v in names.items()});out.update({'item.slavicmyths.'+n+'.desc':v[idx] for n,v in descriptions.items()});out.update({'entity.slavicmyths.karachun':'Карачун' if idx==0 else 'Karachun','entity.slavicmyths.karachun_raven':'Ворон Карачуна' if idx==0 else 'Karachun Raven','entity.slavicmyths.karachun_blade':'Призрачное лезвие' if idx==0 else 'Spectral Blade','effect.slavicmyths.waning':'Угасание' if idx==0 else 'Waning','effect.slavicmyths.fear':'Страх' if idx==0 else 'Fear','bayun.dialogue.karachun':local['npc_title']})
  for key,value in [('breath','Последний вздох' if idx==0 else 'Last Breath'),('harvest','Чёрная жатва' if idx==0 else 'Black Harvest'),('wing','Чёрное крыло' if idx==0 else 'Black Wing'),('reduced','Снизить искажения' if idx==0 else 'Reduce distortions')]:out['key.slavicmyths.night_'+key]=value
  out['key.categories.slavicmyths.night']='Карачун' if idx==0 else 'Karachun'
  for i,name in enumerate(attacks if idx==0 else english,1):out['night192.attack.'+str(i)]=name
  for name in NightSounds:out['subtitle.slavicmyths.karachun.'+name]=('Карачун: ' if idx==0 else 'Karachun: ')+(name.replace('_',' ') if idx else {'ambient_low_wind':'пустой зимний ветер','enter_trial':'зов испытания','voice_whisper':'хриплый шёпот','scythe_windup':'замах косы','scythe_hit':'удар косы','dark_frost':'тёмный иней','long_night_cast':'наступает долгая ночь','touch_hunger':'прикосновение голода','touch_frost':'прикосновение мороза','touch_death':'прикосновение смерти','last_hour_mark':'отсчёт последнего часа','last_hour_impact':'удар последнего часа','fear_phantom_spawn':'ложный шорох','fear_phantom_dispel':'видение рассыпается','raven_call':'карканье ворона','raven_dive':'ворон пикирует','raven_flap':'хлопки крыльев','raven_hit':'удар клюва','yield':'буря затихает','life_eater':'жизнь перетекает','last_breath':'последний вздох','black_harvest':'чёрная жатва','wing_activate':'зов чёрного крыла','waning_stage_up':'угасание усиливается'}[name])
  out['advancement.slavicmyths.endured_last_night']='Последняя ночь' if idx==0 else 'The Last Night';out['advancement.slavicmyths.endured_last_night.desc']=local['success'];out['tales.entry.karachun_trial.title']=local['boss']
  pages=['Карачун — неоднозначный образ зимы и смерти в фольклоре. Коса, вороны и это испытание — игровая интерпретация, а не установленный исторический набор атрибутов.','Яга или Баюн могут дать многоразовый тотем. Подношение: 4 обсидиана и синий лёд. Готовый тотем работает без проверки репутации.','Угасание замедляет, вызывает краткое головокружение и блокирует только вторую руку. Отойди от тёмного мороза; тотем бессмертия остаётся надёжным.','Страх показывает ложных мобов. Они исчезают от попытки удара и не наносят урон. Настоящие вороны предупреждают о пикировании криком. Долгая ночь локальна.','На 5% здоровья Карачун сдаётся. Каждому участнику — руна и осколок, впервые — Чёрное крыло; коса выпадает независимо. Последний час отмечает точку заранее: покинь круг.'] if idx==0 else ['Karachun is an ambiguous folklore image of winter and death. Scythe, ravens and this trial are game interpretations.','Yaga or Bayun supplies a reusable totem for 4 obsidian and blue ice. Possession requires no reputation.','Waning slows, briefly distorts vision and blocks only offhand use. Leave the dark frost; undying totems remain safe.','Fear creates harmless illusions. Real ravens warn before diving. Long Night is local.','At 5% Karachun yields. Each contributor receives a rune and shard; the first clear grants Black Wing. The scythe is an independent roll. Leave the marked Last Hour circle.']
  for i,page in enumerate(pages):out[f'tales.entry.karachun_trial.text.{i}']=page
  for stage in ['known','rumor','seen','discovered','studied','verified']:out['tales.entry.karachun_trial.'+stage]=' ' .join(pages if stage=='verified' else pages[:2])
  merge(A/f'lang/{locale}.json',out)
 write(R/'src/main/resources/data/curios/tags/item/necklace.json',add_tag(R/'src/main/resources/data/curios/tags/item/necklace.json','slavicmyths:black_wing_amulet'))
 for tag in ['rune_heavy','rune_capacity_3']:write(D/f'tags/item/{tag}.json',add_tag(D/f'tags/item/{tag}.json','slavicmyths:karachun_scythe'))
 for tag in ['weapon','sharp_weapon','sword','durability']:
  path=R/f'src/main/resources/data/minecraft/tags/item/enchantable/{tag}.json';write(path,add_tag(path,'slavicmyths:karachun_scythe'))
 write(D/'tales/karachun_trial.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tales.entry.karachun_trial.title','icon':'slavicmyths:karachun_trial_totem','initial':0,'subjects':['entity/slavicmyths:karachun'],'advancements':['slavicmyths:endured_last_night'],'links':[]})
 write(D/'advancement/endured_last_night.json',{'parent':'slavicmyths:root','display':{'icon':{'id':'slavicmyths:shard_of_last_night'},'title':{'translate':'advancement.slavicmyths.endured_last_night'},'description':{'translate':'advancement.slavicmyths.endured_last_night.desc'},'frame':'challenge','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}})
NightSounds=['ambient_low_wind','enter_trial','voice_whisper','scythe_windup','scythe_hit','dark_frost','long_night_cast','touch_hunger','touch_frost','touch_death','last_hour_mark','last_hour_impact','fear_phantom_spawn','fear_phantom_dispel','yield','raven_call','raven_flap','raven_dive','raven_hit','life_eater','last_breath','black_harvest','wing_activate','waning_stage_up']
def main():
 O.mkdir(parents=True,exist_ok=True);r=boss();emit(r,'karachun','KarachunMesh');held(r);bird=raven();emit(bird,'karachun_raven','NightRavenMesh',256);icons();audio();resources();__import__('weapon_patch_assets').main();print('KARACHUN_ASSETS',len(r.bones),len(r.boxes),len(bird.bones),len(bird.boxes))
if __name__=='__main__':main()
