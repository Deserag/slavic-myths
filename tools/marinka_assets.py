"""Authored 1.7.2 rigs: broad silver spear fighter, braided witch, magpie and golden aurochs.
No reference infographic is used as a texture. Only new assets and additive locale/sounds.
"""
from pathlib import Path
import inspect,json,hashlib,shutil,textwrap
from PIL import Image,ImageDraw
import bogatyr_assets as base
R=Path(__file__).resolve().parents[1];S=R/'docs/specs/1.7.2';O=R/'docs/verification/marinka-1.7.2';O.mkdir(parents=True,exist_ok=True)
base.O=O
base.PALETTE.update({'steel':['818890','B5B8B7','565B60'],'white':['E3E3DD','F5F4E9','AFB4BA'],'stubble':['B9BCBB','CDD0CE','AFB4BA'],'eye':['44769E','78A2BC','24262B'],'skin':['DCA88B','E4B398','AD7561'],'red':['7F262D','9A363D','4D1C25'],'gold':['B58B41','D2C9AC','7C5A33'],'black':['21191F','33252E','141117'],'wine':['52222A','7C202C','321921'],'hair':['272124','4B3434','1B181C'],'ruby':['B52A47','F85B59','7E2A35'],'witchskin':['E0B49E','EDC5AF','A97772'],'witcheye':['9B3245','C66B78','272124'],'umber':['3A2920','5B3F2D','271E18'],'beige':['BCAA8D','D4C3A4','8F7C65']})
classes={'dobrynya_nikitich':'DobrynyaMesh','marinka':'MarinkaMesh','dobrynya_aurochs':'AurochsMesh','marinka_magpie':'MagpieMesh','marinka_hex_focus':'HexFocusMesh'}
# Use the established cuboid/UV emitter, with its own class dispatch (never overwrite Ilya).
source=textwrap.dedent(inspect.getsource(base.Rig.emit)).replace("cls='IlyaMesh' if self.name=='ilya_muromets' else 'IdolishcheMesh'","cls=classes[self.name]")
space=dict(base.__dict__,classes=classes);exec(source,space)
class Rig(base.Rig):emit=space['emit']
def box(r,b,x,y,z,w,h,d,m):r.box(b,x,y,z,w,h,d,m)
def hero():
 r=Rig('dobrynya_nikitich');r.bone('hips',None,[0,19.8,0]);box(r,'hips',0,18,0,10.1,3.5,5.8,'steel');box(r,'hips',0,18,-3.1,10.4,1.5,.5,'leather');box(r,'hips',0,18.2,-3.6,2.7,2.4,.8,'gold')
 r.bone('body','hips',[0,23,0]);box(r,'body',0,22,0,13.92,10.24,7.2,'steel')
 for row in range(3):
  for col in range(5):box(r,'body',-5.6+col*2.8,23+row*2.4,-3.75,2.45,1.9,.5,'steel')
 for i in range(7):box(r,'body',-5.8+i*1.7,27.8-i*.8,-4.05,.8,.8,.45,'gold')
 r.bone('neck','body',[0,32.8,0]);box(r,'neck',0,32,0,4,2,4,'red')
 r.bone('head','neck',[0,35,0]);box(r,'head',0,31.58,0,6.88,6.72,6.88,'skin');box(r,'head',0,37.95,.05,7.4,.45,7.3,'white')
 for side in [-1,1]:
  box(r,'head',side*3.5,33.8,0,.8,2.8,6.8,'white');box(r,'head',side*1.9,35,-3.6,1.4,.6,.2,'eye');box(r,'head',side*1.9,35.8,-3.65,1.8,.5,.2,'white');box(r,'head',side*3.5,33,-.1,.8,1.5,1.5,'skin')
 box(r,'head',0,34.2,-3.75,1.1,1.6,.7,'skin');box(r,'head',0,33,-3.5,1.6,.3,.2,'leather')
 for i in range(7):
  r.bone('hair_'+str(i),'head',[-3+i,36,3.6]);box(r,'hair_'+str(i),-3+i,30.2+i%2*.4,3.9,1.2,6.4,1.4,'white')
 for i in range(3):box(r,'head',-2+i*2,36.5+i%2*.35,-3.1,2,1.35,1.3,'white')
 for side,tag in [(-1,'right'),(1,'left')]:
  x=side*3.04;r.bone(tag+'_thigh','hips',[x,19,0]);box(r,tag+'_thigh',x,12.5,0,4.8,6.56,5.28,'steel');r.bone(tag+'_shin',tag+'_thigh',[x,12.5,0]);box(r,tag+'_shin',x,4.16,0,4.32,8.32,4.32,'steel');r.bone(tag+'_foot',tag+'_shin',[x,4.16,0]);box(r,tag+'_foot',x,0,-.32,4.32,4.16,7.36,'leather');box(r,tag+'_foot',x,.8,-2.4,4.5,2.1,3.3,'steel')
  for i in range(3):box(r,tag+'_shin',x,5+i*2.2,-2.45,4.4,.65,.6,'steel')
  x=side*7;r.bone(tag+'_shoulder','body',[x,31,0]);box(r,tag+'_shoulder',x,29,0,3.28,4.32,8.48,'steel');box(r,tag+'_shoulder',x,29,-4.4,3.4,.7,.5,'gold')
  r.bone(tag+'_arm',tag+'_shoulder',[x,29,0]);box(r,tag+'_arm',x,22.5,0,3.68,6.72,4.8,'steel');r.bone(tag+'_hand',tag+'_arm',[x,22.5,0]);box(r,tag+'_hand',x,18,-.8,4,4.48,4.96,'steel');box(r,tag+'_hand',x,17.5,-.85,3.2,1.8,3.3,'skin')
  for i in range(3):box(r,tag+'_arm',x,23+i*1.8,-2.5,3.7,.5,.4,'gold')
 for i in range(3):
  r.bone('cloak_'+str(i),'body',[-3.5+i*3.5,30,4.8]);box(r,'cloak_'+str(i),-3.5+i*3.5,9.3,4.8,3.8,18,1.12,'red');box(r,'cloak_'+str(i),-3.5+i*3.5,10.2,5.45,3.8,.8,.2,'gold')
  for j in range(6):box(r,'cloak_'+str(i),-3.5+i*3.5,15+j*1.5,5.45,1.2,.8,.2,'gold' if j%2 else 'beige')
 r.bone('shield','left_hand',[7,20,-3.2])
 for w,y,h in [(4.2,5,2.1),(6.8,7.1,3.2),(8.32,10.3,6.3),(7,16.6,3),(4.5,19.6,.76)]:
  box(r,'shield',7,y,-3.8,w,h,1.92,'gold');box(r,'shield',7,y+.25,-4.85,w-.8,h-.5,.3,'red')
 for dx,dy in [(0,0),(0,3),(0,-3),(2,0),(-2,0),(1.5,2),(-1.5,2),(1.5,-2),(-1.5,-2)]:box(r,'shield',7+dx,12.8+dy,-5.15,.7,.7,.25,'beige')
 r.bone('spear','right_hand',[-7,20,-1]);box(r,'spear',-7,0,-1,1.6,49.28,1.6,'wood')
 for y in [9,18,29,39]:box(r,'spear',-7,y,-1,1.8,2.6,1.8,'leather')
 for i in range(6):box(r,'spear',-7,49.28+i*1.3866666667,-1,max(.25,4-i*.65),1.3866666667,.9,'steel')
 box(r,'spear',-7,47,-1,2.5,.8,2.5,'gold');box(r,'spear',-5.3,43,-1,1.1,5,.5,'red')
 r.bone('bottle','right_hand',[-7,20,-4]);box(r,'bottle',-7,18,-4,2.8,4,2.8,'green');box(r,'bottle',-7,22,-4,1.2,1.5,1.2,'gold');r.emit()
