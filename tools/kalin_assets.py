"""1.7.5 authored geometry; saber: 27px curved wide steel blade, 8px gold guard, leather hilt.
Horse: 51px to plume; armored mask, four articulated legs, crimson barding. No old rigs changed.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import inspect,textwrap,json,shutil
import bogatyr_assets as base
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/verification/kalin-1.7.5';S=R/'docs/specs/1.7.5';base.O=O
base.PALETTE.update({'black':('29292D','16171C','4A494D'),'crimson':('8C2635','551D28','B64142'),'gold':('C19B50','806331','E1C37D'),'steel':('757A82','424750','AFB2B4')})
classes={'kalin_tsar':'KalinMesh','kalin_war_horse':'WarHorseMesh','kalin_common_soldier':'CommonSoldierMesh','kalin_elite_soldier':'EliteSoldierMesh'}
s=textwrap.dedent(inspect.getsource(base.Rig.emit)).replace("cls='IlyaMesh' if self.name=='ilya_muromets' else 'IdolishcheMesh'","cls=classes[self.name]").replace("'height':40 if self.scale==1 else 144","'height':max(b['center'][1]+b['size'][1]/2 for b in self.boxes)")
space=dict(base.__dict__,classes=classes);exec(s,space)
class Rig(base.Rig):emit=space['emit']
def box(r,b,x,y,z,w,h,d,m):r.box(b,x,y,z,w,h,d,m)
def man(name,boss=False,elite=False):
 r=Rig(name);H=44.8 if boss else 35.2 if elite else 31.2;k=H/(44.8 if boss or elite else 42)
 def bone(n,p,x,y,z):r.bone(n,p,[x*k,y*k,z*k])
 def b(n,x,y,z,w,h,d,m):box(r,n,x*k,y*k,z*k,w*k,h*k,d*k,m)
 bone('hips',None,0,18,0);bone('body','hips',0,30,0);b('body',0,18,0,15 if boss else 12,16,9,'black');b('hips',0,16,0,14,3,9,'leather');b('hips',0,17,-5,3,2,.8,'gold')
 for sign,tag in [(-1,'right'),(1,'left')]:
  bone(tag+'_leg','hips',sign*3.9,17,0);b(tag+'_leg',sign*3.9,4,0,6,14,6,'black');b(tag+'_leg',sign*3.9,0,-.7,6.5,5,8,'steel');b(tag+'_leg',sign*3.9,7,-3.4,6.5,3,.8,'gold')
  bone(tag+'_arm','body',sign*10,32,0);b(tag+'_arm',sign*10,20,0,6,12,7,'black');b(tag+'_arm',sign*10,30,0,9,4,10,'fur' if boss else 'steel');b(tag+'_arm',sign*10,29,-5.1,7,1,.6,'gold');bone(tag+'_hand',tag+'_arm',sign*10,20,0);b(tag+'_hand',sign*10,15,0,5,5,6,'steel');b(tag+'_hand',sign*10,14,-.3,4,2,4,'leather')
  bone(tag+'_tasset','hips',sign*4.5,17,0);b(tag+'_tasset',sign*4.5,8,-.5,5,9,8,'black');b(tag+'_tasset',sign*4.5,8,-4.8,5,.8,.5,'gold')
 bone('head','body',0,35,0);b('head',0,33,0,9,9,9,'black');b('head',0,41,0,10,1,10,'gold');b('head',0,33,-4.8,1,8,.8,'gold');b('head',-2.3,37.5,-4.8,2,.7,.3,'red');b('head',2.3,37.5,-4.8,2,.7,.3,'red')
 if boss or elite:
  for i in range(5):b('head',0,42-i*.6,i*1.7,2.4,2.8,2.5,'crimson')
 for i in range(3):
  bone('cape_'+str(i),'body' if i==0 else 'cape_'+str(i-1),0,33-i*9,5);b('cape_'+str(i),0,24-i*9,5+i*.5,15,9,1,'crimson');b('cape_'+str(i),0,24-i*9,5.7+i*.5,15,.8,.4,'gold')
 for sign in [-1,1]:
  b('body',sign*5,21,-4.8,.7,10,.4,'gold');b('cape_1',sign*3,16,6.8,1,5,.4,'gold');b('cape_1',sign*1.8,19,6.8,2.6,1,.4,'gold')
 b('body',0,24,-5.3,2,6,.8,'gold');b('body',0,26,-5.4,6,1,.8,'gold');b('cape_1',0,13,6.8,1,12,.4,'gold');b('cape_1',0,18,6.8,8,1,.4,'gold')
 bone('weapon','right_hand',-10,15,-3);b('weapon',-10,10,-3,1.8,5,1.8,'leather');b('weapon',-10,10,-3,8,1.4,2.3,'gold')
 # Saber curved across model X rather than a straight recolored sword.
 for i in range(9):b('weapon',-10-(i/8)**2*7,7-i*2.5,-3,3.5 if i<6 else 2.6,3.4,.9,'steel');b('weapon',-11-(i/8)**2*7,7-i*2.5,-3.5,.5,3.2,.25,'gold')
 if elite:
  bone('shield','left_hand',10,17,-4);b('shield',10,5,-5,10,18,2,'crimson');b('shield',10,12,-6.1,4,4,.6,'steel');b('shield',10,5,-6.2,10,.8,.5,'gold')
 r.emit()
def horse():
 r=Rig('kalin_war_horse');r.bone('body',None,[0,29,0]);box(r,'body',0,21,1,15,17,29,'black');box(r,'body',0,34,0,12,3,16,'leather');box(r,'body',0,37,5,12,3,4,'gold')
 for s in [-1,1]:
  tag='left' if s>0 else 'right'
  for z,part in [(-10,'front'),(11,'back')]:
   name=tag+'_'+part;r.bone(name,'body',[s*5.3,23,z]);box(r,name,s*5.3,8,z,5,15,6,'black');box(r,name,s*5.3,9,z-3.2,5.5,2,.7,'gold');r.bone(name+'_hoof',name,[s*5.3,9,z]);box(r,name+'_hoof',s*5.3,0,z-.8,5.7,9,7,'steel');box(r,name+'_hoof',s*5.3,1,z-4.4,5.8,1,.5,'gold')
  r.bone(tag+'_barding','body',[s*8,31,3]);box(r,tag+'_barding',s*8,15,3,1,16,22,'crimson');box(r,tag+'_barding',s*8,15,3,1.4,1,23,'gold');box(r,tag+'_barding',s*8.8,19,4,.5,10,1,'gold');box(r,tag+'_barding',s*8.8,24,4,.5,1,10,'gold')
 r.bone('neck','body',[0,32,-10]);box(r,'neck',0,29,-12,10,18,11,'black');r.bone('head','neck',[0,46,-17]);box(r,'head',0,37,-21,10,10,15,'black');box(r,'head',0,37,-28,11,5,3,'steel');box(r,'head',0,41,-29.5,10,1,.7,'gold');box(r,'head',0,44,-23,11,1,14,'gold');box(r,'head',0,42,-29.5,1,6,.8,'gold')
 for s in [-1,1]:box(r,'head',s*5.3,43,-22,.4,1.1,2,'red');box(r,'head',s*3,47,-15,2,3,2,'black')
 for i in range(5):box(r,'head',0,48-i*.8,-18+i*2.4,2.5,3.2,3,'crimson')
 r.bone('tail','body',[0,32,16]);box(r,'tail',0,17,16,4,15,4,'black');r.emit()
if __name__=='__main__':
 man('kalin_tsar',True);man('kalin_common_soldier');man('kalin_elite_soldier',elite=True);horse()
 for key in ['kalin_battle_seal','kalin_tsar_spawn_egg','kalin_war_horse_spawn_egg']:
  img=Image.new('RGBA',(32,32));d=ImageDraw.Draw(img)
  if key.endswith('seal'):
   d.polygon([(8,5),(23,5),(28,10),(28,24),(23,28),(7,28),(3,23),(3,10)],fill='#29292d',outline='#c19b50',width=2);d.rectangle((8,10,23,23),fill='#8c2635');d.rectangle((14,9,17,25),fill='#e1c37d');d.rectangle((9,14,23,17),fill='#e1c37d');d.rectangle((11,11,13,13),fill='#e1c37d');d.rectangle((20,20,22,22),fill='#e1c37d')
  else:
   d.polygon([(12,3),(19,3),(24,10),(27,20),(24,27),(8,27),(5,20),(8,10)],fill='#29292d',outline='#c19b50',width=1);d.rectangle((10,13,22,21),fill='#8c2635');d.line((16,7,16,24),fill='#e1c37d',width=2)
   if 'horse' in key:d.polygon([(11,19),(13,9),(19,9),(21,14),(18,15),(17,20)],fill='#c19b50')
  img.save(A/'textures/item'/f'{key}.png');(A/'models/item'/f'{key}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{key}'}},indent=2)+'\n')
 subtitles={'banner_flap': ('Знамя хлопает', 'Banner flaps'), 'battle_seal_use': ('Военная печать звенит', 'Battle seal rings'), 'camp_fire_loop_short': ('Костёр потрескивает', 'Campfire crackles'), 'encounter_start_fx': ('Ставка разворачивается', 'Camp is deployed'), 'horse_gallop_loop': ('Боевой конь скачет', 'War horse gallops'), 'horse_rear': ('Конь встаёт на дыбы', 'Horse rears'), 'horse_snort': ('Конь фыркает', 'Horse snorts'), 'horse_step_01': ('Подкованные копыта стучат', 'Armored hooves clop'), 'horse_step_02': ('Тяжёлые копыта стучат', 'Heavy hooves clop'), 'horse_stomp': ('Конь бьёт копытами', 'Horse stomps'), 'kalin_command_shout_fx': ('Калин отдаёт приказ', 'Kalin commands'), 'kalin_kneel_surrender': ('Калин опускается на колено', 'Kalin kneels'), 'kalin_parry': ('Сабля парирует удар', 'Sabre parries'), 'kalin_rage_burst': ('Калин приходит в ярость', 'Kalin rages'), 'kalin_sabre_heavy': ('Тяжёлая сабля рассекает воздух', 'Heavy sabre swings'), 'kalin_sabre_light': ('Сабля свистит', 'Sabre swishes'), 'kalin_spin_attack': ('Стальной вихрь вращается', 'Steel whirlwind spins'), 'kalin_step_heavy_01': ('Тяжёлая броня гремит', 'Heavy armor clanks'), 'kalin_step_heavy_02': ('Калин тяжело шагает', 'Kalin steps heavily'), 'kalin_war_horn': ('Военный рог зовёт дружину', 'War horn calls troops'), 'soldier_spawn_common': ('Воины вступают в бой', 'Soldiers join battle'), 'soldier_spawn_elite': ('Дружинники вступают в бой', 'Elite soldiers join battle'), 'ui_select_two_heroes': ('Пара богатырей выбрана', 'Hero pair selected')}
 sounds=json.loads((A/'sounds.json').read_text());ru=json.loads((A/'lang/ru_ru.json').read_text());en=json.loads((A/'lang/en_us.json').read_text())
 for src in sorted((S/'07_Audio/ogg').glob('*.ogg')):
  dst=A/'sounds/kalin'/src.name;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst);key='kalin.'+src.stem;subtitle='subtitle.slavicmyths.'+key;sounds[key]={'sounds':[{'name':'slavicmyths:kalin/'+src.stem,'stream':False}],'subtitle':subtitle};ru[subtitle],en[subtitle]=subtitles[src.stem]
 names={'kalin_tsar':('Калин-царь','Kalin-tsar'),'kalin_war_horse':('Боевой конь Калина',"Kalin’s War Horse"),'kalin_common_soldier':('Воин Калина','Kalin Soldier'),'kalin_elite_soldier':('Дружинник Калина','Kalin Elite Soldier')}
 for key,(a,b) in names.items():ru['entity.slavicmyths.'+key]=a;en['entity.slavicmyths.'+key]=b
 texts={'item.slavicmyths.kalin_battle_seal':('Печать ставки Калина','Kalin Battle Seal'),'item.slavicmyths.kalin_tsar_spawn_egg':('Яйцо призыва Калина-царя','Kalin-tsar Spawn Egg'),'item.slavicmyths.kalin_war_horse_spawn_egg':('Яйцо призыва боевого коня Калина','Kalin War Horse Spawn Egg'),'tooltip.slavicmyths.kalin_battle_seal':('Призовите ровно двух богатырей и примените печать на открытой ровной площадке 65×65. Не расходуется.','Summon exactly two bogatyrs, then use on an open level 65×65 site. Reusable.'),'message.slavicmyths.kalin_need_open_area':('Нужна ровная открытая площадка 65×65 без построек, воды и препятствий.','A level, open 65×65 site without buildings, water or obstacles is required.'),'message.slavicmyths.kalin_need_two_heroes':('Призовите ровно двух доступных богатырей рядом с собой. Оба должны быть готовы к бою.','Summon exactly two available bogatyrs nearby. Both must be ready for battle.'),'message.slavicmyths.kalin_busy':('Здесь уже идёт испытание или уборка лагеря.','An encounter or camp cleanup is already in progress.'),'message.slavicmyths.kalin_selected':('Пара богатырей выбрана. Печать закрепит выбор до конца битвы.','Hero pair selected. The seal locks this choice until the battle ends.'),'message.slavicmyths.kalin_building':('Ставка Калина разворачивается. Выбранных богатырей нельзя заменить.','Kalin’s camp is being deployed. Selected heroes cannot be replaced.'),'message.slavicmyths.kalin_start':('Калин-царь выходит в бой верхом!','Kalin-tsar rides into battle!'),'message.slavicmyths.kalin_won':('Калин-царь сдаётся! Победа и выбранная пара сохранены; награды появятся в 1.7.6.','Kalin-tsar surrenders! Victory and hero pair saved; rewards follow in 1.7.6.'),'gui.slavicmyths.kalin_army':('Калин-царь • войско %s/14','Kalin-tsar • army %s/14')}
 texts['message.slavicmyths.kalin_surrender']=('Сдаюсь. Моё войско разбито.','I yield. My army is broken.')
 for kind in ['sabre','charge','spin','stomp']:
  texts['death.attack.kalin_'+kind]=('%1$s пал от удара %2$s','%1$s fell to %2$s')
 for key,(a,b) in texts.items():ru[key]=a;en[key]=b
 for path,data in [(A/'sounds.json',sounds),(A/'lang/ru_ru.json',ru),(A/'lang/en_us.json',en)]:path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')

if __name__ == "__main__":
 from feedback_resource_policy import apply as apply_feedback_policy
 apply_feedback_policy()
