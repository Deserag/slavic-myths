package org.slavicmyths.desert;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.combat.IndependentWeaponCooldowns;
import org.slavicmyths.combat.WeaponInputHandler.Action;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.PathData;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class HealingStaffGameTests {
    private static final class ProbeConnection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        ProbeConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class ProbeListener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        ProbeListener(ServerPlayer p){super(p.getServer(),new ProbeConnection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
    }
    private static ServerPlayer player(GameTestHelper h){
        var w=h.getLevel();
        var p=new ServerPlayer(w.getServer(),w,new GameProfile(UUID.randomUUID(),"HealingProbe"),ClientInformation.createDefault());
        p.connection=new ProbeListener(p);p.setGameMode(GameType.SURVIVAL);
        p.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2))));
        p.setNoGravity(true);p.setHealth(5);return p;
    }
    private static ItemStack equip(ServerPlayer p,boolean fire){
        var stack=new ItemStack(fire?ModItems.FIRE_HEALING_STAFF.get():ModItems.ICE_HEALING_STAFF.get());
        p.setItemSlot(EquipmentSlot.MAINHAND,stack);return p.getMainHandItem();
    }
    @GameTest(template="empty") public static void healingStaffSelfAndSharedCooldown(GameTestHelper h){
        var p=player(h);var ice=equip(p,false);var item=(HealingStaffItem)ice.getItem();
        h.assertTrue(ice.getMaxDamage()==512,"durability");
        h.assertTrue(!item.intercepts(Action.LEFT_CLICK)&&item.intercepts(Action.RIGHT_CLICK),"input contract");
        item.onWeaponAction(p,ice,Action.RIGHT_CLICK);
        h.assertTrue(Math.abs(p.getHealth()-13)<.01,"ice heal must be eight HP");
        h.assertTrue(ice.getDamageValue()==1,"durability cost");
        h.assertTrue(PathData.data(p).getLong(HealingStaffItem.IMMUNE)==h.getLevel().getGameTime()+40,"immunity deadline");
        var fire=equip(p,true);((HealingStaffItem)fire.getItem()).onWeaponAction(p,fire,Action.RIGHT_CLICK);
        h.assertTrue(Math.abs(p.getHealth()-13)<.01&&fire.getDamageValue()==0,"switch bypassed shared cooldown");
        h.assertTrue(!IndependentWeaponCooldowns.ready(p,IndependentWeaponCooldowns.HEALING_STAFF,false)
            &&IndependentWeaponCooldowns.ready(p,IndependentWeaponCooldowns.HEALING_STAFF,true),"independent shared cooldowns");
        h.succeed();
    }
    @GameTest(template="empty") public static void healingStaffFireAndArea(GameTestHelper h){
        var p=player(h);var fire=equip(p,true);var item=(HealingStaffItem)fire.getItem();
        item.onWeaponAction(p,fire,Action.RIGHT_CLICK);
        h.assertTrue(Math.abs(p.getHealth()-11)<.01,"fire immediate heal");
        h.assertTrue(PathData.data(p).getLong(HealingStaffItem.REGEN)==h.getLevel().getGameTime()+120,"six-second regeneration");
        p.setShiftKeyDown(true);item.onWeaponAction(p,fire,Action.SHIFT_RIGHT_CLICK);
        var a=PathData.data(p).getCompound(HealingStaffItem.AREA);
        h.assertTrue(a.getBoolean("Fire")&&a.getLong("Until")==h.getLevel().getGameTime()+120,"area metadata");
        h.assertTrue(fire.getDamageValue()==2,"two uses");
        var ice=equip(p,false);((HealingStaffItem)ice.getItem()).onWeaponAction(p,ice,Action.SHIFT_RIGHT_CLICK);
        h.assertTrue(ice.getDamageValue()==0&&PathData.data(p).getCompound(HealingStaffItem.AREA).getBoolean("Fire"),"area switch bypass");
        h.succeed();
    }
}
