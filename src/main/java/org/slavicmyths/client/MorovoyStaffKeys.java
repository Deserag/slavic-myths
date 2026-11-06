package org.slavicmyths.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.morovoy.MorovoyStaff;
import org.slavicmyths.network.MythPayloads;

/** Client bindings are reconfigurable; the server validates every skill request. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class MorovoyStaffKeys {
    private static final KeyMapping MODE=new KeyMapping("key.slavicmyths.morovoy_mode",InputConstants.Type.KEYSYM,82,"key.categories.slavicmyths");
    private static final KeyMapping RAVENS=new KeyMapping("key.slavicmyths.morovoy_ravens",InputConstants.Type.KEYSYM,74,"key.categories.slavicmyths");
    private static final KeyMapping BLAST=new KeyMapping("key.slavicmyths.morovoy_blast",InputConstants.Type.KEYSYM,75,"key.categories.slavicmyths");

    public static final class Registration {
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent e) {
            e.register(MODE);
            e.register(RAVENS);
            e.register(BLAST);
        }
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        var mc=net.minecraft.client.Minecraft.getInstance();
        if(mc.player==null||mc.screen!=null) return;
        boolean staff=mc.player.getMainHandItem().getItem() instanceof MorovoyStaff.Staff;
        while(MODE.consumeClick()) if(staff) PacketDistributor.sendToServer(new MythPayloads.StaffAction(0));
        while(RAVENS.consumeClick()) if(staff) PacketDistributor.sendToServer(new MythPayloads.StaffAction(1));
        while(BLAST.consumeClick()) if(staff) PacketDistributor.sendToServer(new MythPayloads.StaffAction(2));
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent e) {
        if(!(e.getItemStack().getItem() instanceof MorovoyStaff.Staff))return;
        var text=e.getToolTip();
        text.add(Component.literal("ПКМ — магический заряд  •  "+MODE.getTranslatedKeyMessage().getString()+" — смена стихии").withStyle(ChatFormatting.GRAY));
        text.add(Component.literal(RAVENS.getTranslatedKeyMessage().getString()+" — стая воронов  •  "+BLAST.getTranslatedKeyMessage().getString()+" — огненно-моровой удар").withStyle(ChatFormatting.AQUA));
    }

    private MorovoyStaffKeys(){}
}
