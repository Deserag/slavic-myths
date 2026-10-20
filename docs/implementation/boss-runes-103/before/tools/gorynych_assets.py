"""Original reference-guided native rig, pixel assets and synthesized freely authored audio."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,random,wave,struct,subprocess,shutil
from morozko_assets import Rig,write,merge
import karachun_assets as exporter
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/gorynych-1.9.6'
PAL={'scale':['28272D','131519','555158'],'belly':['514049','2B252D','78666A'],'horn':['88847F','41414A','B4ACA1'],'mor':['5A147D','A42FF4','D352EE'],'fire':['A52918','FF6D19','FFB43E'],'predator':['211F23','7C2024','F54B4F'],'glow':['D52B37','68131C','FB5760'],'membrane':['481C28','211922','68131C'],'tooth':['DAD3C0','928D83','F5EEDC']}
PAL.update({'mor_eye':['A42FF4','D352EE','EDBAFF'],'fire_eye':['FF6D19','FFB43E','FFF4C4'],'predator_eye':['F54B4F','D52B37','FFD4D5']})
def rig():
 r=Rig();r.bone('body',None,0,44,12);r.box('body',0,23,12,62,38,88,'scale');r.box('body',0,22,-8,46,12,52,'belly');r.box('body',0,42,4,68,20,58,'scale')
 for side in (-1,1):
  for front,z in [('front',-20),('back',43)]:
   n=f'leg_{front}_{"left" if side<0 else "right"}';r.bone(n,'body',side*28,36,z);r.box(n,side*31,10,z,17,27,20,'scale');r.bone(n+'_foot',n,side*31,12,z);r.box(n+'_foot',side*31,0,z-4,21,12,27,'scale')
   for toe in range(3):r.box(n+'_foot',side*31-7+toe*7,1,z-19,4,5,11,'horn')
   for layer in range(3):r.box(n,side*(32+layer),26-layer*5,z-2,18,5,23,'scale')
  for i in range(9):r.box('body',side*(27+i%2),31+(i%3)*7,-21+i*8,4,6,8,'scale')
 for i in range(11):r.box('body',0,60-i%2*3,-24+i*7,5,8+(i%3)*2,6,'glow' if i%3==0 else 'scale')
 for h in range(3):
  x=(h-1)*30;mat=['mor','fire','predator'][h];parent='body'
  for j in range(5):
   y=40+j*15;z=-27-j*5;n=f'neck_{h}_{j}';r.bone(n,parent,x,y,z);r.box(n,x,y-1,z,18-j,18,19-j,'scale');r.box(n,x,y,z-9,10,16,2,'belly');r.box(n,x,y+8,z+9,4,9,4,mat);parent=n
  n=f'head_{h}';r.bone(n,parent,x,111,-55);r.box(n,x,105,-56,25 if h==1 else 22,18,27,'scale');r.box(n,x,104,-75,20 if h==2 else 18,10,22,'scale');r.box(n,x,103,-83,15,4,9,mat)
  r.bone(f'jaw_{h}',n,x,106,-61);r.box(f'jaw_{h}',x,97,-72,19,7,32,'scale');r.box(f'jaw_{h}',x,102,-77,13,1,18,mat)
  for side in (-1,1):
   r.box(n,x+side*10,115,-64,5,3,10,'scale');r.box(n,x+side*11.8,114,-69,1,2,7,mat+'_eye')
   r.box(n,x+side*9,119,-49,4,12,5,mat if h==0 else 'horn');r.box(n,x+side*11,129,-45,3,8,4,mat if h==0 else 'horn')
   for tooth in range(5):
    r.box(n,x+side*7,100,-66-tooth*4,2,5+(tooth%2)*(3 if h==2 else 1),2,'tooth');r.box(f'jaw_{h}',x+side*7,103,-66-tooth*4,2,3,2,'tooth')
  for i in range(4):r.box(n,x,122,-52+i*5,4,5+i%2*3,4,mat)
 parent='body'
 for i in range(8):
  n=f'tail_{i}';z=52+i*17;y=29-i*2;r.bone(n,parent,0,y,z);w=27-i*2.8;r.box(n,0,y-8,z+8,w,16-i,23,'scale');r.box(n,0,y+6,z+7,3,9,5,'mor' if i>5 else 'glow');parent=n
 for side in (-1,1):
  n='wing_left' if side<0 else 'wing_right';r.bone(n,'body',side*25,56,14)
  for j in range(8):
   x=side*(35+j*16);height=56+math.sin((j+1)/9*math.pi)*48;previous=56 if j==0 else 56+math.sin(j/9*math.pi)*48;web=22+math.sin((j+1)/9*math.pi)*34
   r.box(n,x,min(previous,height)-2,12+j*4,17,abs(previous-height)+5,7,'scale');r.box(n,x,height-web,26+j*4,17,web-3,2,'membrane')
   r.box(n,x,height-web-4,31+j*4,4,web+3,4,'scale')
   if j%2==0:r.box(n,x,height-web-5,33+j*4,5,6,2,'glow')
 return r
def model():
 r=rig();exporter.PAL=PAL;exporter.O=O;exporter.emit(r,'zmey_gorynych_corrupted','GorynychMesh',1024)
 atlas=Image.open(A/'textures/entity/zmey_gorynych_corrupted.png').convert('RGBA');glow=Image.open(A/'textures/entity/zmey_gorynych_corrupted_glow.png').convert('RGBA')
 for box in r.boxes:
  if box['material'].endswith('_eye'):u,v,w,h=box['uv'];glow.paste(atlas.crop((u,v,u+w,v+h)),(u,v))
 glow.save(A/'textures/entity/zmey_gorynych_corrupted_glow.png');clean=atlas.copy();pixels=clean.load()
 replacements={'scale':['E8E3D4','B8B5A8','D3D3CB'],'belly':['D3C7AA','BCA879','EEE4CC'],'horn':['BC8F45','8F7139','F5C16C'],'membrane':['E8E0C4','C9B994','F0E7D4'],'glow':['BC8F45','D3C7AA','F5C16C'],'mor':['E8E3D4','F5C16C','BC8F45'],'fire':['BC8F45','F5C16C','FBE4A0'],'predator':['D3D3CB','BC8F45','F5C16C'],'mor_eye':['F5C16C','BC8F45','FFE9AB'],'fire_eye':['F5C16C','BC8F45','FFE9AB'],'predator_eye':['F5C16C','BC8F45','FFE9AB']}
 mapping={tuple(bytes.fromhex(c)):tuple(bytes.fromhex(replacements[m][i])) for m in replacements for i,c in enumerate(PAL[m])}
 for y in range(clean.height):
  for x in range(clean.width):
   rgba=pixels[x,y]
   if rgba[:3] in mapping:pixels[x,y]=mapping[rgba[:3]]+(rgba[3],)
 clean.save(A/'textures/entity/zmey_gorynych_purified.png')
 for mask in range(1,8):
  partial=atlas.copy()
  for box in r.boxes:
   bone=box['bone'];head=int(bone.split('_')[1]) if bone.startswith(('neck_','head_','jaw_')) else -1
   if head>=0 and mask&1<<head:u,v,w,h=box['uv'];partial.paste(clean.crop((u,v,u+w,v+h)),(u,v))
  partial.save(A/f'textures/entity/zmey_gorynych_clean_{mask}.png')
 # Local flat particle sheet for geometry renderers; pixels remain nearest sampled.
 im=Image.new('RGBA',(16,16),(238,235,224,220));im.save(A/'textures/entity/gorynych_effect.png')
 write(O/'silhouettes.json',{'boss':{'length_blocks':19,'head_height_blocks':8.5,'wingspan_blocks':22,'legs':4,'heads':3},'serpent_crossbow':{'shape':'wide transverse steel limbs, heavy longitudinal stock, forked snake jaws at bow tips','stock_units':14,'limb_span_units':20,'materials':'dark serpent steel, red seam, brown grip','distinguishing':'physical string, paired serpent jaw ends'},'three_breath_crossbow':{'shape':'same crossbow family with three distinct essence chambers above stock','stock_units':15,'limb_span_units':22,'materials':'serpent steel, ivory scale plates, orange/purple/blue chambers','distinguishing':'three raised chambers and colored loaded bolt'}})
def crossbows():
 def element(a,b,uv=(0,0,8,8),texture='metal'):
  return {'from':a,'to':b,'faces':{s:{'texture':'#'+texture,'uv':list(uv)} for s in ['north','south','east','west','up','down']}}
 display={'gui':{'rotation':[28,45,0],'scale':[.75,.75,.75]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.45,.45,.45]},'fixed':{'rotation':[0,0,0],'scale':[.6,.6,.6]},'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1,2,0],'scale':[.65,.65,.65]},'firstperson_lefthand':{'rotation':[0,90,-25],'translation':[1,2,0],'scale':[.65,.65,.65]},'thirdperson_righthand':{'rotation':[-90,0,0],'translation':[0,2,0],'scale':[.55,.55,.55]},'thirdperson_lefthand':{'rotation':[-90,0,0],'translation':[0,2,0],'scale':[.55,.55,.55]}}
 for name in ['serpent_crossbow','three_breath_crossbow']:
  special=name.startswith('three');im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
  for y in range(32):
   for x in range(32):d.point((x,y),fill=(45+((x*3+y*5)%15),42,47,255))
  d.rectangle((16,0,23,7),fill='#C63440');d.rectangle((24,0,31,7),fill='#49382A');d.rectangle((16,8,23,15),fill='#B9ADA0');d.rectangle((24,8,31,15),fill='#FF7424');d.rectangle((16,16,23,23),fill='#AC36EB');d.rectangle((24,16,31,23),fill='#9BDBF2');im.save(A/f'textures/item/{name}.png')
  elements=[element([6.5,6,2],[9.5,9,16]),element([7,5,10],[9,6,15],texture='grip'),element([-2,6,3],[18,8,5]),element([-2,5,1],[1,8,5]),element([15,5,1],[18,8,5]),element([-1,6,4],[0,7,11],texture='seam'),element([16,6,4],[17,7,11],texture='seam'),element([0,6,10],[16,6.3,10.3],texture='ivory')]
  if special:
   for i,mat in enumerate(['fire','mor','storm']):elements.append(element([4+i*3,9,5],[6+i*3,12,8],texture=mat))
  textures={'metal':f'slavicmyths:item/{name}','grip':f'slavicmyths:item/{name}','seam':f'slavicmyths:item/{name}','ivory':f'slavicmyths:item/{name}','fire':f'slavicmyths:item/{name}','mor':f'slavicmyths:item/{name}','storm':f'slavicmyths:item/{name}','particle':f'slavicmyths:item/{name}'}
  uv={'grip':[24,0,31,7],'seam':[16,0,23,7],'ivory':[16,8,23,15],'fire':[24,8,31,15],'mor':[16,16,23,23],'storm':[24,16,31,23]}
  for e in elements:
   for face in e['faces'].values():face['uv']=uv.get(face['texture'][1:],[0,0,15,15])
  for state in ['','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework']:
   es=json.loads(json.dumps(elements))
   if state in ['_arrow','_firework']:es.append(element([7.6,7.2,0],[8.4,8,11],texture='ivory'))
   if '_pulling_' in state:
    progress=int(state[-1])+1
    es[-(4 if special else 1)]['from'][2]=6+progress
   m={'textures':textures,'elements':es,'display':display,'gui_light':'front'}
   if not state:m['overrides']=[{'predicate':{'pulling':1},'model':f'slavicmyths:item/{name}_pulling_0'},{'predicate':{'pulling':1,'pull':.58},'model':f'slavicmyths:item/{name}_pulling_1'},{'predicate':{'pulling':1,'pull':1},'model':f'slavicmyths:item/{name}_pulling_2'},{'predicate':{'charged':1},'model':f'slavicmyths:item/{name}_arrow'},{'predicate':{'charged':1,'firework':1},'model':f'slavicmyths:item/{name}_firework'}]
   write(A/f'models/item/{name}{state}.json',m)
def icons():
 names=['gorynych_scale','ancient_serpent_steel','serpent_seal','gorynych_flame_breath_bottle','gorynych_mor_breath_bottle','gorynych_storm_breath_bottle','rune_mor_breath','rune_dragon_flame','rune_storm_strike','dragon_helmet','dragon_chestplate','dragon_leggings','dragon_boots']
 for name in names:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);dark='#202126';metal='#59565C';red='#D52B37';white='#E8E3D4';color='#A42FF4' if 'mor' in name else '#9ED5E8' if 'storm' in name else '#FF7929'
  if 'bottle' in name:
   d.polygon([(12,2),(19,2),(19,10),(25,16),(26,25),(22,30),(8,30),(5,25),(6,16),(12,10)],fill='#657680',outline=white);d.polygon([(9,18),(15,13),(23,19),(23,25),(20,28),(10,28),(8,24)],fill=color);d.rectangle((11,2,20,5),fill='#806340');d.line((8,17,8,23),fill=white,width=2)
  elif name.startswith('rune'):
   shape=[(7,3),(25,4),(29,24),(23,30),(4,28),(2,11)];d.polygon(shape,fill=dark,outline=metal)
   points=[(8,24),(8,13),(15,19),(23,11),(23,25)] if 'mor' in name else [(7,24),(16,6),(25,24),(18,20),(13,26)] if 'flame' in name else [(5,23),(17,5),(12,18),(26,10),(17,28)]
   d.line(points,fill=color,width=3)
  elif name=='gorynych_scale':d.polygon([(15,1),(28,9),(26,21),(16,31),(3,20),(4,8)],fill=white,outline=metal);d.line([(16,5),(21,13),(14,21),(18,27)],fill='#BC8F45',width=3)
  elif name=='ancient_serpent_steel':d.polygon([(3,11),(24,5),(29,12),(27,22),(6,28),(2,22)],fill=metal,outline=dark);d.line([(4,15),(24,9),(27,12)],fill=red,width=3);d.line([(7,24),(24,19)],fill='#ABA4A1',width=2)
  elif name=='serpent_seal':
   d.ellipse((2,2,29,29),fill='#BC8F45',outline=dark,width=2)
   for x in [9,15,21]:d.line([(16,25),(x,17),(x,9)],fill=dark,width=2);d.rectangle((x-2,7,x+2,10),fill=metal)
  elif name.endswith('helmet'):
   d.polygon([(5,7),(12,4),(20,4),(27,8),(26,28),(21,30),(21,15),(11,15),(10,29),(5,26)],fill=metal,outline=dark);d.line([(4,9),(2,2),(7,6),(12,3),(13,9)],fill=dark,width=3);d.line([(27,8),(30,2),(25,5)],fill=dark,width=3);d.line([(7,12),(15,9),(24,12)],fill=red,width=2)
  elif name.endswith('chestplate'):
   d.polygon([(3,7),(10,3),(12,8),(20,8),(22,3),(29,7),(30,15),(25,15),(25,29),(6,29),(6,15),(1,15)],fill=metal,outline=dark);d.line([(16,10),(21,17),(16,24),(11,17),(16,10)],fill=red,width=3);d.line((5,6,4,2),fill=dark,width=3);d.line((27,6,29,2),fill=dark,width=3)
  elif name.endswith('leggings'):
   d.rectangle((6,2,25,9),fill=metal,outline=dark);d.rectangle((7,10,13,29),fill=metal,outline=dark);d.rectangle((18,10,24,29),fill=metal,outline=dark);d.line((10,12,10,25),fill=red,width=2);d.line((21,12,21,25),fill=red,width=2)
  else:
   for x in [3,18]:d.rectangle((x+3,3,x+10,23),fill=metal,outline=dark);d.polygon([(x+3,21),(x+10,20),(x+12,26),(x+11,30),(x,30),(x,25)],fill=dark,outline=metal);d.line((x+6,5,x+6,21),fill=red,width=2)
  im.save(A/f'textures/item/{name}.png');write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
 write(A/'models/item/zmey_gorynych_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
 for y in range(64):
  for x in range(64):d.point((x,y),fill=(50 if (x//3+y//3)%3 else 80,43,49,255))
 for x in range(4,60,9):d.line([(x,0),(x+3,15),(x,27),(x+4,43),(x,63)],fill='#C02A39',width=2)
 im.save(A/'textures/models/armor/dragon.png')
def integrate_hill_loot():
 p=D/'loot_table/chests/serpent_fortress.json';n=json.loads(p.read_text(encoding='utf-8'))
 if not any(pool.get('name')=='gorynych196' for pool in n['pools']):n['pools'].append({'name':'gorynych196','rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:ancient_serpent_steel','weight':5,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2}}]},{'type':'minecraft:item','name':'slavicmyths:serpent_crossbow','weight':1},{'type':'minecraft:empty','weight':2}]})
 for pool in n['pools']:
  if pool.get('name')=='gorynych196':
   for entry in pool['entries']:
    for function in entry.get('functions',[]):
     if function.get('function')=='minecraft:set_count' and isinstance(function.get('count'),dict):function['count']['type']='minecraft:uniform'
 write(p,n)
def data():
 integrate_hill_loot();write(D/'recipe/gorynych_upgrade.json',{'type':'slavicmyths:gorynych_upgrade','category':'equipment'})
 for name in ['zmey_gorynych','gorynych_breath_cloud','gorynych_breath_shot']:write(D/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':[]})
 for name in ['gorynych_mor','gorynych_fire','gorynych_impact']:write(D/f'damage_type/{name}.json',{'message_id':name,'scaling':'when_caused_by_living_non_player','exhaustion':.1})
 p=R/'src/main/resources/data/minecraft/tags/damage_type/is_fire.json';n=json.loads(p.read_text());n['values']=list(dict.fromkeys(n['values']+['slavicmyths:gorynych_fire']));write(p,n)
 # Existing quest item and IDs; no competing amulet or new access system.
 write(D/'recipe/mor_ward.json',{'type':'slavicmyths:gorynych_upgrade','category':'misc','ingredients':[{'item':'slavicmyths:mor_fire_amulet_fragment'},{'item':'slavicmyths:mor_water_amulet_fragment'},{'item':'slavicmyths:eye_of_mor'}],'result':{'id':'slavicmyths:mor_ward','count':1}})
 write(D/'tales/zmey_gorynych.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tale.slavicmyths.zmey_gorynych.title','icon':'slavicmyths:gorynych_scale','initial':0,'subjects':['entity/slavicmyths:zmey_gorynych'],'advancements':[],'links':[]})
 im=Image.new('RGBA',(128,96),'#19171E');d=ImageDraw.Draw(im);d.ellipse((28,55,104,85),fill='#49424E')
 for x,c in [(36,'#A42FF4'),(64,'#FF7929'),(92,'#F54B4F')]:d.line([(64,65),(x,35),(x,20)],fill='#555158',width=10);d.rectangle((x-9,14,x+12,23),fill='#302A35');d.point((x+9,17),fill=c)
 (A/'textures/tales').mkdir(exist_ok=True);im.save(A/'textures/tales/zmey_gorynych.png')
def language():
 names={'gorynych_scale':('Чешуя Горыныча','Gorynych Scale'),'ancient_serpent_steel':('Древняя змеиная сталь','Ancient Serpent Steel'),'serpent_crossbow':('Змеиный арбалет','Serpent Crossbow'),'three_breath_crossbow':('Арбалет трёх дыханий','Three Breath Crossbow'),'serpent_seal':('Змеиная печать','Serpent Seal'),'gorynych_flame_breath_bottle':('Пламенное дыхание','Flame Breath'),'gorynych_mor_breath_bottle':('Моровое дыхание','Mor Breath'),'gorynych_storm_breath_bottle':('Дыхание бури','Storm Breath'),'rune_mor_breath':('Руна морового дыхания','Mor Breath Rune'),'rune_dragon_flame':('Руна драконьего пламени','Dragon Flame Rune'),'rune_storm_strike':('Руна буревого удара','Storm Strike Rune'),'dragon_helmet':('Драконий шлем','Dragon Helmet'),'dragon_chestplate':('Драконий нагрудник','Dragon Chestplate'),'dragon_leggings':('Драконьи поножи','Dragon Leggings'),'dragon_boots':('Драконьи ботинки','Dragon Boots'),'zmey_gorynych_spawn_egg':('Яйцо призыва Змея Горыныча','Zmey Gorynych Spawn Egg')}
 desc={'gorynych_scale':('Светлая чешуя спасённого Горыныча.','Ivory scale of the rescued dragon.'),'ancient_serpent_steel':('Редкий металл древней крепости.','Rare metal from the serpent fortress.'),'serpent_crossbow':('+18% урона болтов, более долгая зарядка.','+18% bolt damage, slower loading.'),'three_breath_crossbow':('Всегда три стрелы: каждая наносит минимум 14 урона до брони. Shift+ПКМ: Мор (облако и слабость), Пламя (горение 2 с), Буря (+3 урона, отбрасывание и Замедление II на 2 с). Shift+ЛКМ: веер / очередь. Без бутылочек.','Always three arrows: each deals at least 14 damage before armor. Shift+right-click: Mor (cloud and weakness), Flame (2 s burning), Storm (+3 damage, knockback and Slowness II for 2 s). Shift+left-click: fan / burst. No bottles.'),'rune_mor_breath':('Каждое пятое прямое попадание: облако слабости на 4 секунды. Оружие и посохи.','Every fifth direct hit: a 4-second weakness cloud. Melee weapons and staves.'),'rune_dragon_flame':('Каждый четвёртый прямой удар: огненная вспышка по врагам рядом.','Every fourth melee hit: a small flame burst against nearby enemies.'),'rune_storm_strike':('Каждое четвёртое прямое попадание из лука/арбалета: волна отбрасывания.','Every fourth bow/crossbow hit: a knockback wave.'),'dragon_helmet':('Ослепление, тьма и тошнота на 25% короче.','Blindness, darkness and nausea last 25% less.'),'dragon_chestplate':('Урон огня/взрывов −15%.','Fire/explosion damage −15%.'),'dragon_leggings':('Отбрасывание −30%.','Knockback −30%.'),'dragon_boots':('Урон падения −35%.','Fall damage −35%.'),'serpent_seal':('Подтверждение помощи Горынычу. Не открывает портал.','Proof of rescuing Gorynych.'),'zmey_gorynych_spawn_egg':('Самостоятельный бой для творческого режима; сюжетные награды остаются в горе.','Standalone creative encounter; story rewards remain bound to the hill.')}
 ui={'boss':('Змей Горыныч','Zmey Gorynych'),'health':('Горыныч — %s / %s HP','Gorynych — %s / %s HP'),'inventory_full':('Для награды освободите 3 слота и вернитесь к Горынычу.','Free 3 inventory slots and return to Gorynych.'),'heads_hint':('Очистите три головы: порошок, пепел и очищенная вода.','Cleanse the three heads: powder, ash and purified water.'),'cleanse_0':('Моровой голове нужен очищающий порошок.','The Mor head needs cleansing powder.'),'cleanse_1':('Огненной голове нужен пепел очищения.','The fire head needs purifying ash.'),'cleanse_2':('Хищной голове нужна очищенная вода.','The predator head needs purified water.'),'reward':('Горыныч спасён. Получены четыре чешуи, печать и руна.','Gorynych is saved. You receive four scales, a seal and a rune.'),'shed_wait':('Следующая чешуя через %s секунд игрового времени.','Next shed scale in %s seconds of world time.'),'mode_0':('Мор — %s зарядов','Mor — %s charges'),'mode_1':('Огонь — %s зарядов','Flame — %s charges'),'mode_2':('Буря — %s зарядов','Storm — %s charges'),'compatible_mor_breath':('Оружие ближнего боя и посохи.','Melee weapons and staves.'),'compatible_dragon_flame':('Оружие ближнего боя.','Melee weapons.'),'compatible_storm_strike':('Луки и арбалеты.','Bows and crossbows.')}
 desc.update({'gorynych_flame_breath_bottle':('Остаток огненной атаки. Даёт 3 огненных заряда арбалету трёх дыханий.','Collected flame essence. Loads 3 flame shots into a Three Breath Crossbow.'),'gorynych_mor_breath_bottle':('Собранное дыхание Мора. Даёт 3 моровых заряда арбалету трёх дыханий.','Collected Mor essence. Loads 3 Mor shots into a Three Breath Crossbow.'),'gorynych_storm_breath_bottle':('Дыхание крыльев. Даёт 3 буревых заряда арбалету трёх дыханий.','Collected wing storm essence. Loads 3 storm shots into a Three Breath Crossbow.')})
 for i,locale in enumerate(['ru_ru','en_us']):
  values={f'item.slavicmyths.{k}':v[i] for k,v in names.items()};values.update({f'item.slavicmyths.{k}.desc':v[i] for k,v in desc.items()});values.update({f'gorynych196.{k}':v[i] for k,v in ui.items()});values['serpenthill195.ward_needed']='Для защиты горы нужны победа над заражённым Лихом, оба фрагмента хранителей и оберег: в инвентаре или действующий.' if i==0 else 'Hill protection requires defeating blighted Likho, both guardian fragments and a carried or active Mor Ward.';values.update({'entity.slavicmyths.zmey_gorynych':names['zmey_gorynych_spawn_egg'][i].replace('Яйцо призыва ','').replace(' Spawn Egg',''),'entity.slavicmyths.gorynych_breath_cloud':'Облако дыхания' if i==0 else 'Breath Cloud','entity.slavicmyths.gorynych_breath_shot':'Сгусток дыхания' if i==0 else 'Breath Shot','tale.slavicmyths.zmey_gorynych.title':'Спасение Горыныча' if i==0 else 'Rescuing Gorynych','tale.slavicmyths.zmey_gorynych.body':'Игровая интерпретация: три головы заражены Мором. Очищение сохраняет древнему змею жизнь.' if i==0 else 'Game interpretation: three heads infected by Mor. Cleansing preserves the ancient dragon’s life.'})
  for rune in ['mor_breath','dragon_flame','storm_strike']:values['rpg.slavicmyths.effect_'+rune]=desc['rune_'+rune][i];values['rpg.slavicmyths.rune_'+rune]=names['rune_'+rune][i]
  
  for stage in ['unknown','rumor','seen','witnessed','known','discovered','studied','verified']:values[f'tales.entry.zmey_gorynych.{stage}']=values['tale.slavicmyths.zmey_gorynych.body']
  for kind in ['mor','fire','impact']:
   values['death.attack.gorynych_'+kind]='%1$s повержен атакой %2$s' if i==0 else '%1$s was overcome by %2$s'
   values['death.attack.gorynych_'+kind+'.item']='%1$s повержен атакой %2$s с помощью %3$s' if i==0 else '%1$s was overcome by %2$s using %3$s'
  merge(A/f'lang/{locale}.json',values)
def sounds():
 source=(R/'src/main/java/org/slavicmyths/gorynych/GorynychContent.java').read_text();line=source.split('SOUNDS_LIST=List.of(')[1].split(');')[0];names=[s.strip().strip('"') for s in line.split(',')];ff=shutil.which('ffmpeg');assert ff;work=R/'.tools/gorynych-audio';work.mkdir(exist_ok=True);out=A/'sounds/gorynych';out.mkdir(parents=True,exist_ok=True);events={}
 for i,name in enumerate(names):
  duration=1.4 if 'roar' in name or 'transform' in name else .7 if 'charge' in name or 'breath' in name else .35;rate=22050;rng=random.Random(19600+i);samples=[]
  for j in range(int(rate*duration)):
   t=j/rate;fade=math.sin(math.pi*t/duration)**.6;low=55+i*3;freq=low*(1+.15*math.sin(t*9));noise=rng.uniform(-1,1)
   if 'cleanse' in name or 'purified' in name or 'bottle' in name:v=(math.sin(2*math.pi*(500+i*31)*t)+.3*math.sin(2*math.pi*(850+i*27)*t))*fade*.18
   elif 'fire' in name:v=(noise*.65+math.sin(2*math.pi*120*t)*.2)*(1+.2*math.sin(t*70))*fade*.35
   elif 'mor' in name:v=(math.sin(2*math.pi*(170+i*4)*t+.7*math.sin(t*15))*.4+noise*.18)*fade*.4
   elif 'wind' in name or 'wings' in name or 'dive' in name:v=(noise*.5+math.sin(2*math.pi*(60+i*2)*t)*.12)*fade*.35
   elif 'step' in name or 'impact' in name or 'land' in name:v=(math.sin(2*math.pi*(65-30*t/duration)*t)+noise*.2)*math.exp(-t*10)*.4
   elif 'crossbow_charge' in name:v=(noise*.3*(1 if (j//500)%2 else .1)+math.sin(2*math.pi*380*t)*.1)*fade*.4
   else:v=(.5*math.sin(2*math.pi*freq*t)+.24*math.sin(2*math.pi*(freq*1.47)*t)+.2*noise)*fade*.45
   samples.append(struct.pack('<h',int(max(-1,min(1,v))*32767)))
  p=work/(name+'.wav')
  with wave.open(str(p),'wb') as wav:wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(rate);wav.writeframes(b''.join(samples))
  subprocess.run([ff,'-y','-hide_banner','-loglevel','error','-i',str(p),'-c:a','libvorbis','-q:a','3','-flags:a','+bitexact','-fflags','+bitexact',str(out/(name+'.ogg'))],check=True)
  events['gorynych.'+name]={'sounds':['slavicmyths:gorynych/'+name]}
 merge(A/'sounds.json',events)
def main():
 O.mkdir(parents=True,exist_ok=True);model();crossbows();icons();data();language();sounds();__import__('weapon_patch_assets').main();print('GORYNYCH_ASSETS_PASS native rig, 9 appearances, 12 crossbow states, 31 original OGG')
if __name__=='__main__':main()

# Preserve current armor after both the original and weapon overlays.
if __name__ == "__main__":
 from armor_revision_assets import apply as armor_revision
 armor_revision()

if __name__=="__main__":
 from reward_weapon_texts import apply as apply_reward_texts
 apply_reward_texts()
