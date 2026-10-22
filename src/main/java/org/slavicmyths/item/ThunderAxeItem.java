package org.slavicmyths.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModItems;

/** Adds the thunder pulse to one accepted melee hit without a second invulnerability-blocked hurt. */
@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class ThunderAxeItem extends AxeItem {
    private static final float THUNDER_CHANCE = 0.2F;
    private static final float BONUS_DAMAGE = 1.0F;
    private static final int THUNDER_COOLDOWN = 60;

    public ThunderAxeItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(AxeItem.createAttributes(tier, 5.0F, -3.1F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return super.hurtEnemy(stack, target, attacker);
    }

    @SubscribeEvent
    public static void onAcceptedMeleeDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel world)
                || event.getNewDamage() <= 0.0F
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getDirectEntity() instanceof ServerPlayer player)
                || player.getMainHandItem().getItem() != ModItems.THUNDER_AXE.get()
                || player.getCooldowns().isOnCooldown(ModItems.THUNDER_AXE.get())
                || player.getRandom().nextFloat() >= THUNDER_CHANCE) {
            return;
        }

        event.setNewDamage(event.getNewDamage() + BONUS_DAMAGE);
        player.getCooldowns().addCooldown(ModItems.THUNDER_AXE.get(), THUNDER_COOLDOWN);
        LivingEntity target = event.getEntity();
        world.sendParticles(ParticleTypes.ENCHANT, target.getX(),
                target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                8, 0.25, 0.3, 0.25, 0.02);
        world.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(),
                SoundSource.PLAYERS, 0.25F, 1.4F);
    }
}
