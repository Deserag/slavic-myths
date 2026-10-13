package org.slavicmyths.ritual;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.registry.ModItems;

/** Three ordered rites. The first rite keeps its original sequence and output. */
public final class Rituals {
    public static final int FIRST = 0, AXE = 1, CHARM = 2, STORM_STAFF = 3, AMBER_CHARM = 4, CHARCOAL = 5;
    public static final int THUNDER_SPEAR = 6, PERUNITE_MACE = 7;
    public static final int EMBER_AXE = 8;
    public static final int COUNT = 17;
    public static int length(int rite) { return rite == AXE || rite == STORM_STAFF || rite == THUNDER_SPEAR || rite == PERUNITE_MACE ? 4 : 3; }
    public static Item ingredient(int rite, int step) {
        if (rite >= 9 && rite < COUNT) {
            Item[][] ingredients={
                {ModItems.THUNDER_STONE.get(),ModItems.PERUNITE.get(),ModItems.ANCIENT_SIGN.get()},
                {ModItems.RITUAL_CHARCOAL.get(),ModItems.EMBER_HEART.get(),ModItems.ANCIENT_SIGN.get()},
                {ModItems.LESHY_HEART.get(),ModItems.WORMWOOD.get(),ModItems.ANCIENT_SIGN.get()},
                {ModItems.NOON_EAR.get(),ModItems.SILVER_INGOT.get(),ModItems.ANCIENT_SIGN.get()},
                {ModItems.KIKIMORA_LOCK.get(),ModItems.SILVER_INGOT.get(),ModItems.ANCIENT_SIGN.get()},
                {ModItems.SILVER_INGOT.get(),ModItems.WARDING_CHARM.get(),ModItems.ANCIENT_SIGN.get()},
                {ModItems.FERN_FLOWER.get(),net.minecraft.world.item.Items.HONEY_BOTTLE,ModItems.ANCIENT_SIGN.get()},
                {net.minecraft.world.item.Items.FEATHER,ModItems.FIREWEED.get(),ModItems.ANCIENT_SIGN.get()}};
            return ingredients[rite-9][Math.max(0,Math.min(2,step))];
        }
        if (rite == FIRST) return FirstRitual.ingredient(step);
        if (rite == EMBER_AXE) {
            switch(step) { case 0: return ModItems.EMBER_HEART.get(); case 1: return ModItems.PERUNITE_AXE.get(); default: return ModItems.ANCIENT_SIGN.get(); }
        }
        if (rite == THUNDER_SPEAR || rite == PERUNITE_MACE) {
            switch (step) {
                case 0: return rite == THUNDER_SPEAR ? ModItems.SILVER_SPEAR.get() : ModItems.MACE.get();
                case 1: return ModItems.PERUNITE.get();
                case 2: return ModItems.THUNDER_STONE.get();
                default: return ModItems.ANCIENT_SIGN.get();
            }
        }
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
            case 0: return net.minecraft.world.item.Items.CHARCOAL;
            case 1: return ModItems.GROUND_WORMWOOD.get();
            default: return ModItems.SILVER_NUGGET.get();
        }
    }
    public static ItemStack result(int rite) {
        if(rite>=9 && rite<COUNT)return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_"+org.slavicmyths.rpg.Runes.IDS[rite-9])));
        switch (rite) {
            case EMBER_AXE: return new ItemStack(ModItems.THUNDER_AXE.get());
            case THUNDER_SPEAR: return new ItemStack(ModItems.THUNDER_SPEAR.get());
            case PERUNITE_MACE: return new ItemStack(ModItems.PERUNITE_MACE.get());
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
