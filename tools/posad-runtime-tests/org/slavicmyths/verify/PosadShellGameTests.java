package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.posad.*;
import net.minecraft.world.level.block.Blocks;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadShellGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void openGate(GameTestHelper t) {
  for(int x=53;x<=59;x++)for(int z=108;z<=112;z++)for(int y=1;y<=6;y++)
   t.assertTrue(PosadShell.voxel(x,y,z).isAir(),"Gate blocked "+x+","+y+","+z);
  for(int x=0;x<113;x++)for(int z=0;z<113;z++)
   t.assertTrue(PosadShell.voxel(x,0,z)!=null&&!PosadShell.voxel(x,0,z).isAir(),"Platform hole");
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void patrolAndSlots(GameTestHelper t) {
  for(int z=11;z<102;z++)for(int x=1;x<=3;x++) {
   t.assertTrue(!PosadShell.voxel(x,8,z).isAir(),"Missing patrol floor");
   for(int y=9;y<=10;y++)t.assertTrue(PosadShell.voxel(x,y,z).isAir(),"Blocked patrol");
  }
  for(var slot:PosadGeometry.Slot.values())for(int x=slot.local().minX();x<=slot.local().maxX();x++)
   for(int z=slot.local().minZ();z<=slot.local().maxZ();z++)for(int y=1;y<18;y++)
    t.assertTrue(PosadShell.voxel(x,y,z)==null,"Shell occupies building slot "+slot);
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void physicalChunkPlacement(GameTestHelper t) {
  var world=t.getLevel();
  for(int turns=0;turns<4;turns++) {
  var anchor=t.absolutePos(new net.minecraft.core.BlockPos(turns*160,140,0));
  var piece=new PosadPiece(anchor,turns);
  for(var patch:PosadGeometry.chunkPatches(anchor.getX(),anchor.getZ())) {
   world.getChunk(patch.chunkX(),patch.chunkZ());world.setChunkForced(patch.chunkX(),patch.chunkZ(),true);
   var r=patch.worldClip();
   var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(r.minX(),anchor.getY()-2,r.minZ(),r.maxX(),anchor.getY()+17,r.maxZ());
   piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),world.random,clip,
    new net.minecraft.world.level.ChunkPos(patch.chunkX(),patch.chunkZ()),anchor);
  }
  for(int x=0;x<113;x++)for(int z=0;z<113;z++) {
   var at=anchor.offset(x,0,z);
   t.assertTrue(!world.getBlockState(at).isAir(),"Physical platform hole "+at);
  }
  for(int x=53;x<=59;x++)for(int z=108;z<=112;z++)for(int y=1;y<=6;y++) {
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   t.assertTrue(world.getBlockState(anchor.offset(p.x(),y,p.z())).isAir(),"Physical rotated gate blocked");
  }
  // Schema 19: compare the full approved 1337-voxel source with actual rotated Minecraft blocks.
  int teremCount=0;
  for(int y=0;y<18;y++)for(int z=78;z<=95;z++)for(int x=60;x<=72;x++){
   var approved=PosadnikTerem.voxel(x,y,z);
   if(approved==null)continue;
   teremCount++;
   var cell=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   var at=anchor.offset(cell.x(),y,cell.z());
   var actual=world.getBlockState(at);
   var expected=approved.rotate(net.minecraft.world.level.block.Rotation.values()[turns]);
   t.assertTrue(actual.is(expected.getBlock()),"Posadnik A+ mismatch "+x+","+y+","+z+" rot="+turns+" expected="+expected+" actual="+actual);
   if(expected.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)==expected.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING),"Posadnik facing "+at);
   if(expected.hasProperty(net.minecraft.world.level.block.DoorBlock.HALF))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.DoorBlock.HALF)==expected.getValue(net.minecraft.world.level.block.DoorBlock.HALF),"Posadnik door half "+at);
  }
  t.assertTrue(teremCount==1337&&PosadnikTerem.count()==1337,"Posadnik block count drift: "+teremCount);
  for(int z:new int[]{84,85}){
   var door=PosadGeometry.rotate(new PosadGeometry.Cell(62,z),turns);
   t.assertTrue(world.getBlockState(anchor.offset(door.x(),1,door.z())).getBlock() instanceof net.minecraft.world.level.block.DoorBlock,"Posadnik entrance missing rotation "+turns);
  }
  System.out.println("POSADNIK_A_PLUS_NATIVE_PASS rotation="+turns+" approved_voxels="+teremCount);
  // R3 physical roof/fronton, dais and both existing double-width staircases.
  int fronton=0;
  for(int y=12;y<=17;y++)for(int x=50;x<=62;x++){
   var expected=PosadArchitectureR3.voxel(x,y,34);
   if(expected==null)continue;
   fronton++;
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,34),turns);
   var at=anchor.offset(p.x(),y,p.z());
   var actual=world.getBlockState(at);
   t.assertTrue(actual.is(expected.getBlock()),"R3 portico mismatch "+at);
  }
  t.assertTrue(fronton==36,"R3 fronton count "+fronton);
  for(int[] q:new int[][]{{56,2,15},{55,2,15},{57,2,15},{56,2,14},{56,3,14}}){
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(q[0],q[2]),turns);
   t.assertTrue(!world.getBlockState(anchor.offset(p.x(),q[1],p.z())).isAir(),"R3 throne missing");
  }
  for(int step=0;step<6;step++)for(int[] stair:new int[][]{{20,26-step},{21,26-step},{23,84-step},{24,84-step}}){
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(stair[0],stair[1]),turns);
   var at=anchor.offset(p.x(),step+1,p.z());
   t.assertTrue(world.getBlockState(at).getBlock() instanceof net.minecraft.world.level.block.StairBlock,"Blocked 2F flight "+at);
   for(int head=1;head<=2;head++)t.assertTrue(world.getBlockState(at.above(head)).isAir(),"Stair requires jumping "+at+" height="+head);
  }
  // Schema 21: both northbound interior flights have their LOW edges south.
  // Verify real placed blockstates, rotated FACING, HALF and unobstructed headroom.
  int correctedStairs=0;
  for(int step=0;step<6;step++)for(int[] stair:new int[][]{{20,26-step},{21,26-step},{23,84-step},{24,84-step}}){
   int x=stair[0],z=stair[1],y=step+1;
   var source=PosadArchitectureR4.voxel(x,y,z);
   t.assertTrue(source!=null&&source.getValue(net.minecraft.world.level.block.StairBlock.FACING)==net.minecraft.core.Direction.SOUTH,"Invalid schema-21 northbound tread "+x+","+y+","+z);
   var local=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   var at=anchor.offset(local.x(),y,local.z());
   var placed=world.getBlockState(at);
   var expected=source.rotate(net.minecraft.world.level.block.Rotation.values()[turns]);
   t.assertTrue(placed.is(expected.getBlock()),"Missing schema-21 tread "+at);
   t.assertTrue(placed.getValue(net.minecraft.world.level.block.StairBlock.FACING)==expected.getValue(net.minecraft.world.level.block.StairBlock.FACING),"Unrotated/reversed schema-21 stair "+at);
   t.assertTrue(placed.getValue(net.minecraft.world.level.block.StairBlock.HALF)==net.minecraft.world.level.block.state.properties.Half.BOTTOM,"Vertical/upper stair "+at);
   for(int head=1;head<=2;head++)t.assertTrue(world.getBlockState(at.above(head)).isAir(),"Obstructed stair headroom "+at);
   correctedStairs++;
  }
  t.assertTrue(correctedStairs==24&&PosadArchitectureR4.size()==24,"Missing schema-21 stair repairs");
  System.out.println("POSAD_R4_STAIRS_NATIVE_PASS rotation="+turns+" treads="+correctedStairs);
  System.out.println("POSAD_R3_ARCH_PASS rotation="+turns+" fronton="+fronton);
  int hallBlocks=0,houseBlocks=0,houseChests=0;
  for(int x=42;x<=70;x++)for(int z=12;z<=34;z++)for(int y=0;y<18;y++) {
   var source=PosadBuildings.voxel(x,y,z);
   if(source!=null)hallBlocks++;
   var expected=piece.voxel(x,y,z);
   if(expected==null)continue;
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   var actual=world.getBlockState(anchor.offset(p.x(),y,p.z()));
   var rotated=expected.rotate(net.minecraft.world.level.block.Rotation.values()[turns]);
   t.assertTrue(actual.is(rotated.getBlock()),"Current Veche mismatch at "+x+","+y+","+z+" rot="+turns+" expected="+rotated+" found="+actual);
   if(rotated.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)==rotated.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING),"Veche facing mismatch "+x+","+y+","+z+" rot="+turns);
   if(rotated.hasProperty(net.minecraft.world.level.block.DoorBlock.HALF))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.DoorBlock.HALF)==rotated.getValue(net.minecraft.world.level.block.DoorBlock.HALF),"Veche door half mismatch "+x+","+y+","+z+" rot="+turns);
   if(rotated.hasProperty(net.minecraft.world.level.block.DoorBlock.HINGE))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.DoorBlock.HINGE)==rotated.getValue(net.minecraft.world.level.block.DoorBlock.HINGE),"Veche door hinge mismatch "+x+","+y+","+z+" rot="+turns);
   if(rotated.hasProperty(net.minecraft.world.level.block.StairBlock.HALF))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.StairBlock.HALF)==rotated.getValue(net.minecraft.world.level.block.StairBlock.HALF),"Veche roof/stair half mismatch "+x+","+y+","+z+" rot="+turns);
  }
  for(int x=13;x<=35;x++)for(int z=15;z<=29;z++)for(int y=0;y<17;y++) {
   var source=PosadBuildings.voxel(x,y,z);
   if(source!=null)houseBlocks++;
   var expected=piece.voxel(x,y,z);
   if(expected==null)continue;
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   var at=anchor.offset(p.x(),y,p.z());
   var actual=world.getBlockState(at);
   var rotated=expected.rotate(net.minecraft.world.level.block.Rotation.values()[turns]);
   t.assertTrue(actual.is(rotated.getBlock()),"Current communal block mismatch "+x+","+y+","+z+" rot="+turns+" expected="+rotated+" actual="+actual);
   if(rotated.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)==rotated.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING),"Communal roof/door facing mismatch "+at);
   if(rotated.hasProperty(net.minecraft.world.level.block.DoorBlock.HALF))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.DoorBlock.HALF)==rotated.getValue(net.minecraft.world.level.block.DoorBlock.HALF),"Communal door half mismatch "+at);
   if(rotated.hasProperty(net.minecraft.world.level.block.DoorBlock.HINGE))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.DoorBlock.HINGE)==rotated.getValue(net.minecraft.world.level.block.DoorBlock.HINGE),"Communal door hinge mismatch "+at);
   if(rotated.hasProperty(net.minecraft.world.level.block.StairBlock.HALF))
    t.assertTrue(actual.getValue(net.minecraft.world.level.block.StairBlock.HALF)==rotated.getValue(net.minecraft.world.level.block.StairBlock.HALF),"Communal stair half mismatch "+at);
   if(expected.is(Blocks.CHEST)) {
    houseChests++;
    String loot=PosadHouseRenovation.chestLoot(x,y,z);
    if(loot==null)loot=PosadBuildings.chestLoot(x,y,z);
    t.assertTrue(loot!=null,"Missing current-schema communal loot marker "+at);
    t.assertTrue(world.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity,"Chest block entity missing "+at);
    var chest=(net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)world.getBlockEntity(at);
    t.assertTrue(chest.getLootTable()!=null,"Chest lazy loot table missing "+at);
   }
  }
  int militaryBlocks=0,forgeBlocks=0,militaryChests=0,forgeChests=0;
  for(var slot:java.util.List.of(PosadGeometry.Slot.DRUZHINNY_DVOR,PosadGeometry.Slot.KUZNECHNY_DVOR)) {
   for(int x=slot.local().minX();x<=slot.local().maxX();x++)for(int z=slot.local().minZ();z<=slot.local().maxZ();z++)for(int y=0;y<18;y++) {
    var source=PosadNextBuildings.voxel(x,y,z);
    if(source!=null){if(slot==PosadGeometry.Slot.DRUZHINNY_DVOR)militaryBlocks++;else forgeBlocks++;}
    var expected=piece.voxel(x,y,z);
    if(expected==null)continue;
    var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
    var at=anchor.offset(p.x(),y,p.z());
    var actual=world.getBlockState(at);
    var rotated=expected.rotate(net.minecraft.world.level.block.Rotation.values()[turns]);
    t.assertTrue(actual.is(rotated.getBlock()),"Physical district block mismatch "+slot+" "+x+","+y+","+z+" rot="+turns+" expected="+rotated+" found="+actual);
    // Minecraft recalculates neighboring fence/bars/wall connections and stair
    // shapes during chunk placement. Those are NOT fixed art properties.
    // Preserve and assert the directional/functional properties themselves.
    if(rotated.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING))
     t.assertTrue(actual.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)==rotated.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING),"Wrong horizontal facing "+slot+" "+at);
    if(rotated.hasProperty(net.minecraft.world.level.block.DoorBlock.HALF))
     t.assertTrue(actual.getValue(net.minecraft.world.level.block.DoorBlock.HALF)==rotated.getValue(net.minecraft.world.level.block.DoorBlock.HALF),"Wrong door half "+slot+" "+at);
    if(rotated.hasProperty(net.minecraft.world.level.block.DoorBlock.HINGE))
     t.assertTrue(actual.getValue(net.minecraft.world.level.block.DoorBlock.HINGE)==rotated.getValue(net.minecraft.world.level.block.DoorBlock.HINGE),"Wrong door hinge "+slot+" "+at);
    if(rotated.hasProperty(net.minecraft.world.level.block.StairBlock.HALF))
     t.assertTrue(actual.getValue(net.minecraft.world.level.block.StairBlock.HALF)==rotated.getValue(net.minecraft.world.level.block.StairBlock.HALF),"Wrong stair half "+slot+" "+at);
    if(rotated.hasProperty(net.minecraft.world.level.block.ChestBlock.TYPE))
     t.assertTrue(actual.getValue(net.minecraft.world.level.block.ChestBlock.TYPE)==rotated.getValue(net.minecraft.world.level.block.ChestBlock.TYPE),"Wrong chest type "+slot+" "+at);
    if(rotated.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED))
     t.assertTrue(actual.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)==rotated.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED),"Waterlogging changed in Nether "+slot+" "+at);
    if(expected.is(Blocks.CHEST)) {
     if(slot==PosadGeometry.Slot.DRUZHINNY_DVOR)militaryChests++;else forgeChests++;
     var loot=PosadNextBuildings.chestLoot(x,y,z);
     t.assertTrue(loot!=null,"Missing fixed district loot marker "+at);
     t.assertTrue(world.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity,"No district chest BE "+at);
     var chest=(net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)world.getBlockEntity(at);
     t.assertTrue(chest.getLootTable()!=null,"No district lazy loot at "+at);
     t.assertTrue(chest.getLootTable().location().toString().equals("slavicmyths:"+loot),"Incorrect district table at "+at);
    }
   }
  }
  t.assertTrue(militaryBlocks==2440&&forgeBlocks==1901,"Wrong 2.2.4/2.2.5 block counts "+militaryBlocks+"/"+forgeBlocks);
  t.assertTrue(militaryChests==5&&forgeChests==5,"Wrong district chest counts "+militaryChests+"/"+forgeChests);
  int emittedLights=0;
  for(int x=0;x<113;x++)for(int z=0;z<113;z++)for(int y:new int[]{1,7}) {
   var light=PosadLightingR3.voxel(x,y,z);
   if(light==null)continue;
   emittedLights++;
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   var at=anchor.offset(p.x(),y,p.z());
   var actual=world.getBlockState(at);

   if(PosadGeometry.Slot.VECHE_HALL.local().contains(x,z)){
    t.assertTrue(actual.is(Blocks.LANTERN),"Veche old lamp missing "+at);
    t.assertTrue(actual.canSurvive(world,at),"Veche lamp unsupported "+at);
   }else{
    t.assertTrue(actual.is(Blocks.POLISHED_BASALT),"R3 lamppost foot missing "+at+" found "+actual);
    int dx=x==52?2:x==60?-2:x==40?1:x==72?-1:0;
    int dz=z==39?1:z==75?-1:0;
    var hanging=PosadGeometry.rotate(new PosadGeometry.Cell(x+dx,z+dz),turns);
    var lampAt=anchor.offset(hanging.x(),3,hanging.z());
    t.assertTrue(world.getBlockState(lampAt).is(Blocks.LANTERN),"R3 hanging street lantern missing "+lampAt);
    t.assertTrue(world.getBlockState(lampAt).canSurvive(world,lampAt),"R3 hanging street lantern unsupported "+lampAt);
    t.assertTrue(world.getBlockState(lampAt.above()).is(Blocks.CHAIN),"R3 street bracket chain missing "+lampAt);
   }
  }
  for(int[] center:new int[][]{{5,5},{107,5},{5,107},{107,107},{46,107},{66,107}})
   for(int ceiling:new int[]{8,14}){
    var loc=PosadGeometry.rotate(new PosadGeometry.Cell(center[0],center[1]),turns);
    var at=anchor.offset(loc.x(),ceiling-1,loc.z());
    var state=world.getBlockState(at);
    t.assertTrue(state.is(Blocks.LANTERN),"R3 tower lantern absent "+at);
    t.assertTrue(state.canSurvive(world,at),"R3 tower lantern unsupported "+at);
   }
  System.out.println("POSAD_R3_TOWER_LAMPS_PASS rotation="+turns+" supported=12");
  int connectedWindows=0;
  for(int x=0;x<=112;x++)for(int z=0;z<=112;z++)for(int y=1;y<=17;y++){
   var planned=piece.voxel(x,y,z);
   if(planned==null||!(planned.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock))continue;
   var rotated=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   var at=anchor.offset(rotated.x(),y,rotated.z());
   var actual=world.getBlockState(at);
   t.assertTrue(actual.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock,"Missing pane "+at);
   for(var side:new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.WEST}){
    var adjacent=at.relative(side);
    var recomputed=actual.updateShape(side,world.getBlockState(adjacent),world,at,adjacent);
    t.assertTrue(recomputed.equals(actual),"R3 window disconnected "+at+" "+side+" actual="+actual+" expected="+recomputed);
   }
   connectedWindows++;
  }
  t.assertTrue(connectedWindows>=40,"R3 pane regression "+connectedWindows);
  System.out.println("POSAD_R3_PANES_CONNECTED turn="+turns+" panes="+connectedWindows);
  t.assertTrue(emittedLights==28,"Wrong schema-18 physical lantern count "+emittedLights+" turn="+turns);
  int r2Cells=0;
  for(int x=42;x<=70;x++)for(int z=12;z<=34;z++)for(int y=0;y<=16;y++) {
   var r2=PosadHallR2.voxel(x,y,z);
   if(r2==null)continue;
   r2Cells++;
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   var actual=world.getBlockState(anchor.offset(p.x(),y,p.z()));
   t.assertTrue(actual.is(r2.getBlock()),"R2 physical mismatch "+x+","+y+","+z+" rot="+turns+" expected="+r2+" actual="+actual);
  }
  t.assertTrue(r2Cells==160,"Wrong Veche R2 geometry count "+r2Cells);
  t.assertTrue(hallBlocks==3598,"Wrong immutable R1 hall count "+hallBlocks);
  t.assertTrue(houseBlocks==1992 && houseChests==4,"Wrong house voxel/chest count "+houseBlocks+"/"+houseChests);
  // Design-only #376: two possible roof envelopes for a future Posadnik house.
  // Check the *actual* native city, not just an SVG, in all four rotations.
  // This does NOT authorize building the residence or reserving either plot.
  int[][] candidates={{61,72,78,95},{41,51,78,95}};
  for(int candidate=0;candidate<candidates.length;candidate++) {
   int[] area=candidates[candidate];
   for(int x=area[0];x<=area[1];x++)for(int z=area[2];z<=area[3];z++) {
    for(var slot:PosadGeometry.Slot.values())
     t.assertTrue(!slot.local().contains(x,z),"Posadnik candidate overlaps reserved "+slot+" at "+x+","+z);
    for(int y=1;y<=17;y++) {
     var local=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
     var at=anchor.offset(local.x(),y,local.z());
     var planned=piece.voxel(x,y,z);

     if(candidate==0){
      var approved=PosadnikTerem.voxel(x,y,z);
      if(approved==null){
       t.assertTrue(planned==null||planned.isAir(),"Unexpected block beside approved Posadnik "+x+","+y+","+z);
       t.assertTrue(world.getBlockState(at).isAir(),"Unexpected physical block beside Posadnik "+at);
      }else{
       t.assertTrue(planned!=null&&planned.is(approved.getBlock()),"Approved Posadnik source missing "+x+","+y+","+z);
       t.assertTrue(world.getBlockState(at).is(approved.getBlock()),"Approved Posadnik physical block missing "+at);
      }
     }else{
      t.assertTrue(planned==null||planned.isAir(),"West alternative must remain empty "+x+","+y+","+z);
      t.assertTrue(world.getBlockState(at).isAir(),"West alternative occupied "+at);
     }
    }
    var local=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
    t.assertTrue(!world.getBlockState(anchor.offset(local.x(),0,local.z())).isAir(),"Posadnik candidate has no platform at "+x+","+z);
   }
   int entryZ=candidate==0?84:83;
   int from=candidate==0?59:50,to=candidate==0?61:53;
   for(int x=from;x<=to;x++)for(int y=1;y<=2;y++) {
    var local=PosadGeometry.rotate(new PosadGeometry.Cell(x,entryZ),turns);
    var at=anchor.offset(local.x(),y,local.z());
    t.assertTrue(world.getBlockState(at).isAir(),"Posadnik candidate street access blocked turn="+turns+" option="+candidate+" at="+x+","+y+","+entryZ);
   }
   System.out.println("POSAD_TEREM_SITE_SURVEY schema="+PosadGeometry.SCHEMA_VERSION+" turn="+turns+" option="+(candidate==0?"east":"west")+" roof="+area[0]+".."+area[1]+","+area[2]+".."+area[3]+" y=1..17 "+(candidate==0?"built=true":"empty=true")+" roadAccess=true");
  }
  }
  // Observe actual BLOCK light AFTER Minecraft's light engine has processed the
  // placed chunks. This is a survey, not a fake PASS for 8/10 light targets.
  // Samples include the entry, R2 nave, north room, stairs and upper floor,
  // plus major public routes. Log blocked samples rather than hiding them.
  t.runAfterDelay(60,()->{
   String[] labels={"hall_entry","hall_south","hall_nave","hall_north","hall_elder","hall_upper_north","hall_upper_south","hall_west_stair","hall_upper_west","hall_upper_east","city_veche_square","city_veche_street","city_east_road","city_south_road"};
   int[][] samples={{55,1,32},{55,1,30},{55,1,25},{55,1,21},{55,1,17},{55,7,17},{55,7,31},{46,1,26},{46,7,21},{65,7,24},{56,1,56},{56,1,38},{73,1,56},{56,1,76}};
   for(int turns=0;turns<4;turns++){
    var anchor=t.absolutePos(new net.minecraft.core.BlockPos(turns*160,140,0));
    for(int i=0;i<samples.length;i++){
     int[] p=samples[i];
     var local=PosadGeometry.rotate(new PosadGeometry.Cell(p[0],p[2]),turns);
     var at=anchor.offset(local.x(),p[1],local.z());
     var feet=world.getBlockState(at);
     var head=world.getBlockState(at.above());
     var floor=world.getBlockState(at.below());
     int blockLight=world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,at);
     System.out.println("POSAD_LIGHT_SURVEY schema="+PosadGeometry.SCHEMA_VERSION+" turn="+turns+" room="+labels[i]+" local="+p[0]+","+p[1]+","+p[2]+" value="+blockLight+" feet="+feet.getBlock()+" head="+head.getBlock()+" floor="+floor.getBlock());
     // Required public passages >=8 and work rooms >=10, measured after light propagation.
     int minimum=(i==2||i==3||i==4||i==5||i==6)?10:8;
     t.assertTrue(blockLight>=minimum,"Schema-18 dark place turn="+turns+" room="+labels[i]+" light="+blockLight+" required="+minimum+" at "+at);
    }
   }
   // Schema-19: actual Minecraft block light/clearance after light engine updates.
   for(int rotation=0;rotation<4;rotation++){
    var residenceAnchor=t.absolutePos(new net.minecraft.core.BlockPos(rotation*160,140,0));
    for(int[] sample:new int[][]{{67,1,85},{67,1,91},{67,7,83},{67,7,91}}){
     var spot=PosadGeometry.rotate(new PosadGeometry.Cell(sample[0],sample[2]),rotation);
     var at=residenceAnchor.offset(spot.x(),sample[1],spot.z());
     t.assertTrue(world.getBlockState(at).isAir()&&world.getBlockState(at.above()).isAir(),"Posadnik route blocked "+at);
     int brightness=world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,at);
     System.out.println("POSADNIK_LIGHT_SURVEY turn="+rotation+" local="+sample[0]+","+sample[1]+","+sample[2]+" value="+brightness);
     t.assertTrue(brightness>=8,"Posadnik work area too dark "+at+" BLOCK="+brightness);
    }
   }
   // Walkable first-floor R2 central axis and the full eleven-block-wide
   // passage between R05 nave and former R08 vestibule. Verify AIR at feet
   // and head height in ACTUAL Minecraft blocks after all overlays/rotations.
   for(int turns=0;turns<4;turns++){
    var anchor=t.absolutePos(new net.minecraft.core.BlockPos(turns*160,140,0));
    for(int z=20;z<=32;z++){
     var p=PosadGeometry.rotate(new PosadGeometry.Cell(55,z),turns);
     var at=anchor.offset(p.x(),1,p.z());
     t.assertTrue(world.getBlockState(at).isAir() && world.getBlockState(at.above()).isAir(),"R2 central route blocked turn="+turns+" local=55,1,"+z);
     t.assertTrue(!world.getBlockState(at.below()).isAir(),"R2 main route missing floor turn="+turns+" local=55,0,"+z);
     t.assertTrue(world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,at)>=8,"R2 centerline below light 8 turn="+turns+" city=55,1,"+z);
    }
    // Entire public carriageway, not just selected snapshots, stays >=8.
    for(int z=35;z<=106;z++){
     var p=PosadGeometry.rotate(new PosadGeometry.Cell(56,z),turns);
     var at=anchor.offset(p.x(),1,p.z());
     t.assertTrue(world.getBlockState(at).isAir() && world.getBlockState(at.above()).isAir(),"Street path obstructed turn="+turns+" city=56,1,"+z);
     int value=world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,at);
     t.assertTrue(value>=8,"Dark public road turn="+turns+" city=56,1,"+z+" BLOCK="+value);
    }
    for(int x=51;x<=61;x++){
     var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,28),turns);
     var at=anchor.offset(p.x(),1,p.z());
     t.assertTrue(world.getBlockState(at).isAir() && world.getBlockState(at.above()).isAir(),"R2 ceremonial arch blocked turn="+turns+" local="+x+",1,28");
    }
   }
    for(int turns=0;turns<4;turns++){
     var centerAnchor=t.absolutePos(new net.minecraft.core.BlockPos(turns*160,140,0));
     for(int[] center:new int[][]{{5,5},{107,5},{5,107},{107,107},{46,107},{66,107}}){
      var loc=PosadGeometry.rotate(new PosadGeometry.Cell(center[0],center[1]),turns);
      for(int floorY:new int[]{1,9}){
       var pos=centerAnchor.offset(loc.x(),floorY,loc.z());
       int brightness=world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos);
       System.out.println("POSAD_R3_TOWER_LIGHT turn="+turns+" at="+pos+" block="+brightness);
       t.assertTrue(brightness>=8,"R3 unlit tower "+pos+" brightness="+brightness);
      }
     }
    }
   // Grounded district survey: sample two-block-high navigable cells with a
   // solid floor and an overhead structure. Exclude open courtyards, which
   // intentionally have no roof and need different outdoor design targets.
   // Do not assert the target yet: first expose all real dark sample positions.
   for(int turns=0;turns<4;turns++) {
    var anchor=t.absolutePos(new net.minecraft.core.BlockPos(turns*160,140,0));
    for(var slot:PosadGeometry.Slot.values()) {
     if(slot==PosadGeometry.Slot.VECHE_SQUARE||slot==PosadGeometry.Slot.VECHE_HALL)continue;
     for(int y:new int[]{1,7}) {
      int total=0,low8=0,low10=0,minimum=15;
      var dark=new java.util.ArrayList<String>();
      for(int x=slot.local().minX()+2;x<=slot.local().maxX()-2;x+=2)
       for(int z=slot.local().minZ()+2;z<=slot.local().maxZ()-2;z+=2) {
        var rotated=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
        var at=anchor.offset(rotated.x(),y,rotated.z());
        if(!world.getBlockState(at).isAir()||!world.getBlockState(at.above()).isAir()||world.getBlockState(at.below()).isAir())continue;
        boolean roof=false;
        for(int dy=3;dy<=10 && y+dy<=17;dy++)
         if(!world.getBlockState(at.above(dy)).isAir()){roof=true;break;}
        if(!roof)continue;
        int value=world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,at);
        total++;minimum=Math.min(minimum,value);
        if(value<8){low8++;if(dark.size()<16)dark.add(x+","+y+","+z+"="+value);}
        if(value<10)low10++;
       }
      System.out.println("POSAD_DISTRICT_LIGHT_SURVEY schema="+PosadGeometry.SCHEMA_VERSION+" turn="+turns+" district="+slot+" level="+y+" samples="+total+" min="+(total==0?"NA":minimum)+" below8="+low8+" below10="+low10+" dark="+dark);
     }
    }
   }
   t.succeed();
  });
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void reservedAndCompletedCitiesSurviveDataRoundTrip(GameTestHelper t) {
  var data=org.slavicmyths.worldgen.ManualStructureRecords.load(new net.minecraft.nbt.CompoundTag());
  var marker=java.util.UUID.fromString("142b04f5-54d9-4e34-8eae-57538990e9e2");
  var origin=new net.minecraft.core.BlockPos(100,64,100);
  var box=new net.minecraft.world.level.levelgen.structure.BoundingBox(100,16,100,225,81,225);
  data.add(marker,"construction",0,origin,box);
  var snapshot=new net.minecraft.nbt.CompoundTag();
  data.save(snapshot,t.getLevel().registryAccess());
  var restored=org.slavicmyths.worldgen.ManualStructureRecords.load(snapshot);
  t.assertTrue(restored.overlapping(box).size()==1,"Incomplete city reservation was lost after NBT serialization");
  t.assertTrue(restored.overlapping(box).getFirst().family().equals("construction"),"Incomplete city must remain blocked after reload");
  restored.replace(marker,"nether_posad",0,origin,box);
  var complete=new net.minecraft.nbt.CompoundTag();
  restored.save(complete,t.getLevel().registryAccess());
  var again=org.slavicmyths.worldgen.ManualStructureRecords.load(complete);
  t.assertTrue(again.overlapping(box).size()==1,"Completed city was lost after NBT serialization");
  t.assertTrue(again.overlapping(box).getFirst().family().equals("nether_posad"),"Completed city family not restored on reload");
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void chestRewardsAreNotRearmed(GameTestHelper t) {
  var world=t.getLevel();
  var anchor=t.absolutePos(new net.minecraft.core.BlockPos(0,140,0));
  var at=anchor.offset(16,1,18);
  var cp=new net.minecraft.world.level.ChunkPos(at);
  world.getChunk(cp.x,cp.z);
  var piece=new PosadPiece(anchor,0);
  var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(at.getX(),at.getY(),at.getZ(),at.getX(),at.getY(),at.getZ());
  piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),world.random,clip,cp,anchor);
  t.assertTrue(world.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity,"Posad pantry chest must have a block entity");
  var chest=(net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)world.getBlockEntity(at);
  t.assertTrue(chest.getLootTable()!=null,"Pantry loot table must be armed exactly once");
  chest.getItem(0); // Emulates the first inventory read: vanilla rolls and consumes lazy loot.
  t.assertTrue(chest.getLootTable()==null,"Opening the chest must consume its lazy loot marker");
  chest.clearContent(); // All rewards claimed; a second piece pass must NOT refill them.
  piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),world.random,clip,cp,anchor);
  t.assertTrue(world.getBlockEntity(at)==chest,"Existing opened chest must not be replaced");
  t.assertTrue(chest.getLootTable()==null,"Repeated generation re-armed an already opened chest");
  for(int i=0;i<chest.getContainerSize();i++)t.assertTrue(chest.getItem(i).isEmpty(),"Repeated generation duplicated loot in slot "+i);
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void foundationAndBridgeSerialization(GameTestHelper t) {
  var decision=PosadTerrain.inspect((x,y,z)->y>=64?PosadTerrain.Material.AIR:
   (z>=136||y<40?PosadTerrain.Material.NATURAL:PosadTerrain.Material.LAVA),0,64,0,0);
  t.assertTrue(decision.accepted(),"Lava shore plan rejected");
  var piece=new PosadPiece(decision.plan());
  var context=net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(t.getLevel());
  var restored=new PosadPiece(piece.createTag(context));
  t.assertTrue(restored.getBoundingBox().equals(piece.getBoundingBox()),"Bounds changed after NBT");
  for(int z=0;z<=136;z++)for(int x=0;x<113;x++)for(int y=-48;y<18;y++)
   t.assertTrue(java.util.Objects.equals(piece.voxel(x,y,z),restored.voxel(x,y,z)),"Lost terrain geometry after NBT");
  t.assertTrue(piece.voxel(0,-20,0).is(Blocks.POLISHED_BASALT),"Missing deep support");
  for(int z=113;z<=136;z++)for(int x=53;x<=59;x++) {
   t.assertTrue(piece.voxel(x,0,z).is(Blocks.POLISHED_BLACKSTONE_BRICKS),"Bridge deck hole");
   t.assertTrue(piece.voxel(x,1,z).isAir(),"Blocked bridge");
  }
  t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=1200)
 public static void nativeTerrainSurveyFiveSeeds(GameTestHelper t) {
  var world=t.getLevel();var registry=world.registryAccess();
  var noise=registry.registryOrThrow(net.minecraft.core.registries.Registries.NOISE_SETTINGS).getHolderOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.NETHER);
  var preset=registry.registryOrThrow(net.minecraft.core.registries.Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);
  var generator=new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),noise);
  var biome=registry.registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(net.minecraft.world.level.biome.Biomes.NETHER_WASTES);
  var structure=new PosadStructure(new net.minecraft.world.level.levelgen.structure.Structure.StructureSettings(net.minecraft.core.HolderSet.direct(new java.util.ArrayList<>(generator.getBiomeSource().possibleBiomes()))));
  var placement=new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(80,32,net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR,22022026);
  int total=0;
  for(long seed:new long[]{11,29,101,2026,22022026}) {
   var random=net.minecraft.world.level.levelgen.RandomState.create(noise.value(),registry.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE),seed);
   int accepted=0;long start=System.nanoTime();
   for(int cell=0;cell<8;cell++) {
    var cp=placement.getPotentialStructureChunk(seed,cell*80,0);
    var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,world.getStructureManager(),seed,cp,world,structure.biomes()::contains);
    if(structure.findGenerationPoint(context).isPresent())accepted++;
   }
   total+=accepted;
   System.out.println("POSAD_NATIVE_SURVEY seed="+seed+" candidates=8 accepted="+accepted+" nanos="+(System.nanoTime()-start));
  }
  System.out.println("POSAD_NATIVE_SURVEY totalAccepted="+total+" candidates=40; NO PHYSICAL NETHER STARTS/FREQUENCY CLAIM");
  t.succeed();
 }


 @GameTest(template="empty",timeoutTicks=200)
 public static void towerLaddersEntrancesAndMasonry(GameTestHelper t) {
  int[][] towers={{0,0},{102,0},{0,102},{102,102},{42,102},{62,102}};
  for(int[] tower:towers) {
   int ox=tower[0],oz=tower[1];
   for(int y=1;y<=13;y++) {
    t.assertTrue(PosadShell.voxel(ox+2,y,oz+2).is(Blocks.LADDER),"Missing ladder "+ox+","+y+","+oz);
    t.assertTrue(!PosadShell.voxel(ox+2,y,oz+1).isAir(),"Unsupported ladder "+ox+","+y+","+oz);
   }
  }
  for(int y=1;y<=3;y++) {
   for(int z=0;z<113;z++) {
    t.assertTrue(!PosadShell.voxel(0,y,z).isAir(),"West exterior exit "+y+","+z);
    t.assertTrue(!PosadShell.voxel(112,y,z).isAir(),"East exterior exit "+y+","+z);
   }
   for(int x=0;x<113;x++) {
    t.assertTrue(!PosadShell.voxel(x,y,0).isAir(),"North exterior exit "+x+","+y);
    if(x<53||x>59)t.assertTrue(!PosadShell.voxel(x,y,112).isAir(),"South exterior exit "+x+","+y);
   }
  }
  // Inner entrances and connected high patrol openings must remain traversable.
  t.assertTrue(PosadShell.voxel(10,2,5).isAir(),"NW inner entry");
  t.assertTrue(PosadShell.voxel(102,2,5).isAir(),"NE inner entry");
  t.assertTrue(PosadShell.voxel(10,2,107).isAir(),"SW inner entry");
  t.assertTrue(PosadShell.voxel(102,2,107).isAir(),"SE inner entry");
  t.assertTrue(PosadShell.voxel(46,2,102).isAir(),"Left gate tower inner entry");
  t.assertTrue(PosadShell.voxel(66,2,102).isAir(),"Right gate tower inner entry");
  t.assertTrue(PosadShell.voxel(10,9,2).isAir(),"NW patrol link");
  t.assertTrue(PosadShell.voxel(102,9,2).isAir(),"NE patrol link");
  t.assertTrue(PosadShell.voxel(10,9,110).isAir(),"SW patrol link");
  t.assertTrue(PosadShell.voxel(102,9,110).isAir(),"SE patrol link");
  // Gate tower patrol doors open towards their connecting bridge, not outward.
  t.assertTrue(PosadShell.voxel(50,9,110).isAir(),"West gate bridge access");
  t.assertTrue(PosadShell.voxel(62,9,110).isAir(),"East gate bridge access");
  t.assertTrue(!PosadShell.voxel(42,9,110).isAir(),"West gate exterior breach");
  t.assertTrue(!PosadShell.voxel(70,9,110).isAir(),"East gate exterior breach");
  var roadMaterials=new java.util.HashSet<net.minecraft.world.level.block.Block>();
  var wallMaterials=new java.util.HashSet<net.minecraft.world.level.block.Block>();
  for(int z=12;z<=100;z++)roadMaterials.add(PosadShell.voxel(55,0,z).getBlock());
  for(int x=15;x<=90;x++)for(int y=1;y<=8;y++)wallMaterials.add(PosadShell.voxel(x,y,0).getBlock());
  t.assertTrue(roadMaterials.size()>=3,"Road palette lacks variety");
  t.assertTrue(wallMaterials.size()>=3,"Wall palette lacks variety");
  t.succeed();
 }


}
