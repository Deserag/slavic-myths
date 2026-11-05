package org.slavicmyths.ninemor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/** Server-authoritative ritual: consume only when the boss was spawned. */
public final class NineMorSealItem extends Item {
    public NineMorSealItem(Properties properties) { super(properties); }

    @Override
    public net.minecraft.network.chat.Component getName(net.minecraft.world.item.ItemStack stack) {
        return net.minecraft.network.chat.Component.literal("Печать Девяти Моров");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null || !server.getBlockState(pos).canBeReplaced()
                || !server.getBlockState(pos.above()).canBeReplaced()
                || !server.getWorldBorder().isWithinBounds(pos)) return InteractionResult.FAIL;
        AABB clearance = new AABB(pos).inflate(3.0D, 3.0D, 3.0D);
        // A player holding the seal must never block their own ritual.
        if (!server.getEntitiesOfClass(NineMorSerpent.class, clearance.inflate(80D)).isEmpty()) {
            return InteractionResult.FAIL;
        }

        NineMorSerpent boss = NineMorContent.BOSS.get().create(server);
        if (boss == null) return InteractionResult.FAIL;
        boss.moveTo(pos.getX() + .5D, pos.getY(), pos.getZ() + .5D, player.getYRot(), 0.0F);
        if (!server.addFreshEntity(boss)) return InteractionResult.FAIL;
        boss.beginRitual();
        for (int i = 0; i < 9; i++) {
            double a = Math.PI * 2 * i / 9D;
            double x = pos.getX() + .5D + Math.cos(a) * 4.0D;
            double z = pos.getZ() + .5D + Math.sin(a) * 4.0D;
            int color = NineMorSerpent.COLORS[i];
            server.sendParticles(new DustParticleOptions(
                new Vector3f(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F), 1.4F),
                x, pos.getY() + .18D, z, 20, .35, .05, .35, .02);
        }
        server.playSound(null, pos, NineMorContent.SUMMON_SOUND.get(), SoundSource.HOSTILE, 2.0F, .85F);
        if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        player.getCooldowns().addCooldown(this, 30);
        return InteractionResult.CONSUME;
    }
}
