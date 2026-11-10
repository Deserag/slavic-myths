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
        net.minecraft.world.item.component.CustomData custom = stack.get(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (custom != null && custom.copyTag().contains("FirstShipPack", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            net.minecraft.nbt.CompoundTag pack = custom.copyTag().getCompound("FirstShipPack");
            if (pack.contains(ShipBuildConfiguration.NBT_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                try {
                    ShipBuildConfiguration blueprint = ShipBuildConfiguration.load(
                            pack.getCompound(ShipBuildConfiguration.NBT_KEY));
                    String className = switch (blueprint.shipClass()) {
                        case SMALL -> "малый";
                        case MEDIUM -> "средний";
                        case LARGE -> "большой";
                        case LARGEST -> "крупнейший";
                    };
                    return Component.literal("Корабль " + blueprint.width()
                            + "×" + blueprint.length() + " — " + className);
                } catch (RuntimeException ignored) {}
            }
        }
        return Component.literal("Деревянный корабль 7×11 (настоящие блоки)");
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        ShipBuildConfiguration modular = ShipyardModular.configuration(stack);
        if (modular != null) {
            lines.add(Component.literal("Палубы: " + modular.decks()
                    + " | Мачты: " + modular.sailTiers().size()
                    + " | Паруса: " + modular.sailTiers()));
            lines.add(Component.literal("ПКМ по штурвалу на корме: W/S — ход, A/D — руль"));
            lines.add(Component.literal("Shift — выйти; улучшения — в корабельной верфи"));
            return;
        }
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
        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
        if (ship == null) return InteractionResultHolder.fail(held);
        ship.initializeNativeHull(anchor, FirstShipBlockHull.rotationFromYaw(player.getYRot()));
        ship.enableSmoothMotion();
        if (!held.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))
            ship.equipStarterSail();
        if (!ship.loadPacked(held)) {
            player.displayClientMessage(Component.literal("Корабль не установлен: повреждены сохранённые данные"), true);
            return InteractionResultHolder.fail(held);
        }
        ShipBuildConfiguration blueprint = ship.modularConfiguration();
        double distance = blueprint == null ? 9D : Math.max(blueprint.width(), blueprint.length()) / 2D + 5D;
        if (!server.getEntitiesOfClass(FirstShipEntity.class,
                new net.minecraft.world.phys.AABB(anchor).inflate(distance, 12D, distance)).isEmpty()) {
            player.displayClientMessage(Component.literal(
                    "Слишком близко к другому кораблю; старый упакуйте ключом верфи"), true);
            return InteractionResultHolder.fail(held);
        }
        int turns = ship.quarterTurns();
        // Build checks every water/source/floor/mast cell before modifying land.
        // A failed placement never consumes the item or overwrites the terrain.
        if (!(blueprint == null
                ? FirstShipBlockHull.place(server, anchor, turns, ship.pendingBlockCargo(),
                        ship.rigLevel(), ship.fabricQuality(), ship.geometryVersion())
                : ShipModularBlockHull.place(server, anchor, turns, blueprint,
                        ship.pendingBlockCargo()))) {
            player.displayClientMessage(Component.literal(
                    "Нужно свободное водное место под весь корпус и высота под мачты"), true);
            return InteractionResultHolder.fail(held);
        }
        if (!server.addFreshEntity(ship)) {
            if (blueprint == null)
                FirstShipBlockHull.remove(server, anchor, turns, ship.rigLevel(),
                        ship.fabricQuality(), ship.geometryVersion());
            else ShipModularBlockHull.remove(server, anchor, turns, blueprint);
            return InteractionResultHolder.fail(held);
        }
        if (!player.getAbilities().instabuild) held.shrink(1);
        else if (held.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))
            held.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        player.displayClientMessage(Component.literal(
                "Корабль установлен. ПКМ по штурвалу на корме — управление W/S и A/D; Shift — выход."), true);
        return InteractionResultHolder.sidedSuccess(held, false);
    }
}
