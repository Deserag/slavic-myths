"""Authored 1.2.3 overlay; no reference poster copied into production assets."""
from pathlib import Path
import json,zipfile,gzip
from PIL import Image,ImageDraw
import swamp_073 as nbt
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
def write(rel,obj):
 p=RES/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def main():
 a='assets/slavicmyths/'
 for material,base in [('military_wood',(61,40,26)),('military_parchment',(216,197,150)),('military_chain',(92,96,94)),('military_trim',(124,36,31))]:
  im=Image.new('RGB',(128,128));d=ImageDraw.Draw(im)
  for y in range(128):
   for x in range(128):
    shade=((x*13+y*7)%11)-5
    if material=='military_wood':shade-=18 if y%32<2 else 0;shade-=10 if (x//4+y//3)%17==0 else 0
    if material=='military_chain':shade+=24 if x%8 in (1,2) and y%8 in (1,2,5,6) else -12
    im.putpixel((x,y),tuple(max(0,min(255,v+shade))for v in base))
  if material=='military_parchment':
   d.line([(10,84),(35,35),(56,51),(83,19),(114,40)],fill='#675f49',width=2)
   for x,y in [(35,35),(56,51),(83,19)]:d.rectangle((x-3,y-3,x+3,y+3),outline='#a94431',width=2)
  if material=='military_trim':
   for x in range(8,128,16):d.polygon([(x,2),(x+6,8),(x,14),(x-6,8)],fill='#ddd0ab');d.rectangle((x-2,20,x+2,30),fill='#ddd0ab')
  dest=RES/a/f'textures/block/{material}.png';dest.parent.mkdir(parents=True,exist_ok=True);im.save(dest)
 def cube(lo,hi,tex):return {'from':lo,'to':hi,'faces':{f:{'texture':'#'+tex,'uv':[0,0,16,16]}for f in ['north','south','east','west','up','down']}}
 elements=[cube([0,11,0],[16,13,16],'wood')]
 for x,z in [(1,1),(12,1),(1,12),(12,12)]:elements.append(cube([x,0,z],[x+3,11,z+3],'wood'))
 elements += [cube([1,1,8],[8,7,14],'wood'),cube([2,13,3],[13,13.2,12],'paper'),cube([5,6,.2],[11,12,.5],'trim'),cube([1,13,12],[3,14,15],'paper'),cube([12,13,11],[15,16,14],'iron'),cube([12.5,13.5,11.5],[14.5,15.5,13.5],'lantern'),cube([2,13,1],[3,14,2],'iron'),cube([2.3,14,1.3],[2.6,16,1.6],'paper')]
 elements += [cube([6,4,1],[10,9,2],'trim'),cube([7,5.5,.5],[9,7.5,1],'iron'),cube([13,2,5],[13.6,13.2,5.6],'wood'),cube([12.5,12,5],[14.2,13.5,5.6],'iron')]
 write(a+'models/block/druzhinnik_table.json',{'parent':'minecraft:block/block','textures':{'wood':'slavicmyths:block/military_wood','paper':'slavicmyths:block/military_parchment','trim':'slavicmyths:block/military_trim','iron':'minecraft:block/iron_block','lantern':'minecraft:block/lantern','particle':'slavicmyths:block/military_wood'},'elements':elements})
 write(a+'models/item/druzhinnik_table.json',{'parent':'slavicmyths:block/druzhinnik_table','display':{'gui':{'rotation':[30,225,0],'scale':[.75,.75,.75]}}})
 write(a+'blockstates/druzhinnik_table.json',{'variants':{f'facing={f}':{'model':'slavicmyths:block/druzhinnik_table','y':i*90}for i,f in enumerate(['north','east','south','west'])}})
 write('data/slavicmyths/loot_table/blocks/druzhinnik_table.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:druzhinnik_table'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 write('data/slavicmyths/recipe/druzhinnik_table.json',{'type':'minecraft:crafting_shaped','pattern':['BIL','PPP','PPP'],'key':{'B':{'item':'minecraft:paper'},'I':{'item':'minecraft:iron_ingot'},'L':{'item':'minecraft:lantern'},'P':{'item':'minecraft:dark_oak_planks'}},'result':{'id':'slavicmyths:druzhinnik_table','count':1}})
 for rel in ['data/minecraft/tags/block/mineable/axe.json','data/minecraft/tags/point_of_interest_type/acquirable_job_site.json','data/minecraft/tags/point_of_interest_type/village.json']:
  data=json.loads((RES/rel).read_text(encoding='utf-8'));value='slavicmyths:druzhinnik_table' if '/block/'in rel else 'slavicmyths:druzhinnik';data['values']=list(dict.fromkeys(data['values']+[value]));write(rel,data)
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);d.polygon([(38,21),(82,25),(94,80),(75,105),(31,94),(24,51)],fill='#514f46',outline='#928777',width=3);d.line((40,38,73,83),fill='#852f2a',width=10);d.ellipse((52,26,65,40),fill='#252b29');d.line((29,58,84,60),fill='#79776b',width=2);im.save(RES/a/'textures/item/bandit_token.png');write(a+'models/item/bandit_token.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/bandit_token'}})
 im=Image.new('RGB',(512,512),'#39261b');d=ImageDraw.Draw(im)
 for y in range(0,512,32):d.line((0,y,511,y),fill='#281c13',width=3)
 d.rectangle((5,5,506,506),outline='#94714a',width=5)
 for x in range(12,512,24):d.polygon([(x,12),(x+6,18),(x,24),(x-6,18)],fill='#b84435')
 (RES/a/'textures/gui').mkdir(parents=True,exist_ok=True);im.save(RES/a/'textures/gui/military.png')
 # Existing villager UVs, distinct quilted coat + linked chain + red/white sleeve trim.
 for variant in ['a','b']:
  im=Image.open(RES/a/f'textures/entity/villager/profession/armorer_{variant}.png').convert('RGBA');d=ImageDraw.Draw(im)
  for y in range(42,84):
   for x in range(32,88):
    if im.getpixel((x,y))[3]:im.putpixel((x,y),(88+(x+y)%8,77+(x+y)%8,56+(x+y)%8,255))
  d.rectangle((36,64,80,68),fill='#522e1d');d.rectangle((42,43,49,76),fill='#943a30');d.line((50,45,71,68),fill='#ab8c52',width=3)
  im.save(RES/a/f'textures/entity/villager/profession/druzhinnik_{variant}.png')
 # Limb/helmet atlas, 128px native UVs for dedicated guard equipment layer.
 im=Image.new('RGB',(128,128),'#66533c');d=ImageDraw.Draw(im)
 for y in range(128):
  for x in range(128):
   c=(119,112,98) if (x//2+y//2)%2 else (76,76,67)
   if 40<y<64:c=(131,43,33) if y%8<4 else (213,202,167)
   im.putpixel((x,y),c)
 im.save(RES/a/'textures/entity/guard_equipment.png')
 bandits=['slavicmyths:'+v for v in ['bandit_fighter','bandit_archer','bandit_heavy','bandit_senior','ataman']]
 write('data/slavicmyths/tags/entity_type/bandits.json',{'replace':False,'values':bandits})
 hostile=bandits+['minecraft:zombie','minecraft:husk','minecraft:drowned','minecraft:zombie_villager','minecraft:skeleton','minecraft:stray','minecraft:bogged','minecraft:wither_skeleton','minecraft:pillager','minecraft:vindicator','minecraft:evoker','minecraft:ravager','minecraft:vex','slavicmyths:nightingale','slavicmyths:tugarin_zmey','slavicmyths:upyr','slavicmyths:nav','slavicmyths:volkolak']+['slavicmyths:'+v for v in ['kurgan_druzhinnik','kurgan_voevoda','buried_volkhv','unresting_prince','fire_serpent','podvey','likho_one_eyed']]
 # Names are validated against actual registry in server tests.
 write('data/slavicmyths/tags/entity_type/village_hostiles.json',{'replace':False,'values':hostile})
 quests=[]
 for id,type,target,amount,diff,rewards in [('bandit_threat','kill','slavicmyths:bandits',8,0,[('ancient_coin',8),('minecraft:arrows',12)]),('bandit_signs','trophy','slavicmyths:bandit_token',6,0,[('ancient_coin',6),('minecraft:iron_ingot',2)]),('clear_camp','camp','slavicmyths:bandits',1,1,[('ancient_coin',12),('minecraft:iron_ingot',4)]),('defend_settlement','defend','slavicmyths:bandits',1,1,[('ancient_coin',12),('minecraft:arrow',16)]),('solovey','boss','slavicmyths:nightingale',1,2,[('ancient_coin',20),('minecraft:saddle',1)]),('tugarin','boss','slavicmyths:tugarin_zmey',1,2,[('ancient_coin',24),('minecraft:iron_horse_armor',1)])]:
  rewards=[('minecraft:arrow'if item=='minecraft:arrows'else item,n)for item,n in rewards];quests.append({'id':id,'type':type,'target':target,'amount':amount,'difficulty':diff,'rarity':8 if type=='boss' else 1,'refresh':'daily','eligibility':'loaded_uncleared_camp' if type=='camp' else 'registered_entity' if type=='boss' else 'settlement','rewards':[{'item':item if ':'in item else 'slavicmyths:'+item,'count':n}for item,n in rewards]})
 write('data/slavicmyths/military/quests.json',quests)
 def pool(item,chance,count=1,damage=False):
  entry={'type':'minecraft:item','name':item};func=[]
  if count!=1:func.append({'function':'minecraft:set_count','count':count})
  if damage:func.append({'function':'minecraft:set_damage','damage':{'type':'minecraft:uniform','min':.15,'max':.45}})
  if func:entry['functions']=func
  return {'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':chance}],'entries':[entry]}
 for name in ['bandit_fighter','bandit_archer','bandit_heavy','bandit_senior']:
  pools=[pool('slavicmyths:ancient_coin',.85,{'type':'minecraft:uniform','min':0,'max':3}),pool('slavicmyths:bandit_token',.45),pool('minecraft:arrow',.2,{'type':'minecraft:uniform','min':1,'max':4}),pool('minecraft:bread',.18),pool('minecraft:leather',.15),pool('minecraft:iron_sword',.025,damage=True)]
  write(f'data/slavicmyths/loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
 supply=[pool('slavicmyths:ancient_coin',1,{'type':'minecraft:uniform','min':2,'max':8}),pool('minecraft:arrow',.8,{'type':'minecraft:uniform','min':4,'max':12}),pool('minecraft:bread',.8,{'type':'minecraft:uniform','min':2,'max':4}),pool('minecraft:torch',.6,{'type':'minecraft:uniform','min':3,'max':8}),pool('minecraft:leather',.4,{'type':'minecraft:uniform','min':1,'max':3}),pool('minecraft:iron_ingot',.25,{'type':'minecraft:uniform','min':1,'max':3}),pool('slavicmyths:retainer_shield',.08),pool('minecraft:iron_sword',.07,damage=True),pool('minecraft:saddle',.025),pool('minecraft:iron_horse_armor',.02),pool('minecraft:golden_horse_armor',.012),pool('minecraft:diamond',.004),pool('minecraft:diamond_horse_armor',.002)]
 write('data/slavicmyths/loot_table/chests/barracks_supply.json',{'type':'minecraft:chest','pools':supply})
 for path in (RES/'data/slavicmyths/loot_table/chests').glob('stronghold_*.json'):
  if path.stem in ['stronghold_ataman','stronghold_nightingale','stronghold_feather']:continue
  data=json.loads(path.read_text(encoding='utf-8'))
  for row in data['pools']:
   for entry in row['entries']:
    if entry.get('name')=='minecraft:diamond':
     row['conditions']=[{'condition':'minecraft:random_chance','chance':.004}];entry['functions']=[{'function':'minecraft:set_count','count':1}]
  write(str(path.relative_to(RES)),data)

 for p in (RES/'data/slavicmyths/loot_table/chests').glob('bandit_*.json'):
  if 'large' not in p.name:write(str(p.relative_to(RES)),{'type':'minecraft:chest','pools':supply})
 translations={
 'block.slavicmyths.druzhinnik_table':('Стол дружинника','Druzhinnik Table'),'item.slavicmyths.bandit_token':('Разбойничий знак','Bandit Token'),'entity.minecraft.villager.slavicmyths.druzhinnik':('Дружинник','Druzhinnik'),
 'military.slavicmyths.accept':('Принять','Accept'),'military.slavicmyths.track':('Отслеживать','Track'),'military.slavicmyths.turn_in':('Сдать','Turn in'),'military.slavicmyths.abandon':('Отказаться','Abandon'),'military.slavicmyths.close':('Закрыть','Close'),'military.slavicmyths.rewards':('Награды','Rewards'),'military.slavicmyths.objective':('Прогресс: %s / %s','Progress: %s / %s'),'military.slavicmyths.location':('Цель: %s %s %s','Target: %s %s %s'),'military.slavicmyths.turn_in_hint':('Вернитесь к военному столу для сдачи.','Return to a military table to turn in.'),'military.slavicmyths.debug':('Дружина: %s','Military: %s')}
 for status,ru,en in [('available','Доступно','Available'),('active','Принято','Active'),('complete','Выполнено','Complete'),('claimed','Сдано','Claimed'),('abandoned','Отказ','Abandoned')]:translations['military.slavicmyths.status.'+status]=(ru,en)
 for i,(ru,en)in enumerate([('Обычное','Common'),('Опасное','Dangerous'),('Особое','Special')]):translations['military.slavicmyths.difficulty.'+str(i)]=(ru,en)
 names=[('bandit_threat','Разбойничья угроза','Bandit Threat','Уничтожьте 8 разбойников.','Defeat 8 bandits.'),('bandit_signs','Знаки разбойников','Bandit Tokens','Принесите 6 разбойничьих знаков.','Deliver 6 bandit tokens.'),('clear_camp','Зачистка лагеря','Clear the Camp','Посетите указанный лагерь и устраните его отряд.','Visit the specified camp and eliminate its roster.'),('defend_settlement','Защитить поселение','Defend the Settlement','Участвуйте в настоящем налёте и победите нападающих.','Participate in a real bandit raid and defeat its attackers.'),('solovey','Соловей-разбойник','Solovey the Brigand','Победите настоящего Соловья-разбойника.','Defeat the real Solovey boss.'),('tugarin','Тугарин','Tugarin','Победите настоящего Тугарина.','Defeat the real Tugarin boss.')]
 for id,ru,en,desc,endesc in names:translations['military.slavicmyths.quest.'+id]=(ru,en);translations['military.slavicmyths.description.'+id]=(desc,endesc)
 for locale,index in [('ru_ru',0),('en_us',1)]:
  rel=a+'lang/'+locale+'.json';d=json.loads((RES/rel).read_text(encoding='utf-8'));d.update({k:v[index]for k,v in translations.items()});write(rel,d)
 barracks()
 from village_buildings import main as buildings
 buildings()
def barracks()
 from village_buildings import main as buildings
 buildings():
 b=nbt.Blueprint('barracks',(13,13,15));b.box((0,0,0),(12,12,14),'air');b.box((1,0,1),(11,0,13),'cobblestone');b.box((1,1,1),(11,1,9),'spruce_planks')
 for x in [1,11]:b.box((x,2,1),(x,5,9),'spruce_planks')
 for z in [1,9]:b.box((1,2,z),(11,5,z),'spruce_planks')
 for x,z in [(1,1),(11,1),(1,9),(11,9)]:b.box((x,1,z),(x,5,z),'stripped_spruce_log',axis='y')
 b.box((5,2,1),(7,4,1),'air');b.box((9,2,9),(10,4,9),'air')
 for x in [1,11]:
  for z in [3,6]:b.box((x,3,z),(x,4,z+1),'glass_pane',north='true',south='true',east='false',west='false',waterlogged='false')
 # Solid gable slopes; central room head clearance is at least 3 blocks.
 for x in range(0,13):
  y=6+min(x,12-x)//3
  for z in range(0,11):b.put(x,y,z,'spruce_slab',type='bottom',waterlogged='false')
 for x in [2,4,6,8]:
  b.put(x,2,8,'red_bed',facing='north',part='foot',occupied='false');b.put(x,2,7,'red_bed',facing='north',part='head',occupied='false')
 for x in [3,7]:b.put(x,2,3,'slavicmyths:druzhinnik_table',facing='south')
 b.put(2,2,5,'chest',facing='east',type='single',waterlogged='false');state,_=b.blocks[2,2,5];b.blocks[2,2,5]=(state,{'id':'minecraft:chest','LootTable':'slavicmyths:chests/barracks_supply'})
 b.put(10,2,5,'spruce_fence',north='false',south='false',east='false',west='false',waterlogged='false');b.put(10,3,5,'red_banner',rotation='0');b.put(4,5,5,'lantern',hanging='true',waterlogged='false');b.put(8,5,5,'lantern',hanging='true',waterlogged='false')
 for x,z in [(8,10),(12,10),(8,14),(12,14)]:b.box((x,1,z),(x,10,z),'stripped_spruce_log',axis='y')
 # Enclosed tower reached by an aligned interior stair flight; avoid unsafe tight spiral corners.
 for z in range(2,11):
  y=z-1
  b.box((10,y, z),(10,min(12,y+3),z),'air')
 for z in range(2,11):b.put(10,z-1,z,'spruce_stairs',facing='south',half='bottom',shape='straight',waterlogged='false')
 b.box((9,9,11),(11,9,13),'spruce_planks')
 for x in range(8,13):
  for z in range(10,15):
   if x in [8,12]or z in [10,14]:
    if not(x==10 and z==10):b.put(x,10,z,'spruce_fence',north='true',south='true',east='true',west='true',waterlogged='false')
 b.put(12,11,12,'lantern',hanging='false',waterlogged='false');b.put(8,11,12,'white_banner',rotation='0')
 b.put(6,1,0,'jigsaw',orientation='north_up');state,_=b.blocks[6,1,0];b.blocks[6,1,0]=(state,{'id':'minecraft:jigsaw','name':'minecraft:building_entrance','target':'minecraft:street','pool':'minecraft:empty','final_state':'minecraft:air','joint':'rollable','selection_priority':0,'placement_priority':0})
 old=nbt.OUT;nbt.OUT=RES/'data/slavicmyths/structure/military';b.save();nbt.OUT=old
 # Append one low-weight piece, retaining all vanilla elements and weights verbatim.
 jar=ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
 if not jar.exists():jar=next((Path('C:/Users/pavel/.gradle/caches')).rglob('minecraft-resources.jar'))
 with zipfile.ZipFile(jar)as z:
  for biome in ['plains','taiga','snowy','savanna','desert']:
   rel=f'data/minecraft/worldgen/template_pool/village/{biome}/houses.json';d=json.loads(z.read(rel));d['elements'].append({'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':'slavicmyths:military/barracks','processors':'minecraft:empty','projection':'rigid'}});write(rel,d)
if __name__=='__main__':main()
