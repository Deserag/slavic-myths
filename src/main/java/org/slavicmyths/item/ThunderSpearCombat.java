package org.slavicmyths.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModItems;

/** Add the discharge to the accepted melee hit, so hurt invulnerability cannot swallow it. */
@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class ThunderSpearCombat {
    @SubscribeEvent public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving().level instanceof ServerWorld) || event.getAmount() <= 0
                || !(event.getSource().getDirectEntity() instanceof PlayerEntity)
                || !"player".equals(event.getSource().getMsgId())) return;
        PlayerEntity player = (PlayerEntity) event.getSource().getDirectEntity();
        if (player.getMainHandItem().getItem() != ModItems.THUNDER_SPEAR.get()
                || player.getCooldowns().isOnCooldown(ModItems.THUNDER_SPEAR.get())) return;
        event.setAmount(event.getAmount() + 2.0F);
        player.getCooldowns().addCooldown(ModItems.THUNDER_SPEAR.get(), 120);
        ServerWorld world = (ServerWorld) player.level;
        world.sendParticles(ParticleTypes.ENCHANT, event.getEntityLiving().getX(),
                event.getEntityLiving().getY() + 0.8, event.getEntityLiving().getZ(), 10, 0.2, 0.3, 0.2, 0.02);
        world.playSound(null, event.getEntityLiving().blockPosition(), SoundEvents.TRIDENT_THUNDER,
                SoundCategory.PLAYERS, 0.25F, 1.6F);
    }
    private ThunderSpearCombat() { }
}
