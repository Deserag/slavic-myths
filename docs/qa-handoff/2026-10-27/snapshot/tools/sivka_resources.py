"""Editable native rig and original pixel atlases; deterministic and additive."""
from pathlib import Path
import json, math, csv, shutil
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]; A=ROOT/'src/main/resources/assets/slavicmyths'; D=ROOT/'src/main/resources/data/slavicmyths'; OUT=ROOT/'docs/sivka/source'
def write(p,o):
    p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def merge(p,o):
    old=json.loads(p.read_text(encoding='utf8')) if p.exists() else {};old.update(o);write(p,old)
bones=[];cubes=[]
def bone(n,parent,p):
    bones.append(dict(name=n,parent=parent,pivot=p));return n
def box(n,b,p,size,mat):cubes.append(dict(name=n,bone=b,origin=p,size=size,material=mat))
body=bone('body','root',[0,-25.92,0])
box('ribcage',body,[-7.7,-9.3,-10.5],[15.4,16.6,24.7],'coat')
box('back',body,[-6.8,-10.2,-8],[13.6,3,23],'coat')
box('belly',body,[-6.4,4.6,-7],[12.8,4,18],'shadow')
for side in [-1,1]:
    for i in range(4):box('rib_facet_'+str(side)+'_'+str(i),body,[side*6.8-1,-7+i*3,-9+i*.2],[2,4.2,21-i*1.4],'coat')
    box('shoulder_'+str(side),body,[side*6-2.1,-7.2,-13.4],[4.2,12.8,9],'coat')
    box('haunch_'+str(side),body,[side*5.4-2.4,-7.8,6.5],[4.8,13.8,9.1],'coat')
    for i in range(3):box('haunch_facet_'+str(side)+'_'+str(i),body,[side*7-1.4,-5.5+i*3,8+i*.3],[2.8,3.7,6.5],'coat')
neck=bone('neck',body,[0,-6.88,-9.76]);box('neck_lower',neck,[-5,-9,-4.8],[10,15,10],'coat');box('neck_front',neck,[-4.6,-9.5,-6.6],[9.2,15.4,3],'coat')
upper=bone('neckUpper',neck,[0,-3,-2.4]);box('neck_upper',upper,[-4.2,-7.5,-3.9],[8.4,10.6,8.7],'coat')
for i in range(3):box('throat_'+str(i),upper,[-3.6,-6+i*3,-5.4-i*.2],[7.2,3.7,3.5],'coat')
head=bone('head',upper,[0,-1,-4.4]);box('skull',head,[-4.1,-6,-4],[8.2,9,8],'coat');box('forehead',head,[-3.4,-5.5,-5.5],[6.8,7.2,3],'coat');box('muzzle_bridge',head,[-3.4,-1.5,-9.6],[6.8,5.5,7],'coat');box('muzzle',head,[-3.5,1.4,-11.1],[7,3.4,3.2],'muzzle')
jaw=bone('jaw',head,[0,3,-4]);box('jaw',jaw,[-3.2,0,-6],[6.4,2.5,7.8],'shadow')
for side in [-1,1]:
    box('cheek_'+str(side),head,[side*3-1.5,-1.5,-3.6],[3,5.5,5],'coat');box('nostril_'+str(side),head,[side*2.8-.4,1.9,-11.3],[.8,1.3,.5],'dark')
    ear=bone('ear_'+str(side),head,[side*2.9,-5.6,1]);box('ear_'+str(side),ear,[-.95,-5.2,-1],[1.9,5.3,2.1],'coat');box('ear_inner_'+str(side),ear,[-.55,-4.5,-1.1],[1.1,3.4,.3],'shadow')
    eye=bone('eye_'+str(side),head,[side*4.05,-2.9,-2.7]);box('eye_dark_'+str(side),eye,[-.25,-.65,-1.3],[.5,1.6,2.4],'dark');box('eye_amber_'+str(side),eye,[-.32,-.38,-.8],[.64,.8,.9],'eye');box('eye_glint_'+str(side),eye,[-.35,-.45,-.95],[.7,.3,.35],'glow')
