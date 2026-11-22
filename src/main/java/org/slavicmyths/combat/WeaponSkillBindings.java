package org.slavicmyths.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slavicmyths.karachun.NightContent;
import org.slavicmyths.marine.MarineWeapons;
import org.slavicmyths.morovoy.MorovoyStaff;
import org.slavicmyths.morozko.FrostContent;

/** The inventory of weapons whose Shift+LMB is a skill, never an ordinary attack. */
@EventBusSubscriber(modid="slavicmyths")
public final class WeaponSkillBindings {
    public static boolean shiftLeftSkill(ItemStack stack) {
        return stack.is(MarineWeapons.STAFF.get())
            || stack.is(MorovoyStaff.STAFF.get())
            || stack.is(FrostContent.STAFF.get())
            || stack.is(NightContent.SCYTHE.get());
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void preventOrdinaryEntityAttack(AttackEntityEvent event) {
        if(event.getEntity() instanceof ServerPlayer player
                && player.isShiftKeyDown() && shiftLeftSkill(player.getMainHandItem()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void preventOrdinaryBlockAttack(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getEntity() instanceof ServerPlayer player
                && player.isShiftKeyDown() && shiftLeftSkill(player.getMainHandItem()))
            event.setCanceled(true);
    }

    private WeaponSkillBindings() {}
}
