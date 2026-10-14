package org.slavicmyths.verify;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.gorynych.GorynychContent;
import org.slavicmyths.gorynych.CrossbowDamage;
import java.util.UUID;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.EquipmentSlot;
import org.slavicmyths.gorynych.SerpentCrossbow;
import org.slavicmyths.morozko.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;

/** Native hurt/onHitEntity baseline, deliberately separate from physics and client acceptance. */
@GameTestHolder("slavicmyths_rewards") @PrefixGameTestTemplate(false)
public final class RewardIssueTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
    }
    private static final class HitArrow extends Arrow {
        HitArrow(GameTestHelper t, LivingEntity owner, ItemStack weapon) {
            super(t.getLevel(), owner, new ItemStack(Items.ARROW), weapon);
            setBaseDamage(2); setDeltaMovement(0, 0, 3); setCritArrow(false);
        }
        void hit(LivingEntity target) { onHitEntity(new EntityHitResult(target)); }
    }
    @GameTest(template="empty") public static void crossbowBaseline(GameTestHelper t) {
        var owner=EntityType.ZOMBIE.create(t.getLevel());
        var target=EntityType.ZOMBIE.create(t.getLevel());
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); target.setHealth(100);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        ItemStack weapon=new ItemStack(GorynychContent.ITEMS.get("serpent_crossbow").get());
        for(int i=0;i<3;i++) new HitArrow(t,owner,weapon).hit(target);
        System.out.println("REWARD_BASELINE native arrows=3 HP=100->"+target.getHealth()+" invulnerableTime="+target.invulnerableTime);
        t.assertTrue(target.getHealth()==94,"Baseline: equal follow-up arrows are rejected by native hurt");
        t.succeed();
    }
    private static net.minecraft.world.entity.monster.Zombie target(GameTestHelper t) {
        var mob=EntityType.ZOMBIE.create(t.getLevel());
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.setHealth(100);
        mob.getAttribute(Attributes.ARMOR).setBaseValue(0);return mob;
    }
    private static HitArrow shot(GameTestHelper t,LivingEntity owner,ItemStack weapon,UUID volley) {
        var arrow=new HitArrow(t,owner,weapon);arrow.getPersistentData().putUUID(CrossbowDamage.VOLLEY,volley);return arrow;
    }
    @GameTest(template="empty") public static void scopedVolley(GameTestHelper t) {
        for(String id:new String[]{"serpent_crossbow","three_breath_crossbow"}) {
            var owner=target(t);var mob=target(t);var weapon=new ItemStack(GorynychContent.ITEMS.get(id).get());
            var volley=UUID.randomUUID();var first=shot(t,owner,weapon,volley);
            first.hit(mob);shot(t,owner,weapon,volley).hit(mob);shot(t,owner,weapon,volley).hit(mob);
            t.assertTrue(mob.getHealth()==82,id+": three native impacts must each deal 6");
            first.hit(mob);t.assertTrue(mob.getHealth()==82,"duplicate projectile must not hurt twice");
            new HitArrow(t,owner,new ItemStack(Items.CROSSBOW)).hit(mob);
            t.assertTrue(mob.getHealth()==82,"vanilla arrow retains cooldown");
            shot(t,owner,weapon,UUID.randomUUID()).hit(mob);
            t.assertTrue(mob.getHealth()==82,"different volley retains cooldown");
            shot(t,target(t),weapon,volley).hit(mob);
            t.assertTrue(mob.getHealth()==82,"different owner retains cooldown");
            for(int i=0;i<3;i++){var separate=target(t);shot(t,owner,weapon,volley).hit(separate);t.assertTrue(separate.getHealth()==94,"each separate target takes one hit");}
            System.out.println("REWARD_AFTER "+id+" native impacts=3 HP=100->82 duplicate/vanilla/other-volley/other-owner unchanged");
        }
        t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100) public static void nativePhysicsVolley(GameTestHelper t) {
        var owner=target(t);var mob=target(t);
        owner.moveTo(t.absoluteVec(new Vec3(1,2,1)));mob.moveTo(t.absoluteVec(new Vec3(1,2,6)));
        owner.setNoAi(true);mob.setNoAi(true);owner.setNoGravity(true);mob.setNoGravity(true);
        mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        t.getLevel().addFreshEntity(owner);t.getLevel().addFreshEntity(mob);
        var weapon=new ItemStack(GorynychContent.ITEMS.get("serpent_crossbow").get());
        owner.setItemSlot(EquipmentSlot.MAINHAND,weapon);
        weapon.set(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.of(List.of(new ItemStack(Items.ARROW),new ItemStack(Items.ARROW),new ItemStack(Items.ARROW))));
        ((SerpentCrossbow)weapon.getItem()).performShooting(t.getLevel(),owner,InteractionHand.MAIN_HAND,weapon,3.15f,0,mob);
        var arrows=t.getLevel().getEntitiesOfClass(AbstractArrow.class,owner.getBoundingBox().inflate(3),a->a.getOwner()==owner);
        t.assertTrue(arrows.size()==3,"production firing creates three native projectiles");
        // Aim all three at one target to isolate real collision/HP from fan spacing.
        var origin=owner.getEyePosition();var direction=mob.getBoundingBox().getCenter().subtract(origin).normalize();
        for(var arrow:arrows){arrow.setPos(origin);arrow.shoot(direction.x,direction.y,direction.z,3.15f,0);arrow.setCritArrow(false);}
        t.runAfterDelay(8,()->{
            t.assertTrue(mob.getHealth()==76,"three production arrows must really collide and deal 8 each, got "+mob.getHealth());
            t.assertTrue(arrows.stream().allMatch(Entity::isRemoved),"accepted nonpiercing arrows disappear, no reverse deflection");
            t.assertTrue(weapon.getDamageValue()==3,"native durability cost remains three");
            System.out.println("REWARD_PHYSICS serpent_crossbow production arrows=3 HP=100->"+mob.getHealth()+" durability="+weapon.getDamageValue());
            t.succeed();
        });
    }
    @GameTest(template="empty") public static void frostBaseline(GameTestHelper t) {
        var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FrostBaseline"),ClientInformation.createDefault());
        p.connection=new Listener(p);
        var staff=new ItemStack(FrostContent.STAFF.get());p.setItemSlot(EquipmentSlot.MAINHAND,staff);
        t.assertTrue(staff.getUseDuration(p)==60,"declared stream lasts exactly three seconds");
        t.assertTrue(FrostSkills.startStream(p),"first use starts");p.startUsingItem(InteractionHand.MAIN_HAND);
        p.getPersistentData().putLong("FrostStreamHeartbeat",FrostSkills.now(p)-8);
        FrostSkills.streamTick(p,52);
        t.assertTrue(!p.getPersistentData().getBoolean("FrostStreaming")&&!p.isUsingItem(),"8 tick heartbeat gap stops use");
        long remaining=FrostSkills.data(p).getLong("StreamReady")-FrostSkills.now(p);
        t.assertTrue(remaining==200,"early interruption starts entire ten-second cooldown");
        t.assertTrue(!FrostSkills.startStream(p),"fast repeated use rejected");
        System.out.println("REWARD_FROST_BASELINE duration=60 gap=8 interruption=true cooldown="+remaining+"; native methods, not network lag simulation");
        t.succeed();
    }
}
