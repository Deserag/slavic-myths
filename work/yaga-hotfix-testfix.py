from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/verify/YagaPlacementHeadless.java');s=p.read_text().replace('obstruction.blocks.put(new BlockPos(0,8,0)','obstruction.blocks.put(new BlockPos(1,8,1)')
s=s.replace('  for(boolean cancelled:', '''  WorldFixture protectedSite=new WorldFixture(3);protectedSite.blocks.put(new BlockPos(1,8,1),custom.get("slavicmyths:yaga_chicken_leg").defaultBlockState());check(prepare(protectedSite,plan).failure.reason.equals("protected")&&protectedSite.writes==0,"protected mod block overwritten");
  WorldFixture legacy=new WorldFixture(3);legacy.legacyNeighbours=true;YagaPlacement.Site legacySite=prepare(legacy,plan);for(Map.Entry<BlockPos,BlockState> e:legacySite.desired.entrySet())legacy.world.setBlock(e.getKey(),e.getValue(),2);int lost=0;for(Map.Entry<BlockPos,BlockState> e:legacySite.desired.entrySet())if(!e.getValue().isAir()&&legacy.get(e.getKey()).isAir())lost++;check(lost>0,"legacy neighbour-update regression was not reproduced");
  for(boolean cancelled:''')
p.write_text(s,'utf-8')
