package org.slavicmyths.industry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.*;
@EventBusSubscriber(modid="slavicmyths")
public final class ChudThreats {
    private static final java.util.Set<net.minecraft.world.entity.Mob> INSTALLED=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    @SubscribeEvent public static void joined(EntityJoinLevelEvent e){if(!e.getLevel().isClientSide&&(e.getEntity() instanceof Zombie||e.getEntity() instanceof AbstractSkeleton||e.getEntity() instanceof Spider)){var mob=(net.minecraft.world.entity.Mob)e.getEntity();if(INSTALLED.add(mob))mob.targetSelector.addGoal(4,new NearestAttackableTargetGoal<>(mob,ChudPerson.class,true));}}
    private ChudThreats(){}
}
