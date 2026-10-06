from pathlib import Path
import json,gzip,importlib.util
R=Path('.');spec=importlib.util.spec_from_file_location('old_swamp',R/'tools/swamp_073.py');old=importlib.util.module_from_spec(spec);spec.loader.exec_module(old)
B=old.Blueprint
out=R/'src/main/resources/data/slavicmyths/structure/swamp';out.mkdir(parents=True,exist_ok=True)
def deck(b,x,z,w,d):
 b.box((x,1,z),(x+w-1,1,z+d-1),'dark_oak_planks')
 for a in [x,x+w-1]:
  for c in [z,z+d-1]:b.put(a,0,c,'spruce_log',axis='y')
def lamp(b,x,y,z,soul=False):
 b.put(x,y+1,z,'dark_oak_fence',east=False,west=False,north=False,south=False,waterlogged=False)
 b.put(x,y,z,'soul_lantern' if soul else 'lantern',hanging=True,waterlogged=False)
def hut(b,x,z,broken=False):
 deck(b,x,z,7,7);b.box((x+1,2,z+1),(x+5,5,z+5),'air')
 for y in range(2,6):
  for a in range(x,x+7):
   for c in [z,z+6]:b.put(a,y,c,'dark_oak_planks')
  for c in range(z,z+7):
   for a in [x,x+6]:b.put(a,y,c,'dark_oak_planks')
 for a in [x,x+6]:
  for c in [z,z+6]:b.box((a,0,c),(a,5,c),'spruce_log',axis='y')
 b.box((x+3,2,z+6),(x+4,3,z+6),'air')
 for a,c in [(x,z+3),(x+6,z+3),(x+3,z)]:b.put(a,3,c,'air');b.put(a,2,c,'dark_oak_trapdoor',facing='north',open=False,half='bottom',powered=False,waterlogged=False)
 old.roof(b,x,z,7,7,6,(x+4,x+6,z,z+2) if broken else None)
 b.put(x+1,2,z+1,'crafting_table');b.put(x+1,2,z+5,'smoker',facing='east',lit=False)
 b.put(x+5,2,z+1,'barrel',facing='up',open=False);old.stair(b,x+2,2,z+1,'south','dark_oak')
 b.put(x+5,2,z+5,'red_bed',facing='north',part='foot',occupied=False);b.put(x+5,2,z+4,'red_bed',facing='north',part='head',occupied=False)
 lamp(b,x+3,4,z+5)
 if broken:
  b.box((x,2,z+2),(x,4,z+3),'air');b.put(x+1,3,z+1,'cobweb');b.put(x+2,2,z+4,'moss_carpet');b.put(x+1,2,z+2,'brown_mushroom')
def tower(b,x,z,height):
 deck(b,x,z,5,5)
 for a in [x,x+4]:
  for c in [z,z+4]:b.box((a,0,c),(a,height+3,c),'spruce_log',axis='y')
 deck(b,x,z,5,5) # Ground level remains open.
 b.box((x,height,z),(x+4,height,z+4),'dark_oak_planks')
 for y in range(2,height+1):
  b.put(x,y,z+1,'dark_oak_planks');b.put(x+1,y,z+1,'ladder',facing='east',waterlogged=False)
 b.put(x+1,height,z+1,'ladder',facing='east',waterlogged=False)
 for a in range(x,x+5):
  for c in [z,z+4]:b.put(a,height+1,c,'dark_oak_fence',east=True,west=True,north=False,south=False,waterlogged=False)
 for c in range(z+1,z+4):
  for a in [x,x+4]:b.put(a,height+1,c,'dark_oak_fence',east=False,west=False,north=True,south=True,waterlogged=False)
 b.box((x-1,height+4,z-1),(x+5,height+4,z+5),'dark_oak_slab',type='bottom',waterlogged=False);lamp(b,x+2,height+3,z+2)
 b.loot(x+3,height+1,z+3,'watchtower')
def shrine(b,x,z):
 deck(b,x,z,11,11)
 for a in range(x+1,x+10):
  for c in range(z+1,z+10):b.put(a,1,c,'mossy_stone_bricks' if (a+c)%4 else 'cracked_stone_bricks')
 for a,c in [(x+1,z+1),(x+9,z+1),(x+1,z+9),(x+9,z+9)]:
  b.box((a,0,c),(a,4,c),'spruce_log',axis='y');b.put(a,5,c,'chiseled_stone_bricks');lamp(b,a,3,c+1,True)
 b.box((x+4,2,z+3),(x+6,2,z+5),'mossy_stone_bricks');b.put(x+5,3,z+4,'slavicmyths:altar')
 b.put(x+4,3,z+4,'candle',candles='3',lit=True,waterlogged=False);b.put(x+6,3,z+4,'flower_pot');b.loot(x+2,2,z+3,'ritual');b.loot(x+8,2,z+3,'ritual')
 b.encounter(x+5,2,z+8,'bolotnik')
def save(b):
 palette=[];lookup={};blocks=[]
 for pos,(state,nbt) in sorted(b.blocks.items()):
  k=json.dumps(state,sort_keys=True)
  if k not in lookup:lookup[k]=len(palette);palette.append(state)
  row={'pos':list(pos),'state':lookup[k]}
  if nbt:row['nbt']=nbt
  blocks.append(row)
 doc={'DataVersion':3955,'size':list(b.size),'palette':palette,'blocks':blocks,'entities':[]}
 (out/(b.name+'.nbt')).write_bytes(gzip.compress(b'\x0a\0\0'+old.tag(doc)[1],mtime=0))
 return {'size':b.size,'blocks':len(blocks),'lootPoints':sum(n is not None and n.get('metadata','').startswith('loot:') for _,n in b.blocks.values()),'encounters':[n['metadata'] for _,n in b.blocks.values() if n and n.get('metadata','').startswith('encounter:')]}
