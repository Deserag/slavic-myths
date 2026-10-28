"""Scoped, reproducible original pixel assets/rigs; not an approved-PNG reproduction.
Only writes the 2.0.5 IDs and merges their explicit translations/catalog rows.
"""
from pathlib import Path
import json, random
from PIL import Image, ImageDraw
R=Path(__file__).resolve().parents[1]; A=R/'src/main/resources/assets/slavicmyths'; D=R/'src/main/resources/data/slavicmyths'
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def rig(name):
 parts=[]
 def box(n,parent,pivot,size,hot=False):
  parts.append(dict(name=n,parent=parent,pivot=pivot,size=size,offset=[-size[0]/2,-size[1]/2,-size[2]/2],hot=hot))
 if name=='heat_salamander':
  box('body','root',[0,16,0],[12,8,23]);box('head','body',[0,-1,-15],[12,7,10]);box('jaw','head',[0,3,-1],[10,3,9]);box('eye0','head',[-5,-2,-5.1],[2,2,1],True);box('eye1','head',[5,-2,-5.1],[2,2,1],True)
  box('tail','body',[0,0,17],[6,5,14]);box('tail_tip','tail',[0,0,10],[3,3,8])
  for i,(x,z) in enumerate([(-8,-7),(8,-7),(-8,7),(8,7)]):
   box(f'leg{i}','body',[x,3,z],[5,5,5]);box(f'foot{i}',f'leg{i}',[x/8,3,-1],[6,2,7])
  for i in range(5):box(f'ridge{i}','body',[0,-5,-8+i*4],[2,3,3],True)
 elif name=='slag_crab':
  box('body','root',[0,17,0],[20,9,16]);box('carapace','body',[0,-4,0],[16,4,13]);box('head','body',[0,0,-9],[10,5,5]);box('jaw','head',[0,2,-1],[8,2,3])
  for i,x in enumerate([-6,6]):box(f'eyestalk{i}','head',[x,-4,-1],[2,5,2]);box(f'eye{i}',f'eyestalk{i}',[0,-2,-1],[3,2,2],True)
  for i in range(6):
   side=-1 if i%2==0 else 1;box(f'leg{i}','body',[side*11,3,(i//2-1)*6],[4,3,3]);box(f'toe{i}',f'leg{i}',[side*1.5,2,1],[2,4,3])
  for i,side in enumerate([-1,1]):
   box(f'arm{i}','body',[side*11,-1,-8],[5,4,8]);box(f'claw{i}',f'arm{i}',[side*1,0,-7],[6,6,7]);box(f'pincer{i}',f'claw{i}',[side*2,0,-5],[2,4,5])
 elif name=='vent_golem':
  box('body','root',[0,-1,0],[22,22,15]);box('core','body',[0,0,-8],[8,10,2],True);box('head','body',[0,-18,-1],[13,13,12]);box('jaw','head',[0,5,-3],[11,4,9])
  for i,side in enumerate([-1,1]):
   box(f'leg{i}','body',[side*7,16,0],[10,17,12]);box(f'foot{i}',f'leg{i}',[0,8,-2],[12,3,15]);box(f'arm{i}','body',[side*15,1,0],[9,20,10]);box(f'fist{i}',f'arm{i}',[0,13,-2],[11,10,12]);box(f'eye{i}','head',[side*4,-1,-6.1],[3,2,1],True)
 elif name=='magma_lord':
  box('body','root',[0,-20,0],[17,28,12]);box('core','body',[0,-2,-7],[9,11,3],True)
  for i,side in enumerate([-1,1]):
   box(f'shell{i}','body',[side*6,-2,-8],[6,16,4]);box(f'arm{i}','body',[side*15,-7,0],[7,23,7]);box(f'claw{i}',f'arm{i}',[0,15,-2],[8,10,6]);
   for j in range(3):box(f'finger{i}_{j}',f'claw{i}',[(j-1)*3,7,-1],[2,8,2],j==1)
   box(f'backarm{i}','body',[side*16,-12,7],[6,22,6]);parts[-1]['rotation']=[.25,0,side*.65];box(f'backtalon{i}',f'backarm{i}',[side*4,14,-3],[5,10,4]);box(f'leg{i}','body',[side*6,27,0],[7,24,8]);box(f'shard{i}','body',[side*13,18,0],[5,9,6]);box(f'shard_back{i}','body',[side*9,-16,9],[5,11,5])
  box('head','body',[0,-23,-1],[15,13,12]);box('jaw','head',[0,6,-1],[13,4,11]);box('mouth','head',[0,4,-6.1],[10,3,1],True)
  for i,side in enumerate([-1,1]):
   box(f'eye{i}','head',[side*4,-1,-6.1],[5,2,1],True);box(f'horn{i}','head',[side*7,-12,0],[3,15,4]);box(f'horn_back{i}','head',[side*5,-9,5],[3,9,3]);box(f'shard_head{i}','head',[side*11,1,0],[3,5,4])
  box('crown','head',[0,-11,2],[4,10,4])
 else:raise ValueError(name)
 from nether_boss327_geometry import islands
 islands(name,parts)
 atlas=512;u=v=row=0;im=Image.new('RGBA',(atlas,atlas));glow=Image.new('RGBA',(atlas,atlas));rng=random.Random(name)
 for p in parts:
  x,y,z=p['size'];w=2*(x+z);h=y+z
  if u+w+2>atlas:u=0;v+=row+2;row=0
  assert v+h<atlas
  p['uv']=[u,v];row=max(row,h)
  for yy in range(v,v+h):
   for xx in range(u,u+w):
    noise=rng.randrange(-7,8);lx,ly=xx-u,yy-v
    hot=p['hot'] or ((lx+int(ly/5)+len(p['name']))%37==0 and ly%11<5)
    if name=='magma_lord':hot=p['hot'] or (lx%max(7,x+z)==(ly//5+len(p['name']))%max(7,x+z) and ly%13<7)
    rgb=(242+noise//2,104+noise*2,19) if hot else ((39+noise,35+noise,44+noise) if name=='magma_lord' else (47+noise,39+noise,49+noise))
    im.putpixel((xx,yy),(*rgb,255))
    if hot:glow.putpixel((xx,yy),(*rgb,255))
  u+=w+2
 write(A/f'burning_islands_rigs/{name}.json',dict(atlas=atlas,parts=parts))
 dest=A/'textures/entity/burning_islands';dest.mkdir(parents=True,exist_ok=True);im.save(dest/f'{name}.png');glow.save(dest/f'{name}_glow.png')
def icon(name):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);edge='#27212d';stone='#554354';hot='#f98322';yellow='#ffd36a'
 if name=='magma_clot':
  d.polygon([(3,3),(8,1),(13,5),(14,10),(10,14),(4,13),(1,8)],fill=edge);d.polygon([(5,4),(9,3),(11,6),(10,10),(6,12),(4,8)],fill=hot);d.line([(7,4),(6,7),(9,8),(8,11)],fill=yellow)
 elif name=='magma_core':
  d.polygon([(8,0),(14,4),(15,10),(8,15),(1,10),(2,4)],fill=edge);d.polygon([(8,2),(12,5),(12,10),(8,13),(4,10),(4,5)],fill=hot);d.polygon([(8,4),(10,7),(8,11),(6,7)],fill=yellow)
 elif name=='salamander_hide':
  d.polygon([(2,2),(6,3),(10,2),(14,1),(13,6),(14,11),(10,14),(6,12),(2,14),(3,9),(1,6)],fill=edge);d.polygon([(4,4),(11,3),(11,10),(6,11),(4,9)],fill=stone)
  for y in [5,8,10]:d.line([(5,y),(7,y-1),(10,y)],fill=hot)
 elif name=='crab_claw':
  d.polygon([(4,15),(2,11),(4,7),(3,3),(5,0),(7,6),(10,5),(12,1),(14,4),(13,9),(9,12),(7,15)],fill=edge);d.polygon([(5,7),(7,8),(10,7),(12,5),(11,9),(7,11),(4,11)],fill=hot);d.line([(5,12),(6,14)],fill=stone,width=2)
 elif name=='magma_rune_fragment':
  d.polygon([(4,2),(12,1),(14,8),(8,7),(9,14),(2,12)],fill=edge);d.line([(6,4),(10,3),(11,5),(6,6),(5,10),(7,11)],fill=hot,width=2)
 elif name.startswith('rune_magma_'):
  d.polygon([(4,0),(12,0),(15,4),(15,12),(12,15),(4,15),(1,12),(1,4)],fill=edge);d.rectangle((4,3,11,12),fill=stone);d.line([(5,11),(5,5),(8,8),(10,4),(11,11)],fill=hot,width=2)
  if name.endswith('2'):d.line([(3,1),(12,1),(14,4)],fill=yellow);d.point((7,12),fill=yellow)
 elif name=='magma_lord_heart':
  d.polygon([(8,3),(11,1),(14,3),(15,7),(8,15),(1,7),(2,3),(5,1)],fill=edge);d.polygon([(8,5),(11,3),(12,6),(8,12),(4,6),(5,3)],fill=hot);d.line([(8,5),(7,8),(9,9)],fill=yellow)
 elif name=='magma_lord_head':
  d.polygon([(1,0),(4,4),(5,1),(7,4),(11,0),(12,5),(15,2),(14,12),(11,15),(4,15),(1,11)],fill=edge);d.rectangle((3,6,12,11),fill=stone);d.line([(3,7),(6,8)],fill=hot,width=2);d.line([(9,8),(12,7)],fill=hot,width=2);d.polygon([(4,11),(7,12),(11,11),(10,14),(5,14)],fill=hot)
 else:raise ValueError(name)
 dest=A/'textures/item';dest.mkdir(exist_ok=True);im.save(dest/f'{name}.png')
 write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
def recipe(name,ingredients,out):write(D/f'recipe/{name}.json',dict(type='minecraft:crafting_shapeless',category='misc',ingredients=[{'item':f'slavicmyths:{i}'} for i in ingredients],result={'id':f'slavicmyths:{out}','count':1}))
def loot(name,extras):
 pools=[]
 for item,chance in [('magma_clot',1)]+extras:
  pool=dict(rolls=1,entries=[dict(type='minecraft:item',name=f'slavicmyths:{item}')])
  if chance<1:pool['conditions']=[dict(condition='minecraft:random_chance',chance=chance)]
  pools.append(pool)
 write(D/f'loot_table/entities/{name}.json',dict(type='minecraft:entity',pools=pools))
def main():
 from boss331_audio import main as audio
 audio()
 mobs={'heat_salamander':('Жаровая саламандра','Heat Salamander'),'slag_crab':('Шлаковый краб','Slag Crab'),'vent_golem':('Жерловик','Vent Golem'),'magma_lord':('Повелитель магмы','Magma Lord')}
 items={'magma_clot':('Сгусток магмы','Magma Clot'),'magma_core':('Магмовое ядро','Magma Core'),'salamander_hide':('Кожа саламандры','Salamander Hide'),'crab_claw':('Клешня краба','Crab Claw'),'magma_rune_fragment':('Фрагмент Руны магмы','Magma Rune Fragment'),'rune_magma_1':('Руна магмы I','Magma Rune I'),'rune_magma_2':('Руна магмы II','Magma Rune II'),'magma_lord_heart':('Сердце Повелителя магмы','Heart of the Magma Lord'),'magma_lord_head':('Голова Повелителя магмы','Magma Lord Head')}
 for name in mobs:rig(name);write(A/f'models/item/{name}_spawn_egg.json',dict(parent='minecraft:item/template_spawn_egg'))
 for name in items:icon(name)
 recipe('magma_core',['magma_clot']*4,'magma_core')
 recipe('rune_magma_1',['magma_rune_fragment','magma_core','salamander_hide','salamander_hide','crab_claw','blank_rune_1'],'rune_magma_1')
 recipe('rune_magma_2',['rune_magma_1']*2+['magma_core']*2,'rune_magma_2')
 for recipe_id,trigger in [('magma_core','magma_clot'),('rune_magma_1','magma_rune_fragment'),('rune_magma_2','rune_magma_1')]:
  write(D/f'advancement/recipes/burning_islands/{recipe_id}.json',dict(criteria={'has_material':dict(trigger='minecraft:inventory_changed',conditions={'items':[{'items':f'slavicmyths:{trigger}'}]})},requirements=[['has_material']],rewards={'recipes':[f'slavicmyths:{recipe_id}']}))
 loot('heat_salamander',[('salamander_hide',.5)]);loot('slag_crab',[('crab_claw',.1)]);loot('vent_golem',[('magma_rune_fragment',.225)])
 write(D/'loot_table/entities/magma_lord.json',dict(type='minecraft:entity',pools=[])) # inbox is sole boss reward producer
 write(A/'blockstates/magma_lord_head.json',dict(variants={'':{'model':'slavicmyths:block/magma_lord_head'}}))
 write(A/'models/block/magma_lord_head.json',dict(textures={'particle':'slavicmyths:item/magma_lord_head'},elements=[]))
 write(D/'loot_table/blocks/magma_lord_head.json',dict(type='minecraft:block',pools=[dict(rolls=1,conditions=[dict(condition='minecraft:survives_explosion')],entries=[dict(type='minecraft:item',name='slavicmyths:magma_lord_head')])]))
 # A real data-registry biome, intentionally absent from climate selection until strict ocean mask.
 biome=json.loads((D/'worldgen/biome/black_cinder.json').read_text());biome['features']=[[] for _ in range(11)];biome['spawners']={'creature':[dict(type='slavicmyths:'+n,weight=w,minCount=1,maxCount=m) for n,w,m in [('heat_salamander',12,2),('slag_crab',10,2),('vent_golem',3,1)]]};biome['spawn_costs']={};biome['effects']['fog_color']=3550759;biome['effects']['particle']['options']['type']='minecraft:white_ash';write(D/'worldgen/biome/burning_islands.json',biome)
 for lang,index in [('ru_ru',0),('en_us',1)]:
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
  for n,names in items.items():data[('block.' if n=='magma_lord_head' else 'item.')+'slavicmyths.'+n]=names[index]
  for n,names in mobs.items():data['entity.slavicmyths.'+n]=names[index];data['item.slavicmyths.'+n+'_spawn_egg']=(names[0]+' — яйцо призыва') if index==0 else names[1]+' Spawn Egg'
  data['biome.slavicmyths.burning_islands']=('Горючие острова','Burning Islands')[index]
  data['burningislands.heart.future']=('Трофей. Рецепты экипировки появятся в отдельном патче.','Trophy. Equipment recipes are reserved for a later patch.')[index]
  data['burningislands.rune.1']=('Нагрудник: ответный урон 2 HP и поджог 2 с после боевого урона. Перезарядка 2 с.','Chestplate: retaliate for 2 HP and ignite for 2 s after combat damage. Cooldown: 2 s.')[index]
  data['burningislands.rune.2']=('Нагрудник: ответный урон 4 HP и поджог 4 с. Общая перезарядка 2 с; с I не складывается.','Chestplate: retaliate for 4 HP and ignite for 4 s. Shared cooldown: 2 s; does not stack with I.')[index]
  data['burningislands.rune.terrain_blocked']=('Временная магма отключена: безопасное восстановление природных блоков ещё не доказано.','Temporary magma is disabled pending proven natural-block provenance and safe restoration.')[index]
  data['burningislands.rune.family_conflict']=('Не совмещается с другой Руной магмы.','Cannot share equipment with another Magma Rune.')[index]
  data['burningislands.rune.chest']=('Только нагрудник; одна руна семейства магмы.','Chestplate only; one magma-family rune.')[index]
  for tier in [1,2]:data[f'rpg.slavicmyths.rune_magma_{tier}']=data[f'burningislands.rune.{tier}']
  write(p,data)
 catalog=A/'creative_catalog.tsv';text=catalog.read_text(encoding='utf-8');existing={l.split('\t')[0] for l in text.splitlines() if l and not l.startswith('#')}
 for n in list(items)+[n+'_spawn_egg' for n in mobs]:
  if n not in existing:text+=f"{n}\t{'mobs' if n.endswith('_spawn_egg') else 'magic' if n.startswith('rune_') else 'decor' if n.endswith('_head') else 'resources'}\tburning_islands_205\n"
 catalog.write_text(text,encoding='utf-8')
 print('Wrote four independent rigs, eight entity textures, nine icons, three recipes, loot, data biome and translations. Natural selection not enabled; source PNG/audio acceptance blocked.')
if __name__=='__main__':main()
