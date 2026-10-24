"""Reproducible original pixel sprites and voxel models for #258, not a poster import."""
from pathlib import Path
import json
from PIL import Image, ImageDraw
R=Path(__file__).resolve().parents[1]/'src/main/resources'
A=R/'assets/slavicmyths'
IDS=['chud_mining_lantern','chud_master_pickaxe','chud_multitool_axe','chud_multitool_pickaxe','chud_multitool_shovel','chud_mining_lift']
RU=['Шахтёрский фонарь Чуди','Кирка мастера Чуди','Мультиинструмент Чуди: топор','Мультиинструмент Чуди: кирка','Мультиинструмент Чуди: лопата','Шахтёрский подъёмник Чуди']
EN=['Chud Mining Lantern','Chud Master Pickaxe','Chud Multitool: Axe','Chud Multitool: Pickaxe','Chud Multitool: Shovel','Chud Mining Lift']
COLORS={'edge':'#1e292c','metal':'#435055','shine':'#768689','bronze':'#a56e36','gold':'#d5a453','wood':'#68452d','teal':'#5dbcb0','light':'#d4fff1'}
def save(path,data):path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def cube(lo,hi,tex):return {'from':lo,'to':hi,'faces':{face:{'texture':'#'+tex,'uv':[0,0,16,16]} for face in ['north','south','east','west','up','down']}}
def generate():
 for key,color in COLORS.items():
  im=Image.new('RGB',(16,16),color);d=ImageDraw.Draw(im)
  if key in ['metal','wood','bronze']:
   for x,y in [(2,3),(7,11),(12,6),(4,14)]:d.point((x,y),COLORS['edge'])
  im.save(A/'textures/item'/('chud_'+key+'.png'))
 for id in IDS[1:5]:
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
  # Shared folding handle, visibly different head proportions for each actual tool family.
  d.line([(4,14),(10,8)],fill=COLORS['edge'],width=4);d.line([(4,14),(10,8)],fill=COLORS['wood'],width=2)
  d.rectangle((5,11,6,12),fill=COLORS['bronze'])
  if id=='chud_master_pickaxe':
   d.polygon([(2,4),(3,2),(9,2),(13,5),(14,9),(12,8),(10,5),(6,4)],fill=COLORS['edge']);d.line([(3,3),(8,3),(12,6)],fill=COLORS['shine'],width=2);d.rectangle((8,4,10,6),fill=COLORS['bronze']);d.point((9,5),fill=COLORS['teal'])
  elif id.endswith('_axe'):
   d.polygon([(3,2),(9,3),(12,6),(10,9),(6,8),(3,5)],fill=COLORS['edge']);d.polygon([(4,3),(8,4),(10,6),(8,8),(4,5)],fill=COLORS['metal']);d.line([(3,2),(3,5),(6,8)],fill=COLORS['shine']);d.rectangle((9,5,10,6),fill=COLORS['bronze']);d.point((8,5),fill=COLORS['teal'])
  elif id.endswith('_pickaxe'):
   d.polygon([(4,3),(9,2),(13,5),(14,7),(11,5),(7,4),(4,5)],fill=COLORS['metal']);d.line([(4,3),(9,2),(12,4)],fill=COLORS['shine']);d.rectangle((9,4,10,6),fill=COLORS['bronze']);d.point((10,5),fill=COLORS['teal'])
  else:
   d.polygon([(8,1),(12,2),(14,5),(11,8),(8,7),(7,4)],fill=COLORS['edge']);d.polygon([(9,2),(11,3),(13,5),(11,7),(9,6),(8,4)],fill=COLORS['metal']);d.line([(9,2),(11,3),(13,5)],fill=COLORS['shine']);d.point((10,5),fill=COLORS['teal'])
  im.save(A/'textures/item'/(id+'.png'));save(A/'models/item'/(id+'.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'slavicmyths:item/'+id}})
 display={'gui':{'rotation':[25,35,0],'translation':[0,0,0],'scale':[.85,.85,.85]},'firstperson_righthand':{'rotation':[0,-35,0],'translation':[1,2,0],'scale':[.55,.55,.55]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.5,.5,.5]},'ground':{'translation':[0,3,0],'scale':[.4,.4,.4]},'fixed':{'scale':[.7,.7,.7]}}
 display['firstperson_lefthand']={'rotation':[0,35,0],'translation':[-1,2,0],'scale':[.55,.55,.55]}
 display['thirdperson_lefthand']=display['thirdperson_righthand']
 textures={k:'slavicmyths:item/chud_'+k for k in COLORS};textures['particle']=textures['metal']
 lantern=[cube([4,1,4],[12,3,12],'bronze'),cube([4,11,4],[12,13,12],'metal'),cube([6,4,6],[10,10,10],'light'),cube([6,13,7],[7,16,9],'bronze'),cube([9,13,7],[10,16,9],'bronze'),cube([6,15,7],[10,16,9],'bronze')]
 for x in [4,11]:
  for z in [4,11]:lantern.append(cube([x,3,z],[x+1,11,z+1],'metal'))
 lift=[cube([2,2,4],[14,4,12],'metal'),cube([3,4,6],[5,13,10],'bronze'),cube([11,4,6],[13,13,10],'bronze'),cube([5,6,5],[11,12,11],'wood'),cube([1,9,7],[3,11,9],'shine'),cube([0,6,7],[2,10,9],'metal'),cube([6,13,7],[10,14,9],'teal')]
 for name,parts in [(IDS[0],lantern),(IDS[-1],lift)]:save(A/'models/item'/(name+'.json'),{'ambientocclusion':False,'textures':textures,'elements':parts,'display':display})
 for lang,names in [('ru_ru',RU),('en_us',EN)]:
  path=A/'lang'/(lang+'.json');data=json.loads(path.read_text(encoding='utf-8'));data.update({'item.slavicmyths.'+id:name for id,name in zip(IDS,names)})
  hints=({'lantern':'В любой руке: клиентский свет 14. Не меняет спавн мобов.','pickaxe':'Shift + ПКМ по воздуху: Удача II ↔ Шёлковое касание I. Улучшенная Удача сохраняется.','multi':'Shift + ПКМ по воздуху: топор → кирка → лопата. Общие износ, чары и руны. Топор рубит одно естественное дерево с лимитом 128 брёвен.','lift':'Удерживайте ПКМ 1,5 с: безопасный подъём на поверхность. 64 применения, перезарядка 60 с.','fail':'Подъём невозможен: безопасная поверхность не найдена или выход закрыт.'} if lang=='ru_ru' else {'lantern':'Either hand: client light 14. Does not affect mob spawning.','pickaxe':'Shift + air use: Fortune II ↔ Silk Touch I. Upgraded Fortune is retained.','multi':'Shift + air use: axe → pickaxe → shovel. Shared wear, enchantments and runes. Axe fells one natural tree, limited to 128 logs.','lift':'Hold use for 1.5s: safe surface ascent. 64 uses, 60s cooldown.','fail':'Cannot ascend: no safe surface or the exit is locked.'})
  for key,value in hints.items():data['chud_tools.lift.fail' if key=='fail' else 'chud_tools.hint.'+key]=value
  modes=['Удача','Шёлковое касание','Топор','Кирка','Лопата'] if lang=='ru_ru' else ['Fortune','Silk Touch','Axe','Pickaxe','Shovel']
  for key,value in zip(['fortune','silk','axe','pickaxe','shovel'],modes):data['chud_tools.mode.'+key]=value
  save(path,data)
 path=A/'creative_catalog.tsv';lines=path.read_text(encoding='utf-8').splitlines();lines=[x for x in lines if x.split('\t')[0] not in IDS];lines += [id+'\tslavicmyths\tchud_tools' for id in IDS];path.write_text('\n'.join(lines)+'\n',encoding='utf-8')
 for name,chance,items in [('chud_storehouse',.025,IDS),('abandoned_mine',.035,IDS),('abandoned_treasury',.08,IDS)]:
  path=R/'data/slavicmyths/loot_table/chests'/(name+'.json');data=json.loads(path.read_text());data['pools']=[p for p in data['pools'] if p.get('name')!='slavicmyths:chud_tools258']
  data['pools'].append({'name':'slavicmyths:chud_tools258','rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':chance}],'entries':[{'type':'minecraft:item','name':'slavicmyths:'+id,'weight':2 if id==IDS[0] else 1} for id in [IDS[0],IDS[1],IDS[2],IDS[-1]]]});save(path,data)
 tags=R/'data/minecraft/tags/item/enchantable';tags.mkdir(parents=True,exist_ok=True)
 tools=IDS[1:5]
 for tag,ids in {'durability':tools+[IDS[-1]],'mining':tools,'mining_loot':tools,'weapon':[IDS[2]],'sharp_weapon':[IDS[2]]}.items():
  path=tags/(tag+'.json');data=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
  for id in ids:
   if 'slavicmyths:'+id not in data['values']:data['values'].append('slavicmyths:'+id)
  save(path,data)
 for family,ids in {'axes':[IDS[2]],'pickaxes':[IDS[1],IDS[3]],'shovels':[IDS[4]]}.items():
  path=R/'data/minecraft/tags/item'/(family+'.json');data=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
  for id in ids:
   if 'slavicmyths:'+id not in data['values']:data['values'].append('slavicmyths:'+id)
  save(path,data)
if __name__=='__main__':generate()
