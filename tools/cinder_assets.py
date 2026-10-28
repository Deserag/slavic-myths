"""Scoped reproducible Black Cinder assets authored from RECOVERED_VISUAL_SPEC.
Does not claim original PNG identity. Never runs any legacy/audio generator.
"""
from pathlib import Path
import json, random, zipfile, math
from PIL import Image, ImageDraw
R=Path(__file__).resolve().parents[1]; RES=R/'src/main/resources'; A=RES/'assets/slavicmyths'; D=RES/'data/slavicmyths'
V=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
NAMES={
 'black_cinder_soil':('Обожжённая земля','Scorched Soil'),'ash_crust':('Пепельный наст','Ash Crust'),
 'smoking_cinder':('Дымящаяся гарь','Smoking Cinder'),'charred_stone':('Обугленный камень','Charred Stone'),
 'cracked_charred_stone':('Треснувший обугленный камень','Cracked Charred Stone'),
 'blackwood_log':('Бревно чернодрева','Blackwood Log'),'charred_blackwood_log':('Обгорелое бревно чернодрева','Burnt Blackwood Log'),
 'blackwood_wood':('Чернодрево','Blackwood Wood'),'charred_blackwood_wood':('Обгорелое чернодрево','Burnt Blackwood Wood'),
 'blackwood_planks':('Доски чернодрева','Blackwood Planks'),'charred_blackwood_leaves':('Обугленная крона','Charred Blackwood Foliage'),
 'zharenitsa_head':('Голова Жареницы','Zharenitsa Head')}
MOBS={'coal_crawler':('Углеполз','Coal Crawler'),'ash_spirit':('Пепельный дух','Ash Spirit'),'pekelnik':('Пекельник','Pekelnik'),'sparkling':('Искровик','Sparkling'),'zharenitsa':('Жареница — Хозяйка гари','Zharenitsa, Mistress of Cinders')}
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def tag(ns,kind,name,values):
 p=RES/f'data/{ns}/tags/{kind}/{name}.json'; d=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}; d['values']=list(dict.fromkeys(d['values']+values));write(p,d)
def texture(name,style):
 rng=random.Random(name); im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
 palette={'soil':['#252122','#332826','#1d1b1d'],'ash':['#93918a','#74736f','#aaa79b'],
          'stone':['#292729','#353034','#1e1f22'],'bark':['#241d1b','#382b24','#171719'],
          'plank':['#3b2d26','#4d3a2c','#2b2421'],'end':['#382b24','#514031','#241e1c']}
 pal=palette.get(style,palette['stone']);d.rectangle((0,0,15,15),fill=pal[0])
 for _ in range(34):
  x,y=rng.randrange(16),rng.randrange(16);d.rectangle((x,y,min(15,x+rng.randrange(1,4)),min(15,y+1)),fill=rng.choice(pal))
 if style=='bark':
  for x in (1,5,10,14):d.line((x,0,x-1,7,x,15),fill='#110f11')
 if style=='end':
  for r in (1,4,6):d.rectangle((r,r,15-r,15-r),outline='#171516')
 if style=='plank':
  for y in (3,7,11,15):d.line((0,y,15,y),fill='#171719')
  for x,y in ((4,0),(11,4),(7,8),(2,12)):d.line((x,y,x,y+3),fill='#211b1a')
 if 'charred_stone' in name or name=='charred_blackwood_log':
  x=rng.randrange(3,12);points=[(x,0)]
  for y in (3,7,11,15):x=max(1,min(14,x+rng.choice([-3,-2,2,3])));points.append((x,y))
  d.line(points,fill='#ad4d22',width=2 if 'cracked' in name else 1)
  if 'cracked' in name:
   for at,end in [(points[1],(15,5)),(points[2],(0,10)),(points[3],(14,15))]:d.line([at,((at[0]+end[0])//2,at[1]+2),end],fill='#b85b28')
  for x,y in points[1:4]:d.point((x,y),fill='#ee9b43')
 if 'smoking' in name:
  for x,y in ((3,5),(11,10),(7,13)):d.rectangle((x,y,x+1,y+1),fill='#5c3024');d.point((x,y),fill='#ba5d23')
 if name=='black_cinder_soil':
  for x,y in ((3,9),(12,2)):d.point((x,y),fill='#9c4221')
 if 'leaves' in name:
  for _ in range(70):im.putpixel((rng.randrange(16),rng.randrange(16)),(0,0,0,0))
 path=A/f'textures/block/{name}.png';path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
 return im
def model(name,parent='cube_all',textures=None):write(A/f'models/block/{name}.json',{'parent':'minecraft:block/'+parent,'textures':textures or {'all':'slavicmyths:block/'+name}})
def loot(name):
 entry={'type':'minecraft:item','name':'slavicmyths:'+name};conditions=[{'condition':'minecraft:survives_explosion'}]
 if name.endswith('_slab'):entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'type':'double'}}]}]
 if name.endswith('_door'):conditions.append({'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'half':'lower'}})
 if name.endswith('_leaves'):conditions.append({'condition':'minecraft:any_of','terms':[{'condition':'minecraft:match_tool','predicate':{'items':'minecraft:shears'}},{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]})
 write(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry],'conditions':conditions}]})
