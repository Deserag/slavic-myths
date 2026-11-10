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
        net.minecraft.world.item.component.CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().contains("FirstShipPack", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            net.minecraft.nbt.CompoundTag pack = data.copyTag().getCompound("FirstShipPack");
            if (pack.getInt("GeometryVersion") == 9) try {
                ShipSuperstructurePlan plan = ShipSuperstructureData.load(pack.getCompound("Superstructure"));
                return Component.literal("Корабль " + plan.shipClass().name()
                        + " " + plan.hullWidth() + "×" + plan.hullLength());
            } catch (RuntimeException ignored) { return Component.literal("Повреждённый чертёж корабля"); }
        }
        return Component.literal("Деревянный корабль 7×11 (настоящие блоки)");
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.add(Component.literal("Два этажа: обычные деревянные блоки и сухой трюм"));
        lines.add(Component.literal("Руль: ПКМ по штурвалу на корме; Shift — выход на палубу"));
        lines.add(Component.literal("Базовый парус установлен; улучшения: корабельный верстак → «Модификации»."));
        lines.add(Component.literal("W/S: плавное движение; A/D: руль с инерцией (новые корабли)"));
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
        if (!server.getEntitiesOfClass(FirstShipEntity.class,
                new net.minecraft.world.phys.AABB(anchor).inflate(40D, 26D, 40D)).isEmpty()) {
            player.displayClientMessage(Component.literal(
                    "Слишком близко к другому кораблю; старый упакуйте ключом верфи"), true);
            return InteractionResultHolder.fail(held);
        }
        int turns = FirstShipBlockHull.rotationFromYaw(player.getYRot());
        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
        if (ship == null) return InteractionResultHolder.fail(held);
        ship.initializeNativeHull(anchor, turns);
        // Newly placed ships can transition to the continuous moving
        // contraption on their first helm command. Existing in-world saves are
        // not silently converted; they retain the old safe world-block mode.
        ship.enableSmoothMotion();
        // A newly crafted ship includes a functional basic sail; packed ships
        // retain their exact saved rig, including intentional sail removal.
        if (!held.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))
            ship.equipStarterSail();
        if (!ship.loadPacked(held)) {
            player.displayClientMessage(Component.literal("Корабль не установлен: повреждён сохранённый груз"), true);
            return InteractionResultHolder.fail(held);
        }
        // Build checks every water/source/floor/mast cell before modifying land.
        // A failed placement never consumes the item or overwrites the terrain.
        ShipSuperstructurePlan plan = ship.superstructure();
        boolean built = ship.geometryVersion() == 9 && plan != null
                ? ShipHullV9.place(server, anchor, turns, plan, ship.pendingBlockCargo())
                : FirstShipBlockHull.place(server, anchor, turns, ship.pendingBlockCargo(),
                        ship.rigLevel(), ship.fabricQuality(), ship.geometryVersion());
        if (!built) {
            player.displayClientMessage(Component.literal(
                    "Нужны свободная вода, место для всех палуб и мачт, и достаточная глубина"), true);
            return InteractionResultHolder.fail(held);
        }
        if (!server.addFreshEntity(ship)) {
            if (ship.geometryVersion() == 9 && plan != null)
                ShipHullV9.remove(server, anchor, turns, plan);
            else FirstShipBlockHull.remove(server, anchor, turns,
                    ship.rigLevel(), ship.fabricQuality(), ship.geometryVersion());
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
