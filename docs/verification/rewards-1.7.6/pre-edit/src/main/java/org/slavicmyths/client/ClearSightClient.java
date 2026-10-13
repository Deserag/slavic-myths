package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import org.slavicmyths.equipment.*;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ClearSightClient {
    private static final Map<Integer,UUID> TARGETS=new HashMap<>();
    private static int remaining;
    private static ResourceLocation dimension;
    private static net.minecraft.client.multiplayer.ClientLevel origin;
    @SubscribeEvent public static void received(ClearSightNetwork.Received event){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!mc.level.dimension().location().equals(event.payload.dimension()))return;TARGETS.clear();for(var t:event.payload.targets())TARGETS.put(t.id(),t.uuid());remaining=UniqueEquipmentRules.SIGHT_DURATION;dimension=event.payload.dimension();origin=mc.level;}
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(remaining<=0)return;if(mc.level!=origin||mc.player==null||!mc.player.isAlive()||!mc.player.getItemBySlot(EquipmentSlot.HEAD).is(UniqueEquipment.CLEAR_SIGHT_HELMET.get())||--remaining<=0){remaining=0;TARGETS.clear();origin=null;}}
    public static boolean outlined(Entity entity){var mc=Minecraft.getInstance();return remaining>0&&mc.level==origin&&mc.player!=null&&mc.player.isAlive()&&mc.player.getItemBySlot(EquipmentSlot.HEAD).is(UniqueEquipment.CLEAR_SIGHT_HELMET.get())&&mc.level.dimension().location().equals(dimension)&&entity.getUUID().equals(TARGETS.get(entity.getId()))&&entity.distanceToSqr(mc.player)<=576;}
    @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event){if(event.getItemStack().is(UniqueEquipment.CLEAR_SIGHT_HELMET.get()))event.getToolTip().add(net.minecraft.network.chat.Component.translatable("unique.slavicmyths.sight_key",RareRuneClient.USE.getTranslatedKeyMessage()));}
    private ClearSightClient() { }
}
