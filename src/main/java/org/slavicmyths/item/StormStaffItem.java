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
    public static final float BASE_PULSE_DAMAGE=6.0F;
    public static final int PULSE_COOLDOWN=54;
    public static final int RUNE_COOLDOWN=48;
    public static float pulseDamage(boolean staffPower,boolean thunderRune) {
        return BASE_PULSE_DAMAGE*(staffPower?1.12F:1)*(thunderRune?1.15F:1);
    }
    public static int cooldownTicks(boolean activeRune) { return activeRune?RUNE_COOLDOWN:PULSE_COOLDOWN; }
    public StormStaffItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!world.isClientSide) {
            Vec3 start = player.getEyePosition(1.0F); Vec3 look = player.getLookAngle();
            AABB area = player.getBoundingBox().expandTowards(look.scale(7)).inflate(1.0);
            List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e.distanceToSqr(player) <= 49.0 && org.slavicmyths.rpg.Abilities.canHit(player,e) && world.clip(new ClipContext(start, e.getEyePosition(1.0F),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS &&
                            e.position().subtract(start).normalize().dot(look) > 0.93);
            var selected = targets.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            selected.ifPresent(e -> {
                if(e.hurt(player.damageSources().indirectMagic(player,player),pulseDamage(org.slavicmyths.rpg.PathData.has(player,"staff_power"),org.slavicmyths.rpg.Runes.has(stack,"thunder")))){
                    ServerLevel level=(ServerLevel)world;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,e.getX(),e.getY()+e.getBbHeight()*.5,e.getZ(),12,.22,.25,.22,.02);
                    org.slavicmyths.rpg.RuneEffects.staff(player,stack,e);
                }
            });
            ServerLevel server = (ServerLevel) world;
            Vec3 impact = selected.map(e -> e.position().add(0,e.getBbHeight()*.5,0)).orElseGet(() -> world.clip(new ClipContext(start,start.add(look.scale(7)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getLocation());
            for (int i = 1; i <= 14; i++) {
                Vec3 point = start.lerp(impact,i/14.0);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK,point.x,point.y,point.z,4,.10,.10,.10,.035);
            }
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK,impact.x,impact.y,impact.z,42,.4,.4,.4,.12);
            server.sendParticles(ParticleTypes.ENCHANT,impact.x,impact.y,impact.z,12,.3,.35,.3,.05);
            var bolt=net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(server);
            if(bolt!=null){bolt.setVisualOnly(true);bolt.setPos(impact.x,impact.y,impact.z);server.addFreshEntity(bolt);}
            world.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1F, .9F);
            player.getCooldowns().addCooldown(this, cooldownTicks((org.slavicmyths.rpg.Runes.has(stack,"midday") && org.slavicmyths.rpg.RuneEffects.day(player)) || (org.slavicmyths.rpg.Runes.has(stack,"shadow") && org.slavicmyths.rpg.RuneEffects.dark(player))));
            if (!player.getAbilities().instabuild) stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