for idx,(x,z,py,rear) in enumerate([(-6.1,-8.6,-1.6,False),(6.1,-8.6,-1.6,False),(-6.1,12.5,-.64,True),(6.1,12.5,-.64,True)]):
    leg=bone('leg'+str(idx),body,[x,py,z]);box('upper_'+str(idx),leg,[-2.4,-1,-3.1],[4.8,12.9,6.2],'coat');box('outer_muscle_'+str(idx),leg,[-2.6,1,-2.8],[5.2,7.5,5.6],'coat')
    knee=bone('knee'+str(idx),leg,[0,12.4,1.8 if rear else -.8]);box('joint_'+str(idx),knee,[-2.5,-1.2,-2.8],[5,4,5.5],'coat');box('cannon_'+str(idx),knee,[-1.5,1,-1.8],[3,10.7,3.8],'coat');box('cannon_front_'+str(idx),knee,[-1.25,2,-2.3],[2.5,9,1],'coat')
    hoof=bone('hoof'+str(idx),knee,[0,10.6,-.6]);box('fetlock_'+str(idx),hoof,[-2.1,-1,-2.5],[4.2,2.4,4.8],'coat');box('hoof_'+str(idx),hoof,[-2.6,1,-3.3],[5.2,2.6,6.2],'hoof');box('hoof_rim_'+str(idx),hoof,[-2.65,.6,-3.35],[5.3,.65,6.3],'horn')
    for j in range(3):box('feather_'+str(idx)+'_'+str(j),hoof,[-2.5+j*1.4,-2,-2.4],[1.6,3,1.5],'coat')
# Twelve chunky, overlapping locks, rooted on articulated neck rather than a flat sheet.
for i in range(12):
    side=-1 if i%2==0 else 1;j=i//2;lock=bone('mane'+str(i),upper,[side*(4.8+j*.32),-7+j*1.9,4.1+j*.32])
    box('mane_root_'+str(i),lock,[-1.4,-1,-1],[2.8,5.6+j*.45,4.3],'gold');box('mane_edge_'+str(i),lock,[-1.65,1.2,1],[3.3,3.3,3.8],'gold')
    tip=bone('maneTip'+str(i),lock,[side*.4,4.6+j*.4,2.3]);box('mane_tip_'+str(i),tip,[-1.15,-.6,-.8],[2.3,4.3+j*.25,3.2],'copper');box('mane_glint_'+str(i),tip,[-.8,.2,-.95],[1.3,2.2,.3],'gold')
fore=bone('forelock',head,[0,-6.5,-2]);
for i in range(3):box('fringe_'+str(i),fore,[-3.3+i*2.2,-.4,-2.6],[2.5,4+i*.4,2.5],'gold')
tail=bone('tail',body,[0,-8,15]);box('tail_root',tail,[-3,-1,-1],[6,5,6],'gold');mid=bone('tailMid',tail,[0,3,4]);box('tail_mid',mid,[-3.5,-1,-1],[7,8,6],'gold')
for i in range(3):
    tip=bone('tailTip'+str(i),mid,[(i-1)*2.5,5.5,3]);box('tail_tuft_'+str(i),tip,[-1.8,-1,-1.2],[3.6,10.5+i,4.7],'gold');box('tail_inner_'+str(i),tip,[-1.5,4,1],[3,9,4],'copper');box('tail_end_'+str(i),tip,[-1.1,11,2],[2.2,4.5,3],'gold')
