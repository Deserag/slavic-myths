package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.item.*;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.*;

@GameTestHolder("slavicmyths_accessories278") @PrefixGameTestTemplate(false)
public final class Accessories278GameTests {
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
    private static ServerPlayer player(GameTestHelper t){
        var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CharmProbe"),ClientInformation.createDefault());
        p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,20,3)));p.setNoGravity(true);
        for(int i=0;i<61;i++)p.tick();return p;
    }
    private static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
    private static void equip(ServerPlayer p,String slot,int index,ItemStack s){CuriosApi.getCuriosInventory(p).orElseThrow().setEquippedCurio(slot,index,s);p.tick();}
    private static float damage(ServerPlayer p,net.minecraft.world.damagesource.DamageSource source){p.setHealth(20);p.invulnerableTime=0;p.hurt(source,10);return 20-p.getHealth();}
    private static void near(GameTestHelper t,double actual,double expected,String label){t.assertTrue(Math.abs(actual-expected)<.002,label+" actual="+actual+" expected="+expected);}
    private static void legacyTalent(ServerPlayer p){var d=org.slavicmyths.rpg.PathData.data(p);d.remove("Class2");var skills=new net.minecraft.nbt.CompoundTag();skills.putInt("charm_power",1);d.put("Skills",skills);}

    @GameTest(template="empty") public static void all37NativeSlotValidatorsAndCapacity(GameTestHelper t)throws Exception{
        var p=player(t);int count=0,physical=0;
        for(String slot:List.of("head","necklace","ring","belt","charm")){
            var handler=CuriosApi.getCuriosInventory(p).orElseThrow().getStacksHandler(slot).orElseThrow();physical+=handler.getSlots();
            var tag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,ResourceLocation.fromNamespaceAndPath("curios",slot));
            for(var holder:BuiltInRegistries.ITEM.getTagOrEmpty(tag)){
                var s=new ItemStack(holder);var context=new SlotContext(slot,p,0,false,true);
                t.assertTrue(CuriosApi.isStackValid(context,s),"native slot validator rejected "+holder.getRegisteredName());
                t.assertTrue(CuriosApi.getCurio(s).orElseThrow().canEquip(context),"curio canEquip rejected "+holder.getRegisteredName());
                equip(p,slot,0,s);t.assertTrue(CuriosApi.getCuriosInventory(p).orElseThrow().findCurio(slot,0).orElseThrow().stack().is(holder),"equipped wrong item");
                equip(p,slot,0,ItemStack.EMPTY);count++;
            }
        }
        t.assertTrue(count==37&&physical==6,"registry/capacity count="+count+" places="+physical);t.succeed();
    }
    @GameTest(template="empty") public static void amberActualDamageShieldDuplicateAndUnequip(GameTestHelper t){
        var p=player(t);p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        float base=damage(p,p.damageSources().inFire());equip(p,"charm",0,new ItemStack(ModItems.AMBER_CHARM.get()));
        near(t,damage(p,p.damageSources().inFire())/base,.85,"shield+amber protection");
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(ModItems.AMBER_CHARM.get()));near(t,damage(p,p.damageSources().inFire())/base,.85,"duplicate protection");
        equip(p,"charm",0,ItemStack.EMPTY);near(t,damage(p,p.damageSources().inFire())/base,.85,"old offhand");
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));near(t,damage(p,p.damageSources().inFire())/base,1,"unequip");
        equip(p,"charm",0,new ItemStack(ModItems.AMBER_CHARM.get()));near(t,damage(p,p.damageSources().fall()),10,"wrong damage type");t.succeed();
    }
    @GameTest(template="empty") public static void perunAndTravelerActualDamage(GameTestHelper t){
        var p=player(t);p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        float lightning=damage(p,p.damageSources().lightningBolt()),fall=damage(p,p.damageSources().fall());
        equip(p,"charm",0,new ItemStack(ModItems.PERUN_CHARM.get()));near(t,damage(p,p.damageSources().lightningBolt())/lightning,.7,"Perun");
        equip(p,"charm",0,new ItemStack(ModItems.TRAVELER_CHARM.get()));near(t,damage(p,p.damageSources().fall())/fall,.85,"traveler");
        near(t,damage(p,p.damageSources().lightningBolt())/lightning,1,"replace removes Perun");
        equip(p,"charm",0,ItemStack.EMPTY);near(t,damage(p,p.damageSources().fall())/fall,1,"remove traveler");t.succeed();
    }
    @GameTest(template="empty") public static void forestActualTaggedAttackerAndTwoPlayers(GameTestHelper t){
        var a=player(t);var b=player(t);var leshy=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("slavicmyths:leshy")).create(t.getLevel());
        var source=a.damageSources().mobAttack((LivingEntity)leshy);float base=damage(a,source);
        equip(a,"charm",0,new ItemStack(ModItems.FOREST_CHARM.get()));a.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        near(t,damage(a,source)/base,.85,"forest spirit");near(t,damage(b,source)/base,1,"other player");
        var cow=EntityType.COW.create(t.getLevel());near(t,damage(a,a.damageSources().mobAttack(cow))/base,1,"ordinary mob");
        equip(a,"charm",0,ItemStack.EMPTY);near(t,damage(a,source)/base,1,"forest unequip");t.succeed();
    }
    @GameTest(template="empty") public static void talentAndEquipmentRegression(GameTestHelper t){
        var p=player(t);
        equip(p,"charm",0,new ItemStack(ModItems.AMBER_CHARM.get()));legacyTalent(p);near(t,damage(p,p.damageSources().inFire()),8,"legacy talent fire");
        equip(p,"charm",0,new ItemStack(ModItems.PERUN_CHARM.get()));legacyTalent(p);near(t,damage(p,p.damageSources().lightningBolt()),6.5,"legacy talent lightning");
        equip(p,"ring",0,new ItemStack(ModItems.PERUN_RING.get()));legacyTalent(p);near(t,damage(p,p.damageSources().lightningBolt()),6.5*.65,"existing ring combines once");
        equip(p,"ring",1,new ItemStack(ModItems.PERUN_RING.get()));legacyTalent(p);near(t,damage(p,p.damageSources().lightningBolt()),6.5*.65,"two identical rings no doubling");
        equip(p,"ring",0,ItemStack.EMPTY);equip(p,"ring",1,ItemStack.EMPTY);legacyTalent(p);near(t,damage(p,p.damageSources().lightningBolt()),6.5,"ring remove");t.succeed();
    }
    @GameTest(template="empty") public static void unequipAndDeathDoNotLeaveCharmModifier(GameTestHelper t){
        var p=player(t);equip(p,"charm",0,new ItemStack(ModItems.AMBER_CHARM.get()));p.kill();t.assertTrue(!p.isAlive(),"death");
        var fresh=player(t);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(fresh,p,true));
        near(t,damage(fresh,fresh.damageSources().inFire()),10,"no leaked protection after clone");t.succeed();
    }
    @GameTest(template="empty") public static void inactiveSlotsAndSerializedInventory(GameTestHelper t){
        var p=player(t);var inv=CuriosApi.getCuriosInventory(p).orElseThrow();
        equip(p,"charm",0,new ItemStack(ModItems.AMBER_CHARM.get()));
        inv.setSlotActive("charm",0,false);near(t,damage(p,p.damageSources().inFire()),10,"inactive charm");
        inv.setSlotActive("charm",0,true);near(t,damage(p,p.damageSources().inFire()),8.5,"reactivate charm");
        equip(p,"ring",0,new ItemStack(ModItems.PERUN_RING.get()));
        inv.setSlotActive("ring",0,false);near(t,damage(p,p.damageSources().lightningBolt()),10,"inactive ring");
        inv.setSlotActive("ring",0,true);near(t,damage(p,p.damageSources().lightningBolt()),6.5,"reactivate ring");
        var saved=inv.saveInventory(false);equip(p,"charm",0,ItemStack.EMPTY);equip(p,"ring",0,ItemStack.EMPTY);
        inv.loadInventory(saved);near(t,damage(p,p.damageSources().inFire()),8.5,"Curios NBT protection");
        near(t,damage(p,p.damageSources().lightningBolt()),6.5,"Curios NBT ring");
        for(String slot:List.of("head","necklace","belt","ring")){
            var context=new SlotContext(slot,p,0,false,true);var s=new ItemStack(ModItems.AMBER_CHARM.get());
            t.assertTrue(!CuriosApi.isStackValid(context,s),"passive charm accepted in "+slot);
        }
        t.assertTrue(!CuriosApi.isStackValid(new SlotContext("charm",p,0,false,true),new ItemStack(item("hunter_charm"))),"active hunter accepted as passive");t.succeed();
    }
}
