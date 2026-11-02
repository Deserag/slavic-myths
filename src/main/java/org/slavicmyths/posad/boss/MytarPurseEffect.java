package org.slavicmyths.posad.boss;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.item.FolkAccessoryItem;
import org.slavicmyths.registry.ModItems;

/** Only attracts coins that the game already associates with the equipping player. */
@EventBusSubscriber(modid="slavicmyths")
public final class MytarPurseEffect {
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        if (FolkAccessoryItem.equipped(player, ModItems.MYTAR_PURSE.get()).isEmpty()) return;
        for (ItemEntity dropped : player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(4D), item -> item.isAlive() && item.getItem().is(ModItems.ANCIENT_COIN.get()) && item.getOwner() == player)) {
            Vec3 attraction = player.position().add(0D, .65D, 0D).subtract(dropped.position());
            if (attraction.lengthSqr() < .04D) continue;
            Vec3 next = attraction.normalize().scale(.10D);
            if (player.serverLevel().noCollision(dropped, dropped.getBoundingBox().move(next))) dropped.setDeltaMovement(dropped.getDeltaMovement().add(next));
        }
    }
    private MytarPurseEffect() {}
}