def face_stubble(r):
 path=base.A/'textures/entity/dobrynya_nikitich.png';im=Image.open(path).convert('RGBA');b=next(b for b in r.boxes if b['bone']=='head' and b['material']=='skin');x,y,_,_=b['uv'];w,h,d=b['size'];x+=round(d);y+=round(d)
 for yy in range(round(h)-2,round(h)):
  for xx in range(round(w)):
   if (xx+yy)%2==0:im.putpixel((x+xx,y+yy),(185,188,187,255))
 im.save(path)
def witch():
 r=Rig('marinka');r.bone('hips',None,[0,17,0]);box(r,'hips',0,13,0,6.5,5,4.5,'black');r.bone('body','hips',[0,22,0]);box(r,'body',0,17,0,7.7,8.2,4.7,'wine')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone(tag+'_thigh','hips',[side*2,13,0]);box(r,tag+'_thigh',side*2,6,0,2.8,7,3,'black');r.bone(tag+'_shin',tag+'_thigh',[side*2,6,0]);box(r,tag+'_shin',side*2,1,0,2.6,5,2.9,'black');r.bone(tag+'_foot',tag+'_shin',[side*2,2,0]);box(r,tag+'_foot',side*2,0,-.4,3.2,3.36,4.8,'leather')
  r.bone(tag+'_shoulder','body',[side*4.5,25.2,0]);box(r,tag+'_shoulder',side*4.5,22,0,3,4.2,5.6,'black');r.bone(tag+'_arm',tag+'_shoulder',[side*4.5,22,0]);box(r,tag+'_arm',side*5,17,0,3.2,5.5,4.2,'wine');r.bone(tag+'_hand',tag+'_arm',[side*5,17,0]);box(r,tag+'_hand',side*5,13,-.6,2.6,4.3,3.3,'witchskin');box(r,tag+'_hand',side*5,14,-.8,2.9,.7,3.6,'gold')
  for i in range(3):box(r,tag+'_arm',side*5+(i-1)*.8,16+i*.5,-2.3,.8,2.2,.4,'beige')
 r.bone('neck','body',[0,26,0]);box(r,'neck',0,25.2,0,3,1.4,3,'witchskin');r.bone('head','neck',[0,29.6,0]);box(r,'head',0,26.4,0,6.1,6.24,6.24,'witchskin');box(r,'head',0,32.3,.2,6.8,1.3,6.7,'hair')
 for side in [-1,1]:box(r,'head',side*1.65,29.9,-3.25,1.1,.5,.2,'witcheye');box(r,'head',side*3.2,27.4,.5,1.2,5.7,5.9,'hair')
 box(r,'head',0,28.4,-3.5,.8,1,.6,'witchskin');box(r,'head',0,27.3,-3.27,1.1,.3,.2,'red');box(r,'head',0,31.7,0,7,.7,6.8,'red');box(r,'head',3.7,30.7,-.6,.9,1.7,.6,'gold')
 r.bone('braid_0','head',[1.8,29,3.6])
 for i in range(6):
  if i:r.bone('braid_'+str(i),'braid_'+str(i-1),[1.8+(i%2)*.25,29-i*2.4,3.6+i*.15])
  box(r,'braid_'+str(i),1.8+(i%2)*.25,26.6-i*2.4,3.6+i*.15,2.2,2.6,2.4,'hair');box(r,'braid_'+str(i),1.8,27.7-i*2.4,5+i*.15,2.2,.4,.4,'red')
 for i in range(8):
  import math
  a=i*math.pi/4;x,z=math.sin(a)*3.9,math.cos(a)*2.8;r.bone('hem_'+str(i),'hips',[x,15,z]);box(r,'hem_'+str(i),x,1.2+i%2*.3,z,4.1,14.4,1.2 if i%2==0 else 2.4,'black' if i%3 else 'wine')
  for j in range(3):box(r,'hem_'+str(i),x+(j-1)*.7,2+j*1.3,z+(.8 if z>=0 else -.8),.5,.8,.3,'gold')
 r.bone('mantle','body',[0,25,3]);box(r,'mantle',0,7,3.8,11,17.9,1.2,'black');box(r,'mantle',0,8,4.5,3.8,14.2,.4,'wine')
 for i in range(7):box(r,'body',-4+i*1.3,16+i%3*.8,-3.0,.6,2.2+i%2,.6,'gold' if i%2 else 'beige')
 box(r,'body',0,13.6,-3.3,1.5,2.7,.7,'beige');box(r,'body',3.3,12,-2.7,1.1,2.1,.8,'gold');r.bone('ember','left_hand',[5,20,-2]);box(r,'ember',5,18.8,-2,2.2,2.2,2.2,'ruby');box(r,'ember',5.5,21,-2.4,1.4,1.5,1.3,'ruby');r.emit()
