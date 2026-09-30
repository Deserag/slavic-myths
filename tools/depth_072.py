"""Own Elder geometry materials, subdued water sounds and depth progression data."""
from finalize_065 import *
from water_070 import png
from artifact_065 import advance
import sys
def generate():
 tex={'wood':'minecraft:block/dark_oak_log','rope':'minecraft:block/brown_terracotta','iron':'minecraft:block/iron_block','dark':'minecraft:block/gray_concrete','pearl':'minecraft:block/cyan_terracotta','edge':'minecraft:block/light_gray_terracotta','moss':'minecraft:block/mossy_cobblestone','particle':'minecraft:block/cyan_terracotta'}
 display={'gui':{'rotation':[20,135,0],'scale':[.75]*3},'ground':{'scale':[.5]*3},'thirdperson_righthand':{'rotation':[0,90,0],'scale':[.8]*3},'firstperson_righthand':{'rotation':[0,90,0],'scale':[.8]*3}}
 pearl=[cube([5,5,6],[10,10,10],'edge',([8,8,8],'z',22.5)),cube([6,6,5.7],[10,9.7,10.2],'pearl'),cube([8.8,6.7,5.4],[10.3,8.5,6.2],'edge')]
 model('item/pool_pearl',pearl,tex,display)
 sign=[cube([4,4,7],[10,12,8.5],'moss',([8,8,8],'z',-22.5)),cube([5,6,6.8],[9,6.4,7],'pearl'),cube([6,8,6.8],[10,8.4,7],'pearl'),cube([5,10,6.8],[8,10.4,7],'pearl')];model('item/ancient_water_sign',sign,tex,display)
 spear=[cube([7.4,-7,7.4],[8.6,16,8.6],'wood'),cube([7,14,7],[9,18,9],'dark'),cube([7,17,7.5],[9,22,8.5],'iron'),cube([7.5,22,7.7],[8.5,25,8.3],'iron'),cube([6,17,7.6],[7,20,8.4],'iron',([7,18,8],'z',-22.5)),cube([8.8,18,7.6],[9.5,20,8.4],'iron',([9,18,8],'z',22.5)),cube([7.4,15,6.6],[8.6,16.5,7.4],'pearl')]
 for y in range(1,9,2):spear.append(cube([7.1,y,7.1],[8.9,y+.5,8.9],'rope'))
 for y in (18,20,22):spear.append(cube([7.8,y,7.3],[8.2,y+.4,7.6],'pearl'))
 sd=json.loads(json.dumps(display));sd['gui']['scale']=[.45]*3
 for hand in ('thirdperson_righthand','firstperson_righthand'):sd[hand]={'rotation':[0,0,-8],'scale':[1,1,1]}
 model('item/pool_spear',spear,tex,sd)
 amulet=[cube([4,9,7],[4.5,15,7.5],'rope'),cube([11.5,9,7],[12,15,7.5],'rope'),cube([4,14.5,7],[12,15,7.5],'rope'),cube([5,3,6.5],[11,9,8.5],'dark',([8,6,8],'z',22.5)),cube([6.5,4.5,6.1],[10,8,6.6],'pearl')]
 for y in (4,6,8):amulet.append(cube([5.4,y,6.2],[6.2,y+.5,6.5],'edge'))
 model('item/depth_amulet',amulet,tex,display)
 net=[]
 for x in (3,6,9,12):net.append(cube([x,4,4],[x+.6,10,12],'rope'))
 for z in (4,7,10):net.append(cube([3,6,z],[13,7,z+.7],'rope'))
 for x in (3,7,11):net.extend([cube([x,3,4],[x+1.5,5,6],'dark'),cube([x,8,8],[x+1.2,10,9.2],'moss')])
 model('item/vodyanoy_net',net,tex,display)
 stone=[cube([2,0,2],[14,8,14],'moss'),cube([5,8,5],[11,11,11],'dark'),cube([6,10,6],[10,11.3,10],'pearl')];model('block/pool_stone',stone,tex);write(A/'blockstates/pool_stone.json',{'variants':{'':{'model':'slavicmyths:block/pool_stone'}}});write(A/'models/item/pool_stone.json',{'parent':'slavicmyths:block/pool_stone'});write(D/'loot_tables/blocks/pool_stone.json',{'type':'minecraft:block','pools':[]})
 write(A/'models/item/elder_vodyanoy_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
 def pixel(x,y):
  color=(63,82,74) if y<32 else (69,68,54)
  if x>=64:color=(34,47,37) if y>=32 else (22,27,22)
  if x>=96:color=(169,160,89) if y<32 else (134,143,124)
  noise=(x*19+y*13+x*y)%15-7
  if x<64 and (x//7+y//5)%5==0:noise-=14
  return tuple(max(0,min(255,c+noise)) for c in color)+(255,)
 png(A/'textures/entity/elder_vodyanoy.png',256,pixel)
 write(D/'loot_tables/entities/elder_vodyanoy.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:pool_pearl','functions':[{'function':'minecraft:set_count','count':3}]}]}]})
 shaped('pool_spear',[' PI',' S ',' S '],{'P':'pool_pearl','I':'minecraft:iron_ingot','S':'minecraft:stick'})
 shaped('depth_amulet',[' L ','IPI',' I '],{'L':'minecraft:leather','I':'minecraft:iron_nugget','P':'pool_pearl'})
 shaped('vodyanoy_net',['SPS','INI','SSS'],{'S':'minecraft:string','I':'minecraft:iron_nugget','N':'fishing_net','P':'pool_pearl'})
 tags=R/'src/main/resources/data/curios/tags/items/necklace.json';data=json.loads(tags.read_text(encoding='utf-8'));data['values']=list(dict.fromkeys(data['values']+['slavicmyths:depth_amulet']));write(tags,data)
 entries=[('depth_clue','ancient_water_sign','Следы в глубине','Traces in the depths','Получить древний водный знак','Obtain an ancient water sign'),('find_deep_pool','pool_stone','Там, где вода молчит','Where water falls silent','Найти камень Омута','Find the pool stone'),('wake_depth_master','elder_vodyanoy_spawn_egg','Хозяин глубины','Master of the depths','Пробудить Старшего Водяного','Awaken the Elder Vodyanoy'),('defeat_depth_master','pool_pearl','Омут больше не зовёт','The pool calls no more','Победить Старшего Водяного','Defeat the Elder Vodyanoy'),('depth_gift','depth_amulet','Дар глубины','Gift of the depths','Создать предмет из Жемчужины омута','Craft an item with a pool pearl')]
 for i,(name,item,*_) in enumerate(entries):
  advance(name,item);path=D/f'advancements/{name}.json';obj=json.loads(path.read_text(encoding='utf-8'));obj['parent']='slavicmyths:'+('meet_vodyanoy' if i==0 else entries[i-1][0]);write(path,obj)
 names={'pool_pearl':('Жемчужина омута','Pearl of the Pool'),'ancient_water_sign':('Обломок древнего водного знака','Ancient water sign fragment'),'pool_spear':('Копьё омута','Pool spear'),'depth_amulet':('Амулет глубины','Depth amulet'),'vodyanoy_net':('Сеть Водяного','Vodyanoy net'),'elder_vodyanoy_spawn_egg':('Старший Водяной: яйцо призыва','Elder Vodyanoy spawn egg')}
 books={
 'elder_vodyanoy':('Старший Водяной','Elder Vodyanoy','Водяной — фольклорный хозяин воды. Старший Водяной — авторская игровая интерпретация особенно древнего духа, а не отдельный подтверждённый мифологический персонаж. Его омут ищут по знаку, полученному за подношения или редкой речной и болотной рыбалкой.','Vodyanoy is a folkloric water spirit. The Elder is an original gameplay interpretation of a very old spirit, not a separate documented mythological figure. Earn a sign through offerings or find one rarely while fishing in rivers and swamps.'),
 'deep_pool':('Омут Старшего Водяного','The Elder’s pool','Ищи старые столбы и тёмную впадину в новых речных и болотных чанках. Знак указывает лишь сторону уже обнаруженного миром омута; он не загружает далёкие чанки. Коснись знаком центрального камня. После пузырей начнётся бой. Высокие деревянные выступы не затапливаются.','Look for old posts and a dark depression in newly generated river and swamp chunks. The sign points toward a pool already loaded by the world; it does not generate distant chunks. Use it on the central stone. Bubbles warn of the encounter. High wooden ledges remain dry.'),
 'pool_pearl':('Жемчужина омута','Pearl of the Pool','Первая победа даёт три жемчужины — достаточно для копья, амулета и сети. Побеждённый омут не возрождает хозяина после перезахода. Это редкий материал, а не новый уровень всей экипировки.','Victory guarantees three pearls, enough for spear, amulet and net. A defeated pool does not respawn its master after reloading. This is a rare material, not a complete equipment tier.'),
 'pool_spear':('Копьё омута','Pool spear','На суше — медленное сильное копьё. В воде удерживай ПКМ не менее 0,8 секунды и отпусти: короткий выпад, до 9 урона близкой цели впереди, 6 секунд перерыва и расход прочности. Стены останавливают перемещение.','On land it is a slow, strong spear. In water, hold use for at least 0.8 seconds and release for a short thrust: up to 9 damage to a nearby target ahead, six-second cooldown and durability cost. Movement respects walls.'),
 'depth_amulet':('Амулет глубины','Depth amulet','Слот ожерелья Curios. Примерно на треть продлевает обычный запас воздуха; это не бесконечное дыхание. Ослабляет песенное притяжение Русалки на 30% и притяжение Старшего Водяного. Сеть Водяного временно замедляет обычную цель, сильнее в воде; боссы невосприимчивы.','Curios necklace slot. Extends normal air supply by roughly one third without granting endless breathing. Reduces Rusalka pull by 30% and weakens the Elder’s pull. The Vodyanoy net briefly slows ordinary targets, more strongly in water; bosses are immune.')}
 for lang in ('ru_ru','en_us'):
  p=A/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));j=0 if lang=='ru_ru' else 1
  for name,pair in names.items():d['item.slavicmyths.'+name]=pair[j]
  d['block.slavicmyths.pool_stone']=('Камень омута','Pool stone')[j];d['entity.slavicmyths.elder_vodyanoy']=('Старший Водяной','Elder Vodyanoy')[j];d['entity.slavicmyths.thrown_net']=('Брошенная сеть','Thrown net')[j]
  d['item.slavicmyths.depth_amulet.effect']=('Ожерелье: запас воздуха дольше, водное притяжение слабее.','Necklace: longer air supply, weaker water pull.')[j]
  for name,item,ru,en,rd,ed in entries:d[f'advancement.slavicmyths.{name}.title']=(ru,en)[j];d[f'advancement.slavicmyths.{name}.description']=(rd,ed)[j]
  for name,(ru,en,rt,et) in books.items():d[f'book.slavicmyths.{name}.title']=(ru,en)[j];d[f'book.slavicmyths.{name}.text']=(rt,et)[j]
  for direction,ru,en in [('unknown','Знак молчит. Исследуй глубокие реки и болота.','The sign is quiet. Explore deep rivers and swamps.'),('near','Вода рядом хранит старую силу.','An old power rests in nearby water.'),('east','Знак тянется к востоку.','The sign pulls east.'),('west','Знак тянется к западу.','The sign pulls west.'),('north','Знак тянется к северу.','The sign pulls north.'),('south','Знак тянется к югу.','The sign pulls south.')]:d['depth.slavicmyths.hint.'+direction]=(ru,en)[j]
  write(p,d)
 audio()
