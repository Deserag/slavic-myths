package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

/** Separate placement ID and NBT: legacy raft saves are never reinterpreted as ships. */
public final class FirstShipItem extends Item {
    public FirstShipItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) { return Component.literal("Деревянный корабль 7×11"); }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.add(Component.literal("Два уровня: верхняя палуба и нижний трюм"));
        lines.add(Component.literal("Кормовой руль, парус, груз и люк"));
        lines.add(Component.literal("Строится в корабельной верфи"));
        lines.add(Component.literal("Упаковка остановленного пустого корабля: верфяной ключ"));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (ray.getType() != HitResult.Type.BLOCK || !level.getFluidState(ray.getBlockPos()).is(FluidTags.WATER))
            return InteractionResultHolder.pass(held);
        double x = ray.getBlockPos().getX() + .5D;
        double y = ray.getLocation().y;
        double z = ray.getBlockPos().getZ() + .5D;
        float yaw = player.getYRot();

        // Check each rotated half-block footprint, including the narrowing bow.
        // Solid obstacles at hull, upper deck or mast must not be overwritten.
        for (int sx = 0; sx < FirstShipHull.WIDTH * 2; sx++)
            for (int sz = 0; sz < FirstShipHull.LENGTH * 2; sz++) {
                double lx = -FirstShipHull.WIDTH / 2D + (sx + .5D) / 2D;
                double lz = -FirstShipHull.LENGTH / 2D + (sz + .5D) / 2D;
                if (!FirstShipHull.withinHull(lx, lz, .05D)) continue;
                FirstShipHull.Point point = FirstShipHull.toWorld(lx, lz, x, z, yaw);
                BlockPos water = BlockPos.containing(point.x(), y - .14D, point.z());
                if (!level.hasChunkAt(water) || !level.getFluidState(water).is(FluidTags.WATER)
                        || !level.getFluidState(water).isSource()
                        || !level.hasChunkAt(water.below())
                        || !level.getFluidState(water.below()).is(FluidTags.WATER))
                    return failure(player, held, level);
                for (int dy = 1; dy <= 10; dy++) {
                    BlockPos above = water.above(dy);
                    if (!level.hasChunkAt(above) ||
                            !level.getBlockState(above).getCollisionShape(level, above).isEmpty())
                        return failure(player, held, level);
                }
            }
        // Ensure masthead and sail are clear as well.
        BlockPos mast = BlockPos.containing(x, y + 8D, z);
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos tip = mast.above(dy);
            if (!level.hasChunkAt(tip) ||
                    !level.getBlockState(tip).getCollisionShape(level, tip).isEmpty())
                return failure(player, held, level);
        }

        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
        if (ship == null) return InteractionResultHolder.fail(held);
        ship.setPos(x, y - .14D, z);
        ship.setWaterSurface(y);
        ship.setYRot(yaw);
        if (!level.noCollision(ship, ship.getBoundingBox())) return InteractionResultHolder.fail(held);
        if (!level.isClientSide) {
            if (!ship.loadPacked(held)) {
                player.displayClientMessage(Component.literal("Корабль не установлен: ошибка сохранённого груза"), true);
                return InteractionResultHolder.fail(held);
            }
            if (!level.addFreshEntity(ship)) return InteractionResultHolder.fail(held);
            if (!player.getAbilities().instabuild) held.shrink(1);
            else if (held.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))
                held.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    private InteractionResultHolder<ItemStack> failure(Player player, ItemStack held, Level level) {
        if (!level.isClientSide) player.displayClientMessage(
                Component.literal("Кораблю нужна свободная глубокая акватория 7×11"), true);
        return InteractionResultHolder.fail(held);
    }
}