def bull():
 r=Rig('dobrynya_aurochs');r.bone('body',None,[0,20,0]);box(r,'body',0,11,-.5,18.4,16.2,32,'umber');box(r,'body',0,11,7,18.6,5,20,'beige');r.bone('hump','body',[0,25,-7]);box(r,'hump',0,21,-9,15,6.2,15,'umber')
 r.bone('head','body',[0,21,-16]);box(r,'head',0,13.5,-19,12,10,10,'umber');box(r,'head',0,13,-25,10,5,7,'beige');box(r,'head',0,14,-29,9,2.8,1,'leather')
 for side in [-1,1]:
  box(r,'head',side*5.5,20.5,-22,1.2,1.1,.5,'eye');r.bone(('right' if side<0 else 'left')+'_horn','head',[side*5.5,23,-20])
  name=('right' if side<0 else 'left')+'_horn'
  for i in range(4):box(r,name,side*(6.6+i*1.6),22.5+i*.9,-20+i*.1,2.3-i*.3,2.8-i*.3,2.4-i*.35,'gold')
 for side,tag in [(-1,'right'),(1,'left')]:
  for pair,z in [('front',-11),('back',11)]:
   key=tag+'_'+pair;r.bone(key,'body',[side*6.2,15,z]);box(r,key,side*6.2,3,z,4.5,12,5,'umber');r.bone(key+'_hoof',key,[side*6.2,3,z]);box(r,key+'_hoof',side*6.2,0,z-.5,5.2,3,5.8,'leather')
 r.bone('tail','body',[0,20,17]);box(r,'tail',0,9,18,1.5,11,1.5,'umber');box(r,'tail',0,7,18,3,3.5,2.6,'black');r.emit()
