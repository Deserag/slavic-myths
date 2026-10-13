"""Final 1.8.5 overlay: canonical pouch, recipe and early-cleansing dialogue."""
from pathlib import Path
import json
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]; A=ROOT/'src/main/resources/assets/slavicmyths'
def write(p,n):p.write_text(json.dumps(n,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def patch():
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 d.polygon([(5,2),(7,3),(8,2),(9,3),(11,2),(10,5),(12,8),(13,12),(11,14),(4,14),(2,12),(3,8),(6,5)],fill='#B2A38E',outline='#776651')
 d.line([(4,8),(3,11),(5,13)],fill='#D6CABB');d.line([(11,7),(12,12),(10,13)],fill='#66553F');d.line((5,5,11,5),fill='#66553F',width=1)
 for x in [4,7,10]:d.arc((x,4,x+2,7),0,320,fill='#66553F')
 d.polygon([(8,7),(11,10),(8,13),(5,10)],outline='#712E32');d.line((8,8,8,12),fill='#A34C4F');d.point((8,10),fill='#712E32')
 for pos in [(5,2),(8,2),(11,3),(4,15),(12,15)]:d.point(pos,fill='#EAE3D4')
 d.point((13,15),fill='#B3D8A0');im.save(A/'textures/item/cleansing_powder.png')
 atlas=Image.new('RGBA',(16,16),'#B2A38E');ad=ImageDraw.Draw(atlas)
 for x,y,color in [(8,0,'#66553F'),(0,8,'#712E32'),(8,8,'#D6CABB')]:ad.rectangle((x,y,x+7,y+7),fill=color)
 atlas.save(A/'textures/item/cleansing_materials.png')
 model=json.loads((A/'models/item/cleansing_powder_held.json').read_text(encoding='utf-8'))
 model['textures']={'surface':'slavicmyths:item/cleansing_materials','particle':'slavicmyths:item/cleansing_powder'}
 def box(lo,hi,uv):return {'from':lo,'to':hi,'faces':{f:{'texture':'#surface','uv':uv} for f in ['up','down','north','south','east','west']}}
 model['elements']=[box([4,3,5],[12,7.5,11],[0,0,7,7]),box([5,7.5,5.5],[11,9.3,10.5],[0,0,7,7]),box([6,9.3,6],[10,11,10],[8,8,15,15]),box([5.5,8.7,5.3],[10.5,9.3,10.7],[8,0,15,7])]
 for x in [5.5,7.5,9.5]:model['elements'].append(box([x,8.4,5.1],[x+.5,9.8,5.5],[8,0,15,7]))
 for width,y in [(1,4),(3,5),(5,6),(3,7),(1,8)]:model['elements'].append(box([8-width/2,y,4.8],[8+width/2,y+.45,5],[0,8,7,15]))
 write(A/'models/item/cleansing_powder_held.json',model)
 for name,color in [('purify_spark','#EAE3D4'),('purify_green_spark','#B3D8A0'),('cleansed_land_ward','#B3D8A0')]:
  im=Image.new('RGBA',(8,8));d=ImageDraw.Draw(im)
  if name=='purify_spark':d.rectangle((3,3,4,4),fill=color);d.point((1,5),fill='#BFB6A3')
  else:d.line((3,1,3,6),fill=color);d.line((1,3,6,3),fill=color)
  im.save(A/f'textures/particle/{name}.png');write(A/f'particles/{name}.json',{'textures':['slavicmyths:'+name]})
 write(ROOT/'src/main/resources/data/slavicmyths/recipe/cleansing_powder.json',{'type':'minecraft:crafting_shaped','pattern':['BDB',' C ','   '],'key':{'B':{'item':'minecraft:bone_meal'},'D':{'item':'minecraft:dandelion'},'C':{'item':'minecraft:charcoal'}},'result':{'id':'slavicmyths:cleansing_powder','count':4}})
 ru={'study_eye':'Исследовать Око','earth_title':'Возвращение земли','earth_training':'Научи очищать','virtual_clean':'Очистить образцы','try_myself':'Попробую сам','ask_recipe':'Покажи рецепт','earth_progress':'Возвращение земли: %s/20','earth_patch':'Учебный участок: %s, %s, %s','land_protected':'Защита земли: %s с','powder_hint':'Радиус 4, до 9 связанных блоков за 3 с. Защита земли на 90 с.','cleansing_started':'Очищение началось: до 9 связанных блоков за 3 секунды','objective.5':'Победите Полоза или подтвердите прежнюю победу. Вернитесь к Яге: финал истории и 800 XP.','objective.2':'Предъявите Око Лиха для раннего очищения. Исследуйте Лихо и Овинника; покажите Око и Сердце без изъятия.'}
 dialogue={'early.0':'Гляжу, Одноглазое ты одолел. Око своё покажи-ка, не бойся: не заберу. Хворь глубоко в нём сидит.','early.1':'А ведь есть способ эту хворь из земли вывести! Не всё ещё потеряно. Слушай да запоминай: костная мука, одуванчик да древесный уголь.','early.2':'Две горсти костной муки по краям, посреди — цветок, под ним уголь. В верстаке получишь четыре порции. Вот тебе первые четыре — попробуй.','early.3':'Найди больную землю, рассыпь порошок и верни ей прежний облик. Двадцать камней да клочков земли вылечишь — дам ещё припас. В журнале выбери «Научи очищать» или попробуй сам.','early.cat':'Даже Мор не любит, когда за ним прибирают. Поверь коту: у чистой земли память долгая, хоть и ненадолго.','early.wolf':'Ветер ещё несёт дурной запах. Но тропой чистой теперь можно идти не опасаясь — хоть какое-то облегчение.','early.reward':'Вижу, земля снова дышит. Впрок возьми ещё восемь порций, пригодятся. А беда-то большая, дальше копать надо.','early.lesson':'У меня припасены камень да земля, что мор уже тронул. На них и научишься. Здесь учебные образцы: один порошок очищает до девяти за три секунды. Настоящий мир эта демонстрация не меняет.','ritual.5':'Одолел великого змея, а след-то его на земле остался. Но ты теперь знаешь, чем помочь. Есть ещё куда хворь выгонять.','ritual.end':'Полоз пал, не сама Хворь. Береги мир: откуда пришло это зло, мы ещё только узнаем.'}
 english={'study_eye':'Study the Eye','earth_title':'Returning the Land','earth_training':'Teach cleansing','virtual_clean':'Clean samples','try_myself':'Try myself','ask_recipe':'Show recipe','earth_progress':'Returning the Land: %s/20','earth_patch':'Training patch: %s, %s, %s','land_protected':'Land protection: %s s','powder_hint':'Radius 4, up to 9 connected blocks in 3 s; 90 s land protection.'}
 for locale in ['ru_ru','en_us']:
  p=A/f'lang/{locale}.json';data=json.loads(p.read_text(encoding='utf-8'))
  for k,v in (ru if locale=='ru_ru' else {**ru,**english}).items():data['quest185.'+k]=v
  for k,v in dialogue.items():data['quest185.dialogue.'+k]=v
  bodies={'mor_investigation':'Яга расследует чужую хворь. Три моровых, три праха, высшие формы, рог и предостережение. После исследования Ока Лиха доступны очищение и «Возвращение земли»; Овинник и Полоз для этого не нужны. После Полоза — финал расследования.','cleansing_knowledge':'Крафт доступен без сюжета: костная мука — одуванчик — костная мука; древесный уголь под цветком. Результат: 4 порошка. ПКМ по заражённому блоку очищает до 9 связанных блоков в радиусе 4 за 3 секунды. Земля защищена от заражения на 90 секунд. После Ока Яга даёт 4 порошка; за 20 вылеченных блоков — 8 и 175 XP. Разрушенные дома не восстанавливаются.'}
  for entry,body in bodies.items():
   for stage in ['known','rumor','seen','verified','discovered','studied']:data[f'tales.entry.{entry}.{stage}']=body
  write(p,data)
if __name__=='__main__':patch()

if __name__=="__main__":
 from gorynych_assets import data as gorynych_data
 gorynych_data()
