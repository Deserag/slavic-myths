package org.slavicmyths.ninemor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public final class NineMorNamedItems {
    public static final class Spear extends SwordItem {
        public Spear(Tier tier, Properties properties) { super(tier, properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_spear", "Копьё Девяти Пастей"); }
    }
    public static final class Axe extends AxeItem {
        public Axe(Tier tier, Properties properties) { super(tier, properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_axe", "Секира Погибели"); }
    }
    public static final class Shield extends ShieldItem {
        public Shield(Properties properties) { super(properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_shield", "Щит Девяти Чешуй"); }
    }
    public static final class Reward extends Item {
        private final String fallback;
        public Reward(Properties properties, String fallback) { super(properties); this.fallback = fallback; }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback(getDescriptionId(), fallback); }
    }
    private NineMorNamedItems() {}
}
