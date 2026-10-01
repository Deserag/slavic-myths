"""Reproducible 0.8.2 assets. Native cuboid geometry and original synthesized Foley.
No borrowed recordings. UV materials are authored for NightingaleModel's 64px cells.
"""
from finalize_065 import *
from water_070 import png
import math,sys,struct

NAMES={'nightingale_mark':('Знак Соловья-разбойника',"Nightingale's Mark"),'nightingale_dagger':('Кинжал Соловья',"Nightingale's Dagger"),'nightingale_lock':('Прядь Соловья',"Nightingale's Lock"),'nightingale_whistle':('Свисток Соловья','Nightingale Whistle'),'bandit_horn':('Разбойничий рог',"Bandit's Horn")}
SOUNDS={'idle':('Соловей тяжело дышит','Nightingale breathes heavily'),'notice':('Соловей заметил чужака','Nightingale notices an intruder'),'hurt':('Соловей кряхтит','Nightingale grunts in pain'),'breath':('Соловей напрягается','Nightingale strains'),'inhale':('Соловей набирает воздух','Nightingale draws breath'),'deep_inhale':('Соловей глубоко вдыхает','Nightingale takes a deep breath'),'whistle':('Соловей свистит','Nightingale whistles'),'destructive':('Разрушительный свист','Devastating whistle'),'wave':('Удар воздуха','Air rushes'),'melee':('Взмах тяжёлого клинка','Heavy blade swings'),'voice':('Соловей рычит','Nightingale growls'),'death':('Последний свист Соловья',"Nightingale's final whistle"),'horn':('Разбойничий рог звучит',"Bandit's horn sounds")}

