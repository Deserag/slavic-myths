"""Scoped World Bosses resources. Run after Hunt II; preserves existing models and data."""
from finalize_065 import R,A,D,write,shaped
from hunt_091 import merge_tag,pool
from PIL import Image,ImageDraw
import math,json
ITEMS={
'uzel_durnoy_doli':('Узел дурной доли','Ill Fate Knot','Ритуал 3 сек.: ночью в Тёмном лесу, на свободной площадке. Расходуется только при успешном призыве.','Three-second ritual: night in Dark Forest on open ground. Consumed only on success.'),
'oko_likha':('Око Лиха','Likho Eye','Трофей Лиха; компонент оберега и будущих ритуалов.','Likho trophy; charm and future ritual ingredient.'),
'nit_durnoy_doli':('Нить дурной доли','Ill Fate Thread','Чёрная нить с костяными бусинами; ритуальный компонент.','Black thread with bone beads; ritual ingredient.'),
'kost_likha':('Кость Лиха','Likho Bone','Длинная кость с меткой глаза; компонент оберега.','Long eye-marked bone; charm ingredient.'),
'odnoglazyy_obereg':('Одноглазый оберег','One-Eyed Charm','В слоте ожерелья: 20% шанс сократить новое временное отрицательное воздействие вдвое; перезарядка 30 сек. Не снимает постоянное проклятие кургана.','Necklace slot: 20% chance to halve a new timed harmful effect; 30-second cooldown. Does not remove persistent Kurgan Curse.'),
'stepnoy_shtandart':('Степной штандарт','Steppe Standard','Ритуал 3 сек.: открытая равнина или саванна под небом. Расходуется только при успешном призыве.','Three-second ritual: open plains or savanna under the sky. Consumed only on success.'),
'us_tugarina':('Ус Тугарина','Tugarin Moustache','Трофей Тугарина; компонент пояса и будущих ритуалов.','Tugarin trophy; belt and future ritual ingredient.'),
 'tugarinova_kozha':('Тугаринова кожа','Tugarin Hide','Грубая кожа со змеиным узором; компонент пояса.','Rough hide with a snake pattern; belt ingredient.'),
'zmeinaya_pryazhka':('Змеиная пряжка','Snake Buckle','Потёртая латунь и поднятая змеиная спираль.','Worn brass and a raised coiled snake.'),
'naboyka_tugarina':('Набойка Тугарина','Tugarin Heel Plate','Тяжёлая железная набойка. Трофей без активного действия.','Heavy iron heel plate. Trophy with no active ability.'),
'poyas_tugarina':('Пояс Тугарина','Tugarin Belt','В слоте пояса: после 2 сек. непрерывного спринта Напор даёт +0.20 сопротивления отбрасыванию и +15% отбрасывания в ближнем бою. Спадает через 1 сек. после остановки.','Belt slot: after two seconds of continuous sprint, Momentum grants +0.20 knockback resistance and +15% melee knockback. Ends one second after stopping.')}
SMALL={'nit_durnoy_doli','kost_likha','tugarinova_kozha','naboyka_tugarina'}
def art(n):
 im=Image.new('RGBA',(256,256));d=ImageDraw.Draw(im)
 def cord(pts,c='#632126',w=7):d.line(pts,fill='#171719',width=w+4);d.line(pts,fill=c,width=w);d.line([(x-1,y-1) for x,y in pts],fill='#81766A',width=1)
 def bead(x,y):d.polygon([(x-7,y-8),(x+6,y-8),(x+9,y),(x+5,y+9),(x-6,y+7),(x-8,y)],fill='#C2AF8B',outline='#88775B');d.rectangle((x-2,y-2,x+2,y+3),fill='#302622')
 def ellipse(b,c,w=6):d.ellipse(b,fill=c,outline='#241713',width=3)
 def snake(x,y,r):
  pts=[]
  for i in range(120):a=i*math.pi*3.5/119;v=r*(1-i/155);pts.append((x+math.cos(a)*v,y+math.sin(a)*v))
  cord(pts,'#CB9B42',7);d.polygon([(pts[-1][0]-6,pts[-1][1]-4),(pts[-1][0]+6,pts[-1][1]-3),(pts[-1][0]+3,pts[-1][1]+6)],fill='#CB9B42');d.rectangle((pts[-1][0],pts[-1][1],pts[-1][0]+2,pts[-1][1]+2),fill='#241713')
 def eye(x,y,r):ellipse((x-r,y-r*.65,x+r,y+r*.65),'#D5C59B');ellipse((x-r*.42,y-r*.48,x+r*.42,y+r*.48),'#B94B26');ellipse((x-3,y-9,x+3,y+9),'#241713')
 def tassel(x,y):
  for i in range(-5,6,2):cord([(x+i,y),(x+i*1.8,y+33-abs(i))],'#632126',3)
 if n in ['uzel_durnoy_doli','nit_durnoy_doli']:
  for j in range(4 if n=='uzel_durnoy_doli' else 2):
   pts=[(128+math.sin(i*.095+j*.8)*(53-j*6),45+i*1.35) for i in range(110)];cord(pts,'#302622' if j%2 else '#632126',10-j)
  cord([(99,153),(76,187),(73,218)],'#171719',7);cord([(155,79),(201,105),(207,171)],'#632126',6)
  for x,y in [(78,190),(192,105),(206,151)]:bead(x,y)
  ellipse((104,106,144,137),'#88775B');eye(124,120,13)
  if n=='uzel_durnoy_doli':d.polygon([(69,156),(51,213),(67,231),(88,179)],fill='#171719');cord([(76,161),(65,217)],'#353136',2)
 elif n in ['oko_likha','odnoglazyy_obereg']:
  cord([(101,69),(76,25),(110,14),(146,18),(179,37),(153,78)],'#171719',8)
  ellipse((56,66,199,188),'#25272B');ellipse((67,75,188,177),'#88775B' if n=='oko_likha' else '#3C2822');eye(128,126,51 if n=='oko_likha' else 43)
  for a in [0,math.pi/2,math.pi]:x=128+math.cos(a)*66;y=126+math.sin(a)*60;cord([(x,y),(x+11,y+35)],'#632126',4);bead(x+11,y+35)
  if n=='odnoglazyy_obereg':bead(128,190);tassel(129,206);d.rectangle((97,64,157,73),fill='#C2AF8B');d.rectangle((98,177,158,186),fill='#C2AF8B')
 elif n=='kost_likha':
  d.polygon([(108,26),(126,15),(145,21),(146,43),(130,75),(124,175),(142,211),(138,231),(114,241),(98,226),(101,209),(108,174),(110,73),(101,46)],fill='#C9BAA5',outline='#81766A');d.line((122,56,116,183),fill='#E0D2BC',width=4)
  for y in [113,125,137]:cord([(104,y),(132,y+4)],'#171719',7)
  eye(126,47,13)
 elif n=='stepnoy_shtandart':
  d.polygon([(45,19),(58,18),(74,232),(60,235)],fill='#3C2822',outline='#81766A');d.rectangle((43,11,59,28),fill='#25272B')
  d.polygon([(58,38),(211,45),(218,150),(197,175),(180,153),(163,169),(135,151),(89,160),(72,151)],fill='#632126',outline='#47191D');d.line((66,49,202,54),fill='#842B2D',width=7);snake(139,97,33)
  for y in [48,66,148]:cord([(49,y),(74,y+2)],'#5A392B',5)
  cord([(196,56),(227,83),(226,120)],'#632126',4);tassel(226,122);cord([(74,54),(84,102),(80,136)],'#632126',4);tassel(80,139)
 elif n=='us_tugarina':
  cord([(128,91),(109,81),(83,79),(52,109),(39,158),(53,186),(71,190)],'#171719',23);cord([(127,91),(157,74),(184,82),(206,107),(215,151),(202,183),(178,193)],'#171719',24)
  for shift in [-7,0,6]:cord([(70+shift,100),(53+shift,137),(53+shift,167)],'#353136',2);cord([(180+shift,91),(202+shift,132),(200+shift,161)],'#353136',2)
  ellipse((106,64,148,120),'#A9792F');ellipse((116,75,138,110),'#171719');cord([(128,111),(141,153),(134,213)],'#632126',6);tassel(134,213)
 elif n=='tugarinova_kozha':
  d.polygon([(68,23),(94,42),(129,30),(165,17),(174,47),(213,62),(203,98),(219,154),(182,167),(175,212),(145,201),(114,231),(96,211),(49,206),(62,163),(29,138),(44,96),(30,57),(62,59)],fill='#94553F',outline='#3C2822');d.line([(62,74),(77,108),(66,159),(96,185),(124,209)],fill='#C47C56',width=5);cord([(118,76),(153,70),(171,92),(139,111),(132,134),(158,147)],'#6D3A31',7);d.polygon([(115,68),(130,70),(125,81)],fill='#6D3A31')
 elif n in ['zmeinaya_pryazhka','poyas_tugarina']:
  if n=='poyas_tugarina':
   d.polygon([(13,84),(64,66),(193,69),(245,91),(243,158),(192,175),(60,169),(16,151)],fill='#5A392B',outline='#241713');d.line((23,87,232,96),fill='#94553F',width=4)
   for x in [32,57,194,219]:d.rectangle((x,100,x+14,145),fill='#A9792F',outline='#CB9B42')
   for x,y in [(73,161),(173,167)]:cord([(x,y),(x-7,y+55)],'#171719',9);d.rectangle((x-14,y+49,x,y+61),fill='#A9792F')
  ellipse((73,71,186,176),'#25272B');ellipse((83,81,176,166),'#A9792F');snake(130,124,35)
 elif n=='naboyka_tugarina':
  d.polygon([(66,56),(160,41),(206,80),(205,176),(172,211),(69,195),(43,148)],fill='#25272B',outline='#81766A');d.polygon([(64,69),(158,56),(193,86),(191,161),(168,191),(72,179),(55,145)],fill='#171719');d.line([(48,147),(79,198),(169,213),(203,179)],fill='#45494D',width=8)
  for x,y in [(76,83),(171,80),(91,162),(171,169)]:ellipse((x-7,y-7,x+7,y+7),'#A9792F')
  snake(132,125,23)
 im=im.resize((256 if n in SMALL else 512,)*2,Image.Resampling.NEAREST);im.save(A/f'textures/item/{n}.png')

