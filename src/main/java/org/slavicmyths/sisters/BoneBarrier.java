package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Timed wall controller. Its three solid bone blocks, not entity velocity pushes, stop players. */
public final class BoneBarrier extends Entity {
    private UUID owner;
    private int remaining = 20 * 7;
    private int warningTicks;
    private boolean placed;

    public BoneBarrier(EntityType<? extends BoneBarrier> type, Level level) { super(type, level); }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }
    public void bind(SisterBoss boss, int delay) { owner = boss.getUUID(); warningTicks = Math.max(8, delay + 8); remaining = 20 * 7 + warningTicks; }
    @Override public boolean isPickable() { return false; }
    @Override public boolean canBeCollidedWith() { return false; }

    private BlockPos base() { return BlockPos.containing(getX(), getY(), getZ()); }

    private boolean place(ServerLevel world) {
        BlockPos base = base();
        for (int y = 0; y < 3; y++) {
            BlockPos pos = base.above(y);
            if (world.isOutsideBuildHeight(pos) || !world.hasChunkAt(pos) || !world.getBlockState(pos).isAir()) return false;
            if (!world.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(.12), LivingEntity::isAlive).isEmpty()) return false;
        }
        for (int y = 0; y < 3; y++) {
            if (!world.setBlock(base.above(y), Blocks.BONE_BLOCK.defaultBlockState(), 3)) {
                for (int undo = 0; undo < y; undo++) world.removeBlock(base.above(undo), false);
                return false;
            }
        }
        return true;
    }

    private void clear(ServerLevel world) {
        if (!placed) return;
        BlockPos base = base();
        for (int y = 0; y < 3; y++) {
            BlockPos pos = base.above(y);
            if (world.hasChunkAt(pos) && world.getBlockState(pos).is(Blocks.BONE_BLOCK)) world.removeBlock(pos, false);
        }
        placed = false;
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel world)) return;
        if (remaining-- <= 0 || owner == null || !(world.getEntity(owner) instanceof SisterBoss boss) || !boss.isAlive()) { discard(); return; }
        if (!placed) {
            if (warningTicks-- > 0) {
                if (tickCount % 3 == 0) world.sendParticles(ParticleTypes.CRIT, getX(), getY() + .08, getZ(), 4, .35, .02, .35, .01);
                return;
            }
            if (!place(world)) { discard(); return; }
            placed = true;
        }
    }

    @Override public void remove(RemovalReason reason) {
        if ((reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED) && level() instanceof ServerLevel world) clear(world);
        super.remove(reason);
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Remaining", remaining);
        tag.putInt("WarningTicks", warningTicks);
        tag.putBoolean("Placed", placed);
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        remaining = Math.clamp(tag.getInt("Remaining"), 0, 20 * 10);
        warningTicks = Math.clamp(tag.getInt("WarningTicks"), 0, 40);
        placed = tag.getBoolean("Placed");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
