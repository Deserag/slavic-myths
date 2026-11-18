package org.slavicmyths.ninemor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class NineMorNamedItems {
    public static final class Spear extends SwordItem {
        public Spear(Tier tier, Properties properties) { super(tier, properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_spear", "Копьё Девяти Пастей"); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {return NineMorRewardAbilities.spear(level,player,hand);}
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
            lines.add(Component.literal("Постоянный крит ×1.5 (не суммируется с обычным)"));
            lines.add(Component.literal("ПКМ: 4 призрачных копья зависают за спиной и по очереди поражают цель"));
            lines.add(Component.literal("Shift + ПКМ: вращающиеся копья наносят круговой удар"));
        }
    }
    public static final class Axe extends AxeItem {
        public Axe(Tier tier, Properties properties) { super(tier, properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_axe", "Секира Погибели"); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {return NineMorRewardAbilities.axe(level,player,hand);}
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
            lines.add(Component.literal("ПКМ: Разлом Мора — движущаяся ударная волна с иссушением"));
            lines.add(Component.literal("Shift + ПКМ: Последний приговор — падение призрачной секиры (цель ≤35% здоровья)"));
        }
    }
    public static final class Shield extends ShieldItem {
        public Shield(Properties properties) { super(properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_shield", "Щит Девяти Чешуй"); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {InteractionResultHolder<ItemStack> dome=NineMorRewardAbilities.shield(level,player,hand);return dome==null?super.use(level,player,hand):dome;}
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
            lines.add(Component.literal("Девятый заслон — временное поглощение при блокировании"));
            lines.add(Component.literal("Shift + ПКМ: Змеиный купол — 3D-щитовой барьер на 6 секунд"));
        }
    }
    public static final class Amulet extends Item implements top.theillusivec4.curios.api.type.capability.ICurioItem {
        public Amulet(Properties properties) { super(properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback(getDescriptionId(), "Оберег Девятой Жизни"); }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
            lines.add(Component.literal("Curios: ожерелье. Спасение от смертельного удара раз в 5 минут."));
        }
        @Override public boolean canEquip(top.theillusivec4.curios.api.SlotContext context, ItemStack stack) { return "necklace".equals(context.identifier()); }
    }
    public static final class Reward extends Item {
        private final String fallback;
        public Reward(Properties properties, String fallback) { super(properties); this.fallback = fallback; }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback(getDescriptionId(), fallback); }
    }
    private NineMorNamedItems() {}
}
