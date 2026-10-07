"""0.7.0 native geometry, UV materials, authored audio and data resources."""
from finalize_065 import *
from artifact_065 import advance
import struct,zlib,random,sys
def png(path,size,pixel):
 def chunk(t,d):return struct.pack('!I',len(d))+t+d+struct.pack('!I',zlib.crc32(t+d)&0xffffffff)
 raw=b''.join(b'\0'+bytes(v for x in range(size) for v in pixel(x,y)) for y in range(size))
 path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',size,size,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
def material(path,size,color,spirit=False):
 def pixel(x,y):
  base=color
  if spirit:
   if x>=64:base=(37,53,43) if y>=32 else (32,39,33)
   elif y>=32:base=(159,173,156) if color[0]>150 else (65,73,61)
  elif x>=64:base=(25,29,19)
  elif y>=32:base=tuple(max(0,c-24) for c in color)
  noise=((x*17+y*29+x*y*3)%13)-6
  if not spirit and ((x//3+y//2)%7==0):noise+=18
  return tuple(max(0,min(255,c+noise)) for c in base)+(255,)
 png(path,size,pixel)
def generate():
 tex={'wood':'minecraft:block/spruce_planks','rope':'minecraft:block/sandstone_top','iron':'minecraft:block/gray_concrete','green':'minecraft:block/green_wool','leaf':'minecraft:block/mossy_cobblestone','white':'minecraft:block/white_wool','gold':'minecraft:block/yellow_terracotta','water':'minecraft:block/light_blue_terracotta'};tex['particle']=tex['wood']
 display={'gui':{'rotation':[25,135,0],'scale':[.8]*3},'ground':{'scale':[.5]*3},'thirdperson_righthand':{'rotation':[0,90,0],'scale':[.65]*3},'firstperson_righthand':{'rotation':[0,90,0],'scale':[.75]*3}}
 names={
 'raw_pike':('Сырая щука','Raw pike'),'cooked_pike':('Приготовленная щука','Cooked pike'),'raw_carp':('Сырой карп','Raw carp'),'cooked_carp':('Приготовленный карп','Cooked carp'),'smoked_carp':('Копчёный карп','Smoked carp'),'raw_crayfish':('Речной рак','River crayfish'),'cooked_crayfish':('Варёный рак','Cooked crayfish'),'ukha':('Уха','Ukha fish soup'),'old_hook':('Старый крючок','Old fishing hook'),'pearl_fragment':('Перламутровый фрагмент','Nacre fragment'),
 'fishing_net':('Рыболовная сеть','Fishing net'),'reed':('Камыш','Reed'),'water_grass':('Водная трава','Water grass'),'white_lily':('Белая кувшинка','White water lily')}
 animals={'pike':((83,101,56),'Щука','Pike'),'carp':((163,119,56),'Карп','Carp'),'crayfish':((73,78,50),'Речной рак','Crayfish'),'vodyanoy':((104,127,113),'Водяной','Vodyanoy'),'rusalka':((198,213,207),'Русалка','Rusalka')}
 for name,(color,ru,en) in animals.items():
  material(A/f'textures/entity/{name}.png',256,color,name in ('vodyanoy','rusalka'))
  write(A/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
  names[name+'_spawn_egg']=(ru+': яйцо призыва',en+' spawn egg')
  item='raw_'+name if name in ('pike','carp','crayfish') else 'water_grass' if name=='vodyanoy' else 'pearl_fragment'
  write(D/f'loot_tables/entities/{name}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item}]}]})
 for name,(ru,en) in names.items():
  if name.endswith('spawn_egg') or name in ('fishing_net','reed','water_grass','white_lily'):continue
  color=(135,111,57) if 'cooked' in name or 'smoked' in name else (83,101,56) if 'pike' in name else (163,119,56) if 'carp' in name else (90,77,54)
  if name=='cooked_crayfish':color=(168,74,42)
  if name=='pearl_fragment':color=(196,207,204)
  material(A/f'textures/item/{name}.png',32,color)
  own=dict(tex);own['body']='slavicmyths:item/'+name
  if 'pike' in name:els=[cube([1,7,6],[12,10,9],'body'),cube([11,7.4,6.3],[16,9.4,8.7],'body'),cube([0,6,5.5],[1,11,9.5],'body')]
  elif 'carp' in name:els=[cube([4,4,5],[12,12,10],'body'),cube([11,6,5.5],[14,10,9.5],'body'),cube([1,5,6],[4,11,9],'body')]
  elif 'crayfish' in name:els=[cube([5,6,5],[10,9,12],'body'),cube([2,6,2],[5,8,7],'body'),cube([10,6,2],[13,8,7],'body')]+[cube([5.5,6.5,12+i],[9.5,8.5,12.7+i],'body') for i in range(3)]
  elif name=='ukha':els=[cube([3,5,3],[13,6,13],'wood'),cube([3,6,3],[4,9,13],'wood'),cube([12,6,3],[13,9,13],'wood'),cube([4,6,3],[12,9,4],'wood'),cube([4,6,12],[12,9,13],'wood'),cube([4,6,4],[12,8,12],'rope'),cube([5,8,5],[7,8.4,7],'white'),cube([9,8,9],[10.5,8.5,10],'green')]
  elif name=='old_hook':els=[cube([7,3,7],[8,12,8],'iron'),cube([4,2,7],[8,3,8],'iron'),cube([3,3,7],[4,6,8],'iron'),cube([7,12,7],[10,13,8],'iron')]
  else:els=[cube([4,5,7],[11,11,8.3],'body',([8,8,8],'z',22.5))]
  model('item/'+name,els,own,display)
 for filled in (False,True):
  els=[]
  for x in range(1,16,3):els.append(cube([x,.5,7.8],[x+.18,14,8.1],'rope'))
  for y in range(1,15,3):els.append(cube([1,y,7.8],[15,y+.18,8.1],'rope'))
  for x in (1,7,13):els += [cube([x,13,7],[x+2,15,9],'wood'),cube([x,.2,7.4],[x+1,1.2,8.7],'iron')]
  if filled:els += [cube([5,4,7],[10,5.5,9],'gold'),cube([9,8,7],[13,9,9],'green')]
  model('block/fishing_net_'+str(filled).lower(),els,tex)
 write(A/'blockstates/fishing_net.json',{'variants':{f'filled={str(f).lower()}':{'model':'slavicmyths:block/fishing_net_'+str(f).lower()} for f in (False,True)}})
 model('item/fishing_net',[cube([3,5,4],[13,9,12],'rope')]+[cube([x,5,3],[x+1,10,13],'wood') for x in (3,8,12)],tex,display)
 write(D/'loot_tables/blocks/fishing_net.json',{'type':'minecraft:block','pools':[]})
 plants={'reed':[cube([x,0,z],[x+.6,18+i*2,z+.6],'green') for i,(x,z) in enumerate(((4,5),(9,8),(6,11)))]+[cube([x-.3,14+i*2,z-.3],[x+.9,20+i*2,z+.9],'wood') for i,(x,z) in enumerate(((4,5),(9,8),(6,11)))],
 'water_grass':[cube([3+i*2,0,6+i],[3.5+i*2,9+i*1.5,8+i],'green',([4+i*2,1,7+i],'z',22.5 if i%2 else -22.5)) for i in range(5)],
 'white_lily':[cube([1,0,1],[15,.5,15],'green'),cube([5,.5,5],[11,1.5,11],'white'),cube([3,1,6],[13,2,10],'white'),cube([6,1,3],[10,2,13],'white'),cube([6,2,6],[10,2.7,10],'gold')]}
 for name,els in plants.items():model('block/'+name,els,tex);write(A/f'blockstates/{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}});write(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name});drop(name,name)
 shaped('fishing_net',['SWS','WHW','SIS'],{'S':'minecraft:stick','W':'minecraft:string','H':'old_hook','I':'minecraft:iron_nugget'})
 shaped('old_hook',[' I','I ',' I'],{'I':'minecraft:iron_nugget'})
 for raw,cooked in [('raw_pike','cooked_pike'),('raw_carp','cooked_carp'),('raw_crayfish','cooked_crayfish')]:
  for kind in ('smelting','campfire_cooking'):
   write(D/f'recipes/{cooked}_{kind}.json',{'type':'minecraft:'+kind,'ingredient':{'item':'slavicmyths:'+raw},'result':'slavicmyths:'+cooked,'experience':.35,'cookingtime':200 if kind=='smelting' else 600})
 write(D/'recipes/smoked_carp.json',{'type':'minecraft:smoking','ingredient':{'item':'slavicmyths:raw_carp'},'result':'slavicmyths:smoked_carp','experience':.4,'cookingtime':100})
 write(D/'recipes/reed_string.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'slavicmyths:reed'}]*3,'result':{'item':'minecraft:string'}})
 write(D/'loot_tables/chests/fishing_supplies.json',{'type':'minecraft:chest','pools':[{'rolls':{'min':2,'max':4},'entries':[{'type':'minecraft:item','name':n,'weight':weight} for n,weight in [('minecraft:string',5),('minecraft:fishing_rod',1),('minecraft:bread',4),('slavicmyths:old_hook',2),('slavicmyths:smoked_carp',2),('slavicmyths:pearl_fragment',1)]]}]})
 advancements=[('new_catch','raw_pike','Новый улов','A new catch','Поймать новую речную рыбу','Catch a new river fish'),('net_catch','fishing_net','Старый способ','The old way','Забрать рыбу из сети','Collect fish from a net'),('meet_vodyanoy','pearl_fragment','Хозяин воды','Master of the water','Встретить Водяного','Meet Vodyanoy'),('meet_rusalka','white_lily','Тихий берег','A quiet shore','Встретить Русалку','Meet Rusalka'),('water_call','white_lily','На зов','Answering the call','Попасть под сильное очарование','Experience a strong water song'),('break_water_call','veles_amulet','Свой путь','My own path','Вырваться из сильного очарования','Escape a strong water song')]
 for name,item,*_ in advancements:advance(name,item)
 books={
 'water_fish':('Речные обитатели','River life','Щука вытянута и делает короткие рывки; карп широк и спокоен; рак держится у дна. Новые уловы дополняют обычную удочку в пресной воде. Океан сохраняет ванильную рыбу. Карпа можно коптить, щуку использовать для ухи: приготовленная щука, картофель и миска на кухонном столе с горшком.','Pike are long ambush fish, carp are broad and calm, crayfish live near the bottom. Freshwater catches supplement the normal rod. Oceans retain vanilla fish. Smoke carp; cook ukha at the kitchen table using cooked pike, potato, bowl and a pot.'),
 'fishing_net':('Рыболовная сеть','Fishing net','Сеть ставится в воду рядом с дном. Раз в 5–7 минут она пробует поймать рыбу; максимум четыре улова. ПКМ забирает улов, Shift+ПКМ снимает сеть. После 32 уловов сеть рвётся. Близкие сети снижают эффективность. Treasure остаётся преимуществом удочки.','Place a net in water near the bottom. Every 5–7 minutes it attempts a catch, up to four stored fish. Use to collect; sneak-use to retrieve. After 32 catches it breaks. Crowded nets are less efficient; use a rod for treasure.'),
 'vodyanoy':('Водяной','Vodyanoy','Водяной — хозяин местной воды. Хлеб, каравай и перламутр могут улучшить отношение. Частый вылов и скопление сетей раздражают его. Довольный хозяин даёт удачу рыбака; разгневанный опаснее в воде. Это игровая интерпретация фольклорного образа.','Vodyanoy rules local water. Bread, karavai and nacre improve relations. Overfishing and crowded nets anger him. A friendly master grants fishing luck; an angry one is dangerous underwater. This is a gameplay interpretation of folklore.'),
 'rusalka':('Русалка','Rusalka','Услышав песню, не стой долго у воды. Сначала это предупреждение, затем зов начинает тянуть ближе. Уйди за пределы 20 блоков или за препятствие; удар срывает песню. Гусли и оберег Велеса ослабляют влияние, но не дают полной защиты. После ухода очарование быстро спадает. Русалка здесь человекоподобна, без рыбьего хвоста: игровая интерпретация славянского образа.','Do not linger when you hear her song. A warning grows into a pull toward the water. Leave the 20-block range, take cover, or interrupt her with a hit. Gusli and the Veles amulet reduce exposure without granting immunity. Charm fades quickly after escape. This human-shaped, tailless spirit is a gameplay interpretation of Slavic folklore.')}
 for lang in ('ru_ru','en_us'):
  path=A/f'lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'));idx=0 if lang=='ru_ru' else 1
  for name,pair in names.items():data[('block' if name in plants or name=='fishing_net' else 'item')+'.slavicmyths.'+name]=pair[idx]
  for name,(_,ru,en) in animals.items():data['entity.slavicmyths.'+name]=(ru,en)[idx]
  for name,item,ru,en,rd,ed in advancements:data[f'advancement.slavicmyths.{name}.title']=(ru,en)[idx];data[f'advancement.slavicmyths.{name}.description']=(rd,ed)[idx]
  for name,(ru,en,rt,et) in books.items():data[f'book.slavicmyths.{name}.title']=(ru,en)[idx];data[f'book.slavicmyths.{name}.text']=(rt,et)[idx]
  data['water.slavicmyths.charm']=('Зов воды: %s / 100','Water charm: %s / 100')[idx];write(path,data)
 sounds()
def sounds():
 sys.path.insert(0,str(R/'.tools/audio-libs'));import numpy as np;import soundfile as sf
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 for who in ('vodyanoy','rusalka'):
  for kind in ('ambient','angry','hurt','death','song'):
   if who=='vodyanoy' and kind=='song':continue
   name=who+'_'+kind;duration=8 if kind=='song' else 1.8;rate=22050;t=np.arange(int(rate*duration))/rate
   rng=np.random.default_rng(sum(map(ord,name)));freq=(210 if who=='rusalka' else 78)*(1+.04*np.sin(t*2*np.pi*4.5)+.08*np.sin(t*2*np.pi/duration))
   phase=np.cumsum(freq)*2*np.pi/rate;wave=np.zeros(len(t))
   # Original wordless formant vocal: breath plus harmonics, no instruments or sampled voices.
   for k in range(1,20):
    hz=k*(210 if who=='rusalka' else 78);gain=sum(np.exp(-((hz-f)/width)**2) for f,width in ((650,200),(1150,250),(2700,400)))+.07
    wave+=np.sin(phase*k)*gain/k
   noise=rng.normal(0,1,len(t));noise=np.convolve(noise,np.ones(7)/7,mode='same');wave+=noise*(.045 if who=='rusalka' else .28)
   envelope=np.minimum(1,t/.35)*np.minimum(1,(duration-t)/.6)*(.75+.25*np.sin(t*np.pi/2)**2)
   wave=wave*envelope;wave*=.32/(np.max(np.abs(wave))+1e-6);path=A/f'sounds/water/{name}.ogg';path.parent.mkdir(parents=True,exist_ok=True);sf.write(str(path),wave,rate,format='OGG',subtype='VORBIS')
   subtitle='subtitles.slavicmyths.'+name;events[name]={'subtitle':subtitle,'sounds':[{'name':'slavicmyths:water/'+name}]}
   for lang in ('ru_ru','en_us'):
    p=A/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d[subtitle]=( ('Русалка' if who=='rusalka' else 'Водяной')+' — '+{'ambient':'голос','angry':'недовольство','hurt':'боль','death':'затихает','song':'поёт'}[kind]) if lang=='ru_ru' else who.title()+' '+kind;write(p,d)
 write(A/'sounds.json',events)
if __name__=='__main__':generate()

# Keep Kitchen II canonical resources/recipes after this legacy generation pass.
import runpy
runpy.run_path(str(__import__("pathlib").Path(__file__).with_name("kitchen_resources.py")), run_name="__main__")
