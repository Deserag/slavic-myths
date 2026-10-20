"""Reproducible authored pixel assets and datapack resources for the commissioned industry patch."""
from pathlib import Path
import json, random
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parent.parent/'src/main/resources'
A=ROOT/'assets/slavicmyths';D=ROOT/'data/slavicmyths'
def js(path,value):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def png(path,im):
    from metals36_assets import write_owned
    if not write_owned(path):
        path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
def item(name,im):
    png(A/'textures/item'/f'{name}.png',im);js(A/'models/item'/f'{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
def canvas():return Image.new('RGBA',(32,32),(0,0,0,0))
def metal(name,copper=False):
    im=canvas();d=ImageDraw.Draw(im);colors=['#352f30','#6f777b','#c5cbcd','#edf0e7'] if not copper else ['#473326','#885039','#c78250','#e6ad73']
    d.polygon([(4,17),(10,8),(25,8),(29,15),(24,24),(8,24)],fill=colors[0]);d.polygon([(6,16),(11,10),(24,10),(26,15),(21,20),(10,20)],fill=colors[2]);d.polygon([(10,21),(23,21),(26,16),(25,22),(23,23),(10,23)],fill=colors[1]);d.line([(11,12),(21,12)],fill=colors[3],width=2);d.line([(9,15),(19,15)],fill=colors[3]);item(name,im)
metal('purified_iron_ingot');metal('purified_copper_ingot',True)
im=canvas();d=ImageDraw.Draw(im);d.polygon([(5,16),(8,9),(13,8),(17,11),(23,10),(28,16),(25,24),(20,23),(16,26),(7,23)],fill='#533629');d.polygon([(8,14),(12,10),(16,14),(23,13),(24,18),(20,22),(13,21),(9,23)],fill='#a45c30');d.rectangle((9,13,13,16),fill='#d49648');d.rectangle((20,15,23,18),fill='#bf7e3e');d.rectangle((14,21,17,23),fill='#6a432c');item('bog_iron_lump',im)
im=canvas();d=ImageDraw.Draw(im);d.polygon([(10,9),(20,9),(23,15),(27,23),(24,27),(8,27),(5,23),(9,14)],fill='#685e49');d.polygon([(11,11),(19,11),(23,17),(23,24),(8,24),(9,18)],fill='#c5bea3');d.rectangle((11,6,20,10),fill='#907045');d.rectangle((14,4,16,7),fill='#c5a96b');d.rectangle((12,16,15,19),fill='#f0edda');d.rectangle((18,20,21,23),fill='#e6e0bd');item('purification_flux',im)
im=canvas();d=ImageDraw.Draw(im);d.rectangle((1,12,30,20),fill='#473329');d.line((2,12,29,12),fill='#936749',width=2)
for x in (5,18):
 d.ellipse((x-2,7,x+10,24),fill='#442d20');d.ellipse((x,9,x+8,22),fill='#bb8050');d.ellipse((x+2,11,x+6,20),fill='#255567');d.rectangle((x+2,12,x+3,15),fill='#a5dfe7');d.rectangle((x+6,18,x+7,20),fill='#76aabd')
d.rectangle((14,13,18,15),fill='#ac7045');item('miners_goggles',im)
im=canvas();d=ImageDraw.Draw(im);d.ellipse((6,10,27,29),fill='#452f20');d.ellipse((8,11,25,27),fill='#bd8e48');d.ellipse((11,14,22,24),fill=(0,0,0,0));d.rectangle((8,11,11,15),fill='#e3b866');d.rectangle((22,11,25,15),fill='#e3b866');d.polygon([(13,4),(19,4),(23,9),(18,15),(12,13),(10,9)],fill='#243e52');d.polygon([(14,6),(18,6),(19,10),(14,11)],fill='#7eb4c1');item('prospectors_ring',im)
im=canvas();d=ImageDraw.Draw(im);d.line([(3,2),(8,6),(13,9),(16,10),(23,7),(29,2)],fill='#6b4b30',width=3);d.rectangle((14,9,18,13),fill='#906a43');d.ellipse((7,12,25,30),fill='#45494b');d.ellipse((9,14,23,28),fill='#b7bbb3');d.line([(12,24),(15,24),(15,21),(18,21),(18,17),(21,17)],fill='#575c60',width=2);item('stone_path_amulet',im)
im=canvas();d=ImageDraw.Draw(im);d.polygon([(1,10),(8,8),(27,9),(30,12),(29,22),(4,23),(1,20)],fill='#4c3429');d.rectangle((3,11,28,20),fill='#92623d');d.line((4,12,27,12),fill='#c79a62');d.rectangle((11,8,23,23),fill='#343a3d');d.rectangle((13,10,21,21),fill='#a5a79b');d.rectangle((15,12,19,19),fill='#523b2e');d.rectangle((5,15,9,26),fill='#624331');d.rectangle((6,15,9,17),fill='#b2a286');item('underground_guard_belt',im)
for name,tier in [('mining_rune_1',1),('mining_rune_2',2),('fortune_rune_1',0)]:
 im=canvas();d=ImageDraw.Draw(im)
 if tier:
  d.polygon([(7,3),(24,3),(28,8),(26,26),(22,29),(6,27),(4,8)],fill='#383c43');d.polygon([(9,6),(22,6),(24,10),(23,24),(8,24),(7,10)],fill='#737c85');d.line([(11,11),(20,11),(23,15)],fill='#94bfcd',width=2);d.line([(17,11),(12,21)],fill='#445c74',width=3)
  if tier==2:d.line([(7,4),(24,4),(27,9),(25,26),(7,26),(5,8),(7,4)],fill='#bcc4bd',width=2);d.rectangle((24,12,27,14),fill='#45617d');d.rectangle((24,18,27,20),fill='#45617d')
  else:d.rectangle((24,15,26,17),fill='#394b61')
 else:
  d.polygon([(16,2),(28,11),(26,24),(16,30),(4,23),(3,11)],fill='#665437');d.polygon([(16,5),(25,12),(23,22),(16,26),(7,21),(6,12)],fill='#c6b175');d.line([(16,10),(16,22)],fill='#f2dca0',width=2);d.line([(16,15),(10,12)],fill='#eed396',width=2);d.line([(16,15),(22,12)],fill='#eed396',width=2)
 item(name,im)

def stone(name,base,brick=False):
 im=Image.new('RGBA',(32,32),base);d=ImageDraw.Draw(im);r=random.Random(name)
 for i in range(130):
  x=r.randrange(32);y=r.randrange(32);rgb=tuple(max(0,min(255,v+r.randint(-16,16))) for v in base[:3]);d.rectangle((x,y,x+r.randrange(1,4),y+r.randrange(1,3)),fill=rgb+(255,))
 if brick:
  for y in range(0,32,8):d.line((0,y,31,y),fill='#393833');
  for row in range(4):
   for x in range((row%2)*8,32,16):d.line((x,row*8,x,row*8+7),fill='#393833')
 png(A/'textures/block'/f'{name}.png',im);return im
ore=stone('bog_iron_ore',(98,96,88,255));d=ImageDraw.Draw(ore);r=random.Random(15)
for x,y in [(5,7),(20,5),(15,15),(6,25),(25,23)]:
 d.line([(x-3,y+2),(x,y),(x+3,y+1),(x+4,y-2)],fill='#593c2c',width=4);d.line([(x-2,y+1),(x,y),(x+3,y)],fill='#a05e32',width=2);d.rectangle((x,y-1,x+1,y),fill='#d69a4b')
png(A/'textures/block/bog_iron_ore.png',ore)
stone('industry_stone',(102,99,91,255),True);stone('industry_metal',(97,99,96,255));wood=stone('industry_wood',(112,75,46,255));d=ImageDraw.Draw(wood)
for x in (3,11,19,27):d.line((x,0,x,31),fill='#65452c',width=2)
png(A/'textures/block/industry_wood.png',wood)
for lit in (False,True):
 im=Image.open(A/'textures/block/industry_stone.png').copy();d=ImageDraw.Draw(im);d.rectangle((7,15,25,28),fill='#393430');d.rectangle((9,17,23,27),fill='#211f1e')
 if lit:d.polygon([(10,26),(12,21),(14,24),(17,18),(20,23),(22,21),(22,27)],fill='#c9782a');d.rectangle((14,24,19,27),fill='#efbb53')
 png(A/'textures/block'/('industry_fire_lit.png' if lit else 'industry_fire.png'),im)
for name in ('bog_iron_ore','chud_bloomery','refining_hearth'):
 js(A/'models/item'/f'{name}.json',{'parent':f'slavicmyths:block/{name}'})
 if name=='bog_iron_ore':
  js(A/'models/block/bog_iron_ore.json',{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/bog_iron_ore'}});js(A/'blockstates/bog_iron_ore.json',{'variants':{'':{'model':'slavicmyths:block/bog_iron_ore'}}});continue
 for lit in (False,True):
  textures={'stone':'slavicmyths:block/industry_stone','metal':'slavicmyths:block/industry_metal','wood':'slavicmyths:block/industry_wood','front':'slavicmyths:block/industry_fire'+('_lit' if lit else ''),'particle':'slavicmyths:block/industry_stone'}
  def cube(a,b,texture,front=None):return {'from':a,'to':b,'faces':{face:{'texture':'#'+(front if face=='north' and front else texture)} for face in ('north','south','east','west','up','down')}}
  if name=='chud_bloomery':elements=[cube([1,0,1],[15,10,15],'stone','front'),cube([3,10,3],[13,16,13],'stone'),cube([0,1,3],[2,12,13],'metal'),cube([14,2,5],[16,9,12],'wood'),cube([3,15,2],[13,16,3],'metal'),cube([3,15,13],[13,16,14],'metal')]
  else:elements=[cube([1,0,1],[15,9,15],'stone'),cube([1,9,1],[15,12,15],'metal'),cube([5,12,4],[11,13,12],'front'),cube([4,12,3],[5,15,13],'stone'),cube([11,12,3],[12,15,13],'stone'),cube([5,12,3],[11,15,4],'stone'),cube([5,12,12],[11,15,13],'stone'),cube([14,4,6],[16,7,12],'wood')]
  js(A/'models/block'/f'{name}{"_lit" if lit else ""}.json',{'textures':textures,'elements':elements,'display':{'gui':{'rotation':[30,225,0],'scale':[.65,.65,.65]},'ground':{'translation':[0,3,0],'scale':[.25,.25,.25]}}})
 js(A/'blockstates'/f'{name}.json',{'variants':{f'facing={direction},lit={str(lit).lower()}':{'model':'slavicmyths:block/'+name+('_lit' if lit else ''),'y':angle,'uvlock':True} for direction,angle in [('north',0),('east',90),('south',180),('west',270)] for lit in (False,True)}})
for refine in (False,True):
 im=Image.new('RGBA',(256,256),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle((0,0,175,165),fill='#373737');d.rectangle((1,1,174,164),fill='#f5f5f5');d.rectangle((3,3,172,162),fill='#c6c6c6');d.line((3,162,172,162),fill='#555555',width=2)
 slots=[(35,17),(58,17),(46,53),(116,35)] if refine else [(56,17),(56,53),(116,35)]
 slots += [(8+x*18,84+y*18) for y in range(3) for x in range(9)]+[(8+x*18,142) for x in range(9)]
 for x,y in slots:d.rectangle((x-1,y-1,x+16,y+16),fill='#373737');d.rectangle((x,y,x+16,y+16),fill='#ffffff');d.rectangle((x,y,x+15,y+15),fill='#8b8b8b')
 d.polygon([(79,36),(96,36),(96,32),(103,40),(96,48),(96,44),(79,44)],fill='#8b8b8b');png(A/'textures/gui'/('refining_hearth.png' if refine else 'chud_bloomery.png'),im)
im=Image.new('RGBA',(32,32),'#925c37');d=ImageDraw.Draw(im);d.rectangle((16,0,31,7),fill='#467e91');d.rectangle((17,1,18,4),fill='#bcdee3');d.rectangle((0,8,31,31),fill='#4e3328');png(A/'textures/models/miners_goggles.png',im)
for role in range(4):
 im=Image.new('RGBA',(64,64),'#46525c');d=ImageDraw.Draw(im);d.rectangle((0,0,31,15),fill='#b5a083');d.rectangle((32,0,63,15),fill='#757f82' if role==3 else '#56664a');d.rectangle((16,16,39,31),fill=['#607966','#755a43','#5c656a','#65757f'][role]);d.rectangle((0,16,15,31),fill='#49453d');d.rectangle((40,16,63,31),fill='#8e7961');d.rectangle((32,32,49,49),fill='#846344');d.rectangle((24,6,31,13),fill='#427887');d.rectangle((8,8,8,9),fill='#343835');d.rectangle((11,8,11,9),fill='#343835');d.rectangle((16,25,39,27),fill='#3e332c');d.rectangle((25,25,29,28),fill='#9a9c8c');png(A/'textures/entity'/f'chud_{role}.png',im)

def shaped(name,pattern,key,out):js(D/'recipe'/f'{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':out,'count':1}})
shaped('chud_bloomery',['BCB','IFI','BCB'],{'B':'minecraft:stone_bricks','C':'minecraft:copper_ingot','I':'minecraft:iron_ingot','F':'minecraft:blast_furnace'},'slavicmyths:chud_bloomery')
shaped('refining_hearth',['BIB','CSC','BIB'],{'B':'minecraft:stone_bricks','I':'minecraft:iron_ingot','C':'minecraft:copper_ingot','S':'minecraft:smithing_table'},'slavicmyths:refining_hearth')
shaped('miners_goggles',['CGC','AIA','LNL'],{'C':'slavicmyths:purified_copper_ingot','G':'minecraft:glass_pane','A':'minecraft:amethyst_shard','I':'slavicmyths:purified_iron_ingot','L':'minecraft:leather','N':'minecraft:netherite_scrap'},'slavicmyths:miners_goggles')
shaped('prospectors_ring',[' C ','CAC',' C '],{'C':'slavicmyths:purified_copper_ingot','A':'minecraft:amethyst_shard'},'slavicmyths:prospectors_ring')
shaped('stone_path_amulet',[' L ',' I ',' A '],{'L':'minecraft:leather','I':'slavicmyths:purified_iron_ingot','A':'minecraft:amethyst_shard'},'slavicmyths:stone_path_amulet')
shaped('underground_guard_belt',['LCL','CIC',' L '],{'C':'slavicmyths:purified_copper_ingot','I':'slavicmyths:purified_iron_ingot','L':'minecraft:leather'},'slavicmyths:underground_guard_belt')
js(D/'recipe/bog_iron_bloomery.json',{'type':'slavicmyths:chud_bloomery','ingredient':{'item':'slavicmyths:bog_iron_lump'},'result':{'id':'minecraft:iron_ingot','count':1},'duration':200,'experience':.7})
for metal in ('iron','copper'):js(D/'recipe'/f'purify_{metal}.json',{'type':'slavicmyths:refining_hearth','ingredient':{'item':f'minecraft:{metal}_ingot'},'flux':{'item':'slavicmyths:purification_flux'},'result':{'id':f'slavicmyths:purified_{metal}_ingot','count':1},'duration':300})
for slot,entry in [('head','miners_goggles'),('ring','prospectors_ring'),('necklace','stone_path_amulet'),('belt','underground_guard_belt')]:
 p=ROOT/'data/curios/tags/item'/f'{slot}.json';data=json.loads(p.read_text(encoding='utf-8'));value='slavicmyths:'+entry
 if value not in data['values']:data['values'].append(value)
 js(p,data)
def tag(path,values):
 p=ROOT/'data'/path;data=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'replace':False,'values':[]}
 for v in values:
  if v not in data['values']:data['values'].append(v)
 js(p,data)
tag(Path('minecraft/tags/block/mineable/pickaxe.json'),['slavicmyths:'+x for x in ('bog_iron_ore','chud_bloomery','refining_hearth')]);tag(Path('minecraft/tags/block/needs_stone_tool.json'),['slavicmyths:bog_iron_ore']);tag(Path('minecraft/tags/block/needs_iron_tool.json'),['slavicmyths:chud_bloomery','slavicmyths:refining_hearth']);tag(Path('c/tags/block/ores.json'),['slavicmyths:bog_iron_ore','slavicmyths:silver_ore','slavicmyths:perunite_ore']);tag(Path('c/tags/block/ores/iron.json'),['slavicmyths:bog_iron_ore'])
js(D/'tags/worldgen/biome/has_bog_iron.json',{'replace':False,'values':['minecraft:swamp','minecraft:mangrove_swamp']})
js(D/'worldgen/configured_feature/bog_iron.json',{'type':'slavicmyths:bog_iron','config':{}});js(D/'worldgen/placed_feature/bog_iron.json',{'feature':'slavicmyths:bog_iron','placement':[{'type':'minecraft:biome'}]});js(D/'neoforge/biome_modifier/bog_iron.json',{'type':'neoforge:add_features','biomes':'#slavicmyths:has_bog_iron','features':'slavicmyths:bog_iron','step':'underground_ores'})
for name in ('chud_bloomery','refining_hearth'):js(D/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
js(D/'loot_table/blocks/bog_iron_ore.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'slavicmyths:bog_iron_ore','conditions':[{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]},{'type':'minecraft:item','name':'slavicmyths:bog_iron_lump','functions':[{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:ore_drops'},{'function':'minecraft:explosion_decay'}]}]}]}]})

