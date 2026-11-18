package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.quest185.*;
import org.slavicmyths.sanctuary.*;
import org.slavicmyths.artifact.*;
import org.slavicmyths.gorynych.*;
import org.slavicmyths.flight.*;

@GameTestHolder("slavicmyths_artifacts284") @PrefixGameTestTemplate(false)
public final class Artifacts284GameTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener listener){}
    }
    private static ServerPlayer player(GameTestHelper t){var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"RelicProbe"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,20,3)));p.setNoGravity(true);for(int i=0;i<61;i++)p.tick();return p;}
    private static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
    private static ItemStack held(ServerPlayer p,Item item,int count){var s=new ItemStack(item,count);p.setItemInHand(InteractionHand.MAIN_HAND,s);return s;}
    private static InteractionResultHolder<ItemStack> use(ServerPlayer p){return p.getMainHandItem().use(p.level(),p,InteractionHand.MAIN_HAND);}
    private static void deny(ServerPlayer p,net.minecraft.core.Holder<MobEffect> effect,Runnable action){
        java.util.function.Consumer<MobEffectEvent.Applicable> block=e->{if(e.getEntity()==p&&e.getEffectInstance().is(effect))e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);};
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,block);
        try{action.run();}finally{NeoForge.EVENT_BUS.unregister(block);}
    }
    @GameTest(template="empty") public static void all34RegistryAndAcquisitionRecipes(GameTestHelper t)throws Exception{
        var catalog=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(java.nio.file.Path.of("../docs/implementation/artifacts284-20261025/catalog.json"))).getAsJsonObject();
        int count=0,recipes=0;for(var value:catalog.getAsJsonArray("items")){var e=value.getAsJsonObject();var id=e.get("id").getAsString();t.assertTrue(item(id)!=Items.AIR,"missing registry "+id);for(var path:e.getAsJsonArray("recipes")){String file=path.getAsString(),key=file.substring(file.indexOf("/recipe/")+8,file.length()-5);t.assertTrue(t.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("slavicmyths",key)).isPresent(),"unloaded recipe "+key);recipes++;}count++;}
        t.assertTrue(count==34&&recipes>10,"catalog size/recipes "+count+"/"+recipes);t.succeed();
    }
    @GameTest(template="empty") public static void rejectedMorFlaskSpawnDoesNotConsume(GameTestHelper t){
        var p=player(t);var s=held(p,ModItems.MOR_FLASK.get(),3);
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> block=e->{if(e.getEntity() instanceof org.slavicmyths.mor.MorFlaskProjectile)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,block);
        try{var result=use(p);t.assertTrue(result.getResult()==InteractionResult.FAIL&&s.getCount()==3&&!p.getCooldowns().isOnCooldown(s.getItem()),"rejected projectile consumed flask/cooldown");}finally{NeoForge.EVENT_BUS.unregister(block);}t.succeed();
    }
    @GameTest(template="empty") public static void rejectedFlameGuardDoesNotConsume(GameTestHelper t){var p=player(t);var s=held(p,SanctuaryContent.item("fading_flame_charm"),3);deny(p,SanctuaryContent.GUARD,()->{var r=use(p);t.assertTrue(r.getResult()==InteractionResult.FAIL&&s.getCount()==3&&!p.hasEffect(SanctuaryContent.GUARD),"rejected GUARD consumed charm");});t.succeed();}
    @GameTest(template="empty") public static void rejectedMorWardDoesNotConsume(GameTestHelper t){var p=player(t);var s=held(p,Quest185Content.WARD.get(),3);deny(p,Quest185Content.WARD_EFFECT,()->{var r=use(p);t.assertTrue(r.getResult()==InteractionResult.FAIL&&s.getCount()==3&&!p.hasEffect(Quest185Content.WARD_EFFECT),"rejected ward consumed item");});t.succeed();}
    @GameTest(template="empty") public static void rejectedTrackerDoesNotSpendCooldown(GameTestHelper t){var p=player(t);var s=held(p,Quest185Content.TRACKER.get(),1);var before=Quest185Story.data(p).copy();deny(p,Quest185Content.TRACKER_EFFECT,()->{var r=use(p);t.assertTrue(r.getResult()==InteractionResult.FAIL&&Quest185Story.data(p).getLong("TrackerReady")==before.getLong("TrackerReady")&&!p.hasEffect(Quest185Content.TRACKER_EFFECT),"rejected tracker spent cooldown");});t.succeed();}
    @GameTest(template="empty") public static void rejectedSpringTearDoesNotConsume(GameTestHelper t){var p=player(t);var s=held(p,SanctuaryContent.item("spring_tear"),3);deny(p,SanctuaryContent.TEAR,()->{s.getItem().finishUsingItem(s,p.level(),p);t.assertTrue(s.getCount()==3&&!p.getCooldowns().isOnCooldown(s.getItem())&&org.slavicmyths.rpg.PathData.data(p).getLong("Sanctuary193TearReady")==0,"rejected tear consumed item/cooldown");});t.succeed();}
    @GameTest(template="empty") public static void successfulWardsRepeatAndSpecialWard(GameTestHelper t){
        var p=player(t);var s=held(p,Quest185Content.WARD.get(),3);use(p);t.assertTrue(p.hasEffect(Quest185Content.WARD_EFFECT)&&s.getCount()==2,"ward success");use(p);t.assertTrue(s.getCount()==2,"active ward consumed again");p.removeEffect(Quest185Content.WARD_EFFECT);
        var data=new net.minecraft.nbt.CompoundTag();data.putBoolean("GuardianWard196",true);s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));use(p);t.assertTrue(p.hasEffect(Quest185Content.WARD_EFFECT)&&s.getCount()==2,"unique reusable ward consumed");
        s=held(p,SanctuaryContent.item("fading_flame_charm"),3);use(p);t.assertTrue(p.hasEffect(SanctuaryContent.GUARD)&&s.getCount()==2,"flame charm success");use(p);t.assertTrue(s.getCount()==2,"active flame charm consumed twice");t.succeed();
    }
    @GameTest(template="empty") public static void morFlaskSuccessfulUseAndNativeCooldown(GameTestHelper t){
        var p=player(t);var s=held(p,ModItems.MOR_FLASK.get(),3);var w=t.getLevel();
        int before=w.getEntitiesOfClass(org.slavicmyths.mor.MorFlaskProjectile.class,p.getBoundingBox().inflate(4)).size();
        p.gameMode.useItem(p,w,s,InteractionHand.MAIN_HAND);p.gameMode.useItem(p,w,s,InteractionHand.MAIN_HAND);
        int after=w.getEntitiesOfClass(org.slavicmyths.mor.MorFlaskProjectile.class,p.getBoundingBox().inflate(4)).size();t.assertTrue(s.getCount()==2&&after==before+1&&p.getCooldowns().isOnCooldown(s.getItem()),"native cooldown or consumption");t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=140) public static void gusliCalmRangeImmunityExpiryAndCancel(GameTestHelper t){
        var p=player(t);var s=held(p,ModItems.GUSLI.get(),1);var w=t.getLevel();
        var near=EntityType.ZOMBIE.create(w);near.setNoAi(true);near.moveTo(p.position().add(2,0,0));w.addFreshEntity(near);near.setTarget(p);
        var immune=EntityType.ZOMBIE.create(w);immune.setNoAi(true);immune.addTag("slavic_music_immune");immune.moveTo(p.position().add(0,0,3));w.addFreshEntity(immune);
        var far=EntityType.ZOMBIE.create(w);far.setNoAi(true);far.moveTo(p.position().add(13,0,0));w.addFreshEntity(far);
        use(p);for(int at:new int[]{10,20,30})t.runAtTickTime(at,()->s.getItem().onUseTick(w,p,s,72000-at));
        t.runAtTickTime(31,()->{t.assertTrue(ArtifactEvents.calm(near)&&near.getTarget()==null,"eligible mob not calmed");t.assertTrue(!ArtifactEvents.calm(immune)&&!ArtifactEvents.calm(far),"immune/range");s.getItem().releaseUsing(s,w,p,71970);t.assertTrue(p.getCooldowns().isOnCooldown(s.getItem()),"full play cooldown");});
        t.runAtTickTime(100,()->{t.assertTrue(!ArtifactEvents.calm(near),"calm never expired");near.discard();immune.discard();far.discard();t.succeed();});
    }
    @GameTest(template="empty") public static void skatertPortionsSingleReturnAndRest(GameTestHelper t){
        var p=player(t);var w=t.getLevel();var support=BlockPos.containing(p.position()).below();w.setBlockAndUpdate(support,Blocks.STONE.defaultBlockState());var pos=support.above();
        var s=held(p,ModItems.SKATERT.get(),1);var hit=new BlockHitResult(Vec3.atCenterOf(support),Direction.UP,support,false);var r=s.getItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit));t.assertTrue(r.consumesAction()&&s.isEmpty(),"cloth placement");
        var state=w.getBlockState(pos);t.assertTrue(state.getBlock() instanceof SkatertBlock&&state.getValue(SkatertBlock.PORTIONS)==6&&w.getBlockEntity(pos) instanceof SkatertTile,"real cloth state/tile");
        p.getFoodData().setFoodLevel(0);var block=(SkatertBlock)state.getBlock();block.useWithoutItem(state,w,pos,p,hit);t.assertTrue(p.getFoodData().getFoodLevel()==6&&w.getBlockState(pos).getValue(SkatertBlock.PORTIONS)==5,"eat portion");
        block.useWithoutItem(w.getBlockState(pos),w,pos,p,hit);t.assertTrue(w.getBlockState(pos).getValue(SkatertBlock.PORTIONS)==5,"eat cooldown");
        p.setShiftKeyDown(true);block.useWithoutItem(w.getBlockState(pos),w,pos,p,hit);w.removeBlock(pos,false);
        var drops=w.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(2),e->e.getItem().is(ModItems.SKATERT.get()));t.assertTrue(drops.size()==1&&drops.getFirst().getItem().getCount()==1,"duplicate cloth drop");
        var returned=drops.getFirst().getItem();t.assertTrue(org.slavicmyths.item.ItemState.clothReady(returned)>ArtifactEvents.now(w),"rest timer missing");p.setItemInHand(InteractionHand.MAIN_HAND,returned.copy());t.assertTrue(returned.getItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit))==InteractionResult.FAIL,"rest bypass");drops.forEach(Entity::discard);t.succeed();
    }
    @GameTest(template="empty") public static void flightOwnerPestleCargoAndSinglePack(GameTestHelper t){
        var p=player(t);var other=player(t);var w=t.getLevel();var v=org.slavicmyths.registry.ModEntities.FLYING_MORTAR.get().create(w);v.fromItem(new ItemStack(ModItems.FLYING_MORTAR.get()),p.getUUID());v.moveTo(p.position());w.addFreshEntity(v);v.cargo.setItem(0,new ItemStack(Items.DIAMOND,3));
        v.interact(p,InteractionHand.MAIN_HAND);t.assertTrue(v.pilot()==p,"owner cannot mount");v.input(other,1,0,1,0,false);v.tick();t.assertTrue(v.getDeltaMovement().horizontalDistanceSqr()==0,"outsider controls mortar");
        v.input(p,1,0,0,0,false);v.tick();t.assertTrue(v.getDeltaMovement().horizontalDistanceSqr()==0,"mortar without pestle");held(p,ModItems.PESTLE.get(),1);v.input(p,1,0,0,0,false);v.tick();t.assertTrue(v.getDeltaMovement().horizontalDistanceSqr()>0,"pestle drive missing");
        t.assertTrue(!v.hurt(p.damageSources().playerAttack(p),1),"packed occupied vessel");p.stopRiding();p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);t.assertTrue(!v.hurt(other.damageSources().playerAttack(other),1),"outsider packed vessel");t.assertTrue(v.hurt(p.damageSources().playerAttack(p),1)&&v.isRemoved(),"owner pack");t.assertTrue(!v.hurt(p.damageSources().playerAttack(p),1),"repeat pack");
        var packed=p.getInventory().items.stream().filter(x->x.is(ModItems.FLYING_MORTAR.get())).findFirst().orElseThrow();var cargo=org.slavicmyths.item.ItemState.inventory(packed,true,w.registryAccess());t.assertTrue(cargo.getFirst().is(Items.DIAMOND)&&cargo.getFirst().getCount()==3,"cargo lost");t.succeed();
    }
    @GameTest(template="empty") public static void storyCallsCannotBypassIntroductions(GameTestHelper t){var p=player(t);for(String hero:List.of("ilya_muromets","dobrynya_nikitich","alyosha_popovich","finist"))t.assertTrue(!org.slavicmyths.skim.HeroCalls.toggle(p,hero)&&!org.slavicmyths.skim.HeroCalls.map(t.getLevel(),hero).containsKey(p.getUUID()),"unintroduced hero bypass "+hero);t.assertTrue(!WolfSupport.summon(p),"locked wolf bypass");held(p,item("sivka_whistle"),1);t.assertTrue(use(p).getResult()==InteractionResult.FAIL,"unbound Sivka bypass");t.succeed();}
    @GameTest(template="empty") public static void breathBottlesActualUpgradeAndRemainders(GameTestHelper t){
        var stacks=new ArrayList<ItemStack>();for(Item i:List.of(GorynychContent.item("serpent_crossbow"),GorynychContent.item("gorynych_scale"),GorynychContent.item("ancient_serpent_steel"),GorynychContent.bottle(0),GorynychContent.bottle(1),GorynychContent.bottle(2)))stacks.add(new ItemStack(i));
        var input=CraftingInput.of(3,2,stacks);var recipe=new GorynychUpgrade(CraftingBookCategory.EQUIPMENT);t.assertTrue(recipe.matches(input,t.getLevel()),"three-breath upgrade missing");t.assertTrue(recipe.assemble(input,t.getLevel().registryAccess()).is(GorynychContent.item("three_breath_crossbow")),"upgrade output");
        var remains=recipe.getRemainingItems(input);t.assertTrue(remains.stream().filter(s->s.is(Items.GLASS_BOTTLE)).count()==3,"glass remainder");stacks.set(5,ItemStack.EMPTY);t.assertTrue(!recipe.matches(CraftingInput.of(3,2,stacks),t.getLevel()),"missing breath accepted");t.succeed();
    }
    @GameTest(template="empty") public static void threeBreathImpactEffectsAndStormRepeatGuard(GameTestHelper t){
        var p=player(t);var w=t.getLevel();var z=EntityType.ZOMBIE.create(w);z.setNoAi(true);z.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);z.setHealth(100);z.moveTo(p.position().add(0,0,2));w.addFreshEntity(z);var at=z.position();
        GorynychEquipment.burst(p,at,1,3);t.assertTrue(z.getHealth()<100&&z.isOnFire(),"flame impact");z.clearFire();z.invulnerableTime=0;float hp=z.getHealth();StormCrossbow.burst(p,at);t.assertTrue(z.getHealth()<hp&&z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"storm impact");hp=z.getHealth();z.invulnerableTime=0;StormCrossbow.burst(p,at);t.assertTrue(z.getHealth()==hp,"storm per-target repeat bypass");
        w.addNewPlayer(p);try{GorynychEquipment.burst(p,at,0,3);var clouds=w.getEntitiesOfClass(BreathCloud.class,new AABB(at,at).inflate(2));t.assertTrue(!clouds.isEmpty(),"mor cloud absent");var cloud=clouds.getFirst();cloud.tickCount=19;z.invulnerableTime=0;w.tickNonPassenger(cloud);t.assertTrue(z.hasEffect(MobEffects.WEAKNESS),"mor weakness");for(int i=0;i<80&&!cloud.isRemoved();i++)w.tickNonPassenger(cloud);t.assertTrue(cloud.isRemoved(),"mor cloud lifetime");z.discard();clouds.forEach(Entity::discard);}finally{w.removePlayerImmediately(p,Entity.RemovalReason.DISCARDED);}t.succeed();
    }
    @GameTest(template="empty") public static void trackerTearSuccessRepeatAndSavedCooldown(GameTestHelper t){
        var p=player(t);held(p,Quest185Content.TRACKER.get(),1);use(p);long ready=Quest185Story.data(p).getLong("TrackerReady");
        t.assertTrue(p.hasEffect(Quest185Content.TRACKER_EFFECT)&&ready==Quest185Story.now(p)+12000,"tracker success");
        p.removeEffect(Quest185Content.TRACKER_EFFECT);t.assertTrue(use(p).getResult()==InteractionResult.FAIL&&Quest185Story.data(p).getLong("TrackerReady")==ready,"tracker repeat cooldown");
        var saved=new net.minecraft.nbt.CompoundTag();p.saveWithoutId(saved);var restored=player(t);restored.load(saved);t.assertTrue(Quest185Story.data(restored).getLong("TrackerReady")==ready,"native player data lost cooldown");
        var s=held(p,SanctuaryContent.item("spring_tear"),3);s.getItem().finishUsingItem(s,p.level(),p);t.assertTrue(s.getCount()==2&&p.hasEffect(SanctuaryContent.TEAR)&&p.getCooldowns().isOnCooldown(s.getItem()),"tear success");s.getItem().finishUsingItem(s,p.level(),p);t.assertTrue(s.getCount()==2,"tear repeat consumed again");t.succeed();
    }
    @GameTest(template="empty") public static void bottlesCollectedOnceAndNotDirectWeapons(GameTestHelper t){
        var p=player(t);var w=t.getLevel();var boss=GorynychContent.BOSS.get().create(w);
        for(int type=0;type<3;type++){
            var cloud=GorynychContent.CLOUD.get().create(w);cloud.configure(boss,type,p.position(),3);w.addFreshEntity(cloud);var glass=held(p,Items.GLASS_BOTTLE,3);
            for(int i=0;i<3;i++)t.assertTrue(cloud.interact(p,InteractionHand.MAIN_HAND)==InteractionResult.SUCCESS,"collection failed");
            t.assertTrue(glass.isEmpty()&&cloud.isRemoved()&&cloud.uses()==0,"collection budget");
            int count=p.getInventory().items.stream().filter(s->s.is(GorynychContent.bottle(cloud.type()))).mapToInt(ItemStack::getCount).sum();
            t.assertTrue(count==3&&cloud.interact(p,InteractionHand.MAIN_HAND)==InteractionResult.PASS,"repeated collection duplicated bottle");
            var s=held(p,GorynychContent.bottle(type),1);t.assertTrue(use(p).getResult()==InteractionResult.PASS&&s.getCount()==1,"ingredient became direct weapon");
        }boss.discard();t.succeed();
    }
    @GameTest(template="empty") public static void broomPlacementCollisionOwnerAndOneSeat(GameTestHelper t){
        var p=player(t);var other=player(t);var w=t.getLevel();var support=p.blockPosition().below();var pos=support.above();w.setBlockAndUpdate(support,Blocks.STONE.defaultBlockState());w.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        var s=held(p,ModItems.FLYING_BROOM.get(),1);var hit=new BlockHitResult(Vec3.atCenterOf(support),Direction.UP,support,false);var c=new UseOnContext(p,InteractionHand.MAIN_HAND,hit);
        t.assertTrue(s.getItem().useOn(c)==InteractionResult.FAIL&&s.getCount()==1,"blocked placement consumed broom");w.removeBlock(pos,false);
        t.assertTrue(s.getItem().useOn(c).consumesAction()&&s.isEmpty(),"broom placement");var v=w.getEntitiesOfClass(FlyingVessel.class,new AABB(pos).inflate(2),x->!x.mortar).getFirst();
        v.interact(p,InteractionHand.MAIN_HAND);v.interact(other,InteractionHand.MAIN_HAND);t.assertTrue(v.pilot()==p&&v.getPassengers().size()==1,"broom ownership/one seat");p.stopRiding();v.discard();t.succeed();
    }
    @GameTest(template="empty") public static void introducedHeroesSingleCanonicalEntityAndRecall(GameTestHelper t){
        var p=player(t);var w=t.getLevel();var base=p.blockPosition().below();for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)w.setBlockAndUpdate(base.offset(x,0,z),Blocks.STONE.defaultBlockState());
        for(String hero:List.of("ilya_muromets","dobrynya_nikitich","alyosha_popovich","finist")){
            // Controlled precondition, not a claim of completing the natural introduction route.
            org.slavicmyths.bogatyr.HeroIntroductions.state(p,hero).putBoolean("Helped",true);
            t.assertTrue(org.slavicmyths.skim.HeroCalls.toggle(p,hero),"introduced summon "+hero);var map=org.slavicmyths.skim.HeroCalls.map(w,hero);var uuid=map.get(p.getUUID());var entity=(LivingEntity)w.getEntity(uuid);t.assertTrue(entity!=null,"canonical entity absent");
            t.assertTrue(org.slavicmyths.skim.HeroCalls.toggle(p,hero),"recall "+hero);t.assertTrue(!org.slavicmyths.skim.HeroCalls.toggle(p,hero)&&uuid.equals(map.get(p.getUUID())),"duplicate during recall "+hero);
            org.slavicmyths.skim.HeroCalls.retire(entity);t.assertTrue(!map.containsKey(p.getUUID())&&!org.slavicmyths.skim.HeroCalls.available(p,hero),"retire/cooldown "+hero);
        }t.succeed();
    }
    @GameTest(template="empty") public static void morFlaskRealProjectileTransformsEligibleSpirit(GameTestHelper t){
        var p=player(t);var w=t.getLevel();var floor=p.blockPosition().below();for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)w.setBlockAndUpdate(floor.offset(x,0,z),Blocks.STONE.defaultBlockState());
        var spirit=org.slavicmyths.registry.ModEntities.LESHY.get().create(w);spirit.setNoAi(true);spirit.moveTo(Vec3.atBottomCenterOf(floor.above()).add(1,0,0));w.addFreshEntity(spirit);
        var flask=org.slavicmyths.mor.MorContent.FLASK.get().create(w);flask.setOwner(p);flask.setPos(Vec3.atCenterOf(floor).add(0,2,0));flask.setDeltaMovement(0,-1,0);w.addFreshEntity(flask);
        for(int i=0;i<8&&!flask.isRemoved();i++)w.tickNonPassenger(flask);
        t.assertTrue(flask.isRemoved()&&spirit.morState()==1&&spirit.morRemaining()==50,"flask failed to begin Mor transformation");spirit.discard();t.succeed();
    }
}
