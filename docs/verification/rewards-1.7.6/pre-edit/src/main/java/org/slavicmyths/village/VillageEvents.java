package org.slavicmyths.village;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class VillageEvents {
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){
        if(!e.getLevel().isClientSide && (e.getEntity() instanceof Villager || e.getEntity() instanceof ZombieVillager)
            && e.getEntity().getData(VillageRoles.OUTFIT)<0)
            e.getEntity().setData(VillageRoles.OUTFIT,OutfitRules.variant(e.getEntity().getUUID()));
    }
    @SubscribeEvent public static void converted(LivingConversionEvent.Post e){
        if((e.getEntity() instanceof Villager && e.getOutcome() instanceof ZombieVillager)
            || (e.getEntity() instanceof ZombieVillager && e.getOutcome() instanceof Villager)) {
            int variant=e.getEntity().getData(VillageRoles.OUTFIT);
            e.getOutcome().setData(VillageRoles.OUTFIT,variant<0?OutfitRules.variant(e.getEntity().getUUID()):variant);
        }
    }
    private VillageEvents(){}
}
