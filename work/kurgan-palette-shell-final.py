from pathlib import Path
p=Path('tools/kurgan_resources_096.py');s=p.read_text(encoding='utf-8-sig');i=s.index("for lang in ['en_us','ru_ru']:");s=s[:i]+'''for shape in ['slab','stairs','wall']:
 ids=[n for n in new if n.endswith('_'+shape)]
 for registry in ['block','item']:
  p=R/'data/minecraft/tags'/registry/(shape+'s.json');v=json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['slavicmyths:'+n for n in ids]));put(p,v)
'''+s[i:];p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganPlan.java');s=p.read_text();s=s.replace('public Box box(){return new Box(x-rx,y,z-rz,x+rx,y+height+1,z+rz);}','public Box box(){int outer=archetype==Archetype.DEEP?1:0;return new Box(x-rx-outer,y-outer,z-rz-outer,x+rx+outer,y+height+1+outer,z+rz+outer);}')
p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganLayout.java');s=p.read_text();s=s.replace('if(x<=b.x0||x>=b.x1||z<=b.z0||z>=b.z1||y<=r.y||y>r.y+r.height)', 'if(Math.abs(x-r.x)>=r.rx||Math.abs(z-r.z)>=r.rz||y<=r.y||y>r.y+r.height)');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganArchitecture.java');s=p.read_text(encoding='utf-8-sig');needle=' private BlockState slab(int palette)'
code=''' private BlockState roomStone(KurganPlan.Room r,int x,int y,int z){
  // Material/weight pairs: cold, brick, vanilla brick, cracked vanilla, stone, gravel,
  // cracked custom, carved, cobble, damp custom, mossy vanilla.
  boolean floor=y<=r.y;int[] weights=r.archetype==null?new int[]{0,45,1,35,2,15,6,5}:switch(r.archetype){
   case VESTIBULE->floor?new int[]{0,25,1,25,2,20,3,15,4,10,5,5}:new int[]{1,35,0,25,2,20,6,10,8,5,7,5};
   case CROSSROADS->floor?new int[]{1,40,0,25,2,20,7,15}:new int[]{1,40,0,25,2,25,6,10};
   case BURIAL,DEEP->floor?new int[]{0,35,1,30,7,20,2,15}:new int[]{1,45,0,25,2,15,7,10,6,5};
   case WARRIOR->floor?new int[]{1,45,0,25,2,20,6,10}:new int[]{1,50,0,20,2,20,6,5,8,5};
   case TREASURY->floor?new int[]{1,40,0,30,2,20,7,10}:new int[]{1,45,0,20,2,20,6,10,7,5};
   case RITUAL->floor?new int[]{0,35,7,30,1,20,2,15}:new int[]{1,35,0,25,7,15,2,15,6,5,9,5};
   case TRAP->floor?new int[]{1,45,0,20,2,20,6,10,3,5}:new int[]{1,35,0,25,4,15,8,10,6,15};
   case FLOODED->floor?new int[]{0,25,9,20,1,15,10,20,4,10,5,10}:new int[]{9,30,1,20,0,20,10,20,6,10};
   case OSSUARY->floor?new int[]{0,40,1,25,4,20,6,15}:new int[]{1,35,0,30,2,20,6,10,8,5};
   case RELIQUARY->floor?new int[]{0,35,1,30,7,20,2,15}:new int[]{1,40,0,25,7,20,2,15};
   case COLLAPSED->floor?new int[]{0,20,1,20,6,20,8,15,4,15,5,10}:new int[]{1,20,0,20,6,20,8,10,4,10,3,20};
   default->floor?new int[]{0,40,1,30,2,20,6,10}:new int[]{1,40,0,30,2,20,6,10};
  };
  int patch=Math.floorMod(Math.floorDiv(x-r.x,4)*19+Math.floorDiv(z-r.z,4)*31+Math.floorDiv(y-r.y,3)*7+(int)plan.seed+r.id*17,100),kind=0;
  for(int i=0;i<weights.length;i+=2){patch-=weights[i+1];if(patch<0){kind=weights[i];break;}}
  return switch(kind){case 1->KurganBlocks.stone(0,1);case 2->Blocks.STONE_BRICKS.defaultBlockState();case 3->Blocks.CRACKED_STONE_BRICKS.defaultBlockState();case 4->Blocks.STONE.defaultBlockState();case 5->Blocks.GRAVEL.defaultBlockState();case 6->KurganBlocks.stone(r.palette,2);case 7->KurganBlocks.get("carved_burial_stone").defaultBlockState();case 8->Blocks.COBBLESTONE.defaultBlockState();case 9->KurganBlocks.stone(r.palette,3);case 10->Blocks.MOSSY_STONE_BRICKS.defaultBlockState();default->KurganBlocks.stone(0,0);};
 }
''';s=s.replace(needle,code+needle)
start=s.index(' private void shell(');end=s.index(' private void corridor(',start)
s=s[:start]+''' private void shell(KurganPlan.Room r){var b=r.box();
  for(int x=Math.max(b.x0,clip.minX()-piece.origin.getX());x<=Math.min(b.x1,clip.maxX()-piece.origin.getX());x++)for(int z=Math.max(b.z0,clip.minZ()-piece.origin.getZ());z<=Math.min(b.z1,clip.maxZ()-piece.origin.getZ());z++)for(int y=r.y-1;y<=b.y1;y++){
   boolean air=plan.roomAir(r,x,y,z),outer=r.archetype==KurganPlan.Archetype.DEEP&&(x==b.x0||x==b.x1||z==b.z0||z==b.z1||y==b.y0||y==b.y1);
   put(x,y,z,outer?KurganBlocks.get("sealed_kurgan_masonry").defaultBlockState():air?Blocks.CAVE_AIR.defaultBlockState():y<r.y?KurganBlocks.stone(r.palette,0):roomStone(r,x,y,z));
  }
 }
 private void restoreProtectedTomb(){if(plan.tier!=2)return;var r=plan.rooms.get(plan.finalRoom);var b=r.box();
  // Corridor shells must never turn the roof above the sealed doorway into mineable masonry.
  for(int x=b.x0;x<=b.x1;x++)for(int z=b.z0;z<=b.z1;z++)for(int y=b.y0;y<=b.y1;y++)if(x==b.x0||x==b.x1||z==b.z0||z==b.z1||y==b.y0||y==b.y1)put(x,y,z,KurganBlocks.get("sealed_kurgan_masonry").defaultBlockState());
 }
'''+s[end:];s=s.replace('  for(var r:plan.rooms)decorate(r);','  restoreProtectedTomb();\n  for(var r:plan.rooms)decorate(r);')
s=s.replace('   if(r.height>=7)prop(r,-side,r.height,-back,"kurgan_hanging_brazier");','   put(r.x+side,r.y+3,r.z-r.rz+1,Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,Direction.SOUTH));\n   if(r.height>=7)prop(r,-side,r.height,-back,"kurgan_hanging_brazier");');p.write_text(s,encoding='utf-8')
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganReworkGameTests.java');s=p.read_text(encoding='utf-8-sig');s=s.replace('origin.offset(r.x,r.y,r.z)).is(KurganBlocks.get("sealed_kurgan_masonry"))','origin.offset(r.x,r.y-1,r.z)).is(KurganBlocks.get("sealed_kurgan_masonry"))').replace('origin.offset(r.x,r.y+r.height+1,r.z)).is(KurganBlocks.get("sealed_kurgan_masonry"))','origin.offset(r.x,r.y+r.height+2,r.z)).is(KurganBlocks.get("sealed_kurgan_masonry"))');needle='"Unprotected tomb roof");}';s=s.replace(needle,'"Unprotected tomb roof");test.assertTrue(world.getBlockState(origin.offset(r.x,r.y+6,r.z-r.rz-1)).is(KurganBlocks.get("sealed_kurgan_masonry")),"Mineable bypass above seal");}');p.write_text(s,encoding='utf-8')
