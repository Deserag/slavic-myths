"""Resource-only #324. Source-authored pixels, ART_BLOCKED_REFERENCE, no fake original."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths'
ORES=['livingstone_ore','alatyrite_ore','runnik_ore','infernal_ore','sulfur_deposit']
RAW=['raw_livingstone','raw_alatyrite','raw_runnik','raw_infernal_shard','sulfur']
ROAST=['roasted_livingstone','roasted_alatyrite','roasted_runnik','roasted_infernal_shard']
RU=['Живокамень','Алатырит','Рунник','Пекельная руда','Серный нарост']
EN=['Livingstone Ore','Alatyrite Ore','Runnik Ore','Infernal Ore','Sulfur Deposit']
BASE=[(74,30,29),(34,27,25),(36,37,43),(27,25,29),(80,40,34)]
PIGMENT=[[(250,151,43),(255,205,71)],[(240,229,178),(255,246,206)],[(151,70,224),(70,102,220),(51,185,223),(210,62,175),(219,153,232)],[(229,60,24),(255,121,29)],[(215,178,35),(246,214,80)]]
def write(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def merge_tag(ns,path,values):
 p=R/'src/main/resources/data'/ns/'tags'/Path(path+'.json');d=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'replace':False,'values':[]}
 for v in values:
  if v not in d['values']:d['values'].append(v)
 write(p,d)
def texture(i):
 rng=random.Random(324+i);im=Image.new('RGBA',(16,16));px=im.load()
 for y in range(16):
  for x in range(16):
   noise=rng.randrange(-11,12);px[x,y]=tuple(max(0,c+noise) for c in BASE[i])+(255,)
 dr=ImageDraw.Draw(im)
 if i==2:
  # Mineral inclusions, intentionally NO veins.
  for k in range(22):
   x=rng.randrange(1,15);y=rng.randrange(1,15);dr.point((x,y),fill=PIGMENT[i][k%5]+(255,))
 elif i==4:
  for k in range(9):
   x=rng.randrange(1,14);y=rng.randrange(2,14);dr.rectangle((x,y,x+2,y+1),fill=PIGMENT[i][k%2]+(255,));dr.point((x+1,y-1),fill=(255,234,131,255))
 else:
  for k in range(3):
   x=rng.randrange(1,12);y=rng.randrange(1,6);pts=[(x,y),(x+1,y+2),(x-1,y+4),(x+2,y+7)];dr.line(pts,fill=PIGMENT[i][0]+(255,),width=2 if i==0 else 1);dr.line(pts[1:3],fill=PIGMENT[i][1]+(255,))
 path=A/'textures/block'/Path(ORES[i]+'.png');path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
def icon(name,i,roasted=False):
 im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im)
 if name=='alatyr_ingot':
  dr.polygon([(2,7),(5,4),(12,4),(14,8),(11,12),(2,11)],fill=(139,104,55));dr.polygon([(3,7),(6,5),(11,5),(13,8),(10,10),(3,9)],fill=(244,230,175));dr.line([(6,6),(8,8),(9,6)],fill=(148,114,65));dr.line([(4,10),(10,11)],fill=(215,169,82))
 else:
  shape=[(3,5),(8,2),(12,5),(14,9),(9,13),(4,12),(2,8)] if not roasted else [(3,8),(6,3),(11,4),(13,10),(8,13),(4,11)]
  dr.polygon(shape,fill=tuple(max(0,c-50) for c in PIGMENT[i][0]));dr.polygon([(4,6),(8,4),(11,6),(10,10),(6,11)],fill=PIGMENT[i][-1])
  if i==2:
   for k,c in enumerate(PIGMENT[i]):dr.point((5+k,6+k%3),fill=c)
  if roasted:dr.line([(5,5),(8,4),(10,5)],fill=(255,244,198))
 p=A/'textures/item'/Path(name+'.png');p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def main():
 names=ORES+RAW+ROAST+['alatyr_ingot'];lang={code:json.loads((A/'lang'/Path(code+'.json')).read_text(encoding='utf-8')) for code in ['ru_ru','en_us']}
 for i,ore in enumerate(ORES):
  texture(i);icon(RAW[i],i)
  write(A/'blockstates'/Path(ore+'.json'),{'variants':{'':{'model':'slavicmyths:block/'+ore}}});write(A/'models/block'/Path(ore+'.json'),{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/'+ore}});write(A/'models/item'/Path(ore+'.json'),{'parent':'slavicmyths:block/'+ore})
  silk={'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}
  fun=[]
  if i in [0,4]:fun.append({'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2 if i==0 else 3}})
  fun += [{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:ore_drops'},{'function':'minecraft:explosion_decay'}]
  write(D/'loot_table/blocks'/Path(ore+'.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'slavicmyths:'+ore,'conditions':[silk,{'condition':'minecraft:survives_explosion'}]},{'type':'minecraft:item','name':'slavicmyths:'+RAW[i],'functions':fun}]}]}]})
  for code,ls in lang.items():ls['block.slavicmyths.'+ore]=(RU if code=='ru_ru' else EN)[i]
 for i,out in enumerate(ROAST):
  icon(out,i,True)
  for typ,ticks in [('smelting',200),('blasting',100)]:
   recipe=out+'_'+typ;write(D/'recipe'/Path(recipe+'.json'),{'type':'minecraft:'+typ,'category':'misc','ingredient':{'item':'slavicmyths:'+RAW[i]},'result':{'id':'slavicmyths:'+out,'count':1},'experience':.1,'cookingtime':ticks})
   write(D/'advancement/recipes/nether_minerals'/Path(recipe+'.json'),{'parent':'minecraft:recipes/root','criteria':{'has_raw':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'slavicmyths:'+RAW[i]}]}},'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'slavicmyths:'+recipe}}},'requirements':[['has_raw','has_recipe']],'rewards':{'recipes':['slavicmyths:'+recipe]}})
 icon('alatyr_ingot',1)
 write(D/'recipe/alatyr_ingot.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['QAQ','MGB','SAI'],'key':{k:{'item':v} for k,v in {'Q':'minecraft:quartz','A':'slavicmyths:roasted_alatyrite','M':'minecraft:magma_cream','G':'minecraft:gold_ingot','B':'minecraft:blaze_powder','S':'slavicmyths:sulfur','I':'slavicmyths:roasted_infernal_shard'}.items()},'result':{'id':'slavicmyths:alatyr_ingot','count':1}})
 write(D/'advancement/recipes/nether_minerals/alatyr_ingot.json',{'parent':'minecraft:recipes/root','criteria':{'has_alatyrite':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'slavicmyths:roasted_alatyrite'}]}}},'requirements':[['has_alatyrite']],'rewards':{'recipes':['slavicmyths:alatyr_ingot']}})
 for name in RAW+ROAST+['alatyr_ingot']:
  write(A/'models/item'/Path(name+'.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
  for code,ls in lang.items():
   if name=='alatyr_ingot':value='Алатырский слиток' if code=='ru_ru' else 'Alatyr Ingot'
   elif name=='sulfur':value='Сера' if code=='ru_ru' else 'Sulfur'
   else:
    i=(RAW if name in RAW else ROAST).index(name);value=('Сырой ' if name in RAW else 'Обожжённый ')+['живокамень','алатырит','рунник','пекельный осколок'][i] if code=='ru_ru' else ('Raw ' if name in RAW else 'Roasted ')+['Livingstone','Alatyrite','Runnik','Infernal Shard'][i]
   ls['item.slavicmyths.'+name]=value
 for code,ls in lang.items():
  ru=code=='ru_ru';ls.update({'netherores.raw':'Обжарка в печи или плавильной печи: 1:1.' if ru else 'Roast in a furnace or blast furnace: 1:1.','netherores.roasted':'Обожжённый минерал для кузнечных материалов.' if ru else 'Roasted mineral for smithing materials.','netherores.sulfur':'Реагент для Алатырского слитка.' if ru else 'Reagent for an Alatyr Ingot.','netherores.ingot':'Материал адских кузнечных улучшений.' if ru else 'Material for Nether smithing upgrades.'});write(A/'lang'/Path(code+'.json'),ls)
 merge_tag('minecraft','block/mineable/pickaxe',['slavicmyths:'+n for n in ORES]);merge_tag('minecraft','block/needs_stone_tool',['slavicmyths:livingstone_ore']);merge_tag('minecraft','block/needs_iron_tool',['slavicmyths:infernal_ore']);merge_tag('minecraft','block/needs_diamond_tool',['slavicmyths:alatyrite_ore','slavicmyths:runnik_ore'])
 # Explicit geology only. Halls masonry, portals, containers and ancient debris excluded.
 merge_tag('slavicmyths','block/nether_ore_hosts',['minecraft:netherrack','minecraft:blackstone','minecraft:basalt','slavicmyths:charred_stone','slavicmyths:cracked_charred_stone'])
 merge_tag('slavicmyths','worldgen/biome/nether_mineral_biomes',['minecraft:'+b for b in ['nether_wastes','soul_sand_valley','crimson_forest','warped_forest','basalt_deltas']]+['slavicmyths:'+b for b in ['navya_grove','black_cinder','lost_halls','burning_islands']])
 write(D/'worldgen/configured_feature/nether_minerals.json',{'type':'slavicmyths:nether_minerals','config':{}});write(D/'worldgen/placed_feature/nether_minerals.json',{'feature':'slavicmyths:nether_minerals','placement':[{'type':'minecraft:biome'}]});write(D/'neoforge/biome_modifier/nether_minerals.json',{'type':'neoforge:add_features','biomes':'#slavicmyths:nether_mineral_biomes','features':['slavicmyths:nether_minerals'],'step':'top_layer_modification'})
 p=A/'creative_catalog.tsv';s=p.read_text(encoding='utf-8');ids={l.split('\t')[0] for l in s.splitlines()};s+=''.join(n+'\tresources\tnether_minerals\n' for n in names if n not in ids);p.write_text(s,encoding='utf-8')
 print('Generated #324 resources. Original-reference match BLOCKED, runtime NOT_RUN.')
if __name__=='__main__':main()
