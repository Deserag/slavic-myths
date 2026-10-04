"""Native authored high-resolution clustered pixel assets and exact Hunt I data. Run this layer alone."""
from finalize_065 import A,D,R,write
from water_070 import png
from PIL import Image,ImageDraw
import json,random,math
ITEMS={
 'ovinnaya_zola':('Овинная зола','Barn Ash','Тёплая зола из логова Овинника.','Warm ash left by an Ovinnik.'),
 'iskra_ovinnika':('Искра Овинника','Ovinnik Spark','Даже вне огня она продолжает тлеть.','It keeps smouldering away from the fire.'),
 'klyk_volkolaka':('Клык Волколака','Volkolak Fang','Клык зверя, который когда-то был человеком.','A fang of a beast that once was human.'),
 'nochnoy_kogot':('Ночной коготь','Night Claw','Коготь холоднее ночного воздуха.','The claw is colder than the night air.'),
 'zolny_obereg':('Зольный оберег','Ash Charm','Сдерживает жар, но не делает владельца неуязвимым к огню.','Tempers heat without making its wearer immune to fire.'),
 'volchiy_poyas':('Волчий пояс','Wolf Belt','Ночью ноги сами находят быстрый путь.','At night your feet find a swifter path.'),
 'obereg_ohotnika':('Оберег охотника','Hunter Charm','Помогает отыскать уже потревоженную крупную нечисть.','Finds nearby great spirits already disturbed.'),
 'ohotnichiy_rog':('Охотничий рог','Hunting Horn','Shift + ПКМ — выбрать цель. ПКМ — начать охоту при подходящих условиях.','Sneak + use: select target. Use: begin a hunt when conditions allow.'),
 'meshochek_trofeev':('Мешочек трофеев','Trophy Pouch','Хранит только трофеи охоты. Вместимость: 9 ячеек.','Stores only hunt trophies. Capacity: 9 slots.'),
 'pustoy_ritualny_sosud':('Пустой ритуальный сосуд','Empty Ritual Vessel','Сосуд пуст. Его назначение пока неизвестно.','The vessel is empty. Its purpose is still unknown.')}
INGREDIENTS=list(ITEMS)[:4]
COLORS={'bone':('#8A785B','#C8B58E','#E8DAB9'),'coal':('#171719','#302426','#4B4545'),'ash':('#302C29','#403B37','#6A625A'),'leather':('#4D3025','#704734','#916442'),'wood':('#39281D','#765039','#96704B'),'fur':('#343336','#5A5552','#817B76'),'iron':('#202225','#4B4E50','#828383'),'red':('#51141A','#8E2022','#B3352F'),'ember':('#B92616','#F04B13','#FFC23A'),'moss':('#354925','#4D6437','#6D7B43')}

