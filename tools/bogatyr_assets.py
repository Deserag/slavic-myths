"""Deterministic native cuboid rigs, individually packed UV islands and additive resources.

Silhouettes: Ilya 40px, steel/fur broad shoulders, red tapered 16x26 shield in LEFT
hand, RIGHT wooden 3x19 grip / 10x9 spiked iron mace. Idolishche 144px, pear belly,
long asymmetrical hands, articulated teeth/jaw, six-post wooden trophy rack.
No raster reference is used as a UV texture; audio is copied byte-for-byte.
"""
from pathlib import Path
import json, math, shutil, hashlib
from PIL import Image, ImageDraw
from hero_visual_style import prepare, pixel, face
R=Path(__file__).resolve().parents[1]
A=R/'src/main/resources/assets/slavicmyths'
O=R/'docs/verification/bogatyr-1.7.1';O.mkdir(parents=True,exist_ok=True)
S=R/'docs/specs/1.7.1'
PALETTE={'steel':('92969D','454B55','B3B4B2'),'red':('A63C36','6C282B','C39A56'),
 'fur':('AEA79B','756F6A','D4CBBF'),'leather':('6C4531','39281F','9C7452'),
 'skin':('C99773','895F47','E0B293'),'hair':('493125','2D211B','72513C'),
 'gold':('B58A43','765A2C','D8B36E'),'shield':('7A292B','541E24','AB453A'),
 'flesh':('B98571','7B5146','D49D87'),'scar':('A94845','674546','B66E5D'),
 'bone':('C8B698','857764','E2D4B8'),'wood':('56402C','30261D','836145'),
 'cloth':('6C3933','452526','99544C'),'moss':('625843','43532D','879F43'),
 'mouth':('4A2425','241B1C','713D3E'),'eye':('362524','100D0D','97654C'),
 'green':('538347','29442B','91B779'),'rage':('C44332','862C23','D26849')}