def minion_texture():
 im=Image.open(base.A/'textures/entity/dobrynya_aurochs.png').convert('RGBA')
 rig=json.loads((O/'dobrynya_aurochs-rig.json').read_text(encoding='utf8'))
 colours=[tuple(bytes.fromhex(v))+(255,) for v in base.PALETTE['beige']]
 for box in rig['boxes']:
  if 'horn' not in box['bone']:continue
  x,y,w,h=box['uv']
  for yy in range(y,y+h):
   for xx in range(x,x+w):
    if im.getpixel((xx,yy))[3]:im.putpixel((xx,yy),colours[(xx+yy*3)%3])
 im.save(base.A/'textures/entity/marinka_tur_minion.png')

def bird():
 r=Rig('marinka_magpie');r.bone('body',None,[0,6,0]);box(r,'body',0,3,-.5,4.6,4.2,7.2,'black');box(r,'body',0,3.5,-.5,3.2,2,6,'white');r.bone('head','body',[0,7,-4]);box(r,'head',0,5.7,-4,3.8,3.8,4,'black');box(r,'head',0,6.2,-6.6,1.4,1.2,2.6,'leather')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone(tag+'_wing','body',[side*2.1,6,.3]);box(r,tag+'_wing',side*3.4,5,.3,3.1,1.1,5.4,'white');r.bone(tag+'_tip',tag+'_wing',[side*4.8,5,.8]);box(r,tag+'_tip',side*6.1,4.7,1.5,2.2,.8,6.2,'black')
  for i in range(3):box(r,tag+'_tip',side*(5+i*.7),4.6,4.2+i*.7,.65,.6,2.8,'black')
 r.bone('tail','body',[0,5,3]);
 for i in range(4):box(r,'tail',-1.1+i*.75,4.5,7.1+i%2*.4,.65,.65,8.8,'black')
 r.emit()
def focus():
 r=Rig('marinka_hex_focus');r.bone('body',None,[0,10,0]);box(r,'body',0,0,0,12,3,12,'steel');box(r,'body',0,8,0,3.5,10,3.5,'ruby')
 for i in range(3):r.bone('ring_'+str(i),'body',[0,8+i*6,0]);
 for i in range(3):
  for dx,dz in [(0,5),(5,0),(0,-5),(-5,0)]:box(r,'ring_'+str(i),dx,8+i*6,dz,2.2,2.2,2.2,'gold')
 box(r,'body',0,24,0,3,4.8,3,'ruby');r.emit()
