"""Reproducible 2.0.4 resources from the approved written locks.
Original PNG match remains ART_SOURCE_BLOCKED; these are authored game assets.
Never edits audio or saved worlds. Shared biome tags and guardian head loot integration are updated.
"""
from pathlib import Path
import json, math, random, zipfile
from PIL import Image, ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths'
V=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
NAMES={
'lost_hall_stone':('Камень заблудших','Lost Hall Stone'),'cracked_lost_hall_stone':('Треснувший камень заблудших','Cracked Lost Hall Stone'),'polished_lost_hall_stone':('Полированный камень заблудших','Polished Lost Hall Stone'),'luminous_vein_stone':('Светожильный камень','Luminous Vein Stone'),'cave_coating':('Пещерный налёт','Cave Coating'),'lost_halls_waymark':('Путевая метка','Waymark'),
'lost_whisper_thread':('Нить заблудшего шёпота','Lost Whisper Thread'),'lightvein_shard':('Светожильный осколок','Lightvein Shard'),'echo_vial':('Склянка эха','Echo Vial'),'spore_pouch':('Споровый мешочек','Spore Pouch'),'lost_halls_tuning_fork':('Камертон Чертогов','Halls Tuning Fork'),'posten_heart':('Сердце Постеня','Posten Heart'),'rune_shadow_face':('Руна Теневого лика','Rune of the Shadow Face'),'grove_guardian_head':('Голова Сторожа чащи','Grove Guardian Head'),'posten_head':('Голова Постеня','Posten Head')}
MOBS={'sheptun':('Шептун','Whisperer'),'oskolnik':('Оскольник','Shardling'),'gulokryl':('Гулокрыл','Emberwing'),'ekhovik':('Эховик','Echo Beast'),'griboshmyg':('Грибошмыг','Mushroom Scuttler'),'posten':('Постень — Хозяин Чертогов','Posten, Master of the Halls')}
def write(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def tag(ns,kind,name,values):
 p=R/f'src/main/resources/data/{ns}/tags/{kind}/{name}.json';d=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};d['values']=list(dict.fromkeys(d['values']+values));write(p,d)
def recipe(name,items,count=1):
 write(D/f'recipe/{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':x if ':' in x else 'slavicmyths:'+x} for x in items],'result':{'id':'slavicmyths:'+name,'count':count}})
 write(D/f'advancement/recipes/lost_halls/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[items[0] if ':' in items[0] else 'slavicmyths:'+items[0]]}]}},'has_the_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'slavicmyths:'+name}}},'requirements':[['has_material','has_the_recipe']],'rewards':{'recipes':['slavicmyths:'+name]}})
