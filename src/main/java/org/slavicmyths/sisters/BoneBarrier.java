package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Temporary server-enforced collidable segment; never edits, replaces or saves terrain blocks. */
public final class BoneBarrier extends Entity {
    private UUID owner;
    private int remaining = 20*7;
    private boolean emerging;
    public BoneBarrier(EntityType<? extends BoneBarrier> type,Level level) { super(type,level);noPhysics=true; }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }
    public void bind(SisterBoss boss,int delay) { owner=boss.getUUID();emerging=delay>0;remaining=20*7+delay; }
    @Override public boolean isPickable() { return true; }
    @Override public boolean canBeCollidedWith() { return true; }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if(tickCount%6==0) level().addParticle(net.minecraft.core.particles.ParticleTypes.CRIT,getX(),getY()+.15,getZ(),0,.02,0);
            return;
        }
        if (remaining--<=0 || owner==null || !(level() instanceof ServerLevel server) || !(server.getEntity(owner) instanceof SisterBoss boss) || !boss.isAlive()) { discard();return; }
        if (emerging && tickCount<8) return;
        emerging=false;
        AABB collision=getBoundingBox().inflate(.09,0,.09);
        for (Player player : level().getEntitiesOfClass(Player.class,collision,p -> p.isAlive() && !p.isSpectator())) {
            double dx=player.getX()-getX(),dz=player.getZ()-getZ();
            if (Math.abs(dx)>Math.abs(dz)) player.setDeltaMovement(player.getDeltaMovement().add(Math.copySign(.32,dx==0?1:dx),.05,0));
            else player.setDeltaMovement(player.getDeltaMovement().add(0,.05,Math.copySign(.32,dz==0?1:dz)));
            player.hurtMarked=true;
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putInt("Remaining",remaining);if(owner!=null)tag.putUUID("Owner",owner); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { remaining=Math.clamp(tag.getInt("Remaining"),0,20*10);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null; }
}