def entry(name,lo=1,hi=1,weight=1,damage=False):
 out={'type':'minecraft:item','name':name if ':' in name else 'minecraft:'+name,'weight':weight};func=[]
 if lo!=1 or hi!=1:func.append({'function':'minecraft:set_count','count':lo if lo==hi else {'type':'minecraft:uniform','min':lo,'max':hi}})
 if damage:func.append({'function':'minecraft:set_damage','damage':{'type':'minecraft:uniform','min':.25,'max':.65}})
 if func:out['functions']=func
 return out
def base(lo,hi,entries):return {'rolls':{'type':'minecraft:uniform','min':lo,'max':hi},'entries':[entry(*e) for e in entries]}
def chance(name,p,lo=1,hi=1):return {'rolls':1,'entries':[entry(name,lo,hi)],'conditions':[{'condition':'minecraft:random_chance','chance':p}]}
tables={
'chud_residential':[base(2,4,[('bread',1,4,25),('slavicmyths:karavai',1,2,15),('stick',1,6,10),('leather',1,2,10),('string',1,4,10),('coal',1,3,10),('copper_ingot',1,3,15),('slavicmyths:ancient_coin',1,3,5)]),chance('iron_ingot',.20,1,2),chance('gold_ingot',.05)],
'chud_workshop':[base(3,5,[('iron_ingot',1,4,25),('copper_ingot',1,6,25),('coal',2,6,20),('charcoal',2,6,10),('slavicmyths:silver_ingot',1,1,10),('iron_pickaxe',1,1,10,True)]),{'rolls':1,'entries':[entry('coal',1,2)]},chance('slavicmyths:purification_flux',.30,1,2),chance('slavicmyths:purified_iron_ingot',.12),chance('slavicmyths:purified_copper_ingot',.12),chance('diamond',.03)],
'chud_storehouse':[base(3,5,[('coal',2,8,25),('iron_ingot',1,5,20),('copper_ingot',1,6,20),('spruce_planks',2,6,15),('leather',1,3,10),('gravel',2,6,10)]),chance('slavicmyths:silver_ingot',.15,1,2),chance('slavicmyths:purification_flux',.10),chance('gold_ingot',.10,1,2)],
'abandoned_living':[base(2,4,[('rotten_flesh',1,3,20),('string',1,4,20),('leather',1,2,15),('iron_nugget',1,6,20),('iron_ingot',1,2,10),('slavicmyths:ancient_coin',1,3,10),('gold_ingot',1,1,5)]),chance('diamond',.04),chance('slavicmyths:purification_flux',.08),chance('slavicmyths:bog_iron_lump',.20,1,3)],
'abandoned_mine':[base(3,5,[('iron_ingot',1,5,15),('copper_ingot',1,7,15),('raw_iron',1,4,10),('slavicmyths:bog_iron_lump',1,4,15),('coal',2,8,20),('gold_ingot',1,3,5),('torch',2,8,10),('iron_pickaxe',1,1,10,True)]),chance('diamond',.12,1,3),chance('slavicmyths:purification_flux',.14,1,2),chance('slavicmyths:purified_copper_ingot',.07),chance('slavicmyths:purified_iron_ingot',.07),chance('netherite_scrap',.015),chance('slavicmyths:mining_rune_1',.03),chance('slavicmyths:rune_strength',.03)],
'abandoned_treasury':[base(4,6,[('gold_ingot',2,6,25),('iron_ingot',2,5,15),('slavicmyths:silver_ingot',1,3,15),('slavicmyths:ancient_coin',2,8,15),('slavicmyths:purified_iron_ingot',1,3,10),('slavicmyths:purified_copper_ingot',1,3,10),('diamond',1,3,10)]),{'rolls':1,'entries':[entry('diamond',2,2)]},chance('diamond',.35,2,4),chance('netherite_scrap',.06,1,2),chance('netherite_ingot',.005),chance('slavicmyths:mining_rune_1',.07),chance('slavicmyths:mining_rune_2',.02),chance('slavicmyths:fortune_rune_1',.01),chance('slavicmyths:rune_strength',.08),chance('slavicmyths:rune_crushing',.03),chance('slavicmyths:miners_goggles',.002)]}
for name,pools in tables.items():js(D/'loot_table/chests'/f'{name}.json',{'type':'minecraft:chest','pools':pools})

