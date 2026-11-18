package org.slavicmyths.chud;

import java.util.*;
import java.lang.reflect.Proxy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.worldgen.ManualStructureRecords;
import org.slavicmyths.yaga.YagaPlacement;
import org.slavicmyths.serpenthill.*;

/** Plain JVM, native data structures only. Never constructs a world, client or server. */
public final class Performance274Offline {
 static int checks;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static Map<BlockPos,BlockState> raster(List<ChudGeometry.Fill> fills,BoundingBox clip){var result=new HashMap<BlockPos,BlockState>();ChudGeometry.rasterize(fills,clip,result::put);return result;}
 static void columns(){
  var fills=new ArrayList<ChudGeometry.Fill>();var random=new Random(274);
  for(int i=0;i<240;i++){int x=random.nextInt(128)-64,y=random.nextInt(48)-32,z=random.nextInt(128)-64;fills.add(new ChudGeometry.Fill(new BoundingBox(x,y,z,x+random.nextInt(24),y+random.nextInt(24),z+random.nextInt(24)),(i%3==0?Blocks.AIR:i%3==1?Blocks.STONE:Blocks.DIRT).defaultBlockState()));}
  var index=new ChudGeometry.ColumnIndex(fills,()->false);long before=0,after=0;
  for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=-2;y<=2;y++){
   var clip=new BoundingBox(x*16,y*16,z*16,x*16+15,y*16+17,z*16+15);var subset=index.at(x,z);
   check(raster(fills,clip).equals(raster(subset,clip)),"negative coordinates, overwrite order, 18-high lookahead");before+=fills.size();after+=subset.size();
  }
  check(after<before,"candidate checks reduced");System.out.println("COLUMN_CANDIDATES before="+before+" after="+after+"; same 405 clip maps, not gameplay timing");
 }
 static void cancellation(){
  boolean[] cancel={true};boolean rejected=false;
  try{new ChudGeometry.ColumnIndex(List.of(new ChudGeometry.Fill(new BoundingBox(0,0,0,16,16,16),Blocks.STONE.defaultBlockState())),()->cancel[0]);}catch(java.util.concurrent.CancellationException expected){rejected=true;}
  check(rejected,"cancel before index");rejected=false;
  try{ChudCreationJobs.raster(List.of(),null,new BoundingBox(0,0,0,16,16,16),()->true);}catch(java.util.concurrent.CancellationException expected){rejected=true;}
  check(rejected,"cancel before next section, no decor/world needed");
 }
 static void actualGeometry(){
  for(int rotation:new int[]{0,1}){
   var plan=new ChudPlan(274,new BlockPos(-31,0,-17),0,rotation,64);var fills=ChudGeometry.build(plan);var decor=new ChudDecor(plan,null);
   var box=new BoundingBox(-32,0,-32,-1,31,-1);var indexed=ChudCreationJobs.raster(fills,decor,box);int cursor=0;
   for(int x=-2;x<=-1;x++)for(int z=-2;z<=-1;z++)for(int y=0;y<=1;y++){
    var origin=new BlockPos(x*16,y*16,z*16);var clip=new BoundingBox(x*16,y*16,z*16,x*16+15,Math.min(y*16+17,31),z*16+15);var states=new BlockState[18*256];
    ChudGeometry.rasterize(fills,clip,(p,s)->states[(p.getY()-origin.getY())*256+(p.getZ()&15)*16+(p.getX()&15)]=s);boolean used=false;
    for(int i=0;i<4096;i++){var state=states[i];if(state==null)continue;used=true;int px=origin.getX()+(i&15),pz=origin.getZ()+((i>>4)&15);boolean open=states[i+256]!=null&&states[i+256].isAir()&&states[i+512]!=null&&states[i+512].isAir();states[i]=decor.surface(state,px,origin.getY()+(i>>8),pz,decor.zone(px,pz),open);}
    if(used){check(cursor<indexed.size(),"actual sections present");var section=indexed.get(cursor++);check(section.origin().equals(origin)&&Arrays.equals(section.states(),Arrays.copyOf(states,4096)),"production city geometry and decor match old raster");}
   }
   check(cursor==indexed.size(),"no extra actual sections");
  }
 }
 static void records()throws Exception{
  var data=new ManualStructureRecords();var ids=new ArrayList<UUID>();
  for(int i=0;i<40;i++){var id=new UUID(274,i);ids.add(id);data.add(id,"construction",0,BlockPos.ZERO,new BoundingBox(i*32-640,-32,-17,i*32-610,80,17));}
  for(int turn=0;turn<80;turn++){
   var id=ids.get(turn%40);var box=new BoundingBox(turn*5-200,-16,-32,turn*5-180,90,32);
   if(turn%3==0)data.remove(id);else data.replace(id,"site/bayun",0,BlockPos.ZERO,box);
   for(int x=-44;x<44;x++){
    var query=new BoundingBox(x*16,-64,-16,x*16+15,128,16);var expected=new HashSet<UUID>();for(var e:data.entries())if(e.bounds().intersects(query))expected.add(e.id());
    check(new HashSet<>(data.overlapping(query).stream().map(ManualStructureRecords.Entry::id).toList()).equals(expected),"incremental index equals full scan");
   }
  }
  var restored=ManualStructureRecords.load(data.save(new net.minecraft.nbt.CompoundTag(),null));
  check(restored.entries().size()==data.entries().size(),"NBT preserves entries");
  for(var id:ids)data.remove(id);
  var field=ManualStructureRecords.class.getDeclaredField("byChunk");field.setAccessible(true);check(((Map<?,?>)field.get(data)).isEmpty(),"empty buckets removed");
 }
 static void rollback(){
  var blocks=new HashMap<BlockPos,BlockState>();int[] reads={0};
  LevelAccessor world=(LevelAccessor)Proxy.newProxyInstance(LevelAccessor.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},(p,m,a)->switch(m.getName()){
   case "getBlockState"->{reads[0]++;yield blocks.getOrDefault(a[0],Blocks.AIR.defaultBlockState());}
   case "setBlock"->{blocks.put(((BlockPos)a[0]).immutable(),(BlockState)a[1]);yield true;}
   default->throw new UnsupportedOperationException(m.getName());
  });
  for(int writes:new int[]{0,1,7}){
   blocks.clear();reads[0]=0;var site=new YagaPlacement.Site();site.anchor=BlockPos.ZERO;
   for(int i=0;i<1000;i++){var p=new BlockPos(i,10,0);site.old.put(p,Blocks.AIR.defaultBlockState());site.desired.put(p,Blocks.STONE.defaultBlockState());}
   var cursor=new YagaPlacement.Cursor(world,site,()->{throw new AssertionError("spawn during cancelled transaction");},()->{},r->{});
   cursor.step(writes,1_000_000_000L);cursor.abort();int[] own={0};blocks.forEach((p,s)->{if(!s.isAir())own[0]++;});check(own[0]==writes,"partial prefix written");
   cursor.step(writes+1,1_000_000_000L);check(cursor.done()&&!cursor.result.ok,"rollback proportional to written prefix");check(blocks.values().stream().allMatch(BlockState::isAir),"actual old states restored");check(reads[0]<=writes*4,"untouched suffix never read on rollback");
  }
 }
 static void hill(){
  for(long seed:new long[]{274,-274,2432026}){var plan=new HillPlan(seed);
   for(int x=0;x<128;x+=3)for(int z=0;z<128;z+=3){int top=HillRules.hillTop(x,z,seed);boolean vein=Math.abs(x-64-22*Math.sin(z*.075+seed%31))<1.2||Math.abs(z-64-24*Math.sin(x*.085+seed%17))<1.1;String skin=vein?"slavicmyths:mor_stone_bricks":Math.floorMod(x*13+z*19,11)<8?"minecraft:cobbled_deepslate":"minecraft:coarse_dirt";
    for(int y:new int[]{-1,0,top-3,top-2,top,top+1,12,40,73,80,81,94,128,143,144}){String expected="minecraft:air";if(y>=0&&y<144){expected=y<=top?y>=top-2?skin:"minecraft:deepslate":"minecraft:air";for(var patch:plan.patches)if(x>=patch.x0()&&x<=patch.x1()&&z>=patch.z0()&&z<=patch.z1()&&y>=patch.y0()&&y<=patch.y1())expected=patch.state();}check(plan.state(x,y,z).equals(expected),"cached column equals original formula/patch precedence");}
   }
  }
 }
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  columns();actualGeometry();cancellation();records();rollback();hill();System.out.println("PASS 6 offline suites / "+checks+" checks; GAMEPLAY_NOT_RUN, zero worlds/client/server");
 }
}
