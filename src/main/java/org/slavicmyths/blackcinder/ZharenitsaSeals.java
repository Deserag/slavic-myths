package org.slavicmyths.blackcinder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.item.ArmorSetHealth;
import org.slavicmyths.registry.ModItems;

/**
 * One sealed iron or diamond armor piece grants +1 MAX_HEALTH.
 * Four sealed pieces grant +5 MAX_HEALTH in total.
 *
 * No new armor item is minted: the original ItemStack, enchantments, damage,
 * trims and components survive upgrading. All inventory mutations are server
 * authoritative; the permanent marker lives on the armor ItemStack.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class ZharenitsaSeals {
    private static final String MARKER = "slavicmyths_zharenitsa_heat_seal";
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static final DeferredHolder<Item, Item> SEAL =
        ModItems.ITEMS.register("zharenitsa_heat_seal",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static void init() { }

    public static boolean eligible(ItemStack stack) {
        return stack.is(Items.IRON_HELMET) || stack.is(Items.IRON_CHESTPLATE)
            || stack.is(Items.IRON_LEGGINGS) || stack.is(Items.IRON_BOOTS)
            || stack.is(Items.DIAMOND_HELMET) || stack.is(Items.DIAMOND_CHESTPLATE)
            || stack.is(Items.DIAMOND_LEGGINGS) || stack.is(Items.DIAMOND_BOOTS);
    }

    public static boolean upgraded(ItemStack stack) {
        if (!eligible(stack)) return false;
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag().getBoolean(MARKER);
    }

    /** Returns a copy without changing the source or duplicating enchantments. */
    public static ItemStack seal(ItemStack original) {
        if (original.getCount() != 1 || !eligible(original) || upgraded(original))
            return ItemStack.EMPTY;
        ItemStack result = original.copy();
        CompoundTag data = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putBoolean(MARKER, true);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return result;
    }

    public static int count(LivingEntity wearer) {
        int count = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS)
            if (upgraded(wearer.getItemBySlot(slot))) count++;
        return count;
    }

    /** Deterministic health bonus, measured in HP (one HP is half a heart). */
    public static int bonusForPieces(int pieces) {
        int n = Math.max(0, Math.min(4, pieces));
        return n == 4 ? 5 : n;
    }

    public static int bonus(LivingEntity wearer) {
        return bonusForPieces(count(wearer));
    }

    /**
     * Existing Poloz upgrades already use sneak + right click on a smithing
     * table. This independent operation handles ONLY unmodified iron/diamond
     * pieces and consumes a single heat seal from the player's inventory.
     */
    @SubscribeEvent
    public static void smith(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        ItemStack original = event.getItemStack();
        if (!player.isShiftKeyDown() || !player.level().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE)
                || original.getCount() != 1 || !eligible(original) || upgraded(original))
            return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(player instanceof ServerPlayer server)) return;
        if (!player.mayBuild() || !player.mayInteract(server.level(), event.getPos())) return;

        int materialSlot = -1;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(SEAL.get())) {
                materialSlot = slot;
                break;
            }
        }
        if (materialSlot < 0) {
            server.displayClientMessage(Component.translatable("zharenitsa.seal.need_material"), true);
            return;
        }

        ItemStack copy = seal(original);
        if (copy.isEmpty()) return;
        if (!server.getAbilities().instabuild)
            player.getInventory().getItem(materialSlot).shrink(1);
        player.setItemInHand(event.getHand(), copy);
        player.getInventory().setChanged();
        server.level().playSound(null, event.getPos(), SoundEvents.SMITHING_TABLE_USE,
            SoundSource.BLOCKS, 1f, 0.9f);
        server.containerMenu.broadcastChanges();
        ArmorSetHealth.refresh(server);
        server.displayClientMessage(Component.translatable("zharenitsa.seal.applied"), true);
    }

    private ZharenitsaSeals() { }
}
