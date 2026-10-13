package org.slavicmyths.bandit;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

/** Local encounter targets; unrelated animals and neutral monsters are excluded. */
public final class BossTargets {
    public static boolean village(LivingEntity entity) {
        return entity instanceof AbstractVillager || entity instanceof IronGolem;
    }
    public static boolean automatic(LivingEntity entity) {
        return entity instanceof Player || village(entity);
    }
    private BossTargets() {}
}