def textures():
 colors=[(164,119,85),(143,135,110),(69,47,32),(81,66,54),(42,29,23),(103,108,103),(194,184,156),(42,35,30)]
 def pixel(x,y):
  cell=(y//64)*4+x//64;u=x%64;v=y%64;c=colors[cell] if cell<len(colors) else (77,61,44)
  noise=((x*13+y*7+x*y)%9)-4
  if cell==0:noise+= -9 if (u+v*3)%23<3 else 0
  if cell==1:
   noise+=3 if v%3==0 else -2
   if u%16 in (0,1):noise-=17
   if u%16==2 and v%4<2:noise+=22
  if cell==2:
   if v%7==0:noise-=12
   if (u*3+v)%37<2:noise+=14
  if cell==3:noise-=10 if u%11<2 else 0
  if cell==4:noise+=(u%5)*3-6;noise-=7 if (u+v//8)%7==0 else 0
  if cell==5:noise+=9 if (u+2*v)%19==0 else 0
  if cell==6:noise=0
  return tuple(max(0,min(255,n+noise))for n in c)+(255,)
 png(A/'textures/entity/nightingale.png',256,pixel)
 # Item-only material swatches preserve pixel texture without stretching full vanilla blocks.
 for name,base in [('steel',(123,130,127)),('edge',(186,191,177)),('leather',(70,43,28)),('hair',(36,25,20)),('silver',(183,186,169)),('bone',(192,176,131)),('dark',(40,42,39))]:
  def pix(x,y,c=base,n=name):
   v=((x*13+y*7)%7)-3
   if n=='hair':v+=(x%4)*3-4
   if n=='leather' and y%4==0:v-=12
   if n in ('steel','silver') and (x+y*2)%19==0:v+=16
   return tuple(max(0,min(255,k+v))for k in c)+(255,)
  png(A/f'textures/item/nightingale_{name}.png',16,pix)

def items():
 tex={n:'slavicmyths:item/nightingale_'+n for n in ['steel','edge','leather','hair','silver','bone','dark']};tex['particle']=tex['steel']
 display={'gui':{'rotation':[15,135,-12],'scale':[.9]*3},'ground':{'scale':[.5]*3},'fixed':{'rotation':[0,180,0]},'firstperson_righthand':{'rotation':[0,90,-12],'translation':[1,2,0],'scale':[.8]*3},'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,2,0],'scale':[.8]*3}}
 # Broad asymmetric blade, short guard, wrapped grip: shorter than a sword, wider than a knife.
 blade=[cube([7.2,-1,7.3],[8.8,4,8.7],'leather'),cube([6.8,-1.8,7],[9.2,-.7,9],'steel'),cube([5.5,4,7],[10.5,5,9],'steel'),cube([6.5,5,7.5],[9.5,13,8.5],'steel'),cube([6,5,7.5],[6.6,12,8.5],'edge'),cube([6.5,12,7.5],[8.8,15.5,8.5],'steel'),cube([7.1,15.5,7.6],[8.3,17.5,8.4],'edge'),cube([7.3,6,7.3],[7.8,13,7.5],'dark')]
 for y in range(0,4):blade.append(cube([7.05,y,7.15],[8.95,y+.25,8.85],'dark'))
 model('item/nightingale_dagger',blade,tex,display)
 mark=[cube([5,7,7],[5.5,15,7.5],'leather'),cube([10.5,7,7],[11,15,7.5],'leather'),cube([5,14.5,7],[11,15,7.5],'leather'),cube([4.5,2,6.5],[11.5,8,8.5],'dark',([8,5,8],'z',22.5)),cube([6,3,6.2],[10,7,6.5],'steel'),cube([6.5,4,6],[9.5,4.5,6.3],'edge'),cube([7.7,4.5,6],[8.2,6.5,6.3],'edge')]
 model('item/nightingale_mark',mark,tex,display)
 lock=[]
 for i in range(6):
  x=5+i*.9;lock.append(cube([x,3+i%3,7+(i%2)*.4],[x+.7,12-i%2,7.7+(i%2)*.4],'hair',([8,10,8],'z',-22.5 if i<2 else 22.5 if i>3 else 0)))
 lock.extend([cube([4.8,10,6.8],[10.5,11,8.7],'leather'),cube([9.8,8,7],[10.4,11,7.7],'leather')]);model('item/nightingale_lock',lock,tex,display)
 whistle=[cube([4,6,6],[11,10,10],'silver'),cube([10,7,6.5],[14,9,9.5],'bone'),cube([13.5,7.3,7],[14.3,8.7,9],'dark'),cube([6,9.8,7],[8,10.1,9],'dark'),cube([3.2,6.5,7],[4.2,9.5,9],'silver'),cube([3,4,7.5],[3.5,8,8],'leather')]
 for i in range(4):whistle.append(cube([2.3+i*.6,1+i%2,7.3],[2.7+i*.6,5,8],'hair'))
 model('item/nightingale_whistle',whistle,tex,display)
 horn=[cube([3,10,7],[6,12,9],'bone'),cube([5,8,6.5],[8,11,9.5],'bone'),cube([7,5,6],[10,9,10],'bone'),cube([9,3,5],[13,6,11],'steel'),cube([9.5,2.7,5.5],[12.5,3.1,10.5],'dark'),cube([3,10.5,7.3],[3.5,11.5,8.7],'silver')];model('item/bandit_horn',horn,tex,display)

def pool(item,chance=1,lo=1,hi=1):
 p={'rolls':1,'entries':[{'type':'minecraft:item','name':item,'functions':[{'function':'minecraft:set_count','count':{'min':lo,'max':hi}}]}]}
 if chance<1:p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
 return p

def data():
 write(D/'loot_tables/entities/nightingale.json',{'type':'minecraft:entity','pools':[pool('slavicmyths:nightingale_mark'),pool('slavicmyths:nightingale_dagger'),pool('slavicmyths:nightingale_lock',.28)]})
 write(D/'loot_tables/chests/stronghold_nightingale.json',{'type':'minecraft:chest','pools':[pool('slavicmyths:bandit_horn'),pool('slavicmyths:mysterious_black_feather'),pool('slavicmyths:silver_ingot',.85,3,8),pool('slavicmyths:ancient_coin',.85,6,18),pool('minecraft:cooked_beef',1,3,8),pool('minecraft:arrow',.8,8,24),pool('slavicmyths:amber',.35,1,3),pool('slavicmyths:rune_wind',.18),pool('slavicmyths:resin_ring',.1)]})
 shaped('nightingale_whistle',[' SB','SAS',' L '],{'S':'silver_ingot','B':'minecraft:bone','A':'amber','L':'nightingale_lock'})
 write(D/'tags/blocks/whistle_fragile.json',{'replace':False,'values':['minecraft:grass','minecraft:tall_grass','minecraft:fern','minecraft:large_fern','minecraft:dandelion','minecraft:poppy','minecraft:dead_bush','minecraft:oak_leaves','minecraft:birch_leaves','minecraft:spruce_leaves','slavicmyths:pine_leaves','slavicmyths:willow_leaves']})
 write(D/'advancements/meet_nightingale.json',{'criteria':{'encounter':{'trigger':'minecraft:impossible'}}})
 write(D/'advancements/defeat_nightingale.json',{'parent':'slavicmyths:find_large_camp','display':{'icon':{'item':'slavicmyths:nightingale_mark'},'title':{'translate':'advancement.slavicmyths.defeat_nightingale.title'},'description':{'translate':'advancement.slavicmyths.defeat_nightingale.description'},'frame':'challenge','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'victory':{'trigger':'minecraft:impossible'}}})
 for index,lang in enumerate(['ru_ru','en_us']):
  p=A/f'lang/{lang}.json';v=json.loads(p.read_text(encoding='utf-8'))
  for name,titles in NAMES.items():v['item.slavicmyths.'+name]=titles[index]
  for name,titles in SOUNDS.items():v['subtitles.slavicmyths.nightingale_'+name]=titles[index]
  v['entity.slavicmyths.nightingale']=('Соловей-разбойник','Nightingale the Robber')[index]
  v['tooltip.slavicmyths.nightingale_whistle']=('Удерживайте ПКМ: вдох и порыв ветра. Отталкивает обычных существ, тушит огонь. Перезарядка: 30 с.','Hold use: inhale and release a gust. Pushes ordinary creatures, extinguishes fire. Cooldown: 30 s.')[index]
  v['tooltip.slavicmyths.nightingale_dagger']=('Удерживайте ПКМ и отпустите: короткий воздушный толчок. Перезарядка: 7 с.','Hold use, then release: a short air shove. Cooldown: 7 s.')[index]
  v['tooltip.slavicmyths.bandit_horn']=('Рог с помоста лесного разбойника. Удерживайте ПКМ, чтобы протрубить.','A horn from the forest robber’s platform. Hold use to sound it.')[index]
  v['advancement.slavicmyths.defeat_nightingale.title']=('Не свисти — денег не будет','Whistling Your Fortune Away')[index]
  v['advancement.slavicmyths.defeat_nightingale.description']=('Победите Соловья-разбойника','Defeat Nightingale the Robber')[index]
  v['book.slavicmyths.nightingale.title']=('Соловей-разбойник','Nightingale the Robber')[index]
  v['book.slavicmyths.nightingale.text']=(
   'Перо в разбойничьем стане оказалось не птичьей приметой, а чужой насмешкой. Над двором, у старой сосны, сидит широкоплечий человек с тяжёлым кинжалом. Он не поёт: сперва затихает, расправляет грудь и втягивает воздух так, что шевелится пыль. Тогда ищи толстый ствол или камень — хлипкая изгородь не спасёт. Кто спешит прямо к его помосту, рискует услышать за спиной шаги ещё живой шайки. На поясе у Соловья висит тёмный знак; прядь его волос умельцы вплетают в костяной свисток. После его последнего надломленного свиста дорога впервые становится тихой.',
   'The feather in the bandit stronghold was a taunt, not a bird omen. Above the yard, beside an old pine, sits a broad-shouldered man with a heavy dagger. He does not sing: first he falls silent, opens his chest and draws in enough air to stir the dust. Seek a thick trunk or a boulder then; a flimsy fence will not shelter you. Rush straight to his platform and the surviving gang may follow at your heels. A dark token hangs from his belt. Craftsmen bind a lock of his hair into a bone whistle. After his final broken whistle, the road falls quiet at last.')[index]
  write(p,v)

def normalize_ogg(path,serial):
 data=bytearray(path.read_bytes());offset=0
 while offset<len(data):
  count=data[offset+26];length=27+count+sum(data[offset+27:offset+27+count]);data[offset+14:offset+18]=struct.pack('<I',serial);data[offset+22:offset+26]=b'\0'*4;crc=0
  for byte in data[offset:offset+length]:
   crc^=byte<<24
   for _ in range(8):crc=((crc<<1)^0x04c11db7) & 0xffffffff if crc&0x80000000 else (crc<<1)&0xffffffff
  data[offset+22:offset+26]=struct.pack('<I',crc);offset+=length
 path.write_bytes(data)

def sounds():
 sys.path.insert(0,str(R/'.tools/audio-libs'));import numpy as np;import soundfile as sf
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));rate=22050
 def clip(duration,kind,seed):
  t=np.arange(int(rate*duration))/rate;rng=np.random.default_rng(seed);noise=rng.normal(0,1,len(t));freq=np.fft.rfftfreq(len(t),1/rate)
  center=650 if kind in ('wave','destructive','death') else 1400
  air=np.fft.irfft(np.fft.rfft(noise)*np.exp(-((freq-center)/1000)**2),n=len(t));air/=np.max(np.abs(air))+1e-9
  envelope=np.minimum(1,t/.08)*np.minimum(1,(duration-t)/.15)
  if kind in ('inhale','deep_inhale','breath'):
   envelope*=np.sin(np.pi*t/duration)**.7;wave=air*(.18+.48*t/duration)
   if kind=='deep_inhale':wave+=.06*np.sin(2*np.pi*82*t)*(np.sin(np.pi*t/duration)**2)
  elif kind in ('whistle','destructive'):
   f0=(1050 if kind=='whistle' else 680)+35*np.sin(t*29)+18*np.sin(t*11);phase=2*np.pi*np.cumsum(f0)/rate
   wave=(.25*np.sin(phase)+.045*np.sin(phase*2)+air*.45);envelope*=np.exp(-t*(2 if kind=='whistle' else 1.4))
  elif kind in ('wave','melee'):
   wave=air;envelope*=np.exp(-t*5)
  elif kind=='horn':
   phase=2*np.pi*150*t;wave=sum(np.sin(phase*h)*(.3/h)for h in range(1,8));envelope*=np.sin(np.pi*t/duration)**.5
  else:
   f0=89+seed%17+7*np.sin(t*17);phase=2*np.pi*np.cumsum(f0)/rate;wave=air*.1
   for h in range(1,32):
    f=(89+seed%17)*h;gain=sum(math.exp(-.5*((f-center)/width)**2)for center,width in [(420,95),(1000,180),(2200,260)])/h
    wave+=np.sin(phase*h)*gain
   envelope*=np.sin(np.pi*t/duration)**.6
   if kind=='death':
    wave*=np.maximum(0,1-t/1.7);wh=(t>1.45)&(t<2.15);wave+=wh*.12*np.sin(2*np.pi*740*t)*np.maximum(0,1-(t-1.45)/.7)
  wave*=envelope;return wave*(.55/(np.max(np.abs(wave))+1e-9))
 durations={'idle':1.5,'notice':.65,'hurt':.38,'breath':.9,'inhale':1.2,'deep_inhale':1.9,'whistle':.65,'destructive':1.1,'wave':.55,'melee':.28,'voice':.7,'death':2.7,'horn':1.3}
 for k,(name,duration) in enumerate(durations.items()):
  entries=[]
  for variant in range(3 if name=='hurt'else 1):
   stem=name+('_'+str(variant)if name=='hurt'else'');p=A/f'sounds/nightingale/{stem}.ogg';p.parent.mkdir(parents=True,exist_ok=True)
   sf.write(str(p),clip(duration,name,8200+k*3+variant),rate,format='OGG',subtype='VORBIS');normalize_ogg(p,8200+k*3+variant);entries.append({'name':'slavicmyths:nightingale/'+stem})
  events['nightingale_'+name]={'subtitle':'subtitles.slavicmyths.nightingale_'+name,'sounds':entries}
 write(A/'sounds.json',events)

def generate():textures();items();data();sounds()
if __name__=='__main__':generate()
