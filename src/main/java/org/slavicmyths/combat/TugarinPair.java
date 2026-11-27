package org.slavicmyths.combat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** One real pair item; the second blade exists only as a render, never as a transferable stack. */
@EventBusSubscriber(modid="slavicmyths")
public final class TugarinPair {
    private static final String KEY="SlavicTugarinPair";
    private static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> PAIRED=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","paired_weapons"));
    // Tugarin-only combat effects must not be applied to other paired weapons.
    public static boolean held(Player p){return p.getMainHandItem().is(RareWeapons.TUGARIN.get());}
    public static boolean holdsPair(Player p){return held(p)||p.getMainHandItem().is(PAIRED)||org.slavicmyths.savanna.SteppeWeapons.visualPair(p.getMainHandItem());}
    public static boolean usesBothHands(Player p){return holdsPair(p)||p.getMainHandItem().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","two_handed_weapons")));}
    private static CompoundTag state(Player p){var d=p.getPersistentData();if(!d.contains(Player.PERSISTED_NBT_TAG,10))d.put(Player.PERSISTED_NBT_TAG,new CompoundTag());var root=d.getCompound(Player.PERSISTED_NBT_TAG);if(!root.contains(KEY,10))root.put(KEY,new CompoundTag());return root.getCompound(KEY);}
    public static void update(ServerPlayer p){var d=state(p);if(!p.isAlive()||!usesBothHands(p)){restore(p);return;}
        ItemStack off=p.getOffhandItem();
        if(!d.getBoolean("Active")){d.putBoolean("Active",true);d.putBoolean("Silent",!held(p));if(!off.isEmpty())d.put("Offhand",off.save(p.registryAccess()));p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);if(held(p))p.playSound(org.slavicmyths.registry.ModSounds.boss("tugarin.phase1.blade_draw"),.45F,1.25F);}
        else if(!off.isEmpty()){p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);if(!d.contains("Offhand",10))d.put("Offhand",off.save(p.registryAccess()));else returnItem(p,off);}
    }
    public static void restore(ServerPlayer p){var d=state(p);if(!d.getBoolean("Active"))return;ItemStack saved=ItemStack.parseOptional(p.registryAccess(),d.getCompound("Offhand"));d.remove("Offhand");d.putBoolean("Active",false);if(p.isAlive()&&!d.getBoolean("Silent"))p.playSound(org.slavicmyths.registry.ModSounds.boss("tugarin.weapon.sheathe"),.3F,.85F);d.remove("Silent");if(!saved.isEmpty()){if(p.getOffhandItem().isEmpty())p.setItemSlot(EquipmentSlot.OFFHAND,saved);else returnItem(p,saved);}}
    private static void returnItem(ServerPlayer p,ItemStack stack){p.getInventory().add(stack);if(!stack.isEmpty())p.drop(stack,false);}
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent e){if(e.getEntity() instanceof ServerPlayer p&&(e.getSlot()==EquipmentSlot.MAINHAND||e.getSlot()==EquipmentSlot.OFFHAND))update(p);}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)restore(p);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)restore(p);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){restore(p);update(p);}}
    private TugarinPair(){}
}
