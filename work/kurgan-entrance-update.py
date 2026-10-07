from pathlib import Path
p=Path('src/main/java/org/slavicmyths/kurgan/KurganPlan.java');s=p.read_text();s=s.replace('z1=radius+3,y1=14;','z1=radius+(formatVersion==2?17:3),y1=14;');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganArchitecture.java');s=p.read_text();s=s.replace('pos(r.x+1,r.y,r.z)','pos(r.x,r.y,r.z)')
start=s.index(' private void entrance(){');end=s.index(' private boolean coffin',start)
s=s[:start]+''' private void entrance(){int front=plan.radius+1;int h=plan.tier+4,half=plan.tier+3,variant=Math.floorMod((int)plan.seed,3);
  for(int x:new int[]{-half,half})for(int y=0;y<=h;y++)put(x,y,front,KurganBlocks.stone(0,y==h?1:0));
  for(int x=-half;x<=half;x++)if(variant!=1||x<half-1)put(x,h+1,front,KurganBlocks.get("carved_burial_stone").defaultBlockState());
  if(half>3)for(int x:new int[]{-half+1,half-1})for(int y=1;y<=h;y++)put(x,y,front,KurganBlocks.stone(0,1));
  for(int z=front;z<=front+3;z++)for(int x=-1;x<=1;x++)for(int y=1;y<=h;y++)put(x,y,z,Blocks.CAVE_AIR.defaultBlockState());
  // Three authored conditions: intact, broken lintel, earth partly covering the outer pier.
  if(variant==1){put(half-1,1,front+1,slab(0));put(half,1,front+2,KurganBlocks.get("kurgan_rubble").defaultBlockState());}
  if(variant==2)for(int x=half;x<=half+1;x++)for(int y=0;y<=2;y++)put(x,y,front+1,(y==2?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState());
  // Worn approach and two to five scattered markers. No cleared plaza or new trees.
  for(int z=front;z<=front+12;z++)for(int x=-2;x<=2;x++)if(Math.floorMod(x*17+z*13+(int)plan.seed,5)<2)put(x,0,z,Blocks.COBBLESTONE.defaultBlockState());
  int marks=2+Math.floorMod((int)(plan.seed>>>3),4);for(int i=0;i<marks;i++){int dx=(i%2==0?-1:1)*(3+i%3),dz=front+4+i*2;put(dx,0,dz,KurganBlocks.stone(0,2));put(dx,1,dz,i%2==0?slab(0):KurganBlocks.get("kurgan_rubble").defaultBlockState());}
 }
'''+s[end:];p.write_text(s,encoding='utf-8')
# Data generation must include all stone tools and construction crafting routes.
p=Path('tools/kurgan_loot_materials_096.py');s=p.read_text();s=s.replace("# Guaranteed baseline rolls",'''put(D/'recipe/pale_blue_kurgan_stone_stonecutting.json',{'type':'minecraft:stonecutting','ingredient':{'item':'minecraft:stone'},'result':{'id':'slavicmyths:pale_blue_kurgan_stone','count':1}})
put(D/'recipe/bog_green_kurgan_stone.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:stone'}]*4+[{'item':'minecraft:moss_block'}],'result':{'id':'slavicmyths:bog_green_kurgan_stone','count':4}})
p=R/'data/minecraft/tags/block/mineable/pickaxe.json';tag=json.loads(p.read_text(encoding='utf-8-sig'));bases=[f+'_kurgan_'+k if k in ['stone','cobblestone'] else k+'_'+f+'_kurgan_cobblestone' for f in ['pale_blue','bog_green'] for k in ['stone','cobblestone','cracked','mossy']];tag['values']=list(dict.fromkeys(tag['values']+['slavicmyths:'+n for n in bases]));put(p,tag)
# Guaranteed baseline rolls''');p.write_text(s,encoding='utf-8')
