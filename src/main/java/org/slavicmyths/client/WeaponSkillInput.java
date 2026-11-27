package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.combat.WeaponSkillBindings;
import org.slavicmyths.karachun.NightContent;
import org.slavicmyths.karachun.NightNetwork;

import org.slavicmyths.marine.MarineWeapons;
import org.slavicmyths.morovoy.MorovoyStaff;
import org.slavicmyths.morozko.FrostContent;
import org.slavicmyths.morozko.FrostNetwork;
import org.slavicmyths.network.MythPayloads;

/** One client entry point for secondary weapon attacks (Shift+LMB). */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WeaponSkillInput {
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        var mc=Minecraft.getInstance();
        if(!event.isAttack() || mc.player==null || mc.screen!=null || !mc.player.isAlive()
                || mc.player.isSpectator() || !mc.player.isShiftKeyDown()) return;
        var held=mc.player.getMainHandItem();
        if(!WeaponSkillBindings.shiftLeftSkill(held)) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if(held.getItem() instanceof org.slavicmyths.artifact.VelesStaffItem staff && staff.steppe()) {
            PacketDistributor.sendToServer(new org.slavicmyths.artifact.VelesStaffNetwork.SteppeSummon());
        } else if(held.getItem() instanceof org.slavicmyths.sisters.GneteyaGreatHammer) {
            PacketDistributor.sendToServer(new org.slavicmyths.sisters.GneteyaHammerNetwork.BoneFracture());
        } else if(held.getItem() instanceof org.slavicmyths.gorynych.SerpentCrossbow) {
            PacketDistributor.sendToServer(new org.slavicmyths.gorynych.CrossbowVolley.Toggle());

        }
    }
    private WeaponSkillInput() {}
}
