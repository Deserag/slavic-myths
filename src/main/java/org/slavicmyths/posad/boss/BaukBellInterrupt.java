package org.slavicmyths.posad.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Real vanilla bell activation, never a new item or a magic global stun. */
@EventBusSubscriber(modid="slavicmyths")
public final class BaukBellInterrupt {
    @SubscribeEvent public static void ring(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel world) || !world.getBlockState(event.getPos()).is(Blocks.BELL)) return;
        for (PosadBoss boss : world.getEntitiesOfClass(PosadBoss.class, new AABB(event.getPos()).inflate(8D), b -> b.kind() == PosadBoss.Kind.BAUK && b.isAlive())) boss.interruptByBell();
    }
    private BaukBellInterrupt() {}
}
