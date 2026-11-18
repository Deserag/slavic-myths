package org.slavicmyths.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.viy.ViyRewardNetwork;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ViyRewardClient {
    public static final KeyMapping SUMMON=new KeyMapping("key.slavicmyths.viy_summon",79,"key.categories.slavicmyths");
    public static final class Registration {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event){event.register(SUMMON);}
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var minecraft=Minecraft.getInstance();
        while(SUMMON.consumeClick())if(minecraft.player!=null&&minecraft.screen==null)
            PacketDistributor.sendToServer(new ViyRewardNetwork.Summon());
    }
    private ViyRewardClient(){}
}
