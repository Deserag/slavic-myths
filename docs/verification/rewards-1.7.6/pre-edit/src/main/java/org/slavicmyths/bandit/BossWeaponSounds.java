package org.slavicmyths.bandit;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.registry.ModSounds;

/** Equipment event avoids adding another player polling loop for draw feedback. */
@EventBusSubscriber(modid="slavicmyths")
public final class BossWeaponSounds {
    @SubscribeEvent public static void changed(LivingEquipmentChangeEvent event){
        if(event.getEntity() instanceof Player && event.getSlot()==EquipmentSlot.MAINHAND
            && event.getTo().is(ModItems.NIGHTINGALE_DAGGER.get())
            && !event.getFrom().is(ModItems.NIGHTINGALE_DAGGER.get()))
            event.getEntity().playSound(ModSounds.boss("nightingale_blade_draw"),.5F,1.2F);
    }
    private BossWeaponSounds(){}
}
