from pathlib import Path
p=Path('src/main/java/org/slavicmyths/kurgan/KurganArchitecture.java');s=p.read_text(encoding='utf-8-sig');s=s.replace('if(r.archetype==KurganPlan.Archetype.TRAP)containerSpaces.add(pos(r.x,r.y,r.z));','if(r.archetype==KurganPlan.Archetype.TRAP){containerSpaces.add(pos(r.x,r.y,r.z));containerSpaces.add(pos(r.x,r.y+1,r.z));}')
s=s.replace('put(r.x,r.y+1,r.z,Blocks.STONE_PRESSURE_PLATE.defaultBlockState());BlockPos p=pos(r.x,r.y,r.z);int key=r.id*16+15;', 'BlockPos p=pos(r.x,r.y,r.z);int key=r.id*16+15;')
s=s.replace('if(!dispenser.getPersistentData().getBoolean("SlavicKurganTrap")){dispenser.setItem', 'if(!dispenser.getPersistentData().getBoolean("SlavicKurganTrap")){raw(p.above(),Blocks.STONE_PRESSURE_PLATE.defaultBlockState());dispenser.setItem');p.write_text(s,encoding='utf-8')
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganReworkGameTests.java');s=p.read_text(encoding='utf-8-sig');needle=' @GameTest(template="port_empty",timeoutTicks=100)\n public static void variantsAndLoot'
code=''' @GameTest(template="port_empty",timeoutTicks=100)
 public static void finiteTrap(GameTestHelper test){
  KurganPlan selected=null;KurganPlan.Room trap=null;for(long seed=0;seed<16&&trap==null;seed++){var p=KurganPlan.create(2,seed);for(var r:p.rooms)if(r.archetype==KurganPlan.Archetype.TRAP){selected=p;trap=r;break;}}
  test.assertTrue(trap!=null,"No trap sample");var plan=selected;var room=trap;var world=test.getLevel();var origin=test.absolutePos(new BlockPos(2304,80,2304));var b=room.box();var clip=new BoundingBox(origin.getX()+b.x0,origin.getY()+b.y0,origin.getZ()+b.z0,origin.getX()+b.x1,origin.getY()+b.y1,origin.getZ()+b.z1);var piece=new KurganDungeonPiece(plan,origin,UUID.randomUUID());
  piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(0),clip,new ChunkPos(origin),origin);var p=origin.offset(room.x,room.y,room.z);
  test.assertTrue(world.getBlockEntity(p) instanceof DispenserBlockEntity,"Trap dispenser missing");var dispenser=(DispenserBlockEntity)world.getBlockEntity(p);test.assertTrue(dispenser.getItem(0).getCount()==8,"Initial ammo drift");
  world.setBlock(p.above(),Blocks.STONE_PRESSURE_PLATE.defaultBlockState().setValue(PressurePlateBlock.POWERED,true),3);world.neighborChanged(p,Blocks.STONE_PRESSURE_PLATE,p.above());
  test.runAfterDelay(6,()->{test.assertTrue(dispenser.getItem(0).getCount()==7,"Trap did not fire exactly one arrow");piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(0),clip,new ChunkPos(origin),origin);test.assertTrue(world.getBlockEntity(p)==dispenser&&dispenser.getItem(0).getCount()==7,"Trap retry replenishes ammo");System.out.println("KURGAN_RUNTIME_TRAP_PASS initialAmmo=8 afterShot=7 retriesDoNotRefill=true");test.succeed();});
 }
''';assert needle in s;s=s.replace(needle,code+needle);p.write_text(s,encoding='utf-8')
