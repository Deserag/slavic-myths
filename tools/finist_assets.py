"""Authored 1.7.4 rigs. Straight slim 23px steel/gold sword; 39px carved dark eye staff.
Approved sprites/OGGs copied verbatim; reusable UV emitter, no reused character geometry."""
from pathlib import Path
from PIL import Image
import inspect,textwrap,json,shutil,hashlib
import bogatyr_assets as base
R=Path(__file__).resolve().parents[1];S=R/'docs/specs/1.7.4';O=R/'docs/verification/finist-1.7.4';O.mkdir(parents=True,exist_ok=True);A=R/'src/main/resources/assets/slavicmyths';base.O=O
base.PALETTE.update({'sand':('D1AF73','8A6745','E4CA96'),'cream':('E5E0D1','C4BEAE','F2ECE0'),'navy':('243F6A','182A49','38527C'),'iris':('357FC0','254F83','79B7E1'),'skin':('E6BC96','B68464','F2CDA8'),'gold':('D0A64E','967537','EACB67'),'chain':('B6B8B4','606772','D2D4D1'),'crimson':('8F3432','592327','AF4742'),'black':('18181D','2B252A','3A303A'),'iron':('646269','414148','929095'),'rust':('B94C24','873920','DA6735'),'pallid':('B8A28C','806F60','CCB9A5'),'violet':('6C2B9A','432064','9C5CCC'),'lens':('D62D3B','841F2B','EF5E59'),'slate':('716557','483F37','998875'),'amber':('D2A148','493425','E6C16C')})
classes={'finist':'FinistMesh','kartaus':'KartausMesh','finist_falcon':'FalconMesh','kartaus_banner':'BlackBannerMesh','kartaus_curse_anchor':'CurseAnchorMesh'}
s=textwrap.dedent(inspect.getsource(base.Rig.emit)).replace("cls='IlyaMesh' if self.name=='ilya_muromets' else 'IdolishcheMesh'","cls=classes[self.name]").replace('size=256;',"size=128 if self.name=='finist_falcon' else 256;")
s=s.replace("'height':40 if self.scale==1 else 144","'height':max(b['center'][1]+b['size'][1]/2 for b in self.boxes)")
space=dict(base.__dict__,classes=classes);exec(s,space)
class Rig(base.Rig):emit=space['emit']
def b(r,bone,x,y,z,w,h,d,m):r.box(bone,x,y,z,w,h,d,m)
def humanoid(boss=False):
 name='kartaus' if boss else 'finist';r=Rig(name);hips=18 if boss else 15;top=43.2 if boss else 36
 r.bone('hips',None,[0,hips,0]);r.bone('body','hips',[0,hips+5,0]);b(r,'body',0,hips,0,13 if boss else 10.6,17 if boss else 13,8 if boss else 6.8,'black' if boss else 'chain');b(r,'hips',0,hips-1,-.2,12 if boss else 10,3,8 if boss else 7,'leather');b(r,'hips',0,hips,-4.5,2.5,2,.6,'gold')
 for side,tag in [(-1,'right'),(1,'left')]:
  x=side*(3.2 if boss else 2.9);r.bone(tag+'_leg','hips',[x,hips-1,0]);b(r,tag+'_leg',x,4,0,5,hips-4,5,'black' if boss else 'leather');r.bone(tag+'_boot',tag+'_leg',[x,5,0]);b(r,tag+'_boot',x,0,-.8,5.5,5.5,6.6,'black' if boss else 'wood');b(r,tag+'_boot',x,4.5,-.7,5.8,1.1,6.7,'iron' if boss else 'navy')
  x=side*(8 if boss else 7);shoulder=32 if boss else 28;r.bone(tag+'_arm','body',[x,shoulder,0]);b(r,tag+'_arm',x,shoulder-6,0,5 if boss else 4,7,5,'black' if boss else 'cream');b(r,tag+'_arm',x,shoulder-2,0,7 if boss else 4.8,3.6 if boss else 2.5,8 if boss else 5.5,'iron' if boss else 'navy');r.bone(tag+'_hand',tag+'_arm',[x,shoulder-6,0]);b(r,tag+'_hand',x,shoulder-13,0,4 if boss else 3.6,7,4.4,'iron' if boss else 'leather');b(r,tag+'_hand',x,shoulder-14,-3 if boss else 0,3.8,3,4,'pallid' if boss else 'skin')
  for i in range(3):b(r,tag+'_hand',x,shoulder-10+i*1.7,-2.35,4,.5,.35,'rust' if boss else 'crimson')
  if not boss and tag=='left':
   for i in range(5):b(r,tag+'_arm',x-1.4+i*.6,shoulder-i*.4,-3,1.5,.7,.5,'gold')
  if boss:
   for i in range(3):b(r,tag+'_arm',x,shoulder-2+i*1.1,-4.15,7-i*.7,.6,.45,'iron')
  r.bone(tag+'_skirt','hips',[side*3.6,hips,0]);b(r,tag+'_skirt',side*3.6,5 if boss else 8,-.1,4.7,hips-5 if boss else 7,7.4 if boss else 6.8,'black' if boss else 'cream');b(r,tag+'_skirt',side*3.6,6 if boss else 8.8,-4.05 if boss else -3.65,4.8,.7,.3,'rust' if boss else 'crimson')
 r.bone('front_panel','hips',[0,hips,-4.3]);b(r,'front_panel',0,4 if boss else 8,-4.3,3.6,hips-4 if boss else 7,.8,'crimson' if boss else 'cream')
 for i in range(5):b(r,'front_panel',(-1 if i%2 else 1)*.8,7+i*(2 if boss else 1),-4.85,.8,.9,.3,'rust' if boss else 'crimson')
 # Approved embroidered collar / robe trim and pendant.
 b(r,'body',0,hips+9,-4.25 if boss else -3.7,2.3,5,.7,'crimson');r.bone('pendant','hips',[4.2,hips+1,-4.5]);b(r,'pendant',4.2,hips-4,-4.6,2.7,3.5,.5,'gold');b(r,'pendant',4.2,hips-3,-5,1,1.8,.3,'black' if boss else 'navy')
 for i in range(6):b(r,'body',-4+i*1.5,hips+12-i*1.1,-4.3,1.7,1.2,.65,'gold' if i%2==0 else 'leather')
 r.bone('head','body',[0,35 if boss else 31,0]);b(r,'head',0,31 if boss else 27,-.1,10 if boss else 8.8,8 if boss else 8.2,9 if boss else 8.4,'pallid' if boss else 'skin');b(r,'head',0,34 if boss else 30,4.4,10 if boss else 9.4,5 if boss else 4.3,1.7,'iron' if boss else 'sand')
 for side in [-1,1]:
  b(r,'head',side*2.45,35 if boss else 30.6,-4.8 if boss else -4.5,1.8,1.3,.5,'lens' if boss else 'iris');b(r,'head',side*2.45,36.4 if boss else 32,-4.75 if boss else -4.5,2.6,.6,.55,'rust' if boss else 'sand')
 b(r,'head',0,33 if boss else 29.2,-5.1 if boss else -4.8,1.1,1.6,.9,'pallid' if boss else 'skin');b(r,'head',0,31.7 if boss else 28.3,-4.8 if boss else -4.5,2,.4,.3,'leather')
 if boss:
  for side in [-1,1]:
   for n in range(5):b(r,'head',side*(.8+n*1.45),33-n*.48,-5.6+n*.17,2,1.7,.95,'rust')
  r.bone('crown','head',[0,39.2,0]);b(r,'crown',0,38.5,0,11,2.2,10.5,'black');b(r,'crown',0,39,-5.5,10,.7,.4,'rust')
  for n,(x,z,h) in enumerate([(-4,-4,3),(0,-4,4.7),(4,-4,3),(-4,4,3.5),(4,4,3)]):b(r,'crown',x,40,z,1.5,min(h,top-40),1.7,'iron' if n==1 else 'black')
 else:
  b(r,'head',0,34.8,0,9.5,1.2,9,'sand')
  for n in range(10):b(r,'head',-4+n*.9,32.7+(n%3)*.6,-4.5,1.7,min(2.2,top-(32.7+(n%3)*.6)),1.8,'sand')
  for side in [-1,1]:
   for n in range(4):b(r,'head',side*4.5,30+n*.7,2-n*1.7,1.5,2.4,2.6,'sand')
 for n in range(3):
  parent='body' if n==0 else 'cape_'+str(n-1);y=33 if boss else 29;r.bone('cape_'+str(n),parent,[0,y-n*8,5+n*.35]);b(r,'cape_'+str(n),0,y-(n+1)*8,5.2+n*.35,13.2 if boss else 12.5,8.3,1,'black' if boss else 'navy');b(r,'cape_'+str(n),0,y-(n+1)*8,5.8+n*.35,13,.7,.4,'crimson' if boss else 'gold')
 # Separate gold falcon heraldry and crimson eye/cross commander emblem.
 if boss:
  for i in range(9):b(r,'cape_1',0,12+i,6.7,1.1,.8,.35,'crimson')
  for side in [-1,1]:
   for n in range(4):b(r,'cape_1',side*(1+n),17+(1 if n%2 else 0),6.7,1.3,.8,.35,'crimson')
  b(r,'cape_1',0,17,7.1,2.8,2,.4,'crimson');b(r,'cape_1',0,17.5,7.5,.7,1,.3,'black')
  for side in [-1,1]:
   b(r,'body',side*5.7,19,-4.7,1.1,13,.6,'crimson')
   for n in range(4):b(r,'body',side*(2+n*.8),24+n*1.6,-4.8,1.8,.8,.5,'iron')
  b(r,'body',0,26,-5.3,4.2,3.4,.6,'bone');b(r,'body',-1,27,-5.7,.8,.7,.3,'black');b(r,'body',1,27,-5.7,.8,.7,.3,'black')
 else:
  for side in [-1,1]:
   for n in range(5):b(r,'cape_0' if n<3 else 'cape_1',side*(1+n*.8),21+n*.8,6.3 if n<3 else 6.6,1.5,.8,.35,'gold')
  b(r,'cape_1',0,18,6.6,1.6,6,.4,'gold');b(r,'cape_1',0,22,6.6,3,1.2,.4,'gold')
  for side in [-1,1]:
   for n in range(4):b(r,'front_panel',side*.7,9.2+n*1.2,-4.95,.7,.7,.3,'crimson');b(r,('left' if side>0 else 'right')+'_skirt',side*(2+n*.8),9.2+(n%2)*.65,-3.8,.65,.65,.35,'crimson')
 x=-8 if boss else -7;y=18 if boss else 16;r.bone('weapon','right_hand',[x,y,-1])
 if boss:
  b(r,'weapon',x,0,-1,1.9,34,1.9,'wood')
  for n in range(7):b(r,'weapon',x,2+n*4.2,-1,2.4,.7,2.4,'iron')
  for side in [-1,1]:b(r,'weapon',x+side*3,32,-1,1.5,7,4,'iron');b(r,'weapon',x+side*2,38,-1,2,1.5,4,'black')
  b(r,'weapon',x,32.5,-1,4.2,4.2,3,'lens');b(r,'weapon',x,34,-2.7,1.3,1.8,.45,'black');b(r,'weapon',x,30.5,-1,7,1.5,5,'iron')
 else:
  b(r,'weapon',x,12.5,-1,1.5,4,1.6,'navy');b(r,'weapon',x,12,-1,6,1.3,2,'gold');b(r,'weapon',x,16.4,-1,2,1,2,'gold');b(r,'weapon',x,-9,-1,2,21,.8,'steel');b(r,'weapon',x,-11,-1,1,2,.7,'steel');b(r,'weapon',x,2,-1.5,.7,7,.4,'navy')
 if boss:
  for box in r.bones['weapon']['boxes']:box['center'][2]-=5
 r.bone('flask','left_hand',[7,15,-1]);b(r,'flask',7,13,-1,2.2,3.5,2.2,'green');b(r,'flask',7,16.5,-1,1,1,1,'gold')
 r.emit()