names={'bog_iron_ore':('Болотная железная руда','Bog Iron Ore'),'bog_iron_lump':('Железистый болотный комок','Bog Iron Lump'),'chud_bloomery':('Домница Чуди','Chud Bloomery'),'refining_hearth':('Очистной горн','Refining Hearth'),'purification_flux':('Очищающий плавень','Purification Flux'),'purified_iron_ingot':('Очищенный железный слиток','Purified Iron Ingot'),'purified_copper_ingot':('Очищенный медный слиток','Purified Copper Ingot'),'miners_goggles':('Очки копателя',"Miner's Goggles"),'prospectors_ring':('Кольцо старателя',"Prospector's Ring"),'stone_path_amulet':('Оберег каменной тропы','Stone Path Amulet'),'underground_guard_belt':('Пояс подземного стража','Underground Guard Belt'),'mining_rune_1':('Руна рудокопа I',"Miner's Rune I"),'mining_rune_2':('Руна рудокопа II',"Miner's Rune II"),'fortune_rune_1':('Руна удачливого старателя I',"Fortunate Prospector's Rune I")}
hints={'bog_iron_lump':('Плавится только в Домнице Чуди: 1 комок → 1 железный слиток','Bloomery only: 1 lump → 1 iron ingot'),'purification_flux':('Продаётся у ремесленника и плавильщика Чуди. 1 плавень на очистку слитка','Sold by Chud artisans and smelters. 1 flux per refined ingot'),'purified_iron_ingot':('Создаётся в очистном горне; материал для аксессуаров','Made in a refining hearth; used for accessories'),'purified_copper_ingot':('Создаётся в очистном горне; материал для аксессуаров','Made in a refining hearth; used for accessories'),'miners_goggles':('Головной аксессуар: постоянное ночное зрение. Редкий товар жителей Чуди','Head accessory: continuous Night Vision. Rare Chud resident trade'),'prospectors_ring':('Слот кольца: +12% к скорости добычи руды правильной киркой','Ring slot: +12% ore mining speed with a suitable pickaxe'),'stone_path_amulet':('Слот ожерелья: −25% финального урона от обычного падения','Necklace slot: −25% final damage from ordinary falls'),'underground_guard_belt':('Слот пояса: +1 броня, +10 процентных пунктов сопротивления отбрасыванию','Belt slot: +1 armor, +10 percentage points knockback resistance'),'mining_rune_1':('+10% к скорости добычи руды. Установка в Рунной наковальне','+10% ore mining speed. Install at the Runic Anvil'),'mining_rune_2':('+20% к скорости добычи руды; применяется максимальный уровень','+20% ore mining speed; only the highest installed tier applies'),'fortune_rune_1':('+1 к эффективному уровню Удачи, максимум IV; не действует с Шёлковым касанием','+1 effective Fortune level, capped at IV; disabled by Silk Touch')}
extra={'pickaxe_only':('Только кирки; используются существующие рунические гнёзда','Pickaxes only; uses existing rune sockets'),'miner_sells_runes':('Руны добычи продаёт плавильщик Чуди','Mining runes are sold by Chud smelters'),'processing':('Топливо печи • %s секунд','Furnace fuel • %s seconds'),'role.0':('Житель Чуди','Chud Resident'),'role.1':('Ремесленник Чуди','Chud Artisan'),'role.2':('Плавильщик Чуди','Chud Smelter'),'role.3':('Страж Чуди','Chud Guard')}
for i,lang in enumerate(('ru_ru','en_us')):
 p=A/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
 for name,text in names.items():data[('block' if name in ('bog_iron_ore','chud_bloomery','refining_hearth') else 'item')+'.slavicmyths.'+name]=text[i]
 for name,text in hints.items():data['industry.slavicmyths.hint.'+name]=text[i]
 for key,text in extra.items():data['industry.slavicmyths.'+key]=text[i]
 for name in ('chud_resident','chud_artisan','chud_guard'):
  role={'chud_resident':'0','chud_artisan':'1','chud_guard':'3'}[name];data['entity.slavicmyths.'+name]=extra['role.'+role][i];data['item.slavicmyths.'+name+'_spawn_egg']=('Яйцо призыва: ' if i==0 else 'Spawn Egg: ')+extra['role.'+role][i]
 data['industry.slavicmyths.silver_existing']=('Серебро: +1,5 прямого урона против существ с явной серебряной уязвимостью' if i==0 else 'Silver: +1.5 direct damage against explicitly silver-vulnerable creatures')
 data['book.slavicmyths.industry.title']=('Домница и очищение металлов' if i==0 else 'Bloomery and Metal Refining')
 data['book.slavicmyths.industry.text']=('Болотная руда встречается под грунтом болот и добывается каменной киркой. Железистый комок плавится в Домнице Чуди: один комок даёт один железный слиток за 10 секунд. Очистной горн расходует слиток железа или меди, один очищающий плавень и топливо: за 15 секунд получится очищенный слиток. Плавень продают ремесленники и плавильщики Чуди. Очищенные металлы нужны для очков, кольца, оберега и пояса. Очки занимают головной слот Curios и дают ночное зрение. Руны добычи продаёт плавильщик; устанавливайте их в гнёзда кирки через существующую Рунную наковальню. Руна рудокопа ускоряет добычу руды, руна старателя добавляет эффективный уровень Удачи до IV без изменения зачарований; Шёлковое касание сохраняет приоритет. Редкие жители предлагают готовые аксессуары, в новых владениях Чуди есть тематические сундуки. Это игровая интерпретация ремесла, не описание исторического технологического процесса.' if i==0 else 'Bog iron occurs beneath swamp ground and requires a stone pickaxe. The Chud Bloomery turns one lump into one iron ingot in 10 seconds. The Refining Hearth consumes an iron or copper ingot, one flux and fuel to produce a purified ingot in 15 seconds. Chud artisans and smelters sell flux. Purified metals make goggles, a ring, an amulet and a belt. Goggles occupy the Curios head slot and grant Night Vision. Smelters sell mining runes; install them in pickaxe sockets at the existing Runic Anvil. Mining runes improve ore mining speed; the fortune rune adds one effective Fortune level up to IV without rewriting enchantments, with Silk Touch taking priority. Rare residents sell finished accessories, and new Chud structures contain themed chests. This is a game interpretation, not a historical description of metallurgy.')
 data['rpg.slavicmyths.category_pickaxe']=('Кирки' if i==0 else 'Pickaxes');js(p,data)
print('Industry assets, recipes, 6 loot tables and RU/EN written')

if __name__ == "__main__":
 from feedback_resource_policy import apply as apply_feedback_policy
 apply_feedback_policy()
