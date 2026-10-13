"""Authored native rigs: slim blond traveler with curved .98b saber/recurved .93b bow;
Skim: long predator, segmented mane, tusks, four articulated paw chains and five-link tail.
Uses established UV emitter only, never copies a prior hero or wolf model.
"""
from pathlib import Path
import inspect,textwrap,json,shutil,hashlib,math,re
from PIL import Image,ImageDraw
import bogatyr_assets as base
R=Path(__file__).resolve().parents[1];S=R/'docs/specs/1.7.3';O=R/'docs/verification/skim-1.7.3';O.mkdir(parents=True,exist_ok=True)
base.O=O
base.PALETTE.update({'blond':['D7AE54','A77836','F5D981'],'blue':['2C83C9','174F84','79CBF5'],'skin':['E8B38C','BD7F5C','FFD7B1'],'cream':['E9D6BC','BFA78C','FFF0D8'],'red':['982D2B','5C1F22','C85A46'],'leather':['8A5D38','563822','BC8652'],'boots':['453026','251D1A','74513A'],'steel':['C8D0D6','727E8E','EDF5FC'],'gold':['CBA044','906626','F0CC70'],'coat':['4F3830','211E1C','80614B','654234'],'mane':['282122','211E1C','654234'],'scar':['872C2B','5C2525','D35B38'],'bone':['D5C29E','9B896A','E0DBC0'],'eye':['F4B73D','B9492E','FFD772']})
classes={'alyosha_popovich':'AlyoshaMesh','skim_beast':'SkimMesh'}
source=textwrap.dedent(inspect.getsource(base.Rig.emit)).replace("cls='IlyaMesh' if self.name=='ilya_muromets' else 'IdolishcheMesh'","cls=classes[self.name]")
source=source.replace("'height':40 if self.scale==1 else 144","'height':34.4 if self.name=='alyosha_popovich' else 48")
source=source.replace("PartPose.offset('+','.join(map(f,coords))+'));'", "PartPose.offsetAndRotation('+','.join(map(f,coords))+','+','.join(map(f,bone.get('rotation',[0,0,0])))+'));'")
space=dict(base.__dict__,classes=classes);exec(source,space)
class Rig(base.Rig):emit=space['emit']
def box(r,b,x,y,z,w,h,d,m):r.box(b,x,y,z,w,h,d,m)
def hero():
 r=Rig('alyosha_popovich');r.bone('hips',None,[0,16,0]);box(r,'hips',0,13.8,0,9.3,3.2,5.8,'red');r.bone('body','hips',[0,18,0]);box(r,'body',0,16,0,9.76,12,6.08,'cream');box(r,'body',0,17,0,8.3,9.4,6.4,'leather');box(r,'hips',0,14.8,-3.4,10,.9,.6,'boots');box(r,'hips',0,14.7,-4,2,1.7,.6,'gold')
 for i in range(7):box(r,'body',-3.7+i*1.1,26-i*1.3,-3.7,1.8,1.3,.7,'leather')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone(tag+'_thigh','hips',[side*3,14,0]);box(r,tag+'_thigh',side*3,8.2,0,3.5,6.5,4,'boots');r.bone(tag+'_shin',tag+'_thigh',[side*3,8.5,0]);box(r,tag+'_shin',side*3,3.2,0,3.4,5.5,4,'leather');r.bone(tag+'_foot',tag+'_shin',[side*3,4,0]);box(r,tag+'_foot',side*3,0,-.8,4,4.6,5.9,'boots')
  for j in range(3):box(r,tag+'_shin',side*3,3.5+j*1.7,-2.2,3.6,.7,.6,'gold')
  r.bone(tag+'_shoulder','body',[side*6.3,28,0]);box(r,tag+'_shoulder',side*6.4,22.5,0,3.3,5.5,4.2,'cream');box(r,tag+'_shoulder',side*6.4,25,0,3.9,2.5,4.7,'leather');r.bone(tag+'_hand',tag+'_shoulder',[side*6.4,22,0]);box(r,tag+'_hand',side*6.4,18.1,0,3,4.8,3.2,'skin');box(r,tag+'_hand',side*6.4,20.7,0,3.4,1.3,3.5,'boots')
  r.bone(tag+'_flap','hips',[side*3,15,0]);box(r,tag+'_flap',side*3,9.7,-.3,4.4,5.2,6.3,'red');box(r,tag+'_flap',side*3,10,-3.65,4.4,.7,.3,'gold');box(r,'hips',side*5.2,12,1,2.5,3.8,3.3,'leather')
 r.bone('neck','body',[0,28.6,0]);box(r,'neck',0,27.7,0,4,1.5,4.3,'red');r.bone('head','neck',[0,29.1,0]);box(r,'head',0,26.72,0,8.48,7.68,8,'skin')
 # Height fixed at34.4; asymmetric locks lie around, never a generic armor cap.
 box(r,'head',0,33.0,0,8.7,.6,8.4,'blond')
 for i in range(12):box(r,'head',-3.4+(i%4)*2.2,33.1,(i//4-1)*2.5,2.5,.7+(i%3)*.3,2.8,'blond')
 box(r,'head',0,29.1,4.1,8.7,4.4,1.4,'blond')
 for i in range(9):box(r,'head',-4+i,31.2+(i%3)*.55,-4.2,1.55,2.2-(i%3)*.3,1.8,'blond')
 for side in [-1,1]:
  for i in range(4):box(r,'head',side*4.1,29.2+i*.8,1.2-i*1.4,1.4,2.1,2.1,'blond')
  box(r,'head',side*2.15,29.8,-4.15,1.7,1.3,.35,'blue');box(r,'head',side*2.15,31.3,-4.1,2.2,.4,.4,'blond')
 box(r,'head',0,29,-4.4,.9,1.1,.6,'skin');box(r,'head',0,28,-4.2,2,.35,.25,'boots');box(r,'neck',0,25.3,-3.8,9.5,1.7,1.8,'red')
 for i in range(3):r.bone('scarf_'+str(i),'neck' if i==0 else 'scarf_'+str(i-1),[3+i*.3,27-i*2.2,3.3]);box(r,'scarf_'+str(i),3+i*.3,24.8-i*2.2,3.4,2.5,2.6,.7,'red')
 for i in range(3):r.bone('cloak_'+str(i),'body' if i==0 else 'cloak_'+str(i-1),[0,27-i*4.5,4.1+i*.3]);box(r,'cloak_'+str(i),0,22.4-i*4.5,4.2+i*.3,8.2,4.8,.8,'red');box(r,'cloak_'+str(i),0,22.5-i*4.5,4.8+i*.3,7,.5,.25,'gold')
 r.bone('quiver','body',[3.7,25,5.5]);box(r,'quiver',3.7,17.5,5.5,2.8,8,2.7,'leather')
 for i in range(4):box(r,'quiver',2.8+i*.6,24,5.3, .4,6.5,.4,'wood');box(r,'quiver',2.8+i*.6,29,5.3,.8,1.8,.7,'cream')
 r.bone('saber','right_hand',[-6.4,18.3,-1]);box(r,'saber',-6.4,15.6,-1,1.4,2.9,1.4,'wood');box(r,'saber',-6.4,15,-1,3.4,.8,1.7,'gold');box(r,'saber',-6.4,18.3,-1,1.7,.8,1.7,'gold')
 for i in range(7):box(r,'saber',-6.4+i*.12,4.1+i*1.6,-1-i*.20,1.55,1.75,.65,'steel')
 r.bone('bow','left_hand',[6.4,20,0]);box(r,'bow',6.4,17.5,-1,.8,3.5,.9,'wood')
 for side in [-1,1]:
  for i in range(4):box(r,'bow',6.4,19+side*(2+i*1.5),-1-(i/2)**2,.8,1.8,.85,'wood')
 r.bone('string','bow',[6.4,20,-4.3]);box(r,'string',6.4,12,-4.3,.2,14.8,.2,'cream');r.bone('draw_arrow','left_hand',[6.4,20,-1]);box(r,'draw_arrow',6.4,20,-9,.35,.35,9,'wood')
 r.bone('back_bow','body',[-3,22,5.9])
 for side in [-1,1]:
  for i in range(4):box(r,'back_bow',-3,22+side*(2+i*1.5),5.9+(i/2)**2,.8,1.8,.85,'wood')
 box(r,'back_bow',-3,14.6,9.2,.2,14.8,.2,'cream')
 r.bone('flask','right_hand',[-6.4,18,0]);box(r,'flask',-6.4,17,-1,2.2,3,2.2,'green');box(r,'flask',-6.4,20,-1,1.1,1,1.1,'gold');r.emit()
def beast():
 r=Rig('skim_beast',3);r.bone('body',None,[0,33,0]);box(r,'body',0,18,-9,30,23,34,'coat');r.bone('hips','body',[0,27,17]);box(r,'hips',0,18,15,25,18,29,'coat');r.bone('neck','body',[0,35,-22]);box(r,'neck',0,25,-23,32,20,23,'mane');r.bone('head','neck',[0,37,-28]);box(r,'head',0,34,-34,16.5,13.1,19.7,'coat');box(r,'head',0,32,-44,12,6,8,'mane');box(r,'head',0,34,-48,9,3,2,'boots')
 for side in [-1,1]:
  box(r,'head',side*6,39,-44.4,3,1.8,.6,'eye');box(r,'head',side*7.5,42,-30,3.6,4.5,4,'mane');
  for i in range(3):box(r,'head',side*(5.7+i*.35),29.3-i*2.1,-49+i*.6,2.4-i*.5,4.2,2.5-i*.4,'bone')
 r.bone('jaw','head',[0,33,-30]);box(r,'jaw',0,28,-40,12.5,5,14,'coat');box(r,'jaw',0,32,-40,11.5,1.2,12,'mouth')
 for i in range(6):box(r,'jaw',-4.5+i*1.8,32,-44,.9,2,.9,'bone')
 for i in range(24):
  a=i*math.pi/12;x=math.sin(a)*(13+(i%3));y=34+math.cos(a)*9;z=-24+(i%3)*4;r.bone('mane_'+str(i),'neck',[x,y,z]);r.bones['mane_'+str(i)]['rotation']=[-.12,0,-math.sin(a)*.22];box(r,'mane_'+str(i),x,y-5,z,3.2+(i%3),min(9+(i%4),48-(y-5)),5,'mane' if i%4 else 'coat');box(r,'mane_'+str(i),x,y-5.2,z-2.9,1.2,6,.8,'scar' if i%3==0 else 'bone')
 for side,tag in [(-1,'right'),(1,'left')]:
  for pair,z,x in [('front',-16,13),('back',20,10)]:
   k=tag+'_'+pair;r.bone(k,'body' if pair=='front' else 'hips',[side*x,25,z]);box(r,k,side*x,12,z,7,14,8,'coat');r.bone(k+'_shin',k,[side*x,13,z]);box(r,k+'_shin',side*x,4,z-1,5.6,9,6,'mane');r.bone(k+'_paw',k+'_shin',[side*x,5,z-2]);box(r,k+'_paw',side*x,0,z-3,8.3,5.4,12.5,'coat')
   for i in range(4):box(r,k+'_paw',side*x-3+i*2, .1,z-9,1.6,3.5,3.7,'bone')
   for i in range(3):box(r,k,side*x,18-i*3,z-4.2,7.3,.8,.5,'scar')
 for i in range(5):
  r.bone('tail_'+str(i),'hips' if i==0 else 'tail_'+str(i-1),[0,28+i*.2,30+i*5.5]);box(r,'tail_'+str(i),0,25+i*.2,32+i*5.5,4.5-i*.4,4.8-i*.3,6.1,'coat');box(r,'tail_'+str(i),0,25.5+i*.2,32+i*5.5,4.7-i*.4,.8,5,'scar')
 box(r,'tail_4',0,25.8,57,7,5,5,'mane')
 for side in [-1,1]:
  for i in range(7):box(r,'body',side*15.1,28+(i%2)*1.1,-13+i*4, .6,3,1.8,'scar');box(r,'hips',side*12.6,24+i%2,12+i, .6,3,1,'scar')
 r.emit()

def items():
 ids=['alyosha_call_whistle','ilya_call_token','dobrynya_call_emblem','beast_challenge_horn','skim_beast_spawn_egg','idolishche_poganoe_spawn_egg','marinka_spawn_egg']
 for k in ids:
  im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
  if k=='alyosha_call_whistle':
   d.line([(30,37),(25,47),(31,54),(27,59)],fill='#5C1F22',width=5);d.line([(30,37),(25,47),(31,54),(27,59)],fill='#9C3A2D',width=3)
   d.polygon([(12,25),(18,17),(46,12),(53,21),(49,35),(22,43),(13,37)],fill='#69462C');d.polygon([(14,25),(19,19),(46,14),(50,22),(46,33),(23,39),(16,35)],fill='#A77B3C');d.line([(19,19),(46,14),(50,22)],fill='#E8C66A',width=3);d.rectangle((35,20,40,23),fill='#453026');d.polygon([(27,27),(31,23),(35,27),(31,31)],fill='#2C83C9')
  elif k=='ilya_call_token':
   d.line([(15,14),(8,9),(8,3),(15,4),(19,10)],fill='#69462C',width=4);d.rectangle((12,9,51,51),fill='#30363E');d.rectangle((15,12,48,48),fill='#73777E');d.line([(15,48),(15,12),(48,12)],fill='#BEC4C8',width=2);d.ellipse((21,17,43,40),fill='#541E24');d.polygon([(24,19),(40,19),(40,30),(32,41),(24,30)],fill='#9D302C');d.line([(32,21),(32,33)],fill='#E9C970',width=3);d.line([(27,25),(37,25)],fill='#E9C970',width=3)
  elif k=='dobrynya_call_emblem':
   d.polygon([(18,50),(21,41),(29,30),(46,7),(43,33),(32,42),(24,54)],fill='#69462C');d.polygon([(22,43),(27,32),(46,7),(41,31),(31,42)],fill='#7F93A2');d.polygon([(27,32),(46,7),(31,39)],fill='#BBC9CE');d.line([(22,43),(46,7)],fill='#EAF2F2',width=2);d.line([(31,42),(41,31),(46,7)],fill='#C1A05A',width=2);d.polygon([(24,43),(21,50),(16,54),(21,54),(27,48),(30,53),(33,52),(29,43)],fill='#8DAFC4')
  elif k=='beast_challenge_horn':
   d.polygon([(10,52),(12,39),(22,36),(35,30),(45,19),(55,9),(53,25),(44,38),(29,48),(21,57)],fill='#251D1A');d.polygon([(13,49),(15,41),(26,39),(39,30),(50,18),(50,26),(42,37),(28,45),(20,53)],fill='#4B342B');d.polygon([(39,30),(50,18),(55,9),(53,25),(44,37)],fill='#E5CDA4');d.line([(16,40),(20,44),(24,48)],fill='#A13335',width=5)
   for i in range(5):d.line([(27+i*3,40-i*3),(30+i*3,43-i*3)],fill='#80614B',width=2)
   d.line([(14,47),(8,55),(13,59)],fill='#CBA044',width=2);d.line([(15,47),(12,54),(17,59)],fill='#A13335',width=2)
  else:
   colors={'skim_beast_spawn_egg':('#3B2A25','#9A3430','#D6C19D'),'idolishche_poganoe_spawn_egg':('#59413B','#C1A48A','#887363'),'marinka_spawn_egg':('#2F222C','#9B3351','#69445D')}[k]
   d.ellipse((15,9,48,55),fill=colors[0]);d.arc((17,11,46,53),180,280,fill=colors[2],width=3)
   for x,y in [(24,17),(37,23),(22,35),(38,42),(30,49)]:d.rectangle((x,y,x+4,y+5),fill=colors[1]);d.rectangle((x+2,y+1,x+3,y+2),fill=colors[2])
  im.save(base.A/'textures/item'/f'{k}.png')
  model={'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{k}'}}
  if k in ('alyosha_call_whistle','beast_challenge_horn'):
   model['display']={side:{'rotation':[0,-35,-20 if k=='alyosha_call_whistle' else 35],'translation':[0,1,0],'scale':[.65,.65,.65] if k=='alyosha_call_whistle' else [.9,.9,.9]} for side in ['firstperson_righthand','firstperson_lefthand']}
  (base.A/'models/item'/f'{k}.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf8')
 def recipe(k,pattern,keys):
  data={'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{c:{'item':'minecraft:'+v} for c,v in keys.items()},'result':{'id':'slavicmyths:'+k,'count':1}}
  q=R/'src/main/resources/data/slavicmyths/recipe'/f'{k}.json';q.write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')
  unlock={'parent':'minecraft:recipes/root','criteria':{'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['minecraft:'+next(iter(keys.values()))]}]}}},'requirements':[['has_material']],'rewards':{'recipes':['slavicmyths:'+k]}}
  (R/'src/main/resources/data/slavicmyths/advancement/recipes'/f'{k}.json').write_text(json.dumps(unlock,indent=2)+'\n',encoding='utf8')
 recipe('beast_challenge_horn',[' B ',' H ',' R '],{'B':'bone','H':'goat_horn','R':'red_dye'})
 recipe('alyosha_call_whistle',[' N ',' C ',' R '],{'N':'iron_nugget','C':'copper_ingot','R':'red_dye'})
 recipe('ilya_call_token',[' I ',' R ',' P '],{'I':'iron_ingot','R':'red_dye','P':'paper'})
 recipe('dobrynya_call_emblem',[' N ',' I ',' P '],{'N':'gold_nugget','I':'iron_ingot','P':'paper'})
def resources():
 manifest=json.loads((S/'07_Audio/manifest.json').read_text(encoding='utf8'));sounds=json.loads((base.A/'sounds.json').read_text(encoding='utf8'))
 langs={k:json.loads((base.A/'lang'/f'{k}.json').read_text(encoding='utf8')) for k in ('ru_ru','en_us')}
 # Exact UI pairs authored in the package.
 for line in (S/'08_Localization/01_RU_EN_STRINGS.md').read_text(encoding='utf8').splitlines():
  parts=[x.strip() for x in line.strip('|').split('|')]
  if len(parts)==3 and parts[0].startswith(('entity.','item.','message.','subtitle.')):
   for lang,i in [('ru_ru',1),('en_us',2)]:langs[lang][parts[0]]=parts[i]
 extras={'gui.slavicmyths.173.recovering':('Восстановление','Recovering'),'tooltip.slavicmyths.hero_call':('Нерасходуемый призыв или отзыв вне сюжетной битвы. Перерыв: 5 секунд.','Reusable call or recall outside story battles. Cooldown: 5 seconds.'),'tooltip.slavicmyths.beast_horn':('Нерасходуемый вызов только у знака долины. Сначала призовите Алёшу.','Reusable challenge at the valley marker. Call Alyosha before starting.'),'message.slavicmyths.summon.locked':('Сначала откройте запись в книге знаний.','Discover the lore book entry first.'),'message.slavicmyths.summon.need_alyosha':('Перед вызовом зверя призовите Алёшу рядом с собой.','Call Alyosha nearby before challenging the beast.'),'message.slavicmyths.valley.unsafe':('Для долины нужен свободный сухой участок. Постройки и чанки сохраняются.','The valley needs a clear dry site. Buildings and unloaded chunks are protected.'),'message.slavicmyths.skim.start':('Долина Ским-зверя. Призыв и отзыв богатырей закрыт до конца боя.','Skim Beast Valley. Hero calls and recalls are locked until the battle ends.')}
 for key,pair in extras.items():
  for lang,i in [('ru_ru',0),('en_us',1)]:langs[lang][key]=pair[i]
 labels={'summon':('Алёша появляется','Alyosha arrives'),'footstep_soft':('Тихие шаги Алёши','Alyosha steps softly'),'taunt':('Алёша свистит и привлекает внимание','Alyosha whistles a taunt'),'dodge_swish':('Алёша уворачивается','Alyosha dodges'),'saber_swing':('Свист сабли','Saber swishes'),'saber_hit':('Сабля попадает в цель','Saber strikes'),'bow_draw':('Натягивается лук','Bow draws'),'arrow_release':('Стрела летит','Arrow flies'),'feint':('Алёша делает ложный замах','Alyosha feints'),'counter':('Алёша контратакует','Alyosha counters'),'hurt':('Болезненный выдох','Painful grunt'),'drink_tonic':('Алёша пьёт настой','Alyosha drinks tonic'),'recover_breath':('Алёша переводит дух','Alyosha catches his breath'),'victory':('Алёша радуется победе','Alyosha celebrates'),'ambient':('Зверь низко рычит','Beast growls'),'step':('Тяжёлые шаги зверя','Heavy beast steps'),'run_paws':('Зверь бежит','Beast runs'),'claw_windup':('Зверь заносит когти','Beast raises claws'),'claw_hit':('Удар огромных когтей','Huge claws strike'),'leap_charge':('Зверь готовится прыгнуть','Beast prepares to leap'),'leap_land':('Зверь приземляется','Beast lands'),'whistle_charge':('Зверь набирает воздух','Beast draws breath'),'whistle_release':('Змеиный свист зверя','Serpentine whistle'),'roar':('Ским-зверь рычит','Skim Beast roars'),'grab':('Зверь хватает добычу','Beast grabs prey'),'grip_hold':('Скрип звериной пасти','Beast jaws creak'),'throw':('Зверь отбрасывает добычу','Beast throws prey'),'rage':('Звериное неистовство','Beast rages'),'exhausted':('Зверь тяжело дышит','Beast pants'),'stone_collision':('Зверь ударяется о камень','Beast hits stone'),'death':('Зверь рухнул','Beast falls'),'wind':('Ветер долины','Valley wind'),'stone_impact':('Каменный удар','Stone impact'),'den_stone_open':('Осыпается преграда логова','Den barrier crumbles'),'camp_creak':('Скрип лагеря','Camp creaks'),'alyosha_whistle':('Свисток Алёши','Alyosha whistle'),'ilya_token':('Звенит знак Ильи','Ilya token chimes'),'dobrynya_emblem':('Звенит знак Добрыни','Dobrynya emblem chimes'),'beast_horn':('Рог вызывает зверя','Beast challenge horn')}
 provenance=[]
 for e in manifest['events']:
  src=S/'07_Audio'/e['path'];dst=base.A/'sounds'/Path(e['path'].lower());dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst);assert hashlib.sha256(dst.read_bytes()).hexdigest()==e['sha256']
  key=e['key'];sub='subtitles.slavicmyths.'+key;sounds[key]={'subtitle':sub,'sounds':[{'name':'slavicmyths:'+Path(e['path'].lower()).with_suffix('').as_posix(),'attenuation_distance':48 if key=='skim.roar' else 36 if key.startswith('skim.whistle') else 28 if key.startswith('skim.') else 18}]}
  label=re.sub(r'_[12]$','',key.split('.')[1]);pair=labels[label]
  for lang,i in [('ru_ru',0),('en_us',1)]:langs[lang][sub]=pair[i]
  provenance.append({'event':key,'path':str(dst.relative_to(R)),'sha256':e['sha256'],'source':e['path']})
 for lang,data in langs.items():(base.A/'lang'/f'{lang}.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 (base.A/'sounds.json').write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf8');(O/'audio-provenance.json').write_text(json.dumps(provenance,indent=2)+'\n',encoding='utf8')
 for k in ['claw','leap','whistle','roar','flank','grip']:
  q=R/'src/main/resources/data/slavicmyths/damage_type'/f'skim_{k}.json';q.write_text(json.dumps({'message_id':'slavicmyths.skim_'+k,'exhaustion':.1,'scaling':'when_caused_by_living_non_player'})+'\n',encoding='utf8')
  for lang in langs:langs[lang]['death.attack.slavicmyths.skim_'+k]='%1$s пал от лап Ским-зверя' if lang=='ru_ru' else '%1$s fell to the Skim Beast'
 for lang,data in langs.items():(base.A/'lang'/f'{lang}.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
if __name__=='__main__':hero();beast();items();resources()

if __name__ == "__main__":
 from feedback_resource_policy import apply as apply_feedback_policy
 apply_feedback_policy()
