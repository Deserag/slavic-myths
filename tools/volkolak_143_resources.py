"""Authored Variant B anatomy and original clustered pixel resources. Keeps legacy Volkolak assets."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random,math
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths';D=ROOT/'src/main/resources/data/slavicmyths'
P=['#242326','#343237','#464349','#5B565A','#685A50','#858077','#BDB6A7','#2B292C','#C1B9A6','#805447','#CE963B','#F3C163','#171518','#1D1B1D','#DDD5C0']
def write(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(p,im):p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
parts=[]
def part(name,parent,pos,boxes=(),rotation=(0,0,0),material='fur'):
 parts.append(dict(name=name,parent=parent,pos=pos,rotation=rotation,boxes=[dict(shape=list(b),material=material)for b in boxes]))
part('root',None,[0,24,0]);part('pelvis','root',[0,-14,11],[[-7.5,-6,-6.5,15,12,13]])
part('spine','pelvis',[0,-2,-5],[[-7,-6,-10,14,12,17]],(.17,0,0))
part('chest','spine',[0,-3,-8],[[-11,-9,-9,22,17,15]])
part('neck','chest',[0,-4,-3],[[-6.5,-5,-11,13,11,12]])
part('head','neck',[0,4,-5],[[-6.5,-6,-7,13,12,13]])
part('muzzle','head',[0,2,-6],[[-4,-2,-12,8,6,12]])
part('nose','muzzle',[0,0,-12],[[-3,-1,-.7,6,3,1]],material='nose')
part('jaw','head',[0,5,-6],[[-4,0,-11,8,3,11]],material='jaw')
part('earL','head',[-5,-6,0],[[-1.5,-7,-1.5,3,7,3]],(0,0,-.14))
part('earR','head',[5,-6,0],[[-1.5,-6,-1.5,3,6,3]],(.08,0,.22))
for side,x in [('L',-1),('R',1)]:
 part('brow'+side,'head',[x*5,-2,-6.9],[[-1.5,-1,-.7,3,2,2]])
 part('eye'+side,'head',[x*4,-1,-7.3],[[-.9,-.6,-.2,1.8,1.2,.4]],material='eye')
 part('upper'+side,'chest',[x*11,-6,-3],[[-4,0,-4,8,9,8],[-3.5,8,-3.5,7,8,7]],(.82,0,x*.2))
 part('fore'+side,'upper'+side,[0,16,0],[[-3.5,0,-3.5,7,14,7]],(-1.65,0,0))
 part('hand'+side,'fore'+side,[0,14,0],[[-4.5,-1,-5,9,4,8]],(.83,0,0))
 for i in range(4):
  part(f'claw{side}{i}','hand'+side,[-2.5+i*1.7,-1.5,-4],[[-.5,0,-4,1,1.5,4]],(.12,0,0),material='claw')
  part(f'hook{side}{i}',f'claw{side}{i}',[0,0,-4],[[-.5,0,-1.5,1,2,1.5]],(.35,0,0),material='clawtip')
 part('thigh'+side,'pelvis',[x*5,1,2],[[-3.5,0,-4,7,13,8]],(1.35,0,x*.08))
 part('shin'+side,'thigh'+side,[0,13,0],[[-2.5,0,-2.5,5,15,5]],(-2.4,0,0))
 part('foot'+side,'shin'+side,[0,15,0],[[-3.5,-1,-7,7,4,10]],(1.05,0,0))
 for i in range(3):part(f'toe{side}{i}','foot'+side,[-2+i*2,1,-6],[[-.5,0,-2,1,1,2]],material='clawtip')
part('tail1','pelvis',[0,0,6],[[-3.5,-3,0,7,8,7]],(.35,0,0))
part('tail2','tail1',[0,3,3],[[-4,-3,0,8,10,9]],(.25,0,0))
part('tail3','tail2',[0,4,3],[[-3.5,-3,0,7,9,8]],(.1,0,.08))
for name,parent,pos,box in [
 ('furShoulderL','chest',[-9,-7,2],[-2,-2,-4,4,7,8]),('furShoulderR','chest',[9,-7,2],[-2,-2,-4,4,7,8]),
 ('furNape','neck',[0,-5,0],[-7,-2,-4,14,4,10]),('furRuffL','neck',[-6,0,-5],[-2,-4,-4,4,9,8]),('furRuffR','neck',[6,0,-5],[-2,-4,-4,4,9,8]),
 ('furBack','spine',[0,-6,0],[-6,-2,-3,12,4,9]),('furTail','tail3',[0,5,6],[-3,-2,-1,6,5,4]),('furJaw','jaw',[0,2,-2],[-4,-1,-3,8,4,5])]:part(name,parent,pos,[box])
part('chestV','chest',[0,0,-9.15],[[-7,-6,-.3,14,12,.6]],material='chestV')
part('sternumFur','chest',[0,8,0],[[-7,0,-8,14,2.2,12]],material='chestV')
part('sternumTip','chest',[0,8.5,3],[[-5,0,0,10,2,4]],(.08,0,0),material='chestV')
part('throatUnder','neck',[0,5,-5],[[-5,0,-6,10,2.5,11]],material='chestV')
part('throatFur','neck',[0,3,-10],[[-5,-3,-1,10,8,2]],material='chestV')
part('muzzleLight','muzzle',[0,2,-5],[[-4.1,0,-6.5,8.2,2.2,12]],material='jawLight')
# Connected, broad ruff tiers follow the shoulder/nape surface instead of detached spikes.
for side,x in [('L',-1),('R',1)]:
 part('shoulderRuff'+side,'chest',[x*8,-5,1],[[-3,-3,-5,6,10,11]],(0,0,x*.12))
 part('foreFur'+side,'fore'+side,[0,2,0],[[-3,-1,-3,6,7,6]])
 part('handKnuckle'+side,'hand'+side,[0,-1,0],[[-4,-1,-4,8,3,7]])
 part('cheekRuff'+side,'head',[x*5,2,1],[[-2,-3,-4,4,8,6]],(0,0,x*.15))
part('napeRidge','neck',[0,-5,1],[[-6,-3,-3,12,5,9]])
# Cream ruff wraps the neck volume and remains visible when the spine rises.
# Tier tips overlap their roots, so the silhouette stays connected in both stances.
for tier in range(4):
 width=12-tier*1.7
 part('bibTier'+str(tier),'neck',[0,3+tier*2,-8],
      [[-width/2,-1,-3,width,4,4]],(.12,0,0),material='chestV')
for side,x in [('L',-1),('R',1)]:
 part('lightCheek'+side,'head',[x*4,4,-4],[[-2,-1,-4,4,4,6]],(0,0,x*.16),material='jawLight')
 for tier in range(3):
  part('ruffTip'+side+str(tier),'neck',[x*(5-tier*.5),tier*2,-3],
       [[-1.7,-1,-2,3.4,4,5]],(.18,0,x*.16),material='chestV')
for side,x in [('L',-1),('R',1)]:
 part('fangUpper'+side,'muzzle',[x*2.7,3.5,-9],[[-.6,0,-.6,1.2,3.5,1.2]],material='tooth')
 part('fangLower'+side,'jaw',[x*2.6,0,-7],[[-.5,-2.5,-.5,1,2.5,1]],material='tooth')
 part('toothSide'+side,'muzzle',[x*3.7,3.3,-4],[[-.5,0,-.5,1,2,1]],material='tooth')
part('scarArm','upperL',[-3.1,4,-2],[[-.25,0,-.5,.5,6,1]],material='scar')
part('scarRibs','chest',[10.1,0,1],[[-.25,-3,-.5,.5,6,1]],material='scar')

atlas=Image.new('RGBA',(512,512));draw=ImageDraw.Draw(atlas);rng=random.Random(143);cx=cy=row=0;rectangles=[]
for p in parts:
 for b in p['boxes']:
  x,y,z,w,h,d=b['shape'];iw,ih,id=math.ceil(w),math.ceil(h),math.ceil(d);rw=2*(iw+id)+2;rh=ih+id+2
  if cx+rw>512:cx=0;cy+=row;row=0
  if cy+rh>512:raise RuntimeError('Authored UV atlas overflow')
  b['uv']=[cx+1,cy+1];rectangles.append([cx,cy,rw,rh]);u,v=b['uv'];material=b['material']
  palette={'nose':[13,0,1],'eye':[10,11,12],'claw':[7,1,4],'clawtip':[8,5,7],'tooth':[14,8,6],'scar':[9,4,1],'chestV':[6,5,6],'jawLight':[6,5,8],'jaw':[1,2,5]}.get(material,[2,2,2,1,3,4])
  for py in range(cy+1,cy+rh-1):
   for px in range(cx+1,cx+rw-1):
    index=palette[(px//3*7+py//4*11)%len(palette)]
    if material=='eye':index=12 if px%4==1 else 11 if py%3==0 else 10
    draw.point((px,py),fill=P[index])
  if material=='chestV':
   draw.polygon([(u+id,v+id),(u+id+iw/2,v+id+ih),(u+id+iw,v+id)],fill=P[6])
  cx+=rw;row=max(row,rh)
save(A/'textures/entity/cursed_volkolak.png',atlas)
eyes=Image.new('RGBA',(512,512));ed=ImageDraw.Draw(eyes)
for p in parts:
 for b in p['boxes']:
  if b['material']=='eye':
   u,v=b['uv'];w,h,d=b['shape'][3:];ed.rectangle((u,v,u+2*(math.ceil(w)+math.ceil(d))-1,v+math.ceil(h)+math.ceil(d)-1),fill='#795423')
save(A/'textures/entity/cursed_volkolak_eyes.png',eyes)
write(A/'cursed_volkolak_geometry.json',{'variant':'B','atlas':[512,512],'unit':'1/16 block','parts':parts,'uv_rectangles':rectangles})
lines=['package org.slavicmyths.client;','/** Generated by tools/volkolak_143_resources.py from authored anatomy. */','final class CursedVolkolakGeometry {','static void build(CursedVolkolakModel m){']
def arr(values):return 'new float[]{'+','.join(f'{float(x):g}F'for x in values)+'}'
for p in parts:
 parent='null'if p['parent']is None else '"'+p['parent']+'"'
 lines.append(f'm.part("{p["name"]}",{parent},{arr(p["pos"])},{arr(p["rotation"])});')
 for b in p['boxes']:lines.append(f'm.box("{p["name"]}",{b["uv"][0]},{b["uv"][1]},{arr(b["shape"])});')
lines+=['}','private CursedVolkolakGeometry(){}','}'];(ROOT/'src/main/java/org/slavicmyths/client/CursedVolkolakGeometry.java').write_text('\n'.join(lines)+'\n')

def sprite(kind):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if kind=='egg':
  d.polygon([(13,3),(19,3),(22,6),(24,12),(25,22),(22,28),(10,28),(7,23),(8,13),(10,7)],fill=P[0]);d.polygon([(13,4),(19,4),(22,9),(23,18),(23,24),(20,27),(11,27),(9,23),(10,12)],fill=P[2]);d.line([(11,8),(10,15),(10,21)],fill=P[3],width=2)
  for box in [(16,7,19,10),(11,15,14,18),(19,18,22,21),(14,24,17,26)]:d.rectangle(box,fill=P[4])
  d.point((17,13),fill=P[10]);d.point((20,23),fill=P[10])
 elif kind=='skull':
  d.polygon([(10,6),(21,6),(25,11),(25,21),(21,24),(20,27),(12,27),(11,24),(7,22),(6,12)],fill='#5C6062');d.polygon([(10,7),(20,7),(24,12),(23,21),(20,24),(12,24),(8,20),(8,12)],fill='#D6D0BE');d.line([(10,8),(19,8),(22,10)],fill='#EBE5CF',width=2);d.rectangle((10,15,14,18),fill=P[0]);d.rectangle((18,14,22,18),fill=P[0]);d.polygon([(16,19),(17,22),(14,22)],fill=P[1]);d.rectangle((12,24,20,27),fill='#ADA9A0');d.line([(14,24),(14,26)],fill='#484B50');d.line([(18,24),(18,27)],fill='#484B50');d.point((12,23),fill='#7C9195');d.rectangle((19,22,20,23),fill=P[0])
 elif kind=='blood':
  d.polygon([(16,4),(13,9),(9,16),(8,23),(11,27),(21,27),(24,23),(23,17),(19,10)],fill='#241A25');d.polygon([(16,7),(12,14),(10,20),(11,24),(14,26),(20,25),(22,22),(20,15)],fill='#642C3D');d.line([(15,12),(14,18),(17,22),(16,25)],fill='#2D1D2B');d.line([(14,18),(11,17)],fill='#2D1D2B');d.line([(17,22),(21,21)],fill='#2D1D2B');d.line([(12,19),(12,22)],fill='#874356')
 elif kind=='plant':
  for x in [8,13,19,24]:d.line([(16,29),(x,8)],fill='#56615B');d.line([(16,28),(x+2,13)],fill='#71756A');d.polygon([(x,14),(x-3,18),(x,20)],fill='#77736D')
  for x,y in [(8,8),(19,7)]:d.polygon([(x,y-3),(x+3,y-1),(x+3,y+2),(x,y+4),(x-3,y+1),(x-2,y-2)],fill='#BBBDAF');d.rectangle((x,y,x+1,y+1),fill='#D8D8C2')
  d.line([(14,26),(21,24)],fill='#64574D',width=2)
 elif kind=='quest':
  d.polygon([(7,3),(11,7),(21,7),(25,3),(26,17),(23,24),(17,28),(10,24),(6,17)],fill=P[0]);d.polygon([(9,9),(21,9),(24,15),(21,21),(17,25),(11,21),(8,16)],fill=P[2]);d.rectangle((10,13,13,15),fill=P[10]);d.polygon([(18,17),(24,11),(23,19),(18,25)],fill=P[6]);d.line([(19,18),(22,13)],fill=P[0]);d.line([(11,22),(13,26)],fill=P[4]);d.line([(14,21),(16,26)],fill=P[4])
 return im
for name,kind in [('cursed_volkolak_spawn_egg','egg'),('cursed_volkolak_skull','skull')]:save(A/f'textures/item/{name}.png',sprite(kind));write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
save(A/'textures/mob_effect/bad_blood.png',sprite('blood'));save(A/'textures/block/plach_trava.png',sprite('plant'));save(A/'textures/gui/grey_wolf/quest_3.png',sprite('quest'))
for step in range(5):
 icon=Image.new('RGBA',(32,32));d=ImageDraw.Draw(icon)
 if step==0:
  d.polygon([(8,22),(12,14),(20,14),(24,22),(22,28),(10,28)],fill=P[4]);d.rectangle((6,8,10,13),fill=P[5]);d.rectangle((13,4,17,10),fill=P[5]);d.rectangle((21,7,25,13),fill=P[5]);d.line([(10,24),(20,24)],fill=P[6])
 elif step==1:
  for x in [7,14,21]:d.polygon([(x,4),(x+4,4),(x+1,22),(x-3,28)],fill=P[8]);d.line([(x,6),(x-2,24)],fill=P[0])
 elif step==2:
  d.polygon([(9,4),(21,4),(25,9),(24,23),(27,28),(19,25),(15,29),(10,25),(5,28),(8,17)],fill='#565155');d.rectangle((10,11,13,14),fill=P[0]);d.rectangle((18,11,21,14),fill=P[10]);d.line([(15,18),(13,24)],fill='#764C45',width=2)
 elif step==3:
  d.polygon([(3,25),(9,21),(16,23),(22,19),(29,25),(28,29),(4,29)],fill=P[4]);d.line([(9,23),(23,6)],fill='#856343',width=3);d.polygon([(19,6),(23,3),(28,5),(28,10),(24,13),(20,11)],fill=P[8]);d.line([(21,8),(25,5)],fill=P[6])
 else:
  icon=sprite('skull');d=ImageDraw.Draw(icon);d.line([(17,27),(28,27)],fill='#91AA88',width=2);d.polygon([(25,23),(30,27),(25,31)],fill='#91AA88')
 save(A/f'textures/gui/grey_wolf/volkolak_objective_{step}.png',icon)
cache=Image.new('RGBA',(32,32));d=ImageDraw.Draw(cache)
for y in range(32):
 for x in range(32):d.point((x,y),fill=['#4B4236','#5E5141','#6B5B47','#796B55'][rng.randrange(4)])
d.line([(2,9),(11,12),(13,19),(23,22),(27,28)],fill='#302F2D',width=1);d.rectangle((21,8,24,11),fill='#ADA9A0');save(A/'textures/block/cursed_skull_cache.png',cache)
write(A/'blockstates/plach_trava.json',{'variants':{'':{'model':'slavicmyths:block/plach_trava'}}});write(A/'models/block/plach_trava.json',{'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':'slavicmyths:block/plach_trava'}});write(A/'models/item/plach_trava.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:block/plach_trava'}})
write(A/'blockstates/cursed_skull_cache.json',{'variants':{'':{'model':'slavicmyths:block/cursed_skull_cache'}}});write(A/'models/block/cursed_skull_cache.json',{'textures':{'all':'slavicmyths:block/cursed_skull_cache','particle':'slavicmyths:block/cursed_skull_cache'},'elements':[{'from':[.8,0,.8],'to':[15.2,12,15.2],'faces':{face:{'texture':'#all'}for face in ['down','up','north','south','west','east']}}]});write(A/'models/item/cursed_skull_cache.json',{'parent':'slavicmyths:block/cursed_skull_cache'})
write(D/'loot_table/blocks/plach_trava.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:plach_trava'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]});write(D/'loot_table/blocks/cursed_skull_cache.json',{'type':'minecraft:block','pools':[]});write(D/'loot_table/entities/cursed_volkolak.json',{'type':'minecraft:entity','pools':[]})
quests_path=D/'wolf_story/quests.json';quests=json.loads(quests_path.read_text(encoding='utf-8'));q=quests[3];q.update(dialogue_by_state={state:f'wolfstory.slavicmyths.dialog.3.{state.lower()}'for state in ['LOCKED','OFFERED','ACTIVE','REWARD_READY','COMPLETED']},objectives=[{'kind':'cursed_volkolak_and_bound_skull','required':1}],reward_visibility_policy='REVEAL_ON_ASK',reward_id='finist_bow_and_quiver',target_tracking_reference='volkolak_world_encounter');write(quests_path,quests)
print(f'Variant B: {len(parts)} joints, {sum(len(p["boxes"])for p in parts)} cuboids; original 512px atlas and 32px assets.')
text=json.loads((ROOT/'tools/volkolak_143_text.json').read_text(encoding='utf-8'))
subtitles={
 'howl_short':('Проклятый Оборотень коротко воет','Cursed Werewolf howls briefly'), 'ambient':('Проклятый Оборотень тяжело дышит','Cursed Werewolf breathes'), 'alert':('Проклятый Оборотень замечает добычу','Cursed Werewolf alerts'), 'snarl':('Проклятый Оборотень скалится','Cursed Werewolf snarls'), 'claw_windup':('Когти натягиваются','Claws tense'), 'claw_swing':('Когти рассекают воздух','Claws slash'), 'bite':('Проклятый Оборотень кусает','Cursed Werewolf bites'), 'pounce_grunt':('Проклятый Оборотень бросается','Cursed Werewolf pounces'), 'land':('Проклятый Оборотень приземляется','Cursed Werewolf lands'), 'stance_shift':('Проклятый Оборотень встаёт','Cursed Werewolf rises'), 'grab_prepare':('Проклятый Оборотень готовит хватку','Cursed Werewolf prepares to grab'), 'grab_bite':('Пасть смыкается','Jaws snap shut'), 'grab_release':('Проклятый Оборотень отпускает добычу','Cursed Werewolf releases prey'), 'hurt':('Проклятый Оборотень ранен','Cursed Werewolf hurts'), 'phase_roar':('Проклятый Оборотень ревёт','Cursed Werewolf roars'), 'fake_fall':('Зверь падает','Beast falls'), 'curse_revive':('Проклятие оживляет зверя','Curse revives the beast'), 'escape_howl':('Проклятый Оборотень воет на бегу','Cursed Werewolf howls and flees'), 'bad_blood_apply':('Кровь темнеет','Blood darkens'), 'grave_rustle':('У могилы шуршит трава','Grave grass rustles'), 'grave_dig':('Земля могилы осыпается','Grave soil crumbles'), 'skull_unearth':('Череп извлечён','Skull unearthed'), 'curse_cleared':('Проклятие снято','Curse lifted')}
for locale in ('ru_ru','en_us'):
 path=A/f'lang/{locale}.json';merged=json.loads(path.read_text(encoding='utf-8'));merged.update(text[locale]);merged.update({'subtitles.slavicmyths.volkolak.'+name:values[0 if locale=='ru_ru'else 1]for name,values in subtitles.items()});write(path,merged)

if __name__ == "__main__":
 from feedback_resource_policy import apply as apply_feedback_policy
 apply_feedback_policy()
