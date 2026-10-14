package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.karachun.*;

/** Actual server world + native LivingEntity.hurt. Test transport never enters JAR. */
@GameTestHolder("slavicmyths_scythe") @PrefixGameTestTemplate(false)
public final class ScytheGameTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel = new io.netty.channel.embedded.EmbeddedChannel();
        Connection() { super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND); }
        @Override public io.netty.channel.Channel channel() { return channel; }
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p) { super(p.server, new Connection(), p, net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(), false)); }
        @Override public void send(net.minecraft.network.protocol.Packet<?> p) { }
        @Override public void send(net.minecraft.network.protocol.Packet<?> p, net.minecraft.network.PacketSendListener l) { }
    }
    @SuppressWarnings("unchecked")
    private static Map<UUID, ServerPlayer> online(ServerPlayer p) {
        try {
            var f = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
            f.setAccessible(true); return (Map<UUID, ServerPlayer>) f.get(p.server.getPlayerList());
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    private static ServerPlayer player(GameTestHelper t) {
        var w=t.getLevel();
        var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ScytheQA"),ClientInformation.createDefault());
        p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);
        p.moveTo(t.absoluteVec(new Vec3(8,2,8)));p.setYRot(0);p.setXRot(0);p.setOnGround(true);
        w.addNewPlayer(p);online(p).put(p.getUUID(),p);
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(NightContent.SCYTHE.get()));p.doTick();p.attackStrengthTicker=1000;
        return p;
    }
    private static <T extends Mob> T target(GameTestHelper t,ServerPlayer p,EntityType<T> type,Vec3 offset) {
        T mob=type.create(t.getLevel());mob.moveTo(p.position().add(offset));mob.setNoAi(true);mob.setNoGravity(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setHealth(100);
        t.getLevel().addFreshEntity(mob);return mob;
    }
    private static void ready(ServerPlayer p) {
        NightCombat.cancel(p);NightLedger.get(p.serverLevel()).player(p.getUUID()).putLong("SwingReady",0);
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(NightContent.SCYTHE.get()));p.doTick();p.attackStrengthTicker=1000;
    }
    private static void hit(ServerPlayer p,LivingEntity t) { t.invulnerableTime=0;NightCombat.damage(p,t,p.getMainHandItem(),1); }

    @GameTest(template="empty",timeoutTicks=180)
    public static void scythe(GameTestHelper t) {
        List<String> evidence=new ArrayList<>();
        java.util.function.BiConsumer<Boolean,String> check=(ok,label)->{t.assertTrue(ok,label);evidence.add(label);System.out.println("SCYTHE_CHECK "+label);};
        var p=player(t);p.setHealth(10);
        var cow=target(t,p,EntityType.COW,new Vec3(0,0,2));
        var zombie=target(t,p,EntityType.ZOMBIE,new Vec3(1,0,2));
        var behind=target(t,p,EntityType.COW,new Vec3(0,0,-2));
        var outside=target(t,p,EntityType.COW,new Vec3(0,0,4.5));
        var blocked=target(t,p,EntityType.COW,new Vec3(-2.5,0,2.8));
        for(int x=-2;x<=-1;x++)for(int y=0;y<=2;y++)t.getLevel().setBlockAndUpdate(p.blockPosition().offset(x,y,1),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        check.accept(!p.hasLineOfSight(blocked),"Fixture wall actually blocks native line of sight before attacking");
        var villager=target(t,p,EntityType.VILLAGER,new Vec3(1.6,0,1.2));
        var pet=target(t,p,EntityType.WOLF,new Vec3(.8,0,.8));pet.tame(p);final float petHealth=pet.getHealth();
        check.accept(Math.abs(p.getAttributeValue(Attributes.ATTACK_DAMAGE)-12)<.01,"Native equipped attack attribute = 12 HP (observed="+p.getAttributeValue(Attributes.ATTACK_DAMAGE)+")");
        NightCombat.action(p,0);
        check.accept(p.getPersistentData().contains(NightCombat.KEY),"Server starts delayed swing");
        check.accept(cow.getHealth()==100,"No instant/pre-windup damage");
        t.runAfterDelay(7,()->{
            check.accept(Math.abs(cow.getHealth()-88)<.05,"Real arc decreases cow HP: 100 -> 88");
            check.accept(Math.abs(zombie.getHealth()-85)<.05,"Real arc decreases undead HP: 100 -> 85 (+25%)");
            check.accept(behind.getHealth()==100,"Behind-sector target untouched");
            check.accept(villager.getHealth()==100&&pet.getHealth()==petHealth,"Protected villager and owned wolf untouched (villager="+villager.getHealth()+", pet="+pet.getHealth()+"/"+petHealth+")");
            check.accept(Math.abs(p.getHealth()-12)<.05,"Life Eater heals 2 HP once across multiple targets");
            check.accept(outside.getHealth()==100,"Arc respects 3.5-block bounds");
            check.accept(blocked.getHealth()==100,"Solid block prevents arc damage through line of sight");
            for(int x=-2;x<=-1;x++)for(int y=0;y<=2;y++)t.getLevel().setBlockAndUpdate(p.blockPosition().offset(x,y,1),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            float healed=p.getHealth();hit(p,cow);hit(p,cow);
            check.accept(cow.hasEffect(MobEffects.WITHER)&&cow.getEffect(MobEffects.WITHER).getAmplifier()==0&&cow.getEffect(MobEffects.WITHER).getDuration()==80,"Third same-target hit applies Wither I / 80 ticks once");
            check.accept(p.getHealth()==healed,"Life Eater cooldown prevents repeated healing");
            zombie.setHealth(1);hit(p,zombie);check.accept(!zombie.isAlive()&&Math.abs(p.getHealth()-15)<.05,"Undead kill heals exactly 3 HP");
            cow.removeAllEffects();cow.setHealth(100);p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,0));
            hit(p,cow);check.accept(Math.abs(cow.getHealth()-85)<.05,"Strength I changes actual HP loss from 12 to 15");
            p.removeEffect(MobEffects.DAMAGE_BOOST);p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200,0));cow.setHealth(100);
            hit(p,cow);check.accept(Math.abs(cow.getHealth()-92)<.05,"Weakness I changes actual HP loss to 8");p.removeEffect(MobEffects.WEAKNESS);
            cow.removeAllEffects();cow.setHealth(100);cow.getAttribute(Attributes.ARMOR).setBaseValue(20);
            hit(p,cow);float armored=100-cow.getHealth();check.accept(armored>0&&armored<12,"Armor reduces actual HP loss without zeroing the attack");cow.getAttribute(Attributes.ARMOR).setBaseValue(0);
            var sharp=p.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS);
            p.getMainHandItem().enchant(sharp,3);cow.setHealth(100);cow.removeAllEffects();hit(p,cow);
            check.accept(Math.abs(cow.getHealth()-86)<.05,"Sharpness III passes through native modifyDamage: 14 HP");
            ready(p);cow.removeAllEffects();cow.setHealth(100);
            try {
                var field=NightConfig.class.getDeclaredField("VALUES");field.setAccessible(true);
                var value=(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue)((Map<?,?>)field.get(null)).get("baseDamage");
                double original=value.get();try {value.set(0d);hit(p,cow);check.accept(cow.getHealth()==100,"Configured baseDamage=0 is an intentional zero, not missing attributes");}finally{value.set(original);}
            } catch(ReflectiveOperationException e) {throw new RuntimeException(e);}
            cow.setInvulnerable(true);hit(p,cow);check.accept(cow.getHealth()==100,"Invulnerable target is intentionally protected");cow.setInvulnerable(false);
            UUID cast=UUID.randomUUID();check.accept(NightCombat.claim(p,cast,cow.getUUID())&&!NightCombat.claim(p,cast,cow.getUUID()),"Three harvest lanes share a per-target cast ledger");
            for(int combo=0;combo<3;combo++) {
                int windup=combo==2?7:5,duration=combo==2?20:combo==1?15:14;
                float previous=0;
                for(int i=0;i<=duration*100;i++) {
                    float age=i/100F,value=ScytheSwingPose.motion(age,windup,duration);
                    t.assertTrue(Float.isFinite(value)&&Math.abs(value-previous)<.02,"Continuous swing sample");previous=value;
                }
                check.accept(ScytheSwingPose.motion(0,windup,duration)==0&&ScytheSwingPose.motion(duration,windup,duration)==0,"Combo "+combo+" returns exactly to neutral without hit-time jump");
            }
            ready(p);NightCombat.action(p,0);p.getInventory().selected=1;NightCombat.tick(p);
            check.accept(!p.getPersistentData().contains(NightCombat.KEY),"Slot change cancels server swing before damage");p.getInventory().selected=0;
            ready(p);NightCombat.action(p,0);p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);NightCombat.tick(p);
            check.accept(!p.getPersistentData().contains(NightCombat.KEY),"Dropping/removing the scythe cancels swing");
            ready(p);NightCombat.action(p,0);p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(NightContent.SCYTHE.get()));NightCombat.tick(p);
            check.accept(!p.getPersistentData().contains(NightCombat.KEY),"Replacing with another scythe stack cancels swing");
            ready(p);NightCombat.action(p,0);NightEvents.leave(p);
            check.accept(!p.getPersistentData().contains(NightCombat.KEY)&&!p.getPersistentData().contains("Night192Casts")&&!p.getPersistentData().contains("Night192ComboTime")&&!p.getPersistentData().contains("Night192Combo"),"Logout/death/dimension cleanup removes transient cast and combo state");
            ready(p);NightCombat.action(p,0);NightCombat.action(p,0);
            check.accept(p.getPersistentData().getCompound(NightCombat.KEY).getInt("Mode")==0,"Repeated input retains one active swing");
            cow.removeAllEffects();cow.moveTo(p.position().add(20,0,0));
            t.runAfterDelay(25,()->{
                check.accept(!p.getPersistentData().contains(NightCombat.KEY),"Miss expires naturally through server ticks");
                ready(p);p.setXRot(15);
                var ranged=target(t,p,EntityType.COW,new Vec3(0,0,3));NightCombat.action(p,2);
                t.runAfterDelay(12,()->check.accept(t.getLevel().getEntitiesOfClass(NightBlade.class,p.getBoundingBox().inflate(16),b->b.getOwner()==p).size()==3,"Black Harvest launches three real projectiles after windup"));
                t.runAfterDelay(20,()->{
                    check.accept(Math.abs(ranged.getHealth()-86.8)<.08,"Black Harvest physical collision deals 13.2 HP once");
                    NightCombat.cancel(p);NightCombat.action(p,2);check.accept(!p.getPersistentData().contains(NightCombat.KEY),"Harvest cooldown refuses another cast");
                    p.setXRot(0);ranged.removeAllEffects();ranged.setHealth(100);ranged.invulnerableTime=0;NightCombat.action(p,1);
                    t.runAfterDelay(12,()->{
                        check.accept(Math.abs(ranged.getHealth()-83.2)<.08,"Last Breath delayed area hit deals 16.8 HP");
                        check.accept(villager.getHealth()==100&&pet.getHealth()==petHealth,"Both special abilities preserve NPC/pet protection");
                        check.accept(p.getMainHandItem().getDamageValue()>0,"Successful special attacks consume real durability");
                        NightCombat.cancel(p);NightCombat.action(p,1);check.accept(!p.getPersistentData().contains(NightCombat.KEY),"Breath cooldown refuses another cast");
                        ranged.discard();
                        online(p).remove(p.getUUID());p.discard();cow.discard();behind.discard();outside.discard();blocked.discard();villager.discard();pet.discard();
                        try {
                            var path=java.nio.file.Path.of(System.getProperty("slavicmyths.scytheRoot"),"docs/weapon-stage-a/server-evidence.json");
                            var out=new com.google.gson.JsonObject();out.addProperty("status","PASS");out.addProperty("scope","actual GameTest server; fake packet transport; no client or two-player session");
                            out.add("checks",new com.google.gson.Gson().toJsonTree(evidence));out.addProperty("client_starts",0);
                            List<Map<String,Object>> samples=new ArrayList<>();
                            for(int combo=0;combo<3;combo++) {
                                int windup=combo==2?7:5,duration=combo==2?20:combo==1?15:14;
                                for(int age=0;age<=duration;age++)samples.add(Map.of("combo",combo,"age",age,"duration",duration,"motion",ScytheSwingPose.motion(age,windup,duration),"influence",ScytheSwingPose.influence(age,duration)));
                            }
                            out.add("pose_samples",new com.google.gson.Gson().toJsonTree(samples));
                            java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(out)+"\n");
                        } catch(java.io.IOException e) { throw new RuntimeException(e); }
                        System.out.println("SCYTHE_SERVER_PASS assertions="+evidence.size());t.succeed();
                    });
                });
            });
        });
    }
}
