package org.slavicmyths.sisters;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class LomeyaLegacyMigration {
    @SubscribeEvent public static void inventory(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0)return;
        var inventory=p.getInventory();
        for(int i=0;i<inventory.getContainerSize();i++){
            var old=inventory.getItem(i);
            if(old.is(SistersContent.LOMEYA_HAMMER_OFFHAND.get()))
                inventory.setItem(i,old.transmuteCopy(SistersContent.LOMEYA_HAMMER.get(),old.getCount()));
        }
    }
    private LomeyaLegacyMigration(){}
}