reports={}
for v in range(3):
 b=B('v097_fisher_'+str(v),(13,12,17));hut(b,3,2);deck(b,3,9,7,7)
 for z in [11,15]:b.box((3,0,z),(3,4,z),'spruce_log',axis='y')
 lamp(b,3,3,11);b.put(4+v,2,11,'cobweb');b.loot(8,2,8,'fisher');b.loot(5,2,14,'fisher');b.put(9,2,12,'composter',level='2');reports[b.name]=save(b)
 b=B('v097_abandoned_'+str(v),(13,12,15));hut(b,3,2,True);b.put(5+v,2,5,'moss_carpet');deck(b,3,9,7,4);b.loot(8,2,7,'salvage');b.loot(4,2,3,'hidden');b.encounter(5,2,7,'bolotnik');reports[b.name]=save(b)
 b=B('v097_watchtower_'+str(v),(9,21,9));tower(b,2,2,9+v*2);b.loot(4,2,4,'supplies');b.encounter(5,2,4,'bolotnik');reports[b.name]=save(b)
 b=B('v097_shrine_'+str(v),(13,9,13));shrine(b,1,1);b.put(3+v,2,9,'flower_pot');reports[b.name]=save(b)
 b=B('v097_boardwalk_'+str(v),(7,7,19))
 for z in range(1,18):
  for x in range(2,5):
   if z not in [6+v,12+v] or x==3:old.slab(b,x,1,z,'dark_oak',True)
  if z%4==1:
   for x in [1,5]:b.box((x,0,z),(x,3,z),'spruce_log',axis='y')
 lamp(b,1,3,5);b.loot(4,2,17,'supplies');reports[b.name]=save(b)
 b=B('v097_landing_'+str(v),(11,10,13));deck(b,2,2,7,9)
 for x in [2,8]:b.box((x,0,3),(x,5,3),'spruce_log',axis='y');b.box((x,5,3),(x,5,6),'dark_oak_planks')
 b.box((2,6,2),(8,6,6),'dark_oak_slab',type='bottom',waterlogged=False);lamp(b,5,5,3);b.put(3+v,2,4,'cobweb');b.loot(7,2,4,'fisher');b.loot(7,2,9,'supplies');old.boat(b,0,1,6);reports[b.name]=save(b)
 b=B('v097_cluster_'+str(v),(35,22,33));shrine(b,2,2);hut(b,21,3,True);tower(b,23,22,9+v)
 for z in range(13,27):deck(b,6,z,3,1)
 for x in range(8,24):deck(b,x,25,1,3)
 b.loot(8,2,10,'great_shrine');b.loot(26,2,8,'great_shrine');b.loot(26,10+v,25,'great_shrine');b.encounter(5,2,10,'bolotnik_elite');reports[b.name]=save(b)
(root:=R/'docs/swamp').mkdir(exist_ok=True);(root/'templates-0.9.7.json').write_text(json.dumps(reports,indent=2)+'\n',encoding='utf-8')
RES=R/'src/main/resources/data/slavicmyths'
def js(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8')
themes={'fisher':[('minecraft:cod',7,2,5),('minecraft:salmon',6,1,4),('minecraft:fishing_rod',2,1,1),('minecraft:string',6,2,6),('slavicmyths:reed',5,2,5)],'watchtower':[('minecraft:arrow',8,6,16),('minecraft:bread',6,2,5),('minecraft:iron_ingot',3,1,3),('minecraft:bow',2,1,1)],'ritual':[('slavicmyths:pearl_fragment',4,1,3),('minecraft:bone',8,2,6),('slavicmyths:reed',6,3,7),('slavicmyths:ancient_water_sign',1,1,1)],'salvage':[('minecraft:iron_nugget',8,3,9),('minecraft:string',6,2,5),('minecraft:bread',5,1,3)],'great_shrine':[('slavicmyths:pearl_fragment',5,2,5),('slavicmyths:ancient_water_sign',2,1,1),('minecraft:emerald',4,2,5),('minecraft:gold_ingot',4,2,4)]}
for kind,rows in themes.items():
 entries=[{'type':'minecraft:item','name':n,'weight':w,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':a,'max':b}}]} for n,w,a,b in rows]
 js(RES/('loot_table/chests/swamp_'+kind+'.json'),{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':3 if kind=='great_shrine' else 2,'max':6 if kind=='great_shrine' else 4},'entries':entries}]})
ids=['swamp_hut','abandoned_settlement','bog_causeway','flooded_shrine','fishing_camp','underwater_ruins','swamp_remnants','swamp_watchtower']
for ident in ids:
 js(RES/('tags/worldgen/biome/structures/'+ident+'.json'),{'replace':False,'values':['minecraft:swamp']})
 if ident=='swamp_watchtower':
  js(RES/('worldgen/structure/'+ident+'.json'),{'type':'slavicmyths:'+ident,'biomes':'#slavicmyths:structures/'+ident,'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'none'})
  js(RES/('worldgen/structure_set/'+ident+'.json'),{'structures':[{'structure':'slavicmyths:'+ident,'weight':1}],'placement':{'type':'minecraft:random_spread','spacing':32,'separation':16,'salt':970041,'spread_type':'linear'}})
# Separate frequency layers without changing IDs.
for ident,spacing in [('bog_causeway',16),('swamp_remnants',20),('fishing_camp',26),('swamp_hut',30),('flooded_shrine',36),('abandoned_settlement',56)]:
 p=RES/('worldgen/structure_set/'+ident+'.json');d=json.loads(p.read_text());d['placement']['spacing']=spacing;d['placement']['separation']=spacing//2;js(p,d)
print('Authored',len(reports),'modern templates; loot themes',len(themes))
