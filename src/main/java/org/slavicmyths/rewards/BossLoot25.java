package org.slavicmyths.rewards;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.slavicmyths.morovoy.MorovoyFireZmey;
import org.slavicmyths.morovoy.MorovoyFireZmeyContent;
import org.slavicmyths.morovoy.MorovoyRaven;
import org.slavicmyths.morovoy.MorovoyRavenContent;
import org.slavicmyths.ninemor.NineMorSerpent;
import org.slavicmyths.sisters.AgneyaBoss;
import org.slavicmyths.sisters.LedeyaBoss;
import org.slavicmyths.sisters.LomeyaBoss;
import org.slavicmyths.sisters.NeveyaBoss;
import org.slavicmyths.sisters.SisterBoss;
import org.slavicmyths.sisters.SistersContent;
import org.slavicmyths.sisters.ViyBoss;

/** Additional 2.5 crafting rewards. Existing boss ledgers and quest tokens remain untouched. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BossLoot25 {
    private static final String AWARDED = "SlavicBossLoot25Awarded";

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity().level() instanceof ServerLevel)) return;
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer)
                && !(victim.getKillCredit() instanceof ServerPlayer)) return;
        if (victim.getPersistentData().getBoolean(AWARDED)) return;

        Item material = null;
        Item bonus = null;
        int tier = 4;
        if (victim instanceof SisterBoss sister) {
            material = (sister.isTryaseya() ? SistersContent.TRYASEYA_INGOT : SistersContent.GNETEYA_INGOT).get();
        } else if (victim instanceof LedeyaBoss) {
            material = SistersContent.LEDEYA_INGOT.get();
        } else if (victim instanceof AgneyaBoss) {
            material = SistersContent.AGNEYA_INGOT.get();
        } else if (victim instanceof NeveyaBoss) {
            material = SistersContent.NEVEYA_INGOT.get();
            tier = 5;
        } else if (victim instanceof LomeyaBoss) {
            material = SistersContent.LOMEYA_INGOT.get();
            tier = 5;
        } else if (victim instanceof MorovoyRaven raven && !raven.minion()) {
            material = MorovoyRavenContent.FEATHER.get();
            bonus = MorovoyRavenContent.CORE.get();
        } else if (victim instanceof MorovoyFireZmey) {
            material = MorovoyFireZmeyContent.BONE.get();
            bonus = MorovoyFireZmeyContent.CORE.get();
        } else if (victim instanceof ViyBoss) {
            victim.getPersistentData().putBoolean(AWARDED, true);
            drop(victim, SistersContent.NETHER_SOUL_IV.get(), 2 + victim.getRandom().nextInt(2));
            if (victim.getRandom().nextFloat() < .60F) drop(victim, SistersContent.NETHER_SOUL_V.get(), 1);
            return;
        } else if (victim instanceof NineMorSerpent) {
            victim.getPersistentData().putBoolean(AWARDED, true);
            drop(victim, SistersContent.NETHER_SOUL_V.get(), 2 + victim.getRandom().nextInt(2));
            return;
        }
        if (material == null) return;

        victim.getPersistentData().putBoolean(AWARDED, true);
        drop(victim, material, 2 + victim.getRandom().nextInt(2));
        if (victim.getRandom().nextFloat() < .70F) drop(victim, bonus != null ? bonus : material, 1);
        if (victim instanceof SisterBoss sister && sister.isTryaseya() && victim.getRandom().nextFloat()<.30F)drop(victim,org.slavicmyths.sisters.TremorRune.ITEM.get(),1);
        if (victim.getRandom().nextFloat() < (tier == 5 ? .85F : .65F)) drop(victim, SistersContent.NETHER_SOUL_IV.get(), 1);
        if (tier == 5 && victim.getRandom().nextFloat() < .40F) drop(victim, SistersContent.NETHER_SOUL_V.get(), 1);
    }

    private static void drop(LivingEntity victim, Item item, int amount) {
        victim.spawnAtLocation(new ItemStack(item, amount));
    }

    private BossLoot25() {}
}
