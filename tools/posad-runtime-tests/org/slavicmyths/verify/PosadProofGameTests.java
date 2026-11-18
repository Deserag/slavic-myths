package org.slavicmyths.verify;
import java.util.UUID;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import org.slavicmyths.posad.*;
import org.slavicmyths.losthalls.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths_posad")
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadProofGameTests {
 private static UUID cancelDeath;
 @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
 public static void cancelFixtureDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event){
  if(event.getEntity().getUUID().equals(cancelDeath)){event.setCanceled(true);event.getEntity().setHealth(1);}
 }
 @GameTest(template="empty",timeoutTicks=100,batch="proofs")
 public static void cancelledDeathDoesNotIssueCertificate(GameTestHelper t){
  var w=t.getLevel();var p=PosadTestPlayers.create(w,"RevivalProofQA");p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(3,8,3)));w.addNewPlayer(p);
  var target=boss(w,"bauk",p);cancelDeath=target.getUUID();
  try{target.hurt(p.damageSources().playerAttack(p),10000);}finally{cancelDeath=null;}
  t.assertTrue(target.isAlive(),"Revival fixture failed to cancel death");
  t.assertTrue(BossRewardInbox.find(w.getServer(),"posad_test_proof/BAUK/"+target.getUUID()+"/"+p.getUUID())==null,"Cancelled death created a durable victory pin");
  t.assertTrue(count(p,PosadTestProofs.PROOF.get())==0,"Cancelled death gave certificate");p.discard();target.discard();t.succeed();
 }
 private static int count(ServerPlayer p,Item item){int count=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(item))count+=p.getInventory().getItem(i).getCount();return count;}
 private static LivingEntity boss(ServerLevel world,String id,ServerPlayer player){
  var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse("slavicmyths:"+id));
  var entity=(LivingEntity)type.create(world);entity.setPos(player.getX()+2,player.getY(),player.getZ());if(entity instanceof Mob mob)mob.setNoAi(true);return entity;
 }
 private static void kill(GameTestHelper t,LivingEntity entity,ServerPlayer player){
  for(int hit=0;entity.isAlive()&&hit<256;hit++){entity.invulnerableTime=0;entity.hurt(player.damageSources().playerAttack(player),10000);}
  t.assertTrue(!entity.isAlive(),"Proof fixture could not kill "+entity.getType());
 }
 private static PosadResidentEntity leader(ServerLevel world,ServerPlayer player){
  var npc=org.slavicmyths.registry.ModEntities.POSAD_RESIDENT.get().create(world);var member=PosadPopulationPlan.roster().getFirst();
  npc.configure(new PosadPopulationPlan.WorldMember(UUID.randomUUID(),member,player.getBlockX(),player.getBlockZ()),UUID.randomUUID(),player.blockPosition());npc.setPos(player.position());return npc;
 }
 @GameTest(template="empty",timeoutTicks=200,batch="proofs")
 public static void actualKillPendingInventoryAuthenticityAndGlobalExchange(GameTestHelper t){
  var w=t.getLevel();var winner=PosadTestPlayers.create(w,"ProofWinnerQA");winner.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);winner.setNoGravity(true);winner.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(3,8,3)));w.addNewPlayer(winner);
  var minor=PosadTestPlayers.create(w,"MinorHitQA");minor.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);minor.setNoGravity(true);minor.setPos(winner.position());w.addNewPlayer(minor);
  for(int i=0;i<36;i++)winner.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
  var target=boss(w,"cursed_mytar",winner);target.hurt(minor.damageSources().playerAttack(minor),1);kill(t,target,winner);
  String key="posad_test_proof/MYTAR/"+target.getUUID()+"/"+winner.getUUID();
  t.assertTrue(BossRewardInbox.find(w.getServer(),key)!=null,"Actual eligible kill did not pin its test proof");
  t.assertTrue(count(winner,PosadTestProofs.PROOF.get())==0,"Full inventory overflowed test proof");
  t.assertTrue(count(minor,PosadTestProofs.PROOF.get())==0,"Less than five percent damage earned proof");
  winner.getInventory().setItem(35,ItemStack.EMPTY);BossRewardInbox.reconcile(winner);
  t.assertTrue(count(winner,PosadTestProofs.PROOF.get())==1,"Pending proof not delivered when space became available");
  var proof=winner.getInventory().getItem(35).copy();winner.getInventory().setItem(35,ItemStack.EMPTY);
  var npc=leader(w,minor);minor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PosadTestProofs.PROOF.get()));
  npc.interact(minor,InteractionHand.MAIN_HAND);t.assertTrue(!minor.getMainHandItem().isEmpty()&&count(minor,PosadTestProofs.RECEIPT.get())==0,"Forged Creative certificate was redeemed");
  minor.setItemInHand(InteractionHand.MAIN_HAND,proof.copy());npc.interact(minor,InteractionHand.MAIN_HAND);
  t.assertTrue(count(minor,PosadTestProofs.RECEIPT.get())==1&&count(minor,PosadTestProofs.PROOF.get())==0,"Transferred genuine proof did not exchange once");
  winner.getInventory().setItem(0,proof.copy());var otherCity=leader(w,winner);otherCity.interact(winner,InteractionHand.MAIN_HAND);
  t.assertTrue(count(winner,PosadTestProofs.RECEIPT.get())==0,"Copied proof redeemed in another city");
  BossRewardInbox.reconcile(minor);t.assertTrue(count(minor,PosadTestProofs.RECEIPT.get())==1,"Inbox duplicated diagnostic receipt");
  winner.discard();minor.discard();npc.discard();otherCity.discard();target.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200,batch="proofs")
 public static void bothSistersRequiredForOneCertificate(GameTestHelper t){
  var w=t.getLevel();var p=PosadTestPlayers.create(w,"PairProofQA");p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(3,8,3)));w.addNewPlayer(p);
  var g=(org.slavicmyths.sisters.SisterBoss)boss(w,"gneteya",p);var s=(org.slavicmyths.sisters.SisterBoss)boss(w,"tryaseya",p);var encounter=UUID.randomUUID();g.link(encounter,s.getUUID());s.link(encounter,g.getUUID());
  kill(t,g,p);t.assertTrue(count(p,PosadTestProofs.PROOF.get())==0,"One sister counted as a completed pair");
  kill(t,s,p);t.assertTrue(count(p,PosadTestProofs.PROOF.get())==1,"Both legitimate sister kills did not yield one pair proof");
  PosadTestProofs.death(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(s,p.damageSources().playerAttack(p),new java.util.ArrayList<>(),true));
  t.assertTrue(count(p,PosadTestProofs.PROOF.get())==1,"Repeated death callback duplicated pair proof");
  p.discard();g.discard();s.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200,batch="encounters")
 public static void allDiagnosticEncountersRealNpcSearchAndSafeRejection(GameTestHelper t){
  var w=t.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);var at=new net.minecraft.core.BlockPos(9000,181,9000);
  for(int x=-40;x<=40;x++)for(int z=-40;z<=40;z++){var floor=at.offset(x,-1,z);w.getChunk(floor);w.setBlock(floor,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);}
  // The mod's actual Nether roof extends above vanilla Y128. This disposable
  // fixture must explicitly prepare enough headroom for the 5.5-block Gneteya.
  for(var cell:net.minecraft.core.BlockPos.betweenClosed(at.offset(-40,0,-40),at.offset(40,9,40)))if(!w.getBlockState(cell).isAir())w.setBlock(cell,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
  var p=PosadTestPlayers.create(w,"BossSearchQA");p.setNoGravity(true);p.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);w.addNewPlayer(p);
  var npc=leader(w,p);w.addFreshEntity(npc);
  var source=w.getServer().createCommandSourceStack().withLevel(w).withPosition(p.position()).withPermission(2);
  for(var kind:PosadTestProofs.Kind.values()){
   t.assertTrue(PosadDiagnosticEncounters.create(source.withPermission(0),kind)==0,"Non-OP created diagnostic encounter");
   int[] made={-1};w.getServer().getCommands().performPrefixedCommand(source.withCallback((ok,n)->made[0]=ok?n:0),"slavicmyths dev posad encounter "+kind.name().toLowerCase(java.util.Locale.ROOT));
   t.assertTrue(made[0]==1,"Actual encounter command failed: "+kind);
   var actors=w.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(at).inflate(64),e->e.getPersistentData().getBoolean("PosadTestDiagnostic"));
   t.assertTrue(actors.size()==(kind==PosadTestProofs.Kind.FEVER_SISTERS||kind==PosadTestProofs.Kind.ELEMENT_SISTERS?2:1),"Wrong real actor count: "+kind);
   if(actors.size()==2)t.assertTrue(actors.get(0).getPersistentData().getUUID("PosadTestEncounter").equals(actors.get(1).getPersistentData().getUUID("PosadTestEncounter")),"Pair encounter IDs differ");
   int chunks=w.getChunkSource().getLoadedChunksCount();int[] found={-1};
   w.getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack().withPermission(0).withSuppressedOutput().withCallback((ok,n)->found[0]=ok?n:0),"slavicmyths hero posad find "+kind.name().toLowerCase(java.util.Locale.ROOT));
   t.assertTrue(found[0]==1,"Survival-level real NPC search failed: "+kind);
   t.assertTrue(w.getChunkSource().getLoadedChunksCount()==chunks,"NPC search loaded chunks");
   t.assertTrue(PosadDiagnosticEncounters.clear(source)==actors.size(),"Diagnostic cleanup missed actors");
   t.assertTrue(PosadBossSearch.get(w).nearest(w,at,kind).isEmpty(),"Removed diagnostic boss left a false live trace");
  }
  for(var cell:net.minecraft.core.BlockPos.betweenClosed(at.offset(-40,-4,-40),at.offset(40,-1,40)))w.setBlock(cell,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
  var chestAt=at.offset(12,0,0);w.setBlock(chestAt,net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(),2);
  var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)w.getBlockEntity(chestAt);chest.setItem(0,new ItemStack(Items.DIAMOND,2));
  t.assertTrue(PosadDiagnosticEncounters.create(source,PosadTestProofs.Kind.ELEMENT_SISTERS)==0,"Unsupported site accepted");
  t.assertTrue(chest.getItem(0).getCount()==2,"Rejected encounter damaged foreign inventory");p.discard();npc.discard();t.succeed();
 }

}
