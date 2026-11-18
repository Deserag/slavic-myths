package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.SiteChunks;
@GameTestHolder("slavicmyths_generation") @PrefixGameTestTemplate(false)
public final class GenerationTests {
 @GameTest(template="empty",timeoutTicks=600) public static void unloaded_site_chunk(GameTestHelper t){
  var world=t.getLevel();var pos=new ChunkPos(96,96);var chunks=new SiteChunks(world);long start=System.nanoTime();
  t.onEachTick(()->{try{if(chunks.ready(pos)){chunks.close();System.out.println("GENERATION_RUNTIME site_ready_ms="+(System.nanoTime()-start)/1_000_000);t.succeed();}}catch(RuntimeException e){chunks.close();throw e;}});
 }
 @GameTest(template="empty",timeoutTicks=100) public static void sanctuary_fire_restoration(GameTestHelper t)throws Exception{
  var w=t.getLevel();var at=t.absolutePos(new net.minecraft.core.BlockPos(3,2,3));var block=org.slavicmyths.sanctuary.SanctuaryContent.block("unfading_brazier");w.setBlockAndUpdate(at,block.defaultBlockState());var heart=(org.slavicmyths.sanctuary.SanctuaryHeart)w.getBlockEntity(at);var id=java.util.UUID.randomUUID();heart.configure(id,at.offset(-40,-2,-51),net.minecraft.world.level.block.Rotation.NONE,false,-1);
  var p=new net.minecraft.server.level.ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"RitualQA"),net.minecraft.server.level.ClientInformation.createDefault());p.connection=new NormalWorldProbe.Listener(p);p.moveTo(at.getX(),at.getY()+1,at.getZ());p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(org.slavicmyths.sanctuary.SanctuaryContent.item("sacred_ember"),2));
  heart.interact(p,net.minecraft.world.InteractionHand.MAIN_HAND);t.assertTrue(org.slavicmyths.sanctuary.SanctuaryRecords.get(w).restored(id),"fire not restored");t.assertTrue(p.getMainHandItem().getCount()==1,"ember must be consumed once");t.assertTrue(w.getBlockState(at).getValue(org.slavicmyths.sanctuary.SanctuaryBlock.LIT),"brazier not lit");
  heart.interact(p,net.minecraft.world.InteractionHand.MAIN_HAND);t.assertTrue(p.getMainHandItem().getCount()==1,"repeat restoration consumed an ember");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=100) public static void yaga_terrace_accepts_sand_but_protects_buildings(GameTestHelper t){
  var w=t.getLevel();var center=t.absolutePos(new net.minecraft.core.BlockPos(8,30,8));
  for(int x=-10;x<=10;x++)for(int z=-17;z<=10;z++)for(int y=-2;y<=0;y++)w.setBlock(center.offset(x,y,z),net.minecraft.world.level.block.Blocks.SANDSTONE.defaultBlockState(),2);
  var plan=new org.slavicmyths.yaga.YagaHutPlan();
  var site=org.slavicmyths.yaga.YagaPlacement.prepareForCreation(w,center,plan,id->net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState());
  t.assertTrue(site.failure==null,"Natural sandstone terrace refused: "+(site.failure==null?"":site.failure.reason));
  var occupied=site.anchor.offset(plan.blocks.keySet().iterator().next());
  w.setBlock(occupied,net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState(),2);
  var protectedSite=org.slavicmyths.yaga.YagaPlacement.prepareForCreation(w,center,plan,id->net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState());
  t.assertTrue(protectedSite.failure!=null,"Player building material must not be overwritten");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=1200) public static void cancel_pending_site_then_build_another(GameTestHelper t){
  var w=t.getLevel();var abandoned=new SiteChunks(w);abandoned.ready(new ChunkPos(192,-192));abandoned.close();
  var replacement=new SiteChunks(w);long start=System.nanoTime();
  t.onEachTick(()->{try{if(replacement.ready(new ChunkPos(193,-192))){replacement.close();System.out.println("GENERATION_RUNTIME cancelled_then_replaced_ms="+(System.nanoTime()-start)/1_000_000);t.succeed();}}catch(RuntimeException error){replacement.close();throw error;}});
 }
 @GameTest(template="empty",timeoutTicks=1200) public static void overlapping_jobs_keep_their_own_tickets(GameTestHelper t){
  var w=t.getLevel();var pos=new ChunkPos(224,224);var first=new SiteChunks(w);var second=new SiteChunks(w);long[] until={-1};
  t.onEachTick(()->{try{
   if(until[0]<0){if(!first.ready(pos))return;t.assertTrue(second.ready(pos),"Second job could not retain loaded site");first.close();until[0]=w.getGameTime()+100;}
   else if(w.getGameTime()>=until[0]){t.assertTrue(w.getChunkSource().getChunkNow(pos.x,pos.z)!=null,"Closing one job unloaded the other job's site");second.close();t.succeed();}
  }catch(RuntimeException error){first.close();second.close();throw error;}});
 }
}