def falcon():
 r=Rig('finist_falcon');r.bone('body',None,[0,6,0]);b(r,'body',0,3,0,5,7,5,'cream');b(r,'body',0,5,2.5,5.5,5.5,1.5,'slate');r.bone('head','body',[0,10,-2]);b(r,'head',0,9,-2,5,5,5,'slate');b(r,'head',0,9,-4.5,4,3,.5,'cream');b(r,'head',0,9.5,-6,1.5,1.5,3,'gold');b(r,'head',0,8.5,-6.8,1,1,1,'black')
 for side,tag in [(-1,'right'),(1,'left')]:
  b(r,'head',side*2,11.3,-4.6,1.2,1,.3,'amber');r.bone(tag+'_wing','body',[side*2.5,8,0]);b(r,tag+'_wing',side*6.5,7,0,8,1.7,5,'slate');r.bone(tag+'_tips',tag+'_wing',[side*9,7,0]);
  for n in range(3):b(r,tag+'_tips',side*(10+n*.5),6.8,-1+n*1.6,4,1,1.7,'cream' if n==0 else 'slate')
  r.bone(tag+'_leg','body',[side*1.6,4,0]);b(r,tag+'_leg',side*1.6,1.5,0,1,3,1,'gold');b(r,tag+'_leg',side*1.6,0,-1,1.8,1.6,2,'black')
 r.bone('tail','body',[0,4,2]);b(r,'tail',0,3,5.3,4.8,.8,7,'slate');b(r,'body',0,8.1,-2.8,5.3,.7,.6,'gold');r.emit()
