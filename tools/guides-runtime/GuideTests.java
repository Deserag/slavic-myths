package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.flight.*;
import org.slavicmyths.guides.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.tales.*;
import org.slavicmyths.yaga.*;
@GameTestHolder("slavicmyths_guides") @PrefixGameTestTemplate(false)
public final class GuideTests {
 private static final class Connection extends net.minecraft.network.Connection {
  final io.netty.channel.embedded.EmbeddedChannel c=new io.netty.channel.embedded.EmbeddedChannel();Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return c;}
 }
 private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
  Listener(ServerPlayer p){super(p.server,new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
 }
 @SuppressWarnings("unchecked") static ServerPlayer player(GameTestHelper t)throws Exception{
  var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"GuideQA"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.CREATIVE);p.moveTo(t.absoluteVec(new Vec3(3,3,3)));p.setNoGravity(true);t.getLevel().addNewPlayer(p);
  var field=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");field.setAccessible(true);((Map<UUID,ServerPlayer>)field.get(p.server.getPlayerList())).put(p.getUUID(),p);return p;
 }
 static void check(GameTestHelper t,boolean ok,String reason){t.assertTrue(ok,reason);System.out.println("GUIDE_QA_PASS "+reason);}
 static FlyingVessel vessel(GameTestHelper t,ServerPlayer p,boolean mortar){var v=(mortar?ModEntities.FLYING_MORTAR.get():ModEntities.FLYING_BROOM.get()).create(t.getLevel());v.fromItem(new ItemStack(mortar?ModItems.FLYING_MORTAR.get():ModItems.FLYING_BROOM.get()),p.getUUID());v.moveTo(p.position());t.getLevel().addFreshEntity(v);return v;}
 static long count(ServerPlayer p,Item item){long n=0;for(var s:p.getInventory().items)if(s.is(item))n+=s.getCount();return n;}
 @GameTest(template="empty") public static void mortar_authority(GameTestHelper t)throws Exception{
  var a=player(t);var b=player(t);var c=player(t);var v=vessel(t,a,true);v.interact(a,InteractionHand.MAIN_HAND);v.interact(b,InteractionHand.MAIN_HAND);v.interact(c,InteractionHand.MAIN_HAND);
  check(t,v.getPassengers().size()==2&&v.pilot()==a&&b.getVehicle()==v&&!c.isPassenger(),"mortар two player seats and capacity");
  check(t,v.getPassengerRidingPosition(a).distanceTo(v.getPassengerRidingPosition(b))>.8,"distinct seat positions");
  v.input(b,1,1,1,170,false);check(t,v.drive()==0,"passenger input rejected");v.openCargo(b);check(t,b.containerMenu==b.inventoryMenu,"passenger cannot open owner cargo");
  a.stopRiding();check(t,v.pilot()==null&&b.getVehicle()==v,"pilot exit does not promote passenger");v.input(b,1,1,1,170,false);v.tick();check(t,v.getDeltaMovement().y<0,"controlled inactive descent with passenger");
  a.moveTo(v.position());v.interact(a,InteractionHand.MAIN_HAND);check(t,v.pilot()==a,"owner explicitly resumes pilot seat");v.ejectPassengers();v.discard();t.succeed();
 }
 @GameTest(template="empty") public static void broom_capacity_and_cargo_save(GameTestHelper t)throws Exception{
  var a=player(t);var b=player(t);var broom=vessel(t,a,false);broom.interact(a,InteractionHand.MAIN_HAND);broom.interact(b,InteractionHand.MAIN_HAND);check(t,broom.getPassengers().size()==1&&broom.pilot()==a,"broom stays one-seat");a.stopRiding();broom.discard();
  var v=vessel(t,a,true);v.cargo.setItem(8,new ItemStack(Items.DIAMOND,7));var n=new CompoundTag();v.saveWithoutId(n);check(t,n.hasUUID("Pilot")&&n.hasUUID("Owner"),"pilot and owner persisted");n.remove("Pilot");var copy=ModEntities.FLYING_MORTAR.get().create(t.getLevel());copy.load(n);check(t,copy.cargo.getContainerSize()==9&&copy.cargo.getItem(8).getCount()==7,"legacy vessel preserves nine-slot cargo");a.startRiding(copy);check(t,copy.pilot()==a,"old NBT derives pilot from owner");a.stopRiding();copy.discard();v.discard();t.succeed();
 }
 @GameTest(template="empty") public static void flight_descent_regression(GameTestHelper t)throws Exception{
  for(boolean mortar:new boolean[]{false,true}){var a=player(t);a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.PESTLE.get()));var v=vessel(t,a,mortar);v.setPos(v.getX(),140,v.getZ());a.startRiding(v);double y=v.getY();for(int i=0;i<20;i++){v.input(a,0,0,-1,0,false);v.tick();}check(t,v.getY()<y-3&&a.getVehicle()==v,"existing controlled descent "+mortar);a.stopRiding();v.discard();}t.succeed();
 }
 @GameTest(template="empty") public static void flight_exchanges(GameTestHelper t)throws Exception{
  var a=player(t);var npc=ModEntities.BABA_YAGA.get().create(t.getLevel());npc.moveTo(a.position().add(1,0,0));npc.finalizeSpawn(t.getLevel(),t.getLevel().getCurrentDifficultyAt(npc.blockPosition()),net.minecraft.world.entity.MobSpawnType.SPAWN_EGG,null);t.getLevel().addFreshEntity(npc);var progress=YagaData.get(t.getLevel()).progress(a.getUUID());
  for(String id:new String[]{"flying_broom","flying_mortar","pestle"}){int index=0;while(!YagaServices.EXCHANGES[index].id.equals(id))index++;var offer=YagaServices.EXCHANGES[index];a.getInventory().clearContent();for(var need:offer.inputs)a.getInventory().add(need.stack());var menu=new YagaMenu(1,a.getInventory(),npc.getId());a.containerMenu=menu;progress.stage=offer.stage-1;a.getPersistentData().remove("YagaExchangeUntil");check(t,!menu.clickMenuButton(a,400+index)&&count(a,offer.output().getItem())==0,"locked flight trade "+id);progress.stage=offer.stage;a.getPersistentData().remove("YagaExchangeUntil");check(t,menu.clickMenuButton(a,400+index)&&count(a,offer.output().getItem())==1,"real menu trade outputs one "+id);for(var need:offer.inputs)check(t,count(a,need.item())==0,"trade consumes "+need.id);check(t,!menu.clickMenuButton(a,400+index)&&count(a,offer.output().getItem())==1,"duplicate trade denied "+id);menu.removed(a);a.containerMenu=a.inventoryMenu;}
  npc.discard();t.succeed();
 }
 @GameTest(template="empty") public static void bayun_specific_trust_and_nonce(GameTestHelper t)throws Exception{
  var a=player(t);var cat=org.slavicmyths.bayun.BayunContent.ENTITY.get().create(t.getLevel());cat.moveTo(a.position().add(1,0,0));t.getLevel().addFreshEntity(cat);a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MILK_BUCKET));cat.interact(a,InteractionHand.MAIN_HAND);check(t,cat.trust(a)==25&&cat.canTalk(a),"actual milk gift and safe dialogue");
  check(t,GuideQuests.permitted(a,"bayun",cat,0)&&!GuideQuests.permitted(a,"bayun",cat,1)&&!GuideQuests.permitted(a,"bayun",cat,2),"Bayun threshold25");cat.bond(a).putInt("Trust",60);check(t,GuideQuests.permitted(a,"bayun",cat,1)&&!GuideQuests.permitted(a,"bayun",cat,2),"Bayun threshold60");cat.bond(a).putInt("Trust",85);check(t,GuideQuests.permitted(a,"bayun",cat,2),"Bayun threshold85");
  var other=player(t);check(t,!GuideQuests.permitted(other,"bayun",cat,0),"trust is per player and cat");org.slavicmyths.bayun.BayunNetwork.choose(a,new org.slavicmyths.bayun.BayunNetwork.Choice(cat.getId(),UUID.randomUUID(),1,7));check(t,GuideQuests.snapshot(a).isEmpty(),"forged dialogue nonce cannot accept quest");cat.bond(a).putInt("Trust",80);cat.rewardTrust(a,5);check(t,cat.trust(a)==85&&YagaServices.killed(a,"friend_of_bayun"),"quest trust uses native friendship award at85");cat.discard();t.succeed();
 }
 @GameTest(template="empty") public static void wolf_four_levels(GameTestHelper t)throws Exception{
  var a=player(t);check(t,org.slavicmyths.quest185.Quest185Story.wolfTrust(a)==1,"wolf stranger");org.slavicmyths.wolf.WolfProgress.data(a).putBoolean("wolf_test_passed",true);check(t,org.slavicmyths.quest185.Quest185Story.wolfTrust(a)==2,"wolf trial trust");
  var old=org.slavicmyths.military.MilitaryQuests.state(a);var chain=TaleData.compound(old,"GreyWolfStory");TaleData.compound(chain,org.slavicmyths.military.GreyWolfStory.IDS[3]).putBoolean("Claimed",true);check(t,org.slavicmyths.quest185.Quest185Story.wolfTrust(a)==3,"legacy wolf chain preserved");org.slavicmyths.quest185.Quest185State.quest(org.slavicmyths.quest185.Quest185Story.data(a),8).putBoolean("Claimed",true);check(t,org.slavicmyths.quest185.Quest185Story.wolfTrust(a)==4,"loyalty adds fourth trust");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=120) public static void guide_existing_target_and_private_snapshot(GameTestHelper t)throws Exception{
  var a=player(t);YagaData.get(t.getLevel()).progress(a.getUUID()).stage=1;var b=new org.slavicmyths.hunt.HuntRecords.Boss("qa:"+a.getUUID(),org.slavicmyths.hunt.BossKind.LIKHO,a.blockPosition(),true);var actual=ModEntities.LIKHO_ONE_EYED.get().create(t.getLevel());actual.moveTo(a.position().add(2,0,0));actual.setNoAi(true);t.getLevel().addFreshEntity(actual);b.active=true;b.target=actual.getUUID();org.slavicmyths.hunt.HuntRecords.get(t.getLevel()).bosses.put(b.key,b);
  check(t,GuideQuests.request(a,"yaga",null,0),"accept guide request");var privateState=TaleData.get(t.getLevel()).player(a.getUUID()).getCompound("GuideQuests").getCompound("likho_one_eyed");UUID id=privateState.getUUID("Id");check(t,!GuideQuests.request(a,"yaga",null,0),"request rate limit");
  t.runAfterDelay(6,()->{check(t,privateState.getString("Status").equals("ACTIVE")&&privateState.getUUID("Target").equals(b.target),"existing anchor bound idempotently");var publicState=GuideQuests.snapshot(a).getCompound("likho_one_eyed");check(t,!publicState.contains("Anchor")&&!publicState.contains("Target")&&!publicState.contains("BossKey"),"no private target fields in book delta");check(t,org.slavicmyths.navigation.NavigationRecords.get(t.getLevel()).player(a.getUUID()).markers.containsKey(id),"existing navigation search marker");b.active=false;GuideQuests.reconcile(a);check(t,privateState.getString("Status").equals("UNAVAILABLE"),"dead encounter becomes unavailable, never respawned");org.slavicmyths.hunt.HuntRecords.get(t.getLevel()).bosses.remove(b.key);actual.discard();t.succeed();});
 }
 @GameTest(template="empty") public static void victory_gate_matrix(GameTestHelper t)throws Exception{
  var a=player(t);String[] ids={"name_your_misfortune","steppe_champion","defeat_nightingale"};check(t,!YagaDiscovery.eligible(a)&&GuideQuests.victories(a)==0,"zero victories gate");
  for(String id:ids){var advancement=a.server.getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id));for(String criterion:advancement.value().criteria().keySet())a.getAdvancements().award(advancement,criterion);check(t,YagaDiscovery.eligible(a)&&GuideQuests.victories(a)==1,"one of three victory gate "+id);for(String criterion:advancement.value().criteria().keySet())a.getAdvancements().revoke(advancement,criterion);}
  for(int i=0;i<3;i++){var adv=a.server.getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",ids[i]));for(String criterion:adv.value().criteria().keySet())a.getAdvancements().award(adv,criterion);check(t,GuideQuests.victories(a)==i+1,"distinct victories counted "+(i+1));}
  check(t,TaleData.get(t.getLevel()).stage(a.getUUID(),"slavicmyths:bayun_search")>=1,"two/three victories add Bayun rumor");t.succeed();
 }
 @GameTest(template="empty") public static void tracking_authority(GameTestHelper t)throws Exception{
  var p=player(t);var nav=org.slavicmyths.navigation.NavigationRecords.get(t.getLevel());
  for(String source:new String[]{"qa:a","qa:b"})org.slavicmyths.navigation.NavigationManager.reveal(p,source,org.slavicmyths.navigation.MarkerCategory.QUEST,org.slavicmyths.navigation.SlavicMarker.Kind.TRAVEL_POINT,"guides.track",net.minecraft.world.level.Level.OVERWORLD.location(),p.blockPosition(),false,false);
  var a=org.slavicmyths.navigation.SlavicMarker.identity("qa:a");var b=org.slavicmyths.navigation.SlavicMarker.identity("qa:b");nav.player(p.getUUID()).track(b);
  org.slavicmyths.navigation.NavigationManager.request(p,new org.slavicmyths.navigation.NavigationNetwork.Request(org.slavicmyths.navigation.NavigationNetwork.Action.STOP,a,0,false));check(t,b.equals(nav.player(p.getUUID()).tracked),"guide STOP cannot clear unrelated tracking");
  org.slavicmyths.navigation.NavigationManager.request(p,new org.slavicmyths.navigation.NavigationNetwork.Request(org.slavicmyths.navigation.NavigationNetwork.Action.TRACK,UUID.randomUUID(),0,true));check(t,b.equals(nav.player(p.getUUID()).tracked),"foreign marker cannot be tracked");
  org.slavicmyths.navigation.NavigationManager.request(p,new org.slavicmyths.navigation.NavigationNetwork.Request(org.slavicmyths.navigation.NavigationNetwork.Action.STOP,b,0,false));check(t,nav.player(p.getUUID()).tracked==null,"matching guide untracks");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=160) public static void guide_reward_is_nonrepeatable(GameTestHelper t)throws Exception{
  var a=player(t);YagaData.get(t.getLevel()).progress(a.getUUID()).stage=1;var actual=ModEntities.LIKHO_ONE_EYED.get().create(t.getLevel());actual.moveTo(a.position().add(2,0,0));actual.setNoAi(true);t.getLevel().addFreshEntity(actual);var b=new org.slavicmyths.hunt.HuntRecords.Boss("qa:reward:"+a.getUUID(),org.slavicmyths.hunt.BossKind.LIKHO,a.blockPosition(),true);b.active=true;b.target=actual.getUUID();org.slavicmyths.hunt.HuntRecords.get(t.getLevel()).bosses.put(b.key,b);
  var npc=ModEntities.BABA_YAGA.get().create(t.getLevel());npc.moveTo(a.position().add(1,0,0));npc.setNoGravity(true);npc.setNoAi(true);npc.finalizeSpawn(t.getLevel(),t.getLevel().getCurrentDifficultyAt(npc.blockPosition()),net.minecraft.world.entity.MobSpawnType.SPAWN_EGG,null);t.getLevel().addFreshEntity(npc);var menu=new YagaMenu(2,a.getInventory(),npc.getId());a.containerMenu=menu;check(t,menu.clickMenuButton(a,704),"actual menu accepts one-shot errand");
  t.runAfterDelay(25,()->{
   var adv=a.server.getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","name_your_misfortune"));for(String criterion:adv.value().criteria().keySet())a.getAdvancements().award(adv,criterion);GuideQuests.reconcile(a);var q=TaleData.get(t.getLevel()).player(a.getUUID()).getCompound("GuideQuests").getCompound("likho_one_eyed");check(t,q.getString("Status").equals("RETURN"),"native advancement completes errand and requires return");check(t,menu.clickMenuButton(a,704),"original guide claims reward");check(t,count(a,ModItems.ANCIENT_SIGN.get())==1&&q.getBoolean("Claimed")&&q.getString("Status").equals("DONE"),"one ancient sign and durable claimed flag");menu.clickMenuButton(a,704);
   t.runAfterDelay(25,()->{menu.clickMenuButton(a,704);check(t,count(a,ModItems.ANCIENT_SIGN.get())==1,"delayed duplicate claim cannot repeat reward");org.slavicmyths.hunt.HuntRecords.get(t.getLevel()).bosses.remove(b.key);actual.discard();npc.discard();t.succeed();});
  });
 }
 @GameTest(template="empty",timeoutTicks=120) public static void nightingale_existing_index_shared_by_two_players(GameTestHelper t)throws Exception{
  var a=player(t);var b=player(t);YagaData.get(t.getLevel()).progress(a.getUUID()).stage=1;YagaData.get(t.getLevel()).progress(b.getUUID()).stage=1;
  var boss=ModEntities.NIGHTINGALE.get().create(t.getLevel());boss.camp=UUID.randomUUID();boss.home=a.blockPosition().offset(2,0,0);boss.moveTo(net.minecraft.world.phys.Vec3.atCenterOf(boss.home));boss.setNoAi(true);org.slavicmyths.bandit.StrongholdRecords.get(t.getLevel()).nightingaleSpawned(boss.camp);org.slavicmyths.bandit.StrongholdRecords.get(t.getLevel()).record(boss.camp).cleared=true;t.getLevel().addFreshEntity(boss);
  check(t,"nightingale".equals(org.slavicmyths.navigation.NavigationManager.type(boss)),"Nightingale participates in existing navigation index");check(t,org.slavicmyths.navigation.NavigationRecords.get(t.getLevel()).encounters.containsKey(boss.getUUID()),"actual entity join indexed live Nightingale even after bandit roster cleared");check(t,GuideQuests.request(a,"yaga",null,2)&&GuideQuests.request(b,"yaga",null,2),"two separate guide quests accepted");
  t.runAfterDelay(8,()->{for(var player:List.of(a,b)){var q=TaleData.get(t.getLevel()).player(player.getUUID()).getCompound("GuideQuests").getCompound("nightingale");check(t,q.getString("Status").equals("ACTIVE")&&q.getUUID("Target").equals(boss.getUUID()),"same actual Nightingale reused by "+player.getUUID());}boss.discard();t.succeed();});
 }
 @GameTest(template="empty",batch="bayun_routes",timeoutTicks=160) public static void bayun_authenticated_three_routes(GameTestHelper t)throws Exception{
  var a=player(t);var cat=org.slavicmyths.bayun.BayunContent.ENTITY.get().create(t.getLevel());cat.home=a.blockPosition();cat.moveTo(a.position().add(1,0,0));cat.setNoAi(true);cat.setNoGravity(true);t.getLevel().addFreshEntity(cat);a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MILK_BUCKET));cat.interact(a,InteractionHand.MAIN_HAND);cat.bond(a).putInt("Trust",85);a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);bayunRoute(t,a,cat,0);
 }
 static void bayunRoute(GameTestHelper t,ServerPlayer p,org.slavicmyths.bayun.BayunEntity cat,int target){
  cat.interact(p,InteractionHand.MAIN_HAND);org.slavicmyths.bayun.BayunNetwork.open(p,cat);var nonce=p.getPersistentData().getUUID("BayunMenu");org.slavicmyths.bayun.BayunNetwork.choose(p,new org.slavicmyths.bayun.BayunNetwork.Choice(cat.getId(),nonce,1,7+target));check(t,GuideQuests.snapshot(p).contains(GuideQuests.TARGETS[target]),"authenticated Bayun route "+GuideQuests.TARGETS[target]);
  if(target==2){cat.discard();t.succeed();}else t.runAfterDelay(25,()->bayunRoute(t,p,cat,target+1));
 }
 @GameTest(template="empty",timeoutTicks=2400) public static void command_large_camp_preserves_sentinel(GameTestHelper t)throws Exception{
  var a=player(t);a.teleportTo(t.getLevel(),12000,70,12000,0,0);var sentinel=new BlockPos(12020,-56,11840);t.getLevel().setBlockAndUpdate(sentinel,net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.defaultBlockState());
  a.server.getCommands().performPrefixedCommand(a.createCommandSourceStack().withPermission(4),"slavicmyths generate bandit_camp large 2362026");UUID job=org.slavicmyths.worldgen.ManualStructureJobs.lastJob;check(t,job!=null,"actual large-camp command accepted");poll(t,a,job,sentinel,0);
 }
 static void poll(GameTestHelper t,ServerPlayer a,UUID job,BlockPos sentinel,int waited){t.runAfterDelay(20,()->{var result=org.slavicmyths.worldgen.ManualStructureJobs.result(job);if(result==null){if(waited>2000)throw new AssertionError("manual command timeout");poll(t,a,job,sentinel,waited+20);return;}check(t,t.getLevel().getBlockState(sentinel).is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK),"manual command preserves player block INSIDE intended footprint");System.out.println("GUIDE_COMMAND_RESULT "+result);check(t,result.success(),"whole command fortress generated");check(t,result.entrance().getX()!=12039||result.entrance().getZ()!=11822,"unsafe original footprint rejected and relocated");t.succeed();});}
}