def audio():
 sys.path.insert(0,str(R/'.tools/audio-libs'));import numpy as np;import soundfile as sf
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 for i,kind in enumerate(('idle','hurt','attack','heavy','dash','wave','pull','phase','death')):
  duration=2.4 if kind in ('phase','death','wave') else 1.2;rate=22050;t=np.arange(int(rate*duration))/rate;rng=np.random.default_rng(720+i);noise=rng.normal(0,1,len(t));wet=np.convolve(noise,np.ones(25)/25,mode='same');base=62+i*3
  phase=2*np.pi*np.cumsum(base*(1+.12*np.sin(t*5)))/rate;grunt=np.sin(phase)*.4+np.sin(phase*2)*.12;burble=np.sin(t*2*np.pi*(130+35*np.sin(t*17)))*np.exp(-t*2)*.12
  wave=(grunt+wet*(1.2 if kind in ('dash','wave','pull') else .5)+burble)*np.sin(np.pi*t/duration)**1.2;wave*=.5/(np.max(np.abs(wave))+1e-6);name='elder_'+kind;p=A/f'sounds/depth/{name}.ogg';p.parent.mkdir(parents=True,exist_ok=True);sf.write(str(p),wave,rate,format='OGG',subtype='VORBIS');sub='subtitles.slavicmyths.'+name;events[name]={'subtitle':sub,'sounds':[{'name':'slavicmyths:depth/'+name}]}
  for lang in ('ru_ru','en_us'):
   p=A/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d[sub]=('Старший Водяной — '+{'idle':'ворчит','hurt':'ранен','attack':'замах','heavy':'тяжёлый замах','dash':'водный рывок','wave':'волна','pull':'притяжение','phase':'глубина гудит','death':'затихает'}[kind]) if lang=='ru_ru' else 'Elder Vodyanoy '+kind;write(p,d)
 write(A/'sounds.json',events)
if __name__=='__main__':generate()
