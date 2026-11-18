"""Scoped, reproducible assets for Navya Grove. Never runs legacy asset/audio writers."""
from pathlib import Path
import json
import random
import xml.etree.ElementTree as ET
import zipfile
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
A=RES/'assets/slavicmyths'
D=RES/'data/slavicmyths'
VANILLA=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
GEO=['navya_soil','mossy_navya_soil','navya_stone','cracked_navya_stone','grove_bone_block','grove_bone_column','grove_bone_spike','rooted_navya_stone']
WOOD=['navya_log','navya_wood','stripped_navya_log','stripped_navya_wood','navya_planks','navya_stairs','navya_slab','navya_fence','navya_fence_gate','navya_door','navya_trapdoor','navya_button','navya_pressure_plate','navya_sign','navya_hanging_sign']
FLORA=['navya_dry_branches','navya_roots','navya_hanging_roots','navya_shrub','ghost_fungus','navya_growth','navya_sapling','navya_dead_leaves']
NAMES={
 'navya_soil':('Навий грунт','Navya Soil'),'mossy_navya_soil':('Навий грунт с мёртвым мхом','Dead-Moss Navya Soil'),
 'navya_stone':('Навий камень','Navya Stone'),'cracked_navya_stone':('Потрескавшийся навий камень','Cracked Navya Stone'),
 'grove_bone_block':('Костяной блок чащи','Grove Bone Block'),'grove_bone_column':('Костяная колонна','Grove Bone Column'),
 'grove_bone_spike':('Костяной сталагмит','Grove Bone Spike'),'rooted_navya_stone':('Корневой камень','Rooted Navya Stone'),
 'navya_log':('Бледное навье бревно','Pale Navya Log'),'navya_wood':('Навья древесина','Navya Wood'),
 'stripped_navya_log':('Очищенное навье бревно','Stripped Navya Log'),'stripped_navya_wood':('Очищенная навья древесина','Stripped Navya Wood'),
 'navya_planks':('Навьи доски','Navya Planks'),'navya_stairs':('Навьи ступени','Navya Stairs'),
 'navya_slab':('Навья плита','Navya Slab'),'navya_fence':('Навий забор','Navya Fence'),
 'navya_fence_gate':('Навья калитка','Navya Fence Gate'),'navya_door':('Навья дверь','Navya Door'),
 'navya_trapdoor':('Навий люк','Navya Trapdoor'),'navya_button':('Навья кнопка','Navya Button'),
 'navya_pressure_plate':('Навья нажимная плита','Navya Pressure Plate'),'navya_sign':('Навья табличка','Navya Sign'),
 'navya_hanging_sign':('Навья подвесная табличка','Navya Hanging Sign'),
 'navya_dry_branches':('Сухие навьи ветви','Dry Navya Branches'),'navya_roots':('Навьи корни','Navya Roots'),
 'navya_hanging_roots':('Свисающие навьи корни','Hanging Navya Roots'),'navya_shrub':('Мёртвый навий куст','Dead Navya Shrub'),
 'ghost_fungus':('Призрачный гриб','Ghost Fungus'),'navya_growth':('Светящийся навий нарост','Luminous Navya Growth'),
 'navya_sapling':('Навий росток','Navya Shoot'),'navya_dead_leaves':('Сухая навья поросль','Dry Navya Foliage')}
MOBS={'navka':('Навка','Navka'),'rootling':('Корневик','Rootling'),'bone_crawler':('Костяной ползун','Bone Crawler'),
      'navya_wisp':('Навий огонёк','Navya Wisp'),'grove_guardian':('Сторож чащи','Grove Guardian')}
MATERIALS={'navya_ash':('Навий прах','Navya Ash'),'living_root':('Живой корень','Living Root'),'bone_thorn':('Костяной шип','Bone Thorn'),
           'grove_core':('Сердцевина чащи','Grove Core'),'guardian_branch':('Ветвь стража','Guardian Branch'),'soul_blight':('Скверна души','Soul Blight')}

def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def texture(name,image):
 path=A/'textures/block'/f'{name}.png';path.parent.mkdir(parents=True,exist_ok=True);image.save(path)