def props(anchor=False):
 name='kartaus_curse_anchor' if anchor else 'kartaus_banner';r=Rig(name);r.bone('body',None,[0,12,0]);
 if anchor:
  b(r,'body',0,2,0,7,11,2,'black');b(r,'body',0,6,-1.2,4.5,4.5,.6,'violet');b(r,'body',0,7,-1.7,1,2,.5,'lens')
 else:
  b(r,'body',0,0,0,1.5,31,1.5,'wood');b(r,'body',0,28,0,13,1.4,1.4,'iron');r.bone('cloth','body',[0,27,0]);b(r,'cloth',0,6,.9,11,21,.5,'black');b(r,'cloth',0,9,.4,1.1,16,.4,'crimson');b(r,'cloth',0,16,.3,7,.9,.4,'crimson')
  for side in [-1,1]:b(r,'cloth',side*2.6,13,.3,1,7,.4,'crimson')
 r.emit()
humanoid();humanoid(True);falcon();props();props(True)
# Same bird family geometry, distinct transformed markings / sky-blue iris.
img=Image.open(A/'textures/entity/finist_falcon.png').convert('RGBA');rig=json.loads((O/'finist_falcon-rig.json').read_text())
for box in rig['boxes']:
 if box['material']=='amber' or box['bone']=='tail':
  x,y,w,h=box['uv']
  for yy in range(y,y+h):
   for xx in range(x,x+w):img.putpixel((xx,yy),tuple(bytes.fromhex('357FC0' if box['material']=='amber' else 'D0A64E' if (xx+yy)%4==0 else '243F6A'))+(255,))