class Rig:
 def __init__(self,name,scale=1):self.name=name;self.scale=scale;self.bones={};self.boxes=[]
 def bone(self,name,parent,pos):self.bones[name]={'parent':parent,'pos':pos,'boxes':[]};return name
 def box(self,bone,cx,y,z,w,h,d,mat):
  b={'bone':bone,'center':[cx,y+h/2,z],'size':[w,h,d],'material':mat};self.boxes.append(b);self.bones[bone]['boxes'].append(b)
 def emit(self):
  prepare(self)
  # Unique rectangles, two transparent pixels padding. Boss pixels correspond to 4 model px.
  size=256;img=Image.new('RGBA',(size,size));debug=Image.new('RGBA',(size,size),(24,25,29,255));dd=ImageDraw.Draw(debug)
  x=y=2;row=0
  for idx,b in enumerate(self.boxes):
   w,h,d=b['size'];rw=math.ceil((2*w+2*d)/self.scale);rh=math.ceil((h+d)/self.scale)
   if x+rw+2>size:x=2;y+=row+4;row=0
   if y+rh+2>size:raise ValueError((self.name,'UV overflow',idx,y,rh))
   b['uv']=[x,y,rw,rh];row=max(row,rh)
   colors=[tuple(bytes.fromhex(c))+(255,) for c in PALETTE[b['material']]]
   for py in range(rh):
    for px in range(rw):
     mat=b['material'];color=colors[0];noise=(px*17+py*31+idx*11)%29
     color=pixel(mat,colors,px,py,round(w/self.scale),round(h/self.scale),round(d/self.scale),idx,self.name,rw,rh)
     img.putpixel((x+px,y+py),color)
   dd.rectangle((x,y,x+rw-1,y+rh-1),outline=colors[0]);dd.text((x,y),str(idx),fill='white');x+=rw+4
  face(self,img,PALETTE)
  path=A/'textures/entity'/f'{self.name}.png';path.parent.mkdir(parents=True,exist_ok=True);img.save(path);debug.save(O/f'UV_LAYOUT_{self.name}.png')
  cls='IlyaMesh' if self.name=='ilya_muromets' else 'IdolishcheMesh';lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;',f'public final class {cls} {{ public static ModelPart create(){{var mesh=new MeshDefinition();var root=mesh.getRoot();']
  f=lambda n: f'{float(n):.5f}F'
  for name,bone in self.bones.items():
   pos=bone['pos'];parent=bone['parent'];pp=self.bones[parent]['pos'] if parent else [0,24,0]
   cubes='CubeListBuilder.create()'
   for b in bone['boxes']:
    cx,cy,cz=b['center'];w,h,d=b['size'];u,v,_,_=b['uv']
    coords=[cx-w/2-pos[0],pos[1]-(cy+h/2),cz-d/2-pos[2],w,h,d]
    cubes+=f'.texOffs({u*self.scale},{v*self.scale}).addBox('+','.join(map(f,coords))+f',CubeDeformation.NONE,{f(self.scale)},{f(self.scale)})'
   coords=[pos[0]-pp[0],pp[1]-pos[1],pos[2]-pp[2]]
   lines.append(f'var {name}={(parent or "root")}.addOrReplaceChild("{name}",{cubes},PartPose.offset('+','.join(map(f,coords))+'));')
  lines.append(f'return LayerDefinition.create(mesh,{size},{size}).bakeRoot();}} private {cls}(){{}} }}')
  (R/'src/main/java/org/slavicmyths/client'/f'{cls}.java').write_text('\n'.join(lines)+'\n',encoding='utf8')
  (O/f'{self.name}-rig.json').write_text(json.dumps({'height':40 if self.scale==1 else 144,'atlas':size,'uvScale':self.scale,'bones':self.bones,'boxes':self.boxes},indent=2)+'\n',encoding='utf8')
  print(self.name,len(self.bones),'bones',len(self.boxes),'cuboids','atlas',size)

def hero():
 r=Rig('ilya_muromets');r.bone('hips',None,[0,18,0]);r.box('hips',0,14,0,14,5,8,'red')
 r.bone('body','hips',[0,19,0]);r.box('body',0,18,0,18,15,9,'steel');r.box('body',0,17,-.1,18.2,3,9.4,'leather')
 r.box('body',0,17,-5.2,4,3.5,1,'gold');r.box('body',-5,21,5.1,6,5,3,'leather')
 for i in range(6):r.box('body',-7+i*2.5,29-i*1.7,-4.8,3,2.6,1,'leather')
 r.bone('neck','body',[0,32,0]);r.box('neck',0,31,0,20,3,11,'fur')
 for i in range(7):r.box('neck',-9+i*3,30.2+(i%2)*.4,-5.4,2.7,3,2,'fur')
 r.bone('head','neck',[0,33,0]);r.box('head',0,32,0,9,8,9,'skin')
 r.box('head',0,38.8,.2,9.4,1.2,9.2,'hair')
 for i in range(8):r.box('head',-4+i*1.1,37.1+(i%3)*.4,-4.5,1.4,min(2.5,40-(37.1+(i%3)*.4)),1.2,'hair')
 r.box('head',0,33.5,-5.1,2,3,1.7,'skin')
 for side in [-1,1]:
  r.box('head',side*2.5,36.1,-4.7,2.4,1,.6,'hair');r.box('head',side*2.5,35.4,-4.68,1.8,.7,.7,'eye')
  r.box('head',side*4.8,34.2,0,1,2,2,'skin')
 for i in range(5):r.box('head',-3.2+i*1.6,30.8+(i%2)*.5,-6.3,1.9,3.7,3.3,'hair')
 r.box('head',0,34.7,4.6,9.3,4.3,1.4,'hair')
 for side in [-1,1]:r.box('head',side*4.6,36,0,1.2,3.6,8.8,'hair')
 r.box('head',0,33.5,-5.4,6,1,1.2,'hair')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone(tag+'_thigh','hips',[side*4,17,0]);r.box(tag+'_thigh',side*4,11,0,6.8,7,7,'red')
  r.bone(tag+'_shin',tag+'_thigh',[side*4,11,0]);r.box(tag+'_shin',side*4,5,0,6,7,6,'steel')
  r.bone(tag+'_foot',tag+'_shin',[side*4,5,0]);r.box(tag+'_foot',side*4,0,-.6,6,6,7,'leather');r.box(tag+'_foot',side*4,4.4,-.3,6.2,1,7,'fur')
  for i in range(3):r.box(tag+'_thigh',side*4,12+i*1.8,-3.65,7,.7,.5,'bone')
  r.bone(tag+'_shoulder','body',[side*11,31,0]);r.box(tag+'_shoulder',side*11,28,0,6,5,9,'steel');r.box(tag+'_shoulder',side*11,28.6,-4.7,4,3,.8,'red')
  r.bone(tag+'_arm',tag+'_shoulder',[side*11,28,0]);r.box(tag+'_arm',side*11,20,0,6,9,7,'steel')
  r.bone(tag+'_hand',tag+'_arm',[side*11,20,0]);r.box(tag+'_hand',side*11,13.5,0,6,7,7,'steel');r.box(tag+'_hand',side*11,12.5,-.2,5,3,5,'skin')
  for i in range(3):r.box(tag+'_hand',side*11,15+i*1.8,-3.65,6.2,.7,.6,'gold')
 r.bone('shield','left_hand',[11,17,-4.8])
 for i,(w,yy,hh) in enumerate([(10,5,4),(14,9,5),(16,14,9),(14,23,5),(10,28,3)]):
  r.box('shield',11,yy,-5.8,w,hh,2,'steel');r.box('shield',11,yy+.7,-7,w-1.8,hh-1.2,.45,'shield')
 r.box('shield',11,15.5,-7.8,4,4,1.4,'steel')
 for dx,dy in [(-4,0),(4,0),(0,6),(0,-6),(-3,4),(3,4),(-3,-4),(3,-4)]:r.box('shield',11+dx,17+dy,-7.5,1.3,1.3,.5,'gold')
 r.bone('mace','right_hand',[-11,14,-1]);r.box('mace',-11,3,-1,3,19,3,'wood');r.box('mace',-11,4,-1,10,9,10,'steel')
 for s in [-1,1]:
  for off in [-2.5,2.5]:r.box('mace',-11+s*6,6+off/3,off-1,3,2,2,'steel');r.box('mace',-11+off,6+off/3,-1+s*6,2,2,3,'steel')
 r.box('mace',-11,2,-1,2,3,2,'steel')
 r.bone('flask','right_hand',[-11,14,-4]);r.box('flask',-11,13,-4,3,4,3,'green');r.box('flask',-11,17,-4,1.6,1.8,1.6,'gold')
 r.emit()
def boss():
 r=Rig('idolishche_poganoe',4);r.bone('hips',None,[0,52,0]);r.box('hips',0,48,0,52,14,36,'cloth')
 r.bone('belly','hips',[0,58,-1]);r.box('belly',0,52,-3,60,36,44,'flesh');r.box('belly',0,58,-8,54,26,43,'flesh')
 for i in range(3):r.box('belly',0,62+i*7,-30,47-i*3,1.8,1.8,'scar')
 r.bone('chest','belly',[0,86,0]);r.box('chest',0,80,0,65,33,40,'flesh');r.box('chest',0,110,1,44,7,31,'flesh')
 for i in range(7):r.box('chest',-16+i*4,91+i%3*2,-20.6,2,8,1.2,'scar')
 r.bone('head','chest',[0,116,-4]);r.box('head',0,115,-4,35,21,29,'flesh');r.box('head',0,127,-3,36,9,29,'hair');r.box('head',0,119,-19.7,9,10,6,'flesh')
 r.box('head',0,115,-21,26,6,3,'mouth')
 for side in [-1,1]:
  r.box('head',side*10,125,-19,10,3,2,'scar');r.box('head',side*10,123.7,-19.5,5,2.1,1,'eye');r.box('head',side*19,119,-2,4,9,8,'flesh')
  tag='rage_eye_'+str(side+1);r.bone(tag,'head',[side*10,124,-20]);r.box(tag,side*10,123.7,-20.1,5,2.1,.5,'rage')
 for i in range(8):r.box('head',-13+i*3.7,114.5-i%3,-23,2.3,3+i%3,2.5,'bone')
 for side in [-1,1]:r.box('head',side*13.4,109.5,-22,4,9,4,'bone')
 r.bone('jaw','head',[0,116,-6]);r.box('jaw',0,109,-10,29,10,25,'flesh');r.box('jaw',0,116,-13,24,1,17,'mouth')
 for i in range(7):r.box('jaw',-10+i*3.3,118,-23,2.2,3+i%2,2.5,'bone')
 r.box('jaw',0,117,-13,10,1.3,10,'scar')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone(tag+'_thigh','hips',[side*15,50,0]);r.box(tag+'_thigh',side*15,24,0,27,28,25,'flesh')
  r.bone(tag+'_shin',tag+'_thigh',[side*15,27,0]);r.box(tag+'_shin',side*15,7,0,21,24,21,'flesh')
  for i in range(3):r.box(tag+'_shin',side*15,9+i*5,0,21.4,2,21.4,'leather')
  r.bone(tag+'_foot',tag+'_shin',[side*15,9,0]);r.box(tag+'_foot',side*15,0,-3,23,9,28,'flesh')
  for i in range(4):r.box(tag+'_foot',side*15-8+i*5,0,-18,3.5,4,7,'bone')
  r.bone(tag+'_shoulder','chest',[side*37,112,0]);r.box(tag+'_shoulder',side*37,101,0,27,24,31,'flesh' if side<0 else 'wood')
  r.bone(tag+'_arm',tag+'_shoulder',[side*37,101,0]);r.box(tag+'_arm',side*37,73,0,24,34,26,'flesh')
  r.bone(tag+'_forearm',tag+'_arm',[side*37,77,0]);r.box(tag+'_forearm',side*37,52,-2,22,30,27,'flesh')
  for i in range(3):r.box(tag+'_forearm',side*37,55+i*7,-2,22.5,3,27.5,'leather')
  r.bone(tag+'_hand',tag+'_forearm',[side*37,55,-2]);r.box(tag+'_hand',side*37,43,-3,26,18,25,'flesh')
  for i in range(4):
   r.bone(tag+'_finger_'+str(i),tag+'_hand',[side*37-9+i*6,46,-13]);r.box(tag+'_finger_'+str(i),side*37-9+i*6,35+i%2,-13,5,12-i%2,8,'flesh')
   r.box(tag+'_finger_'+str(i),side*37-9+i*6,34+i%2,-15,4,3,5,'bone')
  r.bone(tag+'_thumb',tag+'_hand',[side*50,49,-3]);r.box(tag+'_thumb',side*50,40,-3,7,11,9,'flesh')
 r.bone('rack','chest',[0,106,24])
 for i in range(6):r.box('rack',-23+i*9,103,24+(i%2)*2,4,41-(i%3)*4,5,'wood')
 for yy in [110,127,138]:r.box('rack',0,yy,26,50,4,5,'wood')
 for i in range(4):
  tag='trophy_'+str(i);r.bone(tag,'rack',[-20+i*13,133,31]);r.box(tag,-20+i*13,124,31,7,9,6,'bone');r.box(tag,-20+i*13,127,34.4,5,2,1,'mouth');r.box(tag,-20+i*13,121,31,5,4,5,'bone')
 for i in range(3):
  tag='belt_trophy_'+str(i);r.bone(tag,'hips',[-16+i*16,57,-23]);r.box(tag,-16+i*16,49,-24,8,9,6,'bone');r.box(tag,-16+i*16,52,-27.5,5,2,1,'mouth')
 for i in range(5):r.box('hips',-23+i*11,38+i%2*4,-20,8,15,2,'cloth')
 r.emit()
def merge(path,new):
 data=json.loads(path.read_text(encoding='utf-8-sig')) if path.exists() else {};data.update(new);path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def resources():
 events={};lang={'ru_ru':{},'en_us':{}}
 for who,ru,en in [('ilya_muromets','Илья Муромец','Ilya Muromets'),('idolishche_poganoe','Идолище Поганое','Foul Idolishche')]:
  for p in sorted((S/'05_Звуки'/who).glob('*.ogg')):
   dest=A/'sounds/entity'/who/p.name;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,dest)
   key=who+'.'+p.stem;sub='subtitle.slavicmyths.171.'+key;events[key]={'subtitle':sub,'sounds':[{'name':'slavicmyths:entity/'+who+'/'+p.stem,'attenuation_distance':48 if who.startswith('idol') else 32}]}
   labels={'step':'тяжело ступает','stomp':'топает','mace':'взмахивает палицей','shield':'держит щит','drink':'пьёт отвар','taunt':'бросает вызов','breath':'дышит','hurt':'стонет','pain':'стонет','rage':'ревёт в ярости','eat':'чавкает','grasp':'хватает','throw':'бросает','death':'падает','summon':'вызывает прислужников','roar':'рычит','rush':'разбегается','recover':'восстанавливается','return':'возвращается','retreat':'отступает','unyielding':'собирается с силами'}
   label=next((v for k,v in labels.items() if k in p.stem),'издаёт звук');lang['ru_ru'][sub]=ru+' '+label;lang['en_us'][sub]=en+': '+p.stem.rsplit('_',1)[0].replace('_',' ')
 for p in (S/'07_Локализация/01_Имена_сообщения_субтитры.md').read_text(encoding='utf8').splitlines():
  if p.startswith('| ') and not p.startswith('| Key'):
   parts=[v.strip() for v in p.strip('|').split('|')]
   if len(parts)==3 and '.' in parts[0]:lang['ru_ru'][parts[0]]=parts[1];lang['en_us'][parts[0]]=parts[2]
 extra={'gui.slavicmyths.171.arena_blocked':('Двор требует свободного загруженного пространства 61 × 14 × 61. Блоки не удаляются.','The courtyard needs empty loaded space 61 × 14 × 61. No blocks are removed.'),'gui.slavicmyths.171.arena_building':('Двор строится. Центр: %s %s %s.','Building courtyard. Center: %s %s %s.'),'gui.slavicmyths.171.arena_ready':('Двор готов. Призовите Илью до начала встречи с Идолищем.','Courtyard ready. Call Ilya before starting the Idolishche encounter.'),'gui.slavicmyths.171.combo_hint':('Атакуйте Идолище сбоку!','Attack Idolishche from the side!'),'key.slavicmyths.bogatyr_menu':('Призыв богатыря','Call a bogatyr'),'key.slavicmyths.grab_escape':('Освободиться из хватки','Struggle free'),
 'gui.slavicmyths.171.call':('Призвать Илью','Call Ilya'),'gui.slavicmyths.171.hero_menu':('Богатыри','Bogatyrs'),
 'gui.slavicmyths.171.slot_locked':('Место для будущего богатыря','Reserved hero slot'),
 'gui.slavicmyths.171.already_called':('Илья уже призван. Повторный вызов запрещён.','Ilya is already called. Duplicate calls are forbidden.'),
 'gui.slavicmyths.171.no_space':('Нет безопасного места для богатыря.','No safe space for the bogatyr.'),
 'gui.slavicmyths.171.boss_no_space':('Идолищу нужен свободный двор высотой 13 блоков.','Idolishche needs a clear courtyard 13 blocks high.'),
 'gui.slavicmyths.171.leave_arena':('Вернитесь в круг боя: встреча прекратится через 5 секунд.','Return to the arena: the encounter will reset after 5 seconds.'),
 'dialogue.slavicmyths.171.ilya':('Илья: Ишь, какое чудище землю русскую терзает!','Ilya: What a monster torments this land!'),
 'dialogue.slavicmyths.171.idol':('Идолище: Мал ты против меня, богатырь!','Idolishche: You are small beside me, bogatyr!')}
 for key,(ru,en) in extra.items():lang['ru_ru'][key]=ru;lang['en_us'][key]=en
 for attack in ['fist','stomp','grasp','charge','breath']:
  path=R/'src/main/resources/data/slavicmyths/damage_type'/f'idolishche_{attack}.json';path.write_text(json.dumps({'message_id':'idolishche_'+attack,'scaling':'never','exhaustion':.1})+'\n',encoding='utf8')
  for locale in lang:lang[locale]['death.attack.idolishche_'+attack]='%1$s был сражён Идолищем Поганым' if locale=='ru_ru' else '%1$s was slain by Foul Idolishche'
 merge(A/'sounds.json',events)
 for locale,data in lang.items():merge(A/'lang'/f'{locale}.json',data)
 # Ground-only translucent warning geometry uses a white pixel field, not a weapon sprite.
 path=A/'textures/entity/bogatyr_warning.png';Image.new('RGBA',(32,32),'white').save(path)
 (O/'audio-origin.json').write_text(json.dumps({str(p.relative_to(S)):hashlib.sha256(p.read_bytes()).hexdigest() for p in (S/'05_Звуки').rglob('*.ogg')},indent=2)+'\n')
if __name__=='__main__':
 hero();boss();resources()
 from voice_rework_60 import restore_accepted
 restore_accepted()