def blocks():
 mapping={f'oak_{part}':f'blackwood_{part}' for part in ('planks','stairs','slab','fence','fence_gate','door','trapdoor','button','pressure_plate','log','wood')}
 for stone in ('charred_stone','cracked_charred_stone'):
  for part in ('stairs','slab','wall'):mapping['blackstone_'+part if stone=='charred_stone' else 'polished_blackstone_'+part]=stone+'_'+part
 mapping.update({'blackstone':'charred_stone','polished_blackstone':'cracked_charred_stone'})
 def rewrite(v):
  if isinstance(v,dict):return {k:rewrite(x) for k,x in v.items()}
  if isinstance(v,list):return [rewrite(x) for x in v]
  if isinstance(v,str) and v.startswith('minecraft:'):
   prefix,leaf=v.rsplit('/',1) if '/' in v else ('minecraft:',v.split(':')[1]);prefix=prefix+'/' if '/' in v else prefix
   for old,new in sorted(mapping.items(),key=lambda x:-len(x[0])):
    if leaf==old or leaf.startswith(old+'_'):return prefix.replace('minecraft:','slavicmyths:')+new+leaf[len(old):]
  return v
 with zipfile.ZipFile(V) as z:
  copied=set()
  def copy_model(identifier):
   target=rewrite(identifier)
   if target==identifier or target in copied:return
   copied.add(target);p=identifier.split(':')[1];data=json.loads(z.read('assets/minecraft/models/'+p+'.json'));write(A/('models/'+target.split(':')[1]+'.json'),rewrite(data));copy_model(data.get('parent',''))
  def visit(v):
   if isinstance(v,dict):
    if 'model' in v:copy_model(v['model'])
    for x in v.values():visit(x)
   elif isinstance(v,list):
    for x in v:visit(x)
  for old,new in mapping.items():
   if old in ('blackstone','polished_blackstone'):continue
   state=json.loads(z.read('assets/minecraft/blockstates/'+old+'.json'));visit(state);write(A/f'blockstates/{new}.json',rewrite(state))
   item=json.loads(z.read('assets/minecraft/models/item/'+old+'.json'));copy_model(item.get('parent',''));write(A/f'models/item/{new}.json',rewrite(item))
   recipe='data/minecraft/recipe/'+old+'.json'
   if recipe in z.namelist():write(D/f'recipe/{new}.json',rewrite(json.loads(z.read(recipe))))
 for name,style in [('black_cinder_soil','soil'),('ash_crust','ash'),('smoking_cinder','stone'),('charred_stone','stone'),('cracked_charred_stone','stone'),('blackwood_log','bark'),('charred_blackwood_log','bark'),('blackwood_log_top','end'),('blackwood_planks','plank'),('charred_blackwood_leaves','stone')]:texture(name,style)
 texture('charred_stone_top','stone')
 for name in ('black_cinder_soil','ash_crust','smoking_cinder','charred_stone','cracked_charred_stone','charred_blackwood_leaves'):
  if name in ('black_cinder_soil','ash_crust'):
   texture(name+'_side','soil' if name=='black_cinder_soil' else 'ash');model(name,'cube_bottom_top',{'top':'slavicmyths:block/'+name,'side':'slavicmyths:block/'+name+'_side','bottom':'slavicmyths:block/black_cinder_soil'})
  else:model(name,'leaves' if name.endswith('leaves') else 'cube_all')
  write(A/f'blockstates/{name}.json',{'variants':{'':[{'model':'slavicmyths:block/'+name,'y':angle} for angle in (0,90,180,270)]}});write(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name})
 for name in ('blackwood_log','charred_blackwood_log','blackwood_wood','charred_blackwood_wood'):
  side='charred_blackwood_log' if name.startswith('charred') else 'blackwood_log';model(name,'cube_column',{'side':'slavicmyths:block/'+side,'end':'slavicmyths:block/'+('blackwood_log_top' if name.endswith('log') else side)})
  write(A/f'blockstates/{name}.json',{'variants':{'axis=y':{'model':'slavicmyths:block/'+name},'axis=x':{'model':'slavicmyths:block/'+name,'x':90,'y':90},'axis=z':{'model':'slavicmyths:block/'+name,'x':90}}});write(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name})
 for name in ('blackwood_door_bottom','blackwood_door_top','blackwood_trapdoor'):
  im=texture(name,'plank');d=ImageDraw.Draw(im)
  if name.endswith('top'):d.rectangle((4,2,11,8),fill=(0,0,0,0))
  if name.endswith('trapdoor'):
   for x,y in ((3,3),(9,3),(3,9),(9,9)):d.rectangle((x,y,x+3,y+3),fill=(0,0,0,0))
  d.line((1,1,1,14),fill='#70665c');im.save(A/f'textures/block/{name}.png')
 door=Image.new('RGBA',(16,16));d=ImageDraw.Draw(door);d.rectangle((4,0,11,15),fill='#3b2d26',outline='#73695d');d.rectangle((5,2,10,7),fill=(0,0,0,0));p=A/'textures/item/blackwood_door.png';p.parent.mkdir(parents=True,exist_ok=True);door.save(p)
 # Sculpted head with actual crown geometry, rotations and placement-facing state.
 elements=[]
 def box(a,b,tex):elements.append({'from':a,'to':b,'faces':{f:{'texture':'#'+tex,'uv':[0,0,16,16]} for f in ('up','down','north','south','east','west')}})
 box([3,0,3],[13,10,13],'head')
 for x,h in ((2,14),(5,16),(9,15),(12,13)):box([x,8,2],[x+2,h,4],'crown');box([x,8,12],[x+2,h,14],'crown')
 write(A/'models/block/zharenitsa_head.json',{'textures':{'head':'slavicmyths:block/zharenitsa_face','crown':'slavicmyths:block/charred_blackwood_log','particle':'slavicmyths:block/charred_stone'},'elements':elements})
 face=texture('zharenitsa_face','stone');d=ImageDraw.Draw(face);d.rectangle((3,5,5,7),fill='#fa8c27');d.rectangle((10,5,12,7),fill='#fa8c27');face.save(A/'textures/block/zharenitsa_face.png')
 write(A/'blockstates/zharenitsa_head.json',{'variants':{f'facing={direction}':{'model':'slavicmyths:block/zharenitsa_head','y':angle} for direction,angle in [('north',0),('east',90),('south',180),('west',270)]}});write(A/'models/item/zharenitsa_head.json',{'parent':'slavicmyths:block/zharenitsa_head'})
 # Enumerate actual registered IDs from the scoped Java source, including loop forms.
 names=list(NAMES)
 for base in ('charred_stone','cracked_charred_stone'):
  for suffix,ru,en in [('stairs','ступени','Stairs'),('slab','плита','Slab'),('wall','ограда','Wall')]:
   name=base+'_'+suffix;NAMES[name]=(NAMES[base][0]+' — '+ru,NAMES[base][1]+' '+en);names.append(name)
 for suffix,ru,en in [('stairs','Ступени','Stairs'),('slab','Плита','Slab'),('fence','Забор','Fence'),('fence_gate','Калитка','Fence Gate'),('door','Дверь','Door'),('trapdoor','Люк','Trapdoor'),('button','Кнопка','Button'),('pressure_plate','Нажимная плита','Pressure Plate')]:
  name='blackwood_'+suffix;NAMES[name]=(ru+' чернодрева','Blackwood '+en);names.append(name)
 for name in names:loot(name)
 for stone in ('charred_stone','cracked_charred_stone'):
  for suffix,count in [('stairs',1),('slab',2),('wall',1)]:write(D/f'recipe/{stone}_{suffix}_from_stonecutting.json',{'type':'minecraft:stonecutting','ingredient':{'item':'slavicmyths:'+stone},'result':{'id':'slavicmyths:'+stone+'_'+suffix,'count':count}})
 write(D/'recipe/blackwood_planks.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'tag':'slavicmyths:blackwood_logs'}],'result':{'id':'slavicmyths:blackwood_planks','count':4}})
 for log in ('blackwood_log','charred_blackwood_log'):
  wood=log.replace('_log','_wood');write(D/f'recipe/{wood}.json',{'type':'minecraft:crafting_shaped','pattern':['LL','LL'],'key':{'L':{'item':'slavicmyths:'+log}},'result':{'id':'slavicmyths:'+wood,'count':3}})
 logs=['slavicmyths:'+n for n in ('blackwood_log','charred_blackwood_log','blackwood_wood','charred_blackwood_wood')]
 for kind in ('block','item'):
  tag('slavicmyths',kind,'blackwood_logs',logs);tag('minecraft',kind,'logs', ['#slavicmyths:blackwood_logs']);tag('minecraft',kind,'non_flammable_wood',logs+['slavicmyths:blackwood_planks']);tag('minecraft',kind,'planks',['slavicmyths:blackwood_planks']);tag('minecraft',kind,'leaves',['slavicmyths:charred_blackwood_leaves'])
 tag('minecraft','block','mineable/pickaxe',['slavicmyths:'+n for n in names if 'stone' in n or n in ('smoking_cinder','zharenitsa_head')])
 tag('minecraft','block','mineable/shovel',['slavicmyths:black_cinder_soil','slavicmyths:ash_crust'])
 tag('minecraft','block','mineable/axe',['slavicmyths:'+n for n in names if 'blackwood' in n and not n.endswith('leaves')])
 for suffix,tagname in [('stairs','stairs'),('slab','slabs'),('wall','walls'),('fence','wooden_fences'),('fence_gate','fence_gates'),('door','wooden_doors'),('trapdoor','wooden_trapdoors'),('button','wooden_buttons'),('pressure_plate','wooden_pressure_plates')]:
  for kind in ('block','item'):tag('minecraft',kind,tagname,['slavicmyths:'+n for n in names if n.endswith('_'+suffix)])
 return names

def rigs():
 allrigs={}
 for name in MOBS:
  parts=[]
  def box(n,p,size,material='coal',parent='root'):
   parts.append({'name':n,'parent':parent,'pivot':p,'size':size,'material':material})
  if name=='coal_crawler':
   box('body',[0,12,0],[22,10,26]);box('head',[0,11,-17],[16,9,12]);box('jaw',[0,5,-2],[14,3,10],'coal','head');
   for x in (-4,4):box('eye'+str(x),[x,-1,-6.5],[2,2,1],'ember','head')
   for i,(x,z) in enumerate([(-12,-8),(12,-8),(-11,10),(11,10)]):box('leg'+str(i),[x,17,z],[7,13,8])
   for i in range(8):
    box('spine'+str(i),[(-1 if i%2 else 1)*6,4,-12+i*3],[3,6+i%3,4]);box('hot_tip'+str(i),[0,-3,0],[2,2,3],'ember','spine'+str(i))
   for i in range(6):box('tooth'+str(i),[-6+i*2.3,3,-7],[1,3,2],'ash','head')
  elif name=='ash_spirit':
   box('face',[0,2,-1],[10,9,8]);box('cloud',[0,5,2],[17,10,13],'ash');box('whorl',[0,15,0],[7,11,7],'ash')
   for i in range(9):box('cloud'+str(i),[math.cos(i*2.4)*11,3+(i%4)*5,math.sin(i*2.4)*10],[4+i%3,4+i%2,4],'ash')
   for x in (-3,3):box('eye'+str(x),[x,-2,-4.5],[2,2,1],'ember','face')
  elif name=='sparkling':
   box('face',[0,18,0],[6,6,6]);
   for x in (-2,2):box('eye'+str(x),[x,-1,-3.5],[2,2,1],'ember','face')
   for i in range(6):box('fleck'+str(i),[math.cos(i)*5,17+i%3*2,math.sin(i)*5],[1,1,1],'ember')
  elif name=='pekelnik':
   box('body',[0,-4,0],[19,22,11]);box('head',[0,-21,-1],[12,12,11]);
   for i,x in enumerate((-7,7)):box('leg'+str(i),[x,15,0],[8,18,9])
   for i,x in enumerate((-14,14)):
    box('arm'+str(i),[x,-9,0],[8,25,8]);
    for j in range(3):box('claw'+str(i)+str(j),[0,16,-3+j*3],[2,11,2],'ember','arm'+str(i))
   box('heart',[0,-5,-6],[5,9,1],'ember')
   for x in (-3,3):box('eye'+str(x),[x,-2,-5.5],[2,2,1],'ember','head')
  else:
   box('body',[0,-17,0],[13,18,8]);box('waist',[0,-4,0],[9,8,7]);box('head',[0,-33,0],[9,10,9]);box('heart',[0,-19,-4.5],[5,7,2],'ember')
   for x in (-2,2):box('eye'+str(x),[x,-1,-5],[2,2,1],'ember','head')
   for i,x in enumerate((-11,11)):
    box('arm'+str(i),[x,-22,0],[4,22,5]);box('hand'+str(i),[0,15,0],[4,9,3],'ember','arm'+str(i))
   for i in range(12):
    a=i*math.pi/6;box('crown'+str(i),[math.cos(a)*6,-8-i%3*2,math.sin(a)*6],[2,6+i%4,2],'ember' if i%3==0 else 'coal','head')
   for i in range(10):
    a=i*math.pi/5;box('ribbon'+str(i),[math.cos(a)*5,-3,math.sin(a)*5],[3,29+i%3*2,3]);parts[-1]['offset']=[-1.5,0,-1.5];parts[-1]['rotation']=[math.sin(a)*.24,0,-math.cos(a)*.32];box('tip'+str(i),[0,29+i%3*2,0],[2,3,2],'ember','ribbon'+str(i))
   for i in range(5):box('hair'+str(i),[-5+i*2.5,10,5],[2,18+i%2*4,3],'ash','head')
  from nether_boss327_geometry import cinder
  cinder(name,parts)
  # Disjoint UV islands, also used directly to author the matching textures.
  cursor_x=2;cursor_y=2;rowh=0;atlas=512
  im=Image.new('RGBA',(atlas,atlas));glow=Image.new('RGBA',(atlas,atlas));rng=random.Random(name)
  for part in parts:
   sx,sy,sz=part['size'];w=2*(sx+sz);h=sy+sz
   if cursor_x+w+2>atlas:cursor_x=2;cursor_y+=rowh+2;rowh=0
   assert cursor_y+h+2<=atlas
   part['uv']=[cursor_x,cursor_y];rowh=max(rowh,h)
   palette={'coal':[(32,29,30),(45,39,36),(22,23,25)],'ash':[(108,105,101),(77,76,75),(139,134,125)],'ember':[(204,76,20),(245,132,35),(255,181,62)]}[part['material']]
   if name=='zharenitsa' and part['material']=='ash':palette=[(76,72,75),(56,53,59),(107,101,102)]
   for y in range(cursor_y,cursor_y+h):
    for x in range(cursor_x,cursor_x+w):
     rgb=rng.choice(palette) if (x//2+y//3)%5==0 else palette[0];im.putpixel((x,y),(*rgb,255))
     if part['material']=='ember':glow.putpixel((x,y),(*rgb,255))
   # Local fractured incandescent veins in dark body surfaces, never full-body glow.
   if part['material']=='coal' and part['name'] in ('body','head','face'):
    d=ImageDraw.Draw(im);g=ImageDraw.Draw(glow)
    for off in range(0,w,13):
     points=[(cursor_x+off,cursor_y+2),(cursor_x+off+2,cursor_y+h//2),(cursor_x+min(w-1,off+1),cursor_y+h-1)];d.line(points,fill='#c66025');g.line(points,fill='#b34b1f')
   cursor_x+=w+2
  out=A/'textures/entity';out.mkdir(parents=True,exist_ok=True)
  for image,suffix in [(im,''),(glow,'_glow')]:
   path=out/(name+suffix+'.png')
   if not path.exists() or Image.open(path).convert('RGBA').tobytes()!=image.tobytes():image.save(path)
  write(A/f'cinder_rigs/{name}.json',{'atlas':atlas,'parts':parts});allrigs[name]=parts
 write(R/'docs/implementation/black-cinder-production/rig-ledger.json',allrigs)

def main():
 names=blocks();rigs()
 materials={'zharenitsa_heat_seal':('Жаростойкая печать','Heatproof Seal'),'rune_ash_mantle':('Руна Пепельного покрова','Rune of the Ash Mantle')}
 for i,name in enumerate(materials):
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
  if i==0:d.polygon([(8,1),(13,5),(12,12),(7,15),(2,10),(3,4)],fill='#34282c',outline='#86776e');d.polygon([(8,4),(10,8),(8,12),(6,8)],fill='#ed7927')
  else:d.ellipse((1,1,14,14),fill='#302b2a',outline='#ac9276');d.line([(5,11),(3,7),(6,4),(11,5),(12,9),(8,11),(6,8),(9,7)],fill='#ee8c37',width=2)
  im.save(A/f'textures/item/{name}.png');write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
 for name in MOBS:write(A/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
 # Engineering baseline shared with existing souls, with no invented unique materials.
 for name in MOBS:
  pools=[]
  if name not in ('sparkling','zharenitsa'):
   pools=[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'slavicmyths:soul','conditions':[{'condition':'minecraft:random_chance_with_enchanted_bonus','enchantment':'minecraft:looting','unenchanted_chance':.02,'enchanted_chance':{'type':'minecraft:linear','base':.03,'per_level_above_first':.01}}]},{'type':'minecraft:item','name':'slavicmyths:soul_fragment','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]}]}]}]
  write(D/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
 sounds={'crawler_warn':'entity.ravager.attack','crawler_hit':'entity.ravager.step','spirit_warn':'entity.breeze.idle_air','spirit_pulse':'entity.breeze.wind_burst','pekelnik_warn':'entity.iron_golem.attack','pekelnik_throw':'entity.blaze.shoot','spark_idle':'block.fire.ambient','ring':'entity.blaze.shoot','spikes':'block.basalt.break','shift':'entity.breeze.jump','vortex':'entity.breeze.whirl','phase':'entity.blaze.ambient','death':'entity.blaze.death','hurt':'entity.blaze.hurt','mantle':'item.firecharge.use'}
 p=A/'sounds.json'; data=json.loads(p.read_text(encoding='utf-8'))
 for name,event in sounds.items():data['cinder.'+name]={'subtitle':'subtitle.slavicmyths.cinder.'+name,'sounds':[{'name':'minecraft:'+event,'type':'event'}]}
 write(p,data)
 for locale,index in [('ru_ru',0),('en_us',1)]:
  p=A/f'lang/{locale}.json';d=json.loads(p.read_text(encoding='utf-8'))
  for name,pair in NAMES.items():d['block.slavicmyths.'+name]=pair[index]
  for name,pair in MOBS.items():d['entity.slavicmyths.'+name]=pair[index];d['item.slavicmyths.'+name+'_spawn_egg']=('Яйцо призыва: ' if index==0 else 'Spawn Egg: ')+pair[index]
  for name,pair in materials.items():d['item.slavicmyths.'+name]=pair[index]
  d['cinder.boss.no_arena_reward']=['Награды Жареницы выдаются за победу на зарегистрированной природной арене.','Zharenitsa rewards require a registered natural arena victory.'][index]
  d['item.slavicmyths.infected_medium_soul']=('Заражённая душа средней силы','Medium Infected Soul')[index]
  d['biome.slavicmyths.black_cinder']=('Чёрная гарь','Black Cinder')[index]
  d['cinder.mantle.desc']=('ПКМ: на 8 с уменьшает огненный урон на 40%; один ответный порыв. Перезарядка 30 с.','Use: 8s of 40% fire damage reduction; one retaliation gust. 30s cooldown.')[index]
  d['cinder.mantle.armor']=('Устанавливается в рунный слот брони: срабатывает от огненного урона.','Install in an armor rune slot: activates on fire damage.')[index]
  d['death.attack.cinder_heat']=('%1$s погибает в пепельном жаре','%1$s perished in cinder heat')[index]
  d['death.attack.cinder_heat.player']=('%1$s сгорел в жаре от %2$s','%1$s burned in heat from %2$s')[index]
  for name in sounds:d['subtitle.slavicmyths.cinder.'+name]=('Чёрная гарь: '+{'ring':'кольцо жара','spikes':'поднимаются пики','shift':'дымовой сдвиг','vortex':'пепельный вихрь','phase':'раскрывается сердцевина','mantle':'пепельный покров'}.get(name,{'crawler_warn':'хищник готовится к рывку','crawler_hit':'тяжёлые лапы','spirit_warn':'дух собирает пепел','spirit_pulse':'пепельный всплеск','pekelnik_warn':'замах когтей','pekelnik_throw':'брошены угли','spark_idle':'потрескивание искр','death':'угасание','hurt':'треск породы'}.get(name,name))) if index==0 else 'Cinders: '+name.replace('_',' ')
  for key,ru,en in [('need_material','Нужна Жаростойкая печать','A Heatproof Seal is required'),('applied','Броня усилена печатью Жареницы','Armor enhanced with a Heatproof Seal')]:d['zharenitsa.seal.'+key]=ru if index==0 else en
  write(p,d)
 catalog=A/'creative_catalog.tsv';text=catalog.read_text(encoding='utf-8');known={l.split('\t')[0] for l in text.splitlines()};new=[]
 for name in names+list(materials)+[n+'_spawn_egg' for n in MOBS]:
  if name not in known:new.append(name+'\t'+('entities' if name.endswith('spawn_egg') else 'building' if name in names else 'magic')+'\tnether')
 # Use existing canonical category labels rather than introducing a new tab.
 existing=text.splitlines();blocktab=next(l.split('\t')[1] for l in existing if l.startswith('navya_soil\t'));mobtab=next(l.split('\t')[1] for l in existing if l.startswith('navka_spawn_egg\t'));runetab=next(l.split('\t')[1] for l in existing if l.startswith('rune_heat\t'))
 new=[l.replace('\tbuilding\t','\t'+blocktab+'\t').replace('\tentities\t','\t'+mobtab+'\t').replace('\tmagic\t','\t'+runetab+'\t') for l in new];catalog.write_text(text.rstrip()+'\n'+('\n'.join(new)+'\n' if new else ''),encoding='utf-8')
 # Natural biome and deterministic structure anchors; no frequency roll.
 biome=json.loads((D/'worldgen/biome/navya_grove.json').read_text())
 biome['effects']['fog_color']=0x4c2924;biome['effects']['sky_color']=0x361e1d
 biome['effects']['ambient_sound']='minecraft:ambient.nether_wastes.loop'
 biome['effects']['mood_sound']['sound']='minecraft:ambient.nether_wastes.mood'
 biome['effects']['particle']['probability']=.0008
 biome['features'][2]=['slavicmyths:black_cinder_terrain'];biome['features'][9]=['minecraft:spring_lava','slavicmyths:black_cinder_nature']
 biome['spawners']['monster']=[{'type':'slavicmyths:'+n,'weight':w,'minCount':1,'maxCount':2} for n,w in [('coal_crawler',22),('ash_spirit',15),('pekelnik',16)]]+[{'type':'minecraft:zombified_piglin','weight':30,'minCount':1,'maxCount':3},{'type':'minecraft:piglin','weight':10,'minCount':1,'maxCount':2},{'type':'minecraft:ghast','weight':2,'minCount':1,'maxCount':1}]
 biome['spawners']['ambient']=[{'type':'slavicmyths:sparkling','weight':12,'minCount':1,'maxCount':2}]
 write(D/'worldgen/biome/black_cinder.json',biome)
 for name in ('black_cinder_terrain','black_cinder_nature'):
  write(D/f'worldgen/configured_feature/{name}.json',{'type':'slavicmyths:'+name,'config':{}})
  write(D/f'worldgen/placed_feature/{name}.json',{'feature':'slavicmyths:'+name,'placement':[{'type':'minecraft:biome'}]})
 write(D/'worldgen/structure/zharenitsa_arena.json',{'type':'slavicmyths:zharenitsa_arena','biomes':['slavicmyths:black_cinder'],'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'none'})
 write(D/'worldgen/structure_set/zharenitsa_arena.json',{'structures':[{'structure':'slavicmyths:zharenitsa_arena','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':16,'separation':15,'salt':2032026,'frequency':1.0}})
 for feature in ('nether_fortress','bastion_remnant','ruined_portal_nether'):tag('minecraft','worldgen/biome','has_structure/'+feature,['slavicmyths:black_cinder'])
 write(D/'damage_type/cinder_heat.json',{'message_id':'cinder_heat','scaling':'when_caused_by_living_non_player','exhaustion':.1,'effects':'burning'})
 tag('minecraft','damage_type','is_fire',['slavicmyths:cinder_heat'])
 for kind in ('block','item'):
  tag('minecraft',kind,'wooden_stairs',['slavicmyths:blackwood_stairs']);tag('minecraft',kind,'wooden_slabs',['slavicmyths:blackwood_slab'])
 im=Image.new('RGBA',(64,64),'#332b2a');dr=ImageDraw.Draw(im)
 for x in (4,17,31,48,59):dr.line((x,0,x+3,20,x-2,44,x,63),fill='#d96b26',width=2)
 im.save(A/'textures/entity/cinder_effect.png')
 write(R/'docs/implementation/black-cinder-production/asset-manifest.json',{'blocks':names,'residents':list(MOBS)[:4],'boss':'zharenitsa','attack_entities':['cinder_coal','cinder_ash_spike'],'items':list(materials),'original_compare':'ART_ORIGINAL_COMPARE_NOT_RUN','source':'RECOVERED_VISUAL_SPEC_2.0.3.md'})
 print('Black Cinder: scoped assets,',len(names),'blocks, five distinct rigs; original compare NOT_RUN')
if __name__=='__main__':
 main()
 from halls_assets import world_and_heads
 world_and_heads()
