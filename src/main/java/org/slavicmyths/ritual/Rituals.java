package org.slavicmyths.ritual;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.slavicmyths.registry.ModItems;

/** Three ordered rites. The first rite keeps its original sequence and output. */
public final class Rituals {
    public static final int FIRST = 0, AXE = 1, CHARM = 2, STORM_STAFF = 3, AMBER_CHARM = 4, CHARCOAL = 5;
    public static final int COUNT = 6;
    public static int length(int rite) { return rite == AXE || rite == STORM_STAFF ? 4 : 3; }
    public static Item ingredient(int rite, int step) {
        if (rite == FIRST) return FirstRitual.ingredient(step);
        if (rite == AXE) {
            switch (step) {
                case 0: return ModItems.PERUNITE_AXE.get();
                case 1: return ModItems.THUNDER_STONE.get();
                case 2: return ModItems.ANCIENT_SIGN.get();
                default: return ModItems.RITUAL_BOWL.get();
            }
        }
        if (rite == CHARM) {
            switch (step) {
                case 0: return ModItems.LINEN_CLOTH.get();
                case 1: return ModItems.THUNDER_STONE.get();
                default: return ModItems.ANCIENT_SIGN.get();
            }
        }
        if (rite == STORM_STAFF) {
            switch (step) {
                case 0: return ModItems.CARVED_STAFF.get();
                case 1: return ModItems.PERUNITE.get();
                case 2: return ModItems.THUNDER_STONE.get();
                default: return ModItems.ANCIENT_SIGN.get();
            }
        }
        if (rite == AMBER_CHARM) {
            switch (step) {
                case 0: return ModItems.WARDING_CHARM.get();
                case 1: return ModItems.AMBER.get();
                default: return ModItems.JUNIPER_BLEND.get();
            }
        }
        switch (step) {
            case 0: return net.minecraft.item.Items.CHARCOAL;
            case 1: return ModItems.GROUND_WORMWOOD.get();
            default: return ModItems.SILVER_NUGGET.get();
        }
    }
    public static ItemStack result(int rite) {
        switch (rite) {
            case AXE: return new ItemStack(ModItems.THUNDER_AXE.get());
            case CHARM: return new ItemStack(ModItems.PERUN_CHARM.get());
            case STORM_STAFF: return new ItemStack(ModItems.STORM_STAFF.get());
            case AMBER_CHARM: return new ItemStack(ModItems.AMBER_CHARM.get());
            case CHARCOAL: return new ItemStack(ModItems.RITUAL_CHARCOAL.get(), 2);
            default: return new ItemStack(ModItems.ANCIENT_SIGN.get());
        }
    }
    private Rituals() { }
}
