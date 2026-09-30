package org.slavicmyths.item;

import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Bounded ray pulse on use; no projectile, terrain damage, or tick scan. */
public final class StormStaffItem extends Item {
    public StormStaffItem(Properties properties) { super(properties); }
    @Override public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return ActionResult.fail(stack);
        if (!world.isClientSide) {
            Vector3d start = player.getEyePosition(1.0F); Vector3d look = player.getLookAngle();
            AxisAlignedBB area = player.getBoundingBox().expandTowards(look.scale(7)).inflate(1.0);
            List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, area,
                    e -> org.slavicmyths.rpg.Abilities.canHit(player,e) && world.clip(new RayTraceContext(start, e.getEyePosition(1.0F),
                            RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player)).getType() == RayTraceResult.Type.MISS &&
                            e.position().subtract(start).normalize().dot(look) > 0.93);
            targets.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(player))).ifPresent(
                    e -> {e.hurt(DamageSource.indirectMagic(player,player),3.0F*(org.slavicmyths.rpg.PathData.has(player,"staff_power")?1.12F:1)*(org.slavicmyths.rpg.Runes.has(stack,"thunder")?1.15F:1));org.slavicmyths.rpg.RuneEffects.staff(player,stack,e);});
            ServerWorld server = (ServerWorld) world;
            for (int i = 1; i <= 7; i++) {
                Vector3d point = start.add(look.scale(i));
                server.sendParticles(ParticleTypes.ENCHANT, point.x, point.y, point.z, 3, 0.12, 0.12, 0.12, 0.01);
            }
            world.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundCategory.PLAYERS, 0.25F, 1.7F);
            player.getCooldowns().addCooldown(this, ((org.slavicmyths.rpg.Runes.has(stack,"midday") && org.slavicmyths.rpg.RuneEffects.day(player)) || (org.slavicmyths.rpg.Runes.has(stack,"shadow") && org.slavicmyths.rpg.RuneEffects.dark(player)))?72:80);
            if (!player.abilities.instabuild) stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide);
    }
}