def resources():
 a=base.A
 for group in ['dobrynya','marinka','arena']:
  for p in (S/'06_Audio'/group).glob('*.ogg'):
   dest=a/'sounds'/group/p.name;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,dest)
 base.merge(a/'sounds.json',json.loads((S/'08_Legal_and_Resources/sounds_json_TEMPLATE.json').read_text(encoding='utf8')))
 pairs={}
 text=(S/'09_Localization/01_RU_EN_STRINGS_AND_LORE.md').read_text(encoding='utf8')
 for line in text.splitlines():
  if not line.startswith('| ') or line.startswith('| Suggested') or line.startswith('|---'):continue
  parts=[v.strip() for v in line.strip('|').split('|')];key=parts[0]
  if key.startswith(('entity.','block.','structure.','zone.')):first,rest=key.split('.',1);key=first+'.slavicmyths.'+rest
  elif key.startswith(('msg.','subtitle.')):first,rest=key.split('.',1);key=first+'.slavicmyths.'+rest
  pairs[key]=(parts[1],parts[2])
 pairs.update({'gui.slavicmyths.172.call_dobrynya':('Призвать Добрыню','Call Dobrynya'),'msg.slavicmyths.dobrynya_duplicate':('Добрыня уже призван.','Dobrynya is already called.'),'msg.slavicmyths.yard_building':('Подворье строится. Центр: %s %s %s.','Building witch yard. Center: %s %s %s.'),'msg.slavicmyths.yard_invalid':('Нужен сухой свободный загруженный участок 60 × 60. Существующие строения сохраняются.','A dry, clear, loaded 60 × 60 site is required. Existing buildings are preserved.'),'dialogue.slavicmyths.172.marinka_intro':('Добрыня Никитич... След твой мне знаком.','Dobrynya Nikitich... I know your footsteps.'),'entity.slavicmyths.marinka_hex_focus':('Сердце заклятия','Hex Anchor'),'entity.slavicmyths.marinka_bolt':('Ведьмин огонь','Witchfire'),'entity.slavicmyths.marinka_rune':('Колдовской след','Hexed Footprint')})
 import re
 for folder,who in [('01_Dobrynya','dobrynya'),('02_Marinka','marinka')]:
  script=next((S/folder).glob('04*.md')).read_text(encoding='utf8')
  lines=re.findall(r'RU[^«\n]*«([^»]+)»[^\n]*EN[ \t]*[“\"]([^”\"\n]+)[”\"]',script)
  assert len(lines)==(10 if who=='dobrynya' else 9),(who,len(lines))
  for i,(ru,en) in enumerate(lines,1):pairs[f'dialogue.slavicmyths.172.{who}_{i}']=(ru,en)
 pairs.update({
 'lore.slavicmyths.172.note_1':('Кто вступает на землю подворья, оставляет след. А след, верно взятый, может стать цепью.','Whoever enters the yard leaves a footprint. A footprint, properly taken, can become a chain.'),
 'lore.slavicmyths.172.note_2':('Золоторогий тур хранит волю в своём сердце. Сломи печать, и чары падут.','The golden-horned aurochs holds his will within his heart. Break the seal, and the spell will fall.'),
 'lore.slavicmyths.172.note_3':('Страх не охраняет тайны. Тайны хранит терпение.','Fear does not guard secrets. Patience guards secrets.'),
 'lore.slavicmyths.172.note_4':('Следы у нас под ногами, но воля — при нас.','Our footprints lie beneath our feet, but our will remains our own.')})
 for attack in ['fire','trace','lure','rage']:
  pairs['death.attack.marinka_'+attack]=('%s погиб от чар Маринки','%s was slain by Marinka’s sorcery')
 for locale,i in [('ru_ru',0),('en_us',1)]:
  data=json.loads((S/'08_Legal_and_Resources'/f'sound_subtitles_{locale}_TEMPLATE.json').read_text(encoding='utf8'));data.update({k:v[i] for k,v in pairs.items()});base.merge(a/'lang'/f'{locale}.json',data)
 for attack in ['fire','trace','lure','rage']:
  path=R/'src/main/resources/data/slavicmyths/damage_type'/f'marinka_{attack}.json';path.write_text(json.dumps({'message_id':'marinka_'+attack,'scaling':'never','exhaustion':.1})+'\n')
 # Vanilla atlas references for the new solid rune seal; a real block model/loot/item.
 base.merge(a/'blockstates/witch_yard_altar.json',{'variants':{'':{'model':'slavicmyths:block/witch_yard_altar'}}})
 path=a/'models/block/witch_yard_altar.json';path.write_text(json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/witch_yard_altar'}})+'\n')
 path=a/'models/item/witch_yard_altar.json';path.write_text(json.dumps({'parent':'slavicmyths:block/witch_yard_altar'})+'\n')
 path=R/'src/main/resources/data/slavicmyths/loot_table/blocks/witch_yard_altar.json';path.write_text(json.dumps({'type':'minecraft:block','pools':[]})+'\n')
 path=a/'textures/block/witch_yard_altar.png';im=Image.new('RGBA',(32,32),(75,64,64,255));d=ImageDraw.Draw(im);d.rectangle((3,3,28,28),outline='#7E2A35',width=2);d.polygon([(16,5),(26,16),(16,26),(5,16)],outline='#C49B57');d.line([(16,8),(16,24)],fill='#CC5D73',width=2);im.save(path)
 path=a/'textures/entity/marinka_magic.png';im=Image.new('RGBA',(64,64),(255,255,255,255));im.save(path)
 (O/'audio-origin.json').write_text(json.dumps({str(p.relative_to(S)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in (S/'06_Audio').rglob('*.ogg')},indent=2)+'\n')
if __name__=='__main__':hero();witch();bull();minion_texture();bird();focus();resources()

if __name__ == "__main__":
 from feedback_resource_policy import apply as apply_feedback_policy
 apply_feedback_policy()
