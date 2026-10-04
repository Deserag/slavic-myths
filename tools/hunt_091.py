"""Scoped native clustered-pixel art and exact Hunt II data; never rewrites Hunt I/user art."""
from finalize_065 import A,D,R,write
from PIL import Image,ImageDraw
import math,json,random
ITEMS={
'pylayuschaya_cheshuya':('Пылающая чешуя','Burning Scale','Чешуя всё ещё хранит жар полёта.','The scale still holds the heat of flight.'),
'ognennoe_pero':('Огненное перо','Fire Feather','Пламя держит форму даже после смерти Змея.','The flame keeps its shape after the Serpent dies.'),
'obereg_padayuschey_zvezdy':('Оберег падающей звезды','Falling Star Charm','В слоте ожерелья спасает от опасного падения: медленное падение на 5 сек. Перезарядка: 60 сек.','Equipped in the necklace slot: 5 seconds of slow falling in a dangerous fall. Cooldown: 60 seconds.'),
'uzel_podveya':('Узел Подвея','Podvey Knot','Узел затянут ветром, а не руками.','A knot tied by wind, not hands.'),
'vihrevaya_nit':('Вихревая нить','Whirlwind Thread','Нить дрожит даже в закрытом сундуке.','The thread trembles even in a closed chest.'),
'vetrovoy_uzel':('Ветровой узел','Wind Knot','ПКМ: отбрасывает обычных врагов в радиусе 4 блоков; сильные враги сопротивляются. Перезарядка: 25 сек.','Use: pushes ordinary enemies within 4 blocks; powerful enemies resist. Cooldown: 25 seconds.'),
'sosud_ognennogo_dyhaniya':('Сосуд огненного дыхания','Fire Breath Vessel','Внутри заперт жар летающего змея. Ритуальный компонент; активного действия пока нет.','The heat of a flying serpent is sealed within. Ritual component; no active ability yet.'),
'sosud_podveya':('Сосуд Подвея','Podvey Vessel','Даже запечатанный ветер ищет выход. Ритуальный компонент; активного действия пока нет.','Even sealed wind seeks an escape. Ritual component; no active ability yet.')}
SMALL={'pylayuschaya_cheshuya','uzel_podveya','vihrevaya_nit'}
COLORS={'coal':['#171417','#282323','#3E3331','#50464A'],'fire':['#771D15','#B92716','#F34E13','#FFC33A'],'rope':['#7D7160','#AB9C82','#C5BBAA','#E4D9C6'],'blue':['#263448','#41536A','#658096','#9DAAB4'],'bone':['#897657','#B6A27F','#D8C7A7','#F0DEBA'],'leather':['#332319','#5E3B2A','#8B5E3F','#B07C50'],'iron':['#25272B','#45494D','#72777C','#A1A6AB'],'red':['#48171D','#821D20','#B13C38','#DA6950'],'wind':['#416174','#658096','#B8CFDB','#DCE4E7']}
def art(name):
 # Draw directly on a 256px authoring grid; complex pieces upscale only 2x, with nearest pixels.
 im=Image.new('RGBA',(256,256));d=ImageDraw.Draw(im);rng=random.Random(910+list(ITEMS).index(name))
 def line(points,color,width=2):d.line([(round(x*2),round(y*2)) for x,y in points],fill=color,width=max(1,round(width*2)))
 def material(points,key):
  pts=[(round(x*2),round(y*2)) for x,y in points];pal=COLORS[key];mask=Image.new('L',im.size);ImageDraw.Draw(mask).polygon(pts,fill=255);layer=Image.new('RGBA',im.size,pal[1]);ld=ImageDraw.Draw(layer)
  for _ in range(2000):
   x=rng.randrange(256);y=rng.randrange(256);ld.rectangle((x,y,x+rng.choice([2,3,5]),y+rng.choice([2,4,7])),fill=rng.choice(pal))
  im.paste(layer,(0,0),mask);d.line(pts+[pts[0]],fill=pal[0],width=3);line(points[:2],pal[2],1)
 def rect(box,key):x,y,a,b=box;material([(x,y),(a,y),(a,b),(x,b)],key)
 def ellipse(box,key,width=5):
  x,y,a,b=box;pal=COLORS[key];d.ellipse((x*2,y*2,a*2,b*2),outline=pal[0],width=int((width+2)*2));d.ellipse((x*2+2,y*2+2,a*2-2,b*2-2),outline=pal[1],width=int(width*2));d.arc((x*2+3,y*2+3,a*2-3,b*2-3),190,320,fill=pal[3],width=2)
 def cord(points,key='red',width=3):
  pal=COLORS[key];line(points,pal[0],width+2);line(points,pal[1],width);line([(x-.4,y-.4) for x,y in points],pal[2],1)
 def tassel(x,y,key='red'):
  cord([(x,y-8),(x,y)],key,4)
  for i in range(-4,5,2):cord([(x+i,y),(x+i*1.5,y+18-abs(i))],key,1.5)
 def bead(x,y,key='iron'):material([(x-3,y-4),(x+3,y-4),(x+4,y),(x+2,y+4),(x-3,y+3),(x-4,y)],key)
 def spiral(x,y,r=15):
  pts=[]
  for i in range(140):a=i*.14;v=r*(1-i/150);pts.append((x+math.cos(a)*v,y+math.sin(a)*v))
  line(pts,'#416174',4);line(pts,'#78AFC8',2.3);line([(a-.4,b-.4) for a,b in pts],'#C8EEFF',1)
 def star(x,y,size):
  for pts in [[(x,y-size),(x+2,y-3),(x+size,y),(x+3,y+2),(x,y+size),(x-2,y+3),(x-size,y),(x-3,y-2),(x,y-size)],[(x-size*.7,y-size*.7),(x+size*.7,y+size*.7)],[(x+size*.7,y-size*.7),(x-size*.7,y+size*.7)]]:
   line(pts,'#A4251A',6);line(pts,'#F35A17',3);line(pts,'#FFC13A',1)
 def fang(x,y):material([(x,y),(x+12,y+2),(x+19,y+16),(x+22,y+35),(x+15,y+50),(x+14,y+29),(x+5,y+17)],'bone');line([(x+7,y+7),(x+15,y+23),(x+18,y+36)],'#F0DEBA',2)
 if name=='pylayuschaya_cheshuya':
  material([(17,116),(13,97),(22,79),(23,65),(38,49),(44,35),(60,29),(68,17),(90,14),(106,8),(110,32),(117,48),(115,64),(104,86),(87,96),(63,110),(39,119)],'fire')
  for row in range(7):
   for col in range(3):
    x=25+row*9+col*13;y=102-row*12+col*2
    material([(x-8,y+6),(x-10,y-3),(x+1,y-15),(x+9,y-11),(x+6,y+1)],'coal')
  line([(23,112),(42,94),(49,71),(67,56),(74,32),(103,12)],'#F45115',3);line([(24,110),(44,95),(49,72),(68,56),(76,32)],'#FFB52B',1)
  line([(49,72),(34,62)],'#F45115',2);line([(67,56),(99,63)],'#F45115',2)
 elif name=='ognennoe_pero':
  for i in range(9):
   x=25+i*8;y=103-i*10
   material([(x,y),(x-12,y-7),(x-11,y-24),(x-7,y-29),(x+4,y-9)],'coal');material([(x,y),(x+12,y-8),(x+29,y-8),(x+26,y+1),(x+8,y+7)],'coal');line([(x,y),(x-6,y-22)],'#B92518',2);line([(x,y),(x+24,y-5)],'#F55A16',1)
  line([(16,121),(29,100),(47,78),(66,55),(91,23),(105,10)],'#B92518',6);line([(16,121),(29,100),(47,78),(66,55),(91,23)],'#F55A16',4);line([(19,115),(47,78),(66,55),(91,23)],'#FFC43A',1.5)
 elif name=='obereg_padayuschey_zvezdy':
  ellipse((54,7,81,33),'iron',5);cord([(58,13),(41,10),(22,18),(17,36),(31,40),(53,28)],width=5);cord([(65,29),(67,43)],width=5)
  material([(67,34),(76,53),(87,61),(103,76),(91,85),(82,101),(65,120),(55,106),(41,94),(33,77),(49,61),(54,45)],'coal')
  for x,y in [(55,55),(42,77),(77,54),(91,80),(78,102),(61,109)]:material([(x-6,y),(x,y-7),(x+7,y+2),(x+4,y+8),(x-4,y+6)],'coal')
  star(67,78,22);line([(63,45),(66,64),(74,85),(69,111)],'#B92518',2);cord([(66,31),(44,58),(47,82),(66,99),(87,106)],width=2);cord([(18,35),(15,55),(19,74)],width=4);bead(18,55);bead(19,72,'leather');tassel(19,83)
 elif name=='uzel_podveya':
  for i in range(5):
   a=i*math.pi*2/5;x=63+math.cos(a)*22;y=65+math.sin(a)*21;ellipse((x-17,y-21,x+17,y+21),'blue' if i%2==0 else 'rope',9)
  cord([(32,63),(36,85),(58,97),(83,88),(89,66),(75,43),(52,33),(31,45)],'rope',6);spiral(65,65,17);cord([(88,50),(109,72),(104,87)],'blue',3);bead(104,85);cord([(38,81),(22,92)],'rope',3);bead(23,94,'leather');tassel(22,99,'rope');bead(61,19)
 elif name=='vihrevaya_nit':
  for j in range(3):
   pts=[]
   for i in range(90):
    t=i/89;y=13+t*98;x=64+math.sin(t*math.pi*2.1+j*.25)*(25+9*math.sin(t*math.pi));pts.append((x+j*4,y))
   cord(pts,'wind',8-j*1.5)
  ellipse((22,7,69,41),'wind',2);ellipse((66,88,113,119),'wind',2)
  for x,y in [(19,32),(103,42),(31,108),(86,8),(111,81)]:d.rectangle((x*2,y*2,x*2+4,y*2+4),fill='#B8CFDB')
 elif name=='vetrovoy_uzel':
  cord([(68,24),(48,15),(28,18),(22,31),(30,45)],'leather',6);fang(72,57)
  for i in range(4):material([(55+i*4,23+i*8),(77+i*4,21+i*8),(88+i*3,30+i*8),(64+i*4,34+i*8)],'leather')
  rect((66,37,92,45),'iron');bead(82,39);ellipse((38,40,70,73),'bone',5);spiral(54,56,13);cord([(32,40),(19,70),(23,88)],'leather',3);bead(20,64,'bone');bead(23,87);tassel(23,96,'leather');cord([(52,72),(56,86)],'leather',2);ellipse((48,86,70,111),'iron',6);line([(58,90),(58,106)],'#1E2125',2);line([(52,98),(65,98)],'#1E2125',2)
 elif name in ('sosud_ognennogo_dyhaniya','sosud_podveya'):
  fire=name=='sosud_ognennogo_dyhaniya';key='red' if fire else 'blue'
  material([(45,37),(79,37),(81,48),(96,58),(100,83),(93,106),(78,117),(44,117),(29,107),(24,84),(30,62),(43,49)],'coal');rect((46,14,80,25),'iron');rect((51,25,76,40),'coal');rect((37,44,88,52),'iron');rect((46,110,79,119),'iron');cord([(36,53),(59,57),(88,51),(104,58),(113,80)],key,4)
  if fire:
   star(62,83,22)
   for pts in [[(32,65),(43,71),(39,91),(47,104)],[(89,63),(78,75),(89,91),(80,110)],[(51,25),(64,30),(75,26)]]:line(pts,'#F45B16',2)
  else:spiral(62,83,28);spiral(61,31,7)
  cord([(31,53),(15,61),(11,90)],key,3);bead(13,75,'bone');tassel(12,98,key);cord([(91,53),(103,64),(104,88)],key,3);fang(94,62);tassel(112,94,key);bead(81,54,'leather');bead(48,54,'bone');line([(37,71),(32,85),(38,103)],'#50464A',2)
 size=256 if name in SMALL else 512;path=A/f'textures/item/{name}.png';path.parent.mkdir(parents=True,exist_ok=True);im.resize((size,size),Image.Resampling.NEAREST).save(path)