def art(name):
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);rng=random.Random(900+list(ITEMS).index(name))
 def material(points,key):
  palette=COLORS[key];mask=Image.new('L',im.size);md=ImageDraw.Draw(mask);md.polygon(points,fill=255);layer=Image.new('RGBA',im.size,palette[1]);ld=ImageDraw.Draw(layer)
  for _ in range(600):
   x=rng.randrange(0,128);y=rng.randrange(0,128);w=rng.choice((2,3,4));h=rng.choice((2,3,5));ld.rectangle((x,y,x+w,y+h),fill=rng.choice(palette))
  im.paste(layer,(0,0),mask);d.line(points+[points[0]],fill=palette[0],width=2)
 def rect(box,key):x,y,a,b=box;material([(x,y),(a,y),(a,b),(x,b)],key)
 def cord(points,width=4):d.line(points,fill=COLORS['red'][0],width=width+2);d.line(points,fill=COLORS['red'][1],width=width);[d.rectangle((x-1,y-1,x+1,y+1),fill=COLORS['red'][2]) for x,y in points]
 def tassel(x,y,length=24):cord([(x,y-12),(x,y)],5);[cord([(x+i,y),(x+i//2,y+length-abs(i))],2) for i in range(-6,7,2)]
 def sign(x,y,size=12,color='#A72421'):
  d.line([(x,y-size),(x,y+size)],fill=color,width=3);d.line([(x-size,y),(x+size,y)],fill=color,width=3);d.line([(x,y-size*.65),(x+size*.7,y),(x,y+size*.65),(x-size*.7,y),(x,y-size*.65)],fill=color,width=3)
 def tag(x,y):rect((x,y,x+12,y+17),'bone');sign(x+6,y+8,4);cord([(x+4,y-8),(x+5,y)],2)
 def fang(x,y,s=1):
  pts=[(x,y),(x+10*s,y-1*s),(x+13*s,y+14*s),(x+12*s,y+28*s),(x+6*s,y+43*s),(x+3*s,y+37*s),(x-1*s,y+18*s)];material(pts,'bone');d.line([(x+6*s,y+5*s),(x+8*s,y+23*s),(x+6*s,y+34*s)],fill='#E8DAB9',width=max(1,int(2*s)))
 if name=='ovinnaya_zola':
  material([(8,105),(18,85),(34,75),(43,60),(58,64),(71,53),(81,66),(91,70),(103,88),(119,103),(110,114),(15,114)],'ash')
  for x,y in [(28,91),(47,80),(65,67),(83,86),(99,98)]:
   material([(x-6,y-3),(x,y-9),(x+8,y-5),(x+6,y+5),(x-7,y+5)],'coal');d.rectangle((x-1,y,x+2,y+2),fill='#D43B19');d.point((x+1,y),fill='#F47A1F')
  cord([(80,64),(94,57),(104,67),(115,63)],4)
 elif name=='iskra_ovinnika':
  material([(31,105),(23,82),(38,70),(32,57),(46,52),(44,31),(68,19),(79,13),(94,28),(92,48),(107,62),(95,86),(84,90),(70,112),(44,116)],'coal')
  for pts in [[(76,22),(68,48),(80,60),(64,77),(69,101)],[(43,57),(52,67),(43,84),(36,98)],[(89,53),(80,66),(94,72)]]:
   d.line(pts,fill='#B92616',width=9);d.line(pts,fill='#F04B13',width=5);d.line(pts,fill='#FFC23A',width=2)
  for x,y in [(20,52),(98,29),(21,98),(105,91)]:d.rectangle((x,y,x+3,y+4),fill='#F04B13')
 elif name=='klyk_volkolaka':
  material([(44,31),(62,32),(80,47),(91,66),(91,85),(82,108),(72,119),(72,99),(58,82),(47,60),(38,45)],'bone');d.line([(52,42),(68,61),(80,85),(79,104)],fill='#E8DAB9',width=4)
  for k in range(3):material([(34+k*3,20+k*5),(55+k*3,15+k*5),(64+k*3,21+k*5),(45+k*3,30+k*5)],'leather')
  cord([(39,21),(27,22),(20,47),(25,62),(32,48)],3)
 elif name=='nochnoy_kogot':
  material([(41,30),(65,28),(86,39),(99,56),(105,76),(102,108),(96,94),(83,80),(65,69),(49,60),(35,45)],'coal');d.line([(61,38),(82,53),(95,78)],fill='#48444D',width=4)
  for k in range(3):cord([(35+k*5,25+k*3),(54+k*5,17+k*3),(61+k*5,29+k*3)],4)
  cord([(34,30),(22,52),(27,69)],3);tag(18,70)
 elif name=='zolny_obereg':
  cord([(72,30),(54,9),(34,8),(21,25),(17,54)],5);tassel(17,62,31)
  material([(57,29),(71,20),(85,26),(108,50),(112,78),(102,101),(81,111),(59,105),(42,91),(39,64),(45,43)],'iron')
  material([(57,40),(75,33),(93,44),(102,66),(98,91),(80,102),(57,92),(49,68)],'ash')
  sign(76,69,19,'#CE331B')
  for pts in [[(75,34),(71,50),(78,62)],[(99,73),(86,79),(86,96)],[(54,51),(60,62),(52,81)]]:d.line(pts,fill='#A72D16',width=4);d.line(pts,fill='#FF9327',width=1)
  for x,y in [(52,39),(100,56),(44,81),(97,97),(80,105)]:rect((x-2,y-2,x+2,y+2),'iron');d.point((x,y),fill='#FF9327')
  cord([(67,27),(79,24),(91,34)],3)
 elif name=='volchiy_poyas':
  material([(10,36),(27,25),(96,25),(118,38),(111,81),(93,86),(21,83),(8,63)],'leather')
  d.ellipse((17,24,111,47),fill='#261D19',outline='#916442',width=3)
  for x in range(10,117,7):material([(x,42),(x+6,40),(x+7,57),(x+3,65),(x-1,60)],'fur')
  rect((13,51,113,70),'leather');rect((49,44,78,74),'iron');rect((54,49,73,69),'leather');rect((61,55,66,66),'iron')
  fang(29,73,.85);fang(42,74,.9);rect((80,71,94,113),'red');sign(87,87,7)
  rect((104,68,111,103),'leather');rect((102,91,113,101),'iron')
  for x in (16,90,102):rect((x,57,x+3,61),'iron')
 elif name=='obereg_ohotnika':
  cord([(67,24),(51,8),(28,13),(18,36),(25,56)],5);tassel(22,61,30)
  material([(46,28),(80,22),(101,34),(109,84),(98,102),(57,110),(44,94),(36,43)],'wood')
  for x in (46,53,78,87):d.line([(x,36),(x+9,94)],fill='#4A3424',width=2)
  sign(73,67,22)
  for x,y in [(42,44),(88,27),(98,74),(53,103),(100,94)]:material([(x-4,y),(x,y-5),(x+5,y+1),(x+2,y+6)],'moss')
  rect((53,23,60,31),'bone')
 elif name=='ohotnichiy_rog':
  material([(8,19),(35,10),(47,31),(42,49),(51,68),(66,75),(88,71),(92,77),(90,89),(68,99),(46,95),(25,76),(15,56)],'bone')
  d.ellipse((8,11,39,27),fill='#39281D',outline='#C8B58E',width=3)
  material([(16,49),(44,39),(48,49),(23,64)],'iron');material([(24,65),(52,58),(57,69),(32,80)],'leather');material([(74,72),(83,71),(85,92),(77,96)],'iron');sign(51,82,7)
  tassel(25,82,22);tag(57,99)
  # Right-hand strip is the UV palette used by the physical held geometry.
  for k,key in enumerate(['bone','iron','leather','red','wood','coal','ember','ash']):rect((96,k*16,127,k*16+15),key)
 elif name=='meshochek_trofeev':
  material([(36,29),(76,26),(87,48),(102,64),(100,103),(87,115),(25,111),(17,97),(20,69),(28,50)],'leather');d.ellipse((33,19,81,39),fill='#231A16',outline='#916442',width=4)
  cord([(29,45),(48,49),(72,44),(82,45),(105,62),(115,91)],5);cord([(81,46),(104,56),(116,63)],4)
  for y in range(61,106,7):d.line([(22,y),(28,y+3)],fill='#C8A36C',width=2)
  material([(31,64),(43,67),(39,91),(26,87)],'moss');fang(50,58,.8)
  d.ellipse((73,69,91,95),fill='#4B4E50',outline='#828383',width=3);sign(82,81,5,'#7E1C20');rect((74,53,78,65),'iron')
 elif name=='pustoy_ritualny_sosud':
  material([(47,34),(76,33),(80,48),(95,58),(106,77),(103,100),(93,113),(38,114),(23,100),(23,78),(35,55),(45,47)],'coal');rect((49,17,75,31),'iron');rect((51,30,74,47),'coal');cord([(46,39),(75,37),(85,46)],4);sign(65,82,20);tag(96,50);d.line([(39,65),(31,79),(33,93)],fill='#3A393B',width=4)
 size=256 if name in INGREDIENTS else 512;path=A/f'textures/item/{name}.png';path.parent.mkdir(parents=True,exist_ok=True);im.resize((size,size),Image.Resampling.NEAREST).save(path)

def atlas(name,oven):
 colors=[(36,28,24),(142,107,57),(28,24,22),(241,83,23),(20,17,17),(91,58,43),(255,125,20),(138,32,39),(75,78,80),(216,199,165),(98,93,86),(40,35,32),(52,47,42),(80,67,46),(35,30,27),(61,56,53)] if oven else [(87,84,88),(133,128,128),(38,36,38),(216,199,165),(45,19,23),(91,58,43),(199,30,32),(138,32,39),(75,78,80),(216,199,165),(79,73,75),(40,37,42),(52,47,48),(80,67,46),(35,30,27),(61,56,53)]
 def pixel(x,y):
  mat=x//64+y//64*4;u=x%64;v=y%64;color=colors[mat];grain=((u//2*7+v//3*11)%7)-3
  if mat in (0,2):grain+=-10 if (u+v//7)%9<2 else 8 if u%9==2 else 0
  if mat==1:grain+=15 if (u+v//11)%5==0 else -6
  if mat==3 and oven:color=(255,173,40) if (u+v//3)%7<2 else colors[mat]
  if mat==5:grain+=-12 if v%7==0 else 5
  if mat in (6,):grain=0
  return tuple(max(0,min(255,c+grain)) for c in color)+(255,)
 png(A/f'textures/entity/{name}.png',256,pixel)

def horn_model():
 elements=[]
 def cube(a,b,mat):
  elements.append({'from':a,'to':b,'faces':{f:{'texture':'#material','uv':[12,mat*2,16,mat*2+2]} for f in ['north','south','east','west','up','down']}})
 # A stepped curve tapers from a hollow 7x7 mouth into a narrow raised tip.
 for a,b,mat in [([1,7,4],[3,14,11],0),([6,7,4],[8,14,11],0),([3,7,4],[6,14,5.5],0),([3,7,9.5],[6,14,11],0),([3,7,5.5],[6,8,9.5],5),([1,7,3.8],[8,8.2,5.5],1),([1,7,9.5],[8,8.2,11.2],1),([1,7,5.5],[2.5,8.2,9.5],1),([6.5,7,5.5],[8,8.2,9.5],1),([3,4,5],[8,7.2,10],0),([6,2.8,5.5],[10.5,5.3,9.5],0),([9,3,6],[13,5.3,9],0),([12.5,4,6.4],[15,6.2,8.6],0),([6,2.7,5.3],[7.2,5.6,9.7],2),([11.5,3,5.8],[12.7,5.6,9.2],1),([14,4,6.2],[15.2,6.4,8.8],2),([2.7,4,4.9],[3.4,6.8,5.3],3),([2.2,1,4.8],[4,4,5.4],3),([8,1,5.2],[9.8,3.3,5.7],4),([8.5,1.5,5],[9.3,2.8,5.2],3),([7,4,5.3],[8.8,4.3,5.6],3),([7.7,3.5,5.3],[8,5,5.6],3)]:cube(a,b,mat)
 write(A/'models/item/ohotnichiy_rog.json',{'parent':'minecraft:block/block','gui_light':'front','textures':{'material':'slavicmyths:item/ohotnichiy_rog','particle':'slavicmyths:item/ohotnichiy_rog'},'elements':elements,'display':{'gui':{'rotation':[25,-35,0],'scale':[.85]*3},'ground':{'scale':[.5]*3},'thirdperson_righthand':{'rotation':[0,90,-25],'translation':[0,1,0],'scale':[.65]*3},'firstperson_righthand':{'rotation':[0,75,-20],'translation':[0,0,0],'scale':[.7]*3}}})

def recipe(name,rows,key):write(D/f'recipes/{name}.json',{'type':'minecraft:crafting_shaped','pattern':[r.replace('.',' ') for r in rows if r!='...'],'key':{k:{'item':v} for k,v in key.items()},'result':{'item':'slavicmyths:'+name}})
def pool(item,lo=1,hi=1,chance=1):
 p={'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item,'functions':[{'function':'minecraft:set_count','count':{'min':lo,'max':hi}}]}]}
 if chance<1:p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
 return p
def tagwrite(path,obj):
 if path.exists():obj['values']=list(dict.fromkeys(json.loads(path.read_text(encoding='utf-8'))['values']+obj['values']))
 write(path,obj)

def generate():
 langs={l:json.loads((A/f'lang/{l}.json').read_text(encoding='utf-8')) for l in ('ru_ru','en_us')}
 for name,(ru,en,tipru,tipen) in ITEMS.items():
  art(name)
  if name!='ohotnichiy_rog':write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
  for l,label,tip in [('ru_ru',ru,tipru),('en_us',en,tipen)]:langs[l]['item.slavicmyths.'+name]=label;langs[l]['item.slavicmyths.'+name+'.effect']=tip
 horn_model();atlas('ovinnik',True);atlas('volkolak',False);atlas('hunt_accessories',False)
 for name,ru,en in [('ovinnik','Овинник','Ovinnik'),('volkolak','Волколак','Volkolak')]:
  write(A/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
  for l,label in [('ru_ru',ru),('en_us',en)]:langs[l]['entity.slavicmyths.'+name]=label;langs[l]['item.slavicmyths.'+name+'_spawn_egg']=label+(' — яйцо призыва' if l=='ru_ru' else ' Spawn Egg')
 recipes={
 'ohotnichiy_rog':(['BL.','.BI','S.B'],{'B':'minecraft:bone','L':'minecraft:leather','I':'minecraft:iron_nugget','S':'minecraft:string'}),
 'zolny_obereg':(['ISI','AKA','.A.'],{'I':'minecraft:iron_nugget','S':'minecraft:string','A':'slavicmyths:ovinnaya_zola','K':'slavicmyths:iskra_ovinnika'}),
 'volchiy_poyas':(['LLL','FIF','...'],{'L':'minecraft:leather','F':'slavicmyths:klyk_volkolaka','I':'minecraft:iron_ingot'}),
 'obereg_ohotnika':(['SBS','WCW','.R.'],{'S':'minecraft:string','B':'minecraft:bone','W':'minecraft:oak_planks','C':'slavicmyths:nochnoy_kogot','R':'minecraft:red_dye'}),
 'meshochek_trofeev':(['LSL','L.L','LIL'],{'L':'minecraft:leather','S':'minecraft:string','I':'minecraft:iron_nugget'})}
 for name,(rows,key) in recipes.items():recipe(name,rows,key)
 for name,pools in {'ovinnik':[pool('ovinnaya_zola',8,14),pool('iskra_ovinnika'),pool('iskra_ovinnika',chance=.2),pool('pustoy_ritualny_sosud',chance=.08)],'volkolak':[pool('klyk_volkolaka',2,4),pool('nochnoy_kogot',chance=.35),pool('klyk_volkolaka',chance=.25),pool('pustoy_ritualny_sosud',chance=.08)]}.items():write(D/f'loot_tables/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
 tagwrite(D/'tags/items/hunt_trophies.json',{'replace':False,'values':['slavicmyths:'+n for n in INGREDIENTS+['pustoy_ritualny_sosud']]});tagwrite(D/'tags/entity_types/hunt_targets.json',{'replace':False,'values':['slavicmyths:ovinnik','slavicmyths:volkolak']});write(D/'tags/items/silver_weapons.json',{'replace':False,'values':['slavicmyths:'+n for n in ['silver_sword','silver_dagger','silver_spear','silver_mace','ritual_knife','noon_sickle']]})
 for slot,name in [('necklace','zolny_obereg'),('belt','volchiy_poyas')]:
  p=R/f'src/main/resources/data/curios/tags/items/{slot}.json';data=json.loads(p.read_text());value='slavicmyths:'+name
  if value not in data['values']:data['values'].append(value)
  write(p,data)
 messages={
 'hunt.target':('Цель охоты: %s','Hunt target: %s'),'hunt.fail.dimension':('Охота возможна только в обычном мире.','Hunts require the Overworld.'),'hunt.fail.active':('У вас уже есть активная призванная охота.','You already own an active summoned hunt.'),'hunt.fail.cooldown':('Рог ещё отдыхает: %s сек.','The horn rests for another %s seconds.'),'hunt.fail.daytime':('Для этой охоты нужна ночь.','This hunt requires night.'),'hunt.fail.water':('Нельзя начинать охоту из воды.','You cannot start a hunt in water.'),'hunt.fail.biome':('Волколак отзывается только в лесу или тайге.','Volkolak answers only in forests or taiga.'),'hunt.fail.environment':('В радиусе 18 блоков нет сена, костра или коптильни.','No hay, campfire or smoker within 18 blocks.'),'hunt.fail.position':('Не найдено безопасное место в 12–24 блоках.','No safe position found 12–24 blocks away.'),'hunt.fail.nearby':('Поблизости уже есть такой мини-босс.','A mini-boss of this kind is already nearby.'),'hunt.fail.peaceful':('На Мирной сложности охота недоступна.','Hunts are unavailable in Peaceful.'),'hunt.trace':('След %s: %s, около %s блоков.','Trace of %s: %s, about %s blocks.'),'hunt.no_trace':('Следов крупной нечисти поблизости нет.','No traces of great spirits nearby.'),'hunt.cleared':('Призванная охота остановлена; рог готов.','Owned hunt stopped; horn ready.'),'container.slavicmyths.trophy_pouch':('Мешочек трофеев','Trophy Pouch')}
 for key,ru,en in [('east','восток','east'),('southeast','юго-восток','southeast'),('south','юг','south'),('southwest','юго-запад','southwest'),('west','запад','west'),('northwest','северо-запад','northwest'),('north','север','north'),('northeast','северо-восток','northeast')]:messages['hunt.direction.'+key]=(ru,en)
 advances={'hunt_begins':('Охота началась','The Hunt Begins','Начните охоту с помощью рога.','Start a hunt with the horn.','ohotnichiy_rog'),'extinguish_the_barn':('Загасить овин','Extinguish the Barn','Победите Овинника.','Defeat an Ovinnik.','iskra_ovinnika'),'silver_remedy':('Серебряное средство','Silver Remedy','Победите Волколака.','Defeat a Volkolak.','klyk_volkolaka'),'trophy_keeper':('Добыча охотника','Trophy Keeper','Положите трофей в мешочек.','Store a trophy in the pouch.','meshochek_trofeev')}
 for name,(ru,en,rdesc,edesc,icon) in advances.items():
  write(D/f'advancements/{name}.json',{'parent':'slavicmyths:root','display':{'icon':{'item':'slavicmyths:'+icon},'title':{'translate':'advancements.slavicmyths.'+name+'.title'},'description':{'translate':'advancements.slavicmyths.'+name+'.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}});messages['advancements.slavicmyths.'+name+'.title']=(ru,en);messages['advancements.slavicmyths.'+name+'.description']=(rdesc,edesc)
 sounds=json.loads((A/'sounds.json').read_text(encoding='utf-8'));fallback={}
 for name,family in [('ovinnik','blaze'),('volkolak','wolf')]:
  for cue,event in {'ambient':'entity.'+family+'.'+('ambient' if family=='blaze' else 'growl'),'hurt':'entity.'+family+'.hurt','death':'entity.'+family+'.death','angry':'entity.ravager.roar','power':'entity.ravager.roar'}.items():
   key=name+'_'+cue
   if name=='volkolak':continue # Direct verified vanilla SoundEvents are used for the new wolf cues.
   sub='subtitles.slavicmyths.'+key;sounds[key]={'subtitle':sub,'sounds':[{'name':'minecraft:'+event,'type':'event','pitch':.7}]};fallback[key]=event;messages[sub]=('Овинник: '+{'ambient':'треск','hurt':'ранен','death':'затухает','angry':'рычит','power':'ярость'}[cue],'Ovinnik: '+cue)
 write(A/'sounds.json',sounds)
 sounds['hunt_horn']={'subtitle':'subtitles.slavicmyths.hunt_horn','sounds':[{'name':'minecraft:event.raid.horn','type':'event','volume':100}]}
 # Vanilla raid samples are authored at volume .01; normalize the source rather than broadcast to an entire raid radius.
 messages['subtitles.slavicmyths.hunt_horn']=('Звучит охотничий рог','Hunting horn sounds')
 fallback['hunt_horn']='event.raid.horn'
 write(A/'sounds.json',sounds)
 for key,(ru,en) in messages.items():langs['ru_ru'][key]=ru;langs['en_us'][key]=en
 for l,data in langs.items():write(A/f'lang/{l}.json',data)
 write(R/'docs/verification/hunt-0.9.0.json',{'version':'0.9.0','items':list(ITEMS),'ingredient_resolution':256,'equipment_resolution':512,'entities':['ovinnik','volkolak'],'recipes':list(recipes),'fallbacks':fallback,'minecraft_launches':0})
if __name__=='__main__':generate()
