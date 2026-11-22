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
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.combat.WeaponInputHandler;

public final class NineMorNamedItems {
    public static final class Spear extends SwordItem implements WeaponInputHandler {
        public Spear(Tier tier, Properties properties) { super(tier, properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_spear", "Копьё Девяти Пастей"); }
        @Override public boolean intercepts(Action action) {
            return action == Action.RIGHT_CLICK || action == Action.SHIFT_RIGHT_CLICK;
        }
        @Override public void onWeaponAction(ServerPlayer player, ItemStack held, Action action) {
            if (held != player.getMainHandItem() || !player.isAlive() || player.isSpectator()) return;
            if (action == Action.RIGHT_CLICK || action == Action.SHIFT_RIGHT_CLICK) {
                NineMorRewardAbilities.spear(player.level(), player, InteractionHand.MAIN_HAND,
                        action == Action.SHIFT_RIGHT_CLICK);
            }
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
            lines.add(Component.translatable("tooltip.slavicmyths.nine_mor.spear.passive"));
            lines.add(Component.translatable("tooltip.slavicmyths.nine_mor.spear.primary"));
            lines.add(Component.translatable("tooltip.slavicmyths.nine_mor.spear.secondary"));
        }
    }
    public static final class Axe extends AxeItem implements WeaponInputHandler {
        public Axe(Tier tier, Properties properties) { super(tier, properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_axe", "Секира Погибели"); }
        @Override public boolean intercepts(Action action) {
            return action == Action.RIGHT_CLICK || action == Action.SHIFT_RIGHT_CLICK;
        }
        @Override public void onWeaponAction(ServerPlayer player, ItemStack held, Action action) {
            if (held != player.getMainHandItem() || !player.isAlive() || player.isSpectator()) return;
            if (action == Action.RIGHT_CLICK || action == Action.SHIFT_RIGHT_CLICK) {
                NineMorRewardAbilities.axe(player.level(), player, InteractionHand.MAIN_HAND,
                        action == Action.SHIFT_RIGHT_CLICK);
            }
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
            lines.add(Component.translatable("tooltip.slavicmyths.nine_mor.axe.primary"));
            lines.add(Component.translatable("tooltip.slavicmyths.nine_mor.axe.secondary"));
        }
    }
    public static final class Shield extends ShieldItem {
        public Shield(Properties properties) { super(properties); }
        @Override public Component getName(ItemStack stack) { return Component.translatableWithFallback("item.slavicmyths.nine_mor_shield", "Щит Девяти Чешуй"); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {InteractionResultHolder<ItemStack> dome=NineMorRewardAbilities.shield(level,player,hand);return dome==null?super.use(level,player,hand):dome;}
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
            lines.add(Component.translatable("tooltip.slavicmyths.nine_mor.shield.passive"));
            lines.add(Component.translatable("tooltip.slavicmyths.nine_mor.shield.active"));
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