def generate():
 langs={l:json.loads((A/f'lang/{l}.json').read_text('utf8')) for l in ['ru_ru','en_us']}
 for n,(ru,en,tr,te) in ITEMS.items():
  art(n);write(A/f'models/item/{n}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+n}})
  for l,title,tip in [('ru_ru',ru,tr),('en_us',en,te)]:langs[l]['item.slavicmyths.'+n]=title;langs[l]['item.slavicmyths.'+n+'.effect']=tip
 for n,ru,en in [('likho_one_eyed','Лихо Одноглазое','Likho the One-Eyed'),('tugarin_zmey','Тугарин Змей','Tugarin Zmey')]:
  write(A/f'models/item/{n}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
  for l,title in [('ru_ru',ru),('en_us',en)]:langs[l]['entity.slavicmyths.'+n]=title;langs[l]['item.slavicmyths.'+n+'_spawn_egg']=title+(' — яйцо призыва' if l=='ru_ru' else ' Spawn Egg')
 write(D/'tags/entity_types/boss_targets.json',{'replace':False,'values':['slavicmyths:likho_one_eyed','slavicmyths:tugarin_zmey']})
 merge_tag(D/'tags/items/hunt_trophies.json',['slavicmyths:'+n for n in ITEMS if n not in ['odnoglazyy_obereg','poyas_tugarina']])
 for n,slot in [('odnoglazyy_obereg','necklace'),('poyas_tugarina','belt')]:merge_tag(R/f'src/main/resources/data/curios/tags/items/{slot}.json',['slavicmyths:'+n])
 shaped('stepnoy_shtandart',['RSR','LVL',' W '],{'R':'minecraft:red_wool','S':'pylayuschaya_cheshuya','L':'minecraft:leather','V':'sosud_ognennogo_dyhaniya','W':'minecraft:stick'})
 shaped('odnoglazyy_obereg',['BTB','IEI',' K '],{'B':'minecraft:bone','T':'nit_durnoy_doli','I':'minecraft:iron_nugget','E':'oko_likha','K':'kost_likha'})
 shaped('poyas_tugarina',['LHL','SBS',' L '],{'L':'tugarinova_kozha','H':'us_tugarina','S':'minecraft:leather','B':'zmeinaya_pryazhka'})
 write(D/'recipes/uzel_durnoy_doli.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'slavicmyths:'+i} for i in ['sosud_podveya','nochnoy_kogot','vihrevaya_nit','uzel_podveya']]+[{'item':'minecraft:string'}],'result':{'item':'slavicmyths:uzel_durnoy_doli'}})
 for boss,rows in [('likho_one_eyed',[('oko_likha',1,1,1),('nit_durnoy_doli',2,4,1),('kost_likha',1,1,.35),('pustoy_ritualny_sosud',1,1,.12)]),('tugarin_zmey',[('us_tugarina',1,1,1),('tugarinova_kozha',3,6,1),('zmeinaya_pryazhka',1,1,.45),('naboyka_tugarina',1,1,.30)])]:
  pools=[pool(*row) for row in rows];pools[1]['entries'][0]['functions'].append({'function':'minecraft:looting_enchant','count':{'min':0,'max':1}});write(D/f'loot_tables/entities/{boss}.json',{'type':'minecraft:entity','pools':pools})
 msgs={'effect.slavicmyths.durnaya_dolya':('Дурная доля','Ill Fate'),'boss.fail.dimension':('Ритуал возможен только в обычном мире.','The ritual requires the Overworld.'),'boss.fail.active':('Ваш прежний призыв ещё активен, в том числе в выгруженных чанках.','Your previous summon is still active, including unloaded chunks.'),'boss.fail.nearby':('Другой такой босс слишком близко.','Another boss of this kind is too close.'),'boss.fail.peaceful':('Ритуал невозможен в мирной сложности.','The ritual cannot run on Peaceful difficulty.'),'boss.fail.biome':('Лихо требует Тёмного леса; Тугарин — равнины или саванны.','Likho requires Dark Forest; Tugarin requires plains or savanna.'),'boss.fail.night':('Лихо можно призвать только ночью.','Likho can only be summoned at night.'),'boss.fail.sky':('Для Тугарина нужен открытый небосвод.','Tugarin requires an open sky.'),'boss.fail.position':('Не найдено безопасное свободное место. Предмет сохранён.','No safe open position found. The item was preserved.'),'boss.summoned':('Призван: %s','Summoned: %s'),'boss.located':('Точка %s: %s; активен: %s','Anchor %s: %s; active: %s'),'boss.not_found':('Среди изученных регионов точка не найдена.','No anchor found among explored regions.'),'boss.cleared':('Ваш ритуальный бой сброшен.','Your ritual encounter has been cleared.')}
 advances=[('name_your_misfortune','Назови своё несчастье','Name Your Misfortune','Победите Лихо Одноглазое.','Defeat Likho the One-Eyed.','oko_likha'),('steppe_champion','Степной богатырь','Steppe Champion','Победите Тугарина Змея.','Defeat Tugarin Zmey.','us_tugarina'),('ill_fate_broken','Доля переломлена','Ill Fate Broken','Создайте Одноглазый оберег.','Obtain a One-Eyed Charm.','odnoglazyy_obereg'),('full_speed_ahead','Полный напор','Full Speed Ahead','Активируйте Напор пояса Тугарина.','Activate Tugarin Belt Momentum.','poyas_tugarina')]
 for id,ru,en,dr,de,icon in advances:
  criteria={'event':{'trigger':'minecraft:impossible'}}
  if id=='ill_fate_broken':criteria={'event':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['slavicmyths:odnoglazyy_obereg']}]}}}
  write(D/f'advancements/{id}.json',{'parent':'slavicmyths:hunt_begins','display':{'icon':{'item':'slavicmyths:'+icon},'title':{'translate':'advancements.slavicmyths.'+id+'.title'},'description':{'translate':'advancements.slavicmyths.'+id+'.description'},'frame':'challenge','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':criteria})
  msgs['advancements.slavicmyths.'+id+'.title']=(ru,en);msgs['advancements.slavicmyths.'+id+'.description']=(dr,de)
 for k,(ru,en) in msgs.items():langs['ru_ru'][k]=ru;langs['en_us'][k]=en
 for l,data in langs.items():write(A/f'lang/{l}.json',data)
 effect=Image.new('RGBA',(64,64));ed=ImageDraw.Draw(effect);ed.line([(12,53),(20,37),(11,24),(19,12),(41,13),(50,27),(41,39),(22,39),(16,26),(25,19),(37,21),(42,29),(33,32),(28,27),(34,24)],fill='#302622',width=6);ed.line([(12,53),(20,37),(11,24),(19,12),(41,13),(50,27),(41,39)],fill='#632126',width=2);ed.ellipse((26,22,39,30),fill='#88775B');ed.ellipse((31,24,34,28),fill='#241713');effect.save(A/'textures/mob_effect/durnaya_dolya.png')
 write(R/'docs/verification/hunt-0.9.2.json',{'version':'0.9.2','items':list(ITEMS),'small_items':sorted(SMALL),'entities':['likho_one_eyed','tugarin_zmey'],'recipes':['uzel_durnoy_doli','stepnoy_shtandart','odnoglazyy_obereg','poyas_tugarina'],'minecraft_launches':0})
 # Actual authored item sheet for visual inspection.
 sheet=Image.new('RGBA',(768,512),'#20232A');sd=ImageDraw.Draw(sheet)
 for i,n in enumerate(ITEMS):sheet.alpha_composite(Image.open(A/f'textures/item/{n}.png').resize((128,128),Image.Resampling.NEAREST),(i%6*128,i//6*256));sd.text((i%6*128,i//6*256+132),n[:20],fill='white')
 sheet.convert('RGB').save(R/'docs/verification/world-bosses-0.9.2/items.png')
if __name__=='__main__':generate()