def blocks():
 rng=random.Random(2043)
 for name in ['lost_hall_stone','cracked_lost_hall_stone','polished_lost_hall_stone','luminous_vein_stone','cave_coating']:
  im=Image.new('RGBA',(16,16),(45,48,60,255));draw=ImageDraw.Draw(im)
  for x,y,w,h in [(0,0,7,5),(8,0,7,7),(0,6,5,8),(6,7,9,5),(4,13,11,2)]:
   tone=rng.randrange(42,66);draw.rectangle((x,y,x+w,y+h),fill=(tone,tone+2,tone+9,255));draw.line((x,y,x+w-1,y),fill=(tone+9,tone+11,tone+18,255))
  if name=='polished_lost_hall_stone':draw.rectangle((1,1,14,14),outline='#626675')
  if name=='cracked_lost_hall_stone':draw.line([(5,0),(7,5),(4,8),(10,12),(9,15)],fill='#151820',width=2)
  if name=='luminous_vein_stone':
   for points in [[(3,0),(4,4),(2,8)],[(11,5),(9,9),(12,13)]]:draw.line(points,fill='#38b2c9',width=2);draw.point(points[1],fill='#b7f4ee')
  if name=='cave_coating':
   im=Image.new('RGBA',(16,16));draw=ImageDraw.Draw(im)
   for _ in range(35):x,y=rng.randrange(16),rng.randrange(16);draw.line((x,y,min(15,x+2),min(15,y+1)),fill=rng.choice(['#3c5a53','#4b6d62','#65a197']))
  p=A/f'textures/block/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
  if name!='cave_coating':im.transpose(Image.Transpose.ROTATE_90).save(A/f'textures/block/{name}_top.png')
  if name!='cave_coating':write(A/f'models/block/{name}.json',{'parent':'minecraft:block/cube_bottom_top','textures':{'side':'slavicmyths:block/'+name,'top':'slavicmyths:block/'+name+'_top','bottom':'slavicmyths:block/'+name+'_top'}});write(A/f'blockstates/{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}});write(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name})
 with zipfile.ZipFile(V) as z:
  for base in ['lost_hall_stone','polished_lost_hall_stone']:
   for form in ['stairs','slab','wall']:
    new=base+'_'+form;NAMES[new]=(NAMES[base][0]+' — '+{'stairs':'ступени','slab':'плита','wall':'ограда'}[form],NAMES[base][1]+' '+form.title());old='blackstone_'+form
    def rewrite(v):
     if isinstance(v,dict):return {k:rewrite(x) for k,x in v.items()}
     if isinstance(v,list):return [rewrite(x) for x in v]
     if isinstance(v,str):return v.replace('minecraft:block/blackstone_'+form,'slavicmyths:block/'+new).replace('minecraft:block/blackstone','slavicmyths:block/'+base)
     return v
    state=rewrite(json.loads(z.read('assets/minecraft/blockstates/'+old+'.json')));write(A/f'blockstates/{new}.json',state)
    for suffix in {'stairs':['','_inner','_outer'],'slab':['','_top'],'wall':['_post','_side','_side_tall','_inventory']}[form]:write(A/f'models/block/{new}{suffix}.json',rewrite(json.loads(z.read('assets/minecraft/models/block/'+old+suffix+'.json'))))
    write(A/f'models/item/{new}.json',{'parent':'slavicmyths:block/'+new+('_inventory' if form=='wall' else '')})
    amount=4 if form=='stairs' else 6
    write(D/f'recipe/{new}.json',{'type':'minecraft:stonecutting','ingredient':{'item':'slavicmyths:'+base},'result':{'id':'slavicmyths:'+new,'count':2 if form=='slab' else 1}})
  state=json.loads(z.read('assets/minecraft/blockstates/glow_lichen.json'))
  def coating(v):
   if isinstance(v,dict):return {k:coating(x) for k,x in v.items()}
   if isinstance(v,list):return [coating(x) for x in v]
   return v.replace('minecraft:block/glow_lichen','slavicmyths:block/cave_coating') if isinstance(v,str) else v
  write(A/'blockstates/cave_coating.json',coating(state));write(A/'models/block/cave_coating.json',coating(json.loads(z.read('assets/minecraft/models/block/glow_lichen.json'))));write(A/'models/item/cave_coating.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:block/cave_coating'}})
 # Visible pedestal and split crystalline crown: full 3D model, no hologram.
 elements=[]
 for lo,hi,tex in [([3,0,3],[13,2,13],'stone'),([5,2,5],[11,4,11],'stone'),([7,4,6],[9,11,10],'crystal'),([4,3,6],[6,7,10],'stone'),([10,3,6],[12,8,10],'stone')]:elements.append({'from':lo,'to':hi,'faces':{f:{'texture':'#'+tex} for f in ['north','south','east','west','up','down']}})
 write(A/'models/block/lost_halls_waymark.json',{'textures':{'stone':'slavicmyths:block/lost_hall_stone','crystal':'slavicmyths:block/luminous_vein_stone','particle':'slavicmyths:block/lost_hall_stone'},'elements':elements})
 write(A/'models/block/lost_halls_waymark_wall.json',{'textures':{'stone':'slavicmyths:block/lost_hall_stone','crystal':'slavicmyths:block/luminous_vein_stone','particle':'slavicmyths:block/lost_hall_stone'},'elements':[{'from':lo,'to':hi,'faces':{f:{'texture':'#'+tex} for f in ['north','south','east','west','up','down']}} for lo,hi,tex in [([3,3,13],[13,11,16],'stone'),([5,5,10],[11,8,14],'stone'),([7,6,6],[9,13,10],'crystal')]]})
 write(A/'blockstates/lost_halls_waymark.json',{'variants':{f'face={face},facing={direction}':{'model':'slavicmyths:block/lost_halls_waymark'+('_wall' if face=='wall' else ''),'y':angle,'x':180 if face=='ceiling' else 0} for face in ['floor','wall','ceiling'] for direction,angle in [('north',0),('east',90),('south',180),('west',270)]}});write(A/'models/item/lost_halls_waymark.json',{'parent':'slavicmyths:block/lost_halls_waymark'})
 for name in list(NAMES):
  if not (A/f'blockstates/{name}.json').exists():continue
  entry={'type':'minecraft:item','name':'slavicmyths:'+name}
  if name.endswith('_slab'):entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'type':'double'}}]}]
  write(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:survives_explosion'}],'entries':[entry]}]})
 names=[n for n in NAMES if (A/f'blockstates/{n}.json').exists()];tag('minecraft','block','mineable/pickaxe',['slavicmyths:'+n for n in names if n!='cave_coating']);tag('minecraft','block','walls',['slavicmyths:'+n for n in names if n.endswith('_wall')]);recipe('polished_lost_hall_stone',['lost_hall_stone']*4,4)
 recipe('lost_halls_waymark',['lost_whisper_thread','lightvein_shard','minecraft:polished_blackstone']);recipe('lost_halls_tuning_fork',['echo_vial','lightvein_shard','minecraft:stick'])
def rig(name):
 parts=[]
 def box(n,p,size,mat='stone',parent='root',rot=(0,0,0),off=None):parts.append(dict(name=n,parent=parent,pivot=p,size=size,material=mat,rotation=rot,offset=off or [-s/2 for s in size]))
 if name=='sheptun':
  box('body',[0,3,0],[10,19,7],'cloth');box('head',[0,-11,0],[8,9,7],'dark');box('hood',[0,-2,0],[10,11,9],'cloth','head');box('face',[0,0,-5],[6,6,1],'dark','head')
  for i,s in enumerate([-1,1]):box('arm'+str(i),[s*8,0,0],[4,20,4],'cloth',rot=(0,0,s*.13));box('eye'+str(i),[s*2,-1,-5.6],[1,1,1],'light','head')
  for i in range(5):box('ribbon'+str(i),[-4+i*2,12,0],[2,10+i%3*3,3],'cloth',off=[-1,0,-1.5])
 elif name=='oskolnik':
  box('body',[0,-5,0],[22,20,13]);box('head',[0,-20,-1],[10,10,10]);
  for i,s in enumerate([-1,1]):box('leg'+str(i),[s*6,13,0],[9,18,9]);box('arm'+str(i),[s*15,-5,0],[7,17,7]);box('forearm'+str(i),[0,12,0],[8,12,8],parent='arm'+str(i));box('eye'+str(i),[s*3,-1,-5.5],[2,2,1],'light','head')
  for i in range(3):box('crystal'+str(i),[-8+i*8,-21,7],[4,15+i%2*5,4],'light',rot=(0,0,(i-1)*.17))
 elif name=='gulokryl':
  box('body',[0,11,0],[12,12,15],'dark');box('head',[0,8,-8],[10,9,7],'dark');box('core',[0,12,-8],[6,5,2],'fire')
  for i,s in enumerate([-1,1]):
   box('eye'+str(i),[s*3,0,-4],[2,2,1],'fire','head');box('ear'+str(i),[s*4,-6,0],[3,6,3],'dark','head',rot=(0,0,s*.3));box('wing_'+('left' if s<0 else 'right'),[s*7,9,0],[16,3,13],'fire',rot=(0,0,s*.2));box('wing_tip_'+('left' if s<0 else 'right'),[s*12,1,0],[15,2,10],'fire',parent='wing_'+('left' if s<0 else 'right'),rot=(0,0,s*.15))
 elif name=='ekhovik':
  box('body',[0,7,0],[22,19,30]);box('head',[0,6,-20],[13,11,14]);
  for i,(x,z) in enumerate([(-9,-9),(9,-9),(-9,10),(9,10)]):box('leg'+str(i),[x,18,z],[7,12,8])
  for i in range(6):box('crystal'+str(i),[0,-7,-12+i*5],[4,7+i%3*2,4],'light')
  for i,s in enumerate([-1,1]):box('eye'+str(i),[s*4,-1,-7.5],[2,2,1],'light','head')
 elif name=='griboshmyg':
  box('body',[0,16,0],[14,10,12],'cloth');box('head',[0,15,-7],[12,8,5],'cloth');box('cap',[0,9,0],[21,6,19],'cap')
  for i,(x,z) in enumerate([(-5,-4),(5,-4),(-5,4),(5,4)]):box('leg'+str(i),[x,22,z],[3,4,4]);box('sidefungus'+str(i),[x*1.5,15,z],[4,3,4],'light')
  for i,s in enumerate([-1,1]):box('eye'+str(i),[s*3,0,-3],[3,3,1],'pale','head')
 elif name=='posten':
  box('body',[0,-17,0],[16,24,10],'dark');box('head',[0,-39,0],[8,10,7],'dark');box('mask_ring',[0,0,0],[3,3,3],'dark','head')
  for i,mat in enumerate(['sorrow','anger','decay','silence']):
   a=i*math.pi/2;box('mask_'+str(i),[math.sin(a)*10,0,-math.cos(a)*10],[8,15,3],mat,'mask_ring',rot=(0,-a,0));box('eye_mask'+str(i),[0,-2,-2],[5,1,1],'dark','mask_'+str(i))
   for side in [-1,1]:
    box('socket_'+str(i)+'_'+str(side),[side*2,-2,-2],[2,2,1],'dark','mask_'+str(i))
    if i==0:box('tear_'+str(side),[side*2,1,-2],[1,5,1],'light','mask_'+str(i))
    if i==1:box('brow_'+str(side),[side*2,-4,-2],[4,2,1],'dark','mask_'+str(i),rot=(0,0,side*.3))
   if i==0:box('sorrow_mouth',[0,5,-2],[3,1,1],'dark','mask_'+str(i))
   if i==1:
    box('anger_mouth',[0,3,-2],[5,3,1],'dark','mask_'+str(i))
    for tooth in [-1,1]:box('tooth_'+str(tooth),[tooth,2,-3],[1,2,1],'pale','mask_'+str(i))
   if i==2:
    for chip in range(4):box('decay_chip_'+str(chip),[-3+chip*2,chip-1,-2],[2,3,1],'dark','mask_'+str(i))
   if i==3:
    box('silence_hollow',[0,2,-2],[2,8,1],'dark','mask_'+str(i));box('silence_crack',[2,-4,-2],[1,4,1],'dark','mask_'+str(i))
  for i,s in enumerate([-1,1]):
   box('shoulder'+str(i),[s*12,-25,0],[9,9,9],'dark');box('arm'+str(i),[s*13,-18,0],[5,19,5],'dark');box('forearm'+str(i),[0,15,0],[4,17,4],'cloth','arm'+str(i));
   for j in range(3):box('finger'+str(i)+str(j),[(j-1)*2,12,-1],[1,8,2],'pale','forearm'+str(i))
   box('lantern'+str(i),[s*16,0,0],[4,5,4],'light')
  for i in range(8):a=i*math.pi/4;box('ribbon'+str(i),[math.cos(a)*5,-5,math.sin(a)*4],[3,20+i%3*4,3],'cloth',off=[-1.5,0,-1.5])
 else:raise ValueError(name)
 from mob332_assets import halls_parts,palette as mob_palette,pattern
 halls_parts(name,parts)
 atlas=512;u=v=row=0
 for p in parts:
  x,y,z=p['size'];w,h=2*(x+z),y+z
  if u+w+2>atlas:u=0;v+=row+2;row=0
  assert v+h+2<=atlas;p['uv']=[u,v];row=max(row,h);u+=w+2
 write(A/f'lost_halls_rigs/{name}.json',dict(atlas=atlas,parts=parts))
 variants=range(4) if name=='sheptun' else [None]
 for variant in variants:
  im=Image.new('RGBA',(atlas,atlas));glow=Image.new('RGBA',(atlas,atlas));rng=random.Random(name)
  palette={'stone':(56,63,75),'cloth':(39,33,53),'dark':(21,21,31),'light':[(231,65,47),(73,218,73),(192,193,201),(155,74,229)][variant] if variant is not None else (49,180,203),'fire':(231,102,32),'cap':(155,35,80),'pale':(193,206,203),'sorrow':(130,183,199),'anger':(150,58,55),'decay':(69,109,66),'silence':(104,62,143)}
  for p in parts:
   u,v=p['uv'];x,y,z=p['size'];w,h=2*(x+z),y+z;color=palette[p['material']]
   if name!='posten' and p['material'] not in ['light','fire']:color=mob_palette(name,p,color)
   if name in ['oskolnik','ekhovik'] and p['material']=='light':color=(100,150,236) if name=='oskolnik' else (83,206,177)
   for yy in range(v,v+h):
    for xx in range(u,u+w):
     noise=rng.randrange(-6,7);rgb=tuple(max(0,min(255,c+noise+(pattern(name,p['name'],xx-u,yy-v) if name!='posten' else 0))) for c in color);im.putpixel((xx,yy),(*rgb,255))
     if p['material'] in ['light','fire'] or p['name'].startswith('eye_mask'):glow.putpixel((xx,yy),(*rgb,255))
  suffix='_'+str(variant) if variant is not None else '';folder=A/'textures/entity/lost_halls';folder.mkdir(parents=True,exist_ok=True);im.save(folder/(name+suffix+'.png'));glow.save(folder/(name+suffix+'_glow.png'))
def icons():
 for n in ['lost_whisper_thread','lightvein_shard','echo_vial','spore_pouch','lost_halls_tuning_fork','posten_heart','rune_shadow_face']:
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);dark='#282331';blue='#61cadd'
  if n=='lost_whisper_thread':d.line([(3,3),(10,2),(6,6),(12,8),(4,10),(9,14)],fill='#8a62b3',width=2)
  elif n=='lightvein_shard':d.polygon([(9,1),(12,5),(10,12),(4,15),(3,9)],fill=dark);d.polygon([(9,3),(9,10),(5,13),(5,8)],fill=blue)
  elif n=='echo_vial':d.rectangle((6,1,9,4),fill='#8b756c');d.polygon([(5,4),(10,4),(13,8),(12,14),(3,14),(2,8)],fill='#4e707f');d.rectangle((4,8,11,12),fill='#294664');d.line((6,9,9,7,10,11),fill=blue,width=1)
  elif n=='spore_pouch':d.polygon([(5,2),(10,2),(9,5),(13,9),(12,14),(3,14),(2,9),(6,5)],fill='#694476');d.line((4,5,11,5),fill='#a881b7',width=2);d.point((7,10),fill='#f085de');d.point((10,12),fill='#c55db9')
  elif n=='lost_halls_tuning_fork':d.line((7,14,7,7,3,4,3,1),fill=dark,width=3);d.line((7,7,12,4,12,1),fill=dark,width=3);d.line((3,1,3,4),fill=blue,width=2);d.line((12,1,12,4),fill=blue,width=2)
  elif n=='posten_heart':d.polygon([(2,4),(5,2),(8,5),(11,2),(14,5),(12,10),(7,15),(3,10)],fill='#592640');d.line((7,4,6,8,9,10,7,13),fill='#bd4d85',width=2)
  else:d.rectangle((3,1,12,14),fill='#3e2b51');d.ellipse((6,3,9,6),fill=blue);d.line((7,7,7,11,5,13),fill=blue,width=2);d.line((7,11,10,13),fill=blue,width=1)
  p=A/f'textures/item/{n}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p);write(A/f'models/item/{n}.json',{'parent':'minecraft:item/handheld' if 'fork' in n else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+n}})
 for n in MOBS:
  if n!='posten':write(A/f'models/item/{n}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
def loot():
 for n,item,lo,hi in [('sheptun','lost_whisper_thread',0,2),('oskolnik','lightvein_shard',1,3)]:
  pool={'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}},{'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}}]}]}
  write(D/f'loot_table/entities/{n}.json',{'type':'minecraft:entity','pools':[pool]})