def atlas(name):
 colors=['#24191A','#35201D','#771D15','#F34E13','#FFC33A','#D7DEE2','#9BA8B1','#292C31','#8DE5FF','#6B2529','#657583','#1E2125','#6B4A32','#C5BBAA','#45494D','#41536A']
 im=Image.new('RGBA',(512,512));d=ImageDraw.Draw(im);rng=random.Random(911)
 for m,col in enumerate(colors):
  x=m%4*128;y=m//4*128;d.rectangle((x,y,x+127,y+127),fill=col)
  for v in range(0,128,4):
   for u in range(0,128,4):
    color=tuple(int(col[i:i+2],16) for i in (1,3,5));grain=rng.choice([-12,-7,0,5,9]);d.rectangle((x+u,y+v,x+u+3,y+v+3),fill=tuple(max(0,min(255,c+grain)) for c in color)+(255,))
  if m in (0,1):
   for v in range(0,128,12):
    for u in range(-8,128,16):
     shift=8 if v//12%2 else 0;d.line([(x+max(0,u+shift),y+v+1),(x+min(127,u+shift+12),y+v+1),(x+min(127,u+shift+14),y+v+9)],fill='#4D3230',width=2)
  if m==3:
   for k in range(0,128,12):d.line([(x+k,y),(x+k+5,y+20),(x+k-2,y+35)],fill='#FFC33A',width=2)
  if m==9:
   for k in range(0,128,10):d.line([(x,y+k),(x+127,y+k)],fill='#934047',width=2)
 im.save(A/f'textures/entity/{name}.png')
 if name=='fire_serpent':
  # Existing Curios atlas layout: only this new star accessory, no old atlas edits.
  star=Image.new('RGBA',(256,256));sd=ImageDraw.Draw(star)
  for i in range(16):
   col={2:'#282323',3:'#FFC13A',5:'#821D20',7:'#F35A17'}.get(i,'#45494D');sd.rectangle((i%4*64,i//4*64,i%4*64+63,i//4*64+63),fill=col)
  star.save(A/'textures/entity/hunt_star_accessory.png')
def merge_tag(path,values):
 obj=json.loads(path.read_text(encoding='utf8')) if path.exists() else {'replace':False,'values':[]};obj['values']=list(dict.fromkeys(obj['values']+values));write(path,obj)
def pool(item,lo=1,hi=1,chance=1):
 p={'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item,'functions':[{'function':'minecraft:set_count','count':{'min':lo,'max':hi}}]}]}
 if chance<1:p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
 return p
def generate():
 langs={l:json.loads((A/f'lang/{l}.json').read_text(encoding='utf8')) for l in ('ru_ru','en_us')}
 for n,(ru,en,tr,te) in ITEMS.items():
  art(n);write(A/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+n}})
  for l,title,tip in [('ru_ru',ru,tr),('en_us',en,te)]:langs[l]['item.slavicmyths.'+n]=title;langs[l]['item.slavicmyths.'+n+'.effect']=tip
 for n,ru,en in [('fire_serpent','Огненный змей','Fire Serpent'),('podvey','Подвей','Podvey')]:
  atlas(n);write(A/f'models/item/{n}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
  for l,title in [('ru_ru',ru),('en_us',en)]:langs[l]['entity.slavicmyths.'+n]=title;langs[l]['item.slavicmyths.'+n+'_spawn_egg']=title+(' — яйцо призыва' if l=='ru_ru' else ' Spawn Egg')
 for l in langs:langs[l]['entity.slavicmyths.serpent_projection']='Огненный двойник' if l=='ru_ru' else 'Fire Projection'
 merge_tag(D/'tags/entity_types/hunt_targets.json',['slavicmyths:fire_serpent','slavicmyths:podvey']);merge_tag(D/'tags/items/hunt_trophies.json',['slavicmyths:'+n for n in ITEMS if n not in ['obereg_padayuschey_zvezdy','vetrovoy_uzel']]);merge_tag(R/'src/main/resources/data/curios/tags/items/necklace.json',['slavicmyths:obereg_padayuschey_zvezdy'])
 rows={'obereg_padayuschey_zvezdy':(['SIS',' F ','STS'],{'S':'slavicmyths:pylayuschaya_cheshuya','I':'minecraft:iron_nugget','F':'slavicmyths:ognennoe_pero','T':'minecraft:string'}),'vetrovoy_uzel':(['TUT','LBL',' S '],{'T':'slavicmyths:vihrevaya_nit','U':'slavicmyths:uzel_podveya','L':'minecraft:leather','B':'minecraft:bone','S':'minecraft:string'})}
 for n,(pat,keys) in rows.items():write(D/f'recipes/{n}.json',{'type':'minecraft:crafting_shaped','pattern':pat,'key':{k:{'item':v} for k,v in keys.items()},'result':{'item':'slavicmyths:'+n}})
 for n,ingredients in [('sosud_ognennogo_dyhaniya',['pustoy_ritualny_sosud','pylayuschaya_cheshuya','pylayuschaya_cheshuya','ognennoe_pero']),('sosud_podveya',['pustoy_ritualny_sosud','uzel_podveya','vihrevaya_nit','vihrevaya_nit'])]:write(D/f'recipes/{n}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'slavicmyths:'+i} for i in ingredients],'result':{'item':'slavicmyths:'+n}})
 scale=pool('pylayuschaya_cheshuya',3,6);scale['entries'][0]['functions']+=[{'function':'minecraft:looting_enchant','count':{'min':0,'max':1},'limit':9}]
 for n,pools in [('fire_serpent',[scale,pool('ognennoe_pero',chance=.35),pool('pustoy_ritualny_sosud',chance=.1)]),('podvey',[pool('uzel_podveya',1,2),pool('vihrevaya_nit',chance=.35),pool('pustoy_ritualny_sosud',chance=.1)])]:write(D/f'loot_tables/entities/{n}.json',{'type':'minecraft:entity','pools':pools})
 messages={'hunt.fire_serpent.day':('Огненного змея можно вызвать только ночью.','The Fire Serpent can be called only at night.'),'hunt.fire_serpent.no_open_sky':('Огненный змей требует открытого неба.','The Fire Serpent needs open sky.'),'hunt.fire_serpent.no_air_space':('Поблизости нет безопасного воздушного пространства для Змея.','No safe air space for the Serpent nearby.'),'hunt.podvey.no_open_sky':('Подвею нужно открытое небо.','Podvey needs open sky.'),'hunt.podvey.biome':('Подвею нужна открытая равнина, снежная равнина или возвышенность.','Podvey needs open plains, snowy plains or highlands.'),'hunt.podvey.position':('Не найдено сухое свободное место для Подвея.','No dry open position for Podvey was found.')}
 advances={'fire_in_the_sky':('Огонь в небе','Fire in the Sky','Победите Огненного змея.','Defeat the Fire Serpent.','ognennoe_pero'),'catch_the_wind':('Поймать ветер','Catch the Wind','Победите Подвея.','Defeat Podvey.','uzel_podveya'),'falling_star':('Падающая звезда','Falling Star','Спаситесь от падения оберегом.','Escape a fall with the Falling Star Charm.','obereg_padayuschey_zvezdy'),'bottled_elements':('Запечатанные стихии','Bottled Elements','Получите оба сосуда стихий.','Obtain both elemental vessels.','sosud_podveya')}
 for n,(ru,en,dr,de,icon) in advances.items():
  obj={'parent':'slavicmyths:hunt_begins','display':{'icon':{'item':'slavicmyths:'+icon},'title':{'translate':'advancements.slavicmyths.'+n+'.title'},'description':{'translate':'advancements.slavicmyths.'+n+'.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}}
  if n=='bottled_elements':obj['criteria']={'event':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['slavicmyths:sosud_ognennogo_dyhaniya']},{'items':['slavicmyths:sosud_podveya']}]}}}
  write(D/f'advancements/{n}.json',obj);messages['advancements.slavicmyths.'+n+'.title']=(ru,en);messages['advancements.slavicmyths.'+n+'.description']=(dr,de)
 for key,(ru,en) in messages.items():langs['ru_ru'][key]=ru;langs['en_us'][key]=en
 for l,data in langs.items():write(A/f'lang/{l}.json',data)
 write(R/'docs/verification/hunt-0.9.1.json',{'version':'0.9.1','items':list(ITEMS),'small_items':sorted(SMALL),'entities':['fire_serpent','podvey'],'recipes':list(rows)+['sosud_ognennogo_dyhaniya','sosud_podveya'],'minecraft_launches':0})
if __name__=='__main__':generate()
