package org.slavicmyths.item;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/** Bounded ray pulse on use; no projectile, terrain damage, or tick scan. */
public final class StormStaffItem extends Item {
    public StormStaffItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!world.isClientSide) {
            Vec3 start = player.getEyePosition(1.0F); Vec3 look = player.getLookAngle();
            AABB area = player.getBoundingBox().expandTowards(look.scale(7)).inflate(1.0);
            List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area,
                    e -> org.slavicmyths.rpg.Abilities.canHit(player,e) && world.clip(new ClipContext(start, e.getEyePosition(1.0F),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS &&
                            e.position().subtract(start).normalize().dot(look) > 0.93);
            targets.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(player))).ifPresent(
                    e -> {e.hurt(player.damageSources().indirectMagic(player,player),3.0F*(org.slavicmyths.rpg.PathData.has(player,"staff_power")?1.12F:1)*(org.slavicmyths.rpg.Runes.has(stack,"thunder")?1.15F:1));org.slavicmyths.rpg.RuneEffects.staff(player,stack,e);});
            ServerLevel server = (ServerLevel) world;
            for (int i = 1; i <= 7; i++) {
                Vec3 point = start.add(look.scale(i));
                server.sendParticles(ParticleTypes.ENCHANT, point.x, point.y, point.z, 3, 0.12, 0.12, 0.12, 0.01);
            }
            world.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.25F, 1.7F);
            player.getCooldowns().addCooldown(this, ((org.slavicmyths.rpg.Runes.has(stack,"midday") && org.slavicmyths.rpg.RuneEffects.day(player)) || (org.slavicmyths.rpg.Runes.has(stack,"shadow") && org.slavicmyths.rpg.RuneEffects.dark(player)))?72:80);
            if (!player.getAbilities().instabuild) stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
