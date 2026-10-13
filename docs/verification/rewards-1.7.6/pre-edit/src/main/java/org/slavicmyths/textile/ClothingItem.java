package org.slavicmyths.textile;
import net.minecraft.core.Holder;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Equipable without ArmorItem: zero armor, attributes and durability. */
public final class ClothingItem extends Item implements Equipable {
    private final EquipmentSlot slot;
    public ClothingItem(EquipmentSlot slot){super(new Properties().stacksTo(1));this.slot=slot;}
    @Override public EquipmentSlot getEquipmentSlot(){return slot;}
    @Override public Holder<SoundEvent> getEquipSound(){return SoundEvents.ARMOR_EQUIP_LEATHER;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return swapWithEquipmentSlot(this,level,player,hand);}
}