img.save(A/'textures/entity/finist_morph_falcon.png')
# Exact approved supplied assets; no new crafts or rewards invented.
for name in ['finist_call_feather','kartaus_black_seal','kartaus_spawn_egg']:
 shutil.copy2(S/'06_References/Technical/Item_Sprites'/f'{name}.png',A/'textures/item'/f'{name}.png')
 model={'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}}
 if not name.endswith('egg'):model['display']={'firstperson_righthand':{'rotation':[0,-15,-20] if 'feather' in name else [0,15,0],'translation':[1,3,1],'scale':[.7,.7,.7]}}
 (A/'models/item'/f'{name}.json').write_text(json.dumps(model,indent=2)+'\n')
fragment=json.loads((S/'07_Audio/Integration_Examples/sounds_174_fragment.json').read_text());sounds=json.loads((A/'sounds.json').read_text(encoding='utf8'));sounds.update(fragment);(A/'sounds.json').write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
manifest=json.loads((S/'07_Audio/MANIFEST_RIGHTS_AUDIO.json').read_text());provenance=[]
for e in manifest['files']:
 src=S/e['path'];sub=Path(e['path']).parts[1:];dest=A/'sounds'/Path(*[x.lower() for x in sub]);dest.parent.mkdir(parents=True,exist_ok=True);assert hashlib.sha256(src.read_bytes()).hexdigest()==e['sha256'];shutil.copy2(src,dest);provenance.append(dict(path=dest.relative_to(R).as_posix(),sha256=e['sha256']))
(O/'audio-provenance.json').write_text(json.dumps(provenance,indent=2)+'\n')
translations={}
for line in (S/'08_Localization/01_TRANSLATIONS_RU_EN.md').read_text(encoding='utf8').splitlines():
 if line.startswith('| ') and not line.startswith('| Key'):
  cells=[x.strip() for x in line.split('|')[1:-1]]
  if len(cells)==3 and cells[0].startswith(('entity.','item.','message.','tooltip.','subtitle.')):translations[cells[0]]=cells[1:]
translations.update({'entity.slavicmyths.kartaus_banner':['Чёрное знамя','Black Banner'],'entity.slavicmyths.kartaus_curse_anchor':['Печать оков','Curse Anchor'],'message.slavicmyths.event_return':['Вернитесь к месту испытания','Return to the challenge site'],'gui.slavicmyths.174.recovery':['Восстановление','Recovering'],'gui.slavicmyths.174.anchor':['Печать: %s / 12','Seal: %s / 12']})
for k,v in fragment.items():
 slug=k.split('.')[-1];subtitle=v['subtitle'];translations.setdefault(subtitle,[{'finist':'Финист','falcon':'Сокол','kartaus':'Картаус'}.get(k.split('.')[1],'Испытание')+': '+slug.replace('_',' '),slug.replace('_',' ').capitalize()])
# Specific effect descriptions, never subtitles pretending absent voiced dialogue.
ru={'summon_chime':'Звон призыва','mark_cast':'Знак слабости','wing_dash':'Свист крылатого рывка','inspire':'Звон воодушевления','transform':'Шум обращения','banner_break':'Знамя падает','hurt_1':'Звук ранения','hurt_2':'Звук ранения','hurt_3':'Звук ранения','retreat':'Шаги отступления','tonic':'Глоток настойки','victory':'Звон победы','sword_swing_1':'Взмах меча','sword_swing_2':'Взмах меча','sword_contact':'Удар меча','run_step_1':'Лёгкие шаги','run_step_2':'Лёгкие шаги','scout_call':'Сокол зовёт','wing_flap_1':'Взмах крыльев','wing_flap_2':'Взмах крыльев','dive_start':'Сокол пикирует','dive_impact':'Удар сокола','intercept':'Крылья перехватывают удар','chained':'Звон оков сокола','chain_break':'Цепи разрываются','perch':'Сокол садится','morph_flight':'Шум полёта','ambient_call':'Крик сокола','intro':'Гул появления Картауса','banner_raise':'Знамя поднимается','summon_guard':'Рог чёрной дружины','staff_wave':'Посох выпускает волну','staff_melee':'Взмах посоха','chain_bind':'Звон тёмных цепей','black_eye_charge':'Чёрное око заряжается','black_eye_impact':'Удар чёрного ока','curse_falcon':'Оковы сжимают сокола','barrier_break':'Защита рушится','rage':'Гул гнева','death':'Картаус падает','heavy_step_1':'Тяжёлые шаги','heavy_step_2':'Тяжёлые шаги','staff_ground':'Посох ударяет о землю','finist_call':'Перо звенит','kartaus_seal':'Печать гудит','kartaus_egg':'Шорох призыва','runic_emerge':'Руны возникают','black_banner_die':'Знамя распадается','curse_anchor_die':'Печать разрушается','victory_clear':'Звон победы','ambient_wind':'Ветер испытания'}
for k,v in fragment.items():translations[v['subtitle']]=[ru[k.split('.')[-1]],k.split('.')[-1].replace('_',' ').capitalize()]
for index,lang in enumerate(['ru_ru','en_us']):
 p=A/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf8'));data.update({k:v[index] for k,v in translations.items()});p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
for name in ['wave','melee','eye','chains']:
 p=R/'src/main/resources/data/slavicmyths/damage_type'/f'kartaus_{name}.json';p.write_text(json.dumps({'message_id':'kartaus_'+name,'exhaustion':.1,'scaling':'when_caused_by_living_non_player'},indent=2)+'\n')
 for index,lang in enumerate(['ru_ru','en_us']):
  p=A/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf8'));data['death.attack.kartaus_'+name]=['%s повержен магией Картауса','%s was defeated by Kartaus'][index];p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('Approved sprites and',len(provenance),'original audio files installed')

if __name__ == "__main__":
 from feedback_resource_policy import apply as apply_feedback_policy
 apply_feedback_policy()
