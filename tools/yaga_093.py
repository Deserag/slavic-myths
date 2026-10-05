"""Scoped native pixel assets and translations for 0.9.3. Never rewrites earlier art."""
from pathlib import Path
from PIL import Image,ImageDraw
import random,math,json
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';D=RES/'data/slavicmyths'
ITEMS=['putevodny_klubok','svyazka_trav_yagi','otvar_ochishcheniya','letuchaya_maz','otvar_lesnoy_zorkosti','yagin_nastoy_stoykosti','otvar_bodrosti','lesnoy_nastoy_vosstanovleniya'];BLOCKS=['yaga_cauldron','yaga_dried_herbs','yaga_bone_charm','yaga_chicken_leg']
def out(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(im,path,size=None):path.parent.mkdir(parents=True,exist_ok=True);im.convert('RGBA').resize(size or im.size,Image.Resampling.NEAREST).save(path)
def sprite(id):
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
 if id=='putevodny_klubok':
  d.line([(46,40),(56,46),(52,55),(40,56),(37,61)],fill='#bda674',width=3);d.ellipse((8,8,51,50),fill='#382421');d.ellipse((10,9,50,48),fill='#873529');d.ellipse((13,10,48,43),fill='#ad4e3c');
  for k in range(7):d.arc((12+k*2,10,47-k,46),70,290,fill=('#d87850' if k%2 else '#653027'),width=2)
  for y in range(17,41,5):d.arc((10,y-7,49,y+9),0,175,fill='#e09666',width=1)
  d.line([(12,31),(45,18)],fill='#d4b27b',width=2);d.line([(15,36),(47,24)],fill='#887150',width=1);d.rectangle((41,47,48,54),fill='#d6c3a1');d.line((43,49,46,52),fill='#785a3e',width=1);d.line([(38,46),(44,48)],fill='#a38b62',width=2)
 elif id=='svyazka_trav_yagi':
  for i in range(11):
   x=12+i*4;y=8+(i%3)*4;d.line((x,y,31,53),fill='#75824c',width=2)
   for j in range(5):xx=x+(31-x)*j/7;yy=y+(53-y)*j/7;d.ellipse((xx-4,yy-1,xx+2,yy+3),fill=['#677743','#4a6240','#9a9551'][i%3])
  d.line((17,37,43,41),fill='#a66a45',width=4);d.line((21,42,40,45),fill='#d4ad71',width=2);d.rectangle((27,40,34,46),fill='#663b2b');d.line((29,46,23,59),fill='#8d4631',width=2)
 elif id=='letuchaya_maz':
  d.polygon([(11,22),(53,22),(50,50),(46,54),(18,54),(14,50)],fill='#302822');d.rectangle((14,27,50,49),fill='#72513b');
  for x in range(16,49,7):d.line((x,27,x,49),fill='#9b7750',width=2)
  d.ellipse((10,13,54,28),fill='#251f1c');d.ellipse((12,12,52,24),fill='#8a6241');d.ellipse((16,14,48,22),fill='#ac8053');d.line((17,17,46,17),fill='#c49a69',width=1);d.rectangle((13,32,51,35),fill='#963b30');d.rectangle((14,44,50,47),fill='#963b30');d.polygon([(28,29),(38,35),(31,43),(24,37)],fill='#b69d69');d.line((26,38,35,33),fill='#4d6341',width=2)
 else:
  hues={'otvar_ochishcheniya':('#9aab56','#d3ca7b'),'otvar_lesnoy_zorkosti':('#637e60','#a3b683'),'yagin_nastoy_stoykosti':('#744d37','#bd8e57'),'otvar_bodrosti':('#b58946','#e0bd71'),'lesnoy_nastoy_vosstanovleniya':('#817753','#bcad75')};c,h=hues[id]
  d.rectangle((26,7,38,17),fill='#392920');d.rectangle((27,8,37,12),fill='#9e7950');d.rectangle((25,15,39,24),fill='#b4b19a');d.polygon([(25,22),(16,31),(14,49),(21,56),(44,56),(50,49),(48,31),(39,22)],fill='#292c28');d.polygon([(26,23),(19,32),(17,48),(22,53),(43,53),(47,48),(45,32),(38,23)],fill='#969c87');d.polygon([(19,34),(17,48),(23,52),(43,52),(47,47),(45,34)],fill=c);d.line((21,34,20,44),fill=h,width=3);d.line((26,25,22,30),fill='#d6d4b8',width=2);d.rectangle((23,35,41,47),fill='#c3b084');d.rectangle((24,36,40,46),fill='#ddca9d');d.line((28,43,36,38),fill='#536543',width=2);d.line((31,40,28,37),fill='#6a7848',width=2);d.line((34,40,37,44),fill='#6a7848',width=2);d.line((24,18,41,18),fill='#8c4932',width=2)
 return im
RU_NAMES=['Путеводный клубок','Связка трав Яги','Отвар очищения','Летучая мазь','Отвар лесной зоркости','Ягин настой стойкости','Отвар бодрости','Лесной настой восстановления'];EN_NAMES=['Wayfinding thread ball',"Yaga’s herb bundle",'Cleansing decoction','Flying salve','Forest-sight decoction',"Yaga’s steadfast infusion",'Vigor decoction','Forest recovery infusion']
RU_E=['Указывает направление и расстояние до избушки. Перерыв: 4 секунды.','Сушёные травы для услуг котла.','Снимает обычные недуги, но не проклятие кургана.','На 10 минут сокращает восстановление после сбора метлы/ступы с 3 до 1,5 секунды.','Ночное зрение I: 1 минута.','Сопротивление I: 30 секунд.','Спешка I: 1 минута.','Регенерация I: 10 секунд.'];EN_E=['Points toward the saved hut. Cooldown: 4 seconds.','Dried herbs for cauldron services.','Removes ordinary ailments; burial curses remain.','For 10 minutes, halves broom/mortar repacking recovery from 3 to 1.5 seconds.','Night Vision I: 1 minute.','Resistance I: 30 seconds.','Haste I: 1 minute.','Regeneration I: 10 seconds.']
for id in ITEMS:
 save(sprite(id),A/f'textures/item/{id}.png',(512,512) if id==ITEMS[0] else (256,256));out(A/f'models/item/{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}})
# Material bands match BabaYagaModel UV origins. Subtle stitches/folds retain pixel surfaces.
im=Image.new('RGBA',(512,512));d=ImageDraw.Draw(im);pal=['#b8a785','#ba9348','#827a70','#3e3433','#6d382f','#614835','#c1b498','#71483b','#cebd94','#292420'];rng=random.Random(93)
for m,c in enumerate(pal):
 x=(m%8)*64;y=(m//8)*64;d.rectangle((x,y,x+63,y+63),fill=c);base=tuple(int(c[i:i+2],16) for i in (1,3,5))
 for j in range(500):
  px=x+rng.randrange(64);py=y+rng.randrange(64);v=rng.choice([-13,-7,5,9]);d.point((px,py),fill=tuple(max(0,min(255,a+v)) for a in base)+(255,))
 if m in (3,4,6):
  for row in range(9,64,12):d.line((x,row+y,x+63,row+y),fill=tuple(max(0,a-12) for a in base)+(255,),width=1)
save(im,A/'textures/entity/baba_yaga.png')
out(D/'loot_tables/entities/baba_yaga.json',{'type':'minecraft:entity','pools':[]})
# World-only service and hanging decorations, no misleading portable crafting workstation.
colors={'yaga_iron':'#3e4038','yaga_liquid':'#595d32','yaga_herbs':'#708050','yaga_bone':'#c6b690','yaga_binding':'#834031','yaga_scales':'#9d8e6e','yaga_claw':'#373832'}
for id,c in colors.items():
 im=Image.new('RGBA',(64,64),c);d=ImageDraw.Draw(im);base=tuple(int(c[i:i+2],16) for i in (1,3,5));rng=random.Random(id)
 for j in range(850):x=rng.randrange(64);y=rng.randrange(64);v=rng.randint(-17,12);d.point((x,y),fill=tuple(max(0,min(255,a+v)) for a in base)+(255,))
 if id=='yaga_iron':
  for y in [5,51,59]:d.line((0,y,63,y),fill='#686650',width=2)
 if id=='yaga_scales':
  for yy in range(0,64,8):
   for xx in range(-8,64,16):
    off=8 if yy%16 else 0;d.rectangle((xx+off,yy,xx+off+14,yy+6),outline='#6b6554');d.line((xx+off+2,yy+1,xx+off+12,yy+1),fill='#b6a47c',width=1)
 if id=='yaga_liquid':
  for i in range(5):d.arc((8+i*5,8+i*5,59-i*4,57-i*4),20,250,fill='#8c9459',width=1)
 save(im,A/f'textures/block/{id}.png')
def cub(a,b,t):return {'from':a,'to':b,'faces':{face:{'uv':[0,0,16,16],'texture':'#'+t} for face in ['north','south','east','west','up','down']}}
caul=[cub([2,3,2],[14,5,14],'iron'),cub([2,5,2],[14,13,4],'iron'),cub([2,5,12],[14,13,14],'iron'),cub([2,5,4],[4,13,12],'iron'),cub([12,5,4],[14,13,12],'iron'),cub([4,5,4],[12,7,12],'liquid')]
for x in [2,11]:
 for z in [2,11]:caul.append(cub([x,0,z],[x+3,4,z+3],'iron'))
for x in [0,14]:caul.append(cub([x,9,6],[x+2,12,10],'iron'))
for z in [1,14]:caul.append(cub([6,7,z],[10,9,z+1],'binding'));caul.append(cub([7,5,z],[9,11,z+1],'bone'))
herbs=[cub([6,12,6],[10,16,10],'binding')]
for i in range(5):herbs.append(cub([3+i*2,2+(i%2),6+(i%2)*2],[5+i*2,13,8+(i%2)*2],'herbs'))
bones=[cub([7,7,7],[9,16,9],'binding'),cub([4,3,7],[12,5,9],'bone'),cub([4,5,7],[6,6,9],'bone'),cub([10,5,7],[12,6,9],'bone'),cub([7,1,7],[9,8,9],'bone')]
for id,el in zip(BLOCKS,[caul,herbs,bones]):
 out(A/f'models/block/{id}.json',{'ambientocclusion':False,'textures':{'particle':'slavicmyths:block/yaga_iron' if id==BLOCKS[0] else 'slavicmyths:block/yaga_herbs','iron':'slavicmyths:block/yaga_iron','liquid':'slavicmyths:block/yaga_liquid','binding':'slavicmyths:block/yaga_binding','bone':'slavicmyths:block/yaga_bone','herbs':'slavicmyths:block/yaga_herbs'},'elements':el});out(A/f'blockstates/{id}.json',{'variants':{'':{'model':'slavicmyths:block/'+id}}});out(D/f'loot_tables/blocks/{id}.json',{'type':'minecraft:block','pools':[]})
# Native anatomy models: thigh, knee, tarsus, long toe and curved dark claw.
leg_shapes=[([3,0,3],[13,16,13]),([3,0,0],[13,10,16]),([5,0,5],[11,16,11]),([6,0,0],[10,4,16]),([6,0,6],[10,4,16]),([6,0,0],[10,4,16]),([5,0,5],[11,16,11])]
for part,(lo,hi) in enumerate(leg_shapes):
 elements=[cub(lo,hi,'claw' if part==4 else 'scales')]
 if part==1:elements.append(cub([4,8,5],[12,14,15],'scales'))
 if part==4:elements.extend([cub([6.5,0,3],[9.5,3,9],'claw'),cub([7,0,0],[9,2,5],'claw')])
 if part==5:elements.append(cub([0,0,12],[16,4,16],'scales'))
 if part==6:elements.append(cub([3,0,0],[13,4,16],'scales'))
 out(A/f'models/block/yaga_leg_{part}.json',{'textures':{'particle':'slavicmyths:block/yaga_scales','scales':'slavicmyths:block/yaga_scales','claw':'slavicmyths:block/yaga_claw'},'elements':elements})
out(A/'blockstates/yaga_chicken_leg.json',{'variants':{f'part={part},facing={face}':{'model':f'slavicmyths:block/yaga_leg_{part}','y':rot} for part in range(7) for face,rot in [('north',0),('east',90),('south',180),('west',270)]}})
out(D/'loot_tables/blocks/yaga_chicken_leg.json',{'type':'minecraft:block','pools':[]})
# 320x236 carved wooden frame inside a power-of-two RGBA sheet.
im=Image.new('RGBA',(512,256));d=ImageDraw.Draw(im);d.rectangle((0,0,319,235),fill='#281b16');d.rectangle((3,3,316,232),fill='#634633');d.rectangle((6,6,313,229),fill='#453026');rng=random.Random(93)
for y in range(4,232,4):d.line((4,y,315,y+rng.randrange(-1,2)),fill='#78543b' if y%8 else '#38261e',width=1)
d.rectangle((106,39,313,122),fill='#b6a07a');d.rectangle((109,42,310,119),fill='#ddcba5');d.rectangle((8,39,102,122),fill='#231e19');d.rectangle((10,41,100,120),outline='#a58450',width=2)
for x in [7,312]:
 for y in [8,30,124,230]:d.rectangle((x-2,y-2,x+2,y+2),fill='#a79768');d.point((x,y),fill='#342a21')
for x in range(15,303,16):d.line((x,145,x+8,142),fill='#a38050',width=1)
d.rectangle((130,145,299,202),fill='#82674c');d.rectangle((130,203,299,226),fill='#82674c');save(im,A/'textures/gui/yaga.png')
# Shared RU/EN keys; descriptions deliberately fit the two opt-in popup panels.
texts={
'help':('?', '?'),'next':('>','>'),'previous':('<','<'),'close':('×','×'),'arrow':('→','→'),
'cauldron':('Котёл Яги',"Yaga’s cauldron"),'tab.talk':('Разговор','Talk'),'tab.quests':('Поручения','Errands'),'tab.exchange':('Обмен','Exchange'),'tab.cauldron':('Котёл','Cauldron'),
'favor.label':('Благосклонность','Favor'),'favor.stranger':('Чужак','Stranger'),'favor.guest':('Гость','Guest'),'favor.trusted':('Доверенный','Trusted'),
'favor.0':('Чужак','Stranger'),'favor.1':('Гость','Guest'),'favor.2':('Гость','Guest'),'favor.3':('Доверенный','Trusted'),
'help.favor.title':('Благосклонность Яги',"Yaga’s favor"),'help.favor.body':('Чужаку Яга ещё не доверяет. Гость получает обмен и новые поручения. Доверенному доступны редкие услуги. Благосклонность растёт после её поручений.','A Stranger has yet to earn trust. Guests unlock exchange and further errands. The Trusted gain rare services. Complete her errands to earn favor.'),
'help.services.title':('Услуги избушки','Hut services'),'help.services.body':('Разговор — советы. Поручения — условия и сдача. Обмен — трофеи за припасы. Котёл — три ингредиента и сосуд. Рецепты видны сразу, варка откроется после Жара и ветра.','Talk gives advice. Errands list tasks. Exchange trades trophies for supplies. The cauldron needs three ingredients and a vessel. Heat and Wind unlocks brewing.'),
'accept':('Принять','Accept'),'submit':('Сдать','Deliver'),'exchange':('Обменять','Exchange'),'brew':('Варить','Brew'),'locked':('Нет доверия','Locked'),'reward':('Награда','Reward'),'reward.hidden':('Сперва заслужи доверие.','First, earn her trust.'),'ingredients':('Ингредиенты','Ingredients'),'vessel':('Сосуд','Vessel'),'cooldown':('Жди: %s с','Wait: %s s'),
'talk.work':('Есть ли для меня работа?','Is there work for me?'),'talk.question':('Спросить','Ask a question'),'talk.forest':('О лесе','About the forest'),'talk.bye':('До встречи','Farewell'),
'dialogue.stage0':('Не всякого гостя кормят. Принеси лесные трофеи — тогда и поговорим.','Not every guest is welcome. Bring forest trophies; then we shall talk.'),'dialogue.stage1':('Гость ты теперь. Принеси жар и ветер в сосудах — научу пользоваться котлом.','You are a Guest now. Bring heat and wind in vessels, and I shall open the cauldron.'),'dialogue.stage2':('Жар и ветер укрощены. Одолей Лихо и Тугарина — силу докажи, а трофеи оставь себе.','Heat and wind are tamed. Defeat Likho and Tugarin to prove your strength. Keep their trophies.'),'dialogue.stage3':('Доверяю тебе. Котёл готов, припасы есть; и для верной руки дело найдётся.','You have my trust. The cauldron is ready, supplies are stocked, and there is work for a steady hand.'),
'dialogue.question':('Ищи хозяев огня и ветра по следам охоты. Лихо и Тугарин — испытания для подготовленного охотника.','Follow hunting clues to the masters of fire and wind. Likho and Tugarin test a prepared hunter.'),'dialogue.forest':('Лес помнит шаги. Клубок ведёт к моей избушке, но через иной мир нить не протянешь.','The forest remembers footsteps. The thread leads to my hut, but cannot reach through another world.'),'dialogue.completed':('Слово сдержал. Бери обещанное.','You kept your word. Take what was promised.'),
'intro.wait':('Избушка молчит. Позови хозяйку ещё раз.','The hut is silent. Call its mistress once more.'),'intro.welcome':('Дерево скрипит, пыль оседает. Яга признала гостя.','Wood creaks and dust settles. Yaga acknowledges her visitor.'),
'warning.first':('Не тронь хозяйку. Второго предупреждения не будет.','Do not strike the mistress. There will be no second warning.'),'warning.expelled':('Незваный гость! Приходи, когда минуют сутки.','Uninvited guest! Return after a day has passed.'),
'fail.uninvited':('Яга не принимает незваного гостя. Подожди сутки.','Yaga refuses an uninvited guest. Wait one day.'),'fail.quest':('Заверши принятое поручение или дождись нового.','Finish your accepted errand or wait for the next one.'),'fail.ingredients':('Не хватает нужных предметов.','Required items are missing.'),'fail.strength':('Сначала одолей и Лихо, и Тугарина. Трофеи Яга не забирает.','Defeat both Likho and Tugarin first. Yaga does not consume their trophies.'),'fail.locked':('Для этой услуги ещё не хватает доверия Яги.','You have not earned enough trust for this service.'),'fail.output':('Освободи выход котла.','Empty the cauldron output.'),'fail.cauldron':('Нужен котёл в избушке Яги.','The cauldron must be inside Yaga’s hut.'),'fail.home':('Подходящее место избушки ещё не найдено.','No suitable hut site has been found yet.'),'fail.exists':('В этом мире избушка Яги уже сохранена.','This world already has a saved Yaga hut.'),'fail.terrain':('Нужна загруженная ровная площадка без воды и построек.','A loaded, level clearing without water or construction is required.'),
'located':('Избушка: %s; размещена: %s','Hut: %s; placed: %s'),'status':('Благосклонность: %s; поручение: %s; отказ: %s','Favor: %s; errand: %s; refused: %s'),
'thread.dimension':('Нить тянется к избушке в обычном мире.','The thread leads to a hut in the Overworld.'),'thread.direction':('Клубок тянется: %s, примерно %s блоков.','The thread pulls %s, about %s blocks.'),
'quest.boss_proof':('Одолеть Лихо и Тугарина. Уникальные трофеи не расходуются.','Defeat Likho and Tugarin. Unique trophies are kept.')}
quests=[('not_empty_handed','Не с пустыми руками','Not empty-handed','Трофеи четырёх охотных существ.','Bring trophies from the four hunting creatures.'),('heat_and_wind','Жар и ветер','Heat and Wind','Принеси оба сосуда стихий.','Bring both elemental vessels.'),('prove_strength','Докажи силу','Prove your strength','Победи двух мировых противников.','Defeat both world adversaries.'),('contract_ovinnik','Зола овинника',"Ovinnik’s ashes",'Новая зола для хозяйки котла.','Fresh ashes for the cauldron mistress.'),('contract_volkolak','Клыки волколака',"Volkolak’s fangs",'Яге нужны охотные клыки.','Yaga needs hunting fangs.'),('contract_serpent','Чешуя огненного змея','Fire Serpent scales','Принеси пылающую чешую.','Bring blazing scales.'),('contract_podvey','Нити подвeя',"Podvey’s threads",'Ветер оставляет полезные нити.','Wind leaves useful threads.'),('contract_likho','Нити дурной доли','Threads of ill fate','Редкое поручение доверенному.','A rare errand for the Trusted.'),('contract_tugarin','Тугаринова кожа',"Tugarin’s hide",'Крепкая кожа для припасов Яги.','Strong hide for Yaga’s supplies.')]
for id,rt,et,rd,ed in quests:texts['quest.'+id+'.title']=(rt,et);texts['quest.'+id+'.desc']=(rd,ed)
for id,ru,en in zip(ITEMS[2:],RU_E[2:],EN_E[2:]):texts['recipe.'+id]=(ru,en)
advs=[('find_the_hut','Найти избушку','Find the hut','Позвать хозяйку лесной избушки.','Call the mistress of the forest hut.'),('not_empty_handed','Не с пустыми руками','Not empty-handed','Выполнить первое поручение Яги.','Complete Yaga’s first errand.'),('guest_of_yaga','Гость Яги',"Yaga’s guest",'Заслужить право на обмен.','Earn access to exchange.'),('trusted_by_yaga','Доверенный Яги','Trusted by Yaga','Одолеть Лихо и Тугарина и сдать поручение.','Defeat Likho and Tugarin and complete the errand.'),('follow_the_thread','За нитью','Follow the thread','Получить или использовать клубок.','Receive or use the thread ball.'),('cauldron_service','Услуги котла','Cauldron service','Сварить отвар в избушке.','Brew a decoction inside the hut.')]
for lang,index,names,effects in [('ru_ru',0,RU_NAMES,RU_E),('en_us',1,EN_NAMES,EN_E)]:
 p=A/f'lang/{lang}.json';loc=json.loads(p.read_text('utf-8'));loc.update({'yaga.'+k:v[index] for k,v in texts.items()});loc['entity.slavicmyths.baba_yaga']=['Баба-яга','Baba Yaga'][index]
 for id,n,e in zip(ITEMS,names,effects):loc['item.slavicmyths.'+id]=n;loc['item.slavicmyths.'+id+'.effect']=e
 for id,n in zip(BLOCKS,[['Котёл Яги','Yaga’s cauldron'],['Сушёные травы Яги','Yaga’s hanging herbs'],['Костяной подвес','Bone charm'],['Куриная лапа избушки','Hut chicken leg']]):loc['block.slavicmyths.'+id]=n[index]
 for id,rt,et,rd,ed in advs:loc['advancements.slavicmyths.'+id+'.title']=[rt,et][index];loc['advancements.slavicmyths.'+id+'.description']=[rd,ed][index]
 out(p,loc)
for id,*_ in advs:
 parent={'find_the_hut':'root','not_empty_handed':'find_the_hut','guest_of_yaga':'not_empty_handed','follow_the_thread':'guest_of_yaga','trusted_by_yaga':'follow_the_thread','cauldron_service':'follow_the_thread'}[id];out(D/f'advancements/{id}.json',{'parent':'slavicmyths:'+parent,'display':{'icon':{'item':'slavicmyths:'+('putevodny_klubok' if id=='follow_the_thread' else 'svyazka_trav_yagi')},'title':{'translate':f'advancements.slavicmyths.{id}.title'},'description':{'translate':f'advancements.slavicmyths.{id}.description'},'frame':'task','show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}})
out(ROOT/'docs/verification/yaga-0.9.3.json',{'version':'0.9.3','items':ITEMS,'world_only_blocks':BLOCKS,'entities':['baba_yaga'],'advancements':[x[0] for x in advs],'png_sizes':{'textures/item/'+id+'.png':([512,512] if id==ITEMS[0] else [256,256]) for id in ITEMS}|{'textures/entity/baba_yaga.png':[512,512],'textures/gui/yaga.png':[512,256]}|{'textures/block/'+id+'.png':[64,64] for id in colors},'minecraft_launches':0,'runtime_verified':False})
print('Generated scoped 0.9.3 assets: 8 items, 4 world-only blocks, NPC atlas, GUI, 6 advancements, RU/EN.')

# Semantic sound IDs use vanilla event fallbacks, with our own localized context.
sound_defs={
 'ambient':('entity.witch.ambient','Яга бормочет','Yaga mutters'),
 'talk':('entity.witch.ambient','Яга отвечает','Yaga answers'),
 'warn':('entity.witch.celebrate','Яга прогоняет гостя','Yaga dismisses a guest'),
 'stir':('block.brewing_stand.brew','Котёл Яги бурлит','Yaga’s cauldron bubbles'),
 'open':('block.chest.open','Яга принимает гостя','Yaga welcomes a guest'),
 'close':('block.chest.close','Яга прощается','Yaga bids farewell'),
 'hurt':('entity.witch.hurt','Яга предупреждает','Yaga warns'),
 'step':('block.bone_block.step','Костяная нога стучит','A bone leg taps'),
 'hut_creak':('block.wooden_door.open','Избушка скрипит','The hut creaks'),
 'hut_step':('entity.iron_golem.step','Избушка топает','The hut stamps')}
p=A/'sounds.json';sounds=json.loads(p.read_text('utf-8'))
for name,(event,ru,en) in sound_defs.items():sounds['yaga_'+name]={'subtitle':'subtitles.slavicmyths.yaga_'+name,'sounds':[{'name':'minecraft:'+event,'type':'event'}]}
out(p,sounds)
for lang,i in [('ru_ru',1),('en_us',2)]:
 p=A/f'lang/{lang}.json';loc=json.loads(p.read_text('utf-8'));loc.update({'subtitles.slavicmyths.yaga_'+n:v[i] for n,v in sound_defs.items()});out(p,loc)