palette={'coat':['#F4E9D8','#FFF6EA','#EADCCA','#E2D1BB'],'shadow':['#D8C5AE','#E0CCB4','#C7B299','#E9D9C6'],'gold':['#F9BF45','#FFD46B','#DF8B2A','#ECA338'],'copper':['#DF8B2A','#AF591C','#EEA339','#F7BC48'],'hoof':['#493B34','#635044','#3E332D','#79634F'],'horn':['#9E8056','#C6A46A','#866643','#DFC082'],'muzzle':['#8B7A68','#A0917E','#756659','#B9A794'],'dark':['#342B25','#493B34','#28231E','#635044'],'eye':['#E9AD43']*4,'glow':['#FFE18D']*4}
atlas=Image.new('RGBA',(256,256));overlay=Image.new('RGBA',(256,256));draw=ImageDraw.Draw(atlas);ed=ImageDraw.Draw(overlay);uv=Image.new('RGBA',(256,256),(25,27,30,255));ud=ImageDraw.Draw(uv)
# Material shelves share UV only within a material family; coherent low-resolution voxel pixels.
regions={'coat':(0,0,120,88),'shadow':(0,90,120,42),'gold':(122,0,132,94),'copper':(122,96,132,62),'hoof':(0,134,120,35),'horn':(0,171,120,25),'muzzle':(122,160,132,28),'dark':(122,190,132,24),'eye':(0,198,80,24),'glow':(0,224,110,30)}
for mat,(x,y,w,h) in regions.items():
    colors=palette[mat]
    for yy in range(y,y+h):
        for xx in range(x,x+w):draw.point((xx,yy),fill=colors[(xx//5+yy//7+(xx//17))%len(colors)])
    ud.rectangle((x,y,x+w-1,y+h-1),outline='#ffffff');ud.text((x+2,y+2),mat,fill='#ffffff')
    if mat=='glow':ed.rectangle((x,y,x+w-1,y+h-1),fill='#FFE18D')
lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','/** Generated from docs/sivka/source/native-rig.json; editable pivots, never vanilla horse geometry. */','public final class SivkaMesh {','public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();']
# Adjacent overlapping facets used to share exactly the same plane. Colour their
# conflicts and offset only those surfaces by a small, reproducible shell gap.
origins={'root':[0,0,0]}
for b in bones:origins[b['name']]=[a+v for a,v in zip(origins[b['parent']],b['pivot'])]
bounds=[]
for c in cubes:
 lo=[a+v for a,v in zip(origins[c['bone']],c['origin'])];hi=[a+v for a,v in zip(lo,c['size'])]
 color=0
 while True:
  shell=color*.0413;a=[v-shell for v in lo];b=[v+shell for v in hi];conflict=False
  for oldlo,oldhi in bounds:
   for axis in range(3):
    if any(abs(v-w)<.0001 for v in [a[axis],b[axis]] for w in [oldlo[axis],oldhi[axis]]) and all(min(b[k],oldhi[k])-max(a[k],oldlo[k])>.001 for k in range(3) if k!=axis):conflict=True;break
   if conflict:break
  if not conflict:break
  color+=1
  assert color<20,c
 bounds.append((a,b));c['shell']=shell

def f(v):return f'{v:.5f}F'
for b in bones:
    cs=[c for c in cubes if c['bone']==b['name']];builder='CubeListBuilder.create()'
    for c in cs:
        x,y,w,h=regions[c['material']];a,bb,cc=c['size'];needw=math.ceil(2*(a+cc));needh=math.ceil(bb+cc)
        assert needw<=w and needh<=h,(c,needw,needh)
        c['uv']=[x+1,y+1];builder+=f'.texOffs({x+1},{y+1}).addBox('+','.join(f(v) for v in c['origin']+c['size'])+',new CubeDeformation('+f(c['shell'])+'))'
    lines.append('var '+b['name'].replace('-','neg')+'='+b['parent'].replace('-','neg')+'.addOrReplaceChild("'+b['name']+'",'+builder+',PartPose.offset('+','.join(f(v) for v in b['pivot'])+'));')
lines+=['return LayerDefinition.create(mesh,256,256).bakeRoot();}','public static java.util.Map<String,ModelPart> index(ModelPart root){var map=new java.util.HashMap<String,ModelPart>();map.put("root",root);']
for b in bones:lines.append('map.put("'+b['name']+'",map.get("'+b['parent']+'").getChild("'+b['name']+'"));')
lines+=['return map;}','private SivkaMesh(){}','}']
p=ROOT/'src/main/java/org/slavicmyths/client/SivkaMesh.java';p.write_text('\n'.join(lines)+'\n',encoding='utf8')
tex=A/'textures/entity';tex.mkdir(parents=True,exist_ok=True);atlas.save(tex/'sivka_burka.png');overlay.save(tex/'sivka_burka_glow.png');OUT.mkdir(parents=True,exist_ok=True);uv.save(OUT/'uv-regions.png');write(OUT/'native-rig.json',{'units_per_block':16,'axis':'Y down, front -Z, root feet y24','bones':bones,'cubes':cubes})
icon=Image.new('RGBA',(64,64));di=ImageDraw.Draw(icon)
di.polygon([(11,49),(11,40),(20,35),(25,27),(24,11),(30,5),(34,18),(41,11),(44,25),(54,34),(51,42),(38,43),(32,53),(22,57)],fill='#513420');di.polygon([(14,47),(14,42),(23,37),(29,28),(28,12),(31,10),(33,23),(40,18),(41,28),(49,35),(48,39),(36,39),(28,51),(22,53)],fill='#BF8545');di.line([(17,44),(26,39),(33,28),(33,19)],fill='#E6B66C',width=3);di.rectangle((37,28,40,31),fill='#30291F');di.line([(25,44),(29,41),(32,44),(35,41)],fill='#664626',width=2);di.ellipse((7,48,20,61),outline='#C7C8B3',width=3);di.rectangle((45,35,49,37),fill='#38291E');icon.save(A/'textures/item/sivka_whistle.png')
write(A/'models/item/sivka_whistle.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/sivka_whistle'}});write(A/'models/item/sivka_burka_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
write(D/'loot_table/entities/sivka_burka.json',{'type':'minecraft:entity','pools':[]})
write(D/'tags/worldgen/biome/sivka_grasslands.json',{'replace':False,'values':['minecraft:plains','minecraft:sunflower_plains','minecraft:meadow']})
write(D/'neoforge/biome_modifier/sivka_burka.json',{'type':'neoforge:add_spawns','biomes':'#slavicmyths:sivka_grasslands','spawners':[{'type':'slavicmyths:sivka_burka','weight':1,'minCount':1,'maxCount':1}]})
spec=ROOT/'work/sivka-spec/Sivka_Burka_1.6_Codex_TZ';rows=list(csv.DictReader((spec/'02_animation/04_FRAME_TIMELINES.csv').read_text().splitlines()));write(A/'animations/sivka_burka.json',{'source':'1.6 frame timeline','clips':rows});shutil.copyfile(spec/'02_animation/04_FRAME_TIMELINES.csv',OUT/'keyframes.csv')
pose=['package org.slavicmyths.sivka;','/** Eased cyclic keyframes generated from the supplied 1.6 CSV. */','public final class SivkaPoses {']
for clip,name in [('land_gallop','GROUND'),('air_gallop','AIR')]:
    rr=[r for r in rows if r['clip']==clip];cols=['normalized_phase','torso_pitch_deg','render_bob_blocks','foreleg_left_deg','foreleg_right_deg','hindleg_left_deg','hindleg_right_deg','fore_knee_deg','hock_deg'];pose.append('public static final float[][] '+name+'={'+','.join('{'+','.join(f(float(r[c])) for c in cols)+'}' for r in rr)+'};')
pose+=['public static float sample(boolean air,float u,int col){var rows=air?AIR:GROUND;u=u-(float)Math.floor(u);for(int i=1;i<rows.length;i++)if(u<=rows[i][0]){float t=(u-rows[i-1][0])/(rows[i][0]-rows[i-1][0]);t=t*t*(3-2*t);return rows[i-1][col]+(rows[i][col]-rows[i-1][col])*t;}return rows[0][col];}','private SivkaPoses(){}','}'];(ROOT/'src/main/java/org/slavicmyths/sivka/SivkaPoses.java').write_text('\n'.join(pose)+'\n',encoding='utf8')
ru={'entity.slavicmyths.sivka_burka':'Сивка-Бурка','item.slavicmyths.sivka_whistle':'Молодецкий свист','item.slavicmyths.sivka_burka_spawn_egg':'Яйцо призыва Сивки-Бурки','key.slavicmyths.sivka_descend':'Сивка-Бурка: снижение'}
en={'entity.slavicmyths.sivka_burka':'Sivka-Burka','item.slavicmyths.sivka_whistle':'Heroic Whistle','item.slavicmyths.sivka_burka_spawn_egg':'Sivka-Burka Spawn Egg','key.slavicmyths.sivka_descend':'Sivka-Burka: descend'}
texts={'temporary':('Временная связь с Сивкой-Буркой','Temporary bond with Sivka-Burka'),'permanent':('Сивка-Бурка откликнется на зов','Sivka-Burka will answer your call'),'progress':('Доверие: %s/7','Trust: %s/7'),'next':('Сивка ждёт: %s','Sivka awaits: %s'),'timer':('Связь сохранится ещё: %s','Bond remains for: %s'),'wrong_food':('Сивка ждёт другого угощения: %s','Sivka awaits another treat: %s'),'accepted':('Сивка приняла угощение. Доверие: %s/7','Sivka accepted the treat. Trust: %s/7'),'complete':('Сивка-Бурка признаёт тебя хозяином','Sivka-Burka accepts you as her owner'),'expired':('Связь ослабла. Найдите Сивку и дайте яблоко; доверие сохранено','The bond faded. Find Sivka and offer an apple; trust is preserved'),'nearby':('Сивка уже рядом','Sivka is already nearby'),'no_room':('Здесь для Сивки-Бурки недостаточно места','There is not enough safe space for Sivka-Burka'),'not_ready':('Для взлёта нажмите Ctrl+W+Space на земле','Press Ctrl+W+Space on the ground to take off'),'obstacle':('Над Сивкой недостаточно свободного пространства','There is not enough clearance above Sivka'),'cooldown':('Сивка ещё не может откликнуться','Sivka cannot answer yet'),'other_owner':('Этот свист или конь предназначен другому хозяину','This whistle or horse belongs to another owner'),'existing_bond':('Сначала завершите текущую связь с Сивкой','Complete your current Sivka bond first'),'free_horse':('Сивка свободна: ей не нужны седло и поводок','Sivka is free: no saddle or lead'),'reissued':('Выдан новый свист; прежний больше не действует','A new whistle was issued; the old token is revoked'),'controls':('W/S — ход, A/D — вбок; Space — прыжок; Ctrl+W+Space — взлёт; в полёте Space вверх, X вниз, отпустите W для торможения; Shift — спешиться','W/S move, A/D strafe; Space jump; Ctrl+W+Space take off; in flight Space up, X down, release W to brake; Shift dismount')}
for k,(r,e) in texts.items():ru['sivka.slavicmyths.'+k]=r;en['sivka.slavicmyths.'+k]=e
soundrows=list(csv.DictReader((spec/'04_audio_fx/03_AUDIO_CUES_CSV.csv').read_text(encoding='utf8').splitlines()));sounds={}
for row in soundrows:
    event=row['event'];variants=int(row['required_variants']);sounds['sivka.'+event]={'subtitle':'subtitles.slavicmyths.sivka.'+event,'sounds':[{'name':'slavicmyths:entity/sivka/'+event+'_'+str(i+1),'attenuation_distance':28 if 'whinny' in event or event=='summon_response' else 16} for i in range(variants)]};ru['subtitles.slavicmyths.sivka.'+event]=row['subtitles_ru'];en['subtitles.slavicmyths.sivka.'+event]='Sivka-Burka: '+event.replace('_',' ')
merge(A/'lang/ru_ru.json',ru);merge(A/'lang/en_us.json',en);merge(A/'sounds.json',sounds)
print('Sivka assets:',len(bones),'bones,',len(cubes),'cuboids,',len(sounds),'sound events')
