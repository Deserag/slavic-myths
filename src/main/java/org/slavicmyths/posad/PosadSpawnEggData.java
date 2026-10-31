package org.slavicmyths.posad;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

/** Egg metadata uses the existing entity's serialized role and weapon. */
public final class PosadSpawnEggData {
    public static CustomData create(PosadPopulationPlan.Role role, PosadPopulationPlan.Weapon weapon) {
        if (role.guard() == (weapon == PosadPopulationPlan.Weapon.NONE)) throw new IllegalArgumentException("Incorrect guard equipment");
        CompoundTag tag = new CompoundTag();
        tag.putInt("PosadRole", role.ordinal());
        tag.putInt("PosadWeapon", weapon.ordinal());
        tag.putBoolean("PosadEgg", true);
        return CustomData.of(tag);
    }
    private PosadSpawnEggData() { }
}
