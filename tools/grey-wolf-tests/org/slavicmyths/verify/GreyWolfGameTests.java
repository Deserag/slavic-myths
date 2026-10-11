package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.wolf.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.combat.*;
import net.neoforged.neoforge.event.entity.living.*;
@GameTestHolder("slavicmyths_wolf") @PrefixGameTestTemplate(false)
public final class GreyWolfGameTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) { }
    }
    private static ServerPlayer player(GameTestHelper t){var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ArmorProbe"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,2,3)));p.setNoGravity(true);for(int i=0;i<61;i++)p.tick();return p;}

 private static ServerPlayer actor(GameTestHelper t){var p=player(t);t.getLevel().addNewPlayer(p);return p;}
 private static GreyWolf wolf(GameTestHelper t){var w=ModEntities.GREY_WOLF.get().create(t.getLevel());w.moveTo(t.absoluteVec(new Vec3(3,2,5)));w.setNoGravity(true);w.setOnGround(true);w.debug(true);t.getLevel().addFreshEntity(w);return w;}
 private static UUID token(GreyWolf wolf)throws Exception{var f=GreyWolf.class.getDeclaredField("token");f.setAccessible(true);return (UUID)f.get(wolf);}
 private static void elapsed(GreyWolf wolf,int ticks)throws Exception{var f=GreyWolf.class.getDeclaredField("CLOCK");f.setAccessible(true);wolf.getEntityData().set((net.minecraft.network.syncher.EntityDataAccessor<Long>)f.get(null),wolf.level().getGameTime()-ticks);}
 @GameTest(template="empty") public static void registryEggAndNeutralImmunity(GameTestHelper t){var w=wolf(t);var p=actor(t);t.assertTrue(ModItems.GREY_WOLF_SPAWN_EGG.get() instanceof SpawnEggItem,"egg");t.assertTrue(ModItemGroup.items(8).contains(ModItems.GREY_WOLF_SPAWN_EGG.get()),"creative");float hp=w.getHealth();w.hurt(w.damageSources().playerAttack(p),1000);w.hurt(w.damageSources().lava(),1000);w.kill();t.assertTrue(w.isAlive()&&w.getHealth()==hp,"neutral died");t.assertTrue(w.getTarget()==null,"neutral aggression");t.succeed();}
 @GameTest(template="empty") public static void consentNonceDeclineAndDoubleStart(GameTestHelper t)throws Exception{
  var p=actor(t);var other=actor(t);var w=wolf(t);w.greet(p);var nonce=token(w);elapsed(w,10);w.tick();t.assertTrue(w.state()==WolfTimeline.State.WAITING_DECISION,"dialog state");t.assertTrue(!w.decision(other,nonce,true),"foreign nonce");t.assertTrue(!w.decision(p,UUID.randomUUID(),true),"forged nonce");t.assertTrue(w.decision(p,nonce,false)&&!w.busy(),"decline aggression");w.greet(p);nonce=token(w);elapsed(w,10);w.tick();t.assertTrue(w.decision(p,nonce,true),"accept");t.assertTrue(w.getHealth()==500&&w.state()==WolfTimeline.State.TRIAL_PREPARE,"prepare");t.assertTrue(!w.start(other)&&!w.decision(p,nonce,true),"double start");t.succeed();
 }
 @GameTest(template="empty") public static void completionClampAndPersonalWaypoint(GameTestHelper t)throws Exception{
  var p=actor(t);var other=actor(t);var w=wolf(t);t.assertTrue(w.start(p),"start");elapsed(w,30);w.tick();w.setHealth(55);w.invulnerableTime=0;w.hurt(w.damageSources().playerAttack(other),120);t.assertTrue(w.getHealth()==55,"spectator stole progress");w.hurt(w.damageSources().playerAttack(p),120);t.assertTrue(w.getHealth()==50&&w.isAlive()&&w.state()==WolfTimeline.State.TRIAL_COMPLETE_CINEMATIC,"threshold death");w.hurt(w.damageSources().explosion(null,null),10000);t.assertTrue(w.getHealth()==50,"NPC damage");t.assertTrue(!WolfProgress.passed(p),"early progress");elapsed(w,160);w.tick();t.assertTrue(WolfProgress.passed(p)&&!WolfProgress.passed(other),"personal flags");var state=org.slavicmyths.navigation.NavigationRecords.get(t.getLevel()).player(p.getUUID());var mark=state.markers.values().stream().filter(m->m.source().startsWith("grey_wolf:")).findFirst().orElseThrow();t.assertTrue(mark.x()==w.home().getX()&&mark.z()==w.home().getZ(),"wrong anchor");WolfProgress.complete(p,w);t.assertTrue(state.markers.values().stream().filter(m->m.source().startsWith("grey_wolf:")).count()==1,"duplicate waypoint");t.assertTrue(w.state()==WolfTimeline.State.POST_TRIAL_NPC,"NPC state");t.assertTrue(w.start(other)&&w.getHealth()==500,"second personal trial");w.abort();t.assertTrue(!WolfProgress.passed(other),"abort awarded");t.succeed();
 }
 @GameTest(template="empty") public static void shieldDodgeAndCutCap(GameTestHelper t)throws Exception{
  var p=actor(t);var w=wolf(t);w.start(p);elapsed(w,30);w.tick();w.setYRot(180);w.setXRot(0);p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));p.setYRot(0);p.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);for(int i=0;i<6;i++)p.doTick();t.assertTrue(p.isBlocking(),"shield setup");w.strike(p,WolfTimeline.Move.BITE);t.assertTrue(!p.hasEffect(ModEffects.WOLF_CUT)&&p.getHealth()==20,"shield bleed");p.stopUsingItem();p.moveTo(w.position().add(0,0,-2));p.invulnerableTime=0;w.strike(p,WolfTimeline.Move.BITE);t.assertTrue(p.hasEffect(ModEffects.WOLF_CUT),"unblocked no cut");p.setHealth(20);for(int i=0;i<6;i++)WolfBleeding.apply(w,p);t.assertTrue(p.getEffect(ModEffects.WOLF_CUT).getAmplifier()==2&&p.getEffect(ModEffects.WOLF_CUT).getDuration()==160,"cut cap/refresh");p.removeAllEffects();p.moveTo(w.position().add(0,0,10));w.strike(p,WolfTimeline.Move.BITE);t.assertTrue(!p.hasEffect(ModEffects.WOLF_CUT),"dodge bleed");t.succeed();
 }
 @GameTest(template="empty") public static void cutCadenceMixedPriorityAndMilk(GameTestHelper t){
  var p=actor(t);var w=wolf(t);for(int level=1;level<=3;level++){p.removeAllEffects();p.setHealth(20);for(int i=0;i<level;i++)WolfBleeding.apply(w,p);p.getPersistentData().putLong("RareBleedPulse",0);p.invulnerableTime=0;ModEffects.WOLF_CUT.get().applyEffectTick(p,level-1);t.assertTrue(Math.abs(p.getHealth()-(20-2*level))<.001,"DoT level "+level);ModEffects.WOLF_CUT.get().applyEffectTick(p,level-1);t.assertTrue(Math.abs(p.getHealth()-(20-2*level))<.001,"double pulse");}
  p.removeAllEffects();p.getServer().setPvpAllowed(true);var caster=actor(t);t.assertTrue(CutEffect.permitted(caster,p),"mixed PvP fixture");RareBleedEffect.applyRune(caster,p);WolfBleeding.apply(w,p);RareBleedEffect.applyRune(caster,p);t.assertTrue(p.hasEffect(ModEffects.WOLF_CUT)&&!p.hasEffect(ModEffects.RARE_BLEED),"weak replaced strong");t.assertTrue(p.getEffect(ModEffects.WOLF_CUT).getAmplifier()<=2,"mixed cap");p.removeAllEffects();t.assertTrue(RareBleedEffect.current(p)==null,"milk clear");RareBleedEffect.applyRune(caster,p);t.assertTrue(p.getPersistentData().getInt("RareBleedWolfStacks")==0,"stale strong after milk");t.succeed();
 }
 @GameTest(template="empty") public static void reloadAbortsAndNpcPersists(GameTestHelper t)throws Exception{var p=actor(t);var w=wolf(t);w.start(p);var tag=new net.minecraft.nbt.CompoundTag();w.saveWithoutId(tag);var loaded=ModEntities.GREY_WOLF.get().create(t.getLevel());loaded.load(tag);t.assertTrue(!loaded.busy()&&loaded.getHealth()==500&&loaded.participant()==null,"reload resumed hostile trial");elapsed(w,30);w.tick();w.finish();elapsed(w,160);w.tick();tag=new net.minecraft.nbt.CompoundTag();w.saveWithoutId(tag);loaded.load(tag);t.assertTrue(loaded.state()==WolfTimeline.State.POST_TRIAL_NPC&&loaded.getHealth()==50&&loaded.encounterId().equals(w.encounterId()),"persistent NPC");t.succeed();}
 @GameTest(template="empty") public static void realTimelineAndHowlPack(GameTestHelper t)throws Exception{
  var p=actor(t);var w=wolf(t);w.start(p);elapsed(w,30);w.tick();
  for(int x=-12;x<=18;x++)for(int z=-12;z<=18;z++){t.setBlock(new net.minecraft.core.BlockPos(x,1,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=2;y<=8;y++)t.setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}
  w.setOnGround(true);t.assertTrue(w.debugMove(WolfTimeline.Move.HOWL_SUMMON),"safe howl rejected");
  var before=t.getLevel().getEntitiesOfClass(org.slavicmyths.entity.WildlifeEntity.class,w.getBoundingBox().inflate(15)).size();elapsed(w,109);w.tick();t.assertTrue(t.getLevel().getEntitiesOfClass(org.slavicmyths.entity.WildlifeEntity.class,w.getBoundingBox().inflate(15)).size()==before,"early pack");elapsed(w,110);w.tick();var pack=t.getLevel().getEntitiesOfClass(org.slavicmyths.entity.WildlifeEntity.class,w.getBoundingBox().inflate(15),a->a.getPersistentData().hasUUID("encounterId")&&a.getPersistentData().getUUID("encounterId").equals(w.encounterId()));t.assertTrue(pack.size()>=2&&pack.size()<=3,"missing pack "+pack.size());for(var a:pack)t.assertTrue(a.getTarget()==null&&a.getType()==ModEntities.FOREST_WOLF.get(),"wrong immediate pack target");for(var a:pack){a.getPersistentData().putLong("greyWolfActivate",t.getLevel().getGameTime());a.aiStep();t.assertTrue(a.getTarget()==p,"pack did not select consenting owner");}
  w.abort();for(var a:pack)t.assertTrue(a.getPersistentData().contains("greyWolfLeave"),"pack not recalled");t.assertTrue(WolfTimeline.hit(WolfTimeline.Move.DOUBLE_CLAW,14)&&WolfTimeline.hit(WolfTimeline.Move.DOUBLE_CLAW,33)&&!WolfTimeline.hit(WolfTimeline.Move.DOUBLE_CLAW,15),"hit windows");t.succeed();
 }
 @GameTest(template="empty") public static void nativePounceAndDashMovement(GameTestHelper t)throws Exception{
  for(int x=-8;x<=18;x++)for(int z=-8;z<=22;z++){t.setBlock(new net.minecraft.core.BlockPos(x,20,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=21;y<=30;y++)t.setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}
  var p=actor(t);var w=wolf(t);p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);p.moveTo(t.absoluteVec(new Vec3(3.5,21,9.5)));w.moveTo(t.absoluteVec(new Vec3(3.5,21,3.5)));w.setNoGravity(false);w.setOnGround(true);w.start(p);elapsed(w,30);w.tick();w.setOnGround(true);w.setYRot(0);t.assertTrue(w.debugMove(WolfTimeline.Move.POUNCE),"pounce start");double startZ=w.getZ(),y=w.getY(),peak=0;boolean flew=false;int landing=0;
  for(int tick=1;tick<=40;tick++){elapsed(w,tick);w.tick();peak=Math.max(peak,w.getY()-y);if(!w.onGround())flew=true;if(flew&&w.onGround()){landing=tick;break;}}
  double range=w.getZ()-startZ;t.assertTrue(flew&&landing>16&&range>=5&&range<=8,"native pounce range="+range+" landing="+landing+" peak="+peak);System.out.println("GREY_WOLF_NATIVE_POUNCE range="+range+" apex="+peak+" landing="+landing);
  elapsed(w,40);w.tick();w.setOnGround(true);p.moveTo(w.position().add(0,0,3));t.assertTrue(w.debugMove(WolfTimeline.Move.FLANK_DASH),"dash start");var begin=w.position();for(int tick=1;tick<=18;tick++){elapsed(w,tick);w.tick();}double dash=w.position().subtract(begin).horizontalDistance();t.assertTrue(dash>=2.5&&dash<=4,"dash distance "+dash);System.out.println("GREY_WOLF_NATIVE_DASH distance="+dash);t.succeed();
 }
 @GameTest(template="empty") public static void wallBlocksHitAndCadenceWindows(GameTestHelper t)throws Exception{
  var p=actor(t);var w=wolf(t);w.start(p);elapsed(w,30);w.tick();w.setYRot(180);p.moveTo(w.position().add(0,0,-2));var wall=net.minecraft.core.BlockPos.containing(w.position().add(0,0,-1));t.getLevel().setBlock(wall,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);t.getLevel().setBlock(wall.above(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);t.getLevel().setBlock(wall.above(2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);w.strike(p,WolfTimeline.Move.POUNCE);t.assertTrue(!p.hasEffect(ModEffects.WOLF_CUT)&&p.getHealth()==20,"wall hit");
  t.assertTrue(!WolfTimeline.hit(WolfTimeline.Move.BITE,11)&&WolfTimeline.hit(WolfTimeline.Move.BITE,12)&&!WolfTimeline.hit(WolfTimeline.Move.BITE,13),"bite frame");t.assertTrue(WolfTimeline.HOWL_DELAY==600&&WolfTimeline.HOWL_COOLDOWN==2000&&WolfTimeline.MAX_SUMMONS==2,"summon balance");t.succeed();
 }
 @GameTest(template="empty") public static void naturalQuotaAndSavedEncounterDedup(GameTestHelper t){
  int hits=0;for(int i=0;i<10000;i++)if(WolfEncounter.eligibleRegion(471,i,-i))hits++;t.assertTrue(hits>800&&hits<1200,"region probability "+hits);
  var records=org.slavicmyths.hunt.HuntRecords.get(t.getLevel());var added=new ArrayList<String>();for(int i=0;i<5;i++){String key="wolf_fixture_"+UUID.randomUUID();var b=new org.slavicmyths.hunt.HuntRecords.Boss(key,org.slavicmyths.hunt.BossKind.TUGARIN,t.absolutePos(new net.minecraft.core.BlockPos(i,1,1)),true);b.safe=true;records.bosses.put(key,b);added.add(key);}
  t.assertTrue(WolfEncounter.quota(t.getLevel())>=2,"quota");added.forEach(records.bosses::remove);
  var d=new WolfRecords();UUID id=UUID.randomUUID();var home=t.absolutePos(new net.minecraft.core.BlockPos(3,2,3));d.homes.put(id,home);d.regions.add(123L);var n=d.save(new net.minecraft.nbt.CompoundTag(),t.getLevel().registryAccess());var loaded=WolfRecords.load(n,t.getLevel().registryAccess());t.assertTrue(loaded.homes.size()==1&&loaded.homes.get(id).equals(home)&&loaded.regions.contains(123L),"encounter persistence");t.succeed();
 }
 @GameTest(template="empty") public static void cutExpiryCreativeAndLegacyDamage(GameTestHelper t){
  var p=actor(t);var w=wolf(t);WolfBleeding.apply(w,p);p.getPersistentData().putLong("RareBleedWolfUntil",RareCombat.now(p));t.assertTrue(!ModEffects.WOLF_CUT.get().applyEffectTick(p,0),"strong did not expire");p.removeAllEffects();p.setGameMode(GameType.CREATIVE);WolfBleeding.apply(w,p);t.assertTrue(!p.hasEffect(ModEffects.WOLF_CUT),"creative cut");
  var caster=actor(t);var cow=EntityType.COW.create(t.getLevel());cow.setHealth(10);RareBleedEffect.applyRune(caster,cow);cow.getPersistentData().putLong("RareBleedPulse",0);cow.invulnerableTime=0;ModEffects.RARE_BLEED.get().applyEffectTick(cow,0);t.assertTrue(Math.abs(cow.getHealth()-9.5)<.001,"legacy changed");t.succeed();
 }
 @GameTest(template="empty") public static void cutRealMilkItemAndShieldBothClaws(GameTestHelper t)throws Exception{
  var p=actor(t);var w=wolf(t);w.start(p);elapsed(w,30);w.tick();w.setYRot(180);p.setYRot(0);p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));p.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);for(int i=0;i<6;i++)p.doTick();for(int i=0;i<2;i++){p.invulnerableTime=0;w.strike(p,WolfTimeline.Move.DOUBLE_CLAW);}t.assertTrue(!p.hasEffect(ModEffects.WOLF_CUT)&&p.getHealth()==20,"claw shield bleed");p.stopUsingItem();WolfBleeding.apply(w,p);new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);t.assertTrue(!p.hasEffect(ModEffects.WOLF_CUT),"real milk retained wound");t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=220) public static void nativeWoundExpiryAndLegacyRestore(GameTestHelper t)throws Exception{
  var p=actor(t);var caster=actor(t);p.getServer().setPvpAllowed(true);var owners=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");owners.setAccessible(true);var online=(java.util.Map<UUID,ServerPlayer>)owners.get(p.getServer().getPlayerList());online.put(caster.getUUID(),caster);var w=wolf(t);WolfBleeding.apply(w,p);
  for(int i=1;i<=165;i++){int tick=i;t.runAtTickTime(i,()->{if(tick==100)RareBleedEffect.applyRune(caster,p);p.doTick();});}
  t.runAtTickTime(165,()->{t.assertTrue(!p.hasEffect(ModEffects.WOLF_CUT),"strong outlived160");t.assertTrue(p.hasEffect(ModEffects.RARE_BLEED),"weak was lost after strong expired");online.remove(caster.getUUID());t.succeed();});
 }

 @GameTest(template="empty") public static void ownerDisconnectAndSummonBudget(GameTestHelper t)throws Exception{
  var p=actor(t);var w=wolf(t);w.start(p);elapsed(w,30);w.tick();var field=GreyWolf.class.getDeclaredField("summons");field.setAccessible(true);field.setInt(w,2);t.assertTrue(!w.debugMove(WolfTimeline.Move.HOWL_SUMMON),"third summon accepted");p.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_WITH_PLAYER);w.tick();t.assertTrue(!w.busy()&&!WolfProgress.passed(p),"logout retained trial or reward");t.succeed();
 }

 @GameTest(template="empty") public static void clearingColumnRejectsCeilingAndHazard(GameTestHelper t){
  var p=t.absolutePos(new net.minecraft.core.BlockPos(3,2,3));t.setBlock(new net.minecraft.core.BlockPos(3,1,3),net.minecraft.world.level.block.Blocks.STONE);for(int y=2;y<7;y++)t.setBlock(new net.minecraft.core.BlockPos(3,y,3),net.minecraft.world.level.block.Blocks.AIR);
  t.assertTrue(WolfEncounter.safeArenaColumn(t.getLevel(),p),"flat solid floor rejected");t.setBlock(new net.minecraft.core.BlockPos(3,4,3),net.minecraft.world.level.block.Blocks.STONE);t.assertTrue(!WolfEncounter.safeArenaColumn(t.getLevel(),p),"low ceiling accepted");t.setBlock(new net.minecraft.core.BlockPos(3,4,3),net.minecraft.world.level.block.Blocks.AIR);t.setBlock(new net.minecraft.core.BlockPos(3,1,3),net.minecraft.world.level.block.Blocks.MAGMA_BLOCK);t.assertTrue(!WolfEncounter.safeArenaColumn(t.getLevel(),p),"hazard accepted");t.succeed();
 }

 private static void hotfixArena(GameTestHelper t,int floor){for(int x=-6;x<=14;x++)for(int z=-6;z<=16;z++){t.setBlock(new net.minecraft.core.BlockPos(x,floor,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=floor+1;y<=floor+11;y++)t.setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}}
 private static GreyWolf hotfixWolf(GameTestHelper t,ServerPlayer p,int floor)throws Exception{var w=wolf(t);w.moveTo(t.absoluteVec(new Vec3(3.5,floor+1,3.5)));p.moveTo(t.absoluteVec(new Vec3(3.5,floor+1,9.5)));w.setNoGravity(false);w.setOnGround(true);p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);t.assertTrue(w.start(p),"start");elapsed(w,30);w.tick();w.setOnGround(true);return w;}
 @GameTest(template="empty") public static void lowCanopyRejectsPounceAndSelfDamage(GameTestHelper t)throws Exception{
  hotfixArena(t,40);var p=actor(t);var w=hotfixWolf(t,p,40);
  for(int x=1;x<=5;x++)for(int z=2;z<=10;z++)t.setBlock(new net.minecraft.core.BlockPos(x,45,z),net.minecraft.world.level.block.Blocks.OAK_LEAVES);
  t.assertTrue(!w.debugMove(WolfTimeline.Move.POUNCE)&&w.move()==WolfTimeline.Move.NONE,"jump into canopy");float hp=w.getHealth();
  w.causeFallDamage(8,1,w.damageSources().fall());w.hurt(w.damageSources().inWall(),10);w.hurt(w.damageSources().mobAttack(w),10);t.assertTrue(w.getHealth()==hp,"self/environment damage");
  w.invulnerableTime=0;w.hurt(w.damageSources().playerAttack(p),5);t.assertTrue(w.getHealth()<hp,"participant damage blocked");t.succeed();
 }
 @GameTest(template="empty") public static void trunkAndLateCanopyCancelLaunch(GameTestHelper t)throws Exception{
  hotfixArena(t,60);var p=actor(t);var w=hotfixWolf(t,p,60);
  for(int y=61;y<=67;y++)t.setBlock(new net.minecraft.core.BlockPos(3,y,6),net.minecraft.world.level.block.Blocks.OAK_LOG);
  t.assertTrue(!w.debugMove(WolfTimeline.Move.POUNCE),"jump through trunk");
  for(int y=61;y<=67;y++)t.setBlock(new net.minecraft.core.BlockPos(3,y,6),net.minecraft.world.level.block.Blocks.AIR);
  t.assertTrue(w.debugMove(WolfTimeline.Move.POUNCE),"open jump blocked");
  for(int x=1;x<=5;x++)for(int z=2;z<=10;z++)t.setBlock(new net.minecraft.core.BlockPos(x,65,z),net.minecraft.world.level.block.Blocks.OAK_LEAVES);
  elapsed(w,16);w.tick();t.assertTrue(w.move()==WolfTimeline.Move.NONE&&w.getDeltaMovement().y<=0,"late overhead not cancelled");t.succeed();
 }
 @GameTest(template="empty") public static void nativePounceTracksReleaseAndKeepsHealth(GameTestHelper t)throws Exception{
  hotfixArena(t,100);var p=actor(t);var w=hotfixWolf(t,p,100);t.assertTrue(w.debugMove(WolfTimeline.Move.POUNCE),"jump start");
  p.moveTo(w.position().add(6,0,0));elapsed(w,16);w.tick();t.assertTrue(w.getDeltaMovement().x>.5&&Math.abs(w.getDeltaMovement().z)<.01,"stale launch direction move="+w.move()+" ground="+w.onGround()+" v="+w.getDeltaMovement());
  double x=w.getX();float hp=w.getHealth();boolean flew=false;for(int i=17;i<=40;i++){elapsed(w,i);w.tick();if(!w.onGround())flew=true;}
  t.assertTrue(flew&&w.getX()-x>3&&w.getHealth()==hp,"native jump damage/movement flew="+flew+" range="+(w.getX()-x)+" hp="+w.getHealth()+" expected="+hp);t.succeed();
 }
 @GameTest(template="empty") public static void broomAndMortarDescendWithoutDismount(GameTestHelper t){
  for(boolean mortar:new boolean[]{false,true}){var p=actor(t);p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ModItems.PESTLE.get()));var vessel=(mortar?ModEntities.FLYING_MORTAR.get():ModEntities.FLYING_BROOM.get()).create(t.getLevel());vessel.moveTo(t.absoluteVec(new Vec3(3,140,3)));vessel.fromItem(new ItemStack(mortar?ModItems.FLYING_MORTAR.get():ModItems.FLYING_BROOM.get()),p.getUUID());t.getLevel().addFreshEntity(vessel);t.assertTrue(p.startRiding(vessel),"mount");double y=vessel.getY();
   for(int i=0;i<20;i++){vessel.input(p,0,0,-1,0,false);vessel.tick();}t.assertTrue(vessel.getY()<y-3&&p.getVehicle()==vessel,"descent/dismount "+mortar);
   vessel.input(p,0,0,0,0,true);for(int i=0;i<50;i++)vessel.tick();t.assertTrue(Math.abs(vessel.getDeltaMovement().y)<.001,"released descent not stopped");p.stopRiding();t.assertTrue(p.getVehicle()==null,"normal dismount blocked");vessel.discard();
  }t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=220) public static void sustainedTrialUnderCanopy(GameTestHelper t)throws Exception{
  for(int x=-3;x<=12;x++)for(int z=-3;z<=14;z++){t.setBlock(new net.minecraft.core.BlockPos(x,80,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=81;y<=87;y++)t.setBlock(new net.minecraft.core.BlockPos(x,y,z),y==85?net.minecraft.world.level.block.Blocks.OAK_LEAVES:net.minecraft.world.level.block.Blocks.AIR);}
  var p=actor(t);var w=wolf(t);w.moveTo(t.absoluteVec(new Vec3(3.5,81,3.5)));p.moveTo(t.absoluteVec(new Vec3(3.5,81,9.5)));w.setNoGravity(false);w.setOnGround(true);p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);t.assertTrue(w.start(p),"start");elapsed(w,30);w.tick();
  t.runAfterDelay(180,()->{t.assertTrue(w.active()&&w.getHealth()==500,"canopy damaged/aborted wolf");t.assertTrue(p.getHealth()<1000,"AI never approached or attacked under canopy");t.assertTrue(t.getLevel().noCollision(w,w.getBoundingBox().deflate(.001)),"wolf inside blocks");t.succeed();});
 }

}