def atlas(file):
 root=ET.parse(ROOT/'docs/concepts/navya-grove'/file).getroot()
 rects=list(root.iter('{http://www.w3.org/2000/svg}rect'))
 result=[]
 for card in [r for r in rects if r.get('rx')]:
  x,y=int(card.get('x')),int(card.get('y'))
  pixels=[r for r in rects if r.get('width')=='10' and x<=int(r.get('x'))<x+178 and y<=int(r.get('y'))<y+190]
  sx=min(int(r.get('x')) for r in pixels);sy=min(int(r.get('y')) for r in pixels)
  image=Image.new('RGB',(16,16))
  for p in pixels:image.putpixel(((int(p.get('x'))-sx)//10,(int(p.get('y'))-sy)//10),tuple(bytes.fromhex(p.get('fill')[1:])))
  result.append(image)
 return result

def model(name,parent='cube_all',textures=None,elements=None):
 data={'parent':'minecraft:block/'+parent,'textures':textures or {'all':'slavicmyths:block/'+name}}
 if elements is not None:data={'textures':textures,'elements':elements}
 write(A/'models/block'/f'{name}.json',data)

def loot(name):
 entry={'type':'minecraft:item','name':'slavicmyths:'+name}
 if name=='navya_slab':entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:navya_slab','properties':{'type':'double'}}]}]
 conditions=[{'condition':'minecraft:survives_explosion'}]
 if name=='navya_door':conditions.append({'condition':'minecraft:block_state_property','block':'slavicmyths:navya_door','properties':{'half':'lower'}})
 if name=='navya_dead_leaves':
  entry={'type':'minecraft:alternatives','children':[
   {'type':'minecraft:item','name':'slavicmyths:navya_dead_leaves','conditions':[{'condition':'minecraft:any_of','terms':[
     {'condition':'minecraft:match_tool','predicate':{'items':'minecraft:shears'}},
     {'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]}]},
   {'type':'minecraft:item','name':'slavicmyths:navya_sapling','conditions':[{'condition':'minecraft:random_chance','chance':0.08}]}]}
 write(D/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':conditions,'entries':[entry]}],'random_sequence':'slavicmyths:blocks/'+name})

def tag(namespace,kind,name,values):
 path=RES/'data'/namespace/'tags'/kind/(name+'.json')
 data=json.loads(path.read_text()) if path.exists() else {'replace':False,'values':[]}
 data['values']=list(dict.fromkeys(data['values']+values));write(path,data)

def main():
 geo=atlas('blocks-2.0.2.2-atlas.svg')
 # Turn the schematic's material principles into small, irregular tileable
 # pixel clusters. Do not retain the schematic's repeated illustrative rings.
 soil=Image.new('RGB',(16,16),'#171c20');stone=Image.new('RGB',(16,16),'#373b40')
 rng=random.Random(202202)
 for image,palette,count in [(soil,['#1c2023','#23272b','#2b3033'],42),(stone,['#303238','#45484b','#4d5053'],35)]:
  for _ in range(count):
   x,y=rng.randrange(16),rng.randrange(16);color=tuple(bytes.fromhex(rng.choice(palette)[1:]))
   for dx,dy in [(0,0),(1,0),(0,1)]:image.putpixel(((x+dx)%16,(y+dy)%16),color)
 sd=ImageDraw.Draw(stone);sd.line([(0,3),(3,4),(5,4),(7,7)],fill='#66686b');sd.line([(11,12),(14,10),(15,10)],fill='#595d61')
 cracked=stone.copy();cd=ImageDraw.Draw(cracked)
 cd.line([(2,0),(3,3),(6,5),(5,8),(8,11),(7,15)],fill='#191d22');cd.line([(5,8),(1,10),(0,13)],fill='#1e2227')
 moss=soil.copy();md=ImageDraw.Draw(moss)
 for x,y in [(2,3),(8,1),(11,9),(5,13)]:md.line([(x,y+2),(x,y),(x+1,y+1),(x+3,y-1)],fill='#a9a394')
 rooted=stone.copy();rd=ImageDraw.Draw(rooted)
 rd.line([(1,0),(2,4),(6,6),(8,10),(12,12),(13,15)],fill='#a49d8c',width=2)
 rd.line([(6,6),(12,4),(15,4)],fill='#c6bdab');rd.line([(8,10),(5,13),(4,15)],fill='#7e796e')
 geo[0]=soil;geo[1]=moss;geo[2]=stone;geo[3]=cracked;geo[7]=rooted
 for name,image in zip(GEO,geo):texture(name,image)
 texture('navya_soil_side',soil.transpose(Image.Transpose.ROTATE_90))
 wood=atlas('wood-2.0.2.3-atlas.svg')
 bark,old,end,planks=wood
 for name,image in [('navya_log',bark),('navya_log_weathered',old),('navya_log_top',end),('navya_planks',planks)]:texture(name,image)
 stripped=Image.new('RGB',(16,16));draw=ImageDraw.Draw(stripped);draw.rectangle((0,0,15,15),fill='#b9b2a5')
 for x in [1,4,9,13]:draw.line((x,0,x,15),fill='#8e8a80');draw.point((x+1,6),fill='#d2cab9')
 texture('stripped_navya_log',stripped);texture('stripped_navya_log_top',end)
 side=geo[0].copy();sd=ImageDraw.Draw(side)
 for x in [2,6,11,14]:sd.line((x,0,x,2+(x%3)),fill='#969489')
 texture('mossy_navya_soil_side',side)
 # Cubes retain distinct top/side drawings; all are non-emissive.
 for name in GEO:
  if name in ['grove_bone_block','grove_bone_column']:continue
  if name=='grove_bone_spike':continue
  if name=='mossy_navya_soil':model(name,'cube_bottom_top',{'top':'slavicmyths:block/'+name,'side':'slavicmyths:block/'+name+'_side','bottom':'slavicmyths:block/navya_soil'})
  elif name=='navya_soil':model(name,'cube_bottom_top',{'top':'slavicmyths:block/navya_soil','side':'slavicmyths:block/navya_soil_side','bottom':'slavicmyths:block/navya_soil'})
  else:model(name)
  write(A/'blockstates'/f'{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}})
 for name in ['grove_bone_block','grove_bone_column']:
  top=geo[4].copy();draw=ImageDraw.Draw(top)
  for r in [1,4,7]:draw.rectangle((r,r,15-r,15-r),outline='#8c8170')
  texture(name+'_top',top);model(name,'cube_column',{'side':'slavicmyths:block/'+name,'end':'slavicmyths:block/'+name+'_top'})
  write(A/'blockstates'/f'{name}.json',{'variants':{'axis=y':{'model':'slavicmyths:block/'+name},'axis=x':{'model':'slavicmyths:block/'+name,'x':90,'y':90},'axis=z':{'model':'slavicmyths:block/'+name,'x':90}}})
 parts=[]
 for a,b,t in [([0,0,0],[16,3,16],'base'),([2,3,3],[7,11,8],'bone'),([3,11,4],[6,16,7],'bone'),([9,3,9],[14,9,14],'bone')]:
  parts.append({'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]} for f in ['up','down','north','south','east','west']}})
 model('grove_bone_spike',textures={'base':'slavicmyths:block/navya_stone','bone':'slavicmyths:block/grove_bone_block','particle':'slavicmyths:block/grove_bone_block'},elements=parts)
 write(A/'blockstates/grove_bone_spike.json',{'variants':{'tip_direction=up':{'model':'slavicmyths:block/grove_bone_spike'},'tip_direction=down':{'model':'slavicmyths:block/grove_bone_spike','x':180}}})
 # Use vanilla shape/state templates for the actual same-purpose wood family.
 replacements={f'oak_{n}':f'navya_{n}' for n in ['planks','stairs','slab','fence','fence_gate','door','trapdoor','button','pressure_plate','sign','wall_sign','hanging_sign','wall_hanging_sign','log','wood']}
 replacements.update({'stripped_oak_log':'stripped_navya_log','stripped_oak_wood':'stripped_navya_wood','oak_logs':'navya_logs'})
 def rewrite(value):
  if isinstance(value,dict):return {k:rewrite(v) for k,v in value.items()}
  if isinstance(value,list):return [rewrite(v) for v in value]
  if isinstance(value,str):
   for old,new in sorted(replacements.items(),key=lambda x:-len(x[0])):
    if old in value and value.startswith('minecraft:'):
     return value.replace('minecraft:','slavicmyths:').replace(old,new)
  return value
 with zipfile.ZipFile(VANILLA) as z:
  copied=set()
  def copy_model(identifier):
   if not identifier.startswith('minecraft:block/') or 'oak' not in identifier:return
   name=identifier.split(':',1)[1];target=rewrite(identifier).split(':',1)[1]
   if target in copied:return
   copied.add(target);data=json.loads(z.read('assets/minecraft/models/'+name+'.json'));write(A/'models'/(target+'.json'),rewrite(data))
   copy_model(data.get('parent',''))
  for old,new in replacements.items():
   if old=='oak_logs':continue
   data=json.loads(z.read('assets/minecraft/blockstates/'+old+'.json'))
   def collect(v):
    if isinstance(v,dict):
     if 'model' in v:copy_model(v['model'])
     for x in v.values():collect(x)
    elif isinstance(v,list):
     for x in v:collect(x)
   collect(data);write(A/'blockstates'/(new+'.json'),rewrite(data))
   if 'wall_' not in old:
    item_model=json.loads(z.read('assets/minecraft/models/item/'+old+'.json'))
    copy_model(item_model.get('parent',''))
    write(A/'models/item'/(new+'.json'),rewrite(item_model))
    try:write(D/'recipe'/(new+'.json'),rewrite(json.loads(z.read('data/minecraft/recipe/'+old+'.json'))))
    except KeyError:pass
 # One log ID with two bark ages: FallingTree need not bridge separate log IDs.
 for name in ['navya_log','navya_wood','stripped_navya_log','stripped_navya_wood']:
  stripped=name.startswith('stripped');base='stripped_navya_log' if stripped else 'navya_log'
  variants={}
  for weathered in [False,True]:
   suffix='_weathered' if weathered and not stripped else ''
   mn=name+suffix
   model(mn,'cube_column',{'side':'slavicmyths:block/'+base+suffix,'end':'slavicmyths:block/'+(base+suffix if name.endswith('wood') else base+'_top')})
   for axis,rotation in [('y',{}),('x',{'x':90,'y':90}),('z',{'x':90})]:variants[f'axis={axis},weathered={str(weathered).lower()}']={'model':'slavicmyths:block/'+mn,**rotation}
  write(A/'blockstates'/(name+'.json'),{'variants':variants})
 # Purpose-drawn door, trapdoor and inventory sign silhouettes.
 for name in ['navya_door_bottom','navya_door_top','navya_trapdoor']:
  im=planks.copy();d=ImageDraw.Draw(im)
  if name=='navya_door_top':d.rectangle((3,2,12,10),fill=(0,0,0));im=im.convert('RGBA');ImageDraw.Draw(im).rectangle((4,3,11,9),fill=(0,0,0,0))
  elif name=='navya_door_bottom':d.line((0,3,15,3),fill='#5b5652');d.line((0,12,15,12),fill='#5b5652');d.point((12,5),fill='#352e33')
  else:
   im=im.convert('RGBA');d=ImageDraw.Draw(im)
   for box in [(3,3,6,6),(9,3,12,6),(3,9,6,12),(9,9,12,12)]:d.rectangle(box,fill=(0,0,0,0))
  texture(name,im)
 for name in ['navya_sign','navya_hanging_sign']:
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.rectangle((1,3,14,10),fill='#b4ada0',outline='#5b5652');d.line((3,5,12,5),fill='#77716a')
  if name.endswith('hanging_sign'):
   for x in [3,12]:d.line((x,0,x,3),fill='#77716a')
  else:d.rectangle((7,11,8,15),fill='#77716a')
  path=A/'textures/item'/f'{name}.png';path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
 door=Image.new('RGBA',(16,16));dd=ImageDraw.Draw(door)
 dd.rectangle((4,0,11,15),fill='#b4ada0',outline='#5b5652');dd.rectangle((5,2,10,7),fill=(0,0,0,0));dd.point((10,9),fill='#51464b')
 door.save(A/'textures/item/navya_door.png')
 # Standalone sign atlas retains correct renderer dimensions and own wood palette.
 for folder in ['signs','signs/hanging']:
  im=Image.new('RGBA',(64,32));d=ImageDraw.Draw(im)
  for y in range(32):
   for x in range(64):im.putpixel((x,y),(*planks.getpixel((x%16,y%16)),255))
  path=A/'textures/entity'/folder/'navya.png';path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
 # Several distinct transparent flora silhouettes, not recolours of one cross.
 for i,name in enumerate(FLORA):
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
  if name=='ghost_fungus':
   d.rectangle((7,7,8,15),fill='#b4c6bf');d.polygon([(2,7),(4,3),(7,1),(10,3),(14,7)],fill='#a3c8c5');d.line((3,7,13,7),fill='#d8e2d1');d.rectangle((5,4,6,5),fill='#64827e')
  elif name=='navya_growth':
   for x,y in [(3,12),(7,8),(11,13),(9,3)]:d.rectangle((x,y,x+2,y+2),fill='#a8ccc3');d.point((x+1,y),fill='#dbe5cd')
  elif name=='navya_dead_leaves':
   for x,y in [(1,2),(8,1),(12,8),(2,11),(7,12)]:d.line((x,y,min(15,x+3),min(15,y+3)),fill='#8c8880',width=2)
  else:
   d.line((7,15,8,3 if name!='navya_shrub' else 8),fill='#9d988c',width=2)
   for j,(x,y) in enumerate([(2,9),(12,6),(3,2),(13,12)]):
    if name=='navya_sapling' and j>1:continue
    d.line((7,12-j*2,x,y),fill='#bdb6a5',width=1 if 'branches' in name else 2)
   if name=='navya_roots':d.line((2,15,13,15),fill='#6d6661',width=2)
   if name=='navya_hanging_roots':im=im.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
  texture(name,im)
  if name=='navya_dead_leaves':model(name,'leaves')
  else:model(name,'cross',{'cross':'slavicmyths:block/'+name})
  write(A/'blockstates'/(name+'.json'),{'variants':{'':{'model':'slavicmyths:block/'+name}}})
 for name in GEO+WOOD+FLORA:
  if name not in WOOD:write(A/'models/item'/(name+'.json'),{'parent':'slavicmyths:block/'+name})
  loot(name)
 write(D/'worldgen/configured_feature/navya_tree.json',{'type':'slavicmyths:navya_tree','config':{}})
 for locale,index in [('ru_ru',0),('en_us',1)]:
  path=A/'lang'/(locale+'.json');data=json.loads(path.read_text(encoding='utf-8'))
  data['navya.command.tree']='Навье дерево создано: %s, %s, %s' if index==0 else 'Navya tree created: %s, %s, %s'
  data['navya.command.blocked']='Нужно свободное место над навьим грунтом; строения и соседние деревья не заменяются.' if index==0 else 'Free space above Navya soil is required; builds and adjacent trees are preserved.'
  write(path,data)
 logs=['slavicmyths:'+n for n in ['navya_log','navya_wood','stripped_navya_log','stripped_navya_wood']]
 for kind in ['block','item']:
  tag('slavicmyths',kind,'navya_logs',logs);tag('minecraft',kind,'logs',['#slavicmyths:navya_logs'])
  tag('minecraft',kind,'leaves',['slavicmyths:navya_dead_leaves']);tag('minecraft',kind,'planks',['slavicmyths:navya_planks'])
  tag('minecraft',kind,'saplings',['slavicmyths:navya_sapling'])
 for category,names in [('mineable/pickaxe',GEO[2:]),('mineable/shovel',GEO[:2]),('mineable/axe',WOOD),('fences',['navya_fence']),('wooden_fences',['navya_fence']),('fence_gates',['navya_fence_gate']),('wooden_stairs',['navya_stairs']),('wooden_slabs',['navya_slab']),('wooden_doors',['navya_door']),('wooden_trapdoors',['navya_trapdoor']),('wooden_buttons',['navya_button']),('wooden_pressure_plates',['navya_pressure_plate']),('standing_signs',['navya_sign']),('wall_signs',['navya_wall_sign']),('ceiling_hanging_signs',['navya_hanging_sign']),('wall_hanging_signs',['navya_wall_hanging_sign'])]:tag('minecraft','block',category,['slavicmyths:'+n for n in names])
 # Item tags for vanilla recipes/enchanting and sign placement conventions.
 for category,names in [('wooden_stairs',['navya_stairs']),('wooden_slabs',['navya_slab']),('wooden_fences',['navya_fence']),('fence_gates',['navya_fence_gate']),('wooden_doors',['navya_door']),('wooden_trapdoors',['navya_trapdoor']),('wooden_buttons',['navya_button']),('wooden_pressure_plates',['navya_pressure_plate']),('signs',['navya_sign']),('hanging_signs',['navya_hanging_sign'])]:tag('minecraft','item',category,['slavicmyths:'+n for n in names])
 for locale,index in [('ru_ru',0),('en_us',1)]:
  path=A/'lang'/(locale+'.json');data=json.loads(path.read_text(encoding='utf-8'))
  for name,pair in NAMES.items():data['block.slavicmyths.'+name]=pair[index]
  for name,pair in MOBS.items():
   data['entity.slavicmyths.'+name]=pair[index]
   data['item.slavicmyths.'+name+'_spawn_egg']=('Яйцо призыва: '+pair[index]) if index==0 else pair[index]+' Spawn Egg'
  for name,pair in MATERIALS.items():data['item.slavicmyths.'+name]=pair[index]
  data['biome.slavicmyths.navya_grove']='Навья роща' if index==0 else 'Navya Grove'
  write(path,data)
 catalog=A/'creative_catalog.tsv';text=catalog.read_text(encoding='utf-8');known={line.split('\t')[0] for line in text.splitlines()}
 for name in GEO+WOOD+FLORA:
  if name not in known:text+=f'{name}\tdecor\tnavya_grove\n'
 for name in MOBS:
  egg=name+'_spawn_egg'
  write(A/'models/item'/(egg+'.json'),{'parent':'minecraft:item/template_spawn_egg'})
  if egg not in known:text+=f'{egg}\tmobs\tnavya_grove\n'
 for name in MATERIALS:
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
  if name=='navya_ash':
   d.polygon([(1,13),(5,8),(7,10),(10,6),(14,12),(13,15),(3,15)],fill='#66616d',outline='#343039')
   for xy in [(5,11),(9,10),(11,13),(6,14)]:d.point(xy,fill='#b4a9b6')
  elif name=='bone_thorn':
   d.polygon([(3,15),(5,10),(6,5),(11,0),(10,6),(9,11),(7,15)],fill='#bdb4a0',outline='#5b5353');d.line((6,13,9,4),fill='#e0d4b9')
  elif name=='living_root':
   d.line([(7,15),(7,10),(5,7),(7,1)],fill='#66574e',width=3)
   for poly in [[(7,12),(12,9),(13,5)],[(6,8),(2,6),(1,2)],[(7,4),(11,2)]]:d.line(poly,fill='#a49983',width=2)
   d.rectangle((6,7,7,8),fill='#8f3766')
  elif name=='guardian_branch':
   d.line([(4,15),(7,10),(7,5),(10,1)],fill='#3d2e33',width=5);d.line([(7,7),(12,7),(14,3)],fill='#705047',width=3)
   d.line((5,14,8,5),fill='#912b51');d.rectangle((9,6,11,7),fill='#a48c7c')
  elif name=='soul_blight':
   d.polygon([(7,0),(12,4),(10,8),(14,11),(10,15),(4,14),(1,9),(4,5)],fill='#4c1848',outline='#241b30')
   d.line([(7,3),(9,6),(6,8),(9,11),(7,13)],fill='#be4d87',width=2);d.point((5,11),fill='#e59a9f')
  else:
   d.polygon([(8,0),(13,4),(14,10),(10,15),(4,14),(1,8),(3,2)],fill='#35272e',outline='#a38b7a')
   d.polygon([(7,3),(11,5),(11,10),(8,13),(4,10),(4,6)],fill='#8e1e58');d.line([(6,5),(8,4),(10,7),(8,10)],fill='#e24d30',width=2)
  path=A/'textures/item'/(name+'.png');path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
  write(A/'models/item'/(name+'.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
  if name not in known:text+=f'{name}\tresources\tnavya_grove\n'
 catalog.write_text(text,encoding='utf-8')
 report=ROOT/'docs/implementation/navya-grove';report.mkdir(parents=True,exist_ok=True)
 sheet=Image.new('RGB',(640,320),'#1b1d23');d=ImageDraw.Draw(sheet)
 for i,name in enumerate(GEO+['navya_log','navya_log_weathered','stripped_navya_log','navya_planks']):
  x=(i%4)*160;y=(i//4)*104
  im=Image.open(A/'textures/block'/(name+'.png')).convert('RGB').resize((64,64),Image.Resampling.NEAREST);sheet.paste(im,(x+48,y+6));d.text((x+3,y+76),name,fill='#e5ddcf')
 sheet.save(report/'actual-texture-sheet.png')
 write(report/'asset-manifest.json',{'geology':GEO,'wood':WOOD,'flora':FLORA,'sources':['blocks-2.0.2.2-atlas.svg','wood-2.0.2.3-atlas.svg'],'status':'OWNER_VISUAL_PENDING'})
 print('Navya assets written:',len(GEO+WOOD+FLORA),'inventory entries')

if __name__=='__main__':main()
