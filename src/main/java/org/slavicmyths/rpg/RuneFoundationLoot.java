package org.slavicmyths.rpg;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.kurgan.*;

/** Death event only: original finite burial population, never summoned replacements. */
@EventBusSubscriber(modid="slavicmyths")
public final class RuneFoundationLoot {
    @SubscribeEvent public static void drops(LivingDropsEvent event) {
        if(event.getEntity() instanceof KurganCreature mob&&!mob.level().isClientSide
            &&mob.kind==KurganFighter.Kind.UPYR&&mob.kurgan!=null
            &&!mob.getPersistentData().getBoolean("RuneSummoned")&&mob.getRandom().nextFloat()<.35F)
            event.getDrops().add(new ItemEntity(mob.level(),mob.getX(),mob.getY(),mob.getZ(),new ItemStack(RuneFoundation.item("soul_fragment"))));
    }
    private RuneFoundationLoot(){}
}
