package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.posad.*;
import net.minecraft.world.level.block.Blocks;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadCommandGameTests {
 @GameTest(template="empty",timeoutTicks=400)
 public static void actualBuildingCommandsBothDimensions(GameTestHelper t) {
  var server=t.getLevel().getServer();
  for(var dimension:java.util.List.of(net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER)) {
   var w=server.getLevel(dimension);int index=0;
   for(String id:new String[]{"veche_hall_2_2_2","communal_house_2_2_3","druzhinny_dvor_2_2_4","kuznechny_dvor_2_2_5","zeleynaya_2_2_7","granary_2_2_8"}) {
    var at=new net.minecraft.core.BlockPos(index++*48,180,800);
    for(int cx=at.getX()>>4;cx<=(at.getX()+32)>>4;cx++)for(int cz=at.getZ()>>4;cz<=(at.getZ()+32)>>4;cz++){w.getChunk(cx,cz);w.setChunkForced(cx,cz,true);}
    for(var p:net.minecraft.core.BlockPos.betweenClosed(at,at.offset(31,24,31)))w.setBlock(p,Blocks.AIR.defaultBlockState(),2);
    for(int x=at.getX();x<=at.getX()+31;x++)for(int z=at.getZ();z<=at.getZ()+31;z++)w.setBlock(new net.minecraft.core.BlockPos(x,179,z),Blocks.STONE.defaultBlockState(),2);
    if(id.equals("veche_hall_2_2_2"))for(var p:net.minecraft.core.BlockPos.betweenClosed(at,at.offset(28,17,22)))w.setBlock(p,Blocks.STONE.defaultBlockState(),2);
    var source=server.createCommandSourceStack().withLevel(w).withPermission(2).withPosition(net.minecraft.world.phys.Vec3.atLowerCornerOf(at)).withSuppressedOutput();
    int[] result={-1};server.getCommands().performPrefixedCommand(source.withCallback((ok,n)->result[0]=ok?n:0),"slavicmyths dev posad building "+id+" ~ ~ ~");
    t.assertTrue(result[0]>0,"Actual command failed "+dimension+" "+id+" result="+result[0]);
    int count=0;for(var p:net.minecraft.core.BlockPos.betweenClosed(at,at.offset(31,24,31)))if(!w.getBlockState(p).isAir())count++;
    t.assertTrue(count>100,"Command reported success without physical building: "+id+" blocks="+count);
    if(id.equals("veche_hall_2_2_2"))t.assertTrue(count==3598,"Natural stone left inside actual hall: "+count);
    int repeat=PosadCommands.building(source,id,at);t.assertTrue(repeat==0,"Repeated building overwrote existing building");
    System.out.println("POSAD_COMMAND_PASS dimension="+dimension.location()+" id="+id+" physical_blocks="+count+" repeatRejected=true world="+server.getWorldData().getLevelName());
   }
  }
  t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=1200)
 public static void actualCityCommandOrdinaryWorldNamesAndOpTwo(GameTestHelper t) {
  var server=t.getLevel().getServer();
  t.assertTrue(!server.getWorldData().getLevelName().startsWith("posad-qa-"),"Regression must use ordinary world name");
  for(var dimension:java.util.List.of(net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER)) {
   var w=server.getLevel(dimension);var at=new net.minecraft.core.BlockPos(3000,180,2400);
   for(int x=3000;x<=3112;x++)for(int z=2400;z<=2515;z++)for(int y=177;y<=179;y++)w.setBlock(new net.minecraft.core.BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),2);
   for(var clear:net.minecraft.core.BlockPos.betweenClosed(at,at.offset(112,18,115)))w.setBlock(clear,Blocks.AIR.defaultBlockState(),2);
   var source=server.createCommandSourceStack().withLevel(w).withPermission(2).withPosition(net.minecraft.world.phys.Vec3.atLowerCornerOf(at)).withSuppressedOutput();
   int[] result={-1};server.getCommands().performPrefixedCommand(source.withCallback((ok,n)->result[0]=ok?n:0),"slavicmyths dev posad create 0 ~ ~ ~");
   t.assertTrue(result[0]>0,"Actual create command rejected ordinary world/OP2: "+dimension);
  }
  t.succeedWhen(()->{
   for(var dimension:java.util.List.of(net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER)) {
    var w=server.getLevel(dimension);
    t.assertTrue(!org.slavicmyths.worldgen.ManualStructureRecords.get(w).overlapping(new net.minecraft.world.level.levelgen.structure.BoundingBox(3000,180,2400,3112,197,2512)).isEmpty(),"City not yet reserved "+dimension);
    var entries=org.slavicmyths.worldgen.ManualStructureRecords.get(w).overlapping(new net.minecraft.world.level.levelgen.structure.BoundingBox(3000,180,2400,3112,197,2512));
    t.assertTrue(entries.stream().anyMatch(e->e.family().equals("nether_posad")),"City not yet finished "+dimension);
    t.assertTrue(!w.getBlockState(new net.minecraft.core.BlockPos(3056,180,2456)).isAir(),"No physical city platform");
   }
   System.out.println("POSAD_CITY_COMMAND_PASS OP2 ordinary world name both dimensions, actual relative-coordinate dispatcher, physical blocks and completed durable records");
  });
 }

 @GameTest(template="empty",timeoutTicks=200)
 public static void permissionsForeignChestAndCancelBeforeWrite(GameTestHelper t) {
  var w=t.getLevel();var at=t.absolutePos(new net.minecraft.core.BlockPos(0,100,64));
  for(int cx=at.getX()>>4;cx<=(at.getX()+32)>>4;cx++)for(int cz=at.getZ()>>4;cz<=(at.getZ()+32)>>4;cz++){w.getChunk(cx,cz);w.setChunkForced(cx,cz,true);}
  var source=w.getServer().createCommandSourceStack().withLevel(w).withPermission(2).withPosition(net.minecraft.world.phys.Vec3.atLowerCornerOf(at)).withSuppressedOutput();
  t.assertTrue(PosadCommands.building(source.withPermission(1),"veche_hall_2_2_2",at)==0,"Non-OP placement allowed");
  w.setBlock(at,Blocks.CHEST.defaultBlockState(),2);
  var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)w.getBlockEntity(at);chest.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,3));
  t.assertTrue(PosadCommands.building(source,"veche_hall_2_2_2",at)==0,"Foreign chest overwritten");
  t.assertTrue(chest.getItem(0).getCount()==3&&w.getBlockEntity(at)==chest,"Foreign chest changed");
  t.assertTrue(org.slavicmyths.netherstructures.ManualNetherBuildings.place(source.withPermission(1),"wayfarer_camp",at)==0,"Non-OP Nether building allowed");
  t.assertTrue(org.slavicmyths.netherstructures.ManualNetherBuildings.place(source,"wayfarer_camp",at)==0,"Manual Nether building overwrote chest");
  t.assertTrue(PosadManualJobs.create(source.withPermission(1),0,at.getX(),at.getY(),at.getZ())==0,"Non-OP city allowed");
  var cancelSource=source.withLevel(w.getServer().getLevel(net.minecraft.world.level.Level.END));
  t.assertTrue(PosadManualJobs.create(cancelSource,0,at.getX(),80,at.getZ())==1,"City start rejected");
  t.assertTrue(PosadManualJobs.status(cancelSource,true)==1,"Cancel did not stop job");
  t.assertTrue(PosadManualJobs.status(cancelSource,false)==0,"Cancelled job remained active");
  t.assertTrue(chest.getItem(0).getCount()==3,"Cancel wrote blocks");
  System.out.println("POSAD_COMMAND_NEGATIVE_PASS OP1 rejected; foreign chest preserved with3diamonds; immediate cancel zero writes/no active job");t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=400)
 public static void sixNetherManualCommandsBothDimensions(GameTestHelper t){
  var server=t.getLevel().getServer();
  for(var dim:java.util.List.of(net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER)){
   var w=server.getLevel(dim);int i=0;
   for(String id:org.slavicmyths.netherstructures.ManualNetherBuildings.IDS){
    var at=new net.minecraft.core.BlockPos(i++*48,190,1200);
    for(int cx=(at.getX()-8)>>4;cx<=(at.getX()+32)>>4;cx++)for(int cz=(at.getZ()-8)>>4;cz<=(at.getZ()+32)>>4;cz++){w.getChunk(cx,cz);w.setChunkForced(cx,cz,true);}
    for(var p:net.minecraft.core.BlockPos.betweenClosed(at.offset(-8,-6,-8),at.offset(31,20,31)))w.setBlock(p,Blocks.AIR.defaultBlockState(),2);
    for(int x=at.getX()-8;x<=at.getX()+31;x++)for(int z=at.getZ()-8;z<=at.getZ()+31;z++)w.setBlock(new net.minecraft.core.BlockPos(x,at.getY()-7,z),Blocks.STONE.defaultBlockState(),2);
    var src=server.createCommandSourceStack().withLevel(w).withPermission(2).withPosition(net.minecraft.world.phys.Vec3.atLowerCornerOf(at)).withSuppressedOutput();
    int[] result={0};server.getCommands().performPrefixedCommand(src.withCallback((ok,n)->result[0]=ok?n:0),"slavicmyths dev nether create "+id+" ~ ~ ~");
    t.assertTrue(result[0]>0,"Manual Nether command failed "+id+" "+dim);
    int count=0,chests=0;for(var p:net.minecraft.core.BlockPos.betweenClosed(at.offset(-8,-6,-8),at.offset(31,20,31))){if(!w.getBlockState(p).isAir())count++;if(w.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity chest){chests++;t.assertTrue(chest.getLootTable()!=null,"Missing lazy loot "+id);}}
    t.assertTrue(count>20,"No physical structure "+id);t.assertTrue(chests==(id.equals("broken_nether_bridge")?0:1),"Unexpected chest count "+id+":"+chests);
    t.assertTrue(org.slavicmyths.netherstructures.ManualNetherBuildings.place(src,id,at)==0,"Duplicate manual structure allowed");
    System.out.println("NETHER_MANUAL_COMMAND_PASS "+dim.location()+" "+id+" blocks="+count+" chests="+chests+" repeatRejected");
   }
  }t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=600)
 public static void canonicalGenerateAdaptsSnowTreesAndUnevenGround(GameTestHelper t){
  var w=t.getLevel();
  for(int layout=0;layout<5;layout++)for(int type=0;type<2;type++){
   int x=6000+layout*160+type*64,z=4000;
   for(int cx=(x-8)>>4;cx<=(x+32)>>4;cx++)for(int cz=(z-8)>>4;cz<=(z+32)>>4;cz++){w.getChunk(cx,cz);w.setChunkForced(cx,cz,true);}
   for(int dx=-8;dx<=31;dx++)for(int dz=-8;dz<=31;dz++){
    int floor=99-(layout>=3?Math.floorMod(dx+dz,4):0);
    for(int y=94;y<=120;y++)w.setBlock(new net.minecraft.core.BlockPos(x+dx,y,z+dz),(y<=floor?Blocks.STONE:Blocks.AIR).defaultBlockState(),2);
    if(layout>=1)w.setBlock(new net.minecraft.core.BlockPos(x+dx,floor+1,z+dz),Blocks.SNOW.defaultBlockState(),2);
   }
   if(layout>=2){for(int y=100;y<=108;y++)w.setBlock(new net.minecraft.core.BlockPos(x,y,z),Blocks.SPRUCE_LOG.defaultBlockState(),2);for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)w.setBlock(new net.minecraft.core.BlockPos(x+dx,109,z+dz),Blocks.SPRUCE_LEAVES.defaultBlockState(),2);}
   var source=w.getServer().createCommandSourceStack().withLevel(w).withPermission(2).withPosition(new net.minecraft.world.phys.Vec3(x+.5,110,z+.5)).withSuppressedOutput();
   String id=type==0?"nether_watchpost":"veche_hall";int[] result={0};
   w.getServer().getCommands().performPrefixedCommand(source.withCallback((ok,n)->result[0]=ok?n:0),"slavicmyths dev generate "+id);
   t.assertTrue(result[0]>0,"Canonical generate failed snow/tree/slope case="+layout+" id="+id);
   var entries=org.slavicmyths.worldgen.ManualStructureRecords.get(w).overlapping(new net.minecraft.world.level.levelgen.structure.BoundingBox(x-5,90,z-5,x+31,120,z+31));
   t.assertTrue(!entries.isEmpty(),"No persisted generated building");
   t.assertTrue(entries.get(0).arrival().getY()<110,"Used canopy/player height instead of actual soil");
   System.out.println("CANONICAL_TERRAIN_PASS layout="+layout+" id="+id+" ground="+entries.get(0).arrival());
  }t.succeed();
 }

 @GameTest(batch="flatCity",template="empty",timeoutTicks=1800)
 public static void canonicalCityInActualSuperflatWithSnowAndTrees(GameTestHelper t){
  var w=t.getLevel();int x=9000,z=7000;w.getChunk(x>>4,z>>4);
  int ground=org.slavicmyths.worldgen.LandTerrain.ground(w,x,z);
  t.assertTrue(ground==w.getMinBuildHeight()+3,"This regression must use actual default superflat ground: "+ground);
  int ax=x-56,az=z+12;
  w.setBlock(new net.minecraft.core.BlockPos(ax+3,ground+1,az+3),Blocks.SNOW.defaultBlockState(),2);
  for(int dy=1;dy<=7;dy++)w.setBlock(new net.minecraft.core.BlockPos(ax+6,ground+dy,az+6),Blocks.SPRUCE_LOG.defaultBlockState(),2);
  for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)w.setBlock(new net.minecraft.core.BlockPos(ax+6+dx,ground+8,az+6+dz),Blocks.SPRUCE_LEAVES.defaultBlockState(),2);
  var source=w.getServer().createCommandSourceStack().withLevel(w).withPermission(2).withPosition(new net.minecraft.world.phys.Vec3(x+.5,ground+1,z+.5)).withSuppressedOutput();int[] result={0};
  w.getServer().getCommands().performPrefixedCommand(source.withCallback((ok,n)->result[0]=ok?n:0),"slavicmyths dev generate nether_posad");
  t.assertTrue(result[0]>0,"Canonical city rejected actual superflat world");
  t.succeedWhen(()->{
   var entries=org.slavicmyths.worldgen.ManualStructureRecords.get(w).overlapping(new net.minecraft.world.level.levelgen.structure.BoundingBox(ax,ground,az,ax+112,ground+17,az+112));
   t.assertTrue(entries.stream().anyMatch(e->e.family().equals("nether_posad")),"Flat city not yet complete");
   t.assertTrue(w.getBlockState(new net.minecraft.core.BlockPos(ax,ground,az)).is(Blocks.BLACKSTONE),"Flat platform missing");
   t.assertTrue(w.getBlockState(new net.minecraft.core.BlockPos(ax+56,ground+3,az+110)).isAir(),"Flat gate blocked");
   var replaced=w.getBlockState(new net.minecraft.core.BlockPos(ax+6,ground+8,az+6));
   t.assertTrue(!replaced.is(net.minecraft.tags.BlockTags.LEAVES)&&!replaced.is(net.minecraft.tags.BlockTags.LOGS)&&!replaced.is(Blocks.SNOW),"Canopy left inside city");
   System.out.println("CANONICAL_SUPERFLAT_CITY_PASS actual_ground="+ground+" min="+w.getMinBuildHeight()+" snow/tree removed; physical platform/gate; durable completion");
  });
 }

 @GameTest(template="empty",timeoutTicks=200)
 public static void bothBossesAndEggsPresentInPackagedMod(GameTestHelper t){
  var path=net.neoforged.fml.ModList.get().getModFileById("slavicmyths").getFile().getFilePath();
  t.assertTrue(path.toString().endsWith(".jar"),"Must load production JAR, not loose classes");
  int mobs=org.slavicmyths.registry.CreativeCatalog.TABS.indexOf("mobs");
  var stacks=org.slavicmyths.registry.ModItemGroup.stacks(mobs);
  for(boolean morova:new boolean[]{true,false}){
   var type=morova?org.slavicmyths.registry.ModEntities.MOROVA_DEVA.get():org.slavicmyths.registry.ModEntities.TRIZNA_PRINCE.get();
   var boss=type.create(t.getLevel());t.assertTrue(boss!=null&&boss.getMaxHealth()==(morova?1350:1800),"Boss registry/HP missing");
   String id=morova?"morova_deva_spawn_egg":"trizna_prince_spawn_egg";
   var egg=org.slavicmyths.netherboss.NetherBossItems.item(id);
   t.assertTrue(stacks.stream().anyMatch(stack->stack.is(egg)),"Boss egg absent from actual Creative stack provider: "+id);
   System.out.println("PACKAGED_BOSS_PASS id="+net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type)+" HP="+boss.getMaxHealth()+" creativeEgg="+id+" jar="+path);
  }t.succeed();
 }
}
