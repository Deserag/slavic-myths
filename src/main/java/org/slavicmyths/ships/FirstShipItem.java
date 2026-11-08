package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The ship item PLACES ordinary Minecraft blocks in the world instead of
 * spawning a visual model with fake collision. A successful placement replaces
 * only water with a sealed wooden lower floor; no water exists in the hold.
 */
public final class FirstShipItem extends Item {
    public FirstShipItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) {
        return Component.literal("Деревянный корабль 7×11 (настоящие блоки)");
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.add(Component.literal("Два этажа: обычные деревянные блоки и сухой трюм"));
        lines.add(Component.literal("Руль: ПКМ у кормы, W/S ход, A/D поворот на 90°"));
        lines.add(Component.literal("Движение пока дискретное — 1 блок за команду"));
        lines.add(Component.literal("Обычные сундуки, бочки, печи и верстаки размещаются вручную"));
        lines.add(Component.literal("Ключ верфи: упаковка остановленного пустого судна"));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (ray.getType() != HitResult.Type.BLOCK
                || !level.getFluidState(ray.getBlockPos()).is(FluidTags.WATER))
            return InteractionResultHolder.pass(held);

        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(held, true);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.fail(held);

        BlockPos anchor = ray.getBlockPos();
        int turns = FirstShipBlockHull.rotationFromYaw(player.getYRot());
        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
        if (ship == null) return InteractionResultHolder.fail(held);
        ship.initializeNativeHull(anchor, turns);
        if (!ship.loadPacked(held)) {
            player.displayClientMessage(Component.literal("Корабль не установлен: повреждён сохранённый груз"), true);
            return InteractionResultHolder.fail(held);
        }
        // Build checks every water/source/floor/mast cell before modifying land.
        // A failed placement never consumes the item or overwrites the terrain.
        if (!FirstShipBlockHull.place(server, anchor, turns, ship.pendingBlockCargo())) {
            player.displayClientMessage(Component.literal(
                    "Нужно свободное водное место 7×11, 2 блока глубины и 13 блоков высоты"), true);
            return InteractionResultHolder.fail(held);
        }
        if (!server.addFreshEntity(ship)) {
            FirstShipBlockHull.remove(server, anchor, turns);
            return InteractionResultHolder.fail(held);
        }
        if (!player.getAbilities().instabuild) held.shrink(1);
        else if (held.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))
            held.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        player.displayClientMessage(Component.literal(
                "Корабль установлен. Блоки настоящие: палуба и трюм имеют обычную физику."), true);
        return InteractionResultHolder.sidedSuccess(held, false);
    }
}
