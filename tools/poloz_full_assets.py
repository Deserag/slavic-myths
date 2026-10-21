"""Deterministic full-TZ reward geometry, pixel art and organic glottal horn audio.
Own synthesis only: irregular airflow, glottal cycles and vocal formants; no samples.
Run AFTER legacy poloz_assets.py to reproduce the final full-TZ assets.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,random,wave,array,subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths'
def write(p,data):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def main():
 gold='#C5A15C';dark='#25232A';red='#C72C3E';bone='#DAC6A1'
 names=['poloz_helmet','poloz_chestplate','poloz_leggings','poloz_boots','poloz_whip','rune_earth_lord_iv','poloz_head']
 for name in names:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
  if name=='poloz_whip':
   pts=[(5,28),(8,23),(10,18),(15,15),(22,14),(27,10),(26,5),(20,3),(13,5),(11,9),(14,11),(19,10)]
   d.line(pts,fill=dark,width=4)
   for x,y in pts[1:]:d.rectangle((x-1,y-1,x+1,y+1),fill=gold);d.point((x,y),fill=red)
   d.line((3,30,8,23),fill=bone,width=3);d.polygon([(17,9),(24,7),(22,12)],fill=gold)
  elif name=='rune_earth_lord_iv':
   d.polygon([(16,2),(29,10),(27,24),(16,30),(3,24),(2,10)],fill=dark,outline=gold,width=2);d.line([(6,8),(10,12),(7,18),(11,26)],fill=red,width=2);d.line([(26,10),(22,16),(24,24)],fill=red,width=2);d.polygon([(16,5),(21,13),(16,25),(11,13)],outline=gold,width=2);d.line((16,5,16,27),fill=gold,width=2)
  elif name=='poloz_head':
   d.polygon([(4,9),(12,5),(25,7),(28,17),(22,25),(6,23),(3,16)],fill=dark,outline=gold,width=2)
   for x in [8,15,23]:d.polygon([(x-2,9),(x,0),(x+3,10)],fill=bone)
   d.rectangle((8,12,12,14),fill=red);d.rectangle((21,12,24,14),fill=red);d.rectangle((8,20,23,22),fill=bone)
  elif name=='poloz_helmet':
   d.polygon([(5,12),(10,7),(23,7),(27,13),(26,26),(20,29),(20,19),(12,19),(12,29),(5,25)],fill=dark,outline=gold,width=2)
   for x in [6,16,26]:d.polygon([(x-2,12),(x,1),(x+3,12)],fill=gold)
   d.line((9,16,13,16),fill=red,width=2);d.line((19,16,23,16),fill=red,width=2)
  elif name=='poloz_chestplate':
   d.polygon([(2,8),(11,5),(14,10),(18,10),(21,5),(30,8),(28,16),(23,15),(23,29),(9,29),(9,15),(4,16)],fill=dark,outline=gold,width=2)
   for x in [4,8,24,28]:d.polygon([(x-2,9),(x,1),(x+2,9)],fill=gold)
   d.line([(11,13),(16,17),(21,13),(16,17),(16,26)],fill=red,width=3)
  elif name=='poloz_leggings':
   d.polygon([(6,3),(26,3),(25,29),(19,29),(18,16),(14,16),(13,29),(7,29)],fill=dark,outline=gold,width=2)
   for x in [9,22]:
    for y in [9,16,23]:d.line((x-2,y,x+2,y+1),fill=gold,width=2)
    d.line((x,6,x,25),fill=red)
  else:
   for x in [5,19]:d.polygon([(x,9),(x+8,9),(x+8,22),(x+11,26),(x+10,29),(x-1,29)],fill=dark,outline=gold,width=2);d.line((x+2,14,x+6,17,x+3,24),fill=red,width=2)
  path=A/f'textures/item/{name}.png';path.parent.mkdir(parents=True,exist_ok=True)
  from boss_runes103 import write_owned
  if not write_owned(path):im.save(path)
  write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
 # UV swatches for layered armor geometry, gold horns and a red chest core.
 im=Image.new('RGBA',(64,64),dark);d=ImageDraw.Draw(im);rng=random.Random('armor')
 for y in range(64):
  for x in range(48):
   c=rng.choice(['#25232A','#302C31','#3B3031','#584833']);d.point((x,y),fill=c)
 for y in range(0,32,4):d.line((0,y,47,y),fill=gold);d.line((8,y+1,40,y+1),fill='#80683E')
 d.rectangle((48,32,63,47),fill=bone);d.rectangle((48,48,63,63),fill=red)
 (A/'textures/models/armor').mkdir(parents=True,exist_ok=True);im.save(A/'textures/models/armor/poloz.png')
 atlas=Image.new('RGBA',(16,16),dark);d=ImageDraw.Draw(atlas);d.rectangle((0,0,7,7),fill=gold);d.rectangle((8,0,15,7),fill=bone);d.rectangle((8,8,15,15),fill=red);atlas.save(A/'textures/item/poloz_whip_segments.png')
 # Every pose has a grip, connected individual plates, hinge rods and a spiked tip.
 for pose in range(5):
  elements=[]
  def box(x,y,z,w,h,depth,uv):
   elements.append({'from':[x,y,z],'to':[x+w,y+h,z+depth],'faces':{f:{'texture':'#lash','uv':uv} for f in ['up','down','north','south','east','west']}})
  box(7,-4,7,2,9,2,[8,0,12,4]);box(6.5,3,6.5,3,1,3,[0,0,4,4])
  last=(8,5)
  for i in range(10):
   angle=(-.5+i*.2) if pose==0 else (-1.2+pose*.2+i*(.11 if pose<4 else .22))
   x,y=last[0]+math.sin(angle)*2,last[1]+math.cos(angle)*2
   box((last[0]+x)/2-.4,(last[1]+y)/2-.4,7.6,.8,.8,.8,[8,0,12,4]);box(x-1,y-.6,6.8,2,1.4,2.4,[0,0,4,4]);box(x-.3,y,6.6,.6,.7,.3,[8,8,12,12]);last=x,y
  x,y=last;box(x-1.5,y,6.5,3,2,3,[0,0,4,4]);box(x-.4,y+2,7.6,.8,2,.8,[8,0,12,4]);box(x-2.5,y+.5,7.6,1,1,.8,[8,0,12,4]);box(x+1.5,y+.5,7.6,1,1,.8,[8,0,12,4])
  write(A/f'models/item/poloz_whip_pose_{pose}.json',{'textures':{'lash':'slavicmyths:item/poloz_whip_segments','particle':'slavicmyths:item/poloz_whip_segments'},'elements':elements,'display':{'firstperson_righthand':{'rotation':[0,-90,-15],'translation':[0,1,0],'scale':[.65,.65,.65]},'thirdperson_righthand':{'rotation':[0,0,-15],'translation':[0,1,0],'scale':[.8,.8,.8]}}})
 write(A/'models/item/poloz_whip.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/poloz_whip'},'overrides':[{'predicate':{'slavicmyths:whip_pose':v},'model':f'slavicmyths:item/poloz_whip_pose_{i}'} for i,v in enumerate([.1,.35,.5,.7,1])]})
 # 36 collision/occupancy cells, one tile and exactly one loot-bearing anchor.
 write(A/'blockstates/poloz_head.json',{'variants':{'':{'model':'slavicmyths:block/poloz_head'}}})
 write(A/'models/block/poloz_head.json',{'textures':{'particle':'slavicmyths:entity/great_poloz_head'}})
 write(RES/'data/slavicmyths/loot_table/blocks/poloz_head.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:poloz_head','properties':{'cell':'0'}},{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':'slavicmyths:poloz_head'}]}]})
 write(RES/'data/slavicmyths/tags/block/mineable/pickaxe.json',merge_tag(RES/'data/slavicmyths/tags/block/mineable/pickaxe.json','slavicmyths:poloz_head'))
 # Dark stone core under golden overlapping scales; face and bone UV areas stay explicit.
 for name in ['great_poloz_body','great_poloz_head']:
  im=Image.open(A/f'textures/entity/{name}.png');d=ImageDraw.Draw(im)
  for y in range(0,24):
   for x in range(0,48):d.point((x,y),fill=random.Random(f'{name}{x}:{y}').choice(['#25232A','#383034','#4A3B34','#29242A']))
  for x in range(4,44,9):d.line([(x,0),(x+2,7),(x-1,14),(x+1,23)],fill=red)
  im.save(A/f'textures/entity/{name}.png')
 Image.open(A/'textures/entity/great_poloz_body.png').save(A/'textures/entity/poloz_stone_spike.png')
 # Atmospheric growths have their own three tapering spires, not a cube recolor.
 elements=[]
 for cx,cz,height in [(5,5,14),(11,10,10),(5,12,7)]:
  for y in range(0,height,3):
   width=max(1,4-y//4);elements.append({'from':[cx-width/2,y,cz-width/2],'to':[cx+width/2,min(height,y+3),cz+width/2],'faces':{f:{'texture':'#growth'} for f in ['up','down','north','south','east','west']}})
 write(A/'models/block/mor_growth.json',{'textures':{'growth':'slavicmyths:block/mor_stone','particle':'slavicmyths:block/mor_stone'},'elements':elements})
 write(A/'models/item/mor_growth.json',{'parent':'slavicmyths:block/mor_growth'})
 write(A/'blockstates/mor_growth.json',{'variants':{'':{'model':'slavicmyths:block/mor_growth'}}})
 write(RES/'data/slavicmyths/loot_table/blocks/mor_growth.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':'slavicmyths:mor_growth'}]}]})
 localize();organic_audio()
def merge_tag(p,value):
 data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
 if value not in data['values']:data['values'].append(value)
 return data
def localize():
 ru={'poloz_helmet':'Шлем, укреплённый чешуёй Полоза','poloz_chestplate':'Нагрудник, укреплённый чешуёй Полоза','poloz_leggings':'Поножи, укреплённые чешуёй Полоза','poloz_boots':'Ботинки, укреплённые чешуёй Полоза','poloz_whip':'Плеть Полоза','rune_earth_lord_iv':'Руна Земного Владыки IV','poloz_head':'Голова Великого Полоза','poloz_core':'Сердце Великого Полоза'}
 en={'poloz_helmet':'Poloz Scale Helmet','poloz_chestplate':'Poloz Scale Chestplate','poloz_leggings':'Poloz Scale Leggings','poloz_boots':'Poloz Scale Boots','poloz_whip':'Poloz Whip','rune_earth_lord_iv':'Earth Lord Rune IV','poloz_head':'Great Poloz Head','poloz_core':'Great Poloz Heart'}
 for lang,names in [('ru_ru',ru),('en_us',en)]:
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));r=lang=='ru_ru'
  for name,title in names.items():data['item.slavicmyths.'+name]=title
  data['block.slavicmyths.mor_growth']='Моровые наросты' if r else 'Blight Growths'
  data['block.slavicmyths.poloz_head']=names['poloz_head'];data['entity.slavicmyths.poloz_stone_spike']='Каменный шип Полоза' if r else 'Poloz Stone Spike'
  for name,kb,dr in [('helmet',10,4),('chestplate',20,8),('leggings',15,6),('boots',10,4)]:data['item.slavicmyths.poloz_'+name+'.desc']=f'+{kb}% сопротивление отбрасыванию; −{dr}% входящего урона. Улучшение: Shift + ПКМ по столу кузнеца с незеритовой частью в руке и 2 чешуями в инвентаре.' if r else f'+{kb}% knockback resistance; −{dr}% incoming damage. Sneak-use a smithing table with the netherite piece in hand and two scales in inventory.'
  data['poloz.full_set']='Полный комплект: +14 HP и защита от заражённой земли.' if r else 'Full set: +14 HP and immunity to infected ground.'
  data['poloz.upgrade_help']='Укрепление Полоза: Shift + ПКМ по столу кузнеца с этой частью в руке; 2 чешуи в инвентаре. Зачарования, руны и прочие свойства сохраняются.' if r else 'Poloz upgrade: sneak-use a smithing table with this piece in hand and two scales in inventory. All components are preserved.'
  data['poloz.upgrade_need']='Требуются 2 чешуи Великого Полоза.' if r else 'Two Great Poloz Scales required.'
  data['item.slavicmyths.poloz_whip.desc']='13 урона; дальность 6 блоков; широкий удар. ПКМ: 3 каменных шипа, перезарядка 18 с.' if r else '13 damage; 6-block reach; wide lash. Use: three stone spikes, 18 s cooldown.'
  data['poloz.rune_earth_lord_iv']='Высшая руна: каждый четвёртый успешный удар вызывает разлом (6 урона, до 4 целей в радиусе 2 блоков); перезарядка 20 с. Шанс трофея Полоза — 5%.' if r else 'Tier IV: every fourth successful hit causes a fissure (6 damage, up to four targets within 2 blocks); 20 s cooldown. Poloz drop chance: 5%.'
  for key in ['rpg.slavicmyths.effect_earth_lord_iv']+[f'rpg.slavicmyths.rune_hint_earth_lord_iv_{i}' for i in [1,2,3]]:data[key]=data['poloz.rune_earth_lord_iv']
  data['item.slavicmyths.rune_earth_lord_iv.desc']=data['poloz.rune_earth_lord_iv'];data['rpg.slavicmyths.category_whip']='Плеть' if r else 'Whip'
  data['poloz.relic.rune_earth_lord_iv']=data['poloz.rune_earth_lord_iv']
  data['poloz.relic.poloz_core']= 'Сердце глубинной силы — гарантированный трофей Полоза.' if r else 'Heart of the depths, a guaranteed Poloz trophy.'
  for entry,title,text in [('poloz_rewards','Награды Полоза' if r else 'Poloz Rewards','Полоз оставляет голову, сердце, 8–12 чешуй и плеть. Две чешуи укрепляют незеритовую часть у стола кузнеца: удерживайте Shift и нажмите ПКМ с бронёй в руке. Свойства брони сохраняются. Руна Земного Владыки IV выпадает с шансом 5%. Голова занимает 3×3×4 блока.' if r else 'Poloz drops his head, heart, 8–12 scales and whip. Sneak-use a smithing table holding netherite armor to upgrade it with two scales; existing components survive. Earth Lord Rune IV has a 5% drop chance. The trophy occupies 3×3×4 blocks.')]:
   data[f'tales.entry.{entry}.title']=title
   for stage in ['known','rumor','seen','verified','discovered','studied']:data[f'tales.entry.{entry}.{stage}']=text
  for key in list(data):
   if key.startswith('tales.entry.great_poloz.') and key.endswith(('known','rumor','seen','verified','discovered','studied')):data[key]=data[key].replace('Сердцевина и чешуя — трофеи.','Голова, сердце, 8–12 чешуй и плеть — гарантированные трофеи. Руна IV выпадает редко; чешуя укрепляет незеритовую броню.').replace('Core and scales are trophies.','Head, heart, scales and whip are guaranteed trophies; scales reinforce netherite armor.')
  write(p,data)
 write(RES/'data/slavicmyths/tales/poloz_rewards.json',{'part':'crafts','chapter':'materials','section':'catalogue','title':'tales.entry.poloz_rewards.title','icon':'slavicmyths:poloz_head','initial':0,'subjects':['item/slavicmyths:poloz_head','item/slavicmyths:poloz_whip','item/slavicmyths:rune_earth_lord_iv'],'advancements':[],'links':['slavicmyths:great_poloz']})
def organic_audio():
 rate=44100;target=A/'sounds/poloz';rng=random.Random('alligator-variant-2')
 with tempfile.TemporaryDirectory() as tmp:
  for name,duration in [('mor_horn_call',6),('mor_horn_charge',3),('poloz_distant_reply',3),('poloz_surface_roar',2.3),('poloz_phase3_roar',2),('poloz_minion_call',1.2),('poloz_death',5),('poloz_ambient_rumble',2),('poloz_weakspot_hit',.8),('poloz_armor_deflect',.5),('poloz_move_crush',.7),('poloz_earth_rumble',3),('poloz_tail_slam',1.5),('poloz_mor_rain',2),('poloz_burrow',2),('poloz_emerge',1.4),('poloz_phase2_cracks',3)]:
   cycle=0;period=rate/48;breath=0;rough=0;frames=array.array('h');formants=[[0.,0.] for _ in range(3)];jitter=1
   for i in range(int(rate*duration)):
    t=i/rate;env=min(1,t/.22)*min(1,(duration-t)/.65);breath=.96*breath+.04*rng.uniform(-1,1);rough=.65*rough+.35*rng.uniform(-1,1)
    if cycle>=period:cycle-=period;jitter=rng.uniform(.78,1.22);period=rate/(43+18*math.sin(t*1.7)+8*breath)*jitter
    phase=cycle/period;cycle+=1
    pulse=(math.sin(math.pi*phase/.42)**2 if phase<.42 else 0)-.21
    source=pulse*(.7+.3*rough)+rough*.13+breath*.4;v=0
    for f,band,gain,history in zip([160,470,940],[80,160,260],[.5,.2,.09],formants):
     r=math.exp(-math.pi*band/rate);out=(1-r)*source+2*r*math.cos(2*math.pi*f/rate)*history[0]-r*r*history[1];history[1]=history[0];history[0]=out;v+=gain*out
    contour=(.24 if t<.8 else 1) if name=='mor_horn_call' else 1
    frames.append(round(math.tanh((v+breath*.3)*2)*env*contour*.68*32767))
   wav=Path(tmp)/(name+'.wav')
   with wave.open(str(wav),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(frames.tobytes())
   subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','5',str(target/(name+'.ogg'))],check=True)
if __name__=='__main__':main()

# Preserve the user-authorized 1.9.1 summon rules after legacy text generation.
from summon_access_policy import apply as apply_summon_access
apply_summon_access()

# Keep the current armor revision after legacy asset output.
if __name__ == "__main__":
 from armor_revision_assets import apply as armor_revision
 armor_revision()

from weapon_feedback_assets import apply as weapon_feedback
weapon_feedback()