def finish():
 for index,lang in enumerate(['ru_ru','en_us']):
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
  for name,labels in NAMES.items():data[('block.' if (A/f'blockstates/{name}.json').exists() or name.endswith('_head') else 'item.')+'slavicmyths.'+name]=labels[index]
  for name,labels in MOBS.items():data['entity.slavicmyths.'+name]=labels[index];data['item.slavicmyths.'+name+'_spawn_egg']=('Яйцо призыва: ' if index==0 else 'Spawn Egg: ')+labels[index]
  data.update({'halls.fork.found':['Чертоги: проход %s, %s, %s','Halls: passage at %s, %s, %s'][index],'halls.fork.none':['Рядом нет известного доступного зала','No known accessible hall nearby'][index],'halls.shadow.desc':['Активная руна нагрудника: 4 с невидимости; повторное нажатие — ранний выход. До 3 безопасно размещённых боевых копий на 6 с; перезарядка 75 с после выхода.','Active chestplate rune: 4s invisibility; press again to exit early. Up to 3 safely placed combat decoys for 6s; 75s cooldown after exit.'][index],'halls.shadow.partial':['Безопасно размещено копий: %s','Safely placed decoys: %s'][index],'rpg.slavicmyths.rune_shadow_face':['Теневой лик','Shadow Face'][index]});write(p,data)
 p=A/'creative_catalog.tsv';text=p.read_text(encoding='utf-8');known={line.split('\t')[0] for line in text.splitlines()};rows=[]
 for n in NAMES:
  if n not in known:rows.append(n+'\t'+('decor' if (A/f'blockstates/{n}.json').exists() else 'magic' if n.startswith('rune_') else 'tools' if 'fork' in n else 'resources')+'\tnether_lost_halls')
 for n in MOBS:
  if n+'_spawn_egg' not in known:rows.append(n+'_spawn_egg\tmobs\tnether_lost_halls')
 # Resolve the repository's actual tab names, not invented categories.
 tabs=set(line.split('\t')[1] for line in text.splitlines() if not line.startswith('#') and '\t' in line)
 rows=[line.replace('\tbuilding\t','\t'+('building' if 'building' in tabs else 'building_blocks')+'\t').replace('\tcreatures\t','\t'+('creatures' if 'creatures' in tabs else 'spawns')+'\t') for line in rows]
 lines=text.rstrip().splitlines()+rows
 for i,line in enumerate(lines):
  if line.startswith('lost_halls_tuning_fork\t'):lines[i]='lost_halls_tuning_fork\ttools\tnether_lost_halls'
 p.write_text('\n'.join(lines)+'\n',encoding='utf-8')
