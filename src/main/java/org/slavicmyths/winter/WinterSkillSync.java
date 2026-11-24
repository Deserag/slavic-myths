package org.slavicmyths.winter;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.rpg.PathData;

@EventBusSubscriber(modid="slavicmyths")
public final class WinterSkillSync {
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var stack=p.getMainHandItem();var item=stack.getItem();
        if(!(item instanceof WinterMeleeItem||item instanceof WinterPairItem||item instanceof WinterLegendItem||item instanceof WinterRanged.Bow||item instanceof WinterRanged.Crossbow||item instanceof org.slavicmyths.rewards.KalinSabreCombo.Sabre))return;
        var data=PathData.data(p);
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        boolean changed=false;
        for(String key:data.getAllKeys()){
            if(!key.startsWith("CD_winter_")&&!key.equals("KalinWaveReady")&&!key.equals("KalinSummonReady"))continue;
            long value=data.getLong(key);
            if(tag.getLong(key)!=value){tag.putLong(key,value);changed=true;}
        }
        for(String key:new java.util.ArrayList<>(tag.getAllKeys())){
            if((key.startsWith("CD_winter_")||key.equals("KalinWaveReady")||key.equals("KalinSummonReady"))&&!data.contains(key)){tag.remove(key);changed=true;}
        }
        if(changed)stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    private WinterSkillSync(){}
}