def world_and_heads():
 biome=json.loads((D/'worldgen/biome/black_cinder.json').read_text())
 biome['effects'].update(fog_color=0x333a50,sky_color=0x282b3c,particle={'options':{'type':'minecraft:warped_spore'},'probability':.0005})
 for group in biome['features']:
  group[:]=[v for v in group if not v.startswith('slavicmyths:')]
 biome['features'][2].append('slavicmyths:lost_halls_terrain')
 biome['spawners']={'monster':[{'type':'slavicmyths:'+n,'weight':weight,'minCount':1,'maxCount':2} for n,weight in [('sheptun',12),('oskolnik',8)]],'creature':[{'type':'slavicmyths:'+n,'weight':8,'minCount':1,'maxCount':2} for n in ['gulokryl','ekhovik','griboshmyg']]};biome['spawn_costs']={}
 write(D/'worldgen/biome/lost_halls.json',biome);write(D/'worldgen/configured_feature/lost_halls_terrain.json',{'type':'slavicmyths:lost_halls_terrain','config':{}});write(D/'worldgen/placed_feature/lost_halls_terrain.json',{'feature':'slavicmyths:lost_halls_terrain','placement':[{'type':'minecraft:biome'}]})
 for name in ['nether_fortress','bastion_remnant','ruined_portal_nether']:tag('minecraft','worldgen/biome','has_structure/'+name,['slavicmyths:lost_halls'])
 for name in ['grove_guardian_head','zharenitsa_head','posten_head']:
  write(A/f'blockstates/{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}});write(A/f'models/block/{name}.json',{'textures':{'particle':'slavicmyths:block/lost_hall_stone'}})
  write(A/f'models/item/{name}.json',{'parent':'minecraft:builtin/entity','gui_light':'front','display':{'gui':{'rotation':[0,180,0],'scale':[.7,.7,.7]},'fixed':{'rotation':[0,180,0]}}})
  write(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'half':'lower'}},{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}]}]})
 # Trophy-only larger masks; the living boss rig and existing block ID stay unchanged.
 source=json.loads((A/'lost_halls_rigs/posten.json').read_text());selected=[];known={'head'}
 for part in source['parts']:
  if part['name']=='head' or part['parent'] in known:
   part=json.loads(json.dumps(part));selected.append(part);known.add(part['name'])
   if part['name'].startswith('mask_') and part['name']!='mask_ring':
    part['pivot'][0]*=.9;part['pivot'][2]*=.9;part['size'][1]=19;part['offset'][1]=-9.5
   elif part['parent'].startswith('mask_') and part['parent']!='mask_ring':part['pivot'][1]*=1.2
 write(A/'lost_halls_rigs/posten_trophy.json',{'atlas':source['atlas'],'parts':selected})
 from boss331_assets import posten_trophy
 posten_trophy()
 # One additional head from the existing guardian loot route, not a second death event.
 p=D/'loot_table/entities/grove_guardian.json';data=json.loads(p.read_text());data['pools']=[v for v in data['pools'] if all(e.get('name')!='slavicmyths:grove_guardian_head' for e in v.get('entries',[]))];write(p,data)
 source=json.loads((A/'cinder_rigs/zharenitsa.json').read_text());selected=[];known={'head'}
 for p in source['parts']:
  if p['name']=='head' or p['parent'] in known:
   p=dict(p);p.setdefault('rotation',[0,0,0]);p.setdefault('offset',[-v/2 for v in p['size']]);selected.append(p);known.add(p['name'])
 write(A/'lost_halls_rigs/zharenitsa_trophy.json',{'atlas':source['atlas'],'parts':selected})
 folder=A/'textures/entity/lost_halls';folder.mkdir(parents=True,exist_ok=True)
 for suffix in ['', '_glow']:(folder/f'zharenitsa_trophy{suffix}.png').write_bytes((A/f'textures/entity/zharenitsa{suffix}.png').read_bytes())
 write(A/'models/item/posten_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
 for name in ['gulokryl','ekhovik','griboshmyg','posten','shadow_face_decoy']:write(D/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':[]})
 for name in ['sheptun','oskolnik']:
  p=D/f'loot_table/entities/{name}.json';data=json.loads(p.read_text())
  data['pools']=[pool for pool in data['pools'] if all(e.get('name') not in {'slavicmyths:soul_fragment','slavicmyths:soul'} for e in pool.get('entries',[]))]
  for item,base,bonus in [('soul_fragment',.25,.05),('soul',.02,.01)]:data['pools'].append({'rolls':1,'conditions':[{'condition':'minecraft:random_chance_with_enchanted_bonus','unenchanted_chance':base,'enchanted_chance':{'type':'minecraft:clamped','value':{'type':'minecraft:linear','base':base+bonus,'per_level_above_first':bonus},'min':base,'max':.4 if item=='soul_fragment' else .05},'enchantment':'minecraft:looting'}],'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item}]})
  write(p,data)
def shadow_language():
 for lang,source,effect in [('ru_ru','Постень — награда за победу','Нагрудник: 4 с невидимости, 3 копии на 6 с; перезарядка 75 с.'),('en_us','Posten — victory reward','Chestplate: invisible for 4 s, 3 decoys for 6 s; cooldown 75 s.')]:
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));data['biome.slavicmyths.lost_halls']='Заблудшие Чертоги' if lang=='ru_ru' else 'Lost Halls';data['entity.slavicmyths.shadow_face_decoy']='Теневой двойник' if lang=='ru_ru' else 'Shadow Decoy';data['entity.slavicmyths.halls_bolt']='Снаряд Чертогов' if lang=='ru_ru' else 'Halls Bolt';data['rare_runes.slavicmyths.source_shadow_face']=source;data['rare_runes.slavicmyths.effect_shadow_face']=effect;write(p,data)

def main():
 blocks()
 for n in MOBS:rig(n)
 icons();loot();world_and_heads();finish();shadow_language()
if __name__=='__main__':main()
